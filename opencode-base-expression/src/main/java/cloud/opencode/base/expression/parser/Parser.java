package cloud.opencode.base.expression.parser;

import cloud.opencode.base.expression.OpenExpressionException;
import cloud.opencode.base.expression.ast.*;

import java.util.*;

/**
 * Expression Parser
 * 表达式解析器
 *
 * <p>Parses token lists into abstract syntax trees.</p>
 * <p>将词法单元列表解析为抽象语法树。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Recursive descent parsing with operator precedence - 带运算符优先级的递归下降解析</li>
 *   <li>Support for ternary, logical, comparison, arithmetic, power operators - 支持三元、逻辑、比较、算术、幂运算符</li>
 *   <li>Property access, method calls, index access - 属性访问、方法调用、索引访问</li>
 *   <li>Collection filter (.?[]) and projection (.![]) - 集合过滤和投影</li>
 *   <li>Null-safe navigation (?.) - 空安全导航</li>
 *   <li>Maximum nesting depth limit (200) - 最大嵌套深度限制（200）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node ast = Parser.parse("price * (1 - discount)");
 * Object result = ast.evaluate(ctx);
 *
 * // Or step by step
 * List<Token> tokens = Tokenizer.tokenize("a + b");
 * Parser parser = new Parser(tokens);
 * Node node = parser.parse();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, stateful parser instance - 线程安全: 否，有状态的解析器实例</li>
 *   <li>Null-safe: No, null expression not supported - 空值安全: 否，不支持null表达式</li>
 *   <li>Depth-limited to prevent stack overflow attacks - 深度限制以防止栈溢出攻击</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for parse where n is the number of tokens - 时间复杂度: parse 为 O(n)，n为词法单元数量</li>
 *   <li>Space complexity: O(n) for the resulting AST - 空间复杂度: O(n)，存储生成的 AST</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class Parser {

    private static final int MAX_DEPTH = 200;

    private final List<Token> tokens;
    private int pos;
    private int depth;

    /**
     * Create parser with tokens
     * 使用词法单元创建解析器
     *
     * @param tokens the token list | 词法单元列表
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.depth = 0;
    }

    /**
     * Parse tokens into AST
     * 将词法单元解析为AST
     *
     * @return the AST root node | AST根节点
     */
    public Node parse() {
        Node node = parseExpression();
        if (!isAtEnd()) {
            throw OpenExpressionException.parseError("Unexpected token: " + current(), current().position());
        }
        return node;
    }

    private Node parseExpression() {
        if (++depth > MAX_DEPTH) {
            depth--;
            throw OpenExpressionException.parseError(
                "Maximum expression nesting depth (" + MAX_DEPTH + ") exceeded", current().position());
        }
        try {
            return parseTernary();
        } finally {
            depth--;
        }
    }

    private Node parseTernary() {
        Node condition = parseOr();

        if (match(TokenType.QUESTION)) {
            Node trueExpr = parseExpression();
            consume(TokenType.COLON, "Expected ':' in ternary expression");
            Node falseExpr = parseExpression();
            return TernaryOpNode.of(condition, trueExpr, falseExpr);
        }

        return condition;
    }

    private Node parseOr() {
        Node left = parseAnd();

        while (match(TokenType.OR)) {
            Node right = parseAnd();
            left = BinaryOpNode.of(left, "||", right);
        }

        return left;
    }

    private Node parseAnd() {
        Node left = parseEquality();

        while (match(TokenType.AND)) {
            Node right = parseEquality();
            left = BinaryOpNode.of(left, "&&", right);
        }

        return left;
    }

    private Node parseEquality() {
        Node left = parseComparison();

        while (true) {
            if (match(TokenType.EQ)) {
                Node right = parseComparison();
                left = BinaryOpNode.of(left, "==", right);
            } else if (match(TokenType.NE)) {
                Node right = parseComparison();
                left = BinaryOpNode.of(left, "!=", right);
            } else if (match(TokenType.MATCHES)) {
                Node right = parseComparison();
                left = BinaryOpNode.of(left, "matches", right);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parseComparison() {
        Node left = parseAdditive();

        while (true) {
            if (match(TokenType.LT)) {
                Node right = parseAdditive();
                left = BinaryOpNode.of(left, "<", right);
            } else if (match(TokenType.LE)) {
                Node right = parseAdditive();
                left = BinaryOpNode.of(left, "<=", right);
            } else if (match(TokenType.GT)) {
                Node right = parseAdditive();
                left = BinaryOpNode.of(left, ">", right);
            } else if (match(TokenType.GE)) {
                Node right = parseAdditive();
                left = BinaryOpNode.of(left, ">=", right);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parseAdditive() {
        Node left = parseMultiplicative();

        while (true) {
            if (match(TokenType.PLUS)) {
                Node right = parseMultiplicative();
                left = BinaryOpNode.of(left, "+", right);
            } else if (match(TokenType.MINUS)) {
                Node right = parseMultiplicative();
                left = BinaryOpNode.of(left, "-", right);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parseMultiplicative() {
        Node left = parsePower();

        while (true) {
            if (match(TokenType.STAR)) {
                Node right = parsePower();
                left = BinaryOpNode.of(left, "*", right);
            } else if (match(TokenType.SLASH)) {
                Node right = parsePower();
                left = BinaryOpNode.of(left, "/", right);
            } else if (match(TokenType.PERCENT)) {
                Node right = parsePower();
                left = BinaryOpNode.of(left, "%", right);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parsePower() {
        Node left = parseUnary();

        // Power is right-associative: 2**3**2 = 2**(3**2) = 512, not (2**3)**2 = 64
        if (match(TokenType.POWER)) {
            Node right = parsePower(); // Recurse to achieve right-associativity
            left = BinaryOpNode.of(left, "**", right);
        }

        return left;
    }

    private Node parseUnary() {
        if (match(TokenType.NOT)) {
            Node operand = parseUnary();
            return UnaryOpNode.of("!", operand);
        }
        if (match(TokenType.MINUS)) {
            Node operand = parseUnary();
            return UnaryOpNode.of("-", operand);
        }
        return parsePostfix();
    }

    private Node parsePostfix() {
        Node node = parsePrimary();

        while (true) {
            if (match(TokenType.DOT)) {
                // Property access or method call
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
                if (match(TokenType.LPAREN)) {
                    // Method call
                    List<Node> args = parseArguments();
                    consume(TokenType.RPAREN, "Expected ')' after method arguments");
                    node = MethodCallNode.of(node, name.stringValue(), args);
                } else {
                    // Property access
                    node = PropertyAccessNode.of(node, name.stringValue(), false);
                }
            } else if (match(TokenType.SAFE_NAV)) {
                // Null-safe property access or method call
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '?.'");
                if (match(TokenType.LPAREN)) {
                    // Method call with null-safe
                    List<Node> args = parseArguments();
                    consume(TokenType.RPAREN, "Expected ')' after method arguments");
                    node = MethodCallNode.of(node, name.stringValue(), args, true);
                } else {
                    // Null-safe property access
                    node = PropertyAccessNode.of(node, name.stringValue(), true);
                }
            } else if (match(TokenType.LBRACKET)) {
                // Index access
                Node index = parseExpression();
                consume(TokenType.RBRACKET, "Expected ']' after index");
                node = IndexAccessNode.of(node, index);
            } else if (match(TokenType.FILTER)) {
                // Collection filter: .?[predicate]
                Node predicate = parseExpression();
                consume(TokenType.RBRACKET, "Expected ']' after filter predicate");
                node = CollectionFilterNode.of(node, predicate);
            } else if (match(TokenType.PROJECT)) {
                // Collection projection: .![expression]
                Node projection = parseExpression();
                consume(TokenType.RBRACKET, "Expected ']' after projection expression");
                node = CollectionProjectNode.of(node, projection);
            } else {
                break;
            }
        }

        return node;
    }

    private Node parsePrimary() {
        // Literals
        if (match(TokenType.NUMBER)) {
            return LiteralNode.of(previous().value());
        }
        if (match(TokenType.STRING)) {
            return LiteralNode.of(previous().value());
        }
        if (match(TokenType.BOOLEAN_TRUE)) {
            return LiteralNode.of(true);
        }
        if (match(TokenType.BOOLEAN_FALSE)) {
            return LiteralNode.of(false);
        }
        if (match(TokenType.NULL)) {
            return LiteralNode.ofNull();
        }

        // Variables with #
        if (match(TokenType.HASH)) {
            Token name = consume(TokenType.IDENTIFIER, "Expected variable name after '#'");
            return IdentifierNode.of("#" + name.stringValue());
        }

        // Identifiers (can be variable or function call)
        if (match(TokenType.IDENTIFIER)) {
            String name = previous().stringValue();
            if (match(TokenType.LPAREN)) {
                // Function call
                List<Node> args = parseArguments();
                consume(TokenType.RPAREN, "Expected ')' after function arguments");
                return FunctionCallNode.of(name, args);
            }
            return IdentifierNode.of(name);
        }

        // Parenthesized expression
        if (match(TokenType.LPAREN)) {
            Node expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        // List literal
        if (match(TokenType.LBRACE)) {
            List<Node> elements = new ArrayList<>();
            if (!check(TokenType.RBRACE)) {
                do {
                    elements.add(parseExpression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RBRACE, "Expected '}' after list elements");
            return ListLiteralNode.of(elements);
        }

        throw OpenExpressionException.parseError("Unexpected token: " + current(), current().position());
    }

    private List<Node> parseArguments() {
        List<Node> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                args.add(parseExpression());
            } while (match(TokenType.COMMA));
        }
        return args;
    }

    // Helper methods

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && current().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) pos++;
        return previous();
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private boolean isAtEnd() {
        return current().type() == TokenType.EOF;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw OpenExpressionException.parseError(message, current().position());
    }

    /**
     * Parse expression string
     * 解析表达式字符串
     *
     * @param expression the expression | 表达式
     * @return the AST root node | AST根节点
     */
    public static Node parse(String expression) {
        List<Token> tokens = Tokenizer.tokenize(expression);
        return new Parser(tokens).parse();
    }
}
