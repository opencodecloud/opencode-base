package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.*;

/**
 * RsaCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("RsaCipher 测试")
class RsaCipherTest {

    private static final String TEST_MESSAGE = "Hello, RSA!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create创建实例")
        void testCreate() {
            RsaCipher cipher = RsaCipher.create();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("RSA/ECB/PKCS1Padding");
        }

        @Test
        @DisplayName("rsa2048创建带密钥对的实例")
        void testRsa2048() {
            RsaCipher cipher = RsaCipher.rsa2048();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("rsa4096创建带密钥对的实例")
        void testRsa4096() {
            RsaCipher cipher = RsaCipher.rsa4096();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair创建自定义大小密钥对")
        void testWithGeneratedKeyPair() {
            RsaCipher cipher = RsaCipher.withGeneratedKeyPair(2048);
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("密钥大小太小抛出异常")
        void testKeySizeTooSmall() {
            assertThatThrownBy(() -> RsaCipher.withGeneratedKeyPair(1024))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2048");
        }

        @Test
        @DisplayName("密钥大小不是1024的倍数抛出异常")
        void testKeySizeNotMultipleOf1024() {
            assertThatThrownBy(() -> RsaCipher.withGeneratedKeyPair(2500))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1024");
        }
    }

    @Nested
    @DisplayName("setPublicKey测试")
    class SetPublicKeyTests {

        @Test
        @DisplayName("setPublicKey(PublicKey)")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaCipher cipher = RsaCipher.create();
            cipher.setPublicKey(keyPair.getPublic());
            assertThat(cipher.getPublicKey()).isEqualTo(keyPair.getPublic());
        }

        @Test
        @DisplayName("setPublicKey(byte[])")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPublic().getEncoded();
            RsaCipher cipher = RsaCipher.create();
            cipher.setPublicKey(encoded);
            assertThat(cipher.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assertThatThrownBy(() -> RsaCipher.create().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null字节数组抛出异常")
        void testSetPublicKeyNullBytes() {
            assertThatThrownBy(() -> RsaCipher.create().setPublicKey((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem null抛出异常")
        void testSetPublicKeyPemNull() {
            assertThatThrownBy(() -> RsaCipher.create().setPublicKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem无效格式抛出异常")
        void testSetPublicKeyPemInvalid() {
            assertThatThrownBy(() -> RsaCipher.create().setPublicKeyPem("invalid-pem"))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("setPrivateKey测试")
    class SetPrivateKeyTests {

        @Test
        @DisplayName("setPrivateKey(PrivateKey)")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaCipher cipher = RsaCipher.create();
            cipher.setPrivateKey(keyPair.getPrivate());
            assertThat(cipher.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPrivate().getEncoded();
            RsaCipher cipher = RsaCipher.create();
            cipher.setPrivateKey(encoded);
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assertThatThrownBy(() -> RsaCipher.create().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem null抛出异常")
        void testSetPrivateKeyPemNull() {
            assertThatThrownBy(() -> RsaCipher.create().setPrivateKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("setKeyPair测试")
    class SetKeyPairTests {

        @Test
        @DisplayName("setKeyPair设置公私钥")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaCipher cipher = RsaCipher.create();
            cipher.setKeyPair(keyPair);
            assertThat(cipher.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(cipher.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assertThatThrownBy(() -> RsaCipher.create().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt和decrypt字节数组")
        void testEncryptDecryptBytes() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt和decrypt字符串")
        void testEncryptDecryptString() {
            RsaCipher cipher = RsaCipher.rsa2048();

            byte[] ciphertext = cipher.encrypt(TEST_MESSAGE);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("encrypt null plaintext抛出异常")
        void testEncryptNullPlaintext() {
            RsaCipher cipher = RsaCipher.rsa2048();

            assertThatThrownBy(() -> cipher.encrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt null字符串抛出异常")
        void testEncryptNullString() {
            RsaCipher cipher = RsaCipher.rsa2048();

            assertThatThrownBy(() -> cipher.encrypt((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt未设置公钥抛出异常")
        void testEncryptWithoutPublicKey() {
            RsaCipher cipher = RsaCipher.create();

            assertThatThrownBy(() -> cipher.encrypt("test".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("decrypt null ciphertext抛出异常")
        void testDecryptNullCiphertext() {
            RsaCipher cipher = RsaCipher.rsa2048();

            assertThatThrownBy(() -> cipher.decrypt(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("decrypt未设置私钥抛出异常")
        void testDecryptWithoutPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaCipher cipher = RsaCipher.create();
            cipher.setPublicKey(keyPair.getPublic());

            byte[] ciphertext = cipher.encrypt("test".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> cipher.decrypt(ciphertext))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("decrypt错误ciphertext抛出异常")
        void testDecryptInvalidCiphertext() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] invalidCiphertext = "invalid-ciphertext".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> cipher.decrypt(invalidCiphertext))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Base64加密解密测试")
    class Base64EncryptDecryptTests {

        @Test
        @DisplayName("encryptBase64")
        void testEncryptBase64() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64Null() {
            RsaCipher cipher = RsaCipher.rsa2048();

            assertThatThrownBy(() -> cipher.decryptBase64(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Hex加密解密测试")
    class HexEncryptDecryptTests {

        @Test
        @DisplayName("encryptHex")
        void testEncryptHex() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            RsaCipher cipher = RsaCipher.rsa2048();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex null抛出异常")
        void testDecryptHexNull() {
            RsaCipher cipher = RsaCipher.rsa2048();

            assertThatThrownBy(() -> cipher.decryptHex(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            RsaCipher cipher = RsaCipher.create();
            assertThat(cipher.getAlgorithm()).isEqualTo("RSA/ECB/PKCS1Padding");
        }

        @Test
        @DisplayName("getMaxEncryptSize")
        void testGetMaxEncryptSize() {
            RsaCipher cipher = RsaCipher.rsa2048();
            // 2048 bits = 256 bytes - 11 (PKCS1 padding) = 245 bytes
            assertThat(cipher.getMaxEncryptSize()).isEqualTo(245);
        }

        @Test
        @DisplayName("generateKeyPair")
        void testGenerateKeyPair() {
            RsaCipher cipher = RsaCipher.create();
            KeyPair keyPair = cipher.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("实现AsymmetricCipher接口")
        void testImplementsAsymmetricCipher() {
            RsaCipher cipher = RsaCipher.create();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }
    }

    @Nested
    @DisplayName("数据大小限制测试")
    class DataSizeLimitTests {

        @Test
        @DisplayName("encrypt数据过大抛出异常")
        void testEncryptDataTooLarge() {
            RsaCipher cipher = RsaCipher.rsa2048();
            // Create data larger than max encrypt size (245 bytes for 2048-bit key)
            byte[] largeData = new byte[300];

            assertThatThrownBy(() -> cipher.encrypt(largeData))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("encrypt最大允许数据大小")
        void testEncryptMaxSize() {
            RsaCipher cipher = RsaCipher.rsa2048();
            int maxSize = cipher.getMaxEncryptSize();
            byte[] maxData = new byte[maxSize];

            byte[] ciphertext = cipher.encrypt(maxData);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(maxData);
        }
    }
}
