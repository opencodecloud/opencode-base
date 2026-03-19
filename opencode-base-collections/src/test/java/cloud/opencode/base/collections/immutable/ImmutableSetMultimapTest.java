package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ArrayListMultimap;
import cloud.opencode.base.collections.HashSetMultimap;
import cloud.opencode.base.collections.ImmutableSet;
import cloud.opencode.base.collections.Multimap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableSetMultimap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableSetMultimap 测试")
class ImmutableSetMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 空多值映射")
        void testOfEmpty() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of();

            assertThat(multimap.isEmpty()).isTrue();
            assertThat(multimap.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("of - 单个条目")
        void testOfSingleEntry() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThat(multimap.size()).isEqualTo(1);
            assertThat(multimap.get("a")).containsExactly(1);
        }

        @Test
        @DisplayName("of - null 键抛异常")
        void testOfNullKey() {
            assertThatThrownBy(() -> ImmutableSetMultimap.of(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - null 值抛异常")
        void testOfNullValue() {
            assertThatThrownBy(() -> ImmutableSetMultimap.of("a", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of - 两个条目")
        void testOfTwoEntries() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "b", 2);

            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.get("a")).containsExactly(1);
            assertThat(multimap.get("b")).containsExactly(2);
        }

        @Test
        @DisplayName("of - 两个条目同键不同值")
        void testOfTwoEntriesSameKeyDifferentValues() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "a", 2);

            assertThat(multimap.size()).isEqualTo(2);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("copyOf - 从 Multimap 复制")
        void testCopyOf() {
            Multimap<String, Integer> source = HashSetMultimap.create();
            source.put("a", 1);
            source.put("a", 2);
            source.put("b", 3);

            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.copyOf(source);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("copyOf - 从自身复制返回同一实例")
        void testCopyOfSelf() {
            ImmutableSetMultimap<String, Integer> original = ImmutableSetMultimap.of("a", 1);

            ImmutableSetMultimap<String, Integer> copy = ImmutableSetMultimap.copyOf(original);

            assertThat(copy).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("put - 添加条目")
        void testBuilderPut() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 2)
                    .build();

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("put - null 键抛异常")
        void testBuilderPutNullKey() {
            ImmutableSetMultimap.Builder<String, Integer> builder = ImmutableSetMultimap.builder();

            assertThatThrownBy(() -> builder.put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - null 值抛异常")
        void testBuilderPutNullValue() {
            ImmutableSetMultimap.Builder<String, Integer> builder = ImmutableSetMultimap.builder();

            assertThatThrownBy(() -> builder.put("a", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("put - 重复值被忽略")
        void testBuilderPutDuplicatesIgnored() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 1)
                    .put("a", 1)
                    .build();

            assertThat(multimap.get("a")).containsExactly(1);
            assertThat(multimap.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("putAll - Iterable 批量添加")
        void testBuilderPutAllIterable() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .putAll("a", List.of(1, 2, 3))
                    .build();

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - 可变参数批量添加")
        void testBuilderPutAllVarargs() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .putAll("a", 1, 2, 3)
                    .build();

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("putAll - 从 Multimap 批量添加")
        void testBuilderPutAllMultimap() {
            Multimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 1);
            source.put("b", 2);

            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .putAll(source)
                    .build();

            assertThat(multimap.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("build - 空构建器返回空多值映射")
        void testBuilderBuildEmpty() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder().build();

            assertThat(multimap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("get 方法测试")
    class GetMethodTests {

        @Test
        @DisplayName("get - 返回 ImmutableSet")
        void testGetReturnsImmutableSet() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "a", 2);

            ImmutableSet<Integer> values = multimap.get("a");

            assertThat(values).isInstanceOf(ImmutableSet.class);
            assertThat(values).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("get - 不存在的键返回空集合")
        void testGetNonExistent() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            ImmutableSet<Integer> values = multimap.get("b");

            assertThat(values).isEmpty();
        }

        @Test
        @DisplayName("get - 不包含重复")
        void testGetNoDuplicates() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 1)
                    .put("a", 2)
                    .build();

            assertThat(multimap.get("a")).containsExactlyInAnyOrder(1, 2);
        }
    }

    @Nested
    @DisplayName("inverse 方法测试")
    class InverseMethodTests {

        @Test
        @DisplayName("inverse - 逆映射")
        void testInverse() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "b", 2);

            ImmutableSetMultimap<Integer, String> inverse = multimap.inverse();

            assertThat(inverse.get(1)).containsExactly("a");
            assertThat(inverse.get(2)).containsExactly("b");
        }

        @Test
        @DisplayName("inverse - 多对一变一对多")
        void testInverseManyToOne() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("b", 1)
                    .build();

            ImmutableSetMultimap<Integer, String> inverse = multimap.inverse();

            assertThat(inverse.get(1)).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("inverse - 空多值映射")
        void testInverseEmpty() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of();

            ImmutableSetMultimap<Integer, String> inverse = multimap.inverse();

            assertThat(inverse.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("asMap 方法测试")
    class AsMapMethodTests {

        @Test
        @DisplayName("asMap - 返回映射视图")
        void testAsMap() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 2)
                    .put("b", 3)
                    .build();

            Map<String, ? extends Collection<Integer>> map = multimap.asMap();

            assertThat(map).hasSize(2);
            assertThat(map.get("a")).containsExactlyInAnyOrder(1, 2);
            assertThat(map.get("b")).containsExactly(3);
        }

        @Test
        @DisplayName("asMap - 不可修改")
        void testAsMapUnmodifiable() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            Map<String, ? extends Collection<Integer>> map = multimap.asMap();

            assertThatThrownBy(map::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Multimap 方法测试")
    class MultimapMethodTests {

        @Test
        @DisplayName("size - 总条目数（去重后）")
        void testSize() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 2)
                    .put("a", 1)  // duplicate, ignored
                    .put("b", 3)
                    .build();

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ImmutableSetMultimap<String, Integer> empty = ImmutableSetMultimap.of();
            ImmutableSetMultimap<String, Integer> nonEmpty = ImmutableSetMultimap.of("a", 1);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "a", 2);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(3)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - 包含条目")
        void testContainsEntry() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isFalse();
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "b", 2);

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("values - 所有值")
        void testValues() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1).put("a", 2).put("b", 3).build();

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("entries - 所有条目")
        void testEntries() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1, "b", 2);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(2);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("put - 抛出异常")
        void testPutThrows() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.put("b", 2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("remove - 抛出异常")
        void testRemoveThrows() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.remove("a", 1))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("clear - 抛出异常")
        void testClearThrows() {
            ImmutableSetMultimap<String, Integer> multimap = ImmutableSetMultimap.of("a", 1);

            assertThatThrownBy(multimap::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("与 ListMultimap 对比测试")
    class ComparisonWithListMultimapTests {

        @Test
        @DisplayName("SetMultimap 去重，ListMultimap 保留重复")
        void testDeduplicationDifference() {
            ImmutableSetMultimap<String, Integer> setMultimap = ImmutableSetMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 1)
                    .put("a", 1)
                    .build();

            ImmutableListMultimap<String, Integer> listMultimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 1)
                    .put("a", 1)
                    .build();

            assertThat(setMultimap.size()).isEqualTo(1);
            assertThat(listMultimap.size()).isEqualTo(3);
        }
    }
}
