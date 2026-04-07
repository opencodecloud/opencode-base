package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * GeoSecurityException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("GeoSecurityException 测试")
class GeoSecurityExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            GeoSecurityException ex = new GeoSecurityException("Security violation");

            assertThat(ex.getRawMessage()).isEqualTo("Security violation");
            assertThat(ex.getMessage()).contains("Security violation");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
        }

        @Test
        @DisplayName("消息和错误码构造函数")
        void testMessageAndErrorCodeConstructor() {
            GeoSecurityException ex = new GeoSecurityException("Invalid timestamp", GeoErrorCode.INVALID_TIMESTAMP);

            assertThat(ex.getRawMessage()).isEqualTo("Invalid timestamp");
            assertThat(ex.getMessage()).contains("Invalid timestamp");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_TIMESTAMP);
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("locationSpoofing创建位置欺骗异常")
        void testLocationSpoofing() {
            GeoSecurityException ex = GeoSecurityException.locationSpoofing();

            assertThat(ex.getMessage()).contains("Location spoofing");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.LOCATION_SPOOFING);
        }

        @Test
        @DisplayName("invalidTimestamp创建无效时间戳异常")
        void testInvalidTimestamp() {
            GeoSecurityException ex = GeoSecurityException.invalidTimestamp();

            assertThat(ex.getMessage()).contains("timestamp");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_TIMESTAMP);
        }

        @Test
        @DisplayName("impossibleSpeed创建不可能速度异常")
        void testImpossibleSpeed() {
            GeoSecurityException ex = GeoSecurityException.impossibleSpeed(1500.0);

            assertThat(ex.getMessage()).contains("1500").contains("km/h");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.IMPOSSIBLE_SPEED);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            GeoSecurityException ex = new GeoSecurityException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            GeoSecurityException ex = new GeoSecurityException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
