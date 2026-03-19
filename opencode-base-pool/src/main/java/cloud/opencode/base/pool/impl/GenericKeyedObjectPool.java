package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.*;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.KeyedPooledObjectFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GenericKeyedObjectPool - Generic Keyed Object Pool Implementation
 * GenericKeyedObjectPool - 通用键控对象池实现
 *
 * <p>Keyed object pool that manages separate pools for each key.
 * Useful for multi-tenant or multi-datasource scenarios.</p>
 * <p>为每个键管理独立池的键控对象池。适用于多租户或多数据源场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-key pool management - 每键池管理</li>
 *   <li>Lazy pool creation - 延迟池创建</li>
 *   <li>Shared configuration per pool - 每池共享配置</li>
 *   <li>Thread-safe key operations - 线程安全的键操作</li>
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
 *
 * GenericKeyedObjectPool<String, Connection> pool =
 *     new GenericKeyedObjectPool<>(factory, config);
 *
 * Connection masterConn = pool.borrowObject("master");
 * Connection slaveConn = pool.borrowObject("slave");
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap, AtomicBoolean) - 线程安全: 是（ConcurrentHashMap，AtomicBoolean）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param <K> the key type - 键类型
 * @param <V> the value type - 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class GenericKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {

    private final KeyedPooledObjectFactory<K, V> factory;
    private final PoolConfig config;
    private final Map<K, ObjectPool<V>> pools = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Creates a keyed pool with default configuration.
     * 使用默认配置创建键控池。
     *
     * @param factory the keyed factory - 键控工厂
     */
    public GenericKeyedObjectPool(KeyedPooledObjectFactory<K, V> factory) {
        this(factory, PoolConfig.defaults());
    }

    /**
     * Creates a keyed pool with custom configuration.
     * 使用自定义配置创建键控池。
     *
     * @param factory the keyed factory - 键控工厂
     * @param config  the pool configuration - 池配置
     */
    public GenericKeyedObjectPool(KeyedPooledObjectFactory<K, V> factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
    }

    @Override
    public V borrowObject(K key) throws OpenPoolException {
        checkClosed();
        return getPool(key).borrowObject();
    }

    @Override
    public V borrowObject(K key, Duration timeout) throws OpenPoolException {
        checkClosed();
        return getPool(key).borrowObject(timeout);
    }

    @Override
    public void returnObject(K key, V obj) {
        ObjectPool<V> pool = pools.get(key);
        if (pool != null) {
            pool.returnObject(obj);
        }
        // If pool is null (e.g., after close()), silently ignore the return.
        // The object was likely already destroyed during close().
    }

    @Override
    public void invalidateObject(K key, V obj) {
        ObjectPool<V> pool = pools.get(key);
        if (pool != null) {
            pool.invalidateObject(obj);
        }
        // If pool is null (e.g., after close()), silently ignore.
    }

    @Override
    public int getNumIdle(K key) {
        ObjectPool<V> pool = pools.get(key);
        return pool != null ? pool.getNumIdle() : 0;
    }

    @Override
    public int getNumActive(K key) {
        ObjectPool<V> pool = pools.get(key);
        return pool != null ? pool.getNumActive() : 0;
    }

    @Override
    public void clear(K key) {
        ObjectPool<V> pool = pools.get(key);
        if (pool != null) {
            pool.clear();
        }
    }

    @Override
    public void clear() {
        pools.values().forEach(ObjectPool::clear);
    }

    @Override
    public int getNumKeys() {
        return pools.size();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;

        pools.values().forEach(pool -> {
            try {
                pool.close();
            } catch (Exception e) {
                // Ignore
            }
        });
        pools.clear();
    }

    /**
     * Gets total idle count across all keys.
     * 获取所有键的空闲总数。
     *
     * @return the total idle count - 空闲总数
     */
    public int getTotalNumIdle() {
        return pools.values().stream()
                .mapToInt(ObjectPool::getNumIdle)
                .sum();
    }

    /**
     * Gets total active count across all keys.
     * 获取所有键的活跃总数。
     *
     * @return the total active count - 活跃总数
     */
    public int getTotalNumActive() {
        return pools.values().stream()
                .mapToInt(ObjectPool::getNumActive)
                .sum();
    }

    // ==================== Internal Methods ====================

    private void checkClosed() throws OpenPoolException {
        if (closed.get()) {
            throw new OpenPoolException("Pool is closed");
        }
    }

    private ObjectPool<V> getPool(K key) {
        return pools.computeIfAbsent(key, k ->
                new GenericObjectPool<>(
                        new KeyedFactoryAdapter<>(factory, k),
                        config
                )
        );
    }

    /**
     * Adapter to convert KeyedPooledObjectFactory to PooledObjectFactory.
     * 将KeyedPooledObjectFactory转换为PooledObjectFactory的适配器。
     */
    private record KeyedFactoryAdapter<K, V>(
            KeyedPooledObjectFactory<K, V> factory,
            K key
    ) implements PooledObjectFactory<V> {

        @Override
        public PooledObject<V> makeObject() throws OpenPoolException {
            return factory.makeObject(key);
        }

        @Override
        public void destroyObject(PooledObject<V> obj) throws OpenPoolException {
            factory.destroyObject(key, obj);
        }

        @Override
        public boolean validateObject(PooledObject<V> obj) {
            return factory.validateObject(key, obj);
        }

        @Override
        public void activateObject(PooledObject<V> obj) throws OpenPoolException {
            factory.activateObject(key, obj);
        }

        @Override
        public void passivateObject(PooledObject<V> obj) throws OpenPoolException {
            factory.passivateObject(key, obj);
        }
    }
}
