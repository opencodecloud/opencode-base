package cloud.opencode.base.collections;

import java.util.Iterator;

/**
 * UnmodifiableIterator - Iterator that does not support remove operation
 * UnmodifiableIterator - 不支持删除操作的迭代器
 *
 * <p>An iterator that does not support the {@link #remove()} operation. Any attempt
 * to call remove() will throw an {@link UnsupportedOperationException}.</p>
 * <p>不支持 {@link #remove()} 操作的迭代器。任何调用 remove() 的尝试都会抛出
 * {@link UnsupportedOperationException}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Read-only iteration - 只读迭代</li>
 *   <li>Safe for immutable collections - 适用于不可变集合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UnmodifiableIterator<String> iterator = UnmodifiableIterator.of(list.iterator());
 * while (iterator.hasNext()) {
 *     String element = iterator.next();
 *     // iterator.remove() would throw UnsupportedOperationException
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>All operations: O(1) per call - 所有操作: 每次调用 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Same as underlying iterator - 线程安全: 与底层迭代器相同</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class UnmodifiableIterator<E> implements Iterator<E> {

    /**
     * Protected constructor for subclasses.
     * 子类使用的保护构造方法。
     */
    protected UnmodifiableIterator() {
    }

    /**
     * Remove operation is not supported.
     * 不支持删除操作。
     *
     * @throws UnsupportedOperationException always | 总是抛出
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException("remove() is not supported by UnmodifiableIterator");
    }

    /**
     * Create an unmodifiable iterator wrapping the given iterator.
     * 创建包装给定迭代器的不可修改迭代器。
     *
     * @param <E>      element type | 元素类型
     * @param iterator the iterator to wrap | 要包装的迭代器
     * @return unmodifiable iterator | 不可修改迭代器
     */
    public static <E> UnmodifiableIterator<E> of(Iterator<? extends E> iterator) {
        if (iterator instanceof UnmodifiableIterator) {
            @SuppressWarnings("unchecked")
            UnmodifiableIterator<E> result = (UnmodifiableIterator<E>) iterator;
            return result;
        }
        return new WrappingUnmodifiableIterator<>(iterator);
    }

    /**
     * Wrapping implementation of UnmodifiableIterator.
     * UnmodifiableIterator 的包装实现。
     *
     * @param <E> element type | 元素类型
     */
    private static final class WrappingUnmodifiableIterator<E> extends UnmodifiableIterator<E> {
        private final Iterator<? extends E> delegate;

        WrappingUnmodifiableIterator(Iterator<? extends E> delegate) {
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
    }
}
