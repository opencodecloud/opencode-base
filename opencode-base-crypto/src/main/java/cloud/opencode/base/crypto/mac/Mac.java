package cloud.opencode.base.crypto.mac;

/**
 * Message Authentication Code (MAC) interface - Provides cryptographic message authentication
 * 消息认证码接口 - 提供加密消息认证功能
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message authentication code computation - 消息认证码计算</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 *   <li>Constant-time verification - 常量时间验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Mac mac = HmacSha256.of(key);
 * byte[] tag = mac.compute("message".getBytes());
 * boolean valid = mac.verify("message".getBytes(), tag);
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
public interface Mac {

    /**
     * Computes MAC for the given byte array
     * 计算给定字节数组的 MAC 值
     *
     * @param data the data to authenticate
     * @return the MAC value
     */
    byte[] compute(byte[] data);

    /**
     * Computes MAC for the given string (UTF-8 encoded)
     * 计算给定字符串的 MAC 值（UTF-8 编码）
     *
     * @param data the string to authenticate
     * @return the MAC value
     */
    byte[] compute(String data);

    /**
     * Computes MAC and returns as hexadecimal string
     * 计算 MAC 值并返回十六进制字符串
     *
     * @param data the data to authenticate
     * @return the MAC value as hex string
     */
    String computeHex(byte[] data);

    /**
     * Computes MAC and returns as Base64 string
     * 计算 MAC 值并返回 Base64 字符串
     *
     * @param data the data to authenticate
     * @return the MAC value as Base64 string
     */
    String computeBase64(byte[] data);

    /**
     * Verifies MAC for the given data using constant-time comparison
     * 使用常量时间比较验证给定数据的 MAC 值
     *
     * @param data the data to verify
     * @param mac the expected MAC value
     * @return true if MAC is valid, false otherwise
     */
    boolean verify(byte[] data, byte[] mac);

    /**
     * Verifies MAC from hexadecimal string using constant-time comparison
     * 使用常量时间比较验证十六进制字符串的 MAC 值
     *
     * @param data the data to verify
     * @param macHex the expected MAC value in hex format
     * @return true if MAC is valid, false otherwise
     */
    boolean verifyHex(byte[] data, String macHex);

    /**
     * Verifies MAC from Base64 string using constant-time comparison
     * 使用常量时间比较验证 Base64 字符串的 MAC 值
     *
     * @param data the data to verify
     * @param macBase64 the expected MAC value in Base64 format
     * @return true if MAC is valid, false otherwise
     */
    boolean verifyBase64(byte[] data, String macBase64);

    /**
     * Updates the MAC with a chunk of data (for incremental computation)
     * 使用数据块更新 MAC（用于增量计算）
     *
     * @param data the data chunk to add
     * @return this MAC instance for chaining
     */
    Mac update(byte[] data);

    /**
     * Updates the MAC with a string (UTF-8 encoded)
     * 使用字符串更新 MAC（UTF-8 编码）
     *
     * @param data the string to add
     * @return this MAC instance for chaining
     */
    Mac update(String data);

    /**
     * Completes the MAC computation and returns the result
     * 完成 MAC 计算并返回结果
     *
     * @return the final MAC value
     */
    byte[] doFinal();

    /**
     * Completes the MAC computation and returns as hexadecimal string
     * 完成 MAC 计算并返回十六进制字符串
     *
     * @return the final MAC value as hex string
     */
    String doFinalHex();

    /**
     * Completes the MAC computation and returns as Base64 string
     * 完成 MAC 计算并返回 Base64 字符串
     *
     * @return the final MAC value as Base64 string
     */
    String doFinalBase64();

    /**
     * Resets the MAC to its initial state
     * 将 MAC 重置为初始状态
     *
     * @return this MAC instance for chaining
     */
    Mac reset();

    /**
     * Returns the algorithm name
     * 返回算法名称
     *
     * @return the algorithm name
     */
    String getAlgorithm();

    /**
     * Returns the MAC length in bytes
     * 返回 MAC 长度（字节数）
     *
     * @return the MAC length
     */
    int getMacLength();
}
