package cloud.opencode.base.geo.fence;

import cloud.opencode.base.geo.Coordinate;

import java.util.List;

/**
 * Polygon Fence
 * 多边形围栏
 *
 * <p>A polygon geo-fence defined by a list of vertices.</p>
 * <p>由顶点列表定义的多边形地理围栏。</p>
 *
 * <p><strong>Algorithm | 算法:</strong></p>
 * <p>Uses the Ray Casting algorithm for point-in-polygon detection.</p>
 * <p>使用射线投射算法进行点在多边形内检测。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Coordinate> vertices = List.of(
 *     Coordinate.wgs84(116.0, 39.0),
 *     Coordinate.wgs84(117.0, 39.0),
 *     Coordinate.wgs84(117.0, 40.0),
 *     Coordinate.wgs84(116.0, 40.0)
 * );
 * GeoFence fence = new PolygonFence(vertices);
 * boolean inside = fence.contains(point);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Polygon-shaped geofence - 多边形地理围栏</li>
 *   <li>Ray-casting containment algorithm - 射线投射包含算法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param vertices the polygon vertices | 多边形顶点
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public record PolygonFence(List<Coordinate> vertices) implements GeoFence {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造函数
     */
    public PolygonFence {
        if (vertices == null || vertices.size() < 3) {
            throw new IllegalArgumentException("Polygon must have at least 3 vertices");
        }
        vertices = List.copyOf(vertices);
    }

    @Override
    public boolean contains(Coordinate point) {
        if (point == null) {
            return false;
        }

        double px = point.longitude();
        double py = point.latitude();
        int n = vertices.size();

        // Tolerance for floating point comparison (~1mm at equator)
        final double EPSILON = 1e-8;

        // Single-pass: ray casting + boundary check combined
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).longitude();
            double yi = vertices.get(i).latitude();
            double xj = vertices.get(j).longitude();
            double yj = vertices.get(j).latitude();

            // Check if point is on vertex
            if (Math.abs(px - xi) < EPSILON && Math.abs(py - yi) < EPSILON) {
                return true;
            }

            // Check if point is on edge
            if (isOnEdge(px, py, xi, yi, xj, yj, EPSILON)) {
                return true;
            }

            // Ray casting
            if (((yi > py) != (yj > py))
                && (px < (xj - xi) * (py - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Check if point (px, py) lies on the edge from (x1, y1) to (x2, y2)
     * 检查点是否在边上
     */
    private boolean isOnEdge(double px, double py,
                             double x1, double y1, double x2, double y2,
                             double epsilon) {
        // Check bounding box first
        double minX = Math.min(x1, x2) - epsilon;
        double maxX = Math.max(x1, x2) + epsilon;
        double minY = Math.min(y1, y2) - epsilon;
        double maxY = Math.max(y1, y2) + epsilon;

        if (px < minX || px > maxX || py < minY || py > maxY) {
            return false;
        }

        // Check collinearity using cross product
        double crossProduct = (py - y1) * (x2 - x1) - (px - x1) * (y2 - y1);
        return Math.abs(crossProduct) < epsilon;
    }

    /**
     * Get the number of vertices
     * 获取顶点数量
     *
     * @return number of vertices | 顶点数量
     */
    public int vertexCount() {
        return vertices.size();
    }

    /**
     * Get the bounding box
     * 获取边界框
     *
     * @return rectangle fence representing the bounding box | 表示边界框的矩形围栏
     */
    public RectangleFence getBoundingBox() {
        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        for (Coordinate v : vertices) {
            if (v.longitude() < minLng) minLng = v.longitude();
            if (v.longitude() > maxLng) maxLng = v.longitude();
            if (v.latitude() < minLat) minLat = v.latitude();
            if (v.latitude() > maxLat) maxLat = v.latitude();
        }

        return new RectangleFence(
            Coordinate.wgs84(minLng, minLat),
            Coordinate.wgs84(maxLng, maxLat)
        );
    }
}
