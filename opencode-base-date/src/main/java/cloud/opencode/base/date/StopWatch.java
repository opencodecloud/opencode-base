package cloud.opencode.base.date;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * High-precision StopWatch for performance timing
 * 高精度计时器，用于性能计时
 *
 * <p>This class provides a simple way to time code execution with support for multiple
 * laps, task naming, and formatted output. It uses System.nanoTime() for high precision.</p>
 * <p>此类提供一种简单的方式来计时代码执行，支持多圈、任务命名和格式化输出。
 * 使用System.nanoTime()以获得高精度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High-precision timing using nanoTime - 使用nanoTime高精度计时</li>
 *   <li>Multiple named tasks/laps - 多个命名任务/圈</li>
 *   <li>Formatted output - 格式化输出</li>
 *   <li>Thread-safe for single-threaded use - 单线程使用时线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple timing
 * StopWatch sw = StopWatch.start();
 * // ... do something
 * Duration elapsed = sw.stop();
 * System.out.println("Elapsed: " + sw.formatTime());
 *
 * // Named tasks
 * StopWatch sw = new StopWatch("My Process");
 * sw.start("Task 1");
 * // ... task 1
 * sw.stop();
 * sw.start("Task 2");
 * // ... task 2
 * sw.stop();
 * System.out.println(sw.prettyPrint());
 *
 * // Quick timing with lambda
 * Duration elapsed = StopWatch.time(() -> {
 *     // ... operation
 * });
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Nanosecond precision - 纳秒精度</li>
 *   <li>Minimal overhead - 最小开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Not thread-safe for concurrent access - 不支持并发访问</li>
 *   <li>Thread-safe for single-threaded use - 单线程使用时线程安全</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public class StopWatch {

    /**
     * The name of this stopwatch
     */
    private final String name;

    /**
     * List of completed tasks
     */
    private final List<TaskInfo> tasks;

    /**
     * Name of the current task
     */
    private String currentTaskName;

    /**
     * Start time of the current task in nanos
     */
    private long startTimeNanos;

    /**
     * Whether the stopwatch is currently running
     */
    private boolean running;

    /**
     * Total time in nanoseconds
     */
    private long totalTimeNanos;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Creates a StopWatch with no name
     * 创建无名称的StopWatch
     */
    public StopWatch() {
        this("");
    }

    /**
     * Creates a StopWatch with the specified name
     * 创建具有指定名称的StopWatch
     *
     * @param name the name | 名称
     */
    public StopWatch(String name) {
        this.name = Objects.requireNonNullElse(name, "");
        this.tasks = new ArrayList<>();
        this.running = false;
        this.totalTimeNanos = 0;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates and starts a StopWatch
     * 创建并启动StopWatch
     *
     * @return the started StopWatch | 已启动的StopWatch
     */
    public static StopWatch createStarted() {
        StopWatch sw = new StopWatch();
        sw.start("");
        return sw;
    }

    /**
     * Creates and starts a named StopWatch
     * 创建并启动命名的StopWatch
     *
     * @param name the name | 名称
     * @return the started StopWatch | 已启动的StopWatch
     */
    public static StopWatch createStarted(String name) {
        StopWatch sw = new StopWatch(name);
        sw.start(name);
        return sw;
    }

    /**
     * Times the execution of a Runnable
     * 计时Runnable的执行
     *
     * @param runnable the runnable to time | 要计时的Runnable
     * @return the elapsed duration | 经过的时长
     */
    public static Duration time(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        StopWatch sw = createStarted();
        try {
            runnable.run();
        } finally {
            sw.stop();
        }
        return sw.getTotalDuration();
    }

    /**
     * Times the execution of a Runnable with a task name
     * 使用任务名称计时Runnable的执行
     *
     * @param taskName the task name | 任务名称
     * @param runnable the runnable to time | 要计时的Runnable
     * @return the elapsed duration | 经过的时长
     */
    public static Duration time(String taskName, Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        StopWatch sw = new StopWatch();
        sw.start(taskName);
        try {
            runnable.run();
        } finally {
            sw.stop();
        }
        return sw.getTotalDuration();
    }

    // ==================== Control Methods | 控制方法 ====================

    /**
     * Starts timing a new task
     * 开始计时新任务
     *
     * @param taskName the task name | 任务名称
     * @throws IllegalStateException if already running | 如果已在运行则抛出异常
     */
    public void start(String taskName) {
        if (running) {
            throw new IllegalStateException("StopWatch is already running. Call stop() before starting a new task.");
        }
        this.currentTaskName = Objects.requireNonNullElse(taskName, "");
        this.running = true;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Stops timing the current task
     * 停止计时当前任务
     *
     * @return the elapsed duration | 经过的时长
     * @throws IllegalStateException if not running | 如果未运行则抛出异常
     */
    public Duration stop() {
        if (!running) {
            throw new IllegalStateException("StopWatch is not running. Call start() first.");
        }
        long endTimeNanos = System.nanoTime();
        long elapsedNanos = endTimeNanos - startTimeNanos;
        this.totalTimeNanos += elapsedNanos;
        this.running = false;

        TaskInfo taskInfo = new TaskInfo(currentTaskName, elapsedNanos);
        tasks.add(taskInfo);

        return Duration.ofNanos(elapsedNanos);
    }

    /**
     * Resets the stopwatch
     * 重置计时器
     */
    public void reset() {
        this.tasks.clear();
        this.running = false;
        this.totalTimeNanos = 0;
        this.startTimeNanos = 0;
        this.currentTaskName = null;
    }

    /**
     * Splits the current lap and starts a new one
     * 分割当前圈并开始新的一圈
     *
     * @param newTaskName the name for the new task | 新任务的名称
     * @return the elapsed duration of the previous task | 上一个任务的经过时长
     */
    public Duration split(String newTaskName) {
        Duration elapsed = stop();
        start(newTaskName);
        return elapsed;
    }

    // ==================== Status Methods | 状态方法 ====================

    /**
     * Checks if the stopwatch is currently running
     * 检查计时器是否正在运行
     *
     * @return true if running | 如果正在运行返回true
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gets the name of this stopwatch
     * 获取计时器的名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of completed tasks
     * 获取已完成任务的数量
     *
     * @return the task count | 任务数量
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Gets the list of completed tasks
     * 获取已完成任务的列表
     *
     * @return the task list | 任务列表
     */
    public List<TaskInfo> getTasks() {
        return List.copyOf(tasks);
    }

    // ==================== Time Getters | 时间获取 ====================

    /**
     * Gets the total elapsed time in nanoseconds
     * 获取总经过时间（纳秒）
     *
     * @return the total time in nanos | 总纳秒时间
     */
    public long getTotalTimeNanos() {
        return totalTimeNanos;
    }

    /**
     * Gets the total elapsed time in milliseconds
     * 获取总经过时间（毫秒）
     *
     * @return the total time in millis | 总毫秒时间
     */
    public long getTotalTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(totalTimeNanos);
    }

    /**
     * Gets the total elapsed time in seconds
     * 获取总经过时间（秒）
     *
     * @return the total time in seconds | 总秒时间
     */
    public double getTotalTimeSeconds() {
        return totalTimeNanos / 1_000_000_000.0;
    }

    /**
     * Gets the total elapsed time as Duration
     * 获取总经过时间作为Duration
     *
     * @return the total Duration | 总Duration
     */
    public Duration getTotalDuration() {
        return Duration.ofNanos(totalTimeNanos);
    }

    /**
     * Gets the current elapsed time in nanoseconds (if running)
     * 获取当前经过时间（纳秒，如果正在运行）
     *
     * @return the current elapsed time, or total time if stopped | 当前经过时间，如果已停止则返回总时间
     */
    public long getCurrentTimeNanos() {
        if (running) {
            return System.nanoTime() - startTimeNanos + totalTimeNanos;
        }
        return totalTimeNanos;
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Formats the total time as a human-readable string
     * 将总时间格式化为人类可读的字符串
     *
     * @return the formatted time | 格式化的时间
     */
    public String formatTime() {
        return formatDuration(totalTimeNanos);
    }

    /**
     * Creates a pretty-printed summary of all tasks
     * 创建所有任务的漂亮打印摘要
     *
     * @return the summary string | 摘要字符串
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();

        if (!name.isEmpty()) {
            sb.append("StopWatch '").append(name).append("'");
        } else {
            sb.append("StopWatch");
        }
        sb.append(": ").append(formatTime()).append("\n");

        if (tasks.isEmpty()) {
            sb.append("  No tasks recorded\n");
            return sb.toString();
        }

        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("%-20s %15s %10s%n", "Task", "Time", "Percent"));
        sb.append("-".repeat(60)).append("\n");

        for (TaskInfo task : tasks) {
            double percent = totalTimeNanos > 0 ? (task.timeNanos() * 100.0 / totalTimeNanos) : 0;
            sb.append(String.format("%-20s %15s %9.1f%%%n",
                    truncate(task.name(), 20),
                    formatDuration(task.timeNanos()),
                    percent));
        }

        sb.append("-".repeat(60)).append("\n");
        return sb.toString();
    }

    /**
     * Creates a short summary string
     * 创建简短的摘要字符串
     *
     * @return the short summary | 简短摘要
     */
    public String shortSummary() {
        if (name.isEmpty()) {
            return "StopWatch: " + formatTime();
        }
        return "StopWatch '" + name + "': " + formatTime();
    }

    @Override
    public String toString() {
        return shortSummary();
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Formats a duration in nanoseconds to a human-readable string
     */
    private static String formatDuration(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        }
        if (nanos < 1_000_000) {
            return String.format("%.2f µs", nanos / 1_000.0);
        }
        if (nanos < 1_000_000_000) {
            return String.format("%.2f ms", nanos / 1_000_000.0);
        }
        if (nanos < 60_000_000_000L) {
            return String.format("%.2f s", nanos / 1_000_000_000.0);
        }
        long seconds = nanos / 1_000_000_000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%d m %d s", minutes, seconds);
        }
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%d h %d m %d s", hours, minutes, seconds);
    }

    /**
     * Truncates a string to the specified length
     */
    private static String truncate(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    // ==================== Task Info Record | 任务信息记录 ====================

    /**
     * Information about a timed task
     * 计时任务的信息
     *
     * @param name      the task name | 任务名称
     * @param timeNanos the elapsed time in nanoseconds | 经过时间（纳秒）
     */
    public record TaskInfo(String name, long timeNanos) {

        /**
         * Gets the elapsed time in milliseconds
         * 获取经过时间（毫秒）
         *
         * @return the time in millis | 毫秒时间
         */
        public long timeMillis() {
            return TimeUnit.NANOSECONDS.toMillis(timeNanos);
        }

        /**
         * Gets the elapsed time in seconds
         * 获取经过时间（秒）
         *
         * @return the time in seconds | 秒时间
         */
        public double timeSeconds() {
            return timeNanos / 1_000_000_000.0;
        }

        /**
         * Gets the elapsed time as Duration
         * 获取经过时间作为Duration
         *
         * @return the Duration | Duration
         */
        public Duration duration() {
            return Duration.ofNanos(timeNanos);
        }

        @Override
        public String toString() {
            return name + ": " + formatDuration(timeNanos);
        }
    }
}
