package cloud.opencode.base.functional.monad;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Either Monad - Represents one of two possible values (Left or Right)
 * Either Monad - 表示两种可能值之一（Left 或 Right）
 *
 * <p>A sealed type that contains either a Left value (typically for errors)
 * or a Right value (typically for success). By convention, Right is the "happy path".</p>
 * <p>一个密封类型，包含 Left 值（通常表示错误）或 Right 值（通常表示成功）。
 * 按照惯例，Right 是"正常路径"。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe error handling - 类型安全的错误处理</li>
 *   <li>Right-biased operations - Right 倾向的操作</li>
 *   <li>Monadic operations (map, flatMap) - Monad 操作</li>
 *   <li>Folding/pattern matching - 折叠/模式匹配</li>
 *   <li>Swap operation - 交换操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Creating Either values
 * Either<String, User> success = Either.right(user);
 * Either<String, User> error = Either.left("User not found");
 *
 * // Returning from methods
 * public Either<String, User> findUser(Long id) {
 *     User user = userRepository.findById(id);
 *     return user == null
 *         ? Either.left("User not found: " + id)
 *         : Either.right(user);
 * }
 *
 * // Using the result
 * findUser(1L)
 *     .map(User::getName)
 *     .fold(
 *         error -> log.error(error),
 *         name -> log.info("Found: " + name)
 *     );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param <L> left type (typically error) - 左类型（通常为错误）
 * @param <R> right type (typically success) - 右类型（通常为成功）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public sealed interface Either<L, R> permits Either.Left, Either.Right {

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a Left Either
     * 创建 Left Either
     *
     * @param value left value - 左值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     * @return Left Either
     */
    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    /**
     * Create a Right Either
     * 创建 Right Either
     *
     * @param value right value - 右值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     * @return Right Either
     */
    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    // ==================== State Queries | 状态查询 ====================

    /**
     * Check if this is a Left
     * 检查是否为 Left
     *
     * @return true if Left - 如果是 Left 返回 true
     */
    boolean isLeft();

    /**
     * Check if this is a Right
     * 检查是否为 Right
     *
     * @return true if Right - 如果是 Right 返回 true
     */
    boolean isRight();

    // ==================== Value Access | 值访问 ====================

    /**
     * Get the Left value if present
     * 获取 Left 值（如果存在）
     *
     * @return Optional containing Left value
     */
    Optional<L> getLeft();

    /**
     * Get the Right value if present
     * 获取 Right 值（如果存在）
     *
     * @return Optional containing Right value
     */
    Optional<R> getRight();

    // ==================== Transformations | 转换 ====================

    /**
     * Transform the Right value
     * 转换 Right 值
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Either
     */
    <U> Either<L, U> map(Function<? super R, ? extends U> mapper);

    /**
     * Transform the Right value to another Either
     * 将 Right 值转换为另一个 Either
     *
     * @param mapper transformation function returning Either - 返回 Either 的转换函数
     * @param <U>    result type - 结果类型
     * @return resulting Either
     */
    <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper);

    /**
     * Transform the Left value
     * 转换 Left 值
     *
     * @param mapper transformation function - 转换函数
     * @param <U>    result type - 结果类型
     * @return transformed Either
     */
    <U> Either<U, R> mapLeft(Function<? super L, ? extends U> mapper);

    /**
     * Transform both sides
     * 转换两侧的值
     *
     * @param leftMapper  transformation for Left - Left 的转换函数
     * @param rightMapper transformation for Right - Right 的转换函数
     * @param <L2>        new left type - 新的左类型
     * @param <R2>        new right type - 新的右类型
     * @return transformed Either
     */
    <L2, R2> Either<L2, R2> bimap(
            Function<? super L, ? extends L2> leftMapper,
            Function<? super R, ? extends R2> rightMapper);

    // ==================== Recovery | 恢复 ====================

    /**
     * Get Right value or default if Left
     * 获取 Right 值或默认值（如果是 Left）
     *
     * @param defaultValue default value - 默认值
     * @return Right value or default
     */
    R getOrElse(R defaultValue);

    /**
     * Return this or other Either if Left
     * 返回本 Either 或其他 Either（如果是 Left）
     *
     * @param other alternative Either - 备选 Either
     * @return this if Right, other if Left
     */
    Either<L, R> orElse(Either<L, R> other);

    // ==================== Folding | 折叠 ====================

    /**
     * Fold both cases to a single result
     * 将两种情况折叠为单一结果
     *
     * @param leftMapper  function for Left - Left 的函数
     * @param rightMapper function for Right - Right 的函数
     * @param <T>         result type - 结果类型
     * @return folded result
     */
    <T> T fold(Function<? super L, ? extends T> leftMapper,
               Function<? super R, ? extends T> rightMapper);

    /**
     * Swap Left and Right
     * 交换 Left 和 Right
     *
     * @return swapped Either
     */
    Either<R, L> swap();

    // ==================== Side Effects | 副作用 ====================

    /**
     * Execute action on Right value
     * 对 Right 值执行操作
     *
     * @param action action to execute - 要执行的操作
     * @return this Either for chaining
     */
    Either<L, R> peek(Consumer<? super R> action);

    /**
     * Execute action on Left value
     * 对 Left 值执行操作
     *
     * @param action action to execute - 要执行的操作
     * @return this Either for chaining
     */
    Either<L, R> peekLeft(Consumer<? super L> action);

    // ==================== Filtering | 过滤 ====================

    /**
     * Filter the Right value with a predicate, converting to Left if it fails.
     * 用谓词过滤 Right 值，如果不满足则转换为 Left。
     *
     * <p>If this is a Right and the predicate matches, returns this.
     * If this is a Right but the predicate fails, returns Left with the orElse value.
     * If this is a Left, returns this unchanged.</p>
     * <p>如果是 Right 且谓词匹配，返回 this。
     * 如果是 Right 但谓词不匹配，返回包含 orElse 值的 Left。
     * 如果是 Left，原样返回。</p>
     *
     * @param predicate the predicate to test the Right value | 用于测试 Right 值的谓词
     * @param orElse    supplier for the Left value if predicate fails | 谓词不满足时的 Left 值提供者
     * @return filtered Either | 过滤后的 Either
     */
    default Either<L, R> filterOrElse(Predicate<? super R> predicate, Supplier<? extends L> orElse) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        Objects.requireNonNull(orElse, "orElse must not be null");
        if (isRight() && !predicate.test(getRight().orElse(null))) {
            return Either.left(orElse.get());
        }
        return this;
    }

    // ==================== Conversions | 类型转换 ====================

    /**
     * Convert to Option: Right becomes Some, Left becomes None.
     * 转换为 Option：Right 变为 Some，Left 变为 None。
     *
     * @return Option containing the Right value, or empty | 包含 Right 值的 Option，或为空
     */
    default Option<R> toOption() {
        if (isRight()) {
            return Option.of(getRight().orElse(null));
        }
        return Option.none();
    }

    /**
     * Convert to Try: Right becomes Success, Left becomes Failure.
     * 转换为 Try：Right 变为 Success，Left 变为 Failure。
     *
     * <p>For Left: if the value is a Throwable, it is used directly;
     * otherwise an OpenFunctionalException wrapping it is created.</p>
     * <p>对于 Left：如果值是 Throwable，则直接使用；
     * 否则创建包装它的 OpenFunctionalException。</p>
     *
     * @return Try containing the Right value or the Left error | 包含 Right 值或 Left 错误的 Try
     */
    default Try<R> toTry() {
        if (isRight()) {
            return Try.success(getRight().orElse(null));
        }
        L leftValue = getLeft().orElse(null);
        if (leftValue instanceof Throwable t) {
            return Try.failure(t);
        }
        return Try.failure(new OpenFunctionalException("Either.Left: " + leftValue));
    }

    /**
     * Convert to Validation: Right becomes Valid, Left becomes Invalid.
     * 转换为 Validation：Right 变为 Valid，Left 变为 Invalid。
     *
     * @return Validation containing the Right value or the Left error | 包含 Right 值或 Left 错误的 Validation
     */
    default Validation<L, R> toValidation() {
        if (isRight()) {
            return Validation.valid(getRight().orElse(null));
        }
        return Validation.invalid(getLeft().orElse(null));
    }

    /**
     * Convert to Stream: Right becomes a single-element Stream, Left becomes empty.
     * 转换为 Stream：Right 变为单元素 Stream，Left 变为空 Stream。
     *
     * @return Stream containing the Right value, or empty | 包含 Right 值的 Stream，或为空
     */
    default Stream<R> stream() {
        if (isRight()) {
            return getRight().stream();
        }
        return Stream.empty();
    }

    // ==================== Testing | 测试方法 ====================

    /**
     * Check if this is a Right containing the given value.
     * 检查是否为包含给定值的 Right。
     *
     * @param value the value to compare | 要比较的值
     * @return true if Right and value equals contained value | 如果是 Right 且值相等返回 true
     */
    default boolean contains(R value) {
        return isRight() && Objects.equals(getRight().orElse(null), value);
    }

    /**
     * Check if this is a Right and the predicate matches the value.
     * 检查是否为 Right 且谓词匹配该值。
     *
     * @param predicate the predicate to test | 要测试的谓词
     * @return true if Right and predicate matches | 如果是 Right 且谓词匹配返回 true
     */
    default boolean exists(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return isRight() && predicate.test(getRight().orElse(null));
    }

    /**
     * Check if this is a Left (vacuously true) or the predicate matches the Right value.
     * 检查是否为 Left（空真）或谓词匹配 Right 值。
     *
     * @param predicate the predicate to test | 要测试的谓词
     * @return true if Left or predicate matches Right value | 如果是 Left 或谓词匹配 Right 值返回 true
     */
    default boolean forAll(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return isLeft() || predicate.test(getRight().orElse(null));
    }

    // ==================== Left Implementation | Left 实现 ====================

    /**
     * Left - Represents the left case (typically error)
     * Left - 表示左情况（通常为错误）
     *
     * @param <L> left type - 左类型
     * @param <R> right type - 右类型
     */
    final class Left<L, R> implements Either<L, R> {
        private final L value;

        public Left(L value) {
            this.value = value;
        }

        /**
         * Get the left value
         * 获取左值
         *
         * @return the left value - 左值
         */
        public L value() {
            return value;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public Optional<L> getLeft() {
            return Optional.ofNullable(value);
        }

        @Override
        public Optional<R> getRight() {
            return Optional.empty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
            return (Either<L, U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper) {
            return (Either<L, U>) this;
        }

        @Override
        public <U> Either<U, R> mapLeft(Function<? super L, ? extends U> mapper) {
            return new Left<>(mapper.apply(value));
        }

        @Override
        public <L2, R2> Either<L2, R2> bimap(
                Function<? super L, ? extends L2> leftMapper,
                Function<? super R, ? extends R2> rightMapper) {
            return new Left<>(leftMapper.apply(value));
        }

        @Override
        public R getOrElse(R defaultValue) {
            return defaultValue;
        }

        @Override
        public Either<L, R> orElse(Either<L, R> other) {
            return other;
        }

        @Override
        public <T> T fold(Function<? super L, ? extends T> leftMapper,
                          Function<? super R, ? extends T> rightMapper) {
            return leftMapper.apply(value);
        }

        @Override
        public Either<R, L> swap() {
            return new Right<>(value);
        }

        @Override
        public Either<L, R> peek(Consumer<? super R> action) {
            return this;
        }

        @Override
        public Either<L, R> peekLeft(Consumer<? super L> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Try<R> toTry() {
            if (value instanceof Throwable t) {
                return Try.failure(t);
            }
            return Try.failure(new OpenFunctionalException("Either.Left: " + value));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Left<?, ?> left)) return false;
            return Objects.equals(value, left.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Left[" + value + "]";
        }
    }

    // ==================== Right Implementation | Right 实现 ====================

    /**
     * Right - Represents the right case (typically success)
     * Right - 表示右情况（通常为成功）
     *
     * @param <L> left type - 左类型
     * @param <R> right type - 右类型
     */
    final class Right<L, R> implements Either<L, R> {
        private final R value;

        public Right(R value) {
            this.value = value;
        }

        /**
         * Get the right value
         * 获取右值
         *
         * @return the right value - 右值
         */
        public R value() {
            return value;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public Optional<L> getLeft() {
            return Optional.empty();
        }

        @Override
        public Optional<R> getRight() {
            return Optional.ofNullable(value);
        }

        @Override
        public <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
            return new Right<>(mapper.apply(value));
        }

        @Override
        public <U> Either<L, U> flatMap(Function<? super R, Either<L, U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Either<U, R> mapLeft(Function<? super L, ? extends U> mapper) {
            return (Either<U, R>) this;
        }

        @Override
        public <L2, R2> Either<L2, R2> bimap(
                Function<? super L, ? extends L2> leftMapper,
                Function<? super R, ? extends R2> rightMapper) {
            return new Right<>(rightMapper.apply(value));
        }

        @Override
        public R getOrElse(R defaultValue) {
            return value;
        }

        @Override
        public Either<L, R> orElse(Either<L, R> other) {
            return this;
        }

        @Override
        public <T> T fold(Function<? super L, ? extends T> leftMapper,
                          Function<? super R, ? extends T> rightMapper) {
            return rightMapper.apply(value);
        }

        @Override
        public Either<R, L> swap() {
            return new Left<>(value);
        }

        @Override
        public Either<L, R> peek(Consumer<? super R> action) {
            action.accept(value);
            return this;
        }

        @Override
        public Either<L, R> peekLeft(Consumer<? super L> action) {
            return this;
        }

        @Override
        public Try<R> toTry() {
            return Try.success(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Right<?, ?> right)) return false;
            return Objects.equals(value, right.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Right[" + value + "]";
        }
    }
}
