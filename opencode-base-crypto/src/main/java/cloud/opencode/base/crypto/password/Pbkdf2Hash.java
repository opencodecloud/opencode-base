package cloud.opencode.base.crypto.password;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.random.RandomBytes;
import cloud.opencode.base.crypto.util.CryptoUtil;
import cloud.opencode.base.crypto.util.SecureEraser;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * PBKDF2 password hashing implementation - Password-Based Key Derivation Function 2 using JDK standard library
 * PBKDF2 密码哈希实现 - 使用 JDK 标准库的基于密码的密钥派生函数 2
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PBKDF2 with SHA-256/SHA-512 - PBKDF2（SHA-256/SHA-512）</li>
 *   <li>Configurable iteration count - 可配置迭代次数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Pbkdf2Hash pbkdf2 = Pbkdf2Hash.sha256();
 * String hash = pbkdf2.hash("password");
 * boolean valid = pbkdf2.verify("password", hash);
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
 *   <li>Time complexity: O(iterations) - 时间复杂度: O(iterations)（迭代次数）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class Pbkdf2Hash implements PasswordHash {

    private static final String ALGORITHM_PREFIX = "PBKDF2";

    // OWASP recommended parameters (2023)
    private static final int DEFAULT_ITERATIONS = 600000; // 600,000 iterations for PBKDF2-HMAC-SHA256
    private static final int DEFAULT_KEY_LENGTH = 256;    // 256 bits
    private static final int DEFAULT_SALT_LENGTH = 16;    // 16 bytes = 128 bits

    private final DigestAlgorithm algorithm;
    private final int iterations;
    private final int keyLength;
    private final int saltLength;
    private final SecureRandom secureRandom;

    private Pbkdf2Hash(DigestAlgorithm algorithm, int iterations, int keyLength, int saltLength, SecureRandom secureRandom) {
        this.algorithm = algorithm;
        this.iterations = iterations;
        this.keyLength = keyLength;
        this.saltLength = saltLength;
        this.secureRandom = secureRandom;
    }

    /**
     * Create PBKDF2 hasher with SHA-256 and OWASP recommended parameters
     * 使用 SHA-256 和 OWASP 推荐参数创建 PBKDF2 哈希器
     *
     * @return new Pbkdf2Hash instance
     */
    public static Pbkdf2Hash sha256() {
        return new Pbkdf2Hash(DigestAlgorithm.SHA256, DEFAULT_ITERATIONS, DEFAULT_KEY_LENGTH, DEFAULT_SALT_LENGTH, new SecureRandom());
    }

    /**
     * Create PBKDF2 hasher with SHA-512
     * 使用 SHA-512 创建 PBKDF2 哈希器
     *
     * @return new Pbkdf2Hash instance
     */
    public static Pbkdf2Hash sha512() {
        return new Pbkdf2Hash(DigestAlgorithm.SHA512, DEFAULT_ITERATIONS, 512, DEFAULT_SALT_LENGTH, new SecureRandom());
    }

    /**
     * Create PBKDF2 hasher with OWASP recommended parameters (SHA-256, 600k iterations)
     * 使用 OWASP 推荐参数创建 PBKDF2 哈希器（SHA-256，60 万次迭代）
     *
     * @return new Pbkdf2Hash instance
     */
    public static Pbkdf2Hash owaspRecommended() {
        return sha256();
    }

    /**
     * Create a builder for custom PBKDF2 configuration
     * 创建用于自定义 PBKDF2 配置的构建器
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String hash(char[] password) {
        Objects.requireNonNull(password, "Password cannot be null");
        if (password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            byte[] salt = RandomBytes.generate(saltLength, secureRandom);
            byte[] hash = generatePbkdf2Hash(password, salt);

            return encodeHash(salt, hash);
        } catch (Exception e) {
            throw new OpenCryptoException(getAlgorithmName(), "hash", "Failed to hash password", e);
        }
    }

    @Override
    public String hash(String password) {
        Objects.requireNonNull(password, "Password cannot be null");
        char[] chars = password.toCharArray();
        try {
            return hash(chars);
        } finally {
            PasswordHash.secureErase(chars);
        }
    }

    @Override
    public boolean verify(char[] password, String hash) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(hash, "Hash cannot be null");

        try {
            HashComponents components = parseHash(hash);

            byte[] computedHash = generatePbkdf2HashWithParams(
                    password,
                    components.salt,
                    components.algorithm,
                    components.iterations,
                    components.keyLength
            );

            return constantTimeEquals(computedHash, components.hash);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean verify(String password, String hash) {
        Objects.requireNonNull(password, "Password cannot be null");
        char[] chars = password.toCharArray();
        try {
            return verify(chars, hash);
        } finally {
            PasswordHash.secureErase(chars);
        }
    }

    @Override
    public boolean needsRehash(String hash) {
        Objects.requireNonNull(hash, "Hash cannot be null");

        try {
            HashComponents components = parseHash(hash);
            return !components.algorithm.equals(getAlgorithmIdentifier())
                    || components.iterations != this.iterations
                    || components.keyLength != this.keyLength;
        } catch (Exception e) {
            return true;
        }
    }

    private byte[] generatePbkdf2Hash(char[] password, byte[] salt) {
        return generatePbkdf2HashWithParams(password, salt, getAlgorithmIdentifier(), iterations, keyLength);
    }

    private byte[] generatePbkdf2HashWithParams(char[] password, byte[] salt, String algorithmId, int iters, int keyLen) {
        PBEKeySpec spec = null;
        try {
            String jdkAlgorithm = getJdkAlgorithmName(algorithmId);
            spec = new PBEKeySpec(password, salt, iters, keyLen);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(jdkAlgorithm);

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new OpenCryptoException(getAlgorithmName(), "initialization", "Algorithm not available: " + algorithmId, e);
        } catch (InvalidKeySpecException e) {
            throw new OpenCryptoException(getAlgorithmName(), "hash", "Failed to generate PBKDF2 hash", e);
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    private String encodeHash(byte[] salt, byte[] hash) {
        // Format: $pbkdf2-sha256$i=600000$salt$hash
        String saltEncoded = OpenBase64.encodeNoPadding(salt);
        String hashEncoded = OpenBase64.encodeNoPadding(hash);

        return String.format("$pbkdf2-%s$i=%d$%s$%s",
                getAlgorithmIdentifier(),
                iterations,
                saltEncoded,
                hashEncoded);
    }

    private HashComponents parseHash(String hash) {
        String[] parts = hash.split("\\$");
        if (parts.length != 5 || !parts[1].startsWith("pbkdf2-")) {
            throw new OpenCryptoException(ALGORITHM_PREFIX, "parse", "Invalid PBKDF2 hash format");
        }

        // Extract algorithm identifier (e.g., "sha256" from "pbkdf2-sha256")
        String algorithmId = parts[1].substring(7);

        // Parse iterations (i=600000)
        String iterStr = parts[2];
        if (!iterStr.startsWith("i=")) {
            throw new OpenCryptoException(ALGORITHM_PREFIX, "parse", "Invalid iterations format");
        }
        int iters = Integer.parseInt(iterStr.substring(2));

        // Decode salt and hash
        byte[] salt = OpenBase64.decode(parts[3]);
        byte[] hashBytes = OpenBase64.decode(parts[4]);

        int keyLen = hashBytes.length * 8; // Convert bytes to bits

        return new HashComponents(algorithmId, iters, keyLen, salt, hashBytes);
    }

    private String getAlgorithmName() {
        return ALGORITHM_PREFIX + "WithHmac" + algorithm.name();
    }

    private String getAlgorithmIdentifier() {
        return switch (algorithm) {
            case SHA1 -> "sha1";
            case SHA256 -> "sha256";
            case SHA384 -> "sha384";
            case SHA512 -> "sha512";
            default -> throw new OpenCryptoException(ALGORITHM_PREFIX, "initialization",
                    "Unsupported digest algorithm for PBKDF2: " + algorithm);
        };
    }

    private String getJdkAlgorithmName(String algorithmId) {
        return switch (algorithmId) {
            case "sha1" -> "PBKDF2WithHmacSHA1";
            case "sha256" -> "PBKDF2WithHmacSHA256";
            case "sha384" -> "PBKDF2WithHmacSHA384";
            case "sha512" -> "PBKDF2WithHmacSHA512";
            default -> throw new OpenCryptoException(ALGORITHM_PREFIX, "initialization",
                    "Unknown algorithm identifier: " + algorithmId);
        };
    }

    /**
     * Constant-time byte array comparison to prevent timing attacks.
     * Delegates to CryptoUtil which uses ConstantTimeUtil for a safe implementation
     * that does not leak length information via early return.
     * 恒定时间字节数组比较以防止时序攻击。
     * 委托给 CryptoUtil，它使用 ConstantTimeUtil 的安全实现，不会通过提前返回泄漏长度信息。
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        return CryptoUtil.constantTimeEquals(a, b);
    }

    private record HashComponents(String algorithm, int iterations, int keyLength, byte[] salt, byte[] hash) {}

    @Override
    public String getAlgorithm() {
        return getAlgorithmName();
    }

    /**
     * Builder for creating custom Pbkdf2Hash instances
     * 用于创建自定义 Pbkdf2Hash 实例的构建器
     */
    public static class Builder {
        private DigestAlgorithm algorithm = DigestAlgorithm.SHA256;
        private int iterations = DEFAULT_ITERATIONS;
        private int keyLength = DEFAULT_KEY_LENGTH;
        private int saltLength = DEFAULT_SALT_LENGTH;
        private SecureRandom secureRandom = new SecureRandom();

        private Builder() {}

        /**
         * Set the digest algorithm
         * 设置摘要算法
         *
         * @param algorithm the digest algorithm (SHA256, SHA384, or SHA512 recommended)
         * @return this builder
         */
        public Builder algorithm(DigestAlgorithm algorithm) {
            this.algorithm = Objects.requireNonNull(algorithm, "Algorithm cannot be null");
            if (algorithm != DigestAlgorithm.SHA1 &&
                algorithm != DigestAlgorithm.SHA256 &&
                algorithm != DigestAlgorithm.SHA384 &&
                algorithm != DigestAlgorithm.SHA512) {
                throw new IllegalArgumentException("Only SHA1, SHA256, SHA384, and SHA512 are supported for PBKDF2");
            }
            return this;
        }

        /**
         * Set the number of iterations
         * 设置迭代次数
         *
         * @param iterations iteration count (minimum 10000, recommended 600000 for SHA256)
         * @return this builder
         */
        public Builder iterations(int iterations) {
            if (iterations < 10000) {
                throw new IllegalArgumentException("Iterations must be at least 10,000 (recommended 600,000+)");
            }
            this.iterations = iterations;
            return this;
        }

        /**
         * Set the key output length in bits
         * 设置密钥输出长度（位）
         *
         * @param length key length in bits (minimum 128, recommended 256)
         * @return this builder
         */
        public Builder keyLength(int length) {
            if (length < 128) {
                throw new IllegalArgumentException("Key length must be at least 128 bits");
            }
            this.keyLength = length;
            return this;
        }

        /**
         * Set the salt length in bytes
         * 设置盐长度（字节）
         *
         * @param length salt length in bytes (minimum 8, recommended 16)
         * @return this builder
         */
        public Builder saltLength(int length) {
            if (length < 8) {
                throw new IllegalArgumentException("Salt length must be at least 8 bytes");
            }
            this.saltLength = length;
            return this;
        }

        /**
         * Set custom SecureRandom instance
         * 设置自定义 SecureRandom 实例
         *
         * @param secureRandom the SecureRandom instance
         * @return this builder
         */
        public Builder secureRandom(SecureRandom secureRandom) {
            this.secureRandom = Objects.requireNonNull(secureRandom, "SecureRandom cannot be null");
            return this;
        }

        /**
         * Build the Pbkdf2Hash instance
         * 构建 Pbkdf2Hash 实例
         *
         * @return new Pbkdf2Hash instance
         */
        public Pbkdf2Hash build() {
            return new Pbkdf2Hash(algorithm, iterations, keyLength, saltLength, secureRandom);
        }
    }
}
