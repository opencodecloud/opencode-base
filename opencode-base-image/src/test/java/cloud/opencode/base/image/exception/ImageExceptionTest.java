package cloud.opencode.base.image.exception;

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
 * @since JDK 25, opencode-base-image V1.0.0
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

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.READ_FAILED);
        }

        @Test
        @DisplayName("带消息、原因和错误码构造")
        void testConstructorWithMessageCauseAndErrorCode() {
            Throwable cause = new RuntimeException("Cause");
            ImageException ex = new ImageException("Test error", cause, ImageErrorCode.WRITE_FAILED);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.WRITE_FAILED);
        }

        @Test
        @DisplayName("仅带消息构造")
        void testConstructorWithMessageOnly() {
            ImageException ex = new ImageException("Test error");

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Cause");
            ImageException ex = new ImageException("Test error", cause);

            assertThat(ex.getMessage()).isEqualTo("Test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

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
    }

    @Nested
    @DisplayName("getErrorCode方法测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testReturnsCorrectErrorCode() {
            ImageException ex = new ImageException("Test", ImageErrorCode.CROP_FAILED);

            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.CROP_FAILED);
        }

        @Test
        @DisplayName("默认错误码为UNKNOWN")
        void testDefaultErrorCode() {
            ImageException ex = new ImageException("Test");

            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.UNKNOWN);
        }
    }
}
