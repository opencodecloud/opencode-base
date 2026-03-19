package cloud.opencode.base.core;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Stopwatch - A lightweight timing utility for measuring elapsed time
 * 秒表 - 用于测量经过时间的轻量级计时工具
 *
 * <p>This class provides a simple and efficient way to measure elapsed time, commonly used for
 * performance testing and debugging.</p>
 * <p>该类提供了一种简单高效的方式来测量经过的时间，通常用于性能测试和调试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High-precision timing using System.nanoTime() - 使用 System.nanoTime() 实现高精度计时</li>
 *   <li>Start, stop, reset, and resume operations - 支持开始、停止、重置和恢复操作</li>
 *   <li>Multiple time unit conversions - 多种时间单位转换</li>
 *   <li>Fluent API design - 流式 API 设计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic usage | 基本用法
 * Stopwatch sw = Stopwatch.createStarted();
 * // ... do something
 * long elapsedMs = sw.elapsed(TimeUnit.MILLISECONDS);
 * System.out.println("Elapsed: " + sw);  // "Elapsed: 123.4 ms"
 *
 * // Manual control | 手动控制
 * Stopwatch sw = Stopwatch.createUnstarted();
 * sw.start();
 * // ... do something
 * sw.stop();
 * Duration duration = sw.elapsed();
 *
 * // Fluent style | 流式风格
 * Stopwatch.createStarted()
 *     .stop()
 *     .reset()
 *     .start();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is NOT thread-safe. For concurrent timing, use external synchronization.</p>
 * <p>此类非线程安全。如需并发计时，请使用外部同步。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Stopwatch {

    // Precompiled patterns for formatDouble
    private static final Pattern TRAILING_ZEROS_PATTERN = Pattern.compile("0+$");
    private static final Pattern TRAILING_DOT_PATTERN = Pattern.compile("\\.$");

    /**
     * Whether the stopwatch is currently running
     * 秒表是否正在运行
     */
    private boolean isRunning;

    /**
     * Total elapsed nanoseconds (accumulated when stopped)
     * 已累计的纳秒数（停止时累加）
     */
    private long elapsedNanos;

    /**
     * Start timestamp in nanoseconds (only valid when running)
     * 开始时间戳纳秒数（仅运行时有效）
     */
    private long startNanos;

    /**
     * Private constructor to enforce factory method usage
     * 私有构造器，强制使用工厂方法
     */
    private Stopwatch() {
        this.isRunning = false;
        this.elapsedNanos = 0;
        this.startNanos = 0;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an unstarted stopwatch
     * 创建未启动的秒表
     *
     * @return a new unstarted stopwatch
     */
    public static Stopwatch createUnstarted() {
        return new Stopwatch();
    }

    /**
     * Creates and starts a stopwatch
     * 创建并启动秒表
     *
     * @return a new started stopwatch
     */
    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    // ==================== Control Methods | 控制方法 ====================

    /**
     * Starts or resumes the stopwatch
     * 启动或恢复秒表
     *
     * @return this stopwatch for fluent chaining
     * @throws IllegalStateException if the stopwatch is already running
     */
    public Stopwatch start() {
        if (isRunning) {
            throw new IllegalStateException("Stopwatch is already running");
        }
        isRunning = true;
        startNanos = System.nanoTime();
        return this;
    }

    /**
     * Stops the stopwatch
     * 停止秒表
     *
     * @return this stopwatch for fluent chaining
     * @throws IllegalStateException if the stopwatch is not running
     */
    public Stopwatch stop() {
        if (!isRunning) {
            throw new IllegalStateException("Stopwatch is not running");
        }
        long now = System.nanoTime();
        elapsedNanos += now - startNanos;
        isRunning = false;
        return this;
    }

    /**
     * Resets the stopwatch to zero and stops it
     * 重置秒表为零并停止
     *
     * @return this stopwatch for fluent chaining
     */
    public Stopwatch reset() {
        elapsedNanos = 0;
        isRunning = false;
        startNanos = 0;
        return this;
    }

    // ==================== Query Methods | 查询方法 ====================

    /**
     * Returns whether the stopwatch is currently running
     * 返回秒表是否正在运行
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns the elapsed time as a Duration
     * 返回经过的时间（Duration 格式）
     *
     * @return the elapsed duration
     */
    public Duration elapsed() {
        return Duration.ofNanos(elapsedNanos());
    }

    /**
     * Returns the elapsed time in the specified time unit
     * 返回指定时间单位的经过时间
     *
     * @param unit the desired time unit
     * @return the elapsed time in the specified unit
     */
    public long elapsed(TimeUnit unit) {
        return unit.convert(elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the elapsed time in nanoseconds
     * 返回经过的纳秒数
     *
     * @return elapsed nanoseconds
     */
    public long elapsedNanos() {
        return isRunning ? elapsedNanos + (System.nanoTime() - startNanos) : elapsedNanos;
    }

    /**
     * Returns the elapsed time in milliseconds
     * 返回经过的毫秒数
     *
     * @return elapsed milliseconds
     */
    public long elapsedMillis() {
        return elapsed(TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the elapsed time in seconds
     * 返回经过的秒数
     *
     * @return elapsed seconds
     */
    public long elapsedSeconds() {
        return elapsed(TimeUnit.SECONDS);
    }

    // ==================== toString | 字符串表示 ====================

    /**
     * Returns a human-readable string representation of the elapsed time
     * 返回人类可读的经过时间字符串
     *
     * <p>The time unit is automatically selected based on the magnitude:</p>
     * <ul>
     *   <li>Less than 1 microsecond: nanoseconds (e.g., "123 ns")</li>
     *   <li>Less than 1 millisecond: microseconds (e.g., "123.4 μs")</li>
     *   <li>Less than 1 second: milliseconds (e.g., "123.4 ms")</li>
     *   <li>Less than 1 minute: seconds (e.g., "12.34 s")</li>
     *   <li>Otherwise: minutes (e.g., "1.23 min")</li>
     * </ul>
     *
     * @return a formatted string representation
     */
    @Override
    public String toString() {
        long nanos = elapsedNanos();

        // Less than 1 microsecond
        if (nanos < 1_000L) {
            return nanos + " ns";
        }

        // Less than 1 millisecond
        if (nanos < 1_000_000L) {
            return formatDouble(nanos / 1_000.0) + " μs";
        }

        // Less than 1 second
        if (nanos < 1_000_000_000L) {
            return formatDouble(nanos / 1_000_000.0) + " ms";
        }

        // Less than 1 minute
        if (nanos < 60_000_000_000L) {
            return formatDouble(nanos / 1_000_000_000.0) + " s";
        }

        // Minutes
        return formatDouble(nanos / 60_000_000_000.0) + " min";
    }

    /**
     * Formats a double value, removing trailing zeros
     */
    private static String formatDouble(double value) {
        // Use up to 4 significant digits
        String formatted = String.format("%.4g", value);
        // Remove trailing zeros after decimal point
        if (formatted.contains(".")) {
            formatted = TRAILING_ZEROS_PATTERN.matcher(formatted).replaceAll("");
            formatted = TRAILING_DOT_PATTERN.matcher(formatted).replaceAll("");
        }
        return formatted;
    }
}
