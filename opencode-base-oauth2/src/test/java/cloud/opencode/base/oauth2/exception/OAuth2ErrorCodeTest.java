package cloud.opencode.base.oauth2.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OAuth2ErrorCodeTest Tests
 * OAuth2ErrorCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("OAuth2ErrorCode 测试")
class OAuth2ErrorCodeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("Token错误码 (7001-7020)")
        void testTokenErrorCodes() {
            assertThat(OAuth2ErrorCode.TOKEN_EXPIRED.code()).isEqualTo(7001);
            assertThat(OAuth2ErrorCode.TOKEN_INVALID.code()).isEqualTo(7002);
            assertThat(OAuth2ErrorCode.TOKEN_REFRESH_FAILED.code()).isEqualTo(7003);
            assertThat(OAuth2ErrorCode.TOKEN_REVOKED.code()).isEqualTo(7004);
            assertThat(OAuth2ErrorCode.TOKEN_STORE_ERROR.code()).isEqualTo(7005);
            assertThat(OAuth2ErrorCode.TOKEN_NOT_FOUND.code()).isEqualTo(7006);
            assertThat(OAuth2ErrorCode.TOKEN_PARSE_ERROR.code()).isEqualTo(7007);
        }

        @Test
        @DisplayName("授权错误码 (7021-7040)")
        void testAuthorizationErrorCodes() {
            assertThat(OAuth2ErrorCode.AUTHORIZATION_FAILED.code()).isEqualTo(7021);
            assertThat(OAuth2ErrorCode.AUTHORIZATION_DENIED.code()).isEqualTo(7022);
            assertThat(OAuth2ErrorCode.INVALID_GRANT.code()).isEqualTo(7023);
            assertThat(OAuth2ErrorCode.INVALID_SCOPE.code()).isEqualTo(7024);
            assertThat(OAuth2ErrorCode.INVALID_STATE.code()).isEqualTo(7025);
            assertThat(OAuth2ErrorCode.CODE_EXPIRED.code()).isEqualTo(7026);
            assertThat(OAuth2ErrorCode.AUTHORIZATION_PENDING.code()).isEqualTo(7027);
            assertThat(OAuth2ErrorCode.SLOW_DOWN.code()).isEqualTo(7028);
            assertThat(OAuth2ErrorCode.ACCESS_DENIED.code()).isEqualTo(7029);
        }

        @Test
        @DisplayName("Provider错误码 (7041-7060)")
        void testProviderErrorCodes() {
            assertThat(OAuth2ErrorCode.PROVIDER_NOT_FOUND.code()).isEqualTo(7041);
            assertThat(OAuth2ErrorCode.PROVIDER_ERROR.code()).isEqualTo(7042);
            assertThat(OAuth2ErrorCode.USERINFO_NOT_SUPPORTED.code()).isEqualTo(7043);
            assertThat(OAuth2ErrorCode.REVOCATION_NOT_SUPPORTED.code()).isEqualTo(7044);
            assertThat(OAuth2ErrorCode.DEVICE_CODE_NOT_SUPPORTED.code()).isEqualTo(7045);
        }

        @Test
        @DisplayName("PKCE错误码 (7061-7070)")
        void testPkceErrorCodes() {
            assertThat(OAuth2ErrorCode.PKCE_ERROR.code()).isEqualTo(7061);
            assertThat(OAuth2ErrorCode.PKCE_REQUIRED.code()).isEqualTo(7062);
            assertThat(OAuth2ErrorCode.INVALID_PKCE_VERIFIER.code()).isEqualTo(7063);
        }

        @Test
        @DisplayName("网络错误码 (7071-7080)")
        void testNetworkErrorCodes() {
            assertThat(OAuth2ErrorCode.NETWORK_ERROR.code()).isEqualTo(7071);
            assertThat(OAuth2ErrorCode.TIMEOUT.code()).isEqualTo(7072);
            assertThat(OAuth2ErrorCode.SERVER_ERROR.code()).isEqualTo(7073);
            assertThat(OAuth2ErrorCode.INVALID_RESPONSE.code()).isEqualTo(7074);
        }

        @Test
        @DisplayName("配置错误码 (7081-7090)")
        void testConfigErrorCodes() {
            assertThat(OAuth2ErrorCode.INVALID_CONFIG.code()).isEqualTo(7081);
            assertThat(OAuth2ErrorCode.MISSING_CLIENT_ID.code()).isEqualTo(7082);
            assertThat(OAuth2ErrorCode.MISSING_CLIENT_SECRET.code()).isEqualTo(7083);
            assertThat(OAuth2ErrorCode.MISSING_REDIRECT_URI.code()).isEqualTo(7084);
            assertThat(OAuth2ErrorCode.MISSING_TOKEN_ENDPOINT.code()).isEqualTo(7085);
        }

        @Test
        @DisplayName("发现错误码 (7091-7095)")
        void testDiscoveryErrorCodes() {
            assertThat(OAuth2ErrorCode.DISCOVERY_FAILED.code()).isEqualTo(7091);
            assertThat(OAuth2ErrorCode.DISCOVERY_FAILED.message()).isEqualTo("OIDC discovery failed");
            assertThat(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE.code()).isEqualTo(7092);
            assertThat(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE.message()).isEqualTo("Invalid OIDC discovery response");
        }

        @Test
        @DisplayName("内省错误码 (7096-7100)")
        void testIntrospectionErrorCodes() {
            assertThat(OAuth2ErrorCode.INTROSPECTION_FAILED.code()).isEqualTo(7096);
            assertThat(OAuth2ErrorCode.INTROSPECTION_FAILED.message()).isEqualTo("Token introspection failed");
            assertThat(OAuth2ErrorCode.INTROSPECTION_NOT_SUPPORTED.code()).isEqualTo(7097);
            assertThat(OAuth2ErrorCode.INTROSPECTION_NOT_SUPPORTED.message()).isEqualTo("Token introspection not supported");
        }

        @Test
        @DisplayName("PAR错误码 (7101-7105)")
        void testParErrorCodes() {
            assertThat(OAuth2ErrorCode.PAR_FAILED.code()).isEqualTo(7101);
            assertThat(OAuth2ErrorCode.PAR_FAILED.message()).isEqualTo("Pushed authorization request failed");
            assertThat(OAuth2ErrorCode.PAR_NOT_SUPPORTED.code()).isEqualTo(7102);
            assertThat(OAuth2ErrorCode.PAR_NOT_SUPPORTED.message()).isEqualTo("Pushed authorization requests not supported");
        }

        @Test
        @DisplayName("颁发者错误码 (7106-7110)")
        void testIssuerErrorCodes() {
            assertThat(OAuth2ErrorCode.ISSUER_MISMATCH.code()).isEqualTo(7106);
            assertThat(OAuth2ErrorCode.ISSUER_MISMATCH.message()).isEqualTo("Authorization server issuer mismatch");
        }
    }

    @Nested
    @DisplayName("方法测试")
    class MethodTests {

        @Test
        @DisplayName("code方法")
        void testCodeMethod() {
            assertThat(OAuth2ErrorCode.TOKEN_EXPIRED.code()).isEqualTo(7001);
        }

        @Test
        @DisplayName("message方法")
        void testMessageMethod() {
            assertThat(OAuth2ErrorCode.TOKEN_EXPIRED.message()).isNotNull();
            assertThat(OAuth2ErrorCode.TOKEN_EXPIRED.message()).isNotEmpty();
            assertThat(OAuth2ErrorCode.TOKEN_EXPIRED.message()).isEqualTo("Token has expired");
        }

        @Test
        @DisplayName("toString方法")
        void testToStringMethod() {
            String str = OAuth2ErrorCode.TOKEN_EXPIRED.toString();
            assertThat(str).contains("7001");
            assertThat(str).contains("Token has expired");
        }
    }

    @Nested
    @DisplayName("枚举标准方法测试")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values方法")
        void testValues() {
            OAuth2ErrorCode[] values = OAuth2ErrorCode.values();
            assertThat(values).isNotEmpty();
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            OAuth2ErrorCode code = OAuth2ErrorCode.valueOf("TOKEN_EXPIRED");
            assertThat(code).isEqualTo(OAuth2ErrorCode.TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("valueOf新增枚举值")
        void testValueOfNewCodes() {
            assertThat(OAuth2ErrorCode.valueOf("DISCOVERY_FAILED")).isEqualTo(OAuth2ErrorCode.DISCOVERY_FAILED);
            assertThat(OAuth2ErrorCode.valueOf("DISCOVERY_INVALID_RESPONSE")).isEqualTo(OAuth2ErrorCode.DISCOVERY_INVALID_RESPONSE);
            assertThat(OAuth2ErrorCode.valueOf("INTROSPECTION_FAILED")).isEqualTo(OAuth2ErrorCode.INTROSPECTION_FAILED);
            assertThat(OAuth2ErrorCode.valueOf("INTROSPECTION_NOT_SUPPORTED")).isEqualTo(OAuth2ErrorCode.INTROSPECTION_NOT_SUPPORTED);
            assertThat(OAuth2ErrorCode.valueOf("PAR_FAILED")).isEqualTo(OAuth2ErrorCode.PAR_FAILED);
            assertThat(OAuth2ErrorCode.valueOf("PAR_NOT_SUPPORTED")).isEqualTo(OAuth2ErrorCode.PAR_NOT_SUPPORTED);
            assertThat(OAuth2ErrorCode.valueOf("ISSUER_MISMATCH")).isEqualTo(OAuth2ErrorCode.ISSUER_MISMATCH);
        }

        @Test
        @DisplayName("valueOf无效值抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> OAuth2ErrorCode.valueOf("INVALID_CODE"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("错误码唯一性测试")
    class UniqueCodeTests {

        @Test
        @DisplayName("所有错误码唯一")
        void testAllCodesUnique() {
            OAuth2ErrorCode[] values = OAuth2ErrorCode.values();
            long uniqueCount = java.util.Arrays.stream(values)
                    .map(OAuth2ErrorCode::code)
                    .distinct()
                    .count();
            assertThat(uniqueCount).isEqualTo(values.length);
        }
    }
}
