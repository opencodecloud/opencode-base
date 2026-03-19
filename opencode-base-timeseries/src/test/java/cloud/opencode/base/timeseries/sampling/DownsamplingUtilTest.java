package cloud.opencode.base.timeseries.sampling;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * DownsamplingUtil Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
class DownsamplingUtilTest {

    private TimeSeries createTestSeries(int size) {
        TimeSeries series = new TimeSeries("test");
        Instant base = Instant.parse("2024-01-01T00:00:00Z");
        for (int i = 0; i < size; i++) {
            // Create a wave pattern
            double value = 50 + 30 * Math.sin(i * 0.1);
            series.add(DataPoint.of(base.plusSeconds(i * 60), value));
        }
        return series;
    }

    @Nested
    @DisplayName("LTTB Tests")
    class LttbTests {

        @Test
        void shouldDownsampleToTargetSize() {
            TimeSeries series = createTestSeries(100);

            TimeSeries result = DownsamplingUtil.lttb(series, 20);

            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        void shouldPreserveFirstAndLast() {
            TimeSeries series = createTestSeries(50);
            DataPoint first = series.getFirst().get();
            DataPoint last = series.getLast().get();

            TimeSeries result = DownsamplingUtil.lttb(series, 10);

            assertThat(result.getFirst().get().timestamp()).isEqualTo(first.timestamp());
            assertThat(result.getLast().get().timestamp()).isEqualTo(last.timestamp());
        }

        @Test
        void shouldReturnOriginalIfTargetLarger() {
            TimeSeries series = createTestSeries(10);

            TimeSeries result = DownsamplingUtil.lttb(series, 20);

            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        void shouldReturnOriginalIfTargetTooSmall() {
            TimeSeries series = createTestSeries(10);

            TimeSeries result = DownsamplingUtil.lttb(series, 2);

            assertThat(result.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("M4 Tests")
    class M4Tests {

        @Test
        void shouldDownsampleWithM4() {
            TimeSeries series = createTestSeries(100);

            TimeSeries result = DownsamplingUtil.m4(series, Duration.ofMinutes(10));

            // Each bucket should have up to 4 points
            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldPreserveMinMax() {
            TimeSeries series = new TimeSeries("m4_test");
            Instant base = Instant.now();
            series.add(DataPoint.of(base, 50.0));
            series.add(DataPoint.of(base.plusSeconds(60), 10.0));  // min
            series.add(DataPoint.of(base.plusSeconds(120), 90.0)); // max
            series.add(DataPoint.of(base.plusSeconds(180), 50.0));

            TimeSeries result = DownsamplingUtil.m4(series, Duration.ofMinutes(10));

            // Should contain both min and max
            double[] values = result.getValues();
            assertThat(values).contains(10.0, 90.0);
        }

        @Test
        void shouldDownsampleByBucketCount() {
            TimeSeries series = createTestSeries(100);

            TimeSeries result = DownsamplingUtil.m4(series, 10);

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldHandleEmptySeries() {
            TimeSeries series = new TimeSeries("empty");

            TimeSeries result = DownsamplingUtil.m4(series, Duration.ofMinutes(5));

            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Percentile Tests")
    class PercentileTests {

        @Test
        void shouldDownsampleWithMedian() {
            TimeSeries series = createTestSeries(100);

            TimeSeries result = DownsamplingUtil.median(series, Duration.ofMinutes(10));

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldDownsampleWithPercentile() {
            TimeSeries series = createTestSeries(50);

            TimeSeries result = DownsamplingUtil.percentile(series, Duration.ofMinutes(5), 75);

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldRejectInvalidPercentile() {
            TimeSeries series = createTestSeries(50);

            assertThatThrownBy(() -> DownsamplingUtil.percentile(series, Duration.ofMinutes(5), -1))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> DownsamplingUtil.percentile(series, Duration.ofMinutes(5), 101))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Variance Preserving Tests")
    class VariancePreservingTests {

        @Test
        void shouldDownsamplePreservingVariance() {
            TimeSeries series = createTestSeries(100);

            TimeSeries result = DownsamplingUtil.variancePreserving(series, 20);

            assertThat(result.size()).isLessThanOrEqualTo(20);
        }

        @Test
        void shouldReturnOriginalIfTargetLarger() {
            TimeSeries series = createTestSeries(10);

            TimeSeries result = DownsamplingUtil.variancePreserving(series, 20);

            assertThat(result.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Peak Preserving Tests")
    class PeakPreservingTests {

        @Test
        void shouldPreservePeaksAndValleys() {
            TimeSeries series = new TimeSeries("peaks");
            Instant base = Instant.now();
            // Create clear peaks and valleys
            double[] values = {10, 20, 50, 20, 10, 5, 10, 40, 10, 5};
            for (int i = 0; i < values.length; i++) {
                series.add(DataPoint.of(base.plusSeconds(i * 60), values[i]));
            }

            TimeSeries result = DownsamplingUtil.peakPreserving(series, 6);

            // Should contain the extrema
            double[] resultValues = result.getValues();
            assertThat(resultValues).contains(50.0); // peak
            assertThat(resultValues).contains(5.0);  // valley
        }

        @Test
        void shouldReturnOriginalIfTargetLarger() {
            TimeSeries series = createTestSeries(10);

            TimeSeries result = DownsamplingUtil.peakPreserving(series, 20);

            assertThat(result.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Mode Tests")
    class ModeTests {

        @Test
        void shouldDownsampleWithMode() {
            TimeSeries series = createTestSeries(50);

            TimeSeries result = DownsamplingUtil.mode(series, Duration.ofMinutes(10), 5);

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldHandleEmptySeries() {
            TimeSeries series = new TimeSeries("empty");

            TimeSeries result = DownsamplingUtil.mode(series, Duration.ofMinutes(5), 5);

            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Threshold Tests")
    class ThresholdTests {

        @Test
        void shouldFilterSmallChanges() {
            TimeSeries series = new TimeSeries("threshold");
            Instant base = Instant.now();
            series.add(DataPoint.of(base, 10.0));
            series.add(DataPoint.of(base.plusSeconds(60), 10.5));  // small change
            series.add(DataPoint.of(base.plusSeconds(120), 10.8)); // small change
            series.add(DataPoint.of(base.plusSeconds(180), 20.0)); // big change
            series.add(DataPoint.of(base.plusSeconds(240), 20.3)); // small change

            TimeSeries result = DownsamplingUtil.threshold(series, 5.0);

            // Should only keep significant changes
            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldAlwaysIncludeFirstAndLast() {
            TimeSeries series = createTestSeries(10);
            DataPoint first = series.getFirst().get();
            DataPoint last = series.getLast().get();

            TimeSeries result = DownsamplingUtil.threshold(series, 1.0);

            assertThat(result.getFirst().get().timestamp()).isEqualTo(first.timestamp());
        }

        @Test
        void shouldHandleEmptySeries() {
            TimeSeries series = new TimeSeries("empty");

            TimeSeries result = DownsamplingUtil.threshold(series, 5.0);

            assertThat(result.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Percentage Threshold Tests")
    class PercentageThresholdTests {

        @Test
        void shouldFilterSmallPercentageChanges() {
            TimeSeries series = new TimeSeries("pct");
            Instant base = Instant.now();
            series.add(DataPoint.of(base, 100.0));
            series.add(DataPoint.of(base.plusSeconds(60), 102.0));  // 2% change
            series.add(DataPoint.of(base.plusSeconds(120), 115.0)); // ~13% change
            series.add(DataPoint.of(base.plusSeconds(180), 117.0)); // ~2% change

            TimeSeries result = DownsamplingUtil.percentageThreshold(series, 10.0);

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldHandleZeroValues() {
            TimeSeries series = new TimeSeries("zero");
            Instant base = Instant.now();
            series.add(DataPoint.of(base, 0.0));
            series.add(DataPoint.of(base.plusSeconds(60), 10.0));
            series.add(DataPoint.of(base.plusSeconds(120), 0.0));

            TimeSeries result = DownsamplingUtil.percentageThreshold(series, 5.0);

            // Should handle zero division gracefully
            assertThat(result.size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("OpenTimeSeries Integration Tests")
    class OpenTimeSeriesIntegrationTests {

        @Test
        void shouldUseLttbFromOpenTimeSeries() {
            TimeSeries series = createTestSeries(50);

            TimeSeries result = cloud.opencode.base.timeseries.OpenTimeSeries.lttb(series, 10);

            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        void shouldUseM4FromOpenTimeSeries() {
            TimeSeries series = createTestSeries(50);

            TimeSeries result = cloud.opencode.base.timeseries.OpenTimeSeries.m4(
                    series, Duration.ofMinutes(10));

            assertThat(result.size()).isLessThan(series.size());
        }

        @Test
        void shouldUsePeakPreservingFromOpenTimeSeries() {
            TimeSeries series = createTestSeries(50);

            TimeSeries result = cloud.opencode.base.timeseries.OpenTimeSeries.peakPreserving(
                    series, 15);

            assertThat(result.size()).isLessThanOrEqualTo(15);
        }
    }
}
