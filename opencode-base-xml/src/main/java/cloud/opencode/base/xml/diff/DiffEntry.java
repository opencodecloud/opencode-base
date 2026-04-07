package cloud.opencode.base.xml.diff;

/**
 * Diff Entry - A single difference between two XML documents
 * 差异条目 - 两个 XML 文档之间的单个差异
 *
 * <p>Represents one specific difference found during XML comparison,
 * including the XPath-like path, the type of change, and old/new values.</p>
 * <p>表示在 XML 比较期间发现的一个具体差异，包括类 XPath 路径、变更类型和旧值/新值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with path, type, old/new values - 包含路径、类型、旧值/新值的不可变记录</li>
 *   <li>XPath-like path format: /root/child[0]/grandchild[1] - 类 XPath 路径格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffEntry entry = new DiffEntry("/root/item[0]", DiffType.ADDED, null, "new value");
 * System.out.println(entry.path());  // "/root/item[0]"
 * System.out.println(entry.type());  // ADDED
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Allows null for oldValue/newValue - 空值安全: 允许 oldValue/newValue 为 null</li>
 * </ul>
 *
 * @param path     the XPath-like path to the differing element | 差异元素的类 XPath 路径
 * @param type     the type of difference | 差异类型
 * @param oldValue the old value (from first document), null if added | 旧值（来自第一个文档），如果是新增则为 null
 * @param newValue the new value (from second document), null if removed | 新值（来自第二个文档），如果是删除则为 null
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public record DiffEntry(String path, DiffType type, String oldValue, String newValue) {

    /**
     * Compact constructor validates required fields.
     * 紧凑构造器验证必填字段。
     */
    public DiffEntry {
        java.util.Objects.requireNonNull(path, "path must not be null");
        java.util.Objects.requireNonNull(type, "type must not be null");
    }
}
