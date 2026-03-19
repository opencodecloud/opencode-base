package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.calendar.SolarTerm;
import cloud.opencode.base.lunar.exception.DateOutOfRangeException;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.internal.LunarCalculator;
import cloud.opencode.base.lunar.internal.LunarData;
import cloud.opencode.base.lunar.zodiac.Constellation;
import cloud.opencode.base.lunar.zodiac.Zodiac;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OpenLunar
 * 农历门面入口类
 *
 * <p>Static utility class for lunar calendar operations.</p>
 * <p>农历操作的静态工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar-lunar date conversion - 公历农历转换</li>
 *   <li>Zodiac and constellation lookup - 生肖和星座查询</li>
 *   <li>GanZhi (Heavenly Stems and Earthly Branches) calculation - 干支计算</li>
 *   <li>Festival and solar term queries - 节日和节气查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert solar to lunar
 * LunarDate lunar = OpenLunar.solarToLunar(LocalDate.of(2024, 2, 10));
 * System.out.println(lunar.format());  // 甲辰年 正月初一
 *
 * // Get zodiac
 * Zodiac zodiac = OpenLunar.getZodiac(2024);  // DRAGON
 *
 * // Get constellation
 * Constellation constellation = OpenLunar.getConstellation(3, 15);  // PISCES
 *
 * // Get GanZhi
 * GanZhi ganZhi = OpenLunar.getYearGanZhi(2024);  // 甲辰
 *
 * // Get today's lunar date
 * LunarDate today = OpenLunar.today();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (date arguments must not be null) - 空值安全: 否（日期参数不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class OpenLunar {

    private OpenLunar() {
        // Utility class
    }

    // ============ Date Conversion | 日期转换 ============

    /**
     * Convert solar date to lunar date
     * 公历转农历
     *
     * @param date the solar date | 公历日期
     * @return the lunar date | 农历日期
     */
    public static LunarDate solarToLunar(LocalDate date) {
        Objects.requireNonNull(date, "Date must not be null");
        validateYear(date.getYear());
        return LunarCalculator.solarToLunar(date);
    }

    /**
     * Convert solar date to lunar date
     * 公历转农历
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     * @return the lunar date | 农历日期
     */
    public static LunarDate solarToLunar(int year, int month, int day) {
        return solarToLunar(LocalDate.of(year, month, day));
    }

    /**
     * Convert lunar date to solar date
     * 农历转公历
     *
     * @param lunar the lunar date | 农历日期
     * @return the solar date | 公历日期
     */
    public static SolarDate lunarToSolar(LunarDate lunar) {
        Objects.requireNonNull(lunar, "Lunar date must not be null");
        return LunarCalculator.lunarToSolar(lunar);
    }

    /**
     * Convert lunar date to solar date
     * 农历转公历
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month | 农历月
     * @param day the lunar day | 农历日
     * @param isLeap whether leap month | 是否闰月
     * @return the solar date | 公历日期
     */
    public static SolarDate lunarToSolar(int year, int month, int day, boolean isLeap) {
        return lunarToSolar(new LunarDate(year, month, day, isLeap));
    }

    // ============ Solar Terms | 节气 ============

    /**
     * Get solar term for date (returns null if not a solar term day)
     * 获取某日的节气（如果不是节气日返回null）
     *
     * @param date the date | 日期
     * @return the solar term or null | 节气或null
     */
    public static SolarTerm getSolarTerm(LocalDate date) {
        return SolarTerm.of(date);
    }

    /**
     * Get all solar terms for year
     * 获取某年所有节气
     *
     * @param year the year | 年份
     * @return list of solar terms | 节气列表
     */
    public static List<SolarTerm> getSolarTerms(int year) {
        validateYear(year);
        return SolarTerm.ofYear(year);
    }

    /**
     * Get next solar term from date
     * 获取下一个节气
     *
     * @param date the date | 日期
     * @return the next solar term | 下一个节气
     */
    public static SolarTerm getNextSolarTerm(LocalDate date) {
        return SolarTerm.next(date);
    }

    // ============ Festivals | 节日 ============

    /**
     * Get festivals for date
     * 获取某日的节日
     *
     * @param date the date | 日期
     * @return list of festivals | 节日列表
     */
    public static List<Festival> getFestivals(LocalDate date) {
        List<Festival> festivals = new ArrayList<>();

        // Solar festivals
        festivals.addAll(Festival.getSolarFestivals(date));

        // Lunar festivals
        LunarDate lunar = solarToLunar(date);
        festivals.addAll(Festival.getLunarFestivals(lunar.month(), lunar.day()));

        return festivals;
    }

    /**
     * Check if date is a festival
     * 检查是否为节日
     *
     * @param date the date | 日期
     * @return true if festival | 如果是节日返回true
     */
    public static boolean isFestival(LocalDate date) {
        return !getFestivals(date).isEmpty();
    }

    // ============ Zodiac & Constellation | 生肖与星座 ============

    /**
     * Get zodiac for year
     * 获取年份的生肖
     *
     * @param year the year | 年份
     * @return the zodiac | 生肖
     */
    public static Zodiac getZodiac(int year) {
        return Zodiac.of(year);
    }

    /**
     * Get constellation for date
     * 获取日期的星座
     *
     * @param date the date | 日期
     * @return the constellation | 星座
     */
    public static Constellation getConstellation(LocalDate date) {
        return Constellation.of(date);
    }

    /**
     * Get constellation for month and day
     * 获取月日的星座
     *
     * @param month the month | 月
     * @param day the day | 日
     * @return the constellation | 星座
     */
    public static Constellation getConstellation(int month, int day) {
        return Constellation.of(month, day);
    }

    // ============ GanZhi | 干支 ============

    /**
     * Get year GanZhi
     * 获取年干支
     *
     * @param year the year | 年份
     * @return the GanZhi | 干支
     */
    public static GanZhi getYearGanZhi(int year) {
        return GanZhi.ofYear(year);
    }

    /**
     * Get month GanZhi
     * 获取月干支
     *
     * @param year the year | 年份
     * @param month the month | 月份
     * @return the GanZhi | 干支
     */
    public static GanZhi getMonthGanZhi(int year, int month) {
        return GanZhi.ofMonth(year, month);
    }

    /**
     * Get day GanZhi
     * 获取日干支
     *
     * @param date the date | 日期
     * @return the GanZhi | 干支
     */
    public static GanZhi getDayGanZhi(LocalDate date) {
        return GanZhi.ofDay(date);
    }

    // ============ Utility Methods | 辅助方法 ============

    /**
     * Get leap month for year (0 = no leap month)
     * 获取某年的闰月（0表示无闰月）
     *
     * @param year the year | 年份
     * @return the leap month | 闰月
     */
    public static int getLeapMonth(int year) {
        validateYear(year);
        return LunarData.getLeapMonth(year);
    }

    /**
     * Get lunar month days
     * 获取农历某月天数
     *
     * @param year the year | 年份
     * @param month the month | 月份
     * @param isLeap whether leap month | 是否闰月
     * @return the days | 天数
     */
    public static int getLunarMonthDays(int year, int month, boolean isLeap) {
        validateYear(year);
        return LunarCalculator.getMonthDays(year, month, isLeap);
    }

    /**
     * Get today's lunar date
     * 获取今天的农历日期
     *
     * @return the lunar date | 农历日期
     */
    public static LunarDate today() {
        return solarToLunar(LocalDate.now());
    }

    /**
     * Check if year is supported
     * 检查年份是否支持
     *
     * @param year the year | 年份
     * @return true if supported | 如果支持返回true
     */
    public static boolean isSupported(int year) {
        return LunarData.isSupported(year);
    }

    /**
     * Validate year range
     * 验证年份范围
     */
    private static void validateYear(int year) {
        if (!isSupported(year)) {
            throw new DateOutOfRangeException(year);
        }
    }
}
