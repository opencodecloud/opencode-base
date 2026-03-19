package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("OpenSet 测试")
class OpenSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newHashSet - 创建空 HashSet")
        void testNewHashSet() {
            HashSet<String> set = OpenSet.newHashSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("of - 从元素创建 HashSet")
        void testOf() {
            HashSet<String> set = OpenSet.of("a", "b", "c");

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("of - 去重")
        void testOfDedupe() {
            HashSet<String> set = OpenSet.of("a", "b", "a");

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("from - 从 Iterable 创建")
        void testFrom() {
            HashSet<String> set = OpenSet.from(List.of("a", "b", "a"));

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("from - 从 Collection 创建")
        void testFromCollection() {
            Set<String> source = Set.of("a", "b", "c");
            HashSet<String> set = OpenSet.from(source);

            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("withExpectedSize - 指定预期大小")
        void testWithExpectedSize() {
            HashSet<String> set = OpenSet.withExpectedSize(100);

            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("newLinkedHashSet - 创建空 LinkedHashSet")
        void testNewLinkedHashSet() {
            LinkedHashSet<String> set = OpenSet.newLinkedHashSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(LinkedHashSet.class);
        }

        @Test
        @DisplayName("newTreeSet - 创建空 TreeSet")
        void testNewTreeSet() {
            TreeSet<String> set = OpenSet.newTreeSet();

            assertThat(set).isEmpty();
            assertThat(set).isInstanceOf(TreeSet.class);
        }

        @Test
        @DisplayName("newTreeSet - 带比较器")
        void testNewTreeSetWithComparator() {
            TreeSet<String> set = OpenSet.newTreeSet(Comparator.reverseOrder());
            set.addAll(List.of("a", "b", "c"));

            assertThat(new ArrayList<>(set)).containsExactly("c", "b", "a");
        }

        @Test
        @DisplayName("newConcurrentHashSet - 创建并发 HashSet")
        void testNewConcurrentHashSet() {
            Set<String> set = OpenSet.newConcurrentHashSet();
            set.add("a");

            assertThat(set).contains("a");
        }
    }

    @Nested
    @DisplayName("集合代数测试")
    class SetAlgebraTests {

        @Test
        @DisplayName("union - 并集")
        void testUnion() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(3, 4, 5);

            SetView<Integer> union = OpenSet.union(set1, set2);

            assertThat(union).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("intersection - 交集")
        void testIntersection() {
            Set<Integer> set1 = Set.of(1, 2, 3, 4);
            Set<Integer> set2 = Set.of(3, 4, 5, 6);

            SetView<Integer> intersection = OpenSet.intersection(set1, set2);

            assertThat(intersection).containsExactlyInAnyOrder(3, 4);
        }

        @Test
        @DisplayName("difference - 差集")
        void testDifference() {
            Set<Integer> set1 = Set.of(1, 2, 3, 4);
            Set<Integer> set2 = Set.of(3, 4, 5);

            SetView<Integer> difference = OpenSet.difference(set1, set2);

            assertThat(difference).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("symmetricDifference - 对称差集")
        void testSymmetricDifference() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(2, 3, 4);

            SetView<Integer> symmetricDiff = OpenSet.symmetricDifference(set1, set2);

            assertThat(symmetricDiff).containsExactlyInAnyOrder(1, 4);
        }
    }

    @Nested
    @DisplayName("高级操作测试")
    class AdvancedOperationTests {

        @Test
        @DisplayName("powerSet - 幂集")
        void testPowerSet() {
            Set<Integer> set = Set.of(1, 2);

            Set<Set<Integer>> powerSet = OpenSet.powerSet(set);

            assertThat(powerSet).hasSize(4);
        }

        @Test
        @DisplayName("combinations - 组合")
        void testCombinations() {
            Set<Integer> set = Set.of(1, 2, 3);

            Set<Set<Integer>> combinations = OpenSet.combinations(set, 2);

            assertThat(combinations).hasSize(3);
        }

        @Test
        @DisplayName("cartesianProduct - 笛卡尔积")
        void testCartesianProduct() {
            Set<Integer> set1 = Set.of(1, 2);
            Set<Integer> set2 = Set.of(3, 4);

            Set<List<Integer>> product = OpenSet.cartesianProduct(set1, set2);

            assertThat(product).hasSize(4);
        }
    }

    @Nested
    @DisplayName("过滤操作测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 按谓词过滤")
        void testFilter() {
            Set<Integer> set = Set.of(1, 2, 3, 4, 5, 6);

            Set<Integer> result = OpenSet.filter(set, n -> n % 2 == 0);

            assertThat(result).containsExactlyInAnyOrder(2, 4, 6);
        }

        @Test
        @DisplayName("filter - null Set")
        void testFilterNull() {
            Set<Integer> result = OpenSet.filter(null, n -> true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - 空 Set")
        void testFilterEmpty() {
            Set<Integer> result = OpenSet.filter(Set.of(), n -> true);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter - 无匹配")
        void testFilterNoMatch() {
            Set<Integer> set = Set.of(1, 3, 5);

            Set<Integer> result = OpenSet.filter(set, n -> n % 2 == 0);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("查询操作测试")
    class QueryTests {

        @Test
        @DisplayName("algebra - 获取 SetAlgebra")
        void testAlgebra() {
            Set<Integer> set = Set.of(1, 2, 3);

            SetAlgebra<Integer> algebra = OpenSet.algebra(set);

            assertThat(algebra).isNotNull();
            assertThat(algebra.getSet()).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("disjoint - 不相交")
        void testDisjoint() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(4, 5, 6);

            assertThat(OpenSet.disjoint(set1, set2)).isTrue();
        }

        @Test
        @DisplayName("disjoint - 有交集")
        void testNotDisjoint() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(3, 4, 5);

            assertThat(OpenSet.disjoint(set1, set2)).isFalse();
        }

        @Test
        @DisplayName("disjoint - null Set")
        void testDisjointNull() {
            assertThat(OpenSet.disjoint(null, Set.of(1))).isTrue();
            assertThat(OpenSet.disjoint(Set.of(1), null)).isTrue();
        }

        @Test
        @DisplayName("disjoint - 空 Set")
        void testDisjointEmpty() {
            assertThat(OpenSet.disjoint(Set.of(), Set.of(1))).isTrue();
            assertThat(OpenSet.disjoint(Set.of(1), Set.of())).isTrue();
        }

        @Test
        @DisplayName("isSubset - 子集")
        void testIsSubset() {
            Set<Integer> subset = Set.of(1, 2);
            Set<Integer> superset = Set.of(1, 2, 3);

            assertThat(OpenSet.isSubset(subset, superset)).isTrue();
        }

        @Test
        @DisplayName("isSubset - 不是子集")
        void testIsNotSubset() {
            Set<Integer> set1 = Set.of(1, 2, 4);
            Set<Integer> set2 = Set.of(1, 2, 3);

            assertThat(OpenSet.isSubset(set1, set2)).isFalse();
        }

        @Test
        @DisplayName("isSubset - null 或空")
        void testIsSubsetNullOrEmpty() {
            assertThat(OpenSet.isSubset(null, Set.of(1))).isTrue();
            assertThat(OpenSet.isSubset(Set.of(), Set.of(1))).isTrue();
            assertThat(OpenSet.isSubset(Set.of(1), null)).isFalse();
        }

        @Test
        @DisplayName("equals - 集合相等")
        void testEquals() {
            Set<Integer> set1 = Set.of(1, 2, 3);
            Set<Integer> set2 = Set.of(1, 2, 3);
            Set<Integer> set3 = Set.of(1, 2, 4);

            assertThat(OpenSet.equals(set1, set2)).isTrue();
            assertThat(OpenSet.equals(set1, set3)).isFalse();
        }

        @Test
        @DisplayName("equals - 同一实例")
        void testEqualsSameInstance() {
            Set<Integer> set = Set.of(1, 2, 3);

            assertThat(OpenSet.equals(set, set)).isTrue();
        }

        @Test
        @DisplayName("equals - null")
        void testEqualsNull() {
            assertThat(OpenSet.equals(null, Set.of(1))).isFalse();
            assertThat(OpenSet.equals(Set.of(1), null)).isFalse();
            // Both null should also return false in their method
        }
    }
}
