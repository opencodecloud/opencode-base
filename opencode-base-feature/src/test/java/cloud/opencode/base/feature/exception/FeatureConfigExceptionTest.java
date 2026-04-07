package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureConfigException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureConfigException 测试")
class FeatureConfigExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            FeatureConfigException ex = new FeatureConfigException("Config error");

            assertThat(ex.getRawMessage()).isEqualTo("Config error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (2003) Config error");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.INVALID_CONFIG);
        }

        @Test
        @DisplayName("消息和错误码构造")
        void testMessageAndErrorCodeConstructor() {
            FeatureConfigException ex = new FeatureConfigException(
                    "Strategy error", FeatureErrorCode.INVALID_STRATEGY
            );

            assertThat(ex.getRawMessage()).isEqualTo("Strategy error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (2001) Strategy error");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.INVALID_STRATEGY);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureConfigException ex = new FeatureConfigException("Config error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Config error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (2003) Config error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.INVALID_CONFIG);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承FeatureException")
        void testExtendsFeatureException() {
            FeatureConfigException ex = new FeatureConfigException("error");

            assertThat(ex).isInstanceOf(FeatureException.class);
        }
    }
}
