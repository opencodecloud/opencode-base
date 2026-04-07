package cloud.opencode.base.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility for finding the closest date in a collection
 * 在集合中查找最接近日期的工具类
 *
 * <p>This class provides static methods to find the closest {@link LocalDate} or
 * {@link LocalDateTime} to a given target from a collection of candidates. It supports
 * finding the overall closest, the closest before, or the closest after the target.</p>
 * <p>此类提供静态方法，从候选集合中查找与给定目标最接近的 {@link LocalDate} 或
 * {@link LocalDateTime}。支持查找总体最接近、目标之前最接近或目标之后最接近的日期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Find closest date to a target - 查找最接近目标的日期</li>
 *   <li>Find closest date strictly before target - 查找严格在目标之前最接近的日期</li>
 *   <li>Find closest date strictly after target - 查找严格在目标之后最接近的日期</li>
 *   <li>Support for both LocalDate and LocalDateTime - 同时支持 LocalDate 和 LocalDateTime</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<LocalDate> dates = List.of(
 *     LocalDate.of(2024, 1, 1),
 *     LocalDate.of(2024, 6, 15),
 *     LocalDate.of(2024, 12, 31)
 * );
 * LocalDate target = LocalDate.of(2024, 6, 10);
 *
 * Optional<LocalDate> closest = ClosestDate.closestTo(target, dates);
 * // returns 2024-06-15
 *
 * Optional<LocalDate> before = ClosestDate.closestBefore(target, dates);
 * // returns 2024-01-01
 *
 * Optional<LocalDate> after = ClosestDate.closestAfter(target, dates);
 * // returns 2024-06-15
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Yes (null target throws NullPointerException) - 空值安全: 是（null 目标抛出 NullPointerException）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.3
 */
public final class ClosestDate {

    private ClosestDate() {
        // utility class
    }

    // ==================== LocalDate Methods | LocalDate 方法 ====================

