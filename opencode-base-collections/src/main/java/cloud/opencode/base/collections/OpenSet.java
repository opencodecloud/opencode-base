package cloud.opencode.base.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * OpenSet - Set Facade Utility Class
 * OpenSet - Set 门面工具类
 *
 * <p>Provides comprehensive set operations including creation, set algebra,
 * filtering, and querying.</p>
 * <p>提供全面的 Set 操作，包括创建、集合代数、过滤和查询。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for set creation - Set 创建工厂方法</li>
 *   <li>Set algebra operations - 集合代数运算</li>
 *   <li>Set filtering - Set 过滤</li>
 *   <li>Power set and combinations - 幂集和组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create set - 创建 Set
 * Set<String> set = OpenSet.of("a", "b", "c");
 *
 * // Union - 并集
 * SetView<String> union = OpenSet.union(set1, set2);
 *
 * // Intersection - 交集
 * SetView<String> intersection = OpenSet.intersection(set1, set2);
 *
 * // Power set - 幂集
 * Set<Set<String>> powerSet = OpenSet.powerSet(set);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Most operations: O(n) - 大多数操作: O(n)</li>
 *   <li>Contains: O(1) for HashSet - 包含: HashSet 为 O(1)</li>
 *   <li>Power set: O(2^n) - 幂集: O(2^n)</li>
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
public final class OpenSet {

