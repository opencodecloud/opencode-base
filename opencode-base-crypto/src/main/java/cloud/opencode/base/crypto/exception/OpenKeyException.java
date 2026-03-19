package cloud.opencode.base.crypto.exception;

/**
 * Exception for key-related operations - Runtime exception for key generation, parsing and validation failures
 * 密钥相关异常 - 密钥生成、解析和验证失败时的运行时异常
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key type context in error messages - 错误消息中包含密钥类型上下文</li>
 *   <li>Covers key generation, parsing, and validation failures - 涵盖密钥生成、解析和验证失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenKeyException("RSA", "Invalid key size");
 * throw new OpenKeyException("Key parsing failed", cause);
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
public class OpenKeyException extends RuntimeException {
    private static final String COMPONENT = "crypto";
    private final String keyType;

    /**
     * Constructs a new OpenKeyException with the specified message.
     * 使用指定消息构造新的密钥异常
     *
     * @param message the detail message
     */
    public OpenKeyException(String message) {
        super(message);
        this.keyType = null;
    }

    /**
     * Constructs a new OpenKeyException with the specified message and cause.
     * 使用指定消息和原因构造新的密钥异常
     *
     * @param message the detail message
     * @param cause the cause
     */
    public OpenKeyException(String message, Throwable cause) {
        super(message, cause);
        this.keyType = null;
    }

    /**
     * Constructs a new OpenKeyException with key type and message.
     * 使用密钥类型和消息构造新的密钥异常
     *
     * @param keyType the type of key involved
     * @param message the detail message
     */
    public OpenKeyException(String keyType, String message) {
        super(formatMessage(keyType, message));
        this.keyType = keyType;
    }

    /**
     * Constructs a new OpenKeyException with key type, message and cause.
     * 使用密钥类型、消息和原因构造新的密钥异常
     *
     * @param keyType the type of key involved
     * @param message the detail message
     * @param cause the cause
     */
    public OpenKeyException(String keyType, String message, Throwable cause) {
        super(formatMessage(keyType, message), cause);
        this.keyType = keyType;
    }

    /**
     * Returns the key type associated with this exception.
     * 返回与此异常关联的密钥类型
     *
     * @return the key type, or null if not specified
     */
    public String keyType() {
        return keyType;
    }

    /**
     * Creates an exception for key generation failures.
     * 创建密钥生成失败时的异常
     *
     * @param keyType the type of key that failed to generate
     * @param cause the underlying cause
     * @return a new OpenKeyException
     */
    public static OpenKeyException generationFailed(String keyType, Throwable cause) {
        return new OpenKeyException(keyType, "Key generation failed", cause);
    }

    /**
     * Creates an exception for key parsing failures.
     * 创建密钥解析失败时的异常
     *
     * @param keyType the type of key that failed to parse
     * @param cause the underlying cause
     * @return a new OpenKeyException
     */
    public static OpenKeyException parseFailed(String keyType, Throwable cause) {
        return new OpenKeyException(keyType, "Key parsing failed", cause);
    }

    /**
     * Creates an exception for insufficient key strength.
     * 创建密钥强度不足时的异常
     *
     * @param keyType the type of key
     * @param minBits the minimum required key size in bits
     * @param actualBits the actual key size in bits
     * @return a new OpenKeyException
     */
    public static OpenKeyException insufficientStrength(String keyType, int minBits, int actualBits) {
        return new OpenKeyException(keyType,
            String.format("Insufficient key strength: minimum %d bits required, got %d bits",
                minBits, actualBits));
    }

    /**
     * Creates an exception for invalid key format.
     * 创建密钥格式无效时的异常
     *
     * @param keyType the type of key
     * @param format the invalid format
     * @return a new OpenKeyException
     */
    public static OpenKeyException invalidFormat(String keyType, String format) {
        return new OpenKeyException(keyType,
            String.format("Invalid key format: %s", format));
    }

    /**
     * Creates an exception for key type mismatch.
     * 创建密钥类型不匹配时的异常
     *
     * @param expected the expected key type
     * @param actual the actual key type
     * @return a new OpenKeyException
     */
    public static OpenKeyException typeMismatch(String expected, String actual) {
        return new OpenKeyException(actual,
            String.format("Key type mismatch: expected %s, got %s", expected, actual));
    }

    /**
     * Creates an exception for when a required key is not set.
     * 创建必需密钥未设置时的异常
     *
     * @param operation the operation requiring the key
     * @return a new OpenKeyException
     */
    public static OpenKeyException keyNotSet(String operation) {
        return new OpenKeyException(
            String.format("Key not set for operation: %s", operation));
    }

    /**
     * Formats the exception message with key type context.
     * 使用密钥类型上下文格式化异常消息
     *
     * @param keyType the key type
     * @param message the base message
     * @return formatted message
     */
    private static String formatMessage(String keyType, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(COMPONENT).append("]");
        if (keyType != null) {
            sb.append(" [").append(keyType).append("]");
        }
        sb.append(" ").append(message);
        return sb.toString();
    }
}
