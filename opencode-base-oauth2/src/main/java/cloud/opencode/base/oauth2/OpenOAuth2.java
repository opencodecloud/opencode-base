package cloud.opencode.base.oauth2;

import cloud.opencode.base.oauth2.discovery.DiscoveryDocument;
import cloud.opencode.base.oauth2.discovery.OidcDiscovery;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import cloud.opencode.base.oauth2.introspection.IntrospectionResult;
import cloud.opencode.base.oauth2.introspection.TokenIntrospection;
import cloud.opencode.base.oauth2.oidc.JwtClaims;
import cloud.opencode.base.oauth2.par.ParResponse;
import cloud.opencode.base.oauth2.par.PushedAuthorizationRequest;
import cloud.opencode.base.oauth2.pkce.PkceChallenge;
import cloud.opencode.base.oauth2.provider.OAuth2Provider;
import cloud.opencode.base.oauth2.provider.Providers;
import cloud.opencode.base.oauth2.security.StateParameter;
import cloud.opencode.base.oauth2.token.DefaultTokenManager;
import cloud.opencode.base.oauth2.token.FileTokenStore;
import cloud.opencode.base.oauth2.token.InMemoryTokenStore;
import cloud.opencode.base.oauth2.token.TokenManager;
import cloud.opencode.base.oauth2.token.TokenStore;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * OAuth2 Facade Class
 * OAuth2 门面类
 *
 * <p>Main entry point for OAuth2 operations. Provides convenient factory methods
 * for creating OAuth2 clients with pre-configured providers.</p>
 * <p>OAuth2 操作的主入口点。提供便捷的工厂方法来创建带有预配置提供者的 OAuth2 客户端。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-configured providers (Google, Microsoft, GitHub) - 预配置提供者</li>
 *   <li>PKCE challenge generation - PKCE 挑战生成</li>
 *   <li>JWT parsing - JWT 解析</li>
 *   <li>Token store factories - Token 存储工厂</li>
 *   <li>State parameter generation (CSRF protection) - State 参数生成（CSRF 防护）</li>
 *   <li>OIDC Discovery - OIDC 自动发现</li>
 *   <li>Token Introspection (RFC 7662) - Token 内省</li>
 *   <li>Pushed Authorization Requests (RFC 9126) - 推送授权请求</li>
 *   <li>Token lifecycle management - Token 生命周期管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick start with Google
 * OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
 *     .redirectUri("https://yourapp.com/callback")
 *     .scopes("https://mail.google.com/")
 *     .build();
 *
 * // Generate PKCE challenge
 * PkceChallenge pkce = OpenOAuth2.generatePkce();
 *
 * // Get authorization URL
 * String authUrl = client.getAuthorizationUrl("state", pkce);
 *
 * // Exchange code for token
 * OAuth2Token token = client.exchangeCode(code, pkce.verifier());
 *
 * // Parse JWT
 * JwtClaims claims = OpenOAuth2.parseJwt(token.idToken());
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is stateless and thread-safe.</p>
 * <p>此类是无状态的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public final class OpenOAuth2 {

    private OpenOAuth2() {
        // Utility class - prevent instantiation
    }

    // ==================== Provider Factory Methods ====================

    /**
     * Create a Google OAuth2 client builder
     * 创建 Google OAuth2 客户端构建器
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * OAuth2Client client = OpenOAuth2.google("client-id", "client-secret")
     *     .redirectUri("https://yourapp.com/callback")
     *     .scopes("https://mail.google.com/")
     *     .build();
     * }</pre>
     *
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder google(String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.GOOGLE)
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    /**
     * Create a Microsoft OAuth2 client builder
     * 创建 Microsoft OAuth2 客户端构建器
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * OAuth2Client client = OpenOAuth2.microsoft("client-id", "client-secret")
     *     .redirectUri("https://yourapp.com/callback")
     *     .build();
     * }</pre>
     *
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder microsoft(String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.MICROSOFT)
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    /**
     * Create a Microsoft OAuth2 client builder for a specific tenant
     * 为特定租户创建 Microsoft OAuth2 客户端构建器
     *
     * @param tenantId     the Azure AD tenant ID | Azure AD 租户 ID
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder microsoft(String tenantId, String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.microsoftTenant(tenantId))
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    /**
     * Create a GitHub OAuth2 client builder
     * 创建 GitHub OAuth2 客户端构建器
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * OAuth2Client client = OpenOAuth2.github("client-id", "client-secret")
     *     .redirectUri("https://yourapp.com/callback")
     *     .build();
     * }</pre>
     *
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder github(String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.GITHUB)
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    /**
     * Create an Apple OAuth2 client builder
     * 创建 Apple OAuth2 客户端构建器
     *
     * @param clientId     the client ID (Services ID) | 客户端 ID（服务 ID）
     * @param clientSecret the client secret (JWT) | 客户端密钥（JWT）
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder apple(String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.APPLE)
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    /**
     * Create a Facebook OAuth2 client builder
     * 创建 Facebook OAuth2 客户端构建器
     *
     * @param clientId     the client ID (App ID) | 客户端 ID（应用 ID）
     * @param clientSecret the client secret (App Secret) | 客户端密钥（应用密钥）
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder facebook(String clientId, String clientSecret) {
        return OAuth2Client.builder()
                .provider(Providers.FACEBOOK)
                .clientId(clientId)
                .clientSecret(clientSecret);
    }

    // ==================== Custom Provider ====================

    /**
     * Create a custom OAuth2 client builder
     * 创建自定义 OAuth2 客户端构建器
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * OAuth2Client client = OpenOAuth2.client()
     *     .clientId("client-id")
     *     .clientSecret("client-secret")
     *     .authorizationEndpoint("https://auth.example.com/authorize")
     *     .tokenEndpoint("https://auth.example.com/token")
     *     .redirectUri("https://yourapp.com/callback")
     *     .build();
     * }</pre>
     *
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder client() {
        return OAuth2Client.builder();
    }

    /**
     * Create an OAuth2 client builder with a custom provider
     * 使用自定义提供者创建 OAuth2 客户端构建器
     *
     * @param provider the OAuth2 provider | OAuth2 提供者
     * @return the client builder | 客户端构建器
     */
    public static OAuth2Client.Builder client(OAuth2Provider provider) {
        return OAuth2Client.builder().provider(provider);
    }

    /**
     * Create an OAuth2 client from configuration
     * 从配置创建 OAuth2 客户端
     *
     * @param config the OAuth2 configuration | OAuth2 配置
     * @return the client | 客户端
     */
    public static OAuth2Client fromConfig(OAuth2Config config) {
        return OAuth2Client.builder().config(config).build();
    }

    // ==================== PKCE ====================

    /**
     * Generate a PKCE challenge
     * 生成 PKCE 挑战
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * PkceChallenge pkce = OpenOAuth2.generatePkce();
     *
     * // Use in authorization request
     * String authUrl = client.getAuthorizationUrl("state", pkce);
     *
     * // Use verifier in token exchange
     * OAuth2Token token = client.exchangeCode(code, pkce.verifier());
     * }</pre>
     *
     * @return the PKCE challenge | PKCE 挑战
     */
    public static PkceChallenge generatePkce() {
        return PkceChallenge.generate();
    }

    // ==================== JWT ====================

    /**
     * Parse a JWT token without signature verification
     * 解析 JWT 令牌（不验证签名）
     *
     * <p><strong>Warning | 警告:</strong></p>
     * <p>This method does NOT verify the JWT signature. For security-critical applications,
     * use a proper JWT library with signature verification.</p>
     * <p>此方法不验证 JWT 签名。对于安全关键的应用程序，请使用具有签名验证的正式 JWT 库。</p>
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * JwtClaims claims = OpenOAuth2.parseJwt(idToken);
     *
     * String subject = claims.sub();
     * String issuer = claims.iss();
     *
     * if (claims.isExpired()) {
     *     // Token is expired
     * }
     * }</pre>
     *
     * @param token the JWT token | JWT 令牌
     * @return the JWT claims | JWT 声明
     */
    public static JwtClaims parseJwt(String token) {
        return JwtClaims.parse(token);
    }

    /**
     * Check if a token is expired
     * 检查令牌是否已过期
     *
     * @param token the OAuth2 token | OAuth2 令牌
     * @return true if expired | 已过期返回 true
     */
    public static boolean isExpired(OAuth2Token token) {
        return token.isExpired();
    }

    /**
     * Check if a token is expiring soon
     * 检查令牌是否即将过期
     *
     * @param token     the OAuth2 token | OAuth2 令牌
     * @param threshold the time threshold | 时间阈值
     * @return true if expiring within threshold | 在阈值内即将过期返回 true
     */
    public static boolean isExpiringSoon(OAuth2Token token, Duration threshold) {
        return token.isExpiringSoon(threshold);
    }

    // ==================== Token Store ====================

    /**
     * Create an in-memory token store
     * 创建内存令牌存储
     *
     * @return the token store | 令牌存储
     */
    public static TokenStore inMemoryTokenStore() {
        return new InMemoryTokenStore();
    }

    /**
     * Create a file-based token store
     * 创建文件令牌存储
     *
     * @param directory the storage directory | 存储目录
     * @return the token store | 令牌存储
     */
    public static TokenStore fileTokenStore(Path directory) {
        return new FileTokenStore(directory);
    }

    /**
     * Create a file-based token store in the user's home directory
     * 在用户主目录中创建文件令牌存储
     *
     * @param appName the application name (used as subdirectory) | 应用程序名称（用作子目录）
     * @return the token store | 令牌存储
     */
    public static TokenStore fileTokenStore(String appName) {
        Path homeDir = Path.of(System.getProperty("user.home"));
        Path tokenDir = homeDir.resolve("." + appName).resolve("tokens");
        return new FileTokenStore(tokenDir);
    }

    // ==================== State Parameter ====================

    /**
     * Generate a cryptographically secure state parameter for CSRF protection
     * 生成用于 CSRF 防护的加密安全 state 参数
     *
     * @return the state parameter string | state 参数字符串
     */
    public static String generateState() {
        return StateParameter.generate();
    }

    /**
     * Validate a state parameter using constant-time comparison
     * 使用常量时间比较验证 state 参数
     *
     * @param expected the expected state | 期望的 state
     * @param actual   the actual state from callback | 回调中的实际 state
     * @return true if valid | 有效返回 true
     */
    public static boolean validateState(String expected, String actual) {
        return StateParameter.validate(expected, actual);
    }

    // ==================== OIDC Discovery ====================

    /**
     * Discover OIDC endpoints from an issuer URL
     * 从 issuer URL 发现 OIDC 端点
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * DiscoveryDocument doc = OpenOAuth2.discover("https://accounts.google.com");
     * String tokenEndpoint = doc.tokenEndpoint();
     * }</pre>
     *
     * @param issuerUrl the OIDC issuer URL | OIDC 颁发者 URL
     * @return the discovery document | 发现文档
     */
    public static DiscoveryDocument discover(String issuerUrl) {
        return OidcDiscovery.discover(issuerUrl);
    }

    /**
     * Discover OIDC endpoints using a custom HTTP client
     * 使用自定义 HTTP 客户端发现 OIDC 端点
     *
     * @param issuerUrl  the OIDC issuer URL | OIDC 颁发者 URL
     * @param httpClient the HTTP client | HTTP 客户端
     * @return the discovery document | 发现文档
     */
    public static DiscoveryDocument discover(String issuerUrl, OAuth2HttpClient httpClient) {
        return OidcDiscovery.discover(issuerUrl, httpClient);
    }

    // ==================== Token Introspection ====================

    /**
     * Create a token introspection client
     * 创建 Token 内省客户端
     *
     * @param introspectionEndpoint the introspection endpoint URL | 内省端点 URL
     * @param clientId              the client ID | 客户端 ID
     * @param clientSecret          the client secret | 客户端密钥
     * @return the token introspection client | Token 内省客户端
     */
    public static TokenIntrospection tokenIntrospection(String introspectionEndpoint,
                                                         String clientId, String clientSecret) {
        return new TokenIntrospection(introspectionEndpoint, clientId, clientSecret, new OAuth2HttpClient());
    }

    // ==================== PAR ====================

    /**
     * Create a pushed authorization request client
     * 创建推送授权请求客户端
     *
     * @param parEndpoint  the PAR endpoint URL | PAR 端点 URL
     * @param clientId     the client ID | 客户端 ID
     * @param clientSecret the client secret | 客户端密钥
     * @return the PAR client | PAR 客户端
     */
    public static PushedAuthorizationRequest par(String parEndpoint,
                                                  String clientId, String clientSecret) {
        return new PushedAuthorizationRequest(parEndpoint, clientId, clientSecret, new OAuth2HttpClient());
    }

    // ==================== Token Manager ====================

    /**
     * Create a default token manager builder
     * 创建默认 Token 管理器构建器
     *
     * <p><strong>Example | 示例:</strong></p>
     * <pre>{@code
     * TokenManager manager = OpenOAuth2.tokenManager()
     *     .tokenStore(OpenOAuth2.inMemoryTokenStore())
     *     .build();
     * }</pre>
     *
     * @return the token manager builder | Token 管理器构建器
     */
    public static DefaultTokenManager.Builder tokenManager() {
        return DefaultTokenManager.builder();
    }

    // ==================== Configuration Builder ====================

    /**
     * Create a new OAuth2 configuration builder
     * 创建新的 OAuth2 配置构建器
     *
     * @return the configuration builder | 配置构建器
     */
    public static OAuth2Config.Builder configBuilder() {
        return OAuth2Config.builder();
    }
}
