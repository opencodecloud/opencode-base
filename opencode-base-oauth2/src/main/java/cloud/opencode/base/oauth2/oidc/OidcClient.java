package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.OAuth2Client;
import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * OpenID Connect Client
 * OpenID Connect 客户端
 *
 * <p>Extends OAuth2 client with OIDC-specific functionality.</p>
 * <p>使用 OIDC 特定功能扩展 OAuth2 客户端。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ID token validation - ID Token 验证</li>
 *   <li>Nonce generation and validation - Nonce 生成和验证</li>
 *   <li>User info endpoint access - 用户信息端点访问</li>
 *   <li>Claims validation - 声明验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create OIDC client
 * OidcClient client = OidcClient.builder()
 *     .oauth2Client(oauth2Client)
 *     .oidcConfig(OidcConfig.builder()
 *         .issuer("https://accounts.google.com")
 *         .validateIdToken(true)
 *         .build())
 *     .build();
 *
 * // Generate authorization URL with nonce
 * String nonce = client.generateNonce();
 * String authUrl = client.getAuthorizationUrl(state, pkce, nonce);
 *
 * // Exchange code and validate
 * OidcToken token = client.exchangeCode(code, pkce.verifier(), nonce);
 *
 * // Get user info
 * UserInfo userInfo = client.getUserInfo(token);
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe.</p>
 * <p>此类是线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class OidcClient implements AutoCloseable {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OAuth2Client oauth2Client;
    private final OidcConfig oidcConfig;
    private final boolean ownsOAuth2Client;

    /**
     * Create an OIDC client
     * 创建 OIDC 客户端
     *
     * @param oauth2Client     the underlying OAuth2 client | 底层 OAuth2 客户端
     * @param oidcConfig       the OIDC configuration | OIDC 配置
     * @param ownsOAuth2Client whether this client owns the OAuth2 client | 此客户端是否拥有 OAuth2 客户端
     */
    OidcClient(OAuth2Client oauth2Client, OidcConfig oidcConfig, boolean ownsOAuth2Client) {
        this.oauth2Client = Objects.requireNonNull(oauth2Client, "oauth2Client cannot be null");
        this.oidcConfig = oidcConfig != null ? oidcConfig : OidcConfig.defaults();
        this.ownsOAuth2Client = ownsOAuth2Client;
    }

    /**
     * Get the underlying OAuth2 client
     * 获取底层 OAuth2 客户端
     *
     * @return the OAuth2 client | OAuth2 客户端
     */
    public OAuth2Client oauth2Client() {
        return oauth2Client;
    }

    /**
     * Get the OIDC configuration
     * 获取 OIDC 配置
     *
     * @return the OIDC config | OIDC 配置
     */
    public OidcConfig oidcConfig() {
        return oidcConfig;
    }

    // ==================== Authorization ====================

    /**
     * Generate a nonce for authorization request
     * 为授权请求生成 nonce
     *
     * @return the nonce | nonce
     */
    public String generateNonce() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Get authorization URL
     * 获取授权 URL
     *
     * @param state the state parameter | state 参数
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state) {
        return oauth2Client.getAuthorizationUrl(state);
    }

    /**
     * Get authorization URL with PKCE
     * 获取带有 PKCE 的授权 URL
     *
     * @param state the state parameter | state 参数
     * @param pkce  the PKCE challenge | PKCE 挑战
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state, PkceChallenge pkce) {
        return oauth2Client.getAuthorizationUrl(state, pkce);
    }

    /**
     * Get authorization URL with PKCE and nonce
     * 获取带有 PKCE 和 nonce 的授权 URL
     *
     * @param state the state parameter | state 参数
     * @param pkce  the PKCE challenge | PKCE 挑战
     * @param nonce the nonce for ID token validation | 用于 ID Token 验证的 nonce
     * @return the authorization URL | 授权 URL
     */
    public String getAuthorizationUrl(String state, PkceChallenge pkce, String nonce) {
        Map<String, String> additionalParams = new HashMap<>();
        if (nonce != null) {
            additionalParams.put("nonce", nonce);
        }
        return oauth2Client.getAuthorizationUrl(state, pkce, additionalParams);
    }

    // ==================== Token Exchange ====================

    /**
     * Exchange authorization code for OIDC token
     * 使用授权码交换 OIDC 令牌
     *
     * @param code the authorization code | 授权码
     * @return the OIDC token | OIDC 令牌
     */
    public OidcToken exchangeCode(String code) {
        return exchangeCode(code, null, null);
    }

    /**
     * Exchange authorization code for OIDC token with PKCE verifier
     * 使用授权码和 PKCE 验证器交换 OIDC 令牌
     *
     * @param code         the authorization code | 授权码
     * @param codeVerifier the PKCE code verifier | PKCE 代码验证器
     * @return the OIDC token | OIDC 令牌
     */
    public OidcToken exchangeCode(String code, String codeVerifier) {
        return exchangeCode(code, codeVerifier, null);
    }

    /**
     * Exchange authorization code for OIDC token with validation
     * 使用授权码交换带有验证的 OIDC 令牌
     *
     * @param code          the authorization code | 授权码
     * @param codeVerifier  the PKCE code verifier | PKCE 代码验证器
     * @param expectedNonce the expected nonce | 预期的 nonce
     * @return the OIDC token | OIDC 令牌
     * @throws OAuth2Exception if validation fails | 如果验证失败
     */
    public OidcToken exchangeCode(String code, String codeVerifier, String expectedNonce) {
        OAuth2Token oauth2Token = oauth2Client.exchangeCode(code, codeVerifier);
        OidcToken oidcToken = OidcToken.from(oauth2Token);

        if (oidcConfig.validateIdToken() && oidcToken.hasIdToken()) {
            validateIdToken(oidcToken, expectedNonce);
        }

        return oidcToken;
    }

    /**
     * Validate ID token
     * 验证 ID Token
     *
     * @param token         the OIDC token | OIDC 令牌
     * @param expectedNonce the expected nonce | 预期的 nonce
     * @throws OAuth2Exception if validation fails | 如果验证失败
     */
    public void validateIdToken(OidcToken token, String expectedNonce) {
        if (!token.hasIdToken()) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "No ID token present");
        }

        JwtClaims claims = token.idTokenClaims().orElseThrow(() ->
                new OAuth2Exception(OAuth2ErrorCode.TOKEN_PARSE_ERROR, "Failed to parse ID token"));

        // Validate expiration
        if (oidcConfig.validateExpiration()) {
            Instant exp = claims.exp();
            if (exp != null) {
                Instant now = Instant.now();
                Instant expWithSkew = exp.plus(oidcConfig.clockSkew());
                if (now.isAfter(expWithSkew)) {
                    throw OAuth2Exception.tokenExpired();
                }
            }
        }

        // Validate issuer
        if (oidcConfig.canValidateIssuer()) {
            String issuer = claims.iss();
            if (issuer == null || !issuer.equals(oidcConfig.issuer())) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID,
                        "Invalid issuer: expected " + oidcConfig.issuer() + ", got " + issuer);
            }
        }

        // Validate audience
        if (oidcConfig.validateAudience()) {
            String clientId = oauth2Client.config().clientId();
            if (!claims.hasAudience(clientId)) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID,
                        "Token audience does not include client ID");
            }
        }

        // Validate nonce
        if (oidcConfig.validateNonce() && expectedNonce != null) {
            String tokenNonce = claims.nonce();
            if (tokenNonce == null || !tokenNonce.equals(expectedNonce)) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID,
                        "Invalid nonce: expected " + expectedNonce + ", got " + tokenNonce);
            }
        }

        // Validate required claims
        for (String claim : oidcConfig.requiredClaims()) {
            if (claims.getClaim(claim).isEmpty()) {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID,
                        "Missing required claim: " + claim);
            }
        }
    }

    // ==================== Token Refresh ====================

    /**
     * Refresh an OIDC token
     * 刷新 OIDC 令牌
     *
     * @param token the token to refresh | 要刷新的令牌
     * @return the new OIDC token | 新 OIDC 令牌
     */
    public OidcToken refreshToken(OidcToken token) {
        OAuth2Token newOAuth2Token = oauth2Client.refreshToken(token.oauth2Token());
        return OidcToken.from(newOAuth2Token);
    }

    /**
     * Get a valid OIDC token, refreshing if necessary
     * 获取有效的 OIDC 令牌，必要时刷新
     *
     * @param key the storage key | 存储键
     * @return the valid OIDC token | 有效的 OIDC 令牌
     */
    public OidcToken getValidToken(String key) {
        OAuth2Token oauth2Token = oauth2Client.getValidToken(key);
        return OidcToken.from(oauth2Token);
    }

    // ==================== User Info ====================

    /**
     * Get user info
     * 获取用户信息
     *
     * @param token the OIDC token | OIDC 令牌
     * @return the user info | 用户信息
     */
    public UserInfo getUserInfo(OidcToken token) {
        return oauth2Client.getUserInfo(token.oauth2Token());
    }

    /**
     * Get user info using access token
     * 使用访问令牌获取用户信息
     *
     * @param token the OAuth2 token | OAuth2 令牌
     * @return the user info | 用户信息
     */
    public UserInfo getUserInfo(OAuth2Token token) {
        return oauth2Client.getUserInfo(token);
    }

    // ==================== Token Storage ====================

    /**
     * Store a token
     * 存储令牌
     *
     * @param key   the storage key | 存储键
     * @param token the OIDC token | OIDC 令牌
     */
    public void storeToken(String key, OidcToken token) {
        oauth2Client.storeToken(key, token.oauth2Token());
    }

    /**
     * Get a stored token
     * 获取存储的令牌
     *
     * @param key the storage key | 存储键
     * @return the OIDC token if found | 找到的 OIDC 令牌
     */
    public Optional<OidcToken> getStoredToken(String key) {
        return oauth2Client.getStoredToken(key).map(OidcToken::from);
    }

    /**
     * Remove a stored token
     * 移除存储的令牌
     *
     * @param key the storage key | 存储键
     */
    public void removeToken(String key) {
        oauth2Client.removeToken(key);
    }

    @Override
    public void close() {
        if (ownsOAuth2Client) {
            oauth2Client.close();
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
     * OidcClient Builder
     * OidcClient 构建器
     */
    public static class Builder {
        private OAuth2Client oauth2Client;
        private OidcConfig oidcConfig;
        private boolean ownsOAuth2Client;

        /**
         * Set the OAuth2 client
         * 设置 OAuth2 客户端
         *
         * @param oauth2Client the OAuth2 client | OAuth2 客户端
         * @return this builder | 此构建器
         */
        public Builder oauth2Client(OAuth2Client oauth2Client) {
            this.oauth2Client = oauth2Client;
            this.ownsOAuth2Client = false;
            return this;
        }

        /**
         * Set the OAuth2 client (owned)
         * 设置 OAuth2 客户端（拥有）
         *
         * @param oauth2Client the OAuth2 client | OAuth2 客户端
         * @return this builder | 此构建器
         */
        public Builder ownedOAuth2Client(OAuth2Client oauth2Client) {
            this.oauth2Client = oauth2Client;
            this.ownsOAuth2Client = true;
            return this;
        }

        /**
         * Set the OIDC configuration
         * 设置 OIDC 配置
         *
         * @param oidcConfig the OIDC config | OIDC 配置
         * @return this builder | 此构建器
         */
        public Builder oidcConfig(OidcConfig oidcConfig) {
            this.oidcConfig = oidcConfig;
            return this;
        }

        /**
         * Build the OidcClient
         * 构建 OidcClient
         *
         * @return the OIDC client | OIDC 客户端
         */
        public OidcClient build() {
            Objects.requireNonNull(oauth2Client, "oauth2Client is required");
            return new OidcClient(oauth2Client, oidcConfig, ownsOAuth2Client);
        }
    }
}
