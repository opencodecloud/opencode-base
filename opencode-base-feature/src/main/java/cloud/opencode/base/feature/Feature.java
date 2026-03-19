package cloud.opencode.base.feature;

import cloud.opencode.base.feature.strategy.AlwaysOffStrategy;
import cloud.opencode.base.feature.strategy.AlwaysOnStrategy;
import cloud.opencode.base.feature.strategy.EnableStrategy;
import cloud.opencode.base.feature.strategy.PercentageStrategy;
import cloud.opencode.base.feature.strategy.UserListStrategy;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Feature Definition
 * 功能定义
 *
 * <p>Immutable record representing a feature toggle.</p>
 * <p>表示功能开关的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Feature key and metadata - 功能键和元数据</li>
 *   <li>Enable strategy - 启用策略</li>
 *   <li>Default value - 默认值</li>
 *   <li>Timestamps - 时间戳</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple feature
 * Feature feature = Feature.builder("dark-mode")
 *     .name("Dark Mode")
 *     .description("Enable dark theme")
 *     .alwaysOn()
 *     .build();
 *
 * // Percentage rollout
 * Feature feature = Feature.builder("new-feature")
 *     .percentage(10)
 *     .build();
 *
 * // User whitelist
 * Feature feature = Feature.builder("beta-feature")
 *     .forUsers("user1", "user2")
 *     .build();
 * }</pre>
 *
 * @param key            the unique feature key | 唯一功能键
 * @param name           the display name | 显示名称
 * @param description    the description | 描述
 * @param defaultEnabled default enabled state | 默认启用状态
 * @param strategy       the enable strategy | 启用策略
 * @param metadata       custom metadata | 自定义元数据
 * @param createdAt      creation timestamp | 创建时间戳
 * @param updatedAt      update timestamp | 更新时间戳
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
public record Feature(
    String key,
    String name,
    String description,
    boolean defaultEnabled,
    EnableStrategy strategy,
    Map<String, Object> metadata,
    Instant createdAt,
    Instant updatedAt
) {

    /**
     * Create a builder for feature
     * 创建功能构建器
     *
     * @param key the feature key | 功能键
     * @return new builder | 新的构建器
     */
    public static Builder builder(String key) {
        return new Builder(key);
    }

    /**
     * Check if this feature is enabled with empty context
     * 使用空上下文检查此功能是否启用
     *
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled() {
        return isEnabled(FeatureContext.empty());
    }

    /**
     * Check if this feature is enabled with context
     * 使用上下文检查此功能是否启用
     *
     * @param context the evaluation context | 评估上下文
     * @return true if enabled | 如果启用返回true
     */
    public boolean isEnabled(FeatureContext context) {
        if (strategy == null) {
            return defaultEnabled;
        }
        return strategy.isEnabled(this, context);
    }

    /**
     * Get metadata value
     * 获取元数据值
     *
     * @param key the metadata key | 元数据键
     * @param <T> the value type | 值类型
     * @return the metadata value or null | 元数据值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return metadata != null ? (T) metadata.get(key) : null;
    }

    /**
     * Get metadata value with default
     * 获取元数据值，带默认值
     *
     * @param key          the metadata key | 元数据键
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the metadata value or default | 元数据值或默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, T defaultValue) {
        if (metadata == null) {
            return defaultValue;
        }
        Object value = metadata.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Create a copy with updated strategy
     * 创建具有更新策略的副本
     *
     * @param newStrategy the new strategy | 新策略
     * @return new Feature with updated strategy | 具有更新策略的新Feature
     */
    public Feature withStrategy(EnableStrategy newStrategy) {
        return new Feature(key, name, description, defaultEnabled,
            newStrategy, metadata, createdAt, Instant.now());
    }

    /**
     * Builder for Feature
     * Feature构建器
     */
    public static class Builder {
        private final String key;
        private String name;
        private String description;
        private boolean defaultEnabled = false;
        private EnableStrategy strategy = null;  // null means use defaultEnabled
        private final Map<String, Object> metadata = new HashMap<>();

        /**
         * Create builder with key
         * 使用键创建构建器
         *
         * @param key the feature key | 功能键
         */
        public Builder(String key) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Feature key cannot be null or blank");
            }
            this.key = key;
            this.name = key;
        }

        /**
         * Set display name
         * 设置显示名称
         *
         * @param name the display name | 显示名称
         * @return this builder | 此构建器
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set description
         * 设置描述
         *
         * @param description the description | 描述
         * @return this builder | 此构建器
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Set default enabled state
         * 设置默认启用状态
         *
         * @param enabled the default state | 默认状态
         * @return this builder | 此构建器
         */
        public Builder defaultEnabled(boolean enabled) {
            this.defaultEnabled = enabled;
            return this;
        }

        /**
         * Set enable strategy
         * 设置启用策略
         *
         * @param strategy the strategy | 策略
         * @return this builder | 此构建器
         */
        public Builder strategy(EnableStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * Set always on strategy
         * 设置始终启用策略
         *
         * @return this builder | 此构建器
         */
        public Builder alwaysOn() {
            return strategy(AlwaysOnStrategy.INSTANCE);
        }

        /**
         * Set always off strategy
         * 设置始终禁用策略
         *
         * @return this builder | 此构建器
         */
        public Builder alwaysOff() {
            return strategy(AlwaysOffStrategy.INSTANCE);
        }

        /**
         * Set percentage strategy
         * 设置百分比策略
         *
         * @param percent the percentage (0-100) | 百分比 (0-100)
         * @return this builder | 此构建器
         */
        public Builder percentage(int percent) {
            return strategy(new PercentageStrategy(percent));
        }

        /**
         * Set user list strategy
         * 设置用户列表策略
         *
         * @param userIds the allowed user IDs | 允许的用户ID
         * @return this builder | 此构建器
         */
        public Builder forUsers(String... userIds) {
            return strategy(new UserListStrategy(Set.of(userIds)));
        }

        /**
         * Set user list strategy
         * 设置用户列表策略
         *
         * @param userIds the allowed user IDs | 允许的用户ID
         * @return this builder | 此构建器
         */
        public Builder forUsers(Set<String> userIds) {
            return strategy(new UserListStrategy(userIds));
        }

        /**
         * Add metadata entry
         * 添加元数据条目
         *
         * @param key   the metadata key | 元数据键
         * @param value the metadata value | 元数据值
         * @return this builder | 此构建器
         */
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        /**
         * Add all metadata from map
         * 从映射添加所有元数据
         *
         * @param metadata the metadata map | 元数据映射
         * @return this builder | 此构建器
         */
        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        /**
         * Build the feature
         * 构建功能
         *
         * @return new Feature | 新的Feature
         */
        public Feature build() {
            Instant now = Instant.now();
            return new Feature(key, name, description, defaultEnabled,
                strategy, Map.copyOf(metadata), now, now);
        }
    }
}
