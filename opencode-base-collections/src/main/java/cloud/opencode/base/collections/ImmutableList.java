package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * ImmutableList - Immutable List Implementation
 * ImmutableList - 不可变列表实现
 *
 * <p>A list that cannot be modified after creation. Any attempt to modify
 * the list will throw an exception.</p>
 * <p>创建后不能修改的列表。任何修改列表的尝试都会抛出异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>Random access - 随机访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from elements - 从元素创建
 * ImmutableList<String> list = ImmutableList.of("a", "b", "c");
 *
 * // Create from collection - 从集合创建
 * ImmutableList<String> list = ImmutableList.copyOf(existingList);
 *
 * // Use builder - 使用构建器
 * ImmutableList<String> list = ImmutableList.<String>builder()
 *     .add("a")
 *     .addAll(Arrays.asList("b", "c"))
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>contains: O(n) - contains: O(n)</li>
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
public final class ImmutableList<E> extends AbstractList<E> implements RandomAccess, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableList<?> EMPTY = new ImmutableList<>(new Object[0]);

    private final Object[] elements;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param elements the elements | 元素
     */
    private ImmutableList(Object[] elements) {
        this.elements = elements;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable list.
     * 返回空不可变列表。
     *
     * @param <E> element type | 元素类型
     * @return empty immutable list | 空不可变列表
     */
    @SuppressWarnings("unchecked")
    public static <E> ImmutableList<E> of() {
        return (ImmutableList<E>) EMPTY;
    }

    /**
     * Return an immutable list containing the given element.
     * 返回包含给定元素的不可变列表。
     *
     * @param <E> element type | 元素类型
     * @param e1  the element | 元素
     * @return immutable list | 不可变列表
     */
    public static <E> ImmutableList<E> of(E e1) {
        return new ImmutableList<>(new Object[]{Objects.requireNonNull(e1)});
    }

    /**
     * Return an immutable list containing the given elements.
     * 返回包含给定元素的不可变列表。
     *
     * @param <E> element type | 元素类型
     * @param e1  first element | 第一个元素
     * @param e2  second element | 第二个元素
     * @return immutable list | 不可变列表
     */
    public static <E> ImmutableList<E> of(E e1, E e2) {
        return new ImmutableList<>(new Object[]{
                Objects.requireNonNull(e1),
                Objects.requireNonNull(e2)
        });
    }

    /**
     * Return an immutable list containing the given elements.
     * 返回包含给定元素的不可变列表。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable list | 不可变列表
     */
    @SafeVarargs
    public static <E> ImmutableList<E> of(E... elements) {
        if (elements == null || elements.length == 0) {
            return of();
        }
        Object[] copy = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            copy[i] = Objects.requireNonNull(elements[i], "Element at index " + i + " is null");
        }
        return new ImmutableList<>(copy);
    }

    /**
     * Return an immutable list containing the elements of the given collection.
     * 返回包含给定集合元素的不可变列表。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable list | 不可变列表
     */
    public static <E> ImmutableList<E> copyOf(Collection<? extends E> elements) {
        if (elements == null || elements.isEmpty()) {
            return of();
        }
        if (elements instanceof ImmutableList) {
            @SuppressWarnings("unchecked")
            ImmutableList<E> result = (ImmutableList<E>) elements;
            return result;
        }
        Object[] array = elements.toArray();
        for (int i = 0; i < array.length; i++) {
            Objects.requireNonNull(array[i], "Element at index " + i + " is null");
        }
        return new ImmutableList<>(array);
    }

    /**
     * Return an immutable list containing the elements of the given iterable.
     * 返回包含给定可迭代对象元素的不可变列表。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable list | 不可变列表
     */
    public static <E> ImmutableList<E> copyOf(Iterable<? extends E> elements) {
        if (elements == null) {
            return of();
        }
        if (elements instanceof Collection) {
            return copyOf((Collection<? extends E>) elements);
        }
        return ImmutableList.<E>builder().addAll(elements).build();
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

    // ==================== List 实现 | List Implementation ====================

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        Objects.checkIndex(index, elements.length);
        return (E) elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            return -1;
        }
        for (int i = 0; i < elements.length; i++) {
            if (o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            return -1;
        }
        for (int i = elements.length - 1; i >= 0; i--) {
            if (o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Object[] toArray() {
        return elements.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int size = elements.length;
        if (a.length < size) {
            return (T[]) Arrays.copyOf(elements, size, a.getClass());
        }
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    // ==================== 不可变保护 | Immutability Protection ====================

    @Override
    public E set(int index, E element) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void add(int index, E element) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public E remove(int index) {
        throw OpenCollectionException.immutableCollection();
    }

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
    public boolean addAll(int index, Collection<? extends E> c) {
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

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw OpenCollectionException.immutableCollection();
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw OpenCollectionException.immutableCollection();
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableList
     * ImmutableList 构建器
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private final List<E> elements = new ArrayList<>();

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
            elements.add(Objects.requireNonNull(element));
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
            for (E e : elements) {
                add(e);
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
            for (E e : elements) {
                add(e);
            }
            return this;
        }

        /**
         * Build the immutable list.
         * 构建不可变列表。
         *
         * @return immutable list | 不可变列表
         */
        public ImmutableList<E> build() {
            if (elements.isEmpty()) {
                return of();
            }
            return new ImmutableList<>(elements.toArray());
        }
    }
}
