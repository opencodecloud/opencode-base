package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;
import cloud.opencode.base.geo.OpenGeo;

/**
 * Circle Fence
 * 圆形围栏
 *
 * <p>A circular geo-fence defined by a center point and radius.</p>
 * <p>由中心点和半径定义的圆形地理围栏。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Circular geofence definition - 圆形地理围栏定义</li>
 *   <li>Radius-based containment check - 基于半径的包含检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Coordinate center = Coordinate.wgs84(116.4074, 39.9042);
 * GeoFence fence = new CircleFence(center, 1000); // 1km radius
 * boolean inside = fence.contains(point);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param center the center point | 中心点
 * @param radius the radius in meters | 半径（米）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public record CircleFence(Coordinate center, double radius) implements GeoFence {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public CircleFence {
        if (center == null) {
            throw new IllegalArgumentException("Center cannot be null");
        }
        if (radius < 0) {
            throw new IllegalArgumentException("Radius cannot be negative");
        }
    }

    @Override
    public boolean contains(Coordinate point) {
        if (point == null) {
            return false;
        }
        return OpenGeo.distance(point, center) <= radius;
    }
}
