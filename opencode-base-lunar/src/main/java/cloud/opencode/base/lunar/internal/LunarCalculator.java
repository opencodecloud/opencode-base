package cloud.opencode.base.lunar.internal;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.exception.DateConversionException;
import cloud.opencode.base.lunar.exception.DateOutOfRangeException;

import java.time.LocalDate;

/**
 * Lunar Calculator
 * 农历计算器
 *
 * <p>Internal calculator for lunar-solar date conversion.</p>
 * <p>农历与公历转换的内部计算器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar-to-lunar conversion algorithm - 公历转农历算法</li>
 *   <li>Lunar-to-solar conversion algorithm - 农历转公历算法</li>
 *   <li>Leap month calculation - 闰月计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal use only
 * LunarDate lunar = LunarCalculator.solarToLunar(LocalDate.of(2024, 2, 10));
 * SolarDate solar = LunarCalculator.lunarToSolar(new LunarDate(2024, 1, 1, false));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (date arguments must not be null) - 空值安全: 否（日期参数不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per calculation using precomputed tables - 每次计算 O(1), 使用预计算表</li>
 *   <li>Space complexity: O(1) for computation - 计算 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class LunarCalculator {

    /**
     * Base date: 1900-01-31 is lunar 1900-01-01
     * 基准日期：1900年1月31日是农历1900年正月初一
     */
    private static final LocalDate BASE_SOLAR_DATE = LocalDate.of(1900, 1, 31);

    private LunarCalculator() {
        // Utility class
    }

    /**
     * Convert solar date to lunar date
     * 公历转农历
     *
     * @param solarDate the solar date | 公历日期
     * @return the lunar date | 农历日期
     */
    public static LunarDate solarToLunar(LocalDate solarDate) {
        int year = solarDate.getYear();
        if (!LunarData.isSupported(year)) {
            throw new DateOutOfRangeException(year);
        }

        // Calculate days from base date
        long daysDiff = solarDate.toEpochDay() - BASE_SOLAR_DATE.toEpochDay();
        if (daysDiff < 0) {
            throw new DateOutOfRangeException(year);
        }

        int offset = (int) daysDiff;

        // Find lunar year
        int lunarYear = LunarData.MIN_YEAR;
        while (lunarYear <= LunarData.MAX_YEAR) {
            int yearDays = LunarData.getYearDays(lunarYear);
            if (offset < yearDays) {
                break;
            }
            offset -= yearDays;
            lunarYear++;
        }

        if (lunarYear > LunarData.MAX_YEAR) {
            throw new DateOutOfRangeException(year);
        }

        // Find lunar month
        int leapMonth = LunarData.getLeapMonth(lunarYear);
        boolean isLeapMonth = false;
        int lunarMonth = 1;

        for (int i = 1; i <= 12; i++) {
            int monthDays = LunarData.getMonthDays(lunarYear, i);

            if (offset < monthDays) {
                lunarMonth = i;
                break;
            }
            offset -= monthDays;

            // Check leap month
            if (leapMonth == i) {
                int leapDays = LunarData.getLeapMonthDays(lunarYear);
                if (offset < leapDays) {
                    lunarMonth = i;
                    isLeapMonth = true;
                    break;
                }
                offset -= leapDays;
            }

            if (i == 12) {
                lunarMonth = 12;
            }
        }

        int lunarDay = offset + 1;

        return new LunarDate(lunarYear, lunarMonth, lunarDay, isLeapMonth);
    }

    /**
     * Convert lunar date to solar date
     * 农历转公历
     *
     * @param lunarDate the lunar date | 农历日期
     * @return the solar date | 公历日期
     */
    public static SolarDate lunarToSolar(LunarDate lunarDate) {
        int year = lunarDate.year();
        int month = lunarDate.month();
        int day = lunarDate.day();
        boolean isLeap = lunarDate.isLeapMonth();

        if (!LunarData.isSupported(year)) {
            throw new DateOutOfRangeException(year);
        }

        // Calculate offset from year start
        int offset = LunarData.getDaysToYearStart(year);

        // Add months
        int leapMonth = LunarData.getLeapMonth(year);
        for (int i = 1; i < month; i++) {
            offset += LunarData.getMonthDays(year, i);
            if (leapMonth == i) {
                offset += LunarData.getLeapMonthDays(year);
            }
        }

        // If leap month, add non-leap month days first
        if (isLeap && leapMonth == month) {
            offset += LunarData.getMonthDays(year, month);
        }

        // Add days
        offset += day - 1;

        // Calculate solar date
        LocalDate solarDate = BASE_SOLAR_DATE.plusDays(offset);

        return new SolarDate(solarDate.getYear(), solarDate.getMonthValue(), solarDate.getDayOfMonth());
    }

    /**
     * Get lunar month days
     * 获取农历某月天数
     *
     * @param year the year | 年份
     * @param month the month | 月份
     * @param isLeap whether leap month | 是否闰月
     * @return days | 天数
     */
    public static int getMonthDays(int year, int month, boolean isLeap) {
        if (isLeap) {
            int leapMonth = LunarData.getLeapMonth(year);
            if (leapMonth != month) {
                return 0; // No such leap month
            }
            return LunarData.getLeapMonthDays(year);
        }
        return LunarData.getMonthDays(year, month);
    }
}
