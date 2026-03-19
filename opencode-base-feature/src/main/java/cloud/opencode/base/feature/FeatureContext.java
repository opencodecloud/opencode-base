package cloud.opencode.base.feature;

import java.util.HashMap;
import java.util.Map;

/**
 * Feature Context
 * 功能上下文
 *
 * <p>Immutable context for feature evaluation.</p>
 * <p>用于功能评估的不可变上下文。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>User context - 用户上下文</li>
 *   <li>Tenant context - 租户上下文</li>
 *   <li>Custom attributes - 自定义属性</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Empty context
 * FeatureContext context = FeatureContext.empty();
 *
 * // User context
 * FeatureContext context = FeatureContext.ofUser("user-123");
 *
 * // Full context with builder
 * FeatureContext context = FeatureContext.builder()
 *     .userId("user-123")
 *     .tenantId("tenant-456")
 *     .attribute("role", "admin")
 *     .build();
 * }</pre>
 *
 * @param userId     the user ID | 用户ID
 * @param tenantId   the tenant ID | 租户ID
 * @param attributes custom attributes map | 自定义属性映射
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
public record FeatureContext(
    String userId,
    String tenantId,
    Map<String, Object> attributes
) {

    /**
     * Create an empty context
     * 创建空上下文
     *
     * @return empty context | 空上下文
     */
    public static FeatureContext empty() {
        return new FeatureContext(null, null, Map.of());
    }

    /**
     * Create a context with user ID only
     * 仅使用用户ID创建上下文
     *
     * @param userId the user ID | 用户ID
     * @return context with user | 带用户的上下文
     */
    public static FeatureContext ofUser(String userId) {
        return new FeatureContext(userId, null, Map.of());
    }

    /**
     * Create a context with tenant ID only
     * 仅使用租户ID创建上下文
     *
     * @param tenantId the tenant ID | 租户ID
     * @return context with tenant | 带租户的上下文
     */
    public static FeatureContext ofTenant(String tenantId) {
        return new FeatureContext(null, tenantId, Map.of());
    }

    /**
     * Create a context with user and tenant
     * 使用用户和租户创建上下文
     *
     * @param userId   the user ID | 用户ID
     * @param tenantId the tenant ID | 租户ID
     * @return context | 上下文
     */
    public static FeatureContext of(String userId, String tenantId) {
        return new FeatureContext(userId, tenantId, Map.of());
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get an attribute value
     * 获取属性值
     *
     * @param key the attribute key | 属性键
     * @param <T> the value type | 值类型
     * @return the attribute value or null | 属性值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Get an attribute value with default
     * 获取属性值，带默认值
     *
     * @param key          the attribute key | 属性键
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the attribute value or default | 属性值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Check if context has user ID
     * 检查上下文是否有用户ID
     *
     * @return true if has user ID | 如果有用户ID返回true
     */
    public boolean hasUserId() {
        return userId != null && !userId.isEmpty();
    }

    /**
     * Check if context has tenant ID
     * 检查上下文是否有租户ID
     *
     * @return true if has tenant ID | 如果有租户ID返回true
     */
    public boolean hasTenantId() {
        return tenantId != null && !tenantId.isEmpty();
    }

    /**
     * Builder for FeatureContext
     * FeatureContext构建器
     */
    public static class Builder {
        private String userId;
        private String tenantId;
        private final Map<String, Object> attributes = new HashMap<>();

        /**
         * Set user ID
         * 设置用户ID
         *
         * @param userId the user ID | 用户ID
         * @return this builder | 此构建器
         */
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Set tenant ID
         * 设置租户ID
         *
         * @param tenantId the tenant ID | 租户ID
         * @return this builder | 此构建器
         */
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        /**
         * Add an attribute
         * 添加属性
         *
         * @param key   the attribute key | 属性键
         * @param value the attribute value | 属性值
         * @return this builder | 此构建器
         */
        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        /**
         * Add all attributes from a map
         * 从映射添加所有属性
         *
         * @param attributes the attributes map | 属性映射
         * @return this builder | 此构建器
         */
        public Builder attributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        /**
         * Build the context
         * 构建上下文
         *
         * @return new FeatureContext | 新的FeatureContext
         */
        public FeatureContext build() {
            return new FeatureContext(userId, tenantId, Map.copyOf(attributes));
        }
    }
}
