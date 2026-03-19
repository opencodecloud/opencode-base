package cloud.opencode.base.crypto.pgp;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PgpKeyUtil}.
 * PgpKeyUtil单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PgpKeyUtil Tests / PgpKeyUtil测试")
class PgpKeyUtilTest {

    private static PgpKeyPair testKeyPair;
    private static String armoredPublicKey;
    private static String armoredSecretKey;
    private static final String TEST_USER_ID = "test@example.com";
    private static final String TEST_PASSPHRASE = "testPassphrase123!";

    @BeforeAll
    static void setup() {
        testKeyPair = PgpKeyUtil.generateKeyPair(TEST_USER_ID, TEST_PASSPHRASE, 2048);
        armoredPublicKey = PgpKeyUtil.exportPublicKey(testKeyPair.publicKey());
        armoredSecretKey = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("生成密钥对成功")
        void testGenerateKeyPair() {
            PgpKeyPair keyPair = PgpKeyUtil.generateKeyPair("gen@test.com", "password");

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.publicKey()).isNotNull();
            assertThat(keyPair.secretKey()).isNotNull();
            assertThat(keyPair.userId()).isEqualTo("gen@test.com");
            assertThat(keyPair.keyId()).isNotEqualTo(0L);
        }

        @Test
        @DisplayName("使用默认密钥大小生成密钥对")
        void testGenerateKeyPairDefaultSize() {
            PgpKeyPair keyPair = PgpKeyUtil.generateKeyPair("default@test.com", "password");

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.canEncrypt()).isTrue();
            assertThat(keyPair.canSign()).isTrue();
        }

        @Test
        @DisplayName("使用指定密钥大小生成密钥对")
        void testGenerateKeyPairWithSize() {
            PgpKeyPair keyPair = PgpKeyUtil.generateKeyPair("sized@test.com", "password", 2048);

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.canEncrypt()).isTrue();
        }

        @Test
        @DisplayName("密钥大小小于2048抛出异常")
        void testGenerateKeyPairTooSmallSizeThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.generateKeyPair("small@test.com", "password", 1024))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2048");
        }

        @Test
        @DisplayName("userId为null抛出异常")
        void testGenerateKeyPairNullUserIdThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.generateKeyPair(null, "password"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("userId");
        }

        @Test
        @DisplayName("passphrase为null抛出异常")
        void testGenerateKeyPairNullPassphraseThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.generateKeyPair("test@test.com", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("passphrase");
        }
    }

    @Nested
    @DisplayName("Public Key Import Tests / 公钥导入测试")
    class PublicKeyImportTests {

        @Test
        @DisplayName("从armored字符串导入公钥成功")
        void testImportPublicKeyFromString() {
            PGPPublicKey publicKey = PgpKeyUtil.importPublicKey(armoredPublicKey);

            assertThat(publicKey).isNotNull();
            assertThat(publicKey.isEncryptionKey()).isTrue();
        }

        @Test
        @DisplayName("从字节数组导入公钥成功")
        void testImportPublicKeyFromBytes() {
            byte[] keyBytes = armoredPublicKey.getBytes();

            PGPPublicKey publicKey = PgpKeyUtil.importPublicKey(keyBytes);

            assertThat(publicKey).isNotNull();
            assertThat(publicKey.isEncryptionKey()).isTrue();
        }

        @Test
        @DisplayName("armoredKey为null抛出异常")
        void testImportPublicKeyNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.importPublicKey((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("armoredKey");
        }

        @Test
        @DisplayName("无效armored格式抛出异常")
        void testImportPublicKeyInvalidFormatThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.importPublicKey("invalid-key-data"))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("Secret Key Import Tests / 私钥导入测试")
    class SecretKeyImportTests {

        @Test
        @DisplayName("从armored字符串导入私钥成功")
        void testImportSecretKeyFromString() {
            PGPSecretKey secretKey = PgpKeyUtil.importSecretKey(armoredSecretKey, TEST_PASSPHRASE);

            assertThat(secretKey).isNotNull();
            assertThat(secretKey.isSigningKey()).isTrue();
        }

        @Test
        @DisplayName("armoredKey为null抛出异常")
        void testImportSecretKeyNullKeyThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.importSecretKey(null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("armoredKey");
        }

        @Test
        @DisplayName("passphrase为null抛出异常")
        void testImportSecretKeyNullPassphraseThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.importSecretKey(armoredSecretKey, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("passphrase");
        }

        @Test
        @DisplayName("错误passphrase抛出异常")
        void testImportSecretKeyWrongPassphraseThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.importSecretKey(armoredSecretKey, "wrongPassword"))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("passphrase");
        }
    }

    @Nested
    @DisplayName("Key Pair Import Tests / 密钥对导入测试")
    class KeyPairImportTests {

        @Test
        @DisplayName("从armored私钥导入密钥对成功")
        void testImportKeyPair() {
            PgpKeyPair keyPair = PgpKeyUtil.importKeyPair(armoredSecretKey, TEST_PASSPHRASE);

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.publicKey()).isNotNull();
            assertThat(keyPair.secretKey()).isNotNull();
            assertThat(keyPair.keyId()).isEqualTo(testKeyPair.keyId());
        }
    }

    @Nested
    @DisplayName("Public Key Export Tests / 公钥导出测试")
    class PublicKeyExportTests {

        @Test
        @DisplayName("导出公钥为armored字符串")
        void testExportPublicKey() {
            String armored = PgpKeyUtil.exportPublicKey(testKeyPair.publicKey());

            assertThat(armored).isNotNull().isNotEmpty();
            assertThat(armored).contains("-----BEGIN PGP PUBLIC KEY BLOCK-----");
            assertThat(armored).contains("-----END PGP PUBLIC KEY BLOCK-----");
        }

        @Test
        @DisplayName("publicKey为null抛出异常")
        void testExportPublicKeyNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.exportPublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("publicKey");
        }

        @Test
        @DisplayName("导出公钥为字节数组")
        void testExportPublicKeyBytes() {
            byte[] bytes = PgpKeyUtil.exportPublicKeyBytes(testKeyPair.publicKey());

            assertThat(bytes).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("exportPublicKeyBytes为null抛出异常")
        void testExportPublicKeyBytesNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.exportPublicKeyBytes(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("publicKey");
        }
    }

    @Nested
    @DisplayName("Secret Key Export Tests / 私钥导出测试")
    class SecretKeyExportTests {

        @Test
        @DisplayName("导出私钥为armored字符串")
        void testExportSecretKey() {
            String armored = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());

            assertThat(armored).isNotNull().isNotEmpty();
            assertThat(armored).contains("-----BEGIN PGP PRIVATE KEY BLOCK-----");
            assertThat(armored).contains("-----END PGP PRIVATE KEY BLOCK-----");
        }

        @Test
        @DisplayName("secretKey为null抛出异常")
        void testExportSecretKeyNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.exportSecretKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }
    }

    @Nested
    @DisplayName("Extract Private Key Tests / 提取私钥测试")
    class ExtractPrivateKeyTests {

        @Test
        @DisplayName("成功提取私钥")
        void testExtractPrivateKeySuccess() {
            PGPPrivateKey privateKey = PgpKeyUtil.extractPrivateKey(testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(privateKey).isNotNull();
            assertThat(privateKey.getKeyID()).isEqualTo(testKeyPair.keyId());
        }

        @Test
        @DisplayName("secretKey为null抛出异常")
        void testExtractPrivateKeyNullSecretKeyThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.extractPrivateKey(null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }

        @Test
        @DisplayName("passphrase为null抛出异常")
        void testExtractPrivateKeyNullPassphraseThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.extractPrivateKey(testKeyPair.secretKey(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("passphrase");
        }

        @Test
        @DisplayName("错误passphrase抛出异常")
        void testExtractPrivateKeyWrongPassphraseThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.extractPrivateKey(testKeyPair.secretKey(), "wrongPassword"))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("passphrase");
        }
    }

    @Nested
    @DisplayName("Key ID Hex Tests / 密钥ID十六进制测试")
    class KeyIdHexTests {

        @Test
        @DisplayName("返回大写十六进制密钥ID")
        void testKeyIdHex() {
            String hex = PgpKeyUtil.keyIdHex(testKeyPair.publicKey());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9A-F]+");
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testKeyIdHexNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.keyIdHex(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }
    }

    @Nested
    @DisplayName("Fingerprint Hex Tests / 指纹十六进制测试")
    class FingerprintHexTests {

        @Test
        @DisplayName("返回大写十六进制指纹")
        void testFingerprintHex() {
            String hex = PgpKeyUtil.fingerprintHex(testKeyPair.publicKey());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9A-F]+");
            // Fingerprints are typically 40 characters (20 bytes) for SHA-1
            assertThat(hex.length()).isGreaterThanOrEqualTo(32);
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testFingerprintHexNullThrows() {
            assertThatThrownBy(() -> PgpKeyUtil.fingerprintHex(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }
    }

    @Nested
    @DisplayName("Utility Class Tests / 工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("私有构造函数抛出AssertionError")
        void testPrivateConstructorThrowsAssertionError() throws Exception {
            Constructor<PgpKeyUtil> constructor = PgpKeyUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Round-Trip Tests / 往返测试")
    class RoundTripTests {

        @Test
        @DisplayName("公钥导出导入往返成功")
        void testPublicKeyRoundTrip() {
            String exported = PgpKeyUtil.exportPublicKey(testKeyPair.publicKey());
            PGPPublicKey imported = PgpKeyUtil.importPublicKey(exported);

            assertThat(imported.getKeyID()).isEqualTo(testKeyPair.publicKey().getKeyID());
            assertThat(PgpKeyUtil.fingerprintHex(imported))
                    .isEqualTo(PgpKeyUtil.fingerprintHex(testKeyPair.publicKey()));
        }

        @Test
        @DisplayName("私钥导出导入往返成功")
        void testSecretKeyRoundTrip() {
            String exported = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());
            PGPSecretKey imported = PgpKeyUtil.importSecretKey(exported, TEST_PASSPHRASE);

            assertThat(imported.getKeyID()).isEqualTo(testKeyPair.secretKey().getKeyID());
        }

        @Test
        @DisplayName("密钥对导出导入往返成功")
        void testKeyPairRoundTrip() {
            String exported = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());
            PgpKeyPair imported = PgpKeyUtil.importKeyPair(exported, TEST_PASSPHRASE);

            assertThat(imported.keyId()).isEqualTo(testKeyPair.keyId());
            assertThat(imported.canEncrypt()).isEqualTo(testKeyPair.canEncrypt());
            assertThat(imported.canSign()).isEqualTo(testKeyPair.canSign());
        }
    }

    @Nested
    @DisplayName("Integration Tests / 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("新生成的密钥对可以用于加密解密")
        void testGeneratedKeyPairWorksForEncryption() {
            PgpKeyPair keyPair = PgpKeyUtil.generateKeyPair("integration@test.com", "integrationPass", 2048);
            String plaintext = "Integration test message";

            String encrypted = PgpCipher.create()
                    .withPublicKey(keyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(keyPair, "integrationPass")
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("导入的公钥可以用于加密")
        void testImportedPublicKeyCanEncrypt() {
            PGPPublicKey importedPublicKey = PgpKeyUtil.importPublicKey(armoredPublicKey);
            String plaintext = "Test with imported key";

            String encrypted = PgpCipher.create()
                    .withPublicKey(importedPublicKey)
                    .encryptArmored(plaintext);

            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");

            // Decrypt with original key pair
            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
