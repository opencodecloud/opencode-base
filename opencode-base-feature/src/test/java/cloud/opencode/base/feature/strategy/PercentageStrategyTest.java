package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PercentageStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("PercentageStrategy 测试")
class PercentageStrategyTest {

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建百分比策略")
        void testConstructor() {
            PercentageStrategy strategy = new PercentageStrategy(50);

            assertThat(strategy.getPercentage()).isEqualTo(50);
        }

        @Test
        @DisplayName("百分比截断到0-100范围")
        void testPercentageClamp() {
            PercentageStrategy low = new PercentageStrategy(-10);
            PercentageStrategy high = new PercentageStrategy(150);

            assertThat(low.getPercentage()).isEqualTo(0);
            assertThat(high.getPercentage()).isEqualTo(100);
        }

        @Test
        @DisplayName("0%百分比")
        void testZeroPercentage() {
            PercentageStrategy strategy = new PercentageStrategy(0);

            assertThat(strategy.getPercentage()).isEqualTo(0);
        }

        @Test
        @DisplayName("100%百分比")
        void testHundredPercentage() {
            PercentageStrategy strategy = new PercentageStrategy(100);

            assertThat(strategy.getPercentage()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("isEnabled() 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("100%始终启用")
        void testHundredPercentAlwaysEnabled() {
            PercentageStrategy strategy = new PercentageStrategy(100);
            Feature feature = Feature.builder("test").build();

            for (int i = 0; i < 100; i++) {
                assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user" + i))).isTrue();
            }
        }

        @Test
        @DisplayName("0%始终禁用")
        void testZeroPercentAlwaysDisabled() {
            PercentageStrategy strategy = new PercentageStrategy(0);
            Feature feature = Feature.builder("test").build();

            for (int i = 0; i < 100; i++) {
                assertThat(strategy.isEnabled(feature, FeatureContext.ofUser("user" + i))).isFalse();
            }
        }

        @Test
        @DisplayName("有用户ID时使用一致性哈希")
        void testConsistentHashWithUserId() {
            PercentageStrategy strategy = new PercentageStrategy(50);
            Feature feature = Feature.builder("test").build();
            FeatureContext ctx = FeatureContext.ofUser("user123");

            boolean result1 = strategy.isEnabled(feature, ctx);
            boolean result2 = strategy.isEnabled(feature, ctx);
            boolean result3 = strategy.isEnabled(feature, ctx);

            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        }

        @Test
        @DisplayName("无用户ID时使用随机")
        void testRandomWithoutUserId() {
            PercentageStrategy strategy = new PercentageStrategy(50);
            Feature feature = Feature.builder("test").build();

            int enabledCount = 0;
            int total = 1000;

            for (int i = 0; i < total; i++) {
                if (strategy.isEnabled(feature, FeatureContext.empty())) {
                    enabledCount++;
                }
            }

            // 应该大约是50%，允许较大的误差
            assertThat(enabledCount).isBetween(350, 650);
        }

        @Test
        @DisplayName("百分比分布")
        void testPercentageDistribution() {
            PercentageStrategy strategy = new PercentageStrategy(30);
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
            PercentageStrategy strategy = new PercentageStrategy(75);

            assertThat(strategy.getPercentage()).isEqualTo(75);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("包含百分比值")
        void testToString() {
            PercentageStrategy strategy = new PercentageStrategy(50);

            assertThat(strategy.toString())
                    .contains("PercentageStrategy")
                    .contains("50");
        }
    }

    @Nested
    @DisplayName("实现EnableStrategy测试")
    class ImplementsEnableStrategyTests {

        @Test
        @DisplayName("实现EnableStrategy接口")
        void testImplementsEnableStrategy() {
            PercentageStrategy strategy = new PercentageStrategy(50);

            assertThat(strategy).isInstanceOf(EnableStrategy.class);
        }
    }
}
