package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.password.Argon2Type;
import cloud.opencode.base.crypto.util.CryptoUtil;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Argon2 key derivation function implementation - Winner of the Password Hashing Competition (PHC)
 * Argon2 密钥派生函数实现 - 密码哈希竞赛（PHC）获胜者
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Argon2 key derivation - Argon2 密钥派生</li>
 *   <li>Memory-hard function - 内存密集型函数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Argon2Kdf kdf = Argon2Kdf.argon2id();
 * byte[] key = kdf.deriveKey(password, salt, 32);
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
public final class Argon2Kdf implements KdfEngine {

    private static final String ALGORITHM_NAME = "Argon2";
    private static final int DEFAULT_MEMORY_KB = 65536;   // 64 MB (OWASP recommended minimum)
    private static final int DEFAULT_ITERATIONS = 3;       // OWASP recommended
    private static final int DEFAULT_PARALLELISM = 4;      // OWASP recommended
    private static final int DEFAULT_SALT_LENGTH = 16;
    private static final int MIN_ITERATIONS = 1;
    private static final int MIN_MEMORY = 8;              // Minimum 8 KB
    private static final int MIN_PARALLELISM = 1;

    private final Argon2Type type;
    private final int memoryKB;
    private final int iterations;
    private final int parallelism;

    private Argon2Kdf(Argon2Type type, int memoryKB, int iterations, int parallelism) {
        validateParameters(iterations, memoryKB, parallelism);
        this.type = type;
        this.memoryKB = memoryKB;
        this.iterations = iterations;
        this.parallelism = parallelism;
    }

    /**
     * Creates Argon2id instance with default OWASP recommended parameters
     * 创建使用 OWASP 推荐默认参数的 Argon2id 实例
     *
     * @return new Argon2Kdf instance using Argon2id
     */
    public static Argon2Kdf argon2id() {
        return new Argon2Kdf(Argon2Type.ARGON2ID, DEFAULT_MEMORY_KB, DEFAULT_ITERATIONS, DEFAULT_PARALLELISM);
    }

    /**
     * Creates Argon2d instance with default parameters
     * 创建使用默认参数的 Argon2d 实例
     *
     * @return new Argon2Kdf instance using Argon2d
     */
    public static Argon2Kdf argon2d() {
        return new Argon2Kdf(Argon2Type.ARGON2D, DEFAULT_MEMORY_KB, DEFAULT_ITERATIONS, DEFAULT_PARALLELISM);
    }

    /**
     * Creates Argon2i instance with default parameters
     * 创建使用默认参数的 Argon2i 实例
     *
     * @return new Argon2Kdf instance using Argon2i
     */
    public static Argon2Kdf argon2i() {
        return new Argon2Kdf(Argon2Type.ARGON2I, DEFAULT_MEMORY_KB, DEFAULT_ITERATIONS, DEFAULT_PARALLELISM);
    }

    /**
     * Creates a builder for Argon2Kdf configuration
     * 创建 Argon2Kdf 配置构建器
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Argon2Kdf configuration
     * Argon2Kdf 配置构建器
     */
    public static class Builder {
        private Argon2Type type = Argon2Type.ARGON2ID;
        private int memoryKB = DEFAULT_MEMORY_KB;
        private int iterations = DEFAULT_ITERATIONS;
        private int parallelism = DEFAULT_PARALLELISM;

        private Builder() {
        }

        /**
         * Sets the memory size in kilobytes
         * 设置内存大小（千字节）
         *
         * @param memoryKB memory in KB (must be at least 8 * parallelism)
         * @return this builder
         */
        public Builder memory(int memoryKB) {
            this.memoryKB = memoryKB;
            return this;
        }

        /**
         * Sets the number of iterations
         * 设置迭代次数
         *
         * @param iterations number of iterations (must be positive)
         * @return this builder
         */
        public Builder iterations(int iterations) {
            this.iterations = iterations;
            return this;
        }

        /**
         * Sets the parallelism factor
         * 设置并行度因子
         *
         * @param parallelism degree of parallelism (must be positive)
         * @return this builder
         */
        public Builder parallelism(int parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        /**
         * Sets the Argon2 type
         * 设置 Argon2 类型
         *
         * @param type the Argon2 variant to use
         * @return this builder
         */
        public Builder type(Argon2Type type) {
            Objects.requireNonNull(type, "Argon2Type cannot be null");
            this.type = type;
            return this;
        }

        /**
         * Builds the Argon2Kdf instance
         * 构建 Argon2Kdf 实例
         *
         * @return new Argon2Kdf instance
         * @throws IllegalArgumentException if parameters are invalid
         */
        public Argon2Kdf build() {
            return new Argon2Kdf(type, memoryKB, iterations, parallelism);
        }
    }

