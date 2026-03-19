package cloud.opencode.base.crypto.pgp;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PgpCipher}.
 * PgpCipher单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PgpCipher Tests / PgpCipher测试")
class PgpCipherTest {

    private static PgpKeyPair testKeyPair;
    private static String armoredPublicKey;
    private static String armoredSecretKey;
    private static final String TEST_USER_ID = "cipher@example.com";
    private static final String TEST_PASSPHRASE = "cipherPassphrase123!";

    @BeforeAll
    static void setup() {
        testKeyPair = PgpKeyUtil.generateKeyPair(TEST_USER_ID, TEST_PASSPHRASE, 2048);
        armoredPublicKey = PgpKeyUtil.exportPublicKey(testKeyPair.publicKey());
        armoredSecretKey = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create()创建新的PgpCipher实例")
        void testCreate() {
            PgpCipher cipher = PgpCipher.create();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("create()每次返回新实例")
        void testCreateReturnsNewInstance() {
            PgpCipher cipher1 = PgpCipher.create();
            PgpCipher cipher2 = PgpCipher.create();

            assertThat(cipher1).isNotSameAs(cipher2);
        }
    }

    @Nested
    @DisplayName("Configuration Tests / 配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("withPublicKey(PGPPublicKey)设置公钥")
        void testWithPublicKeyObject() {
            PgpCipher cipher = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey());

            assertThat(cipher.getPublicKey()).isEqualTo(testKeyPair.publicKey());
        }

        @Test
        @DisplayName("withPublicKey(String)设置armored公钥")
        void testWithPublicKeyString() {
            PgpCipher cipher = PgpCipher.create()
                    .withPublicKey(armoredPublicKey);

            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPublicKey().getKeyID())
                    .isEqualTo(testKeyPair.publicKey().getKeyID());
        }

