package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.internal.LunarCalculator;
import cloud.opencode.base.lunar.zodiac.Zodiac;

import java.util.List;

/**
 * Lunar Date
 * 农历日期
 *
 * <p>Immutable record representing a lunar (Chinese) calendar date.</p>
 * <p>表示农历（中国历法）日期的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lunar date representation with leap month support - 农历日期表示（支持闰月）</li>
 *   <li>Chinese date formatting - 中文日期格式化</li>
 *   <li>Zodiac and GanZhi calculation - 生肖和干支计算</li>
 *   <li>Solar date conversion - 公历日期转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LunarDate lunar = new LunarDate(2024, 1, 1, false);
 * System.out.println(lunar.format());  // 甲辰年 正月初一
 * Zodiac zodiac = lunar.getZodiac();   // DRAGON
 * SolarDate solar = lunar.toSolar();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: N/A (primitive fields) - 空值安全: 不适用（原始类型字段）</li>
 * </ul>
 *
 * @param year the lunar year | 农历年
 * @param month the lunar month (1-12) | 农历月
 * @param day the lunar day | 农历日
 * @param isLeapMonth whether leap month | 是否闰月
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record LunarDate(int year, int month, int day, boolean isLeapMonth) {

    /**
     * Month names | 月份名
     */
    private static final String[] MONTH_NAMES = {
        "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    /**
     * Day prefixes | 日期前缀
     */
    private static final String[] DAY_PREFIXES = {"初", "十", "廿", "卅"};

    /**
     * Day numbers | 日期数字
     */
    private static final String[] DAY_NUMBERS = {
        "", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"
    };

    /**
     * Create lunar date without leap month
     * 创建非闰月农历日期
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     */
    public LunarDate(int year, int month, int day) {
        this(year, month, day, false);
    }

    /**
     * Get zodiac
     * 获取生肖
     *
     * @return the zodiac | 生肖
     */
    public Zodiac getZodiac() {
        return Zodiac.of(year);
    }

    /**
     * Get year GanZhi
     * 获取年干支
     *
     * @return the GanZhi | 干支
     */
    public GanZhi getYearGanZhi() {
        return GanZhi.ofYear(year);
    }

    /**
     * Get month name
     * 获取月份名
     *
     * @return the month name | 月份名
     */
    public String getMonthName() {
        return (isLeapMonth ? "闰" : "") + MONTH_NAMES[month - 1] + "月";
    }

    /**
     * Get day name
     * 获取日期名
     *
     * @return the day name | 日期名
     */
    public String getDayName() {
        if (day < 1 || day > 30) {
            throw new IllegalStateException("Invalid lunar day: " + day);
        }
        if (day == 10) {
            return "初十";
        }
        if (day == 20) {
            return "二十";
        }
        if (day == 30) {
            return "三十";
        }

        int prefix = (day - 1) / 10;
        int number = day % 10;
        if (number == 0) {
            number = 10;
        }

        return DAY_PREFIXES[prefix] + DAY_NUMBERS[number];
    }

    /**
     * Convert to solar date
     * 转换为公历日期
     *
     * @return the solar date | 公历日期
     */
    public SolarDate toSolar() {
        return LunarCalculator.lunarToSolar(this);
    }

    /**
     * Get festivals
     * 获取节日
     *
     * @return list of festivals | 节日列表
     */
    public List<Festival> getFestivals() {
        return Festival.getLunarFestivals(month, day);
    }

    /**
     * Check if festival day
     * 是否为节日
     *
     * @return true if festival | 如果是节日返回true
     */
    public boolean isFestival() {
        return !getFestivals().isEmpty();
    }

    /**
     * Format as Chinese date
     * 格式化为中文日期
     *
     * @return the formatted string | 格式化字符串
     */
    public String format() {
        return getYearGanZhi() + "年 " + getMonthName() + getDayName();
    }

    /**
     * Format as simple string
     * 格式化为简单字符串
     *
     * @return the formatted string | 格式化字符串
     */
    public String formatSimple() {
        return String.format("%d年%s%s", year, getMonthName(), getDayName());
    }

    @Override
    public String toString() {
        return format();
    }
}
