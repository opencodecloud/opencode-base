package cloud.opencode.base.log.perf;

import cloud.opencode.base.log.OpenLog;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * StopWatch - High-precision Timer for Performance Measurement
 * 计时器 - 用于性能测量的高精度计时器
 *
 * <p>StopWatch provides a simple way to measure elapsed time for operations.
 * It uses System.nanoTime() for high precision timing.</p>
 * <p>StopWatch 提供了一种测量操作耗时的简单方法。它使用 System.nanoTime() 进行高精度计时。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Basic usage
 * StopWatch watch = StopWatch.start("queryUsers");
 * List<User> users = userDao.findAll();
 * watch.stopAndLog();  // Output: queryUsers completed in 123ms
 *
 * // With try-with-resources
 * try (StopWatch watch = StopWatch.start("processOrder")) {
 *     orderService.process(order);
 * }  // Automatically logs on close
 *
 * // Manual control
 * StopWatch watch = StopWatch.create("batchProcess");
 * watch.startTiming();
 * // ... operations
 * long elapsed = watch.stop().getElapsedMillis();
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High-precision timing via System.nanoTime() - 通过 System.nanoTime() 的高精度计时</li>
 *   <li>AutoCloseable for try-with-resources - 支持 try-with-resources 的 AutoCloseable</li>
 *   <li>Automatic logging with optional threshold warning - 自动记录，可选阈值警告</li>
 *   <li>Manual start/stop/reset control - 手动启动/停止/重置控制</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for shared use) - 线程安全: 否（不适用于共享使用）</li>
 *   <li>Null-safe: No (operation name must not be null) - 空值安全: 否（操作名称不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class StopWatch implements AutoCloseable {

    private final String operation;
    private long startTime;
    private long endTime;
    private boolean running;

    private StopWatch(String operation) {
        this.operation = operation;
        this.startTime = 0;
        this.endTime = 0;
        this.running = false;
    }

    /**
     * Creates and starts a new StopWatch.
     * 创建并启动一个新的 StopWatch。
     *
     * @param operation the operation name - 操作名称
     * @return the started StopWatch - 已启动的 StopWatch
     */
    public static StopWatch start(String operation) {
        StopWatch watch = new StopWatch(operation);
        watch.startTiming();
        return watch;
    }

    /**
     * Creates a StopWatch without starting it.
     * 创建一个未启动的 StopWatch。
     *
     * @param operation the operation name - 操作名称
     * @return the StopWatch - StopWatch
     */
    public static StopWatch create(String operation) {
        return new StopWatch(operation);
    }

    /**
     * Starts the timer.
     * 启动计时器。
     *
     * @return this StopWatch - 此 StopWatch
     */
    public StopWatch startTiming() {
        if (running) {
            throw new IllegalStateException("StopWatch is already running");
        }
        this.startTime = System.nanoTime();
        this.running = true;
        return this;
    }

    /**
     * Stops the timer.
     * 停止计时器。
     *
     * @return this StopWatch - 此 StopWatch
     */
    public StopWatch stop() {
        if (!running) {
            throw new IllegalStateException("StopWatch is not running");
        }
        this.endTime = System.nanoTime();
        this.running = false;
        return this;
    }

    /**
     * Resets the timer.
     * 重置计时器。
     *
     * @return this StopWatch - 此 StopWatch
     */
    public StopWatch reset() {
        this.startTime = 0;
        this.endTime = 0;
        this.running = false;
        return this;
    }

    /**
     * Returns the elapsed time in milliseconds.
     * 返回以毫秒为单位的耗时。
     *
     * @return elapsed time in milliseconds - 毫秒耗时
     */
    public long getElapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getElapsedNanos());
    }

    /**
     * Returns the elapsed time in nanoseconds.
     * 返回以纳秒为单位的耗时。
     *
     * @return elapsed time in nanoseconds - 纳秒耗时
     */
    public long getElapsedNanos() {
        if (running) {
            return System.nanoTime() - startTime;
        }
        return endTime - startTime;
    }

    /**
     * Returns the elapsed time as a Duration.
     * 返回耗时作为 Duration。
     *
     * @return the elapsed Duration - 耗时 Duration
     */
    public Duration getElapsed() {
        return Duration.ofNanos(getElapsedNanos());
    }

    /**
     * Checks if the timer is running.
     * 检查计时器是否正在运行。
     *
     * @return true if running - 如果运行中返回 true
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the operation name.
     * 返回操作名称。
     *
     * @return the operation name - 操作名称
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Stops the timer and logs the result.
     * 停止计时器并记录结果。
     */
    public void stopAndLog() {
        if (running) {
            stop();
        }
        long elapsed = getElapsedMillis();
        OpenLog.info("{} completed in {}ms", operation, elapsed);
    }

    /**
     * Stops the timer and logs with threshold warning.
     * 停止计时器并带阈值警告记录。
     *
     * @param thresholdMs the threshold in milliseconds - 毫秒阈值
     */
    public void stopAndLog(long thresholdMs) {
        if (running) {
            stop();
        }
        long elapsed = getElapsedMillis();
        if (elapsed > thresholdMs) {
            OpenLog.warn("{} took {}ms (threshold: {}ms)", operation, elapsed, thresholdMs);
        } else {
            OpenLog.info("{} completed in {}ms", operation, elapsed);
        }
    }

    @Override
    public void close() {
        if (running) {
            stopAndLog();
        }
    }

    @Override
    public String toString() {
        long elapsed = getElapsedMillis();
        return String.format("StopWatch[%s, %dms, %s]",
                operation, elapsed, running ? "running" : "stopped");
    }
}
