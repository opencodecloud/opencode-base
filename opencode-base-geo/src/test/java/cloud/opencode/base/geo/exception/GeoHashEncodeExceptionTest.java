package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoHashEncodeException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoHashEncodeException 测试")
class GeoHashEncodeExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GeoHashEncodeException ex = new GeoHashEncodeException("Encode failed");

            assertThat(ex.getRawMessage()).isEqualTo("Encode failed");
            assertThat(ex.getMessage()).contains("Encode failed");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            GeoHashEncodeException ex = new GeoHashEncodeException("Encode failed", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Encode failed");
            assertThat(ex.getMessage()).contains("Encode failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoHashException子类")
        void testIsGeoHashException() {
            GeoHashEncodeException ex = new GeoHashEncodeException("test");

            assertThat(ex).isInstanceOf(GeoHashException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            GeoHashEncodeException ex = new GeoHashEncodeException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            GeoHashEncodeException ex = new GeoHashEncodeException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
