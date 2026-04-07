package cloud.opencode.base.expression;

import cloud.opencode.base.expression.ast.BetweenNode;
import cloud.opencode.base.expression.ast.BinaryOpNode;
import cloud.opencode.base.expression.ast.BitwiseOpNode;
import cloud.opencode.base.expression.ast.CollectionFilterNode;
import cloud.opencode.base.expression.ast.CollectionProjectNode;
import cloud.opencode.base.expression.ast.ElvisNode;
import cloud.opencode.base.expression.ast.FunctionCallNode;
import cloud.opencode.base.expression.ast.IdentifierNode;
import cloud.opencode.base.expression.ast.InNode;
import cloud.opencode.base.expression.ast.IndexAccessNode;
import cloud.opencode.base.expression.ast.LambdaNode;
import cloud.opencode.base.expression.ast.ListLiteralNode;
import cloud.opencode.base.expression.ast.LiteralNode;
import cloud.opencode.base.expression.ast.MapLiteralNode;
import cloud.opencode.base.expression.ast.MethodCallNode;
import cloud.opencode.base.expression.ast.PropertyAccessNode;
import cloud.opencode.base.expression.ast.StringInterpolationNode;
import cloud.opencode.base.expression.ast.TernaryOpNode;
import cloud.opencode.base.expression.ast.UnaryOpNode;

