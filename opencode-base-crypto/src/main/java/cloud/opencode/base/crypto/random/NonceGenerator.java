package cloud.opencode.base.crypto.random;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Nonce (Number Used Once) generator providing various nonce generation strategies.
 * Nonce（一次性数字）生成器，提供多种 nonce 生成策略。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cryptographic nonce generation - 加密随机数生成</li>
 *   <li>Counter-based and random nonce strategies - 基于计数器和随机的随机数策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NonceGenerator gen = NonceGenerator.random(12);
 * byte[] nonce = gen.nextNonce();
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
public final class NonceGenerator {

    private static final SecureRandom DEFAULT_RANDOM = new SecureRandom();

    /**
     * Recommended nonce size for AES-GCM (96 bits / 12 bytes).
     * AES-GCM 推荐的 nonce 大小（96 位 / 12 字节）。
     */
    public static final int AES_GCM_NONCE_SIZE = 12;

    /**
     * Recommended nonce size for ChaCha20-Poly1305 (96 bits / 12 bytes).
     * ChaCha20-Poly1305 推荐的 nonce 大小（96 位 / 12 字节）。
     */
    public static final int CHACHA20_NONCE_SIZE = 12;

    /**
     * Size of timestamp component in bytes (8 bytes for long).
     * 时间戳组件的字节大小（long 类型 8 字节）。
     */
    private static final int TIMESTAMP_SIZE = Long.BYTES;

    /**
     * Default random bytes size for hybrid nonce.
     * 混合 nonce 的默认随机字节大小。
     */
    private static final int DEFAULT_RANDOM_SIZE = 8;

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private NonceGenerator() {
        throw new AssertionError("No NonceGenerator instances for you!");
    }

    /**
     * Generates a random nonce of the specified length.
     * 生成指定长度的随机 nonce。
     *
     * @param length the length of the nonce in bytes
     * @return array of random nonce bytes
     * @throws IllegalArgumentException if length is not positive
     */
    public static byte[] random(int length) {
        return random(length, DEFAULT_RANDOM);
    }

    /**
     * Generates a random nonce of the specified length using the provided SecureRandom.
     * 使用提供的 SecureRandom 生成指定长度的随机 nonce。
     *
     * @param length the length of the nonce in bytes
     * @param random the SecureRandom instance to use
     * @return array of random nonce bytes
     * @throws IllegalArgumentException if length is not positive or random is null
     */
    public static byte[] random(int length, SecureRandom random) {
        return RandomBytes.generate(length, random);
    }

    /**
     * Generates a counter-based nonce by encoding a counter value into bytes.
     * Note: The counter must be securely stored and incremented for each operation.
     * 通过将计数器值编码为字节生成基于计数器的 nonce。
     * 注意：计数器必须安全存储并在每次操作时递增。
     *
     * @param counter the counter value (must be unique for each operation)
     * @param length the total length of the nonce in bytes
     * @return array of nonce bytes with counter value
     * @throws IllegalArgumentException if length is less than 8 bytes
     */
    public static byte[] counter(long counter, int length) {
        if (length < Long.BYTES) {
            throw new IllegalArgumentException(
                "Length must be at least " + Long.BYTES + " bytes to accommodate counter"
            );
        }

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putLong(counter);

        // Fill remaining bytes with zeros if length > 8
        if (length > Long.BYTES) {
            byte[] padding = new byte[length - Long.BYTES];
            buffer.put(padding);
        }

        return buffer.array();
    }

    /**
     * Generates a timestamp-based nonce combining current timestamp with random bytes.
     * 生成基于时间戳的 nonce，将当前时间戳与随机字节组合。
     *
     * @param randomLength the number of random bytes to append to the timestamp
     * @return array of nonce bytes (timestamp + random bytes)
     * @throws IllegalArgumentException if randomLength is negative
     */
    public static byte[] timestamp(int randomLength) {
        return timestamp(randomLength, DEFAULT_RANDOM);
    }

