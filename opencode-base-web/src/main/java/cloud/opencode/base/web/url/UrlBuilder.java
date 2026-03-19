package cloud.opencode.base.web.url;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URL Builder - Fluent URL Constructor
 * URL 构建器 - 流畅的 URL 构造器
 *
 * <p>This class provides a fluent builder for constructing URLs with
 * scheme, host, port, path, and query parameters.</p>
 * <p>此类提供流畅的构建器用于构建包含协议、主机、端口、路径和查询参数的 URL。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Build complete URL
 * String url = UrlBuilder.create()
 *     .scheme("https")
 *     .host("api.example.com")
 *     .port(8080)
 *     .path("/users/{id}")
 *     .pathParam("id", "123")
 *     .queryParam("page", "1")
 *     .queryParam("size", "20")
 *     .build(); // "https://api.example.com:8080/users/123?page=1&size=20"
 *
 * // From existing URL
 * String newUrl = UrlBuilder.from("https://example.com/api")
 *     .path("/users")
 *     .queryParam("active", "true")
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent URL construction - 流式URL构建</li>
 *   <li>Path parameter substitution - 路径参数替换</li>
 *   <li>Query parameter building - 查询参数构建</li>
 *   <li>Path traversal protection - 路径遍历保护</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String url = UrlBuilder.create()
 *     .scheme("https").host("api.example.com")
 *     .path("/users/{id}").pathParam("id", "123")
 *     .queryParam("page", "1").build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder) - 否（可变构建器）</li>
 *   <li>Null-safe: Partial (handles null paths) - 部分（处理null路径）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = URL components - O(n), n为URL组件数</li>
 *   <li>Space complexity: O(n) for URL string - URL字符串 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class UrlBuilder {

    private String scheme = "https";
    private String host;
    private int port = -1;
    private String path = "";
    private String fragment;
    private final Map<String, String> pathParams = new HashMap<>();
    private final QueryString.Builder queryBuilder = QueryString.builder();

    private UrlBuilder() {
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new URL builder.
     * 创建新的 URL 构建器。
     *
     * @return the builder - 构建器
     */
    public static UrlBuilder create() {
        return new UrlBuilder();
    }

    /**
     * Creates a URL builder from existing URL.
     * 从现有 URL 创建构建器。
     *
     * @param url the base URL - 基础 URL
     * @return the builder - 构建器
     */
    public static UrlBuilder from(String url) {
        UrlBuilder builder = new UrlBuilder();
        if (url == null || url.isEmpty()) {
            return builder;
        }

        try {
            URI uri = URI.create(url);
            if (uri.getScheme() != null) {
                builder.scheme = uri.getScheme();
            }
            builder.host = uri.getHost();
            builder.port = uri.getPort();
            builder.path = uri.getPath() != null ? uri.getPath() : "";
            builder.fragment = uri.getFragment();

            if (uri.getQuery() != null) {
                QueryString qs = QueryString.parse(uri.getQuery());
                qs.toMap().forEach(builder.queryBuilder::add);
            }
        } catch (Exception e) {
            // If parsing fails, treat as path
            builder.path = url;
        }

        return builder;
    }

    /**
     * Creates a URL builder from URI.
     * 从 URI 创建构建器。
     *
     * @param uri the URI - URI
     * @return the builder - 构建器
     */
    public static UrlBuilder from(URI uri) {
        return from(uri.toString());
    }

    // ==================== Scheme ====================

    /**
     * Sets the scheme.
     * 设置协议。
     *
     * @param scheme the scheme (http or https) - 协议
     * @return this builder - 此构建器
     */
    public UrlBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Uses HTTP scheme.
     * 使用 HTTP 协议。
     *
     * @return this builder - 此构建器
     */
    public UrlBuilder http() {
        this.scheme = "http";
        return this;
    }

    /**
     * Uses HTTPS scheme.
     * 使用 HTTPS 协议。
     *
     * @return this builder - 此构建器
     */
    public UrlBuilder https() {
        this.scheme = "https";
        return this;
    }

    // ==================== Host and Port ====================

    /**
     * Sets the host.
     * 设置主机。
     *
     * @param host the host - 主机
     * @return this builder - 此构建器
     */
    public UrlBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Sets the port.
     * 设置端口。
     *
     * @param port the port (-1 for default) - 端口
     * @return this builder - 此构建器
     */
    public UrlBuilder port(int port) {
        this.port = port;
        return this;
    }

    // ==================== Path ====================

    /**
     * Sets the path.
     * 设置路径。
     *
     * @param path the path - 路径
     * @return this builder - 此构建器
     */
    public UrlBuilder path(String path) {
        this.path = path != null ? path : "";
        return this;
    }

    /**
     * Appends to the path.
     * 追加路径。
     *
     * @param segment the path segment - 路径段
     * @return this builder - 此构建器
     */
    public UrlBuilder appendPath(String segment) {
        if (segment == null || segment.isEmpty()) {
            return this;
        }
        if (this.path.endsWith("/") && segment.startsWith("/")) {
            this.path = this.path + segment.substring(1);
        } else if (!this.path.endsWith("/") && !segment.startsWith("/")) {
            this.path = this.path + "/" + segment;
        } else {
            this.path = this.path + segment;
        }
        return this;
    }

    /**
     * Sets a path parameter.
     * 设置路径参数。
     *
     * @param name  the parameter name - 参数名
     * @param value the parameter value - 参数值
     * @return this builder - 此构建器
     */
    public UrlBuilder pathParam(String name, String value) {
        this.pathParams.put(name, value);
        return this;
    }

    /**
     * Sets path parameters.
     * 设置路径参数。
     *
     * @param params the parameters - 参数
     * @return this builder - 此构建器
     */
    public UrlBuilder pathParams(Map<String, String> params) {
        this.pathParams.putAll(params);
        return this;
    }

    // ==================== Query Parameters ====================

    /**
     * Adds a query parameter.
     * 添加查询参数。
     *
     * @param name  the parameter name - 参数名
     * @param value the parameter value - 参数值
     * @return this builder - 此构建器
     */
    public UrlBuilder queryParam(String name, String value) {
        this.queryBuilder.add(name, value);
        return this;
    }

    /**
     * Adds a query parameter if value is not null.
     * 如果值不为 null，添加查询参数。
     *
     * @param name  the parameter name - 参数名
     * @param value the parameter value - 参数值
     * @return this builder - 此构建器
     */
    public UrlBuilder queryParamIfNotNull(String name, String value) {
        this.queryBuilder.addIfNotNull(name, value);
        return this;
    }

    /**
     * Adds a query parameter if value is not empty.
     * 如果值不为空，添加查询参数。
     *
     * @param name  the parameter name - 参数名
     * @param value the parameter value - 参数值
     * @return this builder - 此构建器
     */
    public UrlBuilder queryParamIfNotEmpty(String name, String value) {
        this.queryBuilder.addIfNotEmpty(name, value);
        return this;
    }

    /**
     * Adds query parameters.
     * 添加查询参数。
     *
     * @param params the parameters - 参数
     * @return this builder - 此构建器
     */
    public UrlBuilder queryParams(Map<String, String> params) {
        this.queryBuilder.addAll(params);
        return this;
    }

    /**
     * Sets query string.
     * 设置查询字符串。
     *
     * @param queryString the query string - 查询字符串
     * @return this builder - 此构建器
     */
    public UrlBuilder queryString(QueryString queryString) {
        queryString.toMap().forEach(this.queryBuilder::add);
        return this;
    }

    // ==================== Fragment ====================

    /**
     * Sets the fragment.
     * 设置片段。
     *
     * @param fragment the fragment - 片段
     * @return this builder - 此构建器
     */
    public UrlBuilder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    // ==================== Build ====================

    /**
     * Builds the URL string.
     * 构建 URL 字符串。
     *
     * @return the URL string - URL 字符串
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        // Scheme and host
        if (host != null && !host.isEmpty()) {
            sb.append(scheme).append("://").append(host);
            if (port > 0 && !isDefaultPort(scheme, port)) {
                sb.append(":").append(port);
            }
        }

        // Path with parameters replaced
        String resolvedPath = resolvePath();
        if (!resolvedPath.isEmpty()) {
            if (!resolvedPath.startsWith("/") && sb.length() > 0) {
                sb.append("/");
            }
            sb.append(resolvedPath);
        }

        // Query string
        QueryString qs = queryBuilder.build();
        if (!qs.isEmpty()) {
            sb.append("?").append(qs);
        }

        // Fragment
        if (fragment != null && !fragment.isEmpty()) {
            sb.append("#").append(fragment);
        }

        return sb.toString();
    }

    /**
     * Builds as URI.
     * 构建为 URI。
     *
     * @return the URI - URI
     */
    public URI buildUri() {
        return URI.create(build());
    }

    private String resolvePath() {
        if (path == null || path.isEmpty()) {
            return "";
        }
        String result = path;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            String encoded = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            validatePathParam(entry.getKey(), encoded);
            result = result.replace("{" + entry.getKey() + "}", encoded);
        }
        return result;
    }

    private void validatePathParam(String name, String encodedValue) {
        // Decode for inspection: URLEncoder encodes '/' as '%2F', '.' as '.',
        // ':' as '%3A'. Check both encoded and decoded forms for traversal sequences.
        String decoded = java.net.URLDecoder.decode(encodedValue, StandardCharsets.UTF_8);
        if (decoded.contains("..") || decoded.contains("//") || decoded.contains(":")) {
            throw new IllegalArgumentException(
                "Path parameter '" + name + "' contains dangerous sequence (path traversal, " +
                "double slash, or protocol prefix): " + decoded);
        }
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equals(scheme) && port == 80) ||
                ("https".equals(scheme) && port == 443);
    }

    @Override
    public String toString() {
        return build();
    }
}
