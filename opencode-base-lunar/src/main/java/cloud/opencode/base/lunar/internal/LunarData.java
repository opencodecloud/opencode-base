package cloud.opencode.base.lunar.internal;

/**
 * Lunar Data
 * 农历数据表
 *
 * <p>Internal lunar calendar data table (1900-2100).</p>
 * <p>内部农历数据表（1900-2100）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encoded lunar year data (1900-2100) - 编码的农历年数据（1900-2100）</li>
 *   <li>Month length extraction - 月份天数提取</li>
 *   <li>Leap month detection - 闰月检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal use only
 * int leapMonth = LunarData.getLeapMonth(2024);
 * int monthDays = LunarData.getMonthDays(2024, 1);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable static data) - 线程安全: 是（不可变静态数据）</li>
 *   <li>Null-safe: N/A (primitive parameters) - 空值安全: 不适用（原始类型参数）</li>
 * </ul>
 *
 * <p>Data encoding: Each year is encoded as a 32-bit integer.</p>
 * <ul>
 *   <li>Bits 0-3: Leap month (0 = no leap month)</li>
 *   <li>Bits 4-16: Month lengths (1 = 30 days, 0 = 29 days)</li>
 *   <li>Bit 17: Leap month length (1 = 30 days)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class LunarData {

    /**
     * Base year | 基准年
     */
    public static final int BASE_YEAR = 1900;

    /**
     * Minimum supported year | 最小支持年份
     */
    public static final int MIN_YEAR = 1900;

    /**
     * Maximum supported year | 最大支持年份
     */
    public static final int MAX_YEAR = 2100;

    /**
     * Lunar year data (1900-2100)
     * 农历年数据
     */
    private static final int[] LUNAR_INFO = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, // 1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, // 1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, // 1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, // 1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, // 1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, // 1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, // 1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, // 1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, // 1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, // 1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, // 2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, // 2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, // 2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, // 2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, // 2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, // 2050-2059
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, // 2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, // 2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, // 2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, // 2090-2099
        0x0d520  // 2100
    };

    /**
     * Days from 1900-01-31 to year start
     * 从1900年1月31日到各年年初的天数
     */
    private static final int[] YEAR_DAYS;

    static {
        YEAR_DAYS = new int[MAX_YEAR - MIN_YEAR + 2];
        YEAR_DAYS[0] = 0;
        for (int i = 0; i < LUNAR_INFO.length; i++) {
            YEAR_DAYS[i + 1] = YEAR_DAYS[i] + getYearDays(MIN_YEAR + i);
        }
    }

    private LunarData() {
        // Utility class
    }

    /**
     * Get leap month for year (0 = no leap month)
     * 获取某年的闰月（0表示无闰月）
     *
     * @param year the year | 年份
     * @return the leap month | 闰月
     */
    public static int getLeapMonth(int year) {
        return LUNAR_INFO[year - BASE_YEAR] & 0xf;
    }

    /**
     * Get leap month days
     * 获取闰月天数
     *
     * @param year the year | 年份
     * @return days (0, 29, or 30) | 天数
     */
    public static int getLeapMonthDays(int year) {
        if (getLeapMonth(year) == 0) {
            return 0;
        }
        return (LUNAR_INFO[year - BASE_YEAR] & 0x10000) != 0 ? 30 : 29;
    }

    /**
     * Get month days
     * 获取某月天数
     *
     * @param year the year | 年份
     * @param month the month (1-12) | 月份
     * @return days (29 or 30) | 天数
     */
    public static int getMonthDays(int year, int month) {
        return (LUNAR_INFO[year - BASE_YEAR] & (0x10000 >> month)) != 0 ? 30 : 29;
    }

    /**
     * Get total days in year
     * 获取某年总天数
     *
     * @param year the year | 年份
     * @return total days | 总天数
     */
    public static int getYearDays(int year) {
        int sum = 0;
        for (int i = 1; i <= 12; i++) {
            sum += getMonthDays(year, i);
        }
        return sum + getLeapMonthDays(year);
    }

    /**
     * Get days from base date to year start
     * 获取从基准日期到年初的天数
     *
     * @param year the year | 年份
     * @return days | 天数
     */
    public static int getDaysToYearStart(int year) {
        return YEAR_DAYS[year - MIN_YEAR];
    }

    /**
     * Check if year is supported
     * 检查年份是否支持
     *
     * @param year the year | 年份
     * @return true if supported | 如果支持返回true
     */
    public static boolean isSupported(int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }
}
