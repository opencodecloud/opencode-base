package cloud.opencode.base.crypto.sealedbox;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SecretBox}.
 * SecretBox单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SecretBox Tests / SecretBox测试")
class SecretBoxTest {

    private static SecretKey testKey;

    @BeforeAll
    static void setup() {
        testKey = SecretBox.generateKey();
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKey生成256位密钥")
        void testGenerateKey() {
            SecretKey key = SecretBox.generateKey();

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).hasSize(32); // 256 bits = 32 bytes
        }

        @Test
        @DisplayName("每次生成的密钥不同")
        void testGenerateKeyUnique() {
            SecretKey key1 = SecretBox.generateKey();
            SecretKey key2 = SecretBox.generateKey();

            assertThat(key1.getEncoded()).isNotEqualTo(key2.getEncoded());
        }
    }

    @Nested
    @DisplayName("keyFromBytes Tests / keyFromBytes方法测试")
    class KeyFromBytesTests {

        @Test
        @DisplayName("从32字节数组创建密钥成功")
        void testKeyFromBytesSuccess() {
            byte[] keyBytes = new byte[32];
            Arrays.fill(keyBytes, (byte) 0x42);

            SecretKey key = SecretBox.keyFromBytes(keyBytes);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
            assertThat(key.getEncoded()).isEqualTo(keyBytes);
        }

        @Test
        @DisplayName("null字节数组抛出异常")
        void testKeyFromBytesNullThrows() {
            assertThatThrownBy(() -> SecretBox.keyFromBytes(null))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("32 bytes");
        }

        @Test
        @DisplayName("长度不是32的字节数组抛出异常")
        void testKeyFromBytesWrongLengthThrows() {
            assertThatThrownBy(() -> SecretBox.keyFromBytes(new byte[16]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("32 bytes");

            assertThatThrownBy(() -> SecretBox.keyFromBytes(new byte[64]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("32 bytes");
        }
    }

    @Nested
    @DisplayName("Encryption Tests / 加密测试")
    class EncryptionTests {

        @Test
        @DisplayName("encrypt(byte[], key)加密字节数组")
        void testEncryptBytes() {
            byte[] plaintext = "Hello, SecretBox!".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);

            assertThat(encrypted).isNotNull();
            // Nonce (12 bytes) + ciphertext (with 16 byte tag)
            assertThat(encrypted.length).isGreaterThan(plaintext.length + 12 + 16 - 1);
            assertThat(encrypted).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt(String, key)加密字符串")
        void testEncryptString() {
            String plaintext = "Hello, String SecretBox!";

            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(plaintext.length());
        }

        @Test
        @DisplayName("相同明文每次加密结果不同(随机nonce)")
        void testEncryptRandomNonce() {
            byte[] plaintext = "Same message".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted1 = SecretBox.encrypt(plaintext, testKey);
            byte[] encrypted2 = SecretBox.encrypt(plaintext, testKey);

            assertThat(encrypted1).isNotEqualTo(encrypted2);
        }

        @Test
        @DisplayName("plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            assertThatThrownBy(() -> SecretBox.encrypt((byte[]) null, testKey))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("plaintext");
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testEncryptNullKeyThrows() {
            assertThatThrownBy(() -> SecretBox.encrypt("test".getBytes(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }
    }

    @Nested
    @DisplayName("encryptWithAad Tests / encryptWithAad方法测试")
    class EncryptWithAadTests {

        @Test
        @DisplayName("使用AAD加密成功")
        void testEncryptWithAad() {
            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);
            byte[] aad = "header-info".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, aad);

            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(plaintext.length);
        }

        @Test
        @DisplayName("AAD为null时正常加密")
        void testEncryptWithNullAad() {
            byte[] plaintext = "Data without AAD".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, null);

            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("AAD为空数组时正常加密")
        void testEncryptWithEmptyAad() {
            byte[] plaintext = "Data with empty AAD".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, new byte[0]);

            assertThat(encrypted).isNotNull();
        }

        @Test
        @DisplayName("plaintext为null抛出异常")
        void testEncryptWithAadNullPlaintextThrows() {
            assertThatThrownBy(() -> SecretBox.encryptWithAad(null, testKey, new byte[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("plaintext");
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testEncryptWithAadNullKeyThrows() {
            assertThatThrownBy(() -> SecretBox.encryptWithAad("test".getBytes(), null, new byte[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);

            byte[] decrypted = SecretBox.decrypt(encrypted, testKey);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptAsString解密为字符串")
        void testDecryptAsString() {
            String plaintext = "Hello, String Decryption!";
            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);

            String decrypted = SecretBox.decryptAsString(encrypted, testKey);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypted为null抛出异常")
        void testDecryptNullEncryptedThrows() {
            assertThatThrownBy(() -> SecretBox.decrypt(null, testKey))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("encrypted");
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testDecryptNullKeyThrows() {
            byte[] encrypted = SecretBox.encrypt("test".getBytes(), testKey);

            assertThatThrownBy(() -> SecretBox.decrypt(encrypted, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }

        @Test
        @DisplayName("数据太短抛出异常")
        void testDecryptTooShortThrows() {
            byte[] tooShort = new byte[10]; // Less than nonce + tag

            assertThatThrownBy(() -> SecretBox.decrypt(tooShort, testKey))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("使用错误密钥解密抛出异常")
        void testDecryptWrongKeyThrows() {
            byte[] encrypted = SecretBox.encrypt("test".getBytes(), testKey);
            SecretKey wrongKey = SecretBox.generateKey();

            assertThatThrownBy(() -> SecretBox.decrypt(encrypted, wrongKey))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Authentication failed");
        }

        @Test
        @DisplayName("篡改的数据解密抛出异常")
        void testDecryptTamperedDataThrows() {
            byte[] encrypted = SecretBox.encrypt("test".getBytes(), testKey);
            // Tamper with data
            encrypted[encrypted.length - 1] ^= 0xFF;

            assertThatThrownBy(() -> SecretBox.decrypt(encrypted, testKey))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Authentication failed");
        }
    }

    @Nested
    @DisplayName("decryptWithAad Tests / decryptWithAad方法测试")
    class DecryptWithAadTests {

        @Test
        @DisplayName("使用正确AAD解密成功")
        void testDecryptWithAad() {
            byte[] plaintext = "AAD protected data".getBytes(StandardCharsets.UTF_8);
            byte[] aad = "auth-header".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, aad);
            byte[] decrypted = SecretBox.decryptWithAad(encrypted, testKey, aad);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用错误AAD解密抛出异常")
        void testDecryptWithWrongAadThrows() {
            byte[] plaintext = "AAD protected data".getBytes(StandardCharsets.UTF_8);
            byte[] aad = "correct-aad".getBytes(StandardCharsets.UTF_8);
            byte[] wrongAad = "wrong-aad".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, aad);

            assertThatThrownBy(() -> SecretBox.decryptWithAad(encrypted, testKey, wrongAad))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Authentication failed");
        }

        @Test
        @DisplayName("AAD为null时正常解密")
        void testDecryptWithNullAad() {
            byte[] plaintext = "Data without AAD".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = SecretBox.encryptWithAad(plaintext, testKey, null);

            byte[] decrypted = SecretBox.decryptWithAad(encrypted, testKey, null);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypted为null抛出异常")
        void testDecryptWithAadNullEncryptedThrows() {
            assertThatThrownBy(() -> SecretBox.decryptWithAad(null, testKey, new byte[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("encrypted");
        }

        @Test
        @DisplayName("key为null抛出异常")
        void testDecryptWithAadNullKeyThrows() {
            byte[] encrypted = SecretBox.encryptWithAad("test".getBytes(), testKey, new byte[0]);

            assertThatThrownBy(() -> SecretBox.decryptWithAad(encrypted, null, new byte[0]))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
        }

        @Test
        @DisplayName("数据太短抛出异常")
        void testDecryptWithAadTooShortThrows() {
            byte[] tooShort = new byte[10];

            assertThatThrownBy(() -> SecretBox.decryptWithAad(tooShort, testKey, new byte[0]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整加密解密流程 - 字节数组")
        void testEndToEndBytes() {
            SecretKey key = SecretBox.generateKey();
            byte[] plaintext = "End-to-end byte array test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encrypt(plaintext, key);
            byte[] decrypted = SecretBox.decrypt(encrypted, key);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整加密解密流程 - 字符串")
        void testEndToEndString() {
            SecretKey key = SecretBox.generateKey();
            String plaintext = "End-to-end string test";

            byte[] encrypted = SecretBox.encrypt(plaintext, key);
            String decrypted = SecretBox.decryptAsString(encrypted, key);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            String plaintext = "你好世界 🎉 Special: <>&\"'";

            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);
            String decrypted = SecretBox.decryptAsString(encrypted, testKey);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("空字符串")
        void testEmptyString() {
            byte[] plaintext = new byte[0];

            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);
            byte[] decrypted = SecretBox.decrypt(encrypted, testKey);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testLargeData() {
            byte[] plaintext = new byte[1024 * 1024]; // 1 MB
            Arrays.fill(plaintext, (byte) 0x42);

            byte[] encrypted = SecretBox.encrypt(plaintext, testKey);
            byte[] decrypted = SecretBox.decrypt(encrypted, testKey);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整AAD加密解密流程")
        void testEndToEndWithAad() {
            SecretKey key = SecretBox.generateKey();
            byte[] plaintext = "AAD end-to-end test".getBytes(StandardCharsets.UTF_8);
            byte[] aad = "metadata".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = SecretBox.encryptWithAad(plaintext, key, aad);
            byte[] decrypted = SecretBox.decryptWithAad(encrypted, key, aad);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用keyFromBytes创建的密钥加密解密")
        void testWithKeyFromBytes() {
            byte[] keyBytes = new byte[32];
            Arrays.fill(keyBytes, (byte) 0xAB);
            SecretKey key = SecretBox.keyFromBytes(keyBytes);

            String plaintext = "Test with custom key bytes";
            byte[] encrypted = SecretBox.encrypt(plaintext, key);
            String decrypted = SecretBox.decryptAsString(encrypted, key);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
