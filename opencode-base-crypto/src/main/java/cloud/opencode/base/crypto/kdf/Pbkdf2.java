package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.util.CryptoUtil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * PBKDF2 (Password-Based Key Derivation Function 2) implementation - PKCS #5 v2.0 standard KDF
 * PBKDF2 密钥派生函数实现 - PKCS #5 v2.0 标准 KDF
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PBKDF2 key derivation - PBKDF2 密钥派生</li>
 *   <li>OWASP recommended configuration - OWASP 推荐配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Pbkdf2 pbkdf2 = Pbkdf2.owaspRecommended();
 * byte[] key = pbkdf2.deriveKey(password, salt, 32);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class Pbkdf2 implements KdfEngine {

    private static final int DEFAULT_SALT_LENGTH = 16; // 128 bits
    private static final int OWASP_ITERATIONS = 600_000; // OWASP recommended (2023) for PBKDF2-HMAC-SHA256
    private static final int OWASP_SHA512_ITERATIONS = 210_000; // OWASP recommended for PBKDF2-HMAC-SHA512

    private final String algorithm;
    private final int iterations;

    private Pbkdf2(String algorithm, int iterations) {
        this.algorithm = algorithm;
        this.iterations = iterations;
    }

    /**
     * Creates PBKDF2 instance using HMAC-SHA256 with specified iterations
     * 创建使用 HMAC-SHA256 和指定迭代次数的 PBKDF2 实例
     *
     * @param iterations the number of iterations (must be positive)
     * @return new Pbkdf2 instance
     * @throws IllegalArgumentException if iterations is not positive
     */
    public static Pbkdf2 hmacSha256(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations must be positive");
        }
        return new Pbkdf2("PBKDF2WithHmacSHA256", iterations);
    }

    /**
     * Creates PBKDF2 instance using HMAC-SHA512 with specified iterations
     * 创建使用 HMAC-SHA512 和指定迭代次数的 PBKDF2 实例
     *
     * @param iterations the number of iterations (must be positive)
     * @return new Pbkdf2 instance
     * @throws IllegalArgumentException if iterations is not positive
     */
    public static Pbkdf2 hmacSha512(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations must be positive");
        }
        return new Pbkdf2("PBKDF2WithHmacSHA512", iterations);
    }

    /**
     * Creates PBKDF2 instance with OWASP recommended parameters (2023)
     * Uses PBKDF2-HMAC-SHA256 with 600,000 iterations
     * 创建使用 OWASP 推荐参数的 PBKDF2 实例（2023）
     * 使用 PBKDF2-HMAC-SHA256 和 600,000 次迭代
     *
     * @return new Pbkdf2 instance with OWASP recommended settings
     */
    public static Pbkdf2 owaspRecommended() {
        return new Pbkdf2("PBKDF2WithHmacSHA256", OWASP_ITERATIONS);
    }

    /**
     * Generates a cryptographically secure random salt
     * 生成密码学安全的随机盐值
     *
     * @return random salt byte array (16 bytes)
     */
    public byte[] generateSalt() {
        return CryptoUtil.randomSalt(DEFAULT_SALT_LENGTH);
    }

    /**
     * Generates a cryptographically secure random salt with specified length
     * 生成指定长度的密码学安全的随机盐值
     *
     * @param length the salt length in bytes
     * @return random salt byte array
     * @throws IllegalArgumentException if length is not positive
     */
    public byte[] generateSalt(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Salt length must be positive");
        }
        return CryptoUtil.randomSalt(length);
    }

    /**
     * Derives a key from a password and salt
     * 从密码和盐值派生密钥
     *
     * @param password  the password as char array (will not be modified)
     * @param salt      the salt value
     * @param keyLength the desired key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if keyLength is not positive
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(char[] password, byte[] salt, int keyLength) {
        return deriveKey(password, salt, keyLength, this.iterations);
    }

    /**
     * Derives a key from a password and salt with custom iteration count
     * 使用自定义迭代次数从密码和盐值派生密钥
     *
     * @param password   the password as char array (will not be modified)
     * @param salt       the salt value
     * @param keyLength  the desired key length in bytes
     * @param iterations the number of iterations
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if keyLength or iterations is not positive
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(char[] password, byte[] salt, int keyLength, int iterations) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(salt, "Salt cannot be null");

        if (keyLength <= 0) {
            throw new IllegalArgumentException("Key length must be positive");
        }

        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations must be positive");
        }

        if (salt.length < 8) {
            throw new IllegalArgumentException("Salt length should be at least 8 bytes");
        }

        PBEKeySpec spec = null;
        try {
            // PBEKeySpec takes key length in bits
            spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        } catch (InvalidKeySpecException e) {
            throw new OpenCryptoException(algorithm, "derive", "Invalid key specification", e);
        } finally {
            // Clear the spec to remove password from memory
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, byte[] salt, byte[] info, int length) {
        Objects.requireNonNull(inputKeyMaterial, "Input key material cannot be null");

        // Convert byte array to char array for password
        char[] password = new char[inputKeyMaterial.length];
        for (int i = 0; i < inputKeyMaterial.length; i++) {
            password[i] = (char) (inputKeyMaterial[i] & 0xFF);
        }

        try {
            // PBKDF2 does not use 'info' parameter, so it's ignored
            byte[] actualSalt = (salt == null || salt.length == 0) ? generateSalt() : salt;
            return deriveKey(password, actualSalt, length);
        } finally {
            // Securely erase password
            CryptoUtil.secureErase(password);
        }
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, int length) {
        return derive(inputKeyMaterial, null, null, length);
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Gets the iteration count
     * 获取迭代次数
     *
     * @return the number of iterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Gets the OWASP recommended iteration count for PBKDF2-HMAC-SHA256
     * 获取 OWASP 推荐的 PBKDF2-HMAC-SHA256 迭代次数
     *
     * @return the OWASP recommended iterations (600,000 as of 2023)
     */
    public static int getOwaspIterations() {
        return OWASP_ITERATIONS;
    }

    /**
     * Gets the OWASP recommended iteration count for PBKDF2-HMAC-SHA512
     * 获取 OWASP 推荐的 PBKDF2-HMAC-SHA512 迭代次数
     *
     * @return the OWASP recommended iterations (210,000 as of 2023)
     */
    public static int getOwaspSha512Iterations() {
        return OWASP_SHA512_ITERATIONS;
    }
}
