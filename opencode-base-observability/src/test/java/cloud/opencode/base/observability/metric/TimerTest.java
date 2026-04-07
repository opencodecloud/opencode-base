package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Timer} via {@link MetricRegistry}.
 */
@DisplayName("Timer")
class TimerTest {

    private MetricRegistry registry;
    private Timer timer;

    @BeforeEach
    void setUp() {
        registry = MetricRegistry.create();
        timer = registry.timer("test.timer");
    }

    @Nested
    @DisplayName("record()")
    class Record {

        @Test
        @DisplayName("should record duration")
        void shouldRecordDuration() {
            timer.record(Duration.ofMillis(100));
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime()).isEqualTo(Duration.ofMillis(100));
        }

        @Test
        @DisplayName("should accumulate multiple recordings")
        void shouldAccumulate() {
            timer.record(Duration.ofMillis(100));
            timer.record(Duration.ofMillis(200));
            assertThat(timer.count()).isEqualTo(2);
            assertThat(timer.totalTime()).isEqualTo(Duration.ofMillis(300));
        }

        @Test
        @DisplayName("should throw on negative duration")
        void shouldThrowOnNegativeDuration() {
            assertThatThrownBy(() -> timer.record(Duration.ofMillis(-1)))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("must not be negative");
        }

        @Test
        @DisplayName("should throw on null duration")
        void shouldThrowOnNullDuration() {
            assertThatThrownBy(() -> timer.record(null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("should accept zero duration")
        void shouldAcceptZeroDuration() {
            timer.record(Duration.ZERO);
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("time(Runnable)")
    class TimeRunnable {

        @Test
        @DisplayName("should time and record a runnable")
        void shouldTimeRunnable() {
            timer.time(() -> {
                // Simulate work
                long sum = 0;
                for (int i = 0; i < 1000; i++) sum += i;
            });
            assertThat(timer.count()).isEqualTo(1);
            assertThat(timer.totalTime().toNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should throw on null runnable")
        void shouldThrowOnNullRunnable() {
            assertThatThrownBy(() -> timer.time((Runnable) null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("must not be null");
        }
    }

    @Nested
    @DisplayName("time(Callable)")
    class TimeCallable {

        @Test
        @DisplayName("should time callable and return result")
        void shouldTimeCallable() {
            String result = timer.time(() -> "hello");
            assertThat(result).isEqualTo("hello");
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should still record duration when callable throws runtime exception")
        void shouldRecordOnRuntimeException() {
            assertThatThrownBy(() -> timer.time((Callable<String>) () -> {
                throw new IllegalStateException("boom");
            })).isInstanceOf(IllegalStateException.class);
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should wrap checked exception in ObservabilityException")
        void shouldWrapCheckedException() {
            assertThatThrownBy(() -> timer.time((Callable<String>) () -> {
                throw new Exception("checked");
            }))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("checked exception")
                    .hasCauseInstanceOf(Exception.class);
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw on null callable")
        void shouldThrowOnNullCallable() {
            assertThatThrownBy(() -> timer.time((Callable<String>) null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("must not be null");
        }
    }

    @Nested
    @DisplayName("max()")
    class Max {

        @Test
        @DisplayName("should track max duration")
        void shouldTrackMax() {
            timer.record(Duration.ofMillis(100));
            timer.record(Duration.ofMillis(300));
            timer.record(Duration.ofMillis(200));
            assertThat(timer.max()).isEqualTo(Duration.ofMillis(300));
        }

        @Test
        @DisplayName("should return zero when no recordings")
        void shouldReturnZeroWhenEmpty() {
            assertThat(timer.max()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("mean()")
    class Mean {

        @Test
        @DisplayName("should compute mean duration")
        void shouldComputeMean() {
            timer.record(Duration.ofMillis(100));
            timer.record(Duration.ofMillis(300));
            assertThat(timer.mean()).isEqualTo(Duration.ofMillis(200));
        }

        @Test
        @DisplayName("should return zero when no recordings")
        void shouldReturnZeroWhenEmpty() {
            assertThat(timer.mean()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("id()")
    class Id {

        @Test
        @DisplayName("should return correct metric id")
        void shouldReturnCorrectId() {
            assertThat(timer.id().name()).isEqualTo("test.timer");
        }
    }
}
