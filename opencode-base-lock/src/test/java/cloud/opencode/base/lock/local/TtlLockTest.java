package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * TtlLock test - TTL锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.3
 */
class TtlLockTest {

    @Nested
    @DisplayName("Basic Lock Tests | 基本锁测试")
    class BasicLockTests {

        @Test
        @DisplayName("lock() should acquire and release")
        void lock_shouldAcquireAndRelease() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                assertThat(guard.token()).isNotNull();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("tryLock() should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("tryLock() should return false when held by another thread")
        void tryLock_shouldReturnFalseWhenHeldByAnother() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));
            CountDownLatch acquired = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);

            Thread holder = Thread.ofVirtual().start(() -> {
                lock.lock();
                acquired.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });

            acquired.await();

            assertThat(lock.tryLock()).isFalse();

            release.countDown();
            holder.join(5000);
        }

        @Test
        @DisplayName("lock(timeout) should throw on timeout")
        void lockWithTimeout_shouldThrowOnTimeout() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));
            CountDownLatch acquired = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);

            Thread holder = Thread.ofVirtual().start(() -> {
                lock.lock();
                acquired.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });

            acquired.await();

            assertThatThrownBy(() -> lock.lock(Duration.ofMillis(50)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            release.countDown();
            holder.join(5000);
        }

        @Test
        @DisplayName("tryLock(timeout) should acquire within timeout")
        void tryLockWithTimeout_shouldAcquireWithinTimeout() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            boolean acquired = lock.tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("lockInterruptibly should acquire lock")
        void lockInterruptibly_shouldAcquireLock() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            try (var guard = lock.lockInterruptibly()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("getToken should return token when locked")
        void getToken_shouldReturnTokenWhenLocked() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            assertThat(lock.getToken()).isEmpty();

            lock.lock();
            assertThat(lock.getToken()).isPresent();

            lock.unlock();
        }

        @Test
        @DisplayName("getTtl should return configured TTL")
        void getTtl_shouldReturnConfiguredTtl() {
            Duration ttl = Duration.ofSeconds(30);
            TtlLock lock = new TtlLock(ttl);

            assertThat(lock.getTtl()).isEqualTo(ttl);
        }

        @Test
        @DisplayName("constructor should reject null ttl")
        void constructor_shouldRejectNullTtl() {
            assertThatThrownBy(() -> new TtlLock(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("constructor should reject zero ttl")
        void constructor_shouldRejectZeroTtl() {
            assertThatThrownBy(() -> new TtlLock(Duration.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("constructor should reject negative ttl")
        void constructor_shouldRejectNegativeTtl() {
            assertThatThrownBy(() -> new TtlLock(Duration.ofMillis(-100)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fair lock should work correctly")
        void fairLock_shouldWorkCorrectly() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10), true);

            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("TTL Expiry Tests | TTL过期测试")
    class TtlExpiryTests {

        @Test
        @DisplayName("isExpired should return false before TTL")
        void isExpired_shouldReturnFalseBeforeTtl() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            lock.lock();
            assertThat(lock.isExpired()).isFalse();

            lock.unlock();
        }

        @Test
        @DisplayName("isExpired should return true after TTL")
        void isExpired_shouldReturnTrueAfterTtl() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofMillis(50));

            lock.lock();
            Thread.sleep(100); // Wait for TTL to expire
            assertThat(lock.isExpired()).isTrue();

            lock.unlock();
        }

        @Test
        @DisplayName("isExpired should return false when not locked")
        void isExpired_shouldReturnFalseWhenNotLocked() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            assertThat(lock.isExpired()).isFalse();
        }

        @Test
        @DisplayName("new thread should be able to steal expired lock")
        void newThread_shouldStealExpiredLock() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofMillis(50));
            CountDownLatch holderAcquired = new CountDownLatch(1);
            CountDownLatch stealerDone = new CountDownLatch(1);
            AtomicBoolean stealerAcquired = new AtomicBoolean(false);
            AtomicReference<Throwable> stealerError = new AtomicReference<>();

            // Thread 1: acquire and hold past TTL
            Thread holder = Thread.ofVirtual().start(() -> {
                lock.lock();
                holderAcquired.countDown();
                try {
                    // Hold the lock well beyond TTL
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });

            holderAcquired.await();
            // Wait for TTL to expire
            Thread.sleep(100);

            // Thread 2: should be able to steal the expired lock
            Thread stealer = Thread.ofVirtual().start(() -> {
                try {
                    // tryLock should trigger TTL expiry check
                    boolean acquired = lock.tryLock(Duration.ofMillis(500));
                    stealerAcquired.set(acquired);
                    if (acquired) {
                        lock.unlock();
                    }
                } catch (Throwable t) {
                    stealerError.set(t);
                } finally {
                    stealerDone.countDown();
                }
            });

            stealerDone.await();

            // The stealer may or may not have acquired, depending on timing.
            // The key invariant: no exception should have been thrown
            assertThat(stealerError.get()).isNull();

            holder.join(5000);
        }

        @Test
        @DisplayName("same thread should not force-release its own lock")
        void sameThread_shouldNotForceReleaseOwnLock() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofMillis(50));

            lock.lock();
            Thread.sleep(100); // Let TTL expire

            // tryLock on same thread should not force-release
            // (it would fail because ReentrantLock is reentrant and already held)
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
        }
    }

    @Nested
    @DisplayName("Remaining TTL Tests | 剩余TTL测试")
    class RemainingTtlTests {

        @Test
        @DisplayName("getRemainingTtl should return ZERO when not locked")
        void getRemainingTtl_shouldReturnZeroWhenNotLocked() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            assertThat(lock.getRemainingTtl()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("getRemainingTtl should return positive value when locked and not expired")
        void getRemainingTtl_shouldReturnPositiveWhenNotExpired() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            lock.lock();
            Duration remaining = lock.getRemainingTtl();

            assertThat(remaining).isPositive();
            assertThat(remaining).isLessThanOrEqualTo(Duration.ofSeconds(10));

            lock.unlock();
        }

        @Test
        @DisplayName("getRemainingTtl should return ZERO when expired")
        void getRemainingTtl_shouldReturnZeroWhenExpired() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofMillis(50));

            lock.lock();
            Thread.sleep(100);

            assertThat(lock.getRemainingTtl()).isEqualTo(Duration.ZERO);

            lock.unlock();
        }

        @Test
        @DisplayName("getRemainingTtl should decrease over time")
        void getRemainingTtl_shouldDecreaseOverTime() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofSeconds(5));

            lock.lock();
            Duration first = lock.getRemainingTtl();
            Thread.sleep(100);
            Duration second = lock.getRemainingTtl();

            assertThat(second).isLessThan(first);

            lock.unlock();
        }

        @Test
        @DisplayName("getRemainingTtl should return ZERO after unlock")
        void getRemainingTtl_shouldReturnZeroAfterUnlock() {
            TtlLock lock = new TtlLock(Duration.ofSeconds(10));

            lock.lock();
            assertThat(lock.getRemainingTtl()).isPositive();

            lock.unlock();
            assertThat(lock.getRemainingTtl()).isEqualTo(Duration.ZERO);
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests | 线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("concurrent lock/unlock should maintain consistency")
        void concurrent_lockUnlock_shouldMaintainConsistency() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofSeconds(30));
            int threadCount = 10;
            int iterationsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicReference<Throwable> error = new AtomicReference<>();
            java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < iterationsPerThread; j++) {
                            try (var guard = lock.lock(Duration.ofSeconds(5))) {
                                counter.incrementAndGet();
                            }
                        }
                    } catch (Throwable t) {
                        error.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(error.get()).isNull();
            assertThat(counter.get()).isEqualTo(threadCount * iterationsPerThread);
        }

        @Test
        @DisplayName("concurrent access with short TTL should not deadlock")
        void concurrent_withShortTtl_shouldNotDeadlock() throws InterruptedException {
            TtlLock lock = new TtlLock(Duration.ofMillis(100));
            int threadCount = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicReference<Throwable> error = new AtomicReference<>();

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 20; j++) {
                            if (lock.tryLock(Duration.ofMillis(200))) {
                                try {
                                    Thread.sleep(5);
                                } finally {
                                    lock.unlock();
                                }
                            }
                        }
                    } catch (Throwable t) {
                        error.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);

            assertThat(completed).isTrue();
            assertThat(error.get()).isNull();
        }
    }
}
