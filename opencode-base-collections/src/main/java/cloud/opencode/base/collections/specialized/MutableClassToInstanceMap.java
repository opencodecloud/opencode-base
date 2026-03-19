package cloud.opencode.base.collections.specialized;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * MutableClassToInstanceMap - Mutable Class to Instance Map Implementation
 * MutableClassToInstanceMap - 可变类实例映射实现
 *
 * <p>A mutable type-safe map from Class objects to instances of that class.</p>
 * <p>从 Class 对象到该类实例的可变类型安全映射。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe instance retrieval - 类型安全的实例获取</li>
 *   <li>Type-safe instance storage - 类型安全的实例存储</li>
 *   <li>Mutable - 可变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
 * map.putInstance(String.class, "hello");
 * map.putInstance(Integer.class, 42);
 *
 * String str = map.getInstance(String.class);  // "hello"
 * Integer num = map.getInstance(Integer.class); // 42
 *
 * // Also supports Map interface
 * map.put(Double.class, 3.14);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>getInstance: O(1) - getInstance: O(1)</li>
 *   <li>putInstance: O(1) - putInstance: O(1)</li>
 *   <li>containsKey: O(1) - containsKey: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <B> base type | 基础类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MutableClassToInstanceMap<B> extends AbstractMap<Class<? extends B>, B>
        implements ClassToInstanceMap<B>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Class<? extends B>, B> delegate;

    // ==================== 构造方法 | Constructors ====================

    private MutableClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
        this.delegate = delegate;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create a new MutableClassToInstanceMap backed by a HashMap.
     * 创建由 HashMap 支持的新 MutableClassToInstanceMap。
     *
     * @param <B> base type | 基础类型
     * @return new MutableClassToInstanceMap | 新 MutableClassToInstanceMap
     */
    public static <B> MutableClassToInstanceMap<B> create() {
        return new MutableClassToInstanceMap<>(new LinkedHashMap<>());
    }

    /**
     * Create a new MutableClassToInstanceMap backed by the given map.
     * 创建由给定映射支持的新 MutableClassToInstanceMap。
     *
     * @param <B>         base type | 基础类型
     * @param backingMap  the backing map | 支持映射
     * @return new MutableClassToInstanceMap | 新 MutableClassToInstanceMap
     */
    public static <B> MutableClassToInstanceMap<B> create(Map<Class<? extends B>, B> backingMap) {
        return new MutableClassToInstanceMap<>(backingMap);
    }

    // ==================== ClassToInstanceMap 方法 | ClassToInstanceMap Methods ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T extends B> T getInstance(Class<T> type) {
        return (T) delegate.get(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends B> T putInstance(Class<T> type, T value) {
        Objects.requireNonNull(type, "Type cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        if (!type.isInstance(value)) {
            throw new ClassCastException("Value is not an instance of " + type);
        }
        return (T) delegate.put(type, value);
    }

    // ==================== Map 方法 | Map Methods ====================

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public B get(Object key) {
        return delegate.get(key);
    }

    @Override
    public B put(Class<? extends B> key, B value) {
        Objects.requireNonNull(key, "Key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        if (!key.isInstance(value)) {
            throw new ClassCastException("Value is not an instance of " + key);
        }
        return delegate.put(key, value);
    }

    @Override
    public B remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends Class<? extends B>, ? extends B> m) {
        for (Entry<? extends Class<? extends B>, ? extends B> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<Class<? extends B>> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<B> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<Class<? extends B>, B>> entrySet() {
        return delegate.entrySet();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map<?, ?> map)) return false;
        return delegate.equals(map);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
