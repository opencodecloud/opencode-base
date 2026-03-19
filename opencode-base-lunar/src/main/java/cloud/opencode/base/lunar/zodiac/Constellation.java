package cloud.opencode.base.lunar.zodiac;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Constellation (Western Zodiac)
 * 星座枚举
 *
 * <p>The twelve Western zodiac constellations.</p>
 * <p>十二星座。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Twelve constellation definitions - 十二星座定义</li>
 *   <li>Date range mapping - 日期范围映射</li>
 *   <li>Symbol and bilingual names - 符号和双语名称</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Constellation pisces = Constellation.PISCES;
 * String symbol = pisces.getSymbol();  // ...
 * Constellation c = Constellation.of(3, 15);  // PISCES
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum Constellation {

    /**
     * Capricorn | 摩羯座 (12/22 - 1/19)
     */
    CAPRICORN("摩羯座", "Capricorn", "♑", MonthDay.of(12, 22), MonthDay.of(1, 19)),

    /**
     * Aquarius | 水瓶座 (1/20 - 2/18)
     */
    AQUARIUS("水瓶座", "Aquarius", "♒", MonthDay.of(1, 20), MonthDay.of(2, 18)),

    /**
     * Pisces | 双鱼座 (2/19 - 3/20)
     */
    PISCES("双鱼座", "Pisces", "♓", MonthDay.of(2, 19), MonthDay.of(3, 20)),

    /**
     * Aries | 白羊座 (3/21 - 4/19)
     */
    ARIES("白羊座", "Aries", "♈", MonthDay.of(3, 21), MonthDay.of(4, 19)),

    /**
     * Taurus | 金牛座 (4/20 - 5/20)
     */
    TAURUS("金牛座", "Taurus", "♉", MonthDay.of(4, 20), MonthDay.of(5, 20)),

    /**
     * Gemini | 双子座 (5/21 - 6/21)
     */
    GEMINI("双子座", "Gemini", "♊", MonthDay.of(5, 21), MonthDay.of(6, 21)),

    /**
     * Cancer | 巨蟹座 (6/22 - 7/22)
     */
    CANCER("巨蟹座", "Cancer", "♋", MonthDay.of(6, 22), MonthDay.of(7, 22)),

    /**
     * Leo | 狮子座 (7/23 - 8/22)
     */
    LEO("狮子座", "Leo", "♌", MonthDay.of(7, 23), MonthDay.of(8, 22)),

    /**
     * Virgo | 处女座 (8/23 - 9/22)
     */
    VIRGO("处女座", "Virgo", "♍", MonthDay.of(8, 23), MonthDay.of(9, 22)),

    /**
     * Libra | 天秤座 (9/23 - 10/23)
     */
    LIBRA("天秤座", "Libra", "♎", MonthDay.of(9, 23), MonthDay.of(10, 23)),

    /**
     * Scorpio | 天蝎座 (10/24 - 11/22)
     */
    SCORPIO("天蝎座", "Scorpio", "♏", MonthDay.of(10, 24), MonthDay.of(11, 22)),

    /**
     * Sagittarius | 射手座 (11/23 - 12/21)
     */
    SAGITTARIUS("射手座", "Sagittarius", "♐", MonthDay.of(11, 23), MonthDay.of(12, 21));

    private final String name;
    private final String englishName;
    private final String symbol;
    private final MonthDay startDate;
    private final MonthDay endDate;

    Constellation(String name, String englishName, String symbol, MonthDay startDate, MonthDay endDate) {
        this.name = name;
        this.englishName = englishName;
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Get Chinese name
     * 获取中文名
     *
     * @return the Chinese name | 中文名
     */
    public String getName() {
        return name;
    }

    /**
     * Get English name
     * 获取英文名
     *
     * @return the English name | 英文名
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * Get symbol
     * 获取符号
     *
     * @return the symbol | 符号
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Get start date
     * 获取起始日期
     *
     * @return the start date | 起始日期
     */
    public MonthDay getStartDate() {
        return startDate;
    }

    /**
     * Get end date
     * 获取结束日期
     *
     * @return the end date | 结束日期
     */
    public MonthDay getEndDate() {
        return endDate;
    }

    /**
     * Get constellation by date
     * 按日期获取星座
     *
     * @param date the date | 日期
     * @return the constellation | 星座
     */
    public static Constellation of(LocalDate date) {
        return of(date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Get constellation by month and day
     * 按月日获取星座
     *
     * @param month the month | 月
     * @param day the day | 日
     * @return the constellation | 星座
     */
    public static Constellation of(int month, int day) {
        MonthDay md = MonthDay.of(month, day);

        // Check each constellation
        for (Constellation c : values()) {
            if (c.contains(md)) {
                return c;
            }
        }

        // Default to Capricorn (shouldn't happen)
        return CAPRICORN;
    }

    /**
     * Check if date is in this constellation period
     * 检查日期是否在该星座周期内
     *
     * @param md the month-day | 月日
     * @return true if in period | 如果在周期内返回true
     */
    public boolean contains(MonthDay md) {
        // Handle Capricorn (crosses year boundary)
        if (this == CAPRICORN) {
            return md.compareTo(startDate) >= 0 || md.compareTo(endDate) <= 0;
        }

        return md.compareTo(startDate) >= 0 && md.compareTo(endDate) <= 0;
    }

    /**
     * Check if date is this constellation
     * 检查日期是否为该星座
     *
     * @param date the date | 日期
     * @return true if matches | 如果匹配返回true
     */
    public boolean isDate(LocalDate date) {
        return of(date) == this;
    }

    @Override
    public String toString() {
        return name;
    }
}
