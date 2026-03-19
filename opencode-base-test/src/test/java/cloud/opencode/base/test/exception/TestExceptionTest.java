package cloud.opencode.base.test.exception;

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

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).isEqualTo("Assertion failed");
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            TestException ex = new TestException(TestErrorCode.ASSERTION_FAILED, "expected true but was false");

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).isEqualTo("Assertion failed: expected true but was false");
        }

        @Test
        @DisplayName("Should create with error code and cause")
        void shouldCreateWithErrorCodeAndCause() {
            Throwable cause = new RuntimeException("root cause");
            TestException ex = new TestException(TestErrorCode.TIMEOUT, cause);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.TIMEOUT);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create with message only")
        void shouldCreateWithMessageOnly() {
            TestException ex = new TestException("Custom message");

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.GENERAL_ERROR);
            assertThat(ex.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root cause");
            TestException ex = new TestException("Custom message", cause);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.GENERAL_ERROR);
            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("getErrorCode() Tests")
    class GetErrorCodeTests {

        @Test
        @DisplayName("Should return correct error code")
        void shouldReturnCorrectErrorCode() {
            TestException ex = new TestException(TestErrorCode.MOCK_SETUP_FAILED);
            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.MOCK_SETUP_FAILED);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be RuntimeException")
        void shouldBeRuntimeException() {
            TestException ex = new TestException(TestErrorCode.GENERAL_ERROR);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
