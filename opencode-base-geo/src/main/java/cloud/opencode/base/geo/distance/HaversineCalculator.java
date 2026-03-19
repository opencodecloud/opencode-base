package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;

/**
 * Haversine Distance Calculator
 * Haversine距离计算器
 *
 * <p>Calculates distance using the Haversine formula.</p>
 * <p>使用Haversine公式计算距离。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fast calculation - 快速计算</li>
 *   <li>Good accuracy (~0.5% error) - 良好精度（约0.5%误差）</li>
 *   <li>Suitable for most applications - 适合大多数应用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistanceCalculator calculator = new HaversineCalculator();
 * double distance = calculator.calculate(coord1, coord2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless singleton) - 线程安全: 是（无状态单例）</li>
 *   <li>Null-safe: No (callers must ensure non-null coordinates) - 空值安全: 否（调用者须确保坐标非null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class HaversineCalculator implements DistanceCalculator {

    /** Earth's radius in meters */
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * Singleton instance
     * 单例实例
     */
    public static final HaversineCalculator INSTANCE = new HaversineCalculator();

    @Override
    public double calculate(Coordinate c1, Coordinate c2) {
        double lat1 = Math.toRadians(c1.latitude());
        double lat2 = Math.toRadians(c2.latitude());
        double dLat = Math.toRadians(c2.latitude() - c1.latitude());
        double dLng = Math.toRadians(c2.longitude() - c1.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        // Clamp to [0, 1] to handle floating-point rounding near antipodal points
        // where 'a' can slightly exceed 1.0, causing NaN from sqrt(1-a)
        a = Math.max(0.0, Math.min(a, 1.0));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
