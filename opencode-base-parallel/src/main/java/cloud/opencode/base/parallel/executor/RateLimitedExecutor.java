package cloud.opencode.base.parallel.executor;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rate Limited Executor - Token Bucket Rate Limiting Executor
 * 限速执行器 - 令牌桶限速执行器
 *
 * <p>An executor that limits task submission rate using the token bucket algorithm.
 * Different from concurrency limiting, this controls throughput over time.</p>
 * <p>使用令牌桶算法限制任务提交速率的执行器。
 * 与并发限制不同，这是控制单位时间内的吞吐量。</p>
 *
 * <p><strong>Token Bucket Algorithm | 令牌桶算法:</strong></p>
 * <ul>
 *   <li>Tokens are added at a fixed rate (permits per second) - 以固定速率添加令牌</li>
 *   <li>Bucket has a maximum capacity for burst handling - 桶有最大容量用于处理突发</li>
 *   <li>Each task consumes one token - 每个任务消耗一个令牌</li>
 *   <li>If no token available, task waits or is rejected - 无令牌时等待或拒绝</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Allow 100 requests per second with burst of 10
 * RateLimitedExecutor executor = RateLimitedExecutor.create(100);
 *
 * // With burst capacity
 * RateLimitedExecutor bursty = RateLimitedExecutor.create(100, 50);
 *
 * // Submit tasks (will be rate limited)
 * executor.submit(() -> callApi());
 *
 * // Try without waiting
 * executor.trySubmit(() -> callApi())
 *     .ifPresent(future -> future.join());
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Token bucket rate limiting - 令牌桶限速</li>
 *   <li>Configurable permits per second - 可配置的每秒许可数</li>
 *   <li>Burst capacity support - 突发容量支持</li>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class RateLimitedExecutor implements AutoCloseable {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final ExecutorService delegate;
    private final double permitsPerSecond;
    private final long burstCapacity;
    private final ReentrantLock lock = new ReentrantLock();

    // Token bucket state (volatile for lock-free fast-path reads)
    // 令牌桶状态（volatile 用于无锁快速路径读取）
    private volatile double storedPermits;
    private volatile long lastRefillTime;

    // Statistics
    private final AtomicLong submittedCount = new AtomicLong(0);
    private final AtomicLong completedCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);
    private final AtomicLong rejectedCount = new AtomicLong(0);
    private final AtomicLong waitedCount = new AtomicLong(0);
    private final AtomicLong totalWaitNanos = new AtomicLong(0);

    private RateLimitedExecutor(ExecutorService delegate, double permitsPerSecond, long burstCapacity) {
        if (permitsPerSecond <= 0) {
            throw new IllegalArgumentException("Permits per second must be positive: " + permitsPerSecond);
        }
        if (burstCapacity <= 0) {
            throw new IllegalArgumentException("Burst capacity must be positive: " + burstCapacity);
        }
        this.delegate = delegate;
        this.permitsPerSecond = permitsPerSecond;
        this.burstCapacity = burstCapacity;
        this.storedPermits = burstCapacity;
        this.lastRefillTime = System.nanoTime();
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a rate limited executor with specified permits per second.
     * 创建指定每秒许可数的限速执行器。
     *
     * <p>Burst capacity defaults to permitsPerSecond / 10, minimum 1.</p>
     * <p>突发容量默认为 permitsPerSecond / 10，最小为 1。</p>
     *
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @return the executor - 执行器
     */
    public static RateLimitedExecutor create(double permitsPerSecond) {
        long burstCapacity = Math.max(1, (long) (permitsPerSecond / 10));
        return create(permitsPerSecond, burstCapacity);
    }

    /**
     * Creates a rate limited executor with specified rate and burst capacity.
     * 创建指定速率和突发容量的限速执行器。
     *
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @param burstCapacity    the burst capacity - 突发容量
     * @return the executor - 执行器
     */
    public static RateLimitedExecutor create(double permitsPerSecond, long burstCapacity) {
        return new RateLimitedExecutor(
                Executors.newVirtualThreadPerTaskExecutor(),
                permitsPerSecond,
                burstCapacity);
    }

    /**
     * Creates a rate limited executor with custom executor service.
     * 使用自定义执行器服务创建限速执行器。
     *
     * @param delegate         the underlying executor - 底层执行器
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @param burstCapacity    the burst capacity - 突发容量
     * @return the executor - 执行器
     */
    public static RateLimitedExecutor withExecutor(ExecutorService delegate,
                                                    double permitsPerSecond,
                                                    long burstCapacity) {
        return new RateLimitedExecutor(delegate, permitsPerSecond, burstCapacity);
    }

    /**
     * Creates a builder for more configuration options.
     * 创建构建器以获取更多配置选项。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Submit Methods ====================

    /**
     * Submits a runnable task, waiting for a permit if necessary.
     * 提交 Runnable 任务，必要时等待许可。
     *
     * @param task the task - 任务
     * @return the future - Future
     */
    public CompletableFuture<Void> submit(Runnable task) {
        acquire();
        submittedCount.incrementAndGet();
        return CompletableFuture.runAsync(task, delegate)
                .whenComplete((_, ex) -> {
                    if (ex != null) {
                        failedCount.incrementAndGet();
                    } else {
                        completedCount.incrementAndGet();
                    }
                });
    }

    /**
     * Submits a callable task, waiting for a permit if necessary.
     * 提交 Callable 任务，必要时等待许可。
     *
     * @param task the task - 任务
     * @param <T>  the result type - 结果类型
     * @return the future - Future
     */
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        acquire();
        submittedCount.incrementAndGet();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new OpenParallelException("Task execution failed", e);
            }
        }, delegate).whenComplete((_, ex) -> {
            if (ex != null) {
                failedCount.incrementAndGet();
            } else {
                completedCount.incrementAndGet();
            }
        });
    }

    /**
     * Submits a runnable task with timeout for acquiring permit.
     * 提交 Runnable 任务，带获取许可超时。
     *
     * @param task    the task - 任务
     * @param timeout the timeout for acquiring permit - 获取许可的超时
     * @return the future - Future
     * @throws OpenParallelException if timeout waiting for permit - 等待许可超时时抛出
     */
    public CompletableFuture<Void> submit(Runnable task, Duration timeout) {
        if (!tryAcquire(timeout)) {
            throw new OpenParallelException("Timeout waiting for rate limit permit after " + timeout.toMillis() + "ms");
        }
        submittedCount.incrementAndGet();
        return CompletableFuture.runAsync(task, delegate)
                .whenComplete((_, ex) -> {
                    if (ex != null) {
                        failedCount.incrementAndGet();
                    } else {
                        completedCount.incrementAndGet();
                    }
                });
    }

    /**
     * Submits a callable task with timeout for acquiring permit.
     * 提交 Callable 任务，带获取许可超时。
     *
     * @param task    the task - 任务
     * @param timeout the timeout for acquiring permit - 获取许可的超时
     * @param <T>     the result type - 结果类型
     * @return the future - Future
     * @throws OpenParallelException if timeout waiting for permit - 等待许可超时时抛出
     */
    public <T> CompletableFuture<T> submit(Callable<T> task, Duration timeout) {
        if (!tryAcquire(timeout)) {
            throw new OpenParallelException("Timeout waiting for rate limit permit after " + timeout.toMillis() + "ms");
        }
        submittedCount.incrementAndGet();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new OpenParallelException("Task execution failed", e);
            }
        }, delegate).whenComplete((_, ex) -> {
            if (ex != null) {
                failedCount.incrementAndGet();
            } else {
                completedCount.incrementAndGet();
            }
        });
    }

    /**
     * Tries to submit a runnable task without waiting.
     * 尝试提交 Runnable 任务，不等待。
     *
     * @param task the task - 任务
     * @return optional future, empty if rate limited - 可选的 Future，如果被限速则为空
     */
    public java.util.Optional<CompletableFuture<Void>> trySubmit(Runnable task) {
        if (!tryAcquire()) {
            rejectedCount.incrementAndGet();
            return java.util.Optional.empty();
        }
        submittedCount.incrementAndGet();
        return java.util.Optional.of(CompletableFuture.runAsync(task, delegate)
                .whenComplete((_, ex) -> {
                    if (ex != null) {
                        failedCount.incrementAndGet();
                    } else {
                        completedCount.incrementAndGet();
                    }
                }));
    }

    /**
     * Tries to submit a callable task without waiting.
     * 尝试提交 Callable 任务，不等待。
     *
     * @param task the task - 任务
     * @param <T>  the result type - 结果类型
     * @return optional future, empty if rate limited - 可选的 Future，如果被限速则为空
     */
    public <T> java.util.Optional<CompletableFuture<T>> trySubmit(Callable<T> task) {
        if (!tryAcquire()) {
            rejectedCount.incrementAndGet();
            return java.util.Optional.empty();
        }
        submittedCount.incrementAndGet();
        return java.util.Optional.of(CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new OpenParallelException("Task execution failed", e);
            }
        }, delegate).whenComplete((_, ex) -> {
            if (ex != null) {
                failedCount.incrementAndGet();
            } else {
                completedCount.incrementAndGet();
            }
        }));
    }

    /**
     * Submits multiple tasks and waits for all.
     * 提交多个任务并等待全部完成。
     *
     * @param tasks the tasks - 任务
     * @param <T>   the result type - 结果类型
     * @return the results - 结果
     */
    public <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Submits multiple tasks with timeout.
     * 提交多个任务并设置超时。
     *
     * @param tasks   the tasks - 任务
     * @param timeout the timeout - 超时
     * @param <T>     the result type - 结果类型
     * @return the results - 结果
     */
    public <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks, Duration timeout) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(this::submit)
                .toList();

        CompletableFuture<Void> all = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            all.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw new OpenParallelException("Invoke all failed", e);
        }

        return futures.stream()
                .filter(f -> !f.isCancelled())
                .map(CompletableFuture::join)
                .toList();
    }

    // ==================== Token Bucket Implementation ====================

    /**
     * Acquires a permit, blocking until available.
     * 获取一个许可，阻塞直到可用。
     */
    public void acquire() {
        long waitNanos = reservePermit();
        if (waitNanos > 0) {
            waitedCount.incrementAndGet();
            totalWaitNanos.addAndGet(waitNanos);
            try {
                TimeUnit.NANOSECONDS.sleep(waitNanos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenParallelException("Interrupted while waiting for rate limit permit", e);
            }
        }
    }

    /**
     * Tries to acquire a permit without waiting.
     * 尝试获取许可，不等待。
     *
     * @return true if acquired - 如果获取成功返回 true
     */
    public boolean tryAcquire() {
        lock.lock();
        try {
            refillTokens();
            if (storedPermits >= 1.0) {
                storedPermits -= 1.0;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tries to acquire a permit with timeout.
     * 尝试获取许可，带超时。
     *
     * @param timeout the timeout - 超时
     * @return true if acquired - 如果获取成功返回 true
     */
    public boolean tryAcquire(Duration timeout) {
        long now = System.nanoTime();
        long timeoutNanos = timeout.toNanos();
        // Guard against nanoTime overflow
        long deadlineNanos = (Long.MAX_VALUE - now < timeoutNanos) ? Long.MAX_VALUE : now + timeoutNanos;
        long waitNanos;

        lock.lock();
        try {
            refillTokens();
            if (storedPermits >= 1.0) {
                storedPermits -= 1.0;
                return true;
            }

            // Calculate wait time
            // 计算等待时间
            double permitsNeeded = 1.0 - storedPermits;
            waitNanos = (long) (permitsNeeded / permitsPerSecond * NANOS_PER_SECOND);

            if (System.nanoTime() + waitNanos > deadlineNanos) {
                return false;
            }

            storedPermits = 0;
            // Advance lastRefillTime so concurrent threads see the "debt"
            // 推进 lastRefillTime 使并发线程能看到"欠债"
            lastRefillTime += waitNanos;
        } finally {
            lock.unlock();
        }

        // Wait outside the lock to avoid blocking other threads
        waitedCount.incrementAndGet();
        totalWaitNanos.addAndGet(waitNanos);
        try {
            TimeUnit.NANOSECONDS.sleep(waitNanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private long reservePermit() {
        lock.lock();
        try {
            refillTokens();
            if (storedPermits >= 1.0) {
                storedPermits -= 1.0;
                return 0;
            }

            // Calculate wait time for next permit
            // 计算下一个许可的等待时间
            double permitsNeeded = 1.0 - storedPermits;
            long waitNanos = (long) (permitsNeeded / permitsPerSecond * NANOS_PER_SECOND);
            storedPermits = 0;
            // Advance lastRefillTime so concurrent threads see the "debt"
            // 推进 lastRefillTime 使并发线程能看到"欠债"
            lastRefillTime += waitNanos;
            return waitNanos;
        } finally {
            lock.unlock();
        }
    }

    private void refillTokens() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillTime;
        double newPermits = elapsed * permitsPerSecond / NANOS_PER_SECOND;
        storedPermits = Math.min(burstCapacity, storedPermits + newPermits);
        lastRefillTime = now;
    }

    // ==================== Statistics ====================

    /**
     * Gets the number of submitted tasks.
     * 获取提交的任务数。
     *
     * @return the submitted count - 提交数
     */
    public long getSubmittedCount() {
        return submittedCount.get();
    }

    /**
     * Gets the number of completed tasks.
     * 获取完成的任务数。
     *
     * @return the completed count - 完成数
     */
    public long getCompletedCount() {
        return completedCount.get();
    }

    /**
     * Gets the number of failed tasks.
     * 获取失败的任务数。
     *
     * @return the failed count - 失败数
     */
    public long getFailedCount() {
        return failedCount.get();
    }

    /**
     * Gets the number of rejected tasks (when using trySubmit).
     * 获取被拒绝的任务数（使用 trySubmit 时）。
     *
     * @return the rejected count - 拒绝数
     */
    public long getRejectedCount() {
        return rejectedCount.get();
    }

    /**
     * Gets the number of tasks that waited for a permit.
     * 获取等待许可的任务数。
     *
     * @return the waited count - 等待数
     */
    public long getWaitedCount() {
        return waitedCount.get();
    }

    /**
     * Gets the total wait time in nanoseconds.
     * 获取总等待时间（纳秒）。
     *
     * @return the total wait nanos - 总等待纳秒数
     */
    public long getTotalWaitNanos() {
        return totalWaitNanos.get();
    }

    /**
     * Gets the average wait time in milliseconds.
     * 获取平均等待时间（毫秒）。
     *
     * @return the average wait millis - 平均等待毫秒数
     */
    public double getAverageWaitMillis() {
        long waited = waitedCount.get();
        if (waited == 0) {
            return 0.0;
        }
        return totalWaitNanos.get() / (waited * 1_000_000.0);
    }

    /**
     * Gets the configured permits per second.
     * 获取配置的每秒许可数。
     *
     * @return the permits per second - 每秒许可数
     */
    public double getPermitsPerSecond() {
        return permitsPerSecond;
    }

    /**
     * Gets the burst capacity.
     * 获取突发容量。
     *
     * @return the burst capacity - 突发容量
     */
    public long getBurstCapacity() {
        return burstCapacity;
    }

    /**
     * Gets the currently available permits.
     * 获取当前可用的许可数。
     *
     * @return the available permits - 可用许可数
     */
    public double getAvailablePermits() {
        lock.lock();
        try {
            refillTokens();
            return storedPermits;
        } finally {
            lock.unlock();
        }
    }

    // ==================== Lifecycle ====================

    /**
     * Shuts down the executor.
     * 关闭执行器。
     */
    public void shutdown() {
        delegate.shutdown();
    }

    /**
     * Shuts down and waits for termination.
     * 关闭并等待终止。
     *
     * @param timeout the timeout - 超时
     * @return true if terminated - 如果终止返回 true
     * @throws InterruptedException if interrupted - 如果中断
     */
    public boolean shutdownAndAwait(Duration timeout) throws InterruptedException {
        delegate.shutdown();
        return delegate.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if the executor is shutdown.
     * 检查执行器是否已关闭。
     *
     * @return true if shutdown - 如果已关闭返回 true
     */
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    /**
     * Checks if the executor is terminated.
     * 检查执行器是否已终止。
     *
     * @return true if terminated - 如果已终止返回 true
     */
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public void close() {
        delegate.shutdown();
        try {
            if (!delegate.awaitTermination(5, TimeUnit.SECONDS)) {
                delegate.shutdownNow();
            }
        } catch (InterruptedException e) {
            delegate.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for RateLimitedExecutor.
     * RateLimitedExecutor 的构建器。
     */
    public static final class Builder {
        private double permitsPerSecond = 100;
        private long burstCapacity = 10;
        private ExecutorService executor;
        private String namePrefix = "rate-limited-";

        private Builder() {
        }

        /**
         * Sets the permits per second.
         * 设置每秒许可数。
         *
         * @param permitsPerSecond the permits per second - 每秒许可数
         * @return this builder - 此构建器
         */
        public Builder permitsPerSecond(double permitsPerSecond) {
            if (permitsPerSecond <= 0) {
                throw new IllegalArgumentException("Permits per second must be positive: " + permitsPerSecond);
            }
            this.permitsPerSecond = permitsPerSecond;
            return this;
        }

        /**
         * Sets the burst capacity.
         * 设置突发容量。
         *
         * @param burstCapacity the burst capacity - 突发容量
         * @return this builder - 此构建器
         */
        public Builder burstCapacity(long burstCapacity) {
            if (burstCapacity <= 0) {
                throw new IllegalArgumentException("Burst capacity must be positive: " + burstCapacity);
            }
            this.burstCapacity = burstCapacity;
            return this;
        }

        /**
         * Sets a custom executor service.
         * 设置自定义执行器服务。
         *
         * @param executor the executor - 执行器
         * @return this builder - 此构建器
         */
        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Sets the thread name prefix (for default virtual thread executor).
         * 设置线程名称前缀（用于默认虚拟线程执行器）。
         *
         * @param namePrefix the name prefix - 名称前缀
         * @return this builder - 此构建器
         */
        public Builder namePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        /**
         * Builds the executor.
         * 构建执行器。
         *
         * @return the executor - 执行器
         */
        public RateLimitedExecutor build() {
            ExecutorService exec = this.executor;
            if (exec == null) {
                Thread.Builder.OfVirtual builder = Thread.ofVirtual();
                if (namePrefix != null) {
                    builder.name(namePrefix, 0);
                }
                exec = Executors.newThreadPerTaskExecutor(builder.factory());
            }
            return new RateLimitedExecutor(exec, permitsPerSecond, burstCapacity);
        }
    }
}
