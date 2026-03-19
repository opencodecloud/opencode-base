package cloud.opencode.base.oauth2.provider;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.grant.GrantType;

import java.util.HashSet;
import java.util.Set;

/**
 * OAuth2 Provider Interface
 * OAuth2 Provider 接口
 *
 * <p>Defines the contract for OAuth2 identity providers.</p>
 * <p>定义 OAuth2 身份提供者的契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Endpoint configuration - 端点配置</li>
 *   <li>Default scopes - 默认权限范围</li>
 *   <li>Config builder integration - 配置构建器集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use built-in provider
 * OAuth2Client client = OAuth2Client.builder()
 *     .provider(Providers.GOOGLE)
 *     .clientId("your-client-id")
 *     .clientSecret("your-client-secret")
 *     .build();
 *
 * // Create custom provider
 * OAuth2Provider myProvider = new OAuth2Provider() {
 *     @Override
 *     public String name() { return "MyProvider"; }
 *     @Override
 *     public String authorizationEndpoint() { return "https://..."; }
 *     // ... other methods
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public interface OAuth2Provider {

    /**
     * Get the provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String name();

    /**
     * Get the authorization endpoint URL
     * 获取授权端点 URL
     *
     * @return the authorization endpoint | 授权端点
     */
    String authorizationEndpoint();

    /**
     * Get the token endpoint URL
     * 获取令牌端点 URL
     *
     * @return the token endpoint | 令牌端点
     */
    String tokenEndpoint();

    /**
     * Get the user info endpoint URL
     * 获取用户信息端点 URL
     *
     * @return the user info endpoint, or null if not supported | 用户信息端点，不支持则返回 null
     */
    default String userInfoEndpoint() {
        return null;
    }

    /**
     * Get the revocation endpoint URL
     * 获取撤销端点 URL
     *
     * @return the revocation endpoint, or null if not supported | 撤销端点，不支持则返回 null
     */
    default String revocationEndpoint() {
        return null;
    }

    /**
     * Get the device authorization endpoint URL
     * 获取设备授权端点 URL
     *
     * @return the device authorization endpoint, or null if not supported | 设备授权端点，不支持则返回 null
     */
    default String deviceAuthorizationEndpoint() {
        return null;
    }

    /**
     * Get the default scopes for this provider
     * 获取此提供者的默认权限范围
     *
     * @return the default scopes | 默认权限范围
     */
    default Set<String> defaultScopes() {
        return Set.of();
    }

    /**
     * Check if PKCE is required for this provider
     * 检查此提供者是否需要 PKCE
     *
     * @return true if PKCE is required | 需要 PKCE 返回 true
     */
    default boolean requiresPkce() {
        return false;
    }

    /**
     * Check if this provider supports device code flow
     * 检查此提供者是否支持设备码流程
     *
     * @return true if device code is supported | 支持设备码返回 true
     */
    default boolean supportsDeviceCode() {
        return deviceAuthorizationEndpoint() != null;
    }

    /**
     * Convert this provider to an OAuth2Config
     * 将此提供者转换为 OAuth2Config
     *
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @param redirectUri  the redirect URI | 重定向 URI
     * @return the OAuth2Config | OAuth2 配置
     */
    default OAuth2Config toConfig(String clientId, String clientSecret, String redirectUri) {
        return toConfig(clientId, clientSecret, redirectUri, Set.of(), GrantType.AUTHORIZATION_CODE);
    }

    /**
     * Convert this provider to an OAuth2Config with additional scopes
     * 将此提供者转换为带有额外权限范围的 OAuth2Config
     *
     * @param clientId         the client ID | 客户端 ID
     * @param clientSecret     the client secret | 客户端密钥
     * @param redirectUri      the redirect URI | 重定向 URI
     * @param additionalScopes additional scopes | 额外权限范围
     * @return the OAuth2Config | OAuth2 配置
     */
    default OAuth2Config toConfig(String clientId, String clientSecret, String redirectUri,
                                   Set<String> additionalScopes) {
        return toConfig(clientId, clientSecret, redirectUri, additionalScopes, GrantType.AUTHORIZATION_CODE);
    }

    /**
     * Convert this provider to an OAuth2Config with custom settings
     * 将此提供者转换为带有自定义设置的 OAuth2Config
     *
     * @param clientId         the client ID | 客户端 ID
     * @param clientSecret     the client secret | 客户端密钥
     * @param redirectUri      the redirect URI | 重定向 URI
     * @param additionalScopes additional scopes | 额外权限范围
     * @param grantType        the grant type | 授权类型
     * @return the OAuth2Config | OAuth2 配置
     */
    default OAuth2Config toConfig(String clientId, String clientSecret, String redirectUri,
                                   Set<String> additionalScopes, GrantType grantType) {
        Set<String> scopes = new HashSet<>(defaultScopes());
        if (additionalScopes != null) {
            scopes.addAll(additionalScopes);
        }

        return OAuth2Config.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationEndpoint(authorizationEndpoint())
                .tokenEndpoint(tokenEndpoint())
                .userInfoEndpoint(userInfoEndpoint())
                .revocationEndpoint(revocationEndpoint())
                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint())
                .redirectUri(redirectUri)
                .scopes(scopes)
                .grantType(grantType)
                .usePkce(requiresPkce())
                .build();
    }
}
