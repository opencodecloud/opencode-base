package cloud.opencode.base.log;

import java.util.Objects;

/**
 * Log Level Enumeration - Defines Standard Log Levels
 * 日志级别枚举 - 定义标准日志级别
 *
 * <p>This enum defines the standard log levels used throughout the logging framework.
 * Levels are ordered by severity from TRACE (lowest) to OFF (highest).</p>
 * <p>此枚举定义日志框架中使用的标准日志级别。
 * 级别按严重程度从 TRACE（最低）到 OFF（最高）排序。</p>
 *
 * <p><strong>Level Hierarchy | 级别层次:</strong></p>
 * <pre>
 * TRACE (1) &lt; DEBUG (2) &lt; INFO (3) &lt; WARN (4) &lt; ERROR (5) &lt; OFF (6)
 * </pre>
 *
 * <p><strong>Usage Guidelines | 使用指南:</strong></p>
 * <ul>
 *   <li>TRACE - Fine-grained debug info, typically disabled - 细粒度调试信息，通常禁用</li>
 *   <li>DEBUG - Debugging information - 调试信息</li>
 *   <li>INFO - Informational messages - 信息性消息</li>
 *   <li>WARN - Warning situations - 警告情况</li>
 *   <li>ERROR - Error conditions - 错误情况</li>
 *   <li>OFF - Turn off logging - 关闭日志</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Six standard log levels from TRACE to OFF - 从 TRACE 到 OFF 的六个标准日志级别</li>
 *   <li>Numeric level comparison - 数值级别比较</li>
 *   <li>Case-insensitive parsing from name - 不区分大小写的名称解析</li>
 *   <li>Safe parsing with default fallback - 安全解析，带默认回退</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse from name
 * LogLevel level = LogLevel.fromName("INFO");
 * 
 * // Compare levels
 * if (level.isGreaterOrEqual(LogLevel.WARN)) {
 *     // handle warning and above
 * }
 * 
 * // Safe parse with default
 * LogLevel level = LogLevel.fromNameOrDefault("UNKNOWN", LogLevel.INFO);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: No (throws on null input) - 空值安全: 否（null 输入抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public enum LogLevel {

    /**
     * TRACE level - Most detailed information.
     * TRACE 级别 - 最详细的信息。
     */
    TRACE(1, "TRACE"),

    /**
     * DEBUG level - Debugging information.
     * DEBUG 级别 - 调试信息。
     */
    DEBUG(2, "DEBUG"),

    /**
     * INFO level - Informational messages.
     * INFO 级别 - 信息性消息。
     */
    INFO(3, "INFO"),

    /**
     * WARN level - Warning situations.
     * WARN 级别 - 警告情况。
     */
    WARN(4, "WARN"),

    /**
     * ERROR level - Error conditions.
     * ERROR 级别 - 错误情况。
     */
    ERROR(5, "ERROR"),

    /**
     * OFF level - Turn off logging.
     * OFF 级别 - 关闭日志。
     */
    OFF(6, "OFF");

    private final int level;
    private final String name;

    LogLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    /**
     * Returns the numeric level value.
     * 返回数值级别。
     *
     * @return the level value - 级别数值
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the level name.
     * 返回级别名称。
     *
     * @return the level name - 级别名称
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this level is greater than or equal to the specified level.
     * 检查此级别是否大于或等于指定级别。
     *
     * @param other the other level to compare - 要比较的其他级别
     * @return true if this level is greater than or equal to other - 如果此级别大于或等于其他级别则返回 true
     */
    public boolean isGreaterOrEqual(LogLevel other) {
        return this.level >= other.level;
    }

    /**
     * Checks if this level is enabled for the specified threshold level.
     * 检查此级别是否对指定阈值级别启用。
     *
     * @param threshold the threshold level - 阈值级别
     * @return true if enabled - 如果启用则返回 true
     */
    public boolean isEnabledFor(LogLevel threshold) {
        return this.level >= threshold.level;
    }

    /**
     * Parses a log level from its name.
     * 从名称解析日志级别。
     *
     * @param name the level name (case-insensitive) - 级别名称（不区分大小写）
     * @return the log level - 日志级别
     * @throws IllegalArgumentException if the name is not valid - 如果名称无效
     */
    public static LogLevel fromName(String name) {
        Objects.requireNonNull(name, "Level name must not be null");
        String upperName = name.toUpperCase();
        for (LogLevel level : values()) {
            if (level.name.equals(upperName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown log level: " + name);
    }

    /**
     * Safely parses a log level from its name, returning a default if not found.
     * 安全地从名称解析日志级别，如果未找到则返回默认值。
     *
     * @param name         the level name - 级别名称
     * @param defaultLevel the default level - 默认级别
     * @return the log level or default - 日志级别或默认值
     */
    public static LogLevel fromNameOrDefault(String name, LogLevel defaultLevel) {
        if (name == null || name.isBlank()) {
            return defaultLevel;
        }
        try {
            return fromName(name);
        } catch (IllegalArgumentException e) {
            return defaultLevel;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
