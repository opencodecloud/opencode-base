package cloud.opencode.base.lock;

import cloud.opencode.base.lock.exception.OpenLockTimeoutException;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.metrics.LockMetrics;
import cloud.opencode.base.lock.metrics.LockStats;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LocalLock test - 本地锁测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LocalLockTest {

    private LocalLock lock;

    @BeforeEach
    void setUp() {
        lock = new LocalLock();
    }

    @Nested
    @DisplayName("Basic Operations | 基本操作")
    class BasicOperationsTests {

        @Test
        @DisplayName("lock should acquire and release")
        void lock_shouldAcquireAndRelease() {
            assertThat(lock.isHeldByCurrentThread()).isFalse();

            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                assertThat(guard.token()).isNotNull();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("tryLock should return true when available")
        void tryLock_shouldReturnTrueWhenAvailable() {
            assertThat(lock.tryLock()).isTrue();
            try {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            } finally {
                lock.unlock();
            }
        }

        @Test
        @DisplayName("lock should be reentrant")
        void lock_shouldBeReentrant() {
            try (var guard1 = lock.lock()) {
                assertThat(lock.getHoldCount()).isEqualTo(1);

                try (var guard2 = lock.lock()) {
                    assertThat(lock.getHoldCount()).isEqualTo(2);
                }

                assertThat(lock.getHoldCount()).isEqualTo(1);
            }

            assertThat(lock.getHoldCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Execute Methods | 执行方法")
    class ExecuteMethodsTests {

        @Test
        @DisplayName("execute should run action with lock")
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
        @DisplayName("executeWithResult should return result")
        void executeWithResult_shouldReturnResult() {
            String result = lock.executeWithResult(() -> "hello");

            assertThat(result).isEqualTo("hello");
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("execute should release lock on exception")
        void execute_shouldReleaseLockOnException() {
            assertThatThrownBy(() -> lock.execute(() -> {
                throw new RuntimeException("test");
            })).isInstanceOf(RuntimeException.class);

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Timeout Tests | 超时测试")
    class TimeoutTests {

        @Test
        @DisplayName("lock with timeout should throw when timeout exceeded")
        void lockWithTimeout_shouldThrowWhenTimeoutExceeded() throws Exception {
            // Acquire lock in another thread
            Thread holder = new Thread(() -> {
                lock.lock();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();

            // Wait for lock to be acquired
            Thread.sleep(100);

            // Try to acquire with short timeout
            assertThatThrownBy(() -> lock.lock(Duration.ofMillis(100)))
                    .isInstanceOf(OpenLockTimeoutException.class);

            holder.interrupt();
            holder.join(1000);
        }

        @Test
        @DisplayName("tryLock with timeout should return false when timeout exceeded")
        void tryLockWithTimeout_shouldReturnFalseWhenTimeoutExceeded() throws Exception {
            // Acquire lock in another thread
            Thread holder = new Thread(() -> {
                lock.lock();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
            holder.start();

            // Wait for lock to be acquired
            Thread.sleep(100);

            // Try to acquire with short timeout
            assertThat(lock.tryLock(Duration.ofMillis(100))).isFalse();

            holder.interrupt();
            holder.join(1000);
        }
    }

    @Nested
    @DisplayName("Metrics Tests | 指标测试")
    class MetricsTests {

        @Test
        @DisplayName("metrics should track acquire and release")
        void metrics_shouldTrackAcquireAndRelease() {
            LocalLock lockWithMetrics = new LocalLock(OpenLock.configBuilder()
                    .enableMetrics(true)
                    .build());

            for (int i = 0; i < 10; i++) {
                lockWithMetrics.execute(() -> {});
            }

            LockMetrics metrics = lockWithMetrics.getMetrics().orElseThrow();
            LockStats stats = metrics.snapshot();

            assertThat(stats.acquireCount()).isEqualTo(10);
            assertThat(stats.releaseCount()).isEqualTo(10);
            assertThat(stats.timeoutCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Fair Lock Tests | 公平锁测试")
    class FairLockTests {

        @Test
        @DisplayName("fair lock should be fair")
        void fairLock_shouldBeFair() {
            LocalLock fairLock = new LocalLock(OpenLock.configBuilder()
                    .fair(true)
                    .build());

            assertThat(fairLock.isFair()).isTrue();
        }
    }
}
