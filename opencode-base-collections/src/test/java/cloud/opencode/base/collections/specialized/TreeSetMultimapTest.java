package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.ArrayListMultimap;
import cloud.opencode.base.collections.Multimap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeSetMultimap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeSetMultimap 测试")
class TreeSetMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射（自然排序）")
        void testCreate() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 指定值比较器")
        void testCreateWithValueComparator() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create(
                    Comparator.reverseOrder());

            multimap.put("key", 1);
            multimap.put("key", 3);
            multimap.put("key", 2);

            SortedSet<Integer> values = multimap.get("key");
            assertThat(values).containsExactly(3, 2, 1); // 反序
        }

        @Test
        @DisplayName("create - 指定键和值比较器")
        void testCreateWithBothComparators() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create(
                    String.CASE_INSENSITIVE_ORDER,
                    Comparator.naturalOrder());

            multimap.put("A", 1);
            multimap.put("a", 2); // 相同键（大小写不敏感）

            assertThat(multimap.get("A")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("create - 从现有Multimap创建")
        void testCreateFromMultimap() {
            ArrayListMultimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 3);
            source.put("a", 1);
            source.put("a", 2);

            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create(source);

            assertThat(multimap.get("a")).containsExactly(1, 2, 3); // 排序后
        }

        @Test
        @DisplayName("create - 从现有Multimap创建带比较器")
        void testCreateFromMultimapWithComparator() {
            ArrayListMultimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 1);
            source.put("a", 2);
            source.put("a", 3);

            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create(
                    source, Comparator.reverseOrder());

            assertThat(multimap.get("a")).containsExactly(3, 2, 1); // 反序
        }
    }

    @Nested
    @DisplayName("值排序测试")
    class ValueOrderingTests {

        @Test
        @DisplayName("put - 值自动排序")
        void testValuesSorted() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("numbers", 5);
            multimap.put("numbers", 2);
            multimap.put("numbers", 8);
            multimap.put("numbers", 1);

            SortedSet<Integer> values = multimap.get("numbers");

            assertThat(values).containsExactly(1, 2, 5, 8);
        }

        @Test
        @DisplayName("put - 重复值不添加")
        void testNoDuplicateValues() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 1);
            multimap.put("key", 1);

            assertThat(multimap.get("key")).containsExactly(1);
            assertThat(multimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("valueComparator - 返回值比较器")
        void testValueComparator() {
            Comparator<Integer> comparator = Comparator.reverseOrder();
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create(comparator);

            assertThat(multimap.valueComparator()).isEqualTo(comparator);
        }
    }

    @Nested
    @DisplayName("SortedSet 方法测试")
    class SortedSetMethodsTests {

        @Test
        @DisplayName("get - 返回SortedSet")
        void testGetReturnsSortedSet() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 3);
            multimap.put("key", 1);
            multimap.put("key", 2);

            SortedSet<Integer> values = multimap.get("key");

            assertThat(values).isInstanceOf(SortedSet.class);
            assertThat(values.first()).isEqualTo(1);
            assertThat(values.last()).isEqualTo(3);
        }

        @Test
        @DisplayName("get - 不存在的键返回空SortedSet")
        void testGetNonExistentKey() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            SortedSet<Integer> values = multimap.get("nonexistent");

            assertThat(values).isEmpty();
            assertThat(values).isInstanceOf(SortedSet.class);
        }

        @Test
        @DisplayName("first - 获取最小值")
        void testFirst() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 5);
            multimap.put("key", 2);
            multimap.put("key", 8);

            Optional<Integer> first = multimap.first("key");

            assertThat(first).hasValue(2);
        }

        @Test
        @DisplayName("first - 不存在的键返回空")
        void testFirstNonExistent() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            Optional<Integer> first = multimap.first("key");

            assertThat(first).isEmpty();
        }

        @Test
        @DisplayName("last - 获取最大值")
        void testLast() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 5);
            multimap.put("key", 2);
            multimap.put("key", 8);

            Optional<Integer> last = multimap.last("key");

            assertThat(last).hasValue(8);
        }

        @Test
        @DisplayName("last - 不存在的键返回空")
        void testLastNonExistent() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            Optional<Integer> last = multimap.last("key");

            assertThat(last).isEmpty();
        }

        @Test
        @DisplayName("headSet - 获取小于指定值的子集")
        void testHeadSet() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 3);
            multimap.put("key", 4);
            multimap.put("key", 5);

            SortedSet<Integer> headSet = multimap.headSet("key", 3);

            assertThat(headSet).containsExactly(1, 2);
        }

        @Test
        @DisplayName("tailSet - 获取大于等于指定值的子集")
        void testTailSet() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 3);
            multimap.put("key", 4);
            multimap.put("key", 5);

            SortedSet<Integer> tailSet = multimap.tailSet("key", 3);

            assertThat(tailSet).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("subSet - 获取范围子集")
        void testSubSet() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 3);
            multimap.put("key", 4);
            multimap.put("key", 5);

            SortedSet<Integer> subSet = multimap.subSet("key", 2, 4);

            assertThat(subSet).containsExactly(2, 3);
        }
    }

    @Nested
    @DisplayName("Multimap 操作测试")
    class MultimapOperationsTests {

        @Test
        @DisplayName("removeAll - 移除所有值")
        void testRemoveAll() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);
            multimap.put("key", 3);

            SortedSet<Integer> removed = multimap.removeAll("key");

            assertThat(removed).containsExactly(1, 2, 3);
            assertThat(multimap.get("key")).isEmpty();
        }

        @Test
        @DisplayName("removeAll - 不存在的键返回空集")
        void testRemoveAllNonExistent() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            SortedSet<Integer> removed = multimap.removeAll("key");

            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("replaceValues - 替换所有值")
        void testReplaceValues() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);

            SortedSet<Integer> oldValues = multimap.replaceValues("key", List.of(5, 3, 4));

            assertThat(oldValues).containsExactly(1, 2);
            assertThat(multimap.get("key")).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("replaceValues - null值清除所有")
        void testReplaceValuesWithNull() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);

            SortedSet<Integer> oldValues = multimap.replaceValues("key", null);

            assertThat(oldValues).containsExactly(1, 2);
            assertThat(multimap.containsKey("key")).isFalse();
        }

        @Test
        @DisplayName("entries - 获取所有条目")
        void testEntries() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Set<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(3);
        }

        @Test
        @DisplayName("asMap - 作为Map视图")
        void testAsMap() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Map<String, Collection<Integer>> map = multimap.asMap();

            assertThat(map).containsKeys("a", "b");
            assertThat(map.get("a")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("asMapOfSortedSets - 作为SortedSet Map视图")
        void testAsMapOfSortedSets() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("a", 2);
            multimap.put("a", 1);

            Map<String, SortedSet<Integer>> map = multimap.asMapOfSortedSets();

            assertThat(map.get("a")).containsExactly(1, 2);
            assertThat(map.get("a")).isInstanceOf(SortedSet.class);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("put - 添加键值对")
        void testPut() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            assertThat(multimap.put("key", 1)).isTrue();
            assertThat(multimap.put("key", 2)).isTrue();

            assertThat(multimap.get("key")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("putAll - 批量添加")
        void testPutAll() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.putAll("key", List.of(3, 1, 2));

            assertThat(multimap.get("key")).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("remove - 移除键值对")
        void testRemove() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);
            multimap.put("key", 2);

            assertThat(multimap.remove("key", 1)).isTrue();
            assertThat(multimap.get("key")).containsExactly(2);
        }

        @Test
        @DisplayName("containsKey - 判断键是否存在")
        void testContainsKey() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);

            assertThat(multimap.containsKey("key")).isTrue();
            assertThat(multimap.containsKey("other")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 判断值是否存在")
        void testContainsValue() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("key", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("size - 返回总条目数")
        void testSize() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 判断是否为空")
        void testIsEmpty() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();
            multimap.put("key", 1);
            assertThat(multimap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            TreeSetMultimap<String, Integer> multimap = TreeSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);
            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
        }
    }
}
