package cloud.opencode.base.crypto.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSignatureException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenSignatureException 测试")
class OpenSignatureExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造方法")
        void testMessageConstructor() {
            OpenSignatureException ex = new OpenSignatureException("signature error");

            assertThat(ex.getMessage()).isEqualTo("signature error");
            assertThat(ex.algorithm()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造方法")
        void testMessageCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenSignatureException ex = new OpenSignatureException("signature error", cause);

            assertThat(ex.getMessage()).isEqualTo("signature error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.algorithm()).isNull();
        }

        @Test
        @DisplayName("算法和消息构造方法")
        void testAlgorithmMessageConstructor() {
            OpenSignatureException ex = new OpenSignatureException("ECDSA", "verification failed");

            assertThat(ex.algorithm()).isEqualTo("ECDSA");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[ECDSA]").contains("verification failed");
        }

        @Test
        @DisplayName("完整参数构造方法")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenSignatureException ex = new OpenSignatureException("RSA-PSS", "sign failed", cause);

            assertThat(ex.algorithm()).isEqualTo("RSA-PSS");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[RSA-PSS]");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("signFailed - 签名生成失败")
        void testSignFailed() {
            RuntimeException cause = new RuntimeException("key not found");
            OpenSignatureException ex = OpenSignatureException.signFailed("Ed25519", cause);

            assertThat(ex.algorithm()).isEqualTo("Ed25519");
            assertThat(ex.getMessage()).contains("Signature generation failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("verifyFailed - 签名验证失败")
        void testVerifyFailed() {
            RuntimeException cause = new RuntimeException("invalid key");
            OpenSignatureException ex = OpenSignatureException.verifyFailed("ECDSA", cause);

            assertThat(ex.algorithm()).isEqualTo("ECDSA");
            assertThat(ex.getMessage()).contains("Signature verification failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("invalidSignature - 无效签名")
        void testInvalidSignature() {
            OpenSignatureException ex = OpenSignatureException.invalidSignature("RSA-SHA256");

            assertThat(ex.algorithm()).isEqualTo("RSA-SHA256");
            assertThat(ex.getMessage()).contains("Invalid signature");
            assertThat(ex.getMessage()).contains("verification failed");
        }

        @Test
        @DisplayName("invalidFormat - 无效签名格式")
        void testInvalidFormat() {
            OpenSignatureException ex = OpenSignatureException.invalidFormat("ECDSA", "DER encoding");

            assertThat(ex.algorithm()).isEqualTo("ECDSA");
            assertThat(ex.getMessage()).contains("Invalid signature format");
            assertThat(ex.getMessage()).contains("DER encoding");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("algorithm()返回算法名")
        void testAlgorithmGetter() {
            OpenSignatureException ex = new OpenSignatureException("Ed448", "test");
            assertThat(ex.algorithm()).isEqualTo("Ed448");
        }

        @Test
        @DisplayName("简单构造时algorithm返回null")
        void testAlgorithmReturnsNull() {
            OpenSignatureException ex = new OpenSignatureException("simple error");
            assertThat(ex.algorithm()).isNull();
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含[crypto]前缀")
        void testMessageHasCryptoPrefix() {
            OpenSignatureException ex = new OpenSignatureException("RSA", "test");
            assertThat(ex.getMessage()).startsWith("[crypto]");
        }

        @Test
        @DisplayName("消息包含算法标签")
        void testMessageHasAlgorithmTag() {
            OpenSignatureException ex = new OpenSignatureException("Ed25519", "test");
            assertThat(ex.getMessage()).contains("[Ed25519]");
        }
    }
}
