package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureSecurityException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureSecurityException 测试")
class FeatureSecurityExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            FeatureSecurityException ex = new FeatureSecurityException("Security error");

            assertThat(ex.getRawMessage()).isEqualTo("Security error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (4001) Security error");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("消息和错误码构造")
        void testMessageAndErrorCodeConstructor() {
            FeatureSecurityException ex = new FeatureSecurityException(
                    "Audit error", FeatureErrorCode.AUDIT_FAILED
            );

            assertThat(ex.getRawMessage()).isEqualTo("Audit error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (4002) Audit error");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.AUDIT_FAILED);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureSecurityException ex = new FeatureSecurityException("Security error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Security error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (4001) Security error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承FeatureException")
        void testExtendsFeatureException() {
            FeatureSecurityException ex = new FeatureSecurityException("error");

            assertThat(ex).isInstanceOf(FeatureException.class);
        }
    }
}
