package cloud.opencode.base.crypto.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtAlgorithm}.
 * JwtAlgorithm单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("JwtAlgorithm Tests / JwtAlgorithm测试")
class JwtAlgorithmTest {

    @Nested
    @DisplayName("Enum Value Tests / 枚举值测试")
    class EnumValueTests {

        @Test
        @DisplayName("HS256算法属性正确")
        void testHS256Properties() {
            JwtAlgorithm alg = JwtAlgorithm.HS256;
            assertThat(alg.algorithmName()).isEqualTo("HS256");
            assertThat(alg.jcaName()).isEqualTo("HmacSHA256");
            assertThat(alg.type()).isEqualTo("symmetric");
            assertThat(alg.isSymmetric()).isTrue();
            assertThat(alg.isAsymmetric()).isFalse();
        }

        @Test
        @DisplayName("HS384算法属性正确")
        void testHS384Properties() {
            JwtAlgorithm alg = JwtAlgorithm.HS384;
            assertThat(alg.algorithmName()).isEqualTo("HS384");
            assertThat(alg.jcaName()).isEqualTo("HmacSHA384");
            assertThat(alg.isSymmetric()).isTrue();
        }

        @Test
        @DisplayName("HS512算法属性正确")
        void testHS512Properties() {
            JwtAlgorithm alg = JwtAlgorithm.HS512;
            assertThat(alg.algorithmName()).isEqualTo("HS512");
            assertThat(alg.jcaName()).isEqualTo("HmacSHA512");
            assertThat(alg.isSymmetric()).isTrue();
        }

