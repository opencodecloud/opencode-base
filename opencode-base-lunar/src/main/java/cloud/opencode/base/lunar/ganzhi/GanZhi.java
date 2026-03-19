package cloud.opencode.base.lunar.ganzhi;

import java.time.LocalDate;

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
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GanZhi yearGZ = GanZhi.ofYear(2024);    // 甲辰
 * GanZhi monthGZ = GanZhi.ofMonth(2024, 1);
 * String name = yearGZ.toString();         // 甲辰
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

    /**
     * Get GanZhi for year
     * 获取年干支
     *
     * @param year the year | 年份
     * @return the GanZhi | 干支
     */
    public static GanZhi ofYear(int year) {
        int ganIndex = (year - 4) % 10;
        int zhiIndex = (year - 4) % 12;
        return new GanZhi(Gan.values()[ganIndex], Zhi.values()[zhiIndex]);
    }

    /**
     * Get GanZhi for month
     * 获取月干支
     *
     * @param year the year | 年份
     * @param month the month (1-12) | 月份（1-12）
     * @return the GanZhi | 干支
     */
    public static GanZhi ofMonth(int year, int month) {
        // 月干 = (年干 * 2 + 月) % 10
        int yearGanIndex = (year - 4) % 10;
        int monthGanIndex = (yearGanIndex * 2 + month) % 10;
        // 月支固定：正月寅开始
        int monthZhiIndex = (month + 1) % 12;
        return new GanZhi(Gan.values()[monthGanIndex], Zhi.values()[monthZhiIndex]);
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
