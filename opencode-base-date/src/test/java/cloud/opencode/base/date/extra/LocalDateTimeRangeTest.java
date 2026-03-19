package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalDateTimeRange 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("LocalDateTimeRange 测试")
class LocalDateTimeRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 从日期时间创建")
        void testOf() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 17, 0);
            LocalDateTimeRange range = LocalDateTimeRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
            assertThat(range.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("of() 起始等于结束")
        void testOfSameDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTimeRange range = LocalDateTimeRange.of(dateTime, dateTime);

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("of() 起始在结束之后抛出异常")
        void testOfStartAfterEndThrows() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 17, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 9, 0);

            assertThatThrownBy(() -> LocalDateTimeRange.of(start, end))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("of(start, duration) 从起始和时长创建")
        void testOfWithDuration() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            Duration duration = Duration.ofHours(8);
            LocalDateTimeRange range = LocalDateTimeRange.of(start, duration);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(start.plus(duration));
        }

        @Test
        @DisplayName("of(start, duration) 负时长抛出异常")
        void testOfWithNegativeDurationThrows() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            Duration duration = Duration.ofHours(-1);

            assertThatThrownBy(() -> LocalDateTimeRange.of(start, duration))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("ofDay() 创建整天范围")
        void testOfDay() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDateTimeRange range = LocalDateTimeRange.ofDay(date);

            assertThat(range.getStart()).isEqualTo(date.atStartOfDay());
            assertThat(range.getEnd()).isEqualTo(date.plusDays(1).atStartOfDay());
        }

        @Test
        @DisplayName("empty() 创建空范围")
        void testEmpty() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTimeRange range = LocalDateTimeRange.empty(dateTime);

            assertThat(range.isEmpty()).isTrue();
            assertThat(range.getStart()).isEqualTo(dateTime);
            assertThat(range.getEnd()).isEqualTo(dateTime);
        }

        @Test
        @DisplayName("parse() 解析字符串")
        void testParse() {
            LocalDateTimeRange range = LocalDateTimeRange.parse("2024-01-01T09:00:00/2024-01-01T17:00:00");

            assertThat(range.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0, 0));
            assertThat(range.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 17, 0, 0));
        }

        @Test
        @DisplayName("parse() 无效格式抛出异常")
        void testParseInvalidThrows() {
            assertThatThrownBy(() -> LocalDateTimeRange.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("时长计算测试")
    class DurationTests {

        @Test
        @DisplayName("toDuration() 计算时长")
        void testToDuration() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.toDuration()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("toHours() 计算小时数")
        void testToHours() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 30)
            );

            assertThat(range.toHours()).isEqualTo(8);
        }

        @Test
        @DisplayName("toMinutes() 计算分钟数")
        void testToMinutes() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 10, 30)
            );

            assertThat(range.toMinutes()).isEqualTo(90);
        }

        @Test
        @DisplayName("toSeconds() 计算秒数")
        void testToSeconds() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 9, 1, 30)
            );

            assertThat(range.toSeconds()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("包含测试")
    class ContainmentTests {

        @Test
        @DisplayName("contains() 包含日期时间")
        void testContains() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 12, 0))).isTrue();
            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 9, 0))).isTrue();
        }

        @Test
        @DisplayName("contains() 不包含结束时间")
        void testContainsEndExclusive() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 17, 0))).isFalse();
        }

        @Test
        @DisplayName("contains() 不包含范围外时间")
        void testContainsOutside() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 8, 0))).isFalse();
            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 18, 0))).isFalse();
        }

        @Test
        @DisplayName("encloses() 完全包含另一范围")
        void testEncloses() {
            LocalDateTimeRange outer = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            LocalDateTimeRange inner = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 16, 0)
            );

            assertThat(outer.encloses(inner)).isTrue();
            assertThat(inner.encloses(outer)).isFalse();
        }
    }

    @Nested
    @DisplayName("重叠测试")
    class OverlapTests {

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("overlaps() 不重叠范围")
        void testOverlapsNoOverlap() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.overlaps(range2)).isFalse();
        }

        @Test
        @DisplayName("abuts() 相邻范围")
        void testAbuts() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.abuts(range2)).isTrue();
            assertThat(range2.abuts(range1)).isTrue();
        }

        @Test
        @DisplayName("isBefore() 在之前")
        void testIsBefore() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.isBefore(range2)).isTrue();
            assertThat(range2.isBefore(range1)).isFalse();
        }

        @Test
        @DisplayName("isAfter() 在之后")
        void testIsAfter() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );

            assertThat(range1.isAfter(range2)).isTrue();
            assertThat(range2.isAfter(range1)).isFalse();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<LocalDateTimeRange> intersection = range1.intersection(range2);

            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
            assertThat(intersection.get().getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 14, 0));
        }

        @Test
        @DisplayName("intersection() 无交集返回空")
        void testIntersectionNoOverlap() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<LocalDateTimeRange> intersection = range1.intersection(range2);

            assertThat(intersection).isEmpty();
        }

        @Test
        @DisplayName("union() 计算并集")
        void testUnion() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateTimeRange union = range1.union(range2);

            assertThat(union.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0));
            assertThat(union.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 17, 0));
        }

        @Test
        @DisplayName("union() 相邻范围")
        void testUnionAbutting() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateTimeRange union = range1.union(range2);

            assertThat(union.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0));
            assertThat(union.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 17, 0));
        }

        @Test
        @DisplayName("union() 不重叠且不相邻抛出异常")
        void testUnionNotConnectedThrows() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThatThrownBy(() -> range1.union(range2))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("gap() 计算间隙")
        void testGap() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<LocalDateTimeRange> gap = range1.gap(range2);

            assertThat(gap).isPresent();
            assertThat(gap.get().getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
            assertThat(gap.get().getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 14, 0));
        }

        @Test
        @DisplayName("gap() 重叠范围无间隙")
        void testGapOverlapping() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<LocalDateTimeRange> gap = range1.gap(range2);

            assertThat(gap).isEmpty();
        }
    }

    @Nested
    @DisplayName("修改方法测试")
    class ModificationTests {

        @Test
        @DisplayName("withStart() 修改起始")
        void testWithStart() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateTimeRange modified = range.withStart(LocalDateTime.of(2024, 1, 1, 8, 0));

            assertThat(modified.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 8, 0));
            assertThat(modified.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 17, 0));
        }

        @Test
        @DisplayName("withEnd() 修改结束")
        void testWithEnd() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateTimeRange modified = range.withEnd(LocalDateTime.of(2024, 1, 1, 18, 0));

            assertThat(modified.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0));
            assertThat(modified.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 18, 0));
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalDateRange() 转换为日期范围")
        void testToLocalDateRange() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 3, 17, 0)
            );

            LocalDateRange dateRange = range.toLocalDateRange();

            assertThat(dateRange.getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(dateRange.getEnd()).isEqualTo(LocalDate.of(2024, 1, 3));
        }

        @Test
        @DisplayName("toLocalDateRange() 同一天不同时间")
        void testToLocalDateRangeSameDay() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateRange dateRange = range.toLocalDateRange();

            assertThat(dateRange.getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(dateRange.getEnd()).isEqualTo(LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("toInterval() 转换为Interval")
        void testToInterval() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Interval interval = range.toInterval(ZoneId.systemDefault());

            assertThat(interval).isNotNull();
            assertThat(interval.toDuration()).isEqualTo(Duration.ofHours(8));
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等范围")
        void testEquals() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("equals() 不相等范围")
        void testEqualsNotEqual() {
            LocalDateTimeRange range1 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            LocalDateTimeRange range2 = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 18, 0)
            );

            assertThat(range1).isNotEqualTo(range2);
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.toString()).contains("2024-01-01T09:00");
            assertThat(range.toString()).contains("2024-01-01T17:00");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of() null start抛出异常")
        void testOfNullStart() {
            assertThatThrownBy(() -> LocalDateTimeRange.of(null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() null end抛出异常")
        void testOfNullEnd() {
            assertThatThrownBy(() -> LocalDateTimeRange.of(LocalDateTime.now(), (LocalDateTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of(start, duration) null duration抛出异常")
        void testOfNullDuration() {
            assertThatThrownBy(() -> LocalDateTimeRange.of(LocalDateTime.now(), (Duration) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("contains() null抛出异常")
        void testContainsNull() {
            LocalDateTimeRange range = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThatThrownBy(() -> range.contains(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
