package cloud.opencode.base.rules.condition;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CompositeCondition Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("CompositeCondition Tests")
class CompositeConditionTest {

    private final Condition trueCondition = ctx -> true;
    private final Condition falseCondition = ctx -> false;
    private final RuleContext context = RuleContext.create();

    @Nested
    @DisplayName("AND Operator Tests")
    class AndOperatorTests {

        @Test
        @DisplayName("AND should return true when all conditions are true")
        void andShouldReturnTrueWhenAllConditionsAreTrue() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(trueCondition, trueCondition, trueCondition)
            );

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("AND should return false when any condition is false")
        void andShouldReturnFalseWhenAnyConditionIsFalse() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(trueCondition, falseCondition, trueCondition)
            );

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("AND should return true for empty list")
        void andShouldReturnTrueForEmptyList() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of()
            );

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("AND should short-circuit on first false")
        void andShouldShortCircuitOnFirstFalse() {
            int[] callCount = {0};
            Condition countingTrue = ctx -> { callCount[0]++; return true; };
            Condition countingFalse = ctx -> { callCount[0]++; return false; };

            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(countingTrue, countingFalse, countingTrue)
            );

            condition.evaluate(context);
            assertThat(callCount[0]).isEqualTo(2); // Should not evaluate third
        }
    }

    @Nested
    @DisplayName("OR Operator Tests")
    class OrOperatorTests {

        @Test
        @DisplayName("OR should return true when any condition is true")
        void orShouldReturnTrueWhenAnyConditionIsTrue() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.OR,
                    List.of(falseCondition, trueCondition, falseCondition)
            );

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("OR should return false when all conditions are false")
        void orShouldReturnFalseWhenAllConditionsAreFalse() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.OR,
                    List.of(falseCondition, falseCondition, falseCondition)
            );

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("OR should return false for empty list")
        void orShouldReturnFalseForEmptyList() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.OR,
                    List.of()
            );

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("OR should short-circuit on first true")
        void orShouldShortCircuitOnFirstTrue() {
            int[] callCount = {0};
            Condition countingTrue = ctx -> { callCount[0]++; return true; };
            Condition countingFalse = ctx -> { callCount[0]++; return false; };

            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.OR,
                    List.of(countingFalse, countingTrue, countingFalse)
            );

            condition.evaluate(context);
            assertThat(callCount[0]).isEqualTo(2); // Should not evaluate third
        }
    }

    @Nested
    @DisplayName("NOT Operator Tests")
    class NotOperatorTests {

        @Test
        @DisplayName("NOT should negate true condition")
        void notShouldNegateTrueCondition() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.NOT,
                    List.of(trueCondition)
            );

            assertThat(condition.evaluate(context)).isFalse();
        }

        @Test
        @DisplayName("NOT should negate false condition")
        void notShouldNegateFalseCondition() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.NOT,
                    List.of(falseCondition)
            );

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("NOT should return true for empty list")
        void notShouldReturnTrueForEmptyList() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.NOT,
                    List.of()
            );

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("NOT should only negate first condition")
        void notShouldOnlyNegateFirstCondition() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.NOT,
                    List.of(falseCondition, trueCondition)
            );

            // Should only negate the first condition
            assertThat(condition.evaluate(context)).isTrue();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("and() should create AND composite")
        void andShouldCreateAndComposite() {
            CompositeCondition condition = CompositeCondition.and(trueCondition, trueCondition);
            assertThat(condition.getOperator()).isEqualTo(CompositeCondition.Operator.AND);
            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("or() should create OR composite")
        void orShouldCreateOrComposite() {
            CompositeCondition condition = CompositeCondition.or(falseCondition, trueCondition);
            assertThat(condition.getOperator()).isEqualTo(CompositeCondition.Operator.OR);
            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("not() should create NOT composite")
        void notShouldCreateNotComposite() {
            CompositeCondition condition = CompositeCondition.not(trueCondition);
            assertThat(condition.getOperator()).isEqualTo(CompositeCondition.Operator.NOT);
            assertThat(condition.evaluate(context)).isFalse();
        }
    }

    @Nested
    @DisplayName("Accessor Tests")
    class AccessorTests {

        @Test
        @DisplayName("getOperator() should return operator")
        void getOperatorShouldReturnOperator() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(trueCondition)
            );

            assertThat(condition.getOperator()).isEqualTo(CompositeCondition.Operator.AND);
        }

        @Test
        @DisplayName("getConditions() should return immutable list")
        void getConditionsShouldReturnImmutableList() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(trueCondition)
            );

            assertThatThrownBy(() -> condition.getConditions().add(falseCondition))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("getConditions() should return copy of conditions")
        void getConditionsShouldReturnCopyOfConditions() {
            CompositeCondition condition = new CompositeCondition(
                    CompositeCondition.Operator.AND,
                    List.of(trueCondition, falseCondition)
            );

            assertThat(condition.getConditions()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Operator Enum Tests")
    class OperatorEnumTests {

        @Test
        @DisplayName("Operator enum should have all values")
        void operatorEnumShouldHaveAllValues() {
            assertThat(CompositeCondition.Operator.values())
                    .containsExactly(
                            CompositeCondition.Operator.AND,
                            CompositeCondition.Operator.OR,
                            CompositeCondition.Operator.NOT
                    );
        }

        @Test
        @DisplayName("Operator.valueOf() should work")
        void operatorValueOfShouldWork() {
            assertThat(CompositeCondition.Operator.valueOf("AND"))
                    .isEqualTo(CompositeCondition.Operator.AND);
        }
    }

    @Nested
    @DisplayName("Nested Composite Tests")
    class NestedCompositeTests {

        @Test
        @DisplayName("Should support nested composites")
        void shouldSupportNestedComposites() {
            // (true AND true) OR false = true
            Condition andCondition = CompositeCondition.and(trueCondition, trueCondition);
            CompositeCondition condition = CompositeCondition.or(andCondition, falseCondition);

            assertThat(condition.evaluate(context)).isTrue();
        }

        @Test
        @DisplayName("Should support deeply nested composites")
        void shouldSupportDeeplyNestedComposites() {
            // NOT(false OR (true AND false)) = NOT(false) = true
            Condition innerAnd = CompositeCondition.and(trueCondition, falseCondition);
            Condition innerOr = CompositeCondition.or(falseCondition, innerAnd);
            CompositeCondition condition = CompositeCondition.not(innerOr);

            assertThat(condition.evaluate(context)).isTrue();
        }
    }
}
