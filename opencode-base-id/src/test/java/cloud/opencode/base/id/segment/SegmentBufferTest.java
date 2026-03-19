package cloud.opencode.base.id.segment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SegmentBuffer 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
@DisplayName("SegmentBuffer 测试")
class SegmentBufferTest {

    @Nested
    @DisplayName("创建方法测试")
    class CreateTests {

        @Test
        @DisplayName("默认构造方法")
        void testConstructor() {
            SegmentBuffer buffer = new SegmentBuffer();

            assertThat(buffer).isNotNull();
            assertThat(buffer.isInitialized()).isFalse();
        }

        @Test
        @DisplayName("自定义预加载阈值构造方法")
        void testConstructorWithThreshold() {
            SegmentBuffer buffer = new SegmentBuffer(0.3);

            assertThat(buffer).isNotNull();
        }
    }

    @Nested
    @DisplayName("初始化测试")
    class InitTests {

        @Test
        @DisplayName("初始化缓冲区")
        void testInit() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 1000, 1000);

            buffer.init(segment);

            assertThat(buffer.isInitialized()).isTrue();
            assertThat(buffer.getCurrentValue()).isEqualTo(0);
            assertThat(buffer.getMaxValue()).isEqualTo(1000);
            assertThat(buffer.getStep()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("getNext测试")
    class GetNextTests {

        @Test
        @DisplayName("获取下一个值")
        void testGetNext() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 1000, 1000);
            buffer.init(segment);

            assertThat(buffer.getNext()).isEqualTo(0);
            assertThat(buffer.getNext()).isEqualTo(1);
            assertThat(buffer.getNext()).isEqualTo(2);
        }

        @Test
        @DisplayName("耗尽返回-1")
        void testGetNextExhausted() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 3, 3);
            buffer.init(segment);

            assertThat(buffer.getNext()).isEqualTo(0);
            assertThat(buffer.getNext()).isEqualTo(1);
            assertThat(buffer.getNext()).isEqualTo(2);
            assertThat(buffer.getNext()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("状态查询测试")
    class StatusTests {

        @Test
        @DisplayName("isExhausted方法")
        void testIsExhausted() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 2, 2);
            buffer.init(segment);

            assertThat(buffer.isExhausted()).isFalse();
            buffer.getNext();
            buffer.getNext();
            assertThat(buffer.isExhausted()).isTrue();
        }

        @Test
        @DisplayName("shouldPreload方法")
        void testShouldPreload() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 100, 100);
            buffer.init(segment);

            // 初始时不需要预加载
            assertThat(buffer.shouldPreload()).isFalse();

            // 消耗到20%阈值以下
            for (int i = 0; i < 81; i++) {
                buffer.getNext();
            }

            // 现在应该预加载
            assertThat(buffer.shouldPreload()).isTrue();
        }

        @Test
        @DisplayName("shouldPreload未初始化返回false")
        void testShouldPreloadNotInitialized() {
            SegmentBuffer buffer = new SegmentBuffer();

            assertThat(buffer.shouldPreload()).isFalse();
        }

        @Test
        @DisplayName("getUsagePercent方法")
        void testGetUsagePercent() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 100, 100);
            buffer.init(segment);

            // 消耗50个
            for (int i = 0; i < 50; i++) {
                buffer.getNext();
            }

            assertThat(buffer.getUsagePercent()).isEqualTo(50);
        }

        @Test
        @DisplayName("getUsagePercent未初始化返回0")
        void testGetUsagePercentNotInitialized() {
            SegmentBuffer buffer = new SegmentBuffer();

            assertThat(buffer.getUsagePercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("getRemaining方法")
        void testGetRemaining() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 100, 100);
            buffer.init(segment);

            assertThat(buffer.getRemaining()).isEqualTo(100);

            buffer.getNext();
            buffer.getNext();

            assertThat(buffer.getRemaining()).isEqualTo(98);
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantTests {

        @Test
        @DisplayName("默认预加载阈值")
        void testDefaultPreloadThreshold() {
            assertThat(SegmentBuffer.DEFAULT_PRELOAD_THRESHOLD).isEqualTo(0.2);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString方法")
        void testToString() {
            SegmentBuffer buffer = new SegmentBuffer();
            SegmentAllocator.Segment segment = new SegmentAllocator.Segment(0, 100, 100);
            buffer.init(segment);

            assertThat(buffer.toString()).contains("SegmentBuffer");
            assertThat(buffer.toString()).contains("max=100");
            assertThat(buffer.toString()).contains("step=100");
            assertThat(buffer.toString()).contains("initialized=true");
        }
    }
}
