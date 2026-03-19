package cloud.opencode.base.lunar.ganzhi;

import cloud.opencode.base.lunar.zodiac.Zodiac;

/**
 * Zhi (Earthly Branches)
 * 地支枚举
 *
 * <p>The twelve Earthly Branches (地支) in Chinese calendar system.</p>
 * <p>中国历法系统中的十二地支。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Twelve Earthly Branch definitions - 十二地支定义</li>
 *   <li>Zodiac animal and element associations - 生肖和五行关联</li>
 *   <li>Time period mapping - 时辰映射</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Zhi zi = Zhi.ZI;
 * String name = zi.getChineseName();  // 子
 * String element = zi.getElement();   // Water
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>Earthly Branches | 地支:</strong></p>
 * <pre>
 * 子 丑 寅 卯 辰 巳 午 未 申 酉 戌 亥
 * </pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum Zhi {

    /**
     * 子 (Rat) | 子（鼠）
     */
    ZI("子", "Water", 23, 1),

    /**
     * 丑 (Ox) | 丑（牛）
     */
    CHOU("丑", "Earth", 1, 3),

    /**
     * 寅 (Tiger) | 寅（虎）
     */
    YIN("寅", "Wood", 3, 5),

    /**
     * 卯 (Rabbit) | 卯（兔）
     */
    MAO("卯", "Wood", 5, 7),

    /**
     * 辰 (Dragon) | 辰（龙）
     */
    CHEN("辰", "Earth", 7, 9),

    /**
     * 巳 (Snake) | 巳（蛇）
     */
    SI("巳", "Fire", 9, 11),

    /**
     * 午 (Horse) | 午（马）
     */
    WU("午", "Fire", 11, 13),

    /**
     * 未 (Goat) | 未（羊）
     */
    WEI("未", "Earth", 13, 15),

    /**
     * 申 (Monkey) | 申（猴）
     */
    SHEN("申", "Metal", 15, 17),

    /**
     * 酉 (Rooster) | 酉（鸡）
     */
    YOU("酉", "Metal", 17, 19),

    /**
     * 戌 (Dog) | 戌（狗）
     */
    XU("戌", "Earth", 19, 21),

    /**
     * 亥 (Pig) | 亥（猪）
     */
    HAI("亥", "Water", 21, 23);

    private final String name;
    private final String element;
    private final int hourStart;
    private final int hourEnd;

    Zhi(String name, String element, int hourStart, int hourEnd) {
        this.name = name;
        this.element = element;
        this.hourStart = hourStart;
        this.hourEnd = hourEnd;
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
     * Get associated element (Five Elements)
     * 获取关联的五行元素
     *
     * @return the element | 五行元素
     */
    public String getElement() {
        return element;
    }

    /**
     * Get hour start (24-hour format)
     * 获取时辰起始小时（24小时制）
     *
     * @return the start hour | 起始小时
     */
    public int getHourStart() {
        return hourStart;
    }

    /**
     * Get hour end (24-hour format)
     * 获取时辰结束小时（24小时制）
     *
     * @return the end hour | 结束小时
     */
    public int getHourEnd() {
        return hourEnd;
    }

    /**
     * Get associated Zodiac
     * 获取关联的生肖
     *
     * @return the zodiac | 生肖
     */
    public Zodiac getZodiac() {
        return Zodiac.values()[ordinal()];
    }

    /**
     * Get Zhi by index (0-11)
     * 按索引获取地支（0-11）
     *
     * @param index the index | 索引
     * @return the Zhi | 地支
     */
    public static Zhi of(int index) {
        return values()[Math.floorMod(index, 12)];
    }

    /**
     * Get Zhi from year
     * 从年份获取地支
     *
     * @param year the year | 年份
     * @return the Zhi | 地支
     */
    public static Zhi ofYear(int year) {
        return of((year - 4) % 12);
    }

    /**
     * Get Zhi from hour (24-hour format)
     * 从小时获取地支（24小时制）
     *
     * @param hour the hour (0-23) | 小时（0-23）
     * @return the Zhi | 地支
     */
    public static Zhi ofHour(int hour) {
        int index = ((hour + 1) / 2) % 12;
        return values()[index];
    }

    /**
     * Get next Zhi
     * 获取下一个地支
     *
     * @return the next Zhi | 下一个地支
     */
    public Zhi next() {
        return of(ordinal() + 1);
    }

    /**
     * Get previous Zhi
     * 获取上一个地支
     *
     * @return the previous Zhi | 上一个地支
     */
    public Zhi previous() {
        return of(ordinal() - 1);
    }

    @Override
    public String toString() {
        return name;
    }
}
