package cloud.opencode.base.csv.exception;

import java.io.Serial;

/**
 * CSV Parse Exception - Exception for CSV parsing errors
 * CSV解析异常 - CSV解析错误异常
 *
 * <p>Thrown when CSV content cannot be parsed due to syntax errors such as
 * unclosed quotes, invalid escaping, or malformed records. Carries the
 * problematic line content for diagnostics.</p>
 * <p>当CSV内容因语法错误（如未关闭的引号、无效转义或畸形记录）而无法解析时抛出。
 * 携带有问题的行内容用于诊断。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw CsvParseException.of("Unclosed quote", 5, 23, "field1,\"unclosed");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public class CsvParseException extends OpenCsvException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The content of the problematic line
     * 有问题的行内容
     */
    private final String lineContent;

    /**
     * Constructs a parse exception with all details
     * 构造带所有详细信息的解析异常
     *
     * @param message     the detail message | 详细消息
     * @param line        the line number | 行号
     * @param column      the column number | 列号
     * @param lineContent the problematic line content | 有问题的行内容
     */
    public CsvParseException(String message, int line, int column, String lineContent) {
        super(message, line, column);
        this.lineContent = lineContent;
    }

    /**
     * Constructs a parse exception with all details and cause
     * 构造带所有详细信息和原因的解析异常
     *
     * @param message     the detail message | 详细消息
     * @param line        the line number | 行号
     * @param column      the column number | 列号
     * @param lineContent the problematic line content | 有问题的行内容
     * @param cause       the cause | 原因
     */
    public CsvParseException(String message, int line, int column, String lineContent, Throwable cause) {
        super(message, line, column, cause);
        this.lineContent = lineContent;
    }

    /**
     * Gets the content of the problematic line
     * 获取有问题的行内容
     *
     * @return the line content, or null if not available | 行内容，如果不可用则为null
     */
    public String getLineContent() {
        return lineContent;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a parse exception with location and line content
     * 创建带位置和行内容的解析异常
     *
     * @param message     the error message | 错误消息
     * @param line        the line number | 行号
     * @param column      the column number | 列号
     * @param lineContent the problematic line content | 有问题的行内容
     * @return the exception | 异常
     */
    public static CsvParseException of(String message, int line, int column, String lineContent) {
        return new CsvParseException(message, line, column, lineContent);
    }
}
