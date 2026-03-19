package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * RangeSet 接口测试
 * 通过 TreeRangeSet 实现类测试接口的所有方法
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("RangeSet 接口测试")
class RangeSetTest {

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("contains - 检查值是否包含")
        void testContains() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.contains(5)).isTrue();
            assertThat(rangeSet.contains(1)).isTrue();
            assertThat(rangeSet.contains(10)).isTrue();
            assertThat(rangeSet.contains(0)).isFalse();
            assertThat(rangeSet.contains(11)).isFalse();
        }

        @Test
        @DisplayName("contains - 多个范围")
        void testContainsMultipleRanges() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));

            assertThat(rangeSet.contains(5)).isTrue();
            assertThat(rangeSet.contains(25)).isTrue();
            assertThat(rangeSet.contains(15)).isFalse();
        }

        @Test
        @DisplayName("rangeContaining - 获取包含值的范围")
        void testRangeContaining() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            Range<Integer> range = rangeSet.rangeContaining(5);

            assertThat(range).isEqualTo(Range.closed(1, 10));
        }

        @Test
        @DisplayName("rangeContaining - 不存在返回 null")
        void testRangeContainingNotExists() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            Range<Integer> range = rangeSet.rangeContaining(20);

            assertThat(range).isNull();
        }

        @Test
        @DisplayName("encloses - 检查是否完全包含范围")
        void testEncloses() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 20));

            assertThat(rangeSet.encloses(Range.closed(5, 10))).isTrue();
            assertThat(rangeSet.encloses(Range.closed(1, 20))).isTrue();
            assertThat(rangeSet.encloses(Range.closed(0, 10))).isFalse();
            assertThat(rangeSet.encloses(Range.closed(10, 25))).isFalse();
        }

        @Test
        @DisplayName("enclosesAll - 检查是否包含另一个 RangeSet 的所有范围")
        void testEnclosesAll() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 100));

            RangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(10, 20));
            other.add(Range.closed(30, 40));

            assertThat(rangeSet.enclosesAll(other)).isTrue();
        }

        @Test
        @DisplayName("enclosesAll - 不完全包含返回 false")
        void testEnclosesAllFalse() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 50));

            RangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(10, 60));

            assertThat(rangeSet.enclosesAll(other)).isFalse();
        }

        @Test
        @DisplayName("intersects - 检查是否与范围相交")
        void testIntersects() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.intersects(Range.closed(5, 15))).isTrue();
            assertThat(rangeSet.intersects(Range.closed(0, 5))).isTrue();
            assertThat(rangeSet.intersects(Range.closed(20, 30))).isFalse();
        }

        @Test
        @DisplayName("isEmpty - 空判断")
        void testIsEmpty() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();

            assertThat(rangeSet.isEmpty()).isTrue();

            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("视图方法测试")
    class ViewMethodTests {

        @Test
        @DisplayName("asRanges - 返回所有范围")
        void testAsRanges() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));

            Set<Range<Integer>> ranges = rangeSet.asRanges();

            assertThat(ranges).hasSize(2);
            assertThat(ranges).contains(Range.closed(1, 10), Range.closed(20, 30));
        }

        @Test
        @DisplayName("asDescendingSetOfRanges - 降序返回")
        void testAsDescendingSetOfRanges() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));

            Set<Range<Integer>> ranges = rangeSet.asDescendingSetOfRanges();

            assertThat(ranges).hasSize(2);
        }

        @Test
        @DisplayName("subRangeSet - 子范围视图")
        void testSubRangeSet() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));
            rangeSet.add(Range.closed(40, 50));

            RangeSet<Integer> subSet = rangeSet.subRangeSet(Range.closed(5, 35));

            assertThat(subSet.contains(7)).isTrue();
            assertThat(subSet.contains(25)).isTrue();
            assertThat(subSet.contains(3)).isFalse();
            assertThat(subSet.contains(45)).isFalse();
        }

        @Test
        @DisplayName("span - 获取跨度")
        void testSpan() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));

            Range<Integer> span = rangeSet.span();

            assertThat(span).isEqualTo(Range.closed(1, 30));
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationMethodTests {

        @Test
        @DisplayName("add - 添加范围")
        void testAdd() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();

            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.contains(5)).isTrue();
        }

        @Test
        @DisplayName("add - 自动合并重叠范围")
        void testAddCoalescing() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(5, 15));

            assertThat(rangeSet.asRanges()).hasSize(1);
            assertThat(rangeSet.rangeContaining(1)).isEqualTo(Range.closed(1, 15));
        }

        @Test
        @DisplayName("add - 自动合并相邻范围")
        void testAddAdjacentCoalescing() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(11, 20));

            // 闭区间 [1,10] 和 [11,20] 不会合并，因为 10 和 11 之间有间隙
            // 但 [1,10] 和 [10,20] 会合并
            assertThat(rangeSet.asRanges()).hasSize(2);
        }

        @Test
        @DisplayName("remove - 移除范围")
        void testRemove() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 20));

            rangeSet.remove(Range.closed(5, 15));

            assertThat(rangeSet.contains(3)).isTrue();
            assertThat(rangeSet.contains(10)).isFalse();
            assertThat(rangeSet.contains(18)).isTrue();
        }

        @Test
        @DisplayName("remove - 完全移除范围")
        void testRemoveComplete() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            rangeSet.remove(Range.closed(1, 10));

            assertThat(rangeSet.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("addAll - 添加另一个 RangeSet")
        void testAddAll() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            RangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(20, 30));

            rangeSet.addAll(other);

            assertThat(rangeSet.contains(5)).isTrue();
            assertThat(rangeSet.contains(25)).isTrue();
        }

        @Test
        @DisplayName("removeAll - 移除另一个 RangeSet")
        void testRemoveAll() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 100));

            RangeSet<Integer> other = TreeRangeSet.create();
            other.add(Range.closed(20, 30));
            other.add(Range.closed(50, 60));

            rangeSet.removeAll(other);

            assertThat(rangeSet.contains(10)).isTrue();
            assertThat(rangeSet.contains(25)).isFalse();
            assertThat(rangeSet.contains(40)).isTrue();
            assertThat(rangeSet.contains(55)).isFalse();
            assertThat(rangeSet.contains(80)).isTrue();
        }

        @Test
        @DisplayName("clear - 清空所有范围")
        void testClear() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(20, 30));

            rangeSet.clear();

            assertThat(rangeSet.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("开闭区间测试")
    class BoundTypeTests {

        @Test
        @DisplayName("closed - 闭区间")
        void testClosed() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.contains(1)).isTrue();
            assertThat(rangeSet.contains(10)).isTrue();
        }

        @Test
        @DisplayName("open - 开区间")
        void testOpen() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.open(1, 10));

            assertThat(rangeSet.contains(1)).isFalse();
            assertThat(rangeSet.contains(2)).isTrue();
            assertThat(rangeSet.contains(9)).isTrue();
            assertThat(rangeSet.contains(10)).isFalse();
        }

        @Test
        @DisplayName("closedOpen - 左闭右开")
        void testClosedOpen() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closedOpen(1, 10));

            assertThat(rangeSet.contains(1)).isTrue();
            assertThat(rangeSet.contains(10)).isFalse();
        }

        @Test
        @DisplayName("openClosed - 左开右闭")
        void testOpenClosed() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.openClosed(1, 10));

            assertThat(rangeSet.contains(1)).isFalse();
            assertThat(rangeSet.contains(10)).isTrue();
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("单点范围")
        void testSingletonRange() {
            RangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(5, 5));

            assertThat(rangeSet.contains(5)).isTrue();
            assertThat(rangeSet.contains(4)).isFalse();
            assertThat(rangeSet.contains(6)).isFalse();
        }

    }

    @Nested
    @DisplayName("字符串类型测试")
    class StringTypeTests {

        @Test
        @DisplayName("字符串范围")
        void testStringRange() {
            RangeSet<String> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed("a", "m"));
            rangeSet.add(Range.closed("n", "z"));

            assertThat(rangeSet.contains("c")).isTrue();
            assertThat(rangeSet.contains("p")).isTrue();
        }
    }
}
