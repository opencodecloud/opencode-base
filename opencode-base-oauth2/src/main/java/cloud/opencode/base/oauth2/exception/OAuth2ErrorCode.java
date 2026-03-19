package cloud.opencode.base.oauth2.exception;

/**
 * OAuth2 Error Code Enum
 * OAuth2 错误码枚举
 *
 * <p>Defines all error codes for OAuth2 operations.</p>
 * <p>定义所有 OAuth2 操作的错误码。</p>
 *
 * <p><strong>Error Code Ranges | 错误码范围:</strong></p>
 * <ul>
 *   <li>7001-7020: Token errors - Token 错误</li>
 *   <li>7021-7040: Authorization errors - 授权错误</li>
 *   <li>7041-7060: Provider errors - Provider 错误</li>
 *   <li>7061-7070: PKCE errors - PKCE 错误</li>
 *   <li>7071-7080: Network errors - 网络错误</li>
 *   <li>7081-7090: Configuration errors - 配置错误</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard OAuth2 error code enumeration - 标准OAuth2错误码枚举</li>
 *   <li>RFC 6749 compliant error codes - 符合RFC 6749的错误码</li>
 *   <li>Human-readable error descriptions - 可读的错误描述</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check error codes in OAuth2 responses
 * // 检查OAuth2响应中的错误码
 * OAuth2ErrorCode code = OAuth2ErrorCode.INVALID_GRANT;
 * String description = code.getDescription();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public enum OAuth2ErrorCode {

    // ==================== Token错误 (7001-7020) ====================

    /**
     * Token has expired
     * Token 已过期
     */
    TOKEN_EXPIRED(7001, "Token has expired"),

    /**
     * Token is invalid
     * Token 无效
     */
    TOKEN_INVALID(7002, "Token is invalid"),

    /**
     * Failed to refresh token
     * Token 刷新失败
     */
    TOKEN_REFRESH_FAILED(7003, "Failed to refresh token"),

    /**
     * Token has been revoked
     * Token 已被撤销
     */
    TOKEN_REVOKED(7004, "Token has been revoked"),

    /**
     * Token store error
     * Token 存储错误
     */
    TOKEN_STORE_ERROR(7005, "Token store error"),

    /**
     * Token not found
     * Token 未找到
     */
    TOKEN_NOT_FOUND(7006, "Token not found"),

    /**
     * Token parse error
     * Token 解析错误
     */
    TOKEN_PARSE_ERROR(7007, "Token parse error"),

    // ==================== 授权错误 (7021-7040) ====================

    /**
     * Authorization failed
     * 授权失败
     */
    AUTHORIZATION_FAILED(7021, "Authorization failed"),

    /**
     * Authorization denied by user
     * 用户拒绝授权
     */
    AUTHORIZATION_DENIED(7022, "Authorization denied by user"),

    /**
     * Invalid authorization grant
     * 无效的授权许可
     */
    INVALID_GRANT(7023, "Invalid authorization grant"),

    /**
     * Invalid scope
     * 无效的权限范围
     */
    INVALID_SCOPE(7024, "Invalid scope"),

    /**
     * Invalid state parameter
     * 无效的 state 参数
     */
    INVALID_STATE(7025, "Invalid state parameter"),

    /**
     * Authorization code expired
     * 授权码已过期
     */
    CODE_EXPIRED(7026, "Authorization code expired"),

    /**
     * Authorization pending (device code flow)
     * 授权待定（设备码流程）
     */
    AUTHORIZATION_PENDING(7027, "Authorization pending"),

    /**
     * Slow down polling (device code flow)
     * 降低轮询频率（设备码流程）
     */
    SLOW_DOWN(7028, "Slow down polling"),

    /**
     * Access denied
     * 访问被拒绝
     */
    ACCESS_DENIED(7029, "Access denied"),

    // ==================== Provider错误 (7041-7060) ====================

    /**
     * OAuth2 provider not found
     * OAuth2 Provider 未找到
     */
    PROVIDER_NOT_FOUND(7041, "OAuth2 provider not found"),

    /**
     * OAuth2 provider error
     * OAuth2 Provider 错误
     */
    PROVIDER_ERROR(7042, "OAuth2 provider error"),

    /**
     * User info endpoint not supported
     * 用户信息端点不支持
     */
    USERINFO_NOT_SUPPORTED(7043, "User info endpoint not supported"),

    /**
     * Token revocation not supported
     * Token 撤销不支持
     */
    REVOCATION_NOT_SUPPORTED(7044, "Token revocation not supported"),

    /**
     * Device code endpoint not supported
     * 设备码端点不支持
     */
    DEVICE_CODE_NOT_SUPPORTED(7045, "Device code endpoint not supported"),

    // ==================== PKCE错误 (7061-7070) ====================

    /**
     * PKCE error
     * PKCE 错误
     */
    PKCE_ERROR(7061, "PKCE error"),

    /**
     * PKCE is required
     * PKCE 是必需的
     */
    PKCE_REQUIRED(7062, "PKCE is required"),

    /**
     * Invalid PKCE verifier
     * 无效的 PKCE 验证器
     */
    INVALID_PKCE_VERIFIER(7063, "Invalid PKCE verifier"),

    // ==================== 网络错误 (7071-7080) ====================

    /**
     * Network error
     * 网络错误
     */
    NETWORK_ERROR(7071, "Network error"),

    /**
     * Request timeout
     * 请求超时
     */
    TIMEOUT(7072, "Request timeout"),

    /**
     * Server error
     * 服务器错误
     */
    SERVER_ERROR(7073, "Server error"),

    /**
     * Invalid response
     * 无效的响应
     */
    INVALID_RESPONSE(7074, "Invalid response"),

    // ==================== 配置错误 (7081-7090) ====================

    /**
     * Invalid OAuth2 configuration
     * 无效的 OAuth2 配置
     */
    INVALID_CONFIG(7081, "Invalid OAuth2 configuration"),

    /**
     * Client ID is required
     * Client ID 是必需的
     */
    MISSING_CLIENT_ID(7082, "Client ID is required"),

    /**
     * Client secret is required
     * Client Secret 是必需的
     */
    MISSING_CLIENT_SECRET(7083, "Client secret is required"),

    /**
     * Redirect URI is required
     * Redirect URI 是必需的
     */
    MISSING_REDIRECT_URI(7084, "Redirect URI is required"),

    /**
     * Token endpoint is required
     * Token 端点是必需的
     */
    MISSING_TOKEN_ENDPOINT(7085, "Token endpoint is required");

    private final int code;
    private final String message;

    OAuth2ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the error code number
     * 获取错误码数字
     *
     * @return the error code | 错误码
     */
    public int code() {
        return code;
    }

    /**
     * Get the error message
     * 获取错误消息
     *
     * @return the error message | 错误消息
     */
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}
