package cloud.opencode.base.yml;

/**
 * YAML Configuration - Configuration options for YAML processing
 * YAML 配置 - YAML 处理的配置选项
 *
 * <p>This class provides configuration options for YAML parsing and dumping.</p>
 * <p>此类提供 YAML 解析和输出的配置选项。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable indent size and flow/scalar styles - 可配置缩进大小和流/标量风格</li>
 *   <li>Safe mode with alias, depth, and size limits - 带别名、深度和大小限制的安全模式</li>
 *   <li>Duplicate key policy control - 重复键策略控制</li>
 *   <li>Builder pattern for flexible construction - 构建器模式实现灵活构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create custom configuration
 * YmlConfig config = YmlConfig.builder()
 *     .indent(4)
 *     .prettyPrint(true)
 *     .safeMode(true)
 *     .build();
 *
 * // Use defaults
 * YmlConfig config = YmlConfig.defaults();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: Yes (builder uses safe defaults) - 空值安全: 是（构建器使用安全默认值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlConfig {

    private final int indent;
    private final boolean prettyPrint;
    private final boolean allowDuplicateKeys;
    private final boolean safeMode;
    private final int maxAliasesForCollections;
    private final int maxNestingDepth;
    private final long maxDocumentSize;
    private final FlowStyle defaultFlowStyle;
    private final ScalarStyle defaultScalarStyle;
    private final boolean strictTypes;

    private YmlConfig(Builder builder) {
        this.indent = builder.indent;
        this.prettyPrint = builder.prettyPrint;
        this.allowDuplicateKeys = builder.allowDuplicateKeys;
        this.safeMode = builder.safeMode;
        this.maxAliasesForCollections = builder.maxAliasesForCollections;
        this.maxNestingDepth = builder.maxNestingDepth;
        this.maxDocumentSize = builder.maxDocumentSize;
        this.defaultFlowStyle = builder.defaultFlowStyle;
        this.defaultScalarStyle = builder.defaultScalarStyle;
        this.strictTypes = builder.strictTypes;
    }

    /**
     * Creates the default configuration.
     * 创建默认配置。
     *
     * @return the default configuration | 默认配置
     */
    public static YmlConfig defaults() {
        return builder().build();
    }

    /**
     * Creates a configuration builder.
     * 创建配置构建器。
     *
     * @return a new builder | 新构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the indent size.
     * 获取缩进大小。
     *
     * @return the indent spaces | 缩进空格数
     */
    public int getIndent() {
        return indent;
    }

    /**
     * Checks if pretty print is enabled.
     * 检查是否启用美化打印。
     *
     * @return true if enabled | 如果启用则返回 true
     */
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    /**
     * Checks if duplicate keys are allowed.
     * 检查是否允许重复键。
     *
     * @return true if allowed | 如果允许则返回 true
     */
    public boolean isAllowDuplicateKeys() {
        return allowDuplicateKeys;
    }

    /**
     * Checks if safe mode is enabled.
     * 检查是否启用安全模式。
     *
     * @return true if enabled | 如果启用则返回 true
     */
    public boolean isSafeMode() {
        return safeMode;
    }

    /**
     * Gets the maximum number of aliases for collections.
     * 获取集合的最大别名数。
     *
     * @return the maximum aliases | 最大别名数
     */
    public int getMaxAliasesForCollections() {
        return maxAliasesForCollections;
    }

    /**
     * Gets the maximum nesting depth.
     * 获取最大嵌套深度。
     *
     * @return the maximum depth | 最大深度
     */
    public int getMaxNestingDepth() {
        return maxNestingDepth;
    }

    /**
     * Gets the maximum document size in bytes.
     * 获取最大文档大小（字节）。
     *
     * @return the maximum size | 最大大小
     */
    public long getMaxDocumentSize() {
        return maxDocumentSize;
    }

    /**
     * Gets the default flow style.
     * 获取默认流风格。
     *
     * @return the flow style | 流风格
     */
    public FlowStyle getDefaultFlowStyle() {
        return defaultFlowStyle;
    }

    /**
     * Gets the default scalar style.
     * 获取默认标量风格。
     *
     * @return the scalar style | 标量风格
     */
    public ScalarStyle getDefaultScalarStyle() {
        return defaultScalarStyle;
    }

    /**
     * Checks if strict types mode is enabled.
     * 检查是否启用严格类型模式。
     *
     * <p>When enabled, YAML 1.1 boolean values (YES/NO/ON/OFF/y/n/Y/N) are
     * treated as strings instead of being converted to booleans.</p>
     * <p>启用时，YAML 1.1 布尔值（YES/NO/ON/OFF/y/n/Y/N）将被视为字符串，
     * 而不会被转换为布尔值。</p>
     *
     * @return true if strict types is enabled | 如果启用严格类型模式则返回 true
     */
    public boolean isStrictTypes() {
        return strictTypes;
    }

    /**
     * Flow Style - YAML output flow style
     * 流风格 - YAML 输出流风格
     */
    public enum FlowStyle {
        /**
         * Flow style: {key: value}
         * 流式风格：{key: value}
         */
        FLOW,

        /**
         * Block style with line breaks
         * 带换行的块风格
         */
        BLOCK,

        /**
         * Automatic selection
         * 自动选择
         */
        AUTO
    }

    /**
     * Scalar Style - YAML scalar output style
     * 标量风格 - YAML 标量输出风格
     */
    public enum ScalarStyle {
        /**
         * Plain (unquoted)
         * 普通（无引号）
         */
        PLAIN,

        /**
         * Single quoted
         * 单引号
         */
        SINGLE_QUOTED,

        /**
         * Double quoted
         * 双引号
         */
        DOUBLE_QUOTED,

        /**
         * Literal block scalar (|)
         * 字面块标量（|）
         */
        LITERAL,

        /**
         * Folded block scalar (>)
         * 折叠块标量（>）
         */
        FOLDED
    }

    /**
     * Configuration Builder
     * 配置构建器
     */
    public static final class Builder {

        private int indent = 2;
        private boolean prettyPrint = true;
        private boolean allowDuplicateKeys = false;
        private boolean safeMode = true;
        private int maxAliasesForCollections = 50;
        private int maxNestingDepth = 100;
        private long maxDocumentSize = 10 * 1024 * 1024; // 10MB
        private FlowStyle defaultFlowStyle = FlowStyle.BLOCK;
        private ScalarStyle defaultScalarStyle = ScalarStyle.PLAIN;
        private boolean strictTypes = false;

        private Builder() {}

        /**
         * Sets the indent size.
         * 设置缩进大小。
         *
         * @param indent the indent spaces | 缩进空格数
         * @return this builder | 此构建器
         */
        public Builder indent(int indent) {
            this.indent = indent;
            return this;
        }

        /**
         * Sets whether to pretty print.
         * 设置是否美化打印。
         *
         * @param prettyPrint whether to pretty print | 是否美化打印
         * @return this builder | 此构建器
         */
        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        /**
         * Sets whether to allow duplicate keys.
         * 设置是否允许重复键。
         *
         * @param allow whether to allow | 是否允许
         * @return this builder | 此构建器
         */
        public Builder allowDuplicateKeys(boolean allow) {
            this.allowDuplicateKeys = allow;
            return this;
        }

        /**
         * Sets whether to enable safe mode.
         * 设置是否启用安全模式。
         *
         * @param safe whether safe mode is enabled | 是否启用安全模式
         * @return this builder | 此构建器
         */
        public Builder safeMode(boolean safe) {
            this.safeMode = safe;
            return this;
        }

        /**
         * Sets the maximum aliases for collections.
         * 设置集合的最大别名数。
         *
         * @param max the maximum number | 最大数量
         * @return this builder | 此构建器
         */
        public Builder maxAliasesForCollections(int max) {
            this.maxAliasesForCollections = max;
            return this;
        }

        /**
         * Sets the maximum nesting depth.
         * 设置最大嵌套深度。
         *
         * @param depth the maximum depth | 最大深度
         * @return this builder | 此构建器
         */
        public Builder maxNestingDepth(int depth) {
            this.maxNestingDepth = depth;
            return this;
        }

        /**
         * Sets the maximum document size.
         * 设置最大文档大小。
         *
         * @param size the maximum size in bytes | 最大大小（字节）
         * @return this builder | 此构建器
         */
        public Builder maxDocumentSize(long size) {
            this.maxDocumentSize = size;
            return this;
        }

        /**
         * Sets the default flow style.
         * 设置默认流风格。
         *
         * @param flowStyle the flow style | 流风格
         * @return this builder | 此构建器
         */
        public Builder defaultFlowStyle(FlowStyle flowStyle) {
            this.defaultFlowStyle = flowStyle;
            return this;
        }

        /**
         * Sets the default scalar style.
         * 设置默认标量风格。
         *
         * @param scalarStyle the scalar style | 标量风格
         * @return this builder | 此构建器
         */
        public Builder defaultScalarStyle(ScalarStyle scalarStyle) {
            this.defaultScalarStyle = scalarStyle;
            return this;
        }

        /**
         * Sets whether to enable strict types mode.
         * 设置是否启用严格类型模式。
         *
         * <p>When enabled, YAML 1.1 boolean values (YES/NO/ON/OFF/y/n/Y/N) are
         * treated as strings instead of being converted to booleans.</p>
         * <p>启用时，YAML 1.1 布尔值（YES/NO/ON/OFF/y/n/Y/N）将被视为字符串，
         * 而不会被转换为布尔值。</p>
         *
         * @param strictTypes whether strict types is enabled | 是否启用严格类型
         * @return this builder | 此构建器
         */
        public Builder strictTypes(boolean strictTypes) {
            this.strictTypes = strictTypes;
            return this;
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the configuration | 配置
         */
        public YmlConfig build() {
            return new YmlConfig(this);
        }
    }
}
