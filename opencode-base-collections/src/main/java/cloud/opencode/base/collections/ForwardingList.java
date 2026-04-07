package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * ForwardingList - Abstract Decorator Base for List
 * ForwardingList - List 的抽象装饰器基类
 *
 * <p>An abstract base class that forwards all List method calls to a delegate.
 * Extends {@link ForwardingCollection} and adds List-specific method delegation.
 * Subclasses implement {@link #delegate()} and can override individual methods
 * to add custom behavior.</p>
 * <p>将所有 List 方法调用转发给委托对象的抽象基类。扩展 {@link ForwardingCollection}
 * 并添加 List 特定的方法委托。子类实现 {@link #delegate()} 并可以重写单个方法以添加自定义行为。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decorator pattern for List - List 的装饰器模式</li>
 *   <li>All List methods forwarded - 所有 List 方法转发</li>
 *   <li>Includes index-based operations - 包含基于索引的操作</li>
 *   <li>ListIterator and subList delegation - ListIterator 和 subList 委托</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a read-only list wrapper
 * class UnmodifiableList<E> extends ForwardingList<E> {
 *     private final List<E> delegate;
 *
 *     UnmodifiableList(List<E> delegate) {
 *         this.delegate = delegate;
 *     }
 *
 *     @Override protected List<E> delegate() { return delegate; }
 *
 *     @Override public boolean add(E e) {
 *         throw new UnsupportedOperationException();
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
public abstract class ForwardingList<E> extends ForwardingCollection<E> implements List<E> {

    /**
     * Protected constructor for subclasses.
     * 子类的受保护构造方法。
     */
    protected ForwardingList() {
    }

    /**
     * Return the backing delegate list.
     * 返回后备委托列表。
     *
     * @return the delegate list | 委托列表
     */
    @Override
    protected abstract List<E> delegate();

    // ==================== List 方法 | List Methods ====================

    @Override
    public void add(int index, E element) {
        delegate().add(index, element);
    }

    /**
     * Add all elements at the given index by iterating through {@link #add(int, Object)}.
     * This ensures subclass overrides of {@code add(int, E)} are honored.
     * 通过迭代调用 {@link #add(int, Object)} 在指定位置添加所有元素。确保子类对 {@code add(int, E)} 的覆写被执行。
     *
     * @param index   position to insert | 插入位置
     * @param c       collection of elements to add | 要添加的元素集合
     * @return true if this list changed | 如果列表发生变化则返回 true
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean changed = false;
        int i = index;
        for (E e : c) {
            add(i++, e);
            changed = true;
        }
        return changed;
    }

    @Override
    public E get(int index) {
        return delegate().get(index);
    }

    @Override
    public E set(int index, E element) {
        return delegate().set(index, element);
    }

    @Override
    public E remove(int index) {
        return delegate().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return delegate().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return delegate().listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }
}