        @Test
        @DisplayName("withPublicKey null抛出异常")
        void testWithPublicKeyNullThrows() {
            assertThatThrownBy(() -> PgpCipher.create().withPublicKey((PGPPublicKey) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("publicKey");
        }

        @Test
        @DisplayName("withSecretKey(PGPSecretKey, passphrase)设置私钥")
        void testWithSecretKeyObject() {
            PgpCipher cipher = PgpCipher.create()
                    .withSecretKey(testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(cipher.getSecretKey()).isEqualTo(testKeyPair.secretKey());
        }

        @Test
        @DisplayName("withSecretKey(String, passphrase)设置armored私钥")
        void testWithSecretKeyString() {
            PgpCipher cipher = PgpCipher.create()
                    .withSecretKey(armoredSecretKey, TEST_PASSPHRASE);

            assertThat(cipher.getSecretKey()).isNotNull();
            assertThat(cipher.getSecretKey().getKeyID())
                    .isEqualTo(testKeyPair.secretKey().getKeyID());
        }

        @Test
        @DisplayName("withSecretKey null抛出异常")
        void testWithSecretKeyNullThrows() {
            assertThatThrownBy(() -> PgpCipher.create().withSecretKey((PGPSecretKey) null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }

        @Test
        @DisplayName("withKeyPair设置密钥对")
        void testWithKeyPair() {
            PgpCipher cipher = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE);

            assertThat(cipher.getPublicKey()).isEqualTo(testKeyPair.publicKey());
            assertThat(cipher.getSecretKey()).isEqualTo(testKeyPair.secretKey());
        }

        @Test
        @DisplayName("withKeyPair null抛出异常")
        void testWithKeyPairNullThrows() {
            assertThatThrownBy(() -> PgpCipher.create().withKeyPair(null, TEST_PASSPHRASE))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("keyPair");
        }

        @Test
        @DisplayName("withSymmetricAlgorithm设置对称算法")
        void testWithSymmetricAlgorithm() {
            PgpCipher cipher = PgpCipher.create()
                    .withSymmetricAlgorithm(PgpAlgorithm.Symmetric.AES_128);

            assertThat(cipher.getSymmetricAlgorithm()).isEqualTo(PgpAlgorithm.Symmetric.AES_128);
        }

        @Test
        @DisplayName("withSymmetricAlgorithm null抛出异常")
        void testWithSymmetricAlgorithmNullThrows() {
            assertThatThrownBy(() -> PgpCipher.create().withSymmetricAlgorithm(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("algorithm");
        }

        @Test
        @DisplayName("默认对称算法为AES_256")
        void testDefaultSymmetricAlgorithm() {
            PgpCipher cipher = PgpCipher.create();

            assertThat(cipher.getSymmetricAlgorithm()).isEqualTo(PgpAlgorithm.Symmetric.AES_256);
        }

        @Test
        @DisplayName("withIntegrityCheck设置完整性检查")
        void testWithIntegrityCheck() {
            PgpCipher cipher = PgpCipher.create()
                    .withIntegrityCheck(false);

            // Verify by encrypting/decrypting
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("withCompression设置压缩")
        void testWithCompression() {
            PgpCipher cipher = PgpCipher.create()
                    .withCompression(false);

            // Verify by encrypting/decrypting
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("方法链正常工作")
        void testMethodChaining() {
            PgpCipher cipher = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .withSymmetricAlgorithm(PgpAlgorithm.Symmetric.AES_128)
                    .withIntegrityCheck(true)
                    .withCompression(true);

            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getSymmetricAlgorithm()).isEqualTo(PgpAlgorithm.Symmetric.AES_128);
        }
    }

    @Nested
    @DisplayName("Encryption Tests / 加密测试")
    class EncryptionTests {

        @Test
        @DisplayName("encryptArmored(String)加密字符串")
        void testEncryptArmoredString() {
            String plaintext = "Hello, PGP!";

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
            assertThat(encrypted).contains("-----END PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encryptArmored(byte[])加密字节数组")
        void testEncryptArmoredBytes() {
            byte[] data = "Binary data test".getBytes(StandardCharsets.UTF_8);

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(data);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
        }

        @Test
        @DisplayName("encrypt(byte[])加密为原始字节")
        void testEncryptBytes() {
            byte[] data = "Raw bytes test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encrypt(data);

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted.length).isGreaterThan(data.length);
        }

        @Test
        @DisplayName("encryptBase64加密为Base64字符串")
        void testEncryptBase64() {
            byte[] data = "Base64 test".getBytes(StandardCharsets.UTF_8);

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptBase64(data);

            assertThat(encrypted).isNotNull().isNotEmpty();
            // Should be valid base64
            assertThatCode(() -> Base64.getDecoder().decode(encrypted)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("未设置公钥加密抛出异常")
        void testEncryptWithoutPublicKeyThrows() {
            assertThatThrownBy(() -> PgpCipher.create().encryptArmored("test"))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("plaintext");
        }

        @Test
        @DisplayName("data为null抛出异常")
        void testEncryptNullDataThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("data");
        }

        @Test
        @DisplayName("使用不同对称算法加密")
        void testEncryptWithDifferentAlgorithms() {
            String plaintext = "Algorithm test";

            for (PgpAlgorithm.Symmetric alg : PgpAlgorithm.Symmetric.values()) {
                String encrypted = PgpCipher.create()
                        .withPublicKey(testKeyPair.publicKey())
                        .withSymmetricAlgorithm(alg)
                        .encryptArmored(plaintext);

                assertThat(encrypted).isNotNull();
                assertThat(encrypted).contains("-----BEGIN PGP MESSAGE-----");
            }
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decryptArmored解密armored消息")
        void testDecryptArmored() {
            String plaintext = "Decrypt armored test";
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptArmoredToBytes解密为字节数组")
        void testDecryptArmoredToBytes() {
            String plaintext = "Decrypt to bytes test";
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            byte[] decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmoredToBytes(encrypted);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt(byte[])解密原始字节")
        void testDecryptBytes() {
            byte[] data = "Raw bytes decrypt test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encrypt(data);

            byte[] decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decrypt(encrypted);

            assertThat(decrypted).isEqualTo(data);
        }

        @Test
        @DisplayName("decryptBase64解密Base64编码数据")
        void testDecryptBase64() {
            byte[] data = "Base64 decrypt test".getBytes(StandardCharsets.UTF_8);
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptBase64(data);

            byte[] decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptBase64(encrypted);

            assertThat(decrypted).isEqualTo(data);
        }

        @Test
        @DisplayName("decryptBase64ToString解密Base64为字符串")
        void testDecryptBase64ToString() {
            byte[] data = "Base64 to string test".getBytes(StandardCharsets.UTF_8);
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptBase64(data);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptBase64ToString(encrypted);

            assertThat(decrypted).isEqualTo("Base64 to string test");
        }

        @Test
        @DisplayName("未设置私钥解密抛出异常")
        void testDecryptWithoutSecretKeyThrows() {
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored("test");

            assertThatThrownBy(() -> PgpCipher.create().decryptArmored(encrypted))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("armoredData为null抛出异常")
        void testDecryptNullArmoredDataThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("armoredData");
        }

        @Test
        @DisplayName("encryptedData为null抛出异常")
        void testDecryptNullEncryptedDataThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("encryptedData");
        }

        @Test
        @DisplayName("base64Data为null抛出异常")
        void testDecryptBase64NullThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptBase64(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("base64Data");
        }

        @Test
        @DisplayName("无效armored数据抛出异常")
        void testDecryptInvalidArmoredDataThrows() {
            assertThatThrownBy(() -> PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored("invalid armored data"))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("使用错误密钥解密抛出异常")
        void testDecryptWithWrongKeyThrows() {
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored("test");

            // Create a different key pair
            PgpKeyPair differentKeyPair = PgpKeyUtil.generateKeyPair("different@test.com", "differentPass", 2048);

            assertThatThrownBy(() -> PgpCipher.create()
                    .withKeyPair(differentKeyPair, "differentPass")
                    .decryptArmored(encrypted))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("No matching key");
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整加密解密流程 - 字符串")
        void testEndToEndString() {
            String plaintext = "End-to-end test message with special chars: 你好世界!";

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withSecretKey(testKeyPair.secretKey(), TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整加密解密流程 - 字节数组")
        void testEndToEndBytes() {
            byte[] data = new byte[256];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) i;
            }

            byte[] encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encrypt(data);

            byte[] decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decrypt(encrypted);

            assertThat(decrypted).isEqualTo(data);
        }

        @Test
        @DisplayName("不同对称算法加密解密")
        void testEndToEndDifferentAlgorithms() {
            String plaintext = "Algorithm test message";

            for (PgpAlgorithm.Symmetric alg : PgpAlgorithm.Symmetric.values()) {
                String encrypted = PgpCipher.create()
                        .withPublicKey(testKeyPair.publicKey())
                        .withSymmetricAlgorithm(alg)
                        .encryptArmored(plaintext);

                String decrypted = PgpCipher.create()
                        .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                        .decryptArmored(encrypted);

                assertThat(decrypted)
                        .as("Algorithm %s should work correctly", alg.algorithmName())
                        .isEqualTo(plaintext);
            }
        }

        @Test
        @DisplayName("禁用压缩加密解密")
        void testEndToEndNoCompression() {
            String plaintext = "No compression test";

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .withCompression(false)
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用armored公钥字符串加密")
        void testEndToEndWithArmoredPublicKey() {
            String plaintext = "Armored key test";

            String encrypted = PgpCipher.create()
                    .withPublicKey(armoredPublicKey)
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用armored私钥字符串解密")
        void testEndToEndWithArmoredSecretKey() {
            String plaintext = "Armored secret key test";

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withSecretKey(armoredSecretKey, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testEndToEndLargeData() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                sb.append("Line ").append(i).append(": Large data test content.\n");
            }
            String plaintext = sb.toString();

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("空字符串加密解密")
        void testEndToEndEmptyString() {
            String plaintext = "";

            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Getter Tests / getter测试")
    class GetterTests {

        @Test
        @DisplayName("getPublicKey返回设置的公钥")
        void testGetPublicKey() {
            PgpCipher cipher = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey());

            assertThat(cipher.getPublicKey()).isEqualTo(testKeyPair.publicKey());
        }

        @Test
        @DisplayName("getPublicKey未设置时返回null")
        void testGetPublicKeyWhenNotSet() {
            PgpCipher cipher = PgpCipher.create();

            assertThat(cipher.getPublicKey()).isNull();
        }

        @Test
        @DisplayName("getSecretKey返回设置的私钥")
        void testGetSecretKey() {
            PgpCipher cipher = PgpCipher.create()
                    .withSecretKey(testKeyPair.secretKey(), TEST_PASSPHRASE);

            assertThat(cipher.getSecretKey()).isEqualTo(testKeyPair.secretKey());
        }

        @Test
        @DisplayName("getSecretKey未设置时返回null")
        void testGetSecretKeyWhenNotSet() {
            PgpCipher cipher = PgpCipher.create();

            assertThat(cipher.getSecretKey()).isNull();
        }

        @Test
        @DisplayName("getSymmetricAlgorithm返回设置的算法")
        void testGetSymmetricAlgorithm() {
            PgpCipher cipher = PgpCipher.create()
                    .withSymmetricAlgorithm(PgpAlgorithm.Symmetric.TWOFISH);

            assertThat(cipher.getSymmetricAlgorithm()).isEqualTo(PgpAlgorithm.Symmetric.TWOFISH);
        }
    }
}
