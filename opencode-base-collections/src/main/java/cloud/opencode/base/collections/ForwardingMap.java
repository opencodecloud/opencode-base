package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * ForwardingMap - Abstract Decorator Base for Map
 * ForwardingMap - Map 的抽象装饰器基类
 *
 * <p>An abstract base class that forwards all Map method calls to a delegate.
 * Subclasses implement {@link #delegate()} and can override individual methods
 * to add custom behavior such as logging, validation, or transformation.</p>
 * <p>将所有 Map 方法调用转发给委托对象的抽象基类。子类实现 {@link #delegate()}
 * 并可以重写单个方法以添加自定义行为，如日志记录、验证或转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decorator pattern for Map - Map 的装饰器模式</li>
 *   <li>All Map methods forwarded - 所有 Map 方法转发</li>
 *   <li>Override individual methods for custom behavior - 重写单个方法以自定义行为</li>
 *   <li>Consistent equals/hashCode/toString delegation - 一致的 equals/hashCode/toString 委托</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a case-insensitive map wrapper
 * class CaseInsensitiveMap<V> extends ForwardingMap<String, V> {
 *     private final Map<String, V> delegate;
 *
 *     CaseInsensitiveMap(Map<String, V> delegate) {
 *         this.delegate = delegate;
 *     }
 *
 *     @Override protected Map<String, V> delegate() { return delegate; }
 *
 *     @Override public V put(String key, V value) {
 *         return super.put(key.toLowerCase(), value);
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
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public abstract class ForwardingMap<K, V> implements Map<K, V> {

    /**
     * Protected constructor for subclasses.
     * 子类的受保护构造方法。
     */
    protected ForwardingMap() {
    }

    /**
     * Return the backing delegate map.
     * 返回后备委托映射。
     *
     * @return the delegate map | 委托映射
     */
    protected abstract Map<K, V> delegate();

    // ==================== Map 方法 | Map Methods ====================

    @Override
    public int size() {
        return delegate().size();
    }

    @Override
    public boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate().get(key);
    }

    @Override
    public V put(K key, V value) {
        return delegate().put(key, value);
    }

    @Override
    public V remove(Object key) {
        return delegate().remove(key);
    }

    /**
     * Put all entries by iterating through {@link #put(Object, Object)}.
     * This ensures subclass overrides of {@code put()} are honored.
     * 通过迭代调用 {@link #put(Object, Object)} 放入所有条目。确保子类对 {@code put()} 的覆写被执行。
     *
     * @param m mappings to put | 要放入的映射
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate().keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate().entrySet();
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
