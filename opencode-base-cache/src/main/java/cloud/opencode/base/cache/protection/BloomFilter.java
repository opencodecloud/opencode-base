package cloud.opencode.base.cache.protection;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bloom Filter - Probabilistic data structure for cache penetration prevention
 * 布隆过滤器 - 用于防止缓存穿透的概率数据结构
 *
 * <p>Space-efficient probabilistic data structure to test set membership.</p>
 * <p>空间高效的概率数据结构，用于测试集合成员关系。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>False positive possible, false negative impossible - 可能误判存在，不会误判不存在</li>
 *   <li>O(k) add and query - O(k) 添加和查询</li>
 *   <li>Space efficient - 空间高效</li>
 *   <li>Configurable false positive rate - 可配置误判率</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BloomFilter<String> filter = new BloomFilter<>(100000, 0.01);
 * filter.add("existing-key");
 *
 * // Before cache lookup - 缓存查询前
 * if (!filter.mightContain(key)) {
 *     return null; // Definitely not exists - 一定不存在
 * }
 * return cache.get(key); // Might exist - 可能存在
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k) where k is hash count - 时间复杂度: O(k) k 为哈希次数</li>
 *   <li>Space complexity: O(m) bits - 空间复杂度: O(m) 位</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses AtomicLongArray with CAS) - 线程安全: 是（使用AtomicLongArray和CAS）</li>
 *   <li>Null-safe: No (throws on null) - 空值安全: 否（null 时抛异常）</li>
 * </ul>
 *
 * @param <T> the type of elements | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class BloomFilter<T> {

    private final AtomicLongArray bits;
    private final int bitSize;
    private final int hashCount;
    private final AtomicLong insertedCount;

    /**
     * Create bloom filter with expected insertions and false positive probability
     * 使用预期插入数和误判率创建布隆过滤器
     *
     * @param expectedInsertions expected number of insertions | 预期插入数
     * @param fpp                false positive probability (0 &lt; fpp &lt; 1) | 误判率
     */
    public BloomFilter(long expectedInsertions, double fpp) {
        if (expectedInsertions <= 0) {
            throw new IllegalArgumentException("Expected insertions must be positive");
        }
        if (fpp <= 0 || fpp >= 1) {
            throw new IllegalArgumentException("FPP must be between 0 and 1");
        }

        this.bitSize = optimalBitSize(expectedInsertions, fpp);
        this.hashCount = optimalHashCount(expectedInsertions, bitSize);
        this.bits = new AtomicLongArray((bitSize + 63) / 64);
        this.insertedCount = new AtomicLong(0);
    }

    /**
     * Create bloom filter with specific bit size and hash count
     * 使用特定位大小和哈希次数创建布隆过滤器
     *
     * @param bitSize   number of bits | 位数
     * @param hashCount number of hash functions | 哈希函数数
     */
    public BloomFilter(int bitSize, int hashCount) {
        this.bitSize = bitSize;
        this.hashCount = hashCount;
        this.bits = new AtomicLongArray((bitSize + 63) / 64);
        this.insertedCount = new AtomicLong(0);
    }

    /**
     * Add element to filter
     * 添加元素到过滤器
     *
     * @param element the element | 元素
     * @return true if bits changed (element was new) | 位改变返回 true（元素是新的）
     */
    public boolean add(T element) {
        boolean bitsChanged = false;
        int hash1 = hash1(element);
        int hash2 = hash2(element);

        for (int i = 0; i < hashCount; i++) {
            int index = ((hash1 + i * hash2) % bitSize + bitSize) % bitSize;
            if (setBit(index)) {
                bitsChanged = true;
            }
        }

        if (bitsChanged) {
            insertedCount.incrementAndGet();
        }
        return bitsChanged;
    }

    /**
     * Check if element might be in the filter
     * 检查元素可能存在于过滤器中
     *
     * @param element the element | 元素
     * @return true if element might exist, false if definitely not | 可能存在返回 true，一定不存在返回 false
     */
    public boolean mightContain(T element) {
        int hash1 = hash1(element);
        int hash2 = hash2(element);

        for (int i = 0; i < hashCount; i++) {
            int index = ((hash1 + i * hash2) % bitSize + bitSize) % bitSize;
            if (!getBit(index)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add all elements
     * 批量添加元素
     *
     * @param elements the elements | 元素集合
     */
    public void addAll(Collection<? extends T> elements) {
        for (T element : elements) {
            add(element);
        }
    }

    /**
     * Get expected false positive probability based on current insertions
     * 获取基于当前插入数的预期误判率
     *
     * @return expected FPP | 预期误判率
     */
    public double expectedFpp() {
        return Math.pow(1 - Math.exp(-hashCount * insertedCount.get() / (double) bitSize), hashCount);
    }

    /**
     * Get approximate element count
     * 获取近似元素数
     *
     * @return approximate count | 近似数量
     */
    public long approximateElementCount() {
        return insertedCount.get();
    }

    /**
     * Merge another bloom filter into this one
     * 合并另一个布隆过滤器
     *
     * @param other the other filter | 另一个过滤器
     * @throws IllegalArgumentException if filters are incompatible | 过滤器不兼容时抛出异常
     */
    public void merge(BloomFilter<T> other) {
        if (this.bitSize != other.bitSize || this.hashCount != other.hashCount) {
            throw new IllegalArgumentException("Bloom filters must have same size and hash count");
        }
        int length = bits.length();
        for (int i = 0; i < length; i++) {
            long otherValue = other.bits.get(i);
            long oldValue, newValue;
            do {
                oldValue = bits.get(i);
                newValue = oldValue | otherValue;
            } while (!bits.compareAndSet(i, oldValue, newValue));
        }
        this.insertedCount.addAndGet(other.insertedCount.get());
    }

    /**
     * Clear the filter
     * 清空过滤器
     */
    public void clear() {
        int length = bits.length();
        for (int i = 0; i < length; i++) {
            bits.set(i, 0);
        }
        insertedCount.set(0);
    }

    /**
     * Get bit size
     * 获取位大小
     *
     * @return bit size | 位大小
     */
    public int bitSize() {
        return bitSize;
    }

    /**
     * Get hash count
     * 获取哈希次数
     *
     * @return hash count | 哈希次数
     */
    public int hashCount() {
        return hashCount;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Atomically set a bit using CAS
     * 使用CAS原子设置位
     */
    private boolean setBit(int index) {
        int longIndex = index >>> 6;
        long mask = 1L << index;
        long oldValue, newValue;
        do {
            oldValue = bits.get(longIndex);
            if ((oldValue & mask) != 0) {
                return false; // Already set
            }
            newValue = oldValue | mask;
        } while (!bits.compareAndSet(longIndex, oldValue, newValue));
        return true;
    }

    /**
     * Atomically read a bit
     * 原子读取位
     */
    private boolean getBit(int index) {
        int longIndex = index >>> 6;
        long mask = 1L << index;
        return (bits.get(longIndex) & mask) != 0;
    }

    private int hash1(T element) {
        int h = element.hashCode();
        h ^= (h >>> 16);
        return h;
    }

    private int hash2(T element) {
        int h = element.hashCode();
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h;
    }

    private static int optimalBitSize(long n, double fpp) {
        long bits = (long) Math.ceil(-n * Math.log(fpp) / (Math.log(2) * Math.log(2)));
        if (bits > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Required bit size " + bits + " exceeds Integer.MAX_VALUE for expectedInsertions=" + n + " fpp=" + fpp);
        }
        return (int) bits;
    }

    private static int optimalHashCount(long n, int m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    /**
     * Create bloom filter for expected insertions with 1% FPP
     * 创建预期插入数的布隆过滤器（1% 误判率）
     *
     * @param expectedInsertions expected insertions | 预期插入数
     * @param <T>                element type | 元素类型
     * @return bloom filter | 布隆过滤器
     */
    public static <T> BloomFilter<T> create(long expectedInsertions) {
        return new BloomFilter<>(expectedInsertions, 0.01);
    }

    /**
     * Create bloom filter with custom FPP
     * 创建自定义误判率的布隆过滤器
     *
     * @param expectedInsertions expected insertions | 预期插入数
     * @param fpp                false positive probability | 误判率
     * @param <T>                element type | 元素类型
     * @return bloom filter | 布隆过滤器
     */
    public static <T> BloomFilter<T> create(long expectedInsertions, double fpp) {
        return new BloomFilter<>(expectedInsertions, fpp);
    }
}
