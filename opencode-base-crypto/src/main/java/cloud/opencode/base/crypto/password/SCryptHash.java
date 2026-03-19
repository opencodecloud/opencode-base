package cloud.opencode.base.crypto.password;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.random.RandomBytes;
import cloud.opencode.base.crypto.util.CryptoUtil;
import cloud.opencode.base.crypto.util.SecureEraser;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * SCrypt password hashing implementation - Memory-hard password hashing using scrypt algorithm (requires Bouncy Castle)
 * SCrypt 密码哈希实现 - 使用 scrypt 算法的内存困难密码哈希（需要 Bouncy Castle）
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SCrypt password hashing - SCrypt 密码哈希</li>
 *   <li>Memory-hard key derivation - 内存密集型密钥派生</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SCryptHash scrypt = SCryptHash.of();
 * String hash = scrypt.hash("password");
 * boolean valid = scrypt.verify("password", hash);
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
 *   <li>Time complexity: O(N * r * p) - 时间复杂度: O(N * r * p)，N/r/p为参数</li>
 *   <li>Space complexity: O(N * r) - 空间复杂度: O(N * r)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class SCryptHash implements PasswordHash {

    private static final String ALGORITHM = "SCrypt";
    private static final boolean BOUNCY_CASTLE_AVAILABLE = isBouncyCastleAvailable();

    // OWASP recommended parameters (2023)
    private static final int DEFAULT_WORK_FACTOR = 32768; // N (CPU/memory cost)
    private static final int DEFAULT_BLOCK_SIZE = 8;      // r (block size)
    private static final int DEFAULT_PARALLELISM = 1;     // p (parallelization)
    private static final int DEFAULT_KEY_LENGTH = 32;     // 32 bytes = 256 bits
    private static final int DEFAULT_SALT_LENGTH = 16;    // 16 bytes = 128 bits

    private final int workFactor;
    private final int blockSize;
    private final int parallelism;
    private final int keyLength;
    private final int saltLength;
    private final SecureRandom secureRandom;

    private SCryptHash(int workFactor, int blockSize, int parallelism, int keyLength, int saltLength, SecureRandom secureRandom) {
        this.workFactor = workFactor;
        this.blockSize = blockSize;
        this.parallelism = parallelism;
        this.keyLength = keyLength;
        this.saltLength = saltLength;
        this.secureRandom = secureRandom;
    }

    /**
     * Create SCrypt hasher with default parameters
     * 使用默认参数创建 SCrypt 哈希器
     *
     * @return new SCryptHash instance
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    public static SCryptHash of() {
        checkBouncyCastleAvailable();
        return new SCryptHash(DEFAULT_WORK_FACTOR, DEFAULT_BLOCK_SIZE, DEFAULT_PARALLELISM,
                DEFAULT_KEY_LENGTH, DEFAULT_SALT_LENGTH, new SecureRandom());
    }

    /**
     * Create SCrypt hasher with OWASP recommended parameters
     * 使用 OWASP 推荐参数创建 SCrypt 哈希器
     *
     * @return new SCryptHash instance
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    public static SCryptHash owaspRecommended() {
        return of(); // Already uses OWASP parameters
    }

    /**
     * Create a builder for custom SCrypt configuration
     * 创建用于自定义 SCrypt 配置的构建器
     *
     * @return new Builder instance
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    public static Builder builder() {
        checkBouncyCastleAvailable();
        return new Builder();
    }

    @Override
    public String hash(char[] password) {
        Objects.requireNonNull(password, "Password cannot be null");
        if (password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        byte[] passwordBytes = null;
        try {
            passwordBytes = charArrayToBytes(password);
            byte[] salt = RandomBytes.generate(saltLength, secureRandom);
            byte[] hash = generateSCryptHash(passwordBytes, salt);

            return encodeHash(salt, hash);
        } finally {
            SecureEraser.erase(passwordBytes);
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

        byte[] passwordBytes = null;
        try {
            HashComponents components = parseHash(hash);
            passwordBytes = charArrayToBytes(password);

            byte[] computedHash = generateSCryptHashWithParams(
                    passwordBytes,
                    components.salt,
                    components.workFactor,
                    components.blockSize,
                    components.parallelism
            );

            return constantTimeEquals(computedHash, components.hash);
        } catch (Exception e) {
            return false;
        } finally {
            SecureEraser.erase(passwordBytes);
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
            return components.workFactor != this.workFactor
                    || components.blockSize != this.blockSize
                    || components.parallelism != this.parallelism;
        } catch (Exception e) {
            return true;
        }
    }

    private byte[] generateSCryptHash(byte[] password, byte[] salt) {
        return generateSCryptHashWithParams(password, salt, workFactor, blockSize, parallelism);
    }

    private byte[] generateSCryptHashWithParams(byte[] password, byte[] salt, int n, int r, int p) {
        try {
            Class<?> scryptClass = Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            return (byte[]) scryptClass.getMethod("generate", byte[].class, byte[].class, int.class, int.class, int.class, int.class)
                    .invoke(null, password, salt, n, r, p, keyLength);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "hash", "Failed to generate SCrypt hash", e);
        }
    }

    private String encodeHash(byte[] salt, byte[] hash) {
        // Format: $scrypt$n=32768,r=8,p=1$salt$hash
        String saltEncoded = OpenBase64.encodeNoPadding(salt);
        String hashEncoded = OpenBase64.encodeNoPadding(hash);

        return String.format("$scrypt$n=%d,r=%d,p=%d$%s$%s",
                workFactor,
                blockSize,
                parallelism,
                saltEncoded,
                hashEncoded);
    }

    private HashComponents parseHash(String hash) {
        String[] parts = hash.split("\\$");
        if (parts.length != 5 || !parts[1].equals("scrypt")) {
            throw new OpenCryptoException(ALGORITHM, "parse", "Invalid SCrypt hash format");
        }

        // Parse parameters (n=32768,r=8,p=1)
        String[] params = parts[2].split(",");
        int n = Integer.parseInt(params[0].substring(2));
        int r = Integer.parseInt(params[1].substring(2));
        int p = Integer.parseInt(params[2].substring(2));

        byte[] salt = OpenBase64.decode(parts[3]);
        byte[] hashBytes = OpenBase64.decode(parts[4]);

        return new HashComponents(n, r, p, salt, hashBytes);
    }

    private byte[] charArrayToBytes(char[] chars) {
        return new String(chars).getBytes(StandardCharsets.UTF_8);
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

    private static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void checkBouncyCastleAvailable() {
        if (!BOUNCY_CASTLE_AVAILABLE) {
            throw new OpenCryptoException(ALGORITHM, "initialization",
                    "Bouncy Castle library is required for SCrypt. Add bcprov-jdk18on dependency.");
        }
    }

    private record HashComponents(int workFactor, int blockSize, int parallelism, byte[] salt, byte[] hash) {}

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    /**
     * Builder for creating custom SCryptHash instances
     * 用于创建自定义 SCryptHash 实例的构建器
     */
    public static class Builder {
        private int workFactor = DEFAULT_WORK_FACTOR;
        private int blockSize = DEFAULT_BLOCK_SIZE;
        private int parallelism = DEFAULT_PARALLELISM;
        private int keyLength = DEFAULT_KEY_LENGTH;
        private SecureRandom secureRandom = new SecureRandom();

        private Builder() {}

        /**
         * Set CPU/memory cost parameter (N)
         * 设置 CPU/内存成本参数（N）
         *
         * @param n work factor (must be power of 2, minimum 2)
         * @return this builder
         */
        public Builder workFactor(int n) {
            if (n < 2 || (n & (n - 1)) != 0) {
                throw new IllegalArgumentException("Work factor must be a power of 2 and at least 2");
            }
            this.workFactor = n;
            return this;
        }

        /**
         * Set block size parameter (r)
         * 设置块大小参数（r）
         *
         * @param r block size (minimum 1)
         * @return this builder
         */
        public Builder blockSize(int r) {
            if (r < 1) {
                throw new IllegalArgumentException("Block size must be at least 1");
            }
            this.blockSize = r;
            return this;
        }

        /**
         * Set parallelization parameter (p)
         * 设置并行化参数（p）
         *
         * @param p parallelism (minimum 1)
         * @return this builder
         */
        public Builder parallelism(int p) {
            if (p < 1) {
                throw new IllegalArgumentException("Parallelism must be at least 1");
            }
            this.parallelism = p;
            return this;
        }

        /**
         * Set key output length in bytes
         * 设置密钥输出长度（字节）
         *
         * @param length key length in bytes (minimum 16)
         * @return this builder
         */
        public Builder keyLength(int length) {
            if (length < 16) {
                throw new IllegalArgumentException("Key length must be at least 16 bytes");
            }
            this.keyLength = length;
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
         * Build the SCryptHash instance
         * 构建 SCryptHash 实例
         *
         * @return new SCryptHash instance
         */
        public SCryptHash build() {
            return new SCryptHash(workFactor, blockSize, parallelism, keyLength, DEFAULT_SALT_LENGTH, secureRandom);
        }
    }
}
