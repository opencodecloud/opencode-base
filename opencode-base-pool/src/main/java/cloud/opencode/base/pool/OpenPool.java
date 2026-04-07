package cloud.opencode.base.pool;

import cloud.opencode.base.pool.factory.KeyedPooledObjectFactory;
import cloud.opencode.base.pool.factory.SimplePooledObjectFactory;
import cloud.opencode.base.pool.impl.GenericKeyedObjectPool;
import cloud.opencode.base.pool.impl.GenericObjectPool;
import cloud.opencode.base.pool.impl.SoftReferencePool;
import cloud.opencode.base.pool.impl.ThreadLocalPool;
import cloud.opencode.base.pool.impl.VirtualThreadPool;
import cloud.opencode.base.pool.policy.EvictionPolicy;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * OpenPool - Pool Component Facade Entry Class
 * OpenPool - 池组件门面入口类
 *
 * <p>Provides simplified APIs for creating and managing object pools.
 * This is the main entry point for the pool component.</p>
 * <p>提供创建和管理对象池的简化API。这是池组件的主要入口点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for all pool types - 所有池类型的工厂方法</li>
 *   <li>Configuration builder access - 配置构建器访问</li>
 *   <li>Eviction policy helpers - 驱逐策略辅助方法</li>
 *   <li>Fluent API design - 流式API设计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simplest: create pool from Supplier
 * ObjectPool<StringBuilder> pool = OpenPool.createPool(StringBuilder::new);
 *
 * // Create pool from Supplier + Consumer (with destroyer)
 * ObjectPool<Connection> pool = OpenPool.createPool(
 *     () -> DriverManager.getConnection(url),
 *     Connection::close);
 *
 * // Create generic pool with factory
 * ObjectPool<Connection> pool = OpenPool.createPool(factory);
 *
 * // Create pool with custom config
 * ObjectPool<Connection> pool = OpenPool.createPool(factory,
 *     OpenPool.configBuilder()
 *         .maxTotal(20)
 *         .testOnBorrow(true)
 *         .build());
 *
 * // Create keyed pool
 * KeyedObjectPool<String, Connection> keyedPool =
 *     OpenPool.createKeyedPool(keyedFactory, config);
 *
 * // Create eviction policy (with max age for connection recycling)
 * EvictionPolicy<Connection> policy = OpenPool.allEviction(
 *     OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
 *     OpenPool.maxAgeEviction(Duration.ofHours(1)));
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility class) - 线程安全: 是（工具类）</li>
 *   <li>Null-safe: No (null parameters not accepted) - 空值安全: 否（不接受空参数）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public final class OpenPool {

    private OpenPool() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Generic Pool Factory Methods ====================

    /**
     * Creates a generic object pool with default configuration.
     * 使用默认配置创建通用对象池。
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(PooledObjectFactory<T> factory) {
        return new GenericObjectPool<>(factory, PoolConfig.defaults());
    }

    /**
     * Creates a generic object pool with custom configuration.
     * 使用自定义配置创建通用对象池。
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @param config  the pool configuration - 池配置
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(
            PooledObjectFactory<T> factory,
            PoolConfig config) {
        return new GenericObjectPool<>(factory, config);
    }

    // ==================== Simplified Pool Factory Methods ====================

    /**
     * Creates a pool from a Supplier (simplest API).
     * 从Supplier创建池（最简API）。
     *
     * @param <T>     the object type - 对象类型
     * @param creator the object creator - 对象创建器
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(Supplier<T> creator) {
        return createPool(SimplePooledObjectFactory.of(creator));
    }

    /**
     * Creates a pool from a Supplier with a destroyer Consumer.
     * 从Supplier和销毁Consumer创建池。
     *
     * @param <T>       the object type - 对象类型
     * @param creator   the object creator - 对象创建器
     * @param destroyer the object destroyer - 对象销毁器
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(Supplier<T> creator, Consumer<T> destroyer) {
        return createPool(SimplePooledObjectFactory.of(creator, destroyer));
    }

    /**
     * Creates a pool from a Supplier with custom configuration.
     * 从Supplier和自定义配置创建池。
     *
     * @param <T>     the object type - 对象类型
     * @param creator the object creator - 对象创建器
     * @param config  the pool configuration - 池配置
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(Supplier<T> creator, PoolConfig config) {
        return createPool(SimplePooledObjectFactory.of(creator), config);
    }

    /**
     * Creates a pool from a Supplier with a destroyer and custom configuration.
     * 从Supplier、销毁Consumer和自定义配置创建池。
     *
     * @param <T>       the object type - 对象类型
     * @param creator   the object creator - 对象创建器
     * @param destroyer the object destroyer - 对象销毁器
     * @param config    the pool configuration - 池配置
     * @return the object pool - 对象池
     */
    public static <T> ObjectPool<T> createPool(Supplier<T> creator, Consumer<T> destroyer, PoolConfig config) {
        return createPool(SimplePooledObjectFactory.of(creator, destroyer), config);
    }

    // ==================== Keyed Pool Factory Methods ====================

    /**
     * Creates a keyed object pool with default configuration.
     * 使用默认配置创建键控对象池。
     *
     * @param <K>     the key type - 键类型
     * @param <V>     the value type - 值类型
     * @param factory the keyed factory - 键控工厂
     * @return the keyed pool - 键控池
     */
    public static <K, V> KeyedObjectPool<K, V> createKeyedPool(
            KeyedPooledObjectFactory<K, V> factory) {
        return new GenericKeyedObjectPool<>(factory, PoolConfig.defaults());
    }

    /**
     * Creates a keyed object pool with custom configuration.
     * 使用自定义配置创建键控对象池。
     *
     * @param <K>     the key type - 键类型
     * @param <V>     the value type - 值类型
     * @param factory the keyed factory - 键控工厂
     * @param config  the pool configuration - 池配置
     * @return the keyed pool - 键控池
     */
    public static <K, V> KeyedObjectPool<K, V> createKeyedPool(
            KeyedPooledObjectFactory<K, V> factory,
            PoolConfig config) {
        return new GenericKeyedObjectPool<>(factory, config);
    }

    // ==================== Specialized Pool Factory Methods ====================

    /**
     * Creates a thread-local pool (one object per thread).
     * 创建线程本地池（每线程一个对象）。
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @return the thread-local pool - 线程本地池
     */
    public static <T> ObjectPool<T> createThreadLocalPool(
            PooledObjectFactory<T> factory) {
        return new ThreadLocalPool<>(factory);
    }

    /**
     * Creates a soft reference pool (GC-friendly).
     * 创建软引用池（GC友好）。
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @return the soft reference pool - 软引用池
     */
    public static <T> ObjectPool<T> createSoftReferencePool(
            PooledObjectFactory<T> factory) {
        return new SoftReferencePool<>(factory);
    }

    /**
     * Creates a soft reference pool with custom configuration.
     * 使用自定义配置创建软引用池。
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @param config  the pool configuration - 池配置
     * @return the soft reference pool - 软引用池
     */
    public static <T> ObjectPool<T> createSoftReferencePool(
            PooledObjectFactory<T> factory,
            PoolConfig config) {
        return new SoftReferencePool<>(factory, config);
    }

    /**
     * Creates a virtual thread optimized pool with default configuration.
     * 使用默认配置创建虚拟线程优化的池。
     *
     * <p>This pool is optimized for Virtual Threads with ScopedValue context propagation.</p>
     * <p>此池针对虚拟线程进行了优化，支持 ScopedValue 上下文传播。</p>
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @return the virtual thread pool - 虚拟线程池
     */
    public static <T> ObjectPool<T> createVirtualThreadPool(
            PooledObjectFactory<T> factory) {
        return new VirtualThreadPool<>(factory, PoolConfig.defaults());
    }

    /**
     * Creates a virtual thread optimized pool with custom configuration.
     * 使用自定义配置创建虚拟线程优化的池。
     *
     * <p>This pool is optimized for Virtual Threads with ScopedValue context propagation.</p>
     * <p>此池针对虚拟线程进行了优化，支持 ScopedValue 上下文传播。</p>
     *
     * @param <T>     the object type - 对象类型
     * @param factory the object factory - 对象工厂
     * @param config  the pool configuration - 池配置
     * @return the virtual thread pool - 虚拟线程池
     */
    public static <T> ObjectPool<T> createVirtualThreadPool(
            PooledObjectFactory<T> factory,
            PoolConfig config) {
        return new VirtualThreadPool<>(factory, config);
    }

    // ==================== Configuration Methods ====================

    /**
     * Creates a pool configuration builder.
     * 创建池配置构建器。
     *
     * @return the builder - 构建器
     */
    public static PoolConfig.Builder configBuilder() {
        return PoolConfig.builder();
    }

    /**
     * Gets the default pool configuration.
     * 获取默认池配置。
     *
     * @return the default config - 默认配置
     */
    public static PoolConfig defaultConfig() {
        return PoolConfig.defaults();
    }

    // ==================== Eviction Policy Factory Methods ====================

    /**
     * Creates an idle time eviction policy.
     * 创建空闲时间驱逐策略。
     *
     * @param <T>         the object type - 对象类型
     * @param maxIdleTime the maximum idle time - 最大空闲时间
     * @return the eviction policy - 驱逐策略
     */
    public static <T> EvictionPolicy<T> idleTimeEviction(Duration maxIdleTime) {
        return new EvictionPolicy.IdleTime<>(maxIdleTime);
    }

    /**
     * Creates an LRU eviction policy.
     * 创建LRU驱逐策略。
     *
     * @param <T>        the object type - 对象类型
     * @param maxObjects the maximum objects to keep - 保留的最大对象数
     * @return the eviction policy - 驱逐策略
     */
    public static <T> EvictionPolicy<T> lruEviction(int maxObjects) {
        return new EvictionPolicy.LRU<>(maxObjects);
    }

    /**
     * Creates an LFU eviction policy.
     * 创建LFU驱逐策略。
     *
     * @param <T>            the object type - 对象类型
     * @param minBorrowCount the minimum borrow count - 最小借用次数
     * @return the eviction policy - 驱逐策略
     */
    public static <T> EvictionPolicy<T> lfuEviction(long minBorrowCount) {
        return new EvictionPolicy.LFU<>(minBorrowCount);
    }

    /**
     * Creates a composite eviction policy (all must match).
     * 创建组合驱逐策略（全部必须匹配）。
     *
     * @param <T>      the object type - 对象类型
     * @param policies the policies to combine - 要组合的策略
     * @return the eviction policy - 驱逐策略
     */
    @SafeVarargs
    public static <T> EvictionPolicy<T> allEviction(EvictionPolicy<T>... policies) {
        return new EvictionPolicy.Composite<>(List.of(policies), true);
    }

    /**
     * Creates a composite eviction policy (any can match).
     * 创建组合驱逐策略（任一匹配即可）。
     *
     * @param <T>      the object type - 对象类型
     * @param policies the policies to combine - 要组合的策略
     * @return the eviction policy - 驱逐策略
     */
    @SafeVarargs
    public static <T> EvictionPolicy<T> anyEviction(EvictionPolicy<T>... policies) {
        return new EvictionPolicy.Composite<>(List.of(policies), false);
    }

    /**
     * Creates a max age eviction policy.
     * 创建最大生命周期驱逐策略。
     *
     * <p>Evicts objects that have exceeded the specified lifetime since creation.
     * Essential for database connections that must be recycled periodically.</p>
     * <p>驱逐自创建以来超过指定生命周期的对象。
     * 对于必须定期回收的数据库连接至关重要。</p>
     *
     * @param <T>         the object type - 对象类型
     * @param maxLifetime the maximum object lifetime - 最大对象生命周期
     * @return the eviction policy - 驱逐策略
     */
    public static <T> EvictionPolicy<T> maxAgeEviction(Duration maxLifetime) {
        return new EvictionPolicy.MaxAge<>(maxLifetime);
    }
}
