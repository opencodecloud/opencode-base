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
 * Unit tests for {@link EnvelopeCrypto}.
 * EnvelopeCrypto单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EnvelopeCrypto Tests / EnvelopeCrypto测试")
class EnvelopeCryptoTest {

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
        @DisplayName("rsaAesGcm创建实例")
        void testRsaAesGcm() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThat(crypto).isNotNull();
            assertThat(crypto.getAsymmetricAlgorithm()).isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
            assertThat(crypto.getSymmetricAlgorithm()).isEqualTo(SymmetricAlgorithm.AES_GCM_256);
        }

        @Test
        @DisplayName("ecdhAesGcm throws UnsupportedOperationException (not yet implemented)")
        void testEcdhAesGcm() {
            assertThatThrownBy(EnvelopeCrypto::ecdhAesGcm)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("ECDH key agreement not yet implemented");
        }

        @Test
        @DisplayName("x25519ChaCha20 throws UnsupportedOperationException (not yet implemented)")
        void testX25519ChaCha20() {
            assertThatThrownBy(EnvelopeCrypto::x25519ChaCha20)
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("X25519 key agreement not yet implemented");
        }
    }

    @Nested
    @DisplayName("Configuration Tests / 配置测试")
    class ConfigurationTests {

        @Test
        @DisplayName("setRecipientPublicKey设置公钥")
        void testSetRecipientPublicKey() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            EnvelopeCrypto result = crypto.setRecipientPublicKey(rsaKeyPair.getPublic());

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setRecipientPublicKey null抛出异常")
        void testSetRecipientPublicKeyNullThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThatThrownBy(() -> crypto.setRecipientPublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("setRecipientPrivateKey设置私钥")
        void testSetRecipientPrivateKey() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            EnvelopeCrypto result = crypto.setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setRecipientPrivateKey null抛出异常")
        void testSetRecipientPrivateKeyNullThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThatThrownBy(() -> crypto.setRecipientPrivateKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Private key");
        }
    }

    @Nested
    @DisplayName("Encryption Tests / 加密测试")
    class EncryptionTests {

        @Test
        @DisplayName("encrypt加密成功")
        void testEncrypt() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            byte[] plaintext = "Hello, Envelope!".getBytes(StandardCharsets.UTF_8);

            EncryptedEnvelope envelope = crypto.encrypt(plaintext);

            assertThat(envelope).isNotNull();
            assertThat(envelope.encryptedKey()).isNotEmpty();
            assertThat(envelope.iv()).isNotEmpty();
            assertThat(envelope.ciphertext()).isNotEmpty();
            assertThat(envelope.tag()).isNotEmpty(); // GCM has tag
        }

        @Test
        @DisplayName("encrypt plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            assertThatThrownBy(() -> crypto.encrypt(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Plaintext");
        }

        @Test
        @DisplayName("未设置公钥encrypt抛出异常")
        void testEncryptWithoutPublicKeyThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThatThrownBy(() -> crypto.encrypt("test".getBytes()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("public key");
        }

        @Test
        @DisplayName("encryptBase64返回Base64字符串")
        void testEncryptBase64() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            String base64 = crypto.encryptBase64("test".getBytes());

            assertThat(base64).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("使用AAD加密成功")
        void testEncryptWithAad() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            byte[] plaintext = "AAD protected data".getBytes();
            byte[] aad = "header-info".getBytes();

            EncryptedEnvelope envelope = crypto.encrypt(plaintext, aad);

            assertThat(envelope).isNotNull();
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            EncryptedEnvelope envelope = crypto.encrypt(plaintext);

            byte[] decrypted = crypto.decrypt(envelope);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt envelope为null抛出异常")
        void testDecryptNullEnvelopeThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decrypt(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Envelope");
        }

        @Test
        @DisplayName("未设置私钥decrypt抛出异常")
        void testDecryptWithoutPrivateKeyThrows() {
            EnvelopeCrypto encryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());
            EncryptedEnvelope envelope = encryptor.encrypt("test".getBytes());

            EnvelopeCrypto decryptor = EnvelopeCrypto.rsaAesGcm();

            assertThatThrownBy(() -> decryptor.decrypt(envelope))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("private key");
        }

        @Test
        @DisplayName("decryptBase64解密Base64字符串")
        void testDecryptBase64() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
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
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decryptBase64(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Base64");
        }

        @Test
        @DisplayName("使用错误密钥解密抛出异常")
        void testDecryptWrongKeyThrows() throws Exception {
            EnvelopeCrypto encryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());
            EncryptedEnvelope envelope = encryptor.encrypt("test".getBytes());

            // Generate different key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair wrongKeyPair = keyGen.generateKeyPair();

            EnvelopeCrypto decryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPrivateKey(wrongKeyPair.getPrivate());

            assertThatThrownBy(() -> decryptor.decrypt(envelope))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("使用AAD解密成功")
        void testDecryptWithAad() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "AAD protected data".getBytes();
            byte[] aad = "header-info".getBytes();

            EncryptedEnvelope envelope = crypto.encrypt(plaintext, aad);
            byte[] decrypted = crypto.decrypt(envelope, aad);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用错误AAD解密抛出异常")
        void testDecryptWithWrongAadThrows() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "AAD protected data".getBytes();
            byte[] aad = "correct-aad".getBytes();
            byte[] wrongAad = "wrong-aad".getBytes();

            EncryptedEnvelope envelope = crypto.encrypt(plaintext, aad);

            assertThatThrownBy(() -> crypto.decrypt(envelope, wrongAad))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整加密解密流程")
        void testEndToEnd() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "End-to-end test message".getBytes(StandardCharsets.UTF_8);

            EncryptedEnvelope envelope = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(envelope);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            String plaintext = "你好世界 🎉 Special: <>&\"'";

            EncryptedEnvelope envelope = crypto.encrypt(plaintext.getBytes(StandardCharsets.UTF_8));
            String decrypted = new String(crypto.decrypt(envelope), StandardCharsets.UTF_8);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("空字节数组")
        void testEmptyBytes() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = new byte[0];

            EncryptedEnvelope envelope = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(envelope);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testLargeData() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic())
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = new byte[100 * 1024]; // 100 KB
            Arrays.fill(plaintext, (byte) 0x42);

            EncryptedEnvelope envelope = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(envelope);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("分离的加密器和解密器")
        void testSeparateEncryptorDecryptor() {
            EnvelopeCrypto encryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            EnvelopeCrypto decryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Separate encryptor/decryptor test".getBytes();

            EncryptedEnvelope envelope = encryptor.encrypt(plaintext);
            byte[] decrypted = decryptor.decrypt(envelope);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("通过Base64传输")
        void testBase64Transfer() {
            EnvelopeCrypto encryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPublicKey(rsaKeyPair.getPublic());

            EnvelopeCrypto decryptor = EnvelopeCrypto.rsaAesGcm()
                    .setRecipientPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Base64 transfer test".getBytes();

            // Encrypt and get Base64
            String base64 = encryptor.encryptBase64(plaintext);

            // Decrypt from Base64
            byte[] decrypted = decryptor.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Getter Tests / getter测试")
    class GetterTests {

        @Test
        @DisplayName("getAsymmetricAlgorithm返回正确值")
        void testGetAsymmetricAlgorithm() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThat(crypto.getAsymmetricAlgorithm())
                    .isEqualTo(AsymmetricAlgorithm.RSA_OAEP_SHA256);
        }

        @Test
        @DisplayName("getSymmetricAlgorithm返回正确值")
        void testGetSymmetricAlgorithm() {
            EnvelopeCrypto crypto = EnvelopeCrypto.rsaAesGcm();

            assertThat(crypto.getSymmetricAlgorithm())
                    .isEqualTo(SymmetricAlgorithm.AES_GCM_256);
        }
    }
}
