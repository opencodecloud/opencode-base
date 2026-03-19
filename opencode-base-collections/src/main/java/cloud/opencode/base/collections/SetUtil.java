package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 * SetUtil - Set Utility Class
 * SetUtil - 集合工具类
 *
 * <p>Provides factory methods and set operations including union, intersection,
 * difference, and symmetric difference.</p>
 * <p>提供集合工厂方法和集合运算，包括并集、交集、差集和对称差集。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for set creation - 集合创建工厂方法</li>
 *   <li>Set algebra operations (union, intersection, difference) - 集合代数运算</li>
 *   <li>Power set and combinations - 幂集和组合</li>
 *   <li>Cartesian product - 笛卡尔积</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create HashSet - 创建 HashSet
 * Set<String> set = SetUtil.newHashSet("a", "b", "c");
 *
 * // Union view - 并集视图
 * SetView<String> union = SetUtil.union(set1, set2);
 *
 * // Intersection view - 交集视图
 * SetView<String> intersection = SetUtil.intersection(set1, set2);
 *
 * // Difference view - 差集视图
 * SetView<String> difference = SetUtil.difference(set1, set2);
 *
 * // Power set - 幂集
 * Set<Set<String>> powerSet = SetUtil.powerSet(set);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Factory methods: O(n) - 工厂方法: O(n)</li>
 *   <li>Set operations: O(1) creation, O(n) iteration - 集合运算: O(1) 创建, O(n) 遍历</li>
 *   <li>Power set: O(2^n) - 幂集: O(2^n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (except concurrent variants) - 线程安全: 否（并发变体除外）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class SetUtil {

    private SetUtil() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a new HashSet
     * 创建 HashSet
     *
     * @param <E> element type | 元素类型
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Create a new HashSet with initial elements
     * 创建带初始元素的 HashSet
     *
     * @param <E>      element type | 元素类型
     * @param elements initial elements | 初始元素
     * @return new HashSet | 新的 HashSet
     */
    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        if (elements == null || elements.length == 0) {
            return new HashSet<>();
        }
        HashSet<E> set = HashSet.newHashSet(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Create a new HashSet from an Iterable
     * 从 Iterable 创建 HashSet
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
        if (elements == null) {
            return new HashSet<>();
        }
        if (elements instanceof Collection<? extends E> collection) {
            return new HashSet<>(collection);
        }
        HashSet<E> set = new HashSet<>();
        for (E e : elements) {
            set.add(e);
        }
        return set;
    }

    /**
     * Create a new HashSet with expected size
     * 创建指定容量的 HashSet
     *
     * @param <E>          element type | 元素类型
     * @param expectedSize expected size | 预期大小
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSetWithExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Expected size cannot be negative: " + expectedSize);
        }
        return HashSet.newHashSet(expectedSize);
    }

    /**
     * Create a new LinkedHashSet
     * 创建 LinkedHashSet
     *
     * @param <E> element type | 元素类型
     * @return new LinkedHashSet | 新的 LinkedHashSet
     */
    public static <E> LinkedHashSet<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Create a new LinkedHashSet with expected size
     * 创建指定容量的 LinkedHashSet
     *
     * @param <E>          element type | 元素类型
     * @param expectedSize expected size | 预期大小
     * @return new LinkedHashSet | 新的 LinkedHashSet
     */
    public static <E> LinkedHashSet<E> newLinkedHashSetWithExpectedSize(int expectedSize) {
        if (expectedSize < 0) {
            throw new IllegalArgumentException("Expected size cannot be negative: " + expectedSize);
        }
        return LinkedHashSet.newLinkedHashSet(expectedSize);
    }

    /**
     * Create a new TreeSet
     * 创建 TreeSet
     *
     * @param <E> element type | 元素类型
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E extends Comparable<E>> TreeSet<E> newTreeSet() {
        return new TreeSet<>();
    }

    /**
     * Create a new TreeSet with comparator
     * 带比较器的 TreeSet
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
        return new TreeSet<>(comparator);
    }

    /**
     * Create a new CopyOnWriteArraySet
     * 创建 CopyOnWriteArraySet
     *
     * @param <E> element type | 元素类型
     * @return new CopyOnWriteArraySet | 新的 CopyOnWriteArraySet
     */
    public static <E> CopyOnWriteArraySet<E> newCopyOnWriteArraySet() {
        return new CopyOnWriteArraySet<>();
    }

    /**
     * Create a new concurrent hash set
     * 创建 ConcurrentHashSet
     *
     * @param <E> element type | 元素类型
     * @return new concurrent set | 新的并发集合
     */
    public static <E> Set<E> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    // ==================== 集合运算 | Set Operations ====================

    /**
     * Return a union view of two sets
     * 并集视图
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return union view | 并集视图
     */
    public static <E> SetView<E> union(Set<? extends E> set1, Set<? extends E> set2) {
        final Set<? extends E> s1 = set1 == null ? Collections.emptySet() : set1;
        final Set<? extends E> s2 = set2 == null ? Collections.emptySet() : set2;

        return new SetView<>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<>() {
                    private final Iterator<? extends E> it1 = s1.iterator();
                    private final Iterator<? extends E> it2 = s2.iterator();
                    private E next;
                    private boolean hasNext;
                    private boolean useIt1 = true;

                    private void advance() {
                        hasNext = false;
                        while (useIt1 && it1.hasNext()) {
                            next = it1.next();
                            hasNext = true;
                            return;
                        }
                        useIt1 = false;
                        while (it2.hasNext()) {
                            E candidate = it2.next();
                            if (!s1.contains(candidate)) {
                                next = candidate;
                                hasNext = true;
                                return;
                            }
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        if (!hasNext) {
                            advance();
                        }
                        return hasNext;
                    }

                    @Override
                    public E next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        E result = next;
                        hasNext = false;
                        return result;
                    }
                };
            }

            @Override
            public int size() {
                int size = s1.size();
                for (E e : s2) {
                    if (!s1.contains(e)) {
                        size++;
                    }
                }
                return size;
            }

            @Override
            public boolean contains(Object o) {
                return s1.contains(o) || s2.contains(o);
            }
        };
    }

    /**
     * Return an intersection view of two sets
     * 交集视图
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return intersection view | 交集视图
     */
    public static <E> SetView<E> intersection(Set<E> set1, Set<?> set2) {
        final Set<E> s1 = set1 == null ? Collections.emptySet() : set1;
        final Set<?> s2 = set2 == null ? Collections.emptySet() : set2;

        return new SetView<>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<>() {
                    private final Iterator<E> it = s1.iterator();
                    private E next;
                    private boolean hasNext;

                    private void advance() {
                        hasNext = false;
                        while (it.hasNext()) {
                            E candidate = it.next();
                            if (s2.contains(candidate)) {
                                next = candidate;
                                hasNext = true;
                                return;
                            }
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        if (!hasNext) {
                            advance();
                        }
                        return hasNext;
                    }

                    @Override
                    public E next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        E result = next;
                        hasNext = false;
                        return result;
                    }
                };
            }

            @Override
            public int size() {
                int size = 0;
                for (E e : s1) {
                    if (s2.contains(e)) {
                        size++;
                    }
                }
                return size;
            }

            @Override
            public boolean contains(Object o) {
                return s1.contains(o) && s2.contains(o);
            }
        };
    }

    /**
     * Return a difference view (set1 - set2)
     * 差集视图
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return difference view | 差集视图
     */
    public static <E> SetView<E> difference(Set<E> set1, Set<?> set2) {
        final Set<E> s1 = set1 == null ? Collections.emptySet() : set1;
        final Set<?> s2 = set2 == null ? Collections.emptySet() : set2;

        return new SetView<>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<>() {
                    private final Iterator<E> it = s1.iterator();
                    private E next;
                    private boolean hasNext;

                    private void advance() {
                        hasNext = false;
                        while (it.hasNext()) {
                            E candidate = it.next();
                            if (!s2.contains(candidate)) {
                                next = candidate;
                                hasNext = true;
                                return;
                            }
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        if (!hasNext) {
                            advance();
                        }
                        return hasNext;
                    }

                    @Override
                    public E next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        E result = next;
                        hasNext = false;
                        return result;
                    }
                };
            }

            @Override
            public int size() {
                int size = 0;
                for (E e : s1) {
                    if (!s2.contains(e)) {
                        size++;
                    }
                }
                return size;
            }

            @Override
            public boolean contains(Object o) {
                return s1.contains(o) && !s2.contains(o);
            }
        };
    }

    /**
     * Return a symmetric difference view
     * 对称差集视图
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return symmetric difference view | 对称差集视图
     */
    public static <E> SetView<E> symmetricDifference(Set<? extends E> set1, Set<? extends E> set2) {
        final Set<? extends E> s1 = set1 == null ? Collections.emptySet() : set1;
        final Set<? extends E> s2 = set2 == null ? Collections.emptySet() : set2;

        return new SetView<>() {
            @Override
            public Iterator<E> iterator() {
                return IteratorUtil.concat(
                        difference(new HashSet<>(s1), s2).iterator(),
                        difference(new HashSet<>(s2), s1).iterator()
                );
            }

            @Override
            public int size() {
                int size = 0;
                for (E e : s1) {
                    if (!s2.contains(e)) {
                        size++;
                    }
                }
                for (E e : s2) {
                    if (!s1.contains(e)) {
                        size++;
                    }
                }
                return size;
            }

            @Override
            public boolean contains(Object o) {
                return s1.contains(o) ^ s2.contains(o);
            }
        };
    }

    // ==================== 高级操作 | Advanced Operations ====================

    /**
     * Compute the Cartesian product of sets
     * 笛卡尔积
     *
     * @param <E>  element type | 元素类型
     * @param sets sets to compute product | 要计算积的集合
     * @return Cartesian product | 笛卡尔积
     */
    public static <E> Set<List<E>> cartesianProduct(List<? extends Set<? extends E>> sets) {
        if (sets == null || sets.isEmpty()) {
            Set<List<E>> result = new HashSet<>();
            result.add(Collections.emptyList());
            return result;
        }
        for (Set<? extends E> set : sets) {
            if (set == null || set.isEmpty()) {
                return Collections.emptySet();
            }
        }
        List<List<? extends E>> lists = new ArrayList<>();
        for (Set<? extends E> set : sets) {
            lists.add(new ArrayList<>(set));
        }
        return new HashSet<>(ListUtil.cartesianProduct(lists));
    }

    /**
     * Compute the Cartesian product of sets
     * 笛卡尔积
     *
     * @param <E>  element type | 元素类型
     * @param sets sets to compute product | 要计算积的集合
     * @return Cartesian product | 笛卡尔积
     */
    @SafeVarargs
    public static <E> Set<List<E>> cartesianProduct(Set<? extends E>... sets) {
        if (sets == null || sets.length == 0) {
            Set<List<E>> result = new HashSet<>();
            result.add(Collections.emptyList());
            return result;
        }
        return cartesianProduct(Arrays.asList(sets));
    }

    /**
     * Compute the power set (all subsets)
     * 幂集（所有子集）
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return power set | 幂集
     */
    public static <E> Set<Set<E>> powerSet(Set<E> set) {
        if (set == null || set.isEmpty()) {
            Set<Set<E>> result = new HashSet<>();
            result.add(Collections.emptySet());
            return result;
        }
        if (set.size() > 30) {
            throw new IllegalArgumentException("Set too large for power set: " + set.size());
        }
        List<E> elements = new ArrayList<>(set);
        int n = elements.size();
        int powerSetSize = 1 << n;
        Set<Set<E>> result = new HashSet<>(powerSetSize);
        for (int i = 0; i < powerSetSize; i++) {
            Set<E> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    subset.add(elements.get(j));
                }
            }
            result.add(subset);
        }
        return result;
    }

    /**
     * Compute combinations of specified size
     * 指定大小的组合
     *
     * @param <E>  element type | 元素类型
     * @param set  the set | 集合
     * @param size combination size | 组合大小
     * @return set of combinations | 组合集合
     */
    public static <E> Set<Set<E>> combinations(Set<E> set, int size) {
        if (set == null || size < 0) {
            return Collections.emptySet();
        }
        if (size == 0) {
            Set<Set<E>> result = new HashSet<>();
            result.add(Collections.emptySet());
            return result;
        }
        if (size > set.size()) {
            return Collections.emptySet();
        }
        Set<Set<E>> result = new HashSet<>();
        List<E> elements = new ArrayList<>(set);
        combinationsHelper(elements, size, 0, new ArrayList<>(), result);
        return result;
    }

    private static <E> void combinationsHelper(List<E> elements, int size, int start,
                                                List<E> current, Set<Set<E>> result) {
        if (current.size() == size) {
            result.add(new HashSet<>(current));
            return;
        }
        for (int i = start; i < elements.size(); i++) {
            current.add(elements.get(i));
            combinationsHelper(elements, size, i + 1, current, result);
            current.removeLast();
        }
    }

    // ==================== 过滤 | Filtering ====================

    /**
     * Return a filtered view of a set
     * 过滤视图
     *
     * @param <E>        element type | 元素类型
     * @param unfiltered the unfiltered set | 未过滤的集合
     * @param predicate  the predicate | 谓词
     * @return filtered view | 过滤视图
     */
    public static <E> Set<E> filter(Set<E> unfiltered, Predicate<? super E> predicate) {
        if (unfiltered == null || predicate == null) {
            return Collections.emptySet();
        }
        return new FilteredSet<>(unfiltered, predicate);
    }

    /**
     * Return a filtered view of a SortedSet
     * 过滤 SortedSet 视图
     *
     * @param <E>        element type | 元素类型
     * @param unfiltered the unfiltered set | 未过滤的集合
     * @param predicate  the predicate | 谓词
     * @return filtered view | 过滤视图
     */
    public static <E> SortedSet<E> filter(SortedSet<E> unfiltered, Predicate<? super E> predicate) {
        if (unfiltered == null || predicate == null) {
            return Collections.emptySortedSet();
        }
        return new FilteredSortedSet<>(unfiltered, predicate);
    }

    /**
     * Return a filtered view of a NavigableSet
     * 过滤 NavigableSet 视图
     *
     * @param <E>        element type | 元素类型
     * @param unfiltered the unfiltered set | 未过滤的集合
     * @param predicate  the predicate | 谓词
     * @return filtered view | 过滤视图
     */
    public static <E> NavigableSet<E> filter(NavigableSet<E> unfiltered, Predicate<? super E> predicate) {
        if (unfiltered == null || predicate == null) {
            return Collections.emptyNavigableSet();
        }
        return new FilteredNavigableSet<>(unfiltered, predicate);
    }

    // ==================== 包装 | Wrappers ====================

    /**
     * Return a synchronized set
     * 同步包装
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return synchronized set | 同步集合
     */
    public static <E> Set<E> synchronizedSet(Set<E> set) {
        return set == null ? Collections.emptySet() : Collections.synchronizedSet(set);
    }

    /**
     * Return an unmodifiable set
     * 不可修改包装
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return unmodifiable set | 不可修改集合
     */
    public static <E> Set<E> unmodifiableSet(Set<? extends E> set) {
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    /**
     * Return an unmodifiable NavigableSet
     * NavigableSet 视图
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return unmodifiable NavigableSet | 不可修改的 NavigableSet
     */
    public static <E> NavigableSet<E> unmodifiableNavigableSet(NavigableSet<E> set) {
        return set == null ? Collections.emptyNavigableSet() : Collections.unmodifiableNavigableSet(set);
    }

    /**
     * Return a synchronized NavigableSet
     * 同步 NavigableSet
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return synchronized NavigableSet | 同步的 NavigableSet
     */
    public static <E> NavigableSet<E> synchronizedNavigableSet(NavigableSet<E> set) {
        return set == null ? Collections.emptyNavigableSet() : Collections.synchronizedNavigableSet(set);
    }

    // ==================== 辅助方法 | Helper Methods ====================

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Filtered set view
     */
    private static class FilteredSet<E> extends AbstractSet<E> {
        private final Set<E> unfiltered;
        private final Predicate<? super E> predicate;

        FilteredSet(Set<E> unfiltered, Predicate<? super E> predicate) {
            this.unfiltered = unfiltered;
            this.predicate = predicate;
        }

        @Override
        public Iterator<E> iterator() {
            return IteratorUtil.filter(unfiltered.iterator(), predicate);
        }

        @Override
        public int size() {
            int size = 0;
            for (E e : unfiltered) {
                if (predicate.test(e)) {
                    size++;
                }
            }
            return size;
        }

        @Override
        public boolean contains(Object o) {
            try {
                @SuppressWarnings("unchecked")
                E e = (E) o;
                return unfiltered.contains(o) && predicate.test(e);
            } catch (ClassCastException e) {
                return false;
            }
        }
    }

    /**
     * Filtered sorted set view
     */
    private static class FilteredSortedSet<E> extends FilteredSet<E> implements SortedSet<E> {
        private final SortedSet<E> sortedUnfiltered;

        FilteredSortedSet(SortedSet<E> unfiltered, Predicate<? super E> predicate) {
            super(unfiltered, predicate);
            this.sortedUnfiltered = unfiltered;
        }

        @Override
        public Comparator<? super E> comparator() {
            return sortedUnfiltered.comparator();
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return new FilteredSortedSet<>(sortedUnfiltered.subSet(fromElement, toElement),
                    e -> ((FilteredSet<E>) this).predicate.test(e));
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return new FilteredSortedSet<>(sortedUnfiltered.headSet(toElement),
                    e -> ((FilteredSet<E>) this).predicate.test(e));
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return new FilteredSortedSet<>(sortedUnfiltered.tailSet(fromElement),
                    e -> ((FilteredSet<E>) this).predicate.test(e));
        }

        @Override
        public E first() {
            for (E e : sortedUnfiltered) {
                if (((FilteredSet<E>) this).predicate.test(e)) {
                    return e;
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public E last() {
            E last = null;
            boolean found = false;
            for (E e : sortedUnfiltered) {
                if (((FilteredSet<E>) this).predicate.test(e)) {
                    last = e;
                    found = true;
                }
            }
            if (!found) {
                throw new NoSuchElementException();
            }
            return last;
        }
    }

    /**
     * Filtered navigable set view
     */
    private static class FilteredNavigableSet<E> extends FilteredSortedSet<E> implements NavigableSet<E> {
        private final NavigableSet<E> navigableUnfiltered;
        private final Predicate<? super E> predicate;

        FilteredNavigableSet(NavigableSet<E> unfiltered, Predicate<? super E> predicate) {
            super(unfiltered, predicate);
            this.navigableUnfiltered = unfiltered;
            this.predicate = predicate;
        }

        @Override
        public E lower(E e) {
            for (E candidate : navigableUnfiltered.headSet(e, false).descendingSet()) {
                if (predicate.test(candidate)) {
                    return candidate;
                }
            }
            return null;
        }

        @Override
        public E floor(E e) {
            for (E candidate : navigableUnfiltered.headSet(e, true).descendingSet()) {
                if (predicate.test(candidate)) {
                    return candidate;
                }
            }
            return null;
        }

        @Override
        public E ceiling(E e) {
            for (E candidate : navigableUnfiltered.tailSet(e, true)) {
                if (predicate.test(candidate)) {
                    return candidate;
                }
            }
            return null;
        }

        @Override
        public E higher(E e) {
            for (E candidate : navigableUnfiltered.tailSet(e, false)) {
                if (predicate.test(candidate)) {
                    return candidate;
                }
            }
            return null;
        }

        @Override
        public E pollFirst() {
            throw new UnsupportedOperationException("Filtered view does not support pollFirst");
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException("Filtered view does not support pollLast");
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new FilteredNavigableSet<>(navigableUnfiltered.descendingSet(), predicate);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return IteratorUtil.filter(navigableUnfiltered.descendingIterator(), predicate);
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new FilteredNavigableSet<>(
                    navigableUnfiltered.subSet(fromElement, fromInclusive, toElement, toInclusive),
                    predicate);
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new FilteredNavigableSet<>(navigableUnfiltered.headSet(toElement, inclusive), predicate);
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new FilteredNavigableSet<>(navigableUnfiltered.tailSet(fromElement, inclusive), predicate);
        }
    }
}
