package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;

/**
 * Rectangle Fence
 * 矩形围栏
 *
 * <p>A rectangular geo-fence defined by southwest and northeast corners.</p>
 * <p>由西南角和东北角定义的矩形地理围栏。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Coordinate sw = Coordinate.wgs84(116.0, 39.0);
 * Coordinate ne = Coordinate.wgs84(117.0, 40.0);
 * GeoFence fence = new RectangleFence(sw, ne);
 * boolean inside = fence.contains(point);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rectangle geofence definition - 矩形地理围栏定义</li>
 *   <li>Bounding box containment check - 边界框包含检查</li>
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
public record RectangleFence(Coordinate southwest, Coordinate northeast) implements GeoFence {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public RectangleFence {
        if (southwest == null || northeast == null) {
            throw new IllegalArgumentException("Corners cannot be null");
        }
    }

    @Override
    public boolean contains(Coordinate point) {
        if (point == null) {
            return false;
        }

        // Check latitude first (same logic regardless of date line)
        boolean latInRange = point.latitude() >= southwest.latitude()
            && point.latitude() <= northeast.latitude();

        if (!latInRange) {
            return false;
        }

        // Handle date line crossing: when sw.longitude > ne.longitude
        // the rectangle crosses the 180° meridian
        if (crossesDateLine()) {
            // Point is inside if it's east of SW OR west of NE
            return point.longitude() >= southwest.longitude()
                || point.longitude() <= northeast.longitude();
        }

        // Normal case: doesn't cross date line
        return point.longitude() >= southwest.longitude()
            && point.longitude() <= northeast.longitude();
    }

    /**
     * Check if this fence crosses the international date line (180° meridian)
     * 检查此围栏是否跨越国际日期变更线（180°经线）
     *
     * <p>A rectangle crosses the date line when the southwest corner's longitude
     * is greater than the northeast corner's longitude.</p>
     * <p>当西南角的经度大于东北角的经度时，矩形跨越日期变更线。</p>
     *
     * @return true if crosses date line | 如果跨越日期变更线返回true
     */
    public boolean crossesDateLine() {
        return southwest.longitude() > northeast.longitude();
    }

    /**
     * Get the width in degrees
     * 获取宽度（度）
     *
     * <p>Correctly handles rectangles crossing the international date line.</p>
     * <p>正确处理跨越国际日期变更线的矩形。</p>
     *
     * @return width in degrees (always positive) | 宽度（度，始终为正）
     */
    public double widthDegrees() {
        if (crossesDateLine()) {
            // Width spans from SW eastward across date line to NE
            // e.g., SW=170°, NE=-170° → width = (180-170) + (180-|-170|) = 10 + 10 = 20°
            return (180 - southwest.longitude()) + (180 + northeast.longitude());
        }
        return northeast.longitude() - southwest.longitude();
    }

    /**
     * Get the height in degrees
     * 获取高度（度）
     *
     * @return height in degrees | 高度（度）
     */
    public double heightDegrees() {
        return northeast.latitude() - southwest.latitude();
    }

    /**
     * Get the center point
     * 获取中心点
     *
     * <p>Correctly handles rectangles crossing the international date line.</p>
     * <p>正确处理跨越国际日期变更线的矩形。</p>
     *
     * @return center coordinate | 中心坐标
     */
    public Coordinate center() {
        double centerLat = (southwest.latitude() + northeast.latitude()) / 2;
        double centerLng;

        if (crossesDateLine()) {
            // Calculate center longitude across date line
            // e.g., SW=170°, NE=-170° → center should be at 180° (or -180°)
            double width = widthDegrees();
            centerLng = southwest.longitude() + width / 2;
            // Normalize to [-180, 180]
            if (centerLng > 180) {
                centerLng -= 360;
            }
        } else {
            centerLng = (southwest.longitude() + northeast.longitude()) / 2;
        }

        return Coordinate.wgs84(centerLng, centerLat);
    }
}
