package cloud.opencode.base.test.benchmark;

import cloud.opencode.base.test.exception.BenchmarkException;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * BenchmarkRunnerTest Tests
 * BenchmarkRunnerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("BenchmarkRunner Tests")
class BenchmarkRunnerTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create should return new runner")
        void createShouldReturnNewRunner() {
            BenchmarkRunner runner = BenchmarkRunner.create();
            assertThat(runner).isNotNull();
        }

        @Test
        @DisplayName("runSingle should run single benchmark")
        void runSingleShouldRunSingleBenchmark() {
            AtomicInteger counter = new AtomicInteger(0);
            BenchmarkResult result = BenchmarkRunner.runSingle("test", counter::incrementAndGet);

            assertThat(result.name()).isEqualTo("test");
            assertThat(result.iterations()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("warmup should set warmup iterations")
        void warmupShouldSetWarmupIterations() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .warmup(50)
                .iterations(10)
                .add("test", () -> {});

            List<BenchmarkResult> results = runner.run();
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("warmup should throw for negative iterations")
        void warmupShouldThrowForNegativeIterations() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> BenchmarkRunner.create().warmup(-1));
        }

        @Test
        @DisplayName("iterations should set measure iterations")
        void iterationsShouldSetMeasureIterations() {
            AtomicInteger counter = new AtomicInteger(0);
            BenchmarkRunner runner = BenchmarkRunner.create()
                .warmup(0)
                .iterations(50)
                .add("test", counter::incrementAndGet);

            runner.run();
            assertThat(counter.get()).isEqualTo(50);
        }

        @Test
        @DisplayName("iterations should throw for non-positive iterations")
        void iterationsShouldThrowForNonPositiveIterations() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> BenchmarkRunner.create().iterations(0));
        }

        @Test
        @DisplayName("timeout should set timeout duration")
        void timeoutShouldSetTimeoutDuration() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .timeout(Duration.ofSeconds(10))
                .iterations(10)
                .add("test", () -> {});

            // Should not throw
            assertThatNoException().isThrownBy(runner::run);
        }

        @Test
        @DisplayName("timeout should throw for null")
        void timeoutShouldThrowForNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> BenchmarkRunner.create().timeout(null));
        }

        @Test
        @DisplayName("output should set output stream")
        void outputShouldSetOutputStream() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .output(ps)
                .verbose()
                .warmup(1)
                .iterations(10)
                .add("test", () -> {});

            runner.run();

            assertThat(baos.toString()).contains("Starting benchmarks");
        }

        @Test
        @DisplayName("output should throw for null")
        void outputShouldThrowForNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> BenchmarkRunner.create().output(null));
        }

        @Test
        @DisplayName("verbose should enable verbose output")
        void verboseShouldEnableVerboseOutput() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .output(ps)
                .verbose()
                .warmup(1)
                .iterations(10)
                .add("test", () -> {});

            runner.run();

            assertThat(baos.toString()).contains("Running:");
            assertThat(baos.toString()).contains("Completed:");
        }
    }

    @Nested
    @DisplayName("Benchmark Registration Tests")
    class BenchmarkRegistrationTests {

        @Test
        @DisplayName("add should register benchmark")
        void addShouldRegisterBenchmark() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .iterations(10)
                .add("test", () -> {});

            List<BenchmarkResult> results = runner.run();
            assertThat(results).hasSize(1);
            assertThat(results.getFirst().name()).isEqualTo("test");
        }

        @Test
        @DisplayName("add should throw for null name")
        void addShouldThrowForNullName() {
            assertThatNullPointerException()
                .isThrownBy(() -> BenchmarkRunner.create().add(null, () -> {}));
        }

        @Test
        @DisplayName("add should throw for null runnable")
        void addShouldThrowForNullRunnable() {
            assertThatNullPointerException()
                .isThrownBy(() -> BenchmarkRunner.create().add("test", (Runnable) null));
        }

        @Test
        @DisplayName("add should throw for duplicate name")
        void addShouldThrowForDuplicateName() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> BenchmarkRunner.create()
                    .add("test", () -> {})
                    .add("test", () -> {}));
        }

        @Test
        @DisplayName("add with callable should register benchmark")
        void addWithCallableShouldRegisterBenchmark() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .iterations(10)
                .add("test", () -> "result");

            List<BenchmarkResult> results = runner.run();
            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Execution Tests")
    class ExecutionTests {

        @Test
        @DisplayName("run should throw when no benchmarks registered")
        void runShouldThrowWhenNoBenchmarksRegistered() {
            BenchmarkRunner runner = BenchmarkRunner.create();

            assertThatIllegalStateException()
                .isThrownBy(runner::run)
                .withMessageContaining("No benchmarks registered");
        }

        @Test
        @DisplayName("run should execute all benchmarks")
        void runShouldExecuteAllBenchmarks() {
            AtomicInteger counter1 = new AtomicInteger(0);
            AtomicInteger counter2 = new AtomicInteger(0);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .warmup(0)
                .iterations(10)
                .add("test1", counter1::incrementAndGet)
                .add("test2", counter2::incrementAndGet);

            runner.run();

            assertThat(counter1.get()).isEqualTo(10);
            assertThat(counter2.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("run should throw BenchmarkException on failure")
        void runShouldThrowBenchmarkExceptionOnFailure() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .warmup(0)
                .iterations(10)
                .add("failing", () -> { throw new RuntimeException("error"); });

            assertThatThrownBy(runner::run)
                .isInstanceOf(BenchmarkException.class)
                .hasMessageContaining("Benchmark failed");
        }
    }

    @Nested
    @DisplayName("Results Tests")
    class ResultsTests {

        @Test
        @DisplayName("getResults should return results after run")
        void getResultsShouldReturnResultsAfterRun() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .iterations(10)
                .add("test", () -> {});

            runner.run();
            List<BenchmarkResult> results = runner.getResults();

            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("getResults should throw when run not called")
        void getResultsShouldThrowWhenRunNotCalled() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .add("test", () -> {});

            assertThatIllegalStateException()
                .isThrownBy(runner::getResults)
                .withMessageContaining("Must call run()");
        }

        @Test
        @DisplayName("printResults should print formatted results")
        void printResultsShouldPrintFormattedResults() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .output(ps)
                .iterations(10)
                .add("test", () -> {});

            runner.run();
            runner.printResults();

            String output = baos.toString();
            assertThat(output).contains("BENCHMARK RESULTS");
            assertThat(output).contains("test");
            assertThat(output).contains("Avg");
        }

        @Test
        @DisplayName("printResults should throw when run not called")
        void printResultsShouldThrowWhenRunNotCalled() {
            BenchmarkRunner runner = BenchmarkRunner.create()
                .add("test", () -> {});

            assertThatIllegalStateException()
                .isThrownBy(runner::printResults);
        }

        @Test
        @DisplayName("printComparison should print comparison when multiple benchmarks")
        void printComparisonShouldPrintComparisonWhenMultipleBenchmarks() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .output(ps)
                .iterations(10)
                .add("test1", () -> {})
                .add("test2", () -> {});

            runner.run();
            runner.printComparison();

            String output = baos.toString();
            assertThat(output).contains("COMPARISON");
            assertThat(output).contains("Baseline");
        }

        @Test
        @DisplayName("printComparison should do nothing for single benchmark")
        void printComparisonShouldDoNothingForSingleBenchmark() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .output(ps)
                .iterations(10)
                .add("test", () -> {});

            runner.run();
            runner.printComparison();

            String output = baos.toString();
            assertThat(output).doesNotContain("COMPARISON");
        }

        @Test
        @DisplayName("onComplete should execute callback with results")
        void onCompleteShouldExecuteCallbackWithResults() {
            AtomicInteger callbackCount = new AtomicInteger(0);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .iterations(10)
                .add("test", () -> {});

            runner.run();
            runner.onComplete(results -> callbackCount.incrementAndGet());

            assertThat(callbackCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("onComplete should not execute callback when not run")
        void onCompleteShouldNotExecuteCallbackWhenNotRun() {
            AtomicInteger callbackCount = new AtomicInteger(0);

            BenchmarkRunner runner = BenchmarkRunner.create()
                .add("test", () -> {});

            runner.onComplete(results -> callbackCount.incrementAndGet());

            assertThat(callbackCount.get()).isEqualTo(0);
        }
    }
}
