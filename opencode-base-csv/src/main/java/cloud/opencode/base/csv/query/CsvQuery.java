package cloud.opencode.base.csv.query;

import cloud.opencode.base.csv.CsvDocument;
import cloud.opencode.base.csv.CsvRow;
import cloud.opencode.base.csv.exception.OpenCsvException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CSV Query - Fluent, immutable query builder for CsvDocument
 * CSV查询 - CsvDocument的流式不可变查询构建器
 *
 * <p>Provides a SQL-like query API for filtering, sorting, projecting, and grouping
 * CSV data. Each intermediate method returns a new CsvQuery instance without
 * mutating the original, following the immutable builder pattern. Terminal operations
 * execute the query pipeline lazily and return results.</p>
 * <p>提供类似SQL的查询API，用于过滤、排序、投影和分组CSV数据。每个中间方法返回一个新的
 * CsvQuery实例而不改变原始对象，遵循不可变构建器模式。终端操作延迟执行查询管道并返回结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable query chain - 不可变查询链</li>
 *   <li>Lazy execution on terminal operations - 终端操作时延迟执行</li>
 *   <li>Column projection (select) - 列投影（select）</li>
 *   <li>Row filtering (where) - 行过滤（where）</li>
 *   <li>Sorting with custom comparators - 自定义比较器排序</li>
 *   <li>Pagination (limit/offset) - 分页（limit/offset）</li>
 *   <li>Deduplication (distinct) - 去重（distinct）</li>
 *   <li>Grouping and counting - 分组和计数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CsvDocument result = CsvQuery.from(doc)
 *     .where(row -> !"".equals(row.get(0)))
 *     .select("name", "age")
 *     .orderBy("age", true)
 *     .limit(10)
 *     .execute();
 *
 * long count = CsvQuery.from(doc)
 *     .where(row -> Integer.parseInt(row.get(1)) > 25)
 *     .count();
 *
 * Map<String, CsvDocument> groups = CsvQuery.from(doc)
 *     .groupBy("role");
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
public final class CsvQuery {

    private final CsvDocument document;
    private final List<String> selectColumns;
    private final List<Predicate<CsvRow>> filters;
    private final String orderByColumn;
    private final Comparator<String> orderByComparator;
    private final boolean orderAscending;
    private final int limitRows;
    private final int offsetRows;
    private final boolean distinctAll;
    private final List<String> distinctColumns;

