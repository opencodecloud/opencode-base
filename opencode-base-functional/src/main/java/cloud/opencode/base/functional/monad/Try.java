package cloud.opencode.base.functional.monad;

import cloud.opencode.base.core.func.CheckedSupplier;
import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Try Monad - Encapsulates computations that may fail
 * Try Monad - 封装可能失败的计算
 *
 * <p>A sealed type representing the result of a computation that may either
 * succeed with a value ({@link Success}) or fail with an exception ({@link Failure}).</p>
 * <p>一个密封类型，表示可能成功（带值）或失败（带异常）的计算结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception handling without try-catch - 无需 try-catch 的异常处理</li>
 *   <li>Monadic operations (map, flatMap, filter) - Monad 操作</li>
 *   <li>Recovery mechanisms - 恢复机制</li>
 *   <li>Conversion to Optional/Either - 转换为 Optional/Either</li>
 *   <li>Side effect handling - 副作用处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic usage
 * Try<Integer> result = Try.of(() -> Integer.parseInt("123"));
 *
 * // Chained operations
 * String message = Try.of(() -> readFile("config.json"))
 *     .map(this::parseJson)
 *     .map(config -> config.get("name"))
 *     .map(name -> "Hello, " + name)
 *     .getOrElse("Hello, Guest");
 *
 * // Error recovery
 * User user = Try.of(() -> userService.findById(id))
 *     .recover(e -> defaultUser)
 *     .get();
 *
 * // Side effects
 * Try.of(() -> sendEmail(to, subject, body))
 *     .onSuccess(r -> log.info("Email sent"))
 *     .onFailure(e -> log.error("Failed", e));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public sealed interface Try<T> permits Try.Success, Try.Failure {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Execute a computation that may fail
     * 执行可能失败的计算
     *
     * @param supplier computation to execute - 要执行的计算
     * @param <T>      result type - 结果类型
     * @return Success with result or Failure with exception
     */
    static <T> Try<T> of(CheckedSupplier<T> supplier) {
        try {
            return new Success<>(supplier.get());
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    /**
     * Create a Success with value
     * 创建成功结果
     *
     * @param value result value - 结果值
     * @param <T>   value type - 值类型
     * @return Success containing the value
     */
    static <T> Try<T> success(T value) {
        return new Success<>(value);
    }

    /**
     * Create a Failure with exception
     * 创建失败结果
     *
     * @param throwable the exception - 异常
     * @param <T>       value type - 值类型
     * @return Failure containing the exception
     */
    static <T> Try<T> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if this is a Success
     * 检查是否成功
     *
     * @return true if success - 如果成功返回 true
     */
    boolean isSuccess();

    /**
     * Check if this is a Failure
     * 检查是否失败
     *
     * @return true if failure - 如果失败返回 true
     */
    boolean isFailure();

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the value, throws if failure
     * 获取值，失败时抛出异常
     *
     * @return the value - 值
     * @throws OpenFunctionalException if this is a Failure
     */
    T get();

    /**
     * Get the exception if this is a Failure
     * 获取异常（如果失败）
     *
     * @return Optional containing the exception, empty if Success
     */
    Optional<Throwable> getCause();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the value if Success
     * 如果成功则转换值
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Try
     */
    <U> Try<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Transform the value to another Try if Success
     * 如果成功则转换为另一个 Try
     *
     * @param mapper transformation function returning Try - 返回 Try 的转换函数
     * @param <U>    result type - 结果类型
     * @return resulting Try
     */
    <U> Try<U> flatMap(Function<? super T, Try<U>> mapper);

    /**
     * Filter the value with a predicate
     * 使用谓词过滤值
     *
     * @param predicate filter condition - 过滤条件
     * @return filtered Try (Failure if predicate not satisfied)
     */
    Try<T> filter(Predicate<? super T> predicate);

    // ==================== Recovery | 恢复 ====================

    /**
     * Get value or default if Failure
     * 获取值或默认值（如果失败）
     *
     * @param defaultValue default value to use on failure - 失败时使用的默认值
     * @return value or default
     */
    T getOrElse(T defaultValue);

    /**
     * Return this or other Try if Failure
     * 返回本 Try 或其他 Try（如果失败）
     *
     * @param other alternative Try - 备选 Try
     * @return this if Success, other if Failure
     */
    Try<T> orElse(Try<T> other);

    /**
     * Recover from Failure with a function
     * 使用函数从失败恢复
     *
     * @param recovery recovery function - 恢复函数
     * @return recovered Try
     */
    Try<T> recover(Function<Throwable, T> recovery);

    /**
     * Recover from Failure with a function returning Try
     * 使用返回 Try 的函数从失败恢复
     *
     * @param recovery recovery function returning Try - 返回 Try 的恢复函数
     * @return recovered Try
     */
    Try<T> recoverWith(Function<Throwable, Try<T>> recovery);

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to Optional (empty if Failure)
     * 转换为 Optional（失败时为空）
     *
     * @return Optional containing value or empty
     */
    Optional<T> toOptional();

    /**
     * Convert to Either (Left=exception, Right=value)
     * 转换为 Either（Left=异常，Right=值）
     *
     * @return Either with exception or value
     */
    Either<Throwable, T> toEither();

    // ==================== Side Effects | 副作用 ====================

    /**
     * Execute action on the value if Success
     * 如果成功则对值执行操作
     *
     * @param action action to execute - 要执行的操作
     * @return this Try for chaining
     */
    Try<T> peek(Consumer<? super T> action);

    /**
     * Execute action on failure
     * 失败时执行操作
     *
     * @param action action to execute on exception - 对异常执行的操作
     * @return this Try for chaining
     */
    Try<T> onFailure(Consumer<Throwable> action);

    /**
     * Execute action on success
     * 成功时执行操作
     *
     * @param action action to execute on value - 对值执行的操作
     * @return this Try for chaining
     */
    Try<T> onSuccess(Consumer<? super T> action);

    // ==================== Enhanced Operations | 增强操作 ====================

    /**
     * Execute action regardless of success or failure, similar to try-finally semantics.
     * 无论成功或失败都执行操作，类似 try-finally 语义。
     *
     * <p>If the action succeeds, returns this Try unchanged.
     * If the action throws and this is a Success, returns a new Failure with the action's exception.
     * If the action throws and this is already a Failure, the action's exception is added
     * as a suppressed exception to the original cause, and the original Failure is returned.</p>
     * <p>如果操作成功，原样返回此 Try。
     * 如果操作抛出异常且当前为 Success，返回包含该异常的新 Failure。
     * 如果操作抛出异常且当前已为 Failure，操作异常作为 suppressed 异常添加到原始异常中，
     * 并返回原始 Failure。</p>
     *
     * @param action the action to execute - 要执行的操作
     * @return this Try if action succeeds, or Failure if action throws | 如果操作成功返回此 Try，否则返回 Failure
     */
    default Try<T> andFinally(Runnable action) {
        Objects.requireNonNull(action, "action must not be null");
        try {
            action.run();
            return this;
        } catch (Throwable t) {
            if (this instanceof Failure<T> f) {
                if (f.cause() != t) {
                    f.cause().addSuppressed(t);
                }
                return this;
            }
            return new Failure<>(t);
        }
    }

    /**
     * Transform the exception if this is a Failure. If Success, return this unchanged.
     * 如果是 Failure 则转换异常。如果是 Success 则原样返回。
     *
     * <p>If the mapper itself throws, returns a new {@link Failure} wrapping the mapper's exception,
     * with the original cause added as a suppressed exception (mirroring try-with-resources semantics).</p>
     * <p>如果 mapper 本身抛出异常，返回包装 mapper 异常的新 {@link Failure}，
     * 原始 cause 作为 suppressed 异常附加（类似 try-with-resources 语义）。</p>
     *
     * @param mapper function to transform the exception - 转换异常的函数
     * @return Try with mapped exception if Failure, or this if Success | 如果是 Failure 返回转换后的异常，否则返回此 Try
     */
    default Try<T> mapFailure(Function<Throwable, ? extends Throwable> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return this;
    }

    /**
     * Fold this Try into a single value by applying one of two functions.
     * 通过应用两个函数之一将此 Try 折叠为单个值。
     *
     * @param failureMapper function for Failure case - Failure 情况的函数
     * @param successMapper function for Success case - Success 情况的函数
     * @param <R>           result type - 结果类型
     * @return result of applying the appropriate function | 应用相应函数的结果
     */
    default <R> R fold(Function<Throwable, ? extends R> failureMapper,
                       Function<? super T, ? extends R> successMapper) {
        Objects.requireNonNull(failureMapper, "failureMapper must not be null");
        Objects.requireNonNull(successMapper, "successMapper must not be null");
        if (this instanceof Success<T> s) {
            return successMapper.apply(s.value());
        } else {
            return failureMapper.apply(((Failure<T>) this).cause());
        }
    }

    /**
     * Convert to Option. Success maps to Option.of(value), Failure maps to Option.none().
     * 转换为 Option。Success 映射为 Option.of(value)，Failure 映射为 Option.none()。
     *
     * @return Option containing the value or none | 包含值的 Option 或 none
     */
    default Option<T> toOption() {
        if (this instanceof Success<T> s) {
            return Option.of(s.value());
        }
        return Option.none();
    }

    /**
     * Convert to Validation. Success maps to Validation.valid(value), Failure maps to Validation.invalid(cause).
     * 转换为 Validation。Success 映射为 Validation.valid(value)，Failure 映射为 Validation.invalid(cause)。
     *
     * @return Validation containing the value or the cause | 包含值或异常原因的 Validation
     */
    default Validation<Throwable, T> toValidation() {
        if (this instanceof Success<T> s) {
            return Validation.valid(s.value());
        }
        return Validation.invalid(((Failure<T>) this).cause());
    }

    /**
     * Convert to Stream. Success maps to Stream.of(value), Failure maps to Stream.empty().
     * 转换为 Stream。Success 映射为 Stream.of(value)，Failure 映射为 Stream.empty()。
     *
     * @return Stream containing the value or empty | 包含值的 Stream 或空 Stream
     */
    default Stream<T> stream() {
        if (this instanceof Success<T> s) {
            return s.value() != null ? Stream.of(s.value()) : Stream.empty();
        }
        return Stream.empty();
    }

    /**
     * Check if this is a Success containing a value equal to the given value.
     * 检查是否为包含与给定值相等的值的 Success。
     *
     * @param value the value to compare - 要比较的值
     * @return true if Success and value equals contained value | 如果是 Success 且值相等返回 true
     */
    default boolean contains(T value) {
        if (this instanceof Success<T> s) {
            return Objects.equals(s.value(), value);
        }
        return false;
    }

    /**
     * Check if this is a Success and the predicate matches the contained value.
     * 检查是否为 Success 且谓词匹配包含的值。
     *
     * @param predicate the predicate to test - 要测试的谓词
     * @return true if Success and predicate matches | 如果是 Success 且谓词匹配返回 true
     */
    default boolean exists(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        if (this instanceof Success<T> s) {
            return predicate.test(s.value());
        }
        return false;
    }

    // ==================== Success Implementation | Success 实现 ====================

    /**
     * Success - Represents a successful computation
     * Success - 表示成功的计算
     *
     * @param <T> value type - 值类型
     */
    final class Success<T> implements Try<T> {
        private final T value;

        public Success(T value) {
            this.value = value;
        }

        /**
         * Get the contained value
         * 获取包含的值
         *
         * @return the value - 值
         */
        public T value() {
            return value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Optional<Throwable> getCause() {
            return Optional.empty();
        }

        @Override
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            return Try.of(() -> mapper.apply(value));
        }

        @Override
        public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
            try {
                return mapper.apply(value);
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }

        @Override
        public Try<T> filter(Predicate<? super T> predicate) {
            if (predicate.test(value)) {
                return this;
            }
            return new Failure<>(new NoSuchElementException("Predicate not satisfied"));
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public Try<T> orElse(Try<T> other) {
            return this;
        }

        @Override
        public Try<T> recover(Function<Throwable, T> recovery) {
            return this;
        }

        @Override
        public Try<T> recoverWith(Function<Throwable, Try<T>> recovery) {
            return this;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Either<Throwable, T> toEither() {
            return Either.right(value);
        }

        @Override
        public Try<T> peek(Consumer<? super T> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Try<T> onFailure(Consumer<Throwable> action) {
            return this;
        }

        @Override
        public Try<T> onSuccess(Consumer<? super T> action) {
            action.accept(value);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Success<?> success)) return false;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Success[" + value + "]";
        }
    }

    // ==================== Failure Implementation | Failure 实现 ====================

    /**
     * Failure - Represents a failed computation
     * Failure - 表示失败的计算
     *
     * @param <T> value type - 值类型
     */
    final class Failure<T> implements Try<T> {
        private final Throwable cause;

        public Failure(Throwable cause) {
            this.cause = java.util.Objects.requireNonNull(cause, "cause must not be null");
        }

        /**
         * Get the exception
         * 获取异常
         *
         * @return the exception - 异常
         */
        public Throwable cause() {
            return cause;
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
        public T get() {
            throw new OpenFunctionalException("Try.Failure.get() called", cause);
        }

        @Override
        public Optional<Throwable> getCause() {
            return Optional.of(cause);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Try<U> map(Function<? super T, ? extends U> mapper) {
            return (Try<U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
            return (Try<U>) this;
        }

        @Override
        public Try<T> filter(Predicate<? super T> predicate) {
            return this;
        }

        @Override
        public Try<T> mapFailure(Function<Throwable, ? extends Throwable> mapper) {
            try {
                return new Failure<>(mapper.apply(cause));
            } catch (Throwable t) {
                t.addSuppressed(cause);
                return new Failure<>(t);
            }
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public Try<T> orElse(Try<T> other) {
            return other;
        }

        @Override
        public Try<T> recover(Function<Throwable, T> recovery) {
            return Try.of(() -> recovery.apply(cause));
        }

        @Override
        public Try<T> recoverWith(Function<Throwable, Try<T>> recovery) {
            try {
                return recovery.apply(cause);
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public Either<Throwable, T> toEither() {
            return Either.left(cause);
        }

        @Override
        public Try<T> peek(Consumer<? super T> action) {
            return this;
        }

        @Override
        public Try<T> onFailure(Consumer<Throwable> action) {
            action.accept(cause);
            return this;
        }

        @Override
        public Try<T> onSuccess(Consumer<? super T> action) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Failure<?> failure)) return false;
            return Objects.equals(cause.getClass(), failure.cause.getClass()) &&
                   Objects.equals(cause.getMessage(), failure.cause.getMessage());
        }

        @Override
        public int hashCode() {
            return Objects.hash(cause.getClass(), cause.getMessage());
        }

        @Override
        public String toString() {
            return "Failure[" + cause + "]";
        }
    }
}
