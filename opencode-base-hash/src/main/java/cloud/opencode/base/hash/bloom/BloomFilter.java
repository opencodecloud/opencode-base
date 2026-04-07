package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import cloud.opencode.base.hash.exception.OpenHashException;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

import java.util.function.Predicate;

/**
 * Bloom filter implementation
 * 布隆过滤器实现
 *
 * <p>A space-efficient probabilistic data structure for membership testing.
 * Returns no false negatives, but may return false positives.</p>
 * <p>一种用于成员测试的空间高效概率数据结构。不会返回假阴性，但可能返回假阳性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable expected insertions and FPP - 可配置的预期插入量和误判率</li>
 *   <li>No false negatives guaranteed - 保证无假阴性</li>
 *   <li>Serialization support - 序列化支持</li>
 *   <li>Merge operation - 合并操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BloomFilter<String> filter = BloomFilter.builder(Funnel.STRING_FUNNEL)
 *     .expectedInsertions(1_000_000)
 *     .fpp(0.01)
 *     .build();
 *
 * filter.put("item1");
 * if (filter.mightContain("item1")) {
 *     // Might be present
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Optional (via BitArray) - 线程安全: 可选（通过BitArray）</li>
 * </ul>
 *
 * @param <T> element type | 元素类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class BloomFilter<T> implements Predicate<T> {

    private final BitArray bits;
    private final int numHashFunctions;
    private final Funnel<? super T> funnel;
    private final HashFunction hashFunction;
    private final long expectedInsertions;
    private final double fpp;

    BloomFilter(BitArray bits, int numHashFunctions, Funnel<? super T> funnel,
                HashFunction hashFunction, long expectedInsertions, double fpp) {
        this.bits = bits;
        this.numHashFunctions = numHashFunctions;
        this.funnel = funnel;
        this.hashFunction = hashFunction;
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
    }

    // ==================== Core Operations | 核心操作 ====================

    /**
     * Adds an element to the filter
     * 向过滤器添加元素
     *
     * @param element the element | 元素
     * @return true if the filter may have been changed | 如果过滤器可能已更改返回true
     */
    public boolean put(T element) {
        long[] indices = getHashIndices(element);
        boolean changed = false;
        for (long index : indices) {
            changed |= bits.set(index);
        }
        return changed;
    }

    /**
     * Adds multiple elements to the filter
     * 向过滤器添加多个元素
     *
     * @param elements the elements | 元素
     * @return number of elements that may have caused changes | 可能导致更改的元素数量
     */
    public int putAll(Iterable<? extends T> elements) {
        int count = 0;
        for (T element : elements) {
            if (put(element)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tests if an element might be in the filter
     * 测试元素是否可能在过滤器中
     *
     * @param element the element | 元素
     * @return false means definitely not present, true means possibly present |
     *         false表示肯定不存在，true表示可能存在
     */
    @Override
    public boolean test(T element) {
        return mightContain(element);
    }

    /**
     * Tests if an element might be in the filter
     * 测试元素是否可能在过滤器中
     *
     * @param element the element | 元素
     * @return false means definitely not present, true means possibly present |
     *         false表示肯定不存在，true表示可能存在
     */
    public boolean mightContain(T element) {
        long[] indices = getHashIndices(element);
        for (long index : indices) {
            if (!bits.get(index)) {
                return false;
            }
        }
        return true;
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Gets the expected false positive probability based on current fill
     * 根据当前填充获取预期的假阳性概率
     *
     * @return current expected FPP | 当前预期误判率
     */
    public double expectedFpp() {
        double bitsSet = bits.bitCount();
        double bitRatio = bitsSet / bits.bitSize();
        return Math.pow(bitRatio, numHashFunctions);
    }

    /**
     * Gets the approximate number of elements inserted
     * 获取大约的已插入元素数量
     *
     * @return approximate element count | 大约元素数量
     */
    public long approximateElementCount() {
        double bitsSet = bits.bitCount();
        double bitSize = bits.bitSize();
        double k = numHashFunctions;

        if (bitsSet == 0) {
            return 0;
        }
        if (bitsSet >= bitSize) {
            return (long) bitSize;
        }

        // Formula: n ≈ -(m/k) * ln(1 - x/m)
        // where m = bit size, k = hash functions, x = bits set
        return (long) (-(bitSize / k) * Math.log(1 - bitsSet / bitSize));
    }

    /**
     * Gets the bit array size
     * 获取位数组大小
     *
     * @return bit size | 位数
     */
    public long bitSize() {
        return bits.bitSize();
    }

    /**
     * Gets the number of hash functions
     * 获取哈希函数数量
     *
     * @return hash function count | 哈希函数数量
     */
    public int hashCount() {
        return numHashFunctions;
    }

    // ==================== Merge and Serialization | 合并和序列化 ====================

    /**
     * Merges another bloom filter into this one
     * 将另一个布隆过滤器合并到此过滤器
     *
     * @param other the other filter | 另一个过滤器
     * @return this filter | 此过滤器
     */
    public BloomFilter<T> merge(BloomFilter<T> other) {
        if (this.bits.bitSize() != other.bits.bitSize()) {
            throw OpenHashException.invalidBloomFilterConfig("Bit sizes must match for merge");
        }
        if (this.numHashFunctions != other.numHashFunctions) {
            throw OpenHashException.invalidBloomFilterConfig("Hash function counts must match for merge");
        }
        this.bits.or(other.bits);
        return this;
    }

    /**
     * Serializes to byte array
     * 序列化为字节数组
     *
     * @return byte array | 字节数组
     */
    public byte[] toBytes() {
        byte[] bitsBytes = bits.toBytes();
        byte[] result = new byte[4 + bitsBytes.length];

        // Write hash function count
        result[0] = (byte) numHashFunctions;
        result[1] = (byte) (numHashFunctions >> 8);
        result[2] = (byte) (numHashFunctions >> 16);
        result[3] = (byte) (numHashFunctions >> 24);

        System.arraycopy(bitsBytes, 0, result, 4, bitsBytes.length);
        return result;
    }

    /**
     * Deserializes from byte array
     * 从字节数组反序列化
     *
     * @param bytes  byte array | 字节数组
     * @param funnel element funnel | 元素funnel
     * @param <T>    element type | 元素类型
     * @return bloom filter | 布隆过滤器
     * @throws OpenHashException if bytes are invalid | 如果字节无效则抛出异常
     */
    public static <T> BloomFilter<T> fromBytes(byte[] bytes, Funnel<? super T> funnel) {
        if (bytes == null) {
            throw OpenHashException.invalidBloomFilterConfig("Byte array cannot be null");
        }
        // Minimum: 4 bytes for numHashFunctions + 8 bytes for BitArray header
        if (bytes.length < 12) {
            throw OpenHashException.invalidBloomFilterConfig("Invalid byte array length: " + bytes.length);
        }

        int numHashFunctions = (bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24);

        // Validate numHashFunctions range (must be 1-30)
        if (numHashFunctions < 1 || numHashFunctions > 30) {
            throw OpenHashException.invalidBloomFilterConfig(
                "Invalid number of hash functions: " + numHashFunctions + " (must be 1-30)");
        }

        byte[] bitsBytes = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, bitsBytes, 0, bitsBytes.length);

        BitArray nonSafeBits = BitArray.fromBytes(bitsBytes);
        // Create a thread-safe BitArray by copying data into an AtomicLongArray-backed array
        BitArray bits = new BitArray(nonSafeBits.bitSize(), true);
        nonSafeBits.or(bits); // no-op since bits is empty, but we need to copy the other way
        bits.or(nonSafeBits); // merge deserialized data into thread-safe array
        return new BloomFilter<>(bits, numHashFunctions, funnel,
                Murmur3HashFunction.murmur3_128(), 0, 0);
    }

    // ==================== Internal Methods | 内部方法 ====================

    private long[] getHashIndices(T element) {
        Hasher hasher = hashFunction.newHasher();
        funnel.funnel(element, hasher);
        HashCode hashCode = hasher.hash();

        long hash1;
        long hash2;
        if (hashCode.bits() >= 128) {
            // Single asBytes() call to extract both hash1 and hash2,
            // avoiding separate padToLong() + asBytes() double allocation
            byte[] bytes = hashCode.asBytes();
            hash1 = 0;
            for (int i = 0; i < 8; i++) {
                hash1 |= (bytes[i] & 0xFFL) << (i * 8);
            }
            hash2 = 0;
            for (int i = 8; i < 16 && i < bytes.length; i++) {
                hash2 |= (bytes[i] & 0xFFL) << ((i - 8) * 8);
            }
        } else {
            hash1 = hashCode.padToLong();
            hash2 = hash1 >>> 32;
            if (hash2 == 0) {
                // For < 64-bit hashes where upper bits are zero,
                // derive hash2 via bit mixing to ensure index independence
                hash2 = hash1 * 0x9E3779B97F4A7C15L;
            }
        }

        long[] indices = new long[numHashFunctions];
        long bitSize = bits.bitSize();

        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            // Ensure positive
            indices[i] = (combinedHash & Long.MAX_VALUE) % bitSize;
        }

        return indices;
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a builder
     * 创建构建器
     *
     * @param funnel element funnel | 元素funnel
     * @param <T>    element type | 元素类型
     * @return builder | 构建器
     */
    public static <T> BloomFilterBuilder<T> builder(Funnel<? super T> funnel) {
        return new BloomFilterBuilder<>(funnel);
    }
}
