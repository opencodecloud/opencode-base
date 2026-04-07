package cloud.opencode.base.timeseries.rate;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link RateUtil}
 */
class RateUtilTest {

    private static final Instant BASE = Instant.parse("2024-01-01T00:00:00Z");

    private static TimeSeries buildCounter(String name, long[] offsetsSeconds, double[] values) {
        TimeSeries ts = new TimeSeries(name);
        for (int i = 0; i < values.length; i++) {
            ts.add(BASE.plusSeconds(offsetsSeconds[i]), values[i]);
        }
        return ts;
    }

    @Nested
    class RateTest {

        @Test
        void shouldCalculatePerSecondRate() {
            // Counter increases by 100 every 10 seconds -> rate = 10/s
            TimeSeries counter = buildCounter("requests",
                    new long[]{0, 10, 20, 30},
                    new double[]{0.0, 100.0, 200.0, 300.0});

            TimeSeries result = RateUtil.rate(counter, Duration.ofMinutes(5));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            for (DataPoint p : pts) {
                assertThat(p.value()).isCloseTo(10.0, within(0.001));
            }
        }

        @Test
        void shouldHandleCounterReset() {
            // Counter: 100, 200, 50 (reset), 150
            TimeSeries counter = buildCounter("requests",
                    new long[]{0, 10, 20, 30},
                    new double[]{100.0, 200.0, 50.0, 150.0});

            TimeSeries result = RateUtil.rate(counter, Duration.ofMinutes(5));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            // 0->10: (200-100)/10 = 10.0
            assertThat(pts.get(0).value()).isCloseTo(10.0, within(0.001));
            // 10->20: reset, delta = 50 (curr value) / 10s = 5.0
            assertThat(pts.get(1).value()).isCloseTo(5.0, within(0.001));
            // 20->30: (150-50)/10 = 10.0
            assertThat(pts.get(2).value()).isCloseTo(10.0, within(0.001));
        }

        @Test
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries counter = buildCounter("single",
                    new long[]{0},
                    new double[]{100.0});

            TimeSeries result = RateUtil.rate(counter, Duration.ofMinutes(1));

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries counter = new TimeSeries("empty");

