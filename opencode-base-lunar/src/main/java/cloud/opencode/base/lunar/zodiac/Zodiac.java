package cloud.opencode.base.lunar.zodiac;

import cloud.opencode.base.lunar.ganzhi.Zhi;

/**
 * Zodiac (Chinese Zodiac)
 * 生肖枚举
 *
 * <p>The twelve Chinese zodiac animals.</p>
 * <p>十二生肖。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Twelve zodiac animal definitions - 十二生肖定义</li>
 *   <li>Year-to-zodiac mapping - 年份到生肖映射</li>
 *   <li>Earthly Branch association - 地支关联</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Zodiac dragon = Zodiac.DRAGON;
 * String name = dragon.getChineseName();  // 龙
 * Zodiac z = Zodiac.ofYear(2024);         // DRAGON
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>Zodiac Animals | 十二生肖:</strong></p>
 * <pre>
 * 鼠 牛 虎 兔 龙 蛇 马 羊 猴 鸡 狗 猪
 * </pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum Zodiac {

    /**
     * Rat | 鼠
     */
    RAT("鼠", "Rat", "子"),

    /**
     * Ox | 牛
     */
    OX("牛", "Ox", "丑"),

    /**
     * Tiger | 虎
     */
    TIGER("虎", "Tiger", "寅"),

    /**
     * Rabbit | 兔
     */
    RABBIT("兔", "Rabbit", "卯"),

    /**
     * Dragon | 龙
     */
    DRAGON("龙", "Dragon", "辰"),

    /**
     * Snake | 蛇
     */
    SNAKE("蛇", "Snake", "巳"),

    /**
     * Horse | 马
     */
    HORSE("马", "Horse", "午"),

    /**
     * Goat | 羊
     */
    GOAT("羊", "Goat", "未"),

    /**
     * Monkey | 猴
     */
    MONKEY("猴", "Monkey", "申"),

    /**
     * Rooster | 鸡
     */
    ROOSTER("鸡", "Rooster", "酉"),

    /**
     * Dog | 狗
     */
    DOG("狗", "Dog", "戌"),

    /**
     * Pig | 猪
     */
    PIG("猪", "Pig", "亥");

    private final String name;
    private final String englishName;
    private final String earthlyBranch;

    Zodiac(String name, String englishName, String earthlyBranch) {
        this.name = name;
        this.englishName = englishName;
        this.earthlyBranch = earthlyBranch;
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
     * Get associated Earthly Branch
     * 获取关联的地支
     *
     * @return the earthly branch | 地支
     */
    public String getEarthlyBranch() {
        return earthlyBranch;
    }

    /**
     * Get associated Zhi
     * 获取关联的地支枚举
     *
     * @return the Zhi | 地支
     */
    public Zhi getZhi() {
        return Zhi.values()[ordinal()];
    }

    /**
     * Get zodiac by year
     * 按年份获取生肖
     *
     * @param year the year | 年份
     * @return the zodiac | 生肖
     */
    public static Zodiac of(int year) {
        return values()[Math.floorMod(year - 4, 12)];
    }

    /**
     * Get zodiac by index (0-11)
     * 按索引获取生肖（0-11）
     *
     * @param index the index | 索引
     * @return the zodiac | 生肖
     */
    public static Zodiac ofIndex(int index) {
        return values()[Math.floorMod(index, 12)];
    }

    /**
     * Get next zodiac year
     * 获取下一个生肖年
     *
     * @param fromYear the starting year | 起始年份
     * @return the next year with this zodiac | 下一个该生肖的年份
     */
    public int nextYear(int fromYear) {
        int currentZodiacIndex = (fromYear - 4) % 12;
        int targetIndex = ordinal();
        int diff = (targetIndex - currentZodiacIndex + 12) % 12;
        if (diff == 0) {
            diff = 12;
        }
        return fromYear + diff;
    }

    /**
     * Get previous zodiac year
     * 获取上一个生肖年
     *
     * @param fromYear the starting year | 起始年份
     * @return the previous year with this zodiac | 上一个该生肖的年份
     */
    public int previousYear(int fromYear) {
        int currentZodiacIndex = (fromYear - 4) % 12;
        int targetIndex = ordinal();
        int diff = (currentZodiacIndex - targetIndex + 12) % 12;
        if (diff == 0) {
            diff = 12;
        }
        return fromYear - diff;
    }

    /**
     * Check if year is this zodiac
     * 检查年份是否为该生肖
     *
     * @param year the year | 年份
     * @return true if matches | 如果匹配返回true
     */
    public boolean isYear(int year) {
        return of(year) == this;
    }

    @Override
    public String toString() {
        return name;
    }
}
