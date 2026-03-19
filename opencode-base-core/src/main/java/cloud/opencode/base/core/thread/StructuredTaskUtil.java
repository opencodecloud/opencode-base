package cloud.opencode.base.core.thread;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.BiFunction;

/**
 * StructuredTaskScope Utility - JDK 25 Structured Concurrency support (JEP 505)
 * 结构化并发工具类 - JDK 25 结构化并发支持 (JEP 505)
 *
 * <p>Provides structured concurrent task management, ensuring child task lifecycle is bound to parent task.</p>
 * <p>提供结构化的并发任务管理，确保子任务生命周期与父任务绑定。</p>
 *
 * <p><strong>JDK 25 Structured Concurrency Features | JDK 25 结构化并发特性:</strong></p>
 * <ul>
 *   <li>Structured lifecycle - Child tasks cannot escape parent task scope - 结构化生命周期：子任务不会逃逸出父任务作用域</li>
 *   <li>Auto cancellation - Child tasks are automatically cancelled when parent is cancelled - 自动取消：父任务取消时子任务自动取消</li>
 *   <li>Error propagation - Child task exceptions automatically propagate to parent - 错误传播：子任务异常自动传播到父任务</li>
 *   <li>Resource safety - Ensures all child tasks complete before exiting scope - 资源安全：确保所有子任务完成后才退出作用域</li>
 * </ul>
 *
 * <p><strong>JDK 25 API Changes | JDK 25 API 变化:</strong></p>
 * <ul>
 *   <li>Uses Joiner API instead of ShutdownOnFailure/ShutdownOnSuccess subclasses</li>
 *   <li>StructuredTaskScope.open() - default behavior (all must succeed)</li>
 *   <li>Joiner.allSuccessfulOrThrow() - collect all successful results</li>
 *   <li>Joiner.anySuccessfulResultOrThrow() - first to succeed wins</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Execute all tasks in parallel, all must succeed
 * List<String> results = StructuredTaskUtil.invokeAll(List.of(
 *     () -> fetchFromService1(),
 *     () -> fetchFromService2(),
 *     () -> fetchFromService3()
 * ));
 *
 * // Execute tasks, first to succeed wins (redundancy pattern)
 * String result = StructuredTaskUtil.invokeAny(List.of(
 *     () -> fetchFromPrimary(),
 *     () -> fetchFromBackup()
 * ));
 *
 * // Parallel execution with result combination
 * UserOrderInfo info = StructuredTaskUtil.parallel(
 *     () -> userService.getUser(userId),
 *     () -> orderService.getOrders(userId),
 *     (user, orders) -> new UserOrderInfo(user, orders)
 * );
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> Yes - StructuredTaskScope manages thread safety internally</p>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see StructuredTaskScope
 * @see ScopedValueUtil
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class StructuredTaskUtil {

    private StructuredTaskUtil() {
    }

    /**
     * Execute all tasks in parallel, all must succeed
     * 并行执行所有任务，全部成功才返回
     *
     * <p>Uses Joiner.allSuccessfulOrThrow() strategy: if any task fails, all other tasks are cancelled
     * and the exception is thrown.</p>
     * <p>使用 Joiner.allSuccessfulOrThrow() 策略：如果任何任务失败，其他任务将被取消并抛出异常。</p>
     *
     * @param <T>   the result type of the tasks
     * @param tasks the list of tasks to execute
     * @return a list of results from all tasks
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<T>allSuccessfulOrThrow())) {
            tasks.forEach(scope::fork);
            return scope.join()
                    .map(Subtask::get)
                    .toList();
        }
    }

    /**
     * Execute all tasks in parallel with timeout, all must succeed
     * 并行执行所有任务（带超时），全部成功才返回
     *
     * @param <T>     the result type of the tasks
     * @param tasks   the list of tasks to execute
     * @param timeout the maximum time to wait
     * @return a list of results from all tasks
     * @throws InterruptedException if the current thread is interrupted or timeout occurs
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks, Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                Joiner.<T>allSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            tasks.forEach(scope::fork);
            return scope.join()
                    .map(Subtask::get)
                    .toList();
        }
    }

    /**
     * Execute tasks in parallel, first to succeed wins
     * 并行执行任务，任一成功即返回
     *
     * <p>Uses Joiner.anySuccessfulResultOrThrow() strategy: when any task succeeds, all other tasks are cancelled
     * and the result is returned.</p>
     * <p>使用 Joiner.anySuccessfulResultOrThrow() 策略：当任何任务成功时，其他任务将被取消并返回结果。</p>
     *
     * <p>This is useful for redundancy patterns where you want to try multiple sources
     * and use the first successful result.</p>
     *
     * @param <T>   the result type of the tasks
     * @param tasks the list of tasks to execute
     * @return the result from the first successful task
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T> T invokeAny(List<Callable<T>> tasks) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<T>anySuccessfulResultOrThrow())) {
            tasks.forEach(scope::fork);
            return scope.join();
        }
    }

    /**
     * Execute tasks in parallel with timeout, first to succeed wins
     * 并行执行任务（带超时），任一成功即返回
     *
     * @param <T>     the result type of the tasks
     * @param tasks   the list of tasks to execute
     * @param timeout the maximum time to wait
     * @return the result from the first successful task
     * @throws InterruptedException if the current thread is interrupted or timeout occurs
     */
    public static <T> T invokeAny(List<Callable<T>> tasks, Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                Joiner.<T>anySuccessfulResultOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            tasks.forEach(scope::fork);
            return scope.join();
        }
    }

    /**
     * Execute two tasks in parallel and combine their results
     * 并行执行两个任务并合并结果
     *
     * <p>Both tasks must succeed. If either fails, the other is cancelled.</p>
     * <p>两个任务必须都成功。如果任一失败，另一个将被取消。</p>
     *
     * @param <T>      the result type of the first task
     * @param <U>      the result type of the second task
     * @param <R>      the combined result type
     * @param task1    the first task
     * @param task2    the second task
     * @param combiner the function to combine the two results
     * @return the combined result
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T, U, R> R parallel(Callable<T> task1, Callable<U> task2,
                                       BiFunction<T, U, R> combiner) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<Object>awaitAllSuccessfulOrThrow())) {
            Subtask<T> subtask1 = scope.fork(task1);
            Subtask<U> subtask2 = scope.fork(task2);

            scope.join();

            return combiner.apply(subtask1.get(), subtask2.get());
        }
    }

    /**
     * Execute two tasks in parallel with timeout and combine their results
     * 并行执行两个任务（带超时）并合并结果
     *
     * @param <T>      the result type of the first task
     * @param <U>      the result type of the second task
     * @param <R>      the combined result type
     * @param task1    the first task
     * @param task2    the second task
     * @param combiner the function to combine the two results
     * @param timeout  the maximum time to wait
     * @return the combined result
     * @throws InterruptedException if the current thread is interrupted or timeout occurs
     */
    public static <T, U, R> R parallel(Callable<T> task1, Callable<U> task2,
                                       BiFunction<T, U, R> combiner, Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                Joiner.<Object>awaitAllSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            Subtask<T> subtask1 = scope.fork(task1);
            Subtask<U> subtask2 = scope.fork(task2);

            scope.join();

            return combiner.apply(subtask1.get(), subtask2.get());
        }
    }

    /**
     * Execute three tasks in parallel and combine their results
     * 并行执行三个任务并合并结果
     *
     * @param <T1>     the result type of the first task
     * @param <T2>     the result type of the second task
     * @param <T3>     the result type of the third task
     * @param <R>      the combined result type
     * @param task1    the first task
     * @param task2    the second task
     * @param task3    the third task
     * @param combiner the function to combine the three results
     * @return the combined result
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T1, T2, T3, R> R parallel(Callable<T1> task1, Callable<T2> task2, Callable<T3> task3,
                                              TriFunction<T1, T2, T3, R> combiner) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<Object>awaitAllSuccessfulOrThrow())) {
            Subtask<T1> subtask1 = scope.fork(task1);
            Subtask<T2> subtask2 = scope.fork(task2);
            Subtask<T3> subtask3 = scope.fork(task3);

            scope.join();

            return combiner.apply(subtask1.get(), subtask2.get(), subtask3.get());
        }
    }

    /**
     * Execute a single task with structured concurrency semantics
     * 使用结构化并发语义执行单个任务
     *
     * <p>This ensures the task completes or is cancelled before the scope exits.</p>
     *
     * @param <T>  the result type
     * @param task the task to execute
     * @return the result of the task
     * @throws InterruptedException if the current thread is interrupted
     */
    public static <T> T run(Callable<T> task) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<T>awaitAllSuccessfulOrThrow())) {
            Subtask<T> subtask = scope.fork(task);
            scope.join();
            return subtask.get();
        }
    }

    /**
     * Execute a single task with timeout
     * 执行单个任务（带超时）
     *
     * @param <T>     the result type
     * @param task    the task to execute
     * @param timeout the maximum time to wait
     * @return the result of the task
     * @throws InterruptedException if the current thread is interrupted or timeout occurs
     */
    public static <T> T run(Callable<T> task, Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                Joiner.<T>awaitAllSuccessfulOrThrow(),
                cf -> cf.withTimeout(timeout))) {
            Subtask<T> subtask = scope.fork(task);
            scope.join();
            return subtask.get();
        }
    }

    /**
     * Functional interface for combining three values
     * 用于合并三个值的函数式接口
     *
     * @param <T1> the type of the first argument
     * @param <T2> the type of the second argument
     * @param <T3> the type of the third argument
     * @param <R>  the type of the result
     */
    @FunctionalInterface
    public interface TriFunction<T1, T2, T3, R> {
        /**
         * Apply the function to three arguments
         *
         * @param t1 the first argument
         * @param t2 the second argument
         * @param t3 the third argument
         * @return the function result
         */
        R apply(T1 t1, T2 t2, T3 t3);
    }
}
