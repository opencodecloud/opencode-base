package cloud.opencode.base.test.benchmark;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BenchmarkResultTest Tests
 * BenchmarkResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("BenchmarkResult Tests")
class BenchmarkResultTest {

    private BenchmarkResult createResult() {
        return new BenchmarkResult("test", new long[]{1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000});
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create with name and times")
        void shouldCreateWithNameAndTimes() {
            long[] times = {1_000_000, 2_000_000};
            BenchmarkResult result = new BenchmarkResult("myBenchmark", times);

            assertThat(result.name()).isEqualTo("myBenchmark");
            assertThat(result.timesNanos()).isEqualTo(times);
        }
    }

    @Nested
    @DisplayName("iterations() Tests")
    class IterationsTests {

        @Test
        @DisplayName("Should return correct iteration count")
        void shouldReturnCorrectIterationCount() {
            BenchmarkResult result = createResult();
            assertThat(result.iterations()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Average Tests")
    class AverageTests {

        @Test
        @DisplayName("averageMs() should calculate correct average")
        void averageMsShouldCalculateCorrectAverage() {
            BenchmarkResult result = createResult();
            // Average of 1,2,3,4,5 million nanos = 3 million nanos = 3ms
            assertThat(result.averageMs()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("averageNanos() should calculate correct average")
        void averageNanosShouldCalculateCorrectAverage() {
            BenchmarkResult result = createResult();
            assertThat(result.averageNanos()).isEqualTo(3_000_000.0);
        }
    }

    @Nested
    @DisplayName("Min/Max Tests")
    class MinMaxTests {

        @Test
        @DisplayName("minMs() should return minimum time")
        void minMsShouldReturnMinimumTime() {
            BenchmarkResult result = createResult();
            assertThat(result.minMs()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("maxMs() should return maximum time")
        void maxMsShouldReturnMaximumTime() {
            BenchmarkResult result = createResult();
            assertThat(result.maxMs()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("Percentile Tests")
    class PercentileTests {

        @Test
        @DisplayName("percentileMs() should calculate correct percentile")
        void percentileMsShouldCalculateCorrectPercentile() {
            BenchmarkResult result = createResult();
            // For sorted values [1,2,3,4,5] and p50, index = ceil(0.5*5)-1 = 2
            assertThat(result.percentileMs(50)).isGreaterThan(0);
        }

        @Test
        @DisplayName("percentileMs() should throw for invalid percentile")
        void percentileMsShouldThrowForInvalidPercentile() {
            BenchmarkResult result = createResult();

            assertThatIllegalArgumentException()
                .isThrownBy(() -> result.percentileMs(-1));
            assertThatIllegalArgumentException()
                .isThrownBy(() -> result.percentileMs(101));
        }

        @Test
        @DisplayName("medianMs() should return 50th percentile")
        void medianMsShouldReturn50thPercentile() {
            BenchmarkResult result = createResult();
            assertThat(result.medianMs()).isEqualTo(result.percentileMs(50));
        }

        @Test
        @DisplayName("p95Ms() should return 95th percentile")
        void p95MsShouldReturn95thPercentile() {
            BenchmarkResult result = createResult();
            assertThat(result.p95Ms()).isEqualTo(result.percentileMs(95));
        }

        @Test
        @DisplayName("p99Ms() should return 99th percentile")
        void p99MsShouldReturn99thPercentile() {
            BenchmarkResult result = createResult();
            assertThat(result.p99Ms()).isEqualTo(result.percentileMs(99));
        }
    }

    @Nested
    @DisplayName("Total Time Tests")
    class TotalTimeTests {

        @Test
        @DisplayName("totalMs() should return sum of all times")
        void totalMsShouldReturnSumOfAllTimes() {
            BenchmarkResult result = createResult();
            // Sum of 1+2+3+4+5 million nanos = 15 million nanos = 15ms
            assertThat(result.totalMs()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Throughput Tests")
    class ThroughputTests {

        @Test
        @DisplayName("throughputPerSecond() should calculate operations per second")
        void throughputPerSecondShouldCalculateOperationsPerSecond() {
            BenchmarkResult result = createResult();
            // Average is 3ms, so throughput = 1000/3 = ~333.33 ops/sec
            assertThat(result.throughputPerSecond()).isGreaterThan(0);
        }

        @Test
        @DisplayName("throughputPerSecond() should return 0 for zero average")
        void throughputPerSecondShouldReturnZeroForZeroAverage() {
            BenchmarkResult result = new BenchmarkResult("test", new long[0]);
            assertThat(result.throughputPerSecond()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Standard Deviation Tests")
    class StandardDeviationTests {

        @Test
        @DisplayName("stdDevMs() should calculate standard deviation")
        void stdDevMsShouldCalculateStandardDeviation() {
            BenchmarkResult result = createResult();
            assertThat(result.stdDevMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Summary Tests")
    class SummaryTests {

        @Test
        @DisplayName("summary() should return formatted string")
        void summaryShouldReturnFormattedString() {
            BenchmarkResult result = createResult();
            String summary = result.summary();

            assertThat(summary).contains("test:");
            assertThat(summary).contains("avg=");
            assertThat(summary).contains("min=");
            assertThat(summary).contains("max=");
            assertThat(summary).contains("p50=");
            assertThat(summary).contains("p95=");
            assertThat(summary).contains("p99=");
            assertThat(summary).contains("stdDev=");
            assertThat(summary).contains("throughput=");
        }

        @Test
        @DisplayName("toString() should return summary")
        void toStringShouldReturnSummary() {
            BenchmarkResult result = createResult();
            assertThat(result.toString()).isEqualTo(result.summary());
        }
    }
}
