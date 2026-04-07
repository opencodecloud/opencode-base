package cloud.opencode.base.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;

/**
 * Static Predicate Methods for Date/Time Objects
 * 日期时间对象的静态谓词方法
 *
 * <p>This utility class provides a comprehensive set of predicate methods for testing
 * properties of date and time objects. All methods are null-safe and return {@code false}
 * when any argument is {@code null}.</p>
 * <p>此工具类提供一套完整的谓词方法，用于测试日期时间对象的属性。
 * 所有方法均为空值安全，当任何参数为 {@code null} 时返回 {@code false}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Future/Past checks - 未来/过去检查</li>
 *   <li>Same day/month/year/week comparisons - 同日/同月/同年/同周比较</li>
 *   <li>Day-of-month position checks (first/last) - 月中日位置检查（首日/末日）</li>
 *   <li>Day-of-week checks (Monday through Sunday, weekend/weekday) - 星期几检查</li>
 *   <li>Leap year detection - 闰年检测</li>
 *   <li>Range inclusion (between) checks - 范围包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate today = LocalDate.now();
 * LocalDate christmas = LocalDate.of(2026, 12, 25);
 *
 * boolean future = DatePredicates.isFuture(christmas);
 * boolean weekend = DatePredicates.isWeekend(today);
 * boolean sameMonth = DatePredicates.isSameMonth(today, christmas);
 * boolean between = DatePredicates.isBetween(today,
 *     LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.3
 */
public final class DatePredicates {

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private DatePredicates() {
        throw new AssertionError("No DatePredicates instances for you!");
    }

    // ==================== Future / Past | 未来 / 过去 ====================

    /**
     * Checks if the given date is strictly after today.
     * 检查给定日期是否严格在今天之后。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is in the future | 如果日期在未来则返回 {@code true}
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Checks if the given date-time is strictly after the current date-time.
     * 检查给定日期时间是否严格在当前日期时间之后。
     *
     * @param dateTime the date-time to check | 要检查的日期时间
     * @return {@code true} if the date-time is in the future | 如果日期时间在未来则返回 {@code true}
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the given date is strictly before today.
     * 检查给定日期是否严格在今天之前。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is in the past | 如果日期在过去则返回 {@code true}
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the given date-time is strictly before the current date-time.
     * 检查给定日期时间是否严格在当前日期时间之前。
     *
     * @param dateTime the date-time to check | 要检查的日期时间
     * @return {@code true} if the date-time is in the past | 如果日期时间在过去则返回 {@code true}
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    // ==================== Same Comparisons | 相同比较 ====================

    /**
     * Checks if two dates represent the same day.
     * 检查两个日期是否表示同一天。
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return {@code true} if both dates are the same day | 如果两个日期是同一天则返回 {@code true}
     */
    public static boolean isSameDay(LocalDate a, LocalDate b) {
        return a != null && b != null && a.isEqual(b);
    }

    /**
     * Checks if two date-times represent the same day (ignoring time).
     * 检查两个日期时间是否表示同一天（忽略时间部分）。
     *
     * @param a the first date-time | 第一个日期时间
     * @param b the second date-time | 第二个日期时间
     * @return {@code true} if both date-times fall on the same day | 如果两个日期时间在同一天则返回 {@code true}
     */
    public static boolean isSameDay(LocalDateTime a, LocalDateTime b) {
        return a != null && b != null && a.toLocalDate().isEqual(b.toLocalDate());
    }

    /**
     * Checks if two dates fall in the same year and month.
     * 检查两个日期是否在同一年同一月。
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return {@code true} if both dates share the same year and month | 如果两个日期年月相同则返回 {@code true}
     */
    public static boolean isSameMonth(LocalDate a, LocalDate b) {
        return a != null && b != null
                && a.getYear() == b.getYear()
                && a.getMonth() == b.getMonth();
    }

    /**
     * Checks if two dates fall in the same year.
     * 检查两个日期是否在同一年。
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return {@code true} if both dates share the same year | 如果两个日期年份相同则返回 {@code true}
     */
    public static boolean isSameYear(LocalDate a, LocalDate b) {
        return a != null && b != null && a.getYear() == b.getYear();
    }