    private CsvQuery(CsvDocument document,
                     List<String> selectColumns,
                     List<Predicate<CsvRow>> filters,
                     String orderByColumn,
                     Comparator<String> orderByComparator,
                     boolean orderAscending,
                     int limitRows,
                     int offsetRows,
                     boolean distinctAll,
                     List<String> distinctColumns) {
        this.document = document;
        this.selectColumns = selectColumns;
        this.filters = filters;
        this.orderByColumn = orderByColumn;
        this.orderByComparator = orderByComparator;
        this.orderAscending = orderAscending;
        this.limitRows = limitRows;
        this.offsetRows = offsetRows;
        this.distinctAll = distinctAll;
        this.distinctColumns = distinctColumns;
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Creates a query from a CsvDocument
     * 从CsvDocument创建查询
     *
     * @param doc the source document | 源文档
     * @return a new CsvQuery instance | 新的CsvQuery实例
     * @throws NullPointerException if doc is null | 如果doc为null
     */
    public static CsvQuery from(CsvDocument doc) {
        Objects.requireNonNull(doc, "doc must not be null");
        return new CsvQuery(doc, List.of(), List.of(), null, null, true, -1, 0, false, List.of());
    }

    // ==================== Intermediate Operations | 中间操作 ====================

    /**
     * Selects specific columns by header name
     * 按标题名选择特定列
     *
     * <p>Only the specified columns will be included in the result document.
     * Column names are validated when the query executes.</p>
     * <p>结果文档中只包含指定的列。列名在查询执行时验证。</p>
     *
     * @param columns the column names to select | 要选择的列名
     * @return a new CsvQuery with the select applied | 应用了select的新CsvQuery
     * @throws NullPointerException if columns is null | 如果columns为null
     */
    public CsvQuery select(String... columns) {
        Objects.requireNonNull(columns, "columns must not be null");
        return new CsvQuery(document, List.of(columns), filters,
                orderByColumn, orderByComparator, orderAscending,
                limitRows, offsetRows, distinctAll, distinctColumns);
    }

    /**
     * Filters rows by a predicate
     * 按谓词过滤行
     *
     * <p>Multiple where calls are combined with logical AND.</p>
     * <p>多个where调用以逻辑AND组合。</p>
     *
     * @param predicate the filter predicate | 过滤谓词
     * @return a new CsvQuery with the filter added | 添加了过滤器的新CsvQuery
     * @throws NullPointerException if predicate is null | 如果predicate为null
     */
    public CsvQuery where(Predicate<CsvRow> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        List<Predicate<CsvRow>> newFilters = new ArrayList<>(filters);
        newFilters.add(predicate);
        return new CsvQuery(document, selectColumns, List.copyOf(newFilters),
                orderByColumn, orderByComparator, orderAscending,
                limitRows, offsetRows, distinctAll, distinctColumns);
    }

    /**
     * Sorts rows by a column using natural string ordering
     * 按列使用自然字符串排序对行排序
     *
     * <p>Null or missing column values are sorted last when ascending,
     * first when descending.</p>
     * <p>升序时null或缺失的列值排在最后，降序时排在最前。</p>
     *
     * @param column    the column name to sort by | 排序列名
     * @param ascending true for ascending, false for descending | true为升序，false为降序
     * @return a new CsvQuery with sorting applied | 应用了排序的新CsvQuery
     * @throws NullPointerException if column is null | 如果column为null
     */
    public CsvQuery orderBy(String column, boolean ascending) {
        Objects.requireNonNull(column, "column must not be null");
        return new CsvQuery(document, selectColumns, filters,
                column, Comparator.naturalOrder(), ascending,
                limitRows, offsetRows, distinctAll, distinctColumns);
    }

    /**
     * Sorts rows by a column using a custom comparator
     * 按列使用自定义比较器对行排序
     *
     * @param column     the column name to sort by | 排序列名
     * @param comparator the comparator for column values | 列值比较器
     * @return a new CsvQuery with sorting applied | 应用了排序的新CsvQuery
     * @throws NullPointerException if column or comparator is null | 如果column或comparator为null
     */
    public CsvQuery orderBy(String column, Comparator<String> comparator) {
        Objects.requireNonNull(column, "column must not be null");
        Objects.requireNonNull(comparator, "comparator must not be null");
        return new CsvQuery(document, selectColumns, filters,
                column, comparator, true,
                limitRows, offsetRows, distinctAll, distinctColumns);
    }

    /**
     * Limits the number of result rows
     * 限制结果行数
     *
     * @param maxRows the maximum number of rows | 最大行数
     * @return a new CsvQuery with the limit applied | 应用了限制的新CsvQuery
     * @throws IllegalArgumentException if maxRows is negative | 如果maxRows为负数
     */
    public CsvQuery limit(int maxRows) {
        if (maxRows < 0) {
            throw new IllegalArgumentException("maxRows must not be negative: " + maxRows);
        }
        return new CsvQuery(document, selectColumns, filters,
                orderByColumn, orderByComparator, orderAscending,
                maxRows, offsetRows, distinctAll, distinctColumns);
    }

    /**
     * Skips the first N rows of the result
     * 跳过结果的前N行
     *
     * @param skipRows the number of rows to skip | 要跳过的行数
     * @return a new CsvQuery with the offset applied | 应用了偏移的新CsvQuery
     * @throws IllegalArgumentException if skipRows is negative | 如果skipRows为负数
     */
    public CsvQuery offset(int skipRows) {
        if (skipRows < 0) {
            throw new IllegalArgumentException("skipRows must not be negative: " + skipRows);
        }
        return new CsvQuery(document, selectColumns, filters,
                orderByColumn, orderByComparator, orderAscending,
                limitRows, skipRows, distinctAll, distinctColumns);
    }

    /**
     * Removes duplicate rows (comparing all field values)
     * 移除重复行（比较所有字段值）
     *
     * <p>Rows are considered duplicates if all their field values are equal,
     * regardless of row number.</p>
     * <p>如果所有字段值相等则认为行重复，不考虑行号。</p>
     *
     * @return a new CsvQuery with distinct applied | 应用了去重的新CsvQuery
     */
    public CsvQuery distinct() {
        return new CsvQuery(document, selectColumns, filters,
                orderByColumn, orderByComparator, orderAscending,
                limitRows, offsetRows, true, List.of());
    }

    /**
     * Removes duplicate rows based on specific columns
     * 基于特定列移除重复行
     *
     * <p>Rows are considered duplicates if the values of the specified columns
     * are all equal. The first occurrence of each group is kept.</p>
     * <p>如果指定列的值都相等则认为行重复。保留每组的第一个出现。</p>
     *
     * @param columns the column names to check for duplicates | 检查重复的列名
     * @return a new CsvQuery with distinct applied | 应用了去重的新CsvQuery
     * @throws NullPointerException if columns is null | 如果columns为null
     */
    public CsvQuery distinct(String... columns) {
        Objects.requireNonNull(columns, "columns must not be null");
        return new CsvQuery(document, selectColumns, filters,
                orderByColumn, orderByComparator, orderAscending,
                limitRows, offsetRows, false, List.of(columns));
    }

    // ==================== Terminal Operations | 终端操作 ====================

    /**
     * Executes the query and returns the result as a CsvDocument
     * 执行查询并将结果返回为CsvDocument
     *
     * @return the result document | 结果文档
     * @throws OpenCsvException if a referenced column does not exist | 如果引用的列不存在
     */
    public CsvDocument execute() {
        List<String> headers = document.headers();

        // 1. Filter rows
        Stream<CsvRow> stream = document.rows().stream();
        for (Predicate<CsvRow> filter : filters) {
            stream = stream.filter(filter);
        }
        List<CsvRow> rows = stream.collect(Collectors.toCollection(ArrayList::new));

        // 2. Distinct (before sort, on full or partial columns)
        if (distinctAll) {
            rows = applyDistinctAll(rows);
        } else if (!distinctColumns.isEmpty()) {
            rows = applyDistinctColumns(rows, headers, distinctColumns);
        }

        // 3. Sort
        if (orderByColumn != null) {
            int colIndex = resolveColumnIndex(headers, orderByColumn);
            Comparator<CsvRow> rowComparator = buildRowComparator(colIndex, orderByComparator, orderAscending);
            rows.sort(rowComparator);
        }

        // 4. Offset + Limit (single subList to avoid double copy)
        int from = Math.min(offsetRows > 0 ? offsetRows : 0, rows.size());
        int to = limitRows >= 0 ? Math.min(from + limitRows, rows.size()) : rows.size();
        if (from > 0 || to < rows.size()) {
            rows = new ArrayList<>(rows.subList(from, to));
        }

        // 6. Select columns
        if (!selectColumns.isEmpty()) {
            return projectColumns(headers, rows, selectColumns);
        }

        return buildDocument(headers, rows);
    }

    /**
     * Counts the number of matching rows
     * 计算匹配行数
     *
     * <p>Equivalent to {@code execute().rowCount()}.</p>
     * <p>等效于 {@code execute().rowCount()}。</p>
     *
     * @return the count of matching rows | 匹配行数
     * @throws OpenCsvException if a referenced column does not exist | 如果引用的列不存在
     */
    public long count() {
        return execute().rowCount();
    }

    /**
     * Extracts the values of a single column from matching rows
     * 从匹配行中提取单列的值
     *
     * @param name the column name | 列名
     * @return list of column values | 列值列表
     * @throws NullPointerException if name is null | 如果name为null
     * @throws OpenCsvException     if the column does not exist | 如果列不存在
     */
    public List<String> column(String name) {
        Objects.requireNonNull(name, "name must not be null");
        CsvDocument result = execute();
        int colIndex = resolveColumnIndex(result.headers(), name);
        List<String> values = new ArrayList<>(result.rowCount());
        for (CsvRow row : result.rows()) {
            values.add(colIndex < row.size() ? row.get(colIndex) : null);
        }
        return List.copyOf(values);
    }

    /**
     * Groups matching rows by a column value
     * 按列值对匹配行分组
     *
     * <p>Returns a LinkedHashMap preserving insertion order (order of first occurrence).
     * Each group is a CsvDocument with the same headers as the source.</p>
     * <p>返回保持插入顺序的LinkedHashMap（首次出现的顺序）。
     * 每个组是与源具有相同标题的CsvDocument。</p>
     *
     * @param column the column name to group by | 分组列名
     * @return map of column value to grouped document | 列值到分组文档的映射
     * @throws NullPointerException if column is null | 如果column为null
     * @throws OpenCsvException     if the column does not exist | 如果列不存在
     */
    public Map<String, CsvDocument> groupBy(String column) {
        Objects.requireNonNull(column, "column must not be null");
        CsvDocument result = execute();
        List<String> headers = result.headers();
        int colIndex = resolveColumnIndex(headers, column);

        Map<String, List<CsvRow>> groups = new LinkedHashMap<>();
        for (CsvRow row : result.rows()) {
            String key = colIndex < row.size() ? row.get(colIndex) : "";
            groups.computeIfAbsent(key, _ -> new ArrayList<>()).add(row);
        }

        Map<String, CsvDocument> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<CsvRow>> entry : groups.entrySet()) {
            resultMap.put(entry.getKey(), buildDocument(headers, entry.getValue()));
        }
        return resultMap;
    }