/**
 * Expression Visitor Interface
 * 表达式访问者接口
 *
 * <p>Generic visitor interface for traversing expression Abstract Syntax Trees (ASTs).
 * Implements the Visitor design pattern to enable operations over the AST without
 * modifying the node classes.</p>
 * <p>用于遍历表达式抽象语法树（AST）的泛型访问者接口。
 * 实现访问者设计模式，允许在不修改节点类的情况下对AST执行操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe visitor pattern with generic return type - 带泛型返回类型的类型安全访问者模式</li>
 *   <li>Complete coverage of all AST node types - 覆盖所有AST节点类型</li>
 *   <li>Supports core nodes (literal, identifier, binary, unary, ternary) - 支持核心节点（字面量、标识符、二元、一元、三元）</li>
 *   <li>Supports access nodes (property, index, method, function) - 支持访问节点（属性、索引、方法、函数）</li>
 *   <li>Supports collection nodes (filter, project, list literal) - 支持集合节点（过滤、投影、列表字面量）</li>
 *   <li>Supports V1.0.3 nodes (elvis, in, between, bitwise, lambda, map literal, string interpolation) -
 *       支持V1.0.3节点（elvis、in、between、位运算、lambda、映射字面量、字符串插值）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a visitor that converts AST to string representation
 * ExpressionVisitor<String> printer = new ExpressionVisitor<>() {
 *     @Override
 *     public String visit(LiteralNode node) {
 *         return String.valueOf(node.value());
 *     }
 *
 *     @Override
 *     public String visit(BinaryOpNode node) {
 *         return "(" + visit(node.left()) + " " + node.operator() + " " + visit(node.right()) + ")";
 *     }
 *
 *     // ... implement other visit methods
 * };
 *
 * // Create a visitor that collects all function names
 * ExpressionVisitor<Set<String>> functionCollector = new ExpressionVisitor<>() {
 *     @Override
 *     public Set<String> visit(FunctionCallNode node) {
 *         Set<String> names = new HashSet<>();
 *         names.add(node.functionName());
 *         for (Node arg : node.arguments()) {
 *             names.addAll(arg.accept(this));
 *         }
 *         return names;
 *     }
 *     // ... implement other visit methods
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Implementations should guard against null nodes - 空值安全: 实现应防御null节点</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>O(n) traversal where n is the number of nodes in the AST - O(n)遍历，n为AST中的节点数</li>
 *   <li>No additional memory allocation beyond implementation-specific state - 除实现特定状态外无额外内存分配</li>
 * </ul>
 *
 * @param <T> the return type of the visit operations | 访问操作的返回类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public interface ExpressionVisitor<T> {

    // ==================== Core Nodes | 核心节点 ====================

    /**
     * Visit a literal node
     * 访问字面量节点
     *
     * @param node the literal node | 字面量节点
     * @return the visit result | 访问结果
     */
    T visit(LiteralNode node);

    /**
     * Visit an identifier node
     * 访问标识符节点
     *
     * @param node the identifier node | 标识符节点
     * @return the visit result | 访问结果
     */
    T visit(IdentifierNode node);

    /**
     * Visit a binary operation node
     * 访问二元运算节点
     *
     * @param node the binary operation node | 二元运算节点
     * @return the visit result | 访问结果
     */
    T visit(BinaryOpNode node);

    /**
     * Visit a unary operation node
     * 访问一元运算节点
     *
     * @param node the unary operation node | 一元运算节点
     * @return the visit result | 访问结果
     */
    T visit(UnaryOpNode node);

    /**
     * Visit a ternary operation node
     * 访问三元运算节点
     *
     * @param node the ternary operation node | 三元运算节点
     * @return the visit result | 访问结果
     */
    T visit(TernaryOpNode node);

    // ==================== Access Nodes | 访问节点 ====================

    /**
     * Visit a property access node
     * 访问属性访问节点
     *
     * @param node the property access node | 属性访问节点
     * @return the visit result | 访问结果
     */
    T visit(PropertyAccessNode node);

    /**
     * Visit an index access node
     * 访问索引访问节点
     *
     * @param node the index access node | 索引访问节点
     * @return the visit result | 访问结果
     */
    T visit(IndexAccessNode node);

    /**
     * Visit a method call node
     * 访问方法调用节点
     *
     * @param node the method call node | 方法调用节点
     * @return the visit result | 访问结果
     */
    T visit(MethodCallNode node);

    /**
     * Visit a function call node
     * 访问函数调用节点
     *
     * @param node the function call node | 函数调用节点
     * @return the visit result | 访问结果
     */
    T visit(FunctionCallNode node);

    // ==================== Collection Nodes | 集合节点 ====================

    /**
     * Visit a collection filter node
     * 访问集合过滤节点
     *
     * @param node the collection filter node | 集合过滤节点
     * @return the visit result | 访问结果
     */
    T visit(CollectionFilterNode node);

    /**
     * Visit a collection project node
     * 访问集合投影节点
     *
     * @param node the collection project node | 集合投影节点
     * @return the visit result | 访问结果
     */
    T visit(CollectionProjectNode node);

    /**
     * Visit a list literal node
     * 访问列表字面量节点
     *
     * @param node the list literal node | 列表字面量节点
     * @return the visit result | 访问结果
     */
    T visit(ListLiteralNode node);

    // ==================== V1.0.3 Nodes | V1.0.3 新增节点 ====================

    /**
     * Visit an elvis (null-coalescing) node
     * 访问Elvis（空值合并）节点
     *
     * @param node the elvis node | Elvis节点
     * @return the visit result | 访问结果
     */
    T visit(ElvisNode node);

    /**
     * Visit an {@code in} membership test node
     * 访问 {@code in} 成员测试节点
     *
     * @param node the in node | in节点
     * @return the visit result | 访问结果
     */
    T visit(InNode node);

    /**
     * Visit a {@code between} range test node
     * 访问 {@code between} 范围测试节点
     *
     * @param node the between node | between节点
     * @return the visit result | 访问结果
     */
    T visit(BetweenNode node);

    /**
     * Visit a bitwise operation node
     * 访问位运算节点
     *
     * @param node the bitwise operation node | 位运算节点
     * @return the visit result | 访问结果
     */
    T visit(BitwiseOpNode node);

    /**
     * Visit a lambda expression node
     * 访问Lambda表达式节点
     *
     * @param node the lambda node | Lambda节点
     * @return the visit result | 访问结果
     */
    T visit(LambdaNode node);

    /**
     * Visit a map literal node
     * 访问映射字面量节点
     *
     * @param node the map literal node | 映射字面量节点
     * @return the visit result | 访问结果
     */
    T visit(MapLiteralNode node);

    /**
     * Visit a string interpolation node
     * 访问字符串插值节点
     *
     * @param node the string interpolation node | 字符串插值节点
     * @return the visit result | 访问结果
     */
    T visit(StringInterpolationNode node);
}
