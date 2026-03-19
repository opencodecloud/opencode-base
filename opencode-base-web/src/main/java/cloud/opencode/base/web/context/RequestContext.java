package cloud.opencode.base.web.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Request Context
 * 请求上下文
 *
 * <p>Holds request-scoped information for the current request.</p>
 * <p>保存当前请求范围内的信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable request-scoped data record - 不可变的请求范围数据记录</li>
 *   <li>Trace ID and request ID tracking - 追踪 ID 和请求 ID 跟踪</li>
 *   <li>User context association - 用户上下文关联</li>
 *   <li>Builder pattern for flexible construction - 构建器模式支持灵活构建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create with builder
 * RequestContext ctx = RequestContext.builder()
 *     .traceId("abc123")
 *     .clientIp("192.168.1.1")
 *     .user(UserContext.of("1", "john"))
 *     .build();
 *
 * // Simple creation
 * RequestContext ctx = RequestContext.of("trace-id-123");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (traceId and clientIp can be null) - 空值安全: 部分（traceId 和 clientIp 可为 null）</li>
 * </ul>
 *
 * @param traceId the trace ID | 追踪ID
 * @param requestId the request ID | 请求ID
 * @param requestTime the request time | 请求时间
 * @param clientIp the client IP | 客户端IP
 * @param userAgent the user agent | 用户代理
 * @param locale the locale | 区域设置
 * @param user the user context | 用户上下文
 * @param attributes additional attributes | 附加属性
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record RequestContext(
    String traceId,
    String requestId,
    Instant requestTime,
    String clientIp,
    String userAgent,
    Locale locale,
    UserContext user,
    Map<String, Object> attributes
) {

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public RequestContext {
        requestTime = requestTime != null ? requestTime : Instant.now();
        locale = locale != null ? locale : Locale.getDefault();
        user = user != null ? user : UserContext.ANONYMOUS;
        attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
    }

    /**
     * Create request context builder
     * 创建请求上下文构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create simple request context
     * 创建简单请求上下文
     *
     * @param traceId the trace ID | 追踪ID
     * @return the request context | 请求上下文
     */
    public static RequestContext of(String traceId) {
        return builder().traceId(traceId).build();
    }

    /**
     * Get attribute
     * 获取属性
     *
     * @param key the attribute key | 属性键
     * @param <T> the attribute type | 属性类型
     * @return the attribute value or null | 属性值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Get attribute with default value
     * 获取属性（带默认值）
     *
     * @param key the attribute key | 属性键
     * @param defaultValue the default value | 默认值
     * @param <T> the attribute type | 属性类型
     * @return the attribute value or default | 属性值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Check if user is authenticated
     * 检查用户是否已认证
     *
     * @return true if authenticated | 如果已认证返回true
     */
    public boolean isAuthenticated() {
        return user != null && user.isAuthenticated();
    }

    /**
     * Get user ID
     * 获取用户ID
     *
     * @return the user ID or null | 用户ID或null
     */
    public String getUserId() {
        return user != null ? user.userId() : null;
    }

    /**
     * Get username
     * 获取用户名
     *
     * @return the username or null | 用户名或null
     */
    public String getUsername() {
        return user != null ? user.username() : null;
    }

    /**
     * Request Context Builder
     * 请求上下文构建器
     */
    public static final class Builder {
        private String traceId;
        private String requestId;
        private Instant requestTime;
        private String clientIp;
        private String userAgent;
        private Locale locale;
        private UserContext user;
        private final Map<String, Object> attributes = new HashMap<>();

        private Builder() {}

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder requestTime(Instant requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder user(UserContext user) {
            this.user = user;
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public RequestContext build() {
            return new RequestContext(
                traceId, requestId, requestTime, clientIp,
                userAgent, locale, user, attributes
            );
        }
    }
}
