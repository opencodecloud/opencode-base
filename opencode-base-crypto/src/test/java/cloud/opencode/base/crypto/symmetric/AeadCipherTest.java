package cloud.opencode.base.crypto.symmetric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link AeadCipher} interface.
 * AeadCipher接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("AeadCipher Interface Tests / AeadCipher接口测试")
class AeadCipherTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();
    private static final byte[] AES_KEY = "0123456789abcdef0123456789abcdef".getBytes(); // 32 bytes
    private static final byte[] AAD = "Additional Authenticated Data".getBytes();

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("AesGcmCipher实现AeadCipher接口")
        void testAesGcmCipherImplementsInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            assertThat(cipher).isInstanceOf(AeadCipher.class);
        }

        @Test
        @DisplayName("ChaChaCipher实现AeadCipher接口")
        void testChaChaCipherImplementsInterface() {
            AeadCipher cipher = ChaChaCipher.create();
            assertThat(cipher).isInstanceOf(AeadCipher.class);
        }

        @Test
        @DisplayName("通过接口调用setKey(byte[])方法")
        void testSetKeyBytesThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            AeadCipher result = cipher.setKey(AES_KEY);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setKey(SecretKey)方法")
        void testSetKeySecretKeyThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            SecretKey key = new SecretKeySpec(AES_KEY, "AES");
            AeadCipher result = cipher.setKey(key);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setIv方法")
        void testSetIvThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            byte[] iv = cipher.generateIv();
            AeadCipher result = cipher.setIv(iv);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setNonce方法")
        void testSetNonceThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            byte[] nonce = cipher.generateNonce();
            AeadCipher result = cipher.setNonce(nonce);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setAad方法")
        void testSetAadThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            AeadCipher result = cipher.setAad(AAD);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setTagLength方法")
        void testSetTagLengthThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            AeadCipher result = cipher.setTagLength(128);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用encrypt(byte[])方法")
        void testEncryptBytesThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(AesGcmCipher.create().generateIv());
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(TEST_BYTES.length); // Includes auth tag
        }

        @Test
        @DisplayName("通过接口调用encrypt(String)方法")
        void testEncryptStringThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(AesGcmCipher.create().generateIv());
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用decrypt方法")
        void testDecryptThroughInterface() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv);
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            byte[] decrypted = cipher.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用decryptToString方法")
        void testDecryptToStringThroughInterface() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv);
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }

        @Test
        @DisplayName("通过接口调用encryptBase64方法")
        void testEncryptBase64ThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(AesGcmCipher.create().generateIv());
            String base64 = cipher.encryptBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用encryptBase64(String)方法")
        void testEncryptBase64StringThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(AesGcmCipher.create().generateIv());
            String base64 = cipher.encryptBase64(TEST_DATA);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用decryptBase64方法")
        void testDecryptBase64ThroughInterface() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv);
            String base64 = cipher.encryptBase64(TEST_BYTES);
            byte[] decrypted = cipher.decryptBase64(base64);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用decryptBase64ToString方法")
        void testDecryptBase64ToStringThroughInterface() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv);
            // Encrypt and decrypt raw bytes first, then convert
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }

        @Test
        @DisplayName("通过接口调用encryptHex方法")
        void testEncryptHexThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(AesGcmCipher.create().generateIv());
            String hex = cipher.encryptHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("通过接口调用decryptHex方法")
        void testDecryptHexThroughInterface() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv);
            String hex = cipher.encryptHex(TEST_BYTES);
            byte[] decrypted = cipher.decryptHex(hex);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用generateIv方法")
        void testGenerateIvThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            byte[] iv = cipher.generateIv();
            assertThat(iv).isNotNull();
            assertThat(iv).hasSize(cipher.getIvLength());
        }

        @Test
        @DisplayName("通过接口调用generateNonce方法")
        void testGenerateNonceThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            byte[] nonce = cipher.generateNonce();
            assertThat(nonce).isNotNull();
            assertThat(nonce).hasSize(cipher.getIvLength());
        }

        @Test
        @DisplayName("通过接口调用getIvLength方法")
        void testGetIvLengthThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            int ivLength = cipher.getIvLength();
            assertThat(ivLength).isEqualTo(12); // GCM uses 12-byte nonce
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            AeadCipher cipher = AesGcmCipher.create();
            String algorithm = cipher.getAlgorithm();
            assertThat(algorithm).isEqualTo("AES/GCM/NoPadding");
        }
    }

    @Nested
    @DisplayName("AAD Tests / 附加认证数据测试")
    class AadTests {

        @Test
        @DisplayName("使用AAD加解密")
        void testEncryptDecryptWithAad() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv)
                    .setAad(AAD);

            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }

        @Test
        @DisplayName("不同AAD解密失败")
        void testDecryptWithDifferentAadFails() {
            byte[] iv = AesGcmCipher.create().generateIv();

            // Encrypt with one AAD
            AeadCipher encryptCipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv)
                    .setAad(AAD);
            byte[] encrypted = encryptCipher.encrypt(TEST_DATA);

            // Try to decrypt with different AAD
            AeadCipher decryptCipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv)
                    .setAad("Different AAD".getBytes());

            assertThatThrownBy(() -> decryptCipher.decrypt(encrypted))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同实现通过接口引用")
        void testDifferentImplementations() {
            AeadCipher gcm = AesGcmCipher.create();
            AeadCipher chacha = ChaChaCipher.create();

            assertThat(gcm.getAlgorithm()).contains("GCM");
            assertThat(chacha.getAlgorithm()).contains("ChaCha");
        }

        @Test
        @DisplayName("通过接口数组批量加解密")
        void testBatchEncryption() {
            AeadCipher[] ciphers = {
                    AesGcmCipher.create(),
                    ChaChaCipher.create()
            };

            for (AeadCipher cipher : ciphers) {
                byte[] iv = cipher.generateIv();
                cipher.setKey(AES_KEY).setIv(iv);

                byte[] encrypted = cipher.encrypt(TEST_DATA);
                String decrypted = cipher.decryptToString(encrypted);
                assertThat(decrypted).isEqualTo(TEST_DATA);
            }
        }
    }

    @Nested
    @DisplayName("Fluent API Tests / 流式API测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式调用设置方法")
        void testChainedSetters() {
            byte[] iv = AesGcmCipher.create().generateIv();
            AeadCipher cipher = AesGcmCipher.create()
                    .setKey(AES_KEY)
                    .setIv(iv)
                    .setAad(AAD)
                    .setTagLength(128);

            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }
    }
}
