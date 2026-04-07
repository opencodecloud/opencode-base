package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;

import java.util.function.Supplier;

/**
 * Segment Lock Implementation for Fine-Grained Locking
 * 细粒度锁定的分段锁实现
 *
 * <p>Divides the lock space into multiple segments to reduce lock contention.
 * Keys are mapped to segments using consistent hashing, allowing operations
 * on different keys to proceed concurrently.</p>
 * <p>将锁空间划分为多个分段以减少锁争用。使用一致性哈希将键映射到分段，
 * 允许对不同键的操作并发进行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Reduces lock contention through partitioning - 通过分区减少锁争用</li>
 *   <li>Consistent hash-based key mapping - 基于一致性哈希的键映射</li>
 *   <li>Configurable number of segments - 可配置的分段数量</li>
 *   <li>Per-key locking semantics - 按键锁定语义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create segment lock with 32 segments | 创建32个分段的分段锁
 * SegmentLock<String> lock = new SegmentLock<>(32);
 *
 * // Different keys may map to different segments (concurrent) | 不同键可能映射到不同分段（并发）
 * lock.execute("user:123", () -> updateUser("123"));
 * lock.execute("user:456", () -> updateUser("456"));
 *
 * // Get result with lock | 带锁获取结果
 * User user = lock.executeWithResult("user:123", () -> loadUser("123"));
 *
 * // Access underlying lock | 访问底层锁
 * Lock<Long> userLock = lock.getLock("user:123");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Higher concurrency with more segments - 更多分段提供更高并发性</li>
 *   <li>Memory trade-off: more segments = more memory - 内存权衡：更多分段=更多内存</li>
 *   <li>Recommended: segments = 2x expected concurrent threads - 建议：分段数=预期并发线程数的2倍</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Virtual Thread friendly: Yes - 虚拟线程友好: 是</li>
 * </ul>
 *
 * @param <K> the key type | 键类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see LocalLock
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class SegmentLock<K> {

    private final int segments;
    private final int mask;
    private final Lock<Long>[] locks;
    private final LockConfig config;

    /**
     * Creates a segment lock with default 16 segments
     * 使用默认16个分段创建分段锁
     */
    public SegmentLock() {
        this(16);
    }

    /**
     * Creates a segment lock with specified number of segments
     * 使用指定分段数创建分段锁
     *
     * @param segments the number of segments | 分段数
     */
    public SegmentLock(int segments) {
        this(segments, LockConfig.defaults());
    }

    /**
     * Creates a segment lock with specified segments and configuration
     * 使用指定分段数和配置创建分段锁
     *
     * @param segments the number of segments (must be positive) | 分段数（必须为正数）
     * @param config   the lock configuration | 锁配置
     * @throws IllegalArgumentException if segments is not positive | 如果分段数非正数则抛出异常
     */
    @SuppressWarnings("unchecked")
    public SegmentLock(int segments, LockConfig config) {
        if (segments <= 0) {
            throw new IllegalArgumentException("Segments must be positive");
        }
        // Round up to power of two for fast bitwise indexing
        int actualSegments = Integer.highestOneBit(segments - 1) << 1;
        if (actualSegments <= 0) actualSegments = 1;
        this.segments = actualSegments;
        this.mask = actualSegments - 1;
        this.config = config;
        this.locks = new Lock[actualSegments];
        for (int i = 0; i < actualSegments; i++) {
            this.locks[i] = new LocalLock(config);
        }
    }

    /**
     * Gets the lock for the specified key
     * 获取指定键对应的锁
     *
     * @param key the key | 键
     * @return the lock for this key | 此键对应的锁
     */
    public Lock<Long> getLock(K key) {
        return locks[key.hashCode() & mask];
    }

    /**
     * Executes action with the lock for the specified key
     * 使用指定键对应的锁执行操作
     *
     * @param key    the key | 键
     * @param action the action to execute | 要执行的操作
     */
    public void execute(K key, Runnable action) {
        getLock(key).execute(action);
    }

    /**
     * Executes supplier with the lock for the specified key and returns result
     * 使用指定键对应的锁执行并返回结果
     *
     * @param key      the key | 键
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    public <R> R executeWithResult(K key, Supplier<R> supplier) {
        return getLock(key).executeWithResult(supplier);
    }

    /**
     * Gets the total number of segments
     * 获取总分段数
     *
     * @return the number of segments | 分段数
     */
    public int getSegments() {
        return segments;
    }

    /**
     * Gets the segment index for the specified key
     * 获取指定键对应的分段索引
     *
     * @param key the key | 键
     * @return the segment index (0 to segments-1) | 分段索引（0到segments-1）
     */
    public int getSegmentIndex(K key) {
        return key.hashCode() & mask;
    }
}
