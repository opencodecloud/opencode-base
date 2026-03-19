
package cloud.opencode.base.json;

import cloud.opencode.base.json.annotation.JsonNaming;
import cloud.opencode.base.json.spi.JsonFeature;

import java.time.ZoneId;
import java.util.*;

/**
 * JSON Config - Configuration for JSON Processing
 * JSON 配置 - JSON 处理的配置
 *
 * <p>This class provides configuration options for JSON serialization
 * and deserialization, including features, naming strategies, and limits.</p>
 * <p>此类提供 JSON 序列化和反序列化的配置选项，
 * 包括特性、命名策略和限制。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonConfig config = JsonConfig.builder()
 *     .enable(JsonFeature.PRETTY_PRINT)
 *     .enable(JsonFeature.IGNORE_UNKNOWN_PROPERTIES)
 *     .namingStrategy(JsonNaming.Strategy.SNAKE_CASE)
 *     .dateFormat("yyyy-MM-dd HH:mm:ss")
 *     .timezone(ZoneId.of("Asia/Shanghai"))
 *     .maxDepth(100)
 *     .build();
 *
 * OpenJson json = OpenJson.withConfig(config);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Builder pattern for flexible configuration - 构建器模式实现灵活配置</li>
 *   <li>Feature toggle for serialization/deserialization - 序列化/反序列化特性开关</li>
 *   <li>Configurable naming strategies, date formats, and limits - 可配置的命名策略、日期格式和限制</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonConfig {

    /**
     * Default configuration
     * 默认配置
     */
    public static final JsonConfig DEFAULT = builder().build();

    /**
     * Enabled features
     * 启用的特性
     */
    private final Set<JsonFeature> enabledFeatures;

    /**
     * Disabled features
     * 禁用的特性
     */
    private final Set<JsonFeature> disabledFeatures;

    /**
     * Naming strategy for property names
     * 属性名的命名策略
     */
    private final JsonNaming.Strategy namingStrategy;

    /**
     * Date/time format pattern
     * 日期/时间格式模式
     */
    private final String dateFormat;

    /**
     * Timezone for date/time handling
     * 日期/时间处理的时区
     */
    private final ZoneId timezone;

    /**
     * Maximum nesting depth
     * 最大嵌套深度
     */
    private final int maxDepth;

    /**
     * Maximum string length
     * 最大字符串长度
     */
    private final int maxStringLength;

    /**
     * Maximum array/object size
     * 最大数组/对象大小
     */
    private final int maxSize;

    /**
     * Indentation string for pretty printing
     * 美化打印的缩进字符串
     */
    private final String indent;

    private JsonConfig(Builder builder) {
        this.enabledFeatures = Set.copyOf(builder.enabledFeatures);
        this.disabledFeatures = Set.copyOf(builder.disabledFeatures);
        this.namingStrategy = builder.namingStrategy;
        this.dateFormat = builder.dateFormat;
        this.timezone = builder.timezone;
        this.maxDepth = builder.maxDepth;
        this.maxStringLength = builder.maxStringLength;
        this.maxSize = builder.maxSize;
        this.indent = builder.indent;
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return a new builder - 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder initialized with this config.
     * 创建使用此配置初始化的构建器。
     *
     * @return a new builder - 新构建器
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Returns whether a feature is enabled.
     * 返回特性是否启用。
     *
     * @param feature the feature to check - 要检查的特性
     * @return true if enabled - 如果启用则返回 true
     */
    public boolean isEnabled(JsonFeature feature) {
        if (enabledFeatures.contains(feature)) {
            return true;
        }
        if (disabledFeatures.contains(feature)) {
            return false;
        }
        return feature.isEnabledByDefault();
    }

    /**
     * Returns the enabled features.
     * 返回启用的特性。
     *
     * @return the enabled features - 启用的特性
     */
    public Set<JsonFeature> getEnabledFeatures() {
        return enabledFeatures;
    }

    /**
     * Returns the naming strategy.
     * 返回命名策略。
     *
     * @return the naming strategy - 命名策略
     */
    public JsonNaming.Strategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * Returns the date format pattern.
     * 返回日期格式模式。
     *
     * @return the date format - 日期格式
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Returns the timezone.
     * 返回时区。
     *
     * @return the timezone - 时区
     */
    public ZoneId getTimezone() {
        return timezone;
    }

    /**
     * Returns the maximum nesting depth.
     * 返回最大嵌套深度。
     *
     * @return the max depth - 最大深度
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Returns the maximum string length.
     * 返回最大字符串长度。
     *
     * @return the max string length - 最大字符串长度
     */
    public int getMaxStringLength() {
        return maxStringLength;
    }

    /**
     * Returns the maximum array/object size.
     * 返回最大数组/对象大小。
     *
     * @return the max size - 最大大小
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Returns the indentation string.
     * 返回缩进字符串。
     *
     * @return the indent - 缩进
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Builder for JsonConfig.
     * JsonConfig 的构建器。
     */
    public static final class Builder {
        private final Set<JsonFeature> enabledFeatures = EnumSet.noneOf(JsonFeature.class);
        private final Set<JsonFeature> disabledFeatures = EnumSet.noneOf(JsonFeature.class);
        private JsonNaming.Strategy namingStrategy = JsonNaming.Strategy.IDENTITY;
        private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
        private ZoneId timezone = ZoneId.systemDefault();
        private int maxDepth = 1000;
        private int maxStringLength = 20_000_000;
        private int maxSize = 100_000;
        private String indent = "  ";

        private Builder() {}

        private Builder(JsonConfig config) {
            this.enabledFeatures.addAll(config.enabledFeatures);
            this.disabledFeatures.addAll(config.disabledFeatures);
            this.namingStrategy = config.namingStrategy;
            this.dateFormat = config.dateFormat;
            this.timezone = config.timezone;
            this.maxDepth = config.maxDepth;
            this.maxStringLength = config.maxStringLength;
            this.maxSize = config.maxSize;
            this.indent = config.indent;
        }

        /**
         * Enables a feature.
         * 启用特性。
         *
         * @param feature the feature to enable - 要启用的特性
         * @return this builder - 此构建器
         */
        public Builder enable(JsonFeature feature) {
            enabledFeatures.add(feature);
            disabledFeatures.remove(feature);
            return this;
        }

        /**
         * Enables multiple features.
         * 启用多个特性。
         *
         * @param features the features to enable - 要启用的特性
         * @return this builder - 此构建器
         */
        public Builder enable(JsonFeature... features) {
            for (JsonFeature feature : features) {
                enable(feature);
            }
            return this;
        }

        /**
         * Disables a feature.
         * 禁用特性。
         *
         * @param feature the feature to disable - 要禁用的特性
         * @return this builder - 此构建器
         */
        public Builder disable(JsonFeature feature) {
            disabledFeatures.add(feature);
            enabledFeatures.remove(feature);
            return this;
        }

        /**
         * Disables multiple features.
         * 禁用多个特性。
         *
         * @param features the features to disable - 要禁用的特性
         * @return this builder - 此构建器
         */
        public Builder disable(JsonFeature... features) {
            for (JsonFeature feature : features) {
                disable(feature);
            }
            return this;
        }

        /**
         * Sets the naming strategy.
         * 设置命名策略。
         *
         * @param strategy the naming strategy - 命名策略
         * @return this builder - 此构建器
         */
        public Builder namingStrategy(JsonNaming.Strategy strategy) {
            this.namingStrategy = Objects.requireNonNull(strategy);
            return this;
        }

        /**
         * Sets the date format pattern.
         * 设置日期格式模式。
         *
         * @param format the date format - 日期格式
         * @return this builder - 此构建器
         */
        public Builder dateFormat(String format) {
            this.dateFormat = Objects.requireNonNull(format);
            return this;
        }

        /**
         * Sets the timezone.
         * 设置时区。
         *
         * @param timezone the timezone - 时区
         * @return this builder - 此构建器
         */
        public Builder timezone(ZoneId timezone) {
            this.timezone = Objects.requireNonNull(timezone);
            return this;
        }

        /**
         * Sets the timezone by ID.
         * 按 ID 设置时区。
         *
         * @param timezoneId the timezone ID - 时区 ID
         * @return this builder - 此构建器
         */
        public Builder timezone(String timezoneId) {
            this.timezone = ZoneId.of(timezoneId);
            return this;
        }

        /**
         * Sets the maximum nesting depth.
         * 设置最大嵌套深度。
         *
         * @param maxDepth the max depth - 最大深度
         * @return this builder - 此构建器
         */
        public Builder maxDepth(int maxDepth) {
            if (maxDepth < 1) {
                throw new IllegalArgumentException("maxDepth must be positive");
            }
            this.maxDepth = maxDepth;
            return this;
        }

        /**
         * Sets the maximum string length.
         * 设置最大字符串长度。
         *
         * @param maxStringLength the max string length - 最大字符串长度
         * @return this builder - 此构建器
         */
        public Builder maxStringLength(int maxStringLength) {
            if (maxStringLength < 1) {
                throw new IllegalArgumentException("maxStringLength must be positive");
            }
            this.maxStringLength = maxStringLength;
            return this;
        }

        /**
         * Sets the maximum array/object size.
         * 设置最大数组/对象大小。
         *
         * @param maxSize the max size - 最大大小
         * @return this builder - 此构建器
         */
        public Builder maxSize(int maxSize) {
            if (maxSize < 1) {
                throw new IllegalArgumentException("maxSize must be positive");
            }
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Sets the indentation string.
         * 设置缩进字符串。
         *
         * @param indent the indent - 缩进
         * @return this builder - 此构建器
         */
        public Builder indent(String indent) {
            this.indent = Objects.requireNonNull(indent);
            return this;
        }

        /**
         * Enables pretty printing.
         * 启用美化打印。
         *
         * @return this builder - 此构建器
         */
        public Builder prettyPrint() {
            return enable(JsonFeature.PRETTY_PRINT);
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the built config - 构建的配置
         */
        public JsonConfig build() {
            return new JsonConfig(this);
        }
    }
}
