package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.OpenLock;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.local.LocalReadWriteLock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lock Manager for Centralized Lock Creation and Management
 * 集中式锁创建和管理的锁管理器
 *
 * <p>Provides centralized management for creating and retrieving locks by name,
 * supporting both local locks and read-write locks.</p>
 * <p>提供按名称创建和检索锁的集中管理，支持本地锁和读写锁。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named lock management - 命名锁管理</li>
 *   <li>Lazy lock creation - 延迟锁创建</li>
 *   <li>Local and read-write lock support - 支持本地锁和读写锁</li>
 *   <li>Lock lifecycle management - 锁生命周期管理</li>
 *   <li>AutoCloseable for cleanup - AutoCloseable支持清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create lock manager | 创建锁管理器
 * LockManager manager = new LockManager();
 *
 * // Get or create a local lock | 获取或创建本地锁
 * Lock<Long> lock = manager.getLocalLock("user:123");
 * lock.execute(() -> updateUser("123"));
 *
 * // Get or create a read-write lock | 获取或创建读写锁
 * ReadWriteLock<Long> rwLock = manager.getLocalReadWriteLock("config");
 * rwLock.executeRead(() -> loadConfig());
 *
 * // Direct execute | 直接执行
 * manager.executeWithLocalLock("order:456", () -> processOrder("456"));
 *
 * // Cleanup | 清理
 * manager.close();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see ReadWriteLock
 * @see OpenLock#manager()
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class LockManager implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(LockManager.class.getName());

    private final ConcurrentMap<String, Lock<?>> localLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ReadWriteLock<?>> rwLocks = new ConcurrentHashMap<>();
    private final LockConfig localConfig;

    /**
     * Creates a lock manager with default configuration
     * 使用默认配置创建锁管理器
     */
    public LockManager() {
        this(LockConfig.defaults());
    }

    /**
     * Creates a lock manager with specified configuration
     * 使用指定配置创建锁管理器
     *
     * @param localConfig the configuration for local locks | 本地锁配置
     */
    public LockManager(LockConfig localConfig) {
        this.localConfig = localConfig;
    }

    /**
     * Gets or creates a local lock by name
     * 按名称获取或创建本地锁
     *
     * @param name the lock name | 锁名称
     * @return the lock | 锁
     */
    @SuppressWarnings("unchecked")
    public Lock<Long> getLocalLock(String name) {
        return (Lock<Long>) localLocks.computeIfAbsent(name,
                k -> new LocalLock(localConfig));
    }

    /**
     * Gets or creates a local read-write lock by name
     * 按名称获取或创建本地读写锁
     *
     * @param name the lock name | 锁名称
     * @return the read-write lock | 读写锁
     */
    @SuppressWarnings("unchecked")
    public ReadWriteLock<Long> getLocalReadWriteLock(String name) {
        return (ReadWriteLock<Long>) rwLocks.computeIfAbsent(name,
                k -> new LocalReadWriteLock(localConfig));
    }

    /**
     * Executes action with a named local lock
     * 使用命名本地锁执行操作
     *
     * @param name   the lock name | 锁名称
     * @param action the action to execute | 要执行的操作
     */
    public void executeWithLocalLock(String name, Runnable action) {
        getLocalLock(name).execute(action);
    }

    /**
     * Checks if a lock with the given name exists
     * 检查给定名称的锁是否存在
     *
     * @param name the lock name | 锁名称
     * @return true if the lock exists | true表示锁存在
     */
    public boolean hasLock(String name) {
        return localLocks.containsKey(name) || rwLocks.containsKey(name);
    }

    /**
     * Removes a lock by name and closes it
     * 按名称移除锁并关闭它
     *
     * @param name the lock name | 锁名称
     * @return true if the lock was removed | true表示锁已移除
     */
    public boolean removeLock(String name) {
        Lock<?> lock = localLocks.remove(name);
        ReadWriteLock<?> rwLock = rwLocks.remove(name);

        if (lock != null) {
            try {
                lock.close();
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING, "Failed to close lock '" + name + "'", e);
            }
        }

        return lock != null || rwLock != null;
    }

    /**
     * Gets the names of all managed locks
     * 获取所有托管锁的名称
     *
     * @return unmodifiable set of lock names | 不可修改的锁名称集合
     */
    public Set<String> getManagedLockNames() {
        Set<String> names = new HashSet<>();
        names.addAll(localLocks.keySet());
        names.addAll(rwLocks.keySet());
        return Collections.unmodifiableSet(names);
    }

    /**
     * Gets the total count of managed locks
     * 获取托管锁的总数
     *
     * @return the count | 数量
     */
    public int getManagedLockCount() {
        return localLocks.size() + rwLocks.size();
    }

    @Override
    public void close() {
        localLocks.values().forEach(lock -> {
            try {
                lock.close();
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING, "Failed to close lock during shutdown", e);
            }
        });
        localLocks.clear();

        rwLocks.values().forEach(rwLock -> {
            try {
                rwLock.readLock().close();
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING, "Failed to close read lock during shutdown", e);
            }
            try {
                rwLock.writeLock().close();
            } catch (Exception e) {
                LOG.log(System.Logger.Level.WARNING, "Failed to close write lock during shutdown", e);
            }
        });
        rwLocks.clear();
    }
}
