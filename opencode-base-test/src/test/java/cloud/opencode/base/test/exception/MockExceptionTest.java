package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MockExceptionTest Tests
 * MockExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("MockException Tests")
class MockExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with error code")
        void shouldCreateWithErrorCode() {
            MockException ex = new MockException(TestErrorCode.MOCK_SETUP_FAILED);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_SETUP_FAILED);
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            MockException ex = new MockException(TestErrorCode.MOCK_CREATION_FAILED, "details");

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_CREATION_FAILED);
            assertThat(ex.getMessage()).contains("details");
        }

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            MockException ex = new MockException("Custom message");
            assertThat(ex.getMessage()).contains("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root");
            MockException ex = new MockException("Custom message", cause);

            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("creationFailed() should create mock creation exception")
        void creationFailedShouldCreateMockCreationException() {
            MockException ex = MockException.creationFailed(String.class);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_CREATION_FAILED);
            assertThat(ex.getMessage()).contains("java.lang.String");
        }

        @Test
        @DisplayName("notInterface() should create not interface exception")
        void notInterfaceShouldCreateNotInterfaceException() {
            MockException ex = MockException.notInterface(String.class);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_NOT_INTERFACE);
            assertThat(ex.getMessage()).contains("java.lang.String");
            assertThat(ex.getMessage()).contains("Only interfaces");
        }

        @Test
        @DisplayName("verificationFailed() should create verification exception")
        void verificationFailedShouldCreateVerificationException() {
            MockException ex = MockException.verificationFailed("called once", 3);

            assertThat(ex.getTestErrorCode()).isEqualTo(TestErrorCode.MOCK_VERIFICATION_FAILED);
            assertThat(ex.getMessage()).contains("called once");
            assertThat(ex.getMessage()).contains("3");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend TestException")
        void shouldExtendTestException() {
            MockException ex = new MockException(TestErrorCode.MOCK_SETUP_FAILED);
            assertThat(ex).isInstanceOf(TestException.class);
        }
    }
}
