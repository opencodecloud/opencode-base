package cloud.opencode.base.collections;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * OpenCollectors - Unified Streaming API Entry Point
 * OpenCollectors - 统一流式 API 入口
 *
 * <p>Provides a fluent API for collection operations, combining the power of
 * streams with collection-specific operations.</p>
 * <p>提供流式 API 进行集合操作，结合 Stream 的强大功能和集合特定操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent collection creation - 流式集合创建</li>
 *   <li>Grouping and partitioning - 分组和分区</li>
 *   <li>Set algebra operations - 集合代数运算</li>
 *   <li>Custom collectors - 自定义收集器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from collection - 从集合创建
 * List<String> names = OpenCollectors.from(users)
 *     .filter(u -> u.getAge() > 18)
 *     .map(User::getName)
 *     .distinct()
 *     .sorted()
 *     .limit(10)
 *     .toList();
 *
 * // Grouping - 分组
 * Map<String, List<User>> byCity = OpenCollectors.from(users)
 *     .groupBy(User::getCity);
 *
 * // Partitioning - 分区
 * List<List<User>> batches = OpenCollectors.from(users)
 *     .partition(100);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Based on Stream API - 基于 Stream API</li>
 *   <li>Lazy evaluation where possible - 尽可能延迟求值</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class OpenCollectors {

    private OpenCollectors() {
    }

    // ==================== 流式入口 | Streaming Entry Points ====================

    /**
     * Create a CollectorFlow from an Iterable.
     * 从 Iterable 创建 CollectorFlow。
     *
     * @param <E>      element type | 元素类型
     * @param iterable the iterable | 可迭代对象
     * @return collector flow | 收集器流
     */
    public static <E> CollectorFlow<E> from(Iterable<E> iterable) {
        if (iterable == null) {
            return new CollectorFlow<>(Stream.empty());
        }
        if (iterable instanceof Collection<E> coll) {
            return new CollectorFlow<>(coll.stream());
        }
        return new CollectorFlow<>(StreamSupport.stream(iterable.spliterator(), false));
    }

    /**
     * Create a CollectorFlow from an array.
     * 从数组创建 CollectorFlow。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return collector flow | 收集器流
     */
    @SafeVarargs
    public static <E> CollectorFlow<E> of(E... elements) {
        if (elements == null || elements.length == 0) {
            return new CollectorFlow<>(Stream.empty());
        }
        return new CollectorFlow<>(Arrays.stream(elements));
    }

    /**
     * Create a CollectorFlow from a Stream.
     * 从 Stream 创建 CollectorFlow。
     *
     * @param <E>    element type | 元素类型
     * @param stream the stream | 流
     * @return collector flow | 收集器流
     */
    public static <E> CollectorFlow<E> fromStream(Stream<E> stream) {
        return new CollectorFlow<>(stream != null ? stream : Stream.empty());
    }

    // ==================== 集合代数 | Set Algebra ====================

    /**
     * Create a SetAlgebra for set operations.
     * 创建用于集合运算的 SetAlgebra。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return SetAlgebra instance | SetAlgebra 实例
     */
    public static <E> SetAlgebra<E> algebra(Set<E> set) {
        return SetAlgebra.of(set);
    }

    // ==================== 收集器 | Collectors ====================

    /**
     * Collector to ImmutableList.
     * 收集到 ImmutableList。
     *
     * @param <E> element type | 元素类型
     * @return collector | 收集器
     */
    public static <E> Collector<E, ?, ImmutableList<E>> toImmutableList() {
        return Collector.of(
                ImmutableList::<E>builder,
                ImmutableList.Builder::add,
                (b1, b2) -> {
                    b1.addAll(b2.build());
                    return b1;
                },
                ImmutableList.Builder::build
        );
    }

    /**
     * Collector to ImmutableSet.
     * 收集到 ImmutableSet。
     *
     * @param <E> element type | 元素类型
     * @return collector | 收集器
     */
    public static <E> Collector<E, ?, ImmutableSet<E>> toImmutableSet() {
        return Collector.of(
                ImmutableSet::<E>builder,
                ImmutableSet.Builder::add,
                (b1, b2) -> {
                    b1.addAll(b2.build());
                    return b1;
                },
                ImmutableSet.Builder::build
        );
    }

    /**
     * Collector to ImmutableMap.
     * 收集到 ImmutableMap。
     *
     * @param <T>           input element type | 输入元素类型
     * @param <K>           key type | 键类型
     * @param <V>           value type | 值类型
     * @param keyFunction   the key function | 键函数
     * @param valueFunction the value function | 值函数
     * @return collector | 收集器
     */
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, ? extends K> keyFunction,
            Function<? super T, ? extends V> valueFunction) {
        return Collector.of(
                ImmutableMap::<K, V>builder,
                (builder, t) -> builder.put(keyFunction.apply(t), valueFunction.apply(t)),
                (b1, b2) -> {
                    b1.putAll(b2.build());
                    return b1;
                },
                ImmutableMap.Builder::build
        );
    }

    /**
     * Collector to Multiset.
     * 收集到 Multiset。
     *
     * @param <E> element type | 元素类型
     * @return collector | 收集器
     */
    public static <E> Collector<E, ?, Multiset<E>> toMultiset() {
        return Collector.of(
                HashMultiset::<E>create,
                Multiset::add,
                (m1, m2) -> {
                    m1.addAll(m2);
                    return m1;
                }
        );
    }

    /**
     * Collector to count elements (Multiset).
     * 收集并计数元素。
     *
     * @param <E> element type | 元素类型
     * @return collector | 收集器
     */
    public static <E> Collector<E, ?, Map<E, Long>> counting() {
        return Collectors.groupingBy(Function.identity(), Collectors.counting());
    }

    /**
     * Collector to get only element.
     * 收集唯一元素。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     * @throws IllegalArgumentException if not exactly one element | 如果不是恰好一个元素
     */
    public static <T> Collector<T, ?, T> onlyElement() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalArgumentException(
                                "Expected exactly one element, but got " + list.size());
                    }
                    return list.getFirst();
                }
        );
    }

    /**
     * Collector to get optional single element.
     * 收集可选的单个元素。
     *
     * @param <T> element type | 元素类型
     * @return collector | 收集器
     * @throws IllegalArgumentException if more than one element | 如果多于一个元素
     */
    public static <T> Collector<T, ?, Optional<T>> toOptional() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.isEmpty()) {
                        return Optional.empty();
                    }
                    if (list.size() > 1) {
                        throw new IllegalArgumentException(
                                "Expected at most one element, but got " + list.size());
                    }
                    return Optional.of(list.getFirst());
                }
        );
    }

    /**
     * Collector to get least K elements.
     * 收集最小的 K 个元素。
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, List<T>> leastK(int k, Comparator<? super T> comparator) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    list.sort(comparator);
                    return list.subList(0, Math.min(k, list.size()));
                }
        );
    }

    /**
     * Collector to get greatest K elements.
     * 收集最大的 K 个元素。
     *
     * @param <T>        element type | 元素类型
     * @param k          number of elements | 元素数量
     * @param comparator the comparator | 比较器
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, List<T>> greatestK(int k, Comparator<? super T> comparator) {
        return leastK(k, comparator.reversed());
    }

    /**
     * Collector to partition by size.
     * 按大小分区收集。
     *
     * @param <T>  element type | 元素类型
     * @param size partition size | 分区大小
     * @return collector | 收集器
     */
    public static <T> Collector<T, ?, List<List<T>>> partitionBySize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Partition size must be positive: " + size);
        }
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    List<List<T>> result = new ArrayList<>((list.size() + size - 1) / size);
                    for (int i = 0; i < list.size(); i += size) {
                        result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
                    }
                    return result;
                }
        );
    }

    // ==================== CollectorFlow 内部类 | CollectorFlow Inner Class ====================

    /**
     * Fluent collection flow for chained operations.
     * 用于链式操作的流式集合流。
     *
     * @param <E> element type | 元素类型
     */
    public static final class CollectorFlow<E> {
        private final Stream<E> stream;

        CollectorFlow(Stream<E> stream) {
            this.stream = stream;
        }

        /**
         * Filter elements.
         * 过滤元素。
         *
         * @param predicate the predicate | 谓词
         * @return new flow | 新的流
         */
        public CollectorFlow<E> filter(Predicate<? super E> predicate) {
            return new CollectorFlow<>(stream.filter(predicate));
        }

        /**
         * Map elements.
         * 映射元素。
         *
         * @param <R>    result type | 结果类型
         * @param mapper the mapper | 映射器
         * @return new flow | 新的流
         */
        public <R> CollectorFlow<R> map(Function<? super E, ? extends R> mapper) {
            return new CollectorFlow<>(stream.map(mapper));
        }

        /**
         * FlatMap elements.
         * 扁平映射元素。
         *
         * @param <R>    result type | 结果类型
         * @param mapper the mapper | 映射器
         * @return new flow | 新的流
         */
        public <R> CollectorFlow<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
            return new CollectorFlow<>(stream.flatMap(mapper));
        }

        /**
         * Get distinct elements.
         * 获取不同元素。
         *
         * @return new flow | 新的流
         */
        public CollectorFlow<E> distinct() {
            return new CollectorFlow<>(stream.distinct());
        }

        /**
         * Sort elements.
         * 排序元素。
         *
         * @return new flow | 新的流
         */
        public CollectorFlow<E> sorted() {
            return new CollectorFlow<>(stream.sorted());
        }

        /**
         * Sort elements with comparator.
         * 使用比较器排序元素。
         *
         * @param comparator the comparator | 比较器
         * @return new flow | 新的流
         */
        public CollectorFlow<E> sorted(Comparator<? super E> comparator) {
            return new CollectorFlow<>(stream.sorted(comparator));
        }

        /**
         * Limit elements.
         * 限制元素数量。
         *
         * @param maxSize maximum size | 最大数量
         * @return new flow | 新的流
         */
        public CollectorFlow<E> limit(long maxSize) {
            return new CollectorFlow<>(stream.limit(maxSize));
        }

        /**
         * Skip elements.
         * 跳过元素。
         *
         * @param n number to skip | 跳过数量
         * @return new flow | 新的流
         */
        public CollectorFlow<E> skip(long n) {
            return new CollectorFlow<>(stream.skip(n));
        }

        /**
         * Peek at elements.
         * 窥视元素。
         *
         * @param action the action | 动作
         * @return new flow | 新的流
         */
        public CollectorFlow<E> peek(Consumer<? super E> action) {
            return new CollectorFlow<>(stream.peek(action));
        }

        // ==================== 终端操作 | Terminal Operations ====================

        /**
         * Collect to List.
         * 收集到 List。
         *
         * @return list | 列表
         */
        public List<E> toList() {
            return stream.toList();
        }

        /**
         * Collect to ArrayList.
         * 收集到 ArrayList。
         *
         * @return ArrayList | ArrayList
         */
        public ArrayList<E> toArrayList() {
            return stream.collect(Collectors.toCollection(ArrayList::new));
        }

        /**
         * Collect to Set.
         * 收集到 Set。
         *
         * @return set | 集合
         */
        public Set<E> toSet() {
            return stream.collect(Collectors.toSet());
        }

        /**
         * Collect to ImmutableList.
         * 收集到 ImmutableList。
         *
         * @return immutable list | 不可变列表
         */
        public ImmutableList<E> toImmutableList() {
            return stream.collect(OpenCollectors.toImmutableList());
        }

        /**
         * Collect to ImmutableSet.
         * 收集到 ImmutableSet。
         *
         * @return immutable set | 不可变集合
         */
        public ImmutableSet<E> toImmutableSet() {
            return stream.collect(OpenCollectors.toImmutableSet());
        }

        /**
         * Group by key.
         * 按键分组。
         *
         * @param <K>           key type | 键类型
         * @param keyExtractor the key extractor | 键提取器
         * @return grouped map | 分组 Map
         */
        public <K> Map<K, List<E>> groupBy(Function<? super E, ? extends K> keyExtractor) {
            return stream.collect(Collectors.groupingBy(keyExtractor));
        }

        /**
         * Partition by size.
         * 按大小分区。
         *
         * @param size partition size | 分区大小
         * @return partitions | 分区列表
         */
        public List<List<E>> partition(int size) {
            return stream.collect(OpenCollectors.partitionBySize(size));
        }

        /**
         * Count elements.
         * 计数元素。
         *
         * @return count | 数量
         */
        public long count() {
            return stream.count();
        }

        /**
         * Find first element.
         * 查找第一个元素。
         *
         * @return optional element | 可选元素
         */
        public Optional<E> findFirst() {
            return stream.findFirst();
        }

        /**
         * Find any element.
         * 查找任意元素。
         *
         * @return optional element | 可选元素
         */
        public Optional<E> findAny() {
            return stream.findAny();
        }

        /**
         * Check if any match.
         * 检查是否有任意匹配。
         *
         * @param predicate the predicate | 谓词
         * @return true if any match | 如果有任意匹配则返回 true
         */
        public boolean anyMatch(Predicate<? super E> predicate) {
            return stream.anyMatch(predicate);
        }

        /**
         * Check if all match.
         * 检查是否全部匹配。
         *
         * @param predicate the predicate | 谓词
         * @return true if all match | 如果全部匹配则返回 true
         */
        public boolean allMatch(Predicate<? super E> predicate) {
            return stream.allMatch(predicate);
        }

        /**
         * Check if none match.
         * 检查是否无匹配。
         *
         * @param predicate the predicate | 谓词
         * @return true if none match | 如果无匹配则返回 true
         */
        public boolean noneMatch(Predicate<? super E> predicate) {
            return stream.noneMatch(predicate);
        }

        /**
         * For each element.
         * 遍历每个元素。
         *
         * @param action the action | 动作
         */
        public void forEach(Consumer<? super E> action) {
            stream.forEach(action);
        }

        /**
         * Collect using a collector.
         * 使用收集器收集。
         *
         * @param <R>       result type | 结果类型
         * @param <A>       accumulator type | 累加器类型
         * @param collector the collector | 收集器
         * @return result | 结果
         */
        public <R, A> R collect(Collector<? super E, A, R> collector) {
            return stream.collect(collector);
        }

        /**
         * Get the underlying stream.
         * 获取底层流。
         *
         * @return stream | 流
         */
        public Stream<E> stream() {
            return stream;
        }
    }
}
