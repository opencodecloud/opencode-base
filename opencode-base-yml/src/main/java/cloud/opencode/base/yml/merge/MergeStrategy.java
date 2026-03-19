package cloud.opencode.base.yml.merge;

/**
 * Merge Strategy - Defines how YAML documents are merged
 * 合并策略 - 定义 YAML 文档如何合并
 *
 * <p>This enum defines different strategies for merging YAML documents.</p>
 * <p>此枚举定义了合并 YAML 文档的不同策略。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Override, keep-first, deep merge, list append, unique merge, and fail-on-conflict - 覆盖、保留首个、深度合并、列表追加、唯一合并和冲突失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Object> merged = YmlMerger.merge(base, overlay, MergeStrategy.DEEP_MERGE);
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
public enum MergeStrategy {

    /**
     * Override values (later wins)
     * 覆盖值（后者优先）
     */
    OVERRIDE,

    /**
     * Keep first value (earlier wins)
     * 保留第一个值（先者优先）
     */
    KEEP_FIRST,

    /**
     * Deep merge maps recursively
     * 递归深度合并映射
     */
    DEEP_MERGE,

    /**
     * Append lists instead of replacing
     * 追加列表而不是替换
     */
    APPEND_LISTS,

    /**
     * Merge lists by removing duplicates
     * 通过去重合并列表
     */
    MERGE_LISTS_UNIQUE,

    /**
     * Fail on conflict
     * 冲突时失败
     */
    FAIL_ON_CONFLICT
}
