package cloud.opencode.base.timeseries.query;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Time Range
 * 时间范围
 *
 * <p>Represents a time range for queries.</p>
 * <p>表示查询的时间范围。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods: of, last, today, thisHour, ofMillis - 工厂方法</li>
 *   <li>Range operations: contains, overlaps, extend, shift - 范围操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeRange range = TimeRange.last(Duration.ofHours(1));
 * boolean contains = range.contains(Instant.now());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (null from/to throws NullPointerException) - 空值安全: 否</li>
 * </ul>
 *
 * @param from the start time (inclusive) | 开始时间（包含）
 * @param to the end time (inclusive) | 结束时间（包含）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
public record TimeRange(Instant from, Instant to) {

    public TimeRange {
        Objects.requireNonNull(from, "from cannot be null");
        Objects.requireNonNull(to, "to cannot be null");
    }

    /**
     * Create time range from duration ago until now
     * 创建从指定时长前到现在的时间范围
     *
     * @param duration the duration | 时长
     * @return the time range | 时间范围
     */
    public static TimeRange last(Duration duration) {
        Instant now = Instant.now();
        return new TimeRange(now.minus(duration), now);
    }

    /**
     * Create time range for today
     * 创建今天的时间范围
     *
     * @return the time range | 时间范围
     */
    public static TimeRange today() {
        return last(Duration.ofDays(1));
    }

    /**
     * Create time range for this hour
     * 创建当前小时的时间范围
     *
     * @return the time range | 时间范围
     */
    public static TimeRange thisHour() {
        return last(Duration.ofHours(1));
    }

    /**
     * Create time range between two instants
     * 创建两个时间点之间的时间范围
     *
     * @param from the start time | 开始时间
     * @param to the end time | 结束时间
     * @return the time range | 时间范围
     */
    public static TimeRange of(Instant from, Instant to) {
        return new TimeRange(from, to);
    }

    /**
     * Create time range from epoch millis
     * 从纪元毫秒创建时间范围
     *
     * @param fromMillis the start millis | 开始毫秒
     * @param toMillis the end millis | 结束毫秒
     * @return the time range | 时间范围
     */
    public static TimeRange ofMillis(long fromMillis, long toMillis) {
        return new TimeRange(
            Instant.ofEpochMilli(fromMillis),
            Instant.ofEpochMilli(toMillis)
        );
    }

    /**
     * Get duration of range
     * 获取范围的时长
     *
     * @return the duration | 时长
     */
    public Duration duration() {
        return Duration.between(from, to);
    }

    /**
     * Check if range is valid ({@code from <= to}).
     * 检查范围是否有效（{@code from <= to}）。
     *
     * @return true if valid | 如果有效返回true
     */
    public boolean isValid() {
        return !from.isAfter(to);
    }

    /**
     * Check if range is empty (from == to)
     * 检查范围是否为空（from == to）
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return from.equals(to);
    }

    /**
     * Check if timestamp is in range
     * 检查时间戳是否在范围内
     *
     * @param timestamp the timestamp | 时间戳
     * @return true if in range | 如果在范围内返回true
     */
    public boolean contains(Instant timestamp) {
        return !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }

    /**
     * Check if ranges overlap
     * 检查范围是否重叠
     *
     * @param other the other range | 其他范围
     * @return true if overlap | 如果重叠返回true
     */
    public boolean overlaps(TimeRange other) {
        return !this.to.isBefore(other.from) && !other.to.isBefore(this.from);
    }

    /**
     * Extend range by duration
     * 按时长扩展范围
     *
     * @param duration the duration to extend | 扩展的时长
     * @return the extended range | 扩展后的范围
     */
    public TimeRange extend(Duration duration) {
        return new TimeRange(from.minus(duration), to.plus(duration));
    }

    /**
     * Shift range by duration
     * 按时长移动范围
     *
     * @param duration the duration to shift | 移动的时长
     * @return the shifted range | 移动后的范围
     */
    public TimeRange shift(Duration duration) {
        return new TimeRange(from.plus(duration), to.plus(duration));
    }

    @Override
    public String toString() {
        return String.format("TimeRange[%s to %s]", from, to);
    }
}
