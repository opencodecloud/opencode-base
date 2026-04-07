package cloud.opencode.base.csv.diff;

import cloud.opencode.base.csv.CsvRow;

/**
 * CSV Change - Represents a single change between two CSV documents
 * CSV变更 - 表示两个CSV文档之间的单个变更
 *
 * <p>An immutable record capturing a row-level difference. Used by CSV diff
 * operations to report additions, removals, and modifications.</p>
 * <p>一个不可变记录，捕获行级差异。被CSV差异操作用于报告添加、删除和修改。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvChange added = new CsvChange(ChangeType.ADDED, 3, null, newRow);
 * CsvChange removed = new CsvChange(ChangeType.REMOVED, 1, oldRow, null);
 * CsvChange modified = new CsvChange(ChangeType.MODIFIED, 2, oldRow, newRow);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param type     the type of change | 变更类型
 * @param rowIndex the row index in the original (for REMOVED/MODIFIED) or target (for ADDED) | 原始中的行索引（REMOVED/MODIFIED）或目标中的行索引（ADDED）
 * @param oldRow   the old row (null for ADDED) | 旧行（ADDED时为null）
 * @param newRow   the new row (null for REMOVED) | 新行（REMOVED时为null）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public record CsvChange(
        ChangeType type,
        int rowIndex,
        CsvRow oldRow,
        CsvRow newRow
) {

    /**
     * The type of change between two CSV documents
     * 两个CSV文档之间的变更类型
     */
    public enum ChangeType {

        /** A row was added | 添加了一行 */
        ADDED,

        /** A row was removed | 删除了一行 */
        REMOVED,

        /** A row was modified | 修改了一行 */
        MODIFIED
    }
}
