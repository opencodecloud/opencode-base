package cloud.opencode.base.lunar.calendar;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Festival
 * 节日
 *
 * <p>Traditional Chinese and solar festivals.</p>
 * <p>中国传统节日和公历节日。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar and lunar festival definitions - 公历和农历节日定义</li>
 *   <li>Festival type classification - 节日类型分类</li>
 *   <li>Date-based festival lookup - 基于日期的节日查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Festival springFest = Festival.SPRING_FESTIVAL;
 * String name = springFest.name();
 * FestivalType type = springFest.type();
 * List<Festival> festivals = Festival.getSolarFestivals(MonthDay.of(1, 1));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (fields must not be null) - 空值安全: 否（字段不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record Festival(
    String name,
    String englishName,
    FestivalType type,
    MonthDay date
) {

    /**
     * Festival type | 节日类型
     */
    public enum FestivalType {
        /**
         * Solar calendar festival | 公历节日
         */
        SOLAR,
        /**
         * Lunar calendar festival | 农历节日
         */
        LUNAR,
        /**
         * Solar term festival | 节气节日
         */
        SOLAR_TERM
    }

    // ============ Solar Festivals | 公历节日 ============

    /**
     * New Year's Day | 元旦
     */
    public static final Festival NEW_YEAR = new Festival("元旦", "New Year's Day", FestivalType.SOLAR, MonthDay.of(1, 1));

    /**
     * Valentine's Day | 情人节
     */
    public static final Festival VALENTINE = new Festival("情人节", "Valentine's Day", FestivalType.SOLAR, MonthDay.of(2, 14));

    /**
     * Women's Day | 妇女节
     */
    public static final Festival WOMEN_DAY = new Festival("妇女节", "Women's Day", FestivalType.SOLAR, MonthDay.of(3, 8));

    /**
     * Arbor Day | 植树节
     */
    public static final Festival ARBOR_DAY = new Festival("植树节", "Arbor Day", FestivalType.SOLAR, MonthDay.of(3, 12));

    /**
     * Labor Day | 劳动节
     */
    public static final Festival LABOR_DAY = new Festival("劳动节", "Labor Day", FestivalType.SOLAR, MonthDay.of(5, 1));

    /**
     * Youth Day | 青年节
     */
    public static final Festival YOUTH_DAY = new Festival("青年节", "Youth Day", FestivalType.SOLAR, MonthDay.of(5, 4));

    /**
     * Children's Day | 儿童节
     */
    public static final Festival CHILDREN_DAY = new Festival("儿童节", "Children's Day", FestivalType.SOLAR, MonthDay.of(6, 1));

    /**
     * Party Day | 建党节
     */
    public static final Festival PARTY_DAY = new Festival("建党节", "Party Day", FestivalType.SOLAR, MonthDay.of(7, 1));

    /**
     * Army Day | 建军节
     */
    public static final Festival ARMY_DAY = new Festival("建军节", "Army Day", FestivalType.SOLAR, MonthDay.of(8, 1));

    /**
     * Teachers' Day | 教师节
     */
    public static final Festival TEACHERS_DAY = new Festival("教师节", "Teachers' Day", FestivalType.SOLAR, MonthDay.of(9, 10));

    /**
     * National Day | 国庆节
     */
    public static final Festival NATIONAL_DAY = new Festival("国庆节", "National Day", FestivalType.SOLAR, MonthDay.of(10, 1));

    /**
     * Christmas | 圣诞节
     */
    public static final Festival CHRISTMAS = new Festival("圣诞节", "Christmas", FestivalType.SOLAR, MonthDay.of(12, 25));

    // ============ Lunar Festivals | 农历节日 ============

    /**
     * Spring Festival | 春节
     */
    public static final Festival SPRING_FESTIVAL = new Festival("春节", "Spring Festival", FestivalType.LUNAR, MonthDay.of(1, 1));

    /**
     * Lantern Festival | 元宵节
     */
    public static final Festival LANTERN_FESTIVAL = new Festival("元宵节", "Lantern Festival", FestivalType.LUNAR, MonthDay.of(1, 15));

    /**
     * Dragon Head Raising Day | 龙抬头
     */
    public static final Festival DRAGON_HEAD = new Festival("龙抬头", "Dragon Head Raising Day", FestivalType.LUNAR, MonthDay.of(2, 2));

    /**
     * Dragon Boat Festival | 端午节
     */
    public static final Festival DRAGON_BOAT = new Festival("端午节", "Dragon Boat Festival", FestivalType.LUNAR, MonthDay.of(5, 5));

    /**
     * Qixi Festival | 七夕节
     */
    public static final Festival QIXI = new Festival("七夕节", "Qixi Festival", FestivalType.LUNAR, MonthDay.of(7, 7));

    /**
     * Ghost Festival | 中元节
     */
    public static final Festival GHOST_FESTIVAL = new Festival("中元节", "Ghost Festival", FestivalType.LUNAR, MonthDay.of(7, 15));

    /**
     * Mid-Autumn Festival | 中秋节
     */
    public static final Festival MID_AUTUMN = new Festival("中秋节", "Mid-Autumn Festival", FestivalType.LUNAR, MonthDay.of(8, 15));

    /**
     * Double Ninth Festival | 重阳节
     */
    public static final Festival DOUBLE_NINTH = new Festival("重阳节", "Double Ninth Festival", FestivalType.LUNAR, MonthDay.of(9, 9));

    /**
     * Laba Festival | 腊八节
     */
    public static final Festival LABA = new Festival("腊八节", "Laba Festival", FestivalType.LUNAR, MonthDay.of(12, 8));

    /**
     * Little New Year | 小年
     */
    public static final Festival LITTLE_NEW_YEAR = new Festival("小年", "Little New Year", FestivalType.LUNAR, MonthDay.of(12, 23));

    /**
     * New Year's Eve | 除夕
     */
    public static final Festival NEW_YEARS_EVE = new Festival("除夕", "New Year's Eve", FestivalType.LUNAR, MonthDay.of(12, 30));

    /**
     * All solar festivals
     */
    private static final List<Festival> SOLAR_FESTIVALS = List.of(
        NEW_YEAR, VALENTINE, WOMEN_DAY, ARBOR_DAY, LABOR_DAY, YOUTH_DAY,
        CHILDREN_DAY, PARTY_DAY, ARMY_DAY, TEACHERS_DAY, NATIONAL_DAY, CHRISTMAS
    );

    /**
     * All lunar festivals
     */
    private static final List<Festival> LUNAR_FESTIVALS = List.of(
        SPRING_FESTIVAL, LANTERN_FESTIVAL, DRAGON_HEAD, DRAGON_BOAT,
        QIXI, GHOST_FESTIVAL, MID_AUTUMN, DOUBLE_NINTH, LABA, LITTLE_NEW_YEAR, NEW_YEARS_EVE
    );

    /**
     * Get solar festivals for date
     * 获取公历日期的节日
     *
     * @param date the date | 日期
     * @return list of festivals | 节日列表
     */
    public static List<Festival> getSolarFestivals(LocalDate date) {
        MonthDay md = MonthDay.from(date);
        List<Festival> result = new ArrayList<>();
        for (Festival f : SOLAR_FESTIVALS) {
            if (f.date().equals(md)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Get lunar festivals for lunar date
     * 获取农历日期的节日
     *
     * @param month the lunar month | 农历月
     * @param day the lunar day | 农历日
     * @return list of festivals | 节日列表
     */
    public static List<Festival> getLunarFestivals(int month, int day) {
        MonthDay md = MonthDay.of(month, day);
        List<Festival> result = new ArrayList<>();
        for (Festival f : LUNAR_FESTIVALS) {
            if (f.date().equals(md)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Get all solar festivals
     * 获取所有公历节日
     *
     * @return list of festivals | 节日列表
     */
    public static List<Festival> getAllSolarFestivals() {
        return SOLAR_FESTIVALS;
    }

    /**
     * Get all lunar festivals
     * 获取所有农历节日
     *
     * @return list of festivals | 节日列表
     */
    public static List<Festival> getAllLunarFestivals() {
        return LUNAR_FESTIVALS;
    }

    @Override
    public String toString() {
        return name;
    }
}
