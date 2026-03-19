package cloud.opencode.base.oauth2.oidc;

import java.time.Duration;
import java.util.*;

/**
 * OpenID Connect Configuration
 * OpenID Connect 配置
 *
 * <p>Configuration for OIDC-specific features.</p>
 * <p>OIDC 特定功能的配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>OIDC discovery support - OIDC 发现支持</li>
 *   <li>ID token validation settings - ID Token 验证设置</li>
 *   <li>Nonce support - Nonce 支持</li>
 *   <li>Claims configuration - 声明配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create OIDC config
 * OidcConfig config = OidcConfig.builder()
 *     .issuer("https://accounts.google.com")
 *     .jwksUri("https://www.googleapis.com/oauth2/v3/certs")
 *     .validateIdToken(true)
 *     .clockSkew(Duration.ofMinutes(5))
 *     .build();
 *
 * // Use with OidcClient
 * OidcClient client = OidcClient.builder()
 *     .oauth2Client(oauth2Client)
 *     .oidcConfig(config)
 *     .build();
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This record is immutable and thread-safe.</p>
 * <p>此记录是不可变的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public record OidcConfig(
        String issuer,
        String jwksUri,
        String userInfoEndpoint,
        boolean validateIdToken,
        boolean validateNonce,
        boolean validateAudience,
        boolean validateExpiration,
        Duration clockSkew,
        Set<String> requiredClaims,
        Set<String> requestedClaims
) {

    /**
     * Compact constructor
     * 紧凑构造器
     */
    public OidcConfig {
        clockSkew = clockSkew != null ? clockSkew : Duration.ofMinutes(5);
        requiredClaims = requiredClaims != null ? Set.copyOf(requiredClaims) : Set.of();
        requestedClaims = requestedClaims != null ? Set.copyOf(requestedClaims) : Set.of();
    }

    /**
     * Check if signature validation is possible
     * 检查是否可以进行签名验证
     *
     * @return true if JWKS URI is configured | 如果配置了 JWKS URI 返回 true
     */
    public boolean canValidateSignature() {
        return jwksUri != null && !jwksUri.isBlank();
    }

    /**
     * Check if issuer validation is possible
     * 检查是否可以进行发行者验证
     *
     * @return true if issuer is configured | 如果配置了发行者返回 true
     */
    public boolean canValidateIssuer() {
        return issuer != null && !issuer.isBlank();
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
     * Create default OIDC config
     * 创建默认 OIDC 配置
     *
     * @return the default config | 默认配置
     */
    public static OidcConfig defaults() {
        return builder().build();
    }

    /**
     * Create strict OIDC config with all validations enabled
     * 创建启用所有验证的严格 OIDC 配置
     *
     * @param issuer  the expected issuer | 预期的发行者
     * @param jwksUri the JWKS URI | JWKS URI
     * @return the strict config | 严格配置
     */
    public static OidcConfig strict(String issuer, String jwksUri) {
        return builder()
                .issuer(issuer)
                .jwksUri(jwksUri)
                .validateIdToken(true)
                .validateNonce(true)
                .validateAudience(true)
                .validateExpiration(true)
                .clockSkew(Duration.ofMinutes(2))
                .build();
    }

    /**
     * OidcConfig Builder
     * OidcConfig 构建器
     */
    public static class Builder {
        private String issuer;
        private String jwksUri;
        private String userInfoEndpoint;
        private boolean validateIdToken = true;
        private boolean validateNonce = false;
        private boolean validateAudience = true;
        private boolean validateExpiration = true;
        private Duration clockSkew = Duration.ofMinutes(5);
        private Set<String> requiredClaims = new LinkedHashSet<>();
        private Set<String> requestedClaims = new LinkedHashSet<>();

        /**
         * Set the expected issuer
         * 设置预期的发行者
         *
         * @param issuer the issuer | 发行者
         * @return this builder | 此构建器
         */
        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Set the JWKS URI for signature validation
         * 设置用于签名验证的 JWKS URI
         *
         * @param jwksUri the JWKS URI | JWKS URI
         * @return this builder | 此构建器
         */
        public Builder jwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
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
         * Enable or disable ID token validation
         * 启用或禁用 ID Token 验证
         *
         * @param validateIdToken whether to validate ID token | 是否验证 ID Token
         * @return this builder | 此构建器
         */
        public Builder validateIdToken(boolean validateIdToken) {
            this.validateIdToken = validateIdToken;
            return this;
        }

        /**
         * Enable or disable nonce validation
         * 启用或禁用 nonce 验证
         *
         * @param validateNonce whether to validate nonce | 是否验证 nonce
         * @return this builder | 此构建器
         */
        public Builder validateNonce(boolean validateNonce) {
            this.validateNonce = validateNonce;
            return this;
        }

        /**
         * Enable or disable audience validation
         * 启用或禁用受众验证
         *
         * @param validateAudience whether to validate audience | 是否验证受众
         * @return this builder | 此构建器
         */
        public Builder validateAudience(boolean validateAudience) {
            this.validateAudience = validateAudience;
            return this;
        }

        /**
         * Enable or disable expiration validation
         * 启用或禁用过期验证
         *
         * @param validateExpiration whether to validate expiration | 是否验证过期
         * @return this builder | 此构建器
         */
        public Builder validateExpiration(boolean validateExpiration) {
            this.validateExpiration = validateExpiration;
            return this;
        }

        /**
         * Set the clock skew tolerance
         * 设置时钟偏差容忍度
         *
         * @param clockSkew the clock skew | 时钟偏差
         * @return this builder | 此构建器
         */
        public Builder clockSkew(Duration clockSkew) {
            this.clockSkew = clockSkew;
            return this;
        }

        /**
         * Set required claims
         * 设置必需的声明
         *
         * @param claims the required claims | 必需的声明
         * @return this builder | 此构建器
         */
        public Builder requiredClaims(String... claims) {
            this.requiredClaims = new LinkedHashSet<>(Arrays.asList(claims));
            return this;
        }

        /**
         * Set requested claims
         * 设置请求的声明
         *
         * @param claims the requested claims | 请求的声明
         * @return this builder | 此构建器
         */
        public Builder requestedClaims(String... claims) {
            this.requestedClaims = new LinkedHashSet<>(Arrays.asList(claims));
            return this;
        }

        /**
         * Build the OidcConfig
         * 构建 OidcConfig
         *
         * @return the config | 配置
         */
        public OidcConfig build() {
            return new OidcConfig(
                    issuer, jwksUri, userInfoEndpoint,
                    validateIdToken, validateNonce, validateAudience, validateExpiration,
                    clockSkew, requiredClaims, requestedClaims
            );
        }
    }
}
