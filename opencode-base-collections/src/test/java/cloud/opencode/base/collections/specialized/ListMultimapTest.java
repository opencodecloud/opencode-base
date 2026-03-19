package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.ArrayListMultimap;
import cloud.opencode.base.collections.Multimap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ListMultimap 接口测试
 * 通过 ArrayListMultimap 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ListMultimap 接口测试")
class ListMultimapTest {

    private ListMultimap<String, Integer> createListMultimap() {
        return MultimapBuilder.linkedHashKeys().arrayListValues().build();
    }

    @Nested
    @DisplayName("get 方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("get - 返回 List")
        void testGetReturnsList() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> values = multimap.get("a");

            assertThat(values).isInstanceOf(List.class);
            assertThat(values).containsExactly(1, 2);
        }

        @Test
        @DisplayName("get - 空键返回空列表")
        void testGetNonExistent() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            List<Integer> values = multimap.get("nonexistent");

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("get - 保留插入顺序")
        void testGetPreservesOrder() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 3);
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> values = multimap.get("a");

            assertThat(values).containsExactly(3, 1, 2);
        }

        @Test
        @DisplayName("get - 允许重复值")
        void testGetAllowsDuplicates() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 1);
            multimap.put("a", 1);

            List<Integer> values = multimap.get("a");

            assertThat(values).containsExactly(1, 1, 1);
        }
    }

    @Nested
    @DisplayName("removeAll 方法测试")
    class RemoveAllMethodTests {

        @Test
        @DisplayName("removeAll - 返回 List")
        void testRemoveAllReturnsList() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> removed = multimap.removeAll("a");

            assertThat(removed).isInstanceOf(List.class);
            assertThat(removed).containsExactly(1, 2);
        }

        @Test
        @DisplayName("removeAll - 移除后为空")
        void testRemoveAllEmpty() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            multimap.removeAll("a");

            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("removeAll - 不存在的键返回空列表")
        void testRemoveAllNonExistent() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            List<Integer> removed = multimap.removeAll("nonexistent");

            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("removeAll - 保留顺序")
        void testRemoveAllPreservesOrder() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 3);
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> removed = multimap.removeAll("a");

            assertThat(removed).containsExactly(3, 1, 2);
        }
    }

    @Nested
    @DisplayName("replaceValues 方法测试")
    class ReplaceValuesMethodTests {

        @Test
        @DisplayName("replaceValues - 返回旧值列表")
        void testReplaceValuesReturnsOld() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> old = multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(old).isInstanceOf(List.class);
            assertThat(old).containsExactly(1, 2);
        }

        @Test
        @DisplayName("replaceValues - 新值生效")
        void testReplaceValuesNewValues() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);

            multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(multimap.get("a")).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("replaceValues - 不存在的键返回空列表")
        void testReplaceValuesNonExistent() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            List<Integer> old = multimap.replaceValues("a", List.of(1, 2));

            assertThat(old).isEmpty();
            assertThat(multimap.get("a")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("replaceValues - 替换为空列表")
        void testReplaceValuesEmpty() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> old = multimap.replaceValues("a", List.of());

            assertThat(old).containsExactly(1, 2);
            assertThat(multimap.containsKey("a")).isFalse();
        }
    }

    @Nested
    @DisplayName("Multimap 继承方法测试")
    class MultimapMethodTests {

        @Test
        @DisplayName("继承 Multimap 接口")
        void testExtendsMultimap() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            assertThat(multimap).isInstanceOf(Multimap.class);
        }

        @Test
        @DisplayName("put - 添加值")
        void testPut() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("put - 添加重复值")
        void testPutDuplicate() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);

            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.get("a")).containsExactly(1, 1);
        }

        @Test
        @DisplayName("size - 总条目数")
        void testSize() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 包含条目")
        void testContainsEntry() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isFalse();
        }

        @Test
        @DisplayName("remove - 移除条目")
        void testRemove() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            boolean result = multimap.remove("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.get("a")).containsExactly(2);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("b", 2);

            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("values - 所有值")
        void testValues() {
            ListMultimap<String, Integer> multimap = createListMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("大量重复值")
        void testManyDuplicates() {
            ListMultimap<String, Integer> multimap = createListMultimap();

            for (int i = 0; i < 100; i++) {
                multimap.put("key", 1);
            }

            assertThat(multimap.get("key")).hasSize(100);
            assertThat(multimap.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("多键多值")
        void testManyKeysAndValues() {
            ListMultimap<Integer, Integer> multimap = MultimapBuilder.linkedHashKeys()
                    .arrayListValues()
                    .build();

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    multimap.put(i, j);
                }
            }

            assertThat(multimap.size()).isEqualTo(100);
            assertThat(multimap.keySet()).hasSize(10);
        }
    }
}