    /**
     * Check if Bouncy Castle provider is available.
     * 检查 Bouncy Castle 提供商是否可用。
     *
     * @return true if available / 如果可用则返回 true
     */
    public static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.Argon2BytesGenerator");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void ensureBouncyCastleAvailable() {
        if (!isBouncyCastleAvailable()) {
            throw new OpenCryptoException(
                "Argon2 requires Bouncy Castle provider. Add dependency: org.bouncycastle:bcprov-jdk18on"
            );
        }
    }

    /**
     * Derives a key from password and salt using Argon2 algorithm
     * 使用 Argon2 算法从密码和盐值派生密钥
     *
     * @param password the password as byte array
     * @param salt     the salt value (recommended minimum 16 bytes)
     * @param length   the desired output key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if length or salt length is invalid
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(byte[] password, byte[] salt, int length) {
        return deriveKey(password, salt, null, null, length);
    }

    /**
     * Derives a key from password and salt with optional additional data and secret
     * 使用可选的附加数据和密钥从密码和盐值派生密钥
     *
     * @param password the password as byte array
     * @param salt     the salt value (recommended minimum 16 bytes)
     * @param secret   optional secret value (can be null)
     * @param ad       optional additional data (can be null)
     * @param length   the desired output key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if length or salt length is invalid
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(byte[] password, byte[] salt, byte[] secret, byte[] ad, int length) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(salt, "Salt cannot be null");

        if (salt.length < DEFAULT_SALT_LENGTH) {
            throw new IllegalArgumentException(
                    "Salt length should be at least " + DEFAULT_SALT_LENGTH + " bytes");
        }

        if (length <= 0) {
            throw new IllegalArgumentException("Output length must be positive");
        }

        try {
            // Ensure Bouncy Castle is available
            ensureBouncyCastleAvailable();

            // Use reflection to call Bouncy Castle Argon2 implementation
            Class<?> argon2ParamsBuilderClass = Class.forName("org.bouncycastle.crypto.params.Argon2Parameters$Builder");
            Class<?> argon2ParamsClass = Class.forName("org.bouncycastle.crypto.params.Argon2Parameters");
            Class<?> argon2GeneratorClass = Class.forName("org.bouncycastle.crypto.generators.Argon2BytesGenerator");

            // Build Argon2 parameters
            Object builder = argon2ParamsBuilderClass.getConstructor(int.class).newInstance(type.getTypeId());
            argon2ParamsBuilderClass.getMethod("withSalt", byte[].class).invoke(builder, (Object) salt);
            argon2ParamsBuilderClass.getMethod("withIterations", int.class).invoke(builder, iterations);
            argon2ParamsBuilderClass.getMethod("withMemoryAsKB", int.class).invoke(builder, memoryKB);
            argon2ParamsBuilderClass.getMethod("withParallelism", int.class).invoke(builder, parallelism);

            // Add optional secret and additional data
            if (secret != null && secret.length > 0) {
                argon2ParamsBuilderClass.getMethod("withSecret", byte[].class).invoke(builder, (Object) secret);
            }
            if (ad != null && ad.length > 0) {
                argon2ParamsBuilderClass.getMethod("withAdditional", byte[].class).invoke(builder, (Object) ad);
            }

            Object params = argon2ParamsBuilderClass.getMethod("build").invoke(builder);

            // Generate key
            Object generator = argon2GeneratorClass.getDeclaredConstructor().newInstance();
            argon2GeneratorClass.getMethod("init", argon2ParamsClass).invoke(generator, params);

            byte[] derivedKey = new byte[length];
            argon2GeneratorClass.getMethod("generateBytes", byte[].class, byte[].class).invoke(generator, password, derivedKey);

            return derivedKey;
        } catch (ClassNotFoundException e) {
            throw new OpenCryptoException(ALGORITHM_NAME, "derive",
                    "Argon2 requires Bouncy Castle provider. Add dependency: org.bouncycastle:bcprov-jdk18on", e);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM_NAME, "derive",
                    "Argon2 derivation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Derives a key from password (as char array) and salt
     * 从密码（字符数组）和盐值派生密钥
     *
     * @param password the password as char array
     * @param salt     the salt value (recommended minimum 16 bytes)
     * @param length   the desired output key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if length or salt length is invalid
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(char[] password, byte[] salt, int length) {
        Objects.requireNonNull(password, "Password cannot be null");

        // Convert char[] to byte[] using proper UTF-8 encoding.
        // Note: An intermediate String is created for encoding. Java does not provide
        // a way to zero the String's internal byte array, so the password may remain
        // in memory until garbage collected. For maximum security, prefer the
        // deriveKey(byte[], byte[], int) overload with pre-encoded UTF-8 bytes.
        byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);

        try {
            return deriveKey(passwordBytes, salt, length);
        } finally {
            // Securely erase password bytes
            CryptoUtil.secureErase(passwordBytes);
        }
    }

    /**
     * Derives a key from password string and salt
     * 从密码字符串和盐值派生密钥
     *
     * @param password the password as string
     * @param salt     the salt value (recommended minimum 16 bytes)
     * @param length   the desired output key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if length or salt length is invalid
     * @throws OpenCryptoException      if derivation fails
     */
    public byte[] deriveKey(String password, byte[] salt, int length) {
        Objects.requireNonNull(password, "Password cannot be null");
        char[] passwordChars = password.toCharArray();
        try {
            return deriveKey(passwordChars, salt, length);
        } finally {
            // Securely erase password characters
            CryptoUtil.secureErase(passwordChars);
        }
    }

