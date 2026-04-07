package cloud.opencode.base.event.monitor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HeartbeatMonitor")
class HeartbeatMonitorTest {

    private HeartbeatMonitor monitor;

    @AfterEach
    void tearDown() {
        if (monitor != null) {
            monitor.close();
        }
    }

    @Nested
    @DisplayName("builder")
    class BuilderTest {

        @Test
        @DisplayName("should create monitor with defaults")
        void shouldCreateWithDefaults() {
            monitor = HeartbeatMonitor.builder().build();
            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("should set check period")
        void shouldSetCheckPeriod() {
            monitor = HeartbeatMonitor.builder()
                    .checkPeriod(Duration.ofSeconds(10))
                    .build();
            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("should set onMissed callback")
        void shouldSetCallback() {
            monitor = HeartbeatMonitor.builder()
                    .onMissed(id -> {})
                    .build();
            assertThat(monitor).isNotNull();
        }

        @Test
        @DisplayName("should throw for null checkPeriod")
        void shouldThrowForNullCheckPeriod() {
            assertThatThrownBy(() -> HeartbeatMonitor.builder().checkPeriod(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("watch")
    class Watch {

        @Test
        @DisplayName("should register an item to be watched")
        void shouldRegister() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.watch("service-a", Duration.ofMinutes(1));
            assertThat(monitor.watchedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw for null id")
        void shouldThrowForNullId() {
            monitor = HeartbeatMonitor.builder().build();
            assertThatThrownBy(() -> monitor.watch(null, Duration.ofMinutes(1)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw for null interval")
        void shouldThrowForNullInterval() {
            monitor = HeartbeatMonitor.builder().build();
            assertThatThrownBy(() -> monitor.watch("id", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("heartbeat")
    class Heartbeat {

        @Test
        @DisplayName("should record heartbeat without throwing")
        void shouldRecordHeartbeat() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.watch("service-a", Duration.ofMinutes(1));
            monitor.heartbeat("service-a");
            // No exception means success
        }

        @Test
        @DisplayName("should throw for null id")
        void shouldThrowForNullId() {
            monitor = HeartbeatMonitor.builder().build();
            assertThatThrownBy(() -> monitor.heartbeat(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("unwatch")
    class Unwatch {

        @Test
        @DisplayName("should remove item from watch list")
        void shouldRemove() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.watch("service-a", Duration.ofMinutes(1));
            assertThat(monitor.watchedCount()).isEqualTo(1);
            monitor.unwatch("service-a");
            assertThat(monitor.watchedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("should handle null id gracefully")
        void shouldHandleNullId() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.unwatch(null); // should not throw
        }

        @Test
        @DisplayName("should handle non-existent id")
        void shouldHandleNonExistentId() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.unwatch("non-existent"); // should not throw
        }
    }

    @Nested
    @DisplayName("getMissedIds")
    class GetMissedIds {

        @Test
        @DisplayName("should return empty set when no items watched")
        void shouldReturnEmptyWhenNone() {
            monitor = HeartbeatMonitor.builder().build();
            Set<String> missed = monitor.getMissedIds();
            assertThat(missed).isEmpty();
        }

        @Test
        @DisplayName("should not report item that just received heartbeat")
        void shouldNotReportFreshItem() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.watch("service-a", Duration.ofMinutes(1));
            monitor.heartbeat("service-a");
            Set<String> missed = monitor.getMissedIds();
            assertThat(missed).isEmpty();
        }
    }

    @Nested
    @DisplayName("watchedCount")
    class WatchedCount {

        @Test
        @DisplayName("should return correct count")
        void shouldReturnCount() {
            monitor = HeartbeatMonitor.builder().build();
            assertThat(monitor.watchedCount()).isEqualTo(0);
            monitor.watch("a", Duration.ofSeconds(10));
            monitor.watch("b", Duration.ofSeconds(10));
            assertThat(monitor.watchedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("start and stop")
    class StartStop {

        @Test
        @DisplayName("should start and stop without errors")
        void shouldStartAndStop() {
            monitor = HeartbeatMonitor.builder()
                    .checkPeriod(Duration.ofMillis(100))
                    .build();
            monitor.start();
            monitor.stop();
        }

        @Test
        @DisplayName("should be idempotent on multiple starts")
        void shouldHandleMultipleStarts() {
            monitor = HeartbeatMonitor.builder()
                    .checkPeriod(Duration.ofMillis(100))
                    .build();
            monitor.start();
            monitor.start(); // second start should be no-op
            monitor.stop();
        }

        @Test
        @DisplayName("should be idempotent on multiple stops")
        void shouldHandleMultipleStops() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.start();
            monitor.stop();
            monitor.stop(); // second stop should be no-op
        }
    }

    @Nested
    @DisplayName("close")
    class CloseTest {

        @Test
        @DisplayName("should work as alias for stop")
        void shouldCloseAsStop() {
            monitor = HeartbeatMonitor.builder().build();
            monitor.start();
            monitor.close();
            // Should be safe to close again
            monitor.close();
        }
    }

    @Nested
    @DisplayName("missed heartbeat detection")
    class MissedHeartbeatDetection {

        @Test
        @DisplayName("should detect missed heartbeat after interval expires")
        void shouldDetectMissed() throws InterruptedException {
            CopyOnWriteArrayList<String> missedIds = new CopyOnWriteArrayList<>();
            monitor = HeartbeatMonitor.builder()
                    .checkPeriod(Duration.ofMillis(50))
                    .onMissed(missedIds::add)
                    .build();
            monitor.watch("service-a", Duration.ofMillis(10));
            monitor.start();

            // Wait for the interval to expire and checker to run
            Thread.sleep(200);

            assertThat(missedIds).contains("service-a");
        }
    }
}
