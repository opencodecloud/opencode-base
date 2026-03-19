package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AlwaysOffStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("AlwaysOffStrategy 测试")
class AlwaysOffStrategyTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(AlwaysOffStrategy.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是单例")
        void testInstanceIsSingleton() {
            AlwaysOffStrategy instance1 = AlwaysOffStrategy.INSTANCE;
            AlwaysOffStrategy instance2 = AlwaysOffStrategy.INSTANCE;

            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("始终返回false")
        void testAlwaysReturnsFalse() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("任何用户都返回false")
        void testAnyUserReturnsFalse() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("user1"))).isFalse();
            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("user2"))).isFalse();
            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("admin"))).isFalse();
        }

        @Test
        @DisplayName("任何租户都返回false")
        void testAnyTenantReturnsFalse() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, 
                    FeatureContext.builder().tenantId("tenant1").build())).isFalse();
            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature, 
                    FeatureContext.builder().tenantId("tenant2").build())).isFalse();
        }

        @Test
        @DisplayName("任何功能都返回false")
        void testAnyFeatureReturnsFalse() {
            Feature feature1 = Feature.builder("feature1").build();
            Feature feature2 = Feature.builder("feature2").build();

            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature1, FeatureContext.empty())).isFalse();
            assertThat(AlwaysOffStrategy.INSTANCE.isEnabled(feature2, FeatureContext.empty())).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("返回策略名称")
        void testToString() {
            assertThat(AlwaysOffStrategy.INSTANCE.toString()).isEqualTo("AlwaysOffStrategy");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            assertThat(AlwaysOffStrategy.INSTANCE).isInstanceOf(EnableStrategy.class);
        }
    }
}
