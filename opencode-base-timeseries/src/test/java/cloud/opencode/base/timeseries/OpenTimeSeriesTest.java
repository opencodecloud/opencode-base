package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenTimeSeriesTest Tests
 * OpenTimeSeriesTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("OpenTimeSeries Tests")
class OpenTimeSeriesTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
        OpenTimeSeries.clearAll();
    }

    @AfterEach
    void tearDown() {
        OpenTimeSeries.clearAll();
    }

    @Nested
    @DisplayName("Create and Get Tests")
    class CreateAndGetTests {

        @Test
        @DisplayName("create should create new time series")
        void createShouldCreateNewTimeSeries() {
            TimeSeries series = OpenTimeSeries.create("test-series");

            assertThat(series).isNotNull();
            assertThat(series.getName()).isEqualTo("test-series");
        }

        @Test
        @DisplayName("get should return or create series")
        void getShouldReturnOrCreateSeries() {
            TimeSeries series = OpenTimeSeries.get("test-series");

            assertThat(series).isNotNull();
            assertThat(series.getName()).isEqualTo("test-series");
        }

        @Test
        @DisplayName("get should return same instance on subsequent calls")
        void getShouldReturnSameInstanceOnSubsequentCalls() {
            TimeSeries series1 = OpenTimeSeries.get("test-series");
            TimeSeries series2 = OpenTimeSeries.get("test-series");

            assertThat(series1).isSameAs(series2);
        }

        @Test
        @DisplayName("of should create time series with points")
        void ofShouldCreateTimeSeriesWithPoints() {
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 1.0),
                DataPoint.of(baseTime.plusSeconds(1), 2.0)
            );

            TimeSeries series = OpenTimeSeries.of("test-series", points);

            assertThat(series).isNotNull();
            assertThat(series.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("bounded should create bounded time series")
        void boundedShouldCreateBoundedTimeSeries() {
            BoundedTimeSeries series = OpenTimeSeries.bounded("bounded-series", 100, Duration.ofHours(1));

            assertThat(series).isNotNull();
            assertThat(series.getMaxSize()).isEqualTo(100);
            assertThat(series.getMaxAge()).isEqualTo(Duration.ofHours(1));
        }
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("record should add data point to series")
        void recordShouldAddDataPointToSeries() {
            OpenTimeSeries.record("test-series", baseTime, 42.0);

            TimeSeries series = OpenTimeSeries.get("test-series");
            assertThat(series.size()).isEqualTo(1);
            assertThat(series.get(baseTime).map(DataPoint::value)).contains(42.0);
        }

        @Test
        @DisplayName("record with current time should add data point")
        void recordWithCurrentTimeShouldAddDataPoint() {
            Instant before = Instant.now();
            OpenTimeSeries.record("test-series", 42.0);
            Instant after = Instant.now();

            TimeSeries series = OpenTimeSeries.get("test-series");
            assertThat(series.size()).isEqualTo(1);

            DataPoint latest = series.getLatest();
            assertThat(latest).isNotNull();
            assertThat(latest.timestamp()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("Query Tests")
    class QueryTests {

        @Test
        @DisplayName("query should return data points for duration")
        void queryShouldReturnDataPointsForDuration() {
            for (int i = 0; i < 10; i++) {
                OpenTimeSeries.record("test-series", baseTime.plusSeconds(i), i * 1.0);
            }

            List<DataPoint> points = OpenTimeSeries.query("test-series", Duration.ofSeconds(100));

            assertThat(points).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Stats Tests")
    class StatsTests {

        @Test
        @DisplayName("stats should return series statistics")
        void statsShouldReturnSeriesStatistics() {
            for (int i = 1; i <= 5; i++) {
                OpenTimeSeries.record("test-series", baseTime.plusSeconds(i), i * 10.0);
            }

            TimeSeriesStats stats = OpenTimeSeries.stats("test-series");

            assertThat(stats.count()).isEqualTo(5);
            assertThat(stats.sum()).isEqualTo(150.0);
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("exists should return true for existing series")
        void existsShouldReturnTrueForExistingSeries() {
            OpenTimeSeries.get("test-series");

            assertThat(OpenTimeSeries.exists("test-series")).isTrue();
        }

        @Test
        @DisplayName("exists should return false for non-existing series")
        void existsShouldReturnFalseForNonExistingSeries() {
            assertThat(OpenTimeSeries.exists("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("remove should delete series")
        void removeShouldDeleteSeries() {
            OpenTimeSeries.get("test-series");
            OpenTimeSeries.remove("test-series");

            assertThat(OpenTimeSeries.exists("test-series")).isFalse();
        }

        @Test
        @DisplayName("getSeriesNames should return all series names")
        void getSeriesNamesShouldReturnAllSeriesNames() {
            OpenTimeSeries.get("series1");
            OpenTimeSeries.get("series2");
            OpenTimeSeries.get("series3");

            List<String> names = OpenTimeSeries.getSeriesNames();

            assertThat(names).contains("series1", "series2", "series3");
        }

        @Test
        @DisplayName("clearAll should remove all series")
        void clearAllShouldRemoveAllSeries() {
            OpenTimeSeries.get("series1");
            OpenTimeSeries.get("series2");

            OpenTimeSeries.clearAll();

            assertThat(OpenTimeSeries.exists("series1")).isFalse();
            assertThat(OpenTimeSeries.exists("series2")).isFalse();
        }

        @Test
        @DisplayName("cleanup should remove expired data")
        void cleanupShouldRemoveExpiredData() {
            OpenTimeSeries.record("test-series", baseTime.minus(Duration.ofDays(100)), 1.0);
            OpenTimeSeries.record("test-series", baseTime, 2.0);

            OpenTimeSeries.cleanup(Duration.ofDays(30));

            // Series should still exist
            assertThat(OpenTimeSeries.exists("test-series")).isTrue();
        }
    }

    @Nested
    @DisplayName("Point Factory Tests")
    class PointFactoryTests {

        @Test
        @DisplayName("point should create data point with timestamp and value")
        void pointShouldCreateDataPointWithTimestampAndValue() {
            DataPoint point = OpenTimeSeries.point(baseTime, 42.0);

            assertThat(point.timestamp()).isEqualTo(baseTime);
            assertThat(point.value()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("point should create data point with current time")
        void pointShouldCreateDataPointWithCurrentTime() {
            Instant before = Instant.now();
            DataPoint point = OpenTimeSeries.point(42.0);
            Instant after = Instant.now();

            assertThat(point.timestamp()).isBetween(before, after);
            assertThat(point.value()).isEqualTo(42.0);
        }
    }

    @Nested
    @DisplayName("Aggregation Utility Tests")
    class AggregationUtilityTests {

        @Test
        @DisplayName("downsample should reduce data points")
        void downsampleShouldReduceDataPoints() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries downsampled = OpenTimeSeries.downsample(series, Duration.ofSeconds(10));

            assertThat(downsampled.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("movingAverage should calculate moving average")
        void movingAverageShouldCalculateMovingAverage() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries ma = OpenTimeSeries.movingAverage(series, 10);

            assertThat(ma.size()).isLessThan(series.size());
        }

        @Test
        @DisplayName("ema should calculate exponential moving average")
        void emaShouldCalculateExponentialMovingAverage() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries ema = OpenTimeSeries.ema(series, 0.2);

            assertThat(ema.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("rollingStats should calculate rolling statistics")
        void rollingStatsShouldCalculateRollingStatistics() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            Map<String, TimeSeries> stats = OpenTimeSeries.rollingStats(series, 10);

            assertThat(stats).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Analysis Utility Tests")
    class AnalysisUtilityTests {

        @Test
        @DisplayName("detectAnomalies should find anomalies")
        void detectAnomaliesShouldFindAnomalies() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                double value = 50.0;
                if (i == 50) value = 200.0; // Anomaly
                series.add(baseTime.plusSeconds(i), value);
            }

            List<DataPoint> anomalies = OpenTimeSeries.detectAnomalies(series, 2.0);

            assertThat(anomalies).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Data Processing Tests")
    class DataProcessingTests {

        @Test
        @DisplayName("merge should combine multiple series")
        void mergeShouldCombineMultipleSeries() {
            TimeSeries series1 = new TimeSeries("series1");
            TimeSeries series2 = new TimeSeries("series2");
            series1.add(baseTime, 1.0);
            series2.add(baseTime.plusSeconds(1), 2.0);

            TimeSeries merged = OpenTimeSeries.merge(series1, series2);

            assertThat(merged.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("fill should replace NaN values")
        void fillShouldReplaceNaNValues() {
            TimeSeries series = new TimeSeries("test");
            series.add(baseTime, Double.NaN);
            series.add(baseTime.plusSeconds(1), 10.0);

            TimeSeries filled = OpenTimeSeries.fill(series, 0.0);

            assertThat(filled.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("normalize should scale values to 0-1 range")
        void normalizeShouldScaleValuesToRange() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 10; i++) {
                series.add(baseTime.plusSeconds(i), i * 10.0);
            }

            TimeSeries normalized = OpenTimeSeries.normalize(series);

            assertThat(normalized.min()).isPresent().hasValue(0.0);
            assertThat(normalized.max()).isPresent().hasValue(1.0);
        }

        @Test
        @DisplayName("standardize should scale values to zero mean")
        void standardizeShouldScaleValues() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries standardized = OpenTimeSeries.standardize(series);

            assertThat(standardized.average()).isPresent();
            assertThat(standardized.average().orElse(1.0)).isCloseTo(0.0, within(0.01));
        }
    }

    @Nested
    @DisplayName("Forecasting Tests")
    class ForecastingTests {

        @Test
        @DisplayName("smaForecast should forecast with simple moving average")
        void smaForecastShouldForecast() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), 50.0 + Math.sin(i * 0.1) * 10);
            }

            TimeSeries forecast = OpenTimeSeries.smaForecast(series, 10, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("emaForecast should forecast with exponential moving average")
        void emaForecastShouldForecast() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), 50.0 + Math.sin(i * 0.1) * 10);
            }

            TimeSeries forecast = OpenTimeSeries.emaForecast(series, 0.2, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }

        @Test
        @DisplayName("linearForecast should forecast with linear regression")
        void linearForecastShouldForecast() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries forecast = OpenTimeSeries.linearForecast(series, 10);

            assertThat(forecast.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("holtForecast should forecast with Holt's method")
        void holtForecastShouldForecast() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0 + Math.sin(i * 0.1) * 5);
            }

            TimeSeries forecast = OpenTimeSeries.holtForecast(series, 0.3, 0.1, 5);

            assertThat(forecast.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Downsampling Algorithm Tests")
    class DownsamplingAlgorithmTests {

        @Test
        @DisplayName("lttb should downsample using LTTB algorithm")
        void lttbShouldDownsampleUsingLttbAlgorithm() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 1000; i++) {
                series.add(baseTime.plusSeconds(i), Math.sin(i * 0.01) * 100);
            }

            TimeSeries downsampled = OpenTimeSeries.lttb(series, 100);

            assertThat(downsampled.size()).isLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("m4 should downsample using M4 algorithm")
        void m4ShouldDownsampleUsingM4Algorithm() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 1000; i++) {
                series.add(baseTime.plusSeconds(i), Math.sin(i * 0.01) * 100);
            }

            TimeSeries downsampled = OpenTimeSeries.m4(series, Duration.ofSeconds(100));

            assertThat(downsampled.size()).isLessThan(series.size());
        }

        @Test
        @DisplayName("peakPreserving should preserve peaks and valleys")
        void peakPreservingShouldPreservePeaksAndValleys() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 1000; i++) {
                series.add(baseTime.plusSeconds(i), Math.sin(i * 0.01) * 100);
            }

            TimeSeries downsampled = OpenTimeSeries.peakPreserving(series, 100);

            assertThat(downsampled.size()).isLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("percentile should downsample using percentile")
        void percentileShouldDownsampleUsingPercentile() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 1000; i++) {
                series.add(baseTime.plusSeconds(i), i * 1.0);
            }

            TimeSeries downsampled = OpenTimeSeries.percentile(series, Duration.ofSeconds(100), 50);

            assertThat(downsampled.size()).isLessThan(series.size());
        }

        @Test
        @DisplayName("thresholdDownsample should keep only significant changes")
        void thresholdDownsampleShouldKeepOnlySignificantChanges() {
            TimeSeries series = new TimeSeries("test");
            for (int i = 0; i < 100; i++) {
                double value = i % 10 == 0 ? i * 10.0 : 50.0;
                series.add(baseTime.plusSeconds(i), value);
            }

            TimeSeries downsampled = OpenTimeSeries.thresholdDownsample(series, 10.0);

            assertThat(downsampled.size()).isLessThan(series.size());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty series operations")
        void shouldHandleEmptySeriesOperations() {
            OpenTimeSeries.get("empty");

            TimeSeriesStats stats = OpenTimeSeries.stats("empty");

            assertThat(stats.count()).isZero();
        }

        @Test
        @DisplayName("merge with no series should return empty")
        void mergeWithNoSeriesShouldReturnEmpty() {
            TimeSeries merged = OpenTimeSeries.merge();

            assertThat(merged.isEmpty()).isTrue();
        }
    }
}
