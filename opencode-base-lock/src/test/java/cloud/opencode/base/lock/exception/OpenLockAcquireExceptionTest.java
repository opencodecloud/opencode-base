package cloud.opencode.base.lock.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLockAcquireException test - 锁获取异常测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class OpenLockAcquireExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessage_shouldSetMessage() {
            OpenLockAcquireException exception = new OpenLockAcquireException("acquire failed");

            assertThat(exception.getMessage()).contains("acquire failed");
            assertThat(exception.lockName()).isNull();
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCause_shouldSetBoth() {
            InterruptedException cause = new InterruptedException("interrupted");
            OpenLockAcquireException exception = new OpenLockAcquireException("acquire failed", cause);

            assertThat(exception.getMessage()).contains("acquire failed");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
        }

        @Test
        @DisplayName("constructor with lockName and cause should set all")
        void constructorWithLockNameAndCause_shouldSetAll() {
            RuntimeException cause = new RuntimeException("error");
            OpenLockAcquireException exception = new OpenLockAcquireException(
                    "acquire failed", "myLock", cause
            );

            assertThat(exception.getMessage()).contains("acquire failed");
            assertThat(exception.lockName()).isEqualTo("myLock");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests | 继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenLockException")
        void shouldExtendOpenLockException() {
            OpenLockAcquireException exception = new OpenLockAcquireException("test");

            assertThat(exception).isInstanceOf(OpenLockException.class);
        }

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            OpenLockAcquireException exception = new OpenLockAcquireException("test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("errorType should always be ACQUIRE")
        void errorType_shouldAlwaysBeAcquire() {
            OpenLockAcquireException ex1 = new OpenLockAcquireException("test");
            OpenLockAcquireException ex2 = new OpenLockAcquireException("test", new Exception());
            OpenLockAcquireException ex3 = new OpenLockAcquireException("test", "lock", new Exception());

            assertThat(ex1.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
            assertThat(ex2.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
            assertThat(ex3.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
        }
    }

    @Nested
    @DisplayName("Cause Chain Tests | 原因链测试")
    class CauseChainTests {

        @Test
        @DisplayName("should preserve cause chain")
        void shouldPreserveCauseChain() {
            Exception root = new Exception("root cause");
            RuntimeException middle = new RuntimeException("middle", root);
            OpenLockAcquireException exception = new OpenLockAcquireException("acquire failed", middle);

            assertThat(exception.getCause()).isSameAs(middle);
            assertThat(exception.getCause().getCause()).isSameAs(root);
        }

        @Test
        @DisplayName("should handle null cause")
        void shouldHandleNullCause() {
            OpenLockAcquireException exception = new OpenLockAcquireException("test", (Throwable) null);

            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("should handle InterruptedException as cause")
        void shouldHandleInterruptedExceptionAsCause() {
            InterruptedException cause = new InterruptedException("interrupted");
            OpenLockAcquireException exception = new OpenLockAcquireException("acquire interrupted", cause);

            assertThat(exception.getCause()).isInstanceOf(InterruptedException.class);
        }
    }

    @Nested
    @DisplayName("Throwable Tests | 可抛出测试")
    class ThrowableTests {

        @Test
        @DisplayName("should be throwable and catchable as OpenLockAcquireException")
        void shouldBeThrowableAndCatchableAsOpenLockAcquireException() {
            assertThatThrownBy(() -> {
                throw new OpenLockAcquireException("lock acquire failed");
            }).isInstanceOf(OpenLockAcquireException.class)
              .hasMessageContaining("lock acquire failed");
        }

        @Test
        @DisplayName("should be catchable as OpenLockException")
        void shouldBeCatchableAsOpenLockException() {
            assertThatThrownBy(() -> {
                throw new OpenLockAcquireException("test");
            }).isInstanceOf(OpenLockException.class);
        }

        @Test
        @DisplayName("caught exception should have correct lockName")
        void caughtException_shouldHaveCorrectLockName() {
            try {
                throw new OpenLockAcquireException("acquire failed", "testLock", new Exception());
            } catch (OpenLockAcquireException e) {
                assertThat(e.lockName()).isEqualTo("testLock");
            }
        }
    }

    @Nested
    @DisplayName("Message Format Tests | 消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage() should include component prefix")
        void getMessage_shouldIncludeComponentPrefix() {
            OpenLockAcquireException exception = new OpenLockAcquireException("test");

            assertThat(exception.getMessage()).contains("[LOCK]");
        }
    }

    @Nested
    @DisplayName("Common Use Cases | 常见用例")
    class CommonUseCasesTests {

        @Test
        @DisplayName("should work for interrupted acquisition")
        void shouldWorkForInterruptedAcquisition() {
            InterruptedException cause = new InterruptedException();
            OpenLockAcquireException exception = new OpenLockAcquireException(
                    "Lock acquisition interrupted", cause
            );

            assertThat(exception.getMessage()).contains("interrupted");
            assertThat(exception.getCause()).isInstanceOf(InterruptedException.class);
        }

        @Test
        @DisplayName("should work for general acquisition failure")
        void shouldWorkForGeneralAcquisitionFailure() {
            RuntimeException cause = new RuntimeException("Connection lost");
            OpenLockAcquireException exception = new OpenLockAcquireException(
                    "Failed to acquire distributed lock", "redis:mylock", cause
            );

            assertThat(exception.lockName()).isEqualTo("redis:mylock");
            assertThat(exception.getCause().getMessage()).contains("Connection lost");
        }
    }
}
