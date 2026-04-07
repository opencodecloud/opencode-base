package cloud.opencode.base.web.exception;

import cloud.opencode.base.core.exception.OpenException;
import cloud.opencode.base.web.ResultCode;
import cloud.opencode.base.web.CommonResultCode;

import java.io.Serial;

/**
 * Open Web Exception
 * Web异常
 *
 * <p>Base exception for web layer errors. Extends {@link OpenException} to integrate
 * with the unified OpenCode exception hierarchy.</p>
 * <p>Web层错误的基础异常。继承 {@link OpenException} 以集成到 OpenCode 统一异常体系。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Error code and HTTP status support - 错误码和 HTTP 状态码支持</li>
 *   <li>ResultCode integration - ResultCode 集成</li>
 *   <li>Factory methods for common HTTP errors - 常见 HTTP 错误的工厂方法</li>
 *   <li>Cause chain support - 异常链支持</li>
 *   <li>OpenException hierarchy integration - OpenException 体系集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Using factory methods
 * throw OpenWebException.badRequest("Invalid parameter");
 * throw OpenWebException.notFound("User not found");
 * throw OpenWebException.unauthorized("Token expired");
 *
 * // Using ResultCode
 * throw new OpenWebException(CommonResultCode.FORBIDDEN);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable fields) - 线程安全: 是（不可变字段）</li>
 *   <li>Null-safe: No (message should not be null) - 空值安全: 否（消息不应为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public class OpenWebException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Web";

    private final int httpStatus;

    /**
     * Create exception with message
     * 使用消息创建异常
     *
     * @param message the message | 消息
     */
    public OpenWebException(String message) {
        super(COMPONENT, CommonResultCode.INTERNAL_ERROR.getCode(), message);
        this.httpStatus = CommonResultCode.INTERNAL_ERROR.getHttpStatus();
    }

    /**
     * Create exception with code and message
     * 使用代码和消息创建异常
     *
     * @param code the code | 代码
     * @param message the message | 消息
     */
    public OpenWebException(String code, String message) {
        super(COMPONENT, code, message);
        this.httpStatus = 500;
    }

    /**
     * Create exception with code, message and HTTP status
     * 使用代码、消息和HTTP状态创建异常
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param httpStatus the HTTP status | HTTP状态
     */
    public OpenWebException(String code, String message, int httpStatus) {
        super(COMPONENT, code, message);
        this.httpStatus = httpStatus;
    }

    /**
     * Create exception with result code
     * 使用响应码创建异常
     *
     * @param resultCode the result code | 响应码
     */
    public OpenWebException(ResultCode resultCode) {
        super(COMPONENT, resultCode.getCode(), resultCode.getMessage());
        this.httpStatus = resultCode.getHttpStatus();
    }

    /**
     * Create exception with result code and custom message
     * 使用响应码和自定义消息创建异常
     *
     * @param resultCode the result code | 响应码
     * @param message the custom message | 自定义消息
     */
    public OpenWebException(ResultCode resultCode, String message) {
        super(COMPONENT, resultCode.getCode(), message);
        this.httpStatus = resultCode.getHttpStatus();
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     */
    public OpenWebException(String message, Throwable cause) {
        super(COMPONENT, CommonResultCode.INTERNAL_ERROR.getCode(), message, cause);
        this.httpStatus = CommonResultCode.INTERNAL_ERROR.getHttpStatus();
    }

    /**
     * Create exception with result code and cause
     * 使用响应码和原因创建异常
     *
     * @param resultCode the result code | 响应码
     * @param cause the cause | 原因
     */
    public OpenWebException(ResultCode resultCode, Throwable cause) {
        super(COMPONENT, resultCode.getCode(), resultCode.getMessage(), cause);
        this.httpStatus = resultCode.getHttpStatus();
    }

    /**
     * Create exception with code, message, HTTP status and cause
     * 使用代码、消息、HTTP状态和原因创建异常
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param httpStatus the HTTP status | HTTP状态
     * @param cause the cause | 原因
     */
    protected OpenWebException(String code, String message, int httpStatus, Throwable cause) {
        super(COMPONENT, code, message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * Get the code
     * 获取代码
     *
     * @return the code | 代码
     */
    public String getCode() {
        return getErrorCode();
    }

    /**
     * Get the HTTP status
     * 获取HTTP状态
     *
     * @return the HTTP status | HTTP状态
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Returns the raw message without component/code prefix formatting.
     * 返回不含组件/错误码前缀格式化的原始消息。
     *
     * <p>Overrides OpenException's formatted getMessage() to preserve backward
     * compatibility for web exception consumers who depend on the plain message.</p>
     * <p>覆盖 OpenException 的格式化 getMessage()，保持 Web 异常消费者对纯消息的向后兼容性。</p>
     *
     * @return the raw message | 原始消息
     */
    @Override
    public String getMessage() {
        return getRawMessage();
    }

    // === Factory Methods ===

    /**
     * Create bad request exception
     * 创建请求错误异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenWebException badRequest(String message) {
        return new OpenWebException(CommonResultCode.BAD_REQUEST, message);
    }

    /**
     * Create unauthorized exception
     * 创建未授权异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenWebException unauthorized(String message) {
        return new OpenWebException(CommonResultCode.UNAUTHORIZED, message);
    }

    /**
     * Create forbidden exception
     * 创建禁止访问异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenWebException forbidden(String message) {
        return new OpenWebException(CommonResultCode.FORBIDDEN, message);
    }

    /**
     * Create not found exception
     * 创建资源不存在异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenWebException notFound(String message) {
        return new OpenWebException(CommonResultCode.NOT_FOUND, message);
    }

    /**
     * Create internal error exception
     * 创建内部错误异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenWebException internalError(String message) {
        return new OpenWebException(CommonResultCode.INTERNAL_ERROR, message);
    }

    /**
     * Create internal error exception with cause
     * 创建带原因的内部错误异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OpenWebException internalError(String message, Throwable cause) {
        return new OpenWebException(
                CommonResultCode.INTERNAL_ERROR.getCode(), message, 500, cause);
    }
}
