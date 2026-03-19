package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.Result;

/**
 * Result Encryptor SPI
 * 响应加密器SPI
 *
 * <p>Service Provider Interface for encrypting result data.</p>
 * <p>加密响应数据的服务提供者接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Result encryption and decryption - 响应加密和解密</li>
 *   <li>Algorithm-based dispatch - 基于算法的分发</li>
 *   <li>Pluggable encryption implementations - 可插拔的加密实现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Encrypt a result
 * ResultEncryptor encryptor = new AesResultEncryptor(key);
 * EncryptedResult encrypted = encryptor.encrypt(result);
 *
 * // Decrypt back
 * Result<String> decrypted = encryptor.decrypt(encrypted, String.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (result and encrypted data must not be null) - 空值安全: 否（结果和加密数据不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public interface ResultEncryptor {

    /**
     * Encrypt result
     * 加密响应
     *
     * @param result the result to encrypt | 要加密的响应
     * @param <T> the data type | 数据类型
     * @return the encrypted result | 加密后的响应
     */
    <T> EncryptedResult encrypt(Result<T> result);

    /**
     * Decrypt to result
     * 解密为响应
     *
     * @param encrypted the encrypted result | 加密的响应
     * @param dataType the data type class | 数据类型类
     * @param <T> the data type | 数据类型
     * @return the decrypted result | 解密后的响应
     */
    <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType);

    /**
     * Get the algorithm name
     * 获取算法名称
     *
     * @return the algorithm name | 算法名称
     */
    String getAlgorithm();

    /**
     * Check if this encryptor supports the algorithm
     * 检查此加密器是否支持该算法
     *
     * @param algorithm the algorithm to check | 要检查的算法
     * @return true if supported | 如果支持返回true
     */
    default boolean supports(String algorithm) {
        return getAlgorithm().equalsIgnoreCase(algorithm);
    }
}
