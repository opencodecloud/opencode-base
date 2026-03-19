package cloud.opencode.base.oauth2.grant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * DeviceCodeResponseTest Tests
 * DeviceCodeResponseTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("DeviceCodeResponse 测试")
class DeviceCodeResponseTest {

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用必需字段构建")
        void testBuildWithRequiredFields() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .build();

            assertThat(response.deviceCode()).isEqualTo("device123");
            assertThat(response.userCode()).isEqualTo("USER-CODE");
            assertThat(response.verificationUri()).isEqualTo("https://example.com/device");
        }

        @Test
        @DisplayName("使用所有字段构建")
        void testBuildWithAllFields() {
            Instant now = Instant.now();

            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .verificationUriComplete("https://example.com/device?user_code=USER-CODE")
                    .expiresIn(600)
                    .interval(5)
                    .createdAt(now)
                    .build();

            assertThat(response.deviceCode()).isEqualTo("device123");
            assertThat(response.userCode()).isEqualTo("USER-CODE");
            assertThat(response.verificationUri()).isEqualTo("https://example.com/device");
            assertThat(response.verificationUriComplete()).isEqualTo("https://example.com/device?user_code=USER-CODE");
            assertThat(response.expiresIn()).isEqualTo(600);
            assertThat(response.interval()).isEqualTo(5);
            assertThat(response.createdAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("默认interval为5")
        void testDefaultInterval() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .build();

            assertThat(response.interval()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("过期方法测试")
    class ExpirationTests {

        @Test
        @DisplayName("isExpired - 已过期")
        void testIsExpiredTrue() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .expiresIn(60)
                    .createdAt(Instant.now().minusSeconds(120))
                    .build();

            assertThat(response.isExpired()).isTrue();
        }

        @Test
        @DisplayName("isExpired - 未过期")
        void testIsExpiredFalse() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .expiresIn(600)
                    .createdAt(Instant.now())
                    .build();

            assertThat(response.isExpired()).isFalse();
        }

        @Test
        @DisplayName("expiresAt计算正确")
        void testExpiresAt() {
            Instant now = Instant.now();
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .expiresIn(600)
                    .createdAt(now)
                    .build();

            Instant expected = now.plusSeconds(600);
            assertThat(response.expiresAt()).isEqualTo(expected);
        }

        @Test
        @DisplayName("remainingSeconds计算正确")
        void testRemainingSeconds() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .expiresIn(600)
                    .createdAt(Instant.now())
                    .build();

            long remaining = response.remainingSeconds();
            assertThat(remaining).isGreaterThan(500);
            assertThat(remaining).isLessThanOrEqualTo(600);
        }
    }

    @Nested
    @DisplayName("验证URI方法测试")
    class VerificationUriTests {

        @Test
        @DisplayName("hasVerificationUriComplete - 有")
        void testHasVerificationUriCompleteTrue() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .verificationUriComplete("https://example.com/device?user_code=USER-CODE")
                    .build();

            assertThat(response.hasVerificationUriComplete()).isTrue();
        }

        @Test
        @DisplayName("hasVerificationUriComplete - 无")
        void testHasVerificationUriCompleteFalse() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .build();

            assertThat(response.hasVerificationUriComplete()).isFalse();
        }

        @Test
        @DisplayName("getBestVerificationUri - 有complete")
        void testGetBestVerificationUriWithComplete() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .verificationUriComplete("https://example.com/device?user_code=USER-CODE")
                    .build();

            assertThat(response.getBestVerificationUri())
                    .isEqualTo("https://example.com/device?user_code=USER-CODE");
        }

        @Test
        @DisplayName("getBestVerificationUri - 无complete")
        void testGetBestVerificationUriWithoutComplete() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .build();

            assertThat(response.getBestVerificationUri())
                    .isEqualTo("https://example.com/device");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            Instant now = Instant.now();
            DeviceCodeResponse response1 = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .createdAt(now)
                    .build();
            DeviceCodeResponse response2 = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .createdAt(now)
                    .build();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            DeviceCodeResponse response = DeviceCodeResponse.builder()
                    .deviceCode("device123")
                    .userCode("USER-CODE")
                    .verificationUri("https://example.com/device")
                    .build();

            assertThat(response.toString()).contains("device123");
            assertThat(response.toString()).contains("USER-CODE");
        }
    }
}
