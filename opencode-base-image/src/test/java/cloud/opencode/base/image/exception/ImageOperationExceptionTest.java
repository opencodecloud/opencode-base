package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageOperationException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageOperationException 异常测试")
class ImageOperationExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageOperationException ex = new ImageOperationException("Operation failed");

            assertThat(ex.getMessage()).isEqualTo("Operation failed");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.INVALID_PARAMETERS);
            assertThat(ex.getOperation()).isNull();
        }

        @Test
        @DisplayName("带消息和操作名构造")
        void testConstructorWithMessageAndOperation() {
            ImageOperationException ex = new ImageOperationException("Resize failed", "resize");

            assertThat(ex.getMessage()).isEqualTo("Resize failed");
            assertThat(ex.getOperation()).isEqualTo("resize");
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageOperationException ex = new ImageOperationException("Crop failed", ImageErrorCode.CROP_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Crop failed");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.CROP_FAILED);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Internal error");
            ImageOperationException ex = new ImageOperationException("Op failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Op failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带消息、原因和错误码构造")
        void testConstructorWithAllParams() {
            Throwable cause = new RuntimeException("Error");
            ImageOperationException ex = new ImageOperationException("Rotate failed", cause, ImageErrorCode.ROTATE_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Rotate failed");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.ROTATE_FAILED);
        }
    }

    @Nested
    @DisplayName("getOperation方法测试")
    class GetOperationTests {

        @Test
        @DisplayName("返回正确的操作名")
        void testReturnsCorrectOperation() {
            ImageOperationException ex = new ImageOperationException("Error", "watermark");

            assertThat(ex.getOperation()).isEqualTo("watermark");
        }

        @Test
        @DisplayName("无操作名时返回null")
        void testReturnsNullWhenNoOperation() {
            ImageOperationException ex = new ImageOperationException("Error");

            assertThat(ex.getOperation()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageException")
        void testExtendsImageException() {
            ImageOperationException ex = new ImageOperationException("Test");

            assertThat(ex).isInstanceOf(ImageException.class);
        }
    }
}
