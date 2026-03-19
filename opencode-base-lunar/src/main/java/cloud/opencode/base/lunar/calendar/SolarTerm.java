package cloud.opencode.base.lunar.calendar;

import cloud.opencode.base.lunar.internal.SolarTermData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Solar Term (24 Solar Terms)
 * 节气枚举
 *
 * <p>The 24 solar terms in Chinese calendar.</p>
 * <p>中国历法中的二十四节气。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>24 solar term definitions - 二十四节气定义</li>
 *   <li>Date calculation for each term - 每个节气的日期计算</li>
 *   <li>Bilingual names (Chinese and English) - 双语名称（中文和英文）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SolarTerm term = SolarTerm.LI_CHUN;
 * String name = term.getChineseName();  // 立春
 * LocalDate date = SolarTerm.getDate(2024, SolarTerm.LI_CHUN);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>24 Solar Terms | 二十四节气:</strong></p>
 * <pre>
 * 小寒 大寒 立春 雨水 惊蛰 春分 清明 谷雨 立夏 小满 芒种 夏至
 * 小暑 大暑 立秋 处暑 白露 秋分 寒露 霜降 立冬 小雪 大雪 冬至
 * </pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum SolarTerm {

    /**
     * Minor Cold | 小寒
     */
    XIAO_HAN("小寒", "Minor Cold", 1),

    /**
     * Major Cold | 大寒
     */
    DA_HAN("大寒", "Major Cold", 1),

    /**
     * Start of Spring | 立春
     */
    LI_CHUN("立春", "Start of Spring", 2),

    /**
     * Rain Water | 雨水
     */
    YU_SHUI("雨水", "Rain Water", 2),

    /**
     * Awakening of Insects | 惊蛰
     */
    JING_ZHE("惊蛰", "Awakening of Insects", 3),

    /**
     * Spring Equinox | 春分
     */
    CHUN_FEN("春分", "Spring Equinox", 3),

    /**
     * Clear and Bright | 清明
     */
    QING_MING("清明", "Clear and Bright", 4),

    /**
     * Grain Rain | 谷雨
     */
    GU_YU("谷雨", "Grain Rain", 4),

    /**
     * Start of Summer | 立夏
     */
    LI_XIA("立夏", "Start of Summer", 5),

    /**
     * Grain Buds | 小满
     */
    XIAO_MAN("小满", "Grain Buds", 5),

    /**
     * Grain in Ear | 芒种
     */
    MANG_ZHONG("芒种", "Grain in Ear", 6),

    /**
     * Summer Solstice | 夏至
     */
    XIA_ZHI("夏至", "Summer Solstice", 6),

    /**
     * Minor Heat | 小暑
     */
    XIAO_SHU("小暑", "Minor Heat", 7),

    /**
     * Major Heat | 大暑
     */
    DA_SHU("大暑", "Major Heat", 7),

    /**
     * Start of Autumn | 立秋
     */
    LI_QIU("立秋", "Start of Autumn", 8),

    /**
     * End of Heat | 处暑
     */
    CHU_SHU("处暑", "End of Heat", 8),

    /**
     * White Dew | 白露
     */
    BAI_LU("白露", "White Dew", 9),

    /**
     * Autumn Equinox | 秋分
     */
    QIU_FEN("秋分", "Autumn Equinox", 9),

    /**
     * Cold Dew | 寒露
     */
    HAN_LU("寒露", "Cold Dew", 10),

    /**
     * Frost Descent | 霜降
     */
    SHUANG_JIANG("霜降", "Frost Descent", 10),

    /**
     * Start of Winter | 立冬
     */
    LI_DONG("立冬", "Start of Winter", 11),

    /**
     * Minor Snow | 小雪
     */
    XIAO_XUE("小雪", "Minor Snow", 11),

    /**
     * Major Snow | 大雪
     */
    DA_XUE("大雪", "Major Snow", 12),

    /**
     * Winter Solstice | 冬至
     */
    DONG_ZHI("冬至", "Winter Solstice", 12);

    private final String name;
    private final String englishName;
    private final int typicalMonth;

    SolarTerm(String name, String englishName, int typicalMonth) {
        this.name = name;
        this.englishName = englishName;
        this.typicalMonth = typicalMonth;
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
     * Get typical month
     * 获取典型月份
     *
     * @return the typical month | 典型月份
     */
    public int getTypicalMonth() {
        return typicalMonth;
    }

    /**
     * Get date for this solar term in given year
     * 获取该节气在指定年份的日期
     *
     * @param year the year | 年份
     * @return the date | 日期
     */
    public LocalDate getDate(int year) {
        return SolarTermData.getDate(year, ordinal());
    }

    /**
     * Check if this is a major solar term (中气)
     * 检查是否为中气
     *
     * @return true if major | 如果是中气返回true
     */
    public boolean isMajor() {
        return ordinal() % 2 == 1;
    }

    /**
     * Check if this is a minor solar term (节气)
     * 检查是否为节气
     *
     * @return true if minor | 如果是节气返回true
     */
    public boolean isMinor() {
        return ordinal() % 2 == 0;
    }

    /**
     * Get solar term by date (returns null if not a solar term day)
     * 按日期获取节气（如果不是节气日返回null）
     *
     * @param date the date | 日期
     * @return the solar term or null | 节气或null
     */
    public static SolarTerm of(LocalDate date) {
        int year = date.getYear();
        for (SolarTerm term : values()) {
            if (term.getDate(year).equals(date)) {
                return term;
            }
        }
        return null;
    }

    /**
     * Get all solar terms for a year
     * 获取一年所有节气
     *
     * @param year the year | 年份
     * @return list of solar terms with dates | 节气及日期列表
     */
    public static List<SolarTerm> ofYear(int year) {
        List<SolarTerm> terms = new ArrayList<>(24);
        for (SolarTerm term : values()) {
            terms.add(term);
        }
        return terms;
    }

    /**
     * Get next solar term from date
     * 获取日期之后的下一个节气
     *
     * @param date the date | 日期
     * @return the next solar term | 下一个节气
     */
    public static SolarTerm next(LocalDate date) {
        int year = date.getYear();
        for (SolarTerm term : values()) {
            LocalDate termDate = term.getDate(year);
            if (termDate.isAfter(date)) {
                return term;
            }
        }
        // Return first term of next year
        return XIAO_HAN;
    }

    /**
     * Get previous solar term from date
     * 获取日期之前的上一个节气
     *
     * @param date the date | 日期
     * @return the previous solar term | 上一个节气
     */
    public static SolarTerm previous(LocalDate date) {
        int year = date.getYear();
        SolarTerm prev = DONG_ZHI;
        for (SolarTerm term : values()) {
            LocalDate termDate = term.getDate(year);
            if (!termDate.isBefore(date)) {
                return prev;
            }
            prev = term;
        }
        return DONG_ZHI;
    }

    @Override
    public String toString() {
        return name;
    }
}
