package cloud.opencode.base.collections.primitive;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ObjectDoubleMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ObjectDoubleMap 测试")
class ObjectDoubleMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空映射")
        void testCreate() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("create - 指定初始容量")
        void testCreateWithCapacity() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create(100);

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("放置和获取操作测试")
    class PutGetTests {

        @Test
        @DisplayName("put/get - 基本放置和获取")
        void testPutGet() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            map.put("pi", 3.14);
            map.put("e", 2.71);

            assertThat(map.get("pi")).isEqualTo(3.14);
            assertThat(map.get("e")).isEqualTo(2.71);
        }

        @Test
        @DisplayName("put - 替换已有值")
        void testPutReplace() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("pi", 3.14);

            map.put("pi", 3.14159);

            assertThat(map.get("pi")).isEqualTo(3.14159);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("put - null 键抛出 NullPointerException")
        void testPutNullKey() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThatThrownBy(() -> map.put(null, 1.0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("get - 不存在的键抛出 NoSuchElementException")
        void testGetMissing() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThatThrownBy(() -> map.get("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("getOrDefault - 存在的键返回值")
        void testGetOrDefaultExists() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("pi", 3.14);

            assertThat(map.getOrDefault("pi", -1.0)).isEqualTo(3.14);
        }

        @Test
        @DisplayName("getOrDefault - 不存在的键返回默认值")
        void testGetOrDefaultMissing() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThat(map.getOrDefault("missing", -1.0)).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("put - 特殊 double 值")
        void testSpecialDoubleValues() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            map.put("zero", 0.0);
            map.put("negZero", -0.0);
            map.put("max", Double.MAX_VALUE);
            map.put("min", Double.MIN_VALUE);

            assertThat(map.get("zero")).isEqualTo(0.0);
            assertThat(map.get("negZero")).isEqualTo(-0.0);
            assertThat(map.get("max")).isEqualTo(Double.MAX_VALUE);
            assertThat(map.get("min")).isEqualTo(Double.MIN_VALUE);
        }
    }

    @Nested
    @DisplayName("删除操作测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemoveExisting() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("pi", 3.14);

            double removed = map.remove("pi");

            assertThat(removed).isEqualTo(3.14);
            assertThat(map.containsKey("pi")).isFalse();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("remove - 删除不存在的键抛出 NoSuchElementException")
        void testRemoveMissing() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThatThrownBy(() -> map.remove("missing"))
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("remove - 删除后其他元素仍可访问")
        void testRemoveDoesNotBreakOthers() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.1);
            map.put("b", 2.2);
            map.put("c", 3.3);

            map.remove("b");

            assertThat(map.get("a")).isEqualTo(1.1);
            assertThat(map.get("c")).isEqualTo(3.3);
            assertThat(map.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("containsKey - 包含和不包含")
        void testContainsKey() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("pi", 3.14);

            assertThat(map.containsKey("pi")).isTrue();
            assertThat(map.containsKey("e")).isFalse();
        }

        @Test
        @DisplayName("containsKey - null 键抛出 NullPointerException")
        void testContainsKeyNull() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThatThrownBy(() -> map.containsKey(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("size - 大小随操作变化")
        void testSize() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThat(map.size()).isZero();

            map.put("a", 1.0);
            assertThat(map.size()).isEqualTo(1);

            map.put("b", 2.0);
            assertThat(map.size()).isEqualTo(2);

            map.remove("a");
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("isEmpty - 空和非空")
        void testIsEmpty() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            assertThat(map.isEmpty()).isTrue();

            map.put("a", 1.0);
            assertThat(map.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("清空测试")
    class ClearTests {

        @Test
        @DisplayName("clear - 清空后为空")
        void testClear() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.0);
            map.put("b", 2.0);

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
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("score", 10.5);

            map.addTo("score", 5.5);

            assertThat(map.get("score")).isEqualTo(16.0);
        }

        @Test
        @DisplayName("addTo - 键不存在时设置为增量")
        void testAddToNew() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            map.addTo("score", 7.5);

            assertThat(map.get("score")).isEqualTo(7.5);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("addTo - 负增量")
        void testAddToNegative() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("score", 10.0);

            map.addTo("score", -3.5);

            assertThat(map.get("score")).isEqualTo(6.5);
        }
    }

    @Nested
    @DisplayName("遍历测试")
    class ForEachTests {

        @Test
        @DisplayName("forEach - 遍历所有条目")
        void testForEach() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.1);
            map.put("b", 2.2);
            map.put("c", 3.3);

            Map<String, Double> collected = new HashMap<>();
            map.forEach(collected::put);

            assertThat(collected).hasSize(3);
            assertThat(collected).containsEntry("a", 1.1);
            assertThat(collected).containsEntry("b", 2.2);
            assertThat(collected).containsEntry("c", 3.3);
        }

        @Test
        @DisplayName("forEach - 空映射不调用")
        void testForEachEmpty() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

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
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.0);
            map.put("b", 2.0);
            map.put("c", 3.0);

            Set<String> keys = map.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("keySet - 返回不可修改集合")
        void testKeySetUnmodifiable() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.0);

            Set<String> keys = map.keySet();

            assertThatThrownBy(() -> keys.add("b"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("toValueArray - 返回正确的值数组")
        void testToValueArray() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("a", 1.1);
            map.put("b", 2.2);
            map.put("c", 3.3);

            double[] values = map.toValueArray();

            assertThat(values).hasSize(3);
            assertThat(values).containsExactlyInAnyOrder(1.1, 2.2, 3.3);
        }

        @Test
        @DisplayName("toValueArray - 空映射返回空数组")
        void testToValueArrayEmpty() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            double[] values = map.toValueArray();

            assertThat(values).isEmpty();
        }
    }

    @Nested
    @DisplayName("扩容测试")
    class ResizeTests {

        @Test
        @DisplayName("大量元素触发扩容")
        void testResize() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();

            for (int i = 0; i < 1000; i++) {
                map.put("key-" + i, i * 0.1);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get("key-" + i)).isEqualTo(i * 0.1);
            }
        }

        @Test
        @DisplayName("小初始容量也能正确扩容")
        void testResizeFromSmallCapacity() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create(2);

            for (int i = 0; i < 100; i++) {
                map.put("k" + i, i * 1.5);
            }

            assertThat(map.size()).isEqualTo(100);
            for (int i = 0; i < 100; i++) {
                assertThat(map.get("k" + i)).isEqualTo(i * 1.5);
            }
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等的映射")
        void testEquals() {
            ObjectDoubleMap<String> map1 = ObjectDoubleMap.create();
            map1.put("a", 1.1);
            map1.put("b", 2.2);

            ObjectDoubleMap<String> map2 = ObjectDoubleMap.create();
            map2.put("a", 1.1);
            map2.put("b", 2.2);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不相等的映射")
        void testNotEquals() {
            ObjectDoubleMap<String> map1 = ObjectDoubleMap.create();
            map1.put("a", 1.1);

            ObjectDoubleMap<String> map2 = ObjectDoubleMap.create();
            map2.put("a", 2.2);

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相等映射有相同 hashCode")
        void testHashCode() {
            ObjectDoubleMap<String> map1 = ObjectDoubleMap.create();
            map1.put("a", 1.1);

            ObjectDoubleMap<String> map2 = ObjectDoubleMap.create();
            map2.put("a", 1.1);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 包含键值对")
        void testToString() {
            ObjectDoubleMap<String> map = ObjectDoubleMap.create();
            map.put("pi", 3.14);

            String str = map.toString();

            assertThat(str).contains("pi");
            assertThat(str).contains("3.14");
        }
    }
}
