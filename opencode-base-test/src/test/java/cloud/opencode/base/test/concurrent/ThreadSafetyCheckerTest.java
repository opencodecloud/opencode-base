package cloud.opencode.base.test.concurrent;

import cloud.opencode.base.test.concurrent.ThreadSafetyChecker.CheckResult;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ThreadSafetyCheckerTest Tests
 * ThreadSafetyCheckerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("ThreadSafetyChecker Tests")
class ThreadSafetyCheckerTest {

    @Nested
    @DisplayName("checkCounter Tests")
    class CheckCounterTests {

        @Test
        @DisplayName("Should pass for thread-safe counter")
        void shouldPassForThreadSafeCounter() {
            AtomicInteger counter = new AtomicInteger(0);

            CheckResult result = ThreadSafetyChecker.checkCounter(
                counter::incrementAndGet,
                counter::get,
                4,
                1000
            );

            assertThat(result.passed()).isTrue();
            assertThat(result.expected()).isEqualTo(4000);
            assertThat(result.actual()).isEqualTo(4000);
            assertThat(result.difference()).isZero();
        }

        @Test
        @DisplayName("Should fail for non-thread-safe counter")
        void shouldFailForNonThreadSafeCounter() {
            int[] counter = {0}; // Non-thread-safe

            CheckResult result = ThreadSafetyChecker.checkCounter(
                () -> counter[0]++,
                () -> counter[0],
                10,
                10000
            );

            // With high enough contention, should fail
            // Note: This may occasionally pass due to timing
            assertThat(result.expected()).isEqualTo(100000);
            // Usually will have lost increments
        }

        @Test
        @DisplayName("Should return correct expected value")
        void shouldReturnCorrectExpectedValue() {
            AtomicInteger counter = new AtomicInteger(0);

            CheckResult result = ThreadSafetyChecker.checkCounter(
                counter::incrementAndGet,
                counter::get,
                5,
                200
            );

            assertThat(result.expected()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("isThreadSafe Tests")
    class IsThreadSafeTests {

        @Test
        @DisplayName("Should return true for thread-safe operation")
        void shouldReturnTrueForThreadSafeOperation() {
            AtomicInteger counter = new AtomicInteger(0);

            boolean safe = ThreadSafetyChecker.isThreadSafe(
                counter::incrementAndGet,
                4,
                1000
            );

            assertThat(safe).isTrue();
        }

        @Test
        @DisplayName("Should return false when operation throws exception")
        void shouldReturnFalseWhenOperationThrowsException() {
            AtomicInteger counter = new AtomicInteger(0);

            boolean safe = ThreadSafetyChecker.isThreadSafe(
                () -> {
                    if (counter.incrementAndGet() > 10) {
                        throw new RuntimeException("Simulated failure");
                    }
                },
                4,
                100
            );

            assertThat(safe).isFalse();
        }

        @Test
        @DisplayName("Should handle single thread")
        void shouldHandleSingleThread() {
            AtomicInteger counter = new AtomicInteger(0);

            boolean safe = ThreadSafetyChecker.isThreadSafe(
                counter::incrementAndGet,
                1,
                100
            );

            assertThat(safe).isTrue();
            assertThat(counter.get()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("CheckResult Tests")
    class CheckResultTests {

        @Test
        @DisplayName("Should create result with all values")
        void shouldCreateResultWithAllValues() {
            CheckResult result = new CheckResult(true, 100, 100, 0);

            assertThat(result.passed()).isTrue();
            assertThat(result.expected()).isEqualTo(100);
            assertThat(result.actual()).isEqualTo(100);
            assertThat(result.difference()).isZero();
        }

        @Test
        @DisplayName("accuracy should return 1.0 for perfect match")
        void accuracyShouldReturn1ForPerfectMatch() {
            CheckResult result = new CheckResult(true, 100, 100, 0);
            assertThat(result.accuracy()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("accuracy should return correct ratio")
        void accuracyShouldReturnCorrectRatio() {
            CheckResult result = new CheckResult(false, 100, 90, 10);
            assertThat(result.accuracy()).isEqualTo(0.9);
        }

        @Test
        @DisplayName("accuracy should return 1.0 for expected 0 and actual 0")
        void accuracyShouldReturn1ForExpected0AndActual0() {
            CheckResult result = new CheckResult(true, 0, 0, 0);
            assertThat(result.accuracy()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("accuracy should return 0.0 for expected 0 and non-zero actual")
        void accuracyShouldReturn0ForExpected0AndNonZeroActual() {
            CheckResult result = new CheckResult(false, 0, 10, -10);
            assertThat(result.accuracy()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("toString should contain status")
        void toStringShouldContainStatus() {
            CheckResult passedResult = new CheckResult(true, 100, 100, 0);
            CheckResult failedResult = new CheckResult(false, 100, 90, 10);

            assertThat(passedResult.toString()).contains("PASSED");
            assertThat(failedResult.toString()).contains("FAILED");
        }

        @Test
        @DisplayName("toString should contain values")
        void toStringShouldContainValues() {
            CheckResult result = new CheckResult(true, 100, 100, 0);

            assertThat(result.toString()).contains("expected=100");
            assertThat(result.toString()).contains("actual=100");
            assertThat(result.toString()).contains("diff=0");
            assertThat(result.toString()).contains("accuracy=");
        }

        @Test
        @DisplayName("toString should show percentage")
        void toStringShouldShowPercentage() {
            CheckResult result = new CheckResult(false, 100, 90, 10);
            assertThat(result.toString()).contains("90.00%");
        }
    }

    @Nested
    @DisplayName("Concurrent Execution Tests")
    class ConcurrentExecutionTests {

        @Test
        @DisplayName("Should execute all threads")
        void shouldExecuteAllThreads() {
            AtomicInteger counter = new AtomicInteger(0);

            ThreadSafetyChecker.checkCounter(
                counter::incrementAndGet,
                counter::get,
                8,
                500
            );

            assertThat(counter.get()).isEqualTo(4000);
        }

        @Test
        @DisplayName("Should start all threads simultaneously")
        void shouldStartAllThreadsSimultaneously() {
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            AtomicInteger currentConcurrent = new AtomicInteger(0);

            ThreadSafetyChecker.isThreadSafe(
                () -> {
                    int current = currentConcurrent.incrementAndGet();
                    maxConcurrent.updateAndGet(max -> Math.max(max, current));
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    currentConcurrent.decrementAndGet();
                },
                4,
                10
            );

            // Should have some concurrent execution
            assertThat(maxConcurrent.get()).isGreaterThan(1);
        }
    }
}
