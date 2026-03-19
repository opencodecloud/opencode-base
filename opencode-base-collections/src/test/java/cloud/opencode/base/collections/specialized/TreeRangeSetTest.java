package cloud.opencode.base.collections.specialized;

import cloud.opencode.base.collections.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TreeRangeSet 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("TreeRangeSet 测试")
class TreeRangeSetTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create - 创建空 RangeSet")
        void testCreate() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();

            assertThat(rangeSet.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("create - 从范围 Iterable 创建")
        void testCreateFromRanges() {
            List<Range<Integer>> ranges = List.of(
                    Range.closed(1, 5),
                    Range.closed(10, 15));

            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create(ranges);

            assertThat(rangeSet.contains(3)).isTrue();
            assertThat(rangeSet.contains(12)).isTrue();
            assertThat(rangeSet.contains(7)).isFalse();
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("contains - 包含值")
        void testContains() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.contains(1)).isTrue();
            assertThat(rangeSet.contains(5)).isTrue();
            assertThat(rangeSet.contains(10)).isTrue();
            assertThat(rangeSet.contains(0)).isFalse();
            assertThat(rangeSet.contains(11)).isFalse();
        }

        @Test
        @DisplayName("rangeContaining - 包含值的范围")
        void testRangeContaining() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            Range<Integer> range = rangeSet.rangeContaining(5);

            assertThat(range).isNotNull();
            assertThat(range.lowerEndpoint()).isEqualTo(1);
            assertThat(range.upperEndpoint()).isEqualTo(10);
        }

        @Test
        @DisplayName("rangeContaining - 不包含时返回 null")
        void testRangeContainingNotFound() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            Range<Integer> range = rangeSet.rangeContaining(15);

            assertThat(range).isNull();
        }

        @Test
        @DisplayName("encloses - 包围范围")
        void testEncloses() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.encloses(Range.closed(2, 5))).isTrue();
            assertThat(rangeSet.encloses(Range.closed(1, 10))).isTrue();
            assertThat(rangeSet.encloses(Range.closed(0, 5))).isFalse();
        }

        @Test
        @DisplayName("encloses - 空范围总是被包围")
        void testEnclosesEmptyRange() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.encloses(Range.closedOpen(5, 5))).isTrue();
        }

        @Test
        @DisplayName("enclosesAll - 包围所有范围")
        void testEnclosesAll() {
            TreeRangeSet<Integer> rangeSet1 = TreeRangeSet.create();
            rangeSet1.add(Range.closed(1, 20));

            TreeRangeSet<Integer> rangeSet2 = TreeRangeSet.create();
            rangeSet2.add(Range.closed(2, 5));
            rangeSet2.add(Range.closed(10, 15));

            assertThat(rangeSet1.enclosesAll(rangeSet2)).isTrue();
        }

        @Test
        @DisplayName("intersects - 交叉判断")
        void testIntersects() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.intersects(Range.closed(5, 15))).isTrue();
            assertThat(rangeSet.intersects(Range.closed(11, 20))).isFalse();
        }

        @Test
        @DisplayName("span - 跨度")
        void testSpan() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 5));
            rangeSet.add(Range.closed(10, 15));

            Range<Integer> span = rangeSet.span();

            assertThat(span.lowerEndpoint()).isEqualTo(1);
            assertThat(span.upperEndpoint()).isEqualTo(15);
        }

        @Test
        @DisplayName("span - 空集抛异常")
        void testSpanEmpty() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();

            assertThatThrownBy(rangeSet::span)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("asRanges - 获取所有范围")
        void testAsRanges() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 5));
            rangeSet.add(Range.closed(10, 15));

            Set<Range<Integer>> ranges = rangeSet.asRanges();

            assertThat(ranges).hasSize(2);
        }

        @Test
        @DisplayName("asDescendingSetOfRanges - 降序获取范围")
        void testAsDescendingSetOfRanges() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 5));
            rangeSet.add(Range.closed(10, 15));

            Set<Range<Integer>> ranges = rangeSet.asDescendingSetOfRanges();

            assertThat(ranges).hasSize(2);
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationMethodTests {

        @Test
        @DisplayName("add - 添加范围")
        void testAdd() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();

            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.contains(5)).isTrue();
        }

        @Test
        @DisplayName("add - 空范围不添加")
        void testAddEmptyRange() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();

            rangeSet.add(Range.closedOpen(5, 5));

            assertThat(rangeSet.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("add - 范围合并")
        void testAddCoalescing() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));
            rangeSet.add(Range.closed(5, 15));

            Set<Range<Integer>> ranges = rangeSet.asRanges();

            assertThat(ranges).hasSize(1);
            Range<Integer> range = ranges.iterator().next();
            assertThat(range.lowerEndpoint()).isEqualTo(1);
            assertThat(range.upperEndpoint()).isEqualTo(15);
        }

        @Test
        @DisplayName("add - 连接的范围合并")
        void testAddConnectedCoalescing() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 5));
            rangeSet.add(Range.closed(5, 10));

            Set<Range<Integer>> ranges = rangeSet.asRanges();

            assertThat(ranges).hasSize(1);
        }

        @Test
        @DisplayName("remove - 移除范围")
        void testRemove() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 20));

            rangeSet.remove(Range.closed(5, 15));

            assertThat(rangeSet.contains(3)).isTrue();
            assertThat(rangeSet.contains(10)).isFalse();
            assertThat(rangeSet.contains(18)).isTrue();
        }

        @Test
        @DisplayName("remove - 空范围不移除")
        void testRemoveEmptyRange() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            rangeSet.remove(Range.closedOpen(5, 5));

            assertThat(rangeSet.asRanges()).hasSize(1);
        }

        @Test
        @DisplayName("addAll - 添加所有范围")
        void testAddAll() {
            TreeRangeSet<Integer> rangeSet1 = TreeRangeSet.create();
            rangeSet1.add(Range.closed(1, 5));

            TreeRangeSet<Integer> rangeSet2 = TreeRangeSet.create();
            rangeSet2.add(Range.closed(10, 15));

            rangeSet1.addAll(rangeSet2);

            assertThat(rangeSet1.asRanges()).hasSize(2);
        }

        @Test
        @DisplayName("removeAll - 移除所有范围")
        void testRemoveAll() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 20));

            TreeRangeSet<Integer> toRemove = TreeRangeSet.create();
            toRemove.add(Range.closed(5, 10));

            rangeSet.removeAll(toRemove);

            assertThat(rangeSet.contains(3)).isTrue();
            assertThat(rangeSet.contains(7)).isFalse();
            assertThat(rangeSet.contains(15)).isTrue();
        }

        @Test
        @DisplayName("clear - 清空")
        void testClear() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            rangeSet.clear();

            assertThat(rangeSet.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("视图方法测试")
    class ViewMethodTests {

        @Test
        @DisplayName("subRangeSet - 子范围集")
        void testSubRangeSet() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 20));

            RangeSet<Integer> subSet = rangeSet.subRangeSet(Range.closed(5, 15));

            assertThat(subSet.contains(10)).isTrue();
            assertThat(subSet.contains(3)).isFalse();
            assertThat(subSet.contains(18)).isFalse();
        }

        @Test
        @DisplayName("complement - 补集")
        void testComplement() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 5));
            rangeSet.add(Range.closed(10, 15));

            RangeSet<Integer> complement = rangeSet.complement();

            // Complement should contain the gap between the two ranges
            assertThat(complement.isEmpty()).isFalse();
            assertThat(complement.contains(7)).isTrue();
            assertThat(complement.contains(3)).isFalse();
            assertThat(complement.contains(12)).isFalse();
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等")
        void testEquals() {
            TreeRangeSet<Integer> rangeSet1 = TreeRangeSet.create();
            rangeSet1.add(Range.closed(1, 10));

            TreeRangeSet<Integer> rangeSet2 = TreeRangeSet.create();
            rangeSet2.add(Range.closed(1, 10));

            assertThat(rangeSet1).isEqualTo(rangeSet2);
        }

        @Test
        @DisplayName("hashCode - 哈希码")
        void testHashCode() {
            TreeRangeSet<Integer> rangeSet1 = TreeRangeSet.create();
            rangeSet1.add(Range.closed(1, 10));

            TreeRangeSet<Integer> rangeSet2 = TreeRangeSet.create();
            rangeSet2.add(Range.closed(1, 10));

            assertThat(rangeSet1.hashCode()).isEqualTo(rangeSet2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            TreeRangeSet<Integer> rangeSet = TreeRangeSet.create();
            rangeSet.add(Range.closed(1, 10));

            assertThat(rangeSet.toString()).contains("1").contains("10");
        }
    }
}
