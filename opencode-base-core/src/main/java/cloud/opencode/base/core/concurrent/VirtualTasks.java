package cloud.opencode.base.core.concurrent;

import cloud.opencode.base.core.Preconditions;
import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.core.exception.OpenTimeoutException;
import cloud.opencode.base.core.result.Result;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * VirtualTasks - Virtual thread concurrency utilities
 * VirtualTasks - 虚拟线程并发工具类
 *
 * <p>Provides high-level concurrency primitives built on JDK 25 virtual threads.
 * All methods create a short-lived virtual-thread-per-task executor, submit tasks,
 * collect results, and shut down the executor before returning.</p>
 * <p>提供基于 JDK 25 虚拟线程的高级并发原语。
 * 所有方法创建短生命周期的每任务虚拟线程执行器，提交任务，收集结果，
 * 并在返回前关闭执行器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link #invokeAll(List)} - All-or-nothing execution: all succeed or throw on first failure
 *       全部成功或在首次失败时抛出异常</li>
 *   <li>{@link #invokeAny(List)} - First success wins, cancel rest
 *       首个成功结果胜出，取消其余任务</li>
 *   <li>{@link #invokeAllSettled(List)} - Collect all results as {@link Result}, never throws from task failures
 *       收集所有结果为 {@link Result}，不因任务失败而抛出异常</li>
 *   <li>{@link #parallelMap(List, Function)} - Parallel mapping over a list
 *       对列表进行并行映射</li>
 *   <li>{@link #runAll(List)} - Run all tasks to completion
 *       运行所有任务直至完成</li>
 *   <li>{@link #supplyAsync(Callable)} - Bridge virtual thread execution with CompletableFuture
 *       将虚拟线程执行与 CompletableFuture 桥接</li>
 *   <li>{@link #runAsync(Runnable)} - Run a task on a virtual thread returning CompletableFuture
 *       在虚拟线程上运行任务并返回 CompletableFuture</li>
 *   <li>{@link #parallelMap(List, Function, int)} - Parallel mapping with concurrency limit
 *       带并发限制的并行映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // All-or-nothing
 * List<String> results = VirtualTasks.invokeAll(List.of(
 *     () -> fetchFromServiceA(),
 *     () -> fetchFromServiceB()
 * ));
 *
 * // First success wins
 * String fastest = VirtualTasks.invokeAny(List.of(
 *     () -> queryMirror1(),
 *     () -> queryMirror2()
 * ));
 *
 * // Collect all (success or failure)
 * List<Result<String>> settled = VirtualTasks.invokeAllSettled(List.of(
 *     () -> riskyOperation1(),
 *     () -> riskyOperation2()
 * ));
 *
 * // Parallel map with timeout
 * List<Integer> lengths = VirtualTasks.parallelMap(
 *     urls, url -> download(url).length(), Duration.ofSeconds(30)
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是 (无状态工具类)</li>
 *   <li>Resource-safe: Executors are always shut down in finally blocks - 资源安全: 执行器始终在 finally 块中关闭</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Result
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class VirtualTasks {

    private VirtualTasks() {
        // Utility class, not instantiable | 工具类，不可实例化
    }

    // ==================== invokeAll ====================

    /**
     * Execute all tasks and return their results. If any task fails, cancel all remaining
     * and throw an {@link OpenException} wrapping the first failure cause.
     * 执行所有任务并返回结果。如果任何任务失败，取消所有剩余任务并抛出包装首个失败原因的 {@link OpenException}。
     *
     * @param tasks the tasks to execute - 要执行的任务列表
     * @param <T>   result type - 结果类型
     * @return immutable list of results in submission order - 按提交顺序的不可变结果列表
     * @throws OpenException if any task fails - 如果任何任务失败
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks) {
        validateTasks(tasks);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAll(executor, tasks, null);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Execute all tasks with a timeout. If any task fails or the timeout expires,
     * cancel all remaining and throw.
     * 在超时限制内执行所有任务。如果任何任务失败或超时，取消所有剩余任务并抛出异常。
     *
     * @param tasks   the tasks to execute - 要执行的任务列表
     * @param timeout the maximum duration to wait - 最大等待时长
     * @param <T>     result type - 结果类型
     * @return immutable list of results in submission order - 按提交顺序的不可变结果列表
     * @throws OpenException        if any task fails - 如果任何任务失败
     * @throws OpenTimeoutException if the timeout expires - 如果超时
     */
    public static <T> List<T> invokeAll(List<Callable<T>> tasks, Duration timeout) {
        validateTasks(tasks);
        Preconditions.checkNotNull(timeout, "timeout must not be null");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAll(executor, tasks, timeout);
        } finally {
            executor.shutdownNow();
        }
    }

    // ==================== invokeAny ====================

    /**
     * Execute all tasks and return the result of the first one to succeed.
     * All remaining tasks are cancelled. If all tasks fail, throws {@link OpenException}.
     * 执行所有任务并返回首个成功的结果。取消所有剩余任务。如果所有任务都失败，抛出 {@link OpenException}。
     *
     * @param tasks the tasks to execute - 要执行的任务列表
     * @param <T>   result type - 结果类型
     * @return the first successful result - 首个成功的结果
     * @throws OpenException if all tasks fail - 如果所有任务都失败
     */
    public static <T> T invokeAny(List<Callable<T>> tasks) {
        validateTasks(tasks);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAny(executor, tasks, null);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Execute all tasks with a timeout and return the first successful result.
     * 在超时限制内执行所有任务并返回首个成功的结果。
     *
     * @param tasks   the tasks to execute - 要执行的任务列表
     * @param timeout the maximum duration to wait - 最大等待时长
     * @param <T>     result type - 结果类型
     * @return the first successful result - 首个成功的结果
     * @throws OpenException        if all tasks fail - 如果所有任务都失败
     * @throws OpenTimeoutException if the timeout expires before any task succeeds - 如果在任何任务成功前超时
     */
    public static <T> T invokeAny(List<Callable<T>> tasks, Duration timeout) {
        validateTasks(tasks);
        Preconditions.checkNotNull(timeout, "timeout must not be null");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAny(executor, tasks, timeout);
        } finally {
            executor.shutdownNow();
        }
    }

    // ==================== invokeAllSettled ====================

    /**
     * Execute all tasks and collect every result as {@link Result#success(Object)} or
     * {@link Result#failure(Throwable)}. Never throws from task failures.
     * 执行所有任务并将每个结果收集为 {@link Result#success(Object)} 或 {@link Result#failure(Throwable)}。
     * 不因任务失败而抛出异常。
     *
     * @param tasks the tasks to execute - 要执行的任务列表
     * @param <T>   result type - 结果类型
     * @return immutable list of Results in submission order - 按提交顺序的不可变 Result 列表
     */
    public static <T> List<Result<T>> invokeAllSettled(List<Callable<T>> tasks) {
        validateTasks(tasks);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAllSettled(executor, tasks, null);
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Execute all tasks with a timeout and collect every result as {@link Result}.
     * Tasks that have not completed by the timeout deadline are recorded as
     * {@link Result#failure(Throwable)} with an {@link OpenTimeoutException}.
     * 在超时限制内执行所有任务并将每个结果收集为 {@link Result}。
     * 超时未完成的任务记录为包含 {@link OpenTimeoutException} 的 {@link Result#failure(Throwable)}。
     *
     * @param tasks   the tasks to execute - 要执行的任务列表
     * @param timeout the maximum duration to wait - 最大等待时长
     * @param <T>     result type - 结果类型
     * @return immutable list of Results in submission order - 按提交顺序的不可变 Result 列表
     */
    public static <T> List<Result<T>> invokeAllSettled(List<Callable<T>> tasks, Duration timeout) {
        validateTasks(tasks);
        Preconditions.checkNotNull(timeout, "timeout must not be null");
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            return doInvokeAllSettled(executor, tasks, timeout);
        } finally {
            executor.shutdownNow();
        }
    }

    // ==================== parallelMap ====================

    /**
     * Apply a mapping function to each item in parallel using virtual threads.
     * 使用虚拟线程对每个元素并行应用映射函数。
     *
     * @param items  the input items - 输入元素列表
     * @param mapper the mapping function - 映射函数
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return immutable list of mapped results in input order - 按输入顺序的不可变映射结果列表
     * @throws OpenException if any mapping fails - 如果任何映射失败
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper) {
        Preconditions.checkNotNull(items, "items must not be null");
        Preconditions.checkNotNull(mapper, "mapper must not be null");
        Preconditions.checkArgument(!items.isEmpty(), "items must not be empty");
        List<Callable<R>> tasks = items.stream()
                .<Callable<R>>map(item -> () -> mapper.apply(item))
                .toList();
        return invokeAll(tasks);
    }

    /**
     * Apply a mapping function to each item in parallel with a timeout.
     * 在超时限制内使用虚拟线程对每个元素并行应用映射函数。
     *
     * @param items   the input items - 输入元素列表
     * @param mapper  the mapping function - 映射函数
     * @param timeout the maximum duration to wait - 最大等待时长
     * @param <T>     input type - 输入类型
     * @param <R>     result type - 结果类型
     * @return immutable list of mapped results in input order - 按输入顺序的不可变映射结果列表
     * @throws OpenException        if any mapping fails - 如果任何映射失败
     * @throws OpenTimeoutException if the timeout expires - 如果超时
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper, Duration timeout) {
        Preconditions.checkNotNull(items, "items must not be null");
        Preconditions.checkNotNull(mapper, "mapper must not be null");
        Preconditions.checkArgument(!items.isEmpty(), "items must not be empty");
        Preconditions.checkNotNull(timeout, "timeout must not be null");
        List<Callable<R>> tasks = items.stream()
                .<Callable<R>>map(item -> () -> mapper.apply(item))
                .toList();
        return invokeAll(tasks, timeout);
    }

    // ==================== runAll ====================

    /**
     * Run all tasks to completion. If any task fails, cancel remaining and throw.
     * 运行所有任务直至完成。如果任何任务失败，取消剩余任务并抛出异常。
     *
     * @param tasks the tasks to run - 要运行的任务列表
     * @throws OpenException if any task fails - 如果任何任务失败
     */
    public static void runAll(List<Runnable> tasks) {
        Preconditions.checkNotNull(tasks, "tasks must not be null");
        Preconditions.checkArgument(!tasks.isEmpty(), "tasks must not be empty");
        List<Callable<Void>> callables = tasks.stream()
                .<Callable<Void>>map(task -> () -> {
                    task.run();
                    return null;
                })
                .toList();
        invokeAll(callables);
    }

    /**
     * Run all tasks to completion with a timeout.
     * 在超时限制内运行所有任务直至完成。
     *
     * @param tasks   the tasks to run - 要运行的任务列表
     * @param timeout the maximum duration to wait - 最大等待时长
     * @throws OpenException        if any task fails - 如果任何任务失败
     * @throws OpenTimeoutException if the timeout expires - 如果超时
     */
    public static void runAll(List<Runnable> tasks, Duration timeout) {
        Preconditions.checkNotNull(tasks, "tasks must not be null");
        Preconditions.checkArgument(!tasks.isEmpty(), "tasks must not be empty");
        Preconditions.checkNotNull(timeout, "timeout must not be null");
        List<Callable<Void>> callables = tasks.stream()
                .<Callable<Void>>map(task -> () -> {
                    task.run();
                    return null;
                })
                .toList();
        invokeAll(callables, timeout);
    }

    // ==================== Async Bridge | 异步桥接 ====================

    /**
     * Executes a callable on a virtual thread and returns a CompletableFuture.
     * 在虚拟线程上执行 Callable 并返回 CompletableFuture。
     *
     * <p>Bridges virtual thread execution with the CompletableFuture API.
     * The callable is executed on a new virtual thread, and the returned
     * future completes when the callable finishes.</p>
     * <p>将虚拟线程执行与 CompletableFuture API 桥接。
     * Callable 在新虚拟线程上执行，返回的 future 在 Callable 完成时完成。</p>
     *
     * @param task the task to execute - 要执行的任务
     * @param <T>  result type - 结果类型
     * @return a CompletableFuture that completes with the task result - 完成后包含任务结果的 CompletableFuture
     * @throws NullPointerException if task is null - 如果 task 为 null
     */
    public static <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        Preconditions.checkNotNull(task, "task must not be null");
        CompletableFuture<T> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                future.complete(task.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Executes a runnable on a virtual thread and returns a CompletableFuture.
     * 在虚拟线程上执行 Runnable 并返回 CompletableFuture。
     *
     * @param task the task to run - 要运行的任务
     * @return a CompletableFuture that completes when the task finishes - 任务完成时完成的 CompletableFuture
     * @throws NullPointerException if task is null - 如果 task 为 null
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        Preconditions.checkNotNull(task, "task must not be null");
        CompletableFuture<Void> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    // ==================== Concurrency-Limited Parallel Map | 并发限制的并行映射 ====================

    /**
     * Apply a mapping function to each item in parallel with a concurrency limit.
     * 使用并发限制对每个元素并行应用映射函数。
     *
     * <p>Uses a {@link Semaphore} to limit the number of concurrently running
     * virtual threads. This is useful when the mapper accesses a resource with
     * limited capacity (e.g., a connection pool).</p>
     * <p>使用 {@link Semaphore} 限制并发运行的虚拟线程数。
     * 当映射函数访问容量有限的资源（如连接池）时，此方法非常有用。</p>
     *
     * @param items          the input items - 输入元素列表
     * @param mapper         the mapping function - 映射函数
     * @param maxConcurrency maximum number of concurrent virtual threads (must be positive) -
     *                       最大并发虚拟线程数（必须为正数）
     * @param <T>            input type - 输入类型
     * @param <R>            result type - 结果类型
     * @return immutable list of mapped results in input order - 按输入顺序的不可变映射结果列表
     * @throws OpenException                if any mapping fails - 如果任何映射失败
     * @throws IllegalArgumentException     if maxConcurrency is not positive - 如果 maxConcurrency 不为正数
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper, int maxConcurrency) {
        Preconditions.checkNotNull(items, "items must not be null");
        Preconditions.checkNotNull(mapper, "mapper must not be null");
        Preconditions.checkArgument(!items.isEmpty(), "items must not be empty");
        Preconditions.checkArgument(maxConcurrency > 0, "maxConcurrency must be positive, got: %s", maxConcurrency);

        if (maxConcurrency >= items.size()) {
            return parallelMap(items, mapper);
        }

        Semaphore semaphore = new Semaphore(maxConcurrency);
        List<Callable<R>> tasks = items.stream()
                .<Callable<R>>map(item -> () -> {
                    semaphore.acquire();
                    try {
                        return mapper.apply(item);
                    } finally {
                        semaphore.release();
                    }
                })
                .toList();
        return invokeAll(tasks);
    }

    /**
     * Apply a mapping function with concurrency limit and timeout.
     * 使用并发限制和超时对每个元素并行应用映射函数。
     *
     * @param items          the input items - 输入元素列表
     * @param mapper         the mapping function - 映射函数
     * @param maxConcurrency maximum concurrent virtual threads - 最大并发虚拟线程数
     * @param timeout        the maximum duration to wait - 最大等待时长
     * @param <T>            input type - 输入类型
     * @param <R>            result type - 结果类型
     * @return immutable list of mapped results in input order - 按输入顺序的不可变映射结果列表
     * @throws OpenException        if any mapping fails - 如果任何映射失败
     * @throws OpenTimeoutException if the timeout expires - 如果超时
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper,
                                              int maxConcurrency, Duration timeout) {
        Preconditions.checkNotNull(items, "items must not be null");
        Preconditions.checkNotNull(mapper, "mapper must not be null");
        Preconditions.checkArgument(!items.isEmpty(), "items must not be empty");
        Preconditions.checkArgument(maxConcurrency > 0, "maxConcurrency must be positive, got: %s", maxConcurrency);
        Preconditions.checkNotNull(timeout, "timeout must not be null");

        if (maxConcurrency >= items.size()) {
            return parallelMap(items, mapper, timeout);
        }

        Semaphore semaphore = new Semaphore(maxConcurrency);
        List<Callable<R>> tasks = items.stream()
                .<Callable<R>>map(item -> () -> {
                    semaphore.acquire();
                    try {
                        return mapper.apply(item);
                    } finally {
                        semaphore.release();
                    }
                })
                .toList();
        return invokeAll(tasks, timeout);
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Validates task list preconditions.
     * 校验任务列表前置条件。
     */
    private static void validateTasks(List<?> tasks) {
        Preconditions.checkNotNull(tasks, "tasks must not be null");
        Preconditions.checkArgument(!tasks.isEmpty(), "tasks must not be empty");
    }

    /**
     * Core invokeAll implementation.
     * invokeAll 核心实现。
     */
    private static <T> List<T> doInvokeAll(ExecutorService executor, List<Callable<T>> tasks,
                                            Duration timeout) {
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(executor.submit(task));
        }

        long deadlineNanos = timeout != null ? deadlineNanos(timeout) : 0;
        List<T> results = new ArrayList<>(tasks.size());

        try {
            for (Future<T> future : futures) {
                try {
                    if (timeout != null) {
                        long remainingNanos = deadlineNanos - System.nanoTime();
                        if (remainingNanos <= 0) {
                            throw OpenTimeoutException.of("invokeAll", timeout);
                        }
                        results.add(future.get(remainingNanos, TimeUnit.NANOSECONDS));
                    } else {
                        results.add(future.get());
                    }
                } catch (TimeoutException e) {
                    throw OpenTimeoutException.of("invokeAll", timeout);
                } catch (ExecutionException e) {
                    throw new OpenException("Task execution failed", unwrap(e));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            for (Future<T> future : futures) {
                future.cancel(true);
            }
            throw new OpenException("Task execution interrupted", e);
        } catch (OpenException e) {
            // Cancel all remaining futures before re-throwing
            // (also catches OpenTimeoutException which extends OpenException)
            for (Future<T> future : futures) {
                future.cancel(true);
            }
            throw e;
        }

        return java.util.Collections.unmodifiableList(results);
    }

    /**
     * Core invokeAny implementation using CompletionService.
     * 使用 CompletionService 的 invokeAny 核心实现。
     */
    private static <T> T doInvokeAny(ExecutorService executor, List<Callable<T>> tasks,
                                      Duration timeout) {
        CompletionService<T> completionService = new ExecutorCompletionService<>(executor);
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(completionService.submit(task));
        }

        long deadlineNanos = timeout != null ? deadlineNanos(timeout) : 0;
        Throwable lastException = null;
        int remaining = tasks.size();

        try {
            while (remaining > 0) {
                Future<T> completed;
                try {
                    if (timeout != null) {
                        long remainingNanos = deadlineNanos - System.nanoTime();
                        if (remainingNanos <= 0) {
                            throw OpenTimeoutException.of("invokeAny", timeout);
                        }
                        completed = completionService.poll(remainingNanos, TimeUnit.NANOSECONDS);
                        if (completed == null) {
                            throw OpenTimeoutException.of("invokeAny", timeout);
                        }
                    } else {
                        completed = completionService.take();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenException("Task execution interrupted", e);
                }

                remaining--;
                try {
                    T result = completed.get();
                    // Success: cancel all remaining futures
                    for (Future<T> future : futures) {
                        future.cancel(true);
                    }
                    return result;
                } catch (ExecutionException e) {
                    lastException = unwrap(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new OpenException("Task execution interrupted", e);
                }
            }
        } catch (OpenException e) {
            // Also catches OpenTimeoutException which extends OpenException
            for (Future<T> future : futures) {
                future.cancel(true);
            }
            throw e;
        }

        // All tasks failed
        for (Future<T> future : futures) {
            future.cancel(true);
        }
        throw new OpenException("All tasks failed", lastException);
    }

    /**
     * Core invokeAllSettled implementation.
     * invokeAllSettled 核心实现。
     */
    private static <T> List<Result<T>> doInvokeAllSettled(ExecutorService executor,
                                                          List<Callable<T>> tasks,
                                                          Duration timeout) {
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(executor.submit(task));
        }

        long deadlineNanos = timeout != null ? deadlineNanos(timeout) : 0;
        List<Result<T>> results = new ArrayList<>(tasks.size());

        for (Future<T> future : futures) {
            try {
                T value;
                if (timeout != null) {
                    long remainingNanos = deadlineNanos - System.nanoTime();
                    if (remainingNanos <= 0) {
                        future.cancel(true);
                        results.add(Result.failure(OpenTimeoutException.of("invokeAllSettled", timeout)));
                        continue;
                    }
                    value = future.get(remainingNanos, TimeUnit.NANOSECONDS);
                } else {
                    value = future.get();
                }
                results.add(Result.success(value));
            } catch (TimeoutException e) {
                future.cancel(true);
                results.add(Result.failure(OpenTimeoutException.of("invokeAllSettled", timeout)));
            } catch (ExecutionException e) {
                results.add(Result.failure(unwrap(e)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.cancel(true);
                results.add(Result.failure(e));
                // Cancel all remaining futures — subsequent get() calls would
                // immediately throw IE again due to the restored interrupt flag
                for (int j = futures.indexOf(future) + 1; j < futures.size(); j++) {
                    futures.get(j).cancel(true);
                    results.add(Result.failure(e));
                }
                break;
            }
        }

        return List.copyOf(results);
    }

    /**
     * Unwrap ExecutionException to get the root cause.
     * 解包 ExecutionException 获取根本原因。
     */
    private static Throwable unwrap(ExecutionException e) {
        return e.getCause() != null ? e.getCause() : e;
    }

    /**
     * Compute deadline as nanoTime, clamped to avoid overflow.
     * 计算截止时间（纳秒），防止溢出。
     */
    private static long deadlineNanos(Duration timeout) {
        long now = System.nanoTime();
        long nanos = timeout.toNanos();
        // Clamp: if adding nanos would overflow, use Long.MAX_VALUE
        return nanos > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + nanos;
    }
}
