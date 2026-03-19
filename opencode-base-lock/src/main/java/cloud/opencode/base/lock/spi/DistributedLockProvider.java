package cloud.opencode.base.lock.spi;

import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.distributed.DistributedLock;
import cloud.opencode.base.lock.distributed.DistributedLockConfig;

/**
 * Distributed Lock Provider Service Provider Interface (SPI)
 * 分布式锁提供者服务提供者接口（SPI）
 *
 * <p>Service Provider Interface for implementing distributed locks
 * using various backends such as Redis, Zookeeper, Etcd, or databases.</p>
 * <p>用于使用各种后端（如Redis、Zookeeper、Etcd或数据库）实现分布式锁的服务提供者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pluggable lock backends - 可插拔的锁后端</li>
 *   <li>Lock and read-write lock support - 支持锁和读写锁</li>
 *   <li>Availability checking - 可用性检查</li>
 *   <li>Graceful shutdown - 优雅关闭</li>
 * </ul>
 *
 * <p><strong>Implementation Examples | 实现示例:</strong></p>
 * <pre>{@code
 * // Implement for Redis | Redis实现
 * public class RedisLockProvider implements DistributedLockProvider {
 *     private final RedisClient client;
 *
 *     @Override
 *     public String getName() {
 *         return "redis";
 *     }
 *
 *     @Override
 *     public DistributedLock createLock(String lockName, DistributedLockConfig config) {
 *         return new RedisDistributedLock(client, lockName, config);
 *     }
 *
 *     @Override
 *     public boolean isAvailable() {
 *         return client.isConnected();
 *     }
 *
 *     @Override
 *     public void shutdown() {
 *         client.close();
 *     }
 * }
 *
 * // Register in META-INF/services | 在META-INF/services中注册
 * // cloud.opencode.base.lock.spi.DistributedLockProvider
 * // com.example.RedisLockProvider
 * }</pre>
 *
 * <p><strong>Supported Backends | 支持的后端:</strong></p>
 * <ul>
 *   <li>Redis - distributed in-memory store | 分布式内存存储</li>
 *   <li>Zookeeper - coordination service | 协调服务</li>
 *   <li>Etcd - distributed key-value store | 分布式键值存储</li>
 *   <li>Database - relational databases | 关系型数据库</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DistributedLock
 * @see DistributedLockConfig
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public interface DistributedLockProvider {

    /**
     * Gets the unique name of this provider
     * 获取此提供者的唯一名称
     *
     * @return the provider name (e.g., "redis", "zookeeper", "etcd") | 提供者名称（如"redis"、"zookeeper"、"etcd"）
     */
    String getName();

    /**
     * Creates a new distributed lock with the specified name and configuration
     * 使用指定名称和配置创建新的分布式锁
     *
     * @param lockName the lock name | 锁名称
     * @param config   the lock configuration | 锁配置
     * @return the distributed lock | 分布式锁
     */
    DistributedLock createLock(String lockName, DistributedLockConfig config);

    /**
     * Creates a distributed read-write lock
     * 创建分布式读写锁
     *
     * @param lockName the lock name | 锁名称
     * @param config   the lock configuration | 锁配置
     * @return the read-write lock | 读写锁
     * @throws UnsupportedOperationException if not supported by this provider | 如果此提供者不支持则抛出异常
     */
    default ReadWriteLock<String> createReadWriteLock(String lockName, DistributedLockConfig config) {
        throw new UnsupportedOperationException("Read-write lock not supported by " + getName());
    }

    /**
     * Checks if this provider is currently available
     * 检查此提供者当前是否可用
     *
     * @return true if available and ready to create locks | true表示可用并准备好创建锁
     */
    boolean isAvailable();

    /**
     * Shuts down the provider and releases resources
     * 关闭提供者并释放资源
     */
    default void shutdown() {
        // Default no-op
    }
}
