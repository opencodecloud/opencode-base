package cloud.opencode.base.crypto.symmetric;

import javax.crypto.SecretKey;

/**
 * Interface for symmetric encryption algorithms (CBC, CTR modes).
 * 对称加密算法接口（CBC、CTR 模式）。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Symmetric encryption and decryption - 对称加密和解密</li>
 *   <li>Key and IV management - 密钥和 IV 管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SymmetricCipher cipher = AesCipher.cbc();
 * cipher.setKey(secretKey);
 * byte[] encrypted = cipher.encrypt(data);
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
public interface SymmetricCipher {

    /**
     * Set the secret key.
     * 设置密钥。
     *
     * @param key secret key / 密钥
     * @return this cipher instance / 当前加密实例
     */
    SymmetricCipher setKey(SecretKey key);

    /**
     * Set the secret key from bytes.
     * 从字节数组设置密钥。
     *
     * @param key key bytes / 密钥字节
     * @return this cipher instance / 当前加密实例
     */
    SymmetricCipher setKey(byte[] key);

    /**
     * Set the initialization vector.
     * 设置初始化向量。
     *
     * @param iv initialization vector / 初始化向量
     * @return this cipher instance / 当前加密实例
     */
    SymmetricCipher setIv(byte[] iv);

    /**
     * Set the cipher mode.
     * 设置加密模式。
     *
     * @param mode cipher mode / 加密模式
     * @return this cipher instance / 当前加密实例
     */
    SymmetricCipher setMode(CipherMode mode);

    /**
     * Set the padding scheme.
     * 设置填充方案。
     *
     * @param padding padding scheme / 填充方案
     * @return this cipher instance / 当前加密实例
     */
    SymmetricCipher setPadding(Padding padding);

    /**
     * Encrypt plaintext bytes.
     * 加密明文字节。
     *
     * @param plaintext plaintext bytes / 明文字节
     * @return ciphertext bytes / 密文字节
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * Encrypt plaintext string.
     * 加密明文字符串。
     *
     * @param plaintext plaintext string / 明文字符串
     * @return ciphertext bytes / 密文字节
     */
    byte[] encrypt(String plaintext);

    /**
     * Encrypt and encode as Base64.
     * 加密并编码为 Base64。
     *
     * @param plaintext plaintext bytes / 明文字节
     * @return Base64 encoded ciphertext / Base64 编码的密文
     */
    String encryptBase64(byte[] plaintext);

    /**
     * Encrypt and encode as hexadecimal.
     * 加密并编码为十六进制。
     *
     * @param plaintext plaintext bytes / 明文字节
     * @return hex encoded ciphertext / 十六进制编码的密文
     */
    String encryptHex(byte[] plaintext);

    /**
     * Decrypt ciphertext bytes.
     * 解密密文字节。
     *
     * @param ciphertext ciphertext bytes / 密文字节
     * @return plaintext bytes / 明文字节
     */
    byte[] decrypt(byte[] ciphertext);

    /**
     * Decrypt and convert to string.
     * 解密并转换为字符串。
     *
     * @param ciphertext ciphertext bytes / 密文字节
     * @return plaintext string / 明文字符串
     */
    String decryptToString(byte[] ciphertext);

    /**
     * Decrypt Base64 encoded ciphertext.
     * 解密 Base64 编码的密文。
     *
     * @param base64Ciphertext Base64 encoded ciphertext / Base64 编码的密文
     * @return plaintext bytes / 明文字节
     */
    byte[] decryptBase64(String base64Ciphertext);

    /**
     * Decrypt hexadecimal encoded ciphertext.
     * 解密十六进制编码的密文。
     *
     * @param hexCiphertext hex encoded ciphertext / 十六进制编码的密文
     * @return plaintext bytes / 明文字节
     */
    byte[] decryptHex(String hexCiphertext);

    /**
     * Generate a random initialization vector.
     * 生成随机初始化向量。
     *
     * @return IV bytes / 初始化向量字节
     */
    byte[] generateIv();

    /**
     * Get the block size in bytes.
     * 获取块大小（字节）。
     *
     * @return block size / 块大小
     */
    int getBlockSize();

    /**
     * Get the algorithm name.
     * 获取算法名称。
     *
     * @return algorithm name / 算法名称
     */
    String getAlgorithm();

    /**
     * Get the IV length in bytes.
     * 获取 IV 长度（字节）。
     *
     * @return IV length / IV 长度
     */
    int getIvLength();

    /**
     * Generate a new secret key.
     * 生成新密钥。
     *
     * @param keySize key size in bits / 密钥大小（比特）
     * @return generated secret key / 生成的密钥
     */
    SecretKey generateKey(int keySize);
}
