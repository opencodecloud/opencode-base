package cloud.opencode.base.yml.exception;

/**
 * Open YAML Exception - Base exception for YAML operations
 * YAML 异常基类 - YAML 操作的基础异常
 *
 * <p>This is the base class for all YAML-related exceptions in this module.</p>
 * <p>这是本模块中所有 YAML 相关异常的基类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Line and column location tracking for parse errors - 解析错误的行列位置跟踪</li>
 *   <li>Base class for all YAML exceptions (parse, bind, path, security) - 所有 YAML 异常的基类</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     OpenYml.parse(invalidYaml);
 * } catch (OpenYmlException e) {
 *     if (e.hasLocation()) {
 *         System.err.println("Error at line " + e.getLine());
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (message may be null) - 空值安全: 否（消息可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public class OpenYmlException extends RuntimeException {

    private final int line;
    private final int column;

    /**
     * Constructs an exception with message.
     * 构造带消息的异常。
     *
     * @param message the detail message | 详细消息
     */
    public OpenYmlException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    /**
     * Constructs an exception with message and location.
     * 构造带消息和位置的异常。
     *
     * @param message the detail message | 详细消息
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public OpenYmlException(String message, int line, int column) {
        super(formatMessage(message, line, column));
        this.line = line;
        this.column = column;
    }

    /**
     * Constructs an exception with message and cause.
     * 构造带消息和原因的异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public OpenYmlException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }

    /**
     * Constructs an exception with message, cause and location.
     * 构造带消息、原因和位置的异常。
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     * @param line    the line number | 行号
     * @param column  the column number | 列号
     */
    public OpenYmlException(String message, Throwable cause, int line, int column) {
        super(formatMessage(message, line, column), cause);
        this.line = line;
        this.column = column;
    }

    /**
     * Gets the line number where the error occurred.
     * 获取错误发生的行号。
     *
     * @return the line number, or -1 if unknown | 行号，如果未知则返回 -1
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number where the error occurred.
     * 获取错误发生的列号。
     *
     * @return the column number, or -1 if unknown | 列号，如果未知则返回 -1
     */
    public int getColumn() {
        return column;
    }

    /**
     * Checks if location information is available.
     * 检查位置信息是否可用。
     *
     * @return true if location is available | 如果位置可用则返回 true
     */
    public boolean hasLocation() {
        return line >= 0 && column >= 0;
    }

    private static String formatMessage(String message, int line, int column) {
        if (line >= 0 && column >= 0) {
            return String.format("%s (line: %d, column: %d)", message, line, column);
        }
        return message;
    }
}
