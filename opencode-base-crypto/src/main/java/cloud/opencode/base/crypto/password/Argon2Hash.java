package cloud.opencode.base.crypto.password;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.random.RandomBytes;
import cloud.opencode.base.crypto.util.CryptoUtil;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Argon2 password hashing implementation - Modern memory-hard password hashing algorithm (requires Bouncy Castle)
 * Argon2 密码哈希实现 - 现代内存困难密码哈希算法（需要 Bouncy Castle）
 *
 * <p>Argon2 is a modern password hashing algorithm that won the Password Hashing Competition (PHC) in 2015.
 * It is designed to be resistant to GPU cracking attacks and side-channel attacks. This implementation
 * supports all three variants: Argon2d, Argon2i, and Argon2id (recommended).
 *
 * <p>Argon2 是一种现代密码哈希算法，在 2015 年赢得密码哈希竞赛（PHC）。
 * 它被设计为抵抗 GPU 破解攻击和旁道攻击。此实现支持所有三种变体：Argon2d、Argon2i 和 Argon2id（推荐）。
 *
 * <p><strong>Important:</strong> This implementation requires Bouncy Castle library (bcprov-jdk18on).
 * If Bouncy Castle is not available, operations will throw {@link OpenCryptoException}.
 *
 * <p><strong>重要：</strong>此实现需要 Bouncy Castle 库（bcprov-jdk18on）。
 * 如果 Bouncy Castle 不可用，操作将抛出 {@link OpenCryptoException}。
 *
 * <p>Usage example:
 * <pre>{@code
 * // Create Argon2id hasher with default parameters (recommended)
 * PasswordHash hasher = Argon2Hash.argon2id();
 *
 * // Hash a password
 * String hash = hasher.hash("myPassword123");
 *
 * // Verify a password
 * boolean valid = hasher.verify("myPassword123", hash);
 *
 * // Check if rehashing is needed
 * if (hasher.needsRehash(hash)) {
 *     String newHash = hasher.hash("myPassword123");
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Argon2id, Argon2i, Argon2d variants - Argon2id、Argon2i、Argon2d 变体</li>
 *   <li>Memory-hard password hashing - 内存密集型密码哈希</li>
 *   <li>Configurable time/memory/parallelism - 可配置时间/内存/并行度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Argon2Hash argon2 = Argon2Hash.argon2id();
 * String hash = argon2.hash("password");
 * boolean valid = argon2.verify("password", hash);
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
 *   <li>Time complexity: O(memory * iterations) - 时间复杂度: O(memory * iterations)，memory为内存参数</li>
 *   <li>Space complexity: O(memory) - 空间复杂度: O(memory)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class Argon2Hash implements PasswordHash {

    // Default parameters based on OWASP recommendations (2023)
    // 基于 OWASP 推荐的默认参数（2023）
    private static final int DEFAULT_SALT_LENGTH = 16; // 16 bytes = 128 bits
    private static final int DEFAULT_HASH_LENGTH = 32; // 32 bytes = 256 bits
    private static final int DEFAULT_ITERATIONS = 3; // Time cost parameter
    private static final int DEFAULT_MEMORY = 65536; // Memory cost in KB (64 MB)
    private static final int DEFAULT_PARALLELISM = 4; // Number of parallel threads

    // Hash format pattern: $argon2id$v=19$m=65536,t=3,p=4$salt$hash
    // 哈希格式模式：$argon2id$v=19$m=65536,t=3,p=4$salt$hash
    private static final Pattern HASH_PATTERN = Pattern.compile(
        "^\\$(argon2(?:d|i|id))\\$v=(\\d+)\\$m=(\\d+),t=(\\d+),p=(\\d+)\\$([A-Za-z0-9+/]+)\\$([A-Za-z0-9+/]+)$"
    );

    private final Argon2Type type;
    private final int saltLength;
    private final int hashLength;
    private final int iterations;
    private final int memory;
    private final int parallelism;
    private final SecureRandom secureRandom;

    /**
     * Private constructor for creating Argon2 hasher with custom parameters
     * 使用自定义参数创建 Argon2 哈希器的私有构造函数
     */
    private Argon2Hash(Argon2Type type, int saltLength, int hashLength, int iterations,
                       int memory, int parallelism, SecureRandom secureRandom) {
        this.type = Objects.requireNonNull(type, "Argon2 type cannot be null");
        this.saltLength = validatePositive(saltLength, "Salt length");
        this.hashLength = validatePositive(hashLength, "Hash length");
        this.iterations = validatePositive(iterations, "Iterations");
        this.memory = validatePositive(memory, "Memory");
        this.parallelism = validatePositive(parallelism, "Parallelism");
        this.secureRandom = Objects.requireNonNull(secureRandom, "SecureRandom cannot be null");
    }

    /**
     * Create Argon2id hasher with default parameters (recommended for most use cases)
     * 使用默认参数创建 Argon2id 哈希器（推荐用于大多数用例）
     *
     * @return Argon2id password hasher
     */
    public static Argon2Hash argon2id() {
        return builder().type(Argon2Type.ARGON2ID).build();
    }

    /**
     * Create Argon2i hasher with default parameters (resistant to side-channel attacks)
     * 使用默认参数创建 Argon2i 哈希器（抵抗旁道攻击）
     *
     * @return Argon2i password hasher
     */
    public static Argon2Hash argon2i() {
        return builder().type(Argon2Type.ARGON2I).build();
    }

    /**
     * Create Argon2d hasher with default parameters (resistant to GPU attacks)
     * 使用默认参数创建 Argon2d 哈希器（抵抗 GPU 攻击）
     *
     * @return Argon2d password hasher
     */
    public static Argon2Hash argon2d() {
        return builder().type(Argon2Type.ARGON2D).build();
    }

    /**
     * Create a builder for custom Argon2 configuration
     * 创建用于自定义 Argon2 配置的构建器
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String hash(char[] password) {
        Objects.requireNonNull(password, "Password cannot be null");

        try {
            // Check if Bouncy Castle is available
            // 检查 Bouncy Castle 是否可用
            checkBouncyCastleAvailability();

            // Generate random salt
            // 生成随机盐值
            byte[] salt = RandomBytes.generate(saltLength, secureRandom);

            // Convert password to bytes
            // 将密码转换为字节
            byte[] passwordBytes = charsToBytes(password);

            try {
                // Perform Argon2 hashing using Bouncy Castle
                // 使用 Bouncy Castle 执行 Argon2 哈希
                byte[] hash = argon2Hash(passwordBytes, salt);

                // Encode to PHC string format
                // 编码为 PHC 字符串格式
                return encodeHash(salt, hash);
            } finally {
                // Securely erase password bytes
                // 安全擦除密码字节
                java.util.Arrays.fill(passwordBytes, (byte) 0);
            }
        } catch (Exception e) {
            throw new OpenCryptoException("Argon2", "hash",
                "Failed to hash password: " + e.getMessage(), e);
        }
    }

    @Override
    public String hash(String password) {
        Objects.requireNonNull(password, "Password cannot be null");
        char[] passwordChars = password.toCharArray();
        try {
            return hash(passwordChars);
        } finally {
            CryptoUtil.secureErase(passwordChars);
        }
    }

    @Override
    public boolean verify(char[] password, String hash) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(hash, "Hash cannot be null");

        try {
            // Parse the hash string
            // 解析哈希字符串
            HashParameters params = parseHash(hash);

            // Convert password to bytes
            // 将密码转换为字节
            byte[] passwordBytes = charsToBytes(password);

            try {
                // Hash the password with the same parameters
                // 使用相同参数哈希密码
                byte[] computedHash = argon2Hash(passwordBytes, params.salt,
                    params.iterations, params.memory, params.parallelism);

                // Constant-time comparison to prevent timing attacks
                // 恒定时间比较以防止时序攻击
                return constantTimeEquals(computedHash, params.hash);
            } finally {
                // Securely erase password bytes
                // 安全擦除密码字节
                java.util.Arrays.fill(passwordBytes, (byte) 0);
            }
        } catch (Exception e) {
            // Don't reveal whether hash parsing failed or verification failed
            // 不要透露是哈希解析失败还是验证失败
            return false;
        }
    }

    @Override
    public boolean verify(String password, String hash) {
        Objects.requireNonNull(password, "Password cannot be null");
        char[] passwordChars = password.toCharArray();
        try {
            return verify(passwordChars, hash);
        } finally {
            CryptoUtil.secureErase(passwordChars);
        }
    }

    @Override
    public boolean needsRehash(String hash) {
        Objects.requireNonNull(hash, "Hash cannot be null");

        try {
            HashParameters params = parseHash(hash);

            // Check if parameters match current configuration
            // 检查参数是否匹配当前配置
            return !params.type.equals(type.getAlgorithmName())
                || params.iterations != iterations
                || params.memory != memory
                || params.parallelism != parallelism
                || params.hash.length != hashLength;
        } catch (Exception e) {
            // If we can't parse the hash, it definitely needs rehashing
            // 如果无法解析哈希，则肯定需要重新哈希
            return true;
        }
    }

    /**
     * Check if Bouncy Castle library is available on the classpath
     * 检查 Bouncy Castle 库是否在类路径中可用
     *
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    private void checkBouncyCastleAvailability() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.Argon2BytesGenerator");
        } catch (ClassNotFoundException e) {
            throw new OpenCryptoException("Argon2", "initialization",
                "Bouncy Castle library is required for Argon2 but not found on classpath. " +
                "Add dependency: org.bouncycastle:bcprov-jdk18on", e);
        }
    }

    /**
     * Perform Argon2 hashing using current instance parameters
     * 使用当前实例参数执行 Argon2 哈希
     */
    private byte[] argon2Hash(byte[] password, byte[] salt) {
        return argon2Hash(password, salt, iterations, memory, parallelism);
    }

    /**
     * Perform Argon2 hashing with specified parameters (uses Bouncy Castle via reflection)
     * 使用指定参数执行 Argon2 哈希（通过反射使用 Bouncy Castle）
     */
    private byte[] argon2Hash(byte[] password, byte[] salt, int iterations, int memory, int parallelism) {
        try {
            // Use reflection to avoid compile-time dependency on Bouncy Castle
            // 使用反射避免对 Bouncy Castle 的编译时依赖
            Class<?> argon2Class = Class.forName("org.bouncycastle.crypto.generators.Argon2BytesGenerator");
            Class<?> paramsClass = Class.forName("org.bouncycastle.crypto.params.Argon2Parameters");
            Class<?> builderClass = Class.forName("org.bouncycastle.crypto.params.Argon2Parameters$Builder");

            // Create Argon2Parameters.Builder
            // 创建 Argon2Parameters.Builder
            Object builder = builderClass.getConstructor(int.class).newInstance(type.getTypeId());
            builderClass.getMethod("withSalt", byte[].class).invoke(builder, salt);
            builderClass.getMethod("withIterations", int.class).invoke(builder, iterations);
            builderClass.getMethod("withMemoryAsKB", int.class).invoke(builder, memory);
            builderClass.getMethod("withParallelism", int.class).invoke(builder, parallelism);
            Object params = builderClass.getMethod("build").invoke(builder);

            // Create Argon2BytesGenerator and initialize
            // 创建 Argon2BytesGenerator 并初始化
            Object generator = argon2Class.getDeclaredConstructor().newInstance();
            argon2Class.getMethod("init", paramsClass).invoke(generator, params);

            // Generate hash
            // 生成哈希
            byte[] hash = new byte[hashLength];
            argon2Class.getMethod("generateBytes", char[].class, byte[].class)
                .invoke(generator, bytesToChars(password), hash);

            return hash;
        } catch (Exception e) {
            throw new OpenCryptoException("Argon2", "hash",
                "Failed to perform Argon2 hashing: " + e.getMessage(), e);
        }
    }

    /**
     * Encode salt and hash into PHC string format
     * 将盐值和哈希编码为 PHC 字符串格式
     *
     * <p>Format: $argon2id$v=19$m=65536,t=3,p=4$salt$hash
     */
    private String encodeHash(byte[] salt, byte[] hash) {
        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        return String.format("$%s$v=19$m=%d,t=%d,p=%d$%s$%s",
            type.getAlgorithmName(),
            memory,
            iterations,
            parallelism,
            encoder.encodeToString(salt),
            encoder.encodeToString(hash));
    }

    /**
     * Parse PHC string format hash into components
     * 将 PHC 字符串格式哈希解析为组件
     */
    private HashParameters parseHash(String hashString) {
        Matcher matcher = HASH_PATTERN.matcher(hashString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Argon2 hash format");
        }

        Base64.Decoder decoder = Base64.getDecoder();

        return new HashParameters(
            matcher.group(1), // type (argon2d, argon2i, or argon2id)
            Integer.parseInt(matcher.group(2)), // version
            Integer.parseInt(matcher.group(3)), // memory (m)
            Integer.parseInt(matcher.group(4)), // iterations (t)
            Integer.parseInt(matcher.group(5)), // parallelism (p)
            decoder.decode(matcher.group(6)), // salt
            decoder.decode(matcher.group(7))  // hash
        );
    }

    /**
     * Convert character array to byte array (UTF-8)
     * 将字符数组转换为字节数组（UTF-8）
     */
    private byte[] charsToBytes(char[] chars) {
        return new String(chars).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert byte array to character array (UTF-8)
     * 将字节数组转换为字符数组（UTF-8）
     */
    private char[] bytesToChars(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8).toCharArray();
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

    /**
     * Validate that a parameter is positive
     * 验证参数为正数
     */
    private static int validatePositive(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " must be positive, got: " + value);
        }
        return value;
    }

    /**
     * Builder for creating customized Argon2 hashers
     * 用于创建自定义 Argon2 哈希器的构建器
     */
    public static final class Builder {
        private Argon2Type type = Argon2Type.ARGON2ID;
        private int saltLength = DEFAULT_SALT_LENGTH;
        private int hashLength = DEFAULT_HASH_LENGTH;
        private int iterations = DEFAULT_ITERATIONS;
        private int memory = DEFAULT_MEMORY;
        private int parallelism = DEFAULT_PARALLELISM;
        private SecureRandom secureRandom = new SecureRandom();

        private Builder() {}

        /**
         * Set the Argon2 variant type
         * 设置 Argon2 变体类型
         *
         * @param type the Argon2 type (ARGON2D, ARGON2I, or ARGON2ID)
         * @return this builder
         */
        public Builder type(Argon2Type type) {
            this.type = Objects.requireNonNull(type, "Type cannot be null");
            return this;
        }

        /**
         * Set the salt length in bytes
         * 设置盐值长度（字节）
         *
         * @param saltLength salt length (recommended: 16 bytes minimum)
         * @return this builder
         */
        public Builder saltLength(int saltLength) {
            this.saltLength = saltLength;
            return this;
        }

        /**
         * Set the hash output length in bytes
         * 设置哈希输出长度（字节）
         *
         * @param hashLength hash length (recommended: 32 bytes)
         * @return this builder
         */
        public Builder hashLength(int hashLength) {
            this.hashLength = hashLength;
            return this;
        }

        /**
         * Set the number of iterations (time cost)
         * 设置迭代次数（时间成本）
         *
         * @param iterations iteration count (recommended: 3-4 for interactive use)
         * @return this builder
         */
        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        /**
         * Set the memory cost in kilobytes
         * 设置内存成本（千字节）
         *
         * @param memory memory in KB (recommended: 65536 KB = 64 MB)
         * @return this builder
         */
        public Builder memory(int memory) {
            this.memory = memory;
            return this;
        }

        /**
         * Set the parallelism factor (number of threads)
         * 设置并行因子（线程数）
         *
         * @param parallelism number of parallel threads (recommended: 4)
         * @return this builder
         */
        public Builder parallelism(int parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        /**
         * Set the SecureRandom instance for salt generation
         * 设置用于盐值生成的 SecureRandom 实例
         *
         * @param secureRandom the SecureRandom instance
         * @return this builder
         */
        public Builder secureRandom(SecureRandom secureRandom) {
            this.secureRandom = Objects.requireNonNull(secureRandom, "SecureRandom cannot be null");
            return this;
        }

        /**
         * Build the Argon2Hash instance
         * 构建 Argon2Hash 实例
         *
         * @return configured Argon2Hash instance
         */
        public Argon2Hash build() {
            return new Argon2Hash(type, saltLength, hashLength, iterations,
                                 memory, parallelism, secureRandom);
        }
    }

    @Override
    public String getAlgorithm() {
        return type.getAlgorithmName();
    }

    /**
     * Internal class for storing parsed hash parameters
     * 用于存储解析的哈希参数的内部类
     */
    private record HashParameters(
        String type,
        int version,
        int memory,
        int iterations,
        int parallelism,
        byte[] salt,
        byte[] hash
    ) {}
}
