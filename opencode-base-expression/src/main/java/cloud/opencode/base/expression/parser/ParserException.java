package cloud.opencode.base.expression.parser;

import cloud.opencode.base.expression.OpenExpressionException;

/**
 * Parser Exception
 * 解析异常
 *
 * <p>Thrown when an error occurs during expression parsing.
 * Includes detailed position information for error reporting.</p>
 * <p>在表达式解析过程中发生错误时抛出。包含用于错误报告的详细位置信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detailed error position (line, column) - 详细的错误位置（行、列）</li>
 *   <li>Expression context with position indicator (^) - 表达式上下文和位置指示符</li>
 *   <li>Typed error categories via ErrorType enum - 通过ErrorType枚举的类型化错误分类</li>
 *   <li>Static factory methods for common parse errors - 常见解析错误的静态工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     Parser.parse("1 ++ 2");
 * } catch (ParserException e) {
 *     int line = e.getLine();
 *     int col = e.getColumn();
 *     ParserException.ErrorType type = e.getErrorType();
 * }
 *
 * // Create specific errors
 * throw ParserException.unexpectedToken("+", "1 ++ 2", 3);
 * throw ParserException.unterminatedString(5);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, immutable after construction - 线程安全: 是，构造后不可变</li>
 *   <li>Null-safe: Yes, null expression handled in formatting - 空值安全: 是，null表达式在格式化中处理</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class ParserException extends OpenExpressionException {

    private final String expression;
    private final int line;
    private final int column;
    private final ErrorType errorType;

    /**
     * Create parser exception
     * 创建解析异常
     *
     * @param message the error message | 错误消息
     */
    public ParserException(String message) {
        this(message, null, -1, -1, ErrorType.GENERAL);
    }

    /**
     * Create parser exception with position
     * 创建带位置的解析异常
     *
     * @param message the error message | 错误消息
     * @param position the error position | 错误位置
     */
    public ParserException(String message, int position) {
        this(message, null, 1, position, ErrorType.GENERAL);
    }

    /**
     * Create parser exception with expression and position
     * 创建带表达式和位置的解析异常
     *
     * @param message the error message | 错误消息
     * @param expression the expression | 表达式
     * @param position the error position | 错误位置
     */
    public ParserException(String message, String expression, int position) {
        this(message, expression, 1, position, ErrorType.GENERAL);
    }

    /**
     * Create parser exception with full details
     * 创建带完整详情的解析异常
     *
     * @param message the error message | 错误消息
     * @param expression the expression | 表达式
     * @param line the line number | 行号
     * @param column the column number | 列号
     * @param errorType the error type | 错误类型
     */
    public ParserException(String message, String expression, int line, int column, ErrorType errorType) {
        super(formatMessage(message, expression, line, column), expression, column);
        this.expression = expression;
        this.line = line;
        this.column = column;
        this.errorType = errorType;
    }

    private static String formatMessage(String message, String expression, int line, int column) {
        StringBuilder sb = new StringBuilder();
        sb.append("Parse error: ").append(message);

        if (line > 0 && column >= 0) {
            sb.append(" at line ").append(line).append(", column ").append(column);
        } else if (column >= 0) {
            sb.append(" at position ").append(column);
        }

        if (expression != null && !expression.isEmpty()) {
            sb.append("\n  Expression: ").append(expression);

            // Add position indicator
            if (column >= 0 && column < expression.length()) {
                sb.append("\n              ");
                sb.append(" ".repeat(column));
                sb.append("^");
            }
        }

        return sb.toString();
    }

    /**
     * Get the expression
     * 获取表达式
     *
     * @return the expression | 表达式
     */
    @Override
    public String getExpression() {
        return expression;
    }

    /**
     * Get the line number
     * 获取行号
     *
     * @return the line number | 行号
     */
    public int getLine() {
        return line;
    }

    /**
     * Get the column number
     * 获取列号
     *
     * @return the column number | 列号
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the error type
     * 获取错误类型
     *
     * @return the error type | 错误类型
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create unexpected token error
     * 创建意外词法单元错误
     *
     * @param token the unexpected token | 意外的词法单元
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException unexpectedToken(String token, int position) {
        return new ParserException(
                "Unexpected token: " + token,
                null, 1, position, ErrorType.UNEXPECTED_TOKEN
        );
    }

    /**
     * Create unexpected token error with expression
     * 创建带表达式的意外词法单元错误
     *
     * @param token the unexpected token | 意外的词法单元
     * @param expression the expression | 表达式
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException unexpectedToken(String token, String expression, int position) {
        return new ParserException(
                "Unexpected token: " + token,
                expression, 1, position, ErrorType.UNEXPECTED_TOKEN
        );
    }

    /**
     * Create expected token error
     * 创建期望词法单元错误
     *
     * @param expected the expected token | 期望的词法单元
     * @param actual the actual token | 实际的词法单元
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException expectedToken(String expected, String actual, int position) {
        return new ParserException(
                "Expected '" + expected + "' but found '" + actual + "'",
                null, 1, position, ErrorType.EXPECTED_TOKEN
        );
    }

    /**
     * Create expected token error with expression
     * 创建带表达式的期望词法单元错误
     *
     * @param expected the expected token | 期望的词法单元
     * @param actual the actual token | 实际的词法单元
     * @param expression the expression | 表达式
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException expectedToken(String expected, String actual,
            String expression, int position) {
        return new ParserException(
                "Expected '" + expected + "' but found '" + actual + "'",
                expression, 1, position, ErrorType.EXPECTED_TOKEN
        );
    }

    /**
     * Create unexpected end of expression error
     * 创建表达式意外结束错误
     *
     * @param expression the expression | 表达式
     * @return the exception | 异常
     */
    public static ParserException unexpectedEnd(String expression) {
        return new ParserException(
                "Unexpected end of expression",
                expression, 1, expression != null ? expression.length() : 0,
                ErrorType.UNEXPECTED_END
        );
    }

    /**
     * Create invalid number format error
     * 创建无效数字格式错误
     *
     * @param value the invalid value | 无效的值
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException invalidNumber(String value, int position) {
        return new ParserException(
                "Invalid number format: " + value,
                null, 1, position, ErrorType.INVALID_NUMBER
        );
    }

    /**
     * Create unterminated string error
     * 创建未终止字符串错误
     *
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException unterminatedString(int position) {
        return new ParserException(
                "Unterminated string literal",
                null, 1, position, ErrorType.UNTERMINATED_STRING
        );
    }

    /**
     * Create invalid escape sequence error
     * 创建无效转义序列错误
     *
     * @param sequence the invalid sequence | 无效的序列
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException invalidEscapeSequence(String sequence, int position) {
        return new ParserException(
                "Invalid escape sequence: " + sequence,
                null, 1, position, ErrorType.INVALID_ESCAPE
        );
    }

    /**
     * Create unbalanced parentheses error
     * 创建不平衡括号错误
     *
     * @param position the position | 位置
     * @return the exception | 异常
     */
    public static ParserException unbalancedParentheses(int position) {
        return new ParserException(
                "Unbalanced parentheses",
                null, 1, position, ErrorType.UNBALANCED_PARENS
        );
    }

    /**
     * Error Type Enum
     * 错误类型枚举
     */
    public enum ErrorType {
        /**
         * General parse error
         * 一般解析错误
         */
        GENERAL,

        /**
         * Unexpected token
         * 意外词法单元
         */
        UNEXPECTED_TOKEN,

        /**
         * Expected token not found
         * 未找到期望的词法单元
         */
        EXPECTED_TOKEN,

        /**
         * Unexpected end of expression
         * 表达式意外结束
         */
        UNEXPECTED_END,

        /**
         * Invalid number format
         * 无效数字格式
         */
        INVALID_NUMBER,

        /**
         * Unterminated string
         * 未终止字符串
         */
        UNTERMINATED_STRING,

        /**
         * Invalid escape sequence
         * 无效转义序列
         */
        INVALID_ESCAPE,

        /**
         * Unbalanced parentheses
         * 不平衡括号
         */
        UNBALANCED_PARENS,

        /**
         * Invalid identifier
         * 无效标识符
         */
        INVALID_IDENTIFIER,

        /**
         * Unknown operator
         * 未知运算符
         */
        UNKNOWN_OPERATOR
    }
}
