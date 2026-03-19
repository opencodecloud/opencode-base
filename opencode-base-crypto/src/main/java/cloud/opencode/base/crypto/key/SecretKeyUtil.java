package cloud.opencode.base.crypto.key;

import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Secret key utility class - Utility methods for symmetric key operations
 * 对称密钥工具类 - 对称密钥操作的实用方法
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Secret key creation and conversion - 密钥创建和转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SecretKey key = SecretKeyUtil.fromBytes(keyBytes, "AES");
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
public final class SecretKeyUtil {

    private static final int PBKDF2_ITERATIONS = 100000;

    private SecretKeyUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generate secret key with specified algorithm and key size
     * 生成指定算法和密钥大小的对称密钥
     *
     * @param algorithm key algorithm (e.g., "AES", "ChaCha20")
     * @param keySize   key size in bits
     * @return generated secret key
     * @throws OpenKeyException if key generation fails
     */
    public static SecretKey generate(String algorithm, int keySize) {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        if (keySize <= 0) {
            throw new OpenKeyException("Key size must be positive");
        }

        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance(algorithm);
            keyGen.init(keySize);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw OpenKeyException.generationFailed(algorithm, e);
        }
    }

    /**
     * Create secret key from byte array
     * 从字节数组创建对称密钥
     *
     * @param keyBytes  key bytes
     * @param algorithm key algorithm (e.g., "AES", "ChaCha20")
     * @return secret key
     * @throws OpenKeyException if key creation fails
     */
    public static SecretKey fromBytes(byte[] keyBytes, String algorithm) {
        if (keyBytes == null || keyBytes.length == 0) {
            throw new OpenKeyException("Key bytes cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }

        try {
            return new SecretKeySpec(keyBytes, algorithm);
        } catch (Exception e) {
            throw OpenKeyException.generationFailed(algorithm, e);
        }
    }

    /**
     * Convert secret key to byte array
     * 将对称密钥转换为字节数组
     *
     * @param key secret key
     * @return key bytes
     * @throws OpenKeyException if key is null or conversion fails
     */
    public static byte[] toBytes(SecretKey key) {
        if (key == null) {
            throw new OpenKeyException("Secret key cannot be null");
        }

        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            throw new OpenKeyException("Unable to encode secret key");
        }

        return encoded;
    }

    /**
     * Derive secret key from master key using PBKDF2
     * 使用 PBKDF2 从主密钥派生对称密钥
     *
     * @param masterKey master key bytes (e.g., password)
     * @param salt      salt for key derivation
     * @param algorithm target algorithm (e.g., "AES")
     * @param keySize   target key size in bits
     * @return derived secret key
     * @throws OpenKeyException if key derivation fails
     */
    public static SecretKey derive(byte[] masterKey, byte[] salt, String algorithm, int keySize) {
        if (masterKey == null || masterKey.length == 0) {
            throw new OpenKeyException("Master key cannot be null or empty");
        }
        if (salt == null || salt.length == 0) {
            throw new OpenKeyException("Salt cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        if (keySize <= 0) {
            throw new OpenKeyException("Key size must be positive");
        }

        try {
            // Convert bytes to char array for PBEKeySpec
            char[] password = new char[masterKey.length];
            for (int i = 0; i < masterKey.length; i++) {
                password[i] = (char) (masterKey[i] & 0xFF);
            }

            try {
                PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, keySize);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                SecretKey tmp = factory.generateSecret(spec);
                spec.clearPassword();

                return new SecretKeySpec(tmp.getEncoded(), algorithm);
            } finally {
                Arrays.fill(password, '\0');
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw OpenKeyException.generationFailed(algorithm, e);
        }
    }

    /**
     * Derive secret key from password using PBKDF2
     * 使用 PBKDF2 从密码派生对称密钥
     *
     * @param password  password characters
     * @param salt      salt for key derivation
     * @param algorithm target algorithm (e.g., "AES")
     * @param keySize   target key size in bits
     * @return derived secret key
     * @throws OpenKeyException if key derivation fails
     */
    public static SecretKey deriveFromPassword(char[] password, byte[] salt, String algorithm, int keySize) {
        if (password == null || password.length == 0) {
            throw new OpenKeyException("Password cannot be null or empty");
        }
        if (salt == null || salt.length == 0) {
            throw new OpenKeyException("Salt cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new OpenKeyException("Algorithm cannot be null or empty");
        }
        if (keySize <= 0) {
            throw new OpenKeyException("Key size must be positive");
        }

        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, keySize);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            SecretKey tmp = factory.generateSecret(spec);
            spec.clearPassword();

            return new SecretKeySpec(tmp.getEncoded(), algorithm);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw OpenKeyException.generationFailed(algorithm, e);
        }
    }

    /**
     * Check if two secret keys are equal
     * 检查两个对称密钥是否相等
     *
     * @param key1 first secret key
     * @param key2 second secret key
     * @return true if keys are equal
     * @throws OpenKeyException if either key is null
     */
    public static boolean equals(SecretKey key1, SecretKey key2) {
        if (key1 == null || key2 == null) {
            throw new OpenKeyException("Keys cannot be null");
        }

        if (!key1.getAlgorithm().equals(key2.getAlgorithm())) {
            return false;
        }

        byte[] encoded1 = key1.getEncoded();
        byte[] encoded2 = key2.getEncoded();

        if (encoded1 == null || encoded2 == null) {
            return false;
        }

        return MessageDigest.isEqual(encoded1, encoded2);
    }
}
