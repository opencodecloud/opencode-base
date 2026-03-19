package cloud.opencode.base.expression.compiler;

import cloud.opencode.base.expression.Expression;
import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.context.StandardContext;
import cloud.opencode.base.expression.eval.TypeCoercion;
import cloud.opencode.base.expression.parser.Parser;

import java.util.Objects;

/**
 * Compiled Expression
 * 编译后的表达式
 *
 * <p>A pre-compiled expression for efficient repeated evaluation.</p>
 * <p>用于高效重复求值的预编译表达式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-parsed AST for repeated evaluation - 预解析AST用于重复求值</li>
 *   <li>Implements Expression interface - 实现Expression接口</li>
 *   <li>Type-safe evaluation with conversion - 类型安全求值与转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CompiledExpression expr = CompiledExpression.compile("price * quantity");
 * // Reuse across multiple contexts
 * Object result1 = expr.getValue(ctx1);
 * Object result2 = expr.getValue(ctx2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction - 线程安全: 是，构造后不可变</li>
 *   <li>Null-safe: Yes, null context creates default - 空值安全: 是，null上下文创建默认值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class CompiledExpression implements Expression {

    private final String expressionString;
    private final Node ast;

    /**
     * Package-private constructor for use by ExpressionCompiler
     * 包私有构造函数，供 ExpressionCompiler 使用
     *
     * @param expressionString the expression string | 表达式字符串
     * @param ast the AST node | AST 节点
     */
    CompiledExpression(String expressionString, Node ast) {
        this.expressionString = expressionString;
        this.ast = ast;
    }

    /**
     * Compile an expression
     * 编译表达式
     *
     * @param expression the expression string | 表达式字符串
     * @return the compiled expression | 编译后的表达式
     */
    public static CompiledExpression compile(String expression) {
        Objects.requireNonNull(expression, "expression cannot be null");
        Node ast = Parser.parse(expression);
        return new CompiledExpression(expression, ast);
    }

    @Override
    public String getExpressionString() {
        return expressionString;
    }

    @Override
    public Object getValue() {
        return getValue(new StandardContext());
    }

    @Override
    public Object getValue(EvaluationContext context) {
        return ast.evaluate(context != null ? context : new StandardContext());
    }

    @Override
    public <T> T getValue(Class<T> targetType) {
        return getValue(new StandardContext(), targetType);
    }

    @Override
    public <T> T getValue(EvaluationContext context, Class<T> targetType) {
        Object result = getValue(context);
        return TypeCoercion.convert(result, targetType);
    }

    @Override
    public Object getValue(Object rootObject) {
        return getValue(new StandardContext(rootObject));
    }

    @Override
    public <T> T getValue(Object rootObject, Class<T> targetType) {
        return getValue(new StandardContext(rootObject), targetType);
    }

    @Override
    public Class<?> getValueType() {
        Object value = getValue();
        return value != null ? value.getClass() : Object.class;
    }

    @Override
    public Class<?> getValueType(EvaluationContext context) {
        Object value = getValue(context);
        return value != null ? value.getClass() : Object.class;
    }

    /**
     * Get the AST root node
     * 获取AST根节点
     *
     * @return the AST root node | AST根节点
     */
    public Node getAst() {
        return ast;
    }

    @Override
    public String toString() {
        return "CompiledExpression[" + expressionString + "]";
    }
}
