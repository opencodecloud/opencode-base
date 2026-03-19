package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateException 测试")
class CoordinateExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            CoordinateException ex = new CoordinateException("Coordinate error");

            assertThat(ex.getMessage()).isEqualTo("Coordinate error");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            CoordinateException ex = new CoordinateException("Coordinate error", cause);

            assertThat(ex.getMessage()).isEqualTo("Coordinate error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            CoordinateException ex = new CoordinateException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }
    }
}
