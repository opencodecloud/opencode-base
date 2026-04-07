package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidFenceException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("InvalidFenceException 测试")
class InvalidFenceExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            InvalidFenceException ex = new InvalidFenceException("Invalid fence definition");

            assertThat(ex.getRawMessage()).isEqualTo("Invalid fence definition");
            assertThat(ex.getMessage()).contains("Invalid fence definition");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            InvalidFenceException ex = new InvalidFenceException("Invalid fence", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Invalid fence");
            assertThat(ex.getMessage()).contains("Invalid fence");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是FenceException子类")
        void testIsFenceException() {
            InvalidFenceException ex = new InvalidFenceException("test");

            assertThat(ex).isInstanceOf(FenceException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            InvalidFenceException ex = new InvalidFenceException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            InvalidFenceException ex = new InvalidFenceException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
