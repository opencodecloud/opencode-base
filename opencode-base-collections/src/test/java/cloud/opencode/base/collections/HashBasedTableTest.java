package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HashBasedTable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("HashBasedTable 测试")
class HashBasedTableTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Table")
        void testCreate() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            assertThat(table).isNotNull();
            assertThat(table.isEmpty()).isTrue();
            assertThat(table.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create(32, 16);

            assertThat(table).isNotNull();
            assertThat(table.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> HashBasedTable.create(-1, 16))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> HashBasedTable.create(16, -1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("create - 从现有 Table 创建")
        void testCreateFromTable() {
            HashBasedTable<String, String, Integer> source = HashBasedTable.create();
            source.put("row1", "col1", 100);
            source.put("row1", "col2", 200);
            source.put("row2", "col1", 300);

            HashBasedTable<String, String, Integer> copy = HashBasedTable.create(source);

            assertThat(copy.size()).isEqualTo(3);
            assertThat(copy.get("row1", "col1")).isEqualTo(100);
            assertThat(copy.get("row1", "col2")).isEqualTo(200);
            assertThat(copy.get("row2", "col1")).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 添加新值")
        void testPut() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            Integer previous = table.put("row1", "col1", 100);

            assertThat(previous).isNull();
            assertThat(table.size()).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isEqualTo(100);
        }

        @Test
        @DisplayName("put - 替换现有值")
        void testPutReplace() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            Integer previous = table.put("row1", "col1", 200);

            assertThat(previous).isEqualTo(100);
            assertThat(table.size()).isEqualTo(1);  // size unchanged
            assertThat(table.get("row1", "col1")).isEqualTo(200);
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testPutNullKey() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            assertThatThrownBy(() -> table.put(null, "col1", 100))
                    .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> table.put("row1", null, 100))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值允许")
        void testPutNullValue() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            table.put("row1", "col1", null);

            assertThat(table.size()).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isNull();
            assertThat(table.contains("row1", "col1")).isTrue();
        }

        @Test
        @DisplayName("put - 多行多列")
        void testPutMultiple() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            table.put("A", "X", 1);
            table.put("A", "Y", 2);
            table.put("B", "X", 3);
            table.put("B", "Y", 4);

            assertThat(table.size()).isEqualTo(4);
            assertThat(table.get("A", "X")).isEqualTo(1);
            assertThat(table.get("A", "Y")).isEqualTo(2);
            assertThat(table.get("B", "X")).isEqualTo(3);
            assertThat(table.get("B", "Y")).isEqualTo(4);
        }

        @Test
        @DisplayName("putAll - 从另一个 Table 添加")
        void testPutAll() {
            HashBasedTable<String, String, Integer> source = HashBasedTable.create();
            source.put("row1", "col1", 100);
            source.put("row2", "col2", 200);

            HashBasedTable<String, String, Integer> target = HashBasedTable.create();
            target.putAll(source);

            assertThat(target.size()).isEqualTo(2);
            assertThat(target.get("row1", "col1")).isEqualTo(100);
            assertThat(target.get("row2", "col2")).isEqualTo(200);
        }

        @Test
        @DisplayName("putAll - null 表无影响")
        void testPutAllNull() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            table.putAll(null);

            assertThat(table.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("get 操作测试")
    class GetOperationTests {

        @Test
        @DisplayName("get - 获取存在的值")
        void testGet() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            assertThat(table.get("row1", "col1")).isEqualTo(100);
        }

        @Test
        @DisplayName("get - 获取不存在的值")
        void testGetNonExistent() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            assertThat(table.get("row1", "col1")).isNull();
        }

        @Test
        @DisplayName("get - 行存在但列不存在")
        void testGetColumnNotExist() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            assertThat(table.get("row1", "col2")).isNull();
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("contains - 存在的单元格")
        void testContains() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            assertThat(table.contains("row1", "col1")).isTrue();
            assertThat(table.contains("row1", "col2")).isFalse();
            assertThat(table.contains("row2", "col1")).isFalse();
        }

        @Test
        @DisplayName("containsRow - 存在的行")
        void testContainsRow() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            assertThat(table.containsRow("row1")).isTrue();
            assertThat(table.containsRow("row2")).isFalse();
        }

        @Test
        @DisplayName("containsColumn - 存在的列")
        void testContainsColumn() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col2", 200);

            assertThat(table.containsColumn("col1")).isTrue();
            assertThat(table.containsColumn("col2")).isTrue();
            assertThat(table.containsColumn("col3")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 存在的值")
        void testContainsValue() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col2", 200);

            assertThat(table.containsValue(100)).isTrue();
            assertThat(table.containsValue(200)).isTrue();
            assertThat(table.containsValue(300)).isFalse();
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove - 移除存在的值")
        void testRemove() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row1", "col2", 200);

            Integer removed = table.remove("row1", "col1");

            assertThat(removed).isEqualTo(100);
            assertThat(table.size()).isEqualTo(1);
            assertThat(table.contains("row1", "col1")).isFalse();
        }

        @Test
        @DisplayName("remove - 移除不存在的值")
        void testRemoveNonExistent() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            Integer removed = table.remove("row1", "col1");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("remove - 移除行的最后一个值时删除行")
        void testRemoveLastInRow() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            table.remove("row1", "col1");

            assertThat(table.containsRow("row1")).isFalse();
        }

        @Test
        @DisplayName("clear - 清空所有")
        void testClear() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col2", 200);

            table.clear();

            assertThat(table.isEmpty()).isTrue();
            assertThat(table.size()).isZero();
        }
    }

    @Nested
    @DisplayName("视图测试")
    class ViewTests {

        @Test
        @DisplayName("rowKeySet - 行键集合")
        void testRowKeySet() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col2", 200);

            Set<String> rowKeys = table.rowKeySet();

            assertThat(rowKeys).containsExactlyInAnyOrder("row1", "row2");
        }

        @Test
        @DisplayName("columnKeySet - 列键集合")
        void testColumnKeySet() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row1", "col2", 200);
            table.put("row2", "col1", 300);

            Set<String> columnKeys = table.columnKeySet();

            assertThat(columnKeys).containsExactlyInAnyOrder("col1", "col2");
        }

        @Test
        @DisplayName("values - 所有值")
        void testValues() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row1", "col2", 200);
            table.put("row2", "col1", 300);

            Collection<Integer> values = table.values();

            assertThat(values).containsExactlyInAnyOrder(100, 200, 300);
        }

        @Test
        @DisplayName("cellSet - 单元格集合")
        void testCellSet() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col2", 200);

            Set<Table.Cell<String, String, Integer>> cells = table.cellSet();

            assertThat(cells).hasSize(2);
            assertThat(cells).extracting(Table.Cell::getRowKey).containsExactlyInAnyOrder("row1", "row2");
            assertThat(cells).extracting(Table.Cell::getColumnKey).containsExactlyInAnyOrder("col1", "col2");
            assertThat(cells).extracting(Table.Cell::getValue).containsExactlyInAnyOrder(100, 200);
        }

        @Test
        @DisplayName("row - 行视图")
        void testRow() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row1", "col2", 200);
            table.put("row2", "col1", 300);

            Map<String, Integer> row = table.row("row1");

            assertThat(row).hasSize(2);
            assertThat(row.get("col1")).isEqualTo(100);
            assertThat(row.get("col2")).isEqualTo(200);
        }

        @Test
        @DisplayName("row - 不存在的行返回空")
        void testRowNonExistent() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            Map<String, Integer> row = table.row("nonexistent");

            assertThat(row).isEmpty();
        }

        @Test
        @DisplayName("row - 不可修改")
        void testRowUnmodifiable() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            Map<String, Integer> row = table.row("row1");

            assertThatThrownBy(() -> row.put("col2", 200))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("column - 列视图")
        void testColumn() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col1", 200);
            table.put("row1", "col2", 300);

            Map<String, Integer> column = table.column("col1");

            assertThat(column).hasSize(2);
            assertThat(column.get("row1")).isEqualTo(100);
            assertThat(column.get("row2")).isEqualTo(200);
        }

        @Test
        @DisplayName("column - 不存在的列返回空")
        void testColumnNonExistent() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            Map<String, Integer> column = table.column("nonexistent");

            assertThat(column).isEmpty();
        }

        @Test
        @DisplayName("rowMap - 行映射视图")
        void testRowMap() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row1", "col2", 200);
            table.put("row2", "col1", 300);

            Map<String, Map<String, Integer>> rowMap = table.rowMap();

            assertThat(rowMap).hasSize(2);
            assertThat(rowMap.get("row1")).containsEntry("col1", 100).containsEntry("col2", 200);
            assertThat(rowMap.get("row2")).containsEntry("col1", 300);
        }

        @Test
        @DisplayName("rowMap - 不可修改")
        void testRowMapUnmodifiable() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            Map<String, Map<String, Integer>> rowMap = table.rowMap();

            assertThatThrownBy(() -> rowMap.put("row2", Map.of("col1", 200)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("columnMap - 列映射视图")
        void testColumnMap() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);
            table.put("row2", "col1", 200);
            table.put("row1", "col2", 300);

            Map<String, Map<String, Integer>> columnMap = table.columnMap();

            assertThat(columnMap).hasSize(2);
            assertThat(columnMap.get("col1")).containsEntry("row1", 100).containsEntry("row2", 200);
            assertThat(columnMap.get("col2")).containsEntry("row1", 300);
        }
    }

    @Nested
    @DisplayName("Cell 测试")
    class CellTests {

        @Test
        @DisplayName("Cell - 基本属性")
        void testCellBasicProperties() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            Table.Cell<String, String, Integer> cell = table.cellSet().iterator().next();

            assertThat(cell.getRowKey()).isEqualTo("row1");
            assertThat(cell.getColumnKey()).isEqualTo("col1");
            assertThat(cell.getValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("Cell - equals")
        void testCellEquals() {
            HashBasedTable<String, String, Integer> table1 = HashBasedTable.create();
            table1.put("row1", "col1", 100);

            HashBasedTable<String, String, Integer> table2 = HashBasedTable.create();
            table2.put("row1", "col1", 100);

            Table.Cell<String, String, Integer> cell1 = table1.cellSet().iterator().next();
            Table.Cell<String, String, Integer> cell2 = table2.cellSet().iterator().next();

            assertThat(cell1).isEqualTo(cell2);
        }

        @Test
        @DisplayName("Cell - hashCode")
        void testCellHashCode() {
            HashBasedTable<String, String, Integer> table1 = HashBasedTable.create();
            table1.put("row1", "col1", 100);

            HashBasedTable<String, String, Integer> table2 = HashBasedTable.create();
            table2.put("row1", "col1", 100);

            Table.Cell<String, String, Integer> cell1 = table1.cellSet().iterator().next();
            Table.Cell<String, String, Integer> cell2 = table2.cellSet().iterator().next();

            assertThat(cell1.hashCode()).isEqualTo(cell2.hashCode());
        }

        @Test
        @DisplayName("Cell - toString")
        void testCellToString() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            Table.Cell<String, String, Integer> cell = table.cellSet().iterator().next();
            String str = cell.toString();

            assertThat(str).contains("row1");
            assertThat(str).contains("col1");
            assertThat(str).contains("100");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            HashBasedTable<String, String, Integer> table1 = HashBasedTable.create();
            table1.put("row1", "col1", 100);

            HashBasedTable<String, String, Integer> table2 = HashBasedTable.create();
            table2.put("row1", "col1", 100);

            assertThat(table1).isEqualTo(table2);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            assertThat(table.equals(table)).isTrue();
        }

        @Test
        @DisplayName("equals - 不相等")
        void testEqualsNotEqual() {
            HashBasedTable<String, String, Integer> table1 = HashBasedTable.create();
            table1.put("row1", "col1", 100);

            HashBasedTable<String, String, Integer> table2 = HashBasedTable.create();
            table2.put("row1", "col1", 200);

            assertThat(table1).isNotEqualTo(table2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            HashBasedTable<String, String, Integer> table1 = HashBasedTable.create();
            table1.put("row1", "col1", 100);

            HashBasedTable<String, String, Integer> table2 = HashBasedTable.create();
            table2.put("row1", "col1", 100);

            assertThat(table1.hashCode()).isEqualTo(table2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();
            table.put("row1", "col1", 100);

            String str = table.toString();

            assertThat(str).contains("row1");
            assertThat(str).contains("col1");
            assertThat(str).contains("100");
        }
    }

    @Nested
    @DisplayName("size/isEmpty 测试")
    class SizeTests {

        @Test
        @DisplayName("size - 统计单元格数")
        void testSize() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            assertThat(table.size()).isZero();

            table.put("row1", "col1", 100);
            assertThat(table.size()).isEqualTo(1);

            table.put("row1", "col2", 200);
            assertThat(table.size()).isEqualTo(2);

            table.put("row2", "col1", 300);
            assertThat(table.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size - 替换不增加")
        void testSizeReplace() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            table.put("row1", "col1", 100);
            assertThat(table.size()).isEqualTo(1);

            table.put("row1", "col1", 200);  // replace
            assertThat(table.size()).isEqualTo(1);  // size unchanged
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            HashBasedTable<String, String, Integer> table = HashBasedTable.create();

            assertThat(table.isEmpty()).isTrue();

            table.put("row1", "col1", 100);
            assertThat(table.isEmpty()).isFalse();

            table.remove("row1", "col1");
            assertThat(table.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("电子表格场景")
        void testSpreadsheetScenario() {
            HashBasedTable<Integer, String, Double> spreadsheet = HashBasedTable.create();

            // Headers: A, B, C
            // Rows: 1, 2, 3
            spreadsheet.put(1, "A", 10.0);
            spreadsheet.put(1, "B", 20.0);
            spreadsheet.put(1, "C", 30.0);
            spreadsheet.put(2, "A", 40.0);
            spreadsheet.put(2, "B", 50.0);
            spreadsheet.put(2, "C", 60.0);

            // Get row 1
            Map<String, Double> row1 = spreadsheet.row(1);
            assertThat(row1.values().stream().mapToDouble(Double::doubleValue).sum()).isEqualTo(60.0);

            // Get column A
            Map<Integer, Double> columnA = spreadsheet.column("A");
            assertThat(columnA.values().stream().mapToDouble(Double::doubleValue).sum()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("矩阵场景")
        void testMatrixScenario() {
            HashBasedTable<Integer, Integer, Integer> matrix = HashBasedTable.create();

            // 3x3 matrix
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    matrix.put(i, j, i * 3 + j);
                }
            }

            assertThat(matrix.size()).isEqualTo(9);

            // Verify diagonal
            assertThat(matrix.get(0, 0)).isEqualTo(0);
            assertThat(matrix.get(1, 1)).isEqualTo(4);
            assertThat(matrix.get(2, 2)).isEqualTo(8);
        }
    }
}
