package cloud.opencode.base.geo.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * TimestampException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("TimestampException 测试")
class TimestampExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            TimestampException ex = new TimestampException("Invalid timestamp");

            assertThat(ex.getRawMessage()).isEqualTo("Invalid timestamp");
            assertThat(ex.getMessage()).contains("Invalid timestamp");
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_TIMESTAMP);
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            TimestampException ex = new TimestampException("Invalid timestamp", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Invalid timestamp");
            assertThat(ex.getMessage()).contains("Invalid timestamp");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getGeoErrorCode()).isEqualTo(GeoErrorCode.INVALID_TIMESTAMP);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoSecurityException子类")
        void testIsGeoSecurityException() {
            TimestampException ex = new TimestampException("test");

            assertThat(ex).isInstanceOf(GeoSecurityException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            TimestampException ex = new TimestampException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }

        @Test
        @DisplayName("是OpenException子类")
        void testIsOpenException() {
            TimestampException ex = new TimestampException("test");

            assertThat(ex).isInstanceOf(OpenException.class);
        }
    }
}
