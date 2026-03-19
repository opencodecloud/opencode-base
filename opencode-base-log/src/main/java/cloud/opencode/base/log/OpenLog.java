package cloud.opencode.base.log;

import cloud.opencode.base.log.marker.Marker;

import java.util.function.Supplier;

/**
 * OpenLog - Unified Logging Facade
 * OpenLog - 统一日志门面
 *
 * <p>This is the main entry point for the OpenCode logging framework.
 * It provides static methods for all logging operations with zero configuration.</p>
 * <p>这是 OpenCode 日志框架的主入口点。它提供零配置的所有日志操作的静态方法。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Static facade methods - 静态门面方法</li>
 *   <li>Automatic caller class detection - 自动调用类检测</li>
 *   <li>Lambda lazy evaluation - Lambda 延迟求值</li>
 *   <li>Parameterized logging with {} - 使用 {} 的参数化日志</li>
 *   <li>Marker support - 标记支持</li>
 *   <li>Exception logging - 异常日志</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Simple logging
 * OpenLog.info("Application started");
 *
 * // Parameterized logging
 * OpenLog.info("User {} logged in from {}", userId, ipAddress);
 *
 * // Lambda lazy evaluation
 * OpenLog.debug(() -> "Expensive: " + computeValue());
 *
 * // Exception logging
 * OpenLog.error("Operation failed", exception);
 *
 * // Get Logger instance
 * Logger log = OpenLog.get(MyClass.class);
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static facade) - 线程安全: 是（无状态静态门面）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class OpenLog {

    private OpenLog() {
        // Utility class
    }

    // ==================== Logger Access ====================

    /**
     * Gets a logger for the calling class.
     * 获取调用类的日志记录器。
     *
     * @return the logger instance - 日志记录器实例
     */
    public static Logger get() {
        return getCallerLogger();
    }

    /**
     * Gets a logger for the specified class.
     * 获取指定类的日志记录器。
     *
     * @param clazz the class - 类
     * @return the logger instance - 日志记录器实例
     */
    public static Logger get(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Gets a logger for the specified name.
     * 获取指定名称的日志记录器。
     *
     * @param name the logger name - 日志记录器名称
     * @return the logger instance - 日志记录器实例
     */
    public static Logger get(String name) {
        return LoggerFactory.getLogger(name);
    }

    // ==================== TRACE Level ====================

    /**
     * Logs a message at TRACE level.
     * 在 TRACE 级别记录消息。
     *
     * @param message the message - 消息
     */
    public static void trace(String message) {
        getCallerLogger().trace(message);
    }

    /**
     * Logs a message at TRACE level with parameters.
     * 在 TRACE 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    public static void trace(String format, Object... args) {
        getCallerLogger().trace(format, args);
    }

    /**
     * Logs a message at TRACE level using lazy evaluation.
     * 使用延迟求值在 TRACE 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    public static void trace(Supplier<String> messageSupplier) {
        getCallerLogger().trace(messageSupplier);
    }

    // ==================== DEBUG Level ====================

    /**
     * Logs a message at DEBUG level.
     * 在 DEBUG 级别记录消息。
     *
     * @param message the message - 消息
     */
    public static void debug(String message) {
        getCallerLogger().debug(message);
    }

    /**
     * Logs a message at DEBUG level with parameters.
     * 在 DEBUG 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    public static void debug(String format, Object... args) {
        getCallerLogger().debug(format, args);
    }

    /**
     * Logs a message at DEBUG level using lazy evaluation.
     * 使用延迟求值在 DEBUG 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    public static void debug(Supplier<String> messageSupplier) {
        getCallerLogger().debug(messageSupplier);
    }

    // ==================== INFO Level ====================

    /**
     * Logs a message at INFO level.
     * 在 INFO 级别记录消息。
     *
     * @param message the message - 消息
     */
    public static void info(String message) {
        getCallerLogger().info(message);
    }

    /**
     * Logs a message at INFO level with parameters.
     * 在 INFO 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    public static void info(String format, Object... args) {
        getCallerLogger().info(format, args);
    }

    /**
     * Logs a message at INFO level using lazy evaluation.
     * 使用延迟求值在 INFO 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    public static void info(Supplier<String> messageSupplier) {
        getCallerLogger().info(messageSupplier);
    }

    /**
     * Logs a message at INFO level with a marker.
     * 使用标记在 INFO 级别记录消息。
     *
     * @param marker  the marker - 标记
     * @param message the message - 消息
     */
    public static void info(Marker marker, String message) {
        getCallerLogger().info(marker, message);
    }

    // ==================== WARN Level ====================

    /**
     * Logs a message at WARN level.
     * 在 WARN 级别记录消息。
     *
     * @param message the message - 消息
     */
    public static void warn(String message) {
        getCallerLogger().warn(message);
    }

    /**
     * Logs a message at WARN level with parameters.
     * 在 WARN 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    public static void warn(String format, Object... args) {
        getCallerLogger().warn(format, args);
    }

    /**
     * Logs a message at WARN level with an exception.
     * 在 WARN 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    public static void warn(String message, Throwable throwable) {
        getCallerLogger().warn(message, throwable);
    }

    /**
     * Logs a message at WARN level using lazy evaluation.
     * 使用延迟求值在 WARN 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     */
    public static void warn(Supplier<String> messageSupplier) {
        getCallerLogger().warn(messageSupplier);
    }

    // ==================== ERROR Level ====================

    /**
     * Logs a message at ERROR level.
     * 在 ERROR 级别记录消息。
     *
     * @param message the message - 消息
     */
    public static void error(String message) {
        getCallerLogger().error(message);
    }

    /**
     * Logs a message at ERROR level with parameters.
     * 在 ERROR 级别记录带参数的消息。
     *
     * @param format the format string - 格式字符串
     * @param args   the arguments - 参数
     */
    public static void error(String format, Object... args) {
        getCallerLogger().error(format, args);
    }

    /**
     * Logs a message at ERROR level with an exception.
     * 在 ERROR 级别记录带异常的消息。
     *
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    public static void error(String message, Throwable throwable) {
        getCallerLogger().error(message, throwable);
    }

    /**
     * Logs an exception at ERROR level.
     * 在 ERROR 级别记录异常。
     *
     * @param throwable the exception - 异常
     */
    public static void error(Throwable throwable) {
        getCallerLogger().error(throwable);
    }

    /**
     * Logs a message at ERROR level using lazy evaluation with exception.
     * 使用延迟求值和异常在 ERROR 级别记录消息。
     *
     * @param messageSupplier the message supplier - 消息提供者
     * @param throwable       the exception - 异常
     */
    public static void error(Supplier<String> messageSupplier, Throwable throwable) {
        getCallerLogger().error(messageSupplier, throwable);
    }

    /**
     * Logs a message at ERROR level with a marker and exception.
     * 使用标记和异常在 ERROR 级别记录消息。
     *
     * @param marker    the marker - 标记
     * @param message   the message - 消息
     * @param throwable the exception - 异常
     */
    public static void error(Marker marker, String message, Throwable throwable) {
        getCallerLogger().error(marker, message, throwable);
    }

    // ==================== Level Checks ====================

    /**
     * Checks if TRACE level is enabled.
     * 检查 TRACE 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public static boolean isTraceEnabled() {
        return getCallerLogger().isTraceEnabled();
    }

    /**
     * Checks if DEBUG level is enabled.
     * 检查 DEBUG 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public static boolean isDebugEnabled() {
        return getCallerLogger().isDebugEnabled();
    }

    /**
     * Checks if INFO level is enabled.
     * 检查 INFO 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public static boolean isInfoEnabled() {
        return getCallerLogger().isInfoEnabled();
    }

    /**
     * Checks if WARN level is enabled.
     * 检查 WARN 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public static boolean isWarnEnabled() {
        return getCallerLogger().isWarnEnabled();
    }

    /**
     * Checks if ERROR level is enabled.
     * 检查 ERROR 级别是否启用。
     *
     * @return true if enabled - 如果启用返回 true
     */
    public static boolean isErrorEnabled() {
        return getCallerLogger().isErrorEnabled();
    }

    // ==================== Internal Methods ====================

    private static Logger getCallerLogger() {
        String callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .skip(2) // Skip getCallerLogger and the OpenLog method
                        .map(StackWalker.StackFrame::getClassName)
                        .findFirst()
                        .orElse("UNKNOWN"));
        return LoggerFactory.getLogger(callerClass);
    }
}
