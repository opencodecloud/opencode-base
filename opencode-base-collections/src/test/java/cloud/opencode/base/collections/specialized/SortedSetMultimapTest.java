package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.*;

import java.util.SortedSet;

import static org.assertj.core.api.Assertions.*;

/**
 * SortedSetMultimapTest Tests
 * SortedSetMultimapTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SortedSetMultimap 接口测试")
class SortedSetMultimapTest {

    @Nested
    @DisplayName("通过TreeSetMultimap测试SortedSetMultimap接口")
    class TreeSetMultimapTests {

        @Test
        @DisplayName("get返回排序后的值集合")
        void testGetReturnsSortedSet() {
            SortedSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("nums", 3);
            multimap.put("nums", 1);
            multimap.put("nums", 2);

            SortedSet<Integer> values = multimap.get("nums");

            assertThat(values).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("值自动去重")
        void testNoDuplicateValues() {
            SortedSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("nums", 1);
            multimap.put("nums", 1);

            assertThat(multimap.get("nums")).containsExactly(1);
        }

        @Test
        @DisplayName("valueComparator返回比较器")
        void testValueComparator() {
            SortedSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            // Natural ordering uses null comparator
            assertThat(multimap.valueComparator()).isNull();
        }

        @Test
        @DisplayName("removeAll返回排序集合")
        void testRemoveAll() {
            SortedSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("nums", 3);
            multimap.put("nums", 1);

            SortedSet<Integer> removed = multimap.removeAll("nums");

            assertThat(removed).containsExactly(1, 3);
        }
    }
}
