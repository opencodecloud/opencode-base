package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableTable - Immutable Table (Row-Column-Value) Implementation
 * ImmutableTable - 不可变表格（行-列-值）实现
 *
 * <p>A two-dimensional map-like data structure that uses row and column keys to store values.
 * Cannot be modified after creation.</p>
 * <p>使用行和列键存储值的二维类映射数据结构。创建后不能修改。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Null-safe (nulls not allowed) - 空值安全（不允许空值）</li>
 *   <li>Two-dimensional structure - 二维结构</li>
 *   <li>O(1) cell access - O(1) 单元格访问</li>
 *   <li>Row and column views - 行和列视图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create table - 创建表格
 * ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
 *     .put("row1", "col1", 1)
 *     .put("row1", "col2", 2)
 *     .put("row2", "col1", 3)
 *     .put("row2", "col2", 4)
 *     .build();
 *
 * // Access cell - 访问单元格
 * Integer value = table.get("row1", "col1"); // Returns 1 - 返回 1
 *
 * // Check existence - 检查存在性
 * boolean exists = table.contains("row1", "col1"); // Returns true - 返回 true
 *
 * // Get row - 获取行
 * Map<String, Integer> row = table.row("row1"); // Returns {col1=1, col2=2}
 *
 * // Get column - 获取列
 * Map<String, Integer> column = table.column("col1"); // Returns {row1=1, row2=3}
 *
 * // Get all rows/columns - 获取所有行/列
 * Set<String> rowKeys = table.rowKeySet();
 * Set<String> columnKeys = table.columnKeySet();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get(row, column): O(1) - get(行, 列): O(1)</li>
 *   <li>contains: O(1) - contains: O(1)</li>
 *   <li>row/column: O(1) map lookup - row/column: O(1) 映射查找</li>
 *   <li>rowKeySet/columnKeySet: O(1) - rowKeySet/columnKeySet: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (nulls not allowed) - 空值安全: 是（不允许空值）</li>
 * </ul>
 *
 * @param <R> row key type | 行键类型
 * @param <C> column key type | 列键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ImmutableTable<R, C, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final ImmutableTable<?, ?, ?> EMPTY = new ImmutableTable<>(Map.of(), Map.of(), Set.of());

    private final Map<R, Map<C, V>> rowMap;
    private final Map<C, Map<R, V>> columnMap;
    private final Set<Cell<R, C, V>> cellSet;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param rowMap    the row map | 行映射
     * @param columnMap the column map | 列映射
     * @param cellSet   the cell set | 单元格集
     */
    private ImmutableTable(Map<R, Map<C, V>> rowMap, Map<C, Map<R, V>> columnMap, Set<Cell<R, C, V>> cellSet) {
        this.rowMap = rowMap;
        this.columnMap = columnMap;
        this.cellSet = cellSet;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable table.
     * 返回空不可变表格。
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     * @return empty immutable table | 空不可变表格
     */
    @SuppressWarnings("unchecked")
    public static <R, C, V> ImmutableTable<R, C, V> of() {
        return (ImmutableTable<R, C, V>) EMPTY;
    }

    /**
     * Return an immutable table containing the given cell.
     * 返回包含给定单元格的不可变表格。
     *
     * @param <R>    row key type | 行键类型
     * @param <C>    column key type | 列键类型
     * @param <V>    value type | 值类型
     * @param rowKey the row key | 行键
     * @param colKey the column key | 列键
     * @param value  the value | 值
     * @return immutable table | 不可变表格
     */
    public static <R, C, V> ImmutableTable<R, C, V> of(R rowKey, C colKey, V value) {
        return ImmutableTable.<R, C, V>builder()
                .put(rowKey, colKey, value)
                .build();
    }

    /**
     * Return a new builder.
     * 返回新构建器。
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <R, C, V> Builder<R, C, V> builder() {
        return new Builder<>();
    }

    // ==================== Table 查询方法 | Table Query Methods ====================

    /**
     * Return the value at the given row and column.
     * 返回给定行和列处的值。
     *
     * @param rowKey the row key | 行键
     * @param colKey the column key | 列键
     * @return the value, or null if not present | 值，如果不存在则返回 null
     */
    public V get(Object rowKey, Object colKey) {
        if (rowKey == null || colKey == null) {
            return null;
        }
        Map<C, V> row = rowMap.get(rowKey);
        return row == null ? null : row.get(colKey);
    }

    /**
     * Check if the table contains the given row and column.
     * 检查表格是否包含给定的行和列。
     *
     * @param rowKey the row key | 行键
     * @param colKey the column key | 列键
     * @return true if present | 如果存在则返回 true
     */
    public boolean contains(Object rowKey, Object colKey) {
        if (rowKey == null || colKey == null) {
            return false;
        }
        Map<C, V> row = rowMap.get(rowKey);
        return row != null && row.containsKey(colKey);
    }

    /**
     * Check if the table contains the given row key.
     * 检查表格是否包含给定的行键。
     *
     * @param rowKey the row key | 行键
     * @return true if present | 如果存在则返回 true
     */
    public boolean containsRow(Object rowKey) {
        return rowKey != null && rowMap.containsKey(rowKey);
    }

    /**
     * Check if the table contains the given column key.
     * 检查表格是否包含给定的列键。
     *
     * @param colKey the column key | 列键
     * @return true if present | 如果存在则返回 true
     */
    public boolean containsColumn(Object colKey) {
        return colKey != null && columnMap.containsKey(colKey);
    }

    /**
     * Check if the table contains the given value.
     * 检查表格是否包含给定的值。
     *
     * @param value the value | 值
     * @return true if present | 如果存在则返回 true
     */
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        for (Map<C, V> row : rowMap.values()) {
            if (row.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Table 视图方法 | Table View Methods ====================

    /**
     * Return the row map for the given row key.
     * 返回给定行键的行映射。
     *
     * @param rowKey the row key | 行键
     * @return unmodifiable map of columns to values | 列到值的不可修改映射
     */
    public Map<C, V> row(R rowKey) {
        Map<C, V> row = rowMap.get(rowKey);
        return row == null ? Collections.emptyMap() : Collections.unmodifiableMap(row);
    }

    /**
     * Return the column map for the given column key.
     * 返回给定列键的列映射。
     *
     * @param colKey the column key | 列键
     * @return unmodifiable map of rows to values | 行到值的不可修改映射
     */
    public Map<R, V> column(C colKey) {
        Map<R, V> column = columnMap.get(colKey);
        return column == null ? Collections.emptyMap() : Collections.unmodifiableMap(column);
    }

    /**
     * Return the map of all rows.
     * 返回所有行的映射。
     *
     * @return unmodifiable map of row keys to row maps | 行键到行映射的不可修改映射
     */
    public Map<R, Map<C, V>> rowMap() {
        Map<R, Map<C, V>> result = new LinkedHashMap<>();
        for (Map.Entry<R, Map<C, V>> entry : rowMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Return the map of all columns.
     * 返回所有列的映射。
     *
     * @return unmodifiable map of column keys to column maps | 列键到列映射的不可修改映射
     */
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> result = new LinkedHashMap<>();
        for (Map.Entry<C, Map<R, V>> entry : columnMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Return the set of all row keys.
     * 返回所有行键的集合。
     *
     * @return unmodifiable set of row keys | 行键的不可修改集合
     */
    public Set<R> rowKeySet() {
        return Collections.unmodifiableSet(rowMap.keySet());
    }

    /**
     * Return the set of all column keys.
     * 返回所有列键的集合。
     *
     * @return unmodifiable set of column keys | 列键的不可修改集合
     */
    public Set<C> columnKeySet() {
        return Collections.unmodifiableSet(columnMap.keySet());
    }

    /**
     * Return the collection of all values.
     * 返回所有值的集合。
     *
     * @return unmodifiable collection of values | 值的不可修改集合
     */
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (Map<C, V> row : rowMap.values()) {
            values.addAll(row.values());
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Return the set of all cells.
     * 返回所有单元格的集合。
     *
     * @return unmodifiable set of cells | 单元格的不可修改集合
     */
    public Set<Cell<R, C, V>> cellSet() {
        return Collections.unmodifiableSet(cellSet);
    }

    // ==================== Table 属性 | Table Properties ====================

    /**
     * Return the number of cells in the table.
     * 返回表格中的单元格数。
     *
     * @return the size | 大小
     */
    public int size() {
        return cellSet.size();
    }

    /**
     * Check if the table is empty.
     * 检查表格是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return cellSet.isEmpty();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableTable<?, ?, ?> that)) return false;
        return cellSet.equals(that.cellSet);
    }

    @Override
    public int hashCode() {
        return cellSet.hashCode();
    }

    @Override
    public String toString() {
        return rowMap.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Cell interface representing a table cell
     * 表示表格单元格的单元格接口
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     */
    public interface Cell<R, C, V> {
        /**
         * Return the row key.
         * 返回行键。
         *
         * @return the row key | 行键
         */
        R getRowKey();

        /**
         * Return the column key.
         * 返回列键。
         *
         * @return the column key | 列键
         */
        C getColumnKey();

        /**
         * Return the value.
         * 返回值。
         *
         * @return the value | 值
         */
        V getValue();
    }

    /**
     * Cell implementation
     */
    private static class CellImpl<R, C, V> implements Cell<R, C, V>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final R rowKey;
        private final C columnKey;
        private final V value;

        CellImpl(R rowKey, C columnKey, V value) {
            this.rowKey = rowKey;
            this.columnKey = columnKey;
            this.value = value;
        }

        @Override
        public R getRowKey() {
            return rowKey;
        }

        @Override
        public C getColumnKey() {
            return columnKey;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell<?, ?, ?> cell)) return false;
            return Objects.equals(rowKey, cell.getRowKey()) &&
                    Objects.equals(columnKey, cell.getColumnKey()) &&
                    Objects.equals(value, cell.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(rowKey, columnKey, value);
        }

        @Override
        public String toString() {
            return "(" + rowKey + "," + columnKey + ")=" + value;
        }
    }

    // ==================== 构建器 | Builder ====================

    /**
     * Builder for ImmutableTable
     * ImmutableTable 构建器
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     */
    public static final class Builder<R, C, V> {
        private final Map<R, Map<C, V>> rowMap = new LinkedHashMap<>();
        private final Map<C, Map<R, V>> columnMap = new LinkedHashMap<>();
        private final Set<Cell<R, C, V>> cellSet = new LinkedHashSet<>();

        private Builder() {
        }

        /**
         * Put a cell in the table.
         * 在表格中放置单元格。
         *
         * @param rowKey the row key | 行键
         * @param colKey the column key | 列键
         * @param value  the value | 值
         * @return this builder | 此构建器
         * @throws OpenCollectionException if the cell already exists | 如果单元格已存在
         */
        public Builder<R, C, V> put(R rowKey, C colKey, V value) {
            Objects.requireNonNull(rowKey, "Row key cannot be null");
            Objects.requireNonNull(colKey, "Column key cannot be null");
            Objects.requireNonNull(value, "Value cannot be null");

            // Check if cell already exists
            Map<C, V> row = rowMap.get(rowKey);
            if (row != null && row.containsKey(colKey)) {
                throw new OpenCollectionException("Cell (" + rowKey + ", " + colKey + ") already exists");
            }

            // Add to row map
            rowMap.computeIfAbsent(rowKey, k -> new LinkedHashMap<>()).put(colKey, value);

            // Add to column map
            columnMap.computeIfAbsent(colKey, k -> new LinkedHashMap<>()).put(rowKey, value);

            // Add to cell set
            cellSet.add(new CellImpl<>(rowKey, colKey, value));

            return this;
        }

        /**
         * Put a cell in the table.
         * 在表格中放置单元格。
         *
         * @param cell the cell | 单元格
         * @return this builder | 此构建器
         */
        public Builder<R, C, V> put(Cell<? extends R, ? extends C, ? extends V> cell) {
            return put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }

        /**
         * Put all cells from another table.
         * 从另一个表格放置所有单元格。
         *
         * @param table the table | 表格
         * @return this builder | 此构建器
         */
        public Builder<R, C, V> putAll(ImmutableTable<? extends R, ? extends C, ? extends V> table) {
            for (Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                put(cell);
            }
            return this;
        }

        /**
         * Build the immutable table.
         * 构建不可变表格。
         *
         * @return immutable table | 不可变表格
         */
        public ImmutableTable<R, C, V> build() {
            if (cellSet.isEmpty()) {
                return of();
            }

            // Create defensive copies
            Map<R, Map<C, V>> rowMapCopy = new LinkedHashMap<>();
            for (Map.Entry<R, Map<C, V>> entry : rowMap.entrySet()) {
                rowMapCopy.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
            }

            Map<C, Map<R, V>> columnMapCopy = new LinkedHashMap<>();
            for (Map.Entry<C, Map<R, V>> entry : columnMap.entrySet()) {
                columnMapCopy.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
            }

            Set<Cell<R, C, V>> cellSetCopy = new LinkedHashSet<>(cellSet);

            return new ImmutableTable<>(rowMapCopy, columnMapCopy, cellSetCopy);
        }
    }
}
