package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Date Range Strategy
 * 日期范围策略
 *
 * <p>Strategy that enables feature within a specific time window.</p>
 * <p>在特定时间窗口内启用功能的策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-based activation - 基于时间的激活</li>
 *   <li>Scheduled features - 定时功能</li>
 *   <li>Limited-time promotions - 限时促销</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Spring sale (March 2024)
 * Feature feature = Feature.builder("spring-sale")
 *     .strategy(new DateRangeStrategy(
 *         Instant.parse("2024-03-01T00:00:00Z"),
 *         Instant.parse("2024-03-31T23:59:59Z")
 *     ))
 *     .build();
 *
 * // Using LocalDateTime
 * Feature feature = Feature.builder("new-year-promo")
 *     .strategy(DateRangeStrategy.of(
 *         LocalDateTime.of(2024, 1, 1, 0, 0),
 *         LocalDateTime.of(2024, 1, 7, 23, 59)
 *     ))
 *     .build();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public class DateRangeStrategy implements EnableStrategy {

    private final Instant startTime;
    private final Instant endTime;

    /**
     * Create date range strategy
     * 创建日期范围策略
     *
     * @param startTime the start time (inclusive) | 开始时间（包含）
     * @param endTime   the end time (inclusive) | 结束时间（包含）
     */
    public DateRangeStrategy(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Create from LocalDateTime using system timezone
     * 使用系统时区从LocalDateTime创建
     *
     * @param startTime the start time | 开始时间
     * @param endTime   the end time | 结束时间
     * @return new DateRangeStrategy | 新的DateRangeStrategy
     */
    public static DateRangeStrategy of(LocalDateTime startTime, LocalDateTime endTime) {
        return of(startTime, endTime, ZoneId.systemDefault());
    }

    /**
     * Create from LocalDateTime with specific timezone
     * 使用特定时区从LocalDateTime创建
     *
     * @param startTime the start time | 开始时间
     * @param endTime   the end time | 结束时间
     * @param zoneId    the timezone | 时区
     * @return new DateRangeStrategy | 新的DateRangeStrategy
     */
    public static DateRangeStrategy of(LocalDateTime startTime, LocalDateTime endTime, ZoneId zoneId) {
        return new DateRangeStrategy(
            startTime.atZone(zoneId).toInstant(),
            endTime.atZone(zoneId).toInstant()
        );
    }

    /**
     * Create strategy that starts now and ends at specified time
     * 创建从现在开始到指定时间结束的策略
     *
     * @param endTime the end time | 结束时间
     * @return new DateRangeStrategy | 新的DateRangeStrategy
     */
    public static DateRangeStrategy until(Instant endTime) {
        return new DateRangeStrategy(Instant.now(), endTime);
    }

    /**
     * Create strategy that starts at specified time and never ends
     * 创建从指定时间开始永不结束的策略
     *
     * @param startTime the start time | 开始时间
     * @return new DateRangeStrategy | 新的DateRangeStrategy
     */
    public static DateRangeStrategy from(Instant startTime) {
        return new DateRangeStrategy(startTime, Instant.MAX);
    }

    /**
     * Check if current time is within the date range
     * 检查当前时间是否在日期范围内
     *
     * @param feature the feature | 功能
     * @param context the context | 上下文
     * @return true if within range | 如果在范围内返回true
     */
    @Override
    public boolean isEnabled(Feature feature, FeatureContext context) {
        Instant now = Instant.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * Get start time
     * 获取开始时间
     *
     * @return start time | 开始时间
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Get end time
     * 获取结束时间
     *
     * @return end time | 结束时间
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Check if the time window has started
     * 检查时间窗口是否已开始
     *
     * @return true if started | 如果已开始返回true
     */
    public boolean hasStarted() {
        return !Instant.now().isBefore(startTime);
    }

    /**
     * Check if the time window has ended
     * 检查时间窗口是否已结束
     *
     * @return true if ended | 如果已结束返回true
     */
    public boolean hasEnded() {
        return Instant.now().isAfter(endTime);
    }

    @Override
    public String toString() {
        return "DateRangeStrategy{startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}
