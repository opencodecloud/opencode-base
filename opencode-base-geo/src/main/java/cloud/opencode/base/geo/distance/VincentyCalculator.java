package cloud.opencode.base.geo.distance;

import cloud.opencode.base.geo.Coordinate;

/**
 * Vincenty Distance Calculator
 * Vincenty距离计算器
 *
 * <p>Calculates distance using Vincenty's formulae for ellipsoidal geodesy.</p>
 * <p>使用Vincenty公式进行椭球体测地计算距离。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High precision (~0.5mm error) - 高精度（约0.5mm误差）</li>
 *   <li>Uses WGS84 ellipsoid - 使用WGS84椭球</li>
 *   <li>Suitable for surveying - 适合测绘</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DistanceCalculator calculator = new VincentyCalculator();
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
 *   <li>Time complexity: O(iterations) - 时间复杂度: O(iterations)（迭代次数）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
public class VincentyCalculator implements DistanceCalculator {

    /** WGS84 semi-major axis (equatorial radius) in meters */
    private static final double A = 6378137.0;

    /** WGS84 semi-minor axis (polar radius) in meters */
    private static final double B = 6356752.314245;

    /** WGS84 flattening */
    private static final double F = 1 / 298.257223563;

    /** Maximum iterations for convergence */
    private static final int MAX_ITERATIONS = 200;

    /** Convergence tolerance */
    private static final double TOLERANCE = 1e-12;

    /**
     * Singleton instance
     * 单例实例
     */
    public static final VincentyCalculator INSTANCE = new VincentyCalculator();

    @Override
    public double calculate(Coordinate c1, Coordinate c2) {
        double lng1 = Math.toRadians(c1.longitude());
        double lat1 = Math.toRadians(c1.latitude());
        double lng2 = Math.toRadians(c2.longitude());
        double lat2 = Math.toRadians(c2.latitude());

        double L = lng2 - lng1;
        double U1 = Math.atan((1 - F) * Math.tan(lat1));
        double U2 = Math.atan((1 - F) * Math.tan(lat2));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambda = L;
        double lambdaP;
        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;

        int iterLimit = MAX_ITERATIONS;
        do {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);

            sinSigma = Math.sqrt(
                (cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                    * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
            );

            if (sinSigma < 1e-15) {
                return 0; // Co-incident points
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;

            cos2SigmaM = cosSqAlpha != 0
                ? cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha
                : 0;

            double C = F / 16 * cosSqAlpha * (4 + F * (4 - 3 * cosSqAlpha));

            lambdaP = lambda;
            lambda = L + (1 - C) * F * sinAlpha
                * (sigma + C * sinSigma
                * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));

        } while (Math.abs(lambda - lambdaP) > TOLERANCE && --iterLimit > 0);

        if (iterLimit == 0) {
            // Failed to converge, fall back to Haversine
            return HaversineCalculator.INSTANCE.calculate(c1, c2);
        }

        double uSq = cosSqAlpha * (A * A - B * B) / (B * B);
        double AA = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double BB = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = BB * sinSigma
            * (cos2SigmaM + BB / 4
            * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
            - BB / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma)
            * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        return B * AA * (sigma - deltaSigma);
    }
}
