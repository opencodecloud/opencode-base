package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.grant.GrantType;

import java.util.*;

/**
 * Custom OAuth2 Provider
 * 自定义 OAuth2 Provider
 *
 * <p>A configurable OAuth2 provider for custom authorization servers.</p>
 * <p>用于自定义授权服务器的可配置 OAuth2 提供者。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fully configurable endpoints - 完全可配置的端点</li>
 *   <li>Custom default scopes - 自定义默认范围</li>
 *   <li>Immutable after creation - 创建后不可变</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a custom provider
 * OAuth2Provider provider = CustomProvider.builder()
 *     .name("MyProvider")
 *     .authorizationEndpoint("https://auth.example.com/authorize")
 *     .tokenEndpoint("https://auth.example.com/token")
 *     .userInfoEndpoint("https://auth.example.com/userinfo")
 *     .defaultScopes("openid", "profile", "email")
 *     .build();
 *
 * // Use the provider
 * OAuth2Client client = OAuth2Client.builder()
 *     .provider(provider)
 *     .clientId("my-client-id")
 *     .clientSecret("my-client-secret")
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
public final class CustomProvider implements OAuth2Provider {

    private final String name;
    private final String authorizationEndpoint;
    private final String tokenEndpoint;
    private final String userInfoEndpoint;
    private final String revocationEndpoint;
    private final String deviceAuthorizationEndpoint;
    private final String jwksUri;
    private final String issuer;
    private final Set<String> defaultScopes;

    private CustomProvider(Builder builder) {
        this.name = builder.name;
        this.authorizationEndpoint = builder.authorizationEndpoint;
        this.tokenEndpoint = builder.tokenEndpoint;
        this.userInfoEndpoint = builder.userInfoEndpoint;
        this.revocationEndpoint = builder.revocationEndpoint;
        this.deviceAuthorizationEndpoint = builder.deviceAuthorizationEndpoint;
        this.jwksUri = builder.jwksUri;
        this.issuer = builder.issuer;
        this.defaultScopes = Set.copyOf(builder.defaultScopes);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String authorizationEndpoint() {
        return authorizationEndpoint;
    }

    @Override
    public String tokenEndpoint() {
        return tokenEndpoint;
    }

    @Override
    public String userInfoEndpoint() {
        return userInfoEndpoint;
    }

    @Override
    public String revocationEndpoint() {
        return revocationEndpoint;
    }

    @Override
    public String deviceAuthorizationEndpoint() {
        return deviceAuthorizationEndpoint;
    }

    @Override
    public Set<String> defaultScopes() {
        return defaultScopes;
    }

    /**
     * Get the JWKS URI
     * 获取 JWKS URI
     *
     * @return the JWKS URI | JWKS URI
     */
    public String jwksUri() {
        return jwksUri;
    }

    /**
     * Get the issuer
     * 获取发行者
     *
     * @return the issuer | 发行者
     */
    public String issuer() {
        return issuer;
    }

    @Override
    public OAuth2Config toConfig(String clientId, String clientSecret, String redirectUri,
                                  Set<String> additionalScopes, GrantType grantType) {
        Set<String> scopes = new LinkedHashSet<>(defaultScopes);
        if (additionalScopes != null) {
            scopes.addAll(additionalScopes);
        }

        return OAuth2Config.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationEndpoint(authorizationEndpoint)
                .tokenEndpoint(tokenEndpoint)
                .userInfoEndpoint(userInfoEndpoint)
                .revocationEndpoint(revocationEndpoint)
                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint)
                .redirectUri(redirectUri)
                .scopes(scopes)
                .grantType(grantType != null ? grantType : GrantType.AUTHORIZATION_CODE)
                .build();
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
     * Create a builder from an existing provider
     * 从现有 Provider 创建构建器
     *
     * @param provider the provider to copy | 要复制的 Provider
     * @return the builder | 构建器
     */
    public static Builder from(OAuth2Provider provider) {
        return new Builder()
                .name(provider.name())
                .authorizationEndpoint(provider.authorizationEndpoint())
                .tokenEndpoint(provider.tokenEndpoint())
                .userInfoEndpoint(provider.userInfoEndpoint())
                .revocationEndpoint(provider.revocationEndpoint())
                .deviceAuthorizationEndpoint(provider.deviceAuthorizationEndpoint())
                .defaultScopes(provider.defaultScopes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomProvider that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(authorizationEndpoint, that.authorizationEndpoint) &&
                Objects.equals(tokenEndpoint, that.tokenEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, authorizationEndpoint, tokenEndpoint);
    }

    @Override
    public String toString() {
        return "CustomProvider{" +
                "name='" + name + '\'' +
                ", authorizationEndpoint='" + authorizationEndpoint + '\'' +
                ", tokenEndpoint='" + tokenEndpoint + '\'' +
                '}';
    }

    /**
     * CustomProvider Builder
     * CustomProvider 构建器
     */
    public static class Builder {
        private String name;
        private String authorizationEndpoint;
        private String tokenEndpoint;
        private String userInfoEndpoint;
        private String revocationEndpoint;
        private String deviceAuthorizationEndpoint;
        private String jwksUri;
        private String issuer;
        private Set<String> defaultScopes = new LinkedHashSet<>();

        /**
         * Set the provider name
         * 设置 Provider 名称
         *
         * @param name the name | 名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
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
         * 设置 Token 端点
         *
         * @param tokenEndpoint the token endpoint | Token 端点
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
         * Set the JWKS URI
         * 设置 JWKS URI
         *
         * @param jwksUri the JWKS URI | JWKS URI
         * @return this builder | 此构建器
         */
        public Builder jwksUri(String jwksUri) {
            this.jwksUri = jwksUri;
            return this;
        }

        /**
         * Set the issuer
         * 设置发行者
         *
         * @param issuer the issuer | 发行者
         * @return this builder | 此构建器
         */
        public Builder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        /**
         * Set the default scopes
         * 设置默认范围
         *
         * @param scopes the default scopes | 默认范围
         * @return this builder | 此构建器
         */
        public Builder defaultScopes(String... scopes) {
            this.defaultScopes = new LinkedHashSet<>(Arrays.asList(scopes));
            return this;
        }

        /**
         * Set the default scopes
         * 设置默认范围
         *
         * @param scopes the default scopes | 默认范围
         * @return this builder | 此构建器
         */
        public Builder defaultScopes(Set<String> scopes) {
            this.defaultScopes = scopes != null ? new LinkedHashSet<>(scopes) : new LinkedHashSet<>();
            return this;
        }

        /**
         * Add a default scope
         * 添加默认范围
         *
         * @param scope the scope to add | 要添加的范围
         * @return this builder | 此构建器
         */
        public Builder addScope(String scope) {
            this.defaultScopes.add(scope);
            return this;
        }

        /**
         * Build the CustomProvider
         * 构建 CustomProvider
         *
         * @return the provider | Provider
         */
        public CustomProvider build() {
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(tokenEndpoint, "tokenEndpoint is required");
            return new CustomProvider(this);
        }
    }
}
