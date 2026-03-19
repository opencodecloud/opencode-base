package cloud.opencode.base.date.holiday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;

/**
 * A calendar for managing and querying holidays
 * 管理和查询假日的日历
 *
 * <p>This class provides a comprehensive holiday calendar with support for
 * multiple years, holiday types, and workday calculations.</p>
 * <p>此类提供全面的假日日历，支持多年份、假日类型和工作日计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Holiday storage and retrieval - 假日存储和检索</li>
 *   <li>Special workday (makeup day) support - 特殊工作日（调休日）支持</li>
 *   <li>Workday calculations - 工作日计算</li>
 *   <li>Year and month filtering - 年份和月份过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HolidayCalendar calendar = HolidayCalendar.builder()
 *     .addHoliday(Holiday.of(LocalDate.of(2024, 1, 1), "New Year"))
 *     .addHoliday(Holiday.of(LocalDate.of(2024, 12, 25), "Christmas"))
 *     .addSpecialWorkday(LocalDate.of(2024, 9, 29)) // Makeup day
 *     .build();
 *
 * boolean isHoliday = calendar.isHoliday(LocalDate.of(2024, 1, 1)); // true
 * boolean isWorkday = calendar.isWorkday(LocalDate.of(2024, 1, 1)); // false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction via builder) - 线程安全: 是（通过构建器构造后不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see Holiday
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class HolidayCalendar {

    /**
     * Map of date to holiday
     */
    private final Map<LocalDate, Holiday> holidays;

    /**
     * Set of special workdays (e.g., makeup days)
     */
    private final Set<LocalDate> specialWorkdays;

    /**
     * Weekend days
     */
    private final Set<DayOfWeek> weekendDays;

    /**
     * Calendar name
     */
    private final String name;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor for builder
     */
    private HolidayCalendar(Builder builder) {
        this.holidays = Collections.unmodifiableMap(new TreeMap<>(builder.holidays));
        this.specialWorkdays = Collections.unmodifiableSet(new TreeSet<>(builder.specialWorkdays));
        this.weekendDays = builder.weekendDays != null ?
                Collections.unmodifiableSet(EnumSet.copyOf(builder.weekendDays)) :
                Collections.unmodifiableSet(EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        this.name = builder.name;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an empty calendar
     * 创建空日历
     *
     * @return the empty calendar | 空日历
     */
    public static HolidayCalendar empty() {
        return builder().build();
    }

    /**
     * Creates a calendar from a collection of holidays
     * 从假日集合创建日历
     *
     * @param holidays the holidays | 假日集合
     * @return the calendar | 日历
     */
    public static HolidayCalendar of(Collection<Holiday> holidays) {
        Builder builder = builder();
        holidays.forEach(builder::addHoliday);
        return builder.build();
    }

    /**
     * Creates a builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Checks if a date is a holiday
     * 检查日期是否为假日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a holiday | 如果是假日返回true
     */
    public boolean isHoliday(LocalDate date) {
        return holidays.containsKey(date);
    }

    /**
     * Checks if a date is a public holiday
     * 检查日期是否为公共假日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a public holiday | 如果是公共假日返回true
     */
    public boolean isPublicHoliday(LocalDate date) {
        Holiday holiday = holidays.get(date);
        return holiday != null && holiday.isPublicHoliday();
    }

    /**
     * Checks if a date is a special workday
     * 检查日期是否为特殊工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a special workday | 如果是特殊工作日返回true
     */
    public boolean isSpecialWorkday(LocalDate date) {
        return specialWorkdays.contains(date);
    }

    /**
     * Checks if a date is a weekend
     * 检查日期是否为周末
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a weekend | 如果是周末返回true
     */
    public boolean isWeekend(LocalDate date) {
        return weekendDays.contains(date.getDayOfWeek());
    }

    /**
     * Checks if a date is a workday
     * 检查日期是否为工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a workday | 如果是工作日返回true
     */
    public boolean isWorkday(LocalDate date) {
        // Special workdays override weekend
        if (isSpecialWorkday(date)) {
            return !isHoliday(date);
        }

        // Weekend is not a workday
        if (isWeekend(date)) {
            return false;
        }

        // Holiday is not a workday
        return !isHoliday(date);
    }

    /**
     * Gets the holiday for a date
     * 获取日期的假日
     *
     * @param date the date | 日期
     * @return the holiday, or null if not a holiday | 假日，如果不是假日则为null
     */
    public Holiday getHoliday(LocalDate date) {
        return holidays.get(date);
    }

    /**
     * Gets the holiday for a date as Optional
     * 获取日期的假日（Optional形式）
     *
     * @param date the date | 日期
     * @return the holiday optional | 假日Optional
     */
    public Optional<Holiday> findHoliday(LocalDate date) {
        return Optional.ofNullable(holidays.get(date));
    }

    // ==================== Filtering Methods | 过滤方法 ====================

    /**
     * Gets all holidays
     * 获取所有假日
     *
     * @return the list of holidays | 假日列表
     */
    public List<Holiday> getAllHolidays() {
        return new ArrayList<>(holidays.values());
    }

    /**
     * Gets holidays for a specific year
     * 获取特定年份的假日
     *
     * @param year the year | 年份
     * @return the list of holidays | 假日列表
     */
    public List<Holiday> getHolidays(int year) {
        return holidays.values().stream()
                .filter(h -> h.getYear() == year)
                .sorted()
                .toList();
    }

    /**
     * Gets holidays for a specific year and month
     * 获取特定年份和月份的假日
     *
     * @param year  the year | 年份
     * @param month the month | 月份
     * @return the list of holidays | 假日列表
     */
    public List<Holiday> getHolidays(int year, Month month) {
        return holidays.values().stream()
                .filter(h -> h.getYear() == year && h.getDate().getMonth() == month)
                .sorted()
                .toList();
    }

    /**
     * Gets holidays of a specific type
     * 获取特定类型的假日
     *
     * @param type the holiday type | 假日类型
     * @return the list of holidays | 假日列表
     */
    public List<Holiday> getHolidaysByType(Holiday.HolidayType type) {
        return holidays.values().stream()
                .filter(h -> h.getType() == type)
                .sorted()
                .toList();
    }

    /**
     * Gets holidays in a date range
     * 获取日期范围内的假日
     *
     * @param start the start date (inclusive) | 开始日期（包含）
     * @param end   the end date (inclusive) | 结束日期（包含）
     * @return the list of holidays | 假日列表
     */
    public List<Holiday> getHolidaysInRange(LocalDate start, LocalDate end) {
        return holidays.values().stream()
                .filter(h -> !h.getDate().isBefore(start) && !h.getDate().isAfter(end))
                .sorted()
                .toList();
    }

    // ==================== Workday Calculations | 工作日计算 ====================

    /**
     * Gets the next workday after the specified date
     * 获取指定日期之后的下一个工作日
     *
     * @param date the starting date | 开始日期
     * @return the next workday | 下一个工作日
     */
    public LocalDate nextWorkday(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isWorkday(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Gets the previous workday before the specified date
     * 获取指定日期之前的上一个工作日
     *
     * @param date the starting date | 开始日期
     * @return the previous workday | 上一个工作日
     */
    public LocalDate previousWorkday(LocalDate date) {
        LocalDate prev = date.minusDays(1);
        while (!isWorkday(prev)) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    /**
     * Adds workdays to a date
     * 向日期添加工作日
     *
     * @param date the starting date | 开始日期
     * @param days the number of workdays to add | 要添加的工作日数
     * @return the resulting date | 结果日期
     */
    public LocalDate addWorkdays(LocalDate date, int days) {
        if (days == 0) {
            return date;
        }

        int remaining = Math.abs(days);
        int direction = days > 0 ? 1 : -1;
        LocalDate current = date;

        while (remaining > 0) {
            current = current.plusDays(direction);
            if (isWorkday(current)) {
                remaining--;
            }
        }

        return current;
    }

    /**
     * Counts workdays between two dates
     * 计算两个日期之间的工作日数
     *
     * @param start the start date (inclusive) | 开始日期（包含）
     * @param end   the end date (exclusive) | 结束日期（不包含）
     * @return the number of workdays | 工作日数
     */
    public long countWorkdays(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return -countWorkdays(end, start);
        }

        long count = 0;
        LocalDate current = start;
        while (current.isBefore(end)) {
            if (isWorkday(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the calendar name
     * 获取日历名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all special workdays
     * 获取所有特殊工作日
     *
     * @return the set of special workdays | 特殊工作日集合
     */
    public Set<LocalDate> getSpecialWorkdays() {
        return specialWorkdays;
    }

    /**
     * Gets the weekend days
     * 获取周末天数
     *
     * @return the weekend days | 周末天数
     */
    public Set<DayOfWeek> getWeekendDays() {
        return weekendDays;
    }

    /**
     * Creates a predicate for checking holidays
     * 创建检查假日的谓词
     *
     * @return the predicate | 谓词
     */
    public Predicate<LocalDate> asHolidayPredicate() {
        return this::isHoliday;
    }

    /**
     * Creates a predicate for checking workdays
     * 创建检查工作日的谓词
     *
     * @return the predicate | 谓词
     */
    public Predicate<LocalDate> asWorkdayPredicate() {
        return this::isWorkday;
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public String toString() {
        return "HolidayCalendar{" +
                "name='" + name + '\'' +
                ", holidays=" + holidays.size() +
                ", specialWorkdays=" + specialWorkdays.size() +
                '}';
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for HolidayCalendar
     * HolidayCalendar构建器
     */
    public static class Builder {
        private final Map<LocalDate, Holiday> holidays = new HashMap<>();
        private final Set<LocalDate> specialWorkdays = new HashSet<>();
        private Set<DayOfWeek> weekendDays;
        private String name;

        private Builder() {
        }

        /**
         * Sets the calendar name
         * 设置日历名称
         *
         * @param name the name | 名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds a holiday
         * 添加假日
         *
         * @param holiday the holiday | 假日
         * @return this builder | 此构建器
         */
        public Builder addHoliday(Holiday holiday) {
            holidays.put(holiday.getDate(), holiday);
            return this;
        }

        /**
         * Adds multiple holidays
         * 添加多个假日
         *
         * @param holidays the holidays | 假日集合
         * @return this builder | 此构建器
         */
        public Builder addHolidays(Collection<Holiday> holidays) {
            holidays.forEach(this::addHoliday);
            return this;
        }

        /**
         * Adds a special workday
         * 添加特殊工作日
         *
         * @param date the date | 日期
         * @return this builder | 此构建器
         */
        public Builder addSpecialWorkday(LocalDate date) {
            specialWorkdays.add(date);
            return this;
        }

        /**
         * Adds multiple special workdays
         * 添加多个特殊工作日
         *
         * @param dates the dates | 日期集合
         * @return this builder | 此构建器
         */
        public Builder addSpecialWorkdays(Collection<LocalDate> dates) {
            specialWorkdays.addAll(dates);
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
            this.weekendDays = weekendDays;
            return this;
        }

        /**
         * Builds the HolidayCalendar
         * 构建HolidayCalendar
         *
         * @return the HolidayCalendar | 假日日历
         */
        public HolidayCalendar build() {
            return new HolidayCalendar(this);
        }
    }
}
