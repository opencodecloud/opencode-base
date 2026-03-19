package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * FluentIterable 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("FluentIterable 测试")
class FluentIterableTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("from - 从 Iterable 创建")
        void testFrom() {
            List<String> list = List.of("a", "b", "c");

            FluentIterable<String> fluent = FluentIterable.from(list);

            assertThat(fluent.toList()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("from - FluentIterable 直接返回")
        void testFromFluentIterable() {
            FluentIterable<String> original = FluentIterable.of("a", "b", "c");

            FluentIterable<String> result = FluentIterable.from(original);

            assertThat(result).isSameAs(original);
        }

        @Test
        @DisplayName("of - 从可变参数创建")
        void testOf() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            assertThat(fluent.toList()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("empty - 创建空实例")
        void testEmpty() {
            FluentIterable<String> fluent = FluentIterable.empty();

            assertThat(fluent.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("concat - 连接多个 Iterable")
        void testConcat() {
            List<String> list1 = List.of("a", "b");
            List<String> list2 = List.of("c", "d");

            FluentIterable<String> fluent = FluentIterable.concat(list1, list2);

            assertThat(fluent.toList()).containsExactly("a", "b", "c", "d");
        }
    }

    @Nested
    @DisplayName("中间操作测试")
    class IntermediateOperationTests {

        @Test
        @DisplayName("filter - 过滤")
        void testFilter() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            List<Integer> result = fluent.filter(n -> n % 2 == 0).toList();

            assertThat(result).containsExactly(2, 4);
        }

        @Test
        @DisplayName("filter - 按类型过滤")
        void testFilterByType() {
            FluentIterable<Object> fluent = FluentIterable.of("a", 1, "b", 2, "c");

            List<String> result = fluent.filter(String.class).toList();

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("transform - 转换")
        void testTransform() {
            FluentIterable<String> fluent = FluentIterable.of("a", "bb", "ccc");

            List<Integer> result = fluent.transform(String::length).toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("flatMap - 平面映射")
        void testFlatMap() {
            FluentIterable<List<Integer>> fluent = FluentIterable.of(
                    List.of(1, 2),
                    List.of(3, 4),
                    List.of(5)
            );

            List<Integer> result = fluent.flatMap(list -> list).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("limit - 限制数量")
        void testLimit() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            List<Integer> result = fluent.limit(3).toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("limit - 负数抛异常")
        void testLimitNegative() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            assertThatThrownBy(() -> fluent.limit(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("skip - 跳过")
        void testSkip() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            List<Integer> result = fluent.skip(2).toList();

            assertThat(result).containsExactly(3, 4, 5);
        }

        @Test
        @DisplayName("skip - 负数抛异常")
        void testSkipNegative() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            assertThatThrownBy(() -> fluent.skip(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("append - 追加 Iterable")
        void testAppend() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            List<Integer> result = fluent.append(List.of(4, 5)).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("append - 追加可变参数")
        void testAppendVarargs() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            List<Integer> result = fluent.append(4, 5).toList();

            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("distinct - 去重")
        void testDistinct() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 2, 3, 3, 3);

            List<Integer> result = fluent.distinct().toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("cycle - 无限循环 (需要 limit)")
        void testCycle() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            List<Integer> result = fluent.cycle().limit(7).toList();

            assertThat(result).containsExactly(1, 2, 3, 1, 2, 3, 1);
        }
    }

    @Nested
    @DisplayName("终端操作测试")
    class TerminalOperationTests {

        @Test
        @DisplayName("first - 获取第一个元素")
        void testFirst() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            Optional<Integer> result = fluent.first();

            assertThat(result).contains(1);
        }

        @Test
        @DisplayName("first - 空集合")
        void testFirstEmpty() {
            FluentIterable<Integer> fluent = FluentIterable.empty();

            Optional<Integer> result = fluent.first();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("firstOr - 获取第一个或默认值")
        void testFirstOr() {
            FluentIterable<Integer> fluent = FluentIterable.empty();

            Integer result = fluent.firstOr(0);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("last - 获取最后一个元素")
        void testLast() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            Optional<Integer> result = fluent.last();

            assertThat(result).contains(3);
        }

        @Test
        @DisplayName("last - 空集合")
        void testLastEmpty() {
            FluentIterable<Integer> fluent = FluentIterable.empty();

            Optional<Integer> result = fluent.last();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("lastOr - 获取最后一个或默认值")
        void testLastOr() {
            FluentIterable<Integer> fluent = FluentIterable.empty();

            Integer result = fluent.lastOr(0);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("get - 获取指定索引元素")
        void testGet() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            assertThat(fluent.get(1)).contains("b");
            assertThat(fluent.get(5)).isEmpty();
            assertThat(fluent.get(-1)).isEmpty();
        }

        @Test
        @DisplayName("anyMatch - 任意匹配")
        void testAnyMatch() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            assertThat(fluent.anyMatch(n -> n > 3)).isTrue();
            assertThat(fluent.anyMatch(n -> n > 10)).isFalse();
        }

        @Test
        @DisplayName("allMatch - 全部匹配")
        void testAllMatch() {
            FluentIterable<Integer> fluent = FluentIterable.of(2, 4, 6, 8);

            assertThat(fluent.allMatch(n -> n % 2 == 0)).isTrue();
            assertThat(fluent.allMatch(n -> n > 5)).isFalse();
        }

        @Test
        @DisplayName("noneMatch - 无匹配")
        void testNoneMatch() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 3, 5, 7);

            assertThat(fluent.noneMatch(n -> n % 2 == 0)).isTrue();
            assertThat(fluent.noneMatch(n -> n > 5)).isFalse();
        }

        @Test
        @DisplayName("firstMatch - 找到第一个匹配")
        void testFirstMatch() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            assertThat(fluent.firstMatch(n -> n > 2)).contains(3);
            assertThat(fluent.firstMatch(n -> n > 10)).isEmpty();
        }

        @Test
        @DisplayName("contains - 包含检查")
        void testContains() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            assertThat(fluent.contains("b")).isTrue();
            assertThat(fluent.contains("d")).isFalse();
        }

        @Test
        @DisplayName("size - 计算大小")
        void testSize() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            assertThat(fluent.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("isEmpty - 空检查")
        void testIsEmpty() {
            assertThat(FluentIterable.empty().isEmpty()).isTrue();
            assertThat(FluentIterable.of(1).isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("收集操作测试")
    class CollectOperationTests {

        @Test
        @DisplayName("toList - 收集为列表")
        void testToList() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            List<Integer> result = fluent.toList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toImmutableList - 收集为不可变列表")
        void testToImmutableList() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);

            ImmutableList<Integer> result = fluent.toImmutableList();

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("toSet - 收集为集合")
        void testToSet() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 2, 3, 3);

            Set<Integer> result = fluent.toSet();

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("toImmutableSet - 收集为不可变集合")
        void testToImmutableSet() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 2, 3, 3);

            ImmutableSet<Integer> result = fluent.toImmutableSet();

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("toSortedList - 收集为排序列表")
        void testToSortedList() {
            FluentIterable<Integer> fluent = FluentIterable.of(3, 1, 4, 1, 5);

            ImmutableList<Integer> result = fluent.toSortedList(Comparator.naturalOrder());

            assertThat(result).containsExactly(1, 1, 3, 4, 5);
        }

        @Test
        @DisplayName("toMap - 收集为映射")
        void testToMap() {
            FluentIterable<String> fluent = FluentIterable.of("a", "bb", "ccc");

            Map<Integer, String> result = fluent.toMap(String::length, s -> s);

            assertThat(result).containsEntry(1, "a");
            assertThat(result).containsEntry(2, "bb");
            assertThat(result).containsEntry(3, "ccc");
        }

        @Test
        @DisplayName("uniqueIndex - 唯一键索引")
        void testUniqueIndex() {
            FluentIterable<String> fluent = FluentIterable.of("a", "bb", "ccc");

            Map<Integer, String> result = fluent.uniqueIndex(String::length);

            assertThat(result.get(1)).isEqualTo("a");
            assertThat(result.get(2)).isEqualTo("bb");
        }

        @Test
        @DisplayName("uniqueIndex - 重复键抛异常")
        void testUniqueIndexDuplicate() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            assertThatThrownBy(() -> fluent.uniqueIndex(String::length))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Duplicate key");
        }

        @Test
        @DisplayName("toArray - 收集为数组")
        void testToArray() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            String[] result = fluent.toArray(String.class);

            assertThat(result).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("copyInto - 复制到集合")
        void testCopyInto() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3);
            List<Integer> list = new ArrayList<>();

            fluent.copyInto(list);

            assertThat(list).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("join - 连接为字符串")
        void testJoin() {
            FluentIterable<String> fluent = FluentIterable.of("a", "b", "c");

            String result = fluent.join(", ");

            assertThat(result).isEqualTo("a, b, c");
        }
    }

    @Nested
    @DisplayName("链式操作测试")
    class ChainedOperationTests {

        @Test
        @DisplayName("filter + transform + limit")
        void testFilterTransformLimit() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<String> result = fluent
                    .filter(n -> n % 2 == 0)
                    .transform(n -> "Number: " + n)
                    .limit(3)
                    .toList();

            assertThat(result).containsExactly("Number: 2", "Number: 4", "Number: 6");
        }

        @Test
        @DisplayName("skip + limit 实现分页")
        void testPagination() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            List<Integer> page2 = fluent
                    .skip(3)
                    .limit(3)
                    .toList();

            assertThat(page2).containsExactly(4, 5, 6);
        }

        @Test
        @DisplayName("transform + distinct")
        void testTransformDistinct() {
            FluentIterable<String> fluent = FluentIterable.of("Hello", "HELLO", "hello", "World");

            List<String> result = fluent
                    .transform(String::toLowerCase)
                    .distinct()
                    .toList();

            assertThat(result).containsExactly("hello", "world");
        }
    }

    @Nested
    @DisplayName("Stream 兼容测试")
    class StreamCompatibilityTests {

        @Test
        @DisplayName("stream - 转为 Stream")
        void testStream() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            long count = fluent.stream().filter(n -> n > 2).count();

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("collect - 使用 Collector")
        void testCollect() {
            FluentIterable<Integer> fluent = FluentIterable.of(1, 2, 3, 4, 5);

            int sum = fluent.collect(java.util.stream.Collectors.summingInt(n -> n));

            assertThat(sum).isEqualTo(15);
        }
    }
}
