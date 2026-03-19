package cloud.opencode.base.lunar.ganzhi;

/**
 * Gan (Heavenly Stems)
 * 天干枚举
 *
 * <p>The ten Heavenly Stems (天干) in Chinese calendar system.</p>
 * <p>中国历法系统中的十天干。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Ten Heavenly Stem definitions - 十天干定义</li>
 *   <li>Yin/Yang and element associations - 阴阳和五行关联</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Gan jia = Gan.JIA;
 * String name = jia.getChineseName();  // 甲
 * boolean isYang = jia.isYang();       // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>Heavenly Stems | 天干:</strong></p>
 * <pre>
 * 甲 乙 丙 丁 戊 己 庚 辛 壬 癸
 * </pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum Gan {

    /**
     * 甲 (Wood Yang) | 甲（阳木）
     */
    JIA("甲", "Wood", true),

    /**
     * 乙 (Wood Yin) | 乙（阴木）
     */
    YI("乙", "Wood", false),

    /**
     * 丙 (Fire Yang) | 丙（阳火）
     */
    BING("丙", "Fire", true),

    /**
     * 丁 (Fire Yin) | 丁（阴火）
     */
    DING("丁", "Fire", false),

    /**
     * 戊 (Earth Yang) | 戊（阳土）
     */
    WU("戊", "Earth", true),

    /**
     * 己 (Earth Yin) | 己（阴土）
     */
    JI("己", "Earth", false),

    /**
     * 庚 (Metal Yang) | 庚（阳金）
     */
    GENG("庚", "Metal", true),

    /**
     * 辛 (Metal Yin) | 辛（阴金）
     */
    XIN("辛", "Metal", false),

    /**
     * 壬 (Water Yang) | 壬（阳水）
     */
    REN("壬", "Water", true),

    /**
     * 癸 (Water Yin) | 癸（阴水）
     */
    GUI("癸", "Water", false);

    private final String name;
    private final String element;
    private final boolean yang;

    Gan(String name, String element, boolean yang) {
        this.name = name;
        this.element = element;
        this.yang = yang;
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
     * Check if Yang (阳)
     * 是否为阳
     *
     * @return true if Yang | 如果为阳返回true
     */
    public boolean isYang() {
        return yang;
    }

    /**
     * Check if Yin (阴)
     * 是否为阴
     *
     * @return true if Yin | 如果为阴返回true
     */
    public boolean isYin() {
        return !yang;
    }

    /**
     * Get Gan by index (0-9)
     * 按索引获取天干（0-9）
     *
     * @param index the index | 索引
     * @return the Gan | 天干
     */
    public static Gan of(int index) {
        return values()[Math.floorMod(index, 10)];
    }

    /**
     * Get Gan from year
     * 从年份获取天干
     *
     * @param year the year | 年份
     * @return the Gan | 天干
     */
    public static Gan ofYear(int year) {
        return of((year - 4) % 10);
    }

    /**
     * Get next Gan
     * 获取下一个天干
     *
     * @return the next Gan | 下一个天干
     */
    public Gan next() {
        return of(ordinal() + 1);
    }

    /**
     * Get previous Gan
     * 获取上一个天干
     *
     * @return the previous Gan | 上一个天干
     */
    public Gan previous() {
        return of(ordinal() - 1);
    }

    @Override
    public String toString() {
        return name;
    }
}
