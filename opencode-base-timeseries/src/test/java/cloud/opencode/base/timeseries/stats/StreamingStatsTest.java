package cloud.opencode.base.timeseries.stats;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.TimeSeries;
import cloud.opencode.base.timeseries.TimeSeriesStats;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * StreamingStats Tests
 * StreamingStats 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
@DisplayName("StreamingStats Tests")
class StreamingStatsTest {

    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    @Nested
    @DisplayName("Empty Stats Tests")
    class EmptyStatsTests {

        @Test
        @DisplayName("new instance should have count 0")
        void newInstanceShouldHaveCountZero() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.count()).isZero();
        }

        @Test
        @DisplayName("empty stats should return NaN for mean")
        void emptyStatsShouldReturnNaNForMean() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.mean()).isNaN();
        }

        @Test
        @DisplayName("empty stats should return NaN for min and max")
        void emptyStatsShouldReturnNaNForMinAndMax() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.min()).isNaN();
            assertThat(stats.max()).isNaN();
        }

        @Test
        @DisplayName("empty stats should return 0 for sum")
        void emptyStatsShouldReturnZeroForSum() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.sum()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("empty stats should return 0 for variance and stdDev")
        void emptyStatsShouldReturnZeroForVarianceAndStdDev() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.variance()).isEqualTo(0.0);
            assertThat(stats.stdDev()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("empty snapshot should be empty TimeSeriesStats")
        void emptySnapshotShouldBeEmptyTimeSeriesStats() {
            StreamingStats stats = new StreamingStats();

            TimeSeriesStats snapshot = stats.snapshot();

            assertThat(snapshot.isEmpty()).isTrue();
            assertThat(snapshot.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Single Value Tests")
    class SingleValueTests {

        @Test
        @DisplayName("should compute correct stats for single value")
        void shouldComputeCorrectStatsForSingleValue() {
            StreamingStats stats = new StreamingStats();
            stats.add(42.0);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.sum()).isEqualTo(42.0);
            assertThat(stats.mean()).isEqualTo(42.0);
            assertThat(stats.min()).isEqualTo(42.0);
            assertThat(stats.max()).isEqualTo(42.0);
            assertThat(stats.variance()).isEqualTo(0.0);
            assertThat(stats.stdDev()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Multiple Values Tests")
    class MultipleValuesTests {

        @Test
        @DisplayName("should compute correct stats for multiple values")
        void shouldComputeCorrectStatsForMultipleValues() {
            StreamingStats stats = new StreamingStats();
            // Values: 2, 4, 4, 4, 5, 5, 7, 9
            // Mean = 40/8 = 5.0
            // Sample variance = sum((xi-mean)^2) / (n-1) = 32/7 ~= 4.571
            double[] values = {2, 4, 4, 4, 5, 5, 7, 9};
            for (double v : values) {
                stats.add(v);
            }

            assertThat(stats.count()).isEqualTo(8);
            assertThat(stats.sum()).isEqualTo(40.0);
            assertThat(stats.mean()).isEqualTo(5.0);
            assertThat(stats.min()).isEqualTo(2.0);
            assertThat(stats.max()).isEqualTo(9.0);
            assertThat(stats.variance()).isCloseTo(32.0 / 7.0, within(1e-10));
        }

        @Test
        @DisplayName("variance should use Bessel's correction (n-1)")
        void varianceShouldUseBesselsCorrection() {
            StreamingStats stats = new StreamingStats();
            // Values: 10, 20
            // Mean = 15, deviations: -5, 5
            // Population var = 25, Sample var = 50 / 1 = 50
            stats.add(10.0);
            stats.add(20.0);

            // Bessel's: divided by (n-1) = 1
            assertThat(stats.variance()).isCloseTo(50.0, within(1e-10));
        }

        @Test
        @DisplayName("stdDev should match sqrt of variance")
        void stdDevShouldMatchSqrtOfVariance() {
            StreamingStats stats = new StreamingStats();
            stats.add(10.0);
            stats.add(20.0);
            stats.add(30.0);

            assertThat(stats.stdDev()).isCloseTo(Math.sqrt(stats.variance()), within(1e-10));
        }
    }

    @Nested
    @DisplayName("Add DataPoint Tests")
    class AddDataPointTests {

        @Test
        @DisplayName("should accept DataPoint and use its value")
        void shouldAcceptDataPointAndUseItsValue() {
            StreamingStats stats = new StreamingStats();
            DataPoint point = DataPoint.of(BASE, 7.5);

            stats.add(point);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.mean()).isEqualTo(7.5);
        }
    }

    @Nested
    @DisplayName("addAll Tests")
    class AddAllTests {

        @Test
        @DisplayName("should add all points from a TimeSeries")
        void shouldAddAllPointsFromTimeSeries() {
            TimeSeries ts = new TimeSeries("test");
            ts.add(BASE, 10.0);
            ts.add(BASE.plusSeconds(1), 20.0);
            ts.add(BASE.plusSeconds(2), 30.0);

            StreamingStats stats = new StreamingStats();
            stats.addAll(ts);

            assertThat(stats.count()).isEqualTo(3);
            assertThat(stats.sum()).isEqualTo(60.0);
            assertThat(stats.mean()).isEqualTo(20.0);
            assertThat(stats.min()).isEqualTo(10.0);
            assertThat(stats.max()).isEqualTo(30.0);
        }
    }

    @Nested
    @DisplayName("Snapshot Tests")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot should return correct TimeSeriesStats")
        void snapshotShouldReturnCorrectTimeSeriesStats() {
            StreamingStats stats = new StreamingStats();
            stats.add(10.0);
            stats.add(20.0);
            stats.add(30.0);

            TimeSeriesStats snapshot = stats.snapshot();

            assertThat(snapshot.count()).isEqualTo(3);
            assertThat(snapshot.sum()).isEqualTo(60.0);
            assertThat(snapshot.average()).isEqualTo(20.0);
            assertThat(snapshot.min()).isEqualTo(10.0);
            assertThat(snapshot.max()).isEqualTo(30.0);
            assertThat(snapshot.stdDev()).isCloseTo(stats.stdDev(), within(1e-10));
        }
    }

    @Nested
    @DisplayName("Merge Tests")
    class MergeTests {

        @Test
        @DisplayName("merge should combine two stats correctly")
        void mergeShouldCombineTwoStatsCorrectly() {
            StreamingStats stats1 = new StreamingStats();
            stats1.add(1.0);
            stats1.add(2.0);
            stats1.add(3.0);

            StreamingStats stats2 = new StreamingStats();
            stats2.add(4.0);
            stats2.add(5.0);
            stats2.add(6.0);

            // Compute expected from scratch
            StreamingStats expected = new StreamingStats();
            for (double v : new double[]{1, 2, 3, 4, 5, 6}) {
                expected.add(v);
            }

            stats1.merge(stats2);

            assertThat(stats1.count()).isEqualTo(expected.count());
            assertThat(stats1.sum()).isCloseTo(expected.sum(), within(1e-10));
            assertThat(stats1.mean()).isCloseTo(expected.mean(), within(1e-10));
            assertThat(stats1.min()).isEqualTo(expected.min());
            assertThat(stats1.max()).isEqualTo(expected.max());
            assertThat(stats1.variance()).isCloseTo(expected.variance(), within(1e-10));
        }

        @Test
        @DisplayName("merge empty into non-empty should not change stats")
        void mergeEmptyIntoNonEmptyShouldNotChange() {
            StreamingStats stats = new StreamingStats();
            stats.add(5.0);
            stats.add(15.0);

            double meanBefore = stats.mean();
            long countBefore = stats.count();

            stats.merge(new StreamingStats());

            assertThat(stats.count()).isEqualTo(countBefore);
            assertThat(stats.mean()).isEqualTo(meanBefore);
        }

        @Test
        @DisplayName("merge into empty should copy other stats")
        void mergeIntoEmptyShouldCopyOther() {
            StreamingStats empty = new StreamingStats();
            StreamingStats other = new StreamingStats();
            other.add(10.0);
            other.add(20.0);

            empty.merge(other);

            assertThat(empty.count()).isEqualTo(2);
            assertThat(empty.mean()).isEqualTo(15.0);
            assertThat(empty.sum()).isEqualTo(30.0);
        }

        @Test
        @DisplayName("merge null should throw NullPointerException")
        void mergeNullShouldThrow() {
            StreamingStats stats = new StreamingStats();

            assertThatNullPointerException()
                    .isThrownBy(() -> stats.merge(null));
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("reset should clear all stats")
        void resetShouldClearAllStats() {
            StreamingStats stats = new StreamingStats();
            stats.add(10.0);
            stats.add(20.0);
            stats.add(30.0);

            stats.reset();

            assertThat(stats.count()).isZero();
            assertThat(stats.sum()).isEqualTo(0.0);
            assertThat(stats.mean()).isNaN();
            assertThat(stats.min()).isNaN();
            assertThat(stats.max()).isNaN();
            assertThat(stats.variance()).isEqualTo(0.0);
            assertThat(stats.stdDev()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("reset followed by add should work correctly")
        void resetFollowedByAddShouldWork() {
            StreamingStats stats = new StreamingStats();
            stats.add(100.0);
            stats.reset();
            stats.add(42.0);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.mean()).isEqualTo(42.0);
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("should reject NaN input")
        void shouldRejectNaN() {
            StreamingStats stats = new StreamingStats();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> stats.add(Double.NaN))
                    .withMessageContaining("NaN");
        }

        @Test
        @DisplayName("should reject positive infinity")
        void shouldRejectPositiveInfinity() {
            StreamingStats stats = new StreamingStats();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> stats.add(Double.POSITIVE_INFINITY));
        }

        @Test
        @DisplayName("should reject negative infinity")
        void shouldRejectNegativeInfinity() {
            StreamingStats stats = new StreamingStats();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> stats.add(Double.NEGATIVE_INFINITY));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("empty stats toString should indicate empty")
        void emptyToString() {
            StreamingStats stats = new StreamingStats();

            assertThat(stats.toString()).contains("empty");
        }

        @Test
        @DisplayName("non-empty stats toString should contain count and mean")
        void nonEmptyToString() {
            StreamingStats stats = new StreamingStats();
            stats.add(10.0);

            String s = stats.toString();
            assertThat(s).contains("count=1");
            assertThat(s).contains("mean=");
        }
    }
}
