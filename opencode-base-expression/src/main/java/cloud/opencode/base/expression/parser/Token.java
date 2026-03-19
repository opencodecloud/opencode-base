package cloud.opencode.base.expression.parser;

/**
 * Token Record
 * 词法单元记录
 *
 * <p>Represents a single token in expression parsing.</p>
 * <p>表示表达式解析中的单个词法单元。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable token with type, value, position, and length - 不可变词法单元，包含类型、值、位置和长度</li>
 *   <li>Factory methods for common token creation - 常见词法单元创建的工厂方法</li>
 *   <li>Type checking utilities - 类型检查工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Token num = Token.of(TokenType.NUMBER, 42, 0, 2);
 * Token op = Token.of(TokenType.PLUS, 3);
 * boolean isNum = num.is(TokenType.NUMBER);  // true
 * String val = num.stringValue();  // "42"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable record - 线程安全: 是，不可变记录</li>
 *   <li>Null-safe: Yes, null value handled in stringValue() - 空值安全: 是，stringValue()中处理null值</li>
 * </ul>
 *
 * @param type the token type | 词法单元类型
 * @param value the token value | 词法单元值
 * @param position the position in the source | 源中的位置
 * @param length the length of the token | 词法单元长度
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public record Token(TokenType type, Object value, int position, int length) {

    /**
     * Create a token with no value
     * 创建无值的词法单元
     *
     * @param type the token type | 词法单元类型
     * @param position the position | 位置
     * @return the token | 词法单元
     */
    public static Token of(TokenType type, int position) {
        return new Token(type, null, position, 1);
    }

    /**
     * Create a token with position and length
     * 创建带位置和长度的词法单元
     *
     * @param type the token type | 词法单元类型
     * @param position the position | 位置
     * @param length the length | 长度
     * @return the token | 词法单元
     */
    public static Token of(TokenType type, int position, int length) {
        return new Token(type, null, position, length);
    }

    /**
     * Create a token with value
     * 创建有值的词法单元
     *
     * @param type the token type | 词法单元类型
     * @param value the value | 值
     * @param position the position | 位置
     * @param length the length | 长度
     * @return the token | 词法单元
     */
    public static Token of(TokenType type, Object value, int position, int length) {
        return new Token(type, value, position, length);
    }

    /**
     * Check if this token is of given type
     * 检查此词法单元是否为给定类型
     *
     * @param type the type to check | 要检查的类型
     * @return true if matches | 如果匹配返回true
     */
    public boolean is(TokenType type) {
        return this.type == type;
    }

    /**
     * Check if this token is any of given types
     * 检查此词法单元是否为给定类型之一
     *
     * @param types the types to check | 要检查的类型
     * @return true if matches any | 如果匹配任一返回true
     */
    public boolean isAny(TokenType... types) {
        for (TokenType t : types) {
            if (this.type == t) return true;
        }
        return false;
    }

    /**
     * Get string value
     * 获取字符串值
     *
     * @return the string value | 字符串值
     */
    public String stringValue() {
        return value != null ? value.toString() : "";
    }

    /**
     * Get number value
     * 获取数字值
     *
     * @return the number value | 数字值
     */
    public Number numberValue() {
        if (value instanceof Number n) {
            return n;
        }
        return 0;
    }
}
