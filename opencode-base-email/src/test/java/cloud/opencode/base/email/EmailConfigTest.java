package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailConfig")
class EmailConfigTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should build with required fields")
        void shouldBuildWithRequiredFields() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(587)
                    .username("user@example.com")
                    .password("password")
                    .build();

            assertThat(config.host()).isEqualTo("smtp.example.com");
            assertThat(config.port()).isEqualTo(587);
            assertThat(config.username()).isEqualTo("user@example.com");
            assertThat(config.password()).isEqualTo("password");
        }

        @Test
        @DisplayName("should have default values")
        void shouldHaveDefaultValues() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.port()).isEqualTo(587);
            assertThat(config.ssl()).isFalse();
            assertThat(config.starttls()).isTrue();
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(10));
            assertThat(config.maxRetries()).isEqualTo(3);
            assertThat(config.debug()).isFalse();
        }

        @Test
        @DisplayName("should set SSL and change port to 465")
        void shouldSetSslAndChangePort() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .ssl(true)
                    .build();

            assertThat(config.ssl()).isTrue();
            assertThat(config.port()).isEqualTo(465);
        }

        @Test
        @DisplayName("should set default from with name")
        void shouldSetDefaultFromWithName() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .defaultFrom("noreply@example.com", "System")
                    .build();

            assertThat(config.defaultFrom()).isEqualTo("noreply@example.com");
            assertThat(config.defaultFromName()).isEqualTo("System");
        }
    }

    @Nested
    @DisplayName("Authentication")
    class Authentication {

        @Test
        @DisplayName("should require auth when username and password set")
        void shouldRequireAuthWhenUsernameAndPasswordSet() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("user")
                    .password("pass")
                    .build();

            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("should not require auth when no credentials")
        void shouldNotRequireAuthWhenNoCredentials() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("should require auth when OAuth2 token set")
        void shouldRequireAuthWhenOAuth2Set() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("user@gmail.com")
                    .oauth2Token("access-token")
                    .build();

            assertThat(config.requiresAuth()).isTrue();
            assertThat(config.hasOAuth2()).isTrue();
        }
    }

    @Nested
    @DisplayName("DKIM")
    class DkimSupport {

        @Test
        @DisplayName("should not have DKIM by default")
        void shouldNotHaveDkimByDefault() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.hasDkim()).isFalse();
            assertThat(config.dkim()).isNull();
        }
    }
}
