package cloud.opencode.base.timeseries.alignment;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import cloud.opencode.base.timeseries.sampling.AggregationType;
import cloud.opencode.base.timeseries.sampling.FillStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link AlignmentUtil}
 */
class AlignmentUtilTest {

    private static final Instant BASE = Instant.parse("2024-01-01T00:00:00Z");

    private static TimeSeries buildSeries(String name, double... values) {
        TimeSeries ts = new TimeSeries(name);
        for (int i = 0; i < values.length; i++) {
            ts.add(BASE.plusSeconds(i * 10), values[i]);
        }
        return ts;
    }

    private static TimeSeries buildSeriesAt(String name, long[] offsetsSeconds, double[] values) {
        TimeSeries ts = new TimeSeries(name);
        for (int i = 0; i < values.length; i++) {
            ts.add(BASE.plusSeconds(offsetsSeconds[i]), values[i]);
        }
        return ts;
    }

    @Nested
    class AlignTest {

        @Test
        void shouldAlignTwoSeriesToCommonGrid() {
            // Series a: 0s, 10s, 20s, 30s
            TimeSeries a = buildSeries("a", 1.0, 2.0, 3.0, 4.0);
            // Series b: 5s, 15s, 25s, 35s
            TimeSeries b = buildSeriesAt("b",
                    new long[]{5, 15, 25, 35},
                    new double[]{10.0, 20.0, 30.0, 40.0});

            TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofSeconds(10), FillStrategy.LINEAR);

            assertThat(aligned).hasSize(2);
            // Both should have the same timestamps
            List<DataPoint> pointsA = aligned[0].getPoints();
            List<DataPoint> pointsB = aligned[1].getPoints();
            assertThat(pointsA).hasSameSizeAs(pointsB);
            for (int i = 0; i < pointsA.size(); i++) {
                assertThat(pointsA.get(i).timestamp()).isEqualTo(pointsB.get(i).timestamp());
            }
        }

        @Test
        void shouldAlignWithLinearFill() {
            // a: 0s=0.0, 20s=20.0
            TimeSeries a = buildSeriesAt("a",
                    new long[]{0, 20},
                    new double[]{0.0, 20.0});
            // b: 0s=100.0, 20s=200.0
            TimeSeries b = buildSeriesAt("b",
                    new long[]{0, 20},
                    new double[]{100.0, 200.0});

            TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofSeconds(10), FillStrategy.LINEAR);

            List<DataPoint> ptsA = aligned[0].getPoints();
            assertThat(ptsA).hasSize(3); // 0s, 10s, 20s
            assertThat(ptsA.get(0).value()).isEqualTo(0.0);
            assertThat(ptsA.get(1).value()).isCloseTo(10.0, within(0.001));
            assertThat(ptsA.get(2).value()).isEqualTo(20.0);

