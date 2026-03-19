package cloud.opencode.base.oauth2.grant;

/**
 * OAuth2 Grant Type Enum
 * OAuth2 授权类型枚举
 *
 * <p>Defines the supported OAuth2 grant types.</p>
 * <p>定义支持的 OAuth2 授权类型。</p>
 *
 * <p><strong>Supported Grant Types | 支持的授权类型:</strong></p>
 * <ul>
 *   <li>AUTHORIZATION_CODE - Web applications - Web 应用</li>
 *   <li>CLIENT_CREDENTIALS - Server-to-server - 服务器间通信</li>
 *   <li>DEVICE_CODE - CLI/IoT devices - CLI/IoT 设备</li>
 *   <li>REFRESH_TOKEN - Token refresh - Token 刷新</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OAuth2 grant type enumeration - OAuth2授权类型枚举</li>
 *   <li>Supports authorization_code, client_credentials, device_code, refresh_token - 支持授权码、客户端凭证、设备码、刷新令牌</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Select grant type for OAuth2 flow
 * // 为OAuth2流程选择授权类型
 * GrantType type = GrantType.AUTHORIZATION_CODE;
 * String value = type.getValue();
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
public enum GrantType {

    /**
     * Authorization Code Grant
     * 授权码模式
     *
     * <p>Best for web applications with a backend server.</p>
     * <p>最适合有后端服务器的 Web 应用。</p>
     */
    AUTHORIZATION_CODE("authorization_code"),

    /**
     * Client Credentials Grant
     * 客户端凭证模式
     *
     * <p>Best for server-to-server communication without user context.</p>
     * <p>最适合无用户上下文的服务器间通信。</p>
     */
    CLIENT_CREDENTIALS("client_credentials"),

    /**
     * Device Code Grant (RFC 8628)
     * 设备码模式
     *
     * <p>Best for devices with limited input capabilities (CLI, IoT, TV).</p>
     * <p>最适合输入能力有限的设备（CLI、IoT、TV）。</p>
     */
    DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code"),

    /**
     * Refresh Token Grant
     * 刷新令牌模式
     *
     * <p>Used to obtain a new access token using a refresh token.</p>
     * <p>用于使用刷新令牌获取新的访问令牌。</p>
     */
    REFRESH_TOKEN("refresh_token");

    private final String value;

    GrantType(String value) {
        this.value = value;
    }

    /**
     * Get the grant type value for OAuth2 requests
     * 获取用于 OAuth2 请求的授权类型值
     *
     * @return the grant type value | 授权类型值
     */
    public String value() {
        return value;
    }

    /**
     * Parse grant type from string value
     * 从字符串值解析授权类型
     *
     * @param value the string value | 字符串值
     * @return the grant type | 授权类型
     * @throws IllegalArgumentException if value is not recognized | 如果值无法识别
     */
    public static GrantType fromValue(String value) {
        for (GrantType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown grant type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
