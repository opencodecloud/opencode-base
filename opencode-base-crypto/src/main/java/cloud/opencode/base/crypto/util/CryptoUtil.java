package cloud.opencode.base.crypto.util;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cryptographic utility class providing security-focused operations - Thread-safe utility for secure random generation, constant-time comparison, and key validation
 * 加密工具类提供安全相关操作 - 线程安全的工具类，用于安全随机数生成、常量时间比较和密钥验证
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>General cryptographic utility methods - 通用加密工具方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // General cryptographic utility methods
 * CryptoUtil.ensureAlgorithmAvailable("AES");
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
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为数据长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class CryptoUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Constant-time byte array comparison to prevent timing attacks
     * 常量时间字节数组比较，防止时序攻击
     *
     * @param a first byte array
     * @param b second byte array
     * @return true if arrays are equal, false otherwise
     */
    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        return ConstantTimeUtil.equals(a, b);
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     * 常量时间字符串比较，防止时序攻击
     *
     * @param a first string
     * @param b second string
     * @return true if strings are equal, false otherwise
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        return ConstantTimeUtil.equals(a, b);
    }

    /**
     * Securely erase byte array by overwriting with zeros
     * 通过用零覆盖来安全擦除字节数组
     *
     * @param data byte array to erase
     */
    public static void secureErase(byte[] data) {
        SecureEraser.erase(data);
    }

    /**
     * Securely erase char array by overwriting with zeros
     * 通过用零覆盖来安全擦除字符数组
     *
     * @param data char array to erase
     */
    public static void secureErase(char[] data) {
        SecureEraser.erase(data);
    }

    /**
     * Securely erase ByteBuffer by overwriting with zeros
     * 通过用零覆盖来安全擦除ByteBuffer
     *
     * @param buffer ByteBuffer to erase
     */
    public static void secureErase(ByteBuffer buffer) {
        SecureEraser.erase(buffer);
    }

    /**
     * Get a thread-safe SecureRandom instance
     * 获取线程安全的SecureRandom实例
     *
     * @return SecureRandom instance
     */
    public static SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }

    /**
     * Generate cryptographically secure random bytes
     * 生成密码学安全的随机字节
     *
     * @param length number of bytes to generate
     * @return random byte array
     * @throws IllegalArgumentException if length is negative
     */
    public static byte[] randomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("Length must be non-negative");
        }
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generate cryptographically secure random nonce
     * 生成密码学安全的随机nonce
     *
     * @param length number of bytes to generate
     * @return random nonce byte array
     * @throws IllegalArgumentException if length is negative
     */
    public static byte[] randomNonce(int length) {
        return randomBytes(length);
    }

    /**
     * Generate cryptographically secure random initialization vector
     * 生成密码学安全的随机初始化向量
     *
     * @param length number of bytes to generate
     * @return random IV byte array
     * @throws IllegalArgumentException if length is negative
     */
    public static byte[] randomIv(int length) {
        return randomBytes(length);
    }

    /**
     * Generate cryptographically secure random salt
     * 生成密码学安全的随机盐值
     *
     * @param length number of bytes to generate
     * @return random salt byte array
     * @throws IllegalArgumentException if length is negative
     */
    public static byte[] randomSalt(int length) {
        return randomBytes(length);
    }

    /**
     * Check if secret key has sufficient strength
     * 检查密钥是否具有足够的强度
     *
     * @param key secret key to check
     * @param minBits minimum required key length in bits
     * @return true if key strength is sufficient
     * @throws IllegalArgumentException if key is null or minBits is negative
     */
    public static boolean isKeyStrengthSufficient(SecretKey key, int minBits) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if (minBits < 0) {
            throw new IllegalArgumentException("Minimum bits must be non-negative");
        }

        byte[] encoded = key.getEncoded();
        if (encoded == null) {
            return false;
        }

        int keyBits = encoded.length * 8;
        SecureEraser.erase(encoded);

        return keyBits >= minBits;
    }

    /**
     * Check if key pair has sufficient strength
     * 检查密钥对是否具有足够的强度
     *
     * @param keyPair key pair to check
     * @param minBits minimum required key length in bits
     * @return true if key pair strength is sufficient
     * @throws IllegalArgumentException if keyPair is null or minBits is negative
     */
    public static boolean isKeyPairStrengthSufficient(KeyPair keyPair, int minBits) {
        if (keyPair == null) {
            throw new IllegalArgumentException("Key pair must not be null");
        }
        if (minBits < 0) {
            throw new IllegalArgumentException("Minimum bits must be non-negative");
        }

        PublicKey publicKey = keyPair.getPublic();
        if (publicKey == null) {
            return false;
        }

        // Check RSA key
        if (publicKey instanceof RSAKey rsaKey) {
            return rsaKey.getModulus().bitLength() >= minBits;
        }

        // Check EC key
        if (publicKey instanceof ECKey ecKey) {
            return ecKey.getParams().getOrder().bitLength() >= minBits;
        }

        // For other key types, check encoded length
        byte[] encoded = publicKey.getEncoded();
        if (encoded == null) {
            return false;
        }

        return encoded.length * 8 >= minBits;
    }

    /**
     * Check if cryptographic algorithm is available
     * 检查加密算法是否可用
     *
     * @param algorithm algorithm name to check
     * @return true if algorithm is available
     * @throws IllegalArgumentException if algorithm is null or empty
     */
    public static boolean isAlgorithmAvailable(String algorithm) {
        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm must not be null or empty");
        }

        try {
            // Try different providers to check algorithm availability

            // Check Cipher
            try {
                javax.crypto.Cipher.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                // Continue checking other types
            }

            // Check MessageDigest
            try {
                MessageDigest.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException e) {
                // Continue checking other types
            }

            // Check Mac
            try {
                javax.crypto.Mac.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException e) {
                // Continue checking other types
            }

            // Check KeyGenerator
            try {
                javax.crypto.KeyGenerator.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException e) {
                // Continue checking other types
            }

            // Check KeyPairGenerator
            try {
                KeyPairGenerator.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException e) {
                // Continue checking other types
            }

            // Check Signature
            try {
                Signature.getInstance(algorithm);
                return true;
            } catch (NoSuchAlgorithmException e) {
                // Algorithm not found
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all available algorithms of specified type
     * 获取指定类型的所有可用算法
     *
     * @param type algorithm type (e.g., "Cipher", "MessageDigest", "Mac", "KeyGenerator", "Signature")
     * @return set of available algorithm names
     * @throws IllegalArgumentException if type is null or empty
     */
    public static Set<String> getAvailableAlgorithms(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type must not be null or empty");
        }

        Set<String> algorithms = new TreeSet<>();

        for (Provider provider : Security.getProviders()) {
            for (Provider.Service service : provider.getServices()) {
                if (type.equalsIgnoreCase(service.getType())) {
                    algorithms.add(service.getAlgorithm());
                }
            }
        }

        return algorithms;
    }
}
