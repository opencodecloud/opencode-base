package cloud.opencode.base.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import cloud.opencode.base.core.tuple.Pair;

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
 *   <li>Suspend and resume without reset (suspend, resume) - 暂停和恢复（不重置）</li>
 *   <li>Lap/split timing (split, getLaps) - 计次/分段计时</li>
 *   <li>One-liner timing (time) - 一行代码计时</li>
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
 *
 * // Suspend/Resume | 暂停/恢复
 * sw.suspend();
 * // ... pause ...
 * sw.resume();
 *
 * // Lap timing | 计次
 * Stopwatch sw = Stopwatch.createStarted();
 * // ... phase 1 ...
 * Duration lap1 = sw.split();
 * // ... phase 2 ...
 * Duration lap2 = sw.split();
 * List<Duration> laps = sw.getLaps();  // [lap1, lap2]
 *
 * // One-liner timing | 一行代码计时
 * Duration elapsed = Stopwatch.time(() -> heavyComputation());
 * var timed = Stopwatch.time(() -> fetchData());  // Pair<Result, Duration>
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
     * Lap durations (recorded via split())
     * 计次时间记录（通过 split() 记录）
     */
    private final List<Duration> laps = new ArrayList<>();

    /**
     * Logical elapsed nanos at last split point (tracks accumulated time, not wall-clock)
     * 上次分段点的逻辑累计纳秒数（追踪累计时间，非墙钟时间）
     */
    private long lastSplitElapsedNanos;

    /**
     * Private constructor to enforce factory method usage
     * 私有构造器，强制使用工厂方法
     */
    private Stopwatch() {
        this.isRunning = false;
        this.elapsedNanos = 0;
        this.startNanos = 0;
        this.lastSplitElapsedNanos = 0;
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

    /**
     * Times the execution of a callable and returns both the result and elapsed duration.
     * 计时执行 Callable 并返回结果和经过的时间。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * var timed = Stopwatch.time(() -> fetchData());
     * System.out.println("Result: " + timed.left() + ", took " + timed.right());
     * }</pre>
     *
     * @param task the callable to time | 待计时的任务
     * @param <T>  the result type | 结果类型
     * @return a Pair of (result, elapsed duration) | (结果, 经过时间) 的 Pair
     * @throws Exception if the callable throws | 如果任务抛出异常
     */
    public static <T> Pair<T, Duration> time(Callable<T> task) throws Exception {
        Stopwatch sw = createStarted();
        T result = task.call();
        sw.stop();
        return Pair.of(result, sw.elapsed());
    }

    /**
     * Times the execution of a runnable and returns the elapsed duration.
     * 计时执行 Runnable 并返回经过的时间。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * Duration elapsed = Stopwatch.time(() -> heavyComputation());
     * }</pre>
     *
     * @param task the runnable to time | 待计时的任务
     * @return the elapsed duration | 经过的时间
     */
    public static Duration time(Runnable task) {
        Stopwatch sw = createStarted();
        task.run();
        sw.stop();
        return sw.elapsed();
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
        laps.clear();
        lastSplitElapsedNanos = 0;
        return this;
    }

    // ==================== Suspend & Resume | 暂停与恢复 ====================

    /**
     * Suspends the stopwatch without resetting the elapsed time.
     * 暂停秒表但不重置已经过的时间。
     *
     * <p>The stopwatch can be resumed later with {@link #resume()}. The elapsed time
     * between suspend and resume is not counted.</p>
     * <p>稍后可以通过 {@link #resume()} 恢复。暂停和恢复之间的时间不计入。</p>
     *
     * @return this stopwatch for fluent chaining | 此秒表，支持链式调用
     * @throws IllegalStateException if the stopwatch is not running | 如果秒表未运行
     */
    public Stopwatch suspend() {
        return stop();  // stop already accumulates elapsed and sets isRunning=false
    }

    /**
     * Resumes a suspended stopwatch.
     * 恢复已暂停的秒表。
     *
     * <p>Equivalent to calling {@link #start()} on a stopped (but not reset) stopwatch.</p>
     * <p>等同于在已停止（但未重置）的秒表上调用 {@link #start()}。</p>
     *
     * @return this stopwatch for fluent chaining | 此秒表，支持链式调用
     * @throws IllegalStateException if the stopwatch is already running | 如果秒表已在运行
     */
    public Stopwatch resume() {
        return start();  // start sets isRunning=true and records new startNanos
    }

    // ==================== Lap / Split | 计次 ====================

    /**
     * Records a lap (split) time without stopping the stopwatch.
     * 记录一个计次（分段）时间，不停止秒表。
     *
     * <p>Returns the duration since the last split (or since start if no previous split).
     * The stopwatch continues running.</p>
     * <p>返回自上次分段以来的时间（如果没有上次分段，则返回自启动以来的时间）。
     * 秒表继续运行。</p>
     *
     * @return the lap duration | 本次计次的时间
     * @throws IllegalStateException if the stopwatch is not running | 如果秒表未运行
     */
    public Duration split() {
        if (!isRunning) {
            throw new IllegalStateException("Stopwatch is not running");
        }
        long currentElapsed = elapsedNanos();
        long lapNanos = currentElapsed - lastSplitElapsedNanos;
        lastSplitElapsedNanos = currentElapsed;
        Duration lap = Duration.ofNanos(lapNanos);
        laps.add(lap);
        return lap;
    }

    /**
     * Returns all recorded lap durations as an unmodifiable list.
     * 返回所有已记录的计次时间（不可修改列表）。
     *
     * @return the list of lap durations | 计次时间列表
     */
    public List<Duration> getLaps() {
        return Collections.unmodifiableList(new ArrayList<>(laps));
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
