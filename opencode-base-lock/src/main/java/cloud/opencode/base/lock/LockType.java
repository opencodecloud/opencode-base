package cloud.opencode.base.lock;

/**
 * Lock Type Enumeration
 * 锁类型枚举
 *
 * <p>Defines the different types of locks available in the lock component.</p>
 * <p>定义锁组件中可用的不同锁类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type identification for lock configuration - 锁配置的类型标识</li>
 *   <li>Factory method selection support - 工厂方法选择支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Select a lock type for configuration
 * // 选择锁类型进行配置
 * LockType type = LockType.REENTRANT;
 * Lock lock = LockManager.create(type);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockConfig
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public enum LockType {

    /**
     * Reentrant lock - allows recursive locking by same thread
     * 可重入锁 - 允许同一线程递归锁定
     */
    REENTRANT,

    /**
     * Read-write lock - allows concurrent readers, exclusive writers
     * 读写锁 - 允许并发读取，排他写入
     */
    READ_WRITE,

    /**
     * Stamped lock - supports optimistic read locking
     * 印戳锁 - 支持乐观读锁定
     */
    STAMPED,

    /**
     * Spin lock - busy-waits for short critical sections
     * 自旋锁 - 用于短临界区的忙等待
     */
    SPIN,

    /**
     * Segment lock - partitioned locks for fine-grained control
     * 分段锁 - 用于细粒度控制的分区锁
     */
    SEGMENT
}
