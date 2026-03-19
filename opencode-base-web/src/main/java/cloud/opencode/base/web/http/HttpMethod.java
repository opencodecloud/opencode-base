package cloud.opencode.base.web.http;

/**
 * HTTP Method - HTTP Request Method Enumeration
 * HTTP 方法 - HTTP 请求方法枚举
 *
 * <p>This enum defines standard HTTP methods as specified in RFC 7231 and RFC 5789.</p>
 * <p>此枚举定义了 RFC 7231 和 RFC 5789 中规定的标准 HTTP 方法。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * HttpMethod method = HttpMethod.GET;
 * boolean hasBody = method.hasRequestBody();
 * boolean safe = method.isSafe();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard HTTP method enumeration - 标准HTTP方法枚举</li>
 *   <li>Idempotent and safe method metadata - 幂等和安全方法元数据</li>
 *   <li>Request body indicator - 请求体指示器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpMethod method = HttpMethod.GET;
 * boolean safe = method.isSafe();       // true
 * boolean hasBody = method.hasRequestBody(); // false
 * HttpMethod parsed = HttpMethod.fromString("POST");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 是（枚举是不可变的）</li>
 *   <li>Null-safe: No (fromString throws on null) - 否（fromString对null抛出异常）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public enum HttpMethod {

    /**
     * GET - Retrieve resource
     * GET - 获取资源
     */
    GET(false, true, true),

    /**
     * POST - Create resource
     * POST - 创建资源
     */
    POST(true, false, false),

    /**
     * PUT - Replace resource
     * PUT - 替换资源
     */
    PUT(true, true, false),

    /**
     * DELETE - Delete resource
     * DELETE - 删除资源
     */
    DELETE(false, true, false),

    /**
     * PATCH - Partial update
     * PATCH - 部分更新
     */
    PATCH(true, false, false),

    /**
     * HEAD - Get headers only
     * HEAD - 仅获取头部
     */
    HEAD(false, true, true),

    /**
     * OPTIONS - Get supported methods
     * OPTIONS - 获取支持的方法
     */
    OPTIONS(false, true, true),

    /**
     * TRACE - Echo request
     * TRACE - 回显请求
     */
    TRACE(false, true, true),

    /**
     * CONNECT - Establish tunnel
     * CONNECT - 建立隧道
     */
    CONNECT(false, false, false);

    private final boolean hasRequestBody;
    private final boolean idempotent;
    private final boolean safe;

    HttpMethod(boolean hasRequestBody, boolean idempotent, boolean safe) {
        this.hasRequestBody = hasRequestBody;
        this.idempotent = idempotent;
        this.safe = safe;
    }

    /**
     * Checks if this method typically has a request body.
     * 检查此方法是否通常有请求体。
     *
     * @return true if has body - 如果有请求体返回 true
     */
    public boolean hasRequestBody() {
        return hasRequestBody;
    }

    /**
     * Checks if this method is idempotent (same request produces same result).
     * 检查此方法是否是幂等的（相同请求产生相同结果）。
     *
     * @return true if idempotent - 如果幂等返回 true
     */
    public boolean isIdempotent() {
        return idempotent;
    }

    /**
     * Checks if this method is safe (no side effects).
     * 检查此方法是否安全（无副作用）。
     *
     * @return true if safe - 如果安全返回 true
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Parses method name to enum.
     * 解析方法名称为枚举。
     *
     * @param method the method name - 方法名称
     * @return the HttpMethod - HTTP 方法
     * @throws IllegalArgumentException if method is invalid - 如果方法无效
     */
    public static HttpMethod fromString(String method) {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("HTTP method must not be blank");
        }
        return valueOf(method.toUpperCase());
    }

    /**
     * Checks if the given method name is valid.
     * 检查给定的方法名称是否有效。
     *
     * @param method the method name - 方法名称
     * @return true if valid - 如果有效返回 true
     */
    public static boolean isValid(String method) {
        if (method == null || method.isBlank()) {
            return false;
        }
        try {
            valueOf(method.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
