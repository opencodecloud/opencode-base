package cloud.opencode.base.core.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import cloud.opencode.base.core.tuple.Pair;

/**
 * OpenCollections - Unmodifiable collection factory and utility methods.
 * OpenCollections - 不可修改集合的工厂与工具方法。
 *
 * <p>All methods return standard JDK unmodifiable collections ({@link List},
 * {@link Set}, {@link Map}). Null elements and keys are rejected.
 * Every "mutation" method returns a <em>new</em> collection, leaving the
 * original unchanged.</p>
 * <p>所有方法返回标准 JDK 不可修改集合。拒绝 null 元素和键。
 * 每个"变更"方法都返回<em>新</em>集合，原集合保持不变。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> All public methods are stateless
 * and return unmodifiable collections, making them inherently thread-safe.</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class OpenCollections {

    private OpenCollections() {
        throw new AssertionError("No OpenCollections instances");
    }

    // ==================== List Builder ====================

    /**
     * Creates a new {@link ListBuilder} with default initial capacity.
     * 创建具有默认初始容量的 {@link ListBuilder}。
     *
     * @param <T> the element type / 元素类型
     * @return a new list builder / 新列表构建器
     */
    public static <T> ListBuilder<T> listBuilder() {
        return new ListBuilder<>();
    }

    /**
     * Creates a new {@link ListBuilder} with the specified expected size.
     * 创建具有指定预期大小的 {@link ListBuilder}。
     *
     * @param <T>          the element type / 元素类型
     * @param expectedSize the expected number of elements / 预期元素数量
     * @return a new list builder / 新列表构建器
     * @throws IllegalArgumentException if expectedSize is negative / 如果 expectedSize 为负数
     */
    public static <T> ListBuilder<T> listBuilder(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("expectedSize must be non-negative: " + expectedSize);
        }
        return new ListBuilder<>(expectedSize);
    }

    // ==================== Map Builder ====================

    /**
     * Creates a new {@link MapBuilder} with default initial capacity.
     * 创建具有默认初始容量的 {@link MapBuilder}。
     *
     * @param <K> the key type / 键类型
     * @param <V> the value type / 值类型
     * @return a new map builder / 新映射构建器
     */
    public static <K, V> MapBuilder<K, V> mapBuilder() {
        return new MapBuilder<>();
    }

    // ==================== Immutable List Operations ====================

    /**
     * Returns a new unmodifiable list with the given element appended.
     * 返回在末尾追加给定元素的新不可修改列表。
     *
     * @param <T>     the element type / 元素类型
     * @param list    the source list / 源列表
     * @param element the element to append (must not be null) / 要追加的元素（不可为 null）
     * @return a new unmodifiable list / 新不可修改列表
     * @throws NullPointerException if list or element is null / 如果 list 或 element 为 null
     */
    public static <T> List<T> append(List<T> list, T element) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(element, "element");
        List<T> result = new ArrayList<>(list.size() + 1);
        result.addAll(list);
        result.add(element);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a new unmodifiable list with the given element prepended.
     * 返回在开头插入给定元素的新不可修改列表。
     *
     * @param <T>     the element type / 元素类型
     * @param element the element to prepend (must not be null) / 要插入的元素（不可为 null）
     * @param list    the source list / 源列表
     * @return a new unmodifiable list / 新不可修改列表
     * @throws NullPointerException if list or element is null / 如果 list 或 element 为 null
     */
    public static <T> List<T> prepend(T element, List<T> list) {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(list, "list");
        List<T> result = new ArrayList<>(list.size() + 1);
        result.add(element);
        result.addAll(list);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a new unmodifiable list that is the concatenation of two lists.
     * 返回两个列表连接后的新不可修改列表。
     *
     * @param <T> the element type / 元素类型
     * @param a   the first list / 第一个列表
     * @param b   the second list / 第二个列表
     * @return a new unmodifiable list containing all elements of a followed by b /
     *         包含 a 和 b 所有元素的新不可修改列表
     * @throws NullPointerException if a or b is null / 如果 a 或 b 为 null
     */
    public static <T> List<T> concat(List<T> a, List<T> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        // Clamp capacity to Integer.MAX_VALUE to avoid overflow
        int capacity = (int) Math.min((long) a.size() + b.size(), Integer.MAX_VALUE);
        List<T> result = new ArrayList<>(capacity);
        result.addAll(a);
        result.addAll(b);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a new unmodifiable list with the first occurrence of the given element removed.
     * 返回移除给定元素第一次出现后的新不可修改列表。
     *
     * <p>If the element is not present, returns an unmodifiable copy of the original list.</p>
     * <p>如果元素不存在，返回原列表的不可修改副本。</p>
     *
     * @param <T>     the element type / 元素类型
     * @param list    the source list / 源列表
     * @param element the element to remove (must not be null) / 要移除的元素（不可为 null）
     * @return a new unmodifiable list / 新不可修改列表
     * @throws NullPointerException if list or element is null / 如果 list 或 element 为 null
     */
    public static <T> List<T> without(List<T> list, T element) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(element, "element");
        // Single-pass: copy elements, skip first match
        List<T> result = new ArrayList<>(list.size());
        boolean removed = false;
        for (T item : list) {
            if (!removed && element.equals(item)) {
                removed = true;
            } else {
                result.add(item);
            }
        }
        if (!removed) {
            return List.copyOf(list);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a new unmodifiable list with the element at the given index replaced.
     * 返回将指定索引处元素替换后的新不可修改列表。
     *
     * @param <T>        the element type / 元素类型
     * @param list       the source list / 源列表
     * @param index      the index of the element to replace / 要替换的元素索引
     * @param newElement the replacement element (must not be null) / 替换元素（不可为 null）
     * @return a new unmodifiable list / 新不可修改列表
     * @throws NullPointerException      if list or newElement is null / 如果 list 或 newElement 为 null
     * @throws IndexOutOfBoundsException if index is out of range / 如果索引越界
     */
    public static <T> List<T> withReplaced(List<T> list, int index, T newElement) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(newElement, "newElement");
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for list of size " + list.size());
        }
        List<T> result = new ArrayList<>(list);
        result.set(index, newElement);
        return Collections.unmodifiableList(result);
    }

    // ==================== Set Operations ====================

    /**
     * Returns a new unmodifiable set that is the union of two sets.
     * 返回两个集合并集的新不可修改集合。
     *
     * @param <T> the element type / 元素类型
     * @param a   the first set / 第一个集合
     * @param b   the second set / 第二个集合
     * @return the union of a and b / a 和 b 的并集
     * @throws NullPointerException if a or b is null / 如果 a 或 b 为 null
     */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        int capacity = (int) Math.min((long) a.size() + b.size(), Integer.MAX_VALUE);
        Set<T> result = new LinkedHashSet<>(capacity);
        result.addAll(a);
        result.addAll(b);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns a new unmodifiable set that is the intersection of two sets.
     * 返回两个集合交集的新不可修改集合。
     *
     * @param <T> the element type / 元素类型
     * @param a   the first set / 第一个集合
     * @param b   the second set / 第二个集合
     * @return the intersection of a and b / a 和 b 的交集
     * @throws NullPointerException if a or b is null / 如果 a 或 b 为 null
     */
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        // Iterate over the smaller set for efficiency
        Set<T> smaller = a.size() <= b.size() ? a : b;
        Set<T> larger = smaller == a ? b : a;
        Set<T> result = new LinkedHashSet<>();
        for (T element : smaller) {
            if (larger.contains(element)) {
                result.add(element);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Returns a new unmodifiable set containing elements in {@code a} but not in {@code b}.
     * 返回包含在 {@code a} 中但不在 {@code b} 中的元素的新不可修改集合。
     *
     * @param <T> the element type / 元素类型
     * @param a   the first set / 第一个集合
     * @param b   the second set / 第二个集合
     * @return the difference a \ b / 差集 a \ b
     * @throws NullPointerException if a or b is null / 如果 a 或 b 为 null
     */
    public static <T> Set<T> difference(Set<T> a, Set<T> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        Set<T> result = new LinkedHashSet<>();
        for (T element : a) {
            if (!b.contains(element)) {
                result.add(element);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    // ==================== Partition & Grouping | 分区与分组 ====================

    /**
     * Splits a list into two groups based on a predicate.
     * 根据谓词将列表分为两组。
     *
     * <p>Elements matching the predicate are placed in the {@code true} list,
     * others in the {@code false} list. Both result lists are unmodifiable.</p>
     *
     * @param <T>       the element type / 元素类型
     * @param list      the source list / 源列表
     * @param predicate the predicate to test elements / 测试元素的谓词
     * @return a map with {@code true} and {@code false} keys / 包含 true 和 false 键的映射
     * @throws NullPointerException if list or predicate is null
     */
    public static <T> Map<Boolean, List<T>> partition(List<T> list, Predicate<? super T> predicate) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(predicate, "predicate");
        List<T> trueList = new ArrayList<>();
        List<T> falseList = new ArrayList<>();
        for (T element : list) {
            if (predicate.test(element)) {
                trueList.add(element);
            } else {
                falseList.add(element);
            }
        }
        Map<Boolean, List<T>> result = new LinkedHashMap<>(4);
        result.put(true, Collections.unmodifiableList(trueList));
        result.put(false, Collections.unmodifiableList(falseList));
        return Collections.unmodifiableMap(result);
    }

    /**
     * Groups elements by a classifier function.
     * 根据分类函数对元素进行分组。
     *
     * <p>Returns an unmodifiable map with unmodifiable list values.
     * Preserves insertion order for both keys and values.</p>
     *
     * @param <T>       the element type / 元素类型
     * @param <K>       the key type / 键类型
     * @param list      the source list / 源列表
     * @param classifier the classifier function / 分类函数
     * @return grouped elements / 分组后的元素
     * @throws NullPointerException if list, classifier, or any computed key is null
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<? super T, ? extends K> classifier) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(classifier, "classifier");
        Map<K, List<T>> groups = new LinkedHashMap<>();
        for (T element : list) {
            K key = Objects.requireNonNull(classifier.apply(element), "classifier returned null key");
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(element);
        }
        Map<K, List<T>> result = new LinkedHashMap<>(groups.size());
        groups.forEach((k, v) -> result.put(k, Collections.unmodifiableList(v)));
        return Collections.unmodifiableMap(result);
    }

    // ==================== Chunk & Sliding | 分块与滑动窗口 ====================

    /**
     * Splits a list into fixed-size chunks. The last chunk may be smaller.
     * 将列表分成固定大小的块。最后一块可能较小。
     *
     * @param <T>  the element type / 元素类型
     * @param list the source list / 源列表
     * @param size the chunk size (must be positive) / 块大小（必须为正数）
     * @return unmodifiable list of unmodifiable chunks / 不可修改块列表
     * @throws NullPointerException     if list is null
     * @throws IllegalArgumentException if size is not positive
     */
    public static <T> List<List<T>> chunk(List<T> list, int size) {
        Objects.requireNonNull(list, "list");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive, got: " + size);
        }
        if (list.isEmpty()) {
            return List.of();
        }
        int listSize = list.size();
        int chunkCount = (int) Math.min(((long) listSize + size - 1) / size, Integer.MAX_VALUE);
        List<List<T>> chunks = new ArrayList<>(chunkCount);
        for (int i = 0; i < listSize; i += size) {
            chunks.add(List.copyOf(list.subList(i, Math.min(i + size, listSize))));
        }
        return Collections.unmodifiableList(chunks);
    }

    /**
     * Returns sliding windows of the given size with step 1.
     * 返回步长为 1 的给定大小的滑动窗口。
     *
     * @param <T>  the element type / 元素类型
     * @param list the source list / 源列表
     * @param size the window size (must be positive) / 窗口大小（必须为正数）
     * @return unmodifiable list of unmodifiable windows / 不可修改窗口列表
     * @throws NullPointerException     if list is null
     * @throws IllegalArgumentException if size is not positive
     */
    public static <T> List<List<T>> sliding(List<T> list, int size) {
        Objects.requireNonNull(list, "list");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive, got: " + size);
        }
        if (list.size() < size) {
            return List.of();
        }
        int windowCount = list.size() - size + 1;
        List<List<T>> windows = new ArrayList<>(windowCount);
        for (int i = 0; i < windowCount; i++) {
            windows.add(List.copyOf(list.subList(i, i + size)));
        }
        return Collections.unmodifiableList(windows);
    }

    // ==================== Zip | 合并 ====================

    /**
     * Zips two lists into a list of {@link Pair}s, truncated to the shorter length.
     * 将两个列表合并为 {@link Pair} 列表，截断到较短长度。
     *
     * @param <A> the first element type / 第一个元素类型
     * @param <B> the second element type / 第二个元素类型
     * @param a   the first list / 第一个列表
     * @param b   the second list / 第二个列表
     * @return unmodifiable list of pairs / 不可修改的 Pair 列表
     * @throws NullPointerException if a or b is null
     */
    public static <A, B> List<Pair<A, B>> zip(List<A> a, List<B> b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        int size = Math.min(a.size(), b.size());
        List<Pair<A, B>> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(Pair.of(a.get(i), b.get(i)));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Zips two lists using a combiner function, truncated to the shorter length.
     * 使用组合函数合并两个列表，截断到较短长度。
     *
     * @param <A>      the first element type / 第一个元素类型
     * @param <B>      the second element type / 第二个元素类型
     * @param <R>      the result type / 结果类型
     * @param a        the first list / 第一个列表
     * @param b        the second list / 第二个列表
     * @param combiner the combiner function / 组合函数
     * @return unmodifiable list of combined results / 不可修改的组合结果列表
     * @throws NullPointerException if any argument is null
     */
    public static <A, B, R> List<R> zipWith(List<A> a, List<B> b, BiFunction<? super A, ? super B, ? extends R> combiner) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        Objects.requireNonNull(combiner, "combiner");
        int size = Math.min(a.size(), b.size());
        List<R> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(combiner.apply(a.get(i), b.get(i)));
        }
        return Collections.unmodifiableList(result);
    }

    // ==================== Distinct & Frequency | 去重与频率 ====================

    /**
     * Returns a new list with duplicates removed based on a key extractor.
     * 根据键提取器去重，保留首次出现的顺序。
     *
     * @param <T>          the element type / 元素类型
     * @param <K>          the key type / 键类型
     * @param list         the source list / 源列表
     * @param keyExtractor the key extractor / 键提取器
     * @return unmodifiable deduplicated list / 不可修改的去重列表
     * @throws NullPointerException if list or keyExtractor is null
     */
    public static <T, K> List<T> distinctBy(List<T> list, Function<? super T, ? extends K> keyExtractor) {
        Objects.requireNonNull(list, "list");
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        Set<K> seen = new java.util.HashSet<>();
        List<T> result = new ArrayList<>();
        for (T element : list) {
            if (seen.add(keyExtractor.apply(element))) {
                result.add(element);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns element frequencies as an unmodifiable map.
     * 返回元素频率的不可修改映射。
     *
     * @param <T>        the element type / 元素类型
     * @param collection the source collection / 源集合
     * @return unmodifiable map of element to count / 不可修改的元素到计数映射
     * @throws NullPointerException if collection is null
     */
    public static <T> Map<T, Long> frequencies(java.util.Collection<T> collection) {
        Objects.requireNonNull(collection, "collection");
        Map<T, Long> freq = new LinkedHashMap<>();
        for (T element : collection) {
            freq.merge(element, 1L, Long::sum);
        }
        return Collections.unmodifiableMap(freq);
    }

    // ==================== Flatten | 展平 ====================

    /**
     * Flattens a list of lists into a single unmodifiable list.
     * 将嵌套列表展平为单个不可修改列表。
     *
     * @param <T>   the element type / 元素类型
     * @param lists the nested lists / 嵌套列表
     * @return unmodifiable flattened list / 不可修改的展平列表
     * @throws NullPointerException if lists is null
     */
    public static <T> List<T> flatten(List<? extends List<T>> lists) {
        Objects.requireNonNull(lists, "lists");
        long totalSizeLong = 0;
        for (List<T> inner : lists) {
            Objects.requireNonNull(inner, "inner list must not be null");
            totalSizeLong += inner.size();
        }
        int totalSize = (int) Math.min(totalSizeLong, Integer.MAX_VALUE);
        List<T> result = new ArrayList<>(totalSize);
        for (List<T> inner : lists) {
            result.addAll(inner);
        }
        return Collections.unmodifiableList(result);
    }

    // ==================== Collectors ====================

    /**
     * Returns a {@link Collector} that accumulates elements into an unmodifiable {@link List}.
     * 返回将元素收集为不可修改 {@link List} 的 {@link Collector}。
     *
     * <p>Null elements are permitted.</p>
     * <p>允许 null 元素。</p>
     *
     * @param <T> the element type / 元素类型
     * @return a collector producing an unmodifiable list / 产生不可修改列表的收集器
     */
    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                Collections::unmodifiableList
        );
    }

    /**
     * Returns a {@link Collector} that accumulates elements into an unmodifiable {@link Set}.
     * 返回将元素收集为不可修改 {@link Set} 的 {@link Collector}。
     *
     * <p>Null elements are permitted.</p>
     * <p>允许 null 元素。</p>
     *
     * @param <T> the element type / 元素类型
     * @return a collector producing an unmodifiable set / 产生不可修改集合的收集器
     */
    public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collectors.collectingAndThen(
                Collectors.toCollection(LinkedHashSet::new),
                Collections::unmodifiableSet
        );
    }

    // ==================== ListBuilder ====================

    /**
     * A builder for creating unmodifiable lists incrementally.
     * 用于增量创建不可修改列表的构建器。
     *
     * @param <T> the element type / 元素类型
     * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static final class ListBuilder<T> {
        private final List<T> elements;

        ListBuilder() {
            this.elements = new ArrayList<>();
        }

        ListBuilder(int expectedSize) {
            this.elements = new ArrayList<>(expectedSize);
        }

        /**
         * Adds an element to this builder.
         * 向此构建器添加元素。
         *
         * @param element the element to add (must not be null) / 要添加的元素（不可为 null）
         * @return this builder / 此构建器
         * @throws NullPointerException if element is null / 如果 element 为 null
         */
        public ListBuilder<T> add(T element) {
            Objects.requireNonNull(element, "element");
            elements.add(element);
            return this;
        }

        /**
         * Adds all elements from the iterable to this builder.
         * 将可迭代对象中的所有元素添加到此构建器。
         *
         * @param iterable the elements to add (must not be null, no null elements) /
         *                 要添加的元素（不可为 null，元素不可为 null）
         * @return this builder / 此构建器
         * @throws NullPointerException if iterable or any element is null /
         *                              如果 iterable 或任何元素为 null
         */
        public ListBuilder<T> addAll(Iterable<? extends T> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (T element : iterable) {
                Objects.requireNonNull(element, "element in iterable");
                elements.add(element);
            }
            return this;
        }

        /**
         * Builds and returns an unmodifiable list containing all added elements.
         * 构建并返回包含所有已添加元素的不可修改列表。
         *
         * @return an unmodifiable list / 不可修改列表
         */
        public List<T> build() {
            return List.copyOf(elements);
        }
    }

    // ==================== MapBuilder ====================

    /**
     * A builder for creating unmodifiable maps incrementally.
     * 用于增量创建不可修改映射的构建器。
     *
     * @param <K> the key type / 键类型
     * @param <V> the value type / 值类型
     * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static final class MapBuilder<K, V> {
        private final Map<K, V> entries;

        MapBuilder() {
            this.entries = new LinkedHashMap<>();
        }

        /**
         * Puts a key-value pair into this builder.
         * 向此构建器添加键值对。
         *
         * @param key   the key (must not be null) / 键（不可为 null）
         * @param value the value (must not be null) / 值（不可为 null）
         * @return this builder / 此构建器
         * @throws NullPointerException if key or value is null / 如果 key 或 value 为 null
         */
        public MapBuilder<K, V> put(K key, V value) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
            entries.put(key, value);
            return this;
        }

        /**
         * Puts all entries from the given map into this builder.
         * 将给定映射的所有条目添加到此构建器。
         *
         * @param map the map whose entries to add (must not be null, no null keys or values) /
         *            要添加条目的映射（不可为 null，键和值不可为 null）
         * @return this builder / 此构建器
         * @throws NullPointerException if map, any key, or any value is null /
         *                              如果 map、任何键或任何值为 null
         */
        public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
            Objects.requireNonNull(map, "map");
            map.forEach((k, v) -> {
                Objects.requireNonNull(k, "key in map");
                Objects.requireNonNull(v, "value in map");
                entries.put(k, v);
            });
            return this;
        }

        /**
         * Builds and returns an unmodifiable map containing all added entries.
         * 构建并返回包含所有已添加条目的不可修改映射。
         *
         * @return an unmodifiable map / 不可修改映射
         */
        public Map<K, V> build() {
            return Map.copyOf(entries);
        }
    }
}
