package cloud.opencode.base.pdf.exception;

/**
 * PDF Exception - Base exception for all PDF operations
 * PDF 异常 - 所有 PDF 操作的基础异常
 *
 * <p>This exception is thrown when PDF operations fail, including parsing,
 * writing, signing, and form processing errors.</p>
 * <p>当 PDF 操作失败时抛出此异常，包括解析、写入、签名和表单处理错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Operation type tracking - 操作类型跟踪</li>
 *   <li>Page number association for page-specific errors - 页面相关错误的页码关联</li>
 *   <li>Factory methods for common error scenarios - 常见错误场景的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenPdfException.invalidFormat("Missing header");
 * throw OpenPdfException.readFailed("/path/to/file.pdf", cause);
 * throw OpenPdfException.invalidPageNumber(5, 3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable after construction - 线程安全: 是 — 构造后不可变</li>
 *   <li>Null-safe: Partial — message is required, operation and pageNumber may be null - 空值安全: 部分 — 消息必填，操作和页码可为空</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public class OpenPdfException extends RuntimeException {

    private static final String COMPONENT = "[PDF]";

    /** Operation type | 操作类型 */
    private final String operation;

    /** Page number (if applicable) | 页码（如适用）*/
    private final Integer pageNumber;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates exception with message
     * 创建带消息的异常
     *
     * @param message error message | 错误消息
     */
    public OpenPdfException(String message) {
        super(COMPONENT + " " + message);
        this.operation = null;
        this.pageNumber = null;
    }

    /**
     * Creates exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message error message | 错误消息
     * @param cause   root cause | 根本原因
     */
    public OpenPdfException(String message, Throwable cause) {
        super(COMPONENT + " " + message, cause);
        this.operation = null;
        this.pageNumber = null;
    }

    /**
     * Creates exception with operation and message
     * 创建带操作和消息的异常
     *
     * @param operation operation type | 操作类型
     * @param message   error message | 错误消息
     */
    public OpenPdfException(String operation, String message) {
        super(COMPONENT + " " + message);
        this.operation = operation;
        this.pageNumber = null;
    }

    /**
     * Creates exception with operation, message and cause
     * 创建带操作、消息和原因的异常
     *
     * @param operation operation type | 操作类型
     * @param message   error message | 错误消息
     * @param cause     root cause | 根本原因
     */
    public OpenPdfException(String operation, String message, Throwable cause) {
        super(COMPONENT + " " + message, cause);
        this.operation = operation;
        this.pageNumber = null;
    }

    /**
     * Creates exception with operation, page number and message
     * 创建带操作、页码和消息的异常
     *
     * @param operation  operation type | 操作类型
     * @param pageNumber page number | 页码
     * @param message    error message | 错误消息
     */
    public OpenPdfException(String operation, int pageNumber, String message) {
        super(COMPONENT + " " + message);
        this.operation = operation;
        this.pageNumber = pageNumber;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the operation type
     * 获取操作类型
     *
     * @return operation type | 操作类型
     */
    public String operation() {
        return operation;
    }

    /**
     * Gets the page number
     * 获取页码
     *
     * @return page number, or null if not applicable | 页码，如不适用则返回 null
     */
    public Integer pageNumber() {
        return pageNumber;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for invalid PDF format
     * 创建无效 PDF 格式异常
     *
     * @param reason reason description | 原因描述
     * @return OpenPdfException
     */
    public static OpenPdfException invalidFormat(String reason) {
        return new OpenPdfException("parse",
            String.format("Invalid PDF format: %s", reason));
    }

    /**
     * Creates exception for read failure
     * 创建读取失败异常
     *
     * @param path  file path | 文件路径
     * @param cause root cause | 根本原因
     * @return OpenPdfException
     */
    public static OpenPdfException readFailed(String path, Throwable cause) {
        return new OpenPdfException("read",
            String.format("Failed to read PDF: %s", path), cause);
    }

    /**
     * Creates exception for write failure
     * 创建写入失败异常
     *
     * @param path  file path | 文件路径
     * @param cause root cause | 根本原因
     * @return OpenPdfException
     */
    public static OpenPdfException writeFailed(String path, Throwable cause) {
        return new OpenPdfException("write",
            String.format("Failed to write PDF: %s", path), cause);
    }

    /**
     * Creates exception for invalid page number
     * 创建无效页码异常
     *
     * @param pageNumber page number | 页码
     * @param totalPages total pages in document | 文档总页数
     * @return OpenPdfException
     */
    public static OpenPdfException invalidPageNumber(int pageNumber, int totalPages) {
        return new OpenPdfException("page", pageNumber,
            String.format("Invalid page number: %d (document has %d pages)",
                pageNumber, totalPages));
    }

    /**
     * Creates exception for password required
     * 创建需要密码异常
     *
     * @return OpenPdfException
     */
    public static OpenPdfException passwordRequired() {
        return new OpenPdfException("decrypt",
            "Document is encrypted. Password required.");
    }

    /**
     * Creates exception for wrong password
     * 创建密码错误异常
     *
     * @return OpenPdfException
     */
    public static OpenPdfException wrongPassword() {
        return new OpenPdfException("decrypt",
            "Incorrect password provided.");
    }

    /**
     * Creates exception for signature failure
     * 创建签名失败异常
     *
     * @param reason reason description | 原因描述
     * @param cause  root cause | 根本原因
     * @return OpenPdfException
     */
    public static OpenPdfException signatureFailed(String reason, Throwable cause) {
        return new OpenPdfException("sign",
            String.format("Signature failed: %s", reason), cause);
    }

    /**
     * Creates exception for form field not found
     * 创建表单字段未找到异常
     *
     * @param fieldName field name | 字段名
     * @return OpenPdfException
     */
    public static OpenPdfException fieldNotFound(String fieldName) {
        return new OpenPdfException("form",
            String.format("Form field not found: %s", fieldName));
    }

    /**
     * Creates exception for unsupported feature
     * 创建不支持功能异常
     *
     * @param feature feature name | 功能名称
     * @return OpenPdfException
     */
    public static OpenPdfException unsupportedFeature(String feature) {
        return new OpenPdfException("feature",
            String.format("Unsupported PDF feature: %s", feature));
    }

    /**
     * Creates exception for merge failure
     * 创建合并失败异常
     *
     * @param reason reason description | 原因描述
     * @param cause  root cause | 根本原因
     * @return OpenPdfException
     */
    public static OpenPdfException mergeFailed(String reason, Throwable cause) {
        return new OpenPdfException("merge",
            String.format("PDF merge failed: %s", reason), cause);
    }

    /**
     * Creates exception for split failure
     * 创建拆分失败异常
     *
     * @param reason reason description | 原因描述
     * @param cause  root cause | 根本原因
     * @return OpenPdfException
     */
    public static OpenPdfException splitFailed(String reason, Throwable cause) {
        return new OpenPdfException("split",
            String.format("PDF split failed: %s", reason), cause);
    }
}
