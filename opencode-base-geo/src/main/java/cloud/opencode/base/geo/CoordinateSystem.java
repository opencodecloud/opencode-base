package cloud.opencode.base.geo;

/**
 * Coordinate System Enumeration
 * 坐标系枚举
 *
 * <p>Defines the supported coordinate systems for geographic operations.</p>
 * <p>定义地理操作支持的坐标系。</p>
 *
 * <p><strong>Supported Systems | 支持的坐标系:</strong></p>
 * <ul>
 *   <li>WGS84 - GPS standard (International) | GPS标准（国际）</li>
 *   <li>GCJ02 - China coordinate system (Mars coordinates) | 中国坐标系（火星坐标）</li>
 *   <li>BD09 - Baidu coordinate system | 百度坐标系</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CoordinateSystem system = CoordinateSystem.WGS84;
 * Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
 * Coordinate gcj02 = coord.to(CoordinateSystem.GCJ02);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WGS84 GPS standard support - WGS84 GPS标准支持</li>
 *   <li>GCJ02 China coordinate system support - GCJ02中国坐标系支持</li>
 *   <li>BD09 Baidu coordinate system support - BD09百度坐标系支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public enum CoordinateSystem {

    /**
     * GPS coordinate system (International standard)
     * GPS坐标系（国际标准）
     */
    WGS84("WGS84", "World Geodetic System 1984"),

    /**
     * China National Bureau of Surveying coordinate system (Mars coordinates)
     * 中国国测局坐标系（火星坐标系）
     */
    GCJ02("GCJ02", "China Geodetic Coordinate System 2002"),

    /**
     * Baidu coordinate system
     * 百度坐标系
     */
    BD09("BD09", "Baidu Coordinate System 09");

    private final String code;
    private final String description;

    CoordinateSystem(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get the code
     * 获取代码
     *
     * @return the code | 代码
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the description
     * 获取描述
     *
     * @return the description | 描述
     */
    public String getDescription() {
        return description;
    }
}
