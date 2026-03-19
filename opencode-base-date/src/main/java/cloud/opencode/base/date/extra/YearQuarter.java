package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
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
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * Year-Quarter combination representing a specific quarter in a specific year
 * 年-季度组合，表示特定年份的特定季度
 *
 * <p>This class represents a year and quarter combination, such as "2024-Q1" for the
 * first quarter of 2024. It is modeled after ThreeTen-Extra's YearQuarter class.</p>
 * <p>此类表示年份和季度的组合，例如"2024-Q1"表示2024年第一季度。
 * 设计参考ThreeTen-Extra的YearQuarter类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create from year and quarter - 从年份和季度创建</li>
 *   <li>Parse from string format - 从字符串格式解析</li>
 *   <li>Add/subtract quarters and years - 加减季度和年份</li>
 *   <li>Get start/end date of quarter - 获取季度的开始/结束日期</li>
 *   <li>Comparison and equality - 比较和相等性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create YearQuarter
 * YearQuarter yq = YearQuarter.of(2024, 1);           // 2024-Q1
 * YearQuarter yq2 = YearQuarter.of(2024, Quarter.Q2); // 2024-Q2
 * YearQuarter now = YearQuarter.now();                // Current quarter
 *
 * // Parse from string
 * YearQuarter parsed = YearQuarter.parse("2024-Q1");
 *
 * // Calculate
 * YearQuarter next = yq.plusQuarters(1);  // 2024-Q2
 * YearQuarter prev = yq.minusYears(1);    // 2023-Q1
 *
 * // Get dates
 * LocalDate start = yq.atStartOfQuarter(); // 2024-01-01
 * LocalDate end = yq.atEndOfQuarter();     // 2024-03-31
 *
 * // Compare
 * boolean before = yq.isBefore(yq2);  // true
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
 * @see Quarter
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class YearQuarter implements Temporal, TemporalAdjuster, Comparable<YearQuarter>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The year
     */
    private final int year;

    /**
     * The quarter
     */
    private final Quarter quarter;

    /**
     * Default formatter for parsing and formatting (2024-Q1)
     */
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral("-Q")
            .appendValue(IsoFields.QUARTER_OF_YEAR, 1)
            .toFormatter();

    // ==================== Constructors | 构造函数 ====================

    private YearQuarter(int year, Quarter quarter) {
        this.year = year;
        this.quarter = quarter;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a YearQuarter from year and quarter value
     * 从年份和季度值创建YearQuarter
     *
     * @param year    the year | 年份
     * @param quarter the quarter value from 1 to 4 | 季度值，范围1到4
     * @return the YearQuarter instance | YearQuarter实例
     * @throws OpenDateException if the quarter is invalid | 如果季度无效则抛出异常
     */
    public static YearQuarter of(int year, int quarter) {
        return new YearQuarter(year, Quarter.of(quarter));
    }

    /**
     * Creates a YearQuarter from year and Quarter enum
     * 从年份和Quarter枚举创建YearQuarter
     *
     * @param year    the year | 年份
     * @param quarter the Quarter enum | Quarter枚举
     * @return the YearQuarter instance | YearQuarter实例
     * @throws NullPointerException if quarter is null | 如果季度为null则抛出异常
     */
    public static YearQuarter of(int year, Quarter quarter) {
        Objects.requireNonNull(quarter, "quarter must not be null");
        return new YearQuarter(year, quarter);
    }

    /**
     * Creates a YearQuarter from a TemporalAccessor
     * 从TemporalAccessor创建YearQuarter
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the YearQuarter instance | YearQuarter实例
     * @throws OpenDateException if unable to convert | 如果无法转换则抛出异常
     */
    public static YearQuarter from(TemporalAccessor temporal) {
        if (temporal instanceof YearQuarter yq) {
            return yq;
        }
        try {
            int year = temporal.get(ChronoField.YEAR);
            int quarterValue = temporal.get(IsoFields.QUARTER_OF_YEAR);
            return of(year, quarterValue);
        } catch (Exception e) {
            throw new OpenDateException("Unable to obtain YearQuarter from temporal: " + temporal, e);
        }
    }

    /**
     * Gets the current YearQuarter using system default zone
     * 使用系统默认时区获取当前YearQuarter
     *
     * @return the current YearQuarter | 当前YearQuarter
     */
    public static YearQuarter now() {
        return now(ZoneId.systemDefault());
    }

    /**
     * Gets the current YearQuarter for the specified zone
     * 获取指定时区的当前YearQuarter
     *
     * @param zone the zone ID | 时区ID
     * @return the current YearQuarter | 当前YearQuarter
     */
    public static YearQuarter now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    /**
     * Gets the current YearQuarter using the specified clock
     * 使用指定时钟获取当前YearQuarter
     *
     * @param clock the clock to use | 要使用的时钟
     * @return the current YearQuarter | 当前YearQuarter
     */
    public static YearQuarter now(Clock clock) {
        return from(LocalDate.now(clock));
    }

    /**
     * Parses a string to YearQuarter
     * 解析字符串为YearQuarter
     *
     * <p>Supported formats: "2024-Q1", "2024Q1"</p>
     * <p>支持的格式："2024-Q1"、"2024Q1"</p>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed YearQuarter | 解析后的YearQuarter
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static YearQuarter parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString().trim().toUpperCase();

        try {
            // Try format: 2024-Q1
            if (str.contains("-Q")) {
                return FORMATTER.parse(str, YearQuarter::from);
            }
            // Try format: 2024Q1
            if (str.contains("Q")) {
                int qIndex = str.indexOf('Q');
                int year = Integer.parseInt(str.substring(0, qIndex));
                int quarter = Integer.parseInt(str.substring(qIndex + 1));
                return of(year, quarter);
            }
            throw OpenDateException.parseError(text.toString(), "yyyy-Qn or yyyyQn");
        } catch (DateTimeParseException | NumberFormatException e) {
            throw OpenDateException.parseError(text.toString(), "yyyy-Qn", e);
        }
    }

    /**
     * Parses a string to YearQuarter using the specified formatter
     * 使用指定格式化器解析字符串为YearQuarter
     *
     * @param text      the text to parse | 要解析的文本
     * @param formatter the formatter to use | 要使用的格式化器
     * @return the parsed YearQuarter | 解析后的YearQuarter
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static YearQuarter parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(formatter, "formatter must not be null");
        try {
            return formatter.parse(text, YearQuarter::from);
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(text.toString(), formatter.toString(), e);
        }
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the year
     * 获取年份
     *
     * @return the year | 年份
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the quarter value (1-4)
     * 获取季度值(1-4)
     *
     * @return the quarter value | 季度值
     */
    public int getQuarterValue() {
        return quarter.getValue();
    }

    /**
     * Gets the Quarter enum
     * 获取Quarter枚举
     *
     * @return the Quarter | Quarter枚举
     */
    public Quarter getQuarter() {
        return quarter;
    }

    /**
     * Checks if this year is a leap year
     * 检查此年份是否为闰年
     *
     * @return true if leap year | 如果是闰年返回true
     */
    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear(year);
    }

    /**
     * Gets the length of this quarter in days
     * 获取此季度的天数
     *
     * @return the number of days | 天数
     */
    public int lengthOfQuarter() {
        return quarter.length(isLeapYear());
    }

    /**
     * Checks if this is the first quarter of the year
     * 检查是否为年度第一季度
     *
     * @return true if Q1 | 如果是Q1返回true
     */
    public boolean isFirstQuarter() {
        return quarter == Quarter.Q1;
    }

    /**
     * Checks if this is the last quarter of the year
     * 检查是否为年度最后一个季度
     *
     * @return true if Q4 | 如果是Q4返回true
     */
    public boolean isLastQuarter() {
        return quarter == Quarter.Q4;
    }

    // ==================== Calculation Methods | 计算方法 ====================

    /**
     * Adds years to this YearQuarter
     * 在此YearQuarter基础上加年数
     *
     * @param years the years to add | 要加的年数
     * @return a new YearQuarter | 新的YearQuarter
     */
    public YearQuarter plusYears(long years) {
        return new YearQuarter(Math.toIntExact(year + years), quarter);
    }

    /**
     * Adds quarters to this YearQuarter
     * 在此YearQuarter基础上加季度数
     *
     * @param quarters the quarters to add | 要加的季度数
     * @return a new YearQuarter | 新的YearQuarter
     */
    public YearQuarter plusQuarters(long quarters) {
        if (quarters == 0) {
            return this;
        }
        long totalQuarters = year * 4L + (quarter.getValue() - 1) + quarters;
        int newYear = Math.toIntExact(Math.floorDiv(totalQuarters, 4));
        int newQuarter = (int) Math.floorMod(totalQuarters, 4) + 1;
        return new YearQuarter(newYear, Quarter.of(newQuarter));
    }

    /**
     * Subtracts years from this YearQuarter
     * 在此YearQuarter基础上减年数
     *
     * @param years the years to subtract | 要减的年数
     * @return a new YearQuarter | 新的YearQuarter
     */
    public YearQuarter minusYears(long years) {
        return plusYears(-years);
    }

    /**
     * Subtracts quarters from this YearQuarter
     * 在此YearQuarter基础上减季度数
     *
     * @param quarters the quarters to subtract | 要减的季度数
     * @return a new YearQuarter | 新的YearQuarter
     */
    public YearQuarter minusQuarters(long quarters) {
        return plusQuarters(-quarters);
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Gets the start date of this quarter
     * 获取此季度的开始日期
     *
     * @return the first day of the quarter | 季度的第一天
     */
    public LocalDate atStartOfQuarter() {
        return LocalDate.of(year, quarter.firstMonth(), 1);
    }

    /**
     * Gets the end date of this quarter
     * 获取此季度的结束日期
     *
     * @return the last day of the quarter | 季度的最后一天
     */
    public LocalDate atEndOfQuarter() {
        Month lastMonth = Month.of(quarter.lastMonth());
        return LocalDate.of(year, lastMonth, lastMonth.length(isLeapYear()));
    }

    /**
     * Gets the date at a specific day within this quarter
     * 获取此季度内特定天的日期
     *
     * @param dayOfQuarter the day within the quarter (1 to length) | 季度内的天数（1到长度）
     * @return the LocalDate | LocalDate
     * @throws OpenDateException if the day is invalid | 如果天数无效则抛出异常
     */
    public LocalDate atDay(int dayOfQuarter) {
        if (dayOfQuarter < 1 || dayOfQuarter > lengthOfQuarter()) {
            throw OpenDateException.invalidValue("dayOfQuarter", dayOfQuarter, "1-" + lengthOfQuarter());
        }
        return atStartOfQuarter().plusDays(dayOfQuarter - 1);
    }

    /**
     * Gets the YearMonth at a specific month within this quarter
     * 获取此季度内特定月份的YearMonth
     *
     * @param monthOfQuarter the month within the quarter (1, 2, or 3) | 季度内的月份（1、2或3）
     * @return the YearMonth | YearMonth
     * @throws OpenDateException if the month is invalid | 如果月份无效则抛出异常
     */
    public YearMonth atMonth(int monthOfQuarter) {
        if (monthOfQuarter < 1 || monthOfQuarter > 3) {
            throw OpenDateException.invalidValue("monthOfQuarter", monthOfQuarter, "1-3");
        }
        return YearMonth.of(year, quarter.firstMonth() + monthOfQuarter - 1);
    }

    // ==================== Comparison Methods | 比较方法 ====================

    /**
     * Checks if this YearQuarter is before the specified one
     * 检查此YearQuarter是否在指定的之前
     *
     * @param other the other YearQuarter | 另一个YearQuarter
     * @return true if before | 如果在之前返回true
     */
    public boolean isBefore(YearQuarter other) {
        return compareTo(other) < 0;
    }

    /**
     * Checks if this YearQuarter is after the specified one
     * 检查此YearQuarter是否在指定的之后
     *
     * @param other the other YearQuarter | 另一个YearQuarter
     * @return true if after | 如果在之后返回true
     */
    public boolean isAfter(YearQuarter other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(YearQuarter other) {
        int yearCompare = Integer.compare(year, other.year);
        if (yearCompare != 0) {
            return yearCompare;
        }
        return Integer.compare(quarter.getValue(), other.quarter.getValue());
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Formats this YearQuarter using the default format (2024-Q1)
     * 使用默认格式(2024-Q1)格式化此YearQuarter
     *
     * @return the formatted string | 格式化的字符串
     */
    public String format() {
        return year + "-Q" + quarter.getValue();
    }

    /**
     * Formats this YearQuarter using the specified formatter
     * 使用指定格式化器格式化此YearQuarter
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
        if (field == ChronoField.YEAR || field == IsoFields.QUARTER_OF_YEAR) {
            return true;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS || unit == IsoFields.QUARTER_YEARS) {
            return true;
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.YEAR) {
            return year;
        }
        if (field == IsoFields.QUARTER_OF_YEAR) {
            return quarter.getValue();
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.YEAR) {
            return ChronoField.YEAR.range();
        }
        if (field == IsoFields.QUARTER_OF_YEAR) {
            return ValueRange.of(1, 4);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public Temporal with(TemporalField field, long newValue) {
        if (field == ChronoField.YEAR) {
            return of((int) newValue, quarter);
        }
        if (field == IsoFields.QUARTER_OF_YEAR) {
            return of(year, (int) newValue);
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    @Override
    public Temporal plus(long amountToAdd, TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return plusYears(amountToAdd);
        }
        if (unit == IsoFields.QUARTER_YEARS) {
            return plusQuarters(amountToAdd);
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        YearQuarter end = from(endExclusive);
        if (unit == ChronoUnit.YEARS) {
            return (end.year * 4L + end.quarter.getValue() - year * 4L - quarter.getValue()) / 4;
        }
        if (unit == IsoFields.QUARTER_YEARS) {
            return end.year * 4L + end.quarter.getValue() - year * 4L - quarter.getValue();
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    // ==================== TemporalAdjuster Implementation | TemporalAdjuster实现 ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal
                .with(ChronoField.YEAR, year)
                .with(IsoFields.QUARTER_OF_YEAR, quarter.getValue());
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof YearQuarter other) {
            return year == other.year && quarter == other.quarter;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return year ^ (quarter.getValue() << 16);
    }

    @Override
    public String toString() {
        return format();
    }
}
