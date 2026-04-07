package cloud.opencode.base.web.http;

import cloud.opencode.base.core.OpenBase64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * HTTP Headers - HTTP Request/Response Headers Container
 * HTTP 头部 - HTTP 请求/响应头部容器
 *
 * <p>This class provides a case-insensitive header container with support for
 * multiple values per header name.</p>
 * <p>此类提供不区分大小写的头部容器，支持每个头部名称有多个值。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * HttpHeaders headers = HttpHeaders.of()
 *     .add("Content-Type", "application/json")
 *     .add("Accept", "application/json")
 *     .add("X-Custom", "value1")
 *     .add("X-Custom", "value2");
 *
 * String contentType = headers.get("content-type"); // Case-insensitive
 * List<String> custom = headers.getAll("X-Custom");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Case-insensitive header storage - 不区分大小写的头部存储</li>
 *   <li>Multi-value header support - 多值头部支持</li>
 *   <li>Fluent API for header manipulation - 流式头部操作API</li>
 *   <li>Standard header name constants - 标准头部名称常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpHeaders headers = HttpHeaders.of()
 *     .contentType("application/json")
 *     .bearerAuth("token123");
 * String ct = headers.get("content-type");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: No (header name should not be null) - 否（头部名称不应为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class HttpHeaders implements Iterable<Map.Entry<String, List<String>>> {

    private static final System.Logger LOGGER = System.getLogger(HttpHeaders.class.getName());

    // ==================== Standard Header Names ====================

    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String COOKIE = "Cookie";
    public static final String DATE = "Date";
    public static final String ETAG = "ETag";
    public static final String EXPIRES = "Expires";
    public static final String HOST = "Host";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
    public static final String ORIGIN = "Origin";
    public static final String PRAGMA = "Pragma";
    public static final String RANGE = "Range";
    public static final String REFERER = "Referer";
    public static final String RETRY_AFTER = "Retry-After";
    public static final String SERVER = "Server";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String USER_AGENT = "User-Agent";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REQUESTED_WITH = "X-Requested-With";

    // ==================== Instance Fields ====================

    private final Map<String, List<String>> headers;

    private HttpHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates empty headers.
     * 创建空头部。
     *
     * @return empty HttpHeaders - 空的 HttpHeaders
     */
    public static HttpHeaders of() {
        return new HttpHeaders(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Creates headers from a map.
     * 从 Map 创建头部。
     *
     * @param map the headers map - 头部 Map
     * @return HttpHeaders - HttpHeaders
     */
    public static HttpHeaders of(Map<String, String> map) {
        HttpHeaders headers = of();
        if (map != null) {
            map.forEach(headers::set);
        }
        return headers;
    }

    /**
     * Creates headers from JDK HttpHeaders.
     * 从 JDK HttpHeaders 创建头部。
     *
     * @param jdkHeaders the JDK headers - JDK 头部
     * @return HttpHeaders - HttpHeaders
     */
    public static HttpHeaders from(java.net.http.HttpHeaders jdkHeaders) {
        Map<String, List<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        jdkHeaders.map().forEach((k, v) -> map.put(k, new ArrayList<>(v)));
        return new HttpHeaders(map);
    }

    /**
     * Creates a copy of the headers.
     * 创建头部的副本。
     *
     * @param source the source headers - 源头部
     * @return HttpHeaders copy - HttpHeaders 副本
     */
    public static HttpHeaders copyOf(HttpHeaders source) {
        Map<String, List<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        source.headers.forEach((k, v) -> map.put(k, new ArrayList<>(v)));
        return new HttpHeaders(map);
    }

    // ==================== Modification Methods ====================

    /**
     * Adds a header value.
     * 添加头部值。
     *
     * @param name  the header name - 头部名称
     * @param value the header value - 头部值
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders add(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * Sets a header value (replaces existing).
     * 设置头部值（替换现有值）。
     *
     * @param name  the header name - 头部名称
     * @param value the header value - 头部值
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders set(String name, String value) {
        List<String> values = new ArrayList<>(1);
        values.add(value);
        headers.put(name, values);
        return this;
    }

    /**
     * Sets multiple values for a header.
     * 为头部设置多个值。
     *
     * @param name   the header name - 头部名称
     * @param values the header values - 头部值列表
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders set(String name, List<String> values) {
        headers.put(name, new ArrayList<>(values));
        return this;
    }

    /**
     * Removes a header.
     * 移除头部。
     *
     * @param name the header name - 头部名称
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders remove(String name) {
        headers.remove(name);
        return this;
    }

    /**
     * Clears all headers.
     * 清除所有头部。
     *
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders clear() {
        headers.clear();
        return this;
    }

    // ==================== Query Methods ====================

    /**
     * Gets the first value for a header.
     * 获取头部的第一个值。
     *
     * @param name the header name - 头部名称
     * @return the first value or null - 第一个值或 null
     */
    public String get(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() ? values.getFirst() : null;
    }

    /**
     * Gets the first value for a header with default.
     * 获取头部的第一个值，带默认值。
     *
     * @param name         the header name - 头部名称
     * @param defaultValue the default value - 默认值
     * @return the first value or default - 第一个值或默认值
     */
    public String getOrDefault(String name, String defaultValue) {
        String value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets all values for a header.
     * 获取头部的所有值。
     *
     * @param name the header name - 头部名称
     * @return the values list (never null) - 值列表（永不为 null）
     */
    public List<String> getAll(String name) {
        List<String> values = headers.get(name);
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    /**
     * Checks if a header exists.
     * 检查头部是否存在。
     *
     * @param name the header name - 头部名称
     * @return true if exists - 如果存在返回 true
     */
    public boolean contains(String name) {
        return headers.containsKey(name);
    }

    /**
     * Gets the number of headers.
     * 获取头部数量。
     *
     * @return the count - 数量
     */
    public int size() {
        return headers.size();
    }

    /**
     * Checks if headers are empty.
     * 检查头部是否为空。
     *
     * @return true if empty - 如果为空返回 true
     */
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    /**
     * Gets all header names.
     * 获取所有头部名称。
     *
     * @return the names set - 名称集合
     */
    public Set<String> names() {
        return Collections.unmodifiableSet(headers.keySet());
    }

    /**
     * Converts to unmodifiable map.
     * 转换为不可修改的 Map。
     *
     * @return the map - Map
     */
    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach((k, v) -> result.put(k, Collections.unmodifiableList(v)));
        return Collections.unmodifiableMap(result);
    }

    /**
     * Converts to single-value map.
     * 转换为单值 Map。
     *
     * @return the map - Map
     */
    public Map<String, String> toSingleValueMap() {
        Map<String, String> result = new LinkedHashMap<>();
        headers.forEach((k, v) -> {
            if (!v.isEmpty()) {
                result.put(k, v.getFirst());
            }
        });
        return Collections.unmodifiableMap(result);
    }

    // ==================== Specific Header Methods ====================

    /**
     * Gets Content-Type header.
     * 获取 Content-Type 头部。
     *
     * @return the content type or null - 内容类型或 null
     */
    public ContentType getContentType() {
        String value = get(CONTENT_TYPE);
        return value != null ? ContentType.parse(value) : null;
    }

    /**
     * Gets Content-Length header.
     * 获取 Content-Length 头部。
     *
     * @return the content length or -1 - 内容长度或 -1
     */
    public long getContentLength() {
        String value = get(CONTENT_LENGTH);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                LOGGER.log(System.Logger.Level.DEBUG, "Invalid Content-Length header value", e);
            }
        }
        return -1;
    }

    /**
     * Sets Content-Type header.
     * 设置 Content-Type 头部。
     *
     * @param contentType the content type - 内容类型
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders contentType(String contentType) {
        return set(CONTENT_TYPE, contentType);
    }

    /**
     * Sets Content-Type header.
     * 设置 Content-Type 头部。
     *
     * @param contentType the content type - 内容类型
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders contentType(ContentType contentType) {
        return set(CONTENT_TYPE, contentType.toString());
    }

    /**
     * Sets Accept header.
     * 设置 Accept 头部。
     *
     * @param accept the accept value - Accept 值
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders accept(String accept) {
        return set(ACCEPT, accept);
    }

    /**
     * Sets Authorization header with Bearer token.
     * 使用 Bearer token 设置 Authorization 头部。
     *
     * @param token the bearer token - Bearer token
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders bearerAuth(String token) {
        return set(AUTHORIZATION, "Bearer " + token);
    }

    /**
     * Sets Authorization header with Basic auth.
     * 使用 Basic auth 设置 Authorization 头部。
     *
     * @param username the username - 用户名
     * @param password the password - 密码
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = OpenBase64.encode(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return set(AUTHORIZATION, "Basic " + encoded);
    }

    /**
     * Sets User-Agent header.
     * 设置 User-Agent 头部。
     *
     * @param userAgent the user agent - User-Agent
     * @return this for chaining - 用于链式调用
     */
    public HttpHeaders userAgent(String userAgent) {
        return set(USER_AGENT, userAgent);
    }

    // ==================== Object Methods ====================

    @Override
    public Iterator<Map.Entry<String, List<String>>> iterator() {
        return Collections.unmodifiableMap(headers).entrySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HttpHeaders{");
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }
}
