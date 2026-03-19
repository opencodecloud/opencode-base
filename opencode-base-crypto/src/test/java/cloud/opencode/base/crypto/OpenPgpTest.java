package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.pgp.PgpAlgorithm;
import cloud.opencode.base.crypto.pgp.PgpCipher;
import cloud.opencode.base.crypto.pgp.PgpKeyPair;
import cloud.opencode.base.crypto.pgp.PgpKeyUtil;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link OpenPgp}.
 * OpenPgp单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenPgp Tests / OpenPgp测试")
class OpenPgpTest {

    private static PgpKeyPair testKeyPair;
    private static String armoredPublicKey;
    private static String armoredSecretKey;
    private static final String TEST_USER_ID = "facade@example.com";
    private static final String TEST_PASSPHRASE = "facadePassphrase123!";

    @BeforeAll
    static void setup() {
        testKeyPair = OpenPgp.generateKeyPair(TEST_USER_ID, TEST_PASSPHRASE, 2048);
        armoredPublicKey = OpenPgp.exportPublicKey(testKeyPair);
        armoredSecretKey = OpenPgp.exportSecretKey(testKeyPair);
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair生成密钥对")
        void testGenerateKeyPair() {
            PgpKeyPair keyPair = OpenPgp.generateKeyPair("test@test.com", "password");

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.publicKey()).isNotNull();
            assertThat(keyPair.secretKey()).isNotNull();
            assertThat(keyPair.userId()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("generateKeyPair使用指定大小")
        void testGenerateKeyPairWithSize() {
            PgpKeyPair keyPair = OpenPgp.generateKeyPair("sized@test.com", "password", 2048);

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.canEncrypt()).isTrue();
            assertThat(keyPair.canSign()).isTrue();
        }
    }

    @Nested
    @DisplayName("Encryption Tests (String) / 字符串加密测试")
    class EncryptionStringTests {

