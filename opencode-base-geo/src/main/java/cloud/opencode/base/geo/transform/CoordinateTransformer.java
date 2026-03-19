package cloud.opencode.base.geo.transform;

import cloud.opencode.base.geo.Coordinate;

/**
 * Coordinate Transformer Interface
 * 坐标转换器接口
 *
 * <p>Interface for transforming coordinates between coordinate systems.</p>
 * <p>用于在坐标系之间转换坐标的接口。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>{@link WGS84ToGCJ02Transformer} - WGS84 to GCJ02 | WGS84转GCJ02</li>
 *   <li>{@link GCJ02ToBD09Transformer} - GCJ02 to BD09 | GCJ02转BD09</li>
 *   <li>{@link BD09ToWGS84Transformer} - BD09 to WGS84 | BD09转WGS84</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Coordinate system transformation interface - 坐标系统转换接口</li>
 *   <li>Multi-system support - 多系统支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CoordinateTransformer transformer = new WGS84ToGCJ02Transformer();
 * Coordinate result = transformer.transform(coordinate);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@FunctionalInterface
public interface CoordinateTransformer {

    /**
     * Transform a coordinate
     * 转换坐标
     *
     * @param source the source coordinate | 源坐标
     * @return transformed coordinate | 转换后的坐标
     */
    Coordinate transform(Coordinate source);
}
