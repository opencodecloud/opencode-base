package cloud.opencode.base.crypto.envelope;

import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link HybridCrypto}.
 * HybridCrypto单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("HybridCrypto Tests / HybridCrypto测试")
class HybridCryptoTest {

    private static KeyPair rsaKeyPair;

    @BeforeAll
    static void setup() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        rsaKeyPair = keyGen.generateKeyPair();
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("rsaAes创建实例")
        void testRsaAes() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            assertThat(crypto).isNotNull();
            assertThat(crypto.getAsymmetricAlgorithm()).isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
            assertThat(crypto.getSymmetricAlgorithm()).isEqualTo(SymmetricAlgorithm.AES_GCM_256);
        }

        @Test
        @DisplayName("ecdhAes尚未实现应抛出异常")
        void testEcdhAes() {
            assertThatThrownBy(HybridCrypto::ecdhAes)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("x25519ChaCha20尚未实现应抛出异常")
        void testX25519ChaCha20() {
            assertThatThrownBy(HybridCrypto::x25519ChaCha20)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder()创建新的Builder")
        void testBuilder() {
            HybridCrypto.Builder builder = HybridCrypto.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("build()创建HybridCrypto实例")
        void testBuild() {
            HybridCrypto crypto = HybridCrypto.builder().build();

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("asymmetricAlgorithm()设置非对称算法")
        void testAsymmetricAlgorithm() {
            HybridCrypto crypto = HybridCrypto.builder()
                    .asymmetricAlgorithm(AsymmetricAlgorithm.RSA_OAEP_SHA256)
                    .build();

            assertThat(crypto.getAsymmetricAlgorithm()).isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
        }

        @Test
        @DisplayName("symmetricAlgorithm()设置对称算法")
        void testSymmetricAlgorithm() {
            HybridCrypto crypto = HybridCrypto.builder()
                    .symmetricAlgorithm(SymmetricAlgorithm.AES_GCM_128)
                    .build();

            assertThat(crypto.getSymmetricAlgorithm()).isEqualTo(SymmetricAlgorithm.AES_GCM_128);
        }

        @Test
        @DisplayName("asymmetricAlgorithm null抛出异常")
        void testAsymmetricAlgorithmNullThrows() {
            assertThatThrownBy(() -> HybridCrypto.builder().asymmetricAlgorithm(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Asymmetric algorithm");
        }

        @Test
        @DisplayName("symmetricAlgorithm null抛出异常")
        void testSymmetricAlgorithmNullThrows() {
            assertThatThrownBy(() -> HybridCrypto.builder().symmetricAlgorithm(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Symmetric algorithm");
        }

        @Test
        @DisplayName("方法链正常工作")
        void testMethodChaining() {
            HybridCrypto crypto = HybridCrypto.builder()
                    .asymmetricAlgorithm(AsymmetricAlgorithm.RSA_OAEP_SHA256)
                    .symmetricAlgorithm(SymmetricAlgorithm.AES_GCM_256)
                    .build();

            assertThat(crypto.getAsymmetricAlgorithm()).isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
            assertThat(crypto.getSymmetricAlgorithm()).isEqualTo(SymmetricAlgorithm.AES_GCM_256);
        }
    }

    @Nested
    @DisplayName("Configuration Tests / 配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("setRecipientPublicKey设置公钥")
        void testSetRecipientPublicKey() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            HybridCrypto result = crypto.setRecipientPublicKey(rsaKeyPair.getPublic());

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setRecipientPrivateKey设置私钥")
        void testSetRecipientPrivateKey() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            HybridCrypto result = crypto.setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThat(result).isSameAs(crypto); // Fluent API
        }
    }

    @Nested
    @DisplayName("Encryption Tests / 加密测试")
    class EncryptionTests {

        @Test
        @DisplayName("encrypt加密成功")
        void testEncrypt() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            byte[] plaintext = "Hello, Hybrid!".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = crypto.encrypt(plaintext);

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            assertThatThrownBy(() -> crypto.encrypt(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Plaintext");
        }

        @Test
        @DisplayName("未设置公钥encrypt抛出异常")
        void testEncryptWithoutPublicKeyThrows() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            assertThatThrownBy(() -> crypto.encrypt("test".getBytes()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("encryptBase64返回Base64字符串")
        void testEncryptBase64() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            String base64 = crypto.encryptBase64("test".getBytes());

            assertThat(base64).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = crypto.encrypt(plaintext);

            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt ciphertext为null抛出异常")
        void testDecryptNullCiphertextThrows() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decrypt(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Ciphertext");
        }

        @Test
        @DisplayName("未设置私钥decrypt抛出异常")
        void testDecryptWithoutPrivateKeyThrows() {
            HybridCrypto encryptor = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = encryptor.encrypt("test".getBytes());

            HybridCrypto decryptor = HybridCrypto.rsaAes();

            assertThatThrownBy(() -> decryptor.decrypt(encrypted))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("decryptBase64解密Base64字符串")
        void testDecryptBase64() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Base64 test".getBytes();
            String base64 = crypto.encryptBase64(plaintext);

            byte[] decrypted = crypto.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64NullThrows() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decryptBase64(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Base64");
        }

        @Test
        @DisplayName("使用错误密钥解密抛出异常")
        void testDecryptWrongKeyThrows() throws Exception {
            HybridCrypto encryptor = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = encryptor.encrypt("test".getBytes());

            // Generate different key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair wrongKeyPair = keyGen.generateKeyPair();

            HybridCrypto decryptor = HybridCrypto.rsaAes()
                    .setRecipientPrivateKey(wrongKeyPair.getPrivate());

            assertThatThrownBy(() -> decryptor.decrypt(encrypted))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整加密解密流程")
        void testEndToEnd() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "End-to-end test message".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            String plaintext = "你好世界 🎉 Special: <>&\"'";

            byte[] encrypted = crypto.encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
            String decrypted = new String(crypto.decrypt(encrypted), StandardCharsets.UTF_8);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("空字节数组")
        void testEmptyBytes() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = new byte[0];

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testLargeData() {
            HybridCrypto crypto = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = new byte[100 * 1024]; // 100 KB
            Arrays.fill(plaintext, (byte) 0x42);

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("分离的加密器和解密器")
        void testSeparateEncryptorDecryptor() {
            HybridCrypto encryptor = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            HybridCrypto decryptor = HybridCrypto.rsaAes()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Separate encryptor/decryptor test".getBytes();

            byte[] encrypted = encryptor.encrypt(plaintext);
            byte[] decrypted = decryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("通过Base64传输")
        void testBase64Transfer() {
            HybridCrypto encryptor = HybridCrypto.rsaAes()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            HybridCrypto decryptor = HybridCrypto.rsaAes()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Base64 transfer test".getBytes();

            // Encrypt and get Base64
            String base64 = encryptor.encryptBase64(plaintext);

            // Decrypt from Base64
            byte[] decrypted = decryptor.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用Builder创建的实例加密解密")
        void testWithBuilder() {
            HybridCrypto crypto = HybridCrypto.builder()
                    .asymmetricAlgorithm(AsymmetricAlgorithm.RSA_OAEP_SHA256)
                    .symmetricAlgorithm(SymmetricAlgorithm.AES_GCM_256)
                    .build()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Builder test".getBytes();

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Getter Tests / getter测试")
    class GetterTests {

        @Test
        @DisplayName("getAsymmetricAlgorithm返回正确值")
        void testGetAsymmetricAlgorithm() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            assertThat(crypto.getAsymmetricAlgorithm())
                    .isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
        }

        @Test
        @DisplayName("getSymmetricAlgorithm返回正确值")
        void testGetSymmetricAlgorithm() {
            HybridCrypto crypto = HybridCrypto.rsaAes();

            assertThat(crypto.getSymmetricAlgorithm())
                    .isEqualTo(SymmetricAlgorithm.AES_GCM_256);
        }
    }
}
