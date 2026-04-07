package cloud.opencode.base.csv.transform;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * CSV Transform - Fluent, immutable transformation pipeline for CsvDocument
 * CSV转换 - CsvDocument的流式不可变转换管道
 *
 * <p>Provides a fluent API for transforming CSV documents by renaming, reordering,
 * adding, removing, and mapping columns. Each method returns a new CsvTransform
 * instance (immutable chain pattern). All transformations are applied in sequence
 * when the terminal {@link #execute()} method is called.</p>
 * <p>提供用于转换CSV文档的流式API，支持重命名、重新排序、添加、删除和映射列。
 * 每个方法返回一个新的CsvTransform实例（不可变链模式）。所有转换在调用终端
 * {@link #execute()} 方法时按顺序执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable transformation chain - 不可变转换链</li>
 *   <li>Lazy execution on terminal operation - 终端操作时延迟执行</li>
 *   <li>Column rename (single and batch) - 列重命名（单个和批量）</li>
 *   <li>Column reorder, add, remove - 列重新排序、添加、删除</li>
 *   <li>Column value mapping - 列值映射</li>
 *   <li>Row-level transformation - 行级转换</li>
 *   <li>Column filtering by predicate - 按谓词过滤列</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument result = CsvTransform.from(doc)
 *     .renameColumn("name", "fullName")
 *     .addColumn("status", "active")
 *     .removeColumns("role")
 *     .mapColumn("fullName", String::toUpperCase)
 *     .execute();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Null parameters throw NullPointerException - 空值安全: null参数抛出NullPointerException</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public final class CsvTransform {

    private final CsvDocument document;
    private final List<Function<CsvDocument, CsvDocument>> steps;

    private CsvTransform(CsvDocument document, List<Function<CsvDocument, CsvDocument>> steps) {
        this.document = document;
        this.steps = steps;
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Creates a transformation pipeline from a CsvDocument
     * 从CsvDocument创建转换管道
     *
     * @param doc the source document | 源文档
     * @return a new CsvTransform instance | 新的CsvTransform实例
     * @throws NullPointerException if doc is null | 如果doc为null
     */
    public static CsvTransform from(CsvDocument doc) {
        Objects.requireNonNull(doc, "doc must not be null");
        return new CsvTransform(doc, List.of());
    }

    // ==================== Column Operations | 列操作 ====================

    /**
     * Renames a single column
     * 重命名单个列
     *
     * @param oldName the current column name | 当前列名
     * @param newName the new column name (must not be blank) | 新列名（不得为空白）
     * @return a new CsvTransform with the rename step added | 添加了重命名步骤的新CsvTransform
     * @throws NullPointerException if oldName or newName is null | 如果oldName或newName为null
     */
    public CsvTransform renameColumn(String oldName, String newName) {
        Objects.requireNonNull(oldName, "oldName must not be null");
        Objects.requireNonNull(newName, "newName must not be null");
        return addStep(doc -> doRenameColumn(doc, oldName, newName));
    }

    /**
     * Renames multiple columns using a mapping
     * 使用映射重命名多个列
     *
     * <p>Each entry in the map represents an old name to new name mapping.
     * All old names must exist in the document headers.</p>
     * <p>映射中的每个条目表示旧名到新名的映射。所有旧名必须存在于文档标题中。</p>
     *
     * @param mapping the old-to-new name mapping | 旧名到新名的映射
     * @return a new CsvTransform with the rename step added | 添加了重命名步骤的新CsvTransform
     * @throws NullPointerException if mapping is null | 如果mapping为null
     */
    public CsvTransform renameColumns(Map<String, String> mapping) {
        Objects.requireNonNull(mapping, "mapping must not be null");
        // Defensive copy
        Map<String, String> copy = Map.copyOf(mapping);
        return addStep(doc -> doRenameColumns(doc, copy));
    }

    /**
     * Reorders columns by specifying the desired column order
     * 通过指定期望的列顺序来重新排列列
     *
     * <p>All column names must exist in the document headers.
     * Only the specified columns will appear in the result.</p>
     * <p>所有列名必须存在于文档标题中。结果中只出现指定的列。</p>
     *
     * @param columnOrder the desired column order | 期望的列顺序
     * @return a new CsvTransform with the reorder step added | 添加了重排步骤的新CsvTransform
     * @throws NullPointerException if columnOrder is null | 如果columnOrder为null
     */
    public CsvTransform reorderColumns(String... columnOrder) {
        Objects.requireNonNull(columnOrder, "columnOrder must not be null");
        List<String> order = List.of(columnOrder);
        return addStep(doc -> doReorderColumns(doc, order));
    }

    /**
     * Adds a new column with a fixed default value
     * 添加具有固定默认值的新列
     *
     * @param name         the new column name | 新列名
     * @param defaultValue the default value for all rows | 所有行的默认值
     * @return a new CsvTransform with the add column step added | 添加了新列步骤的新CsvTransform
     * @throws NullPointerException if name is null | 如果name为null
     */
    public CsvTransform addColumn(String name, String defaultValue) {
        Objects.requireNonNull(name, "name must not be null");
        return addStep(doc -> doAddColumnDefault(doc, name, defaultValue));
    }

    /**
     * Adds a new column with values computed from each row
     * 添加值由每行计算得出的新列
     *
     * @param name        the new column name | 新列名
     * @param valueMapper function to compute the value for each row | 计算每行值的函数
     * @return a new CsvTransform with the add column step added | 添加了新列步骤的新CsvTransform
     * @throws NullPointerException if name or valueMapper is null | 如果name或valueMapper为null
     */
    public CsvTransform addColumn(String name, Function<CsvRow, String> valueMapper) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(valueMapper, "valueMapper must not be null");
        return addStep(doc -> doAddColumnMapper(doc, name, valueMapper));
    }

    /**
     * Removes specified columns
     * 移除指定的列
     *
     * @param columns the column names to remove | 要移除的列名
     * @return a new CsvTransform with the remove step added | 添加了移除步骤的新CsvTransform
     * @throws NullPointerException if columns is null | 如果columns为null
     */
    public CsvTransform removeColumns(String... columns) {
        Objects.requireNonNull(columns, "columns must not be null");
        List<String> toRemove = List.of(columns);
        return addStep(doc -> doRemoveColumns(doc, toRemove));
    }

    /**
     * Transforms values in a specific column
     * 转换特定列中的值
     *
     * @param column the column name | 列名
     * @param mapper the transformation function | 转换函数
     * @return a new CsvTransform with the map step added | 添加了映射步骤的新CsvTransform
     * @throws NullPointerException if column or mapper is null | 如果column或mapper为null
     */
    public CsvTransform mapColumn(String column, UnaryOperator<String> mapper) {
        Objects.requireNonNull(column, "column must not be null");
        Objects.requireNonNull(mapper, "mapper must not be null");
        return addStep(doc -> doMapColumn(doc, column, mapper));
    }

    /**
     * Transforms entire rows
     * 转换整行
     *
     * <p>The mapper receives each CsvRow and must return a new CsvRow.
     * Headers remain unchanged.</p>
     * <p>映射器接收每个CsvRow并必须返回一个新的CsvRow。标题保持不变。</p>
     *
     * @param mapper the row transformation function | 行转换函数
     * @return a new CsvTransform with the map step added | 添加了映射步骤的新CsvTransform
     * @throws NullPointerException if mapper is null | 如果mapper为null
     */
    public CsvTransform mapRows(UnaryOperator<CsvRow> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        return addStep(doc -> doMapRows(doc, mapper));
    }

    /**
     * Keeps only columns whose header names match the predicate
     * 仅保留标题名匹配谓词的列
     *
     * @param headerPredicate the predicate to test header names | 测试标题名的谓词
     * @return a new CsvTransform with the filter step added | 添加了过滤步骤的新CsvTransform
     * @throws NullPointerException if headerPredicate is null | 如果headerPredicate为null
     */
    public CsvTransform filterColumns(Predicate<String> headerPredicate) {
        Objects.requireNonNull(headerPredicate, "headerPredicate must not be null");
        return addStep(doc -> doFilterColumns(doc, headerPredicate));
    }

    // ==================== Terminal | 终端操作 ====================

    /**
     * Executes all transformation steps and returns the result
     * 执行所有转换步骤并返回结果
     *
     * @return the transformed document | 转换后的文档
     * @throws OpenCsvException if a transformation step fails | 如果转换步骤失败
     */
    public CsvDocument execute() {
        CsvDocument current = document;
        for (Function<CsvDocument, CsvDocument> step : steps) {
            current = step.apply(current);
        }
        return current;
    }

    // ==================== Internal | 内部方法 ====================

    private CsvTransform addStep(Function<CsvDocument, CsvDocument> step) {
        List<Function<CsvDocument, CsvDocument>> newSteps = new ArrayList<>(steps);
        newSteps.add(step);
        return new CsvTransform(document, List.copyOf(newSteps));
    }

    private static int requireColumnIndex(List<String> headers, String column) {
        int index = headers.indexOf(column);
        if (index < 0) {
            throw new OpenCsvException("Column not found: " + column);
        }
        return index;
    }

    private static CsvDocument doRenameColumn(CsvDocument doc, String oldName, String newName) {
        if (newName.isBlank()) {
            throw new OpenCsvException("New column name must not be blank");
        }
        List<String> headers = doc.headers();
        requireColumnIndex(headers, oldName);

        List<String> newHeaders = new ArrayList<>(headers);
        newHeaders.set(newHeaders.indexOf(oldName), newName);

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            builder.addRow(row);
        }
        return builder.build();
    }

    private static CsvDocument doRenameColumns(CsvDocument doc, Map<String, String> mapping) {
        List<String> headers = doc.headers();
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            requireColumnIndex(headers, entry.getKey());
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                throw new OpenCsvException("New column name must not be blank for: " + entry.getKey());
            }
        }

        List<String> newHeaders = new ArrayList<>(headers);
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            int idx = newHeaders.indexOf(entry.getKey());
            if (idx >= 0) {
                newHeaders.set(idx, entry.getValue());
            }
        }

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            builder.addRow(row);
        }
        return builder.build();
    }

    private static CsvDocument doReorderColumns(CsvDocument doc, List<String> columnOrder) {
        // Reject duplicate column names to prevent silent data duplication
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String col : columnOrder) {
            if (!seen.add(col)) {
                throw new OpenCsvException("Duplicate column in reorder: " + col);
            }
        }
        List<String> headers = doc.headers();
        int[] indices = new int[columnOrder.size()];
        for (int i = 0; i < columnOrder.size(); i++) {
            indices[i] = requireColumnIndex(headers, columnOrder.get(i));
        }

        CsvDocument.Builder builder = CsvDocument.builder().header(columnOrder);
        for (CsvRow row : doc.rows()) {
            String[] fields = new String[indices.length];
            for (int i = 0; i < indices.length; i++) {
                fields[i] = indices[i] < row.size() ? row.get(indices[i]) : null;
            }
            builder.addRow(fields);
        }
        return builder.build();
    }

    private static CsvDocument doAddColumnDefault(CsvDocument doc, String name, String defaultValue) {
        List<String> newHeaders = new ArrayList<>(doc.headers());
        newHeaders.add(name);

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            List<String> fields = new ArrayList<>(row.values());
            fields.add(defaultValue);
            builder.addRow(fields.toArray(new String[0]));
        }
        return builder.build();
    }

    private static CsvDocument doAddColumnMapper(CsvDocument doc, String name,
                                                  Function<CsvRow, String> valueMapper) {
        List<String> newHeaders = new ArrayList<>(doc.headers());
        newHeaders.add(name);

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            List<String> fields = new ArrayList<>(row.values());
            fields.add(valueMapper.apply(row));
            builder.addRow(fields.toArray(new String[0]));
        }
        return builder.build();
    }

    private static CsvDocument doRemoveColumns(CsvDocument doc, List<String> toRemove) {
        List<String> headers = doc.headers();
        List<Integer> keepIndices = new ArrayList<>();
        List<String> newHeaders = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (!toRemove.contains(headers.get(i))) {
                keepIndices.add(i);
                newHeaders.add(headers.get(i));
            }
        }

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            String[] fields = new String[keepIndices.size()];
            for (int i = 0; i < keepIndices.size(); i++) {
                int idx = keepIndices.get(i);
                fields[i] = idx < row.size() ? row.get(idx) : null;
            }
            builder.addRow(fields);
        }
        return builder.build();
    }

    private static CsvDocument doMapColumn(CsvDocument doc, String column, UnaryOperator<String> mapper) {
        List<String> headers = doc.headers();
        int colIndex = requireColumnIndex(headers, column);

        CsvDocument.Builder builder = CsvDocument.builder().header(headers);
        for (CsvRow row : doc.rows()) {
            List<String> fields = new ArrayList<>(row.values());
            // Pad if needed
            while (fields.size() <= colIndex) {
                fields.add(null);
            }
            fields.set(colIndex, mapper.apply(fields.get(colIndex)));
            builder.addRow(fields.toArray(new String[0]));
        }
        return builder.build();
    }

    private static CsvDocument doMapRows(CsvDocument doc, UnaryOperator<CsvRow> mapper) {
        CsvDocument.Builder builder = CsvDocument.builder().header(doc.headers());
        for (CsvRow row : doc.rows()) {
            CsvRow mapped = mapper.apply(row);
            Objects.requireNonNull(mapped, "Row mapper must not return null");
            builder.addRow(mapped);
        }
        return builder.build();
    }

    private static CsvDocument doFilterColumns(CsvDocument doc, Predicate<String> headerPredicate) {
        List<String> headers = doc.headers();
        List<Integer> keepIndices = new ArrayList<>();
        List<String> newHeaders = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            if (headerPredicate.test(headers.get(i))) {
                keepIndices.add(i);
                newHeaders.add(headers.get(i));
            }
        }

        CsvDocument.Builder builder = CsvDocument.builder().header(newHeaders);
        for (CsvRow row : doc.rows()) {
            String[] fields = new String[keepIndices.size()];
            for (int i = 0; i < keepIndices.size(); i++) {
                int idx = keepIndices.get(i);
                fields[i] = idx < row.size() ? row.get(idx) : null;
            }
            builder.addRow(fields);
        }
        return builder.build();
    }
}
