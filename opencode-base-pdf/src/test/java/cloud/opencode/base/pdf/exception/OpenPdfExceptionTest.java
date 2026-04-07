package cloud.opencode.base.pdf.exception;

import cloud.opencode.base.core.exception.OpenException;
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
 * @since JDK 25, opencode-base-pdf V1.0.3
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

            assertThat(ex.getMessage()).contains("[PDF]").contains("Test message");
            assertThat(ex.getRawMessage()).isEqualTo("Test message");
            assertThat(ex.getComponent()).isEqualTo("PDF");
            assertThat(ex.getErrorCode()).isNull();
            assertThat(ex.operation()).isNull();
            assertThat(ex.pageNumber()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("Original");
            OpenPdfException ex = new OpenPdfException("Test message", cause);

            assertThat(ex.getMessage()).contains("[PDF]").contains("Test message");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getComponent()).isEqualTo("PDF");
            assertThat(ex.operation()).isNull();
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作和消息构造")
        void testOperationAndMessageConstructor() {
            OpenPdfException ex = new OpenPdfException("read", "Failed to read file");

            assertThat(ex.getMessage()).contains("[PDF]").contains("Failed to read file");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_READ");
            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作、消息和原因构造")
        void testOperationMessageAndCauseConstructor() {
            Exception cause = new RuntimeException("IO Error");
            OpenPdfException ex = new OpenPdfException("write", "Failed to write", cause);

            assertThat(ex.getMessage()).contains("[PDF]").contains("Failed to write");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_WRITE");
            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.pageNumber()).isNull();
        }

        @Test
        @DisplayName("操作、页码和消息构造")
        void testOperationPageNumberAndMessageConstructor() {
            OpenPdfException ex = new OpenPdfException("page", 5, "Invalid page");

            assertThat(ex.getMessage()).contains("[PDF]").contains("Invalid page");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_PAGE");
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
            assertThat(ex.getErrorCode()).isEqualTo("PDF_PARSE");
        }

        @Test
        @DisplayName("readFailed 创建读取失败异常")
        void testReadFailed() {
            Exception cause = new RuntimeException("IO");
            OpenPdfException ex = OpenPdfException.readFailed("/path/to/file.pdf", cause);

            assertThat(ex.getMessage()).contains("Failed to read PDF").contains("/path/to/file.pdf");
            assertThat(ex.operation()).isEqualTo("read");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_READ");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("writeFailed 创建写入失败异常")
        void testWriteFailed() {
            Exception cause = new RuntimeException("IO");
            OpenPdfException ex = OpenPdfException.writeFailed("/path/to/output.pdf", cause);

            assertThat(ex.getMessage()).contains("Failed to write PDF").contains("/path/to/output.pdf");
            assertThat(ex.operation()).isEqualTo("write");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_WRITE");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("invalidPageNumber 创建无效页码异常")
        void testInvalidPageNumber() {
            OpenPdfException ex = OpenPdfException.invalidPageNumber(10, 5);

            assertThat(ex.getMessage()).contains("Invalid page number").contains("10").contains("5 pages");
            assertThat(ex.operation()).isEqualTo("page");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_PAGE");
            assertThat(ex.pageNumber()).isEqualTo(10);
        }

        @Test
        @DisplayName("passwordRequired 创建需要密码异常")
        void testPasswordRequired() {
            OpenPdfException ex = OpenPdfException.passwordRequired();

            assertThat(ex.getMessage()).contains("encrypted").contains("Password required");
            assertThat(ex.operation()).isEqualTo("decrypt");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_DECRYPT");
        }

        @Test
        @DisplayName("wrongPassword 创建密码错误异常")
        void testWrongPassword() {
            OpenPdfException ex = OpenPdfException.wrongPassword();

            assertThat(ex.getMessage()).contains("Incorrect password");
            assertThat(ex.operation()).isEqualTo("decrypt");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_DECRYPT");
        }

        @Test
        @DisplayName("signatureFailed 创建签名失败异常")
        void testSignatureFailed() {
            Exception cause = new RuntimeException("Key error");
            OpenPdfException ex = OpenPdfException.signatureFailed("Invalid key", cause);

            assertThat(ex.getMessage()).contains("Signature failed").contains("Invalid key");
            assertThat(ex.operation()).isEqualTo("sign");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_SIGN");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("fieldNotFound 创建字段未找到异常")
        void testFieldNotFound() {
            OpenPdfException ex = OpenPdfException.fieldNotFound("firstName");

            assertThat(ex.getMessage()).contains("Form field not found").contains("firstName");
            assertThat(ex.operation()).isEqualTo("form");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_FORM");
        }

        @Test
        @DisplayName("unsupportedFeature 创建不支持功能异常")
        void testUnsupportedFeature() {
            OpenPdfException ex = OpenPdfException.unsupportedFeature("3D content");

            assertThat(ex.getMessage()).contains("Unsupported PDF feature").contains("3D content");
            assertThat(ex.operation()).isEqualTo("feature");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_FEATURE");
        }

        @Test
        @DisplayName("mergeFailed 创建合并失败异常")
        void testMergeFailed() {
            Exception cause = new RuntimeException("Conflict");
            OpenPdfException ex = OpenPdfException.mergeFailed("Page conflict", cause);

            assertThat(ex.getMessage()).contains("PDF merge failed").contains("Page conflict");
            assertThat(ex.operation()).isEqualTo("merge");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_MERGE");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("splitFailed 创建拆分失败异常")
        void testSplitFailed() {
            Exception cause = new RuntimeException("Range error");
            OpenPdfException ex = OpenPdfException.splitFailed("Invalid range", cause);

            assertThat(ex.getMessage()).contains("PDF split failed").contains("Invalid range");
            assertThat(ex.operation()).isEqualTo("split");
            assertThat(ex.getErrorCode()).isEqualTo("PDF_SPLIT");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("异常层次结构测试")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("是 OpenException 的子类")
        void testIsOpenException() {
            OpenPdfException ex = new OpenPdfException("Test");
            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("是 RuntimeException 的子类")
        void testIsRuntimeException() {
            OpenPdfException ex = new OpenPdfException("Test");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被 OpenException catch 捕获")
        void testCatchAsOpenException() {
            assertThatThrownBy(() -> {
                throw new OpenPdfException("Test");
            }).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("组件名称始终为 PDF")
        void testComponentIsPdf() {
            OpenPdfException ex1 = new OpenPdfException("msg");
            OpenPdfException ex2 = new OpenPdfException("read", "msg");
            OpenPdfException ex3 = OpenPdfException.invalidFormat("bad");

            assertThat(ex1.getComponent()).isEqualTo("PDF");
            assertThat(ex2.getComponent()).isEqualTo("PDF");
            assertThat(ex3.getComponent()).isEqualTo("PDF");
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

    @Nested
    @DisplayName("消息格式测试")
    class MessageFormatTests {

        @Test
        @DisplayName("getMessage 包含 [PDF] 前缀")
        void testMessageContainsPdfPrefix() {
            OpenPdfException ex = new OpenPdfException("something went wrong");

            assertThat(ex.getMessage()).startsWith("[PDF] ");
        }

        @Test
        @DisplayName("带操作的 getMessage 包含错误码")
        void testMessageWithErrorCode() {
            OpenPdfException ex = new OpenPdfException("read", "file not found");

            assertThat(ex.getMessage()).contains("[PDF]").contains("(PDF_READ)").contains("file not found");
        }

        @Test
        @DisplayName("getRawMessage 返回原始消息不含前缀")
        void testRawMessage() {
            OpenPdfException ex = new OpenPdfException("read", "file not found");

            assertThat(ex.getRawMessage()).isEqualTo("file not found");
        }
    }

    @Nested
    @DisplayName("错误码映射测试")
    class ErrorCodeMappingTests {

        @Test
        @DisplayName("所有操作类型映射到正确的错误码")
        void testAllErrorCodeMappings() {
            assertThat(OpenPdfException.invalidFormat("x").getErrorCode()).isEqualTo("PDF_PARSE");
            assertThat(OpenPdfException.readFailed("x", null).getErrorCode()).isEqualTo("PDF_READ");
            assertThat(OpenPdfException.writeFailed("x", null).getErrorCode()).isEqualTo("PDF_WRITE");
            assertThat(OpenPdfException.signatureFailed("x", null).getErrorCode()).isEqualTo("PDF_SIGN");
            assertThat(OpenPdfException.fieldNotFound("x").getErrorCode()).isEqualTo("PDF_FORM");
            assertThat(OpenPdfException.mergeFailed("x", null).getErrorCode()).isEqualTo("PDF_MERGE");
            assertThat(OpenPdfException.splitFailed("x", null).getErrorCode()).isEqualTo("PDF_SPLIT");
            assertThat(OpenPdfException.passwordRequired().getErrorCode()).isEqualTo("PDF_DECRYPT");
            assertThat(OpenPdfException.unsupportedFeature("x").getErrorCode()).isEqualTo("PDF_FEATURE");
        }

        @Test
        @DisplayName("未知操作类型使用大写格式")
        void testUnknownOperationErrorCode() {
            OpenPdfException ex = new OpenPdfException("custom", "test message");

            assertThat(ex.getErrorCode()).isEqualTo("PDF_CUSTOM");
        }

        @Test
        @DisplayName("仅消息构造的错误码为 null")
        void testMessageOnlyErrorCodeIsNull() {
            OpenPdfException ex = new OpenPdfException("test");

            assertThat(ex.getErrorCode()).isNull();
        }
    }
}
