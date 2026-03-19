package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;

/**
 * Quarter Enum representing the four quarters of a year
 * 季度枚举，表示一年中的四个季度
 *
 * <p>This enum represents the four quarters of a year: Q1 (Jan-Mar), Q2 (Apr-Jun),
 * Q3 (Jul-Sep), and Q4 (Oct-Dec).</p>
 * <p>此枚举表示一年中的四个季度：Q1（1-3月）、Q2（4-6月）、Q3（7-9月）、Q4（10-12月）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get quarter from value (1-4) - 从值(1-4)获取季度</li>
 *   <li>Get quarter from month - 从月份获取季度</li>
 *   <li>Get first/last month of quarter - 获取季度的第一个/最后一个月</li>
 *   <li>Calculate quarter length in days - 计算季度天数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get quarter from value
 * Quarter q1 = Quarter.of(1);          // Q1
 *
 * // Get quarter from month
 * Quarter q2 = Quarter.ofMonth(5);     // Q2 (May is in Q2)
 * Quarter q3 = Quarter.from(Month.AUGUST); // Q3
 *
 * // Get month information
 * int firstMonth = Quarter.Q1.firstMonth();  // 1 (January)
 * int lastMonth = Quarter.Q1.lastMonth();    // 3 (March)
 *
 * // Calculate length
 * int days = Quarter.Q1.length(false);  // 90 days (non-leap year)
 * int daysLeap = Quarter.Q1.length(true); // 91 days (leap year)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is inherently thread-safe) - 线程安全: 是（枚举本身是线程安全的）</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see YearQuarter
 * @since JDK 25, opencode-base-date V1.0.0
 */
public enum Quarter implements TemporalAccessor, TemporalQuery<Quarter> {

    /**
     * First quarter: January, February, March
     * 第一季度：一月、二月、三月
     */
    Q1(1),

    /**
     * Second quarter: April, May, June
     * 第二季度：四月、五月、六月
     */
    Q2(2),

    /**
     * Third quarter: July, August, September
     * 第三季度：七月、八月、九月
     */
    Q3(3),

    /**
     * Fourth quarter: October, November, December
     * 第四季度：十月、十一月、十二月
     */
    Q4(4);

    /**
     * The quarter value (1-4)
     */
    private final int value;

    /**
     * Cache of quarters for fast lookup
     */
    private static final Quarter[] QUARTERS = values();

    Quarter(int value) {
        this.value = value;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Gets the Quarter from a numeric value (1-4)
     * 从数值(1-4)获取季度
     *
     * @param quarter the quarter value from 1 to 4 | 季度值，范围1到4
     * @return the Quarter instance | 季度实例
     * @throws OpenDateException if the value is out of range | 如果值超出范围则抛出异常
     */
    public static Quarter of(int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw OpenDateException.invalidValue("quarter", quarter, "1-4");
        }
        return QUARTERS[quarter - 1];
    }

    /**
     * Gets the Quarter from a month value (1-12)
     * 从月份值(1-12)获取季度
     *
     * @param month the month value from 1 to 12 | 月份值，范围1到12
     * @return the Quarter containing the specified month | 包含指定月份的季度
     * @throws OpenDateException if the month is out of range | 如果月份超出范围则抛出异常
     */
    public static Quarter ofMonth(int month) {
        if (month < 1 || month > 12) {
            throw OpenDateException.invalidValue("month", month, "1-12");
        }
        return QUARTERS[(month - 1) / 3];
    }

    /**
     * Gets the Quarter from a Month enum
     * 从Month枚举获取季度
     *
     * @param month the Month enum | Month枚举
     * @return the Quarter containing the specified month | 包含指定月份的季度
     * @throws NullPointerException if month is null | 如果月份为null则抛出异常
     */
    public static Quarter from(Month month) {
        if (month == null) {
            throw new NullPointerException("month must not be null");
        }
        return ofMonth(month.getValue());
    }

