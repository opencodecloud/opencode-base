package cloud.opencode.base.crypto.kdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Pbkdf2 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Pbkdf2 测试")
class Pbkdf2Test {

    private static final char[] TEST_PASSWORD = "password123".toCharArray();
    private static final byte[] TEST_SALT = "random-salt-16b!".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("hmacSha256创建实例")
        void testHmacSha256() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(10000);
            assertThat(pbkdf2).isNotNull();
            assertThat(pbkdf2.getAlgorithm()).isEqualTo("PBKDF2WithHmacSHA256");
            assertThat(pbkdf2.getIterations()).isEqualTo(10000);
        }

        @Test
        @DisplayName("hmacSha512创建实例")
        void testHmacSha512() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha512(10000);
            assertThat(pbkdf2).isNotNull();
            assertThat(pbkdf2.getAlgorithm()).isEqualTo("PBKDF2WithHmacSHA512");
            assertThat(pbkdf2.getIterations()).isEqualTo(10000);
        }

        @Test
        @DisplayName("owaspRecommended创建实例")
        void testOwaspRecommended() {
            Pbkdf2 pbkdf2 = Pbkdf2.owaspRecommended();
            assertThat(pbkdf2).isNotNull();
            assertThat(pbkdf2.getAlgorithm()).isEqualTo("PBKDF2WithHmacSHA256");
            assertThat(pbkdf2.getIterations()).isEqualTo(600_000);
        }

        @Test
        @DisplayName("无效迭代次数抛出异常")
        void testInvalidIterations() {
            assertThatThrownBy(() -> Pbkdf2.hmacSha256(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Pbkdf2.hmacSha256(-1))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> Pbkdf2.hmacSha512(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("KdfEngine接口测试")
    class KdfEngineTests {

        @Test
        @DisplayName("实现KdfEngine接口")
        void testImplementsKdfEngine() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            assertThat(pbkdf2).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("derive(ikm, salt, info, length)")
        void testDeriveWithAllParams() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] ikm = "password".getBytes(StandardCharsets.UTF_8);
            byte[] result = pbkdf2.derive(ikm, TEST_SALT, null, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive(ikm, length)")
        void testDeriveSimple() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] ikm = "password".getBytes(StandardCharsets.UTF_8);
            byte[] result = pbkdf2.derive(ikm, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive null salt自动生成")
        void testDeriveNullSalt() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] ikm = "password".getBytes(StandardCharsets.UTF_8);
            byte[] result = pbkdf2.derive(ikm, null, null, 32);

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("generateSalt测试")
    class GenerateSaltTests {

        @Test
        @DisplayName("generateSalt默认长度")
        void testGenerateSaltDefault() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] salt = pbkdf2.generateSalt();

            assertThat(salt).hasSize(16);
        }

        @Test
        @DisplayName("generateSalt自定义长度")
        void testGenerateSaltCustomLength() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] salt = pbkdf2.generateSalt(32);

            assertThat(salt).hasSize(32);
        }

        @Test
        @DisplayName("generateSalt生成不同值")
        void testGenerateSaltDifferent() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] salt1 = pbkdf2.generateSalt();
            byte[] salt2 = pbkdf2.generateSalt();

            assertThat(salt1).isNotEqualTo(salt2);
        }

        @Test
        @DisplayName("generateSalt无效长度抛出异常")
        void testGenerateSaltInvalidLength() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThatThrownBy(() -> pbkdf2.generateSalt(0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> pbkdf2.generateSalt(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deriveKey测试")
    class DeriveKeyTests {

        @Test
        @DisplayName("deriveKey基本功能")
        void testDeriveKey() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] key = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey自定义迭代次数")
        void testDeriveKeyCustomIterations() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] key = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32, 5000);

            assertThat(key).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey确定性")
        void testDeriveKeyDeterministic() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            byte[] key1 = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] key2 = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(key1).isEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同密码产生不同结果")
        void testDeriveKeyDifferentPasswords() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            byte[] key1 = pbkdf2.deriveKey("password1".toCharArray(), TEST_SALT, 32);
            byte[] key2 = pbkdf2.deriveKey("password2".toCharArray(), TEST_SALT, 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同盐值产生不同结果")
        void testDeriveKeyDifferentSalts() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            byte[] key1 = pbkdf2.deriveKey(TEST_PASSWORD, "salt1-random-16!".getBytes(StandardCharsets.UTF_8), 32);
            byte[] key2 = pbkdf2.deriveKey(TEST_PASSWORD, "salt2-random-16!".getBytes(StandardCharsets.UTF_8), 32);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey不同迭代次数产生不同结果")
        void testDeriveKeyDifferentIterations() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            byte[] key1 = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32, 1000);
            byte[] key2 = pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32, 2000);

            assertThat(key1).isNotEqualTo(key2);
        }

        @Test
        @DisplayName("deriveKey多种长度")
        void testDeriveKeyVariousLengths() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThat(pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 16)).hasSize(16);
            assertThat(pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32)).hasSize(32);
            assertThat(pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 64)).hasSize(64);
        }

        @Test
        @DisplayName("deriveKey null password抛出异常")
        void testDeriveKeyNullPassword() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThatThrownBy(() -> pbkdf2.deriveKey(null, TEST_SALT, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey null salt抛出异常")
        void testDeriveKeyNullSalt() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, null, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey无效长度抛出异常")
        void testDeriveKeyInvalidLength() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey无效迭代次数抛出异常")
        void testDeriveKeyInvalidIterations() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, TEST_SALT, 32, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey盐值太短抛出异常")
        void testDeriveKeyShortSalt() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(1000);
            byte[] shortSalt = "short".getBytes(StandardCharsets.UTF_8);

            assertThatThrownBy(() -> pbkdf2.deriveKey(TEST_PASSWORD, shortSalt, 32))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("属性测试")
    class PropertyTests {

        @Test
        @DisplayName("getIterations")
        void testGetIterations() {
            Pbkdf2 pbkdf2 = Pbkdf2.hmacSha256(12345);
            assertThat(pbkdf2.getIterations()).isEqualTo(12345);
        }

        @Test
        @DisplayName("getOwaspIterations")
        void testGetOwaspIterations() {
            assertThat(Pbkdf2.getOwaspIterations()).isEqualTo(600_000);
        }

        @Test
        @DisplayName("getOwaspSha512Iterations")
        void testGetOwaspSha512Iterations() {
            assertThat(Pbkdf2.getOwaspSha512Iterations()).isEqualTo(210_000);
        }
    }

    @Nested
    @DisplayName("不同算法测试")
    class AlgorithmComparisonTests {

        @Test
        @DisplayName("SHA256和SHA512产生不同结果")
        void testDifferentAlgorithmsDifferentResults() {
            Pbkdf2 sha256 = Pbkdf2.hmacSha256(1000);
            Pbkdf2 sha512 = Pbkdf2.hmacSha512(1000);

            byte[] result256 = sha256.deriveKey(TEST_PASSWORD, TEST_SALT, 32);
            byte[] result512 = sha512.deriveKey(TEST_PASSWORD, TEST_SALT, 32);

            assertThat(result256).isNotEqualTo(result512);
        }
    }
}
