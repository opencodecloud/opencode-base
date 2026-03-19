package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.Set;

/**
 * Multiset - Collection that counts element occurrences
 * Multiset - 计数元素出现次数的集合
 *
 * <p>A collection that supports order-independent equality, like Set, but may have
 * duplicate elements. Also known as a bag or multiset.</p>
 * <p>支持与顺序无关的相等性的集合，类似于 Set，但可以有重复元素。也称为袋或多重集。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Count element occurrences - 计算元素出现次数</li>
 *   <li>Add/remove multiple occurrences - 添加/移除多个出现</li>
 *   <li>Set element count directly - 直接设置元素计数</li>
 *   <li>Element set view (distinct elements) - 元素集合视图（去重元素）</li>
 *   <li>Entry set view (element with count) - 条目集合视图（带计数的元素）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create Multiset - 创建 Multiset
 * Multiset<String> multiset = HashMultiset.create();
 *
 * // Add elements - 添加元素
 * multiset.add("apple");
 * multiset.add("apple", 3);  // add 3 more apples
 *
 * // Count elements - 计算元素
 * int count = multiset.count("apple");  // 4
 *
 * // Set count directly - 直接设置计数
 * multiset.setCount("banana", 5);
 *
 * // Get distinct elements - 获取去重元素
 * Set<String> elements = multiset.elementSet();
 *
 * // Iterate with counts - 带计数迭代
 * for (Multiset.Entry<String> entry : multiset.entrySet()) {
 *     System.out.println(entry.getElement() + ": " + entry.getCount());
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(1) for hash-based - add: O(1) 基于哈希</li>
 *   <li>count: O(1) for hash-based - count: O(1) 基于哈希</li>
 *   <li>remove: O(1) for hash-based - remove: O(1) 基于哈希</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface Multiset<E> extends Collection<E> {

    // ==================== 计数操作 | Count Operations ====================

    /**
     * Return the count of the specified element.
     * 返回指定元素的计数。
     *
     * @param element the element to count | 要计数的元素
     * @return the count (0 if not present) | 计数（不存在则为 0）
     */
    int count(Object element);

    /**
     * Add a number of occurrences of an element.
     * 添加元素的多个出现。
     *
     * @param element     the element to add | 要添加的元素
     * @param occurrences number of occurrences to add | 要添加的出现次数
     * @return previous count | 之前的计数
     * @throws IllegalArgumentException if occurrences is negative | 如果出现次数为负
     */
    int add(E element, int occurrences);

    /**
     * Remove a number of occurrences of an element.
     * 移除元素的多个出现。
     *
     * @param element     the element to remove | 要移除的元素
     * @param occurrences number of occurrences to remove | 要移除的出现次数
     * @return previous count | 之前的计数
     * @throws IllegalArgumentException if occurrences is negative | 如果出现次数为负
     */
    int remove(Object element, int occurrences);

    /**
     * Set the count of an element.
     * 设置元素的计数。
     *
     * @param element the element | 元素
     * @param count   the new count | 新计数
     * @return previous count | 之前的计数
     * @throws IllegalArgumentException if count is negative | 如果计数为负
     */
    int setCount(E element, int count);

    /**
     * Conditionally set the count of an element.
     * 条件性地设置元素的计数。
     *
     * @param element  the element | 元素
     * @param oldCount expected current count | 期望的当前计数
     * @param newCount the new count | 新计数
     * @return true if count was changed | 如果计数被更改则返回 true
     * @throws IllegalArgumentException if counts are negative | 如果计数为负
     */
    boolean setCount(E element, int oldCount, int newCount);

    // ==================== 集合视图 | Set Views ====================

    /**
     * Return the set of distinct elements.
     * 返回去重元素的集合。
     *
     * @return element set | 元素集合
     */
    Set<E> elementSet();

    /**
     * Return the set of entries (element with count).
     * 返回条目集合（带计数的元素）。
     *
     * @return entry set | 条目集合
     */
    Set<Entry<E>> entrySet();

    // ==================== Collection 重写 | Collection Overrides ====================

    /**
     * Return the total count of all elements.
     * 返回所有元素的总计数。
     *
     * @return total size | 总大小
     */
    @Override
    int size();

    /**
     * Check if multiset contains the element.
     * 检查多重集是否包含元素。
     *
     * @param element the element | 元素
     * @return true if contains | 如果包含则返回 true
     */
    @Override
    boolean contains(Object element);

    /**
     * Add one occurrence of the element.
     * 添加元素的一个出现。
     *
     * @param element the element | 元素
     * @return always true | 始终为 true
     */
    @Override
    boolean add(E element);

    /**
     * Remove one occurrence of the element.
     * 移除元素的一个出现。
     *
     * @param element the element | 元素
     * @return true if element was present | 如果元素存在则返回 true
     */
    @Override
    boolean remove(Object element);

    // ==================== 条目接口 | Entry Interface ====================

    /**
     * Entry - Element with occurrence count
     * Entry - 带出现次数的元素
     *
     * @param <E> element type | 元素类型
     */
    interface Entry<E> {

        /**
         * Return the element.
         * 返回元素。
         *
         * @return element | 元素
         */
        E getElement();

        /**
         * Return the count.
         * 返回计数。
         *
         * @return count | 计数
         */
        int getCount();
    }
}
