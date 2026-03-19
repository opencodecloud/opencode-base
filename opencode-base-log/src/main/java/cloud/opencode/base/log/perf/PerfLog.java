package cloud.opencode.base.log.perf;

import cloud.opencode.base.log.exception.OpenLogException;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;
import cloud.opencode.base.log.marker.Markers;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PerfLog - Performance Logging Utility
 * PerfLog - 性能日志工具
 *
 * <p>PerfLog provides convenient methods for measuring and logging operation
 * performance. It integrates with StopWatch and SlowOperationConfig.</p>
 * <p>PerfLog 提供测量和记录操作性能的便捷方法。它与 StopWatch 和 SlowOperationConfig 集成。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Start a StopWatch
 * StopWatch watch = PerfLog.start("queryUsers");
 * List<User> users = userDao.findAll();
 * watch.stopAndLog();
 *
 * // Timed execution
 * PerfLog.timed("processOrder", () -> orderService.process(order));
 *
 * // With threshold warning
 * PerfLog.timedWithThreshold("slowOp", 1000, () -> heavyOperation());
 *
 * // Configure global threshold
 * PerfLog.setGlobalThreshold(500);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>StopWatch-based performance timing - 基于 StopWatch 的性能计时</li>
 *   <li>Timed execution with automatic logging - 自动记录的计时执行</li>
 *   <li>Configurable slow operation threshold and warning - 可配置的慢操作阈值和警告</li>
 *   <li>Integration with PERFORMANCE marker - 与 PERFORMANCE 标记集成</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (AtomicReference for config) - 线程安全: 是（AtomicReference 用于配置）</li>
 *   <li>Null-safe: No (operation name must not be null) - 空值安全: 否（操作名称不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class PerfLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfLog.class);
    private static final AtomicReference<SlowOperationConfig> CONFIG =
            new AtomicReference<>(SlowOperationConfig.DEFAULT);
    private static volatile long globalThreshold = 1000; // 1 second default

    private PerfLog() {
        // Utility class
    }

    // ==================== StopWatch Methods ====================

    /**
     * Starts a new StopWatch.
     * 启动一个新的 StopWatch。
     *
     * @param operation the operation name - 操作名称
     * @return the started StopWatch - 已启动的 StopWatch
     */
    public static StopWatch start(String operation) {
        return StopWatch.start(operation);
    }

    // ==================== Timed Execution ====================

    /**
     * Executes a task and logs the elapsed time.
     * 执行任务并记录耗时。
     *
     * @param operation the operation name - 操作名称
     * @param runnable  the task - 任务
     */
    public static void timed(String operation, Runnable runnable) {
        StopWatch watch = start(operation);
        try {
            runnable.run();
        } finally {
            watch.stopAndLog();
        }
    }

    /**
     * Executes a task and logs the elapsed time, returning the result.
     * 执行任务并记录耗时，返回结果。
     *
     * @param operation the operation name - 操作名称
     * @param callable  the task - 任务
     * @param <T>       the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T timed(String operation, Callable<T> callable) {
        StopWatch watch = start(operation);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new OpenLogException("Operation failed: " + operation, e);
        } finally {
            watch.stopAndLog();
        }
    }

    /**
     * Executes a task with threshold warning.
     * 执行带阈值警告的任务。
     *
     * @param operation   the operation name - 操作名称
     * @param thresholdMs the threshold in milliseconds - 毫秒阈值
     * @param runnable    the task - 任务
     */
    public static void timedWithThreshold(String operation, long thresholdMs, Runnable runnable) {
        StopWatch watch = start(operation);
        try {
            runnable.run();
        } finally {
            watch.stopAndLog(thresholdMs);
        }
    }

    /**
     * Executes a task with threshold warning, returning the result.
     * 执行带阈值警告的任务，返回结果。
     *
     * @param operation   the operation name - 操作名称
     * @param thresholdMs the threshold in milliseconds - 毫秒阈值
     * @param callable    the task - 任务
     * @param <T>         the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T timedWithThreshold(String operation, long thresholdMs, Callable<T> callable) {
        StopWatch watch = start(operation);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new OpenLogException("Operation failed: " + operation, e);
        } finally {
            watch.stopAndLog(thresholdMs);
        }
    }

    // ==================== Logging Methods ====================

    /**
     * Logs performance information.
     * 记录性能信息。
     *
     * @param operation the operation name - 操作名称
     * @param elapsed   the elapsed time in milliseconds - 毫秒耗时
     */
    public static void log(String operation, long elapsed) {
        LOGGER.info(Markers.PERFORMANCE, "{} completed in {}ms", operation, elapsed);
    }

    /**
     * Logs slow operation warning.
     * 记录慢操作警告。
     *
     * @param operation   the operation name - 操作名称
     * @param elapsed     the elapsed time in milliseconds - 毫秒耗时
     * @param thresholdMs the threshold in milliseconds - 毫秒阈值
     */
    public static void warnSlow(String operation, long elapsed, long thresholdMs) {
        LOGGER.warn(Markers.PERFORMANCE, "SLOW: {} took {}ms (threshold: {}ms)",
                operation, elapsed, thresholdMs);
    }

    // ==================== Configuration ====================

    /**
     * Sets the global slow operation threshold.
     * 设置全局慢操作阈值。
     *
     * @param thresholdMs the threshold in milliseconds - 毫秒阈值
     */
    public static void setGlobalThreshold(long thresholdMs) {
        globalThreshold = thresholdMs;
    }

    /**
     * Gets the global slow operation threshold.
     * 获取全局慢操作阈值。
     *
     * @return the threshold in milliseconds - 毫秒阈值
     */
    public static long getGlobalThreshold() {
        return globalThreshold;
    }

    /**
     * Sets the slow operation configuration.
     * 设置慢操作配置。
     *
     * @param config the configuration - 配置
     */
    public static void setSlowOperationConfig(SlowOperationConfig config) {
        CONFIG.set(config != null ? config : SlowOperationConfig.DEFAULT);
    }

    /**
     * Gets the slow operation configuration.
     * 获取慢操作配置。
     *
     * @return the configuration - 配置
     */
    public static SlowOperationConfig getSlowOperationConfig() {
        return CONFIG.get();
    }

    /**
     * Checks if an operation is slow based on global threshold.
     * 根据全局阈值检查操作是否慢。
     *
     * @param elapsedMs the elapsed time in milliseconds - 毫秒耗时
     * @return true if slow - 如果慢返回 true
     */
    public static boolean isSlow(long elapsedMs) {
        return elapsedMs > globalThreshold;
    }
}
