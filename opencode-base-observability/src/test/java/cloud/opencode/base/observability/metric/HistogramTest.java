package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Histogram} via {@link MetricRegistry}.
 */
@DisplayName("Histogram")
class HistogramTest {

    private MetricRegistry registry;
    private Histogram histogram;

    @BeforeEach
    void setUp() {
        registry = MetricRegistry.create();
        histogram = registry.histogram("test.histogram");
    }

    @Nested
    @DisplayName("record()")
    class Record {

        @Test
        @DisplayName("should record a value")
        void shouldRecordValue() {
            histogram.record(42.0);
            assertThat(histogram.count()).isEqualTo(1);
            assertThat(histogram.totalAmount()).isCloseTo(42.0, within(0.001));
        }

        @Test
        @DisplayName("should accumulate multiple values")
        void shouldAccumulate() {
            histogram.record(10.0);
            histogram.record(20.0);
            histogram.record(30.0);
            assertThat(histogram.count()).isEqualTo(3);
            assertThat(histogram.totalAmount()).isCloseTo(60.0, within(0.001));
        }

        @Test
        @DisplayName("should throw on NaN")
        void shouldThrowOnNaN() {
            assertThatThrownBy(() -> histogram.record(Double.NaN))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("finite");
        }

        @Test
        @DisplayName("should throw on positive infinity")
        void shouldThrowOnPositiveInfinity() {
            assertThatThrownBy(() -> histogram.record(Double.POSITIVE_INFINITY))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("finite");
        }

        @Test
        @DisplayName("should throw on negative infinity")
        void shouldThrowOnNegativeInfinity() {
            assertThatThrownBy(() -> histogram.record(Double.NEGATIVE_INFINITY))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("finite");
        }
    }

    @Nested
    @DisplayName("max()")
    class Max {

        @Test
        @DisplayName("should track max value")
        void shouldTrackMax() {
            histogram.record(10.0);
            histogram.record(50.0);
            histogram.record(30.0);
            assertThat(histogram.max()).isCloseTo(50.0, within(0.001));
        }

        @Test
        @DisplayName("should return zero when empty")
        void shouldReturnZeroWhenEmpty() {
            assertThat(histogram.max()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("mean()")
    class Mean {

        @Test
        @DisplayName("should compute mean")
        void shouldComputeMean() {
            histogram.record(10.0);
            histogram.record(20.0);
            histogram.record(30.0);
            assertThat(histogram.mean()).isCloseTo(20.0, within(0.001));
        }

        @Test
        @DisplayName("should return zero when empty")
        void shouldReturnZeroWhenEmpty() {
            assertThat(histogram.mean()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("percentile()")
    class Percentile {

        @Test
        @DisplayName("should compute p50")
        void shouldComputeP50() {
            for (int i = 1; i <= 100; i++) {
                histogram.record(i);
            }
            double p50 = histogram.percentile(0.5);
            assertThat(p50).isCloseTo(50.0, within(1.0));
        }

        @Test
        @DisplayName("should compute p90")
        void shouldComputeP90() {
            for (int i = 1; i <= 100; i++) {
                histogram.record(i);
            }
            double p90 = histogram.percentile(0.9);
            assertThat(p90).isCloseTo(90.0, within(1.0));
        }

        @Test
        @DisplayName("should compute p99")
        void shouldComputeP99() {
            for (int i = 1; i <= 100; i++) {
                histogram.record(i);
            }
            double p99 = histogram.percentile(0.99);
            assertThat(p99).isCloseTo(99.0, within(1.0));
        }

        @Test
        @DisplayName("should handle p0 boundary")
        void shouldHandleP0() {
            for (int i = 1; i <= 10; i++) {
                histogram.record(i);
            }
            double p0 = histogram.percentile(0.0);
            assertThat(p0).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("should handle p1.0 boundary")
        void shouldHandleP100() {
            for (int i = 1; i <= 10; i++) {
                histogram.record(i);
            }
            double p100 = histogram.percentile(1.0);
            assertThat(p100).isCloseTo(10.0, within(0.001));
        }

        @Test
        @DisplayName("should return zero when empty")
        void shouldReturnZeroWhenEmpty() {
            assertThat(histogram.percentile(0.5)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should throw on percentile below 0")
        void shouldThrowOnPercentileBelowZero() {
            assertThatThrownBy(() -> histogram.percentile(-0.1))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Percentile must be in range");
        }

        @Test
        @DisplayName("should throw on percentile above 1")
        void shouldThrowOnPercentileAboveOne() {
            assertThatThrownBy(() -> histogram.percentile(1.1))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Percentile must be in range");
        }
    }

    @Nested
    @DisplayName("id()")
    class Id {

        @Test
        @DisplayName("should return correct metric id")
        void shouldReturnCorrectId() {
            assertThat(histogram.id().name()).isEqualTo("test.histogram");
        }
    }
}
