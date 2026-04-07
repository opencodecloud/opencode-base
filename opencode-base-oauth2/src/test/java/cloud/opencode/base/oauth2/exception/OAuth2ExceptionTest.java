package cloud.opencode.base.oauth2.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ExceptionTest Tests
 * OAuth2ExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2Exception 测试")
class OAuth2ExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用错误码构造")
        void testErrorCodeConstructor() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.getMessage()).contains("Token has expired");
            assertThat(ex.details()).isNull();
            assertThat(ex.serverError()).isNull();
            assertThat(ex.serverErrorDescription()).isNull();
            assertThat(ex.serverErrorUri()).isNull();
        }

        @Test
        @DisplayName("使用错误码和消息构造")
        void testErrorCodeAndMessageConstructor() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED, "Custom message");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.details()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("使用错误码和原因构造")
        void testErrorCodeAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, cause);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.NETWORK_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.details()).isEqualTo("cause");
        }

        @Test
        @DisplayName("使用错误码、消息和原因构造")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("cause");
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID, "Custom message", cause);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_INVALID);
            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.details()).isEqualTo("Custom message");
        }

        @Test
        @DisplayName("使用服务器错误字段构造")
        void testServerErrorConstructor() {
            OAuth2Exception ex = new OAuth2Exception(
                    OAuth2ErrorCode.PROVIDER_ERROR, "invalid_grant",
                    "invalid_grant", "The authorization code has expired",
                    "https://example.com/errors/expired");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.PROVIDER_ERROR);
            assertThat(ex.serverError()).isEqualTo("invalid_grant");
            assertThat(ex.serverErrorDescription()).isEqualTo("The authorization code has expired");
            assertThat(ex.serverErrorUri()).isEqualTo("https://example.com/errors/expired");
        }

        @Test
        @DisplayName("使用null原因构造")
        void testNullCauseConstructor() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, (Throwable) null);
            assertThat(ex.getCause()).isNull();
            assertThat(ex.details()).isNull();
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("tokenExpired")
        void testTokenExpired() {
            OAuth2Exception ex = OAuth2Exception.tokenExpired();
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("tokenInvalid")
        void testTokenInvalid() {
            OAuth2Exception ex = OAuth2Exception.tokenInvalid("Invalid token format");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_INVALID);
            assertThat(ex.getMessage()).contains("Invalid token format");
        }

        @Test
        @DisplayName("authorizationFailed")
        void testAuthorizationFailed() {
            OAuth2Exception ex = OAuth2Exception.authorizationFailed("Access denied");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.AUTHORIZATION_FAILED);
            assertThat(ex.getMessage()).contains("Access denied");
        }

        @Test
        @DisplayName("networkError")
        void testNetworkError() {
            Throwable cause = new RuntimeException("Connection refused");
            OAuth2Exception ex = OAuth2Exception.networkError(cause);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.NETWORK_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("invalidConfig")
        void testInvalidConfig() {
            OAuth2Exception ex = OAuth2Exception.invalidConfig("Missing client ID");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.INVALID_CONFIG);
            assertThat(ex.getMessage()).contains("Missing client ID");
        }

        @Test
        @DisplayName("fromServerError with all fields")
        void testFromServerError() {
            OAuth2Exception ex = OAuth2Exception.fromServerError(
                    "invalid_grant", "Code expired", "https://example.com/err");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.PROVIDER_ERROR);
            assertThat(ex.serverError()).isEqualTo("invalid_grant");
            assertThat(ex.serverErrorDescription()).isEqualTo("Code expired");
            assertThat(ex.serverErrorUri()).isEqualTo("https://example.com/err");
            assertThat(ex.details()).isEqualTo("Code expired");
        }

        @Test
        @DisplayName("fromServerError with null description uses error as message")
        void testFromServerErrorNullDescription() {
            OAuth2Exception ex = OAuth2Exception.fromServerError("invalid_grant", null, null);
            assertThat(ex.serverError()).isEqualTo("invalid_grant");
            assertThat(ex.serverErrorDescription()).isNull();
            assertThat(ex.serverErrorUri()).isNull();
            assertThat(ex.details()).isEqualTo("invalid_grant");
        }
    }

    @Nested
    @DisplayName("Getter测试")
    class GetterTests {

        @Test
        @DisplayName("errorCode方法")
        void testErrorCode() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("code方法")
        void testCode() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.code()).isEqualTo(7001);
        }
    }

    @Nested
    @DisplayName("异常层次结构测试")
    class HierarchyTests {

        @Test
        @DisplayName("继承OpenException")
        void testExtendsOpenException() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为OpenException")
        void testCatchAsOpenException() {
            assertThatThrownBy(() -> {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            }).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("可以被捕获为RuntimeException")
        void testCatchAsRuntimeException() {
            assertThatThrownBy(() -> {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            }).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("OpenException组件名为OAuth2")
        void testOpenExceptionComponent() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.getComponent()).isEqualTo("OAuth2");
        }

        @Test
        @DisplayName("OpenException错误码为字符串形式的int code")
        void testOpenExceptionErrorCode() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.getErrorCode()).isEqualTo("7001");
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含组件名和错误码")
        void testMessageContainsComponentAndCode() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            String message = ex.getMessage();
            assertThat(message).contains("[OAuth2]");
            assertThat(message).contains("(7001)");
            assertThat(message).contains("Token has expired");
        }

        @Test
        @DisplayName("消息包含详情")
        void testMessageContainsDetails() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED, "Custom detail");
            String message = ex.getMessage();
            assertThat(message).contains("Custom detail");
        }

        @Test
        @DisplayName("消息包含服务器错误详情")
        void testMessageContainsServerErrorDetails() {
            OAuth2Exception ex = OAuth2Exception.fromServerError(
                    "invalid_grant", "Code expired", "https://example.com/err");
            String message = ex.getMessage();
            assertThat(message).contains("[server_error=invalid_grant]");
            assertThat(message).contains("[server_error_description=Code expired]");
            assertThat(message).contains("[server_error_uri=https://example.com/err]");
        }

        @Test
        @DisplayName("无服务器错误时消息不包含服务器错误标记")
        void testMessageWithoutServerError() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            String message = ex.getMessage();
            assertThat(message).doesNotContain("[server_error=");
        }

        @Test
        @DisplayName("getRawMessage返回原始消息")
        void testGetRawMessage() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED, "detail");
            String rawMessage = ex.getRawMessage();
            assertThat(rawMessage).isEqualTo("Token has expired: detail");
            assertThat(rawMessage).doesNotContain("[OAuth2]");
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("实现Serializable")
        void testIsSerializable() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex).isInstanceOf(java.io.Serializable.class);
        }
    }
}
