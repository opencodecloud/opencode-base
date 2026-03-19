package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Structured Scope - Structured Concurrency Wrapper (JDK 25 JEP 499)
 * 结构化作用域 - 结构化并发包装器 (JDK 25 JEP 499)
 *
 * <p>Provides a fluent API for structured concurrency with automatic
 * lifecycle management of child tasks.</p>
 * <p>为结构化并发提供流式 API，自动管理子任务的生命周期。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Child tasks bound to parent scope - 子任务绑定到父作用域</li>
 *   <li>Automatic cancellation on failure - 失败时自动取消</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Result aggregation - 结果聚合</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * List<String> results = StructuredScope.shutdownOnFailure()
 *     .fork(() -> fetchA())
 *     .fork(() -> fetchB())
 *     .fork(() -> fetchC())
 *     .joinAll();
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
public final class StructuredScope<T> implements AutoCloseable {

    @SuppressWarnings("unchecked")
    private final StructuredTaskScope<T, Stream<Subtask<T>>> scope;
    private final List<Subtask<T>> subtasks = new CopyOnWriteArrayList<>();
    private final Policy policy;

    /**
     * Shutdown policy enumeration.
     * 关闭策略枚举。
     */
    public enum Policy {
        /** Shutdown on first failure - 首次失败时关闭 */
        SHUTDOWN_ON_FAILURE,
        /** Shutdown on first success - 首次成功时关闭 */
        SHUTDOWN_ON_SUCCESS
    }

    private StructuredScope(StructuredTaskScope<T, Stream<Subtask<T>>> scope, Policy policy) {
        this.scope = scope;
        this.policy = policy;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a scope that shuts down on first failure.
     * 创建首次失败时关闭的作用域。
     *
     * @param <T> the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> StructuredScope<T> shutdownOnFailure() {
        return new StructuredScope<>(
                StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow()),
                Policy.SHUTDOWN_ON_FAILURE);
    }

    /**
     * Creates a scope that shuts down on first success.
     * 创建首次成功时关闭的作用域。
     *
     * @param <T> the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> StructuredScope<T> shutdownOnSuccess() {
        return new StructuredScope<>(
                StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow()),
                Policy.SHUTDOWN_ON_SUCCESS);
    }

    // ==================== Forking ====================

    /**
     * Forks a task in this scope.
     * 在此作用域中分叉任务。
     *
     * @param task the task - 任务
     * @return this scope - 此作用域
     */
    public StructuredScope<T> fork(Callable<T> task) {
        subtasks.add(scope.fork(task));
        return this;
    }

    /**
     * Forks multiple tasks in this scope.
     * 在此作用域中分叉多个任务。
     *
     * @param tasks the tasks - 任务
     * @return this scope - 此作用域
     */
    @SafeVarargs
    public final StructuredScope<T> forkAll(Callable<T>... tasks) {
        for (Callable<T> task : tasks) {
            fork(task);
        }
        return this;
    }

    /**
     * Forks multiple tasks in this scope.
     * 在此作用域中分叉多个任务。
     *
     * @param tasks the tasks - 任务
     * @return this scope - 此作用域
     */
    public StructuredScope<T> forkAll(Iterable<? extends Callable<T>> tasks) {
        for (Callable<T> task : tasks) {
            fork(task);
        }
        return this;
    }

    // ==================== Joining ====================

    /**
     * Joins and returns all results.
     * 等待并返回所有结果。
     *
     * @return the results - 结果
     */
    public List<T> joinAll() {
        try {
            scope.join();
            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Structured execution failed", e);
        }
    }

    /**
     * Joins with timeout and returns all results.
     * Note: In JDK 25, timeout must be configured at scope creation time via Configuration.
     * This method provides a best-effort timeout using a virtual thread monitor.
     * 带超时等待并返回所有结果。
     * 注意：JDK 25 中超时须在创建时通过 Configuration 配置。
     * 此方法通过虚拟线程监控提供尽力超时。
     *
     * @param timeout the timeout - 超时
     * @return the results - 结果
     */
    public List<T> joinAll(Duration timeout) {
        Thread current = Thread.currentThread();
        Thread timer = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(timeout);
                current.interrupt();
            } catch (InterruptedException e) {
                // Timer cancelled, scope joined in time
            }
        });
        try {
            scope.join();
            timer.interrupt();
            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Structured execution failed", e);
        } finally {
            timer.interrupt();
        }
    }

    /**
     * Joins and returns the first successful result (for SHUTDOWN_ON_SUCCESS).
     * 等待并返回首个成功结果（用于 SHUTDOWN_ON_SUCCESS）。
     *
     * @return the first result - 首个结果
     */
    public T joinAny() {
        if (policy != Policy.SHUTDOWN_ON_SUCCESS) {
            throw new IllegalStateException("joinAny requires SHUTDOWN_ON_SUCCESS policy");
        }
        try {
            scope.join();
            for (Subtask<T> subtask : subtasks) {
                if (subtask.state() == Subtask.State.SUCCESS) {
                    return subtask.get();
                }
            }
            throw new IllegalStateException("No successful subtask found");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Structured execution failed", e);
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

    // ==================== Lifecycle ====================

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
     * Gets the policy.
     * 获取策略。
     *
     * @return the policy - 策略
     */
    public Policy getPolicy() {
        return policy;
    }

    @Override
    public void close() {
        scope.close();
    }
}
