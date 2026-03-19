package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

/**
 * xxHash64 hash function implementation
 * xxHash64 哈希函数实现
 *
 * <p>xxHash is an extremely fast non-cryptographic hash algorithm,
 * working at speeds close to RAM limits.</p>
 * <p>xxHash 是一种极快的非加密哈希算法，工作速度接近 RAM 限制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>64-bit output - 64位输出</li>
 *   <li>Extremely high performance - 极高性能</li>
 *   <li>Good distribution - 良好的分布</li>
 *   <li>Configurable seed - 可配置种子</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashCode hash = XxHashFunction.xxHash64().hashUtf8("Hello World");
 * long value = hash.asLong();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Not for cryptographic use - 不用于加密用途</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class XxHashFunction extends AbstractHashFunction {

    private static final long PRIME64_1 = 0x9E3779B185EBCA87L;
    private static final long PRIME64_2 = 0xC2B2AE3D27D4EB4FL;
    private static final long PRIME64_3 = 0x165667B19E3779F9L;
    private static final long PRIME64_4 = 0x85EBCA77C2B2AE63L;
    private static final long PRIME64_5 = 0x27D4EB2F165667C5L;

    private final long seed;

    private XxHashFunction(long seed) {
        super(64, "xxHash64");
        this.seed = seed;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an xxHash64 function with default seed (0)
     * 使用默认种子（0）创建xxHash64函数
     *
     * @return hash function | 哈希函数
     */
    public static XxHashFunction xxHash64() {
        return xxHash64(0L);
    }

    /**
     * Creates an xxHash64 function with specified seed
     * 使用指定种子创建xxHash64函数
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static XxHashFunction xxHash64(long seed) {
        return new XxHashFunction(seed);
    }

    @Override
    public Hasher newHasher() {
        return new XxHash64Hasher(seed);
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        return HashCode.fromLong(hash64(input, offset, length, seed));
    }

    // ==================== Hash Implementation | 哈希实现 ====================

    private static long hash64(byte[] input, int offset, int length, long seed) {
        long h64;
        int end = offset + length;

        if (length >= 32) {
            int limit = end - 32;
            long v1 = seed + PRIME64_1 + PRIME64_2;
            long v2 = seed + PRIME64_2;
            long v3 = seed;
            long v4 = seed - PRIME64_1;

            int p = offset;
            do {
                v1 = round(v1, getLongLE(input, p));
                p += 8;
                v2 = round(v2, getLongLE(input, p));
                p += 8;
                v3 = round(v3, getLongLE(input, p));
                p += 8;
                v4 = round(v4, getLongLE(input, p));
                p += 8;
            } while (p <= limit);

            h64 = Long.rotateLeft(v1, 1)
                    + Long.rotateLeft(v2, 7)
                    + Long.rotateLeft(v3, 12)
                    + Long.rotateLeft(v4, 18);

            h64 = mergeRound(h64, v1);
            h64 = mergeRound(h64, v2);
            h64 = mergeRound(h64, v3);
            h64 = mergeRound(h64, v4);

            offset = p;
        } else {
            h64 = seed + PRIME64_5;
        }

        h64 += length;

        // Process remaining bytes
        while (offset + 8 <= end) {
            long k1 = round(0, getLongLE(input, offset));
            h64 ^= k1;
            h64 = Long.rotateLeft(h64, 27) * PRIME64_1 + PRIME64_4;
            offset += 8;
        }

        if (offset + 4 <= end) {
            h64 ^= (getIntLE(input, offset) & 0xFFFFFFFFL) * PRIME64_1;
            h64 = Long.rotateLeft(h64, 23) * PRIME64_2 + PRIME64_3;
            offset += 4;
        }

        while (offset < end) {
            h64 ^= (input[offset] & 0xFFL) * PRIME64_5;
            h64 = Long.rotateLeft(h64, 11) * PRIME64_1;
            offset++;
        }

        // Final mix
        h64 ^= h64 >>> 33;
        h64 *= PRIME64_2;
        h64 ^= h64 >>> 29;
        h64 *= PRIME64_3;
        h64 ^= h64 >>> 32;

        return h64;
    }

    private static long round(long acc, long input) {
        acc += input * PRIME64_2;
        acc = Long.rotateLeft(acc, 31);
        acc *= PRIME64_1;
        return acc;
    }

    private static long mergeRound(long acc, long val) {
        val = round(0, val);
        acc ^= val;
        acc = acc * PRIME64_1 + PRIME64_4;
        return acc;
    }

    private static long getLongLE(byte[] data, int offset) {
        return (data[offset] & 0xffL)
                | ((data[offset + 1] & 0xffL) << 8)
                | ((data[offset + 2] & 0xffL) << 16)
                | ((data[offset + 3] & 0xffL) << 24)
                | ((data[offset + 4] & 0xffL) << 32)
                | ((data[offset + 5] & 0xffL) << 40)
                | ((data[offset + 6] & 0xffL) << 48)
                | ((data[offset + 7] & 0xffL) << 56);
    }

    private static int getIntLE(byte[] data, int offset) {
        return (data[offset] & 0xff)
                | ((data[offset + 1] & 0xff) << 8)
                | ((data[offset + 2] & 0xff) << 16)
                | ((data[offset + 3] & 0xff) << 24);
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    private static class XxHash64Hasher extends BufferedHasher {
        private final long seed;

        XxHash64Hasher(long seed) {
            super(128);
            this.seed = seed;
        }

        @Override
        protected HashCode doHash() {
            byte[] bytes = getBytes();
            return HashCode.fromLong(hash64(bytes, 0, bytes.length, seed));
        }
    }
}
