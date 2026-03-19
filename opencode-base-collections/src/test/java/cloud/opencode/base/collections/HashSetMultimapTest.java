package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HashSetMultimap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("HashSetMultimap 测试")
class HashSetMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 Multimap")
        void testCreate() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            assertThat(multimap).isNotNull();
            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create(32, 8);

            assertThat(multimap).isNotNull();
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> HashSetMultimap.create(-1, 4))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> HashSetMultimap.create(16, -1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("create - 从现有 Multimap 创建")
        void testCreateFromMultimap() {
            HashSetMultimap<String, Integer> source = HashSetMultimap.create();
            source.put("a", 1);
            source.put("a", 2);
            source.put("b", 3);

            HashSetMultimap<String, Integer> copy = HashSetMultimap.create(source);

            assertThat(copy.size()).isEqualTo(3);
            assertThat(copy.get("a")).containsExactlyInAnyOrder(1, 2);
            assertThat(copy.get("b")).containsExactly(3);
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 添加单个值")
        void testPut() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            boolean result = multimap.put("a", 1);

            assertThat(result).isTrue();
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("put - 不允许重复值")
        void testPutDuplicateValue() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            multimap.put("a", 1);
            boolean result = multimap.put("a", 1);

            assertThat(result).isFalse();  // HashSet does not allow duplicates
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("put - 多个键")
        void testPutMultipleKeys() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
            assertThat(multimap.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("put - null 键和值")
        void testPutNullKeyAndValue() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            multimap.put(null, 1);
            multimap.put("a", null);
            multimap.put(null, null);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get(null)).containsExactlyInAnyOrder(1, null);
            assertThat(multimap.get("a")).containsExactly((Integer) null);
        }

        @Test
        @DisplayName("putAll - 从 Map 添加")
        void testPutAllFromMap() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            Map<String, Integer> map = Map.of("a", 1, "b", 2);

            multimap.putAll(map);

            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("b", 2)).isTrue();
        }

        @Test
        @DisplayName("putAll - 从 Multimap 添加")
        void testPutAllFromMultimap() {
            HashSetMultimap<String, Integer> source = HashSetMultimap.create();
            source.put("a", 1);
            source.put("a", 2);

            HashSetMultimap<String, Integer> target = HashSetMultimap.create();
            target.putAll(source);

            assertThat(target.size()).isEqualTo(2);
            assertThat(target.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("putAll - 为键添加多个值")
        void testPutAllForKey() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            boolean result = multimap.putAll("a", List.of(1, 2, 3));

            assertThat(result).isTrue();
            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - 包含重复值时仅添加不重复的")
        void testPutAllWithDuplicates() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            boolean result = multimap.putAll("a", List.of(1, 2, 3));

            assertThat(result).isTrue();  // changed because 2 and 3 were added
            assertThat(multimap.size()).isEqualTo(3);  // 1, 2, 3
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - null 值不改变")
        void testPutAllNull() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> values = multimap.get("a");

            assertThat(values).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("get - 获取不存在的键")
        void testGetNonExistent() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            Collection<Integer> values = multimap.get("nonexistent");

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("getSet - 获取集合")
        void testGetSet() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Set<Integer> set = multimap.getSet("a");

            assertThat(set).containsExactlyInAnyOrder(1, 2);
            assertThat(set).isInstanceOf(Set.class);
        }

        @Test
        @DisplayName("getSet - 不存在的键返回空集合")
        void testGetSetNonExistent() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            Set<Integer> set = multimap.getSet("nonexistent");

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(HashSet.class);
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("containsKey - 存在的键")
        void testContainsKey() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 存在的值")
        void testContainsValue() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isTrue();
            assertThat(multimap.containsValue(3)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 存在的条目")
        void testContainsEntry() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            boolean result = multimap.remove("a", 2);

            assertThat(result).isFalse();
            assertThat(multimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("remove - 移除最后一个值时删除键")
        void testRemoveLastValue() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            multimap.remove("a", 1);

            assertThat(multimap.containsKey("a")).isFalse();
            assertThat(multimap.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("removeAll - 移除键的所有值")
        void testRemoveAll() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Collection<Integer> removed = multimap.removeAll("a");

            assertThat(removed).containsExactlyInAnyOrder(1, 2);
            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("removeAll - 移除不存在的键")
        void testRemoveAllNonExistent() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            Collection<Integer> removed = multimap.removeAll("nonexistent");

            assertThat(removed).isEmpty();
        }

        @Test
        @DisplayName("replaceValues - 替换键的所有值")
        void testReplaceValues() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", List.of(3, 4, 5));

            assertThat(old).containsExactlyInAnyOrder(1, 2);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(3, 4, 5);
            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("replaceValues - null 值清除键")
        void testReplaceValuesNull() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);

            Collection<Integer> old = multimap.replaceValues("a", null);

            assertThat(old).containsExactlyInAnyOrder(1, 2);
            assertThat(multimap.containsKey("a")).isFalse();
        }

        @Test
        @DisplayName("clear - 清空所有")
        void testClear() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("b", 2);
            multimap.put("a", 3);

            Set<String> keys = multimap.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("keys - 键多重集视图")
        void testKeys() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Collection<Integer> values = multimap.values();

            assertThat(values).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("entries - 条目视图")
        void testEntries() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("b", 3);

            Map<String, ? extends Collection<Integer>> map = multimap.asMap();

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).containsExactlyInAnyOrder(1, 2);
            assertThat(map.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("asMap - 不可修改")
        void testAsMapUnmodifiable() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            Map<String, Collection<Integer>> map = multimap.asMap();

            assertThatThrownBy(() -> map.put("b", Set.of(2)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("去重特性测试")
    class DeduplicationTests {

        @Test
        @DisplayName("自动去重")
        void testDeduplication() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            multimap.put("a", 1);
            multimap.put("a", 1);
            multimap.put("a", 2);
            multimap.put("a", 2);
            multimap.put("a", 3);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            HashSetMultimap<String, Integer> multimap1 = HashSetMultimap.create();
            multimap1.put("a", 1);
            multimap1.put("a", 2);

            HashSetMultimap<String, Integer> multimap2 = HashSetMultimap.create();
            multimap2.put("a", 2);
            multimap2.put("a", 1);

            assertThat(multimap1).isEqualTo(multimap2);  // Order doesn't matter for Set
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
            multimap.put("a", 1);

            assertThat(multimap.equals(multimap)).isTrue();
        }

        @Test
        @DisplayName("equals - 不相等")
        void testEqualsNotEqual() {
            HashSetMultimap<String, Integer> multimap1 = HashSetMultimap.create();
            multimap1.put("a", 1);

            HashSetMultimap<String, Integer> multimap2 = HashSetMultimap.create();
            multimap2.put("a", 2);

            assertThat(multimap1).isNotEqualTo(multimap2);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            HashSetMultimap<String, Integer> multimap1 = HashSetMultimap.create();
            multimap1.put("a", 1);

            HashSetMultimap<String, Integer> multimap2 = HashSetMultimap.create();
            multimap2.put("a", 1);

            assertThat(multimap1.hashCode()).isEqualTo(multimap2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();
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
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            assertThat(multimap.size()).isZero();

            multimap.put("a", 1);
            assertThat(multimap.size()).isEqualTo(1);

            multimap.put("a", 2);
            assertThat(multimap.size()).isEqualTo(2);

            multimap.put("b", 3);
            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size - 重复值不增加")
        void testSizeDuplicates() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            multimap.put("a", 1);
            assertThat(multimap.size()).isEqualTo(1);

            multimap.put("a", 1);  // duplicate
            assertThat(multimap.size()).isEqualTo(1);  // size unchanged
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            HashSetMultimap<String, Integer> multimap = HashSetMultimap.create();

            assertThat(multimap.isEmpty()).isTrue();

            multimap.put("a", 1);
            assertThat(multimap.isEmpty()).isFalse();

            multimap.remove("a", 1);
            assertThat(multimap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("与 ArrayListMultimap 比较")
    class ComparisonWithArrayListMultimapTests {

        @Test
        @DisplayName("跨实现相等性")
        void testCrossImplementationEquality() {
            ArrayListMultimap<String, Integer> listMultimap = ArrayListMultimap.create();
            listMultimap.put("a", 1);
            listMultimap.put("a", 2);

            HashSetMultimap<String, Integer> setMultimap = HashSetMultimap.create();
            setMultimap.put("a", 1);
            setMultimap.put("a", 2);

            // They should have the same key-value pairs (but different underlying collection types)
            assertThat(listMultimap.asMap().keySet()).isEqualTo(setMultimap.asMap().keySet());
            assertThat(listMultimap.get("a")).containsExactlyInAnyOrder(1, 2);
            assertThat(setMultimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }
    }
}
