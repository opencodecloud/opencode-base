package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.calendar.Festival;
import cloud.opencode.base.lunar.ganzhi.GanZhi;
import cloud.opencode.base.lunar.internal.LunarCalculator;
import cloud.opencode.base.lunar.internal.LunarData;
import cloud.opencode.base.lunar.zodiac.Zodiac;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
 *   <li>Date arithmetic (plusDays, minusDays, daysUntil) - 日期算术（加天、减天、间隔天数）</li>
 *   <li>Comparable ordering via epoch day conversion - 基于纪元日的比较排序</li>
 *   <li>Static factory methods (of, from) - 静态工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LunarDate lunar = LunarDate.of(2024, 1, 1);
 * LunarDate leap = LunarDate.of(2020, 4, 15, true);
 * LunarDate fromSolar = LunarDate.from(LocalDate.of(2024, 2, 10));
 * System.out.println(lunar.format());  // 甲辰年 正月初一
 * Zodiac zodiac = lunar.getZodiac();   // DRAGON
 * SolarDate solar = lunar.toSolar();
 * LunarDate next = lunar.plusDays(30);
 * long diff = lunar.daysUntil(next);
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
public record LunarDate(int year, int month, int day, boolean isLeapMonth)
        implements Comparable<LunarDate> {

    /**
     * Compact constructor validation
     * 紧凑构造函数验证
     */
    public LunarDate {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Lunar month must be between 1 and 12, got: " + month);
        }
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Lunar day must be between 1 and 30, got: " + day);
        }
        if (LunarData.isSupported(year)) {
            int maxDays;
            if (isLeapMonth) {
                int leapMonth = LunarData.getLeapMonth(year);
                if (leapMonth != month) {
                    throw new IllegalArgumentException(
                            "Year " + year + " has no leap month " + month);
                }
                maxDays = LunarData.getLeapMonthDays(year);
            } else {
                maxDays = LunarData.getMonthDays(year, month);
            }
            if (day > maxDays) {
                throw new IllegalArgumentException(
                        "Lunar day must be between 1 and " + maxDays
                                + " for year " + year + " month " + month
                                + (isLeapMonth ? " (leap)" : "") + ", got: " + day);
            }
        }
    }

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
     * Create a non-leap-month lunar date
     * 创建非闰月农历日期
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月
     * @param day the lunar day | 农历日
     * @return the lunar date | 农历日期
     * @since V1.0.3
     */
    public static LunarDate of(int year, int month, int day) {
        return new LunarDate(year, month, day, false);
    }

    /**
     * Create a lunar date with explicit leap month flag
     * 创建指定闰月标志的农历日期
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月
     * @param day the lunar day | 农历日
     * @param isLeapMonth whether leap month | 是否闰月
     * @return the lunar date | 农历日期
     * @since V1.0.3
     */
    public static LunarDate of(int year, int month, int day, boolean isLeapMonth) {
        return new LunarDate(year, month, day, isLeapMonth);
    }

    /**
     * Convert a solar (Gregorian) date to a lunar date
     * 将公历日期转换为农历日期
     *
     * @param solarDate the solar date | 公历日期
     * @return the lunar date | 农历日期
     * @throws cloud.opencode.base.lunar.exception.DateOutOfRangeException if outside 1900-2100
     * @since V1.0.3
     */
    public static LunarDate from(LocalDate solarDate) {
        Objects.requireNonNull(solarDate, "solarDate must not be null");
        return LunarCalculator.solarToLunar(solarDate);
    }

    /**
     * Add days to this lunar date via solar conversion roundtrip
     * 通过公历转换往返来加天数
     *
     * @param days the number of days to add (may be negative) | 要加的天数（可为负）
     * @return a new lunar date | 新的农历日期
     * @since V1.0.3
     */
    public LunarDate plusDays(int days) {
        LocalDate solar = toSolar().toLocalDate();
        LocalDate result = solar.plusDays(days);
        return LunarCalculator.solarToLunar(result);
    }

    /**
     * Subtract days from this lunar date
     * 从此农历日期减去天数
     *
     * @param days the number of days to subtract (may be negative) | 要减的天数（可为负）
     * @return a new lunar date | 新的农历日期
     * @since V1.0.3
     */
    public LunarDate minusDays(int days) {
        LocalDate solar = toSolar().toLocalDate();
        LocalDate result = solar.minusDays(days);
        return LunarCalculator.solarToLunar(result);
    }

    /**
     * Calculate the number of days from this date to another lunar date
     * 计算从此日期到另一个农历日期的天数
     *
     * <p>Returns a positive value if {@code other} is after this date,
     * negative if before, and zero if the same day.</p>
     * <p>如果 {@code other} 在此日期之后返回正值，之前返回负值，同一天返回零。</p>
     *
     * @param other the other lunar date | 另一个农历日期
     * @return the number of days between | 间隔天数
     * @since V1.0.3
     */
    public long daysUntil(LunarDate other) {
        Objects.requireNonNull(other, "other must not be null");
        long thisEpoch = toSolar().toLocalDate().toEpochDay();
        long otherEpoch = other.toSolar().toLocalDate().toEpochDay();
        return otherEpoch - thisEpoch;
    }

    /**
     * Compare two lunar dates by converting to epoch day
     * 通过转换为纪元日来比较两个农历日期
     *
     * @param other the other lunar date | 另一个农历日期
     * @return negative, zero, or positive | 负值、零或正值
     * @since V1.0.3
     */
    @Override
    public int compareTo(LunarDate other) {
        Objects.requireNonNull(other, "other must not be null");
        long thisEpoch = toSolar().toLocalDate().toEpochDay();
        long otherEpoch = other.toSolar().toLocalDate().toEpochDay();
        return Long.compare(thisEpoch, otherEpoch);
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
     * Get month GanZhi
     * 获取月干支
     *
     * @return the month GanZhi | 月干支
     * @since V1.0.3
     */
    public GanZhi getMonthGanZhi() {
        return GanZhi.ofMonth(toSolar().toLocalDate());
    }

    /**
     * Get day GanZhi
     * 获取日干支
     *
     * @return the day GanZhi | 日干支
     * @since V1.0.3
     */
    public GanZhi getDayGanZhi() {
        return GanZhi.ofDay(toSolar().toLocalDate());
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
        // Festivals are only on regular months, not leap months
        if (isLeapMonth) {
            return List.of();
        }
        return Festival.getLunarFestivals(year, month, day);
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
