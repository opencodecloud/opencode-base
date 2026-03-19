package cloud.opencode.base.hash.bloom;

import cloud.opencode.base.hash.Funnel;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.exception.OpenHashException;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

/**
 * Builder for bloom filter
 * 布隆过滤器构建器
 *
 * <p>Provides a fluent API for configuring and building a bloom filter.</p>
 * <p>提供流畅的API来配置和构建布隆过滤器。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BloomFilter<String> filter = BloomFilter.builder(Funnel.STRING_FUNNEL)
 *     .expectedInsertions(1_000_000)
 *     .fpp(0.01)
 *     .hashFunction(OpenHash.murmur3_128())
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for BloomFilter construction - 流畅的BloomFilter构建器API</li>
 *   <li>Auto-calculates optimal bit size and hash count - 自动计算最优位大小和哈希数量</li>
 *   <li>Configurable false positive rate - 可配置的误判率</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for build() and configuration methods; optimalNumOfBits and optimalNumOfHashFunctions are O(1) math computations - 时间复杂度: build() 和配置方法为 O(1)；optimalNumOfBits 和 optimalNumOfHashFunctions 为 O(1) 数学计算</li>
 *   <li>Space complexity: O(m) where m=optimal bit array size derived from expectedInsertions and fpp - 空间复杂度: O(m)，m 为由 expectedInsertions 和 fpp 推导的最优位数组大小</li>
 * </ul>
 *
 * @param <T> element type | 元素类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class BloomFilterBuilder<T> {

    private final Funnel<? super T> funnel;
    private long expectedInsertions = 1000;
    private double fpp = 0.03;
    private HashFunction hashFunction;
    private boolean threadSafe = true;

    public BloomFilterBuilder(Funnel<? super T> funnel) {
        this.funnel = funnel;
    }

    /**
     * Sets the expected number of insertions
     * 设置预期插入数量
     *
     * @param expectedInsertions expected insertions | 预期插入量
     * @return this builder | 此构建器
     */
    public BloomFilterBuilder<T> expectedInsertions(long expectedInsertions) {
        if (expectedInsertions <= 0) {
            throw OpenHashException.invalidBloomFilterConfig("Expected insertions must be positive");
        }
        this.expectedInsertions = expectedInsertions;
        return this;
    }

    /**
     * Sets the false positive probability (0.0 - 1.0)
     * 设置误判率（0.0 - 1.0）
     *
     * @param fpp false positive probability | 误判率
     * @return this builder | 此构建器
     */
    public BloomFilterBuilder<T> fpp(double fpp) {
        if (fpp <= 0 || fpp >= 1) {
            throw OpenHashException.invalidBloomFilterConfig("FPP must be between 0 and 1 (exclusive)");
        }
        this.fpp = fpp;
        return this;
    }

    /**
     * Sets the hash function
     * 设置哈希函数
     *
     * @param hashFunction hash function | 哈希函数
     * @return this builder | 此构建器
     */
    public BloomFilterBuilder<T> hashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        return this;
    }

    /**
     * Sets whether to use thread-safe bit array
     * 设置是否使用线程安全的位数组
     *
     * @param threadSafe whether to be thread-safe | 是否线程安全
     * @return this builder | 此构建器
     */
    public BloomFilterBuilder<T> threadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
        return this;
    }

    /**
     * Builds the bloom filter
     * 构建布隆过滤器
     *
     * @return bloom filter | 布隆过滤器
     */
    public BloomFilter<T> build() {
        // Calculate optimal parameters
        // m = -n * ln(p) / (ln(2))^2
        // k = (m/n) * ln(2)

        long optimalBitSize = optimalNumOfBits(expectedInsertions, fpp);
        int optimalHashFunctions = optimalNumOfHashFunctions(expectedInsertions, optimalBitSize);

        BitArray bits = new BitArray(optimalBitSize, threadSafe);
        HashFunction hf = hashFunction != null ? hashFunction : Murmur3HashFunction.murmur3_128();

        return new BloomFilter<>(bits, optimalHashFunctions, funnel, hf, expectedInsertions, fpp);
    }

    /**
     * Calculates optimal bit array size
     * 计算最优位数组大小
     */
    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * Calculates optimal number of hash functions
     * 计算最优哈希函数数量
     */
    static int optimalNumOfHashFunctions(long n, long m) {
        int k = Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
        return Math.min(k, 30); // Cap at 30 hash functions
    }
}
