package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.exception.OpenWebException;

/**
 * Open Crypto Exception
 * 加密异常
 *
 * <p>Exception for encryption/decryption errors.</p>
 * <p>加密/解密错误的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Specific exception for encryption/decryption errors - 加密/解密错误的特定异常</li>
 *   <li>Factory methods for common error types - 常见错误类型的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw OpenCryptoException.encryptionFailed("Invalid padding");
 * throw OpenCryptoException.decryptionFailed("Corrupt data");
 * throw OpenCryptoException.invalidKey("Key too short");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 是（构造后不可变）</li>
 *   <li>Null-safe: No (message should not be null) - 否（消息不应为null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public class OpenCryptoException extends OpenWebException {

    /**
     * Create exception with message
     * 使用消息创建异常
     *
     * @param message the message | 消息
     */
    public OpenCryptoException(String message) {
        super("C1001", message, 500);
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     */
    public OpenCryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create encryption exception
     * 创建加密异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenCryptoException encryptionFailed(String message) {
        return new OpenCryptoException("Encryption failed: " + message);
    }

    /**
     * Create decryption exception
     * 创建解密异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenCryptoException decryptionFailed(String message) {
        return new OpenCryptoException("Decryption failed: " + message);
    }

    /**
     * Create key exception
     * 创建密钥异常
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static OpenCryptoException invalidKey(String message) {
        return new OpenCryptoException("Invalid key: " + message);
    }
}
