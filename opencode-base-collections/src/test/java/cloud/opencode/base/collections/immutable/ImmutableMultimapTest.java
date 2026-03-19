package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ArrayListMultimap;
import cloud.opencode.base.collections.Multimap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableMultimap 抽象类测试
 * 通过 ImmutableListMultimap 实现类测试 ImmutableMultimap 的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableMultimap 测试")
class ImmutableMultimapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("copyOf - 从 Multimap 复制")
        void testCopyOf() {
            Multimap<String, Integer> source = ArrayListMultimap.create();
            source.put("a", 1);
            source.put("a", 2);
            source.put("b", 3);

            ImmutableMultimap<String, Integer> multimap = ImmutableMultimap.copyOf(source);

            assertThat(multimap.size()).isEqualTo(3);
            assertThat(multimap.get("a")).containsExactly(1, 2);
        }

        @Test
        @DisplayName("copyOf - 空 Multimap")
        void testCopyOfEmpty() {
            Multimap<String, Integer> source = ArrayListMultimap.create();

            ImmutableMultimap<String, Integer> multimap = ImmutableMultimap.copyOf(source);

            assertThat(multimap.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Multimap 方法测试")
    class MultimapMethodTests {

        @Test
        @DisplayName("size - 总条目数")
        void testSize() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1).put("a", 2).put("b", 3).build();

            assertThat(multimap.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("size - 空多值映射")
        void testSizeEmpty() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of();

            assertThat(multimap.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            ImmutableMultimap<String, Integer> empty = ImmutableListMultimap.of();
            ImmutableMultimap<String, Integer> nonEmpty = ImmutableListMultimap.of("a", 1);

            assertThat(empty.isEmpty()).isTrue();
            assertThat(nonEmpty.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThat(multimap.containsKey("a")).isTrue();
            assertThat(multimap.containsKey("b")).isFalse();
        }

        @Test
        @DisplayName("containsKey - null 键抛异常")
        void testContainsKeyNull() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.containsKey(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("containsValue - 包含值")
        void testContainsValue() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1, "b", 2);

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(3)).isFalse();
        }

        @Test
        @DisplayName("containsValue - 多个值")
        void testContainsValueMultiple() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("a", 2)
                    .put("b", 3)
                    .build();

            assertThat(multimap.containsValue(1)).isTrue();
            assertThat(multimap.containsValue(2)).isTrue();
            assertThat(multimap.containsValue(3)).isTrue();
        }

        @Test
        @DisplayName("containsEntry - 包含条目")
        void testContainsEntry() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThat(multimap.containsEntry("a", 1)).isTrue();
            assertThat(multimap.containsEntry("a", 2)).isFalse();
            assertThat(multimap.containsEntry("b", 1)).isFalse();
        }

        @Test
        @DisplayName("containsEntry - null 键抛异常")
        void testContainsEntryNullKey() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.containsEntry(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("containsEntry - null 值返回 false")
        void testContainsEntryNullValue() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThat(multimap.containsEntry("a", null)).isFalse();
        }

        @Test
        @DisplayName("keySet - 键集合")
        void testKeySet() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1).put("b", 2).put("a", 3).build();

            assertThat(multimap.keySet()).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("keySet - 空多值映射")
        void testKeySetEmpty() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of();

            assertThat(multimap.keySet()).isEmpty();
        }

        @Test
        @DisplayName("values - 所有值")
        void testValues() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1).put("a", 2).put("b", 3).build();

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("values - 重复值")
        void testValuesDuplicates() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
                    .put("a", 1)
                    .put("b", 1)
                    .build();

            assertThat(multimap.values()).containsExactlyInAnyOrder(1, 1);
        }

        @Test
        @DisplayName("entries - 所有条目")
        void testEntries() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1, "b", 2);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();

            assertThat(entries).hasSize(2);
        }

        @Test
        @DisplayName("entries - 条目内容正确")
        void testEntriesContent() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            Collection<Map.Entry<String, Integer>> entries = multimap.entries();
            Map.Entry<String, Integer> entry = entries.iterator().next();

            assertThat(entry.getKey()).isEqualTo("a");
            assertThat(entry.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("keys - 抛出异常")
        void testKeysThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(multimap::keys)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("不可变性测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("put - 抛出异常")
        void testPutThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.put("b", 2))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("remove - 抛出异常")
        void testRemoveThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.remove("a", 1))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("putAll key - 抛出异常")
        void testPutAllKeyThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.putAll("b", List.of(2, 3)))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("putAll multimap - 抛出异常")
        void testPutAllMultimapThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);
            Multimap<String, Integer> other = ArrayListMultimap.create();
            other.put("b", 2);

            assertThatThrownBy(() -> multimap.putAll(other))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("replaceValues - 抛出异常")
        void testReplaceValuesThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.replaceValues("a", List.of(2, 3)))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("removeAll - 抛出异常")
        void testRemoveAllThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(() -> multimap.removeAll("a"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }

        @Test
        @DisplayName("clear - 抛出异常")
        void testClearThrows() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThatThrownBy(multimap::clear)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("immutable");
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("实现 Serializable")
        void testIsSerializable() {
            ImmutableMultimap<String, Integer> multimap = ImmutableListMultimap.of("a", 1);

            assertThat(multimap).isInstanceOf(java.io.Serializable.class);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("大量数据")
        void testLargeData() {
            ImmutableListMultimap.Builder<Integer, Integer> builder = ImmutableListMultimap.builder();
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 10; j++) {
                    builder.put(i, j);
                }
            }

            ImmutableMultimap<Integer, Integer> multimap = builder.build();

            assertThat(multimap.size()).isEqualTo(10000);
            assertThat(multimap.keySet()).hasSize(1000);
        }

        @Test
        @DisplayName("单键多值")
        void testSingleKeyManyValues() {
            ImmutableListMultimap.Builder<String, Integer> builder = ImmutableListMultimap.builder();
            for (int i = 0; i < 100; i++) {
                builder.put("key", i);
            }

            ImmutableMultimap<String, Integer> multimap = builder.build();

            assertThat(multimap.size()).isEqualTo(100);
            assertThat(multimap.get("key")).hasSize(100);
        }
    }
}
