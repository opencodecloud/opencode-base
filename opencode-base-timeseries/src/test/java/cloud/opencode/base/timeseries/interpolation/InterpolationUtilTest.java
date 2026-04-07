package cloud.opencode.base.timeseries.interpolation;

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
 * Tests for {@link InterpolationUtil}
 */
class InterpolationUtilTest {

    private static final Instant BASE = Instant.parse("2024-01-01T00:00:00Z");

    private static TimeSeries buildSeriesAt(String name, long[] offsetsSeconds, double[] values) {
        TimeSeries ts = new TimeSeries(name);
        for (int i = 0; i < values.length; i++) {
            ts.add(BASE.plusSeconds(offsetsSeconds[i]), values[i]);
        }
        return ts;
    }

    @Nested
    class LinearTest {

        @Test
        void shouldInterpolateLinearly() {
            // Two points: 0s=0.0, 20s=100.0
            TimeSeries ts = buildSeriesAt("metric",
                    new long[]{0, 20},
                    new double[]{0.0, 100.0});

            TimeSeries result = InterpolationUtil.linear(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3); // 0s, 10s, 20s
            assertThat(pts.get(0).value()).isCloseTo(0.0, within(0.001));
            assertThat(pts.get(1).value()).isCloseTo(50.0, within(0.001));
            assertThat(pts.get(2).value()).isCloseTo(100.0, within(0.001));
        }

        @Test
        void shouldInterpolateWithTwoPoints() {
            TimeSeries ts = buildSeriesAt("temp",
                    new long[]{0, 60},
                    new double[]{20.0, 80.0});

            TimeSeries result = InterpolationUtil.linear(ts, Duration.ofSeconds(20));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(4); // 0s, 20s, 40s, 60s
            assertThat(pts.get(0).value()).isCloseTo(20.0, within(0.001));
            assertThat(pts.get(1).value()).isCloseTo(40.0, within(0.001));
            assertThat(pts.get(2).value()).isCloseTo(60.0, within(0.001));
            assertThat(pts.get(3).value()).isCloseTo(80.0, within(0.001));
        }

