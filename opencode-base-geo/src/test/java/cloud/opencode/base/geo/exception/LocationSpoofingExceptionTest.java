package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LocationSpoofingException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("LocationSpoofingException 测试")
class LocationSpoofingExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            LocationSpoofingException ex = new LocationSpoofingException("Location spoofing detected");

            assertThat(ex.getRawMessage()).isEqualTo("Location spoofing detected");
            assertThat(ex.getMessage()).contains("Location spoofing detected");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            LocationSpoofingException ex = new LocationSpoofingException("Location spoofing detected", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Location spoofing detected");
            assertThat(ex.getMessage()).contains("Location spoofing detected");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoSecurityException子类")
        void testIsGeoSecurityException() {
            LocationSpoofingException ex = new LocationSpoofingException("test");

            assertThat(ex).isInstanceOf(GeoSecurityException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            LocationSpoofingException ex = new LocationSpoofingException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            LocationSpoofingException ex = new LocationSpoofingException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
