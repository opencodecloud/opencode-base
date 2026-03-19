package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link OpenAsymmetric}.
 * OpenAsymmetric单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenAsymmetric Tests / OpenAsymmetric测试")
class OpenAsymmetricTest {

    private static KeyPair rsaKeyPair;

    @BeforeAll
    static void setup() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        rsaKeyPair = keyGen.generateKeyPair();
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("rsaOaep创建RSA-OAEP实例")
        void testRsaOaep() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            assertThat(crypto).isNotNull();
            assertThat(crypto.getAlgorithm()).containsIgnoringCase("RSA");
        }

        @Test
        @DisplayName("rsa创建RSA-PKCS1实例")
        void testRsa() {
            OpenAsymmetric crypto = OpenAsymmetric.rsa();

            assertThat(crypto).isNotNull();
            assertThat(crypto.getAlgorithm()).containsIgnoringCase("RSA");
        }

        @Test
        @DisplayName("ecc创建ECC实例")
        void testEcc() {
            OpenAsymmetric crypto = OpenAsymmetric.ecc();

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("sm2创建SM2实例")
        void testSm2() {
            OpenAsymmetric crypto = OpenAsymmetric.sm2();

            assertThat(crypto).isNotNull();
            assertThat(crypto.getAlgorithm()).containsIgnoringCase("SM2");
        }

        @Test
        @DisplayName("of(RSA_PKCS1)创建RSA实例")
        void testOfRsaPkcs1() {
            OpenAsymmetric crypto = OpenAsymmetric.of(AsymmetricAlgorithm.RSA_PKCS1);

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("of(RSA_OAEP_SHA256)创建RSA-OAEP实例")
        void testOfRsaOaepSha256() {
            OpenAsymmetric crypto = OpenAsymmetric.of(AsymmetricAlgorithm.RSA_OAEP_SHA256);

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("of(RSA_OAEP_SHA384)创建RSA-OAEP实例")
        void testOfRsaOaepSha384() {
            OpenAsymmetric crypto = OpenAsymmetric.of(AsymmetricAlgorithm.RSA_OAEP_SHA384);

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("of(RSA_OAEP_SHA512)创建RSA-OAEP实例")
        void testOfRsaOaepSha512() {
            OpenAsymmetric crypto = OpenAsymmetric.of(AsymmetricAlgorithm.RSA_OAEP_SHA512);

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("of(SM2)创建SM2实例")
        void testOfSm2() {
            OpenAsymmetric crypto = OpenAsymmetric.of(AsymmetricAlgorithm.SM2);

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("of(null)抛出异常")
        void testOfNullThrows() {
            assertThatThrownBy(() -> OpenAsymmetric.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Algorithm");
        }
    }

    @Nested
    @DisplayName("Key Configuration Tests / 密钥配置测试")
    class KeyConfigurationTests {

        @Test
        @DisplayName("setPrivateKey设置私钥")
        void testSetPrivateKey() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            OpenAsymmetric result = crypto.setPrivateKey(rsaKeyPair.getPrivate());

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            assertThatThrownBy(() -> crypto.setPrivateKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("setPublicKey设置公钥")
        void testSetPublicKey() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            OpenAsymmetric result = crypto.setPublicKey(rsaKeyPair.getPublic());

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            assertThatThrownBy(() -> crypto.setPublicKey(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("setKeyPair设置密钥对")
        void testSetKeyPair() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            OpenAsymmetric result = crypto.setKeyPair(rsaKeyPair);

            assertThat(result).isSameAs(crypto); // Fluent API
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            assertThatThrownBy(() -> crypto.setKeyPair(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Key pair");
        }
    }

    @Nested
    @DisplayName("Encryption Tests / 加密测试")
    class EncryptionTests {

        @Test
        @DisplayName("encrypt(byte[])加密字节数组")
        void testEncryptBytes() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            byte[] plaintext = "Hello, RSA!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = crypto.encrypt(plaintext);

            assertThat(encrypted).isNotNull().isNotEmpty();
            assertThat(encrypted).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt(String)加密字符串")
        void testEncryptString() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            byte[] encrypted = crypto.encrypt("Hello, RSA!");

            assertThat(encrypted).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encrypt plaintext为null抛出异常")
        void testEncryptNullBytesThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            assertThatThrownBy(() -> crypto.encrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Plaintext");
        }

        @Test
        @DisplayName("encrypt string为null抛出异常")
        void testEncryptNullStringThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            assertThatThrownBy(() -> crypto.encrypt((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Plaintext");
        }

        @Test
        @DisplayName("未设置公钥encrypt抛出异常")
        void testEncryptWithoutPublicKeyThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            assertThatThrownBy(() -> crypto.encrypt("test".getBytes()))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("encryptHex(byte[])返回十六进制字符串")
        void testEncryptHexBytes() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            String hex = crypto.encryptHex("test".getBytes());

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9a-fA-F]+");
        }

        @Test
        @DisplayName("encryptHex(String)返回十六进制字符串")
        void testEncryptHexString() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            String hex = crypto.encryptHex("test");

            assertThat(hex).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encryptBase64(byte[])返回Base64字符串")
        void testEncryptBase64Bytes() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            String base64 = crypto.encryptBase64("test".getBytes());

            assertThat(base64).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encryptBase64(String)返回Base64字符串")
        void testEncryptBase64String() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            String base64 = crypto.encryptBase64("test");

            assertThat(base64).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Decryption Tests / 解密测试")
    class DecryptionTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decrypt ciphertext为null抛出异常")
        void testDecryptNullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decrypt(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Ciphertext");
        }

        @Test
        @DisplayName("未设置私钥decrypt抛出异常")
        void testDecryptWithoutPrivateKeyThrows() {
            OpenAsymmetric encryptor = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = encryptor.encrypt("test".getBytes());

            OpenAsymmetric decryptor = OpenAsymmetric.rsaOaep();

            assertThatThrownBy(() -> decryptor.decrypt(encrypted))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("decryptToString解密为字符串")
        void testDecryptToString() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            String plaintext = "Decrypt to string!";
            byte[] encrypted = crypto.encrypt(plaintext);
            String decrypted = crypto.decryptToString(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex解密十六进制字符串")
        void testDecryptHex() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            byte[] plaintext = "Hex test".getBytes(StandardCharsets.UTF_8);
            String hex = crypto.encryptHex(plaintext);
            byte[] decrypted = crypto.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex null抛出异常")
        void testDecryptHexNullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decryptHex(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Ciphertext");
        }

        @Test
        @DisplayName("decryptHexToString解密为字符串")
        void testDecryptHexToString() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            String plaintext = "Hex to string!";
            String hex = crypto.encryptHex(plaintext);
            String decrypted = crypto.decryptHexToString(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64解密Base64字符串")
        void testDecryptBase64() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            byte[] plaintext = "Base64 test".getBytes(StandardCharsets.UTF_8);
            String base64 = crypto.encryptBase64(plaintext);
            byte[] decrypted = crypto.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64NullThrows() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            assertThatThrownBy(() -> crypto.decryptBase64(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Ciphertext");
        }

        @Test
        @DisplayName("decryptBase64ToString解密为字符串")
        void testDecryptBase64ToString() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            String plaintext = "Base64 to string!";
            String base64 = crypto.encryptBase64(plaintext);
            String decrypted = crypto.decryptBase64ToString(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair生成密钥对")
        void testGenerateKeyPair() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            KeyPair keyPair = crypto.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair生成并设置密钥对")
        void testWithGeneratedKeyPair() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .withGeneratedKeyPair();

            // Should be able to encrypt and decrypt
            byte[] plaintext = "Generated key test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Info Method Tests / 信息方法测试")
    class InfoMethodTests {

        @Test
        @DisplayName("getAlgorithm返回算法名称")
        void testGetAlgorithm() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            String algorithm = crypto.getAlgorithm();

            assertThat(algorithm).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("getMaxEncryptSize返回最大加密大小")
        void testGetMaxEncryptSize() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep();

            int maxSize = crypto.getMaxEncryptSize();

            assertThat(maxSize).isNotEqualTo(0);
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("RSA-OAEP完整加密解密流程")
        void testRsaOaepEndToEnd() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .withGeneratedKeyPair();

            byte[] plaintext = "RSA-OAEP end-to-end test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("RSA-PKCS1完整加密解密流程")
        void testRsaPkcs1EndToEnd() {
            OpenAsymmetric crypto = OpenAsymmetric.rsa()
                    .withGeneratedKeyPair();

            byte[] plaintext = "RSA-PKCS1 end-to-end test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("SM2完整加密解密流程")
        void testSm2EndToEnd() {
            OpenAsymmetric crypto = OpenAsymmetric.sm2()
                    .withGeneratedKeyPair();

            byte[] plaintext = "SM2 end-to-end test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            OpenAsymmetric crypto = OpenAsymmetric.rsaOaep()
                    .setKeyPair(rsaKeyPair);

            String plaintext = "你好世界 Special: <>&\"'";

            byte[] encrypted = crypto.encrypt(plaintext);
            String decrypted = crypto.decryptToString(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("分离的加密器和解密器")
        void testSeparateEncryptorDecryptor() {
            OpenAsymmetric encryptor = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            OpenAsymmetric decryptor = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            byte[] plaintext = "Separate encryptor/decryptor test".getBytes();

            byte[] encrypted = encryptor.encrypt(plaintext);
            byte[] decrypted = decryptor.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("通过Hex传输")
        void testHexTransfer() {
            OpenAsymmetric encryptor = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            OpenAsymmetric decryptor = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            String plaintext = "Hex transfer test";

            String hex = encryptor.encryptHex(plaintext);
            String decrypted = decryptor.decryptHexToString(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("通过Base64传输")
        void testBase64Transfer() {
            OpenAsymmetric encryptor = OpenAsymmetric.rsaOaep()
                    .setPublicKey(rsaKeyPair.getPublic());

            OpenAsymmetric decryptor = OpenAsymmetric.rsaOaep()
                    .setPrivateKey(rsaKeyPair.getPrivate());

            String plaintext = "Base64 transfer test";

            String base64 = encryptor.encryptBase64(plaintext);
            String decrypted = decryptor.decryptBase64ToString(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
