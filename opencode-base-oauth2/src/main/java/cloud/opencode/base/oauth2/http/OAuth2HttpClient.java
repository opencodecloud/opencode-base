package cloud.opencode.base.oauth2.http;

import cloud.opencode.base.oauth2.OAuth2Config;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;
import cloud.opencode.base.oauth2.internal.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OAuth2 HTTP Client
 * OAuth2 HTTP 客户端
 *
 * <p>HTTP client wrapper for OAuth2 operations using JDK HttpClient.</p>
 * <p>使用 JDK HttpClient 的 OAuth2 操作 HTTP 客户端包装器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JDK HttpClient integration - JDK HttpClient 集成</li>
 *   <li>Form URL encoded requests - 表单 URL 编码请求</li>
 *   <li>JSON response handling - JSON 响应处理</li>
 *   <li>Timeout configuration - 超时配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Used internally by OAuth2Client
 * // 由OAuth2Client内部使用
 * OAuth2HttpClient client = new OAuth2HttpClient(config);
 * String response = client.post(tokenUrl, params);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless after construction) - 线程安全: 是（构造后无状态）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class OAuth2HttpClient implements AutoCloseable {

    private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ACCEPT_JSON = "application/json";

    private final HttpClient httpClient;
    private final Duration connectTimeout;
    private final Duration readTimeout;

    /**
     * Create a new OAuth2 HTTP client with default settings
     * 使用默认设置创建新的 OAuth2 HTTP 客户端
     */
    public OAuth2HttpClient() {
        this(Duration.ofSeconds(10), Duration.ofSeconds(30));
    }

    /**
     * Create a new OAuth2 HTTP client with custom timeouts
     * 使用自定义超时创建新的 OAuth2 HTTP 客户端
     *
     * @param connectTimeout the connection timeout | 连接超时
     * @param readTimeout    the read timeout | 读取超时
     */
    public OAuth2HttpClient(Duration connectTimeout, Duration readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * Create a new OAuth2 HTTP client from config
     * 从配置创建新的 OAuth2 HTTP 客户端
     *
     * @param config the OAuth2 config | OAuth2 配置
     */
    public OAuth2HttpClient(OAuth2Config config) {
        this(config.connectTimeout(), config.readTimeout());
    }

    /**
     * Send a POST request with form-encoded body
     * 发送带有表单编码正文的 POST 请求
     *
     * @param url    the URL | URL
     * @param params the form parameters | 表单参数
     * @return the response body | 响应正文
     * @throws OAuth2Exception on HTTP errors | HTTP 错误时抛出
     */
    public String postForm(String url, Map<String, String> params) {
        return postForm(url, params, Map.of());
    }

    /**
     * Send a POST request with form-encoded body and headers
     * 发送带有表单编码正文和头的 POST 请求
     *
     * @param url     the URL | URL
     * @param params  the form parameters | 表单参数
     * @param headers additional headers | 附加头
     * @return the response body | 响应正文
     * @throws OAuth2Exception on HTTP errors | HTTP 错误时抛出
     */
    public String postForm(String url, Map<String, String> params, Map<String, String> headers) {
        validateHttpsUrl(url);
        String body = encodeFormParams(params);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(readTimeout)
                .header("Content-Type", CONTENT_TYPE_FORM)
                .header("Accept", ACCEPT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(body));

        headers.forEach(builder::header);

        return execute(builder.build());
    }

    /**
     * Send a GET request
     * 发送 GET 请求
     *
     * @param url     the URL | URL
     * @param headers the headers | 头
     * @return the response body | 响应正文
     * @throws OAuth2Exception on HTTP errors | HTTP 错误时抛出
     */
    public String get(String url, Map<String, String> headers) {
        validateHttpsUrl(url);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(readTimeout)
                .header("Accept", ACCEPT_JSON)
                .GET();

        headers.forEach(builder::header);

        return execute(builder.build());
    }

    /**
     * Execute an HTTP request
     * 执行 HTTP 请求
     *
     * @param request the request | 请求
     * @return the response body | 响应正文
     * @throws OAuth2Exception on HTTP errors | HTTP 错误时抛出
     */
    private String execute(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                return response.body();
            }

            // Handle OAuth2 error response
            String errorBody = response.body();
            throw createErrorException(status, errorBody);

        } catch (IOException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OAuth2Exception(OAuth2ErrorCode.TIMEOUT, "Request interrupted", e);
        }
    }

    /**
     * Create an exception from an error response
     * 从错误响应创建异常
     *
     * @param status the HTTP status | HTTP 状态
     * @param body   the response body | 响应正文
     * @return the exception | 异常
     */
    private OAuth2Exception createErrorException(int status, String body) {
        // Try to parse OAuth2 error response (RFC 6749 Section 5.2)
        String error = JsonParser.getString(body, "error");
        String description = JsonParser.getString(body, "error_description");
        String errorUri = JsonParser.getString(body, "error_uri");

        if (error != null) {
            return OAuth2Exception.fromServerError(error, description, errorUri);
        }

        // Generic HTTP error
        OAuth2ErrorCode code = status >= 500 ? OAuth2ErrorCode.SERVER_ERROR :
                status == 401 ? OAuth2ErrorCode.AUTHORIZATION_FAILED :
                        OAuth2ErrorCode.INVALID_RESPONSE;

        String safeBody = body != null && body.length() > 256
                ? body.substring(0, 256) + "..." : body;
        return new OAuth2Exception(code, "HTTP " + status + ": " + safeBody);
    }

    /**
     * Encode form parameters
     * 编码表单参数
     *
     * @param params the parameters | 参数
     * @return the encoded string | 编码后的字符串
     */
    private String encodeFormParams(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * Validate that URL uses HTTPS protocol (allows HTTP for localhost/127.0.0.1 for dev/testing).
     * 验证 URL 使用 HTTPS 协议（允许 localhost/127.0.0.1 使用 HTTP 进行开发/测试）。
     *
     * @param url the URL to validate | 要验证的 URL
     * @throws OAuth2Exception if URL is not HTTPS | 如果 URL 不是 HTTPS 则抛出
     */
    private void validateHttpsUrl(String url) {
        if (url == null) {
            throw new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG,
                    "OAuth2 endpoint URL cannot be null");
        }
        if (url.toLowerCase().startsWith("https://")) {
            return;
        }
        if (url.toLowerCase().startsWith("http://")) {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)
                    || "::1".equals(host)) {
                return;
            }
        }
        // Redact query parameters that may contain credentials
        String safeUrl = url;
        int queryIdx = url.indexOf('?');
        if (queryIdx >= 0) {
            safeUrl = url.substring(0, queryIdx) + "?[redacted]";
        }
        throw new OAuth2Exception(OAuth2ErrorCode.INVALID_CONFIG,
                "OAuth2 endpoints must use HTTPS for security (HTTP allowed only for localhost/127.0.0.1). URL: " + safeUrl);
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
