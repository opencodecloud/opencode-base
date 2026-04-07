package cloud.opencode.base.csv.diff;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CSV Diff - Computes differences between two CSV documents
 * CSV差异 - 计算两个CSV文档之间的差异
 *
 * <p>Provides two comparison strategies: row-by-row positional comparison
 * and key-based matching for more meaningful diffing of tabular data.</p>
 * <p>提供两种比较策略：逐行位置比较和基于键的匹配，用于更有意义的表格数据差异比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Positional row-by-row diff - 位置逐行差异</li>
 *   <li>Key-based diff using a key column - 基于键列的差异</li>
 *   <li>Detects ADDED, REMOVED, and MODIFIED rows - 检测新增、删除和修改的行</li>
 *   <li>Preserves row order in results - 结果保持行顺序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<CsvChange> changes = CsvDiff.diff(original, modified);
 * List<CsvChange> changes = CsvDiff.diffByKey(original, modified, "id");
 *
 * for (CsvChange change : changes) {
 *     switch (change.type()) {
 *         case ADDED    -> System.out.println("Added: " + change.newRow());
 *         case REMOVED  -> System.out.println("Removed: " + change.oldRow());
 *         case MODIFIED -> System.out.println("Modified row " + change.rowIndex());
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see CsvChange
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvDiff {

    private CsvDiff() {
        // utility class
    }

    /**
     * Computes differences between two CSV documents using positional row comparison
     * 使用位置行比较计算两个CSV文档之间的差异
     *
     * <p>Compares rows at the same index position. Rows present only in the
     * modified document are reported as ADDED, rows missing from the modified
     * document are reported as REMOVED, and rows that differ at the same
     * position are reported as MODIFIED.</p>
     * <p>比较相同索引位置的行。仅在修改文档中存在的行报告为ADDED，
     * 修改文档中缺失的行报告为REMOVED，相同位置不同的行报告为MODIFIED。</p>
     *
     * @param original the original document | 原始文档
     * @param modified the modified document | 修改后的文档
     * @return list of changes (empty if identical) | 变更列表（相同则为空）
     * @throws NullPointerException if either argument is null | 如果任一参数为null
     */
    public static List<CsvChange> diff(CsvDocument original, CsvDocument modified) {
        Objects.requireNonNull(original, "original must not be null");
        Objects.requireNonNull(modified, "modified must not be null");

        List<CsvChange> changes = new ArrayList<>();
        int origSize = original.rowCount();
        int modSize = modified.rowCount();
        int commonSize = Math.min(origSize, modSize);

        // Compare common rows
        for (int i = 0; i < commonSize; i++) {
            CsvRow origRow = original.getRow(i);
            CsvRow modRow = modified.getRow(i);
            if (!rowsEqual(origRow, modRow)) {
                changes.add(new CsvChange(CsvChange.ChangeType.MODIFIED, i, origRow, modRow));
            }
        }

        // Extra rows in modified → ADDED
        for (int i = commonSize; i < modSize; i++) {
            changes.add(new CsvChange(CsvChange.ChangeType.ADDED, i, null, modified.getRow(i)));
        }

        // Missing rows in modified → REMOVED
        for (int i = commonSize; i < origSize; i++) {
            changes.add(new CsvChange(CsvChange.ChangeType.REMOVED, i, original.getRow(i), null));
        }

        return List.copyOf(changes);
    }

    /**
     * Computes differences using a key column for row matching
     * 使用键列进行行匹配来计算差异
     *
     * <p>Builds an index from the specified key column and matches rows
     * by their key values. This is useful for comparing datasets where
     * rows may be reordered but have a unique identifier.</p>
     * <p>从指定键列构建索引，并按键值匹配行。适用于行可能重新排序
     * 但具有唯一标识符的数据集比较。</p>
     *
     * @param original  the original document | 原始文档
     * @param modified  the modified document | 修改后的文档
     * @param keyColumn the header name of the key column | 键列的标题名称
     * @return list of changes (empty if identical) | 变更列表（相同则为空）
     * @throws NullPointerException if any argument is null | 如果任一参数为null
     * @throws OpenCsvException     if key column not found | 如果键列未找到
     */
    public static List<CsvChange> diffByKey(CsvDocument original, CsvDocument modified,
                                             String keyColumn) {
        Objects.requireNonNull(original, "original must not be null");
        Objects.requireNonNull(modified, "modified must not be null");
        Objects.requireNonNull(keyColumn, "keyColumn must not be null");

        int origKeyIdx = findColumnIndex(original, keyColumn);
        int modKeyIdx = findColumnIndex(modified, keyColumn);

        // Build index: key → row for original
        Map<String, IndexedRow> origIndex = buildKeyIndex(original, origKeyIdx);
        Map<String, IndexedRow> modIndex = buildKeyIndex(modified, modKeyIdx);

        List<CsvChange> changes = new ArrayList<>();

        // Check original rows: REMOVED or MODIFIED
        for (Map.Entry<String, IndexedRow> entry : origIndex.entrySet()) {
            String key = entry.getKey();
            IndexedRow origEntry = entry.getValue();
            IndexedRow modEntry = modIndex.get(key);

            if (modEntry == null) {
                changes.add(new CsvChange(CsvChange.ChangeType.REMOVED,
                        origEntry.index, origEntry.row, null));
            } else if (!rowsEqual(origEntry.row, modEntry.row)) {
                changes.add(new CsvChange(CsvChange.ChangeType.MODIFIED,
                        modEntry.index, origEntry.row, modEntry.row));
            }
        }

        // Check modified rows: ADDED
        for (Map.Entry<String, IndexedRow> entry : modIndex.entrySet()) {
            if (!origIndex.containsKey(entry.getKey())) {
                IndexedRow modEntry = entry.getValue();
                changes.add(new CsvChange(CsvChange.ChangeType.ADDED,
                        modEntry.index, null, modEntry.row));
            }
        }

        return List.copyOf(changes);
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Finds the column index by header name
     */
    private static int findColumnIndex(CsvDocument doc, String columnName) {
        List<String> headers = doc.headers();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new OpenCsvException("Key column '" + columnName + "' not found in headers: "
                + headers);
    }

    /**
     * Builds a key index from a document using the specified column
     */
    private static Map<String, IndexedRow> buildKeyIndex(CsvDocument doc, int keyColumnIndex) {
        Map<String, IndexedRow> index = new LinkedHashMap<>();
        List<CsvRow> rows = doc.rows();
        for (int i = 0; i < rows.size(); i++) {
            CsvRow row = rows.get(i);
            String key = (keyColumnIndex < row.size()) ? row.get(keyColumnIndex) : "";
            index.put(key, new IndexedRow(i, row));
        }
        return index;
    }

    /**
     * Compares two rows for field-level equality
     */
    private static boolean rowsEqual(CsvRow a, CsvRow b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Internal holder for row index and row data
     */
    private record IndexedRow(int index, CsvRow row) {
    }
}
