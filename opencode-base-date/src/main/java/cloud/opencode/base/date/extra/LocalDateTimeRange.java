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
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * DateTime Range representing a span of LocalDateTimes
 * 日期时间范围，表示LocalDateTime的跨度
 *
 * <p>This class represents a range of date-times from a start to an end (end exclusive).
 * It provides operations for containment, overlap, intersection, and conversion to other range types.</p>
 * <p>此类表示从起始到结束（结束不包含）的日期时间范围。
 * 提供包含、重叠、交集和转换为其他范围类型的操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create from start/end LocalDateTimes - 从起始/结束LocalDateTime创建</li>
 *   <li>Create from start and Duration - 从起始和时长创建</li>
 *   <li>Containment and overlap checking - 包含和重叠检查</li>
 *   <li>Convert to LocalDateRange or Interval - 转换为日期范围或Interval</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create LocalDateTimeRange
 * LocalDateTimeRange range = LocalDateTimeRange.of(
 *     LocalDateTime.of(2024, 1, 1, 9, 0),
 *     LocalDateTime.of(2024, 1, 1, 17, 0)
 * );
 *
 * // Create from duration
 * LocalDateTimeRange range2 = LocalDateTimeRange.of(
 *     LocalDateTime.now(),
 *     Duration.ofHours(2)
 * );
 *
 * // Check containment
 * boolean contains = range.contains(LocalDateTime.of(2024, 1, 1, 12, 0));
 *
 * // Get duration
 * Duration duration = range.toDuration();
 *
 * // Convert to Interval
 * Interval interval = range.toInterval(ZoneId.systemDefault());
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
 * @see Interval
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class LocalDateTimeRange implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The start datetime (inclusive)
     */
    private final LocalDateTime start;

    /**
     * The end datetime (exclusive)
     */
    private final LocalDateTime end;

    // ==================== Constructors | 构造函数 ====================

    private LocalDateTimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a LocalDateTimeRange from start and end DateTimes
     * 从起始和结束日期时间创建LocalDateTimeRange
     *
     * @param start the start datetime (inclusive) | 起始日期时间（包含）
     * @param end   the end datetime (exclusive) | 结束日期时间（不包含）
     * @return the LocalDateTimeRange instance | LocalDateTimeRange实例
     * @throws NullPointerException if start or end is null | 如果起始或结束为null则抛出异常
     * @throws OpenDateException    if start is after end | 如果起始在结束之后则抛出异常
     */
    public static LocalDateTimeRange of(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (start.isAfter(end)) {
            throw OpenDateException.rangeError("Start datetime must not be after end datetime: " + start + " > " + end);
        }
        return new LocalDateTimeRange(start, end);
    }

    /**
     * Creates a LocalDateTimeRange from start DateTime and Duration
     * 从起始日期时间和时长创建LocalDateTimeRange
     *
     * @param start    the start datetime (inclusive) | 起始日期时间（包含）
     * @param duration the duration | 时长
     * @return the LocalDateTimeRange instance | LocalDateTimeRange实例
     * @throws NullPointerException if start or duration is null | 如果起始或时长为null则抛出异常
     * @throws OpenDateException    if duration is negative | 如果时长为负则抛出异常
     */
    public static LocalDateTimeRange of(LocalDateTime start, Duration duration) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        if (duration.isNegative()) {
            throw OpenDateException.rangeError("Duration must not be negative: " + duration);
        }
        return new LocalDateTimeRange(start, start.plus(duration));
    }

    /**
     * Creates a LocalDateTimeRange for an entire day
     * 创建整天的LocalDateTimeRange
     *
     * @param date the date | 日期
     * @return the LocalDateTimeRange covering the entire day | 覆盖整天的LocalDateTimeRange
     */
    public static LocalDateTimeRange ofDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return new LocalDateTimeRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    /**
     * Creates an empty LocalDateTimeRange at the specified datetime
     * 在指定日期时间创建空的LocalDateTimeRange
     *
     * @param dateTime the datetime | 日期时间
     * @return an empty range | 空范围
     */
    public static LocalDateTimeRange empty(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return new LocalDateTimeRange(dateTime, dateTime);
    }

    /**
     * Parses a string to LocalDateTimeRange
     * 解析字符串为LocalDateTimeRange
     *
     * <p>Supported format: "2024-01-01T09:00:00/2024-01-01T17:00:00"</p>
     * <p>支持的格式："2024-01-01T09:00:00/2024-01-01T17:00:00"</p>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDateTimeRange | 解析后的LocalDateTimeRange
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDateTimeRange parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString();

        int slashIndex = str.indexOf('/');
        if (slashIndex < 0) {
            throw OpenDateException.parseError(str, "start/end");
        }

        try {
            LocalDateTime start = LocalDateTime.parse(str.substring(0, slashIndex));
            LocalDateTime end = LocalDateTime.parse(str.substring(slashIndex + 1));
            return of(start, end);
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(str, "yyyy-MM-ddTHH:mm:ss/yyyy-MM-ddTHH:mm:ss", e);
        }
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the start datetime (inclusive)
     * 获取起始日期时间（包含）
     *
     * @return the start datetime | 起始日期时间
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Gets the end datetime (exclusive)
     * 获取结束日期时间（不包含）
     *
     * @return the end datetime | 结束日期时间
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Gets the duration of this range
     * 获取此范围的时长
     *
     * @return the duration | 时长
     */
    public Duration toDuration() {
        return Duration.between(start, end);
    }

    /**
     * Gets the length in hours
     * 获取小时数
     *
     * @return the number of hours | 小时数
     */
    public long toHours() {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Gets the length in minutes
     * 获取分钟数
     *
     * @return the number of minutes | 分钟数
     */
    public long toMinutes() {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Gets the length in seconds
     * 获取秒数
     *
     * @return the number of seconds | 秒数
     */
    public long toSeconds() {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Checks if this range is empty (start equals end)
     * 检查此范围是否为空（起始等于结束）
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return start.equals(end);
    }

    // ==================== Containment Methods | 包含方法 ====================

    /**
     * Checks if this range contains the specified datetime
     * 检查此范围是否包含指定日期时间
     *
     * @param dateTime the datetime to check | 要检查的日期时间
     * @return true if contained | 如果包含返回true
     */
    public boolean contains(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return !dateTime.isBefore(start) && dateTime.isBefore(end);
    }

    /**
     * Checks if this range encloses (fully contains) the specified range
     * 检查此范围是否包围（完全包含）指定范围
     *
     * @param other the other range | 另一个范围
     * @return true if enclosed | 如果包围返回true
     */
    public boolean encloses(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return !start.isAfter(other.start) && !end.isBefore(other.end);
    }

    // ==================== Overlap Methods | 重叠方法 ====================

    /**
     * Checks if this range overlaps with the specified range
     * 检查此范围是否与指定范围重叠
     *
     * @param other the other range | 另一个范围
     * @return true if overlapping | 如果重叠返回true
     */
    public boolean overlaps(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    /**
     * Checks if this range abuts (is adjacent to) the specified range
     * 检查此范围是否与指定范围相邻
     *
     * @param other the other range | 另一个范围
     * @return true if abutting | 如果相邻返回true
     */
    public boolean abuts(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return end.equals(other.start) || start.equals(other.end);
    }

    /**
     * Checks if this range is before the specified range
     * 检查此范围是否在指定范围之前
     *
     * @param other the other range | 另一个范围
     * @return true if before | 如果在之前返回true
     */
    public boolean isBefore(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return !end.isAfter(other.start) && !equals(other);
    }

    /**
     * Checks if this range is after the specified range
     * 检查此范围是否在指定范围之后
     *
     * @param other the other range | 另一个范围
     * @return true if after | 如果在之后返回true
     */
    public boolean isAfter(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        return !start.isBefore(other.end) && !equals(other);
    }

    // ==================== Set Operations | 集合运算 ====================

    /**
     * Gets the intersection of this range with the specified range
     * 获取此范围与指定范围的交集
     *
     * @param other the other range | 另一个范围
     * @return the intersection, or empty if no overlap | 交集，如果无重叠则为空
     */
    public Optional<LocalDateTimeRange> intersection(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!overlaps(other)) {
            return Optional.empty();
        }
        LocalDateTime maxStart = start.isAfter(other.start) ? start : other.start;
        LocalDateTime minEnd = end.isBefore(other.end) ? end : other.end;
        return Optional.of(new LocalDateTimeRange(maxStart, minEnd));
    }

    /**
     * Gets the union of this range with the specified range
     * 获取此范围与指定范围的并集
     *
     * <p>The ranges must overlap or abut.</p>
     * <p>范围必须重叠或相邻。</p>
     *
     * @param other the other range | 另一个范围
     * @return the union | 并集
     * @throws OpenDateException if the ranges do not overlap or abut | 如果范围不重叠且不相邻则抛出异常
     */
    public LocalDateTimeRange union(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!overlaps(other) && !abuts(other)) {
            throw OpenDateException.rangeError("Ranges must overlap or abut to form union");
        }
        LocalDateTime minStart = start.isBefore(other.start) ? start : other.start;
        LocalDateTime maxEnd = end.isAfter(other.end) ? end : other.end;
        return new LocalDateTimeRange(minStart, maxEnd);
    }

    /**
     * Gets the gap between this range and the specified range
     * 获取此范围与指定范围之间的间隙
     *
     * @param other the other range | 另一个范围
     * @return the gap, or empty if overlapping or abutting | 间隙，如果重叠或相邻则为空
     */
    public Optional<LocalDateTimeRange> gap(LocalDateTimeRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (overlaps(other) || abuts(other)) {
            return Optional.empty();
        }
        if (isBefore(other)) {
            return Optional.of(new LocalDateTimeRange(end, other.start));
        } else {
            return Optional.of(new LocalDateTimeRange(other.end, start));
        }
    }

    // ==================== Modification Methods | 修改方法 ====================

    /**
     * Creates a new range with a different start
     * 创建具有不同起始的新范围
     *
     * @param start the new start | 新的起始
     * @return a new LocalDateTimeRange | 新的LocalDateTimeRange
     * @throws OpenDateException if the new start is after the end | 如果新起始在结束之后则抛出异常
     */
    public LocalDateTimeRange withStart(LocalDateTime start) {
        return of(start, end);
    }

    /**
     * Creates a new range with a different end
     * 创建具有不同结束的新范围
     *
     * @param end the new end | 新的结束
     * @return a new LocalDateTimeRange | 新的LocalDateTimeRange
     * @throws OpenDateException if the start is after the new end | 如果起始在新结束之后则抛出异常
     */
    public LocalDateTimeRange withEnd(LocalDateTime end) {
        return of(start, end);
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts this range to a LocalDateRange
     * 将此范围转换为LocalDateRange
     *
     * @return the LocalDateRange | LocalDateRange
     */
    public LocalDateRange toLocalDateRange() {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        if (startDate.equals(endDate) && !start.equals(end)) {
            // Same day but different times - single day range
            return LocalDateRange.of(startDate, startDate);
        }
        return LocalDateRange.of(startDate, endDate);
    }

    /**
     * Converts this range to an Interval using the specified zone
     * 使用指定时区将此范围转换为Interval
     *
     * @param zone the zone | 时区
     * @return the Interval | Interval
     */
    public Interval toInterval(ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        Instant startInstant = start.atZone(zone).toInstant();
        Instant endInstant = end.atZone(zone).toInstant();
        return Interval.of(startInstant, endInstant);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDateTimeRange other) {
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