        @Test
        @DisplayName("encrypt(String, PGPPublicKey)加密成功")
        void testEncryptWithPublicKey() {
            String plaintext = "Test message";

            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair.publicKey());

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
            assertThat(encrypted).contains("-----END PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encrypt(String, String)使用armored公钥加密")
        void testEncryptWithArmoredPublicKey() {
            String plaintext = "Test with armored key";

            String encrypted = OpenPgp.encrypt(plaintext, armoredPublicKey);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encrypt(String, PgpKeyPair)使用密钥对加密")
        void testEncryptWithKeyPair() {
            String plaintext = "Test with key pair";

            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encrypt plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            assertThatThrownBy(() -> OpenPgp.encrypt((String) null, testKeyPair.publicKey()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("plaintext");
        }

        @Test
        @DisplayName("encrypt publicKey为null抛出异常")
        void testEncryptNullPublicKeyThrows() {
            assertThatThrownBy(() -> OpenPgp.encrypt("test", (PGPPublicKey) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("publicKey");
        }

        @Test
        @DisplayName("encrypt armoredPublicKey为null抛出异常")
        void testEncryptNullArmoredPublicKeyThrows() {
            assertThatThrownBy(() -> OpenPgp.encrypt("test", (String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("armoredPublicKey");
        }

        @Test
        @DisplayName("encrypt keyPair为null抛出异常")
        void testEncryptNullKeyPairThrows() {
            assertThatThrownBy(() -> OpenPgp.encrypt("test", (PgpKeyPair) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyPair");
        }
    }

    @Nested
    @DisplayName("Encryption Tests (Bytes) / 字节加密测试")
    class EncryptionBytesTests {

        @Test
        @DisplayName("encrypt(byte[], PGPPublicKey)加密字节数组")
        void testEncryptBytesWithPublicKey() {
            byte[] data = "Binary data".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = OpenPgp.encrypt(data, testKeyPair.publicKey());

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted.length).isGreaterThan(data.length);
        }

        @Test
        @DisplayName("encryptArmored(byte[], PGPPublicKey)加密为armored")
        void testEncryptArmoredBytesWithPublicKey() {
            byte[] data = "Binary data armored".getBytes(StandardCharsets.UTF_8);

            String encrypted = OpenPgp.encryptArmored(data, testKeyPair.publicKey());

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encrypt data为null抛出异常")
        void testEncryptNullDataThrows() {
            assertThatThrownBy(() -> OpenPgp.encrypt((byte[]) null, testKeyPair.publicKey()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("data");
        }

        @Test
        @DisplayName("encryptArmored data为null抛出异常")
        void testEncryptArmoredNullDataThrows() {
            assertThatThrownBy(() -> OpenPgp.encryptArmored(null, testKeyPair.publicKey()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("data");
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decrypt(String, PGPSecretKey, passphrase)解密成功")
        void testDecryptWithSecretKey() {
            String plaintext = "Decrypt with secret key";
            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair.publicKey());

            String decrypted = OpenPgp.decrypt(encrypted, testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt(String, String, passphrase)使用armored私钥解密")
        void testDecryptWithArmoredSecretKey() {
            String plaintext = "Decrypt with armored key";
            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair.publicKey());

            String decrypted = OpenPgp.decrypt(encrypted, armoredSecretKey, TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt(String, PgpKeyPair, passphrase)使用密钥对解密")
        void testDecryptWithKeyPair() {
            String plaintext = "Decrypt with key pair";
            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair);

            String decrypted = OpenPgp.decrypt(encrypted, testKeyPair, TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt(byte[], PGPSecretKey, passphrase)解密字节数组")
        void testDecryptBytesWithSecretKey() {
            byte[] data = "Binary decrypt test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = OpenPgp.encrypt(data, testKeyPair.publicKey());

            byte[] decrypted = OpenPgp.decrypt(encrypted, testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(data);
        }

        @Test
        @DisplayName("decrypt armoredMessage为null抛出异常")
        void testDecryptNullArmoredMessageThrows() {
            assertThatThrownBy(() -> OpenPgp.decrypt((String) null, testKeyPair.secretKey(), TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("armoredMessage");
        }

        @Test
        @DisplayName("decrypt secretKey为null抛出异常")
        void testDecryptNullSecretKeyThrows() {
            assertThatThrownBy(() -> OpenPgp.decrypt("test", (PGPSecretKey) null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }

        @Test
        @DisplayName("decrypt passphrase为null抛出异常")
        void testDecryptNullPassphraseThrows() {
            assertThatThrownBy(() -> OpenPgp.decrypt("test", testKeyPair.secretKey(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("passphrase");
        }

        @Test
        @DisplayName("decrypt keyPair为null抛出异常")
        void testDecryptNullKeyPairThrows() {
            assertThatThrownBy(() -> OpenPgp.decrypt("test", (PgpKeyPair) null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyPair");
        }

        @Test
        @DisplayName("decrypt encryptedData为null抛出异常")
        void testDecryptNullEncryptedDataThrows() {
            assertThatThrownBy(() -> OpenPgp.decrypt((byte[]) null, testKeyPair.secretKey(), TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("encryptedData");
        }
    }

    @Nested
    @DisplayName("Export Key Tests / 导出密钥测试")
    class ExportKeyTests {

        @Test
        @DisplayName("exportPublicKey(PgpKeyPair)导出公钥")
        void testExportPublicKeyFromKeyPair() {
            String armored = OpenPgp.exportPublicKey(testKeyPair);

            assertThat(armored).isNotNull();
            assertThat(armored).contains("-----BEGIN PGP PUBLIC KEY BLOCK-----");
            assertThat(armored).contains("-----END PGP PUBLIC KEY BLOCK-----");
        }

        @Test
        @DisplayName("exportPublicKey(PGPPublicKey)导出公钥")
        void testExportPublicKeyFromPublicKey() {
            String armored = OpenPgp.exportPublicKey(testKeyPair.publicKey());

            assertThat(armored).isNotNull();
            assertThat(armored).contains("-----BEGIN PGP PUBLIC KEY BLOCK-----");
        }

        @Test
        @DisplayName("exportSecretKey(PgpKeyPair)导出私钥")
        void testExportSecretKeyFromKeyPair() {
            String armored = OpenPgp.exportSecretKey(testKeyPair);

            assertThat(armored).isNotNull();
            assertThat(armored).contains("-----BEGIN PGP PRIVATE KEY BLOCK-----");
            assertThat(armored).contains("-----END PGP PRIVATE KEY BLOCK-----");
        }

        @Test
        @DisplayName("exportSecretKey(PGPSecretKey)导出私钥")
        void testExportSecretKeyFromSecretKey() {
            String armored = OpenPgp.exportSecretKey(testKeyPair.secretKey());

            assertThat(armored).isNotNull();
            assertThat(armored).contains("-----BEGIN PGP PRIVATE KEY BLOCK-----");
        }

        @Test
        @DisplayName("exportPublicKey keyPair为null抛出异常")
        void testExportPublicKeyNullKeyPairThrows() {
            assertThatThrownBy(() -> OpenPgp.exportPublicKey((PgpKeyPair) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyPair");
        }

        @Test
        @DisplayName("exportSecretKey keyPair为null抛出异常")
        void testExportSecretKeyNullKeyPairThrows() {
            assertThatThrownBy(() -> OpenPgp.exportSecretKey((PgpKeyPair) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyPair");
        }
    }

    @Nested
    @DisplayName("Import Key Tests / 导入密钥测试")
    class ImportKeyTests {

        @Test
        @DisplayName("importPublicKey导入公钥成功")
        void testImportPublicKey() {
            PGPPublicKey publicKey = OpenPgp.importPublicKey(armoredPublicKey);

            assertThat(publicKey).isNotNull();
            assertThat(publicKey.getKeyID()).isEqualTo(testKeyPair.publicKey().getKeyID());
        }

        @Test
        @DisplayName("importKeyPair导入密钥对成功")
        void testImportKeyPair() {
            PgpKeyPair keyPair = OpenPgp.importKeyPair(armoredSecretKey, TEST_PASSPHRASE);

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.keyId()).isEqualTo(testKeyPair.keyId());
        }
    }

    @Nested
    @DisplayName("Key Information Tests / 密钥信息测试")
    class KeyInformationTests {

        @Test
        @DisplayName("keyIdHex返回十六进制密钥ID")
        void testKeyIdHex() {
            String hex = OpenPgp.keyIdHex(testKeyPair.publicKey());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9A-F]+");
        }

        @Test
        @DisplayName("fingerprintHex返回十六进制指纹")
        void testFingerprintHex() {
            String hex = OpenPgp.fingerprintHex(testKeyPair.publicKey());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9A-F]+");
            assertThat(hex.length()).isGreaterThanOrEqualTo(32);
        }
    }

    @Nested
    @DisplayName("Cipher Builder Tests / 加密器构建器测试")
    class CipherBuilderTests {

        @Test
        @DisplayName("cipher()返回新的PgpCipher实例")
        void testCipher() {
            PgpCipher cipher = OpenPgp.cipher();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("cipher()可以配置和使用")
        void testCipherConfigurable() {
            String plaintext = "Cipher builder test";

            String encrypted = OpenPgp.cipher()
                    .withPublicKey(testKeyPair.publicKey())
                    .withSymmetricAlgorithm(PgpAlgorithm.Symmetric.AES_128)
                    .encryptArmored(plaintext);

            String decrypted = OpenPgp.cipher()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests / 工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("私有构造函数抛出AssertionError")
        void testPrivateConstructorThrowsAssertionError() throws Exception {
            Constructor<OpenPgp> constructor = OpenPgp.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整流程: 生成密钥、加密、解密")
        void testCompleteFlow() {
            // Generate key pair
            PgpKeyPair keyPair = OpenPgp.generateKeyPair("e2e@test.com", "e2ePassword", 2048);

            // Encrypt
            String plaintext = "Complete end-to-end test message";
            String encrypted = OpenPgp.encrypt(plaintext, keyPair);

            // Decrypt
            String decrypted = OpenPgp.decrypt(encrypted, keyPair, "e2ePassword");

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整流程: 导出公钥、分享、加密、解密")
        void testSharePublicKeyFlow() {
            // Alice generates key pair
            PgpKeyPair aliceKeyPair = OpenPgp.generateKeyPair("alice@test.com", "alicePassword", 2048);

            // Alice exports public key for sharing
            String alicePublicKeyArmored = OpenPgp.exportPublicKey(aliceKeyPair);

            // Bob imports Alice's public key
            PGPPublicKey alicePublicKey = OpenPgp.importPublicKey(alicePublicKeyArmored);

            // Bob encrypts message for Alice
            String message = "Hello Alice, this is Bob!";
            String encrypted = OpenPgp.encrypt(message, alicePublicKey);

            // Alice decrypts message
            String decrypted = OpenPgp.decrypt(encrypted, aliceKeyPair, "alicePassword");

            assertThat(decrypted).isEqualTo(message);
        }

        @Test
        @DisplayName("中文和特殊字符加密解密")
        void testUnicodeAndSpecialChars() {
            String plaintext = "Hello 你好 مرحبا 🎉 Special: <>&\"'";

            String encrypted = OpenPgp.encrypt(plaintext, testKeyPair);
            String decrypted = OpenPgp.decrypt(encrypted, testKeyPair, TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("二进制数据加密解密")
        void testBinaryData() {
            byte[] data = new byte[1024];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            byte[] encrypted = OpenPgp.encrypt(data, testKeyPair.publicKey());
            byte[] decrypted = OpenPgp.decrypt(encrypted, testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(decrypted).isEqualTo(data);
        }

        @Test
        @DisplayName("导出导入密钥对后仍可使用")
        void testExportImportKeyPairStillWorks() {
            // Generate and export
            PgpKeyPair originalKeyPair = OpenPgp.generateKeyPair("export@test.com", "exportPass", 2048);
            String armoredSecretKey = OpenPgp.exportSecretKey(originalKeyPair);

            // Import
            PgpKeyPair importedKeyPair = OpenPgp.importKeyPair(armoredSecretKey, "exportPass");

            // Use imported key pair
            String plaintext = "Test with imported key pair";
            String encrypted = OpenPgp.encrypt(plaintext, importedKeyPair);
            String decrypted = OpenPgp.decrypt(encrypted, importedKeyPair, "exportPass");

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
