package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EnableStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("EnableStrategy 测试")
class EnableStrategyTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Lambda实现")
        void testLambdaImplementation() {
            EnableStrategy strategy = (feature, context) -> true;
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("基于上下文的Lambda实现")
        void testContextBasedLambda() {
            EnableStrategy strategy = (feature, context) -> 
                "admin".equals(context.getAttribute("role", ""));
            Feature feature = Feature.builder("test").build();

            FeatureContext adminContext = FeatureContext.builder()
                    .attribute("role", "admin")
                    .build();
            FeatureContext userContext = FeatureContext.builder()
                    .attribute("role", "user")
                    .build();

            assertThat(strategy.isEnabled(feature, adminContext)).isTrue();
            assertThat(strategy.isEnabled(feature, userContext)).isFalse();
        }

        @Test
        @DisplayName("方法引用实现")
        void testMethodReferenceImplementation() {
            EnableStrategy strategy = EnableStrategyTest::customStrategy;
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("vip"))).isTrue();
            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("normal"))).isFalse();
        }
    }

    @Nested
    @DisplayName("匿名类实现测试")
    class AnonymousClassTests {

        @Test
        @DisplayName("匿名类实现")
        void testAnonymousClassImplementation() {
            EnableStrategy strategy = new EnableStrategy() {
                @Override
                public boolean isEnabled(Feature feature, FeatureContext context) {
                    return feature.key().startsWith("enabled-");
                }
            };

            Feature enabled = Feature.builder("enabled-feature").build();
            Feature disabled = Feature.builder("disabled-feature").build();

            assertThat(strategy.isEnabled(enabled, FeatureContext.empty())).isTrue();
            assertThat(strategy.isEnabled(disabled, FeatureContext.empty())).isFalse();
        }
    }

    private static boolean customStrategy(Feature feature, FeatureContext context) {
        return "vip".equals(context.userId());
    }
}
