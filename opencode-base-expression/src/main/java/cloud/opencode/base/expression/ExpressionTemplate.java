package cloud.opencode.base.expression;

import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.StandardContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Expression Template Engine
 * 表达式模板引擎
 *
 * <p>Template engine that mixes literal text with {@code ${expression}} placeholders.
 * Each placeholder is evaluated using the expression engine and the result is
 * converted to a string and substituted inline.</p>
 * <p>将字面文本与 {@code ${expression}} 占位符混合的模板引擎。
 * 每个占位符使用表达式引擎求值，结果转换为字符串并内联替换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Delimiter syntax: {@code ${expression}} (like JavaScript/Kotlin template strings) -
 *       分隔符语法: {@code ${expression}}（类似JavaScript/Kotlin模板字符串）</li>
 *   <li>Escape support: {@code \${} for literal {@code ${}} -
 *       转义支持: {@code \${} 表示字面量 {@code ${}}</li>
 *   <li>Variable binding via Map or EvaluationContext - 通过Map或EvaluationContext绑定变量</li>
 *   <li>Full expression engine support inside placeholders - 占位符内支持完整的表达式引擎</li>
 *   <li>Nested brace handling within expressions - 表达式内的嵌套花括号处理</li>
 *   <li>Error reporting with position information for unclosed delimiters - 未关闭分隔符的位置信息错误报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple variable substitution
 * String result = ExpressionTemplate.render(
 *     "Hello, ${name}!",
 *     Map.of("name", "World")
 * );
 * // result = "Hello, World!"
 *
 * // Expression evaluation within template
 * String result = ExpressionTemplate.render(
 *     "Total: ${price * quantity}",
 *     Map.of("price", 9.99, "quantity", 3)
 * );
 * // result = "Total: 29.97"
 *
 * // Multiple placeholders
 * String result = ExpressionTemplate.render(
 *     "${firstName} ${lastName} is ${age} years old",
 *     Map.of("firstName", "John", "lastName", "Doe", "age", 30)
 * );
 * // result = "John Doe is 30 years old"
 *
 * // Escaped placeholder
 * String result = ExpressionTemplate.render(
 *     "Use \\${variable} for templates",
 *     Map.of()
 * );
 * // result = "Use ${variable} for templates"
 *
 * // With EvaluationContext
 * StandardContext ctx = new StandardContext();
 * ctx.setVariable("user", myUser);
 * String result = ExpressionTemplate.render(
 *     "Welcome, ${user.name}!",
 *     ctx
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility with no shared mutable state -
 *       线程安全: 是，无共享可变状态的无状态工具</li>
 *   <li>Null-safe: No, null template or variables throw NullPointerException -
 *       空值安全: 否，null模板或变量抛出NullPointerException</li>
 *   <li>Expressions within placeholders are subject to the same security policies as
 *       direct expression evaluation - 占位符内的表达式受与直接表达式求值相同的安全策略约束</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Single-pass template parsing - 单次遍历模板解析</li>
 *   <li>StringBuilder-based rendering with no regex overhead - 基于StringBuilder的渲染，无正则开销</li>
 *   <li>Expression results are cached by the underlying expression engine - 表达式结果由底层表达式引擎缓存</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public final class ExpressionTemplate {

    private ExpressionTemplate() {
    }

    /**
     * Render a template with variable bindings from a Map
     * 使用Map中的变量绑定渲染模板
     *
     * @param template the template string containing {@code ${expression}} placeholders | 包含 {@code ${expression}} 占位符的模板字符串
     * @param variables the variable map for expression evaluation | 用于表达式求值的变量映射
     * @return the rendered string with all placeholders resolved | 所有占位符已解析的渲染字符串
     * @throws NullPointerException if template or variables is null | 如果模板或变量为null
     * @throws OpenExpressionException if a placeholder is unclosed or expression evaluation fails | 如果占位符未关闭或表达式求值失败
     */
    public static String render(String template, Map<String, Object> variables) {
        Objects.requireNonNull(template, "template cannot be null");
        Objects.requireNonNull(variables, "variables cannot be null");
        StandardContext ctx = new StandardContext();
        variables.forEach(ctx::setVariable);
        return render(template, ctx);
    }

    /**
     * Render a template with an EvaluationContext
     * 使用EvaluationContext渲染模板
     *
     * @param template the template string containing {@code ${expression}} placeholders | 包含 {@code ${expression}} 占位符的模板字符串
     * @param context the evaluation context for expression evaluation | 用于表达式求值的求值上下文
     * @return the rendered string with all placeholders resolved | 所有占位符已解析的渲染字符串
     * @throws NullPointerException if template or context is null | 如果模板或上下文为null
     * @throws OpenExpressionException if a placeholder is unclosed or expression evaluation fails | 如果占位符未关闭或表达式求值失败
     */
    private static final int MAX_TEMPLATE_LENGTH = 1_000_000;
    private static final int MAX_BRACE_DEPTH = 50;

    public static String render(String template, EvaluationContext context) {
        Objects.requireNonNull(template, "template cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        if (template.length() > MAX_TEMPLATE_LENGTH) {
            throw new OpenExpressionException("Template length " + template.length()
                    + " exceeds maximum " + MAX_TEMPLATE_LENGTH);
        }

        List<Segment> segments = parseTemplate(template);
        StringBuilder sb = new StringBuilder(template.length());
        for (Segment segment : segments) {
            switch (segment) {
                case TextSegment text -> sb.append(text.text());
                case ExpressionSegment expr -> {
                    try {
                        Object result = OpenExpression.eval(expr.expression(), context);
                        sb.append(result == null ? "null" : result.toString());
                    } catch (OpenExpressionException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new OpenExpressionException(
                                "Failed to evaluate template expression: " + expr.expression(), e);
                    }
                }
            }
        }
        return sb.toString();
    }

    // ==================== Internal Template Parsing | 内部模板解析 ====================

    /**
     * Parse a template string into a list of text and expression segments.
     * 将模板字符串解析为文本和表达式段的列表。
     *
     * @param template the template string | 模板字符串
     * @return the list of segments | 段列表
     */
    private static List<Segment> parseTemplate(String template) {
        List<Segment> segments = new ArrayList<>();
        int length = template.length();
        int pos = 0;
        StringBuilder textBuffer = new StringBuilder();

        while (pos < length) {
            // Check for escaped ${ -> \${
            if (pos < length - 2 && template.charAt(pos) == '\\' && template.charAt(pos + 1) == '$'
                    && template.charAt(pos + 2) == '{') {
                textBuffer.append("${");
                pos += 3;
                continue;
            }

            // Check for ${ delimiter
            if (pos < length - 1 && template.charAt(pos) == '$' && template.charAt(pos + 1) == '{') {
                // Flush accumulated text
                if (!textBuffer.isEmpty()) {
                    segments.add(new TextSegment(textBuffer.toString()));
                    textBuffer.setLength(0);
                }

                // Find matching closing brace, accounting for nested braces
                int start = pos + 2;
                int braceDepth = 1;
                int exprEnd = start;
                while (exprEnd < length && braceDepth > 0) {
                    char ch = template.charAt(exprEnd);
                    if (ch == '{') {
                        braceDepth++;
                        if (braceDepth > MAX_BRACE_DEPTH) {
                            throw new OpenExpressionException(
                                    "Template expression nesting exceeds maximum depth of " + MAX_BRACE_DEPTH,
                                    template, exprEnd);
                        }
                    } else if (ch == '}') {
                        braceDepth--;
                    } else if (ch == '\'' || ch == '"') {
                        // Skip string literals to avoid counting braces within strings
                        char quote = ch;
                        exprEnd++;
                        while (exprEnd < length && template.charAt(exprEnd) != quote) {
                            if (template.charAt(exprEnd) == '\\') {
                                exprEnd++; // skip escaped character
                            }
                            exprEnd++;
                        }
                        // exprEnd now points at the closing quote (or end of string)
                    }
                    exprEnd++;
                }

                if (braceDepth != 0) {
                    throw new OpenExpressionException(
                            "Unclosed template expression delimiter '${'",
                            template, pos);
                }

                // exprEnd is one past the closing }, so the expression is [start, exprEnd-1)
                String expression = template.substring(start, exprEnd - 1).trim();
                if (expression.isEmpty()) {
                    throw new OpenExpressionException(
                            "Empty template expression '${}'",
                            template, pos);
                }
                segments.add(new ExpressionSegment(expression));
                pos = exprEnd;
                continue;
            }

            // Regular character
            textBuffer.append(template.charAt(pos));
            pos++;
        }

        // Flush remaining text
        if (!textBuffer.isEmpty()) {
            segments.add(new TextSegment(textBuffer.toString()));
        }

        return segments;
    }

    // ==================== Segment Types | 段类型 ====================

    /**
     * A segment in a parsed template.
     * 解析模板中的一个段。
     */
    private sealed interface Segment permits TextSegment, ExpressionSegment {
    }

    /**
     * A literal text segment.
     * 字面文本段。
     */
    private record TextSegment(String text) implements Segment {
    }

    /**
     * An expression segment to be evaluated.
     * 要被求值的表达式段。
     */
    private record ExpressionSegment(String expression) implements Segment {
    }
}
