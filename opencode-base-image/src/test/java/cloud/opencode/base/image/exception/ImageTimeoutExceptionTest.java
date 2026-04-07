package cloud.opencode.base.image.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * ImageTimeoutException 异常测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V1.0.3
 */
@DisplayName("ImageTimeoutException 异常测试")
class ImageTimeoutExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("带消息构造")
        void testConstructorWithMessage() {
            ImageTimeoutException ex = new ImageTimeoutException("Operation timed out");

            assertThat(ex.getRawMessage()).isEqualTo("Operation timed out");
            assertThat(ex.getImageErrorCode()).isEqualTo(ImageErrorCode.TIMEOUT);
            assertThat(ex.getTimeout()).isNull();
        }

        @Test
        @DisplayName("带消息和超时时间构造")
        void testConstructorWithMessageAndTimeout() {
            Duration timeout = Duration.ofSeconds(30);
            ImageTimeoutException ex = new ImageTimeoutException("Timeout", timeout);

            assertThat(ex.getRawMessage()).contains("30");
            assertThat(ex.getTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("带消息和原因构造")
        void testConstructorWithMessageAndCause() {
            Throwable cause = new RuntimeException("Thread interrupted");
            ImageTimeoutException ex = new ImageTimeoutException("Timeout error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Timeout error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getTimeout()).isNull();
        }
    }

    @Nested
    @DisplayName("getTimeout方法测试")
    class GetTimeoutTests {

        @Test
        @DisplayName("返回正确的超时时间")
        void testReturnsCorrectTimeout() {
            Duration timeout = Duration.ofMinutes(5);
            ImageTimeoutException ex = new ImageTimeoutException("Timeout", timeout);

            assertThat(ex.getTimeout()).isEqualTo(timeout);
        }

        @Test
        @DisplayName("无超时时间时返回null")
        void testReturnsNullWhenNoTimeout() {
            ImageTimeoutException ex = new ImageTimeoutException("Timeout");

            assertThat(ex.getTimeout()).isNull();
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承自ImageResourceException")
        void testExtendsImageResourceException() {
            ImageTimeoutException ex = new ImageTimeoutException("Test");

            assertThat(ex).isInstanceOf(ImageResourceException.class);
        }

        @Test
        @DisplayName("继承自OpenException")
        void testExtendsOpenException() {
            ImageTimeoutException ex = new ImageTimeoutException("Test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("getComponent返回Image")
        void testGetComponentReturnsImage() {
            ImageTimeoutException ex = new ImageTimeoutException("Test");

            assertThat(ex.getComponent()).isEqualTo("Image");
        }
    }
}
