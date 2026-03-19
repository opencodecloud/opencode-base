package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TenantAwareStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("TenantAwareStrategy 测试")
class TenantAwareStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带回退策略的租户策略")
        void testConstructorWithFallback() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true, "tenant2", false),
                    AlwaysOnStrategy.INSTANCE
            );

            assertThat(strategy.getTenantOverrides()).containsEntry("tenant1", true);
            assertThat(strategy.getTenantOverrides()).containsEntry("tenant2", false);
            assertThat(strategy.getFallbackStrategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
        }

        @Test
        @DisplayName("创建使用默认回退的租户策略")
        void testConstructorWithDefaultFallback() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true)
            );

            assertThat(strategy.getFallbackStrategy()).isEqualTo(AlwaysOffStrategy.INSTANCE);
        }

        @Test
        @DisplayName("null覆盖映射转为空映射")
        void testNullOverrides() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(null);

            assertThat(strategy.getTenantOverrides()).isEmpty();
        }

        @Test
        @DisplayName("null回退策略使用默认值")
        void testNullFallback() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true),
                    null
            );

            assertThat(strategy.getFallbackStrategy()).isEqualTo(AlwaysOffStrategy.INSTANCE);
        }

        @Test
        @DisplayName("覆盖映射是不可变的")
        void testImmutableOverrides() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true)
            );

            assertThatThrownBy(() -> strategy.getTenantOverrides().put("tenant2", false))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("租户启用时返回true")
        void testTenantEnabled() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("enterprise", true, "trial", false)
            );
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.builder().tenantId("enterprise").build();

            assertThat(strategy.isEnabled(feature, ctx)).isTrue();
        }

        @Test
        @DisplayName("租户禁用时返回false")
        void testTenantDisabled() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("enterprise", true, "trial", false)
            );
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.builder().tenantId("trial").build();

            assertThat(strategy.isEnabled(feature, ctx)).isFalse();
        }

        @Test
        @DisplayName("租户不在映射中时使用回退策略")
        void testFallbackForUnknownTenant() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("enterprise", true),
                    AlwaysOnStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.builder().tenantId("unknown").build();

            assertThat(strategy.isEnabled(feature, ctx)).isTrue();
        }

        @Test
        @DisplayName("无租户ID时使用回退策略")
        void testFallbackForNoTenant() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("enterprise", true),
                    AlwaysOnStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("默认回退策略是AlwaysOff")
        void testDefaultFallbackIsAlwaysOff() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("enterprise", true)
            );
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.builder().tenantId("unknown").build();

            assertThat(strategy.isEnabled(feature, ctx)).isFalse();
        }
    }

    @Nested
    @DisplayName("getTenantOverrides() 测试")
    class GetTenantOverridesTests {

        @Test
        @DisplayName("获取租户覆盖映射")
        void testGetTenantOverrides() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true, "tenant2", false, "tenant3", true)
            );

            assertThat(strategy.getTenantOverrides())
                    .hasSize(3)
                    .containsEntry("tenant1", true)
                    .containsEntry("tenant2", false)
                    .containsEntry("tenant3", true);
        }
    }

    @Nested
    @DisplayName("getFallbackStrategy() 测试")
    class GetFallbackStrategyTests {

        @Test
        @DisplayName("获取回退策略")
        void testGetFallbackStrategy() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true),
                    AlwaysOnStrategy.INSTANCE
            );

            assertThat(strategy.getFallbackStrategy()).isEqualTo(AlwaysOnStrategy.INSTANCE);
        }
    }

    @Nested
    @DisplayName("hasTenantOverride() 测试")
    class HasTenantOverrideTests {

        @Test
        @DisplayName("租户有覆盖")
        void testHasTenantOverride() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true)
            );

            assertThat(strategy.hasTenantOverride("tenant1")).isTrue();
        }

        @Test
        @DisplayName("租户无覆盖")
        void testNoTenantOverride() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true)
            );

            assertThat(strategy.hasTenantOverride("tenant2")).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含租户数量")
        void testToString() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(
                    Map.of("tenant1", true, "tenant2", false)
            );

            assertThat(strategy.toString())
                    .contains("TenantAwareStrategy")
                    .contains("2");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            TenantAwareStrategy strategy = new TenantAwareStrategy(Map.of());

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
