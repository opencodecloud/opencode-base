package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FenceException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("FenceException 测试")
class FenceExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            FenceException ex = new FenceException("Fence error");

            assertThat(ex.getMessage()).isEqualTo("Fence error");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.FENCE_CHECK_FAILED);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            FenceException ex = new FenceException("Fence error", cause);

            assertThat(ex.getMessage()).isEqualTo("Fence error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.FENCE_CHECK_FAILED);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            FenceException ex = new FenceException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            FenceException ex = new FenceException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
