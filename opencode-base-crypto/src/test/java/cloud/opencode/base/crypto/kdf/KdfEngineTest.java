package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * KdfEngine 接口测试类
 * 通过测试所有实现类来验证接口行为
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("KdfEngine 接口测试")
class KdfEngineTest {

    private static final byte[] TEST_IKM = "input-key-material-for-testing".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_SALT = "random-salt-value".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_INFO = "context-info".getBytes(StandardCharsets.UTF_8);

    /**
     * 提供所有KdfEngine实现
     */
    static Stream<KdfEngine> kdfEngineProvider() {
        return Stream.of(
                Hkdf.sha256(),
                Hkdf.sha384(),
                Hkdf.sha512(),
                Pbkdf2.hmacSha256(10000),
                Pbkdf2.hmacSha512(10000)
        );
    }

    /**
     * 提供HKDF实现（用于确定性测试，因为PBKDF2的简单derive方法会生成随机盐）
     */
    static Stream<KdfEngine> hkdfProvider() {
        return Stream.of(
                Hkdf.sha256(),
                Hkdf.sha384(),
                Hkdf.sha512()
        );
    }

    @Nested
    @DisplayName("derive(ikm, salt, info, length) 测试")
    class DeriveWithAllParamsTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("派生正确长度的密钥")
        void testDeriveCorrectLength(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(derived).hasSize(32);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("相同参数产生相同密钥")
        void testDeriveDeterministic(KdfEngine engine) {
            byte[] derived1 = engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] derived2 = engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(derived1).isEqualTo(derived2);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("不同IKM产生不同密钥")
        void testDifferentIkmDifferentKey(KdfEngine engine) {
            byte[] derived1 = engine.derive("ikm1".getBytes(), TEST_SALT, TEST_INFO, 32);
            byte[] derived2 = engine.derive("ikm2".getBytes(), TEST_SALT, TEST_INFO, 32);

            assertThat(derived1).isNotEqualTo(derived2);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("不同salt产生不同密钥")
        void testDifferentSaltDifferentKey(KdfEngine engine) {
            byte[] derived1 = engine.derive(TEST_IKM, "salt1---padding".getBytes(), TEST_INFO, 32);
            byte[] derived2 = engine.derive(TEST_IKM, "salt2---padding".getBytes(), TEST_INFO, 32);

            assertThat(derived1).isNotEqualTo(derived2);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("派生不同长度")
        void testDeriveVariousLengths(KdfEngine engine) {
            assertThat(engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 16)).hasSize(16);
            assertThat(engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32)).hasSize(32);
            assertThat(engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 64)).hasSize(64);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("null salt 有效")
        void testNullSaltValid(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, null, TEST_INFO, 32);

            assertThat(derived).hasSize(32);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("null info 有效")
        void testNullInfoValid(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, TEST_SALT, null, 32);

            assertThat(derived).hasSize(32);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("无效长度抛出异常")
        void testInvalidLengthThrowsException(KdfEngine engine) {
            assertThatThrownBy(() -> engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("derive(ikm, length) 测试")
    class DeriveSimpleTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("简单派生返回正确长度")
        void testSimpleDeriveCorrectLength(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, 32);

            assertThat(derived).hasSize(32);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#hkdfProvider")
        @DisplayName("简单派生是确定性的（仅HKDF）")
        void testSimpleDeriveDeterministic(KdfEngine engine) {
            // 注意：PBKDF2的简单derive方法会生成随机盐，所以不是确定性的
            byte[] derived1 = engine.derive(TEST_IKM, 32);
            byte[] derived2 = engine.derive(TEST_IKM, 32);

            assertThat(derived1).isEqualTo(derived2);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("简单派生不同IKM产生不同密钥")
        void testSimpleDeriveDifferentIkm(KdfEngine engine) {
            byte[] derived1 = engine.derive("ikm1".getBytes(), 32);
            byte[] derived2 = engine.derive("ikm2".getBytes(), 32);

            assertThat(derived1).isNotEqualTo(derived2);
        }
    }

    @Nested
    @DisplayName("getAlgorithm 测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("HKDF-SHA256算法名")
        void testHkdfSha256Algorithm() {
            KdfEngine engine = Hkdf.sha256();
            assertThat(engine.getAlgorithm()).isEqualTo("HmacSHA256");
        }

        @Test
        @DisplayName("HKDF-SHA384算法名")
        void testHkdfSha384Algorithm() {
            KdfEngine engine = Hkdf.sha384();
            assertThat(engine.getAlgorithm()).isEqualTo("HmacSHA384");
        }

        @Test
        @DisplayName("HKDF-SHA512算法名")
        void testHkdfSha512Algorithm() {
            KdfEngine engine = Hkdf.sha512();
            assertThat(engine.getAlgorithm()).isEqualTo("HmacSHA512");
        }

        @Test
        @DisplayName("PBKDF2-SHA256算法名")
        void testPbkdf2Sha256Algorithm() {
            KdfEngine engine = Pbkdf2.hmacSha256(10000);
            assertThat(engine.getAlgorithm()).contains("SHA256");
        }

        @Test
        @DisplayName("PBKDF2-SHA512算法名")
        void testPbkdf2Sha512Algorithm() {
            KdfEngine engine = Pbkdf2.hmacSha512(10000);
            assertThat(engine.getAlgorithm()).contains("SHA512");
        }
    }

    @Nested
    @DisplayName("实现类型验证测试")
    class ImplementationTypeTests {

        @Test
        @DisplayName("Hkdf实现KdfEngine接口")
        void testHkdfImplementsKdfEngine() {
            assertThat(Hkdf.sha256()).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("Pbkdf2实现KdfEngine接口")
        void testPbkdf2ImplementsKdfEngine() {
            assertThat(Pbkdf2.owaspRecommended()).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("Scrypt实现KdfEngine接口")
        void testScryptImplementsKdfEngine() {
            assertThat(Scrypt.of()).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("Argon2Kdf实现KdfEngine接口")
        void testArgon2KdfImplementsKdfEngine() {
            assertThat(Argon2Kdf.argon2id()).isInstanceOf(KdfEngine.class);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("最小长度派生")
        void testMinimumLengthDerive(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, 1);
            assertThat(derived).hasSize(1);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("空salt有效")
        void testEmptySaltValid(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, new byte[0], TEST_INFO, 32);
            assertThat(derived).hasSize(32);
        }

        @ParameterizedTest
        @MethodSource("cloud.opencode.base.crypto.kdf.KdfEngineTest#kdfEngineProvider")
        @DisplayName("空info有效")
        void testEmptyInfoValid(KdfEngine engine) {
            byte[] derived = engine.derive(TEST_IKM, TEST_SALT, new byte[0], 32);
            assertThat(derived).hasSize(32);
        }
    }

    @Nested
    @DisplayName("密钥派生一致性测试")
    class ConsistencyTests {

        @Test
        @DisplayName("HKDF派生与完整参数一致")
        void testHkdfConsistency() {
            Hkdf hkdf = Hkdf.sha256();

            // 使用接口方法
            byte[] interfaceResult = hkdf.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            // 使用具体实现方法
            byte[] implResult = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(interfaceResult).isEqualTo(implResult);
        }

        @Test
        @DisplayName("HKDF简单派生与默认参数一致")
        void testHkdfSimpleConsistency() {
            Hkdf hkdf = Hkdf.sha256();

            // 使用接口简单方法
            byte[] interfaceResult = hkdf.derive(TEST_IKM, 32);

            // 使用接口完整方法（null参数）
            byte[] fullResult = hkdf.derive(TEST_IKM, null, null, 32);

            assertThat(interfaceResult).isEqualTo(fullResult);
        }
    }

    @Nested
    @DisplayName("不同算法对比测试")
    class AlgorithmComparisonTests {

        @Test
        @DisplayName("不同HKDF算法产生不同密钥")
        void testDifferentHkdfAlgorithmsDifferentKeys() {
            byte[] sha256 = Hkdf.sha256().derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] sha384 = Hkdf.sha384().derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] sha512 = Hkdf.sha512().derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(sha256).isNotEqualTo(sha384);
            assertThat(sha256).isNotEqualTo(sha512);
            assertThat(sha384).isNotEqualTo(sha512);
        }

        @Test
        @DisplayName("不同KDF类型产生不同密钥")
        void testDifferentKdfTypesDifferentKeys() {
            byte[] hkdf = Hkdf.sha256().derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] pbkdf2 = Pbkdf2.hmacSha256(10000).derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(hkdf).isNotEqualTo(pbkdf2);
        }
    }

    @Nested
    @DisplayName("大输出长度测试")
    class LargeOutputTests {

        @Test
        @DisplayName("HKDF派生大长度密钥")
        void testHkdfLargeOutput() {
            KdfEngine engine = Hkdf.sha256();

            byte[] derived = engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 1024);

            assertThat(derived).hasSize(1024);
        }

        @Test
        @DisplayName("PBKDF2派生大长度密钥")
        void testPbkdf2LargeOutput() {
            KdfEngine engine = Pbkdf2.hmacSha256(10000);

            byte[] derived = engine.derive(TEST_IKM, TEST_SALT, TEST_INFO, 512);

            assertThat(derived).hasSize(512);
        }
    }
}
