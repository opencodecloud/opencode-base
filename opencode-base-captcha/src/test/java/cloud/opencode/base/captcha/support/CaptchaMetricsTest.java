package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaMetrics Test - Unit tests for CAPTCHA metrics collection
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
class CaptchaMetricsTest {

    private CaptchaMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = CaptchaMetrics.create();
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("should create metrics with zero counters")
        void shouldCreateWithZeroCounters() {
            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalGenerated()).isZero();
            assertThat(snapshot.totalValidations()).isZero();
            assertThat(snapshot.successfulValidations()).isZero();
            assertThat(snapshot.failedValidations()).isZero();
            assertThat(snapshot.successRate()).isEqualTo(0.0);
            assertThat(snapshot.averageResponseTime()).isEqualTo(Duration.ZERO);
            assertThat(snapshot.generationsByType()).isEmpty();
        }

        @Test
        @DisplayName("should have non-negative uptime")
        void shouldHaveNonNegativeUptime() {
            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.uptime().isNegative()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record Generation Tests")
    class RecordGenerationTests {

        @Test
        @DisplayName("should increment total generated count")
        void shouldIncrementTotalGenerated() {
            metrics.recordGeneration(CaptchaType.ALPHANUMERIC);
            metrics.recordGeneration(CaptchaType.NUMERIC);
            metrics.recordGeneration(CaptchaType.ALPHANUMERIC);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalGenerated()).isEqualTo(3);
        }

        @Test
        @DisplayName("should track generation counts by type")
        void shouldTrackByType() {
            metrics.recordGeneration(CaptchaType.ALPHANUMERIC);
            metrics.recordGeneration(CaptchaType.ALPHANUMERIC);
            metrics.recordGeneration(CaptchaType.NUMERIC);
            metrics.recordGeneration(CaptchaType.GIF);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.generationsByType())
                    .containsEntry(CaptchaType.ALPHANUMERIC, 2L)
                    .containsEntry(CaptchaType.NUMERIC, 1L)
                    .containsEntry(CaptchaType.GIF, 1L)
                    .hasSize(3);
        }

        @Test
        @DisplayName("should reject null type")
        void shouldRejectNullType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> metrics.recordGeneration(null))
                    .withMessageContaining("type must not be null");
        }
    }

    @Nested
    @DisplayName("Record Validation Tests")
    class RecordValidationTests {

        @Test
        @DisplayName("should count successful validations")
        void shouldCountSuccessfulValidations() {
            metrics.recordValidation(true);
            metrics.recordValidation(true);
            metrics.recordValidation(true);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalValidations()).isEqualTo(3);
            assertThat(snapshot.successfulValidations()).isEqualTo(3);
            assertThat(snapshot.failedValidations()).isZero();
            assertThat(snapshot.successRate()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should count failed validations")
        void shouldCountFailedValidations() {
            metrics.recordValidation(false);
            metrics.recordValidation(false);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalValidations()).isEqualTo(2);
            assertThat(snapshot.successfulValidations()).isZero();
            assertThat(snapshot.failedValidations()).isEqualTo(2);
            assertThat(snapshot.successRate()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should calculate mixed success rate")
        void shouldCalculateMixedSuccessRate() {
            metrics.recordValidation(true);
            metrics.recordValidation(false);
            metrics.recordValidation(true);
            metrics.recordValidation(false);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalValidations()).isEqualTo(4);
            assertThat(snapshot.successfulValidations()).isEqualTo(2);
            assertThat(snapshot.failedValidations()).isEqualTo(2);
            assertThat(snapshot.successRate()).isCloseTo(0.5, within(0.001));
        }

        @Test
        @DisplayName("should track response time")
        void shouldTrackResponseTime() {
            metrics.recordValidation(true, Duration.ofMillis(100));
            metrics.recordValidation(true, Duration.ofMillis(200));
            metrics.recordValidation(false, Duration.ofMillis(300));

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.averageResponseTime()).isEqualTo(Duration.ofMillis(200));
        }

        @Test
        @DisplayName("should handle null response time gracefully")
        void shouldHandleNullResponseTime() {
            metrics.recordValidation(true, null);
            metrics.recordValidation(false);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.totalValidations()).isEqualTo(2);
            assertThat(snapshot.averageResponseTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("should return zero average response time when no validations")
        void shouldReturnZeroAvgWhenNoValidations() {
            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.averageResponseTime()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("Snapshot Tests")
    class SnapshotTests {

        @Test
        @DisplayName("snapshot generationsByType map should be unmodifiable")
        void snapshotMapShouldBeUnmodifiable() {
            metrics.recordGeneration(CaptchaType.NUMERIC);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> snapshot.generationsByType().put(CaptchaType.GIF, 99L));
        }

        @Test
        @DisplayName("snapshot should be independent of subsequent changes")
        void snapshotShouldBeIndependent() {
            metrics.recordGeneration(CaptchaType.NUMERIC);
            CaptchaMetrics.MetricsSnapshot before = metrics.snapshot();

            metrics.recordGeneration(CaptchaType.NUMERIC);
            metrics.recordGeneration(CaptchaType.NUMERIC);

            assertThat(before.totalGenerated()).isEqualTo(1);
            assertThat(metrics.snapshot().totalGenerated()).isEqualTo(3);
        }

        @Test
        @DisplayName("snapshot should include uptime")
        void snapshotShouldIncludeUptime() throws Exception {
            Thread.sleep(10);
            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();

            assertThat(snapshot.uptime().toMillis()).isGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {

        @Test
        @DisplayName("should reset all counters to zero")
        void shouldResetAllCounters() {
            metrics.recordGeneration(CaptchaType.ALPHANUMERIC);
            metrics.recordGeneration(CaptchaType.GIF);
            metrics.recordValidation(true, Duration.ofMillis(100));
            metrics.recordValidation(false, Duration.ofMillis(200));

            metrics.reset();

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.totalGenerated()).isZero();
            assertThat(snapshot.totalValidations()).isZero();
            assertThat(snapshot.successfulValidations()).isZero();
            assertThat(snapshot.failedValidations()).isZero();
            assertThat(snapshot.successRate()).isEqualTo(0.0);
            assertThat(snapshot.averageResponseTime()).isEqualTo(Duration.ZERO);
            assertThat(snapshot.generationsByType()).isEmpty();
        }

        @Test
        @DisplayName("should reset uptime after reset")
        void shouldResetUptime() throws Exception {
            Thread.sleep(20);
            metrics.reset();

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.uptime().toMillis()).isLessThan(20);
        }

        @Test
        @DisplayName("should be usable after reset")
        void shouldBeUsableAfterReset() {
            metrics.recordGeneration(CaptchaType.NUMERIC);
            metrics.reset();
            metrics.recordGeneration(CaptchaType.GIF);
            metrics.recordValidation(true);

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.totalGenerated()).isEqualTo(1);
            assertThat(snapshot.totalValidations()).isEqualTo(1);
            assertThat(snapshot.generationsByType())
                    .containsEntry(CaptchaType.GIF, 1L)
                    .doesNotContainKey(CaptchaType.NUMERIC);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent generation recording")
        void shouldHandleConcurrentGeneration() throws Exception {
            int threadCount = 8;
            int iterationsPerThread = 1000;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            CaptchaType[] types = {CaptchaType.NUMERIC, CaptchaType.ALPHANUMERIC, CaptchaType.GIF, CaptchaType.ALPHA};

            List<Thread> threads = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                final int threadIdx = t;
                Thread thread = Thread.ofVirtual().start(() -> {
                    try {
                        startLatch.await();
                        CaptchaType type = types[threadIdx % types.length];
                        for (int i = 0; i < iterationsPerThread; i++) {
                            metrics.recordGeneration(type);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                threads.add(thread);
            }

            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.totalGenerated()).isEqualTo((long) threadCount * iterationsPerThread);

            long typeSum = snapshot.generationsByType().values().stream().mapToLong(Long::longValue).sum();
            assertThat(typeSum).isEqualTo((long) threadCount * iterationsPerThread);
        }

        @Test
        @DisplayName("should handle concurrent validation recording")
        void shouldHandleConcurrentValidation() throws Exception {
            int threadCount = 8;
            int iterationsPerThread = 1000;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int t = 0; t < threadCount; t++) {
                final boolean success = t % 2 == 0;
                Thread.ofVirtual().start(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < iterationsPerThread; i++) {
                            metrics.recordValidation(success, Duration.ofNanos(100));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

            CaptchaMetrics.MetricsSnapshot snapshot = metrics.snapshot();
            assertThat(snapshot.totalValidations()).isEqualTo((long) threadCount * iterationsPerThread);
            assertThat(snapshot.successfulValidations() + snapshot.failedValidations())
                    .isEqualTo(snapshot.totalValidations());
        }
    }

    @Nested
    @DisplayName("MetricsSnapshot Record Tests")
    class MetricsSnapshotTests {

        @Test
        @DisplayName("should reject null averageResponseTime")
        void shouldRejectNullAvgResponseTime() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CaptchaMetrics.MetricsSnapshot(
                            0, 0, 0, 0, 0.0, null, java.util.Map.of(), Duration.ZERO));
        }

        @Test
        @DisplayName("should reject null generationsByType")
        void shouldRejectNullGenerationsByType() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CaptchaMetrics.MetricsSnapshot(
                            0, 0, 0, 0, 0.0, Duration.ZERO, null, Duration.ZERO));
        }

        @Test
        @DisplayName("should reject null uptime")
        void shouldRejectNullUptime() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new CaptchaMetrics.MetricsSnapshot(
                            0, 0, 0, 0, 0.0, Duration.ZERO, java.util.Map.of(), null));
        }

        @Test
        @DisplayName("should defensively copy generationsByType map")
        void shouldDefensivelyCopyMap() {
            java.util.Map<CaptchaType, Long> mutableMap = new java.util.HashMap<>();
            mutableMap.put(CaptchaType.NUMERIC, 5L);

            CaptchaMetrics.MetricsSnapshot snapshot = new CaptchaMetrics.MetricsSnapshot(
                    5, 0, 0, 0, 0.0, Duration.ZERO, mutableMap, Duration.ZERO);

            mutableMap.put(CaptchaType.GIF, 10L);

            assertThat(snapshot.generationsByType())
                    .containsEntry(CaptchaType.NUMERIC, 5L)
                    .doesNotContainKey(CaptchaType.GIF);
        }
    }
}
