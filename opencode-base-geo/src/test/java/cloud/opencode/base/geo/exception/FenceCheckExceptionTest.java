package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FenceCheckException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("FenceCheckException 测试")
class FenceCheckExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造函数")
        void testMessageOnlyConstructor() {
            FenceCheckException ex = new FenceCheckException("Fence check failed");

            assertThat(ex.getMessage()).isEqualTo("Fence check failed");
        }

        @Test
        @DisplayName("消息和原因构造函数")
        void testMessageAndCauseConstructor() {
            RuntimeException cause = new RuntimeException("cause");
            FenceCheckException ex = new FenceCheckException("Fence check failed", cause);

            assertThat(ex.getMessage()).isEqualTo("Fence check failed");
            assertThat(ex.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是FenceException子类")
        void testIsFenceException() {
            FenceCheckException ex = new FenceCheckException("test");

            assertThat(ex).isInstanceOf(FenceException.class);
        }

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            FenceCheckException ex = new FenceCheckException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }
    }
}
