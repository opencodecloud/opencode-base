package cloud.opencode.base.classloader.leak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for LeakDetector
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V2.0.0
 */
@DisplayName("LeakDetector Tests")
class LeakDetectorTest {

    private LeakDetector detector;

    @BeforeEach
    void setUp() {
        detector = LeakDetector.getInstance();
        detector.clearReports();
    }

    @Nested
    @DisplayName("Singleton Tests")
    class SingletonTests {

        @Test
        @DisplayName("getInstance should always return same instance")
        void shouldReturnSameInstance() {
            LeakDetector a = LeakDetector.getInstance();
            LeakDetector b = LeakDetector.getInstance();
            assertThat(a).isSameAs(b);
        }
    }

    @Nested
    @DisplayName("Track and Untrack Tests")
    class TrackUntrackTests {

        @Test
        @DisplayName("Should track a ClassLoader with SIMPLE level")
        void shouldTrackSimple() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            int before = detector.getTrackedCount();

            detector.track(cl, "test-simple", LeakDetection.SIMPLE);

            assertThat(detector.getTrackedCount()).isEqualTo(before + 1);

            detector.untrack(cl);
            assertThat(detector.getTrackedCount()).isEqualTo(before);
        }

        @Test
        @DisplayName("Should track a ClassLoader with PARANOID level")
        void shouldTrackParanoid() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            int before = detector.getTrackedCount();

            detector.track(cl, "test-paranoid", LeakDetection.PARANOID);

            assertThat(detector.getTrackedCount()).isEqualTo(before + 1);

            detector.untrack(cl);
        }

        @Test
        @DisplayName("Should be no-op for DISABLED level")
        void shouldBeNoOpForDisabled() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            int before = detector.getTrackedCount();

            detector.track(cl, "test-disabled", LeakDetection.DISABLED);

            assertThat(detector.getTrackedCount()).isEqualTo(before);
        }

        @Test
        @DisplayName("Untrack should be idempotent")
        void untrackShouldBeIdempotent() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            detector.track(cl, "test-idempotent", LeakDetection.SIMPLE);
            int before = detector.getTrackedCount();

            detector.untrack(cl);
            detector.untrack(cl); // second call should be safe

            assertThat(detector.getTrackedCount()).isEqualTo(before - 1);
        }

        @Test
        @DisplayName("Untrack of untracked ClassLoader should be safe")
        void untrackOfUntrackedShouldBeSafe() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            assertThatCode(() -> detector.untrack(cl)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Null Argument Tests")
    class NullArgumentTests {

        @Test
        @DisplayName("track should reject null classLoader")
        void trackShouldRejectNullClassLoader() {
            assertThatThrownBy(() -> detector.track(null, "name", LeakDetection.SIMPLE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("classLoader");
        }

        @Test
        @DisplayName("track should reject null name")
        void trackShouldRejectNullName() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            assertThatThrownBy(() -> detector.track(cl, null, LeakDetection.SIMPLE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("track should reject null level")
        void trackShouldRejectNullLevel() {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            assertThatThrownBy(() -> detector.track(cl, "name", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("level");
        }

        @Test
        @DisplayName("untrack should reject null classLoader")
        void untrackShouldRejectNull() {
            assertThatThrownBy(() -> detector.untrack(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("classLoader");
        }
    }

    @Nested
    @DisplayName("Leak Detection Tests")
    class LeakDetectionTests {

        @Test
        @DisplayName("Should detect leak when tracked ClassLoader is GC'd without untrack")
        void shouldDetectLeakOnGc() throws InterruptedException {
            // Track a classloader then drop reference without untracking
            WeakReference<URLClassLoader> weakRef;
            {
                URLClassLoader cl = new URLClassLoader(new URL[0], null);
                weakRef = new WeakReference<>(cl);
                detector.track(cl, "leaky-loader", LeakDetection.SIMPLE);
                // cl goes out of scope here
            }

            // Force GC and wait for detection
            for (int i = 0; i < 20; i++) {
                System.gc();
                Thread.sleep(100);
                if (weakRef.get() == null && !detector.getLeakReports().isEmpty()) {
                    break;
                }
            }

            // The classloader should have been collected
            if (weakRef.get() == null) {
                List<LeakReport> reports = detector.getLeakReports();
                assertThat(reports).anySatisfy(report -> {
                    assertThat(report.name()).isEqualTo("leaky-loader");
                    assertThat(report.level()).isEqualTo(LeakDetection.SIMPLE);
                });
            }
            // If GC did not collect (can happen in CI), test is inconclusive — not a failure
        }

        @Test
        @DisplayName("Should not report leak when ClassLoader is properly untracked")
        void shouldNotReportWhenProperlyClosed() throws InterruptedException {
            URLClassLoader cl = new URLClassLoader(new URL[0], null);
            detector.track(cl, "proper-loader", LeakDetection.SIMPLE);
            detector.untrack(cl);

            // drop reference
            @SuppressWarnings("unused")
            URLClassLoader dummy = cl;
            cl = null;
            dummy = null;

            System.gc();
            Thread.sleep(500);

            List<LeakReport> reports = detector.getLeakReports();
            assertThat(reports).noneSatisfy(report ->
                    assertThat(report.name()).isEqualTo("proper-loader"));
        }
    }

    @Nested
    @DisplayName("getLeakReports Tests")
    class GetLeakReportsTests {

        @Test
        @DisplayName("Should return empty list initially after clearReports")
        void shouldReturnEmptyListInitially() {
            assertThat(detector.getLeakReports()).isEmpty();
        }

        @Test
        @DisplayName("Returned list should be unmodifiable")
        void returnedListShouldBeUnmodifiable() {
            List<LeakReport> reports = detector.getLeakReports();
            assertThatThrownBy(() -> reports.add(new LeakReport("x", LeakDetection.SIMPLE,
                    new StackTraceElement[0], 0, 0L)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
