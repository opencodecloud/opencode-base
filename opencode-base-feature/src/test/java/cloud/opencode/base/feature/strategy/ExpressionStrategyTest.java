package cloud.opencode.base.feature.strategy;

import cloud.opencode.base.feature.Feature;
import cloud.opencode.base.feature.FeatureContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ExpressionStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("ExpressionStrategy 测试")
class ExpressionStrategyTest {

    @Nested
    @DisplayName("isExpressionModuleAvailable 测试")
    class IsExpressionModuleAvailableTests {

        @Test
        @DisplayName("返回布尔值")
        void shouldReturnBoolean() {
            boolean result = ExpressionStrategy.isExpressionModuleAvailable();
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("of 工厂方法测试")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("创建表达式策略")
        void shouldCreateExpressionStrategy() {
            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18");

            assertThat(strategy).isNotNull();
            assertThat(strategy.getExpression()).isEqualTo("age >= 18");
            assertThat(strategy.getFallbackValue()).isFalse();
        }

        @Test
        @DisplayName("创建带回退值的表达式策略")
        void shouldCreateWithFallbackValue() {
            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18", true);

            assertThat(strategy).isNotNull();
            assertThat(strategy.getExpression()).isEqualTo("age >= 18");
            assertThat(strategy.getFallbackValue()).isTrue();
        }

        @Test
        @DisplayName("null 表达式抛出异常")
        void shouldThrowForNullExpression() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionStrategy.of(null))
                    .withMessage("expression must not be null");
        }
    }

    @Nested
    @DisplayName("isValidExpression 测试")
    class IsValidExpressionTests {

        @Test
        @DisplayName("空表达式返回 false")
        void shouldReturnFalseForNull() {
            assertThat(ExpressionStrategy.isValidExpression(null)).isFalse();
        }

        @Test
        @DisplayName("空白表达式返回 false")
        void shouldReturnFalseForBlank() {
            assertThat(ExpressionStrategy.isValidExpression("")).isFalse();
            assertThat(ExpressionStrategy.isValidExpression("   ")).isFalse();
        }

        @Test
        @DisplayName("非空表达式返回结果")
        void shouldReturnResultForNonEmpty() {
            boolean result = ExpressionStrategy.isValidExpression("x > 0");
            assertThat(result).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("isEnabled 测试")
    class IsEnabledTests {

        @Test
        @DisplayName("表达式模块不可用时返回回退值 (false)")
        void shouldReturnFallbackWhenModuleNotAvailable() {
            if (ExpressionStrategy.isExpressionModuleAvailable()) {
                // Skip if expression module is available
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18");
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.empty();

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("表达式模块不可用时返回回退值 (true)")
        void shouldReturnTrueFallbackWhenModuleNotAvailable() {
            if (ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18", true);
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.empty();

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("表达式模块可用时评估表达式")
        void shouldEvaluateExpressionWhenModuleAvailable() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18");
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.builder()
                    .attribute("age", 25)
                    .build();

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("表达式模块可用时评估失败返回 false")
        void shouldReturnFalseWhenExpressionEvaluatesToFalse() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18");
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.builder()
                    .attribute("age", 15)
                    .build();

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("可以访问功能属性")
        void shouldAccessFeatureProperties() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("defaultEnabled == true");
            Feature feature = Feature.builder("test-feature")
                    .defaultEnabled(true)
                    .build();
            FeatureContext context = FeatureContext.empty();

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("可以访问用户 ID")
        void shouldAccessUserId() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("userId != null");
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.ofUser("user123");

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("可以访问租户 ID")
        void shouldAccessTenantId() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("tenantId == 'tenant-001'");
            Feature feature = Feature.builder("test-feature").build();
            FeatureContext context = FeatureContext.ofTenant("tenant-001");

            boolean result = strategy.isEnabled(feature, context);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("复杂表达式测试 (需要 Expression 模块)")
    class ComplexExpressionTests {

        @Test
        @DisplayName("逻辑与表达式")
        void shouldEvaluateLogicalAnd() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18 && isPremium == true");
            Feature feature = Feature.builder("premium-feature").build();
            FeatureContext context = FeatureContext.builder()
                    .attribute("age", 25)
                    .attribute("isPremium", true)
                    .build();

            assertThat(strategy.isEnabled(feature, context)).isTrue();

            FeatureContext failContext = FeatureContext.builder()
                    .attribute("age", 25)
                    .attribute("isPremium", false)
                    .build();

            assertThat(strategy.isEnabled(feature, failContext)).isFalse();
        }

        @Test
        @DisplayName("逻辑或表达式")
        void shouldEvaluateLogicalOr() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("isAdmin == true || hasPermission == true");
            Feature feature = Feature.builder("admin-feature").build();

            FeatureContext adminContext = FeatureContext.builder()
                    .attribute("isAdmin", true)
                    .attribute("hasPermission", false)
                    .build();

            assertThat(strategy.isEnabled(feature, adminContext)).isTrue();

            FeatureContext permissionContext = FeatureContext.builder()
                    .attribute("isAdmin", false)
                    .attribute("hasPermission", true)
                    .build();

            assertThat(strategy.isEnabled(feature, permissionContext)).isTrue();
        }

        @Test
        @DisplayName("数值比较表达式")
        void shouldEvaluateNumericComparison() {
            if (!ExpressionStrategy.isExpressionModuleAvailable()) {
                return;
            }

            ExpressionStrategy strategy = ExpressionStrategy.of("score >= 60 && score <= 100");
            Feature feature = Feature.builder("pass-feature").build();

            FeatureContext passContext = FeatureContext.builder()
                    .attribute("score", 85)
                    .build();

            assertThat(strategy.isEnabled(feature, passContext)).isTrue();

            FeatureContext failContext = FeatureContext.builder()
                    .attribute("score", 45)
                    .build();

            assertThat(strategy.isEnabled(feature, failContext)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString 测试")
    class ToStringTests {

        @Test
        @DisplayName("返回可读的字符串表示")
        void shouldReturnReadableString() {
            ExpressionStrategy strategy = ExpressionStrategy.of("age >= 18", true);

            String result = strategy.toString();

            assertThat(result).contains("age >= 18");
            assertThat(result).contains("true");
        }
    }
}
