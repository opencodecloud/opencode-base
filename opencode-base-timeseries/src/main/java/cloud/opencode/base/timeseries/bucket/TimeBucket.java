package cloud.opencode.base.timeseries.bucket;

/**
 * Time Bucket
 * 时间分桶
 *
 * <p>Enumeration of calendar-aware time bucket granularities for aggregating time series data.</p>
 * <p>用于聚合时间序列数据的日历感知时间桶粒度枚举。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Calendar-aware bucketing from second to year granularity - 从秒到年粒度的日历感知分桶</li>
 *   <li>Supports standard time units: SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR - 支持标准时间单位</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSeries daily = TimeBucketUtil.bucket(series, TimeBucket.DAY, ZoneId.of("UTC"), AggregationType.AVG);
 * TimeSeries monthly = TimeBucketUtil.bucket(series, TimeBucket.MONTH, ZoneId.systemDefault(), AggregationType.SUM);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum constants are immutable) - 线程安全: 是（枚举常量不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
public enum TimeBucket {

    /**
     * Second granularity | 秒粒度
     */
    SECOND,

    /**
     * Minute granularity | 分钟粒度
     */
    MINUTE,

    /**
     * Hour granularity | 小时粒度
     */
    HOUR,

    /**
     * Day granularity | 天粒度
     */
    DAY,

    /**
     * Week granularity (starts on Monday) | 周粒度（从周一开始）
     */
    WEEK,

    /**
     * Month granularity | 月粒度
     */
    MONTH,

    /**
     * Quarter granularity (Q1=Jan, Q2=Apr, Q3=Jul, Q4=Oct) | 季度粒度（Q1=1月, Q2=4月, Q3=7月, Q4=10月）
     */
    QUARTER,

    /**
     * Year granularity | 年粒度
     */
    YEAR
}
