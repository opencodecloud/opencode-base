package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.exception.OpenParallelException;
import cloud.opencode.base.parallel.pipeline.TriFunction;
import cloud.opencode.base.parallel.structured.StructuredScope;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Open Structured - Structured Concurrency Facade (JDK 25 JEP 499)
 * Open 结构化 - 结构化并发门面 (JDK 25 JEP 499)
 *
 * <p>Provides static methods for structured concurrency operations.
 * Structured concurrency ensures that child tasks are bound to parent
 * task lifecycle - they cannot outlive the parent.</p>
 * <p>为结构化并发操作提供静态方法。结构化并发确保子任务绑定到父任务
 * 的生命周期 - 它们不能超出父任务的生存期。</p>
 *
 * <p><strong>Key Features | 主要特性:</strong></p>
 * <ul>
 *   <li>All subtasks complete before scope closes - 所有子任务在作用域关闭前完成</li>
 *   <li>Automatic cancellation on parent cancellation - 父任务取消时自动取消子任务</li>
 *   <li>Exception propagation follows structured rules - 异常传播遵循结构化规则</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // All must succeed
 * List<String> results = OpenStructured.invokeAll(List.of(
 *     () -> fetchA(),
 *     () -> fetchB(),
 *     () -> fetchC()
 * ));
 *
 * // First success wins
 * String result = OpenStructured.invokeAny(List.of(
 *     () -> fetchFromPrimary(),
 *     () -> fetchFromBackup()
 * ));
 *
 * // Parallel combine
 * Result result = OpenStructured.parallel(
 *     () -> fetchUser(),
 *     () -> fetchOrders(),
 *     (user, orders) -> new Result(user, orders)
 * );
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility class, stateless) - 线程安全: 是（工具类，无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class OpenStructured {

    private OpenStructured() {
        // Static utility class
    }

    // ==================== ShutdownOnFailure ====================

    /**
     * Invokes all tasks, failing fast if any task fails.
     * 调用所有任务，任一任务失败则快速失败。
     *
     * @param tasks the tasks - 任务
     * @param <T>   the result type - 结果类型
     * @return the results - 结果
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks) {
        try (var scope = StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow())) {
            List<Subtask<T>> subtasks = tasks.stream()
                    .map(scope::fork)
                    .toList();

            Stream<Subtask<T>> result = scope.join();

            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Structured invocation failed", e);
        }
    }

    /**
     * Invokes all tasks with timeout.
     * 带超时调用所有任务。
     *
     * @param tasks   the tasks - 任务
     * @param timeout the timeout - 超时
     * @param <T>     the result type - 结果类型
     * @return the results - 结果
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks, Duration timeout) {
        try (var scope = StructuredTaskScope.open(
                Joiner.<T>allSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            List<Subtask<T>> subtasks = tasks.stream()
                    .map(scope::fork)
                    .toList();

            scope.join();

            return subtasks.stream()
                    .map(Subtask::get)
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            // TimeoutException may be wrapped in FailedException in JDK 25
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw OpenParallelException.timeout(timeout);
            }
            throw OpenParallelException.executionFailed("Structured invocation failed", e);
        }
    }

    /**
     * Invokes two tasks in parallel and combines results.
     * 并行调用两个任务并组合结果。
     *
     * @param task1    the first task - 第一个任务
     * @param task2    the second task - 第二个任务
     * @param combiner the combiner function - 组合函数
     * @param <T>      the first type - 第一个类型
     * @param <U>      the second type - 第二个类型
     * @param <R>      the result type - 结果类型
     * @return the combined result - 组合结果
     */
    public static <T, U, R> R parallel(
            Callable<T> task1,
            Callable<U> task2,
            BiFunction<T, U, R> combiner) {
        try (var scope = StructuredTaskScope.open()) {
            var subtask1 = scope.fork(task1);
            var subtask2 = scope.fork(task2);

            scope.join();

            return combiner.apply(subtask1.get(), subtask2.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Parallel execution failed", e);
        }
    }

    /**
     * Invokes three tasks in parallel and combines results.
     * 并行调用三个任务并组合结果。
     *
     * @param task1    the first task - 第一个任务
     * @param task2    the second task - 第二个任务
     * @param task3    the third task - 第三个任务
     * @param combiner the combiner function - 组合函数
     * @param <T>      the first type - 第一个类型
     * @param <U>      the second type - 第二个类型
     * @param <V>      the third type - 第三个类型
     * @param <R>      the result type - 结果类型
     * @return the combined result - 组合结果
     */
    public static <T, U, V, R> R parallel(
            Callable<T> task1,
            Callable<U> task2,
            Callable<V> task3,
            TriFunction<T, U, V, R> combiner) {
        try (var scope = StructuredTaskScope.open()) {
            var subtask1 = scope.fork(task1);
            var subtask2 = scope.fork(task2);
            var subtask3 = scope.fork(task3);

            scope.join();

            return combiner.apply(subtask1.get(), subtask2.get(), subtask3.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Parallel execution failed", e);
        }
    }

    // ==================== ShutdownOnSuccess ====================

    /**
     * Invokes all tasks, returning first success.
     * 调用所有任务，返回首个成功。
     *
     * @param tasks the tasks - 任务
     * @param <T>   the result type - 结果类型
     * @return the first successful result - 首个成功结果
     */
    public static <T> T invokeAny(List<Callable<T>> tasks) {
        try (var scope = StructuredTaskScope.open(Joiner.<T>anySuccessfulResultOrThrow())) {
            tasks.forEach(scope::fork);

            return scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("All tasks failed", e);
        }
    }

    /**
     * Invokes all tasks with timeout, returning first success.
     * 带超时调用所有任务，返回首个成功。
     *
     * @param tasks   the tasks - 任务
     * @param timeout the timeout - 超时
     * @param <T>     the result type - 结果类型
     * @return the first successful result - 首个成功结果
     */
    public static <T> T invokeAny(List<Callable<T>> tasks, Duration timeout) {
        try (var scope = StructuredTaskScope.open(
                Joiner.<T>anySuccessfulResultOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            tasks.forEach(scope::fork);

            return scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenParallelException.interrupted(e);
        } catch (Exception e) {
            // TimeoutException may be wrapped in FailedException in JDK 25
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                throw OpenParallelException.timeout(timeout);
            }
            throw OpenParallelException.executionFailed("All tasks failed", e);
        }
    }

    /**
     * Races multiple tasks, returning the first to complete.
     * 竞争多个任务，返回首个完成的。
     *
     * @param tasks the tasks - 任务
     * @param <T>   the result type - 结果类型
     * @return the first result - 首个结果
     */
    @SafeVarargs
    public static <T> T race(Callable<T>... tasks) {
        return invokeAny(List.of(tasks));
    }

    // ==================== Scoped Values ====================

    /**
     * Runs a task with a scoped value bound.
     * 使用绑定的作用域值运行任务。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param value       the value to bind - 要绑定的值
     * @param task        the task - 任务
     * @param <T>         the value type - 值类型
     * @param <R>         the result type - 结果类型
     * @return the result - 结果
     */
    public static <T, R> R runWithContext(
            ScopedValue<T> scopedValue,
            T value,
            Callable<R> task) {
        try {
            return ScopedValue.where(scopedValue, value).call(task::call);
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Scoped execution failed", e);
        }
    }

    /**
     * Runs multiple tasks with a scoped value bound.
     * 使用绑定的作用域值运行多个任务。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param value       the value to bind - 要绑定的值
     * @param tasks       the tasks - 任务
     * @param <T>         the value type - 值类型
     * @param <R>         the result type - 结果类型
     * @return the results - 结果
     */
    public static <T, R> List<R> runAllWithContext(
            ScopedValue<T> scopedValue,
            T value,
            List<Callable<R>> tasks) {
        try {
            return ScopedValue.where(scopedValue, value).<List<R>, Exception>call(() -> invokeAll(tasks));
        } catch (Exception e) {
            throw OpenParallelException.executionFailed("Scoped execution failed", e);
        }
    }

    // ==================== Scope Builder ====================

    /**
     * Creates a structured scope with shutdown-on-failure policy.
     * 创建具有失败关闭策略的结构化作用域。
     *
     * @param <T> the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> StructuredScope<T> scope() {
        return StructuredScope.shutdownOnFailure();
    }

    /**
     * Creates a structured scope with shutdown-on-success policy.
     * 创建具有成功关闭策略的结构化作用域。
     *
     * @param <T> the result type - 结果类型
     * @return the scope - 作用域
     */
    public static <T> StructuredScope<T> scopeAny() {
        return StructuredScope.shutdownOnSuccess();
    }
}
