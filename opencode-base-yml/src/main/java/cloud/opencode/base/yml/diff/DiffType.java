package cloud.opencode.base.yml.diff;

/**
 * Diff Type - Enumeration of YAML diff change types
 * 差异类型 - YAML 差异变更类型枚举
 *
 * <p>Represents the kind of change detected when comparing two YAML documents.</p>
 * <p>表示比较两个 YAML 文档时检测到的变更类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ADDED - a key/value exists only in the new document - 仅在新文档中存在的键/值</li>
 *   <li>REMOVED - a key/value exists only in the base document - 仅在基础文档中存在的键/值</li>
 *   <li>MODIFIED - a key exists in both but values differ - 键在两者中都存在但值不同</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffEntry entry = DiffEntry.added("server.port", 8080);
 * assert entry.type() == DiffType.ADDED;
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public enum DiffType {

    /**
     * A new key/value was added.
     * 新增了键/值。
     */
    ADDED,

    /**
     * An existing key/value was removed.
     * 移除了已有的键/值。
     */
    REMOVED,

    /**
     * An existing key's value was modified.
     * 修改了已有键的值。
     */
    MODIFIED
}
