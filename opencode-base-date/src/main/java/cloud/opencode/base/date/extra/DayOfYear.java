package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.Temporal;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * DayOfYear class representing a day within a year (1-366)
 * 年份中的天，表示年份中的一天（1-366）
 *
 * <p>This class represents a day-of-year value, independent of any particular year.
 * When combined with a specific year, the validity of the day-of-year is checked.</p>
 * <p>此类表示年份中的天值，独立于任何特定的年份。
 * 当与特定年份组合时，会检查日期的有效性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Day-of-year representation (1-366) - 年中天数表示（1-366）</li>
 *   <li>Validation for specific years - 特定年份验证</li>
 *   <li>Combine with year to create LocalDate - 与年份组合创建LocalDate</li>
 *   <li>TemporalAccessor and TemporalAdjuster implementations - 实现TemporalAccessor和TemporalAdjuster</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DayOfYear day = DayOfYear.of(100);
 * DayOfYear first = DayOfYear.first();
 * boolean valid = day.isValidFor(Year.of(2024));  // true
 * LocalDate date = day.atYear(2024);  // 2024-04-09
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DayOfYear implements TemporalAccessor, TemporalAdjuster,
        Comparable<DayOfYear>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int day;

    private DayOfYear(int day) {
        this.day = day;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a DayOfYear from a value (1-366)
     * 从值（1-366）创建DayOfYear
     *
     * @param dayOfYear the day of year (1-366) | 年份中的天（1-366）
     * @return the DayOfYear | DayOfYear实例
     * @throws OpenDateException if the value is out of range | 如果值超出范围则抛出异常
     */
    public static DayOfYear of(int dayOfYear) {
        if (dayOfYear < 1 || dayOfYear > 366) {
            throw OpenDateException.invalidValue("dayOfYear", dayOfYear, "1-366");
        }
        return new DayOfYear(dayOfYear);
    }

    /**
     * Gets the first day of year (1)
     * 获取年份的第一天（1）
     *
     * @return day 1 | 第1天
     */
    public static DayOfYear first() {
        return of(1);
    }

    /**
     * Gets the last day of year for the given year
     * 获取给定年份的最后一天
     *
     * @param year the year | 年份
     * @return day 365 or 366 | 第365或366天
     */
    public static DayOfYear lastOf(int year) {
        return of(Year.isLeap(year) ? 366 : 365);
    }

    /**
     * Gets a DayOfYear from a TemporalAccessor
     * 从TemporalAccessor获取DayOfYear
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the DayOfYear | DayOfYear实例
     */
    public static DayOfYear from(TemporalAccessor temporal) {
        if (temporal instanceof DayOfYear doy) {
            return doy;
        }
        return of(temporal.get(ChronoField.DAY_OF_YEAR));
    }

    /**
     * Gets the current day of year
     * 获取当前年份中的天
     *
     * @return the current DayOfYear | 当前的DayOfYear
     */
    public static DayOfYear now() {
        return from(LocalDate.now());
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the day-of-year value (1-366)
     * 获取年份中的天值（1-366）
     *
     * @return the day value | 天值
     */
    public int getValue() {
        return day;
    }

    // ==================== Validation Methods ====================

    /**
     * Checks if this day is valid for the given year
     * 检查此天在给定年份中是否有效
     *
     * @param year the year | 年份
     * @return true if valid | 如果有效返回true
     */
    public boolean isValidFor(Year year) {
        Objects.requireNonNull(year, "year must not be null");
        return day <= year.length();
    }

    /**
     * Checks if this day is valid for the given year
     * 检查此天在给定年份中是否有效
     *
     * @param year the year | 年份
     * @return true if valid | 如果有效返回true
     */
    public boolean isValidFor(int year) {
        return isValidFor(Year.of(year));
    }

    /**
     * Checks if this day is valid for both leap and non-leap years
     * 检查此天在闰年和非闰年中是否都有效
     *
     * @return true if day is 1-365 | 如果天数是1-365返回true
     */
    public boolean isValidForAllYears() {
        return day <= 365;
    }

    /**
     * Checks if this is the first day of year
     * 检查是否为年份的第一天
     *
     * @return true if day is 1 | 如果是第1天返回true
     */
    public boolean isFirst() {
        return day == 1;
    }

    /**
     * Checks if this is day 366 (leap year only)
     * 检查是否为第366天（仅闰年）
     *
     * @return true if day is 366 | 如果是第366天返回true
     */
    public boolean isLeapDay() {
        return day == 366;
    }

    // ==================== Conversion Methods ====================

    /**
     * Combines this day with a Year to create a LocalDate
     * 将此天与Year组合创建LocalDate
     *
     * @param year the year | 年份
     * @return the resulting date | 结果日期
     * @throws OpenDateException if this day is not valid for the given year | 如果此天在给定年份无效则抛出异常
     */
    public LocalDate atYear(Year year) {
        Objects.requireNonNull(year, "year must not be null");
        if (!isValidFor(year)) {
            throw new OpenDateException("Day " + day + " is not valid for year " + year);
        }
        return year.atDay(day);
    }

    /**
     * Combines this day with a year value to create a LocalDate
     * 将此天与年份值组合创建LocalDate
     *
     * @param year the year | 年份
     * @return the resulting date | 结果日期
     */
    public LocalDate atYear(int year) {
        return atYear(Year.of(year));
    }

    // ==================== TemporalAccessor Implementation ====================

    @Override
    public boolean isSupported(TemporalField field) {
        return field == ChronoField.DAY_OF_YEAR;
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.DAY_OF_YEAR) {
            return day;
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.DAY_OF_YEAR) {
            return ValueRange.of(1, 366);
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    // ==================== TemporalAdjuster Implementation ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_YEAR, day);
    }

    // ==================== Query ====================

    /**
     * Gets a query for extracting DayOfYear from a temporal
     * 获取从时间对象提取DayOfYear的查询
     *
     * @return the query | 查询
     */
    public static TemporalQuery<DayOfYear> query() {
        return DayOfYear::from;
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(DayOfYear other) {
        return Integer.compare(day, other.day);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DayOfYear other) {
            return day == other.day;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(day);
    }

    @Override
    public String toString() {
        return "DayOfYear(" + day + ")";
    }
}
