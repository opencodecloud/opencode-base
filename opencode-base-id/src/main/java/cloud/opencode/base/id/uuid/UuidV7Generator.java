package cloud.opencode.base.id.uuid;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * UUID v7 Generator
 * UUID v7生成器
 *
 * <p>Generates time-ordered UUIDs based on RFC 9562 version 7.
 * The first 48 bits contain Unix timestamp in milliseconds,
 * making these UUIDs naturally sortable and database-friendly.</p>
 * <p>基于RFC 9562版本7生成时间有序的UUID。前48位包含Unix毫秒时间戳，
 * 使这些UUID天然可排序且对数据库友好。</p>
 *
 * <p><strong>UUID v7 Structure | UUID v7结构:</strong></p>
 * <pre>
 * 0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         unix_ts_ms (48 bits)                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          unix_ts_ms           |  ver  |  rand_a (12 bits)    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |var|                    rand_b (62 bits)                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                         rand_b                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-ordered for database efficiency - 时间有序，数据库高效</li>
 *   <li>Monotonic within same millisecond - 同毫秒内单调递增</li>
 *   <li>Lexicographically sortable - 字典序可排序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UuidV7Generator gen = UuidV7Generator.create();
 * UUID uuid = gen.generate();
 *
 * // Extract timestamp
 * long timestamp = UuidV7Generator.extractTimestamp(uuid);
 *
 * // Check if v7
 * boolean isV7 = UuidV7Generator.isV7(uuid);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class UuidV7Generator implements IdGenerator<UUID> {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final UuidV7Generator INSTANCE = new UuidV7Generator();

    private final AtomicLong lastTimestamp = new AtomicLong(0);
    private final AtomicLong counter = new AtomicLong(0);

    private UuidV7Generator() {
    }

    /**
     * Creates a UUID v7 generator
     * 创建UUID v7生成器
     *
     * @return generator | 生成器
     */
    public static UuidV7Generator create() {
        return INSTANCE;
    }

    private static final int MAX_COUNTER = 0xFFF; // 4095

    @Override
    public UUID generate() {
        long timestamp = System.currentTimeMillis();

        // Handle monotonicity within same millisecond
        long last = lastTimestamp.get();
        long count;

        if (timestamp == last) {
            count = counter.incrementAndGet();
            if (count > MAX_COUNTER) {
                // Counter overflow: wait for next millisecond
                while (timestamp <= last) {
                    timestamp = System.currentTimeMillis();
                }
                if (lastTimestamp.compareAndSet(last, timestamp)) {
                    counter.set(0);
                    count = 0;
                } else {
                    return generate(); // Retry
                }
            }
        } else if (timestamp > last) {
            if (lastTimestamp.compareAndSet(last, timestamp)) {
                counter.set(0);
                count = 0;
            } else {
                // Retry if CAS failed
                return generate();
            }
        } else {
            // Clock moved backward, use last timestamp
            timestamp = last;
            count = counter.incrementAndGet();
            if (count > MAX_COUNTER) {
                // Counter overflow: wait for next millisecond
                while (timestamp <= last) {
                    timestamp = System.currentTimeMillis();
                }
                if (lastTimestamp.compareAndSet(last, timestamp)) {
                    counter.set(0);
                    count = 0;
                } else {
                    return generate(); // Retry
                }
            }
        }

        // Build UUID v7
        // Most significant 64 bits: 48-bit timestamp + 4-bit version + 12-bit rand_a
        long msb = (timestamp << 16) | 0x7000L | (count & MAX_COUNTER);

        // Least significant 64 bits: 2-bit variant + 62-bit random
        long lsb = RANDOM.nextLong();
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // Set variant bits

        return new UUID(msb, lsb);
    }

    /**
     * Generates a UUID as string
     * 生成UUID字符串
     *
     * @return UUID string | UUID字符串
     */
    public String generateStr() {
        return generate().toString();
    }

    /**
     * Extracts the timestamp from a UUID v7
     * 从UUID v7提取时间戳
     *
     * @param uuid the UUID | UUID
     * @return timestamp in milliseconds | 时间戳（毫秒）
     */
    public static long extractTimestamp(UUID uuid) {
        return uuid.getMostSignificantBits() >>> 16;
    }

    /**
     * Checks if a UUID is version 7
     * 检查UUID是否为版本7
     *
     * @param uuid the UUID | UUID
     * @return true if v7 | 如果是v7返回true
     */
    public static boolean isV7(UUID uuid) {
        return uuid.version() == 7;
    }

    @Override
    public String getType() {
        return "UUID-v7";
    }
}
