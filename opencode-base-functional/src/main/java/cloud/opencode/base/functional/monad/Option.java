package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Option Monad - Enhanced Optional with more functional operations
 * Option Monad - 增强的 Optional，提供更多函数式操作
 *
 * <p>A sealed type representing an optional value - either {@link Some} containing
 * a value or {@link None} representing absence.</p>
 * <p>一个密封类型表示可选值 - {@link Some} 包含值或 {@link None} 表示缺失。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed type with Some/None - 带 Some/None 的密封类型</li>
 *   <li>Monadic operations (map, flatMap, filter) - Monad 操作</li>
 *   <li>Interoperability with Optional - 与 Optional 互操作</li>
 *   <li>Pattern matching friendly - 模式匹配友好</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Creating Options
 * Option<String> some = Option.some("value");
 * Option<String> none = Option.none();
 * Option<String> fromNullable = Option.of(nullableValue);
 *
 * // Chained operations
 * String result = Option.of(user)
 *     .map(User::getAddress)
 *     .flatMap(addr -> Option.of(addr.getCity()))
 *     .map(String::toUpperCase)
 *     .getOrElse("UNKNOWN");
 *
 * // Pattern matching with JDK 25
 * String desc = switch (option) {
 *     case Option.Some(var v) -> "Has: " + v;
 *     case Option.None() -> "Empty";
 * };
 * }</pre>
 *
 * <p><strong>vs java.util.Optional | 与 Optional 对比:</strong></p>
 * <ul>
 *   <li>Sealed type enables pattern matching - 密封类型支持模式匹配</li>
 *   <li>Some/None explicitly typed - Some/None 显式类型</li>
 *   <li>Richer API - 更丰富的 API</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: None for null - 空值安全: null 返回 None</li>
 * </ul>
 *
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public sealed interface Option<T> permits Option.Some, Option.None {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Some with value
     * 创建包含值的 Some
     *
     * @param value the value (must not be null) - 值（不能为 null）
     * @param <T>   value type - 值类型
     * @return Some containing the value
     * @throws NullPointerException if value is null
     */
    static <T> Option<T> some(T value) {
        Objects.requireNonNull(value, "Some value cannot be null");
        return new Some<>(value);
    }

    /**
     * Create a None
     * 创建 None
     *
     * @param <T> value type - 值类型
     * @return None instance
     */
    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return (Option<T>) None.INSTANCE;
    }

    /**
     * Create Option from nullable value
     * 从可空值创建 Option
     *
     * @param value nullable value - 可空值
     * @param <T>   value type - 值类型
     * @return Some if value not null, None otherwise
     */
    static <T> Option<T> of(T value) {
        return value == null ? none() : new Some<>(value);
    }

    /**
     * Create Option from Optional
     * 从 Optional 创建 Option
     *
     * @param optional the optional - Optional
     * @param <T>      value type - 值类型
     * @return corresponding Option
     */
    static <T> Option<T> fromOptional(Optional<T> optional) {
        return optional.map(Option::some).orElseGet(Option::none);
    }

    /**
     * Create Option from supplier if condition is true
     * 如果条件为真则从供应商创建 Option
     *
     * @param condition condition - 条件
     * @param supplier  value supplier - 值供应商
     * @param <T>       value type - 值类型
     * @return Some if condition true and value not null, None otherwise
     */
    static <T> Option<T> when(boolean condition, Supplier<T> supplier) {
        return condition ? of(supplier.get()) : none();
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if this is Some
     * 检查是否为 Some
     *
     * @return true if Some - 如果是 Some 返回 true
     */
    boolean isSome();

    /**
     * Check if this is None
     * 检查是否为 None
     *
     * @return true if None - 如果是 None 返回 true
     */
    boolean isNone();

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the value, throws if None
     * 获取值，如果是 None 则抛出异常
     *
     * @return the value - 值
     * @throws NoSuchElementException if None
     */
    T get();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the value if Some
     * 如果是 Some 则转换值
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Option
     */
    <U> Option<U> map(Function<? super T, ? extends U> mapper);

    /**
     * Transform to another Option if Some
     * 如果是 Some 则转换为另一个 Option
     *
     * @param mapper transformation function returning Option - 返回 Option 的转换函数
     * @param <U>    result type - 结果类型
     * @return resulting Option
     */
    <U> Option<U> flatMap(Function<? super T, Option<U>> mapper);

    /**
     * Filter the value with predicate
     * 使用谓词过滤值
     *
     * @param predicate filter condition - 过滤条件
     * @return filtered Option (None if predicate not satisfied)
     */
    Option<T> filter(Predicate<? super T> predicate);

    // ==================== Fold | 折叠 ====================

    /**
     * Fold this Option by applying one of two functions
     * 通过应用两个函数之一来折叠此 Option
     *
     * <p>This is the catamorphism for Option - handles both cases symmetrically.</p>
     * <p>这是 Option 的态射 - 对称地处理两种情况。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * String result = option.fold(
     *     () -&gt; "No value",          // None case
     *     value -&gt; "Got: " + value    // Some case
     * );
     * </pre>
     *
     * @param ifNone function for None case - None 情况的函数
     * @param ifSome function for Some case - Some 情况的函数
     * @param <R>    result type - 结果类型
     * @return result of applying the appropriate function
     */
    <R> R fold(Supplier<? extends R> ifNone, Function<? super T, ? extends R> ifSome);

    // ==================== Recovery | 恢复 ====================

    /**
     * Get value or default if None
     * 获取值或默认值（如果是 None）
     *
     * @param defaultValue default value - 默认值
     * @return value or default
     */
    T getOrElse(T defaultValue);

    /**
     * Get value or compute default if None
     * 获取值或计算默认值（如果是 None）
     *
     * @param supplier default value supplier - 默认值供应商
     * @return value or computed default
     */
    T getOrElse(Supplier<? extends T> supplier);

    /**
     * Return this or other Option if None
     * 返回本 Option 或其他 Option（如果是 None）
     *
     * @param other alternative Option - 备选 Option
     * @return this if Some, other if None
     */
    Option<T> orElse(Option<T> other);

    /**
     * Return this or computed Option if None
     * 返回本 Option 或计算的 Option（如果是 None）
     *
     * @param supplier Option supplier - Option 供应商
     * @return this if Some, computed if None
     */
    Option<T> orElse(Supplier<Option<T>> supplier);

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to Optional
     * 转换为 Optional
     *
     * @return Optional
     */
    Optional<T> toOptional();

    /**
     * Convert to Either (None becomes Left)
     * 转换为 Either（None 变为 Left）
     *
     * @param left value for Left if None - None 时的 Left 值
     * @param <L>  left type - 左类型
     * @return Either
     */
    <L> Either<L, T> toEither(L left);

    // ==================== Side Effects | 副作用 ====================

    /**
     * Execute action if Some
     * 如果是 Some 则执行操作
     *
     * @param action action to execute - 要执行的操作
     * @return this Option for chaining
     */
    Option<T> peek(Consumer<? super T> action);

    /**
     * Execute action if None
     * 如果是 None 则执行操作
     *
     * @param action action to execute - 要执行的操作
     * @return this Option for chaining
     */
    Option<T> onNone(Runnable action);

    // ==================== Enhanced Operations | 增强操作 ====================

    /**
     * Check if this is a Some containing a value equal to the given value.
     * 检查是否为包含与给定值相等的值的 Some。
     *
     * @param value the value to compare - 要比较的值
     * @return true if Some and value equals contained value | 如果是 Some 且值相等返回 true
     */
    default boolean contains(T value) {
        if (this instanceof Some<T> s) {
            return Objects.equals(s.value(), value);
        }
        return false;
    }

    /**
     * Check if this is a Some and the predicate matches the contained value.
     * 检查是否为 Some 且谓词匹配包含的值。
     *
     * @param predicate the predicate to test - 要测试的谓词
     * @return true if Some and predicate matches | 如果是 Some 且谓词匹配返回 true
     */
    default boolean exists(Predicate<? super T> predicate) {
        if (this instanceof Some<T> s) {
            return predicate.test(s.value());
        }
        return false;
    }

    /**
     * Check if the predicate holds for all values. Returns true for None (vacuously true).
     * 检查谓词是否对所有值成立。对于 None 返回 true（空集满足）。
     *
     * @param predicate the predicate to test - 要测试的谓词
     * @return true if None or predicate matches | 如果是 None 或谓词匹配返回 true
     */
    default boolean forAll(Predicate<? super T> predicate) {
        if (this instanceof Some<T> s) {
            return predicate.test(s.value());
        }
        return true;
    }

    /**
     * Convert to Try. Some maps to Try.success(value), None maps to Try.failure with the supplied exception.
     * 转换为 Try。Some 映射为 Try.success(value)，None 映射为带有提供异常的 Try.failure。
     *
     * @param exceptionSupplier supplier for the exception when None - None 时异常的供应商
     * @return Try containing the value or failure | 包含值的 Try 或失败
     */
    default Try<T> toTry(Supplier<? extends Throwable> exceptionSupplier) {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier must not be null");
        if (this instanceof Some<T> s) {
            return Try.success(s.value());
        }
        return Try.failure(exceptionSupplier.get());
    }

    /**
     * Convert to Stream. Some maps to Stream.of(value), None maps to Stream.empty().
     * 转换为 Stream。Some 映射为 Stream.of(value)，None 映射为 Stream.empty()。
     *
     * @return Stream containing the value or empty | 包含值的 Stream 或空 Stream
     */
    default Stream<T> stream() {
        if (this instanceof Some<T> s) {
            return Stream.of(s.value());
        }
        return Stream.empty();
    }

    /**
     * Zip this Option with another using a combining function.
     * Both must be Some for the result to be Some.
     * 使用组合函数将此 Option 与另一个 Option 进行组合。
     * 两者都必须是 Some 才能得到 Some 结果。
     *
     * @param other  the other Option - 另一个 Option
     * @param zipper the combining function - 组合函数
     * @param <U>    the other value type - 另一个值类型
     * @param <R>    the result type - 结果类型
     * @return Some with combined value if both are Some, None otherwise | 如果两者都是 Some 返回组合值的 Some，否则返回 None
     */
    default <U, R> Option<R> zip(Option<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(zipper, "zipper must not be null");
        if (this instanceof Some<T> s && other instanceof Some<U> o) {
            return Option.of(zipper.apply(s.value(), o.value()));
        }
        return Option.none();
    }

    /**
     * Convert to Validation. Some maps to Validation.valid(value), None maps to Validation.invalid(error).
     * 转换为 Validation。Some 映射为 Validation.valid(value)，None 映射为 Validation.invalid(error)。
     *
     * @param error the error value for None case - None 情况下的错误值
     * @param <E>   error type - 错误类型
     * @return Validation containing the value or error | 包含值或错误的 Validation
     */
    default <E> Validation<E, T> toValidation(E error) {
        if (this instanceof Some<T> s) {
            return Validation.valid(s.value());
        }
        return Validation.invalid(error);
    }

    // ==================== Some Implementation | Some 实现 ====================

    /**
     * Some - Contains a value
     * Some - 包含值
     *
     * @param <T> value type - 值类型
     */
    record Some<T>(T value) implements Option<T> {
        public Some {
            Objects.requireNonNull(value, "Some value cannot be null");
        }

        @Override
        public boolean isSome() {
            return true;
        }

        @Override
        public boolean isNone() {
            return false;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
            return Option.of(mapper.apply(value));
        }

        @Override
        public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public Option<T> filter(Predicate<? super T> predicate) {
            return predicate.test(value) ? this : none();
        }

        @Override
        public <R> R fold(Supplier<? extends R> ifNone, Function<? super T, ? extends R> ifSome) {
            return ifSome.apply(value);
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public T getOrElse(Supplier<? extends T> supplier) {
            return value;
        }

        @Override
        public Option<T> orElse(Option<T> other) {
            return this;
        }

        @Override
        public Option<T> orElse(Supplier<Option<T>> supplier) {
            return this;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        @Override
        public <L> Either<L, T> toEither(L left) {
            return Either.right(value);
        }

        @Override
        public Option<T> peek(Consumer<? super T> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Option<T> onNone(Runnable action) {
            return this;
        }

        @Override
        public String toString() {
            return "Some[" + value + "]";
        }
    }

    // ==================== None Implementation | None 实现 ====================

    /**
     * None - Represents absence of value
     * None - 表示值缺失
     *
     * @param <T> value type - 值类型
     */
    final class None<T> implements Option<T> {
        @SuppressWarnings("rawtypes")
        private static final None INSTANCE = new None();

        private None() {
        }

        @Override
        public boolean isSome() {
            return false;
        }

        @Override
        public boolean isNone() {
            return true;
        }

        @Override
        public T get() {
            throw new NoSuchElementException("Option.None.get() called");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
            return (Option<U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
            return (Option<U>) this;
        }

        @Override
        public Option<T> filter(Predicate<? super T> predicate) {
            return this;
        }

        @Override
        public <R> R fold(Supplier<? extends R> ifNone, Function<? super T, ? extends R> ifSome) {
            return ifNone.get();
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElse(Supplier<? extends T> supplier) {
            return supplier.get();
        }

        @Override
        public Option<T> orElse(Option<T> other) {
            return other;
        }

        @Override
        public Option<T> orElse(Supplier<Option<T>> supplier) {
            return supplier.get();
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public <L> Either<L, T> toEither(L left) {
            return Either.left(left);
        }

        @Override
        public Option<T> peek(Consumer<? super T> action) {
            return this;
        }

        @Override
        public Option<T> onNone(Runnable action) {
            action.run();
            return this;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof None;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "None";
        }
    }
}
