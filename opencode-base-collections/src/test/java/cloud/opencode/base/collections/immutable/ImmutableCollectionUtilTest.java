package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableList;
import cloud.opencode.base.collections.ImmutableSet;
import cloud.opencode.base.collections.exception.OpenCollectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableCollectionUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("ImmutableCollectionUtil 测试")
class ImmutableCollectionUtilTest {

    @Nested
    @DisplayName("List 操作测试")
    class ListOperationTests {

        @Test
        @DisplayName("transform - 转换列表")
        void testTransform() {
            ImmutableList<String> list = ImmutableList.of("1", "2", "3");

            ImmutableList<Integer> result = ImmutableCollectionUtil.transform(list, Integer::parseInt);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("transform - null 列表")
        void testTransformNull() {
            ImmutableList<String> nullList = null;
            ImmutableList<Integer> result = ImmutableCollectionUtil.transform(nullList, Integer::parseInt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transform - 空列表")
        void testTransformEmpty() {
            ImmutableList<String> list = ImmutableList.of();

            ImmutableList<Integer> result = ImmutableCollectionUtil.transform(list, Integer::parseInt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - 连接列表")
        void testConcat() {
            ImmutableList<String> list1 = ImmutableList.of("a", "b");
            ImmutableList<String> list2 = ImmutableList.of("c", "d");

            ImmutableList<String> result = ImmutableCollectionUtil.concat(list1, list2);

            assertThat(result).containsExactly("a", "b", "c", "d");
        }

        @Test
        @DisplayName("concat - null 参数")
        void testConcatNull() {
            ImmutableList<String> result = ImmutableCollectionUtil.concat((ImmutableList<String>[]) null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("concat - 包含 null 的数组")
        void testConcatWithNulls() {
            ImmutableList<String> list1 = ImmutableList.of("a", "b");

            ImmutableList<String> result = ImmutableCollectionUtil.concat(list1, null);

            assertThat(result).containsExactly("a", "b");
        }

        @Test
        @DisplayName("reverse - 反转列表")
        void testReverse() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "c");

            ImmutableList<String> result = ImmutableCollectionUtil.reverse(list);

            assertThat(result).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("reverse - null 列表")
        void testReverseNull() {
            ImmutableList<String> result = ImmutableCollectionUtil.reverse(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("reverse - 单元素列表")
        void testReverseSingle() {
            ImmutableList<String> list = ImmutableList.of("a");

            ImmutableList<String> result = ImmutableCollectionUtil.reverse(list);

            assertThat(result).containsExactly("a");
        }

        @Test
        @DisplayName("subList - 子列表")
        void testSubList() {
            ImmutableList<Integer> list = ImmutableList.of(1, 2, 3, 4, 5);

            ImmutableList<Integer> result = ImmutableCollectionUtil.subList(list, 1, 4);

            assertThat(result).containsExactly(2, 3, 4);
        }

        @Test
        @DisplayName("subList - null 列表")
        void testSubListNull() {
            ImmutableList<Integer> result = ImmutableCollectionUtil.subList(null, 0, 1);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Set 操作测试")
    class SetOperationTests {

        @Test
        @DisplayName("transform - 转换集合")
        void testTransformSet() {
            ImmutableSet<String> set = ImmutableSet.of("1", "2", "3");

            ImmutableSet<Integer> result = ImmutableCollectionUtil.transform(set, Integer::parseInt);

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("transform - null 集合")
        void testTransformSetNull() {
            ImmutableSet<Integer> result = ImmutableCollectionUtil.transform((ImmutableSet<String>) null, Integer::parseInt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("union - 并集")
        void testUnion() {
            ImmutableSet<Integer> set1 = ImmutableSet.of(1, 2, 3);
            ImmutableSet<Integer> set2 = ImmutableSet.of(3, 4, 5);

            ImmutableSet<Integer> result = ImmutableCollectionUtil.union(set1, set2);

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("union - null 参数")
        void testUnionNull() {
            ImmutableSet<Integer> result = ImmutableCollectionUtil.union((ImmutableSet<Integer>[]) null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("intersection - 交集")
        void testIntersection() {
            ImmutableSet<Integer> set1 = ImmutableSet.of(1, 2, 3, 4);
            ImmutableSet<Integer> set2 = ImmutableSet.of(3, 4, 5, 6);

            ImmutableSet<Integer> result = ImmutableCollectionUtil.intersection(set1, set2);

            assertThat(result).containsExactlyInAnyOrder(3, 4);
        }

        @Test
        @DisplayName("intersection - null 参数")
        void testIntersectionNull() {
            ImmutableSet<Integer> result = ImmutableCollectionUtil.intersection(null, ImmutableSet.of(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("difference - 差集")
        void testDifference() {
            ImmutableSet<Integer> set1 = ImmutableSet.of(1, 2, 3, 4);
            ImmutableSet<Integer> set2 = ImmutableSet.of(3, 4, 5);

            ImmutableSet<Integer> result = ImmutableCollectionUtil.difference(set1, set2);

            assertThat(result).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("difference - null 第一个参数")
        void testDifferenceNullFirst() {
            ImmutableSet<Integer> result = ImmutableCollectionUtil.difference(null, ImmutableSet.of(1));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("difference - null 第二个参数")
        void testDifferenceNullSecond() {
            ImmutableSet<Integer> set1 = ImmutableSet.of(1, 2, 3);

            ImmutableSet<Integer> result = ImmutableCollectionUtil.difference(set1, null);

            assertThat(result).isEqualTo(set1);
        }
    }

    @Nested
    @DisplayName("Multiset 操作测试")
    class MultisetOperationTests {

        @Test
        @DisplayName("toMultiset - 转换为多重集")
        void testToMultiset() {
            List<String> list = List.of("a", "b", "a", "c", "b", "a");

            ImmutableMultiset<String> result = ImmutableCollectionUtil.toMultiset(list);

            assertThat(result.count("a")).isEqualTo(3);
            assertThat(result.count("b")).isEqualTo(2);
            assertThat(result.count("c")).isEqualTo(1);
        }

        @Test
        @DisplayName("transform - 转换多重集")
        void testTransformMultiset() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("1", "2", "1");

            ImmutableMultiset<Integer> result = ImmutableCollectionUtil.transform(multiset, Integer::parseInt);

            assertThat(result.count(1)).isEqualTo(2);
            assertThat(result.count(2)).isEqualTo(1);
        }

        @Test
        @DisplayName("combine - 组合多重集")
        void testCombine() {
            ImmutableMultiset<String> ms1 = ImmutableMultiset.of("a", "b");
            ImmutableMultiset<String> ms2 = ImmutableMultiset.of("b", "c");

            ImmutableMultiset<String> result = ImmutableCollectionUtil.combine(ms1, ms2);

            assertThat(result.count("a")).isEqualTo(1);
            assertThat(result.count("b")).isEqualTo(2);
            assertThat(result.count("c")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("BiMap 操作测试")
    class BiMapOperationTests {

        @Test
        @DisplayName("toBiMap - 转换为双向映射")
        void testToBiMap() {
            Map<String, Integer> map = Map.of("one", 1, "two", 2);

            ImmutableBiMap<String, Integer> result = ImmutableCollectionUtil.toBiMap(map);

            assertThat(result.get("one")).isEqualTo(1);
            assertThat(result.inverse().get(1)).isEqualTo("one");
        }

        @Test
        @DisplayName("zipToBiMap - 从两个列表创建双向映射")
        void testZipToBiMap() {
            List<String> keys = List.of("one", "two", "three");
            List<Integer> values = List.of(1, 2, 3);

            ImmutableBiMap<String, Integer> result = ImmutableCollectionUtil.zipToBiMap(keys, values);

            assertThat(result.get("one")).isEqualTo(1);
            assertThat(result.get("two")).isEqualTo(2);
            assertThat(result.get("three")).isEqualTo(3);
        }

        @Test
        @DisplayName("zipToBiMap - 列表大小不同抛异常")
        void testZipToBiMapDifferentSizes() {
            List<String> keys = List.of("one", "two");
            List<Integer> values = List.of(1);

            assertThatThrownBy(() -> ImmutableCollectionUtil.zipToBiMap(keys, values))
                    .isInstanceOf(OpenCollectionException.class);
        }
    }

    @Nested
    @DisplayName("Table 操作测试")
    class TableOperationTests {

        @Test
        @DisplayName("toTable - 从嵌套 Map 创建表格")
        void testToTable() {
            Map<String, Map<String, Integer>> nestedMap = new LinkedHashMap<>();
            nestedMap.put("row1", Map.of("col1", 1, "col2", 2));
            nestedMap.put("row2", Map.of("col1", 3, "col2", 4));

            ImmutableTable<String, String, Integer> result = ImmutableCollectionUtil.toTable(nestedMap);

            assertThat(result.get("row1", "col1")).isEqualTo(1);
            assertThat(result.get("row2", "col2")).isEqualTo(4);
        }

        @Test
        @DisplayName("toTable - null 参数")
        void testToTableNull() {
            ImmutableTable<String, String, Integer> result = ImmutableCollectionUtil.toTable(null);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("transpose - 转置表格")
        void testTranspose() {
            ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
                    .put("row1", "col1", 1)
                    .put("row1", "col2", 2)
                    .build();

            ImmutableTable<String, String, Integer> result = ImmutableCollectionUtil.transpose(table);

            assertThat(result.get("col1", "row1")).isEqualTo(1);
            assertThat(result.get("col2", "row1")).isEqualTo(2);
        }

        @Test
        @DisplayName("transpose - null 表格")
        void testTransposeNull() {
            ImmutableTable<String, String, Integer> result = ImmutableCollectionUtil.transpose(null);

            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("转换操作测试")
    class ConversionOperationTests {

        @Test
        @DisplayName("toSet - 列表转集合")
        void testToSet() {
            ImmutableList<String> list = ImmutableList.of("a", "b", "a", "c");

            ImmutableSet<String> result = ImmutableCollectionUtil.toSet(list);

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("toList - 集合转列表")
        void testToList() {
            ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");

            ImmutableList<String> result = ImmutableCollectionUtil.toList(set);

            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("toList - 多重集转列表")
        void testToListFromMultiset() {
            ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a");

            ImmutableList<String> result = ImmutableCollectionUtil.toList(multiset);

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("过滤操作测试")
    class FilteringOperationTests {

        @Test
        @DisplayName("filter - 过滤列表")
        void testFilterList() {
            ImmutableList<Integer> list = ImmutableList.of(1, 2, 3, 4, 5, 6);

            ImmutableList<Integer> result = ImmutableCollectionUtil.filter(list, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }

        @Test
        @DisplayName("filter - null 列表")
        void testFilterListNull() {
            ImmutableList<Integer> result = ImmutableCollectionUtil.filter((ImmutableList<Integer>) null, n -> true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - 过滤集合")
        void testFilterSet() {
            ImmutableSet<Integer> set = ImmutableSet.of(1, 2, 3, 4, 5, 6);

            ImmutableSet<Integer> result = ImmutableCollectionUtil.filter(set, n -> n % 2 == 0);

            assertThat(result).containsExactlyInAnyOrder(2, 4, 6);
        }

        @Test
        @DisplayName("filter - null 集合")
        void testFilterSetNull() {
            ImmutableSet<Integer> result = ImmutableCollectionUtil.filter((ImmutableSet<Integer>) null, n -> true);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("空值检查测试")
    class NullCheckTests {

        @Test
        @DisplayName("isNullOrEmpty - Collection")
        void testIsNullOrEmptyCollection() {
            assertThat(ImmutableCollectionUtil.isNullOrEmpty((Collection<?>) null)).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(List.of())).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(List.of("a"))).isFalse();
        }

        @Test
        @DisplayName("isNullOrEmpty - Map")
        void testIsNullOrEmptyMap() {
            assertThat(ImmutableCollectionUtil.isNullOrEmpty((Map<?, ?>) null)).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(Map.of())).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(Map.of("a", 1))).isFalse();
        }

        @Test
        @DisplayName("isNullOrEmpty - Table")
        void testIsNullOrEmptyTable() {
            assertThat(ImmutableCollectionUtil.isNullOrEmpty((ImmutableTable<?, ?, ?>) null)).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(ImmutableTable.of())).isTrue();
            assertThat(ImmutableCollectionUtil.isNullOrEmpty(ImmutableTable.of("r", "c", 1))).isFalse();
        }
    }
}
