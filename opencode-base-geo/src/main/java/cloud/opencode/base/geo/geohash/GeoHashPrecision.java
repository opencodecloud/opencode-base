package cloud.opencode.base.geo.geohash;

/**
 * GeoHash Precision Levels - Human-readable precision enum
 * GeoHash精度级别 - 可读性强的精度枚举
 *
 * <p>Provides named precision levels for GeoHash encoding with approximate
 * cell dimensions in kilometers, making it easy to choose the right precision
 * for a given use case.</p>
 * <p>为GeoHash编码提供命名精度级别，附带以公里为单位的近似单元格尺寸，
 * 便于根据使用场景选择合适的精度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named precision levels from continent to door - 从大洲到门级的命名精度级别</li>
 *   <li>Approximate cell width/height in km - 以公里为单位的近似单元格宽度/高度</li>
 *   <li>Bilingual descriptions - 双语描述</li>
 *   <li>Auto-select precision for search radius - 根据搜索半径自动选择精度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get precision for city-level search
 * GeoHashPrecision precision = GeoHashPrecision.CITY;
 * int value = precision.getValue(); // 5
 *
 * // Auto-select precision for 2km radius search
 * GeoHashPrecision best = GeoHashPrecision.forRadius(2.0);
 * // returns NEIGHBORHOOD (precision 6, ~1.2km width)
 *
 * // Use with GeoHashUtil
 * String hash = GeoHashUtil.encode(39.9042, 116.4074, precision.getValue());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see GeoHashUtil
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public enum GeoHashPrecision {

    /**
     * Continental area (~5000km x ~5000km)
     * 大洲级别 (~5000km x ~5000km)
     */
    CONTINENT(1, 5000.0, 5000.0, "Continental area", "大洲级别"),

    /**
     * Country area (~1250km x ~625km)
     * 国家级别 (~1250km x ~625km)
     */
    COUNTRY(2, 1250.0, 625.0, "Country area", "国家级别"),

    /**
     * Large region/state (~156km x ~156km)
     * 大区域/省级 (~156km x ~156km)
     */
    LARGE_REGION(3, 156.0, 156.0, "Large region/state", "大区域/省级"),

    /**
     * Region/city (~39km x ~19.5km)
     * 区域/城市级 (~39km x ~19.5km)
     */
    REGION(4, 39.0, 19.5, "Region/city", "区域/城市级"),

    /**
     * City/district (~4.9km x ~4.9km)
     * 城市/区级 (~4.9km x ~4.9km)
     */
    CITY(5, 4.9, 4.9, "City/district", "城市/区级"),

    /**
     * Neighborhood (~1.2km x ~0.61km)
     * 街道/社区级 (~1.2km x ~0.61km)
     */
    NEIGHBORHOOD(6, 1.2, 0.61, "Neighborhood", "街道/社区级"),

    /**
     * Street (~153m x ~153m)
     * 街道级 (~153m x ~153m)
     */
    STREET(7, 0.153, 0.153, "Street", "街道级"),

    /**
     * Building (~38m x ~19m)
     * 建筑级 (~38m x ~19m)
     */
    BUILDING(8, 0.038, 0.019, "Building", "建筑级"),

    /**
     * Door/entrance (~4.8m x ~4.8m)
     * 门级 (~4.8m x ~4.8m)
     */
    DOOR(9, 0.0048, 0.0048, "Door/entrance", "门级");

    /**
     * Array-indexed lookup for O(1) fromValue (index 0 unused, indices 1-9)
     * 基于数组索引的 O(1) fromValue 查找（索引0未使用，索引1-9）
     */
    private static final GeoHashPrecision[] BY_VALUE = new GeoHashPrecision[10];

    private final int value;
    private final double widthKm;
    private final double heightKm;
    private final String description;
    private final String descriptionZh;

    GeoHashPrecision(int value, double widthKm, double heightKm,
                     String description, String descriptionZh) {
        this.value = value;
        this.widthKm = widthKm;
        this.heightKm = heightKm;
        this.description = description;
        this.descriptionZh = descriptionZh;
    }

    static {
        for (GeoHashPrecision p : values()) {
            BY_VALUE[p.value] = p;
        }
    }

    /**
     * Get the GeoHash precision value (1-9)
     * 获取GeoHash精度值 (1-9)
     *
     * @return precision value | 精度值
     */
    public int getValue() {
        return value;
    }

    /**
     * Get approximate cell width in kilometers
     * 获取近似单元格宽度（公里）
     *
     * @return width in km | 宽度（公里）
     */
    public double getWidthKm() {
        return widthKm;
    }

    /**
     * Get approximate cell height in kilometers
     * 获取近似单元格高度（公里）
     *
     * @return height in km | 高度（公里）
     */
    public double getHeightKm() {
        return heightKm;
    }

    /**
     * Get English description
     * 获取英文描述
     *
     * @return English description | 英文描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get Chinese description
     * 获取中文描述
     *
     * @return Chinese description | 中文描述
     */
    public String getDescriptionZh() {
        return descriptionZh;
    }

    /**
     * Get GeoHashPrecision from integer value
     * 根据整数值获取GeoHashPrecision
     *
     * @param value the precision value (1-9) | 精度值 (1-9)
     * @return the corresponding GeoHashPrecision | 对应的GeoHashPrecision
     * @throws IllegalArgumentException if value is not between 1 and 9 | 当值不在1-9之间时抛出
     */
    public static GeoHashPrecision fromValue(int value) {
        if (value >= 1 && value <= 9) {
            return BY_VALUE[value];
        }
        throw new IllegalArgumentException(
                "GeoHash precision must be between 1 and 9, got: " + value);
    }

    /**
     * Get the best precision for a given search radius in kilometers
     * 根据搜索半径（公里）获取最佳精度
     *
     * <p>Returns the finest (highest) precision whose cell width is at least
     * as large as the given radius. This ensures that at most a 3x3 grid
     * of cells is needed to cover the search area.</p>
     * <p>返回单元格宽度至少与给定半径一样大的最细（最高）精度。
     * 这确保最多需要3x3的单元格网格来覆盖搜索区域。</p>
     *
     * @param radiusKm the search radius in kilometers | 搜索半径（公里）
     * @return the best precision for the radius | 该半径的最佳精度
     * @throws IllegalArgumentException if radiusKm is not positive | 当radiusKm不为正数时抛出
     */
    public static GeoHashPrecision forRadius(double radiusKm) {
        if (radiusKm <= 0 || Double.isNaN(radiusKm) || Double.isInfinite(radiusKm)) {
            throw new IllegalArgumentException(
                    "Search radius must be a positive finite number, got: " + radiusKm);
        }

        // Find the finest precision whose cell width still covers the search diameter.
        // We want cell width >= radiusKm so that a 3x3 grid of cells covers the circle.
        // Iterate from finest to coarsest, return the first whose width >= radiusKm.
        GeoHashPrecision[] levels = values();
        for (int i = levels.length - 1; i >= 0; i--) {
            if (levels[i].widthKm >= radiusKm) {
                return levels[i];
            }
        }
        // If radius exceeds all levels, return the coarsest
        return CONTINENT;
    }
}
