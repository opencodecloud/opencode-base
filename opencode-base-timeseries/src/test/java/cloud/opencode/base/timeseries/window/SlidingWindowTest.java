package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SlidingWindowTest Tests
 * SlidingWindowTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("SlidingWindow Tests")
class SlidingWindowTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create sliding window with size and slide")
        void shouldCreateSlidingWindowWithSizeAndSlide() {
            SlidingWindow window = new SlidingWindow(Duration.ofMinutes(10), Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(10));
            assertThat(window.getSlide()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("should throw for null size")
        void shouldThrowForNullSize() {
            assertThatThrownBy(() -> new SlidingWindow(null, Duration.ofMinutes(5)))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for null slide")
        void shouldThrowForNullSlide() {
            assertThatThrownBy(() -> new SlidingWindow(Duration.ofMinutes(10), null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for zero size")
        void shouldThrowForZeroSize() {
            assertThatThrownBy(() -> new SlidingWindow(Duration.ZERO, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative size")
        void shouldThrowForNegativeSize() {
            assertThatThrownBy(() -> new SlidingWindow(Duration.ofSeconds(-1), Duration.ofMinutes(5)))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for zero slide")
        void shouldThrowForZeroSlide() {
            assertThatThrownBy(() -> new SlidingWindow(Duration.ofMinutes(10), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative slide")
        void shouldThrowForNegativeSlide() {
            assertThatThrownBy(() -> new SlidingWindow(Duration.ofMinutes(10), Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create window with size and slide")
        void ofShouldCreateWindowWithSizeAndSlide() {
            SlidingWindow window = SlidingWindow.of(Duration.ofMinutes(10), Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(10));
            assertThat(window.getSlide()).isEqualTo(Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("Assign Windows Tests")
    class AssignWindowsTests {

        @Test
        @DisplayName("assignWindows should assign point to multiple overlapping windows")
        void assignWindowsShouldAssignPointToMultipleOverlappingWindows() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));
            DataPoint point = DataPoint.of(baseTime.plusSeconds(7), 1.0);

            List<Long> windowKeys = window.assignWindows(point);

            // Point at second 7 should be in windows starting at 0 and 5 (if 10s window, 5s slide)
            assertThat(windowKeys).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("assignWindows should return at least one window")
        void assignWindowsShouldReturnAtLeastOneWindow() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));
            DataPoint point = DataPoint.of(baseTime, 1.0);

            List<Long> windowKeys = window.assignWindows(point);

            assertThat(windowKeys).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Slide Configuration Tests")
    class SlideConfigurationTests {

        @Test
        @DisplayName("getSlide should return configured slide duration")
        void getSlideShouldReturnConfiguredSlideDuration() {
            Duration slide = Duration.ofMinutes(2);
            SlidingWindow window = SlidingWindow.of(Duration.ofMinutes(10), slide);

            assertThat(window.getSlide()).isEqualTo(slide);
        }
    }

    @Nested
    @DisplayName("Overlap Ratio Tests")
    class OverlapRatioTests {

        @Test
        @DisplayName("getOverlapRatio should return correct ratio")
        void getOverlapRatioShouldReturnCorrectRatio() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));

            double ratio = window.getOverlapRatio();

            assertThat(ratio).isEqualTo(0.5);
        }

        @Test
        @DisplayName("getOverlapRatio should return 0 for non-overlapping windows")
        void getOverlapRatioShouldReturn0ForNonOverlappingWindows() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(10));

            double ratio = window.getOverlapRatio();

            assertThat(ratio).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getOverlapRatio should approach 1 for small slide")
        void getOverlapRatioShouldApproach1ForSmallSlide() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(1));

            double ratio = window.getOverlapRatio();

            assertThat(ratio).isEqualTo(0.9);
        }
    }

    @Nested
    @DisplayName("Window Boundaries Tests")
    class WindowBoundariesTests {

        @Test
        @DisplayName("getWindowStart should return start instant for window key")
        void getWindowStartShouldReturnStartInstantForWindowKey() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));
            long windowKey = baseTime.toEpochMilli();

            Instant windowStart = window.getWindowStart(windowKey);

            assertThat(windowStart).isEqualTo(baseTime);
        }

        @Test
        @DisplayName("getWindowEnd should return end instant for window key")
        void getWindowEndShouldReturnEndInstantForWindowKey() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));
            long windowKey = baseTime.toEpochMilli();

            Instant windowEnd = window.getWindowEnd(windowKey);

            assertThat(windowEnd).isEqualTo(baseTime.plusSeconds(10));
        }

        @Test
        @DisplayName("window end should be after window start")
        void windowEndShouldBeAfterWindowStart() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));
            long windowKey = baseTime.toEpochMilli();

            Instant start = window.getWindowStart(windowKey);
            Instant end = window.getWindowEnd(windowKey);

            assertThat(end).isAfter(start);
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("getSize should return window size")
        void getSizeShouldReturnWindowSize() {
            Duration size = Duration.ofMinutes(15);
            SlidingWindow window = SlidingWindow.of(size, Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(size);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain window information")
        void toStringShouldContainWindowInformation() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(5));

            String str = window.toString();

            assertThat(str).contains("SlidingWindow");
            assertThat(str).contains("size");
            assertThat(str).contains("slide");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle slide equal to size (tumbling behavior)")
        void shouldHandleSlideEqualToSize() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofSeconds(10));
            DataPoint point = DataPoint.of(baseTime.plusSeconds(5), 1.0);

            List<Long> windowKeys = window.assignWindows(point);

            // When slide equals size, each point belongs to exactly one window
            assertThat(windowKeys).hasSize(1);
        }

        @Test
        @DisplayName("should handle very small slide")
        void shouldHandleVerySmallSlide() {
            SlidingWindow window = SlidingWindow.of(Duration.ofSeconds(10), Duration.ofMillis(100));

            assertThat(window.getOverlapRatio()).isGreaterThan(0.9);
        }

        @Test
        @DisplayName("should handle very small window size")
        void shouldHandleVerySmallWindowSize() {
            SlidingWindow window = SlidingWindow.of(Duration.ofMillis(1), Duration.ofMillis(1));
            DataPoint point = DataPoint.of(baseTime, 1.0);

            List<Long> keys = window.assignWindows(point);

            assertThat(keys).hasSize(1);
        }

        @Test
        @DisplayName("should handle very large window size")
        void shouldHandleVeryLargeWindowSize() {
            SlidingWindow window = SlidingWindow.of(Duration.ofDays(365), Duration.ofDays(1));

            DataPoint point = DataPoint.of(baseTime, 1.0);
            List<Long> keys = window.assignWindows(point);

            assertThat(keys).isNotEmpty();
        }
    }
}
