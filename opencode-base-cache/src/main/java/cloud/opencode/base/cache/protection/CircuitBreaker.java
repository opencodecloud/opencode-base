package cloud.opencode.base.cache.protection;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Circuit Breaker - Cascade failure prevention for cache backends
 * 熔断器 - 防止缓存后端级联故障
 *
 * <p>Protects cache backends from cascade failures by failing fast when error rate is high.</p>
 * <p>当错误率过高时快速失败，保护缓存后端免受级联故障。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three states: CLOSED, OPEN, HALF_OPEN - 三种状态：关闭、打开、半开</li>
 *   <li>Failure threshold triggering - 失败阈值触发</li>
 *   <li>Automatic recovery - 自动恢复</li>
 *   <li>Fallback support - 降级支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CircuitBreaker breaker = CircuitBreaker.create(
 *     new CircuitBreaker.Config(5, Duration.ofSeconds(30), 3, 0.5));
 *
 * User user = breaker.execute(
 *     () -> loadFromDb(key),
 *     () -> loadFromCache(key));  // fallback
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) state check - 时间复杂度: O(1) 状态检查</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class CircuitBreaker {

    private final Config config;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicInteger halfOpenAttempts = new AtomicInteger(0);

    /**
     * Circuit breaker state
     * 熔断器状态
     */
    public enum State {
        /**
         * Closed - Normal operation, requests pass through
         * 关闭 - 正常运行，请求通过
         */
        CLOSED,

        /**
         * Open - Failing fast, requests rejected
         * 打开 - 快速失败，请求被拒绝
         */
        OPEN,

        /**
         * Half-Open - Testing recovery, limited requests allowed
         * 半开 - 测试恢复，允许有限请求
         */
        HALF_OPEN
    }

    /**
     * Circuit breaker configuration
     * 熔断器配置
     *
     * @param failureThreshold     failures to trigger open | 触发打开的失败次数
     * @param openDuration         time to stay open | 保持打开的时间
     * @param halfOpenRequests     requests allowed in half-open | 半开状态允许的请求数
     * @param failureRateThreshold failure rate to trigger open | 触发打开的失败率
     */
    public record Config(
            int failureThreshold,
            Duration openDuration,
            int halfOpenRequests,
            double failureRateThreshold
    ) {
        /**
         * builder | builder
         * @return the result | 结果
         */
        public static Builder builder() {
            return new Builder();
        }

        /** public static class Builder */
        public static class Builder {

            /** Creates a new Builder instance | 创建新的 Builder 实例 */
            public Builder() {}
            private int failureThreshold = 5;
            private Duration openDuration = Duration.ofSeconds(30);
            private int halfOpenRequests = 3;
            private double failureRateThreshold = 0.5;

            /**
             * failureThreshold | failureThreshold
             * @param threshold the threshold | threshold
             * @return the result | 结果
             */
            public Builder failureThreshold(int threshold) {
                this.failureThreshold = threshold;
                return this;
            }

            /**
             * openDuration | openDuration
             * @param duration the duration | duration
             * @return the result | 结果
             */
            public Builder openDuration(Duration duration) {
                this.openDuration = duration;
                return this;
            }

            /**
             * halfOpenRequests | halfOpenRequests
             * @param requests the requests | requests
             * @return the result | 结果
             */
            public Builder halfOpenRequests(int requests) {
                this.halfOpenRequests = requests;
                return this;
            }

            /**
             * failureRateThreshold | failureRateThreshold
             * @param threshold the threshold | threshold
             * @return the result | 结果
             */
            public Builder failureRateThreshold(double threshold) {
                this.failureRateThreshold = threshold;
                return this;
            }

            /**
             * build | build
             * @return the result | 结果
             */
            public Config build() {
                return new Config(failureThreshold, openDuration, halfOpenRequests, failureRateThreshold);
            }
        }
    }

    private CircuitBreaker(Config config) {
        this.config = config;
    }

    /**
     * Create circuit breaker with config
     * 使用配置创建熔断器
     *
     * @param config the config | 配置
     * @return circuit breaker | 熔断器
     */
    public static CircuitBreaker create(Config config) {
        return new CircuitBreaker(config);
    }

    /**
     * Create circuit breaker with defaults
     * 使用默认配置创建熔断器
     *
     * @return circuit breaker | 熔断器
     */
    public static CircuitBreaker create() {
        return new CircuitBreaker(Config.builder().build());
    }

    /**
     * Execute operation with circuit breaker protection
     * 在熔断器保护下执行操作
     *
     * @param supplier the operation | 操作
     * @param <T>      result type | 结果类型
     * @return result | 结果
     * @throws CircuitBreakerOpenException if circuit is open | 熔断器打开时抛出异常
     */
    public <T> T execute(Supplier<T> supplier) {
        if (!allowRequest()) {
            throw new CircuitBreakerOpenException("Circuit breaker is open");
        }

        try {
            T result = supplier.get();
            recordSuccess();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }

    /**
     * Execute operation with fallback
     * 执行操作并带降级
     *
     * @param supplier the operation | 操作
     * @param fallback the fallback | 降级操作
     * @param <T>      result type | 结果类型
     * @return result | 结果
     */
    public <T> T execute(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return execute(supplier);
        } catch (CircuitBreakerOpenException e) {
            return fallback.get();
        }
    }

    /**
     * Execute async operation
     * 异步执行操作
     *
     * @param supplier the async operation | 异步操作
     * @param <T>      result type | 结果类型
     * @return future containing result | 包含结果的 Future
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<CompletableFuture<T>> supplier) {
        if (!allowRequest()) {
            return CompletableFuture.failedFuture(
                    new CircuitBreakerOpenException("Circuit breaker is open"));
        }

        return supplier.get()
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        recordFailure();
                    } else {
                        recordSuccess();
                    }
                });
    }

    /**
     * Get current state
     * 获取当前状态
     *
     * @return state | 状态
     */
    public State getState() {
        return state.get();
    }

    /**
     * Manually open circuit
     * 手动打开熔断器
     */
    public void open() {
        state.set(State.OPEN);
        lastFailureTime.set(System.currentTimeMillis());
    }

    /**
     * Manually close circuit
     * 手动关闭熔断器
     */
    public void close() {
        state.set(State.CLOSED);
        reset();
    }

    /**
     * Reset statistics
     * 重置统计
     */
    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        halfOpenAttempts.set(0);
    }

    // ==================== Request Control Methods ====================

    /**
     * Check if request is allowed through circuit breaker
     * 检查是否允许请求通过熔断器
     *
     * @return true if allowed | 允许返回 true
     */
    public boolean allowRequest() {
        State currentState = state.get();

        if (currentState == State.CLOSED) {
            return true;
        }

        if (currentState == State.OPEN) {
            // Check if open duration has passed
            long elapsed = System.currentTimeMillis() - lastFailureTime.get();
            if (elapsed >= config.openDuration.toMillis()) {
                if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    halfOpenAttempts.set(0);
                }
            } else {
                return false;
            }
        }

        // HALF_OPEN state
        return halfOpenAttempts.incrementAndGet() <= config.halfOpenRequests;
    }

    /**
     * Record a successful operation
     * 记录成功的操作
     */
    public synchronized void recordSuccess() {
        successCount.incrementAndGet();

        if (state.get() == State.HALF_OPEN) {
            if (successCount.get() >= config.halfOpenRequests) {
                state.set(State.CLOSED);
                reset();
            }
        }
    }

    /**
     * Record a failed operation
     * 记录失败的操作
     */
    public synchronized void recordFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());

        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            state.set(State.OPEN);
            return;
        }

        if (currentState == State.CLOSED && failures >= config.failureThreshold) {
            state.set(State.OPEN);
        }
    }

    /**
     * Exception thrown when circuit breaker is open
     * 熔断器打开时抛出的异常
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        /**
         * CircuitBreakerOpenException | CircuitBreakerOpenException
         * @param message the message | message
         */
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
