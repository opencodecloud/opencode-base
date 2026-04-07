package cloud.opencode.base.csv.stats;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CSV Stats - Statistical operations on CSV columns
 * CSV统计 - 对CSV列的统计操作
 *
 * <p>Provides count, numeric aggregate, distinct, and frequency operations
 * on columns of a {@link CsvDocument}. All methods are static and the class
 * cannot be instantiated.</p>
 * <p>提供对 {@link CsvDocument} 列的计数、数值聚合、去重和频率操作。
 * 所有方法均为静态方法，该类不可实例化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Count operations (non-blank, total rows) - 计数操作（非空白、总行数）</li>
 *   <li>Numeric aggregation (sum, avg, min, max) - 数值聚合（求和、平均、最小、最大）</li>
 *   <li>String operations (distinct, frequency) - 字符串操作（去重、频率）</li>
 *   <li>Column summary (all stats at once) - 列摘要（一次性获取所有统计）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument doc = CsvDocument.builder()
 *     .header("name", "score")
 *     .addRow("Alice", "95")
 *     .addRow("Bob", "87")
 *     .build();
 *
 * long count = CsvStats.count(doc, "name");           // 2
 * BigDecimal total = CsvStats.sum(doc, "score");       // 182
 * BigDecimal average = CsvStats.avg(doc, "score");     // 91.000000
 * List<String> names = CsvStats.distinct(doc, "name"); // [Alice, Bob]
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
public final class CsvStats {

    private static final int AVG_SCALE = 6;

    private CsvStats() {
        // utility class
    }

    // ==================== Count Operations | 计数操作 ====================

    /**
     * Counts non-blank values in a column
     * 计算列中非空白值的数量
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the count of non-blank values | 非空白值数量
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static long count(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        long count = 0;
        for (CsvRow row : doc.rows()) {
            String val = safeGet(row, idx);
            if (val != null && !val.isBlank()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts all rows in the document
     * 计算文档中所有行的数量
     *
     * @param doc the document | 文档
     * @return the total row count | 总行数
     * @throws NullPointerException if doc is null | 如果doc为null
     */
    public static long countAll(CsvDocument doc) {
        Objects.requireNonNull(doc, "doc must not be null");
        return doc.rowCount();
    }

    // ==================== Numeric Operations | 数值操作 ====================

