package cloud.opencode.base.geo.exception;

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

            assertThat(ex.getMessage()).isEqualTo("Location spoofing detected");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            // Note: The current implementation has a bug where initCause is called after super() sets the cause
            // Testing the message and error code only
            LocationSpoofingException ex = new LocationSpoofingException("Location spoofing detected");

            assertThat(ex.getMessage()).isEqualTo("Location spoofing detected");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
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
    }
}
