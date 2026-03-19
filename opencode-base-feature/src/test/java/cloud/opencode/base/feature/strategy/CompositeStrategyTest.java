package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("CompositeStrategy 测试")
class CompositeStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建AND组合策略")
        void testConstructorAnd() {
            CompositeStrategy strategy = new CompositeStrategy(
                    List.of(AlwaysOnStrategy.INSTANCE, AlwaysOnStrategy.INSTANCE),
                    true
            );

            assertThat(strategy.isRequireAll()).isTrue();
            assertThat(strategy.getStrategies()).hasSize(2);
        }

        @Test
        @DisplayName("创建OR组合策略")
        void testConstructorOr() {
            CompositeStrategy strategy = new CompositeStrategy(
                    List.of(AlwaysOnStrategy.INSTANCE, AlwaysOffStrategy.INSTANCE),
                    false
            );

            assertThat(strategy.isRequireAll()).isFalse();
            assertThat(strategy.getStrategies()).hasSize(2);
        }

        @Test
        @DisplayName("null策略列表转为空列表")
        void testNullStrategies() {
            CompositeStrategy strategy = new CompositeStrategy(null, true);

            assertThat(strategy.getStrategies()).isEmpty();
        }

        @Test
        @DisplayName("策略列表是不可变的")
        void testImmutableStrategies() {
            CompositeStrategy strategy = new CompositeStrategy(
                    List.of(AlwaysOnStrategy.INSTANCE),
                    true
            );

            assertThatThrownBy(() -> strategy.getStrategies().add(AlwaysOffStrategy.INSTANCE))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("allOf() 测试")
    class AllOfTests {

        @Test
        @DisplayName("使用可变参数创建AND组合")
        void testAllOfVarargs() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOnStrategy.INSTANCE
            );

            assertThat(strategy.isRequireAll()).isTrue();
            assertThat(strategy.getStrategies()).hasSize(2);
        }

        @Test
        @DisplayName("使用列表创建AND组合")
        void testAllOfList() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    List.of(AlwaysOnStrategy.INSTANCE, AlwaysOnStrategy.INSTANCE)
            );

            assertThat(strategy.isRequireAll()).isTrue();
            assertThat(strategy.getStrategies()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("anyOf() 测试")
    class AnyOfTests {

        @Test
        @DisplayName("使用可变参数创建OR组合")
        void testAnyOfVarargs() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );

            assertThat(strategy.isRequireAll()).isFalse();
            assertThat(strategy.getStrategies()).hasSize(2);
        }

        @Test
        @DisplayName("使用列表创建OR组合")
        void testAnyOfList() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    List.of(AlwaysOnStrategy.INSTANCE, AlwaysOffStrategy.INSTANCE)
            );

            assertThat(strategy.isRequireAll()).isFalse();
            assertThat(strategy.getStrategies()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("isEnabled() AND逻辑测试")
    class AndLogicTests {

        @Test
        @DisplayName("所有策略都通过时返回true")
        void testAllPass() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOnStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("任一策略失败时返回false")
        void testOneFails() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("所有策略都失败时返回false")
        void testAllFail() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOffStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("空策略列表返回false")
        void testEmptyStrategies() {
            CompositeStrategy strategy = CompositeStrategy.allOf();
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }
    }

    @Nested
    @DisplayName("isEnabled() OR逻辑测试")
    class OrLogicTests {

        @Test
        @DisplayName("任一策略通过时返回true")
        void testOnePass() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("所有策略都通过时返回true")
        void testAllPass() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOnStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isTrue();
        }

        @Test
        @DisplayName("所有策略都失败时返回false")
        void testAllFail() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    AlwaysOffStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("空策略列表返回false")
        void testEmptyStrategies() {
            CompositeStrategy strategy = CompositeStrategy.anyOf();
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }
    }

    @Nested
    @DisplayName("复杂组合测试")
    class ComplexCompositeTests {

        @Test
        @DisplayName("VIP用户AND在时间范围内")
        void testVipAndDateRange() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    new UserListStrategy(Set.of("vip1", "vip2")),
                    AlwaysOnStrategy.INSTANCE // 模拟时间范围
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("vip1"))).isTrue();
            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("regular"))).isFalse();
        }

        @Test
        @DisplayName("VIP用户OR百分比通过")
        void testVipOrPercentage() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    new UserListStrategy(Set.of("vip1")),
                    new PercentageStrategy(100) // 100%通过
            );
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("vip1"))).isTrue();
            assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("regular"))).isTrue();
        }
    }

    @Nested
    @DisplayName("getStrategies() 测试")
    class GetStrategiesTests {

        @Test
        @DisplayName("获取策略列表")
        void testGetStrategies() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );

            assertThat(strategy.getStrategies()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("isRequireAll() 测试")
    class IsRequireAllTests {

        @Test
        @DisplayName("AND组合返回true")
        void testAndReturnsTrue() {
            CompositeStrategy strategy = CompositeStrategy.allOf(AlwaysOnStrategy.INSTANCE);

            assertThat(strategy.isRequireAll()).isTrue();
        }

        @Test
        @DisplayName("OR组合返回false")
        void testOrReturnsFalse() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(AlwaysOnStrategy.INSTANCE);

            assertThat(strategy.isRequireAll()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("AND组合包含AND")
        void testAndToString() {
            CompositeStrategy strategy = CompositeStrategy.allOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );

            assertThat(strategy.toString())
                    .contains("CompositeStrategy")
                    .contains("AND")
                    .contains("2");
        }

        @Test
        @DisplayName("OR组合包含OR")
        void testOrToString() {
            CompositeStrategy strategy = CompositeStrategy.anyOf(
                    AlwaysOnStrategy.INSTANCE,
                    AlwaysOffStrategy.INSTANCE
            );

            assertThat(strategy.toString())
                    .contains("CompositeStrategy")
                    .contains("OR")
                    .contains("2");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            CompositeStrategy strategy = CompositeStrategy.allOf(AlwaysOnStrategy.INSTANCE);

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
