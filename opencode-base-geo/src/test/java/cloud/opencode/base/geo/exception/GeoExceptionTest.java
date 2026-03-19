package cloud.opencode.base.geo.exception;

import cloud.opencode.base.geo.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoException 测试")
class GeoExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GeoException ex = new GeoException("test error");

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.UNKNOWN);
            assertThat(ex.getCoordinate()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            GeoException ex = new GeoException("test error", GeoErrorCode.INVALID_COORDINATE);

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
            assertThat(ex.getCoordinate()).isNull();
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GeoException ex = new GeoException("test error", cause, GeoErrorCode.TRANSFORM_FAILED);

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.TRANSFORM_FAILED);
        }

        @Test
        @DisplayName("全参数构造函数")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            GeoException ex = new GeoException("test error", cause, GeoErrorCode.COORDINATE_OUT_OF_RANGE, coord);

            assertThat(ex.getMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
            assertThat(ex.getCoordinate()).isEqualTo(coord);
        }

        @Test
        @DisplayName("null错误码使用UNKNOWN")
        void testNullErrorCodeUsesUnknown() {
            GeoException ex = new GeoException("test", null, null, null);

            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("getErrorCode()测试")
    class GetErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetErrorCode() {
            GeoException ex = new GeoException("test", GeoErrorCode.FENCE_NOT_FOUND);

            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.FENCE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getCoordinate()测试")
    class GetCoordinateTests {

        @Test
        @DisplayName("返回正确的坐标")
        void testGetCoordinate() {
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            GeoException ex = new GeoException("test", null, GeoErrorCode.UNKNOWN, coord);

            assertThat(ex.getCoordinate()).isEqualTo(coord);
        }

        @Test
        @DisplayName("无坐标返回null")
        void testGetCoordinateNull() {
            GeoException ex = new GeoException("test");

            assertThat(ex.getCoordinate()).isNull();
        }
    }

    @Nested
    @DisplayName("继承RuntimeException测试")
    class InheritanceTests {

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            GeoException ex = new GeoException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可以被抛出和捕获")
        void testThrowAndCatch() {
            assertThatThrownBy(() -> {
                throw new GeoException("test error");
            }).isInstanceOf(GeoException.class)
              .hasMessage("test error");
        }
    }
}
