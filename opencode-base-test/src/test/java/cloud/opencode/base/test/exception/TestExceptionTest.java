package cloud.opencode.base.test.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TestExceptionTest Tests
 * TestExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestException Tests")
class TestExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with error code")
        void shouldCreateWithErrorCode() {
            TestException ex = new TestException(TestErrorCode.ASSERTION_FAILED);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getErrorCode()).isEqualTo("TEST-1001");
            assertThat(ex.getComponent()).isEqualTo("Test");
            assertThat(ex.getMessage()).contains("Assertion failed");
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            TestException ex = new TestException(TestErrorCode.ASSERTION_FAILED, "expected true but was false");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).contains("Assertion failed: expected true but was false");
        }

        @Test
        @DisplayName("Should create with error code and cause")
        void shouldCreateWithErrorCodeAndCause() {
            Throwable cause = new RuntimeException("root cause");
            TestException ex = new TestException(TestErrorCode.TIMEOUT, cause);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.TIMEOUT);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            TestException ex = new TestException("Custom message");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.GENERAL_ERROR);
            assertThat(ex.getMessage()).contains("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root cause");
            TestException ex = new TestException("Custom message", cause);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.GENERAL_ERROR);
            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getTestErrorCode() Tests")
    class GetTestErrorCodeTests {

        @Test
        @DisplayName("Should return correct test error code")
        void shouldReturnCorrectTestErrorCode() {
            TestException ex = new TestException(TestErrorCode.MOCK_SETUP_FAILED);
            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_SETUP_FAILED);
        }

        @Test
        @DisplayName("getErrorCode() should return string error code from OpenException")
        void getErrorCodeShouldReturnStringErrorCode() {
            TestException ex = new TestException(TestErrorCode.MOCK_SETUP_FAILED);
            assertThat(ex.getErrorCode()).isEqualTo("TEST-2003");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be OpenException")
        void shouldBeOpenException() {
            TestException ex = new TestException(TestErrorCode.GENERAL_ERROR);
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("Should be RuntimeException")
        void shouldBeRuntimeException() {
            TestException ex = new TestException(TestErrorCode.GENERAL_ERROR);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage() should include component and error code prefix")
        void getMessageShouldIncludePrefix() {
            TestException ex = new TestException(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).isEqualTo("[Test] (TEST-1001) Assertion failed");
        }

        @Test
        @DisplayName("getRawMessage() should return message without prefix")
        void getRawMessageShouldReturnMessageWithoutPrefix() {
            TestException ex = new TestException(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getRawMessage()).isEqualTo("Assertion failed");
        }
    }
}
