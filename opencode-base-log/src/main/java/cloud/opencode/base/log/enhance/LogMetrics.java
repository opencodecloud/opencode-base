/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Log Metrics - Logging with Built-in Metrics Collection
 * 日志指标 - 带内置指标收集的日志
 *
 * <p>Provides logging methods that simultaneously record metrics,
 * useful for integrating logging with monitoring systems.</p>
 * <p>提供同时记录指标的日志方法，用于将日志与监控系统集成。</p>
 *
 * <p><strong>Features | 功能:</strong></p>
 * <ul>
 *   <li>Log and count events - 记录日志并计数事件</li>
 *   <li>Log and time operations - 记录日志并计时操作</li>
 *   <li>Retrieve log statistics - 获取日志统计</li>
 *   <li>Thread-safe counters - 线程安全计数器</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Log and count
 * LogMetrics.errorAndCount("payment.failed", "Payment failed: orderId={}", orderId);
 * // Records ERROR log and increments payment.failed counter
 *
 * // Log and time
 * Object result = LogMetrics.infoAndTime("db.query", "Execute query", () -> {
 *     return database.query(sql);
 * });
 * // Records INFO log with duration and tracks db.query timing
 *
 * // Get statistics
 * LogStats stats = LogMetrics.getStats();
 * System.out.println("Total errors: " + stats.errorCount());
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class LogMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogMetrics.class);

    // Counters
    private static final LongAdder TOTAL_LOGS = new LongAdder();
    private static final LongAdder ERROR_COUNT = new LongAdder();
    private static final LongAdder WARN_COUNT = new LongAdder();
    private static final LongAdder INFO_COUNT = new LongAdder();
    private static final LongAdder DEBUG_COUNT = new LongAdder();

    // Named counters for specific metrics
    private static final ConcurrentHashMap<String, LongAdder> NAMED_COUNTERS = new ConcurrentHashMap<>();

    // Timing metrics
    private static final ConcurrentHashMap<String, TimingStats> TIMING_STATS = new ConcurrentHashMap<>();

    private LogMetrics() {
    }

    // ==================== Log and Count | 记录并计数 ====================

    /**
     * Logs at DEBUG level and increments counter
     * 以 DEBUG 级别记录日志并增加计数器
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param args       the arguments | 参数
     */
    public static void debugAndCount(String metricName, String message, Object... args) {
        LOGGER.debug(message, args);
        TOTAL_LOGS.increment();
        DEBUG_COUNT.increment();
        incrementCounter(metricName);
    }

    /**
     * Logs at INFO level and increments counter
     * 以 INFO 级别记录日志并增加计数器
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param args       the arguments | 参数
     */
    public static void infoAndCount(String metricName, String message, Object... args) {
        LOGGER.info(message, args);
        TOTAL_LOGS.increment();
        INFO_COUNT.increment();
        incrementCounter(metricName);
    }

    /**
     * Logs at WARN level and increments counter
     * 以 WARN 级别记录日志并增加计数器
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param args       the arguments | 参数
     */
    public static void warnAndCount(String metricName, String message, Object... args) {
        LOGGER.warn(message, args);
        TOTAL_LOGS.increment();
        WARN_COUNT.increment();
        incrementCounter(metricName);
    }

    /**
     * Logs at ERROR level and increments counter
     * 以 ERROR 级别记录日志并增加计数器
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param args       the arguments | 参数
     */
    public static void errorAndCount(String metricName, String message, Object... args) {
        LOGGER.error(message, args);
        TOTAL_LOGS.increment();
        ERROR_COUNT.increment();
        incrementCounter(metricName);
    }

    /**
     * Logs at ERROR level with exception and increments counter
     * 以 ERROR 级别带异常记录日志并增加计数器
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param throwable  the exception | 异常
     */
    public static void errorAndCount(String metricName, String message, Throwable throwable) {
        LOGGER.error(message, throwable);
        TOTAL_LOGS.increment();
        ERROR_COUNT.increment();
        incrementCounter(metricName);
    }

    // ==================== Log and Time | 记录并计时 ====================

    /**
     * Logs at DEBUG level with timing and records duration metric
     * 以 DEBUG 级别带计时记录日志并记录耗时指标
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param task       the task to execute | 要执行的任务
     * @param <T>        the result type | 结果类型
     * @return the task result | 任务结果
     * @throws Exception if task fails | 如果任务失败
     */
    public static <T> T debugAndTime(String metricName, String message, Callable<T> task) throws Exception {
        long start = System.nanoTime();
        try {
            T result = task.call();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.debug("{} completed in {}ms", message, elapsed);
            recordTiming(metricName, elapsed);
            TOTAL_LOGS.increment();
            DEBUG_COUNT.increment();
            return result;
        } catch (Exception e) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.debug("{} failed in {}ms: {}", message, elapsed, e.getMessage());
            recordTiming(metricName + ".failed", elapsed);
            TOTAL_LOGS.increment();
            DEBUG_COUNT.increment();
            throw e;
        }
    }

    /**
     * Logs at INFO level with timing and records duration metric
     * 以 INFO 级别带计时记录日志并记录耗时指标
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param task       the task to execute | 要执行的任务
     * @param <T>        the result type | 结果类型
     * @return the task result | 任务结果
     * @throws Exception if task fails | 如果任务失败
     */
    public static <T> T infoAndTime(String metricName, String message, Callable<T> task) throws Exception {
        long start = System.nanoTime();
        try {
            T result = task.call();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info("{} completed in {}ms", message, elapsed);
            recordTiming(metricName, elapsed);
            TOTAL_LOGS.increment();
            INFO_COUNT.increment();
            return result;
        } catch (Exception e) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.error("{} failed in {}ms", message, elapsed, e);
            recordTiming(metricName + ".failed", elapsed);
            TOTAL_LOGS.increment();
            ERROR_COUNT.increment();
            throw e;
        }
    }

    /**
     * Logs at INFO level with timing (void return)
     * 以 INFO 级别带计时记录日志（无返回值）
     *
     * @param metricName the metric name | 指标名
     * @param message    the log message | 日志消息
     * @param task       the task to execute | 要执行的任务
     */
    public static void infoAndTime(String metricName, String message, Runnable task) {
        long start = System.nanoTime();
        try {
            task.run();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info("{} completed in {}ms", message, elapsed);
            recordTiming(metricName, elapsed);
            TOTAL_LOGS.increment();
            INFO_COUNT.increment();
        } catch (RuntimeException e) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            LOGGER.error("{} failed in {}ms", message, elapsed, e);
            recordTiming(metricName + ".failed", elapsed);
            TOTAL_LOGS.increment();
            ERROR_COUNT.increment();
            throw e;
        }
    }

    // ==================== Counter Management | 计数器管理 ====================

    private static void incrementCounter(String name) {
        NAMED_COUNTERS.computeIfAbsent(name, _ -> new LongAdder()).increment();
    }

    private static void recordTiming(String name, long millis) {
        TIMING_STATS.computeIfAbsent(name, _ -> new TimingStats()).record(millis);
    }

    /**
     * Gets the count for a specific metric
     * 获取特定指标的计数
     *
     * @param metricName the metric name | 指标名
     * @return the count | 计数
     */
    public static long getCount(String metricName) {
        LongAdder counter = NAMED_COUNTERS.get(metricName);
        return counter != null ? counter.sum() : 0;
    }

    /**
     * Gets timing statistics for a specific metric
     * 获取特定指标的计时统计
     *
     * @param metricName the metric name | 指标名
     * @return the timing stats or null | 计时统计或 null
     */
    public static TimingStats getTimingStats(String metricName) {
        return TIMING_STATS.get(metricName);
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Gets overall log statistics
     * 获取整体日志统计
     *
     * @return log stats | 日志统计
     */
    public static LogStats getStats() {
        return new LogStats(
                TOTAL_LOGS.sum(),
                ERROR_COUNT.sum(),
                WARN_COUNT.sum(),
                INFO_COUNT.sum(),
                DEBUG_COUNT.sum(),
                Map.copyOf(NAMED_COUNTERS.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().sum()
                        )))
        );
    }

    /**
     * Resets all statistics (useful for testing)
     * 重置所有统计（用于测试）
     */
    public static void reset() {
        TOTAL_LOGS.reset();
        ERROR_COUNT.reset();
        WARN_COUNT.reset();
        INFO_COUNT.reset();
        DEBUG_COUNT.reset();
        NAMED_COUNTERS.clear();
        TIMING_STATS.clear();
    }

    // ==================== Record Types | 记录类型 ====================

    /**
     * Log Statistics Snapshot
     * 日志统计快照
     *
     * @param totalLogs    total log count | 总日志计数
     * @param errorCount   error count | 错误计数
     * @param warnCount    warn count | 警告计数
     * @param infoCount    info count | 信息计数
     * @param debugCount   debug count | 调试计数
     * @param namedCounts  named metric counts | 命名指标计数
     */
    public record LogStats(
            long totalLogs,
            long errorCount,
            long warnCount,
            long infoCount,
            long debugCount,
            Map<String, Long> namedCounts
    ) {
        /**
         * Gets count for a named metric
         * 获取命名指标的计数
         *
         * @param name the metric name | 指标名
         * @return the count | 计数
         */
        public long getNamedCount(String name) {
            return namedCounts.getOrDefault(name, 0L);
        }

        @Override
        public String toString() {
            return String.format(
                    "LogStats[total=%d, error=%d, warn=%d, info=%d, debug=%d, named=%d]",
                    totalLogs, errorCount, warnCount, infoCount, debugCount, namedCounts.size()
            );
        }
    }

    /**
     * Timing Statistics
     * 计时统计
     */
    public static class TimingStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalMillis = new LongAdder();
        private final AtomicLong minMillis = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxMillis = new AtomicLong(0);

        public void record(long millis) {
            count.increment();
            totalMillis.add(millis);
            minMillis.accumulateAndGet(millis, Math::min);
            maxMillis.accumulateAndGet(millis, Math::max);
        }

        public long getCount() {
            return count.sum();
        }

        public long getTotalMillis() {
            return totalMillis.sum();
        }

        public double getAverageMillis() {
            long c = count.sum();
            return c > 0 ? (double) totalMillis.sum() / c : 0;
        }

        public long getMinMillis() {
            long min = minMillis.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }

        public long getMaxMillis() {
            return maxMillis.get();
        }

        @Override
        public String toString() {
            return String.format(
                    "TimingStats[count=%d, avg=%.2fms, min=%dms, max=%dms]",
                    getCount(), getAverageMillis(), getMinMillis(), getMaxMillis()
            );
        }
    }
}
