package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.exception.OpenCacheException;
import cloud.opencode.base.cache.protection.Bulkhead;
import cloud.opencode.base.cache.protection.CircuitBreaker;
import cloud.opencode.base.cache.spi.CacheLoader;
import cloud.opencode.base.cache.spi.RetryPolicy;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Resilient Cache Loader - Wraps loaders with retry, circuit breaker, bulkhead, and timeout
 * 弹性缓存加载器 - 用重试、熔断、舱壁和超时包装加载器
 *
 * <p>Provides comprehensive resilience patterns for cache loading operations:</p>
 * <p>为缓存加载操作提供全面的弹性模式：</p>
 * <ul>
 *   <li><strong>Retry</strong> - Automatic retry with configurable backoff | 带可配置退避的自动重试</li>
 *   <li><strong>Circuit Breaker</strong> - Fail fast when backend is down | 后端故障时快速失败</li>
 *   <li><strong>Bulkhead</strong> - Limit concurrent loads | 限制并发加载</li>
 *   <li><strong>Timeout</strong> - Fail if load takes too long | 加载超时则失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create resilient loader with all patterns
 * Function<String, User> baseLoader = key -> userService.findById(key);
 *
 * Function<String, User> resilientLoader = ResilientCacheLoader.<String, User>builder()
 *     .loader(baseLoader)
 *     .retry(RetryPolicy.exponentialBackoffWithJitter(3,
 *         Duration.ofMillis(100), Duration.ofSeconds(5)))
 *     .circuitBreaker(CircuitBreaker.builder()
 *         .failureThreshold(5)
 *         .resetTimeout(Duration.ofSeconds(30))
 *         .build())
 *     .bulkhead(Bulkhead.semaphore(10))
 *     .timeout(Duration.ofSeconds(5))
 *     .build();
 *
 * // Use with cache
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * User user = cache.get("user:1", resilientLoader);
 *
 * // Batch loader variant
 * Function<Set<String>, Map<String, User>> batchLoader =
 *     ResilientCacheLoader.<String, User>batchBuilder()
 *         .loader(keys -> userService.findAllByIds(keys))
 *         .retry(RetryPolicy.fixedDelay(2, Duration.ofMillis(200)))
 *         .build();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Retry with configurable backoff - 可配置退避的重试</li>
 *   <li>Circuit breaker integration - 熔断器集成</li>
 *   <li>Bulkhead concurrency limiting - 舱壁并发限制</li>
 *   <li>Timeout support - 超时支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.9.0
 */
public final class ResilientCacheLoader<K, V> implements Function<K, V> {

    private final Function<K, V> delegate;
    private final RetryPolicy retryPolicy;
    private final CircuitBreaker circuitBreaker;
    private final Bulkhead bulkhead;
    private final Duration timeout;
    private final Function<Throwable, V> fallback;

    private ResilientCacheLoader(Builder<K, V> builder) {
        this.delegate = builder.loader;
        this.retryPolicy = builder.retryPolicy;
        this.circuitBreaker = builder.circuitBreaker;
        this.bulkhead = builder.bulkhead;
        this.timeout = builder.timeout;
        this.fallback = builder.fallback;
    }

    /**
     * Create builder for single-key loader
     * 创建单键加载器构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    /**
     * Create builder for batch loader
     * 创建批量加载器构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return batch builder | 批量构建器
     */
    public static <K, V> BatchBuilder<K, V> batchBuilder() {
        return new BatchBuilder<>();
    }

    /**
     * Wrap a simple loader with default resilience settings
     * 使用默认弹性设置包装简单加载器
     *
     * @param loader the loader | 加载器
     * @param <K>    key type | 键类型
     * @param <V>    value type | 值类型
     * @return resilient loader | 弹性加载器
     */
    public static <K, V> Function<K, V> wrap(Function<K, V> loader) {
        return ResilientCacheLoader.<K, V>builder()
                .loader(loader)
                .retry(RetryPolicy.exponentialBackoffWithJitter(3,
                        Duration.ofMillis(100), Duration.ofSeconds(5)))
                .build();
    }

    @Override
    public V apply(K key) {
        return load(key);
    }

    /**
     * Load value with resilience patterns
     * 使用弹性模式加载值
     *
     * @param key the key | 键
     * @return loaded value | 加载的值
     */
    public V load(K key) {
        // Check circuit breaker first
        if (circuitBreaker != null && !circuitBreaker.allowRequest()) {
            return handleCircuitBreakerOpen(key);
        }

        // Acquire bulkhead permit
        if (bulkhead != null && !bulkhead.tryAcquire()) {
            return handleBulkheadRejection(key);
        }

        try {
            return executeWithRetry(key);
        } finally {
            if (bulkhead != null) {
                bulkhead.release();
            }
        }
    }

    private V executeWithRetry(K key) {
        int maxRetries = retryPolicy != null ? retryPolicy.maxRetries() : 0;
        Throwable lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0 && retryPolicy != null) {
                    Duration delay = retryPolicy.getDelay(attempt);
                    Thread.sleep(delay.toMillis());
                }

