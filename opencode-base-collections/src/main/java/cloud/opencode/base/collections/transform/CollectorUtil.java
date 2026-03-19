package cloud.opencode.base.collections.transform;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * CollectorUtil - Custom Collector Utilities
 * CollectorUtil - 自定义收集器工具
 *
 * <p>Provides custom collectors for stream operations.</p>
 * <p>提供用于流操作的自定义收集器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable collection collectors - 不可变集合收集器</li>
 *   <li>Map collectors - 映射收集器</li>
 *   <li>Partitioning collectors - 分区收集器</li>
 *   <li>Special purpose collectors - 特殊用途收集器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Collect to unmodifiable list - 收集为不可修改列表
 * List<String> list = stream.collect(CollectorUtil.toUnmodifiableList());
 *
 * // Collect to LinkedHashSet - 收集为 LinkedHashSet
 * Set<String> set = stream.collect(CollectorUtil.toLinkedHashSet());
 *
 * // Collect to TreeMap - 收集为 TreeMap
 * Map<K, V> map = stream.collect(CollectorUtil.toTreeMap(keyFn, valueFn));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (input must not be null) - 否（输入不能为null）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) accumulation where n is the number of stream elements - 时间复杂度: O(n) 累积，n为流元素数量</li>
 *   <li>Space complexity: O(n) for collected results - 空间复杂度: O(n)，存储收集结果</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class CollectorUtil {

    private CollectorUtil() {
    }

    // ==================== List 收集器 | List Collectors ====================

    /**
     * Collector to ArrayList.
     * 收集到 ArrayList。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collector.of(
                ArrayList::new,
                ArrayList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to ArrayList with initial capacity.
     * 收集到带初始容量的 ArrayList。
     *
     * @param <T>      element type | 元素类型
     * @param capacity initial capacity | 初始容量
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ArrayList<T>> toArrayList(int capacity) {
        return Collector.of(
                () -> new ArrayList<>(capacity),
                ArrayList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to LinkedList.
     * 收集到 LinkedList。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, LinkedList<T>> toLinkedList() {
        return Collector.of(
                LinkedList::new,
                LinkedList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to unmodifiable list.
     * 收集到不可修改列表。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collector.of(
                ArrayList<T>::new,
                ArrayList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> Collections.unmodifiableList(list)
        );
    }

    // ==================== Set 收集器 | Set Collectors ====================

    /**
     * Collector to HashSet.
     * 收集到 HashSet。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, HashSet<T>> toHashSet() {
        return Collector.of(
                HashSet::new,
                HashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to LinkedHashSet.
     * 收集到 LinkedHashSet。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, LinkedHashSet<T>> toLinkedHashSet() {
        return Collector.of(
                LinkedHashSet::new,
                LinkedHashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to TreeSet with natural ordering.
     * 收集到自然排序的 TreeSet。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T extends Comparable<? super T>> Collector<T, ?, TreeSet<T>> toTreeSet() {
        return Collector.of(
                TreeSet::new,
                TreeSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to TreeSet with custom comparator.
     * 收集到自定义比较器的 TreeSet。
     *
     * @param <T>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, TreeSet<T>> toTreeSet(Comparator<? super T> comparator) {
        return Collector.of(
                () -> new TreeSet<>(comparator),
                TreeSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to unmodifiable set.
     * 收集到不可修改集合。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collector.of(
                HashSet<T>::new,
                HashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                set -> Collections.unmodifiableSet(set)
        );
    }

    // ==================== Map 收集器 | Map Collectors ====================

    /**
     * Collector to LinkedHashMap.
     * 收集到 LinkedHashMap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toLinkedHashMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                LinkedHashMap::new,
                (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to TreeMap with natural ordering.
     * 收集到自然排序的 TreeMap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K extends Comparable<? super K>, V> Collector<T, ?, TreeMap<K, V>> toTreeMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                TreeMap::new,
                (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to TreeMap with custom comparator.
     * 收集到自定义比较器的 TreeMap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @param comparator    the comparator | 比较器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, TreeMap<K, V>> toTreeMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            Comparator<? super K> comparator) {
        return Collector.of(
                () -> new TreeMap<>(comparator),
                (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to unmodifiable map.
     * 收集到不可修改映射。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                HashMap<K, V>::new,
                (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                },
                map -> Collections.unmodifiableMap(map)
        );
    }

    // ==================== 特殊收集器 | Special Collectors ====================

    /**
     * Collector to count elements.
     * 收集以计数元素。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Long> counting() {
        return Collector.of(
                () -> new long[1],
                (a, t) -> a[0]++,
                (a, b) -> {
                    a[0] += b[0];
                    return a;
                },
                a -> a[0]
        );
    }

    /**
     * Collector to join strings.
     * 收集以连接字符串。
     *
     * @param delimiter the delimiter | 分隔符
     * @return collector | 收集器
     */
    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
        return joining(delimiter, "", "");
    }

    /**
     * Collector to join strings with prefix and suffix.
     * 收集以连接带前缀和后缀的字符串。
     *
     * @param delimiter the delimiter | 分隔符
     * @param prefix    the prefix | 前缀
     * @param suffix    the suffix | 后缀
     * @return collector | 收集器
     */
    public static Collector<CharSequence, ?, String> joining(
            CharSequence delimiter,
            CharSequence prefix,
            CharSequence suffix) {
        return Collector.of(
                () -> new StringJoiner(delimiter, prefix, suffix),
                StringJoiner::add,
                StringJoiner::merge,
                StringJoiner::toString
        );
    }

    /**
     * Create a custom collector.
     * 创建自定义收集器。
     *
     * @param <T>         element type | 元素类型
     * @param <A>         accumulator type | 累加器类型
     * @param <R>         result type | 结果类型
     * @param supplier    the supplier | 供应器
     * @param accumulator the accumulator | 累加器
     * @param combiner    the combiner | 组合器
     * @param finisher    the finisher | 完成器
     * @return collector | 收集器
     */
    public static <T, A, R> Collector<T, A, R> of(
            Supplier<A> supplier,
            BiConsumer<A, T> accumulator,
            BinaryOperator<A> combiner,
            Function<A, R> finisher) {
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    /**
     * Create a custom collector without finisher.
     * 创建不带完成器的自定义收集器。
     *
     * @param <T>         element type | 元素类型
     * @param <R>         result type | 结果类型
     * @param supplier    the supplier | 供应器
     * @param accumulator the accumulator | 累加器
     * @param combiner    the combiner | 组合器
     * @return collector | 收集器
     */
    public static <T, R> Collector<T, R, R> of(
            Supplier<R> supplier,
            BiConsumer<R, T> accumulator,
            BinaryOperator<R> combiner) {
        return Collector.of(supplier, accumulator, combiner);
    }
}
