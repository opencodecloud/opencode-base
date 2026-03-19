package cloud.opencode.base.crypto.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCryptoException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenCryptoException 测试")
class OpenCryptoExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("消息构造方法")
        void testMessageConstructor() {
            OpenCryptoException ex = new OpenCryptoException("test error");

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.algorithm()).isNull();
            assertThat(ex.operation()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造方法")
        void testMessageCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenCryptoException ex = new OpenCryptoException("test error", cause);

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("算法、操作和消息构造方法")
        void testAlgorithmOperationMessageConstructor() {
            OpenCryptoException ex = new OpenCryptoException("AES", "encrypt", "Failed");

            assertThat(ex.algorithm()).isEqualTo("AES");
            assertThat(ex.operation()).isEqualTo("encrypt");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[AES]").contains("[encrypt]").contains("Failed");
        }

        @Test
        @DisplayName("完整参数构造方法")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            OpenCryptoException ex = new OpenCryptoException("RSA", "decrypt", "Error", cause);

            assertThat(ex.algorithm()).isEqualTo("RSA");
            assertThat(ex.operation()).isEqualTo("decrypt");
            assertThat(ex.getMessage()).contains("[crypto]").contains("[RSA]").contains("[decrypt]");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("algorithmNotAvailable - 算法不可用")
        void testAlgorithmNotAvailable() {
            OpenCryptoException ex = OpenCryptoException.algorithmNotAvailable("UNKNOWN_ALGO");

            assertThat(ex.algorithm()).isEqualTo("UNKNOWN_ALGO");
            assertThat(ex.operation()).isEqualTo("initialization");
            assertThat(ex.getMessage()).contains("Algorithm not available").contains("UNKNOWN_ALGO");
        }

        @Test
        @DisplayName("encryptionFailed - 加密失败")
        void testEncryptionFailed() {
            RuntimeException cause = new RuntimeException("data too long");
            OpenCryptoException ex = OpenCryptoException.encryptionFailed("RSA", cause);

            assertThat(ex.algorithm()).isEqualTo("RSA");
            assertThat(ex.operation()).isEqualTo("encryption");
            assertThat(ex.getMessage()).contains("Encryption failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("decryptionFailed - 解密失败")
        void testDecryptionFailed() {
            RuntimeException cause = new RuntimeException("padding error");
            OpenCryptoException ex = OpenCryptoException.decryptionFailed("AES", cause);

            assertThat(ex.algorithm()).isEqualTo("AES");
            assertThat(ex.operation()).isEqualTo("decryption");
            assertThat(ex.getMessage()).contains("Decryption failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("authenticationFailed - 认证失败")
        void testAuthenticationFailed() {
            OpenCryptoException ex = OpenCryptoException.authenticationFailed("AES-GCM");

            assertThat(ex.algorithm()).isEqualTo("AES-GCM");
            assertThat(ex.operation()).isEqualTo("authentication");
            assertThat(ex.getMessage()).contains("Authentication failed");
            assertThat(ex.getMessage()).contains("tampered");
        }

        @Test
        @DisplayName("paddingError - 填充错误")
        void testPaddingError() {
            OpenCryptoException ex = OpenCryptoException.paddingError("AES-CBC");

            assertThat(ex.algorithm()).isEqualTo("AES-CBC");
            assertThat(ex.operation()).isEqualTo("padding");
            assertThat(ex.getMessage()).contains("Invalid padding");
        }

        @Test
        @DisplayName("invalidIv - 无效IV")
        void testInvalidIv() {
            OpenCryptoException ex = OpenCryptoException.invalidIv("AES-GCM", 12, 16);

            assertThat(ex.algorithm()).isEqualTo("AES-GCM");
            assertThat(ex.operation()).isEqualTo("initialization");
            assertThat(ex.getMessage()).contains("Invalid IV length");
            assertThat(ex.getMessage()).contains("12").contains("16");
        }

        @Test
        @DisplayName("dataTooLong - 数据过长")
        void testDataTooLong() {
            OpenCryptoException ex = OpenCryptoException.dataTooLong("RSA-2048", 245);

            assertThat(ex.algorithm()).isEqualTo("RSA-2048");
            assertThat(ex.operation()).isEqualTo("validation");
            assertThat(ex.getMessage()).contains("exceeds maximum").contains("245");
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("algorithm()返回算法名")
        void testAlgorithmGetter() {
            OpenCryptoException ex = new OpenCryptoException("ChaCha20", "encrypt", "test");
            assertThat(ex.algorithm()).isEqualTo("ChaCha20");
        }

        @Test
        @DisplayName("operation()返回操作名")
        void testOperationGetter() {
            OpenCryptoException ex = new OpenCryptoException("AES", "decrypt", "test");
            assertThat(ex.operation()).isEqualTo("decrypt");
        }

        @Test
        @DisplayName("简单构造时getter返回null")
        void testGettersReturnNull() {
            OpenCryptoException ex = new OpenCryptoException("simple error");
            assertThat(ex.algorithm()).isNull();
            assertThat(ex.operation()).isNull();
        }
    }

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("消息包含[crypto]前缀")
        void testMessageHasCryptoPrefix() {
            OpenCryptoException ex = new OpenCryptoException("AES", "encrypt", "test");
            assertThat(ex.getMessage()).startsWith("[crypto]");
        }

        @Test
        @DisplayName("消息包含算法标签")
        void testMessageHasAlgorithmTag() {
            OpenCryptoException ex = new OpenCryptoException("RSA-4096", "decrypt", "test");
            assertThat(ex.getMessage()).contains("[RSA-4096]");
        }

        @Test
        @DisplayName("消息包含操作标签")
        void testMessageHasOperationTag() {
            OpenCryptoException ex = new OpenCryptoException("AES", "verify", "test");
            assertThat(ex.getMessage()).contains("[verify]");
        }
    }
}
