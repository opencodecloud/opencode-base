package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockAcquireException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import cloud.opencode.base.lock.metrics.LockMetrics;
import cloud.opencode.base.lock.metrics.LockStats;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalLock full test - 本地锁完整测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LocalLockFullTest {

    private LocalLock lock;

    @BeforeEach
    void setUp() {
        lock = new LocalLock();
    }

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should create non-fair lock")
        void defaultConstructor_shouldCreateNonFairLock() {
            LocalLock defaultLock = new LocalLock();

            assertThat(defaultLock.isFair()).isFalse();
        }

        @Test
        @DisplayName("constructor with config should apply fairness")
        void constructorWithConfig_shouldApplyFairness() {
            LocalLock fairLock = new LocalLock(LockConfig.builder().fair(true).build());

            assertThat(fairLock.isFair()).isTrue();
        }

        @Test
        @DisplayName("constructor with metrics enabled should create metrics")
        void constructorWithMetricsEnabled_shouldCreateMetrics() {
            LocalLock metricsLock = new LocalLock(LockConfig.builder().enableMetrics(true).build());

            assertThat(metricsLock.getMetrics()).isPresent();
        }

        @Test
        @DisplayName("constructor with metrics disabled should not create metrics")
        void constructorWithMetricsDisabled_shouldNotCreateMetrics() {
            LocalLock noMetricsLock = new LocalLock(LockConfig.builder().enableMetrics(false).build());

            assertThat(noMetricsLock.getMetrics()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Lock Operations | 锁操作")
    class LockOperationsTests {

        @Test
        @DisplayName("lock() should return guard with token")
        void lock_shouldReturnGuardWithToken() {
            LockGuard<Long> guard = lock.lock();

            assertThat(guard).isNotNull();
            assertThat(guard.token()).isNotNull();
            assertThat(guard.token()).isGreaterThan(0);
            assertThat(guard.lock()).isSameAs(lock);

            guard.close();
        }

        @Test
        @DisplayName("lock(timeout) should acquire within timeout")
        void lockWithTimeout_shouldAcquireWithinTimeout() {
            LockGuard<Long> guard = lock.lock(Duration.ofSeconds(5));

            assertThat(lock.isHeldByCurrentThread()).isTrue();

            guard.close();
        }

        @Test
        @DisplayName("lock(timeout) should throw on timeout")
        void lockWithTimeout_shouldThrowOnTimeout() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            assertThatThrownBy(() -> lock.lock(Duration.ofMillis(50)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("TryLock Operations | 尝试锁操作")
    class TryLockOperationsTests {

        @Test
        @DisplayName("tryLock() should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            boolean result = lock.tryLock();

            assertThat(result).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
        }

        @Test
        @DisplayName("tryLock() should return false when held by another thread")
        void tryLock_shouldReturnFalseWhenHeldByAnotherThread() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    done.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            assertThat(lock.tryLock()).isFalse();

            done.countDown();
            holder.join();
        }

        @Test
        @DisplayName("tryLock(timeout) should wait and acquire")
        void tryLockWithTimeout_shouldWaitAndAcquire() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            boolean acquired = lock.tryLock(Duration.ofSeconds(2));

            assertThat(acquired).isTrue();
            lock.unlock();

            holder.join();
        }

        @Test
        @DisplayName("tryLock(timeout) should return false on timeout")
        void tryLockWithTimeout_shouldReturnFalseOnTimeout() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            boolean acquired = lock.tryLock(Duration.ofMillis(50));

            assertThat(acquired).isFalse();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("LockInterruptibly Operations | 可中断锁操作")
    class LockInterruptiblyOperationsTests {

        @Test
        @DisplayName("lockInterruptibly() should acquire lock")
        void lockInterruptibly_shouldAcquireLock() throws Exception {
            LockGuard<Long> guard = lock.lockInterruptibly();

            assertThat(lock.isHeldByCurrentThread()).isTrue();

            guard.close();
        }

        @Test
        @DisplayName("lockInterruptibly() should throw on interrupt")
        void lockInterruptibly_shouldThrowOnInterrupt() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch waiting = new CountDownLatch(1);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            Thread waiter = new Thread(() -> {
                waiting.countDown();
                try {
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    interrupted.set(true);
                }
            });
            waiter.start();
            waiting.await();

            Thread.sleep(50);
            waiter.interrupt();
            waiter.join(1000);

            assertThat(interrupted.get()).isTrue();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("Unlock Operations | 解锁操作")
    class UnlockOperationsTests {

        @Test
        @DisplayName("unlock() should release lock")
        void unlock_shouldReleaseLock() {
            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("unlock() should be safe when not held")
        void unlock_shouldBeSafeWhenNotHeld() {
            // LocalLock only unlocks if held by current thread
            assertThatCode(() -> lock.unlock()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("unlock() should decrement hold count")
        void unlock_shouldDecrementHoldCount() {
            lock.lock();
            lock.lock();
            assertThat(lock.getHoldCount()).isEqualTo(2);

            lock.unlock();
            assertThat(lock.getHoldCount()).isEqualTo(1);

            lock.unlock();
            assertThat(lock.getHoldCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Token Operations | 令牌操作")
    class TokenOperationsTests {

        @Test
        @DisplayName("getToken() should return token when held")
        void getToken_shouldReturnTokenWhenHeld() {
            LockGuard<Long> guard = lock.lock();

            Optional<Long> token = lock.getToken();

            assertThat(token).isPresent();
            assertThat(token.get()).isEqualTo(guard.token());

            guard.close();
        }

        @Test
        @DisplayName("getToken() should return empty when not held")
        void getToken_shouldReturnEmptyWhenNotHeld() {
            assertThat(lock.getToken()).isEmpty();
        }

        @Test
        @DisplayName("tokens should increment")
        void tokens_shouldIncrement() {
            LockGuard<Long> guard1 = lock.lock();
            Long token1 = guard1.token();
            guard1.close();

            LockGuard<Long> guard2 = lock.lock();
            Long token2 = guard2.token();
            guard2.close();

            assertThat(token2).isGreaterThan(token1);
        }
    }

    @Nested
    @DisplayName("Queue Information | 队列信息")
    class QueueInformationTests {

        @Test
        @DisplayName("hasQueuedThreads() should return false initially")
        void hasQueuedThreads_shouldReturnFalseInitially() {
            assertThat(lock.hasQueuedThreads()).isFalse();
        }

        @Test
        @DisplayName("getQueueLength() should return 0 initially")
        void getQueueLength_shouldReturnZeroInitially() {
            assertThat(lock.getQueueLength()).isEqualTo(0);
        }

        @Test
        @DisplayName("hasQueuedThreads() should return true when threads waiting")
        void hasQueuedThreads_shouldReturnTrueWhenThreadsWaiting() throws Exception {
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch waiting = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                lock.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();
            locked.await();

            Thread waiter = new Thread(() -> {
                waiting.countDown();
                lock.lock();
                lock.unlock();
            });
            waiter.start();
            waiting.await();

            Thread.sleep(50);

            assertThat(lock.hasQueuedThreads()).isTrue();
            assertThat(lock.getQueueLength()).isGreaterThan(0);

            holder.interrupt();
            holder.join(1000);
            waiter.join(1000);
        }
    }

    @Nested
    @DisplayName("Metrics Tests | 指标测试")
    class MetricsTests {

        @Test
        @DisplayName("metrics should record acquires and releases")
        void metrics_shouldRecordAcquiresAndReleases() {
            LocalLock metricsLock = new LocalLock(LockConfig.builder().enableMetrics(true).build());

            for (int i = 0; i < 5; i++) {
                metricsLock.execute(() -> {});
            }

            LockMetrics metrics = metricsLock.getMetrics().orElseThrow();
            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isEqualTo(5);
            assertThat(stats.releaseCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("metrics should record timeouts")
        void metrics_shouldRecordTimeouts() throws Exception {
            LocalLock metricsLock = new LocalLock(LockConfig.builder().enableMetrics(true).build());
            CountDownLatch locked = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                metricsLock.lock();
                locked.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    metricsLock.unlock();
                }
            });
            holder.start();
            locked.await();

            try {
                metricsLock.lock(Duration.ofMillis(10));
            } catch (OpenLockTimeoutException e) {
                // Expected
            }

            LockMetrics metrics = metricsLock.getMetrics().orElseThrow();
            LockStats stats = metrics.snapshot();

            assertThat(stats.timeoutCount()).isEqualTo(1);

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("Close/AutoCloseable Tests | 关闭/自动关闭测试")
    class CloseTests {

        @Test
        @DisplayName("close() should release lock if held")
        void close_shouldReleaseLockIfHeld() {
            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.close();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("close() should be safe when not held")
        void close_shouldBeSafeWhenNotHeld() {
            assertThatCode(() -> lock.close()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests | 并发访问测试")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("should handle high contention")
        void shouldHandleHighContention() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            int threads = 20;
            int operations = 500;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < operations; j++) {
                        lock.execute(counter::incrementAndGet);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(counter.get()).isEqualTo(threads * operations);
        }

        @Test
        @DisplayName("should provide mutual exclusion")
        void shouldProvideMutualExclusion() throws Exception {
            AtomicInteger concurrent = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            AtomicBoolean failed = new AtomicBoolean(false);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < 100; j++) {
                        lock.execute(() -> {
                            int c = concurrent.incrementAndGet();
                            maxConcurrent.updateAndGet(m -> Math.max(m, c));
                            if (c > 1) failed.set(true);
                            concurrent.decrementAndGet();
                        });
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(failed.get()).isFalse();
            assertThat(maxConcurrent.get()).isEqualTo(1);
        }
    }
}
