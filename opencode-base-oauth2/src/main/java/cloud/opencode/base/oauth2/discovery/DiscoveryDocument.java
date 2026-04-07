package cloud.opencode.base.oauth2.discovery;

import cloud.opencode.base.oauth2.OAuth2Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OIDC Discovery Document
 * OIDC 发现文档
 *
 * <p>Immutable record representing an OpenID Connect Discovery 1.0 configuration document
 * as defined in the OIDC Discovery specification.</p>
 * <p>表示 OpenID Connect Discovery 1.0 配置文档的不可变记录，
 * 按照 OIDC 发现规范定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All standard OIDC discovery fields - 所有标准 OIDC 发现字段</li>
 *   <li>Builder pattern for construction - 构建器模式用于构建</li>
 *   <li>Conversion to OAuth2Config - 转换为 OAuth2Config</li>
 *   <li>Capability query methods - 能力查询方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Build a discovery document
 * DiscoveryDocument doc = DiscoveryDocument.builder()
 *     .issuer("https://accounts.example.com")
 *     .authorizationEndpoint("https://accounts.example.com/authorize")
 *     .tokenEndpoint("https://accounts.example.com/token")
 *     .build();
 *
 * // Check capabilities
 * boolean supportsPkce = doc.supportsPkce();
 * boolean supportsRefresh = doc.supports("refresh_token");
 *
 * // Convert to OAuth2Config
 * OAuth2Config config = doc.toConfig("client-id", "client-secret");
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is immutable and thread-safe. All list fields are unmodifiable.</p>
 * <p>此类是不可变的，线程安全。所有列表字段都是不可修改的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OIDC Discovery 1.0</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public record DiscoveryDocument(
        String issuer,
        String authorizationEndpoint,
        String tokenEndpoint,
        String userinfoEndpoint,
        String jwksUri,
        String registrationEndpoint,
        String revocationEndpoint,
        String introspectionEndpoint,
        String deviceAuthorizationEndpoint,
        String parEndpoint,
        List<String> scopesSupported,
        List<String> responseTypesSupported,
        List<String> grantTypesSupported,
        List<String> tokenEndpointAuthMethodsSupported,
        List<String> codeChallengeMethodsSupported
) {

    /**
     * Compact constructor with defensive copying and validation.
     * 带防御性复制和验证的紧凑构造器。
     */
    public DiscoveryDocument {
        Objects.requireNonNull(issuer, "issuer cannot be null");
        scopesSupported = scopesSupported != null ? List.copyOf(scopesSupported) : List.of();
        responseTypesSupported = responseTypesSupported != null ? List.copyOf(responseTypesSupported) : List.of();
        grantTypesSupported = grantTypesSupported != null ? List.copyOf(grantTypesSupported) : List.of();
        tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported != null
                ? List.copyOf(tokenEndpointAuthMethodsSupported) : List.of();
        codeChallengeMethodsSupported = codeChallengeMethodsSupported != null
                ? List.copyOf(codeChallengeMethodsSupported) : List.of();
    }

    /**
     * Convert this discovery document to an OAuth2Config using the given client credentials.
     * 使用给定的客户端凭证将此发现文档转换为 OAuth2Config。
     *
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret (may be null for public clients) | 客户端密钥（公共客户端可为 null）
     * @return the OAuth2Config built from this discovery document | 从此发现文档构建的 OAuth2Config
     * @throws NullPointerException if clientId is null | 如果 clientId 为 null
     */
    public OAuth2Config toConfig(String clientId, String clientSecret) {
        Objects.requireNonNull(clientId, "clientId cannot be null");
        OAuth2Config.Builder builder = OAuth2Config.builder()
                .clientId(clientId)
                .authorizationEndpoint(authorizationEndpoint)
                .tokenEndpoint(tokenEndpoint)
                .userInfoEndpoint(userinfoEndpoint)
                .revocationEndpoint(revocationEndpoint)
                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint)
                .usePkce(supportsPkce());

        if (clientSecret != null) {
            builder.clientSecret(clientSecret);
        }

        return builder.build();
    }

    /**
     * Check if the authorization server supports the given grant type.
     * 检查授权服务器是否支持给定的授权类型。
     *
     * @param grantType the grant type to check (e.g., "authorization_code", "refresh_token") |
     *                  要检查的授权类型（例如 "authorization_code"、"refresh_token"）
     * @return true if the grant type is supported | 如果支持该授权类型返回 true
     */
    public boolean supports(String grantType) {
        if (grantType == null) {
            return false;
        }
        return grantTypesSupported.contains(grantType);
    }

    /**
     * Check if the authorization server supports the given scope.
     * 检查授权服务器是否支持给定的权限范围。
     *
     * @param scope the scope to check (e.g., "openid", "email") |
     *              要检查的权限范围（例如 "openid"、"email"）
     * @return true if the scope is supported | 如果支持该权限范围返回 true
     */
    public boolean supportsScope(String scope) {
        if (scope == null) {
            return false;
        }
        return scopesSupported.contains(scope);
    }

    /**
     * Check if the authorization server supports PKCE (S256 code challenge method).
     * 检查授权服务器是否支持 PKCE（S256 代码挑战方法）。
     *
     * @return true if PKCE with S256 is supported | 如果支持 S256 的 PKCE 返回 true
     */
    public boolean supportsPkce() {
        return codeChallengeMethodsSupported.contains("S256");
    }

    /**
     * Create a new builder.
     * 创建新的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * DiscoveryDocument Builder
     * DiscoveryDocument 构建器
     *
     * <p>Builder for constructing immutable {@link DiscoveryDocument} instances.</p>
     * <p>用于构建不可变 {@link DiscoveryDocument} 实例的构建器。</p>
     */
    public static final class Builder {
        private String issuer;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String userinfoEndpoint;
        private String jwksUri;
        private String registrationEndpoint;
        private String revocationEndpoint;
        private String introspectionEndpoint;
        private String deviceAuthorizationEndpoint;
        private String parEndpoint;
        private List<String> scopesSupported = new ArrayList<>();
        private List<String> responseTypesSupported = new ArrayList<>();
        private List<String> grantTypesSupported = new ArrayList<>();
        private List<String> tokenEndpointAuthMethodsSupported = new ArrayList<>();
        private List<String> codeChallengeMethodsSupported = new ArrayList<>();

        Builder() {
        }

        /**
         * Set the issuer identifier.
         * 设置颁发者标识符。
         *
         * @param issuer the issuer URL | 颁发者 URL
         * @return this builder | 此构建器
         */
        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Set the authorization endpoint.
         * 设置授权端点。
         *
         * @param authorizationEndpoint the authorization endpoint URL | 授权端点 URL
         * @return this builder | 此构建器
         */
        public Builder authorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
            return this;
        }

        /**
         * Set the token endpoint.
         * 设置令牌端点。
         *
         * @param tokenEndpoint the token endpoint URL | 令牌端点 URL
         * @return this builder | 此构建器
         */
        public Builder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        /**
         * Set the userinfo endpoint.
         * 设置用户信息端点。
         *
         * @param userinfoEndpoint the userinfo endpoint URL | 用户信息端点 URL
         * @return this builder | 此构建器
         */
        public Builder userinfoEndpoint(String userinfoEndpoint) {
            this.userinfoEndpoint = userinfoEndpoint;
            return this;
        }

        /**
         * Set the JWKS URI.
         * 设置 JWKS URI。
         *
         * @param jwksUri the JWKS URI | JWKS URI
         * @return this builder | 此构建器
         */
        public Builder jwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
            return this;
        }

        /**
         * Set the registration endpoint.
         * 设置注册端点。
         *
         * @param registrationEndpoint the registration endpoint URL | 注册端点 URL
         * @return this builder | 此构建器
         */
        public Builder registrationEndpoint(String registrationEndpoint) {
            this.registrationEndpoint = registrationEndpoint;
            return this;
        }

        /**
         * Set the revocation endpoint.
         * 设置撤销端点。
         *
         * @param revocationEndpoint the revocation endpoint URL | 撤销端点 URL
         * @return this builder | 此构建器
         */
        public Builder revocationEndpoint(String revocationEndpoint) {
            this.revocationEndpoint = revocationEndpoint;
            return this;
        }

        /**
         * Set the introspection endpoint.
         * 设置内省端点。
         *
         * @param introspectionEndpoint the introspection endpoint URL | 内省端点 URL
         * @return this builder | 此构建器
         */
        public Builder introspectionEndpoint(String introspectionEndpoint) {
            this.introspectionEndpoint = introspectionEndpoint;
            return this;
        }

        /**
         * Set the device authorization endpoint.
         * 设置设备授权端点。
         *
         * @param deviceAuthorizationEndpoint the device authorization endpoint URL | 设备授权端点 URL
         * @return this builder | 此构建器
         */
        public Builder deviceAuthorizationEndpoint(String deviceAuthorizationEndpoint) {
            this.deviceAuthorizationEndpoint = deviceAuthorizationEndpoint;
            return this;
        }

        /**
         * Set the pushed authorization request endpoint.
         * 设置推送授权请求端点。
         *
         * @param parEndpoint the PAR endpoint URL | PAR 端点 URL
         * @return this builder | 此构建器
         */
        public Builder parEndpoint(String parEndpoint) {
            this.parEndpoint = parEndpoint;
            return this;
        }

        /**
         * Set the supported scopes.
         * 设置支持的权限范围。
         *
         * @param scopesSupported the list of supported scopes | 支持的权限范围列表
         * @return this builder | 此构建器
         */
        public Builder scopesSupported(List<String> scopesSupported) {
            this.scopesSupported = scopesSupported != null ? new ArrayList<>(scopesSupported) : new ArrayList<>();
            return this;
        }

        /**
         * Set the supported response types.
         * 设置支持的响应类型。
         *
         * @param responseTypesSupported the list of supported response types | 支持的响应类型列表
         * @return this builder | 此构建器
         */
        public Builder responseTypesSupported(List<String> responseTypesSupported) {
            this.responseTypesSupported = responseTypesSupported != null
                    ? new ArrayList<>(responseTypesSupported) : new ArrayList<>();
            return this;
        }

        /**
         * Set the supported grant types.
         * 设置支持的授权类型。
         *
         * @param grantTypesSupported the list of supported grant types | 支持的授权类型列表
         * @return this builder | 此构建器
         */
        public Builder grantTypesSupported(List<String> grantTypesSupported) {
            this.grantTypesSupported = grantTypesSupported != null
                    ? new ArrayList<>(grantTypesSupported) : new ArrayList<>();
            return this;
        }

        /**
         * Set the supported token endpoint authentication methods.
         * 设置支持的令牌端点认证方法。
         *
         * @param tokenEndpointAuthMethodsSupported the list of supported auth methods |
         *                                          支持的认证方法列表
         * @return this builder | 此构建器
         */
        public Builder tokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
            this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported != null
                    ? new ArrayList<>(tokenEndpointAuthMethodsSupported) : new ArrayList<>();
            return this;
        }

        /**
         * Set the supported code challenge methods.
         * 设置支持的代码挑战方法。
         *
         * @param codeChallengeMethodsSupported the list of supported code challenge methods |
         *                                      支持的代码挑战方法列表
         * @return this builder | 此构建器
         */
        public Builder codeChallengeMethodsSupported(List<String> codeChallengeMethodsSupported) {
            this.codeChallengeMethodsSupported = codeChallengeMethodsSupported != null
                    ? new ArrayList<>(codeChallengeMethodsSupported) : new ArrayList<>();
            return this;
        }

        /**
         * Build the DiscoveryDocument.
         * 构建 DiscoveryDocument。
         *
         * @return the discovery document | 发现文档
         * @throws NullPointerException if issuer is null | 如果 issuer 为 null
         */
        public DiscoveryDocument build() {
            return new DiscoveryDocument(
                    issuer,
                    authorizationEndpoint,
                    tokenEndpoint,
                    userinfoEndpoint,
                    jwksUri,
                    registrationEndpoint,
                    revocationEndpoint,
                    introspectionEndpoint,
                    deviceAuthorizationEndpoint,
                    parEndpoint,
                    scopesSupported,
                    responseTypesSupported,
                    grantTypesSupported,
                    tokenEndpointAuthMethodsSupported,
                    codeChallengeMethodsSupported
            );
        }
    }
}
