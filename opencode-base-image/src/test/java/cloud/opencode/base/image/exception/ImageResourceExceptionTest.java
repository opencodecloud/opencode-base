package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageResourceException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageResourceException 异常测试")
class ImageResourceExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageResourceException ex = new ImageResourceException("Resource unavailable");

            assertThat(ex.getRawMessage()).isEqualTo("Resource unavailable");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.RESOURCE_UNAVAILABLE);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Out of memory");
            ImageResourceException ex = new ImageResourceException("Resource error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Resource error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageResourceException ex = new ImageResourceException("Memory error", ImageErrorCode.OUT_OF_MEMORY);

            assertThat(ex.getRawMessage()).isEqualTo("Memory error");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.OUT_OF_MEMORY);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("tooManyRequests工厂方法")
        void testTooManyRequestsFactory() {
            ImageResourceException ex = ImageResourceException.tooManyRequests();

            assertThat(ex.getRawMessage()).contains("many");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.TOO_MANY_REQUESTS);
        }

        @Test
        @DisplayName("outOfMemory工厂方法")
        void testOutOfMemoryFactory() {
            ImageResourceException ex = ImageResourceException.outOfMemory();

            assertThat(ex.getRawMessage()).contains("memory");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.OUT_OF_MEMORY);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageException")
        void testExtendsImageException() {
            ImageResourceException ex = new ImageResourceException("Test");

            assertThat(ex).isInstanceOf(ImageException.class);
        }

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            ImageResourceException ex = new ImageResourceException("Test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getComponent返回Image")
        void testGetComponentReturnsImage() {
            ImageResourceException ex = new ImageResourceException("Test");

            assertThat(ex.getComponent()).isEqualTo("Image");
        }
    }
}
