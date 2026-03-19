package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Scheduled Scope - Scheduled Structured Concurrency (JDK 25 JEP 499)
 * 定时作用域 - 定时结构化并发 (JDK 25 JEP 499)
 *
 * <p>Extends structured concurrency with scheduling capabilities including
 * delayed execution, periodic tasks, and deadline-based operations.</p>
 * <p>扩展结构化并发以支持调度功能，包括延迟执行、周期性任务和基于截止时间的操作。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Delayed task forking - 延迟任务分叉</li>
 *   <li>Periodic task execution within scope - 作用域内的周期性任务执行</li>
 *   <li>Deadline-based joining - 基于截止时间的等待</li>
 *   <li>Automatic cancellation on scope close - 作用域关闭时自动取消</li>
 *   <li>Structured concurrency guarantees maintained - 保持结构化并发保证</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Delayed execution
 * try (var scope = ScheduledScope.<String>create()) {
 *     scope.fork(() -> fetchA());
 *     scope.forkDelayed(Duration.ofSeconds(1), () -> fetchB());
 *     scope.forkAt(Instant.now().plusSeconds(2), () -> fetchC());
 *     List<String> results = scope.joinAll();
 * }
 *
 * // Periodic task with limit
 * try (var scope = ScheduledScope.<Integer>create()) {
 *     scope.forkPeriodic(Duration.ofSeconds(1), 5, () -> pollStatus());
 *     List<Integer> statuses = scope.joinAll();
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use within single scope/thread) - 线程安全: 否（在单个作用域/线程内使用）</li>
 * </ul>
 * @param <T> the result type - 结果类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class ScheduledScope<T> implements AutoCloseable {

    private final StructuredTaskScope<T, Stream<Subtask<T>>> scope;
    private final ScheduledExecutorService scheduler;
    // CopyOnWriteArrayList because subtasks are added from the scheduler thread
    // (in forkDelayed/forkPeriodic callbacks) and read from the calling thread (in joinAll).
    private final List<Subtask<T>> subtasks = new CopyOnWriteArrayList<>();
    private final List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Instant deadline;

    private ScheduledScope(StructuredTaskScope<T, Stream<Subtask<T>>> scope,
                           ScheduledExecutorService scheduler,
                           Instant deadline) {
        this.scope = scope;
        this.scheduler = scheduler;
        this.deadline = deadline;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new scheduled scope.
     * 创建新的定时作用域。
     *
     * @param <T> the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> ScheduledScope<T> create() {
        return new ScheduledScope<>(
                StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow()),
                Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory()),
                null);
    }

    /**
     * Creates a scheduled scope with deadline.
     * 创建带截止时间的定时作用域。
     *
     * @param deadline the deadline - 截止时间
     * @param <T>      the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> ScheduledScope<T> withDeadline(Instant deadline) {
        return new ScheduledScope<>(
                StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow()),
                Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory()),
                deadline);
    }

    /**
     * Creates a scheduled scope with timeout (deadline from now).
     * 创建带超时的定时作用域（从现在开始计算截止时间）。
     *
     * @param timeout the timeout - 超时
     * @param <T>     the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> ScheduledScope<T> withTimeout(Duration timeout) {
        return withDeadline(Instant.now().plus(timeout));
    }

    /**
     * Creates a builder for more configuration options.
     * 创建构建器以获取更多配置选项。
     *
     * @param <T> the result type - 结果类型
     * @return the builder - 构建器
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    // ==================== Immediate Forking ====================

    /**
     * Forks a task immediately.
     * 立即分叉任务。
     *
     * @param task the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> fork(Callable<T> task) {
        checkNotClosed();
        checkDeadline();
        subtasks.add(scope.fork(task));
        return this;
    }

    /**
     * Forks multiple tasks immediately.
     * 立即分叉多个任务。
     *
     * @param tasks the tasks - 任务
     * @return this scope - 此作用域
     */
    @SafeVarargs
    public final ScheduledScope<T> forkAll(Callable<T>... tasks) {
        for (Callable<T> task : tasks) {
            fork(task);
        }
        return this;
    }

    /**
     * Forks multiple tasks immediately.
     * 立即分叉多个任务。
     *
     * @param tasks the tasks - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkAll(Iterable<? extends Callable<T>> tasks) {
        for (Callable<T> task : tasks) {
            fork(task);
        }
        return this;
    }

    // ==================== Delayed Forking ====================

    /**
     * Forks a task after a delay.
     * 延迟后分叉任务。
     *
     * @param delay the delay - 延迟
     * @param task  the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkDelayed(Duration delay, Callable<T> task) {
        checkNotClosed();
        checkDeadline(delay);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            if (!closed.get()) {
                subtasks.add(scope.fork(task));
            }
        }, delay.toNanos(), TimeUnit.NANOSECONDS);

        scheduledFutures.add(future);
        return this;
    }

    /**
     * Forks a task at a specific instant.
     * 在指定时刻分叉任务。
     *
     * @param instant the instant - 时刻
     * @param task    the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkAt(Instant instant, Callable<T> task) {
        Duration delay = Duration.between(Instant.now(), instant);
        if (delay.isNegative()) {
            // Execute immediately if the instant is in the past
            return fork(task);
        }
        return forkDelayed(delay, task);
    }

    // ==================== Periodic Forking ====================

    /**
     * Forks a periodic task with a maximum number of executions.
     * 分叉周期性任务，指定最大执行次数。
     *
     * <p>The task will be executed periodically until the count is reached
     * or the scope is closed.</p>
     * <p>任务将周期性执行，直到达到次数或作用域关闭。</p>
     *
     * @param period the period between executions - 执行间隔
     * @param count  the maximum number of executions - 最大执行次数
     * @param task   the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkPeriodic(Duration period, int count, Callable<T> task) {
        checkNotClosed();
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive: " + count);
        }

        AtomicInteger remaining = new AtomicInteger(count);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (!closed.get() && remaining.getAndDecrement() > 0) {
                subtasks.add(scope.fork(task));
            }
        }, 0, period.toNanos(), TimeUnit.NANOSECONDS);

        scheduledFutures.add(future);
        return this;
    }

    /**
     * Forks a periodic task until deadline or scope close.
     * 分叉周期性任务，直到截止时间或作用域关闭。
     *
     * @param period   the period between executions - 执行间隔
     * @param deadline the deadline - 截止时间
     * @param task     the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkPeriodicUntil(Duration period, Instant deadline, Callable<T> task) {
        checkNotClosed();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (!closed.get() && Instant.now().isBefore(deadline)) {
                subtasks.add(scope.fork(task));
            }
        }, 0, period.toNanos(), TimeUnit.NANOSECONDS);

        scheduledFutures.add(future);
        return this;
    }

    /**
     * Forks a periodic task with delay between completion and next execution.
     * 分叉周期性任务，在完成和下次执行之间有延迟。
     *
     * @param delay the delay between completion and next execution - 完成和下次执行之间的延迟
     * @param count the maximum number of executions - 最大执行次数
     * @param task  the task - 任务
     * @return this scope - 此作用域
     */
    public ScheduledScope<T> forkWithFixedDelay(Duration delay, int count, Callable<T> task) {
        checkNotClosed();
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive: " + count);
        }

        AtomicInteger remaining = new AtomicInteger(count);

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(() -> {
            if (!closed.get() && remaining.getAndDecrement() > 0) {
                subtasks.add(scope.fork(task));
            }
        }, 0, delay.toNanos(), TimeUnit.NANOSECONDS);

        scheduledFutures.add(future);
        return this;
    }

    // ==================== Joining ====================

    /**
     * Waits for all scheduled tasks to complete and joins.
     * 等待所有已调度任务完成并等待结果。
     *
     * @return the results - 结果
     */
    public List<T> joinAll() {
        try {
            // Wait for all scheduled futures to complete or cancel
            waitForScheduled();
            scope.join();
            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Scheduled execution failed", e);
        }
    }

    /**
     * Waits for all tasks with timeout.
     * 带超时等待所有任务。
     *
     * @param timeout the timeout - 超时
     * @return the results - 结果
     */
    public List<T> joinAll(Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        try {
            // Wait for scheduled futures with deadline
            waitForScheduledUntil(deadline);
            scope.join();
            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Scheduled execution failed", e);
        }
    }

    /**
     * Joins and reduces results.
     * 等待并归约结果。
     *
     * @param identity the identity value - 恒等值
     * @param reducer  the reducer function - 归约函数
     * @return the reduced result - 归约结果
     */
    public T joinAndReduce(T identity, BiFunction<T, T, T> reducer) {
        List<T> results = joinAll();
        T result = identity;
        for (T r : results) {
            result = reducer.apply(result, r);
        }
        return result;
    }

    /**
     * Joins and returns results as TaskResults.
     * 等待并返回结果为 TaskResult。
     *
     * @return the task results - 任务结果
     */
    public List<TaskResult<T>> joinAsResults() {
        try {
            waitForScheduled();
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        }

        List<TaskResult<T>> results = new ArrayList<>();
        for (Subtask<T> subtask : subtasks) {
            TaskResult<T> result = switch (subtask.state()) {
                case SUCCESS -> TaskResult.success(subtask.get());
                case FAILED -> TaskResult.failure(subtask.exception());
                case UNAVAILABLE -> TaskResult.cancelled();
            };
            results.add(result);
        }
        return results;
    }

    // ==================== Query Methods ====================

    /**
     * Gets the number of forked tasks.
     * 获取分叉的任务数。
     *
     * @return the task count - 任务数
     */
    public int getTaskCount() {
        return subtasks.size();
    }

    /**
     * Gets the number of pending scheduled futures.
     * 获取待处理的已调度 Future 数量。
     *
     * @return the pending scheduled count - 待调度数
     */
    public int getPendingScheduledCount() {
        return (int) scheduledFutures.stream()
                .filter(f -> !f.isDone())
                .count();
    }

    /**
     * Gets the deadline if set.
     * 获取截止时间（如果设置）。
     *
     * @return the deadline or null - 截止时间或 null
     */
    public Instant getDeadline() {
        return deadline;
    }

    /**
     * Gets the remaining time until deadline.
     * 获取距截止时间的剩余时间。
     *
     * @return the remaining time or null if no deadline - 剩余时间，无截止时间时为 null
     */
    public Duration getRemainingTime() {
        if (deadline == null) {
            return null;
        }
        Duration remaining = Duration.between(Instant.now(), deadline);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Checks if the deadline has passed.
     * 检查截止时间是否已过。
     *
     * @return true if deadline has passed - 如果已过截止时间返回 true
     */
    public boolean isDeadlinePassed() {
        return deadline != null && Instant.now().isAfter(deadline);
    }

    // ==================== Private Methods ====================

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("ScheduledScope is closed");
        }
    }

    private void checkDeadline() {
        if (isDeadlinePassed()) {
            throw new OpenParallelException("Deadline has passed: " + deadline);
        }
    }

    private void checkDeadline(Duration delay) {
        if (deadline != null && Instant.now().plus(delay).isAfter(deadline)) {
            throw new OpenParallelException(
                    "Scheduled task would exceed deadline: delay=" + delay + ", deadline=" + deadline);
        }
    }

    private void waitForScheduled() throws InterruptedException {
        // Cancel all periodic/delayed futures so no new executions are scheduled
        cancelAllScheduled();
        // Orderly shutdown: lets any in-flight scheduler task (e.g. a scope.fork() call
        // that was already running when we cancelled) finish before we proceed.
        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void waitForScheduledUntil(Instant deadline) throws InterruptedException {
        long remainingMillis = Duration.between(Instant.now(), deadline).toMillis();
        if (remainingMillis <= 0) {
            cancelAllScheduled();
            return;
        }

        // Wait up to the deadline
        Thread.sleep(Math.min(remainingMillis, 100));
        cancelAllScheduled();
    }

    private void cancelAllScheduled() {
        for (ScheduledFuture<?> future : scheduledFutures) {
            future.cancel(false);
        }
    }

    // ==================== Lifecycle ====================

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            // Cancel all scheduled futures
            cancelAllScheduled();
            scheduledFutures.clear();
            // Shutdown scheduler
            scheduler.shutdown();
            // Close the scope
            scope.close();
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for ScheduledScope.
     * ScheduledScope 的构建器。
     *
     * @param <T> the result type - 结果类型
     */
    public static final class Builder<T> {
        private Instant deadline;
        private int schedulerThreads = 1;

        private Builder() {
        }

        /**
         * Sets the deadline.
         * 设置截止时间。
         *
         * @param deadline the deadline - 截止时间
         * @return this builder - 此构建器
         */
        public Builder<T> deadline(Instant deadline) {
            this.deadline = deadline;
            return this;
        }

        /**
         * Sets the timeout (deadline from now).
         * 设置超时（从现在开始计算截止时间）。
         *
         * @param timeout the timeout - 超时
         * @return this builder - 此构建器
         */
        public Builder<T> timeout(Duration timeout) {
            this.deadline = Instant.now().plus(timeout);
            return this;
        }

        /**
         * Sets the number of scheduler threads.
         * 设置调度器线程数。
         *
         * @param threads the number of threads - 线程数
         * @return this builder - 此构建器
         */
        public Builder<T> schedulerThreads(int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("Scheduler threads must be positive: " + threads);
            }
            this.schedulerThreads = threads;
            return this;
        }

        /**
         * Builds the scope.
         * 构建作用域。
         *
         * @return the scope - 作用域
         */
        public ScheduledScope<T> build() {
            ScheduledExecutorService scheduler = schedulerThreads == 1
                    ? Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory())
                    : Executors.newScheduledThreadPool(schedulerThreads, Thread.ofVirtual().factory());

            return new ScheduledScope<>(
                    StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow()),
                    scheduler,
                    deadline);
        }
    }
}
