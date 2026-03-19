package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Sm2Cipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sm2Cipher 测试")
class Sm2CipherTest {

    private static final String TEST_MESSAGE = "Hello, SM2!";

    /**
     * Check if Bouncy Castle is available
     */
    static boolean isBouncyCastleAvailable() {
        return Sm2Cipher.isAvailable();
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create创建实例")
        void testCreate() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("SM2");
        }

        @Test
        @DisplayName("withGeneratedKeyPair创建带密钥对的实例")
        void testWithGeneratedKeyPair() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("isAvailable检查BC可用性")
        void testIsAvailable() {
            // This test always passes - just verifies the method works
            boolean available = Sm2Cipher.isAvailable();
            assertThat(available).isIn(true, false);
        }

        @Test
        @DisplayName("create没有BC时抛出异常")
        void testCreateWithoutBc() {
            if (!isBouncyCastleAvailable()) {
                assertThatThrownBy(Sm2Cipher::create)
                        .isInstanceOf(OpenCryptoException.class)
                        .hasMessageContaining("Bouncy Castle");
            }
        }
    }

    @Nested
    @DisplayName("setPublicKey测试")
    class SetPublicKeyTests {

        @Test
        @DisplayName("setPublicKey(PublicKey)")
        void testSetPublicKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();

            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setPublicKey(original.getPublicKey());
            assertThat(cipher.getPublicKey()).isEqualTo(original.getPublicKey());
        }

        @Test
        @DisplayName("setPublicKey(byte[])")
        void testSetPublicKeyBytes() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();

            byte[] encoded = original.getPublicKey().getEncoded();
            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setPublicKey(encoded);
            assertThat(cipher.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null字节数组抛出异常")
        void testSetPublicKeyNullBytes() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPublicKey((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem null抛出异常")
        void testSetPublicKeyPemNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPublicKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem无效格式抛出异常")
        void testSetPublicKeyPemInvalid() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPublicKeyPem("invalid-pem"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey非EC密钥抛出异常")
        void testSetPublicKeyWrongAlgorithm() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> Sm2Cipher.create().setPublicKey(keyPair.getPublic()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }
    }

    @Nested
    @DisplayName("setPrivateKey测试")
    class SetPrivateKeyTests {

        @Test
        @DisplayName("setPrivateKey(PrivateKey)")
        void testSetPrivateKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();

            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setPrivateKey(original.getPrivateKey());
            assertThat(cipher.getPrivateKey()).isEqualTo(original.getPrivateKey());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])")
        void testSetPrivateKeyBytes() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();

            byte[] encoded = original.getPrivateKey().getEncoded();
            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setPrivateKey(encoded);
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem null抛出异常")
        void testSetPrivateKeyPemNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setPrivateKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey非EC密钥抛出异常")
        void testSetPrivateKeyWrongAlgorithm() throws Exception {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            java.security.KeyPairGenerator generator = java.security.KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> Sm2Cipher.create().setPrivateKey(keyPair.getPrivate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }
    }

    @Nested
    @DisplayName("setKeyPair测试")
    class SetKeyPairTests {

        @Test
        @DisplayName("setKeyPair设置公私钥")
        void testSetKeyPair() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();
            KeyPair keyPair = new KeyPair(original.getPublicKey(), original.getPrivateKey());

            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setKeyPair(keyPair);
            assertThat(cipher.getPublicKey()).isEqualTo(original.getPublicKey());
            assertThat(cipher.getPrivateKey()).isEqualTo(original.getPrivateKey());
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            assertThatThrownBy(() -> Sm2Cipher.create().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt和decrypt字节数组")
        void testEncryptDecryptBytes() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt和decrypt字符串")
        void testEncryptDecryptString() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

            byte[] ciphertext = cipher.encrypt(TEST_MESSAGE);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("encrypt null plaintext抛出异常")
        void testEncryptNullPlaintext() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

            assertThatThrownBy(() -> cipher.encrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt null字符串抛出异常")
        void testEncryptNullString() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

            assertThatThrownBy(() -> cipher.encrypt((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt未设置公钥抛出异常")
        void testEncryptWithoutPublicKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();

            assertThatThrownBy(() -> cipher.encrypt("test".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("decrypt null ciphertext抛出异常")
        void testDecryptNullCiphertext() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

            assertThatThrownBy(() -> cipher.decrypt(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("decrypt未设置私钥抛出异常")
        void testDecryptWithoutPrivateKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher original = Sm2Cipher.withGeneratedKeyPair();

            Sm2Cipher cipher = Sm2Cipher.create();
            cipher.setPublicKey(original.getPublicKey());

            byte[] ciphertext = cipher.encrypt("test".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> cipher.decrypt(ciphertext))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("decrypt错误ciphertext抛出异常")
        void testDecryptInvalidCiphertext() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
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
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64Null() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

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
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex null抛出异常")
        void testDecryptHexNull() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.withGeneratedKeyPair();

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
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();
            assertThat(cipher.getAlgorithm()).isEqualTo("SM2");
        }

        @Test
        @DisplayName("getMaxEncryptSize返回-1")
        void testGetMaxEncryptSize() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();
            assertThat(cipher.getMaxEncryptSize()).isEqualTo(-1);
        }

        @Test
        @DisplayName("generateKeyPair")
        void testGenerateKeyPair() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();
            KeyPair keyPair = cipher.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("实现AsymmetricCipher接口")
        void testImplementsAsymmetricCipher() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher = Sm2Cipher.create();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }
    }

    @Nested
    @DisplayName("密钥互操作测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用相同密钥对")
        void testKeyInteroperability() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher1 = Sm2Cipher.withGeneratedKeyPair();

            // Create another cipher with same keys
            Sm2Cipher cipher2 = Sm2Cipher.create();
            cipher2.setPublicKey(cipher1.getPublicKey());
            cipher2.setPrivateKey(cipher1.getPrivateKey());

            // Encrypt with cipher1, decrypt with cipher2
            byte[] ciphertext = cipher1.encrypt(TEST_MESSAGE);
            String decrypted = cipher2.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("通过字节数组传递密钥")
        void testKeyTransferViaBytes() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Sm2Cipher cipher1 = Sm2Cipher.withGeneratedKeyPair();

            byte[] publicKeyBytes = cipher1.getPublicKey().getEncoded();
            byte[] privateKeyBytes = cipher1.getPrivateKey().getEncoded();

            // Create another cipher with transferred keys
            Sm2Cipher cipher2 = Sm2Cipher.create();
            cipher2.setPublicKey(publicKeyBytes);
            cipher2.setPrivateKey(privateKeyBytes);

            // Encrypt with cipher1, decrypt with cipher2
            byte[] ciphertext = cipher1.encrypt(TEST_MESSAGE);
            String decrypted = cipher2.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }
    }
}
