package cloud.opencode.base.log.enhance;

import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.LoggerFactory;
import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.marker.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Structured Log - JSON-style Structured Logging
 * 结构化日志 - JSON 风格的结构化日志
 *
 * <p>StructuredLog provides a fluent API for creating structured log entries
 * with key-value pairs, suitable for log aggregation systems like ELK/Loki.</p>
 * <p>StructuredLog 提供流式 API 来创建带键值对的结构化日志条目，
 * 适合 ELK/Loki 等日志聚合系统。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * StructuredLog.info()
 *     .message("User login successful")
 *     .field("userId", "user123")
 *     .field("ip", "192.168.1.1")
 *     .field("duration", 150)
 *     .log();
 *
 * // Output (JSON format):
 * // {"message":"User login successful","userId":"user123","ip":"192.168.1.1","duration":150}
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for structured log entries - 用于结构化日志条目的流式构建器 API</li>
 *   <li>JSON-format output for log aggregation systems - JSON 格式输出，适用于日志聚合系统</li>
 *   <li>Support for all log levels, markers, and exceptions - 支持所有日志级别、标记和异常</li>
 *   <li>Automatic JSON escaping of special characters - 自动 JSON 转义特殊字符</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (Builder is not shared) - 线程安全: 否（Builder 不共享）</li>
 *   <li>Null-safe: Yes (null fields are skipped) - 空值安全: 是（null 字段被跳过）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class StructuredLog {

    private StructuredLog() {
        // Utility class
    }

    /**
     * Creates a TRACE level structured log builder.
     * 创建 TRACE 级别的结构化日志构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder trace() {
        return new Builder(LogLevel.TRACE);
    }

    /**
     * Creates a DEBUG level structured log builder.
     * 创建 DEBUG 级别的结构化日志构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder debug() {
        return new Builder(LogLevel.DEBUG);
    }

    /**
     * Creates an INFO level structured log builder.
     * 创建 INFO 级别的结构化日志构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder info() {
        return new Builder(LogLevel.INFO);
    }

    /**
     * Creates a WARN level structured log builder.
     * 创建 WARN 级别的结构化日志构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder warn() {
        return new Builder(LogLevel.WARN);
    }

    /**
     * Creates an ERROR level structured log builder.
     * 创建 ERROR 级别的结构化日志构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder error() {
        return new Builder(LogLevel.ERROR);
    }

    /**
     * Structured Log Builder.
     * 结构化日志构建器。
     */
    public static final class Builder {
        private final LogLevel level;
        private final Map<String, Object> fields = new HashMap<>();
        private String message;
        private Throwable exception;
        private Marker marker;
        private String traceId;
        private String spanId;

        private Builder(LogLevel level) {
            this.level = level;
        }

        /**
         * Sets the log message.
         * 设置日志消息。
         *
         * @param message the message - 消息
         * @return this builder - 此构建器
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Adds a field.
         * 添加字段。
         *
         * @param key   the field key - 字段键
         * @param value the field value - 字段值
         * @return this builder - 此构建器
         */
        public Builder field(String key, Object value) {
            this.fields.put(key, value);
            return this;
        }

        /**
         * Adds multiple fields.
         * 添加多个字段。
         *
         * @param fields the fields - 字段
         * @return this builder - 此构建器
         */
        public Builder fields(Map<String, Object> fields) {
            this.fields.putAll(fields);
            return this;
        }

        /**
         * Sets the exception.
         * 设置异常。
         *
         * @param throwable the exception - 异常
         * @return this builder - 此构建器
         */
        public Builder exception(Throwable throwable) {
            this.exception = throwable;
            return this;
        }

        /**
         * Sets the marker.
         * 设置标记。
         *
         * @param marker the marker - 标记
         * @return this builder - 此构建器
         */
        public Builder marker(Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Sets the trace ID.
         * 设置追踪 ID。
         *
         * @param traceId the trace ID - 追踪 ID
         * @return this builder - 此构建器
         */
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * Sets the span ID.
         * 设置跨度 ID。
         *
         * @param spanId the span ID - 跨度 ID
         * @return this builder - 此构建器
         */
        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        /**
         * Logs to the default logger.
         * 记录到默认日志记录器。
         */
        public void log() {
            String callerClass = StackWalker.getInstance()
                    .walk(frames -> frames
                            .skip(1)
                            .map(StackWalker.StackFrame::getClassName)
                            .findFirst()
                            .orElse("UNKNOWN"));
            log(LoggerFactory.getLogger(callerClass));
        }

        /**
         * Logs to the specified logger.
         * 记录到指定的日志记录器。
         *
         * @param logger the logger - 日志记录器
         */
        public void log(Logger logger) {
            String formattedMessage = formatMessage();

            switch (level) {
                case TRACE -> {
                    if (exception != null) logger.trace(formattedMessage, exception);
                    else logger.trace(formattedMessage);
                }
                case DEBUG -> {
                    if (exception != null) logger.debug(formattedMessage, exception);
                    else logger.debug(formattedMessage);
                }
                case INFO -> {
                    if (marker != null) logger.info(marker, formattedMessage);
                    else if (exception != null) logger.info(formattedMessage, exception);
                    else logger.info(formattedMessage);
                }
                case WARN -> {
                    if (marker != null && exception != null) logger.warn(marker, formattedMessage, exception);
                    else if (exception != null) logger.warn(formattedMessage, exception);
                    else logger.warn(formattedMessage);
                }
                case ERROR -> {
                    if (marker != null && exception != null) logger.error(marker, formattedMessage, exception);
                    else if (exception != null) logger.error(formattedMessage, exception);
                    else logger.error(formattedMessage);
                }
                default -> logger.info(formattedMessage);
            }
        }

        private String formatMessage() {
            StringJoiner joiner = new StringJoiner(", ", "{", "}");

            if (message != null) {
                joiner.add("\"message\":\"" + escapeJson(message) + "\"");
            }
            if (traceId != null) {
                joiner.add("\"traceId\":\"" + traceId + "\"");
            }
            if (spanId != null) {
                joiner.add("\"spanId\":\"" + spanId + "\"");
            }

            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String key = escapeJson(entry.getKey());
                Object value = entry.getValue();
                if (value instanceof String) {
                    joiner.add("\"" + key + "\":\"" + escapeJson((String) value) + "\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    joiner.add("\"" + key + "\":" + value);
                } else if (value != null) {
                    joiner.add("\"" + key + "\":\"" + escapeJson(value.toString()) + "\"");
                }
            }

            return joiner.toString();
        }

        /**
         * Pre-computed escape strings for control characters 0x00-0x1F.
         * 预计算的控制字符 0x00-0x1F 转义字符串，避免 String.format() 调用。
         */
        private static final String[] CONTROL_CHAR_ESCAPES = new String[0x20];

        static {
            for (int i = 0; i < 0x20; i++) {
                CONTROL_CHAR_ESCAPES[i] = switch (i) {
                    case '\b' -> "\\b";
                    case '\t' -> "\\t";
                    case '\n' -> "\\n";
                    case '\f' -> "\\f";
                    case '\r' -> "\\r";
                    default -> String.format("\\u%04x", i);
                };
            }
        }

        private String escapeJson(String value) {
            if (value == null) return "";

            StringBuilder sb = new StringBuilder(value.length());
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch == '\\') {
                    sb.append("\\\\");
                } else if (ch == '\"') {
                    sb.append("\\\"");
                } else if (ch < 0x20) {
                    sb.append(CONTROL_CHAR_ESCAPES[ch]);
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
    }
}
