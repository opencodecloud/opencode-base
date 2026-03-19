package cloud.opencode.base.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EmailReceiveConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("EmailReceiveConfig")
class EmailReceiveConfigTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("should build IMAP config with defaults")
        void shouldBuildImapConfigWithDefaults() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user@example.com")
                    .password("password")
                    .imap()
                    .build();

            assertThat(config.host()).isEqualTo("imap.example.com");
            assertThat(config.username()).isEqualTo("user@example.com");
            assertThat(config.password()).isEqualTo("password");
            assertThat(config.protocol()).isEqualTo(EmailReceiveConfig.Protocol.IMAP);
            assertThat(config.port()).isEqualTo(993); // SSL port
            assertThat(config.ssl()).isTrue();
            assertThat(config.defaultFolder()).isEqualTo("INBOX");
        }

        @Test
        @DisplayName("should build POP3 config")
        void shouldBuildPop3Config() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .username("user@example.com")
                    .password("password")
                    .pop3()
                    .ssl(true)
                    .build();

            assertThat(config.protocol()).isEqualTo(EmailReceiveConfig.Protocol.POP3);
            assertThat(config.port()).isEqualTo(995); // POP3 SSL port
            assertThat(config.isPop3()).isTrue();
            assertThat(config.isImap()).isFalse();
        }

        @Test
        @DisplayName("should use custom port when specified")
        void shouldUseCustomPort() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .port(1993)
                    .imap()
                    .build();

            assertThat(config.port()).isEqualTo(1993);
        }

        @Test
        @DisplayName("should use non-SSL port when SSL disabled")
        void shouldUseNonSslPort() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .ssl(false)
                    .build();

            assertThat(config.port()).isEqualTo(143);
        }

        @Test
        @DisplayName("should set timeout values")
        void shouldSetTimeoutValues() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .timeout(Duration.ofSeconds(60))
                    .connectionTimeout(Duration.ofSeconds(20))
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(20));
        }

        @Test
        @DisplayName("should set receive options")
        void shouldSetReceiveOptions() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .maxMessages(50)
                    .deleteAfterReceive(true)
                    .markAsReadAfterReceive(false)
                    .build();

            assertThat(config.maxMessages()).isEqualTo(50);
            assertThat(config.deleteAfterReceive()).isTrue();
            assertThat(config.markAsReadAfterReceive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Protocol")
    class ProtocolTest {

        @Test
        @DisplayName("should get correct store protocol for IMAP")
        void shouldGetCorrectStoreProtocolForImap() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .ssl(true)
                    .build();

            assertThat(config.getStoreProtocol()).isEqualTo("imaps");
        }

        @Test
        @DisplayName("should get correct store protocol for POP3")
        void shouldGetCorrectStoreProtocolForPop3() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .pop3()
                    .ssl(true)
                    .build();

            assertThat(config.getStoreProtocol()).isEqualTo("pop3s");
        }

        @Test
        @DisplayName("should get non-SSL protocol name")
        void shouldGetNonSslProtocolName() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .ssl(false)
                    .build();

            assertThat(config.getStoreProtocol()).isEqualTo("imap");
        }
    }

    @Nested
    @DisplayName("Authentication")
    class Authentication {

        @Test
        @DisplayName("should require auth when username and password set")
        void shouldRequireAuthWhenUsernameAndPasswordSet() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("pass")
                    .build();

            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("should not require auth when no credentials")
        void shouldNotRequireAuthWhenNoCredentials() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("should support OAuth2 token")
        void shouldSupportOAuth2Token() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.gmail.com")
                    .username("user@gmail.com")
                    .oauth2Token("access-token")
                    .imap()
                    .build();

            assertThat(config.hasOAuth2()).isTrue();
            assertThat(config.requiresAuth()).isTrue();
            assertThat(config.oauth2Token()).isEqualTo("access-token");
        }
    }
}
