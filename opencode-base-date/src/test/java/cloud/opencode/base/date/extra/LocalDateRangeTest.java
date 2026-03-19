package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalDateRange 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("LocalDateRange 测试")
class LocalDateRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 创建日期范围")
        void testOf() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 1, 31);
            LocalDateRange range = LocalDateRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
            assertThat(range.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("of() 起始等于结束")
        void testOfSameDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDateRange range = LocalDateRange.of(date, date);

            assertThat(range.getStart()).isEqualTo(date);
            assertThat(range.getEnd()).isEqualTo(date);
            assertThat(range.lengthInDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("of() 起始在结束之后抛出异常")
        void testOfStartAfterEndThrows() {
            LocalDate start = LocalDate.of(2024, 1, 31);
            LocalDate end = LocalDate.of(2024, 1, 1);

            assertThatThrownBy(() -> LocalDateRange.of(start, end))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("ofExclusive() 创建不包含结束的范围")
        void testOfExclusive() {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate endExclusive = LocalDate.of(2024, 1, 10);
            LocalDateRange range = LocalDateRange.ofExclusive(start, endExclusive);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(endExclusive.minusDays(1));
            assertThat(range.lengthInDays()).isEqualTo(9);
        }

        @Test
        @DisplayName("ofExclusive() 相同日期返回空")
        void testOfExclusiveSameDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDateRange range = LocalDateRange.ofExclusive(date, date);

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("empty() 创建空范围")
        void testEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.isEmpty()).isTrue();
            assertThat(range.getStart()).isNull();
            assertThat(range.getEnd()).isNull();
            assertThat(range.lengthInDays()).isEqualTo(0);
        }

        @Test
        @DisplayName("parse() 解析字符串")
        void testParse() {
            LocalDateRange range = LocalDateRange.parse("2024-01-01/2024-01-31");

            assertThat(range.getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(range.getEnd()).isEqualTo(LocalDate.of(2024, 1, 31));
        }

        @Test
        @DisplayName("parse() 无效格式抛出异常")
        void testParseInvalidThrows() {
            assertThatThrownBy(() -> LocalDateRange.parse("invalid"))
                    .isInstanceOf(OpenDateException.class);
        }

        @Test
        @DisplayName("parse() 无效日期抛出异常")
        void testParseInvalidDateThrows() {
            assertThatThrownBy(() -> LocalDateRange.parse("2024-13-01/2024-01-31"))
                    .isInstanceOf(OpenDateException.class);
        }
    }

    @Nested
    @DisplayName("长度计算测试")
    class LengthTests {

        @Test
        @DisplayName("lengthInDays() 计算天数")
        void testLengthInDays() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.lengthInDays()).isEqualTo(31);
        }

        @Test
        @DisplayName("lengthInDays() 单天")
        void testLengthInDaysSingleDay() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            LocalDateRange range = LocalDateRange.of(date, date);

            assertThat(range.lengthInDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("lengthInDays() 空范围返回0")
        void testLengthInDaysEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.lengthInDays()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("包含测试")
    class ContainmentTests {

        @Test
        @DisplayName("contains() 包含日期")
        void testContains() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.contains(LocalDate.of(2024, 1, 15))).isTrue();
            assertThat(range.contains(LocalDate.of(2024, 1, 1))).isTrue();
            assertThat(range.contains(LocalDate.of(2024, 1, 31))).isTrue();
        }

        @Test
        @DisplayName("contains() 不包含日期")
        void testContainsOutside() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.contains(LocalDate.of(2023, 12, 31))).isFalse();
            assertThat(range.contains(LocalDate.of(2024, 2, 1))).isFalse();
        }

        @Test
        @DisplayName("contains() 空范围不包含任何日期")
        void testContainsEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.contains(LocalDate.of(2024, 1, 15))).isFalse();
        }

        @Test
        @DisplayName("contains() null返回false")
        void testContainsNull() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.contains(null)).isFalse();
        }

        @Test
        @DisplayName("encloses() 完全包含另一范围")
        void testEncloses() {
            LocalDateRange outer = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            LocalDateRange inner = LocalDateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 20)
            );

            assertThat(outer.encloses(inner)).isTrue();
            assertThat(inner.encloses(outer)).isFalse();
        }

        @Test
        @DisplayName("encloses() 相同范围")
        void testEnclosesSame() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.encloses(range)).isTrue();
        }
    }

    @Nested
    @DisplayName("重叠测试")
    class OverlapTests {

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 15)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 25)
            );

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("overlaps() 不重叠范围")
        void testOverlapsNoOverlap() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 20),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range1.overlaps(range2)).isFalse();
        }

        @Test
        @DisplayName("overlaps() 相邻范围")
        void testOverlapsAdjacent() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 20)
            );

            assertThat(range1.overlaps(range2)).isTrue();
        }

        @Test
        @DisplayName("isConnected() 相连范围")
        void testIsConnected() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 11),
                    LocalDate.of(2024, 1, 20)
            );

            assertThat(range1.isConnected(range2)).isTrue();
        }

        @Test
        @DisplayName("isConnected() 不相连范围")
        void testIsConnectedNotConnected() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 20),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range1.isConnected(range2)).isFalse();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 20)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 10),
                    LocalDate.of(2024, 1, 31)
            );

            Optional<LocalDateRange> intersection = range1.intersection(range2);

            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(LocalDate.of(2024, 1, 10));
            assertThat(intersection.get().getEnd()).isEqualTo(LocalDate.of(2024, 1, 20));
        }

        @Test
        @DisplayName("intersection() 无交集返回空")
        void testIntersectionNoOverlap() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 20),
                    LocalDate.of(2024, 1, 31)
            );

            Optional<LocalDateRange> intersection = range1.intersection(range2);

            assertThat(intersection).isEmpty();
        }

        @Test
        @DisplayName("span() 计算跨度")
        void testSpan() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 20),
                    LocalDate.of(2024, 1, 31)
            );

            LocalDateRange span = range1.span(range2);

            assertThat(span.getStart()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(span.getEnd()).isEqualTo(LocalDate.of(2024, 1, 31));
        }

        @Test
        @DisplayName("span() 空范围")
        void testSpanWithEmpty() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            LocalDateRange empty = LocalDateRange.empty();

            assertThat(range.span(empty)).isEqualTo(range);
            assertThat(empty.span(range)).isEqualTo(range);
        }
    }

    @Nested
    @DisplayName("迭代测试")
    class IterationTests {

        @Test
        @DisplayName("iterator() 遍历日期")
        void testIterator() {
            LocalDateRange range = LocalDateRange.of(
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
        @DisplayName("iterator() 空范围")
        void testIteratorEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.iterator().hasNext()).isFalse();
        }

        @Test
        @DisplayName("iterator() next()超出范围抛出异常")
        void testIteratorNextThrows() {
            LocalDateRange range = LocalDateRange.empty();
            var iterator = range.iterator();

            assertThatThrownBy(iterator::next)
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        @DisplayName("stream() 流式处理")
        void testStream() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 10)
            );

            long count = range.stream().count();

            assertThat(count).isEqualTo(10);
        }

        @Test
        @DisplayName("stream() 空范围")
        void testStreamEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.stream().count()).isEqualTo(0);
        }

        @Test
        @DisplayName("stream(Period) 步长遍历")
        void testStreamWithStep() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            List<LocalDate> dates = range.stream(Period.ofWeeks(1)).toList();

            assertThat(dates).hasSize(5); // 1, 8, 15, 22, 29
        }

        @Test
        @DisplayName("stream(Period) 零或负步长返回空")
        void testStreamWithZeroStep() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.stream(Period.ZERO).count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toList() 转换为列表")
        void testToList() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 5)
            );

            List<LocalDate> list = range.toList();

            assertThat(list).hasSize(5);
            assertThat(list.get(0)).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(list.get(4)).isEqualTo(LocalDate.of(2024, 1, 5));
        }

        @Test
        @DisplayName("toList() 空范围返回空列表")
        void testToListEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.toList()).isEmpty();
        }

        @Test
        @DisplayName("splitByWeek() 按周分割")
        void testSplitByWeek() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1), // Monday
                    LocalDate.of(2024, 1, 21) // Sunday
            );

            List<LocalDateRange> weeks = range.splitByWeek();

            assertThat(weeks).isNotEmpty();
        }

        @Test
        @DisplayName("splitByWeek() 空范围")
        void testSplitByWeekEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.splitByWeek()).isEmpty();
        }

        @Test
        @DisplayName("splitByMonth() 按月分割")
        void testSplitByMonth() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 3, 15)
            );

            List<LocalDateRange> months = range.splitByMonth();

            assertThat(months).hasSize(3);
        }

        @Test
        @DisplayName("splitByMonth() 空范围")
        void testSplitByMonthEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.splitByMonth()).isEmpty();
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等范围")
        void testEquals() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("equals() 不相等范围")
        void testEqualsNotEqual() {
            LocalDateRange range1 = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );
            LocalDateRange range2 = LocalDateRange.of(
                    LocalDate.of(2024, 2, 1),
                    LocalDate.of(2024, 2, 28)
            );

            assertThat(range1).isNotEqualTo(range2);
        }

        @Test
        @DisplayName("equals() 空范围相等")
        void testEqualsEmpty() {
            LocalDateRange empty1 = LocalDateRange.empty();
            LocalDateRange empty2 = LocalDateRange.empty();

            assertThat(empty1).isEqualTo(empty2);
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThat(range.toString()).isEqualTo("2024-01-01/2024-01-31");
        }

        @Test
        @DisplayName("toString() 空范围")
        void testToStringEmpty() {
            LocalDateRange range = LocalDateRange.empty();

            assertThat(range.toString()).isEqualTo("LocalDateRange[]");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of() null start抛出异常")
        void testOfNullStart() {
            assertThatThrownBy(() -> LocalDateRange.of(null, LocalDate.now()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() null end抛出异常")
        void testOfNullEnd() {
            assertThatThrownBy(() -> LocalDateRange.of(LocalDate.now(), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parse() null text抛出异常")
        void testParseNullThrows() {
            assertThatThrownBy(() -> LocalDateRange.parse(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("span() null other抛出异常")
        void testSpanNullThrows() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThatThrownBy(() -> range.span(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("stream(Period) null step抛出异常")
        void testStreamNullStepThrows() {
            LocalDateRange range = LocalDateRange.of(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 31)
            );

            assertThatThrownBy(() -> range.stream(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
