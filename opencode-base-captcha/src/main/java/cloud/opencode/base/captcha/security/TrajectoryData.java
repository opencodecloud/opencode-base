package cloud.opencode.base.captcha.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Trajectory Data — Records user interaction coordinate and timestamp sequences
 * 轨迹数据 — 记录用户交互的坐标和时间戳序列
 *
 * <p>This record captures the full trajectory of a user's mouse or touch interaction,
 * including coordinate points and their corresponding timestamps. It provides derived
 * metrics such as speed, acceleration, direction changes, and jitter for bot detection.</p>
 * <p>此记录捕获用户鼠标或触摸交互的完整轨迹，包括坐标点及其对应的时间戳。
 * 它提供派生指标，如速度、加速度、方向变化和抖动，用于机器人检测。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable trajectory storage with defensive copies - 不可变轨迹存储（防御性拷贝）</li>
 *   <li>Speed and acceleration calculation - 速度和加速度计算</li>
 *   <li>Direction change detection - 方向变化检测</li>
 *   <li>Jitter standard deviation measurement - 抖动标准差测量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<TrajectoryData.Point> points = List.of(
 *     new TrajectoryData.Point(0, 0),
 *     new TrajectoryData.Point(10, 5),
 *     new TrajectoryData.Point(20, 8),
 *     new TrajectoryData.Point(30, 10)
 * );
 * List<Long> timestamps = List.of(0L, 100L, 200L, 300L);
 * TrajectoryData data = new TrajectoryData(points, timestamps, 300L);
 *
 * List<Double> speeds = data.speeds();
 * double jitter = data.jitterStdDev();
 * int changes = data.directionChanges();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record with defensive copies) - 线程安全: 是（不可变记录，防御性拷贝）</li>
 *   <li>Null-safe: No (points and timestamps must not be null) - 空值安全: 否（点和时间戳不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Construction: O(n) for defensive copy - 构造: O(n) 防御性拷贝</li>
 *   <li>Speed/acceleration: O(n) per call - 速度/加速度: 每次调用 O(n)</li>
 *   <li>Direction changes: O(n) per call - 方向变化: 每次调用 O(n)</li>
 *   <li>Jitter: O(n) per call - 抖动: 每次调用 O(n)</li>
 * </ul>
 *
 * @param points          the coordinate sequence | 坐标序列
 * @param timestamps      the corresponding timestamps in milliseconds | 对应时间戳（毫秒）
 * @param totalDurationMs the total duration in milliseconds | 总耗时（毫秒）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public record TrajectoryData(
    List<Point> points,
    List<Long> timestamps,
    long totalDurationMs
) {

    /**
     * Minimum angle difference in radians to count as a direction change (15 degrees).
     * 方向变化的最小角度差（弧度），对应 15 度。
     */
    private static final double DIRECTION_CHANGE_THRESHOLD = Math.toRadians(15);

    /**
     * Compact constructor with validation and defensive copies.
     * 紧凑构造器，包含验证和防御性拷贝。
     *
     * @throws NullPointerException     if points or timestamps is null | 如果 points 或 timestamps 为 null
     * @throws IllegalArgumentException if sizes mismatch, fewer than 2 points,
     *                                  or totalDurationMs is negative |
     *                                  如果大小不匹配、少于 2 个点或 totalDurationMs 为负数
     */
    public TrajectoryData {
        Objects.requireNonNull(points, "points must not be null");
        Objects.requireNonNull(timestamps, "timestamps must not be null");
        if (points.size() != timestamps.size()) {
            throw new IllegalArgumentException(
                "points size (" + points.size() + ") must equal timestamps size (" + timestamps.size() + ")");
        }
        if (points.size() < 2) {
            throw new IllegalArgumentException("at least 2 points are required, got " + points.size());
        }
        if (points.size() > 10_000) {
            throw new IllegalArgumentException("too many trajectory points: " + points.size() + ", max 10000");
        }
        if (totalDurationMs < 0) {
            throw new IllegalArgumentException("totalDurationMs must be >= 0, got " + totalDurationMs);
        }
        points = List.copyOf(points);
        timestamps = List.copyOf(timestamps);
    }

    /**
     * Calculates speed sequence between consecutive points.
     * 计算相邻点之间的速度序列。
     *
     * <p>Speed is computed as Euclidean distance divided by time difference.
     * If the time difference between two consecutive points is zero, the speed is reported as 0.</p>
     * <p>速度计算为欧几里得距离除以时间差。如果两个连续点之间的时间差为零，则速度报告为 0。</p>
     *
     * @return speeds in pixels per millisecond, with size = points.size() - 1 |
     *         以像素/毫秒为单位的速度列表，大小 = points.size() - 1
     */
    public List<Double> speeds() {
        List<Double> result = new ArrayList<>(points.size() - 1);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            double dx = (double) p2.x() - p1.x();
            double dy = (double) p2.y() - p1.y();
            double distance = Math.sqrt(dx * dx + dy * dy);
            long dt = timestamps.get(i + 1) - timestamps.get(i);
            result.add(dt == 0 ? 0.0 : distance / dt);
        }
        return List.copyOf(result);
    }

    /**
     * Calculates acceleration sequence from the speed sequence.
     * 从速度序列计算加速度序列。
     *
     * <p>Acceleration is computed as the change in speed divided by the time difference
     * between the midpoints of the corresponding speed intervals. If the time difference
     * is zero, the acceleration is reported as 0.</p>
     * <p>加速度计算为速度变化除以对应速度区间中点之间的时间差。
     * 如果时间差为零，则加速度报告为 0。</p>
     *
     * @return accelerations, with size = points.size() - 2 |
     *         加速度列表，大小 = points.size() - 2
     */
    public List<Double> accelerations() {
        List<Double> speedList = speeds();
        if (speedList.size() < 2) {
            return List.of();
        }
        List<Double> result = new ArrayList<>(speedList.size() - 1);
        for (int i = 0; i < speedList.size() - 1; i++) {
            long dt = timestamps.get(i + 2) - timestamps.get(i + 1);
            double dSpeed = speedList.get(i + 1) - speedList.get(i);
            result.add(dt == 0 ? 0.0 : dSpeed / dt);
        }
        return List.copyOf(result);
    }

    /**
     * Counts direction changes in the trajectory.
     * 计算轨迹中的方向变化次数。
     *
     * <p>A direction change is counted when the angle between two consecutive
     * movement vectors exceeds 15 degrees.</p>
     * <p>当两个连续运动向量之间的角度超过 15 度时，计为一次方向变化。</p>
     *
     * @return number of direction changes | 方向变化次数
     */
    public int directionChanges() {
        if (points.size() < 3) {
            return 0;
        }
        int changes = 0;
        for (int i = 0; i < points.size() - 2; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            Point p3 = points.get(i + 2);

            double dx1 = (double) p2.x() - p1.x();
            double dy1 = (double) p2.y() - p1.y();
            double dx2 = (double) p3.x() - p2.x();
            double dy2 = (double) p3.y() - p2.y();

            double len1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double len2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

            // Skip zero-length segments
            if (len1 == 0 || len2 == 0) {
                continue;
            }

            double angle1 = Math.atan2(dy1, dx1);
            double angle2 = Math.atan2(dy2, dx2);
            double angleDiff = Math.abs(angle2 - angle1);

            // Normalize to [0, PI]
            if (angleDiff > Math.PI) {
                angleDiff = 2.0 * Math.PI - angleDiff;
            }

            if (angleDiff > DIRECTION_CHANGE_THRESHOLD) {
                changes++;
            }
        }
        return changes;
    }

    /**
     * Calculates jitter standard deviation perpendicular to the main movement direction.
     * 计算垂直于主运动方向的抖动标准差。
     *
     * <p>The main movement direction is defined as the line from the first point to the
     * last point. The jitter is the standard deviation of the perpendicular distances
     * from all points to this line.</p>
     * <p>主运动方向定义为从第一个点到最后一个点的连线。
     * 抖动是所有点到此连线的垂直距离的标准差。</p>
     *
     * @return jitter standard deviation in pixels | 抖动标准差（像素）
     */
    public double jitterStdDev() {
        Point first = points.getFirst();
        Point last = points.getLast();

        double lineX = (double) last.x() - first.x();
        double lineY = (double) last.y() - first.y();
        double lineLen = Math.sqrt(lineX * lineX + lineY * lineY);

        // If start and end are the same point, compute std dev of distances from first point
        if (lineLen == 0) {
            double sumSq = 0;
            double sum = 0;
            for (Point p : points) {
                double dxp = (double) p.x() - first.x();
                double dyp = (double) p.y() - first.y();
                double dist = Math.sqrt(dxp * dxp + dyp * dyp);
                sum += dist;
                sumSq += dist * dist;
            }
            double mean = sum / points.size();
            return Math.sqrt(Math.max(0.0, sumSq / points.size() - mean * mean));
        }

        // Compute perpendicular distances to the line from first to last
        double[] distances = new double[points.size()];
        double sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            // Cross product gives signed perpendicular distance * lineLen
            double cross = lineX * ((double) p.y() - first.y()) - lineY * ((double) p.x() - first.x());
            distances[i] = cross / lineLen;
            sum += distances[i];
        }

        double mean = sum / points.size();
        double varianceSum = 0;
        for (double d : distances) {
            double diff = d - mean;
            varianceSum += diff * diff;
        }
        return Math.sqrt(Math.max(0.0, varianceSum / points.size()));
    }

    /**
     * Trajectory point.
     * 轨迹点。
     *
     * @param x the x coordinate | x 坐标
     * @param y the y coordinate | y 坐标
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public record Point(int x, int y) {}
}
