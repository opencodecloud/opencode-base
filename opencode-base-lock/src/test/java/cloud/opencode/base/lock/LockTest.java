package cloud.opencode.base.lock;

import cloud.opencode.base.lock.local.LocalLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Lock Interface Tests
 * Lock 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
@DisplayName("Lock Interface Tests | Lock 接口测试")
class LockTest {

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("execute runs action with lock | execute 持有锁运行动作")
        void testExecute() {
            Lock<Long> lock = OpenLock.lock();
            AtomicBoolean executed = new AtomicBoolean(false);

            lock.execute(() -> executed.set(true));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("execute with timeout runs action | execute 带超时运行动作")
        void testExecuteWithTimeout() {
            Lock<Long> lock = OpenLock.lock();
            AtomicBoolean executed = new AtomicBoolean(false);

            lock.execute(() -> executed.set(true), Duration.ofSeconds(5));

            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("executeWithResult returns result | executeWithResult 返回结果")
        void testExecuteWithResult() {
            Lock<Long> lock = OpenLock.lock();

            String result = lock.executeWithResult(() -> "hello");

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("executeWithResult with timeout returns result | executeWithResult 带超时返回结果")
        void testExecuteWithResultTimeout() {
            Lock<Long> lock = OpenLock.lock();

            Integer result = lock.executeWithResult(() -> 42, Duration.ofSeconds(5));

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("close releases lock if held | close 如果持有则释放锁")
        void testClose() {
            Lock<Long> lock = OpenLock.lock();

            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.close();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Lock Operations Tests | 锁操作测试")
    class LockOperationsTests {

        @Test
        @DisplayName("lock returns guard | lock 返回守卫")
        void testLock() {
            Lock<Long> lock = OpenLock.lock();

            try (LockGuard<Long> guard = lock.lock()) {
                assertThat(guard).isNotNull();
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("lock with timeout returns guard | lock 带超时返回守卫")
        void testLockWithTimeout() {
            Lock<Long> lock = OpenLock.lock();

            try (LockGuard<Long> guard = lock.lock(Duration.ofSeconds(5))) {
                assertThat(guard).isNotNull();
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            }
        }

        @Test
        @DisplayName("tryLock returns true when available | tryLock 当可用时返回 true")
        void testTryLock() {
            Lock<Long> lock = OpenLock.lock();

            boolean acquired = lock.tryLock();

            assertThat(acquired).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("tryLock with timeout returns true when available | tryLock 带超时当可用时返回 true")
        void testTryLockWithTimeout() {
            Lock<Long> lock = OpenLock.lock();

            boolean acquired = lock.tryLock(Duration.ofSeconds(1));

            assertThat(acquired).isTrue();
            lock.unlock();
        }

        @Test
        @DisplayName("lockInterruptibly can be interrupted | lockInterruptibly 可以被中断")
        void testLockInterruptibly() throws InterruptedException {
            Lock<Long> lock = OpenLock.lock();

            LockGuard<Long> guard = lock.lockInterruptibly();
            assertThat(lock.isHeldByCurrentThread()).isTrue();
            guard.close();
        }

        @Test
        @DisplayName("unlock releases lock | unlock 释放锁")
        void testUnlock() {
            Lock<Long> lock = OpenLock.lock();

            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Tests | 令牌测试")
    class TokenTests {

        @Test
        @DisplayName("getToken returns empty when not locked | getToken 未锁定时返回空")
        void testGetTokenEmpty() {
            Lock<Long> lock = OpenLock.lock();

            Optional<Long> token = lock.getToken();

            assertThat(token).isEmpty();
        }

        @Test
        @DisplayName("getToken returns token when locked | getToken 锁定时返回令牌")
        void testGetTokenPresent() {
            Lock<Long> lock = OpenLock.lock();

            lock.lock();
            Optional<Long> token = lock.getToken();
            lock.unlock();

            assertThat(token).isPresent();
        }
    }

    @Nested
    @DisplayName("IsHeldByCurrentThread Tests | isHeldByCurrentThread 测试")
    class IsHeldByCurrentThreadTests {

        @Test
        @DisplayName("returns false when not held | 未持有时返回 false")
        void testNotHeld() {
            Lock<Long> lock = OpenLock.lock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("returns true when held | 持有时返回 true")
        void testHeld() {
            Lock<Long> lock = OpenLock.lock();

            lock.lock();
            assertThat(lock.isHeldByCurrentThread()).isTrue();
            lock.unlock();
        }
    }

    @Nested
    @DisplayName("Reentrant Tests | 可重入测试")
    class ReentrantTests {

        @Test
        @DisplayName("lock is reentrant | 锁是可重入的")
        void testReentrant() {
            Lock<Long> lock = OpenLock.lock();

            lock.lock();
            lock.lock(); // Reentrant acquire

            assertThat(lock.isHeldByCurrentThread()).isTrue();

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isTrue(); // Still held

            lock.unlock();
            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("OpenLock Factory Tests | OpenLock 工厂测试")
    class OpenLockFactoryTests {

        @Test
        @DisplayName("lock creates default lock | lock 创建默认锁")
        void testLockDefault() {
            Lock<Long> lock = OpenLock.lock();
            assertThat(lock).isNotNull();
            assertThat(lock).isInstanceOf(LocalLock.class);
        }

        @Test
        @DisplayName("lock with config creates configured lock | lock 带配置创建配置的锁")
        void testLockWithConfig() {
            LockConfig config = LockConfig.builder()
                    .fair(true)
                    .build();

            Lock<Long> lock = OpenLock.lock(config);
            assertThat(lock).isNotNull();
        }
    }
}
