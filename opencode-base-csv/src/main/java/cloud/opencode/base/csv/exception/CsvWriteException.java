package cloud.opencode.base.csv.exception;

import java.io.Serial;

/**
 * CSV Write Exception - Exception for CSV writing errors
 * CSV写入异常 - CSV写入错误异常
 *
 * <p>Thrown when CSV content cannot be written due to I/O errors,
 * encoding problems, or invalid data.</p>
 * <p>当由于I/O错误、编码问题或无效数据而无法写入CSV内容时抛出。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw CsvWriteException.of("Failed to flush output", ioException);
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
public class CsvWriteException extends OpenCsvException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a write exception with message
     * 构造带消息的写入异常
     *
     * @param message the detail message | 详细消息
     */
    public CsvWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a write exception with message and cause
     * 构造带消息和原因的写入异常
     *
     * @param message the detail message | 详细消息
     * @param cause   the cause | 原因
     */
    public CsvWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a write exception with message and cause
     * 创建带消息和原因的写入异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     * @return the exception | 异常
     */
    public static CsvWriteException of(String message, Throwable cause) {
        return new CsvWriteException(message, cause);
    }
}
