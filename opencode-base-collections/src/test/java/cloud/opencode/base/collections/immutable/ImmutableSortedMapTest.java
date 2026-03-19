package cloud.opencode.base.collections.immutable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableSortedMap 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableSortedMap 测试")
class ImmutableSortedMapTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 创建空 SortedMap")
        void testOfEmpty() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("of - 创建包含一个条目的 SortedMap")
        void testOfOne() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1);

            assertThat(map).hasSize(1);
            assertThat(map.get("a")).isEqualTo(1);
        }

        @Test
        @DisplayName("of - 创建包含两个条目的 SortedMap")
        void testOfTwo() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1);

            assertThat(map).hasSize(2);
            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "c");
        }

        @Test
        @DisplayName("of - 创建包含三个条目的 SortedMap")
        void testOfThree() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1, "b", 2);

            assertThat(map).hasSize(3);
            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 从 Map 复制")
        void testCopyOf() {
            Map<String, Integer> source = new HashMap<>();
            source.put("c", 3);
            source.put("a", 1);
            source.put("b", 2);

            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.copyOf(source);

            assertThat(map).hasSize(3);
            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyOf - 使用比较器")
        void testCopyOfWithComparator() {
            Map<String, Integer> source = Map.of("a", 1, "b", 2, "c", 3);

            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.copyOf(source, Comparator.reverseOrder());

            assertThat(new ArrayList<>(map.keySet())).containsExactly("c", "b", "a");
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("naturalOrder - 自然顺序构建")
        void testNaturalOrder() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>naturalOrder()
                    .put("c", 3)
                    .put("a", 1)
                    .put("b", 2)
                    .build();

            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("orderedBy - 自定义顺序构建")
        void testOrderedBy() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>orderedBy(Comparator.reverseOrder())
                    .put("a", 1)
                    .put("b", 2)
                    .put("c", 3)
                    .build();

            assertThat(new ArrayList<>(map.keySet())).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>naturalOrder().build();

            assertThat(map).isEmpty();
        }

        @Test
        @DisplayName("builder - putAll")
        void testBuilderPutAll() {
            Map<String, Integer> source = Map.of("c", 3, "a", 1);

            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>naturalOrder()
                    .putAll(source)
                    .build();

            assertThat(new ArrayList<>(map.keySet())).containsExactly("a", "c");
        }

        @Test
        @DisplayName("builder - null 键抛异常")
        void testBuilderNullKey() {
            assertThatThrownBy(() -> ImmutableSortedMap.<String, Integer>naturalOrder()
                    .put(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder - null 值抛异常")
        void testBuilderNullValue() {
            assertThatThrownBy(() -> ImmutableSortedMap.<String, Integer>naturalOrder()
                    .put("a", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Map 方法测试")
    class MapMethodTests {

        @Test
        @DisplayName("size - 大小")
        void testSize() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1, "b", 2);

            assertThat(map.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            assertThat(ImmutableSortedMap.of().isEmpty()).isTrue();
            assertThat(ImmutableSortedMap.of("a", 1).isEmpty()).isFalse();
        }

        @Test
        @DisplayName("containsKey - 包含键")
        void testContainsKey() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1);

            assertThat(map.containsKey("a")).isTrue();
            assertThat(map.containsKey("b")).isFalse();
            assertThat(map.containsKey(null)).isFalse();
        }

        @Test
        @DisplayName("get - 获取值")
        void testGet() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1);

            assertThat(map.get("a")).isEqualTo(1);
            assertThat(map.get("b")).isNull();
            assertThat(map.get(null)).isNull();
        }

        @Test
        @DisplayName("entrySet - 条目集")
        void testEntrySet() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1, "b", 2);

            assertThat(map.entrySet()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("NavigableMap 方法测试")
    class NavigableMapMethodTests {

        @Test
        @DisplayName("comparator - 获取比较器")
        void testComparator() {
            ImmutableSortedMap<String, Integer> natural = ImmutableSortedMap.of("a", 1);
            ImmutableSortedMap<String, Integer> custom = ImmutableSortedMap.copyOf(Map.of("a", 1), Comparator.reverseOrder());

            assertThat(natural.comparator()).isNull();
            assertThat(custom.comparator()).isNotNull();
        }

        @Test
        @DisplayName("firstKey - 第一个键")
        void testFirstKey() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1, "b", 2);

            assertThat(map.firstKey()).isEqualTo("a");
        }

        @Test
        @DisplayName("firstKey - 空映射抛异常")
        void testFirstKeyEmpty() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of();

            assertThatThrownBy(map::firstKey)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("lastKey - 最后一个键")
        void testLastKey() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1, "b", 2);

            assertThat(map.lastKey()).isEqualTo("c");
        }

        @Test
        @DisplayName("lowerKey - 小于给定键的最大键")
        void testLowerKey() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.of(1, "a", 3, "c", 5, "e");

            assertThat(map.lowerKey(3)).isEqualTo(1);
            assertThat(map.lowerKey(4)).isEqualTo(3);
            assertThat(map.lowerKey(1)).isNull();
        }

        @Test
        @DisplayName("floorKey - 小于等于给定键的最大键")
        void testFloorKey() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.of(1, "a", 3, "c", 5, "e");

            assertThat(map.floorKey(3)).isEqualTo(3);
            assertThat(map.floorKey(4)).isEqualTo(3);
            assertThat(map.floorKey(0)).isNull();
        }

        @Test
        @DisplayName("ceilingKey - 大于等于给定键的最小键")
        void testCeilingKey() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.of(1, "a", 3, "c", 5, "e");

            assertThat(map.ceilingKey(3)).isEqualTo(3);
            assertThat(map.ceilingKey(2)).isEqualTo(3);
            assertThat(map.ceilingKey(6)).isNull();
        }

        @Test
        @DisplayName("higherKey - 大于给定键的最小键")
        void testHigherKey() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.of(1, "a", 3, "c", 5, "e");

            assertThat(map.higherKey(3)).isEqualTo(5);
            assertThat(map.higherKey(2)).isEqualTo(3);
            assertThat(map.higherKey(5)).isNull();
        }

        @Test
        @DisplayName("firstEntry - 第一个条目")
        void testFirstEntry() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1);

            Map.Entry<String, Integer> entry = map.firstEntry();

            assertThat(entry.getKey()).isEqualTo("a");
            assertThat(entry.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("lastEntry - 最后一个条目")
        void testLastEntry() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1);

            Map.Entry<String, Integer> entry = map.lastEntry();

            assertThat(entry.getKey()).isEqualTo("c");
            assertThat(entry.getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("pollFirstEntry - 抛异常")
        void testPollFirstEntry() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1);

            assertThatThrownBy(map::pollFirstEntry)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("pollLastEntry - 抛异常")
        void testPollLastEntry() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("a", 1);

            assertThatThrownBy(map::pollLastEntry)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("navigableKeySet - 可导航键集")
        void testNavigableKeySet() {
            ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.of("c", 3, "a", 1, "b", 2);

            NavigableSet<String> keySet = map.navigableKeySet();

            assertThat(new ArrayList<>(keySet)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("subMap - 子映射")
        void testSubMap() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.<Integer, String>naturalOrder()
                    .put(1, "a").put(2, "b").put(3, "c").put(4, "d").put(5, "e")
                    .build();

            NavigableMap<Integer, String> subMap = map.subMap(2, true, 4, true);

            assertThat(subMap.keySet()).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("headMap - 头映射")
        void testHeadMap() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.<Integer, String>naturalOrder()
                    .put(1, "a").put(2, "b").put(3, "c").put(4, "d").put(5, "e")
                    .build();

            SortedMap<Integer, String> headMap = map.headMap(3);

            assertThat(headMap.keySet()).containsExactly(1, 2);
        }

        @Test
        @DisplayName("tailMap - 尾映射")
        void testTailMap() {
            ImmutableSortedMap<Integer, String> map = ImmutableSortedMap.<Integer, String>naturalOrder()
                    .put(1, "a").put(2, "b").put(3, "c").put(4, "d").put(5, "e")
                    .build();

            SortedMap<Integer, String> tailMap = map.tailMap(3);

            assertThat(tailMap.keySet()).containsExactly(3, 4, 5);
        }
    }
}
