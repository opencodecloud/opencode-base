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
 * LocalLock extended test - 本地锁扩展测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LocalLockExtendedTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should use default config")
        void defaultConstructor_shouldUseDefaultConfig() {
            LocalLock lock = new LocalLock();

            assertThat(lock.isFair()).isFalse();
        }

        @Test
        @DisplayName("constructor with config should apply config")
        void constructorWithConfig_shouldApplyConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .enableMetrics(true)
                    .build();

            LocalLock lock = new LocalLock(config);

            assertThat(lock.isFair()).isTrue();
            assertThat(lock.getMetrics()).isPresent();
        }

        @Test
        @DisplayName("constructor with metrics disabled should not create metrics")
        void constructorWithMetricsDisabled_shouldNotCreateMetrics() {
            LockConfig config = LockConfig.builder()
                    .enableMetrics(false)
                    .build();

            LocalLock lock = new LocalLock(config);

            assertThat(lock.getMetrics()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Lock Interruptibly Tests | 可中断锁测试")
    class LockInterruptiblyTests {

        @Test
        @DisplayName("lockInterruptibly() should acquire lock")
        void lockInterruptibly_shouldAcquireLock() throws Exception {
            LocalLock lock = new LocalLock();

            LockGuard<Long> guard = lock.lockInterruptibly();

            assertThat(lock.isHeldByCurrentThread()).isTrue();

            guard.close();
        }

        @Test
        @DisplayName("lockInterruptibly() should throw when interrupted")
        void lockInterruptibly_shouldThrowWhenInterrupted() throws Exception {
            LocalLock lock = new LocalLock();
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch started = new CountDownLatch(1);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            Thread holder = new Thread(() -> {
                try (var guard = lock.lock()) {
                    locked.countDown();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            holder.start();

            locked.await();

            Thread waiter = new Thread(() -> {
                try {
                    started.countDown();
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    interrupted.set(true);
                }
            });
            waiter.start();

            started.await();
            Thread.sleep(50);
            waiter.interrupt();
            waiter.join(1000);

            assertThat(interrupted.get()).isTrue();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("Queue Methods Tests | 队列方法测试")
    class QueueMethodsTests {

        @Test
        @DisplayName("hasQueuedThreads() should return false initially")
        void hasQueuedThreads_shouldReturnFalseInitially() {
            LocalLock lock = new LocalLock();

            assertThat(lock.hasQueuedThreads()).isFalse();
        }

        @Test
        @DisplayName("hasQueuedThreads() should return true when threads waiting")
        void hasQueuedThreads_shouldReturnTrueWhenThreadsWaiting() throws Exception {
            LocalLock lock = new LocalLock();
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch waiting = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                try (var guard = lock.lock()) {
                    locked.countDown();
                    waiting.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            holder.start();

            locked.await();

            Thread waiter = new Thread(() -> {
                try (var guard = lock.lock()) {
                    // Got the lock
                }
            });
            waiter.start();

            Thread.sleep(100); // Wait for waiter to queue

            assertThat(lock.hasQueuedThreads()).isTrue();

            waiting.countDown();
            holder.join(1000);
            waiter.join(1000);
        }

        @Test
        @DisplayName("getQueueLength() should return 0 initially")
        void getQueueLength_shouldReturnZeroInitially() {
            LocalLock lock = new LocalLock();

            assertThat(lock.getQueueLength()).isEqualTo(0);
        }

        @Test
        @DisplayName("getQueueLength() should return count of waiting threads")
        void getQueueLength_shouldReturnCountOfWaitingThreads() throws Exception {
            LocalLock lock = new LocalLock();
            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                try (var guard = lock.lock()) {
                    locked.countDown();
                    done.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            holder.start();

            locked.await();

            List<Thread> waiters = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Thread waiter = new Thread(() -> {
                    try (var guard = lock.lock()) {
                        // Got the lock
                    }
                });
                waiters.add(waiter);
                waiter.start();
            }

            Thread.sleep(200); // Wait for waiters to queue

            assertThat(lock.getQueueLength()).isEqualTo(3);

            done.countDown();
            holder.join(1000);
            for (Thread waiter : waiters) {
                waiter.join(1000);
            }
        }
    }

    @Nested
    @DisplayName("Token Tests | 令牌测试")
    class TokenTests {

        @Test
        @DisplayName("getToken() should return empty when not held")
        void getToken_shouldReturnEmptyWhenNotHeld() {
            LocalLock lock = new LocalLock();

            assertThat(lock.getToken()).isEmpty();
        }

        @Test
        @DisplayName("getToken() should return token when held")
        void getToken_shouldReturnTokenWhenHeld() {
            LocalLock lock = new LocalLock();

            try (var guard = lock.lock()) {
                Optional<Long> token = lock.getToken();
                assertThat(token).isPresent();
                assertThat(token.get()).isEqualTo(guard.token());
            }
        }

        @Test
        @DisplayName("tokens should be sequential")
        void tokens_shouldBeSequential() {
            LocalLock lock = new LocalLock();

            Long token1, token2, token3;

            try (var guard = lock.lock()) {
                token1 = guard.token();
            }

            try (var guard = lock.lock()) {
                token2 = guard.token();
            }

            try (var guard = lock.lock()) {
                token3 = guard.token();
            }

            assertThat(token2).isEqualTo(token1 + 1);
            assertThat(token3).isEqualTo(token2 + 1);
        }
    }

    @Nested
    @DisplayName("Metrics Tests | 指标测试")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics() should return metrics when enabled")
        void getMetrics_shouldReturnMetricsWhenEnabled() {
            LocalLock lock = new LocalLock(LockConfig.builder()
                    .enableMetrics(true)
                    .build());

            assertThat(lock.getMetrics()).isPresent();
        }

        @Test
        @DisplayName("metrics should track operations")
        void metrics_shouldTrackOperations() {
            LocalLock lock = new LocalLock(LockConfig.builder()
                    .enableMetrics(true)
                    .build());

            for (int i = 0; i < 5; i++) {
                lock.execute(() -> {});
            }

            LockMetrics metrics = lock.getMetrics().orElseThrow();
            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isEqualTo(5);
            assertThat(stats.releaseCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("metrics should track contention")
        void metrics_shouldTrackContention() throws Exception {
            LocalLock lock = new LocalLock(LockConfig.builder()
                    .enableMetrics(true)
                    .build());

            CountDownLatch locked = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                try (var guard = lock.lock()) {
                    locked.countDown();
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            holder.start();

            locked.await();

            Thread waiter = new Thread(() -> {
                try (var guard = lock.lock()) {
                    // Got the lock
                }
            });
            waiter.start();

            Thread.sleep(100);
            release.countDown();

            holder.join(1000);
            waiter.join(1000);

            LockMetrics metrics = lock.getMetrics().orElseThrow();
            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isEqualTo(2);
            assertThat(stats.contentionCount()).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Close Tests | 关闭测试")
    class CloseTests {

        @Test
        @DisplayName("close() should release lock if held")
        void close_shouldReleaseLockIfHeld() {
            LocalLock lock = new LocalLock();

            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.close();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("close() should be safe when not held")
        void close_shouldBeSafeWhenNotHeld() {
            LocalLock lock = new LocalLock();

            assertThatCode(() -> lock.close()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Timeout with Default Config Tests | 默认配置超时测试")
    class TimeoutWithDefaultConfigTests {

        @Test
        @DisplayName("lock() should use default timeout from config")
        void lock_shouldUseDefaultTimeoutFromConfig() throws Exception {
            LocalLock lock = new LocalLock(LockConfig.builder()
                    .timeout(Duration.ofMillis(100))
                    .build());

            CountDownLatch locked = new CountDownLatch(1);

            Thread holder = new Thread(() -> {
                try (var guard = lock.lock()) {
                    locked.countDown();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            holder.start();

            locked.await();

            assertThatThrownBy(() -> lock.lock())
                    .isInstanceOf(OpenLockTimeoutException.class);

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("Execute with Timeout Tests | 带超时执行测试")
    class ExecuteWithTimeoutTests {

        @Test
        @DisplayName("execute(action, timeout) should run action")
        void executeWithTimeout_shouldRunAction() {
            LocalLock lock = new LocalLock();
            AtomicBoolean executed = new AtomicBoolean(false);

            lock.execute(() -> executed.set(true), Duration.ofSeconds(5));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("executeWithResult(supplier, timeout) should return result")
        void executeWithResultWithTimeout_shouldReturnResult() {
            LocalLock lock = new LocalLock();

            String result = lock.executeWithResult(() -> "test", Duration.ofSeconds(5));

            assertThat(result).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("Heavy Concurrency Tests | 高并发测试")
    class HeavyConcurrencyTests {

        @Test
        @DisplayName("should handle many concurrent operations")
        void shouldHandleManyConcurrentOperations() throws Exception {
            LocalLock lock = new LocalLock();
            AtomicInteger counter = new AtomicInteger(0);

            int threads = 50;
            int operations = 100;

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
    }
}
