package cloud.opencode.base.rules.validation;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.dsl.RuleBuilder;
import cloud.opencode.base.rules.engine.DefaultRule;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import cloud.opencode.base.rules.validation.ValidationIssue.IssueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link RuleValidator}.
 * {@link RuleValidator} 的测试。
 */
@DisplayName("RuleValidator - 规则验证器")
class RuleValidatorTest {

    @Nested
    @DisplayName("Valid rules - 有效规则")
    class ValidRules {

        @Test
        @DisplayName("Should return valid report for well-formed rules - 格式良好的规则应返回有效报告")
        void shouldReturnValidForWellFormedRules() {
            Rule rule1 = RuleBuilder.rule("rule-a")
                    .when((Condition) _ -> true).then((Action) _ -> {}).build();
            Rule rule2 = RuleBuilder.rule("rule-b")
                    .when((Condition) _ -> false).then((Action) _ -> {}).build();

            ValidationReport report = RuleValidator.validate(List.of(rule1, rule2));

            assertThat(report.isValid()).isTrue();
            assertThat(report.hasErrors()).isFalse();
            assertThat(report.hasWarnings()).isFalse();
            assertThat(report.issueCount()).isZero();
        }

        @Test
        @DisplayName("Should return valid report for empty collection - 空集合应返回有效报告")
        void shouldReturnValidForEmptyCollection() {
            ValidationReport report = RuleValidator.validate(List.of());

            assertThat(report.isValid()).isTrue();
            assertThat(report.issueCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Duplicate names - 重复名称")
    class DuplicateNames {

        @Test
        @DisplayName("Should detect duplicate rule names - 应检测重复规则名称")
        void shouldDetectDuplicateNames() {
            Rule rule1 = RuleBuilder.rule("same-name")
                    .when((Condition) _ -> true).then((Action) _ -> {}).build();
            Rule rule2 = RuleBuilder.rule("same-name")
                    .when((Condition) _ -> false).then((Action) _ -> {}).build();

            ValidationReport report = RuleValidator.validate(List.of(rule1, rule2));

            assertThat(report.isValid()).isFalse();
            assertThat(report.hasErrors()).isTrue();
            assertThat(report.errors()).hasSize(1);
            assertThat(report.errors().getFirst().type()).isEqualTo(IssueType.DUPLICATE_RULE_NAME);
        }
    }

    @Nested
    @DisplayName("Empty names - 空名称")
    class EmptyNames {

        @Test
        @DisplayName("Should detect null rule name - 应检测null规则名称")
        void shouldDetectNullName() {
            Rule rule = createRuleWithName(null);

            ValidationReport report = RuleValidator.validate(List.of(rule));

            assertThat(report.isValid()).isFalse();
            assertThat(report.errors()).hasSize(1);
            assertThat(report.errors().getFirst().type()).isEqualTo(IssueType.EMPTY_RULE_NAME);
        }

        @Test
        @DisplayName("Should detect blank rule name - 应检测空白规则名称")
        void shouldDetectBlankName() {
            Rule rule = new DefaultRule("  ", "desc", 1, null, true,
                    _ -> true, _ -> {});

            ValidationReport report = RuleValidator.validate(List.of(rule));

            assertThat(report.isValid()).isFalse();
            assertThat(report.errors().getFirst().type()).isEqualTo(IssueType.EMPTY_RULE_NAME);
        }
    }

    @Nested
    @DisplayName("Priority warnings - 优先级警告")
    class PriorityWarnings {

        @Test
        @DisplayName("Should warn on negative priority - 应对负优先级发出警告")
        void shouldWarnOnNegativePriority() {
            Rule rule = new DefaultRule("neg-priority", "desc", -5, null, true,
                    _ -> true, _ -> {});

            ValidationReport report = RuleValidator.validate(List.of(rule));

            assertThat(report.isValid()).isTrue();
            assertThat(report.hasWarnings()).isTrue();
            assertThat(report.warnings()).hasSize(1);
            assertThat(report.warnings().getFirst().type()).isEqualTo(IssueType.NEGATIVE_PRIORITY);
        }
    }

    @Nested
    @DisplayName("Empty group - 空分组")
    class EmptyGroup {

        @Test
        @DisplayName("Should warn on empty group string - 应对空分组字符串发出警告")
        void shouldWarnOnEmptyGroup() {
            Rule rule = new DefaultRule("group-rule", "desc", 1, "", true,
                    _ -> true, _ -> {});

            ValidationReport report = RuleValidator.validate(List.of(rule));

            assertThat(report.isValid()).isTrue();
            assertThat(report.hasWarnings()).isTrue();
            assertThat(report.warnings().getFirst().type()).isEqualTo(IssueType.EMPTY_GROUP);
        }

        @Test
        @DisplayName("Should not warn on null group - 不应对null分组发出警告")
        void shouldNotWarnOnNullGroup() {
            Rule rule = RuleBuilder.rule("no-group")
                    .when((Condition) _ -> true).then((Action) _ -> {}).build();

            ValidationReport report = RuleValidator.validate(List.of(rule));

            assertThat(report.warnings().stream()
                    .filter(i -> i.type() == IssueType.EMPTY_GROUP)
                    .toList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Null conditions - null条件")
    class NullConditions {

        @Test
        @DisplayName("DefaultRule rejects null condition - DefaultRule拒绝null条件")
        void defaultRuleRejectsNullCondition() {
            assertThatThrownBy(() -> new DefaultRule("null-cond", "desc", 1, null, true,
                    null, _ -> {}))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("DefaultRule rejects null action - DefaultRule拒绝null动作")
        void defaultRuleRejectsNullAction() {
            assertThatThrownBy(() -> new DefaultRule("null-action", "desc", 1, null, true,
                    _ -> true, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Mixed issues - 混合问题")
    class MixedIssues {

        @Test
        @DisplayName("Should collect multiple issues - 应收集多个问题")
        void shouldCollectMultipleIssues() {
            Rule dupA = RuleBuilder.rule("dup")
                    .when((Condition) _ -> true).then((Action) _ -> {}).build();
            Rule dupB = RuleBuilder.rule("dup")
                    .when((Condition) _ -> true).then((Action) _ -> {}).build();
            Rule negPriority = new DefaultRule("neg", "desc", -1, "", true,
                    _ -> true, _ -> {});

            ValidationReport report = RuleValidator.validate(List.of(dupA, dupB, negPriority));

            // 1 duplicate error + 1 negative priority warning + 1 empty group warning
            assertThat(report.isValid()).isFalse();
            assertThat(report.errors()).hasSize(1);
            assertThat(report.warnings().size()).isGreaterThanOrEqualTo(2);
            assertThat(report.issueCount()).isGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Null input - null输入")
    class NullInput {

        @Test
        @DisplayName("Should throw on null rules collection - 应对null规则集合抛出异常")
        void shouldThrowOnNullInput() {
            assertThatThrownBy(() -> RuleValidator.validate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private static Rule createRuleWithName(String name) {
        return new Rule() {
            @Override public String getName() { return name; }
            @Override public String getDescription() { return "desc"; }
            @Override public int getPriority() { return 1; }
            @Override public boolean evaluate(RuleContext context) { return true; }
            @Override public void execute(RuleContext context) { }
        };
    }
}
