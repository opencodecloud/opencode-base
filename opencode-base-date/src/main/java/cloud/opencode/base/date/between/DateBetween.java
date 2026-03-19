package cloud.opencode.base.date.between;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * Calculates the difference between two dates or date-times
 * 计算两个日期或日期时间之间的差异
 *
 * <p>This class provides various methods to calculate the difference between two temporal
 * objects in different units (days, weeks, months, years, hours, minutes, seconds, etc.).</p>
 * <p>此类提供多种方法来计算两个时间对象之间的差异，支持不同的时间单位
 * （天、周、月、年、小时、分钟、秒等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Calculate differences in various units - 计算各种时间单位的差异</li>
 *   <li>Support for LocalDate and LocalDateTime - 支持LocalDate和LocalDateTime</li>
 *   <li>Absolute and signed differences - 绝对值和带符号的差异</li>
 *   <li>Period and Duration extraction - Period和Duration提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate start = LocalDate.of(2024, 1, 1);
 * LocalDate end = LocalDate.of(2024, 12, 31);
 *
 * DateBetween between = DateBetween.of(start, end);
 * long days = between.days();           // 365
 * long weeks = between.weeks();         // 52
 * long months = between.months();       // 11
 * Period period = between.toPeriod();   // P11M30D
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
public final class DateBetween {

    /**
     * The start temporal
     */
    private final Temporal start;

