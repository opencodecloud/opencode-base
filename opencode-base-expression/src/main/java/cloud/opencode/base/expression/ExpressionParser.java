package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * Expression Parser Interface
 * 表达式解析器接口
 *
 * <p>Parses expression strings into Expression objects.</p>
 * <p>将表达式字符串解析为Expression对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse expression strings into evaluable Expression objects - 将表达式字符串解析为可求值的Expression对象</li>
 *   <li>Support template expressions with embedded variables - 支持嵌入变量的模板表达式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExpressionParser parser = OpenExpression.parser();
 * Expression expr = parser.parseExpression("price * quantity");
 * Object result = expr.getValue(context);
 *
 * // Template parsing
 * Expression template = parser.parseTemplate("Hello #{name}");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No, null expression throws exception - 空值安全: 否，null表达式抛出异常</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for parseExpression where n is the expression length - 时间复杂度: parseExpression 为 O(n)，n为表达式长度</li>
 *   <li>Space complexity: O(n) for the resulting AST - 空间复杂度: O(n)，存储生成的 AST</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface ExpressionParser {

    /**
     * Parse expression string
     * 解析表达式字符串
     *
     * @param expressionString the expression string | 表达式字符串
     * @return the parsed expression | 解析后的表达式
     * @throws OpenExpressionException if parsing fails | 如果解析失败
     */
    Expression parseExpression(String expressionString);

    /**
     * Parse expression with template
     * 使用模板解析表达式
     *
     * <p>Parses expressions embedded in template strings like "Hello #{name}".</p>
     * <p>解析嵌入在模板字符串中的表达式，如"Hello #{name}"。</p>
     *
     * @param templateString the template string | 模板字符串
     * @return the parsed expression | 解析后的表达式
     */
    default Expression parseTemplate(String templateString) {
        return parseExpression(templateString);
    }
}
