package cloud.opencode.base.parallel.pipeline;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Async Pipeline - Asynchronous Processing Pipeline
 * 异步流水线 - 异步处理流水线
 *
 * <p>A fluent API for chaining asynchronous operations with error handling
 * and timeout support.</p>
 * <p>用于链接异步操作的流式 API，支持错误处理和超时控制。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * String result = AsyncPipeline.of(() -> fetchData())
 *     .then(this::transform)
 *     .then(this::validate)
 *     .peek(data -> log.info("Processed: {}", data))
 *     .onError(e -> "fallback")
 *     .get(Duration.ofSeconds(30));
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for async operation chaining - 异步操作链的流式API</li>
 *   <li>Error handling and recovery - 错误处理和恢复</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Peek for side effects - Peek用于副作用</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (backed by CompletableFuture) - 线程安全: 是（基于CompletableFuture）</li>
 * </ul>
 * @param <T> the type of the pipeline value - 流水线值的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class AsyncPipeline<T> {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final CompletableFuture<T> future;

    /**
     * Constructs a new async pipeline.
     * 构造新的异步流水线。
     *
     * @param future the underlying future - 底层 Future
     */
    public AsyncPipeline(CompletableFuture<T> future) {
        this.future = future;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a pipeline from a CompletableFuture.
     * 从 CompletableFuture 创建流水线。
     *
     * @param future the future - Future
     * @param <T>    the result type - 结果类型
     * @return the pipeline - 流水线
     */
    public static <T> AsyncPipeline<T> of(CompletableFuture<T> future) {
        return new AsyncPipeline<>(future);
    }

    /**
     * Creates a pipeline with a completed value.
     * 使用已完成的值创建流水线。
     *
     * @param value the value - 值
     * @param <T>   the result type - 结果类型
     * @return the pipeline - 流水线
     */
    public static <T> AsyncPipeline<T> completed(T value) {
        return new AsyncPipeline<>(CompletableFuture.completedFuture(value));
    }

    /**
     * Creates a pipeline with a failed value.
     * 使用失败值创建流水线。
     *
     * @param error the error - 错误
     * @param <T>   the result type - 结果类型
     * @return the pipeline - 流水线
     */
    public static <T> AsyncPipeline<T> failed(Throwable error) {
        return new AsyncPipeline<>(CompletableFuture.failedFuture(error));
    }

    // ==================== Transformation ====================

    /**
     * Applies a transformation function.
     * 应用转换函数。
     *
     * @param fn  the transformation function - 转换函数
     * @param <R> the result type - 结果类型
     * @return the new pipeline - 新流水线
     */
    public <R> AsyncPipeline<R> then(Function<T, R> fn) {
        return new AsyncPipeline<>(future.thenApplyAsync(fn, EXECUTOR));
    }

    /**
     * Applies an async transformation function.
     * 应用异步转换函数。
     *
     * @param fn  the async transformation function - 异步转换函数
     * @param <R> the result type - 结果类型
     * @return the new pipeline - 新流水线
     */
    public <R> AsyncPipeline<R> thenAsync(Function<T, CompletableFuture<R>> fn) {
        return new AsyncPipeline<>(future.thenComposeAsync(fn, EXECUTOR));
    }

    /**
     * Peeks at the value without transforming it.
     * 查看值但不转换。
     *
     * @param action the action - 动作
     * @return this pipeline - 此流水线
     */
    public AsyncPipeline<T> peek(Consumer<T> action) {
        return new AsyncPipeline<>(future.thenApplyAsync(t -> {
            action.accept(t);
            return t;
        }, EXECUTOR));
    }

    /**
     * Filters the value.
     * 过滤值。
     *
     * @param predicate the predicate - 谓词
     * @return the new pipeline with Optional result - 带 Optional 结果的新流水线
     */
    public AsyncPipeline<T> filter(java.util.function.Predicate<T> predicate) {
        return new AsyncPipeline<>(future.thenApplyAsync(t -> {
            if (predicate.test(t)) {
                return t;
            }
            throw new IllegalStateException("Value did not match filter");
        }, EXECUTOR));
    }

    // ==================== Error Handling ====================

    /**
     * Handles errors with a recovery function.
     * 使用恢复函数处理错误。
     *
     * @param handler the error handler - 错误处理器
     * @return the new pipeline - 新流水线
     */
    public AsyncPipeline<T> onError(Function<Throwable, T> handler) {
        return new AsyncPipeline<>(future.exceptionally(handler));
    }

    /**
     * Handles errors with an async recovery function.
     * 使用异步恢复函数处理错误。
     *
     * @param handler the async error handler - 异步错误处理器
     * @return the new pipeline - 新流水线
     */
    public AsyncPipeline<T> onErrorAsync(Function<Throwable, CompletableFuture<T>> handler) {
        return new AsyncPipeline<>(future.exceptionallyComposeAsync(handler, EXECUTOR));
    }

    /**
     * Handles both success and error.
     * 同时处理成功和错误。
     *
     * @param handler the handler - 处理器
     * @param <R>     the result type - 结果类型
     * @return the new pipeline - 新流水线
     */
    public <R> AsyncPipeline<R> handle(java.util.function.BiFunction<T, Throwable, R> handler) {
        return new AsyncPipeline<>(future.handleAsync(handler, EXECUTOR));
    }

    // ==================== Combining ====================

    /**
     * Combines with another pipeline.
     * 与另一个流水线组合。
     *
     * @param other    the other pipeline - 另一个流水线
     * @param combiner the combiner function - 组合函数
     * @param <U>      the other type - 另一个类型
     * @param <R>      the result type - 结果类型
     * @return the new pipeline - 新流水线
     */
    public <U, R> AsyncPipeline<R> combine(AsyncPipeline<U> other,
                                            java.util.function.BiFunction<T, U, R> combiner) {
        return new AsyncPipeline<>(future.thenCombineAsync(other.future, combiner, EXECUTOR));
    }

    /**
     * Runs after another pipeline completes.
     * 在另一个流水线完成后运行。
     *
     * @param other the other pipeline - 另一个流水线
     * @return this pipeline - 此流水线
     */
    public AsyncPipeline<T> runAfter(AsyncPipeline<?> other) {
        return new AsyncPipeline<>(other.future.thenComposeAsync(_ -> future, EXECUTOR));
    }

    // ==================== Terminal Operations ====================

    /**
     * Gets the result, blocking if necessary.
     * 获取结果，如有必要则阻塞。
     *
     * @return the result - 结果
     */
    public T get() {
        return future.join();
    }

    /**
     * Gets the result with timeout.
     * 带超时获取结果。
     *
     * @param timeout the timeout - 超时
     * @return the result - 结果
     * @throws TimeoutException if timeout - 如果超时
     */
    public T get(Duration timeout) throws TimeoutException {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (ExecutionException e) {
            throw new OpenParallelException("Pipeline execution failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OpenParallelException("Pipeline execution interrupted", e);
        }
    }

    /**
     * Gets the result or default on error.
     * 获取结果或错误时返回默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the result or default - 结果或默认值
     */
    public T getOrDefault(T defaultValue) {
        try {
            return future.join();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets the result as Optional.
     * 获取结果为 Optional。
     *
     * @return the optional result - Optional 结果
     */
    public java.util.Optional<T> getOptional() {
        try {
            return java.util.Optional.ofNullable(future.join());
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Gets the underlying CompletableFuture.
     * 获取底层 CompletableFuture。
     *
     * @return the future - Future
     */
    public CompletableFuture<T> toFuture() {
        return future;
    }

    /**
     * Checks if the pipeline is completed.
     * 检查流水线是否完成。
     *
     * @return true if completed - 如果完成返回 true
     */
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Checks if the pipeline completed exceptionally.
     * 检查流水线是否异常完成。
     *
     * @return true if completed exceptionally - 如果异常完成返回 true
     */
    public boolean isCompletedExceptionally() {
        return future.isCompletedExceptionally();
    }

    /**
     * Cancels the pipeline.
     * 取消流水线。
     *
     * @param mayInterruptIfRunning whether to interrupt - 是否中断
     * @return true if cancelled - 如果取消返回 true
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }
}