            List<DataPoint> ptsB = aligned[1].getPoints();
            assertThat(ptsB.get(1).value()).isCloseTo(150.0, within(0.001));
        }

        @Test
        void shouldAlignWithPreviousFill() {
            // a: 0s=5.0, 20s=15.0
            TimeSeries a = buildSeriesAt("a",
                    new long[]{0, 20},
                    new double[]{5.0, 15.0});
            // b: 0s=100.0, 20s=200.0
            TimeSeries b = buildSeriesAt("b",
                    new long[]{0, 20},
                    new double[]{100.0, 200.0});

            TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofSeconds(10), FillStrategy.PREVIOUS);

            List<DataPoint> ptsA = aligned[0].getPoints();
            // At 10s, PREVIOUS should carry forward 5.0
            assertThat(ptsA.get(1).value()).isEqualTo(5.0);
        }

        @Test
        void shouldReturnEmptyWhenNoOverlap() {
            // a: 0s-20s
            TimeSeries a = buildSeriesAt("a",
                    new long[]{0, 10, 20},
                    new double[]{1.0, 2.0, 3.0});
            // b: 100s-120s (no overlap)
            TimeSeries b = buildSeriesAt("b",
                    new long[]{100, 110, 120},
                    new double[]{10.0, 20.0, 30.0});

            TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofSeconds(5), FillStrategy.LINEAR);

            assertThat(aligned[0].isEmpty()).isTrue();
            assertThat(aligned[1].isEmpty()).isTrue();
        }

        @Test
        void shouldReturnEmptyWhenEitherSeriesIsEmpty() {
            TimeSeries a = new TimeSeries("a");
            TimeSeries b = buildSeries("b", 1.0, 2.0);

            TimeSeries[] aligned = AlignmentUtil.align(a, b, Duration.ofSeconds(10), FillStrategy.ZERO);

            assertThat(aligned[0].isEmpty()).isTrue();
            assertThat(aligned[1].isEmpty()).isTrue();
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries a = buildSeries("a", 1.0);
            TimeSeries b = buildSeries("b", 2.0);

            assertThatThrownBy(() -> AlignmentUtil.align(a, b, Duration.ZERO, FillStrategy.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> AlignmentUtil.align(a, b, Duration.ofSeconds(-1), FillStrategy.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    class ResampleTest {

        @Test
        void shouldResampleIrregularToRegular() {
            // Irregular: 0s, 7s, 13s, 30s
            TimeSeries ts = buildSeriesAt("sensor",
                    new long[]{0, 7, 13, 30},
                    new double[]{10.0, 17.0, 23.0, 40.0});

            TimeSeries result = AlignmentUtil.resample(ts, Duration.ofSeconds(10), FillStrategy.LINEAR);

            List<DataPoint> pts = result.getPoints();
            // Grid: 0s, 10s, 20s, 30s
            assertThat(pts).hasSize(4);
            assertThat(pts.get(0).timestamp()).isEqualTo(BASE);
            assertThat(pts.get(1).timestamp()).isEqualTo(BASE.plusSeconds(10));
            assertThat(pts.get(2).timestamp()).isEqualTo(BASE.plusSeconds(20));
            assertThat(pts.get(3).timestamp()).isEqualTo(BASE.plusSeconds(30));
        }

        @Test
        void shouldResampleWithZeroFill() {
            // Points at 0s, 30s only
            TimeSeries ts = buildSeriesAt("metric",
                    new long[]{0, 30},
                    new double[]{100.0, 200.0});

            TimeSeries result = AlignmentUtil.resample(ts, Duration.ofSeconds(10), FillStrategy.ZERO);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(4); // 0s, 10s, 20s, 30s
            assertThat(pts.get(0).value()).isEqualTo(100.0); // exact match
            assertThat(pts.get(1).value()).isEqualTo(0.0);   // zero fill
            assertThat(pts.get(2).value()).isEqualTo(0.0);   // zero fill
            assertThat(pts.get(3).value()).isEqualTo(200.0); // exact match
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            TimeSeries result = AlignmentUtil.resample(ts, Duration.ofSeconds(10), FillStrategy.LINEAR);

            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getName()).isEqualTo("empty_resampled");
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries ts = buildSeries("test", 1.0, 2.0);

            assertThatThrownBy(() -> AlignmentUtil.resample(ts, Duration.ZERO, FillStrategy.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
            assertThatThrownBy(() -> AlignmentUtil.resample(ts, Duration.ofMillis(-5), FillStrategy.ZERO))
                    .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        void shouldPreserveExactMatchValues() {
            // All points land on the grid
            TimeSeries ts = buildSeries("exact", 1.0, 2.0, 3.0);

            TimeSeries result = AlignmentUtil.resample(ts, Duration.ofSeconds(10), FillStrategy.LINEAR);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts).hasSize(3);
            assertThat(pts.get(0).value()).isEqualTo(1.0);
            assertThat(pts.get(1).value()).isEqualTo(2.0);
            assertThat(pts.get(2).value()).isEqualTo(3.0);
        }
    }

    @Nested
    class AlignToGridTest {

        @Test
        void shouldAggregateWithAvg() {
            // Put multiple points in a 60-second bucket
            TimeSeries ts = buildSeriesAt("cpu",
                    new long[]{0, 10, 20, 60, 70},
                    new double[]{10.0, 20.0, 30.0, 40.0, 50.0});

            TimeSeries result = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.AVG);

            List<DataPoint> pts = result.getPoints();
            // Bucket [0,60): values 10, 20, 30 -> avg = 20.0
            // Bucket [60,120): values 40, 50 -> avg = 45.0
            assertThat(pts).hasSize(2);
            assertThat(pts.get(0).value()).isCloseTo(20.0, within(0.001));
            assertThat(pts.get(1).value()).isCloseTo(45.0, within(0.001));
        }

        @Test
        void shouldAggregateWithSum() {
            TimeSeries ts = buildSeriesAt("requests",
                    new long[]{0, 10, 20, 60, 70},
                    new double[]{1.0, 2.0, 3.0, 4.0, 5.0});

            TimeSeries result = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.SUM);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts.get(0).value()).isCloseTo(6.0, within(0.001));  // 1+2+3
            assertThat(pts.get(1).value()).isCloseTo(9.0, within(0.001));  // 4+5
        }

        @Test
        void shouldAggregateWithMinMax() {
            TimeSeries ts = buildSeriesAt("temp",
                    new long[]{0, 10, 20},
                    new double[]{15.0, 25.0, 10.0});

            TimeSeries minResult = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.MIN);
            TimeSeries maxResult = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.MAX);

            assertThat(minResult.getPoints().getFirst().value()).isEqualTo(10.0);
            assertThat(maxResult.getPoints().getFirst().value()).isEqualTo(25.0);
        }

        @Test
        void shouldAggregateWithCount() {
            TimeSeries ts = buildSeriesAt("events",
                    new long[]{0, 5, 10, 60},
                    new double[]{1.0, 2.0, 3.0, 4.0});

            TimeSeries result = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.COUNT);

            List<DataPoint> pts = result.getPoints();
            assertThat(pts.get(0).value()).isEqualTo(3.0); // 3 points in first bucket
            assertThat(pts.get(1).value()).isEqualTo(1.0); // 1 point in second bucket
        }

        @Test
        void shouldReturnEmptyForEmptySeries() {
            TimeSeries ts = new TimeSeries("empty");

            TimeSeries result = AlignmentUtil.alignToGrid(ts, Duration.ofSeconds(60), AggregationType.AVG);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        void shouldThrowOnNonPositiveInterval() {
            TimeSeries ts = buildSeries("test", 1.0);

            assertThatThrownBy(() -> AlignmentUtil.alignToGrid(ts, Duration.ZERO, AggregationType.SUM))
                    .isInstanceOf(TimeSeriesException.class);
        }
    }
}
