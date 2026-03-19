package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageIOException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageIOException 异常测试")
class ImageIOExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageIOException ex = new ImageIOException("IO error occurred");

            assertThat(ex.getMessage()).isEqualTo("IO error occurred");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.IO_ERROR);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            ImageIOException ex = new ImageIOException("IO error", cause);

            assertThat(ex.getMessage()).isEqualTo("IO error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.IO_ERROR);
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageIOException ex = new ImageIOException("File not found", ImageErrorCode.FILE_NOT_FOUND);

            assertThat(ex.getMessage()).isEqualTo("File not found");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.FILE_NOT_FOUND);
        }

        @Test
        @DisplayName("带消息、原因和错误码构造")
        void testConstructorWithAllParams() {
            Throwable cause = new RuntimeException("Root cause");
            ImageIOException ex = new ImageIOException("Read failed", cause, ImageErrorCode.READ_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Read failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.READ_FAILED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageException")
        void testExtendsImageException() {
            ImageIOException ex = new ImageIOException("Test");

            assertThat(ex).isInstanceOf(ImageException.class);
        }

        @Test
        @DisplayName("可以被捕获为ImageException")
        void testCatchAsImageException() {
            boolean caught = false;
            try {
                throw new ImageIOException("Test");
            } catch (ImageException e) {
                caught = true;
            }

            assertThat(caught).isTrue();
        }
    }
}
