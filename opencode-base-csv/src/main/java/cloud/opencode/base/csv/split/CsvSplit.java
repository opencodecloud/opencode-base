package cloud.opencode.base.csv.split;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * CSV Split - Utility for splitting CSV documents
 * CSV拆分 - CSV文档拆分工具
 *
 * <p>Provides static methods for splitting a {@link CsvDocument} by row count,
 * by a predicate condition, or by grouping on a column value. All methods
 * preserve the original document's headers in the resulting sub-documents.</p>
 * <p>提供静态方法，按行数、按谓词条件或按列值分组拆分 {@link CsvDocument}。
 * 所有方法在结果子文档中保留原始文档的标题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Split by size (fixed chunk size) - 按大小拆分（固定块大小）</li>
 *   <li>Split by condition (binary partition) - 按条件拆分（二元分区）</li>
 *   <li>Split by column value (GROUP BY) - 按列值拆分（分组）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<CsvDocument> chunks = CsvSplit.bySize(doc, 100);
 * List<CsvDocument> parts = CsvSplit.byCondition(doc, row -> row.get(0).startsWith("A"));
 * Map<String, CsvDocument> groups = CsvSplit.byColumn(doc, "department");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: Validates all inputs - 空值安全: 验证所有输入</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvSplit {

    private CsvSplit() {
        // utility class
    }

    /**
     * Splits a document into chunks of at most maxRows rows each
     * 将文档拆分为每块最多maxRows行的块
     *
     * <p>Each sub-document shares the same headers as the original.
     * The last chunk may contain fewer rows.</p>
     * <p>每个子文档与原始文档共享相同的标题。最后一块可能包含更少的行。</p>
     *
     * @param doc     the document to split | 要拆分的文档
     * @param maxRows the maximum number of rows per chunk | 每块最大行数
     * @return a list of sub-documents | 子文档列表
     * @throws NullPointerException if doc is null | 如果doc为null
     * @throws OpenCsvException     if maxRows is not positive | 如果maxRows不为正数
     */
    public static List<CsvDocument> bySize(CsvDocument doc, int maxRows) {
        Objects.requireNonNull(doc, "doc must not be null");
        if (maxRows <= 0) {
            throw new OpenCsvException("maxRows must be positive, but was: " + maxRows);
        }

        if (doc.isEmpty()) {
            return List.of(buildDoc(doc.headers(), List.of()));
        }

        List<CsvDocument> result = new ArrayList<>();
        List<CsvRow> rows = doc.rows();
        for (int i = 0; i < rows.size(); i += maxRows) {
            int end = Math.min(i + maxRows, rows.size());
            result.add(buildDoc(doc.headers(), rows.subList(i, end)));
        }
        return List.copyOf(result);
    }

    /**
     * Splits a document into two: rows matching the predicate, and rows not matching
     * 将文档拆分为两部分：匹配谓词的行和不匹配的行
     *
     * <p>Always returns exactly 2 documents: [matching, non-matching].
     * Both share the same headers.</p>
     * <p>始终返回恰好2个文档：[匹配的, 不匹配的]。两者共享相同的标题。</p>
     *
     * @param doc       the document to split | 要拆分的文档
     * @param predicate the row predicate | 行谓词
     * @return a list of exactly 2 documents [matching, non-matching] | 恰好2个文档的列表
     * @throws NullPointerException if doc or predicate is null | 如果doc或predicate为null
     */
    public static List<CsvDocument> byCondition(CsvDocument doc, Predicate<CsvRow> predicate) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(predicate, "predicate must not be null");

        List<CsvRow> matching = new ArrayList<>();
        List<CsvRow> nonMatching = new ArrayList<>();

        for (CsvRow row : doc.rows()) {
            if (predicate.test(row)) {
                matching.add(row);
            } else {
                nonMatching.add(row);
            }
        }

        return List.of(
                buildDoc(doc.headers(), matching),
                buildDoc(doc.headers(), nonMatching)
        );
    }

    /**
     * Splits a document by grouping rows on a column value (like SQL GROUP BY)
     * 按列值分组拆分文档（类似SQL GROUP BY）
     *
     * <p>Returns a {@link LinkedHashMap} preserving first-seen order of column values.
     * Each sub-document shares the same headers.</p>
     * <p>返回保留列值首次出现顺序的 {@link LinkedHashMap}。
     * 每个子文档共享相同的标题。</p>
     *
     * @param doc    the document to split | 要拆分的文档
     * @param column the column name to group by | 用于分组的列名
     * @return a map of column value to sub-document | 列值到子文档的映射
     * @throws NullPointerException  if doc or column is null | 如果doc或column为null
     * @throws OpenCsvException      if column is not found in headers | 如果列在标题中未找到
     */
    public static Map<String, CsvDocument> byColumn(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");

        if (doc.isEmpty()) {
            return Map.of();
        }

        List<String> headers = doc.headers();
        int colIndex = headers.indexOf(column);
        if (colIndex < 0) {
            throw new OpenCsvException("Column not found: " + column);
        }

        LinkedHashMap<String, List<CsvRow>> groups = new LinkedHashMap<>();
        for (CsvRow row : doc.rows()) {
            String key = colIndex < row.size() ? row.get(colIndex) : "";
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        LinkedHashMap<String, CsvDocument> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<CsvRow>> entry : groups.entrySet()) {
            result.put(entry.getKey(), buildDoc(headers, entry.getValue()));
        }
        return Map.copyOf(result);
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    private static CsvDocument buildDoc(List<String> headers, List<CsvRow> rows) {
        CsvDocument.Builder builder = CsvDocument.builder().header(headers);
        for (CsvRow row : rows) {
            builder.addRow(row);
        }
        return builder.build();
    }
}
