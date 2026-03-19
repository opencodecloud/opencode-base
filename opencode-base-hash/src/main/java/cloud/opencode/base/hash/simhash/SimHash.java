package cloud.opencode.base.hash.simhash;

import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * SimHash algorithm implementation for text fingerprinting
 * SimHash 算法实现用于文本指纹
 *
 * <p>SimHash is a locality-sensitive hashing algorithm that produces similar
 * hash values for similar input texts. Useful for detecting near-duplicate
 * content and text similarity.</p>
 * <p>SimHash 是一种局部敏感哈希算法，为相似的输入文本生成相似的哈希值。
 * 用于检测近似重复内容和文本相似度。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>64-bit and 32-bit fingerprints - 64位和32位指纹</li>
 *   <li>Configurable tokenization - 可配置的分词</li>
 *   <li>Token weighting support - 标记权重支持</li>
 *   <li>Hamming distance calculation - 海明距离计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SimHash simHash = SimHash.builder()
 *     .nGram(3)
 *     .build();
 *
 * long hash1 = simHash.hash("Hello World");
 * long hash2 = simHash.hash("Hello World!");
 *
 * int distance = SimHash.hammingDistance(hash1, hash2);
 * double similarity = SimHash.similarity(hash1, hash2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n * k) where n = tokens, k = hash bits - O(n * k), n为词元数, k为哈希位数</li>
 *   <li>Space complexity: O(k) where k = hash bits (default 64) - O(k), k为哈希位数(默认64)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class SimHash {

    private final Tokenizer tokenizer;
    private final HashFunction hashFunction;
    private final int bits;
    private final Function<String, Integer> weightFunction;

    SimHash(Tokenizer tokenizer, HashFunction hashFunction, int bits,
            Function<String, Integer> weightFunction) {
        this.tokenizer = tokenizer;
        this.hashFunction = hashFunction;
        this.bits = bits;
        this.weightFunction = weightFunction;
    }

    // ==================== Hash Methods | 哈希方法 ====================

    /**
     * Computes SimHash for text
     * 计算文本的SimHash
     *
     * @param text input text | 输入文本
     * @return SimHash value | SimHash值
     */
    public long hash(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        List<String> tokens = tokenizer.tokenize(text);
        if (tokens.isEmpty()) {
            return 0;
        }

        // Initialize bit counters
        int[] v = new int[bits];

        for (String token : tokens) {
            long tokenHash = hashFunction
                    .hashString(token, StandardCharsets.UTF_8)
                    .padToLong();

            int weight = weightFunction != null ? weightFunction.apply(token) : 1;

            for (int i = 0; i < bits; i++) {
                if (((tokenHash >> i) & 1) == 1) {
                    v[i] += weight;
                } else {
                    v[i] -= weight;
                }
            }
        }

        // Build fingerprint
        long fingerprint = 0;
        for (int i = 0; i < bits; i++) {
            if (v[i] > 0) {
                fingerprint |= (1L << i);
            }
        }

        return fingerprint;
    }

    /**
     * Computes a Fingerprint object for text
     * 计算文本的Fingerprint对象
     *
     * @param text input text | 输入文本
     * @return Fingerprint object | Fingerprint对象
     */
    public Fingerprint fingerprint(String text) {
        return new Fingerprint(hash(text), bits);
    }

    /**
     * Gets the number of bits in the fingerprint
     * 获取指纹的位数
     *
     * @return number of bits | 位数
     */
    public int bits() {
        return bits;
    }

    // ==================== Static Utility Methods | 静态工具方法 ====================

    /**
     * Calculates Hamming distance between two hash values
     * 计算两个哈希值之间的海明距离
     *
     * @param hash1 first hash | 第一个哈希
     * @param hash2 second hash | 第二个哈希
     * @return number of different bits | 不同的位数
     */
    public static int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    /**
     * Calculates similarity between two hash values
     * 计算两个哈希值之间的相似度
     *
     * @param hash1 first hash | 第一个哈希
     * @param hash2 second hash | 第二个哈希
     * @return similarity (0.0 - 1.0) | 相似度（0.0 - 1.0）
     */
    public static double similarity(long hash1, long hash2) {
        int distance = hammingDistance(hash1, hash2);
        return 1.0 - (double) distance / 64;
    }

    /**
     * Calculates similarity with specified bits
     * 使用指定位数计算相似度
     *
     * @param hash1 first hash | 第一个哈希
     * @param hash2 second hash | 第二个哈希
     * @param bits  number of bits | 位数
     * @return similarity (0.0 - 1.0) | 相似度（0.0 - 1.0）
     */
    public static double similarity(long hash1, long hash2, int bits) {
        if (bits <= 0) {
            throw new IllegalArgumentException("bits must be positive, got: " + bits);
        }
        int distance = hammingDistance(hash1, hash2);
        return 1.0 - (double) distance / bits;
    }

    /**
     * Checks if two hashes are similar within threshold
     * 检查两个哈希是否在阈值内相似
     *
     * @param hash1     first hash | 第一个哈希
     * @param hash2     second hash | 第二个哈希
     * @param threshold Hamming distance threshold | 海明距离阈值
     * @return true if similar | 如果相似返回true
     */
    public static boolean isSimilar(long hash1, long hash2, int threshold) {
        return hammingDistance(hash1, hash2) <= threshold;
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Creates a builder
     * 创建构建器
     *
     * @return builder | 构建器
     */
    public static SimHashBuilder builder() {
        return new SimHashBuilder();
    }

    /**
     * Creates a default SimHash instance with 3-gram tokenization
     * 创建使用3-gram分词的默认SimHash实例
     *
     * @return SimHash instance | SimHash实例
     */
    public static SimHash create() {
        return builder().nGram(3).build();
    }
}
