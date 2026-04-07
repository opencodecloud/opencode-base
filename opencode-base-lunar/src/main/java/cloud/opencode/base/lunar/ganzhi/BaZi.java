package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.calendar.SolarTerm;
import cloud.opencode.base.lunar.element.WuXing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * BaZi (Eight Characters / Four Pillars of Destiny)
 * 八字（四柱命理）
 *
 * <p>Calculates the Four Pillars (四柱) of Chinese astrology from a date and hour.
 * Each pillar is a {@link GanZhi} pair of Heavenly Stem and Earthly Branch.</p>
 * <p>根据日期和时辰计算中国命理学的四柱。每一柱都是一个由天干和地支组成的
 * {@link GanZhi} 对。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Year pillar with 立春 boundary - 以立春为界的年柱计算</li>
 *   <li>Month pillar based on solar terms - 基于节气的月柱计算</li>
 *   <li>Day pillar from Julian day calculation - 基于儒略日的日柱计算</li>
 *   <li>Hour pillar from day stem and hour - 基于日干和时辰的时柱计算</li>
 *   <li>Day Master (日主) WuXing element - 日主五行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From date and hour
 * BaZi bazi = BaZi.of(LocalDate.of(2024, 2, 4), 14);
 * System.out.println(bazi.format());         // 甲辰 丙寅 壬戌 丁未
 * System.out.println(bazi.formatWithLabels()); // 年柱:甲辰 月柱:丙寅 日柱:壬戌 时柱:丁未
 *
 * // From LocalDateTime
 * BaZi bazi2 = BaZi.of(LocalDateTime.of(2024, 2, 4, 14, 30));
 *
 * // Day Master element
 * WuXing dayMaster = bazi.getDayMaster();    // WATER
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (pillars must not be null) - 空值安全: 否（四柱不能为null）</li>
 * </ul>
 *
 * <p><strong>Four Pillars | 四柱:</strong></p>
 * <pre>
 * 年柱 (Year)  - Determined by 立春 boundary, not January 1st
 * 月柱 (Month) - Determined by solar term boundaries
 * 日柱 (Day)   - Determined by Julian day calculation
 * 时柱 (Hour)  - Determined by day stem and Chinese hour (时辰)
 * </pre>
 *
 * @param yearPillar  the year pillar | 年柱
 * @param monthPillar the month pillar | 月柱
 * @param dayPillar   the day pillar | 日柱
 * @param hourPillar  the hour pillar | 时柱
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see GanZhi
 * @see WuXing
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
public record BaZi(
        GanZhi yearPillar,
        GanZhi monthPillar,
        GanZhi dayPillar,
        GanZhi hourPillar
) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     */
    public BaZi {
        Objects.requireNonNull(yearPillar, "yearPillar must not be null");
        Objects.requireNonNull(monthPillar, "monthPillar must not be null");
        Objects.requireNonNull(dayPillar, "dayPillar must not be null");
        Objects.requireNonNull(hourPillar, "hourPillar must not be null");
    }

    /**
     * Create BaZi from date and hour
     * 根据日期和时辰创建八字
     *
     * @param date the solar date | 公历日期
     * @param hour the hour (0-23) | 小时（0-23）
     * @return the BaZi | 八字
     * @throws NullPointerException     if date is null
     * @throws IllegalArgumentException if hour is out of range
     */
    public static BaZi of(LocalDate date, int hour) {
        Objects.requireNonNull(date, "date must not be null");
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be between 0 and 23, got: " + hour);
        }

        // Year pillar: GanZhi year starts at 立春 (Start of Spring)
        int year = date.getYear();
        LocalDate liChun = SolarTerm.LI_CHUN.getDate(year);
        int ganZhiYear = date.isBefore(liChun) ? year - 1 : year;
        GanZhi yearPillar = GanZhi.ofYear(ganZhiYear);

        // Month pillar: delegates to GanZhi.ofMonth(LocalDate) which handles
        // solar term boundaries and cross-year months correctly
        GanZhi monthPillar = GanZhi.ofMonth(date);

        // Day pillar: direct calculation
        GanZhi dayPillar = GanZhi.ofDay(date);

        // Hour pillar: based on day stem and hour
        GanZhi hourPillar = GanZhi.ofHour(dayPillar, hour);

        return new BaZi(yearPillar, monthPillar, dayPillar, hourPillar);
    }

    /**
     * Create BaZi from LocalDateTime
     * 根据 LocalDateTime 创建八字
     *
     * @param dateTime the date and time | 日期时间
     * @return the BaZi | 八字
     * @throws NullPointerException if dateTime is null
     */
    public static BaZi of(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return of(dateTime.toLocalDate(), dateTime.getHour());
    }

    /**
     * Get the Day Master (日主) element
     * 获取日主五行
     *
     * <p>The Day Master is the Heavenly Stem of the day pillar,
     * which represents the self in BaZi analysis.</p>
     * <p>日主是日柱的天干，在八字分析中代表自身。</p>
     *
     * @return the Day Master's WuXing element | 日主五行
     */
    public WuXing getDayMaster() {
        return WuXing.fromGan(dayPillar.gan().ordinal());
    }

    /**
     * Format BaZi as a string with four pillars separated by spaces
     * 格式化八字为空格分隔的四柱字符串
     *
     * @return formatted string like "甲辰 丙寅 壬戌 丁未" | 格式化字符串
     */
    public String format() {
        return yearPillar.getName() + " " + monthPillar.getName() + " "
                + dayPillar.getName() + " " + hourPillar.getName();
    }

    /**
     * Format BaZi with labels
     * 格式化带标签的八字
     *
     * @return formatted string like "年柱:甲辰 月柱:丙寅 日柱:壬戌 时柱:丁未" | 格式化字符串
     */
    public String formatWithLabels() {
        return "年柱:" + yearPillar.getName()
                + " 月柱:" + monthPillar.getName()
                + " 日柱:" + dayPillar.getName()
                + " 时柱:" + hourPillar.getName();
    }

    @Override
    public String toString() {
        return format();
    }
}
