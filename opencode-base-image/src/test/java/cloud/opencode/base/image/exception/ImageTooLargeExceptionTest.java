package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageTooLargeException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageTooLargeException 异常测试")
class ImageTooLargeExceptionTest {

    @Nested
    @DisplayName("尺寸构造函数测试")
    class DimensionsConstructorTests {

        @Test
        @DisplayName("创建尺寸过大异常")
        void testDimensionsConstructor() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);

            assertThat(ex.getWidth()).isEqualTo(5000);
            assertThat(ex.getHeight()).isEqualTo(4000);
            assertThat(ex.getMaxWidth()).isEqualTo(4000);
            assertThat(ex.getMaxHeight()).isEqualTo(3000);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.IMAGE_TOO_LARGE);
        }

        @Test
        @DisplayName("消息包含尺寸信息")
        void testDimensionsMessage() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);

            assertThat(ex.getMessage()).contains("5000");
            assertThat(ex.getMessage()).contains("4000");
        }

        @Test
        @DisplayName("文件大小默认为0")
        void testFileSizeDefaultsToZero() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);

            assertThat(ex.getFileSize()).isEqualTo(0);
            assertThat(ex.getMaxFileSize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("文件大小构造函数测试")
    class FileSizeConstructorTests {

        @Test
        @DisplayName("创建文件过大异常")
        void testFileSizeConstructor() {
            ImageTooLargeException ex = new ImageTooLargeException(15_000_000L, 10_000_000L);

            assertThat(ex.getFileSize()).isEqualTo(15_000_000L);
            assertThat(ex.getMaxFileSize()).isEqualTo(10_000_000L);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.FILE_TOO_LARGE);
        }

        @Test
        @DisplayName("消息包含文件大小信息")
        void testFileSizeMessage() {
            ImageTooLargeException ex = new ImageTooLargeException(15_000_000L, 10_000_000L);

            assertThat(ex.getMessage()).contains("15000000");
            assertThat(ex.getMessage()).contains("10000000");
        }

        @Test
        @DisplayName("尺寸默认为0")
        void testDimensionsDefaultToZero() {
            ImageTooLargeException ex = new ImageTooLargeException(15_000_000L, 10_000_000L);

            assertThat(ex.getWidth()).isEqualTo(0);
            assertThat(ex.getHeight()).isEqualTo(0);
            assertThat(ex.getMaxWidth()).isEqualTo(0);
            assertThat(ex.getMaxHeight()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("消息构造函数测试")
    class MessageConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testMessageConstructor() {
            ImageTooLargeException ex = new ImageTooLargeException("Custom message");

            assertThat(ex.getMessage()).isEqualTo("Custom message");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.IMAGE_TOO_LARGE);
            assertThat(ex.getWidth()).isEqualTo(0);
            assertThat(ex.getFileSize()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getter方法测试")
    class GetterMethodsTests {

        @Test
        @DisplayName("getWidth返回正确的值")
        void testGetWidth() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);
            assertThat(ex.getWidth()).isEqualTo(5000);
        }

        @Test
        @DisplayName("getHeight返回正确的值")
        void testGetHeight() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);
            assertThat(ex.getHeight()).isEqualTo(4000);
        }

        @Test
        @DisplayName("getMaxWidth返回正确的值")
        void testGetMaxWidth() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);
            assertThat(ex.getMaxWidth()).isEqualTo(4000);
        }

        @Test
        @DisplayName("getMaxHeight返回正确的值")
        void testGetMaxHeight() {
            ImageTooLargeException ex = new ImageTooLargeException(5000, 4000, 4000, 3000);
            assertThat(ex.getMaxHeight()).isEqualTo(3000);
        }

        @Test
        @DisplayName("getFileSize返回正确的值")
        void testGetFileSize() {
            ImageTooLargeException ex = new ImageTooLargeException(15_000_000L, 10_000_000L);
            assertThat(ex.getFileSize()).isEqualTo(15_000_000L);
        }

        @Test
        @DisplayName("getMaxFileSize返回正确的值")
        void testGetMaxFileSize() {
            ImageTooLargeException ex = new ImageTooLargeException(15_000_000L, 10_000_000L);
            assertThat(ex.getMaxFileSize()).isEqualTo(10_000_000L);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageValidationException")
        void testExtendsImageValidationException() {
            ImageTooLargeException ex = new ImageTooLargeException("Test");

            assertThat(ex).isInstanceOf(ImageValidationException.class);
        }
    }
}
