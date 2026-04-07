package cloud.opencode.base.geo.wkt;

/**
 * WKT Geometry Type Enumeration
 * WKT 几何类型枚举
 *
 * <p>Defines the basic geometry types supported by the WKT codec.</p>
 * <p>定义 WKT 编解码器支持的基本几何类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>POINT - single coordinate point | 单坐标点</li>
 *   <li>LINESTRING - ordered sequence of points | 有序点序列</li>
 *   <li>POLYGON - closed area with optional holes | 封闭区域（可含孔洞）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * WktType type = WktType.POINT;
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
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public enum WktType {

    /**
     * Point geometry type
     * 点几何类型
     */
    POINT,

    /**
     * LineString geometry type
     * 线段几何类型
     */
    LINESTRING,

    /**
     * Polygon geometry type
     * 多边形几何类型
     */
    POLYGON
}
