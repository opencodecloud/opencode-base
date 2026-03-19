package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableTable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableTable 测试")
class ImmutableTableTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空 Table")
        void testOfEmpty() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of();

            assertThat(table.isEmpty()).isTrue();
            assertThat(table.size()).isZero();
        }

        @Test
        @DisplayName("of - 创建包含一个单元格的 Table")
        void testOfOne() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.size()).isEqualTo(1);
            assertThat(table.get("row1", "col1")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 构建 Table")
        void testBuilder() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .put("row2", "col1", 3)
                    .put("row2", "col2", 4)
                    .build();

            assertThat(table.size()).isEqualTo(4);
            assertThat(table.get("row1", "col1")).isEqualTo(1);
            assertThat(table.get("row1", "col2")).isEqualTo(2);
            assertThat(table.get("row2", "col1")).isEqualTo(3);
            assertThat(table.get("row2", "col2")).isEqualTo(4);
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder().build();

            assertThat(table.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("builder - 重复单元格抛异常")
        void testBuilderDuplicateCell() {
            assertThatThrownBy(() -> ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col1", 2)
                    .build())
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("builder - null 行键抛异常")
        void testBuilderNullRowKey() {
            assertThatThrownBy(() -> ImmutableTable.<String, String, Integer>builder()
                    .put(null, "col1", 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - null 列键抛异常")
        void testBuilderNullColKey() {
            assertThatThrownBy(() -> ImmutableTable.<String, String, Integer>builder()
                    .put("row1", null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - null 值抛异常")
        void testBuilderNullValue() {
            assertThatThrownBy(() -> ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - putAll")
        void testBuilderPutAll() {
            ImmutableTable<String, String, Integer> source = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .build();

            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .putAll(source)
                    .build();

            assertThat(table.get("row1", "col1")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.get("row1", "col1")).isEqualTo(1);
            assertThat(table.get("row1", "col2")).isNull();
            assertThat(table.get("row2", "col1")).isNull();
        }

        @Test
        @DisplayName("get - null 参数")
        void testGetNull() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.get(null, "col1")).isNull();
            assertThat(table.get("row1", null)).isNull();
        }

        @Test
        @DisplayName("contains - 包含单元格")
        void testContains() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.contains("row1", "col1")).isTrue();
            assertThat(table.contains("row1", "col2")).isFalse();
        }

        @Test
        @DisplayName("contains - null 参数")
        void testContainsNull() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.contains(null, "col1")).isFalse();
            assertThat(table.contains("row1", null)).isFalse();
        }

        @Test
        @DisplayName("containsRow - 包含行")
        void testContainsRow() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.containsRow("row1")).isTrue();
            assertThat(table.containsRow("row2")).isFalse();
            assertThat(table.containsRow(null)).isFalse();
        }

        @Test
        @DisplayName("containsColumn - 包含列")
        void testContainsColumn() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.containsColumn("col1")).isTrue();
            assertThat(table.containsColumn("col2")).isFalse();
            assertThat(table.containsColumn(null)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.containsValue(1)).isTrue();
            assertThat(table.containsValue(2)).isFalse();
            assertThat(table.containsValue(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("视图方法测试")
    class ViewMethodTests {

        @Test
        @DisplayName("row - 获取行")
        void testRow() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .build();

            Map<String, Integer> row = table.row("row1");

            assertThat(row).containsEntry("col1", 1);
            assertThat(row).containsEntry("col2", 2);
        }

        @Test
        @DisplayName("row - 不存在的行")
        void testRowNotExists() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            Map<String, Integer> row = table.row("row2");

            assertThat(row).isEmpty();
        }

        @Test
        @DisplayName("column - 获取列")
        void testColumn() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row2", "col1", 3)
                    .build();

            Map<String, Integer> column = table.column("col1");

            assertThat(column).containsEntry("row1", 1);
            assertThat(column).containsEntry("row2", 3);
        }

        @Test
        @DisplayName("column - 不存在的列")
        void testColumnNotExists() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            Map<String, Integer> column = table.column("col2");

            assertThat(column).isEmpty();
        }

        @Test
        @DisplayName("rowKeySet - 行键集")
        void testRowKeySet() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row2", "col1", 2)
                    .build();

            assertThat(table.rowKeySet()).containsExactlyInAnyOrder("row1", "row2");
        }

        @Test
        @DisplayName("columnKeySet - 列键集")
        void testColumnKeySet() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .build();

            assertThat(table.columnKeySet()).containsExactlyInAnyOrder("col1", "col2");
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .build();

            assertThat(table.values()).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("cellSet - 单元格集")
        void testCellSet() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.cellSet()).hasSize(1);
            ImmutableTable.Cell<String, String, Integer> cell = table.cellSet().iterator().next();
            assertThat(cell.getRowKey()).isEqualTo("row1");
            assertThat(cell.getColumnKey()).isEqualTo("col1");
            assertThat(cell.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("rowMap - 行映射")
        void testRowMap() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .build();

            Map<String, Map<String, Integer>> rowMap = table.rowMap();

            assertThat(rowMap).containsKey("row1");
            assertThat(rowMap.get("row1")).containsEntry("col1", 1);
            assertThat(rowMap.get("row1")).containsEntry("col2", 2);
        }

        @Test
        @DisplayName("columnMap - 列映射")
        void testColumnMap() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row2", "col1", 3)
                    .build();

            Map<String, Map<String, Integer>> columnMap = table.columnMap();

            assertThat(columnMap).containsKey("col1");
            assertThat(columnMap.get("col1")).containsEntry("row1", 1);
            assertThat(columnMap.get("col1")).containsEntry("row2", 3);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .put("row2", "col1", 3)
                    .build();

            assertThat(table.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            assertThat(ImmutableTable.of().isEmpty()).isTrue();
            assertThat(ImmutableTable.of("row1", "col1", 1).isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ImmutableTable<String, String, Integer> table1 = ImmutableTable.of("row1", "col1", 1);
            ImmutableTable<String, String, Integer> table2 = ImmutableTable.of("row1", "col1", 1);

            assertThat(table1).isEqualTo(table2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ImmutableTable<String, String, Integer> table1 = ImmutableTable.of("row1", "col1", 1);
            ImmutableTable<String, String, Integer> table2 = ImmutableTable.of("row1", "col1", 1);

            assertThat(table1.hashCode()).isEqualTo(table2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.of("row1", "col1", 1);

            assertThat(table.toString()).contains("row1").contains("col1").contains("1");
        }
    }
}