    /**
     * Checks if two dates fall in the same ISO week (same week-based year and week number).
     * 检查两个日期是否在同一 ISO 周（相同的基于周的年份和周数）。
     *
     * @param a the first date | 第一个日期
     * @param b the second date | 第二个日期
     * @return {@code true} if both dates are in the same ISO week | 如果两个日期在同一 ISO 周则返回 {@code true}
     */
    public static boolean isSameWeek(LocalDate a, LocalDate b) {
        if (a == null || b == null) {
            return false;
        }
        return a.get(IsoFields.WEEK_BASED_YEAR) == b.get(IsoFields.WEEK_BASED_YEAR)
                && a.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == b.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    // ==================== Day Position | 日位置 ====================

    /**
     * Checks if the given date is the first day of its month.
     * 检查给定日期是否为所在月份的第一天。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is the first day of the month | 如果日期是月份第一天则返回 {@code true}
     */
    public static boolean isFirstDayOfMonth(LocalDate date) {
        return date != null && date.getDayOfMonth() == 1;
    }

    /**
     * Checks if the given date is the last day of its month.
     * 检查给定日期是否为所在月份的最后一天。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is the last day of the month | 如果日期是月份最后一天则返回 {@code true}
     */
    public static boolean isLastDayOfMonth(LocalDate date) {
        return date != null && date.getDayOfMonth() == date.lengthOfMonth();
    }

    // ==================== Day of Week | 星期几 ====================

    /**
     * Checks if the given date is a Monday.
     * 检查给定日期是否为周一。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Monday | 如果日期为周一则返回 {@code true}
     */
    public static boolean isMonday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.MONDAY;
    }

    /**
     * Checks if the given date is a Tuesday.
     * 检查给定日期是否为周二。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Tuesday | 如果日期为周二则返回 {@code true}
     */
    public static boolean isTuesday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.TUESDAY;
    }

    /**
     * Checks if the given date is a Wednesday.
     * 检查给定日期是否为周三。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Wednesday | 如果日期为周三则返回 {@code true}
     */
    public static boolean isWednesday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.WEDNESDAY;
    }

    /**
     * Checks if the given date is a Thursday.
     * 检查给定日期是否为周四。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Thursday | 如果日期为周四则返回 {@code true}
     */
    public static boolean isThursday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.THURSDAY;
    }

    /**
     * Checks if the given date is a Friday.
     * 检查给定日期是否为周五。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Friday | 如果日期为周五则返回 {@code true}
     */
    public static boolean isFriday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    /**
     * Checks if the given date is a Saturday.
     * 检查给定日期是否为周六。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Saturday | 如果日期为周六则返回 {@code true}
     */
    public static boolean isSaturday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    /**
     * Checks if the given date is a Sunday.
     * 检查给定日期是否为周日。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Sunday | 如果日期为周日则返回 {@code true}
     */
    public static boolean isSunday(LocalDate date) {
        return date != null && date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if the given date falls on a weekend (Saturday or Sunday).
     * 检查给定日期是否为周末（周六或周日）。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is Saturday or Sunday | 如果日期为周六或周日则返回 {@code true}
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if the given date falls on a weekday (Monday through Friday).
     * 检查给定日期是否为工作日（周一至周五）。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the date is a weekday | 如果日期为工作日则返回 {@code true}
     */
    public static boolean isWeekday(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    // ==================== Leap Year | 闰年 ====================

    /**
     * Checks if the given date falls in a leap year.
     * 检查给定日期所在年份是否为闰年。
     *
     * @param date the date to check | 要检查的日期
     * @return {@code true} if the year is a leap year | 如果年份为闰年则返回 {@code true}
     */
    public static boolean isLeapYear(LocalDate date) {
        return date != null && date.isLeapYear();
    }

    // ==================== Between | 范围 ====================

    /**
     * Checks if the given date is between start and end (inclusive on both ends).
     * 检查给定日期是否在起始和结束之间（两端都包含）。
     *
     * @param date  the date to check | 要检查的日期
     * @param start the start of the range (inclusive) | 范围起始（包含）
     * @param end   the end of the range (inclusive) | 范围结束（包含）
     * @return {@code true} if the date is within the range | 如果日期在范围内则返回 {@code true}
     */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Checks if the given date-time is between start and end (inclusive on both ends).
     * 检查给定日期时间是否在起始和结束之间（两端都包含）。
     *
     * @param dateTime the date-time to check | 要检查的日期时间
     * @param start    the start of the range (inclusive) | 范围起始（包含）
     * @param end      the end of the range (inclusive) | 范围结束（包含）
     * @return {@code true} if the date-time is within the range | 如果日期时间在范围内则返回 {@code true}
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }
}
