package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;

/**
 * Geo Fence Interface
 * 地理围栏接口
 *
 * <p>Interface for geo-fence containment checks.</p>
 * <p>用于地理围栏包含检查的接口。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>{@link CircleFence} - Circular fence | 圆形围栏</li>
 *   <li>{@link PolygonFence} - Polygon fence | 多边形围栏</li>
 *   <li>{@link RectangleFence} - Rectangle fence | 矩形围栏</li>
 *   <li>{@link CrossDateLineFence} - Cross date line fence | 跨日期线围栏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GeoFence fence = new CircleFence(center, 1000);
 * boolean inside = fence.contains(point);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Geofence interface abstraction - 地理围栏接口抽象</li>
 *   <li>Point containment checking - 点包含检查</li>
 * </ul>
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
public interface GeoFence {

    /**
     * Check if a point is inside the fence
     * 检查点是否在围栏内
     *
     * @param point the point to check | 要检查的点
     * @return true if inside | 如果在内部返回true
     */
    boolean contains(Coordinate point);
}
