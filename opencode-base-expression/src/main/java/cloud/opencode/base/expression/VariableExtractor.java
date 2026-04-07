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
import cloud.opencode.base.expression.ast.Node;
import cloud.opencode.base.expression.ast.PropertyAccessNode;
import cloud.opencode.base.expression.ast.StringInterpolationNode;
import cloud.opencode.base.expression.ast.TernaryOpNode;
import cloud.opencode.base.expression.ast.UnaryOpNode;
import cloud.opencode.base.expression.parser.Parser;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Variable Extractor
 * 变量提取器
 *
 * <p>Utility class to extract variable names referenced in an expression.
 * Parses the expression into an AST and recursively walks all nodes
 * to collect {@link IdentifierNode} names.</p>
 * <p>用于提取表达式中引用的变量名的工具类。
 * 将表达式解析为AST并递归遍历所有节点以收集 {@link IdentifierNode} 名称。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract variables from expression string or pre-parsed AST node - 从表达式字符串或预解析的AST节点提取变量</li>
 *   <li>Recursive traversal with sealed interface pattern matching - 使用密封接口模式匹配的递归遍历</li>
 *   <li>Excludes special identifiers (#root, #this, root, this) - 排除特殊标识符（#root、#this、root、this）</li>
 *   <li>Returns insertion-ordered, unmodifiable Set - 返回插入有序的不可修改Set</li>
 *   <li>Handles all node types including V1.0.3 additions - 处理所有节点类型，包括V1.0.3新增节点</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract from expression string
 * Set<String> vars = VariableExtractor.extract("x + y * 2");
 * // vars = ["x", "y"]
 *
 * // Extract from complex expression
 * Set<String> vars = VariableExtractor.extract("user.name == 'John' && age > 18");
 * // vars = ["user", "age"]
 *
 * // Extract from pre-parsed AST node
 * Node ast = Parser.parse("a + b");
 * Set<String> vars = VariableExtractor.extract(ast);
 * // vars = ["a", "b"]
 *
 * // Special identifiers are excluded
 * Set<String> vars = VariableExtractor.extract("#root.name + #this");
 * // vars = [] (special identifiers excluded)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility with no shared mutable state - 线程安全: 是，无共享可变状态的无状态工具</li>
 *   <li>Null-safe: No, null expressions throw NullPointerException - 空值安全: 否，null表达式抛出NullPointerException</li>
 * </ul>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>O(n) traversal where n is the number of AST nodes - O(n)遍历，n为AST节点数</li>
 *   <li>Uses LinkedHashSet for deterministic insertion order - 使用LinkedHashSet保证确定的插入顺序</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.3
 */
public final class VariableExtractor {

    /** Special identifiers that are not user-defined variables. */
    private static final Set<String> SPECIAL_IDENTIFIERS = Set.of(
            "#root", "#this", "root", "this",
            "true", "false", "null"
    );

    private VariableExtractor() {
    }

    /**
     * Extract variable names from an expression string
     * 从表达式字符串中提取变量名
     *
     * <p>Parses the expression and walks the resulting AST to collect
     * all identifier names that represent user-defined variables.</p>
     * <p>解析表达式并遍历生成的AST，收集所有代表用户定义变量的标识符名称。</p>
     *
     * @param expression the expression string | 表达式字符串
     * @return unmodifiable set of variable names in insertion order | 按插入顺序排列的不可修改变量名集合
     * @throws NullPointerException if expression is null | 如果表达式为null
     * @throws OpenExpressionException if expression cannot be parsed | 如果表达式无法解析
     */
    public static Set<String> extract(String expression) {
        Objects.requireNonNull(expression, "expression cannot be null");
        Node ast = Parser.parse(expression);
        return extract(ast);
    }

    /**
     * Extract variable names from a pre-parsed AST node
     * 从预解析的AST节点中提取变量名
     *
     * <p>Recursively walks the AST to collect all identifier names
     * that represent user-defined variables (excluding special identifiers
     * such as {@code #root}, {@code #this}, {@code true}, {@code false}, {@code null}).</p>
     * <p>递归遍历AST，收集所有代表用户定义变量的标识符名称
     * （排除特殊标识符如 {@code #root}、{@code #this}、{@code true}、{@code false}、{@code null}）。</p>
     *
     * @param node the AST node to traverse | 要遍历的AST节点
     * @return unmodifiable set of variable names in insertion order | 按插入顺序排列的不可修改变量名集合
     * @throws NullPointerException if node is null | 如果节点为null
     */
    public static Set<String> extract(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        LinkedHashSet<String> variables = new LinkedHashSet<>();
        collectVariables(node, variables);
        return Collections.unmodifiableSet(variables);
    }

    /**
     * Recursively collect variable names from an AST node
     * 从AST节点递归收集变量名
     *
     * @param node the current node | 当前节点
     * @param variables the accumulator set | 累积集合
     */
    private static void collectVariables(Node node, Set<String> variables) {
        switch (node) {
            case LiteralNode _ -> {
                // Literals contain no variables
            }

            case IdentifierNode id -> {
                String name = id.name();
                if (!SPECIAL_IDENTIFIERS.contains(name)) {
                    variables.add(name);
                }
            }

            case BinaryOpNode binary -> {
                collectVariables(binary.left(), variables);
                collectVariables(binary.right(), variables);
            }

            case UnaryOpNode unary -> collectVariables(unary.operand(), variables);

            case TernaryOpNode ternary -> {
                collectVariables(ternary.condition(), variables);
                collectVariables(ternary.trueValue(), variables);
                collectVariables(ternary.falseValue(), variables);
            }

            case PropertyAccessNode prop -> collectVariables(prop.target(), variables);

            case IndexAccessNode idx -> {
                collectVariables(idx.target(), variables);
                collectVariables(idx.index(), variables);
            }

            case MethodCallNode method -> {
                collectVariables(method.target(), variables);
                for (Node arg : method.arguments()) {
                    collectVariables(arg, variables);
                }
            }

            case FunctionCallNode func -> {
                // Function names are not variables; only arguments are
                for (Node arg : func.arguments()) {
                    collectVariables(arg, variables);
                }
            }

            case CollectionFilterNode filter -> {
                collectVariables(filter.target(), variables);
                collectVariables(filter.predicate(), variables);
            }

            case CollectionProjectNode project -> {
                collectVariables(project.target(), variables);
                collectVariables(project.projection(), variables);
            }

            case ListLiteralNode list -> {
                for (Node element : list.elements()) {
                    collectVariables(element, variables);
                }
            }

            case ElvisNode elvis -> {
                collectVariables(elvis.value(), variables);
                collectVariables(elvis.defaultValue(), variables);
            }

            case InNode in -> {
                collectVariables(in.value(), variables);
                collectVariables(in.collection(), variables);
            }

            case BetweenNode between -> {
                collectVariables(between.value(), variables);
                collectVariables(between.lower(), variables);
                collectVariables(between.upper(), variables);
            }

            case BitwiseOpNode bitwise -> {
                collectVariables(bitwise.left(), variables);
                if (bitwise.right() != null) {
                    collectVariables(bitwise.right(), variables);
                }
            }

            case LambdaNode lambda -> {
                // Lambda parameters introduce local bindings; collect body variables separately
                // to avoid removing outer-scope references to the same name
                LinkedHashSet<String> bodyVars = new LinkedHashSet<>();
                collectVariables(lambda.body(), bodyVars);
                bodyVars.remove(lambda.parameter());
                variables.addAll(bodyVars);
            }

            case MapLiteralNode map -> {
                for (var entry : map.entries()) {
                    collectVariables(entry.getKey(), variables);
                    collectVariables(entry.getValue(), variables);
                }
            }

            case StringInterpolationNode interp -> {
                for (Node part : interp.parts()) {
                    collectVariables(part, variables);
                }
            }
        }
    }
}
