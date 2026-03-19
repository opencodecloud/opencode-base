package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Scrypt 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Scrypt 测试")
class ScryptTest {

    private static final char[] TEST_PASSWORD = "password123".toCharArray();
    private static final byte[] TEST_SALT = "random-salt-16b!".getBytes(StandardCharsets.UTF_8);

    /**
     * Check if Bouncy Castle is available
     */
    static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of()创建默认实例")
        void testOfDefault() {
            Scrypt scrypt = Scrypt.of();
            assertThat(scrypt).isNotNull();
            assertThat(scrypt.getN()).isEqualTo(32768);
            assertThat(scrypt.getR()).isEqualTo(8);
            assertThat(scrypt.getP()).isEqualTo(1);
        }

        @Test
        @DisplayName("of(n,r,p)创建自定义实例")
        void testOfCustom() {
            Scrypt scrypt = Scrypt.of(16384, 16, 2);
            assertThat(scrypt).isNotNull();
            assertThat(scrypt.getN()).isEqualTo(16384);
            assertThat(scrypt.getR()).isEqualTo(16);
            assertThat(scrypt.getP()).isEqualTo(2);
        }

        @Test
        @DisplayName("N不是2的幂抛出异常")
        void testInvalidN() {
            assertThatThrownBy(() -> Scrypt.of(1000, 8, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("power of 2");
        }

        @Test
        @DisplayName("N小于等于1抛出异常")
        void testNTooSmall() {
            assertThatThrownBy(() -> Scrypt.of(1, 8, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("r无效抛出异常")
        void testInvalidR() {
            assertThatThrownBy(() -> Scrypt.of(16384, 0, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> Scrypt.of(16384, -1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("p无效抛出异常")
        void testInvalidP() {
            assertThatThrownBy(() -> Scrypt.of(16384, 8, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");

            assertThatThrownBy(() -> Scrypt.of(16384, 8, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("Builder默认值")
        void testBuilderDefaults() {
            Scrypt scrypt = Scrypt.builder().build();
            assertThat(scrypt.getN()).isEqualTo(32768);
            assertThat(scrypt.getR()).isEqualTo(8);
            assertThat(scrypt.getP()).isEqualTo(1);
        }

        @Test
        @DisplayName("Builder自定义值")
        void testBuilderCustom() {
            Scrypt scrypt = Scrypt.builder()
                    .workFactor(8192)
                    .blockSize(16)
                    .parallelism(2)
                    .build();

            assertThat(scrypt.getN()).isEqualTo(8192);
            assertThat(scrypt.getR()).isEqualTo(16);
            assertThat(scrypt.getP()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("KdfEngine接口测试")
    class KdfEngineTests {

        @Test
        @DisplayName("实现KdfEngine接口")
        void testImplementsKdfEngine() {
            Scrypt scrypt = Scrypt.of();
            assertThat(scrypt).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("getAlgorithm")
        void testGetAlgorithm() {
            Scrypt scrypt = Scrypt.of();
            assertThat(scrypt.getAlgorithm()).isEqualTo("Scrypt");
        }
    }

    @Nested
    @DisplayName("deriveKey测试")
    class DeriveKeyTests {

        @Test
        @DisplayName("deriveKey基本功能")
        void testDeriveKey() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);
            byte[] key = scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey确定性")
        void testDeriveKeyDeterministic() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            byte[] key1 = scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] key2 = scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同密码产生不同结果")
        void testDeriveKeyDifferentPasswords() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            byte[] key1 = scrypt.deriveKey("password1".toCharArray(), TEST_SALT, 32);
            byte[] key2 = scrypt.deriveKey("password2".toCharArray(), TEST_SALT, 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同盐值产生不同结果")
        void testDeriveKeyDifferentSalts() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            byte[] key1 = scrypt.deriveKey(TEST_PASSWORD, "salt1-random-16!".getBytes(StandardCharsets.UTF_8), 32);
            byte[] key2 = scrypt.deriveKey(TEST_PASSWORD, "salt2-random-16!".getBytes(StandardCharsets.UTF_8), 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey多种长度")
        void testDeriveKeyVariousLengths() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            assertThat(scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 16)).hasSize(16);
            assertThat(scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32)).hasSize(32);
            assertThat(scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 64)).hasSize(64);
        }

        @Test
        @DisplayName("deriveKey null password抛出异常")
        void testDeriveKeyNullPassword() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            assertThatThrownBy(() -> scrypt.deriveKey(null, TEST_SALT, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey null salt抛出异常")
        void testDeriveKeyNullSalt() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            assertThatThrownBy(() -> scrypt.deriveKey(TEST_PASSWORD, null, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey无效长度抛出异常")
        void testDeriveKeyInvalidLength() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            assertThatThrownBy(() -> scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey盐值太短抛出异常")
        void testDeriveKeyShortSalt() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);
            byte[] shortSalt = "short".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> scrypt.deriveKey(TEST_PASSWORD, shortSalt, 32))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("KdfEngine derive测试")
    class KdfEngineDeriveTests {

        @Test
        @DisplayName("derive(ikm, salt, info, length)")
        void testDeriveWithAllParams() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);
            byte[] ikm = "password".getBytes(StandardCharsets.UTF_8);
            byte[] result = scrypt.derive(ikm, TEST_SALT, null, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive(ikm, length)")
        void testDeriveSimple() {
            assumeTrue(isBouncyCastleAvailable(), "Bouncy Castle required");
            Scrypt scrypt = Scrypt.of(4096, 8, 1);
            byte[] ikm = "password".getBytes(StandardCharsets.UTF_8);
            byte[] result = scrypt.derive(ikm, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive null ikm抛出异常")
        void testDeriveNullIkm() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            assertThatThrownBy(() -> scrypt.derive(null, TEST_SALT, null, 32))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("无Bouncy Castle测试")
    class NoBouncyCastleTests {

        @Test
        @DisplayName("没有Bouncy Castle时deriveKey抛出异常或成功")
        void testDeriveKeyWithoutBouncyCastle() {
            Scrypt scrypt = Scrypt.of(4096, 8, 1);

            if (!isBouncyCastleAvailable()) {
                assertThatThrownBy(() -> scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32))
                        .isInstanceOf(OpenCryptoException.class)
                        .hasMessageContaining("Bouncy Castle");
            } else {
                // If BC is available, it should work
                byte[] key = scrypt.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
                assertThat(key).hasSize(32);
            }
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getN")
        void testGetN() {
            Scrypt scrypt = Scrypt.of(8192, 8, 1);
            assertThat(scrypt.getN()).isEqualTo(8192);
        }

        @Test
        @DisplayName("getR")
        void testGetR() {
            Scrypt scrypt = Scrypt.of(8192, 16, 1);
            assertThat(scrypt.getR()).isEqualTo(16);
        }

        @Test
        @DisplayName("getP")
        void testGetP() {
            Scrypt scrypt = Scrypt.of(8192, 8, 4);
            assertThat(scrypt.getP()).isEqualTo(4);
        }
    }
}
