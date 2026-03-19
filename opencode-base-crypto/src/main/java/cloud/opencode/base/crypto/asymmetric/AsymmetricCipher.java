package cloud.opencode.base.crypto.asymmetric;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Interface for asymmetric encryption operations - Provides fluent API for public/private key cryptography
 * 非对称加密操作接口 - 提供公钥/私钥加密的流式 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Public key encryption and private key decryption - 公钥加密和私钥解密</li>
 *   <li>Key pair generation and management - 密钥对生成和管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsymmetricCipher cipher = RsaOaepCipher.sha256();
 * cipher.setPublicKey(publicKey);
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
 *   <li>Time complexity: O(k^2) to O(k^3) depending on algorithm - 时间复杂度: O(k^2)~O(k^3)，取决于算法</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public interface AsymmetricCipher {

    /**
     * Set public key for encryption operations
     * 设置用于加密操作的公钥
     *
     * @param publicKey the public key
     * @return this cipher instance for method chaining
     * @throws NullPointerException if publicKey is null
     */
    AsymmetricCipher setPublicKey(PublicKey publicKey);

    /**
     * Set public key from encoded byte array
     * 从编码的字节数组设置公钥
     *
     * @param encodedKey the encoded public key bytes
     * @return this cipher instance for method chaining
     * @throws NullPointerException if encodedKey is null
     * @throws cloud.opencode.base.crypto.exception.OpenKeyException if key format is invalid
     */
    AsymmetricCipher setPublicKey(byte[] encodedKey);

    /**
     * Set public key from PEM formatted string
     * 从 PEM 格式字符串设置公钥
     *
     * @param pem the PEM formatted public key
     * @return this cipher instance for method chaining
     * @throws NullPointerException if pem is null
     * @throws cloud.opencode.base.crypto.exception.OpenKeyException if PEM format is invalid
     */
    AsymmetricCipher setPublicKeyPem(String pem);

    /**
     * Set private key for decryption operations
     * 设置用于解密操作的私钥
     *
     * @param privateKey the private key
     * @return this cipher instance for method chaining
     * @throws NullPointerException if privateKey is null
     */
    AsymmetricCipher setPrivateKey(PrivateKey privateKey);

    /**
     * Set private key from encoded byte array
     * 从编码的字节数组设置私钥
     *
     * @param encodedKey the encoded private key bytes
     * @return this cipher instance for method chaining
     * @throws NullPointerException if encodedKey is null
     * @throws cloud.opencode.base.crypto.exception.OpenKeyException if key format is invalid
     */
    AsymmetricCipher setPrivateKey(byte[] encodedKey);

    /**
     * Set private key from PEM formatted string
     * 从 PEM 格式字符串设置私钥
     *
     * @param pem the PEM formatted private key
     * @return this cipher instance for method chaining
     * @throws NullPointerException if pem is null
     * @throws cloud.opencode.base.crypto.exception.OpenKeyException if PEM format is invalid
     */
    AsymmetricCipher setPrivateKeyPem(String pem);

    /**
     * Set both public and private keys from key pair
     * 从密钥对设置公钥和私钥
     *
     * @param keyPair the key pair containing public and private keys
     * @return this cipher instance for method chaining
     * @throws NullPointerException if keyPair is null
     */
    AsymmetricCipher setKeyPair(KeyPair keyPair);

    /**
     * Encrypt data using public key
     * 使用公钥加密数据
     *
     * @param plaintext the data to encrypt
     * @return encrypted bytes
     * @throws NullPointerException if plaintext is null
     * @throws IllegalStateException if public key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if encryption fails
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * Encrypt string using public key
     * 使用公钥加密字符串
     *
     * @param plaintext the string to encrypt
     * @return encrypted bytes
     * @throws NullPointerException if plaintext is null
     * @throws IllegalStateException if public key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if encryption fails
     */
    byte[] encrypt(String plaintext);

    /**
     * Encrypt data and return Base64 encoded result
     * 加密数据并返回 Base64 编码结果
     *
     * @param plaintext the data to encrypt
     * @return Base64 encoded ciphertext
     * @throws NullPointerException if plaintext is null
     * @throws IllegalStateException if public key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if encryption fails
     */
    String encryptBase64(byte[] plaintext);

    /**
     * Encrypt data and return hexadecimal encoded result
     * 加密数据并返回十六进制编码结果
     *
     * @param plaintext the data to encrypt
     * @return hexadecimal encoded ciphertext
     * @throws NullPointerException if plaintext is null
     * @throws IllegalStateException if public key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if encryption fails
     */
    String encryptHex(byte[] plaintext);

    /**
     * Decrypt data using private key
     * 使用私钥解密数据
     *
     * @param ciphertext the encrypted data
     * @return decrypted bytes
     * @throws NullPointerException if ciphertext is null
     * @throws IllegalStateException if private key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if decryption fails
     */
    byte[] decrypt(byte[] ciphertext);

    /**
     * Decrypt data and return as string
     * 解密数据并返回字符串
     *
     * @param ciphertext the encrypted data
     * @return decrypted string
     * @throws NullPointerException if ciphertext is null
     * @throws IllegalStateException if private key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if decryption fails
     */
    String decryptToString(byte[] ciphertext);

    /**
     * Decrypt Base64 encoded ciphertext
     * 解密 Base64 编码的密文
     *
     * @param base64Ciphertext the Base64 encoded ciphertext
     * @return decrypted bytes
     * @throws NullPointerException if base64Ciphertext is null
     * @throws IllegalStateException if private key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if decryption fails
     */
    byte[] decryptBase64(String base64Ciphertext);

    /**
     * Decrypt hexadecimal encoded ciphertext
     * 解密十六进制编码的密文
     *
     * @param hexCiphertext the hexadecimal encoded ciphertext
     * @return decrypted bytes
     * @throws NullPointerException if hexCiphertext is null
     * @throws IllegalStateException if private key is not set
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if decryption fails
     */
    byte[] decryptHex(String hexCiphertext);

    /**
     * Get the algorithm name
     * 获取算法名称
     *
     * @return the algorithm name
     */
    String getAlgorithm();

    /**
     * Get maximum size of data that can be encrypted in a single operation
     * 获取单次操作可加密的最大数据大小
     *
     * @return maximum encrypt size in bytes, or -1 if not applicable
     */
    int getMaxEncryptSize();

    /**
     * Generate a new key pair for this cipher
     * 为此加密器生成新的密钥对
     *
     * @return generated key pair
     * @throws cloud.opencode.base.crypto.exception.OpenKeyException if key generation fails
     */
    KeyPair generateKeyPair();
}
