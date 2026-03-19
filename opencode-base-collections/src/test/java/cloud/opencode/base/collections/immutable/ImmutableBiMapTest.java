package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableBiMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableBiMap 测试")
class ImmutableBiMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空 BiMap")
        void testOfEmpty() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of();

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("of - 创建包含一个条目的 BiMap")
        void testOfOne() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap).hasSize(1);
            assertThat(bimap.get("one")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 创建包含两个条目的 BiMap")
        void testOfTwo() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            assertThat(bimap).hasSize(2);
            assertThat(bimap.get("one")).isEqualTo(1);
            assertThat(bimap.get("two")).isEqualTo(2);
        }

        @Test
        @DisplayName("copyOf - 从 Map 复制")
        void testCopyOf() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2);
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.copyOf(source);

            assertThat(bimap).hasSize(2);
            assertThat(bimap.get("a")).isEqualTo(1);
            assertThat(bimap.get("b")).isEqualTo(2);
        }

        @Test
        @DisplayName("copyOf - null Map")
        void testCopyOfNull() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.copyOf(null);

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 空 Map")
        void testCopyOfEmpty() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.copyOf(Map.of());

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("copyOf - 返回相同实例")
        void testCopyOfSameInstance() {
            ImmutableBiMap<String, Integer> original = ImmutableBiMap.of("one", 1);
            ImmutableBiMap<String, Integer> copy = ImmutableBiMap.copyOf(original);

            assertThat(copy).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 构建 BiMap")
        void testBuilder() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.<String, Integer>builder()
                    .put("one", 1)
                    .put("two", 2)
                    .put("three", 3)
                    .build();

            assertThat(bimap).hasSize(3);
            assertThat(bimap.get("one")).isEqualTo(1);
            assertThat(bimap.get("two")).isEqualTo(2);
            assertThat(bimap.get("three")).isEqualTo(3);
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.<String, Integer>builder().build();

            assertThat(bimap).isEmpty();
        }

        @Test
        @DisplayName("builder - putAll")
        void testBuilderPutAll() {
            Map<String, Integer> source = new LinkedHashMap<>();
            source.put("a", 1);
            source.put("b", 2);

            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.<String, Integer>builder()
                    .putAll(source)
                    .build();

            assertThat(bimap).hasSize(2);
        }

        @Test
        @DisplayName("builder - 重复键抛异常")
        void testBuilderDuplicateKey() {
            assertThatThrownBy(() -> ImmutableBiMap.<String, Integer>builder()
                    .put("one", 1)
                    .put("one", 2)
                    .build())
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("builder - 重复值抛异常")
        void testBuilderDuplicateValue() {
            assertThatThrownBy(() -> ImmutableBiMap.<String, Integer>builder()
                    .put("one", 1)
                    .put("two", 1)
                    .build())
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("builder - null 键抛异常")
        void testBuilderNullKey() {
            assertThatThrownBy(() -> ImmutableBiMap.<String, Integer>builder()
                    .put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - null 值抛异常")
        void testBuilderNullValue() {
            assertThatThrownBy(() -> ImmutableBiMap.<String, Integer>builder()
                    .put("one", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("BiMap 特有方法测试")
    class BiMapMethodTests {

        @Test
        @DisplayName("inverse - 反向映射")
        void testInverse() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            ImmutableBiMap<Integer, String> inverse = bimap.inverse();

            assertThat(inverse.get(1)).isEqualTo("one");
            assertThat(inverse.get(2)).isEqualTo("two");
        }

        @Test
        @DisplayName("inverse - 缓存")
        void testInverseCached() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            ImmutableBiMap<Integer, String> inverse1 = bimap.inverse();
            ImmutableBiMap<Integer, String> inverse2 = bimap.inverse();

            assertThat(inverse1).isSameAs(inverse2);
        }

        @Test
        @DisplayName("inverse 的 inverse 是原映射")
        void testInverseOfInverse() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            ImmutableBiMap<String, Integer> result = bimap.inverse().inverse();

            assertThat(result).isSameAs(bimap);
        }

        @Test
        @DisplayName("getKey - 通过值获取键")
        void testGetKey() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            assertThat(bimap.getKey(1)).isEqualTo("one");
            assertThat(bimap.getKey(2)).isEqualTo("two");
            assertThat(bimap.getKey(3)).isNull();
        }
    }

    @Nested
    @DisplayName("Map 实现测试")
    class MapImplementationTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            assertThat(bimap.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            assertThat(ImmutableBiMap.of().isEmpty()).isTrue();
            assertThat(ImmutableBiMap.of("one", 1).isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap.containsKey("one")).isTrue();
            assertThat(bimap.containsKey("two")).isFalse();
            assertThat(bimap.containsKey(null)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap.containsValue(1)).isTrue();
            assertThat(bimap.containsValue(2)).isFalse();
            assertThat(bimap.containsValue(null)).isFalse();
        }

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap.get("one")).isEqualTo(1);
            assertThat(bimap.get("two")).isNull();
        }

        @Test
        @DisplayName("keySet - 键集")
        void testKeySet() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            assertThat(bimap.keySet()).containsExactlyInAnyOrder("one", "two");
        }

        @Test
        @DisplayName("values - 值集合")
        void testValues() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1, "two", 2);

            assertThat(bimap.values()).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap.entrySet()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("不可变保护测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("put - 抛异常")
        void testPut() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThatThrownBy(() -> bimap.put("two", 2))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("remove - 抛异常")
        void testRemove() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThatThrownBy(() -> bimap.remove("one"))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("putAll - 抛异常")
        void testPutAll() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThatThrownBy(() -> bimap.putAll(Map.of("two", 2)))
                    .isInstanceOf(OpenCollectionException.class);
        }

        @Test
        @DisplayName("clear - 抛异常")
        void testClear() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThatThrownBy(bimap::clear)
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            ImmutableBiMap<String, Integer> bimap1 = ImmutableBiMap.of("one", 1);
            ImmutableBiMap<String, Integer> bimap2 = ImmutableBiMap.of("one", 1);
            Map<String, Integer> map = Map.of("one", 1);

            assertThat(bimap1).isEqualTo(bimap2);
            assertThat(bimap1).isEqualTo(map);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            ImmutableBiMap<String, Integer> bimap1 = ImmutableBiMap.of("one", 1);
            ImmutableBiMap<String, Integer> bimap2 = ImmutableBiMap.of("one", 1);

            assertThat(bimap1.hashCode()).isEqualTo(bimap2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.of("one", 1);

            assertThat(bimap.toString()).contains("one").contains("1");
        }
    }
}
