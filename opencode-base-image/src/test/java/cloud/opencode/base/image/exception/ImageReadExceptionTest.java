package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageReadException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageReadException 异常测试")
class ImageReadExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageReadException ex = new ImageReadException("Failed to read");

            assertThat(ex.getMessage()).isEqualTo("Failed to read");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.READ_FAILED);
            assertThat(ex.getPath()).isNull();
        }

        @Test
        @DisplayName("带路径构造")
        void testConstructorWithPath() {
            Path path = Path.of("/test/image.jpg");
            ImageReadException ex = new ImageReadException(path);

            assertThat(ex.getMessage()).contains("/test/image.jpg");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.READ_FAILED);
            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("带路径和原因构造")
        void testConstructorWithPathAndCause() {
            Path path = Path.of("/test/image.jpg");
            Throwable cause = new RuntimeException("Root cause");
            ImageReadException ex = new ImageReadException(path, cause);

            assertThat(ex.getMessage()).contains("/test/image.jpg");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            ImageReadException ex = new ImageReadException("Read error", cause);

            assertThat(ex.getMessage()).isEqualTo("Read error");
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
            Path path = Path.of("/images/test.png");
            ImageReadException ex = new ImageReadException(path);

            assertThat(ex.getPath()).isEqualTo(path);
        }

        @Test
        @DisplayName("无路径时返回null")
        void testReturnsNullWhenNoPath() {
            ImageReadException ex = new ImageReadException("Error");

            assertThat(ex.getPath()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageIOException")
        void testExtendsImageIOException() {
            ImageReadException ex = new ImageReadException("Test");

            assertThat(ex).isInstanceOf(ImageIOException.class);
        }
    }
}
