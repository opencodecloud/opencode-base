package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.grant.DeviceCodeResponse;
import cloud.opencode.base.oauth2.grant.GrantType;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import cloud.opencode.base.oauth2.internal.JsonParser;
import cloud.opencode.base.oauth2.oidc.UserInfo;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import cloud.opencode.base.oauth2.provider.OAuth2Provider;
import cloud.opencode.base.oauth2.token.InMemoryTokenStore;
import cloud.opencode.base.oauth2.token.TokenStore;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * OAuth2 Client
 * OAuth2 客户端
 *
 * <p>Main client for OAuth2 authentication flows.</p>
 * <p>OAuth2 认证流程的主客户端。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Authorization Code Flow - 授权码流程</li>
 *   <li>Client Credentials Flow - 客户端凭证流程</li>
 *   <li>Device Code Flow - 设备码流程</li>
 *   <li>Token refresh - Token 刷新</li>
 *   <li>Token management - Token 管理</li>
 *   <li>PKCE support - PKCE 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create client with provider
 * OAuth2Client client = OAuth2Client.builder()
 *     .provider(Providers.GOOGLE)
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .redirectUri("https://yourapp.com/callback")
 *     .build();
 *
 * // Generate authorization URL
 * PkceChallenge pkce = PkceChallenge.generate();
 * String authUrl = client.getAuthorizationUrl("state", pkce);
 *
 * // Exchange code for token
 * OAuth2Token token = client.exchangeCode(code, pkce.verifier());
 *
 * // Use token
 * String accessToken = token.accessToken();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe.</p>
 * <p>此类是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class OAuth2Client implements AutoCloseable {

    private final OAuth2Config config;
    private final OAuth2HttpClient httpClient;
    private final TokenStore tokenStore;
    private final boolean ownsHttpClient;

    /**
     * Create a new OAuth2 client
     * 创建新的 OAuth2 客户端
     *
     * @param config     the configuration | 配置
     * @param tokenStore the token store | Token 存储
     * @param httpClient the HTTP client | HTTP 客户端
     */
    OAuth2Client(OAuth2Config config, TokenStore tokenStore, OAuth2HttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.tokenStore = tokenStore != null ? tokenStore : new InMemoryTokenStore();
        this.ownsHttpClient = httpClient == null;
        this.httpClient = httpClient != null ? httpClient : new OAuth2HttpClient(config);
    }

    /**
     * Get the configuration
     * 获取配置
     *
     * @return the configuration | 配置
     */
    public OAuth2Config config() {
        return config;
    }

    // ==================== Authorization Code Flow ====================

    /**
     * Generate authorization URL
     * 生成授权 URL
     *
     * @param state the state parameter for CSRF protection | 用于 CSRF 保护的 state 参数
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state) {
        return getAuthorizationUrl(state, null, null);
    }

    /**
     * Generate authorization URL with PKCE
     * 生成带有 PKCE 的授权 URL
     *
     * @param state the state parameter | state 参数
     * @param pkce  the PKCE challenge | PKCE 挑战
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state, PkceChallenge pkce) {
        return getAuthorizationUrl(state, pkce, null);
    }

    /**
     * Generate authorization URL with PKCE and additional parameters
     * 生成带有 PKCE 和附加参数的授权 URL
     *
     * @param state            the state parameter | state 参数
     * @param pkce             the PKCE challenge | PKCE 挑战
     * @param additionalParams additional query parameters | 附加查询参数
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state, PkceChallenge pkce,
                                       Map<String, String> additionalParams) {
        if (config.authorizationEndpoint() == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG,
                    "Authorization endpoint is not configured");
        }

        // Validate HTTPS scheme (allow HTTP only for localhost / 127.0.0.1 for dev/testing)
        // 验证 HTTPS 协议（仅允许 localhost / 127.0.0.1 使用 HTTP，用于开发/测试）
        validateHttpsEndpoint(config.authorizationEndpoint());

        StringBuilder url = new StringBuilder(config.authorizationEndpoint())
                .append("?response_type=code")
                .append("&client_id=").append(encode(config.clientId()));

        if (config.redirectUri() != null) {
            url.append("&redirect_uri=").append(encode(config.redirectUri()));
        }

        if (state != null) {
            url.append("&state=").append(encode(state));
        }

        if (!config.scopes().isEmpty()) {
            url.append("&scope=").append(encode(String.join(" ", config.scopes())));
        }

        if (pkce != null) {
            url.append("&code_challenge=").append(encode(pkce.challenge()));
            url.append("&code_challenge_method=").append(encode(pkce.method()));
        }

        if (additionalParams != null) {
            additionalParams.forEach((k, v) -> url.append("&").append(encode(k)).append("=").append(encode(v)));
        }

        return url.toString();
    }

    /**
     * Exchange authorization code for token
     * 使用授权码交换 Token
     *
     * @param code the authorization code | 授权码
     * @return the token | Token
     */
    public OAuth2Token exchangeCode(String code) {
        return exchangeCode(code, null);
    }

    /**
     * Exchange authorization code for token with PKCE verifier
     * 使用授权码和 PKCE 验证器交换 Token
     *
     * @param code         the authorization code | 授权码
     * @param codeVerifier the PKCE code verifier | PKCE 代码验证器
     * @return the token | Token
     */
    public OAuth2Token exchangeCode(String code, String codeVerifier) {
        Objects.requireNonNull(code, "code cannot be null");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", GrantType.AUTHORIZATION_CODE.value());
        params.put("code", code);
        params.put("client_id", config.clientId());

        if (config.redirectUri() != null) {
            params.put("redirect_uri", config.redirectUri());
        }

        if (config.clientSecret() != null) {
            params.put("client_secret", config.clientSecret());
        }

        if (codeVerifier != null) {
            params.put("code_verifier", codeVerifier);
        }

        return requestToken(params);
    }

    // ==================== Client Credentials Flow ====================

    /**
     * Get token using client credentials
     * 使用客户端凭证获取 Token
     *
     * @return the token | Token
     */
    public OAuth2Token getClientCredentialsToken() {
        if (config.clientSecret() == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.MISSING_CLIENT_SECRET);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", GrantType.CLIENT_CREDENTIALS.value());
        params.put("client_id", config.clientId());
        params.put("client_secret", config.clientSecret());

        if (!config.scopes().isEmpty()) {
            params.put("scope", String.join(" ", config.scopes()));
        }

        return requestToken(params);
    }

    // ==================== Device Code Flow ====================

    /**
     * Request device code for device authorization flow
     * 请求设备码以进行设备授权流程
     *
     * @return the device code response | 设备码响应
     */
    public DeviceCodeResponse requestDeviceCode() {
        if (!config.hasDeviceAuthorizationEndpoint()) {
            throw new OAuth2Exception(OAuth2ErrorCode.DEVICE_CODE_NOT_SUPPORTED);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("client_id", config.clientId());

        if (!config.scopes().isEmpty()) {
            params.put("scope", String.join(" ", config.scopes()));
        }

        String response = httpClient.postForm(config.deviceAuthorizationEndpoint(), params);
        return parseDeviceCodeResponse(response);
    }

    /**
     * Poll for token using device code
     * 使用设备码轮询 Token
     *
     * @param deviceCode the device code | 设备码
     * @return the token if authorized, empty if still pending | 如果授权则返回 Token，仍在等待则返回空
     * @throws OAuth2Exception if authorization is denied or expired | 如果授权被拒绝或过期
     */
    public Optional<OAuth2Token> pollDeviceToken(String deviceCode) {
        Objects.requireNonNull(deviceCode, "deviceCode cannot be null");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", GrantType.DEVICE_CODE.value());
        params.put("device_code", deviceCode);
        params.put("client_id", config.clientId());

        try {
            return Optional.of(requestToken(params));
        } catch (OAuth2Exception e) {
            if (e.errorCode() == OAuth2ErrorCode.AUTHORIZATION_PENDING ||
                    e.errorCode() == OAuth2ErrorCode.SLOW_DOWN) {
                return Optional.empty();
            }
            throw e;
        }
    }

    // ==================== Token Refresh ====================

    /**
     * Refresh a token
     * 刷新 Token
     *
     * @param refreshToken the refresh token | 刷新令牌
     * @return the new token | 新 Token
     */
    public OAuth2Token refreshToken(String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", GrantType.REFRESH_TOKEN.value());
        params.put("refresh_token", refreshToken);
        params.put("client_id", config.clientId());

        if (config.clientSecret() != null) {
            params.put("client_secret", config.clientSecret());
        }

        return requestToken(params);
    }

    /**
     * Refresh a token
     * 刷新 Token
     *
     * @param token the token to refresh | 要刷新的 Token
     * @return the new token | 新 Token
     */
    public OAuth2Token refreshToken(OAuth2Token token) {
        if (!token.hasRefreshToken()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_REFRESH_FAILED,
                    "Token has no refresh token");
        }
        return refreshToken(token.refreshToken());
    }

    // ==================== Token Revocation ====================

    /**
     * Revoke a token
     * 撤销 Token
     *
     * @param token the token to revoke | 要撤销的 Token
     */
    public void revokeToken(String token) {
        if (!config.hasRevocationEndpoint()) {
            throw new OAuth2Exception(OAuth2ErrorCode.REVOCATION_NOT_SUPPORTED);
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("token", token);
        params.put("client_id", config.clientId());

        if (config.clientSecret() != null) {
            params.put("client_secret", config.clientSecret());
        }

        httpClient.postForm(config.revocationEndpoint(), params);
    }

    /**
     * Revoke a token
     * 撤销 Token
     *
     * @param token the token to revoke | 要撤销的 Token
     */
    public void revokeToken(OAuth2Token token) {
        revokeToken(token.accessToken());
    }

    // ==================== User Info ====================

    /**
     * Get user info using a token
     * 使用 Token 获取用户信息
     *
     * @param token the access token | 访问令牌
     * @return the user info | 用户信息
     */
    public UserInfo getUserInfo(OAuth2Token token) {
        if (!config.hasUserInfoEndpoint()) {
            throw new OAuth2Exception(OAuth2ErrorCode.USERINFO_NOT_SUPPORTED);
        }

        String response = httpClient.get(config.userInfoEndpoint(),
                Map.of("Authorization", token.toBearerHeader()));

        return UserInfo.fromJson(response);
    }

    // ==================== Token Store ====================

    /**
     * Store a token
     * 存储 Token
     *
     * @param key   the storage key | 存储键
     * @param token the token | Token
     */
    public void storeToken(String key, OAuth2Token token) {
        tokenStore.save(key, token);
    }

    /**
     * Get a stored token
     * 获取存储的 Token
     *
     * @param key the storage key | 存储键
     * @return the token if found | 找到的 Token
     */
    public Optional<OAuth2Token> getStoredToken(String key) {
        return tokenStore.load(key);
    }

    /**
     * Get a valid token, refreshing if necessary
     * 获取有效的 Token，必要时刷新
     *
     * @param key the storage key | 存储键
     * @return the valid token | 有效的 Token
     * @throws OAuth2Exception if no token found or refresh fails | 如果未找到 Token 或刷新失败
     */
    public OAuth2Token getValidToken(String key) {
        OAuth2Token token = tokenStore.load(key)
                .orElseThrow(() -> new OAuth2Exception(OAuth2ErrorCode.TOKEN_NOT_FOUND));

        if (token.isExpiringSoon(config.refreshThreshold()) && token.hasRefreshToken()) {
            try {
                OAuth2Token newToken = refreshToken(token);
                tokenStore.save(key, newToken);
                return newToken;
            } catch (OAuth2Exception e) {
                if (token.isExpired()) {
                    throw e;
                }
                // Return existing token if not yet expired
            }
        }

        if (token.isExpired()) {
            throw OAuth2Exception.tokenExpired();
        }

        return token;
    }

    /**
     * Remove a stored token
     * 移除存储的 Token
     *
     * @param key the storage key | 存储键
     */
    public void removeToken(String key) {
        tokenStore.delete(key);
    }

    // ==================== Private Methods ====================

    /**
     * Request token from token endpoint
     * 从 Token 端点请求 Token
     *
     * @param params the request parameters | 请求参数
     * @return the token | Token
     */
    private OAuth2Token requestToken(Map<String, String> params) {
        if (config.tokenEndpoint() == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.MISSING_TOKEN_ENDPOINT);
        }

        String response = httpClient.postForm(config.tokenEndpoint(), params);
        return parseTokenResponse(response);
    }

    /**
     * Parse token response
     * 解析 Token 响应
     *
     * @param json the JSON response | JSON 响应
     * @return the token | Token
     */
    private OAuth2Token parseTokenResponse(String json) {
        OAuth2Token.Builder builder = OAuth2Token.builder();

        builder.accessToken(JsonParser.getString(json, "access_token"));

        String tokenType = JsonParser.getString(json, "token_type");
        if (tokenType != null) {
            builder.tokenType(tokenType);
        }

        String refreshToken = JsonParser.getString(json, "refresh_token");
        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        String idToken = JsonParser.getString(json, "id_token");
        if (idToken != null) {
            builder.idToken(idToken);
        }

        String scope = JsonParser.getString(json, "scope");
        if (scope != null) {
            builder.scopeString(scope);
        }

        Long expiresIn = JsonParser.getLong(json, "expires_in");
        if (expiresIn != null) {
            builder.expiresIn(expiresIn);
        }

        return builder.build();
    }

    /**
     * Parse device code response
     * 解析设备码响应
     *
     * @param json the JSON response | JSON 响应
     * @return the device code response | 设备码响应
     */
    private DeviceCodeResponse parseDeviceCodeResponse(String json) {
        DeviceCodeResponse.Builder builder = DeviceCodeResponse.builder();

        builder.deviceCode(JsonParser.getString(json, "device_code"));
        builder.userCode(JsonParser.getString(json, "user_code"));
        builder.verificationUri(JsonParser.getString(json, "verification_uri"));

        String verificationUriComplete = JsonParser.getString(json, "verification_uri_complete");
        if (verificationUriComplete != null) {
            builder.verificationUriComplete(verificationUriComplete);
        }

        Long expiresIn = JsonParser.getLong(json, "expires_in");
        if (expiresIn != null) {
            builder.expiresIn(expiresIn.intValue());
        }

        Long interval = JsonParser.getLong(json, "interval");
        if (interval != null) {
            builder.interval(interval.intValue());
        }

        builder.createdAt(Instant.now());

        return builder.build();
    }

    /**
     * Validate that the endpoint URL uses HTTPS, allowing HTTP only for localhost / 127.0.0.1.
     * 验证端点 URL 使用 HTTPS，仅允许 localhost / 127.0.0.1 使用 HTTP。
     *
     * @param endpoint the endpoint URL | 端点 URL
     */
    private void validateHttpsEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            String scheme = uri.getScheme();
            if ("https".equalsIgnoreCase(scheme)) {
                return;
            }
            if ("http".equalsIgnoreCase(scheme)) {
                String host = uri.getHost();
                if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                        || "::1".equals(host)) {
                    return;
                }
            }
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG,
                    "Authorization endpoint must use HTTPS (HTTP allowed only for localhost/127.0.0.1)");
        } catch (IllegalArgumentException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG,
                    "Invalid authorization endpoint URL: " + e.getMessage());
        }
    }

    /**
     * URL encode a string
     * URL 编码字符串
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        if (ownsHttpClient) {
            httpClient.close();
        }
    }

    // ==================== Builder ====================

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
     * OAuth2Client Builder
     * OAuth2Client 构建器
     */
    public static class Builder {
        private OAuth2Config config;
        private OAuth2Provider provider;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private Set<String> scopes = new HashSet<>();
        private GrantType grantType = GrantType.AUTHORIZATION_CODE;
        private TokenStore tokenStore;
        private OAuth2HttpClient httpClient;

        /**
         * Set the configuration
         * 设置配置
         *
         * @param config the configuration | 配置
         * @return this builder | 此构建器
         */
        public Builder config(OAuth2Config config) {
            this.config = config;
            return this;
        }

        /**
         * Set the provider
         * 设置提供者
         *
         * @param provider the provider | 提供者
         * @return this builder | 此构建器
         */
        public Builder provider(OAuth2Provider provider) {
            this.provider = provider;
            return this;
        }

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
         * Add scopes
         * 添加权限范围
         *
         * @param scopes the scopes | 权限范围
         * @return this builder | 此构建器
         */
        public Builder scopes(String... scopes) {
            this.scopes.addAll(Arrays.asList(scopes));
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
         * Set the token store
         * 设置 Token 存储
         *
         * @param tokenStore the token store | Token 存储
         * @return this builder | 此构建器
         */
        public Builder tokenStore(TokenStore tokenStore) {
            this.tokenStore = tokenStore;
            return this;
        }

        /**
         * Set the HTTP client
         * 设置 HTTP 客户端
         *
         * @param httpClient the HTTP client | HTTP 客户端
         * @return this builder | 此构建器
         */
        public Builder httpClient(OAuth2HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Build the OAuth2Client
         * 构建 OAuth2Client
         *
         * @return the client | 客户端
         */
        public OAuth2Client build() {
            OAuth2Config finalConfig = this.config;

            if (finalConfig == null && provider != null) {
                finalConfig = provider.toConfig(clientId, clientSecret, redirectUri, scopes, grantType);
            }

            if (finalConfig == null) {
                // Build config from individual parameters
                if (clientId == null) {
                    throw new OAuth2Exception(OAuth2ErrorCode.MISSING_CLIENT_ID);
                }

                finalConfig = OAuth2Config.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .redirectUri(redirectUri)
                        .scopes(scopes)
                        .grantType(grantType)
                        .build();
            }

            return new OAuth2Client(finalConfig, tokenStore, httpClient);
        }
    }
}
