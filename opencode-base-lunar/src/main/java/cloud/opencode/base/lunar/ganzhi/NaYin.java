package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.element.WuXing;

/**
 * NaYin (纳音五行) - Sixty JiaZi NaYin Lookup
 * 纳音五行 - 六十甲子纳音查询
 *
 * <p>NaYin (纳音) is a traditional Chinese system that assigns one of 30 named
 * element types to each pair of consecutive GanZhi in the 60-cycle.
 * Each NaYin is associated with one of the Five Elements (WuXing).</p>
 * <p>纳音是中国传统命理体系，将六十甲子中每两个连续干支分配一种纳音名称，
 * 每种纳音对应一种五行属性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>30 NaYin definitions for the 60-cycle - 30种纳音定义</li>
 *   <li>WuXing (Five Element) association - 五行属性关联</li>
 *   <li>Lookup by GanZhi cycle index - 按干支索引查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NaYin naYin = NaYin.HAI_ZHONG_JIN;
 * String name = naYin.getName();          // "海中金"
 * WuXing element = naYin.getWuXing();     // WuXing.METAL
 *
 * // Lookup from GanZhi
 * NaYin naYin2 = NaYin.of(0);            // 甲子→海中金
 * NaYin naYin3 = NaYin.of(1);            // 乙丑→海中金 (same pair)
 *
 * // From GanZhi record
 * GanZhi gz = GanZhi.ofYear(2024);
 * NaYin yearNaYin = gz.getNaYin();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>NaYin Table | 纳音表:</strong></p>
 * <pre>
 * 甲子乙丑 海中金    丙寅丁卯 炉中火    戊辰己巳 大林木
 * 庚午辛未 路旁土    壬申癸酉 剑锋金    甲戌乙亥 山头火
 * 丙子丁丑 涧下水    戊寅己卯 城头土    庚辰辛巳 白蜡金
 * 壬午癸未 杨柳木    甲申乙酉 泉中水    丙戌丁亥 屋上土
 * 戊子己丑 霹雳火    庚寅辛卯 松柏木    壬辰癸巳 长流水
 * 甲午乙未 沙中金    丙申丁酉 山下火    戊戌己亥 平地木
 * 庚子辛丑 壁上土    壬寅癸卯 金箔金    甲辰乙巳 覆灯火
 * 丙午丁未 天河水    戊申己酉 大驿土    庚戌辛亥 钗钏金
 * 壬子癸丑 桑柘木    甲寅乙卯 大溪水    丙辰丁巳 沙中土
 * 戊午己未 天上火    庚申辛酉 石榴木    壬戌癸亥 大海水
 * </pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
public enum NaYin {

    /**
     * 海中金 (Gold in Sea) - 甲子、乙丑 | Metal
     */
    HAI_ZHONG_JIN("海中金", WuXing.METAL),

    /**
     * 炉中火 (Fire in Furnace) - 丙寅、丁卯 | Fire
     */
    LU_ZHONG_HUO("炉中火", WuXing.FIRE),

    /**
     * 大林木 (Wood of Great Forest) - 戊辰、己巳 | Wood
     */
    DA_LIN_MU("大林木", WuXing.WOOD),

    /**
     * 路旁土 (Earth by Roadside) - 庚午、辛未 | Earth
     */
    LU_PANG_TU("路旁土", WuXing.EARTH),

    /**
     * 剑锋金 (Gold of Sword Edge) - 壬申、癸酉 | Metal
     */
    JIAN_FENG_JIN("剑锋金", WuXing.METAL),

    /**
     * 山头火 (Fire on Mountain Top) - 甲戌、乙亥 | Fire
     */
    SHAN_TOU_HUO("山头火", WuXing.FIRE),

    /**
     * 涧下水 (Water under Stream) - 丙子、丁丑 | Water
     */
    JIAN_XIA_SHUI("涧下水", WuXing.WATER),

    /**
     * 城头土 (Earth on City Wall) - 戊寅、己卯 | Earth
     */
    CHENG_TOU_TU("城头土", WuXing.EARTH),

    /**
     * 白蜡金 (White Wax Gold) - 庚辰、辛巳 | Metal
     */
    BAI_LA_JIN("白蜡金", WuXing.METAL),

    /**
     * 杨柳木 (Willow Wood) - 壬午、癸未 | Wood
     */
    YANG_LIU_MU("杨柳木", WuXing.WOOD),

    /**
     * 泉中水 (Water in Spring) - 甲申、乙酉 | Water
     */
    QUAN_ZHONG_SHUI("泉中水", WuXing.WATER),

    /**
     * 屋上土 (Earth on Roof) - 丙戌、丁亥 | Earth
     */
    WU_SHANG_TU("屋上土", WuXing.EARTH),

    /**
     * 霹雳火 (Thunderbolt Fire) - 戊子、己丑 | Fire
     */
    PI_LI_HUO("霹雳火", WuXing.FIRE),

    /**
     * 松柏木 (Pine and Cypress Wood) - 庚寅、辛卯 | Wood
     */
    SONG_BAI_MU("松柏木", WuXing.WOOD),

    /**
     * 长流水 (Long-flowing Water) - 壬辰、癸巳 | Water
     */
    CHANG_LIU_SHUI("长流水", WuXing.WATER),

    /**
     * 沙中金 (Gold in Sand) - 甲午、乙未 | Metal
     */
    SHA_ZHONG_JIN("沙中金", WuXing.METAL),

    /**
     * 山下火 (Fire under Mountain) - 丙申、丁酉 | Fire
     */
    SHAN_XIA_HUO("山下火", WuXing.FIRE),

    /**
     * 平地木 (Flatland Wood) - 戊戌、己亥 | Wood
     */
    PING_DI_MU("平地木", WuXing.WOOD),

    /**
     * 壁上土 (Earth on Wall) - 庚子、辛丑 | Earth
     */
    BI_SHANG_TU("壁上土", WuXing.EARTH),

    /**
     * 金箔金 (Gold Foil) - 壬寅、癸卯 | Metal
     */
    JIN_BO_JIN("金箔金", WuXing.METAL),

    /**
     * 覆灯火 (Lamp Fire) - 甲辰、乙巳 | Fire
     */
    FU_DENG_HUO("覆灯火", WuXing.FIRE),

    /**
     * 天河水 (Water of Milky Way) - 丙午、丁未 | Water
     */
    TIAN_HE_SHUI("天河水", WuXing.WATER),

    /**
     * 大驿土 (Earth of Great Post Road) - 戊申、己酉 | Earth
     */
    DA_YI_TU("大驿土", WuXing.EARTH),

    /**
     * 钗钏金 (Hairpin Gold) - 庚戌、辛亥 | Metal
     */
    CHAI_CHUAN_JIN("钗钏金", WuXing.METAL),

    /**
     * 桑柘木 (Mulberry Wood) - 壬子、癸丑 | Wood
     */
    SANG_ZHE_MU("桑柘木", WuXing.WOOD),

    /**
     * 大溪水 (Water of Great Stream) - 甲寅、乙卯 | Water
     */
    DA_XI_SHUI("大溪水", WuXing.WATER),

    /**
     * 沙中土 (Earth in Sand) - 丙辰、丁巳 | Earth
     */
    SHA_ZHONG_TU("沙中土", WuXing.EARTH),

    /**
     * 天上火 (Heavenly Fire) - 戊午、己未 | Fire
     */
    TIAN_SHANG_HUO("天上火", WuXing.FIRE),

    /**
     * 石榴木 (Pomegranate Wood) - 庚申、辛酉 | Wood
     */
    SHI_LIU_MU("石榴木", WuXing.WOOD),

    /**
     * 大海水 (Water of Great Sea) - 壬戌、癸亥 | Water
     */
    DA_HAI_SHUI("大海水", WuXing.WATER);

    private final String name;
    private final WuXing wuXing;

    NaYin(String name, WuXing wuXing) {
        this.name = name;
        this.wuXing = wuXing;
    }

    /**
     * Get Chinese name
     * 获取中文名称
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * String name = NaYin.HAI_ZHONG_JIN.getName();  // "海中金"
     * }</pre>
     *
     * @return the Chinese name | 中文名称
     */
    public String getName() {
        return name;
    }

    /**
     * Get associated WuXing (Five Element)
     * 获取关联的五行属性
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * WuXing element = NaYin.HAI_ZHONG_JIN.getWuXing();  // WuXing.METAL
     * }</pre>
     *
     * @return the WuXing element | 五行属性
     */
    public WuXing getWuXing() {
        return wuXing;
    }

    /**
     * Get NaYin by 60-cycle index
     * 按六十甲子索引获取纳音
     *
     * <p>Each pair of consecutive indexes (0-1, 2-3, ..., 58-59) maps to the same NaYin.
     * The index is normalized to [0, 60) via {@link Math#floorMod(int, int)}.</p>
     * <p>每两个连续索引（0-1, 2-3, ..., 58-59）对应同一纳音。
     * 索引通过 {@link Math#floorMod(int, int)} 标准化到 [0, 60) 范围。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * NaYin n0 = NaYin.of(0);   // 甲子 → 海中金
     * NaYin n1 = NaYin.of(1);   // 乙丑 → 海中金 (same)
     * NaYin n2 = NaYin.of(2);   // 丙寅 → 炉中火
     * }</pre>
     *
     * @param cycleIndex the 60-cycle index | 六十甲子索引
     * @return the NaYin | 纳音
     */
    public static NaYin of(int cycleIndex) {
        int normalized = Math.floorMod(cycleIndex, 60);
        return values()[normalized / 2];
    }

    @Override
    public String toString() {
        return name;
    }
}
