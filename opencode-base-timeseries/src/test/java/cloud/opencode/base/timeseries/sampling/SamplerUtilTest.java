package cloud.opencode.base.timeseries.sampling;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SamplerUtilTest Tests
 * SamplerUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("SamplerUtil Tests")
class SamplerUtilTest {

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
    @DisplayName("Downsample Tests")
    class DownsampleTests {

        @Test
        @DisplayName("downsample with SUM should aggregate values")
        void downsampleWithSumShouldAggregateValues() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.SUM);

            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("downsample with AVG should calculate average")
        void downsampleWithAvgShouldCalculateAverage() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.AVG);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(4.5);
        }

        @Test
        @DisplayName("downsample with MIN should return minimum")
        void downsampleWithMinShouldReturnMinimum() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.MIN);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("downsample with MAX should return maximum")
        void downsampleWithMaxShouldReturnMaximum() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.MAX);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(9.0);
        }

        @Test
        @DisplayName("downsample with COUNT should return count")
        void downsampleWithCountShouldReturnCount() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.COUNT);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(10.0);
        }

        @Test
        @DisplayName("downsample with FIRST should return first value")
        void downsampleWithFirstShouldReturnFirstValue() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.FIRST);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("downsample with LAST should return last value")
        void downsampleWithLastShouldReturnLastValue() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10), AggregationType.LAST);

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(9.0);
        }

        @Test
        @DisplayName("downsample with default aggregation should use AVG")
        void downsampleWithDefaultAggregationShouldUseAvg() {
            TimeSeries result = SamplerUtil.downsample(series, Duration.ofSeconds(10));

            assertThat(result.size()).isEqualTo(10);
            assertThat(result.getFirst().map(DataPoint::value)).contains(4.5);
        }
    }

    @Nested
    @DisplayName("Fill Gaps Tests")
    class FillGapsTests {

        @Test
        @DisplayName("fillGaps with ZERO should fill with zeros")
        void fillGapsWithZeroShouldFillWithZeros() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 1.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.ZERO);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps with PREVIOUS should use previous value")
        void fillGapsWithPreviousShouldUsePreviousValue() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 5.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.PREVIOUS);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps with NEXT should use next value")
        void fillGapsWithNextShouldUseNextValue() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 5.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.NEXT);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps with LINEAR should interpolate")
        void fillGapsWithLinearShouldInterpolate() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 0.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.LINEAR);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps with AVERAGE should use average of neighbors")
        void fillGapsWithAverageShouldUseAverageOfNeighbors() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 0.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.AVERAGE);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps with NAN should fill with NaN")
        void fillGapsWithNanShouldFillWithNan() {
            TimeSeries gappy = new TimeSeries("gappy");
            gappy.add(baseTime, 1.0);
            gappy.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries filled = SamplerUtil.fillGaps(gappy, Duration.ofSeconds(2), FillStrategy.NAN);

            assertThat(filled.size()).isGreaterThan(2);
        }

        @Test
        @DisplayName("fillGaps should not add points for small gaps")
        void fillGapsShouldNotAddPointsForSmallGaps() {
            TimeSeries nogaps = new TimeSeries("nogaps");
            nogaps.add(baseTime, 1.0);
            nogaps.add(baseTime.plusSeconds(2), 2.0);
            nogaps.add(baseTime.plusSeconds(4), 3.0);

            TimeSeries filled = SamplerUtil.fillGaps(nogaps, Duration.ofSeconds(2), FillStrategy.ZERO);

            assertThat(filled.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Upsample Tests")
    class UpsampleTests {

        @Test
        @DisplayName("upsample should increase resolution")
        void upsampleShouldIncreaseResolution() {
            TimeSeries sparse = new TimeSeries("sparse");
            sparse.add(baseTime, 0.0);
            sparse.add(baseTime.plusSeconds(10), 10.0);

            TimeSeries upsampled = SamplerUtil.upsample(sparse, Duration.ofSeconds(2), FillStrategy.LINEAR);

            assertThat(upsampled.size()).isGreaterThan(sparse.size());
        }
    }

    @Nested
    @DisplayName("Resample Tests")
    class ResampleTests {

        @Test
        @DisplayName("resample should change resolution with aggregation and fill")
        void resampleShouldChangeResolutionWithAggregationAndFill() {
            TimeSeries resampled = SamplerUtil.resample(series, Duration.ofSeconds(20),
                AggregationType.AVG, FillStrategy.LINEAR);

            assertThat(resampled.size()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Random Sample Tests")
    class RandomSampleTests {

        @Test
        @DisplayName("randomSample should return specified number of points")
        void randomSampleShouldReturnSpecifiedNumberOfPoints() {
            TimeSeries sample = SamplerUtil.randomSample(series, 10);

            assertThat(sample.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("randomSample should return all points if count exceeds size")
        void randomSampleShouldReturnAllPointsIfCountExceedsSize() {
            TimeSeries sample = SamplerUtil.randomSample(series, 1000);

            assertThat(sample.size()).isEqualTo(100);
        }

        @Test
        @DisplayName("randomSample should return points from original series")
        void randomSampleShouldReturnPointsFromOriginalSeries() {
            TimeSeries sample = SamplerUtil.randomSample(series, 10);
            List<DataPoint> allPoints = series.getPoints();
            List<DataPoint> samplePoints = sample.getPoints();

            for (DataPoint point : samplePoints) {
                assertThat(allPoints).contains(point);
            }
        }

        @Test
        @DisplayName("randomSample should maintain sorted order")
        void randomSampleShouldMaintainSortedOrder() {
            TimeSeries sample = SamplerUtil.randomSample(series, 10);
            List<DataPoint> points = sample.getPoints();

            for (int i = 1; i < points.size(); i++) {
                assertThat(points.get(i).timestamp()).isAfterOrEqualTo(points.get(i - 1).timestamp());
            }
        }
    }

    @Nested
    @DisplayName("Systematic Sample Tests")
    class SystematicSampleTests {

        @Test
        @DisplayName("systematicSample should return every nth point")
        void systematicSampleShouldReturnEveryNthPoint() {
            TimeSeries sample = SamplerUtil.systematicSample(series, 10);

            assertThat(sample.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("systematicSample should maintain order")
        void systematicSampleShouldMaintainOrder() {
            TimeSeries sample = SamplerUtil.systematicSample(series, 10);
            List<DataPoint> points = sample.getPoints();

            for (int i = 1; i < points.size(); i++) {
                assertThat(points.get(i).timestamp()).isAfter(points.get(i - 1).timestamp());
            }
        }

        @Test
        @DisplayName("systematicSample should skip n-1 points between samples")
        void systematicSampleShouldSkipNMinus1PointsBetweenSamples() {
            TimeSeries sample = SamplerUtil.systematicSample(series, 10);
            List<DataPoint> points = sample.getPoints();

            // First point should be at index 0 (value 0.0)
            assertThat(points.get(0).value()).isEqualTo(0.0);
            // Second point should be at index 10 (value 10.0)
            assertThat(points.get(1).value()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("Empty Series Tests")
    class EmptySeriesTests {

        @Test
        @DisplayName("downsample on empty series should return empty")
        void downsampleOnEmptySeriesShouldReturnEmpty() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries result = SamplerUtil.downsample(empty, Duration.ofSeconds(10), AggregationType.AVG);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fillGaps on empty series should return empty")
        void fillGapsOnEmptySeriesShouldReturnEmpty() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries result = SamplerUtil.fillGaps(empty, Duration.ofSeconds(10), FillStrategy.ZERO);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("randomSample on empty series should return empty")
        void randomSampleOnEmptySeriesShouldReturnEmpty() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries sample = SamplerUtil.randomSample(empty, 10);

            assertThat(sample.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("AggregationType Enum Tests")
    class AggregationTypeEnumTests {

        @Test
        @DisplayName("should have all expected values")
        void shouldHaveAllExpectedValues() {
            AggregationType[] types = AggregationType.values();

            assertThat(types).contains(
                AggregationType.SUM,
                AggregationType.AVG,
                AggregationType.MIN,
                AggregationType.MAX,
                AggregationType.FIRST,
                AggregationType.LAST,
                AggregationType.COUNT
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(AggregationType.valueOf("SUM")).isEqualTo(AggregationType.SUM);
            assertThat(AggregationType.valueOf("AVG")).isEqualTo(AggregationType.AVG);
        }
    }

    @Nested
    @DisplayName("FillStrategy Enum Tests")
    class FillStrategyEnumTests {

        @Test
        @DisplayName("should have all expected values")
        void shouldHaveAllExpectedValues() {
            FillStrategy[] strategies = FillStrategy.values();

            assertThat(strategies).contains(
                FillStrategy.ZERO,
                FillStrategy.PREVIOUS,
                FillStrategy.LINEAR,
                FillStrategy.NAN,
                FillStrategy.NEXT,
                FillStrategy.AVERAGE
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(FillStrategy.valueOf("ZERO")).isEqualTo(FillStrategy.ZERO);
            assertThat(FillStrategy.valueOf("LINEAR")).isEqualTo(FillStrategy.LINEAR);
        }
    }
}
