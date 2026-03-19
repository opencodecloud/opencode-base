package cloud.opencode.base.oauth2.oidc;

import cloud.opencode.base.oauth2.OAuth2Token;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * OpenID Connect Token
 * OpenID Connect 令牌
 *
 * <p>Wraps an OAuth2 token with parsed ID token claims.</p>
 * <p>包装带有已解析 ID Token 声明的 OAuth2 令牌。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access to parsed ID token claims - 访问已解析的 ID Token 声明</li>
 *   <li>User identity information - 用户身份信息</li>
 *   <li>Token validation status - Token 验证状态</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from OAuth2 token
 * OidcToken oidcToken = OidcToken.from(oauth2Token);
 *
 * // Access claims
 * String subject = oidcToken.subject();
 * String email = oidcToken.email();
 * String name = oidcToken.name();
 *
 * // Check validation
 * if (oidcToken.isValid()) {
 *     // Token is valid
 * }
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is immutable and thread-safe.</p>
 * <p>此类是不可变的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#IDToken">OIDC ID Token</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public final class OidcToken {

    private final OAuth2Token oauth2Token;
    private final JwtClaims idTokenClaims;

    /**
     * Create an OIDC token
     * 创建 OIDC 令牌
     *
     * @param oauth2Token   the OAuth2 token | OAuth2 令牌
     * @param idTokenClaims the parsed ID token claims | 已解析的 ID Token 声明
     */
    public OidcToken(OAuth2Token oauth2Token, JwtClaims idTokenClaims) {
        this.oauth2Token = Objects.requireNonNull(oauth2Token, "oauth2Token cannot be null");
        this.idTokenClaims = idTokenClaims;
    }

    /**
     * Create from OAuth2 token by parsing the ID token
     * 通过解析 ID Token 从 OAuth2 令牌创建
     *
     * @param oauth2Token the OAuth2 token | OAuth2 令牌
     * @return the OIDC token | OIDC 令牌
     */
    public static OidcToken from(OAuth2Token oauth2Token) {
        Objects.requireNonNull(oauth2Token, "oauth2Token cannot be null");
        JwtClaims claims = null;
        if (oauth2Token.idToken() != null && !oauth2Token.idToken().isBlank()) {
            claims = JwtClaims.parse(oauth2Token.idToken());
        }
        return new OidcToken(oauth2Token, claims);
    }

    /**
     * Get the underlying OAuth2 token
     * 获取底层 OAuth2 令牌
     *
     * @return the OAuth2 token | OAuth2 令牌
     */
    public OAuth2Token oauth2Token() {
        return oauth2Token;
    }

    /**
     * Get the parsed ID token claims
     * 获取已解析的 ID Token 声明
     *
     * @return the claims if available | 声明（如果可用）
     */
    public Optional<JwtClaims> idTokenClaims() {
        return Optional.ofNullable(idTokenClaims);
    }

    /**
     * Check if ID token is present
     * 检查是否存在 ID Token
     *
     * @return true if ID token exists | 如果存在 ID Token 返回 true
     */
    public boolean hasIdToken() {
        return idTokenClaims != null;
    }

    // ==================== Delegated OAuth2Token Methods ====================

    /**
     * Get the access token
     * 获取访问令牌
     *
     * @return the access token | 访问令牌
     */
    public String accessToken() {
        return oauth2Token.accessToken();
    }

    /**
     * Get the refresh token
     * 获取刷新令牌
     *
     * @return the refresh token | 刷新令牌
     */
    public String refreshToken() {
        return oauth2Token.refreshToken();
    }

    /**
     * Get the raw ID token string
     * 获取原始 ID Token 字符串
     *
     * @return the ID token string | ID Token 字符串
     */
    public String idToken() {
        return oauth2Token.idToken();
    }

    /**
     * Check if token is expired
     * 检查令牌是否已过期
     *
     * @return true if expired | 如果已过期返回 true
     */
    public boolean isExpired() {
        return oauth2Token.isExpired();
    }

    /**
     * Check if token is expiring soon
     * 检查令牌是否即将过期
     *
     * @param threshold the time threshold | 时间阈值
     * @return true if expiring within threshold | 如果在阈值内即将过期返回 true
     */
    public boolean isExpiringSoon(Duration threshold) {
        return oauth2Token.isExpiringSoon(threshold);
    }

    /**
     * Check if token has refresh token
     * 检查令牌是否有刷新令牌
     *
     * @return true if has refresh token | 如果有刷新令牌返回 true
     */
    public boolean hasRefreshToken() {
        return oauth2Token.hasRefreshToken();
    }

    /**
     * Get the Bearer authorization header
     * 获取 Bearer 授权头
     *
     * @return the header value | 头值
     */
    public String toBearerHeader() {
        return oauth2Token.toBearerHeader();
    }

    // ==================== ID Token Claims Accessors ====================

    /**
     * Get the subject (user ID)
     * 获取主题（用户 ID）
     *
     * @return the subject | 主题
     */
    public String subject() {
        return idTokenClaims != null ? idTokenClaims.sub() : null;
    }

    /**
     * Get the issuer
     * 获取发行者
     *
     * @return the issuer | 发行者
     */
    public String issuer() {
        return idTokenClaims != null ? idTokenClaims.iss() : null;
    }

    /**
     * Get the audience
     * 获取受众
     *
     * @return the audience | 受众
     */
    public String audience() {
        return idTokenClaims != null ? idTokenClaims.audience() : null;
    }

    /**
     * Get the expiration time
     * 获取过期时间
     *
     * @return the expiration time | 过期时间
     */
    public Instant expiration() {
        return idTokenClaims != null ? idTokenClaims.exp() : null;
    }

    /**
     * Get the issued at time
     * 获取发行时间
     *
     * @return the issued at time | 发行时间
     */
    public Instant issuedAt() {
        return idTokenClaims != null ? idTokenClaims.iat() : null;
    }

    /**
     * Get the nonce
     * 获取 nonce
     *
     * @return the nonce | nonce
     */
    public String nonce() {
        return idTokenClaims != null ? idTokenClaims.nonce() : null;
    }

    /**
     * Get the email claim
     * 获取电子邮件声明
     *
     * @return the email | 电子邮件
     */
    public String email() {
        if (idTokenClaims == null) return null;
        return idTokenClaims.getClaimAsString("email").orElse(null);
    }

    /**
     * Check if email is verified
     * 检查电子邮件是否已验证
     *
     * @return true if verified | 如果已验证返回 true
     */
    public boolean isEmailVerified() {
        if (idTokenClaims == null) return false;
        return idTokenClaims.getClaim("email_verified")
                .map(v -> v instanceof Boolean b ? b : Boolean.parseBoolean(v.toString()))
                .orElse(false);
    }

    /**
     * Get the name claim
     * 获取名称声明
     *
     * @return the name | 名称
     */
    public String name() {
        if (idTokenClaims == null) return null;
        return idTokenClaims.getClaimAsString("name").orElse(null);
    }

    /**
     * Get the picture URL claim
     * 获取头像 URL 声明
     *
     * @return the picture URL | 头像 URL
     */
    public String picture() {
        if (idTokenClaims == null) return null;
        return idTokenClaims.getClaimAsString("picture").orElse(null);
    }

    /**
     * Check if the ID token is currently valid
     * 检查 ID Token 当前是否有效
     *
     * @return true if valid | 如果有效返回 true
     */
    public boolean isValid() {
        return idTokenClaims != null && idTokenClaims.isValid();
    }

    /**
     * Check if the ID token is expired
     * 检查 ID Token 是否已过期
     *
     * @return true if expired | 如果已过期返回 true
     */
    public boolean isIdTokenExpired() {
        return idTokenClaims != null && idTokenClaims.isExpired();
    }

    /**
     * Get the scopes
     * 获取范围
     *
     * @return the scopes | 范围
     */
    public Set<String> scopes() {
        return oauth2Token.scopes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OidcToken that)) return false;
        return Objects.equals(oauth2Token, that.oauth2Token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oauth2Token);
    }

    @Override
    public String toString() {
        return "OidcToken{" +
                "subject='" + subject() + '\'' +
                ", email='" + email() + '\'' +
                ", hasRefreshToken=" + hasRefreshToken() +
                ", isExpired=" + isExpired() +
                '}';
    }
}
