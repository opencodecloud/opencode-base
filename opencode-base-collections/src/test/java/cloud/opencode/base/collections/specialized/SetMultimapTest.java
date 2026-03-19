package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.HashSetMultimap;
import cloud.opencode.base.collections.Multimap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * SetMultimap 接口测试
 * 通过 HashSetMultimap 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SetMultimap 接口测试")
class SetMultimapTest {

    private SetMultimap<String, Integer> createSetMultimap() {
        return MultimapBuilder.linkedHashKeys().linkedHashSetValues().build();
    }

    @Nested
    @DisplayName("get 方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("get - 返回 Set")
        void testGetReturnsSet() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Set<Integer> values = multimap.get("a");

            assertThat(values).isInstanceOf(Set.class);
            assertThat(values).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("get - 空键返回空集合")
        void testGetNonExistent() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            Set<Integer> values = multimap.get("nonexistent");

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("get - 不允许重复值")
        void testGetNoDuplicates() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 1);
            multimap.put("a", 1);

            Set<Integer> values = multimap.get("a");

            assertThat(values).containsExactly(1);
        }
    }

    @Nested
    @DisplayName("removeAll 方法测试")
    class RemoveAllMethodTests {

        @Test
        @DisplayName("removeAll - 返回 Set")
        void testRemoveAllReturnsSet() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Set<Integer> removed = multimap.removeAll("a");

            assertThat(removed).isInstanceOf(Set.class);
            assertThat(removed).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("removeAll - 移除后为空")
        void testRemoveAllEmpty() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            multimap.removeAll("a");

            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("removeAll - 不存在的键返回空集合")
        void testRemoveAllNonExistent() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            Set<Integer> removed = multimap.removeAll("nonexistent");

            assertThat(removed).isEmpty();
        }
    }

    @Nested
    @DisplayName("replaceValues 方法测试")
    class ReplaceValuesMethodTests {

        @Test
        @DisplayName("replaceValues - 返回旧值集合")
        void testReplaceValuesReturnsOld() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Set<Integer> old = multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(old).isInstanceOf(Set.class);
            assertThat(old).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("replaceValues - 新值生效")
        void testReplaceValuesNewValues() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(3, 4, 5);
        }

        @Test
        @DisplayName("replaceValues - 新值去重")
        void testReplaceValuesDedup() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            multimap.replaceValues("a", List.of(1, 1, 1, 2, 2));

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("replaceValues - 不存在的键返回空集合")
        void testReplaceValuesNonExistent() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            Set<Integer> old = multimap.replaceValues("a", List.of(1, 2));

            assertThat(old).isEmpty();
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("replaceValues - 替换为空集合")
        void testReplaceValuesEmpty() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Set<Integer> old = multimap.replaceValues("a", List.of());

            assertThat(old).containsExactlyInAnyOrder(1, 2);
            assertThat(multimap.containsKey("a")).isFalse();
        }
    }

    @Nested
    @DisplayName("entries 方法测试")
    class EntriesMethodTests {

        @Test
        @DisplayName("entries - 返回 Set")
        void testEntriesReturnsSet() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("b", 2);

            Set<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).isInstanceOf(Set.class);
            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("entries - 无重复条目")
        void testEntriesNoDuplicates() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 1);  // 重复，被忽略

            Set<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Multimap 继承方法测试")
    class MultimapMethodTests {

        @Test
        @DisplayName("继承 Multimap 接口")
        void testExtendsMultimap() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            assertThat(multimap).isInstanceOf(Multimap.class);
        }

        @Test
        @DisplayName("put - 添加值")
        void testPut() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("put - 添加重复值返回 false")
        void testPutDuplicate() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            boolean result = multimap.put("a", 1);

            assertThat(result).isFalse();
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("size - 总条目数（去重后）")
        void testSize() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("a", 1);  // 重复，被忽略
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 包含条目")
        void testContainsEntry() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isFalse();
        }

        @Test
        @DisplayName("remove - 移除条目")
        void testRemove() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);

            boolean result = multimap.remove("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.get("a")).containsExactly(2);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("b", 2);

            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("values - 所有值")
        void testValues() {
            SetMultimap<String, Integer> multimap = createSetMultimap();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("与 ListMultimap 对比测试")
    class ComparisonWithListMultimapTests {

        @Test
        @DisplayName("SetMultimap 去重，ListMultimap 保留重复")
        void testDeduplicationDifference() {
            SetMultimap<String, Integer> setMultimap = createSetMultimap();
            setMultimap.put("a", 1);
            setMultimap.put("a", 1);
            setMultimap.put("a", 1);

            ListMultimap<String, Integer> listMultimap = MultimapBuilder.linkedHashKeys()
                    .arrayListValues()
                    .build();
            listMultimap.put("a", 1);
            listMultimap.put("a", 1);
            listMultimap.put("a", 1);

            assertThat(setMultimap.size()).isEqualTo(1);
            assertThat(listMultimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("put 返回值不同")
        void testPutReturnValueDifference() {
            SetMultimap<String, Integer> setMultimap = createSetMultimap();
            setMultimap.put("a", 1);

            ListMultimap<String, Integer> listMultimap = MultimapBuilder.linkedHashKeys()
                    .arrayListValues()
                    .build();
            listMultimap.put("a", 1);

            // 第二次 put 相同值
            assertThat(setMultimap.put("a", 1)).isFalse();  // Set 返回 false
            assertThat(listMultimap.put("a", 1)).isTrue();  // List 返回 true
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("大量唯一值")
        void testManyUniqueValues() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            for (int i = 0; i < 100; i++) {
                multimap.put("key", i);
            }

            assertThat(multimap.get("key")).hasSize(100);
        }

        @Test
        @DisplayName("多键多值")
        void testManyKeysAndValues() {
            SetMultimap<Integer, Integer> multimap = MultimapBuilder.linkedHashKeys()
                    .linkedHashSetValues()
                    .build();

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    multimap.put(i, j);
                }
            }

            assertThat(multimap.size()).isEqualTo(100);
            assertThat(multimap.keySet()).hasSize(10);
        }

        @Test
        @DisplayName("高效包含检查")
        void testEfficientContains() {
            SetMultimap<String, Integer> multimap = createSetMultimap();

            for (int i = 0; i < 1000; i++) {
                multimap.put("key", i);
            }

            // Set 的 contains 应该是 O(1)
            assertThat(multimap.get("key").contains(500)).isTrue();
            assertThat(multimap.get("key").contains(5000)).isFalse();
        }
    }
}
