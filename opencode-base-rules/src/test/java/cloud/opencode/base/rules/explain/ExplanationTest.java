package cloud.opencode.base.rules.explain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Explanation Tests
 * Explanation 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
@DisplayName("Explanation Tests | Explanation 测试")
class ExplanationTest {

    @Nested
    @DisplayName("Record Tests | 记录测试")
    class RecordTests {

        @Test
        @DisplayName("firedCount returns correct count | firedCount返回正确计数")
        void testFiredCount() {
            Explanation explanation = new Explanation("summary", List.of(
                    new RuleExplanation("r1", true, "fired"),
                    new RuleExplanation("r2", false, "not fired"),
                    new RuleExplanation("r3", true, "fired")
            ));

            assertThat(explanation.firedCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("totalCount returns correct count | totalCount返回正确计数")
        void testTotalCount() {
            Explanation explanation = new Explanation("summary", List.of(
                    new RuleExplanation("r1", true, "fired"),
                    new RuleExplanation("r2", false, "not fired")
            ));

            assertThat(explanation.totalCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("toString produces multi-line output | toString生成多行输出")
        void testToString() {
            Explanation explanation = new Explanation("Summary line", List.of(
                    new RuleExplanation("r1", true, "Rule 'r1' fired"),
                    new RuleExplanation("r2", false, "Rule 'r2' did not fire")
            ));

            String output = explanation.toString();
            assertThat(output).contains("Summary line");
            assertThat(output).contains("  - Rule 'r1' fired");
            assertThat(output).contains("  - Rule 'r2' did not fire");
        }

        @Test
        @DisplayName("empty details | 空详情列表")
        void testEmptyDetails() {
            Explanation explanation = new Explanation("No rules", List.of());

            assertThat(explanation.firedCount()).isZero();
            assertThat(explanation.totalCount()).isZero();
            assertThat(explanation.toString()).contains("No rules");
        }
    }

    @Nested
    @DisplayName("Validation Tests | 验证测试")
    class ValidationTests {

        @Test
        @DisplayName("null summary throws NullPointerException | null summary抛出NullPointerException")
        void testNullSummary() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Explanation(null, List.of()));
        }

        @Test
        @DisplayName("null details throws NullPointerException | null details抛出NullPointerException")
        void testNullDetails() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new Explanation("summary", null));
        }

        @Test
        @DisplayName("details is immutable | details是不可变的")
        void testImmutableDetails() {
            Explanation explanation = new Explanation("s", List.of(
                    new RuleExplanation("r1", true, "reason")
            ));
            assertThatThrownBy(() -> explanation.details().add(
                    new RuleExplanation("r2", false, "x")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
