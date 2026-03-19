package cloud.opencode.base.crypto.exception;

/**
 * Exception for digital signature operations - Runtime exception for signature generation and verification failures
 * 签名相关异常 - 数字签名生成和验证失败时的运行时异常
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Algorithm context in error messages - 错误消息中包含算法上下文</li>
 *   <li>Covers signature generation and verification failures - 涵盖签名生成和验证失败</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenSignatureException("Ed25519", "Signature verification failed");
 * throw new OpenSignatureException("Invalid signature", cause);
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
public class OpenSignatureException extends RuntimeException {
    private static final String COMPONENT = "crypto";
    private final String algorithm;

    /**
     * Constructs a new OpenSignatureException with the specified message.
     * 使用指定消息构造新的签名异常
     *
     * @param message the detail message
     */
    public OpenSignatureException(String message) {
        super(message);
        this.algorithm = null;
    }

    /**
     * Constructs a new OpenSignatureException with the specified message and cause.
     * 使用指定消息和原因构造新的签名异常
     *
     * @param message the detail message
     * @param cause the cause
     */
    public OpenSignatureException(String message, Throwable cause) {
        super(message, cause);
        this.algorithm = null;
    }

    /**
     * Constructs a new OpenSignatureException with algorithm and message.
     * 使用算法和消息构造新的签名异常
     *
     * @param algorithm the signature algorithm
     * @param message the detail message
     */
    public OpenSignatureException(String algorithm, String message) {
        super(formatMessage(algorithm, message));
        this.algorithm = algorithm;
    }

    /**
     * Constructs a new OpenSignatureException with algorithm, message and cause.
     * 使用算法、消息和原因构造新的签名异常
     *
     * @param algorithm the signature algorithm
     * @param message the detail message
     * @param cause the cause
     */
    public OpenSignatureException(String algorithm, String message, Throwable cause) {
        super(formatMessage(algorithm, message), cause);
        this.algorithm = algorithm;
    }

    /**
     * Returns the signature algorithm associated with this exception.
     * 返回与此异常关联的签名算法
     *
     * @return the algorithm, or null if not specified
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Creates an exception for signature generation failures.
     * 创建签名生成失败时的异常
     *
     * @param algorithm the signature algorithm
     * @param cause the underlying cause
     * @return a new OpenSignatureException
     */
    public static OpenSignatureException signFailed(String algorithm, Throwable cause) {
        return new OpenSignatureException(algorithm, "Signature generation failed", cause);
    }

    /**
     * Creates an exception for signature verification failures.
     * 创建签名验证失败时的异常
     *
     * @param algorithm the signature algorithm
     * @param cause the underlying cause
     * @return a new OpenSignatureException
     */
    public static OpenSignatureException verifyFailed(String algorithm, Throwable cause) {
        return new OpenSignatureException(algorithm, "Signature verification failed", cause);
    }

    /**
     * Creates an exception for invalid signatures.
     * 创建签名无效时的异常
     *
     * @param algorithm the signature algorithm
     * @return a new OpenSignatureException
     */
    public static OpenSignatureException invalidSignature(String algorithm) {
        return new OpenSignatureException(algorithm,
            "Invalid signature - verification failed");
    }

    /**
     * Creates an exception for invalid signature format.
     * 创建签名格式无效时的异常
     *
     * @param algorithm the signature algorithm
     * @param format the invalid format description
     * @return a new OpenSignatureException
     */
    public static OpenSignatureException invalidFormat(String algorithm, String format) {
        return new OpenSignatureException(algorithm,
            String.format("Invalid signature format: %s", format));
    }

    /**
     * Formats the exception message with algorithm context.
     * 使用算法上下文格式化异常消息
     *
     * @param algorithm the algorithm
     * @param message the base message
     * @return formatted message
     */
    private static String formatMessage(String algorithm, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(COMPONENT).append("]");
        if (algorithm != null) {
            sb.append(" [").append(algorithm).append("]");
        }
        sb.append(" ").append(message);
        return sb.toString();
    }
}
