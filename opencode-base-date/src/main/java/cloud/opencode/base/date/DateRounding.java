package cloud.opencode.base.date;

import cloud.opencode.base.date.exception.OpenDateException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility for rounding date/time values to various intervals
 * 将日期时间值舍入到各种间隔的工具类
 *
 * <p>This class provides static methods to round, floor, and ceil {@link LocalDateTime} values
 * to specified time intervals such as hours, minutes, half hours, quarter hours, and arbitrary
 * {@link Duration} values.</p>
 * <p>此类提供静态方法，将 {@link LocalDateTime} 值舍入、向下取整和向上取整到指定的时间间隔，
 * 如小时、分钟、半小时、刻钟以及任意 {@link Duration} 值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Round to nearest hour/minute - 舍入到最接近的小时/分钟</li>
 *   <li>Floor and ceil operations - 向下取整和向上取整操作</li>
 *   <li>Round to arbitrary N-minute intervals - 舍入到任意 N 分钟间隔</li>
 *   <li>Round to arbitrary Duration intervals - 舍入到任意 Duration 间隔</li>
 *   <li>Half-hour and quarter-hour rounding - 半小时和刻钟舍入</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 10, 37, 45);
 *
 * DateRounding.roundToNearestHour(dt);        // 2024-06-15T11:00
 * DateRounding.floorToHour(dt);               // 2024-06-15T10:00
 * DateRounding.ceilToHour(dt);                // 2024-06-15T11:00
 * DateRounding.roundToNearest(dt, 15);        // 2024-06-15T10:45
 * DateRounding.roundToNearestHalfHour(dt);    // 2024-06-15T10:30
 *
 * Duration fiveMin = Duration.ofMinutes(5);
 * DateRounding.roundToNearest(dt, fiveMin);   // 2024-06-15T10:40
 * }</pre>
 *
 * <p><strong>Note | 注意:</strong> Generic {@link Duration} rounding uses UTC epoch seconds as the
 * reference point. This means results are consistent regardless of timezone but may not align with
 * local midnight for durations that don't evenly divide 24 hours.</p>
 * <p>通用 {@link Duration} 舍入使用 UTC 纪元秒作为参考点。这意味着结果在各时区间一致，
 * 但对于不能整除 24 小时的间隔，结果可能不与本地午夜对齐。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Yes (null dateTime throws NullPointerException) - 空值安全: 是（null dateTime 抛出 NullPointerException）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.3
 */
public final class DateRounding {

    private DateRounding() {
        // utility class
    }

    // ==================== Hour Rounding | 小时舍入 ====================

