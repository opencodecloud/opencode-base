package cloud.opencode.base.collections.immutable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * PersistentSet - Persistent immutable set based on {@link PersistentMap}
 * PersistentSet - 基于 {@link PersistentMap} 的持久化不可变集合
 *
 * <p>All mutation operations ({@code add}, {@code remove}, set algebra) return
 * a new {@code PersistentSet} while sharing structure with the original via
 * the underlying HAMT-based {@link PersistentMap}.</p>
 * <p>所有变更操作（{@code add}、{@code remove}、集合代数）均返回新的
 * {@code PersistentSet}，同时通过底层基于 HAMT 的 {@link PersistentMap}
 * 与原集合共享结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Structural sharing via HAMT - 基于 HAMT 的结构共享</li>
 *   <li>Immutable - 不可变</li>
 *   <li>O(log32 n) add/remove/contains - O(log32 n) 的添加/删除/查询</li>
 *   <li>Set algebra: union, intersection, difference - 集合代数：并集、交集、差集</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create set - 创建集合
 * PersistentSet<String> set = PersistentSet.of("a", "b", "c");
 *
 * // Add element (returns new set) - 添加元素（返回新集合）
 * PersistentSet<String> set2 = set.add("d");
 * // set still has size 3, set2 has size 4
 *
 * // Set algebra - 集合代数
 * PersistentSet<String> other = PersistentSet.of("b", "c", "d");
 * PersistentSet<String> union = set.union(other);       // {a, b, c, d}
 * PersistentSet<String> inter = set.intersection(other); // {b, c}
 * PersistentSet<String> diff  = set.difference(other);   // {a}
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(log32 n) - add: O(log32 n)</li>
 *   <li>remove: O(log32 n) - remove: O(log32 n)</li>
 *   <li>contains: O(log32 n) - contains: O(log32 n)</li>
 *   <li>union/intersection/difference: O(n + m) - union/intersection/difference: O(n + m)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class PersistentSet<E> implements Iterable<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Sentinel value used as map values. | 用作映射值的哨兵值。 */
    private static final Boolean PRESENT = Boolean.TRUE;

    @SuppressWarnings("rawtypes")
    private static final PersistentSet EMPTY = new PersistentSet<>(PersistentMap.empty());

    /** Internal storage backed by PersistentMap. | 由 PersistentMap 支撑的内部存储。 */
    private final PersistentMap<E, Boolean> map;

    private PersistentSet(PersistentMap<E, Boolean> map) {
        this.map = map;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty persistent set.
     * 返回一个空的持久化集合。
     *
     * @param <E> element type | 元素类型
     * @return empty persistent set | 空的持久化集合
     */
    @SuppressWarnings("unchecked")
    public static <E> PersistentSet<E> empty() {
        return (PersistentSet<E>) EMPTY;
    }

    /**
     * Create a persistent set from the given elements.
     * 从给定的元素创建持久化集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return persistent set containing the elements | 包含元素的持久化集合
     */
    @SafeVarargs
    public static <E> PersistentSet<E> of(E... elements) {
        Objects.requireNonNull(elements, "Elements array must not be null");
        PersistentMap<E, Boolean> m = PersistentMap.empty();
        for (E element : elements) {
            m = m.put(element, PRESENT);
        }
        return new PersistentSet<>(m);
    }

    /**
     * Create a persistent set from an iterable.
     * 从可迭代对象创建持久化集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the iterable | 可迭代对象
     * @return persistent set containing the elements | 包含元素的持久化集合
     */
    public static <E> PersistentSet<E> from(Iterable<? extends E> elements) {
        Objects.requireNonNull(elements, "Elements iterable must not be null");
        PersistentMap<E, Boolean> m = PersistentMap.empty();
        for (E element : elements) {
            m = m.put(element, PRESENT);
        }
        return new PersistentSet<>(m);
    }

    // ==================== 核心操作 | Core Operations ====================

    /**
     * Return a new set with the given element added.
     * 返回添加给定元素后的新集合。
     *
     * <p>If the element is already present, returns {@code this}.</p>
     * <p>如果元素已存在，返回 {@code this}。</p>
     *
     * @param element the element to add | 要添加的元素
     * @return a new set containing the element | 包含该元素的新集合
     */
    public PersistentSet<E> add(E element) {
        Objects.requireNonNull(element, "element must not be null");
        PersistentMap<E, Boolean> newMap = map.put(element, PRESENT);
        if (newMap.size() == map.size()) {
            return this;
        }
        return new PersistentSet<>(newMap);
    }

    /**
     * Return a new set with the given element removed.
     * 返回删除给定元素后的新集合。
     *
     * <p>If the element is not present, returns {@code this}.</p>
     * <p>如果元素不存在，返回 {@code this}。</p>
     *
     * @param element the element to remove | 要删除的元素
     * @return a new set without the element | 不包含该元素的新集合
     */
    public PersistentSet<E> remove(E element) {
        Objects.requireNonNull(element, "element must not be null");
        PersistentMap<E, Boolean> newMap = map.remove(element);
        if (newMap.size() == map.size()) {
            return this;
        }
        return new PersistentSet<>(newMap);
    }

    // ==================== 查询操作 | Query Operations ====================

    /**
     * Check if this set contains the given element.
     * 检查此集合是否包含给定元素。
     *
     * @param element the element to search for | 要搜索的元素
     * @return true if the element is present | 如果元素存在则返回 true
     */
    public boolean contains(E element) {
        Objects.requireNonNull(element, "element must not be null");
        return map.containsKey(element);
    }

    /**
     * Return the number of elements in this set.
     * 返回此集合中的元素数量。
     *
     * @return the size | 大小
     */
    public int size() {
        return map.size();
    }

    /**
     * Check if this set is empty.
     * 检查此集合是否为空。
     *
     * @return true if the set is empty | 如果集合为空则返回 true
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    // ==================== 集合代数 | Set Algebra ====================

    /**
     * Return the union of this set and another set.
     * 返回此集合与另一个集合的并集。
     *
     * @param other the other set | 另一个集合
     * @return a new set containing all elements from both sets | 包含两个集合所有元素的新集合
     */
    public PersistentSet<E> union(PersistentSet<E> other) {
        Objects.requireNonNull(other, "Other set must not be null");
        PersistentSet<E> result = this;
        for (E element : other) {
            result = result.add(element);
        }
        return result;
    }

    /**
     * Return the intersection of this set and another set.
     * 返回此集合与另一个集合的交集。
     *
     * @param other the other set | 另一个集合
     * @return a new set containing only elements present in both sets | 仅包含两个集合共有元素的新集合
     */
    public PersistentSet<E> intersection(PersistentSet<E> other) {
        Objects.requireNonNull(other, "Other set must not be null");
        PersistentMap<E, Boolean> result = PersistentMap.empty();
        // Iterate over the smaller set for efficiency
        // 遍历较小的集合以提高效率
        PersistentSet<E> smaller = this.size() <= other.size() ? this : other;
        PersistentSet<E> larger = smaller == this ? other : this;
        for (E element : smaller) {
            if (larger.contains(element)) {
                result = result.put(element, PRESENT);
            }
        }
        return new PersistentSet<>(result);
    }

    /**
     * Return the difference of this set minus another set.
     * 返回此集合减去另一个集合的差集。
     *
     * @param other the other set | 另一个集合
     * @return a new set containing elements in this set but not in the other | 包含在此集合中但不在另一个集合中的元素的新集合
     */
    public PersistentSet<E> difference(PersistentSet<E> other) {
        Objects.requireNonNull(other, "Other set must not be null");
        PersistentSet<E> result = this;
        for (E element : other) {
            result = result.remove(element);
        }
        return result;
    }

    // ==================== 迭代与转换 | Iteration & Conversion ====================

    /**
     * Return an iterator over the elements of this set.
     * 返回此集合元素上的迭代器。
     *
     * @return an iterator | 迭代器
     */
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Return a sequential stream over the elements.
     * 返回元素上的顺序流。
     *
     * @return a stream | 流
     */
    public Stream<E> stream() {
        return map.keySet().stream();
    }

    /**
     * Convert this persistent set to a JDK {@link Set}.
     * 将此持久化集合转换为 JDK {@link Set}。
     *
     * @return an unmodifiable set containing all elements | 包含所有元素的不可修改集合
     */
    public Set<E> toSet() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(map.keySet()));
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersistentSet<?> other)) {
            return false;
        }
        if (this.size() != other.size()) {
            return false;
        }
        return this.toSet().equals(other.toSet());
    }

    @Override
    public int hashCode() {
        return toSet().hashCode();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "PersistentSet[", "]");
        for (E element : this) {
            joiner.add(String.valueOf(element));
        }
        return joiner.toString();
    }
}
