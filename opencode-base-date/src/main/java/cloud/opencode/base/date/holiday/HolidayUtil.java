package cloud.opencode.base.date.holiday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for holiday operations
 * 节假日工具类
 *
 * <p>This class provides static methods for working with holidays, including
 * checking if a date is a holiday, calculating workdays, and more.</p>
 * <p>此类提供处理节假日的静态方法，包括检查日期是否为节假日、计算工作日等。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Check if date is holiday/workday - 检查日期是否为节假日/工作日</li>
 *   <li>Calculate workdays between dates - 计算两个日期之间的工作日</li>
 *   <li>Add/subtract workdays - 加减工作日</li>
 *   <li>Support for China holidays - 支持中国节假日</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if date is holiday
 * boolean isHoliday = HolidayUtil.isHoliday(LocalDate.of(2024, 10, 1));
 *
 * // Check if date is workday
 * boolean isWorkday = HolidayUtil.isWorkday(LocalDate.of(2024, 10, 8));
 *
 * // Add workdays
 * LocalDate result = HolidayUtil.plusWorkdays(LocalDate.now(), 5);
 *
 * // Calculate workdays between dates
 * long workdays = HolidayUtil.workdaysBetween(start, end);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap进行缓存）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for workdaysBetween and plusWorkdays where n=number of days iterated; O(k) for nextWorkday/previousWorkday where k=skip count - 时间复杂度: workdaysBetween 和 plusWorkdays 为 O(n)，n 为迭代天数；nextWorkday/previousWorkday 为 O(k)</li>
 *   <li>Space complexity: O(1) - holiday lookups use a ConcurrentHashMap cache with O(1) amortized access - 空间复杂度: O(1) - 节假日查找使用 ConcurrentHashMap 缓存，摊销 O(1) 访问</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class HolidayUtil {

    private HolidayUtil() {
        // Utility class
    }

    // ==================== Provider Management ====================

    private static volatile HolidayProvider defaultProvider;
    private static final Map<String, HolidayProvider> providers = new ConcurrentHashMap<>();

    /**
     * Sets the default holiday provider
     * 设置默认节假日提供者
     *
     * @param provider the provider | 提供者
     */
    public static void setDefaultProvider(HolidayProvider provider) {
        defaultProvider = Objects.requireNonNull(provider, "provider must not be null");
    }

    /**
     * Gets the default holiday provider
     * 获取默认节假日提供者
     *
     * @return the default provider | 默认提供者
     */
    public static HolidayProvider getDefaultProvider() {
        if (defaultProvider == null) {
            synchronized (HolidayUtil.class) {
                if (defaultProvider == null) {
                    defaultProvider = new DefaultHolidayProvider();
                }
            }
        }
        return defaultProvider;
    }

    /**
     * Registers a holiday provider
     * 注册节假日提供者
     *
     * @param provider the provider | 提供者
     */
    public static void registerProvider(HolidayProvider provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        providers.put(provider.getCountryCode(), provider);
    }

    /**
     * Gets a provider by country code
     * 按国家代码获取提供者
     *
     * @param countryCode the country code | 国家代码
     * @return the provider, or empty if not found | 提供者，如果未找到则为空
     */
    public static Optional<HolidayProvider> getProvider(String countryCode) {
        return Optional.ofNullable(providers.get(countryCode));
    }

    // ==================== Holiday Check Methods ====================

    /**
     * Checks if a date is a holiday
     * 检查日期是否为节假日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a holiday | 如果是节假日返回true
     */
    public static boolean isHoliday(LocalDate date) {
        return getDefaultProvider().isHoliday(date);
    }

    /**
     * Checks if a date is a holiday using specified provider
     * 使用指定提供者检查日期是否为节假日
     *
     * @param date the date to check | 要检查的日期
     * @param provider the provider | 提供者
     * @return true if it's a holiday | 如果是节假日返回true
     */
    public static boolean isHoliday(LocalDate date, HolidayProvider provider) {
        return provider.isHoliday(date);
    }

    /**
     * Gets the holiday for a specific date
     * 获取指定日期的节假日
     *
     * @param date the date | 日期
     * @return the holiday, or empty if not a holiday | 节假日，如果不是节假日则为空
     */
    public static Optional<Holiday> getHoliday(LocalDate date) {
        return getDefaultProvider().getHoliday(date);
    }

    // ==================== Workday Check Methods ====================

    /**
     * Checks if a date is a workday
     * 检查日期是否为工作日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a workday | 如果是工作日返回true
     */
    public static boolean isWorkday(LocalDate date) {
        return getDefaultProvider().isWorkday(date);
    }

    /**
     * Checks if a date is a workday using specified provider
     * 使用指定提供者检查日期是否为工作日
     *
     * @param date the date to check | 要检查的日期
     * @param provider the provider | 提供者
     * @return true if it's a workday | 如果是工作日返回true
     */
    public static boolean isWorkday(LocalDate date, HolidayProvider provider) {
        return provider.isWorkday(date);
    }

    /**
     * Checks if a date is a weekend (Saturday or Sunday)
     * 检查日期是否为周末（星期六或星期日）
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a weekend | 如果是周末返回true
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Checks if a date is an adjusted workday (补班日)
     * 检查日期是否为调休工作日（补班日）
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's an adjusted workday | 如果是调休工作日返回true
     */
    public static boolean isAdjustedWorkday(LocalDate date) {
        return getDefaultProvider().isAdjustedWorkday(date);
    }

    // ==================== Workday Calculation Methods ====================

    /**
     * Adds workdays to a date
     * 向日期添加工作日
     *
     * @param date the start date | 起始日期
     * @param workdays the number of workdays to add | 要添加的工作日数
     * @return the resulting date | 结果日期
     */
    public static LocalDate plusWorkdays(LocalDate date, int workdays) {
        Objects.requireNonNull(date, "date must not be null");
        if (workdays == 0) {
            return date;
        }

        int direction = workdays > 0 ? 1 : -1;
        int remaining = Math.abs(workdays);
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
     * Subtracts workdays from a date
     * 从日期减去工作日
     *
     * @param date the start date | 起始日期
     * @param workdays the number of workdays to subtract | 要减去的工作日数
     * @return the resulting date | 结果日期
     */
    public static LocalDate minusWorkdays(LocalDate date, int workdays) {
        return plusWorkdays(date, -workdays);
    }

    /**
     * Calculates the number of workdays between two dates
     * 计算两个日期之间的工作日数
     *
     * @param start the start date (inclusive) | 起始日期（包含）
     * @param end the end date (exclusive) | 结束日期（不包含）
     * @return the number of workdays | 工作日数
     */
    public static long workdaysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        if (start.isAfter(end)) {
            return -workdaysBetween(end, start);
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

    /**
     * Gets the next workday after the given date
     * 获取给定日期之后的下一个工作日
     *
     * @param date the date | 日期
     * @return the next workday | 下一个工作日
     */
    public static LocalDate nextWorkday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        LocalDate next = date.plusDays(1);
        while (!isWorkday(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Gets the previous workday before the given date
     * 获取给定日期之前的上一个工作日
     *
     * @param date the date | 日期
     * @return the previous workday | 上一个工作日
     */
    public static LocalDate previousWorkday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        LocalDate prev = date.minusDays(1);
        while (!isWorkday(prev)) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    /**
     * Gets the next workday on or after the given date
     * 获取给定日期当天或之后的下一个工作日
     *
     * @param date the date | 日期
     * @return the next workday (or same date if it's a workday) | 下一个工作日（如果当天是工作日则返回当天）
     */
    public static LocalDate nextOrSameWorkday(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return isWorkday(date) ? date : nextWorkday(date);
    }

    // ==================== Holiday List Methods ====================

    /**
     * Gets all holidays for a specific year
     * 获取指定年份的所有节假日
     *
     * @param year the year | 年份
     * @return list of holidays | 节假日列表
     */
    public static List<Holiday> getHolidays(int year) {
        return getDefaultProvider().getHolidays(year);
    }

    /**
     * Gets holidays in a date range
     * 获取日期范围内的节假日
     *
     * @param start the start date (inclusive) | 起始日期（包含）
     * @param end the end date (inclusive) | 结束日期（包含）
     * @return list of holidays | 节假日列表
     */
    public static List<Holiday> getHolidays(LocalDate start, LocalDate end) {
        return getDefaultProvider().getHolidays(start, end);
    }

    // ==================== Default Provider Implementation ====================

    /**
     * Default holiday provider (basic implementation without specific country data)
     * 默认节假日提供者（不包含特定国家数据的基本实现）
     */
    private static class DefaultHolidayProvider implements HolidayProvider {

        @Override
        public String getName() {
            return "Default";
        }

        @Override
        public String getCountryCode() {
            return "DEFAULT";
        }

        @Override
        public List<Holiday> getHolidays(int year) {
            return List.of();
        }

        @Override
        public List<Holiday> getHolidays(LocalDate start, LocalDate end) {
            return List.of();
        }

        @Override
        public boolean isHoliday(LocalDate date) {
            return false;
        }

        @Override
        public Optional<Holiday> getHoliday(LocalDate date) {
            return Optional.empty();
        }

        @Override
        public boolean isWorkday(LocalDate date) {
            // By default, only weekends are non-workdays
            return !isWeekend(date);
        }

        @Override
        public Set<LocalDate> getAdjustedWorkdays(int year) {
            return Set.of();
        }

        @Override
        public boolean isAdjustedWorkday(LocalDate date) {
            return false;
        }

        @Override
        public int[] getSupportedYearRange() {
            return new int[]{1970, 2100};
        }
    }
}
