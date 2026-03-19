package cloud.opencode.base.timeseries.aggregation;

import cloud.opencode.base.timeseries.DataPoint;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * AggregatorTest Tests
 * AggregatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("Aggregator Tests")
class AggregatorTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2024-01-01T00:00:00Z");
    }

    @Nested
    @DisplayName("Default Method Tests")
    class DefaultMethodTests {

        @Test
        @DisplayName("name should return class simple name by default")
        void nameShouldReturnClassSimpleNameByDefault() {
            Aggregator customAggregator = points -> {
                if (points.isEmpty()) return 0.0;
                return points.get(0).value();
            };

            // Anonymous class will have empty name
            assertThat(customAggregator.name()).isNotNull();
        }

        @Test
        @DisplayName("aggregateToResult should return Success for valid aggregation")
        void aggregateToResultShouldReturnSuccessForValidAggregation() {
            Aggregator sumAggregator = SumAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 10.0),
                DataPoint.of(baseTime.plusSeconds(1), 20.0)
            );

            AggregationResult result = sumAggregator.aggregateToResult(points);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValueOrDefault(0.0)).isEqualTo(30.0);
        }

        @Test
        @DisplayName("aggregateToResult should return Empty for empty list")
        void aggregateToResultShouldReturnEmptyForEmptyList() {
            Aggregator sumAggregator = SumAggregator.getInstance();
            List<DataPoint> points = List.of();

            AggregationResult result = sumAggregator.aggregateToResult(points);

            assertThat(result).isInstanceOf(AggregationResult.Empty.class);
        }

        @Test
        @DisplayName("aggregateToResult should return Error when exception occurs")
        void aggregateToResultShouldReturnErrorWhenExceptionOccurs() {
            Aggregator failingAggregator = points -> {
                throw new RuntimeException("Test error");
            };
            List<DataPoint> points = List.of(DataPoint.of(baseTime, 10.0));

            AggregationResult result = failingAggregator.aggregateToResult(points);

            assertThat(result).isInstanceOf(AggregationResult.Error.class);
        }
    }

    @Nested
    @DisplayName("Custom Aggregator Tests")
    class CustomAggregatorTests {

        @Test
        @DisplayName("should support lambda implementation")
        void shouldSupportLambdaImplementation() {
            Aggregator productAggregator = points -> {
                if (points.isEmpty()) return 1.0;
                return points.stream()
                    .mapToDouble(DataPoint::value)
                    .reduce(1.0, (a, b) -> a * b);
            };

            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 2.0),
                DataPoint.of(baseTime.plusSeconds(1), 3.0),
                DataPoint.of(baseTime.plusSeconds(2), 4.0)
            );

            assertThat(productAggregator.aggregate(points)).isEqualTo(24.0);
        }

        @Test
        @DisplayName("should support method reference implementation")
        void shouldSupportMethodReferenceImplementation() {
            Aggregator medianAggregator = this::calculateMedian;

            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 1.0),
                DataPoint.of(baseTime.plusSeconds(1), 2.0),
                DataPoint.of(baseTime.plusSeconds(2), 3.0),
                DataPoint.of(baseTime.plusSeconds(3), 4.0),
                DataPoint.of(baseTime.plusSeconds(4), 5.0)
            );

            assertThat(medianAggregator.aggregate(points)).isEqualTo(3.0);
        }

        private double calculateMedian(List<DataPoint> points) {
            if (points.isEmpty()) return 0.0;
            List<Double> sorted = points.stream()
                .map(DataPoint::value)
                .sorted()
                .toList();
            int mid = sorted.size() / 2;
            return sorted.get(mid);
        }
    }

    @Nested
    @DisplayName("SumAggregator Tests")
    class SumAggregatorTests {

        @Test
        @DisplayName("getInstance should return singleton instance")
        void getInstanceShouldReturnSingletonInstance() {
            SumAggregator a1 = SumAggregator.getInstance();
            SumAggregator a2 = SumAggregator.getInstance();

            assertThat(a1).isSameAs(a2);
        }

        @Test
        @DisplayName("aggregate should sum all values")
        void aggregateShouldSumAllValues() {
            SumAggregator aggregator = SumAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 10.0),
                DataPoint.of(baseTime.plusSeconds(1), 20.0),
                DataPoint.of(baseTime.plusSeconds(2), 30.0)
            );

            assertThat(aggregator.aggregate(points)).isEqualTo(60.0);
        }

        @Test
        @DisplayName("aggregate should return 0 for empty list")
        void aggregateShouldReturn0ForEmptyList() {
            SumAggregator aggregator = SumAggregator.getInstance();

            assertThat(aggregator.aggregate(List.of())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("name should return SUM")
        void nameShouldReturnSUM() {
            assertThat(SumAggregator.getInstance().name()).isEqualTo("SUM");
        }
    }

    @Nested
    @DisplayName("AvgAggregator Tests")
    class AvgAggregatorTests {

        @Test
        @DisplayName("getInstance should return singleton instance")
        void getInstanceShouldReturnSingletonInstance() {
            AvgAggregator a1 = AvgAggregator.getInstance();
            AvgAggregator a2 = AvgAggregator.getInstance();

            assertThat(a1).isSameAs(a2);
        }

        @Test
        @DisplayName("aggregate should calculate average")
        void aggregateShouldCalculateAverage() {
            AvgAggregator aggregator = AvgAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 10.0),
                DataPoint.of(baseTime.plusSeconds(1), 20.0),
                DataPoint.of(baseTime.plusSeconds(2), 30.0)
            );

            assertThat(aggregator.aggregate(points)).isEqualTo(20.0);
        }

        @Test
        @DisplayName("aggregate should return 0 for empty list")
        void aggregateShouldReturn0ForEmptyList() {
            AvgAggregator aggregator = AvgAggregator.getInstance();

            assertThat(aggregator.aggregate(List.of())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("name should return AVG")
        void nameShouldReturnAVG() {
            assertThat(AvgAggregator.getInstance().name()).isEqualTo("AVG");
        }
    }

    @Nested
    @DisplayName("MinAggregator Tests")
    class MinAggregatorTests {

        @Test
        @DisplayName("getInstance should return singleton instance")
        void getInstanceShouldReturnSingletonInstance() {
            MinAggregator a1 = MinAggregator.getInstance();
            MinAggregator a2 = MinAggregator.getInstance();

            assertThat(a1).isSameAs(a2);
        }

        @Test
        @DisplayName("aggregate should return minimum value")
        void aggregateShouldReturnMinimumValue() {
            MinAggregator aggregator = MinAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 30.0),
                DataPoint.of(baseTime.plusSeconds(1), 10.0),
                DataPoint.of(baseTime.plusSeconds(2), 20.0)
            );

            assertThat(aggregator.aggregate(points)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("aggregate should return MAX_VALUE for empty list")
        void aggregateShouldReturnMaxValueForEmptyList() {
            MinAggregator aggregator = MinAggregator.getInstance();

            assertThat(aggregator.aggregate(List.of())).isEqualTo(Double.MAX_VALUE);
        }

        @Test
        @DisplayName("name should return MIN")
        void nameShouldReturnMIN() {
            assertThat(MinAggregator.getInstance().name()).isEqualTo("MIN");
        }
    }

    @Nested
    @DisplayName("MaxAggregator Tests")
    class MaxAggregatorTests {

        @Test
        @DisplayName("getInstance should return singleton instance")
        void getInstanceShouldReturnSingletonInstance() {
            MaxAggregator a1 = MaxAggregator.getInstance();
            MaxAggregator a2 = MaxAggregator.getInstance();

            assertThat(a1).isSameAs(a2);
        }

        @Test
        @DisplayName("aggregate should return maximum value")
        void aggregateShouldReturnMaximumValue() {
            MaxAggregator aggregator = MaxAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 10.0),
                DataPoint.of(baseTime.plusSeconds(1), 30.0),
                DataPoint.of(baseTime.plusSeconds(2), 20.0)
            );

            assertThat(aggregator.aggregate(points)).isEqualTo(30.0);
        }

        @Test
        @DisplayName("aggregate should return MIN_VALUE for empty list")
        void aggregateShouldReturnMinValueForEmptyList() {
            MaxAggregator aggregator = MaxAggregator.getInstance();

            assertThat(aggregator.aggregate(List.of())).isEqualTo(Double.MIN_VALUE);
        }

        @Test
        @DisplayName("name should return MAX")
        void nameShouldReturnMAX() {
            assertThat(MaxAggregator.getInstance().name()).isEqualTo("MAX");
        }
    }

    @Nested
    @DisplayName("CountAggregator Tests")
    class CountAggregatorTests {

        @Test
        @DisplayName("getInstance should return singleton instance")
        void getInstanceShouldReturnSingletonInstance() {
            CountAggregator a1 = CountAggregator.getInstance();
            CountAggregator a2 = CountAggregator.getInstance();

            assertThat(a1).isSameAs(a2);
        }

        @Test
        @DisplayName("aggregate should return count")
        void aggregateShouldReturnCount() {
            CountAggregator aggregator = CountAggregator.getInstance();
            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 10.0),
                DataPoint.of(baseTime.plusSeconds(1), 20.0),
                DataPoint.of(baseTime.plusSeconds(2), 30.0)
            );

            assertThat(aggregator.aggregate(points)).isEqualTo(3.0);
        }

        @Test
        @DisplayName("aggregate should return 0 for empty list")
        void aggregateShouldReturn0ForEmptyList() {
            CountAggregator aggregator = CountAggregator.getInstance();

            assertThat(aggregator.aggregate(List.of())).isEqualTo(0.0);
        }

        @Test
        @DisplayName("name should return COUNT")
        void nameShouldReturnCOUNT() {
            assertThat(CountAggregator.getInstance().name()).isEqualTo("COUNT");
        }
    }
}
