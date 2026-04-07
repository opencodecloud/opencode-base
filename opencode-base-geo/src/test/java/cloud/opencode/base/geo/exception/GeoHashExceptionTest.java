package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoHashException 测试")
class GeoHashExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GeoHashException ex = new GeoHashException("GeoHash error");

            assertThat(ex.getRawMessage()).isEqualTo("GeoHash error");
            assertThat(ex.getMessage()).contains("GeoHash error");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_GEOHASH);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GeoHashException ex = new GeoHashException("GeoHash error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("GeoHash error");
            assertThat(ex.getMessage()).contains("GeoHash error");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_GEOHASH);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            GeoHashException ex = new GeoHashException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            GeoHashException ex = new GeoHashException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
