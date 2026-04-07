package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
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

            assertThat(ex.getRawMessage()).isEqualTo("Coordinate error");
            assertThat(ex.getMessage()).contains("Coordinate error");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            CoordinateException ex = new CoordinateException("Coordinate error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Coordinate error");
            assertThat(ex.getMessage()).contains("Coordinate error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
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

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            CoordinateException ex = new CoordinateException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
