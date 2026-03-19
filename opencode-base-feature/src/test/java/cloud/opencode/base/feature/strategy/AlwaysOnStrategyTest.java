package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * AlwaysOnStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("AlwaysOnStrategy 测试")
class AlwaysOnStrategyTest {

    @Nested
    @DisplayName("单例测试")
    class SingletonTests {

        @Test
        @DisplayName("INSTANCE不为null")
        void testInstanceNotNull() {
            assertThat(AlwaysOnStrategy.INSTANCE).isNotNull();
        }

        @Test
        @DisplayName("INSTANCE是单例")
        void testInstanceIsSingleton() {
            AlwaysOnStrategy instance1 = AlwaysOnStrategy.INSTANCE;
            AlwaysOnStrategy instance2 = AlwaysOnStrategy.INSTANCE;

            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("始终返回true")
        void testAlwaysReturnsTrue() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("任何用户都返回true")
        void testAnyUserReturnsTrue() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("user1"))).isTrue();
            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("user2"))).isTrue();
            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, FeatureContext.ofUser("admin"))).isTrue();
        }

        @Test
        @DisplayName("任何租户都返回true")
        void testAnyTenantReturnsTrue() {
            Feature feature = Feature.builder("test").build();

            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, 
                    FeatureContext.builder().tenantId("tenant1").build())).isTrue();
            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature, 
                    FeatureContext.builder().tenantId("tenant2").build())).isTrue();
        }

        @Test
        @DisplayName("任何功能都返回true")
        void testAnyFeatureReturnsTrue() {
            Feature feature1 = Feature.builder("feature1").build();
            Feature feature2 = Feature.builder("feature2").build();

            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature1, FeatureContext.empty())).isTrue();
            assertThat(AlwaysOnStrategy.INSTANCE.isEnabled(feature2, FeatureContext.empty())).isTrue();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("返回策略名称")
        void testToString() {
            assertThat(AlwaysOnStrategy.INSTANCE.toString()).isEqualTo("AlwaysOnStrategy");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            assertThat(AlwaysOnStrategy.INSTANCE).isInstanceOf(EnableStrategy.class);
        }
    }
}
