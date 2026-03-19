package cloud.opencode.base.parallel.executor;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Hybrid Executor - Hybrid Thread Executor
 * 混合执行器 - 混合线程执行器
 *
 * <p>An executor that maintains two thread pools: a fixed platform thread pool
 * for CPU-bound work and a virtual thread executor for IO-bound work.
 * Tasks implementing {@link CpuBound} are automatically dispatched to the
 * platform thread pool; all other tasks go to the virtual thread pool.</p>
 * <p>维护两个线程池的执行器：用于 CPU 密集型工作的固定平台线程池和用于 IO 密集型工作的虚拟线程执行器。
 * 实现 {@link CpuBound} 的任务自动分派到平台线程池；其他所有任务进入虚拟线程池。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Default: CPU pool = availableProcessors()
 * try (var executor = HybridExecutor.create()) {
 *     executor.execute(() -> fetchFromNetwork());           // IO pool (virtual threads)
 *     executor.execute((CpuBound) () -> computeHash(data)); // CPU pool (platform threads)
 *
 *     CompletableFuture<String> result = executor.submitOnIoPool(() -> callApi());
 *     CompletableFuture<Long> hash = executor.submitOnCpuPool(() -> heavyCompute());
 * }
 *
 * // Custom configuration via builder
 * try (var executor = HybridExecutor.builder()
 *         .cpuPoolSize(4)
 *         .cpuThreadNamePrefix("compute-")
 *         .ioThreadNamePrefix("io-")
 *         .build()) {
 *     executor.execute(task);
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dual thread pool (platform + virtual) - 双线程池（平台+虚拟）</li>
 *   <li>Automatic CPU/IO task dispatching - 自动CPU/IO任务分派</li>
 *   <li>CpuBound marker interface support - CpuBound标记接口支持</li>
 *   <li>Builder pattern configuration - 构建器模式配置</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CpuBound
 * @see VirtualExecutor
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class HybridExecutor implements AutoCloseable {

    private final ExecutorService cpuPool;
    private final ExecutorService ioPool;
    private final LongAdder cpuSubmitted = new LongAdder();
    private final LongAdder ioSubmitted = new LongAdder();
    private final LongAdder completedCount = new LongAdder();
    private final LongAdder failedCount = new LongAdder();
    private volatile boolean shutdown = false;

    private HybridExecutor(ExecutorService cpuPool, ExecutorService ioPool) {
        this.cpuPool = cpuPool;
        this.ioPool = ioPool;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a hybrid executor with default settings.
     * 使用默认设置创建混合执行器。
     *
     * <p>CPU pool size defaults to {@code Runtime.getRuntime().availableProcessors()}.</p>
     * <p>CPU 池大小默认为 {@code Runtime.getRuntime().availableProcessors()}。</p>
     *
     * @return the hybrid executor - 混合执行器
     */
    public static HybridExecutor create() {
        return builder().build();
    }

    /**
     * Creates a hybrid executor with the specified CPU pool size.
     * 使用指定的 CPU 池大小创建混合执行器。
     *
     * @param cpuPoolSize the CPU pool size - CPU 池大小
     * @return the hybrid executor - 混合执行器
     * @throws IllegalArgumentException if cpuPoolSize is not positive - 如果 cpuPoolSize 非正数
     */
    public static HybridExecutor withCpuPoolSize(int cpuPoolSize) {
        return builder().cpuPoolSize(cpuPoolSize).build();
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Execute Methods ====================

    /**
     * Executes a runnable, auto-selecting the pool based on type.
     * 执行 Runnable，根据类型自动选择线程池。
     *
     * <p>If the runnable implements {@link CpuBound}, it is dispatched to the
     * platform thread pool. Otherwise, it goes to the virtual thread pool.</p>
     * <p>如果 Runnable 实现了 {@link CpuBound}，则分派到平台线程池。否则进入虚拟线程池。</p>
     *
     * @param task the task to execute - 要执行的任务
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public void execute(Runnable task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        if (task instanceof CpuBound) {
            executeOnCpuPool(task);
        } else {
            executeOnIoPool(task);
        }
    }

    /**
     * Executes a runnable on the CPU (platform thread) pool.
     * 在 CPU（平台线程）池上执行 Runnable。
     *
     * @param task the task to execute - 要执行的任务
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public void executeOnCpuPool(Runnable task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        cpuSubmitted.increment();
        cpuPool.execute(wrapRunnable(task));
    }

    /**
     * Executes a runnable on the IO (virtual thread) pool.
     * 在 IO（虚拟线程）池上执行 Runnable。
     *
     * @param task the task to execute - 要执行的任务
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public void executeOnIoPool(Runnable task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        ioSubmitted.increment();
        ioPool.execute(wrapRunnable(task));
    }

    // ==================== Submit Methods ====================

    /**
     * Submits a callable, auto-selecting the pool based on type.
     * 提交 Callable，根据类型自动选择线程池。
     *
     * <p>If the callable implements {@link CpuBound}, it is dispatched to the
     * platform thread pool. Otherwise, it goes to the virtual thread pool.</p>
     * <p>如果 Callable 实现了 {@link CpuBound}，则分派到平台线程池。否则进入虚拟线程池。</p>
     *
     * @param task the task to submit - 要提交的任务
     * @param <T>  the result type - 结果类型
     * @return a CompletableFuture for the result - 结果的 CompletableFuture
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        if (task instanceof CpuBound) {
            return submitOnCpuPool(task);
        }
        return submitOnIoPool(task);
    }

    /**
     * Submits a callable on the CPU (platform thread) pool.
     * 在 CPU（平台线程）池上提交 Callable。
     *
     * @param task the task to submit - 要提交的任务
     * @param <T>  the result type - 结果类型
     * @return a CompletableFuture for the result - 结果的 CompletableFuture
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public <T> CompletableFuture<T> submitOnCpuPool(Callable<T> task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        cpuSubmitted.increment();
        return CompletableFuture.supplyAsync(() -> callSafely(task), cpuPool)
                .whenComplete((_, ex) -> recordCompletion(ex));
    }

    /**
     * Submits a callable on the IO (virtual thread) pool.
     * 在 IO（虚拟线程）池上提交 Callable。
     *
     * @param task the task to submit - 要提交的任务
     * @param <T>  the result type - 结果类型
     * @return a CompletableFuture for the result - 结果的 CompletableFuture
     * @throws IllegalStateException if the executor is shut down - 如果执行器已关闭
     */
    public <T> CompletableFuture<T> submitOnIoPool(Callable<T> task) {
        requireNotShutdown();
        Objects.requireNonNull(task, "task must not be null");
        ioSubmitted.increment();
        return CompletableFuture.supplyAsync(() -> callSafely(task), ioPool)
                .whenComplete((_, ex) -> recordCompletion(ex));
    }

    // ==================== Statistics ====================

    /**
     * Gets the number of tasks submitted to the CPU pool.
     * 获取提交到 CPU 池的任务数。
     *
     * @return the CPU submitted count - CPU 提交数
     */
    public long getCpuSubmittedCount() {
        return cpuSubmitted.sum();
    }

    /**
     * Gets the number of tasks submitted to the IO pool.
     * 获取提交到 IO 池的任务数。
     *
     * @return the IO submitted count - IO 提交数
     */
    public long getIoSubmittedCount() {
        return ioSubmitted.sum();
    }

    /**
     * Gets the total number of completed tasks.
     * 获取完成的任务总数。
     *
     * @return the completed count - 完成数
     */
    public long getCompletedCount() {
        return completedCount.sum();
    }

    /**
     * Gets the total number of failed tasks.
     * 获取失败的任务总数。
     *
     * @return the failed count - 失败数
     */
    public long getFailedCount() {
        return failedCount.sum();
    }

    /**
     * Checks if the executor is shut down.
     * 检查执行器是否已关闭。
     *
     * @return true if shut down - 如果已关闭返回 true
     */
    public boolean isShutdown() {
        return shutdown;
    }

    // ==================== Lifecycle ====================

    /**
     * Shuts down both pools gracefully.
     * 优雅地关闭两个线程池。
     */
    public void shutdown() {
        shutdown = true;
        cpuPool.shutdown();
        ioPool.shutdown();
    }

    /**
     * Shuts down both pools immediately.
     * 立即关闭两个线程池。
     */
    public void shutdownNow() {
        shutdown = true;
        cpuPool.shutdownNow();
        ioPool.shutdownNow();
    }

    /**
     * Shuts down and waits for termination of both pools.
     * 关闭并等待两个线程池终止。
     *
     * @param timeout the timeout to wait - 等待超时
     * @return true if both pools terminated within timeout - 如果两个池在超时内终止返回 true
     * @throws InterruptedException if interrupted while waiting - 如果等待时被中断
     */
    public boolean shutdownAndAwait(Duration timeout) throws InterruptedException {
        shutdown();
        long millis = timeout.toMillis();
        long start = System.nanoTime();
        boolean cpuDone = cpuPool.awaitTermination(millis, TimeUnit.MILLISECONDS);
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        long remaining = Math.max(0, millis - elapsed);
        boolean ioDone = ioPool.awaitTermination(remaining, TimeUnit.MILLISECONDS);
        return cpuDone && ioDone;
    }

    @Override
    public void close() {
        shutdown();
        try {
            if (!cpuPool.awaitTermination(5, TimeUnit.SECONDS)) {
                cpuPool.shutdownNow();
            }
            if (!ioPool.awaitTermination(5, TimeUnit.SECONDS)) {
                ioPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            cpuPool.shutdownNow();
            ioPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== Internal ====================

    private void requireNotShutdown() {
        if (shutdown) {
            throw new IllegalStateException("HybridExecutor is shut down / 混合执行器已关闭");
        }
    }

    private Runnable wrapRunnable(Runnable task) {
        return () -> {
            try {
                task.run();
                completedCount.increment();
            } catch (Exception e) {
                failedCount.increment();
                throw e;
            }
        };
    }

    private <T> T callSafely(Callable<T> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new OpenParallelException("Task execution failed / 任务执行失败", e);
        }
    }

    private void recordCompletion(Throwable ex) {
        if (ex != null) {
            failedCount.increment();
        } else {
            completedCount.increment();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for HybridExecutor.
     * HybridExecutor 的构建器。
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * HybridExecutor executor = HybridExecutor.builder()
     *     .cpuPoolSize(8)
     *     .cpuThreadNamePrefix("compute-")
     *     .ioThreadNamePrefix("io-")
     *     .build();
     * }</pre>
     */
    public static final class Builder {

        private int cpuPoolSize = Runtime.getRuntime().availableProcessors();
        private String cpuThreadNamePrefix = "hybrid-cpu-";
        private String ioThreadNamePrefix = "hybrid-io-";

        private Builder() {
        }

        /**
         * Sets the CPU pool size (platform threads).
         * 设置 CPU 池大小（平台线程）。
         *
         * @param cpuPoolSize the pool size - 池大小
         * @return this builder - 此构建器
         * @throws IllegalArgumentException if cpuPoolSize is not positive - 如果 cpuPoolSize 非正数
         */
        public Builder cpuPoolSize(int cpuPoolSize) {
            if (cpuPoolSize <= 0) {
                throw new IllegalArgumentException(
                        "CPU pool size must be positive: " + cpuPoolSize
                                + " / CPU 池大小必须为正数: " + cpuPoolSize);
            }
            this.cpuPoolSize = cpuPoolSize;
            return this;
        }

        /**
         * Sets the CPU thread name prefix.
         * 设置 CPU 线程名称前缀。
         *
         * @param prefix the name prefix - 名称前缀
         * @return this builder - 此构建器
         */
        public Builder cpuThreadNamePrefix(String prefix) {
            this.cpuThreadNamePrefix = Objects.requireNonNull(prefix, "prefix must not be null");
            return this;
        }

        /**
         * Sets the IO thread name prefix.
         * 设置 IO 线程名称前缀。
         *
         * @param prefix the name prefix - 名称前缀
         * @return this builder - 此构建器
         */
        public Builder ioThreadNamePrefix(String prefix) {
            this.ioThreadNamePrefix = Objects.requireNonNull(prefix, "prefix must not be null");
            return this;
        }

        /**
         * Builds the HybridExecutor.
         * 构建 HybridExecutor。
         *
         * @return the hybrid executor - 混合执行器
         */
        public HybridExecutor build() {
            ExecutorService cpuPool = Executors.newFixedThreadPool(cpuPoolSize,
                    Thread.ofPlatform().name(cpuThreadNamePrefix, 0).factory());
            ExecutorService ioPool = Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual().name(ioThreadNamePrefix, 0).factory());
            return new HybridExecutor(cpuPool, ioPool);
        }
    }
}
