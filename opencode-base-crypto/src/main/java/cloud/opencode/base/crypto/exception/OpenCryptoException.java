package cloud.opencode.base.crypto.exception;

/**
 * Base exception for cryptographic operations - Runtime exception for encryption/decryption failures
 * 加密组件基础异常 - 加密/解密操作失败时的运行时异常
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Algorithm and operation context in error messages - 错误消息中包含算法和操作上下文</li>
 *   <li>Factory methods for common error scenarios - 常见错误场景的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenCryptoException("AES", "encrypt", "Key not set");
 * throw new OpenCryptoException("Operation failed", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public class OpenCryptoException extends RuntimeException {
    private static final String COMPONENT = "crypto";
    private final String algorithm;
    private final String operation;

    /**
     * Constructs a new OpenCryptoException with the specified message.
     * 使用指定消息构造新的加密异常
     *
     * @param message the detail message
     */
    public OpenCryptoException(String message) {
        super(message);
        this.algorithm = null;
        this.operation = null;
    }

    /**
     * Constructs a new OpenCryptoException with the specified message and cause.
     * 使用指定消息和原因构造新的加密异常
     *
     * @param message the detail message
     * @param cause the cause
     */
    public OpenCryptoException(String message, Throwable cause) {
        super(message, cause);
        this.algorithm = null;
        this.operation = null;
    }

    /**
     * Constructs a new OpenCryptoException with algorithm, operation and message.
     * 使用算法、操作和消息构造新的加密异常
     *
     * @param algorithm the cryptographic algorithm
     * @param operation the operation being performed
     * @param message the detail message
     */
    public OpenCryptoException(String algorithm, String operation, String message) {
        super(formatMessage(algorithm, operation, message));
        this.algorithm = algorithm;
        this.operation = operation;
    }

    /**
     * Constructs a new OpenCryptoException with algorithm, operation, message and cause.
     * 使用算法、操作、消息和原因构造新的加密异常
     *
     * @param algorithm the cryptographic algorithm
     * @param operation the operation being performed
     * @param message the detail message
     * @param cause the cause
     */
    public OpenCryptoException(String algorithm, String operation, String message, Throwable cause) {
        super(formatMessage(algorithm, operation, message), cause);
        this.algorithm = algorithm;
        this.operation = operation;
    }

    /**
     * Returns the cryptographic algorithm associated with this exception.
     * 返回与此异常关联的加密算法
     *
     * @return the algorithm, or null if not specified
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Returns the operation being performed when this exception occurred.
     * 返回发生异常时正在执行的操作
     *
     * @return the operation, or null if not specified
     */
    public String operation() {
        return operation;
    }

    /**
     * Creates an exception for when a cryptographic algorithm is not available.
     * 创建加密算法不可用时的异常
     *
     * @param algorithm the unavailable algorithm
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException algorithmNotAvailable(String algorithm) {
        return new OpenCryptoException(algorithm, "initialization",
            "Algorithm not available: " + algorithm);
    }

    /**
     * Creates an exception for encryption failures.
     * 创建加密失败时的异常
     *
     * @param algorithm the encryption algorithm
     * @param cause the underlying cause
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException encryptionFailed(String algorithm, Throwable cause) {
        return new OpenCryptoException(algorithm, "encryption",
            "Encryption failed", cause);
    }

    /**
     * Creates an exception for decryption failures.
     * 创建解密失败时的异常
     *
     * @param algorithm the decryption algorithm
     * @param cause the underlying cause
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException decryptionFailed(String algorithm, Throwable cause) {
        return new OpenCryptoException(algorithm, "decryption",
            "Decryption failed", cause);
    }

    /**
     * Creates an exception for authentication failures.
     * 创建身份验证失败时的异常
     *
     * @param algorithm the authentication algorithm
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException authenticationFailed(String algorithm) {
        return new OpenCryptoException(algorithm, "authentication",
            "Authentication failed - data may have been tampered with");
    }

    /**
     * Creates an exception for padding errors.
     * 创建填充错误时的异常
     *
     * @param algorithm the algorithm with padding error
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException paddingError(String algorithm) {
        return new OpenCryptoException(algorithm, "padding",
            "Invalid padding or corrupted data");
    }

    /**
     * Creates an exception for invalid initialization vector (IV).
     * 创建无效初始化向量时的异常
     *
     * @param algorithm the algorithm requiring IV
     * @param expected the expected IV length
     * @param actual the actual IV length
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException invalidIv(String algorithm, int expected, int actual) {
        return new OpenCryptoException(algorithm, "initialization",
            String.format("Invalid IV length: expected %d bytes, got %d bytes", expected, actual));
    }

    /**
     * Creates an exception for data that exceeds maximum size.
     * 创建数据超过最大长度时的异常
     *
     * @param algorithm the algorithm with size limitation
     * @param maxSize the maximum allowed size
     * @return a new OpenCryptoException
     */
    public static OpenCryptoException dataTooLong(String algorithm, int maxSize) {
        return new OpenCryptoException(algorithm, "validation",
            String.format("Data exceeds maximum size of %d bytes for algorithm %s", maxSize, algorithm));
    }

    /**
     * Formats the exception message with algorithm and operation context.
     * 使用算法和操作上下文格式化异常消息
     *
     * @param algorithm the algorithm
     * @param operation the operation
     * @param message the base message
     * @return formatted message
     */
    private static String formatMessage(String algorithm, String operation, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(COMPONENT).append("]");
        if (algorithm != null) {
            sb.append(" [").append(algorithm).append("]");
        }
        if (operation != null) {
            sb.append(" [").append(operation).append("]");
        }
        sb.append(" ").append(message);
        return sb.toString();
    }
}
