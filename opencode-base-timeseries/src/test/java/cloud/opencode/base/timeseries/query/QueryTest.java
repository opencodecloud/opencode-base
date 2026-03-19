package cloud.opencode.base.timeseries.query;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.sampling.AggregationType;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.*;

/**
 * QueryTest Tests
 * QueryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("Query Tests")
class QueryTest {

    private TimeSeries series;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        series = new TimeSeries("test-series");
        baseTime = Instant.parse("2024-01-01T00:00:00Z");

        // Add 100 data points
        for (int i = 0; i < 100; i++) {
            Map<String, String> tags = Map.of(
                "sensor", i % 2 == 0 ? "sensor1" : "sensor2",
                "region", i % 3 == 0 ? "north" : "south"
            );
            series.add(DataPoint.of(baseTime.plusSeconds(i), i * 1.0, tags));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("from should create query from series")
        void fromShouldCreateQueryFromSeries() {
            Query query = Query.from(series);

            assertThat(query).isNotNull();
        }
    }

    @Nested
    @DisplayName("Range Filter Tests")
    class RangeFilterTests {

        @Test
        @DisplayName("range should filter by time range")
        void rangeShouldFilterByTimeRange() {
            List<DataPoint> result = Query.from(series)
                .range(baseTime.plusSeconds(10), baseTime.plusSeconds(20))
                .execute();

            assertThat(result).hasSize(11);
            assertThat(result.get(0).value()).isEqualTo(10.0);
            assertThat(result.get(10).value()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("range with TimeRange should filter correctly")
        void rangeWithTimeRangeShouldFilterCorrectly() {
            TimeRange timeRange = TimeRange.of(baseTime.plusSeconds(10), baseTime.plusSeconds(20));
            List<DataPoint> result = Query.from(series)
                .range(timeRange)
                .execute();

            assertThat(result).hasSize(11);
        }

        @Test
        @DisplayName("last should return last duration of points")
        void lastShouldReturnLastDurationOfPoints() {
            List<DataPoint> result = Query.from(series)
                .last(Duration.ofSeconds(10))
                .execute();

            assertThat(result.size()).isLessThanOrEqualTo(11);
        }
    }

    @Nested
    @DisplayName("Limit Tests")
    class LimitTests {

        @Test
        @DisplayName("limit should restrict result count")
        void limitShouldRestrictResultCount() {
            List<DataPoint> result = Query.from(series)
                .limit(10)
                .execute();

            assertThat(result).hasSize(10);
        }

        @Test
        @DisplayName("limit should return all if limit exceeds size")
        void limitShouldReturnAllIfLimitExceedsSize() {
            List<DataPoint> result = Query.from(series)
                .limit(1000)
                .execute();

            assertThat(result).hasSize(100);
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("filter should apply predicate")
        void filterShouldApplyPredicate() {
            List<DataPoint> result = Query.from(series)
                .filter(p -> p.value() >= 50)
                .execute();

            assertThat(result).hasSize(50);
            assertThat(result).allMatch(p -> p.value() >= 50);
        }

        @Test
        @DisplayName("valueRange should filter by value range")
        void valueRangeShouldFilterByValueRange() {
            List<DataPoint> result = Query.from(series)
                .valueRange(20.0, 30.0)
                .execute();

            assertThat(result).hasSize(11);
            assertThat(result).allMatch(p -> p.value() >= 20.0 && p.value() <= 30.0);
        }
    }

    @Nested
    @DisplayName("Tag Filter Tests")
    class TagFilterTests {

        @Test
        @DisplayName("tag should filter by tag value")
        void tagShouldFilterByTagValue() {
            List<DataPoint> result = Query.from(series)
                .tag("sensor", "sensor1")
                .execute();

            assertThat(result).hasSize(50);
            assertThat(result).allMatch(p -> "sensor1".equals(p.getTag("sensor")));
        }
    }

    @Nested
    @DisplayName("Aggregation Tests")
    class AggregationTests {

        @Test
        @DisplayName("aggregate should downsample with function")
        void aggregateShouldDownsampleWithFunction() {
            List<DataPoint> result = Query.from(series)
                .groupBy(Duration.ofSeconds(10))
                .aggregate(AggregationType.AVG)
                .execute();

            assertThat(result).hasSize(10);
        }

        @Test
        @DisplayName("aggregate with SUM should sum values")
        void aggregateWithSumShouldSumValues() {
            List<DataPoint> result = Query.from(series)
                .groupBy(Duration.ofSeconds(10))
                .aggregate(AggregationType.SUM)
                .execute();

            assertThat(result).hasSize(10);
            // First bucket: 0+1+2+...+9 = 45
            assertThat(result.get(0).value()).isEqualTo(45.0);
        }
    }

    @Nested
    @DisplayName("Execute Tests")
    class ExecuteTests {

        @Test
        @DisplayName("execute should return filtered and processed points")
        void executeShouldReturnFilteredAndProcessedPoints() {
            List<DataPoint> result = Query.from(series)
                .range(baseTime, baseTime.plusSeconds(50))
                .filter(p -> p.value() >= 20)
                .limit(10)
                .execute();

            assertThat(result).hasSize(10);
            assertThat(result).allMatch(p -> p.value() >= 20);
        }

        @Test
        @DisplayName("executeScalar should return aggregated value")
        void executeScalarShouldReturnAggregatedValue() {
            OptionalDouble result = Query.from(series)
                .range(baseTime, baseTime.plusSeconds(10))
                .aggregate(AggregationType.SUM)
                .executeScalar();

            assertThat(result).isPresent();
            assertThat(result.getAsDouble()).isEqualTo(55.0); // 0+1+2+...+10 = 55
        }

        @Test
        @DisplayName("executeScalar on empty result should return empty")
        void executeScalarOnEmptyResultShouldReturnEmpty() {
            OptionalDouble result = Query.from(series)
                .filter(p -> p.value() > 1000)
                .executeScalar();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("should support method chaining")
        void shouldSupportMethodChaining() {
            List<DataPoint> result = Query.from(series)
                .range(baseTime, baseTime.plusSeconds(50))
                .valueRange(10.0, 40.0)
                .filter(p -> p.value() % 2 == 0)
                .limit(5)
                .execute();

            assertThat(result.size()).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Empty Series Tests")
    class EmptySeriesTests {

        @Test
        @DisplayName("execute on empty series should return empty list")
        void executeOnEmptySeriesShouldReturnEmptyList() {
            TimeSeries empty = new TimeSeries("empty");
            List<DataPoint> result = Query.from(empty).execute();

            assertThat(result).isEmpty();
        }
    }
}
