package cloud.opencode.base.csv.merge;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedSet;

/**
 * CSV Merge - Utility class for merging multiple CSV documents
 * CSV合并 - 合并多个CSV文档的工具类
 *
 * <p>Provides vertical merge (row concatenation) and horizontal merge (join)
 * operations for CSV documents. All methods are static and the class cannot
 * be instantiated.</p>
 * <p>提供CSV文档的垂直合并（行连接）和水平合并（连接）操作。
 * 所有方法均为静态方法，该类不可实例化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Vertical merge (concat) - appends rows from multiple documents
 *       - 垂直合并（concat）- 追加多个文档的行</li>
 *   <li>Inner join - matches rows by key column, both sides required
 *       - 内连接 - 按键列匹配行，双方都需要存在</li>
 *   <li>Left join - matches rows by key column, all left rows preserved
 *       - 左连接 - 按键列匹配行，保留所有左表行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument a = CsvDocument.builder()
 *     .header("id", "name").addRow("1", "Alice").build();
 * CsvDocument b = CsvDocument.builder()
 *     .header("id", "name").addRow("2", "Bob").build();
 *
 * CsvDocument merged = CsvMerge.concat(a, b);      // 2 rows
 * CsvDocument joined = CsvMerge.innerJoin(a, b, "id"); // join by id
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: All null params throw NullPointerException - 空值安全: 所有null参数抛出NullPointerException</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvMerge {

    private CsvMerge() {
        // utility class
    }

    // ==================== Vertical Merge | 垂直合并 ====================

    /**
     * Concatenates multiple CSV documents vertically (appends rows)
     * 垂直连接多个CSV文档（追加行）
     *
     * <p>Uses headers from the first non-empty document. Columns are aligned
     * to the header: missing columns are filled with empty string, extra
     * columns are ignored.</p>
     * <p>使用第一个非空文档的标题。列对齐到标题：缺少的列用空字符串填充，
     * 多余的列被忽略。</p>
     *
     * @param docs the documents to concatenate | 要连接的文档
     * @return the concatenated document | 连接后的文档
     * @throws NullPointerException if docs or any element is null | 如果docs或任何元素为null
     * @throws OpenCsvException     if no documents are provided | 如果没有提供文档
     */
    public static CsvDocument concat(CsvDocument... docs) {
        Objects.requireNonNull(docs, "docs must not be null");
        return concat(List.of(docs));
    }

    /**
     * Concatenates multiple CSV documents vertically from a list
     * 从列表垂直连接多个CSV文档
     *
     * <p>Uses headers from the first non-empty document. Columns are aligned
     * to the header: missing columns are filled with empty string, extra
     * columns are ignored.</p>
     * <p>使用第一个非空文档的标题。列对齐到标题：缺少的列用空字符串填充，
     * 多余的列被忽略。</p>
     *
     * @param docs the documents to concatenate | 要连接的文档
     * @return the concatenated document | 连接后的文档
     * @throws NullPointerException if docs or any element is null | 如果docs或任何元素为null
     * @throws OpenCsvException     if the list is empty | 如果列表为空
     */
    public static CsvDocument concat(List<CsvDocument> docs) {
        Objects.requireNonNull(docs, "docs must not be null");
        if (docs.isEmpty()) {
            throw new OpenCsvException("Cannot concat empty document list");
        }
        for (CsvDocument doc : docs) {
            Objects.requireNonNull(doc, "document element must not be null");
        }

        // Find headers from first non-empty document
        List<String> headers = List.of();
        for (CsvDocument doc : docs) {
            if (!doc.headers().isEmpty()) {
                headers = doc.headers();
                break;
            }
        }

        CsvDocument.Builder builder = CsvDocument.builder();
        if (!headers.isEmpty()) {
            builder.header(headers);
        }

        // Build column index map for alignment
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndex.put(headers.get(i), i);
        }

        int rowNum = 1;
        for (CsvDocument doc : docs) {
            if (headers.isEmpty()) {
                // No headers - just append rows as-is
                for (CsvRow row : doc.rows()) {
                    builder.addRow(CsvRow.of(rowNum++, row.values()));
                }
            } else {
                // Align columns
                List<String> docHeaders = doc.headers();
                for (CsvRow row : doc.rows()) {
                    String[] aligned = new String[headers.size()];
                    for (int i = 0; i < headers.size(); i++) {
                        aligned[i] = "";
                    }

                    if (docHeaders.isEmpty()) {
                        // No headers in this doc - map by position
                        for (int i = 0; i < Math.min(row.size(), headers.size()); i++) {
                            aligned[i] = row.get(i);
                        }
                    } else {
                        // Map by header name
                        for (int i = 0; i < docHeaders.size(); i++) {
                            Integer targetIdx = headerIndex.get(docHeaders.get(i));
                            if (targetIdx != null && i < row.size()) {
                                aligned[targetIdx] = row.get(i);
                            }
                        }
                    }
                    builder.addRow(CsvRow.of(rowNum++, aligned));
                }
            }
        }

        return builder.build();
    }

    // ==================== Horizontal Merge | 水平合并 ====================

    /**
     * Performs an inner join of two CSV documents on a key column
     * 对两个CSV文档按键列执行内连接
     *
     * <p>Both documents must have the key column in their headers.
     * The result contains all columns from the left document followed by
     * all columns from the right document (excluding the key column).
     * Only rows where the key exists in both documents are included.</p>
     * <p>两个文档的标题中都必须包含键列。
     * 结果包含左文档的所有列，然后是右文档的所有列（排除键列）。
     * 仅包含键在两个文档中都存在的行。</p>
     *
     * @param left      the left document | 左文档
     * @param right     the right document | 右文档
     * @param keyColumn the key column name | 键列名
     * @return the joined document | 连接后的文档
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if keyColumn is not found in either document | 如果键列在任一文档中未找到
     */
    public static CsvDocument innerJoin(CsvDocument left, CsvDocument right, String keyColumn) {
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
        Objects.requireNonNull(keyColumn, "keyColumn must not be null");

        int leftKeyIdx = validateKeyColumn(left, keyColumn, "left");
        int rightKeyIdx = validateKeyColumn(right, keyColumn, "right");

        // Build right index: key -> list of rows
        Map<String, List<CsvRow>> rightIndex = buildIndex(right, rightKeyIdx);

        // Build merged headers
        List<String> mergedHeaders = buildMergedHeaders(left, right, rightKeyIdx);

        CsvDocument.Builder builder = CsvDocument.builder().header(mergedHeaders);

        int rowNum = 1;
        for (CsvRow leftRow : left.rows()) {
            String key = safeGet(leftRow, leftKeyIdx);
            List<CsvRow> matchingRightRows = rightIndex.get(key);
            if (matchingRightRows != null) {
                for (CsvRow rightRow : matchingRightRows) {
                    String[] merged = mergeRow(leftRow, rightRow, left.headers().size(),
                            right.headers().size(), rightKeyIdx);
                    builder.addRow(CsvRow.of(rowNum++, merged));
                }
            }
        }

        return builder.build();
    }

    /**
     * Performs a left join of two CSV documents on a key column
     * 对两个CSV文档按键列执行左连接
     *
     * <p>Both documents must have the key column in their headers.
     * The result contains all columns from the left document followed by
     * all columns from the right document (excluding the key column).
     * All left rows are included; if no match in right, right columns
     * are filled with empty string.</p>
     * <p>两个文档的标题中都必须包含键列。
     * 结果包含左文档的所有列，然后是右文档的所有列（排除键列）。
     * 包含所有左表行；如果右表没有匹配，右列用空字符串填充。</p>
     *
     * @param left      the left document | 左文档
     * @param right     the right document | 右文档
     * @param keyColumn the key column name | 键列名
     * @return the joined document | 连接后的文档
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if keyColumn is not found in either document | 如果键列在任一文档中未找到
     */
    public static CsvDocument leftJoin(CsvDocument left, CsvDocument right, String keyColumn) {
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
        Objects.requireNonNull(keyColumn, "keyColumn must not be null");

        int leftKeyIdx = validateKeyColumn(left, keyColumn, "left");
        int rightKeyIdx = validateKeyColumn(right, keyColumn, "right");

        // Build right index: key -> list of rows
        Map<String, List<CsvRow>> rightIndex = buildIndex(right, rightKeyIdx);

        // Build merged headers
        List<String> mergedHeaders = buildMergedHeaders(left, right, rightKeyIdx);

        int rightColsExcludingKey = right.headers().size() - 1;

        CsvDocument.Builder builder = CsvDocument.builder().header(mergedHeaders);

        int rowNum = 1;
        for (CsvRow leftRow : left.rows()) {
            String key = safeGet(leftRow, leftKeyIdx);
            List<CsvRow> matchingRightRows = rightIndex.get(key);
            if (matchingRightRows != null) {
                for (CsvRow rightRow : matchingRightRows) {
                    String[] merged = mergeRow(leftRow, rightRow, left.headers().size(),
                            right.headers().size(), rightKeyIdx);
                    builder.addRow(CsvRow.of(rowNum++, merged));
                }
            } else {
                // No match - fill right columns with empty string
                String[] merged = new String[mergedHeaders.size()];
                for (int i = 0; i < left.headers().size(); i++) {
                    merged[i] = safeGet(leftRow, i);
                }
                for (int i = 0; i < rightColsExcludingKey; i++) {
                    merged[left.headers().size() + i] = "";
                }
                builder.addRow(CsvRow.of(rowNum++, merged));
            }
        }

        return builder.build();
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    private static int validateKeyColumn(CsvDocument doc, String keyColumn, String side) {
        int idx = doc.headers().indexOf(keyColumn);
        if (idx < 0) {
            throw new OpenCsvException(
                    "Key column '" + keyColumn + "' not found in " + side + " document headers");
        }
        return idx;
    }

    private static Map<String, List<CsvRow>> buildIndex(CsvDocument doc, int keyIdx) {
        Map<String, List<CsvRow>> index = new LinkedHashMap<>();
        for (CsvRow row : doc.rows()) {
            String key = safeGet(row, keyIdx);
            index.computeIfAbsent(key, _ -> new ArrayList<>()).add(row);
        }
        return index;
    }

    private static List<String> buildMergedHeaders(CsvDocument left, CsvDocument right, int rightKeyIdx) {
        SequencedSet<String> seen = new LinkedHashSet<>(left.headers());
        List<String> merged = new ArrayList<>(left.headers());
        for (int i = 0; i < right.headers().size(); i++) {
            if (i != rightKeyIdx) {
                String header = right.headers().get(i);
                // Avoid duplicate header names by suffixing
                String finalHeader = header;
                if (seen.contains(header)) {
                    finalHeader = header + "_right";
                }
                seen.add(finalHeader);
                merged.add(finalHeader);
            }
        }
        return List.copyOf(merged);
    }

    private static String[] mergeRow(CsvRow leftRow, CsvRow rightRow,
                                     int leftColCount, int rightColCount, int rightKeyIdx) {
        int rightColsExcludingKey = rightColCount - 1;
        String[] merged = new String[leftColCount + rightColsExcludingKey];
        for (int i = 0; i < leftColCount; i++) {
            merged[i] = safeGet(leftRow, i);
        }
        int pos = leftColCount;
        for (int i = 0; i < rightColCount; i++) {
            if (i != rightKeyIdx) {
                merged[pos++] = safeGet(rightRow, i);
            }
        }
        return merged;
    }

    private static String safeGet(CsvRow row, int index) {
        if (index < row.size()) {
            String val = row.get(index);
            return val != null ? val : "";
        }
        return "";
    }
}
