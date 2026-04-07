package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import cloud.opencode.base.crypto.util.SecureEraser;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;

/**
 * Key utility class for key operations and queries - Utility methods for working with cryptographic keys
 * 密钥工具类 - 密钥操作和查询的实用方法
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key conversion and encoding utilities - 密钥转换和编码工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] encoded = KeyUtil.encode(key);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(k) - 空间复杂度: O(k)，k为密钥大小</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class KeyUtil {

    private KeyUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Get key size in bits
     * 获取密钥大小（位）
     *
     * @param key the key to check
     * @return key size in bits
     * @throws OpenKeyException if key size cannot be determined
     */
    public static int getKeySize(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }

        // Try RSA key
        if (key instanceof RSAKey rsaKey) {
            return rsaKey.getModulus().bitLength();
        }

        // Try EC key
        if (key instanceof ECKey ecKey) {
            return ecKey.getParams().getOrder().bitLength();
        }

        // Try SecretKey
        if (key instanceof SecretKey) {
            byte[] encoded = key.getEncoded();
            if (encoded != null) {
                int size = encoded.length * 8;
                SecureEraser.erase(encoded);
                return size;
            }
        }

        // Fallback to encoded format
        byte[] encoded = key.getEncoded();
        if (encoded != null) {
            int size = encoded.length * 8;
            SecureEraser.erase(encoded);
            return size;
        }

        throw new OpenKeyException("Unable to determine key size for algorithm: " + key.getAlgorithm());
    }

    /**
     * Get key algorithm
     * 获取密钥算法
     *
     * @param key the key to check
     * @return algorithm name
     * @throws OpenKeyException if key is null
     */
    public static String getAlgorithm(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        return key.getAlgorithm();
    }

    /**
     * Check if key is a private key
     * 检查密钥是否为私钥
     *
     * @param key the key to check
     * @return true if key is a private key
     * @throws OpenKeyException if key is null
     */
    public static boolean isPrivateKey(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        return key instanceof PrivateKey;
    }

    /**
     * Check if key is a public key
     * 检查密钥是否为公钥
     *
     * @param key the key to check
     * @return true if key is a public key
     * @throws OpenKeyException if key is null
     */
    public static boolean isPublicKey(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        return key instanceof PublicKey;
    }

    /**
     * Check if key is a secret key
     * 检查密钥是否为对称密钥
     *
     * @param key the key to check
     * @return true if key is a secret key
     * @throws OpenKeyException if key is null
     */
    public static boolean isSecretKey(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        return key instanceof SecretKey;
    }

    /**
     * Get encoded key bytes
     * 获取编码的密钥字节
     *
     * @param key the key to encode
     * @return encoded key bytes
     * @throws OpenKeyException if key is null or encoding fails
     */
    public static byte[] getEncoded(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            throw new OpenKeyException("Key encoding failed for algorithm: " + key.getAlgorithm());
        }
        return encoded;
    }

    /**
     * Get key format
     * 获取密钥格式
     *
     * @param key the key to check
     * @return key format (e.g., "X.509", "PKCS#8", "RAW")
     * @throws OpenKeyException if key is null
     */
    public static String getFormat(Key key) {
        if (key == null) {
            throw new OpenKeyException("Key cannot be null");
        }
        String format = key.getFormat();
        return format != null ? format : "Unknown";
    }
}
