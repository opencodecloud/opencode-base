package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeSeriesStatsTest Tests
 * TimeSeriesStatsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TimeSeriesStats Tests")
class TimeSeriesStatsTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create stats with all values")
        void shouldCreateStatsWithAllValues() {
            TimeSeriesStats stats = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 7.07);

            assertThat(stats.count()).isEqualTo(10);
            assertThat(stats.sum()).isEqualTo(100.0);
            assertThat(stats.average()).isEqualTo(10.0);
            assertThat(stats.min()).isEqualTo(5.0);
            assertThat(stats.max()).isEqualTo(25.0);
            assertThat(stats.stdDev()).isEqualTo(7.07);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("empty should create stats with zero values")
        void emptyShouldCreateStatsWithZeroValues() {
            TimeSeriesStats stats = TimeSeriesStats.empty();

            assertThat(stats.count()).isZero();
            assertThat(stats.sum()).isZero();
            assertThat(stats.average()).isZero();
            assertThat(stats.min()).isZero();
            assertThat(stats.max()).isZero();
            assertThat(stats.stdDev()).isZero();
        }
    }

    @Nested
    @DisplayName("Computed Values Tests")
    class ComputedValuesTests {

        @Test
        @DisplayName("isEmpty should return true for zero count")
        void isEmptyShouldReturnTrueForZeroCount() {
            TimeSeriesStats stats = TimeSeriesStats.empty();

            assertThat(stats.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty should return false for non-zero count")
        void isEmptyShouldReturnFalseForNonZeroCount() {
            TimeSeriesStats stats = new TimeSeriesStats(5, 50.0, 10.0, 5.0, 15.0, 3.16);

            assertThat(stats.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("range should return difference between max and min")
        void rangeShouldReturnDifferenceBetweenMaxAndMin() {
            TimeSeriesStats stats = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);

            assertThat(stats.range()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("variance should return square of stdDev")
        void varianceShouldReturnSquareOfStdDev() {
            TimeSeriesStats stats = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);

            assertThat(stats.variance()).isEqualTo(25.0);
        }
    }

    @Nested
    @DisplayName("Record Method Tests")
    class RecordMethodTests {

        @Test
        @DisplayName("equals should compare all fields")
        void equalsShouldCompareAllFields() {
            TimeSeriesStats stats1 = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);
            TimeSeriesStats stats2 = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);
            TimeSeriesStats stats3 = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 6.0);

            assertThat(stats1).isEqualTo(stats2);
            assertThat(stats1).isNotEqualTo(stats3);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            TimeSeriesStats stats1 = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);
            TimeSeriesStats stats2 = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);

            assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
        }

        @Test
        @DisplayName("toString should contain all field values")
        void toStringShouldContainAllFieldValues() {
            TimeSeriesStats stats = new TimeSeriesStats(10, 100.0, 10.0, 5.0, 25.0, 5.0);
            String str = stats.toString();

            assertThat(str).contains("10");
            assertThat(str).contains("100.0");
            assertThat(str).contains("5.0");
            assertThat(str).contains("25.0");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle negative values")
        void shouldHandleNegativeValues() {
            TimeSeriesStats stats = new TimeSeriesStats(5, -50.0, -10.0, -20.0, -5.0, 6.0);

            assertThat(stats.sum()).isEqualTo(-50.0);
            assertThat(stats.average()).isEqualTo(-10.0);
            assertThat(stats.min()).isEqualTo(-20.0);
            assertThat(stats.max()).isEqualTo(-5.0);
            assertThat(stats.range()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("should handle single value stats")
        void shouldHandleSingleValueStats() {
            TimeSeriesStats stats = new TimeSeriesStats(1, 42.0, 42.0, 42.0, 42.0, 0.0);

            assertThat(stats.count()).isEqualTo(1);
            assertThat(stats.range()).isZero();
            assertThat(stats.variance()).isZero();
        }

        @Test
        @DisplayName("should handle very large values")
        void shouldHandleVeryLargeValues() {
            TimeSeriesStats stats = new TimeSeriesStats(
                Long.MAX_VALUE / 2,
                Double.MAX_VALUE / 2,
                1000000.0,
                0.0,
                Double.MAX_VALUE / 2,
                10000.0
            );

            assertThat(stats.isEmpty()).isFalse();
        }
    }
}
