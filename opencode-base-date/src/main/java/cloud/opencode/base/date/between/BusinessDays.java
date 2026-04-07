package cloud.opencode.base.date.between;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility for calculating business day differences and offsets
 * 工作日差异和偏移量计算工具类
 *
 * <p>Provides methods to count business days between two dates and to add/subtract
 * business days from a date, with support for custom holidays and weekend definitions.</p>
 * <p>提供计算两个日期间工作日数量以及从日期加减工作日的方法，
 * 支持自定义假期和周末定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Count business days between two dates - 计算两个日期间的工作日数</li>
 *   <li>Add/subtract business days from a date - 从日期加减工作日</li>
 *   <li>Custom holiday sets - 自定义假期集合</li>
 *   <li>Custom weekend day definitions - 自定义周末日定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Count business days (Mon-Fri, no holidays)
 * long days = BusinessDays.between(
 *     LocalDate.of(2026, 4, 6),   // Monday
 *     LocalDate.of(2026, 4, 13)); // next Monday
 * // days = 5
 *
 * // Add 5 business days
 * LocalDate result = BusinessDays.addBusinessDays(
 *     LocalDate.of(2026, 4, 6), 5);
 * // result = 2026-04-13 (skips weekend)
 *
 * // With holidays
 * Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 4, 7));
 * long days2 = BusinessDays.between(
 *     LocalDate.of(2026, 4, 6),
 *     LocalDate.of(2026, 4, 13),
 *     holidays);
 * // days2 = 4 (Tuesday is a holiday)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (throws NullPointerException for null args) - 空值安全: 否（null参数抛异常）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.3
 */
public final class BusinessDays {

    private static final Set<DayOfWeek> DEFAULT_WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private BusinessDays() {
    }

    // ==================== Between Methods | 差值方法 ====================

    /**
     * Counts business days between two dates, excluding weekends (Saturday and Sunday)
     * 计算两个日期之间的工作日数，排除周末（周六和周日）
     *
     * @param startInclusive the start date, inclusive | 起始日期（包含）
     * @param endExclusive   the end date, exclusive | 结束日期（不包含）
     * @return the number of business days | 工作日数量
     */
    public static long between(LocalDate startInclusive, LocalDate endExclusive) {
        return between(startInclusive, endExclusive, Set.of(), DEFAULT_WEEKEND);
    }

    /**
     * Counts business days between two dates, excluding weekends and specified holidays
     * 计算两个日期之间的工作日数，排除周末和指定假期
     *
     * @param startInclusive the start date, inclusive | 起始日期（包含）
     * @param endExclusive   the end date, exclusive | 结束日期（不包含）
     * @param holidays       the set of holiday dates to exclude | 要排除的假期日期集合
     * @return the number of business days | 工作日数量
     */
    public static long between(LocalDate startInclusive, LocalDate endExclusive, Set<LocalDate> holidays) {
        return between(startInclusive, endExclusive, holidays, DEFAULT_WEEKEND);
    }

    /**
     * Counts business days between two dates with custom weekend and holiday definitions
     * 使用自定义周末和假期定义计算两个日期之间的工作日数
     *
     * @param startInclusive the start date, inclusive | 起始日期（包含）
     * @param endExclusive   the end date, exclusive | 结束日期（不包含）
     * @param holidays       the set of holiday dates to exclude | 要排除的假期日期集合
     * @param weekendDays    the set of days considered as weekend | 被视为周末的星期几集合
     * @return the number of business days (non-negative) | 工作日数量（非负数）
     */
    public static long between(LocalDate startInclusive, LocalDate endExclusive,
                               Set<LocalDate> holidays, Set<DayOfWeek> weekendDays) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");
        Objects.requireNonNull(holidays, "holidays must not be null");
        Objects.requireNonNull(weekendDays, "weekendDays must not be null");

        if (!startInclusive.isBefore(endExclusive)) {
            return 0;
        }

        return startInclusive.datesUntil(endExclusive)
                .filter(d -> !weekendDays.contains(d.getDayOfWeek()))
                .filter(d -> !holidays.contains(d))
                .count();
    }

    // ==================== Add Business Days | 加工作日 ====================

    /**
     * Adds the specified number of business days to a date, skipping weekends
     * 向日期添加指定数量的工作日，跳过周末
     *
     * @param date the starting date | 起始日期
     * @param days the number of business days to add (negative for subtraction) | 要添加的工作日数（负数为减少）
     * @return the resulting date | 结果日期
     */
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        return addBusinessDays(date, days, Set.of(), DEFAULT_WEEKEND);
    }

    /**
     * Adds business days with custom holidays
     * 使用自定义假期添加工作日
     *
     * @param date     the starting date | 起始日期
     * @param days     the number of business days to add (negative for subtraction) | 要添加的工作日数
     * @param holidays the set of holiday dates to skip | 要跳过的假期日期集合
     * @return the resulting date | 结果日期
     */
    public static LocalDate addBusinessDays(LocalDate date, int days, Set<LocalDate> holidays) {
        return addBusinessDays(date, days, holidays, DEFAULT_WEEKEND);
    }

    /**
     * Adds business days with custom holidays and weekend definitions
     * 使用自定义假期和周末定义添加工作日
     *
     * @param date        the starting date | 起始日期
     * @param days        the number of business days to add (negative for subtraction) | 要添加的工作日数
     * @param holidays    the set of holiday dates to skip | 要跳过的假期日期集合
     * @param weekendDays the set of days considered as weekend | 被视为周末的星期几集合
     * @return the resulting date | 结果日期
     */
    public static LocalDate addBusinessDays(LocalDate date, int days,
                                            Set<LocalDate> holidays, Set<DayOfWeek> weekendDays) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(holidays, "holidays must not be null");
        Objects.requireNonNull(weekendDays, "weekendDays must not be null");

        if (days == 0) {
            return date;
        }

        if (weekendDays.size() >= 7) {
            throw new IllegalArgumentException("weekendDays cannot contain all 7 days of the week");
        }

        int direction = days > 0 ? 1 : -1;
        int remaining = Math.abs(days);
        LocalDate current = date;

        while (remaining > 0) {
            current = current.plusDays(direction);
            if (!weekendDays.contains(current.getDayOfWeek()) && !holidays.contains(current)) {
                remaining--;
            }
        }
        return current;
    }
}
