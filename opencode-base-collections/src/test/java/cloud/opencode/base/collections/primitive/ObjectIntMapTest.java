package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ObjectIntMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ObjectIntMap 测试")
class ObjectIntMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            ObjectIntMap<String> map = ObjectIntMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置和获取操作测试")
    class PutGetTests {

        @Test
        @DisplayName("put/get - 基本放置和获取")
        void testPutGet() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            map.put("apple", 3);
            map.put("banana", 5);

            assertThat(map.get("apple")).isEqualTo(3);
            assertThat(map.get("banana")).isEqualTo(5);
        }

        @Test
        @DisplayName("put - 替换已有值")
        void testPutReplace() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("apple", 3);

            map.put("apple", 10);

            assertThat(map.get("apple")).isEqualTo(10);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("put - null 键抛出 NullPointerException")
        void testPutNullKey() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThatThrownBy(() -> map.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("get - 不存在的键抛出 NoSuchElementException")
        void testGetMissing() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThatThrownBy(() -> map.get("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOrDefault - 存在的键返回值")
        void testGetOrDefaultExists() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("apple", 3);

            assertThat(map.getOrDefault("apple", -1)).isEqualTo(3);
        }

        @Test
        @DisplayName("getOrDefault - 不存在的键返回默认值")
        void testGetOrDefaultMissing() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThat(map.getOrDefault("missing", -1)).isEqualTo(-1);
        }

        @Test
        @DisplayName("put - 零值和负值")
        void testZeroAndNegativeValues() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            map.put("zero", 0);
            map.put("negative", -42);

            assertThat(map.get("zero")).isEqualTo(0);
            assertThat(map.get("negative")).isEqualTo(-42);
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemoveExisting() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("apple", 3);

            int removed = map.remove("apple");

            assertThat(removed).isEqualTo(3);
            assertThat(map.containsKey("apple")).isFalse();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("remove - 删除不存在的键抛出 NoSuchElementException")
        void testRemoveMissing() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThatThrownBy(() -> map.remove("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("remove - 删除后其他元素仍可访问")
        void testRemoveDoesNotBreakOthers() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            map.remove("b");

            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("c")).isEqualTo(3);
            assertThat(map.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("containsKey - 包含和不包含")
        void testContainsKey() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("apple", 3);

            assertThat(map.containsKey("apple")).isTrue();
            assertThat(map.containsKey("banana")).isFalse();
        }

        @Test
        @DisplayName("containsKey - null 键抛出 NullPointerException")
        void testContainsKeyNull() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThatThrownBy(() -> map.containsKey(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("size - 大小随操作变化")
        void testSize() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThat(map.size()).isZero();

            map.put("a", 1);
            assertThat(map.size()).isEqualTo(1);

            map.put("b", 2);
            assertThat(map.size()).isEqualTo(2);

            map.remove("a");
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("isEmpty - 空和非空")
        void testIsEmpty() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            assertThat(map.isEmpty()).isTrue();

            map.put("a", 1);
            assertThat(map.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("清空测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清空后为空")
        void testClear() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);
            map.put("b", 2);

            map.clear();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
            assertThat(map.containsKey("a")).isFalse();
        }
    }

    @Nested
    @DisplayName("addTo 测试")
    class AddToTests {

        @Test
        @DisplayName("addTo - 增加已有键的值")
        void testAddToExisting() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("count", 10);

            map.addTo("count", 5);

            assertThat(map.get("count")).isEqualTo(15);
        }

        @Test
        @DisplayName("addTo - 键不存在时设置为增量")
        void testAddToNew() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            map.addTo("count", 7);

            assertThat(map.get("count")).isEqualTo(7);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("addTo - 负增量")
        void testAddToNegative() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("count", 10);

            map.addTo("count", -3);

            assertThat(map.get("count")).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("遍历测试")
    class ForEachTests {

        @Test
        @DisplayName("forEach - 遍历所有条目")
        void testForEach() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Map<String, Integer> collected = new HashMap<>();
            map.forEach(collected::put);

            assertThat(collected).hasSize(3);
            assertThat(collected).containsEntry("a", 1);
            assertThat(collected).containsEntry("b", 2);
            assertThat(collected).containsEntry("c", 3);
        }

        @Test
        @DisplayName("forEach - 空映射不调用")
        void testForEachEmpty() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            List<String> visited = new ArrayList<>();
            map.forEach((k, v) -> visited.add(k));

            assertThat(visited).isEmpty();
        }
    }

    @Nested
    @DisplayName("视图操作测试")
    class ViewTests {

        @Test
        @DisplayName("keySet - 返回正确的键集合")
        void testKeySet() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            Set<String> keys = map.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("keySet - 返回不可修改集合")
        void testKeySetUnmodifiable() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);

            Set<String> keys = map.keySet();

            assertThatThrownBy(() -> keys.add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("toValueArray - 返回正确的值数组")
        void testToValueArray() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);
            map.put("b", 2);
            map.put("c", 3);

            int[] values = map.toValueArray();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("toValueArray - 空映射返回空数组")
        void testToValueArrayEmpty() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            int[] values = map.toValueArray();

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素触发扩容")
        void testResize() {
            ObjectIntMap<String> map = ObjectIntMap.create();

            for (int i = 0; i < 1000; i++) {
                map.put("key-" + i, i);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get("key-" + i)).isEqualTo(i);
            }
        }

        @Test
        @DisplayName("小初始容量也能正确扩容")
        void testResizeFromSmallCapacity() {
            ObjectIntMap<String> map = ObjectIntMap.create(2);

            for (int i = 0; i < 100; i++) {
                map.put("k" + i, i);
            }

            assertThat(map.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(map.get("k" + i)).isEqualTo(i);
            }
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等的映射")
        void testEquals() {
            ObjectIntMap<String> map1 = ObjectIntMap.create();
            map1.put("a", 1);
            map1.put("b", 2);

            ObjectIntMap<String> map2 = ObjectIntMap.create();
            map2.put("a", 1);
            map2.put("b", 2);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不相等的映射")
        void testNotEquals() {
            ObjectIntMap<String> map1 = ObjectIntMap.create();
            map1.put("a", 1);

            ObjectIntMap<String> map2 = ObjectIntMap.create();
            map2.put("a", 2);

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相等映射有相同 hashCode")
        void testHashCode() {
            ObjectIntMap<String> map1 = ObjectIntMap.create();
            map1.put("a", 1);

            ObjectIntMap<String> map2 = ObjectIntMap.create();
            map2.put("a", 1);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 包含键值对")
        void testToString() {
            ObjectIntMap<String> map = ObjectIntMap.create();
            map.put("a", 1);

            String str = map.toString();

            assertThat(str).contains("a");
            assertThat(str).contains("1");
        }
    }
}
