package cloud.opencode.base.captcha.security;

import java.util.List;

/**
 * Trajectory Analyzer — Analyzes user interaction trajectories for bot detection
 * 轨迹分析器 — 分析用户交互轨迹以检测机器人
 *
 * <p>This class examines trajectory data (coordinate points, timestamps) to distinguish
 * human interactions from automated bot behavior. It uses multiple heuristics including
 * speed variance, jitter analysis, and direction change counting.</p>
 * <p>此类检查轨迹数据（坐标点、时间戳）以区分人类交互和自动化机器人行为。
 * 它使用多种启发式方法，包括速度方差、抖动分析和方向变化计数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Speed variance analysis - 速度方差分析</li>
 *   <li>Micro-jitter detection - 微抖动检测</li>
 *   <li>Direction change counting - 方向变化计数</li>
 *   <li>Completion time validation - 完成时间验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TrajectoryAnalyzer analyzer = new TrajectoryAnalyzer();
 * TrajectoryData data = new TrajectoryData(points, timestamps, 1500L);
 * TrajectoryResult result = analyzer.analyze(data);
 * if (result != TrajectoryResult.HUMAN) {
 *     // Reject as bot
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, no mutable fields) - 线程安全: 是（无状态，无可变字段）</li>
 *   <li>Null-safe: No (data must not be null) - 空值安全: 否（数据不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Analysis: O(n) where n is the number of trajectory points - 分析: O(n)，n 为轨迹点数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class TrajectoryAnalyzer {

    /**
     * Minimum jitter threshold in pixels. Trajectories with jitter below this
     * value are considered bot-like (too smooth).
     * 最小抖动阈值（像素）。抖动低于此值的轨迹被视为机器人特征（过于平滑）。
     */
    private static final double MIN_JITTER = 0.5;

    /**
     * Maximum speed variance ratio (variance / mean). Speeds with a ratio below this
     * threshold indicate unnaturally constant speed (bot-like).
     * 最大速度方差比（方差 / 均值）。低于此阈值的速度比表示不自然的恒速（机器人特征）。
     */
    private static final double MAX_SPEED_VARIANCE_RATIO = 0.1;

    /**
     * Minimum completion duration in milliseconds. Completions faster than this
     * are considered bot-like.
     * 最短完成时间（毫秒）。快于此时间的完成被视为机器人特征。
     */
    private static final long MIN_DURATION_MS = 200;

    /**
     * Minimum number of trajectory points required for meaningful analysis.
     * 有意义分析所需的最少轨迹点数。
     */
    private static final int MIN_POINTS = 5;

    /**
     * Minimum number of direction changes expected in a human trajectory.
     * 人类轨迹中预期的最少方向变化次数。
     */
    private static final int MIN_DIRECTION_CHANGES = 2;

    /**
     * Analyzes trajectory data and returns a result indicating whether the trajectory
     * appears human or bot-like.
     * 分析轨迹数据并返回结果，指示轨迹是否看起来像人类或机器人。
     *
     * <p>The analysis proceeds through the following checks in order:</p>
     * <p>分析按以下顺序进行检查：</p>
     * <ol>
     *   <li>Insufficient data check (fewer than {@value MIN_POINTS} points)</li>
     *   <li>Too-fast completion check (less than {@value MIN_DURATION_MS} ms)</li>
     *   <li>Constant speed detection (speed variance / mean &lt; {@value MAX_SPEED_VARIANCE_RATIO})</li>
     *   <li>No-jitter detection (jitter std dev &lt; {@value MIN_JITTER} pixels)</li>
     *   <li>Linear trajectory detection (fewer than {@value MIN_DIRECTION_CHANGES} direction changes)</li>
     * </ol>
     *
     * @param data the trajectory data to analyze | 要分析的轨迹数据
     * @return the analysis result | 分析结果
     * @throws NullPointerException if data is null | 如果 data 为 null
     */
    public TrajectoryResult analyze(TrajectoryData data) {
        java.util.Objects.requireNonNull(data, "data must not be null");

        // 1. Insufficient data
        if (data.points().size() < MIN_POINTS) {
            return TrajectoryResult.INSUFFICIENT_DATA;
        }

        // 2. Too fast
        if (data.totalDurationMs() < MIN_DURATION_MS) {
            return TrajectoryResult.BOT_TOO_FAST;
        }

        // 3. Constant speed detection
        List<Double> speeds = data.speeds();
        double speedMean = 0;
        for (double s : speeds) {
            speedMean += s;
        }
        speedMean /= speeds.size();

        if (speedMean > 0) {
            double variance = 0;
            for (double s : speeds) {
                double diff = s - speedMean;
                variance += diff * diff;
            }
            variance /= speeds.size();
            double varianceRatio = variance / (speedMean * speedMean);
            if (varianceRatio < MAX_SPEED_VARIANCE_RATIO) {
                return TrajectoryResult.BOT_CONSTANT_SPEED;
            }
        }

        // 4. No jitter
        if (data.jitterStdDev() < MIN_JITTER) {
            return TrajectoryResult.BOT_NO_JITTER;
        }

        // 5. Linear trajectory
        if (data.directionChanges() < MIN_DIRECTION_CHANGES) {
            return TrajectoryResult.BOT_LINEAR;
        }

        // All checks passed
        return TrajectoryResult.HUMAN;
    }

    /**
     * Trajectory analysis result.
     * 轨迹分析结果。
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-captcha V1.0.3
     */
    public enum TrajectoryResult {
        /** Human-like trajectory | 类人轨迹 */
        HUMAN,
        /** Linear trajectory (bot-like) | 直线轨迹（机器人特征） */
        BOT_LINEAR,
        /** No micro-jitter (bot-like) | 无微抖动（机器人特征） */
        BOT_NO_JITTER,
        /** Constant speed (bot-like) | 恒速移动（机器人特征） */
        BOT_CONSTANT_SPEED,
        /** Too fast completion (bot-like) | 过快完成（机器人特征） */
        BOT_TOO_FAST,
        /** Insufficient data for analysis | 数据不足 */
        INSUFFICIENT_DATA
    }
}