    /**
     * Rounds to the nearest hour. Minutes >= 30 round up, otherwise round down.
     * 舍入到最接近的小时。分钟 >= 30 向上舍入，否则向下舍入。
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime roundToNearestHour(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        if (dateTime.getMinute() >= 30) {
            return ceilToHour(dateTime);
        }
        return floorToHour(dateTime);
    }

    /**
     * Ceils to the next hour boundary. If already at an exact hour, returns unchanged.
     * 向上取整到下一个小时边界。如果已经在整点，则原样返回。
     *
     * @param dateTime the date-time to ceil | 要向上取整的日期时间
     * @return the ceiled date-time | 向上取整后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime ceilToHour(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        if (dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0) {
            return dateTime;
        }
        return dateTime.truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

    /**
     * Floors to the current hour boundary.
     * 向下取整到当前小时边界。
     *
     * @param dateTime the date-time to floor | 要向下取整的日期时间
     * @return the floored date-time | 向下取整后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime floorToHour(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.truncatedTo(ChronoUnit.HOURS);
    }

    // ==================== Minute Rounding | 分钟舍入 ====================

    /**
     * Rounds to the nearest minute. Seconds >= 30 round up, otherwise round down.
     * 舍入到最接近的分钟。秒 >= 30 向上舍入，否则向下舍入。
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime roundToNearestMinute(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        if (dateTime.getSecond() >= 30) {
            return ceilToMinute(dateTime);
        }
        return floorToMinute(dateTime);
    }

    /**
     * Ceils to the next minute boundary. If already at an exact minute, returns unchanged.
     * 向上取整到下一个分钟边界。如果已经在整分钟，则原样返回。
     *
     * @param dateTime the date-time to ceil | 要向上取整的日期时间
     * @return the ceiled date-time | 向上取整后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime ceilToMinute(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        if (dateTime.getSecond() == 0 && dateTime.getNano() == 0) {
            return dateTime;
        }
        return dateTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
    }

    /**
     * Floors to the current minute boundary.
     * 向下取整到当前分钟边界。
     *
     * @param dateTime the date-time to floor | 要向下取整的日期时间
     * @return the floored date-time | 向下取整后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime floorToMinute(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.truncatedTo(ChronoUnit.MINUTES);
    }

    // ==================== N-Minute Rounding | N分钟舍入 ====================

    /**
     * Rounds to the nearest N-minute interval.
     * 舍入到最接近的 N 分钟间隔。
     *
     * <p>For example, with minutes=15, 10:07 rounds to 10:00 and 10:08 rounds to 10:15.</p>
     * <p>例如，minutes=15 时，10:07 舍入到 10:00，10:08 舍入到 10:15。</p>
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @param minutes  the interval in minutes (must be positive) | 间隔分钟数（必须为正数）
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     * @throws OpenDateException    if minutes is not positive | 如果分钟数不为正数则抛出异常
     */
    public static LocalDateTime roundToNearest(LocalDateTime dateTime, int minutes) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        if (minutes <= 0) {
            throw new OpenDateException("Rounding interval must be positive, got: " + minutes);
        }
        return roundToNearest(dateTime, Duration.ofMinutes(minutes));
    }

    /**
     * Rounds to the nearest half hour (30-minute interval).
     * 舍入到最接近的半小时（30 分钟间隔）。
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime roundToNearestHalfHour(LocalDateTime dateTime) {
        return roundToNearest(dateTime, 30);
    }

    /**
     * Rounds to the nearest quarter hour (15-minute interval).
     * 舍入到最接近的刻钟（15 分钟间隔）。
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime is null | 如果 dateTime 为 null 则抛出空指针异常
     */
    public static LocalDateTime roundToNearestQuarterHour(LocalDateTime dateTime) {
        return roundToNearest(dateTime, 15);
    }

    // ==================== Generic Duration Rounding | 通用 Duration 舍入 ====================

    /**
     * Rounds to the nearest multiple of the given duration.
     * 舍入到给定持续时间的最接近倍数。
     *
     * <p>Uses UTC epoch seconds as the reference point for alignment. For sub-second
     * durations, nanosecond precision is used.</p>
     * <p>使用 UTC 纪元秒作为对齐参考点。对于亚秒级持续时间，使用纳秒精度。</p>
     *
     * @param dateTime the date-time to round | 要舍入的日期时间
     * @param duration the rounding interval (must be positive) | 舍入间隔（必须为正数）
     * @return the rounded date-time | 舍入后的日期时间
     * @throws NullPointerException if dateTime or duration is null | 如果 dateTime 或 duration 为 null 则抛出空指针异常
     * @throws OpenDateException    if duration is zero or negative | 如果 duration 为零或负数则抛出异常
     */
    public static LocalDateTime roundToNearest(LocalDateTime dateTime, Duration duration) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        validatePositiveDuration(duration);

        long durationSeconds = duration.getSeconds();
        int durationNanos = duration.getNano();

        // For durations with only whole seconds (the common case)
        if (durationNanos == 0 && durationSeconds > 0) {
            long epochSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
            long remainder = Math.floorMod(epochSecond, durationSeconds);
            long floorEpoch = epochSecond - remainder;
            // If there's sub-second data in the input, it counts as additional remainder
            boolean hasSubSecond = dateTime.getNano() > 0;
            if (hasSubSecond) {
                // Always have a remainder if there are sub-seconds
                long ceilEpoch = floorEpoch + durationSeconds;
                // Total distance from floor in nanoseconds
                long distFromFloor = Math.addExact(Math.multiplyExact(remainder, 1_000_000_000L), dateTime.getNano());
                long halfDuration = Math.multiplyExact(durationSeconds, 500_000_000L);
                if (distFromFloor >= halfDuration) {
                    return LocalDateTime.ofEpochSecond(ceilEpoch, 0, ZoneOffset.UTC);
                }
                return LocalDateTime.ofEpochSecond(floorEpoch, 0, ZoneOffset.UTC);
            }
            if (remainder == 0) {
                return dateTime;
            }
            long ceilEpoch = floorEpoch + durationSeconds;
            if (remainder * 2 >= durationSeconds) {
                return LocalDateTime.ofEpochSecond(ceilEpoch, 0, ZoneOffset.UTC);
            }
            return LocalDateTime.ofEpochSecond(floorEpoch, 0, ZoneOffset.UTC);
        }

        // For sub-second durations, use nanosecond arithmetic
        long totalDurationNanos;
        try {
            totalDurationNanos = Math.addExact(Math.multiplyExact(durationSeconds, 1_000_000_000L), durationNanos);
        } catch (ArithmeticException e) {
            throw new OpenDateException("Duration too large for nanosecond rounding");
        }
        if (totalDurationNanos <= 0) {
            throw new OpenDateException("Duration must be positive");
        }
        long epochSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
        long totalNanos;
        try {
            totalNanos = Math.addExact(Math.multiplyExact(epochSecond, 1_000_000_000L), dateTime.getNano());
        } catch (ArithmeticException e) {
            throw new OpenDateException("DateTime too far from epoch for nanosecond-precision rounding");
        }
        long remainder = Math.floorMod(totalNanos, totalDurationNanos);
        long floorNanos = totalNanos - remainder;
        if (remainder == 0) {
            return fromTotalNanos(floorNanos);
        }
        long ceilNanos = floorNanos + totalDurationNanos;
        if (remainder * 2 >= totalDurationNanos) {
            return fromTotalNanos(ceilNanos);
        }
        return fromTotalNanos(floorNanos);
    }

    /**
     * Ceils to the next multiple of the given duration.
     * 向上取整到给定持续时间的下一个倍数。
     *
     * <p>If the date-time is already exactly on a boundary, returns unchanged.</p>
     * <p>如果日期时间已经在边界上，则原样返回。</p>
     *
     * @param dateTime the date-time to ceil | 要向上取整的日期时间
     * @param duration the interval (must be positive) | 间隔（必须为正数）
     * @return the ceiled date-time | 向上取整后的日期时间
     * @throws NullPointerException if dateTime or duration is null | 如果 dateTime 或 duration 为 null 则抛出空指针异常
     * @throws OpenDateException    if duration is zero or negative | 如果 duration 为零或负数则抛出异常
     */
    public static LocalDateTime ceilTo(LocalDateTime dateTime, Duration duration) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        validatePositiveDuration(duration);

        long durationSeconds = duration.getSeconds();
        int durationNanos = duration.getNano();

        if (durationNanos == 0 && durationSeconds > 0) {
            long epochSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
            long remainder = Math.floorMod(epochSecond, durationSeconds);
            if (remainder == 0 && dateTime.getNano() == 0) {
                return dateTime;
            }
            long floorEpoch = epochSecond - remainder;
            return LocalDateTime.ofEpochSecond(floorEpoch + durationSeconds, 0, ZoneOffset.UTC);
        }

        long totalDurationNanos = toTotalDurationNanos(durationSeconds, durationNanos);
        long totalNanos = toTotalEpochNanos(dateTime);
        long remainder = Math.floorMod(totalNanos, totalDurationNanos);
        if (remainder == 0) {
            return dateTime;
        }
        return fromTotalNanos(totalNanos - remainder + totalDurationNanos);
    }

    /**
     * Floors to the current multiple of the given duration.
     * 向下取整到给定持续时间的当前倍数。
     *
     * @param dateTime the date-time to floor | 要向下取整的日期时间
     * @param duration the interval (must be positive) | 间隔（必须为正数）
     * @return the floored date-time | 向下取整后的日期时间
     * @throws NullPointerException if dateTime or duration is null | 如果 dateTime 或 duration 为 null 则抛出空指针异常
     * @throws OpenDateException    if duration is zero or negative | 如果 duration 为零或负数则抛出异常
     */
    public static LocalDateTime floorTo(LocalDateTime dateTime, Duration duration) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        validatePositiveDuration(duration);

        long durationSeconds = duration.getSeconds();
        int durationNanos = duration.getNano();

        if (durationNanos == 0 && durationSeconds > 0) {
            long epochSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
            long remainder = Math.floorMod(epochSecond, durationSeconds);
            long floorEpoch = epochSecond - remainder;
            return LocalDateTime.ofEpochSecond(floorEpoch, 0, ZoneOffset.UTC);
        }

        long totalDurationNanos = toTotalDurationNanos(durationSeconds, durationNanos);
        long totalNanos = toTotalEpochNanos(dateTime);
        long remainder = Math.floorMod(totalNanos, totalDurationNanos);
        return fromTotalNanos(totalNanos - remainder);
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Validates that the duration is positive.
     * 验证持续时间为正数。
     */
    private static void validatePositiveDuration(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            throw new OpenDateException("Rounding duration must be positive, got: " + duration);
        }
    }

    /**
     * Converts duration seconds + nanos to total nanoseconds with overflow protection.
     * 将持续时间秒+纳秒转换为总纳秒数，带溢出保护。
     */
    private static long toTotalDurationNanos(long durationSeconds, int durationNanos) {
        try {
            long total = Math.addExact(Math.multiplyExact(durationSeconds, 1_000_000_000L), durationNanos);
            if (total <= 0) {
                throw new OpenDateException("Duration must be positive");
            }
            return total;
        } catch (ArithmeticException e) {
            throw new OpenDateException("Duration too large for nanosecond rounding");
        }
    }

    /**
     * Converts a LocalDateTime to total nanoseconds since epoch (UTC) with overflow protection.
     * 将 LocalDateTime 转换为纪元以来的总纳秒数（UTC），带溢出保护。
     */
    private static long toTotalEpochNanos(LocalDateTime dateTime) {
        try {
            long epochSecond = dateTime.toEpochSecond(ZoneOffset.UTC);
            return Math.addExact(Math.multiplyExact(epochSecond, 1_000_000_000L), dateTime.getNano());
        } catch (ArithmeticException e) {
            throw new OpenDateException("DateTime too far from epoch for nanosecond-precision rounding");
        }
    }

    /**
     * Converts total nanoseconds since epoch to LocalDateTime.
     * 将纪元以来的总纳秒数转换为 LocalDateTime。
     */
    private static LocalDateTime fromTotalNanos(long totalNanos) {
        long seconds = Math.floorDiv(totalNanos, 1_000_000_000L);
        int nanos = (int) Math.floorMod(totalNanos, 1_000_000_000L);
        return LocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC);
    }
}
