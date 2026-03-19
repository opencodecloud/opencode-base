package cloud.opencode.base.test.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TestErrorCodeTest Tests
 * TestErrorCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("TestErrorCode Tests")
class TestErrorCodeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have all expected assertion error codes")
        void shouldHaveAllExpectedAssertionErrorCodes() {
            assertThat(TestErrorCode.ASSERTION_FAILED).isNotNull();
            assertThat(TestErrorCode.EXPECTED_EXCEPTION_NOT_THROWN).isNotNull();
            assertThat(TestErrorCode.UNEXPECTED_EXCEPTION).isNotNull();
            assertThat(TestErrorCode.VALUE_MISMATCH).isNotNull();
            assertThat(TestErrorCode.ASSERTION_NULL).isNotNull();
            assertThat(TestErrorCode.ASSERTION_EQUALS).isNotNull();
            assertThat(TestErrorCode.ASSERTION_TIMEOUT).isNotNull();
        }

        @Test
        @DisplayName("Should have all expected setup error codes")
        void shouldHaveAllExpectedSetupErrorCodes() {
            assertThat(TestErrorCode.FIXTURE_NOT_FOUND).isNotNull();
            assertThat(TestErrorCode.FIXTURE_INIT_FAILED).isNotNull();
            assertThat(TestErrorCode.MOCK_SETUP_FAILED).isNotNull();
            assertThat(TestErrorCode.MOCK_CREATION_FAILED).isNotNull();
            assertThat(TestErrorCode.MOCK_NOT_INTERFACE).isNotNull();
            assertThat(TestErrorCode.MOCK_VERIFICATION_FAILED).isNotNull();
        }

        @Test
        @DisplayName("Should have all expected execution error codes")
        void shouldHaveAllExpectedExecutionErrorCodes() {
            assertThat(TestErrorCode.TIMEOUT).isNotNull();
            assertThat(TestErrorCode.CONCURRENT_ERROR).isNotNull();
            assertThat(TestErrorCode.BENCHMARK_FAILED).isNotNull();
            assertThat(TestErrorCode.BENCHMARK_TIMEOUT).isNotNull();
            assertThat(TestErrorCode.DATA_GENERATION_FAILED).isNotNull();
            assertThat(TestErrorCode.DATA_RANGE_INVALID).isNotNull();
        }

        @Test
        @DisplayName("Should have all expected general error codes")
        void shouldHaveAllExpectedGeneralErrorCodes() {
            assertThat(TestErrorCode.GENERAL_ERROR).isNotNull();
            assertThat(TestErrorCode.INVALID_CONFIGURATION).isNotNull();
        }
    }

    @Nested
    @DisplayName("code() Tests")
    class CodeTests {

        @Test
        @DisplayName("Should return correct code for assertion errors")
        void shouldReturnCorrectCodeForAssertionErrors() {
            assertThat(TestErrorCode.ASSERTION_FAILED.code()).isEqualTo("TEST-1001");
            assertThat(TestErrorCode.EXPECTED_EXCEPTION_NOT_THROWN.code()).isEqualTo("TEST-1002");
            assertThat(TestErrorCode.ASSERTION_TIMEOUT.code()).isEqualTo("TEST-1007");
        }

        @Test
        @DisplayName("Should return correct code for setup errors")
        void shouldReturnCorrectCodeForSetupErrors() {
            assertThat(TestErrorCode.FIXTURE_NOT_FOUND.code()).isEqualTo("TEST-2001");
            assertThat(TestErrorCode.MOCK_NOT_INTERFACE.code()).isEqualTo("TEST-2005");
        }

        @Test
        @DisplayName("Should return correct code for execution errors")
        void shouldReturnCorrectCodeForExecutionErrors() {
            assertThat(TestErrorCode.TIMEOUT.code()).isEqualTo("TEST-3001");
            assertThat(TestErrorCode.DATA_RANGE_INVALID.code()).isEqualTo("TEST-3006");
        }
    }

    @Nested
    @DisplayName("message() Tests")
    class MessageTests {

        @Test
        @DisplayName("Should return non-empty message")
        void shouldReturnNonEmptyMessage() {
            for (TestErrorCode code : TestErrorCode.values()) {
                assertThat(code.message()).isNotBlank();
            }
        }

        @Test
        @DisplayName("Should return correct message")
        void shouldReturnCorrectMessage() {
            assertThat(TestErrorCode.ASSERTION_FAILED.message()).isEqualTo("Assertion failed");
            assertThat(TestErrorCode.TIMEOUT.message()).isEqualTo("Test timeout");
            assertThat(TestErrorCode.MOCK_NOT_INTERFACE.message()).isEqualTo("Mock target is not an interface");
        }
    }

    @Nested
    @DisplayName("toString() Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return code and message")
        void shouldReturnCodeAndMessage() {
            String result = TestErrorCode.ASSERTION_FAILED.toString();
            assertThat(result).isEqualTo("TEST-1001: Assertion failed");
        }

        @Test
        @DisplayName("Should format correctly for all codes")
        void shouldFormatCorrectlyForAllCodes() {
            for (TestErrorCode code : TestErrorCode.values()) {
                String result = code.toString();
                assertThat(result).contains(code.code());
                assertThat(result).contains(code.message());
                assertThat(result).contains(": ");
            }
        }
    }

    @Nested
    @DisplayName("Enum Standard Methods Tests")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values() should return all codes")
        void valuesShouldReturnAllCodes() {
            TestErrorCode[] values = TestErrorCode.values();
            assertThat(values).hasSizeGreaterThanOrEqualTo(14);
        }

        @Test
        @DisplayName("valueOf() should return correct code")
        void valueOfShouldReturnCorrectCode() {
            assertThat(TestErrorCode.valueOf("ASSERTION_FAILED")).isEqualTo(TestErrorCode.ASSERTION_FAILED);
            assertThat(TestErrorCode.valueOf("TIMEOUT")).isEqualTo(TestErrorCode.TIMEOUT);
        }
    }
}
