package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MultimapBuilder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("MultimapBuilder 测试")
class MultimapBuilderTest {

    @Nested
    @DisplayName("ListMultimap 构建测试")
    class ListMultimapBuildTests {

        @Test
        @DisplayName("hashKeys.arrayListValues - 构建")
        void testHashKeysArrayListValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 1);

            assertThat(multimap.get("key")).containsExactly(1, 2, 1);
        }

        @Test
        @DisplayName("hashKeys(size).arrayListValues(size) - 指定大小")
        void testHashKeysWithSizeArrayListValuesWithSize() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys(16)
                    .arrayListValues(8)
                    .build();

            multimap.put("key", 1);

            assertThat(multimap.get("key")).containsExactly(1);
        }

        @Test
        @DisplayName("linkedHashKeys.linkedListValues - 构建")
        void testLinkedHashKeysLinkedListValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .linkedHashKeys()
                    .linkedListValues()
                    .build();

            multimap.put("b", 1);
            multimap.put("a", 2);
            multimap.put("c", 3);

            assertThat(new ArrayList<>(multimap.keySet())).containsExactly("b", "a", "c");
        }

        @Test
        @DisplayName("treeKeys.arrayListValues - 排序键")
        void testTreeKeysArrayListValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .<String>treeKeys()
                    .arrayListValues()
                    .build();

            multimap.put("c", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(new ArrayList<>(multimap.keySet())).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("treeKeys(comparator) - 自定义比较器")
        void testTreeKeysWithComparator() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .<String>treeKeys(Comparator.reverseOrder())
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("c", 2);
            multimap.put("b", 3);

            assertThat(new ArrayList<>(multimap.keySet())).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("SetMultimap 构建测试")
    class SetMultimapBuildTests {

        @Test
        @DisplayName("hashKeys.hashSetValues - 构建")
        void testHashKeysHashSetValues() {
            SetMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .hashSetValues()
                    .build();

            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 1);

            assertThat(multimap.get("key")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("hashKeys(size).hashSetValues(size) - 指定大小")
        void testHashKeysWithSizeHashSetValuesWithSize() {
            SetMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys(16)
                    .hashSetValues(8)
                    .build();

            multimap.put("key", 1);

            assertThat(multimap.get("key")).containsExactly(1);
        }

        @Test
        @DisplayName("linkedHashKeys.linkedHashSetValues - 保持顺序")
        void testLinkedHashKeysLinkedHashSetValues() {
            SetMultimap<String, Integer> multimap = MultimapBuilder
                    .linkedHashKeys()
                    .linkedHashSetValues()
                    .build();

            multimap.put("key", 3);
            multimap.put("key", 1);
            multimap.put("key", 2);

            assertThat(new ArrayList<>(multimap.get("key"))).containsExactly(3, 1, 2);
        }

        @Test
        @DisplayName("treeKeys.treeSetValues - 排序键和值")
        void testTreeKeysTreeSetValues() {
            SetMultimap<String, Integer> multimap = MultimapBuilder
                    .<String>treeKeys()
                    .treeSetValues()
                    .build();

            multimap.put("key", 3);
            multimap.put("key", 1);
            multimap.put("key", 2);

            assertThat(new ArrayList<>(multimap.get("key"))).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Multimap 操作测试")
    class MultimapOperationTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            assertThat(multimap.isEmpty()).isTrue();

            multimap.put("key", 1);

            assertThat(multimap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);

            assertThat(multimap.containsKey("key")).isTrue();
            assertThat(multimap.containsKey("other")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 包含条目")
        void testContainsEntry() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);

            assertThat(multimap.containsEntry("key", 1)).isTrue();
            assertThat(multimap.containsEntry("key", 2)).isFalse();
        }

        @Test
        @DisplayName("remove - 移除")
        void testRemove() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);
            multimap.put("key", 2);

            boolean removed = multimap.remove("key", 1);

            assertThat(removed).isTrue();
            assertThat(multimap.get("key")).containsExactly(2);
        }

        @Test
        @DisplayName("removeAll - 移除所有")
        void testRemoveAll() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);
            multimap.put("key", 2);

            List<Integer> removed = multimap.removeAll("key");

            assertThat(removed).containsExactly(1, 2);
            assertThat(multimap.containsKey("key")).isFalse();
        }

        @Test
        @DisplayName("replaceValues - 替换值")
        void testReplaceValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);

            List<Integer> old = multimap.replaceValues("key", List.of(2, 3));

            assertThat(old).containsExactly(1);
            assertThat(multimap.get("key")).containsExactly(2, 3);
        }

        @Test
        @DisplayName("putAll(key, values) - 批量放置")
        void testPutAllKeyValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.putAll("key", List.of(1, 2, 3));

            assertThat(multimap.get("key")).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("putAll(multimap) - 批量放置")
        void testPutAllMultimap() {
            ListMultimap<String, Integer> source = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            source.put("key", 1);

            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap.putAll(source);

            assertThat(multimap.get("key")).containsExactly(1);
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("key", 1);
            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("keySet - 键集")
        void testKeySet() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("entries - 条目集合")
        void testEntries() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("a", 2);

            assertThat(multimap.entries()).hasSize(2);
        }

        @Test
        @DisplayName("asMap - 转为 Map")
        void testAsMap() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();

            multimap.put("a", 1);
            multimap.put("a", 2);

            Map<String, ? extends Collection<Integer>> map = multimap.asMap();

            assertThat(map).containsKey("a");
            assertThat(map.get("a")).containsExactly(1, 2);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ListMultimap<String, Integer> multimap1 = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap1.put("key", 1);

            ListMultimap<String, Integer> multimap2 = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap2.put("key", 1);

            assertThat(multimap1).isEqualTo(multimap2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ListMultimap<String, Integer> multimap1 = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap1.put("key", 1);

            ListMultimap<String, Integer> multimap2 = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap2.put("key", 1);

            assertThat(multimap1.hashCode()).isEqualTo(multimap2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ListMultimap<String, Integer> multimap = MultimapBuilder
                    .hashKeys()
                    .arrayListValues()
                    .build();
            multimap.put("key", 1);

            assertThat(multimap.toString()).contains("key").contains("1");
        }
    }
}
