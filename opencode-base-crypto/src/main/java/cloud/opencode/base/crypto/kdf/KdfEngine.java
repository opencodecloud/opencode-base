package cloud.opencode.base.crypto.kdf;

/**
 * Key Derivation Function (KDF) engine interface - Provides unified API for various KDF algorithms
 * 密钥派生函数引擎接口 - 为各种 KDF 算法提供统一的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key derivation from passwords or shared secrets - 从密码或共享密钥派生密钥</li>
 *   <li>Configurable output key length - 可配置的输出密钥长度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KdfEngine kdf = Hkdf.sha256();
 * byte[] derivedKey = kdf.deriveKey(inputKey, salt, info, 32);
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
public interface KdfEngine {

    /**
     * Derives a key from input key material with salt and info parameters
     * 使用盐值和信息参数从输入密钥材料派生密钥
     *
     * @param inputKeyMaterial the input key material (IKM)
     * @param salt             the salt value (can be null or empty for some algorithms)
     * @param info             the context and application specific information (can be null)
     * @param length           the desired output key length in bytes
     * @return the derived key
     * @throws IllegalArgumentException if length is invalid
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if derivation fails
     */
    byte[] derive(byte[] inputKeyMaterial, byte[] salt, byte[] info, int length);

    /**
     * Derives a key from input key material with default parameters
     * 使用默认参数从输入密钥材料派生密钥
     *
     * @param inputKeyMaterial the input key material (IKM)
     * @param length           the desired output key length in bytes
     * @return the derived key
     * @throws IllegalArgumentException if length is invalid
     * @throws cloud.opencode.base.crypto.exception.OpenCryptoException if derivation fails
     */
    byte[] derive(byte[] inputKeyMaterial, int length);

    /**
     * Returns the algorithm name of this KDF
     * 返回此 KDF 的算法名称
     *
     * @return the algorithm name
     */
    String getAlgorithm();
}
