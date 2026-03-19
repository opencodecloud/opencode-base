package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.Temporal;
import java.time.temporal.ValueRange;
import java.util.Objects;

/**
 * DayOfMonth class representing a day within a month (1-31)
 * 月份中的天，表示月份中的一天（1-31）
 *
 * <p>This class represents a day-of-month value, independent of any particular month or year.
 * When combined with a specific month/year, the validity of the day-of-month is checked.</p>
 * <p>此类表示月份中的天值，独立于任何特定的月份或年份。
 * 当与特定月份/年份组合时，会检查日期的有效性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Day-of-month representation (1-31) - 月中天数表示（1-31）</li>
 *   <li>Validation for specific year-months - 特定年月验证</li>
 *   <li>Combine with YearMonth to create LocalDate - 与YearMonth组合创建LocalDate</li>
 *   <li>TemporalAccessor and TemporalAdjuster implementations - 实现TemporalAccessor和TemporalAdjuster</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DayOfMonth day = DayOfMonth.of(15);
 * DayOfMonth first = DayOfMonth.first();
 * boolean valid = day.isValidFor(YearMonth.of(2024, 2));  // true for day 15 in Feb 2024
 * LocalDate date = day.atYearMonth(YearMonth.of(2024, 3));  // 2024-03-15
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable, cached instances) - 线程安全: 是（不可变，缓存实例）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DayOfMonth implements TemporalAccessor, TemporalAdjuster,
        Comparable<DayOfMonth>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DayOfMonth[] CACHE = new DayOfMonth[31];

    static {
        for (int i = 0; i < 31; i++) {
            CACHE[i] = new DayOfMonth(i + 1);
        }
    }

    private final int day;

    private DayOfMonth(int day) {
        this.day = day;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a DayOfMonth from a value (1-31)
     * 从值（1-31）创建DayOfMonth
     *
     * @param dayOfMonth the day of month (1-31) | 月份中的天（1-31）
     * @return the DayOfMonth | DayOfMonth实例
     * @throws OpenDateException if the value is out of range | 如果值超出范围则抛出异常
     */
    public static DayOfMonth of(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw OpenDateException.invalidValue("dayOfMonth", dayOfMonth, "1-31");
        }
        return CACHE[dayOfMonth - 1];
    }

    /**
     * Gets the first day of month (1)
     * 获取月份的第一天（1）
     *
     * @return day 1 | 第1天
     */
    public static DayOfMonth first() {
        return CACHE[0];
    }

    /**
     * Gets a DayOfMonth from a TemporalAccessor
     * 从TemporalAccessor获取DayOfMonth
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the DayOfMonth | DayOfMonth实例
     */
    public static DayOfMonth from(TemporalAccessor temporal) {
        if (temporal instanceof DayOfMonth dom) {
            return dom;
        }
        return of(temporal.get(ChronoField.DAY_OF_MONTH));
    }

    /**
     * Gets the current day of month
     * 获取当前月份中的天
     *
     * @return the current DayOfMonth | 当前的DayOfMonth
     */
    public static DayOfMonth now() {
        return from(LocalDate.now());
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the day-of-month value (1-31)
     * 获取月份中的天值（1-31）
     *
     * @return the day value | 天值
     */
    public int getValue() {
        return day;
    }

    // ==================== Validation Methods ====================

    /**
     * Checks if this day is valid for the given YearMonth
     * 检查此天在给定YearMonth中是否有效
     *
     * @param yearMonth the year-month | 年月
     * @return true if valid | 如果有效返回true
     */
    public boolean isValidFor(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        return day <= yearMonth.lengthOfMonth();
    }

    /**
     * Checks if this day is valid for the given month in a leap year
     * 检查此天在闰年的给定月份中是否有效
     *
     * @param month the month | 月份
     * @param leapYear whether it's a leap year | 是否为闰年
     * @return true if valid | 如果有效返回true
     */
    public boolean isValidFor(Month month, boolean leapYear) {
        Objects.requireNonNull(month, "month must not be null");
        return day <= month.length(leapYear);
    }

    /**
     * Checks if this is the first day of month
     * 检查是否为月份的第一天
     *
     * @return true if day is 1 | 如果是第1天返回true
     */
    public boolean isFirst() {
        return day == 1;
    }

    /**
     * Checks if this could be the last day of any month
     * 检查是否可能是某月的最后一天
     *
     * @return true if day is 28, 29, 30, or 31 | 如果天数是28、29、30或31返回true
     */
    public boolean isPossibleLastDay() {
        return day >= 28;
    }

    // ==================== Conversion Methods ====================

    /**
     * Combines this day with a YearMonth to create a LocalDate
     * 将此天与YearMonth组合创建LocalDate
     *
     * @param yearMonth the year-month | 年月
     * @return the resulting date | 结果日期
     * @throws OpenDateException if this day is not valid for the given month | 如果此天在给定月份无效则抛出异常
     */
    public LocalDate atYearMonth(YearMonth yearMonth) {
        Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        if (!isValidFor(yearMonth)) {
            throw OpenDateException.invalidValue("dayOfMonth", day,
                "1-" + yearMonth.lengthOfMonth() + " for " + yearMonth);
        }
        return yearMonth.atDay(day);
    }

    /**
     * Combines this day with year and month to create a LocalDate
     * 将此天与年和月组合创建LocalDate
     *
     * @param year the year | 年
     * @param month the month | 月
     * @return the resulting date | 结果日期
     */
    public LocalDate atYearMonth(int year, int month) {
        return atYearMonth(YearMonth.of(year, month));
    }

    /**
     * Combines this day with year and Month to create a LocalDate
     * 将此天与年和Month组合创建LocalDate
     *
     * @param year the year | 年
     * @param month the Month | 月份
     * @return the resulting date | 结果日期
     */
    public LocalDate atYearMonth(int year, Month month) {
        return atYearMonth(YearMonth.of(year, month));
    }

    // ==================== TemporalAccessor Implementation ====================

    @Override
    public boolean isSupported(TemporalField field) {
        return field == ChronoField.DAY_OF_MONTH;
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.DAY_OF_MONTH) {
            return day;
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.DAY_OF_MONTH) {
            return ValueRange.of(1, 31);
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    // ==================== TemporalAdjuster Implementation ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.DAY_OF_MONTH, day);
    }

    // ==================== Query ====================

    /**
     * Gets a query for extracting DayOfMonth from a temporal
     * 获取从时间对象提取DayOfMonth的查询
     *
     * @return the query | 查询
     */
    public static TemporalQuery<DayOfMonth> query() {
        return DayOfMonth::from;
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(DayOfMonth other) {
        return Integer.compare(day, other.day);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DayOfMonth other) {
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
        return "DayOfMonth(" + day + ")";
    }
}
