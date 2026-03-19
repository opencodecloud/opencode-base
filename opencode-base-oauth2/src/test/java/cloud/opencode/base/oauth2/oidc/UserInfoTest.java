package cloud.opencode.base.oauth2.oidc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserInfoTest Tests
 * UserInfoTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("UserInfo 测试")
class UserInfoTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用builder构建")
        void testBuilder() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .name("John Doe")
                    .email("john@example.com")
                    .build();

            assertThat(userInfo.sub()).isEqualTo("user123");
            assertThat(userInfo.name()).isEqualTo("John Doe");
            assertThat(userInfo.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("使用所有字段构建")
        void testBuilderAllFields() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .name("John Doe")
                    .givenName("John")
                    .familyName("Doe")
                    .middleName("Middle")
                    .nickname("johnny")
                    .preferredUsername("johnd")
                    .profile("https://example.com/john")
                    .picture("https://example.com/john.jpg")
                    .website("https://johndoe.com")
                    .email("john@example.com")
                    .emailVerified(true)
                    .gender("male")
                    .birthdate("1990-01-01")
                    .zoneinfo("America/New_York")
                    .locale("en-US")
                    .phoneNumber("+1234567890")
                    .phoneNumberVerified(true)
                    .build();

            assertThat(userInfo.sub()).isEqualTo("user123");
            assertThat(userInfo.givenName()).isEqualTo("John");
            assertThat(userInfo.familyName()).isEqualTo("Doe");
            assertThat(userInfo.middleName()).isEqualTo("Middle");
            assertThat(userInfo.nickname()).isEqualTo("johnny");
            assertThat(userInfo.preferredUsername()).isEqualTo("johnd");
            assertThat(userInfo.profile()).isEqualTo("https://example.com/john");
            assertThat(userInfo.picture()).isEqualTo("https://example.com/john.jpg");
            assertThat(userInfo.website()).isEqualTo("https://johndoe.com");
            assertThat(userInfo.emailVerified()).isTrue();
            assertThat(userInfo.gender()).isEqualTo("male");
            assertThat(userInfo.birthdate()).isEqualTo("1990-01-01");
            assertThat(userInfo.zoneinfo()).isEqualTo("America/New_York");
            assertThat(userInfo.locale()).isEqualTo("en-US");
            assertThat(userInfo.phoneNumber()).isEqualTo("+1234567890");
            assertThat(userInfo.phoneNumberVerified()).isTrue();
        }

        @Test
        @DisplayName("添加自定义claim")
        void testBuilderCustomClaim() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .claim("custom_field", "custom_value")
                    .build();

            Optional<Object> claim = userInfo.getClaim("custom_field");
            assertThat(claim).isPresent();
            assertThat(claim.get()).isEqualTo("custom_value");
        }
    }

    @Nested
    @DisplayName("getClaim方法测试")
    class GetClaimTests {

        @Test
        @DisplayName("getClaim返回自定义claim")
        void testGetClaim() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .claims(Map.of("custom", "value"))
                    .build();

            assertThat(userInfo.getClaim("custom")).isPresent();
            assertThat(userInfo.getClaim("custom").get()).isEqualTo("value");
        }

        @Test
        @DisplayName("getClaim不存在返回empty")
        void testGetClaimNotFound() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .build();

            assertThat(userInfo.getClaim("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("getClaimAsString")
        void testGetClaimAsString() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .claims(Map.of("number", 42))
                    .build();

            Optional<String> claim = userInfo.getClaimAsString("number");
            assertThat(claim).isPresent();
            assertThat(claim.get()).isEqualTo("42");
        }
    }

    @Nested
    @DisplayName("验证方法测试")
    class VerificationMethodsTests {

        @Test
        @DisplayName("isEmailVerified - true")
        void testIsEmailVerifiedTrue() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .emailVerified(true)
                    .build();

            assertThat(userInfo.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("isEmailVerified - false")
        void testIsEmailVerifiedFalse() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .emailVerified(false)
                    .build();

            assertThat(userInfo.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("isEmailVerified - null")
        void testIsEmailVerifiedNull() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .build();

            assertThat(userInfo.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("isPhoneNumberVerified - true")
        void testIsPhoneNumberVerifiedTrue() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .phoneNumberVerified(true)
                    .build();

            assertThat(userInfo.isPhoneNumberVerified()).isTrue();
        }

        @Test
        @DisplayName("isPhoneNumberVerified - false")
        void testIsPhoneNumberVerifiedFalse() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .phoneNumberVerified(false)
                    .build();

            assertThat(userInfo.isPhoneNumberVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("displayName方法测试")
    class DisplayNameTests {

        @Test
        @DisplayName("displayName优先使用name")
        void testDisplayNameUsesName() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .name("John Doe")
                    .preferredUsername("johnd")
                    .email("john@example.com")
                    .build();

            assertThat(userInfo.displayName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("displayName其次使用preferredUsername")
        void testDisplayNameUsesPreferredUsername() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .preferredUsername("johnd")
                    .email("john@example.com")
                    .build();

            assertThat(userInfo.displayName()).isEqualTo("johnd");
        }

        @Test
        @DisplayName("displayName再次使用email的用户名部分")
        void testDisplayNameUsesEmail() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .email("john@example.com")
                    .build();

            assertThat(userInfo.displayName()).isEqualTo("john");
        }

        @Test
        @DisplayName("displayName最后使用sub")
        void testDisplayNameUsesSub() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .build();

            assertThat(userInfo.displayName()).isEqualTo("user123");
        }
    }

    @Nested
    @DisplayName("fromJson方法测试")
    class FromJsonTests {

        @Test
        @DisplayName("fromJson解析基本字段")
        void testFromJsonBasicFields() {
            String json = """
                    {
                        "sub": "user123",
                        "name": "John Doe",
                        "email": "john@example.com"
                    }
                    """;

            UserInfo userInfo = UserInfo.fromJson(json);

            assertThat(userInfo.sub()).isEqualTo("user123");
            assertThat(userInfo.name()).isEqualTo("John Doe");
            assertThat(userInfo.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("fromJson解析布尔值")
        void testFromJsonBooleanFields() {
            String json = """
                    {
                        "sub": "user123",
                        "email_verified": true,
                        "phone_number_verified": false
                    }
                    """;

            UserInfo userInfo = UserInfo.fromJson(json);

            assertThat(userInfo.emailVerified()).isTrue();
            assertThat(userInfo.phoneNumberVerified()).isFalse();
        }

        @Test
        @DisplayName("fromJson解析空JSON")
        void testFromJsonEmpty() {
            String json = "{}";
            UserInfo userInfo = UserInfo.fromJson(json);

            assertThat(userInfo.sub()).isNull();
        }

        @Test
        @DisplayName("fromJson解析null")
        void testFromJsonNull() {
            UserInfo userInfo = UserInfo.fromJson(null);
            assertThat(userInfo.sub()).isNull();
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("claims是不可变的")
        void testClaimsImmutable() {
            UserInfo userInfo = UserInfo.builder()
                    .sub("user123")
                    .claims(Map.of("key", "value"))
                    .build();

            Map<String, Object> claims = userInfo.claims();
            assertThatThrownBy(() -> claims.put("new", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
