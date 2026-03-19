package cloud.opencode.base.date.adjuster;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

/**
 * Extended temporal adjusters for common date operations
 * 扩展的时间调整器，用于常见日期操作
 *
 * <p>This class provides additional temporal adjusters beyond those in
 * java.time.temporal.TemporalAdjusters, focused on business and common use cases.</p>
 * <p>此类提供比java.time.temporal.TemporalAdjusters更多的时间调整器，
 * 专注于业务和常见用例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Start/end of year, quarter, month, week - 年、季、月、周的开始/结束</li>
 *   <li>Next/previous occurrence adjusters - 下一个/上一个发生调整器</li>
 *   <li>Nth occurrence adjusters - 第N次发生调整器</li>
 *   <li>Business day adjusters - 工作日调整器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate date = LocalDate.of(2024, 6, 15);
 *
 * // Start of quarter
 * LocalDate startOfQ = date.with(DateAdjusters.startOfQuarter());
 * // 2024-04-01
 *
 * // End of quarter
 * LocalDate endOfQ = date.with(DateAdjusters.endOfQuarter());
 * // 2024-06-30
 *
 * // Next Monday
 * LocalDate nextMon = date.with(DateAdjusters.nextOrSame(DayOfWeek.MONDAY));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No (callers must provide non-null arguments) - 空值安全: 否（调用者必须提供非空参数）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see java.time.temporal.TemporalAdjusters
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class DateAdjusters {

    // ==================== Private Constructor | 私有构造函数 ====================

    private DateAdjusters() {
        // Utility class
    }

    // ==================== Year Adjusters | 年调整器 ====================

    /**
     * Returns the start of the year adjuster
     * 返回年初调整器
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfYear() {
        return temporal -> temporal.with(ChronoField.DAY_OF_YEAR, 1);
    }

    /**
     * Returns the end of the year adjuster
     * 返回年末调整器
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfYear() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            return temporal.with(ChronoField.DAY_OF_YEAR, date.lengthOfYear());
        };
    }

    // ==================== Quarter Adjusters | 季度调整器 ====================

    /**
     * Returns the start of the current quarter adjuster
     * 返回当前季度开始调整器
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfQuarter() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
            return date.withMonth(quarterStartMonth).withDayOfMonth(1);
        };
    }

    /**
     * Returns the end of the current quarter adjuster
     * 返回当前季度结束调整器
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfQuarter() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int quarterEndMonth = ((month - 1) / 3 + 1) * 3;
            LocalDate endDate = date.withMonth(quarterEndMonth);
            return endDate.withDayOfMonth(endDate.lengthOfMonth());
        };
    }

    /**
     * Returns the start of a specific quarter adjuster
     * 返回特定季度开始调整器
     *
     * @param quarter the quarter (1-4) | 季度（1-4）
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfQuarter(int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
        int startMonth = (quarter - 1) * 3 + 1;
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            return date.withMonth(startMonth).withDayOfMonth(1);
        };
    }

    // ==================== Month Adjusters | 月调整器 ====================

    /**
     * Returns the start of the month adjuster (alias for firstDayOfMonth)
     * 返回月初调整器（firstDayOfMonth的别名）
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfMonth() {
        return TemporalAdjusters.firstDayOfMonth();
    }

    /**
     * Returns the end of the month adjuster (alias for lastDayOfMonth)
     * 返回月末调整器（lastDayOfMonth的别名）
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfMonth() {
        return TemporalAdjusters.lastDayOfMonth();
    }

    // ==================== Week Adjusters | 周调整器 ====================

    /**
     * Returns the start of the week adjuster (Monday)
     * 返回周初调整器（周一）
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfWeek() {
        return startOfWeek(DayOfWeek.MONDAY);
    }

    /**
     * Returns the start of the week adjuster for a specific first day
     * 返回特定首日的周初调整器
     *
     * @param firstDayOfWeek the first day of week | 一周的第一天
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfWeek(DayOfWeek firstDayOfWeek) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int daysToSubtract = (date.getDayOfWeek().getValue() - firstDayOfWeek.getValue() + 7) % 7;
            return date.minusDays(daysToSubtract);
        };
    }

    /**
     * Returns the end of the week adjuster (Sunday)
     * 返回周末调整器（周日）
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfWeek() {
        return endOfWeek(DayOfWeek.SUNDAY);
    }

    /**
     * Returns the end of the week adjuster for a specific last day
     * 返回特定末日的周末调整器
     *
     * @param lastDayOfWeek the last day of week | 一周的最后一天
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfWeek(DayOfWeek lastDayOfWeek) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int daysToAdd = (lastDayOfWeek.getValue() - date.getDayOfWeek().getValue() + 7) % 7;
            return date.plusDays(daysToAdd);
        };
    }

    // ==================== Nth Day Adjusters | 第N天调整器 ====================

    /**
     * Returns the nth day of the month adjuster
     * 返回月份第N天调整器
     *
     * @param day the day of month | 月份的日期
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster dayOfMonth(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Day must be between 1 and 31");
        }
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int maxDay = date.lengthOfMonth();
            return date.withDayOfMonth(Math.min(day, maxDay));
        };
    }

    /**
     * Returns the nth weekday of the month adjuster
     * 返回月份第N个工作日调整器
     *
     * @param ordinal  the ordinal (1-5, or -1 for last) | 序数（1-5，或-1表示最后）
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nthDayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        if (ordinal == -1) {
            return TemporalAdjusters.lastInMonth(dayOfWeek);
        }
        if (ordinal < 1 || ordinal > 5) {
            throw new IllegalArgumentException("Ordinal must be between 1 and 5, or -1 for last");
        }
        return TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek);
    }

    // ==================== Relative Adjusters | 相对调整器 ====================

    /**
     * Returns an adjuster for the next or same occurrence of a day of week
     * 返回下一个或相同星期几的调整器
     *
     * @param dayOfWeek the day of week | 星期几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.nextOrSame(dayOfWeek);
    }

    /**
     * Returns an adjuster for the previous or same occurrence of a day of week
     * 返回上一个或相同星期几的调整器
     *
     * @param dayOfWeek the day of week | 星期几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.previousOrSame(dayOfWeek);
    }

    /**
     * Returns an adjuster for the next occurrence of a day of week (strictly after)
     * 返回下一个星期几的调整器（严格之后）
     *
     * @param dayOfWeek the day of week | 星期几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.next(dayOfWeek);
    }

    /**
     * Returns an adjuster for the previous occurrence of a day of week (strictly before)
     * 返回上一个星期几的调整器（严格之前）
     *
     * @param dayOfWeek the day of week | 星期几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.previous(dayOfWeek);
    }

    // ==================== Half Year Adjusters | 半年调整器 ====================

    /**
     * Returns the start of the first half of the year
     * 返回上半年开始
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfFirstHalf() {
        return temporal -> LocalDate.from(temporal).withMonth(1).withDayOfMonth(1);
    }

    /**
     * Returns the end of the first half of the year
     * 返回上半年结束
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfFirstHalf() {
        return temporal -> LocalDate.from(temporal).withMonth(6).withDayOfMonth(30);
    }

    /**
     * Returns the start of the second half of the year
     * 返回下半年开始
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfSecondHalf() {
        return temporal -> LocalDate.from(temporal).withMonth(7).withDayOfMonth(1);
    }

    /**
     * Returns the end of the second half of the year
     * 返回下半年结束
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfSecondHalf() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            return date.withMonth(12).withDayOfMonth(31);
        };
    }

    // ==================== Special Date Adjusters | 特殊日期调整器 ====================

    /**
     * Returns an adjuster to the next occurrence of a specific month and day
     * 返回下一个特定月日的调整器
     *
     * @param month the month | 月份
     * @param day   the day of month | 日期
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nextMonthDay(Month month, int day) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            LocalDate target = date.withMonth(month.getValue()).withDayOfMonth(day);
            if (!target.isAfter(date)) {
                target = target.plusYears(1);
            }
            return target;
        };
    }

    /**
     * Returns an adjuster for adding/subtracting business days
     * 返回添加/减去工作日的调整器
     *
     * @param days the number of business days (can be negative) | 工作日数（可为负）
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster plusBusinessDays(int days) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int remaining = Math.abs(days);
            int direction = days >= 0 ? 1 : -1;

            while (remaining > 0) {
                date = date.plusDays(direction);
                DayOfWeek dow = date.getDayOfWeek();
                if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                    remaining--;
                }
            }
            return date;
        };
    }

    /**
     * Returns an adjuster to the nearest weekday
     * 返回最近工作日的调整器
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nearestWeekday() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            DayOfWeek dow = date.getDayOfWeek();

            return switch (dow) {
                case SATURDAY -> date.minusDays(1);
                case SUNDAY -> date.plusDays(1);
                default -> date;
            };
        };
    }

    // ==================== Utility Adjusters | 工具调整器 ====================

    /**
     * Returns an adjuster that adds the specified number of units
     * 返回添加指定数量单位的调整器
     *
     * @param amount the amount to add | 要添加的数量
     * @param unit   the unit | 单位
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster plus(long amount, ChronoUnit unit) {
        return temporal -> temporal.plus(amount, unit);
    }

    /**
     * Returns an adjuster that subtracts the specified number of units
     * 返回减去指定数量单位的调整器
     *
     * @param amount the amount to subtract | 要减去的数量
     * @param unit   the unit | 单位
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster minus(long amount, ChronoUnit unit) {
        return temporal -> temporal.minus(amount, unit);
    }
}
