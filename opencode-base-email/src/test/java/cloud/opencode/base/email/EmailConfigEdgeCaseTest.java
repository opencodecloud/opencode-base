package cloud.opencode.base.email;

import cloud.opencode.base.email.security.DkimConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Edge case tests for EmailConfig and EmailReceiveConfig.
 * EmailConfig 和 EmailReceiveConfig 的边界情况测试。
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
@DisplayName("EmailConfig Edge Case Tests")
class EmailConfigEdgeCaseTest {

    // ========================================================================
    // EmailConfig tests
    // ========================================================================

    @Nested
    @DisplayName("OAuth2Tests")
    class OAuth2Tests {

        @Test
        @DisplayName("hasOAuth2() returns true when token is set")
        void testHasOAuth2True() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.gmail.com")
                    .oauth2Token("ya29.xxxx")
                    .build();

            assertThat(config.hasOAuth2()).isTrue();
        }

        @Test
        @DisplayName("hasOAuth2() returns false when token is null")
        void testHasOAuth2FalseNull() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.hasOAuth2()).isFalse();
        }

        @Test
        @DisplayName("hasOAuth2() returns false when token is blank")
        void testHasOAuth2FalseBlank() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .oauth2Token("   ")
                    .build();

            assertThat(config.hasOAuth2()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns true with oauth2 token only")
        void testRequiresAuthWithOAuth2Only() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.gmail.com")
                    .oauth2Token("access-token")
                    .build();

            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("requiresAuth() returns false with blank username")
        void testRequiresAuthFalseBlankUsername() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("   ")
                    .password("pass")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns false with blank password")
        void testRequiresAuthFalseBlankPassword() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("user")
                    .password("   ")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns false with null username and no oauth2")
        void testRequiresAuthFalseNullUsername() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .password("pass")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns false with null password and no oauth2")
        void testRequiresAuthFalseNullPassword() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("user")
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }
    }

    @Nested
    @DisplayName("DkimConfigTests")
    class DkimConfigTests {

        @Test
        @DisplayName("hasDkim() returns false when dkim is null")
        void testHasDkimFalse() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.hasDkim()).isFalse();
            assertThat(config.dkim()).isNull();
        }

        @Test
        @DisplayName("hasDkim() returns true when dkim is set")
        void testHasDkimTrue() throws Exception {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            PrivateKey privateKey = kpg.generateKeyPair().getPrivate();

            DkimConfig dkim = DkimConfig.of("example.com", "mail", privateKey);
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .dkim(dkim)
                    .build();

            assertThat(config.hasDkim()).isTrue();
            assertThat(config.dkim()).isNotNull();
            assertThat(config.dkim().domain()).isEqualTo("example.com");
            assertThat(config.dkim().selector()).isEqualTo("mail");
        }
    }

    @Nested
    @DisplayName("ToStringTests")
    class ToStringTests {

        @Test
        @DisplayName("toString() masks password when present")
        void testToStringMasksPassword() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .username("user")
                    .password("supersecret")
                    .build();

            String str = config.toString();
            assertThat(str).contains("password=***");
            assertThat(str).doesNotContain("supersecret");
        }

        @Test
        @DisplayName("toString() shows null for password when null")
        void testToStringPasswordNull() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            String str = config.toString();
            assertThat(str).contains("password=null");
        }

        @Test
        @DisplayName("toString() masks oauth2Token when present")
        void testToStringMasksOAuth2Token() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.gmail.com")
                    .oauth2Token("ya29.secrettoken")
                    .build();

            String str = config.toString();
            assertThat(str).contains("oauth2Token=***");
            assertThat(str).doesNotContain("ya29.secrettoken");
        }

        @Test
        @DisplayName("toString() shows null for oauth2Token when null")
        void testToStringOAuth2TokenNull() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            String str = config.toString();
            assertThat(str).contains("oauth2Token=null");
        }

        @Test
        @DisplayName("toString() includes host and port")
        void testToStringIncludesHostPort() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(465)
                    .build();

            String str = config.toString();
            assertThat(str).contains("host=smtp.example.com");
            assertThat(str).contains("port=465");
        }
    }

    @Nested
    @DisplayName("DefaultsTests")
    class DefaultsTests {

        @Test
        @DisplayName("default port is 587")
        void testDefaultPort() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.port()).isEqualTo(587);
        }

        @Test
        @DisplayName("SSL changes default port to 465")
        void testSslChangesPort() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .ssl(true)
                    .build();

            assertThat(config.port()).isEqualTo(465);
        }

        @Test
        @DisplayName("SSL does NOT change port if already set to non-587")
        void testSslDoesNotChangeCustomPort() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(2525)
                    .ssl(true)
                    .build();

            assertThat(config.port()).isEqualTo(2525);
        }

        @Test
        @DisplayName("default starttls is true")
        void testDefaultStarttls() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.starttls()).isTrue();
        }

        @Test
        @DisplayName("default timeout is 30 seconds")
        void testDefaultTimeout() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        @DisplayName("default connectionTimeout is 10 seconds")
        void testDefaultConnectionTimeout() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        @DisplayName("default maxRetries is 3")
        void testDefaultMaxRetries() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.maxRetries()).isEqualTo(3);
        }

        @Test
        @DisplayName("default poolSize is 5")
        void testDefaultPoolSize() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.poolSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("default poolIdleTimeout is 5 minutes")
        void testDefaultPoolIdleTimeout() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.poolIdleTimeout()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        @DisplayName("default debug is false")
        void testDefaultDebug() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .build();

            assertThat(config.debug()).isFalse();
        }
    }

    @Nested
    @DisplayName("BuilderAllFieldsTests")
    class BuilderAllFieldsTests {

        @Test
        @DisplayName("builder sets all fields correctly")
        void testAllBuilderFields() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .port(2525)
                    .username("admin")
                    .password("secret")
                    .oauth2Token("token123")
                    .ssl(false)
                    .starttls(false)
                    .defaultFrom("from@example.com")
                    .timeout(Duration.ofSeconds(60))
                    .connectionTimeout(Duration.ofSeconds(20))
                    .maxRetries(5)
                    .poolSize(10)
                    .poolIdleTimeout(Duration.ofMinutes(10))
                    .debug(true)
                    .build();

            assertThat(config.host()).isEqualTo("smtp.example.com");
            assertThat(config.port()).isEqualTo(2525);
            assertThat(config.username()).isEqualTo("admin");
            assertThat(config.password()).isEqualTo("secret");
            assertThat(config.oauth2Token()).isEqualTo("token123");
            assertThat(config.ssl()).isFalse();
            assertThat(config.starttls()).isFalse();
            assertThat(config.defaultFrom()).isEqualTo("from@example.com");
            assertThat(config.defaultFromName()).isNull();
            assertThat(config.timeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(config.connectionTimeout()).isEqualTo(Duration.ofSeconds(20));
            assertThat(config.maxRetries()).isEqualTo(5);
            assertThat(config.poolSize()).isEqualTo(10);
            assertThat(config.poolIdleTimeout()).isEqualTo(Duration.ofMinutes(10));
            assertThat(config.debug()).isTrue();
        }

        @Test
        @DisplayName("defaultFrom(email) without name leaves name null")
        void testDefaultFromWithoutName() {
            EmailConfig config = EmailConfig.builder()
                    .host("smtp.example.com")
                    .defaultFrom("noreply@example.com")
                    .build();

            assertThat(config.defaultFrom()).isEqualTo("noreply@example.com");
            assertThat(config.defaultFromName()).isNull();
        }
    }

    // ========================================================================
    // EmailReceiveConfig edge case tests
    // ========================================================================

    @Nested
    @DisplayName("ReceiveConfigOAuth2Tests")
    class ReceiveConfigOAuth2Tests {

        @Test
        @DisplayName("hasOAuth2() returns true with token")
        void testHasOAuth2True() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.gmail.com")
                    .oauth2Token("ya29.xxxx")
                    .imap()
                    .build();

            assertThat(config.hasOAuth2()).isTrue();
        }

        @Test
        @DisplayName("hasOAuth2() returns false with blank token")
        void testHasOAuth2FalseBlank() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .oauth2Token("   ")
                    .imap()
                    .build();

            assertThat(config.hasOAuth2()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns true with oauth2")
        void testRequiresAuthWithOAuth2() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.gmail.com")
                    .oauth2Token("token")
                    .imap()
                    .build();

            assertThat(config.requiresAuth()).isTrue();
        }

        @Test
        @DisplayName("requiresAuth() returns false with blank username and no oauth2")
        void testRequiresAuthFalseBlankUsername() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("   ")
                    .password("pass")
                    .imap()
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }

        @Test
        @DisplayName("requiresAuth() returns false with blank password and no oauth2")
        void testRequiresAuthFalseBlankPassword() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("   ")
                    .imap()
                    .build();

            assertThat(config.requiresAuth()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReceiveConfigToStringTests")
    class ReceiveConfigToStringTests {

        @Test
        @DisplayName("toString() masks password")
        void testToStringMasksPassword() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .username("user")
                    .password("supersecret")
                    .imap()
                    .build();

            String str = config.toString();
            assertThat(str).contains("password=***");
            assertThat(str).doesNotContain("supersecret");
        }

        @Test
        @DisplayName("toString() masks oauth2Token")
        void testToStringMasksOAuth2() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.gmail.com")
                    .oauth2Token("ya29.secret")
                    .imap()
                    .build();

            String str = config.toString();
            assertThat(str).contains("oauth2Token=***");
            assertThat(str).doesNotContain("ya29.secret");
        }

        @Test
        @DisplayName("toString() shows null for null password")
        void testToStringPasswordNull() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .build();

            String str = config.toString();
            assertThat(str).contains("password=null");
        }

        @Test
        @DisplayName("toString() shows null for null oauth2Token")
        void testToStringOAuth2Null() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .build();

            String str = config.toString();
            assertThat(str).contains("oauth2Token=null");
        }

        @Test
        @DisplayName("toString() includes protocol and folder")
        void testToStringIncludesProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .defaultFolder("Sent")
                    .build();

            String str = config.toString();
            assertThat(str).contains("protocol=IMAP");
            assertThat(str).contains("defaultFolder=Sent");
        }
    }

    @Nested
    @DisplayName("ReceiveConfigProtocolTests")
    class ReceiveConfigProtocolTests {

        @Test
        @DisplayName("Protocol.IMAP getName returns 'imap'")
        void testImapProtocolName() {
            assertThat(EmailReceiveConfig.Protocol.IMAP.getName()).isEqualTo("imap");
        }

        @Test
        @DisplayName("Protocol.POP3 getName returns 'pop3'")
        void testPop3ProtocolName() {
            assertThat(EmailReceiveConfig.Protocol.POP3.getName()).isEqualTo("pop3");
        }

        @Test
        @DisplayName("Protocol.IMAP default port is 143")
        void testImapDefaultPort() {
            assertThat(EmailReceiveConfig.Protocol.IMAP.getDefaultPort()).isEqualTo(143);
        }

        @Test
        @DisplayName("Protocol.IMAP default SSL port is 993")
        void testImapDefaultSslPort() {
            assertThat(EmailReceiveConfig.Protocol.IMAP.getDefaultSslPort()).isEqualTo(993);
        }

        @Test
        @DisplayName("Protocol.POP3 default port is 110")
        void testPop3DefaultPort() {
            assertThat(EmailReceiveConfig.Protocol.POP3.getDefaultPort()).isEqualTo(110);
        }

        @Test
        @DisplayName("Protocol.POP3 default SSL port is 995")
        void testPop3DefaultSslPort() {
            assertThat(EmailReceiveConfig.Protocol.POP3.getDefaultSslPort()).isEqualTo(995);
        }

        @Test
        @DisplayName("Protocol.getStoreProtocol with SSL appends 's'")
        void testGetStoreProtocolSsl() {
            assertThat(EmailReceiveConfig.Protocol.IMAP.getStoreProtocol(true)).isEqualTo("imaps");
            assertThat(EmailReceiveConfig.Protocol.POP3.getStoreProtocol(true)).isEqualTo("pop3s");
        }

        @Test
        @DisplayName("Protocol.getStoreProtocol without SSL returns plain name")
        void testGetStoreProtocolNoSsl() {
            assertThat(EmailReceiveConfig.Protocol.IMAP.getStoreProtocol(false)).isEqualTo("imap");
            assertThat(EmailReceiveConfig.Protocol.POP3.getStoreProtocol(false)).isEqualTo("pop3");
        }

        @Test
        @DisplayName("builder protocol() method sets protocol")
        void testBuilderProtocol() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("mail.example.com")
                    .protocol(EmailReceiveConfig.Protocol.POP3)
                    .build();

            assertThat(config.protocol()).isEqualTo(EmailReceiveConfig.Protocol.POP3);
            assertThat(config.isPop3()).isTrue();
        }

        @Test
        @DisplayName("POP3 without SSL uses port 110")
        void testPop3NoSslPort() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("pop.example.com")
                    .pop3()
                    .ssl(false)
                    .build();

            assertThat(config.port()).isEqualTo(110);
        }
    }

    @Nested
    @DisplayName("ReceiveConfigBuilderTests")
    class ReceiveConfigBuilderTests {

        @Test
        @DisplayName("builder sets starttls")
        void testBuilderStarttls() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .ssl(false)
                    .starttls(true)
                    .build();

            assertThat(config.starttls()).isTrue();
        }

        @Test
        @DisplayName("builder sets debug mode")
        void testBuilderDebug() {
            EmailReceiveConfig config = EmailReceiveConfig.builder()
                    .host("imap.example.com")
                    .imap()
                    .debug(true)
                    .build();

            assertThat(config.debug()).isTrue();
        }
    }
}
