package cloud.opencode.base.crypto.symmetric;

import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Interface for AEAD (Authenticated Encryption with Associated Data) ciphers.
 * AEAD（关联数据认证加密）加密接口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Authenticated encryption with associated data - 带关联数据的认证加密</li>
 *   <li>Automatic nonce generation - 自动随机数生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AeadCipher cipher = AesGcmCipher.aes256Gcm();
 * byte[] encrypted = cipher.encrypt(data, key);
 * byte[] decrypted = cipher.decrypt(encrypted, key);
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
public interface AeadCipher {

    /**
     * Set the secret key.
     * 设置密钥。
     *
     * @param key secret key / 密钥
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setKey(SecretKey key);

    /**
     * Set the secret key from bytes.
     * 从字节数组设置密钥。
     *
     * @param key key bytes / 密钥字节
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setKey(byte[] key);

    /**
     * Set the initialization vector.
     * 设置初始化向量。
     *
     * @param iv initialization vector / 初始化向量
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setIv(byte[] iv);

    /**
     * Set the nonce (same as IV for most AEAD ciphers).
     * 设置随机数（对于大多数 AEAD 密码与 IV 相同）。
     *
     * @param nonce nonce bytes / 随机数字节
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setNonce(byte[] nonce);

    /**
     * Set additional authenticated data (AAD).
     * 设置附加认证数据（AAD）。
     *
     * @param aad additional authenticated data / 附加认证数据
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setAad(byte[] aad);

    /**
     * Set authentication tag length in bits.
     * 设置认证标签长度（比特）。
     *
     * @param tagBits tag length in bits / 标签长度（比特）
     * @return this cipher instance / 当前加密实例
     */
    AeadCipher setTagLength(int tagBits);

    /**
     * Encrypt plaintext bytes.
     * 加密明文字节。
     *
     * @param plaintext plaintext bytes / 明文字节
     * @return ciphertext with authentication tag / 带认证标签的密文
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * Encrypt plaintext string.
     * 加密明文字符串。
     *
     * @param plaintext plaintext string / 明文字符串
     * @return ciphertext with authentication tag / 带认证标签的密文
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
     * Encrypt string and encode as Base64.
     * 加密字符串并编码为 Base64。
     *
     * @param plaintext plaintext string / 明文字符串
     * @return Base64 encoded ciphertext / Base64 编码的密文
     */
    String encryptBase64(String plaintext);

    /**
     * Encrypt and encode as hexadecimal.
     * 加密并编码为十六进制。
     *
     * @param plaintext plaintext bytes / 明文字节
     * @return hex encoded ciphertext / 十六进制编码的密文
     */
    String encryptHex(byte[] plaintext);

    /**
     * Encrypt a file.
     * 加密文件。
     *
     * @param source source file path / 源文件路径
     * @param target target file path / 目标文件路径
     */
    void encryptFile(Path source, Path target);

    /**
     * Create an encrypting output stream.
     * 创建加密输出流。
     *
     * @param output underlying output stream / 底层输出流
     * @return encrypting output stream / 加密输出流
     */
    OutputStream encryptStream(OutputStream output);

    /**
     * Decrypt ciphertext bytes.
     * 解密密文字节。
     *
     * @param ciphertext ciphertext with authentication tag / 带认证标签的密文
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
     * Decrypt Base64 encoded ciphertext to string.
     * 解密 Base64 编码的密文为字符串。
     *
     * @param base64Ciphertext Base64 encoded ciphertext / Base64 编码的密文
     * @return plaintext string / 明文字符串
     */
    String decryptBase64ToString(String base64Ciphertext);

    /**
     * Decrypt hexadecimal encoded ciphertext.
     * 解密十六进制编码的密文。
     *
     * @param hexCiphertext hex encoded ciphertext / 十六进制编码的密文
     * @return plaintext bytes / 明文字节
     */
    byte[] decryptHex(String hexCiphertext);

    /**
     * Decrypt a file.
     * 解密文件。
     *
     * @param source source file path / 源文件路径
     * @param target target file path / 目标文件路径
     */
    void decryptFile(Path source, Path target);

    /**
     * Create a decrypting input stream.
     * 创建解密输入流。
     *
     * @param input underlying input stream / 底层输入流
     * @return decrypting input stream / 解密输入流
     */
    InputStream decryptStream(InputStream input);

    /**
     * Generate a random initialization vector.
     * 生成随机初始化向量。
     *
     * @return IV bytes / 初始化向量字节
     */
    byte[] generateIv();

    /**
     * Generate a random nonce.
     * 生成随机随机数。
     *
     * @return nonce bytes / 随机数字节
     */
    byte[] generateNonce();

    /**
     * Get the IV/nonce length in bytes.
     * 获取初始化向量/随机数长度（字节）。
     *
     * @return IV length / 初始化向量长度
     */
    int getIvLength();

    /**
     * Get the algorithm name.
     * 获取算法名称。
     *
     * @return algorithm name / 算法名称
     */
    String getAlgorithm();
}
