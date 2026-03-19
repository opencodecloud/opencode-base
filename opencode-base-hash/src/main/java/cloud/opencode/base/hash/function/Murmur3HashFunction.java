package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

/**
 * MurmurHash3 hash function implementation
 * MurmurHash3 哈希函数实现
 *
 * <p>MurmurHash3 is a non-cryptographic hash function that provides excellent
 * distribution and collision resistance with high performance.</p>
 * <p>MurmurHash3 是一种非加密哈希函数，提供出色的分布和碰撞阻力，性能高。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>32-bit and 128-bit variants - 32位和128位变体</li>
 *   <li>Configurable seed value - 可配置的种子值</li>
 *   <li>High performance - 高性能</li>
 *   <li>Good distribution - 良好的分布</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // 32-bit hash
 * HashCode hash32 = Murmur3HashFunction.murmur3_32().hashUtf8("Hello");
 *
 * // 128-bit hash with seed
 * HashCode hash128 = Murmur3HashFunction.murmur3_128(42).hashBytes(data);
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
public final class Murmur3HashFunction extends AbstractHashFunction {

    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;

    private static final long C1_128 = 0x87c37b91114253d5L;
    private static final long C2_128 = 0x4cf5ad432745937fL;

    private final int seed;
    private final boolean is128Bit;

    private Murmur3HashFunction(int bits, int seed) {
        super(bits, "murmur3_" + bits);
        this.seed = seed;
        this.is128Bit = (bits == 128);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a 32-bit MurmurHash3 function with default seed
     * 使用默认种子创建32位MurmurHash3函数
     *
     * @return hash function | 哈希函数
     */
    public static Murmur3HashFunction murmur3_32() {
        return murmur3_32(0);
    }

    /**
     * Creates a 32-bit MurmurHash3 function with specified seed
     * 使用指定种子创建32位MurmurHash3函数
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static Murmur3HashFunction murmur3_32(int seed) {
        return new Murmur3HashFunction(32, seed);
    }

    /**
     * Creates a 128-bit MurmurHash3 function with default seed
     * 使用默认种子创建128位MurmurHash3函数
     *
     * @return hash function | 哈希函数
     */
    public static Murmur3HashFunction murmur3_128() {
        return murmur3_128(0);
    }

    /**
     * Creates a 128-bit MurmurHash3 function with specified seed
     * 使用指定种子创建128位MurmurHash3函数
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static Murmur3HashFunction murmur3_128(int seed) {
        return new Murmur3HashFunction(128, seed);
    }

    @Override
    public Hasher newHasher() {
        return is128Bit ? new Murmur3_128Hasher(seed) : new Murmur3_32Hasher(seed);
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        if (is128Bit) {
            return hash128(input, offset, length, seed);
        } else {
            return HashCode.fromInt(hash32(input, offset, length, seed));
        }
    }

    // ==================== 32-bit Implementation | 32位实现 ====================

    private static int hash32(byte[] data, int offset, int length, int seed) {
        int h1 = seed;
        int nblocks = length / 4;

        // Body
        for (int i = 0; i < nblocks; i++) {
            int k1 = getIntLE(data, offset + i * 4);
            k1 *= C1_32;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= C2_32;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // Tail
        int k1 = 0;
        int tailStart = offset + nblocks * 4;
        switch (length & 3) {
            case 3:
                k1 ^= (data[tailStart + 2] & 0xff) << 16;
            case 2:
                k1 ^= (data[tailStart + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[tailStart] & 0xff);
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= C2_32;
                h1 ^= k1;
        }

        // Finalization
        h1 ^= length;
        h1 = fmix32(h1);

        return h1;
    }

    private static int fmix32(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    // ==================== 128-bit Implementation | 128位实现 ====================

    private static HashCode hash128(byte[] data, int offset, int length, int seed) {
        long h1 = seed;
        long h2 = seed;
        int nblocks = length / 16;

        // Body
        for (int i = 0; i < nblocks; i++) {
            long k1 = getLongLE(data, offset + i * 16);
            long k2 = getLongLE(data, offset + i * 16 + 8);

            k1 *= C1_128;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2_128;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= C2_128;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1_128;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        // Tail
        long k1 = 0;
        long k2 = 0;
        int tailStart = offset + nblocks * 16;

        switch (length & 15) {
            case 15: k2 ^= (long) (data[tailStart + 14] & 0xff) << 48;
            case 14: k2 ^= (long) (data[tailStart + 13] & 0xff) << 40;
            case 13: k2 ^= (long) (data[tailStart + 12] & 0xff) << 32;
            case 12: k2 ^= (long) (data[tailStart + 11] & 0xff) << 24;
            case 11: k2 ^= (long) (data[tailStart + 10] & 0xff) << 16;
            case 10: k2 ^= (long) (data[tailStart + 9] & 0xff) << 8;
            case 9:
                k2 ^= (data[tailStart + 8] & 0xff);
                k2 *= C2_128;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= C1_128;
                h2 ^= k2;
            case 8: k1 ^= (long) (data[tailStart + 7] & 0xff) << 56;
            case 7: k1 ^= (long) (data[tailStart + 6] & 0xff) << 48;
            case 6: k1 ^= (long) (data[tailStart + 5] & 0xff) << 40;
            case 5: k1 ^= (long) (data[tailStart + 4] & 0xff) << 32;
            case 4: k1 ^= (long) (data[tailStart + 3] & 0xff) << 24;
            case 3: k1 ^= (long) (data[tailStart + 2] & 0xff) << 16;
            case 2: k1 ^= (long) (data[tailStart + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[tailStart] & 0xff);
                k1 *= C1_128;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= C2_128;
                h1 ^= k1;
        }

        // Finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        byte[] result = new byte[16];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (h1 >> (i * 8));
            result[i + 8] = (byte) (h2 >> (i * 8));
        }
        return HashCode.fromBytes(result);
    }

    private static long fmix64(long h) {
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    // ==================== Utility Methods | 工具方法 ====================

    private static int getIntLE(byte[] data, int offset) {
        return (data[offset] & 0xff)
                | ((data[offset + 1] & 0xff) << 8)
                | ((data[offset + 2] & 0xff) << 16)
                | ((data[offset + 3] & 0xff) << 24);
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

    // ==================== Hasher Implementations | Hasher实现 ====================

    private static class Murmur3_32Hasher extends BufferedHasher {
        private final int seed;

        Murmur3_32Hasher(int seed) {
            super(128);
            this.seed = seed;
        }

        @Override
        protected HashCode doHash() {
            byte[] bytes = getBytes();
            return HashCode.fromInt(hash32(bytes, 0, bytes.length, seed));
        }
    }

    private static class Murmur3_128Hasher extends BufferedHasher {
        private final int seed;

        Murmur3_128Hasher(int seed) {
            super(128);
            this.seed = seed;
        }

        @Override
        protected HashCode doHash() {
            byte[] bytes = getBytes();
            return hash128(bytes, 0, bytes.length, seed);
        }
    }
}
