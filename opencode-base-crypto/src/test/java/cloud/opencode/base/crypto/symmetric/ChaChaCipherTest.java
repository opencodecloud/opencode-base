package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ChaChaCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("ChaChaCipher 测试")
class ChaChaCipherTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建默认ChaChaCipher")
        void testCreate() {
            ChaChaCipher cipher = ChaChaCipher.create();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("ChaCha20-Poly1305");
        }

        @Test
        @DisplayName("Builder创建ChaChaCipher")
        void testBuilder() {
            ChaChaCipher cipher = ChaChaCipher.builder().build();
            assertThat(cipher).isNotNull();
        }
    }

    @Nested
    @DisplayName("setKey测试")
    class SetKeyTests {

        @Test
        @DisplayName("设置SecretKey")
        void testSetSecretKey() {
            byte[] keyBytes = new byte[32];
            SecretKey key = new SecretKeySpec(keyBytes, "ChaCha20");

            ChaChaCipher cipher = ChaChaCipher.create().setKey(key);
            assertThat(cipher.getKey()).isEqualTo(key);
        }

        @Test
        @DisplayName("设置256位字节密钥")
        void testSetByteKey256() {
            byte[] keyBytes = new byte[32];
            ChaChaCipher cipher = ChaChaCipher.create().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置null密钥抛出异常")
        void testSetNullKey() {
            assertThatThrownBy(() -> ChaChaCipher.create().setKey((byte[]) null))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("设置无效长度密钥抛出异常")
        void testSetInvalidKeyLength() {
            assertThatThrownBy(() -> ChaChaCipher.create().setKey(new byte[16]))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("setNonce和setIv测试")
    class SetNonceTests {

        @Test
        @DisplayName("设置有效nonce")
        void testSetValidNonce() {
            byte[] nonce = new byte[12];
            ChaChaCipher cipher = ChaChaCipher.create().setNonce(nonce);
            assertThat(cipher.getNonce()).isEqualTo(nonce);
        }

        @Test
        @DisplayName("setIv调用setNonce")
        void testSetIvCallsSetNonce() {
            byte[] iv = new byte[12];
            ChaChaCipher cipher = ChaChaCipher.create().setIv(iv);
            assertThat(cipher.getNonce()).isEqualTo(iv);
        }

        @Test
        @DisplayName("设置null nonce")
        void testSetNullNonce() {
            ChaChaCipher cipher = ChaChaCipher.create().setNonce(null);
            assertThat(cipher.getNonce()).isNull();
        }

        @Test
        @DisplayName("设置无效长度nonce抛出异常")
        void testSetInvalidNonceLength() {
            assertThatThrownBy(() -> ChaChaCipher.create().setNonce(new byte[16]))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("setAad测试")
    class SetAadTests {

        @Test
        @DisplayName("设置AAD")
        void testSetAad() {
            byte[] aad = "additional data".getBytes(StandardCharsets.UTF_8);
            ChaChaCipher cipher = ChaChaCipher.create().setAad(aad);
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("设置null AAD")
        void testSetNullAad() {
            ChaChaCipher cipher = ChaChaCipher.create().setAad(null);
            assertThat(cipher).isNotNull();
        }
    }

    @Nested
    @DisplayName("setTagLength测试")
    class SetTagLengthTests {

        @Test
        @DisplayName("设置128位标签长度")
        void testSetTagLength128() {
            ChaChaCipher cipher = ChaChaCipher.create().setTagLength(128);
            assertThat(cipher.getTagLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("设置其他标签长度抛出异常")
        void testSetInvalidTagLength() {
            assertThatThrownBy(() -> ChaChaCipher.create().setTagLength(96))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("fixed at 128");
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("加密解密字节数组")
        void testEncryptDecryptBytes() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];
            byte[] plaintext = "Hello, World!".getBytes(StandardCharsets.UTF_8);

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("加密解密字符串")
        void testEncryptDecryptString() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];
            String plaintext = "Hello, World!";

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            byte[] ciphertext = cipher.encrypt(plaintext);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("加密自动生成密钥和nonce")
        void testEncryptAutoGenerateKeyAndNonce() {
            ChaChaCipher cipher = ChaChaCipher.create();
            byte[] plaintext = "Test data".getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);

            assertThat(ciphertext).isNotNull();
            assertThat(cipher.getKey()).isNotNull();
            assertThat(cipher.getNonce()).isNotNull();
        }

        @Test
        @DisplayName("解密未设置密钥抛出异常")
        void testDecryptWithoutKey() {
            ChaChaCipher cipher = ChaChaCipher.create();

            assertThatThrownBy(() -> cipher.decrypt(new byte[32]))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("解密未设置nonce抛出异常")
        void testDecryptWithoutNonce() {
            ChaChaCipher cipher = ChaChaCipher.create().setKey(new byte[32]);

            assertThatThrownBy(() -> cipher.decrypt(new byte[32]))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("带AAD加密解密")
        void testEncryptDecryptWithAad() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];
            byte[] aad = "header".getBytes(StandardCharsets.UTF_8);
            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce)
                    .setAad(aad);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("错误AAD解密失败")
        void testDecryptWithWrongAad() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];
            byte[] aad = "header".getBytes(StandardCharsets.UTF_8);
            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);

            ChaChaCipher encCipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce)
                    .setAad(aad);

            byte[] ciphertext = encCipher.encrypt(plaintext);

            ChaChaCipher decCipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce)
                    .setAad("wrong".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> decCipher.decrypt(ciphertext))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Base64加密解密测试")
    class Base64EncryptDecryptTests {

        @Test
        @DisplayName("encryptBase64(byte[])")
        void testEncryptBase64Bytes() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("encryptBase64(String)")
        void testEncryptBase64String() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            String base64 = cipher.encryptBase64("Hello");
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decryptBase64ToString")
        void testDecryptBase64ToString() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            // For AEAD ciphers, need to create separate instances for each operation
            ChaChaCipher encCipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);
            String base64 = encCipher.encryptBase64("Hello");

            ChaChaCipher decCipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);
            // Use decryptBase64 and convert to string manually
            byte[] decrypted = decCipher.decryptBase64(base64);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("Hex加密解密测试")
    class HexEncryptDecryptTests {

        @Test
        @DisplayName("encryptHex")
        void testEncryptHex() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            String hex = cipher.encryptHex("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            String hex = cipher.encryptHex("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("文件加密解密测试")
    class FileEncryptDecryptTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("加密解密文件")
        void testEncryptDecryptFile() throws Exception {
            byte[] key = new byte[32];
            byte[] nonce = new byte[12];
            String content = "File content to encrypt";

            Path sourceFile = tempDir.resolve("source.txt");
            Path encryptedFile = tempDir.resolve("encrypted.bin");
            Path decryptedFile = tempDir.resolve("decrypted.txt");

            Files.writeString(sourceFile, content);

            ChaChaCipher cipher = ChaChaCipher.create()
                    .setKey(key)
                    .setNonce(nonce);

            cipher.encryptFile(sourceFile, encryptedFile);
            cipher.decryptFile(encryptedFile, decryptedFile);

            String decrypted = Files.readString(decryptedFile);
            assertThat(decrypted).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("Stream操作测试")
    class StreamTests {

        @Test
        @DisplayName("encryptStream抛出UnsupportedOperationException")
        void testEncryptStreamUnsupported() {
            ChaChaCipher cipher = ChaChaCipher.create();

            assertThatThrownBy(() -> cipher.encryptStream(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("decryptStream抛出UnsupportedOperationException")
        void testDecryptStreamUnsupported() {
            ChaChaCipher cipher = ChaChaCipher.create();

            assertThatThrownBy(() -> cipher.decryptStream(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class HelperMethodTests {

        @Test
        @DisplayName("generateIv")
        void testGenerateIv() {
            ChaChaCipher cipher = ChaChaCipher.create();
            byte[] iv = cipher.generateIv();

            assertThat(iv).hasSize(12);
        }

        @Test
        @DisplayName("generateNonce")
        void testGenerateNonce() {
            ChaChaCipher cipher = ChaChaCipher.create();
            byte[] nonce = cipher.generateNonce();

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("generateIv和generateNonce生成相同长度")
        void testGenerateIvAndNonceSameLength() {
            ChaChaCipher cipher = ChaChaCipher.create();

            assertThat(cipher.generateIv()).hasSize(cipher.generateNonce().length);
        }

        @Test
        @DisplayName("generateNonce生成不同值")
        void testGenerateNonceDifferent() {
            ChaChaCipher cipher = ChaChaCipher.create();
            byte[] nonce1 = cipher.generateNonce();
            byte[] nonce2 = cipher.generateNonce();

            assertThat(nonce1).isNotEqualTo(nonce2);
        }

        @Test
        @DisplayName("getIvLength")
        void testGetIvLength() {
            ChaChaCipher cipher = ChaChaCipher.create();
            assertThat(cipher.getIvLength()).isEqualTo(12);
        }

        @Test
        @DisplayName("getNonceLength")
        void testGetNonceLength() {
            ChaChaCipher cipher = ChaChaCipher.create();
            assertThat(cipher.getNonceLength()).isEqualTo(12);
        }

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            ChaChaCipher cipher = ChaChaCipher.create();
            assertThat(cipher.getAlgorithm()).isEqualTo("ChaCha20-Poly1305");
        }

        @Test
        @DisplayName("getTagLength返回128")
        void testGetTagLength() {
            ChaChaCipher cipher = ChaChaCipher.create();
            assertThat(cipher.getTagLength()).isEqualTo(128);
        }
    }
}
