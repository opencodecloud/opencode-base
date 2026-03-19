package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.OpenRules;
import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Condition Interface Tests
 * Condition 接口测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Condition Interface Tests | Condition 接口测试")
class ConditionTest {

    @Nested
    @DisplayName("Evaluate Tests | 评估测试")
    class EvaluateTests {

        @Test
        @DisplayName("evaluate returns true | evaluate 返回 true")
        void testEvaluateTrue() {
            Condition condition = ctx -> true;
            RuleContext context = OpenRules.context();

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("evaluate returns false | evaluate 返回 false")
        void testEvaluateFalse() {
            Condition condition = ctx -> false;
            RuleContext context = OpenRules.context();

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("evaluate uses context | evaluate 使用上下文")
        void testEvaluateUsesContext() {
            Condition condition = ctx -> {
                Integer value = ctx.get("value", 0);
                return value > 10;
            };

            RuleContext context = OpenRules.context();
            context.put("value", 15);
            assertThat(condition.evaluate(context)).isTrue();

            context.put("value", 5);
            assertThat(condition.evaluate(context)).isFalse();
        }
    }

    @Nested
    @DisplayName("And Tests | and 测试")
    class AndTests {

        @Test
        @DisplayName("and returns true when both true | and 当两者都为 true 时返回 true")
        void testAndBothTrue() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> true;
            Condition combined = c1.and(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("and returns false when first false | and 当第一个为 false 时返回 false")
        void testAndFirstFalse() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> true;
            Condition combined = c1.and(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("and returns false when second false | and 当第二个为 false 时返回 false")
        void testAndSecondFalse() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> false;
            Condition combined = c1.and(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("and short-circuits | and 短路求值")
        void testAndShortCircuit() {
            final boolean[] secondEvaluated = {false};

            Condition c1 = ctx -> false;
            Condition c2 = ctx -> {
                secondEvaluated[0] = true;
                return true;
            };

            Condition combined = c1.and(c2);
            RuleContext context = OpenRules.context();
            combined.evaluate(context);

            assertThat(secondEvaluated[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("Or Tests | or 测试")
    class OrTests {

        @Test
        @DisplayName("or returns true when first true | or 当第一个为 true 时返回 true")
        void testOrFirstTrue() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> false;
            Condition combined = c1.or(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("or returns true when second true | or 当第二个为 true 时返回 true")
        void testOrSecondTrue() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> true;
            Condition combined = c1.or(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("or returns false when both false | or 当两者都为 false 时返回 false")
        void testOrBothFalse() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> false;
            Condition combined = c1.or(c2);

            RuleContext context = OpenRules.context();
            assertThat(combined.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("or short-circuits | or 短路求值")
        void testOrShortCircuit() {
            final boolean[] secondEvaluated = {false};

            Condition c1 = ctx -> true;
            Condition c2 = ctx -> {
                secondEvaluated[0] = true;
                return false;
            };

            Condition combined = c1.or(c2);
            RuleContext context = OpenRules.context();
            combined.evaluate(context);

            assertThat(secondEvaluated[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("Negate Tests | negate 测试")
    class NegateTests {

        @Test
        @DisplayName("negate inverts true to false | negate 将 true 转为 false")
        void testNegateTrue() {
            Condition condition = ctx -> true;
            Condition negated = condition.negate();

            RuleContext context = OpenRules.context();
            assertThat(negated.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("negate inverts false to true | negate 将 false 转为 true")
        void testNegateFalse() {
            Condition condition = ctx -> false;
            Condition negated = condition.negate();

            RuleContext context = OpenRules.context();
            assertThat(negated.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("double negate returns original | 双重否定返回原值")
        void testDoubleNegate() {
            Condition condition = ctx -> true;
            Condition doubleNegated = condition.negate().negate();

            RuleContext context = OpenRules.context();
            assertThat(doubleNegated.evaluate(context)).isTrue();
        }
    }

    @Nested
    @DisplayName("Static Factory Tests | 静态工厂测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("alwaysTrue returns true | alwaysTrue 返回 true")
        void testAlwaysTrue() {
            Condition condition = Condition.alwaysTrue();
            RuleContext context = OpenRules.context();

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("alwaysFalse returns false | alwaysFalse 返回 false")
        void testAlwaysFalse() {
            Condition condition = Condition.alwaysFalse();
            RuleContext context = OpenRules.context();

            assertThat(condition.evaluate(context)).isFalse();
        }
    }

    @Nested
    @DisplayName("Composition Tests | 组合测试")
    class CompositionTests {

        @Test
        @DisplayName("complex composition | 复杂组合")
        void testComplexComposition() {
            // (a > 10 AND b < 5) OR c == true
            Condition a = ctx -> ctx.<Integer>get("a", 0) > 10;
            Condition b = ctx -> ctx.<Integer>get("b", 0) < 5;
            Condition c = ctx -> ctx.<Boolean>get("c", false);

            Condition complex = a.and(b).or(c);

            RuleContext context = OpenRules.context();

            // Test case 1: a=15, b=3, c=false -> (true AND true) OR false = true
            context.put("a", 15);
            context.put("b", 3);
            context.put("c", false);
            assertThat(complex.evaluate(context)).isTrue();

            // Test case 2: a=5, b=10, c=true -> (false AND false) OR true = true
            context.put("a", 5);
            context.put("b", 10);
            context.put("c", true);
            assertThat(complex.evaluate(context)).isTrue();

            // Test case 3: a=5, b=10, c=false -> (false AND false) OR false = false
            context.put("a", 5);
            context.put("b", 10);
            context.put("c", false);
            assertThat(complex.evaluate(context)).isFalse();
        }
    }

    @Nested
    @DisplayName("Functional Interface Tests | 函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("Condition is functional interface | Condition 是函数式接口")
        void testFunctionalInterface() {
            assertThat(Condition.class.isAnnotationPresent(FunctionalInterface.class)).isTrue();
        }
    }
}
