package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableSet - Immutable Set Implementation
 * ImmutableSet - 不可变集合实现
 *
 * <p>A set that cannot be modified after creation. Any attempt to modify
 * the set will throw an exception.</p>
 * <p>创建后不能修改的集合。任何修改集合的尝试都会抛出异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>O(1) contains check - O(1) 包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from elements - 从元素创建
 * ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
 *
 * // Create from collection - 从集合创建
 * ImmutableSet<String> set = ImmutableSet.copyOf(existingSet);
 *
 * // Use builder - 使用构建器
 * ImmutableSet<String> set = ImmutableSet.<String>builder()
 *     .add("a")
 *     .addAll(Arrays.asList("b", "c"))
 *     .build();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>contains: O(1) average - contains: O(1) 平均</li>
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
public final class ImmutableSet<E> extends AbstractSet<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableSet<?> EMPTY = new ImmutableSet<>(Set.of());

    private final Set<E> delegate;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param delegate the delegate set | 委托集合
     */
    private ImmutableSet(Set<E> delegate) {
        this.delegate = delegate;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable set.
     * 返回空不可变集合。
     *
     * @param <E> element type | 元素类型
     * @return empty immutable set | 空不可变集合
     */
    @SuppressWarnings("unchecked")
    public static <E> ImmutableSet<E> of() {
        return (ImmutableSet<E>) EMPTY;
    }

    /**
     * Return an immutable set containing the given element.
     * 返回包含给定元素的不可变集合。
     *
     * @param <E> element type | 元素类型
     * @param e1  the element | 元素
     * @return immutable set | 不可变集合
     */
    public static <E> ImmutableSet<E> of(E e1) {
        return new ImmutableSet<>(Set.of(Objects.requireNonNull(e1)));
    }

    /**
     * Return an immutable set containing the given elements.
     * 返回包含给定元素的不可变集合。
     *
     * @param <E> element type | 元素类型
     * @param e1  first element | 第一个元素
     * @param e2  second element | 第二个元素
     * @return immutable set | 不可变集合
     */
    public static <E> ImmutableSet<E> of(E e1, E e2) {
        return new ImmutableSet<>(Set.of(
                Objects.requireNonNull(e1),
                Objects.requireNonNull(e2)
        ));
    }

    /**
     * Return an immutable set containing the given elements.
     * 返回包含给定元素的不可变集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable set | 不可变集合
     */
    @SafeVarargs
    public static <E> ImmutableSet<E> of(E... elements) {
        if (elements == null || elements.length == 0) {
            return of();
        }
        Set<E> set = new LinkedHashSet<>(elements.length);
        for (int i = 0; i < elements.length; i++) {
            set.add(Objects.requireNonNull(elements[i], "Element at index " + i + " is null"));
        }
        return new ImmutableSet<>(set);
    }

    /**
     * Return an immutable set containing the elements of the given collection.
     * 返回包含给定集合元素的不可变集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable set | 不可变集合
     */
    public static <E> ImmutableSet<E> copyOf(Collection<? extends E> elements) {
        if (elements == null || elements.isEmpty()) {
            return of();
        }
        if (elements instanceof ImmutableSet) {
            @SuppressWarnings("unchecked")
            ImmutableSet<E> result = (ImmutableSet<E>) elements;
            return result;
        }
        Set<E> set = new LinkedHashSet<>(elements.size());
        int i = 0;
        for (E e : elements) {
            set.add(Objects.requireNonNull(e, "Element at index " + i + " is null"));
            i++;
        }
        return new ImmutableSet<>(set);
    }

    /**
     * Return an immutable set containing the elements of the given iterable.
     * 返回包含给定可迭代对象元素的不可变集合。
     *
     * @param <E>      element type | 元素类型
     * @param elements the elements | 元素
     * @return immutable set | 不可变集合
     */
    public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
        if (elements == null) {
            return of();
        }
        if (elements instanceof Collection) {
            return copyOf((Collection<? extends E>) elements);
        }
        return ImmutableSet.<E>builder().addAll(elements).build();
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

    // ==================== Set 实现 | Set Implementation ====================

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIterator<>(delegate.iterator());
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean contains(Object o) {
        return o != null && delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
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

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Unmodifiable iterator
     */
    private static class UnmodifiableIterator<E> implements Iterator<E> {
        private final Iterator<E> delegate;

        UnmodifiableIterator(Iterator<E> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            throw OpenCollectionException.immutableCollection();
        }
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableSet
     * ImmutableSet 构建器
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private final Set<E> elements = new LinkedHashSet<>();

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
         * Build the immutable set.
         * 构建不可变集合。
         *
         * @return immutable set | 不可变集合
         */
        public ImmutableSet<E> build() {
            if (elements.isEmpty()) {
                return of();
            }
            return new ImmutableSet<>(elements);
        }
    }
}
