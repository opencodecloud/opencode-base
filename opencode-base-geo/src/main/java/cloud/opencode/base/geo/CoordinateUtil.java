package cloud.opencode.base.geo;

/**
 * Coordinate Utility Class
 * 坐标工具类
 *
 * <p>Utility class for coordinate transformation between different coordinate systems.</p>
 * <p>用于不同坐标系之间坐标转换的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>WGS84 to GCJ02 - WGS84转GCJ02</li>
 *   <li>GCJ02 to BD09 - GCJ02转BD09</li>
 *   <li>BD09 to GCJ02 - BD09转GCJ02</li>
 *   <li>GCJ02 to WGS84 - GCJ02转WGS84</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Coordinate wgs84 = Coordinate.wgs84(116.4074, 39.9042);
 * Coordinate gcj02 = CoordinateUtil.transform(wgs84, CoordinateSystem.GCJ02);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是（无状态静态方法）</li>
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
public final class CoordinateUtil {

    private static final double PI = Math.PI;
    private static final double A = 6378245.0;  // Semi-major axis
    private static final double EE = 0.00669342162296594323;  // Eccentricity squared

    private CoordinateUtil() {
    }

    /**
     * Transform coordinate to target system
     * 将坐标转换到目标坐标系
     *
     * @param coord the source coordinate | 源坐标
     * @param target the target coordinate system | 目标坐标系
     * @return transformed coordinate | 转换后的坐标
     */
    public static Coordinate transform(Coordinate coord, CoordinateSystem target) {
        if (coord.system() == target) {
            return coord;
        }

        // First transform to GCJ02
        Coordinate gcj02 = switch (coord.system()) {
            case WGS84 -> wgs84ToGcj02(coord);
            case GCJ02 -> coord;
            case BD09 -> bd09ToGcj02(coord);
        };

        // Then transform from GCJ02 to target
        return switch (target) {
            case WGS84 -> gcj02ToWgs84(gcj02);
            case GCJ02 -> gcj02;
            case BD09 -> gcj02ToBd09(gcj02);
        };
    }

    /**
     * Transform WGS84 to GCJ02
     * WGS84转GCJ02
     *
     * @param wgs84 the WGS84 coordinate | WGS84坐标
     * @return GCJ02 coordinate | GCJ02坐标
     */
    public static Coordinate wgs84ToGcj02(Coordinate wgs84) {
        double lng = wgs84.longitude();
        double lat = wgs84.latitude();

        if (!OpenGeo.isInChina(lng, lat)) {
            return Coordinate.gcj02(lng, lat);
        }

        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);

        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI);

        return Coordinate.gcj02(lng + dLng, lat + dLat);
    }

    /**
     * Transform GCJ02 to BD09
     * GCJ02转BD09
     *
     * @param gcj02 the GCJ02 coordinate | GCJ02坐标
     * @return BD09 coordinate | BD09坐标
     */
    public static Coordinate gcj02ToBd09(Coordinate gcj02) {
        double x = gcj02.longitude();
        double y = gcj02.latitude();

        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI * 3000.0 / 180.0);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI * 3000.0 / 180.0);

        return Coordinate.bd09(
            z * Math.cos(theta) + 0.0065,
            z * Math.sin(theta) + 0.006
        );
    }

    /**
     * Transform BD09 to GCJ02
     * BD09转GCJ02
     *
     * @param bd09 the BD09 coordinate | BD09坐标
     * @return GCJ02 coordinate | GCJ02坐标
     */
    public static Coordinate bd09ToGcj02(Coordinate bd09) {
        double x = bd09.longitude() - 0.0065;
        double y = bd09.latitude() - 0.006;

        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI * 3000.0 / 180.0);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI * 3000.0 / 180.0);

        return Coordinate.gcj02(z * Math.cos(theta), z * Math.sin(theta));
    }

    /**
     * Transform GCJ02 to WGS84
     * GCJ02转WGS84
     *
     * @param gcj02 the GCJ02 coordinate | GCJ02坐标
     * @return WGS84 coordinate | WGS84坐标
     */
    public static Coordinate gcj02ToWgs84(Coordinate gcj02) {
        Coordinate wgs = Coordinate.wgs84(gcj02.longitude(), gcj02.latitude());
        Coordinate mGcj = wgs84ToGcj02(wgs);
        double dLng = mGcj.longitude() - gcj02.longitude();
        double dLat = mGcj.latitude() - gcj02.latitude();
        return Coordinate.wgs84(gcj02.longitude() - dLng, gcj02.latitude() - dLat);
    }

    /**
     * Transform latitude offset
     * 转换纬度偏移
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * Transform longitude offset
     * 转换经度偏移
     */
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
}
