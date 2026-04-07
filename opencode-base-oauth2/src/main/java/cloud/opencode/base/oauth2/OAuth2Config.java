package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.grant.GrantType;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * OAuth2 Configuration Record
 * OAuth2 配置记录
 *
 * <p>Immutable configuration for OAuth2 client.</p>
 * <p>OAuth2 客户端的不可变配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Client credentials - 客户端凭证</li>
 *   <li>Endpoint configuration - 端点配置</li>
 *   <li>Scope management - 权限范围管理</li>
 *   <li>PKCE settings - PKCE 设置</li>
 *   <li>Timeout configuration - 超时配置</li>
 *   <li>PAR endpoint (RFC 9126) - PAR 端点</li>
 *   <li>Introspection endpoint (RFC 7662) - 内省端点</li>
 *   <li>Resource indicator (RFC 8707) - 资源指示器</li>
 *   <li>Issuer validation (RFC 9207) - 颁发者验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build configuration
 * OAuth2Config config = OAuth2Config.builder()
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .authorizationEndpoint("https://oauth.example.com/authorize")
 *     .tokenEndpoint("https://oauth.example.com/token")
 *     .redirectUri("https://yourapp.com/callback")
 *     .scopes("openid", "email", "profile")
 *     .parEndpoint("https://oauth.example.com/par")
 *     .introspectionEndpoint("https://oauth.example.com/introspect")
 *     .resource("https://api.example.com")
 *     .expectedIssuer("https://oauth.example.com")
 *     .build();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is immutable and thread-safe.</p>
 * <p>此类是不可变的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record OAuth2Config(
        String clientId,
        String clientSecret,
        String authorizationEndpoint,
        String tokenEndpoint,
        String userInfoEndpoint,
        String revocationEndpoint,
        String deviceAuthorizationEndpoint,
        String redirectUri,
        Set<String> scopes,
        GrantType grantType,
        boolean usePkce,
        Duration connectTimeout,
        Duration readTimeout,
        Duration refreshThreshold,
        String parEndpoint,
        String introspectionEndpoint,
        String resource,
        String expectedIssuer
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public OAuth2Config {
        Objects.requireNonNull(clientId, "clientId cannot be null");
        scopes = scopes != null ? Set.copyOf(scopes) : Set.of();
        grantType = grantType != null ? grantType : GrantType.AUTHORIZATION_CODE;
        connectTimeout = connectTimeout != null ? connectTimeout : Duration.ofSeconds(10);
        readTimeout = readTimeout != null ? readTimeout : Duration.ofSeconds(30);
        refreshThreshold = refreshThreshold != null ? refreshThreshold : Duration.ofMinutes(5);
    }

    /**
     * Returns a string representation with clientSecret redacted.
     * 返回 clientSecret 已脱敏的字符串表示。
     */
    @Override
    public String toString() {
        return "OAuth2Config[clientId=" + clientId
                + ", clientSecret=" + (clientSecret != null ? "***" : "null")
                + ", tokenEndpoint=" + tokenEndpoint
                + ", grantType=" + grantType + "]";
    }

    /**
     * Check if this config is for authorization code flow
     * 检查此配置是否用于授权码流程
     *
     * @return true if authorization code flow | 授权码流程返回 true
     */
    public boolean isAuthorizationCodeFlow() {
        return grantType == GrantType.AUTHORIZATION_CODE;
    }

    /**
     * Check if this config is for client credentials flow
     * 检查此配置是否用于客户端凭证流程
     *
     * @return true if client credentials flow | 客户端凭证流程返回 true
     */
    public boolean isClientCredentialsFlow() {
        return grantType == GrantType.CLIENT_CREDENTIALS;
    }

    /**
     * Check if this config is for device code flow
     * 检查此配置是否用于设备码流程
     *
     * @return true if device code flow | 设备码流程返回 true
     */
    public boolean isDeviceCodeFlow() {
        return grantType == GrantType.DEVICE_CODE;
    }

    /**
     * Check if user info endpoint is configured
     * 检查是否配置了用户信息端点
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasUserInfoEndpoint() {
        return userInfoEndpoint != null && !userInfoEndpoint.isBlank();
    }

    /**
     * Check if revocation endpoint is configured
     * 检查是否配置了撤销端点
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasRevocationEndpoint() {
        return revocationEndpoint != null && !revocationEndpoint.isBlank();
    }

    /**
     * Check if device authorization endpoint is configured
     * 检查是否配置了设备授权端点
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasDeviceAuthorizationEndpoint() {
        return deviceAuthorizationEndpoint != null && !deviceAuthorizationEndpoint.isBlank();
    }

    /**
     * Check if PAR endpoint is configured (RFC 9126)
     * 检查是否配置了 PAR 端点（RFC 9126）
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasParEndpoint() {
        return parEndpoint != null && !parEndpoint.isBlank();
    }

    /**
     * Check if introspection endpoint is configured (RFC 7662)
     * 检查是否配置了内省端点（RFC 7662）
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasIntrospectionEndpoint() {
        return introspectionEndpoint != null && !introspectionEndpoint.isBlank();
    }

    /**
     * Check if resource indicator is configured (RFC 8707)
     * 检查是否配置了资源指示器（RFC 8707）
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasResource() {
        return resource != null && !resource.isBlank();
    }

    /**
     * Check if expected issuer is configured for validation (RFC 9207)
     * 检查是否配置了用于验证的预期颁发者（RFC 9207）
     *
     * @return true if configured | 已配置返回 true
     */
    public boolean hasExpectedIssuer() {
        return expectedIssuer != null && !expectedIssuer.isBlank();
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * OAuth2Config Builder
     * OAuth2Config 构建器
     */
    public static class Builder {
        private String clientId;
        private String clientSecret;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String userInfoEndpoint;
        private String revocationEndpoint;
        private String deviceAuthorizationEndpoint;
        private String redirectUri;
        private Set<String> scopes = new HashSet<>();
        private GrantType grantType = GrantType.AUTHORIZATION_CODE;
        private boolean usePkce = true;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration refreshThreshold = Duration.ofMinutes(5);
        private String parEndpoint;
        private String introspectionEndpoint;
        private String resource;
        private String expectedIssuer;

        /**
         * Set the client ID
         * 设置客户端 ID
         *
         * @param clientId the client ID | 客户端 ID
         * @return this builder | 此构建器
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Set the client secret
         * 设置客户端密钥
         *
         * @param clientSecret the client secret | 客户端密钥
         * @return this builder | 此构建器
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Set the authorization endpoint
         * 设置授权端点
         *
         * @param authorizationEndpoint the authorization endpoint | 授权端点
         * @return this builder | 此构建器
         */
        public Builder authorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
            return this;
        }

        /**
         * Set the token endpoint
         * 设置令牌端点
         *
         * @param tokenEndpoint the token endpoint | 令牌端点
         * @return this builder | 此构建器
         */
        public Builder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        /**
         * Set the user info endpoint
         * 设置用户信息端点
         *
         * @param userInfoEndpoint the user info endpoint | 用户信息端点
         * @return this builder | 此构建器
         */
        public Builder userInfoEndpoint(String userInfoEndpoint) {
            this.userInfoEndpoint = userInfoEndpoint;
            return this;
        }

        /**
         * Set the revocation endpoint
         * 设置撤销端点
         *
         * @param revocationEndpoint the revocation endpoint | 撤销端点
         * @return this builder | 此构建器
         */
        public Builder revocationEndpoint(String revocationEndpoint) {
            this.revocationEndpoint = revocationEndpoint;
            return this;
        }

        /**
         * Set the device authorization endpoint
         * 设置设备授权端点
         *
         * @param deviceAuthorizationEndpoint the device authorization endpoint | 设备授权端点
         * @return this builder | 此构建器
         */
        public Builder deviceAuthorizationEndpoint(String deviceAuthorizationEndpoint) {
            this.deviceAuthorizationEndpoint = deviceAuthorizationEndpoint;
            return this;
        }

        /**
         * Set the redirect URI
         * 设置重定向 URI
         *
         * @param redirectUri the redirect URI | 重定向 URI
         * @return this builder | 此构建器
         */
        public Builder redirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        /**
         * Set the scopes
         * 设置权限范围
         *
         * @param scopes the scopes | 权限范围
         * @return this builder | 此构建器
         */
        public Builder scopes(String... scopes) {
            this.scopes.addAll(Arrays.asList(scopes));
            return this;
        }

        /**
         * Set the scopes from a set
         * 从集合设置权限范围
         *
         * @param scopes the scopes | 权限范围
         * @return this builder | 此构建器
         */
        public Builder scopes(Set<String> scopes) {
            this.scopes = scopes != null ? new HashSet<>(scopes) : new HashSet<>();
            return this;
        }

        /**
         * Add a scope
         * 添加权限范围
         *
         * @param scope the scope | 权限范围
         * @return this builder | 此构建器
         */
        public Builder scope(String scope) {
            this.scopes.add(scope);
            return this;
        }

        /**
         * Set the grant type
         * 设置授权类型
         *
         * @param grantType the grant type | 授权类型
         * @return this builder | 此构建器
         */
        public Builder grantType(GrantType grantType) {
            this.grantType = grantType;
            return this;
        }

        /**
         * Enable or disable PKCE
         * 启用或禁用 PKCE
         *
         * @param usePkce true to enable PKCE | true 启用 PKCE
         * @return this builder | 此构建器
         */
        public Builder usePkce(boolean usePkce) {
            this.usePkce = usePkce;
            return this;
        }

        /**
         * Set the connect timeout
         * 设置连接超时
         *
         * @param connectTimeout the connect timeout | 连接超时
         * @return this builder | 此构建器
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Set the read timeout
         * 设置读取超时
         *
         * @param readTimeout the read timeout | 读取超时
         * @return this builder | 此构建器
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Set the refresh threshold
         * 设置刷新阈值
         *
         * <p>Token will be refreshed when remaining time is less than this threshold.</p>
         * <p>当剩余时间小于此阈值时将刷新令牌。</p>
         *
         * @param refreshThreshold the refresh threshold | 刷新阈值
         * @return this builder | 此构建器
         */
        public Builder refreshThreshold(Duration refreshThreshold) {
            this.refreshThreshold = refreshThreshold;
            return this;
        }

        /**
         * Set the Pushed Authorization Request endpoint (RFC 9126)
         * 设置推送授权请求端点（RFC 9126）
         *
         * @param parEndpoint the PAR endpoint | PAR 端点
         * @return this builder | 此构建器
         */
        public Builder parEndpoint(String parEndpoint) {
            this.parEndpoint = parEndpoint;
            return this;
        }

        /**
         * Set the token introspection endpoint (RFC 7662)
         * 设置令牌内省端点（RFC 7662）
         *
         * @param introspectionEndpoint the introspection endpoint | 内省端点
         * @return this builder | 此构建器
         */
        public Builder introspectionEndpoint(String introspectionEndpoint) {
            this.introspectionEndpoint = introspectionEndpoint;
            return this;
        }

        /**
         * Set the resource indicator (RFC 8707)
         * 设置资源指示器（RFC 8707）
         *
         * @param resource the resource indicator | 资源指示器
         * @return this builder | 此构建器
         */
        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        /**
         * Set the expected issuer for validation (RFC 9207)
         * 设置用于验证的预期颁发者（RFC 9207）
         *
         * @param expectedIssuer the expected issuer | 预期颁发者
         * @return this builder | 此构建器
         */
        public Builder expectedIssuer(String expectedIssuer) {
            this.expectedIssuer = expectedIssuer;
            return this;
        }

        /**
         * Build the OAuth2Config
         * 构建 OAuth2Config
         *
         * @return the config | 配置
         * @throws NullPointerException if clientId is null | 如果 clientId 为 null
         */
        public OAuth2Config build() {
            Objects.requireNonNull(clientId, "clientId is required");
            return new OAuth2Config(
                    clientId,
                    clientSecret,
                    authorizationEndpoint,
                    tokenEndpoint,
                    userInfoEndpoint,
                    revocationEndpoint,
                    deviceAuthorizationEndpoint,
                    redirectUri,
                    scopes,
                    grantType,
                    usePkce,
                    connectTimeout,
                    readTimeout,
                    refreshThreshold,
                    parEndpoint,
                    introspectionEndpoint,
                    resource,
                    expectedIssuer
            );
        }
    }
}
