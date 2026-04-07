package cloud.opencode.base.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * OAuth2 Token Record
 * OAuth2 Token 记录
 *
 * <p>Immutable record representing an OAuth2 access token response.</p>
 * <p>表示 OAuth2 访问令牌响应的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access token storage - 访问令牌存储</li>
 *   <li>Refresh token support - 刷新令牌支持</li>
 *   <li>ID token for OIDC - OIDC 的 ID 令牌</li>
 *   <li>Expiration tracking - 过期时间跟踪</li>
 *   <li>Scope management - 权限范围管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create token with builder
 * OAuth2Token token = OAuth2Token.builder()
 *     .accessToken("eyJhbGciOiJSUzI1NiIs...")
 *     .refreshToken("dGhpcyBpcyBhIHJlZnJlc2g...")
 *     .expiresIn(3600)
 *     .build();
 *
 * // Check expiration
 * if (token.isExpired()) {
 *     // refresh token
 * }
 *
 * // Use in HTTP request
 * request.setHeader("Authorization", token.toBearerHeader());
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
public record OAuth2Token(
        String accessToken,
        String tokenType,
        String refreshToken,
        String idToken,
        Set<String> scopes,
        Instant issuedAt,
        Instant expiresAt
) {

    /**
     * Compact constructor with validation
     * 带验证的紧凑构造器
     */
    public OAuth2Token {
        Objects.requireNonNull(accessToken, "accessToken cannot be null");
        tokenType = tokenType != null ? tokenType : "Bearer";
        scopes = scopes != null ? Set.copyOf(scopes) : Set.of();
        issuedAt = issuedAt != null ? issuedAt : Instant.now();
    }

    /**
     * Check if token is expired
     * 检查令牌是否已过期
     *
     * @return true if expired | 已过期返回 true
     */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if token is expiring soon
     * 检查令牌是否即将过期
     *
     * @param threshold time threshold | 时间阈值
     * @return true if expiring within threshold | 在阈值内即将过期返回 true
     */
    public boolean isExpiringSoon(Duration threshold) {
        if (expiresAt == null) {
            return false;
        }
        return Instant.now().plus(threshold).isAfter(expiresAt);
    }

    /**
     * Get remaining valid time
     * 获取剩余有效时间
     *
     * @return remaining duration, or ZERO if expired | 剩余时间，已过期则返回 ZERO
     */
    public Duration remainingTime() {
        if (expiresAt == null) {
            return Duration.ZERO;
        }
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Check if has refresh token
     * 检查是否有刷新令牌
     *
     * @return true if has refresh token | 有刷新令牌返回 true
     */
    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }

    /**
     * Check if has ID token (OIDC)
     * 检查是否有 ID 令牌（OIDC）
     *
     * @return true if has ID token | 有 ID 令牌返回 true
     */
    public boolean hasIdToken() {
        return idToken != null && !idToken.isBlank();
    }

    /**
     * Get Bearer authorization header value
     * 获取 Bearer 授权头值
     *
     * @return the authorization header value | 授权头值
     */
    public String toBearerHeader() {
        return "Bearer " + accessToken;
    }

    /**
     * Get authorization header value with token type
     * 获取带令牌类型的授权头值
     *
     * @return the authorization header value | 授权头值
     */
    public String toAuthorizationHeader() {
        return tokenType + " " + accessToken;
    }

    /**
     * Returns a string representation with sensitive fields redacted.
     * 返回敏感字段已脱敏的字符串表示。
     */
    @Override
    public String toString() {
        return "OAuth2Token[tokenType=" + tokenType
                + ", hasRefreshToken=" + hasRefreshToken()
                + ", hasIdToken=" + hasIdToken()
                + ", isExpired=" + isExpired()
                + ", scopes=" + scopes + "]";
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
     * Create a builder from existing token
     * 从现有令牌创建构建器
     *
     * @param token the token to copy | 要复制的令牌
     * @return the builder | 构建器
     */
    public static Builder builder(OAuth2Token token) {
        return new Builder()
                .accessToken(token.accessToken)
                .tokenType(token.tokenType)
                .refreshToken(token.refreshToken)
                .idToken(token.idToken)
                .scopes(token.scopes)
                .issuedAt(token.issuedAt)
                .expiresAt(token.expiresAt);
    }

    /**
     * OAuth2Token Builder
     * OAuth2Token 构建器
     */
    public static class Builder {
        private String accessToken;
        private String tokenType = "Bearer";
        private String refreshToken;
        private String idToken;
        private Set<String> scopes = new HashSet<>();
        private Instant issuedAt = Instant.now();
        private Instant expiresAt;

        /**
         * Set the access token
         * 设置访问令牌
         *
         * @param accessToken the access token | 访问令牌
         * @return this builder | 此构建器
         */
        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        /**
         * Set the token type
         * 设置令牌类型
         *
         * @param tokenType the token type (default: Bearer) | 令牌类型（默认：Bearer）
         * @return this builder | 此构建器
         */
        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        /**
         * Set the refresh token
         * 设置刷新令牌
         *
         * @param refreshToken the refresh token | 刷新令牌
         * @return this builder | 此构建器
         */
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        /**
         * Set the ID token (OIDC)
         * 设置 ID 令牌（OIDC）
         *
         * @param idToken the ID token | ID 令牌
         * @return this builder | 此构建器
         */
        public Builder idToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        /**
         * Set the scopes
         * 设置权限范围
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
         * Set scopes from space-separated string
         * 从空格分隔的字符串设置权限范围
         *
         * @param scopeString space-separated scopes | 空格分隔的权限范围
         * @return this builder | 此构建器
         */
        public Builder scopeString(String scopeString) {
            if (scopeString != null && !scopeString.isBlank()) {
                for (String scope : scopeString.split("\\s+")) {
                    this.scopes.add(scope);
                }
            }
            return this;
        }

        /**
         * Set expires in seconds from now
         * 设置从现在起的过期秒数
         *
         * @param seconds seconds until expiration | 到期秒数
         * @return this builder | 此构建器
         */
        public Builder expiresIn(long seconds) {
            if (seconds < 0) {
                throw new IllegalArgumentException("expires_in must not be negative: " + seconds);
            }
            this.expiresAt = Instant.now().plusSeconds(seconds);
            return this;
        }

        /**
         * Set the expiration time
         * 设置过期时间
         *
         * @param expiresAt the expiration time | 过期时间
         * @return this builder | 此构建器
         */
        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * Set the issued at time
         * 设置签发时间
         *
         * @param issuedAt the issued at time | 签发时间
         * @return this builder | 此构建器
         */
        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        /**
         * Build the OAuth2Token
         * 构建 OAuth2Token
         *
         * @return the token | 令牌
         * @throws NullPointerException if accessToken is null | 如果 accessToken 为 null
         */
        public OAuth2Token build() {
            Objects.requireNonNull(accessToken, "accessToken is required");
            return new OAuth2Token(
                    accessToken,
                    tokenType,
                    refreshToken,
                    idToken,
                    scopes,
                    issuedAt,
                    expiresAt
            );
        }
    }
}
