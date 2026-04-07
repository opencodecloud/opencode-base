package cloud.opencode.base.yml.diff;

import java.util.Objects;

/**
 * Diff Entry - Represents a single difference between two YAML documents
 * 差异条目 - 表示两个 YAML 文档之间的单个差异
 *
 * <p>An immutable record that captures the type of change, the path where it occurred,
 * and the old/new values involved.</p>
 * <p>一个不可变记录，捕获变更类型、发生位置的路径以及涉及的旧/新值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record with type, path, old value, new value - 不可变记录：类型、路径、旧值、新值</li>
 *   <li>Factory methods for creating entries - 创建条目的工厂方法</li>
 *   <li>Dot-notation paths with array index support - 点号路径表示法，支持数组索引</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DiffEntry added = DiffEntry.added("server.port", 8080);
 * DiffEntry removed = DiffEntry.removed("logging.level", "DEBUG");
 * DiffEntry modified = DiffEntry.modified("app.name", "old", "new");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param type     the type of change | 变更类型
 * @param path     the dot-notation path where change occurred | 变更发生位置的点号路径
 * @param oldValue the previous value (null for ADDED) | 先前的值（ADDED 时为 null）
 * @param newValue the new value (null for REMOVED) | 新值（REMOVED 时为 null）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public record DiffEntry(DiffType type, String path, Object oldValue, Object newValue) {

    /**
     * Canonical constructor with validation.
     * 带校验的规范构造器。
     *
     * @param type     the type of change | 变更类型
     * @param path     the path | 路径
     * @param oldValue the old value | 旧值
     * @param newValue the new value | 新值
     * @throws NullPointerException if type or path is null | 当 type 或 path 为 null 时
     */
    public DiffEntry {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(path, "path must not be null");
    }

    /**
     * Creates an ADDED entry.
     * 创建新增条目。
     *
     * @param path  the path where value was added | 新增值的路径
     * @param value the added value | 新增的值
     * @return the diff entry | 差异条目
     */
    public static DiffEntry added(String path, Object value) {
        return new DiffEntry(DiffType.ADDED, path, null, value);
    }

    /**
     * Creates a REMOVED entry.
     * 创建移除条目。
     *
     * @param path  the path where value was removed | 移除值的路径
     * @param value the removed value | 移除的值
     * @return the diff entry | 差异条目
     */
    public static DiffEntry removed(String path, Object value) {
        return new DiffEntry(DiffType.REMOVED, path, value, null);
    }

    /**
     * Creates a MODIFIED entry.
     * 创建修改条目。
     *
     * @param path     the path where value was modified | 修改值的路径
     * @param oldValue the previous value | 先前的值
     * @param newValue the new value | 新值
     * @return the diff entry | 差异条目
     */
    public static DiffEntry modified(String path, Object oldValue, Object newValue) {
        return new DiffEntry(DiffType.MODIFIED, path, oldValue, newValue);
    }
}
