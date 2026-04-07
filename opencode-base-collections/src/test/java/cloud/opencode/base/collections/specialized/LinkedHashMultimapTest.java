package cloud.opencode.base.collections.specialized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * LinkedHashMultimap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("LinkedHashMultimap 测试")
class LinkedHashMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定预期键数")
        void testCreateWithExpectedKeys() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create(16);

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }

        @Test
        @DisplayName("create - 负数预期键数抛出异常")
        void testCreateWithNegativeExpectedKeys() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LinkedHashMultimap.create(-1))
                    .withMessageContaining("negative");
        }
    }

    @Nested
    @DisplayName("插入顺序测试")
    class InsertionOrderTests {

        @Test
        @DisplayName("put - 保持键的插入顺序")
        void testKeyInsertionOrder() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("c", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.keySet()).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("put - 保持值的插入顺序")
        void testValueInsertionOrder() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("key", 3);
            multimap.put("key", 1);
            multimap.put("key", 2);

            Collection<Integer> values = multimap.get("key");
            assertThat(values).containsExactly(3, 1, 2);
        }

        @Test
        @DisplayName("entries - 条目按插入顺序迭代")
        void testEntriesInsertionOrder() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("b", 2);
            multimap.put("a", 1);
            multimap.put("b", 3);
            multimap.put("a", 4);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();
            List<String> keys = entries.stream().map(Map.Entry::getKey).toList();
            List<Integer> vals = entries.stream().map(Map.Entry::getValue).toList();

            assertThat(keys).containsExactly("b", "b", "a", "a");
            assertThat(vals).containsExactly(2, 3, 1, 4);
        }
    }

    @Nested
    @DisplayName("集合语义测试")
    class SetSemanticsTests {

        @Test
        @DisplayName("put - 重复值被忽略")
        void testDuplicateValuesIgnored() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            boolean first = multimap.put("key", 1);
            boolean second = multimap.put("key", 1);

            assertThat(first).isTrue();
            assertThat(second).isFalse();
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("key")).containsExactly(1);
        }

        @Test
        @DisplayName("get - 返回 LinkedHashSet")
        void testGetReturnsLinkedHashSet() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("key", 1);
            multimap.put("key", 2);

            Collection<Integer> values = multimap.get("key");
            assertThat(values).isInstanceOf(LinkedHashSet.class);
        }

        @Test
        @DisplayName("get - 不存在的键返回空集合")
        void testGetNonExistentKeyReturnsEmpty() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            Collection<Integer> values = multimap.get("missing");

            assertThat(values).isEmpty();
            assertThat(values).isInstanceOf(LinkedHashSet.class);
        }
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationTests {

        @Test
        @DisplayName("size - 返回键值对总数")
        void testSize() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空映射返回 true")
        void testIsEmpty() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();

            multimap.put("a", 1);

            assertThat(multimap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 检查键是否存在")
        void testContainsKey() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 检查值是否存在")
        void testContainsValue() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 检查键值对是否存在")
        void testContainsEntry() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isFalse();
            assertThat(multimap.containsEntry("b", 1)).isFalse();
        }
    }

    @Nested
    @DisplayName("移除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 移除单个键值对")
        void testRemoveSingleEntry() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);

            boolean removed = multimap.remove("a", 1);

            assertThat(removed).isTrue();
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(2);
        }

        @Test
        @DisplayName("remove - 移除不存在的键值对返回 false")
        void testRemoveNonExistent() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);

            assertThat(multimap.remove("a", 2)).isFalse();
            assertThat(multimap.remove("b", 1)).isFalse();
        }

        @Test
        @DisplayName("remove - 移除最后一个值时清除键")
        void testRemoveLastValueClearsKey() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.remove("a", 1);

            assertThat(multimap.containsKey("a")).isFalse();
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("removeAll - 移除键的所有值")
        void testRemoveAll() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("a", 3);
            multimap.put("b", 4);

            Collection<Integer> removed = multimap.removeAll("a");

            assertThat(removed).containsExactly(1, 2, 3);
            assertThat(multimap.containsKey("a")).isFalse();
            assertThat(multimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeAll - 不存在的键返回空集合")
        void testRemoveAllNonExistentKey() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            Collection<Integer> removed = multimap.removeAll("missing");

            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("clear - 清除所有条目")
        void testClear() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("b", 2);
            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }
    }

    @Nested
    @DisplayName("putAll 测试")
    class PutAllTests {

        @Test
        @DisplayName("putAll - 批量添加值")
        void testPutAll() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            boolean changed = multimap.putAll("a", List.of(1, 2, 3));

            assertThat(changed).isTrue();
            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - 重复值不增加大小")
        void testPutAllWithDuplicates() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.putAll("a", List.of(1, 2, 3));

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactly(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("视图测试")
    class ViewTests {

        @Test
        @DisplayName("asMap - 返回不可修改的映射视图")
        void testAsMap() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("b", 2);

            var mapView = multimap.asMap();

            assertThat(mapView).containsOnlyKeys("a", "b");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> mapView.put("c", List.of(3)));
        }

        @Test
        @DisplayName("values - 返回所有值")
        void testValues() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.values()).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("replaceValues - 替换键的所有值")
        void testReplaceValues() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", List.of(3, 4));

            assertThat(old).containsExactly(1, 2);
            assertThat(multimap.get("a")).containsExactly(3, 4);
        }
    }

    @Nested
    @DisplayName("equals 和 hashCode 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals - 相同内容的映射相等")
        void testEquals() {
            LinkedHashMultimap<String, Integer> m1 = LinkedHashMultimap.create();
            LinkedHashMultimap<String, Integer> m2 = LinkedHashMultimap.create();

            m1.put("a", 1);
            m1.put("b", 2);
            m2.put("a", 1);
            m2.put("b", 2);

            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }

        @Test
        @DisplayName("toString - 返回可读字符串")
        void testToString() {
            LinkedHashMultimap<String, Integer> multimap = LinkedHashMultimap.create();

            multimap.put("a", 1);

            assertThat(multimap.toString()).contains("a");
            assertThat(multimap.toString()).contains("1");
        }
    }
}
