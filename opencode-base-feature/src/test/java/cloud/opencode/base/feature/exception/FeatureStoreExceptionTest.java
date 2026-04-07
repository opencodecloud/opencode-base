package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureStoreException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureStoreException 测试")
class FeatureStoreExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅消息构造")
        void testMessageConstructor() {
            FeatureStoreException ex = new FeatureStoreException("Store error");

            assertThat(ex.getRawMessage()).isEqualTo("Store error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (3001) Store error");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.STORE_ERROR);
        }

        @Test
        @DisplayName("消息和原因构造")
        void testMessageAndCauseConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureStoreException ex = new FeatureStoreException("Store error", cause);

            assertThat(ex.getRawMessage()).isEqualTo("Store error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (3001) Store error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.STORE_ERROR);
        }

        @Test
        @DisplayName("全参数构造")
        void testFullConstructor() {
            Throwable cause = new RuntimeException("cause");
            FeatureStoreException ex = new FeatureStoreException(
                    "Error", cause, "feature-key", FeatureErrorCode.PERSIST_FAILED
            );

            assertThat(ex.getRawMessage()).isEqualTo("Error");
            assertThat(ex.getMessage()).isEqualTo("[feature] (3002) Error");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getFeatureKey()).isEqualTo("feature-key");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.PERSIST_FAILED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承FeatureException")
        void testExtendsFeatureException() {
            FeatureStoreException ex = new FeatureStoreException("error");

            assertThat(ex).isInstanceOf(FeatureException.class);
        }
    }
}
