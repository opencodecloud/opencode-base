package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

/**
 * Throw Exception Strategy for Clock Backward Handling
 * 时钟回拨抛异常策略
 *
 * <p>Immediately throws an exception when clock backward is detected.
 * This is the strictest strategy, suitable for scenarios that cannot
 * tolerate any clock inconsistency.</p>
 * <p>检测到时钟回拨时立即抛出异常。这是最严格的策略，
 * 适用于不能容忍任何时钟不一致的场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Singleton instance - 单例实例</li>
 *   <li>Fail-fast behavior - 快速失败行为</li>
 *   <li>Zero tolerance for clock backward - 对时钟回拨零容忍</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClockBackwardStrategy strategy = ThrowException.getInstance();
 *
 * SnowflakeGenerator gen = SnowflakeBuilder.create()
 *     .clockBackwardStrategy(ThrowException.getInstance())
 *     .build();
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
public final class ThrowException implements ClockBackwardStrategy {

    private static final ThrowException INSTANCE = new ThrowException();

    private ThrowException() {
    }

    /**
     * Gets the singleton instance
     * 获取单例实例
     *
     * @return singleton instance | 单例实例
     */
    public static ThrowException getInstance() {
        return INSTANCE;
    }

    @Override
    public long handle(long lastTimestamp, long currentTimestamp) {
        throw OpenIdGenerationException.clockBackward(lastTimestamp, currentTimestamp);
    }

    @Override
    public String toString() {
        return "ThrowException{}";
    }
}
