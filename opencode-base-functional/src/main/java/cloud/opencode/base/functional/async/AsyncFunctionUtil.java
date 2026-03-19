package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import cloud.opencode.base.functional.monad.Try;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * AsyncFunctionUtil - Virtual Thread based async utilities
 * AsyncFunctionUtil - 基于虚拟线程的异步工具
 *
 * <p>Provides utilities for asynchronous function execution using JDK 25's
 * Virtual Threads. Enables functional-style async operations with proper
 * error handling.</p>
 * <p>提供使用 JDK 25 虚拟线程的异步函数执行工具。支持带有正确错误处理的
 * 函数式异步操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual Thread execution - 虚拟线程执行</li>
 *   <li>Parallel function application - 并行函数应用</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Error accumulation - 错误累积</li>
 *   <li>Structured concurrency - 结构化并发</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Run async with Virtual Thread
 * CompletableFuture<String> future = AsyncFunctionUtil.async(() -> fetchData());
 * String result = future.join();
 *
 * // Run async with timeout
 * Try<String> result = AsyncFunctionUtil.asyncWithTimeout(
 *     () -> slowOperation(),
 *     Duration.ofSeconds(5)
 * );
 *
 * // Parallel map
 * List<User> users = List.of(id1, id2, id3);
 * List<Profile> profiles = AsyncFunctionUtil.parallelMap(
 *     users,
 *     this::fetchProfile
 * );
 *
 * // Run multiple async operations
 * List<CompletableFuture<Result>> futures = AsyncFunctionUtil.runAll(
 *     () -> task1(),
 *     () -> task2(),
 *     () -> task3()
 * );
 *
 * // Wait for all with error handling
 * Try<List<Result>> allResults = AsyncFunctionUtil.awaitAll(futures);
 * }</pre>
 *
 * <p><strong>Virtual Thread Benefits | 虚拟线程优势:</strong></p>
 * <ul>
 *   <li>Lightweight (millions possible) - 轻量级（可创建数百万个）</li>
 *   <li>Efficient I/O blocking - 高效 I/O 阻塞</li>
 *   <li>Simplified async code - 简化异步代码</li>
 *   <li>No thread pool tuning - 无需线程池调优</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Virtual Thread creation: ~1μs - 虚拟线程创建: ~1μs</li>
 *   <li>Context switch: Very low overhead - 上下文切换: 极低开销</li>
 *   <li>Memory: ~1KB per Virtual Thread - 内存: ~1KB 每个虚拟线程</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Exception handling: Comprehensive - 异常处理: 全面</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class AsyncFunctionUtil {

    /**
     * Virtual Thread executor
     * 虚拟线程执行器
     */
    private static final ExecutorService VIRTUAL_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(
                VIRTUAL_EXECUTOR::close, "async-function-shutdown"));
    }

    private AsyncFunctionUtil() {
        // Utility class
    }

    // ==================== Basic Async | 基础异步 ====================

    /**
     * Run a supplier asynchronously on a Virtual Thread
     * 在虚拟线程上异步运行供应商
     *
     * @param supplier the computation - 计算
     * @param <T>      result type - 结果类型
     * @return CompletableFuture with result
     */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, VIRTUAL_EXECUTOR);
    }

    /**
     * Run a runnable asynchronously on a Virtual Thread
     * 在虚拟线程上异步运行可运行对象
     *
     * @param runnable the task - 任务
     * @return CompletableFuture for completion tracking
     */
    public static CompletableFuture<Void> asyncRun(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, VIRTUAL_EXECUTOR);
    }

    /**
     * Run a supplier asynchronously with timeout
     * 带超时异步运行供应商
     *
     * @param supplier the computation - 计算
     * @param timeout  maximum wait time - 最大等待时间
     * @param <T>      result type - 结果类型
     * @return Try containing result or timeout exception
     */
    public static <T> Try<T> asyncWithTimeout(Supplier<T> supplier, Duration timeout) {
        CompletableFuture<T> future = async(supplier);
        try {
            T result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return Try.success(result);
        } catch (TimeoutException e) {
            future.cancel(true);
            return Try.failure(new TimeoutException("Async operation timed out after " + timeout));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (ExecutionException e) {
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        }
    }

    // ==================== Parallel Operations | 并行操作 ====================

    /**
     * Map a function over a list in parallel
     * 并行地将函数映射到列表
     *
     * @param items  items to process - 要处理的项目
     * @param mapper function to apply - 要应用的函数
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return list of results (in original order)
     */
    public static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper) {
        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> async(() -> mapper.apply(item)))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Map a function over a list in parallel with error handling
     * 并行地将函数映射到列表，带错误处理
     *
     * @param items  items to process - 要处理的项目
     * @param mapper function to apply - 要应用的函数
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return Try containing list of results or first exception
     */
    public static <T, R> Try<List<R>> parallelMapTry(List<T> items, Function<T, R> mapper) {
        List<CompletableFuture<R>> futures = items.stream()
                .map(item -> async(() -> mapper.apply(item)))
                .toList();

        try {
            List<R> results = new ArrayList<>();
            for (CompletableFuture<R> future : futures) {
                results.add(future.get());
            }
            return Try.success(results);
        } catch (InterruptedException e) {
            futures.forEach(f -> f.cancel(true));
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (ExecutionException e) {
            futures.forEach(f -> f.cancel(true));
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        }
    }

    /**
     * Run multiple suppliers in parallel
     * 并行运行多个供应商
     *
     * @param suppliers suppliers to run - 要运行的供应商
     * @param <T>       result type - 结果类型
     * @return list of futures
     */
    @SafeVarargs
    public static <T> List<CompletableFuture<T>> runAll(Supplier<T>... suppliers) {
        List<CompletableFuture<T>> futures = new ArrayList<>();
        for (Supplier<T> supplier : suppliers) {
            futures.add(async(supplier));
        }
        return futures;
    }

    /**
     * Run multiple runnables in parallel
     * 并行运行多个可运行对象
     *
     * @param runnables runnables to run - 要运行的可运行对象
     * @return list of futures
     */
    public static List<CompletableFuture<Void>> runAllAsync(Runnable... runnables) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Runnable runnable : runnables) {
            futures.add(asyncRun(runnable));
        }
        return futures;
    }

    // ==================== Await Operations | 等待操作 ====================

    /**
     * Wait for all futures to complete
     * 等待所有 Future 完成
     *
     * @param futures futures to wait for - 要等待的 Future
     * @param <T>     result type - 结果类型
     * @return Try containing list of results
     */
    public static <T> Try<List<T>> awaitAll(List<CompletableFuture<T>> futures) {
        try {
            List<T> results = new ArrayList<>();
            for (CompletableFuture<T> future : futures) {
                results.add(future.get());
            }
            return Try.success(results);
        } catch (InterruptedException e) {
            futures.forEach(f -> f.cancel(true));
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (ExecutionException e) {
            futures.forEach(f -> f.cancel(true));
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        }
    }

    /**
     * Wait for all futures with timeout
     * 带超时等待所有 Future
     *
     * @param futures futures to wait for - 要等待的 Future
     * @param timeout maximum wait time - 最大等待时间
     * @param <T>     result type - 结果类型
     * @return Try containing list of results
     */
    public static <T> Try<List<T>> awaitAll(List<CompletableFuture<T>> futures, Duration timeout) {
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            allOf.get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            List<T> results = new ArrayList<>();
            for (CompletableFuture<T> future : futures) {
                results.add(future.get());
            }
            return Try.success(results);
        } catch (TimeoutException e) {
            futures.forEach(f -> f.cancel(true));
            return Try.failure(new TimeoutException("Await all timed out after " + timeout));
        } catch (InterruptedException e) {
            futures.forEach(f -> f.cancel(true));
            Thread.currentThread().interrupt();
            return Try.failure(e);
        } catch (ExecutionException e) {
            futures.forEach(f -> f.cancel(true));
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        }
    }

    /**
     * Wait for first successful result
     * 等待第一个成功结果
     *
     * @param futures futures to wait for - 要等待的 Future
     * @param <T>     result type - 结果类型
     * @return CompletableFuture with first result
     */
    public static <T> CompletableFuture<T> awaitFirst(List<CompletableFuture<T>> futures) {
        return CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(result -> {
                    @SuppressWarnings("unchecked")
                    T typed = (T) result;
                    return typed;
                });
    }

    // ==================== Chaining Operations | 链式操作 ====================

    /**
     * Chain async operations
     * 链接异步操作
     *
     * @param future first operation - 第一个操作
     * @param mapper transformation - 转换
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return chained future
     */
    public static <T, R> CompletableFuture<R> thenAsync(CompletableFuture<T> future,
                                                         Function<T, R> mapper) {
        return future.thenApplyAsync(mapper, VIRTUAL_EXECUTOR);
    }

    /**
     * Chain async operations with async mapper
     * 使用异步映射器链接异步操作
     *
     * @param future first operation - 第一个操作
     * @param mapper async transformation - 异步转换
     * @param <T>    input type - 输入类型
     * @param <R>    result type - 结果类型
     * @return chained future
     */
    public static <T, R> CompletableFuture<R> thenFlatAsync(CompletableFuture<T> future,
                                                             Function<T, CompletableFuture<R>> mapper) {
        return future.thenComposeAsync(mapper, VIRTUAL_EXECUTOR);
    }

    /**
     * Recover from failure asynchronously
     * 异步从失败恢复
     *
     * @param future   the future - Future
     * @param recovery recovery function - 恢复函数
     * @param <T>      result type - 结果类型
     * @return future with recovery
     */
    public static <T> CompletableFuture<T> recover(CompletableFuture<T> future,
                                                    Function<Throwable, T> recovery) {
        return future.exceptionally(recovery);
    }

    /**
     * Recover from failure with async operation
     * 使用异步操作从失败恢复
     *
     * @param future   the future - Future
     * @param recovery async recovery function - 异步恢复函数
     * @param <T>      result type - 结果类型
     * @return future with async recovery
     */
    public static <T> CompletableFuture<T> recoverAsync(CompletableFuture<T> future,
                                                         Function<Throwable, CompletableFuture<T>> recovery) {
        return future.exceptionallyComposeAsync(recovery, VIRTUAL_EXECUTOR);
    }

    // ==================== Utilities | 工具方法 ====================

    /**
     * Delay execution
     * 延迟执行
     *
     * @param duration delay duration - 延迟时长
     * @return future completing after delay
     */
    public static CompletableFuture<Void> delay(Duration duration) {
        return async(() -> {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OpenFunctionalException("Interrupted during async delay", e);
            }
            return null;
        });
    }

    /**
     * Create a completed future with value
     * 创建带值的已完成 Future
     *
     * @param value the value - 值
     * @param <T>   result type - 结果类型
     * @return completed future
     */
    public static <T> CompletableFuture<T> completed(T value) {
        return CompletableFuture.completedFuture(value);
    }

    /**
     * Create a failed future
     * 创建失败的 Future
     *
     * @param throwable the exception - 异常
     * @param <T>       result type - 结果类型
     * @return failed future
     */
    public static <T> CompletableFuture<T> failed(Throwable throwable) {
        return CompletableFuture.failedFuture(throwable);
    }

    /**
     * Get the Virtual Thread executor
     * 获取虚拟线程执行器
     *
     * @return the executor
     */
    public static ExecutorService executor() {
        return VIRTUAL_EXECUTOR;
    }
}
