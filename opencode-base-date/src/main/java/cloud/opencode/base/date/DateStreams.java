package cloud.opencode.base.date;

import cloud.opencode.base.date.exception.OpenDateException;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility for Creating Streams of Date/Time Objects
 * 日期时间对象流创建工具
 *
 * <p>This utility class provides factory methods for creating {@link Stream} instances
 * that produce sequences of date and time objects. Streams are lazily evaluated, making
 * them efficient for large date ranges.</p>
 * <p>此工具类提供工厂方法来创建产生日期时间对象序列的 {@link Stream} 实例。
 * 流采用惰性求值，因此在处理大日期范围时非常高效。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Day-by-day streams with optional custom step - 逐日流（可自定义步长）</li>
 *   <li>Week-by-week streams (Monday-aligned) - 逐周流（周一对齐）</li>
 *   <li>Month-by-month streams - 逐月流</li>
 *   <li>Hour-by-hour streams - 逐小时流</li>
 *   <li>Generic iterate with any Duration step - 使用任意 Duration 步长的通用迭代</li>
 *   <li>Weekend and weekday filtered streams - 周末和工作日过滤流</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // All days in January 2026
 * DateStreams.days(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1))
 *     .forEach(System.out::println);
 *
 * // Every other day
 * DateStreams.days(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), Period.ofDays(2))
 *     .forEach(System.out::println);
 *
 * // All weekends in 2026
 * DateStreams.weekends(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1))
 *     .count();
 *
 * // Every 30 minutes in a day
 * DateStreams.iterate(
 *         LocalDateTime.of(2026, 1, 1, 0, 0),
 *         LocalDateTime.of(2026, 1, 2, 0, 0),
 *         Duration.ofMinutes(30))
 *     .forEach(System.out::println);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (throws on null) - 空值安全: 是（遇 null 抛异常）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.3
 */
public final class DateStreams {

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private DateStreams() {
        throw new AssertionError("No DateStreams instances for you!");
    }

    // ==================== Day Streams | 日流 ====================