        @Test
        void shouldPreserveExactPoints() {
            TimeSeries ts = buildSeriesAt("exact",
                    new long[]{0, 10, 20},
                    new double[]{5.0, 15.0, 25.0});

            TimeSeries result = InterpolationUtil.linear(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            assertThat(pts.get(0).value()).isEqualTo(5.0);
            assertThat(pts.get(1).value()).isEqualTo(15.0);
            assertThat(pts.get(2).value()).isEqualTo(25.0);
        }

        @Test
        void shouldThrowOnFewerThanTwoPoints() {
            TimeSeries ts = buildSeriesAt("single",
                    new long[]{0},
                    new double[]{1.0});

            assertThatThrownBy(() -> InterpolationUtil.linear(ts, Duration.ofSeconds(10)))
                    .isInstanceOf(TimeSeriesException.class)
                    .hasMessageContaining("at least 2 points");
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries ts = buildSeriesAt("test",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            assertThatThrownBy(() -> InterpolationUtil.linear(ts, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> InterpolationUtil.linear(ts, Duration.ofSeconds(-1)))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        void shouldHandleNonUniformInput() {
            // Irregular spacing: 0s, 5s, 30s
            TimeSeries ts = buildSeriesAt("irregular",
                    new long[]{0, 5, 30},
                    new double[]{0.0, 10.0, 60.0});

            TimeSeries result = InterpolationUtil.linear(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            // 0s=0.0, 10s interpolated between (5s,10.0) and (30s,60.0), 20s, 30s=60.0
            assertThat(pts).hasSize(4); // 0s, 10s, 20s, 30s
            assertThat(pts.get(0).value()).isCloseTo(0.0, within(0.001));
            // At 10s: between (5, 10.0) and (30, 60.0): 10 + (60-10)*(10-5)/(30-5) = 10 + 50*5/25 = 20.0
            assertThat(pts.get(1).value()).isCloseTo(20.0, within(0.001));
            // At 20s: between (5, 10.0) and (30, 60.0): 10 + 50*15/25 = 40.0
            assertThat(pts.get(2).value()).isCloseTo(40.0, within(0.001));
            assertThat(pts.get(3).value()).isCloseTo(60.0, within(0.001));
        }
    }

    @Nested
    class StepTest {

        @Test
        void shouldCarryForwardLastValue() {
            TimeSeries ts = buildSeriesAt("sensor",
                    new long[]{0, 25},
                    new double[]{42.0, 99.0});

            TimeSeries result = InterpolationUtil.step(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            // Grid: 0s, 10s, 20s (25s not on grid)
            assertThat(pts).hasSize(3);
            assertThat(pts.get(0).value()).isEqualTo(42.0); // exact
            assertThat(pts.get(1).value()).isEqualTo(42.0); // LOCF: carry 42.0
            assertThat(pts.get(2).value()).isEqualTo(42.0); // LOCF: carry 42.0
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            TimeSeries result = InterpolationUtil.step(ts, Duration.ofSeconds(10));

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldHandleSinglePoint() {
            TimeSeries ts = buildSeriesAt("single",
                    new long[]{0},
                    new double[]{7.0});

            TimeSeries result = InterpolationUtil.step(ts, Duration.ofSeconds(10));

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getPoints().getFirst().value()).isEqualTo(7.0);
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries ts = buildSeriesAt("test",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            assertThatThrownBy(() -> InterpolationUtil.step(ts, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    class SplineTest {

        @Test
        void shouldInterpolateSmoothly() {
            // 3 points forming a curve: 0s=0, 30s=30, 60s=0
            TimeSeries ts = buildSeriesAt("curve",
                    new long[]{0, 30, 60},
                    new double[]{0.0, 30.0, 0.0});

            TimeSeries result = InterpolationUtil.spline(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(7); // 0, 10, 20, 30, 40, 50, 60
            // Endpoints should match exactly
            assertThat(pts.getFirst().value()).isCloseTo(0.0, within(0.001));
            assertThat(pts.get(3).value()).isCloseTo(30.0, within(0.001));
            assertThat(pts.getLast().value()).isCloseTo(0.0, within(0.001));
            // Symmetry: value at 10s should equal value at 50s
            assertThat(pts.get(1).value()).isCloseTo(pts.get(5).value(), within(0.001));
        }

        @Test
        void shouldPassThroughKnownPoints() {
            // Quadratic-like data: x^2 sampled at 0, 10, 20, 30
            TimeSeries ts = buildSeriesAt("quad",
                    new long[]{0, 10, 20, 30},
                    new double[]{0.0, 100.0, 400.0, 900.0});

            TimeSeries result = InterpolationUtil.spline(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(4);
            assertThat(pts.get(0).value()).isCloseTo(0.0, within(0.001));
            assertThat(pts.get(1).value()).isCloseTo(100.0, within(0.001));
            assertThat(pts.get(2).value()).isCloseTo(400.0, within(0.001));
            assertThat(pts.get(3).value()).isCloseTo(900.0, within(0.001));
        }

        @Test
        void shouldThrowWithFewerThanThreePoints() {
            TimeSeries ts = buildSeriesAt("short",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            assertThatThrownBy(() -> InterpolationUtil.spline(ts, Duration.ofSeconds(5)))
                    .isInstanceOf(TimeSeriesException.class)
                    .hasMessageContaining("at least 3 points");
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries ts = buildSeriesAt("test",
                    new long[]{0, 10, 20},
                    new double[]{1.0, 2.0, 3.0});

            assertThatThrownBy(() -> InterpolationUtil.spline(ts, Duration.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        void shouldProduceSmoothInterpolationBetweenPoints() {
            // Linear data: spline should reproduce linear exactly
            TimeSeries ts = buildSeriesAt("linear",
                    new long[]{0, 20, 40},
                    new double[]{0.0, 20.0, 40.0});

            TimeSeries result = InterpolationUtil.spline(ts, Duration.ofSeconds(10));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(5); // 0, 10, 20, 30, 40
            // For linear data, natural cubic spline should be (close to) exact
            assertThat(pts.get(0).value()).isCloseTo(0.0, within(0.1));
            assertThat(pts.get(1).value()).isCloseTo(10.0, within(0.1));
            assertThat(pts.get(2).value()).isCloseTo(20.0, within(0.1));
            assertThat(pts.get(3).value()).isCloseTo(30.0, within(0.1));
            assertThat(pts.get(4).value()).isCloseTo(40.0, within(0.1));
        }
    }

    @Nested
    class InterpolateAtTest {

        @Test
        void shouldInterpolateAtSpecificTimestamps() {
            // 0s=0.0, 20s=100.0
            TimeSeries ts = buildSeriesAt("data",
                    new long[]{0, 20},
                    new double[]{0.0, 100.0});

            TimeSeries result = InterpolationUtil.interpolateAt(ts,
                    BASE.plusSeconds(5),
                    BASE.plusSeconds(10),
                    BASE.plusSeconds(15));

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            assertThat(pts.get(0).value()).isCloseTo(25.0, within(0.001));
            assertThat(pts.get(1).value()).isCloseTo(50.0, within(0.001));
            assertThat(pts.get(2).value()).isCloseTo(75.0, within(0.001));
        }

        @Test
        void shouldExtrapolateBeyondSeriesBounds() {
            // 0s=10.0, 10s=20.0
            TimeSeries ts = buildSeriesAt("data",
                    new long[]{0, 10},
                    new double[]{10.0, 20.0});

            // Extrapolate before (at -10s) and after (at 20s)
            Instant before = BASE.minusSeconds(10);
            Instant after = BASE.plusSeconds(20);

            TimeSeries result = InterpolationUtil.interpolateAt(ts, before, after);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(2);
            // Before: extrapolate from (0,10) and (10,20) -> slope=1/s, at -10s -> 0.0
            assertThat(pts.get(0).value()).isCloseTo(0.0, within(0.001));
            // After: extrapolate -> at 20s -> 30.0
            assertThat(pts.get(1).value()).isCloseTo(30.0, within(0.001));
        }

        @Test
        void shouldReturnExactValueAtKnownTimestamp() {
            TimeSeries ts = buildSeriesAt("data",
                    new long[]{0, 10, 20},
                    new double[]{5.0, 15.0, 25.0});

            TimeSeries result = InterpolationUtil.interpolateAt(ts, BASE.plusSeconds(10));

            assertThat(result.getPoints().getFirst().value()).isEqualTo(15.0);
        }

        @Test
        void shouldThrowWithFewerThanTwoPoints() {
            TimeSeries ts = buildSeriesAt("single",
                    new long[]{0},
                    new double[]{1.0});

            assertThatThrownBy(() -> InterpolationUtil.interpolateAt(ts, BASE.plusSeconds(5)))
                    .isInstanceOf(TimeSeriesException.class)
                    .hasMessageContaining("at least 2 points");
        }

        @Test
        void shouldReturnEmptyForNoTargets() {
            TimeSeries ts = buildSeriesAt("data",
                    new long[]{0, 10},
                    new double[]{1.0, 2.0});

            TimeSeries result = InterpolationUtil.interpolateAt(ts);

            assertThat(result.isEmpty()).isTrue();
        }
    }
}
