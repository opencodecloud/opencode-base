package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * RetryLock test - 重试锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
class RetryLockTest {

    @Nested
    @DisplayName("Retry Success Tests | 重试成功测试")
    class RetrySuccessTests {

        @Test
        @DisplayName("lock() should succeed on first try when available")
        void lock_shouldSucceedOnFirstTry() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            try (var guard = retryLock.lock()) {
                assertThat(retryLock.isHeldByCurrentThread()).isTrue();
                assertThat(guard.token()).isNotNull();
            }

            assertThat(retryLock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("lock() should succeed after retries when lock becomes available")
        void lock_shouldSucceedAfterRetries() {
            AtomicInteger attempts = new AtomicInteger(0);
            int failBeforeSuccess = 2;

            Lock<Long> mockDelegate = new FailThenSucceedLock(failBeforeSuccess, attempts);
            RetryLock<Long> retryLock = RetryLock.builder(mockDelegate)
                    .maxRetries(3)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            try (var guard = retryLock.lock()) {
                assertThat(guard.token()).isNotNull();
            }

            // 1 initial + 2 retries = 3 total attempts
            assertThat(attempts.get()).isEqualTo(failBeforeSuccess + 1);
        }

        @Test
        @DisplayName("tryLock(timeout) should succeed after retries")
        void tryLockWithTimeout_shouldSucceedAfterRetries() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> mockDelegate = new FailThenSucceedTryLock(1, attempts);
            RetryLock<Long> retryLock = RetryLock.builder(mockDelegate)
                    .maxRetries(3)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            boolean acquired = retryLock.tryLock(Duration.ofSeconds(2));

            assertThat(acquired).isTrue();
            assertThat(attempts.get()).isEqualTo(2);

            retryLock.unlock();
        }

        @Test
        @DisplayName("lock(timeout) should succeed after retries within total timeout")
        void lockWithTimeout_shouldSucceedAfterRetries() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> mockDelegate = new FailThenSucceedLock(1, attempts);
            RetryLock<Long> retryLock = RetryLock.builder(mockDelegate)
                    .maxRetries(3)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            try (var guard = retryLock.lock(Duration.ofSeconds(2))) {
                assertThat(guard.token()).isNotNull();
            }

