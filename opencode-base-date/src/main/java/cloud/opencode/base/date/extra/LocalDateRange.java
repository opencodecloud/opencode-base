package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Date Range representing a span of LocalDates
 * 日期范围，表示LocalDate的跨度
 *
 * <p>This class represents a range of dates from a start date to an end date (inclusive).
 * It is modeled after ThreeTen-Extra's LocalDateRange and provides iteration, streaming,
 * and set operations.</p>
 * <p>此类表示从起始日期到结束日期（包含）的日期范围。
 * 设计参考ThreeTen-Extra的LocalDateRange，提供迭代、流式处理和集合操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create from start/end dates - 从起始/结束日期创建</li>
 *   <li>Iterate over all dates in range - 遍历范围内的所有日期</li>
 *   <li>Stream-based operations - 基于流的操作</li>
 *   <li>Containment and overlap checking - 包含和重叠检查</li>
 *   <li>Split by week/month - 按周/月分割</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create LocalDateRange
 * LocalDateRange range = LocalDateRange.of(
 *     LocalDate.of(2024, 1, 1),
 *     LocalDate.of(2024, 1, 31)
 * );
 *
 * // Iterate over dates
 * for (LocalDate date : range) {
 *     System.out.println(date);
 * }
 *
 * // Stream operations
 * long weekdays = range.stream()
 *     .filter(d -> d.getDayOfWeek().getValue() < 6)
 *     .count();
 *
 * // Check containment
 * boolean contains = range.contains(LocalDate.of(2024, 1, 15));
 *
 * // Split by week
 * List<LocalDateRange> weeks = range.splitByWeek();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 *   <li>Lazy iteration - 延迟迭代</li>
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
 * @see Interval
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class LocalDateRange implements Iterable<LocalDate>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Empty range constant
     */
    private static final LocalDateRange EMPTY = new LocalDateRange(null, null, true);

    /**
     * The start date (inclusive), null for empty range
     */
    private final LocalDate start;

    /**
     * The end date (inclusive), null for empty range
     */
    private final LocalDate end;

    /**
     * Whether this is an empty range
     */
    private final boolean empty;

    // ==================== Constructors | 构造函数 ====================

    private LocalDateRange(LocalDate start, LocalDate end, boolean empty) {
        this.start = start;
        this.end = end;
        this.empty = empty;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a LocalDateRange from start and end dates (both inclusive)
     * 从起始和结束日期创建LocalDateRange（两端包含）
     *
     * @param start the start date (inclusive) | 起始日期（包含）
     * @param end   the end date (inclusive) | 结束日期（包含）
     * @return the LocalDateRange instance | LocalDateRange实例
     * @throws NullPointerException if start or end is null | 如果起始或结束为null则抛出异常
     * @throws OpenDateException    if start is after end | 如果起始在结束之后则抛出异常
     */
    public static LocalDateRange of(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (start.isAfter(end)) {
            throw OpenDateException.rangeError("Start date must not be after end date: " + start + " > " + end);
        }
        return new LocalDateRange(start, end, false);
    }

    /**
     * Creates a LocalDateRange from start and end dates (end exclusive)
     * 从起始和结束日期创建LocalDateRange（结束不包含）
     *
     * @param start        the start date (inclusive) | 起始日期（包含）
     * @param endExclusive the end date (exclusive) | 结束日期（不包含）
     * @return the LocalDateRange instance | LocalDateRange实例
     * @throws NullPointerException if start or endExclusive is null | 如果起始或结束为null则抛出异常
     * @throws OpenDateException    if start is after or equals end | 如果起始在结束之后或相等则抛出异常
     */
    public static LocalDateRange ofExclusive(LocalDate start, LocalDate endExclusive) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");
        if (start.isAfter(endExclusive) || start.equals(endExclusive)) {
            return EMPTY;
        }
        return new LocalDateRange(start, endExclusive.minusDays(1), false);
    }

    /**
     * Creates an empty LocalDateRange
     * 创建空的LocalDateRange
     *
     * @return an empty range | 空范围
     */
    public static LocalDateRange empty() {
        return EMPTY;
    }

    /**
     * Parses a string to LocalDateRange
     * 解析字符串为LocalDateRange
     *
     * <p>Supported format: "2024-01-01/2024-01-31"</p>
     * <p>支持的格式："2024-01-01/2024-01-31"</p>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed LocalDateRange | 解析后的LocalDateRange
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static LocalDateRange parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString();

        int slashIndex = str.indexOf('/');
        if (slashIndex < 0) {
            throw OpenDateException.parseError(str, "start/end");
        }

        try {
            LocalDate start = LocalDate.parse(str.substring(0, slashIndex));
            LocalDate end = LocalDate.parse(str.substring(slashIndex + 1));
            return of(start, end);
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(str, "yyyy-MM-dd/yyyy-MM-dd", e);
        }
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the start date (inclusive)
     * 获取起始日期（包含）
     *
     * @return the start date, or null if empty | 起始日期，如果为空则返回null
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * Gets the end date (inclusive)
     * 获取结束日期（包含）
     *
     * @return the end date, or null if empty | 结束日期，如果为空则返回null
     */
    public LocalDate getEnd() {
        return end;
    }

    /**
     * Gets the length in days (inclusive count)
     * 获取天数（包含计数）
     *
     * @return the number of days in this range | 此范围内的天数
     */
    public long lengthInDays() {
        if (empty) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    /**
     * Checks if this range is empty
     * 检查此范围是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return empty;
    }

    // ==================== Containment Methods | 包含方法 ====================

    /**
     * Checks if this range contains the specified date
     * 检查此范围是否包含指定日期
     *
     * @param date the date to check | 要检查的日期
     * @return true if contained | 如果包含返回true
     */
    public boolean contains(LocalDate date) {
        if (empty || date == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Checks if this range encloses (fully contains) the specified range
     * 检查此范围是否包围（完全包含）指定范围
     *
     * @param other the other range | 另一个范围
     * @return true if enclosed | 如果包围返回true
     */
    public boolean encloses(LocalDateRange other) {
        if (empty || other == null || other.empty) {
            return false;
        }
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
    public boolean overlaps(LocalDateRange other) {
        if (empty || other == null || other.empty) {
            return false;
        }
        return !start.isAfter(other.end) && !other.start.isAfter(end);
    }

    /**
     * Checks if this range is connected (adjacent or overlapping) with the specified range
     * 检查此范围是否与指定范围相连（相邻或重叠）
     *
     * @param other the other range | 另一个范围
     * @return true if connected | 如果相连返回true
     */
    public boolean isConnected(LocalDateRange other) {
        if (empty || other == null || other.empty) {
            return false;
        }
        return !start.isAfter(other.end.plusDays(1)) && !other.start.isAfter(end.plusDays(1));
    }

    // ==================== Set Operations | 集合运算 ====================

    /**
     * Gets the intersection of this range with the specified range
     * 获取此范围与指定范围的交集
     *
     * @param other the other range | 另一个范围
     * @return the intersection, or empty if no overlap | 交集，如果无重叠则为空
     */
    public Optional<LocalDateRange> intersection(LocalDateRange other) {
        if (!overlaps(other)) {
            return Optional.empty();
        }
        LocalDate maxStart = start.isAfter(other.start) ? start : other.start;
        LocalDate minEnd = end.isBefore(other.end) ? end : other.end;
        return Optional.of(new LocalDateRange(maxStart, minEnd, false));
    }

    /**
     * Gets the span (union) of this range with the specified range
     * 获取此范围与指定范围的跨度（并集）
     *
     * <p>Unlike union, span does not require the ranges to be connected.</p>
     * <p>与并集不同，跨度不要求范围相连。</p>
     *
     * @param other the other range | 另一个范围
     * @return the span | 跨度
     */
    public LocalDateRange span(LocalDateRange other) {
        Objects.requireNonNull(other, "other must not be null");
        if (empty) {
            return other;
        }
        if (other.empty) {
            return this;
        }
        LocalDate minStart = start.isBefore(other.start) ? start : other.start;
        LocalDate maxEnd = end.isAfter(other.end) ? end : other.end;
        return new LocalDateRange(minStart, maxEnd, false);
    }

    // ==================== Iteration Methods | 迭代方法 ====================

    @Override
    public Iterator<LocalDate> iterator() {
        if (empty) {
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public LocalDate next() {
                    throw new NoSuchElementException();
                }
            };
        }
        return new Iterator<>() {
            private LocalDate current = start;

            @Override
            public boolean hasNext() {
                return !current.isAfter(end);
            }

            @Override
            public LocalDate next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                LocalDate result = current;
                current = current.plusDays(1);
                return result;
            }
        };
    }

    /**
     * Returns a stream of all dates in this range
     * 返回此范围内所有日期的流
     *
     * @return the stream of dates | 日期流
     */
    public Stream<LocalDate> stream() {
        if (empty) {
            return Stream.empty();
        }
        return StreamSupport.stream(
                Spliterators.spliterator(iterator(), lengthInDays(), Spliterator.ORDERED | Spliterator.IMMUTABLE),
                false
        );
    }

    /**
     * Returns a stream of dates in this range with the specified step
     * 返回此范围内按指定步长的日期流
     *
     * @param step the step period | 步长周期
     * @return the stream of dates | 日期流
     */
    public Stream<LocalDate> stream(Period step) {
        Objects.requireNonNull(step, "step must not be null");
        if (empty || step.isZero() || step.isNegative()) {
            return Stream.empty();
        }

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new Iterator<>() {
                    private LocalDate current = start;

                    @Override
                    public boolean hasNext() {
                        return !current.isAfter(end);
                    }

                    @Override
                    public LocalDate next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        LocalDate result = current;
                        current = current.plus(step);
                        return result;
                    }
                }, Spliterator.ORDERED | Spliterator.IMMUTABLE),
                false
        );
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts this range to a list of dates
     * 将此范围转换为日期列表
     *
     * @return the list of dates | 日期列表
     */
    public List<LocalDate> toList() {
        if (empty) {
            return List.of();
        }
        List<LocalDate> result = new ArrayList<>((int) lengthInDays());
        for (LocalDate date : this) {
            result.add(date);
        }
        return List.copyOf(result);
    }

    /**
     * Splits this range by week (Monday to Sunday)
     * 按周分割此范围（周一到周日）
     *
     * @return list of weekly ranges | 周范围列表
     */
    public List<LocalDateRange> splitByWeek() {
        if (empty) {
            return List.of();
        }

        List<LocalDateRange> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            // Find the end of the current week (Sunday)
            LocalDate weekEnd = current.plusDays(7 - current.getDayOfWeek().getValue());
            if (weekEnd.isAfter(end)) {
                weekEnd = end;
            }
            result.add(LocalDateRange.of(current, weekEnd));
            current = weekEnd.plusDays(1);
        }

        return List.copyOf(result);
    }

    /**
     * Splits this range by month
     * 按月分割此范围
     *
     * @return list of monthly ranges | 月范围列表
     */
    public List<LocalDateRange> splitByMonth() {
        if (empty) {
            return List.of();
        }

        List<LocalDateRange> result = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            // Find the end of the current month
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(end)) {
                monthEnd = end;
            }
            result.add(LocalDateRange.of(current, monthEnd));
            current = monthEnd.plusDays(1);
        }

        return List.copyOf(result);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDateRange other) {
            if (empty && other.empty) {
                return true;
            }
            if (empty || other.empty) {
                return false;
            }
            return start.equals(other.start) && end.equals(other.end);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (empty) {
            return 0;
        }
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        if (empty) {
            return "LocalDateRange[]";
        }
        return start.toString() + "/" + end.toString();
    }
}
