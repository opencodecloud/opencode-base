package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.ast.*;
import cloud.opencode.base.expression.context.EvaluationContext;

import java.util.List;

/**
 * AST Evaluator
 * AST 求值器
 *
 * <p>The default evaluator that traverses and evaluates AST nodes.
 * Uses the visitor pattern to evaluate different node types.</p>
 * <p>默认求值器，遍历和求值 AST 节点。使用访问者模式来求值不同的节点类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Evaluate single nodes or node lists - 求值单个节点或节点列表</li>
 *   <li>Typed evaluation: boolean, number, string - 类型化求值: 布尔值、数字、字符串</li>
 *   <li>Timeout-based evaluation - 基于超时的求值</li>
 *   <li>Singleton and instance-based usage - 单例和基于实例的使用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AstEvaluator evaluator = AstEvaluator.getInstance();
 * Node ast = Parser.parse("x + y");
 * Object result = evaluator.evaluate(ast, ctx);
 *
 * // Typed evaluation
 * boolean flag = evaluator.evaluateAsBoolean(conditionNode, ctx);
 * Number num = evaluator.evaluateAsNumber(calcNode, ctx);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless singleton - 线程安全: 是，无状态单例</li>
 *   <li>Null-safe: Yes, null node returns null - 空值安全: 是，null节点返回null</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for evaluate where n is the number of AST nodes - 时间复杂度: evaluate 为 O(n)，n为 AST 节点数量</li>
 *   <li>Space complexity: O(d) call stack where d is the expression nesting depth - 空间复杂度: O(d) 调用栈，d为表达式嵌套深度</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class AstEvaluator implements Evaluator {

    private static final AstEvaluator INSTANCE = new AstEvaluator();

    /**
     * Get the singleton instance
     * 获取单例实例
     *
     * @return the instance | 实例
     */
    public static AstEvaluator getInstance() {
        return INSTANCE;
    }

    /**
     * Create a new AstEvaluator
     * 创建新的 AstEvaluator
     */
    public AstEvaluator() {
    }

    @Override
    public Object evaluate(Node node, EvaluationContext context) {
        if (node == null) {
            return null;
        }
        return node.evaluate(context);
    }

    /**
     * Evaluate a list of nodes
     * 求值节点列表
     *
     * @param nodes the nodes | 节点列表
     * @param context the context | 上下文
     * @return the results | 结果列表
     */
    public List<Object> evaluateAll(List<Node> nodes, EvaluationContext context) {
        return nodes.stream()
                .map(node -> evaluate(node, context))
                .toList();
    }

    /**
     * Evaluate node and expect boolean result
     * 求值节点并期望布尔结果
     *
     * @param node the node | 节点
     * @param context the context | 上下文
     * @return the boolean result | 布尔结果
     */
    public boolean evaluateAsBoolean(Node node, EvaluationContext context) {
        Object result = evaluate(node, context);
        return TypeCoercion.toBoolean(result);
    }

    /**
     * Evaluate node and expect number result
     * 求值节点并期望数字结果
     *
     * @param node the node | 节点
     * @param context the context | 上下文
     * @return the number result | 数字结果
     */
    public Number evaluateAsNumber(Node node, EvaluationContext context) {
        Object result = evaluate(node, context);
        if (result instanceof Number n) {
            return n;
        }
        if (result instanceof String s) {
            try {
                if (s.contains(".")) {
                    return Double.parseDouble(s);
                }
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw OpenExpressionException.typeError("number", result);
            }
        }
        throw OpenExpressionException.typeError("number", result);
    }

    /**
     * Evaluate node and expect string result
     * 求值节点并期望字符串结果
     *
     * @param node the node | 节点
     * @param context the context | 上下文
     * @return the string result | 字符串结果
     */
    public String evaluateAsString(Node node, EvaluationContext context) {
        Object result = evaluate(node, context);
        return result == null ? null : result.toString();
    }

    /**
     * Evaluate with timeout
     * 带超时求值
     *
     * @param node the node | 节点
     * @param context the context | 上下文
     * @param timeoutMs the timeout in milliseconds | 超时毫秒数
     * @return the result | 结果
     * @throws OpenExpressionException if timeout | 如果超时
     */
    public Object evaluateWithTimeout(Node node, EvaluationContext context, long timeoutMs) {
        if (timeoutMs <= 0) {
            return evaluate(node, context);
        }

        // Use a simple approach: check time before and after
        // For complex expressions, the nodes should check timeout themselves
        long startTime = System.currentTimeMillis();
        Object result = evaluate(node, context);
        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed > timeoutMs) {
            throw OpenExpressionException.timeout(timeoutMs);
        }

        return result;
    }

    /**
     * Static method to evaluate expression
     * 静态方法求值表达式
     *
     * @param node the node | 节点
     * @param context the context | 上下文
     * @return the result | 结果
     */
    public static Object eval(Node node, EvaluationContext context) {
        return INSTANCE.evaluate(node, context);
    }
}