    private OpenSet() {
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashSet.
     * 创建空的 HashSet。
     *
     * @param <E> element type | 元素类型
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> newHashSet() {
        return new HashSet<>();
    }

    /**
     * Create a HashSet with elements.
     * 创建包含元素的 HashSet。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new HashSet | 新的 HashSet
     */
    @SafeVarargs
    public static <E> HashSet<E> of(E... elements) {
        HashSet<E> set = new HashSet<>(MapUtil.capacity(elements.length));
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * Create a HashSet from an Iterable.
     * 从 Iterable 创建 HashSet。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> from(Iterable<? extends E> elements) {
        if (elements instanceof Collection<?> coll) {
            return new HashSet<>((Collection<? extends E>) coll);
        }
        HashSet<E> set = new HashSet<>();
        elements.forEach(set::add);
        return set;
    }

    /**
     * Create a HashSet with expected size.
     * 创建具有预期大小的 HashSet。
     *
     * @param <E>          element type | 元素类型
     * @param expectedSize expected size | 预期大小
     * @return new HashSet | 新的 HashSet
     */
    public static <E> HashSet<E> withExpectedSize(int expectedSize) {
        return new HashSet<>(MapUtil.capacity(expectedSize));
    }

    /**
     * Create an empty LinkedHashSet.
     * 创建空的 LinkedHashSet。
     *
     * @param <E> element type | 元素类型
     * @return new LinkedHashSet | 新的 LinkedHashSet
     */
    public static <E> LinkedHashSet<E> newLinkedHashSet() {
        return new LinkedHashSet<>();
    }

    /**
     * Create an empty TreeSet.
     * 创建空的 TreeSet。
     *
     * @param <E> element type | 元素类型
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E extends Comparable<E>> TreeSet<E> newTreeSet() {
        return new TreeSet<>();
    }

    /**
     * Create a TreeSet with comparator.
     * 创建具有比较器的 TreeSet。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new TreeSet | 新的 TreeSet
     */
    public static <E> TreeSet<E> newTreeSet(Comparator<? super E> comparator) {
        return new TreeSet<>(comparator);
    }

    /**
     * Create a concurrent hash set.
     * 创建并发哈希集合。
     *
     * @param <E> element type | 元素类型
     * @return new concurrent set | 新的并发 Set
     */
    public static <E> Set<E> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    // ==================== 集合代数 | Set Algebra ====================

    /**
     * Compute union of two sets as a SetView.
     * 计算两个集合的并集视图。
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return union view | 并集视图
     */
    public static <E> SetView<E> union(Set<? extends E> set1, Set<? extends E> set2) {
        return SetUtil.union(set1, set2);
    }

    /**
     * Compute intersection of two sets as a SetView.
     * 计算两个集合的交集视图。
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return intersection view | 交集视图
     */
    public static <E> SetView<E> intersection(Set<E> set1, Set<?> set2) {
        return SetUtil.intersection(set1, set2);
    }

    /**
     * Compute difference of two sets as a SetView.
     * 计算两个集合的差集视图。
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return difference view | 差集视图
     */
    public static <E> SetView<E> difference(Set<E> set1, Set<?> set2) {
        return SetUtil.difference(set1, set2);
    }

    /**
     * Compute symmetric difference of two sets as a SetView.
     * 计算两个集合的对称差集视图。
     *
     * @param <E>  element type | 元素类型
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return symmetric difference view | 对称差集视图
     */
    public static <E> SetView<E> symmetricDifference(Set<? extends E> set1, Set<? extends E> set2) {
        return SetUtil.symmetricDifference(set1, set2);
    }

    // ==================== 高级操作 | Advanced Operations ====================

    /**
     * Compute power set (all subsets) of a set.
     * 计算集合的幂集（所有子集）。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return power set | 幂集
     */
    public static <E> Set<Set<E>> powerSet(Set<E> set) {
        return SetUtil.powerSet(set);
    }

    /**
     * Compute combinations of given size.
     * 计算给定大小的组合。
     *
     * @param <E>  element type | 元素类型
     * @param set  the set | 集合
     * @param size combination size | 组合大小
     * @return set of combinations | 组合集合
     */
    public static <E> Set<Set<E>> combinations(Set<E> set, int size) {
        return SetUtil.combinations(set, size);
    }

    /**
     * Compute Cartesian product of sets.
     * 计算集合的笛卡尔积。
     *
     * @param <E>  element type | 元素类型
     * @param sets the sets | 集合
     * @return Cartesian product | 笛卡尔积
     */
    @SafeVarargs
    public static <E> Set<List<E>> cartesianProduct(Set<? extends E>... sets) {
        return SetUtil.cartesianProduct(sets);
    }

    // ==================== 过滤操作 | Filter Operations ====================

    /**
     * Filter set by predicate.
     * 按谓词过滤集合。
     *
     * @param <E>       element type | 元素类型
     * @param set       the set | 集合
     * @param predicate the predicate | 谓词
     * @return filtered set | 过滤后的集合
     */
    public static <E> Set<E> filter(Set<E> set, Predicate<? super E> predicate) {
        if (set == null || set.isEmpty()) {
            return Collections.emptySet();
        }
        Set<E> result = new HashSet<>();
        for (E element : set) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    // ==================== 查询操作 | Query Operations ====================

    /**
     * Get the SetAlgebra interface for the set.
     * 获取集合的 SetAlgebra 接口。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return SetAlgebra instance | SetAlgebra 实例
     */
    public static <E> SetAlgebra<E> algebra(Set<E> set) {
        return SetAlgebra.of(set);
    }

    /**
     * Check if sets are disjoint.
     * 检查集合是否不相交。
     *
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return true if disjoint | 如果不相交则返回 true
     */
    public static boolean disjoint(Set<?> set1, Set<?> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return true;
        }
        Set<?> smaller = set1.size() <= set2.size() ? set1 : set2;
        Set<?> larger = smaller == set1 ? set2 : set1;
        for (Object element : smaller) {
            if (larger.contains(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if one set is a subset of another.
     * 检查一个集合是否为另一个的子集。
     *
     * @param subset   potential subset | 潜在子集
     * @param superset potential superset | 潜在超集
     * @return true if subset | 如果是子集则返回 true
     */
    public static boolean isSubset(Set<?> subset, Set<?> superset) {
        if (subset == null || subset.isEmpty()) {
            return true;
        }
        if (superset == null) {
            return false;
        }
        return superset.containsAll(subset);
    }

    /**
     * Check if sets are equal (same elements).
     * 检查集合是否相等（相同元素）。
     *
     * @param set1 first set | 第一个集合
     * @param set2 second set | 第二个集合
     * @return true if equal | 如果相等则返回 true
     */
    public static boolean equals(Set<?> set1, Set<?> set2) {
        if (set1 == set2) {
            return true;
        }
        if (set1 == null || set2 == null) {
            return false;
        }
        return set1.equals(set2);
    }
}
