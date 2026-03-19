package cloud.opencode.base.collections.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectorUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("CollectorUtil 测试")
class CollectorUtilTest {

    @Nested
    @DisplayName("List 收集器测试")
    class ListCollectorTests {

        @Test
        @DisplayName("toArrayList - 收集到 ArrayList")
        void testToArrayList() {
            ArrayList<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toArrayList());

            assertThat(result).isInstanceOf(ArrayList.class);
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toArrayList - 带容量")
        void testToArrayListWithCapacity() {
            ArrayList<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toArrayList(10));

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toLinkedList - 收集到 LinkedList")
        void testToLinkedList() {
            LinkedList<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toLinkedList());

            assertThat(result).isInstanceOf(LinkedList.class);
            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toUnmodifiableList - 收集到不可修改列表")
        void testToUnmodifiableList() {
            List<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toUnmodifiableList());

            assertThat(result).containsExactly("a", "b", "c");
            assertThatThrownBy(() -> result.add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Set 收集器测试")
    class SetCollectorTests {

        @Test
        @DisplayName("toHashSet - 收集到 HashSet")
        void testToHashSet() {
            HashSet<String> result = Stream.of("a", "b", "a")
                    .collect(CollectorUtil.toHashSet());

            assertThat(result).isInstanceOf(HashSet.class);
            assertThat(result).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("toLinkedHashSet - 收集到 LinkedHashSet")
        void testToLinkedHashSet() {
            LinkedHashSet<String> result = Stream.of("c", "a", "b")
                    .collect(CollectorUtil.toLinkedHashSet());

            assertThat(result).isInstanceOf(LinkedHashSet.class);
            assertThat(new ArrayList<>(result)).containsExactly("c", "a", "b");
        }

        @Test
        @DisplayName("toTreeSet - 自然排序")
        void testToTreeSet() {
            TreeSet<String> result = Stream.of("c", "a", "b")
                    .collect(CollectorUtil.toTreeSet());

            assertThat(result).isInstanceOf(TreeSet.class);
            assertThat(new ArrayList<>(result)).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("toTreeSet - 自定义比较器")
        void testToTreeSetWithComparator() {
            TreeSet<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toTreeSet(Comparator.reverseOrder()));

            assertThat(new ArrayList<>(result)).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("toUnmodifiableSet - 收集到不可修改集合")
        void testToUnmodifiableSet() {
            Set<String> result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.toUnmodifiableSet());

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
            assertThatThrownBy(() -> result.add("d"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Map 收集器测试")
    class MapCollectorTests {

        @Test
        @DisplayName("toLinkedHashMap - 收集到 LinkedHashMap")
        void testToLinkedHashMap() {
            LinkedHashMap<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(CollectorUtil.toLinkedHashMap(String::length, s -> s));

            assertThat(result).isInstanceOf(LinkedHashMap.class);
            assertThat(new ArrayList<>(result.keySet())).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toTreeMap - 自然排序")
        void testToTreeMap() {
            TreeMap<Integer, String> result = Stream.of("ccc", "a", "bb")
                    .collect(CollectorUtil.toTreeMap(String::length, s -> s));

            assertThat(result).isInstanceOf(TreeMap.class);
            assertThat(new ArrayList<>(result.keySet())).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toTreeMap - 自定义比较器")
        void testToTreeMapWithComparator() {
            TreeMap<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(CollectorUtil.toTreeMap(String::length, s -> s, Comparator.reverseOrder()));

            assertThat(new ArrayList<>(result.keySet())).containsExactly(3, 2, 1);
        }

        @Test
        @DisplayName("toUnmodifiableMap - 收集到不可修改映射")
        void testToUnmodifiableMap() {
            Map<Integer, String> result = Stream.of("a", "bb", "ccc")
                    .collect(CollectorUtil.toUnmodifiableMap(String::length, s -> s));

            assertThat(result.get(1)).isEqualTo("a");
            assertThatThrownBy(() -> result.put(4, "dddd"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("特殊收集器测试")
    class SpecialCollectorTests {

        @Test
        @DisplayName("counting - 计数")
        void testCounting() {
            Long count = Stream.of("a", "b", "c", "d", "e")
                    .collect(CollectorUtil.counting());

            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("counting - 空流")
        void testCountingEmpty() {
            Long count = Stream.<String>of()
                    .collect(CollectorUtil.counting());

            assertThat(count).isZero();
        }

        @Test
        @DisplayName("joining - 连接字符串")
        void testJoining() {
            String result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.joining(", "));

            assertThat(result).isEqualTo("a, b, c");
        }

        @Test
        @DisplayName("joining - 带前缀和后缀")
        void testJoiningWithPrefixSuffix() {
            String result = Stream.of("a", "b", "c")
                    .collect(CollectorUtil.joining(", ", "[", "]"));

            assertThat(result).isEqualTo("[a, b, c]");
        }

        @Test
        @DisplayName("joining - 空流")
        void testJoiningEmpty() {
            String result = Stream.<String>of()
                    .collect(CollectorUtil.joining(", ", "[", "]"));

            assertThat(result).isEqualTo("[]");
        }
    }

    @Nested
    @DisplayName("自定义收集器测试")
    class CustomCollectorTests {

        @Test
        @DisplayName("of - 带完成器的自定义收集器")
        void testOfWithFinisher() {
            var collector = CollectorUtil.<String, ArrayList<String>, String>of(
                    ArrayList<String>::new,
                    ArrayList::add,
                    (left, right) -> { left.addAll(right); return left; },
                    list -> String.join("-", list)
            );

            String result = Stream.of("a", "b", "c").collect(collector);

            assertThat(result).isEqualTo("a-b-c");
        }

        @Test
        @DisplayName("of - 不带完成器的自定义收集器")
        void testOfWithoutFinisher() {
            var collector = CollectorUtil.<String, ArrayList<String>>of(
                    ArrayList::new,
                    ArrayList::add,
                    (left, right) -> { left.addAll(right); return left; }
            );

            ArrayList<String> result = Stream.of("a", "b", "c").collect(collector);

            assertThat(result).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("并行收集器测试")
    class ParallelCollectorTests {

        @Test
        @DisplayName("toArrayList - 并行流")
        void testToArrayListParallel() {
            ArrayList<Integer> result = Stream.iterate(1, n -> n + 1)
                    .limit(1000)
                    .parallel()
                    .collect(CollectorUtil.toArrayList());

            assertThat(result).hasSize(1000);
        }

        @Test
        @DisplayName("toHashSet - 并行流")
        void testToHashSetParallel() {
            HashSet<Integer> result = Stream.iterate(1, n -> n + 1)
                    .limit(1000)
                    .parallel()
                    .collect(CollectorUtil.toHashSet());

            assertThat(result).hasSize(1000);
        }
    }
}
