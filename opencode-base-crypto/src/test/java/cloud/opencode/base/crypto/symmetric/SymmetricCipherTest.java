package cloud.opencode.base.crypto.symmetric;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SymmetricCipher} interface.
 * SymmetricCipher接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SymmetricCipher Interface Tests / SymmetricCipher接口测试")
class SymmetricCipherTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();
    private static final byte[] AES_KEY = "0123456789abcdef0123456789abcdef".getBytes(); // 32 bytes for AES-256

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("AesCipher实现SymmetricCipher接口")
        void testAesCipherImplementsInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            assertThat(cipher).isInstanceOf(SymmetricCipher.class);
        }

        @Test
        @DisplayName("通过接口调用setKey(byte[])方法")
        void testSetKeyBytesThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            SymmetricCipher result = cipher.setKey(AES_KEY);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setKey(SecretKey)方法")
        void testSetKeySecretKeyThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            SecretKey key = new SecretKeySpec(AES_KEY, "AES");
            SymmetricCipher result = cipher.setKey(key);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setIv方法")
        void testSetIvThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            byte[] iv = cipher.generateIv();
            SymmetricCipher result = cipher.setIv(iv);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setMode方法")
        void testSetModeThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            SymmetricCipher result = cipher.setMode(CipherMode.CBC);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setPadding方法")
        void testSetPaddingThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            SymmetricCipher result = cipher.setPadding(Padding.PKCS7);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用encrypt(byte[])方法")
        void testEncryptBytesThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(AesCipher.cbc().generateIv());
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用encrypt(String)方法")
        void testEncryptStringThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(AesCipher.cbc().generateIv());
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用decrypt方法")
        void testDecryptThroughInterface() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(iv);
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            byte[] decrypted = cipher.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用decryptToString方法")
        void testDecryptToStringThroughInterface() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(iv);
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }

        @Test
        @DisplayName("通过接口调用encryptBase64方法")
        void testEncryptBase64ThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(AesCipher.cbc().generateIv());
            String base64 = cipher.encryptBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用decryptBase64方法")
        void testDecryptBase64ThroughInterface() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(iv);
            String base64 = cipher.encryptBase64(TEST_BYTES);
            byte[] decrypted = cipher.decryptBase64(base64);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用encryptHex方法")
        void testEncryptHexThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(AesCipher.cbc().generateIv());
            String hex = cipher.encryptHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("通过接口调用decryptHex方法")
        void testDecryptHexThroughInterface() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(iv);
            String hex = cipher.encryptHex(TEST_BYTES);
            byte[] decrypted = cipher.decryptHex(hex);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用generateIv方法")
        void testGenerateIvThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            byte[] iv = cipher.generateIv();
            assertThat(iv).isNotNull();
            assertThat(iv).hasSize(cipher.getIvLength());
        }

        @Test
        @DisplayName("通过接口调用getBlockSize方法")
        void testGetBlockSizeThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            int blockSize = cipher.getBlockSize();
            assertThat(blockSize).isEqualTo(16);
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            String algorithm = cipher.getAlgorithm();
            assertThat(algorithm).contains("AES");
        }

        @Test
        @DisplayName("通过接口调用getIvLength方法")
        void testGetIvLengthThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            int ivLength = cipher.getIvLength();
            assertThat(ivLength).isEqualTo(16);
        }

        @Test
        @DisplayName("通过接口调用generateKey方法")
        void testGenerateKeyThroughInterface() {
            SymmetricCipher cipher = AesCipher.cbc();
            SecretKey key = cipher.generateKey(256);
            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("AES");
        }
    }

    @Nested
    @DisplayName("Fluent API Tests / 流式API测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式调用设置方法")
        void testChainedSetters() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher cipher = AesCipher.cbc()
                    .setKey(AES_KEY)
                    .setIv(iv)
                    .setMode(CipherMode.CBC)
                    .setPadding(Padding.PKCS7);

            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同模式通过接口引用")
        void testDifferentModes() {
            SymmetricCipher cbcCipher = AesCipher.cbc();
            SymmetricCipher ctrCipher = AesCipher.ctr();

            assertThat(cbcCipher.getAlgorithm()).contains("AES");
            assertThat(ctrCipher.getAlgorithm()).contains("AES");
        }

        @Test
        @DisplayName("通过接口数组批量加解密")
        void testBatchEncryption() {
            byte[] iv = AesCipher.cbc().generateIv();
            SymmetricCipher[] ciphers = {
                    AesCipher.cbc().setKey(AES_KEY).setIv(iv),
                    AesCipher.ctr().setKey(AES_KEY).setIv(iv)
            };

            for (SymmetricCipher cipher : ciphers) {
                byte[] encrypted = cipher.encrypt(TEST_DATA);
                String decrypted = cipher.decryptToString(encrypted);
                assertThat(decrypted).isEqualTo(TEST_DATA);
            }
        }
    }
}
