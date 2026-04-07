package cloud.opencode.base.lock;

import cloud.opencode.base.lock.distributed.DistributedLockConfig;
import cloud.opencode.base.lock.event.LockListener;
import cloud.opencode.base.lock.event.ObservableLock;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.local.LocalReadWriteLock;
import cloud.opencode.base.lock.local.RetryLock;
import cloud.opencode.base.lock.local.SegmentLock;
import cloud.opencode.base.lock.local.SpinLock;
import cloud.opencode.base.lock.local.StampedLockAdapter;
import cloud.opencode.base.lock.local.TtlLock;
import cloud.opencode.base.lock.manager.LockGroup;
import cloud.opencode.base.lock.manager.LockManager;
import cloud.opencode.base.lock.manager.NamedLockFactory;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * OpenLock Facade - Unified Entry Point for Lock Component
 * OpenLock 门面 - 锁组件统一入口
 *
 * <p>Provides unified API for creating and managing various types of locks.</p>
 * <p>提供创建和管理各种类型锁的统一API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Local reentrant locks - 本地可重入锁</li>
 *   <li>Read-write locks - 读写锁</li>
 *   <li>Spin locks for short critical sections - 短临界区自旋锁</li>
 *   <li>Segment locks for fine-grained locking - 细粒度分段锁</li>
 *   <li>Stamped locks with optimistic reads - 支持乐观读的戳记锁</li>
 *   <li>Retry locks with exponential backoff - 指数退避重试锁</li>
 *   <li>TTL locks with auto-expiry - 带自动过期的TTL锁</li>
 *   <li>Observable locks with event listeners - 带事件监听的可观察锁</li>
 *   <li>Lock groups with deadlock prevention - 带死锁预防的锁组</li>
 *   <li>Named lock factory with striping - 带条纹的命名锁工厂</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a local lock | 创建本地锁
 * Lock<Long> lock = OpenLock.lock();
 *
 * // Execute with lock | 使用锁执行
 * lock.execute(() -> {
 *     // Critical section | 临界区
 * });
 *
 * // Create read-write lock | 创建读写锁
 * ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
 * rwLock.executeRead(() -> loadData());
 * rwLock.executeWrite(() -> saveData());
 *
 * // Create spin lock for short critical sections | 为短临界区创建自旋锁
 * Lock<Long> spinLock = OpenLock.spinLock();
 *
 * // Create segment lock for key-based locking | 为基于键的锁定创建分段锁
 * SegmentLock<String> segmentLock = OpenLock.segmentLock(32);
 * segmentLock.execute("user:123", () -> updateUser("123"));
 *
 * // Create named lock factory | 创建命名锁工厂
 * NamedLockFactory factory = OpenLock.namedLockFactory();
 * factory.execute("order:12345", () -> processOrder("12345"));
 *
 * // Create lock group (deadlock prevention) | 创建锁组（死锁预防）
 * try (var guard = OpenLock.lockGroup()
 *         .add(lockA).add(lockB)
 *         .build().lockAll()) {
 *     transferFunds(accountA, accountB);
 * }
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
 * @see LockGroup
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public final class OpenLock {

    private static volatile LockManager defaultManager;

    private OpenLock() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Local Locks | 本地锁 ====================

    /**
     * Creates a local reentrant lock with default configuration
     * 使用默认配置创建本地可重入锁
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Lock<Long> lock = OpenLock.lock();
     * lock.execute(() -> doWork());
     * }</pre>
     *
     * @return the local lock | 本地锁
     */
    public static Lock<Long> lock() {
        return new LocalLock();
    }

    /**
     * Creates a local lock with specified configuration
     * 使用指定配置创建本地锁
     *
     * @param config the lock configuration | 锁配置
     * @return the local lock | 本地锁
     */
    public static Lock<Long> lock(LockConfig config) {
        return new LocalLock(config);
    }

    /**
     * Creates a fair lock that grants access in FIFO order
     * 创建按FIFO顺序授予访问权限的公平锁
     *
     * <p>Fair locks may have lower throughput but prevent starvation.</p>
     * <p>公平锁可能具有较低的吞吐量，但可以防止饥饿。</p>
     *
     * @return the fair lock | 公平锁
     */
    public static Lock<Long> fairLock() {
        return new LocalLock(LockConfig.builder().fair(true).build());
    }

    /**
     * Creates a read-write lock allowing concurrent readers
     * 创建允许并发读取的读写锁
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
     * String data = rwLock.executeRead(() -> loadData());
     * rwLock.executeWrite(() -> saveData(newData));
     * }</pre>
     *
     * @return the read-write lock | 读写锁
     */
    public static ReadWriteLock<Long> readWriteLock() {
        return new LocalReadWriteLock();
    }

    /**
     * Creates a read-write lock with specified configuration
     * 使用指定配置创建读写锁
     *
     * @param config the lock configuration | 锁配置
     * @return the read-write lock | 读写锁
     */
    public static ReadWriteLock<Long> readWriteLock(LockConfig config) {
        return new LocalReadWriteLock(config);
    }

    /**
     * Creates a spin lock for very short critical sections
     * 为极短临界区创建自旋锁
     *
     * <p><strong>Performance | 性能特性:</strong></p>
     * <p>Best for critical sections measured in nanoseconds.
     * For longer operations, use regular locks.</p>
     * <p>最适合以纳秒为单位的临界区。对于较长操作，请使用常规锁。</p>
     *
     * @return the spin lock | 自旋锁
     */
    public static Lock<Long> spinLock() {
        return new SpinLock();
    }

    /**
     * Creates a spin lock with custom max spin count
     * 使用自定义最大自旋次数创建自旋锁
     *
     * @param maxSpinCount the maximum spin count before yielding | 让步前的最大自旋次数
     * @return the spin lock | 自旋锁
     */
    public static Lock<Long> spinLock(int maxSpinCount) {
        return new SpinLock(LockConfig.builder().spinCount(maxSpinCount).build());
    }

    /**
     * Creates a segment lock with default 16 segments
     * 使用默认16个分段创建分段锁
     *
     * <p>Segment locks reduce contention by mapping keys to separate lock segments.</p>
     * <p>分段锁通过将键映射到单独的锁段来减少争用。</p>
     *
     * @param <K> the key type | 键类型
     * @return the segment lock | 分段锁
     */
    public static <K> SegmentLock<K> segmentLock() {
        return new SegmentLock<>();
    }

    /**
     * Creates a segment lock with specified number of segments
     * 使用指定分段数创建分段锁
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * SegmentLock<String> lock = OpenLock.segmentLock(32);
     * lock.execute("user:123", () -> updateUser("123"));
     * }</pre>
     *
     * @param <K>      the key type | 键类型
     * @param segments the number of segments | 分段数
     * @return the segment lock | 分段锁
     */
    public static <K> SegmentLock<K> segmentLock(int segments) {
        return new SegmentLock<>(segments);
    }

    // ==================== Stamped Lock | 戳记锁 ====================

    /**
     * Creates a stamped lock adapter with optimistic read support
     * 创建支持乐观读的戳记锁适配器
     *
     * <p>Stamped locks offer higher throughput for read-heavy workloads via optimistic reads.
     * Note: StampedLock is NOT reentrant and has limited virtual thread support.</p>
     * <p>戳记锁通过乐观读为读密集型工作负载提供更高吞吐量。
     * 注意：StampedLock 不可重入，且虚拟线程支持有限。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * StampedLockAdapter stampedLock = OpenLock.stampedLock();
     * String data = stampedLock.optimisticRead(() -> readData());
     * stampedLock.executeWrite(() -> writeData());
     * }</pre>
     *
     * @return the stamped lock adapter | 戳记锁适配器
     */
    public static StampedLockAdapter stampedLock() {
        return new StampedLockAdapter();
    }

    /**
     * Creates a stamped lock adapter with specified configuration
     * 使用指定配置创建戳记锁适配器
     *
     * @param config the lock configuration | 锁配置
     * @return the stamped lock adapter | 戳记锁适配器
     */
    public static StampedLockAdapter stampedLock(LockConfig config) {
        return new StampedLockAdapter(config);
    }

    // ==================== Retry Lock | 重试锁 ====================

    /**
     * Creates a retry lock wrapping a new local lock with default retry settings
     * 使用默认重试设置包装新本地锁创建重试锁
     *
     * <p>Retries lock acquisition with exponential backoff on failure.</p>
     * <p>锁获取失败时使用指数退避重试。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Lock<Long> retryLock = OpenLock.retryLock();
     * retryLock.execute(() -> doWork());
     * }</pre>
     *
     * @return the retry lock | 重试锁
     */
    public static Lock<Long> retryLock() {
        return new RetryLock<>(new LocalLock());
    }

    /**
     * Creates a retry lock builder wrapping the specified lock
     * 创建包装指定锁的重试锁构建器
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Lock<Long> retryLock = OpenLock.retryLock(existingLock)
     *     .maxRetries(5)
     *     .retryDelay(Duration.ofMillis(200))
     *     .backoffMultiplier(1.5)
     *     .build();
     * }</pre>
     *
     * @param <T>      the lock token type | 锁令牌类型
     * @param delegate the lock to wrap with retry logic | 要包装重试逻辑的锁
     * @return the retry lock builder | 重试锁构建器
     */
    public static <T> RetryLock.Builder<T> retryLock(Lock<T> delegate) {
        return RetryLock.builder(delegate);
    }

    // ==================== TTL Lock | TTL锁 ====================

    /**
     * Creates a lock with TTL (Time-To-Live) auto-expiry
     * 创建带 TTL（生存时间）自动过期的锁
     *
     * <p>If the lock is held beyond the TTL, it is marked as expired for monitoring.
     * The actual lock release still depends on the holder calling unlock().
     * Use {@link TtlLock#isExpired()} to detect and handle expired locks.</p>
     * <p>如果锁持有超过TTL，将被标记为过期以供监控。
     * 实际锁释放仍取决于持有者调用 unlock()。
     * 使用 {@link TtlLock#isExpired()} 检测和处理过期锁。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Lock<Long> ttlLock = OpenLock.ttlLock(Duration.ofSeconds(30));
     * ttlLock.execute(() -> processTask());
     * }</pre>
     *
     * @param ttl the maximum time a lock can be held | 锁可持有的最长时间
     * @return the TTL lock | TTL锁
     */
    public static Lock<Long> ttlLock(Duration ttl) {
        return new TtlLock(ttl);
    }

    /**
     * Creates a fair TTL lock
     * 创建公平TTL锁
     *
     * @param ttl  the maximum time a lock can be held | 锁可持有的最长时间
     * @param fair whether the lock should be fair | 锁是否应该是公平的
     * @return the TTL lock | TTL锁
     */
    public static Lock<Long> ttlLock(Duration ttl, boolean fair) {
        return new TtlLock(ttl, fair);
    }

    // ==================== Observable Lock | 可观察锁 ====================

    /**
     * Creates an observable lock that fires events to listeners
     * 创建向监听器触发事件的可观察锁
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Lock<Long> observable = OpenLock.observableLock("myLock",
     *     event -> log.info("Lock event: {}", event));
     * observable.execute(() -> doWork());
     * }</pre>
     *
     * @param lockName  the lock name for event identification | 用于事件标识的锁名称
     * @param listeners the event listeners | 事件监听器
     * @return the observable lock | 可观察锁
     */
    public static Lock<Long> observableLock(String lockName, LockListener... listeners) {
        return new ObservableLock<>(new LocalLock(), lockName, listeners);
    }

    /**
     * Wraps an existing lock with event observation
     * 用事件观察包装现有锁
     *
     * @param <T>       the lock token type | 锁令牌类型
     * @param delegate  the lock to observe | 要观察的锁
     * @param lockName  the lock name for event identification | 用于事件标识的锁名称
     * @param listeners the event listeners | 事件监听器
     * @return the observable lock | 可观察锁
     */
    public static <T> Lock<T> observableLock(Lock<T> delegate, String lockName, LockListener... listeners) {
        return new ObservableLock<>(delegate, lockName, listeners);
    }

    // ==================== Named Lock Factory | 命名锁工厂 ====================

    /**
     * Creates a named lock factory with default settings
     * 使用默认设置创建命名锁工厂
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * NamedLockFactory factory = OpenLock.namedLockFactory();
     * factory.execute("order:12345", () -> processOrder("12345"));
     * }</pre>
     *
     * @return the named lock factory | 命名锁工厂
     */
    public static NamedLockFactory namedLockFactory() {
        return new NamedLockFactory();
    }

    /**
     * Creates a named lock factory with specified stripe count
     * 使用指定条纹数创建命名锁工厂
     *
     * @param stripes the number of stripes | 条纹数
     * @return the named lock factory | 命名锁工厂
     */
    public static NamedLockFactory namedLockFactory(int stripes) {
        return new NamedLockFactory(stripes, true, LockConfig.defaults());
    }

    // ==================== Lock Group | 锁组 ====================

    /**
     * Creates a lock group builder for atomic multi-lock acquisition
     * 创建用于原子多锁获取的锁组构建器
     *
     * <p>Lock groups prevent deadlocks by acquiring locks in consistent order.</p>
     * <p>锁组通过以一致的顺序获取锁来防止死锁。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * try (var guard = OpenLock.lockGroup()
     *         .add(lockA).add(lockB).add(lockC)
     *         .timeout(Duration.ofSeconds(10))
     *         .build().lockAll()) {
     *     // All locks acquired atomically | 原子获取所有锁
     *     transferFunds(accountA, accountB, accountC);
     * }
     * }</pre>
     *
     * @return the lock group builder | 锁组构建器
     */
    public static LockGroup.Builder lockGroup() {
        return LockGroup.builder();
    }

    // ==================== Lock Manager | 锁管理器 ====================

    /**
     * Gets the default singleton lock manager
     * 获取默认单例锁管理器
     *
     * @return the default lock manager | 默认锁管理器
     */
    public static LockManager manager() {
        if (defaultManager == null) {
            synchronized (OpenLock.class) {
                if (defaultManager == null) {
                    defaultManager = new LockManager();
                }
            }
        }
        return defaultManager;
    }

    /**
     * Creates a new lock manager with specified configuration
     * 使用指定配置创建新的锁管理器
     *
     * @param config the lock configuration | 锁配置
     * @return the lock manager | 锁管理器
     */
    public static LockManager manager(LockConfig config) {
        return new LockManager(config);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Executes action with a new temporary lock
     * 使用新的临时锁执行操作
     *
     * @param action the action to execute | 要执行的操作
     */
    public static void execute(Runnable action) {
        lock().execute(action);
    }

    /**
     * Executes supplier with a new temporary lock and returns result
     * 使用新的临时锁执行并返回结果
     *
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    public static <R> R executeWithResult(Supplier<R> supplier) {
        return lock().executeWithResult(supplier);
    }

    // ==================== Configuration Builders | 配置构建器 ====================

    /**
     * Creates a lock configuration builder
     * 创建锁配置构建器
     *
     * @return the configuration builder | 配置构建器
     */
    public static LockConfig.Builder configBuilder() {
        return LockConfig.builder();
    }

    /**
     * Gets default lock configuration
     * 获取默认锁配置
     *
     * @return the default configuration | 默认配置
     */
    public static LockConfig defaultConfig() {
        return LockConfig.defaults();
    }

    /**
     * Creates a distributed lock configuration builder
     * 创建分布式锁配置构建器
     *
     * @return the configuration builder | 配置构建器
     */
    public static DistributedLockConfig.Builder distributedConfigBuilder() {
        return DistributedLockConfig.builder();
    }

    /**
     * Gets default distributed lock configuration
     * 获取默认分布式锁配置
     *
     * @return the default configuration | 默认配置
     */
    public static DistributedLockConfig defaultDistributedConfig() {
        return DistributedLockConfig.defaults();
    }
}