            assertThat(attempts.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Retry Failure Tests | 重试失败测试")
    class RetryFailureTests {

        @Test
        @DisplayName("lock() should throw after all retries exhausted")
        void lock_shouldThrowAfterAllRetriesExhausted() {
            Lock<Long> alwaysFails = new AlwaysFailLock();
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(2)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            assertThatThrownBy(retryLock::lock)
                    .isInstanceOf(OpenLockTimeoutException.class)
                    .hasMessageContaining("3 attempts");
        }

        @Test
        @DisplayName("lock(timeout) should throw when total timeout exceeded")
        void lockWithTimeout_shouldThrowWhenTimeoutExceeded() {
            Lock<Long> alwaysFails = new AlwaysFailLock();
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(100)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            assertThatThrownBy(() -> retryLock.lock(Duration.ofMillis(100)))
                    .isInstanceOf(OpenLockTimeoutException.class);
        }

        @Test
        @DisplayName("tryLock(timeout) should return false when all retries fail")
        void tryLockWithTimeout_shouldReturnFalseWhenAllFail() {
            Lock<Long> alwaysFails = new AlwaysFailTryLock();
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(2)
                    .retryDelay(Duration.ofMillis(10))
                    .build();

            boolean acquired = retryLock.tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isFalse();
        }

        @Test
        @DisplayName("lock() with maxRetries=0 should not retry")
        void lock_withZeroRetries_shouldNotRetry() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> alwaysFails = new CountingAlwaysFailLock(attempts);
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(0)
                    .build();

            assertThatThrownBy(retryLock::lock)
                    .isInstanceOf(OpenLockTimeoutException.class);

            assertThat(attempts.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Backoff Tests | 退避测试")
    class BackoffTests {

        @Test
        @DisplayName("delays should increase exponentially")
        void delays_shouldIncreaseExponentially() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> alwaysFails = new CountingAlwaysFailLock(attempts);
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(3)
                    .retryDelay(Duration.ofMillis(50))
                    .backoffMultiplier(2.0)
                    .maxDelay(Duration.ofSeconds(10))
                    .build();

            long start = System.nanoTime();
            assertThatThrownBy(retryLock::lock)
                    .isInstanceOf(OpenLockTimeoutException.class);
            long elapsed = Duration.ofNanos(System.nanoTime() - start).toMillis();

            // Expected delays: 50 + 100 + 200 = 350ms minimum
            // Allow some tolerance for scheduling
            assertThat(elapsed).isGreaterThanOrEqualTo(300);
            assertThat(attempts.get()).isEqualTo(4); // 1 initial + 3 retries
        }

        @Test
        @DisplayName("delay should be capped at maxDelay")
        void delay_shouldBeCappedAtMaxDelay() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> alwaysFails = new CountingAlwaysFailLock(attempts);
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(4)
                    .retryDelay(Duration.ofMillis(50))
                    .backoffMultiplier(10.0)
                    .maxDelay(Duration.ofMillis(100))
                    .build();

            long start = System.nanoTime();
            assertThatThrownBy(retryLock::lock)
                    .isInstanceOf(OpenLockTimeoutException.class);
            long elapsed = Duration.ofNanos(System.nanoTime() - start).toMillis();

            // Delays: 50, 100(capped), 100(capped), 100(capped) = 350ms
            // With cap at 100ms, should not exceed ~500ms
            assertThat(elapsed).isLessThan(600);
        }

        @Test
        @DisplayName("backoff multiplier of 1.0 should produce constant delay")
        void backoffMultiplierOne_shouldProduceConstantDelay() {
            AtomicInteger attempts = new AtomicInteger(0);
            Lock<Long> alwaysFails = new CountingAlwaysFailLock(attempts);
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(2)
                    .retryDelay(Duration.ofMillis(50))
                    .backoffMultiplier(1.0)
                    .build();

            long start = System.nanoTime();
            assertThatThrownBy(retryLock::lock)
                    .isInstanceOf(OpenLockTimeoutException.class);
            long elapsed = Duration.ofNanos(System.nanoTime() - start).toMillis();

            // Delays: 50 + 50 = 100ms minimum
            assertThat(elapsed).isGreaterThanOrEqualTo(80);
            assertThat(elapsed).isLessThan(300);
        }
    }

    @Nested
    @DisplayName("Builder Tests | 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder should create lock with custom configuration")
        void builder_shouldCreateWithCustomConfig() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = RetryLock.builder(delegate)
                    .maxRetries(5)
                    .retryDelay(Duration.ofMillis(200))
                    .backoffMultiplier(1.5)
                    .maxDelay(Duration.ofSeconds(3))
                    .build();

            assertThat(retryLock.getMaxRetries()).isEqualTo(5);
            assertThat(retryLock.getRetryDelay()).isEqualTo(Duration.ofMillis(200));
            assertThat(retryLock.getBackoffMultiplier()).isEqualTo(1.5);
            assertThat(retryLock.getMaxDelay()).isEqualTo(Duration.ofSeconds(3));
            assertThat(retryLock.getDelegate()).isSameAs(delegate);
        }

        @Test
        @DisplayName("default constructor should use default values")
        void defaultConstructor_shouldUseDefaults() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            assertThat(retryLock.getMaxRetries()).isEqualTo(3);
            assertThat(retryLock.getRetryDelay()).isEqualTo(Duration.ofMillis(100));
            assertThat(retryLock.getBackoffMultiplier()).isEqualTo(2.0);
            assertThat(retryLock.getMaxDelay()).isEqualTo(Duration.ofSeconds(5));
        }

