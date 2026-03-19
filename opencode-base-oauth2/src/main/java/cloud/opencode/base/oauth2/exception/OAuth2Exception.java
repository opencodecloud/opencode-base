package cloud.opencode.base.oauth2.exception;

/**
 * OAuth2 Exception
 * OAuth2 异常
 *
 * <p>Base exception class for all OAuth2 related errors.</p>
 * <p>所有 OAuth2 相关错误的基础异常类。</p>
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
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OAuth2 protocol exception with error code - 带错误码的OAuth2协议异常</li>
 *   <li>Carries error description and URI - 携带错误描述和URI</li>
 * </ul>
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
public class OAuth2Exception extends RuntimeException {

    private final OAuth2ErrorCode errorCode;
    private final String details;

    /**
     * Create exception with error code
     * 使用错误码创建异常
     *
     * @param errorCode the error code | 错误码
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Create exception with error code and details
     * 使用错误码和详情创建异常
     *
     * @param errorCode the error code | 错误码
     * @param details   additional details | 附加详情
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, String details) {
        super(errorCode.message() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Create exception with error code and cause
     * 使用错误码和原因创建异常
     *
     * @param errorCode the error code | 错误码
     * @param cause     the cause | 原因
     */
    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
        this.details = cause != null ? cause.getMessage() : null;
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
        super(errorCode.message() + ": " + details, cause);
        this.errorCode = errorCode;
        this.details = details;
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
}
