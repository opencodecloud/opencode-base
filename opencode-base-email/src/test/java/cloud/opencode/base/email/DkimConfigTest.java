package cloud.opencode.base.email;

import cloud.opencode.base.email.security.DkimConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DkimConfig
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@DisplayName("DkimConfig")
class DkimConfigTest {

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("should create config with of() method")
        void shouldCreateConfigWithOf() throws Exception {
            PrivateKey key = generateTestKey();

            DkimConfig config = DkimConfig.of("example.com", "mail", key);

            assertThat(config.domain()).isEqualTo("example.com");
            assertThat(config.selector()).isEqualTo("mail");
            assertThat(config.privateKey()).isEqualTo(key);
            assertThat(config.headersToSign()).isEqualTo(DkimConfig.getDefaultHeadersToSign());
        }

        @Test
        @DisplayName("should create config with custom headers")
        void shouldCreateConfigWithCustomHeaders() throws Exception {
            PrivateKey key = generateTestKey();
            Set<String> customHeaders = Set.of("From", "To", "Subject");

            DkimConfig config = DkimConfig.of("example.com", "mail", key, customHeaders);

            assertThat(config.headersToSign()).isEqualTo(customHeaders);
        }
    }

    @Nested
    @DisplayName("Default headers")
    class DefaultHeaders {

        @Test
        @DisplayName("should have standard headers to sign")
        void shouldHaveStandardHeaders() {
            Set<String> defaults = DkimConfig.getDefaultHeadersToSign();

            assertThat(defaults).contains("From", "To", "Subject", "Date", "Message-ID");
        }
    }

    private PrivateKey generateTestKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        return keyPair.getPrivate();
    }
}
