package cloud.opencode.base.image.exception;

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
 * @since JDK 25, opencode-base-image V1.0.0
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

            assertThat(ex.getMessage()).isEqualTo("Resource unavailable");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.RESOURCE_UNAVAILABLE);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Out of memory");
            ImageResourceException ex = new ImageResourceException("Resource error", cause);

            assertThat(ex.getMessage()).isEqualTo("Resource error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageResourceException ex = new ImageResourceException("Memory error", ImageErrorCode.OUT_OF_MEMORY);

            assertThat(ex.getMessage()).isEqualTo("Memory error");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.OUT_OF_MEMORY);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("tooManyRequests工厂方法")
        void testTooManyRequestsFactory() {
            ImageResourceException ex = ImageResourceException.tooManyRequests();

            assertThat(ex.getMessage()).contains("many");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.TOO_MANY_REQUESTS);
        }

        @Test
        @DisplayName("outOfMemory工厂方法")
        void testOutOfMemoryFactory() {
            ImageResourceException ex = ImageResourceException.outOfMemory();

            assertThat(ex.getMessage()).contains("memory");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.OUT_OF_MEMORY);
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
    }
}
