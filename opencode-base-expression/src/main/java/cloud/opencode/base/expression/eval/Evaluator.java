package cloud.opencode.base.expression.eval;

import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * Evaluator Interface
 * 求值器接口
 *
 * <p>Base interface for expression evaluators that traverse and evaluate AST nodes.</p>
 * <p>表达式求值器的基础接口，用于遍历和求值 AST 节点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Evaluate AST nodes with context - 使用上下文求值AST节点</li>
 *   <li>Type-safe evaluation with automatic conversion - 类型安全求值与自动转换</li>
 *   <li>Capability check for node support - 节点支持能力检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Evaluator evaluator = AstEvaluator.getInstance();
 * Node node = Parser.parse("1 + 2");
 * Object result = evaluator.evaluate(node, ctx);
 * int typed = evaluator.evaluate(node, ctx, Integer.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per evaluate call where n is the number of AST nodes - 时间复杂度: evaluate 为 O(n)，n为 AST 节点数量</li>
 *   <li>Space complexity: O(d) call stack where d is the expression nesting depth - 空间复杂度: O(d) 调用栈，d为表达式嵌套深度</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public interface Evaluator {

    /**
     * Evaluate an AST node
     * 求值 AST 节点
     *
     * @param node the AST node | AST 节点
     * @param context the evaluation context | 求值上下文
     * @return the evaluation result | 求值结果
     */
    Object evaluate(Node node, EvaluationContext context);

    /**
     * Evaluate an AST node with type conversion
     * 求值 AST 节点并转换类型
     *
     * @param node the AST node | AST 节点
     * @param context the evaluation context | 求值上下文
     * @param targetType the target type | 目标类型
     * @param <T> the target type | 目标类型
     * @return the evaluation result | 求值结果
     */
    default <T> T evaluate(Node node, EvaluationContext context, Class<T> targetType) {
        Object result = evaluate(node, context);
        return TypeCoercion.convert(result, targetType);
    }

    /**
     * Check if this evaluator can evaluate the given node
     * 检查此求值器是否可以求值给定节点
     *
     * @param node the AST node | AST 节点
     * @return true if can evaluate | 如果可以求值返回 true
     */
    default boolean canEvaluate(Node node) {
        return true;
    }
}
