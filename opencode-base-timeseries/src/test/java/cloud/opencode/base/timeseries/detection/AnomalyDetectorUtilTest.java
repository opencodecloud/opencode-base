package cloud.opencode.base.timeseries.detection;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AnomalyDetectorUtilTest Tests
 * AnomalyDetectorUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("AnomalyDetectorUtil Tests")
class AnomalyDetectorUtilTest {

    private TimeSeries series;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
        series = new TimeSeries("test-series");

        // Create series with mostly normal values and some anomalies
        for (int i = 0; i < 100; i++) {
            double value = 50.0 + Math.sin(i * 0.1) * 5; // Normal range: ~45-55
            // Add anomalies at specific indices
            if (i == 25) value = 100.0; // High anomaly
            if (i == 75) value = 0.0;   // Low anomaly
            series.add(baseTime.plusSeconds(i), value);
        }
    }

    @Nested
    @DisplayName("Z-Score Detection Tests")
    class ZScoreDetectionTests {

        @Test
        @DisplayName("detectByZScore should find anomalies")
        void detectByZScoreShouldFindAnomalies() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(series, 2.0);

            assertThat(anomalies).isNotEmpty();
        }

        @Test
        @DisplayName("detectByZScore with high threshold should find fewer anomalies")
        void detectByZScoreWithHighThresholdShouldFindFewerAnomalies() {
            List<DataPoint> lowThreshold = AnomalyDetectorUtil.detectByZScore(series, 2.0);
            List<DataPoint> highThreshold = AnomalyDetectorUtil.detectByZScore(series, 4.0);

            assertThat(highThreshold.size()).isLessThanOrEqualTo(lowThreshold.size());
        }

        @Test
        @DisplayName("detectByZScore should return empty for uniform data")
        void detectByZScoreShouldReturnEmptyForUniformData() {
            TimeSeries uniform = new TimeSeries("uniform");
            for (int i = 0; i < 100; i++) {
                uniform.add(baseTime.plusSeconds(i), 50.0);
            }

            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(uniform, 2.0);

            assertThat(anomalies).isEmpty();
        }
    }

    @Nested
    @DisplayName("IQR Detection Tests")
    class IQRDetectionTests {

        @Test
        @DisplayName("detectByIQR should find outliers")
        void detectByIQRShouldFindOutliers() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByIQR(series, 1.5);

            assertThat(anomalies).isNotEmpty();
        }

        @Test
        @DisplayName("detectByIQR with high multiplier should find fewer outliers")
        void detectByIQRWithHighMultiplierShouldFindFewerOutliers() {
            List<DataPoint> lowMultiplier = AnomalyDetectorUtil.detectByIQR(series, 1.5);
            List<DataPoint> highMultiplier = AnomalyDetectorUtil.detectByIQR(series, 3.0);

            assertThat(highMultiplier.size()).isLessThanOrEqualTo(lowMultiplier.size());
        }
    }

    @Nested
    @DisplayName("Moving Average Detection Tests")
    class MovingAverageDetectionTests {

        @Test
        @DisplayName("detectByMovingAverage should find deviations from trend")
        void detectByMovingAverageShouldFindDeviationsFromTrend() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByMovingAverage(series, 10, 20.0);

            assertThat(anomalies).isNotEmpty();
        }

        @Test
        @DisplayName("detectByMovingAverage with larger window should smooth more")
        void detectByMovingAverageWithLargerWindowShouldSmoothMore() {
            List<DataPoint> smallWindow = AnomalyDetectorUtil.detectByMovingAverage(series, 5, 20.0);
            List<DataPoint> largeWindow = AnomalyDetectorUtil.detectByMovingAverage(series, 20, 20.0);

            // Both should detect anomalies
            assertThat(smallWindow).isNotEmpty();
            assertThat(largeWindow).isNotEmpty();
        }

        @Test
        @DisplayName("detectByMovingAverage should return empty for series shorter than window")
        void detectByMovingAverageShouldReturnEmptyForSeriesShorterThanWindow() {
            TimeSeries shortSeries = new TimeSeries("short");
            for (int i = 0; i < 5; i++) {
                shortSeries.add(baseTime.plusSeconds(i), 50.0);
            }

            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByMovingAverage(shortSeries, 10, 2.0);

            assertThat(anomalies).isEmpty();
        }
    }

    @Nested
    @DisplayName("Standard Deviation Detection Tests")
    class StdDevDetectionTests {

        @Test
        @DisplayName("detectByStdDev should find values beyond threshold")
        void detectByStdDevShouldFindValuesBeyondThreshold() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByStdDev(series, 2.0);

            assertThat(anomalies).isNotEmpty();
        }

        @Test
        @DisplayName("detectByStdDev should work same as detectByZScore")
        void detectByStdDevShouldWorkSameAsDetectByZScore() {
            List<DataPoint> stdDevAnomalies = AnomalyDetectorUtil.detectByStdDev(series, 2.0);
            List<DataPoint> zScoreAnomalies = AnomalyDetectorUtil.detectByZScore(series, 2.0);

            assertThat(stdDevAnomalies).hasSize(zScoreAnomalies.size());
        }
    }

    @Nested
    @DisplayName("Spike Detection Tests")
    class SpikeDetectionTests {

        @Test
        @DisplayName("detectSpikes should find sudden value changes")
        void detectSpikesShouldFindSuddenValueChanges() {
            TimeSeries spikey = new TimeSeries("spikey");
            for (int i = 0; i < 100; i++) {
                double value = 50.0;
                if (i == 50) value = 150.0; // Spike
                spikey.add(baseTime.plusSeconds(i), value);
            }

            List<DataPoint> spikes = AnomalyDetectorUtil.detectSpikes(spikey, 0.5);

            assertThat(spikes).isNotEmpty();
        }

        @Test
        @DisplayName("detectSpikes should return empty for smooth series")
        void detectSpikesShouldReturnEmptyForSmoothSeries() {
            TimeSeries smooth = new TimeSeries("smooth");
            for (int i = 0; i < 100; i++) {
                smooth.add(baseTime.plusSeconds(i), 50.0 + i * 0.01);
            }

            List<DataPoint> spikes = AnomalyDetectorUtil.detectSpikes(smooth, 0.5);

            assertThat(spikes).isEmpty();
        }

        @Test
        @DisplayName("detectSpikes should handle single point series")
        void detectSpikesShouldHandleSinglePointSeries() {
            TimeSeries single = new TimeSeries("single");
            single.add(baseTime, 50.0);

            List<DataPoint> spikes = AnomalyDetectorUtil.detectSpikes(single, 0.5);

            assertThat(spikes).isEmpty();
        }
    }

    @Nested
    @DisplayName("Out of Range Detection Tests")
    class OutOfRangeDetectionTests {

        @Test
        @DisplayName("detectOutOfRange should find values outside bounds")
        void detectOutOfRangeShouldFindValuesOutsideBounds() {
            List<DataPoint> outOfRange = AnomalyDetectorUtil.detectOutOfRange(series, 40.0, 60.0);

            assertThat(outOfRange).isNotEmpty();
        }

        @Test
        @DisplayName("detectOutOfRange should return empty for values in range")
        void detectOutOfRangeShouldReturnEmptyForValuesInRange() {
            List<DataPoint> outOfRange = AnomalyDetectorUtil.detectOutOfRange(series, 0.0, 200.0);

            assertThat(outOfRange).isEmpty();
        }

        @Test
        @DisplayName("detectOutOfRange should detect both high and low anomalies")
        void detectOutOfRangeShouldDetectBothHighAndLowAnomalies() {
            List<DataPoint> outOfRange = AnomalyDetectorUtil.detectOutOfRange(series, 10.0, 90.0);

            // Should detect both the 0.0 and 100.0 anomalies
            assertThat(outOfRange.size()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Get Summary Tests")
    class GetSummaryTests {

        @Test
        @DisplayName("getSummary should return anomaly statistics")
        void getSummaryShouldReturnAnomalyStatistics() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(series, 2.0);

            String summary = AnomalyDetectorUtil.getSummary(anomalies, series.size());

            assertThat(summary).isNotBlank();
            assertThat(summary).containsIgnoringCase("anomal");
        }

        @Test
        @DisplayName("getSummary should handle empty anomaly list")
        void getSummaryShouldHandleEmptyAnomalyList() {
            String summary = AnomalyDetectorUtil.getSummary(List.of(), series.size());

            assertThat(summary).isNotBlank();
            assertThat(summary).contains("No anomalies");
        }

        @Test
        @DisplayName("getSummary should include percentage")
        void getSummaryShouldIncludePercentage() {
            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(series, 2.0);

            String summary = AnomalyDetectorUtil.getSummary(anomalies, series.size());

            assertThat(summary).contains("%");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty series")
        void shouldHandleEmptySeries() {
            TimeSeries empty = new TimeSeries("empty");

            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(empty, 2.0);

            assertThat(anomalies).isEmpty();
        }

        @Test
        @DisplayName("should handle single value series")
        void shouldHandleSingleValueSeries() {
            TimeSeries single = new TimeSeries("single");
            single.add(baseTime, 50.0);

            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(single, 2.0);

            assertThat(anomalies).isEmpty();
        }

        @Test
        @DisplayName("should handle series with all same values")
        void shouldHandleSeriesWithAllSameValues() {
            TimeSeries constant = new TimeSeries("constant");
            for (int i = 0; i < 100; i++) {
                constant.add(baseTime.plusSeconds(i), 50.0);
            }

            List<DataPoint> anomalies = AnomalyDetectorUtil.detectByZScore(constant, 2.0);

            assertThat(anomalies).isEmpty();
        }

        @Test
        @DisplayName("should handle series with NaN values")
        void shouldHandleSeriesWithNaNValues() {
            TimeSeries withNaN = new TimeSeries("with-nan");
            for (int i = 0; i < 100; i++) {
                double value = i % 10 == 0 ? Double.NaN : 50.0;
                withNaN.add(baseTime.plusSeconds(i), value);
            }

            // Should not throw
            assertThatCode(() ->
                AnomalyDetectorUtil.detectByZScore(withNaN, 2.0)
            ).doesNotThrowAnyException();
        }
    }
}
