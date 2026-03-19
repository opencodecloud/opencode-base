package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;

/**
 * Cross Date Line Fence
 * 跨日期线围栏
 *
 * <p>A rectangular geo-fence that can cross the international date line.</p>
 * <p>可以跨越国际日期变更线的矩形地理围栏。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Fence crossing the date line (Pacific Ocean)
 * Coordinate sw = Coordinate.wgs84(170.0, -10.0);  // East of date line
 * Coordinate ne = Coordinate.wgs84(-170.0, 10.0);  // West of date line
 * GeoFence fence = new CrossDateLineFence(sw, ne);
 * boolean inside = fence.contains(point);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Date line crossing fence support - 跨越日期变更线围栏支持</li>
 *   <li>Antimeridian handling - 反子午线处理</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param southwest the southwest corner | 西南角
 * @param northeast the northeast corner | 东北角
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public record CrossDateLineFence(Coordinate southwest, Coordinate northeast) implements GeoFence {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public CrossDateLineFence {
        if (southwest == null || northeast == null) {
            throw new IllegalArgumentException("Corners cannot be null");
        }
    }

    @Override
    public boolean contains(Coordinate point) {
        if (point == null) {
            return false;
        }

        // Check latitude
        boolean latInRange = point.latitude() >= southwest.latitude()
            && point.latitude() <= northeast.latitude();

        if (!latInRange) {
            return false;
        }

        // Check longitude, handling date line crossing
        if (southwest.longitude() > northeast.longitude()) {
            // Crosses the date line
            return point.longitude() >= southwest.longitude()
                || point.longitude() <= northeast.longitude();
        }

        // Normal case (doesn't cross date line)
        return point.longitude() >= southwest.longitude()
            && point.longitude() <= northeast.longitude();
    }

    /**
     * Check if this fence crosses the international date line
     * 检查此围栏是否跨越国际日期变更线
     *
     * @return true if crosses date line | 如果跨越日期变更线返回true
     */
    public boolean crossesDateLine() {
        return southwest.longitude() > northeast.longitude();
    }
}
