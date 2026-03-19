package cloud.opencode.base.lock.manager;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.local.LocalLock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Named Lock Factory with Striped Lock Pattern Support
 * 支持条纹锁模式的命名锁工厂
 *
 * <p>Creates fine-grained locks based on names, supporting striped lock pattern
 * to reduce memory usage while maintaining good concurrency.</p>
 * <p>基于名称创建细粒度锁，支持条纹锁模式以减少内存使用同时保持良好的并发性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Striped lock pattern for memory efficiency - 条纹锁模式提高内存效率</li>
 *   <li>Consistent hashing for name-to-lock mapping - 一致性哈希的名称到锁映射</li>
 *   <li>Optional unlimited named locks mode - 可选的无限命名锁模式</li>
 *   <li>Configurable stripe count - 可配置的条纹数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create factory with 64 stripes | 创建64条纹的工厂
 * NamedLockFactory factory = new NamedLockFactory(64);
 *
 * // Execute with named lock | 使用命名锁执行
 * factory.execute("order:12345", () -> processOrder("12345"));
 *
 * // Different keys may use different locks (concurrent) | 不同键可能使用不同锁（并发）
 * factory.execute("order:67890", () -> processOrder("67890"));
 *
 * // Get result | 获取结果
 * Order order = factory.executeWithResult("order:12345", () -> loadOrder("12345"));
 *
 * // Disable striping for unlimited locks | 禁用条纹获取无限锁
 * NamedLockFactory unlimited = new NamedLockFactory(0, false, LockConfig.defaults());
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Striped mode: Fixed memory, good concurrency - 条纹模式：固定内存，良好并发</li>
 *   <li>Non-striped mode: Unlimited locks, higher memory - 非条纹模式：无限锁，更高内存</li>
 *   <li>Recommended stripes = 2x concurrent threads - 建议条纹数=并发线程数的2倍</li>
 * </ul>
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
 * @see OpenLock#namedLockFactory()
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public class NamedLockFactory {

    private final int stripes;
    private final Lock<Long>[] stripedLocks;
    private final ConcurrentMap<String, Lock<Long>> namedLocks;
    private final LockConfig config;
    private final boolean useStriping;

    /**
     * Creates a factory with default 64 stripes and striping enabled
     * 使用默认64条纹和启用条纹模式创建工厂
     */
    public NamedLockFactory() {
        this(64, true, LockConfig.defaults());
    }

    /**
     * Creates a factory with specified number of stripes
     * 使用指定条纹数创建工厂
     *
     * @param stripes the number of stripes | 条纹数
     */
    public NamedLockFactory(int stripes) {
        this(stripes, true, LockConfig.defaults());
    }

    /**
     * Creates a factory with full configuration
     * 使用完整配置创建工厂
     *
     * @param stripes     the number of stripes (must be positive) | 条纹数（必须为正数）
     * @param useStriping whether to use striping (false = unlimited named locks) | 是否使用条纹（false=无限命名锁）
     * @param config      the lock configuration | 锁配置
     * @throws IllegalArgumentException if stripes is not positive | 如果条纹数非正数则抛出异常
     */
    @SuppressWarnings("unchecked")
    public NamedLockFactory(int stripes, boolean useStriping, LockConfig config) {
        if (stripes <= 0) {
            throw new IllegalArgumentException("Stripes must be positive");
        }
        this.stripes = stripes;
        this.useStriping = useStriping;
        this.config = config;
        this.namedLocks = new ConcurrentHashMap<>();

        if (useStriping) {
            this.stripedLocks = new Lock[stripes];
            for (int i = 0; i < stripes; i++) {
                this.stripedLocks[i] = new LocalLock(config);
            }
        } else {
            this.stripedLocks = null;
        }
    }

    /**
     * Gets a lock for the given name
     * 获取给定名称的锁
     *
     * <p>If striping is enabled, uses consistent hashing to map names to a
     * fixed pool of locks. Otherwise, creates a new lock per unique name.</p>
     * <p>如果启用条纹，使用一致性哈希将名称映射到固定的锁池。否则，为每个唯一名称创建新锁。</p>
     *
     * @param name the lock name | 锁名称
     * @return the lock | 锁
     */
    public Lock<Long> getLock(String name) {
        if (useStriping) {
            // Use bitwise AND to handle Integer.MIN_VALUE case safely
            // Math.abs(Integer.MIN_VALUE) returns Integer.MIN_VALUE (negative)
            int index = (name.hashCode() & 0x7FFFFFFF) % stripes;
            return stripedLocks[index];
        } else {
            return namedLocks.computeIfAbsent(name, k -> new LocalLock(config));
        }
    }

    /**
     * Executes action with the lock for the given name
     * 使用给定名称的锁执行操作
     *
     * @param name   the lock name | 锁名称
     * @param action the action to execute | 要执行的操作
     */
    public void execute(String name, Runnable action) {
        getLock(name).execute(action);
    }

    /**
     * Executes supplier with the lock for the given name and returns result
     * 使用给定名称的锁执行并返回结果
     *
     * @param name     the lock name | 锁名称
     * @param supplier the supplier to execute | 要执行的供应者
     * @param <R>      the result type | 结果类型
     * @return the result | 结果
     */
    public <R> R executeWithResult(String name, Supplier<R> supplier) {
        return getLock(name).executeWithResult(supplier);
    }

    /**
     * Gets the number of stripes configured
     * 获取配置的条纹数
     *
     * @return the number of stripes, or 0 if striping is disabled | 条纹数，如果禁用条纹则为0
     */
    public int getStripes() {
        return useStriping ? stripes : 0;
    }

    /**
     * Gets the number of named locks created
     * 获取已创建的命名锁数量
     *
     * @return the number of named locks (only meaningful if striping disabled) | 命名锁数量（仅在禁用条纹时有意义）
     */
    public int getNamedLockCount() {
        return namedLocks.size();
    }

    /**
     * Checks if striping mode is enabled
     * 检查是否启用条纹模式
     *
     * @return true if striping is enabled | true表示启用条纹
     */
    public boolean isStripingEnabled() {
        return useStriping;
    }
}
