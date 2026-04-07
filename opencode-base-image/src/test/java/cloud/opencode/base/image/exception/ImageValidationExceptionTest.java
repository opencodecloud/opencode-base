package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageValidationException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageValidationException 异常测试")
class ImageValidationExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageValidationException ex = new ImageValidationException("Validation failed");

            assertThat(ex.getRawMessage()).isEqualTo("Validation failed");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.VALIDATION_FAILED);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Invalid data");
            ImageValidationException ex = new ImageValidationException("Validation error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Validation error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageValidationException ex = new ImageValidationException("Too large", ImageErrorCode.IMAGE_TOO_LARGE);

            assertThat(ex.getRawMessage()).isEqualTo("Too large");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.IMAGE_TOO_LARGE);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageException")
        void testExtendsImageException() {
            ImageValidationException ex = new ImageValidationException("Test");

            assertThat(ex).isInstanceOf(ImageException.class);
        }

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            ImageValidationException ex = new ImageValidationException("Test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getComponent返回Image")
        void testGetComponentReturnsImage() {
            ImageValidationException ex = new ImageValidationException("Test");

            assertThat(ex.getComponent()).isEqualTo("Image");
        }
    }
}
