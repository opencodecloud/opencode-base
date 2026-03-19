package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Validation Monad - Accumulating error validation
 * Validation Monad - 累积错误的验证
 *
 * <p>A sealed type for validation that accumulates all errors instead of
 * failing fast. Unlike Either, Validation collects multiple errors.</p>
 * <p>用于验证的密封类型，累积所有错误而不是快速失败。与 Either 不同，Validation 收集多个错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error accumulation - 错误累积</li>
 *   <li>Applicative functor operations - 应用函子操作</li>
 *   <li>Combine multiple validations - 组合多个验证</li>
 *   <li>Convert to Either - 转换为 Either</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Define validations
 * Validation<String, String> validateName(String name) {
 *     if (name == null || name.isBlank())
 *         return Validation.invalid("Name required");
 *     return Validation.valid(name);
 * }
 *
 * Validation<String, Integer> validateAge(int age) {
 *     if (age < 0) return Validation.invalid("Age must be positive");
 *     if (age > 150) return Validation.invalid("Age unrealistic");
 *     return Validation.valid(age);
 * }
 *
 * // Combine validations
 * Validation<String, User> result = Validation.combine(
 *     validateName(name),
 *     validateAge(age),
 *     User::new
 * );
 *
 * // Handle result
 * if (result.isInvalid()) {
 *     result.getErrors().forEach(System.err::println);
 * }
 * }</pre>
 *
 * <p><strong>vs Either | 与 Either 对比:</strong></p>
 * <ul>
 *   <li>Either: Fails fast (first error only) - 快速失败（仅第一个错误）</li>
 *   <li>Validation: Accumulates all errors - 累积所有错误</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param <E> error type - 错误类型
 * @param <T> value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public sealed interface Validation<E, T> permits Validation.Valid, Validation.Invalid {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Valid result
     * 创建有效结果
     *
     * @param value the value - 值
     * @param <E>   error type - 错误类型
     * @param <T>   value type - 值类型
     * @return Valid containing the value
     */
    static <E, T> Validation<E, T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Create an Invalid result with single error
     * 创建带单个错误的无效结果
     *
     * @param error the error - 错误
     * @param <E>   error type - 错误类型
     * @param <T>   value type - 值类型
     * @return Invalid containing the error
     */
    static <E, T> Validation<E, T> invalid(E error) {
        return new Invalid<>(List.of(error));
    }

    /**
     * Create an Invalid result with multiple errors
     * 创建带多个错误的无效结果
     *
     * @param errors the errors - 错误列表
     * @param <E>    error type - 错误类型
     * @param <T>    value type - 值类型
     * @return Invalid containing the errors
     */
    static <E, T> Validation<E, T> invalid(List<E> errors) {
        return new Invalid<>(errors);
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if this is Valid
     * 检查是否有效
     *
     * @return true if valid - 如果有效返回 true
     */
    boolean isValid();

    /**
     * Check if this is Invalid
     * 检查是否无效
     *
     * @return true if invalid - 如果无效返回 true
     */
    boolean isInvalid();

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the value if Valid
     * 获取值（如果有效）
     *
     * @return Optional containing value
     */
    Optional<T> getValue();

    /**
     * Get the errors (empty list if Valid)
     * 获取错误列表（如果有效则为空）
     *
     * @return list of errors
     */
    List<E> getErrors();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the value if Valid
     * 如果有效则转换值
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Validation
     */
    <U> Validation<E, U> map(Function<? super T, ? extends U> mapper);

    /**
     * FlatMap the value if Valid (monadic bind)
     * 如果有效则扁平映射值（单子绑定）
     *
     * <p>Note: Unlike {@link #ap}, flatMap does NOT accumulate errors.
     * If you need error accumulation, use {@link #ap} or {@link #combine}.</p>
     * <p>注意：与 {@link #ap} 不同，flatMap 不会累积错误。
     * 如果需要累积错误，请使用 {@link #ap} 或 {@link #combine}。</p>
     *
     * @param mapper function returning Validation - 返回 Validation 的函数
     * @param <U>    result type - 结果类型
     * @return resulting Validation
     */
    <U> Validation<E, U> flatMap(Function<? super T, Validation<E, U>> mapper);

    /**
     * Apply a validated function to this validation (applicative)
     * 将验证的函数应用于此验证（应用函子）
     *
     * <p>This is the key operation for accumulating errors.</p>
     * <p>这是累积错误的关键操作。</p>
     *
     * @param vf validation containing a function - 包含函数的验证
     * @param <U> result type - 结果类型
     * @return combined Validation
     */
    <U> Validation<E, U> ap(Validation<E, Function<? super T, ? extends U>> vf);

    // ==================== Fold | 折叠 ====================

    /**
     * Fold this Validation by applying one of two functions
     * 通过应用两个函数之一来折叠此 Validation
     *
     * <p>This is the catamorphism for Validation - handles both cases symmetrically.</p>
     * <p>这是 Validation 的态射 - 对称地处理两种情况。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>
     * String result = validation.fold(
     *     errors -&gt; "Errors: " + errors.size(),  // Invalid case
     *     value -&gt; "Success: " + value           // Valid case
     * );
     * </pre>
     *
     * @param ifInvalid function for Invalid case (receives error list) - Invalid 情况的函数（接收错误列表）
     * @param ifValid   function for Valid case - Valid 情况的函数
     * @param <R>       result type - 结果类型
     * @return result of applying the appropriate function
     */
    <R> R fold(Function<? super List<E>, ? extends R> ifInvalid,
               Function<? super T, ? extends R> ifValid);

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to Either (errors as Left, value as Right)
     * 转换为 Either（错误为 Left，值为 Right）
     *
     * @return Either
     */
    Either<List<E>, T> toEither();

    // ==================== Combinators | 组合器 ====================

    /**
     * Combine two validations
     * 组合两个验证
     *
     * @param v1       first validation - 第一个验证
     * @param v2       second validation - 第二个验证
     * @param combiner function to combine values - 组合值的函数
     * @param <E>      error type - 错误类型
     * @param <T1>     first value type - 第一个值类型
     * @param <T2>     second value type - 第二个值类型
     * @param <R>      result type - 结果类型
     * @return combined Validation
     */
    static <E, T1, T2, R> Validation<E, R> combine(
            Validation<E, T1> v1,
            Validation<E, T2> v2,
            BiFunction<T1, T2, R> combiner) {
        if (v1.isValid() && v2.isValid()) {
            return valid(combiner.apply(v1.getValue().get(), v2.getValue().get()));
        }
        List<E> errors = new ArrayList<>();
        errors.addAll(v1.getErrors());
        errors.addAll(v2.getErrors());
        return invalid(errors);
    }

    /**
     * Combine three validations
     * 组合三个验证
     *
     * @param v1       first validation - 第一个验证
     * @param v2       second validation - 第二个验证
     * @param v3       third validation - 第三个验证
     * @param combiner function to combine values - 组合值的函数
     * @param <E>      error type - 错误类型
     * @param <T1>     first value type - 第一个值类型
     * @param <T2>     second value type - 第二个值类型
     * @param <T3>     third value type - 第三个值类型
     * @param <R>      result type - 结果类型
     * @return combined Validation
     */
    static <E, T1, T2, T3, R> Validation<E, R> combine(
            Validation<E, T1> v1,
            Validation<E, T2> v2,
            Validation<E, T3> v3,
            TriFunction<T1, T2, T3, R> combiner) {
        if (v1.isValid() && v2.isValid() && v3.isValid()) {
            return valid(combiner.apply(
                v1.getValue().get(),
                v2.getValue().get(),
                v3.getValue().get()
            ));
        }
        List<E> errors = new ArrayList<>();
        errors.addAll(v1.getErrors());
        errors.addAll(v2.getErrors());
        errors.addAll(v3.getErrors());
        return invalid(errors);
    }

    /**
     * Sequence a list of validations
     * 将验证列表序列化
     *
     * @param validations list of validations - 验证列表
     * @param <E>         error type - 错误类型
     * @param <T>         value type - 值类型
     * @return Validation of list
     */
    static <E, T> Validation<E, List<T>> sequence(List<Validation<E, T>> validations) {
        List<E> errors = new ArrayList<>();
        List<T> values = new ArrayList<>();

        for (Validation<E, T> v : validations) {
            if (v.isValid()) {
                values.add(v.getValue().get());
            } else {
                errors.addAll(v.getErrors());
            }
        }

        return errors.isEmpty() ? valid(values) : invalid(errors);
    }

    // ==================== Valid Implementation | Valid 实现 ====================

    /**
     * Valid - Represents a successful validation
     * Valid - 表示成功的验证
     *
     * @param <E> error type - 错误类型
     * @param <T> value type - 值类型
     */
    final class Valid<E, T> implements Validation<E, T> {
        private final T value;

        public Valid(T value) {
            this.value = value;
        }

        /**
         * Get the value
         * 获取值
         *
         * @return the value - 值
         */
        public T value() {
            return value;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isInvalid() {
            return false;
        }

        @Override
        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }

        @Override
        public List<E> getErrors() {
            return List.of();
        }

        @Override
        public <U> Validation<E, U> map(Function<? super T, ? extends U> mapper) {
            return new Valid<>(mapper.apply(value));
        }

        @Override
        public <U> Validation<E, U> flatMap(Function<? super T, Validation<E, U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <U> Validation<E, U> ap(Validation<E, Function<? super T, ? extends U>> vf) {
            return vf.map(f -> f.apply(value));
        }

        @Override
        public <R> R fold(Function<? super List<E>, ? extends R> ifInvalid,
                          Function<? super T, ? extends R> ifValid) {
            return ifValid.apply(value);
        }

        @Override
        public Either<List<E>, T> toEither() {
            return Either.right(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Valid<?, ?> valid)) return false;
            return Objects.equals(value, valid.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Valid[" + value + "]";
        }
    }

    // ==================== Invalid Implementation | Invalid 实现 ====================

    /**
     * Invalid - Represents a failed validation with accumulated errors
     * Invalid - 表示失败的验证，包含累积的错误
     *
     * @param <E> error type - 错误类型
     * @param <T> value type - 值类型
     */
    final class Invalid<E, T> implements Validation<E, T> {
        private final List<E> errors;

        public Invalid(List<E> errors) {
            this.errors = List.copyOf(errors);
        }

        /**
         * Get the errors
         * 获取错误列表
         *
         * @return the errors - 错误列表
         */
        public List<E> errors() {
            return errors;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean isInvalid() {
            return true;
        }

        @Override
        public Optional<T> getValue() {
            return Optional.empty();
        }

        @Override
        public List<E> getErrors() {
            return errors;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Validation<E, U> map(Function<? super T, ? extends U> mapper) {
            return (Validation<E, U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Validation<E, U> flatMap(Function<? super T, Validation<E, U>> mapper) {
            return (Validation<E, U>) this;
        }

        @Override
        public <U> Validation<E, U> ap(Validation<E, Function<? super T, ? extends U>> vf) {
            List<E> allErrors = new ArrayList<>(errors);
            allErrors.addAll(vf.getErrors());
            return new Invalid<>(allErrors);
        }

        @Override
        public <R> R fold(Function<? super List<E>, ? extends R> ifInvalid,
                          Function<? super T, ? extends R> ifValid) {
            return ifInvalid.apply(errors);
        }

        @Override
        public Either<List<E>, T> toEither() {
            return Either.left(errors);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Invalid<?, ?> invalid)) return false;
            return Objects.equals(errors, invalid.errors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errors);
        }

        @Override
        public String toString() {
            return "Invalid[" + errors + "]";
        }
    }
}
