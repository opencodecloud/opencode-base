package cloud.opencode.base.oauth2.pkce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PkceChallengeTest Tests
 * PkceChallengeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
@DisplayName("PkceChallenge 测试")
class PkceChallengeTest {

    @Nested
    @DisplayName("generate方法测试")
    class GenerateTests {

        @Test
        @DisplayName("generate创建有效的PKCE挑战")
        void testGenerate() {
            PkceChallenge pkce = PkceChallenge.generate();

            assertThat(pkce).isNotNull();
            assertThat(pkce.verifier()).isNotNull();
            assertThat(pkce.verifier()).isNotEmpty();
            assertThat(pkce.challenge()).isNotNull();
            assertThat(pkce.challenge()).isNotEmpty();
            assertThat(pkce.method()).isEqualTo("S256");
        }

        @Test
        @DisplayName("generate创建唯一的挑战")
        void testGenerateUnique() {
            PkceChallenge pkce1 = PkceChallenge.generate();
            PkceChallenge pkce2 = PkceChallenge.generate();

            assertThat(pkce1.verifier()).isNotEqualTo(pkce2.verifier());
            assertThat(pkce1.challenge()).isNotEqualTo(pkce2.challenge());
        }

        @Test
        @DisplayName("verifier长度在43-128之间")
        void testVerifierLength() {
            PkceChallenge pkce = PkceChallenge.generate();

            assertThat(pkce.verifier().length()).isBetween(43, 128);
        }

        @Test
        @DisplayName("generate(int)使用自定义字节长度")
        void testGenerateWithCustomLength() {
            PkceChallenge pkce = PkceChallenge.generate(64);

            assertThat(pkce).isNotNull();
            assertThat(pkce.verifier()).isNotNull();
        }

        @Test
        @DisplayName("generate(int)字节长度小于32抛出异常")
        void testGenerateWithTooSmallLength() {
            assertThatThrownBy(() -> PkceChallenge.generate(16))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("32");
        }
    }

    @Nested
    @DisplayName("plain方法测试")
    class PlainTests {

        @Test
        @DisplayName("plain创建plain方法的PKCE挑战")
        void testPlain() {
            String verifier = "test-verifier-string-that-is-at-least-43-characters-long";
            PkceChallenge pkce = PkceChallenge.plain(verifier);

            assertThat(pkce).isNotNull();
            assertThat(pkce.method()).isEqualTo("plain");
            assertThat(pkce.verifier()).isEqualTo(verifier);
            assertThat(pkce.challenge()).isEqualTo(verifier);
        }
    }

    @Nested
    @DisplayName("calculateS256Challenge方法测试")
    class CalculateS256ChallengeTests {

        @Test
        @DisplayName("calculateS256Challenge计算正确")
        void testCalculateS256Challenge() {
            // 已知的测试向量 (RFC 7636 Appendix B)
            String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
            String expectedChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

            String challenge = PkceChallenge.calculateS256Challenge(verifier);

            assertThat(challenge).isEqualTo(expectedChallenge);
        }
    }

    @Nested
    @DisplayName("verify方法测试")
    class VerifyTests {

        @Test
        @DisplayName("verify - S256有效")
        void testVerifyS256Valid() {
            PkceChallenge pkce = PkceChallenge.generate();

            assertThat(PkceChallenge.verify(pkce.verifier(), pkce.challenge(), pkce.method())).isTrue();
        }

        @Test
        @DisplayName("verify - S256无效")
        void testVerifyS256Invalid() {
            PkceChallenge pkce = PkceChallenge.generate();

            assertThat(PkceChallenge.verify("wrong-verifier", pkce.challenge(), pkce.method())).isFalse();
        }

        @Test
        @DisplayName("verify - plain有效")
        void testVerifyPlainValid() {
            String verifier = "test-verifier-that-is-at-least-43-characters-long-for-rfc";
            PkceChallenge pkce = PkceChallenge.plain(verifier);

            assertThat(PkceChallenge.verify(pkce.verifier(), pkce.challenge(), "plain")).isTrue();
        }

