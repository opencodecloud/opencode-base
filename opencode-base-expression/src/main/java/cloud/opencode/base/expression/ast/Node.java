package cloud.opencode.base.expression.ast;

import cloud.opencode.base.expression.context.EvaluationContext;

/**
 * AST Node Interface
 * AST 节点接口
 *
 * <p>Base interface for all Abstract Syntax Tree nodes in the expression engine.
 * Each node represents a syntactic element that can be evaluated.</p>
 * <p>表达式引擎中所有抽象语法树节点的基础接口。每个节点代表一个可求值的语法元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with known implementations - 密封接口，已知实现类</li>
 *   <li>Context-based evaluation - 基于上下文的求值</li>
 *   <li>String representation for debugging - 用于调试的字符串表示</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node literal = LiteralNode.of(42);
 * Node binary = BinaryOpNode.of(literal, "+", LiteralNode.of(8));
 * Object result = binary.evaluate(new StandardContext());  // 50
 * String repr = binary.toExpressionString();  // "(42 + 8)"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, implementations are immutable records - 线程安全: 是，实现为不可变记录</li>
 *   <li>Null-safe: No, null context may cause exceptions - 空值安全: 否，null上下文可能导致异常</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public sealed interface Node permits
        LiteralNode,
        IdentifierNode,
        BinaryOpNode,
        UnaryOpNode,
        TernaryOpNode,
        PropertyAccessNode,
        IndexAccessNode,
        MethodCallNode,
        FunctionCallNode,
        CollectionFilterNode,
        CollectionProjectNode,
        ListLiteralNode,
        ElvisNode,
        InNode,
        BetweenNode,
        BitwiseOpNode,
        LambdaNode,
        MapLiteralNode,
        StringInterpolationNode {

    /**
     * Evaluate this node
     * 求值此节点
     *
     * @param context the evaluation context | 求值上下文
     * @return the evaluation result | 求值结果
     */
    Object evaluate(EvaluationContext context);

    /**
     * Get node type name
     * 获取节点类型名称
     *
     * @return the type name | 类型名称
     */
    default String getTypeName() {
        return getClass().getSimpleName().replace("Node", "");
    }

    /**
     * Get string representation for debugging
     * 获取用于调试的字符串表示
     *
     * @return the string representation | 字符串表示
     */
    String toExpressionString();
}
