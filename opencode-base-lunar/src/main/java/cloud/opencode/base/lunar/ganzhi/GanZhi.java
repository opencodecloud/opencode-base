package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.calendar.SolarTerm;

import java.time.LocalDate;
import java.util.Objects;

/**
 * GanZhi (Heavenly Stems and Earthly Branches)
 * 干支
 *
 * <p>Combination of Heavenly Stem (天干) and Earthly Branch (地支).</p>
 * <p>天干和地支的组合。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Year, month, and day GanZhi calculation - 年、月、日干支计算</li>
 *   <li>60-year cycle support - 六十甲子循环支持</li>
 *   <li>Chinese name formatting - 中文名称格式化</li>
 *   <li>NaYin (纳音五行) lookup - 纳音五行查询</li>
 *   <li>Solar term-based month GanZhi (立春 as year boundary) - 基于节气的月干支（以立春为年界）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GanZhi yearGZ = GanZhi.ofYear(2024);    // 甲辰
 * GanZhi monthGZ = GanZhi.ofMonth(2024, 1);
 * GanZhi monthGZ2 = GanZhi.ofMonth(LocalDate.of(2024, 2, 10)); // auto solar term
 * String name = yearGZ.toString();         // 甲辰
 * NaYin naYin = yearGZ.getNaYin();         // 纳音五行
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (gan and zhi must not be null) - 空值安全: 否（天干和地支不能为null）</li>
 * </ul>
 *
 * <p><strong>60-Year Cycle | 六十甲子:</strong></p>
 * <pre>
 * 甲子 乙丑 丙寅 丁卯 戊辰 己巳 庚午 辛未 壬申 癸酉
 * 甲戌 乙亥 丙子 丁丑 戊寅 己卯 庚辰 辛巳 壬午 癸未
 * 甲申 乙酉 丙戌 丁亥 戊子 己丑 庚寅 辛卯 壬辰 癸巳
 * 甲午 乙未 丙申 丁酉 戊戌 己亥 庚子 辛丑 壬寅 癸卯
 * 甲辰 乙巳 丙午 丁未 戊申 己酉 庚戌 辛亥 壬子 癸丑
 * 甲寅 乙卯 丙辰 丁巳 戊午 己未 庚申 辛酉 壬戌 癸亥
 * </pre>
 *
 * @param gan the Heavenly Stem | 天干
 * @param zhi the Earthly Branch | 地支
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record GanZhi(Gan gan, Zhi zhi) {

    public GanZhi {
        Objects.requireNonNull(gan, "gan must not be null");
        Objects.requireNonNull(zhi, "zhi must not be null");
    }

    /**
     * Get GanZhi for year
     * 获取年干支
     *
     * @param year the year | 年份
     * @return the GanZhi | 干支
     */
    public static GanZhi ofYear(int year) {
        int ganIndex = Math.floorMod(year - 4, 10);
        int zhiIndex = Math.floorMod(year - 4, 12);
        return new GanZhi(Gan.values()[ganIndex], Zhi.values()[zhiIndex]);
    }

    /**
     * Get GanZhi for month by year and month number
     * 按年份和月份编号获取月干支
     *
     * <p>The month parameter represents the GanZhi month (1-12), where month 1 is 寅月
     * (starts at 立春). The year parameter is the GanZhi year (switches at 立春, not Jan 1).</p>
     * <p>月份参数为干支月（1-12），其中1月为寅月（始于立春）。
     * 年份参数为干支年（以立春为界，非公历1月1日）。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * // 2024年正月（寅月）的月干支
     * GanZhi gz = GanZhi.ofMonth(2024, 1);  // 丙寅
     * }</pre>
     *
     * @param year the GanZhi year (switches at 立春) | 干支年（以立春为界）
     * @param month the GanZhi month (1=寅月, 2=卯月, ..., 12=丑月) | 干支月（1=寅月, 2=卯月, ..., 12=丑月）
     * @return the GanZhi | 干支
     */
    public static GanZhi ofMonth(int year, int month) {
        int yearGanIndex = Math.floorMod(year - 4, 10);
        // 月干 = (年干 * 2 + 月份) % 10
        int monthGanIndex = Math.floorMod(yearGanIndex * 2 + month, 10);
        // 月支固定: 正月=寅(2), 二月=卯(3), ...
        int monthZhiIndex = (month + 1) % 12;
        return new GanZhi(Gan.values()[monthGanIndex], Zhi.values()[monthZhiIndex]);
    }

    /**
     * Get GanZhi for month by solar date, using solar term boundaries
     * 按公历日期获取月干支（以节气为月界）
     *
     * <p>Determines the GanZhi month automatically based on which solar term period
     * the date falls in. 立春 marks the start of month 1 (寅月) and also the start
     * of the new GanZhi year for month stem calculation.</p>
     * <p>根据日期所在的节气区间自动判定干支月。立春为寅月（正月）起点，
     * 也是月干计算中干支年的起点。</p>
     *
     * <p><strong>Solar term month boundaries | 节气月界:</strong></p>
     * <pre>
     * 立春→惊蛰 = 寅月(1)    惊蛰→清明 = 卯月(2)
     * 清明→立夏 = 辰月(3)    立夏→芒种 = 巳月(4)
     * 芒种→小暑 = 午月(5)    小暑→立秋 = 未月(6)
     * 立秋→白露 = 申月(7)    白露→寒露 = 酉月(8)
     * 寒露→立冬 = 戌月(9)    立冬→大雪 = 亥月(10)
     * 大雪→小寒 = 子月(11)   小寒→立春 = 丑月(12)
     * </pre>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * // 2024-02-10 is after 立春(2024-02-04), in 寅月
     * GanZhi gz = GanZhi.ofMonth(LocalDate.of(2024, 2, 10));
     *
     * // 2024-01-15 is before 立春, still in previous year's 丑月
     * GanZhi gz2 = GanZhi.ofMonth(LocalDate.of(2024, 1, 15));
     * }</pre>
     *
     * @param date the solar date | 公历日期
     * @return the month GanZhi | 月干支
     * @throws NullPointerException if date is null | 如果日期为null
     */
    public static GanZhi ofMonth(LocalDate date) {
        int solarYear = date.getYear();

        // Get 立春 date of this solar year
        LocalDate liChun = SolarTerm.LI_CHUN.getDate(solarYear);

        // Determine the GanZhi year: if before 立春, use previous year
        int ganZhiYear = date.isBefore(liChun) ? solarYear - 1 : solarYear;

        // Determine which GanZhi month the date falls in
        // Walk through the 12 "jie" (节) solar terms to find the current month
        int ganZhiMonth = determineGanZhiMonth(date, solarYear);

        return ofMonth(ganZhiYear, ganZhiMonth);
    }

    /**
     * Determine the GanZhi month number (1-12) for the given solar date
     * 确定给定公历日期的干支月编号（1-12）
     *
     * @param date the solar date | 公历日期
     * @param solarYear the solar year | 公历年份
     * @return the GanZhi month (1-12) | 干支月（1-12）
     */
    private static int determineGanZhiMonth(LocalDate date, int solarYear) {
        // The 12 jie solar terms that start each month, in chronological order within a year:
        // 小寒(0)→丑月12, 立春(2)→寅月1, 惊蛰(4)→卯月2, 清明(6)→辰月3,
        // 立夏(8)→巳月4, 芒种(10)→午月5, 小暑(12)→未月6, 立秋(14)→申月7,
        // 白露(16)→酉月8, 寒露(18)→戌月9, 立冬(20)→亥月10, 大雪(22)→子月11

        // Chronological order of jie terms within a solar year
        int[] termIndexes = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
        // Corresponding GanZhi month numbers
        int[] monthNumbers = {12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

        SolarTerm[] allTerms = SolarTerm.values();

        // Find the last jie term on or before the date
        int resultMonth = 11; // default: 子月(11) from 大雪 of previous year
        for (int i = termIndexes.length - 1; i >= 0; i--) {
            LocalDate termDate = allTerms[termIndexes[i]].getDate(solarYear);
            if (!date.isBefore(termDate)) {
                resultMonth = monthNumbers[i];
                break;
            }
        }

        return resultMonth;
    }

    /**
     * Get GanZhi for day
     * 获取日干支
     *
     * @param date the date | 日期
     * @return the GanZhi | 干支
     */
    public static GanZhi ofDay(LocalDate date) {
        // 儒略日算法
        long days = date.toEpochDay() + 25567 + 10;
        int ganIndex = (int) Math.floorMod(days, 10);
        int zhiIndex = (int) Math.floorMod(days, 12);
        return new GanZhi(Gan.values()[ganIndex], Zhi.values()[zhiIndex]);
    }

    /**
     * Get GanZhi for hour
     * 获取时干支
     *
     * @param dayGanZhi the day GanZhi | 日干支
     * @param hour the hour (0-23) | 小时（0-23）
     * @return the GanZhi | 干支
     */
    public static GanZhi ofHour(GanZhi dayGanZhi, int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hour must be between 0 and 23, got: " + hour);
        }
        // 时支
        Zhi hourZhi = Zhi.ofHour(hour);
        // 时干 = (日干 * 2 + 时支序号) % 10
        int hourGanIndex = (dayGanZhi.gan().ordinal() * 2 + hourZhi.ordinal()) % 10;
        return new GanZhi(Gan.values()[hourGanIndex], hourZhi);
    }

    /**
     * Get index in 60-year cycle (0-59)
     * 获取在六十甲子中的索引（0-59）
     *
     * @return the index | 索引
     */
    public int getCycleIndex() {
        int ganIndex = gan.ordinal();
        int zhiIndex = zhi.ordinal();
        // 公式：(天干序号 - 地支序号) * 6 % 60 + 地支序号
        return Math.floorMod((ganIndex - zhiIndex) * 6, 60) + zhiIndex;
    }

    /**
     * Get GanZhi by 60-year cycle index
     * 按六十甲子索引获取干支
     *
     * @param index the index (0-59) | 索引（0-59）
     * @return the GanZhi | 干支
     */
    public static GanZhi ofCycleIndex(int index) {
        int i = Math.floorMod(index, 60);
        return new GanZhi(Gan.of(i), Zhi.of(i));
    }

    /**
     * Get NaYin (纳音五行) for this GanZhi
     * 获取此干支的纳音五行
     *
     * <p>Each pair of consecutive GanZhi in the 60-cycle shares the same NaYin.
     * For example, 甲子 and 乙丑 are both 海中金.</p>
     * <p>六十甲子中每两个连续干支共享同一纳音。
     * 例如，甲子和乙丑都是海中金。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * GanZhi jiaZi = GanZhi.ofCycleIndex(0);
     * NaYin naYin = jiaZi.getNaYin();  // NaYin.HAI_ZHONG_JIN (海中金)
     * }</pre>
     *
     * @return the NaYin | 纳音
     */
    public NaYin getNaYin() {
        return NaYin.of(getCycleIndex());
    }

    /**
     * Get next GanZhi in cycle
     * 获取下一个干支
     *
     * @return the next GanZhi | 下一个干支
     */
    public GanZhi next() {
        return new GanZhi(gan.next(), zhi.next());
    }

    /**
     * Get previous GanZhi in cycle
     * 获取上一个干支
     *
     * @return the previous GanZhi | 上一个干支
     */
    public GanZhi previous() {
        return new GanZhi(gan.previous(), zhi.previous());
    }

    /**
     * Get combined Chinese name
     * 获取组合中文名
     *
     * @return the combined name | 组合名
     */
    public String getName() {
        return gan.getName() + zhi.getName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
