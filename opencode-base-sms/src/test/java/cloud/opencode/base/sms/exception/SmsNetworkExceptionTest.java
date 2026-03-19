package cloud.opencode.base.sms.exception;

import org.junit.jupiter.api.*;

import java.net.URI;

import static org.assertj.core.api.Assertions.*;

/**
 * SmsNetworkExceptionTest Tests
 * SmsNetworkExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
@DisplayName("SmsNetworkException 测试")
class SmsNetworkExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用错误码和主机创建")
        void testCreateWithHost() {
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "api.example.com");

            assertThat(exception.getHost()).isEqualTo("api.example.com");
            assertThat(exception.getPort()).isEqualTo(-1);
            assertThat(exception.getStatusCode()).isEqualTo(-1);
        }

        @Test
        @DisplayName("使用错误码、主机和端口创建")
        void testCreateWithPort() {
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "api.example.com", 443);

            assertThat(exception.getHost()).isEqualTo("api.example.com");
            assertThat(exception.getPort()).isEqualTo(443);
        }

        @Test
        @DisplayName("使用错误码、主机、状态码和消息创建")
        void testCreateWithStatusCode() {
            SmsNetworkException exception = new SmsNetworkException(
                    SmsErrorCode.NETWORK_ERROR,
                    "api.example.com",
                    500,
                    "Internal Server Error"
            );

            assertThat(exception.getHost()).isEqualTo("api.example.com");
            assertThat(exception.getStatusCode()).isEqualTo(500);
            assertThat(exception.getMessage()).contains("Internal Server Error");
        }

        @Test
        @DisplayName("使用错误码、主机和原因创建")
        void testCreateWithCause() {
            Throwable cause = new RuntimeException("Connection refused");
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "api.example.com", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("networkError-主机和原因")
        void testNetworkErrorWithCause() {
            Throwable cause = new RuntimeException("Error");
            SmsNetworkException exception = SmsNetworkException.networkError("api.example.com", cause);

            assertThat(exception.getErrorCode()).isEqualTo(SmsErrorCode.NETWORK_ERROR);
            assertThat(exception.getHost()).isEqualTo("api.example.com");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("networkError-URI和原因")
        void testNetworkErrorWithUri() {
            URI uri = URI.create("https://api.example.com/sms");
            Throwable cause = new RuntimeException("Error");

            SmsNetworkException exception = SmsNetworkException.networkError(uri, cause);

            assertThat(exception.getHost()).isEqualTo("api.example.com");
        }

        @Test
        @DisplayName("httpError工厂方法")
        void testHttpError() {
            SmsNetworkException exception = SmsNetworkException.httpError(
                    "api.example.com",
                    503,
                    "Service Unavailable"
            );

            assertThat(exception.getStatusCode()).isEqualTo(503);
            assertThat(exception.getMessage()).contains("503");
        }

        @Test
        @DisplayName("connectionRefused工厂方法")
        void testConnectionRefused() {
            SmsNetworkException exception = SmsNetworkException.connectionRefused("api.example.com", 443);

            assertThat(exception.getHost()).isEqualTo("api.example.com");
            assertThat(exception.getPort()).isEqualTo(443);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("getHost返回主机")
        void testGetHost() {
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "test.com");

            assertThat(exception.getHost()).isEqualTo("test.com");
        }

        @Test
        @DisplayName("getPort返回端口")
        void testGetPort() {
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "test.com", 8080);

            assertThat(exception.getPort()).isEqualTo(8080);
        }

        @Test
        @DisplayName("getStatusCode返回状态码")
        void testGetStatusCode() {
            SmsNetworkException exception = new SmsNetworkException(SmsErrorCode.NETWORK_ERROR, "test.com", 404, "Not Found");

            assertThat(exception.getStatusCode()).isEqualTo(404);
        }
    }
}
