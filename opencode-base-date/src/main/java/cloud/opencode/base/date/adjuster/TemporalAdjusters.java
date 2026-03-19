package cloud.opencode.base.date.adjuster;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Objects;

/**
 * Temporal Adjusters - Extended temporal adjusters for business use cases
 * 时间调整器 - 用于业务场景的扩展时间调整器
 *
 * <p>Provides additional temporal adjusters beyond JDK's TemporalAdjusters,
 * focused on business and common use cases.</p>
 * <p>提供比JDK的TemporalAdjusters更多的时间调整器，专注于业务和常见用例。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Quarter adjusters - 季度调整器</li>
 *   <li>Half-year adjusters - 半年调整器</li>
 *   <li>Week adjusters (configurable first day) - 周调整器（可配置首日）</li>
 *   <li>Business/workday adjusters - 工作日调整器</li>
 *   <li>Time of day adjusters - 日内时间调整器</li>
 *   <li>Composite adjusters - 组合调整器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate date = LocalDate.of(2024, 3, 15);
 *
 * // Quarter adjusters
 * LocalDate quarterStart = date.with(TemporalAdjusters.firstDayOfQuarter()); // 1月1日
 * LocalDate quarterEnd = date.with(TemporalAdjusters.lastDayOfQuarter());    // 3月31日
 *
 * // Workday adjusters
 * LocalDate nextWorkday = date.with(TemporalAdjusters.nextWorkday());
 * LocalDate plus5Workdays = date.with(TemporalAdjusters.plusWorkdays(5));
 *
 * // Time adjusters
 * LocalDateTime dayStart = dateTime.with(TemporalAdjusters.startOfDay());  // 00:00:00
 * LocalDateTime dayEnd = dateTime.with(TemporalAdjusters.endOfDay());      // 23:59:59.999999999
 *
 * // Composite adjusters
 * TemporalAdjuster combined = TemporalAdjusters.compose(
 *     TemporalAdjusters.firstDayOfNextMonth(),
 *     TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (with explicit null checks on compose/andThen) - 空值安全: 是（compose/andThen有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see java.time.temporal.TemporalAdjusters
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class TemporalAdjusters {

    private TemporalAdjusters() {
    }

    // ============ JDK Aliases | JDK别名 ============

    /**
     * Returns the first day of month adjuster.
     * 返回月初调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfMonth() {
        return java.time.temporal.TemporalAdjusters.firstDayOfMonth();
    }

    /**
     * Returns the last day of month adjuster.
     * 返回月末调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfMonth() {
        return java.time.temporal.TemporalAdjusters.lastDayOfMonth();
    }

    /**
     * Returns the first day of next month adjuster.
     * 返回下月初调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfNextMonth() {
        return java.time.temporal.TemporalAdjusters.firstDayOfNextMonth();
    }

    /**
     * Returns the first day of year adjuster.
     * 返回年初调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfYear() {
        return java.time.temporal.TemporalAdjusters.firstDayOfYear();
    }

    /**
     * Returns the last day of year adjuster.
     * 返回年末调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfYear() {
        return java.time.temporal.TemporalAdjusters.lastDayOfYear();
    }

    /**
     * Returns the first day of next year adjuster.
     * 返回下年初调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfNextYear() {
        return java.time.temporal.TemporalAdjusters.firstDayOfNextYear();
    }

    /**
     * Returns the next day of week adjuster.
     * 返回下一个周几调整器。
     *
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        return java.time.temporal.TemporalAdjusters.next(dayOfWeek);
    }

    /**
     * Returns the next or same day of week adjuster.
     * 返回下一个或相同周几调整器。
     *
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        return java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek);
    }

    /**
     * Returns the previous day of week adjuster.
     * 返回上一个周几调整器。
     *
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        return java.time.temporal.TemporalAdjusters.previous(dayOfWeek);
    }

    /**
     * Returns the previous or same day of week adjuster.
     * 返回上一个或相同周几调整器。
     *
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        return java.time.temporal.TemporalAdjusters.previousOrSame(dayOfWeek);
    }

    /**
     * Returns the nth day of week in month adjuster.
     * 返回月内第N个周几调整器。
     *
     * @param ordinal   the ordinal (1-5) | 序数（1-5）
     * @param dayOfWeek the day of week | 周几
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        return java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek);
    }

    // ============ Quarter Adjusters | 季度调整器 ============

    /**
     * Returns the first day of quarter adjuster.
     * 返回季度首日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfQuarter() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
            return date.withMonth(quarterStartMonth).withDayOfMonth(1);
        };
    }

    /**
     * Returns the last day of quarter adjuster.
     * 返回季度末日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfQuarter() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int quarterEndMonth = ((month - 1) / 3 + 1) * 3;
            LocalDate endDate = date.withMonth(quarterEndMonth);
            return endDate.withDayOfMonth(endDate.lengthOfMonth());
        };
    }

    /**
     * Returns the first day of next quarter adjuster.
     * 返回下季度首日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfNextQuarter() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int nextQuarterStartMonth = ((month - 1) / 3 + 1) * 3 + 1;
            if (nextQuarterStartMonth > 12) {
                return date.plusYears(1).withMonth(1).withDayOfMonth(1);
            }
            return date.withMonth(nextQuarterStartMonth).withDayOfMonth(1);
        };
    }

    // ============ Half-Year Adjusters | 半年调整器 ============

    /**
     * Returns the first day of half-year adjuster.
     * 返回半年首日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfHalf() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            int halfStartMonth = month <= 6 ? 1 : 7;
            return date.withMonth(halfStartMonth).withDayOfMonth(1);
        };
    }

    /**
     * Returns the last day of half-year adjuster.
     * 返回半年末日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfHalf() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int month = date.getMonthValue();
            if (month <= 6) {
                return date.withMonth(6).withDayOfMonth(30);
            }
            return date.withMonth(12).withDayOfMonth(31);
        };
    }

    // ============ Week Adjusters | 周调整器 ============

    /**
     * Returns the first day of week adjuster (Monday).
     * 返回周首日调整器（周一）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfWeek() {
        return firstDayOfWeek(DayOfWeek.MONDAY);
    }

    /**
     * Returns the first day of week adjuster for specific first day.
     * 返回指定首日的周首日调整器。
     *
     * @param firstDayOfWeek the first day of week | 周首日
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster firstDayOfWeek(DayOfWeek firstDayOfWeek) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int daysToSubtract = (date.getDayOfWeek().getValue() - firstDayOfWeek.getValue() + 7) % 7;
            return date.minusDays(daysToSubtract);
        };
    }

    /**
     * Returns the last day of week adjuster (Sunday).
     * 返回周末日调整器（周日）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfWeek() {
        return lastDayOfWeek(DayOfWeek.SUNDAY);
    }

    /**
     * Returns the last day of week adjuster for specific last day.
     * 返回指定末日的周末日调整器。
     *
     * @param lastDayOfWeek the last day of week | 周末日
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster lastDayOfWeek(DayOfWeek lastDayOfWeek) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int daysToAdd = (lastDayOfWeek.getValue() - date.getDayOfWeek().getValue() + 7) % 7;
            return date.plusDays(daysToAdd);
        };
    }

    // ============ Workday Adjusters | 工作日调整器 ============

    /**
     * Returns the next workday adjuster (skips weekends).
     * 返回下一个工作日调整器（跳过周末）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nextWorkday() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal).plusDays(1);
            while (isWeekend(date)) {
                date = date.plusDays(1);
            }
            return date;
        };
    }

    /**
     * Returns the previous workday adjuster (skips weekends).
     * 返回上一个工作日调整器（跳过周末）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster previousWorkday() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal).minusDays(1);
            while (isWeekend(date)) {
                date = date.minusDays(1);
            }
            return date;
        };
    }

    /**
     * Returns the next or same workday adjuster.
     * 返回下一个或相同工作日调整器。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nextOrSameWorkday() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            while (isWeekend(date)) {
                date = date.plusDays(1);
            }
            return date;
        };
    }

    /**
     * Returns an adjuster that adds workdays.
     * 返回添加工作日的调整器。
     *
     * @param days number of workdays to add | 要添加的工作日数
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster plusWorkdays(int days) {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            int remaining = Math.abs(days);
            int direction = days >= 0 ? 1 : -1;

            while (remaining > 0) {
                date = date.plusDays(direction);
                if (!isWeekend(date)) {
                    remaining--;
                }
            }
            return date;
        };
    }

    /**
     * Returns an adjuster that subtracts workdays.
     * 返回减去工作日的调整器。
     *
     * @param days number of workdays to subtract | 要减去的工作日数
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster minusWorkdays(int days) {
        return plusWorkdays(-days);
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    // ============ Time of Day Adjusters | 日内时间调整器 ============

    /**
     * Returns the start of day adjuster (00:00:00.000000000).
     * 返回日初调整器（00:00:00.000000000）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster startOfDay() {
        return temporal -> {
            if (temporal instanceof LocalDateTime ldt) {
                return ldt.with(LocalTime.MIN);
            }
            return temporal;
        };
    }

    /**
     * Returns the end of day adjuster (23:59:59.999999999).
     * 返回日末调整器（23:59:59.999999999）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster endOfDay() {
        return temporal -> {
            if (temporal instanceof LocalDateTime ldt) {
                return ldt.with(LocalTime.MAX);
            }
            return temporal;
        };
    }

    /**
     * Returns the noon adjuster (12:00:00).
     * 返回正午调整器（12:00:00）。
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster noon() {
        return temporal -> {
            if (temporal instanceof LocalDateTime ldt) {
                return ldt.with(LocalTime.NOON);
            }
            return temporal;
        };
    }

    /**
     * Returns an adjuster to specific hour.
     * 返回指定小时调整器。
     *
     * @param hour the hour (0-23) | 小时（0-23）
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster atHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }
        return temporal -> {
            if (temporal instanceof LocalDateTime ldt) {
                return ldt.withHour(hour).withMinute(0).withSecond(0).withNano(0);
            }
            return temporal;
        };
    }

    // ============ Composite Adjusters | 组合调整器 ============

    /**
     * Composes multiple adjusters into one.
     * 将多个调整器组合成一个。
     *
     * @param adjusters the adjusters to compose | 要组合的调整器
     * @return the composite adjuster | 组合调整器
     */
    public static TemporalAdjuster compose(TemporalAdjuster... adjusters) {
        Objects.requireNonNull(adjusters, "adjusters cannot be null");
        if (adjusters.length == 0) {
            return temporal -> temporal;
        }
        return temporal -> {
            Temporal result = temporal;
            for (TemporalAdjuster adjuster : adjusters) {
                result = result.with(adjuster);
            }
            return result;
        };
    }

    /**
     * Returns an adjuster that applies first adjuster then second.
     * 返回先应用第一个调整器再应用第二个的调整器。
     *
     * @param first  the first adjuster | 第一个调整器
     * @param second the second adjuster | 第二个调整器
     * @return the composite adjuster | 组合调整器
     */
    public static TemporalAdjuster andThen(TemporalAdjuster first, TemporalAdjuster second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return temporal -> temporal.with(first).with(second);
    }

    // ============ Custom Adjusters | 自定义调整器 ============

    /**
     * Creates adjuster from lambda.
     * 从lambda创建调整器。
     *
     * @param adjuster the adjuster function | 调整器函数
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster of(TemporalAdjuster adjuster) {
        return Objects.requireNonNull(adjuster, "adjuster cannot be null");
    }
}
