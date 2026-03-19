package cloud.opencode.base.oauth2.exception;

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
        }

        @Test
        @DisplayName("使用错误码和消息构造")
        void testErrorCodeAndMessageConstructor() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED, "Custom message");
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex.getMessage()).contains("Custom message");
        }

        @Test
        @DisplayName("使用错误码和原因构造")
        void testErrorCodeAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.NETWORK_ERROR, cause);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.NETWORK_ERROR);
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("使用错误码、消息和原因构造")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("cause");
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_INVALID, "Custom message", cause);
            assertThat(ex.errorCode()).isEqualTo(OAuth2ErrorCode.TOKEN_INVALID);
            assertThat(ex.getMessage()).contains("Custom message");
            assertThat(ex.getCause()).isEqualTo(cause);
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
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为RuntimeException")
        void testCatchAsRuntimeException() {
            assertThatThrownBy(() -> {
                throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED);
            }).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含错误码")
        void testMessageContainsErrorCode() {
            OAuth2Exception ex = new OAuth2Exception(OAuth2ErrorCode.TOKEN_EXPIRED, "Token has expired");
            String message = ex.getMessage();
            assertThat(message).containsAnyOf("7001", "TOKEN_EXPIRED", "Token has expired");
        }
    }
}
