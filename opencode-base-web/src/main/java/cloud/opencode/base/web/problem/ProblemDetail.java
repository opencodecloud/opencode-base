package cloud.opencode.base.web.problem;

import cloud.opencode.base.web.exception.OpenWebException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RFC 9457 Problem Details for HTTP APIs
 * RFC 9457 HTTP API 问题详情
 *
 * <p>A machine-readable format for conveying error details in HTTP response bodies,
 * as defined by <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457</a>.</p>
 * <p>一种机器可读的格式，用于在 HTTP 响应体中传递错误详情，
 * 由 <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457</a> 定义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 9457 compliant problem details - 符合 RFC 9457 的问题详情</li>
 *   <li>Builder pattern for flexible construction - 构建器模式灵活构造</li>
 *   <li>Factory methods for common use cases - 常见场景的工厂方法</li>
 *   <li>Extension properties support - 扩展属性支持</li>
 *   <li>OpenWebException integration - OpenWebException 集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Quick creation
 * ProblemDetail problem = ProblemDetail.of(404, "User not found");
 *
 * // With builder
 * ProblemDetail problem = ProblemDetail.builder()
 *     .type("https://example.com/not-found")
 *     .title("Not Found")
 *     .status(404)
 *     .detail("User with ID 42 was not found")
 *     .instance("/users/42")
 *     .extension("userId", 42)
 *     .build();
 *
 * // From exception
 * ProblemDetail problem = ProblemDetail.fromException(openWebException);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (null fields treated as absent) - 空值安全: 是（null 字段视为缺失）</li>
 * </ul>
 *
 * @param type       the problem type URI, defaults to "about:blank" | 问题类型 URI，默认 "about:blank"
 * @param title      the human-readable summary | 人类可读的摘要
 * @param status     the HTTP status code | HTTP 状态码
 * @param detail     the human-readable explanation | 人类可读的详细说明
 * @param instance   the problem occurrence URI | 问题实例 URI
 * @param extensions the extension members | 扩展属性
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public record ProblemDetail(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        Map<String, Object> extensions
) {

    /**
     * Content-Type for problem details responses.
     * 问题详情响应的 Content-Type。
     */
    public static final String CONTENT_TYPE = "application/problem+json";

    /**
     * Default problem type URI as defined in RFC 9457.
     * RFC 9457 定义的默认问题类型 URI。
     */
    public static final String DEFAULT_TYPE = "about:blank";

    // ==================== HTTP Status Title Mapping ====================

    private static final Map<Integer, String> STATUS_TITLES;

    static {
        var titles = new LinkedHashMap<Integer, String>();
        titles.put(400, "Bad Request");
        titles.put(401, "Unauthorized");
        titles.put(403, "Forbidden");
        titles.put(404, "Not Found");
        titles.put(405, "Method Not Allowed");
        titles.put(409, "Conflict");
        titles.put(422, "Unprocessable Entity");
        titles.put(429, "Too Many Requests");
        titles.put(500, "Internal Server Error");
        titles.put(502, "Bad Gateway");
        titles.put(503, "Service Unavailable");
        titles.put(504, "Gateway Timeout");
        STATUS_TITLES = Collections.unmodifiableMap(titles);
    }

    // ==================== Compact Constructor ====================

    /**
     * Compact constructor: defensively copies extensions to ensure immutability.
     * 紧凑构造器：防御性复制扩展属性以确保不可变性。
     */
    public ProblemDetail {
        if (type == null || type.isBlank()) {
            type = DEFAULT_TYPE;
        }
        extensions = (extensions == null || extensions.isEmpty())
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
    }

    // ==================== Factory Methods ====================

    /**
     * Create a ProblemDetail with status, title, and detail.
     * 使用状态码、标题和详细说明创建 ProblemDetail。
     *
     * @param status the HTTP status code | HTTP 状态码
     * @param title  the title | 标题
     * @param detail the detail | 详细说明
     * @return the problem detail | 问题详情
     */
    public static ProblemDetail of(int status, String title, String detail) {
        return new ProblemDetail(DEFAULT_TYPE, title, status, detail, null, null);
    }

    /**
     * Create a ProblemDetail with status and detail; title is auto-derived from status.
     * 使用状态码和详细说明创建 ProblemDetail；标题从状态码自动推导。
     *
     * @param status the HTTP status code | HTTP 状态码
     * @param detail the detail | 详细说明
     * @return the problem detail | 问题详情
     */
    public static ProblemDetail of(int status, String detail) {
        return new ProblemDetail(DEFAULT_TYPE, titleForStatus(status), status, detail, null, null);
    }

    /**
     * Create a ProblemDetail from an OpenWebException.
     * 从 OpenWebException 创建 ProblemDetail。
     *
     * @param e the exception | 异常
     * @return the problem detail | 问题详情
     * @throws NullPointerException if e is null | 如果 e 为 null
     */
    public static ProblemDetail fromException(OpenWebException e) {
        return fromException(e, false);
    }

    /**
     * Create a ProblemDetail from an OpenWebException, optionally including the internal error code.
     * 从 OpenWebException 创建 ProblemDetail，可选择是否包含内部错误码。
     *
     * @param e the exception | 异常
     * @param includeCode whether to include the internal error code in extensions | 是否在扩展中包含内部错误码
     * @return the problem detail | 问题详情
     * @throws NullPointerException if e is null | 如果 e 为 null
     */
    public static ProblemDetail fromException(OpenWebException e, boolean includeCode) {
        Objects.requireNonNull(e, "exception must not be null");
        int httpStatus = e.getHttpStatus();
        Map<String, Object> ext = includeCode ? Map.of("code", e.getCode()) : Map.of();
        return new ProblemDetail(
                DEFAULT_TYPE,
                titleForStatus(httpStatus),
                httpStatus,
                e.getMessage(),
                null,
                ext
        );
    }

    /**
     * Create a new Builder.
     * 创建新的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Instance Methods ====================

    /**
     * Check whether this problem has extension properties.
     * 检查是否包含扩展属性。
     *
     * @return true if extensions are present | 如果存在扩展属性返回 true
     */
    public boolean hasExtensions() {
        return !extensions.isEmpty();
    }

    /**
     * Get the content type for problem detail responses.
     * 获取问题详情响应的 Content-Type。
     *
     * @return "application/problem+json"
     */
    public String getContentType() {
        return CONTENT_TYPE;
    }

    // ==================== Helper ====================

    /**
     * Resolve a human-readable title for an HTTP status code.
     * 根据 HTTP 状态码解析人类可读的标题。
     *
     * @param status the HTTP status code | HTTP 状态码
     * @return the title | 标题
     */
    static String titleForStatus(int status) {
        return STATUS_TITLES.getOrDefault(status, "Unknown Error");
    }

    // ==================== Builder ====================

    /**
     * Builder for ProblemDetail
     * ProblemDetail 构建器
     *
     * @since JDK 25, opencode-base-web V1.0.3
     */
    public static final class Builder {

        private String type = DEFAULT_TYPE;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private final Map<String, Object> extensions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Set the problem type URI.
         * 设置问题类型 URI。
         *
         * @param type the type URI | 类型 URI
         * @return this builder | 此构建器
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Set the title.
         * 设置标题。
         *
         * @param title the title | 标题
         * @return this builder | 此构建器
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the HTTP status code.
         * 设置 HTTP 状态码。
         *
         * @param status the status code | 状态码
         * @return this builder | 此构建器
         */
        public Builder status(int status) {
            this.status = status;
            return this;
        }

        /**
         * Set the detail message.
         * 设置详细说明。
         *
         * @param detail the detail | 详细说明
         * @return this builder | 此构建器
         */
        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        /**
         * Set the instance URI.
         * 设置实例 URI。
         *
         * @param instance the instance URI | 实例 URI
         * @return this builder | 此构建器
         */
        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        /**
         * Add an extension property.
         * 添加扩展属性。
         *
         * @param key   the extension key | 扩展键
         * @param value the extension value | 扩展值
         * @return this builder | 此构建器
         * @throws NullPointerException if key is null | 如果键为 null
         */
        public Builder extension(String key, Object value) {
            Objects.requireNonNull(key, "extension key must not be null");
            this.extensions.put(key, value);
            return this;
        }

        /**
         * Build the ProblemDetail.
         * 构建 ProblemDetail。
         *
         * @return the problem detail | 问题详情
         */
        public ProblemDetail build() {
            return new ProblemDetail(type, title, status, detail, instance, extensions);
        }
    }
}
