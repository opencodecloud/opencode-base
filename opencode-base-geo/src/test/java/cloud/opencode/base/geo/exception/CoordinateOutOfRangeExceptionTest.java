package cloud.opencode.base.geo.exception;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateOutOfRangeException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateOutOfRangeException 测试")
class CoordinateOutOfRangeExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            CoordinateOutOfRangeException ex = new CoordinateOutOfRangeException("Out of range");

            assertThat(ex.getMessage()).isEqualTo("Out of range");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("消息和坐标构造函数")
        void testMessageAndCoordinateConstructor() {
            Coordinate coord = Coordinate.wgs84(200, 100);
            CoordinateOutOfRangeException ex = new CoordinateOutOfRangeException("Out of range", coord);

            assertThat(ex.getMessage()).isEqualTo("Out of range");
            assertThat(ex.getCoordinate()).isEqualTo(coord);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("forLongitude创建经度异常")
        void testForLongitude() {
            CoordinateOutOfRangeException ex = CoordinateOutOfRangeException.forLongitude(200.0);

            assertThat(ex.getMessage()).contains("Longitude").contains("-180").contains("180").contains("200.0");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("forLatitude创建纬度异常")
        void testForLatitude() {
            CoordinateOutOfRangeException ex = CoordinateOutOfRangeException.forLatitude(100.0);

            assertThat(ex.getMessage()).contains("Latitude").contains("-90").contains("90").contains("100.0");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            CoordinateOutOfRangeException ex = new CoordinateOutOfRangeException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }
    }
}
