package cloud.opencode.base.core.result;

import cloud.opencode.base.core.func.CheckedSupplier;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Result Monad - Represents the outcome of a computation that may succeed or fail
 * Result Monad - 表示可能成功或失败的计算结果
 *
 * <p>A sealed type that contains either a Success value or a Failure with a Throwable cause.
 * This provides a functional alternative to try-catch for error handling.</p>
 * <p>一个密封类型，包含 Success 值或带有 Throwable 原因的 Failure。
 * 这为错误处理提供了 try-catch 的函数式替代方案。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe error handling without exceptions - 无需异常的类型安全错误处理</li>
 *   <li>Monadic operations (map, flatMap) with automatic error catching - 自动捕获错误的 Monad 操作</li>
 *   <li>Recovery operations - 恢复操作</li>
 *   <li>Safe value extraction - 安全的值提取</li>
 *   <li>Conversion to Optional and Stream - 转换为 Optional 和 Stream</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wrapping a checked operation
 * Result<String> result = Result.of(() -> Files.readString(path));
 *
 * // Chaining operations (errors caught automatically in map)
 * Result<Integer> length = result
 *     .map(String::trim)
 *     .map(String::length);
 *
 * // Recovery
 * String value = result
 *     .recover(ex -> "default")
 *     .getOrElse("fallback");
 *
 * // Pattern matching with switch (JDK 25)
 * switch (result) {
 *     case Result.Success(var v) -> process(v);
 *     case Result.Failure(var e) -> handleError(e);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable records) - 线程安全: 是 (不可变记录)</li>
 *   <li>Failure.toString() hides stack traces for security - Failure.toString() 隐藏堆栈跟踪以保证安全</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Either
 * @since JDK 25, opencode-base-core V1.0.3
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Result by executing a CheckedSupplier, catching any exception as Failure
     * 通过执行 CheckedSupplier 创建 Result，捕获任何异常为 Failure
     *
     * @param supplier the checked supplier to execute - 要执行的受检供应商
     * @param <T>      value type - 值类型
     * @return Success with the value, or Failure with the caught exception
     */
    static <T> Result<T> of(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        try {
            return new Success<>(supplier.get());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    /**
     * Create a successful Result with the given value
     * 创建包含给定值的成功 Result
     *
     * @param value the success value (may be null for Result&lt;Void&gt;) - 成功值（对于 Result&lt;Void&gt; 可以为 null）
     * @param <T>   value type - 值类型
     * @return Success result
     */
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Create a failed Result with the given cause
     * 创建包含给定原因的失败 Result
     *
     * @param cause the failure cause (must not be null) - 失败原因（不能为 null）
     * @param <T>   value type - 值类型
     * @return Failure result
     * @throws NullPointerException if cause is null
     */
    static <T> Result<T> failure(Throwable cause) {
        return new Failure<>(cause);
    }

    /**
     * Create a successful Result&lt;Void&gt; with null value, for side-effect-only operations
     * 创建值为 null 的成功 Result&lt;Void&gt;，用于仅副作用操作
     *
     * @return Success result with null value
     */
    static Result<Void> successVoid() {
        return new Success<>(null);
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if this is a Success
     * 检查是否为成功
     *
     * @return true if Success - 如果成功返回 true
     */
    boolean isSuccess();

    /**
     * Check if this is a Failure
     * 检查是否为失败
     *
     * @return true if Failure - 如果失败返回 true
     */
    boolean isFailure();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the success value. If the mapper throws, returns Failure with the caught exception.
     * 转换成功值。如果映射函数抛出异常，返回包含捕获异常的 Failure。
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Result
     */
    <U> Result<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Transform the success value to another Result. If the mapper throws, returns Failure.
     * 将成功值转换为另一个 Result。如果映射函数抛出异常，返回 Failure。
     *
     * @param mapper transformation function returning Result - 返回 Result 的转换函数
     * @param <U>    result type - 结果类型
     * @return resulting Result
     */
    <U> Result<U> flatMap(Function<? super T, Result<U>> mapper);

    /**
     * Recover from a Failure by applying a function to the cause
     * 通过对原因应用函数来从 Failure 恢复
     *
     * @param recoverer recovery function that takes the cause and returns a value - 接受原因并返回值的恢复函数
     * @return Success with recovered value, or original Success
     */
    Result<T> recover(Function<? super Throwable, ? extends T> recoverer);

    /**
     * Recover from a Failure by applying a function that returns a Result
     * 通过应用返回 Result 的函数来从 Failure 恢复
     *
     * @param recoverer recovery function that takes the cause and returns a Result - 接受原因并返回 Result 的恢复函数
     * @return recovered Result, or original Success
     */
    Result<T> recoverWith(Function<? super Throwable, Result<T>> recoverer);

    // ==================== Side Effects | 副作用 ====================

    /**
     * Execute action on Success value
     * 对成功值执行操作
     *
     * @param action action to execute on success value - 对成功值执行的操作
     * @return this Result for chaining
     */
    Result<T> peek(Consumer<? super T> action);

    /**
     * Execute action on Failure cause
     * 对失败原因执行操作
     *
     * @param action action to execute on failure cause - 对失败原因执行的操作
     * @return this Result for chaining
     */
    Result<T> peekFailure(Consumer<? super Throwable> action);

    // ==================== Terminal Operations | 终端操作 ====================

    /**
     * Get the success value, or return default if Failure
     * 获取成功值，如果是 Failure 返回默认值
     *
     * @param defaultValue default value - 默认值
     * @return success value or default
     */
    T getOrElse(T defaultValue);

    /**
     * Get the success value, or compute default from Supplier if Failure
     * 获取成功值，如果是 Failure 从 Supplier 计算默认值
     *
     * @param supplier default value supplier - 默认值供应商
     * @return success value or computed default
     */
    T getOrElseGet(Supplier<? extends T> supplier);

    /**
     * Get the success value, or throw an exception mapped from the cause
     * 获取成功值，或抛出从原因映射的异常
     *
     * @param exceptionMapper function to map cause to throwable - 将原因映射为异常的函数
     * @param <X>             exception type - 异常类型
     * @return success value
     * @throws X if this is a Failure
     */
    <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) throws X;

    /**
     * Convert to Optional. Success becomes Optional.of(value), Failure becomes Optional.empty().
     * 转换为 Optional。Success 变为 Optional.of(value)，Failure 变为 Optional.empty()。
     *
     * @return Optional containing the success value, or empty if Failure
     */
    Optional<T> toOptional();

    /**
     * Convert to Stream. Success becomes a single-element Stream, Failure becomes an empty Stream.
     * 转换为 Stream。Success 变为单元素 Stream，Failure 变为空 Stream。
     *
     * @return Stream containing the success value, or empty if Failure
     */
    Stream<T> stream();

    // ==================== Success Implementation | Success 实现 ====================

    /**
     * Success - Represents a successful computation result
     * Success - 表示成功的计算结果
     *
     * @param value the success value (may be null for Result&lt;Void&gt;) - 成功值（对于 Result&lt;Void&gt; 可以为 null）
     * @param <T>   value type - 值类型
     */
    record Success<T>(T value) implements Result<T> {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
            try {
                return new Success<>(mapper.apply(value));
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }

        @Override
        public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
            try {
                return mapper.apply(value);
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }

        @Override
        public Result<T> recover(Function<? super Throwable, ? extends T> recoverer) {
            return this;
        }

        @Override
        public Result<T> recoverWith(Function<? super Throwable, Result<T>> recoverer) {
            return this;
        }

        @Override
        public Result<T> peek(Consumer<? super T> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Result<T> peekFailure(Consumer<? super Throwable> action) {
            return this;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public T getOrElseGet(Supplier<? extends T> supplier) {
            return value;
        }

        @Override
        public <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) {
            return value;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Stream<T> stream() {
            return value == null ? Stream.empty() : Stream.of(value);
        }

        @Override
        public String toString() {
            return "Success[" + value + "]";
        }
    }

    // ==================== Failure Implementation | Failure 实现 ====================

    /**
     * Failure - Represents a failed computation result
     * Failure - 表示失败的计算结果
     *
     * @param cause the failure cause (must not be null) - 失败原因（不能为 null）
     * @param <T>   value type - 值类型
     */
    record Failure<T>(Throwable cause) implements Result<T> {

        /**
         * Compact constructor enforcing non-null cause
         * 紧凑构造器强制 cause 不为 null
         */
        public Failure {
            Objects.requireNonNull(cause, "cause must not be null");
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
            return (Result<U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
            return (Result<U>) this;
        }

        @Override
        public Result<T> recover(Function<? super Throwable, ? extends T> recoverer) {
            try {
                return new Success<>(recoverer.apply(cause));
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }

        @Override
        public Result<T> recoverWith(Function<? super Throwable, Result<T>> recoverer) {
            try {
                return recoverer.apply(cause);
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }

        @Override
        public Result<T> peek(Consumer<? super T> action) {
            return this;
        }

        @Override
        public Result<T> peekFailure(Consumer<? super Throwable> action) {
            action.accept(cause);
            return this;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElseGet(Supplier<? extends T> supplier) {
            return supplier.get();
        }

        @Override
        public <X extends Throwable> T getOrElseThrow(Function<? super Throwable, ? extends X> exceptionMapper) throws X {
            throw exceptionMapper.apply(cause);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public Stream<T> stream() {
            return Stream.empty();
        }

        /**
         * Returns string representation showing only class name and message (no stack trace for security).
         * 返回仅显示类名和消息的字符串表示（出于安全考虑不包含堆栈跟踪）。
         *
         * @return string representation
         */
        @Override
        public String toString() {
            return "Failure[" + cause.getClass().getName() + ": " + cause.getMessage() + "]";
        }
    }
}