    /**
     * Gets the Quarter from a TemporalAccessor
     * 从TemporalAccessor获取季度
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the Quarter | 季度
     * @throws OpenDateException if unable to convert | 如果无法转换则抛出异常
     */
    public static Quarter from(TemporalAccessor temporal) {
        if (temporal instanceof Quarter quarter) {
            return quarter;
        }
        if (temporal instanceof Month month) {
            return from(month);
        }
        try {
            int month = temporal.get(ChronoField.MONTH_OF_YEAR);
            return ofMonth(month);
        } catch (Exception e) {
            throw new OpenDateException("Unable to obtain Quarter from temporal: " + temporal, e);
        }
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the quarter value (1-4)
     * 获取季度值(1-4)
     *
     * @return the quarter value from 1 to 4 | 季度值，范围1到4
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the first month of this quarter (1, 4, 7, or 10)
     * 获取季度的第一个月(1, 4, 7, 或 10)
     *
     * @return the first month value | 第一个月的值
     */
    public int firstMonth() {
        return (value - 1) * 3 + 1;
    }

    /**
     * Gets the last month of this quarter (3, 6, 9, or 12)
     * 获取季度的最后一个月(3, 6, 9, 或 12)
     *
     * @return the last month value | 最后一个月的值
     */
    public int lastMonth() {
        return value * 3;
    }

    /**
     * Gets the first Month enum of this quarter
     * 获取季度的第一个Month枚举
     *
     * @return the first Month | 第一个月
     */
    public Month firstMonthOfQuarter() {
        return Month.of(firstMonth());
    }

    /**
     * Gets the last Month enum of this quarter
     * 获取季度的最后一个Month枚举
     *
     * @return the last Month | 最后一个月
     */
    public Month lastMonthOfQuarter() {
        return Month.of(lastMonth());
    }

    /**
     * Gets the length of this quarter in days
     * 获取季度的天数
     *
     * @param leapYear whether it's a leap year | 是否为闰年
     * @return the number of days in this quarter | 季度天数
     */
    public int length(boolean leapYear) {
        return switch (this) {
            case Q1 -> leapYear ? 91 : 90;  // Jan(31) + Feb(28/29) + Mar(31)
            case Q2 -> 91;                   // Apr(30) + May(31) + Jun(30)
            case Q3 -> 92;                   // Jul(31) + Aug(31) + Sep(30)
            case Q4 -> 92;                   // Oct(31) + Nov(30) + Dec(31)
        };
    }

    // ==================== Calculation Methods | 计算方法 ====================

    /**
     * Gets the next quarter
     * 获取下一个季度
     *
     * @return the next quarter (Q1 -> Q2 -> Q3 -> Q4 -> Q1) | 下一个季度
     */
    public Quarter next() {
        return QUARTERS[value % 4];
    }

    /**
     * Gets the previous quarter
     * 获取上一个季度
     *
     * @return the previous quarter (Q4 -> Q3 -> Q2 -> Q1 -> Q4) | 上一个季度
     */
    public Quarter previous() {
        return QUARTERS[(value + 2) % 4];
    }

    /**
     * Adds quarters to this quarter
     * 在此季度基础上加季度数
     *
     * @param quarters the quarters to add | 要加的季度数
     * @return the resulting quarter | 结果季度
     */
    public Quarter plus(int quarters) {
        int result = ((value - 1 + quarters) % 4 + 4) % 4;
        return QUARTERS[result];
    }

    /**
     * Subtracts quarters from this quarter
     * 在此季度基础上减季度数
     *
     * @param quarters the quarters to subtract | 要减的季度数
     * @return the resulting quarter | 结果季度
     */
    public Quarter minus(int quarters) {
        return plus(-quarters);
    }

    // ==================== TemporalAccessor Implementation | TemporalAccessor实现 ====================

    @Override
    public boolean isSupported(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return true;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return firstMonth();
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return ValueRange.of(firstMonth(), lastMonth());
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    // ==================== TemporalQuery Implementation | TemporalQuery实现 ====================

    @Override
    public Quarter queryFrom(TemporalAccessor temporal) {
        return from(temporal);
    }

    /**
     * Gets a query for extracting the Quarter from a temporal
     * 获取从时间对象提取季度的查询
     *
     * @return the quarter query | 季度查询
     */
    public static TemporalQuery<Quarter> query() {
        return Quarter::from;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if the given month is in this quarter
     * 检查给定月份是否在此季度内
     *
     * @param month the month to check | 要检查的月份
     * @return true if the month is in this quarter | 如果月份在此季度内返回true
     */
    public boolean contains(int month) {
        return month >= firstMonth() && month <= lastMonth();
    }

    /**
     * Checks if the given Month is in this quarter
     * 检查给定Month是否在此季度内
     *
     * @param month the Month to check | 要检查的月份
     * @return true if the Month is in this quarter | 如果月份在此季度内返回true
     */
    public boolean contains(Month month) {
        return month != null && contains(month.getValue());
    }
}
