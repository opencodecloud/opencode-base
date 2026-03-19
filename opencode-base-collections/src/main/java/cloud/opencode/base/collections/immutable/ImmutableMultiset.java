package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableMultiset - Immutable Multiset (Bag) Implementation
 * ImmutableMultiset - 不可变多重集（包）实现
 *
 * <p>A collection that supports order-independent equality and allows duplicate elements,
 * tracking the count of each element. Cannot be modified after creation.</p>
 * <p>支持与顺序无关的相等性并允许重复元素的集合，跟踪每个元素的计数。创建后不能修改。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>Element counting - 元素计数</li>
 *   <li>O(1) count operations - O(1) 计数操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from elements - 从元素创建
 * ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "c", "b", "a");
 * int count = multiset.count("a"); // Returns 3 - 返回 3
 *
 * // Create from collection - 从集合创建
 * ImmutableMultiset<String> multiset = ImmutableMultiset.copyOf(Arrays.asList("x", "x", "y"));
 *
 * // Use builder - 使用构建器
 * ImmutableMultiset<String> multiset = ImmutableMultiset.<String>builder()
 *     .add("a")
 *     .addAll(Arrays.asList("b", "c"))
 *     .setCount("a", 3)
 *     .build();
 *
 * // Query operations - 查询操作
 * Set<String> elementSet = multiset.elementSet(); // Unique elements - 唯一元素
 * int totalSize = multiset.size(); // Total count of all elements - 所有元素的总数
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>count: O(1) - count: O(1)</li>
 *   <li>contains: O(1) - contains: O(1)</li>
 *   <li>add/remove: Not supported (immutable) - add/remove: 不支持（不可变）</li>
 *   <li>iteration: O(n) where n is total size - iteration: O(n)，其中n是总大小</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (nulls not allowed) - 空值安全: 是（不允许空值）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ImmutableMultiset<E> extends AbstractCollection<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableMultiset<?> EMPTY = new ImmutableMultiset<>(Map.of());

    private final Map<E, Integer> elementCounts;
    private final int size;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param elementCounts the element counts map | 元素计数映射
     */
    private ImmutableMultiset(Map<E, Integer> elementCounts) {
        this.elementCounts = elementCounts;
        this.size = elementCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable multiset.
     * 返回空不可变多重集。
     *
     * @param <E> element type | 元素类型
     * @return empty immutable multiset | 空不可变多重集
     */
    @SuppressWarnings("unchecked")
    public static <E> ImmutableMultiset<E> of() {
        return (ImmutableMultiset<E>) EMPTY;
    }

    /**
     * Return an immutable multiset containing the given element.
     * 返回包含给定元素的不可变多重集。
     *
     * @param <E> element type | 元素类型
     * @param e1  the element | 元素
     * @return immutable multiset | 不可变多重集
     */
    public static <E> ImmutableMultiset<E> of(E e1) {
        return new ImmutableMultiset<>(Map.of(Objects.requireNonNull(e1), 1));
    }

    /**
     * Return an immutable multiset containing the given elements.
     * 返回包含给定元素的不可变多重集。
     *
     * @param <E> element type | 元素类型
     * @param e1  first element | 第一个元素
     * @param e2  second element | 第二个元素
     * @return immutable multiset | 不可变多重集
     */
    public static <E> ImmutableMultiset<E> of(E e1, E e2) {
        Objects.requireNonNull(e1);
        Objects.requireNonNull(e2);
        if (e1.equals(e2)) {
            return new ImmutableMultiset<>(Map.of(e1, 2));
        }
        return new ImmutableMultiset<>(Map.of(e1, 1, e2, 1));
    }

    /**
     * Return an immutable multiset containing the given elements.
     * 返回包含给定元素的不可变多重集。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable multiset | 不可变多重集
     */
    @SafeVarargs
    public static <E> ImmutableMultiset<E> of(E... elements) {
        if (elements == null || elements.length == 0) {
            return of();
        }
        return copyOf(Arrays.asList(elements));
    }

    /**
     * Return an immutable multiset containing the elements of the given collection.
     * 返回包含给定集合元素的不可变多重集。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable multiset | 不可变多重集
     */
    public static <E> ImmutableMultiset<E> copyOf(Collection<? extends E> elements) {
        if (elements == null || elements.isEmpty()) {
            return of();
        }
        if (elements instanceof ImmutableMultiset) {
            @SuppressWarnings("unchecked")
            ImmutableMultiset<E> result = (ImmutableMultiset<E>) elements;
            return result;
        }
        Map<E, Integer> counts = new LinkedHashMap<>();
        for (E element : elements) {
            Objects.requireNonNull(element, "Null element not allowed");
            counts.merge(element, 1, Integer::sum);
        }
        return new ImmutableMultiset<>(counts);
    }

    /**
     * Return an immutable multiset containing the elements of the given iterable.
     * 返回包含给定可迭代对象元素的不可变多重集。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable multiset | 不可变多重集
     */
    public static <E> ImmutableMultiset<E> copyOf(Iterable<? extends E> elements) {
        if (elements == null) {
            return of();
        }
        if (elements instanceof Collection) {
            return copyOf((Collection<? extends E>) elements);
        }
        return ImmutableMultiset.<E>builder().addAll(elements).build();
    }

    /**
     * Return a new builder.
     * 返回新构建器。
     *
     * @param <E> element type | 元素类型
     * @return builder | 构建器
     */
    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    // ==================== Multiset 特有方法 | Multiset-Specific Methods ====================

    /**
     * Return the count of the given element in this multiset.
     * 返回此多重集中给定元素的计数。
     *
     * @param element the element to count | 要计数的元素
     * @return the count of the element, or 0 if not present | 元素的计数，如果不存在则为0
     */
    public int count(Object element) {
        if (element == null) {
            return 0;
        }
        return elementCounts.getOrDefault(element, 0);
    }

    /**
     * Return the set of distinct elements in this multiset.
     * 返回此多重集中的不同元素集。
     *
     * @return unmodifiable set of distinct elements | 不可修改的不同元素集
     */
    public Set<E> elementSet() {
        return Collections.unmodifiableSet(elementCounts.keySet());
    }

    /**
     * Return the set of entries with elements and their counts.
     * 返回包含元素及其计数的条目集。
     *
     * @return unmodifiable set of entries | 不可修改的条目集
     */
    public Set<Entry<E>> entrySet() {
        Set<Entry<E>> entries = new LinkedHashSet<>();
        for (Map.Entry<E, Integer> entry : elementCounts.entrySet()) {
            entries.add(new EntryImpl<>(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableSet(entries);
    }

    // ==================== Collection 实现 | Collection Implementation ====================

    @Override
    public Iterator<E> iterator() {
        return new MultisetIterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        return o != null && elementCounts.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        int index = 0;
        for (Map.Entry<E, Integer> entry : elementCounts.entrySet()) {
            E element = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                array[index++] = element;
            }
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        int index = 0;
        for (Map.Entry<E, Integer> entry : elementCounts.entrySet()) {
            E element = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                a[index++] = (T) element;
            }
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    // ==================== 不可变保护 | Immutability Protection ====================

    @Override
    public boolean add(E e) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean remove(Object o) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void clear() {
        throw OpenCollectionException.immutableCollection();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableMultiset<?> that)) return false;
        return elementCounts.equals(that.elementCounts);
    }

    @Override
    public int hashCode() {
        return elementCounts.hashCode();
    }

    @Override
    public String toString() {
        return elementCounts.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Iterator for multiset
     */
    private class MultisetIterator implements Iterator<E> {
        private final Iterator<Map.Entry<E, Integer>> entryIterator = elementCounts.entrySet().iterator();
        private E currentElement;
        private int remainingCount;

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
            return currentElement;
        }

        @Override
        public void remove() {
            throw OpenCollectionException.immutableCollection();
        }
    }

    /**
     * Entry interface for multiset entries
     * 多重集条目的条目接口
     *
     * @param <E> element type | 元素类型
     */
    public interface Entry<E> {
        /**
         * Return the element.
         * 返回元素。
         *
         * @return the element | 元素
         */
        E getElement();

        /**
         * Return the count.
         * 返回计数。
         *
         * @return the count | 计数
         */
        int getCount();
    }

    /**
     * Entry implementation
     */
    private static class EntryImpl<E> implements Entry<E> {
        private final E element;
        private final int count;

        EntryImpl(E element, int count) {
            this.element = element;
            this.count = count;
        }

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
            if (this == o) return true;
            if (!(o instanceof Entry<?> entry)) return false;
            return count == entry.getCount() && Objects.equals(element, entry.getElement());
        }

        @Override
        public int hashCode() {
            return Objects.hash(element, count);
        }

        @Override
        public String toString() {
            return element + " x " + count;
        }
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableMultiset
     * ImmutableMultiset 构建器
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private final Map<E, Integer> elementCounts = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Add an element.
         * 添加元素。
         *
         * @param element the element | 元素
         * @return this builder | 此构建器
         */
        public Builder<E> add(E element) {
            Objects.requireNonNull(element);
            elementCounts.merge(element, 1, Integer::sum);
            return this;
        }

        /**
         * Add elements.
         * 添加元素。
         *
         * @param elements the elements | 元素
         * @return this builder | 此构建器
         */
        @SafeVarargs
        public final Builder<E> add(E... elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * Add multiple copies of an element.
         * 添加元素的多个副本。
         *
         * @param element the element | 元素
         * @param count   the number of copies to add | 要添加的副本数
         * @return this builder | 此构建器
         */
        public Builder<E> add(E element, int count) {
            Objects.requireNonNull(element);
            if (count < 0) {
                throw new IllegalArgumentException("Count cannot be negative: " + count);
            }
            if (count > 0) {
                elementCounts.merge(element, count, Integer::sum);
            }
            return this;
        }

        /**
         * Add all elements from an iterable.
         * 从可迭代对象添加所有元素。
         *
         * @param elements the elements | 元素
         * @return this builder | 此构建器
         */
        public Builder<E> addAll(Iterable<? extends E> elements) {
            for (E element : elements) {
                add(element);
            }
            return this;
        }

        /**
         * Set the count for an element.
         * 设置元素的计数。
         *
         * @param element the element | 元素
         * @param count   the count | 计数
         * @return this builder | 此构建器
         */
        public Builder<E> setCount(E element, int count) {
            Objects.requireNonNull(element);
            if (count < 0) {
                throw new IllegalArgumentException("Count cannot be negative: " + count);
            }
            if (count == 0) {
                elementCounts.remove(element);
            } else {
                elementCounts.put(element, count);
            }
            return this;
        }

        /**
         * Build the immutable multiset.
         * 构建不可变多重集。
         *
         * @return immutable multiset | 不可变多重集
         */
        public ImmutableMultiset<E> build() {
            if (elementCounts.isEmpty()) {
                return of();
            }
            return new ImmutableMultiset<>(new LinkedHashMap<>(elementCounts));
        }
    }
}
