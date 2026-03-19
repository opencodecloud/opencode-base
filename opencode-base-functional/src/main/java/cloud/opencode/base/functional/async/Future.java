package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.monad.Option;
import cloud.opencode.base.functional.monad.Try;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Future - Functional wrapper for CompletableFuture
 * Future - CompletableFuture 的函数式包装
 *
 * <p>A functional wrapper around CompletableFuture that provides a cleaner,
 * more composable API with proper error handling. Similar to Vavr's Future.</p>
 * <p>CompletableFuture 的函数式包装，提供更简洁、更可组合的 API 和正确的错误处理。
 * 类似于 Vavr 的 Future。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional API (map, flatMap, filter) - 函数式 API</li>
 *   <li>Error handling (recover, recoverWith) - 错误处理</li>
 *   <li>Timeout support - 超时支持</li>
 *   <li>Conversion to Try/Option - 转换为 Try/Option</li>
 *   <li>Combining futures (zip, sequence, traverse) - 组合 Future</li>
 *   <li>Virtual Thread support - 虚拟线程支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create and transform
 * Future<String> future = Future.of(() -> fetchData())
 *     .map(String::toUpperCase)
 *     .filter(s -> s.length() > 5);
 *
 * // Error handling
 * Future<String> safe = Future.of(() -> riskyOperation())
 *     .recover(ex -> "default value")
 *     .recoverWith(ex -> Future.successful("backup"));
 *
 * // Timeout
 * Future<String> withTimeout = future
 *     .timeout(Duration.ofSeconds(5))
 *     .recover(TimeoutException.class, _ -> "timeout fallback");
 *
 * // Combine futures
 * Future<String> combined = Future.zip(
 *     future1, future2, (a, b) -> a + b);
 *
 * // Await result
 * Try<String> result = future.await();
 * Option<String> value = future.toOption();
 *
 * // Using virtual threads
 * Future<String> vt = Future.ofVirtual(() -> compute());
 * }</pre>
 *
 * <p><strong>Comparison with CompletableFuture | 与 CompletableFuture 对比:</strong></p>
 * <ul>
 *   <li>Cleaner API (no thenApply/thenCompose distinction) - 更简洁的 API</li>
 *   <li>Better error handling - 更好的错误处理</li>
 *   <li>Returns Try on await - 等待时返回 Try</li>
 *   <li>Immutable operations - 不可变操作</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class Future<T> {

    private final CompletableFuture<T> delegate;
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(
                VIRTUAL_EXECUTOR::close, "future-executor-shutdown"));
    }

    private Future(CompletableFuture<T> delegate) {
        this.delegate = delegate;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Future from a supplier (uses ForkJoinPool).
     * 从供应商创建 Future（使用 ForkJoinPool）。
     *
     * @param <T>      value type - 值类型
     * @param supplier the computation - 计算
     * @return new Future - 新 Future
     */
    public static <T> Future<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Future<>(CompletableFuture.supplyAsync(supplier));
    }

    /**
     * Create a Future from a supplier with custom executor.
     * 使用自定义执行器从供应商创建 Future。
     *
     * @param <T>      value type - 值类型
     * @param supplier the computation - 计算
     * @param executor the executor - 执行器
     * @return new Future - 新 Future
     */
    public static <T> Future<T> of(Supplier<T> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        Objects.requireNonNull(executor, "executor must not be null");
        return new Future<>(CompletableFuture.supplyAsync(supplier, executor));
    }

    /**
     * Create a Future from a supplier using virtual threads.
     * 使用虚拟线程从供应商创建 Future。
     *
     * @param <T>      value type - 值类型
     * @param supplier the computation - 计算
     * @return new Future - 新 Future
     */
    public static <T> Future<T> ofVirtual(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return new Future<>(CompletableFuture.supplyAsync(supplier, VIRTUAL_EXECUTOR));
    }

    /**
     * Create a Future from a callable.
     * 从 Callable 创建 Future。
     *
     * @param <T>      value type - 值类型
     * @param callable the callable - Callable
     * @return new Future - 新 Future
     */
    public static <T> Future<T> fromCallable(Callable<T> callable) {
        Objects.requireNonNull(callable, "callable must not be null");
        return of(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Create a successful Future with the given value.
     * 使用给定值创建成功的 Future。
     *
     * @param <T>   value type - 值类型
     * @param value the value - 值
     * @return successful Future - 成功的 Future
     */
    public static <T> Future<T> successful(T value) {
        return new Future<>(CompletableFuture.completedFuture(value));
    }

    /**
     * Create a failed Future with the given exception.
     * 使用给定异常创建失败的 Future。
     *
     * @param <T>       value type - 值类型
     * @param exception the exception - 异常
     * @return failed Future - 失败的 Future
     */
    public static <T> Future<T> failed(Throwable exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        return new Future<>(CompletableFuture.failedFuture(exception));
    }

    /**
     * Create a Future from a CompletableFuture.
     * 从 CompletableFuture 创建 Future。
     *
     * @param <T> value type - 值类型
     * @param cf  the CompletableFuture - CompletableFuture
     * @return new Future - 新 Future
     */
    public static <T> Future<T> fromCompletableFuture(CompletableFuture<T> cf) {
        Objects.requireNonNull(cf, "CompletableFuture must not be null");
        return new Future<>(cf);
    }

    /**
     * Create a Future that never completes.
     * 创建永不完成的 Future。
     *
     * @param <T> value type - 值类型
     * @return never completing Future - 永不完成的 Future
     */
    public static <T> Future<T> never() {
        return new Future<>(new CompletableFuture<>());
    }

    // ==================== Transformation | 转换操作 ====================

    /**
     * Map the value to a new value.
     * 将值映射为新值。
     *
     * @param <R>    result type - 结果类型
     * @param mapper mapping function - 映射函数
     * @return mapped Future - 映射后的 Future
     */
    public <R> Future<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Future<>(delegate.thenApply(mapper));
    }

    /**
     * FlatMap to another Future.
     * 扁平映射到另一个 Future。
     *
     * @param <R>    result type - 结果类型
     * @param mapper mapping function - 映射函数
     * @return flattened Future - 扁平化的 Future
     */
    public <R> Future<R> flatMap(Function<? super T, ? extends Future<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Future<>(delegate.thenCompose(t -> mapper.apply(t).toCompletableFuture()));
    }

    /**
     * Filter the value.
     * 过滤值。
     *
     * @param predicate filter predicate - 过滤谓词
     * @return filtered Future (may be failed with NoSuchElementException) - 过滤后的 Future
     */
    public Future<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return new Future<>(delegate.thenApply(t -> {
            if (predicate.test(t)) {
                return t;
            }
            throw new java.util.NoSuchElementException("Predicate does not match");
        }));
    }

    /**
     * Execute side effect on success.
     * 成功时执行副作用。
     *
     * @param action the action - 操作
     * @return this Future - 此 Future
     */
    public Future<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action must not be null");
        delegate.thenAccept(action);
        return this;
    }

    /**
     * Execute side effect on failure.
     * 失败时执行副作用。
     *
     * @param action the action - 操作
     * @return this Future - 此 Future
     */
    public Future<T> onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action must not be null");
        delegate.exceptionally(ex -> {
            action.accept(ex);
            return null;
        });
        return this;
    }

    /**
     * Execute side effect on completion.
     * 完成时执行副作用。
     *
     * @param action the action - 操作
     * @return this Future - 此 Future
     */
    public Future<T> onComplete(BiConsumer<? super T, ? super Throwable> action) {
        Objects.requireNonNull(action, "action must not be null");
        delegate.whenComplete(action);
        return this;
    }

    // ==================== Error Handling | 错误处理 ====================

    /**
     * Recover from any failure.
     * 从任何失败恢复。
     *
     * @param recovery recovery function - 恢复函数
     * @return recovered Future - 恢复后的 Future
     */
    public Future<T> recover(Function<? super Throwable, ? extends T> recovery) {
        Objects.requireNonNull(recovery, "recovery must not be null");
        return new Future<>(delegate.exceptionally(ex -> recovery.apply(ex)));
    }

    /**
     * Recover from specific exception type.
     * 从特定异常类型恢复。
     *
     * @param <E>           exception type - 异常类型
     * @param exceptionType exception class - 异常类
     * @param recovery      recovery function - 恢复函数
     * @return recovered Future - 恢复后的 Future
     */
    public <E extends Throwable> Future<T> recover(
            Class<E> exceptionType,
            Function<? super E, ? extends T> recovery) {
        Objects.requireNonNull(exceptionType, "exceptionType must not be null");
        Objects.requireNonNull(recovery, "recovery must not be null");
        return new Future<>(delegate.exceptionally(ex -> {
            Throwable cause = ex instanceof CompletionException ce ? ce.getCause() : ex;
            if (exceptionType.isInstance(cause)) {
                return recovery.apply(exceptionType.cast(cause));
            }
            throw (ex instanceof RuntimeException re) ? re : new CompletionException(ex);
        }));
    }

    /**
     * Recover with another Future.
     * 使用另一个 Future 恢复。
     *
     * @param recovery recovery function - 恢复函数
     * @return recovered Future - 恢复后的 Future
     */
    public Future<T> recoverWith(Function<? super Throwable, ? extends Future<T>> recovery) {
        Objects.requireNonNull(recovery, "recovery must not be null");
        return new Future<>(delegate.exceptionallyCompose(ex -> recovery.apply(ex).toCompletableFuture()));
    }

    /**
     * Provide a fallback value on failure.
     * 失败时提供备用值。
     *
     * @param fallback the fallback value - 备用值
     * @return Future with fallback - 带备用值的 Future
     */
    public Future<T> orElse(T fallback) {
        return recover(_ -> fallback);
    }

    /**
     * Provide a fallback Future on failure.
     * 失败时提供备用 Future。
     *
     * @param fallback the fallback Future - 备用 Future
     * @return Future with fallback - 带备用的 Future
     */
    public Future<T> orElse(Future<T> fallback) {
        Objects.requireNonNull(fallback, "fallback must not be null");
        return recoverWith(_ -> fallback);
    }

    // ==================== Timeout | 超时 ====================

    /**
     * Apply a timeout to this Future.
     * 对此 Future 应用超时。
     *
     * @param duration timeout duration - 超时时长
     * @return Future with timeout - 带超时的 Future
     */
    public Future<T> timeout(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return new Future<>(delegate.orTimeout(duration.toMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Apply a timeout with default value.
     * 应用超时并使用默认值。
     *
     * @param duration     timeout duration - 超时时长
     * @param defaultValue value on timeout - 超时时的值
     * @return Future with timeout - 带超时的 Future
     */
    public Future<T> timeout(Duration duration, T defaultValue) {
        Objects.requireNonNull(duration, "duration must not be null");
        return new Future<>(delegate.completeOnTimeout(defaultValue, duration.toMillis(), TimeUnit.MILLISECONDS));
    }

    // ==================== Await / Get | 等待/获取 ====================

    /**
     * Await the result, returning a Try.
     * 等待结果，返回 Try。
     *
     * @return Try containing result or exception - 包含结果或异常的 Try
     */
    public Try<T> await() {
        try {
            return Try.success(delegate.join());
        } catch (CompletionException e) {
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Await the result with timeout, returning a Try.
     * 使用超时等待结果，返回 Try。
     *
     * @param duration timeout duration - 超时时长
     * @return Try containing result or exception - 包含结果或异常的 Try
     */
    public Try<T> await(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        try {
            return Try.success(delegate.get(duration.toMillis(), TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            return Try.failure(e);
        } catch (ExecutionException e) {
            return Try.failure(e.getCause() != null ? e.getCause() : e);
        } catch (Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Get the value, throwing if failed.
     * 获取值，如果失败则抛出异常。
     *
     * @return the value - 值
     * @throws CompletionException if failed - 如果失败
     */
    public T get() {
        return delegate.join();
    }

    /**
     * Get the value with default on failure.
     * 获取值，失败时使用默认值。
     *
     * @param defaultValue default value - 默认值
     * @return the value or default - 值或默认值
     */
    public T getOrElse(T defaultValue) {
        try {
            return delegate.join();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== Status | 状态 ====================

    /**
     * Check if completed.
     * 检查是否完成。
     *
     * @return true if done - 如果完成返回 true
     */
    public boolean isCompleted() {
        return delegate.isDone();
    }

    /**
     * Check if completed successfully.
     * 检查是否成功完成。
     *
     * @return true if successful - 如果成功返回 true
     */
    public boolean isSuccess() {
        return delegate.isDone() && !delegate.isCompletedExceptionally();
    }

    /**
     * Check if failed.
     * 检查是否失败。
     *
     * @return true if failed - 如果失败返回 true
     */
    public boolean isFailure() {
        return delegate.isCompletedExceptionally();
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to Option (None on failure).
     * 转换为 Option（失败时为 None）。
     *
     * @return Option containing value - 包含值的 Option
     */
    public Option<T> toOption() {
        return Option.fromOptional(await().toOptional());
    }

    /**
     * Convert to Try.
     * 转换为 Try。
     *
     * @return Try containing result - 包含结果的 Try
     */
    public Try<T> toTry() {
        return await();
    }

    /**
     * Get the underlying CompletableFuture.
     * 获取底层的 CompletableFuture。
     *
     * @return CompletableFuture - CompletableFuture
     */
    public CompletableFuture<T> toCompletableFuture() {
        return delegate;
    }

    // ==================== Combining | 组合 ====================

    /**
     * Zip two futures into one.
     * 将两个 Future 合并为一个。
     *
     * @param <A>     first type - 第一个类型
     * @param <B>     second type - 第二个类型
     * @param <R>     result type - 结果类型
     * @param futureA first future - 第一个 Future
     * @param futureB second future - 第二个 Future
     * @param zipper  combining function - 组合函数
     * @return combined Future - 组合后的 Future
     */
    public static <A, B, R> Future<R> zip(
            Future<A> futureA,
            Future<B> futureB,
            BiFunction<? super A, ? super B, ? extends R> zipper) {
        Objects.requireNonNull(futureA, "futureA must not be null");
        Objects.requireNonNull(futureB, "futureB must not be null");
        Objects.requireNonNull(zipper, "zipper must not be null");
        return new Future<>(futureA.delegate.thenCombine(futureB.delegate, zipper));
    }

    /**
     * Zip this future with another.
     * 将此 Future 与另一个合并。
     *
     * @param <U>    other type - 其他类型
     * @param <R>    result type - 结果类型
     * @param other  other future - 其他 Future
     * @param zipper combining function - 组合函数
     * @return combined Future - 组合后的 Future
     */
    public <U, R> Future<R> zipWith(Future<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return zip(this, other, zipper);
    }

    /**
     * Combine a list of futures into a future of list.
     * 将 Future 列表组合为列表的 Future。
     *
     * @param <T>     element type - 元素类型
     * @param futures the futures - Future 列表
     * @return Future of list - 列表的 Future
     */
    @SafeVarargs
    public static <T> Future<List<T>> sequence(Future<T>... futures) {
        return sequence(List.of(futures));
    }

    /**
     * Combine a list of futures into a future of list.
     * 将 Future 列表组合为列表的 Future。
     *
     * @param <T>     element type - 元素类型
     * @param futures the futures - Future 列表
     * @return Future of list - 列表的 Future
     */
    public static <T> Future<List<T>> sequence(List<Future<T>> futures) {
        Objects.requireNonNull(futures, "futures must not be null");
        if (futures.isEmpty()) {
            return successful(List.of());
        }

        @SuppressWarnings("unchecked")
        CompletableFuture<T>[] cfs = futures.stream()
                .map(f -> f.delegate)
                .toArray(CompletableFuture[]::new);

        return new Future<>(CompletableFuture.allOf(cfs)
                .thenApply(_ -> futures.stream()
                        .map(f -> f.delegate.join())
                        .toList()));
    }

    /**
     * Transform a collection into a future of list using a mapping function.
     * 使用映射函数将集合转换为列表的 Future。
     *
     * @param <A>      source type - 源类型
     * @param <B>      result type - 结果类型
     * @param items    the items - 项目
     * @param function mapping function - 映射函数
     * @return Future of list - 列表的 Future
     */
    public static <A, B> Future<List<B>> traverse(
            Iterable<A> items,
            Function<? super A, ? extends Future<B>> function) {
        Objects.requireNonNull(items, "items must not be null");
        Objects.requireNonNull(function, "function must not be null");

        List<Future<B>> futures = new java.util.ArrayList<>();
        for (A item : items) {
            futures.add(function.apply(item));
        }
        return sequence(futures);
    }

    /**
     * Return the first completed future.
     * 返回第一个完成的 Future。
     *
     * @param <T>     element type - 元素类型
     * @param futures the futures - Future 列表
     * @return first completed - 第一个完成的
     */
    @SafeVarargs
    public static <T> Future<T> firstCompleted(Future<T>... futures) {
        Objects.requireNonNull(futures, "futures must not be null");
        if (futures.length == 0) {
            return never();
        }

        @SuppressWarnings("unchecked")
        CompletableFuture<T>[] cfs = java.util.Arrays.stream(futures)
                .map(f -> f.delegate)
                .toArray(CompletableFuture[]::new);

        return new Future<>(CompletableFuture.anyOf(cfs).thenApply(o -> {
            @SuppressWarnings("unchecked")
            T t = (T) o;
            return t;
        }));
    }

    // ==================== Async Operations | 异步操作 ====================

    /**
     * Run action after completion on a virtual thread.
     * 完成后在虚拟线程上运行操作。
     *
     * @param <R>    result type - 结果类型
     * @param action the action - 操作
     * @return new Future - 新 Future
     */
    public <R> Future<R> andThenVirtual(Function<? super T, ? extends R> action) {
        Objects.requireNonNull(action, "action must not be null");
        return new Future<>(delegate.thenApplyAsync(action, VIRTUAL_EXECUTOR));
    }

    /**
     * FlatMap on virtual thread.
     * 在虚拟线程上扁平映射。
     *
     * @param <R>    result type - 结果类型
     * @param mapper mapping function - 映射函数
     * @return new Future - 新 Future
     */
    public <R> Future<R> flatMapVirtual(Function<? super T, ? extends Future<R>> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return new Future<>(delegate.thenComposeAsync(t -> mapper.apply(t).toCompletableFuture(), VIRTUAL_EXECUTOR));
    }
}
