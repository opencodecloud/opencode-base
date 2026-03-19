package cloud.opencode.base.id.segment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SegmentAllocator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SegmentAllocator 测试")
class SegmentAllocatorTest {

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("默认步长")
        void testDefaultStep() {
            assertThat(SegmentAllocator.DEFAULT_STEP).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("Segment内部类测试")
    class SegmentTests {

        @Test
        @DisplayName("创建Segment")
        void testConstructor() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 1000, 1000);

            assertThat(segment).isNotNull();
        }

        @Test
        @DisplayName("start方法")
        void testStart() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.start()).isEqualTo(100);
        }

        @Test
        @DisplayName("end方法")
        void testEnd() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.end()).isEqualTo(200);
        }

        @Test
        @DisplayName("step方法")
        void testStep() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.step()).isEqualTo(100);
        }

        @Test
        @DisplayName("getMaxValue方法")
        void testGetMaxValue() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.getMaxValue()).isEqualTo(200);
        }

        @Test
        @DisplayName("getCurrentValue方法")
        void testGetCurrentValue() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.getCurrentValue()).isEqualTo(100);
        }

        @Test
        @DisplayName("remaining方法")
        void testRemaining() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.remaining()).isEqualTo(100);
        }

        @Test
        @DisplayName("toString方法")
        void testToString() {
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(100, 200, 100);

            assertThat(segment.toString()).contains("start=100");
            assertThat(segment.toString()).contains("end=200");
            assertThat(segment.toString()).contains("step=100");
        }
    }
}
