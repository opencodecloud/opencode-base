package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.ConfigBuilder;
import cloud.opencode.base.config.OpenConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for EncryptedConfigProcessor.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("EncryptedConfigProcessor Tests")
class EncryptedConfigProcessorTest {

    private SecretKey secretKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        secretKey = keyGen.generateKey();
    }

    @Nested
    @DisplayName("Plain Value Passthrough Tests")
    class PlainValueTests {

        @Test
        @DisplayName("getString returns plain value unchanged")
        void testPlainValuePassthrough() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("plain.key", "plain-value"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(encrypted.getString("plain.key")).isEqualTo("plain-value");
        }

        @Test
        @DisplayName("getString with default returns plain value")
        void testPlainValueDefaultPassthrough() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(encrypted.getString("key", "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("getString with default returns default for missing key")
        void testMissingKeyDefault() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("other", "value"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(encrypted.getString("missing", "fallback")).isEqualTo("fallback");
        }
    }

    @Nested
    @DisplayName("Encrypted Value Tests")
    class EncryptedValueTests {

        @Test
        @DisplayName("invalid encrypted value throws exception")
        void testInvalidEncryptedValue() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("encrypted.key", "ENC(not-valid-base64!!!)"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThatThrownBy(() -> encrypted.getString("encrypted.key"))
                    .isInstanceOf(OpenConfigException.class);
        }

        @Test
        @DisplayName("value not matching ENC() pattern passes through")
        void testNonEncPattern() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("key", "ENC_not_encrypted"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(encrypted.getString("key")).isEqualTo("ENC_not_encrypted");
        }

        @Test
        @DisplayName("value starting with ENC( but not ending with ) passes through")
        void testPartialEncPattern() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("key", "ENC(something"))
                    .disablePlaceholders()
                    .build();

            Config encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(encrypted.getString("key")).isEqualTo("ENC(something");
        }
    }

    @Nested
    @DisplayName("Delegate Method Tests")
    class DelegateMethodTests {

        private Config encrypted;

        @BeforeEach
        void setUp() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of(
                            "int.key", "42",
                            "long.key", "999",
                            "double.key", "3.14",
                            "bool.key", "true",
                            "duration.key", "30s",
                            "prefix.sub", "subval"
                    ))
                    .disablePlaceholders()
                    .build();
            encrypted = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
        }

        @Test
        @DisplayName("getInt delegates to source")
        void testGetInt() {
            assertThat(encrypted.getInt("int.key")).isEqualTo(42);
        }

        @Test
        @DisplayName("getInt with default delegates to source")
        void testGetIntDefault() {
            assertThat(encrypted.getInt("missing", 99)).isEqualTo(99);
        }

        @Test
        @DisplayName("getLong delegates to source")
        void testGetLong() {
            assertThat(encrypted.getLong("long.key")).isEqualTo(999L);
        }

        @Test
        @DisplayName("getLong with default delegates to source")
        void testGetLongDefault() {
            assertThat(encrypted.getLong("missing", 0L)).isEqualTo(0L);
        }

        @Test
        @DisplayName("getDouble delegates to source")
        void testGetDouble() {
            assertThat(encrypted.getDouble("double.key")).isEqualTo(3.14);
        }

        @Test
        @DisplayName("getDouble with default delegates to source")
        void testGetDoubleDefault() {
            assertThat(encrypted.getDouble("missing", 1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getBoolean delegates to source")
        void testGetBoolean() {
            assertThat(encrypted.getBoolean("bool.key")).isTrue();
        }

        @Test
        @DisplayName("getBoolean with default delegates to source")
        void testGetBooleanDefault() {
            assertThat(encrypted.getBoolean("missing", false)).isFalse();
        }

        @Test
        @DisplayName("hasKey delegates to source")
        void testHasKey() {
            assertThat(encrypted.hasKey("int.key")).isTrue();
            assertThat(encrypted.hasKey("missing")).isFalse();
        }

        @Test
        @DisplayName("getKeys delegates to source")
        void testGetKeys() {
            assertThat(encrypted.getKeys()).contains("int.key", "long.key");
        }

        @Test
        @DisplayName("getByPrefix delegates to source")
        void testGetByPrefix() {
            assertThat(encrypted.getByPrefix("prefix.")).containsEntry("prefix.sub", "subval");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("createEncryptedConfig returns a Config")
        void testCreateReturnsConfig() {
            Config source = new ConfigBuilder()
                    .addProperties(Map.of("key", "value"))
                    .build();
            Config result = EncryptedConfigProcessor.createEncryptedConfig(source, secretKey);
            assertThat(result).isNotNull().isInstanceOf(Config.class);
        }
    }
}
