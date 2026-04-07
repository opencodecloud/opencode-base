package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.internal.LunarData;
import cloud.opencode.base.lunar.zodiac.Zodiac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lunar Year information record
 * 农历年信息记录
 *
 * <p>Immutable record representing a Chinese lunar calendar year with comprehensive
 * year-level information including total days, leap month, GanZhi, zodiac, and
 * month enumeration.</p>
 * <p>表示中国农历年的不可变记录，包含总天数、闰月、干支、生肖和月份枚举等
 * 全面的年级别信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Year-level lunar calendar data - 年级别农历数据</li>
 *   <li>Leap month detection and information - 闰月检测和信息</li>
 *   <li>GanZhi and Zodiac calculation - 干支和生肖计算</li>
 *   <li>Month enumeration - 月份枚举</li>
 *   <li>Year naming (e.g. "甲辰年") - 年份命名</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LunarYear year = LunarYear.of(2024);
 * int totalDays = year.getTotalDays();
 * int leapMonth = year.getLeapMonth();
 * boolean hasLeap = year.hasLeapMonth();
 * GanZhi ganZhi = year.getGanZhi();
 * Zodiac zodiac = year.getZodiac();
 * String name = year.getName();        // "甲辰年"
 * List<LunarMonth> months = year.getMonths();
 * LunarMonth m = year.getMonth(1);     // 正月
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: N/A (primitive field) - 空值安全: 不适用（原始类型字段）</li>
 * </ul>
 *
 * @param year the lunar year (1900-2100) | 农历年（1900-2100）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
public record LunarYear(int year) {

    /**
     * Compact constructor — validates year range
     * 紧凑构造函数 — 验证年份范围
     */
    public LunarYear {
        if (!LunarData.isSupported(year)) {
            throw new IllegalArgumentException(
                    "Year must be between " + LunarData.MIN_YEAR
                            + " and " + LunarData.MAX_YEAR + ", got: " + year);
        }
    }

    /**
     * Create a LunarYear instance
     * 创建农历年实例
     *
     * @param year the lunar year | 农历年
     * @return the LunarYear | 农历年实例
     */
    public static LunarYear of(int year) {
        return new LunarYear(year);
    }

    /**
     * Get total days in this lunar year
     * 获取此农历年的总天数
     *
     * @return total days | 总天数
     */
    public int getTotalDays() {
        return LunarData.getYearDays(year);
    }

    /**
     * Get the leap month number (0 means no leap month)
     * 获取闰月月份（0表示无闰月）
     *
     * @return the leap month number, or 0 | 闰月月份，或0
     */
    public int getLeapMonth() {
        return LunarData.getLeapMonth(year);
    }

    /**
     * Get days in the leap month (0 if no leap month)
     * 获取闰月天数（无闰月时为0）
     *
     * @return days in leap month | 闰月天数
     */
    public int getLeapMonthDays() {
        return LunarData.getLeapMonthDays(year);
    }

    /**
     * Get the number of months in this year (12 or 13)
     * 获取此年的月份数（12或13）
     *
     * @return month count | 月份数
     */
    public int getMonthCount() {
        return hasLeapMonth() ? 13 : 12;
    }

    /**
     * Get all months in this year in order
     * 按顺序获取此年所有月份
     *
     * <p>If the year has a leap month, the leap month appears immediately
     * after the corresponding regular month.</p>
     * <p>如果该年有闰月，闰月紧跟在对应的常规月份之后。</p>
     *
     * @return unmodifiable list of months | 不可变月份列表
     */
    public List<LunarMonth> getMonths() {
        int leapMonth = getLeapMonth();
        List<LunarMonth> months = new ArrayList<>(getMonthCount());
        for (int m = 1; m <= 12; m++) {
            months.add(LunarMonth.of(year, m));
            if (leapMonth == m) {
                months.add(LunarMonth.of(year, m, true));
            }
        }
        return Collections.unmodifiableList(months);
    }

    /**
     * Get the GanZhi for this year
     * 获取此年的干支
     *
     * @return the GanZhi | 干支
     */
    public GanZhi getGanZhi() {
        return GanZhi.ofYear(year);
    }

    /**
     * Get the zodiac for this year
     * 获取此年的生肖
     *
     * @return the zodiac | 生肖
     */
    public Zodiac getZodiac() {
        return Zodiac.of(year);
    }

    /**
     * Get the Chinese name of this year (e.g. "甲辰年")
     * 获取此年的中文名称（如"甲辰年"）
     *
     * @return the year name | 年份名称
     */
    public String getName() {
        return getGanZhi().getName() + "年";
    }

    /**
     * Check whether this year has a leap month
     * 检查此年是否有闰月
     *
     * @return true if has leap month | 如果有闰月返回true
     */
    public boolean hasLeapMonth() {
        return getLeapMonth() != 0;
    }

    /**
     * Get a specific non-leap month in this year
     * 获取此年指定的非闰月
     *
     * @param month the month number (1-12) | 月份（1-12）
     * @return the LunarMonth | 农历月
     */
    public LunarMonth getMonth(int month) {
        return LunarMonth.of(year, month);
    }

    /**
     * Get a specific month in this year, optionally leap
     * 获取此年指定月份（可选闰月）
     *
     * @param month the month number (1-12) | 月份（1-12）
     * @param isLeap whether to get the leap month | 是否获取闰月
     * @return the LunarMonth | 农历月
     */
    public LunarMonth getMonth(int month, boolean isLeap) {
        return LunarMonth.of(year, month, isLeap);
    }

    @Override
    public String toString() {
        return getName();
    }
}
