package cloud.opencode.base.pdf.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenPdfException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
@DisplayName("OpenPdfException 测试")
class OpenPdfExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageOnlyConstructor() {
            OpenPdfException ex = new OpenPdfException("Test message");

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.operation()).isNull();
            assertThat(ex.pageNumber()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenPdfException ex = new OpenPdfException("Test message", cause);

            assertThat(ex.getMessage()).contains("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.operation()).isNull();
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作和消息构造")
        void testOperationAndMessageConstructor() {
            OpenPdfException ex = new OpenPdfException("read", "Failed to read file");

            assertThat(ex.getMessage()).contains("Failed to read file");
            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作、消息和原因构造")
        void testOperationMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("IO Error");
            OpenPdfException ex = new OpenPdfException("write", "Failed to write", cause);

            assertThat(ex.getMessage()).contains("Failed to write");
            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作、页码和消息构造")
        void testOperationPageNumberAndMessageConstructor() {
            OpenPdfException ex = new OpenPdfException("page", 5, "Invalid page");

            assertThat(ex.getMessage()).contains("Invalid page");
            assertThat(ex.operation()).isEqualTo("page");
            assertThat(ex.pageNumber()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("invalidFormat 创建无效格式异常")
        void testInvalidFormat() {
            OpenPdfException ex = OpenPdfException.invalidFormat("Missing header");

            assertThat(ex.getMessage()).contains("Invalid PDF format").contains("Missing header");
            assertThat(ex.operation()).isEqualTo("parse");
        }

        @Test
        @DisplayName("readFailed 创建读取失败异常")
        void testReadFailed() {
            Exception cause = new RuntimeException("IO");
            OpenPdfException ex = OpenPdfException.readFailed("/path/to/file.pdf", cause);

            assertThat(ex.getMessage()).contains("Failed to read PDF").contains("/path/to/file.pdf");
            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("writeFailed 创建写入失败异常")
        void testWriteFailed() {
            Exception cause = new RuntimeException("IO");
            OpenPdfException ex = OpenPdfException.writeFailed("/path/to/output.pdf", cause);

            assertThat(ex.getMessage()).contains("Failed to write PDF").contains("/path/to/output.pdf");
            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("invalidPageNumber 创建无效页码异常")
        void testInvalidPageNumber() {
            OpenPdfException ex = OpenPdfException.invalidPageNumber(10, 5);

            assertThat(ex.getMessage()).contains("Invalid page number").contains("10").contains("5 pages");
            assertThat(ex.operation()).isEqualTo("page");
            assertThat(ex.pageNumber()).isEqualTo(10);
        }

        @Test
        @DisplayName("passwordRequired 创建需要密码异常")
        void testPasswordRequired() {
            OpenPdfException ex = OpenPdfException.passwordRequired();

            assertThat(ex.getMessage()).contains("encrypted").contains("Password required");
            assertThat(ex.operation()).isEqualTo("decrypt");
        }

        @Test
        @DisplayName("wrongPassword 创建密码错误异常")
        void testWrongPassword() {
            OpenPdfException ex = OpenPdfException.wrongPassword();

            assertThat(ex.getMessage()).contains("Incorrect password");
            assertThat(ex.operation()).isEqualTo("decrypt");
        }

        @Test
        @DisplayName("signatureFailed 创建签名失败异常")
        void testSignatureFailed() {
            Exception cause = new RuntimeException("Key error");
            OpenPdfException ex = OpenPdfException.signatureFailed("Invalid key", cause);

            assertThat(ex.getMessage()).contains("Signature failed").contains("Invalid key");
            assertThat(ex.operation()).isEqualTo("sign");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("fieldNotFound 创建字段未找到异常")
        void testFieldNotFound() {
            OpenPdfException ex = OpenPdfException.fieldNotFound("firstName");

            assertThat(ex.getMessage()).contains("Form field not found").contains("firstName");
            assertThat(ex.operation()).isEqualTo("form");
        }

        @Test
        @DisplayName("unsupportedFeature 创建不支持功能异常")
        void testUnsupportedFeature() {
            OpenPdfException ex = OpenPdfException.unsupportedFeature("3D content");

            assertThat(ex.getMessage()).contains("Unsupported PDF feature").contains("3D content");
            assertThat(ex.operation()).isEqualTo("feature");
        }

        @Test
        @DisplayName("mergeFailed 创建合并失败异常")
        void testMergeFailed() {
            Exception cause = new RuntimeException("Conflict");
            OpenPdfException ex = OpenPdfException.mergeFailed("Page conflict", cause);

            assertThat(ex.getMessage()).contains("PDF merge failed").contains("Page conflict");
            assertThat(ex.operation()).isEqualTo("merge");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("splitFailed 创建拆分失败异常")
        void testSplitFailed() {
            Exception cause = new RuntimeException("Range error");
            OpenPdfException ex = OpenPdfException.splitFailed("Invalid range", cause);

            assertThat(ex.getMessage()).contains("PDF split failed").contains("Invalid range");
            assertThat(ex.operation()).isEqualTo("split");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("异常特性测试")
    class ExceptionFeatureTests {

        @Test
        @DisplayName("是 RuntimeException 的子类")
        void testIsRuntimeException() {
            OpenPdfException ex = new OpenPdfException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被抛出和捕获")
        void testThrowAndCatch() {
            assertThatThrownBy(() -> {
                throw new OpenPdfException("Test");
            }).isInstanceOf(OpenPdfException.class);
        }

        @Test
        @DisplayName("异常链正确传递")
        void testExceptionChain() {
            Exception root = new IllegalArgumentException("Root cause");
            OpenPdfException ex = new OpenPdfException("Wrapper", root);

            assertThat(ex.getCause()).isEqualTo(root);
            assertThat(ex.getCause().getMessage()).isEqualTo("Root cause");
        }
    }
}
