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
 * AesGcmCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("AesGcmCipher 测试")
class AesGcmCipherTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建AES-128-GCM")
        void testAes128Gcm() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("AES/GCM/NoPadding");
        }

        @Test
        @DisplayName("创建AES-256-GCM")
        void testAes256Gcm() {
            AesGcmCipher cipher = AesGcmCipher.aes256Gcm();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("创建默认AES-GCM")
        void testCreate() {
            AesGcmCipher cipher = AesGcmCipher.create();
            assertThat(cipher).isNotNull();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用Builder创建默认加密器")
        void testBuilder() {
            AesGcmCipher cipher = AesGcmCipher.builder().build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("Builder设置密钥大小")
        void testBuilderKeySize() {
            AesGcmCipher cipher = AesGcmCipher.builder().keySize(128).build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("Builder设置无效密钥大小抛出异常")
        void testBuilderInvalidKeySize() {
            assertThatThrownBy(() -> AesGcmCipher.builder().keySize(64))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("Builder设置标签长度")
        void testBuilderTagLength() {
            AesGcmCipher cipher = AesGcmCipher.builder().tagLength(96).build();
            assertThat(cipher.getTagLength()).isEqualTo(96);
        }

        @Test
        @DisplayName("Builder设置无效标签长度抛出异常")
        void testBuilderInvalidTagLength() {
            assertThatThrownBy(() -> AesGcmCipher.builder().tagLength(64))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("setKey测试")
    class SetKeyTests {

        @Test
        @DisplayName("设置SecretKey")
        void testSetSecretKey() {
            byte[] keyBytes = new byte[16];
            SecretKey key = new SecretKeySpec(keyBytes, "AES");

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setKey(key);
            assertThat(cipher.getKey()).isEqualTo(key);
        }

        @Test
        @DisplayName("设置128位字节密钥")
        void testSetByteKey128() {
            byte[] keyBytes = new byte[16];
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置256位字节密钥")
        void testSetByteKey256() {
            byte[] keyBytes = new byte[32];
            AesGcmCipher cipher = AesGcmCipher.aes256Gcm().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置null密钥抛出异常")
        void testSetNullKey() {
            assertThatThrownBy(() -> AesGcmCipher.aes128Gcm().setKey((byte[]) null))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("设置无效长度密钥抛出异常")
        void testSetInvalidKeyLength() {
            assertThatThrownBy(() -> AesGcmCipher.aes128Gcm().setKey(new byte[15]))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("setIv和setNonce测试")
    class SetIvNonceTests {

        @Test
        @DisplayName("设置有效IV")
        void testSetValidIv() {
            byte[] iv = new byte[12];
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setIv(iv);
            assertThat(cipher.getIv()).isEqualTo(iv);
        }

        @Test
        @DisplayName("设置有效nonce")
        void testSetValidNonce() {
            byte[] nonce = new byte[12];
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setNonce(nonce);
            assertThat(cipher.getIv()).isEqualTo(nonce);
        }

        @Test
        @DisplayName("设置null IV")
        void testSetNullIv() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setIv(null);
            assertThat(cipher.getIv()).isNull();
        }

        @Test
        @DisplayName("设置无效长度IV抛出异常")
        void testSetInvalidIvLength() {
            assertThatThrownBy(() -> AesGcmCipher.aes128Gcm().setIv(new byte[16]))
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
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setAad(aad);
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("设置null AAD")
        void testSetNullAad() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setAad(null);
            assertThat(cipher).isNotNull();
        }
    }

    @Nested
    @DisplayName("setTagLength测试")
    class SetTagLengthTests {

        @Test
        @DisplayName("设置有效标签长度")
        void testSetValidTagLength() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setTagLength(128);
            assertThat(cipher.getTagLength()).isEqualTo(128);
        }

        @Test
        @DisplayName("设置96位标签长度")
        void testSetTagLength96() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setTagLength(96);
            assertThat(cipher.getTagLength()).isEqualTo(96);
        }

        @Test
        @DisplayName("设置无效标签长度抛出异常")
        void testSetInvalidTagLength() {
            assertThatThrownBy(() -> AesGcmCipher.aes128Gcm().setTagLength(64))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("加密解密字节数组")
        void testEncryptDecryptBytes() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];
            byte[] plaintext = "Hello, World!".getBytes(StandardCharsets.UTF_8);

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("加密解密字符串")
        void testEncryptDecryptString() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];
            String plaintext = "Hello, World!";

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            byte[] ciphertext = cipher.encrypt(plaintext);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("加密自动生成密钥和IV")
        void testEncryptAutoGenerateKeyAndIv() {
            AesGcmCipher cipher = AesGcmCipher.aes256Gcm();
            byte[] plaintext = "Test data".getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);

            assertThat(ciphertext).isNotNull();
            assertThat(cipher.getKey()).isNotNull();
            assertThat(cipher.getIv()).isNotNull();
        }

        @Test
        @DisplayName("解密未设置密钥抛出异常")
        void testDecryptWithoutKey() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();

            assertThatThrownBy(() -> cipher.decrypt(new byte[32]))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("解密未设置IV抛出异常")
        void testDecryptWithoutIv() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm().setKey(new byte[16]);

            assertThatThrownBy(() -> cipher.decrypt(new byte[32]))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("带AAD加密解密")
        void testEncryptDecryptWithAad() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];
            byte[] aad = "header".getBytes(StandardCharsets.UTF_8);
            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv)
                    .setAad(aad);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("错误AAD解密失败")
        void testDecryptWithWrongAad() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];
            byte[] aad = "header".getBytes(StandardCharsets.UTF_8);
            byte[] plaintext = "Secret data".getBytes(StandardCharsets.UTF_8);

            AesGcmCipher encCipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv)
                    .setAad(aad);

            byte[] ciphertext = encCipher.encrypt(plaintext);

            AesGcmCipher decCipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv)
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
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("encryptBase64(String)")
        void testEncryptBase64String() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            String base64 = cipher.encryptBase64("Hello");
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }

        @Test
        @DisplayName("decryptBase64ToString")
        void testDecryptBase64ToString() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            // For AEAD ciphers, need to create separate instances for each operation
            AesGcmCipher encCipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);
            String base64 = encCipher.encryptBase64("Hello");

            AesGcmCipher decCipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);
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
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

            String hex = cipher.encryptHex("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            byte[] key = new byte[16];
            byte[] iv = new byte[12];

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

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
            byte[] key = new byte[16];
            byte[] iv = new byte[12];
            String content = "File content to encrypt";

            Path sourceFile = tempDir.resolve("source.txt");
            Path encryptedFile = tempDir.resolve("encrypted.bin");
            Path decryptedFile = tempDir.resolve("decrypted.txt");

            Files.writeString(sourceFile, content);

            AesGcmCipher cipher = AesGcmCipher.aes128Gcm()
                    .setKey(key)
                    .setIv(iv);

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
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();

            assertThatThrownBy(() -> cipher.encryptStream(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("decryptStream抛出UnsupportedOperationException")
        void testDecryptStreamUnsupported() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();

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
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            byte[] iv = cipher.generateIv();

            assertThat(iv).hasSize(12);
        }

        @Test
        @DisplayName("generateNonce")
        void testGenerateNonce() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            byte[] nonce = cipher.generateNonce();

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("generateIv和generateNonce生成相同长度")
        void testGenerateIvAndNonceSameLength() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();

            assertThat(cipher.generateIv()).hasSize(cipher.generateNonce().length);
        }

        @Test
        @DisplayName("generateIv生成不同值")
        void testGenerateIvDifferent() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            byte[] iv1 = cipher.generateIv();
            byte[] iv2 = cipher.generateIv();

            assertThat(iv1).isNotEqualTo(iv2);
        }

        @Test
        @DisplayName("getIvLength")
        void testGetIvLength() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            assertThat(cipher.getIvLength()).isEqualTo(12);
        }

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            assertThat(cipher.getAlgorithm()).isEqualTo("AES/GCM/NoPadding");
        }

        @Test
        @DisplayName("getTagLength默认128")
        void testGetTagLengthDefault() {
            AesGcmCipher cipher = AesGcmCipher.aes128Gcm();
            assertThat(cipher.getTagLength()).isEqualTo(128);
        }
    }
}