        @Test
        @DisplayName("RS256算法属性正确")
        void testRS256Properties() {
            JwtAlgorithm alg = JwtAlgorithm.RS256;
            assertThat(alg.algorithmName()).isEqualTo("RS256");
            assertThat(alg.jcaName()).isEqualTo("SHA256withRSA");
            assertThat(alg.type()).isEqualTo("asymmetric");
            assertThat(alg.isSymmetric()).isFalse();
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("RS384算法属性正确")
        void testRS384Properties() {
            JwtAlgorithm alg = JwtAlgorithm.RS384;
            assertThat(alg.algorithmName()).isEqualTo("RS384");
            assertThat(alg.jcaName()).isEqualTo("SHA384withRSA");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("RS512算法属性正确")
        void testRS512Properties() {
            JwtAlgorithm alg = JwtAlgorithm.RS512;
            assertThat(alg.algorithmName()).isEqualTo("RS512");
            assertThat(alg.jcaName()).isEqualTo("SHA512withRSA");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("PS256算法属性正确")
        void testPS256Properties() {
            JwtAlgorithm alg = JwtAlgorithm.PS256;
            assertThat(alg.algorithmName()).isEqualTo("PS256");
            assertThat(alg.jcaName()).isEqualTo("SHA256withRSAandMGF1");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("PS384算法属性正确")
        void testPS384Properties() {
            JwtAlgorithm alg = JwtAlgorithm.PS384;
            assertThat(alg.algorithmName()).isEqualTo("PS384");
            assertThat(alg.jcaName()).isEqualTo("SHA384withRSAandMGF1");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("PS512算法属性正确")
        void testPS512Properties() {
            JwtAlgorithm alg = JwtAlgorithm.PS512;
            assertThat(alg.algorithmName()).isEqualTo("PS512");
            assertThat(alg.jcaName()).isEqualTo("SHA512withRSAandMGF1");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("ES256算法属性正确")
        void testES256Properties() {
            JwtAlgorithm alg = JwtAlgorithm.ES256;
            assertThat(alg.algorithmName()).isEqualTo("ES256");
            assertThat(alg.jcaName()).isEqualTo("SHA256withECDSA");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("ES384算法属性正确")
        void testES384Properties() {
            JwtAlgorithm alg = JwtAlgorithm.ES384;
            assertThat(alg.algorithmName()).isEqualTo("ES384");
            assertThat(alg.jcaName()).isEqualTo("SHA384withECDSA");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("ES512算法属性正确")
        void testES512Properties() {
            JwtAlgorithm alg = JwtAlgorithm.ES512;
            assertThat(alg.algorithmName()).isEqualTo("ES512");
            assertThat(alg.jcaName()).isEqualTo("SHA512withECDSA");
            assertThat(alg.isAsymmetric()).isTrue();
        }

        @Test
        @DisplayName("EdDSA算法属性正确")
        void testEdDSAProperties() {
            JwtAlgorithm alg = JwtAlgorithm.EdDSA;
            assertThat(alg.algorithmName()).isEqualTo("EdDSA");
            assertThat(alg.jcaName()).isEqualTo("Ed25519");
            assertThat(alg.isAsymmetric()).isTrue();
        }
    }

    @Nested
    @DisplayName("fromName Tests / fromName方法测试")
    class FromNameTests {

        @Test
        @DisplayName("fromName解析HS256")
        void testFromNameHS256() {
            assertThat(JwtAlgorithm.fromName("HS256")).isEqualTo(JwtAlgorithm.HS256);
        }

        @Test
        @DisplayName("fromName解析HS384")
        void testFromNameHS384() {
            assertThat(JwtAlgorithm.fromName("HS384")).isEqualTo(JwtAlgorithm.HS384);
        }

        @Test
        @DisplayName("fromName解析HS512")
        void testFromNameHS512() {
            assertThat(JwtAlgorithm.fromName("HS512")).isEqualTo(JwtAlgorithm.HS512);
        }

        @Test
        @DisplayName("fromName解析RS256")
        void testFromNameRS256() {
            assertThat(JwtAlgorithm.fromName("RS256")).isEqualTo(JwtAlgorithm.RS256);
        }

        @Test
        @DisplayName("fromName解析ES256")
        void testFromNameES256() {
            assertThat(JwtAlgorithm.fromName("ES256")).isEqualTo(JwtAlgorithm.ES256);
        }

        @Test
        @DisplayName("fromName解析EdDSA")
        void testFromNameEdDSA() {
            assertThat(JwtAlgorithm.fromName("EdDSA")).isEqualTo(JwtAlgorithm.EdDSA);
        }

        @Test
        @DisplayName("fromName忽略大小写")
        void testFromNameIgnoresCase() {
            assertThat(JwtAlgorithm.fromName("hs256")).isEqualTo(JwtAlgorithm.HS256);
            assertThat(JwtAlgorithm.fromName("Hs256")).isEqualTo(JwtAlgorithm.HS256);
            assertThat(JwtAlgorithm.fromName("rs256")).isEqualTo(JwtAlgorithm.RS256);
        }

        @Test
        @DisplayName("fromName不支持的算法抛出异常")
        void testFromNameUnsupportedThrows() {
            assertThatThrownBy(() -> JwtAlgorithm.fromName("UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported JWT algorithm");
        }

        @Test
        @DisplayName("fromName空字符串抛出异常")
        void testFromNameEmptyThrows() {
            assertThatThrownBy(() -> JwtAlgorithm.fromName(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Algorithm Classification Tests / 算法分类测试")
    class AlgorithmClassificationTests {

        @Test
        @DisplayName("对称算法正确分类")
        void testSymmetricAlgorithms() {
            JwtAlgorithm[] symmetric = {
                    JwtAlgorithm.HS256,
                    JwtAlgorithm.HS384,
                    JwtAlgorithm.HS512
            };

            for (JwtAlgorithm alg : symmetric) {
                assertThat(alg.isSymmetric())
                        .as("Expected %s to be symmetric", alg.algorithmName())
                        .isTrue();
                assertThat(alg.isAsymmetric())
                        .as("Expected %s to not be asymmetric", alg.algorithmName())
                        .isFalse();
            }
        }

        @Test
        @DisplayName("非对称算法正确分类")
        void testAsymmetricAlgorithms() {
            JwtAlgorithm[] asymmetric = {
                    JwtAlgorithm.RS256, JwtAlgorithm.RS384, JwtAlgorithm.RS512,
                    JwtAlgorithm.PS256, JwtAlgorithm.PS384, JwtAlgorithm.PS512,
                    JwtAlgorithm.ES256, JwtAlgorithm.ES384, JwtAlgorithm.ES512,
                    JwtAlgorithm.EdDSA
            };

            for (JwtAlgorithm alg : asymmetric) {
                assertThat(alg.isAsymmetric())
                        .as("Expected %s to be asymmetric", alg.algorithmName())
                        .isTrue();
                assertThat(alg.isSymmetric())
                        .as("Expected %s to not be symmetric", alg.algorithmName())
                        .isFalse();
            }
        }
    }

    @Nested
    @DisplayName("Enum Completeness Tests / 枚举完整性测试")
    class EnumCompletenessTests {

        @Test
        @DisplayName("所有枚举值都有非null属性")
        void testAllEnumValuesHaveNonNullProperties() {
            for (JwtAlgorithm alg : JwtAlgorithm.values()) {
                assertThat(alg.algorithmName()).isNotNull().isNotEmpty();
                assertThat(alg.jcaName()).isNotNull().isNotEmpty();
                assertThat(alg.type()).isNotNull().isNotEmpty();
            }
        }

        @Test
        @DisplayName("所有枚举值的type只能是symmetric或asymmetric")
        void testAllEnumValuesHaveValidType() {
            for (JwtAlgorithm alg : JwtAlgorithm.values()) {
                assertThat(alg.type())
                        .isIn("symmetric", "asymmetric");
            }
        }
    }
}
