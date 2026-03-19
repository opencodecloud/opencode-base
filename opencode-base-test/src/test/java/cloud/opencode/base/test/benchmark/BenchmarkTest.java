package cloud.opencode.base.test.benchmark;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * BenchmarkTest Tests
 * BenchmarkTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("Benchmark Tests")
class BenchmarkTest {

    @Nested
    @DisplayName("time(Runnable) Tests")
    class TimeRunnableTests {

        @Test
        @DisplayName("Should measure execution time")
        void shouldMeasureExecutionTime() {
            Duration duration = Benchmark.time(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            assertThat(duration.toMillis()).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("Should return non-negative duration")
        void shouldReturnNonNegativeDuration() {
            Duration duration = Benchmark.time(() -> {});
            assertThat(duration.isNegative()).isFalse();
        }
    }

    @Nested
    @DisplayName("time(Supplier) Tests")
    class TimeSupplierTests {

        @Test
        @DisplayName("Should measure execution time and return result")
        void shouldMeasureExecutionTimeAndReturnResult() {
            Benchmark.TimedResult<String> result = Benchmark.time(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "result";
            });

            assertThat(result.result()).isEqualTo("result");
            assertThat(result.duration().toMillis()).isGreaterThanOrEqualTo(40);
        }

        @Test
        @DisplayName("TimedResult millis should return milliseconds")
        void timedResultMillisShouldReturnMilliseconds() {
            Benchmark.TimedResult<String> result = Benchmark.time(() -> "result");
            assertThat(result.millis()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("TimedResult nanos should return nanoseconds")
        void timedResultNanosShouldReturnNanoseconds() {
            Benchmark.TimedResult<String> result = Benchmark.time(() -> "result");
            assertThat(result.nanos()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("run() Tests")
    class RunTests {

        @Test
        @DisplayName("Should run benchmark with default iterations")
        void shouldRunBenchmarkWithDefaultIterations() {
            AtomicInteger counter = new AtomicInteger(0);

            Benchmark.BenchmarkResult result = Benchmark.run("test", counter::incrementAndGet);

            assertThat(result.getName()).isEqualTo("test");
            assertThat(result.getCount()).isEqualTo(20); // Default 20 measure iterations
        }

        @Test
        @DisplayName("Should run benchmark with custom iterations")
        void shouldRunBenchmarkWithCustomIterations() {
            AtomicInteger counter = new AtomicInteger(0);

            Benchmark.BenchmarkResult result = Benchmark.run("test", counter::incrementAndGet, 10, 50);

            assertThat(result.getCount()).isEqualTo(50);
            // Total calls = warmup + measure = 10 + 50 = 60
            assertThat(counter.get()).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("BenchmarkResult Tests")
    class BenchmarkResultTests {

        @Test
        @DisplayName("Should calculate min, max, average")
        void shouldCalculateMinMaxAverage() {
            Benchmark.BenchmarkResult result = Benchmark.run("test", () -> {});

            assertThat(result.getMin()).isGreaterThanOrEqualTo(0);
            assertThat(result.getMax()).isGreaterThanOrEqualTo(result.getMin());
            assertThat(result.getAverage()).isGreaterThanOrEqualTo(result.getMin());
            assertThat(result.getAverage()).isLessThanOrEqualTo(result.getMax());
        }

        @Test
        @DisplayName("Should return durations")
        void shouldReturnDurations() {
            Benchmark.BenchmarkResult result = Benchmark.run("test", () -> {});

            assertThat(result.getMinDuration().isNegative()).isFalse();
            assertThat(result.getMaxDuration().isNegative()).isFalse();
            assertThat(result.getAverageDuration().isNegative()).isFalse();
        }

        @Test
        @DisplayName("Should calculate ops per second")
        void shouldCalculateOpsPerSecond() {
            Benchmark.BenchmarkResult result = Benchmark.run("test", () -> {});

            assertThat(result.getOpsPerSecond()).isGreaterThan(0);
        }

        @Test
        @DisplayName("toString should return formatted string")
        void toStringShouldReturnFormattedString() {
            Benchmark.BenchmarkResult result = Benchmark.run("test", () -> {});

            String str = result.toString();
            assertThat(str).contains("test:");
            assertThat(str).contains("avg=");
            assertThat(str).contains("min=");
            assertThat(str).contains("max=");
            assertThat(str).contains("ops/s=");
        }
    }

    @Nested
    @DisplayName("compare() Tests")
    class CompareTests {

        @Test
        @DisplayName("Should compare two implementations")
        void shouldCompareTwoImplementations() {
            Benchmark.ComparisonResult result = Benchmark.compare(
                "fast", () -> {},
                "slow", () -> {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            );

            assertThat(result.first().getName()).isEqualTo("fast");
            assertThat(result.second().getName()).isEqualTo("slow");
        }

        @Test
        @DisplayName("getSpeedup should calculate speedup ratio")
        void getSpeedupShouldCalculateSpeedupRatio() {
            Benchmark.ComparisonResult result = Benchmark.compare(
                "fast", () -> {},
                "slow", () -> {}
            );

            assertThat(result.getSpeedup()).isGreaterThan(0);
        }

        @Test
        @DisplayName("getFaster should return faster implementation name")
        void getFasterShouldReturnFasterImplementationName() {
            Benchmark.ComparisonResult result = Benchmark.compare(
                "fast", () -> {},
                "slow", () -> {}
            );

            String faster = result.getFaster();
            assertThat(faster).isIn("fast", "slow");
        }

        @Test
        @DisplayName("toString should return formatted comparison")
        void toStringShouldReturnFormattedComparison() {
            Benchmark.ComparisonResult result = Benchmark.compare(
                "impl1", () -> {},
                "impl2", () -> {}
            );

            String str = result.toString();
            assertThat(str).contains("Comparison:");
            assertThat(str).contains("faster than");
        }
    }
}
