package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;

/**
 * KeyedPooledObjectFactory - Keyed Pooled Object Factory Interface
 * KeyedPooledObjectFactory - 键控池化对象工厂接口
 *
 * <p>Factory interface for creating and managing keyed pooled objects.
 * Each key can have different object configuration or type.</p>
 * <p>用于创建和管理键控池化对象的工厂接口。每个键可以有不同的对象配置或类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key-based object creation - 基于键的对象创建</li>
 *   <li>Per-key lifecycle management - 每个键的生命周期管理</li>
 *   <li>Multi-tenant support - 多租户支持</li>
 *   <li>Multi-datasource pooling - 多数据源池化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KeyedPooledObjectFactory<String, Connection> factory =
 *     new BaseKeyedPooledObjectFactory<>() {
 *         @Override
 *         protected Connection create(String dsName) {
 *             return getDataSource(dsName).getConnection();
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
public interface KeyedPooledObjectFactory<K, V> {

    /**
     * Creates a new pooled object for the given key.
     * 为给定的键创建新的池化对象。
     *
     * @param key the key - 键
     * @return the new pooled object - 新的池化对象
     * @throws OpenPoolException if creation fails - 如果创建失败
     */
    PooledObject<V> makeObject(K key) throws OpenPoolException;

    /**
     * Destroys a pooled object for the given key.
     * 销毁给定键的池化对象。
     *
     * @param key the key - 键
     * @param obj the object to destroy - 要销毁的对象
     * @throws OpenPoolException if destruction fails - 如果销毁失败
     */
    void destroyObject(K key, PooledObject<V> obj) throws OpenPoolException;

    /**
     * Validates a pooled object for the given key.
     * 验证给定键的池化对象。
     *
     * @param key the key - 键
     * @param obj the object to validate - 要验证的对象
     * @return true if valid - 有效返回true
     */
    boolean validateObject(K key, PooledObject<V> obj);

    /**
     * Activates a pooled object for the given key.
     * 激活给定键的池化对象。
     *
     * @param key the key - 键
     * @param obj the object to activate - 要激活的对象
     * @throws OpenPoolException if activation fails - 如果激活失败
     */
    void activateObject(K key, PooledObject<V> obj) throws OpenPoolException;

    /**
     * Passivates a pooled object for the given key.
     * 钝化给定键的池化对象。
     *
     * @param key the key - 键
     * @param obj the object to passivate - 要钝化的对象
     * @throws OpenPoolException if passivation fails - 如果钝化失败
     */
    void passivateObject(K key, PooledObject<V> obj) throws OpenPoolException;
}
