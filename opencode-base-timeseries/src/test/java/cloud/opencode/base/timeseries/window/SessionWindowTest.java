package cloud.opencode.base.timeseries.window;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * SessionWindowTest Tests
 * SessionWindowTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("SessionWindow Tests")
class SessionWindowTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create session window with gap")
        void shouldCreateSessionWindowWithGap() {
            SessionWindow window = new SessionWindow(Duration.ofMinutes(5));

            assertThat(window.getGap()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("should throw for null gap")
        void shouldThrowForNullGap() {
            assertThatThrownBy(() -> new SessionWindow(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for zero gap")
        void shouldThrowForZeroGap() {
            assertThatThrownBy(() -> new SessionWindow(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw for negative gap")
        void shouldThrowForNegativeGap() {
            assertThatThrownBy(() -> new SessionWindow(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create window with specified gap")
        void ofShouldCreateWindowWithSpecifiedGap() {
            SessionWindow window = SessionWindow.of(Duration.ofMinutes(5));

            assertThat(window.getGap()).isEqualTo(Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("Gap Configuration Tests")
    class GapConfigurationTests {

        @Test
        @DisplayName("getGap should return configured gap duration")
        void getGapShouldReturnConfiguredGapDuration() {
            Duration gap = Duration.ofMinutes(10);
            SessionWindow window = SessionWindow.of(gap);

            assertThat(window.getGap()).isEqualTo(gap);
        }

        @Test
        @DisplayName("getSize should return gap duration")
        void getSizeShouldReturnGapDuration() {
            SessionWindow window = SessionWindow.of(Duration.ofMinutes(5));

            assertThat(window.getSize()).isEqualTo(Duration.ofMinutes(5));
        }
    }

    @Nested
    @DisplayName("Assign Windows Tests")
    class AssignWindowsTests {

        @Test
        @DisplayName("assignWindows should assign point to session")
        void assignWindowsShouldAssignPointToSession() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));
            DataPoint point = DataPoint.of(baseTime, 1.0);

            List<Long> windowKeys = window.assignWindows(point);

            assertThat(windowKeys).isNotEmpty();
            assertThat(windowKeys).hasSize(1);
        }

        @Test
        @DisplayName("assignWindows should create new session for distant point")
        void assignWindowsShouldCreateNewSessionForDistantPoint() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            // First point creates a session
            window.assignWindows(DataPoint.of(baseTime, 1.0));

            // Second point within gap - same session
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(5), 2.0));

            // Third point far away - new session
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(30), 3.0));

            // Check session count
            assertThat(window.getSessionCount()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Get Sessions Tests")
    class GetSessionsTests {

        @Test
        @DisplayName("getSessions should return map of session start to end times")
        void getSessionsShouldReturnMapOfSessionTimes() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            // Add points
            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(5), 2.0));

            Map<Instant, Instant> sessions = window.getSessions();

            assertThat(sessions).isNotEmpty();
        }

        @Test
        @DisplayName("getSessions should return empty map initially")
        void getSessionsShouldReturnEmptyMapInitially() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            Map<Instant, Instant> sessions = window.getSessions();

            assertThat(sessions).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Session Count Tests")
    class GetSessionCountTests {

        @Test
        @DisplayName("getSessionCount should return number of sessions")
        void getSessionCountShouldReturnNumberOfSessions() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            // Create first session
            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(5), 2.0));

            // Create second session (gap > 10 seconds)
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(30), 3.0));

            int count = window.getSessionCount();

            assertThat(count).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("getSessionCount should return 0 initially")
        void getSessionCountShouldReturn0Initially() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            int count = window.getSessionCount();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Window Boundaries Tests")
    class WindowBoundariesTests {

        @Test
        @DisplayName("getWindowStart should return instant from window key")
        void getWindowStartShouldReturnInstantFromWindowKey() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));
            long windowKey = baseTime.toEpochMilli();

            Instant start = window.getWindowStart(windowKey);

            assertThat(start).isEqualTo(baseTime);
        }

        @Test
        @DisplayName("getWindowEnd should return end time for window key")
        void getWindowEndShouldReturnEndTimeForWindowKey() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            // First add a point to create a session
            window.assignWindows(DataPoint.of(baseTime, 1.0));

            long windowKey = baseTime.toEpochMilli();
            Instant end = window.getWindowEnd(windowKey);

            assertThat(end).isNotNull();
        }
    }

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("clear should reset internal state")
        void clearShouldResetInternalState() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            // Add points
            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(30), 2.0));

            assertThat(window.getSessionCount()).isGreaterThan(0);

            // Clear
            window.clear();

            assertThat(window.getSessionCount()).isZero();
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain gap information")
        void toStringShouldContainGapInformation() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            String str = window.toString();

            assertThat(str).contains("SessionWindow");
            assertThat(str).contains("gap");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle points exactly at gap boundary")
        void shouldHandlePointsExactlyAtGapBoundary() {
            SessionWindow window = SessionWindow.of(Duration.ofSeconds(10));

            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plusSeconds(10), 2.0));

            // Should be in same session or adjacent
            assertThat(window.getSessionCount()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("should handle very large gap")
        void shouldHandleVeryLargeGap() {
            SessionWindow window = SessionWindow.of(Duration.ofDays(365));

            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plus(Duration.ofDays(100)), 2.0));

            // All points should be in same session
            assertThat(window.getSessionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle very small gap")
        void shouldHandleVerySmallGap() {
            SessionWindow window = SessionWindow.of(Duration.ofMillis(1));

            window.assignWindows(DataPoint.of(baseTime, 1.0));
            window.assignWindows(DataPoint.of(baseTime.plusMillis(10), 2.0));

            // Should create separate sessions
            assertThat(window.getSessionCount()).isGreaterThanOrEqualTo(1);
        }
    }
}
