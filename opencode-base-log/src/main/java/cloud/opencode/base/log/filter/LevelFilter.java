package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.LogEvent;
import cloud.opencode.base.log.LogLevel;

import java.util.Objects;

/**
 * Level Filter - Filters Log Events by Threshold Level
 * 级别过滤器 - 按阈值级别过滤日志事件
 *
 * <p>Denies log events with a level below the configured threshold.
 * Events at or above the threshold pass through as NEUTRAL.</p>
 * <p>拒绝级别低于配置阈值的日志事件。
 * 达到或超过阈值的事件以 NEUTRAL 通过。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Threshold-based level filtering - 基于阈值的级别过滤</li>
 *   <li>DENY for below threshold, NEUTRAL for at or above - 低于阈值返回 DENY，达到或超过返回 NEUTRAL</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Only allow WARN and above
 * LevelFilter filter = new LevelFilter(LogLevel.WARN);
 * chain.addFilter(filter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class LevelFilter implements LogFilter {

    private final LogLevel threshold;

    /**
     * Creates a level filter with the specified threshold.
     * 使用指定阈值创建级别过滤器。
     *
     * @param threshold the minimum level to allow | 允许的最低级别
     * @throws NullPointerException if threshold is null | 如果阈值为 null
     */
    public LevelFilter(LogLevel threshold) {
        this.threshold = Objects.requireNonNull(threshold, "threshold must not be null");
    }

    /**
     * Filters the event based on its level.
     * 根据事件级别进行过滤。
     *
     * @param event the log event | 日志事件
     * @return DENY if below threshold, NEUTRAL otherwise | 低于阈值返回 DENY，否则返回 NEUTRAL
     */
    @Override
    public FilterAction filter(LogEvent event) {
        if (event.level().getLevel() < threshold.getLevel()) {
            return FilterAction.DENY;
        }
        return FilterAction.NEUTRAL;
    }
}
