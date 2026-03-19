package cloud.opencode.base.test.assertion;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AssertionResultTest Tests
 * AssertionResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
@DisplayName("AssertionResult Tests")
class AssertionResultTest {

    @Nested
    @DisplayName("Success Tests")
    class SuccessTests {

        @Test
        @DisplayName("success should create success result")
        void successShouldCreateSuccessResult() {
            AssertionResult result = AssertionResult.success();
            assertThat(result).isNotNull();
            assertThat(result.passed()).isTrue();
        }

        @Test
        @DisplayName("success should return singleton instance")
        void successShouldReturnSingletonInstance() {
            AssertionResult result1 = AssertionResult.success();
            AssertionResult result2 = AssertionResult.success();
            assertThat(result1).isSameAs(result2);
        }

        @Test
        @DisplayName("success toString should return Success")
        void successToStringShouldReturnSuccess() {
            AssertionResult result = AssertionResult.success();
            assertThat(result.toString()).isEqualTo("Success");
        }
    }

    @Nested
    @DisplayName("Failure Tests")
    class FailureTests {

        @Test
        @DisplayName("failure with message should create failure result")
        void failureWithMessageShouldCreateFailureResult() {
            AssertionResult result = AssertionResult.failure("test failure");
            assertThat(result).isNotNull();
            assertThat(result.passed()).isFalse();
        }

        @Test
        @DisplayName("failure with values should create failure result")
        void failureWithValuesShouldCreateFailureResult() {
            AssertionResult result = AssertionResult.failure("test failure", "expected", "actual");
            assertThat(result).isNotNull();
            assertThat(result.passed()).isFalse();
        }
    }

    @Nested
    @DisplayName("Failure Record Tests")
    class FailureRecordTests {

        @Test
        @DisplayName("Should access message")
        void shouldAccessMessage() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("test message");
            assertThat(failure.message()).isEqualTo("test message");
        }

        @Test
        @DisplayName("Should access expected value")
        void shouldAccessExpectedValue() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg", "expected", "actual");
            assertThat(failure.expected()).isEqualTo("expected");
        }

        @Test
        @DisplayName("Should access actual value")
        void shouldAccessActualValue() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg", "expected", "actual");
            assertThat(failure.actual()).isEqualTo("actual");
        }

        @Test
        @DisplayName("hasValues should return true when values exist")
        void hasValuesShouldReturnTrueWhenValuesExist() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg", "expected", "actual");
            assertThat(failure.hasValues()).isTrue();
        }

        @Test
        @DisplayName("hasValues should return false when no values")
        void hasValuesShouldReturnFalseWhenNoValues() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg");
            assertThat(failure.hasValues()).isFalse();
        }

        @Test
        @DisplayName("toString should include values when present")
        void toStringShouldIncludeValuesWhenPresent() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg", "expected", "actual");
            String str = failure.toString();
            assertThat(str).contains("msg");
            assertThat(str).contains("expected");
            assertThat(str).contains("actual");
        }

        @Test
        @DisplayName("toString should not include values when absent")
        void toStringShouldNotIncludeValuesWhenAbsent() {
            AssertionResult.Failure failure = (AssertionResult.Failure) AssertionResult.failure("msg");
            String str = failure.toString();
            assertThat(str).contains("msg");
            assertThat(str).doesNotContain("expected=");
        }
    }

    @Nested
    @DisplayName("Sealed Interface Tests")
    class SealedInterfaceTests {

        @Test
        @DisplayName("Success should implement AssertionResult")
        void successShouldImplementAssertionResult() {
            AssertionResult result = AssertionResult.success();
            assertThat(result).isInstanceOf(AssertionResult.Success.class);
        }

        @Test
        @DisplayName("Failure should implement AssertionResult")
        void failureShouldImplementAssertionResult() {
            AssertionResult result = AssertionResult.failure("test");
            assertThat(result).isInstanceOf(AssertionResult.Failure.class);
        }

        @Test
        @DisplayName("Should work with pattern matching")
        void shouldWorkWithPatternMatching() {
            AssertionResult result = AssertionResult.failure("test", "a", "b");
            String output = switch (result) {
                case AssertionResult.Success _ -> "success";
                case AssertionResult.Failure f -> "failure: " + f.message();
            };
            assertThat(output).isEqualTo("failure: test");
        }
    }
}
