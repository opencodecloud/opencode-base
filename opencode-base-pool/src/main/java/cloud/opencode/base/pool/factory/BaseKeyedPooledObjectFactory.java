package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;

/**
 * BaseKeyedPooledObjectFactory - Base Keyed Pooled Object Factory
 * BaseKeyedPooledObjectFactory - 基础键控池化对象工厂
 *
 * <p>Abstract base class for KeyedPooledObjectFactory implementations.
 * Subclasses only need to implement the create(key) method.</p>
 * <p>KeyedPooledObjectFactory实现的抽象基类。子类只需实现create(key)方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simplified keyed factory implementation - 简化的键控工厂实现</li>
 *   <li>Default no-op lifecycle methods - 默认空操作的生命周期方法</li>
 *   <li>Key-aware object creation - 键感知的对象创建</li>
 *   <li>Override only what you need - 只覆盖需要的方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KeyedPooledObjectFactory<String, Connection> factory =
 *     new BaseKeyedPooledObjectFactory<>() {
 *         @Override
 *         protected Connection create(String dsName) throws OpenPoolException {
 *             try {
 *                 return getDataSource(dsName).getConnection();
 *             } catch (SQLException e) {
 *                 throw new OpenPoolException("Failed to create connection", e);
 *             }
 *         }
 *
 *         @Override
 *         public void destroyObject(String key, PooledObject<Connection> obj) {
 *             try {
 *                 obj.getObject().close();
 *             } catch (SQLException e) {
 *                 // log error
 *             }
 *         }
 *     };
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <K> the key type - 键类型
 * @param <V> the value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public abstract class BaseKeyedPooledObjectFactory<K, V> implements KeyedPooledObjectFactory<K, V> {

    /**
     * Creates the actual object instance for the given key.
     * 为给定的键创建实际的对象实例。
     *
     * <p>Subclasses must implement this method to create the object.</p>
     * <p>子类必须实现此方法来创建对象。</p>
     *
     * @param key the key - 键
     * @return the new object - 新对象
     * @throws OpenPoolException if creation fails - 如果创建失败
     */
    protected abstract V create(K key) throws OpenPoolException;

    /**
     * Wraps an object in a PooledObject wrapper.
     * 将对象包装在PooledObject包装器中。
     *
     * @param obj the object to wrap - 要包装的对象
     * @return the wrapped object - 包装后的对象
     */
    protected PooledObject<V> wrap(V obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public PooledObject<V> makeObject(K key) throws OpenPoolException {
        return wrap(create(key));
    }

    @Override
    public void destroyObject(K key, PooledObject<V> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }

    @Override
    public boolean validateObject(K key, PooledObject<V> obj) {
        return true;
    }

    @Override
    public void activateObject(K key, PooledObject<V> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }

    @Override
    public void passivateObject(K key, PooledObject<V> obj) throws OpenPoolException {
        // Default: no-op, subclasses can override
    }
}
