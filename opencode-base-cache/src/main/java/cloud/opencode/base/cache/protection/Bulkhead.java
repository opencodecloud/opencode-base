package cloud.opencode.base.cache.protection;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Bulkhead - Resource isolation for cache operations
 * 舱壁 - 缓存操作的资源隔离
 *
 * <p>Provides thread pool isolation to prevent cascade failures by limiting
 * concurrent operations and isolating different cache operations.</p>
 * <p>通过限制并发操作并隔离不同的缓存操作，提供线程池隔离以防止级联故障。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Semaphore-based bulkhead - 基于信号量的舱壁</li>
 *   <li>Thread pool-based bulkhead - 基于线程池的舱壁</li>
 *   <li>Configurable max concurrent calls - 可配置最大并发调用数</li>
 *   <li>Configurable max wait duration - 可配置最大等待时间</li>
 *   <li>Metrics collection - 指标收集</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Semaphore-based bulkhead - 基于信号量的舱壁
 * Bulkhead bulkhead = Bulkhead.semaphore("db-operations")
 *     .maxConcurrentCalls(10)
 *     .maxWaitDuration(Duration.ofMillis(500))
 *     .build();
 *
 * String result = bulkhead.execute(() -> loadFromDb(key));
 *
 * // Thread pool-based bulkhead - 基于线程池的舱壁
 * Bulkhead poolBulkhead = Bulkhead.threadPool("async-operations")
 *     .corePoolSize(5)
 *     .maxPoolSize(10)
 *     .queueCapacity(100)
 *     .build();
 *
 * CompletableFuture<String> future = poolBulkhead.submitAsync(() -> loadAsync(key));
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for permit acquire - 时间复杂度: O(1) 获取许可</li>
 *   <li>Space complexity: O(n) for queue - 空间复杂度: O(n) 队列</li>
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
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public sealed interface Bulkhead permits Bulkhead.SemaphoreBulkhead, Bulkhead.ThreadPoolBulkhead {

    /**
     * Execute operation with bulkhead protection
     * 在舱壁保护下执行操作
     *
     * @param supplier the operation | 操作
     * @param <T>      result type | 结果类型
     * @return result | 结果
     * @throws BulkheadFullException if bulkhead is full | 舱壁已满时抛出异常
     */
    <T> T execute(Supplier<T> supplier);

    /**
     * Execute operation with fallback
     * 执行操作并带降级
     *
     * @param supplier the operation | 操作
     * @param fallback the fallback | 降级操作
     * @param <T>      result type | 结果类型
     * @return result | 结果
     */
    default <T> T execute(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return execute(supplier);
        } catch (BulkheadFullException e) {
            return fallback.get();
        }
    }

    /**
     * Execute operation asynchronously
     * 异步执行操作
     *
     * @param supplier the operation | 操作
     * @param <T>      result type | 结果类型
     * @return future containing result | 包含结果的 Future
     */
    <T> CompletableFuture<T> executeAsync(Supplier<T> supplier);

    /**
     * Get bulkhead name
     * 获取舱壁名称
     *
     * @return name | 名称
     */
    String name();

    /**
     * Get current metrics
     * 获取当前指标
     *
     * @return metrics | 指标
     */
    Metrics getMetrics();

    /**
     * Close bulkhead and release resources
     * 关闭舱壁并释放资源
     */
    void close();

    /**
     * Try to acquire a permit from the bulkhead
     * 尝试从舱壁获取许可
     *
     * @return true if permit acquired | 获取许可返回 true
     */
    boolean tryAcquire();

    /**
     * Release a permit back to the bulkhead
     * 释放许可回到舱壁
     */
    void release();

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create semaphore-based bulkhead builder
     * 创建基于信号量的舱壁构建器
     *
     * @param name bulkhead name | 舱壁名称
     * @return builder | 构建器
     */
    static SemaphoreBuilder semaphore(String name) {
        return new SemaphoreBuilder(name);
    }

    /**
     * Create thread pool-based bulkhead builder
     * 创建基于线程池的舱壁构建器
     *
     * @param name bulkhead name | 舱壁名称
     * @return builder | 构建器
     */
    static ThreadPoolBuilder threadPool(String name) {
        return new ThreadPoolBuilder(name);
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Bulkhead metrics
     * 舱壁指标
     *
     * @param name the bulkhead name | 舱壁名称
     * @param maxAllowedConcurrentCalls the maximum allowed concurrent calls | 最大并发调用数
     * @param availableConcurrentCalls the available concurrent call slots | 可用并发调用槽位
     * @param successfulCallsCount the number of successful calls | 成功调用数
     * @param rejectedCallsCount the number of rejected calls | 拒绝调用数
     * @param totalCallsCount the total number of calls | 总调用数
     */
    record Metrics(
            String name,
            int maxAllowedConcurrentCalls,
            int availableConcurrentCalls,
            long successfulCallsCount,
            long rejectedCallsCount,
            long totalCallsCount
    ) {
        /**
         * Get rejection rate
         * 获取拒绝率
         *
         * @return rejection rate (0.0 to 1.0) | 拒绝率 (0.0 到 1.0)
         */
        public double rejectionRate() {
            return totalCallsCount == 0 ? 0.0 : (double) rejectedCallsCount / totalCallsCount;
        }

        /**
         * Get utilization rate
         * 获取利用率
         *
         * @return utilization rate (0.0 to 1.0) | 利用率 (0.0 到 1.0)
         */
        public double utilizationRate() {
            return maxAllowedConcurrentCalls == 0 ? 0.0 :
                    1.0 - (double) availableConcurrentCalls / maxAllowedConcurrentCalls;
        }
    }

    // ==================== Semaphore Bulkhead | 信号量舱壁 ====================

    /**
     * Semaphore-based bulkhead implementation
     * 基于信号量的舱壁实现
     */
    final class SemaphoreBulkhead implements Bulkhead {

        private final String name;
        private final Semaphore semaphore;
        private final Duration maxWaitDuration;
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong rejectedCount = new AtomicLong(0);
        private final int maxConcurrentCalls;
        private final ExecutorService asyncExecutor;

        SemaphoreBulkhead(String name, int maxConcurrentCalls, Duration maxWaitDuration) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.maxConcurrentCalls = maxConcurrentCalls;
            this.semaphore = new Semaphore(maxConcurrentCalls, true);
            this.maxWaitDuration = maxWaitDuration != null ? maxWaitDuration : Duration.ZERO;
            this.asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
        }

        @Override
        public <T> T execute(Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier must not be null");

            boolean acquired = false;
            try {
                if (maxWaitDuration.isZero()) {
                    acquired = semaphore.tryAcquire();
                } else {
                    acquired = semaphore.tryAcquire(maxWaitDuration.toMillis(), TimeUnit.MILLISECONDS);
                }

                if (!acquired) {
                    rejectedCount.incrementAndGet();
                    throw new BulkheadFullException("Bulkhead '" + name + "' is full");
                }

                T result = supplier.get();
                successCount.incrementAndGet();
                return result;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                rejectedCount.incrementAndGet();
                throw new BulkheadFullException("Interrupted while waiting for bulkhead '" + name + "'");
            } finally {
                if (acquired) {
                    semaphore.release();
                }
            }
        }

        @Override
        public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier must not be null");
            return CompletableFuture.supplyAsync(() -> execute(supplier), asyncExecutor);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Metrics getMetrics() {
            return new Metrics(
                    name,
                    maxConcurrentCalls,
                    semaphore.availablePermits(),
                    successCount.get(),
                    rejectedCount.get(),
                    successCount.get() + rejectedCount.get()
            );
        }

        @Override
        public void close() {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean tryAcquire() {
            boolean acquired;
            try {
                if (maxWaitDuration.isZero()) {
                    acquired = semaphore.tryAcquire();
                } else {
                    acquired = semaphore.tryAcquire(maxWaitDuration.toMillis(), TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                acquired = false;
            }
            if (!acquired) {
                rejectedCount.incrementAndGet();
            }
            return acquired;
        }

        @Override
        public void release() {
            semaphore.release();
        }
    }

    // ==================== Thread Pool Bulkhead | 线程池舱壁 ====================

    /**
     * Thread pool-based bulkhead implementation
     * 基于线程池的舱壁实现
     */
    final class ThreadPoolBulkhead implements Bulkhead {

        private final String name;
        private final ThreadPoolExecutor executor;
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong rejectedCount = new AtomicLong(0);
        private final int maxConcurrentCalls;
        private final Duration maxWaitDuration;

        ThreadPoolBulkhead(String name, int corePoolSize, int maxPoolSize,
                          int queueCapacity, Duration keepAliveTime, Duration maxWaitDuration) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.maxConcurrentCalls = maxPoolSize;
            this.maxWaitDuration = maxWaitDuration != null ? maxWaitDuration : Duration.ZERO;

            ThreadFactory threadFactory = Thread.ofVirtual()
                    .name("bulkhead-" + name + "-", 0)
                    .factory();

            BlockingQueue<Runnable> workQueue = queueCapacity > 0
                    ? new ArrayBlockingQueue<>(queueCapacity)
                    : new SynchronousQueue<>();

            this.executor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime.toMillis(),
                    TimeUnit.MILLISECONDS,
                    workQueue,
                    threadFactory,
                    new RejectedHandler()
            );
        }

        @Override
        public <T> T execute(Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier must not be null");

            try {
                Future<T> future = executor.submit(supplier::get);
                T result;
                if (maxWaitDuration.isZero()) {
                    result = future.get();
                } else {
                    result = future.get(maxWaitDuration.toMillis(), TimeUnit.MILLISECONDS);
                }
                successCount.incrementAndGet();
                return result;
            } catch (RejectedExecutionException e) {
                rejectedCount.incrementAndGet();
                throw new BulkheadFullException("Bulkhead '" + name + "' is full");
            } catch (TimeoutException e) {
                rejectedCount.incrementAndGet();
                throw new BulkheadFullException("Operation timed out in bulkhead '" + name + "'");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                rejectedCount.incrementAndGet();
                throw new BulkheadFullException("Interrupted in bulkhead '" + name + "'");
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(cause);
            }
        }

        @Override
        public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier must not be null");

            CompletableFuture<T> future = new CompletableFuture<>();

            try {
                executor.execute(() -> {
                    try {
                        T result = supplier.get();
                        successCount.incrementAndGet();
                        future.complete(result);
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
            } catch (RejectedExecutionException e) {
                rejectedCount.incrementAndGet();
                future.completeExceptionally(new BulkheadFullException("Bulkhead '" + name + "' is full"));
            }

            return future;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Metrics getMetrics() {
            return new Metrics(
                    name,
                    maxConcurrentCalls,
                    maxConcurrentCalls - executor.getActiveCount(),
                    successCount.get(),
                    rejectedCount.get(),
                    successCount.get() + rejectedCount.get()
            );
        }

        @Override
        public void close() {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean tryAcquire() {
            // Thread pool bulkhead: check if there's capacity
            int active = executor.getActiveCount();
            int queue = executor.getQueue().size();
            if (active >= maxConcurrentCalls || queue >= executor.getQueue().remainingCapacity() + queue) {
                rejectedCount.incrementAndGet();
                return false;
            }
            return true;
        }

        @Override
        public void release() {
            // Thread pool bulkhead: no-op, thread pool manages its own resources
        }

        private class RejectedHandler implements RejectedExecutionHandler {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectedCount.incrementAndGet();
                throw new RejectedExecutionException("Task rejected by bulkhead '" + name + "'");
            }
        }
    }

    // ==================== Builders | 构建器 ====================

    /**
     * Builder for semaphore-based bulkhead
     * 信号量舱壁构建器
     */
    class SemaphoreBuilder {
        private final String name;
        private int maxConcurrentCalls = 25;
        private Duration maxWaitDuration = Duration.ZERO;

        SemaphoreBuilder(String name) {
            this.name = name;
        }

        /**
         * Set maximum concurrent calls
         * 设置最大并发调用数
         *
         * @param maxConcurrentCalls max concurrent calls | 最大并发调用数
         * @return this builder | 此构建器
         */
        public SemaphoreBuilder maxConcurrentCalls(int maxConcurrentCalls) {
            if (maxConcurrentCalls <= 0) {
                throw new IllegalArgumentException("maxConcurrentCalls must be positive");
            }
            this.maxConcurrentCalls = maxConcurrentCalls;
            return this;
        }

        /**
         * Set maximum wait duration for permit
         * 设置获取许可的最大等待时间
         *
         * @param maxWaitDuration max wait duration | 最大等待时间
         * @return this builder | 此构建器
         */
        public SemaphoreBuilder maxWaitDuration(Duration maxWaitDuration) {
            this.maxWaitDuration = maxWaitDuration;
            return this;
        }

        /**
         * Build the bulkhead
         * 构建舱壁
         *
         * @return bulkhead | 舱壁
         */
        public Bulkhead build() {
            return new SemaphoreBulkhead(name, maxConcurrentCalls, maxWaitDuration);
        }
    }

    /**
     * Builder for thread pool-based bulkhead
     * 线程池舱壁构建器
     */
    class ThreadPoolBuilder {
        private final String name;
        private int corePoolSize = 10;
        private int maxPoolSize = 25;
        private int queueCapacity = 100;
        private Duration keepAliveTime = Duration.ofSeconds(60);
        private Duration maxWaitDuration = Duration.ZERO;

        ThreadPoolBuilder(String name) {
            this.name = name;
        }

        /**
         * Set core pool size
         * 设置核心池大小
         *
         * @param corePoolSize core pool size | 核心池大小
         * @return this builder | 此构建器
         */
        public ThreadPoolBuilder corePoolSize(int corePoolSize) {
            if (corePoolSize < 0) {
                throw new IllegalArgumentException("corePoolSize must be non-negative");
            }
            this.corePoolSize = corePoolSize;
            return this;
        }

        /**
         * Set max pool size
         * 设置最大池大小
         *
         * @param maxPoolSize max pool size | 最大池大小
         * @return this builder | 此构建器
         */
        public ThreadPoolBuilder maxPoolSize(int maxPoolSize) {
            if (maxPoolSize <= 0) {
                throw new IllegalArgumentException("maxPoolSize must be positive");
            }
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        /**
         * Set queue capacity
         * 设置队列容量
         *
         * @param queueCapacity queue capacity (0 for no queue) | 队列容量 (0 表示无队列)
         * @return this builder | 此构建器
         */
        public ThreadPoolBuilder queueCapacity(int queueCapacity) {
            if (queueCapacity < 0) {
                throw new IllegalArgumentException("queueCapacity must be non-negative");
            }
            this.queueCapacity = queueCapacity;
            return this;
        }

        /**
         * Set keep alive time for idle threads
         * 设置空闲线程的保活时间
         *
         * @param keepAliveTime keep alive time | 保活时间
         * @return this builder | 此构建器
         */
        public ThreadPoolBuilder keepAliveTime(Duration keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
            return this;
        }

        /**
         * Set max wait duration for operation completion
         * 设置操作完成的最大等待时间
         *
         * @param maxWaitDuration max wait duration | 最大等待时间
         * @return this builder | 此构建器
         */
        public ThreadPoolBuilder maxWaitDuration(Duration maxWaitDuration) {
            this.maxWaitDuration = maxWaitDuration;
            return this;
        }

        /**
         * Build the bulkhead
         * 构建舱壁
         *
         * @return bulkhead | 舱壁
         */
        public Bulkhead build() {
            if (corePoolSize > maxPoolSize) {
                throw new IllegalArgumentException("corePoolSize must not exceed maxPoolSize");
            }
            return new ThreadPoolBulkhead(name, corePoolSize, maxPoolSize,
                    queueCapacity, keepAliveTime, maxWaitDuration);
        }
    }

    // ==================== Exception | 异常 ====================

    /**
     * Exception thrown when bulkhead is full
     * 舱壁已满时抛出的异常
     */
    class BulkheadFullException extends RuntimeException {
        /**
         * Creates a BulkheadFullException | 创建舱壁已满异常
         *
         * @param message the error message | 错误消息
         */
        public BulkheadFullException(String message) {
            super(message);
        }
    }
}
