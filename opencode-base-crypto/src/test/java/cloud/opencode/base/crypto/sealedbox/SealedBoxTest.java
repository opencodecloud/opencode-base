package cloud.opencode.base.crypto.sealedbox;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link SealedBox}.
 * SealedBox单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("SealedBox Tests / SealedBox测试")
class SealedBoxTest {

    private static KeyPair testKeyPair;

    @BeforeAll
    static void setup() {
        testKeyPair = SealedBox.generateKeyPair();
    }

    @Nested
    @DisplayName("Key Generation Tests / 密钥生成测试")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKeyPair生成X25519密钥对")
        void testGenerateKeyPair() {
            KeyPair keyPair = SealedBox.generateKeyPair();

            assertThat(keyPair).isNotNull();
            assertThat(keyPair.getPublic()).isNotNull();
            assertThat(keyPair.getPrivate()).isNotNull();
            // JDK may report algorithm as "X25519" or "XDH"
            assertThat(keyPair.getPublic().getAlgorithm()).isIn("X25519", "XDH");
            assertThat(keyPair.getPrivate().getAlgorithm()).isIn("X25519", "XDH");
        }

        @Test
        @DisplayName("每次生成的密钥对不同")
        void testGenerateKeyPairUnique() {
            KeyPair keyPair1 = SealedBox.generateKeyPair();
            KeyPair keyPair2 = SealedBox.generateKeyPair();

            assertThat(keyPair1.getPublic().getEncoded())
                    .isNotEqualTo(keyPair2.getPublic().getEncoded());
        }
    }

    @Nested
    @DisplayName("Static seal Method Tests / 静态seal方法测试")
    class StaticSealMethodTests {

        @Test
        @DisplayName("seal(byte[], publicKey)加密字节数组")
        void testSealBytes() {
            byte[] plaintext = "Hello, SealedBox!".getBytes(StandardCharsets.UTF_8);

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());

            assertThat(sealed).isNotNull();
            // ephemeral public key (32) + nonce (12) + ciphertext (with 16 byte tag)
            assertThat(sealed.length).isGreaterThan(32 + 12 + 16);
            assertThat(sealed).isNotEqualTo(plaintext);
        }

        @Test
        @DisplayName("seal(String, publicKey)加密字符串")
        void testSealString() {
            String plaintext = "Hello, String SealedBox!";

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());

            assertThat(sealed).isNotNull();
            assertThat(sealed.length).isGreaterThan(plaintext.length());
        }

        @Test
        @DisplayName("相同明文每次seal结果不同(临时密钥)")
        void testSealRandomEphemeralKey() {
            byte[] plaintext = "Same message".getBytes(StandardCharsets.UTF_8);

            byte[] sealed1 = SealedBox.seal(plaintext, testKeyPair.getPublic());
            byte[] sealed2 = SealedBox.seal(plaintext, testKeyPair.getPublic());

            assertThat(sealed1).isNotEqualTo(sealed2);
        }
    }

    @Nested
    @DisplayName("Static open Method Tests / 静态open方法测试")
    class StaticOpenMethodTests {

        @Test
        @DisplayName("open解密成功")
        void testOpen() {
            byte[] plaintext = "Hello, Decryption!".getBytes(StandardCharsets.UTF_8);
            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());

            byte[] opened = SealedBox.open(sealed, testKeyPair);

            assertThat(opened).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("openAsString解密为字符串")
        void testOpenAsString() {
            String plaintext = "Hello, String Decryption!";
            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());

            String opened = SealedBox.openAsString(sealed, testKeyPair);

            assertThat(opened).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Builder Tests / 构建器测试")
    class BuilderTests {

        @Test
        @DisplayName("builder()创建新的Builder实例")
        void testBuilder() {
            SealedBox.Builder builder = SealedBox.builder();

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("build()创建SealedBox实例")
        void testBuild() {
            SealedBox box = SealedBox.builder().build();

            assertThat(box).isNotNull();
        }

        @Test
        @DisplayName("algorithm()设置算法")
        void testAlgorithm() {
            SealedBox box = SealedBox.builder()
                    .algorithm(SealedBox.Algorithm.X25519_AES_GCM)
                    .build();

            assertThat(box).isNotNull();
        }

        @Test
        @DisplayName("algorithm为null抛出异常")
        void testAlgorithmNullThrows() {
            assertThatThrownBy(() -> SealedBox.builder().algorithm(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Instance encrypt Method Tests / 实例encrypt方法测试")
    class InstanceEncryptMethodTests {

        @Test
        @DisplayName("encrypt加密成功")
        void testEncrypt() {
            SealedBox box = SealedBox.builder().build();
            byte[] plaintext = "Instance encrypt test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = box.encrypt(plaintext, testKeyPair.getPublic());

            assertThat(encrypted).isNotNull();
            assertThat(encrypted.length).isGreaterThan(plaintext.length);
        }

        @Test
        @DisplayName("plaintext为null抛出异常")
        void testEncryptNullPlaintextThrows() {
            SealedBox box = SealedBox.builder().build();

            assertThatThrownBy(() -> box.encrypt(null, testKeyPair.getPublic()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("plaintext");
        }

        @Test
        @DisplayName("publicKey为null抛出异常")
        void testEncryptNullPublicKeyThrows() {
            SealedBox box = SealedBox.builder().build();

            assertThatThrownBy(() -> box.encrypt("test".getBytes(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("recipientPublicKey");
        }
    }

    @Nested
    @DisplayName("Instance decrypt Method Tests / 实例decrypt方法测试")
    class InstanceDecryptMethodTests {

        @Test
        @DisplayName("decrypt解密成功")
        void testDecrypt() {
            SealedBox box = SealedBox.builder().build();
            byte[] plaintext = "Instance decrypt test".getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = box.encrypt(plaintext, testKeyPair.getPublic());

            byte[] decrypted = box.decrypt(encrypted, testKeyPair);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("sealed为null抛出异常")
        void testDecryptNullSealedThrows() {
            SealedBox box = SealedBox.builder().build();

            assertThatThrownBy(() -> box.decrypt(null, testKeyPair))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("sealed");
        }

        @Test
        @DisplayName("keyPair为null抛出异常")
        void testDecryptNullKeyPairThrows() {
            SealedBox box = SealedBox.builder().build();
            byte[] encrypted = box.encrypt("test".getBytes(), testKeyPair.getPublic());

            assertThatThrownBy(() -> box.decrypt(encrypted, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("recipientKeyPair");
        }

        @Test
        @DisplayName("数据太短抛出异常")
        void testDecryptTooShortThrows() {
            SealedBox box = SealedBox.builder().build();
            byte[] tooShort = new byte[10]; // Less than ephemeral key + nonce + tag

            assertThatThrownBy(() -> box.decrypt(tooShort, testKeyPair))
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("too short");
        }

        @Test
        @DisplayName("使用错误密钥对解密抛出异常")
        void testDecryptWrongKeyPairThrows() {
            SealedBox box = SealedBox.builder().build();
            byte[] encrypted = box.encrypt("test".getBytes(), testKeyPair.getPublic());
            KeyPair wrongKeyPair = SealedBox.generateKeyPair();

            assertThatThrownBy(() -> box.decrypt(encrypted, wrongKeyPair))
                    .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("篡改的数据解密抛出异常")
        void testDecryptTamperedDataThrows() {
            SealedBox box = SealedBox.builder().build();
            byte[] encrypted = box.encrypt("test".getBytes(), testKeyPair.getPublic());
            // Tamper with data (in the ciphertext area, after public key and nonce)
            encrypted[encrypted.length - 1] ^= 0xFF;

            assertThatThrownBy(() -> box.decrypt(encrypted, testKeyPair))
                    .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Enum Tests / 算法枚举测试")
    class AlgorithmEnumTests {

        @Test
        @DisplayName("X25519_AES_GCM存在")
        void testX25519AesGcmExists() {
            assertThat(SealedBox.Algorithm.X25519_AES_GCM).isNotNull();
        }

        @Test
        @DisplayName("Algorithm枚举值正确")
        void testAlgorithmValues() {
            SealedBox.Algorithm[] values = SealedBox.Algorithm.values();

            assertThat(values).hasSize(1);
            assertThat(values[0]).isEqualTo(SealedBox.Algorithm.X25519_AES_GCM);
        }

        @Test
        @DisplayName("Algorithm valueOf正确")
        void testAlgorithmValueOf() {
            assertThat(SealedBox.Algorithm.valueOf("X25519_AES_GCM"))
                    .isEqualTo(SealedBox.Algorithm.X25519_AES_GCM);
        }
    }

    @Nested
    @DisplayName("End-to-End Tests / 端到端测试")
    class EndToEndTests {

        @Test
        @DisplayName("完整加密解密流程 - 静态方法")
        void testEndToEndStatic() {
            KeyPair keyPair = SealedBox.generateKeyPair();
            byte[] plaintext = "End-to-end static test".getBytes(StandardCharsets.UTF_8);

            byte[] sealed = SealedBox.seal(plaintext, keyPair.getPublic());
            byte[] opened = SealedBox.open(sealed, keyPair);

            assertThat(opened).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整加密解密流程 - 实例方法")
        void testEndToEndInstance() {
            SealedBox box = SealedBox.builder().build();
            KeyPair keyPair = SealedBox.generateKeyPair();
            byte[] plaintext = "End-to-end instance test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = box.encrypt(plaintext, keyPair.getPublic());
            byte[] decrypted = box.decrypt(encrypted, keyPair);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("中文和特殊字符")
        void testUnicodeAndSpecialChars() {
            String plaintext = "你好世界 🎉 Special: <>&\"'";

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());
            String opened = SealedBox.openAsString(sealed, testKeyPair);

            assertThat(opened).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("空字节数组")
        void testEmptyBytes() {
            byte[] plaintext = new byte[0];

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());
            byte[] opened = SealedBox.open(sealed, testKeyPair);

            assertThat(opened).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("大数据加密解密")
        void testLargeData() {
            byte[] plaintext = new byte[100 * 1024]; // 100 KB
            Arrays.fill(plaintext, (byte) 0x42);

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());
            byte[] opened = SealedBox.open(sealed, testKeyPair);

            assertThat(opened).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("匿名性测试 - 发送者使用不同临时密钥")
        void testAnonymity() {
            KeyPair recipient = SealedBox.generateKeyPair();
            byte[] plaintext = "Anonymous message".getBytes(StandardCharsets.UTF_8);

            // Seal twice - should use different ephemeral keys
            byte[] sealed1 = SealedBox.seal(plaintext, recipient.getPublic());
            byte[] sealed2 = SealedBox.seal(plaintext, recipient.getPublic());

            // Ephemeral public keys (first 32 bytes) should be different
            byte[] ephemeral1 = Arrays.copyOf(sealed1, 32);
            byte[] ephemeral2 = Arrays.copyOf(sealed2, 32);

            assertThat(ephemeral1).isNotEqualTo(ephemeral2);

            // But both should decrypt to same plaintext
            assertThat(SealedBox.open(sealed1, recipient)).isEqualTo(plaintext);
            assertThat(SealedBox.open(sealed2, recipient)).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("只需公钥即可加密")
        void testEncryptWithOnlyPublicKey() {
            // Simulate receiving only the public key
            KeyPair fullKeyPair = SealedBox.generateKeyPair();
            java.security.PublicKey publicKeyOnly = fullKeyPair.getPublic();

            byte[] plaintext = "Encrypted with public key only".getBytes(StandardCharsets.UTF_8);

            // Should be able to seal with just the public key
            byte[] sealed = SealedBox.seal(plaintext, publicKeyOnly);

            assertThat(sealed).isNotNull();

            // Recipient can decrypt with full key pair
            byte[] opened = SealedBox.open(sealed, fullKeyPair);
            assertThat(opened).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Cross-Compatibility Tests / 交叉兼容性测试")
    class CrossCompatibilityTests {

        @Test
        @DisplayName("静态方法加密,实例方法解密")
        void testStaticEncryptInstanceDecrypt() {
            SealedBox box = SealedBox.builder().build();
            byte[] plaintext = "Cross compatibility test".getBytes(StandardCharsets.UTF_8);

            byte[] sealed = SealedBox.seal(plaintext, testKeyPair.getPublic());
            byte[] decrypted = box.decrypt(sealed, testKeyPair);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("实例方法加密,静态方法解密")
        void testInstanceEncryptStaticDecrypt() {
            SealedBox box = SealedBox.builder().build();
            byte[] plaintext = "Instance to static test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = box.encrypt(plaintext, testKeyPair.getPublic());
            byte[] decrypted = SealedBox.open(encrypted, testKeyPair);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }
}
