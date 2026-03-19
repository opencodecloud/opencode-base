package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SetUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SetUtil 测试")
class SetUtilTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("newHashSet - 空创建")
        void testNewHashSetEmpty() {
            HashSet<String> set = SetUtil.newHashSet();
            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("newHashSet - 带元素")
        void testNewHashSetWithElements() {
            HashSet<String> set = SetUtil.newHashSet("a", "b", "c");
            assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
        }

        @Test
        @DisplayName("newLinkedHashSet - 创建")
        void testNewLinkedHashSet() {
            LinkedHashSet<String> set = SetUtil.newLinkedHashSet();
            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("newTreeSet - 创建")
        void testNewTreeSet() {
            TreeSet<String> set = SetUtil.newTreeSet();
            assertThat(set).isEmpty();
        }

        @Test
        @DisplayName("newConcurrentHashSet - 创建")
        void testNewConcurrentHashSet() {
            Set<String> set = SetUtil.newConcurrentHashSet();
            assertThat(set).isEmpty();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationsTests {

        @Test
        @DisplayName("union - 并集")
        void testUnion() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("c", "d", "e");

            SetView<String> result = SetUtil.union(set1, set2);

            assertThat(result).containsExactlyInAnyOrder("a", "b", "c", "d", "e");
            assertThat(result.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("intersection - 交集")
        void testIntersection() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> result = SetUtil.intersection(set1, set2);

            assertThat(result).containsExactlyInAnyOrder("b", "c");
            assertThat(result.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("difference - 差集")
        void testDifference() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> result = SetUtil.difference(set1, set2);

            assertThat(result).containsExactly("a");
            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("symmetricDifference - 对称差集")
        void testSymmetricDifference() {
            Set<String> set1 = Set.of("a", "b", "c");
            Set<String> set2 = Set.of("b", "c", "d");

            SetView<String> result = SetUtil.symmetricDifference(set1, set2);

            assertThat(result).containsExactlyInAnyOrder("a", "d");
        }
    }

    @Nested
    @DisplayName("SetView 测试")
    class SetViewTests {

        @Test
        @DisplayName("SetView 只读")
        void testSetViewReadOnly() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");

            SetView<String> union = SetUtil.union(set1, set2);

            assertThatThrownBy(() -> union.add("x"))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> union.remove("a"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("SetView copyInto")
        void testSetViewCopyInto() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");

            SetView<String> union = SetUtil.union(set1, set2);
            HashSet<String> copy = union.copyInto(new HashSet<>());

            assertThat(copy).containsExactlyInAnyOrder("a", "b", "c", "d");
        }

        @Test
        @DisplayName("SetView contains")
        void testSetViewContains() {
            Set<String> set1 = Set.of("a", "b");
            Set<String> set2 = Set.of("c", "d");

            SetView<String> union = SetUtil.union(set1, set2);

            assertThat(union.contains("a")).isTrue();
            assertThat(union.contains("c")).isTrue();
            assertThat(union.contains("x")).isFalse();
        }
    }

    @Nested
    @DisplayName("幂集组合测试")
    class PowerSetTests {

        @Test
        @DisplayName("powerSet - 幂集")
        void testPowerSet() {
            Set<String> set = Set.of("a", "b", "c");

            Set<Set<String>> result = SetUtil.powerSet(set);

            assertThat(result).hasSize(8); // 2^3 = 8
            assertThat(result).contains(Set.of());
            assertThat(result).contains(Set.of("a"));
            assertThat(result).contains(Set.of("a", "b"));
            assertThat(result).contains(Set.of("a", "b", "c"));
        }

        @Test
        @DisplayName("combinations - 组合")
        void testCombinations() {
            Set<String> set = Set.of("a", "b", "c", "d");

            Set<Set<String>> result = SetUtil.combinations(set, 2);

            assertThat(result).hasSize(6); // C(4,2) = 6
        }

        @Test
        @DisplayName("combinations - 空组合")
        void testCombinationsEmpty() {
            Set<String> set = Set.of("a", "b", "c");

            Set<Set<String>> result = SetUtil.combinations(set, 0);

            assertThat(result).hasSize(1);
            assertThat(result).contains(Set.of());
        }
    }

    @Nested
    @DisplayName("笛卡尔积测试")
    class CartesianProductTests {

        @Test
        @DisplayName("cartesianProduct - 两个集合")
        void testCartesianProduct() {
            Set<String> set1 = Set.of("a", "b");
            Set<Integer> set2 = Set.of(1, 2);

            Set<List<Object>> result = SetUtil.cartesianProduct(
                    List.of((Set<Object>) (Set) set1, (Set<Object>) (Set) set2));

            assertThat(result).hasSize(4);
        }
    }

    @Nested
    @DisplayName("过滤测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 过滤集合")
        void testFilter() {
            Set<Integer> set = Set.of(1, 2, 3, 4, 5, 6);

            Set<Integer> result = SetUtil.filter(set, n -> n % 2 == 0);

            assertThat(result).containsExactlyInAnyOrder(2, 4, 6);
        }

        @Test
        @DisplayName("filter - SortedSet")
        void testFilterSortedSet() {
            TreeSet<Integer> set = new TreeSet<>(Set.of(1, 2, 3, 4, 5, 6));

            SortedSet<Integer> result = SetUtil.filter(set, n -> n % 2 == 0);

            assertThat(result).containsExactly(2, 4, 6);
        }
    }

    @Nested
    @DisplayName("包装测试")
    class WrapperTests {

        @Test
        @DisplayName("synchronizedSet - 同步包装")
        void testSynchronizedSet() {
            Set<String> set = SetUtil.synchronizedSet(SetUtil.newHashSet("a", "b"));

            assertThat(set).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("unmodifiableSet - 不可修改包装")
        void testUnmodifiableSet() {
            Set<String> set = SetUtil.unmodifiableSet(SetUtil.newHashSet("a", "b"));

            assertThatThrownBy(() -> set.add("c"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
