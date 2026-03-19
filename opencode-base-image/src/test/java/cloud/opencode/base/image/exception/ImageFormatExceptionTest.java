package cloud.opencode.base.image.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageFormatException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.0
 */
@DisplayName("ImageFormatException 异常测试")
class ImageFormatExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageFormatException ex = new ImageFormatException("Invalid format");

            assertThat(ex.getMessage()).isEqualTo("Invalid format");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.UNSUPPORTED_FORMAT);
            assertThat(ex.getFormat()).isNull();
        }

        @Test
        @DisplayName("带消息和格式构造")
        void testConstructorWithMessageAndFormat() {
            ImageFormatException ex = new ImageFormatException("Unsupported", "tiff");

            assertThat(ex.getMessage()).isEqualTo("Unsupported");
            assertThat(ex.getFormat()).isEqualTo("tiff");
        }

        @Test
        @DisplayName("带消息和错误码构造")
        void testConstructorWithMessageAndErrorCode() {
            ImageFormatException ex = new ImageFormatException("Invalid", ImageErrorCode.INVALID_IMAGE);

            assertThat(ex.getMessage()).isEqualTo("Invalid");
            assertThat(ex.getErrorCode()).isEqualTo(ImageErrorCode.INVALID_IMAGE);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Parse error");
            ImageFormatException ex = new ImageFormatException("Format error", cause);

            assertThat(ex.getMessage()).isEqualTo("Format error");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("unsupported工厂方法")
        void testUnsupportedFactory() {
            ImageFormatException ex = ImageFormatException.unsupported("tiff");

            assertThat(ex.getMessage()).contains("tiff");
            assertThat(ex.getMessage()).contains("Unsupported");
            assertThat(ex.getFormat()).isEqualTo("tiff");
        }
    }

    @Nested
    @DisplayName("getFormat方法测试")
    class GetFormatTests {

        @Test
        @DisplayName("返回正确的格式")
        void testReturnsCorrectFormat() {
            ImageFormatException ex = new ImageFormatException("Error", "webp");

            assertThat(ex.getFormat()).isEqualTo("webp");
        }

        @Test
        @DisplayName("无格式时返回null")
        void testReturnsNullWhenNoFormat() {
            ImageFormatException ex = new ImageFormatException("Error");

            assertThat(ex.getFormat()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageException")
        void testExtendsImageException() {
            ImageFormatException ex = new ImageFormatException("Test");

            assertThat(ex).isInstanceOf(ImageException.class);
        }
    }
}
