package cloud.opencode.base.crypto.hash;

/**
 * Hash function interface for cryptographic hash operations - Provides unified API for various hash algorithms
 * 哈希函数接口 - 为各种哈希算法提供统一的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte array and string hashing - 字节数组和字符串哈希</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 *   <li>Algorithm name and digest length access - 算法名称和摘要长度访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashFunction sha256 = Sha2Hash.sha256();
 * byte[] digest = sha256.hash("data");
 * String hex = sha256.hashHex("data");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为数据长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public interface HashFunction {

    /**
     * Compute hash of byte array
     * 计算字节数组的哈希值
     *
     * @param data input data to hash
     * @return hash digest as byte array
     * @throws NullPointerException if data is null
     */
    byte[] hash(byte[] data);

    /**
     * Compute hash of string (UTF-8 encoded)
     * 计算字符串的哈希值（使用 UTF-8 编码）
     *
     * @param data input string to hash
     * @return hash digest as byte array
     * @throws NullPointerException if data is null
     */
    byte[] hash(String data);

    /**
     * Compute hash and return as hexadecimal string
     * 计算哈希值并返回十六进制字符串
     *
     * @param data input data to hash
     * @return hash digest as lowercase hex string
     * @throws NullPointerException if data is null
     */
    String hashHex(byte[] data);

    /**
     * Compute hash of string and return as hexadecimal string
     * 计算字符串哈希值并返回十六进制字符串
     *
     * @param data input string to hash
     * @return hash digest as lowercase hex string
     * @throws NullPointerException if data is null
     */
    String hashHex(String data);

    /**
     * Compute hash and return as Base64 string
     * 计算哈希值并返回 Base64 字符串
     *
     * @param data input data to hash
     * @return hash digest as Base64 string
     * @throws NullPointerException if data is null
     */
    String hashBase64(byte[] data);

    /**
     * Get the digest length in bytes
     * 获取摘要长度（字节数）
     *
     * @return digest length in bytes
     */
    int getDigestLength();

    /**
     * Get the hash algorithm name
     * 获取哈希算法名称
     *
     * @return algorithm name
     */
    String getAlgorithm();
}
