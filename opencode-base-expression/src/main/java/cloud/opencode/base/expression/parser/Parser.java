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
 *   <li>Support for ternary, elvis, logical, comparison, arithmetic, power operators - 支持三元、Elvis、逻辑、比较、算术、幂运算符</li>
 *   <li>Bitwise operators: {@code &, |, ^, ~, <<, >>} - 位运算符</li>
 *   <li>In and between operators for membership and range tests - in和between运算符用于成员和范围测试</li>
 *   <li>Lambda expressions: {@code x -> expr} - Lambda表达式</li>
 *   <li>Map literals: {@code #{key: value}} - Map字面量</li>
 *   <li>String interpolation: {@code "text ${expr}"} - 字符串插值</li>
 *   <li>Property access, method calls, index access - 属性访问、方法调用、索引访问</li>
 *   <li>Collection filter (.?[]) and projection (.![]) - 集合过滤和投影</li>
 *   <li>Null-safe navigation (?.) - 空安全导航</li>
 *   <li>Maximum nesting depth limit (200) - 最大嵌套深度限制（200）</li>
 * </ul>
 *
 * <p><strong>Operator Precedence (low to high) | 运算符优先级（低到高）:</strong></p>
 * <ol>
 *   <li>Ternary: {@code ? :}</li>
 *   <li>Elvis: {@code ?:}</li>
 *   <li>Logical OR: {@code ||, or}</li>
 *   <li>Logical AND: {@code &&, and}</li>
 *   <li>Bitwise OR: {@code |}</li>
 *   <li>Bitwise XOR: {@code ^}</li>
 *   <li>Bitwise AND: {@code &}</li>
 *   <li>Equality: {@code ==, !=, matches}</li>
 *   <li>Relational: {@code <, <=, >, >=, in, between, instanceof}</li>
 *   <li>Shift: {@code <<, >>}</li>
 *   <li>Additive: {@code +, -}</li>
 *   <li>Multiplicative: {@code *, /, %}</li>
 *   <li>Power: {@code **}</li>
 *   <li>Unary: {@code !, -, +, ~}</li>
 *   <li>Postfix: {@code ., ?., [], .?[], .![]}</li>
 *   <li>Primary: literals, identifiers, functions, lambdas, map literals</li>
 * </ol>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Node ast = Parser.parse("price * (1 - discount)");
 * Object result = ast.evaluate(ctx);
 *
 * // Elvis operator
 * Node elvis = Parser.parse("name ?: 'default'");
 *
 * // In operator
 * Node in = Parser.parse("x in {1, 2, 3}");
 *
 * // Between operator
 * Node between = Parser.parse("age between 18 and 65");
 *
 * // Lambda
 * Node lambda = Parser.parse("filter(list, x -> x > 3)");
 *
 * // Map literal
 * Node map = Parser.parse("#{name: 'Jon', age: 30}");
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

    // ==================== Precedence levels (low to high) ====================

    private Node parseTernary() {
        Node condition = parseElvis();

        if (match(TokenType.QUESTION)) {
            Node trueExpr = parseExpression();
            consume(TokenType.COLON, "Expected ':' in ternary expression");
            Node falseExpr = parseExpression();
            return TernaryOpNode.of(condition, trueExpr, falseExpr);
        }

        return condition;
    }

    private Node parseElvis() {
        Node left = parseOr();

        if (match(TokenType.ELVIS)) {
            Node right = parseElvis(); // right-associative
            return ElvisNode.of(left, right);
        }

        return left;
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
        Node left = parseBitwiseOr();

        while (match(TokenType.AND)) {
            Node right = parseBitwiseOr();
            left = BinaryOpNode.of(left, "&&", right);
        }

        return left;
    }

    private Node parseBitwiseOr() {
        Node left = parseBitwiseXor();

        while (match(TokenType.BIT_OR)) {
            Node right = parseBitwiseXor();
            left = BitwiseOpNode.of(left, "|", right);
        }

        return left;
    }

    private Node parseBitwiseXor() {
        Node left = parseBitwiseAnd();

        while (match(TokenType.BIT_XOR)) {
            Node right = parseBitwiseAnd();
            left = BitwiseOpNode.of(left, "^", right);
        }

        return left;
    }

    private Node parseBitwiseAnd() {
        Node left = parseEquality();

        while (match(TokenType.BIT_AND)) {
            Node right = parseEquality();
            left = BitwiseOpNode.of(left, "&", right);
        }

        return left;
    }

    private Node parseEquality() {
        Node left = parseRelational();

        while (true) {
            if (match(TokenType.EQ)) {
                Node right = parseRelational();
                left = BinaryOpNode.of(left, "==", right);
            } else if (match(TokenType.NE)) {
                Node right = parseRelational();
                left = BinaryOpNode.of(left, "!=", right);
            } else if (match(TokenType.MATCHES)) {
                Node right = parseRelational();
                left = BinaryOpNode.of(left, "matches", right);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parseRelational() {
        Node left = parseShift();

        while (true) {
            if (match(TokenType.LT)) {
                Node right = parseShift();
                left = BinaryOpNode.of(left, "<", right);
            } else if (match(TokenType.LE)) {
                Node right = parseShift();
                left = BinaryOpNode.of(left, "<=", right);
            } else if (match(TokenType.GT)) {
                Node right = parseShift();
                left = BinaryOpNode.of(left, ">", right);
            } else if (match(TokenType.GE)) {
                Node right = parseShift();
                left = BinaryOpNode.of(left, ">=", right);
            } else if (match(TokenType.INSTANCEOF)) {
                Node right = parseShift();
                left = BinaryOpNode.of(left, "instanceof", right);
            } else if (match(TokenType.IN)) {
                Node right = parseShift();
                left = InNode.of(left, right);
            } else if (match(TokenType.BETWEEN)) {
                Node lower = parseShift();
                consumeAndKeyword("Expected 'and' after 'between' lower bound");
                Node upper = parseShift();
                left = BetweenNode.of(left, lower, upper);
            } else {
                break;
            }
        }

        return left;
    }

    private Node parseShift() {
        Node left = parseAdditive();

        while (true) {
            if (match(TokenType.LSHIFT)) {
                Node right = parseAdditive();
                left = BitwiseOpNode.of(left, "<<", right);
            } else if (match(TokenType.RSHIFT)) {
                Node right = parseAdditive();
                left = BitwiseOpNode.of(left, ">>", right);
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

        // Power is right-associative: 2**3**2 = 2**(3**2) = 512
        if (match(TokenType.POWER)) {
            enterDepth();
            try {
                Node right = parsePower();
                left = BinaryOpNode.of(left, "**", right);
            } finally { depth--; }
        }

        return left;
    }

    private Node parseUnary() {
        if (match(TokenType.NOT)) {
            enterDepth();
            try {
                Node operand = parseUnary();
                return UnaryOpNode.of("!", operand);
            } finally { depth--; }
        }
        if (match(TokenType.MINUS)) {
            enterDepth();
            try {
                Node operand = parseUnary();
                return UnaryOpNode.of("-", operand);
            } finally { depth--; }
        }
        if (match(TokenType.PLUS)) {
            enterDepth();
            try {
                Node operand = parseUnary();
                return UnaryOpNode.of("+", operand);
            } finally { depth--; }
        }
        if (match(TokenType.BIT_NOT)) {
            enterDepth();
            try {
                Node operand = parseUnary();
                return BitwiseOpNode.ofNot(operand);
            } finally { depth--; }
        }
        return parsePostfix();
    }

    private void enterDepth() {
        if (++depth > MAX_DEPTH) {
            depth--;
            throw OpenExpressionException.parseError(
                "Maximum expression nesting depth (" + MAX_DEPTH + ") exceeded", current().position());
        }
    }

    private Node parsePostfix() {
        Node node = parsePrimary();

        while (true) {
            if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
                if (match(TokenType.LPAREN)) {
                    List<Node> args = parseArguments();
                    consume(TokenType.RPAREN, "Expected ')' after method arguments");
                    node = MethodCallNode.of(node, name.stringValue(), args);
                } else {
                    node = PropertyAccessNode.of(node, name.stringValue(), false);
                }
            } else if (match(TokenType.SAFE_NAV)) {
                Token name = consume(TokenType.IDENTIFIER, "Expected property name after '?.'");
                if (match(TokenType.LPAREN)) {
                    List<Node> args = parseArguments();
                    consume(TokenType.RPAREN, "Expected ')' after method arguments");
                    node = MethodCallNode.of(node, name.stringValue(), args, true);
                } else {
                    node = PropertyAccessNode.of(node, name.stringValue(), true);
                }
            } else if (match(TokenType.LBRACKET)) {
                Node index = parseExpression();
                consume(TokenType.RBRACKET, "Expected ']' after index");
                node = IndexAccessNode.of(node, index);
            } else if (match(TokenType.FILTER)) {
                Node predicate = parseExpression();
                consume(TokenType.RBRACKET, "Expected ']' after filter predicate");
                node = CollectionFilterNode.of(node, predicate);
            } else if (match(TokenType.PROJECT)) {
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

        // Map literal #{key: value, ...}
        if (match(TokenType.MAP_LBRACE)) {
            return parseMapLiteral();
        }

        // Variables with #
        if (match(TokenType.HASH)) {
            Token name = consume(TokenType.IDENTIFIER, "Expected variable name after '#'");
            return IdentifierNode.of("#" + name.stringValue());
        }

        // Identifiers (can be variable, function call, or lambda parameter)
        if (match(TokenType.IDENTIFIER)) {
            String name = previous().stringValue();

            // Check for lambda: identifier -> expr
            if (match(TokenType.ARROW)) {
                Node body = parseExpression();
                return LambdaNode.of(name, body);
            }

            if (match(TokenType.LPAREN)) {
                // Function call
                List<Node> args = parseArguments();
                consume(TokenType.RPAREN, "Expected ')' after function arguments");
                return FunctionCallNode.of(name, args);
            }
            return IdentifierNode.of(name);
        }

        // Parenthesized expression or lambda with parens
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

    private Node parseMapLiteral() {
        List<Map.Entry<Node, Node>> entries = new ArrayList<>();
        if (!check(TokenType.RBRACE)) {
            do {
                Node key = parseExpression();
                consume(TokenType.COLON, "Expected ':' after map key");
                Node value = parseExpression();
                entries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RBRACE, "Expected '}' after map entries");
        return MapLiteralNode.of(entries);
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


    // ==================== Helper methods ====================

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

    /**
     * Consume the 'and' keyword separator used in 'between...and' syntax.
     * Accepts: IDENTIFIER with value "and" (case-insensitive), or AND token
     * produced from the keyword 'and' (length >= 3, distinguishing from '&&' which has length 2).
     */
    private void consumeAndKeyword(String message) {
        if (!isAtEnd()) {
            Token cur = current();
            // Accept 'and' as an IDENTIFIER (should not happen since 'and' is tokenized as AND, but for safety)
            if (cur.type() == TokenType.IDENTIFIER && "and".equalsIgnoreCase(cur.stringValue())) {
                advance();
                return;
            }
            // Accept AND token only when produced from the word "and" (length >= 3), not "&&" (length 2)
            if (cur.type() == TokenType.AND && cur.length() >= 3) {
                advance();
                return;
            }
        }
        throw OpenExpressionException.parseError(message, current().position());
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