    /**
     * Finds the closest date to the target from the given collection.
     * 从给定集合中查找最接近目标的日期。
     *
     * <p>If two dates are equally close, the earlier one is returned.</p>
     * <p>如果两个日期距离相等，则返回较早的那个。</p>
     *
     * @param target the target date to compare against | 要比较的目标日期
     * @param dates  the collection of candidate dates | 候选日期集合
     * @return the closest date, or empty if collection is null or empty | 最接近的日期，如果集合为 null 或空则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDate> closestTo(LocalDate target, Collection<LocalDate> dates) {
        Objects.requireNonNull(target, "target must not be null");
        if (dates == null || dates.isEmpty()) {
            return Optional.empty();
        }
        LocalDate closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDate date : dates) {
            if (date == null) {
                continue;
            }
            long distance = Math.abs(ChronoUnit.DAYS.between(target, date));
            if (distance < minDistance) {
                minDistance = distance;
                closest = date;
            }
        }
        return Optional.ofNullable(closest);
    }

    /**
     * Finds the closest date strictly before the target.
     * 查找严格在目标之前最接近的日期。
     *
     * @param target the target date | 目标日期
     * @param dates  the collection of candidate dates | 候选日期集合
     * @return the closest date before target, or empty if none found | 目标之前最接近的日期，如果未找到则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDate> closestBefore(LocalDate target, Collection<LocalDate> dates) {
        Objects.requireNonNull(target, "target must not be null");
        if (dates == null || dates.isEmpty()) {
            return Optional.empty();
        }
        LocalDate closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDate date : dates) {
            if (date == null || !date.isBefore(target)) {
                continue;
            }
            long distance = ChronoUnit.DAYS.between(date, target);
            if (distance < minDistance) {
                minDistance = distance;
                closest = date;
            }
        }
        return Optional.ofNullable(closest);
    }

    /**
     * Finds the closest date strictly after the target.
     * 查找严格在目标之后最接近的日期。
     *
     * @param target the target date | 目标日期
     * @param dates  the collection of candidate dates | 候选日期集合
     * @return the closest date after target, or empty if none found | 目标之后最接近的日期，如果未找到则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDate> closestAfter(LocalDate target, Collection<LocalDate> dates) {
        Objects.requireNonNull(target, "target must not be null");
        if (dates == null || dates.isEmpty()) {
            return Optional.empty();
        }
        LocalDate closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDate date : dates) {
            if (date == null || !date.isAfter(target)) {
                continue;
            }
            long distance = ChronoUnit.DAYS.between(target, date);
            if (distance < minDistance) {
                minDistance = distance;
                closest = date;
            }
        }
        return Optional.ofNullable(closest);
    }

    // ==================== LocalDateTime Methods | LocalDateTime 方法 ====================

    /**
     * Finds the closest date-time to the target from the given collection.
     * 从给定集合中查找最接近目标的日期时间。
     *
     * <p>Comparison is based on nanosecond precision. If two date-times are equally close,
     * the earlier one is returned.</p>
     * <p>比较基于纳秒精度。如果两个日期时间距离相等，则返回较早的那个。</p>
     *
     * @param target    the target date-time to compare against | 要比较的目标日期时间
     * @param dateTimes the collection of candidate date-times | 候选日期时间集合
     * @return the closest date-time, or empty if collection is null or empty | 最接近的日期时间，如果集合为 null 或空则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDateTime> closestTo(LocalDateTime target, Collection<LocalDateTime> dateTimes) {
        Objects.requireNonNull(target, "target must not be null");
        if (dateTimes == null || dateTimes.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDateTime dateTime : dateTimes) {
            if (dateTime == null) {
                continue;
            }
            long distance = Math.abs(ChronoUnit.NANOS.between(target, dateTime));
            if (distance < minDistance) {
                minDistance = distance;
                closest = dateTime;
            }
        }
        return Optional.ofNullable(closest);
    }

    /**
     * Finds the closest date-time strictly before the target.
     * 查找严格在目标之前最接近的日期时间。
     *
     * @param target    the target date-time | 目标日期时间
     * @param dateTimes the collection of candidate date-times | 候选日期时间集合
     * @return the closest date-time before target, or empty if none found | 目标之前最接近的日期时间，如果未找到则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDateTime> closestBefore(LocalDateTime target, Collection<LocalDateTime> dateTimes) {
        Objects.requireNonNull(target, "target must not be null");
        if (dateTimes == null || dateTimes.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDateTime dateTime : dateTimes) {
            if (dateTime == null || !dateTime.isBefore(target)) {
                continue;
            }
            long distance = ChronoUnit.NANOS.between(dateTime, target);
            if (distance < minDistance) {
                minDistance = distance;
                closest = dateTime;
            }
        }
        return Optional.ofNullable(closest);
    }

    /**
     * Finds the closest date-time strictly after the target.
     * 查找严格在目标之后最接近的日期时间。
     *
     * @param target    the target date-time | 目标日期时间
     * @param dateTimes the collection of candidate date-times | 候选日期时间集合
     * @return the closest date-time after target, or empty if none found | 目标之后最接近的日期时间，如果未找到则返回空
     * @throws NullPointerException if target is null | 如果目标为 null 则抛出空指针异常
     */
    public static Optional<LocalDateTime> closestAfter(LocalDateTime target, Collection<LocalDateTime> dateTimes) {
        Objects.requireNonNull(target, "target must not be null");
        if (dateTimes == null || dateTimes.isEmpty()) {
            return Optional.empty();
        }
        LocalDateTime closest = null;
        long minDistance = Long.MAX_VALUE;
        for (LocalDateTime dateTime : dateTimes) {
            if (dateTime == null || !dateTime.isAfter(target)) {
                continue;
            }
            long distance = ChronoUnit.NANOS.between(target, dateTime);
            if (distance < minDistance) {
                minDistance = distance;
                closest = dateTime;
            }
        }
        return Optional.ofNullable(closest);
    }
}
