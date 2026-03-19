package cloud.opencode.base.web;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultCodeTest Tests
 * ResultCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ResultCode Tests")
class ResultCodeTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("of should create result code with code and message")
        void ofShouldCreateResultCodeWithCodeAndMessage() {
            ResultCode resultCode = ResultCode.of("E001", "Error message");

            assertThat(resultCode.getCode()).isEqualTo("E001");
            assertThat(resultCode.getMessage()).isEqualTo("Error message");
            assertThat(resultCode.getHttpStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("of should create result code with code, message and HTTP status")
        void ofShouldCreateResultCodeWithCodeMessageAndHttpStatus() {
            ResultCode resultCode = ResultCode.of("E001", "Error message", 400);

            assertThat(resultCode.getCode()).isEqualTo("E001");
            assertThat(resultCode.getMessage()).isEqualTo("Error message");
            assertThat(resultCode.getHttpStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("Default Methods Tests")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getHttpStatus should return 200 by default")
        void getHttpStatusShouldReturn200ByDefault() {
            ResultCode resultCode = ResultCode.of("E001", "Error");

            assertThat(resultCode.getHttpStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("isSuccess should return true for 2xx status")
        void isSuccessShouldReturnTrueFor2xxStatus() {
            ResultCode result200 = ResultCode.of("00000", "Success", 200);
            ResultCode result201 = ResultCode.of("00001", "Created", 201);
            ResultCode result299 = ResultCode.of("00099", "OK", 299);

            assertThat(result200.isSuccess()).isTrue();
            assertThat(result201.isSuccess()).isTrue();
            assertThat(result299.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("isSuccess should return false for non-2xx status")
        void isSuccessShouldReturnFalseForNon2xxStatus() {
            ResultCode result400 = ResultCode.of("A0400", "Bad Request", 400);
            ResultCode result500 = ResultCode.of("B0500", "Server Error", 500);
            ResultCode result199 = ResultCode.of("00000", "Info", 199);

            assertThat(result400.isSuccess()).isFalse();
            assertThat(result500.isSuccess()).isFalse();
            assertThat(result199.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("custom result code should implement interface correctly")
        void customResultCodeShouldImplementInterfaceCorrectly() {
            ResultCode customCode = new ResultCode() {
                @Override
                public String getCode() {
                    return "CUSTOM001";
                }

                @Override
                public String getMessage() {
                    return "Custom message";
                }

                @Override
                public int getHttpStatus() {
                    return 418;
                }
            };

            assertThat(customCode.getCode()).isEqualTo("CUSTOM001");
            assertThat(customCode.getMessage()).isEqualTo("Custom message");
            assertThat(customCode.getHttpStatus()).isEqualTo(418);
            assertThat(customCode.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Anonymous Implementation Tests")
    class AnonymousImplementationTests {

        @Test
        @DisplayName("anonymous implementation from of method should work correctly")
        void anonymousImplementationFromOfMethodShouldWorkCorrectly() {
            ResultCode resultCode = ResultCode.of("TEST", "Test message", 202);

            assertThat(resultCode.getCode()).isEqualTo("TEST");
            assertThat(resultCode.getMessage()).isEqualTo("Test message");
            assertThat(resultCode.getHttpStatus()).isEqualTo(202);
            assertThat(resultCode.isSuccess()).isTrue();
        }
    }
}
