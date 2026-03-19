package cloud.opencode.base.collections.transform;

import cloud.opencode.base.collections.*;
import cloud.opencode.base.collections.immutable.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MoreCollectorUtil - Additional Collector Utilities
 * MoreCollectorUtil - 附加收集器工具
 *
 * <p>Provides additional advanced collectors for stream operations.</p>
 * <p>提供用于流操作的附加高级收集器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>First/last element collectors - 首/末元素收集器</li>
 *   <li>Min/max by key collectors - 按键最小/最大收集器</li>
 *   <li>Multimap collectors - 多值映射收集器</li>
 *   <li>Conditional collectors - 条件收集器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get first element - 获取第一个元素
 * Optional<String> first = stream.collect(MoreCollectorUtil.first());
 *
 * // Get max by key - 按键获取最大值
 * Optional<Person> oldest = people.stream()
 *     .collect(MoreCollectorUtil.maxBy(Person::getAge));
 *
 * // Collect to multimap - 收集为多值映射
 * Map<String, List<Person>> byCity = stream
 *     .collect(MoreCollectorUtil.toMultimap(Person::getCity));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (input must not be null) - 否（输入不能为null）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n log k) for minK/maxK where n is elements and k is result size; O(n) for most others - 时间复杂度: minK/maxK 为 O(n log k)，其他大多数操作为 O(n)</li>
 *   <li>Space complexity: O(n) for collected results - 空间复杂度: O(n)，存储收集结果</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MoreCollectorUtil {

    private MoreCollectorUtil() {
    }

    // ==================== 元素选择收集器 | Element Selection Collectors ====================

    /**
     * Collector to get the first element.
     * 收集以获取第一个元素。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Optional<T>> first() {
        return Collector.of(
                () -> new Object[]{null, false},
                (a, t) -> {
                    if (!(boolean) a[1]) {
                        a[0] = t;
                        a[1] = true;
                    }
                },
                (a, b) -> (boolean) a[1] ? a : b,
                a -> (boolean) a[1] ? Optional.of((T) a[0]) : Optional.empty()
        );
    }

    /**
     * Collector to get the last element.
     * 收集以获取最后一个元素。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Optional<T>> last() {
        return Collector.of(
                () -> new Object[]{null, false},
                (a, t) -> {
                    a[0] = t;
                    a[1] = true;
                },
                (a, b) -> (boolean) b[1] ? b : a,
                a -> (boolean) a[1] ? Optional.of((T) a[0]) : Optional.empty()
        );
    }

    /**
     * Collector to get only element (throws if not exactly one).
     * 收集以获取唯一元素（如果不是恰好一个则抛出异常）。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, T> onlyElement() {
        return Collector.of(
                () -> new Object[]{null, 0},
                (a, t) -> {
                    a[0] = t;
                    a[1] = (int) a[1] + 1;
                },
                (a, b) -> {
                    a[1] = (int) a[1] + (int) b[1];
                    return a;
                },
                a -> {
                    int count = (int) a[1];
                    if (count == 0) {
                        throw new NoSuchElementException("Expected exactly one element, but found none");
                    }
                    if (count > 1) {
                        throw new IllegalArgumentException("Expected exactly one element, but found " + count);
                    }
                    return (T) a[0];
                }
        );
    }

    /**
     * Collector to get element at index.
     * 收集以获取指定索引的元素。
     *
     * @param <T>   element type | 元素类型
     * @param index the index | 索引
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Optional<T>> elementAt(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be non-negative");
        }
        return Collector.of(
                () -> new Object[]{null, 0},
                (a, t) -> {
                    int currentIndex = (int) a[1];
                    if (currentIndex == index) {
                        a[0] = t;
                    }
                    a[1] = currentIndex + 1;
                },
                (a, b) -> a,
                a -> (int) a[1] > index ? Optional.of((T) a[0]) : Optional.empty()
        );
    }

    // ==================== 最小/最大收集器 | Min/Max Collectors ====================

    /**
     * Collector to get minimum by key.
     * 收集以按键获取最小值。
     *
     * @param <T>       element type | 元素类型
     * @param <U>       key type | 键类型
     * @param keyMapper the key mapper | 键映射器
     * @return collector | 收集器
     */
    public static <T, U extends Comparable<? super U>> Collector<T, ?, Optional<T>> minBy(
            Function<? super T, ? extends U> keyMapper) {
        return Collectors.minBy(Comparator.comparing(keyMapper));
    }

    /**
     * Collector to get maximum by key.
     * 收集以按键获取最大值。
     *
     * @param <T>       element type | 元素类型
     * @param <U>       key type | 键类型
     * @param keyMapper the key mapper | 键映射器
     * @return collector | 收集器
     */
    public static <T, U extends Comparable<? super U>> Collector<T, ?, Optional<T>> maxBy(
            Function<? super T, ? extends U> keyMapper) {
        return Collectors.maxBy(Comparator.comparing(keyMapper));
    }

    /**
     * Collector to get min and max.
     * 收集以获取最小值和最大值。
     *
     * @param <T>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return collector returning MinMax | 返回 MinMax 的收集器
     */
    public static <T> Collector<T, ?, MinMax<T>> minMax(Comparator<? super T> comparator) {
        return Collector.of(
                () -> new Object[]{null, null, false},
                (a, t) -> {
                    if (!(boolean) a[2]) {
                        a[0] = t;
                        a[1] = t;
                        a[2] = true;
                    } else {
                        if (comparator.compare(t, (T) a[0]) < 0) {
                            a[0] = t;
                        }
                        if (comparator.compare(t, (T) a[1]) > 0) {
                            a[1] = t;
                        }
                    }
                },
                (a, b) -> {
                    if (!(boolean) a[2]) return b;
                    if (!(boolean) b[2]) return a;
                    if (comparator.compare((T) b[0], (T) a[0]) < 0) {
                        a[0] = b[0];
                    }
                    if (comparator.compare((T) b[1], (T) a[1]) > 0) {
                        a[1] = b[1];
                    }
                    return a;
                },
                a -> (boolean) a[2] ? new MinMax<>((T) a[0], (T) a[1]) : new MinMax<>(null, null)
        );
    }

    /**
     * MinMax result container.
     * MinMax 结果容器。
     *
     * @param <T> element type | 元素类型
     */
    public record MinMax<T>(T min, T max) {
        public Optional<T> getMin() {
            return Optional.ofNullable(min);
        }

        public Optional<T> getMax() {
            return Optional.ofNullable(max);
        }

        public boolean isEmpty() {
            return min == null && max == null;
        }
    }

    // ==================== 多值映射收集器 | Multimap Collectors ====================

    /**
     * Collector to multimap (key to list of values).
     * 收集到多值映射（键到值列表）。
     *
     * @param <T>       element type | 元素类型
     * @param <K>       key type | 键类型
     * @param keyMapper the key mapper | 键映射器
     * @return collector | 收集器
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> toMultimap(
            Function<? super T, ? extends K> keyMapper) {
        return Collectors.groupingBy(keyMapper);
    }

    /**
     * Collector to multimap with value transformation.
     * 收集到带值转换的多值映射。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param keyMapper   the key mapper | 键映射器
     * @param valueMapper the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, Map<K, List<V>>> toMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collectors.groupingBy(keyMapper, Collectors.mapping(valueMapper, Collectors.toList()));
    }

    /**
     * Collector to multimap with set values.
     * 收集到带集合值的多值映射。
     *
     * @param <T>       element type | 元素类型
     * @param <K>       key type | 键类型
     * @param keyMapper the key mapper | 键映射器
     * @return collector | 收集器
     */
    public static <T, K> Collector<T, ?, Map<K, Set<T>>> toMultimapSet(
            Function<? super T, ? extends K> keyMapper) {
        return Collectors.groupingBy(keyMapper, Collectors.toSet());
    }

    // ==================== 条件收集器 | Conditional Collectors ====================

    /**
     * Collector that filters before collecting.
     * 在收集前过滤的收集器。
     *
     * @param <T>        element type | 元素类型
     * @param <A>        accumulator type | 累加器类型
     * @param <R>        result type | 结果类型
     * @param predicate  the predicate | 谓词
     * @param downstream the downstream collector | 下游收集器
     * @return collector | 收集器
     */
    public static <T, A, R> Collector<T, ?, R> filtering(
            Predicate<? super T> predicate,
            Collector<? super T, A, R> downstream) {
        return Collectors.filtering(predicate, downstream);
    }

    /**
     * Collector that transforms before collecting.
     * 在收集前转换的收集器。
     *
     * @param <T>        input type | 输入类型
     * @param <U>        mapped type | 映射类型
     * @param <A>        accumulator type | 累加器类型
     * @param <R>        result type | 结果类型
     * @param mapper     the mapper | 映射器
     * @param downstream the downstream collector | 下游收集器
     * @return collector | 收集器
     */
    public static <T, U, A, R> Collector<T, ?, R> mapping(
            Function<? super T, ? extends U> mapper,
            Collector<? super U, A, R> downstream) {
        return Collectors.mapping(mapper, downstream);
    }

    /**
     * Collector that transforms the result.
     * 转换结果的收集器。
     *
     * @param <T>        element type | 元素类型
     * @param <A>        accumulator type | 累加器类型
     * @param <R>        result type | 结果类型
     * @param <RR>       final result type | 最终结果类型
     * @param downstream the downstream collector | 下游收集器
     * @param finisher   the finisher | 完成器
     * @return collector | 收集器
     */
    public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(
            Collector<T, A, R> downstream,
            Function<R, RR> finisher) {
        return Collectors.collectingAndThen(downstream, finisher);
    }

    // ==================== 聚合收集器 | Aggregation Collectors ====================

    /**
     * Collector to fold elements.
     * 折叠元素的收集器。
     *
     * @param <T>      element type | 元素类型
     * @param <R>      result type | 结果类型
     * @param identity the identity value | 恒等值
     * @param folder   the folder function | 折叠函数
     * @return collector | 收集器
     */
    public static <T, R> Collector<T, ?, R> fold(R identity, BiFunction<R, ? super T, R> folder) {
        return Collector.of(
                () -> new Object[]{identity},
                (a, t) -> a[0] = folder.apply((R) a[0], t),
                (a, b) -> {
                    throw new UnsupportedOperationException("Parallel not supported");
                },
                a -> (R) a[0]
        );
    }

    /**
     * Collector to reduce with identity.
     * 带恒等值的归约收集器。
     *
     * @param <T>      element type | 元素类型
     * @param identity the identity value | 恒等值
     * @param reducer  the reducer | 归约器
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, T> reducing(T identity, BinaryOperator<T> reducer) {
        return Collectors.reducing(identity, reducer);
    }

    // ==================== 统计收集器 | Statistics Collectors ====================

    /**
     * Collector to get distinct count.
     * 获取不同计数的收集器。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Long> distinctCount() {
        return Collector.of(
                HashSet::new,
                HashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                set -> (long) set.size()
        );
    }

    /**
     * Collector to check if all match predicate.
     * 检查是否所有元素都匹配谓词的收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate the predicate | 谓词
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Boolean> allMatch(Predicate<? super T> predicate) {
        return Collector.of(
                () -> new boolean[]{true},
                (a, t) -> a[0] = a[0] && predicate.test(t),
                (a, b) -> {
                    a[0] = a[0] && b[0];
                    return a;
                },
                a -> a[0]
        );
    }

    /**
     * Collector to check if any match predicate.
     * 检查是否有元素匹配谓词的收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate the predicate | 谓词
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Boolean> anyMatch(Predicate<? super T> predicate) {
        return Collector.of(
                () -> new boolean[]{false},
                (a, t) -> a[0] = a[0] || predicate.test(t),
                (a, b) -> {
                    a[0] = a[0] || b[0];
                    return a;
                },
                a -> a[0]
        );
    }

    /**
     * Collector to check if none match predicate.
     * 检查是否没有元素匹配谓词的收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate the predicate | 谓词
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Boolean> noneMatch(Predicate<? super T> predicate) {
        return collectingAndThen(anyMatch(predicate), b -> !b);
    }

    // ==================== 不可变集合收集器 | Immutable Collection Collectors ====================

    /**
     * Collector to Optional (returns empty if empty, value if single, throws if multiple).
     * 收集到 Optional（空则返回空，单个则返回值，多个则抛出异常）。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, Optional<T>> toOptional() {
        return Collector.of(
                () -> new Object[]{null, 0},
                (a, t) -> {
                    a[0] = t;
                    a[1] = (int) a[1] + 1;
                },
                (a, b) -> {
                    a[1] = (int) a[1] + (int) b[1];
                    if ((int) b[1] > 0) a[0] = b[0];
                    return a;
                },
                a -> {
                    int count = (int) a[1];
                    if (count > 1) {
                        throw new IllegalArgumentException("Expected at most one element, but found " + count);
                    }
                    return count == 1 ? Optional.of((T) a[0]) : Optional.empty();
                }
        );
    }

    /**
     * Collector to ImmutableList.
     * 收集到 ImmutableList。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return Collector.of(
                ArrayList<T>::new,
                ArrayList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> ImmutableList.copyOf(list)
        );
    }

    /**
     * Collector to ImmutableSet.
     * 收集到 ImmutableSet。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return Collector.of(
                LinkedHashSet<T>::new,
                LinkedHashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                set -> ImmutableSet.copyOf(set)
        );
    }

    /**
     * Collector to ImmutableSortedSet using natural ordering.
     * 使用自然排序收集到 ImmutableSortedSet。
     *
     * @param <T> element type (must be Comparable) | 元素类型（必须是 Comparable）
     * @return collector | 收集器
     */
    public static <T extends Comparable<? super T>> Collector<T, ?, ImmutableSortedSet<T>> toImmutableSortedSet() {
        return Collector.of(
                TreeSet<T>::new,
                TreeSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                ImmutableSortedSet::copyOf
        );
    }

    /**
     * Collector to ImmutableSortedSet.
     * 收集到 ImmutableSortedSet。
     *
     * @param <T>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ImmutableSortedSet<T>> toImmutableSortedSet(Comparator<? super T> comparator) {
        return Collector.of(
                () -> new TreeSet<T>(comparator),
                TreeSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                set -> ImmutableSortedSet.copyOf(set, comparator)
        );
    }

    /**
     * Collector to ImmutableMap.
     * 收集到 ImmutableMap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                LinkedHashMap<K, V>::new,
                (map, t) -> map.put(keyMapper.apply(t), valueMapper.apply(t)),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                },
                ImmutableMap::copyOf
        );
    }

    /**
     * Collector to ImmutableMap with merge function.
     * 收集到带合并函数的 ImmutableMap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valueMapper   the value mapper | 值映射器
     * @param mergeFunction the merge function | 合并函数
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return Collector.of(
                LinkedHashMap<K, V>::new,
                (map, t) -> {
                    K key = keyMapper.apply(t);
                    V value = valueMapper.apply(t);
                    map.merge(key, value, mergeFunction);
                },
                (left, right) -> {
                    right.forEach((k, v) -> left.merge(k, v, mergeFunction));
                    return left;
                },
                ImmutableMap::copyOf
        );
    }

    /**
     * Collector to ImmutableBiMap.
     * 收集到 ImmutableBiMap。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param keyMapper   the key mapper | 键映射器
     * @param valueMapper the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableBiMap::<K, V>builder,
                (builder, t) -> builder.put(keyMapper.apply(t), valueMapper.apply(t)),
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableBiMap.Builder::build
        );
    }

    /**
     * Collector to ImmutableMultiset.
     * 收集到 ImmutableMultiset。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, ImmutableMultiset<T>> toImmutableMultiset() {
        return Collector.of(
                ImmutableMultiset::<T>builder,
                ImmutableMultiset.Builder::add,
                (left, right) -> {
                    left.addAll(right.build());
                    return left;
                },
                ImmutableMultiset.Builder::build
        );
    }

    /**
     * Collector to ImmutableListMultimap.
     * 收集到 ImmutableListMultimap。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param keyMapper   the key mapper | 键映射器
     * @param valueMapper the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> toImmutableListMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableListMultimap::<K, V>builder,
                (builder, t) -> builder.put(keyMapper.apply(t), valueMapper.apply(t)),
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableListMultimap.Builder::build
        );
    }

    /**
     * Collector to ImmutableSetMultimap.
     * 收集到 ImmutableSetMultimap。
     *
     * @param <T>         element type | 元素类型
     * @param <K>         key type | 键类型
     * @param <V>         value type | 值类型
     * @param keyMapper   the key mapper | 键映射器
     * @param valueMapper the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableSetMultimap::<K, V>builder,
                (builder, t) -> builder.put(keyMapper.apply(t), valueMapper.apply(t)),
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableSetMultimap.Builder::build
        );
    }

    /**
     * Collector to ImmutableListMultimap with flattening.
     * 收集到带扁平化的 ImmutableListMultimap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valuesMapper  the values stream mapper | 值流映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableListMultimap<K, V>> flatteningToImmutableListMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends Stream<? extends V>> valuesMapper) {
        return Collector.of(
                ImmutableListMultimap::<K, V>builder,
                (builder, t) -> {
                    K key = keyMapper.apply(t);
                    valuesMapper.apply(t).forEach(v -> builder.put(key, v));
                },
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableListMultimap.Builder::build
        );
    }

    /**
     * Collector to ImmutableSetMultimap with flattening.
     * 收集到带扁平化的 ImmutableSetMultimap。
     *
     * @param <T>           element type | 元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyMapper     the key mapper | 键映射器
     * @param valuesMapper  the values stream mapper | 值流映射器
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> flatteningToImmutableSetMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends Stream<? extends V>> valuesMapper) {
        return Collector.of(
                ImmutableSetMultimap::<K, V>builder,
                (builder, t) -> {
                    K key = keyMapper.apply(t);
                    valuesMapper.apply(t).forEach(v -> builder.put(key, v));
                },
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableSetMultimap.Builder::build
        );
    }

    /**
     * Collector to ImmutableTable.
     * 收集到 ImmutableTable。
     *
     * @param <T>           element type | 元素类型
     * @param <R>           row type | 行类型
     * @param <C>           column type | 列类型
     * @param <V>           value type | 值类型
     * @param rowMapper     the row mapper | 行映射器
     * @param columnMapper  the column mapper | 列映射器
     * @param valueMapper   the value mapper | 值映射器
     * @return collector | 收集器
     */
    public static <T, R, C, V> Collector<T, ?, ImmutableTable<R, C, V>> toImmutableTable(
            Function<? super T, ? extends R> rowMapper,
            Function<? super T, ? extends C> columnMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableTable::<R, C, V>builder,
                (builder, t) -> builder.put(rowMapper.apply(t), columnMapper.apply(t), valueMapper.apply(t)),
                (left, right) -> {
                    left.putAll(right.build());
                    return left;
                },
                ImmutableTable.Builder::build
        );
    }

    /**
     * Collector to ImmutableTable with merge function.
     * 收集到带合并函数的 ImmutableTable。
     *
     * @param <T>           element type | 元素类型
     * @param <R>           row type | 行类型
     * @param <C>           column type | 列类型
     * @param <V>           value type | 值类型
     * @param rowMapper     the row mapper | 行映射器
     * @param columnMapper  the column mapper | 列映射器
     * @param valueMapper   the value mapper | 值映射器
     * @param mergeFunction the merge function | 合并函数
     * @return collector | 收集器
     */
    public static <T, R, C, V> Collector<T, ?, ImmutableTable<R, C, V>> toImmutableTable(
            Function<? super T, ? extends R> rowMapper,
            Function<? super T, ? extends C> columnMapper,
            Function<? super T, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction) {
        return Collector.of(
                () -> new LinkedHashMap<R, Map<C, V>>(),
                (map, t) -> {
                    R row = rowMapper.apply(t);
                    C col = columnMapper.apply(t);
                    V val = valueMapper.apply(t);
                    map.computeIfAbsent(row, k -> new LinkedHashMap<>())
                            .merge(col, val, mergeFunction);
                },
                (left, right) -> {
                    right.forEach((row, colMap) -> {
                        Map<C, V> leftColMap = left.computeIfAbsent(row, k -> new LinkedHashMap<>());
                        colMap.forEach((col, val) -> leftColMap.merge(col, val, mergeFunction));
                    });
                    return left;
                },
                map -> {
                    ImmutableTable.Builder<R, C, V> builder = ImmutableTable.builder();
                    map.forEach((row, colMap) -> colMap.forEach((col, val) -> builder.put(row, col, val)));
                    return builder.build();
                }
        );
    }

    /**
     * Collector to create unique index map.
     * 收集到唯一索引映射。
     *
     * @param <T>       element type | 元素类型
     * @param <K>       key type | 键类型
     * @param keyMapper the key mapper | 键映射器
     * @return collector | 收集器
     */
    public static <T, K> Collector<T, ?, ImmutableMap<K, T>> indexing(Function<? super T, ? extends K> keyMapper) {
        return toImmutableMap(keyMapper, Function.identity());
    }

    // ==================== Top K 收集器 | Top K Collectors ====================

    /**
     * Collector to get the minimum k elements.
     * 收集以获取最小的 k 个元素。
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> minK(int k, Comparator<? super T> comparator) {
        if (k <= 0) {
            return Collector.of(
                    ArrayList<T>::new,
                    (list, t) -> {},
                    (left, right) -> left,
                    list -> Collections.emptyList()
            );
        }
        Comparator<T> typedComparator = (Comparator<T>) comparator;
        return Collector.of(
                () -> new PriorityQueue<T>(k, typedComparator.reversed()),
                (pq, t) -> {
                    if (pq.size() < k) {
                        pq.offer(t);
                    } else if (typedComparator.compare(t, pq.peek()) < 0) {
                        pq.poll();
                        pq.offer(t);
                    }
                },
                (left, right) -> {
                    for (T t : right) {
                        if (left.size() < k) {
                            left.offer(t);
                        } else if (typedComparator.compare(t, left.peek()) < 0) {
                            left.poll();
                            left.offer(t);
                        }
                    }
                    return left;
                },
                pq -> {
                    List<T> result = new ArrayList<>(pq);
                    result.sort(typedComparator);
                    return result;
                }
        );
    }

    /**
     * Collector to get the maximum k elements.
     * 收集以获取最大的 k 个元素。
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> maxK(int k, Comparator<? super T> comparator) {
        Comparator<T> typedComparator = (Comparator<T>) comparator;
        return minK(k, typedComparator.reversed());
    }

    // ==================== 分区收集器 | Partition Collectors ====================

    /**
     * Collector to partition by size.
     * 按大小分区的收集器。
     *
     * @param <T>  element type | 元素类型
     * @param size the partition size | 分区大小
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, List<List<T>>> partitionBySize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive");
        }
        return Collector.of(
                ArrayList::new,
                (lists, t) -> {
                    if (lists.isEmpty() || lists.getLast().size() >= size) {
                        lists.add(new ArrayList<>());
                    }
                    lists.getLast().add(t);
                },
                (left, right) -> {
                    if (left.isEmpty()) {
                        return right;
                    }
                    if (right.isEmpty()) {
                        return left;
                    }
                    // Merge last of left with first of right if possible
                    List<T> lastLeft = left.getLast();
                    if (lastLeft.size() < size) {
                        List<T> firstRight = right.getFirst();
                        int needed = size - lastLeft.size();
                        int toMove = Math.min(needed, firstRight.size());
                        lastLeft.addAll(firstRight.subList(0, toMove));
                        if (toMove == firstRight.size()) {
                            right.removeFirst();
                        } else {
                            right.set(0, new ArrayList<>(firstRight.subList(toMove, firstRight.size())));
                        }
                    }
                    left.addAll(right);
                    return left;
                }
        );
    }

    /**
     * Collector to partition by predicate.
     * 按谓词分区的收集器。
     *
     * @param <T>       element type | 元素类型
     * @param predicate the predicate | 谓词
     * @return collector returning Map with Boolean keys | 返回 Boolean 键映射的收集器
     */
    public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) {
        return Collectors.partitioningBy(predicate);
    }
}
