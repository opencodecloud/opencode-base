package cloud.opencode.base.geo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FenceNotFoundException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-geo V1.0.0
 */
@DisplayName("FenceNotFoundException 测试")
class FenceNotFoundExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("fenceId构造函数")
        void testFenceIdConstructor() {
            FenceNotFoundException ex = new FenceNotFoundException("test-fence");

            assertThat(ex.getMessage()).contains("Fence not found").contains("test-fence");
            assertThat(ex.getFenceId()).isEqualTo("test-fence");
            assertThat(ex.getErrorCode()).isEqualTo(GeoErrorCode.FENCE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getFenceId()测试")
    class GetFenceIdTests {

        @Test
        @DisplayName("返回正确的fenceId")
        void testGetFenceId() {
            FenceNotFoundException ex = new FenceNotFoundException("my-fence-123");

            assertThat(ex.getFenceId()).isEqualTo("my-fence-123");
        }

        @Test
        @DisplayName("null fenceId")
        void testNullFenceId() {
            FenceNotFoundException ex = new FenceNotFoundException(null);

            assertThat(ex.getFenceId()).isNull();
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("是GeoException子类")
        void testIsGeoException() {
            FenceNotFoundException ex = new FenceNotFoundException("test");

            assertThat(ex).isInstanceOf(GeoException.class);
        }
    }
}
