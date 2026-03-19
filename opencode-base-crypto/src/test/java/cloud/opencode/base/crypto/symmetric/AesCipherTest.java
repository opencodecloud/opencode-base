package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * AesCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("AesCipher 测试")
class AesCipherTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建AES-128")
        void testAes128() {
            AesCipher cipher = AesCipher.aes128();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("创建AES-256")
        void testAes256() {
            AesCipher cipher = AesCipher.aes256();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("创建CBC模式")
        void testCbc() {
            AesCipher cipher = AesCipher.cbc();
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-CBC");
        }

        @Test
        @DisplayName("创建CTR模式")
        void testCtr() {
            AesCipher cipher = AesCipher.ctr();
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-CTR");
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("使用Builder创建默认加密器")
        void testBuilder() {
            AesCipher cipher = AesCipher.builder().build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("Builder设置密钥大小")
        void testBuilderKeySize() {
            AesCipher cipher = AesCipher.builder().keySize(128).build();
            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("Builder设置无效密钥大小抛出异常")
        void testBuilderInvalidKeySize() {
            assertThatThrownBy(() -> AesCipher.builder().keySize(64))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("Builder设置模式")
        void testBuilderMode() {
            AesCipher cipher = AesCipher.builder().mode(CipherMode.CTR).build();
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-CTR");
        }

        @Test
        @DisplayName("Builder设置GCM模式抛出异常")
        void testBuilderGcmMode() {
            assertThatThrownBy(() -> AesCipher.builder().mode(CipherMode.GCM))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("AesGcmCipher");
        }

        @Test
        @DisplayName("Builder设置填充")
        void testBuilderPadding() {
            AesCipher cipher = AesCipher.builder().padding(Padding.PKCS5).build();
            assertThat(cipher).isNotNull();
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

            AesCipher cipher = AesCipher.aes128().setKey(key);
            assertThat(cipher.getKey()).isEqualTo(key);
        }

        @Test
        @DisplayName("设置128位字节密钥")
        void testSetByteKey128() {
            byte[] keyBytes = new byte[16];
            AesCipher cipher = AesCipher.aes128().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置192位字节密钥")
        void testSetByteKey192() {
            byte[] keyBytes = new byte[24];
            AesCipher cipher = AesCipher.aes128().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置256位字节密钥")
        void testSetByteKey256() {
            byte[] keyBytes = new byte[32];
            AesCipher cipher = AesCipher.aes256().setKey(keyBytes);
            assertThat(cipher.getKey()).isNotNull();
        }

        @Test
        @DisplayName("设置null密钥抛出异常")
        void testSetNullKey() {
            assertThatThrownBy(() -> AesCipher.aes128().setKey((byte[]) null))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("设置无效长度密钥抛出异常")
        void testSetInvalidKeyLength() {
            assertThatThrownBy(() -> AesCipher.aes128().setKey(new byte[15]))
                    .isInstanceOf(OpenKeyException.class);
        }
    }

    @Nested
    @DisplayName("setIv测试")
    class SetIvTests {

        @Test
        @DisplayName("设置有效IV")
        void testSetValidIv() {
            byte[] iv = new byte[16];
            AesCipher cipher = AesCipher.aes128().setIv(iv);
            assertThat(cipher.getIv()).isEqualTo(iv);
        }

        @Test
        @DisplayName("设置null IV")
        void testSetNullIv() {
            AesCipher cipher = AesCipher.aes128().setIv(null);
            assertThat(cipher.getIv()).isNull();
        }

        @Test
        @DisplayName("设置无效长度IV抛出异常")
        void testSetInvalidIvLength() {
            assertThatThrownBy(() -> AesCipher.aes128().setIv(new byte[15]))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("setMode测试")
    class SetModeTests {

        @Test
        @DisplayName("设置CBC模式")
        void testSetCbcMode() {
            AesCipher cipher = AesCipher.aes128().setMode(CipherMode.CBC);
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-CBC");
        }

        @Test
        @DisplayName("设置CTR模式")
        void testSetCtrMode() {
            AesCipher cipher = AesCipher.aes128().setMode(CipherMode.CTR);
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-CTR");
        }

        @Test
        @DisplayName("设置ECB模式")
        void testSetEcbMode() {
            AesCipher cipher = AesCipher.aes128().setMode(CipherMode.ECB);
            assertThat(cipher.getAlgorithm()).isEqualTo("AES-ECB");
        }

        @Test
        @DisplayName("设置GCM模式抛出异常")
        void testSetGcmMode() {
            assertThatThrownBy(() -> AesCipher.aes128().setMode(CipherMode.GCM))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("AesGcmCipher");
        }

        @Test
        @DisplayName("设置CCM模式抛出异常")
        void testSetCcmMode() {
            assertThatThrownBy(() -> AesCipher.aes128().setMode(CipherMode.CCM))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("AesGcmCipher");
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("加密解密字节数组")
        void testEncryptDecryptBytes() {
            byte[] key = new byte[16];
            byte[] iv = new byte[16];
            byte[] plaintext = "Hello, World!".getBytes(StandardCharsets.UTF_8);

            AesCipher cipher = AesCipher.aes128()
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
            byte[] iv = new byte[16];
            String plaintext = "Hello, World!";

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv);

            byte[] ciphertext = cipher.encrypt(plaintext);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("加密自动生成密钥和IV")
        void testEncryptAutoGenerateKeyAndIv() {
            AesCipher cipher = AesCipher.aes256();
            byte[] plaintext = "Test data".getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);

            assertThat(ciphertext).isNotNull();
            assertThat(cipher.getKey()).isNotNull();
            assertThat(cipher.getIv()).isNotNull();
        }

        @Test
        @DisplayName("解密未设置密钥抛出异常")
        void testDecryptWithoutKey() {
            AesCipher cipher = AesCipher.aes128();

            assertThatThrownBy(() -> cipher.decrypt(new byte[32]))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("解密密文太短抛出异常")
        void testDecryptCiphertextTooShort() {
            AesCipher cipher = AesCipher.aes128().setKey(new byte[16]);

            assertThatThrownBy(() -> cipher.decrypt(new byte[8]))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("CTR模式加密解密")
        void testCtrModeEncryptDecrypt() {
            byte[] key = new byte[32];
            byte[] iv = new byte[16];
            byte[] plaintext = "CTR mode test".getBytes(StandardCharsets.UTF_8);

            AesCipher cipher = AesCipher.ctr()
                    .setKey(key)
                    .setIv(iv);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("ECB模式加密解密")
        void testEcbModeEncryptDecrypt() {
            byte[] key = new byte[16];
            // ECB requires plaintext to be multiple of block size with NoPadding
            byte[] plaintext = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8); // 16 bytes

            AesCipher cipher = AesCipher.aes128()
                    .setMode(CipherMode.ECB)
                    .setKey(key);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Base64加密解密测试")
    class Base64EncryptDecryptTests {

        @Test
        @DisplayName("encryptBase64")
        void testEncryptBase64() {
            byte[] key = new byte[16];
            byte[] iv = new byte[16];

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            byte[] key = new byte[16];
            byte[] iv = new byte[16];

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv);

            String base64 = cipher.encryptBase64("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.decryptBase64(base64);

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
            byte[] iv = new byte[16];

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv);

            String hex = cipher.encryptHex("Hello".getBytes(StandardCharsets.UTF_8));
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            byte[] key = new byte[16];
            byte[] iv = new byte[16];

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv);

            String hex = cipher.encryptHex("Hello".getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(new String(decrypted, StandardCharsets.UTF_8)).isEqualTo("Hello");
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class HelperMethodTests {

        @Test
        @DisplayName("generateIv")
        void testGenerateIv() {
            AesCipher cipher = AesCipher.aes128();
            byte[] iv = cipher.generateIv();

            assertThat(iv).hasSize(16);
        }

        @Test
        @DisplayName("generateIv生成不同值")
        void testGenerateIvDifferent() {
            AesCipher cipher = AesCipher.aes128();
            byte[] iv1 = cipher.generateIv();
            byte[] iv2 = cipher.generateIv();

            assertThat(iv1).isNotEqualTo(iv2);
        }

        @Test
        @DisplayName("getBlockSize")
        void testGetBlockSize() {
            AesCipher cipher = AesCipher.aes128();
            assertThat(cipher.getBlockSize()).isEqualTo(16);
        }

        @Test
        @DisplayName("getIvLength")
        void testGetIvLength() {
            AesCipher cipher = AesCipher.aes128();
            assertThat(cipher.getIvLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("generateKey")
        void testGenerateKey() {
            AesCipher cipher = AesCipher.aes256();
            SecretKey key = cipher.generateKey(256);

            assertThat(key).isNotNull();
            assertThat(key.getEncoded()).hasSize(32);
        }
    }

    @Nested
    @DisplayName("填充测试")
    class PaddingTests {

        @Test
        @DisplayName("setPadding")
        void testSetPadding() {
            AesCipher cipher = AesCipher.aes128()
                    .setPadding(Padding.PKCS5);

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("NoPadding模式需要正确长度明文")
        void testNoPadding() {
            byte[] key = new byte[16];
            byte[] iv = new byte[16];
            byte[] plaintext = new byte[16]; // Must be block aligned

            AesCipher cipher = AesCipher.aes128()
                    .setKey(key)
                    .setIv(iv)
                    .setPadding(Padding.NO_PADDING);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