    /**
     * Counts matching rows grouped by a column value
     * 按列值分组计算匹配行数
     *
     * @param column the column name to count by | 计数列名
     * @return map of column value to count | 列值到计数的映射
     * @throws NullPointerException if column is null | 如果column为null
     * @throws OpenCsvException     if the column does not exist | 如果列不存在
     */
    public Map<String, Long> countBy(String column) {
        Objects.requireNonNull(column, "column must not be null");
        CsvDocument result = execute();
        List<String> headers = result.headers();
        int colIndex = resolveColumnIndex(headers, column);

        Map<String, Long> counts = new LinkedHashMap<>();
        for (CsvRow row : result.rows()) {
            String key = colIndex < row.size() ? row.get(colIndex) : "";
            counts.merge(key, 1L, Long::sum);
        }
        return counts;
    }

    // ==================== Internal | 内部方法 ====================

    private static int resolveColumnIndex(List<String> headers, String column) {
        int index = headers.indexOf(column);
        if (index < 0) {
            throw new OpenCsvException("Column not found: " + column);
        }
        return index;
    }

    private static List<CsvRow> applyDistinctAll(List<CsvRow> rows) {
        LinkedHashSet<List<String>> seen = new LinkedHashSet<>();
        List<CsvRow> result = new ArrayList<>();
        for (CsvRow row : rows) {
            if (seen.add(row.values())) {
                result.add(row);
            }
        }
        return result;
    }

