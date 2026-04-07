package cloud.opencode.base.crypto.versioned;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.key.KeyGenerator;
import cloud.opencode.base.crypto.symmetric.AeadCipher;
import cloud.opencode.base.crypto.symmetric.AesGcmCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link VersionedCipher}.
 */
class VersionedCipherTest {

    private SecretKey keyV1;
    private SecretKey keyV2;

    @BeforeEach
    void setUp() {
        keyV1 = KeyGenerator.generateAesKey(256);
        keyV2 = KeyGenerator.generateAesKey(256);
    }

    private AeadCipher createCipher(SecretKey key) {
        AesGcmCipher cipher = AesGcmCipher.aes256Gcm();
        cipher.setKey(key);
        return cipher;
    }

    @Nested
    @DisplayName("Single Version Encrypt/Decrypt")
    class SingleVersionTests {

        @Test
        @DisplayName("encrypt and decrypt with single version")
        void encryptDecryptSingleVersion() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] plaintext = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = vc.encrypt(plaintext);
            byte[] decrypted = vc.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypted payload is different from plaintext")
        void encryptedDiffersFromPlaintext() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = vc.encrypt(plaintext);

            assertThat(encrypted).isNotEqualTo(plaintext);
            assertThat(encrypted.length).isGreaterThan(plaintext.length);
        }
    }

    @Nested
    @DisplayName("Multi-Version Encrypt/Decrypt")
    class MultiVersionTests {

        @Test
        @DisplayName("v1 encrypts and v1 decrypts")
        void v1EncryptV1Decrypt() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .addVersion(2, createCipher(keyV2))
                    .currentVersion(1)
                    .build();

            byte[] plaintext = "Version 1 data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = vc.encrypt(plaintext);
            byte[] decrypted = vc.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("v2 encrypts and v2 decrypts")
        void v2EncryptV2Decrypt() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .addVersion(2, createCipher(keyV2))
                    .currentVersion(2)
                    .build();

            byte[] plaintext = "Version 2 data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = vc.encrypt(plaintext);
            byte[] decrypted = vc.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Version Migration")
    class VersionMigrationTests {

        @Test
        @DisplayName("v1 encrypted data can be decrypted after upgrading to v2")
        void v1EncryptedDecryptableAfterUpgrade() {
            // Encrypt with v1 as current
            VersionedCipher vcV1 = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] plaintext = "Migrate me".getBytes(StandardCharsets.UTF_8);
            byte[] encryptedV1 = vcV1.encrypt(plaintext);

            // Build new cipher with v2 as current, but v1 still registered
            VersionedCipher vcV2 = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .addVersion(2, createCipher(keyV2))
                    .currentVersion(2)
                    .build();

            // Old v1 ciphertext should still decrypt
            byte[] decrypted = vcV2.decrypt(encryptedV1);
            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("new encryptions use current version after migration")
        void newEncryptionsUseCurrentVersion() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .addVersion(2, createCipher(keyV2))
                    .currentVersion(2)
                    .build();

            byte[] plaintext = "New data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = vc.encrypt(plaintext);

            // Deserialize to check version
            VersionedPayload payload = VersionedPayload.deserialize(encrypted);
            assertThat(payload.version()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Unknown Version Decrypt")
    class UnknownVersionTests {

        @Test
        @DisplayName("decrypting unknown version throws OpenCryptoException")
        void unknownVersionThrows() {
            VersionedCipher vcV1 = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] encrypted = vcV1.encrypt("test".getBytes(StandardCharsets.UTF_8));

            // Build cipher without v1
            VersionedCipher vcV2 = VersionedCipher.builder()
                    .addVersion(2, createCipher(keyV2))
                    .currentVersion(2)
                    .build();

            assertThatThrownBy(() -> vcV2.decrypt(encrypted))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Unknown cipher version");
        }
    }

    @Nested
    @DisplayName("Base64 Variants")
    class Base64Tests {

        @Test
        @DisplayName("encryptBase64 bytes and decryptBase64")
        void base64BytesRoundTrip() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] plaintext = "Base64 test".getBytes(StandardCharsets.UTF_8);
            String base64 = vc.encryptBase64(plaintext);
            byte[] decrypted = vc.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encryptBase64 string and decryptBase64ToString")
        void base64StringRoundTrip() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            String original = "Hello Base64 String";
            String encrypted = vc.encryptBase64(original);
            String decrypted = vc.decryptBase64ToString(encrypted);

            assertThat(decrypted).isEqualTo(original);
        }

        @Test
        @DisplayName("base64 output is valid base64")
        void base64OutputIsValid() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            String base64 = vc.encryptBase64("test");
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("Builder Validation")
    class BuilderValidationTests {

        @Test
        @DisplayName("build without any version throws")
        void buildWithoutVersionThrows() {
            assertThatThrownBy(() -> VersionedCipher.builder().build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("At least one version");
        }

        @Test
        @DisplayName("build without currentVersion set throws")
        void buildWithoutCurrentVersionThrows() {
            assertThatThrownBy(() -> VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("currentVersion");
        }

        @Test
        @DisplayName("currentVersion must be registered first")
        void currentVersionMustBeRegistered() {
            assertThatThrownBy(() -> VersionedCipher.builder()
                    .currentVersion(1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be registered");
        }

        @Test
        @DisplayName("duplicate version registration throws")
        void duplicateVersionThrows() {
            assertThatThrownBy(() -> VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .addVersion(1, createCipher(keyV2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("version out of range throws")
        void versionOutOfRange() {
            assertThatThrownBy(() -> VersionedCipher.builder()
                    .addVersion(256, createCipher(keyV1)))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> VersionedCipher.builder()
                    .addVersion(-1, createCipher(keyV1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null cipher throws NullPointerException")
        void nullCipherThrows() {
            assertThatThrownBy(() -> VersionedCipher.builder()
                    .addVersion(1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null plaintext throws NullPointerException")
        void nullPlaintextThrows() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            assertThatThrownBy(() -> vc.encrypt(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Tamper Detection")
    class TamperDetectionTests {

        @Test
        @DisplayName("tampered ciphertext throws OpenCryptoException")
        void tamperedCiphertextThrows() {
            VersionedCipher vc = VersionedCipher.builder()
                    .addVersion(1, createCipher(keyV1))
                    .currentVersion(1)
                    .build();

            byte[] encrypted = vc.encrypt("sensitive data".getBytes(StandardCharsets.UTF_8));
            encrypted[encrypted.length - 1] ^= 0xFF;

            assertThatThrownBy(() -> vc.decrypt(encrypted))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }
}
