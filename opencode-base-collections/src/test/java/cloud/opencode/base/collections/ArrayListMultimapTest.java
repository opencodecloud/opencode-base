package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ArrayListMultimap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ArrayListMultimap 测试")
class ArrayListMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Multimap")
        void testCreate() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            assertThat(multimap).isNotNull();
            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create(32, 8);

            assertThat(multimap).isNotNull();
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> ArrayListMultimap.create(-1, 4))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> ArrayListMultimap.create(16, -1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("create - 从现有 Multimap 创建")
        void testCreateFromMultimap() {
            ArrayListMultimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 1);
            source.put("a", 2);
            source.put("b", 3);

            ArrayListMultimap<String, Integer> copy = ArrayListMultimap.create(source);

            assertThat(copy.size()).isEqualTo(3);
            assertThat(copy.get("a")).containsExactly(1, 2);
            assertThat(copy.get("b")).containsExactly(3);
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 添加单个值")
        void testPut() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("put - 允许重复值")
        void testPutDuplicateValue() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put("a", 1);
            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();  // ArrayList allows duplicates
            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.get("a")).containsExactly(1, 1);
        }

        @Test
        @DisplayName("put - 多个键")
        void testPutMultipleKeys() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactly(1, 2);
            assertThat(multimap.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("put - null 键和值")
        void testPutNullKeyAndValue() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put(null, 1);
            multimap.put("a", null);
            multimap.put(null, null);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get(null)).containsExactly(1, null);
            assertThat(multimap.get("a")).containsExactly((Integer) null);
        }

        @Test
        @DisplayName("putAll - 从 Map 添加")
        void testPutAllFromMap() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            Map<String, Integer> map = Map.of("a", 1, "b", 2);

            multimap.putAll(map);

            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("b", 2)).isTrue();
        }

        @Test
        @DisplayName("putAll - 从 Multimap 添加")
        void testPutAllFromMultimap() {
            ArrayListMultimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 1);
            source.put("a", 2);

            ArrayListMultimap<String, Integer> target = ArrayListMultimap.create();
            target.putAll(source);

            assertThat(target.size()).isEqualTo(2);
            assertThat(target.get("a")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("putAll - 为键添加多个值")
        void testPutAllForKey() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            boolean result = multimap.putAll("a", List.of(1, 2, 3));

            assertThat(result).isTrue();
            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - null 值不改变")
        void testPutAllNull() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.putAll((Map<String, Integer>) null);
            multimap.putAll((Multimap<String, Integer>) null);
            boolean result = multimap.putAll("a", null);

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("get 操作测试")
    class GetOperationTests {

        @Test
        @DisplayName("get - 获取存在的键")
        void testGet() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> values = multimap.get("a");

            assertThat(values).containsExactly(1, 2);
        }

        @Test
        @DisplayName("get - 获取不存在的键")
        void testGetNonExistent() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            Collection<Integer> values = multimap.get("nonexistent");

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("getList - 获取列表")
        void testGetList() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            List<Integer> list = multimap.getList("a");

            assertThat(list).containsExactly(1, 2);
            assertThat(list).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("getList - 不存在的键返回空列表")
        void testGetListNonExistent() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            List<Integer> list = multimap.getList("nonexistent");

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(ArrayList.class);
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("containsKey - 存在的键")
        void testContainsKey() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 存在的值")
        void testContainsValue() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isTrue();
            assertThat(multimap.containsValue(3)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 存在的条目")
        void testContainsEntry() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isTrue();
            assertThat(multimap.containsEntry("a", 3)).isFalse();
            assertThat(multimap.containsEntry("b", 1)).isFalse();
        }
    }

    @Nested
    @DisplayName("remove 操作测试")
    class RemoveOperationTests {

        @Test
        @DisplayName("remove - 移除单个条目")
        void testRemove() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            boolean result = multimap.remove("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(2);
        }

        @Test
        @DisplayName("remove - 移除不存在的条目")
        void testRemoveNonExistent() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            boolean result = multimap.remove("a", 2);

            assertThat(result).isFalse();
            assertThat(multimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("remove - 移除最后一个值时删除键")
        void testRemoveLastValue() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            multimap.remove("a", 1);

            assertThat(multimap.containsKey("a")).isFalse();
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("removeAll - 移除键的所有值")
        void testRemoveAll() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Collection<Integer> removed = multimap.removeAll("a");

            assertThat(removed).containsExactly(1, 2);
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("removeAll - 移除不存在的键")
        void testRemoveAllNonExistent() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            Collection<Integer> removed = multimap.removeAll("nonexistent");

            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("replaceValues - 替换键的所有值")
        void testReplaceValues() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(old).containsExactly(1, 2);
            assertThat(multimap.get("a")).containsExactly(3, 4, 5);
            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("replaceValues - null 值清除键")
        void testReplaceValuesNull() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", null);

            assertThat(old).containsExactly(1, 2);
            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("clear - 清空所有")
        void testClear() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            multimap.clear();

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }
    }

    @Nested
    @DisplayName("视图测试")
    class ViewTests {

        @Test
        @DisplayName("keySet - 键集合视图")
        void testKeySet() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);
            multimap.put("a", 3);

            Set<String> keys = multimap.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("keys - 键多重集视图")
        void testKeys() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Multiset<String> keys = multimap.keys();

            assertThat(keys.count("a")).isEqualTo(2);
            assertThat(keys.count("b")).isEqualTo(1);
        }

        @Test
        @DisplayName("values - 所有值视图")
        void testValues() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Collection<Integer> values = multimap.values();

            assertThat(values).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("entries - 条目视图")
        void testEntries() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(2);
            assertThat(entries).extracting(Map.Entry::getKey).containsOnly("a");
            assertThat(entries).extracting(Map.Entry::getValue).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("asMap - 映射视图")
        void testAsMap() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Map<String, ? extends Collection<Integer>> map = multimap.asMap();

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).containsExactly(1, 2);
            assertThat(map.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("asMap - 不可修改")
        void testAsMapUnmodifiable() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            Map<String, Collection<Integer>> map = multimap.asMap();

            assertThatThrownBy(() -> map.put("b", List.of(2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("插入顺序测试")
    class InsertionOrderTests {

        @Test
        @DisplayName("保持值的插入顺序")
        void testInsertionOrder() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            multimap.put("a", 3);
            multimap.put("a", 1);
            multimap.put("a", 2);

            assertThat(multimap.get("a")).containsExactly(3, 1, 2);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            ArrayListMultimap<String, Integer> multimap1 = ArrayListMultimap.create();
            multimap1.put("a", 1);
            multimap1.put("a", 2);

            ArrayListMultimap<String, Integer> multimap2 = ArrayListMultimap.create();
            multimap2.put("a", 1);
            multimap2.put("a", 2);

            assertThat(multimap1).isEqualTo(multimap2);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.equals(multimap)).isTrue();
        }

        @Test
        @DisplayName("equals - 不相等")
        void testEqualsNotEqual() {
            ArrayListMultimap<String, Integer> multimap1 = ArrayListMultimap.create();
            multimap1.put("a", 1);

            ArrayListMultimap<String, Integer> multimap2 = ArrayListMultimap.create();
            multimap2.put("a", 2);

            assertThat(multimap1).isNotEqualTo(multimap2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            ArrayListMultimap<String, Integer> multimap1 = ArrayListMultimap.create();
            multimap1.put("a", 1);

            ArrayListMultimap<String, Integer> multimap2 = ArrayListMultimap.create();
            multimap2.put("a", 1);

            assertThat(multimap1.hashCode()).isEqualTo(multimap2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            String str = multimap.toString();

            assertThat(str).contains("a");
            assertThat(str).contains("1");
            assertThat(str).contains("2");
        }
    }

    @Nested
    @DisplayName("size/isEmpty 测试")
    class SizeTests {

        @Test
        @DisplayName("size - 统计总条目数")
        void testSize() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            assertThat(multimap.size()).isZero();

            multimap.put("a", 1);
            assertThat(multimap.size()).isEqualTo(1);

            multimap.put("a", 2);
            assertThat(multimap.size()).isEqualTo(2);

            multimap.put("b", 3);
            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();

            multimap.put("a", 1);
            assertThat(multimap.isEmpty()).isFalse();

            multimap.remove("a", 1);
            assertThat(multimap.isEmpty()).isTrue();
        }
    }
}