        @Test
        @DisplayName("constructor should reject null delegate")
        void constructor_shouldRejectNullDelegate() {
            assertThatThrownBy(() -> new RetryLock<>(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("delegate");
        }

        @Test
        @DisplayName("constructor should reject negative maxRetries")
        void constructor_shouldRejectNegativeMaxRetries() {
            LocalLock delegate = new LocalLock();
            assertThatThrownBy(() -> RetryLock.builder(delegate)
                    .maxRetries(-1)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxRetries");
        }

        @Test
        @DisplayName("constructor should reject backoffMultiplier less than 1.0")
        void constructor_shouldRejectSmallBackoffMultiplier() {
            LocalLock delegate = new LocalLock();
            assertThatThrownBy(() -> RetryLock.builder(delegate)
                    .backoffMultiplier(0.5)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("backoffMultiplier");
        }

        @Test
        @DisplayName("builder should reject null delegate")
        void builder_shouldRejectNullDelegate() {
            assertThatThrownBy(() -> RetryLock.builder(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Delegation Tests | 委托测试")
    class DelegationTests {

        @Test
        @DisplayName("unlock should delegate to underlying lock")
        void unlock_shouldDelegate() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            retryLock.lock();
            assertThat(delegate.isHeldByCurrentThread()).isTrue();

            retryLock.unlock();
            assertThat(delegate.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("isHeldByCurrentThread should delegate")
        void isHeldByCurrentThread_shouldDelegate() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            assertThat(retryLock.isHeldByCurrentThread()).isFalse();

            retryLock.lock();
            assertThat(retryLock.isHeldByCurrentThread()).isTrue();

            retryLock.unlock();
            assertThat(retryLock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("getToken should delegate")
        void getToken_shouldDelegate() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            assertThat(retryLock.getToken()).isEmpty();

            retryLock.lock();
            assertThat(retryLock.getToken()).isPresent();

            retryLock.unlock();
        }

        @Test
        @DisplayName("tryLock() should delegate without retry")
        void tryLock_shouldDelegateWithoutRetry() {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            boolean acquired = retryLock.tryLock();
            assertThat(acquired).isTrue();
            assertThat(delegate.isHeldByCurrentThread()).isTrue();

            retryLock.unlock();
        }

        @Test
        @DisplayName("lockInterruptibly should delegate with retry support")
        void lockInterruptibly_shouldDelegate() throws InterruptedException {
            LocalLock delegate = new LocalLock();
            RetryLock<Long> retryLock = new RetryLock<>(delegate);

            try (var guard = retryLock.lockInterruptibly()) {
                assertThat(retryLock.isHeldByCurrentThread()).isTrue();
            }

            assertThat(retryLock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Interrupt Tests | 中断测试")
    class InterruptTests {

        @Test
        @DisplayName("lock() should throw when sleep is interrupted during retry")
        void lock_shouldThrowWhenInterruptedDuringRetry() throws InterruptedException {
            Lock<Long> alwaysFails = new AlwaysFailLock();
            RetryLock<Long> retryLock = RetryLock.builder(alwaysFails)
                    .maxRetries(10)
                    .retryDelay(Duration.ofSeconds(5))
                    .build();

            CountDownLatch started = new CountDownLatch(1);
            Thread testThread = Thread.ofVirtual().start(() -> {
                started.countDown();
                assertThatThrownBy(retryLock::lock)
                        .isInstanceOf(OpenLockAcquireException.class)
                        .hasMessageContaining("interrupted");
            });

            started.await();
            Thread.sleep(50); // Let the thread enter retry sleep
            testThread.interrupt();
            testThread.join(5000);

            assertThat(testThread.isAlive()).isFalse();
        }
    }

    // --- Test helper Lock implementations ---

    /**
     * Lock that fails N times then succeeds (for lock() method)
     */
    private static class FailThenSucceedLock implements Lock<Long> {
        private final int failCount;
        private final AtomicInteger attempts;
        private final LocalLock realLock = new LocalLock();

        FailThenSucceedLock(int failCount, AtomicInteger attempts) {
            this.failCount = failCount;
            this.attempts = attempts;
        }

        @Override
        public LockGuard<Long> lock() {
            int attempt = attempts.incrementAndGet();
            if (attempt <= failCount) {
                throw new OpenLockTimeoutException("Simulated timeout attempt " + attempt);
            }
            return realLock.lock();
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            return lock();
        }

        @Override
        public boolean tryLock() { return realLock.tryLock(); }

        @Override
        public boolean tryLock(Duration timeout) { return realLock.tryLock(timeout); }

        @Override
        public LockGuard<Long> lockInterruptibly() throws InterruptedException {
            return realLock.lockInterruptibly();
        }

        @Override
        public void unlock() { realLock.unlock(); }

        @Override
        public boolean isHeldByCurrentThread() { return realLock.isHeldByCurrentThread(); }

        @Override
        public Optional<Long> getToken() { return realLock.getToken(); }
    }

    /**
     * Lock that fails N times then succeeds (for tryLock(timeout) method)
     */
    private static class FailThenSucceedTryLock implements Lock<Long> {
        private final int failCount;
        private final AtomicInteger attempts;
        private final LocalLock realLock = new LocalLock();

        FailThenSucceedTryLock(int failCount, AtomicInteger attempts) {
            this.failCount = failCount;
            this.attempts = attempts;
        }

        @Override
        public LockGuard<Long> lock() { return realLock.lock(); }

        @Override
        public LockGuard<Long> lock(Duration timeout) { return realLock.lock(timeout); }

        @Override
        public boolean tryLock() { return realLock.tryLock(); }

        @Override
        public boolean tryLock(Duration timeout) {
            int attempt = attempts.incrementAndGet();
            if (attempt <= failCount) {
                return false;
            }
            return realLock.tryLock();
        }

        @Override
        public LockGuard<Long> lockInterruptibly() throws InterruptedException {
            return realLock.lockInterruptibly();
        }

        @Override
        public void unlock() { realLock.unlock(); }

        @Override
        public boolean isHeldByCurrentThread() { return realLock.isHeldByCurrentThread(); }

        @Override
        public Optional<Long> getToken() { return realLock.getToken(); }
    }

    /**
     * Lock whose lock() always throws OpenLockTimeoutException
     */
    private static class AlwaysFailLock implements Lock<Long> {
        @Override
        public LockGuard<Long> lock() {
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            throw new OpenLockTimeoutException("Always fails", timeout);
        }

        @Override
        public boolean tryLock() { return false; }

        @Override
        public boolean tryLock(Duration timeout) { return false; }

        @Override
        public LockGuard<Long> lockInterruptibly() {
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public void unlock() { }

        @Override
        public boolean isHeldByCurrentThread() { return false; }

        @Override
        public Optional<Long> getToken() { return Optional.empty(); }
    }

    /**
     * Like AlwaysFailLock but counts attempts
     */
    private static class CountingAlwaysFailLock implements Lock<Long> {
        private final AtomicInteger attempts;

        CountingAlwaysFailLock(AtomicInteger attempts) {
            this.attempts = attempts;
        }

        @Override
        public LockGuard<Long> lock() {
            attempts.incrementAndGet();
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            attempts.incrementAndGet();
            throw new OpenLockTimeoutException("Always fails", timeout);
        }

        @Override
        public boolean tryLock() { return false; }

        @Override
        public boolean tryLock(Duration timeout) { return false; }

        @Override
        public LockGuard<Long> lockInterruptibly() {
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public void unlock() { }

        @Override
        public boolean isHeldByCurrentThread() { return false; }

        @Override
        public Optional<Long> getToken() { return Optional.empty(); }
    }

    /**
     * Lock whose tryLock(timeout) always returns false
     */
    private static class AlwaysFailTryLock implements Lock<Long> {
        @Override
        public LockGuard<Long> lock() {
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public LockGuard<Long> lock(Duration timeout) {
            throw new OpenLockTimeoutException("Always fails", timeout);
        }

        @Override
        public boolean tryLock() { return false; }

        @Override
        public boolean tryLock(Duration timeout) { return false; }

        @Override
        public LockGuard<Long> lockInterruptibly() {
            throw new OpenLockTimeoutException("Always fails");
        }

        @Override
        public void unlock() { }

        @Override
        public boolean isHeldByCurrentThread() { return false; }

        @Override
        public Optional<Long> getToken() { return Optional.empty(); }
    }
}
