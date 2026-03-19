package cloud.opencode.base.crypto.asymmetric;

import cloud.opencode.base.crypto.enums.CurveType;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

import static org.assertj.core.api.Assertions.*;

/**
 * EccCipher 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("EccCipher 测试")
class EccCipherTest {

    private static final String TEST_MESSAGE = "Hello, ECC!";

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("p256创建实例")
        void testP256() {
            EccCipher cipher = EccCipher.p256();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getCurveType()).isEqualTo(CurveType.P_256);
            assertThat(cipher.getAlgorithm()).contains("secp256r1");
        }

        @Test
        @DisplayName("p384创建实例")
        void testP384() {
            EccCipher cipher = EccCipher.p384();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getCurveType()).isEqualTo(CurveType.P_384);
            assertThat(cipher.getAlgorithm()).contains("secp384r1");
        }

        @Test
        @DisplayName("p521创建实例")
        void testP521() {
            EccCipher cipher = EccCipher.p521();
            assertThat(cipher).isNotNull();
            assertThat(cipher.getCurveType()).isEqualTo(CurveType.P_521);
            assertThat(cipher.getAlgorithm()).contains("secp521r1");
        }

        @Test
        @DisplayName("withCurve创建自定义曲线实例")
        void testWithCurve() {
            EccCipher cipher = EccCipher.withCurve(CurveType.P_384);
            assertThat(cipher).isNotNull();
            assertThat(cipher.getCurveType()).isEqualTo(CurveType.P_384);
        }

        @Test
        @DisplayName("withCurve null抛出异常")
        void testWithCurveNull() {
            assertThatThrownBy(() -> EccCipher.withCurve(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("withGeneratedKeyPair创建带密钥对的实例")
        void testWithGeneratedKeyPair() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            assertThat(cipher).isNotNull();
            assertThat(cipher.getPublicKey()).isNotNull();
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("withGeneratedKeyPair null抛出异常")
        void testWithGeneratedKeyPairNull() {
            assertThatThrownBy(() -> EccCipher.withGeneratedKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("setPublicKey测试")
    class SetPublicKeyTests {

        @Test
        @DisplayName("setPublicKey(PublicKey)")
        void testSetPublicKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EccCipher cipher = EccCipher.p256();
            cipher.setPublicKey(keyPair.getPublic());
            assertThat(cipher.getPublicKey()).isEqualTo(keyPair.getPublic());
        }

        @Test
        @DisplayName("setPublicKey(byte[])")
        void testSetPublicKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPublic().getEncoded();
            EccCipher cipher = EccCipher.p256();
            cipher.setPublicKey(encoded);
            assertThat(cipher.getPublicKey()).isNotNull();
        }

        @Test
        @DisplayName("setPublicKey null抛出异常")
        void testSetPublicKeyNull() {
            assertThatThrownBy(() -> EccCipher.p256().setPublicKey((java.security.PublicKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKey null字节数组抛出异常")
        void testSetPublicKeyNullBytes() {
            assertThatThrownBy(() -> EccCipher.p256().setPublicKey((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem null抛出异常")
        void testSetPublicKeyPemNull() {
            assertThatThrownBy(() -> EccCipher.p256().setPublicKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPublicKeyPem无效格式抛出异常")
        void testSetPublicKeyPemInvalid() {
            assertThatThrownBy(() -> EccCipher.p256().setPublicKeyPem("invalid-pem"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("setPublicKey非EC密钥抛出异常")
        void testSetPublicKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> EccCipher.p256().setPublicKey(keyPair.getPublic()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }
    }

    @Nested
    @DisplayName("setPrivateKey测试")
    class SetPrivateKeyTests {

        @Test
        @DisplayName("setPrivateKey(PrivateKey)")
        void testSetPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EccCipher cipher = EccCipher.p256();
            cipher.setPrivateKey(keyPair.getPrivate());
            assertThat(cipher.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setPrivateKey(byte[])")
        void testSetPrivateKeyBytes() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            byte[] encoded = keyPair.getPrivate().getEncoded();
            EccCipher cipher = EccCipher.p256();
            cipher.setPrivateKey(encoded);
            assertThat(cipher.getPrivateKey()).isNotNull();
        }

        @Test
        @DisplayName("setPrivateKey null抛出异常")
        void testSetPrivateKeyNull() {
            assertThatThrownBy(() -> EccCipher.p256().setPrivateKey((java.security.PrivateKey) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKeyPem null抛出异常")
        void testSetPrivateKeyPemNull() {
            assertThatThrownBy(() -> EccCipher.p256().setPrivateKeyPem(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("setPrivateKey非EC密钥抛出异常")
        void testSetPrivateKeyWrongAlgorithm() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            assertThatThrownBy(() -> EccCipher.p256().setPrivateKey(keyPair.getPrivate()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EC");
        }
    }

    @Nested
    @DisplayName("setKeyPair测试")
    class SetKeyPairTests {

        @Test
        @DisplayName("setKeyPair设置公私钥")
        void testSetKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EccCipher cipher = EccCipher.p256();
            cipher.setKeyPair(keyPair);
            assertThat(cipher.getPublicKey()).isEqualTo(keyPair.getPublic());
            assertThat(cipher.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        }

        @Test
        @DisplayName("setKeyPair null抛出异常")
        void testSetKeyPairNull() {
            assertThatThrownBy(() -> EccCipher.p256().setKeyPair(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("加密解密测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt和decrypt字节数组")
        void testEncryptDecryptBytes() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            byte[] ciphertext = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("encrypt和decrypt字符串")
        void testEncryptDecryptString() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            byte[] ciphertext = cipher.encrypt(TEST_MESSAGE);
            String decrypted = cipher.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("不同曲线加密解密")
        void testDifferentCurves() {
            // P-256
            EccCipher cipher256 = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] ciphertext = cipher256.encrypt(TEST_MESSAGE);
            assertThat(cipher256.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);

            // P-384
            EccCipher cipher384 = EccCipher.withGeneratedKeyPair(CurveType.P_384);
            ciphertext = cipher384.encrypt(TEST_MESSAGE);
            assertThat(cipher384.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);

            // P-521
            EccCipher cipher521 = EccCipher.withGeneratedKeyPair(CurveType.P_521);
            ciphertext = cipher521.encrypt(TEST_MESSAGE);
            assertThat(cipher521.decryptToString(ciphertext)).isEqualTo(TEST_MESSAGE);
        }

        @Test
        @DisplayName("加密大数据")
        void testEncryptLargeData() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            // ECC can handle larger data because it uses hybrid encryption (ECDH + AES-GCM)
            byte[] largeData = new byte[10000];
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }

            byte[] ciphertext = cipher.encrypt(largeData);
            byte[] decrypted = cipher.decrypt(ciphertext);

            assertThat(decrypted).isEqualTo(largeData);
        }

        @Test
        @DisplayName("encrypt null plaintext抛出异常")
        void testEncryptNullPlaintext() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            assertThatThrownBy(() -> cipher.encrypt((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt null字符串抛出异常")
        void testEncryptNullString() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            assertThatThrownBy(() -> cipher.encrypt((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("encrypt未设置公钥抛出异常")
        void testEncryptWithoutPublicKey() {
            EccCipher cipher = EccCipher.p256();

            assertThatThrownBy(() -> cipher.encrypt("test".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Public key");
        }

        @Test
        @DisplayName("decrypt null ciphertext抛出异常")
        void testDecryptNullCiphertext() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            assertThatThrownBy(() -> cipher.decrypt(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("decrypt未设置私钥抛出异常")
        void testDecryptWithoutPrivateKey() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            EccCipher cipher = EccCipher.p256();
            cipher.setPublicKey(keyPair.getPublic());

            byte[] ciphertext = cipher.encrypt("test".getBytes(StandardCharsets.UTF_8));

            EccCipher decryptCipher = EccCipher.p256();
            assertThatThrownBy(() -> decryptCipher.decrypt(ciphertext))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Private key");
        }

        @Test
        @DisplayName("decrypt错误ciphertext抛出异常")
        void testDecryptInvalidCiphertext() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
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
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            assertThat(base64).matches("[A-Za-z0-9+/=]+");
        }

        @Test
        @DisplayName("decryptBase64")
        void testDecryptBase64() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String base64 = cipher.encryptBase64(plaintext);
            byte[] decrypted = cipher.decryptBase64(base64);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptBase64 null抛出异常")
        void testDecryptBase64Null() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

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
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("decryptHex")
        void testDecryptHex() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);
            byte[] plaintext = TEST_MESSAGE.getBytes(StandardCharsets.UTF_8);

            String hex = cipher.encryptHex(plaintext);
            byte[] decrypted = cipher.decryptHex(hex);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("decryptHex null抛出异常")
        void testDecryptHexNull() {
            EccCipher cipher = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            assertThatThrownBy(() -> cipher.decryptHex(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getAlgorithm包含ECIES")
        void testGetAlgorithm() {
            EccCipher cipher = EccCipher.p256();
            assertThat(cipher.getAlgorithm()).contains("ECIES");
        }

        @Test
        @DisplayName("getMaxEncryptSize返回-1（无限制）")
        void testGetMaxEncryptSize() {
            EccCipher cipher = EccCipher.p256();
            // ECC with hybrid encryption has no practical limit
            assertThat(cipher.getMaxEncryptSize()).isEqualTo(-1);
        }

        @Test
        @DisplayName("generateKeyPair")
        void testGenerateKeyPair() {
            EccCipher cipher = EccCipher.p256();
            KeyPair keyPair = cipher.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
        }

        @Test
        @DisplayName("getCurveType")
        void testGetCurveType() {
            EccCipher cipher = EccCipher.p384();
            assertThat(cipher.getCurveType()).isEqualTo(CurveType.P_384);
        }

        @Test
        @DisplayName("实现AsymmetricCipher接口")
        void testImplementsAsymmetricCipher() {
            EccCipher cipher = EccCipher.p256();
            assertThat(cipher).isInstanceOf(AsymmetricCipher.class);
        }
    }

    @Nested
    @DisplayName("密钥互操作测试")
    class KeyInteroperabilityTests {

        @Test
        @DisplayName("不同实例使用相同密钥对")
        void testKeyInteroperability() {
            EccCipher cipher1 = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            // Create another cipher with same keys
            EccCipher cipher2 = EccCipher.p256();
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
            EccCipher cipher1 = EccCipher.withGeneratedKeyPair(CurveType.P_256);

            byte[] publicKeyBytes = cipher1.getPublicKey().getEncoded();
            byte[] privateKeyBytes = cipher1.getPrivateKey().getEncoded();

            // Create another cipher with transferred keys
            EccCipher cipher2 = EccCipher.p256();
            cipher2.setPublicKey(publicKeyBytes);
            cipher2.setPrivateKey(privateKeyBytes);

            // Encrypt with cipher1, decrypt with cipher2
            byte[] ciphertext = cipher1.encrypt(TEST_MESSAGE);
            String decrypted = cipher2.decryptToString(ciphertext);

            assertThat(decrypted).isEqualTo(TEST_MESSAGE);
        }
    }
}