    /**
     * Computes the sum of numeric values in a column
     * 计算列中数值的总和
     *
     * <p>Non-numeric and blank values are silently skipped.
     * Returns {@link BigDecimal#ZERO} if no numeric values are found.</p>
     * <p>非数值和空白值被静默跳过。
     * 如果没有找到数值，返回 {@link BigDecimal#ZERO}。</p>
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the sum | 总和
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static BigDecimal sum(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        BigDecimal sum = BigDecimal.ZERO;
        for (CsvRow row : doc.rows()) {
            BigDecimal val = tryParse(safeGet(row, idx));
            if (val != null) {
                sum = sum.add(val);
            }
        }
        return sum;
    }

    /**
     * Computes the average of numeric values in a column
     * 计算列中数值的平均值
     *
     * <p>Non-numeric and blank values are silently skipped.
     * Returns null if no numeric values are found.
     * Uses HALF_UP rounding with scale of 6.</p>
     * <p>非数值和空白值被静默跳过。
     * 如果没有找到数值，返回null。
     * 使用HALF_UP舍入模式，精度为6。</p>
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the average, or null if no numeric values | 平均值，无数值时为null
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static BigDecimal avg(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        BigDecimal sum = BigDecimal.ZERO;
        long count = 0;
        for (CsvRow row : doc.rows()) {
            BigDecimal val = tryParse(safeGet(row, idx));
            if (val != null) {
                sum = sum.add(val);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), AVG_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Finds the minimum numeric value in a column
     * 查找列中的最小数值
     *
     * <p>Non-numeric and blank values are silently skipped.
     * Returns null if no numeric values are found.</p>
     * <p>非数值和空白值被静默跳过。
     * 如果没有找到数值，返回null。</p>
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the minimum value, or null if no numeric values | 最小值，无数值时为null
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static BigDecimal min(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        BigDecimal min = null;
        for (CsvRow row : doc.rows()) {
            BigDecimal val = tryParse(safeGet(row, idx));
            if (val != null) {
                if (min == null || val.compareTo(min) < 0) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Finds the maximum numeric value in a column
     * 查找列中的最大数值
     *
     * <p>Non-numeric and blank values are silently skipped.
     * Returns null if no numeric values are found.</p>
     * <p>非数值和空白值被静默跳过。
     * 如果没有找到数值，返回null。</p>
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the maximum value, or null if no numeric values | 最大值，无数值时为null
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static BigDecimal max(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        BigDecimal max = null;
        for (CsvRow row : doc.rows()) {
            BigDecimal val = tryParse(safeGet(row, idx));
            if (val != null) {
                if (max == null || val.compareTo(max) > 0) {
                    max = val;
                }
            }
        }
        return max;
    }

    // ==================== String Operations | 字符串操作 ====================

    /**
     * Returns distinct non-null values in a column, preserving insertion order
     * 返回列中去重的非null值，保留插入顺序
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return unmodifiable list of distinct values | 不可修改的去重值列表
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static List<String> distinct(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (CsvRow row : doc.rows()) {
            String val = safeGet(row, idx);
            if (val != null) {
                seen.add(val);
            }
        }
        return List.copyOf(seen);
    }

    /**
     * Computes frequency of each value in a column, ordered by count descending
     * 计算列中每个值的频率，按计数降序排列
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return unmodifiable map of value to count, sorted by count descending
     *         | 不可修改的值到计数Map，按计数降序排列
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static Map<String, Long> frequency(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        Map<String, Long> freq = new LinkedHashMap<>();
        for (CsvRow row : doc.rows()) {
            String val = safeGet(row, idx);
            if (val != null) {
                freq.merge(val, 1L, Long::sum);
            }
        }

        // Sort by count descending
        List<Map.Entry<String, Long>> entries = new ArrayList<>(freq.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        LinkedHashMap<String, Long> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Long> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(sorted);
    }

    // ==================== Summary | 摘要 ====================

    /**
     * Computes a full statistical summary for a column
     * 计算列的完整统计摘要
     *
     * @param doc    the document | 文档
     * @param column the column name | 列名
     * @return the column statistics | 列统计
     * @throws NullPointerException if any parameter is null | 如果任何参数为null
     * @throws OpenCsvException     if the column is not found | 如果列未找到
     */
    public static CsvColumnStats summary(CsvDocument doc, String column) {
        Objects.requireNonNull(doc, "doc must not be null");
        Objects.requireNonNull(column, "column must not be null");
        int idx = resolveColumnIndex(doc, column);

        long totalCount = doc.rowCount();
        long nonBlankCount = 0;
        LinkedHashSet<String> distinctValues = new LinkedHashSet<>();
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal numMin = null;
        BigDecimal numMax = null;
        long numericCount = 0;

        for (CsvRow row : doc.rows()) {
            String val = safeGet(row, idx);
            if (val != null) {
                distinctValues.add(val);
            }
            if (val != null && !val.isBlank()) {
                nonBlankCount++;
                BigDecimal num = tryParse(val);
                if (num != null) {
                    sum = sum.add(num);
                    numericCount++;
                    if (numMin == null || num.compareTo(numMin) < 0) {
                        numMin = num;
                    }
                    if (numMax == null || num.compareTo(numMax) > 0) {
                        numMax = num;
                    }
                }
            }
        }

        BigDecimal avgVal = null;
        BigDecimal sumVal = null;
        if (numericCount > 0) {
            sumVal = sum;
            avgVal = sum.divide(BigDecimal.valueOf(numericCount), AVG_SCALE, RoundingMode.HALF_UP);
        }

        return new CsvColumnStats(
                column,
                totalCount,
                nonBlankCount,
                distinctValues.size(),
                sumVal,
                avgVal,
                numMin,
                numMax
        );
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    private static int resolveColumnIndex(CsvDocument doc, String column) {
        int idx = doc.headers().indexOf(column);
        if (idx < 0) {
            throw new OpenCsvException("Column '" + column + "' not found in document headers");
        }
        return idx;
    }

    private static String safeGet(CsvRow row, int index) {
        if (index < row.size()) {
            return row.get(index);
        }
        return null;
    }

    private static BigDecimal tryParse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.strip());
        } catch (NumberFormatException _) {
            return null;
        }
    }
}
