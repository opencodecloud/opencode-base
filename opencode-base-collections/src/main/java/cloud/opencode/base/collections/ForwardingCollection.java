package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.Iterator;

/**
 * ForwardingCollection - Abstract Decorator Base for Collection
 * ForwardingCollection - Collection 的抽象装饰器基类
 *
 * <p>An abstract base class that forwards all Collection method calls to a delegate.
 * Subclasses implement {@link #delegate()} and can override individual methods
 * to add custom behavior such as logging, validation, or transformation.</p>
 * <p>将所有 Collection 方法调用转发给委托对象的抽象基类。子类实现 {@link #delegate()}
 * 并可以重写单个方法以添加自定义行为，如日志记录、验证或转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decorator pattern base class - 装饰器模式基类</li>
 *   <li>All Collection methods forwarded - 所有 Collection 方法转发</li>
 *   <li>Override individual methods for custom behavior - 重写单个方法以自定义行为</li>
 *   <li>Consistent equals/hashCode delegation - 一致的 equals/hashCode 委托</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a logging collection wrapper
 * class LoggingCollection<E> extends ForwardingCollection<E> {
 *     private final Collection<E> delegate;
 *
 *     LoggingCollection(Collection<E> delegate) {
 *         this.delegate = delegate;
 *     }
 *
 *     @Override protected Collection<E> delegate() { return delegate; }
 *
 *     @Override public boolean add(E e) {
 *         System.out.println("Adding: " + e);
 *         return super.add(e);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on delegate - 线程安全: 取决于委托对象</li>
 *   <li>Null-safe: Depends on delegate - 空值安全: 取决于委托对象</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public abstract class ForwardingCollection<E> implements Collection<E> {

    /**
     * Protected constructor for subclasses.
     * 子类的受保护构造方法。
     */
    protected ForwardingCollection() {
    }

    /**
     * Return the backing delegate collection.
     * 返回后备委托集合。
     *
     * @return the delegate collection | 委托集合
     */
    protected abstract Collection<E> delegate();

    // ==================== Collection 方法 | Collection Methods ====================

    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate().iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return delegate().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return delegate().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate().containsAll(c);
    }

    /**
     * Add all elements by iterating through {@link #add(Object)}.
     * This ensures subclass overrides of {@code add()} are honored.
     * 通过迭代调用 {@link #add(Object)} 添加所有元素。确保子类对 {@code add()} 的覆写被执行。
     *
     * @param c collection of elements to add | 要添加的元素集合
     * @return true if this collection changed | 如果集合发生变化则返回 true
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E e : c) {
            changed |= add(e);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate().retainAll(c);
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public String toString() {
        return delegate().toString();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || delegate().equals(o);
    }
}