    private static List<CsvRow> applyDistinctColumns(List<CsvRow> rows, List<String> headers,
                                                      List<String> columns) {
        int[] indices = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            indices[i] = resolveColumnIndex(headers, columns.get(i));
        }

        LinkedHashSet<List<String>> seen = new LinkedHashSet<>();
        List<CsvRow> result = new ArrayList<>();
        for (CsvRow row : rows) {
            List<String> key = new ArrayList<>(indices.length);
            for (int idx : indices) {
                key.add(idx < row.size() ? row.get(idx) : null);
            }
            if (seen.add(key)) {
                result.add(row);
            }
        }
        return result;
    }

    private static Comparator<CsvRow> buildRowComparator(int colIndex,
                                                          Comparator<String> valueComparator,
                                                          boolean ascending) {
        Comparator<CsvRow> cmp = (a, b) -> {
            String va = colIndex < a.size() ? a.get(colIndex) : null;
            String vb = colIndex < b.size() ? b.get(colIndex) : null;
            if (va == null && vb == null) return 0;
            if (va == null) return 1;  // nulls last in ascending
            if (vb == null) return -1;
            return valueComparator.compare(va, vb);
        };
        return ascending ? cmp : cmp.reversed();
    }

    private static CsvDocument projectColumns(List<String> headers, List<CsvRow> rows,
                                               List<String> selectCols) {
        int[] indices = new int[selectCols.size()];
        for (int i = 0; i < selectCols.size(); i++) {
            indices[i] = resolveColumnIndex(headers, selectCols.get(i));
        }

        CsvDocument.Builder builder = CsvDocument.builder().header(selectCols);
        for (CsvRow row : rows) {
            String[] projected = new String[indices.length];
            for (int i = 0; i < indices.length; i++) {
                projected[i] = indices[i] < row.size() ? row.get(indices[i]) : null;
            }
            builder.addRow(projected);
        }
        return builder.build();
    }

    private static CsvDocument buildDocument(List<String> headers, List<CsvRow> rows) {
        CsvDocument.Builder builder = CsvDocument.builder().header(headers);
        for (CsvRow row : rows) {
            builder.addRow(row);
        }
        return builder.build();
    }
}
