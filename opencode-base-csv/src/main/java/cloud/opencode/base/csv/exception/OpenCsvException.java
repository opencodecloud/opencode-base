package cloud.opencode.base.csv.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * CSV Exception - Base exception for all CSV operations
 * CSV异常 - 所有CSV操作的基础异常
 *
 * <p>Thrown when CSV parsing, writing, binding, or I/O operations fail.
 * Extends {@link OpenException} to maintain consistent exception hierarchy.
 * Carries location information (line and column) for precise error reporting.</p>
 * <p>当CSV解析、写入、绑定或I/O操作失败时抛出。
 * 继承 {@link OpenException} 以保持一致的异常体系。
 * 携带位置信息（行和列）用于精确错误报告。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extends OpenException for unified hierarchy - 继承OpenException统一异常体系</li>
 *   <li>Line and column location tracking - 行和列位置跟踪</li>
 *   <li>Factory methods for common error types - 常见错误类型的工厂方法</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenCsvException.parseError("Unclosed quote", 5, 23);
 * throw OpenCsvException.writeError("Failed to write CSV", cause);
 * throw OpenCsvException.bindError("Cannot bind to target", cause);
 * throw OpenCsvException.ioError("File not found", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-csv V1.0.3
 */
public class OpenCsvException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "CSV";

    /**
     * The line number where the error occurred (-1 if unknown)
     * 错误发生的行号（-1表示未知）
     */
    private final int line;

    /**
     * The column number where the error occurred (-1 if unknown)
     * 错误发生的列号（-1表示未知）
     */
    private final int column;

    /**
     * Constructs exception with message
     * 构造带消息的异常
     *
     * @param message the detail message | 详细消息
     */
    public OpenCsvException(String message) {
        this(message, -1, -1, null);
    }

    /**
     * Constructs exception with message and cause
     * 构造带消息和原因的异常
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenCsvException(String message, Throwable cause) {
        this(message, -1, -1, cause);
    }

    /**
     * Constructs exception with message and location
     * 构造带消息和位置的异常
     *
     * @param message the detail message | 详细消息
     * @param line    the line number (-1 if unknown) | 行号（-1表示未知）
     * @param column  the column number (-1 if unknown) | 列号（-1表示未知）
     */
    public OpenCsvException(String message, int line, int column) {
        this(message, line, column, null);
    }

    /**
     * Constructs exception with all diagnostic fields
     * 构造带所有诊断字段的异常
     *
     * @param message the detail message | 详细消息
     * @param line    the line number (-1 if unknown) | 行号（-1表示未知）
     * @param column  the column number (-1 if unknown) | 列号（-1表示未知）
     * @param cause   the cause | 原因
     */
    public OpenCsvException(String message, int line, int column, Throwable cause) {
        super(COMPONENT, null, formatMessage(message, line, column), cause);
        this.line = line;
        this.column = column;
    }

    private static String formatMessage(String message, int line, int column) {
        // Sanitize CRLF to prevent log injection
        String safeMessage = sanitize(message);
        if (line < 0 && column < 0) {
            return safeMessage;
        }
        StringBuilder sb = new StringBuilder();
        if (safeMessage != null) {
            sb.append(safeMessage);
        }
        sb.append(" (");
        if (line >= 0) {
            sb.append("line ").append(line);
        }
        if (line >= 0 && column >= 0) {
            sb.append(", ");
        }
        if (column >= 0) {
            sb.append("column ").append(column);
        }
        sb.append(')');
        return sb.toString();
    }

    // ==================== Getters | 获取方法 ====================

    /**
     * Gets the line number where the error occurred
     * 获取错误发生的行号
     *
     * @return the line number, or -1 if unknown | 行号，未知则返回-1
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number where the error occurred
     * 获取错误发生的列号
     *
     * @return the column number, or -1 if unknown | 列号，未知则返回-1
     */
    public int getColumn() {
        return column;
    }

    /**
     * Checks if line location is available
     * 检查行位置是否可用
     *
     * @return true if line is known | 如果行号已知返回true
     */
    public boolean hasLineInfo() {
        return line >= 0;
    }

    /**
     * Checks if column location is available
     * 检查列位置是否可用
     *
     * @return true if column is known | 如果列号已知返回true
     */
    public boolean hasColumnInfo() {
        return column >= 0;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for parse errors
     * 为解析错误创建异常
     *
     * @param message the error message | 错误消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     * @return the exception | 异常
     */
    public static OpenCsvException parseError(String message, int line, int column) {
        return new OpenCsvException("Parse error: " + message, line, column);
    }

    /**
     * Creates exception for write errors
     * 为写入错误创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     * @return the exception | 异常
     */
    public static OpenCsvException writeError(String message, Throwable cause) {
        return new OpenCsvException("Write error: " + message, -1, -1, cause);
    }

    /**
     * Creates exception for bind errors
     * 为绑定错误创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     * @return the exception | 异常
     */
    public static OpenCsvException bindError(String message, Throwable cause) {
        return new OpenCsvException("Bind error: " + message, -1, -1, cause);
    }

    /**
     * Creates exception for I/O errors
     * 为I/O错误创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     * @return the exception | 异常
     */
    public static OpenCsvException ioError(String message, Throwable cause) {
        return new OpenCsvException("I/O error: " + message, -1, -1, cause);
    }

    /**
     * Sanitizes a string by replacing CR/LF with escape sequences to prevent CRLF log injection
     * 净化字符串，将CR/LF替换为转义序列以防止CRLF日志注入
     */
    private static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Fast path: no CR/LF present
        if (input.indexOf('\r') < 0 && input.indexOf('\n') < 0) {
            return input;
        }
        return input.replace("\r", "\\r").replace("\n", "\\n");
    }
}
