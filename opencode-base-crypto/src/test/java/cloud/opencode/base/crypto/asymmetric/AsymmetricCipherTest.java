package cloud.opencode.base.crypto.asymmetric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link AsymmetricCipher} interface.
 * AsymmetricCipher接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("AsymmetricCipher Interface Tests / AsymmetricCipher接口测试")
class AsymmetricCipherTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();

    private KeyPair rsaKeyPair;

    @BeforeEach
    void setUp() {
        rsaKeyPair = RsaCipher.create().generateKeyPair();
    }

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("RsaCipher实现AsymmetricCipher接口")
        void testRsaCipherImplementsInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }

        @Test
        @DisplayName("RsaOaepCipher实现AsymmetricCipher接口")
        void testRsaOaepCipherImplementsInterface() {
            AsymmetricCipher cipher = RsaOaepCipher.sha256();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }

        @Test
        @DisplayName("EccCipher实现AsymmetricCipher接口")
        void testEccCipherImplementsInterface() {
            AsymmetricCipher cipher = EccCipher.p256();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }

        @Test
        @DisplayName("通过接口调用setPublicKey方法")
        void testSetPublicKeyThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            AsymmetricCipher result = cipher.setPublicKey(rsaKeyPair.getPublic());
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setPrivateKey方法")
        void testSetPrivateKeyThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            AsymmetricCipher result = cipher.setPrivateKey(rsaKeyPair.getPrivate());
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setKeyPair方法")
        void testSetKeyPairThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            AsymmetricCipher result = cipher.setKeyPair(rsaKeyPair);
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setPublicKey(byte[])方法")
        void testSetPublicKeyBytesThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            AsymmetricCipher result = cipher.setPublicKey(rsaKeyPair.getPublic().getEncoded());
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用setPrivateKey(byte[])方法")
        void testSetPrivateKeyBytesThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            AsymmetricCipher result = cipher.setPrivateKey(rsaKeyPair.getPrivate().getEncoded());
            assertThat(result).isSameAs(cipher);
        }

        @Test
        @DisplayName("通过接口调用encrypt(byte[])方法")
        void testEncryptBytesThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用encrypt(String)方法")
        void testEncryptStringThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用decrypt方法")
        void testDecryptThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setKeyPair(rsaKeyPair);
            byte[] encrypted = cipher.encrypt(TEST_BYTES);
            byte[] decrypted = cipher.decrypt(encrypted);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用decryptToString方法")
        void testDecryptToStringThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setKeyPair(rsaKeyPair);
            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }

        @Test
        @DisplayName("通过接口调用encryptBase64方法")
        void testEncryptBase64ThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            String base64 = cipher.encryptBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用decryptBase64方法")
        void testDecryptBase64ThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setKeyPair(rsaKeyPair);
            String base64 = cipher.encryptBase64(TEST_BYTES);
            byte[] decrypted = cipher.decryptBase64(base64);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用encryptHex方法")
        void testEncryptHexThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            String hex = cipher.encryptHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("通过接口调用decryptHex方法")
        void testDecryptHexThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setKeyPair(rsaKeyPair);
            String hex = cipher.encryptHex(TEST_BYTES);
            byte[] decrypted = cipher.decryptHex(hex);
            assertThat(decrypted).isEqualTo(TEST_BYTES);
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            String algorithm = cipher.getAlgorithm();
            assertThat(algorithm).contains("RSA");
        }

        @Test
        @DisplayName("通过接口调用getMaxEncryptSize方法")
        void testGetMaxEncryptSizeThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            int maxSize = cipher.getMaxEncryptSize();
            assertThat(maxSize).isGreaterThan(0);
        }

        @Test
        @DisplayName("通过接口调用generateKeyPair方法")
        void testGenerateKeyPairThroughInterface() {
            AsymmetricCipher cipher = RsaCipher.create();
            KeyPair keyPair = cipher.generateKeyPair();
            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Fluent API Tests / 流式API测试")
    class FluentApiTests {

        @Test
        @DisplayName("链式调用设置方法")
        void testChainedSetters() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic())
                    .setPrivateKey(rsaKeyPair.getPrivate());

            byte[] encrypted = cipher.encrypt(TEST_DATA);
            String decrypted = cipher.decryptToString(encrypted);
            assertThat(decrypted).isEqualTo(TEST_DATA);
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同实现通过接口引用")
        void testDifferentImplementations() {
            AsymmetricCipher rsa = RsaCipher.create();
            AsymmetricCipher oaep = RsaOaepCipher.sha256();
            AsymmetricCipher ecc = EccCipher.p256();

            assertThat(rsa.getAlgorithm()).contains("RSA");
            assertThat(oaep.getAlgorithm()).contains("RSA");
            assertThat(ecc.getAlgorithm()).isNotNull();
        }

        @Test
        @DisplayName("通过接口数组批量生成密钥对")
        void testBatchKeyGeneration() {
            AsymmetricCipher[] ciphers = {
                    RsaCipher.create(),
                    RsaOaepCipher.sha256(),
                    EccCipher.p256()
            };

            for (AsymmetricCipher cipher : ciphers) {
                KeyPair keyPair = cipher.generateKeyPair();
                assertThat(keyPair).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("PEM Key Tests / PEM密钥测试")
    class PemKeyTests {

        @Test
        @DisplayName("通过接口调用setPublicKeyPem方法")
        void testSetPublicKeyPemThroughInterface() {
            // Generate PEM formatted key
            AsymmetricCipher cipher = RsaCipher.create();
            KeyPair keyPair = cipher.generateKeyPair();

            // Get PEM string (simplified - in real code use PemCodec)
            String pemHeader = "-----BEGIN PUBLIC KEY-----\n";
            String pemFooter = "\n-----END PUBLIC KEY-----";
            String base64 = java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String pem = pemHeader + base64 + pemFooter;

            AsymmetricCipher result = cipher.setPublicKeyPem(pem);
            assertThat(result).isSameAs(cipher);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests / 错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("未设置公钥时加密抛出异常")
        void testEncryptWithoutPublicKeyThrows() {
            AsymmetricCipher cipher = RsaCipher.create();
            assertThatThrownBy(() -> cipher.encrypt(TEST_BYTES))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("未设置私钥时解密抛出异常")
        void testDecryptWithoutPrivateKeyThrows() {
            AsymmetricCipher cipher = RsaCipher.create()
                    .setPublicKey(rsaKeyPair.getPublic());
            byte[] encrypted = cipher.encrypt(TEST_BYTES);

            AsymmetricCipher decryptCipher = RsaCipher.create();
            assertThatThrownBy(() -> decryptCipher.decrypt(encrypted))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
