package cloud.opencode.base.oauth2.grant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GrantTypeTest Tests
 * GrantTypeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("GrantType 测试")
class GrantTypeTest {

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("AUTHORIZATION_CODE")
        void testAuthorizationCode() {
            assertThat(GrantType.AUTHORIZATION_CODE.value()).isEqualTo("authorization_code");
        }

        @Test
        @DisplayName("CLIENT_CREDENTIALS")
        void testClientCredentials() {
            assertThat(GrantType.CLIENT_CREDENTIALS.value()).isEqualTo("client_credentials");
        }

        @Test
        @DisplayName("DEVICE_CODE")
        void testDeviceCode() {
            assertThat(GrantType.DEVICE_CODE.value()).isEqualTo("urn:ietf:params:oauth:grant-type:device_code");
        }

        @Test
        @DisplayName("REFRESH_TOKEN")
        void testRefreshToken() {
            assertThat(GrantType.REFRESH_TOKEN.value()).isEqualTo("refresh_token");
        }
    }

    @Nested
    @DisplayName("fromValue方法测试")
    class FromValueTests {

        @Test
        @DisplayName("fromValue - authorization_code")
        void testFromValueAuthorizationCode() {
            GrantType result = GrantType.fromValue("authorization_code");
            assertThat(result).isEqualTo(GrantType.AUTHORIZATION_CODE);
        }

        @Test
        @DisplayName("fromValue - client_credentials")
        void testFromValueClientCredentials() {
            GrantType result = GrantType.fromValue("client_credentials");
            assertThat(result).isEqualTo(GrantType.CLIENT_CREDENTIALS);
        }

        @Test
        @DisplayName("fromValue - device_code URN")
        void testFromValueDeviceCode() {
            GrantType result = GrantType.fromValue("urn:ietf:params:oauth:grant-type:device_code");
            assertThat(result).isEqualTo(GrantType.DEVICE_CODE);
        }

        @Test
        @DisplayName("fromValue - refresh_token")
        void testFromValueRefreshToken() {
            GrantType result = GrantType.fromValue("refresh_token");
            assertThat(result).isEqualTo(GrantType.REFRESH_TOKEN);
        }

        @Test
        @DisplayName("fromValue - 无效值抛出异常")
        void testFromValueInvalid() {
            assertThatThrownBy(() -> GrantType.fromValue("invalid_grant_type"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown grant type");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回value")
        void testToString() {
            assertThat(GrantType.AUTHORIZATION_CODE.toString()).isEqualTo("authorization_code");
            assertThat(GrantType.CLIENT_CREDENTIALS.toString()).isEqualTo("client_credentials");
            assertThat(GrantType.DEVICE_CODE.toString()).isEqualTo("urn:ietf:params:oauth:grant-type:device_code");
            assertThat(GrantType.REFRESH_TOKEN.toString()).isEqualTo("refresh_token");
        }
    }

    @Nested
    @DisplayName("枚举标准方法测试")
    class EnumStandardMethodsTests {

        @Test
        @DisplayName("values方法")
        void testValues() {
            GrantType[] values = GrantType.values();
            assertThat(values).hasSize(4);
            assertThat(values).contains(
                    GrantType.AUTHORIZATION_CODE,
                    GrantType.CLIENT_CREDENTIALS,
                    GrantType.DEVICE_CODE,
                    GrantType.REFRESH_TOKEN
            );
        }

        @Test
        @DisplayName("valueOf方法")
        void testValueOf() {
            assertThat(GrantType.valueOf("AUTHORIZATION_CODE")).isEqualTo(GrantType.AUTHORIZATION_CODE);
            assertThat(GrantType.valueOf("CLIENT_CREDENTIALS")).isEqualTo(GrantType.CLIENT_CREDENTIALS);
            assertThat(GrantType.valueOf("DEVICE_CODE")).isEqualTo(GrantType.DEVICE_CODE);
            assertThat(GrantType.valueOf("REFRESH_TOKEN")).isEqualTo(GrantType.REFRESH_TOKEN);
        }

        @Test
        @DisplayName("valueOf无效值抛出异常")
        void testValueOfInvalid() {
            assertThatThrownBy(() -> GrantType.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
