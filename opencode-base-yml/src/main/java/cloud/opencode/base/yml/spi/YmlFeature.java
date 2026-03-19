package cloud.opencode.base.yml.spi;

/**
 * YAML Feature - Enumeration of YAML processing features
 * YAML 特性 - YAML 处理特性的枚举
 *
 * <p>This enum defines optional features that YAML providers may support.</p>
 * <p>此枚举定义了 YAML 提供者可能支持的可选特性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Feature flags for safe mode, block/flow style, comments, YAML 1.2, anchors, multi-doc, etc. - 安全模式、块/流风格、注释、YAML 1.2、锚点、多文档等特性标志</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if provider supports a feature
 * boolean hasSafeMode = provider.supports(YmlFeature.SAFE_MODE);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum constants are immutable) - 线程安全: 是（枚举常量不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public enum YmlFeature {

    /**
     * Allow duplicate keys in mappings
     * 允许映射中的重复键
     */
    ALLOW_DUPLICATE_KEYS,

    /**
     * Allow recursive keys
     * 允许递归键
     */
    ALLOW_RECURSIVE_KEYS,

    /**
     * Enable safe mode (restrict type deserialization)
     * 启用安全模式（限制类型反序列化）
     */
    SAFE_MODE,

    /**
     * Use block style for output
     * 使用块风格输出
     */
    BLOCK_STYLE,

    /**
     * Use flow style for output
     * 使用流式风格输出
     */
    FLOW_STYLE,

    /**
     * Preserve comments during parsing
     * 解析时保留注释
     */
    PRESERVE_COMMENTS,

    /**
     * Support YAML 1.2 specification
     * 支持 YAML 1.2 规范
     */
    YAML_1_2,

    /**
     * Support anchors and aliases
     * 支持锚点和别名
     */
    ANCHORS_ALIASES,

    /**
     * Support custom tags
     * 支持自定义标签
     */
    CUSTOM_TAGS,

    /**
     * Support multi-document parsing
     * 支持多文档解析
     */
    MULTI_DOCUMENT,

    /**
     * Pretty print output
     * 美化打印输出
     */
    PRETTY_PRINT,

    /**
     * Strict type checking
     * 严格类型检查
     */
    STRICT_TYPES
}
