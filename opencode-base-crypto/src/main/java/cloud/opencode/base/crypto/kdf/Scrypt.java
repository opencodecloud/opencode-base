package cloud.opencode.base.crypto.kdf;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.util.CryptoUtil;

import java.util.Objects;

/**
 * Scrypt key derivation function implementation - Memory-hard password-based KDF
 * Scrypt 密钥派生函数实现 - 内存困难的基于密码的 KDF
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SCrypt key derivation - SCrypt 密钥派生</li>
 *   <li>Memory-hard function - 内存密集型函数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Scrypt scrypt = Scrypt.create();
 * byte[] key = scrypt.deriveKey(password, salt, 32);
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
public final class Scrypt implements KdfEngine {

    private static final String ALGORITHM = "Scrypt";
    private static final int DEFAULT_N = 32768;  // 2^15 - CPU/memory cost parameter
    private static final int DEFAULT_R = 8;      // Block size parameter
    private static final int DEFAULT_P = 1;      // Parallelization parameter
    private static final int DEFAULT_SALT_LENGTH = 16;

    private final int n;  // CPU/memory cost parameter (must be power of 2)
    private final int r;  // Block size parameter
    private final int p;  // Parallelization parameter

    private Scrypt(int n, int r, int p) {
        validateParameters(n, r, p);
        this.n = n;
        this.r = r;
        this.p = p;
    }

    /**
     * Creates Scrypt instance with default parameters (N=32768, r=8, p=1)
     * 创建使用默认参数的 Scrypt 实例（N=32768，r=8，p=1）
     *
     * @return new Scrypt instance
     */
    public static Scrypt of() {
        return new Scrypt(DEFAULT_N, DEFAULT_R, DEFAULT_P);
    }

    /**
     * Creates Scrypt instance with custom parameters
     * 创建使用自定义参数的 Scrypt 实例
     *
     * @param n CPU/memory cost parameter (must be power of 2, greater than 1)
     * @param r block size parameter (must be positive)
     * @param p parallelization parameter (must be positive)
     * @return new Scrypt instance
     * @throws IllegalArgumentException if parameters are invalid
     */
    public static Scrypt of(int n, int r, int p) {
        return new Scrypt(n, r, p);
    }

    /**
     * Creates a builder for Scrypt configuration
     * 创建 Scrypt 配置构建器
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Scrypt configuration
     * Scrypt 配置构建器
     */
    public static class Builder {
        private int n = DEFAULT_N;
        private int r = DEFAULT_R;
        private int p = DEFAULT_P;

        private Builder() {
        }

        /**
         * Sets the CPU/memory cost parameter (N)
         * 设置 CPU/内存成本参数（N）
         *
         * @param n CPU/memory cost (must be power of 2, greater than 1)
         * @return this builder
         */
        public Builder workFactor(int n) {
            this.n = n;
            return this;
        }

        /**
         * Sets the block size parameter (r)
         * 设置块大小参数（r）
         *
         * @param r block size (must be positive)
         * @return this builder
         */
        public Builder blockSize(int r) {
            this.r = r;
            return this;
        }

        /**
         * Sets the parallelization parameter (p)
         * 设置并行化参数（p）
         *
         * @param p parallelization factor (must be positive)
         * @return this builder
         */
        public Builder parallelism(int p) {
            this.p = p;
            return this;
        }

        /**
         * Builds the Scrypt instance
         * 构建 Scrypt 实例
         *
         * @return new Scrypt instance
         * @throws IllegalArgumentException if parameters are invalid
         */
        public Scrypt build() {
            return new Scrypt(n, r, p);
        }
    }

    /**
     * Derives a key from a password and salt using Scrypt
     * 使用 Scrypt 从密码和盐值派生密钥
     *
     * @param password  the password as char array (will not be modified)
     * @param salt      the salt value
     * @param keyLength the desired key length in bytes
     * @return the derived key
     * @throws NullPointerException     if password or salt is null
     * @throws IllegalArgumentException if keyLength is not positive
     * @throws OpenCryptoException      if Bouncy Castle is not available or derivation fails
     */
    public byte[] deriveKey(char[] password, byte[] salt, int keyLength) {
        Objects.requireNonNull(password, "Password cannot be null");
        Objects.requireNonNull(salt, "Salt cannot be null");

        if (keyLength <= 0) {
            throw new IllegalArgumentException("Key length must be positive");
        }

        if (salt.length < 8) {
            throw new IllegalArgumentException("Salt length should be at least 8 bytes");
        }

        // Check if Bouncy Castle is available
        if (!isBouncyCastleAvailable()) {
            throw new OpenCryptoException(ALGORITHM, "initialization",
                    "Scrypt requires Bouncy Castle library (bcprov-jdk18on). Add it to your classpath.");
        }

        // Convert char[] to byte[] using UTF-8 encoding to avoid lossy truncation
        // of non-ASCII characters (chars > 255 would lose high bits with simple cast)
        byte[] passwordBytes = new String(password).getBytes(java.nio.charset.StandardCharsets.UTF_8);

        try {
            return scryptBouncyCastle(passwordBytes, salt, n, r, p, keyLength);
        } finally {
            // Securely erase password bytes
            CryptoUtil.secureErase(passwordBytes);
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
            // Scrypt does not use 'info' parameter, so it's ignored
            byte[] actualSalt = (salt == null || salt.length == 0)
                    ? CryptoUtil.randomSalt(DEFAULT_SALT_LENGTH)
                    : salt;
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
        return ALGORITHM;
    }

    /**
     * Gets the CPU/memory cost parameter
     * 获取 CPU/内存成本参数
     *
     * @return the N parameter
     */
    public int getN() {
        return n;
    }

    /**
     * Gets the block size parameter
     * 获取块大小参数
     *
     * @return the r parameter
     */
    public int getR() {
        return r;
    }

    /**
     * Gets the parallelization parameter
     * 获取并行化参数
     *
     * @return the p parameter
     */
    public int getP() {
        return p;
    }

    /**
     * Validates Scrypt parameters
     * 验证 Scrypt 参数
     */
    private static void validateParameters(int n, int r, int p) {
        if (n <= 1 || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("N must be a power of 2 greater than 1");
        }

        if (r <= 0) {
            throw new IllegalArgumentException("r must be positive");
        }

        if (p <= 0) {
            throw new IllegalArgumentException("p must be positive");
        }

        // Check for potential integer overflow: N * r * p < 2^30
        long limit = (long) Integer.MAX_VALUE / 2;
        if ((long) r * (long) p >= limit / (long) n) {
            throw new IllegalArgumentException("Parameters N, r, p would cause memory overflow");
        }
    }

    /**
     * Checks if Bouncy Castle library is available on the classpath
     * 检查 Bouncy Castle 库是否在类路径中可用
     */
    private static boolean isBouncyCastleAvailable() {
        try {
            Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Performs Scrypt key derivation using Bouncy Castle
     * 使用 Bouncy Castle 执行 Scrypt 密钥派生
     */
    private static byte[] scryptBouncyCastle(byte[] password, byte[] salt, int n, int r, int p, int dkLen) {
        try {
            Class<?> scryptClass = Class.forName("org.bouncycastle.crypto.generators.SCrypt");
            java.lang.reflect.Method generateMethod = scryptClass.getMethod(
                    "generate", byte[].class, byte[].class, int.class, int.class, int.class, int.class);
            return (byte[]) generateMethod.invoke(null, password, salt, n, r, p, dkLen);
        } catch (Exception e) {
            throw new OpenCryptoException(ALGORITHM, "derive",
                    "Failed to derive key using Scrypt", e);
        }
    }
}
