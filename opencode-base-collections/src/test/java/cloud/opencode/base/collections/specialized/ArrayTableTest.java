package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ArrayTable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ArrayTable 测试")
class ArrayTableTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 从行列键创建")
        void testCreate() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1", "col2"));

            assertThat(table.rowKeySet()).containsExactly("row1", "row2");
            assertThat(table.columnKeySet()).containsExactly("col1", "col2");
            assertThat(table.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - null 行键抛异常")
        void testCreateNullRowKey() {
            List<String> rows = Arrays.asList("row1", null);
            List<String> cols = List.of("col1");

            assertThatThrownBy(() -> ArrayTable.create(rows, cols))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("create - 重复行键抛异常")
        void testCreateDuplicateRowKey() {
            assertThatThrownBy(() -> ArrayTable.create(
                    List.of("row1", "row1"),
                    List.of("col1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("create - 从 Table 复制")
        void testCreateFromTable() {
            ArrayTable<String, String, Integer> source = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            source.put("row1", "col1", 1);

            ArrayTable<String, String, Integer> copy = ArrayTable.create(source);

            assertThat(copy.get("row1", "col1")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Table 方法测试")
    class TableMethodTests {

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);

            assertThat(table.get("row1", "col1")).isEqualTo(1);
            assertThat(table.get("row1", "col2")).isNull();
            assertThat(table.get("row3", "col1")).isNull();
        }

        @Test
        @DisplayName("put - 放置值")
        void testPut() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            Integer oldValue = table.put("row1", "col1", 1);

            assertThat(oldValue).isNull();
            assertThat(table.get("row1", "col1")).isEqualTo(1);
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            Integer oldValue = table.put("row1", "col1", 2);

            assertThat(oldValue).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isEqualTo(2);
        }

        @Test
        @DisplayName("put - 无效行键抛异常")
        void testPutInvalidRowKey() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThatThrownBy(() -> table.put("row2", "col1", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("put - 无效列键抛异常")
        void testPutInvalidColumnKey() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThatThrownBy(() -> table.put("row1", "col2", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("remove - 移除值")
        void testRemove() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            Integer removed = table.remove("row1", "col1");

            assertThat(removed).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isNull();
        }

        @Test
        @DisplayName("contains - 包含单元格")
        void testContains() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThat(table.contains("row1", "col1")).isTrue();
            assertThat(table.contains("row1", "col2")).isFalse();
        }

        @Test
        @DisplayName("containsRow - 包含行")
        void testContainsRow() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThat(table.containsRow("row1")).isTrue();
            assertThat(table.containsRow("row2")).isFalse();
        }

        @Test
        @DisplayName("containsColumn - 包含列")
        void testContainsColumn() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThat(table.containsColumn("col1")).isTrue();
            assertThat(table.containsColumn("col2")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            assertThat(table.containsValue(1)).isTrue();
            assertThat(table.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("row - 获取行")
        void testRow() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            Map<String, Integer> row = table.row("row1");

            assertThat(row).containsEntry("col1", 1);
            assertThat(row).containsEntry("col2", 2);
        }

        @Test
        @DisplayName("column - 获取列")
        void testColumn() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1"));
            table.put("row1", "col1", 1);
            table.put("row2", "col1", 2);

            Map<String, Integer> column = table.column("col1");

            assertThat(column).containsEntry("row1", 1);
            assertThat(column).containsEntry("row2", 2);
        }

        @Test
        @DisplayName("cellSet - 单元格集")
        void testCellSet() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            var cellSet = table.cellSet();

            assertThat(cellSet).hasSize(1);
            var cell = cellSet.iterator().next();
            assertThat(cell.getRowKey()).isEqualTo("row1");
            assertThat(cell.getColumnKey()).isEqualTo("col1");
            assertThat(cell.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("rowMap - 行映射")
        void testRowMap() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);

            var rowMap = table.rowMap();

            assertThat(rowMap).containsKey("row1");
            assertThat(rowMap.get("row1")).containsEntry("col1", 1);
        }

        @Test
        @DisplayName("columnMap - 列映射")
        void testColumnMap() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            var columnMap = table.columnMap();

            assertThat(columnMap).containsKey("col1");
            assertThat(columnMap.get("col1")).containsEntry("row1", 1);
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            assertThat(table.values()).containsExactlyInAnyOrder(1, 2);
        }
    }

    @Nested
    @DisplayName("额外方法测试")
    class AdditionalMethodTests {

        @Test
        @DisplayName("rowKeyList - 行键列表")
        void testRowKeyList() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1"));

            assertThat(table.rowKeyList()).containsExactly("row1", "row2");
        }

        @Test
        @DisplayName("columnKeyList - 列键列表")
        void testColumnKeyList() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1", "col2"));

            assertThat(table.columnKeyList()).containsExactly("col1", "col2");
        }

        @Test
        @DisplayName("at - 通过索引获取")
        void testAt() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            assertThat(table.at(0, 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("set - 通过索引设置")
        void testSet() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            Integer old = table.set(0, 0, 1);

            assertThat(old).isNull();
            assertThat(table.at(0, 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("erase - 通过索引擦除")
        void testErase() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            Integer old = table.erase(0, 0);

            assertThat(old).isEqualTo(1);
            assertThat(table.at(0, 0)).isNull();
        }

        @Test
        @DisplayName("toArray - 转为二维数组")
        void testToArray() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1", "row2"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);
            table.put("row2", "col2", 4);

            Integer[][] array = table.toArray(new Integer[0][0]);

            assertThat(array[0][0]).isEqualTo(1);
            assertThat(array[1][1]).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1", "col2"));
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            assertThat(table.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));

            assertThat(table.isEmpty()).isTrue();

            table.put("row1", "col1", 1);

            assertThat(table.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            table.clear();

            assertThat(table.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ArrayTable<String, String, Integer> table1 = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table1.put("row1", "col1", 1);

            ArrayTable<String, String, Integer> table2 = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table2.put("row1", "col1", 1);

            assertThat(table1).isEqualTo(table2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ArrayTable<String, String, Integer> table1 = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table1.put("row1", "col1", 1);

            ArrayTable<String, String, Integer> table2 = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table2.put("row1", "col1", 1);

            assertThat(table1.hashCode()).isEqualTo(table2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ArrayTable<String, String, Integer> table = ArrayTable.create(
                    List.of("row1"),
                    List.of("col1"));
            table.put("row1", "col1", 1);

            assertThat(table.toString()).contains("row1").contains("col1").contains("1");
        }
    }
}
