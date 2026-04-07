package cloud.opencode.base.rules.validation;

import cloud.opencode.base.rules.validation.ValidationIssue.IssueType;
import cloud.opencode.base.rules.validation.ValidationIssue.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ValidationReport}.
 * {@link ValidationReport} 的测试。
 */
@DisplayName("ValidationReport - 验证报告")
class ValidationReportTest {

    @Nested
    @DisplayName("Valid report - 有效报告")
    class ValidReport {

        @Test
        @DisplayName("static valid() returns empty valid report - 静态valid()返回空的有效报告")
        void validFactoryReturnsEmptyReport() {
            ValidationReport report = ValidationReport.valid();

            assertThat(report.isValid()).isTrue();
            assertThat(report.hasErrors()).isFalse();
            assertThat(report.hasWarnings()).isFalse();
            assertThat(report.issueCount()).isZero();
            assertThat(report.issues()).isEmpty();
            assertThat(report.errors()).isEmpty();
            assertThat(report.warnings()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error-only report - 仅错误报告")
    class ErrorOnly {

        @Test
        @DisplayName("Report with errors is invalid - 有错误的报告无效")
        void reportWithErrorsIsInvalid() {
            ValidationIssue error = ValidationIssue.error(
                    IssueType.DUPLICATE_RULE_NAME, "rule-x", "Duplicate");

            ValidationReport report = new ValidationReport(List.of(error));

            assertThat(report.isValid()).isFalse();
            assertThat(report.hasErrors()).isTrue();
            assertThat(report.hasWarnings()).isFalse();
            assertThat(report.errors()).hasSize(1);
            assertThat(report.warnings()).isEmpty();
            assertThat(report.issueCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Warning-only report - 仅警告报告")
    class WarningOnly {

        @Test
        @DisplayName("Report with only warnings is valid - 仅有警告的报告仍然有效")
        void reportWithWarningsIsValid() {
            ValidationIssue warning = ValidationIssue.warning(
                    IssueType.NEGATIVE_PRIORITY, "rule-y", "Negative priority");

            ValidationReport report = new ValidationReport(List.of(warning));

            assertThat(report.isValid()).isTrue();
            assertThat(report.hasErrors()).isFalse();
            assertThat(report.hasWarnings()).isTrue();
            assertThat(report.errors()).isEmpty();
            assertThat(report.warnings()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mixed report - 混合报告")
    class MixedReport {

        @Test
        @DisplayName("Report with errors and warnings - 同时有错误和警告的报告")
        void reportWithMixedIssues() {
            ValidationIssue error = ValidationIssue.error(
                    IssueType.EMPTY_RULE_NAME, null, "Empty name");
            ValidationIssue warning = ValidationIssue.warning(
                    IssueType.EMPTY_GROUP, "rule-z", "Empty group");

            ValidationReport report = new ValidationReport(List.of(error, warning));

            assertThat(report.isValid()).isFalse();
            assertThat(report.hasErrors()).isTrue();
            assertThat(report.hasWarnings()).isTrue();
            assertThat(report.errors()).hasSize(1);
            assertThat(report.warnings()).hasSize(1);
            assertThat(report.issueCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Immutability - 不可变性")
    class Immutability {

        @Test
        @DisplayName("Issues list is immutable - 问题列表不可变")
        void issuesListIsImmutable() {
            ValidationIssue issue = ValidationIssue.error(
                    IssueType.DUPLICATE_RULE_NAME, "rule", "dup");
            ValidationReport report = new ValidationReport(List.of(issue));

            org.junit.jupiter.api.Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> report.issues().add(ValidationIssue.warning(
                            IssueType.EMPTY_GROUP, "x", "y")));
        }
    }

    @Nested
    @DisplayName("toString - 字符串表示")
    class ToStringTest {

        @Test
        @DisplayName("Valid report toString - 有效报告的字符串表示")
        void validReportToString() {
            String str = ValidationReport.valid().toString();
            assertThat(str).contains("valid").contains("0 issues");
        }

        @Test
        @DisplayName("Invalid report toString - 无效报告的字符串表示")
        void invalidReportToString() {
            ValidationIssue error = ValidationIssue.error(
                    IssueType.DUPLICATE_RULE_NAME, "rule-a", "Duplicate");
            String str = new ValidationReport(List.of(error)).toString();
            assertThat(str).contains("INVALID").contains("1 error(s)");
        }
    }
}
