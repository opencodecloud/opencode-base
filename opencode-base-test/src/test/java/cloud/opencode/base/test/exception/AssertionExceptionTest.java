package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AssertionExceptionTest Tests
 * AssertionExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("AssertionException Tests")
class AssertionExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with error code")
        void shouldCreateWithErrorCode() {
            AssertionException ex = new AssertionException(TestErrorCode.ASSERTION_FAILED);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).contains("Assertion failed");
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            AssertionException ex = new AssertionException(TestErrorCode.ASSERTION_EQUALS, "expected 1 but was 2");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_EQUALS);
            assertThat(ex.getMessage()).contains("expected 1 but was 2");
        }

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            AssertionException ex = new AssertionException("Custom message");

            assertThat(ex.getMessage()).contains("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root");
            AssertionException ex = new AssertionException("Custom message", cause);

            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("failed() should create assertion failed exception")
        void failedShouldCreateAssertionFailedException() {
            AssertionException ex = AssertionException.failed("test message");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex.getMessage()).contains("test message");
        }

        @Test
        @DisplayName("nullAssertion() should create null assertion exception")
        void nullAssertionShouldCreateNullAssertionException() {
            AssertionException ex = AssertionException.nullAssertion();

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_NULL);
        }

        @Test
        @DisplayName("notEqual() should create equality assertion exception")
        void notEqualShouldCreateEqualityAssertionException() {
            AssertionException ex = AssertionException.notEqual("expected", "actual");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_EQUALS);
            assertThat(ex.getMessage()).contains("expected");
            assertThat(ex.getMessage()).contains("actual");
        }

        @Test
        @DisplayName("timeout() should create timeout assertion exception")
        void timeoutShouldCreateTimeoutAssertionException() {
            AssertionException ex = AssertionException.timeout(1000, 1500);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.ASSERTION_TIMEOUT);
            assertThat(ex.getMessage()).contains("1000ms");
            assertThat(ex.getMessage()).contains("1500ms");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend TestException")
        void shouldExtendTestException() {
            AssertionException ex = new AssertionException(TestErrorCode.ASSERTION_FAILED);
            assertThat(ex).isInstanceOf(TestException.class);
        }
    }
}
