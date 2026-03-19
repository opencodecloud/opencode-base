package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalDateRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * DateRange 测试类 (兼容性包装器)
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("DateRange 测试")
@SuppressWarnings("deprecation")
class DateRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 创建日期范围")
        void testOf() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);
            DateRange range = DateRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("ofExclusive() 创建不包含结束的范围")
        void testOfExclusive() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate endExclusive = LocalDate.of(2024, 1, 10);
            DateRange range = DateRange.ofExclusive(start, endExclusive);

            assertThat(range.getEnd()).isEqualTo(endExclusive.minusDays(1));
        }

        @Test
        @DisplayName("empty() 创建空范围")
        void testEmpty() {
            DateRange range = DateRange.empty();

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("from() 从LocalDateRange创建")
        void testFrom() {
            LocalDateRange localRange = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            DateRange range = DateRange.from(localRange);

            assertThat(range.getStart()).isEqualTo(localRange.getStart());
            assertThat(range.getEnd()).isEqualTo(localRange.getEnd());
        }
    }

    @Nested
    @DisplayName("基本方法测试")
    class BasicMethodTests {

        @Test
        @DisplayName("lengthInDays() 计算天数")
        void testLengthInDays() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.lengthInDays()).isEqualTo(31);
        }

        @Test
        @DisplayName("contains() 包含日期")
        void testContains() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.contains(LocalDate.of(2024, 1, 15))).isTrue();
            assertThat(range.contains(LocalDate.of(2024, 2, 1))).isFalse();
        }

        @Test
        @DisplayName("encloses() 包含另一范围")
        void testEncloses() {
            DateRange outer = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            DateRange inner = DateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 20)
            );

            assertThat(outer.encloses(inner)).isTrue();
        }

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            DateRange range1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 15)
            );
            DateRange range2 = DateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 25)
            );

            assertThat(range1.overlaps(range2)).isTrue();
        }

        @Test
        @DisplayName("isConnected() 相连范围")
        void testIsConnected() {
            DateRange range1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            DateRange range2 = DateRange.of(
                    LocalDate.of(2024, 1, 11),
                    LocalDate.of(2024, 1, 20)
            );

            assertThat(range1.isConnected(range2)).isTrue();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            DateRange range1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 20)
            );
            DateRange range2 = DateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 31)
            );

            Optional<DateRange> intersection = range1.intersection(range2);

            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(LocalDate.of(2024, 1, 10));
            assertThat(intersection.get().getEnd()).isEqualTo(LocalDate.of(2024, 1, 20));
        }

        @Test
        @DisplayName("span() 计算跨度")
        void testSpan() {
            DateRange range1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            DateRange range2 = DateRange.of(
                    LocalDate.of(2024, 1, 20),
                    LocalDate.of(2024, 1, 31)
            );

            DateRange span = range1.span(range2);

            assertThat(span.getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(span.getEnd()).isEqualTo(LocalDate.of(2024, 1, 31));
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("iterator() 遍历")
        void testIterator() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 5)
            );

            int count = 0;
            for (LocalDate date : range) {
                count++;
            }

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("stream() 流式处理")
        void testStream() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );

            assertThat(range.stream().count()).isEqualTo(10);
        }

        @Test
        @DisplayName("stream(Period) 步长遍历")
        void testStreamWithStep() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            List<LocalDate> dates = range.stream(Period.ofWeeks(1)).toList();

            assertThat(dates).hasSize(5);
        }

        @Test
        @DisplayName("toList() 转换为列表")
        void testToList() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 5)
            );

            assertThat(range.toList()).hasSize(5);
        }

        @Test
        @DisplayName("splitByWeek() 按周分割")
        void testSplitByWeek() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 21)
            );

            List<DateRange> weeks = range.splitByWeek();

            assertThat(weeks).isNotEmpty();
        }

        @Test
        @DisplayName("splitByMonth() 按月分割")
        void testSplitByMonth() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 3, 15)
            );

            List<DateRange> months = range.splitByMonth();

            assertThat(months).hasSize(3);
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalDateRange() 转换为LocalDateRange")
        void testToLocalDateRange() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            LocalDateRange localRange = range.toLocalDateRange();

            assertThat(localRange.getStart()).isEqualTo(range.getStart());
            assertThat(localRange.getEnd()).isEqualTo(range.getEnd());
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等范围")
        void testEquals() {
            DateRange range1 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            DateRange range2 = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            DateRange range = DateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.toString()).isEqualTo("2024-01-01/2024-01-31");
        }
    }
}
