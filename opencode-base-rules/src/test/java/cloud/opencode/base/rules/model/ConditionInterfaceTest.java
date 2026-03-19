package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Condition Interface Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("Condition Interface Tests")
class ConditionInterfaceTest {

    @Nested
    @DisplayName("and() Tests")
    class AndTests {

        @Test
        @DisplayName("and() should return true when both are true")
        void andShouldReturnTrueWhenBothTrue() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> true;

            Condition combined = c1.and(c2);

            assertThat(combined.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("and() should return false when first is false")
        void andShouldReturnFalseWhenFirstFalse() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> true;

            Condition combined = c1.and(c2);

            assertThat(combined.evaluate(RuleContext.create())).isFalse();
        }

        @Test
        @DisplayName("and() should return false when second is false")
        void andShouldReturnFalseWhenSecondFalse() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> false;

            Condition combined = c1.and(c2);

            assertThat(combined.evaluate(RuleContext.create())).isFalse();
        }

        @Test
        @DisplayName("and() should return false when both are false")
        void andShouldReturnFalseWhenBothFalse() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> false;

            Condition combined = c1.and(c2);

            assertThat(combined.evaluate(RuleContext.create())).isFalse();
        }

        @Test
        @DisplayName("and() should short-circuit when first is false")
        void andShouldShortCircuit() {
            boolean[] evaluated = {false};
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> {
                evaluated[0] = true;
                return true;
            };

            Condition combined = c1.and(c2);
            combined.evaluate(RuleContext.create());

            assertThat(evaluated[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("or() Tests")
    class OrTests {

        @Test
        @DisplayName("or() should return true when both are true")
        void orShouldReturnTrueWhenBothTrue() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> true;

            Condition combined = c1.or(c2);

            assertThat(combined.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("or() should return true when first is true")
        void orShouldReturnTrueWhenFirstTrue() {
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> false;

            Condition combined = c1.or(c2);

            assertThat(combined.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("or() should return true when second is true")
        void orShouldReturnTrueWhenSecondTrue() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> true;

            Condition combined = c1.or(c2);

            assertThat(combined.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("or() should return false when both are false")
        void orShouldReturnFalseWhenBothFalse() {
            Condition c1 = ctx -> false;
            Condition c2 = ctx -> false;

            Condition combined = c1.or(c2);

            assertThat(combined.evaluate(RuleContext.create())).isFalse();
        }

        @Test
        @DisplayName("or() should short-circuit when first is true")
        void orShouldShortCircuit() {
            boolean[] evaluated = {false};
            Condition c1 = ctx -> true;
            Condition c2 = ctx -> {
                evaluated[0] = true;
                return false;
            };

            Condition combined = c1.or(c2);
            combined.evaluate(RuleContext.create());

            assertThat(evaluated[0]).isFalse();
        }
    }

    @Nested
    @DisplayName("negate() Tests")
    class NegateTests {

        @Test
        @DisplayName("negate() should return false when original is true")
        void negateShouldReturnFalseWhenTrue() {
            Condition c = ctx -> true;

            Condition negated = c.negate();

            assertThat(negated.evaluate(RuleContext.create())).isFalse();
        }

        @Test
        @DisplayName("negate() should return true when original is false")
        void negateShouldReturnTrueWhenFalse() {
            Condition c = ctx -> false;

            Condition negated = c.negate();

            assertThat(negated.evaluate(RuleContext.create())).isTrue();
        }

        @Test
        @DisplayName("Double negate should return original value")
        void doubleNegateShouldReturnOriginal() {
            Condition c = ctx -> true;

            Condition doubleNegated = c.negate().negate();

            assertThat(doubleNegated.evaluate(RuleContext.create())).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysTrue() Tests")
    class AlwaysTrueTests {

        @Test
        @DisplayName("alwaysTrue() should always return true")
        void alwaysTrueShouldReturnTrue() {
            Condition c = Condition.alwaysTrue();

            assertThat(c.evaluate(RuleContext.create())).isTrue();
            assertThat(c.evaluate(RuleContext.of("key", "value"))).isTrue();
        }
    }

    @Nested
    @DisplayName("alwaysFalse() Tests")
    class AlwaysFalseTests {

        @Test
        @DisplayName("alwaysFalse() should always return false")
        void alwaysFalseShouldReturnFalse() {
            Condition c = Condition.alwaysFalse();

            assertThat(c.evaluate(RuleContext.create())).isFalse();
            assertThat(c.evaluate(RuleContext.of("key", "value"))).isFalse();
        }
    }

    @Nested
    @DisplayName("Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should support complex chaining")
        void shouldSupportComplexChaining() {
            Condition c1 = ctx -> ctx.<Integer>get("a") > 0;
            Condition c2 = ctx -> ctx.<Integer>get("b") > 0;
            Condition c3 = ctx -> ctx.<Integer>get("c") > 0;

            Condition complex = c1.and(c2).or(c3);

            RuleContext ctx1 = RuleContext.of("a", 1, "b", 1, "c", 0);
            RuleContext ctx2 = RuleContext.of("a", 0, "b", 0, "c", 1);
            RuleContext ctx3 = RuleContext.of("a", 0, "b", 0, "c", 0);

            assertThat(complex.evaluate(ctx1)).isTrue();
            assertThat(complex.evaluate(ctx2)).isTrue();
            assertThat(complex.evaluate(ctx3)).isFalse();
        }
    }

    @Nested
    @DisplayName("Lambda Implementation Tests")
    class LambdaImplementationTests {

        @Test
        @DisplayName("Lambda should be usable as Condition")
        void lambdaShouldBeUsableAsCondition() {
            Condition c = ctx -> ctx.contains("required");

            assertThat(c.evaluate(RuleContext.of("required", true))).isTrue();
            assertThat(c.evaluate(RuleContext.create())).isFalse();
        }
    }
}
