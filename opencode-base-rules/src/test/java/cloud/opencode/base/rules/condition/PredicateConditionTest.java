package cloud.opencode.base.rules.condition;

import cloud.opencode.base.rules.RuleContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PredicateCondition Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("PredicateCondition Tests")
class PredicateConditionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should create condition from predicate")
        void constructorShouldCreateConditionFromPredicate() {
            PredicateCondition condition = new PredicateCondition(ctx -> true);
            assertThat(condition).isNotNull();
        }
    }

    @Nested
    @DisplayName("evaluate() Tests")
    class EvaluateTests {

        @Test
        @DisplayName("evaluate() should return true when predicate is true")
        void evaluateShouldReturnTrueWhenPredicateIsTrue() {
            PredicateCondition condition = new PredicateCondition(ctx -> true);
            RuleContext context = RuleContext.create();

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("evaluate() should return false when predicate is false")
        void evaluateShouldReturnFalseWhenPredicateIsFalse() {
            PredicateCondition condition = new PredicateCondition(ctx -> false);
            RuleContext context = RuleContext.create();

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("evaluate() should pass context to predicate")
        void evaluateShouldPassContextToPredicate() {
            PredicateCondition condition = new PredicateCondition(
                    ctx -> "VIP".equals(ctx.<String>get("type"))
            );

            RuleContext vipContext = RuleContext.of("type", "VIP");
            RuleContext regularContext = RuleContext.of("type", "REGULAR");

            assertThat(condition.evaluate(vipContext)).isTrue();
            assertThat(condition.evaluate(regularContext)).isFalse();
        }

        @Test
        @DisplayName("evaluate() should work with complex predicates")
        void evaluateShouldWorkWithComplexPredicates() {
            PredicateCondition condition = new PredicateCondition(ctx -> {
                Integer age = ctx.get("age");
                Double income = ctx.get("income");
                return age != null && age >= 18 && income != null && income >= 50000;
            });

            RuleContext eligible = RuleContext.of("age", 25, "income", 75000.0);
            RuleContext tooYoung = RuleContext.of("age", 16, "income", 75000.0);
            RuleContext lowIncome = RuleContext.of("age", 25, "income", 30000.0);

            assertThat(condition.evaluate(eligible)).isTrue();
            assertThat(condition.evaluate(tooYoung)).isFalse();
            assertThat(condition.evaluate(lowIncome)).isFalse();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() should create condition from predicate")
        void ofShouldCreateConditionFromPredicate() {
            PredicateCondition condition = PredicateCondition.of(ctx -> true);
            RuleContext context = RuleContext.create();

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("of() should create same result as constructor")
        void ofShouldCreateSameResultAsConstructor() {
            PredicateCondition fromConstructor = new PredicateCondition(ctx -> ctx.<Integer>get("value") > 10);
            PredicateCondition fromFactory = PredicateCondition.of(ctx -> ctx.<Integer>get("value") > 10);

            RuleContext context = RuleContext.of("value", 15);
            assertThat(fromConstructor.evaluate(context)).isEqualTo(fromFactory.evaluate(context));
        }
    }
}
