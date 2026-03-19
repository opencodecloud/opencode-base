package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeBasedTable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeBasedTable 测试")
class TreeBasedTableTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Table (自然排序)")
        void testCreate() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            assertThat(table.isEmpty()).isTrue();
            assertThat(table.size()).isZero();
        }

        @Test
        @DisplayName("create - 使用自定义比较器")
        void testCreateWithComparators() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create(
                    Comparator.reverseOrder(),
                    Comparator.reverseOrder());

            table.put("a", "x", 1);
            table.put("c", "z", 2);
            table.put("b", "y", 3);

            List<String> rowKeys = new ArrayList<>(table.rowKeySet());
            assertThat(rowKeys).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("Table 方法测试")
    class TableMethodTests {

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.get("row1", "col1")).isEqualTo(1);
            assertThat(table.get("row1", "col2")).isNull();
            assertThat(table.get("row2", "col1")).isNull();
        }

        @Test
        @DisplayName("get - null 参数返回 null")
        void testGetNull() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.get(null, "col1")).isNull();
            assertThat(table.get("row1", null)).isNull();
        }

        @Test
        @DisplayName("put - 放置值")
        void testPut() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            Integer oldValue = table.put("row1", "col1", 1);

            assertThat(oldValue).isNull();
            assertThat(table.get("row1", "col1")).isEqualTo(1);
        }

        @Test
        @DisplayName("put - 替换值")
        void testPutReplace() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            Integer oldValue = table.put("row1", "col1", 2);

            assertThat(oldValue).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isEqualTo(2);
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testPutNullKey() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            assertThatThrownBy(() -> table.put(null, "col1", 1))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> table.put("row1", null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值抛异常")
        void testPutNullValue() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            assertThatThrownBy(() -> table.put("row1", "col1", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("remove - 移除值")
        void testRemove() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            Integer removed = table.remove("row1", "col1");

            assertThat(removed).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isNull();
        }

        @Test
        @DisplayName("remove - 移除后清理空行")
        void testRemoveCleanupEmptyRow() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            table.remove("row1", "col1");

            assertThat(table.containsRow("row1")).isFalse();
        }

        @Test
        @DisplayName("contains - 包含单元格")
        void testContains() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.contains("row1", "col1")).isTrue();
            assertThat(table.contains("row1", "col2")).isFalse();
        }

        @Test
        @DisplayName("containsRow - 包含行")
        void testContainsRow() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.containsRow("row1")).isTrue();
            assertThat(table.containsRow("row2")).isFalse();
            assertThat(table.containsRow(null)).isFalse();
        }

        @Test
        @DisplayName("containsColumn - 包含列")
        void testContainsColumn() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.containsColumn("col1")).isTrue();
            assertThat(table.containsColumn("col2")).isFalse();
            assertThat(table.containsColumn(null)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.containsValue(1)).isTrue();
            assertThat(table.containsValue(2)).isFalse();
            assertThat(table.containsValue(null)).isFalse();
        }

        @Test
        @DisplayName("row - 获取行")
        void testRow() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            Map<String, Integer> row = table.row("row1");

            assertThat(row).containsEntry("col1", 1);
            assertThat(row).containsEntry("col2", 2);
        }

        @Test
        @DisplayName("row - 不存在的行返回空 Map")
        void testRowNotExists() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            Map<String, Integer> row = table.row("row1");

            assertThat(row).isEmpty();
        }

        @Test
        @DisplayName("column - 获取列")
        void testColumn() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);
            table.put("row2", "col1", 2);

            Map<String, Integer> column = table.column("col1");

            assertThat(column).containsEntry("row1", 1);
            assertThat(column).containsEntry("row2", 2);
        }

        @Test
        @DisplayName("cellSet - 单元格集")
        void testCellSet() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);
            table.put("row2", "col2", 2);

            var cellSet = table.cellSet();

            assertThat(cellSet).hasSize(2);
        }

        @Test
        @DisplayName("rowKeySet - 排序的行键集")
        void testRowKeySet() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("c", "col1", 3);
            table.put("a", "col1", 1);
            table.put("b", "col1", 2);

            Set<String> rowKeys = table.rowKeySet();

            assertThat(new ArrayList<>(rowKeys)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("columnKeySet - 排序的列键集")
        void testColumnKeySet() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "c", 3);
            table.put("row1", "a", 1);
            table.put("row1", "b", 2);

            Set<String> columnKeys = table.columnKeySet();

            assertThat(new ArrayList<>(columnKeys)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            assertThat(table.values()).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("rowMap - 行映射")
        void testRowMap() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            var rowMap = table.rowMap();

            assertThat(rowMap).containsKey("row1");
            assertThat(rowMap.get("row1")).containsEntry("col1", 1);
        }

        @Test
        @DisplayName("columnMap - 列映射")
        void testColumnMap() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            var columnMap = table.columnMap();

            assertThat(columnMap).containsKey("col1");
            assertThat(columnMap.get("col1")).containsEntry("row1", 1);
        }

        @Test
        @DisplayName("putAll - 批量放置")
        void testPutAll() {
            TreeBasedTable<String, String, Integer> source = TreeBasedTable.create();
            source.put("row1", "col1", 1);

            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.putAll(source);

            assertThat(table.get("row1", "col1")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("额外方法测试")
    class AdditionalMethodTests {

        @Test
        @DisplayName("rowComparator - 行比较器")
        void testRowComparator() {
            TreeBasedTable<String, String, Integer> natural = TreeBasedTable.create();
            TreeBasedTable<String, String, Integer> custom = TreeBasedTable.create(
                    Comparator.reverseOrder(), null);

            assertThat(natural.rowComparator()).isNull();
            assertThat(custom.rowComparator()).isNotNull();
        }

        @Test
        @DisplayName("columnComparator - 列比较器")
        void testColumnComparator() {
            TreeBasedTable<String, String, Integer> natural = TreeBasedTable.create();
            TreeBasedTable<String, String, Integer> custom = TreeBasedTable.create(
                    null, Comparator.reverseOrder());

            assertThat(natural.columnComparator()).isNull();
            assertThat(custom.columnComparator()).isNotNull();
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);
            table.put("row1", "col2", 2);

            assertThat(table.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();

            assertThat(table.isEmpty()).isTrue();

            table.put("row1", "col1", 1);

            assertThat(table.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            table.clear();

            assertThat(table.isEmpty()).isTrue();
            assertThat(table.size()).isZero();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            TreeBasedTable<String, String, Integer> table1 = TreeBasedTable.create();
            table1.put("row1", "col1", 1);

            TreeBasedTable<String, String, Integer> table2 = TreeBasedTable.create();
            table2.put("row1", "col1", 1);

            assertThat(table1).isEqualTo(table2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            TreeBasedTable<String, String, Integer> table1 = TreeBasedTable.create();
            table1.put("row1", "col1", 1);

            TreeBasedTable<String, String, Integer> table2 = TreeBasedTable.create();
            table2.put("row1", "col1", 1);

            assertThat(table1.hashCode()).isEqualTo(table2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            TreeBasedTable<String, String, Integer> table = TreeBasedTable.create();
            table.put("row1", "col1", 1);

            assertThat(table.toString()).contains("row1").contains("col1").contains("1");
        }
    }
}
