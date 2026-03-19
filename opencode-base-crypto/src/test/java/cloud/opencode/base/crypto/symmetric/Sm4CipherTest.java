package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Sm4Cipher}.
 * Sm4Cipher单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sm4Cipher Tests / Sm4Cipher测试")
class Sm4CipherTest {

    private static byte[] testKey;
    private static byte[] testIvCbc;
    private static byte[] testIvGcm;

    @BeforeAll
    static void setup() {
        // SM4 requires 128-bit (16 bytes) key
        testKey = new byte[16];
        Arrays.fill(testKey, (byte) 0x42);

        // CBC IV is 16 bytes
        testIvCbc = new byte[16];
        Arrays.fill(testIvCbc, (byte) 0x11);

        // GCM IV is 12 bytes
        testIvGcm = new byte[12];
        Arrays.fill(testIvGcm, (byte) 0x22);
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("cbc创建CBC模式实例")
        void testCbc() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("SM4-CBC");
        }

        @Test
        @DisplayName("gcm创建GCM模式实例")
        void testGcm() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();

            assertThat(cipher).isNotNull();
            assertThat(cipher.getAlgorithm()).isEqualTo("SM4-GCM");
        }

        @Test
        @DisplayName("isBouncyCastleAvailable检查BC可用性")
        void testIsBouncyCastleAvailable() {
            boolean available = Sm4Cipher.isBouncyCastleAvailable();

            // Just verify it returns a boolean without throwing
            assertThat(available).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("Key Configuration Tests / 密钥配置测试")
    class KeyConfigurationTests {

        @Test
        @DisplayName("setKey(byte[])设置密钥")
        void testSetKeyBytes() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            Sm4Cipher result = cipher.setKey(testKey);

            assertThat(result).isSameAs(cipher);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("setKey(SecretKey)设置密钥")
        void testSetKeySecretKey() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            SecretKey secretKey = new SecretKeySpec(testKey, "SM4");
            Sm4Cipher result = cipher.setKey(secretKey);

            assertThat(result).isSameAs(cipher);
            assertThat(cipher.getKey()).isEqualTo(secretKey);
        }

        @Test
        @DisplayName("setKey(byte[]) null抛出异常")
        void testSetKeyBytesNullThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setKey((byte[]) null))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("128 bits");
        }

        @Test
        @DisplayName("setKey(byte[])长度不是16抛出异常")
        void testSetKeyBytesWrongLengthThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setKey(new byte[32]))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("128 bits");
        }
    }

    @Nested
    @DisplayName("IV Configuration Tests / IV配置测试")
    class IvConfigurationTests {

        @Test
        @DisplayName("setIv设置CBC IV")
        void testSetIvCbc() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            Sm4Cipher result = cipher.setIv(testIvCbc);

            assertThat(result).isSameAs(cipher);
            assertThat(cipher.getIv()).isEqualTo(testIvCbc);
        }

        @Test
        @DisplayName("setIv设置GCM IV")
        void testSetIvGcm() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();
            Sm4Cipher result = cipher.setIv(testIvGcm);

            assertThat(result).isSameAs(cipher);
            assertThat(cipher.getIv()).isEqualTo(testIvGcm);
        }

        @Test
        @DisplayName("setIv错误长度CBC抛出异常")
        void testSetIvWrongLengthCbcThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setIv(new byte[12]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("16 bytes");
        }

        @Test
        @DisplayName("setIv错误长度GCM抛出异常")
        void testSetIvWrongLengthGcmThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();

            assertThatThrownBy(() -> cipher.setIv(new byte[16]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("12 bytes");
        }

        @Test
        @DisplayName("setNonce与setIv相同")
        void testSetNonce() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();
            Sm4Cipher result = cipher.setNonce(testIvGcm);

            assertThat(result).isSameAs(cipher);
            assertThat(cipher.getIv()).isEqualTo(testIvGcm);
        }
    }

    @Nested
    @DisplayName("AAD Tests / AAD测试")
    class AadTests {

        @Test
        @DisplayName("setAad在GCM模式设置AAD")
        void testSetAadGcm() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();
            byte[] aad = "additional data".getBytes(StandardCharsets.UTF_8);

            Sm4Cipher result = cipher.setAad(aad);

            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("setAad在CBC模式抛出异常")
        void testSetAadCbcThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setAad("aad".getBytes()))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("GCM mode");
        }
    }

    @Nested
    @DisplayName("Mode and Padding Tests / 模式和填充测试")
    class ModeAndPaddingTests {

        @Test
        @DisplayName("setMode设置CBC模式")
        void testSetModeCbc() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            Sm4Cipher result = cipher.setMode(CipherMode.CBC);

            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("setMode设置ECB模式")
        void testSetModeEcb() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            Sm4Cipher result = cipher.setMode(CipherMode.ECB);

            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("setMode不支持的模式抛出异常")
        void testSetModeUnsupportedThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setMode(CipherMode.CTR))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("CBC, GCM, and ECB");
        }

        @Test
        @DisplayName("setPadding设置填充")
        void testSetPadding() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            Sm4Cipher result = cipher.setPadding(Padding.PKCS7);

            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("setPadding在GCM模式抛出异常")
        void testSetPaddingGcmThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();

            assertThatThrownBy(() -> cipher.setPadding(Padding.PKCS7))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("GCM mode does not use padding");
        }

        @Test
        @DisplayName("setTagLength设置标签长度")
        void testSetTagLength() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();
            Sm4Cipher result = cipher.setTagLength(128);

            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("setTagLength在CBC模式抛出异常")
        void testSetTagLengthCbcThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.setTagLength(128))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("GCM mode");
        }

        @Test
        @DisplayName("setTagLength无效长度抛出异常")
        void testSetTagLengthInvalidThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();

            assertThatThrownBy(() -> cipher.setTagLength(64))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("96, 104, 112, 120, or 128");
        }
    }

    @Nested
    @DisplayName("CBC Encryption Tests / CBC加密测试")
    class CbcEncryptionTests {

        @Test
        @DisplayName("encrypt(byte[])加密字节数组")
        void testEncryptBytes() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = "Hello, SM4-CBC!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt(String)加密字符串")
        void testEncryptString() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] encrypted = cipher.encrypt("Hello, SM4-CBC!");

            assertThat(encrypted).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encryptBase64返回Base64字符串")
        void testEncryptBase64() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            String base64 = cipher.encryptBase64("test".getBytes());

            assertThat(base64).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encryptHex返回十六进制字符串")
        void testEncryptHex() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            String hex = cipher.encryptHex("test".getBytes());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9a-fA-F]+");
        }
    }

    @Nested
    @DisplayName("GCM Encryption Tests / GCM加密测试")
    class GcmEncryptionTests {

        @Test
        @DisplayName("encrypt(byte[])加密字节数组")
        void testEncryptBytes() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm()
                    .setKey(testKey)
                    .setIv(testIvGcm);

            byte[] plaintext = "Hello, SM4-GCM!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用AAD加密")
        void testEncryptWithAad() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            byte[] aad = "header-info".getBytes(StandardCharsets.UTF_8);
            Sm4Cipher cipher = Sm4Cipher.gcm()
                    .setKey(testKey)
                    .setIv(testIvGcm)
                    .setAad(aad);

            byte[] plaintext = "AAD protected data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);

            assertThat(encrypted).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("CBC Decryption Tests / CBC解密测试")
    class CbcDecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptToString解密为字符串")
        void testDecryptToString() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            String plaintext = "Decrypt to string!";
            byte[] encrypted = cipher.encrypt(plaintext);
            String decrypted = cipher.decryptToString(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64解密Base64字符串")
        void testDecryptBase64() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = "Base64 test".getBytes(StandardCharsets.UTF_8);
            String base64 = cipher.encryptBase64(plaintext);
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64ToString解密为字符串")
        void testDecryptBase64ToString() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            String plaintext = "Base64 to string!";
            String base64 = cipher.encryptBase64(plaintext);
            String decrypted = cipher.decryptBase64ToString(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex解密十六进制字符串")
        void testDecryptHex() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = "Hex test".getBytes(StandardCharsets.UTF_8);
            String hex = cipher.encryptHex(plaintext);
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("未设置密钥decrypt抛出异常")
        void testDecryptWithoutKeyThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            // Create IV inline to ensure it's properly initialized
            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0x11);

            Sm4Cipher cipher = Sm4Cipher.cbc().setIv(iv);

            // Key check comes first, IV check second in decrypt()
            // OpenKeyException is caught and wrapped in OpenCryptoException
            assertThatThrownBy(() -> cipher.decrypt(new byte[16]))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("密文过短 decrypt抛出异常")
        void testDecryptWithoutIvThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            // Create key inline to ensure it's properly initialized
            byte[] key = new byte[16];
            Arrays.fill(key, (byte) 0x42);

            Sm4Cipher cipher = Sm4Cipher.cbc().setKey(key);

            // CBC IV is 16 bytes, so ciphertext shorter than IV length should fail
            assertThatThrownBy(() -> cipher.decrypt(new byte[8]))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("SM4 decryption failed")
                    .cause()
                    .hasMessageContaining("too short");
        }
    }

    @Nested
    @DisplayName("GCM Decryption Tests / GCM解密测试")
    class GcmDecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm()
                    .setKey(testKey)
                    .setIv(testIvGcm);

            byte[] plaintext = "Hello, GCM Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("使用AAD解密成功")
        void testDecryptWithAad() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            byte[] aad = "header-info".getBytes(StandardCharsets.UTF_8);
            Sm4Cipher cipher = Sm4Cipher.gcm()
                    .setKey(testKey)
                    .setIv(testIvGcm)
                    .setAad(aad);

            byte[] plaintext = "AAD protected data".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("File Encryption Tests / 文件加密测试")
    class FileEncryptionTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("encryptFile加密文件")
        void testEncryptFile() throws Exception {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Path source = tempDir.resolve("source.txt");
            Path encrypted = tempDir.resolve("encrypted.bin");

            Files.writeString(source, "File content to encrypt");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            cipher.encryptFile(source, encrypted);

            assertThat(Files.exists(encrypted)).isTrue();
            assertThat(Files.size(encrypted)).isGreaterThan(0);
        }

        @Test
        @DisplayName("decryptFile解密文件")
        void testDecryptFile() throws Exception {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Path source = tempDir.resolve("source.txt");
            Path encrypted = tempDir.resolve("encrypted.bin");
            Path decrypted = tempDir.resolve("decrypted.txt");

            String content = "File content to encrypt and decrypt";
            Files.writeString(source, content);

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            cipher.encryptFile(source, encrypted);
            cipher.decryptFile(encrypted, decrypted);

            assertThat(Files.readString(decrypted)).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("Stream Encryption Tests / 流加密测试")
    class StreamEncryptionTests {

        @Test
        @DisplayName("encryptStream创建加密输出流")
        void testEncryptStream() throws Exception {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (var out = cipher.encryptStream(baos)) {
                out.write("Stream test".getBytes(StandardCharsets.UTF_8));
            }

            assertThat(baos.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("decryptStream创建解密输入流")
        void testDecryptStream() throws Exception {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            // First encrypt
            byte[] plaintext = "Stream decrypt test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);

            // Then decrypt via stream
            ByteArrayInputStream bais = new ByteArrayInputStream(encrypted);
            try (var in = cipher.decryptStream(bais)) {
                byte[] decrypted = in.readAllBytes();
                assertThat(decrypted).isEqualTo(plaintext);
            }
        }
    }

    @Nested
    @DisplayName("Utility Method Tests / 工具方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("generateIv生成正确长度的IV")
        void testGenerateIv() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cbcCipher = Sm4Cipher.cbc();
            byte[] cbcIv = cbcCipher.generateIv();
            assertThat(cbcIv).hasSize(16);

            Sm4Cipher gcmCipher = Sm4Cipher.gcm();
            byte[] gcmIv = gcmCipher.generateIv();
            assertThat(gcmIv).hasSize(12);
        }

        @Test
        @DisplayName("generateNonce与generateIv相同")
        void testGenerateNonce() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm();
            byte[] nonce = cipher.generateNonce();

            assertThat(nonce).hasSize(12);
        }

        @Test
        @DisplayName("getBlockSize返回16")
        void testGetBlockSize() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThat(cipher.getBlockSize()).isEqualTo(16);
        }

        @Test
        @DisplayName("getIvLength返回正确长度")
        void testGetIvLength() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            assertThat(Sm4Cipher.cbc().getIvLength()).isEqualTo(16);
            assertThat(Sm4Cipher.gcm().getIvLength()).isEqualTo(12);
        }

        @Test
        @DisplayName("getAlgorithm返回算法名称")
        void testGetAlgorithm() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            assertThat(Sm4Cipher.cbc().getAlgorithm()).isEqualTo("SM4-CBC");
            assertThat(Sm4Cipher.gcm().getAlgorithm()).isEqualTo("SM4-GCM");
        }

        @Test
        @DisplayName("generateKey只支持128位")
        void testGenerateKey() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();
            SecretKey key = cipher.generateKey(128);

            assertThat(key).isNotNull();
            assertThat(key.getEncoded()).hasSize(16);
        }

        @Test
        @DisplayName("generateKey非128位抛出异常")
        void testGenerateKeyWrongSizeThrows() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            assertThatThrownBy(() -> cipher.generateKey(256))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("128-bit");
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("CBC完整加密解密流程")
        void testCbcEndToEnd() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = "SM4-CBC end-to-end test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("GCM完整加密解密流程")
        void testGcmEndToEnd() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.gcm()
                    .setKey(testKey)
                    .setIv(testIvGcm);

            byte[] plaintext = "SM4-GCM end-to-end test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            String plaintext = "你好世界 SM4国密 Special: <>&\"'";

            byte[] encrypted = cipher.encrypt(plaintext);
            String decrypted = cipher.decryptToString(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testLargeData() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc()
                    .setKey(testKey)
                    .setIv(testIvCbc);

            byte[] plaintext = new byte[100 * 1024]; // 100 KB
            Arrays.fill(plaintext, (byte) 0x42);

            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("自动生成密钥和IV")
        void testAutoGenerateKeyAndIv() {
            assumeTrue(Sm4Cipher.isBouncyCastleAvailable(), "Bouncy Castle not available");

            Sm4Cipher cipher = Sm4Cipher.cbc();

            byte[] plaintext = "Auto generate test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.encrypt(plaintext);

            // Key and IV should be auto-generated
            assertThat(cipher.getKey()).isNotNull();
            assertThat(cipher.getIv()).isNotNull();

            // Can decrypt with same instance
            byte[] decrypted = cipher.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
