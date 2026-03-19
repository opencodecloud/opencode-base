package cloud.opencode.base.date.lunar;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Utility class for lunar calendar conversions
 * 农历转换工具类
 *
 * <p>This class provides static methods for converting between Gregorian
 * (solar) calendar dates and Chinese lunar calendar dates.</p>
 * <p>此类提供阳历（公历）和农历之间相互转换的静态方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar to lunar conversion - 阳历转农历</li>
 *   <li>Lunar to solar conversion - 农历转阳历</li>
 *   <li>Get zodiac for a date - 获取日期的生肖</li>
 *   <li>Solar term calculations - 节气计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert solar to lunar
 * Lunar lunar = LunarUtil.toLunar(LocalDate.of(2024, 2, 10));
 *
 * // Convert lunar to solar
 * LocalDate solar = LunarUtil.toSolar(Lunar.of(2024, 1, 1));
 *
 * // Get zodiac
 * String zodiac = LunarUtil.getZodiac(2024);  // "龙"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - conversions iterate over a bounded range (years 1900-2100, max ~200 years × 13 months) backed by a fixed lookup table - 时间复杂度: O(1) - 转换在有限范围（1900-2100 年，最多约 200 年 × 13 个月）内迭代，依赖固定查找表</li>
 *   <li>Space complexity: O(1) - uses a pre-allocated static LUNAR_INFO array; no additional allocations per call - 空间复杂度: O(1) - 使用预分配的静态 LUNAR_INFO 数组；每次调用不额外分配内存</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class LunarUtil {

    private LunarUtil() {
        // Utility class
    }

    // Lunar calendar data from 1900 to 2100
    // Each entry contains: leap month info + 12/13 months' days info
    private static final int[] LUNAR_INFO = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
            0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
            0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
            0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
            0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
            0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
            0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
            0x0d520
    };

    private static final int BASE_YEAR = 1900;
    private static final LocalDate BASE_DATE = LocalDate.of(1900, 1, 31);

    // ==================== Conversion Methods ====================

    /**
     * Converts a solar (Gregorian) date to lunar date
     * 将阳历（公历）日期转换为农历日期
     *
     * @param solarDate the solar date | 阳历日期
     * @return the lunar date | 农历日期
     */
    public static Lunar toLunar(LocalDate solarDate) {
        Objects.requireNonNull(solarDate, "solarDate must not be null");

        int offset = (int) (solarDate.toEpochDay() - BASE_DATE.toEpochDay());
        if (offset < 0) {
            throw new IllegalArgumentException("Date must be after 1900-01-31");
        }

        int year = BASE_YEAR;
        int yearDays;

        // Find the lunar year
        while (year < 2100 && offset > 0) {
            yearDays = getLunarYearDays(year);
            if (offset < yearDays) {
                break;
            }
            offset -= yearDays;
            year++;
        }

        // Find the lunar month
        int leapMonth = getLeapMonth(year);
        boolean isLeap = false;
        int month = 1;

        for (int i = 1; i <= 12; i++) {
            int monthDays;

            if (leapMonth > 0 && i == leapMonth + 1 && !isLeap) {
                monthDays = getLeapMonthDays(year);
                isLeap = true;
                i--;
            } else {
                monthDays = getLunarMonthDays(year, i);
            }

            if (offset < monthDays) {
                month = i;
                break;
            }
            offset -= monthDays;

            if (isLeap && i == leapMonth + 1) {
                isLeap = false;
            }
        }

        int day = offset + 1;

        return Lunar.of(year, month, day, isLeap && month == leapMonth);
    }

    /**
     * Converts a lunar date to solar (Gregorian) date
     * 将农历日期转换为阳历（公历）日期
     *
     * @param lunar the lunar date | 农历日期
     * @return the solar date | 阳历日期
     */
    public static LocalDate toSolar(Lunar lunar) {
        Objects.requireNonNull(lunar, "lunar must not be null");

        int offset = 0;
        int year = lunar.getYear();
        int month = lunar.getMonth();
        int day = lunar.getDay();
        boolean isLeap = lunar.isLeapMonth();

        // Add days from base year to target year
        for (int y = BASE_YEAR; y < year; y++) {
            offset += getLunarYearDays(y);
        }

        // Add days from month 1 to target month
        int leapMonth = getLeapMonth(year);
        for (int m = 1; m < month; m++) {
            offset += getLunarMonthDays(year, m);
            if (m == leapMonth) {
                offset += getLeapMonthDays(year);
            }
        }

        // If the target month is a leap month, add the normal month's days first
        if (isLeap && month == leapMonth) {
            offset += getLunarMonthDays(year, month);
        }

        // Add the days within the month
        offset += day - 1;

        return BASE_DATE.plusDays(offset);
    }

    // ==================== Lunar Calendar Info Methods ====================

    /**
     * Gets the leap month for a year (0 if no leap month)
     * 获取指定年份的闰月（如果没有闰月则为0）
     *
     * @param year the lunar year | 农历年
     * @return the leap month (1-12) or 0 | 闰月（1-12）或0
     */
    public static int getLeapMonth(int year) {
        return LUNAR_INFO[year - BASE_YEAR] & 0xf;
    }

    /**
     * Gets the number of days in a lunar year
     * 获取农历年的总天数
     *
     * @param year the lunar year | 农历年
     * @return the number of days | 天数
     */
    public static int getLunarYearDays(int year) {
        int sum = 348;
        for (int i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - BASE_YEAR] & i) != 0) {
                sum += 1;
            }
        }
        return sum + getLeapMonthDays(year);
    }

    /**
     * Gets the number of days in a lunar month
     * 获取农历月的天数
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month | 农历月
     * @return the number of days (29 or 30) | 天数（29或30）
     */
    public static int getLunarMonthDays(int year, int month) {
        return (LUNAR_INFO[year - BASE_YEAR] & (0x10000 >> month)) != 0 ? 30 : 29;
    }

    /**
     * Gets the number of days in the leap month
     * 获取闰月的天数
     *
     * @param year the lunar year | 农历年
     * @return the number of days (0, 29, or 30) | 天数（0、29或30）
     */
    public static int getLeapMonthDays(int year) {
        if (getLeapMonth(year) == 0) {
            return 0;
        }
        return (LUNAR_INFO[year - BASE_YEAR] & 0x10000) != 0 ? 30 : 29;
    }

    // ==================== Zodiac Methods ====================

    /**
     * Gets the Chinese zodiac animal for a year
     * 获取指定年份的生肖
     *
     * @param year the year | 年份
     * @return the zodiac animal | 生肖
     */
    public static String getZodiac(int year) {
        return Lunar.of(year, 1, 1).getZodiac();
    }

    /**
     * Gets the Chinese zodiac animal for a date
     * 获取指定日期的生肖
     *
     * @param date the date | 日期
     * @return the zodiac animal | 生肖
     */
    public static String getZodiac(LocalDate date) {
        return toLunar(date).getZodiac();
    }

    /**
     * Gets the stem-branch year name
     * 获取干支纪年
     *
     * @param year the year | 年份
     * @return the stem-branch year name | 干支年名
     */
    public static String getStemBranchYear(int year) {
        return Lunar.of(year, 1, 1).getStemBranchYear();
    }

    // ==================== Query Methods ====================

    /**
     * Checks if a lunar year has a leap month
     * 检查农历年是否有闰月
     *
     * @param year the lunar year | 农历年
     * @return true if has leap month | 如果有闰月返回true
     */
    public static boolean hasLeapMonth(int year) {
        return getLeapMonth(year) != 0;
    }

    /**
     * Gets the current lunar date
     * 获取当前农历日期
     *
     * @return the current lunar date | 当前农历日期
     */
    public static Lunar today() {
        return toLunar(LocalDate.now());
    }
}
