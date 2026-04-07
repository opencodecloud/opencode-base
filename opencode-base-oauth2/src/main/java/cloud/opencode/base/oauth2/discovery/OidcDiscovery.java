package cloud.opencode.base.oauth2.discovery;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import cloud.opencode.base.oauth2.internal.JsonParser;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OIDC Discovery Client
 * OIDC 发现客户端
 *
 * <p>Fetches and caches OpenID Connect Discovery configuration documents
 * from the well-known endpoint as defined in OIDC Discovery 1.0.</p>
 * <p>从 OIDC Discovery 1.0 定义的 well-known 端点获取和缓存
 * OpenID Connect 发现配置文档。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fetches /.well-known/openid-configuration - 获取 /.well-known/openid-configuration</li>
 *   <li>Thread-safe caching with ConcurrentHashMap - 使用 ConcurrentHashMap 的线程安全缓存</li>
 *   <li>Issuer validation - 颁发者验证</li>
 *   <li>Custom HTTP client support - 自定义 HTTP 客户端支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Discover OIDC configuration
 * DiscoveryDocument doc = OidcDiscovery.discover("https://accounts.google.com");
 *
 * // With custom HTTP client
 * OAuth2HttpClient httpClient = new OAuth2HttpClient();
 * DiscoveryDocument doc = OidcDiscovery.discover("https://accounts.google.com", httpClient);
 *
 * // Clear the cache
 * OidcDiscovery.clearCache();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Validates issuer in response matches expected issuer - 验证响应中的颁发者与期望颁发者匹配</li>
 *   <li>Uses HTTPS for all discovery requests - 所有发现请求使用 HTTPS</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe. The internal cache uses ConcurrentHashMap.</p>
 * <p>此类是线程安全的。内部缓存使用 ConcurrentHashMap。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OIDC Discovery 1.0</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public final class OidcDiscovery {

    /**
     * Well-known path suffix for OIDC discovery.
     * OIDC 发现的 well-known 路径后缀。
     */
    private static final String WELL_KNOWN_PATH = "/.well-known/openid-configuration";

    /**
     * Default cache TTL (1 hour).
     * 默认缓存 TTL（1 小时）。
     */
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * Cached entry wrapping a discovery document with its fetch timestamp.
     * 缓存条目，包含发现文档及其获取时间戳。
     */
    private record CachedEntry(DiscoveryDocument document, Instant fetchedAt) {
        boolean isExpired() {
            return Instant.now().isAfter(fetchedAt.plus(CACHE_TTL));
        }
    }

    /**
     * Thread-safe cache of discovery documents keyed by issuer URL.
     * 以颁发者 URL 为键的发现文档的线程安全缓存。
     */
    private static final ConcurrentHashMap<String, CachedEntry> CACHE = new ConcurrentHashMap<>();

    /**
     * Lazily-initialized default HTTP client holder (initialization-on-demand).
     * 延迟初始化的默认 HTTP 客户端持有者（按需初始化）。
     */
    private static final class DefaultClientHolder {
        static final OAuth2HttpClient INSTANCE = new OAuth2HttpClient();
    }

    private OidcDiscovery() {
        // utility class
    }

    /**
     * Discover OIDC configuration from the given issuer URL using a default HTTP client.
     * 使用默认 HTTP 客户端从给定的颁发者 URL 发现 OIDC 配置。
     *
     * <p>Results are cached by issuer URL. Subsequent calls with the same issuer
     * return the cached document.</p>
     * <p>结果按颁发者 URL 缓存。使用相同颁发者的后续调用返回缓存的文档。</p>
     *
     * @param issuerUrl the issuer URL (e.g., "https://accounts.google.com") | 颁发者 URL
     * @return the discovery document | 发现文档
     * @throws OAuth2Exception with DISCOVERY_FAILED if the request fails |
     *         如果请求失败则抛出 DISCOVERY_FAILED
     * @throws OAuth2Exception with DISCOVERY_INVALID_RESPONSE if the response is invalid |
     *         如果响应无效则抛出 DISCOVERY_INVALID_RESPONSE
     * @throws NullPointerException if issuerUrl is null | 如果 issuerUrl 为 null
     */
    public static DiscoveryDocument discover(String issuerUrl) {
        return discover(issuerUrl, DefaultClientHolder.INSTANCE);
    }

    /**
     * Discover OIDC configuration from the given issuer URL using a custom HTTP client.
     * 使用自定义 HTTP 客户端从给定的颁发者 URL 发现 OIDC 配置。
     *
     * <p>Results are cached by issuer URL. Subsequent calls with the same issuer
     * return the cached document.</p>
     * <p>结果按颁发者 URL 缓存。使用相同颁发者的后续调用返回缓存的文档。</p>
     *
     * @param issuerUrl  the issuer URL (e.g., "https://accounts.google.com") | 颁发者 URL
     * @param httpClient the HTTP client to use | 要使用的 HTTP 客户端
     * @return the discovery document | 发现文档
     * @throws OAuth2Exception with DISCOVERY_FAILED if the request fails |
     *         如果请求失败则抛出 DISCOVERY_FAILED
     * @throws OAuth2Exception with DISCOVERY_INVALID_RESPONSE if the response is invalid |
     *         如果响应无效则抛出 DISCOVERY_INVALID_RESPONSE
     * @throws NullPointerException if issuerUrl or httpClient is null | 如果 issuerUrl 或 httpClient 为 null
     */
    public static DiscoveryDocument discover(String issuerUrl, OAuth2HttpClient httpClient) {
        Objects.requireNonNull(issuerUrl, "issuerUrl cannot be null");
        Objects.requireNonNull(httpClient, "httpClient cannot be null");

        // SSRF guard: issuer URL must use HTTPS per OpenID Connect Discovery spec
        validateIssuerUrl(issuerUrl);

        String normalizedIssuer = normalizeIssuerUrl(issuerUrl);

        // Fast path: lock-free read for cache hits
        CachedEntry entry = CACHE.get(normalizedIssuer);
        if (entry != null && !entry.isExpired()) {
            return entry.document();
        }

        // Slow path: atomic fetch-and-cache
        CachedEntry fresh = CACHE.compute(normalizedIssuer, (key, existing) -> {
            // Double-check after acquiring segment lock
            if (existing != null && !existing.isExpired()) {
                return existing;
            }
            DiscoveryDocument doc = fetchDiscoveryDocument(key, httpClient);
            return new CachedEntry(doc, Instant.now());
        });
        return fresh.document();
    }

    /**
     * Validate that the issuer URL uses HTTPS scheme.
     * 验证颁发者 URL 使用 HTTPS 方案。
     *
     * @param url the issuer URL | 颁发者 URL
     * @throws OAuth2Exception if the URL is invalid | 如果 URL 无效
     */
    private static void validateIssuerUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_FAILED, "Issuer URL cannot be empty");
        }
        if (!url.startsWith("https://")) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_FAILED,
                    "Issuer URL must use HTTPS scheme per OpenID Connect Discovery spec");
        }
    }

    /**
     * Clear the discovery document cache.
     * 清除发现文档缓存。
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Normalize the issuer URL by removing trailing slashes.
     * 通过移除尾部斜杠来规范化颁发者 URL。
     *
     * @param issuerUrl the issuer URL | 颁发者 URL
     * @return the normalized issuer URL | 规范化的颁发者 URL
     */
    private static String normalizeIssuerUrl(String issuerUrl) {
        int end = issuerUrl.length();
        while (end > 0 && issuerUrl.charAt(end - 1) == '/') {
            end--;
        }
        return end == issuerUrl.length() ? issuerUrl : issuerUrl.substring(0, end);
    }

    /**
     * Fetch and parse the discovery document from the well-known endpoint.
     * 从 well-known 端点获取和解析发现文档。
     *
     * @param issuerUrl  the normalized issuer URL | 规范化的颁发者 URL
     * @param httpClient the HTTP client | HTTP 客户端
     * @return the parsed discovery document | 解析后的发现文档
     */
    private static DiscoveryDocument fetchDiscoveryDocument(String issuerUrl, OAuth2HttpClient httpClient) {
        String discoveryUrl = issuerUrl + WELL_KNOWN_PATH;

        String json;
        try {
            json = httpClient.get(discoveryUrl, Map.of());
        } catch (OAuth2Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_FAILED,
                    "Failed to fetch discovery document from " + discoveryUrl, e);
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_FAILED,
                    "Failed to fetch discovery document from " + discoveryUrl, e);
        }

        if (json == null || json.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE,
                    "Empty response from " + discoveryUrl);
        }

        return parseDiscoveryDocument(json, issuerUrl);
    }

    /**
     * Parse a discovery document JSON string using a single-pass JSON parse.
     * 使用单次 JSON 解析来解析发现文档 JSON 字符串。
     *
     * @param json      the JSON string | JSON 字符串
     * @param issuerUrl the expected issuer URL | 期望的颁发者 URL
     * @return the parsed discovery document | 解析后的发现文档
     */
    private static DiscoveryDocument parseDiscoveryDocument(String json, String issuerUrl) {
        Map<String, Object> fields = JsonParser.parseObject(json);

        String issuer = getStringField(fields, "issuer");
        if (issuer == null || issuer.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE,
                    "Missing 'issuer' field in discovery document");
        }

        // Validate issuer matches
        String normalizedResponseIssuer = normalizeIssuerUrl(issuer);
        if (!issuerUrl.equals(normalizedResponseIssuer)) {
            throw new OAuth2Exception(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE,
                    "Issuer mismatch: expected '" + issuerUrl + "' but got '" + issuer + "'");
        }

        return DiscoveryDocument.builder()
                .issuer(issuer)
                .authorizationEndpoint(getStringField(fields, "authorization_endpoint"))
                .tokenEndpoint(getStringField(fields, "token_endpoint"))
                .userinfoEndpoint(getStringField(fields, "userinfo_endpoint"))
                .jwksUri(getStringField(fields, "jwks_uri"))
                .registrationEndpoint(getStringField(fields, "registration_endpoint"))
                .revocationEndpoint(getStringField(fields, "revocation_endpoint"))
                .introspectionEndpoint(getStringField(fields, "introspection_endpoint"))
                .deviceAuthorizationEndpoint(getStringField(fields, "device_authorization_endpoint"))
                .parEndpoint(getStringField(fields, "pushed_authorization_request_endpoint"))
                .scopesSupported(getStringListField(fields, "scopes_supported"))
                .responseTypesSupported(getStringListField(fields, "response_types_supported"))
                .grantTypesSupported(getStringListField(fields, "grant_types_supported"))
                .tokenEndpointAuthMethodsSupported(
                        getStringListField(fields, "token_endpoint_auth_methods_supported"))
                .codeChallengeMethodsSupported(
                        getStringListField(fields, "code_challenge_methods_supported"))
                .build();
    }

    /**
     * Extract a string field from a parsed JSON map.
     * 从解析后的 JSON map 中提取字符串字段。
     *
     * @param fields the parsed JSON fields | 解析后的 JSON 字段
     * @param key    the field key | 字段键
     * @return the string value or null | 字符串值或 null
     */
    private static String getStringField(Map<String, Object> fields, String key) {
        Object value = fields.get(key);
        return value instanceof String s ? s : null;
    }

    /**
     * Extract a string list field from a parsed JSON map.
     * 从解析后的 JSON map 中提取字符串列表字段。
     *
     * @param fields the parsed JSON fields | 解析后的 JSON 字段
     * @param key    the field key | 字段键
     * @return the list of strings, or empty list if not found | 字符串列表，未找到返回空列表
     */
    @SuppressWarnings("unchecked")
    private static List<String> getStringListField(Map<String, Object> fields, String key) {
        Object value = fields.get(key);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }
}
