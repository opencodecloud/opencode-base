package cloud.opencode.base.lunar.calendar;

import cloud.opencode.base.lunar.exception.DateOutOfRangeException;
import cloud.opencode.base.lunar.internal.LunarData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;
import java.time.temporal.TemporalAdjusters;
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
     * Compact constructor with null validation.
     * 带空值验证的紧凑构造器。
     */
    public Festival {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(englishName, "englishName must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(date, "date must not be null");
    }

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

    /**
     * Earth Day | 地球日
     *
     * @since V1.0.3
     */
    public static final Festival EARTH_DAY = new Festival("地球日", "Earth Day", FestivalType.SOLAR, MonthDay.of(4, 22));

    /**
     * Halloween | 万圣节
     *
     * @since V1.0.3
     */
    public static final Festival HALLOWEEN = new Festival("万圣节", "Halloween", FestivalType.SOLAR, MonthDay.of(10, 31));

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
     *
     * <p>Note: This constant uses day 30 as a nominal value. In years where the 12th lunar month
     * has only 29 days, use {@link #getChuXi(int)} for accurate determination.</p>
     * <p>注意：此常量使用30日作为名义值。在腊月只有29天的年份，
     * 请使用 {@link #getChuXi(int)} 获取准确日期。</p>
     */
    public static final Festival NEW_YEARS_EVE = new Festival("除夕", "New Year's Eve", FestivalType.LUNAR, MonthDay.of(12, 30));

    /**
     * Double Third Festival | 上巳节（三月三）
     *
     * @since V1.0.3
     */
    public static final Festival SHANG_SI = new Festival("上巳节", "Double Third Festival", FestivalType.LUNAR, MonthDay.of(3, 3));

    /**
     * Lower Yuan Festival | 下元节（十月十五）
     *
     * @since V1.0.3
     */
    public static final Festival XIA_YUAN = new Festival("下元节", "Lower Yuan Festival", FestivalType.LUNAR, MonthDay.of(10, 15));

    /**
     * Winter Clothing Festival | 寒衣节（十月初一）
     *
     * @since V1.0.3
     */
    public static final Festival HAN_YI = new Festival("寒衣节", "Winter Clothing Festival", FestivalType.LUNAR, MonthDay.of(10, 1));

    /**
     * Mending of the Heavens | 天穿节（正月二十）
     *
     * @since V1.0.3
     */
    public static final Festival TIAN_CHUAN = new Festival("天穿节", "Mending of the Heavens", FestivalType.LUNAR, MonthDay.of(1, 20));

    /**
     * All solar festivals
     */
    private static final List<Festival> SOLAR_FESTIVALS = List.of(
        NEW_YEAR, VALENTINE, WOMEN_DAY, ARBOR_DAY, LABOR_DAY, YOUTH_DAY,
        CHILDREN_DAY, PARTY_DAY, ARMY_DAY, TEACHERS_DAY, NATIONAL_DAY, CHRISTMAS,
        EARTH_DAY, HALLOWEEN
    );

    /**
     * All lunar festivals
     */
    private static final List<Festival> LUNAR_FESTIVALS = List.of(
        SPRING_FESTIVAL, LANTERN_FESTIVAL, DRAGON_HEAD, DRAGON_BOAT,
        QIXI, GHOST_FESTIVAL, MID_AUTUMN, DOUBLE_NINTH, LABA, LITTLE_NEW_YEAR, NEW_YEARS_EVE,
        SHANG_SI, XIA_YUAN, HAN_YI, TIAN_CHUAN
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

    /**
     * Get lunar festivals for lunar date with year context for accurate 除夕 detection
     * 获取农历日期的节日（带年份上下文，用于准确检测除夕）
     *
     * <p>This overload handles the dynamic 除夕 case where the 12th lunar month
     * may have only 29 days. When month=12 and day=29, this method checks
     * whether the 12th month has 29 or 30 days to determine if 除夕 applies.</p>
     * <p>此重载处理动态除夕的情况：腊月可能只有29天。
     * 当month=12且day=29时，此方法检查腊月是29天还是30天来确定是否为除夕。</p>
     *
     * @param lunarYear the lunar year | 农历年
     * @param month     the lunar month | 农历月
     * @param day       the lunar day | 农历日
     * @return list of festivals | 节日列表
     * @since V1.0.3
     */
    public static List<Festival> getLunarFestivals(int lunarYear, int month, int day) {
        List<Festival> result = getLunarFestivals(month, day);
        int lastDay = (month == 12) ? LunarData.getMonthDays(lunarYear, 12) : 0;

        // Handle dynamic 除夕: if month=12, day=30, but the 12th month has only 29 days,
        // remove the spurious NEW_YEARS_EVE match from the 2-arg lookup
        if (month == 12 && day == 30 && lastDay == 29) {
            result = new ArrayList<>(result);
            result.removeIf(f -> f.equals(NEW_YEARS_EVE));
        }

        // Handle dynamic 除夕: if month=12, day=29, and the 12th month has only 29 days,
        // day 29 is actually the last day -> this is 除夕
        if (month == 12 && day == 29 && lastDay == 29) {
            result = new ArrayList<>(result);
            result.add(getChuXi(lunarYear));
        }

        return result;
    }

    /**
     * Get 除夕 (New Year's Eve) for a specific lunar year
     * 获取指定农历年的除夕
     *
     * <p>Calculates the actual last day of the 12th lunar month,
     * which may be the 29th or 30th depending on the year.</p>
     * <p>计算腊月的实际最后一天，根据年份可能是29日或30日。</p>
     *
     * @param lunarYear the lunar year | 农历年
     * @return the 除夕 festival with correct date | 正确日期的除夕节日
     * @since V1.0.3
     */
    public static Festival getChuXi(int lunarYear) {
        if (!LunarData.isSupported(lunarYear)) {
            throw new DateOutOfRangeException(lunarYear);
        }
        int lastDay = LunarData.getMonthDays(lunarYear, 12);
        return new Festival("除夕", "New Year's Eve", FestivalType.LUNAR, MonthDay.of(12, lastDay));
    }

    /**
     * Get Mother's Day date for a given year (second Sunday of May)
     * 获取指定年份的母亲节日期（五月第二个星期日）
     *
     * @param year the solar year | 公历年份
     * @return the date | 日期
     * @since V1.0.3
     */
    public static LocalDate getMothersDay(int year) {
        LocalDate firstDayOfMay = LocalDate.of(year, 5, 1);
        LocalDate firstSunday = firstDayOfMay.with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));
        return firstSunday.plusWeeks(1);
    }

    /**
     * Get Father's Day date for a given year (third Sunday of June)
     * 获取指定年份的父亲节日期（六月第三个星期日）
     *
     * @param year the solar year | 公历年份
     * @return the date | 日期
     * @since V1.0.3
     */
    public static LocalDate getFathersDay(int year) {
        LocalDate firstDayOfJune = LocalDate.of(year, 6, 1);
        LocalDate firstSunday = firstDayOfJune.with(TemporalAdjusters.firstInMonth(DayOfWeek.SUNDAY));
        return firstSunday.plusWeeks(2);
    }

    @Override
    public String toString() {
        return name;
    }
}
