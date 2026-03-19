package cloud.opencode.base.core.tuple;

import java.util.Map;

/**
 * Tuple Factory Utility - Convenience methods for creating tuples
 * 元组工厂工具类 - 提供创建元组的便捷方法
 *
 * <p>Provides factory methods for creating Pair, Triple, and Quadruple tuples.</p>
 * <p>提供创建 Pair、Triple 和 Quadruple 元组的工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create Pair with pair() method - 使用 pair() 创建二元组</li>
 *   <li>Create Triple with triple() method - 使用 triple() 创建三元组</li>
 *   <li>Create Quadruple with quadruple() method - 使用 quadruple() 创建四元组</li>
 *   <li>Create empty tuples - 创建空元组</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Pair<String, Integer> pair = TupleUtil.pair("name", 25);
 * Triple<String, Integer, Boolean> triple = TupleUtil.triple("name", 25, true);
 * Quadruple<String, Integer, Boolean, Double> quad =
 *     TupleUtil.quadruple("name", 25, true, 3.14);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless factory) - 线程安全: 是 (无状态工厂)</li>
 *   <li>Null-safe: Allows null values - 空值安全: 允许 null 值</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per tuple creation - 每次元组创建 O(1)</li>
 *   <li>Space complexity: O(1) per tuple - 每个元组 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class TupleUtil {

    private TupleUtil() {
        // 工具类不可实例化
    }

    // ==================== Pair 创建 ====================

    /**
     * Creates a Pair
     * 创建二元组
     *
     * @param left  left value | 左值
     * @param right right value | 右值
     * @param <L>   left value type | 左值类型
     * @param <R>   right value type | 右值类型
     * @return the Pair | 二元组
     */
    public static <L, R> Pair<L, R> pair(L left, R right) {
        return Pair.of(left, right);
    }

    /**
     * Creates a Pair from Map.Entry
     * 从 Map.Entry 创建二元组
     *
     * @param entry Map.Entry
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return the Pair | 二元组
     */
    public static <K, V> Pair<K, V> pair(Map.Entry<K, V> entry) {
        return Pair.fromEntry(entry);
    }

    /**
     * Creates an empty Pair
     * 创建空二元组
     *
     * @param <L> left value type | 左值类型
     * @param <R> right value type | 右值类型
     * @return an empty Pair | 空二元组
     */
    public static <L, R> Pair<L, R> emptyPair() {
        return Pair.empty();
    }

    // ==================== Triple 创建 ====================

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
    public static <A, B, C> Triple<A, B, C> triple(A first, B second, C third) {
        return Triple.of(first, second, third);
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
    public static <A, B, C> Triple<A, B, C> emptyTriple() {
        return Triple.empty();
    }

    // ==================== Quadruple 创建 ====================

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
    public static <A, B, C, D> Quadruple<A, B, C, D> quadruple(A first, B second, C third, D fourth) {
        return Quadruple.of(first, second, third, fourth);
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
    public static <A, B, C, D> Quadruple<A, B, C, D> emptyQuadruple() {
        return Quadruple.empty();
    }
}
