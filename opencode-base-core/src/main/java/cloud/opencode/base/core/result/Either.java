package cloud.opencode.base.core.result;

import cloud.opencode.base.core.exception.OpenException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
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
 *   <li>Conversion to Result, Optional, Stream - 转换为 Result、Optional、Stream</li>
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
 *
 * // Pattern matching with switch (JDK 25)
 * switch (either) {
 *     case Either.Left(var err)  -> handleError(err);
 *     case Either.Right(var val) -> handleSuccess(val);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable records) - 线程安全: 是 (不可变记录)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param <L> left type (typically error) - 左类型（通常为错误）
 * @param <R> right type (typically success) - 右类型（通常为成功）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Result
 * @since JDK 25, opencode-base-core V1.0.3
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

    // ==================== Conversion | 转换 ====================

    /**
     * Convert to Optional. Right value becomes Optional.of(value), Left becomes Optional.empty().
     * 转换为 Optional。Right 值变为 Optional.of(value)，Left 变为 Optional.empty()。
     *
     * @return Optional containing the Right value, or empty if Left
     */
    Optional<R> toOptional();

    /**
     * Convert to Stream. Right value becomes a single-element Stream, Left becomes an empty Stream.
     * 转换为 Stream。Right 值变为单元素 Stream，Left 变为空 Stream。
     *
     * @return Stream containing the Right value, or empty if Left
     */
    Stream<R> stream();

    /**
     * Convert to Result. Right becomes Result.success, Left(Throwable) becomes Result.failure,
     * Left(other) becomes Result.failure(new OpenException(L.toString())).
     * 转换为 Result。Right 变为 Result.success，Left(Throwable) 变为 Result.failure，
     * Left(其他) 变为 Result.failure(new OpenException(L.toString()))。
     *
     * @return Result representation of this Either
     */
    Result<R> toResult();

    /**
     * Convert to Result with a type-safe left-to-throwable conversion function.
     * 使用类型安全的 left-to-throwable 转换函数转换为 Result。
     *
     * @param leftToThrowable function to convert Left value to a Throwable - 将 Left 值转换为 Throwable 的函数
     * @param <X>             the throwable type - 异常类型
     * @return Result representation of this Either
     */
    <X extends Throwable> Result<R> toResult(Function<L, X> leftToThrowable);

    // ==================== Left Implementation | Left 实现 ====================

    /**
     * Left - Represents the left case (typically error)
     * Left - 表示左情况（通常为错误）
     *
     * @param value left value - 左值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     */
    record Left<L, R>(L value) implements Either<L, R> {

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
        public Optional<R> toOptional() {
            return Optional.empty();
        }

        @Override
        public Stream<R> stream() {
            return Stream.empty();
        }

        @Override
        public Result<R> toResult() {
            if (value instanceof Throwable t) {
                return Result.failure(t);
            }
            return Result.failure(new OpenException(String.valueOf(value)));
        }

        @Override
        public <X extends Throwable> Result<R> toResult(Function<L, X> leftToThrowable) {
            return Result.failure(leftToThrowable.apply(value));
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
     * @param value right value - 右值
     * @param <L>   left type - 左类型
     * @param <R>   right type - 右类型
     */
    record Right<L, R>(R value) implements Either<L, R> {

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
        public Optional<R> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Stream<R> stream() {
            return value == null ? Stream.empty() : Stream.of(value);
        }

        @Override
        public Result<R> toResult() {
            return Result.success(value);
        }

        @Override
        public <X extends Throwable> Result<R> toResult(Function<L, X> leftToThrowable) {
            return Result.success(value);
        }

        @Override
        public String toString() {
            return "Right[" + value + "]";
        }
    }
}
