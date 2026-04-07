package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * EnvironmentStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.3
 */
@DisplayName("EnvironmentStrategy 测试")
class EnvironmentStrategyTest {

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("通过上下文属性解析环境")
        void testResolveFromContext() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .dev(true)
                    .prod(false)
                    .build();

            Feature feature = Feature.builder("test").strategy(strategy).build();
            FeatureContext devCtx = FeatureContext.builder()
                    .attribute("environment", "dev")
                    .build();
            FeatureContext prodCtx = FeatureContext.builder()
                    .attribute("environment", "prod")
                    .build();

            assertThat(strategy.isEnabled(feature, devCtx)).isTrue();
            assertThat(strategy.isEnabled(feature, prodCtx)).isFalse();
        }

        @Test
        @DisplayName("未知环境使用默认状态")
        void testDefaultState() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .dev(true)
                    .defaultState(false)
                    .build();

            Feature feature = Feature.builder("test").build();
            FeatureContext unknownCtx = FeatureContext.builder()
                    .attribute("environment", "unknown")
                    .build();

            assertThat(strategy.isEnabled(feature, unknownCtx)).isFalse();
        }

        @Test
        @DisplayName("默认状态为true时未知环境返回true")
        void testDefaultStateTrue() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .prod(false)
                    .defaultState(true)
                    .build();

            Feature feature = Feature.builder("test").build();
            FeatureContext unknownCtx = FeatureContext.builder()
                    .attribute("environment", "staging")
                    .build();

            assertThat(strategy.isEnabled(feature, unknownCtx)).isTrue();
        }
    }

    @Nested
    @DisplayName("Builder测试")
    class BuilderTests {

        @Test
        @DisplayName("便捷方法设置环境")
        void testConvenienceMethods() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .dev(true)
                    .staging(true)
                    .prod(false)
                    .test(true)
                    .build();

            assertThat(strategy.getEnvironmentStates())
                    .containsEntry("dev", true)
                    .containsEntry("staging", true)
                    .containsEntry("prod", false)
                    .containsEntry("test", true);
        }

        @Test
        @DisplayName("自定义环境名称")
        void testCustomEnvironment() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .environment("canary", true)
                    .build();

            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.builder()
                    .attribute("environment", "canary")
                    .build();

            assertThat(strategy.isEnabled(feature, ctx)).isTrue();
        }

        @Test
        @DisplayName("获取默认状态")
        void testGetDefaultState() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .defaultState(true)
                    .build();

            assertThat(strategy.getDefaultState()).isTrue();
        }
    }

    @Nested
    @DisplayName("环境状态不可变测试")
    class ImmutabilityTests {

        @Test
        @DisplayName("环境状态映射不可变")
        void testImmutableStates() {
            EnvironmentStrategy strategy = EnvironmentStrategy.builder()
                    .dev(true)
                    .build();

            assertThatThrownBy(() -> strategy.getEnvironmentStates().put("prod", true))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
