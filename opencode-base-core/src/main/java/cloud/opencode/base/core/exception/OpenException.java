package cloud.opencode.base.core.exception;

import java.io.Serial;

/**
 * OpenException - Unified exception base class for OpenCode components
 * OpenException - OpenCode 组件统一异常基类
 *
 * <p>Base exception for all OpenCode components with error code and component name support.</p>
 * <p>所有组件异常都应继承此类，支持错误码和组件名称，便于异常分类和定位。</p>
 *
 * <p><strong>Important | 重要说明:</strong>
 * Note that {@link OpenIllegalArgumentException} and {@link OpenIllegalStateException} do NOT extend this class.
 * They extend {@link IllegalArgumentException} and {@link IllegalStateException} respectively,
 * so {@code catch(OpenException e)} will NOT catch those exceptions.</p>
 * <p>注意：{@link OpenIllegalArgumentException} 和 {@link OpenIllegalStateException} 并不继承此类。
 * 它们分别继承自 {@link IllegalArgumentException} 和 {@link IllegalStateException}，
 * 因此 {@code catch(OpenException e)} 无法捕获这两类异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code support - 错误码支持</li>
 *   <li>Component name support - 组件名称支持</li>
 *   <li>Formatted message output [Component] (Code) Message - 格式化消息输出</li>
 *   <li>RuntimeException (unchecked) - 非受检异常</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenException("Operation failed");
 * throw new OpenException("Core", "CORE_001", "Parameter required");
 * throw new OpenException("IO failed", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Serializable: Yes - 可序列化: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class OpenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Error code
     * 错误码
     */
    private final String errorCode;

    /**
     * Component name
     * 组件名称
     */
    private final String component;

    // ==================== 构造方法 ====================

    /**
     * Creates
     * 创建异常
     *
     * @param message the value | 异常消息
     */
    public OpenException(String message) {
        this(null, null, message, null);
    }

    /**
     * Creates
     * 创建异常（带原因）
     *
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenException(String message, Throwable cause) {
        this(null, null, message, cause);
    }

    /**
     * Creates
     * 创建异常（带组件和错误码）
     *
     * @param component the value | 组件名称
     * @param errorCode the value | 错误码
     * @param message the value | 异常消息
     */
    public OpenException(String component, String errorCode, String message) {
        this(component, errorCode, message, null);
    }

    /**
     * Creates
     * 创建异常（完整参数）
     *
     * @param component the value | 组件名称
     * @param errorCode the value | 错误码
     * @param message the value | 异常消息
     * @param cause the value | 原始异常
     */
    public OpenException(String component, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.component = component;
        this.errorCode = errorCode;
    }

    // ==================== Getter ====================

    /**
     * Gets
     * 获取错误码
     *
     * @return the result | 错误码，可能为 null
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets
     * 获取组件名称
     *
     * @return the result | 组件名称，可能为 null
     */
    public String getComponent() {
        return component;
    }

    /**
     * Gets
     * 获取原始消息（不含组件和错误码前缀）
     *
     * @return the result | 原始消息
     */
    public String getRawMessage() {
        return super.getMessage();
    }

    // ==================== 格式化输出 ====================

    /**
     * Gets
     * 获取格式化的异常消息
     * <p>
     * 格式: [组件] (错误码) 消息
     *
     * @return the result | 格式化的消息
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (component != null && !component.isEmpty()) {
            sb.append("[").append(component).append("] ");
        }
        if (errorCode != null && !errorCode.isEmpty()) {
            sb.append("(").append(errorCode).append(") ");
        }
        String rawMessage = super.getMessage();
        if (rawMessage != null) {
            sb.append(rawMessage);
        }
        return sb.toString();
    }
}
