package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ConsistentPercentageStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("ConsistentPercentageStrategy 测试")
class ConsistentPercentageStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带盐的策略")
        void testConstructorWithSalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50, "my-salt");

            assertThat(strategy.getPercentage()).isEqualTo(50);
            assertThat(strategy.hasSalt()).isTrue();
        }

        @Test
        @DisplayName("创建无盐的策略")
        void testConstructorWithoutSalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50);

            assertThat(strategy.getPercentage()).isEqualTo(50);
            assertThat(strategy.hasSalt()).isFalse();
        }

        @Test
        @DisplayName("null盐值转为空字符串")
        void testNullSalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50, null);

            assertThat(strategy.hasSalt()).isFalse();
        }

        @Test
        @DisplayName("百分比截断到0-100范围")
        void testPercentageClamp() {
            ConsistentPercentageStrategy low = new ConsistentPercentageStrategy(-10);
            ConsistentPercentageStrategy high = new ConsistentPercentageStrategy(150);

            assertThat(low.getPercentage()).isEqualTo(0);
            assertThat(high.getPercentage()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("100%始终启用（有用户ID时）")
        void testHundredPercentAlwaysEnabled() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(100);
            Feature feature = Feature.builder("test").build();

            for (int i = 0; i < 100; i++) {
                assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user" + i))).isTrue();
            }
        }

        @Test
        @DisplayName("0%始终禁用")
        void testZeroPercentAlwaysDisabled() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(0);
            Feature feature = Feature.builder("test").build();

            for (int i = 0; i < 100; i++) {
                assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user" + i))).isFalse();
            }
        }

        @Test
        @DisplayName("无用户ID时始终返回false")
        void testNoUserIdReturnsFalse() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(100);
            Feature feature = Feature.builder("test").build();

            assertThat(strategy.isEnabled(feature, FeatureContext.empty())).isFalse();
        }

        @Test
        @DisplayName("一致性哈希")
        void testConsistentHash() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50);
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.ofUser("user123");

            boolean result1 = strategy.isEnabled(feature, ctx);
            boolean result2 = strategy.isEnabled(feature, ctx);
            boolean result3 = strategy.isEnabled(feature, ctx);

            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        }

        @Test
        @DisplayName("不同功能对同一用户可能有不同结果")
        void testFeatureSpecificDistribution() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50);
            FeatureContext ctx = FeatureContext.ofUser("user123");

            // 不同功能键会导致不同的哈希结果
            Feature feature1 = Feature.builder("feature-a").build();
            Feature feature2 = Feature.builder("feature-b").build();

            // 两个功能的结果可能相同也可能不同，但每个功能自身是一致的
            boolean result1a = strategy.isEnabled(feature1, ctx);
            boolean result1b = strategy.isEnabled(feature1, ctx);
            boolean result2a = strategy.isEnabled(feature2, ctx);
            boolean result2b = strategy.isEnabled(feature2, ctx);

            assertThat(result1a).isEqualTo(result1b);
            assertThat(result2a).isEqualTo(result2b);
        }

        @Test
        @DisplayName("盐值影响结果")
        void testSaltAffectsResult() {
            ConsistentPercentageStrategy strategy1 = new ConsistentPercentageStrategy(50, "salt1");
            ConsistentPercentageStrategy strategy2 = new ConsistentPercentageStrategy(50, "salt2");
            Feature feature = Feature.builder("test").build();

            // 统计不同结果的用户数
            int differentResults = 0;
            for (int i = 0; i < 100; i++) {
                FeatureContext ctx = FeatureContext.ofUser("user" + i);
                if (strategy1.isEnabled(feature, ctx) != strategy2.isEnabled(feature, ctx)) {
                    differentResults++;
                }
            }

            // 不同盐值应该导致一些用户有不同的结果
            assertThat(differentResults).isGreaterThan(0);
        }

        @Test
        @DisplayName("百分比分布")
        void testPercentageDistribution() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(30);
            Feature feature = Feature.builder("test").build();

            int enabledCount = 0;
            int total = 1000;

            for (int i = 0; i < total; i++) {
                if (strategy.isEnabled(feature, FeatureContext.ofUser("user" + i))) {
                    enabledCount++;
                }
            }

            // 应该大约是30%
            assertThat(enabledCount).isBetween(200, 400);
        }
    }

    @Nested
    @DisplayName("getPercentage() 测试")
    class GetPercentageTests {

        @Test
        @DisplayName("获取百分比值")
        void testGetPercentage() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(75);

            assertThat(strategy.getPercentage()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("hasSalt() 测试")
    class HasSaltTests {

        @Test
        @DisplayName("有盐值")
        void testHasSalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50, "salt");

            assertThat(strategy.hasSalt()).isTrue();
        }

        @Test
        @DisplayName("无盐值")
        void testNoSalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50);

            assertThat(strategy.hasSalt()).isFalse();
        }

        @Test
        @DisplayName("空盐值")
        void testEmptySalt() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50, "");

            assertThat(strategy.hasSalt()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含百分比和盐状态")
        void testToString() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50, "salt");

            assertThat(strategy.toString())
                    .contains("ConsistentPercentageStrategy")
                    .contains("50")
                    .contains("hasSalt=true");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            ConsistentPercentageStrategy strategy = new ConsistentPercentageStrategy(50);

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
