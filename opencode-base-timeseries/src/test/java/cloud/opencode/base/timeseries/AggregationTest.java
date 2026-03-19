package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AggregationTest Tests
 * AggregationTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("Aggregation Tests")
class AggregationTest {

    private TimeSeries series;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        series = new TimeSeries("test-series");
        baseTime = Instant.parse("2024-01-01T00:00:00Z");

        // Add 100 data points over 100 seconds
        for (int i = 0; i < 100; i++) {
            series.add(baseTime.plusSeconds(i), i * 1.0);
        }
    }

    @Nested
    @DisplayName("Function Enum Tests")
    class FunctionEnumTests {

        @Test
        @DisplayName("should have all expected function values")
        void shouldHaveAllExpectedFunctionValues() {
            Aggregation.Function[] functions = Aggregation.Function.values();

            assertThat(functions).contains(
                Aggregation.Function.SUM,
                Aggregation.Function.AVG,
                Aggregation.Function.MIN,
                Aggregation.Function.MAX,
                Aggregation.Function.COUNT,
                Aggregation.Function.FIRST,
                Aggregation.Function.LAST
            );
        }

        @Test
        @DisplayName("valueOf should return correct function")
        void valueOfShouldReturnCorrectFunction() {
            assertThat(Aggregation.Function.valueOf("SUM")).isEqualTo(Aggregation.Function.SUM);
            assertThat(Aggregation.Function.valueOf("AVG")).isEqualTo(Aggregation.Function.AVG);
            assertThat(Aggregation.Function.valueOf("MIN")).isEqualTo(Aggregation.Function.MIN);
            assertThat(Aggregation.Function.valueOf("MAX")).isEqualTo(Aggregation.Function.MAX);
        }
    }

    @Nested
    @DisplayName("Downsample Tests")
    class DownsampleTests {

        @Test
        @DisplayName("downsample with SUM should aggregate values")
        void downsampleWithSumShouldAggregateValues() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.SUM);

            assertThat(downsampled.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("downsample with AVG should calculate average")
        void downsampleWithAvgShouldCalculateAverage() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.AVG);

            assertThat(downsampled.size()).isEqualTo(10);
            // First bucket: 0-9, average = 4.5
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(4.5);
        }

        @Test
        @DisplayName("downsample with MIN should return minimum")
        void downsampleWithMinShouldReturnMinimum() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.MIN);

            assertThat(downsampled.size()).isEqualTo(10);
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("downsample with MAX should return maximum")
        void downsampleWithMaxShouldReturnMaximum() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.MAX);

            assertThat(downsampled.size()).isEqualTo(10);
            // First bucket: 0-9, max = 9
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(9.0);
        }

        @Test
        @DisplayName("downsample with COUNT should return count")
        void downsampleWithCountShouldReturnCount() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.COUNT);

            assertThat(downsampled.size()).isEqualTo(10);
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(10.0);
        }

        @Test
        @DisplayName("downsample with FIRST should return first value")
        void downsampleWithFirstShouldReturnFirstValue() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.FIRST);

            assertThat(downsampled.size()).isEqualTo(10);
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("downsample with LAST should return last value")
        void downsampleWithLastShouldReturnLastValue() {
            TimeSeries downsampled = Aggregation.downsample(series, Duration.ofSeconds(10), Aggregation.Function.LAST);

            assertThat(downsampled.size()).isEqualTo(10);
            // First bucket: 0-9, last = 9
            assertThat(downsampled.getFirst().map(DataPoint::value)).contains(9.0);
        }
    }

    @Nested
    @DisplayName("Moving Average Tests")
    class MovingAverageTests {

        @Test
        @DisplayName("movingAverage should calculate simple moving average")
        void movingAverageShouldCalculateSimpleMovingAverage() {
            TimeSeries ma = Aggregation.movingAverage(series, 10);

            assertThat(ma.size()).isEqualTo(91); // 100 - 10 + 1
        }

        @Test
        @DisplayName("movingAverage with window 1 should return original values")
        void movingAverageWithWindow1ShouldReturnOriginalValues() {
            TimeSeries ma = Aggregation.movingAverage(series, 1);

            assertThat(ma.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("movingAverage should handle large window")
        void movingAverageShouldHandleLargeWindow() {
            TimeSeries ma = Aggregation.movingAverage(series, 50);

            assertThat(ma.size()).isEqualTo(51);
        }
    }

    @Nested
    @DisplayName("Exponential Moving Average Tests")
    class ExponentialMovingAverageTests {

        @Test
        @DisplayName("exponentialMovingAverage should calculate EMA")
        void exponentialMovingAverageShouldCalculateEma() {
            TimeSeries ema = Aggregation.exponentialMovingAverage(series, 0.2);

            assertThat(ema.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("exponentialMovingAverage with alpha 1 should return original values")
        void exponentialMovingAverageWithAlpha1ShouldReturnOriginalValues() {
            TimeSeries ema = Aggregation.exponentialMovingAverage(series, 1.0);

            assertThat(ema.size()).isEqualTo(100);
            assertThat(ema.getFirst().map(DataPoint::value)).contains(0.0);
            assertThat(ema.getLast().map(DataPoint::value)).contains(99.0);
        }

        @Test
        @DisplayName("exponentialMovingAverage with small alpha should be smoother")
        void exponentialMovingAverageWithSmallAlphaShouldBeSmoother() {
            TimeSeries ema = Aggregation.exponentialMovingAverage(series, 0.1);

            assertThat(ema.size()).isEqualTo(100);
            // EMA should lag behind the actual values
            assertThat(ema.getLast().map(DataPoint::value).orElse(0.0)).isLessThan(99.0);
        }
    }

    @Nested
    @DisplayName("Rolling Stats Tests")
    class RollingStatsTests {

        @Test
        @DisplayName("rollingStats should calculate rolling statistics")
        void rollingStatsShouldCalculateRollingStatistics() {
            Map<String, TimeSeries> stats = Aggregation.rollingStats(series, 10);

            assertThat(stats).containsKeys("min", "max", "avg", "std");
            assertThat(stats.get("min").size()).isEqualTo(91); // 100 - 10 + 1
            assertThat(stats.get("max").size()).isEqualTo(91);
            assertThat(stats.get("avg").size()).isEqualTo(91);
            assertThat(stats.get("std").size()).isEqualTo(91);
        }

        @Test
        @DisplayName("rollingStats min should return correct minimum values")
        void rollingStatsMinShouldReturnCorrectMinimumValues() {
            Map<String, TimeSeries> stats = Aggregation.rollingStats(series, 10);

            // First window (0-9): min = 0
            assertThat(stats.get("min").getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("rollingStats max should return correct maximum values")
        void rollingStatsMaxShouldReturnCorrectMaximumValues() {
            Map<String, TimeSeries> stats = Aggregation.rollingStats(series, 10);

            // First window (0-9): max = 9
            assertThat(stats.get("max").getFirst().map(DataPoint::value)).contains(9.0);
        }

        @Test
        @DisplayName("rollingStats avg should return correct average values")
        void rollingStatsAvgShouldReturnCorrectAverageValues() {
            Map<String, TimeSeries> stats = Aggregation.rollingStats(series, 10);

            // First window (0-9): avg = 4.5
            assertThat(stats.get("avg").getFirst().map(DataPoint::value)).contains(4.5);
        }

        @Test
        @DisplayName("rollingStats should have correct window size output")
        void rollingStatsShouldHaveCorrectWindowSizeOutput() {
            Map<String, TimeSeries> stats = Aggregation.rollingStats(series, 5);

            // 100 points with window 5 = 96 windows
            assertThat(stats.get("avg").size()).isEqualTo(96);
        }
    }

    @Nested
    @DisplayName("Empty Series Tests")
    class EmptySeriesTests {

        @Test
        @DisplayName("downsample on empty series should return empty series")
        void downsampleOnEmptySeriesShouldReturnEmptySeries() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries result = Aggregation.downsample(empty, Duration.ofSeconds(10), Aggregation.Function.SUM);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("movingAverage on empty series should return empty series")
        void movingAverageOnEmptySeriesShouldReturnEmptySeries() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries result = Aggregation.movingAverage(empty, 10);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("rollingStats on empty series should return empty map values")
        void rollingStatsOnEmptySeriesShouldReturnEmptyMapValues() {
            TimeSeries empty = new TimeSeries("empty");
            Map<String, TimeSeries> result = Aggregation.rollingStats(empty, 10);

            assertThat(result.get("min").isEmpty()).isTrue();
            assertThat(result.get("max").isEmpty()).isTrue();
            assertThat(result.get("avg").isEmpty()).isTrue();
            assertThat(result.get("std").isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("downsample with window larger than series should aggregate all")
        void downsampleWithWindowLargerThanSeriesShouldAggregateAll() {
            TimeSeries result = Aggregation.downsample(series, Duration.ofHours(1), Aggregation.Function.SUM);

            assertThat(result.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle series with single point")
        void shouldHandleSeriesWithSinglePoint() {
            TimeSeries single = new TimeSeries("single");
            single.add(baseTime, 42.0);

            TimeSeries result = Aggregation.downsample(single, Duration.ofSeconds(10), Aggregation.Function.AVG);

            assertThat(result.size()).isEqualTo(1);
            assertThat(result.getFirst().map(DataPoint::value)).contains(42.0);
        }
    }
}
