package cloud.opencode.base.id.snowflake;

/**
 * Clock Backward Handling Strategy
 * 时钟回拨处理策略
 *
 * <p>Sealed interface defining strategies to handle clock backward movement.
 * Clock backward occurs when the system clock moves to an earlier time,
 * which can cause duplicate IDs if not handled properly.</p>
 * <p>定义处理时钟回拨策略的密封接口。当系统时钟回退到更早的时间时会发生时钟回拨，
 * 如果不正确处理可能导致ID重复。</p>
 *
 * <p><strong>Available Strategies | 可用策略:</strong></p>
 * <ul>
 *   <li>{@link Wait} - Wait for clock to catch up | 等待时钟追上</li>
 *   <li>{@link ThrowException} - Throw exception immediately | 立即抛出异常</li>
 *   <li>{@link Extend} - Use extension bits | 使用扩展位</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wait strategy with 5 second timeout
 * ClockBackwardStrategy wait = Wait.ofSeconds(5);
 *
 * // Throw exception strategy (singleton)
 * ClockBackwardStrategy throwEx = ThrowException.getInstance();
 *
 * // Extension bits strategy
 * ClockBackwardStrategy extend = new Extend(2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy interface for handling clock backward events - 处理时钟回拨事件的策略接口</li>
 *   <li>Pluggable clock drift resolution - 可插拔的时钟漂移解决方案</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public sealed interface ClockBackwardStrategy
        permits Wait, ThrowException, Extend, ClockBackwardStrategy.Custom {

    /**
     * Handles clock backward movement
     * 处理时钟回拨
     *
     * @param lastTimestamp    the last timestamp used | 上次使用的时间戳
     * @param currentTimestamp the current (backward) timestamp | 当前（回拨的）时间戳
     * @return the timestamp to use | 要使用的时间戳
     */
    long handle(long lastTimestamp, long currentTimestamp);

    /**
     * Custom strategy interface for user-defined implementations
     * 用于用户自定义实现的自定义策略接口
     */
    non-sealed interface Custom extends ClockBackwardStrategy {
    }
}
