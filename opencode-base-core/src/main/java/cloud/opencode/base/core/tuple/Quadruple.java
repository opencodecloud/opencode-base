package cloud.opencode.base.core.tuple;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Quadruple - Immutable four-element tuple (Record implementation)
 * 四元组 - 不可变的四元素元组（Record 实现）
 *
 * <p>Immutable container for four related values.</p>
 * <p>不可变的四值容器，可用于返回四个相关值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable storage for four values - 不可变的四值存储</li>
 *   <li>Element mapping and transformation - 元素映射和转换</li>
 *   <li>Extract to Pair or Triple - 提取为 Pair 或 Triple</li>
 *   <li>Null checks (hasNull, allNonNull) - 空值检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Quadruple<String, Integer, Boolean, Double> quad =
 *     Quadruple.of("name", 25, true, 3.14);
 * String first = quad.first();
 * Triple<String, Integer, Boolean> triple = quad.toFirstTriple();
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
 * @param fourth fourth element - 第四个元素
 * @param <A>    first element type - 第一个元素类型
 * @param <B>    second element type - 第二个元素类型
 * @param <C>    third element type - 第三个元素类型
 * @param <D>    fourth element type - 第四个元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public record Quadruple<A, B, C, D>(A first, B second, C third, D fourth) implements Serializable {

    /**
     * Creates a Quadruple
     * 创建四元组
     *
     * @param first  the first element | 第一个元素
     * @param second the second element | 第二个元素
     * @param third  the third element | 第三个元素
     * @param fourth the fourth element | 第四个元素
     * @param <A>    first element type | 第一个元素类型
     * @param <B>    second element type | 第二个元素类型
     * @param <C>    third element type | 第三个元素类型
     * @param <D>    fourth element type | 第四个元素类型
     * @return the Quadruple | 四元组
     */
    public static <A, B, C, D> Quadruple<A, B, C, D> of(A first, B second, C third, D fourth) {
        return new Quadruple<>(first, second, third, fourth);
    }

    /**
     * Creates an empty Quadruple
     * 创建空四元组
     *
     * @param <A> first element type | 第一个元素类型
     * @param <B> second element type | 第二个元素类型
     * @param <C> third element type | 第三个元素类型
     * @param <D> fourth element type | 第四个元素类型
     * @return an empty Quadruple | 空四元组
     */
    public static <A, B, C, D> Quadruple<A, B, C, D> empty() {
        return new Quadruple<>(null, null, null, null);
    }

    /**
     * Maps the first element
     * 映射第一个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Quadruple | 新四元组
     */
    public <T> Quadruple<T, B, C, D> mapFirst(Function<? super A, ? extends T> mapper) {
        return new Quadruple<>(mapper.apply(first), second, third, fourth);
    }

    /**
     * Maps the second element
     * 映射第二个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Quadruple | 新四元组
     */
    public <T> Quadruple<A, T, C, D> mapSecond(Function<? super B, ? extends T> mapper) {
        return new Quadruple<>(first, mapper.apply(second), third, fourth);
    }

    /**
     * Maps the third element
     * 映射第三个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Quadruple | 新四元组
     */
    public <T> Quadruple<A, B, T, D> mapThird(Function<? super C, ? extends T> mapper) {
        return new Quadruple<>(first, second, mapper.apply(third), fourth);
    }

    /**
     * Maps the fourth element
     * 映射第四个元素
     *
     * @param mapper the mapper function | 映射函数
     * @param <T>    new type | 新类型
     * @return a new Quadruple | 新四元组
     */
    public <T> Quadruple<A, B, C, T> mapFourth(Function<? super D, ? extends T> mapper) {
        return new Quadruple<>(first, second, third, mapper.apply(fourth));
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
    public Pair<C, D> toLastPair() {
        return Pair.of(third, fourth);
    }

    /**
     * Extracts the first three elements as a Triple
     * 提取前三个元素为 Triple
     *
     * @return Triple
     */
    public Triple<A, B, C> toFirstTriple() {
        return Triple.of(first, second, third);
    }

    /**
     * Extracts the last three elements as a Triple
     * 提取后三个元素为 Triple
     *
     * @return Triple
     */
    public Triple<B, C, D> toLastTriple() {
        return Triple.of(second, third, fourth);
    }

    /**
     * Checks if it contains a null value
     * 检查是否包含 null 值
     *
     * @return true if any value is null | 如果任一值为 null 返回 true
     */
    public boolean hasNull() {
        return first == null || second == null || third == null || fourth == null;
    }

    /**
     * Checks if all values are non-null
     * 检查是否都非 null
     *
     * @return true if all values are non-null | 如果都非 null 返回 true
     */
    public boolean allNonNull() {
        return first != null && second != null && third != null && fourth != null;
    }

    /**
     * Converts to an array
     * 转换为数组
     *
     * @return an array containing four elements | 包含四个元素的数组
     */
    public Object[] toArray() {
        return new Object[]{first, second, third, fourth};
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ")";
    }
}
