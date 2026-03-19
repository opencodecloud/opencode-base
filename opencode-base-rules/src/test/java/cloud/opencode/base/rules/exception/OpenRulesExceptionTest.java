package cloud.opencode.base.rules.exception;

import cloud.opencode.base.rules.exception.OpenRulesException.RuleErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenRulesException Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("OpenRulesException Tests")
class OpenRulesExceptionTest {

    @Nested
    @DisplayName("Constructor(String) Tests")
    class MessageConstructorTests {

        @Test
        @DisplayName("Should set message")
        void shouldSetMessage() {
            OpenRulesException exception = new OpenRulesException("Test error");

            assertThat(exception.getMessage()).contains("Test error");
            assertThat(exception.ruleName()).isNull();
            assertThat(exception.errorType()).isEqualTo(RuleErrorType.GENERAL);
        }
    }

    @Nested
    @DisplayName("Constructor(String, Throwable) Tests")
    class MessageCauseConstructorTests {

        @Test
        @DisplayName("Should set message and cause")
        void shouldSetMessageAndCause() {
            RuntimeException cause = new RuntimeException("Cause");
            OpenRulesException exception = new OpenRulesException("Test error", cause);

            assertThat(exception.getMessage()).contains("Test error");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.ruleName()).isNull();
            assertThat(exception.errorType()).isEqualTo(RuleErrorType.GENERAL);
        }
    }

    @Nested
    @DisplayName("Constructor(RuleErrorType, String) Tests")
    class ErrorTypeMessageConstructorTests {

        @Test
        @DisplayName("Should set error type and message")
        void shouldSetErrorTypeAndMessage() {
            OpenRulesException exception = new OpenRulesException(
                    RuleErrorType.CONDITION_EVALUATION,
                    "Condition failed"
            );

            assertThat(exception.getMessage()).contains("Condition failed");
            assertThat(exception.ruleName()).isNull();
            assertThat(exception.errorType()).isEqualTo(RuleErrorType.CONDITION_EVALUATION);
        }
    }

    @Nested
    @DisplayName("Constructor(String, String, RuleErrorType) Tests")
    class FullDetailsConstructorTests {

        @Test
        @DisplayName("Should set all fields")
        void shouldSetAllFields() {
            OpenRulesException exception = new OpenRulesException(
                    "Rule execution failed",
                    "my-rule",
                    RuleErrorType.ACTION_EXECUTION
            );

            assertThat(exception.getMessage()).contains("Rule execution failed");
            assertThat(exception.ruleName()).isEqualTo("my-rule");
            assertThat(exception.errorType()).isEqualTo(RuleErrorType.ACTION_EXECUTION);
        }
    }

    @Nested
    @DisplayName("Constructor(String, String, RuleErrorType, Throwable) Tests")
    class FullDetailsWithCauseConstructorTests {

        @Test
        @DisplayName("Should set all fields with cause")
        void shouldSetAllFieldsWithCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            OpenRulesException exception = new OpenRulesException(
                    "Rule execution failed",
                    "my-rule",
                    RuleErrorType.ACTION_EXECUTION,
                    cause
            );

            assertThat(exception.getMessage()).contains("Rule execution failed");
            assertThat(exception.ruleName()).isEqualTo("my-rule");
            assertThat(exception.errorType()).isEqualTo(RuleErrorType.ACTION_EXECUTION);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("RuleErrorType Enum Tests")
    class RuleErrorTypeTests {

        @Test
        @DisplayName("Should have all expected error types")
        void shouldHaveAllExpectedErrorTypes() {
            RuleErrorType[] values = RuleErrorType.values();

            assertThat(values).hasSize(8);
            assertThat(values).containsExactly(
                    RuleErrorType.GENERAL,
                    RuleErrorType.CONDITION_EVALUATION,
                    RuleErrorType.ACTION_EXECUTION,
                    RuleErrorType.RULE_NOT_FOUND,
                    RuleErrorType.INVALID_DEFINITION,
                    RuleErrorType.CONFLICT_RESOLUTION,
                    RuleErrorType.DECISION_TABLE,
                    RuleErrorType.TIMEOUT
            );
        }

        @Test
        @DisplayName("valueOf() should return correct type")
        void valueOfShouldReturnCorrectType() {
            assertThat(RuleErrorType.valueOf("GENERAL")).isEqualTo(RuleErrorType.GENERAL);
            assertThat(RuleErrorType.valueOf("CONDITION_EVALUATION")).isEqualTo(RuleErrorType.CONDITION_EVALUATION);
            assertThat(RuleErrorType.valueOf("ACTION_EXECUTION")).isEqualTo(RuleErrorType.ACTION_EXECUTION);
            assertThat(RuleErrorType.valueOf("RULE_NOT_FOUND")).isEqualTo(RuleErrorType.RULE_NOT_FOUND);
            assertThat(RuleErrorType.valueOf("INVALID_DEFINITION")).isEqualTo(RuleErrorType.INVALID_DEFINITION);
            assertThat(RuleErrorType.valueOf("CONFLICT_RESOLUTION")).isEqualTo(RuleErrorType.CONFLICT_RESOLUTION);
            assertThat(RuleErrorType.valueOf("DECISION_TABLE")).isEqualTo(RuleErrorType.DECISION_TABLE);
            assertThat(RuleErrorType.valueOf("TIMEOUT")).isEqualTo(RuleErrorType.TIMEOUT);
        }

        @Test
        @DisplayName("valueOf() should throw for invalid name")
        void valueOfShouldThrowForInvalidName() {
            assertThatThrownBy(() -> RuleErrorType.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ordinal() should return correct position")
        void ordinalShouldReturnCorrectPosition() {
            assertThat(RuleErrorType.GENERAL.ordinal()).isEqualTo(0);
            assertThat(RuleErrorType.CONDITION_EVALUATION.ordinal()).isEqualTo(1);
            assertThat(RuleErrorType.ACTION_EXECUTION.ordinal()).isEqualTo(2);
            assertThat(RuleErrorType.RULE_NOT_FOUND.ordinal()).isEqualTo(3);
            assertThat(RuleErrorType.INVALID_DEFINITION.ordinal()).isEqualTo(4);
            assertThat(RuleErrorType.CONFLICT_RESOLUTION.ordinal()).isEqualTo(5);
            assertThat(RuleErrorType.DECISION_TABLE.ordinal()).isEqualTo(6);
            assertThat(RuleErrorType.TIMEOUT.ordinal()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            OpenRulesException exception = new OpenRulesException("Test");

            assertThatThrownBy(() -> { throw exception; })
                    .isInstanceOf(OpenRulesException.class)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Test");
        }
    }
}
