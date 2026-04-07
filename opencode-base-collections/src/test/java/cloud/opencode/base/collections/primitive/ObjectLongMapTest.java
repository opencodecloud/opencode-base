package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ObjectLongMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ObjectLongMap 测试")
class ObjectLongMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            ObjectLongMap<String> map = ObjectLongMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置和获取操作测试")
    class PutGetTests {

        @Test
        @DisplayName("put/get - 基本放置和获取")
        void testPutGet() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            map.put("apple", 3L);
            map.put("banana", 5L);

            assertThat(map.get("apple")).isEqualTo(3L);
            assertThat(map.get("banana")).isEqualTo(5L);
        }

        @Test
        @DisplayName("put - 替换已有值")
        void testPutReplace() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("apple", 3L);

            map.put("apple", 10L);

            assertThat(map.get("apple")).isEqualTo(10L);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("put - null 键抛出 NullPointerException")
        void testPutNullKey() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThatThrownBy(() -> map.put(null, 1L))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("get - 不存在的键抛出 NoSuchElementException")
        void testGetMissing() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThatThrownBy(() -> map.get("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOrDefault - 存在的键返回值")
        void testGetOrDefaultExists() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("apple", 3L);

            assertThat(map.getOrDefault("apple", -1L)).isEqualTo(3L);
        }

        @Test
        @DisplayName("getOrDefault - 不存在的键返回默认值")
        void testGetOrDefaultMissing() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThat(map.getOrDefault("missing", -1L)).isEqualTo(-1L);
        }

        @Test
        @DisplayName("put - 大 long 值")
        void testLargeValues() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            map.put("max", Long.MAX_VALUE);
            map.put("min", Long.MIN_VALUE);

            assertThat(map.get("max")).isEqualTo(Long.MAX_VALUE);
            assertThat(map.get("min")).isEqualTo(Long.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemoveExisting() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("apple", 3L);

            long removed = map.remove("apple");

            assertThat(removed).isEqualTo(3L);
            assertThat(map.containsKey("apple")).isFalse();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("remove - 删除不存在的键抛出 NoSuchElementException")
        void testRemoveMissing() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThatThrownBy(() -> map.remove("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("remove - 删除后其他元素仍可访问")
        void testRemoveDoesNotBreakOthers() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);
            map.put("b", 2L);
            map.put("c", 3L);

            map.remove("b");

            assertThat(map.get("a")).isEqualTo(1L);
            assertThat(map.get("c")).isEqualTo(3L);
            assertThat(map.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("containsKey - 包含和不包含")
        void testContainsKey() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("apple", 3L);

            assertThat(map.containsKey("apple")).isTrue();
            assertThat(map.containsKey("banana")).isFalse();
        }

        @Test
        @DisplayName("containsKey - null 键抛出 NullPointerException")
        void testContainsKeyNull() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThatThrownBy(() -> map.containsKey(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("size - 大小随操作变化")
        void testSize() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThat(map.size()).isZero();

            map.put("a", 1L);
            assertThat(map.size()).isEqualTo(1);

            map.put("b", 2L);
            assertThat(map.size()).isEqualTo(2);

            map.remove("a");
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("isEmpty - 空和非空")
        void testIsEmpty() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            assertThat(map.isEmpty()).isTrue();

            map.put("a", 1L);
            assertThat(map.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("清空测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清空后为空")
        void testClear() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);
            map.put("b", 2L);

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
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("count", 10L);

            map.addTo("count", 5L);

            assertThat(map.get("count")).isEqualTo(15L);
        }

        @Test
        @DisplayName("addTo - 键不存在时设置为增量")
        void testAddToNew() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            map.addTo("count", 7L);

            assertThat(map.get("count")).isEqualTo(7L);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("addTo - 负增量")
        void testAddToNegative() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("count", 10L);

            map.addTo("count", -3L);

            assertThat(map.get("count")).isEqualTo(7L);
        }
    }

    @Nested
    @DisplayName("遍历测试")
    class ForEachTests {

        @Test
        @DisplayName("forEach - 遍历所有条目")
        void testForEach() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);
            map.put("b", 2L);
            map.put("c", 3L);

            Map<String, Long> collected = new HashMap<>();
            map.forEach(collected::put);

            assertThat(collected).hasSize(3);
            assertThat(collected).containsEntry("a", 1L);
            assertThat(collected).containsEntry("b", 2L);
            assertThat(collected).containsEntry("c", 3L);
        }

        @Test
        @DisplayName("forEach - 空映射不调用")
        void testForEachEmpty() {
            ObjectLongMap<String> map = ObjectLongMap.create();

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
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);
            map.put("b", 2L);
            map.put("c", 3L);

            Set<String> keys = map.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("keySet - 返回不可修改集合")
        void testKeySetUnmodifiable() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);

            Set<String> keys = map.keySet();

            assertThatThrownBy(() -> keys.add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("toValueArray - 返回正确的值数组")
        void testToValueArray() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);
            map.put("b", 2L);
            map.put("c", 3L);

            long[] values = map.toValueArray();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("toValueArray - 空映射返回空数组")
        void testToValueArrayEmpty() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            long[] values = map.toValueArray();

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素触发扩容")
        void testResize() {
            ObjectLongMap<String> map = ObjectLongMap.create();

            for (int i = 0; i < 1000; i++) {
                map.put("key-" + i, (long) i);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get("key-" + i)).isEqualTo((long) i);
            }
        }

        @Test
        @DisplayName("小初始容量也能正确扩容")
        void testResizeFromSmallCapacity() {
            ObjectLongMap<String> map = ObjectLongMap.create(2);

            for (int i = 0; i < 100; i++) {
                map.put("k" + i, (long) i);
            }

            assertThat(map.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(map.get("k" + i)).isEqualTo((long) i);
            }
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等的映射")
        void testEquals() {
            ObjectLongMap<String> map1 = ObjectLongMap.create();
            map1.put("a", 1L);
            map1.put("b", 2L);

            ObjectLongMap<String> map2 = ObjectLongMap.create();
            map2.put("a", 1L);
            map2.put("b", 2L);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不相等的映射")
        void testNotEquals() {
            ObjectLongMap<String> map1 = ObjectLongMap.create();
            map1.put("a", 1L);

            ObjectLongMap<String> map2 = ObjectLongMap.create();
            map2.put("a", 2L);

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相等映射有相同 hashCode")
        void testHashCode() {
            ObjectLongMap<String> map1 = ObjectLongMap.create();
            map1.put("a", 1L);

            ObjectLongMap<String> map2 = ObjectLongMap.create();
            map2.put("a", 1L);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 包含键值对")
        void testToString() {
            ObjectLongMap<String> map = ObjectLongMap.create();
            map.put("a", 1L);

            String str = map.toString();

            assertThat(str).contains("a");
            assertThat(str).contains("1");
        }
    }
}
