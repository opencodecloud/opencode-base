package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageException 异常测试")
class ImageExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageException ex = new ImageException("Test error", ImageErrorCode.READ_FAILED);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.READ_FAILED);
            assertThat(ex.getErrorCode()).isEqualTo("READ_FAILED");
            assertThat(ex.getComponent()).isEqualTo("Image");
        }

        @Test
        @DisplayName("带消息、原因和错误码构造")
        void testConstructorWithMessageCauseAndErrorCode() {
            Throwable cause = new RuntimeException("Cause");
            ImageException ex = new ImageException("Test error", cause, ImageErrorCode.WRITE_FAILED);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.WRITE_FAILED);
            assertThat(ex.getErrorCode()).isEqualTo("WRITE_FAILED");
            assertThat(ex.getComponent()).isEqualTo("Image");
        }

        @Test
        @DisplayName("仅带消息构造")
        void testConstructorWithMessageOnly() {
            ImageException ex = new ImageException("Test error");

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
            assertThat(ex.getErrorCode()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Cause");
            ImageException ex = new ImageException("Test error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            ImageException ex = new ImageException("Test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("继承自RuntimeException")
        void testExtendsRuntimeException() {
            ImageException ex = new ImageException("Test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被捕获为Exception")
        void testCatchAsException() {
            boolean caught = false;
            try {
                throw new ImageException("Test");
            } catch (Exception e) {
                caught = true;
            }

            assertThat(caught).isTrue();
        }

        @Test
        @DisplayName("getComponent返回Image")
        void testGetComponentReturnsImage() {
            ImageException ex = new ImageException("Test");

            assertThat(ex.getComponent()).isEqualTo("Image");
        }
    }

    @Nested
    @DisplayName("getImageErrorCode方法测试")
    class GetImageErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testReturnsCorrectErrorCode() {
            ImageException ex = new ImageException("Test", ImageErrorCode.CROP_FAILED);

            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.CROP_FAILED);
        }

        @Test
        @DisplayName("默认错误码为UNKNOWN")
        void testDefaultErrorCode() {
            ImageException ex = new ImageException("Test");

            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("getMessage格式化测试")
    class GetMessageTests {

        @Test
        @DisplayName("getMessage包含组件和错误码")
        void testGetMessageFormat() {
            ImageException ex = new ImageException("Test error", ImageErrorCode.READ_FAILED);

            assertThat(ex.getMessage()).contains("[Image]");
            assertThat(ex.getMessage()).contains("(READ_FAILED)");
            assertThat(ex.getMessage()).contains("Test error");
        }
    }
}
