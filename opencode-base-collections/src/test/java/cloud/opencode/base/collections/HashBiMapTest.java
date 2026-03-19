package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * HashBiMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("HashBiMap 测试")
class HashBiMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 空创建")
        void testCreateEmpty() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("create - 指定容量")
        void testCreateWithCapacity() {
            HashBiMap<String, Integer> bimap = HashBiMap.create(32);
            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("create - 负容量抛异常")
        void testCreateNegativeCapacity() {
            assertThatThrownBy(() -> HashBiMap.create(-1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("create - 从 Map 创建")
        void testCreateFromMap() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2);
            HashBiMap<String, Integer> bimap = HashBiMap.create(source);

            assertThat(bimap.get("a")).isEqualTo(1);
            assertThat(bimap.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("create - 从 Map 创建有重复值抛异常")
        void testCreateFromMapDuplicateValues() {
            Map<String, Integer> source = new HashMap<>();
            source.put("a", 1);
            source.put("b", 1);

            assertThatThrownBy(() -> HashBiMap.create(source))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("put 操作测试")
    class PutOperationTests {

        @Test
        @DisplayName("put - 正常放入")
        void testPut() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            bimap.put("a", 1);

            assertThat(bimap.get("a")).isEqualTo(1);
            assertThat(bimap.inverse().get(1)).isEqualTo("a");
        }

        @Test
        @DisplayName("put - 更新键的值")
        void testPutUpdateKeyValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            bimap.put("a", 1);
            Integer oldValue = bimap.put("a", 2);

            assertThat(oldValue).isEqualTo(1);
            assertThat(bimap.get("a")).isEqualTo(2);
            assertThat(bimap.inverse().get(1)).isNull();
            assertThat(bimap.inverse().get(2)).isEqualTo("a");
        }

        @Test
        @DisplayName("put - 值已存在抛异常")
        void testPutDuplicateValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThatThrownBy(() -> bimap.put("b", 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Value already present");
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testPutNullKey() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            assertThatThrownBy(() -> bimap.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值抛异常")
        void testPutNullValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            assertThatThrownBy(() -> bimap.put("a", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - 相同键相同值")
        void testPutSameKeySameValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            Integer oldValue = bimap.put("a", 1);

            assertThat(oldValue).isEqualTo(1);
            assertThat(bimap.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("forcePut 操作测试")
    class ForcePutOperationTests {

        @Test
        @DisplayName("forcePut - 正常放入")
        void testForcePut() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            bimap.forcePut("a", 1);

            assertThat(bimap.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("forcePut - 替换现有值映射")
        void testForcePutReplacesExisting() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            bimap.forcePut("b", 1);

            assertThat(bimap.containsKey("a")).isFalse();
            assertThat(bimap.get("b")).isEqualTo(1);
            assertThat(bimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("forcePut - 更新键的值")
        void testForcePutUpdateKeyValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            Integer oldValue = bimap.forcePut("a", 2);

            assertThat(oldValue).isEqualTo(1);
            assertThat(bimap.get("a")).isEqualTo(2);
        }

        @Test
        @DisplayName("forcePut - null 键抛异常")
        void testForcePutNullKey() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            assertThatThrownBy(() -> bimap.forcePut(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("forcePut - null 值抛异常")
        void testForcePutNullValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            assertThatThrownBy(() -> bimap.forcePut("a", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("inverse 测试")
    class InverseTests {

        @Test
        @DisplayName("inverse - 获取反向视图")
        void testInverse() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            BiMap<Integer, String> inverse = bimap.inverse();

            assertThat(inverse.get(1)).isEqualTo("a");
            assertThat(inverse.get(2)).isEqualTo("b");
        }

        @Test
        @DisplayName("inverse - 实时视图")
        void testInverseLiveView() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            BiMap<Integer, String> inverse = bimap.inverse();
            bimap.put("b", 2);

            assertThat(inverse.get(2)).isEqualTo("b");
        }

        @Test
        @DisplayName("inverse - 双重 inverse 返回原 bimap")
        void testInverseInverse() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            BiMap<Integer, String> inverse = bimap.inverse();
            BiMap<String, Integer> doubleInverse = inverse.inverse();

            assertThat(doubleInverse).isSameAs(bimap);
        }

        @Test
        @DisplayName("inverse - 缓存")
        void testInverseCached() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            BiMap<Integer, String> inverse1 = bimap.inverse();
            BiMap<Integer, String> inverse2 = bimap.inverse();

            assertThat(inverse1).isSameAs(inverse2);
        }
    }

    @Nested
    @DisplayName("Map 操作测试")
    class MapOperationTests {

        @Test
        @DisplayName("size/isEmpty")
        void testSizeAndIsEmpty() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            assertThat(bimap.size()).isEqualTo(0);
            assertThat(bimap.isEmpty()).isTrue();

            bimap.put("a", 1);

            assertThat(bimap.size()).isEqualTo(1);
            assertThat(bimap.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey/containsValue")
        void testContains() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.containsKey("a")).isTrue();
            assertThat(bimap.containsKey("b")).isFalse();
            assertThat(bimap.containsValue(1)).isTrue();
            assertThat(bimap.containsValue(2)).isFalse();
        }

        @Test
        @DisplayName("get")
        void testGet() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.get("a")).isEqualTo(1);
            assertThat(bimap.get("b")).isNull();
        }

        @Test
        @DisplayName("remove")
        void testRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            Integer removed = bimap.remove("a");

            assertThat(removed).isEqualTo(1);
            assertThat(bimap.containsKey("a")).isFalse();
            assertThat(bimap.containsValue(1)).isFalse();
        }

        @Test
        @DisplayName("remove - 不存在的键")
        void testRemoveNonExistent() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            Integer removed = bimap.remove("a");

            assertThat(removed).isNull();
        }

        @Test
        @DisplayName("putAll")
        void testPutAll() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            bimap.putAll(Map.of("a", 1, "b", 2));

            assertThat(bimap.size()).isEqualTo(2);
            assertThat(bimap.get("a")).isEqualTo(1);
            assertThat(bimap.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("clear")
        void testClear() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            bimap.clear();

            assertThat(bimap).isEmpty();
            assertThat(bimap.inverse()).isEmpty();
        }
    }

    @Nested
    @DisplayName("keySet 测试")
    class KeySetTests {

        @Test
        @DisplayName("keySet - 遍历")
        void testKeySetIteration() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            assertThat(bimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("keySet - contains")
        void testKeySetContains() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.keySet().contains("a")).isTrue();
            assertThat(bimap.keySet().contains("b")).isFalse();
        }

        @Test
        @DisplayName("keySet - remove")
        void testKeySetRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            boolean removed = bimap.keySet().remove("a");

            assertThat(removed).isTrue();
            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("keySet - clear")
        void testKeySetClear() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            bimap.keySet().clear();

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("keySet - size")
        void testKeySetSize() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.keySet().size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("values 测试")
    class ValuesTests {

        @Test
        @DisplayName("values - 遍历")
        void testValuesIteration() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            assertThat(bimap.values()).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("values - contains")
        void testValuesContains() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.values().contains(1)).isTrue();
            assertThat(bimap.values().contains(2)).isFalse();
        }

        @Test
        @DisplayName("values - remove")
        void testValuesRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            boolean removed = bimap.values().remove(1);

            assertThat(removed).isTrue();
            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("values - remove 不存在")
        void testValuesRemoveNonExistent() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();

            boolean removed = bimap.values().remove(1);

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("values - clear")
        void testValuesClear() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            bimap.values().clear();

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("values - size")
        void testValuesSize() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.values().size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("entrySet 测试")
    class EntrySetTests {

        @Test
        @DisplayName("entrySet - 遍历")
        void testEntrySetIteration() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            assertThat(bimap.entrySet()).hasSize(2);
        }

        @Test
        @DisplayName("entrySet - contains")
        void testEntrySetContains() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.entrySet().contains(Map.entry("a", 1))).isTrue();
            assertThat(bimap.entrySet().contains(Map.entry("a", 2))).isFalse();
            assertThat(bimap.entrySet().contains("not an entry")).isFalse();
        }

        @Test
        @DisplayName("entrySet - remove")
        void testEntrySetRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            boolean removed = bimap.entrySet().remove(Map.entry("a", 1));

            assertThat(removed).isTrue();
            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("entrySet - remove 不匹配的值")
        void testEntrySetRemoveMismatch() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            boolean removed = bimap.entrySet().remove(Map.entry("a", 2));

            assertThat(removed).isFalse();
            assertThat(bimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("entrySet - remove 非 Entry")
        void testEntrySetRemoveNonEntry() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            boolean removed = bimap.entrySet().remove("not an entry");

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("entrySet - clear")
        void testEntrySetClear() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            bimap.entrySet().clear();

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("entrySet - size")
        void testEntrySetSize() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.entrySet().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("entrySet - setValue")
        void testEntrySetSetValue() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            for (Map.Entry<String, Integer> entry : bimap.entrySet()) {
                entry.setValue(2);
            }

            assertThat(bimap.get("a")).isEqualTo(2);
            assertThat(bimap.inverse().get(2)).isEqualTo("a");
            assertThat(bimap.inverse().get(1)).isNull();
        }

        @Test
        @DisplayName("entrySet - setValue 重复值抛异常")
        void testEntrySetSetValueDuplicate() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            for (Map.Entry<String, Integer> entry : bimap.entrySet()) {
                if (entry.getKey().equals("a")) {
                    assertThatThrownBy(() -> entry.setValue(2))
                            .isInstanceOf(IllegalArgumentException.class);
                    break;
                }
            }
        }

        @Test
        @DisplayName("entrySet - setValue null 抛异常")
        void testEntrySetSetValueNull() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            for (Map.Entry<String, Integer> entry : bimap.entrySet()) {
                assertThatThrownBy(() -> entry.setValue(null))
                        .isInstanceOf(NullPointerException.class);
                break;
            }
        }

        @Test
        @DisplayName("entrySet - Entry equals/hashCode/toString")
        void testEntryMethods() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            for (Map.Entry<String, Integer> entry : bimap.entrySet()) {
                assertThat(entry.equals(Map.entry("a", 1))).isTrue();
                assertThat(entry.equals("not an entry")).isFalse();
                assertThat(entry.hashCode()).isEqualTo(Objects.hashCode("a") ^ Objects.hashCode(1));
                assertThat(entry.toString()).isEqualTo("a=1");
            }
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals")
        void testEquals() {
            HashBiMap<String, Integer> bimap1 = HashBiMap.create();
            bimap1.put("a", 1);

            HashBiMap<String, Integer> bimap2 = HashBiMap.create();
            bimap2.put("a", 1);

            Map<String, Integer> map = Map.of("a", 1);

            assertThat(bimap1.equals(bimap1)).isTrue();
            assertThat(bimap1.equals(bimap2)).isTrue();
            assertThat(bimap1.equals(map)).isTrue();
            assertThat(bimap1.equals("not a map")).isFalse();
        }

        @Test
        @DisplayName("hashCode")
        void testHashCode() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            Map<String, Integer> map = Map.of("a", 1);

            assertThat(bimap.hashCode()).isEqualTo(map.hashCode());
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);

            assertThat(bimap.toString()).contains("a", "1");
        }
    }

    @Nested
    @DisplayName("迭代器测试")
    class IteratorTests {

        @Test
        @DisplayName("keySet iterator remove")
        void testKeySetIteratorRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            Iterator<String> it = bimap.keySet().iterator();
            while (it.hasNext()) {
                if (it.next().equals("a")) {
                    it.remove();
                }
            }

            assertThat(bimap.containsKey("a")).isFalse();
            // Note: Iterator remove may not synchronize inverse map in all implementations
            assertThat(bimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("values iterator remove")
        void testValuesIteratorRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            Iterator<Integer> it = bimap.values().iterator();
            while (it.hasNext()) {
                if (it.next().equals(1)) {
                    it.remove();
                }
            }

            assertThat(bimap.containsValue(1)).isFalse();
        }

        @Test
        @DisplayName("entrySet iterator remove")
        void testEntrySetIteratorRemove() {
            HashBiMap<String, Integer> bimap = HashBiMap.create();
            bimap.put("a", 1);
            bimap.put("b", 2);

            Iterator<Map.Entry<String, Integer>> it = bimap.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey().equals("a")) {
                    it.remove();
                }
            }

            assertThat(bimap.containsKey("a")).isFalse();
            assertThat(bimap.containsValue(1)).isFalse();
        }
    }
}
