package cloud.opencode.base.rules.decision;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DecisionResult Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@DisplayName("DecisionResult Tests")
class DecisionResultTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("noMatch() should create no-match result")
        void noMatchShouldCreateNoMatchResult() {
            DecisionResult result = DecisionResult.noMatch();

            assertThat(result.matched()).isFalse();
            assertThat(result.hasMatch()).isFalse();
            assertThat(result.outputs()).isEmpty();
            assertThat(result.matchedRows()).isEmpty();
            assertThat(result.allOutputs()).isEmpty();
        }

        @Test
        @DisplayName("singleMatch() should create single-match result")
        void singleMatchShouldCreateSingleMatchResult() {
            Map<String, Object> outputs = Map.of("discount", 0.15, "freeShipping", true);
            DecisionResult result = DecisionResult.singleMatch(2, outputs);

            assertThat(result.matched()).isTrue();
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.outputs()).isEqualTo(outputs);
            assertThat(result.matchedRows()).containsExactly(2);
            assertThat(result.allOutputs()).containsExactly(outputs);
        }

        @Test
        @DisplayName("multipleMatches() should create multiple-match result")
        void multipleMatchesShouldCreateMultipleMatchResult() {
            Map<String, Object> out1 = Map.of("discount", 0.10);
            Map<String, Object> out2 = Map.of("discount", 0.15);
            List<Integer> rows = List.of(1, 3);
            List<Map<String, Object>> allOutputs = List.of(out1, out2);

            DecisionResult result = DecisionResult.multipleMatches(rows, allOutputs);

            assertThat(result.matched()).isTrue();
            assertThat(result.hasMatch()).isTrue();
            assertThat(result.outputs()).isEqualTo(out1); // First output
            assertThat(result.matchedRows()).containsExactly(1, 3);
            assertThat(result.allOutputs()).containsExactly(out1, out2);
        }

        @Test
        @DisplayName("multipleMatches() with empty outputs should use empty map")
        void multipleMatchesWithEmptyOutputsShouldUseEmptyMap() {
            DecisionResult result = DecisionResult.multipleMatches(List.of(1), List.of());

            assertThat(result.outputs()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasMatch() Tests")
    class HasMatchTests {

        @Test
        @DisplayName("hasMatch() should return true when matched")
        void hasMatchShouldReturnTrueWhenMatched() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of());
            assertThat(result.hasMatch()).isTrue();
        }

        @Test
        @DisplayName("hasMatch() should return false when not matched")
        void hasMatchShouldReturnFalseWhenNotMatched() {
            DecisionResult result = DecisionResult.noMatch();
            assertThat(result.hasMatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("get() Tests")
    class GetTests {

        @Test
        @DisplayName("get() should return typed value")
        void getShouldReturnTypedValue() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of(
                    "discount", 0.15,
                    "freeShipping", true,
                    "message", "VIP"
            ));

            Double discount = result.get("discount");
            Boolean freeShipping = result.get("freeShipping");
            String message = result.get("message");

            assertThat(discount).isEqualTo(0.15);
            assertThat(freeShipping).isTrue();
            assertThat(message).isEqualTo("VIP");
        }

        @Test
        @DisplayName("get() should return null for missing key")
        void getShouldReturnNullForMissingKey() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of("key", "value"));

            assertThat(result.<Object>get("missing")).isNull();
        }
    }

    @Nested
    @DisplayName("get() with Default Tests")
    class GetWithDefaultTests {

        @Test
        @DisplayName("get() with default should return value if present")
        void getWithDefaultShouldReturnValueIfPresent() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of("discount", 0.15));

            Double discount = result.get("discount", 0.0);
            assertThat(discount).isEqualTo(0.15);
        }

        @Test
        @DisplayName("get() with default should return default for missing key")
        void getWithDefaultShouldReturnDefaultForMissingKey() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of());

            Double discount = result.get("discount", 0.05);
            assertThat(discount).isEqualTo(0.05);
        }

        @Test
        @DisplayName("get() with default should return default for null value")
        void getWithDefaultShouldReturnDefaultForNullValue() {
            DecisionResult result = DecisionResult.noMatch();

            String value = result.get("key", "default");
            assertThat(value).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("matchCount() Tests")
    class MatchCountTests {

        @Test
        @DisplayName("matchCount() should return 0 for no match")
        void matchCountShouldReturnZeroForNoMatch() {
            DecisionResult result = DecisionResult.noMatch();
            assertThat(result.matchCount()).isZero();
        }

        @Test
        @DisplayName("matchCount() should return 1 for single match")
        void matchCountShouldReturnOneForSingleMatch() {
            DecisionResult result = DecisionResult.singleMatch(0, Map.of());
            assertThat(result.matchCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("matchCount() should return count for multiple matches")
        void matchCountShouldReturnCountForMultipleMatches() {
            DecisionResult result = DecisionResult.multipleMatches(
                    List.of(1, 2, 3),
                    List.of(Map.of(), Map.of(), Map.of())
            );
            assertThat(result.matchCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Record Component Tests")
    class RecordComponentTests {

        @Test
        @DisplayName("Record components should be accessible")
        void recordComponentsShouldBeAccessible() {
            Map<String, Object> outputs = Map.of("key", "value");
            List<Integer> matchedRows = List.of(1, 2);
            List<Map<String, Object>> allOutputs = List.of(outputs, outputs);

            DecisionResult result = new DecisionResult(true, outputs, matchedRows, allOutputs);

            assertThat(result.matched()).isTrue();
            assertThat(result.outputs()).isEqualTo(outputs);
            assertThat(result.matchedRows()).isEqualTo(matchedRows);
            assertThat(result.allOutputs()).isEqualTo(allOutputs);
        }
    }
}
