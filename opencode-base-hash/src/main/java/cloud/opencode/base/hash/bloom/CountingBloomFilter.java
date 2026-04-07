package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import cloud.opencode.base.hash.exception.OpenHashException;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

/**
 * Counting bloom filter implementation
 * 计数布隆过滤器实现
 *
 * <p>A variant of bloom filter that supports deletion by using counters
 * instead of single bits. Each position has a counter that is incremented
 * on insert and decremented on delete.</p>
 * <p>布隆过滤器的变体，通过使用计数器而不是单个位来支持删除。
 * 每个位置都有一个计数器，在插入时递增，在删除时递减。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Supports deletion - 支持删除</li>
 *   <li>Configurable counter bits - 可配置的计数器位数</li>
 *   <li>Count estimation - 计数估计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CountingBloomFilter<String> filter = CountingBloomFilter.builder(Funnel.STRING_FUNNEL)
 *     .expectedInsertions(100_000)
 *     .fpp(0.01)
 *     .counterBits(4)
 *     .build();
 *
 * filter.put("item1");
 * filter.put("item1");  // Count = 2
 * filter.remove("item1");  // Count = 1
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use external synchronization) - 线程安全: 否（使用外部同步）</li>
 * </ul>
 *
 * @param <T> element type | 元素类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class CountingBloomFilter<T> {

    private final int[] counters;
    private final int numBuckets;
    private final int numHashFunctions;
    private final int maxCount;
    private final Funnel<? super T> funnel;
    private final HashFunction hashFunction;

    private CountingBloomFilter(int numBuckets, int numHashFunctions, int counterBits,
                                Funnel<? super T> funnel, HashFunction hashFunction) {
        this.counters = new int[numBuckets];
        this.numBuckets = numBuckets;
        this.numHashFunctions = numHashFunctions;
        this.maxCount = (1 << counterBits) - 1;
        this.funnel = funnel;
        this.hashFunction = hashFunction;
    }

    // ==================== Core Operations | 核心操作 ====================

    /**
     * Adds an element to the filter
     * 向过滤器添加元素
     *
     * @param element the element | 元素
     * @return true if successful | 如果成功返回true
     */
    public boolean put(T element) {
        int[] indices = getHashIndices(element);
        boolean success = true;
        for (int index : indices) {
            if (counters[index] < maxCount) {
                counters[index]++;
            } else {
                success = false; // Counter overflow
            }
        }
        return success;
    }

    /**
     * Removes an element from the filter
     * 从过滤器移除元素
     *
     * @param element the element | 元素
     * @return true if successful | 如果成功返回true
     */
    public boolean remove(T element) {
        int[] indices = getHashIndices(element);

        // First check if element might exist
        for (int index : indices) {
            if (counters[index] == 0) {
                return false; // Element definitely not present
            }
        }

        // Decrement counters
        for (int index : indices) {
            if (counters[index] > 0) {
                counters[index]--;
            }
        }
        return true;
    }

    /**
     * Tests if an element might be in the filter
     * 测试元素是否可能在过滤器中
     *
     * @param element the element | 元素
     * @return true if might contain | 如果可能包含返回true
     */
    public boolean mightContain(T element) {
        int[] indices = getHashIndices(element);
        for (int index : indices) {
            if (counters[index] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the minimum count for an element
     * 获取元素的最小计数
     *
     * @param element the element | 元素
     * @return minimum count across all positions | 所有位置的最小计数
     */
    public int count(T element) {
        int[] indices = getHashIndices(element);
        int minCount = Integer.MAX_VALUE;
        for (int index : indices) {
            minCount = Math.min(minCount, counters[index]);
        }
        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    /**
     * Clears all counters
     * 清除所有计数器
     */
    public void clear() {
        java.util.Arrays.fill(counters, 0);
    }

    // ==================== Internal Methods | 内部方法 ====================

    private int[] getHashIndices(T element) {
        Hasher hasher = hashFunction.newHasher();
        funnel.funnel(element, hasher);
        HashCode hashCode = hasher.hash();

        long hash1;
        long hash2;
        if (hashCode.bits() >= 128) {
            // Single asBytes() call to extract both hash1 and hash2
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
                hash2 = hash1 * 0x9E3779B97F4A7C15L;
            }
        }

        int[] indices = new int[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            indices[i] = (int) ((combinedHash & Long.MAX_VALUE) % numBuckets);
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
    public static <T> Builder<T> builder(Funnel<? super T> funnel) {
        return new Builder<>(funnel);
    }

    /**
     * Builder for counting bloom filter
     * 计数布隆过滤器构建器
     *
     * @param <T> element type | 元素类型
     */
    public static final class Builder<T> {

        private final Funnel<? super T> funnel;
        private long expectedInsertions = 1000;
        private double fpp = 0.03;
        private int counterBits = 4;
        private HashFunction hashFunction;

        public Builder(Funnel<? super T> funnel) {
            this.funnel = funnel;
        }

        /**
         * Sets the expected insertions
         * 设置预期插入量
         *
         * @param expectedInsertions expected insertions | 预期插入量
         * @return this builder | 此构建器
         */
        public Builder<T> expectedInsertions(long expectedInsertions) {
            if (expectedInsertions <= 0) {
                throw OpenHashException.invalidBloomFilterConfig("Expected insertions must be positive");
            }
            this.expectedInsertions = expectedInsertions;
            return this;
        }

        /**
         * Sets the false positive probability
         * 设置误判率
         *
         * @param fpp false positive probability | 误判率
         * @return this builder | 此构建器
         */
        public Builder<T> fpp(double fpp) {
            if (fpp <= 0 || fpp >= 1) {
                throw OpenHashException.invalidBloomFilterConfig("FPP must be between 0 and 1");
            }
            this.fpp = fpp;
            return this;
        }

        /**
         * Sets the counter bits (default 4)
         * 设置计数器位数（默认4）
         *
         * @param bits counter bits | 计数器位数
         * @return this builder | 此构建器
         */
        public Builder<T> counterBits(int bits) {
            if (bits <= 0 || bits > 16) {
                throw OpenHashException.invalidBloomFilterConfig("Counter bits must be between 1 and 16");
            }
            this.counterBits = bits;
            return this;
        }

        /**
         * Sets the hash function
         * 设置哈希函数
         *
         * @param hashFunction hash function | 哈希函数
         * @return this builder | 此构建器
         */
        public Builder<T> hashFunction(HashFunction hashFunction) {
            this.hashFunction = hashFunction;
            return this;
        }

        /**
         * Builds the counting bloom filter
         * 构建计数布隆过滤器
         *
         * @return counting bloom filter | 计数布隆过滤器
         */
        public CountingBloomFilter<T> build() {
            long optimalBitSize = BloomFilterBuilder.optimalNumOfBits(expectedInsertions, fpp);
            int numBuckets = (int) Math.min(optimalBitSize, Integer.MAX_VALUE);
            int numHashFunctions = BloomFilterBuilder.optimalNumOfHashFunctions(expectedInsertions, optimalBitSize);

            HashFunction hf = hashFunction != null ? hashFunction : Murmur3HashFunction.murmur3_128();

            return new CountingBloomFilter<>(numBuckets, numHashFunctions, counterBits, funnel, hf);
        }
    }
}
