package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * DataGenerationExceptionTest Tests
 * DataGenerationExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("DataGenerationException Tests")
class DataGenerationExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create with error code")
        void shouldCreateWithErrorCode() {
            DataGenerationException ex = new DataGenerationException(TestErrorCode.DATA_GENERATION_FAILED);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.DATA_GENERATION_FAILED);
        }

        @Test
        @DisplayName("Should create with error code and detail")
        void shouldCreateWithErrorCodeAndDetail() {
            DataGenerationException ex = new DataGenerationException(TestErrorCode.DATA_RANGE_INVALID, "invalid");

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.DATA_RANGE_INVALID);
            assertThat(ex.getMessage()).contains("invalid");
        }

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            DataGenerationException ex = new DataGenerationException("Custom message");
            assertThat(ex.getMessage()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            Throwable cause = new RuntimeException("root");
            DataGenerationException ex = new DataGenerationException("Custom message", cause);

            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("generationFailed() should create generation failed exception")
        void generationFailedShouldCreateGenerationFailedException() {
            DataGenerationException ex = DataGenerationException.generationFailed("email");

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.DATA_GENERATION_FAILED);
            assertThat(ex.getMessage()).contains("email");
        }

        @Test
        @DisplayName("invalidRange() should create invalid range exception")
        void invalidRangeShouldCreateInvalidRangeException() {
            DataGenerationException ex = DataGenerationException.invalidRange(10, 5);

            assertThat(ex.getErrorCode()).isEqualTo(TestErrorCode.DATA_RANGE_INVALID);
            assertThat(ex.getMessage()).contains("10");
            assertThat(ex.getMessage()).contains("5");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend TestException")
        void shouldExtendTestException() {
            DataGenerationException ex = new DataGenerationException(TestErrorCode.DATA_GENERATION_FAILED);
            assertThat(ex).isInstanceOf(TestException.class);
        }
    }
}