    /**
     * The end temporal
     */
    private final Temporal end;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private DateBetween(Temporal start, Temporal end) {
        this.start = Objects.requireNonNull(start, "start must not be null");
        this.end = Objects.requireNonNull(end, "end must not be null");
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a DateBetween from two LocalDate objects
     * 从两个LocalDate对象创建DateBetween
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the DateBetween instance | DateBetween实例
     */
    public static DateBetween of(LocalDate start, LocalDate end) {
        return new DateBetween(start, end);
    }

    /**
     * Creates a DateBetween from two LocalDateTime objects
     * 从两个LocalDateTime对象创建DateBetween
     *
     * @param start the start date-time | 开始日期时间
     * @param end   the end date-time | 结束日期时间
     * @return the DateBetween instance | DateBetween实例
     */
    public static DateBetween of(LocalDateTime start, LocalDateTime end) {
        return new DateBetween(start, end);
    }

    /**
     * Creates a DateBetween from any two Temporal objects
     * 从任意两个Temporal对象创建DateBetween
     *
     * @param start the start temporal | 开始时间
     * @param end   the end temporal | 结束时间
     * @return the DateBetween instance | DateBetween实例
     */
    public static DateBetween between(Temporal start, Temporal end) {
        return new DateBetween(start, end);
    }

    // ==================== Day Calculations | 天数计算 ====================

    /**
     * Gets the number of days between start and end (signed)
     * 获取开始和结束之间的天数（带符号）
     *
     * @return the number of days | 天数
     */
    public long days() {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Gets the absolute number of days between start and end
     * 获取开始和结束之间的天数绝对值
     *
     * @return the absolute number of days | 天数绝对值
     */
    public long absDays() {
        return Math.abs(days());
    }

    // ==================== Week Calculations | 周数计算 ====================

    /**
     * Gets the number of complete weeks between start and end (signed)
     * 获取开始和结束之间的完整周数（带符号）
     *
     * @return the number of weeks | 周数
     */
    public long weeks() {
        return ChronoUnit.WEEKS.between(start, end);
    }

    /**
     * Gets the absolute number of weeks between start and end
     * 获取开始和结束之间的周数绝对值
     *
     * @return the absolute number of weeks | 周数绝对值
     */
    public long absWeeks() {
        return Math.abs(weeks());
    }

    // ==================== Month Calculations | 月数计算 ====================

    /**
     * Gets the number of complete months between start and end (signed)
     * 获取开始和结束之间的完整月数（带符号）
     *
     * @return the number of months | 月数
     */
    public long months() {
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Gets the absolute number of months between start and end
     * 获取开始和结束之间的月数绝对值
     *
     * @return the absolute number of months | 月数绝对值
     */
    public long absMonths() {
        return Math.abs(months());
    }

    // ==================== Year Calculations | 年数计算 ====================

    /**
     * Gets the number of complete years between start and end (signed)
     * 获取开始和结束之间的完整年数（带符号）
     *
     * @return the number of years | 年数
     */
    public long years() {
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Gets the absolute number of years between start and end
     * 获取开始和结束之间的年数绝对值
     *
     * @return the absolute number of years | 年数绝对值
     */
    public long absYears() {
        return Math.abs(years());
    }

    // ==================== Time Calculations | 时间计算 ====================

    /**
     * Gets the number of hours between start and end (signed)
     * 获取开始和结束之间的小时数（带符号）
     *
     * @return the number of hours | 小时数
     */
    public long hours() {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Gets the number of minutes between start and end (signed)
     * 获取开始和结束之间的分钟数（带符号）
     *
     * @return the number of minutes | 分钟数
     */
    public long minutes() {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Gets the number of seconds between start and end (signed)
     * 获取开始和结束之间的秒数（带符号）
     *
     * @return the number of seconds | 秒数
     */
    public long seconds() {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Gets the number of milliseconds between start and end (signed)
     * 获取开始和结束之间的毫秒数（带符号）
     *
     * @return the number of milliseconds | 毫秒数
     */
    public long millis() {
        return ChronoUnit.MILLIS.between(start, end);
    }

    // ==================== Period & Duration | Period和Duration ====================

    /**
     * Converts to a Period (for LocalDate only)
     * 转换为Period（仅用于LocalDate）
     *
     * @return the Period | Period对象
     * @throws ClassCastException if temporals are not LocalDate | 如果temporal不是LocalDate则抛出异常
     */
    public Period toPeriod() {
        if (start instanceof LocalDate startDate && end instanceof LocalDate endDate) {
            return Period.between(startDate, endDate);
        }
        if (start instanceof LocalDateTime startDt && end instanceof LocalDateTime endDt) {
            return Period.between(startDt.toLocalDate(), endDt.toLocalDate());
        }
        throw new UnsupportedOperationException("Cannot convert to Period for temporal types: " +
                start.getClass().getSimpleName() + " and " + end.getClass().getSimpleName());
    }

    /**
     * Converts to a Duration (for LocalDateTime only)
     * 转换为Duration（仅用于LocalDateTime）
     *
     * @return the Duration | Duration对象
     * @throws ClassCastException if temporals are not LocalDateTime | 如果temporal不是LocalDateTime则抛出异常
     */
    public Duration toDuration() {
        if (start instanceof LocalDateTime startDt && end instanceof LocalDateTime endDt) {
            return Duration.between(startDt, endDt);
        }
        if (start instanceof LocalDate startDate && end instanceof LocalDate endDate) {
            return Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());
        }
        throw new UnsupportedOperationException("Cannot convert to Duration for temporal types: " +
                start.getClass().getSimpleName() + " and " + end.getClass().getSimpleName());
    }

    // ==================== Detailed Breakdown | 详细分解 ====================

    /**
     * Gets a detailed breakdown of the difference
     * 获取差异的详细分解
     *
     * @return the DateDiff with detailed breakdown | 包含详细分解的DateDiff
     */
    public DateDiff toDateDiff() {
        return DateDiff.of(start, end);
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the start temporal
     * 获取开始时间
     *
     * @return the start temporal | 开始时间
     */
    public Temporal getStart() {
        return start;
    }

    /**
     * Gets the end temporal
     * 获取结束时间
     *
     * @return the end temporal | 结束时间
     */
    public Temporal getEnd() {
        return end;
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateBetween that)) return false;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "DateBetween[" + start + " to " + end + ", days=" + days() + "]";
    }
}
