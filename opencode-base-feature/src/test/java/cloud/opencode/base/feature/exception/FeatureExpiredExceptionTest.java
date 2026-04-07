package cloud.opencode.base.feature.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureExpiredException 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("FeatureExpiredException 测试")
class FeatureExpiredExceptionTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用功能键创建")
        void testConstructor() {
            FeatureExpiredException ex = new FeatureExpiredException("promo-banner");

            assertThat(ex.getMessage()).isEqualTo("[feature] (1004) Feature expired: promo-banner");
            assertThat(ex.getFeatureKey()).isEqualTo("promo-banner");
            assertThat(ex.getFeatureErrorCode()).isEqualTo(FeatureErrorCode.EXPIRED);
        }
    }

    @Nested
    @DisplayName("继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("继承FeatureException")
        void testExtendsFeatureException() {
            FeatureExpiredException ex = new FeatureExpiredException("key");

            assertThat(ex).isInstanceOf(FeatureException.class);
        }
    }
}
