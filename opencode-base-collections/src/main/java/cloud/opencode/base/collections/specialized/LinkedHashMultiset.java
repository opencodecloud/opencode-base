package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * LinkedHashMultiset - Linked Hash Multiset Implementation
 * LinkedHashMultiset - 链式哈希多重集合实现
 *
 * <p>A multiset that maintains insertion order of elements.</p>
 * <p>保持元素插入顺序的多重集合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Insertion order preservation - 保持插入顺序</li>
 *   <li>O(1) operations - O(1) 操作</li>
 *   <li>Predictable iteration order - 可预测的迭代顺序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LinkedHashMultiset<String> multiset = LinkedHashMultiset.create();
 * multiset.add("cherry", 3);
 * multiset.add("apple", 2);
 * multiset.add("banana");
 *
 * // Iteration in insertion order - 按插入顺序迭代
 * for (String element : multiset.elementSet()) {
 *     System.out.println(element + ": " + multiset.count(element));
 * }
 * // Output: cherry: 3, apple: 2, banana: 1
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(1) - add: O(1)</li>
 *   <li>remove: O(1) - remove: O(1)</li>
 *   <li>count: O(1) - count: O(1)</li>
 *   <li>contains: O(1) - contains: O(1)</li>
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
public final class LinkedHashMultiset<E> extends AbstractMultiset<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<E, Integer> elementCounts;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private LinkedHashMultiset() {
        this.elementCounts = new LinkedHashMap<>();
        this.size = 0;
    }

    private LinkedHashMultiset(int expectedDistinctElements) {
        this.elementCounts = new LinkedHashMap<>(expectedDistinctElements);
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LinkedHashMultiset.
     * 创建空 LinkedHashMultiset。
     *
     * @param <E> element type | 元素类型
     * @return new empty LinkedHashMultiset | 新空 LinkedHashMultiset
     */
    public static <E> LinkedHashMultiset<E> create() {
        return new LinkedHashMultiset<>();
    }

    /**
     * Create a LinkedHashMultiset with expected size.
     * 创建指定预期大小的 LinkedHashMultiset。
     *
     * @param <E>                      element type | 元素类型
     * @param expectedDistinctElements expected number of distinct elements | 预期不同元素数量
     * @return new empty LinkedHashMultiset | 新空 LinkedHashMultiset
     */
    public static <E> LinkedHashMultiset<E> create(int expectedDistinctElements) {
        return new LinkedHashMultiset<>(expectedDistinctElements);
    }

    /**
     * Create a LinkedHashMultiset from elements.
     * 从元素创建 LinkedHashMultiset。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new LinkedHashMultiset | 新 LinkedHashMultiset
     */
    public static <E> LinkedHashMultiset<E> create(Iterable<? extends E> elements) {
        LinkedHashMultiset<E> multiset = create();
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
    public Set<E> elementSet() {
        return Collections.unmodifiableSet(elementCounts.keySet());
    }

    @Override
    public Set<Entry<E>> entrySet() {
        Set<Entry<E>> entries = new LinkedHashSet<>();
        for (Map.Entry<E, Integer> entry : elementCounts.entrySet()) {
            entries.add(new SimpleEntry<>(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableSet(entries);
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
            LinkedHashMultiset.this.remove(currentElement, 1);
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
