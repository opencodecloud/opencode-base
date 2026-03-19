package cloud.opencode.base.date.extra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalTimeRange 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("LocalTimeRange 测试")
class LocalTimeRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 创建时间范围")
        void testOf() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 0);
            LocalTimeRange range = LocalTimeRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
            assertThat(range.crossesMidnight()).isFalse();
        }

        @Test
        @DisplayName("of() 跨越午夜")
        void testOfCrossesMidnight() {
            LocalTime start = LocalTime.of(22, 0);
            LocalTime end = LocalTime.of(6, 0);
            LocalTimeRange range = LocalTimeRange.of(start, end);

            assertThat(range.crossesMidnight()).isTrue();
        }

        @Test
        @DisplayName("of() 相同时间被视为跨午夜")
        void testOfSameTime() {
            LocalTime time = LocalTime.of(12, 0);
            LocalTimeRange range = LocalTimeRange.of(time, time);

            assertThat(range.crossesMidnight()).isTrue();
        }

        @Test
        @DisplayName("ofHours() 从小时创建")
        void testOfHours() {
            LocalTimeRange range = LocalTimeRange.ofHours(9, 17);

            assertThat(range.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("ofDuration() 从起始和时长创建")
        void testOfDuration() {
            LocalTime start = LocalTime.of(9, 0);
            Duration duration = Duration.ofHours(8);
            LocalTimeRange range = LocalTimeRange.ofDuration(start, duration);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("ofDuration() 负时长抛出异常")
        void testOfDurationNegativeThrows() {
            assertThatThrownBy(() -> LocalTimeRange.ofDuration(LocalTime.of(9, 0), Duration.ofHours(-1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("parse() 解析字符串")
        void testParse() {
            LocalTimeRange range = LocalTimeRange.parse("09:00-17:00");

            assertThat(range.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("parse() 使用波浪线分隔")
        void testParseWithTilde() {
            LocalTimeRange range = LocalTimeRange.parse("09:00~17:00");

            assertThat(range.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("parse() 无效格式抛出异常")
        void testParseInvalidThrows() {
            assertThatThrownBy(() -> LocalTimeRange.parse("invalid"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("ALL_DAY 全天范围")
        void testAllDay() {
            assertThat(LocalTimeRange.ALL_DAY.getStart()).isEqualTo(LocalTime.MIN);
            assertThat(LocalTimeRange.ALL_DAY.getEnd()).isEqualTo(LocalTime.MAX);
        }

        @Test
        @DisplayName("BUSINESS_HOURS 工作时间")
        void testBusinessHours() {
            assertThat(LocalTimeRange.BUSINESS_HOURS.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(LocalTimeRange.BUSINESS_HOURS.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("MORNING 上午")
        void testMorning() {
            assertThat(LocalTimeRange.MORNING.getStart()).isEqualTo(LocalTime.of(6, 0));
            assertThat(LocalTimeRange.MORNING.getEnd()).isEqualTo(LocalTime.NOON);
        }

        @Test
        @DisplayName("AFTERNOON 下午")
        void testAfternoon() {
            assertThat(LocalTimeRange.AFTERNOON.getStart()).isEqualTo(LocalTime.NOON);
            assertThat(LocalTimeRange.AFTERNOON.getEnd()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("EVENING 傍晚")
        void testEvening() {
            assertThat(LocalTimeRange.EVENING.getStart()).isEqualTo(LocalTime.of(18, 0));
            assertThat(LocalTimeRange.EVENING.getEnd()).isEqualTo(LocalTime.of(22, 0));
        }
    }

    @Nested
    @DisplayName("包含测试")
    class ContainmentTests {

        @Test
        @DisplayName("contains() 包含时间")
        void testContains() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.contains(LocalTime.of(12, 0))).isTrue();
            assertThat(range.contains(LocalTime.of(9, 0))).isTrue();
        }

        @Test
        @DisplayName("contains() 不包含结束时间")
        void testContainsEndExclusive() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.contains(LocalTime.of(17, 0))).isFalse();
        }

        @Test
        @DisplayName("contains() 跨午夜范围")
        void testContainsCrossesMidnight() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(22, 0),
                    LocalTime.of(6, 0)
            );

            assertThat(range.contains(LocalTime.of(23, 0))).isTrue();
            assertThat(range.contains(LocalTime.of(1, 0))).isTrue();
            assertThat(range.contains(LocalTime.of(12, 0))).isFalse();
        }

        @Test
        @DisplayName("contains() 包含另一范围")
        void testContainsRange() {
            LocalTimeRange outer = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            LocalTimeRange inner = LocalTimeRange.of(
                    LocalTime.of(10, 0),
                    LocalTime.of(16, 0)
            );

            assertThat(outer.contains(inner)).isTrue();
            assertThat(inner.contains(outer)).isFalse();
        }
    }

    @Nested
    @DisplayName("重叠测试")
    class OverlapTests {

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(14, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range1.overlaps(range2)).isTrue();
            assertThat(range2.overlaps(range1)).isTrue();
        }

        @Test
        @DisplayName("overlaps() 不重叠范围")
        void testOverlapsNoOverlap() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range1.overlaps(range2)).isFalse();
        }

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(14, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange intersection = range1.intersection(range2);

            assertThat(intersection).isNotNull();
            assertThat(intersection.getStart()).isEqualTo(LocalTime.of(12, 0));
            assertThat(intersection.getEnd()).isEqualTo(LocalTime.of(14, 0));
        }

        @Test
        @DisplayName("intersection() 无交集返回null")
        void testIntersectionNoOverlap() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange intersection = range1.intersection(range2);

            assertThat(intersection).isNull();
        }
    }

    @Nested
    @DisplayName("时长测试")
    class DurationTests {

        @Test
        @DisplayName("getDuration() 计算时长")
        void testGetDuration() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.getDuration()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("getDuration() 跨午夜")
        void testGetDurationCrossesMidnight() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(22, 0),
                    LocalTime.of(6, 0)
            );

            assertThat(range.getDuration()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("getHours() 获取小时数")
        void testGetHours() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 30)
            );

            assertThat(range.getHours()).isEqualTo(8);
        }

        @Test
        @DisplayName("getMinutes() 获取分钟数")
        void testGetMinutes() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 30)
            );

            assertThat(range.getMinutes()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("转换方法测试")
    class TransformationTests {

        @Test
        @DisplayName("withStart() 修改起始时间")
        void testWithStart() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange modified = range.withStart(LocalTime.of(8, 0));

            assertThat(modified.getStart()).isEqualTo(LocalTime.of(8, 0));
            assertThat(modified.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("withEnd() 修改结束时间")
        void testWithEnd() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange modified = range.withEnd(LocalTime.of(18, 0));

            assertThat(modified.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(modified.getEnd()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("shift() 移动范围")
        void testShift() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange shifted = range.shift(Duration.ofHours(1));

            assertThat(shifted.getStart()).isEqualTo(LocalTime.of(10, 0));
            assertThat(shifted.getEnd()).isEqualTo(LocalTime.of(18, 0));
        }

        @Test
        @DisplayName("expand() 扩展范围")
        void testExpand() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(10, 0),
                    LocalTime.of(16, 0)
            );

            LocalTimeRange expanded = range.expand(Duration.ofHours(1));

            assertThat(expanded.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(expanded.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }
    }

    @Nested
    @DisplayName("格式化测试")
    class FormattingTests {

        @Test
        @DisplayName("format() 使用格式化器")
        void testFormat() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            String result = range.format(DateTimeFormatter.ofPattern("HH:mm"));

            assertThat(result).isEqualTo("09:00 - 17:00");
        }

        @Test
        @DisplayName("formatCompact() 紧凑格式")
        void testFormatCompact() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 30)
            );

            String result = range.formatCompact();

            assertThat(result).isEqualTo("09:00-17:30");
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("equals() 相等范围")
        void testEquals() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("equals() 不相等范围")
        void testEqualsNotEqual() {
            LocalTimeRange range1 = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            LocalTimeRange range2 = LocalTimeRange.of(
                    LocalTime.of(10, 0),
                    LocalTime.of(18, 0)
            );

            assertThat(range1).isNotEqualTo(range2);
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.toString()).contains("09:00");
            assertThat(range.toString()).contains("17:00");
        }

        @Test
        @DisplayName("toString() 跨午夜标识")
        void testToStringCrossesMidnight() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(22, 0),
                    LocalTime.of(6, 0)
            );

            assertThat(range.toString()).contains("crosses midnight");
        }
    }

    @Nested
    @DisplayName("null安全性测试")
    class NullSafetyTests {

        @Test
        @DisplayName("of() null start抛出异常")
        void testOfNullStart() {
            assertThatThrownBy(() -> LocalTimeRange.of(null, LocalTime.of(17, 0)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("of() null end抛出异常")
        void testOfNullEnd() {
            assertThatThrownBy(() -> LocalTimeRange.of(LocalTime.of(9, 0), null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("contains() null time抛出异常")
        void testContainsNullTime() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThatThrownBy(() -> range.contains((LocalTime) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("contains() null range抛出异常")
        void testContainsNullRange() {
            LocalTimeRange range = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThatThrownBy(() -> range.contains((LocalTimeRange) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("parse() null text抛出异常")
        void testParseNullThrows() {
            assertThatThrownBy(() -> LocalTimeRange.parse(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
