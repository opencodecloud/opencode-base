package cloud.opencode.base.core.tuple;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Triple - Immutable three-element tuple (Record implementation)
 * 三元组 - 不可变的三元素元组（Record 实现）
 *
 * <p>Immutable container for three related values.</p>
 * <p>不可变的三值容器，可用于返回三个相关值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable storage for three values - 不可变的三值存储</li>
 *   <li>Multiple aliases (first/second/third, left/middle/right) - 多种别名访问</li>
 *   <li>Element mapping and transformation - 元素映射和转换</li>
 *   <li>Extract to Pair (first two or last two) - 提取为 Pair</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Triple<String, Integer, Boolean> triple = Triple.of("name", 25, true);
 * String first = triple.first();
 * Triple<String, String, Boolean> mapped = triple.mapSecond(String::valueOf);
 * Pair<String, Integer> pair = triple.toFirstPair();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是 (不可变 Record)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 * @param first  first element - 第一个元素
 * @param second second element - 第二个元素
 * @param third  third element - 第三个元素
 * @param <A>    first element type - 第一个元素类型
 * @param <B>    second element type - 第二个元素类型
 * @param <C>    third element type - 第三个元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public record Triple<A, B, C>(A first, B second, C third) implements Serializable {

    /**
     * Creates a Triple
     * 创建三元组
     *
     * @param first  the first element | 第一个元素
     * @param second the second element | 第二个元素
     * @param third  the third element | 第三个元素
     * @param <A>    first element type | 第一个元素类型
     * @param <B>    second element type | 第二个元素类型
     * @param <C>    third element type | 第三个元素类型
     * @return the Triple | 三元组
     */
    public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
        return new Triple<>(first, second, third);
    }

    /**
     * Creates an empty Triple
     * 创建空三元组
     *
     * @param <A> first element type | 第一个元素类型
     * @param <B> second element type | 第二个元素类型
     * @param <C> third element type | 第三个元素类型
     * @return an empty Triple | 空三元组
     */
    public static <A, B, C> Triple<A, B, C> empty() {
        return new Triple<>(null, null, null);
    }

    /**
     * Gets the left value (alias for first)
     * 获取左值（第一个元素的别名）
     *
     * @return the first element | 第一个元素
     */
    public A left() {
        return first;
    }

    /**
     * Gets the middle value (alias for second)
     * 获取中值（第二个元素的别名）
     *
     * @return the second element | 第二个元素
     */
    public B middle() {
        return second;
    }

    /**
     * Gets the right value (alias for third)
     * 获取右值（第三个元素的别名）
     *
     * @return the third element | 第三个元素
     */
    public C right() {
        return third;
    }

    /**
     * Maps the first element
     * 映射第一个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Triple | 新三元组
     */
    public <T> Triple<T, B, C> mapFirst(Function<? super A, ? extends T> mapper) {
        return new Triple<>(mapper.apply(first), second, third);
    }

    /**
     * Maps the second element
     * 映射第二个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Triple | 新三元组
     */
    public <T> Triple<A, T, C> mapSecond(Function<? super B, ? extends T> mapper) {
        return new Triple<>(first, mapper.apply(second), third);
    }

    /**
     * Maps the third element
     * 映射第三个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Triple | 新三元组
     */
    public <T> Triple<A, B, T> mapThird(Function<? super C, ? extends T> mapper) {
        return new Triple<>(first, second, mapper.apply(third));
    }

    /**
     * Maps all elements simultaneously
     * 同时映射所有元素
     *
     * @param firstMapper  first element mapper function | 第一个元素映射函数
     * @param secondMapper second element mapper function | 第二个元素映射函数
     * @param thirdMapper  third element mapper function | 第三个元素映射函数
     * @param <T>          new first element type | 新第一个元素类型
     * @param <U>          new second element type | 新第二个元素类型
     * @param <V>          new third element type | 新第三个元素类型
     * @return a new Triple | 新三元组
     */
    public <T, U, V> Triple<T, U, V> map(Function<? super A, ? extends T> firstMapper,
                                          Function<? super B, ? extends U> secondMapper,
                                          Function<? super C, ? extends V> thirdMapper) {
        return new Triple<>(
                firstMapper.apply(first),
                secondMapper.apply(second),
                thirdMapper.apply(third)
        );
    }

    /**
     * Applies a tri-function
     * 应用三元函数
     *
     * @param function the tri-function | 三元函数
     * @param <R>      result type | 结果类型
     * @return the function result | 函数结果
     */
    public <R> R apply(TriFunction<? super A, ? super B, ? super C, ? extends R> function) {
        return function.apply(first, second, third);
    }

    /**
     * Extracts the first two elements as a Pair
     * 提取前两个元素为 Pair
     *
     * @return Pair
     */
    public Pair<A, B> toFirstPair() {
        return Pair.of(first, second);
    }

    /**
     * Extracts the last two elements as a Pair
     * 提取后两个元素为 Pair
     *
     * @return Pair
     */
    public Pair<B, C> toLastPair() {
        return Pair.of(second, third);
    }

    /**
     * Checks if it contains a null value
     * 检查是否包含 null 值
     *
     * @return true if any value is null | 如果任一值为 null 返回 true
     */
    public boolean hasNull() {
        return first == null || second == null || third == null;
    }

    /**
     * Checks if all values are non-null
     * 检查是否都非 null
     *
     * @return true if all values are non-null | 如果都非 null 返回 true
     */
    public boolean allNonNull() {
        return first != null && second != null && third != null;
    }

    /**
     * Converts to an array
     * 转换为数组
     *
     * @return an array containing three elements | 包含三个元素的数组
     */
    public Object[] toArray() {
        return new Object[]{first, second, third};
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    /**
     * Three-argument function interface
     * 三元函数接口
     *
     * @param <A> first argument type | 第一个参数类型
     * @param <B> second argument type | 第二个参数类型
     * @param <C> third argument type | 第三个参数类型
     * @param <R> return type | 返回值类型
     */
    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
