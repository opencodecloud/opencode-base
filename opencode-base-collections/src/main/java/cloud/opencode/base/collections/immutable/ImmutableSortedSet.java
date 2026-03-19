package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableSet;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableSortedSet - Immutable Sorted Set Implementation
 * ImmutableSortedSet - 不可变有序集合实现
 *
 * <p>A sorted set that cannot be modified after creation. Elements are stored
 * in sorted order according to their natural ordering or a provided comparator.</p>
 * <p>创建后不能修改的有序集合。元素按自然顺序或提供的比较器排序存储。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Sorted elements - 元素有序</li>
 *   <li>NavigableSet operations - NavigableSet 操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from elements - 从元素创建
 * ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b");
 * // Result: [a, b, c]
 *
 * // Create with comparator - 使用比较器创建
 * ImmutableSortedSet<String> set = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER)
 *     .add("A", "b", "C")
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>contains: O(log n) - contains: O(log n)</li>
 *   <li>iteration: O(n) - iteration: O(n)</li>
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
public final class ImmutableSortedSet<E> extends AbstractSet<E>
        implements NavigableSet<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final ImmutableSortedSet EMPTY = new ImmutableSortedSet<>(new Object[0], null);

    private final Object[] elements;
    private final Comparator<? super E> comparator;

    // ==================== 构造方法 | Constructors ====================

    @SuppressWarnings("unchecked")
    private ImmutableSortedSet(Object[] elements, Comparator<? super E> comparator) {
        this.elements = elements;
        this.comparator = comparator;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable sorted set.
     * 返回空不可变有序集合。
     *
     * @param <E> element type | 元素类型
     * @return empty immutable sorted set | 空不可变有序集合
     */
    @SuppressWarnings("unchecked")
    public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of() {
        return (ImmutableSortedSet<E>) EMPTY;
    }

    /**
     * Return an immutable sorted set with one element.
     * 返回包含一个元素的不可变有序集合。
     *
     * @param <E> element type | 元素类型
     * @param e1  the element | 元素
     * @return immutable sorted set | 不可变有序集合
     */
    public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E e1) {
        Objects.requireNonNull(e1);
        return new ImmutableSortedSet<>(new Object[]{e1}, null);
    }

    /**
     * Return an immutable sorted set with multiple elements.
     * 返回包含多个元素的不可变有序集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable sorted set | 不可变有序集合
     */
    @SafeVarargs
    public static <E extends Comparable<? super E>> ImmutableSortedSet<E> of(E... elements) {
        if (elements.length == 0) {
            return of();
        }
        return copyOf(Arrays.asList(elements));
    }

    /**
     * Copy from a collection.
     * 从集合复制。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable sorted set | 不可变有序集合
     */
    public static <E extends Comparable<? super E>> ImmutableSortedSet<E> copyOf(
            Collection<? extends E> elements) {
        return copyOf(elements, null);
    }

    /**
     * Copy from a collection with comparator.
     * 使用比较器从集合复制。
     *
     * @param <E>        element type | 元素类型
     * @param elements   the elements | 元素
     * @param comparator the comparator | 比较器
     * @return immutable sorted set | 不可变有序集合
     */
    @SuppressWarnings("unchecked")
    public static <E> ImmutableSortedSet<E> copyOf(Collection<? extends E> elements,
                                                    Comparator<? super E> comparator) {
        if (elements.isEmpty()) {
            return (ImmutableSortedSet<E>) EMPTY;
        }
        TreeSet<E> sorted = comparator != null
                ? new TreeSet<>(comparator)
                : new TreeSet<>();
        for (E element : elements) {
            Objects.requireNonNull(element, "null element");
            sorted.add(element);
        }
        return new ImmutableSortedSet<>(sorted.toArray(), comparator);
    }

    /**
     * Create a builder with natural ordering.
     * 创建使用自然顺序的构建器。
     *
     * @param <E> element type | 元素类型
     * @return builder | 构建器
     */
    public static <E extends Comparable<? super E>> Builder<E> naturalOrder() {
        return new Builder<>(null);
    }

    /**
     * Create a builder with specified comparator.
     * 创建使用指定比较器的构建器。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return builder | 构建器
     */
    public static <E> Builder<E> orderedBy(Comparator<? super E> comparator) {
        return new Builder<>(comparator);
    }

    // ==================== Set 方法 | Set Methods ====================

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return elements.length == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        return binarySearch((E) o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayIterator<>(elements, 0, elements.length);
    }

    // ==================== NavigableSet 方法 | NavigableSet Methods ====================

    @Override
    @SuppressWarnings("unchecked")
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) elements[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) elements[elements.length - 1];
    }

    @Override
    @SuppressWarnings("unchecked")
    public E lower(E e) {
        int index = lowerIndex(e);
        return index >= 0 ? (E) elements[index] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E floor(E e) {
        int index = floorIndex(e);
        return index >= 0 ? (E) elements[index] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E ceiling(E e) {
        int index = ceilingIndex(e);
        return index < elements.length ? (E) elements[index] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E higher(E e) {
        int index = higherIndex(e);
        return index < elements.length ? (E) elements[index] : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("ImmutableSortedSet is immutable");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("ImmutableSortedSet is immutable");
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingSet();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ArrayIterator<>(elements, elements.length - 1, -1);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int from = fromInclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        int to = toInclusive ? floorIndex(toElement) : lowerIndex(toElement);
        return subSetByIndex(from, to + 1);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int to = inclusive ? floorIndex(toElement) : lowerIndex(toElement);
        return subSetByIndex(0, to + 1);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int from = inclusive ? ceilingIndex(fromElement) : higherIndex(fromElement);
        return subSetByIndex(from, elements.length);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    // ==================== 辅助方法 | Helper Methods ====================

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int binarySearch(E key) {
        Comparator cmp = comparator != null
                ? comparator
                : Comparator.naturalOrder();
        return Arrays.binarySearch(elements, key, cmp);
    }

    private int lowerIndex(E e) {
        int index = binarySearch(e);
        if (index >= 0) {
            return index - 1;
        }
        return -index - 2;
    }

    private int floorIndex(E e) {
        int index = binarySearch(e);
        if (index >= 0) {
            return index;
        }
        return -index - 2;
    }

    private int ceilingIndex(E e) {
        int index = binarySearch(e);
        if (index >= 0) {
            return index;
        }
        return -index - 1;
    }

    private int higherIndex(E e) {
        int index = binarySearch(e);
        if (index >= 0) {
            return index + 1;
        }
        return -index - 1;
    }

    @SuppressWarnings("unchecked")
    private ImmutableSortedSet<E> subSetByIndex(int from, int to) {
        if (from >= to || from >= elements.length) {
            return (ImmutableSortedSet<E>) EMPTY;
        }
        from = Math.max(0, from);
        to = Math.min(to, elements.length);
        return new ImmutableSortedSet<>(Arrays.copyOfRange(elements, from, to), comparator);
    }

    // ==================== 内部类 | Inner Classes ====================

    /**
     * Builder for ImmutableSortedSet.
     * ImmutableSortedSet 构建器。
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private final TreeSet<E> elements;

        Builder(Comparator<? super E> comparator) {
            this.elements = comparator != null ? new TreeSet<>(comparator) : new TreeSet<>();
        }

        /**
         * Add element.
         * 添加元素。
         *
         * @param element the element | 元素
         * @return this builder | 此构建器
         */
        public Builder<E> add(E element) {
            Objects.requireNonNull(element);
            elements.add(element);
            return this;
        }

        /**
         * Add multiple elements.
         * 添加多个元素。
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
         * Add all elements.
         * 添加所有元素。
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
         * Build the immutable sorted set.
         * 构建不可变有序集合。
         *
         * @return immutable sorted set | 不可变有序集合
         */
        @SuppressWarnings("unchecked")
        public ImmutableSortedSet<E> build() {
            if (elements.isEmpty()) {
                return (ImmutableSortedSet<E>) EMPTY;
            }
            return new ImmutableSortedSet<>(elements.toArray(), elements.comparator());
        }
    }

    private static final class ArrayIterator<E> implements Iterator<E> {
        private final Object[] array;
        private int current;
        private final int end;
        private final int step;

        ArrayIterator(Object[] array, int start, int end) {
            this.array = array;
            this.current = start;
            this.end = end;
            this.step = start <= end ? 1 : -1;
        }

        @Override
        public boolean hasNext() {
            return step > 0 ? current < end : current > end;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E element = (E) array[current];
            current += step;
            return element;
        }
    }

    private final class DescendingSet extends AbstractSet<E> implements NavigableSet<E> {
        @Override
        public int size() {
            return ImmutableSortedSet.this.size();
        }

        @Override
        public Iterator<E> iterator() {
            return descendingIterator();
        }

        @Override
        public Comparator<? super E> comparator() {
            Comparator<? super E> cmp = ImmutableSortedSet.this.comparator();
            return cmp != null ? cmp.reversed() : Collections.reverseOrder();
        }

        @Override
        public E first() {
            return ImmutableSortedSet.this.last();
        }

        @Override
        public E last() {
            return ImmutableSortedSet.this.first();
        }

        @Override
        public E lower(E e) {
            return ImmutableSortedSet.this.higher(e);
        }

        @Override
        public E floor(E e) {
            return ImmutableSortedSet.this.ceiling(e);
        }

        @Override
        public E ceiling(E e) {
            return ImmutableSortedSet.this.floor(e);
        }

        @Override
        public E higher(E e) {
            return ImmutableSortedSet.this.lower(e);
        }

        @Override
        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return ImmutableSortedSet.this;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return ImmutableSortedSet.this.iterator();
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return ImmutableSortedSet.this.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return ImmutableSortedSet.this.tailSet(toElement, inclusive).descendingSet();
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return ImmutableSortedSet.this.headSet(fromElement, inclusive).descendingSet();
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }
    }
}
