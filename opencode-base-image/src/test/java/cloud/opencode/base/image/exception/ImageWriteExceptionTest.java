package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageWriteException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageWriteException 异常测试")
class ImageWriteExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageWriteException ex = new ImageWriteException("Failed to write");

            assertThat(ex.getRawMessage()).isEqualTo("Failed to write");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.WRITE_FAILED);
            assertThat(ex.getPath()).isNull();
        }

        @Test
        @DisplayName("带路径构造")
        void testConstructorWithPath() {
            Path path = Path.of("/test/output.jpg");
            ImageWriteException ex = new ImageWriteException(path);

            assertThat(ex.getRawMessage()).contains("/test/output.jpg");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.WRITE_FAILED);
            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("带路径和原因构造")
        void testConstructorWithPathAndCause() {
            Path path = Path.of("/test/output.jpg");
            Throwable cause = new RuntimeException("Disk full");
            ImageWriteException ex = new ImageWriteException(path, cause);

            assertThat(ex.getRawMessage()).contains("/test/output.jpg");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Write error");
            ImageWriteException ex = new ImageWriteException("Write failed", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Write failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("getPath方法测试")
    class GetPathTests {

        @Test
        @DisplayName("返回正确的路径")
        void testReturnsCorrectPath() {
            Path path = Path.of("/output/result.png");
            ImageWriteException ex = new ImageWriteException(path);

            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("无路径时返回null")
        void testReturnsNullWhenNoPath() {
            ImageWriteException ex = new ImageWriteException("Error");

            assertThat(ex.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageIOException")
        void testExtendsImageIOException() {
            ImageWriteException ex = new ImageWriteException("Test");

            assertThat(ex).isInstanceOf(ImageIOException.class);
        }

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            ImageWriteException ex = new ImageWriteException("Test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getComponent返回Image")
        void testGetComponentReturnsImage() {
            ImageWriteException ex = new ImageWriteException("Test");

            assertThat(ex.getComponent()).isEqualTo("Image");
        }
    }
}
