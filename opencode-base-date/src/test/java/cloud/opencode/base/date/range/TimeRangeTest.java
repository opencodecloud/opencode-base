package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalTimeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeRange 测试类 (兼容性包装器)
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
@DisplayName("TimeRange 测试")
@SuppressWarnings("deprecation")
class TimeRangeTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("of() 创建时间范围")
        void testOf() {
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(17, 0);
            TimeRange range = TimeRange.of(start, end);

            assertThat(range.getStart()).isEqualTo(start);
            assertThat(range.getEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("businessHours() 创建工作时间范围")
        void testBusinessHours() {
            TimeRange range = TimeRange.businessHours();

            assertThat(range.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(range.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }

        @Test
        @DisplayName("from() 从LocalTimeRange创建")
        void testFrom() {
            LocalTimeRange localRange = LocalTimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            TimeRange range = TimeRange.from(localRange);

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
            TimeRange range = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.toDuration()).isEqualTo(Duration.ofHours(8));
        }

        @Test
        @DisplayName("isEmpty() 检查空范围")
        void testIsEmpty() {
            LocalTime time = LocalTime.of(12, 0);
            TimeRange range = TimeRange.of(time, time);

            assertThat(range.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("contains() 包含时间")
        void testContains() {
            TimeRange range = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.contains(LocalTime.of(12, 0))).isTrue();
            assertThat(range.contains(LocalTime.of(18, 0))).isFalse();
        }

        @Test
        @DisplayName("overlaps() 重叠范围")
        void testOverlaps() {
            TimeRange range1 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(14, 0)
            );
            TimeRange range2 = TimeRange.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range1.overlaps(range2)).isTrue();
        }

        @Test
        @DisplayName("encloses() 包含另一范围")
        void testEncloses() {
            TimeRange outer = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            TimeRange inner = TimeRange.of(
                    LocalTime.of(10, 0),
                    LocalTime.of(16, 0)
            );

            assertThat(outer.encloses(inner)).isTrue();
        }
    }

    @Nested
    @DisplayName("集合运算测试")
    class SetOperationTests {

        @Test
        @DisplayName("intersection() 计算交集")
        void testIntersection() {
            TimeRange range1 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(14, 0)
            );
            TimeRange range2 = TimeRange.of(
                    LocalTime.of(12, 0),
                    LocalTime.of(17, 0)
            );

            Optional<TimeRange> intersection = range1.intersection(range2);

            assertThat(intersection).isPresent();
            assertThat(intersection.get().getStart()).isEqualTo(LocalTime.of(12, 0));
            assertThat(intersection.get().getEnd()).isEqualTo(LocalTime.of(14, 0));
        }

        @Test
        @DisplayName("intersection() 无交集返回空")
        void testIntersectionNoOverlap() {
            TimeRange range1 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0)
            );
            TimeRange range2 = TimeRange.of(
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0)
            );

            Optional<TimeRange> intersection = range1.intersection(range2);

            assertThat(intersection).isEmpty();
        }

        @Test
        @DisplayName("span() 计算跨度")
        void testSpan() {
            TimeRange range1 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0)
            );
            TimeRange range2 = TimeRange.of(
                    LocalTime.of(14, 0),
                    LocalTime.of(17, 0)
            );

            TimeRange span = range1.span(range2);

            assertThat(span.getStart()).isEqualTo(LocalTime.of(9, 0));
            assertThat(span.getEnd()).isEqualTo(LocalTime.of(17, 0));
        }
    }

    @Nested
    @DisplayName("转换测试")
    class ConversionTests {

        @Test
        @DisplayName("toLocalTimeRange() 转换为LocalTimeRange")
        void testToLocalTimeRange() {
            TimeRange range = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            LocalTimeRange localRange = range.toLocalTimeRange();

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
            TimeRange range1 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );
            TimeRange range2 = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range1).isEqualTo(range2);
            assertThat(range1.hashCode()).isEqualTo(range2.hashCode());
        }

        @Test
        @DisplayName("toString() 格式化输出")
        void testToString() {
            TimeRange range = TimeRange.of(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0)
            );

            assertThat(range.toString()).contains("09:00");
            assertThat(range.toString()).contains("17:00");
        }
    }
}
