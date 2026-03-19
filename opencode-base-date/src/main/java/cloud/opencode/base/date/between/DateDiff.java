package cloud.opencode.base.date.between;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * Detailed date difference breakdown with years, months, and days
 * 详细的日期差异分解，包含年、月、日
 *
 * <p>This class provides a detailed breakdown of the difference between two dates,
 * showing the exact number of years, months, days, hours, minutes, and seconds.</p>
 * <p>此类提供两个日期之间差异的详细分解，显示精确的年数、月数、天数、
 * 小时数、分钟数和秒数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Complete breakdown in human-readable units - 完整的人类可读单位分解</li>
 *   <li>Support for dates and date-times - 支持日期和日期时间</li>
 *   <li>Formatted output - 格式化输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate birth = LocalDate.of(1990, 5, 15);
 * LocalDate today = LocalDate.of(2024, 3, 20);
 *
 * DateDiff diff = DateDiff.of(birth, today);
 * System.out.println(diff.getYears());   // 33
 * System.out.println(diff.getMonths());  // 10
 * System.out.println(diff.getDays());    // 5
 * System.out.println(diff);              // "33 years, 10 months, 5 days"
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
public final class DateDiff {

    /**
     * The start temporal
     */
    private final Temporal start;

    /**
     * The end temporal
     */
    private final Temporal end;

    /**
     * Number of complete years
     */
    private final int years;

    /**
     * Number of complete months (0-11)
     */
    private final int months;

    /**
     * Number of days (0-30)
     */
    private final int days;

    /**
     * Number of hours (0-23)
     */
    private final int hours;

    /**
     * Number of minutes (0-59)
     */
    private final int minutes;

    /**
     * Number of seconds (0-59)
     */
    private final int seconds;

    /**
     * Whether the difference is negative (end before start)
     */
    private final boolean negative;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private DateDiff(Temporal start, Temporal end, int years, int months, int days,
                     int hours, int minutes, int seconds, boolean negative) {
        this.start = start;
        this.end = end;
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.negative = negative;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a DateDiff from two LocalDate objects
     * 从两个LocalDate对象创建DateDiff
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the DateDiff instance | DateDiff实例
     */
    public static DateDiff of(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        boolean negative = end.isBefore(start);
        LocalDate from = negative ? end : start;
        LocalDate to = negative ? start : end;

        Period period = Period.between(from, to);

        return new DateDiff(start, end,
                period.getYears(), period.getMonths(), period.getDays(),
                0, 0, 0, negative);
    }

    /**
     * Creates a DateDiff from two LocalDateTime objects
     * 从两个LocalDateTime对象创建DateDiff
     *
     * @param start the start date-time | 开始日期时间
     * @param end   the end date-time | 结束日期时间
     * @return the DateDiff instance | DateDiff实例
     */
    public static DateDiff of(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        boolean negative = end.isBefore(start);
        LocalDateTime from = negative ? end : start;
        LocalDateTime to = negative ? start : end;

        // Calculate date part
        Period period = Period.between(from.toLocalDate(), to.toLocalDate());

        // Adjust for time component
        LocalDateTime adjusted = from.plusYears(period.getYears())
                .plusMonths(period.getMonths())
                .plusDays(period.getDays());

        // If adjusted is after 'to', we need to reduce by one day
        int adjustDays = period.getDays();
        if (adjusted.isAfter(to)) {
            adjusted = adjusted.minusDays(1);
            adjustDays--;
        }

        // Calculate time differences
        long totalSeconds = ChronoUnit.SECONDS.between(adjusted, to);
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);

        return new DateDiff(start, end,
                period.getYears(), period.getMonths(), adjustDays,
                hours, minutes, seconds, negative);
    }

