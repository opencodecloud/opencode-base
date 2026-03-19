package cloud.opencode.base.geo.exception;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidCoordinateException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("InvalidCoordinateException 测试")
class InvalidCoordinateExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            InvalidCoordinateException ex = new InvalidCoordinateException("Invalid coordinate");

            assertThat(ex.getMessage()).isEqualTo("Invalid coordinate");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }

        @Test
        @DisplayName("消息和坐标构造函数")
        void testMessageAndCoordinateConstructor() {
            Coordinate coord = Coordinate.wgs84(200, 100);
            InvalidCoordinateException ex = new InvalidCoordinateException("Invalid coordinate", coord);

            assertThat(ex.getMessage()).isEqualTo("Invalid coordinate");
            assertThat(ex.getCoordinate()).isEqualTo(coord);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }

        @Test
        @DisplayName("经纬度构造函数")
        void testLongLatConstructor() {
            InvalidCoordinateException ex = new InvalidCoordinateException(200.0, 100.0);

            assertThat(ex.getMessage()).contains("200.0").contains("100.0");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            InvalidCoordinateException ex = new InvalidCoordinateException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            InvalidCoordinateException ex = new InvalidCoordinateException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
