package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalDateTimeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DateTimeRange 测试类 (兼容性包装器)
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateTimeRange 测试")
@SuppressWarnings("deprecation")
class DateTimeRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 创建日期时间范围")
        void testOf() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            LocalDateTime end = LocalDateTime.of(2024, 1, 1, 17, 0);
            DateTimeRange range = DateTimeRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("of(start, duration) 从起始和时长创建")
        void testOfWithDuration() {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
            Duration duration = Duration.ofHours(8);
            DateTimeRange range = DateTimeRange.of(start, duration);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.toDuration()).isEqualTo(duration);
        }

        @Test
        @DisplayName("empty() 创建空范围")
        void testEmpty() {
            DateTimeRange range = DateTimeRange.empty();

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("empty(dateTime) 在指定时间创建空范围")
        void testEmptyAtTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            DateTimeRange range = DateTimeRange.empty(dateTime);

            assertThat(range.isEmpty()).isTrue();
            assertThat(range.getStart()).isEqualTo(dateTime);
        }

        @Test
        @DisplayName("from() 从LocalDateTimeRange创建")
        void testFrom() {
            LocalDateTimeRange localRange = LocalDateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            DateTimeRange range = DateTimeRange.from(localRange);

            assertThat(range.getStart()).isEqualTo(localRange.getStart());
            assertThat(range.getEnd()).isEqualTo(localRange.getEnd());
        }
    }

    @Nested
    @DisplayName("基本方法测试")
    class BasicMethodTests {

        @Test
        @DisplayName("toDuration() 计算时长")
        void testToDuration() {
            DateTimeRange range = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.toDuration()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("contains() 包含日期时间")
        void testContains() {
            DateTimeRange range = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 12, 0))).isTrue();
            assertThat(range.contains(LocalDateTime.of(2024, 1, 1, 18, 0))).isFalse();
        }

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.overlaps(range2)).isTrue();
        }

        @Test
        @DisplayName("encloses() 包含另一范围")
        void testEncloses() {
            DateTimeRange outer = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            DateTimeRange inner = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 10, 0),
                    LocalDateTime.of(2024, 1, 1, 16, 0)
            );

            assertThat(outer.encloses(inner)).isTrue();
        }

        @Test
        @DisplayName("abuts() 相邻范围")
        void testAbuts() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1.abuts(range2)).isTrue();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 14, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 12, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<DateTimeRange> intersection = range1.intersection(range2);

            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
            assertThat(intersection.get().getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 14, 0));
        }

        @Test
        @DisplayName("span() 计算跨度")
        void testSpan() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            DateTimeRange span = range1.span(range2);

            assertThat(span.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 9, 0));
            assertThat(span.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 17, 0));
        }

        @Test
        @DisplayName("gap() 计算间隙")
        void testGap() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 12, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 14, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            Optional<DateTimeRange> gap = range1.gap(range2);

            assertThat(gap).isPresent();
            assertThat(gap.get().getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0));
            assertThat(gap.get().getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 1, 14, 0));
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalDateTimeRange() 转换为LocalDateTimeRange")
        void testToLocalDateTimeRange() {
            DateTimeRange range = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            LocalDateTimeRange localRange = range.toLocalDateTimeRange();

            assertThat(localRange.getStart()).isEqualTo(range.getStart());
            assertThat(localRange.getEnd()).isEqualTo(range.getEnd());
        }

        @Test
        @DisplayName("toDateRange() 转换为DateRange")
        void testToDateRange() {
            DateTimeRange range = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 3, 17, 0)
            );

            DateRange dateRange = range.toDateRange();

            assertThat(dateRange).isNotNull();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等范围")
        void testEquals() {
            DateTimeRange range1 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );
            DateTimeRange range2 = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            DateTimeRange range = DateTimeRange.of(
                    LocalDateTime.of(2024, 1, 1, 9, 0),
                    LocalDateTime.of(2024, 1, 1, 17, 0)
            );

            assertThat(range.toString()).contains("2024-01-01T09:00");
        }
    }
}
