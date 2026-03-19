package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Multiset;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConcurrentHashMultiset - Thread-safe Multiset Implementation
 * ConcurrentHashMultiset - 线程安全的多重集合实现
 *
 * <p>A thread-safe multiset implementation using ConcurrentHashMap.</p>
 * <p>使用 ConcurrentHashMap 的线程安全多重集合实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>High concurrency - 高并发</li>
 *   <li>Lock-free reads - 无锁读取</li>
 *   <li>Atomic updates - 原子更新</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConcurrentHashMultiset<String> multiset = ConcurrentHashMultiset.create();
 *
 * // Thread-safe operations - 线程安全操作
 * multiset.add("apple", 3);
 * multiset.add("banana");
 *
 * // Atomic count operations - 原子计数操作
 * int count = multiset.count("apple"); // 3
 *
 * // Can be safely used from multiple threads - 可安全地从多个线程使用
 * ExecutorService executor = Executors.newFixedThreadPool(4);
 * for (int i = 0; i < 100; i++) {
 *     executor.submit(() -> multiset.add("concurrent"));
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add: O(1) average - add: O(1) 平均</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>count: O(1) - count: O(1)</li>
 *   <li>contains: O(1) - contains: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
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
public final class ConcurrentHashMultiset<E> extends AbstractCollection<E> implements Multiset<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ConcurrentHashMap<E, AtomicInteger> elementCounts;

    // ==================== 构造方法 | Constructors ====================

    private ConcurrentHashMultiset() {
        this.elementCounts = new ConcurrentHashMap<>();
    }

    private ConcurrentHashMultiset(int initialCapacity) {
        this.elementCounts = new ConcurrentHashMap<>(initialCapacity);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty ConcurrentHashMultiset.
     * 创建空 ConcurrentHashMultiset。
     *
     * @param <E> element type | 元素类型
     * @return new empty ConcurrentHashMultiset | 新空 ConcurrentHashMultiset
     */
    public static <E> ConcurrentHashMultiset<E> create() {
        return new ConcurrentHashMultiset<>();
    }

    /**
     * Create a ConcurrentHashMultiset with initial capacity.
     * 创建指定初始容量的 ConcurrentHashMultiset。
     *
     * @param <E>             element type | 元素类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new empty ConcurrentHashMultiset | 新空 ConcurrentHashMultiset
     */
    public static <E> ConcurrentHashMultiset<E> create(int initialCapacity) {
        return new ConcurrentHashMultiset<>(initialCapacity);
    }

    /**
     * Create a ConcurrentHashMultiset from elements.
     * 从元素创建 ConcurrentHashMultiset。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return new ConcurrentHashMultiset | 新 ConcurrentHashMultiset
     */
    public static <E> ConcurrentHashMultiset<E> create(Iterable<? extends E> elements) {
        ConcurrentHashMultiset<E> multiset = create();
        for (E element : elements) {
            multiset.add(element);
        }
        return multiset;
    }

    // ==================== Multiset 方法 | Multiset Methods ====================

    @Override
    public int count(Object element) {
        if (element == null) return 0;
        AtomicInteger count = elementCounts.get(element);
        return count != null ? count.get() : 0;
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

        AtomicInteger count = elementCounts.computeIfAbsent(element, k -> new AtomicInteger(0));
        int oldValue;
        int newValue;
        do {
            oldValue = count.get();
            newValue = oldValue + occurrences;
            if (newValue < oldValue) {
                throw new IllegalArgumentException("Overflow adding " + occurrences + " to " + oldValue);
            }
        } while (!count.compareAndSet(oldValue, newValue));

        return oldValue;
    }

    @Override
    public int remove(Object element, int occurrences) {
        if (occurrences < 0) {
            throw new IllegalArgumentException("Occurrences cannot be negative: " + occurrences);
        }
        if (element == null) {
            return 0;
        }

        AtomicInteger count = elementCounts.get(element);
        if (count == null) {
            return 0;
        }

        int oldValue;
        int newValue;
        do {
            oldValue = count.get();
            if (oldValue == 0) {
                return 0;
            }
            newValue = Math.max(0, oldValue - occurrences);
        } while (!count.compareAndSet(oldValue, newValue));

        if (newValue == 0) {
            elementCounts.remove(element, count);
        }

        return oldValue;
    }

    @Override
    public int setCount(E element, int count) {
        Objects.requireNonNull(element);
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative: " + count);
        }

        if (count == 0) {
            AtomicInteger oldCount = elementCounts.remove(element);
            return oldCount != null ? oldCount.get() : 0;
        }

        AtomicInteger atomicCount = elementCounts.computeIfAbsent(element, k -> new AtomicInteger(0));
        return atomicCount.getAndSet(count);
    }

    @Override
    public boolean setCount(E element, int oldCount, int newCount) {
        Objects.requireNonNull(element);
        if (oldCount < 0 || newCount < 0) {
            throw new IllegalArgumentException("Counts cannot be negative");
        }

        AtomicInteger count = elementCounts.get(element);
        if (oldCount == 0) {
            if (count == null) {
                if (newCount > 0) {
                    if (elementCounts.putIfAbsent(element, new AtomicInteger(newCount)) != null) {
                        return false; // Another thread inserted between get and putIfAbsent
                    }
                }
                return true;
            }
            // Key exists - CAS the existing AtomicInteger from 0 to newCount
            if (count.get() == 0) {
                if (newCount == 0) return true;
                return count.compareAndSet(0, newCount);
            }
            return false;
        }

        if (count == null) {
            return false;
        }

        if (count.compareAndSet(oldCount, newCount)) {
            if (newCount == 0) {
                elementCounts.remove(element, count);
            }
            return true;
        }
        return false;
    }

    @Override
    public Set<E> elementSet() {
        return Collections.unmodifiableSet(elementCounts.keySet());
    }

    @Override
    public Set<Entry<E>> entrySet() {
        Set<Entry<E>> entries = new LinkedHashSet<>();
        for (Map.Entry<E, AtomicInteger> entry : elementCounts.entrySet()) {
            int count = entry.getValue().get();
            if (count > 0) {
                entries.add(new SimpleEntry<>(entry.getKey(), count));
            }
        }
        return Collections.unmodifiableSet(entries);
    }

    // ==================== Collection 方法 | Collection Methods ====================

    @Override
    public int size() {
        long size = 0;
        for (AtomicInteger count : elementCounts.values()) {
            size += count.get();
        }
        return (int) Math.min(size, Integer.MAX_VALUE);
    }

    @Override
    public boolean isEmpty() {
        for (AtomicInteger count : elementCounts.values()) {
            if (count.get() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return count(o) > 0;
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
        elementCounts.clear();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Multiset<?> that)) return false;

        if (size() != that.size()) return false;
        for (Entry<E> entry : entrySet()) {
            if (that.count(entry.getElement()) != entry.getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Entry<E> entry : entrySet()) {
            hashCode += Objects.hashCode(entry.getElement()) ^ entry.getCount();
        }
        return hashCode;
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
        sb.append("]");
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    private class MultisetIterator implements Iterator<E> {
        private final Iterator<Map.Entry<E, AtomicInteger>> entryIterator = elementCounts.entrySet().iterator();
        private E currentElement;
        private int remainingCount;

        @Override
        public boolean hasNext() {
            while (remainingCount == 0 && entryIterator.hasNext()) {
                Map.Entry<E, AtomicInteger> entry = entryIterator.next();
                currentElement = entry.getKey();
                remainingCount = entry.getValue().get();
            }
            return remainingCount > 0;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            remainingCount--;
            return currentElement;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove not supported in concurrent iterator");
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
