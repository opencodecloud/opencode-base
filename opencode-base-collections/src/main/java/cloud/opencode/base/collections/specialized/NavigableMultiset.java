package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;

import java.util.Comparator;

/**
 * NavigableMultiset - Navigable Multiset Interface
 * NavigableMultiset - 可导航多重集合接口
 *
 * <p>A sorted multiset that provides navigation methods for accessing elements
 * in sorted order.</p>
 * <p>提供按排序顺序访问元素的导航方法的排序多重集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sorted element access - 排序元素访问</li>
 *   <li>Navigation operations (lower, higher, floor, ceiling) - 导航操作</li>
 *   <li>Polling operations - 轮询操作</li>
 *   <li>Comparator access - 比较器访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NavigableMultiset<String> multiset = TreeMultiset.create();
 * multiset.add("banana", 2);
 * multiset.add("apple", 3);
 * multiset.add("cherry");
 *
 * String first = multiset.first();   // "apple"
 * String last = multiset.last();     // "cherry"
 * String lower = multiset.lower("banana"); // "apple"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (interface, implementation-dependent) - 否（接口，取决于实现）</li>
 *   <li>Null-safe: No - 否</li>
 * </ul>
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface NavigableMultiset<E> extends Multiset<E> {

    /**
     * Return the first (lowest) element.
     * 返回第一个（最小）元素。
     *
     * @return the first element | 第一个元素
     * @throws java.util.NoSuchElementException if empty | 如果为空则抛出异常
     */
    E first();

    /**
     * Return the last (highest) element.
     * 返回最后一个（最大）元素。
     *
     * @return the last element | 最后一个元素
     * @throws java.util.NoSuchElementException if empty | 如果为空则抛出异常
     */
    E last();

    /**
     * Return the greatest element strictly less than the given element.
     * 返回严格小于给定元素的最大元素。
     *
     * @param e the element | 元素
     * @return the lower element, or null | 更小的元素，或 null
     */
    E lower(E e);

    /**
     * Return the least element strictly greater than the given element.
     * 返回严格大于给定元素的最小元素。
     *
     * @param e the element | 元素
     * @return the higher element, or null | 更大的元素，或 null
     */
    E higher(E e);

    /**
     * Return the greatest element less than or equal to the given element.
     * 返回小于或等于给定元素的最大元素。
     *
     * @param e the element | 元素
     * @return the floor element, or null | floor 元素，或 null
     */
    E floor(E e);

    /**
     * Return the least element greater than or equal to the given element.
     * 返回大于或等于给定元素的最小元素。
     *
     * @param e the element | 元素
     * @return the ceiling element, or null | ceiling 元素，或 null
     */
    E ceiling(E e);

    /**
     * Remove and return the first entry, or null if empty.
     * 移除并返回第一个条目，如果为空则返回 null。
     *
     * @return the first entry, or null | 第一个条目，或 null
     */
    Entry<E> pollFirstEntry();

    /**
     * Remove and return the last entry, or null if empty.
     * 移除并返回最后一个条目，如果为空则返回 null。
     *
     * @return the last entry, or null | 最后一个条目，或 null
     */
    Entry<E> pollLastEntry();

    /**
     * Return the comparator used to order elements.
     * 返回用于排序元素的比较器。
     *
     * @return the comparator, or null for natural ordering | 比较器，自然排序则为 null
     */
    Comparator<? super E> comparator();
}
