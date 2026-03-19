package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * Time Interval representing a span between two Instants
 * 时间区间，表示两个Instant之间的时间跨度
 *
 * <p>This class represents an interval of time between two Instants (start and end).
 * It is modeled after ThreeTen-Extra's Interval class and provides operations for
 * containment, overlap, intersection, and union.</p>
 * <p>此类表示两个Instant（起始和结束）之间的时间区间。
 * 设计参考ThreeTen-Extra的Interval类，提供包含、重叠、交集和并集操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create from start/end Instants - 从起始/结束Instant创建</li>
 *   <li>Create from start Instant and Duration - 从起始Instant和时长创建</li>
 *   <li>Check containment and overlap - 检查包含和重叠</li>
 *   <li>Calculate intersection and union - 计算交集和并集</li>
 *   <li>Convert to LocalDateRange/LocalDateTimeRange - 转换为日期/日期时间范围</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create Interval
 * Instant start = Instant.now();
 * Instant end = start.plus(Duration.ofHours(2));
 * Interval interval = Interval.of(start, end);
 *
 * // Create from duration
 * Interval interval2 = Interval.of(start, Duration.ofMinutes(30));
 *
 * // Check containment
 * boolean contains = interval.contains(start.plusSeconds(60));  // true
 *
 * // Check overlap
 * boolean overlaps = interval.overlaps(interval2);  // true
 *
 * // Get intersection
 * Optional<Interval> intersection = interval.intersection(interval2);
 *
 * // Get duration
 * Duration duration = interval.toDuration();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 *   <li>All operations are O(1) - 所有操作都是O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LocalDateRange
 * @see LocalDateTimeRange
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class Interval implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The start instant (inclusive)
     */
    private final Instant start;

    /**
     * The end instant (exclusive)
     */
    private final Instant end;

    // ==================== Constructors | 构造函数 ====================

    private Interval(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an Interval from start and end Instants
     * 从起始和结束Instant创建Interval
     *
     * @param start the start instant (inclusive) | 起始时刻（包含）
     * @param end   the end instant (exclusive) | 结束时刻（不包含）
     * @return the Interval instance | Interval实例
     * @throws NullPointerException if start or end is null | 如果起始或结束为null则抛出异常
     * @throws OpenDateException    if start is after end | 如果起始在结束之后则抛出异常
     */
    public static Interval of(Instant start, Instant end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (start.isAfter(end)) {
            throw OpenDateException.rangeError("Start instant must not be after end instant: " + start + " > " + end);
        }
        return new Interval(start, end);
    }

    /**
     * Creates an Interval from start Instant and Duration
     * 从起始Instant和时长创建Interval
     *
     * @param start    the start instant (inclusive) | 起始时刻（包含）
     * @param duration the duration | 时长
     * @return the Interval instance | Interval实例
     * @throws NullPointerException if start or duration is null | 如果起始或时长为null则抛出异常
     * @throws OpenDateException    if duration is negative | 如果时长为负则抛出异常
     */
    public static Interval of(Instant start, Duration duration) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        if (duration.isNegative()) {
            throw OpenDateException.rangeError("Duration must not be negative: " + duration);
        }
        return new Interval(start, start.plus(duration));
    }

    /**
     * Creates an Interval from end Instant and Duration
     * 从结束Instant和时长创建Interval
     *
     * @param duration the duration | 时长
     * @param end      the end instant (exclusive) | 结束时刻（不包含）
     * @return the Interval instance | Interval实例
     * @throws NullPointerException if duration or end is null | 如果时长或结束为null则抛出异常
     * @throws OpenDateException    if duration is negative | 如果时长为负则抛出异常
     */
    public static Interval of(Duration duration, Instant end) {
        Objects.requireNonNull(duration, "duration must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (duration.isNegative()) {
            throw OpenDateException.rangeError("Duration must not be negative: " + duration);
        }
        return new Interval(end.minus(duration), end);
    }

    /**
     * Parses an ISO 8601 interval string
     * 解析ISO 8601区间字符串
     *
     * <p>Supported formats:</p>
     * <ul>
     *   <li>start/end: "2024-01-01T00:00:00Z/2024-01-02T00:00:00Z"</li>
     *   <li>start/duration: "2024-01-01T00:00:00Z/PT1H"</li>
     *   <li>duration/end: "PT1H/2024-01-01T00:00:00Z"</li>
     * </ul>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed Interval | 解析后的Interval
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static Interval parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString();

        int slashIndex = str.indexOf('/');
        if (slashIndex < 0) {
            throw OpenDateException.parseError(str, "start/end or start/duration or duration/end");
        }

        String first = str.substring(0, slashIndex);
        String second = str.substring(slashIndex + 1);

        try {
            // Check if first part is a duration
            if (first.startsWith("P") || first.startsWith("-P")) {
                Duration duration = Duration.parse(first);
                Instant end = Instant.parse(second);
                return of(duration, end);
            }

            // Check if second part is a duration
            if (second.startsWith("P") || second.startsWith("-P")) {
                Instant start = Instant.parse(first);
                Duration duration = Duration.parse(second);
                return of(start, duration);
            }

            // Both are instants
            Instant start = Instant.parse(first);
            Instant end = Instant.parse(second);
            return of(start, end);
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(str, "ISO 8601 interval", e);
        }
    }

    /**
     * Creates an empty Interval at the specified instant
     * 在指定时刻创建空Interval
     *
     * @param instant the instant | 时刻
     * @return an empty Interval | 空Interval
     */
    public static Interval empty(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return new Interval(instant, instant);
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the start instant (inclusive)
     * 获取起始时刻（包含）
     *
     * @return the start instant | 起始时刻
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Gets the end instant (exclusive)
     * 获取结束时刻（不包含）
     *
     * @return the end instant | 结束时刻
     */
    public Instant getEnd() {
        return end;
    }

    /**
     * Gets the duration of this interval
     * 获取此区间的时长
     *
     * @return the duration | 时长
     */
    public Duration toDuration() {
        return Duration.between(start, end);
    }

    /**
     * Checks if this interval is empty (start equals end)
     * 检查此区间是否为空（起始等于结束）
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return start.equals(end);
    }

    // ==================== Containment Methods | 包含方法 ====================

    /**
     * Checks if this interval contains the specified instant
     * 检查此区间是否包含指定时刻
     *
     * @param instant the instant to check | 要检查的时刻
     * @return true if contained | 如果包含返回true
     */
    public boolean contains(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return !instant.isBefore(start) && instant.isBefore(end);
    }

    /**
     * Checks if this interval encloses (fully contains) the specified interval
     * 检查此区间是否包围（完全包含）指定区间
     *
     * @param other the other interval | 另一个区间
     * @return true if enclosed | 如果包围返回true
     */
    public boolean encloses(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        return !start.isAfter(other.start) && !end.isBefore(other.end);
    }

    // ==================== Overlap Methods | 重叠方法 ====================

    /**
     * Checks if this interval overlaps with the specified interval
     * 检查此区间是否与指定区间重叠
     *
     * @param other the other interval | 另一个区间
     * @return true if overlapping | 如果重叠返回true
     */
    public boolean overlaps(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    /**
     * Checks if this interval abuts (is adjacent to) the specified interval
     * 检查此区间是否与指定区间相邻
     *
     * @param other the other interval | 另一个区间
     * @return true if abutting | 如果相邻返回true
     */
    public boolean abuts(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        return end.equals(other.start) || start.equals(other.end);
    }

    /**
     * Checks if this interval is before the specified interval
     * 检查此区间是否在指定区间之前
     *
     * @param other the other interval | 另一个区间
     * @return true if before | 如果在之前返回true
     */
    public boolean isBefore(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        return !end.isAfter(other.start) && !equals(other);
    }

    /**
     * Checks if this interval is before the specified instant
     * 检查此区间是否在指定时刻之前
     *
     * @param instant the instant | 时刻
     * @return true if before | 如果在之前返回true
     */
    public boolean isBefore(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return !end.isAfter(instant);
    }

    /**
     * Checks if this interval is after the specified interval
     * 检查此区间是否在指定区间之后
     *
     * @param other the other interval | 另一个区间
     * @return true if after | 如果在之后返回true
     */
    public boolean isAfter(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        return !start.isBefore(other.end) && !equals(other);
    }

    /**
     * Checks if this interval is after the specified instant
     * 检查此区间是否在指定时刻之后
     *
     * @param instant the instant | 时刻
     * @return true if after | 如果在之后返回true
     */
    public boolean isAfter(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return start.isAfter(instant);
    }

    // ==================== Set Operations | 集合运算 ====================

    /**
     * Gets the intersection of this interval with the specified interval
     * 获取此区间与指定区间的交集
     *
     * @param other the other interval | 另一个区间
     * @return the intersection, or empty if no overlap | 交集，如果无重叠则为空
     */
    public Optional<Interval> intersection(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!overlaps(other)) {
            return Optional.empty();
        }
        Instant maxStart = start.isAfter(other.start) ? start : other.start;
        Instant minEnd = end.isBefore(other.end) ? end : other.end;
        return Optional.of(new Interval(maxStart, minEnd));
    }

    /**
     * Gets the union of this interval with the specified interval
     * 获取此区间与指定区间的并集
     *
     * <p>The intervals must overlap or abut.</p>
     * <p>区间必须重叠或相邻。</p>
     *
     * @param other the other interval | 另一个区间
     * @return the union | 并集
     * @throws OpenDateException if the intervals do not overlap or abut | 如果区间不重叠且不相邻则抛出异常
     */
    public Interval union(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!overlaps(other) && !abuts(other)) {
            throw OpenDateException.rangeError("Intervals must overlap or abut to form union");
        }
        Instant minStart = start.isBefore(other.start) ? start : other.start;
        Instant maxEnd = end.isAfter(other.end) ? end : other.end;
        return new Interval(minStart, maxEnd);
    }

    /**
     * Gets the gap between this interval and the specified interval
     * 获取此区间与指定区间之间的间隙
     *
     * @param other the other interval | 另一个区间
     * @return the gap, or empty if overlapping or abutting | 间隙，如果重叠或相邻则为空
     */
    public Optional<Interval> gap(Interval other) {
        Objects.requireNonNull(other, "other must not be null");
        if (overlaps(other) || abuts(other)) {
            return Optional.empty();
        }
        if (isBefore(other)) {
            return Optional.of(new Interval(end, other.start));
        } else {
            return Optional.of(new Interval(other.end, start));
        }
    }

    // ==================== Modification Methods | 修改方法 ====================

    /**
     * Creates a new interval with a different start
     * 创建具有不同起始的新区间
     *
     * @param start the new start | 新的起始
     * @return a new Interval | 新的Interval
     * @throws OpenDateException if the new start is after the end | 如果新起始在结束之后则抛出异常
     */
    public Interval withStart(Instant start) {
        return of(start, end);
    }

    /**
     * Creates a new interval with a different end
     * 创建具有不同结束的新区间
     *
     * @param end the new end | 新的结束
     * @return a new Interval | 新的Interval
     * @throws OpenDateException if the start is after the new end | 如果起始在新结束之后则抛出异常
     */
    public Interval withEnd(Instant end) {
        return of(start, end);
    }

    /**
     * Expands this interval by the specified duration on both ends
     * 在两端扩展此区间指定的时长
     *
     * @param duration the duration to expand | 要扩展的时长
     * @return a new Interval | 新的Interval
     */
    public Interval expand(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return new Interval(start.minus(duration), end.plus(duration));
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts this interval to a LocalDateRange using the specified zone
     * 使用指定时区将此区间转换为LocalDateRange
     *
     * @param zone the zone | 时区
     * @return the LocalDateRange | LocalDateRange
     */
    public LocalDateRange toLocalDateRange(ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        LocalDate startDate = start.atZone(zone).toLocalDate();
        LocalDate endDate = end.atZone(zone).toLocalDate();
        return LocalDateRange.of(startDate, endDate);
    }

    /**
     * Converts this interval to a LocalDateTimeRange using the specified zone
     * 使用指定时区将此区间转换为LocalDateTimeRange
     *
     * @param zone the zone | 时区
     * @return the LocalDateTimeRange | LocalDateTimeRange
     */
    public LocalDateTimeRange toLocalDateTimeRange(ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        LocalDateTime startDateTime = start.atZone(zone).toLocalDateTime();
        LocalDateTime endDateTime = end.atZone(zone).toLocalDateTime();
        return LocalDateTimeRange.of(startDateTime, endDateTime);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Interval other) {
            return start.equals(other.start) && end.equals(other.end);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return start.toString() + "/" + end.toString();
    }
}
