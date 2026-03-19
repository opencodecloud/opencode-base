package cloud.opencode.base.log.perf;

import cloud.opencode.base.log.LogLevel;

import java.time.Duration;
import java.util.Objects;

/**
 * Slow Operation Config - Configuration for Slow Operation Detection
 * 慢操作配置 - 慢操作检测配置
 *
 * <p>This class defines the configuration for detecting and logging slow operations.
 * It allows setting thresholds, log levels, and optional stack trace inclusion.</p>
 * <p>此类定义检测和记录慢操作的配置。它允许设置阈值、日志级别和可选的堆栈跟踪包含。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * SlowOperationConfig config = SlowOperationConfig.builder()
 *     .threshold(Duration.ofMillis(500))
 *     .logLevel(LogLevel.WARN)
 *     .includeStackTrace(true)
 *     .build();
 *
 * PerfLog.setSlowOperationConfig(config);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable slow operation threshold - 可配置的慢操作阈值</li>
 *   <li>Configurable log level for slow operations - 可配置的慢操作日志级别</li>
 *   <li>Optional stack trace inclusion - 可选的堆栈跟踪包含</li>
 *   <li>Enable/disable toggle - 启用/禁用开关</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (throws on null threshold/logLevel) - 空值安全: 否（null 阈值/日志级别抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class SlowOperationConfig {

    /** Default threshold: 1 second */
    public static final Duration DEFAULT_THRESHOLD = Duration.ofSeconds(1);

    /** Default log level: WARN */
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.WARN;

    /** Default config instance */
    public static final SlowOperationConfig DEFAULT = builder().build();

    private final Duration threshold;
    private final LogLevel logLevel;
    private final boolean includeStackTrace;
    private final boolean enabled;

    private SlowOperationConfig(Builder builder) {
        this.threshold = builder.threshold;
        this.logLevel = builder.logLevel;
        this.includeStackTrace = builder.includeStackTrace;
        this.enabled = builder.enabled;
    }

    /**
     * Returns the slow operation threshold.
     * 返回慢操作阈值。
     *
     * @return the threshold - 阈值
     */
    public Duration getThreshold() {
        return threshold;
    }

    /**
     * Returns the threshold in milliseconds.
     * 返回以毫秒为单位的阈值。
     *
     * @return threshold in milliseconds - 毫秒阈值
     */
    public long getThresholdMillis() {
        return threshold.toMillis();
    }

    /**
     * Returns the log level for slow operations.
     * 返回慢操作的日志级别。
     *
     * @return the log level - 日志级别
     */
    public LogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Returns whether to include stack trace.
     * 返回是否包含堆栈跟踪。
     *
     * @return true to include stack trace - 如果包含堆栈跟踪返回 true
     */
    public boolean isIncludeStackTrace() {
        return includeStackTrace;
    }

    /**
     * Returns whether slow operation detection is enabled.
     * 返回慢操作检测是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if the elapsed time exceeds the threshold.
     * 检查耗时是否超过阈值。
     *
     * @param elapsedMillis the elapsed time in milliseconds - 毫秒耗时
     * @return true if slow - 如果慢返回 true
     */
    public boolean isSlow(long elapsedMillis) {
        return enabled && elapsedMillis > threshold.toMillis();
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("SlowOperationConfig[threshold=%s, logLevel=%s, stackTrace=%s, enabled=%s]",
                threshold, logLevel, includeStackTrace, enabled);
    }

    /**
     * Builder for SlowOperationConfig.
     * SlowOperationConfig 的构建器。
     */
    public static final class Builder {
        private Duration threshold = DEFAULT_THRESHOLD;
        private LogLevel logLevel = DEFAULT_LOG_LEVEL;
        private boolean includeStackTrace = false;
        private boolean enabled = true;

        private Builder() {
        }

        /**
         * Sets the threshold.
         * 设置阈值。
         *
         * @param threshold the threshold - 阈值
         * @return this builder - 此构建器
         */
        public Builder threshold(Duration threshold) {
            this.threshold = Objects.requireNonNull(threshold);
            return this;
        }

        /**
         * Sets the threshold in milliseconds.
         * 设置毫秒阈值。
         *
         * @param millis the threshold in milliseconds - 毫秒阈值
         * @return this builder - 此构建器
         */
        public Builder thresholdMillis(long millis) {
            this.threshold = Duration.ofMillis(millis);
            return this;
        }

        /**
         * Sets the log level.
         * 设置日志级别。
         *
         * @param logLevel the log level - 日志级别
         * @return this builder - 此构建器
         */
        public Builder logLevel(LogLevel logLevel) {
            this.logLevel = Objects.requireNonNull(logLevel);
            return this;
        }

        /**
         * Sets whether to include stack trace.
         * 设置是否包含堆栈跟踪。
         *
         * @param include true to include - 如果包含则为 true
         * @return this builder - 此构建器
         */
        public Builder includeStackTrace(boolean include) {
            this.includeStackTrace = include;
            return this;
        }

        /**
         * Enables slow operation detection.
         * 启用慢操作检测。
         *
         * @return this builder - 此构建器
         */
        public Builder enabled() {
            this.enabled = true;
            return this;
        }

        /**
         * Disables slow operation detection.
         * 禁用慢操作检测。
         *
         * @return this builder - 此构建器
         */
        public Builder disabled() {
            this.enabled = false;
            return this;
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the config - 配置
         */
        public SlowOperationConfig build() {
            return new SlowOperationConfig(this);
        }
    }
}
