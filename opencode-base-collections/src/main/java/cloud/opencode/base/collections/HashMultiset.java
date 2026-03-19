package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * HashMultiset - Hash-based Multiset Implementation
 * HashMultiset - 基于哈希的多重集实现
 *
 * <p>A hash-based implementation of Multiset that maintains element counts
 * using an internal HashMap.</p>
 * <p>基于哈希的 Multiset 实现，使用内部 HashMap 维护元素计数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) count operations - O(1) 计数操作</li>
 *   <li>Null elements allowed - 允许空元素</li>
 *   <li>Serializable - 可序列化</li>
 *   <li>Live views - 实时视图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty Multiset - 创建空 Multiset
 * HashMultiset<String> multiset = HashMultiset.create();
 *
 * // Create with initial elements - 创建带初始元素
 * HashMultiset<String> multiset = HashMultiset.create(Arrays.asList("a", "a", "b"));
 *
 * // Create with initial capacity - 创建指定容量
 * HashMultiset<String> multiset = HashMultiset.create(16);
 *
 * // Operations - 操作
 * multiset.add("apple", 3);
 * multiset.count("apple");  // 3
 * multiset.setCount("apple", 5);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(1) average - add: O(1) 平均</li>
 *   <li>count: O(1) average - count: O(1) 平均</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>setCount: O(1) average - setCount: O(1) 平均</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (allows null elements) - 空值安全: 是（允许空元素）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public class HashMultiset<E> extends AbstractCollection<E> implements Multiset<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<E, Integer> countMap;
    private int totalCount;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param initialCapacity initial capacity | 初始容量
     */
    private HashMultiset(int initialCapacity) {
        this.countMap = new HashMap<>(initialCapacity);
        this.totalCount = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashMultiset.
     * 创建空 HashMultiset。
     *
     * @param <E> element type | 元素类型
     * @return new HashMultiset | 新的 HashMultiset
     */
    public static <E> HashMultiset<E> create() {
        return new HashMultiset<>(16);
    }

    /**
     * Create an empty HashMultiset with initial capacity.
     * 创建指定容量的空 HashMultiset。
     *
     * @param <E>             element type | 元素类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new HashMultiset | 新的 HashMultiset
     */
    public static <E> HashMultiset<E> create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw OpenCollectionException.illegalCapacity(initialCapacity);
        }
        return new HashMultiset<>(initialCapacity);
    }

    /**
     * Create a HashMultiset from an iterable.
     * 从可迭代对象创建 HashMultiset。
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new HashMultiset | 新的 HashMultiset
     */
    public static <E> HashMultiset<E> create(Iterable<? extends E> elements) {
        HashMultiset<E> multiset = create();
        if (elements != null) {
            for (E e : elements) {
                multiset.add(e);
            }
        }
        return multiset;
    }

    /**
     * Create a HashMultiset from varargs.
     * 从可变参数创建 HashMultiset。
     *
     * @param <E>      element type | 元素类型
     * @param elements elements | 元素
     * @return new HashMultiset | 新的 HashMultiset
     */
    @SafeVarargs
    public static <E> HashMultiset<E> create(E... elements) {
        HashMultiset<E> multiset = create(elements.length);
        Collections.addAll(multiset, elements);
        return multiset;
    }

    // ==================== Multiset 实现 | Multiset Implementation ====================

    @Override
    public int count(Object element) {
        Integer count = countMap.get(element);
        return count == null ? 0 : count;
    }

    @Override
    public int add(E element, int occurrences) {
        if (occurrences < 0) {
            throw new IllegalArgumentException("Occurrences cannot be negative: " + occurrences);
        }
        if (occurrences == 0) {
            return count(element);
        }

        int oldCount = count(element);
        long newCount = (long) oldCount + occurrences;
        if (newCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Count overflow");
        }

        countMap.put(element, (int) newCount);
        totalCount += occurrences;
        return oldCount;
    }

    @Override
    public int remove(Object element, int occurrences) {
        if (occurrences < 0) {
            throw new IllegalArgumentException("Occurrences cannot be negative: " + occurrences);
        }
        if (occurrences == 0) {
            return count(element);
        }

        int oldCount = count(element);
        if (oldCount == 0) {
            return 0;
        }

        int newCount = Math.max(0, oldCount - occurrences);
        if (newCount == 0) {
            countMap.remove(element);
            totalCount -= oldCount;
        } else {
            @SuppressWarnings("unchecked")
            E e = (E) element;
            countMap.put(e, newCount);
            totalCount -= occurrences;
        }
        return oldCount;
    }

    @Override
    public int setCount(E element, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }

        int oldCount = count(element);
        if (count == 0) {
            countMap.remove(element);
        } else {
            countMap.put(element, count);
        }
        totalCount += (count - oldCount);
        return oldCount;
    }

    @Override
    public boolean setCount(E element, int oldCount, int newCount) {
        if (oldCount < 0 || newCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        int actualCount = count(element);
        if (actualCount != oldCount) {
            return false;
        }

        setCount(element, newCount);
        return true;
    }

    @Override
    public Set<E> elementSet() {
        return new ElementSet();
    }

    @Override
    public Set<Entry<E>> entrySet() {
        return new EntrySet();
    }

    // ==================== Collection 实现 | Collection Implementation ====================

    @Override
    public int size() {
        return totalCount;
    }

    @Override
    public boolean isEmpty() {
        return totalCount == 0;
    }

    @Override
    public boolean contains(Object element) {
        return countMap.containsKey(element);
    }

    @Override
    public boolean add(E element) {
        add(element, 1);
        return true;
    }

    @Override
    public boolean remove(Object element) {
        return remove(element, 1) > 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new MultisetIterator();
    }

    @Override
    public void clear() {
        countMap.clear();
        totalCount = 0;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Multiset<?> that)) return false;
        if (size() != that.size()) return false;

        for (Entry<E> entry : entrySet()) {
            if (entry.getCount() != that.count(entry.getElement())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Entry<E> entry : entrySet()) {
            E e = entry.getElement();
            hash += (e == null ? 0 : e.hashCode()) ^ entry.getCount();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Entry<E> entry : entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getElement()).append(" x ").append(entry.getCount());
            first = false;
        }
        return sb.append("]").toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Element set view
     */
    private class ElementSet extends AbstractSet<E> {
        @Override
        public Iterator<E> iterator() {
            return countMap.keySet().iterator();
        }

        @Override
        public int size() {
            return countMap.size();
        }

        @Override
        public boolean contains(Object o) {
            return countMap.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            int oldCount = count(o);
            if (oldCount > 0) {
                countMap.remove(o);
                totalCount -= oldCount;
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            HashMultiset.this.clear();
        }
    }

    /**
     * Entry set view
     */
    private class EntrySet extends AbstractSet<Entry<E>> {
        @Override
        public Iterator<Entry<E>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return countMap.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry<?> entry)) {
                return false;
            }
            return count(entry.getElement()) == entry.getCount() && entry.getCount() > 0;
        }

        @Override
        public void clear() {
            HashMultiset.this.clear();
        }
    }

    /**
     * Multiset iterator - iterates over all occurrences
     */
    private class MultisetIterator implements Iterator<E> {
        private final Iterator<Map.Entry<E, Integer>> mapIterator = countMap.entrySet().iterator();
        private Map.Entry<E, Integer> currentEntry;
        private int remaining;
        private boolean canRemove;

        @Override
        public boolean hasNext() {
            return remaining > 0 || mapIterator.hasNext();
        }

        @Override
        public E next() {
            if (remaining == 0) {
                if (!mapIterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                currentEntry = mapIterator.next();
                remaining = currentEntry.getValue();
            }
            remaining--;
            canRemove = true;
            return currentEntry.getKey();
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            canRemove = false;
            int newCount = currentEntry.getValue() - 1;
            if (newCount == 0) {
                mapIterator.remove();
            } else {
                currentEntry.setValue(newCount);
            }
            totalCount--;
        }
    }

    /**
     * Entry iterator
     */
    private class EntryIterator implements Iterator<Entry<E>> {
        private final Iterator<Map.Entry<E, Integer>> mapIterator = countMap.entrySet().iterator();
        private Map.Entry<E, Integer> lastEntry;

        @Override
        public boolean hasNext() {
            return mapIterator.hasNext();
        }

        @Override
        public Entry<E> next() {
            lastEntry = mapIterator.next();
            return new MultisetEntry<>(lastEntry.getKey(), lastEntry.getValue());
        }

        @Override
        public void remove() {
            if (lastEntry == null) {
                throw new IllegalStateException();
            }
            int count = lastEntry.getValue();
            mapIterator.remove();
            totalCount -= count;
            lastEntry = null;
        }
    }

    /**
     * Multiset entry implementation
     */
    private record MultisetEntry<E>(E element, int count) implements Entry<E> {

        @Override
        public E getElement() {
            return element;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry<?> that)) {
                return false;
            }
            return count == that.getCount() && Objects.equals(element, that.getElement());
        }

        @Override
        public int hashCode() {
            return (element == null ? 0 : element.hashCode()) ^ count;
        }

        @Override
        public String toString() {
            return element + " x " + count;
        }
    }
}
