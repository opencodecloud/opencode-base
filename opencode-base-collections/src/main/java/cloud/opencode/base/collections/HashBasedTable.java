package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * HashBasedTable - Hash-based Table Implementation
 * HashBasedTable - 基于哈希的表实现
 *
 * <p>A Table implementation backed by nested HashMaps.
 * Provides O(1) average time for most operations.</p>
 * <p>由嵌套 HashMap 支持的 Table 实现。大多数操作提供 O(1) 平均时间。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) average time operations - O(1) 平均时间操作</li>
 *   <li>Allows null values but not null keys - 允许空值但不允许空键</li>
 *   <li>Serializable - 可序列化</li>
 *   <li>Live views - 实时视图</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空
 * HashBasedTable<String, String, Integer> table = HashBasedTable.create();
 *
 * // Create with expected size - 创建指定预期大小
 * HashBasedTable<String, String, Integer> table = HashBasedTable.create(10, 5);
 *
 * // Create from existing - 从现有创建
 * HashBasedTable<String, String, Integer> table = HashBasedTable.create(existingTable);
 *
 * // Operations - 操作
 * table.put("A", "X", 1);
 * table.put("A", "Y", 2);
 * table.put("B", "X", 3);
 *
 * table.get("A", "X");     // 1
 * table.row("A");          // {X=1, Y=2}
 * table.column("X");       // {A=1, B=3}
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get/put/remove: O(1) average - get/put/remove: O(1) 平均</li>
 *   <li>contains: O(1) average - contains: O(1) 平均</li>
 *   <li>row: O(1) - row: O(1)</li>
 *   <li>column: O(n) first access, O(1) cached - column: O(n) 首次访问, O(1) 缓存</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Values yes, keys no - 空值安全: 值是，键否</li>
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
public class HashBasedTable<R, C, V> implements Table<R, C, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ROW_CAPACITY = 16;
    private static final int DEFAULT_COLUMN_CAPACITY = 16;

    private final Map<R, Map<C, V>> backingMap;
    private final int expectedColumnCapacity;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param expectedRows    expected row count | 预期行数
     * @param expectedColumns expected column count | 预期列数
     */
    private HashBasedTable(int expectedRows, int expectedColumns) {
        this.backingMap = new HashMap<>(expectedRows);
        this.expectedColumnCapacity = expectedColumns;
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashBasedTable.
     * 创建空 HashBasedTable。
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     * @return new HashBasedTable | 新的 HashBasedTable
     */
    public static <R, C, V> HashBasedTable<R, C, V> create() {
        return new HashBasedTable<>(DEFAULT_ROW_CAPACITY, DEFAULT_COLUMN_CAPACITY);
    }

    /**
     * Create an empty HashBasedTable with expected sizes.
     * 创建指定预期大小的空 HashBasedTable。
     *
     * @param <R>             row key type | 行键类型
     * @param <C>             column key type | 列键类型
     * @param <V>             value type | 值类型
     * @param expectedRows    expected row count | 预期行数
     * @param expectedColumns expected column count | 预期列数
     * @return new HashBasedTable | 新的 HashBasedTable
     */
    public static <R, C, V> HashBasedTable<R, C, V> create(int expectedRows, int expectedColumns) {
        if (expectedRows < 0) {
            throw OpenCollectionException.illegalCapacity(expectedRows);
        }
        if (expectedColumns < 0) {
            throw OpenCollectionException.illegalCapacity(expectedColumns);
        }
        return new HashBasedTable<>(expectedRows, expectedColumns);
    }

    /**
     * Create a HashBasedTable from an existing table.
     * 从现有表创建 HashBasedTable。
     *
     * @param <R>   row key type | 行键类型
     * @param <C>   column key type | 列键类型
     * @param <V>   value type | 值类型
     * @param table source table | 源表
     * @return new HashBasedTable | 新的 HashBasedTable
     */
    public static <R, C, V> HashBasedTable<R, C, V> create(Table<? extends R, ? extends C, ? extends V> table) {
        HashBasedTable<R, C, V> result = create();
        result.putAll(table);
        return result;
    }

    // ==================== Table 实现 | Table Implementation ====================

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        backingMap.clear();
        size = 0;
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        Map<C, V> row = backingMap.get(rowKey);
        return row != null && row.containsKey(columnKey);
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return backingMap.containsKey(rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        for (Map<C, V> row : backingMap.values()) {
            if (row.containsKey(columnKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Map<C, V> row : backingMap.values()) {
            if (row.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object rowKey, Object columnKey) {
        Map<C, V> row = backingMap.get(rowKey);
        return row == null ? null : row.get(columnKey);
    }

    @Override
    public V put(R rowKey, C columnKey, V value) {
        Objects.requireNonNull(rowKey, "rowKey");
        Objects.requireNonNull(columnKey, "columnKey");

        Map<C, V> row = backingMap.computeIfAbsent(rowKey, k -> new HashMap<>(expectedColumnCapacity));
        boolean existed = row.containsKey(columnKey);
        V oldValue = row.put(columnKey, value);
        if (!existed) {
            size++;
        }
        return oldValue;
    }

    @Override
    public void putAll(Table<? extends R, ? extends C, ? extends V> table) {
        if (table != null) {
            for (Cell<? extends R, ? extends C, ? extends V> cell : table.cellSet()) {
                put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            }
        }
    }

    @Override
    public V remove(Object rowKey, Object columnKey) {
        Map<C, V> row = backingMap.get(rowKey);
        if (row == null) {
            return null;
        }

        if (!row.containsKey(columnKey)) {
            return null;
        }
        V oldValue = row.remove(columnKey);
        size--;
        if (row.isEmpty()) {
            backingMap.remove(rowKey);
        }
        return oldValue;
    }

    @Override
    public Set<R> rowKeySet() {
        return backingMap.keySet();
    }

    @Override
    public Set<C> columnKeySet() {
        Set<C> columns = new LinkedHashSet<>();
        for (Map<C, V> row : backingMap.values()) {
            columns.addAll(row.keySet());
        }
        return columns;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>(size);
        for (Map<C, V> row : backingMap.values()) {
            values.addAll(row.values());
        }
        return values;
    }

    @Override
    public Set<Cell<R, C, V>> cellSet() {
        Set<Cell<R, C, V>> cells = new LinkedHashSet<>(size);
        for (Map.Entry<R, Map<C, V>> rowEntry : backingMap.entrySet()) {
            R rowKey = rowEntry.getKey();
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                cells.add(new TableCell<>(rowKey, colEntry.getKey(), colEntry.getValue()));
            }
        }
        return cells;
    }

    @Override
    public Map<C, V> row(R rowKey) {
        Map<C, V> row = backingMap.get(rowKey);
        return row == null ? Collections.emptyMap() : Collections.unmodifiableMap(row);
    }

    @Override
    public Map<R, V> column(C columnKey) {
        Map<R, V> column = new LinkedHashMap<>();
        for (Map.Entry<R, Map<C, V>> rowEntry : backingMap.entrySet()) {
            if (rowEntry.getValue().containsKey(columnKey)) {
                column.put(rowEntry.getKey(), rowEntry.getValue().get(columnKey));
            }
        }
        return column;
    }

    @Override
    public Map<R, Map<C, V>> rowMap() {
        return Collections.unmodifiableMap(backingMap);
    }

    @Override
    public Map<C, Map<R, V>> columnMap() {
        Map<C, Map<R, V>> columnMap = new LinkedHashMap<>();
        for (Map.Entry<R, Map<C, V>> rowEntry : backingMap.entrySet()) {
            R rowKey = rowEntry.getKey();
            for (Map.Entry<C, V> colEntry : rowEntry.getValue().entrySet()) {
                columnMap.computeIfAbsent(colEntry.getKey(), k -> new LinkedHashMap<>())
                        .put(rowKey, colEntry.getValue());
            }
        }
        return columnMap;
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
        return backingMap.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Table cell implementation
     */
    private record TableCell<R, C, V>(R rowKey, C columnKey, V value) implements Cell<R, C, V> {

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
