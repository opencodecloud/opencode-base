package cloud.opencode.base.oauth2.par;

import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.http.OAuth2HttpClient;
import cloud.opencode.base.oauth2.internal.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Pushed Authorization Request Client (RFC 9126)
 * 推送授权请求客户端（RFC 9126）
 *
 * <p>Implements the Pushed Authorization Requests (PAR) protocol as defined in RFC 9126.
 * PAR allows clients to push the payload of an authorization request to the authorization
 * server via a direct request, receiving a request_uri in return that can be used as a
 * reference to the authorization request in a subsequent call to the authorization endpoint.</p>
 * <p>实现 RFC 9126 定义的推送授权请求（PAR）协议。PAR 允许客户端通过直接请求将授权请求的
 * 有效载荷推送到授权服务器，返回一个 request_uri，可以在后续调用授权端点时作为授权请求的引用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 9126 compliant pushed authorization requests - 符合 RFC 9126 的推送授权请求</li>
 *   <li>Push authorization parameters to PAR endpoint - 推送授权参数到 PAR 端点</li>
 *   <li>Build authorization URL from PAR response - 从 PAR 响应构建授权 URL</li>
 *   <li>Client authentication via client_id and client_secret - 通过 client_id 和 client_secret 进行客户端认证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create PAR client
 * // 创建 PAR 客户端
 * PushedAuthorizationRequest par = new PushedAuthorizationRequest(
 *     "https://auth.example.com/par",
 *     "my-client-id",
 *     "my-client-secret",
 *     httpClient
 * );
 *
 * // Push authorization parameters
 * // 推送授权参数
 * Map<String, String> params = Map.of(
 *     "response_type", "code",
 *     "redirect_uri", "https://app.example.com/callback",
 *     "scope", "openid profile",
 *     "state", "random-state"
 * );
 * ParResponse response = par.push(params);
 *
 * // Build authorization URL
 * // 构建授权 URL
 * String authUrl = par.buildAuthorizationUrl(
 *     "https://auth.example.com/authorize", response, "my-client-id");
 * // Redirect user to authUrl
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
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9126">RFC 9126 - Pushed Authorization Requests</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public class PushedAuthorizationRequest {

    private final String parEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final OAuth2HttpClient httpClient;

    /**
     * Create a new Pushed Authorization Request client.
     * 创建新的推送授权请求客户端。
     *
     * @param parEndpoint  the PAR endpoint URL | PAR 端点 URL
     * @param clientId     the client ID for authentication | 用于认证的客户端 ID
     * @param clientSecret the client secret for authentication | 用于认证的客户端密钥
     * @param httpClient   the HTTP client to use | 要使用的 HTTP 客户端
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出
     */
    public PushedAuthorizationRequest(String parEndpoint, String clientId,
                                      String clientSecret, OAuth2HttpClient httpClient) {
        this.parEndpoint = Objects.requireNonNull(parEndpoint, "parEndpoint cannot be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId cannot be null");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
    }

    /**
     * Push authorization parameters to the PAR endpoint.
     * 推送授权参数到 PAR 端点。
     *
     * <p>Sends a POST request to the PAR endpoint with the provided authorization parameters
     * along with client credentials. Returns a {@link ParResponse} containing the request_uri
     * that can be used in subsequent authorization requests.</p>
     * <p>向 PAR 端点发送 POST 请求，包含提供的授权参数和客户端凭据。返回包含 request_uri 的
     * {@link ParResponse}，可用于后续的授权请求。</p>
     *
     * @param authorizationParams the authorization parameters to push | 要推送的授权参数
     * @return the PAR response | PAR 响应
     * @throws OAuth2Exception with PAR_FAILED if the request fails
     *                         | 如果请求失败则抛出 PAR_FAILED
     * @throws OAuth2Exception with PAR_NOT_SUPPORTED if PAR is not supported
     *                         | 如果 PAR 不被支持则抛出 PAR_NOT_SUPPORTED
     * @throws NullPointerException if authorizationParams is null | 如果 authorizationParams 为 null 则抛出
     */
    public ParResponse push(Map<String, String> authorizationParams) {
        Objects.requireNonNull(authorizationParams, "authorizationParams cannot be null");

        Map<String, String> params = new LinkedHashMap<>(authorizationParams);
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);

        try {
            String responseBody = httpClient.postForm(parEndpoint, params);
            return parseResponse(responseBody);
        } catch (OAuth2Exception e) {
            if (isNotSupportedError(e)) {
                throw new OAuth2Exception(OAuth2ErrorCode.PAR_NOT_SUPPORTED,
                        e.getMessage(), e);
            }
            throw new OAuth2Exception(OAuth2ErrorCode.PAR_FAILED,
                    e.getMessage(), e);
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.PAR_FAILED,
                    e.getMessage(), e);
        }
    }

    /**
     * Build an authorization URL using the PAR response.
     * 使用 PAR 响应构建授权 URL。
     *
     * <p>Constructs the authorization URL that the user should be redirected to.
     * The URL contains the client_id and the request_uri from the PAR response.</p>
     * <p>构建用户应该被重定向到的授权 URL。URL 包含 client_id 和来自 PAR 响应的 request_uri。</p>
     *
     * @param authorizationEndpoint the authorization endpoint URL | 授权端点 URL
     * @param parResponse           the PAR response containing request_uri | 包含 request_uri 的 PAR 响应
     * @param clientId              the client ID | 客户端 ID
     * @return the authorization URL | 授权 URL
     * @throws NullPointerException if any argument is null | 如果任何参数为 null 则抛出
     */
    public static String buildAuthorizationUrl(String authorizationEndpoint,
                                               ParResponse parResponse,
                                               String clientId) {
        Objects.requireNonNull(authorizationEndpoint, "authorizationEndpoint cannot be null");
        Objects.requireNonNull(parResponse, "parResponse cannot be null");
        Objects.requireNonNull(clientId, "clientId cannot be null");

        return authorizationEndpoint
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&request_uri=" + URLEncoder.encode(parResponse.requestUri(), StandardCharsets.UTF_8);
    }

    /**
     * Parse the JSON response from the PAR endpoint.
     * 解析 PAR 端点的 JSON 响应。
     *
     * @param responseBody the JSON response body | JSON 响应正文
     * @return the parsed PAR response | 解析后的 PAR 响应
     */
    private ParResponse parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.PAR_FAILED, "Empty PAR response");
        }

        String requestUri = JsonParser.getString(responseBody, "request_uri");
        if (requestUri == null || requestUri.isBlank()) {
            throw new OAuth2Exception(OAuth2ErrorCode.PAR_FAILED,
                    "Missing request_uri in PAR response");
        }

        Long expiresInLong = JsonParser.getLong(responseBody, "expires_in");
        int expiresIn = expiresInLong != null ? expiresInLong.intValue() : 0;

        return new ParResponse(requestUri, expiresIn, Instant.now());
    }

    /**
     * Check if the error indicates PAR is not supported.
     * 检查错误是否表示 PAR 不被支持。
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

}
