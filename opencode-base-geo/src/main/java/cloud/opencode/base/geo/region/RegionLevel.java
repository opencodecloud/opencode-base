package cloud.opencode.base.geo.region;

/**
 * Region Level
 * 区域级别
 *
 * <p>Enumeration of administrative region levels in China.</p>
 * <p>中国行政区域级别枚举。</p>
 *
 * <p><strong>Administrative Hierarchy | 行政层级:</strong></p>
 * <ul>
 *   <li>COUNTRY - 国家级 (e.g., China)</li>
 *   <li>PROVINCE - 省/直辖市/自治区 (e.g., Beijing, Shanghai)</li>
 *   <li>CITY - 地级市/地区 (e.g., Haidian District)</li>
 *   <li>DISTRICT - 区/县 (e.g., Zhongguancun)</li>
 *   <li>STREET - 街道/乡镇 (e.g., Zhongguancun Street)</li>
 *   <li>COMMUNITY - 社区/村 (e.g., Zhongguancun Community)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RegionLevel level = RegionLevel.PROVINCE;
 * int code = level.getCode();  // 1
 * String desc = level.getDescription();  // "省/直辖市/自治区"
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Region hierarchy level enumeration - 区域层级枚举</li>
 *   <li>Province, city, district levels - 省、市、区级别</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public enum RegionLevel {

    /**
     * Country level
     * 国家级
     */
    COUNTRY(0, "国家"),

    /**
     * Province/Municipality/Autonomous Region level
     * 省/直辖市/自治区级
     */
    PROVINCE(1, "省/直辖市/自治区"),

    /**
     * City/Prefecture level
     * 地级市/地区级
     */
    CITY(2, "地级市/地区"),

    /**
     * District/County level
     * 区/县级
     */
    DISTRICT(3, "区/县"),

    /**
     * Street/Township level
     * 街道/乡镇级
     */
    STREET(4, "街道/乡镇"),

    /**
     * Community/Village level
     * 社区/村级
     */
    COMMUNITY(5, "社区/村");

    private final int code;
    private final String description;

    RegionLevel(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the level code
     * 获取级别代码
     *
     * @return level code | 级别代码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the level description
     * 获取级别描述
     *
     * @return level description | 级别描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get RegionLevel by code
     * 根据代码获取区域级别
     *
     * @param code the level code | 级别代码
     * @return RegionLevel or null if not found | 区域级别，未找到返回null
     */
    public static RegionLevel fromCode(int code) {
        for (RegionLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return null;
    }

    /**
     * Check if this level is higher than another
     * 检查此级别是否高于另一个级别
     *
     * @param other the other level | 另一个级别
     * @return true if this level is higher | 如果此级别更高返回true
     */
    public boolean isHigherThan(RegionLevel other) {
        return this.code < other.code;
    }

    /**
     * Check if this level is lower than another
     * 检查此级别是否低于另一个级别
     *
     * @param other the other level | 另一个级别
     * @return true if this level is lower | 如果此级别更低返回true
     */
    public boolean isLowerThan(RegionLevel other) {
        return this.code > other.code;
    }
}
