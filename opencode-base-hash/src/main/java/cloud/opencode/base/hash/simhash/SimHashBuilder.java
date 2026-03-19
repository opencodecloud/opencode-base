package cloud.opencode.base.hash.simhash;

import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.function.Murmur3HashFunction;

import java.util.List;
import java.util.function.Function;

/**
 * Builder for SimHash configuration
 * SimHash 配置构建器
 *
 * <p>Provides a fluent API for configuring SimHash instances with
 * custom tokenizers, hash functions, and weighting strategies.</p>
 * <p>提供流畅的API来配置带有自定义分词器、哈希函数和权重策略的SimHash实例。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SimHash simHash = SimHash.builder()
 *     .nGram(3)
 *     .hashFunction(OpenHash.murmur3_64())
 *     .bits(64)
 *     .build();
 *
 * // With custom tokenizer
 * SimHash custom = SimHash.builder()
 *     .tokenizer(text -> Arrays.asList(text.split(",")))
 *     .weightFunction(token -> token.length())
 *     .build();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for SimHash construction - 流畅的SimHash构建器API</li>
 *   <li>Configurable hash bit length - 可配置哈希位长度</li>
 *   <li>Custom tokenizer support - 自定义分词器支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, create per-thread) - 线程安全: 否（构建器模式，每线程创建）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for build() - configuration-only, no hashing performed - 时间复杂度: build() 为 O(1) - 仅配置，不执行哈希计算</li>
 *   <li>Space complexity: O(1) - stores only configuration references - 空间复杂度: O(1) - 仅存储配置引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class SimHashBuilder {

    private Tokenizer tokenizer;
    private HashFunction hashFunction;
    private int bits = 64;
    private Function<String, Integer> weightFunction;

    SimHashBuilder() {
    }

    /**
     * Sets a custom tokenizer
     * 设置自定义分词器
     *
     * @param tokenizer tokenizer function | 分词器函数
     * @return this builder | 此构建器
     */
    public SimHashBuilder tokenizer(Function<String, List<String>> tokenizer) {
        this.tokenizer = tokenizer::apply;
        return this;
    }

    /**
     * Sets a Tokenizer instance
     * 设置Tokenizer实例
     *
     * @param tokenizer tokenizer | 分词器
     * @return this builder | 此构建器
     */
    public SimHashBuilder tokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    /**
     * Uses N-gram tokenization
     * 使用N-gram分词
     *
     * @param n gram size | gram大小
     * @return this builder | 此构建器
     */
    public SimHashBuilder nGram(int n) {
        this.tokenizer = Tokenizer.ngram(n);
        return this;
    }

    /**
     * Uses whitespace tokenization
     * 使用空格分词
     *
     * @return this builder | 此构建器
     */
    public SimHashBuilder whitespaceTokenizer() {
        this.tokenizer = Tokenizer.whitespace();
        return this;
    }

    /**
     * Uses word tokenization
     * 使用单词分词
     *
     * @return this builder | 此构建器
     */
    public SimHashBuilder wordTokenizer() {
        this.tokenizer = Tokenizer.words();
        return this;
    }

    /**
     * Uses character tokenization
     * 使用字符分词
     *
     * @return this builder | 此构建器
     */
    public SimHashBuilder characterTokenizer() {
        this.tokenizer = Tokenizer.characters();
        return this;
    }

    /**
     * Sets the hash function for token hashing
     * 设置用于标记哈希的哈希函数
     *
     * @param hashFunction hash function | 哈希函数
     * @return this builder | 此构建器
     */
    public SimHashBuilder hashFunction(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        return this;
    }

    /**
     * Sets the fingerprint bit size (32 or 64)
     * 设置指纹位大小（32或64）
     *
     * @param bits bit size | 位大小
     * @return this builder | 此构建器
     */
    public SimHashBuilder bits(int bits) {
        if (bits != 32 && bits != 64) {
            throw new IllegalArgumentException("Bits must be 32 or 64");
        }
        this.bits = bits;
        return this;
    }

    /**
     * Sets the token weight function
     * 设置标记权重函数
     *
     * @param weightFunction weight function | 权重函数
     * @return this builder | 此构建器
     */
    public SimHashBuilder weightFunction(Function<String, Integer> weightFunction) {
        this.weightFunction = weightFunction;
        return this;
    }

    /**
     * Uses token length as weight
     * 使用标记长度作为权重
     *
     * @return this builder | 此构建器
     */
    public SimHashBuilder lengthWeighted() {
        this.weightFunction = String::length;
        return this;
    }

    /**
     * Uses uniform weight (1 for all tokens)
     * 使用均匀权重（所有标记为1）
     *
     * @return this builder | 此构建器
     */
    public SimHashBuilder uniformWeight() {
        this.weightFunction = null;
        return this;
    }

    /**
     * Builds the SimHash instance
     * 构建SimHash实例
     *
     * @return SimHash instance | SimHash实例
     */
    public SimHash build() {
        Tokenizer tok = tokenizer != null ? tokenizer : Tokenizer.ngram(3);
        HashFunction hf = hashFunction != null ? hashFunction : Murmur3HashFunction.murmur3_128();

        return new SimHash(tok, hf, bits, weightFunction);
    }
}
