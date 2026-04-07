package cloud.opencode.base.parallel.executor;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Virtual Executor - Virtual Thread Executor
 * 虚拟执行器 - 虚拟线程执行器
 *
 * <p>A wrapper around JDK virtual thread executor with additional features
 * like concurrency limiting, naming, and statistics.</p>
 * <p>JDK 虚拟线程执行器的包装器，提供并发限制、命名和统计等附加功能。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * VirtualExecutor executor = VirtualExecutor.create();
 * executor.submit(() -> processTask());
 *
 * // With concurrency limit
 * VirtualExecutor limited = VirtualExecutor.withConcurrency(10);
 * limited.submit(() -> limitedTask());
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual thread execution - 虚拟线程执行</li>
 *   <li>Concurrency limiting with Semaphore - 使用信号量限制并发</li>
 *   <li>Task statistics collection - 任务统计收集</li>
 *   <li>Named virtual thread support - 命名虚拟线程支持</li>
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
public final class VirtualExecutor implements AutoCloseable {

    private static final VirtualExecutor SHARED = new VirtualExecutor(
            Executors.newVirtualThreadPerTaskExecutor(),
            null,
            ExecutorConfig.defaults());

    private final ExecutorService delegate;
    private final Semaphore semaphore;
    private final ExecutorConfig config;
    private final LongAdder submittedCount = new LongAdder();
    private final LongAdder completedCount = new LongAdder();
    private final LongAdder failedCount = new LongAdder();

    private VirtualExecutor(ExecutorService delegate, Semaphore semaphore, ExecutorConfig config) {
        this.delegate = delegate;
        this.semaphore = semaphore;
        this.config = config;
    }

    // ==================== Factory Methods ====================

    /**
     * Gets the shared virtual executor.
     * 获取共享虚拟执行器。
     *
     * @return the shared executor - 共享执行器
     */
    public static VirtualExecutor shared() {
        return SHARED;
    }

    /**
     * Creates a new virtual executor.
     * 创建新的虚拟执行器。
     *
     * @return the executor - 执行器
     */
    public static VirtualExecutor create() {
        return new VirtualExecutor(
                Executors.newVirtualThreadPerTaskExecutor(),
                null,
                ExecutorConfig.defaults());
    }

    /**
     * Creates a virtual executor with concurrency limit.
     * 创建带并发限制的虚拟执行器。
     *
     * @param maxConcurrency the max concurrency - 最大并发数
     * @return the executor - 执行器
     */
    public static VirtualExecutor withConcurrency(int maxConcurrency) {
        return new VirtualExecutor(
                Executors.newVirtualThreadPerTaskExecutor(),
                new Semaphore(maxConcurrency),
                ExecutorConfig.builder().maxConcurrency(maxConcurrency).build());
    }

    /**
     * Creates a virtual executor with configuration.
     * 使用配置创建虚拟执行器。
     *
     * @param config the configuration - 配置
     * @return the executor - 执行器
     */
    public static VirtualExecutor withConfig(ExecutorConfig config) {
        Semaphore semaphore = config.getMaxConcurrency() < Integer.MAX_VALUE
                ? new Semaphore(config.getMaxConcurrency())
                : null;

        Thread.Builder.OfVirtual builder = Thread.ofVirtual();
        if (config.getNamePrefix() != null) {
            builder.name(config.getNamePrefix(), 0);
        }
        if (!config.isInheritThreadLocals()) {
            builder.inheritInheritableThreadLocals(false);
        }
        if (config.getUncaughtExceptionHandler() != null) {
            builder.uncaughtExceptionHandler(config.getUncaughtExceptionHandler());
        }

        ExecutorService executor = Executors.newThreadPerTaskExecutor(builder.factory());
        return new VirtualExecutor(executor, semaphore, config);
    }

    // ==================== Submit Methods ====================

    /**
     * Submits a runnable task.
     * 提交 Runnable 任务。
     *
     * @param task the task - 任务
     * @return the future - Future
     */
    public CompletableFuture<Void> submit(Runnable task) {
        submittedCount.increment();
        return CompletableFuture.runAsync(wrapRunnable(task), delegate)
                .whenComplete((_, ex) -> {
                    if (ex != null) {
                        failedCount.increment();
                    } else {
                        completedCount.increment();
                    }
                });
    }

    /**
     * Submits a callable task.
     * 提交 Callable 任务。
     *
     * @param task the task - 任务
     * @param <T>  the result type - 结果类型
     * @return the future - Future
     */
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        submittedCount.increment();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return wrapCallable(task).call();
            } catch (Exception e) {
                throw new OpenParallelException("Task execution failed", e);
            }
        }, delegate).whenComplete((_, ex) -> {
            if (ex != null) {
                failedCount.increment();
            } else {
                completedCount.increment();
            }
        });
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

    /**
     * Submits a task using the underlying executor.
     * 使用底层执行器提交任务。
     *
     * @param task the task - 任务
     * @return the future - Future
     */
    public Future<?> execute(Runnable task) {
        submittedCount.increment();
        return delegate.submit(wrapRunnable(task));
    }

    private Runnable wrapRunnable(Runnable task) {
        if (semaphore == null) {
            return task;
        }
        return () -> {
            boolean acquired = false;
            try {
                semaphore.acquire();
                acquired = true;
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenParallelException("Task interrupted", e);
            } finally {
                if (acquired) semaphore.release();
            }
        };
    }

    private <T> Callable<T> wrapCallable(Callable<T> task) {
        if (semaphore == null) {
            return task;
        }
        return () -> {
            boolean acquired = false;
            try {
                semaphore.acquire();
                acquired = true;
                return task.call();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenParallelException("Task interrupted", e);
            } finally {
                if (acquired) semaphore.release();
            }
        };
    }

    // ==================== Statistics ====================

    /**
     * Gets the number of submitted tasks.
     * 获取提交的任务数。
     *
     * @return the submitted count - 提交数
     */
    public long getSubmittedCount() {
        return submittedCount.sum();
    }

    /**
     * Gets the number of completed tasks.
     * 获取完成的任务数。
     *
     * @return the completed count - 完成数
     */
    public long getCompletedCount() {
        return completedCount.sum();
    }

    /**
     * Gets the number of failed tasks.
     * 获取失败的任务数。
     *
     * @return the failed count - 失败数
     */
    public long getFailedCount() {
        return failedCount.sum();
    }

    /**
     * Gets the number of pending tasks.
     * 获取待处理的任务数。
     *
     * @return the pending count - 待处理数
     */
    public long getPendingCount() {
        return submittedCount.sum() - completedCount.sum() - failedCount.sum();
    }

    /**
     * Gets the available permits (for limited executor).
     * 获取可用许可（用于受限执行器）。
     *
     * @return the available permits or -1 if unlimited - 可用许可或 -1（如果无限制）
     */
    public int getAvailablePermits() {
        return semaphore != null ? semaphore.availablePermits() : -1;
    }

    /**
     * Gets the configuration.
     * 获取配置。
     *
     * @return the config - 配置
     */
    public ExecutorConfig getConfig() {
        return config;
    }

    // ==================== Lifecycle ====================

    /**
     * Shuts down the executor.
     * 关闭执行器。
     */
    public void shutdown() {
        if (this != SHARED) {
            delegate.shutdown();
        }
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
        if (this == SHARED) {
            return true;
        }
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
        if (this == SHARED) {
            return;
        }
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
}
