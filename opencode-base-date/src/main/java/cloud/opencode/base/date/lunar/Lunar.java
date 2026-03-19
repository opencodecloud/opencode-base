package cloud.opencode.base.date.lunar;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Lunar (Chinese Calendar) date representation
 * 农历（中国传统历法）日期表示
 *
 * <p>This class represents a date in the Chinese lunar calendar, including
 * year, month, day, and leap month information.</p>
 * <p>此类表示中国农历中的日期，包括年、月、日和闰月信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lunar date representation - 农历日期表示</li>
 *   <li>Chinese zodiac support - 生肖支持</li>
 *   <li>Heavenly stems and earthly branches - 天干地支</li>
 *   <li>Chinese number formatting - 中文数字格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Lunar lunar = Lunar.of(2024, 1, 1, false);
 * String zodiac = lunar.getZodiac();  // "龙"
 * String chinese = lunar.toChinese(); // "二〇二四年正月初一"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class Lunar implements Comparable<Lunar>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Chinese numerals
    private static final String[] CHINESE_NUMBERS = {
            "〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"
    };

    private static final String[] CHINESE_MONTHS = {
            "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    private static final String[] CHINESE_DAYS_PREFIX = {
            "初", "十", "廿", "卅"
    };

    private static final String[] ZODIAC_ANIMALS = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    private static final String[] HEAVENLY_STEMS = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    private static final String[] EARTHLY_BRANCHES = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    private final int year;
    private final int month;
    private final int day;
    private final boolean leapMonth;

    // ==================== Constructors ====================

    private Lunar(int year, int month, int day, boolean leapMonth) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.leapMonth = leapMonth;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a Lunar date
     * 创建农历日期
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月（1-12）
     * @param day the lunar day (1-30) | 农历日（1-30）
     * @param leapMonth whether this is a leap month | 是否为闰月
     * @return the Lunar date | 农历日期
     */
    public static Lunar of(int year, int month, int day, boolean leapMonth) {
        validateLunarDate(year, month, day);
        return new Lunar(year, month, day, leapMonth);
    }

    /**
     * Creates a Lunar date (non-leap month)
     * 创建农历日期（非闰月）
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月（1-12）
     * @param day the lunar day (1-30) | 农历日（1-30）
     * @return the Lunar date | 农历日期
     */
    public static Lunar of(int year, int month, int day) {
        return of(year, month, day, false);
    }

    private static void validateLunarDate(int year, int month, int day) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 1900 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (day < 1 || day > 30) {
            throw new IllegalArgumentException("Day must be between 1 and 30");
        }
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the lunar year
     * 获取农历年
     *
     * @return the year | 年
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the lunar month
     * 获取农历月
     *
     * @return the month (1-12) | 月（1-12）
     */
    public int getMonth() {
        return month;
    }

    /**
     * Gets the lunar day
     * 获取农历日
     *
     * @return the day (1-30) | 日（1-30）
     */
    public int getDay() {
        return day;
    }

    /**
     * Checks if this is a leap month
     * 检查是否为闰月
     *
     * @return true if leap month | 如果是闰月返回true
     */
    public boolean isLeapMonth() {
        return leapMonth;
    }

    // ==================== Chinese Calendar Methods ====================

    /**
     * Gets the Chinese zodiac animal
     * 获取生肖
     *
     * @return the zodiac animal | 生肖
     */
    public String getZodiac() {
        return ZODIAC_ANIMALS[(year - 4) % 12];
    }

    /**
     * Gets the heavenly stem (天干)
     * 获取天干
     *
     * @return the heavenly stem | 天干
     */
    public String getHeavenlyStem() {
        return HEAVENLY_STEMS[(year - 4) % 10];
    }

    /**
     * Gets the earthly branch (地支)
     * 获取地支
     *
     * @return the earthly branch | 地支
     */
    public String getEarthlyBranch() {
        return EARTHLY_BRANCHES[(year - 4) % 12];
    }

    /**
     * Gets the stem-branch year name (干支纪年)
     * 获取干支纪年
     *
     * @return the stem-branch name | 干支名称
     */
    public String getStemBranchYear() {
        return getHeavenlyStem() + getEarthlyBranch();
    }

    /**
     * Gets the full year representation in Chinese
     * 获取完整的中文年份表示
     *
     * @return the Chinese year | 中文年份
     */
    public String getChineseYear() {
        StringBuilder sb = new StringBuilder();
        String yearStr = String.valueOf(year);
        for (char c : yearStr.toCharArray()) {
            sb.append(CHINESE_NUMBERS[c - '0']);
        }
        return sb.toString();
    }

    /**
     * Gets the month name in Chinese
     * 获取中文月份名称
     *
     * @return the Chinese month | 中文月份
     */
    public String getChineseMonth() {
        return (leapMonth ? "闰" : "") + CHINESE_MONTHS[month - 1] + "月";
    }

    /**
     * Gets the day name in Chinese
     * 获取中文日期名称
     *
     * @return the Chinese day | 中文日期
     */
    public String getChineseDay() {
        if (day == 10) {
            return "初十";
        } else if (day == 20) {
            return "二十";
        } else if (day == 30) {
            return "三十";
        }

        int tens = day / 10;
        int ones = day % 10;
        return CHINESE_DAYS_PREFIX[tens] + (ones == 0 ? "十" : CHINESE_NUMBERS[ones]);
    }

    /**
     * Converts to Chinese representation
     * 转换为中文表示
     *
     * @return the Chinese date string | 中文日期字符串
     */
    public String toChinese() {
        return getChineseYear() + "年" + getChineseMonth() + getChineseDay();
    }

    /**
     * Converts to stem-branch representation
     * 转换为干支表示
     *
     * @return the stem-branch date string | 干支日期字符串
     */
    public String toStemBranch() {
        return getStemBranchYear() + "年 " + getChineseMonth() + getChineseDay();
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Lunar other) {
        int cmp = Integer.compare(year, other.year);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(month, other.month);
        if (cmp != 0) return cmp;

        cmp = Boolean.compare(leapMonth, other.leapMonth);
        if (cmp != 0) return cmp;

        return Integer.compare(day, other.day);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Lunar other) {
            return year == other.year &&
                    month == other.month &&
                    day == other.day &&
                    leapMonth == other.leapMonth;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, leapMonth);
    }

    @Override
    public String toString() {
        return String.format("Lunar[%d-%02d-%02d%s]",
                year, month, day, leapMonth ? " (leap)" : "");
    }
}
