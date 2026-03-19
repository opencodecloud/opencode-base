package cloud.opencode.base.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * SetAlgebra - Interface for set algebra operations
 * SetAlgebra - 集合代数操作接口
 *
 * <p>Provides a fluent interface for performing set algebra operations
 * such as union, intersection, difference, and symmetric difference.</p>
 * <p>提供流式接口执行集合代数运算，如并集、交集、差集和对称差集。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Union operation - 并集运算</li>
 *   <li>Intersection operation - 交集运算</li>
 *   <li>Difference operation - 差集运算</li>
 *   <li>Symmetric difference operation - 对称差集运算</li>
 *   <li>Subset and superset checking - 子集和超集检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Set<String> set1 = Set.of("a", "b", "c");
 * SetAlgebra<String> algebra = SetAlgebra.of(set1);
 *
 * Set<String> set2 = Set.of("b", "c", "d");
 * Set<String> union = algebra.union(set2);        // [a, b, c, d]
 * Set<String> intersection = algebra.intersection(set2); // [b, c]
 * Set<String> difference = algebra.difference(set2);     // [a]
 * Set<String> symDiff = algebra.symmetricDifference(set2); // [a, d]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Union/Intersection/Difference: O(n + m) - 并集/交集/差集: O(n + m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface SetAlgebra<E> {

    /**
     * Create a SetAlgebra instance from a set.
     * 从集合创建 SetAlgebra 实例。
     *
     * @param <E> element type | 元素类型
     * @param set the set | 集合
     * @return SetAlgebra instance | SetAlgebra 实例
     */
    static <E> SetAlgebra<E> of(Set<E> set) {
        return new SetAlgebraImpl<>(set);
    }

    /**
     * Get the underlying set.
     * 获取底层集合。
     *
     * @return the set | 集合
     */
    Set<E> getSet();

    /**
     * Compute union with another set.
     * 计算与另一个集合的并集。
     *
     * @param other the other set | 另一个集合
     * @return union of the two sets | 两个集合的并集
     */
    Set<E> union(Set<? extends E> other);

    /**
     * Compute intersection with another set.
     * 计算与另一个集合的交集。
     *
     * @param other the other set | 另一个集合
     * @return intersection of the two sets | 两个集合的交集
     */
    Set<E> intersection(Set<?> other);

    /**
     * Compute difference with another set (this - other).
     * 计算与另一个集合的差集（this - other）。
     *
     * @param other the other set | 另一个集合
     * @return difference of the two sets | 两个集合的差集
     */
    Set<E> difference(Set<?> other);

    /**
     * Compute symmetric difference with another set.
     * 计算与另一个集合的对称差集。
     *
     * @param other the other set | 另一个集合
     * @return symmetric difference of the two sets | 两个集合的对称差集
     */
    Set<E> symmetricDifference(Set<? extends E> other);

    /**
     * Check if this set is a subset of another set.
     * 检查此集合是否为另一个集合的子集。
     *
     * @param other the other set | 另一个集合
     * @return true if this is a subset | 如果是子集则返回 true
     */
    boolean isSubsetOf(Set<?> other);

    /**
     * Check if this set is a superset of another set.
     * 检查此集合是否为另一个集合的超集。
     *
     * @param other the other set | 另一个集合
     * @return true if this is a superset | 如果是超集则返回 true
     */
    boolean isSupersetOf(Set<?> other);

    /**
     * Check if this set is a proper subset of another set.
     * 检查此集合是否为另一个集合的真子集。
     *
     * @param other the other set | 另一个集合
     * @return true if this is a proper subset | 如果是真子集则返回 true
     */
    boolean isProperSubsetOf(Set<?> other);

    /**
     * Check if this set is a proper superset of another set.
     * 检查此集合是否为另一个集合的真超集。
     *
     * @param other the other set | 另一个集合
     * @return true if this is a proper superset | 如果是真超集则返回 true
     */
    boolean isProperSupersetOf(Set<?> other);

    /**
     * Check if this set is disjoint with another set.
     * 检查此集合是否与另一个集合不相交。
     *
     * @param other the other set | 另一个集合
     * @return true if disjoint | 如果不相交则返回 true
     */
    boolean isDisjoint(Set<?> other);

    /**
     * Filter elements matching a predicate.
     * 过滤匹配谓词的元素。
     *
     * @param predicate the predicate | 谓词
     * @return filtered set | 过滤后的集合
     */
    Set<E> filter(Predicate<? super E> predicate);

    /**
     * Default implementation of SetAlgebra.
     * SetAlgebra 的默认实现。
     *
     * @param <E> element type | 元素类型
     */
    class SetAlgebraImpl<E> implements SetAlgebra<E> {
        private final Set<E> set;

        SetAlgebraImpl(Set<E> set) {
            this.set = set != null ? set : Set.of();
        }

        @Override
        public Set<E> getSet() {
            return set;
        }

        @Override
        public Set<E> union(Set<? extends E> other) {
            Set<E> result = new HashSet<>(set);
            if (other != null) {
                result.addAll(other);
            }
            return result;
        }

        @Override
        public Set<E> intersection(Set<?> other) {
            Set<E> result = new HashSet<>();
            if (other != null) {
                for (E element : set) {
                    if (other.contains(element)) {
                        result.add(element);
                    }
                }
            }
            return result;
        }

        @Override
        public Set<E> difference(Set<?> other) {
            Set<E> result = new HashSet<>(set);
            if (other != null) {
                result.removeAll(other);
            }
            return result;
        }

        @Override
        public Set<E> symmetricDifference(Set<? extends E> other) {
            Set<E> result = new HashSet<>(set);
            if (other != null) {
                for (E element : other) {
                    if (!result.remove(element)) {
                        result.add(element);
                    }
                }
            }
            return result;
        }

        @Override
        public boolean isSubsetOf(Set<?> other) {
            if (other == null) {
                return set.isEmpty();
            }
            return other.containsAll(set);
        }

        @Override
        public boolean isSupersetOf(Set<?> other) {
            if (other == null) {
                return true;
            }
            return set.containsAll(other);
        }

        @Override
        public boolean isProperSubsetOf(Set<?> other) {
            if (other == null) {
                return false;
            }
            return other.size() > set.size() && other.containsAll(set);
        }

        @Override
        public boolean isProperSupersetOf(Set<?> other) {
            if (other == null) {
                return !set.isEmpty();
            }
            return set.size() > other.size() && set.containsAll(other);
        }

        @Override
        public boolean isDisjoint(Set<?> other) {
            if (other == null || other.isEmpty()) {
                return true;
            }
            for (E element : set) {
                if (other.contains(element)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Set<E> filter(Predicate<? super E> predicate) {
            Set<E> result = new HashSet<>();
            for (E element : set) {
                if (predicate.test(element)) {
                    result.add(element);
                }
            }
            return result;
        }
    }
}
