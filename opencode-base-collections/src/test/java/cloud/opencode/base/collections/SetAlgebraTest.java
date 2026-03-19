package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SetAlgebra 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("SetAlgebra 测试")
class SetAlgebraTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of - 从 Set 创建")
        void testOf() {
            Set<Integer> set = Set.of(1, 2, 3);
            SetAlgebra<Integer> algebra = SetAlgebra.of(set);

            assertThat(algebra.getSet()).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("of - null Set")
        void testOfNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(null);

            assertThat(algebra.getSet()).isEmpty();
        }
    }

    @Nested
    @DisplayName("并集测试")
    class UnionTests {

        @Test
        @DisplayName("union - 基本并集")
        void testUnion() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.union(Set.of(3, 4, 5));

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("union - null 参数")
        void testUnionNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.union(null);

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("union - 空集合")
        void testUnionEmpty() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.union(Set.of());

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @Nested
    @DisplayName("交集测试")
    class IntersectionTests {

        @Test
        @DisplayName("intersection - 基本交集")
        void testIntersection() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3, 4));

            Set<Integer> result = algebra.intersection(Set.of(3, 4, 5, 6));

            assertThat(result).containsExactlyInAnyOrder(3, 4);
        }

        @Test
        @DisplayName("intersection - null 参数")
        void testIntersectionNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.intersection(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("intersection - 无交集")
        void testIntersectionDisjoint() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.intersection(Set.of(4, 5, 6));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("差集测试")
    class DifferenceTests {

        @Test
        @DisplayName("difference - 基本差集")
        void testDifference() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3, 4));

            Set<Integer> result = algebra.difference(Set.of(3, 4, 5));

            assertThat(result).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("difference - null 参数")
        void testDifferenceNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.difference(null);

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("difference - 完全差集")
        void testDifferenceFull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.difference(Set.of(1, 2, 3, 4, 5));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("对称差集测试")
    class SymmetricDifferenceTests {

        @Test
        @DisplayName("symmetricDifference - 基本对称差集")
        void testSymmetricDifference() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.symmetricDifference(Set.of(2, 3, 4));

            assertThat(result).containsExactlyInAnyOrder(1, 4);
        }

        @Test
        @DisplayName("symmetricDifference - null 参数")
        void testSymmetricDifferenceNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.symmetricDifference(null);

            assertThat(result).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("symmetricDifference - 相同集合")
        void testSymmetricDifferenceSame() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            Set<Integer> result = algebra.symmetricDifference(Set.of(1, 2, 3));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("子集/超集测试")
    class SubsetSupersetTests {

        @Test
        @DisplayName("isSubsetOf - 是子集")
        void testIsSubsetOf() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2));

            assertThat(algebra.isSubsetOf(Set.of(1, 2, 3))).isTrue();
            assertThat(algebra.isSubsetOf(Set.of(1, 2))).isTrue();
        }

        @Test
        @DisplayName("isSubsetOf - 不是子集")
        void testIsNotSubsetOf() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isSubsetOf(Set.of(1, 2))).isFalse();
        }

        @Test
        @DisplayName("isSubsetOf - null 参数")
        void testIsSubsetOfNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2));
            assertThat(algebra.isSubsetOf(null)).isFalse();

            SetAlgebra<Integer> empty = SetAlgebra.of(Set.of());
            assertThat(empty.isSubsetOf(null)).isTrue();
        }

        @Test
        @DisplayName("isSupersetOf - 是超集")
        void testIsSupersetOf() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isSupersetOf(Set.of(1, 2))).isTrue();
            assertThat(algebra.isSupersetOf(Set.of(1, 2, 3))).isTrue();
        }

        @Test
        @DisplayName("isSupersetOf - null 参数")
        void testIsSupersetOfNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2));

            assertThat(algebra.isSupersetOf(null)).isTrue();
        }

        @Test
        @DisplayName("isProperSubsetOf - 真子集")
        void testIsProperSubsetOf() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2));

            assertThat(algebra.isProperSubsetOf(Set.of(1, 2, 3))).isTrue();
            assertThat(algebra.isProperSubsetOf(Set.of(1, 2))).isFalse();
        }

        @Test
        @DisplayName("isProperSupersetOf - 真超集")
        void testIsProperSupersetOf() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isProperSupersetOf(Set.of(1, 2))).isTrue();
            assertThat(algebra.isProperSupersetOf(Set.of(1, 2, 3))).isFalse();
        }

        @Test
        @DisplayName("isProperSupersetOf - null 参数")
        void testIsProperSupersetOfNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2));
            assertThat(algebra.isProperSupersetOf(null)).isTrue();

            SetAlgebra<Integer> empty = SetAlgebra.of(Set.of());
            assertThat(empty.isProperSupersetOf(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("不相交测试")
    class DisjointTests {

        @Test
        @DisplayName("isDisjoint - 不相交")
        void testIsDisjoint() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isDisjoint(Set.of(4, 5, 6))).isTrue();
        }

        @Test
        @DisplayName("isDisjoint - 有交集")
        void testIsNotDisjoint() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isDisjoint(Set.of(3, 4, 5))).isFalse();
        }

        @Test
        @DisplayName("isDisjoint - null 参数")
        void testIsDisjointNull() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isDisjoint(null)).isTrue();
        }

        @Test
        @DisplayName("isDisjoint - 空集合")
        void testIsDisjointEmpty() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3));

            assertThat(algebra.isDisjoint(Set.of())).isTrue();
        }
    }

    @Nested
    @DisplayName("过滤测试")
    class FilterTests {

        @Test
        @DisplayName("filter - 按谓词过滤")
        void testFilter() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 2, 3, 4, 5, 6));

            Set<Integer> result = algebra.filter(n -> n % 2 == 0);

            assertThat(result).containsExactlyInAnyOrder(2, 4, 6);
        }

        @Test
        @DisplayName("filter - 全部匹配")
        void testFilterAllMatch() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(2, 4, 6));

            Set<Integer> result = algebra.filter(n -> n % 2 == 0);

            assertThat(result).containsExactlyInAnyOrder(2, 4, 6);
        }

        @Test
        @DisplayName("filter - 无匹配")
        void testFilterNoMatch() {
            SetAlgebra<Integer> algebra = SetAlgebra.of(Set.of(1, 3, 5));

            Set<Integer> result = algebra.filter(n -> n % 2 == 0);

            assertThat(result).isEmpty();
        }
    }
}
