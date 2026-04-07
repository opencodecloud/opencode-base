package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;

/**
 * SipHash-2-4 hash function implementation
 * SipHash-2-4 哈希函数实现
 *
 * <p>A pure Java implementation of SipHash-2-4, a fast short-input PRF
 * designed by Jean-Philippe Aumasson and Daniel J. Bernstein.
 * SipHash produces a 64-bit output and is optimized for short messages,
 * making it ideal for hash table lookups and hash-flooding protection.</p>
 * <p>SipHash-2-4 的纯 Java 实现，这是由 Jean-Philippe Aumasson 和 Daniel J. Bernstein
 * 设计的快速短输入 PRF。SipHash 产生 64 位输出，针对短消息优化，
 * 非常适合哈希表查找和哈希洪泛防护。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>64-bit keyed hash output - 64位密钥哈希输出</li>
 *   <li>2 compression rounds, 4 finalization rounds - 2轮压缩，4轮终结</li>
 *   <li>Configurable 128-bit key (k0, k1) - 可配置128位密钥(k0, k1)</li>
 *   <li>Hash-flooding resistant - 抗哈希洪泛攻击</li>
 *   <li>Streaming and one-shot hashing - 支持流式和一次性哈希</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default key (k0=0, k1=0)
 * // 默认密钥(k0=0, k1=0)
 * HashCode hash = SipHashFunction.sipHash24().hashUtf8("Hello World");
 *
 * // Custom key
 * // 自定义密钥
 * HashCode hash2 = SipHashFunction.sipHash24(0x0706050403020100L, 0x0f0e0d0c0b0a0908L)
 *     .hashUtf8("Hello World");
 *
 * // Streaming API
 * // 流式API
 * Hasher hasher = SipHashFunction.sipHash24().newHasher();
 * hasher.putInt(42).putLong(123L);
 * HashCode hash3 = hasher.hash();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Key not exposed in toString() - toString()不暴露密钥</li>
 *   <li>Not for cryptographic use - 不用于加密用途</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) for hash state - 哈希状态 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://131002.net/siphash/">SipHash: a fast short-input PRF</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
public final class SipHashFunction extends AbstractHashFunction {

    private final long k0;
    private final long k1;

    private SipHashFunction(long k0, long k1) {
        super(64, "SipHash-2-4");
        this.k0 = k0;
        this.k1 = k1;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a SipHash-2-4 function with default key (k0=0, k1=0)
     * 使用默认密钥(k0=0, k1=0)创建SipHash-2-4函数
     *
     * @return hash function | 哈希函数
     */
    public static SipHashFunction sipHash24() {
        return new SipHashFunction(0L, 0L);
    }

    /**
     * Creates a SipHash-2-4 function with specified 128-bit key
     * 使用指定的128位密钥创建SipHash-2-4函数
     *
     * @param k0 first 64 bits of the key | 密钥的前64位
     * @param k1 last 64 bits of the key | 密钥的后64位
     * @return hash function | 哈希函数
     */
    public static SipHashFunction sipHash24(long k0, long k1) {
        return new SipHashFunction(k0, k1);
    }

    @Override
    public Hasher newHasher() {
        return new SipHasher(k0, k1);
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        java.util.Objects.requireNonNull(input, "input");
        if (offset < 0 || length < 0 || length > input.length - offset) {
            throw cloud.opencode.base.hash.exception.OpenHashException.invalidInput(
                    "offset=" + offset + ", length=" + length + ", array.length=" + input.length);
        }
        return HashCode.fromLong(sipHash24(input, offset, length, k0, k1));
    }

    /**
     * Returns string representation without exposing key values
     * 返回字符串表示，不暴露密钥值
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        return "SipHash-2-4[64]";
    }

    // ==================== SipHash Algorithm | SipHash算法 ====================

    /**
     * Rotates left (circular left shift)
     * 循环左移
     */
    private static long rotateLeft(long val, int shift) {
        return (val << shift) | (val >>> (64 - shift));
    }

    /**
     * Reads a little-endian 64-bit long from a byte array
     * 从字节数组中以小端序读取64位long
     */
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

    /**
     * Computes SipHash-2-4 for the given input
     * 计算给定输入的SipHash-2-4
     *
     * @param data   input byte array | 输入字节数组
     * @param offset starting offset | 起始偏移
     * @param length number of bytes | 字节数
     * @param k0     first key half | 密钥前半部分
     * @param k1     second key half | 密钥后半部分
     * @return 64-bit hash value | 64位哈希值
     */
    static long sipHash24(byte[] data, int offset, int length, long k0, long k1) {
        // Initialization
        long v0 = k0 ^ 0x736f6d6570736575L;
        long v1 = k1 ^ 0x646f72616e646f6dL;
        long v2 = k0 ^ 0x6c7967656e657261L;
        long v3 = k1 ^ 0x7465646279746573L;

        int end = offset + length;
        int blockEnd = offset + (length / 8) * 8;

        // Process 8-byte blocks
        for (int i = offset; i < blockEnd; i += 8) {
            long m = getLongLE(data, i);
            v3 ^= m;

            // 2 SipRounds
            v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
            v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
            v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
            v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

            v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
            v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
            v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
            v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

            v0 ^= m;
        }

        // Process remaining bytes with length byte in high position
        long last = ((long) length) << 56;
        int remaining = end - blockEnd;
        // Fall through intentionally for each case
        if (remaining >= 7) {
            last |= (data[blockEnd + 6] & 0xffL) << 48;
        }
        if (remaining >= 6) {
            last |= (data[blockEnd + 5] & 0xffL) << 40;
        }
        if (remaining >= 5) {
            last |= (data[blockEnd + 4] & 0xffL) << 32;
        }
        if (remaining >= 4) {
            last |= (data[blockEnd + 3] & 0xffL) << 24;
        }
        if (remaining >= 3) {
            last |= (data[blockEnd + 2] & 0xffL) << 16;
        }
        if (remaining >= 2) {
            last |= (data[blockEnd + 1] & 0xffL) << 8;
        }
        if (remaining >= 1) {
            last |= (data[blockEnd] & 0xffL);
        }

        v3 ^= last;

        // 2 SipRounds
        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        v0 ^= last;

        // Finalization
        v2 ^= 0xff;

        // 4 SipRounds
        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        v0 += v1; v1 = rotateLeft(v1, 13); v1 ^= v0; v0 = rotateLeft(v0, 32);
        v2 += v3; v3 = rotateLeft(v3, 16); v3 ^= v2;
        v0 += v3; v3 = rotateLeft(v3, 21); v3 ^= v0;
        v2 += v1; v1 = rotateLeft(v1, 17); v1 ^= v2; v2 = rotateLeft(v2, 32);

        return v0 ^ v1 ^ v2 ^ v3;
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    /**
     * SipHash streaming hasher
     * SipHash流式哈希器
     */
    private static final class SipHasher extends BufferedHasher {

        private final long k0;
        private final long k1;

        SipHasher(long k0, long k1) {
            super(64);
            this.k0 = k0;
            this.k1 = k1;
        }

        @Override
        protected HashCode doHash() {
            byte[] bytes = getBytes();
            return HashCode.fromLong(sipHash24(bytes, 0, bytes.length, k0, k1));
        }
    }
}
