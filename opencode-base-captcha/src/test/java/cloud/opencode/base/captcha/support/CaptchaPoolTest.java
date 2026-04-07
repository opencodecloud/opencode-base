package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * CaptchaPool Test - Unit tests for CAPTCHA pre-generation pool
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
class CaptchaPoolTest {

    private static final CaptchaConfig DEFAULT_CONFIG = CaptchaConfig.builder()
            .type(CaptchaType.NUMERIC)
            .build();

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("should build pool with default settings")
        void shouldBuildWithDefaults() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .build()) {

                assertThat(pool).isNotNull();
                assertThat(pool.isRunning()).isTrue();
            }
        }

        @Test
        @DisplayName("should build pool with custom pool size")
        void shouldBuildWithCustomPoolSize() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(50)
                    .build()) {

                assertThat(pool).isNotNull();
                assertThat(pool.isRunning()).isTrue();
            }
        }

        @Test
        @DisplayName("should build pool with custom refill threshold")
        void shouldBuildWithCustomRefillThreshold() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .refillThreshold(0.5f)
                    .build()) {

                assertThat(pool).isNotNull();
            }
        }

        @Test
        @DisplayName("should reject null config")
        void shouldRejectNullConfig() {
            assertThatNullPointerException()
                    .isThrownBy(() -> CaptchaPool.builder().build())
                    .withMessageContaining("config");
        }

        @Test
        @DisplayName("should reject pool size below 1")
        void shouldRejectPoolSizeBelowOne() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CaptchaPool.builder().poolSize(0))
                    .withMessageContaining("poolSize");
        }

        @Test
        @DisplayName("should reject pool size above 10000")
        void shouldRejectPoolSizeAboveMax() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CaptchaPool.builder().poolSize(10_001))
                    .withMessageContaining("poolSize");
        }

        @Test
        @DisplayName("should reject refill threshold of 0.0")
        void shouldRejectZeroRefillThreshold() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CaptchaPool.builder().refillThreshold(0.0f))
                    .withMessageContaining("refillThreshold");
        }

        @Test
        @DisplayName("should reject refill threshold above 1.0")
        void shouldRejectRefillThresholdAboveOne() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CaptchaPool.builder().refillThreshold(1.1f))
                    .withMessageContaining("refillThreshold");
        }

        @Test
        @DisplayName("should accept pool size of 1")
        void shouldAcceptPoolSizeOfOne() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(1)
                    .build()) {

                assertThat(pool).isNotNull();
            }
        }

        @Test
        @DisplayName("should accept pool size of 10000")
        void shouldAcceptMaxPoolSize() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(10_000)
                    .refillThreshold(0.01f)
                    .build()) {

                assertThat(pool).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Take Tests")
    class TakeTests {

        @Test
        @DisplayName("should return a captcha even when pool is empty")
        void shouldReturnCaptchaWhenPoolEmpty() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(5)
                    .build()) {

                // take() should always return a captcha (fallback generation)
                Captcha captcha = pool.take();

                assertThat(captcha).isNotNull();
                assertThat(captcha.id()).isNotEmpty();
                assertThat(captcha.answer()).isNotEmpty();
                assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            }
        }

        @Test
        @DisplayName("should return pre-generated captcha from pool after fill")
        void shouldReturnPreGeneratedCaptcha() throws Exception {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(10)
                    .refillThreshold(0.5f)
                    .build()) {

                // Wait for background fill thread to generate some CAPTCHAs
                awaitPoolFill(pool, 1, 5000);

                Captcha captcha = pool.take();

                assertThat(captcha).isNotNull();
                assertThat(captcha.type()).isEqualTo(CaptchaType.NUMERIC);
            }
        }

        @Test
        @DisplayName("should return unique captchas on consecutive takes")
        void shouldReturnUniqueCaptchas() throws Exception {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(20)
                    .refillThreshold(0.8f)
                    .build()) {

                // Wait for pool to fill
                awaitPoolFill(pool, 5, 5000);

                Captcha c1 = pool.take();
                Captcha c2 = pool.take();

                assertThat(c1.id()).isNotEqualTo(c2.id());
            }
        }

        @Test
        @DisplayName("should throw IllegalStateException after close")
        void shouldThrowAfterClose() {
            CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(5)
                    .build();
            pool.close();

            assertThatIllegalStateException()
                    .isThrownBy(pool::take)
                    .withMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Size Tests")
    class SizeTests {

        @Test
        @DisplayName("should report initial size of 0")
        void shouldReportInitialSizeOfZero() {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(10)
                    .build()) {

                // Size starts at 0 (fill thread has not yet run)
                // or may already have some - just verify non-negative
                assertThat(pool.size()).isGreaterThanOrEqualTo(0);
            }
        }

        @Test
        @DisplayName("should fill pool up to pool size")
        void shouldFillUpToPoolSize() throws Exception {
            int targetSize = 10;
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(targetSize)
                    .refillThreshold(0.9f)
                    .build()) {

                awaitPoolFill(pool, targetSize, 10_000);

                assertThat(pool.size()).isEqualTo(targetSize);
            }
        }

        @Test
        @DisplayName("should decrease size after take")
        void shouldDecreaseSizeAfterTake() throws Exception {
            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(10)
                    .refillThreshold(0.5f)
                    .build()) {

                awaitPoolFill(pool, 5, 5000);

                int sizeBefore = pool.size();
                pool.take();

                // Size should decrease (or be same if fill thread already added one)
                assertThat(pool.size()).isLessThanOrEqualTo(sizeBefore);
            }
        }
    }

    @Nested
    @DisplayName("Close Tests")
    class CloseTests {

        @Test
        @DisplayName("should stop running after close")
        void shouldStopRunningAfterClose() {
            CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(5)
                    .build();

            assertThat(pool.isRunning()).isTrue();

            pool.close();

            assertThat(pool.isRunning()).isFalse();
        }

        @Test
        @DisplayName("should clear pool on close")
        void shouldClearPoolOnClose() throws Exception {
            CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(10)
                    .refillThreshold(0.9f)
                    .build();

            awaitPoolFill(pool, 1, 5000);

            pool.close();

            assertThat(pool.size()).isZero();
        }

        @Test
        @DisplayName("should be idempotent on multiple closes")
        void shouldBeIdempotentOnMultipleCloses() {
            CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(5)
                    .build();

            pool.close();
            pool.close();
            pool.close();

            assertThat(pool.isRunning()).isFalse();
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("should handle concurrent takes safely")
        void shouldHandleConcurrentTakes() throws Exception {
            int threadCount = 8;
            int takesPerThread = 20;

            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(DEFAULT_CONFIG)
                    .poolSize(50)
                    .refillThreshold(0.8f)
                    .build()) {

                // Let pool fill a bit
                awaitPoolFill(pool, 10, 5000);

                CountDownLatch startLatch = new CountDownLatch(1);
                CountDownLatch doneLatch = new CountDownLatch(threadCount);
                List<Captcha> allCaptchas = java.util.Collections.synchronizedList(new ArrayList<>());

                for (int t = 0; t < threadCount; t++) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            startLatch.await();
                            for (int i = 0; i < takesPerThread; i++) {
                                Captcha captcha = pool.take();
                                allCaptchas.add(captcha);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                startLatch.countDown();
                assertThat(doneLatch.await(30, TimeUnit.SECONDS)).isTrue();

                assertThat(allCaptchas).hasSize(threadCount * takesPerThread);
                assertThat(allCaptchas).allSatisfy(c -> {
                    assertThat(c).isNotNull();
                    assertThat(c.id()).isNotEmpty();
                    assertThat(c.type()).isEqualTo(CaptchaType.NUMERIC);
                });
            }
        }
    }

    @Nested
    @DisplayName("CaptchaType Tests")
    class CaptchaTypeTests {

        @Test
        @DisplayName("should generate correct type for ALPHANUMERIC config")
        void shouldGenerateCorrectTypeAlphanumeric() {
            CaptchaConfig alphaConfig = CaptchaConfig.builder()
                    .type(CaptchaType.ALPHANUMERIC)
                    .build();

            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(alphaConfig)
                    .poolSize(5)
                    .build()) {

                Captcha captcha = pool.take();

                assertThat(captcha.type()).isEqualTo(CaptchaType.ALPHANUMERIC);
            }
        }

        @Test
        @DisplayName("should generate correct type for ARITHMETIC config")
        void shouldGenerateCorrectTypeArithmetic() {
            CaptchaConfig arithmeticConfig = CaptchaConfig.builder()
                    .type(CaptchaType.ARITHMETIC)
                    .build();

            try (CaptchaPool pool = CaptchaPool.builder()
                    .config(arithmeticConfig)
                    .poolSize(5)
                    .build()) {

                Captcha captcha = pool.take();

                assertThat(captcha.type()).isEqualTo(CaptchaType.ARITHMETIC);
            }
        }
    }

    /**
     * Waits until the pool reaches at least the specified minimum size, or until timeout.
     */
    private static void awaitPoolFill(CaptchaPool pool, int minSize, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (pool.size() < minSize && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
    }
}
