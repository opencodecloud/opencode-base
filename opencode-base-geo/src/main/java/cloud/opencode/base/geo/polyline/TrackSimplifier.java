package cloud.opencode.base.geo.polyline;

import cloud.opencode.base.geo.Coordinate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * GPS Track Simplifier - Ramer-Douglas-Peucker Algorithm
 * GPS轨迹简化器 - Ramer-Douglas-Peucker算法
 *
 * <p>Simplifies GPS tracks by removing points that are within a specified
 * tolerance of the simplified path, using the Ramer-Douglas-Peucker algorithm
 * with a stack-based iterative approach to avoid stack overflow on large tracks.</p>
 * <p>使用Ramer-Douglas-Peucker算法通过删除在简化路径指定容差范围内的点来简化GPS轨迹，
 * 采用基于栈的迭代方式避免大型轨迹上的栈溢出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Ramer-Douglas-Peucker simplification - Ramer-Douglas-Peucker简化</li>
 *   <li>Stack-based iterative implementation (no recursion) - 基于栈的迭代实现（无递归）</li>
 *   <li>Spherical distance calculation (Haversine-based) - 球面距离计算（基于Haversine）</li>
 *   <li>Handles large tracks safely - 安全处理大型轨迹</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Coordinate> track = List.of(
 *     Coordinate.wgs84(116.40, 39.90),
 *     Coordinate.wgs84(116.41, 39.91),
 *     Coordinate.wgs84(116.42, 39.90),
 *     Coordinate.wgs84(116.50, 39.95)
 * );
 *
 * // Simplify with 100m tolerance
 * List<Coordinate> simplified = TrackSimplifier.simplify(track, 100.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Stack-safe: Uses iterative approach, no risk of StackOverflow - 栈安全: 使用迭代方式，无StackOverflow风险</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see PolylineCodec
 * @since JDK 25, opencode-base-geo V1.0.3
 */
public final class TrackSimplifier {

    /**
     * Earth's mean radius in meters
     * 地球平均半径（米）
     */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private TrackSimplifier() {
    }

    /**
     * Simplify a GPS track using the Ramer-Douglas-Peucker algorithm
     * 使用Ramer-Douglas-Peucker算法简化GPS轨迹
     *
     * <p>Points that deviate less than the specified tolerance from the simplified
     * line are removed. The first and last points are always preserved.</p>
     * <p>偏离简化线小于指定容差的点将被移除。始终保留第一个和最后一个点。</p>
     *
     * @param track the GPS track coordinates | GPS轨迹坐标
     * @param toleranceMeters the maximum allowed perpendicular distance in meters |
     *        允许的最大垂直距离（米）
     * @return simplified track | 简化后的轨迹
     * @throws IllegalArgumentException if toleranceMeters is negative | 当toleranceMeters为负数时抛出
     */
    public static List<Coordinate> simplify(List<Coordinate> track, double toleranceMeters) {
        if (track == null || track.isEmpty()) {
            return Collections.emptyList();
        }
        if (track.size() <= 2) {
            return new ArrayList<>(track);
        }
        if (Double.isNaN(toleranceMeters) || toleranceMeters < 0) {
            throw new IllegalArgumentException(
                    "Tolerance must be non-negative, got: " + toleranceMeters);
        }
        if (Double.isInfinite(toleranceMeters)) {
            // Infinite tolerance: keep only first and last
            List<Coordinate> result = new ArrayList<>(2);
            result.add(track.getFirst());
            result.add(track.getLast());
            return result;
        }

        int n = track.size();
        BitSet keep = new BitSet(n);
        keep.set(0);
        keep.set(n - 1);

        // Iterative Ramer-Douglas-Peucker using explicit stack
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, n - 1});

        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int start = range[0];
            int end = range[1];

            if (end - start < 2) {
                continue;
            }

            double maxDist = 0;
            int maxIndex = start;

            Coordinate a = track.get(start);
            Coordinate b = track.get(end);

            for (int i = start + 1; i < end; i++) {
                double dist = distanceToSegment(track.get(i), a, b);
                if (dist > maxDist) {
                    maxDist = dist;
                    maxIndex = i;
                }
            }

            if (maxDist > toleranceMeters) {
                keep.set(maxIndex);
                stack.push(new int[]{start, maxIndex});
                stack.push(new int[]{maxIndex, end});
            }
        }

        // Build result preserving original order
        List<Coordinate> result = new ArrayList<>();
        for (int i = keep.nextSetBit(0); i >= 0; i = keep.nextSetBit(i + 1)) {
            result.add(track.get(i));
        }
        return result;
    }

    /**
     * Calculate the perpendicular distance from point P to segment AB on a sphere
     * 计算球面上点P到线段AB的垂直距离
     *
     * <p>Uses the cross-track distance formula based on spherical geometry.</p>
     * <p>使用基于球面几何的横向距离公式。</p>
     *
     * @param p the point | 点
     * @param a segment start | 线段起点
     * @param b segment end | 线段终点
     * @return distance in meters | 距离（米）
     */
    static double distanceToSegment(Coordinate p, Coordinate a, Coordinate b) {
        // If A and B are the same point, return distance from P to A
        if (a.latitude() == b.latitude() && a.longitude() == b.longitude()) {
            return haversineDistance(p.latitude(), p.longitude(),
                    a.latitude(), a.longitude());
        }

        double distAP = haversineDistance(a.latitude(), a.longitude(),
                p.latitude(), p.longitude());
        double distAB = haversineDistance(a.latitude(), a.longitude(),
                b.latitude(), b.longitude());
        double distBP = haversineDistance(b.latitude(), b.longitude(),
                p.latitude(), p.longitude());

        // Use cross-track distance formula
        // angular distance A to P
        double angDistAP = distAP / EARTH_RADIUS_M;

        // Compute bearings from A to B and A to P, sharing A's trig values
        double aLatRad = Math.toRadians(a.latitude());
        double aLonRad = Math.toRadians(a.longitude());
        double cosALat = Math.cos(aLatRad);
        double sinALat = Math.sin(aLatRad);

        double bLatRad = Math.toRadians(b.latitude());
        double dLonAB = Math.toRadians(b.longitude()) - aLonRad;
        double bearingAB = Math.atan2(
                Math.sin(dLonAB) * Math.cos(bLatRad),
                cosALat * Math.sin(bLatRad) - sinALat * Math.cos(bLatRad) * Math.cos(dLonAB));

        double pLatRad = Math.toRadians(p.latitude());
        double dLonAP = Math.toRadians(p.longitude()) - aLonRad;
        double bearingAP = Math.atan2(
                Math.sin(dLonAP) * Math.cos(pLatRad),
                cosALat * Math.sin(pLatRad) - sinALat * Math.cos(pLatRad) * Math.cos(dLonAP));

        // Cross-track distance (clamp asin argument to [-1, 1] for floating-point safety)
        double asinArg = Math.sin(angDistAP) * Math.sin(bearingAP - bearingAB);
        asinArg = Math.max(-1.0, Math.min(1.0, asinArg));
        double crossTrack = Math.asin(asinArg);
        double crossTrackDist = Math.abs(crossTrack) * EARTH_RADIUS_M;

        // Along-track distance to check if projection falls within segment
        double acosArg = Math.cos(angDistAP) / Math.max(Math.cos(crossTrack), 1e-15);
        // Clamp to [-1, 1] to avoid NaN from floating-point rounding
        acosArg = Math.max(-1.0, Math.min(1.0, acosArg));
        double alongTrack = Math.acos(acosArg);
        double angDistAB = distAB / EARTH_RADIUS_M;

        // If along-track distance is negative (behind A), return distance to A
        if (alongTrack < 0) {
            return distAP;
        }
        // If along-track distance exceeds segment length, return distance to B
        if (alongTrack > angDistAB) {
            return distBP;
        }

        return crossTrackDist;
    }

    /**
     * Calculate Haversine distance between two points
     * 计算两点之间的Haversine距离
     *
     * @return distance in meters | 距离（米）
     */
    private static double haversineDistance(double lat1, double lon1,
                                           double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLat = lat2Rad - lat1Rad;
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        // Clamp to [0, 1] to avoid NaN from floating-point rounding
        a = Math.min(a, 1.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_M * c;
    }

    /**
     * Calculate initial bearing from point 1 to point 2
     * 计算从点1到点2的初始方位角
     *
     * @return bearing in radians | 方位角（弧度）
     */
    private static double initialBearing(double lat1, double lon1,
                                         double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);

        double x = Math.sin(dLon) * Math.cos(lat2Rad);
        double y = Math.cos(lat1Rad) * Math.sin(lat2Rad)
                - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        return Math.atan2(x, y);
    }
}
