package cloud.opencode.base.date.lunar;

import java.time.LocalDate;
import java.time.Month;

/**
 * Solar Terms (二十四节气) enumeration
 * 二十四节气枚举
 *
 * <p>The 24 solar terms are a calendar of 24 periods used in traditional
 * East Asian calendars to govern such activities as farming.</p>
 * <p>二十四节气是传统东亚历法中用于指导农业等活动的24个时期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>24 solar terms enumeration - 二十四节气枚举</li>
 *   <li>Chinese and English names - 中英文名称</li>
 *   <li>Solar longitude values - 太阳黄经值</li>
 *   <li>Seasonal grouping - 季节分组</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SolarTerm term = SolarTerm.LICHUN;
 * String name = term.getChineseName();  // "立春"
 * int index = term.getIndex();  // 0
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is inherently thread-safe) - 线程安全: 是（枚举本身是线程安全的）</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public enum SolarTerm {

    // Spring 春季
    LICHUN(0, "立春", "Beginning of Spring", 315),
    YUSHUI(1, "雨水", "Rain Water", 330),
    JINGZHE(2, "惊蛰", "Awakening of Insects", 345),
    CHUNFEN(3, "春分", "Spring Equinox", 0),
    QINGMING(4, "清明", "Clear and Bright", 15),
    GUYU(5, "谷雨", "Grain Rain", 30),

    // Summer 夏季
    LIXIA(6, "立夏", "Beginning of Summer", 45),
    XIAOMAN(7, "小满", "Grain Buds", 60),
    MANGZHONG(8, "芒种", "Grain in Ear", 75),
    XIAZHI(9, "夏至", "Summer Solstice", 90),
    XIAOSHU(10, "小暑", "Minor Heat", 105),
    DASHU(11, "大暑", "Major Heat", 120),

    // Autumn 秋季
    LIQIU(12, "立秋", "Beginning of Autumn", 135),
    CHUSHU(13, "处暑", "End of Heat", 150),
    BAILU(14, "白露", "White Dew", 165),
    QIUFEN(15, "秋分", "Autumn Equinox", 180),
    HANLU(16, "寒露", "Cold Dew", 195),
    SHUANGJIANG(17, "霜降", "Frost's Descent", 210),

    // Winter 冬季
    LIDONG(18, "立冬", "Beginning of Winter", 225),
    XIAOXUE(19, "小雪", "Minor Snow", 240),
    DAXUE(20, "大雪", "Major Snow", 255),
    DONGZHI(21, "冬至", "Winter Solstice", 270),
    XIAOHAN(22, "小寒", "Minor Cold", 285),
    DAHAN(23, "大寒", "Major Cold", 300);

    private final int index;
    private final String chineseName;
    private final String englishName;
    private final double sunLongitude;

    private static final SolarTerm[] TERMS = values();

    SolarTerm(int index, String chineseName, String englishName, double sunLongitude) {
        this.index = index;
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.sunLongitude = sunLongitude;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Gets the solar term by index (0-23)
     * 按索引获取节气（0-23）
     *
     * @param index the index | 索引
     * @return the solar term | 节气
     */
    public static SolarTerm of(int index) {
        if (index < 0 || index > 23) {
            throw new IllegalArgumentException("Index must be between 0 and 23");
        }
        return TERMS[index];
    }

    /**
     * Gets the solar term by Chinese name
     * 按中文名称获取节气
     *
     * @param chineseName the Chinese name | 中文名称
     * @return the solar term, or null if not found | 节气，如果未找到则为null
     */
    public static SolarTerm fromChineseName(String chineseName) {
        for (SolarTerm term : TERMS) {
            if (term.chineseName.equals(chineseName)) {
                return term;
            }
        }
        return null;
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the index (0-23)
     * 获取索引（0-23）
     *
     * @return the index | 索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the Chinese name
     * 获取中文名称
     *
     * @return the Chinese name | 中文名称
     */
    public String getChineseName() {
        return chineseName;
    }

    /**
     * Gets the English name
     * 获取英文名称
     *
     * @return the English name | 英文名称
     */
    public String getEnglishName() {
        return englishName;
    }

    /**
     * Gets the sun longitude in degrees
     * 获取太阳黄经度数
     *
     * @return the sun longitude | 太阳黄经
     */
    public double getSunLongitude() {
        return sunLongitude;
    }

    // ==================== Navigation Methods ====================

    /**
     * Gets the next solar term
     * 获取下一个节气
     *
     * @return the next solar term | 下一个节气
     */
    public SolarTerm next() {
        return TERMS[(index + 1) % 24];
    }

    /**
     * Gets the previous solar term
     * 获取上一个节气
     *
     * @return the previous solar term | 上一个节气
     */
    public SolarTerm previous() {
        return TERMS[(index + 23) % 24];
    }

    // ==================== Type Check Methods ====================

    /**
     * Checks if this is a major solar term (中气)
     * 检查是否为中气
     *
     * @return true if major term | 如果是中气返回true
     */
    public boolean isMajorTerm() {
        return index % 2 == 1;
    }

    /**
     * Checks if this is a minor solar term (节气)
     * 检查是否为节气（小节气）
     *
     * @return true if minor term | 如果是节气返回true
     */
    public boolean isMinorTerm() {
        return index % 2 == 0;
    }

    /**
     * Gets the season of this solar term
     * 获取此节气所属季节
     *
     * @return the season (0=Spring, 1=Summer, 2=Autumn, 3=Winter) | 季节
     */
    public int getSeason() {
        return index / 6;
    }

    /**
     * Gets the season name in Chinese
     * 获取季节的中文名称
     *
     * @return the season name | 季节名称
     */
    public String getSeasonName() {
        return switch (getSeason()) {
            case 0 -> "春";
            case 1 -> "夏";
            case 2 -> "秋";
            case 3 -> "冬";
            default -> "";
        };
    }

    @Override
    public String toString() {
        return chineseName + " (" + englishName + ")";
    }
}
