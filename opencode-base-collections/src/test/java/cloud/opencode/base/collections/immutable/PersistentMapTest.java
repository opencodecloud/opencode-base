package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * PersistentMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("PersistentMap 测试")
class PersistentMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty - 创建空映射")
        void testEmpty() {
            PersistentMap<String, Integer> map = PersistentMap.empty();

            assertThat(map.isEmpty()).isTrue();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("of - 创建包含一个条目的映射")
        void testOfOne() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);

            assertThat(map.size()).isEqualTo(1);
            assertThat(map.get("a")).hasValue(1);
        }

        @Test
        @DisplayName("of - 创建包含两个条目的映射")
        void testOfTwo() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);

            assertThat(map.size()).isEqualTo(2);
            assertThat(map.get("a")).hasValue(1);
            assertThat(map.get("b")).hasValue(2);
        }
    }

    @Nested
    @DisplayName("put + get 测试")
    class PutGetTests {

        @Test
        @DisplayName("put - 单个键值对")
        void testPutSingle() {
            PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty().put("key", 42);

            assertThat(map.get("key")).hasValue(42);
            assertThat(map.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("put - 多个键值对")
        void testPutMultiple() {
            PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
                    .put("a", 1)
                    .put("b", 2)
                    .put("c", 3);

            assertThat(map.size()).isEqualTo(3);
            assertThat(map.get("a")).hasValue(1);
            assertThat(map.get("b")).hasValue(2);
            assertThat(map.get("c")).hasValue(3);
        }

        @Test
        @DisplayName("put - 覆盖已有键")
        void testPutOverwrite() {
            PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
                    .put("key", 1)
                    .put("key", 2);

            assertThat(map.size()).isEqualTo(1);
            assertThat(map.get("key")).hasValue(2);
        }

        @Test
        @DisplayName("put - 覆盖相同值不改变大小")
        void testPutSameValue() {
            PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
                    .put("key", 1);
            PersistentMap<String, Integer> map2 = map.put("key", 1);

            assertThat(map2.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("get - 不存在的键返回 empty")
        void testGetNonExisting() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);

            assertThat(map.get("z")).isEmpty();
        }

        @Test
        @DisplayName("put - null key 抛出异常")
        void testPutNullKey() {
            PersistentMap<String, Integer> map = PersistentMap.empty();

            assertThatThrownBy(() -> map.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null value 抛出异常")
        void testPutNullValue() {
            PersistentMap<String, Integer> map = PersistentMap.empty();

            assertThatThrownBy(() -> map.put("key", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - 大量数据")
        void testPutMany() {
            PersistentMap<Integer, String> map = PersistentMap.empty();
            for (int i = 0; i < 1000; i++) {
                map = map.put(i, "val-" + i);
            }

            assertThat(map.size()).isEqualTo(1000);
            for (int i = 0; i < 1000; i++) {
                assertThat(map.get(i)).hasValue("val-" + i);
            }
        }
    }

    @Nested
    @DisplayName("remove 测试")
    class RemoveTests {

        @Test
        @DisplayName("remove - 删除存在的键")
        void testRemoveExisting() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            PersistentMap<String, Integer> removed = map.remove("a");

            assertThat(removed.size()).isEqualTo(1);
            assertThat(removed.get("a")).isEmpty();
            assertThat(removed.get("b")).hasValue(2);
        }

        @Test
        @DisplayName("remove - 删除不存在的键")
        void testRemoveNonExisting() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);
            PersistentMap<String, Integer> removed = map.remove("z");

            assertThat(removed).isSameAs(map);
            assertThat(removed.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("remove - 删除最后一个键得到空映射")
        void testRemoveLastKey() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);
            PersistentMap<String, Integer> removed = map.remove("a");

            assertThat(removed.isEmpty()).isTrue();
            assertThat(removed.size()).isZero();
        }

        @Test
        @DisplayName("remove - 原映射不变")
        void testRemoveOriginalUnchanged() {
            PersistentMap<String, Integer> original = PersistentMap.of("a", 1, "b", 2);
            original.remove("a");

            assertThat(original.size()).isEqualTo(2);
            assertThat(original.get("a")).hasValue(1);
        }

        @Test
        @DisplayName("remove - 大量删除")
        void testRemoveMany() {
            PersistentMap<Integer, String> map = PersistentMap.empty();
            for (int i = 0; i < 100; i++) {
                map = map.put(i, "val-" + i);
            }
            for (int i = 0; i < 100; i++) {
                map = map.remove(i);
            }

            assertThat(map.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey - 存在的键")
        void testContainsKeyExisting() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);

            assertThat(map.containsKey("a")).isTrue();
        }

        @Test
        @DisplayName("containsKey - 不存在的键")
        void testContainsKeyNonExisting() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);

            assertThat(map.containsKey("z")).isFalse();
        }

        @Test
        @DisplayName("containsKey - 空映射")
        void testContainsKeyEmpty() {
            PersistentMap<String, Integer> map = PersistentMap.empty();

            assertThat(map.containsKey("a")).isFalse();
        }
    }

    @Nested
    @DisplayName("size / isEmpty 测试")
    class SizeIsEmptyTests {

        @Test
        @DisplayName("size - 正确跟踪大小")
        void testSize() {
            PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
                    .put("a", 1)
                    .put("b", 2)
                    .put("c", 3);

            assertThat(map.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空映射")
        void testIsEmpty() {
            assertThat(PersistentMap.empty().isEmpty()).isTrue();
            assertThat(PersistentMap.of("a", 1).isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionTests {

        @Test
        @DisplayName("toMap - 转为 JDK Map")
        void testToMap() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            Map<String, Integer> jdkMap = map.toMap();

            assertThat(jdkMap).containsEntry("a", 1).containsEntry("b", 2);
            assertThat(jdkMap).hasSize(2);
            assertThatThrownBy(() -> jdkMap.put("c", 3))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            Set<String> keys = map.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            Collection<Integer> values = map.values();

            assertThat(values).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("entrySet - 条目集合")
        void testEntrySet() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            Set<Map.Entry<String, Integer>> entries = map.entrySet();

            assertThat(entries).hasSize(2);
            Map<String, Integer> collected = entries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            assertThat(collected).containsEntry("a", 1).containsEntry("b", 2);
        }

        @Test
        @DisplayName("stream - 流操作")
        void testStream() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1, "b", 2);
            int sum = map.stream().mapToInt(Map.Entry::getValue).sum();

            assertThat(sum).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("持久性验证测试")
    class PersistenceTests {

        @Test
        @DisplayName("put 后原映射不变")
        void testPutPersistence() {
            PersistentMap<String, Integer> map1 = PersistentMap.of("a", 1);
            PersistentMap<String, Integer> map2 = map1.put("b", 2);

            assertThat(map1.size()).isEqualTo(1);
            assertThat(map1.get("b")).isEmpty();
            assertThat(map2.size()).isEqualTo(2);
            assertThat(map2.get("b")).hasValue(2);
        }

        @Test
        @DisplayName("remove 后原映射不变")
        void testRemovePersistence() {
            PersistentMap<String, Integer> map1 = PersistentMap.of("a", 1, "b", 2);
            PersistentMap<String, Integer> map2 = map1.remove("a");

            assertThat(map1.size()).isEqualTo(2);
            assertThat(map1.get("a")).hasValue(1);
            assertThat(map2.size()).isEqualTo(1);
            assertThat(map2.get("a")).isEmpty();
        }

        @Test
        @DisplayName("多版本共存")
        void testMultipleVersions() {
            PersistentMap<String, Integer> v1 = PersistentMap.of("a", 1);
            PersistentMap<String, Integer> v2 = v1.put("b", 2);
            PersistentMap<String, Integer> v3 = v2.put("c", 3);
            PersistentMap<String, Integer> v4 = v3.remove("a");

            assertThat(v1.size()).isEqualTo(1);
            assertThat(v2.size()).isEqualTo(2);
            assertThat(v3.size()).isEqualTo(3);
            assertThat(v4.size()).isEqualTo(2);

            assertThat(v1.get("a")).hasValue(1);
            assertThat(v4.get("a")).isEmpty();
            assertThat(v4.get("b")).hasValue(2);
            assertThat(v4.get("c")).hasValue(3);
        }
    }

    @Nested
    @DisplayName("哈希冲突测试")
    class HashCollisionTests {

        /**
         * Key type with controlled hashCode for testing collisions.
         * 用于测试冲突的可控 hashCode 键类型。
         */
        private record CollidingKey(String name, int hash) {
            @Override
            public int hashCode() {
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof CollidingKey other && Objects.equals(name, other.name);
            }
        }

        @Test
        @DisplayName("hash 冲突 - put 和 get")
        void testCollisionPutGet() {
            CollidingKey k1 = new CollidingKey("key1", 42);
            CollidingKey k2 = new CollidingKey("key2", 42);

            PersistentMap<CollidingKey, String> map = PersistentMap.<CollidingKey, String>empty()
                    .put(k1, "value1")
                    .put(k2, "value2");

            assertThat(map.size()).isEqualTo(2);
            assertThat(map.get(k1)).hasValue("value1");
            assertThat(map.get(k2)).hasValue("value2");
        }

        @Test
        @DisplayName("hash 冲突 - 覆盖已有键")
        void testCollisionOverwrite() {
            CollidingKey k1 = new CollidingKey("key1", 42);
            CollidingKey k2 = new CollidingKey("key2", 42);

            PersistentMap<CollidingKey, String> map = PersistentMap.<CollidingKey, String>empty()
                    .put(k1, "v1")
                    .put(k2, "v2")
                    .put(k1, "v1-updated");

            assertThat(map.size()).isEqualTo(2);
            assertThat(map.get(k1)).hasValue("v1-updated");
            assertThat(map.get(k2)).hasValue("v2");
        }

        @Test
        @DisplayName("hash 冲突 - remove")
        void testCollisionRemove() {
            CollidingKey k1 = new CollidingKey("key1", 42);
            CollidingKey k2 = new CollidingKey("key2", 42);
            CollidingKey k3 = new CollidingKey("key3", 42);

            PersistentMap<CollidingKey, String> map = PersistentMap.<CollidingKey, String>empty()
                    .put(k1, "v1")
                    .put(k2, "v2")
                    .put(k3, "v3");

            // Remove from collision node with 3 entries
            PersistentMap<CollidingKey, String> removed = map.remove(k2);
            assertThat(removed.size()).isEqualTo(2);
            assertThat(removed.get(k1)).hasValue("v1");
            assertThat(removed.get(k2)).isEmpty();
            assertThat(removed.get(k3)).hasValue("v3");

            // Remove down to single entry (should collapse to LeafNode)
            PersistentMap<CollidingKey, String> removed2 = removed.remove(k1);
            assertThat(removed2.size()).isEqualTo(1);
            assertThat(removed2.get(k3)).hasValue("v3");
        }

        @Test
        @DisplayName("hash 冲突 - remove 不存在的键")
        void testCollisionRemoveNonExisting() {
            CollidingKey k1 = new CollidingKey("key1", 42);
            CollidingKey k2 = new CollidingKey("key2", 42);
            CollidingKey k3 = new CollidingKey("key3", 42);

            PersistentMap<CollidingKey, String> map = PersistentMap.<CollidingKey, String>empty()
                    .put(k1, "v1")
                    .put(k2, "v2");

            PersistentMap<CollidingKey, String> removed = map.remove(k3);
            assertThat(removed.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("hash 冲突 - containsKey")
        void testCollisionContainsKey() {
            CollidingKey k1 = new CollidingKey("key1", 42);
            CollidingKey k2 = new CollidingKey("key2", 42);
            CollidingKey k3 = new CollidingKey("key3", 42);

            PersistentMap<CollidingKey, String> map = PersistentMap.<CollidingKey, String>empty()
                    .put(k1, "v1")
                    .put(k2, "v2");

            assertThat(map.containsKey(k1)).isTrue();
            assertThat(map.containsKey(k2)).isTrue();
            assertThat(map.containsKey(k3)).isFalse();
        }
    }

    @Nested
    @DisplayName("equals / hashCode / toString 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals - 相同内容的映射相等")
        void testEquals() {
            PersistentMap<String, Integer> map1 = PersistentMap.of("a", 1, "b", 2);
            PersistentMap<String, Integer> map2 = PersistentMap.<String, Integer>empty()
                    .put("b", 2)
                    .put("a", 1);

            assertThat(map1).isEqualTo(map2);
        }

        @Test
        @DisplayName("equals - 不同内容的映射不相等")
        void testNotEquals() {
            PersistentMap<String, Integer> map1 = PersistentMap.of("a", 1);
            PersistentMap<String, Integer> map2 = PersistentMap.of("a", 2);

            assertThat(map1).isNotEqualTo(map2);
        }

        @Test
        @DisplayName("hashCode - 相同内容的映射有相同 hashCode")
        void testHashCode() {
            PersistentMap<String, Integer> map1 = PersistentMap.of("a", 1, "b", 2);
            PersistentMap<String, Integer> map2 = PersistentMap.<String, Integer>empty()
                    .put("b", 2)
                    .put("a", 1);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 包含内容")
        void testToString() {
            PersistentMap<String, Integer> map = PersistentMap.of("a", 1);

            assertThat(map.toString()).contains("PersistentMap");
            assertThat(map.toString()).contains("a");
            assertThat(map.toString()).contains("1");
        }
    }
}
