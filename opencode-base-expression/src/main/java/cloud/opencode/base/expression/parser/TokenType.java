package cloud.opencode.base.expression.parser;

/**
 * Token Type Enum
 * 词法单元类型枚举
 *
 * <p>Represents the types of tokens in expression parsing.</p>
 * <p>表示表达式解析中的词法单元类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Literal types: NUMBER, STRING, BOOLEAN, NULL - 字面量类型</li>
 *   <li>Operators: arithmetic, comparison, logical, assignment - 运算符: 算术、比较、逻辑、赋值</li>
 *   <li>Punctuation: parentheses, brackets, braces, dot, comma - 标点: 括号、方括号、花括号、点、逗号</li>
 *   <li>Special: SAFE_NAV, FILTER, PROJECT, MATCHES, INSTANCEOF - 特殊: 空安全导航、过滤、投影、匹配、instanceof</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Token token = Token.of(TokenType.NUMBER, 42, 0, 2);
 * if (token.is(TokenType.NUMBER)) {
 *     Number value = token.numberValue();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable enum - 线程安全: 是，不可变枚举</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public enum TokenType {

    // Literals
    NUMBER,
    STRING,
    BOOLEAN_TRUE,
    BOOLEAN_FALSE,
    NULL,

    // Identifiers
    IDENTIFIER,

    // Operators
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /
    PERCENT,        // %
    POWER,          // **

    // Comparison
    EQ,             // ==
    NE,             // !=
    LT,             // <
    LE,             // <=
    GT,             // >
    GE,             // >=

    // Logical
    AND,            // && or and
    OR,             // || or or
    NOT,            // ! or not

    // Assignment
    ASSIGN,         // =

    // Special operators
    MATCHES,        // matches
    INSTANCEOF,     // instanceof

    // Punctuation
    LPAREN,         // (
    RPAREN,         // )
    LBRACKET,       // [
    RBRACKET,       // ]
    LBRACE,         // {
    RBRACE,         // }
    DOT,            // .
    SAFE_NAV,       // ?.
    COMMA,          // ,
    COLON,          // :
    QUESTION,       // ?
    HASH,           // #

    // Collection operators
    PROJECT,        // .![
    FILTER,         // .?[

    // End of expression
    EOF
}
