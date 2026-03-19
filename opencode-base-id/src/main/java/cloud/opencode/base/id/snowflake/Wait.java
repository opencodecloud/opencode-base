package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

/**
 * Wait Strategy for Clock Backward Handling
 * 时钟回拨等待策略
 *
 * <p>Waits for the clock to catch up when clock backward is detected.
 * If the wait time exceeds the maximum allowed, throws an exception.</p>
 * <p>检测到时钟回拨时等待时钟追上。如果等待时间超过最大允许值，则抛出异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable wait timeout - 可配置等待超时</li>
 *   <li>Graceful handling for small backward - 小幅回拨优雅处理</li>
 *   <li>Fail-fast for large backward - 大幅回拨快速失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wait up to 5 seconds
 * Wait wait = Wait.ofSeconds(5);
 *
 * // Wait up to 100 milliseconds
 * Wait waitMs = Wait.ofMillis(100);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class Wait implements ClockBackwardStrategy {

    private final long maxWaitMillis;

    /**
     * Creates a wait strategy with maximum wait time
     * 使用最大等待时间创建等待策略
     *
     * @param maxWaitMillis the maximum wait time in milliseconds | 最大等待时间（毫秒）
     */
    public Wait(long maxWaitMillis) {
        if (maxWaitMillis <= 0) {
            throw new IllegalArgumentException("Max wait time must be positive");
        }
        this.maxWaitMillis = maxWaitMillis;
    }

    /**
     * Creates a wait strategy with seconds timeout
     * 使用秒数超时创建等待策略
     *
     * @param seconds the timeout in seconds | 超时秒数
     * @return wait strategy | 等待策略
     */
    public static Wait ofSeconds(int seconds) {
        return new Wait(seconds * 1000L);
    }

    /**
     * Creates a wait strategy with milliseconds timeout
     * 使用毫秒数超时创建等待策略
     *
     * @param millis the timeout in milliseconds | 超时毫秒数
     * @return wait strategy | 等待策略
     */
    public static Wait ofMillis(long millis) {
        return new Wait(millis);
    }

    /**
     * Gets the maximum wait time in milliseconds
     * 获取最大等待时间（毫秒）
     *
     * @return maximum wait time | 最大等待时间
     */
    public long maxWaitMillis() {
        return maxWaitMillis;
    }

    @Override
    public long handle(long lastTimestamp, long currentTimestamp) {
        long diff = lastTimestamp - currentTimestamp;
        if (diff > maxWaitMillis) {
            throw OpenIdGenerationException.clockBackward(lastTimestamp, currentTimestamp);
        }
        try {
            Thread.sleep(diff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenIdGenerationException.clockBackward(lastTimestamp, currentTimestamp);
        }
        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("Wait{maxWaitMillis=%d}", maxWaitMillis);
    }
}
