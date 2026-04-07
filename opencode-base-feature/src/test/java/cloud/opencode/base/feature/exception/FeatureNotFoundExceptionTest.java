package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureNotFoundException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureNotFoundException 测试")
class FeatureNotFoundExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用功能键创建")
        void testConstructor() {
            FeatureNotFoundException ex = new FeatureNotFoundException("my-feature");

            assertThat(ex.getMessage()).isEqualTo("[feature] (1001) Feature not found: my-feature");
            assertThat(ex.getFeatureKey()).isEqualTo("my-feature");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承FeatureException")
        void testExtendsFeatureException() {
            FeatureNotFoundException ex = new FeatureNotFoundException("key");

            assertThat(ex).isInstanceOf(FeatureException.class);
        }
    }
}
