package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
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

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.UNKNOWN);
            assertThat(ex.getCoordinate()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            GeoException ex = new GeoException("test error", GeoErrorCode.INVALID_COORDINATE);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_COORDINATE);
            assertThat(ex.getCoordinate()).isNull();
        }

        @Test
        @DisplayName("消息、原因和错误码构造函数")
        void testMessageCauseAndErrorCodeConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GeoException ex = new GeoException("test error", cause, GeoErrorCode.TRANSFORM_FAILED);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.TRANSFORM_FAILED);
        }

        @Test
        @DisplayName("全参数构造函数")
        void testFullConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            Coordinate coord = Coordinate.wgs84(116.4074, 39.9042);
            GeoException ex = new GeoException("test error", cause, GeoErrorCode.COORDINATE_OUT_OF_RANGE, coord);

            assertThat(ex.getMessage()).contains("test error");
            assertThat(ex.getRawMessage()).isEqualTo("test error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.COORDINATE_OUT_OF_RANGE);
            assertThat(ex.getCoordinate()).isEqualTo(coord);
        }

        @Test
        @DisplayName("null错误码使用UNKNOWN")
        void testNullErrorCodeUsesUnknown() {
            GeoException ex = new GeoException("test", null, null, null);

            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("getGeoErrorCode()测试")
    class GetGeoErrorCodeTests {

        @Test
        @DisplayName("返回正确的错误码")
        void testGetGeoErrorCode() {
            GeoException ex = new GeoException("test", GeoErrorCode.FENCE_NOT_FOUND);

            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.FENCE_NOT_FOUND);
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
    @DisplayName("OpenException集成测试")
    class OpenExceptionIntegrationTests {

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            GeoException ex = new GeoException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("组件名为Geo")
        void testComponentIsGeo() {
            GeoException ex = new GeoException("test");

            assertThat(ex.getComponent()).isEqualTo("Geo");
        }

        @Test
        @DisplayName("getErrorCode返回数字码字符串")
        void testGetErrorCodeReturnsStringCode() {
            GeoException ex = new GeoException("test", GeoErrorCode.INVALID_COORDINATE);

            assertThat(ex.getErrorCode()).isEqualTo(String.valueOf(GeoErrorCode.INVALID_COORDINATE.getCode()));
        }

        @Test
        @DisplayName("getMessage格式化为[Geo] (code) message")
        void testGetMessageFormat() {
            GeoException ex = new GeoException("test error", GeoErrorCode.INVALID_COORDINATE);

            assertThat(ex.getMessage()).isEqualTo("[Geo] (1001) test error");
        }

        @Test
        @DisplayName("getRawMessage返回原始消息")
        void testGetRawMessage() {
            GeoException ex = new GeoException("raw message", GeoErrorCode.UNKNOWN);

            assertThat(ex.getRawMessage()).isEqualTo("raw message");
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
              .isInstanceOf(OpenException.class);
        }
    }
}
