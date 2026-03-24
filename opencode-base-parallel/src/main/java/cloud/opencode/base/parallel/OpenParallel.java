package cloud.opencode.base.parallel;

import cloud.opencode.base.parallel.batch.PartitionUtil;
import cloud.opencode.base.parallel.exception.OpenParallelException;
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;
import cloud.opencode.base.parallel.pipeline.AsyncPipeline;
import cloud.opencode.base.parallel.pipeline.TriFunction;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Open Parallel - Parallel Computing Utility
 * Open 并行 - 并行计算工具
 *
 * <p>A facade class providing static methods for parallel task execution
 * using JDK 25 virtual threads. Features include parallel execution,
 * batch processing, and async pipeline composition.</p>
 * <p>使用 JDK 25 虚拟线程提供并行任务执行的静态方法的门面类。
 * 特性包括并行执行、批处理和异步流水线组合。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Parallel execution
 * OpenParallel.runAll(
 *     () -> sendEmail(),
 *     () -> sendSMS(),
 *     () -> pushNotification()
 * );
 *
 * // Parallel with results
 * List<String> results = OpenParallel.invokeAll(
 *     () -> fetchFromServiceA(),
 *     () -> fetchFromServiceB()
 * );
 *
 * // Parallel map with concurrency limit
 * List<Result> processed = OpenParallel.parallelMap(items, item -> process(item), 10);
 *
 * // Async pipeline
 * String result = OpenParallel.pipeline(() -> fetchData())
 *     .then(this::transform)
 *     .onError(e -> "fallback")
 *     .get();
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
public final class OpenParallel {

