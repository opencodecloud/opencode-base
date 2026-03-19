package cloud.opencode.base.lock.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLockException test - 锁异常测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class OpenLockExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessage_shouldSetMessage() {
            OpenLockException exception = new OpenLockException("test message");

            assertThat(exception.getMessage()).contains("test message");
            assertThat(exception.lockName()).isNull();
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.GENERAL);
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCause_shouldSetBoth() {
            RuntimeException cause = new RuntimeException("cause");
            OpenLockException exception = new OpenLockException("test message", cause);

            assertThat(exception.getMessage()).contains("test message");
            assertThat(exception.getCause()).isSameAs(cause);
            assertThat(exception.lockName()).isNull();
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.GENERAL);
        }

        @Test
        @DisplayName("constructor with full details should set all fields")
        void constructorWithFullDetails_shouldSetAllFields() {
            OpenLockException exception = new OpenLockException(
                    "test message",
                    "myLock",
                    OpenLockException.LockErrorType.TIMEOUT
            );

            assertThat(exception.getMessage()).contains("test message");
            assertThat(exception.lockName()).isEqualTo("myLock");
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("constructor with full details and cause should set all fields")
        void constructorWithFullDetailsAndCause_shouldSetAllFields() {
            RuntimeException cause = new RuntimeException("cause");
            OpenLockException exception = new OpenLockException(
                    "test message",
                    "myLock",
                    OpenLockException.LockErrorType.ACQUIRE,
                    cause
            );

            assertThat(exception.getMessage()).contains("test message");
            assertThat(exception.lockName()).isEqualTo("myLock");
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.ACQUIRE);
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Accessor Tests | 访问器测试")
    class AccessorTests {

        @Test
        @DisplayName("lockName() should return lock name")
        void lockName_shouldReturnLockName() {
            OpenLockException exception = new OpenLockException(
                    "test", "myLock", OpenLockException.LockErrorType.GENERAL
            );

            assertThat(exception.lockName()).isEqualTo("myLock");
        }

        @Test
        @DisplayName("lockName() should return null when not set")
        void lockName_shouldReturnNullWhenNotSet() {
            OpenLockException exception = new OpenLockException("test");

            assertThat(exception.lockName()).isNull();
        }

        @Test
        @DisplayName("errorType() should return error type")
        void errorType_shouldReturnErrorType() {
            OpenLockException exception = new OpenLockException(
                    "test", "myLock", OpenLockException.LockErrorType.DEADLOCK
            );

            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.DEADLOCK);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests | 继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            OpenLockException exception = new OpenLockException("test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be throwable and catchable")
        void shouldBeThrowableAndCatchable() {
            assertThatThrownBy(() -> {
                throw new OpenLockException("test error");
            }).isInstanceOf(OpenLockException.class)
              .hasMessageContaining("test error");
        }
    }

    @Nested
    @DisplayName("LockErrorType Tests | 锁错误类型测试")
    class LockErrorTypeTests {

        @Test
        @DisplayName("should have all expected error types")
        void shouldHaveAllExpectedErrorTypes() {
            OpenLockException.LockErrorType[] types = OpenLockException.LockErrorType.values();

            assertThat(types).contains(
                    OpenLockException.LockErrorType.GENERAL,
                    OpenLockException.LockErrorType.TIMEOUT,
                    OpenLockException.LockErrorType.ACQUIRE,
                    OpenLockException.LockErrorType.RELEASE,
                    OpenLockException.LockErrorType.EXPIRED,
                    OpenLockException.LockErrorType.NOT_HELD,
                    OpenLockException.LockErrorType.DEADLOCK,
                    OpenLockException.LockErrorType.INTERRUPTED
            );
        }

        @Test
        @DisplayName("valueOf() should work correctly")
        void valueOf_shouldWorkCorrectly() {
            assertThat(OpenLockException.LockErrorType.valueOf("TIMEOUT"))
                    .isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("ordinal() should return unique indices")
        void ordinal_shouldReturnUniqueIndices() {
            OpenLockException.LockErrorType[] types = OpenLockException.LockErrorType.values();

            for (int i = 0; i < types.length; i++) {
                assertThat(types[i].ordinal()).isEqualTo(i);
            }
        }
    }

    @Nested
    @DisplayName("Message Format Tests | 消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage() should include component prefix")
        void getMessage_shouldIncludeComponentPrefix() {
            OpenLockException exception = new OpenLockException("test message");

            assertThat(exception.getMessage()).contains("[LOCK]");
        }
    }
}
