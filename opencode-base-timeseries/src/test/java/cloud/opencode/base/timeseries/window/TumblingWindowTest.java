package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * TumblingWindowTest Tests
 * TumblingWindowTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TumblingWindow Tests")
class TumblingWindowTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create tumbling window with size")
        void shouldCreateTumblingWindowWithSize() {
            TumblingWindow window = new TumblingWindow(Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("should throw for null size")
        void shouldThrowForNullSize() {
            assertThatThrownBy(() -> new TumblingWindow(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for zero size")
        void shouldThrowForZeroSize() {
            assertThatThrownBy(() -> new TumblingWindow(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative size")
        void shouldThrowForNegativeSize() {
            assertThatThrownBy(() -> new TumblingWindow(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create window with specified duration")
        void ofShouldCreateWindowWithSpecifiedDuration() {
            TumblingWindow window = TumblingWindow.of(Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("hourly should create 1 hour window")
        void hourlyShouldCreate1HourWindow() {
            TumblingWindow window = TumblingWindow.hourly();

            assertThat(window.getSize()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("daily should create 24 hour window")
        void dailyShouldCreate24HourWindow() {
            TumblingWindow window = TumblingWindow.daily();

            assertThat(window.getSize()).isEqualTo(Duration.ofDays(1));
        }

        @Test
        @DisplayName("minutes should create window with specified minutes")
        void minutesShouldCreateWindowWithSpecifiedMinutes() {
            TumblingWindow window = TumblingWindow.minutes(15);

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(15));
        }
    }

    @Nested
    @DisplayName("Assign Windows Tests")
    class AssignWindowsTests {

        @Test
        @DisplayName("assignWindows should assign point to exactly one window")
        void assignWindowsShouldAssignPointToExactlyOneWindow() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));
            DataPoint point = DataPoint.of(baseTime.plusSeconds(5), 1.0);

            List<Long> windowKeys = window.assignWindows(point);

            assertThat(windowKeys).hasSize(1);
        }

        @Test
        @DisplayName("assignWindows should return window key based on timestamp")
        void assignWindowsShouldReturnWindowKeyBasedOnTimestamp() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));

            // Point at second 5 should be in window starting at 0
            DataPoint point1 = DataPoint.of(baseTime.plusSeconds(5), 1.0);
            List<Long> keys1 = window.assignWindows(point1);

            // Point at second 15 should be in window starting at 10
            DataPoint point2 = DataPoint.of(baseTime.plusSeconds(15), 2.0);
            List<Long> keys2 = window.assignWindows(point2);

            assertThat(keys1.get(0)).isNotEqualTo(keys2.get(0));
        }

        @Test
        @DisplayName("assignWindows should group points in same window together")
        void assignWindowsShouldGroupPointsInSameWindowTogether() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));

            DataPoint point1 = DataPoint.of(baseTime.plusSeconds(1), 1.0);
            DataPoint point2 = DataPoint.of(baseTime.plusSeconds(9), 2.0);

            List<Long> keys1 = window.assignWindows(point1);
            List<Long> keys2 = window.assignWindows(point2);

            assertThat(keys1.get(0)).isEqualTo(keys2.get(0));
        }
    }

    @Nested
    @DisplayName("Window Boundaries Tests")
    class WindowBoundariesTests {

        @Test
        @DisplayName("getWindowStart should return start instant for window key")
        void getWindowStartShouldReturnStartInstantForWindowKey() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));
            long windowKey = baseTime.toEpochMilli();

            Instant windowStart = window.getWindowStart(windowKey);

            assertThat(windowStart).isEqualTo(baseTime);
        }

        @Test
        @DisplayName("getWindowEnd should return end instant for window key")
        void getWindowEndShouldReturnEndInstantForWindowKey() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));
            long windowKey = baseTime.toEpochMilli();

            Instant windowEnd = window.getWindowEnd(windowKey);

            assertThat(windowEnd).isEqualTo(baseTime.plusSeconds(10));
        }

        @Test
        @DisplayName("window boundaries should not overlap")
        void windowBoundariesShouldNotOverlap() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));

            DataPoint point1 = DataPoint.of(baseTime.plusSeconds(5), 1.0);
            DataPoint point2 = DataPoint.of(baseTime.plusSeconds(15), 2.0);

            long key1 = window.assignWindows(point1).get(0);
            long key2 = window.assignWindows(point2).get(0);

            Instant end1 = window.getWindowEnd(key1);
            Instant start2 = window.getWindowStart(key2);

            assertThat(end1).isBeforeOrEqualTo(start2);
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("getSize should return configured duration")
        void getSizeShouldReturnConfiguredDuration() {
            Duration duration = Duration.ofMinutes(30);
            TumblingWindow window = TumblingWindow.of(duration);

            assertThat(window.getSize()).isEqualTo(duration);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain window information")
        void toStringShouldContainWindowInformation() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));

            String str = window.toString();

            assertThat(str).contains("TumblingWindow");
            assertThat(str).contains("size");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle very small window size")
        void shouldHandleVerySmallWindowSize() {
            TumblingWindow window = TumblingWindow.of(Duration.ofMillis(1));
            DataPoint point = DataPoint.of(baseTime, 1.0);

            List<Long> keys = window.assignWindows(point);

            assertThat(keys).hasSize(1);
        }

        @Test
        @DisplayName("should handle very large window size")
        void shouldHandleVeryLargeWindowSize() {
            TumblingWindow window = TumblingWindow.of(Duration.ofDays(365));

            DataPoint point1 = DataPoint.of(baseTime, 1.0);
            DataPoint point2 = DataPoint.of(baseTime.plus(Duration.ofDays(100)), 2.0);

            List<Long> keys1 = window.assignWindows(point1);
            List<Long> keys2 = window.assignWindows(point2);

            // Both should be in same window
            assertThat(keys1.get(0)).isEqualTo(keys2.get(0));
        }

        @Test
        @DisplayName("should handle point at window boundary")
        void shouldHandlePointAtWindowBoundary() {
            TumblingWindow window = TumblingWindow.of(Duration.ofSeconds(10));

            DataPoint point = DataPoint.of(baseTime.plusSeconds(10), 1.0);

            List<Long> keys = window.assignWindows(point);

            // Point at boundary goes to next window
            long expectedKey = baseTime.plusSeconds(10).toEpochMilli();
            assertThat(keys.get(0)).isEqualTo(expectedKey);
        }
    }
}
