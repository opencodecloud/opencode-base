package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Marker;

import java.util.function.Supplier;

/**
 * Logger Interface - Core Logging Contract
 * 日志记录器接口 - 核心日志契约
 *
 * <p>This interface defines the standard logging operations. Implementations
 * delegate to underlying logging frameworks (SLF4J, Log4j2, JUL, etc.).</p>
 * <p>此接口定义标准日志操作。实现委托给底层日志框架（SLF4J、Log4j2、JUL 等）。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Standard log levels (TRACE, DEBUG, INFO, WARN, ERROR) - 标准日志级别</li>
 *   <li>Parameterized logging with {} placeholders - 使用 {} 占位符的参数化日志</li>
 *   <li>Lambda-based lazy evaluation - 基于 Lambda 的延迟求值</li>
 *   <li>Marker support for log categorization - 标记支持用于日志分类</li>
 *   <li>Exception logging with stack traces - 带堆栈跟踪的异常日志</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * Logger log = LoggerFactory.getLogger(MyService.class);
 *
 * // Simple logging
 * log.info("Application started");
 *
 * // Parameterized logging
 * log.info("User {} logged in from {}", userId, ipAddress);
 *
 * // Lambda lazy evaluation
 * log.debug(() -> "Expensive computation: " + computeValue());
 *
 * // Exception logging
 * log.error("Operation failed", exception);
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface Logger {

    /**
     * Returns the name of this logger.
     * 返回此日志记录器的名称。
     *
     * @return the logger name - 日志记录器名称
     */
    String getName();

    // ==================== TRACE Level ====================

    /**
     * Checks if TRACE level is enabled.
     * 检查 TRACE 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    boolean isTraceEnabled();

    /**
     * Checks if TRACE level is enabled for the specified marker.
     * 检查指定标记的 TRACE 级别是否启用。
     *
     * @param marker the marker - 标记
     * @return true if enabled - 如果启用返回 true
     */
    boolean isTraceEnabled(Marker marker);

    /**
     * Logs a message at TRACE level.
     * 在 TRACE 级别记录消息。
     *
     * @param message the message - 消息
     */
    void trace(String message);

    /**
     * Logs a message at TRACE level with parameters.
     * 在 TRACE 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void trace(String format, Object... args);

    /**
     * Logs a message at TRACE level with an exception.
     * 在 TRACE 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void trace(String message, Throwable throwable);

    /**
     * Logs a message at TRACE level using lazy evaluation.
     * 使用延迟求值在 TRACE 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    void trace(Supplier<String> messageSupplier);

    /**
     * Logs a message at TRACE level with a marker.
     * 使用标记在 TRACE 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    void trace(Marker marker, String message);

    /**
     * Logs a message at TRACE level with a marker and parameters.
     * 使用标记和参数在 TRACE 级别记录消息。
     *
     * @param marker the marker - 标记
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void trace(Marker marker, String format, Object... args);

    // ==================== DEBUG Level ====================

    /**
     * Checks if DEBUG level is enabled.
     * 检查 DEBUG 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    boolean isDebugEnabled();

    /**
     * Checks if DEBUG level is enabled for the specified marker.
     * 检查指定标记的 DEBUG 级别是否启用。
     *
     * @param marker the marker - 标记
     * @return true if enabled - 如果启用返回 true
     */
    boolean isDebugEnabled(Marker marker);

    /**
     * Logs a message at DEBUG level.
     * 在 DEBUG 级别记录消息。
     *
     * @param message the message - 消息
     */
    void debug(String message);

    /**
     * Logs a message at DEBUG level with parameters.
     * 在 DEBUG 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void debug(String format, Object... args);

    /**
     * Logs a message at DEBUG level with an exception.
     * 在 DEBUG 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void debug(String message, Throwable throwable);

    /**
     * Logs a message at DEBUG level using lazy evaluation.
     * 使用延迟求值在 DEBUG 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    void debug(Supplier<String> messageSupplier);

    /**
     * Logs a message at DEBUG level with a marker.
     * 使用标记在 DEBUG 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    void debug(Marker marker, String message);

    /**
     * Logs a message at DEBUG level with a marker and parameters.
     * 使用标记和参数在 DEBUG 级别记录消息。
     *
     * @param marker the marker - 标记
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void debug(Marker marker, String format, Object... args);

    // ==================== INFO Level ====================

    /**
     * Checks if INFO level is enabled.
     * 检查 INFO 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    boolean isInfoEnabled();

    /**
     * Checks if INFO level is enabled for the specified marker.
     * 检查指定标记的 INFO 级别是否启用。
     *
     * @param marker the marker - 标记
     * @return true if enabled - 如果启用返回 true
     */
    boolean isInfoEnabled(Marker marker);

    /**
     * Logs a message at INFO level.
     * 在 INFO 级别记录消息。
     *
     * @param message the message - 消息
     */
    void info(String message);

    /**
     * Logs a message at INFO level with parameters.
     * 在 INFO 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void info(String format, Object... args);

    /**
     * Logs a message at INFO level with an exception.
     * 在 INFO 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void info(String message, Throwable throwable);

    /**
     * Logs a message at INFO level using lazy evaluation.
     * 使用延迟求值在 INFO 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    void info(Supplier<String> messageSupplier);

    /**
     * Logs a message at INFO level with a marker.
     * 使用标记在 INFO 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    void info(Marker marker, String message);

    /**
     * Logs a message at INFO level with a marker and parameters.
     * 使用标记和参数在 INFO 级别记录消息。
     *
     * @param marker the marker - 标记
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void info(Marker marker, String format, Object... args);

    // ==================== WARN Level ====================

    /**
     * Checks if WARN level is enabled.
     * 检查 WARN 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    boolean isWarnEnabled();

    /**
     * Checks if WARN level is enabled for the specified marker.
     * 检查指定标记的 WARN 级别是否启用。
     *
     * @param marker the marker - 标记
     * @return true if enabled - 如果启用返回 true
     */
    boolean isWarnEnabled(Marker marker);

    /**
     * Logs a message at WARN level.
     * 在 WARN 级别记录消息。
     *
     * @param message the message - 消息
     */
    void warn(String message);

    /**
     * Logs a message at WARN level with parameters.
     * 在 WARN 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void warn(String format, Object... args);

    /**
     * Logs a message at WARN level with an exception.
     * 在 WARN 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void warn(String message, Throwable throwable);

    /**
     * Logs a message at WARN level using lazy evaluation.
     * 使用延迟求值在 WARN 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    void warn(Supplier<String> messageSupplier);

    /**
     * Logs a message at WARN level with a marker.
     * 使用标记在 WARN 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    void warn(Marker marker, String message);

    /**
     * Logs a message at WARN level with a marker and parameters.
     * 使用标记和参数在 WARN 级别记录消息。
     *
     * @param marker the marker - 标记
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void warn(Marker marker, String format, Object... args);

    /**
     * Logs a message at WARN level with a marker and exception.
     * 使用标记和异常在 WARN 级别记录消息。
     *
     * @param marker    the marker - 标记
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void warn(Marker marker, String message, Throwable throwable);

    // ==================== ERROR Level ====================

    /**
     * Checks if ERROR level is enabled.
     * 检查 ERROR 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    boolean isErrorEnabled();

    /**
     * Checks if ERROR level is enabled for the specified marker.
     * 检查指定标记的 ERROR 级别是否启用。
     *
     * @param marker the marker - 标记
     * @return true if enabled - 如果启用返回 true
     */
    boolean isErrorEnabled(Marker marker);

    /**
     * Logs a message at ERROR level.
     * 在 ERROR 级别记录消息。
     *
     * @param message the message - 消息
     */
    void error(String message);

    /**
     * Logs a message at ERROR level with parameters.
     * 在 ERROR 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void error(String format, Object... args);

    /**
     * Logs a message at ERROR level with an exception.
     * 在 ERROR 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void error(String message, Throwable throwable);

    /**
     * Logs an exception at ERROR level.
     * 在 ERROR 级别记录异常。
     *
     * @param throwable the exception - 异常
     */
    void error(Throwable throwable);

    /**
     * Logs a message at ERROR level using lazy evaluation.
     * 使用延迟求值在 ERROR 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    void error(Supplier<String> messageSupplier);

    /**
     * Logs a message at ERROR level using lazy evaluation with exception.
     * 使用延迟求值和异常在 ERROR 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     * @param throwable       the exception - 异常
     */
    void error(Supplier<String> messageSupplier, Throwable throwable);

    /**
     * Logs a message at ERROR level with a marker.
     * 使用标记在 ERROR 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    void error(Marker marker, String message);

    /**
     * Logs a message at ERROR level with a marker and parameters.
     * 使用标记和参数在 ERROR 级别记录消息。
     *
     * @param marker the marker - 标记
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void error(Marker marker, String format, Object... args);

    /**
     * Logs a message at ERROR level with a marker and exception.
     * 使用标记和异常在 ERROR 级别记录消息。
     *
     * @param marker    the marker - 标记
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void error(Marker marker, String message, Throwable throwable);

    // ==================== Generic Methods ====================

    /**
     * Checks if the specified level is enabled.
     * 检查指定级别是否启用。
     *
     * @param level the log level - 日志级别
     * @return true if enabled - 如果启用返回 true
     */
    boolean isEnabled(LogLevel level);

    /**
     * Logs a message at the specified level.
     * 在指定级别记录消息。
     *
     * @param level   the log level - 日志级别
     * @param message the message - 消息
     */
    void log(LogLevel level, String message);

    /**
     * Logs a message at the specified level with parameters.
     * 在指定级别记录带参数的消息。
     *
     * @param level  the log level - 日志级别
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    void log(LogLevel level, String format, Object... args);

    /**
     * Logs a message at the specified level with an exception.
     * 在指定级别记录带异常的消息。
     *
     * @param level     the log level - 日志级别
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    void log(LogLevel level, String message, Throwable throwable);
}
