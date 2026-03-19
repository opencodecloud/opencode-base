package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCollection 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenCollection 测试")
class OpenCollectionTest {

    @Nested
    @DisplayName("空值检查测试")
    class EmptyCheckTests {

        @Test
        @DisplayName("isEmpty - null 集合")
        void testIsEmptyNullCollection() {
            assertThat(OpenCollection.isEmpty((Collection<?>) null)).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 空集合")
        void testIsEmptyEmptyCollection() {
            assertThat(OpenCollection.isEmpty(List.of())).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 非空集合")
        void testIsEmptyNonEmptyCollection() {
            assertThat(OpenCollection.isEmpty(List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("isNotEmpty - 非空集合")
        void testIsNotEmptyNonEmptyCollection() {
            assertThat(OpenCollection.isNotEmpty(List.of("a", "b"))).isTrue();
        }

        @Test
        @DisplayName("isEmpty - null Map")
        void testIsEmptyNullMap() {
            assertThat(OpenCollection.isEmpty((Map<?, ?>) null)).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 空 Map")
        void testIsEmptyEmptyMap() {
            assertThat(OpenCollection.isEmpty(Map.of())).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 非空 Map")
        void testIsEmptyNonEmptyMap() {
            assertThat(OpenCollection.isEmpty(Map.of("a", 1))).isFalse();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationsTests {

        @Test
        @DisplayName("union - 两个集合")
        void testUnion() {
            List<String> a = List.of("a", "b", "c");
            List<String> b = List.of("c", "d", "e");

            Collection<String> result = OpenCollection.union(a, b);

            assertThat(result).containsExactly("a", "b", "c", "c", "d", "e");
        }

        @Test
        @DisplayName("intersection - 两个集合")
        void testIntersection() {
            List<String> a = List.of("a", "b", "c");
            List<String> b = List.of("b", "c", "d");

            Collection<String> result = OpenCollection.intersection(a, b);

            assertThat(result).containsExactlyInAnyOrder("b", "c");
        }

        @Test
        @DisplayName("subtract - 差集")
        void testSubtract() {
            List<String> a = List.of("a", "b", "c");
            List<String> b = List.of("b", "c", "d");

            Collection<String> result = OpenCollection.subtract(a, b);

            assertThat(result).containsExactly("a");
        }

        @Test
        @DisplayName("disjunction - 对称差集")
        void testDisjunction() {
            List<String> a = List.of("a", "b", "c");
            List<String> b = List.of("b", "c", "d");

            Collection<String> result = OpenCollection.disjunction(a, b);

            assertThat(result).containsExactlyInAnyOrder("a", "d");
        }
    }

    @Nested
    @DisplayName("元素检查测试")
    class ElementCheckTests {

        @Test
        @DisplayName("containsAny - 包含任一元素")
        void testContainsAny() {
            List<String> list = List.of("a", "b", "c");

            assertThat(OpenCollection.containsAny(list, "b", "x")).isTrue();
            assertThat(OpenCollection.containsAny(list, "x", "y")).isFalse();
        }

        @Test
        @DisplayName("containsAll - 包含所有元素")
        void testContainsAll() {
            List<String> list = List.of("a", "b", "c");

            assertThat(OpenCollection.containsAll(list, List.of("a", "b"))).isTrue();
            assertThat(OpenCollection.containsAll(list, List.of("a", "x"))).isFalse();
        }

        @Test
        @DisplayName("isEqualCollection - 相等比较")
        void testIsEqualCollection() {
            List<String> a = List.of("a", "b", "c");
            List<String> b = List.of("c", "b", "a");
            List<String> c = List.of("a", "b");

            assertThat(OpenCollection.isEqualCollection(a, b)).isTrue();
            assertThat(OpenCollection.isEqualCollection(a, c)).isFalse();
        }

        @Test
        @DisplayName("isSubCollection - 子集检查")
        void testIsSubCollection() {
            List<String> sub = List.of("a", "b");
            List<String> sup = List.of("a", "b", "c");

            assertThat(OpenCollection.isSubCollection(sub, sup)).isTrue();
            assertThat(OpenCollection.isSubCollection(sup, sub)).isFalse();
        }
    }

    @Nested
    @DisplayName("统计测试")
    class StatisticsTests {

        @Test
        @DisplayName("getCardinalityMap - 基数映射")
        void testGetCardinalityMap() {
            List<String> list = List.of("a", "b", "a", "c", "a");

            Map<String, Integer> result = OpenCollection.getCardinalityMap(list);

            assertThat(result).containsEntry("a", 3);
            assertThat(result).containsEntry("b", 1);
            assertThat(result).containsEntry("c", 1);
        }

        @Test
        @DisplayName("cardinality - 元素计数")
        void testCardinality() {
            List<String> list = List.of("a", "b", "a", "c", "a");

            assertThat(OpenCollection.cardinality("a", list)).isEqualTo(3);
            assertThat(OpenCollection.cardinality("x", list)).isEqualTo(0);
        }

        @Test
        @DisplayName("countMatches - 条件计数")
        void testCountMatches() {
            List<String> list = List.of("apple", "banana", "apricot", "cherry");

            int count = OpenCollection.countMatches(list, s -> s.startsWith("a"));

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("过滤转换测试")
    class FilterTransformTests {

        @Test
        @DisplayName("select - 选择匹配")
        void testSelect() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

            Collection<Integer> result = OpenCollection.select(list, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("selectRejected - 选择不匹配")
        void testSelectRejected() {
            List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

            Collection<Integer> result = OpenCollection.selectRejected(list, n -> n % 2 == 0);

            assertThat(result).containsExactly(1, 3, 5);
        }

        @Test
        @DisplayName("collect - 转换收集")
        void testCollect() {
            List<String> list = List.of("a", "bb", "ccc");

            Collection<Integer> result = OpenCollection.collect(list, String::length);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("filter - 原地过滤")
        void testFilter() {
            List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

            boolean modified = OpenCollection.filter(list, n -> n % 2 == 0);

            assertThat(modified).isTrue();
            assertThat(list).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("查找测试")
    class SearchTests {

        @Test
        @DisplayName("find - 查找匹配")
        void testFind() {
            List<String> list = List.of("apple", "banana", "cherry");

            String result = OpenCollection.find(list, s -> s.startsWith("b"));

            assertThat(result).isEqualTo("banana");
        }

        @Test
        @DisplayName("find - 未找到")
        void testFindNotFound() {
            List<String> list = List.of("apple", "banana", "cherry");

            String result = OpenCollection.find(list, s -> s.startsWith("x"));

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("exists - 存在匹配")
        void testExists() {
            List<String> list = List.of("apple", "banana", "cherry");

            assertThat(OpenCollection.exists(list, s -> s.startsWith("b"))).isTrue();
            assertThat(OpenCollection.exists(list, s -> s.startsWith("x"))).isFalse();
        }

        @Test
        @DisplayName("matchesAll - 全部匹配")
        void testMatchesAll() {
            List<Integer> list = List.of(2, 4, 6, 8);

            assertThat(OpenCollection.matchesAll(list, n -> n % 2 == 0)).isTrue();
            assertThat(OpenCollection.matchesAll(list, n -> n > 5)).isFalse();
        }

        @Test
        @DisplayName("extractSingleton - 提取单元素")
        void testExtractSingleton() {
            List<String> single = List.of("only");

            assertThat(OpenCollection.extractSingleton(single)).isEqualTo("only");
        }

        @Test
        @DisplayName("extractSingleton - 多元素抛异常")
        void testExtractSingletonMultiple() {
            List<String> multiple = List.of("a", "b");

            assertThatThrownBy(() -> OpenCollection.extractSingleton(multiple))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationsTests {

        @Test
        @DisplayName("addAll - 数组添加")
        void testAddAllArray() {
            List<String> list = new ArrayList<>();

            boolean modified = OpenCollection.addAll(list, "a", "b", "c");

            assertThat(modified).isTrue();
            assertThat(list).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("addIgnoreNull - 忽略null")
        void testAddIgnoreNull() {
            List<String> list = new ArrayList<>();

            assertThat(OpenCollection.addIgnoreNull(list, "a")).isTrue();
            assertThat(OpenCollection.addIgnoreNull(list, null)).isFalse();
            assertThat(list).containsExactly("a");
        }

        @Test
        @DisplayName("removeNulls - 移除null")
        void testRemoveNulls() {
            List<String> list = new ArrayList<>();
            list.add("a");
            list.add(null);
            list.add("b");
            list.add(null);

            boolean modified = OpenCollection.removeNulls(list);

            assertThat(modified).isTrue();
            assertThat(list).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("访问测试")
    class AccessTests {

        @Test
        @DisplayName("get - 按索引获取")
        void testGet() {
            List<String> list = List.of("a", "b", "c");

            assertThat(OpenCollection.get(list, 1)).isEqualTo("b");
        }

        @Test
        @DisplayName("get - 索引越界")
        void testGetOutOfBounds() {
            List<String> list = List.of("a", "b", "c");

            assertThatThrownBy(() -> OpenCollection.get(list, 10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("getFirst - 获取第一个")
        void testGetFirst() {
            List<String> list = List.of("a", "b", "c");

            assertThat(OpenCollection.getFirst(list)).isEqualTo("a");
        }

        @Test
        @DisplayName("getLast - 获取最后一个")
        void testGetLast() {
            List<String> list = List.of("a", "b", "c");

            assertThat(OpenCollection.getLast(list)).isEqualTo("c");
        }

        @Test
        @DisplayName("size - 获取大小")
        void testSize() {
            assertThat(OpenCollection.size(List.of("a", "b", "c"))).isEqualTo(3);
            assertThat(OpenCollection.size(Map.of("a", 1, "b", 2))).isEqualTo(2);
            assertThat(OpenCollection.size(new String[]{"x", "y"})).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("排列组合测试")
    class PermutationTests {

        @Test
        @DisplayName("permutations - 生成排列")
        void testPermutations() {
            List<String> list = List.of("a", "b", "c");

            Collection<List<String>> result = OpenCollection.permutations(list);

            assertThat(result).hasSize(6); // 3! = 6
        }

        @Test
        @DisplayName("collate - 合并有序集合")
        void testCollate() {
            List<Integer> a = List.of(1, 3, 5);
            List<Integer> b = List.of(2, 4, 6);

            List<Integer> result = OpenCollection.collate(a, b, Comparator.naturalOrder());

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }
    }
}
