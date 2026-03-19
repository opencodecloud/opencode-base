package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

/**
 * ListUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ListUtil 测试")
class ListUtilTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newArrayList - 空创建")
        void testNewArrayListEmpty() {
            ArrayList<String> list = ListUtil.newArrayList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayList - 带元素")
        void testNewArrayListWithElements() {
            ArrayList<String> list = ListUtil.newArrayList("a", "b", "c");
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newArrayList - null 元素数组")
        void testNewArrayListNullArray() {
            ArrayList<String> list = ListUtil.newArrayList((String[]) null);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayList - 空元素数组")
        void testNewArrayListEmptyArray() {
            ArrayList<String> list = ListUtil.newArrayList(new String[0]);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayList - 从 Iterable 创建")
        void testNewArrayListFromIterable() {
            List<String> source = List.of("a", "b", "c");
            ArrayList<String> list = ListUtil.newArrayList(source);
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("newArrayList - 从 null Iterable 创建")
        void testNewArrayListFromNullIterable() {
            ArrayList<String> list = ListUtil.newArrayList((Iterable<String>) null);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayList - 从非 Collection Iterable 创建")
        void testNewArrayListFromNonCollection() {
            Iterable<String> iterable = () -> List.of("a", "b").iterator();
            ArrayList<String> list = ListUtil.newArrayList(iterable);
            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("newArrayListWithCapacity - 指定容量")
        void testNewArrayListWithCapacity() {
            ArrayList<String> list = ListUtil.newArrayListWithCapacity(10);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayListWithCapacity - 负容量")
        void testNewArrayListWithNegativeCapacity() {
            assertThatThrownBy(() -> ListUtil.newArrayListWithCapacity(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("newArrayListWithExpectedSize - 预期大小")
        void testNewArrayListWithExpectedSize() {
            ArrayList<String> list = ListUtil.newArrayListWithExpectedSize(10);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newArrayListWithExpectedSize - 负大小")
        void testNewArrayListWithNegativeExpectedSize() {
            assertThatThrownBy(() -> ListUtil.newArrayListWithExpectedSize(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("newLinkedList - 空创建")
        void testNewLinkedListEmpty() {
            LinkedList<String> list = ListUtil.newLinkedList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newLinkedList - 从 Iterable 创建")
        void testNewLinkedListFromIterable() {
            LinkedList<String> list = ListUtil.newLinkedList(List.of("a", "b"));
            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("newLinkedList - 从 null Iterable 创建")
        void testNewLinkedListFromNullIterable() {
            LinkedList<String> list = ListUtil.newLinkedList(null);
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newCopyOnWriteArrayList - 空创建")
        void testNewCopyOnWriteArrayListEmpty() {
            CopyOnWriteArrayList<String> list = ListUtil.newCopyOnWriteArrayList();
            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("newCopyOnWriteArrayList - 从 Iterable 创建")
        void testNewCopyOnWriteArrayListFromIterable() {
            CopyOnWriteArrayList<String> list = ListUtil.newCopyOnWriteArrayList(List.of("a", "b"));
            assertThat(list).containsExactly("a", "b");
        }

        @Test
        @DisplayName("newCopyOnWriteArrayList - 从 null Iterable 创建")
        void testNewCopyOnWriteArrayListFromNullIterable() {
            CopyOnWriteArrayList<String> list = ListUtil.newCopyOnWriteArrayList(null);
            assertThat(list).isEmpty();
        }
    }

    @Nested
    @DisplayName("视图操作测试")
    class ViewOperationsTests {

        @Test
        @DisplayName("reverse - 反转视图")
        void testReverse() {
            List<String> list = List.of("a", "b", "c");
            List<String> reversed = ListUtil.reverse(list);
            assertThat(reversed).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("reverse - null 返回空")
        void testReverseNull() {
            List<String> reversed = ListUtil.reverse(null);
            assertThat(reversed).isEmpty();
        }

        @Test
        @DisplayName("reverse - 可修改视图")
        void testReverseModifiable() {
            ArrayList<String> list = new ArrayList<>(List.of("a", "b", "c"));
            List<String> reversed = ListUtil.reverse(list);

            // Test get
            assertThat(reversed.get(0)).isEqualTo("c");

            // Test set
            reversed.set(0, "x");
            assertThat(list.get(2)).isEqualTo("x");

            // Test add
            reversed.add(0, "y");
            assertThat(list.getLast()).isEqualTo("y");

            // Test remove
            reversed.remove(0);
            assertThat(list).doesNotContain("y");
        }

        @Test
        @DisplayName("partition - 分区视图")
        void testPartition() {
            List<Integer> list = List.of(1, 2, 3, 4, 5);
            List<List<Integer>> partitions = ListUtil.partition(list, 2);

            assertThat(partitions).hasSize(3);
            assertThat(partitions.get(0)).containsExactly(1, 2);
            assertThat(partitions.get(1)).containsExactly(3, 4);
            assertThat(partitions.get(2)).containsExactly(5);
        }

        @Test
        @DisplayName("partition - null 返回空")
        void testPartitionNull() {
            List<List<Integer>> result = ListUtil.partition(null, 2);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partition - 空列表返回空")
        void testPartitionEmpty() {
            List<List<Integer>> result = ListUtil.partition(List.of(), 2);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("partition - 非法大小")
        void testPartitionIllegalSize() {
            assertThatThrownBy(() -> ListUtil.partition(List.of(1, 2), 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ListUtil.partition(List.of(1, 2), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("partition - 索引越界")
        void testPartitionIndexOutOfBounds() {
            List<List<Integer>> partitions = ListUtil.partition(List.of(1, 2), 2);
            assertThatThrownBy(() -> partitions.get(5))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("transform - 转换视图")
        void testTransform() {
            List<String> list = List.of("a", "bb", "ccc");
            List<Integer> lengths = ListUtil.transform(list, String::length);

            assertThat(lengths).containsExactly(1, 2, 3);
            assertThat(lengths.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("transform - null 输入")
        void testTransformNull() {
            assertThat(ListUtil.transform(null, String::length)).isEmpty();
            assertThat(ListUtil.transform(List.of("a"), null)).isEmpty();
        }

        @Test
        @DisplayName("charactersOf - 字符串转字符列表")
        void testCharactersOfString() {
            List<Character> chars = ListUtil.charactersOf("hello");
            assertThat(chars).containsExactly('h', 'e', 'l', 'l', 'o');
            assertThat(chars.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("charactersOf - null/空字符串")
        void testCharactersOfStringEmpty() {
            assertThat(ListUtil.charactersOf((String) null)).isEmpty();
            assertThat(ListUtil.charactersOf("")).isEmpty();
        }

        @Test
        @DisplayName("charactersOf - CharSequence")
        void testCharactersOfCharSequence() {
            CharSequence cs = new StringBuilder("abc");
            List<Character> chars = ListUtil.charactersOf(cs);
            assertThat(chars).containsExactly('a', 'b', 'c');
        }

        @Test
        @DisplayName("charactersOf - null/空 CharSequence")
        void testCharactersOfCharSequenceEmpty() {
            assertThat(ListUtil.charactersOf((CharSequence) null)).isEmpty();
            assertThat(ListUtil.charactersOf(new StringBuilder())).isEmpty();
        }
    }

    @Nested
    @DisplayName("笛卡尔积测试")
    class CartesianProductTests {

        @Test
        @DisplayName("cartesianProduct - 两个列表")
        void testCartesianProductTwoLists() {
            List<String> a = List.of("a", "b");
            List<Integer> b = List.of(1, 2);

            @SuppressWarnings("unchecked")
            List<List<Object>> result = ListUtil.cartesianProduct(
                    (List<Object>) (List<?>) a,
                    (List<Object>) (List<?>) b
            );

            assertThat(result).hasSize(4);
            assertThat(result.get(0)).containsExactly("a", 1);
            assertThat(result.get(1)).containsExactly("a", 2);
            assertThat(result.get(2)).containsExactly("b", 1);
            assertThat(result.get(3)).containsExactly("b", 2);
        }

        @Test
        @DisplayName("cartesianProduct - null 返回单个空列表")
        void testCartesianProductNull() {
            List<List<String>> result = ListUtil.cartesianProduct((List<String>[]) null);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - 空数组返回单个空列表")
        void testCartesianProductEmptyArray() {
            List<List<String>> result = ListUtil.cartesianProduct();
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - List of Lists null")
        void testCartesianProductListNull() {
            List<List<String>> result = ListUtil.cartesianProduct((List<List<String>>) null);
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - List of Lists empty")
        void testCartesianProductListEmpty() {
            List<List<String>> result = ListUtil.cartesianProduct(List.of());
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - 包含空列表返回空")
        void testCartesianProductContainsEmpty() {
            List<List<String>> result = ListUtil.cartesianProduct(List.of(
                    List.of("a", "b"),
                    List.of(),
                    List.of("x")
            ));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - 包含 null 列表返回空")
        void testCartesianProductContainsNull() {
            List<List<String>> lists = new ArrayList<>();
            lists.add(List.of("a", "b"));
            lists.add(null);
            lists.add(List.of("x"));

            List<List<String>> result = ListUtil.cartesianProduct(lists);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("cartesianProduct - 索引越界")
        void testCartesianProductIndexOutOfBounds() {
            List<List<String>> result = ListUtil.cartesianProduct(List.of(
                    List.of("a", "b"),
                    List.of("x")
            ));
            assertThatThrownBy(() -> result.get(10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
            assertThatThrownBy(() -> result.get(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("cartesianProduct - 结果太大抛异常")
        void testCartesianProductTooLarge() {
            List<List<Integer>> largeLists = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                largeLists.add(List.of(1, 2, 3));
            }

            assertThatThrownBy(() -> ListUtil.cartesianProduct(largeLists))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
