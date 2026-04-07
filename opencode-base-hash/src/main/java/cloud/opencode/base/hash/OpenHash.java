package cloud.opencode.base.hash;

import cloud.opencode.base.hash.bloom.BloomFilter;
import cloud.opencode.base.hash.bloom.BloomFilterBuilder;
import cloud.opencode.base.hash.bloom.CountingBloomFilter;
import cloud.opencode.base.hash.consistent.ConsistentHash;
import cloud.opencode.base.hash.consistent.ConsistentHashBuilder;
import cloud.opencode.base.hash.function.*;
import cloud.opencode.base.hash.simhash.SimHash;
import cloud.opencode.base.hash.simhash.SimHashBuilder;

import java.nio.charset.Charset;

/**
 * Hash utility facade class
 * 哈希工具门面类
 *
 * <p>Provides a unified entry point for all hash functions and data structures.
 * This is the main API for hash operations in the library.</p>
 * <p>为所有哈希函数和数据结构提供统一的入口点。
 * 这是库中哈希操作的主要API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Non-cryptographic hashes: MurmurHash3, xxHash, FNV-1a, CRC32, SipHash, Adler32 - 非加密哈希</li>
 *   <li>Cryptographic hashes: MD5, SHA-1, SHA-256, SHA-384, SHA-512, SHA-3 - 加密哈希</li>
 *   <li>HMAC: HMAC-MD5, HMAC-SHA1, HMAC-SHA256, HMAC-SHA384, HMAC-SHA512 - HMAC消息认证</li>
 *   <li>Consistent hash ring - 一致性哈希环</li>
 *   <li>Bloom filter - 布隆过滤器</li>
 *   <li>Counting bloom filter - 计数布隆过滤器</li>
 *   <li>SimHash for text similarity - SimHash用于文本相似度</li>
 *   <li>HashCodes combiner for efficient hashCode() - HashCodes组合器用于高效hashCode()</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Hash a string
 * HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello World");
 *
 * // Consistent hash ring
 * ConsistentHash<String> ring = OpenHash.<String>consistentHash()
 *     .addNode("server1", "192.168.1.1")
 *     .build();
 *
 * // Bloom filter
 * BloomFilter<String> filter = OpenHash.<String>bloomFilter()
 *     .funnel(Funnel.STRING_FUNNEL)
 *     .expectedInsertions(1_000_000)
 *     .build();
 *
 * // SimHash
 * SimHash simHash = OpenHash.simHash().nGram(3).build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (factory class) - 线程安全: 是（工厂类）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) for hash computation - 哈希计算 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class OpenHash {

    private OpenHash() {
        // Utility class
    }

    // ==================== Non-Cryptographic Hashes | 非加密哈希 ====================

    /**
     * MurmurHash3 32-bit
     * MurmurHash3 32位
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction murmur3_32() {
        return Murmur3HashFunction.murmur3_32();
    }

    /**
     * MurmurHash3 32-bit with seed
     * 带种子的MurmurHash3 32位
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static HashFunction murmur3_32(int seed) {
        return Murmur3HashFunction.murmur3_32(seed);
    }

    /**
     * MurmurHash3 128-bit
     * MurmurHash3 128位
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction murmur3_128() {
        return Murmur3HashFunction.murmur3_128();
    }

    /**
     * MurmurHash3 128-bit with seed
     * 带种子的MurmurHash3 128位
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static HashFunction murmur3_128(int seed) {
        return Murmur3HashFunction.murmur3_128(seed);
    }

    /**
     * xxHash 64-bit
     * xxHash 64位
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction xxHash64() {
        return XxHashFunction.xxHash64();
    }

    /**
     * xxHash 64-bit with seed
     * 带种子的xxHash 64位
     *
     * @param seed seed value | 种子值
     * @return hash function | 哈希函数
     */
    public static HashFunction xxHash64(long seed) {
        return XxHashFunction.xxHash64(seed);
    }

    /**
     * FNV-1a 32-bit
     * FNV-1a 32位
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction fnv1a_32() {
        return Fnv1aHashFunction.fnv1a_32();
    }

    /**
     * FNV-1a 64-bit
     * FNV-1a 64位
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction fnv1a_64() {
        return Fnv1aHashFunction.fnv1a_64();
    }

    /**
     * CRC32
     * CRC32
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction crc32() {
        return Crc32HashFunction.crc32();
    }

    /**
     * CRC32C (Castagnoli)
     * CRC32C (Castagnoli)
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction crc32c() {
        return Crc32HashFunction.crc32c();
    }

    /**
     * Adler-32 checksum
     * Adler-32 校验和
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction adler32() {
        return Adler32HashFunction.adler32();
    }

    /**
     * SipHash-2-4 with default key
     * 默认密钥的 SipHash-2-4
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sipHash24() {
        return SipHashFunction.sipHash24();
    }

    /**
     * SipHash-2-4 with specified key
     * 指定密钥的 SipHash-2-4
     *
     * @param k0 first key half | 密钥前半部分
     * @param k1 second key half | 密钥后半部分
     * @return hash function | 哈希函数
     */
    public static HashFunction sipHash24(long k0, long k1) {
        return SipHashFunction.sipHash24(k0, k1);
    }

    // ==================== Cryptographic Hashes | 加密哈希 ====================

    /**
     * MD5 (for checksums only, not secure)
     * MD5（仅用于校验，不安全）
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction md5() {
        return MessageDigestHashFunction.md5();
    }

    /**
     * SHA-1 (for checksums only, not secure)
     * SHA-1（仅用于校验，不安全）
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha1() {
        return MessageDigestHashFunction.sha1();
    }

    /**
     * SHA-256
     * SHA-256
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha256() {
        return MessageDigestHashFunction.sha256();
    }

    /**
     * SHA-512
     * SHA-512
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha512() {
        return MessageDigestHashFunction.sha512();
    }

    /**
     * SHA3-256
     * SHA3-256
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha3_256() {
        return MessageDigestHashFunction.sha3_256();
    }

    /**
     * SHA-384
     * SHA-384
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha384() {
        return MessageDigestHashFunction.sha384();
    }

    /**
     * SHA3-512
     * SHA3-512
     *
     * @return hash function | 哈希函数
     */
    public static HashFunction sha3_512() {
        return MessageDigestHashFunction.sha3_512();
    }

    // ==================== HMAC | HMAC消息认证码 ====================

    /**
     * HMAC-MD5
     * HMAC-MD5
     *
     * @param key secret key bytes | 密钥字节
     * @return hash function | 哈希函数
     */
    public static HashFunction hmacMd5(byte[] key) {
        return HmacHashFunction.hmacMd5(key);
    }

    /**
     * HMAC-SHA1
     * HMAC-SHA1
     *
     * @param key secret key bytes | 密钥字节
     * @return hash function | 哈希函数
     */
    public static HashFunction hmacSha1(byte[] key) {
        return HmacHashFunction.hmacSha1(key);
    }

    /**
     * HMAC-SHA256
     * HMAC-SHA256
     *
     * @param key secret key bytes | 密钥字节
     * @return hash function | 哈希函数
     */
    public static HashFunction hmacSha256(byte[] key) {
        return HmacHashFunction.hmacSha256(key);
    }

    /**
     * HMAC-SHA384
     * HMAC-SHA384
     *
     * @param key secret key bytes | 密钥字节
     * @return hash function | 哈希函数
     */
    public static HashFunction hmacSha384(byte[] key) {
        return HmacHashFunction.hmacSha384(key);
    }

    /**
     * HMAC-SHA512
     * HMAC-SHA512
     *
     * @param key secret key bytes | 密钥字节
     * @return hash function | 哈希函数
     */
    public static HashFunction hmacSha512(byte[] key) {
        return HmacHashFunction.hmacSha512(key);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Hashes a string
     * 哈希字符串
     *
     * @param input    input string | 输入字符串
     * @param charset  character set | 字符集
     * @param function hash function | 哈希函数
     * @return hash code | 哈希码
     */
    public static HashCode hash(CharSequence input, Charset charset, HashFunction function) {
        return function.hashString(input, charset);
    }

    /**
     * Hashes a byte array
     * 哈希字节数组
     *
     * @param input    input bytes | 输入字节
     * @param function hash function | 哈希函数
     * @return hash code | 哈希码
     */
    public static HashCode hash(byte[] input, HashFunction function) {
        return function.hashBytes(input);
    }

    /**
     * Combines hash codes in order
     * 按顺序组合哈希码
     *
     * @param hashCodes hash codes to combine | 要组合的哈希码
     * @return combined hash code | 组合的哈希码
     */
    public static HashCode combineOrdered(HashCode... hashCodes) {
        if (hashCodes == null || hashCodes.length == 0) {
            return HashCode.fromInt(0);
        }
        Hasher hasher = murmur3_128().newHasher();
        for (HashCode hc : hashCodes) {
            hasher.putBytes(hc.asBytes());
        }
        return hasher.hash();
    }

    /**
     * Combines hash codes unordered (XOR)
     * 无序组合哈希码（XOR）
     *
     * @param hashCodes hash codes to combine | 要组合的哈希码
     * @return combined hash code | 组合的哈希码
     */
    public static HashCode combineUnordered(HashCode... hashCodes) {
        if (hashCodes == null || hashCodes.length == 0) {
            return HashCode.fromInt(0);
        }
        long combined = 0;
        for (HashCode hc : hashCodes) {
            combined ^= hc.padToLong();
        }
        return HashCode.fromLong(combined);
    }

    // ==================== Data Structure Builders | 数据结构构建器 ====================

    /**
     * Creates a consistent hash ring builder
     * 创建一致性哈希环构建器
     *
     * @param <T> node data type | 节点数据类型
     * @return builder | 构建器
     */
    public static <T> ConsistentHashBuilder<T> consistentHash() {
        return ConsistentHash.builder();
    }

    /**
     * Creates a bloom filter builder
     * 创建布隆过滤器构建器
     *
     * @param <T> element type | 元素类型
     * @return builder | 构建器
     */
    public static <T> BloomFilterBuilder<T> bloomFilter() {
        return new BloomFilterBuilder<>(null);
    }

    /**
     * Creates a bloom filter builder with funnel
     * 使用funnel创建布隆过滤器构建器
     *
     * @param funnel element funnel | 元素funnel
     * @param <T>    element type | 元素类型
     * @return builder | 构建器
     */
    public static <T> BloomFilterBuilder<T> bloomFilter(Funnel<? super T> funnel) {
        return BloomFilter.builder(funnel);
    }

    /**
     * Creates a counting bloom filter builder
     * 创建计数布隆过滤器构建器
     *
     * @param <T> element type | 元素类型
     * @return builder | 构建器
     */
    public static <T> CountingBloomFilter.Builder<T> countingBloomFilter() {
        return new CountingBloomFilter.Builder<>(null);
    }

    /**
     * Creates a counting bloom filter builder with funnel
     * 使用funnel创建计数布隆过滤器构建器
     *
     * @param funnel element funnel | 元素funnel
     * @param <T>    element type | 元素类型
     * @return builder | 构建器
     */
    public static <T> CountingBloomFilter.Builder<T> countingBloomFilter(Funnel<? super T> funnel) {
        return CountingBloomFilter.builder(funnel);
    }

    /**
     * Creates a SimHash builder
     * 创建SimHash构建器
     *
     * @return builder | 构建器
     */
    public static SimHashBuilder simHash() {
        return SimHash.builder();
    }
}
