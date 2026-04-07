package cloud.opencode.base.rules.validation;

import java.util.List;

/**
 * Validation Report - Aggregated Result of Rule Set Validation
 * 验证报告 - 规则集验证的聚合结果
 *
 * <p>Collects all validation issues found during rule validation and provides
 * convenience methods for querying errors, warnings, and overall validity.</p>
 * <p>收集规则验证期间发现的所有验证问题，并提供便捷方法查询错误、警告和整体有效性。</p>
 *
 * @param issues the list of validation issues | 验证问题列表
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ValidationIssue
 * @see RuleValidator
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record ValidationReport(List<ValidationIssue> issues) {

    /**
     * Creates a validation report with a defensive copy of the issues list
     * 使用问题列表的防御性副本创建验证报告
     *
     * @param issues the validation issues | 验证问题
     */
    public ValidationReport {
        issues = List.copyOf(issues);
    }

    /**
     * Returns a valid report with no issues
     * 返回没有问题的有效报告
     *
     * @return an empty valid report | 空的有效报告
     */
    public static ValidationReport valid() {
        return new ValidationReport(List.of());
    }

    /**
     * Checks whether the rule set is valid (no ERROR-level issues)
     * 检查规则集是否有效（没有ERROR级别的问题）
     *
     * @return true if no errors exist | 如果没有错误返回true
     */
    public boolean isValid() {
        return issues.stream().noneMatch(i -> i.severity() == ValidationIssue.Severity.ERROR);
    }

    /**
     * Checks whether any WARNING-level issues exist
     * 检查是否存在WARNING级别的问题
     *
     * @return true if warnings exist | 如果存在警告返回true
     */
    public boolean hasWarnings() {
        return issues.stream().anyMatch(i -> i.severity() == ValidationIssue.Severity.WARNING);
    }

    /**
     * Checks whether any ERROR-level issues exist
     * 检查是否存在ERROR级别的问题
     *
     * @return true if errors exist | 如果存在错误返回true
     */
    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.severity() == ValidationIssue.Severity.ERROR);
    }

    /**
     * Returns only ERROR-level issues
     * 仅返回ERROR级别的问题
     *
     * @return list of error issues | 错误问题列表
     */
    public List<ValidationIssue> errors() {
        return issues.stream()
                .filter(i -> i.severity() == ValidationIssue.Severity.ERROR)
                .toList();
    }

    /**
     * Returns only WARNING-level issues
     * 仅返回WARNING级别的问题
     *
     * @return list of warning issues | 警告问题列表
     */
    public List<ValidationIssue> warnings() {
        return issues.stream()
                .filter(i -> i.severity() == ValidationIssue.Severity.WARNING)
                .toList();
    }

    /**
     * Returns the total number of issues
     * 返回问题总数
     *
     * @return issue count | 问题数量
     */
    public int issueCount() {
        return issues.size();
    }

    @Override
    public String toString() {
        if (issues.isEmpty()) {
            return "ValidationReport{valid, 0 issues}";
        }
        long errorCount = errors().size();
        long warningCount = warnings().size();
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationReport{");
        sb.append(isValid() ? "valid" : "INVALID");
        sb.append(", ").append(errorCount).append(" error(s), ");
        sb.append(warningCount).append(" warning(s)");
        sb.append("}\n");
        for (ValidationIssue issue : issues) {
            sb.append("  - ").append(issue).append("\n");
        }
        return sb.toString().stripTrailing();
    }
}
