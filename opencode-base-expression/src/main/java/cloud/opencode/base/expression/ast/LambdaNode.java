package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.Objects;

/**
 * Lambda Expression Node
 * Lambda 表达式节点
 *
 * <p>Represents a lambda/arrow function: parameter {@code ->} body.
 * Lambda nodes are first-class values that can be passed as arguments to
 * higher-order functions like filter, map, and reduce.</p>
 * <p>表示 lambda/箭头函数：parameter {@code ->} body。
 * Lambda 节点是一等值，可以作为参数传递给高阶函数，如 filter、map 和 reduce。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>First-class function values in expressions - 表达式中的一等函数值</li>
 *   <li>Single parameter binding with scoped evaluation - 单参数绑定与作用域求值</li>
 *   <li>Child context creation for variable isolation - 子上下文创建用于变量隔离</li>
 *   <li>Multi-argument application support for future extensibility - 多参数应用支持，便于未来扩展</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda as filter predicate
 * Node lambda = LambdaNode.of("x", BinaryOpNode.of(
 *     IdentifierNode.of("x"), ">", LiteralNode.ofInt(10)
 * ));
 * // evaluate() returns the LambdaNode itself (first-class value)
 * Object value = lambda.evaluate(ctx);  // returns LambdaNode
 *
 * // Apply lambda to an argument
 * Object result = lambda.apply(42, ctx);  // true (42 > 10)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: No, parameter and body must be non-null - 空值安全: 否，参数名和函数体不能为空</li>
 * </ul>
 *
 * @param parameter the lambda parameter name | Lambda 参数名
 * @param body the lambda body expression | Lambda 函数体表达式
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public record LambdaNode(String parameter, Node body) implements Node {

    public LambdaNode {
        Objects.requireNonNull(parameter, "parameter cannot be null");
        if (parameter.isBlank()) {
            throw new IllegalArgumentException("parameter cannot be blank");
        }
        Objects.requireNonNull(body, "body cannot be null");
    }

    /**
     * Create lambda expression node
     * 创建 Lambda 表达式节点
     *
     * @param parameter the parameter name | 参数名
     * @param body the body expression | 函数体表达式
     * @return the lambda node | Lambda 节点
     */
    public static LambdaNode of(String parameter, Node body) {
        return new LambdaNode(parameter, body);
    }

    /**
     * Evaluate this lambda node
     * 求值此 Lambda 节点
     *
     * <p>Returns the LambdaNode itself as a first-class value.
     * The lambda is applied later by functions like filter/map via {@link #apply}.</p>
     * <p>返回 LambdaNode 本身作为一等值。
     * Lambda 稍后通过 {@link #apply} 由 filter/map 等函数调用。</p>
     *
     * @param context the evaluation context | 求值上下文
     * @return this LambdaNode | 此 LambdaNode
     */
    @Override
    public Object evaluate(EvaluationContext context) {
        return this;
    }

    /**
     * Apply this lambda to a single argument
     * 将此 Lambda 应用于单个参数
     *
     * <p>Creates a child context, binds the parameter to the argument,
     * and evaluates the body in the child context.</p>
     * <p>创建子上下文，将参数绑定到实参，并在子上下文中求值函数体。</p>
     *
     * @param argument the argument value | 实参值
     * @param context the parent evaluation context | 父求值上下文
     * @return the evaluation result | 求值结果
     */
    public Object apply(Object argument, EvaluationContext context) {
        EvaluationContext child = context.createChild();
        child.setVariable(parameter, argument);
        return body.evaluate(child);
    }

    /**
     * Apply this lambda to multiple arguments (future extensibility)
     * 将此 Lambda 应用于多个参数（未来扩展）
     *
     * <p>Binds the first argument to the parameter name. Additional arguments
     * are bound as {@code _arg1}, {@code _arg2}, etc. for future multi-parameter
     * lambda support.</p>
     * <p>将第一个参数绑定到参数名。额外的参数绑定为 {@code _arg1}、{@code _arg2} 等，
     * 用于未来的多参数 Lambda 支持。</p>
     *
     * @param args the argument values | 实参值数组
     * @param context the parent evaluation context | 父求值上下文
     * @return the evaluation result | 求值结果
     * @throws IllegalArgumentException if args is empty | 如果参数为空则抛出异常
     */
    public Object applyMulti(Object[] args, EvaluationContext context) {
        Objects.requireNonNull(args, "args cannot be null");
        if (args.length == 0) {
            throw new IllegalArgumentException("Lambda requires at least one argument");
        }
        EvaluationContext child = context.createChild();
        child.setVariable(parameter, args[0]);
        for (int i = 1; i < args.length; i++) {
            child.setVariable("_arg" + i, args[i]);
        }
        return body.evaluate(child);
    }

    @Override
    public String toExpressionString() {
        return "(" + parameter + " -> " + body.toExpressionString() + ")";
    }
}
