package cloud.opencode.base.date.adjuster;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Advanced business day adjuster with holiday and special workday support
 * 高级工作日调整器，支持假日和特殊工作日
 *
 * <p>This class extends WorkdayAdjuster functionality with support for
 * special workdays (e.g., Saturday makeup days in China) and complex
 * holiday calendars.</p>
 * <p>此类扩展了WorkdayAdjuster的功能，支持特殊工作日
 * （如中国的周六调休日）和复杂的假日日历。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard workday calculations - 标准工作日计算</li>
 *   <li>Holiday support - 假日支持</li>
 *   <li>Special workday support (makeup days) - 特殊工作日支持（调休日）</li>
 *   <li>Configurable business week - 可配置的工作周</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Chinese calendar with makeup workdays
 * Set<LocalDate> holidays = Set.of(
 *     LocalDate.of(2024, 10, 1),  // National Day
 *     LocalDate.of(2024, 10, 2),
 *     LocalDate.of(2024, 10, 3)
 * );
 * Set<LocalDate> makeupDays = Set.of(
 *     LocalDate.of(2024, 9, 29)   // Saturday makeup day
 * );
 *
 * BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
 *     .holidays(holidays)
 *     .specialWorkdays(makeupDays)
 *     .build();
 *
 * // The makeup day (Saturday) will be counted as a workday
 * boolean isWorkday = adjuster.isBusinessDay(LocalDate.of(2024, 9, 29));
 * // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see WorkdayAdjuster
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class BusinessDayAdjuster implements TemporalAdjuster {

    /**
     * Default weekend days (Saturday and Sunday)
     */
    private static final Set<DayOfWeek> DEFAULT_WEEKEND =
            EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    /**
     * The number of business days to adjust
     */
    private final int days;

    /**
     * The weekend days
     */
    private final Set<DayOfWeek> weekendDays;

    /**
     * Predicate to check if a date is a holiday
     */
    private final Predicate<LocalDate> holidayPredicate;

    /**
     * Predicate to check if a date is a special workday
     */
    private final Predicate<LocalDate> specialWorkdayPredicate;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private BusinessDayAdjuster(int days, Set<DayOfWeek> weekendDays,
                                Predicate<LocalDate> holidayPredicate,
                                Predicate<LocalDate> specialWorkdayPredicate) {
        this.days = days;
        this.weekendDays = weekendDays != null ? EnumSet.copyOf(weekendDays) : DEFAULT_WEEKEND;
        this.holidayPredicate = holidayPredicate != null ? holidayPredicate : date -> false;
        this.specialWorkdayPredicate = specialWorkdayPredicate != null ? specialWorkdayPredicate : date -> false;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an adjuster that adds the specified number of business days
     * 创建添加指定工作日数的调整器
     *
     * @param days the number of business days to add | 要添加的工作日数
     * @return the adjuster | 调整器
     */
    public static BusinessDayAdjuster plusDays(int days) {
        return new BusinessDayAdjuster(days, DEFAULT_WEEKEND, null, null);
    }

    /**
     * Creates an adjuster that subtracts the specified number of business days
     * 创建减去指定工作日数的调整器
     *
     * @param days the number of business days to subtract | 要减去的工作日数
     * @return the adjuster | 调整器
     */
    public static BusinessDayAdjuster minusDays(int days) {
        return new BusinessDayAdjuster(-days, DEFAULT_WEEKEND, null, null);
    }

    /**
     * Creates an adjuster that finds the next business day
     * 创建查找下一个工作日的调整器
     *
     * @return the adjuster | 调整器
     */
    public static BusinessDayAdjuster nextBusinessDay() {
        return new BusinessDayAdjuster(1, DEFAULT_WEEKEND, null, null);
    }

    /**
     * Creates an adjuster that finds the previous business day
     * 创建查找上一个工作日的调整器
     *
     * @return the adjuster | 调整器
     */
    public static BusinessDayAdjuster previousBusinessDay() {
        return new BusinessDayAdjuster(-1, DEFAULT_WEEKEND, null, null);
    }

    /**
     * Creates a builder for customized business day adjusters
     * 创建自定义工作日调整器的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== TemporalAdjuster Implementation | TemporalAdjuster实现 ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        LocalDate date = LocalDate.from(temporal);

        if (days == 0) {
            return temporal;
        }

        int remaining = Math.abs(days);
        int direction = days > 0 ? 1 : -1;

        while (remaining > 0) {
            date = date.plusDays(direction);
            if (isBusinessDay(date)) {
                remaining--;
            }
        }

        // Preserve the time component if present
        if (temporal instanceof LocalDate) {
            return date;
        }
        return temporal.with(date);
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Checks if a date is a business day
     * 检查日期是否为工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a business day | 如果是工作日返回true
     */
    public boolean isBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        // Special workdays override weekend
        if (specialWorkdayPredicate.test(date)) {
            return !holidayPredicate.test(date);
        }

        // Check if it's a weekend
        if (weekendDays.contains(date.getDayOfWeek())) {
            return false;
        }

        // Check if it's a holiday
        return !holidayPredicate.test(date);
    }

    /**
     * Checks if a date is a holiday
     * 检查日期是否为假日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a holiday | 如果是假日返回true
     */
    public boolean isHoliday(LocalDate date) {
        return holidayPredicate.test(date);
    }

    /**
     * Checks if a date is a special workday
     * 检查日期是否为特殊工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a special workday | 如果是特殊工作日返回true
     */
    public boolean isSpecialWorkday(LocalDate date) {
        return specialWorkdayPredicate.test(date);
    }

    /**
     * Counts business days between two dates (exclusive of end date)
     * 计算两个日期之间的工作日（不包括结束日期）
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of business days | 工作日数
     */
    public long countBusinessDays(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        if (start.isAfter(end)) {
            return -countBusinessDays(end, start);
        }

        long count = 0;
        LocalDate current = start;
        while (current.isBefore(end)) {
            if (isBusinessDay(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    /**
     * Finds the next business day from a given date
     * 从给定日期查找下一个工作日
     *
     * @param date the starting date | 开始日期
     * @return the next business day | 下一个工作日
     */
    public LocalDate nextFrom(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Finds the previous business day from a given date
     * 从给定日期查找上一个工作日
     *
     * @param date the starting date | 开始日期
     * @return the previous business day | 上一个工作日
     */
    public LocalDate previousFrom(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        LocalDate prev = date.minusDays(1);
        while (!isBusinessDay(prev)) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    /**
     * Finds the nth business day of the month
     * 查找月份的第N个工作日
     *
     * @param year  the year | 年份
     * @param month the month | 月份
     * @param n     the ordinal (1-based) | 序数（从1开始）
     * @return the nth business day | 第N个工作日
     * @throws IllegalArgumentException if n is out of range | 如果n超出范围则抛出异常
     */
    public LocalDate nthBusinessDayOfMonth(int year, int month, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1");
        }

        LocalDate date = LocalDate.of(year, month, 1);
        int count = 0;

        while (date.getMonthValue() == month) {
            if (isBusinessDay(date)) {
                count++;
                if (count == n) {
                    return date;
                }
            }
            date = date.plusDays(1);
        }

        throw new IllegalArgumentException("Month " + month + "/" + year +
                " has fewer than " + n + " business days");
    }

    // ==================== Builder Class | 构建器类 ====================

    /**
     * Builder for BusinessDayAdjuster
     * BusinessDayAdjuster构建器
     */
    public static class Builder {
        private int days = 0;
        private Set<DayOfWeek> weekendDays = DEFAULT_WEEKEND;
        private Predicate<LocalDate> holidayPredicate = date -> false;
        private Predicate<LocalDate> specialWorkdayPredicate = date -> false;

        private Builder() {
        }

        /**
         * Sets the number of days to adjust
         * 设置要调整的天数
         *
         * @param days the days | 天数
         * @return this builder | 此构建器
         */
        public Builder days(int days) {
            this.days = days;
            return this;
        }

        /**
         * Sets the weekend days
         * 设置周末天数
         *
         * @param weekendDays the weekend days | 周末天数
         * @return this builder | 此构建器
         */
        public Builder weekendDays(Set<DayOfWeek> weekendDays) {
            this.weekendDays = weekendDays != null ? EnumSet.copyOf(weekendDays) : DEFAULT_WEEKEND;
            return this;
        }

        /**
         * Sets the weekend days as Friday and Saturday (Middle East)
         * 设置周末为周五和周六（中东）
         *
         * @return this builder | 此构建器
         */
        public Builder middleEastWeekend() {
            this.weekendDays = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            return this;
        }

        /**
         * Sets the holiday predicate
         * 设置假日谓词
         *
         * @param holidayPredicate the holiday predicate | 假日谓词
         * @return this builder | 此构建器
         */
        public Builder holidays(Predicate<LocalDate> holidayPredicate) {
            this.holidayPredicate = holidayPredicate != null ? holidayPredicate : date -> false;
            return this;
        }

        /**
         * Sets the holidays from a set
         * 从集合设置假日
         *
         * @param holidays the holidays | 假日集合
         * @return this builder | 此构建器
         */
        public Builder holidays(Set<LocalDate> holidays) {
            this.holidayPredicate = holidays != null ? holidays::contains : date -> false;
            return this;
        }

        /**
         * Sets the special workday predicate
         * 设置特殊工作日谓词
         *
         * @param specialWorkdayPredicate the special workday predicate | 特殊工作日谓词
         * @return this builder | 此构建器
         */
        public Builder specialWorkdays(Predicate<LocalDate> specialWorkdayPredicate) {
            this.specialWorkdayPredicate = specialWorkdayPredicate != null ?
                    specialWorkdayPredicate : date -> false;
            return this;
        }

        /**
         * Sets the special workdays from a set
         * 从集合设置特殊工作日
         *
         * @param specialWorkdays the special workdays | 特殊工作日集合
         * @return this builder | 此构建器
         */
        public Builder specialWorkdays(Set<LocalDate> specialWorkdays) {
            this.specialWorkdayPredicate = specialWorkdays != null ?
                    specialWorkdays::contains : date -> false;
            return this;
        }

        /**
         * Builds the BusinessDayAdjuster
         * 构建BusinessDayAdjuster
         *
         * @return the BusinessDayAdjuster | BusinessDayAdjuster
         */
        public BusinessDayAdjuster build() {
            return new BusinessDayAdjuster(days, weekendDays, holidayPredicate, specialWorkdayPredicate);
        }
    }
}
