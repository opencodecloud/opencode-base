package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.password.Argon2Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Argon2Kdf 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Argon2Kdf 测试")
class Argon2KdfTest {

    private static final byte[] TEST_PASSWORD = "password123".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_SALT = "random-salt-16b!".getBytes(StandardCharsets.UTF_8);

    /**
     * Check if Bouncy Castle is available
     */
    static boolean isBouncyCastleAvailable() {
        return Argon2Kdf.isBouncyCastleAvailable();
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("argon2id创建实例")
        void testArgon2id() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();
            assertThat(kdf).isNotNull();
            assertThat(kdf.getType()).isEqualTo(Argon2Type.ARGON2ID);
            assertThat(kdf.getMemory()).isEqualTo(65536);
            assertThat(kdf.getIterations()).isEqualTo(3);
            assertThat(kdf.getParallelism()).isEqualTo(4);
        }

        @Test
        @DisplayName("argon2d创建实例")
        void testArgon2d() {
            Argon2Kdf kdf = Argon2Kdf.argon2d();
            assertThat(kdf).isNotNull();
            assertThat(kdf.getType()).isEqualTo(Argon2Type.ARGON2D);
        }

        @Test
        @DisplayName("argon2i创建实例")
        void testArgon2i() {
            Argon2Kdf kdf = Argon2Kdf.argon2i();
            assertThat(kdf).isNotNull();
            assertThat(kdf.getType()).isEqualTo(Argon2Type.ARGON2I);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("Builder默认值")
        void testBuilderDefaults() {
            Argon2Kdf kdf = Argon2Kdf.builder().build();
            assertThat(kdf.getType()).isEqualTo(Argon2Type.ARGON2ID);
            assertThat(kdf.getMemory()).isEqualTo(65536);
            assertThat(kdf.getIterations()).isEqualTo(3);
            assertThat(kdf.getParallelism()).isEqualTo(4);
        }

        @Test
        @DisplayName("Builder自定义值")
        void testBuilderCustom() {
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .type(Argon2Type.ARGON2I)
                    .memory(32768)
                    .iterations(5)
                    .parallelism(2)
                    .build();

            assertThat(kdf.getType()).isEqualTo(Argon2Type.ARGON2I);
            assertThat(kdf.getMemory()).isEqualTo(32768);
            assertThat(kdf.getIterations()).isEqualTo(5);
            assertThat(kdf.getParallelism()).isEqualTo(2);
        }

        @Test
        @DisplayName("Builder null type抛出异常")
        void testBuilderNullType() {
            assertThatThrownBy(() -> Argon2Kdf.builder().type(null).build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Builder无效迭代次数抛出异常")
        void testBuilderInvalidIterations() {
            assertThatThrownBy(() -> Argon2Kdf.builder().iterations(0).build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Builder无效内存抛出异常")
        void testBuilderInvalidMemory() {
            assertThatThrownBy(() -> Argon2Kdf.builder().memory(4).build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Builder无效并行度抛出异常")
        void testBuilderInvalidParallelism() {
            assertThatThrownBy(() -> Argon2Kdf.builder().parallelism(0).build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("KdfEngine接口测试")
    class KdfEngineTests {

        @Test
        @DisplayName("实现KdfEngine接口")
        void testImplementsKdfEngine() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();
            assertThat(kdf).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            assertThat(Argon2Kdf.argon2id().getAlgorithm()).isEqualTo("Argon2-ARGON2ID");
            assertThat(Argon2Kdf.argon2d().getAlgorithm()).isEqualTo("Argon2-ARGON2D");
            assertThat(Argon2Kdf.argon2i().getAlgorithm()).isEqualTo("Argon2-ARGON2I");
        }
    }

    @Nested
    @DisplayName("generateSalt测试")
    class GenerateSaltTests {

        @Test
        @DisplayName("generateSalt默认长度")
        void testGenerateSaltDefault() {
            byte[] salt = Argon2Kdf.generateSalt();
            assertThat(salt).hasSize(16);
        }

        @Test
        @DisplayName("generateSalt自定义长度")
        void testGenerateSaltCustomLength() {
            byte[] salt = Argon2Kdf.generateSalt(32);
            assertThat(salt).hasSize(32);
        }

        @Test
        @DisplayName("generateSalt生成不同值")
        void testGenerateSaltDifferent() {
            byte[] salt1 = Argon2Kdf.generateSalt();
            byte[] salt2 = Argon2Kdf.generateSalt();
            assertThat(salt1).isNotEqualTo(salt2);
        }

        @Test
        @DisplayName("generateSalt长度太短抛出异常")
        void testGenerateSaltTooShort() {
            assertThatThrownBy(() -> Argon2Kdf.generateSalt(8))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("静态属性测试")
    class StaticPropertyTests {

        @Test
        @DisplayName("getDefaultSaltLength")
        void testGetDefaultSaltLength() {
            assertThat(Argon2Kdf.getDefaultSaltLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("getMinMemory")
        void testGetMinMemory() {
            assertThat(Argon2Kdf.getMinMemory()).isEqualTo(8);
        }

        @Test
        @DisplayName("isBouncyCastleAvailable")
        void testIsBouncyCastleAvailable() {
            // Just check it returns a boolean
            boolean available = Argon2Kdf.isBouncyCastleAvailable();
            assertThat(available).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("deriveKey测试 (byte[])")
    class DeriveKeyBytesTests {

        @Test
        @DisplayName("deriveKey基本功能")
        void testDeriveKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            byte[] key = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey确定性")
        void testDeriveKeyDeterministic() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] key1 = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] key2 = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同密码产生不同结果")
        void testDeriveKeyDifferentPasswords() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] key1 = kdf.deriveKey("password1".getBytes(StandardCharsets.UTF_8), TEST_SALT, 32);
            byte[] key2 = kdf.deriveKey("password2".getBytes(StandardCharsets.UTF_8), TEST_SALT, 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同盐值产生不同结果")
        void testDeriveKeyDifferentSalts() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] key1 = kdf.deriveKey(TEST_PASSWORD, "salt1-random-16!".getBytes(StandardCharsets.UTF_8), 32);
            byte[] key2 = kdf.deriveKey(TEST_PASSWORD, "salt2-random-16!".getBytes(StandardCharsets.UTF_8), 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey多种长度")
        void testDeriveKeyVariousLengths() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            assertThat(kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 16)).hasSize(16);
            assertThat(kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32)).hasSize(32);
            assertThat(kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 64)).hasSize(64);
        }

        @Test
        @DisplayName("deriveKey null password抛出异常")
        void testDeriveKeyNullPassword() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            assertThatThrownBy(() -> kdf.deriveKey((byte[]) null, TEST_SALT, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey null salt抛出异常")
        void testDeriveKeyNullSalt() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            assertThatThrownBy(() -> kdf.deriveKey(TEST_PASSWORD, null, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey无效长度抛出异常")
        void testDeriveKeyInvalidLength() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            assertThatThrownBy(() -> kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> kdf.deriveKey(TEST_PASSWORD, TEST_SALT, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey盐值太短抛出异常")
        void testDeriveKeyShortSalt() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();
            byte[] shortSalt = "short".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> kdf.deriveKey(TEST_PASSWORD, shortSalt, 32))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deriveKey测试 (char[])")
    class DeriveKeyCharTests {

        @Test
        @DisplayName("deriveKey char[]")
        void testDeriveKeyChar() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            byte[] key = kdf.deriveKey("password".toCharArray(), TEST_SALT, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey null char[]抛出异常")
        void testDeriveKeyNullChar() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            assertThatThrownBy(() -> kdf.deriveKey((char[]) null, TEST_SALT, 32))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deriveKey测试 (String)")
    class DeriveKeyStringTests {

        @Test
        @DisplayName("deriveKey String")
        void testDeriveKeyString() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            byte[] key = kdf.deriveKey("password", TEST_SALT, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey null String抛出异常")
        void testDeriveKeyNullString() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            assertThatThrownBy(() -> kdf.deriveKey((String) null, TEST_SALT, 32))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deriveKey带附加数据测试")
    class DeriveKeyWithAdditionalDataTests {

        @Test
        @DisplayName("deriveKey带secret和ad")
        void testDeriveKeyWithSecretAndAd() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] secret = "secret-key".getBytes(StandardCharsets.UTF_8);
            byte[] ad = "additional-data".getBytes(StandardCharsets.UTF_8);
            byte[] key = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, secret, ad, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey带null secret")
        void testDeriveKeyWithNullSecret() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] ad = "additional-data".getBytes(StandardCharsets.UTF_8);
            byte[] key = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, null, ad, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey带null ad")
        void testDeriveKeyWithNullAd() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] secret = "secret-key".getBytes(StandardCharsets.UTF_8);
            byte[] key = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, secret, null, 32);

            assertThat(key).hasSize(32);
        }
    }

    @Nested
    @DisplayName("KdfEngine derive测试")
    class KdfEngineDeriveTests {

        @Test
        @DisplayName("derive(ikm, salt, info, length)")
        void testDeriveWithAllParams() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            byte[] result = kdf.derive(TEST_PASSWORD, TEST_SALT, null, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive(ikm, length)")
        void testDeriveSimple() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf = Argon2Kdf.builder()
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();
            byte[] result = kdf.derive(TEST_PASSWORD, 32);

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("不同算法类型测试")
    class AlgorithmTypeTests {

        @Test
        @DisplayName("不同类型产生不同结果")
        void testDifferentTypesDifferentResults() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Argon2Kdf kdf2d = Argon2Kdf.builder()
                    .type(Argon2Type.ARGON2D)
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            Argon2Kdf kdf2i = Argon2Kdf.builder()
                    .type(Argon2Type.ARGON2I)
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            Argon2Kdf kdf2id = Argon2Kdf.builder()
                    .type(Argon2Type.ARGON2ID)
                    .memory(16384)
                    .iterations(2)
                    .parallelism(1)
                    .build();

            byte[] result2d = kdf2d.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] result2i = kdf2i.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] result2id = kdf2id.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(result2d).isNotEqualTo(result2i);
            assertThat(result2i).isNotEqualTo(result2id);
            assertThat(result2d).isNotEqualTo(result2id);
        }
    }

    @Nested
    @DisplayName("无Bouncy Castle测试")
    class NoBouncyCastleTests {

        @Test
        @DisplayName("没有Bouncy Castle时deriveKey抛出异常或成功")
        void testDeriveKeyWithoutBouncyCastle() {
            Argon2Kdf kdf = Argon2Kdf.argon2id();

            if (!isBouncyCastleAvailable()) {
                assertThatThrownBy(() -> kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32))
                        .isInstanceOf(OpenCryptoException.class)
                        .hasMessageContaining("Bouncy Castle");
            } else {
                // If BC is available, it should work
                byte[] key = kdf.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
                assertThat(key).hasSize(32);
            }
        }
    }
}
