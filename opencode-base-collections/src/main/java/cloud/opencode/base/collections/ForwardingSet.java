package cloud.opencode.base.collections;

import java.util.Set;

/**
 * ForwardingSet - Abstract Decorator Base for Set
 * ForwardingSet - Set 的抽象装饰器基类
 *
 * <p>An abstract base class that forwards all Set method calls to a delegate.
 * Extends {@link ForwardingCollection} with Set-specific equals and hashCode semantics.
 * Subclasses implement {@link #delegate()} and can override individual methods
 * to add custom behavior.</p>
 * <p>将所有 Set 方法调用转发给委托对象的抽象基类。扩展 {@link ForwardingCollection}
 * 并使用 Set 特定的 equals 和 hashCode 语义。子类实现 {@link #delegate()}
 * 并可以重写单个方法以添加自定义行为。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decorator pattern for Set - Set 的装饰器模式</li>
 *   <li>All Set methods forwarded - 所有 Set 方法转发</li>
 *   <li>Set-specific equals/hashCode semantics - Set 特定的 equals/hashCode 语义</li>
 *   <li>Override methods for custom behavior - 重写方法以自定义行为</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a validated set wrapper
 * class NonNullSet<E> extends ForwardingSet<E> {
 *     private final Set<E> delegate;
 *
 *     NonNullSet(Set<E> delegate) {
 *         this.delegate = delegate;
 *     }
 *
 *     @Override protected Set<E> delegate() { return delegate; }
 *
 *     @Override public boolean add(E e) {
 *         Objects.requireNonNull(e, "null elements not allowed");
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
public abstract class ForwardingSet<E> extends ForwardingCollection<E> implements Set<E> {

    /**
     * Protected constructor for subclasses.
     * 子类的受保护构造方法。
     */
    protected ForwardingSet() {
    }

    /**
     * Return the backing delegate set.
     * 返回后备委托集合。
     *
     * @return the delegate set | 委托集合
     */
    @Override
    protected abstract Set<E> delegate();

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        return o == this || delegate().equals(o);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
}
