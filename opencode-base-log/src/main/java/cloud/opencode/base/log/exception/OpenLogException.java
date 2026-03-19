package cloud.opencode.base.log.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Open Log Exception - Logging Component Exception
 * Open 日志异常 - 日志组件异常
 *
 * <p>This exception is thrown when errors occur during logging operations,
 * such as provider initialization failures or configuration errors.</p>
 * <p>当日志操作期间发生错误时抛出此异常，如提供者初始化失败或配置错误。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for logging component errors - 日志组件错误的基础异常</li>
 *   <li>Factory methods for common error scenarios - 常见错误场景的工厂方法</li>
 *   <li>Extends OpenException with LOG component code - 继承 OpenException，使用 LOG 组件代码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Throw provider not found
 * throw OpenLogException.providerNotFound();
 * 
 * // Throw initialization failure
 * throw OpenLogException.initializationFailed(cause);
 * 
 * // Throw invalid config
 * throw OpenLogException.invalidConfig("level", "Invalid log level");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public class OpenLogException extends OpenException {

    private static final String COMPONENT = "LOG";

    /**
     * Constructs a new exception with the specified message.
     * 使用指定消息构造新异常。
     *
     * @param message the detail message - 详细消息
     */
    public OpenLogException(String message) {
        super(COMPONENT, null, message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 使用指定消息和原因构造新异常。
     *
     * @param message the detail message - 详细消息
     * @param cause   the cause - 原因
     */
    public OpenLogException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a provider not found exception.
     * 创建提供者未找到异常。
     *
     * @return the exception - 异常
     */
    public static OpenLogException providerNotFound() {
        return new OpenLogException(
                "No log provider found. Please add a log implementation dependency.");
    }

    /**
     * Creates an initialization failed exception.
     * 创建初始化失败异常。
     *
     * @param cause the cause - 原因
     * @return the exception - 异常
     */
    public static OpenLogException initializationFailed(Throwable cause) {
        return new OpenLogException("Failed to initialize log provider", cause);
    }

    /**
     * Creates an invalid configuration exception.
     * 创建无效配置异常。
     *
     * @param configKey the configuration key - 配置键
     * @param message   the error message - 错误消息
     * @return the exception - 异常
     */
    public static OpenLogException invalidConfig(String configKey, String message) {
        return new OpenLogException(
                "Invalid log configuration for '" + configKey + "': " + message);
    }

    /**
     * Creates an adapter not found exception.
     * 创建适配器未找到异常。
     *
     * @param adapterType the adapter type - 适配器类型
     * @return the exception - 异常
     */
    public static OpenLogException adapterNotFound(String adapterType) {
        return new OpenLogException("Log adapter not found: " + adapterType);
    }

    /**
     * Creates an unsupported operation exception.
     * 创建不支持的操作异常。
     *
     * @param operation the operation - 操作
     * @return the exception - 异常
     */
    public static OpenLogException unsupportedOperation(String operation) {
        return new OpenLogException("Unsupported log operation: " + operation);
    }
}