    /**
     * Creates a DateDiff from any two Temporal objects
     * 从任意两个Temporal对象创建DateDiff
     *
     * @param start the start temporal | 开始时间
     * @param end   the end temporal | 结束时间
     * @return the DateDiff instance | DateDiff实例
     */
    public static DateDiff of(Temporal start, Temporal end) {
        if (start instanceof LocalDate s && end instanceof LocalDate e) {
            return of(s, e);
        }
        if (start instanceof LocalDateTime s && end instanceof LocalDateTime e) {
            return of(s, e);
        }
        // Default: convert to LocalDateTime if possible
        if (start instanceof LocalDate s && end instanceof LocalDateTime e) {
            return of(s.atStartOfDay(), e);
        }
        if (start instanceof LocalDateTime s && end instanceof LocalDate e) {
            return of(s, e.atStartOfDay());
        }

        throw new UnsupportedOperationException("Unsupported temporal types: " +
                start.getClass().getSimpleName() + " and " + end.getClass().getSimpleName());
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the number of complete years
     * 获取完整年数
     *
     * @return the years | 年数
     */
    public int getYears() {
        return years;
    }

    /**
     * Gets the number of complete months (0-11)
     * 获取完整月数（0-11）
     *
     * @return the months | 月数
     */
    public int getMonths() {
        return months;
    }

    /**
     * Gets the number of days (0-30)
     * 获取天数（0-30）
     *
     * @return the days | 天数
     */
    public int getDays() {
        return days;
    }

    /**
     * Gets the number of hours (0-23)
     * 获取小时数（0-23）
     *
     * @return the hours | 小时数
     */
    public int getHours() {
        return hours;
    }

    /**
     * Gets the number of minutes (0-59)
     * 获取分钟数（0-59）
     *
     * @return the minutes | 分钟数
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Gets the number of seconds (0-59)
     * 获取秒数（0-59）
     *
     * @return the seconds | 秒数
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Checks if the difference is negative (end before start)
     * 检查差异是否为负（结束在开始之前）
     *
     * @return true if negative | 如果为负返回true
     */
    public boolean isNegative() {
        return negative;
    }

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

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts to total days (approximate for months/years)
     * 转换为总天数（月/年为近似值）
     *
     * @return the total days | 总天数
     */
    public long toTotalDays() {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Converts to Period (date part only)
     * 转换为Period（仅日期部分）
     *
     * @return the Period | Period对象
     */
    public Period toPeriod() {
        Period p = Period.of(years, months, days);
        return negative ? p.negated() : p;
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Formats as a human-readable string
     * 格式化为人类可读的字符串
     *
     * @return the formatted string | 格式化字符串
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("-");
        }

        boolean hasValue = false;
        if (years > 0) {
            sb.append(years).append(" year").append(years > 1 ? "s" : "");
            hasValue = true;
        }
        if (months > 0) {
            if (hasValue) sb.append(", ");
            sb.append(months).append(" month").append(months > 1 ? "s" : "");
            hasValue = true;
        }
        if (days > 0) {
            if (hasValue) sb.append(", ");
            sb.append(days).append(" day").append(days > 1 ? "s" : "");
            hasValue = true;
        }
        if (hours > 0 || minutes > 0 || seconds > 0) {
            if (hasValue) sb.append(", ");
            if (hours > 0) {
                sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
                if (minutes > 0 || seconds > 0) sb.append(", ");
            }
            if (minutes > 0) {
                sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
                if (seconds > 0) sb.append(", ");
            }
            if (seconds > 0) {
                sb.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
            }
            hasValue = true;
        }

        if (!hasValue) {
            return "0 days";
        }

        return sb.toString();
    }

    /**
     * Formats as a Chinese human-readable string
     * 格式化为中文人类可读的字符串
     *
     * @return the formatted string | 格式化字符串
     */
    public String formatChinese() {
        StringBuilder sb = new StringBuilder();
        if (negative) {
            sb.append("负");
        }

        boolean hasValue = false;
        if (years > 0) {
            sb.append(years).append("年");
            hasValue = true;
        }
        if (months > 0) {
            sb.append(months).append("个月");
            hasValue = true;
        }
        if (days > 0) {
            sb.append(days).append("天");
            hasValue = true;
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
            hasValue = true;
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
            hasValue = true;
        }
        if (seconds > 0) {
            sb.append(seconds).append("秒");
            hasValue = true;
        }

        if (!hasValue) {
            return "0天";
        }

        return sb.toString();
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateDiff dateDiff)) return false;
        return years == dateDiff.years && months == dateDiff.months && days == dateDiff.days &&
                hours == dateDiff.hours && minutes == dateDiff.minutes && seconds == dateDiff.seconds &&
                negative == dateDiff.negative;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, days, hours, minutes, seconds, negative);
    }

    @Override
    public String toString() {
        return format();
    }
}
