package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * WindowTest Tests
 * WindowTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("Window 接口测试")
class WindowTest {

    @Nested
    @DisplayName("默认方法isInWindow测试")
    class IsInWindowTests {

        @Test
        @DisplayName("窗口内的时间戳返回true")
        void testTimestampInWindow() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            long windowKey = 0L;
            Instant timestamp = Instant.ofEpochMilli(60000);

            assertThat(window.isInWindow(windowKey, timestamp)).isTrue();
        }

        @Test
        @DisplayName("窗口外的时间戳返回false")
        void testTimestampOutsideWindow() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            long windowKey = 0L;
            Instant timestamp = Instant.ofEpochMilli(300001);

            assertThat(window.isInWindow(windowKey, timestamp)).isFalse();
        }

        @Test
        @DisplayName("窗口开始时间在窗口内")
        void testWindowStartIsInside() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            long windowKey = 0L;
            Instant start = window.getWindowStart(windowKey);

            assertThat(window.isInWindow(windowKey, start)).isTrue();
        }

        @Test
        @DisplayName("窗口结束时间不在窗口内")
        void testWindowEndIsOutside() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            long windowKey = 0L;
            Instant end = window.getWindowEnd(windowKey);

            assertThat(window.isInWindow(windowKey, end)).isFalse();
        }
    }

    @Nested
    @DisplayName("默认方法getWindowKey测试")
    class GetWindowKeyTests {

        @Test
        @DisplayName("返回正确的窗口键")
        void testGetWindowKeyForTimestamp() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            Instant timestamp = Instant.ofEpochMilli(360000); // 6 minutes

            long key = window.getWindowKey(timestamp);

            assertThat(key).isEqualTo(300000L); // 5 minutes aligned
        }

        @Test
        @DisplayName("窗口起始时间对齐")
        void testWindowKeyAlignment() {
            Window window = new TumblingWindow(Duration.ofSeconds(10));

            long key1 = window.getWindowKey(Instant.ofEpochMilli(5000));
            long key2 = window.getWindowKey(Instant.ofEpochMilli(9999));
            long key3 = window.getWindowKey(Instant.ofEpochMilli(10000));

            assertThat(key1).isEqualTo(key2);
            assertThat(key3).isNotEqualTo(key1);
        }
    }

    @Nested
    @DisplayName("接口实现TumblingWindow测试")
    class TumblingWindowImplTests {

        @Test
        @DisplayName("assignWindows分配到单个窗口")
        void testAssignWindows() {
            Window window = new TumblingWindow(Duration.ofMinutes(5));
            DataPoint point = DataPoint.of(Instant.ofEpochMilli(60000), 42.0);

            List<Long> keys = window.assignWindows(point);

            assertThat(keys).hasSize(1);
        }

        @Test
        @DisplayName("getSize返回窗口大小")
        void testGetSize() {
            Duration size = Duration.ofMinutes(5);
            Window window = new TumblingWindow(size);

            assertThat(window.getSize()).isEqualTo(size);
        }
    }
}
