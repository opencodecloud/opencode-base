package cloud.opencode.base.date.holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for providing holiday data
 * 节假日数据提供者接口
 *
 * <p>This interface defines the contract for providing holiday information.
 * Implementations can provide data from various sources like configuration files,
 * databases, or external APIs.</p>
 * <p>此接口定义了提供节假日信息的契约。实现可以从配置文件、数据库或外部API等各种来源提供数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Holiday data retrieval by year - 按年份检索假日数据</li>
 *   <li>Holiday checking for specific dates - 检查特定日期是否为假日</li>
 *   <li>Special workday support - 特殊工作日支持</li>
 *   <li>Country/region identification - 国家/地区标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HolidayProvider provider = new ChinaHolidayProvider();
 * List<Holiday> holidays = provider.getHolidays(2024);
 * boolean isHoliday = provider.isHoliday(LocalDate.of(2024, 10, 1));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public interface HolidayProvider {

    /**
     * Gets the provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String getName();

    /**
     * Gets the country/region code
     * 获取国家/地区代码
     *
     * @return the country code (e.g., "CN", "US") | 国家代码（如"CN"、"US"）
     */
    String getCountryCode();

    /**
     * Gets all holidays for a specific year
     * 获取指定年份的所有节假日
     *
     * @param year the year | 年份
     * @return list of holidays | 节假日列表
     */
    List<Holiday> getHolidays(int year);

    /**
     * Gets holidays in a date range
     * 获取日期范围内的节假日
     *
     * @param start the start date (inclusive) | 起始日期（包含）
     * @param end the end date (inclusive) | 结束日期（包含）
     * @return list of holidays | 节假日列表
     */
    List<Holiday> getHolidays(LocalDate start, LocalDate end);

    /**
     * Checks if a date is a holiday
     * 检查日期是否为节假日
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a holiday | 如果是节假日返回true
     */
    boolean isHoliday(LocalDate date);

    /**
     * Gets the holiday for a specific date
     * 获取指定日期的节假日
     *
     * @param date the date | 日期
     * @return the holiday, or empty if not a holiday | 节假日，如果不是节假日则为空
     */
    Optional<Holiday> getHoliday(LocalDate date);

    /**
     * Checks if a date is a workday
     * 检查日期是否为工作日
     *
     * <p>This considers both weekends and holidays.</p>
     * <p>这同时考虑周末和节假日。</p>
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's a workday | 如果是工作日返回true
     */
    boolean isWorkday(LocalDate date);

    /**
     * Gets the set of dates that are adjusted workdays (补班日)
     * 获取调休工作日的日期集合（补班日）
     *
     * @param year the year | 年份
     * @return set of adjusted workday dates | 调休工作日日期集合
     */
    Set<LocalDate> getAdjustedWorkdays(int year);

    /**
     * Checks if a date is an adjusted workday (补班日)
     * 检查日期是否为调休工作日（补班日）
     *
     * @param date the date to check | 要检查的日期
     * @return true if it's an adjusted workday | 如果是调休工作日返回true
     */
    boolean isAdjustedWorkday(LocalDate date);

    /**
     * Gets the supported year range
     * 获取支持的年份范围
     *
     * @return array of [minYear, maxYear] | [最小年份, 最大年份]数组
     */
    int[] getSupportedYearRange();

    /**
     * Checks if a year is supported
     * 检查年份是否受支持
     *
     * @param year the year | 年份
     * @return true if supported | 如果支持返回true
     */
    default boolean isYearSupported(int year) {
        int[] range = getSupportedYearRange();
        return year >= range[0] && year <= range[1];
    }

    /**
     * Refreshes the holiday data (if applicable)
     * 刷新节假日数据（如果适用）
     */
    default void refresh() {
        // Default: do nothing
    }
}
