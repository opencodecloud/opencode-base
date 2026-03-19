package cloud.opencode.base.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Range 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
@DisplayName("Range 测试")
class RangeTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("closed - 闭区间 [a, b]")
        void testClosed() {
            Range<Integer> range = Range.closed(1, 10);

            assertThat(range.contains(1)).isTrue();
            assertThat(range.contains(5)).isTrue();
            assertThat(range.contains(10)).isTrue();
            assertThat(range.contains(0)).isFalse();
            assertThat(range.contains(11)).isFalse();
        }

        @Test
        @DisplayName("open - 开区间 (a, b)")
        void testOpen() {
            Range<Integer> range = Range.open(1, 10);

            assertThat(range.contains(1)).isFalse();
            assertThat(range.contains(5)).isTrue();
            assertThat(range.contains(10)).isFalse();
        }

        @Test
        @DisplayName("closedOpen - 左闭右开 [a, b)")
        void testClosedOpen() {
            Range<Integer> range = Range.closedOpen(1, 10);

            assertThat(range.contains(1)).isTrue();
            assertThat(range.contains(5)).isTrue();
            assertThat(range.contains(10)).isFalse();
        }

        @Test
        @DisplayName("openClosed - 左开右闭 (a, b]")
        void testOpenClosed() {
            Range<Integer> range = Range.openClosed(1, 10);

            assertThat(range.contains(1)).isFalse();
            assertThat(range.contains(5)).isTrue();
            assertThat(range.contains(10)).isTrue();
        }

        @Test
        @DisplayName("atMost - (-∞, b]")
        void testAtMost() {
            Range<Integer> range = Range.atMost(10);

            assertThat(range.contains(Integer.MIN_VALUE)).isTrue();
            assertThat(range.contains(10)).isTrue();
            assertThat(range.contains(11)).isFalse();
        }

        @Test
        @DisplayName("lessThan - (-∞, b)")
        void testLessThan() {
            Range<Integer> range = Range.lessThan(10);

            assertThat(range.contains(9)).isTrue();
            assertThat(range.contains(10)).isFalse();
        }

        @Test
        @DisplayName("atLeast - [a, +∞)")
        void testAtLeast() {
            Range<Integer> range = Range.atLeast(10);

            assertThat(range.contains(10)).isTrue();
            assertThat(range.contains(Integer.MAX_VALUE)).isTrue();
            assertThat(range.contains(9)).isFalse();
        }

        @Test
        @DisplayName("greaterThan - (a, +∞)")
        void testGreaterThan() {
            Range<Integer> range = Range.greaterThan(10);

            assertThat(range.contains(10)).isFalse();
            assertThat(range.contains(11)).isTrue();
        }

        @Test
        @DisplayName("all - (-∞, +∞)")
        void testAll() {
            Range<Integer> range = Range.all();

            assertThat(range.contains(Integer.MIN_VALUE)).isTrue();
            assertThat(range.contains(0)).isTrue();
            assertThat(range.contains(Integer.MAX_VALUE)).isTrue();
        }

        @Test
        @DisplayName("singleton - [a, a]")
        void testSingleton() {
            Range<Integer> range = Range.singleton(5);

            assertThat(range.contains(5)).isTrue();
            assertThat(range.contains(4)).isFalse();
            assertThat(range.contains(6)).isFalse();
        }

        @Test
        @DisplayName("create - 显式边界类型")
        void testCreate() {
            Range<Integer> range = Range.create(Range.BoundType.OPEN, 1, Range.BoundType.CLOSED, 10);

            assertThat(range.contains(1)).isFalse();
            assertThat(range.contains(10)).isTrue();
        }

        @Test
        @DisplayName("无效范围抛异常")
        void testInvalidRange() {
            assertThatThrownBy(() -> Range.closed(10, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("查询方法测试")
    class QueryMethodTests {

        @Test
        @DisplayName("hasLowerBound/hasUpperBound - 边界检查")
        void testHasBounds() {
            Range<Integer> closed = Range.closed(1, 10);
            assertThat(closed.hasLowerBound()).isTrue();
            assertThat(closed.hasUpperBound()).isTrue();

            Range<Integer> atMost = Range.atMost(10);
            assertThat(atMost.hasLowerBound()).isFalse();
            assertThat(atMost.hasUpperBound()).isTrue();

            Range<Integer> atLeast = Range.atLeast(1);
            assertThat(atLeast.hasLowerBound()).isTrue();
            assertThat(atLeast.hasUpperBound()).isFalse();

            Range<Integer> all = Range.all();
            assertThat(all.hasLowerBound()).isFalse();
            assertThat(all.hasUpperBound()).isFalse();
        }

        @Test
        @DisplayName("lowerEndpoint/upperEndpoint - 端点获取")
        void testEndpoints() {
            Range<Integer> range = Range.closed(1, 10);

            assertThat(range.lowerEndpoint()).isEqualTo(1);
            assertThat(range.upperEndpoint()).isEqualTo(10);
        }

        @Test
        @DisplayName("无界范围获取端点抛异常")
        void testEndpointsUnbounded() {
            Range<Integer> atMost = Range.atMost(10);

            assertThatThrownBy(atMost::lowerEndpoint)
                    .isInstanceOf(IllegalStateException.class);

            Range<Integer> atLeast = Range.atLeast(1);

            assertThatThrownBy(atLeast::upperEndpoint)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("isLowerBoundClosed/isUpperBoundClosed - 闭合检查")
        void testBoundClosed() {
            Range<Integer> closed = Range.closed(1, 10);
            assertThat(closed.isLowerBoundClosed()).isTrue();
            assertThat(closed.isUpperBoundClosed()).isTrue();

            Range<Integer> open = Range.open(1, 10);
            assertThat(open.isLowerBoundClosed()).isFalse();
            assertThat(open.isUpperBoundClosed()).isFalse();
        }

        @Test
        @DisplayName("lowerBoundType/upperBoundType - 边界类型")
        void testBoundTypes() {
            Range<Integer> closedOpen = Range.closedOpen(1, 10);

            assertThat(closedOpen.lowerBoundType()).isEqualTo(Range.BoundType.CLOSED);
            assertThat(closedOpen.upperBoundType()).isEqualTo(Range.BoundType.OPEN);
        }

        @Test
        @DisplayName("无界范围获取边界类型抛异常")
        void testBoundTypesUnbounded() {
            Range<Integer> atMost = Range.atMost(10);

            assertThatThrownBy(atMost::lowerBoundType)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("isEmpty - 空范围检查")
        void testIsEmpty() {
            Range<Integer> empty = Range.closedOpen(5, 5);
            assertThat(empty.isEmpty()).isTrue();

            Range<Integer> notEmpty = Range.closed(5, 5);
            assertThat(notEmpty.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("contains - null 抛异常")
        void testContainsNull() {
            Range<Integer> range = Range.closed(1, 10);

            assertThatThrownBy(() -> range.contains(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("范围操作测试")
    class RangeOperationTests {

        @Test
        @DisplayName("encloses - 包含范围")
        void testEncloses() {
            Range<Integer> outer = Range.closed(1, 10);
            Range<Integer> inner = Range.closed(3, 7);
            Range<Integer> partial = Range.closed(5, 15);

            assertThat(outer.encloses(inner)).isTrue();
            assertThat(inner.encloses(outer)).isFalse();
            assertThat(outer.encloses(partial)).isFalse();
        }

        @Test
        @DisplayName("isConnected - 连接检查")
        void testIsConnected() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(3, 8);
            Range<Integer> range3 = Range.closed(10, 15);

            assertThat(range1.isConnected(range2)).isTrue();
            assertThat(range1.isConnected(range3)).isFalse();
        }

        @Test
        @DisplayName("intersection - 交集")
        void testIntersection() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(5, 15);

            Range<Integer> intersection = range1.intersection(range2);

            assertThat(intersection.lowerEndpoint()).isEqualTo(5);
            assertThat(intersection.upperEndpoint()).isEqualTo(10);
        }

        @Test
        @DisplayName("intersection - 不连接抛异常")
        void testIntersectionNotConnected() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(10, 15);

            assertThatThrownBy(() -> range1.intersection(range2))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("span - 跨度")
        void testSpan() {
            Range<Integer> range1 = Range.closed(1, 5);
            Range<Integer> range2 = Range.closed(10, 15);

            Range<Integer> span = range1.span(range2);

            assertThat(span.lowerEndpoint()).isEqualTo(1);
            assertThat(span.upperEndpoint()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Object 方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals - 相等比较")
        void testEquals() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(1, 10);
            Range<Integer> range3 = Range.closed(1, 11);

            assertThat(range1).isEqualTo(range2);
            assertThat(range1).isNotEqualTo(range3);
        }

        @Test
        @DisplayName("equals - 同一引用")
        void testEqualsSameReference() {
            Range<Integer> range = Range.closed(1, 10);

            assertThat(range.equals(range)).isTrue();
        }

        @Test
        @DisplayName("hashCode - 哈希码一致性")
        void testHashCode() {
            Range<Integer> range1 = Range.closed(1, 10);
            Range<Integer> range2 = Range.closed(1, 10);

            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("toString - 字符串表示")
        void testToString() {
            assertThat(Range.closed(1, 10).toString()).isEqualTo("[1..10]");
            assertThat(Range.open(1, 10).toString()).isEqualTo("(1..10)");
            assertThat(Range.closedOpen(1, 10).toString()).isEqualTo("[1..10)");
            assertThat(Range.openClosed(1, 10).toString()).isEqualTo("(1..10]");
        }
    }

    @Nested
    @DisplayName("字符串类型测试")
    class StringRangeTests {

        @Test
        @DisplayName("字符串范围")
        void testStringRange() {
            Range<String> range = Range.closed("a", "z");

            assertThat(range.contains("m")).isTrue();
            assertThat(range.contains("A")).isFalse();  // "A" < "a"
        }
    }
}
