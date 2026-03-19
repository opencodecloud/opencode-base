package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * TreeMultiset - Tree-based Multiset Implementation
 * TreeMultiset - 基于树的多重集合实现
 *
 * <p>A multiset that stores elements in sorted order using a tree structure.</p>
 * <p>使用树结构按排序顺序存储元素的多重集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sorted elements - 排序元素</li>
 *   <li>Natural or custom ordering - 自然或自定义排序</li>
 *   <li>NavigableSet view - 可导航集合视图</li>
 *   <li>O(log n) operations - O(log n) 操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Natural ordering - 自然排序
 * TreeMultiset<String> multiset = TreeMultiset.create();
 * multiset.add("banana", 2);
 * multiset.add("apple", 3);
 * multiset.add("cherry");
 *
 * // Iteration in sorted order - 按排序顺序迭代
 * for (String element : multiset.elementSet()) {
 *     System.out.println(element + ": " + multiset.count(element));
 * }
 * // Output: apple: 3, banana: 2, cherry: 1
 *
 * // Custom comparator - 自定义比较器
 * TreeMultiset<String> reverseMultiset = TreeMultiset.create(Comparator.reverseOrder());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(log n) - add: O(log n)</li>
 *   <li>remove: O(log n) - remove: O(log n)</li>
 *   <li>count: O(log n) - count: O(log n)</li>
 *   <li>contains: O(log n) - contains: O(log n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class TreeMultiset<E> extends AbstractMultiset<E> implements NavigableMultiset<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableMap<E, Integer> elementCounts;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private TreeMultiset(Comparator<? super E> comparator) {
        this.elementCounts = comparator != null ? new TreeMap<>(comparator) : new TreeMap<>();
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty TreeMultiset with natural ordering.
     * 创建自然排序的空 TreeMultiset。
     *
     * @param <E> element type | 元素类型
     * @return new empty TreeMultiset | 新空 TreeMultiset
     */
    public static <E extends Comparable<? super E>> TreeMultiset<E> create() {
        return new TreeMultiset<>(null);
    }

    /**
     * Create an empty TreeMultiset with custom comparator.
     * 创建自定义比较器的空 TreeMultiset。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new empty TreeMultiset | 新空 TreeMultiset
     */
    public static <E> TreeMultiset<E> create(Comparator<? super E> comparator) {
        return new TreeMultiset<>(comparator);
    }

    /**
     * Create a TreeMultiset from elements with natural ordering.
     * 从元素创建自然排序的 TreeMultiset。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new TreeMultiset | 新 TreeMultiset
     */
    public static <E extends Comparable<? super E>> TreeMultiset<E> create(Iterable<? extends E> elements) {
        TreeMultiset<E> multiset = create();
        for (E element : elements) {
            multiset.add(element);
        }
        return multiset;
    }

    // ==================== Multiset 方法 | Multiset Methods ====================

    @Override
    public int count(Object element) {
        if (element == null) return 0;
        Integer count = elementCounts.get(element);
        return count != null ? count : 0;
    }

    @Override
    public int add(E element, int occurrences) {
        Objects.requireNonNull(element, "Element cannot be null");
        if (occurrences < 0) {
            throw new IllegalArgumentException("Occurrences cannot be negative: " + occurrences);
        }
        if (occurrences == 0) {
            return count(element);
        }

        int oldCount = count(element);
        int newCount = oldCount + occurrences;
        if (newCount < oldCount) {
            throw new IllegalArgumentException("Overflow adding " + occurrences + " to " + oldCount);
        }

        elementCounts.put(element, newCount);
        size += occurrences;
        return oldCount;
    }

    @Override
    public int remove(Object element, int occurrences) {
        if (element == null || occurrences < 0) {
            return 0;
        }

        int oldCount = count(element);
        if (oldCount == 0) {
            return 0;
        }

        int newCount = Math.max(0, oldCount - occurrences);
        if (newCount == 0) {
            elementCounts.remove(element);
            size -= oldCount;
        } else {
            @SuppressWarnings("unchecked")
            E e = (E) element;
            elementCounts.put(e, newCount);
            size -= (oldCount - newCount);
        }
        return oldCount;
    }

    @Override
    public int setCount(E element, int count) {
        Objects.requireNonNull(element);
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }

        int oldCount = count(element);
        if (count == 0) {
            elementCounts.remove(element);
        } else {
            elementCounts.put(element, count);
        }
        size += (count - oldCount);
        return oldCount;
    }

    @Override
    public boolean setCount(E element, int oldCount, int newCount) {
        Objects.requireNonNull(element);
        if (oldCount < 0 || newCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        int currentCount = count(element);
        if (currentCount != oldCount) {
            return false;
        }

        setCount(element, newCount);
        return true;
    }

    @Override
    public NavigableSet<E> elementSet() {
        return Collections.unmodifiableNavigableSet(elementCounts.navigableKeySet());
    }

    @Override
    public Set<Entry<E>> entrySet() {
        Set<Entry<E>> entries = new LinkedHashSet<>();
        for (Map.Entry<E, Integer> entry : elementCounts.entrySet()) {
            entries.add(new SimpleEntry<>(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableSet(entries);
    }

    // ==================== NavigableMultiset 方法 | NavigableMultiset Methods ====================

    @Override
    public E first() {
        return elementCounts.firstKey();
    }

    @Override
    public E last() {
        return elementCounts.lastKey();
    }

    @Override
    public E lower(E e) {
        return elementCounts.lowerKey(e);
    }

    @Override
    public E higher(E e) {
        return elementCounts.higherKey(e);
    }

    @Override
    public E floor(E e) {
        return elementCounts.floorKey(e);
    }

    @Override
    public E ceiling(E e) {
        return elementCounts.ceilingKey(e);
    }

    @Override
    public Entry<E> pollFirstEntry() {
        Map.Entry<E, Integer> entry = elementCounts.pollFirstEntry();
        if (entry == null) return null;
        size -= entry.getValue();
        return new SimpleEntry<>(entry.getKey(), entry.getValue());
    }

    @Override
    public Entry<E> pollLastEntry() {
        Map.Entry<E, Integer> entry = elementCounts.pollLastEntry();
        if (entry == null) return null;
        size -= entry.getValue();
        return new SimpleEntry<>(entry.getKey(), entry.getValue());
    }

    @Override
    public Comparator<? super E> comparator() {
        return elementCounts.comparator();
    }

    // ==================== Collection 方法 | Collection Methods ====================

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return count(o) > 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new MultisetIterator();
    }

    @Override
    public void clear() {
        elementCounts.clear();
        size = 0;
    }

    // ==================== 内部类 | Internal Classes ====================

    private class MultisetIterator implements Iterator<E> {
        private final Iterator<Map.Entry<E, Integer>> entryIterator = elementCounts.entrySet().iterator();
        private E currentElement;
        private int remainingCount;
        private boolean canRemove = false;

        @Override
        public boolean hasNext() {
            return remainingCount > 0 || entryIterator.hasNext();
        }

        @Override
        public E next() {
            if (remainingCount == 0) {
                if (!entryIterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                Map.Entry<E, Integer> entry = entryIterator.next();
                currentElement = entry.getKey();
                remainingCount = entry.getValue();
            }
            remainingCount--;
            canRemove = true;
            return currentElement;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            TreeMultiset.this.remove(currentElement, 1);
            canRemove = false;
        }
    }

    private record SimpleEntry<E>(E element, int count) implements Entry<E> {
        @Override
        public E getElement() {
            return element;
        }

        @Override
        public int getCount() {
            return count;
        }
    }
}
