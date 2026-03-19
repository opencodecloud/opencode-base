package cloud.opencode.base.collections;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TableTest Tests
 * TableTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Table 接口测试")
class TableTest {

    @Nested
    @DisplayName("通过HashBasedTable测试Table接口")
    class HashBasedTableTests {

        @Test
        @DisplayName("put和get正确存储和获取值")
        void testPutAndGet() {
            Table<String, String, Integer> table = HashBasedTable.create();

            table.put("row1", "col1", 100);

            assertThat(table.get("row1", "col1")).isEqualTo(100);
        }

        @Test
        @DisplayName("size返回正确大小")
        void testSize() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);
            table.put("r1", "c2", 2);
            table.put("r2", "c1", 3);

            assertThat(table.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty正确判断空状态")
        void testIsEmpty() {
            Table<String, String, Integer> table = HashBasedTable.create();

            assertThat(table.isEmpty()).isTrue();
            table.put("r", "c", 1);
            assertThat(table.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains检查行列组合是否存在")
        void testContains() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);

            assertThat(table.contains("r1", "c1")).isTrue();
            assertThat(table.contains("r1", "c2")).isFalse();
        }

        @Test
        @DisplayName("containsRow和containsColumn正确检查")
        void testContainsRowAndColumn() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);

            assertThat(table.containsRow("r1")).isTrue();
            assertThat(table.containsRow("r2")).isFalse();
            assertThat(table.containsColumn("c1")).isTrue();
            assertThat(table.containsColumn("c2")).isFalse();
        }

        @Test
        @DisplayName("row返回行视图")
        void testRow() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);
            table.put("r1", "c2", 2);

            Map<String, Integer> row = table.row("r1");

            assertThat(row).containsEntry("c1", 1).containsEntry("c2", 2);
        }

        @Test
        @DisplayName("column返回列视图")
        void testColumn() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);
            table.put("r2", "c1", 2);

            Map<String, Integer> col = table.column("c1");

            assertThat(col).containsEntry("r1", 1).containsEntry("r2", 2);
        }

        @Test
        @DisplayName("remove移除指定行列的值")
        void testRemove() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);

            Integer removed = table.remove("r1", "c1");

            assertThat(removed).isEqualTo(1);
            assertThat(table.contains("r1", "c1")).isFalse();
        }

        @Test
        @DisplayName("clear清除所有映射")
        void testClear() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);
            table.put("r2", "c2", 2);

            table.clear();

            assertThat(table.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("rowKeySet和columnKeySet返回正确键集合")
        void testKeysSets() {
            Table<String, String, Integer> table = HashBasedTable.create();
            table.put("r1", "c1", 1);
            table.put("r2", "c2", 2);

            assertThat(table.rowKeySet()).containsExactlyInAnyOrder("r1", "r2");
            assertThat(table.columnKeySet()).containsExactlyInAnyOrder("c1", "c2");
        }
    }
}