                V result = executeWithTimeout(key);

                // Record success
                if (circuitBreaker != null) {
                    circuitBreaker.recordSuccess();
                }

                return result;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LoaderInterruptedException("Load interrupted for key: " + key, e);
            } catch (Exception e) {
                lastException = e;

                // Record failure
                if (circuitBreaker != null) {
                    circuitBreaker.recordFailure();
                }

                // Check if we should retry this exception
                if (retryPolicy != null && !retryPolicy.shouldRetry(e)) {
                    break;
                }
            }
        }

        // All retries exhausted
        return handleLoadFailure(key, lastException);
    }

    private V executeWithTimeout(K key) {
        if (timeout == null) {
            return delegate.apply(key);
        }

        CompletableFuture<V> future = CompletableFuture.supplyAsync(() -> delegate.apply(key));
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new LoaderTimeoutException("Load timed out after " + timeout + " for key: " + key, e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new LoaderException("Load failed for key: " + key, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoaderInterruptedException("Load interrupted for key: " + key, e);
        }
    }

    private V handleCircuitBreakerOpen(K key) {
        if (fallback != null) {
            return fallback.apply(new CircuitBreakerOpenException("Circuit breaker is open for key: " + key));
        }
        throw new CircuitBreakerOpenException("Circuit breaker is open for key: " + key);
    }

    private V handleBulkheadRejection(K key) {
        if (fallback != null) {
            return fallback.apply(new BulkheadRejectedException("Bulkhead rejected load for key: " + key));
        }
        throw new BulkheadRejectedException("Bulkhead rejected load for key: " + key);
    }

    private V handleLoadFailure(K key, Throwable lastException) {
        if (fallback != null) {
            return fallback.apply(lastException);
        }
        if (lastException instanceof RuntimeException re) {
            throw re;
        }
        throw new LoaderException("Load failed after retries for key: " + key, lastException);
    }

    // ==================== Builder ====================

    /**
     * Builder for ResilientCacheLoader
     */
    public static class Builder<K, V> {
        private Function<K, V> loader;
        private RetryPolicy retryPolicy;
        private CircuitBreaker circuitBreaker;
        private Bulkhead bulkhead;
        private Duration timeout;
        private Function<Throwable, V> fallback;

        /**
         * Set the base loader function
         * 设置基础加载函数
         *
         * @param loader the loader | 加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(Function<K, V> loader) {
            this.loader = Objects.requireNonNull(loader, "loader cannot be null");
            return this;
        }

        /**
         * Set the base CacheLoader
         * 设置基础 CacheLoader
         *
         * @param loader the cache loader | 缓存加载器
         * @return this builder | 此构建器
         */
        public Builder<K, V> loader(CacheLoader<K, V> loader) {
            Objects.requireNonNull(loader, "loader cannot be null");
            this.loader = key -> {
                try {
                    return loader.load(key);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            return this;
        }

        /**
         * Set retry policy
         * 设置重试策略
         *
         * @param retryPolicy the policy | 策略
         * @return this builder | 此构建器
         */
        public Builder<K, V> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        /**
         * Set circuit breaker
         * 设置熔断器
         *
         * @param circuitBreaker the circuit breaker | 熔断器
         * @return this builder | 此构建器
         */
        public Builder<K, V> circuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }

        /**
         * Set bulkhead
         * 设置舱壁
         *
         * @param bulkhead the bulkhead | 舱壁
         * @return this builder | 此构建器
         */
        public Builder<K, V> bulkhead(Bulkhead bulkhead) {
            this.bulkhead = bulkhead;
            return this;
        }

        /**
         * Set operation timeout
         * 设置操作超时
         *
         * @param timeout the timeout | 超时
         * @return this builder | 此构建器
         */
        public Builder<K, V> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set fallback function
         * 设置回退函数
         *
         * @param fallback fallback for exceptions | 异常回退
         * @return this builder | 此构建器
         */
        public Builder<K, V> fallback(Function<Throwable, V> fallback) {
            this.fallback = fallback;
            return this;
        }

        /**
         * Set fallback value
         * 设置回退值
         *
         * @param value fallback value | 回退值
         * @return this builder | 此构建器
         */
        public Builder<K, V> fallbackValue(V value) {
            this.fallback = ex -> value;
            return this;
        }

        /**
         * Build the resilient loader
         * 构建弹性加载器
         *
         * @return resilient loader function | 弹性加载器函数
         */
        public Function<K, V> build() {
            Objects.requireNonNull(loader, "loader must be set");
            return new ResilientCacheLoader<>(this);
        }
    }

    // ==================== Batch Builder ====================

    /**
     * Builder for batch resilient loader
     */
    public static class BatchBuilder<K, V> {
        private Function<Set<? extends K>, Map<K, V>> loader;
        private RetryPolicy retryPolicy;
        private CircuitBreaker circuitBreaker;
        private Bulkhead bulkhead;
        private Duration timeout;

        /**
         * Set the batch loader function
         * 设置批量加载函数
         *
         * @param loader the loader | 加载器
         * @return this builder | 此构建器
         */
        public BatchBuilder<K, V> loader(Function<Set<? extends K>, Map<K, V>> loader) {
            this.loader = Objects.requireNonNull(loader, "loader cannot be null");
            return this;
        }

        /**
         * Set retry policy
         * 设置重试策略
         */
        public BatchBuilder<K, V> retry(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        /**
         * Set circuit breaker
         * 设置熔断器
         */
        public BatchBuilder<K, V> circuitBreaker(CircuitBreaker circuitBreaker) {
            this.circuitBreaker = circuitBreaker;
            return this;
        }

        /**
         * Set bulkhead
         * 设置舱壁
         */
        public BatchBuilder<K, V> bulkhead(Bulkhead bulkhead) {
            this.bulkhead = bulkhead;
            return this;
        }

        /**
         * Set operation timeout
         * 设置操作超时
         */
        public BatchBuilder<K, V> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Build the resilient batch loader
         * 构建弹性批量加载器
         *
         * @return resilient batch loader function | 弹性批量加载器函数
         */
        public Function<Set<? extends K>, Map<K, V>> build() {
            Objects.requireNonNull(loader, "loader must be set");
            return new ResilientBatchLoader<>(this);
        }
    }

    // ==================== Batch Loader Implementation ====================

    private static class ResilientBatchLoader<K, V> implements Function<Set<? extends K>, Map<K, V>> {
        private final Function<Set<? extends K>, Map<K, V>> delegate;
        private final RetryPolicy retryPolicy;
        private final CircuitBreaker circuitBreaker;
        private final Bulkhead bulkhead;
        private final Duration timeout;

        ResilientBatchLoader(BatchBuilder<K, V> builder) {
            this.delegate = builder.loader;
            this.retryPolicy = builder.retryPolicy;
            this.circuitBreaker = builder.circuitBreaker;
            this.bulkhead = builder.bulkhead;
            this.timeout = builder.timeout;
        }

        @Override
        public Map<K, V> apply(Set<? extends K> keys) {
            if (circuitBreaker != null && !circuitBreaker.allowRequest()) {
                throw new CircuitBreakerOpenException("Circuit breaker is open for batch load");
            }

            if (bulkhead != null && !bulkhead.tryAcquire()) {
                throw new BulkheadRejectedException("Bulkhead rejected batch load");
            }

            try {
                return executeWithRetry(keys);
            } finally {
                if (bulkhead != null) {
                    bulkhead.release();
                }
            }
        }

        private Map<K, V> executeWithRetry(Set<? extends K> keys) {
            int maxRetries = retryPolicy != null ? retryPolicy.maxRetries() : 0;
            Throwable lastException = null;

            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    if (attempt > 0 && retryPolicy != null) {
                        Duration delay = retryPolicy.getDelay(attempt);
                        Thread.sleep(delay.toMillis());
                    }

                    Map<K, V> result = timeout != null
                            ? executeWithTimeout(keys)
                            : delegate.apply(keys);

                    if (circuitBreaker != null) {
                        circuitBreaker.recordSuccess();
                    }
                    return result;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new LoaderInterruptedException("Batch load interrupted", e);
                } catch (Exception e) {
                    lastException = e;
                    if (circuitBreaker != null) {
                        circuitBreaker.recordFailure();
                    }
                    if (retryPolicy != null && !retryPolicy.shouldRetry(e)) {
                        break;
                    }
                }
            }

            if (lastException instanceof RuntimeException re) {
                throw re;
            }
            throw new LoaderException("Batch load failed after retries", lastException);
        }

        private Map<K, V> executeWithTimeout(Set<? extends K> keys) {
            CompletableFuture<Map<K, V>> future = CompletableFuture.supplyAsync(() -> delegate.apply(keys));
            try {
                return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                future.cancel(true);
                throw new LoaderTimeoutException("Batch load timed out after " + timeout, e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new LoaderException("Batch load failed", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LoaderInterruptedException("Batch load interrupted", e);
            }
        }
    }

    // ==================== Exceptions ====================

    /**
     * Base exception for loader failures
     */
    public static class LoaderException extends OpenCacheException {
        public LoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception when circuit breaker is open
     */
    public static class CircuitBreakerOpenException extends LoaderException {
        public CircuitBreakerOpenException(String message) {
            super(message, null);
        }
    }

    /**
     * Exception when bulkhead rejects the request
     */
    public static class BulkheadRejectedException extends LoaderException {
        public BulkheadRejectedException(String message) {
            super(message, null);
        }
    }

    /**
     * Exception when load times out
     */
    public static class LoaderTimeoutException extends LoaderException {
        public LoaderTimeoutException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception when load is interrupted
     */
    public static class LoaderInterruptedException extends LoaderException {
        public LoaderInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