    /**
     * Creates a stream of consecutive days from start (inclusive) to end (exclusive).
     * 创建从起始日（包含）到结束日（不包含）的连续日期流。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @return a stream of {@link LocalDate} | {@link LocalDate} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<LocalDate> days(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return startInclusive.datesUntil(endExclusive);
    }

    /**
     * Creates a stream of dates from start (inclusive) to end (exclusive) with a custom period step.
     * 创建从起始日（包含）到结束日（不包含）的日期流，使用自定义周期步长。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @param step           the period between each date in the stream | 流中每个日期之间的周期
     * @return a stream of {@link LocalDate} | {@link LocalDate} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     * @throws OpenDateException    if the step is zero or negative | 如果步长为零或负值则抛出异常
     */
    public static Stream<LocalDate> days(LocalDate startInclusive, LocalDate endExclusive, Period step) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        Objects.requireNonNull(step, "step must not be null | step 不能为 null");
        validatePositivePeriod(step);
        return Stream.iterate(
                startInclusive,
                date -> date.isBefore(endExclusive),
                date -> date.plus(step)
        );
    }

    // ==================== Week Streams | 周流 ====================

    /**
     * Creates a stream of Monday-aligned week start dates from start (inclusive) to end (exclusive).
     * The first element is the Monday on or after {@code startInclusive}; subsequent elements
     * are each successive Monday.
     * 创建从起始日（包含）到结束日（不包含）的周一对齐的每周起始日期流。
     * 第一个元素是 {@code startInclusive} 当天或之后的周一；后续元素为每个连续的周一。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @return a stream of Monday-aligned {@link LocalDate} | 周一对齐的 {@link LocalDate} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<LocalDate> weeks(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        LocalDate firstMonday = startInclusive.getDayOfWeek() == DayOfWeek.MONDAY
                ? startInclusive
                : startInclusive.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        return Stream.iterate(
                firstMonday,
                date -> date.isBefore(endExclusive),
                date -> date.plusWeeks(1)
        );
    }

    // ==================== Month Streams | 月流 ====================

    /**
     * Creates a stream of consecutive months from start (inclusive) to end (exclusive).
     * 创建从起始月（包含）到结束月（不包含）的连续月份流。
     *
     * @param startInclusive the first month (inclusive) | 起始月份（包含）
     * @param endExclusive   the end month (exclusive) | 结束月份（不包含）
     * @return a stream of {@link YearMonth} | {@link YearMonth} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<YearMonth> months(YearMonth startInclusive, YearMonth endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return Stream.iterate(
                startInclusive,
                ym -> ym.isBefore(endExclusive),
                ym -> ym.plusMonths(1)
        );
    }

    /**
     * Creates a stream of consecutive months derived from a date range.
     * The start and end dates are converted to their respective {@link YearMonth} values.
     * 根据日期范围创建连续月份流。起始和结束日期将转换为各自的 {@link YearMonth} 值。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @return a stream of {@link YearMonth} | {@link YearMonth} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<YearMonth> months(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return months(YearMonth.from(startInclusive), YearMonth.from(endExclusive));
    }

    // ==================== Hour Streams | 小时流 ====================

    /**
     * Creates a stream of consecutive hours from start (inclusive) to end (exclusive).
     * 创建从起始时间（包含）到结束时间（不包含）的连续小时流。
     *
     * @param startInclusive the first date-time (inclusive) | 起始日期时间（包含）
     * @param endExclusive   the end date-time (exclusive) | 结束日期时间（不包含）
     * @return a stream of {@link LocalDateTime} | {@link LocalDateTime} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<LocalDateTime> hours(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return Stream.iterate(
                startInclusive,
                dt -> dt.isBefore(endExclusive),
                dt -> dt.plusHours(1)
        );
    }

    // ==================== Generic Iterate | 通用迭代 ====================

    /**
     * Creates a stream of date-times from start (inclusive) to end (exclusive) with a custom duration step.
     * 创建从起始时间（包含）到结束时间（不包含）的日期时间流，使用自定义持续时间步长。
     *
     * @param startInclusive the first date-time (inclusive) | 起始日期时间（包含）
     * @param endExclusive   the end date-time (exclusive) | 结束日期时间（不包含）
     * @param step           the duration between each element | 每个元素之间的持续时间
     * @return a stream of {@link LocalDateTime} | {@link LocalDateTime} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     * @throws OpenDateException    if the step is zero or negative | 如果步长为零或负值则抛出异常
     */
    public static Stream<LocalDateTime> iterate(LocalDateTime startInclusive, LocalDateTime endExclusive,
                                                 Duration step) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        Objects.requireNonNull(step, "step must not be null | step 不能为 null");
        if (step.isZero() || step.isNegative()) {
            throw new OpenDateException("step must be positive | step 必须为正值");
        }
        return Stream.iterate(
                startInclusive,
                dt -> dt.isBefore(endExclusive),
                dt -> dt.plus(step)
        );
    }

    // ==================== Weekend / Weekday Streams | 周末 / 工作日流 ====================

    /**
     * Creates a stream of weekend days (Saturday and Sunday) from start (inclusive) to end (exclusive).
     * 创建从起始日（包含）到结束日（不包含）的周末日期流（周六和周日）。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @return a stream of weekend {@link LocalDate} | 周末 {@link LocalDate} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<LocalDate> weekends(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return startInclusive.datesUntil(endExclusive)
                .filter(DatePredicates::isWeekend);
    }

    /**
     * Creates a stream of weekdays (Monday through Friday) from start (inclusive) to end (exclusive).
     * 创建从起始日（包含）到结束日（不包含）的工作日日期流（周一至周五）。
     *
     * @param startInclusive the first date (inclusive) | 起始日期（包含）
     * @param endExclusive   the end date (exclusive) | 结束日期（不包含）
     * @return a stream of weekday {@link LocalDate} | 工作日 {@link LocalDate} 流
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出异常
     */
    public static Stream<LocalDate> weekdays(LocalDate startInclusive, LocalDate endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null | startInclusive 不能为 null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null | endExclusive 不能为 null");
        return startInclusive.datesUntil(endExclusive)
                .filter(DatePredicates::isWeekday);
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Validates that a period represents a positive amount of time.
     * 验证周期是否表示正的时间量。
     *
     * @param step the period to validate | 要验证的周期
     * @throws OpenDateException if the period is zero or negative | 如果周期为零或负值则抛出异常
     */
    private static void validatePositivePeriod(Period step) {
        // A period is considered non-positive if it results in no forward movement.
        // We check by applying it to an arbitrary date.
        LocalDate reference = LocalDate.of(2000, 1, 1);
        LocalDate after = reference.plus(step);
        if (!after.isAfter(reference)) {
            throw new OpenDateException("step must be positive | step 必须为正值");
        }
    }
}
