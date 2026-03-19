package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CoordinateTransformException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("CoordinateTransformException 测试")
class CoordinateTransformExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            CoordinateTransformException ex = new CoordinateTransformException("Transform failed");

            assertThat(ex.getMessage()).isEqualTo("Transform failed");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            CoordinateTransformException ex = new CoordinateTransformException("Transform failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Transform failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是CoordinateException子类")
        void testIsCoordinateException() {
            CoordinateTransformException ex = new CoordinateTransformException("test");

            assertThat(ex).isInstanceOf(CoordinateException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            CoordinateTransformException ex = new CoordinateTransformException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }
    }
}
