package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeSeriesTest Tests
 * TimeSeriesTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TimeSeries Tests")
class TimeSeriesTest {

    private TimeSeries series;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        series = new TimeSeries("test-series");
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create empty series with name")
        void shouldCreateEmptySeriesWithName() {
            TimeSeries ts = new TimeSeries("my-series");

            assertThat(ts.getName()).isEqualTo("my-series");
            assertThat(ts.isEmpty()).isTrue();
            assertThat(ts.size()).isZero();
        }

        @Test
        @DisplayName("should create series with initial points")
        void shouldCreateSeriesWithInitialPoints() {
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 1.0),
                DataPoint.of(baseTime.plusSeconds(1), 2.0)
            );
            TimeSeries ts = new TimeSeries("my-series", points);

            assertThat(ts.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Add Operations Tests")
    class AddOperationsTests {

        @Test
        @DisplayName("add should add data point to series")
        void addShouldAddDataPointToSeries() {
            DataPoint point = DataPoint.of(baseTime, 42.0);
            series.add(point);

            assertThat(series.size()).isEqualTo(1);
            assertThat(series.get(baseTime)).isPresent().contains(point);
        }

        @Test
        @DisplayName("add should add point by timestamp and value")
        void addShouldAddPointByTimestampAndValue() {
            series.add(baseTime, 42.0);

            assertThat(series.size()).isEqualTo(1);
            assertThat(series.get(baseTime).map(DataPoint::value)).contains(42.0);
        }

        @Test
        @DisplayName("addNow should add point with current timestamp")
        void addNowShouldAddPointWithCurrentTimestamp() {
            Instant before = Instant.now();
            series.addNow(100.0);
            Instant after = Instant.now();

            assertThat(series.size()).isEqualTo(1);
            DataPoint point = series.getLatest();
            assertThat(point.timestamp()).isBetween(before, after);
            assertThat(point.value()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("addAll should add multiple points")
        void addAllShouldAddMultiplePoints() {
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 1.0),
                DataPoint.of(baseTime.plusSeconds(1), 2.0),
                DataPoint.of(baseTime.plusSeconds(2), 3.0)
            );
            series.addAll(points);

            assertThat(series.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Query Operations Tests")
    class QueryOperationsTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 10; i++) {
                series.add(baseTime.plusSeconds(i), i * 10.0);
            }
        }

        @Test
        @DisplayName("get should return point at timestamp")
        void getShouldReturnPointAtTimestamp() {
            assertThat(series.get(baseTime.plusSeconds(5)).map(DataPoint::value)).contains(50.0);
        }

        @Test
        @DisplayName("get should return empty for non-existent timestamp")
        void getShouldReturnEmptyForNonExistentTimestamp() {
            assertThat(series.get(baseTime.minusSeconds(100))).isEmpty();
        }

        @Test
        @DisplayName("getFirst should return first point")
        void getFirstShouldReturnFirstPoint() {
            assertThat(series.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("getLast should return last point")
        void getLastShouldReturnLastPoint() {
            assertThat(series.getLast().map(DataPoint::value)).contains(90.0);
        }

        @Test
        @DisplayName("getLatest should return latest point")
        void getLatestShouldReturnLatestPoint() {
            assertThat(series.getLatest().value()).isEqualTo(90.0);
        }

        @Test
        @DisplayName("getPoints should return all points")
        void getPointsShouldReturnAllPoints() {
            assertThat(series.getPoints()).hasSize(10);
        }

        @Test
        @DisplayName("getValues should return all values as array")
        void getValuesShouldReturnAllValuesAsArray() {
            double[] values = series.getValues();
            assertThat(values).hasSize(10);
            assertThat(values).containsExactly(0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0);
        }

        @Test
        @DisplayName("getTimestamps should return all timestamps as array")
        void getTimestampsShouldReturnAllTimestampsAsArray() {
            Instant[] timestamps = series.getTimestamps();
            assertThat(timestamps).hasSize(10);
            assertThat(timestamps[0]).isEqualTo(baseTime);
        }
    }

    @Nested
    @DisplayName("Range Operations Tests")
    class RangeOperationsTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 10; i++) {
                series.add(baseTime.plusSeconds(i), i * 10.0);
            }
        }

        @Test
        @DisplayName("range should return series with points in range")
        void rangeShouldReturnSeriesWithPointsInRange() {
            TimeSeries ranged = series.range(baseTime.plusSeconds(2), baseTime.plusSeconds(5));
            assertThat(ranged.size()).isEqualTo(4);
        }

        @Test
        @DisplayName("last should return series with last duration of points")
        void lastShouldReturnSeriesWithLastDurationOfPoints() {
            TimeSeries lastPoints = series.last(Duration.ofSeconds(5));
            assertThat(lastPoints.size()).isLessThanOrEqualTo(6);
        }

        @Test
        @DisplayName("head should return series with first n points")
        void headShouldReturnSeriesWithFirstNPoints() {
            TimeSeries headPoints = series.head(3);
            assertThat(headPoints.size()).isEqualTo(3);
            assertThat(headPoints.getFirst().map(DataPoint::value)).contains(0.0);
        }

        @Test
        @DisplayName("tail should return series with last n points")
        void tailShouldReturnSeriesWithLastNPoints() {
            TimeSeries tailPoints = series.tail(3);
            assertThat(tailPoints.size()).isEqualTo(3);
            assertThat(tailPoints.getLast().map(DataPoint::value)).contains(90.0);
        }
    }

    @Nested
    @DisplayName("Aggregation Tests")
    class AggregationTests {

        @BeforeEach
        void setUpData() {
            series.add(baseTime, 10.0);
            series.add(baseTime.plusSeconds(1), 20.0);
            series.add(baseTime.plusSeconds(2), 30.0);
            series.add(baseTime.plusSeconds(3), 40.0);
            series.add(baseTime.plusSeconds(4), 50.0);
        }

        @Test
        @DisplayName("sum should return sum of all values")
        void sumShouldReturnSumOfAllValues() {
            assertThat(series.sum()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("average should return average of all values")
        void averageShouldReturnAverageOfAllValues() {
            assertThat(series.average()).isPresent().hasValue(30.0);
        }

        @Test
        @DisplayName("min should return minimum value")
        void minShouldReturnMinimumValue() {
            assertThat(series.min()).isPresent().hasValue(10.0);
        }

        @Test
        @DisplayName("max should return maximum value")
        void maxShouldReturnMaximumValue() {
            assertThat(series.max()).isPresent().hasValue(50.0);
        }

        @Test
        @DisplayName("count should return number of points")
        void countShouldReturnNumberOfPoints() {
            assertThat(series.count()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("variance should return variance of values")
        void varianceShouldReturnVarianceOfValues() {
            double variance = series.variance();
            // Sample variance: sum((x-mean)^2) / (n-1) = 1000/4 = 250
            assertThat(variance).isCloseTo(250.0, within(0.01));
        }

        @Test
        @DisplayName("stdDev should return standard deviation")
        void stdDevShouldReturnStandardDeviation() {
            double stdDev = series.stdDev();
            // sqrt(250) = 15.811...
            assertThat(stdDev).isCloseTo(15.81, within(0.1));
        }

        @Test
        @DisplayName("standardDeviation should return standard deviation")
        void standardDeviationShouldReturnStandardDeviation() {
            double stdDev = series.standardDeviation();
            // sqrt(250) = 15.811...
            assertThat(stdDev).isCloseTo(15.81, within(0.1));
        }

        @Test
        @DisplayName("percentile should return correct percentile")
        void percentileShouldReturnCorrectPercentile() {
            assertThat(series.percentile(50)).isEqualTo(30.0);
            assertThat(series.percentile(0)).isEqualTo(10.0);
            assertThat(series.percentile(100)).isEqualTo(50.0);
        }

        @Test
        @DisplayName("stats should return statistics")
        void statsShouldReturnStatistics() {
            TimeSeriesStats stats = series.stats();

            assertThat(stats.count()).isEqualTo(5);
            assertThat(stats.sum()).isEqualTo(150.0);
            assertThat(stats.average()).isEqualTo(30.0);
            assertThat(stats.min()).isEqualTo(10.0);
            assertThat(stats.max()).isEqualTo(50.0);
        }
    }

    @Nested
    @DisplayName("Transform Operations Tests")
    class TransformOperationsTests {

        @BeforeEach
        void setUpData() {
            series.add(baseTime, 10.0);
            series.add(baseTime.plusSeconds(1), 20.0);
            series.add(baseTime.plusSeconds(2), 30.0);
        }

        @Test
        @DisplayName("map should transform values")
        void mapShouldTransformValues() {
            TimeSeries mapped = series.map(v -> v * 2);
            double[] values = mapped.getValues();

            assertThat(values).containsExactly(20.0, 40.0, 60.0);
        }

        @Test
        @DisplayName("diff should calculate differences")
        void diffShouldCalculateDifferences() {
            TimeSeries diff = series.diff();

            assertThat(diff.size()).isEqualTo(2);
            assertThat(diff.getValues()).containsExactly(10.0, 10.0);
        }

        @Test
        @DisplayName("cumSum should calculate cumulative sum")
        void cumSumShouldCalculateCumulativeSum() {
            TimeSeries cumSum = series.cumSum();

            assertThat(cumSum.getValues()).containsExactly(10.0, 30.0, 60.0);
        }

        @Test
        @DisplayName("derivative should calculate rate of change")
        void derivativeShouldCalculateRateOfChange() {
            TimeSeries deriv = series.derivative();

            assertThat(deriv.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Moving Average Tests")
    class MovingAverageTests {

        @BeforeEach
        void setUpData() {
            for (int i = 1; i <= 10; i++) {
                series.add(baseTime.plusSeconds(i), i * 10.0);
            }
        }

        @Test
        @DisplayName("movingAverage should calculate moving average")
        void movingAverageShouldCalculateMovingAverage() {
            TimeSeries ma = series.movingAverage(3);

            assertThat(ma.size()).isEqualTo(8);
        }

        @Test
        @DisplayName("exponentialMovingAverage should calculate EMA")
        void exponentialMovingAverageShouldCalculateEma() {
            TimeSeries ema = series.exponentialMovingAverage(0.5);

            assertThat(ema.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Combine Operations Tests")
    class CombineOperationsTests {

        @Test
        @DisplayName("combine should merge two series with operator")
        void combineShouldMergeTwoSeriesWithOperator() {
            TimeSeries other = new TimeSeries("other");
            series.add(baseTime, 10.0);
            series.add(baseTime.plusSeconds(1), 20.0);
            other.add(baseTime, 5.0);
            other.add(baseTime.plusSeconds(1), 10.0);

            TimeSeries combined = series.combine(other, (a, b) -> a + b);

            assertThat(combined.size()).isEqualTo(2);
            assertThat(combined.get(baseTime).map(DataPoint::value)).contains(15.0);
        }
    }

    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {

        @Test
        @DisplayName("setMetadata should store metadata")
        void setMetadataShouldStoreMetadata() {
            series.setMetadata("key1", "value1");

            assertThat(series.getMetadata("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("getMetadata should return null for missing key")
        void getMetadataShouldReturnNullForMissingKey() {
            assertThat(series.getMetadata("nonexistent")).isNull();
        }

        @Test
        @DisplayName("getAllMetadata should return all metadata")
        void getAllMetadataShouldReturnAllMetadata() {
            series.setMetadata("key1", "value1");
            series.setMetadata("key2", "value2");

            var metadata = series.getAllMetadata();
            assertThat(metadata).hasSize(2);
            assertThat(metadata).containsEntry("key1", "value1");
            assertThat(metadata).containsEntry("key2", "value2");
        }
    }

    @Nested
    @DisplayName("Lifecycle Operations Tests")
    class LifecycleOperationsTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 10; i++) {
                series.add(baseTime.plusSeconds(i), i * 10.0);
            }
        }

        @Test
        @DisplayName("clear should remove all points")
        void clearShouldRemoveAllPoints() {
            series.clear();

            assertThat(series.isEmpty()).isTrue();
            assertThat(series.size()).isZero();
        }

        @Test
        @DisplayName("all should return all points as list")
        void allShouldReturnAllPointsAsList() {
            List<DataPoint> all = series.all();

            assertThat(all).hasSize(10);
        }
    }

    @Nested
    @DisplayName("Lag Tests")
    class LagTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 5; i++) {
                series.add(baseTime.plusSeconds(i), (i + 1) * 10.0);
            }
        }

        @Test
        @DisplayName("should create lagged series")
        void shouldCreateLaggedSeries() {
            // values: 10, 20, 30, 40, 50
            // lag(2): at timestamps[2]=value[0]=10, timestamps[3]=value[1]=20, timestamps[4]=value[2]=30
            TimeSeries lagged = series.lag(2);

            assertThat(lagged.size()).isEqualTo(3);
            double[] values = lagged.getValues();
            assertThat(values).containsExactly(10.0, 20.0, 30.0);
            // Verify timestamps come from the original positions 2, 3, 4
            Instant[] timestamps = lagged.getTimestamps();
            assertThat(timestamps[0]).isEqualTo(baseTime.plusSeconds(2));
            assertThat(timestamps[1]).isEqualTo(baseTime.plusSeconds(3));
            assertThat(timestamps[2]).isEqualTo(baseTime.plusSeconds(4));
        }

        @Test
        @DisplayName("lag(1) should shift values back by one period")
        void lagOneShouldShiftByOnePeriod() {
            TimeSeries lagged = series.lag(1);

            assertThat(lagged.size()).isEqualTo(4);
            assertThat(lagged.getValues()).containsExactly(10.0, 20.0, 30.0, 40.0);
        }

        @Test
        @DisplayName("should return empty for lag greater than or equal to size")
        void shouldReturnEmptyForLagGreaterThanOrEqualToSize() {
            TimeSeries laggedEqual = series.lag(5);
            assertThat(laggedEqual.isEmpty()).isTrue();

            TimeSeries laggedGreater = series.lag(10);
            assertThat(laggedGreater.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("lag(0) should return copy of original series")
        void lagZeroShouldReturnCopy() {
            TimeSeries lagged = series.lag(0);
            assertThat(lagged.size()).isEqualTo(series.size());
            assertThat(lagged.getValues()).containsExactly(series.getValues());
        }

        @Test
        @DisplayName("should throw for negative lag")
        void shouldThrowForNegativeLag() {
            assertThatThrownBy(() -> series.lag(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("periods must be >= 0");
        }
    }

    @Nested
    @DisplayName("Lead Tests")
    class LeadTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 5; i++) {
                series.add(baseTime.plusSeconds(i), (i + 1) * 10.0);
            }
        }

        @Test
        @DisplayName("should create lead series")
        void shouldCreateLeadSeries() {
            // values: 10, 20, 30, 40, 50
            // lead(2): at timestamps[0]=value[2]=30, timestamps[1]=value[3]=40, timestamps[2]=value[4]=50
            TimeSeries led = series.lead(2);

            assertThat(led.size()).isEqualTo(3);
            double[] values = led.getValues();
            assertThat(values).containsExactly(30.0, 40.0, 50.0);
            Instant[] timestamps = led.getTimestamps();
            assertThat(timestamps[0]).isEqualTo(baseTime);
            assertThat(timestamps[1]).isEqualTo(baseTime.plusSeconds(1));
            assertThat(timestamps[2]).isEqualTo(baseTime.plusSeconds(2));
        }

        @Test
        @DisplayName("lead(1) should shift values forward by one period")
        void leadOneShouldShiftByOnePeriod() {
            TimeSeries led = series.lead(1);

            assertThat(led.size()).isEqualTo(4);
            assertThat(led.getValues()).containsExactly(20.0, 30.0, 40.0, 50.0);
        }

        @Test
        @DisplayName("should return empty for lead greater than or equal to size")
        void shouldReturnEmptyForLeadGreaterThanOrEqualToSize() {
            TimeSeries ledEqual = series.lead(5);
            assertThat(ledEqual.isEmpty()).isTrue();

            TimeSeries ledGreater = series.lead(10);
            assertThat(ledGreater.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("lead(0) should return copy of original series")
        void leadZeroShouldReturnCopy() {
            TimeSeries led = series.lead(0);
            assertThat(led.size()).isEqualTo(series.size());
            assertThat(led.getValues()).containsExactly(series.getValues());
        }

        @Test
        @DisplayName("should throw for negative lead")
        void shouldThrowForNegativeLead() {
            assertThatThrownBy(() -> series.lead(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("periods must be >= 0");
        }
    }

    @Nested
    @DisplayName("Shift Tests")
    class ShiftTests {

        @BeforeEach
        void setUpData() {
            for (int i = 0; i < 3; i++) {
                series.add(baseTime.plusSeconds(i), (i + 1) * 10.0);
            }
        }

        @Test
        @DisplayName("should shift timestamps forward by duration")
        void shouldShiftTimestampsForward() {
            Duration oneHour = Duration.ofHours(1);
            TimeSeries shifted = series.shift(oneHour);

            assertThat(shifted.size()).isEqualTo(3);
            Instant[] timestamps = shifted.getTimestamps();
            assertThat(timestamps[0]).isEqualTo(baseTime.plus(oneHour));
            assertThat(timestamps[1]).isEqualTo(baseTime.plusSeconds(1).plus(oneHour));
            assertThat(timestamps[2]).isEqualTo(baseTime.plusSeconds(2).plus(oneHour));
            // Values should remain unchanged
            assertThat(shifted.getValues()).containsExactly(10.0, 20.0, 30.0);
        }

        @Test
        @DisplayName("should shift timestamps backward with negative duration")
        void shouldShiftNegative() {
            Duration negativeShift = Duration.ofMinutes(-30);
            TimeSeries shifted = series.shift(negativeShift);

            assertThat(shifted.size()).isEqualTo(3);
            Instant[] timestamps = shifted.getTimestamps();
            assertThat(timestamps[0]).isEqualTo(baseTime.plus(negativeShift));
            assertThat(timestamps[1]).isEqualTo(baseTime.plusSeconds(1).plus(negativeShift));
            assertThat(timestamps[2]).isEqualTo(baseTime.plusSeconds(2).plus(negativeShift));
        }

        @Test
        @DisplayName("shift by zero should preserve timestamps")
        void shiftByZeroShouldPreserveTimestamps() {
            TimeSeries shifted = series.shift(Duration.ZERO);

            assertThat(shifted.size()).isEqualTo(3);
            assertThat(shifted.getTimestamps()).containsExactly(series.getTimestamps());
            assertThat(shifted.getValues()).containsExactly(series.getValues());
        }

        @Test
        @DisplayName("should throw for null offset")
        void shouldThrowForNullOffset() {
            assertThatThrownBy(() -> series.shift(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("shift on empty series should return empty")
        void shiftOnEmptySeriesShouldReturnEmpty() {
            TimeSeries empty = new TimeSeries("empty");
            TimeSeries shifted = empty.shift(Duration.ofHours(1));
            assertThat(shifted.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Percentage Change Tests")
    class PctChangeTests {

        @Test
        @DisplayName("should calculate percentage change")
        void shouldCalculatePercentageChange() {
            series.add(baseTime, 100.0);
            series.add(baseTime.plusSeconds(1), 110.0);
            series.add(baseTime.plusSeconds(2), 99.0);

            TimeSeries pct = series.pctChange(1);

            assertThat(pct.size()).isEqualTo(2);
            double[] values = pct.getValues();
            // (110 - 100) / 100 = 0.1
            assertThat(values[0]).isCloseTo(0.1, within(1e-10));
            // (99 - 110) / 110 = -0.1
            assertThat(values[1]).isCloseTo(-0.1, within(1e-10));
        }

        @Test
        @DisplayName("should calculate percentage change with periods > 1")
        void shouldCalculatePctChangeWithMultiplePeriods() {
            series.add(baseTime, 100.0);
            series.add(baseTime.plusSeconds(1), 110.0);
            series.add(baseTime.plusSeconds(2), 130.0);
            series.add(baseTime.plusSeconds(3), 120.0);

            TimeSeries pct = series.pctChange(2);

            assertThat(pct.size()).isEqualTo(2);
            // (130 - 100) / 100 = 0.3
            assertThat(pct.getValues()[0]).isCloseTo(0.3, within(1e-10));
            // (120 - 110) / 110 = 0.0909...
            assertThat(pct.getValues()[1]).isCloseTo(10.0 / 110.0, within(1e-10));
        }

        @Test
        @DisplayName("should return NaN when previous value is zero")
        void shouldHandleZeroPreviousValue() {
            series.add(baseTime, 0.0);
            series.add(baseTime.plusSeconds(1), 50.0);

            TimeSeries pct = series.pctChange(1);

            assertThat(pct.size()).isEqualTo(1);
            assertThat(pct.getValues()[0]).isNaN();
        }

        @Test
        @DisplayName("should return empty when periods >= size")
        void shouldReturnEmptyWhenPeriodsGreaterThanOrEqualToSize() {
            series.add(baseTime, 100.0);
            series.add(baseTime.plusSeconds(1), 200.0);

            TimeSeries pct = series.pctChange(2);
            assertThat(pct.isEmpty()).isTrue();

            TimeSeries pctLarger = series.pctChange(10);
            assertThat(pctLarger.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should throw for zero periods")
        void shouldThrowForZeroPeriods() {
            series.add(baseTime, 100.0);

            assertThatThrownBy(() -> series.pctChange(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("periods must be > 0");
        }

        @Test
        @DisplayName("should throw for negative periods")
        void shouldThrowForNegativePeriods() {
            series.add(baseTime, 100.0);

            assertThatThrownBy(() -> series.pctChange(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("periods must be > 0");
        }
    }

    @Nested
    @DisplayName("Empty Series Tests")
    class EmptySeriesTests {

        @Test
        @DisplayName("isEmpty should return true for empty series")
        void isEmptyShouldReturnTrueForEmptySeries() {
            assertThat(series.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for non-empty series")
        void isEmptyShouldReturnFalseForNonEmptySeries() {
            series.add(baseTime, 1.0);
            assertThat(series.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("getFirst should return empty for empty series")
        void getFirstShouldReturnEmptyForEmptySeries() {
            assertThat(series.getFirst()).isEmpty();
        }

        @Test
        @DisplayName("getLast should return empty for empty series")
        void getLastShouldReturnEmptyForEmptySeries() {
            assertThat(series.getLast()).isEmpty();
        }

        @Test
        @DisplayName("getLatest should return null for empty series")
        void getLatestShouldReturnNullForEmptySeries() {
            assertThat(series.getLatest()).isNull();
        }

        @Test
        @DisplayName("average should return empty for empty series")
        void averageShouldReturnEmptyForEmptySeries() {
            assertThat(series.average()).isEmpty();
        }

        @Test
        @DisplayName("min should return empty for empty series")
        void minShouldReturnEmptyForEmptySeries() {
            assertThat(series.min()).isEmpty();
        }

        @Test
        @DisplayName("max should return empty for empty series")
        void maxShouldReturnEmptyForEmptySeries() {
            assertThat(series.max()).isEmpty();
        }
    }
}
