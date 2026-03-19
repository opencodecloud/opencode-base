package cloud.opencode.base.crypto.signature;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Digital signature engine interface - Unified API for signature generation and verification
 * 数字签名引擎接口 - 统一的签名生成和验证 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Digital signature generation and verification - 数字签名生成和验证</li>
 *   <li>Key pair management - 密钥对管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SignatureEngine engine = EddsaSignature.ed25519();
 * engine.setPrivateKey(privateKey);
 * byte[] sig = engine.sign(data);
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
public interface SignatureEngine {

    // ========== Key Configuration Methods ==========

    /**
     * Set the private key for signing operations
     * 设置用于签名操作的私钥
     *
     * @param privateKey the private key
     * @return this engine instance for method chaining
     */
    SignatureEngine setPrivateKey(PrivateKey privateKey);

    /**
     * Set the private key from encoded bytes
     * 从编码字节设置私钥
     *
     * @param encodedKey encoded private key bytes
     * @return this engine instance for method chaining
     */
    SignatureEngine setPrivateKey(byte[] encodedKey);

    /**
     * Set the private key from PEM format
     * 从 PEM 格式设置私钥
     *
     * @param pem PEM formatted private key
     * @return this engine instance for method chaining
     */
    SignatureEngine setPrivateKeyPem(String pem);

    /**
     * Set the public key for verification operations
     * 设置用于验证操作的公钥
     *
     * @param publicKey the public key
     * @return this engine instance for method chaining
     */
    SignatureEngine setPublicKey(PublicKey publicKey);

    /**
     * Set the public key from encoded bytes
     * 从编码字节设置公钥
     *
     * @param encodedKey encoded public key bytes
     * @return this engine instance for method chaining
     */
    SignatureEngine setPublicKey(byte[] encodedKey);

    /**
     * Set the public key from PEM format
     * 从 PEM 格式设置公钥
     *
     * @param pem PEM formatted public key
     * @return this engine instance for method chaining
     */
    SignatureEngine setPublicKeyPem(String pem);

    /**
     * Set both keys from a key pair
     * 从密钥对设置公私钥
     *
     * @param keyPair the key pair
     * @return this engine instance for method chaining
     */
    SignatureEngine setKeyPair(KeyPair keyPair);

    // ========== One-Shot Signing Methods ==========

    /**
     * Sign data and return signature bytes
     * 签名数据并返回签名字节
     *
     * @param data data to sign
     * @return signature bytes
     */
    byte[] sign(byte[] data);

    /**
     * Sign UTF-8 encoded string data
     * 签名 UTF-8 编码的字符串数据
     *
     * @param data string data to sign
     * @return signature bytes
     */
    byte[] sign(String data);

    /**
     * Sign data and return Base64 encoded signature
     * 签名数据并返回 Base64 编码的签名
     *
     * @param data data to sign
     * @return Base64 encoded signature
     */
    String signBase64(byte[] data);

    /**
     * Sign UTF-8 encoded string and return Base64 signature
     * 签名 UTF-8 编码字符串并返回 Base64 签名
     *
     * @param data string data to sign
     * @return Base64 encoded signature
     */
    String signBase64(String data);

    /**
     * Sign data and return hexadecimal encoded signature
     * 签名数据并返回十六进制编码的签名
     *
     * @param data data to sign
     * @return hexadecimal encoded signature
     */
    String signHex(byte[] data);

    /**
     * Sign file content
     * 签名文件内容
     *
     * @param file file to sign
     * @return signature bytes
     */
    byte[] signFile(Path file);

    /**
     * Sign data from input stream
     * 从输入流签名数据
     *
     * @param input input stream to read data from
     * @return signature bytes
     */
    byte[] sign(InputStream input);

    // ========== One-Shot Verification Methods ==========

    /**
     * Verify signature for given data
     * 验证给定数据的签名
     *
     * @param data data that was signed
     * @param signature signature bytes
     * @return true if signature is valid
     */
    boolean verify(byte[] data, byte[] signature);

    /**
     * Verify signature for UTF-8 encoded string
     * 验证 UTF-8 编码字符串的签名
     *
     * @param data string data that was signed
     * @param signature signature bytes
     * @return true if signature is valid
     */
    boolean verify(String data, byte[] signature);

    /**
     * Verify Base64 encoded signature
     * 验证 Base64 编码的签名
     *
     * @param data data that was signed
     * @param base64Signature Base64 encoded signature
     * @return true if signature is valid
     */
    boolean verifyBase64(byte[] data, String base64Signature);

    /**
     * Verify Base64 encoded signature for string data
     * 验证字符串数据的 Base64 编码签名
     *
     * @param data string data that was signed
     * @param base64Signature Base64 encoded signature
     * @return true if signature is valid
     */
    boolean verifyBase64(String data, String base64Signature);

    /**
     * Verify hexadecimal encoded signature
     * 验证十六进制编码的签名
     *
     * @param data data that was signed
     * @param hexSignature hexadecimal encoded signature
     * @return true if signature is valid
     */
    boolean verifyHex(byte[] data, String hexSignature);

    /**
     * Verify signature for file content
     * 验证文件内容的签名
     *
     * @param file file that was signed
     * @param signature signature bytes
     * @return true if signature is valid
     */
    boolean verifyFile(Path file, byte[] signature);

    // ========== Multi-Part Signing Methods ==========

    /**
     * Update the signature with additional data (for multi-part signing)
     * 使用额外数据更新签名（用于多部分签名）
     *
     * @param data data to add
     * @return this engine instance for method chaining
     */
    SignatureEngine update(byte[] data);

    /**
     * Update the signature with UTF-8 encoded string
     * 使用 UTF-8 编码字符串更新签名
     *
     * @param data string data to add
     * @return this engine instance for method chaining
     */
    SignatureEngine update(String data);

    /**
     * Complete the multi-part signing operation
     * 完成多部分签名操作
     *
     * @return signature bytes
     */
    byte[] doSign();

    /**
     * Complete the multi-part signing and return Base64 signature
     * 完成多部分签名并返回 Base64 签名
     *
     * @return Base64 encoded signature
     */
    String doSignBase64();

    /**
     * Complete the multi-part verification operation
     * 完成多部分验证操作
     *
     * @param signature signature to verify
     * @return true if signature is valid
     */
    boolean doVerify(byte[] signature);

    // ========== Information Methods ==========

    /**
     * Get the signature algorithm name
     * 获取签名算法名称
     *
     * @return algorithm name
     */
    String getAlgorithm();
}
