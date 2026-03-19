package cloud.opencode.base.crypto.random;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.DrbgParameters;

/**
 * Secure random number generator factory providing various SecureRandom instances.
 * 安全随机数生成器工厂，提供各种 SecureRandom 实例。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cryptographically secure random byte generation - 密码学安全的随机字节生成</li>
 *   <li>Platform-optimized SecureRandom instances - 平台优化的 SecureRandom 实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] random = SecureRandoms.nextBytes(32);
 * SecureRandom sr = SecureRandoms.getDefault();
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
public final class SecureRandoms {

    private static final String DRBG_ALGORITHM = "DRBG";
    private static final int DEFAULT_DRBG_STRENGTH = 256;

    /**
     * Thread-local cached SecureRandom for hot-path methods (nextBytes, etc.).
     * 线程本地缓存的 SecureRandom，用于热路径方法（nextBytes 等）。
     */
    private static final ThreadLocal<SecureRandom> THREAD_LOCAL_RANDOM =
            ThreadLocal.withInitial(SecureRandom::new);

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private SecureRandoms() {
        throw new AssertionError("No SecureRandoms instances for you!");
    }

    /**
     * Gets the default SecureRandom instance.
     * 获取默认的 SecureRandom 实例。
     *
     * @return a new SecureRandom instance
     */
    public static SecureRandom getDefault() {
        return new SecureRandom();
    }

    /**
     * Gets a strong SecureRandom instance using the strongest available algorithm.
     * 获取使用最强可用算法的 SecureRandom 实例。
     *
     * @return a strong SecureRandom instance
     * @throws IllegalStateException if no strong random number generator is available
     */
    public static SecureRandom getStrong() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No strong SecureRandom algorithm available", e);
        }
    }

    /**
     * Gets a DRBG (Deterministic Random Bit Generator) SecureRandom instance with default configuration.
     * 获取默认配置的 DRBG（确定性随机位生成器）SecureRandom 实例。
     *
     * @return a DRBG SecureRandom instance
     * @throws IllegalStateException if DRBG algorithm is not available
     */
    public static SecureRandom getDrbg() {
        return getDrbg(DEFAULT_DRBG_STRENGTH, DrbgParameters.Capability.PR_AND_RESEED, null);
    }

    /**
     * Gets a DRBG SecureRandom instance with custom configuration.
     * 获取自定义配置的 DRBG SecureRandom 实例。
     *
     * @param strength the security strength in bits (112, 128, 192, or 256)
     * @param capability the DRBG capability
     * @param personalizationString optional personalization string for DRBG (can be null)
     * @return a configured DRBG SecureRandom instance
     * @throws IllegalStateException if DRBG algorithm is not available
     */
    public static SecureRandom getDrbg(int strength,
                                       DrbgParameters.Capability capability,
                                       byte[] personalizationString) {
        try {
            DrbgParameters.Instantiation params = DrbgParameters.instantiation(
                strength,
                capability,
                personalizationString
            );
            return SecureRandom.getInstance(DRBG_ALGORITHM, params);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("DRBG algorithm not available", e);
        }
    }

    /**
     * Gets a SecureRandom instance for the specified algorithm.
     * 获取指定算法的 SecureRandom 实例。
     *
     * @param algorithm the name of the RNG algorithm (e.g., "SHA1PRNG", "NativePRNG")
     * @return a SecureRandom instance
     * @throws IllegalArgumentException if the algorithm is not available
     */
    public static SecureRandom getInstance(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm must not be null or blank");
        }

        try {
            return SecureRandom.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Gets a SecureRandom instance for the specified algorithm and provider.
     * 获取指定算法和提供者的 SecureRandom 实例。
     *
     * @param algorithm the name of the RNG algorithm
     * @param provider the name of the provider
     * @return a SecureRandom instance
     * @throws IllegalArgumentException if the algorithm or provider is not available
     */
    public static SecureRandom getInstance(String algorithm, String provider) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm must not be null or blank");
        }
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider must not be null or blank");
        }

        try {
            return SecureRandom.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Algorithm not available: " + algorithm, e);
        } catch (NoSuchProviderException e) {
            throw new IllegalArgumentException("Provider not available: " + provider, e);
        }
    }

    /**
     * Generates random seed bytes using the default SecureRandom.
     * 使用默认 SecureRandom 生成随机种子字节。
     *
     * @param numBytes the number of seed bytes to generate
     * @return array of random seed bytes
     * @throws IllegalArgumentException if numBytes is not positive
     */
    public static byte[] generateSeed(int numBytes) {
        if (numBytes <= 0) {
            throw new IllegalArgumentException("Number of bytes must be positive");
        }
        return SecureRandom.getSeed(numBytes);
    }

    /**
     * Generates random bytes using a thread-local cached SecureRandom.
     * 使用线程本地缓存的 SecureRandom 生成随机字节。
     *
     * @param numBytes the number of bytes to generate | 要生成的字节数
     * @return array of random bytes | 随机字节数组
     * @throws IllegalArgumentException if numBytes is not positive
     */
    public static byte[] nextBytes(int numBytes) {
        if (numBytes <= 0) {
            throw new IllegalArgumentException("Number of bytes must be positive");
        }
        byte[] bytes = new byte[numBytes];
        THREAD_LOCAL_RANDOM.get().nextBytes(bytes);
        return bytes;
    }
}
