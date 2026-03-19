package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.util.Objects;

/**
 * Year-Week combination representing a specific ISO week in a specific year
 * 年-周组合，表示特定年份的特定ISO周
 *
 * <p>This class represents an ISO week-based year and week combination, such as "2024-W01"
 * for the first week of 2024. The week numbering follows ISO-8601 standard where week 1
 * is the first week containing at least 4 days of the new year.</p>
 * <p>此类表示基于ISO的年份和周的组合，例如"2024-W01"表示2024年第一周。
 * 周编号遵循ISO-8601标准，第1周是包含新年至少4天的第一周。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create from week-based year and week - 从周基准年和周创建</li>
 *   <li>Parse from string format (2024-W01) - 从字符串格式解析</li>
 *   <li>Add/subtract weeks and years - 加减周和年</li>
 *   <li>Get specific day of week - 获取特定星期几的日期</li>
 *   <li>ISO-8601 compliant week numbering - 符合ISO-8601的周编号</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create YearWeek
 * YearWeek yw = YearWeek.of(2024, 1);     // 2024-W01
 * YearWeek now = YearWeek.now();          // Current week
 *
 * // Parse from string
 * YearWeek parsed = YearWeek.parse("2024-W01");
 *
 * // Calculate
 * YearWeek next = yw.plusWeeks(1);        // 2024-W02
 * YearWeek prev = yw.minusYears(1);       // 2023-W01
 *
 * // Get specific day
 * LocalDate monday = yw.atMonday();       // First Monday of week
 * LocalDate friday = yw.atDay(DayOfWeek.FRIDAY);
 *
 * // Compare
 * boolean before = yw.isBefore(next);     // true
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 *   <li>Cached formatter for parsing - 解析使用缓存的格式化器</li>
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
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class YearWeek implements Temporal, TemporalAdjuster, Comparable<YearWeek>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The week-based year
     */
    private final int year;

    /**
     * The week number (1-52 or 53)
     */
    private final int week;

    /**
     * Default formatter for parsing and formatting (2024-W01)
     */
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral("-W")
            .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
            .toFormatter();

    // ==================== Constructors | 构造函数 ====================

    private YearWeek(int year, int week) {
        this.year = year;
        this.week = week;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a YearWeek from week-based year and week
     * 从周基准年和周创建YearWeek
     *
     * @param weekBasedYear the week-based year | 周基准年
     * @param week          the week number (1-52 or 53) | 周数（1-52或53）
     * @return the YearWeek instance | YearWeek实例
     * @throws OpenDateException if the week is invalid for the year | 如果周数对该年无效则抛出异常
     */
    public static YearWeek of(int weekBasedYear, int week) {
        int maxWeeks = weeksInYear(weekBasedYear);
        if (week < 1 || week > maxWeeks) {
            throw OpenDateException.invalidValue("week", week, "1-" + maxWeeks + " for year " + weekBasedYear);
        }
        return new YearWeek(weekBasedYear, week);
    }

    /**
     * Creates a YearWeek from a TemporalAccessor
     * 从TemporalAccessor创建YearWeek
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the YearWeek instance | YearWeek实例
     * @throws OpenDateException if unable to convert | 如果无法转换则抛出异常
     */
    public static YearWeek from(TemporalAccessor temporal) {
        if (temporal instanceof YearWeek yw) {
            return yw;
        }
        try {
            int year = temporal.get(IsoFields.WEEK_BASED_YEAR);
            int week = temporal.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            return new YearWeek(year, week);
        } catch (Exception e) {
            throw new OpenDateException("Unable to obtain YearWeek from temporal: " + temporal, e);
        }
    }

    /**
     * Gets the current YearWeek using system default zone
     * 使用系统默认时区获取当前YearWeek
     *
     * @return the current YearWeek | 当前YearWeek
     */
    public static YearWeek now() {
        return now(ZoneId.systemDefault());
    }

    /**
     * Gets the current YearWeek for the specified zone
     * 获取指定时区的当前YearWeek
     *
     * @param zone the zone ID | 时区ID
     * @return the current YearWeek | 当前YearWeek
     */
    public static YearWeek now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * Gets the current YearWeek using the specified clock
     * 使用指定时钟获取当前YearWeek
     *
     * @param clock the clock to use | 要使用的时钟
     * @return the current YearWeek | 当前YearWeek
     */
    public static YearWeek now(Clock clock) {
        return from(LocalDate.now(clock));
    }

    /**
     * Parses a string to YearWeek
     * 解析字符串为YearWeek
     *
     * <p>Supported formats: "2024-W01", "2024W01"</p>
     * <p>支持的格式："2024-W01"、"2024W01"</p>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed YearWeek | 解析后的YearWeek
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static YearWeek parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString().trim().toUpperCase();

        try {
            // Try format: 2024-W01
            if (str.contains("-W")) {
                return FORMATTER.parse(str, YearWeek::from);
            }
            // Try format: 2024W01
            if (str.contains("W")) {
                int wIndex = str.indexOf('W');
                int year = Integer.parseInt(str.substring(0, wIndex));
                int week = Integer.parseInt(str.substring(wIndex + 1));
                return of(year, week);
            }
            throw OpenDateException.parseError(text.toString(), "yyyy-Www or yyyyWww");
        } catch (DateTimeParseException | NumberFormatException e) {
            throw OpenDateException.parseError(text.toString(), "yyyy-Www", e);
        }
    }

    /**
     * Parses a string to YearWeek using the specified formatter
     * 使用指定格式化器解析字符串为YearWeek
     *
     * @param text      the text to parse | 要解析的文本
     * @param formatter the formatter to use | 要使用的格式化器
     * @return the parsed YearWeek | 解析后的YearWeek
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static YearWeek parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        try {
            return formatter.parse(text, YearWeek::from);
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(text.toString(), formatter.toString(), e);
        }
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the week-based year
     * 获取周基准年
     *
     * @return the week-based year | 周基准年
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the week number (1-52 or 53)
     * 获取周数（1-52或53）
     *
     * @return the week number | 周数
     */
    public int getWeek() {
        return week;
    }

    /**
     * Gets the number of weeks in this week-based year
     * 获取此周基准年的周数
     *
     * @return 52 or 53 | 52或53
     */
    public int lengthOfYear() {
        return weeksInYear(year);
    }

    /**
     * Checks if this is the first week of the year
     * 检查是否为年度第一周
     *
     * @return true if week 1 | 如果是第1周返回true
     */
    public boolean isFirstWeek() {
        return week == 1;
    }

    /**
     * Checks if this is the last week of the year
     * 检查是否为年度最后一周
     *
     * @return true if last week | 如果是最后一周返回true
     */
    public boolean isLastWeek() {
        return week == lengthOfYear();
    }

    // ==================== Calculation Methods | 计算方法 ====================

    /**
     * Adds years to this YearWeek
     * 在此YearWeek基础上加年数
     *
     * @param years the years to add | 要加的年数
     * @return a new YearWeek | 新的YearWeek
     */
    public YearWeek plusYears(long years) {
        int newYear = Math.toIntExact(year + years);
        int maxWeeks = weeksInYear(newYear);
        int newWeek = Math.min(week, maxWeeks);
        return new YearWeek(newYear, newWeek);
    }

    /**
     * Adds weeks to this YearWeek
     * 在此YearWeek基础上加周数
     *
     * @param weeks the weeks to add | 要加的周数
     * @return a new YearWeek | 新的YearWeek
     */
    public YearWeek plusWeeks(long weeks) {
        if (weeks == 0) {
            return this;
        }
        LocalDate date = atMonday().plusWeeks(weeks);
        return from(date);
    }

    /**
     * Subtracts years from this YearWeek
     * 在此YearWeek基础上减年数
     *
     * @param years the years to subtract | 要减的年数
     * @return a new YearWeek | 新的YearWeek
     */
    public YearWeek minusYears(long years) {
        return plusYears(-years);
    }

    /**
     * Subtracts weeks from this YearWeek
     * 在此YearWeek基础上减周数
     *
     * @param weeks the weeks to subtract | 要减的周数
     * @return a new YearWeek | 新的YearWeek
     */
    public YearWeek minusWeeks(long weeks) {
        return plusWeeks(-weeks);
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Gets the date for a specific day of week within this week
     * 获取此周内特定星期几的日期
     *
     * @param dayOfWeek the day of week | 星期几
     * @return the LocalDate | LocalDate
     * @throws NullPointerException if dayOfWeek is null | 如果dayOfWeek为null则抛出异常
     */
    public LocalDate atDay(DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        return LocalDate.of(year, 1, 4)  // Jan 4 is always in week 1
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(ChronoField.DAY_OF_WEEK, dayOfWeek.getValue());
    }

    /**
     * Gets the Monday of this week
     * 获取此周的周一
     *
     * @return the Monday date | 周一日期
     */
    public LocalDate atMonday() {
        return atDay(DayOfWeek.MONDAY);
    }

    /**
     * Gets the Tuesday of this week
     * 获取此周的周二
     *
     * @return the Tuesday date | 周二日期
     */
    public LocalDate atTuesday() {
        return atDay(DayOfWeek.TUESDAY);
    }

    /**
     * Gets the Wednesday of this week
     * 获取此周的周三
     *
     * @return the Wednesday date | 周三日期
     */
    public LocalDate atWednesday() {
        return atDay(DayOfWeek.WEDNESDAY);
    }

    /**
     * Gets the Thursday of this week
     * 获取此周的周四
     *
     * @return the Thursday date | 周四日期
     */
    public LocalDate atThursday() {
        return atDay(DayOfWeek.THURSDAY);
    }

    /**
     * Gets the Friday of this week
     * 获取此周的周五
     *
     * @return the Friday date | 周五日期
     */
    public LocalDate atFriday() {
        return atDay(DayOfWeek.FRIDAY);
    }

    /**
     * Gets the Saturday of this week
     * 获取此周的周六
     *
     * @return the Saturday date | 周六日期
     */
    public LocalDate atSaturday() {
        return atDay(DayOfWeek.SATURDAY);
    }

    /**
     * Gets the Sunday of this week
     * 获取此周的周日
     *
     * @return the Sunday date | 周日日期
     */
    public LocalDate atSunday() {
        return atDay(DayOfWeek.SUNDAY);
    }

    // ==================== Comparison Methods | 比较方法 ====================

    /**
     * Checks if this YearWeek is before the specified one
     * 检查此YearWeek是否在指定的之前
     *
     * @param other the other YearWeek | 另一个YearWeek
     * @return true if before | 如果在之前返回true
     */
    public boolean isBefore(YearWeek other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this YearWeek is after the specified one
     * 检查此YearWeek是否在指定的之后
     *
     * @param other the other YearWeek | 另一个YearWeek
     * @return true if after | 如果在之后返回true
     */
    public boolean isAfter(YearWeek other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(YearWeek other) {
        int yearCompare = Integer.compare(year, other.year);
        if (yearCompare != 0) {
            return yearCompare;
        }
        return Integer.compare(week, other.week);
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Formats this YearWeek using the default format (2024-W01)
     * 使用默认格式(2024-W01)格式化此YearWeek
     *
     * @return the formatted string | 格式化的字符串
     */
    public String format() {
        return String.format("%d-W%02d", year, week);
    }

    /**
     * Formats this YearWeek using the specified formatter
     * 使用指定格式化器格式化此YearWeek
     *
     * @param formatter the formatter to use | 要使用的格式化器
     * @return the formatted string | 格式化的字符串
     */
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter must not be null");
        return formatter.format(this);
    }

    // ==================== Temporal Implementation | Temporal实现 ====================

    @Override
    public boolean isSupported(TemporalField field) {
        if (field == IsoFields.WEEK_BASED_YEAR || field == IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            return true;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit == ChronoUnit.WEEKS || unit == ChronoUnit.YEARS) {
            return true;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == IsoFields.WEEK_BASED_YEAR) {
            return year;
        }
        if (field == IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            return week;
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == IsoFields.WEEK_BASED_YEAR) {
            return IsoFields.WEEK_BASED_YEAR.range();
        }
        if (field == IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            return ValueRange.of(1, lengthOfYear());
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public Temporal with(TemporalField field, long newValue) {
        if (field == IsoFields.WEEK_BASED_YEAR) {
            return of((int) newValue, week);
        }
        if (field == IsoFields.WEEK_OF_WEEK_BASED_YEAR) {
            return of(year, (int) newValue);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public Temporal plus(long amountToAdd, TemporalUnit unit) {
        if (unit == ChronoUnit.WEEKS) {
            return plusWeeks(amountToAdd);
        }
        if (unit == ChronoUnit.YEARS) {
            return plusYears(amountToAdd);
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        YearWeek end = from(endExclusive);
        if (unit == ChronoUnit.WEEKS) {
            return ChronoUnit.WEEKS.between(atMonday(), end.atMonday());
        }
        if (unit == ChronoUnit.YEARS) {
            return end.year - year;
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    // ==================== TemporalAdjuster Implementation | TemporalAdjuster实现 ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week);
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Gets the number of weeks in the specified week-based year
     * 获取指定周基准年的周数
     *
     * @param weekBasedYear the week-based year | 周基准年
     * @return 52 or 53 | 52或53
     */
    private static int weeksInYear(int weekBasedYear) {
        LocalDate dec28 = LocalDate.of(weekBasedYear, 12, 28);
        return dec28.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof YearWeek other) {
            return year == other.year && week == other.week;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return year ^ (week << 16);
    }

    @Override
    public String toString() {
        return format();
    }
}
