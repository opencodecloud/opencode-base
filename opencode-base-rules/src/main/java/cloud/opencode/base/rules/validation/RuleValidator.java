package cloud.opencode.base.rules.validation;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.engine.DefaultRule;
import cloud.opencode.base.rules.validation.ValidationIssue.IssueType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Rule Validator - Validates a Collection of Rules for Correctness
 * 规则验证器 - 验证规则集合的正确性
 *
 * <p>Performs static analysis on a set of rules to detect common issues such as
 * duplicate names, missing conditions, and unusual configurations.</p>
 * <p>对一组规则执行静态分析，检测重复名称、缺失条件和异常配置等常见问题。</p>
 *
 * <p><strong>Validation Checks | 验证检查:</strong></p>
 * <ul>
 *   <li>Duplicate rule names (ERROR) - 重复规则名称（错误）</li>
 *   <li>Empty/null rule names (ERROR) - 空/null规则名称（错误）</li>
 *   <li>Null conditions on DefaultRule (WARNING) - DefaultRule上的null条件（警告）</li>
 *   <li>Negative priority (WARNING) - 负优先级（警告）</li>
 *   <li>Empty group string (WARNING) - 空分组字符串（警告）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ValidationReport
 * @see ValidationIssue
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public final class RuleValidator {

    private RuleValidator() {
        // utility class
    }

    /**
     * Validates a collection of rules and returns a report
     * 验证规则集合并返回报告
     *
     * @param rules the rules to validate | 要验证的规则
     * @return the validation report | 验证报告
     * @throws NullPointerException if rules is null | 如果rules为null则抛出
     */
    public static ValidationReport validate(Collection<Rule> rules) {
        Objects.requireNonNull(rules, "rules must not be null");
        if (rules.isEmpty()) {
            return ValidationReport.valid();
        }

        List<ValidationIssue> issues = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (Rule rule : rules) {
            String name = rule.getName();

            // Check empty/null rule name
            if (name == null || name.isBlank()) {
                issues.add(ValidationIssue.error(
                        IssueType.EMPTY_RULE_NAME, name,
                        "Rule name must not be null or blank"));
            } else {
                // Check duplicate rule names
                if (!seenNames.add(name)) {
                    issues.add(ValidationIssue.error(
                            IssueType.DUPLICATE_RULE_NAME, name,
                            "Duplicate rule name: '" + name + "'"));
                }
            }

            // Check null condition on DefaultRule
            if (rule instanceof DefaultRule defaultRule) {
                if (defaultRule.getCondition() == null) {
                    issues.add(ValidationIssue.warning(
                            IssueType.NULL_CONDITION, name,
                            "Rule has a null condition"));
                }
                if (defaultRule.getAction() == null) {
                    issues.add(ValidationIssue.warning(
                            IssueType.NULL_ACTION, name,
                            "Rule has a null action"));
                }
            }

            // Check negative priority
            if (rule.getPriority() < 0) {
                issues.add(ValidationIssue.warning(
                        IssueType.NEGATIVE_PRIORITY, name,
                        "Rule has negative priority: " + rule.getPriority()));
            }

            // Check empty group string
            String group = rule.getGroup();
            if (group != null && group.isEmpty()) {
                issues.add(ValidationIssue.warning(
                        IssueType.EMPTY_GROUP, name,
                        "Rule has an empty group string"));
            }
        }

        return new ValidationReport(issues);
    }
}
