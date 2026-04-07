package cloud.opencode.base.oauth2.introspection;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import cloud.opencode.base.oauth2.internal.JsonParser;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Token Introspection Client (RFC 7662)
 * Token 内省客户端（RFC 7662）
 *
 * <p>Implements the OAuth 2.0 Token Introspection protocol as defined in RFC 7662.
 * Allows resource servers to query the authorization server about the state of an
 * access token and retrieve metadata about it.</p>
 * <p>实现 RFC 7662 定义的 OAuth 2.0 Token 内省协议。允许资源服务器向授权服务器查询
 * 访问 Token 的状态并获取其元数据信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 7662 compliant token introspection - 符合 RFC 7662 的 Token 内省</li>
 *   <li>Support for token type hints (access_token, refresh_token) - 支持 Token 类型提示</li>
 *   <li>Client authentication via client_id and client_secret - 通过 client_id 和 client_secret 进行客户端认证</li>
 *   <li>Automatic JSON response parsing - 自动 JSON 响应解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create introspection client
 * // 创建内省客户端
 * TokenIntrospection introspection = new TokenIntrospection(
 *     "https://auth.example.com/introspect",
 *     "my-client-id",
 *     "my-client-secret",
 *     httpClient
 * );
 *
 * // Introspect a token
 * // 内省一个 Token
 * IntrospectionResult result = introspection.introspect(accessToken);
 * if (result.active()) {
 *     System.out.println("Token belongs to: " + result.sub());
 * }
 *
 * // Introspect with type hint
 * // 带类型提示的内省
 * IntrospectionResult result = introspection.introspect(token, "refresh_token");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable state, delegates to thread-safe HTTP client)
 *       - 线程安全: 是（不可变状态，委托给线程安全的 HTTP 客户端）</li>
 *   <li>Null-safe: Yes (validates all inputs) - 空值安全: 是（验证所有输入）</li>
 *   <li>Requires HTTPS endpoint - 要求 HTTPS 端点</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc7662">RFC 7662 - OAuth 2.0 Token Introspection</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public class TokenIntrospection {

    private final String introspectionEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final OAuth2HttpClient httpClient;

    /**
     * Create a new token introspection client.
     * 创建新的 Token 内省客户端。
     *
     * @param introspectionEndpoint the introspection endpoint URL | 内省端点 URL
     * @param clientId              the client ID for authentication | 用于认证的客户端 ID
     * @param clientSecret          the client secret for authentication | 用于认证的客户端密钥
     * @param httpClient            the HTTP client to use | 要使用的 HTTP 客户端
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出
     */
    public TokenIntrospection(String introspectionEndpoint, String clientId,
                              String clientSecret, OAuth2HttpClient httpClient) {
        this.introspectionEndpoint = Objects.requireNonNull(introspectionEndpoint,
                "introspectionEndpoint cannot be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId cannot be null");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
    }

    /**
     * Introspect a token without a type hint.
     * 不带类型提示地内省一个 Token。
     *
     * @param token the token to introspect | 要内省的 Token
     * @return the introspection result | 内省结果
     * @throws OAuth2Exception with INTROSPECTION_FAILED if introspection fails
     *                         | 如果内省失败则抛出 INTROSPECTION_FAILED
     * @throws NullPointerException if token is null | 如果 token 为 null 则抛出
     */
    public IntrospectionResult introspect(String token) {
        return introspect(token, null);
    }

    /**
     * Introspect a token with an optional type hint.
     * 带可选类型提示地内省一个 Token。
     *
     * @param token         the token to introspect | 要内省的 Token
     * @param tokenTypeHint optional hint about the token type (e.g., "access_token", "refresh_token")
     *                      | 可选的 Token 类型提示（例如 "access_token"、"refresh_token"）
     * @return the introspection result | 内省结果
     * @throws OAuth2Exception with INTROSPECTION_FAILED if introspection fails
     *                         | 如果内省失败则抛出 INTROSPECTION_FAILED
     * @throws NullPointerException if token is null | 如果 token 为 null 则抛出
     */
    public IntrospectionResult introspect(String token, String tokenTypeHint) {
        Objects.requireNonNull(token, "token cannot be null");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("token", token);
        if (tokenTypeHint != null && !tokenTypeHint.isBlank()) {
            params.put("token_type_hint", tokenTypeHint);
        }
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);

        try {
            String responseBody = httpClient.postForm(introspectionEndpoint, params);
            return parseResponse(responseBody);
        } catch (OAuth2Exception e) {
            // Check for "not supported" style errors
            if (isNotSupportedError(e)) {
                throw new OAuth2Exception(OAuth2ErrorCode.INTROSPECTION_NOT_SUPPORTED,
                        e.getMessage(), e);
            }
            throw new OAuth2Exception(OAuth2ErrorCode.INTROSPECTION_FAILED,
                    e.getMessage(), e);
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.INTROSPECTION_FAILED,
                    e.getMessage(), e);
        }
    }

    /**
     * Parse the JSON response from the introspection endpoint.
     * 解析内省端点的 JSON 响应。
     *
     * @param responseBody the JSON response body | JSON 响应正文
     * @return the parsed introspection result | 解析后的内省结果
     */
    private IntrospectionResult parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.INTROSPECTION_FAILED,
                    "Empty introspection response");
        }

        Map<String, Object> json = JsonParser.parseObject(responseBody);

        if (!json.containsKey("active")) {
            throw new OAuth2Exception(OAuth2ErrorCode.INTROSPECTION_FAILED,
                    "Introspection response missing required 'active' field (RFC 7662)");
        }

        IntrospectionResult.Builder builder = IntrospectionResult.builder();
        builder.active(getBooleanValue(json, "active"));
        builder.scope(getStringValue(json, "scope"));
        builder.clientId(getStringValue(json, "client_id"));
        builder.username(getStringValue(json, "username"));
        builder.tokenType(getStringValue(json, "token_type"));
        builder.sub(getStringValue(json, "sub"));
        builder.aud(getStringValue(json, "aud"));
        builder.iss(getStringValue(json, "iss"));
        builder.jti(getStringValue(json, "jti"));

        Long expValue = getLongValue(json, "exp");
        if (expValue != null) {
            builder.exp(Instant.ofEpochSecond(expValue));
        }
        Long iatValue = getLongValue(json, "iat");
        if (iatValue != null) {
            builder.iat(Instant.ofEpochSecond(iatValue));
        }
        Long nbfValue = getLongValue(json, "nbf");
        if (nbfValue != null) {
            builder.nbf(Instant.ofEpochSecond(nbfValue));
        }

        Set<String> STANDARD_KEYS = Set.of("active", "scope", "client_id", "username",
                "token_type", "exp", "iat", "nbf", "sub", "aud", "iss", "jti");
        Map<String, Object> extraClaims = new LinkedHashMap<>(json);
        STANDARD_KEYS.forEach(extraClaims::remove);
        builder.claims(extraClaims);

        return builder.build();
    }

    /**
     * Check if the error indicates introspection is not supported.
     * 检查错误是否表示内省不被支持。
     *
     * @param e the exception | 异常
     * @return true if the error indicates not supported | 如果错误表示不支持返回 true
     */
    private boolean isNotSupportedError(OAuth2Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase();
        return lower.contains("not supported") || lower.contains("not_supported")
                || lower.contains("unsupported");
    }


    /**
     * Get a string value from the parsed JSON map.
     * 从解析后的 JSON map 中获取字符串值。
     *
     * @param map   the JSON map | JSON map
     * @param field the field name | 字段名
     * @return the string value, or null | 字符串值，或 null
     */
    private String getStringValue(Map<String, Object> map, String field) {
        Object value = map.get(field);
        return value instanceof String s ? s : null;
    }

    /**
     * Get a boolean value from the parsed JSON map.
     * 从解析后的 JSON map 中获取布尔值。
     *
     * @param map   the JSON map | JSON map
     * @param field the field name | 字段名
     * @return the boolean value, defaults to false | 布尔值，默认为 false
     */
    private boolean getBooleanValue(Map<String, Object> map, String field) {
        Object value = map.get(field);
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s);
        }
        return false;
    }

    /**
     * Get a long value from the parsed JSON map.
     * 从解析后的 JSON map 中获取长整型值。
     *
     * @param map   the JSON map | JSON map
     * @param field the field name | 字段名
     * @return the long value, or null | 长整型值，或 null
     */
    private Long getLongValue(Map<String, Object> map, String field) {
        Object value = map.get(field);
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
