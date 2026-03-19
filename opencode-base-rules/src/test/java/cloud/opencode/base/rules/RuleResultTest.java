package cloud.opencode.base.rules;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RuleResult Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("RuleResult Tests")
class RuleResultTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("successBuilder() should create success builder")
        void successBuilderShouldCreateSuccessBuilder() {
            RuleResult result = RuleResult.successBuilder().build();
            assertThat(result.success()).isTrue();
        }

        @Test
        @DisplayName("failure() should create failure builder")
        void failureShouldCreateFailureBuilder() {
            RuleResult result = RuleResult.failure().build();
            assertThat(result.success()).isFalse();
        }

        @Test
        @DisplayName("fired() should add fired rule")
        void firedShouldAddFiredRule() {
            RuleResult result = RuleResult.successBuilder()
                    .fired("rule1")
                    .fired("rule2")
                    .build();

            assertThat(result.firedRules()).containsExactly("rule1", "rule2");
        }

        @Test
        @DisplayName("skipped() should add skipped rule")
        void skippedShouldAddSkippedRule() {
            RuleResult result = RuleResult.successBuilder()
                    .skipped("rule1")
                    .skipped("rule2")
                    .build();

            assertThat(result.skippedRules()).containsExactly("rule1", "rule2");
        }

        @Test
        @DisplayName("failed() should add failed rule with error")
        void failedShouldAddFailedRuleWithError() {
            Exception cause = new RuntimeException("test error");
            RuleResult result = RuleResult.failure()
                    .failed("rule1", "Error message", cause)
                    .build();

            assertThat(result.failedRules()).containsExactly("rule1");
            assertThat(result.errors()).hasSize(1);
            assertThat(result.errors().getFirst().ruleName()).isEqualTo("rule1");
            assertThat(result.errors().getFirst().message()).isEqualTo("Error message");
            assertThat(result.errors().getFirst().cause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("result() should add result entry")
        void resultShouldAddResultEntry() {
            RuleResult result = RuleResult.successBuilder()
                    .result("discount", 0.15)
                    .result("freeShipping", true)
                    .build();

            assertThat(result.results()).containsEntry("discount", 0.15);
            assertThat(result.results()).containsEntry("freeShipping", true);
        }

        @Test
        @DisplayName("results() should add multiple results")
        void resultsShouldAddMultipleResults() {
            RuleResult result = RuleResult.successBuilder()
                    .results(Map.of("key1", "value1", "key2", "value2"))
                    .build();

            assertThat(result.results()).containsEntry("key1", "value1");
            assertThat(result.results()).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("executionTime() should set duration")
        void executionTimeShouldSetDuration() {
            Duration duration = Duration.ofMillis(100);
            RuleResult result = RuleResult.successBuilder()
                    .executionTime(duration)
                    .build();

            assertThat(result.executionTime()).isEqualTo(duration);
        }
    }

    @Nested
    @DisplayName("Count Methods Tests")
    class CountMethodsTests {

        @Test
        @DisplayName("firedCount() should return count of fired rules")
        void firedCountShouldReturnCount() {
            RuleResult result = RuleResult.successBuilder()
                    .fired("r1").fired("r2").fired("r3")
                    .build();

            assertThat(result.firedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("skippedCount() should return count of skipped rules")
        void skippedCountShouldReturnCount() {
            RuleResult result = RuleResult.successBuilder()
                    .skipped("r1").skipped("r2")
                    .build();

            assertThat(result.skippedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("failedCount() should return count of failed rules")
        void failedCountShouldReturnCount() {
            RuleResult result = RuleResult.failure()
                    .failed("r1", "error1", null)
                    .failed("r2", "error2", null)
                    .build();

            assertThat(result.failedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("hasFired() should return true when rules fired")
        void hasFiredShouldReturnTrueWhenRulesFired() {
            RuleResult result = RuleResult.successBuilder()
                    .fired("rule1")
                    .build();

            assertThat(result.hasFired()).isTrue();
        }

        @Test
        @DisplayName("hasFired() should return false when no rules fired")
        void hasFiredShouldReturnFalseWhenNoRulesFired() {
            RuleResult result = RuleResult.successBuilder().build();
            assertThat(result.hasFired()).isFalse();
        }

        @Test
        @DisplayName("wasFired() should check specific rule")
        void wasFiredShouldCheckSpecificRule() {
            RuleResult result = RuleResult.successBuilder()
                    .fired("rule1")
                    .build();

            assertThat(result.wasFired("rule1")).isTrue();
            assertThat(result.wasFired("rule2")).isFalse();
        }

        @Test
        @DisplayName("wasSkipped() should check specific rule")
        void wasSkippedShouldCheckSpecificRule() {
            RuleResult result = RuleResult.successBuilder()
                    .skipped("rule1")
                    .build();

            assertThat(result.wasSkipped("rule1")).isTrue();
            assertThat(result.wasSkipped("rule2")).isFalse();
        }

        @Test
        @DisplayName("hasFailed() should check specific rule")
        void hasFailedShouldCheckSpecificRule() {
            RuleResult result = RuleResult.failure()
                    .failed("rule1", "error", null)
                    .build();

            assertThat(result.hasFailed("rule1")).isTrue();
            assertThat(result.hasFailed("rule2")).isFalse();
        }
    }

    @Nested
    @DisplayName("Result Access Tests")
    class ResultAccessTests {

        @Test
        @DisplayName("getResult() should return typed result")
        void getResultShouldReturnTypedResult() {
            RuleResult result = RuleResult.successBuilder()
                    .result("discount", 0.15)
                    .build();

            Double discount = result.getResult("discount");
            assertThat(discount).isEqualTo(0.15);
        }

        @Test
        @DisplayName("getResult() should return null for missing key")
        void getResultShouldReturnNullForMissingKey() {
            RuleResult result = RuleResult.successBuilder().build();
            assertThat(result.<Object>getResult("missing")).isNull();
        }

        @Test
        @DisplayName("getResult() with default should return default for missing key")
        void getResultWithDefaultShouldReturnDefaultForMissingKey() {
            RuleResult result = RuleResult.successBuilder().build();
            assertThat(result.getResult("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getResult() with default should return value if present")
        void getResultWithDefaultShouldReturnValueIfPresent() {
            RuleResult result = RuleResult.successBuilder()
                    .result("key", "actual")
                    .build();

            assertThat(result.getResult("key", "default")).isEqualTo("actual");
        }

        @Test
        @DisplayName("hasResult() should check if result exists")
        void hasResultShouldCheckIfResultExists() {
            RuleResult result = RuleResult.successBuilder()
                    .result("key", "value")
                    .build();

            assertThat(result.hasResult("key")).isTrue();
            assertThat(result.hasResult("missing")).isFalse();
        }
    }

    @Nested
    @DisplayName("RuleError Record Tests")
    class RuleErrorRecordTests {

        @Test
        @DisplayName("of() should create error without cause")
        void ofShouldCreateErrorWithoutCause() {
            RuleResult.RuleError error = RuleResult.RuleError.of("rule1", "error message");

            assertThat(error.ruleName()).isEqualTo("rule1");
            assertThat(error.message()).isEqualTo("error message");
            assertThat(error.cause()).isNull();
        }

        @Test
        @DisplayName("of() should create error with cause")
        void ofShouldCreateErrorWithCause() {
            Exception cause = new RuntimeException("cause");
            RuleResult.RuleError error = RuleResult.RuleError.of("rule1", "error message", cause);

            assertThat(error.ruleName()).isEqualTo("rule1");
            assertThat(error.message()).isEqualTo("error message");
            assertThat(error.cause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Record Component Tests")
    class RecordComponentTests {

        @Test
        @DisplayName("Record components should be accessible")
        void recordComponentsShouldBeAccessible() {
            RuleResult result = new RuleResult(
                    true,
                    List.of("fired1"),
                    List.of("skipped1"),
                    List.of("failed1"),
                    Map.of("key", "value"),
                    Duration.ofSeconds(1),
                    List.of(RuleResult.RuleError.of("rule", "error"))
            );

            assertThat(result.success()).isTrue();
            assertThat(result.firedRules()).containsExactly("fired1");
            assertThat(result.skippedRules()).containsExactly("skipped1");
            assertThat(result.failedRules()).containsExactly("failed1");
            assertThat(result.results()).containsEntry("key", "value");
            assertThat(result.executionTime()).isEqualTo(Duration.ofSeconds(1));
            assertThat(result.errors()).hasSize(1);
        }
    }
}
