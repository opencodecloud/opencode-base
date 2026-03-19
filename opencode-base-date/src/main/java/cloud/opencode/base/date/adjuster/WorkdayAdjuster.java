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
 * Temporal adjuster for workday calculations
 * 工作日计算的时间调整器
 *
 * <p>This class provides adjusters for calculating workdays, taking into account
 * weekends and optionally holidays.</p>
 * <p>此类提供计算工作日的调整器，考虑周末和可选的假日。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Add/subtract workdays - 添加/减去工作日</li>
 *   <li>Find next/previous workday - 查找下一个/上一个工作日</li>
 *   <li>Configurable weekend days - 可配置的周末天数</li>
 *   <li>Custom holiday support - 自定义假日支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate date = LocalDate.of(2024, 1, 5); // Friday
 *
 * // Add 3 workdays
 * LocalDate after = date.with(WorkdayAdjuster.plusDays(3));
 * // 2024-01-10 (Wednesday, skips weekend)
 *
 * // Next workday
 * LocalDate next = date.with(WorkdayAdjuster.nextWorkday());
 * // 2024-01-08 (Monday)
 *
 * // With custom holidays
 * Set<LocalDate> holidays = Set.of(LocalDate.of(2024, 1, 8));
 * LocalDate after2 = date.with(WorkdayAdjuster.plusDays(1, holidays));
 * // 2024-01-09 (Tuesday, skips weekend and holiday)
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
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class WorkdayAdjuster implements TemporalAdjuster {

    /**
     * Default weekend days (Saturday and Sunday)
     */
    private static final Set<DayOfWeek> DEFAULT_WEEKEND =
            EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    /**
     * The number of workdays to adjust
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

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private WorkdayAdjuster(int days, Set<DayOfWeek> weekendDays, Predicate<LocalDate> holidayPredicate) {
        this.days = days;
        this.weekendDays = weekendDays != null ? EnumSet.copyOf(weekendDays) : DEFAULT_WEEKEND;
        this.holidayPredicate = holidayPredicate != null ? holidayPredicate : date -> false;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an adjuster that adds the specified number of workdays
     * 创建添加指定工作日数的调整器
     *
     * @param days the number of workdays to add | 要添加的工作日数
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster plusDays(int days) {
        return new WorkdayAdjuster(days, DEFAULT_WEEKEND, null);
    }

    /**
     * Creates an adjuster that adds workdays with custom holidays
     * 创建添加工作日并考虑自定义假日的调整器
     *
     * @param days     the number of workdays to add | 要添加的工作日数
     * @param holidays the set of holidays | 假日集合
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster plusDays(int days, Set<LocalDate> holidays) {
        Objects.requireNonNull(holidays, "holidays must not be null");
        return new WorkdayAdjuster(days, DEFAULT_WEEKEND, holidays::contains);
    }

    /**
     * Creates an adjuster that adds workdays with custom holiday predicate
     * 创建添加工作日并使用自定义假日谓词的调整器
     *
     * @param days             the number of workdays to add | 要添加的工作日数
     * @param holidayPredicate predicate to check if a date is a holiday | 检查日期是否为假日的谓词
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster plusDays(int days, Predicate<LocalDate> holidayPredicate) {
        return new WorkdayAdjuster(days, DEFAULT_WEEKEND, holidayPredicate);
    }

    /**
     * Creates an adjuster that subtracts the specified number of workdays
     * 创建减去指定工作日数的调整器
     *
     * @param days the number of workdays to subtract | 要减去的工作日数
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster minusDays(int days) {
        return new WorkdayAdjuster(-days, DEFAULT_WEEKEND, null);
    }

    /**
     * Creates an adjuster that finds the next workday
     * 创建查找下一个工作日的调整器
     *
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster nextWorkday() {
        return new WorkdayAdjuster(1, DEFAULT_WEEKEND, null);
    }

    /**
     * Creates an adjuster that finds the next workday with custom holidays
     * 创建查找下一个工作日并考虑自定义假日的调整器
     *
     * @param holidays the set of holidays | 假日集合
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster nextWorkday(Set<LocalDate> holidays) {
        Objects.requireNonNull(holidays, "holidays must not be null");
        return new WorkdayAdjuster(1, DEFAULT_WEEKEND, holidays::contains);
    }

    /**
     * Creates an adjuster that finds the previous workday
     * 创建查找上一个工作日的调整器
     *
     * @return the adjuster | 调整器
     */
    public static WorkdayAdjuster previousWorkday() {
        return new WorkdayAdjuster(-1, DEFAULT_WEEKEND, null);
    }

    /**
     * Creates an adjuster that finds the nearest workday (or same if already a workday)
     * 创建查找最近工作日的调整器（如果已是工作日则返回原日期）
     *
     * @return the adjuster | 调整器
     */
    public static TemporalAdjuster nearestWorkday() {
        return temporal -> {
            LocalDate date = LocalDate.from(temporal);
            if (isWorkday(date, DEFAULT_WEEKEND, d -> false)) {
                return date;
            }
            // Try forward first
            LocalDate forward = date.plusDays(1);
            while (!isWorkday(forward, DEFAULT_WEEKEND, d -> false)) {
                forward = forward.plusDays(1);
            }
            // Try backward
            LocalDate backward = date.minusDays(1);
            while (!isWorkday(backward, DEFAULT_WEEKEND, d -> false)) {
                backward = backward.minusDays(1);
            }
            // Return the nearest
            long forwardDays = forward.toEpochDay() - date.toEpochDay();
            long backwardDays = date.toEpochDay() - backward.toEpochDay();
            return forwardDays <= backwardDays ? forward : backward;
        };
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a builder for customized workday adjusters
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
            if (isWorkday(date, weekendDays, holidayPredicate)) {
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
     * Checks if a date is a workday
     * 检查日期是否为工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a workday | 如果是工作日返回true
     */
    public boolean isWorkday(LocalDate date) {
        return isWorkday(date, weekendDays, holidayPredicate);
    }

    private static boolean isWorkday(LocalDate date, Set<DayOfWeek> weekendDays,
                                     Predicate<LocalDate> holidayPredicate) {
        if (weekendDays.contains(date.getDayOfWeek())) {
            return false;
        }
        return !holidayPredicate.test(date);
    }

    /**
     * Counts workdays between two dates (exclusive of end date)
     * 计算两个日期之间的工作日（不包括结束日期）
     *
     * @param start the start date | 开始日期
     * @param end   the end date | 结束日期
     * @return the number of workdays | 工作日数
     */
    public long countWorkdays(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return -countWorkdays(end, start);
        }

        long count = 0;
        LocalDate current = start;
        while (current.isBefore(end)) {
            if (isWorkday(current, weekendDays, holidayPredicate)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // ==================== Builder Class | 构建器类 ====================

    /**
     * Builder for WorkdayAdjuster
     * WorkdayAdjuster构建器
     */
    public static class Builder {
        private int days = 0;
        private Set<DayOfWeek> weekendDays = DEFAULT_WEEKEND;
        private Predicate<LocalDate> holidayPredicate = date -> false;

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
         * Builds the WorkdayAdjuster
         * 构建WorkdayAdjuster
         *
         * @return the WorkdayAdjuster | WorkdayAdjuster
         */
        public WorkdayAdjuster build() {
            return new WorkdayAdjuster(days, weekendDays, holidayPredicate);
        }
    }
}
