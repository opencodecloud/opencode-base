package cloud.opencode.base.oauth2.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * OAuth2 Exception
 * OAuth2 异常
 *
 * <p>Base exception class for all OAuth2 related errors, extending {@link OpenException}
 * for unified error handling across OpenCode components.</p>
 * <p>所有 OAuth2 相关错误的基础异常类，继承 {@link OpenException} 以实现 OpenCode 组件的统一错误处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extends OpenException for unified error hierarchy - 继承 OpenException 实现统一异常层次</li>
 *   <li>OAuth2 protocol exception with error code - 带错误码的 OAuth2 协议异常</li>
 *   <li>Carries error description and URI - 携带错误描述和 URI</li>
 *   <li>RFC 6749 server error response parsing - RFC 6749 服务器错误响应解析</li>
 *   <li>Formatted message output [OAuth2] (Code) Message - 格式化消息输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Throw with error code
 * throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
 *
 * // Throw with error code and details
 * throw new OAuth2Exception(OAuth2ErrorCode.AUTHORIZATION_FAILED, "Invalid code");
 *
 * // Throw with error code and cause
 * throw new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, e);
 *
 * // Create from server error response (RFC 6749)
 * throw OAuth2Exception.fromServerError("invalid_grant", "The authorization code has expired", "https://example.com/errors/expired");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class OAuth2Exception extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Component name for OpenException hierarchy
     * OpenException 层次结构中的组件名称
     */
    private static final String COMPONENT = "OAuth2";

    private final OAuth2ErrorCode errorCode;
    private final String details;
    private final String serverError;
    private final String serverErrorDescription;
    private final String serverErrorUri;

    /**
     * Create exception with error code
     * 使用错误码创建异常
     *
     * @param errorCode the error code | 错误码
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode) {
        super(COMPONENT, String.valueOf(errorCode.code()), errorCode.message(), null);
        this.errorCode = errorCode;
        this.details = null;
        this.serverError = null;
        this.serverErrorDescription = null;
        this.serverErrorUri = null;
    }

    /**
     * Create exception with error code and details
     * 使用错误码和详情创建异常
     *
     * @param errorCode the error code | 错误码
     * @param details   additional details | 附加详情
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details) {
        super(COMPONENT, String.valueOf(errorCode.code()),
                details != null ? errorCode.message() + ": " + details : errorCode.message(), null);
        this.errorCode = errorCode;
        this.details = details;
        this.serverError = null;
        this.serverErrorDescription = null;
        this.serverErrorUri = null;
    }

    /**
     * Create exception with error code and cause
     * 使用错误码和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param cause     the cause | 原因
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause) {
        super(COMPONENT, String.valueOf(errorCode.code()), errorCode.message(), cause);
        this.errorCode = errorCode;
        this.details = cause != null ? cause.getMessage() : null;
        this.serverError = null;
        this.serverErrorDescription = null;
        this.serverErrorUri = null;
    }

    /**
     * Create exception with error code, details and cause
     * 使用错误码、详情和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param details   additional details | 附加详情
     * @param cause     the cause | 原因
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details, Throwable cause) {
        super(COMPONENT, String.valueOf(errorCode.code()),
                details != null ? errorCode.message() + ": " + details : errorCode.message(), cause);
        this.errorCode = errorCode;
        this.details = details;
        this.serverError = null;
        this.serverErrorDescription = null;
        this.serverErrorUri = null;
    }

    /**
     * Create exception with error code and server error fields (RFC 6749)
     * 使用错误码和服务器错误字段创建异常（RFC 6749）
     *
     * @param errorCode              the error code | 错误码
     * @param details                additional details | 附加详情
     * @param serverError            the OAuth2 server error string | OAuth2 服务器错误字符串
     * @param serverErrorDescription the server error description | 服务器错误描述
     * @param serverErrorUri         the server error URI | 服务器错误 URI
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details,
                           String serverError, String serverErrorDescription, String serverErrorUri) {
        super(COMPONENT, String.valueOf(errorCode.code()),
                details != null ? errorCode.message() + ": " + details : errorCode.message(), null);
        this.errorCode = errorCode;
        this.details = details;
        this.serverError = serverError;
        this.serverErrorDescription = serverErrorDescription;
        this.serverErrorUri = serverErrorUri;
    }

    /**
     * Get the error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public OAuth2ErrorCode errorCode() {
        return errorCode;
    }

    /**
     * Get additional details
     * 获取附加详情
     *
     * @return the details or null | 详情或 null
     */
    public String details() {
        return details;
    }

    /**
     * Get the error code number
     * 获取错误码数字
     *
     * @return the error code number | 错误码数字
     */
    public int code() {
        return errorCode.code();
    }

    /**
     * Get the server error string from the OAuth2 error response (RFC 6749)
     * 获取 OAuth2 错误响应中的服务器错误字符串（RFC 6749）
     *
     * @return the server error string or null | 服务器错误字符串或 null
     */
    public String serverError() {
        return serverError;
    }

    /**
     * Get the server error description from the OAuth2 error response (RFC 6749)
     * 获取 OAuth2 错误响应中的服务器错误描述（RFC 6749）
     *
     * @return the server error description or null | 服务器错误描述或 null
     */
    public String serverErrorDescription() {
        return serverErrorDescription;
    }

    /**
     * Get the server error URI from the OAuth2 error response (RFC 6749)
     * 获取 OAuth2 错误响应中的服务器错误 URI（RFC 6749）
     *
     * @return the server error URI or null | 服务器错误 URI 或 null
     */
    public String serverErrorUri() {
        return serverErrorUri;
    }

    /**
     * Get the formatted exception message, including server error details when present.
     * 获取格式化的异常消息，当存在时包含服务器错误详情。
     *
     * @return the formatted message | 格式化的消息
     */
    @Override
    public String getMessage() {
        String base = super.getMessage();
        if (serverError == null && serverErrorDescription == null && serverErrorUri == null) {
            return base;
        }
        StringBuilder sb = new StringBuilder(base);
        if (serverError != null) {
            sb.append(" [server_error=").append(serverError).append("]");
        }
        if (serverErrorDescription != null) {
            sb.append(" [server_error_description=").append(serverErrorDescription).append("]");
        }
        if (serverErrorUri != null) {
            sb.append(" [server_error_uri=").append(serverErrorUri).append("]");
        }
        return sb.toString();
    }

    // ==================== Factory Methods ====================

    /**
     * Create token expired exception
     * 创建 Token 过期异常
     *
     * @return the exception | 异常
     */
    public static OAuth2Exception tokenExpired() {
        return new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
    }

    /**
     * Create token invalid exception
     * 创建 Token 无效异常
     *
     * @param details the details | 详情
     * @return the exception | 异常
     */
    public static OAuth2Exception tokenInvalid(String details) {
        return new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID, details);
    }

    /**
     * Create authorization failed exception
     * 创建授权失败异常
     *
     * @param details the details | 详情
     * @return the exception | 异常
     */
    public static OAuth2Exception authorizationFailed(String details) {
        return new OAuth2Exception(OAuth2ErrorCode.AUTHORIZATION_FAILED, details);
    }

    /**
     * Create network error exception
     * 创建网络错误异常
     *
     * @param cause the cause | 原因
     * @return the exception | 异常
     */
    public static OAuth2Exception networkError(Throwable cause) {
        return new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, cause);
    }

    /**
     * Create invalid config exception
     * 创建无效配置异常
     *
     * @param details the details | 详情
     * @return the exception | 异常
     */
    public static OAuth2Exception invalidConfig(String details) {
        return new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG, details);
    }

    /**
     * Create exception from an OAuth2 server error response (RFC 6749 Section 5.2)
     * 从 OAuth2 服务器错误响应创建异常（RFC 6749 第 5.2 节）
     *
     * @param error            the error code string from server | 服务器返回的错误码字符串
     * @param errorDescription the error description from server | 服务器返回的错误描述
     * @param errorUri         the error URI from server | 服务器返回的错误 URI
     * @return the exception | 异常
     */
    public static OAuth2Exception fromServerError(String error, String errorDescription, String errorUri) {
        String message = errorDescription != null ? errorDescription : error;
        return new OAuth2Exception(OAuth2ErrorCode.PROVIDER_ERROR, message,
                error, errorDescription, errorUri);
    }
}
