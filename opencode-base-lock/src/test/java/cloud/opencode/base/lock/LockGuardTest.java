package cloud.opencode.base.lock;

import cloud.opencode.base.lock.local.LocalLock;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * LockGuard test - 锁守卫测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class LockGuardTest {

    private LocalLock lock;

    @BeforeEach
    void setUp() {
        lock = new LocalLock();
    }

    @Nested
    @DisplayName("Basic Operations | 基本操作")
    class BasicOperationsTests {

        @Test
        @DisplayName("lock() should return LockGuard with token")
        void lock_shouldReturnLockGuardWithToken() {
            LockGuard<Long> guard = lock.lock();

            assertThat(guard).isNotNull();
            assertThat(guard.lock()).isSameAs(lock);
            assertThat(guard.token()).isNotNull();
            assertThat(guard.token()).isGreaterThan(0);

            guard.close();
        }

        @Test
        @DisplayName("close() should release lock")
        void close_shouldReleaseLock() {
            LockGuard<Long> guard = lock.lock();

            assertThat(lock.isHeldByCurrentThread()).isTrue();

            guard.close();

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("try-with-resources should auto-release")
        void tryWithResources_shouldAutoRelease() {
            try (var guard = lock.lock()) {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
                assertThat(guard.token()).isNotNull();
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record Methods | 记录方法")
    class RecordMethodsTests {

        @Test
        @DisplayName("lock() accessor should return lock")
        void lockAccessor_shouldReturnLock() {
            LockGuard<Long> guard = lock.lock();

            assertThat(guard.lock()).isSameAs(lock);

            guard.close();
        }

        @Test
        @DisplayName("token() accessor should return token")
        void tokenAccessor_shouldReturnToken() {
            LockGuard<Long> guard = lock.lock();

            Long token = guard.token();
            assertThat(token).isNotNull();
            assertThat(token).isEqualTo(1L);

            guard.close();
        }

        @Test
        @DisplayName("tokens should be unique for each lock")
        void tokens_shouldBeUniqueForEachLock() {
            LockGuard<Long> guard1 = lock.lock();
            Long token1 = guard1.token();
            guard1.close();

            LockGuard<Long> guard2 = lock.lock();
            Long token2 = guard2.token();
            guard2.close();

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("toString() should return readable string")
        void toString_shouldReturnReadableString() {
            LockGuard<Long> guard = lock.lock();

            String str = guard.toString();

            assertThat(str).contains("LockGuard");
            assertThat(str).contains("token=");

            guard.close();
        }

        @Test
        @DisplayName("equals() should work correctly")
        void equals_shouldWorkCorrectly() {
            LockGuard<Long> guard1 = lock.lock();
            LockGuard<Long> guard2 = new LockGuard<>(lock, guard1.token());

            assertThat(guard1).isEqualTo(guard2);

            guard1.close();
        }

        @Test
        @DisplayName("hashCode() should be consistent")
        void hashCode_shouldBeConsistent() {
            LockGuard<Long> guard1 = lock.lock();
            LockGuard<Long> guard2 = new LockGuard<>(lock, guard1.token());

            assertThat(guard1.hashCode()).isEqualTo(guard2.hashCode());

            guard1.close();
        }
    }

    @Nested
    @DisplayName("Exception Handling | 异常处理")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("close() should release lock even after exception")
        void close_shouldReleaseLockEvenAfterException() {
            try {
                try (var guard = lock.lock()) {
                    assertThat(lock.isHeldByCurrentThread()).isTrue();
                    throw new RuntimeException("test exception");
                }
            } catch (RuntimeException e) {
                // Expected
            }

            assertThat(lock.isHeldByCurrentThread()).isFalse();
        }

        @Test
        @DisplayName("nested guards should work correctly")
        void nestedGuards_shouldWorkCorrectly() {
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
    @DisplayName("AutoCloseable Implementation | AutoCloseable 实现")
    class AutoCloseableTests {

        @Test
        @DisplayName("LockGuard should implement AutoCloseable")
        void lockGuard_shouldImplementAutoCloseable() {
            LockGuard<Long> guard = lock.lock();

            assertThat(guard).isInstanceOf(AutoCloseable.class);

            guard.close();
        }

        @Test
        @DisplayName("multiple close() calls should be safe")
        void multipleCloseCalls_shouldBeSafe() {
            LockGuard<Long> guard = lock.lock();

            guard.close();

            // Second close should not throw
            assertThatCode(() -> guard.close()).doesNotThrowAnyException();
        }
    }
}
