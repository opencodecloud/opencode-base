package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.Range;
import cloud.opencode.base.collections.specialized.RangeSet;
import cloud.opencode.base.collections.specialized.TreeRangeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * ImmutableRangeSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
@DisplayName("ImmutableRangeSet 测试")
class ImmutableRangeSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() - 创建空集合")
        void testOfEmpty() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.of();

            assertThat(set.isEmpty()).isTrue();
            assertThat(set.asRanges()).isEmpty();
        }

        @Test
        @DisplayName("of(range) - 创建单范围集合")
        void testOfSingleRange() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.of(Range.closed(1, 10));

            assertThat(set.isEmpty()).isFalse();
            assertThat(set.asRanges()).hasSize(1);
            assertThat(set.contains(5)).isTrue();
        }

        @Test
        @DisplayName("of(emptyRange) - 空范围返回空集合")
        void testOfEmptyRange() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.of(Range.closedOpen(5, 5));

            assertThat(set.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("copyOf(RangeSet) - 复制范围集合")
        void testCopyOfRangeSet() {
            TreeRangeSet<Integer> tree = TreeRangeSet.create();
            tree.add(Range.closed(1, 5));
            tree.add(Range.closed(10, 15));

            ImmutableRangeSet<Integer> set = ImmutableRangeSet.copyOf(tree);

            assertThat(set.asRanges()).hasSize(2);
            assertThat(set.contains(3)).isTrue();
            assertThat(set.contains(12)).isTrue();
            assertThat(set.contains(7)).isFalse();
        }

        @Test
        @DisplayName("copyOf(ImmutableRangeSet) - 返回自身")
        void testCopyOfImmutableReturnsSame() {
            ImmutableRangeSet<Integer> original = ImmutableRangeSet.of(Range.closed(1, 10));
            ImmutableRangeSet<Integer> copy = ImmutableRangeSet.copyOf(original);

            assertThat(copy).isSameAs(original);
        }

        @Test
        @DisplayName("copyOf(Iterable<Range>) - 合并重叠范围")
        void testCopyOfIterable() {
            var ranges = java.util.List.of(
                    Range.closed(1, 5),
                    Range.closed(3, 8),
                    Range.closed(20, 30)
            );

            ImmutableRangeSet<Integer> set = ImmutableRangeSet.copyOf(ranges);

            // [1,5] and [3,8] should coalesce to [1,8]
            assertThat(set.asRanges()).hasSize(2);
            assertThat(set.contains(6)).isTrue();
            assertThat(set.contains(10)).isFalse();
            assertThat(set.contains(25)).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder - 构建多范围集合")
        void testBuilder() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 5))
                    .add(Range.closed(10, 15))
                    .add(Range.closed(20, 25))
                    .build();

            assertThat(set.asRanges()).hasSize(3);
            assertThat(set.contains(3)).isTrue();
            assertThat(set.contains(12)).isTrue();
            assertThat(set.contains(22)).isTrue();
            assertThat(set.contains(7)).isFalse();
        }

        @Test
        @DisplayName("builder - 合并重叠范围")
        void testBuilderCoalesces() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(5, 15))
                    .build();

            assertThat(set.asRanges()).hasSize(1);
            assertThat(set.contains(1)).isTrue();
            assertThat(set.contains(15)).isTrue();
        }

        @Test
        @DisplayName("builder - addAll 添加范围集合")
        void testBuilderAddAll() {
            TreeRangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(1, 5));
            other.add(Range.closed(10, 15));

            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .addAll(other)
                    .build();

            assertThat(set.asRanges()).hasSize(2);
        }

        @Test
        @DisplayName("builder - 空构建")
        void testBuilderEmpty() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder().build();

            assertThat(set.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        private final ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                .add(Range.closed(1, 10))
                .add(Range.closed(20, 30))
                .add(Range.closed(40, 50))
                .build();

        @Test
        @DisplayName("contains - 值在范围内返回 true")
        void testContainsInRange() {
            assertThat(set.contains(5)).isTrue();
            assertThat(set.contains(1)).isTrue();
            assertThat(set.contains(10)).isTrue();
            assertThat(set.contains(25)).isTrue();
            assertThat(set.contains(45)).isTrue();
        }

        @Test
        @DisplayName("contains - 值不在范围内返回 false")
        void testContainsOutOfRange() {
            assertThat(set.contains(0)).isFalse();
            assertThat(set.contains(15)).isFalse();
            assertThat(set.contains(35)).isFalse();
            assertThat(set.contains(55)).isFalse();
        }

        @Test
        @DisplayName("rangeContaining - 返回包含值的范围")
        void testRangeContaining() {
            Range<Integer> range = set.rangeContaining(5);

            assertThat(range).isNotNull();
            assertThat(range).isEqualTo(Range.closed(1, 10));
        }

        @Test
        @DisplayName("rangeContaining - 值不在任何范围内返回 null")
        void testRangeContainingNull() {
            assertThat(set.rangeContaining(15)).isNull();
        }

        @Test
        @DisplayName("encloses - 完全包含的范围")
        void testEncloses() {
            assertThat(set.encloses(Range.closed(2, 8))).isTrue();
            assertThat(set.encloses(Range.closed(1, 10))).isTrue();
        }

        @Test
        @DisplayName("encloses - 未完全包含的范围")
        void testEnclosesNot() {
            assertThat(set.encloses(Range.closed(5, 15))).isFalse();
            assertThat(set.encloses(Range.closed(15, 18))).isFalse();
        }

        @Test
        @DisplayName("encloses - 空范围返回 true")
        void testEnclosesEmpty() {
            assertThat(set.encloses(Range.closedOpen(5, 5))).isTrue();
        }

        @Test
        @DisplayName("enclosesAll - 完全包含所有范围")
        void testEnclosesAll() {
            TreeRangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(2, 8));
            other.add(Range.closed(22, 28));

            assertThat(set.enclosesAll(other)).isTrue();
        }

        @Test
        @DisplayName("enclosesAll - 不完全包含所有范围")
        void testEnclosesAllFalse() {
            TreeRangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(2, 8));
            other.add(Range.closed(15, 18));

            assertThat(set.enclosesAll(other)).isFalse();
        }

        @Test
        @DisplayName("intersects - 与范围相交")
        void testIntersects() {
            assertThat(set.intersects(Range.closed(5, 15))).isTrue();
            assertThat(set.intersects(Range.closed(8, 25))).isTrue();
        }

        @Test
        @DisplayName("intersects - 与范围不相交")
        void testIntersectsNot() {
            assertThat(set.intersects(Range.closed(11, 19))).isFalse();
            assertThat(set.intersects(Range.closed(55, 60))).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 非空集合")
        void testIsNotEmpty() {
            assertThat(set.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空集合")
        void testIsEmpty() {
            assertThat(ImmutableRangeSet.of().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("asRanges - 返回不可修改集合")
        void testAsRangesUnmodifiable() {
            Set<Range<Integer>> ranges = set.asRanges();

            assertThat(ranges).hasSize(3);
            assertThatThrownBy(() -> ranges.add(Range.closed(60, 70)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("asDescendingSetOfRanges - 返回降序集合")
        void testAsDescendingSetOfRanges() {
            Set<Range<Integer>> ranges = set.asDescendingSetOfRanges();
            var rangeList = new java.util.ArrayList<>(ranges);

            assertThat(rangeList).hasSize(3);
            assertThat(rangeList.get(0)).isEqualTo(Range.closed(40, 50));
            assertThat(rangeList.get(2)).isEqualTo(Range.closed(1, 10));
        }

        @Test
        @DisplayName("span - 返回包含所有范围的最小范围")
        void testSpan() {
            Range<Integer> span = set.span();

            assertThat(span).isEqualTo(Range.closed(1, 50));
        }

        @Test
        @DisplayName("span - 空集合抛出异常")
        void testSpanEmpty() {
            ImmutableRangeSet<Integer> empty = ImmutableRangeSet.of();

            assertThatThrownBy(empty::span)
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("complement 测试")
    class ComplementTests {

        @Test
        @DisplayName("complement - 返回间隙范围（含无穷头尾）")
        void testComplement() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(20, 30))
                    .build();

            RangeSet<Integer> comp = set.complement();

            // (-∞, 1) ∪ (10, 20) ∪ (30, +∞)
            assertThat(comp.asRanges()).hasSize(3);
            assertThat(comp.contains(15)).isTrue();
            assertThat(comp.contains(5)).isFalse();
            assertThat(comp.contains(25)).isFalse();
            assertThat(comp.contains(-100)).isTrue();
            assertThat(comp.contains(100)).isTrue();
        }

        @Test
        @DisplayName("complement - 空集合返回全集")
        void testComplementEmpty() {
            RangeSet<Integer> comp = ImmutableRangeSet.<Integer>of().complement();

            // Complement of empty is all values
            assertThat(comp.isEmpty()).isFalse();
            assertThat(comp.contains(0)).isTrue();
            assertThat(comp.contains(Integer.MAX_VALUE)).isTrue();
        }

        @Test
        @DisplayName("complement - 单范围返回两个无穷区间")
        void testComplementSingle() {
            RangeSet<Integer> comp = ImmutableRangeSet.of(Range.closed(1, 10)).complement();

            // (-∞, 1) ∪ (10, +∞)
            assertThat(comp.isEmpty()).isFalse();
            assertThat(comp.asRanges()).hasSize(2);
            assertThat(comp.contains(0)).isTrue();
            assertThat(comp.contains(11)).isTrue();
            assertThat(comp.contains(5)).isFalse();
        }
    }

    @Nested
    @DisplayName("subRangeSet 测试")
    class SubRangeSetTests {

        @Test
        @DisplayName("subRangeSet - 返回视图范围内的子集")
        void testSubRangeSet() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(20, 30))
                    .add(Range.closed(40, 50))
                    .build();

            RangeSet<Integer> sub = set.subRangeSet(Range.closed(5, 25));

            assertThat(sub.contains(5)).isTrue();
            assertThat(sub.contains(10)).isTrue();
            assertThat(sub.contains(20)).isTrue();
            assertThat(sub.contains(25)).isTrue();
            assertThat(sub.contains(1)).isFalse();
            assertThat(sub.contains(30)).isFalse();
            assertThat(sub.contains(45)).isFalse();
        }

        @Test
        @DisplayName("subRangeSet - 空视图返回空")
        void testSubRangeSetEmpty() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.of(Range.closed(1, 10));

            RangeSet<Integer> sub = set.subRangeSet(Range.closedOpen(5, 5));

            assertThat(sub.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("集合操作测试")
    class SetOperationTests {

        private final ImmutableRangeSet<Integer> a = ImmutableRangeSet.<Integer>builder()
                .add(Range.closed(1, 10))
                .add(Range.closed(20, 30))
                .build();

        private final ImmutableRangeSet<Integer> b = ImmutableRangeSet.<Integer>builder()
                .add(Range.closed(5, 25))
                .build();

        @Test
        @DisplayName("union - 两个集合的并集")
        void testUnion() {
            ImmutableRangeSet<Integer> result = a.union(b);

            // [1,10] + [5,25] + [20,30] → [1,30]
            assertThat(result.asRanges()).hasSize(1);
            assertThat(result.contains(1)).isTrue();
            assertThat(result.contains(15)).isTrue();
            assertThat(result.contains(30)).isTrue();
        }

        @Test
        @DisplayName("union - 与空集合的并集")
        void testUnionWithEmpty() {
            ImmutableRangeSet<Integer> result = a.union(ImmutableRangeSet.of());

            assertThat(result).isSameAs(a);
        }

        @Test
        @DisplayName("intersection - 两个集合的交集")
        void testIntersection() {
            ImmutableRangeSet<Integer> result = a.intersection(b);

            // [1,10] ∩ [5,25] = [5,10]; [20,30] ∩ [5,25] = [20,25]
            assertThat(result.asRanges()).hasSize(2);
            assertThat(result.contains(5)).isTrue();
            assertThat(result.contains(10)).isTrue();
            assertThat(result.contains(20)).isTrue();
            assertThat(result.contains(25)).isTrue();
            assertThat(result.contains(3)).isFalse();
            assertThat(result.contains(15)).isFalse();
            assertThat(result.contains(28)).isFalse();
        }

        @Test
        @DisplayName("intersection - 与空集合的交集")
        void testIntersectionWithEmpty() {
            ImmutableRangeSet<Integer> result = a.intersection(ImmutableRangeSet.of());

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("difference - 两个集合的差集")
        void testDifference() {
            ImmutableRangeSet<Integer> result = a.difference(b);

            // a = {[1,10], [20,30]}, b = {[5,25]}
            // a - b = {[1,5), (25,30]}
            assertThat(result.contains(3)).isTrue();
            assertThat(result.contains(28)).isTrue();
            assertThat(result.contains(5)).isFalse();
            assertThat(result.contains(15)).isFalse();
            assertThat(result.contains(25)).isFalse();
        }

        @Test
        @DisplayName("difference - 与空集合的差集返回自身")
        void testDifferenceWithEmpty() {
            ImmutableRangeSet<Integer> result = a.difference(ImmutableRangeSet.of());

            assertThat(result).isSameAs(a);
        }
    }

    @Nested
    @DisplayName("变更方法测试 - 抛出异常")
    class MutationMethodTests {

        private final ImmutableRangeSet<Integer> set = ImmutableRangeSet.of(Range.closed(1, 10));

        @Test
        @DisplayName("add - 抛出 UnsupportedOperationException")
        void testAddThrows() {
            assertThatThrownBy(() -> set.add(Range.closed(20, 30)))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeSet does not support mutation");
        }

        @Test
        @DisplayName("remove - 抛出 UnsupportedOperationException")
        void testRemoveThrows() {
            assertThatThrownBy(() -> set.remove(Range.closed(1, 5)))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeSet does not support mutation");
        }

        @Test
        @DisplayName("addAll - 抛出 UnsupportedOperationException")
        void testAddAllThrows() {
            TreeRangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(20, 30));

            assertThatThrownBy(() -> set.addAll(other))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeSet does not support mutation");
        }

        @Test
        @DisplayName("removeAll - 抛出 UnsupportedOperationException")
        void testRemoveAllThrows() {
            TreeRangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(1, 5));

            assertThatThrownBy(() -> set.removeAll(other))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeSet does not support mutation");
        }

        @Test
        @DisplayName("clear - 抛出 UnsupportedOperationException")
        void testClearThrows() {
            assertThatThrownBy(set::clear)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("ImmutableRangeSet does not support mutation");
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相同范围相等")
        void testEquals() {
            ImmutableRangeSet<Integer> a = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(20, 30))
                    .build();
            ImmutableRangeSet<Integer> b = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(20, 30))
                    .build();

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("equals - 与 TreeRangeSet 相等")
        void testEqualsWithTreeRangeSet() {
            ImmutableRangeSet<Integer> immutable = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .build();
            TreeRangeSet<Integer> tree = TreeRangeSet.create();
            tree.add(Range.closed(1, 10));

            assertThat(immutable).isEqualTo(tree);
        }

        @Test
        @DisplayName("equals - 不同范围不相等")
        void testNotEquals() {
            ImmutableRangeSet<Integer> a = ImmutableRangeSet.of(Range.closed(1, 10));
            ImmutableRangeSet<Integer> b = ImmutableRangeSet.of(Range.closed(1, 20));

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("hashCode - 相同范围相同哈希")
        void testHashCode() {
            ImmutableRangeSet<Integer> a = ImmutableRangeSet.of(Range.closed(1, 10));
            ImmutableRangeSet<Integer> b = ImmutableRangeSet.of(Range.closed(1, 10));

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString - 包含范围表示")
        void testToString() {
            ImmutableRangeSet<Integer> set = ImmutableRangeSet.<Integer>builder()
                    .add(Range.closed(1, 10))
                    .add(Range.closed(20, 30))
                    .build();

            String str = set.toString();

            assertThat(str).contains("[1..10]");
            assertThat(str).contains("[20..30]");
        }
    }
}
