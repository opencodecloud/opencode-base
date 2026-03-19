package cloud.opencode.base.lock.local;

import cloud.opencode.base.lock.LockConfig;
import cloud.opencode.base.lock.LockGuard;
import cloud.opencode.base.lock.exception.OpenLockException;
import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * SpinLock test - 自旋锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class SpinLockTest {

    private SpinLock lock;

    @BeforeEach
    void setUp() {
        lock = new SpinLock();
    }

    @Nested
    @DisplayName("Basic Operations | 基本操作")
    class BasicOperationsTests {

        @Test
        @DisplayName("lock() should acquire lock")
        void lock_shouldAcquireLock() {
            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                assertThat(guard.token()).isNotNull();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("tryLock() should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
        }

        @Test
        @DisplayName("tryLock() should return false when not available")
        void tryLock_shouldReturnFalseWhenNotAvailable() throws Exception {
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
        @DisplayName("unlock() should release lock")
        void unlock_shouldReleaseLock() {
            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("unlock() should throw when not held")
        void unlock_shouldThrowWhenNotHeld() {
            assertThatThrownBy(() -> lock.unlock())
                    .isInstanceOf(OpenLockException.class)
                    .hasMessageContaining("not held");
        }
    }

    @Nested
    @DisplayName("Reentrant Tests | 可重入测试")
    class ReentrantTests {

        @Test
        @DisplayName("lock should be reentrant by default")
        void lock_shouldBeReentrantByDefault() {
            try (var guard1 = lock.lock()) {
                assertThat(lock.getHoldCount()).isEqualTo(1);

                try (var guard2 = lock.lock()) {
                    assertThat(lock.getHoldCount()).isEqualTo(2);
                }

                assertThat(lock.getHoldCount()).isEqualTo(1);
            }

            assertThat(lock.getHoldCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("tryLock() should increment hold count when reentrant")
        void tryLock_shouldIncrementHoldCountWhenReentrant() {
            lock.tryLock();
            assertThat(lock.getHoldCount()).isEqualTo(1);

            lock.tryLock();
            assertThat(lock.getHoldCount()).isEqualTo(2);

            lock.unlock();
            assertThat(lock.getHoldCount()).isEqualTo(1);

            lock.unlock();
            assertThat(lock.getHoldCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("non-reentrant lock should throw on reentry")
        void nonReentrantLock_shouldThrowOnReentry() {
            SpinLock nonReentrantLock = new SpinLock(
                    LockConfig.builder().reentrant(false).build()
            );

            try (var guard = nonReentrantLock.lock()) {
                assertThatThrownBy(() -> nonReentrantLock.lock())
                        .isInstanceOf(OpenLockException.class)
                        .hasMessageContaining("not reentrant");
            }
        }
    }

    @Nested
    @DisplayName("Timeout Tests | 超时测试")
    class TimeoutTests {

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

            assertThatThrownBy(() -> lock.lock(Duration.ofMillis(100)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            holder.interrupt();
            holder.join(1000);
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

            assertThat(lock.tryLock(Duration.ofMillis(100))).isFalse();

            holder.interrupt();
            holder.join(1000);
        }

        @Test
        @DisplayName("tryLock(timeout) should return true when acquired")
        void tryLockWithTimeout_shouldReturnTrueWhenAcquired() {
            boolean acquired = lock.tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
        }
    }

    @Nested
    @DisplayName("Interruptible Tests | 可中断测试")
    class InterruptibleTests {

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
                try {
                    lock.lockInterruptibly();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            waiter.start();

            Thread.sleep(50);
            waiter.interrupt();
            waiter.join(1000);

            assertThat(waiter.isInterrupted()).isTrue();

            holder.interrupt();
            holder.join(1000);
        }

        @Test
        @DisplayName("lockInterruptibly() should be reentrant")
        void lockInterruptibly_shouldBeReentrant() throws Exception {
            LockGuard<Long> guard1 = lock.lockInterruptibly();
            assertThat(lock.getHoldCount()).isEqualTo(1);

            LockGuard<Long> guard2 = lock.lockInterruptibly();
            assertThat(lock.getHoldCount()).isEqualTo(2);

            guard2.close();
            assertThat(lock.getHoldCount()).isEqualTo(1);

            guard1.close();
            assertThat(lock.getHoldCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Token Tests | 令牌测试")
    class TokenTests {

        @Test
        @DisplayName("getToken() should return token when held")
        void getToken_shouldReturnTokenWhenHeld() {
            try (var guard = lock.lock()) {
                assertThat(lock.getToken()).isPresent();
                assertThat(lock.getToken().get()).isEqualTo(guard.token());
            }
        }

        @Test
        @DisplayName("getToken() should return empty when not held")
        void getToken_shouldReturnEmptyWhenNotHeld() {
            assertThat(lock.getToken()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Concurrency Tests | 并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("should be thread-safe")
        void shouldBeThreadSafe() throws Exception {
            AtomicInteger counter = new AtomicInteger(0);
            int threads = 10;
            int iterations = 100;

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < iterations; j++) {
                        lock.execute(counter::incrementAndGet);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();

            assertThat(counter.get()).isEqualTo(threads * iterations);
        }

        @Test
        @DisplayName("should provide mutual exclusion")
        void shouldProvideMutualExclusion() throws Exception {
            AtomicInteger concurrentAccess = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            AtomicBoolean failed = new AtomicBoolean(false);

            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    for (int j = 0; j < 50; j++) {
                        lock.execute(() -> {
                            int current = concurrentAccess.incrementAndGet();
                            maxConcurrent.updateAndGet(max -> Math.max(max, current));
                            if (current > 1) {
                                failed.set(true);
                            }
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            concurrentAccess.decrementAndGet();
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

    @Nested
    @DisplayName("Execute Methods | 执行方法")
    class ExecuteMethodsTests {

        @Test
        @DisplayName("execute() should run action with lock")
        void execute_shouldRunActionWithLock() {
            AtomicBoolean executed = new AtomicBoolean(false);

            lock.execute(() -> {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                executed.set(true);
            });

            assertThat(executed.get()).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("executeWithResult() should return result")
        void executeWithResult_shouldReturnResult() {
            String result = lock.executeWithResult(() -> "test result");

            assertThat(result).isEqualTo("test result");
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("execute() should release lock on exception")
        void execute_shouldReleaseLockOnException() {
            assertThatThrownBy(() -> lock.execute(() -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Configuration Tests | 配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("should respect custom spin count")
        void shouldRespectCustomSpinCount() {
            SpinLock customLock = new SpinLock(
                    LockConfig.builder().spinCount(500).build()
            );

            try (var guard = customLock.lock()) {
                assertThat(customLock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("should use default timeout from config")
        void shouldUseDefaultTimeoutFromConfig() {
            SpinLock customLock = new SpinLock(
                    LockConfig.builder()
                            .timeout(Duration.ofSeconds(10))
                            .build()
            );

            try (var guard = customLock.lock()) {
                assertThat(customLock.isHeldByCurrentThread()).isTrue();
            }
        }
    }
}
