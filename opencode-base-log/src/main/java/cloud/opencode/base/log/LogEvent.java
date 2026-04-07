package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Marker;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Log Event Record - Immutable Representation of a Log Entry
 * 日志事件记录 - 日志条目的不可变表示
 *
 * <p>Encapsulates all information about a single log event, including the log level,
 * message, optional throwable, marker, MDC context, timestamp, thread name, and caller info.</p>
 * <p>封装单个日志事件的所有信息，包括日志级别、消息、可选异常、标记、MDC 上下文、
 * 时间戳、线程名和调用者信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable log event representation - 不可变的日志事件表示</li>
 *   <li>Fluent builder for convenient construction - 流式构建器方便构造</li>
 *   <li>Defensive copy of MDC context map - MDC 上下文映射的防御性拷贝</li>
 *   <li>Formatted string output - 格式化字符串输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a log event
 * LogEvent event = LogEvent.builder(LogLevel.INFO, "User logged in")
 *     .loggerName("com.example.UserService")
 *     .throwable(null)
 *     .build();
 *
 * // Check event properties
 * if (event.hasThrowable()) {
 *     // handle throwable
 * }
 *
 * // Format for output
 * String formatted = event.toFormattedString();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partially (level and message required) - 空值安全: 部分（level 和 message 必填）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public record LogEvent(
        LogLevel level,
        String loggerName,
        String message,
        Throwable throwable,
        Marker marker,
        Map<String, String> mdc,
        long timestampMillis,
        String threadName,
        CallerInfo callerInfo
) {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    /**
     * Compact constructor that validates required fields and makes a defensive copy of the MDC map.
     * 紧凑构造函数，验证必填字段并对 MDC 映射进行防御性拷贝。
     *
     * @param level          the log level | 日志级别
     * @param loggerName     the logger name | 日志记录器名称
     * @param message        the log message | 日志消息
     * @param throwable      the optional throwable | 可选异常
     * @param marker         the optional marker | 可选标记
     * @param mdc            the MDC context map (defensively copied) | MDC 上下文映射（防御性拷贝）
     * @param timestampMillis the event timestamp in milliseconds | 事件时间戳（毫秒）
     * @param threadName     the thread name | 线程名
     * @param callerInfo     the caller info | 调用者信息
     */
    public LogEvent {
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (threadName == null) {
            threadName = Thread.currentThread().getName();
        }
        if (callerInfo == null) {
            callerInfo = CallerInfo.UNKNOWN;
        }
        mdc = mdc == null
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(mdc));
    }

    /**
     * Creates a new builder with the required level and message.
     * 使用必填的级别和消息创建新的构建器。
     *
     * @param level   the log level | 日志级别
     * @param message the log message | 日志消息
     * @return a new builder | 新的构建器
     */
    public static Builder builder(LogLevel level, String message) {
        return new Builder(level, message);
    }

    /**
     * Checks if this event has a throwable attached.
     * 检查此事件是否附带异常。
     *
     * @return true if a throwable is present | 如果有异常则返回 true
     */
    public boolean hasThrowable() {
        return throwable != null;
    }

    /**
     * Checks if this event has a marker attached.
     * 检查此事件是否附带标记。
     *
     * @return true if a marker is present | 如果有标记则返回 true
     */
    public boolean hasMarker() {
        return marker != null;
    }

    /**
     * Returns a formatted string representation of this log event.
     * 返回此日志事件的格式化字符串表示。
     *
     * <p>Format: {@code [timestamp] [thread] LEVEL logger - message}</p>
     * <p>格式: {@code [时间戳] [线程] 级别 日志记录器 - 消息}</p>
     *
     * @return formatted string | 格式化字符串
     */
    public String toFormattedString() {
        String ts = TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(timestampMillis));
        return "[" + ts + "] [" + threadName + "] " + level + " "
                + (loggerName != null ? loggerName : "") + " - " + message;
    }

    /**
     * Log Event Builder - Fluent Builder for LogEvent Construction
     * 日志事件构建器 - LogEvent 的流式构建器
     *
     * <p>Provides a fluent API for building {@link LogEvent} instances with
     * sensible defaults for optional fields.</p>
     * <p>提供流式 API 来构建 {@link LogEvent} 实例，可选字段有合理的默认值。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-log V1.0.3
     */
    public static final class Builder {

        private final LogLevel level;
        private final String message;
        private String loggerName;
        private Throwable throwable;
        private Marker marker;
        private Map<String, String> mdc = Map.of();
        private long timestampMillis = -1;
        private String threadName;
        private CallerInfo callerInfo = CallerInfo.UNKNOWN;

        private Builder(LogLevel level, String message) {
            this.level = Objects.requireNonNull(level, "level must not be null");
            this.message = Objects.requireNonNull(message, "message must not be null");
        }

        /**
         * Sets the logger name.
         * 设置日志记录器名称。
         *
         * @param loggerName the logger name | 日志记录器名称
         * @return this builder | 此构建器
         */
        public Builder loggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        /**
         * Sets the throwable.
         * 设置异常。
         *
         * @param throwable the throwable | 异常
         * @return this builder | 此构建器
         */
        public Builder throwable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        /**
         * Sets the marker.
         * 设置标记。
         *
         * @param marker the marker | 标记
         * @return this builder | 此构建器
         */
        public Builder marker(Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Sets the MDC context map.
         * 设置 MDC 上下文映射。
         *
         * @param mdc the MDC map | MDC 映射
         * @return this builder | 此构建器
         */
        public Builder mdc(Map<String, String> mdc) {
            this.mdc = mdc;
            return this;
        }

        /**
         * Sets the timestamp in milliseconds since epoch.
         * 设置自纪元以来的毫秒时间戳。
         *
         * @param timestampMillis the timestamp | 时间戳
         * @return this builder | 此构建器
         */
        public Builder timestamp(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        /**
         * Sets the thread name.
         * 设置线程名。
         *
         * @param threadName the thread name | 线程名
         * @return this builder | 此构建器
         */
        public Builder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        /**
         * Sets the caller info.
         * 设置调用者信息。
         *
         * @param callerInfo the caller info | 调用者信息
         * @return this builder | 此构建器
         */
        public Builder callerInfo(CallerInfo callerInfo) {
            this.callerInfo = callerInfo;
            return this;
        }

        /**
         * Builds the log event.
         * 构建日志事件。
         *
         * @return the log event | 日志事件
         */
        public LogEvent build() {
            long ts = timestampMillis >= 0 ? timestampMillis : System.currentTimeMillis();
            String tn = threadName != null ? threadName : Thread.currentThread().getName();
            return new LogEvent(
                    level, loggerName, message, throwable, marker,
                    mdc, ts, tn, callerInfo);
        }
    }
}
