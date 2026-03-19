package cloud.opencode.base.test.concurrent;

import cloud.opencode.base.test.exception.TestException;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ConcurrentTesterTest Tests
 * ConcurrentTesterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("ConcurrentTester Tests")
class ConcurrentTesterTest {

    @Nested
    @DisplayName("runConcurrently with Runnable Tests")
    class RunConcurrentlyWithRunnableTests {

        @Test
        @DisplayName("Should run task concurrently")
        void shouldRunTaskConcurrently() {
            AtomicInteger counter = new AtomicInteger(0);

            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                counter::incrementAndGet,
                4,
                10
            );

            assertThat(result.threads()).isEqualTo(4);
            assertThat(result.iterationsPerThread()).isEqualTo(10);
            assertThat(result.totalIterations()).isEqualTo(40);
            assertThat(result.successCount()).isEqualTo(40);
            assertThat(result.failureCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should count failures")
        void shouldCountFailures() {
            AtomicInteger counter = new AtomicInteger(0);

            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {
                    if (counter.incrementAndGet() % 2 == 0) {
                        throw new RuntimeException("even");
                    }
                },
                2,
                10
            );

            assertThat(result.failureCount()).isGreaterThan(0);
            assertThat(result.errors()).isNotEmpty();
        }

        @Test
        @DisplayName("Should track duration")
        void shouldTrackDuration() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                },
                2,
                5
            );

            assertThat(result.totalDuration().toMillis()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("runConcurrently with Consumer Tests")
    class RunConcurrentlyWithConsumerTests {

        @Test
        @DisplayName("Should run task with thread index")
        void shouldRunTaskWithThreadIndex() {
            AtomicInteger maxIndex = new AtomicInteger(-1);

            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                index -> maxIndex.accumulateAndGet(index, Math::max),
                4
            );

            assertThat(result.threads()).isEqualTo(4);
            assertThat(maxIndex.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle exceptions")
        void shouldHandleExceptions() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                index -> {
                    if (index == 0) {
                        throw new RuntimeException("error");
                    }
                },
                4
            );

            assertThat(result.failureCount()).isEqualTo(1);
            assertThat(result.successCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("assertThreadSafe Tests")
    class AssertThreadSafeTests {

        @Test
        @DisplayName("Should pass when task is thread-safe")
        void shouldPassWhenTaskIsThreadSafe() {
            AtomicInteger counter = new AtomicInteger(0);

            assertThatNoException().isThrownBy(() ->
                ConcurrentTester.assertThreadSafe(
                    counter::incrementAndGet,
                    4,
                    100
                )
            );
        }

        @Test
        @DisplayName("Should throw when task fails")
        void shouldThrowWhenTaskFails() {
            assertThatThrownBy(() ->
                ConcurrentTester.assertThreadSafe(
                    () -> { throw new RuntimeException("error"); },
                    4,
                    10
                ))
                .isInstanceOf(TestException.class)
                .hasMessageContaining("Thread safety test failed");
        }
    }

    @Nested
    @DisplayName("ConcurrentResult Tests")
    class ConcurrentResultTests {

        @Test
        @DisplayName("totalIterations should return threads * iterations")
        void totalIterationsShouldReturnThreadsTimesIterations() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {},
                4,
                10
            );

            assertThat(result.totalIterations()).isEqualTo(40);
        }

        @Test
        @DisplayName("throughput should calculate operations per second")
        void throughputShouldCalculateOperationsPerSecond() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {},
                4,
                100
            );

            assertThat(result.throughput()).isGreaterThan(0);
        }

        @Test
        @DisplayName("allSucceeded should return true when no failures")
        void allSucceededShouldReturnTrueWhenNoFailures() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {},
                4,
                10
            );

            assertThat(result.allSucceeded()).isTrue();
        }

        @Test
        @DisplayName("allSucceeded should return false when has failures")
        void allSucceededShouldReturnFalseWhenHasFailures() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> { throw new RuntimeException("error"); },
                4,
                10
            );

            assertThat(result.allSucceeded()).isFalse();
        }

        @Test
        @DisplayName("toString should return formatted string")
        void toStringShouldReturnFormattedString() {
            ConcurrentTester.ConcurrentResult result = ConcurrentTester.runConcurrently(
                () -> {},
                4,
                10
            );

            String str = result.toString();
            assertThat(str).contains("ConcurrentResult");
            assertThat(str).contains("threads=4");
            assertThat(str).contains("iterations=40");
            assertThat(str).contains("success=");
            assertThat(str).contains("throughput=");
        }
    }
}
