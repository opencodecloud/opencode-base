package cloud.opencode.base.crypto.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenKeyException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenKeyException 测试")
class OpenKeyExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造方法")
        void testMessageConstructor() {
            OpenKeyException ex = new OpenKeyException("key error");

            assertThat(ex.getMessage()).isEqualTo("key error");
            assertThat(ex.keyType()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造方法")
        void testMessageCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenKeyException ex = new OpenKeyException("key error", cause);

            assertThat(ex.getMessage()).isEqualTo("key error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.keyType()).isNull();
        }

        @Test
        @DisplayName("密钥类型和消息构造方法")
        void testKeyTypeMessageConstructor() {
            OpenKeyException ex = new OpenKeyException("RSA", "Invalid key");

            assertThat(ex.keyType()).isEqualTo("RSA");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[RSA]").contains("Invalid key");
        }

        @Test
        @DisplayName("完整参数构造方法")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenKeyException ex = new OpenKeyException("EC", "Parse failed", cause);

            assertThat(ex.keyType()).isEqualTo("EC");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[EC]");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("generationFailed - 密钥生成失败")
        void testGenerationFailed() {
            RuntimeException cause = new RuntimeException("RNG failure");
            OpenKeyException ex = OpenKeyException.generationFailed("RSA-4096", cause);

            assertThat(ex.keyType()).isEqualTo("RSA-4096");
            assertThat(ex.getMessage()).contains("Key generation failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("parseFailed - 密钥解析失败")
        void testParseFailed() {
            RuntimeException cause = new RuntimeException("Invalid format");
            OpenKeyException ex = OpenKeyException.parseFailed("EC", cause);

            assertThat(ex.keyType()).isEqualTo("EC");
            assertThat(ex.getMessage()).contains("Key parsing failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("insufficientStrength - 密钥强度不足")
        void testInsufficientStrength() {
            OpenKeyException ex = OpenKeyException.insufficientStrength("RSA", 2048, 1024);

            assertThat(ex.keyType()).isEqualTo("RSA");
            assertThat(ex.getMessage()).contains("Insufficient key strength");
            assertThat(ex.getMessage()).contains("2048").contains("1024");
        }

        @Test
        @DisplayName("invalidFormat - 无效的密钥格式")
        void testInvalidFormat() {
            OpenKeyException ex = OpenKeyException.invalidFormat("RSA", "PKCS8");

            assertThat(ex.keyType()).isEqualTo("RSA");
            assertThat(ex.getMessage()).contains("Invalid key format").contains("PKCS8");
        }

        @Test
        @DisplayName("typeMismatch - 密钥类型不匹配")
        void testTypeMismatch() {
            OpenKeyException ex = OpenKeyException.typeMismatch("RSA", "EC");

            assertThat(ex.keyType()).isEqualTo("EC");
            assertThat(ex.getMessage()).contains("Key type mismatch");
            assertThat(ex.getMessage()).contains("RSA").contains("EC");
        }

        @Test
        @DisplayName("keyNotSet - 密钥未设置")
        void testKeyNotSet() {
            OpenKeyException ex = OpenKeyException.keyNotSet("encryption");

            assertThat(ex.getMessage()).contains("Key not set");
            assertThat(ex.getMessage()).contains("encryption");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("keyType()返回密钥类型")
        void testKeyTypeGetter() {
            OpenKeyException ex = new OpenKeyException("Ed25519", "test");
            assertThat(ex.keyType()).isEqualTo("Ed25519");
        }

        @Test
        @DisplayName("简单构造时keyType返回null")
        void testKeyTypeReturnsNull() {
            OpenKeyException ex = new OpenKeyException("simple error");
            assertThat(ex.keyType()).isNull();
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含[crypto]前缀")
        void testMessageHasCryptoPrefix() {
            OpenKeyException ex = new OpenKeyException("AES", "test");
            assertThat(ex.getMessage()).startsWith("[crypto]");
        }

        @Test
        @DisplayName("消息包含密钥类型标签")
        void testMessageHasKeyTypeTag() {
            OpenKeyException ex = new OpenKeyException("RSA-2048", "test");
            assertThat(ex.getMessage()).contains("[RSA-2048]");
        }
    }
}
