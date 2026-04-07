package cloud.opencode.base.lock.event;

import java.time.Duration;
import java.time.Instant;

/**
 * Lock Lifecycle Event Record
 * 锁生命周期事件记录
 *
 * <p>An immutable record capturing lock lifecycle transitions such as
 * acquisition, release, timeout, and error events. Each event includes
 * the event type, lock name, originating thread information, and
 * optional wait-time duration.</p>
 * <p>一个不可变记录，用于捕获锁生命周期转换，例如获取、释放、超时和错误事件。
 * 每个事件包含事件类型、锁名称、发起线程信息以及可选的等待时间。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable event record - 不可变事件记录</li>
 *   <li>Factory methods for common events - 常见事件的工厂方法</li>
 *   <li>Automatic thread and timestamp capture - 自动捕获线程和时间戳</li>
 *   <li>Wait-time tracking for acquisition events - 获取事件的等待时间跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create an acquired event with wait time | 创建带等待时间的获取事件
 * LockEvent acquired = LockEvent.acquired("myLock", Duration.ofMillis(50));
 *
 * // Create a released event | 创建释放事件
 * LockEvent released = LockEvent.released("myLock");
 *
 * // Create a timeout event | 创建超时事件
 * LockEvent timeout = LockEvent.timeout("myLock", Duration.ofSeconds(5));
 *
 * // Create an error event | 创建错误事件
 * LockEvent error = LockEvent.error("myLock");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: type must not be null - 空值安全: type不能为null</li>
 * </ul>
 *
 * @param type       the event type | 事件类型
 * @param lockName   the lock name | 锁名称
 * @param threadName the originating thread name | 发起线程名称
 * @param threadId   the originating thread ID | 发起线程ID
 * @param timestamp  the event timestamp | 事件时间戳
 * @param waitTime   the wait duration (nullable, only for ACQUIRED/TIMEOUT events) |
 *                   等待时长（可为null，仅用于ACQUIRED/TIMEOUT事件）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockListener
 * @see ObservableLock
 * @since JDK 25, opencode-base-lock V1.0.3
 */
public record LockEvent(
        EventType type,
        String lockName,
        String threadName,
        long threadId,
        Instant timestamp,
        Duration waitTime
) {

    /**
     * Compact constructor with validation and default values
     * 紧凑构造器，包含验证和默认值
     *
     * @throws IllegalArgumentException if type is null | 如果type为null则抛出
     */
    public LockEvent {
        if (type == null) {
            throw new IllegalArgumentException("Event type must not be null");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (threadName == null) {
            threadName = Thread.currentThread().getName();
        }
    }

    /**
     * Creates an ACQUIRED event with wait time
     * 创建带等待时间的ACQUIRED事件
     *
     * @param lockName the lock name | 锁名称
     * @param waitTime the time spent waiting to acquire the lock | 获取锁的等待时间
     * @return an ACQUIRED lock event | ACQUIRED锁事件
     */
    public static LockEvent acquired(String lockName, Duration waitTime) {
        return of(EventType.ACQUIRED, lockName, waitTime);
    }

    /**
     * Creates a RELEASED event
     * 创建RELEASED事件
     *
     * @param lockName the lock name | 锁名称
     * @return a RELEASED lock event | RELEASED锁事件
     */
    public static LockEvent released(String lockName) {
        return of(EventType.RELEASED, lockName, null);
    }

    /**
     * Creates a TIMEOUT event with wait time
     * 创建带等待时间的TIMEOUT事件
     *
     * @param lockName the lock name | 锁名称
     * @param waitTime the time spent waiting before timeout | 超时前的等待时间
     * @return a TIMEOUT lock event | TIMEOUT锁事件
     */
    public static LockEvent timeout(String lockName, Duration waitTime) {
        return of(EventType.TIMEOUT, lockName, waitTime);
    }

    /**
     * Creates an ERROR event
     * 创建ERROR事件
     *
     * @param lockName the lock name | 锁名称
     * @return an ERROR lock event | ERROR锁事件
     */
    public static LockEvent error(String lockName) {
        return of(EventType.ERROR, lockName, null);
    }

    /**
     * Internal factory method to reduce code duplication
     * 内部工厂方法减少代码重复
     */
    private static LockEvent of(EventType type, String lockName, Duration waitTime) {
        Thread current = Thread.currentThread();
        return new LockEvent(type, lockName,
                current.getName(), current.threadId(),
                Instant.now(), waitTime);
    }

    /**
     * Lock Event Type Enumeration
     * 锁事件类型枚举
     *
     * <p>Defines the possible lifecycle events for a lock.</p>
     * <p>定义锁的可能生命周期事件。</p>
     */
    public enum EventType {

        /**
         * Lock successfully acquired | 锁获取成功
         */
        ACQUIRED,

        /**
         * Lock released | 锁释放
         */
        RELEASED,

        /**
         * Lock acquisition timed out | 锁获取超时
         */
        TIMEOUT,

        /**
         * Lock operation error | 锁操作错误
         */
        ERROR
    }
}
