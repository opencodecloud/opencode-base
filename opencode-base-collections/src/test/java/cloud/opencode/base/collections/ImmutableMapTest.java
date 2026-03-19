package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableMap 测试")
class ImmutableMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空映射")
        void testOfEmpty() {
            ImmutableMap<String, Integer> map = ImmutableMap.of();

            assertThat(map).isEmpty();
            assertThat(map.size()).isZero();
        }

        @Test
        @DisplayName("of - 单个条目")
        void testOfSingle() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThat(map).hasSize(1);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 两个条目")
        void testOfTwo() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("of - 三个条目")
        void testOfThree() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2, "c", 3);

            assertThat(map).hasSize(3);
        }

        @Test
        @DisplayName("of - 四个条目")
        void testOfFour() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2, "c", 3, "d", 4);

            assertThat(map).hasSize(4);
        }

        @Test
        @DisplayName("of - 五个条目")
        void testOfFive() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);

            assertThat(map).hasSize(5);
        }

        @Test
        @DisplayName("of - null 键抛异常")
        void testOfNullKey() {
            assertThatThrownBy(() -> ImmutableMap.of(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - null 值抛异常")
        void testOfNullValue() {
            assertThatThrownBy(() -> ImmutableMap.of("a", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("copyOf - 从 Map 复制")
        void testCopyOfMap() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2);

            ImmutableMap<String, Integer> map = ImmutableMap.copyOf(source);

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("copyOf - 从 ImmutableMap 返回相同实例")
        void testCopyOfImmutableMap() {
            ImmutableMap<String, Integer> source = ImmutableMap.of("a", 1);

            ImmutableMap<String, Integer> copy = ImmutableMap.copyOf(source);

            assertThat(copy).isSameAs(source);
        }

        @Test
        @DisplayName("copyOf - null 或空映射返回空映射")
        void testCopyOfNullOrEmpty() {
            assertThat(ImmutableMap.copyOf((Map<String, Integer>) null)).isEmpty();
            assertThat(ImmutableMap.copyOf(Collections.emptyMap())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 添加单个条目")
        void testBuilderPutSingle() {
            ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder()
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("builder - 从 Entry 添加")
        void testBuilderPutEntry() {
            ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder()
                    .put(new AbstractMap.SimpleEntry<>("a", 1))
                    .build();

            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("builder - putAll")
        void testBuilderPutAll() {
            ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder()
                    .put("a", 1)
                    .putAll(Map.of("b", 2, "c", 3))
                    .build();

            assertThat(map).hasSize(3);
        }

        @Test
        @DisplayName("builder - 空构建返回空映射")
        void testBuilderEmpty() {
            ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder().build();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("builder - null 键抛异常")
        void testBuilderNullKey() {
            assertThatThrownBy(() -> ImmutableMap.<String, Integer>builder().put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - null 值抛异常")
        void testBuilderNullValue() {
            assertThatThrownBy(() -> ImmutableMap.<String, Integer>builder().put("a", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Map 操作测试")
    class MapOperationTests {

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isEqualTo(2);
            assertThat(map.get("c")).isNull();
        }

        @Test
        @DisplayName("size - 映射大小")
        void testSize() {
            assertThat(ImmutableMap.of().size()).isZero();
            assertThat(ImmutableMap.of("a", 1).size()).isEqualTo(1);
            assertThat(ImmutableMap.of("a", 1, "b", 2, "c", 3).size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            assertThat(ImmutableMap.of().isEmpty()).isTrue();
            assertThat(ImmutableMap.of("a", 1).isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 键存在检查")
        void testContainsKey() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThat(map.containsKey("a")).isTrue();
            assertThat(map.containsKey("b")).isFalse();
            assertThat(map.containsKey(null)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 值存在检查")
        void testContainsValue() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThat(map.containsValue(1)).isTrue();
            assertThat(map.containsValue(2)).isFalse();
            assertThat(map.containsValue(null)).isFalse();
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            Set<String> keys = map.keySet();

            assertThat(keys).containsExactlyInAnyOrder("a", "b");
            assertThat(keys).isInstanceOf(ImmutableSet.class);
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            Collection<Integer> values = map.values();

            assertThat(values).containsExactlyInAnyOrder(1, 2);
            assertThat(values).isInstanceOf(ImmutableList.class);
        }

        @Test
        @DisplayName("entrySet - 条目集合")
        void testEntrySet() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            Set<Map.Entry<String, Integer>> entries = map.entrySet();

            assertThat(entries).hasSize(2);
        }
    }

    @Nested
    @DisplayName("不可变保护测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("put - 抛异常")
        void testPut() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.put("b", 2))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove - 抛异常")
        void testRemove() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.remove("a"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove(key, value) - 抛异常")
        void testRemoveKeyValue() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.remove("a", 1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("putAll - 抛异常")
        void testPutAll() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.putAll(Map.of("b", 2)))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("clear - 抛异常")
        void testClear() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(map::clear)
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("putIfAbsent - 抛异常")
        void testPutIfAbsent() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.putIfAbsent("b", 2))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("replace - 抛异常")
        void testReplace() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.replace("a", 2))
                    .isInstanceOf(OpenCollectionException.class);

            assertThatThrownBy(() -> map.replace("a", 1, 2))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("computeIfAbsent - 抛异常")
        void testComputeIfAbsent() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.computeIfAbsent("b", k -> 2))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("computeIfPresent - 抛异常")
        void testComputeIfPresent() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.computeIfPresent("a", (k, v) -> v + 1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("compute - 抛异常")
        void testCompute() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.compute("a", (k, v) -> v + 1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("merge - 抛异常")
        void testMerge() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.merge("a", 2, Integer::sum))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("replaceAll - 抛异常")
        void testReplaceAll() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            assertThatThrownBy(() -> map.replaceAll((k, v) -> v + 1))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("entrySet iterator.remove - 抛异常")
        void testEntrySetIteratorRemove() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);
            Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
            iterator.next();

            assertThatThrownBy(iterator::remove)
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("entry.setValue - 抛异常")
        void testEntrySetValue() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);
            Map.Entry<String, Integer> entry = map.entrySet().iterator().next();

            assertThatThrownBy(() -> entry.setValue(2))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("Entry 测试")
    class EntryTests {

        @Test
        @DisplayName("entry - 基本属性")
        void testEntryBasicProperties() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);
            Map.Entry<String, Integer> entry = map.entrySet().iterator().next();

            assertThat(entry.getKey()).isEqualTo("a");
            assertThat(entry.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("entry - equals")
        void testEntryEquals() {
            ImmutableMap<String, Integer> map1 = ImmutableMap.of("a", 1);
            ImmutableMap<String, Integer> map2 = ImmutableMap.of("a", 1);

            Map.Entry<String, Integer> entry1 = map1.entrySet().iterator().next();
            Map.Entry<String, Integer> entry2 = map2.entrySet().iterator().next();

            assertThat(entry1).isEqualTo(entry2);
        }

        @Test
        @DisplayName("entry - hashCode")
        void testEntryHashCode() {
            ImmutableMap<String, Integer> map1 = ImmutableMap.of("a", 1);
            ImmutableMap<String, Integer> map2 = ImmutableMap.of("a", 1);

            Map.Entry<String, Integer> entry1 = map1.entrySet().iterator().next();
            Map.Entry<String, Integer> entry2 = map2.entrySet().iterator().next();

            assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
        }

        @Test
        @DisplayName("entry - toString")
        void testEntryToString() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);
            Map.Entry<String, Integer> entry = map.entrySet().iterator().next();

            assertThat(entry.toString()).isEqualTo("a=1");
        }

        @Test
        @DisplayName("entrySet - contains")
        void testEntrySetContains() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);

            assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", 1))).isTrue();
            assertThat(map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", 2))).isFalse();
            assertThat(map.entrySet().contains("not an entry")).isFalse();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            ImmutableMap<String, Integer> map1 = ImmutableMap.of("a", 1, "b", 2);
            ImmutableMap<String, Integer> map2 = ImmutableMap.of("a", 1, "b", 2);
            Map<String, Integer> map3 = Map.of("a", 1, "b", 2);

            assertThat(map1).isEqualTo(map2);
            assertThat(map1).isEqualTo(map3);
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            ImmutableMap<String, Integer> map1 = ImmutableMap.of("a", 1);
            ImmutableMap<String, Integer> map2 = ImmutableMap.of("a", 1);

            assertThat(map1.hashCode()).isEqualTo(map2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1);

            String str = map.toString();

            assertThat(str).contains("a");
            assertThat(str).contains("1");
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("forEach - 遍历")
        void testForEach() {
            ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);
            Map<String, Integer> result = new HashMap<>();

            map.forEach(result::put);

            assertThat(result).hasSize(2);
            assertThat(result.get("a")).isEqualTo(1);
            assertThat(result.get("b")).isEqualTo(2);
        }
    }
}
