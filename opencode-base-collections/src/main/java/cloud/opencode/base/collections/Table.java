package cloud.opencode.base.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Table - Two-dimensional Map with Row and Column Keys
 * Table - 带行和列键的二维映射
 *
 * <p>A collection that associates an ordered pair of keys (row key, column key)
 * with a single value. Similar to a 2D array or spreadsheet.</p>
 * <p>将有序键对（行键、列键）与单个值关联的集合。类似于二维数组或电子表格。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Two-dimensional mapping - 二维映射</li>
 *   <li>Row view (row key to column-value map) - 行视图（行键到列值映射）</li>
 *   <li>Column view (column key to row-value map) - 列视图（列键到行值映射）</li>
 *   <li>Cell view (all row-column-value triples) - 单元格视图（所有行列值三元组）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create Table - 创建 Table
 * Table<String, String, Integer> table = HashBasedTable.create();
 *
 * // Put values - 放入值
 * table.put("row1", "col1", 100);
 * table.put("row1", "col2", 200);
 * table.put("row2", "col1", 300);
 *
 * // Get value - 获取值
 * Integer value = table.get("row1", "col1");  // 100
 *
 * // Get row view - 获取行视图
 * Map<String, Integer> row = table.row("row1");  // {col1=100, col2=200}
 *
 * // Get column view - 获取列视图
 * Map<String, Integer> col = table.column("col1");  // {row1=100, row2=300}
 *
 * // Iterate cells - 迭代单元格
 * for (Table.Cell<String, String, Integer> cell : table.cellSet()) {
 *     System.out.println(cell.getRowKey() + "," + cell.getColumnKey() + "=" + cell.getValue());
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) for hash-based - get: O(1) 基于哈希</li>
 *   <li>put: O(1) for hash-based - put: O(1) 基于哈希</li>
 *   <li>row/column: O(1) - row/column: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
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
public interface Table<R, C, V> {

    // ==================== 基本操作 | Basic Operations ====================

    /**
     * Check if the table contains any mappings.
     * 检查表是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    boolean isEmpty();

    /**
     * Return the number of row-column-value mappings.
     * 返回行列值映射的数量。
     *
     * @return size | 大小
     */
    int size();

    /**
     * Clear all mappings.
     * 清除所有映射。
     */
    void clear();

    // ==================== 包含检查 | Contains Checks ====================

    /**
     * Check if the table contains a mapping with the row and column keys.
     * 检查表是否包含指定行列键的映射。
     *
     * @param rowKey    the row key | 行键
     * @param columnKey the column key | 列键
     * @return true if contains | 如果包含则返回 true
     */
    boolean contains(Object rowKey, Object columnKey);

    /**
     * Check if the table contains a mapping with the row key.
     * 检查表是否包含指定行键的映射。
     *
     * @param rowKey the row key | 行键
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsRow(Object rowKey);

    /**
     * Check if the table contains a mapping with the column key.
     * 检查表是否包含指定列键的映射。
     *
     * @param columnKey the column key | 列键
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsColumn(Object columnKey);

    /**
     * Check if the table contains a mapping with the value.
     * 检查表是否包含指定值的映射。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    boolean containsValue(Object value);

    // ==================== 获取操作 | Get Operations ====================

    /**
     * Get the value at the specified row and column.
     * 获取指定行列的值。
     *
     * @param rowKey    the row key | 行键
     * @param columnKey the column key | 列键
     * @return the value, or null | 值，或 null
     */
    V get(Object rowKey, Object columnKey);

    // ==================== 放入/移除操作 | Put/Remove Operations ====================

    /**
     * Put a value at the specified row and column.
     * 在指定行列放入值。
     *
     * @param rowKey    the row key | 行键
     * @param columnKey the column key | 列键
     * @param value     the value | 值
     * @return previous value, or null | 之前的值，或 null
     */
    V put(R rowKey, C columnKey, V value);

    /**
     * Put all values from another table.
     * 从另一个表放入所有值。
     *
     * @param table the source table | 源表
     */
    void putAll(Table<? extends R, ? extends C, ? extends V> table);

    /**
     * Remove the value at the specified row and column.
     * 移除指定行列的值。
     *
     * @param rowKey    the row key | 行键
     * @param columnKey the column key | 列键
     * @return removed value, or null | 移除的值，或 null
     */
    V remove(Object rowKey, Object columnKey);

    // ==================== 视图 | Views ====================

    /**
     * Return a view of all row keys.
     * 返回所有行键的视图。
     *
     * @return row key set | 行键集合
     */
    Set<R> rowKeySet();

    /**
     * Return a view of all column keys.
     * 返回所有列键的视图。
     *
     * @return column key set | 列键集合
     */
    Set<C> columnKeySet();

    /**
     * Return a view of all values.
     * 返回所有值的视图。
     *
     * @return values collection | 值集合
     */
    Collection<V> values();

    /**
     * Return a view of all cells.
     * 返回所有单元格的视图。
     *
     * @return cell set | 单元格集合
     */
    Set<Cell<R, C, V>> cellSet();

    /**
     * Return a map view for a specific row.
     * 返回特定行的映射视图。
     *
     * @param rowKey the row key | 行键
     * @return column to value map | 列到值的映射
     */
    Map<C, V> row(R rowKey);

    /**
     * Return a map view for a specific column.
     * 返回特定列的映射视图。
     *
     * @param columnKey the column key | 列键
     * @return row to value map | 行到值的映射
     */
    Map<R, V> column(C columnKey);

    /**
     * Return a map view of all rows.
     * 返回所有行的映射视图。
     *
     * @return row key to column-value map | 行键到列值映射
     */
    Map<R, Map<C, V>> rowMap();

    /**
     * Return a map view of all columns.
     * 返回所有列的映射视图。
     *
     * @return column key to row-value map | 列键到行值映射
     */
    Map<C, Map<R, V>> columnMap();

    // ==================== 单元格接口 | Cell Interface ====================

    /**
     * Cell - A row-column-value triple
     * Cell - 行列值三元组
     *
     * @param <R> row key type | 行键类型
     * @param <C> column key type | 列键类型
     * @param <V> value type | 值类型
     */
    interface Cell<R, C, V> {

        /**
         * Return the row key.
         * 返回行键。
         *
         * @return row key | 行键
         */
        R getRowKey();

        /**
         * Return the column key.
         * 返回列键。
         *
         * @return column key | 列键
         */
        C getColumnKey();

        /**
         * Return the value.
         * 返回值。
         *
         * @return value | 值
         */
        V getValue();
    }
}
