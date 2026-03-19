package cloud.opencode.base.web.http;

/**
 * HTTP Status - HTTP Response Status Code Enumeration
 * HTTP 状态 - HTTP 响应状态码枚举
 *
 * <p>This enum defines standard HTTP status codes as specified in RFC 7231.</p>
 * <p>此枚举定义了 RFC 7231 中规定的标准 HTTP 状态码。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * HttpStatus status = HttpStatus.OK;
 * int code = status.getCode();
 * String reason = status.getReason();
 * boolean success = status.isSuccess();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard HTTP status code enumeration - 标准HTTP状态码枚举</li>
 *   <li>Status category methods (1xx-5xx) - 状态类别方法</li>
 *   <li>Lookup by status code - 按状态码查找</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpStatus status = HttpStatus.OK;
 * boolean success = status.isSuccess();  // true
 * boolean error = status.isError();      // false
 * HttpStatus found = HttpStatus.fromCode(404);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 是（枚举是不可变的）</li>
 *   <li>Null-safe: No (valueOf throws for unknown codes) - 否（valueOf对未知代码抛出异常）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public enum HttpStatus {

    // ==================== 1xx Informational ====================

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    PROCESSING(102, "Processing"),
    EARLY_HINTS(103, "Early Hints"),

    // ==================== 2xx Success ====================

    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTI_STATUS(207, "Multi-Status"),
    ALREADY_REPORTED(208, "Already Reported"),
    IM_USED(226, "IM Used"),

    // ==================== 3xx Redirection ====================

    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    // ==================== 4xx Client Error ====================

    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
    URI_TOO_LONG(414, "URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    IM_A_TEAPOT(418, "I'm a teapot"),
    MISDIRECTED_REQUEST(421, "Misdirected Request"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    LOCKED(423, "Locked"),
    FAILED_DEPENDENCY(424, "Failed Dependency"),
    TOO_EARLY(425, "Too Early"),
    UPGRADE_REQUIRED(426, "Upgrade Required"),
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    // ==================== 5xx Server Error ====================

    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),
    LOOP_DETECTED(508, "Loop Detected"),
    NOT_EXTENDED(510, "Not Extended"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private final int code;
    private final String reason;

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Gets the status code.
     * 获取状态码。
     *
     * @return the code - 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the reason phrase.
     * 获取原因短语。
     *
     * @return the reason - 原因短语
     */
    public String getReason() {
        return reason;
    }

    /**
     * Checks if this is an informational response (1xx).
     * 检查是否是信息响应（1xx）。
     *
     * @return true if informational - 如果是信息响应返回 true
     */
    public boolean isInformational() {
        return code >= 100 && code < 200;
    }

    /**
     * Checks if this is a successful response (2xx).
     * 检查是否是成功响应（2xx）。
     *
     * @return true if success - 如果成功返回 true
     */
    public boolean isSuccess() {
        return code >= 200 && code < 300;
    }

    /**
     * Checks if this is a redirection response (3xx).
     * 检查是否是重定向响应（3xx）。
     *
     * @return true if redirection - 如果是重定向返回 true
     */
    public boolean isRedirection() {
        return code >= 300 && code < 400;
    }

    /**
     * Checks if this is a client error response (4xx).
     * 检查是否是客户端错误响应（4xx）。
     *
     * @return true if client error - 如果是客户端错误返回 true
     */
    public boolean isClientError() {
        return code >= 400 && code < 500;
    }

    /**
     * Checks if this is a server error response (5xx).
     * 检查是否是服务器错误响应（5xx）。
     *
     * @return true if server error - 如果是服务器错误返回 true
     */
    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    /**
     * Checks if this is an error response (4xx or 5xx).
     * 检查是否是错误响应（4xx 或 5xx）。
     *
     * @return true if error - 如果是错误返回 true
     */
    public boolean isError() {
        return code >= 400;
    }

    /**
     * Gets HttpStatus from status code.
     * 从状态码获取 HttpStatus。
     *
     * @param code the status code - 状态码
     * @return the HttpStatus or null if not found - HttpStatus 或如果未找到返回 null
     */
    public static HttpStatus fromCode(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * Gets HttpStatus from status code, throwing exception if not found.
     * 从状态码获取 HttpStatus，如果未找到则抛出异常。
     *
     * @param code the status code - 状态码
     * @return the HttpStatus - HttpStatus
     * @throws IllegalArgumentException if code is not valid - 如果状态码无效
     */
    public static HttpStatus valueOf(int code) {
        HttpStatus status = fromCode(code);
        if (status == null) {
            throw new IllegalArgumentException("Unknown HTTP status code: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return code + " " + reason;
    }
}