    private static final ExecutorService VIRTUAL_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(
                VIRTUAL_EXECUTOR::close, "open-parallel-shutdown"));
    }

    private OpenParallel() {
        // Static utility class
    }

    // ==================== Parallel Execution ====================

    /**
     * Runs all tasks in parallel, waiting for completion.
     * 并行运行所有任务，等待完成。
     *
     * @param tasks the tasks to run - 要运行的任务
     */
    public static void runAll(Runnable... tasks) {
        runAll(Arrays.asList(tasks));
    }

    /**
     * Runs all tasks in parallel, waiting for completion.
     * 并行运行所有任务，等待完成。
     *
     * @param tasks the tasks to run - 要运行的任务
     */
    public static void runAll(Collection<Runnable> tasks) {
        List<CompletableFuture<Void>> futures = tasks.stream()
                .map(t -> CompletableFuture.runAsync(t, VIRTUAL_EXECUTOR))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Runs all tasks in parallel with timeout.
     * 并行运行所有任务，带超时。
     *
     * @param tasks   the tasks to run - 要运行的任务
     * @param timeout the timeout - 超时
     */
    public static void runAll(Collection<Runnable> tasks, Duration timeout) {
        List<CompletableFuture<Void>> futures = tasks.stream()
                .map(t -> CompletableFuture.runAsync(t, VIRTUAL_EXECUTOR))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw new OpenParallelException("Parallel execution failed", e);
        }
    }

    /**
     * Invokes all suppliers in parallel and collects results.
     * 并行调用所有 Supplier 并收集结果。
     *
     * @param suppliers the suppliers - Supplier
     * @param <T>       the result type - 结果类型
     * @return the results - 结果
     */
    @SafeVarargs
    public static <T> List<T> invokeAll(Supplier<T>... suppliers) {
        return invokeAll(Arrays.asList(suppliers));
    }

    /**
     * Invokes all suppliers in parallel and collects results.
     * 并行调用所有 Supplier 并收集结果。
     *
     * @param suppliers the suppliers - Supplier
     * @param <T>       the result type - 结果类型
     * @return the results - 结果
     */
    public static <T> List<T> invokeAll(Collection<Supplier<T>> suppliers) {
        List<CompletableFuture<T>> futures = suppliers.stream()
                .map(s -> CompletableFuture.supplyAsync(s, VIRTUAL_EXECUTOR))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Invokes all suppliers in parallel with timeout.
     * 并行调用所有 Supplier，带超时。
     *
     * @param suppliers the suppliers - Supplier
     * @param timeout   the timeout - 超时
     * @param <T>       the result type - 结果类型
     * @return the results - 结果
     */
    public static <T> List<T> invokeAll(Collection<Supplier<T>> suppliers, Duration timeout) {
        List<CompletableFuture<T>> futures = suppliers.stream()
                .map(s -> CompletableFuture.supplyAsync(s, VIRTUAL_EXECUTOR))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw new OpenParallelException("Parallel execution failed", e);
        }

        return futures.stream()
                .filter(f -> !f.isCancelled())
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Invokes all and returns the first to complete.
     * 调用所有并返回首个完成的。
     *
     * @param suppliers the suppliers - Supplier
     * @param <T>       the result type - 结果类型
     * @return the first result - 首个结果
     */
    @SafeVarargs
    public static <T> T invokeAny(Supplier<T>... suppliers) {
        return invokeAny(Arrays.asList(suppliers));
    }

    /**
     * Invokes all and returns the first to complete.
     * 调用所有并返回首个完成的。
     *
     * @param suppliers the suppliers - Supplier
     * @param <T>       the result type - 结果类型
     * @return the first result - 首个结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeAny(Collection<Supplier<T>> suppliers) {
        List<CompletableFuture<T>> futures = suppliers.stream()
                .map(s -> CompletableFuture.supplyAsync(s, VIRTUAL_EXECUTOR))
                .toList();

        return (T) CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                .join();
    }

    // ==================== Parallel Map ====================

    /**
     * Maps items in parallel using stream.
     * 使用流并行映射项目。
     *
     * @param items  the items - 项目
     * @param mapper the mapper function - 映射函数
     * @param <T>    the input type - 输入类型
     * @param <R>    the result type - 结果类型
     * @return the results - 结果
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper) {
        return items.parallelStream()
                .map(mapper)
                .toList();
    }

    /**
     * Maps items in parallel with concurrency limit.
     * 并行映射项目，带并发限制。
     *
     * @param items       the items - 项目
     * @param mapper      the mapper function - 映射函数
     * @param parallelism the max concurrency - 最大并发数
     * @param <T>         the input type - 输入类型
     * @param <R>         the result type - 结果类型
     * @return the results - 结果
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper,
                                              int parallelism) {
        Semaphore semaphore = new Semaphore(parallelism);

        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return mapper.apply(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new OpenParallelException("Interrupted", e);
                    } finally {
                        semaphore.release();
                    }
                }, VIRTUAL_EXECUTOR))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Maps items in parallel with concurrency limit and timeout.
     * 并行映射项目，带并发限制和超时。
     *
     * @param items       the items - 项目
     * @param mapper      the mapper function - 映射函数
     * @param parallelism the max concurrency - 最大并发数
     * @param timeout     the timeout - 超时
     * @param <T>         the input type - 输入类型
     * @param <R>         the result type - 结果类型
     * @return the results - 结果
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper,
                                              int parallelism, Duration timeout) {
        Semaphore semaphore = new Semaphore(parallelism);

        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return mapper.apply(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new OpenParallelException("Interrupted", e);
                    } finally {
                        semaphore.release();
                    }
                }, VIRTUAL_EXECUTOR))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            throw OpenParallelException.timeout(timeout);
        } catch (Exception e) {
            throw new OpenParallelException("Parallel map failed", e);
        }

        return futures.stream()
                .filter(f -> !f.isCancelled())
                .map(CompletableFuture::join)
                .toList();
    }

    // ==================== Batch Processing ====================

    /**
     * Processes items in batches.
     * 批量处理项目。
     *
     * @param items     the items - 项目
     * @param batchSize the batch size - 批大小
     * @param processor the batch processor - 批处理器
     * @param <T>       the item type - 项目类型
     */
    public static <T> void processBatch(List<T> items, int batchSize,
                                         Consumer<List<T>> processor) {
        List<List<T>> batches = PartitionUtil.partition(items, batchSize);

        List<CompletableFuture<Void>> futures = batches.stream()
                .map(batch -> CompletableFuture.runAsync(
                        () -> processor.accept(batch), VIRTUAL_EXECUTOR))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Processes items in batches with result collection.
     * 批量处理项目并收集结果。
     *
     * @param items     the items - 项目
     * @param batchSize the batch size - 批大小
     * @param processor the batch processor - 批处理器
     * @param <T>       the item type - 项目类型
     * @param <R>       the result type - 结果类型
     * @return the results - 结果
     */
    public static <T, R> List<R> processBatchAndCollect(List<T> items, int batchSize,
                                                         Function<List<T>, List<R>> processor) {
        List<List<T>> batches = PartitionUtil.partition(items, batchSize);

        List<CompletableFuture<List<R>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(
                        () -> processor.apply(batch), VIRTUAL_EXECUTOR))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
    }

    // ==================== Async Pipeline ====================

    /**
     * Creates an async pipeline from initial supplier.
     * 从初始 Supplier 创建异步流水线。
     *
     * @param initial the initial supplier - 初始 Supplier
     * @param <T>     the result type - 结果类型
     * @return the pipeline - 流水线
     */
    public static <T> AsyncPipeline<T> pipeline(Supplier<T> initial) {
        return new AsyncPipeline<>(CompletableFuture.supplyAsync(initial, VIRTUAL_EXECUTOR));
    }

    /**
     * Creates an async pipeline from existing future.
     * 从现有 Future 创建异步流水线。
     *
     * @param future the future - Future
     * @param <T>    the result type - 结果类型
     * @return the pipeline - 流水线
     */
    public static <T> AsyncPipeline<T> pipeline(CompletableFuture<T> future) {
        return new AsyncPipeline<>(future);
    }

    // ==================== Future Combination ====================

    /**
     * Combines two futures with a combiner function.
     * 使用组合函数组合两个 Future。
     *
     * @param f1       the first future - 第一个 Future
     * @param f2       the second future - 第二个 Future
     * @param combiner the combiner function - 组合函数
     * @param <T1>     the first type - 第一个类型
     * @param <T2>     the second type - 第二个类型
     * @param <R>      the result type - 结果类型
     * @return the combined future - 组合的 Future
     */
    public static <T1, T2, R> CompletableFuture<R> combine(
            CompletableFuture<T1> f1,
            CompletableFuture<T2> f2,
            BiFunction<T1, T2, R> combiner) {
        return f1.thenCombine(f2, combiner);
    }

    /**
     * Combines three futures with a combiner function.
     * 使用组合函数组合三个 Future。
     *
     * @param f1       the first future - 第一个 Future
     * @param f2       the second future - 第二个 Future
     * @param f3       the third future - 第三个 Future
     * @param combiner the combiner function - 组合函数
     * @param <T1>     the first type - 第一个类型
     * @param <T2>     the second type - 第二个类型
     * @param <T3>     the third type - 第三个类型
     * @param <R>      the result type - 结果类型
     * @return the combined future - 组合的 Future
     */
    public static <T1, T2, T3, R> CompletableFuture<R> combine(
            CompletableFuture<T1> f1,
            CompletableFuture<T2> f2,
            CompletableFuture<T3> f3,
            TriFunction<T1, T2, T3, R> combiner) {
        return CompletableFuture.allOf(f1, f2, f3)
                .thenApply(_ -> combiner.apply(f1.join(), f2.join(), f3.join()));
    }

    // ==================== Utility ====================

    /**
     * Creates an async supplier.
     * 创建异步 Supplier。
     *
     * @param supplier the supplier - Supplier
     * @param <T>      the result type - 结果类型
     * @return the future - Future
     */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, VIRTUAL_EXECUTOR);
    }

    /**
     * Creates an async runnable.
     * 创建异步 Runnable。
     *
     * @param runnable the runnable - Runnable
     * @return the future - Future
     */
    public static CompletableFuture<Void> async(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, VIRTUAL_EXECUTOR);
    }

    /**
     * Delays execution.
     * 延迟执行。
     *
     * @param delay    the delay - 延迟
     * @param supplier the supplier - Supplier
     * @param <T>      the result type - 结果类型
     * @return the future - Future
     */
    public static <T> CompletableFuture<T> delay(Duration delay, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delay);
                return supplier.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenParallelException("Delayed execution interrupted", e);
            }
        }, VIRTUAL_EXECUTOR);
    }

    /**
     * Gets the shared virtual thread executor.
     * 获取共享虚拟线程执行器。
     *
     * @return the executor - 执行器
     */
    public static ExecutorService getExecutor() {
        return VIRTUAL_EXECUTOR;
    }

    // ==================== Rate Limited Execution ====================

    /**
     * Creates a rate limited executor with specified permits per second.
     * 创建指定每秒许可数的限速执行器。
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * RateLimitedExecutor executor = OpenParallel.rateLimited(100);
     * executor.submit(() -> callApi());
     * }</pre>
     *
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @return the rate limited executor - 限速执行器
     */
    public static RateLimitedExecutor rateLimited(double permitsPerSecond) {
        return RateLimitedExecutor.create(permitsPerSecond);
    }

    /**
     * Creates a rate limited executor with specified rate and burst capacity.
     * 创建指定速率和突发容量的限速执行器。
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * // 100 requests per second, burst of 20
     * RateLimitedExecutor executor = OpenParallel.rateLimited(100, 20);
     * }</pre>
     *
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @param burstCapacity    the burst capacity - 突发容量
     * @return the rate limited executor - 限速执行器
     */
    public static RateLimitedExecutor rateLimited(double permitsPerSecond, long burstCapacity) {
        return RateLimitedExecutor.create(permitsPerSecond, burstCapacity);
    }

    /**
     * Executes tasks with rate limiting.
     * 使用限速执行任务。
     *
     * @param permitsPerSecond the permits per second - 每秒许可数
     * @param tasks            the tasks - 任务
     * @param <T>              the result type - 结果类型
     * @return the results - 结果
     */
    @SafeVarargs
    public static <T> List<T> invokeAllRateLimited(double permitsPerSecond,
                                                    Supplier<T>... tasks) {
        try (RateLimitedExecutor executor = RateLimitedExecutor.create(permitsPerSecond)) {
            return executor.invokeAll(
                    java.util.Arrays.stream(tasks)
                            .map(s -> (java.util.concurrent.Callable<T>) s::get)
                            .toList()
            );
        }
    }

}