        @Test
        @DisplayName("verify - plain无效")
        void testVerifyPlainInvalid() {
            assertThat(PkceChallenge.verify("verifier1", "verifier2", "plain")).isFalse();
        }

        @Test
        @DisplayName("verify - null参数返回false")
        void testVerifyNullParams() {
            assertThat(PkceChallenge.verify(null, "challenge", "S256")).isFalse();
            assertThat(PkceChallenge.verify("verifier", null, "S256")).isFalse();
            assertThat(PkceChallenge.verify("verifier", "challenge", null)).isFalse();
        }

        @Test
        @DisplayName("verify - 未知method返回false")
        void testVerifyUnknownMethod() {
            assertThat(PkceChallenge.verify("verifier", "challenge", "unknown")).isFalse();
        }
    }

    @Nested
    @DisplayName("方法检查测试")
    class MethodCheckTests {

        @Test
        @DisplayName("isS256")
        void testIsS256() {
            PkceChallenge pkce = PkceChallenge.generate();
            assertThat(pkce.isS256()).isTrue();

            PkceChallenge plain = PkceChallenge.plain("verifier-that-is-at-least-43-characters-long-for-rfc7636");
            assertThat(plain.isS256()).isFalse();
        }

        @Test
        @DisplayName("isPlain")
        void testIsPlain() {
            PkceChallenge pkce = PkceChallenge.generate();
            assertThat(pkce.isPlain()).isFalse();

            PkceChallenge plain = PkceChallenge.plain("verifier-that-is-at-least-43-characters-long-for-rfc7636");
            assertThat(plain.isPlain()).isTrue();
        }
    }

    @Nested
    @DisplayName("常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("METHOD_S256常量")
        void testMethodS256() {
            assertThat(PkceChallenge.METHOD_S256).isEqualTo("S256");
        }

        @Test
        @DisplayName("METHOD_PLAIN常量")
        void testMethodPlain() {
            assertThat(PkceChallenge.METHOD_PLAIN).isEqualTo("plain");
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            // 两个独立生成的PKCE应该不相等
            PkceChallenge pkce1 = PkceChallenge.generate();
            PkceChallenge pkce2 = PkceChallenge.generate();

            assertThat(pkce1).isNotEqualTo(pkce2);

            // 相同参数的PKCE应该相等
            PkceChallenge plain1 = PkceChallenge.plain("verifier-that-is-at-least-43-characters-long-for-rfc7636");
            PkceChallenge plain2 = PkceChallenge.plain("verifier-that-is-at-least-43-characters-long-for-rfc7636");
            assertThat(plain1).isEqualTo(plain2);
            assertThat(plain1.hashCode()).isEqualTo(plain2.hashCode());
        }

        @Test
        @DisplayName("toString")
        void testToString() {
            PkceChallenge pkce = PkceChallenge.generate();

            assertThat(pkce.toString()).contains("S256");
            assertThat(pkce.toString()).contains(pkce.verifier());
        }
    }

    @Nested
    @DisplayName("RFC 7636合规性测试")
    class Rfc7636ComplianceTests {

        @Test
        @DisplayName("verifier仅包含合法字符")
        void testVerifierCharacters() {
            PkceChallenge pkce = PkceChallenge.generate();

            // RFC 7636: unreserved characters [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~"
            assertThat(pkce.verifier()).matches("[A-Za-z0-9\\-._~]+");
        }

        @Test
        @DisplayName("challenge是Base64 URL编码")
        void testChallengeBase64Url() {
            PkceChallenge pkce = PkceChallenge.generate();

            // Base64 URL编码不包含 + 和 / ，也没有填充
            assertThat(pkce.challenge()).doesNotContain("+");
            assertThat(pkce.challenge()).doesNotContain("/");
            assertThat(pkce.challenge()).doesNotEndWith("=");
        }
    }
}
