package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Interval 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("Interval 测试")
class IntervalTest {

    private final Instant start = Instant.parse("2024-01-01T00:00:00Z");
    private final Instant end = Instant.parse("2024-01-01T10:00:00Z");

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("of(Instant, Instant) 创建Interval")
        void testOfInstantInstant() {
            Interval interval = Interval.of(start, end);
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("of(Instant, Instant) start在end之后抛出异常")
        void testOfInvalid() {
            assertThatThrownBy(() -> Interval.of(end, start))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("of(Instant, Instant) null抛出异常")
        void testOfNull() {
            assertThatThrownBy(() -> Interval.of((Instant) null, end))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Interval.of(start, (Instant) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of(Instant, Duration) 从起始和时长创建")
        void testOfInstantDuration() {
            Interval interval = Interval.of(start, Duration.ofHours(2));
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(start.plus(Duration.ofHours(2)));
        }

        @Test
        @DisplayName("of(Instant, Duration) 负时长抛出异常")
        void testOfNegativeDuration() {
            assertThatThrownBy(() -> Interval.of(start, Duration.ofHours(-1)))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("of(Duration, Instant) 从时长和结束创建")
        void testOfDurationInstant() {
            Interval interval = Interval.of(Duration.ofHours(2), end);
            assertThat(interval.getEnd()).isEqualTo(end);
            assertThat(interval.getStart()).isEqualTo(end.minus(Duration.ofHours(2)));
        }

        @Test
        @DisplayName("of(Duration, Instant) 负时长抛出异常")
        void testOfDurationInstantNegative() {
            assertThatThrownBy(() -> Interval.of(Duration.ofHours(-1), end))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("parse() 解析start/end格式")
        void testParseStartEnd() {
            Interval interval = Interval.parse("2024-01-01T00:00:00Z/2024-01-01T10:00:00Z");
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("parse() 解析start/duration格式")
        void testParseStartDuration() {
            Interval interval = Interval.parse("2024-01-01T00:00:00Z/PT10H");
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("parse() 解析duration/end格式")
        void testParseDurationEnd() {
            Interval interval = Interval.parse("PT10H/2024-01-01T10:00:00Z");
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("parse() 无效格式抛出异常")
        void testParseInvalid() {
            assertThatThrownBy(() -> Interval.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("empty() 创建空Interval")
        void testEmpty() {
            Interval interval = Interval.empty(start);
            assertThat(interval.getStart()).isEqualTo(start);
            assertThat(interval.getEnd()).isEqualTo(start);
            assertThat(interval.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("获取器测试")
    class GetterTests {

        @Test
        @DisplayName("toDuration() 获取时长")
        void testToDuration() {
            Interval interval = Interval.of(start, end);
            assertThat(interval.toDuration()).isEqualTo(Duration.ofHours(10));
        }

        @Test
        @DisplayName("isEmpty() 检查是否为空")
        void testIsEmpty() {
            assertThat(Interval.of(start, end).isEmpty()).isFalse();
            assertThat(Interval.of(start, start).isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("包含方法测试")
    class ContainmentTests {

        @Test
        @DisplayName("contains(Instant) 包含时刻")
        void testContainsInstant() {
            Interval interval = Interval.of(start, end);
            assertThat(interval.contains(start)).isTrue();
            assertThat(interval.contains(start.plus(Duration.ofHours(5)))).isTrue();
            assertThat(interval.contains(end)).isFalse(); // end exclusive
            assertThat(interval.contains(start.minus(Duration.ofHours(1)))).isFalse();
        }

        @Test
        @DisplayName("contains(Instant) null抛出异常")
        void testContainsNull() {
            assertThatThrownBy(() -> Interval.of(start, end).contains(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encloses() 包围另一Interval")
        void testEncloses() {
            Interval outer = Interval.of(start, end);
            Interval inner = Interval.of(start.plus(Duration.ofHours(1)), end.minus(Duration.ofHours(1)));

            assertThat(outer.encloses(inner)).isTrue();
            assertThat(inner.encloses(outer)).isFalse();
        }
    }

    @Nested
    @DisplayName("重叠方法测试")
    class OverlapTests {

        @Test
        @DisplayName("overlaps() 重叠检查")
        void testOverlaps() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);
            Interval i3 = Interval.of(start.plus(Duration.ofHours(6)), end);

            assertThat(i1.overlaps(i2)).isTrue();
            assertThat(i1.overlaps(i3)).isFalse();
        }

        @Test
        @DisplayName("abuts() 相邻检查")
        void testAbuts() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(5)), end);
            Interval i3 = Interval.of(start.plus(Duration.ofHours(6)), end);

            assertThat(i1.abuts(i2)).isTrue();
            assertThat(i1.abuts(i3)).isFalse();
        }

        @Test
        @DisplayName("isBefore(Interval) 在之前")
        void testIsBeforeInterval() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(2)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);

            assertThat(i1.isBefore(i2)).isTrue();
            assertThat(i2.isBefore(i1)).isFalse();
        }

        @Test
        @DisplayName("isBefore(Instant) 在时刻之前")
        void testIsBeforeInstant() {
            Interval interval = Interval.of(start, start.plus(Duration.ofHours(2)));
            assertThat(interval.isBefore(start.plus(Duration.ofHours(3)))).isTrue();
            assertThat(interval.isBefore(start)).isFalse();
        }

        @Test
        @DisplayName("isAfter(Interval) 在之后")
        void testIsAfterInterval() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(2)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);

            assertThat(i2.isAfter(i1)).isTrue();
            assertThat(i1.isAfter(i2)).isFalse();
        }

        @Test
        @DisplayName("isAfter(Instant) 在时刻之后")
        void testIsAfterInstant() {
            Interval interval = Interval.of(start.plus(Duration.ofHours(3)), end);
            assertThat(interval.isAfter(start)).isTrue();
            assertThat(interval.isAfter(end)).isFalse();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);

            Optional<Interval> intersection = i1.intersection(i2);
            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(start.plus(Duration.ofHours(3)));
            assertThat(intersection.get().getEnd()).isEqualTo(start.plus(Duration.ofHours(5)));
        }

        @Test
        @DisplayName("intersection() 无交集返回空")
        void testIntersectionNoOverlap() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(2)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(5)), end);

            Optional<Interval> intersection = i1.intersection(i2);
            assertThat(intersection).isEmpty();
        }

        @Test
        @DisplayName("union() 计算并集")
        void testUnion() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);

            Interval union = i1.union(i2);
            assertThat(union.getStart()).isEqualTo(start);
            assertThat(union.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("union() 相邻Interval可合并")
        void testUnionAbutting() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(5)), end);

            Interval union = i1.union(i2);
            assertThat(union.getStart()).isEqualTo(start);
            assertThat(union.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("union() 不重叠不相邻抛出异常")
        void testUnionGap() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(2)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(5)), end);

            assertThatThrownBy(() -> i1.union(i2))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("gap() 计算间隙")
        void testGap() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(2)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(5)), end);

            Optional<Interval> gap = i1.gap(i2);
            assertThat(gap).isPresent();
            assertThat(gap.get().getStart()).isEqualTo(start.plus(Duration.ofHours(2)));
            assertThat(gap.get().getEnd()).isEqualTo(start.plus(Duration.ofHours(5)));
        }

        @Test
        @DisplayName("gap() 重叠时返回空")
        void testGapOverlapping() {
            Interval i1 = Interval.of(start, start.plus(Duration.ofHours(5)));
            Interval i2 = Interval.of(start.plus(Duration.ofHours(3)), end);

            Optional<Interval> gap = i1.gap(i2);
            assertThat(gap).isEmpty();
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationTests {

        @Test
        @DisplayName("withStart() 设置新起始")
        void testWithStart() {
            Interval interval = Interval.of(start, end);
            Instant newStart = start.minus(Duration.ofHours(1));
            Interval modified = interval.withStart(newStart);
            assertThat(modified.getStart()).isEqualTo(newStart);
            assertThat(modified.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("withEnd() 设置新结束")
        void testWithEnd() {
            Interval interval = Interval.of(start, end);
            Instant newEnd = end.plus(Duration.ofHours(1));
            Interval modified = interval.withEnd(newEnd);
            assertThat(modified.getStart()).isEqualTo(start);
            assertThat(modified.getEnd()).isEqualTo(newEnd);
        }

        @Test
        @DisplayName("expand() 扩展Interval")
        void testExpand() {
            Interval interval = Interval.of(start, end);
            Interval expanded = interval.expand(Duration.ofHours(1));
            assertThat(expanded.getStart()).isEqualTo(start.minus(Duration.ofHours(1)));
            assertThat(expanded.getEnd()).isEqualTo(end.plus(Duration.ofHours(1)));
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalDateRange() 转换为日期范围")
        void testToLocalDateRange() {
            Interval interval = Interval.of(start, end);
            LocalDateRange range = interval.toLocalDateRange(ZoneId.of("UTC"));
            assertThat(range).isNotNull();
        }

        @Test
        @DisplayName("toLocalDateTimeRange() 转换为日期时间范围")
        void testToLocalDateTimeRange() {
            Interval interval = Interval.of(start, end);
            LocalDateTimeRange range = interval.toLocalDateTimeRange(ZoneId.of("UTC"));
            assertThat(range).isNotNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等对象")
        void testEquals() {
            Interval i1 = Interval.of(start, end);
            Interval i2 = Interval.of(start, end);
            Interval i3 = Interval.of(start, start.plus(Duration.ofHours(5)));

            assertThat(i1).isEqualTo(i2);
            assertThat(i1).isNotEqualTo(i3);
            assertThat(i1).isEqualTo(i1);
            assertThat(i1).isNotEqualTo(null);
        }

        @Test
        @DisplayName("hashCode() 相等对象相同哈希码")
        void testHashCode() {
            Interval i1 = Interval.of(start, end);
            Interval i2 = Interval.of(start, end);
            assertThat(i1.hashCode()).isEqualTo(i2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            Interval interval = Interval.of(start, end);
            assertThat(interval.toString()).contains("/");
        }
    }
}