    /**
     * Generates a cryptographically secure random salt
     * 生成密码学安全的随机盐值
     *
     * @return random salt of default length (16 bytes)
     */
    public static byte[] generateSalt() {
        return CryptoUtil.randomSalt(DEFAULT_SALT_LENGTH);
    }

    /**
     * Generates a cryptographically secure random salt with custom length
     * 生成自定义长度的密码学安全随机盐值
     *
     * @param length the desired salt length in bytes
     * @return random salt of specified length
     * @throws IllegalArgumentException if length is less than minimum
     */
    public static byte[] generateSalt(int length) {
        if (length < DEFAULT_SALT_LENGTH) {
            throw new IllegalArgumentException(
                    "Salt length should be at least " + DEFAULT_SALT_LENGTH + " bytes");
        }
        return CryptoUtil.randomSalt(length);
    }

    /**
     * Validates Argon2 parameters
     * 验证 Argon2 参数
     *
     * @param iterations  time cost parameter
     * @param memory      memory cost in KB
     * @param parallelism degree of parallelism
     * @throws IllegalArgumentException if parameters are invalid
     */
    private static void validateParameters(int iterations, int memory, int parallelism) {
        if (iterations < MIN_ITERATIONS) {
            throw new IllegalArgumentException(
                    "Iterations must be at least " + MIN_ITERATIONS);
        }
        if (memory < MIN_MEMORY) {
            throw new IllegalArgumentException(
                    "Memory must be at least " + MIN_MEMORY + " KB (" + (MIN_MEMORY / 1024) + " MB)");
        }
        if (parallelism < MIN_PARALLELISM) {
            throw new IllegalArgumentException(
                    "Parallelism must be at least " + MIN_PARALLELISM);
        }
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM_NAME + "-" + type.name();
    }

    /**
     * Gets the Argon2 variant type
     * 获取 Argon2 变体类型
     *
     * @return Argon2 type
     */
    public Argon2Type getType() {
        return type;
    }

    /**
     * Gets the time cost parameter (iterations)
     * 获取时间成本参数（迭代次数）
     *
     * @return iterations parameter
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Gets the memory cost parameter in KB
     * 获取内存成本参数（KB）
     *
     * @return memory parameter in KB
     */
    public int getMemory() {
        return memoryKB;
    }

    /**
     * Gets the parallelism parameter
     * 获取并行度参数
     *
     * @return parallelism parameter
     */
    public int getParallelism() {
        return parallelism;
    }

    /**
     * Gets the default salt length
     * 获取默认盐值长度
     *
     * @return default salt length in bytes
     */
    public static int getDefaultSaltLength() {
        return DEFAULT_SALT_LENGTH;
    }

    /**
     * Gets the minimum memory requirement in KB
     * 获取最小内存需求（KB）
     *
     * @return minimum memory in KB
     */
    public static int getMinMemory() {
        return MIN_MEMORY;
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, byte[] salt, byte[] info, int length) {
        // Use info as additional data in Argon2
        return deriveKey(inputKeyMaterial, salt, null, info, length);
    }

    @Override
    public byte[] derive(byte[] inputKeyMaterial, int length) {
        // Generate random salt for derive with default parameters
        byte[] salt = generateSalt();
        return deriveKey(inputKeyMaterial, salt, length);
    }
}