    /**
     * Generates a timestamp-based nonce combining current timestamp with random bytes.
     * 生成基于时间戳的 nonce，将当前时间戳与随机字节组合。
     *
     * @param randomLength the number of random bytes to append to the timestamp
     * @param random the SecureRandom instance to use
     * @return array of nonce bytes (timestamp + random bytes)
     * @throws IllegalArgumentException if randomLength is negative or random is null
     */
    public static byte[] timestamp(int randomLength, SecureRandom random) {
        if (randomLength < 0) {
            throw new IllegalArgumentException("Random length must not be negative");
        }
        if (random == null) {
            throw new IllegalArgumentException("SecureRandom must not be null");
        }

        long timestamp = Instant.now().toEpochMilli();

        ByteBuffer buffer = ByteBuffer.allocate(TIMESTAMP_SIZE + randomLength);
        buffer.putLong(timestamp);

        if (randomLength > 0) {
            byte[] randomBytes = new byte[randomLength];
            random.nextBytes(randomBytes);
            buffer.put(randomBytes);
        }

        return buffer.array();
    }

    /**
     * Generates a hybrid nonce combining timestamp with random bytes.
     * The nonce structure: [8-byte timestamp][remaining random bytes].
     * 生成混合 nonce，将时间戳与随机字节组合。
     * Nonce 结构：[8 字节时间戳][剩余随机字节]。
     *
     * @param totalLength the total length of the nonce in bytes
     * @return array of hybrid nonce bytes
     * @throws IllegalArgumentException if totalLength is less than 8 bytes
     */
    public static byte[] hybrid(int totalLength) {
        return hybrid(totalLength, DEFAULT_RANDOM);
    }

    /**
     * Generates a hybrid nonce combining timestamp with random bytes.
     * The nonce structure: [8-byte timestamp][remaining random bytes].
     * 生成混合 nonce，将时间戳与随机字节组合。
     * Nonce 结构：[8 字节时间戳][剩余随机字节]。
     *
     * @param totalLength the total length of the nonce in bytes
     * @param random the SecureRandom instance to use
     * @return array of hybrid nonce bytes
     * @throws IllegalArgumentException if totalLength is less than 8 bytes or random is null
     */
    public static byte[] hybrid(int totalLength, SecureRandom random) {
        if (totalLength < TIMESTAMP_SIZE) {
            throw new IllegalArgumentException(
                "Total length must be at least " + TIMESTAMP_SIZE + " bytes to include timestamp"
            );
        }

        int randomLength = totalLength - TIMESTAMP_SIZE;
        return timestamp(randomLength, random);
    }

    /**
     * Generates a 12-byte (96-bit) random nonce suitable for AES-GCM.
     * This is the NIST-recommended nonce size for AES-GCM.
     * 生成适用于 AES-GCM 的 12 字节（96 位）随机 nonce。
     * 这是 NIST 推荐的 AES-GCM nonce 大小。
     *
     * @return 12-byte array of random nonce bytes
     */
    public static byte[] forAesGcm() {
        return random(AES_GCM_NONCE_SIZE);
    }

    /**
     * Generates a 12-byte (96-bit) random nonce suitable for AES-GCM using the provided SecureRandom.
     * 使用提供的 SecureRandom 生成适用于 AES-GCM 的 12 字节（96 位）随机 nonce。
     *
     * @param random the SecureRandom instance to use
     * @return 12-byte array of random nonce bytes
     * @throws IllegalArgumentException if random is null
     */
    public static byte[] forAesGcm(SecureRandom random) {
        return random(AES_GCM_NONCE_SIZE, random);
    }

    /**
     * Generates a 12-byte (96-bit) random nonce suitable for ChaCha20-Poly1305.
     * 生成适用于 ChaCha20-Poly1305 的 12 字节（96 位）随机 nonce。
     *
     * @return 12-byte array of random nonce bytes
     */
    public static byte[] forChaCha20() {
        return random(CHACHA20_NONCE_SIZE);
    }

    /**
     * Generates a 12-byte (96-bit) random nonce suitable for ChaCha20-Poly1305 using the provided SecureRandom.
     * 使用提供的 SecureRandom 生成适用于 ChaCha20-Poly1305 的 12 字节（96 位）随机 nonce。
     *
     * @param random the SecureRandom instance to use
     * @return 12-byte array of random nonce bytes
     * @throws IllegalArgumentException if random is null
     */
    public static byte[] forChaCha20(SecureRandom random) {
        return random(CHACHA20_NONCE_SIZE, random);
    }
}
