package cloud.opencode.base.lock.exception;

import org.junit.jupiter.api.*;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenLockTimeoutException test - 锁超时异常测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lock V1.0.0
 */
class OpenLockTimeoutExceptionTest {

    @Nested
    @DisplayName("Constructor Tests | 构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessage_shouldSetMessage() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout occurred");

            assertThat(exception.getMessage()).contains("timeout occurred");
            assertThat(exception.waitTime()).isNull();
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("constructor with message and waitTime should set both")
        void constructorWithMessageAndWaitTime_shouldSetBoth() {
            Duration waitTime = Duration.ofSeconds(5);
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout", waitTime);

            assertThat(exception.getMessage()).contains("timeout");
            assertThat(exception.waitTime()).isEqualTo(waitTime);
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("constructor with lockName and waitTime should set all")
        void constructorWithLockNameAndWaitTime_shouldSetAll() {
            Duration waitTime = Duration.ofSeconds(10);
            OpenLockTimeoutException exception = new OpenLockTimeoutException(
                    "timeout", "myLock", waitTime
            );

            assertThat(exception.getMessage()).contains("timeout");
            assertThat(exception.lockName()).isEqualTo("myLock");
            assertThat(exception.waitTime()).isEqualTo(waitTime);
            assertThat(exception.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }
    }

    @Nested
    @DisplayName("WaitTime Accessor Tests | 等待时间访问器测试")
    class WaitTimeAccessorTests {

        @Test
        @DisplayName("waitTime() should return wait time")
        void waitTime_shouldReturnWaitTime() {
            Duration waitTime = Duration.ofMillis(500);
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout", waitTime);

            assertThat(exception.waitTime()).isEqualTo(waitTime);
        }

        @Test
        @DisplayName("waitTime() should return null when not set")
        void waitTime_shouldReturnNullWhenNotSet() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout");

            assertThat(exception.waitTime()).isNull();
        }

        @Test
        @DisplayName("waitTime() should handle zero duration")
        void waitTime_shouldHandleZeroDuration() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout", Duration.ZERO);

            assertThat(exception.waitTime()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("waitTime() should handle large duration")
        void waitTime_shouldHandleLargeDuration() {
            Duration largeWait = Duration.ofDays(365);
            OpenLockTimeoutException exception = new OpenLockTimeoutException("timeout", largeWait);

            assertThat(exception.waitTime()).isEqualTo(largeWait);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests | 继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenLockException")
        void shouldExtendOpenLockException() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("test");

            assertThat(exception).isInstanceOf(OpenLockException.class);
        }

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("errorType should always be TIMEOUT")
        void errorType_shouldAlwaysBeTimeout() {
            OpenLockTimeoutException ex1 = new OpenLockTimeoutException("test");
            OpenLockTimeoutException ex2 = new OpenLockTimeoutException("test", Duration.ofSeconds(1));
            OpenLockTimeoutException ex3 = new OpenLockTimeoutException("test", "lock", Duration.ofSeconds(1));

            assertThat(ex1.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
            assertThat(ex2.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
            assertThat(ex3.errorType()).isEqualTo(OpenLockException.LockErrorType.TIMEOUT);
        }
    }

    @Nested
    @DisplayName("Throwable Tests | 可抛出测试")
    class ThrowableTests {

        @Test
        @DisplayName("should be throwable and catchable as OpenLockTimeoutException")
        void shouldBeThrowableAndCatchableAsOpenLockTimeoutException() {
            assertThatThrownBy(() -> {
                throw new OpenLockTimeoutException("lock timeout", Duration.ofSeconds(5));
            }).isInstanceOf(OpenLockTimeoutException.class)
              .hasMessageContaining("lock timeout");
        }

        @Test
        @DisplayName("should be catchable as OpenLockException")
        void shouldBeCatchableAsOpenLockException() {
            assertThatThrownBy(() -> {
                throw new OpenLockTimeoutException("timeout");
            }).isInstanceOf(OpenLockException.class);
        }

        @Test
        @DisplayName("caught exception should have correct waitTime")
        void caughtException_shouldHaveCorrectWaitTime() {
            Duration waitTime = Duration.ofMillis(100);

            try {
                throw new OpenLockTimeoutException("timeout", waitTime);
            } catch (OpenLockTimeoutException e) {
                assertThat(e.waitTime()).isEqualTo(waitTime);
            }
        }
    }

    @Nested
    @DisplayName("Message Format Tests | 消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage() should include component prefix")
        void getMessage_shouldIncludeComponentPrefix() {
            OpenLockTimeoutException exception = new OpenLockTimeoutException("test");

            assertThat(exception.getMessage()).contains("[LOCK]");
        }
    }
}
