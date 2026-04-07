package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * InvalidGeoHashException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("InvalidGeoHashException 测试")
class InvalidGeoHashExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            InvalidGeoHashException ex = new InvalidGeoHashException("Invalid GeoHash");

            assertThat(ex.getRawMessage()).isEqualTo("Invalid GeoHash");
            assertThat(ex.getMessage()).contains("Invalid GeoHash");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_GEOHASH);
            assertThat(ex.getGeoHash()).isNull();
        }

        @Test
        @DisplayName("消息和GeoHash构造函数")
        void testMessageAndGeoHashConstructor() {
            InvalidGeoHashException ex = new InvalidGeoHashException("Invalid GeoHash", "abc!");

            assertThat(ex.getRawMessage()).isEqualTo("Invalid GeoHash");
            assertThat(ex.getMessage()).contains("Invalid GeoHash");
            assertThat(ex.getGeoHash()).isEqualTo("abc!");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_GEOHASH);
        }
    }

    @Nested
    @DisplayName("getGeoHash()测试")
    class GetGeoHashTests {

        @Test
        @DisplayName("返回正确的GeoHash")
        void testGetGeoHash() {
            InvalidGeoHashException ex = new InvalidGeoHashException("test", "invalid123");

            assertThat(ex.getGeoHash()).isEqualTo("invalid123");
        }

        @Test
        @DisplayName("无GeoHash返回null")
        void testGetGeoHashNull() {
            InvalidGeoHashException ex = new InvalidGeoHashException("test");

            assertThat(ex.getGeoHash()).isNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            InvalidGeoHashException ex = new InvalidGeoHashException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            InvalidGeoHashException ex = new InvalidGeoHashException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }

        @Test
        @DisplayName("是RuntimeException子类")
        void testIsRuntimeException() {
            InvalidGeoHashException ex = new InvalidGeoHashException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}
