package cloud.opencode.base.collections.tree;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * SkipList - Skip List Implementation
 * SkipList - 跳表实现
 *
 * <p>A probabilistic data structure providing O(log n) search, insert, and delete.</p>
 * <p>提供 O(log n) 搜索、插入和删除的概率数据结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(log n) operations - O(log n) 操作</li>
 *   <li>Sorted order - 排序顺序</li>
 *   <li>Range queries - 范围查询</li>
 *   <li>Simpler than balanced trees - 比平衡树简单</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SkipList<Integer, String> skipList = SkipList.create();
 * skipList.put(1, "one");
 * skipList.put(5, "five");
 * skipList.put(3, "three");
 *
 * String value = skipList.get(3);  // "three"
 *
 * // Iteration in sorted order - 按排序顺序迭代
 * for (SkipList.Entry<Integer, String> entry : skipList) {
 *     System.out.println(entry.getKey() + ": " + entry.getValue());
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>put: O(log n) average - put: O(log n) 平均</li>
 *   <li>get: O(log n) average - get: O(log n) 平均</li>
 *   <li>remove: O(log n) average - remove: O(log n) 平均</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (key must not be null) - 否（键不能为null）</li>
 * </ul>
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class SkipList<K, V> implements Iterable<SkipList.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_LEVEL = 32;
    private static final double P = 0.5;

    private final Comparator<? super K> comparator;
    private final SkipListNode<K, V> head;
    private int level;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private SkipList(Comparator<? super K> comparator) {
        this.comparator = comparator;
        this.head = new SkipListNode<>(null, null, MAX_LEVEL);
        this.level = 0;
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty SkipList with natural ordering.
     * 创建自然排序的空 SkipList。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new empty SkipList | 新空 SkipList
     */
    public static <K extends Comparable<? super K>, V> SkipList<K, V> create() {
        return new SkipList<>(null);
    }

    /**
     * Create an empty SkipList with custom comparator.
     * 创建自定义比较器的空 SkipList。
     *
     * @param <K>        key type | 键类型
     * @param <V>        value type | 值类型
     * @param comparator the comparator | 比较器
     * @return new empty SkipList | 新空 SkipList
     */
    public static <K, V> SkipList<K, V> create(Comparator<? super K> comparator) {
        return new SkipList<>(comparator);
    }

    // ==================== 操作方法 | Operation Methods ====================

    /**
     * Put a key-value pair.
     * 放入键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return the old value or null | 旧值或 null
     */
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");

        SkipListNode<K, V>[] update = new SkipListNode[MAX_LEVEL];
        SkipListNode<K, V> current = head;

        // Find the position
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];

        // Update existing key
        if (current != null && compare(current.key, key) == 0) {
            V oldValue = current.value;
            current.value = value;
            return oldValue;
        }

        // Insert new node
        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level + 1; i <= newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        SkipListNode<K, V> newNode = new SkipListNode<>(key, value, newLevel + 1);
        for (int i = 0; i <= newLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }

        size++;
        return null;
    }

    /**
     * Get the value for a key.
     * 获取键对应的值。
     *
     * @param key the key | 键
     * @return the value or null | 值或 null
     */
    public V get(K key) {
        Objects.requireNonNull(key);
        SkipListNode<K, V> node = findNode(key);
        return node != null ? node.value : null;
    }

    /**
     * Check if the key exists.
     * 检查键是否存在。
     *
     * @param key the key | 键
     * @return true if exists | 如果存在则返回 true
     */
    public boolean containsKey(K key) {
        Objects.requireNonNull(key);
        return findNode(key) != null;
    }

    /**
     * Remove a key.
     * 移除键。
     *
     * @param key the key | 键
     * @return the old value or null | 旧值或 null
     */
    @SuppressWarnings("unchecked")
    public V remove(K key) {
        Objects.requireNonNull(key);

        SkipListNode<K, V>[] update = new SkipListNode[MAX_LEVEL];
        SkipListNode<K, V> current = head;

        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];

        if (current != null && compare(current.key, key) == 0) {
            for (int i = 0; i <= level; i++) {
                if (update[i].forward[i] != current) {
                    break;
                }
                update[i].forward[i] = current.forward[i];
            }

            while (level > 0 && head.forward[level] == null) {
                level--;
            }

            size--;
            return current.value;
        }

        return null;
    }

    /**
     * Return the size of this skip list.
     * 返回此跳表的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this skip list is empty.
     * 检查此跳表是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear this skip list.
     * 清空此跳表。
     */
    public void clear() {
        for (int i = 0; i <= level; i++) {
            head.forward[i] = null;
        }
        level = 0;
        size = 0;
    }

    /**
     * Get the first (smallest) key.
     * 获取第一个（最小）键。
     *
     * @return the first key | 第一个键
     */
    public K firstKey() {
        if (isEmpty()) throw new NoSuchElementException();
        return head.forward[0].key;
    }

    /**
     * Get the last (largest) key.
     * 获取最后一个（最大）键。
     *
     * @return the last key | 最后一个键
     */
    public K lastKey() {
        if (isEmpty()) throw new NoSuchElementException();
        SkipListNode<K, V> current = head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null) {
                current = current.forward[i];
            }
        }
        return current.key;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return new SkipListIterator();
    }

    // ==================== 私有方法 | Private Methods ====================

    private SkipListNode<K, V> findNode(K key) {
        SkipListNode<K, V> current = head;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && compare(current.forward[i].key, key) < 0) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        if (current != null && compare(current.key, key) == 0) {
            return current;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    private int randomLevel() {
        int lvl = 0;
        while (java.util.concurrent.ThreadLocalRandom.current().nextDouble() < P && lvl < MAX_LEVEL - 1) {
            lvl++;
        }
        return lvl;
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Entry interface.
     * 条目接口。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public interface Entry<K, V> {
        K getKey();
        V getValue();
    }

    private static class SkipListNode<K, V> implements Entry<K, V>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        final K key;
        V value;
        final SkipListNode<K, V>[] forward;

        @SuppressWarnings("unchecked")
        SkipListNode(K key, V value, int level) {
            this.key = key;
            this.value = value;
            this.forward = new SkipListNode[level];
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }
    }

    private class SkipListIterator implements Iterator<Entry<K, V>> {
        private SkipListNode<K, V> current = head.forward[0];

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            SkipListNode<K, V> result = current;
            current = current.forward[0];
            return result;
        }
    }
}
