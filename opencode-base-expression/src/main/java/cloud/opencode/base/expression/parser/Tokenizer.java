package cloud.opencode.base.expression.parser;

import cloud.opencode.base.expression.OpenExpressionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Expression Tokenizer
 * 表达式词法分析器
 *
 * <p>Tokenizes expression strings into a list of tokens.</p>
 * <p>将表达式字符串分解为词法单元列表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String literals with escape sequences (\n, \t, \\, \', \") - 带转义序列的字符串字面量</li>
 *   <li>Number literals with decimal, scientific notation, and type suffixes (L, D, F) - 带小数、科学记数法和类型后缀的数字字面量</li>
 *   <li>Keyword recognition: true, false, null, and, or, not, matches, instanceof - 关键字识别</li>
 *   <li>Multi-character operators: ==, !=, &lt;=, &gt;=, **, &amp;&amp;, ||, ?., .?[, .![ - 多字符运算符</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Token> tokens = Tokenizer.tokenize("price * 1.1 + tax");
 * // tokens: [IDENTIFIER("price"), STAR, NUMBER(1.1), PLUS, IDENTIFIER("tax"), EOF]
 *
 * // Or instance-based
 * Tokenizer tokenizer = new Tokenizer("a + b");
 * List<Token> tokens = tokenizer.tokenize();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No, stateful tokenizer instance - 线程安全: 否，有状态的词法分析器实例</li>
 *   <li>Null-safe: Yes, null expression treated as empty string - 空值安全: 是，null表达式视为空字符串</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for tokenize where n is the expression length - 时间复杂度: tokenize 为 O(n)，n为表达式长度</li>
 *   <li>Space complexity: O(n) for the token list - 空间复杂度: O(n)，存储词法单元列表</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class Tokenizer {

    private final String expression;
    private int pos;
    private final int length;

    /**
     * Create tokenizer for expression
     * 为表达式创建词法分析器
     *
     * @param expression the expression | 表达式
     */
    public Tokenizer(String expression) {
        this.expression = expression != null ? expression : "";
        this.pos = 0;
        this.length = this.expression.length();
    }

    /**
     * Tokenize the expression
     * 对表达式进行词法分析
     *
     * @return the token list | 词法单元列表
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < length) {
            skipWhitespace();
            if (pos >= length) break;

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }

        tokens.add(Token.of(TokenType.EOF, pos));
        return tokens;
    }

    private void skipWhitespace() {
        while (pos < length && Character.isWhitespace(expression.charAt(pos))) {
            pos++;
        }
    }

    private Token nextToken() {
        char c = expression.charAt(pos);

        // String literals
        if (c == '\'' || c == '"') {
            return readString();
        }

        // Numbers
        if (Character.isDigit(c) || (c == '.' && pos + 1 < length && Character.isDigit(expression.charAt(pos + 1)))) {
            return readNumber();
        }

        // Identifiers and keywords
        if (Character.isLetter(c) || c == '_' || c == '$') {
            return readIdentifier();
        }

        // Hash for variables
        if (c == '#') {
            pos++;
            return Token.of(TokenType.HASH, pos - 1);
        }

        // Operators and punctuation
        return readOperator();
    }

    private Token readString() {
        int start = pos;
        char quote = expression.charAt(pos++);
        StringBuilder sb = new StringBuilder();

        while (pos < length) {
            char c = expression.charAt(pos++);
            if (c == quote) {
                return Token.of(TokenType.STRING, sb.toString(), start, pos - start);
            }
            if (c == '\\' && pos < length) {
                char escaped = expression.charAt(pos++);
                switch (escaped) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '\\' -> sb.append('\\');
                    case '\'' -> sb.append('\'');
                    case '"' -> sb.append('"');
                    default -> sb.append(escaped);
                }
            } else {
                sb.append(c);
            }
        }

        throw OpenExpressionException.parseError("Unterminated string literal", start);
    }

    private Token readNumber() {
        int start = pos;
        boolean isDouble = false;

        while (pos < length) {
            char c = expression.charAt(pos);
            if (Character.isDigit(c)) {
                pos++;
            } else if (c == '.' && !isDouble) {
                isDouble = true;
                pos++;
            } else if ((c == 'e' || c == 'E') && pos + 1 < length) {
                isDouble = true;
                pos++;
                if (expression.charAt(pos) == '+' || expression.charAt(pos) == '-') {
                    pos++;
                }
            } else if (c == 'L' || c == 'l') {
                pos++;
                String numStr = expression.substring(start, pos - 1);
                return Token.of(TokenType.NUMBER, Long.parseLong(numStr), start, pos - start);
            } else if (c == 'D' || c == 'd' || c == 'F' || c == 'f') {
                pos++;
                String numStr = expression.substring(start, pos - 1);
                return Token.of(TokenType.NUMBER, Double.parseDouble(numStr), start, pos - start);
            } else {
                break;
            }
        }

        String numStr = expression.substring(start, pos);
        Number value;
        if (isDouble) {
            value = Double.parseDouble(numStr);
        } else {
            try {
                value = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                value = Long.parseLong(numStr);
            }
        }
        return Token.of(TokenType.NUMBER, value, start, pos - start);
    }

    private Token readIdentifier() {
        int start = pos;
        while (pos < length) {
            char c = expression.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '$') {
                pos++;
            } else {
                break;
            }
        }

        String identifier = expression.substring(start, pos);
        TokenType type = switch (identifier.toLowerCase()) {
            case "true" -> TokenType.BOOLEAN_TRUE;
            case "false" -> TokenType.BOOLEAN_FALSE;
            case "null" -> TokenType.NULL;
            case "and" -> TokenType.AND;
            case "or" -> TokenType.OR;
            case "not" -> TokenType.NOT;
            case "matches" -> TokenType.MATCHES;
            case "instanceof" -> TokenType.INSTANCEOF;
            default -> TokenType.IDENTIFIER;
        };

        Object value = type == TokenType.IDENTIFIER ? identifier : null;
        if (type == TokenType.BOOLEAN_TRUE) value = true;
        if (type == TokenType.BOOLEAN_FALSE) value = false;

        return Token.of(type, value, start, pos - start);
    }

    private Token readOperator() {
        int start = pos;
        char c = expression.charAt(pos++);

        switch (c) {
            case '+' -> { return Token.of(TokenType.PLUS, start); }
            case '-' -> { return Token.of(TokenType.MINUS, start); }
            case '/' -> { return Token.of(TokenType.SLASH, start); }
            case '%' -> { return Token.of(TokenType.PERCENT, start); }
            case '(' -> { return Token.of(TokenType.LPAREN, start); }
            case ')' -> { return Token.of(TokenType.RPAREN, start); }
            case '[' -> { return Token.of(TokenType.LBRACKET, start); }
            case ']' -> { return Token.of(TokenType.RBRACKET, start); }
            case '{' -> { return Token.of(TokenType.LBRACE, start); }
            case '}' -> { return Token.of(TokenType.RBRACE, start); }
            case ',' -> { return Token.of(TokenType.COMMA, start); }
            case ':' -> { return Token.of(TokenType.COLON, start); }
            case '?' -> {
                if (pos < length && expression.charAt(pos) == '.') {
                    pos++;
                    return Token.of(TokenType.SAFE_NAV, start, 2);
                }
                if (pos < length && expression.charAt(pos) == '[') {
                    // This is .?[ but we already consumed '.', need to handle differently
                    return Token.of(TokenType.QUESTION, start);
                }
                return Token.of(TokenType.QUESTION, start);
            }
            case '*' -> {
                if (pos < length && expression.charAt(pos) == '*') {
                    pos++;
                    return Token.of(TokenType.POWER, start, 2);
                }
                return Token.of(TokenType.STAR, start);
            }
            case '=' -> {
                if (pos < length && expression.charAt(pos) == '=') {
                    pos++;
                    return Token.of(TokenType.EQ, start, 2);
                }
                return Token.of(TokenType.ASSIGN, start);
            }
            case '!' -> {
                if (pos < length && expression.charAt(pos) == '=') {
                    pos++;
                    return Token.of(TokenType.NE, start, 2);
                }
                if (pos < length && expression.charAt(pos) == '[') {
                    // This is .![ but we already consumed '.', need to handle differently
                    return Token.of(TokenType.NOT, start);
                }
                return Token.of(TokenType.NOT, start);
            }
            case '<' -> {
                if (pos < length && expression.charAt(pos) == '=') {
                    pos++;
                    return Token.of(TokenType.LE, start, 2);
                }
                return Token.of(TokenType.LT, start);
            }
            case '>' -> {
                if (pos < length && expression.charAt(pos) == '=') {
                    pos++;
                    return Token.of(TokenType.GE, start, 2);
                }
                return Token.of(TokenType.GT, start);
            }
            case '&' -> {
                if (pos < length && expression.charAt(pos) == '&') {
                    pos++;
                    return Token.of(TokenType.AND, start, 2);
                }
                throw OpenExpressionException.parseError("Unexpected character '&'", start);
            }
            case '|' -> {
                if (pos < length && expression.charAt(pos) == '|') {
                    pos++;
                    return Token.of(TokenType.OR, start, 2);
                }
                throw OpenExpressionException.parseError("Unexpected character '|'", start);
            }
            case '.' -> {
                // Check for collection operators
                if (pos < length) {
                    char next = expression.charAt(pos);
                    if (next == '?' && pos + 1 < length && expression.charAt(pos + 1) == '[') {
                        pos += 2;
                        return Token.of(TokenType.FILTER, start, 3);
                    }
                    if (next == '!' && pos + 1 < length && expression.charAt(pos + 1) == '[') {
                        pos += 2;
                        return Token.of(TokenType.PROJECT, start, 3);
                    }
                }
                return Token.of(TokenType.DOT, start);
            }
            default -> throw OpenExpressionException.parseError("Unexpected character '" + c + "'", start);
        }
    }

    /**
     * Create tokenizer and tokenize
     * 创建词法分析器并进行分析
     *
     * @param expression the expression | 表达式
     * @return the token list | 词法单元列表
     */
    public static List<Token> tokenize(String expression) {
        return new Tokenizer(expression).tokenize();
    }
}
