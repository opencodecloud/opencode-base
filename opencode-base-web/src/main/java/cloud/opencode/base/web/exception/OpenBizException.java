package cloud.opencode.base.web.exception;

import cloud.opencode.base.web.ResultCode;
import cloud.opencode.base.web.CommonResultCode;

/**
 * Open Business Exception
 * 业务异常
 *
 * <p>Exception for business logic errors.</p>
 * <p>业务逻辑错误的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Business error with attached data - 带附加数据的业务错误</li>
 *   <li>Factory methods for common business errors - 常见业务错误的工厂方法</li>
 *   <li>Extends OpenWebException with data payload - 扩展 OpenWebException 并携带数据负载</li>
 *   <li>ResultCode integration - ResultCode 集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Using factory methods
 * throw OpenBizException.dataNotFound("User not found");
 * throw OpenBizException.dataDuplicate("Email already exists");
 * throw OpenBizException.validationError("Invalid input", errors);
 *
 * // With result code
 * throw new OpenBizException(CommonResultCode.BUSINESS_ERROR);
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
public class OpenBizException extends OpenWebException {

    private final Object data;

    /**
     * Create exception with message
     * 使用消息创建异常
     *
     * @param message the message | 消息
     */
    public OpenBizException(String message) {
        super(CommonResultCode.BUSINESS_ERROR.getCode(), message, CommonResultCode.BUSINESS_ERROR.getHttpStatus());
        this.data = null;
    }

    /**
     * Create exception with code and message
     * 使用代码和消息创建异常
     *
     * @param code the code | 代码
     * @param message the message | 消息
     */
    public OpenBizException(String code, String message) {
        super(code, message, 500);
        this.data = null;
    }

    /**
     * Create exception with result code
     * 使用响应码创建异常
     *
     * @param resultCode the result code | 响应码
     */
    public OpenBizException(ResultCode resultCode) {
        super(resultCode);
        this.data = null;
    }

    /**
     * Create exception with result code and custom message
     * 使用响应码和自定义消息创建异常
     *
     * @param resultCode the result code | 响应码
     * @param message the custom message | 自定义消息
     */
    public OpenBizException(ResultCode resultCode, String message) {
        super(resultCode, message);
        this.data = null;
    }

    /**
     * Create exception with result code and data
     * 使用响应码和数据创建异常
     *
     * @param resultCode the result code | 响应码
     * @param data the data | 数据
     */
    public OpenBizException(ResultCode resultCode, Object data) {
        super(resultCode);
        this.data = data;
    }

    /**
     * Create exception with code, message and data
     * 使用代码、消息和数据创建异常
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param data the data | 数据
     */
    public OpenBizException(String code, String message, Object data) {
        super(code, message, 500);
        this.data = data;
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     */
    public OpenBizException(String message, Throwable cause) {
        super(message, cause);
        this.data = null;
    }

    /**
     * Create exception with result code and cause
     * 使用响应码和原因创建异常
     *
     * @param resultCode the result code | 响应码
     * @param cause the cause | 原因
     */
    public OpenBizException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
        this.data = null;
    }

    /**
     * Get the data
     * 获取数据
     *
     * @return the data | 数据
     */
    public Object getData() {
        return data;
    }

    // === Factory Methods ===

    /**
     * Create data not found exception
     * 创建数据不存在异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenBizException dataNotFound(String message) {
        return new OpenBizException(CommonResultCode.DATA_NOT_FOUND, message);
    }

    /**
     * Create data duplicate exception
     * 创建数据重复异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenBizException dataDuplicate(String message) {
        return new OpenBizException(CommonResultCode.DATA_DUPLICATE, message);
    }

    /**
     * Create operation failed exception
     * 创建操作失败异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenBizException operationFailed(String message) {
        return new OpenBizException(CommonResultCode.OPERATION_FAILED, message);
    }

    /**
     * Create validation error exception
     * 创建验证错误异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenBizException validationError(String message) {
        return new OpenBizException(CommonResultCode.VALIDATION_ERROR, message);
    }

    /**
     * Create validation error exception with data
     * 创建带数据的验证错误异常
     *
     * @param message the message | 消息
     * @param errors the validation errors | 验证错误
     * @return the exception | 异常
     */
    public static OpenBizException validationError(String message, Object errors) {
        return new OpenBizException(CommonResultCode.VALIDATION_ERROR.getCode(), message, errors);
    }
}