            TimeSeries result = RateUtil.rate(counter, Duration.ofMinutes(1));

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldThrowOnNonPositiveWindow() {
            TimeSeries counter = buildCounter("test",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            assertThatThrownBy(() -> RateUtil.rate(counter, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> RateUtil.rate(counter, Duration.ofSeconds(-1)))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        void shouldThrowOnNullArguments() {
            assertThatThrownBy(() -> RateUtil.rate(null, Duration.ofMinutes(1)))
                    .isInstanceOf(NullPointerException.class);
            TimeSeries ts = new TimeSeries("test");
            assertThatThrownBy(() -> RateUtil.rate(ts, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldProduceCorrectTimestamps() {
            TimeSeries counter = buildCounter("req",
                    new long[]{0, 10, 20},
                    new double[]{0.0, 50.0, 100.0});

            TimeSeries result = RateUtil.rate(counter, Duration.ofMinutes(5));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts.get(0).timestamp()).isEqualTo(BASE.plusSeconds(10));
            assertThat(pts.get(1).timestamp()).isEqualTo(BASE.plusSeconds(20));
        }
    }

    @Nested
    class IrateTest {

        @Test
        void shouldReturnSinglePointRate() {
            // Last two points: 20s=200, 30s=350 -> rate=(350-200)/10=15.0
            TimeSeries counter = buildCounter("metric",
                    new long[]{0, 10, 20, 30},
                    new double[]{0.0, 100.0, 200.0, 350.0});

            TimeSeries result = RateUtil.irate(counter);

            assertThat(result.size()).isEqualTo(1);
            DataPoint p = result.getPoints().getFirst();
            assertThat(p.timestamp()).isEqualTo(BASE.plusSeconds(30));
            assertThat(p.value()).isCloseTo(15.0, within(0.001));
        }

        @Test
        void shouldHandleCounterResetInIrate() {
            // Last two: 10s=500, 20s=30 (reset) -> delta=30 (curr value), rate=30/10=3.0
            TimeSeries counter = buildCounter("metric",
                    new long[]{0, 10, 20},
                    new double[]{0.0, 500.0, 30.0});

            TimeSeries result = RateUtil.irate(counter);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getPoints().getFirst().value()).isCloseTo(3.0, within(0.001));
        }

        @Test
        void shouldThrowWithFewerThanTwoPoints() {
            TimeSeries counter = buildCounter("single",
                    new long[]{0},
                    new double[]{1.0});

            assertThatThrownBy(() -> RateUtil.irate(counter))
                    .isInstanceOf(TimeSeriesException.class)
                    .hasMessageContaining("at least 2");
        }

        @Test
        void shouldThrowOnEmptySeries() {
            TimeSeries counter = new TimeSeries("empty");

            assertThatThrownBy(() -> RateUtil.irate(counter))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        void shouldWorkWithExactlyTwoPoints() {
            TimeSeries counter = buildCounter("minimal",
                    new long[]{0, 5},
                    new double[]{10.0, 60.0});

            TimeSeries result = RateUtil.irate(counter);

            assertThat(result.size()).isEqualTo(1);
            // (60-10)/5 = 10.0
            assertThat(result.getPoints().getFirst().value()).isCloseTo(10.0, within(0.001));
        }
    }

    @Nested
    class IncreaseTest {

        @Test
        void shouldCalculateTotalIncrease() {
            TimeSeries counter = buildCounter("bytes",
                    new long[]{0, 10, 20, 30},
                    new double[]{100.0, 250.0, 400.0, 600.0});

            TimeSeries result = RateUtil.increase(counter, Duration.ofMinutes(5));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            assertThat(pts.get(0).value()).isCloseTo(150.0, within(0.001)); // 250-100
            assertThat(pts.get(1).value()).isCloseTo(150.0, within(0.001)); // 400-250
            assertThat(pts.get(2).value()).isCloseTo(200.0, within(0.001)); // 600-400
        }

        @Test
        void shouldHandleResetInIncrease() {
            // 100, 200, 50 (reset), 120
            TimeSeries counter = buildCounter("conn",
                    new long[]{0, 10, 20, 30},
                    new double[]{100.0, 200.0, 50.0, 120.0});

            TimeSeries result = RateUtil.increase(counter, Duration.ofMinutes(5));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts.get(0).value()).isCloseTo(100.0, within(0.001)); // 200-100
            assertThat(pts.get(1).value()).isCloseTo(50.0, within(0.001));  // reset: delta=curr=50
            assertThat(pts.get(2).value()).isCloseTo(70.0, within(0.001));  // 120-50
        }

        @Test
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries counter = buildCounter("single",
                    new long[]{0},
                    new double[]{100.0});

            TimeSeries result = RateUtil.increase(counter, Duration.ofMinutes(1));

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldThrowOnNonPositiveWindow() {
            TimeSeries counter = buildCounter("test",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            assertThatThrownBy(() -> RateUtil.increase(counter, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    class ResetsTest {

        @Test
        void shouldCountResets() {
            // Resets at positions 2 and 4
            TimeSeries counter = buildCounter("counter",
                    new long[]{0, 10, 20, 30, 40, 50},
                    new double[]{10.0, 50.0, 20.0, 80.0, 5.0, 100.0});

            int resetCount = RateUtil.resets(counter);

            assertThat(resetCount).isEqualTo(2);
        }

        @Test
        void shouldReturnZeroForMonotonicSeries() {
            TimeSeries counter = buildCounter("monotonic",
                    new long[]{0, 10, 20, 30},
                    new double[]{0.0, 10.0, 20.0, 30.0});

            assertThat(RateUtil.resets(counter)).isZero();
        }

        @Test
        void shouldReturnZeroForEmptySeries() {
            TimeSeries counter = new TimeSeries("empty");

            assertThat(RateUtil.resets(counter)).isZero();
        }

        @Test
        void shouldReturnZeroForSinglePoint() {
            TimeSeries counter = buildCounter("single",
                    new long[]{0},
                    new double[]{42.0});

            assertThat(RateUtil.resets(counter)).isZero();
        }

        @Test
        void shouldDetectConsecutiveResets() {
            // Every transition is a reset: 100, 50, 20, 10
            TimeSeries counter = buildCounter("decreasing",
                    new long[]{0, 10, 20, 30},
                    new double[]{100.0, 50.0, 20.0, 10.0});

            assertThat(RateUtil.resets(counter)).isEqualTo(3);
        }
    }

    @Nested
    class NonNegativeDerivativeTest {

        @Test
        void shouldCalculateDerivativePerSecond() {
            // Monotonic: 0, 100, 200, 300 at 10s intervals -> derivative = 10/s
            TimeSeries ts = buildCounter("counter",
                    new long[]{0, 10, 20, 30},
                    new double[]{0.0, 100.0, 200.0, 300.0});

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            for (DataPoint p : pts) {
                assertThat(p.value()).isCloseTo(10.0, within(0.001));
            }
        }

        @Test
        void shouldHandleResetInDerivative() {
            // 100, 200, 30 (reset), 80
            TimeSeries ts = buildCounter("counter",
                    new long[]{0, 10, 20, 30},
                    new double[]{100.0, 200.0, 30.0, 80.0});

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            // 100->200: delta=100, rate=100/10=10.0
            assertThat(pts.get(0).value()).isCloseTo(10.0, within(0.001));
            // 200->30: reset, delta=30, rate=30/10=3.0
            assertThat(pts.get(1).value()).isCloseTo(3.0, within(0.001));
            // 30->80: delta=50, rate=50/10=5.0
            assertThat(pts.get(2).value()).isCloseTo(5.0, within(0.001));
        }

        @Test
        void shouldReturnEmptyForSinglePoint() {
            TimeSeries ts = buildCounter("single",
                    new long[]{0},
                    new double[]{1.0});

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldProduceCorrectTimestamps() {
            TimeSeries ts = buildCounter("ts",
                    new long[]{0, 10, 20},
                    new double[]{0.0, 50.0, 100.0});

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts.get(0).timestamp()).isEqualTo(BASE.plusSeconds(10));
            assertThat(pts.get(1).timestamp()).isEqualTo(BASE.plusSeconds(20));
        }

        @Test
        void shouldNameResultWithNnderivSuffix() {
            TimeSeries ts = buildCounter("mymetric",
                    new long[]{0, 10},
                    new double[]{0.0, 50.0});

            TimeSeries result = RateUtil.nonNegativeDerivative(ts);

            assertThat(result.getName()).isEqualTo("mymetric_nnderiv");
        }
    }
}
