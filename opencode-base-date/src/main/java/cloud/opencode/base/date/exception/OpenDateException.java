package cloud.opencode.base.date.exception;

/**
 * Date Exception Class for OpenCode Date Module
 * OpenCode日期模块异常类
 *
 * <p>This exception is thrown when date/time operations fail, including:</p>
 * <p>当日期时间操作失败时抛出此异常，包括：</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse failures - 解析失败</li>
 *   <li>Format failures - 格式化失败</li>
 *   <li>Invalid date/time values - 无效的日期时间值</li>
 *   <li>Timezone conversion errors - 时区转换错误</li>
 *   <li>Range validation failures - 范围验证失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Throwing parse exception
 * throw OpenDateException.parseError("2024-13-45", "Invalid date format");
 *
 * // Throwing format exception
 * throw OpenDateException.formatError("Failed to format date");
 *
 * // Throwing with cause
 * throw new OpenDateException("Operation failed", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public class OpenDateException extends RuntimeException {

    /**
     * The input value that caused the exception
     * 导致异常的输入值
     */
    private final String inputValue;

    /**
     * The expected format or pattern
     * 期望的格式或模式
     */
    private final String expectedFormat;

    /**
     * Constructs an exception with a message
     * 使用消息构造异常
     *
     * @param message the detail message | 详细消息
     */
    public OpenDateException(String message) {
        super(message);
        this.inputValue = null;
        this.expectedFormat = null;
    }

    /**
     * Constructs an exception with a message and cause
     * 使用消息和原因构造异常
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenDateException(String message, Throwable cause) {
        super(message, cause);
        this.inputValue = null;
        this.expectedFormat = null;
    }

    /**
     * Constructs an exception with all details
     * 使用所有详细信息构造异常
     *
     * @param message        the detail message | 详细消息
     * @param inputValue     the input value that caused the error | 导致错误的输入值
     * @param expectedFormat the expected format | 期望的格式
     */
    public OpenDateException(String message, String inputValue, String expectedFormat) {
        super(message);
        this.inputValue = inputValue;
        this.expectedFormat = expectedFormat;
    }

    /**
     * Constructs an exception with all details and cause
     * 使用所有详细信息和原因构造异常
     *
     * @param message        the detail message | 详细消息
     * @param inputValue     the input value that caused the error | 导致错误的输入值
     * @param expectedFormat the expected format | 期望的格式
     * @param cause          the cause | 原因
     */
    public OpenDateException(String message, String inputValue, String expectedFormat, Throwable cause) {
        super(message, cause);
        this.inputValue = inputValue;
        this.expectedFormat = expectedFormat;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a parse error exception
     * 创建解析错误异常
     *
     * @param input   the input that failed to parse | 解析失败的输入
     * @param pattern the expected pattern | 期望的模式
     * @return the exception instance | 异常实例
     */
    public static OpenDateException parseError(String input, String pattern) {
        String message = String.format("Failed to parse date/time '%s' with pattern '%s'", input, pattern);
        return new OpenDateException(message, input, pattern);
    }

    /**
     * Creates a parse error exception with cause
     * 创建带原因的解析错误异常
     *
     * @param input   the input that failed to parse | 解析失败的输入
     * @param pattern the expected pattern | 期望的模式
     * @param cause   the cause | 原因
     * @return the exception instance | 异常实例
     */
    public static OpenDateException parseError(String input, String pattern, Throwable cause) {
        String message = String.format("Failed to parse date/time '%s' with pattern '%s'", input, pattern);
        return new OpenDateException(message, input, pattern, cause);
    }

    /**
     * Creates a parse error exception for smart parsing
     * 创建智能解析的解析错误异常
     *
     * @param input the input that failed to parse | 解析失败的输入
     * @return the exception instance | 异常实例
     */
    public static OpenDateException parseError(String input) {
        String message = String.format("Failed to parse date/time '%s': no matching format found", input);
        return new OpenDateException(message, input, null);
    }

    /**
     * Creates a format error exception
     * 创建格式化错误异常
     *
     * @param message the error message | 错误消息
     * @return the exception instance | 异常实例
     */
    public static OpenDateException formatError(String message) {
        return new OpenDateException("Format error: " + message);
    }

    /**
     * Creates a format error exception with cause
     * 创建带原因的格式化错误异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     * @return the exception instance | 异常实例
     */
    public static OpenDateException formatError(String message, Throwable cause) {
        return new OpenDateException("Format error: " + message, cause);
    }

    /**
     * Creates an invalid value exception
     * 创建无效值异常
     *
     * @param field the field name | 字段名
     * @param value the invalid value | 无效值
     * @param range the valid range description | 有效范围描述
     * @return the exception instance | 异常实例
     */
    public static OpenDateException invalidValue(String field, Object value, String range) {
        String message = String.format("Invalid %s value: %s (valid range: %s)", field, value, range);
        return new OpenDateException(message, String.valueOf(value), range);
    }

    /**
     * Creates a timezone error exception
     * 创建时区错误异常
     *
     * @param zoneId the invalid zone ID | 无效的时区ID
     * @return the exception instance | 异常实例
     */
    public static OpenDateException timezoneError(String zoneId) {
        String message = String.format("Unknown timezone: '%s'", zoneId);
        return new OpenDateException(message, zoneId, null);
    }

    /**
     * Creates a range error exception
     * 创建范围错误异常
     *
     * @param message the error message | 错误消息
     * @return the exception instance | 异常实例
     */
    public static OpenDateException rangeError(String message) {
        return new OpenDateException("Range error: " + message);
    }

    /**
     * Creates a cron expression error
     * 创建Cron表达式错误异常
     *
     * @param expression the invalid cron expression | 无效的Cron表达式
     * @param reason     the reason | 原因
     * @return the exception instance | 异常实例
     */
    public static OpenDateException cronError(String expression, String reason) {
        String message = String.format("Invalid cron expression '%s': %s", expression, reason);
        return new OpenDateException(message, expression, null);
    }

    /**
     * Creates a cron expression error with cause
     * 创建带原因的Cron表达式错误异常
     *
     * @param expression the invalid cron expression | 无效的Cron表达式
     * @param reason     the reason | 原因
     * @param cause      the cause | 原因
     * @return the exception instance | 异常实例
     */
    public static OpenDateException cronError(String expression, String reason, Throwable cause) {
        String message = String.format("Invalid cron expression '%s': %s", expression, reason);
        return new OpenDateException(message, cause);
    }

    // ==================== Getters | 获取方法 ====================

    /**
     * Gets the input value that caused the exception
     * 获取导致异常的输入值
     *
     * @return the input value, or null if not available | 输入值，如果不可用则为null
     */
    public String getInputValue() {
        return inputValue;
    }

    /**
     * Gets the expected format
     * 获取期望的格式
     *
     * @return the expected format, or null if not available | 期望的格式，如果不可用则为null
     */
    public String getExpectedFormat() {
        return expectedFormat;
    }

    /**
     * Checks if input value is available
     * 检查输入值是否可用
     *
     * @return true if input value is available | 如果输入值可用则返回true
     */
    public boolean hasInputValue() {
        return inputValue != null;
    }

    /**
     * Checks if expected format is available
     * 检查期望格式是否可用
     *
     * @return true if expected format is available | 如果期望格式可用则返回true
     */
    public boolean hasExpectedFormat() {
        return expectedFormat != null;
    }
}
