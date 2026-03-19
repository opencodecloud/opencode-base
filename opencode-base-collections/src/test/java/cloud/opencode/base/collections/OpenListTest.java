package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenList 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenList 测试")
class OpenListTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newArrayList - 创建空 ArrayList")
        void testNewArrayList() {
            ArrayList<String> list = OpenList.newArrayList();

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("of - 从元素创建 ArrayList")
        void testOf() {
            ArrayList<String> list = OpenList.of("a", "b", "c");

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("of - 空元素")
        void testOfEmpty() {
            ArrayList<String> list = OpenList.of();

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("from - 从 Iterable 创建")
        void testFrom() {
            Set<String> set = new LinkedHashSet<>(List.of("a", "b", "c"));
            ArrayList<String> list = OpenList.from(set);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("from - 从 Collection 创建")
        void testFromCollection() {
            List<String> source = List.of("a", "b", "c");
            ArrayList<String> list = OpenList.from(source);

            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("withCapacity - 指定容量创建")
        void testWithCapacity() {
            ArrayList<String> list = OpenList.withCapacity(100);

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newLinkedList - 创建空 LinkedList")
        void testNewLinkedList() {
            LinkedList<String> list = OpenList.newLinkedList();

            assertThat(list).isEmpty();
            assertThat(list).isInstanceOf(LinkedList.class);
        }

        @Test
        @DisplayName("linkedListFrom - 从 Iterable 创建 LinkedList")
        void testLinkedListFrom() {
            LinkedList<String> list = OpenList.linkedListFrom(List.of("a", "b", "c"));

            assertThat(list).containsExactly("a", "b", "c");
        }
    }

    @Nested
    @DisplayName("视图操作测试")
    class ViewOperationTests {

        @Test
        @DisplayName("reverse - 反转列表")
        void testReverse() {
            List<String> list = List.of("a", "b", "c");

            List<String> reversed = OpenList.reverse(new ArrayList<>(list));

            assertThat(reversed).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("reverse - null 列表")
        void testReverseNull() {
            List<String> result = OpenList.reverse(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("reverse - 空列表")
        void testReverseEmpty() {
            List<String> list = new ArrayList<>();

            List<String> reversed = OpenList.reverse(list);

            assertThat(reversed).isEmpty();
        }

        @Test
        @DisplayName("partition - 分区列表")
        void testPartition() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            List<List<Integer>> partitions = OpenList.partition(list, 2);

            assertThat(partitions).hasSize(3);
            assertThat(partitions.get(0)).containsExactly(1, 2);
            assertThat(partitions.get(1)).containsExactly(3, 4);
            assertThat(partitions.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partition - null 列表")
        void testPartitionNull() {
            List<List<Integer>> result = OpenList.partition(null, 2);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partition - 空列表")
        void testPartitionEmpty() {
            List<List<Integer>> result = OpenList.partition(List.of(), 2);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partition - 大小为负数抛异常")
        void testPartitionNegativeSize() {
            assertThatThrownBy(() -> OpenList.partition(List.of(1, 2, 3), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partition - 大小为零抛异常")
        void testPartitionZeroSize() {
            assertThatThrownBy(() -> OpenList.partition(List.of(1, 2, 3), 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("transform - 转换列表")
        void testTransform() {
            List<String> list = List.of("a", "bb", "ccc");

            List<Integer> transformed = OpenList.transform(list, String::length);

            assertThat(transformed).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform - null 列表")
        void testTransformNull() {
            List<Integer> result = OpenList.transform(null, String::length);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transform - 空列表")
        void testTransformEmpty() {
            List<Integer> result = OpenList.transform(List.of(), String::length);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("charactersOf - 字符串转字符列表")
        void testCharactersOf() {
            List<Character> chars = OpenList.charactersOf("abc");

            assertThat(chars).containsExactly('a', 'b', 'c');
        }

        @Test
        @DisplayName("charactersOf - null 字符串")
        void testCharactersOfNull() {
            List<Character> result = OpenList.charactersOf(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("charactersOf - 空字符串")
        void testCharactersOfEmpty() {
            List<Character> result = OpenList.charactersOf("");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryOperationTests {

        @Test
        @DisplayName("getFirst - 获取第一个元素")
        void testGetFirst() {
            List<String> list = List.of("a", "b", "c");

            String first = OpenList.getFirst(list);

            assertThat(first).isEqualTo("a");
        }

        @Test
        @DisplayName("getFirst - null 列表")
        void testGetFirstNull() {
            String result = OpenList.getFirst(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getFirst - 空列表")
        void testGetFirstEmpty() {
            String result = OpenList.getFirst(List.of());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getFirst - 带默认值")
        void testGetFirstWithDefault() {
            String result = OpenList.getFirst(List.of(), "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("getLast - 获取最后一个元素")
        void testGetLast() {
            List<String> list = List.of("a", "b", "c");

            String last = OpenList.getLast(list);

            assertThat(last).isEqualTo("c");
        }

        @Test
        @DisplayName("getLast - null 列表")
        void testGetLastNull() {
            String result = OpenList.getLast(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getLast - 空列表")
        void testGetLastEmpty() {
            String result = OpenList.getLast(List.of());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getLast - 带默认值")
        void testGetLastWithDefault() {
            String result = OpenList.getLast(List.of(), "default");

            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("findFirst - 查找第一个匹配元素")
        void testFindFirst() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);

            Optional<Integer> result = OpenList.findFirst(list, n -> n > 3);

            assertThat(result).hasValue(4);
        }

        @Test
        @DisplayName("findFirst - 未找到")
        void testFindFirstNotFound() {
            List<Integer> list = List.of(1, 2, 3);

            Optional<Integer> result = OpenList.findFirst(list, n -> n > 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findFirst - null 列表")
        void testFindFirstNull() {
            Optional<Integer> result = OpenList.findFirst(null, n -> n > 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - 过滤列表")
        void testFilter() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

            List<Integer> result = OpenList.filter(list, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("filter - null 列表")
        void testFilterNull() {
            List<Integer> result = OpenList.filter(null, n -> n > 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - 无匹配")
        void testFilterNoMatch() {
            List<Integer> list = List.of(1, 3, 5);

            List<Integer> result = OpenList.filter(list, n -> n % 2 == 0);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("笛卡尔积测试")
    class CartesianProductTests {

        @Test
        @DisplayName("cartesianProduct - 基本笛卡尔积")
        void testCartesianProduct() {
            List<String> list1 = List.of("a", "b");
            List<Integer> list2 = List.of(1, 2);

            @SuppressWarnings("unchecked")
            List<List<Object>> result = OpenList.cartesianProduct(
                    (List<Object>) (List<?>) list1,
                    (List<Object>) (List<?>) list2);

            assertThat(result).hasSize(4);
        }

        @Test
        @DisplayName("cartesianProduct - 使用 List 参数")
        void testCartesianProductWithListParam() {
            List<List<Integer>> lists = List.of(
                    List.of(1, 2),
                    List.of(3, 4)
            );

            List<List<Integer>> result = OpenList.cartesianProduct(lists);

            assertThat(result).hasSize(4);
            assertThat(result).contains(List.of(1, 3), List.of(1, 4), List.of(2, 3), List.of(2, 4));
        }

        @Test
        @DisplayName("cartesianProduct - null 参数")
        void testCartesianProductNull() {
            List<List<Integer>> result = OpenList.cartesianProduct((List<List<Integer>>) null);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - 空参数")
        void testCartesianProductEmpty() {
            List<List<Integer>> result = OpenList.cartesianProduct(List.of());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }
    }
}
