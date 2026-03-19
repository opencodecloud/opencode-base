package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureException 测试")
class FeatureExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            FeatureException ex = new FeatureException("Error message");

            assertThat(ex.getMessage()).isEqualTo("Error message");
            assertThat(ex.getCause()).isNull();
            assertThat(ex.getFeatureKey()).isNull();
            assertThat(ex.getErrorCode()).isEqualTo(FeatureErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureException ex = new FeatureException("Error message", cause);

            assertThat(ex.getMessage()).isEqualTo("Error message");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getErrorCode()).isEqualTo(FeatureErrorCode.UNKNOWN);
        }

        @Test
        @DisplayName("消息和错误码构造")
        void testMessageAndErrorCodeConstructor() {
            FeatureException ex = new FeatureException("Error", FeatureErrorCode.NOT_FOUND);

            assertThat(ex.getMessage()).isEqualTo("Error");
            assertThat(ex.getErrorCode()).isEqualTo(FeatureErrorCode.NOT_FOUND);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureException ex = new FeatureException(
                    "Error", cause, "feature-key", FeatureErrorCode.STORE_ERROR
            );

            assertThat(ex.getMessage()).isEqualTo("Error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getFeatureKey()).isEqualTo("feature-key");
            assertThat(ex.getErrorCode()).isEqualTo(FeatureErrorCode.STORE_ERROR);
        }

        @Test
        @DisplayName("null错误码使用UNKNOWN")
        void testNullErrorCodeUsesUnknown() {
            FeatureException ex = new FeatureException("Error", null, null, null);

            assertThat(ex.getErrorCode()).isEqualTo(FeatureErrorCode.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承RuntimeException")
        void testExtendsRuntimeException() {
            FeatureException ex = new FeatureException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("可抛出和捕获")
        void testThrowAndCatch() {
            assertThatThrownBy(() -> {
                throw new FeatureException("Test error");
            }).isInstanceOf(FeatureException.class)
              .hasMessage("Test error");
        }
    }
}
