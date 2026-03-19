package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.crypto.enums.DigestAlgorithm;
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
 * RsaOaepCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("RsaOaepCipher 测试")
class RsaOaepCipherTest {

    private static final String TEST_MESSAGE = "Hello, RSA-OAEP!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("sha256创建实例")
        void testSha256() {
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).contains("SHA-256");
        }

        @Test
        @DisplayName("sha384创建实例")
        void testSha384() {
            RsaOaepCipher cipher = RsaOaepCipher.sha384();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).contains("SHA-384");
        }

        @Test
        @DisplayName("sha512创建实例")
        void testSha512() {
            RsaOaepCipher cipher = RsaOaepCipher.sha512();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).contains("SHA-512");
        }

        @Test
        @DisplayName("withGeneratedKeyPair创建带密钥对的实例")
        void testWithGeneratedKeyPair() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("密钥大小太小抛出异常")
        void testKeySizeTooSmall() {
            assertThatThrownBy(() -> RsaOaepCipher.withGeneratedKeyPair(1024))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2048");
        }

        @Test
        @DisplayName("密钥大小不是1024的倍数抛出异常")
        void testKeySizeNotMultipleOf1024() {
            assertThatThrownBy(() -> RsaOaepCipher.withGeneratedKeyPair(2500))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1024");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("builder默认值")
        void testBuilderDefaults() {
            RsaOaepCipher cipher = RsaOaepCipher.builder().build();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).contains("SHA-256");
        }

        @Test
        @DisplayName("builder设置digest")
        void testBuilderDigest() {
            RsaOaepCipher cipher = RsaOaepCipher.builder()
                    .digest(DigestAlgorithm.SHA512)
                    .build();
            assertThat(cipher.getAlgorithm()).contains("SHA-512");
        }

        @Test
        @DisplayName("builder设置mgf")
        void testBuilderMgf() {
            RsaOaepCipher cipher = RsaOaepCipher.builder()
                    .mgf("MGF1")
                    .build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("builder设置label")
        void testBuilderLabel() {
            byte[] label = "test-label".getBytes(StandardCharsets.UTF_8);
            RsaOaepCipher cipher = RsaOaepCipher.builder()
                    .label(label)
                    .build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("builder digest为null抛出异常")
        void testBuilderNullDigest() {
            assertThatThrownBy(() -> RsaOaepCipher.builder().digest(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("builder mgf为null抛出异常")
        void testBuilderNullMgf() {
            assertThatThrownBy(() -> RsaOaepCipher.builder().mgf(null))
                    .isInstanceOf(NullPointerException.class);
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

            RsaOaepCipher cipher = RsaOaepCipher.sha256();
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
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            cipher.setPublicKey(encoded);
            assertThat(cipher.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null字节数组抛出异常")
        void testSetPublicKeyNullBytes() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPublicKey((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem null抛出异常")
        void testSetPublicKeyPemNull() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPublicKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem无效格式抛出异常")
        void testSetPublicKeyPemInvalid() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPublicKeyPem("invalid-pem"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey非RSA密钥抛出异常")
        void testSetPublicKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPublicKey(keyPair.getPublic()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("RSA");
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

            RsaOaepCipher cipher = RsaOaepCipher.sha256();
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
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            cipher.setPrivateKey(encoded);
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem null抛出异常")
        void testSetPrivateKeyPemNull() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPrivateKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey非RSA密钥抛出异常")
        void testSetPrivateKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(256);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> RsaOaepCipher.sha256().setPrivateKey(keyPair.getPrivate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("RSA");
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

            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            cipher.setKeyPair(keyPair);
            assertThat(cipher.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(cipher.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assertThatThrownBy(() -> RsaOaepCipher.sha256().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt和decrypt字节数组")
        void testEncryptDecryptBytes() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt和decrypt字符串")
        void testEncryptDecryptString() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

            byte[] ciphertext = cipher.encrypt(TEST_MESSAGE);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("不同digest算法加密解密")
        void testDifferentDigestAlgorithms() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            // SHA-256
            RsaOaepCipher cipher256 = RsaOaepCipher.sha256();
            cipher256.setKeyPair(keyPair);
            byte[] ciphertext = cipher256.encrypt(TEST_MESSAGE);
            assertThat(cipher256.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);

            // SHA-384
            RsaOaepCipher cipher384 = RsaOaepCipher.sha384();
            cipher384.setKeyPair(keyPair);
            ciphertext = cipher384.encrypt(TEST_MESSAGE);
            assertThat(cipher384.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);

            // SHA-512
            RsaOaepCipher cipher512 = RsaOaepCipher.sha512();
            cipher512.setKeyPair(keyPair);
            ciphertext = cipher512.encrypt(TEST_MESSAGE);
            assertThat(cipher512.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("encrypt null plaintext抛出异常")
        void testEncryptNullPlaintext() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

            assertThatThrownBy(() -> cipher.encrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt null字符串抛出异常")
        void testEncryptNullString() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

            assertThatThrownBy(() -> cipher.encrypt((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt未设置公钥抛出异常")
        void testEncryptWithoutPublicKey() {
            RsaOaepCipher cipher = RsaOaepCipher.sha256();

            assertThatThrownBy(() -> cipher.encrypt("test".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("decrypt null ciphertext抛出异常")
        void testDecryptNullCiphertext() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

            assertThatThrownBy(() -> cipher.decrypt(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("decrypt未设置私钥抛出异常")
        void testDecryptWithoutPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            cipher.setPublicKey(keyPair.getPublic());

            byte[] ciphertext = cipher.encrypt("test".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> cipher.decrypt(ciphertext))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("decrypt错误ciphertext抛出异常")
        void testDecryptInvalidCiphertext() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
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
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64Null() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

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
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex null抛出异常")
        void testDecryptHexNull() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);

            assertThatThrownBy(() -> cipher.decryptHex(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm包含OAEP")
        void testGetAlgorithm() {
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            assertThat(cipher.getAlgorithm()).contains("OAEP");
        }

        @Test
        @DisplayName("getMaxEncryptSize")
        void testGetMaxEncryptSize() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            // 2048 bits = 256 bytes - 2 * 32 (SHA-256) - 2 = 190 bytes
            assertThat(cipher.getMaxEncryptSize()).isEqualTo(190);
        }

        @Test
        @DisplayName("getMaxEncryptSize SHA-512")
        void testGetMaxEncryptSizeSha512() {
            RsaOaepCipher cipher = RsaOaepCipher.builder()
                    .digest(DigestAlgorithm.SHA512)
                    .build();
            KeyPair keyPair = cipher.generateKeyPair();
            cipher.setPublicKey(keyPair.getPublic());
            cipher.setPrivateKey(keyPair.getPrivate());
            // 2048 bits = 256 bytes - 2 * 64 (SHA-512) - 2 = 126 bytes
            assertThat(cipher.getMaxEncryptSize()).isEqualTo(126);
        }

        @Test
        @DisplayName("generateKeyPair")
        void testGenerateKeyPair() {
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            KeyPair keyPair = cipher.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("实现AsymmetricCipher接口")
        void testImplementsAsymmetricCipher() {
            RsaOaepCipher cipher = RsaOaepCipher.sha256();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }
    }

    @Nested
    @DisplayName("数据大小限制测试")
    class DataSizeLimitTests {

        @Test
        @DisplayName("encrypt数据过大抛出异常")
        void testEncryptDataTooLarge() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            // SHA-256 max size is 190 bytes for 2048-bit key
            byte[] largeData = new byte[200];

            assertThatThrownBy(() -> cipher.encrypt(largeData))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("encrypt最大允许数据大小")
        void testEncryptMaxSize() {
            RsaOaepCipher cipher = RsaOaepCipher.withGeneratedKeyPair(2048);
            int maxSize = cipher.getMaxEncryptSize();
            byte[] maxData = new byte[maxSize];

            byte[] ciphertext = cipher.encrypt(maxData);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(maxData);
        }
    }

    @Nested
    @DisplayName("标签功能测试")
    class LabelTests {

        @Test
        @DisplayName("使用标签加密解密")
        void testEncryptDecryptWithLabel() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            byte[] label = "context-info".getBytes(StandardCharsets.UTF_8);

            RsaOaepCipher cipher = RsaOaepCipher.builder()
                    .label(label)
                    .build();
            cipher.setKeyPair(keyPair);

            byte[] ciphertext = cipher.encrypt(TEST_MESSAGE);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }
    }
}
