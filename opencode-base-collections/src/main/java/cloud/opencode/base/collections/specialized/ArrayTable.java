package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Table;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ArrayTable - Fixed-size Array-based Table Implementation
 * ArrayTable - 固定大小基于数组的表实现
 *
 * <p>A table implementation using a 2D array with fixed row and column keys.</p>
 * <p>使用具有固定行和列键的二维数组的表实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed dimensions - 固定维度</li>
 *   <li>O(1) access - O(1) 访问</li>
 *   <li>Dense storage - 密集存储</li>
 *   <li>Allows null values - 允许空值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ArrayTable<String, String, Integer> table = ArrayTable.create(
 *     Arrays.asList("row1", "row2"),
 *     Arrays.asList("col1", "col2"));
 *
 * table.put("row1", "col1", 1);
 * table.put("row1", "col2", 2);
 *
 * Integer value = table.get("row1", "col1");  // 1
 *
 * // Direct array access
 * Integer[][] array = table.toArray();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>put: O(1) - put: O(1)</li>
 *   <li>containsRow/Column: O(1) - containsRow/Column: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Values can be null - 空值安全: 值可以为 null</li>
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
public final class ArrayTable<R, C, V> implements Table<R, C, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<R> rowKeys;
    private final List<C> columnKeys;
    private final Map<R, Integer> rowKeyToIndex;
    private final Map<C, Integer> columnKeyToIndex;
    private final Object[][] values;

    // ==================== 构造方法 | Constructors ====================

    private ArrayTable(Iterable<? extends R> rowKeys, Iterable<? extends C> columnKeys) {
        this.rowKeys = new ArrayList<>();
        this.columnKeys = new ArrayList<>();
        this.rowKeyToIndex = new LinkedHashMap<>();
        this.columnKeyToIndex = new LinkedHashMap<>();

        int rowIndex = 0;
        for (R rowKey : rowKeys) {
            Objects.requireNonNull(rowKey, "Row key cannot be null");
            if (rowKeyToIndex.containsKey(rowKey)) {
                throw new IllegalArgumentException("Duplicate row key: " + rowKey);
            }
            this.rowKeys.add(rowKey);
            this.rowKeyToIndex.put(rowKey, rowIndex++);
        }

        int columnIndex = 0;
        for (C columnKey : columnKeys) {
            Objects.requireNonNull(columnKey, "Column key cannot be null");
            if (columnKeyToIndex.containsKey(columnKey)) {
                throw new IllegalArgumentException("Duplicate column key: " + columnKey);
            }
            this.columnKeys.add(columnKey);
            this.columnKeyToIndex.put(columnKey, columnIndex++);
        }

        this.values = new Object[this.rowKeys.size()][this.columnKeys.size()];
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an ArrayTable with the specified row and column keys.
     * 使用指定的行和列键创建 ArrayTable。
     *
     * @param <R>        row key type | 行键类型
     * @param <C>        column key type | 列键类型
     * @param <V>        value type | 值类型
     * @param rowKeys    the row keys | 行键
     * @param columnKeys the column keys | 列键
     * @return new ArrayTable | 新 ArrayTable
     */
    public static <R, C, V> ArrayTable<R, C, V> create(
            Iterable<? extends R> rowKeys, Iterable<? extends C> columnKeys) {
        return new ArrayTable<>(rowKeys, columnKeys);
    }

    /**
     * Create an ArrayTable copying from another table.
     * 从另一个表复制创建 ArrayTable。
     *
     * @param <R>   row key type | 行键类型
     * @param <C>   column key type | 列键类型
     * @param <V>   value type | 值类型
     * @param table the table to copy | 要复制的表
     * @return new ArrayTable | 新 ArrayTable
     */
    public static <R, C, V> ArrayTable<R, C, V> create(Table<R, C, V> table) {
        ArrayTable<R, C, V> result = create(table.rowKeySet(), table.columnKeySet());
        result.putAll(table);
        return result;
    }

    // ==================== Table 实现 | Table Implementation ====================

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object rowKey, Object columnKey) {
        Integer rowIndex = rowKeyToIndex.get(rowKey);
        Integer columnIndex = columnKeyToIndex.get(columnKey);
        if (rowIndex == null || columnIndex == null) {
            return null;
        }
        return (V) values[rowIndex][columnIndex];
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        return rowKeyToIndex.containsKey(rowKey) && columnKeyToIndex.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return rowKeyToIndex.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        return columnKeyToIndex.containsKey(columnKey);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Object[] row : values) {
            for (Object v : row) {
                if (Objects.equals(v, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(R rowKey, C columnKey, V value) {
        Integer rowIndex = rowKeyToIndex.get(rowKey);
        Integer columnIndex = columnKeyToIndex.get(columnKey);
        if (rowIndex == null) {
            throw new IllegalArgumentException("Row key not in table: " + rowKey);
        }
        if (columnIndex == null) {
            throw new IllegalArgumentException("Column key not in table: " + columnKey);
        }
        V oldValue = (V) values[rowIndex][columnIndex];
        values[rowIndex][columnIndex] = value;
        return oldValue;
    }

    @Override
    public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        for (Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
            put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object rowKey, Object columnKey) {
        Integer rowIndex = rowKeyToIndex.get(rowKey);
        Integer columnIndex = columnKeyToIndex.get(columnKey);
        if (rowIndex == null || columnIndex == null) {
            return null;
        }
        V oldValue = (V) values[rowIndex][columnIndex];
        values[rowIndex][columnIndex] = null;
        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<C, V> row(R rowKey) {
        Integer rowIndex = rowKeyToIndex.get(rowKey);
        if (rowIndex == null) {
            return Collections.emptyMap();
        }
        Map<C, V> row = new LinkedHashMap<>();
        for (int i = 0; i < columnKeys.size(); i++) {
            V value = (V) values[rowIndex][i];
            if (value != null) {
                row.put(columnKeys.get(i), value);
            }
        }
        return Collections.unmodifiableMap(row);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<R, V> column(C columnKey) {
        Integer columnIndex = columnKeyToIndex.get(columnKey);
        if (columnIndex == null) {
            return Collections.emptyMap();
        }
        Map<R, V> column = new LinkedHashMap<>();
        for (int i = 0; i < rowKeys.size(); i++) {
            V value = (V) values[i][columnIndex];
            if (value != null) {
                column.put(rowKeys.get(i), value);
            }
        }
        return Collections.unmodifiableMap(column);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Cell<R, C, V>> cellSet() {
        Set<Cell<R, C, V>> cells = new LinkedHashSet<>();
        for (int i = 0; i < rowKeys.size(); i++) {
            for (int j = 0; j < columnKeys.size(); j++) {
                V value = (V) values[i][j];
                if (value != null) {
                    cells.add(new CellImpl<>(rowKeys.get(i), columnKeys.get(j), value));
                }
            }
        }
        return Collections.unmodifiableSet(cells);
    }

    @Override
    public Set<R> rowKeySet() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(rowKeys));
    }

    @Override
    public Set<C> columnKeySet() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(columnKeys));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        List<V> result = new ArrayList<>();
        for (Object[] row : values) {
            for (Object v : row) {
                if (v != null) {
                    result.add((V) v);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
        Map<R, Map<C, V>> result = new LinkedHashMap<>();
        for (R rowKey : rowKeys) {
            result.put(rowKey, row(rowKey));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> result = new LinkedHashMap<>();
        for (C columnKey : columnKeys) {
            result.put(columnKey, column(columnKey));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public int size() {
        int count = 0;
        for (Object[] row : values) {
            for (Object v : row) {
                if (v != null) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        for (Object[] row : values) {
            Arrays.fill(row, null);
        }
    }

    // ==================== 额外方法 | Additional Methods ====================

    /**
     * Return all row keys in order.
     * 按顺序返回所有行键。
     *
     * @return the row keys | 行键
     */
    public List<R> rowKeyList() {
        return Collections.unmodifiableList(rowKeys);
    }

    /**
     * Return all column keys in order.
     * 按顺序返回所有列键。
     *
     * @return the column keys | 列键
     */
    public List<C> columnKeyList() {
        return Collections.unmodifiableList(columnKeys);
    }

    /**
     * Return the value at the given indices.
     * 返回给定索引处的值。
     *
     * @param rowIndex    the row index | 行索引
     * @param columnIndex the column index | 列索引
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public V at(int rowIndex, int columnIndex) {
        return (V) values[rowIndex][columnIndex];
    }

    /**
     * Set the value at the given indices.
     * 设置给定索引处的值。
     *
     * @param rowIndex    the row index | 行索引
     * @param columnIndex the column index | 列索引
     * @param value       the value | 值
     * @return the old value | 旧值
     */
    @SuppressWarnings("unchecked")
    public V set(int rowIndex, int columnIndex, V value) {
        V oldValue = (V) values[rowIndex][columnIndex];
        values[rowIndex][columnIndex] = value;
        return oldValue;
    }

    /**
     * Erase the value at the given indices.
     * 擦除给定索引处的值。
     *
     * @param rowIndex    the row index | 行索引
     * @param columnIndex the column index | 列索引
     * @return the old value | 旧值
     */
    @SuppressWarnings("unchecked")
    public V erase(int rowIndex, int columnIndex) {
        V oldValue = (V) values[rowIndex][columnIndex];
        values[rowIndex][columnIndex] = null;
        return oldValue;
    }

    /**
     * Return the values as a 2D array.
     * 以二维数组形式返回值。
     *
     * @param array the array to fill | 要填充的数组
     * @return the filled array | 填充后的数组
     */
    @SuppressWarnings("unchecked")
    public V[][] toArray(V[][] array) {
        if (array.length < rowKeys.size() || array[0].length < columnKeys.size()) {
            array = (V[][]) java.lang.reflect.Array.newInstance(
                    array.getClass().getComponentType().getComponentType(),
                    rowKeys.size(), columnKeys.size());
        }
        for (int i = 0; i < rowKeys.size(); i++) {
            System.arraycopy(values[i], 0, array[i], 0, columnKeys.size());
        }
        return array;
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table<?, ?, ?> that)) return false;
        return cellSet().equals(that.cellSet());
    }

    @Override
    public int hashCode() {
        return cellSet().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < rowKeys.size(); i++) {
            for (int j = 0; j < columnKeys.size(); j++) {
                if (values[i][j] != null) {
                    if (!first) sb.append(", ");
                    sb.append("(").append(rowKeys.get(i)).append(",")
                            .append(columnKeys.get(j)).append(")=").append(values[i][j]);
                    first = false;
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    private record CellImpl<R, C, V>(R rowKey, C columnKey, V value) implements Cell<R, C, V>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

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
            if (!(o instanceof Cell<?, ?, ?> that)) {
                return false;
            }
            return Objects.equals(rowKey, that.getRowKey())
                    && Objects.equals(columnKey, that.getColumnKey())
                    && Objects.equals(value, that.getValue());
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
}
