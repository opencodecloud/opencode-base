package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Table;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * TreeBasedTable - Tree-based Table Implementation
 * TreeBasedTable - 基于树的表实现
 *
 * <p>A table implementation using TreeMaps for sorted row and column keys.</p>
 * <p>使用 TreeMap 对行和列键进行排序的表实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sorted row and column keys - 排序的行和列键</li>
 *   <li>Natural or custom ordering - 自然或自定义排序</li>
 *   <li>Efficient range queries - 高效范围查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
 * table.put("row1", "col1", 1);
 * table.put("row2", "col2", 2);
 *
 * Integer value = table.get("row1", "col1");  // 1
 *
 * // Rows and columns are sorted
 * for (String row : table.rowKeySet()) {
 *     // Iterates in sorted order
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(log r + log c) where r=rows, c=columns - get: O(log r + log c)</li>
 *   <li>put: O(log r + log c) - put: O(log r + log c)</li>
 *   <li>containsRow/Column: O(log r/c) - containsRow/Column: O(log r/c)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
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
public final class TreeBasedTable<R, C, V> implements Table<R, C, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final NavigableMap<R, NavigableMap<C, V>> rowMap;
    private final Comparator<? super R> rowComparator;
    private final Comparator<? super C> columnComparator;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private TreeBasedTable(Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
        this.rowComparator = rowComparator;
        this.columnComparator = columnComparator;
        this.rowMap = rowComparator != null ? new TreeMap<>(rowComparator) : new TreeMap<>();
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty TreeBasedTable with natural ordering.
     * 创建自然排序的空 TreeBasedTable。
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     * @return new empty TreeBasedTable | 新空 TreeBasedTable
     */
    public static <R extends Comparable<? super R>, C extends Comparable<? super C>, V>
    TreeBasedTable<R, C, V> create() {
        return new TreeBasedTable<>(null, null);
    }

    /**
     * Create an empty TreeBasedTable with custom comparators.
     * 创建自定义比较器的空 TreeBasedTable。
     *
     * @param <R>              row key type | 行键类型
     * @param <C>              column key type | 列键类型
     * @param <V>              value type | 值类型
     * @param rowComparator    row comparator | 行比较器
     * @param columnComparator column comparator | 列比较器
     * @return new empty TreeBasedTable | 新空 TreeBasedTable
     */
    public static <R, C, V> TreeBasedTable<R, C, V> create(
            Comparator<? super R> rowComparator, Comparator<? super C> columnComparator) {
        return new TreeBasedTable<>(rowComparator, columnComparator);
    }

    // ==================== Table 实现 | Table Implementation ====================

    @Override
    public V get(Object rowKey, Object columnKey) {
        if (rowKey == null || columnKey == null) {
            return null;
        }
        NavigableMap<C, V> row = rowMap.get(rowKey);
        return row == null ? null : row.get(columnKey);
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        if (rowKey == null || columnKey == null) {
            return false;
        }
        NavigableMap<C, V> row = rowMap.get(rowKey);
        return row != null && row.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return rowKey != null && rowMap.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        if (columnKey == null) return false;
        for (NavigableMap<C, V> row : rowMap.values()) {
            if (row.containsKey(columnKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        for (NavigableMap<C, V> row : rowMap.values()) {
            if (row.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V put(R rowKey, C columnKey, V value) {
        Objects.requireNonNull(rowKey, "Row key cannot be null");
        Objects.requireNonNull(columnKey, "Column key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");

        NavigableMap<C, V> row = rowMap.computeIfAbsent(rowKey, k ->
                columnComparator != null ? new TreeMap<>(columnComparator) : new TreeMap<>());

        V oldValue = row.put(columnKey, value);
        if (oldValue == null) {
            size++;
        }
        return oldValue;
    }

    @Override
    public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        for (Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
            put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
        }
    }

    @Override
    public V remove(Object rowKey, Object columnKey) {
        if (rowKey == null || columnKey == null) {
            return null;
        }
        NavigableMap<C, V> row = rowMap.get(rowKey);
        if (row == null) {
            return null;
        }
        V oldValue = row.remove(columnKey);
        if (oldValue != null) {
            size--;
            if (row.isEmpty()) {
                rowMap.remove(rowKey);
            }
        }
        return oldValue;
    }

    @Override
    public Map<C, V> row(R rowKey) {
        NavigableMap<C, V> row = rowMap.get(rowKey);
        return row == null ? Collections.emptyNavigableMap() : Collections.unmodifiableNavigableMap(row);
    }

    @Override
    public Map<R, V> column(C columnKey) {
        Map<R, V> result = columnComparator != null ?
                new TreeMap<>(rowComparator) : new TreeMap<>();
        for (Map.Entry<R, NavigableMap<C, V>> entry : rowMap.entrySet()) {
            V value = entry.getValue().get(columnKey);
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
        Set<Cell<R, C, V>> cells = new LinkedHashSet<>();
        for (Map.Entry<R, NavigableMap<C, V>> rowEntry : rowMap.entrySet()) {
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                cells.add(new CellImpl<>(rowEntry.getKey(), colEntry.getKey(), colEntry.getValue()));
            }
        }
        return Collections.unmodifiableSet(cells);
    }

    @Override
    public Set<R> rowKeySet() {
        return Collections.unmodifiableNavigableSet(rowMap.navigableKeySet());
    }

    @Override
    public Set<C> columnKeySet() {
        Set<C> columns = columnComparator != null ?
                new TreeSet<>(columnComparator) : new TreeSet<>();
        for (NavigableMap<C, V> row : rowMap.values()) {
            columns.addAll(row.keySet());
        }
        return Collections.unmodifiableSet(columns);
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (NavigableMap<C, V> row : rowMap.values()) {
            values.addAll(row.values());
        }
        return Collections.unmodifiableList(values);
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
        Map<R, Map<C, V>> result = new LinkedHashMap<>();
        for (Map.Entry<R, NavigableMap<C, V>> entry : rowMap.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableNavigableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> result = columnComparator != null ?
                new TreeMap<>(columnComparator) : new TreeMap<>();
        for (Map.Entry<R, NavigableMap<C, V>> rowEntry : rowMap.entrySet()) {
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                result.computeIfAbsent(colEntry.getKey(), k ->
                        rowComparator != null ? new TreeMap<>(rowComparator) : new TreeMap<>()
                ).put(rowEntry.getKey(), colEntry.getValue());
            }
        }
        // Make inner maps unmodifiable
        Map<C, Map<R, V>> unmodifiableResult = new LinkedHashMap<>();
        for (Map.Entry<C, Map<R, V>> entry : result.entrySet()) {
            unmodifiableResult.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(unmodifiableResult);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        rowMap.clear();
        size = 0;
    }

    // ==================== 额外方法 | Additional Methods ====================

    /**
     * Return the row comparator.
     * 返回行比较器。
     *
     * @return the row comparator | 行比较器
     */
    public Comparator<? super R> rowComparator() {
        return rowComparator;
    }

    /**
     * Return the column comparator.
     * 返回列比较器。
     *
     * @return the column comparator | 列比较器
     */
    public Comparator<? super C> columnComparator() {
        return columnComparator;
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
        return rowMap.toString();
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
