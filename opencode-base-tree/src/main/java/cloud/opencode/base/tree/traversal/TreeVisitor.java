package cloud.opencode.base.tree.traversal;

/**
 * Tree Visitor
 * 树访问者
 *
 * <p>Visitor interface for tree traversal.</p>
 * <p>树遍历的访问者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional visitor interface for tree traversal - 树遍历的函数式访问者接口</li>
 *   <li>Depth-aware visiting - 深度感知的访问</li>
 *   <li>Early termination support - 支持提前终止</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple visitor
 * TreeVisitor<MyNode> visitor = TreeVisitor.of(node -> process(node));
 *
 * // Visitor with depth
 * TreeVisitor<MyNode> visitor = TreeVisitor.withDepth((node, depth) ->
 *     System.out.println("  ".repeat(depth) + node));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: No (node must not be null) - 否（节点不能为null）</li>
 * </ul>
 * @param <T> the node type | 节点类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-tree V1.0.0
 */
@FunctionalInterface
public interface TreeVisitor<T> {

    /**
     * Visit a node
     * 访问节点
     *
     * @param node the node to visit | 要访问的节点
     * @param depth the depth of the node | 节点深度
     * @return true to continue, false to stop | true继续，false停止
     */
    boolean visit(T node, int depth);

    /**
     * Create visitor that always continues
     * 创建始终继续的访问者
     *
     * @param action the action to perform | 要执行的动作
     * @param <T> the node type | 节点类型
     * @return the visitor | 访问者
     */
    static <T> TreeVisitor<T> of(java.util.function.Consumer<T> action) {
        return (node, depth) -> {
            action.accept(node);
            return true;
        };
    }

    /**
     * Create visitor with depth
     * 创建带深度的访问者
     *
     * @param action the action to perform | 要执行的动作
     * @param <T> the node type | 节点类型
     * @return the visitor | 访问者
     */
    static <T> TreeVisitor<T> withDepth(java.util.function.BiConsumer<T, Integer> action) {
        return (node, depth) -> {
            action.accept(node, depth);
            return true;
        };
    }
}
