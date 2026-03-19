package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.context.EvaluationContext;
import cloud.opencode.base.expression.function.Function;

import java.util.List;
import java.util.Objects;

/**
 * Function Call Node
 * 函数调用节点
 *
 * <p>Represents function calls: upper(str), max(a, b)</p>
 * <p>表示函数调用：upper(str), max(a, b)</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invoke registered functions by name - 按名称调用已注册的函数</li>
 *   <li>Support variable number of arguments - 支持可变数量参数</li>
 *   <li>Function lookup via context's FunctionRegistry - 通过上下文的FunctionRegistry查找函数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node arg = LiteralNode.ofString("hello");
 * Node call = FunctionCallNode.of("upper", arg);
 * Object result = call.evaluate(ctx);  // "HELLO"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record with defensive copy of arguments - 线程安全: 是，不可变记录，参数进行防御性拷贝</li>
 *   <li>Null-safe: No, function name required non-null - 空值安全: 否，函数名要求非空</li>
 * </ul>
 *
 * @param functionName the function name | 函数名
 * @param arguments the function arguments | 函数参数
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record FunctionCallNode(String functionName, List<Node> arguments) implements Node {

    public FunctionCallNode {
        Objects.requireNonNull(functionName, "functionName cannot be null");
        arguments = arguments != null ? List.copyOf(arguments) : List.of();
    }

    /**
     * Create function call node
     * 创建函数调用节点
     *
     * @param functionName the function name | 函数名
     * @param arguments the arguments | 参数
     * @return the function call node | 函数调用节点
     */
    public static FunctionCallNode of(String functionName, List<Node> arguments) {
        return new FunctionCallNode(functionName, arguments);
    }

    /**
     * Create function call node with single argument
     * 创建单参数函数调用节点
     *
     * @param functionName the function name | 函数名
     * @param argument the argument | 参数
     * @return the function call node | 函数调用节点
     */
    public static FunctionCallNode of(String functionName, Node argument) {
        return new FunctionCallNode(functionName, List.of(argument));
    }

    @Override
    public Object evaluate(EvaluationContext context) {
        // Get function from registry
        Function function = context.getFunctionRegistry().get(functionName);
        if (function == null) {
            throw OpenExpressionException.functionNotFound(functionName);
        }

        // Evaluate arguments
        Object[] args = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = arguments.get(i).evaluate(context);
        }

        // Invoke function
        try {
            return function.apply(args);
        } catch (OpenExpressionException e) {
            throw e;
        } catch (Exception e) {
            throw OpenExpressionException.evaluationError(
                "Function " + functionName + " failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String toExpressionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(functionName);
        sb.append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i).toExpressionString());
        }
        sb.append(")");
        return sb.toString();
    }
}
