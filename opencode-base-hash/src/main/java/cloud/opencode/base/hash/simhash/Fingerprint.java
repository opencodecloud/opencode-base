package cloud.opencode.base.hash.simhash;

/**
 * Text fingerprint representation for SimHash
 * SimHash 的文本指纹表示
 *
 * <p>Represents a SimHash fingerprint value with utility methods for
 * comparing similarity between fingerprints.</p>
 * <p>表示 SimHash 指纹值，提供比较指纹间相似度的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hamming distance calculation - 海明距离计算</li>
 *   <li>Similarity score computation - 相似度分数计算</li>
 *   <li>Hex and binary representation - 十六进制和二进制表示</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Fingerprint fp1 = Fingerprint.of64(simHash.hash(text1));
 * Fingerprint fp2 = Fingerprint.of64(simHash.hash(text2));
 *
 * int distance = fp1.hammingDistance(fp2);
 * double similarity = fp1.similarity(fp2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class Fingerprint {

    /**
     * Fingerprint value
     */
    private final long value;

    /**
     * Number of bits
     */
    private final int bits;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates a fingerprint
     * 创建指纹
     *
     * @param value fingerprint value | 指纹值
     * @param bits  number of bits | 位数
     */
    public Fingerprint(long value, int bits) {
        if (bits <= 0 || bits > 64) {
            throw new IllegalArgumentException("Bits must be between 1 and 64");
        }
        this.value = value;
        this.bits = bits;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the fingerprint value
     * 获取指纹值
     *
     * @return fingerprint value | 指纹值
     */
    public long value() {
        return value;
    }

    /**
     * Gets the number of bits
     * 获取位数
     *
     * @return number of bits | 位数
     */
    public int bits() {
        return bits;
    }

    // ==================== Comparison Methods | 比较方法 ====================

    /**
     * Calculates the Hamming distance to another fingerprint
     * 计算与另一个指纹的海明距离
     *
     * @param other other fingerprint | 另一个指纹
     * @return number of different bits | 不同的位数
     */
    public int hammingDistance(Fingerprint other) {
        return Long.bitCount(this.value ^ other.value);
    }

    /**
     * Calculates the similarity to another fingerprint
     * 计算与另一个指纹的相似度
     *
     * @param other other fingerprint | 另一个指纹
     * @return similarity (0.0 - 1.0) | 相似度（0.0 - 1.0）
     */
    public double similarity(Fingerprint other) {
        int distance = hammingDistance(other);
        return 1.0 - (double) distance / bits;
    }

    /**
     * Checks if similar within threshold
     * 检查是否在阈值内相似
     *
     * @param other     other fingerprint | 另一个指纹
     * @param threshold Hamming distance threshold | 海明距离阈值
     * @return true if similar | 如果相似返回true
     */
    public boolean isSimilar(Fingerprint other, int threshold) {
        return hammingDistance(other) <= threshold;
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Converts to hexadecimal string
     * 转换为十六进制字符串
     *
     * @return hexadecimal string | 十六进制字符串
     */
    public String toHex() {
        int hexLength = (bits + 3) / 4;
        return String.format("%0" + hexLength + "x", value);
    }

    /**
     * Converts to binary string
     * 转换为二进制字符串
     *
     * @return binary string | 二进制字符串
     */
    public String toBinary() {
        String binary = Long.toBinaryString(value);
        if (binary.length() < bits) {
            binary = "0".repeat(bits - binary.length()) + binary;
        }
        return binary;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a 64-bit fingerprint
     * 创建64位指纹
     *
     * @param value fingerprint value | 指纹值
     * @return fingerprint | 指纹
     */
    public static Fingerprint of64(long value) {
        return new Fingerprint(value, 64);
    }

    /**
     * Creates a 32-bit fingerprint
     * 创建32位指纹
     *
     * @param value fingerprint value | 指纹值
     * @return fingerprint | 指纹
     */
    public static Fingerprint of32(int value) {
        return new Fingerprint(value & 0xFFFFFFFFL, 32);
    }

    /**
     * Creates a fingerprint with specified bits
     * 创建指定位数的指纹
     *
     * @param value fingerprint value | 指纹值
     * @param bits  number of bits | 位数
     * @return fingerprint | 指纹
     */
    public static Fingerprint of(long value, int bits) {
        return new Fingerprint(value, bits);
    }

    /**
     * Creates a fingerprint from hexadecimal string
     * 从十六进制字符串创建指纹
     *
     * @param hex  hexadecimal string | 十六进制字符串
     * @param bits number of bits | 位数
     * @return fingerprint | 指纹
     */
    public static Fingerprint fromHex(String hex, int bits) {
        return new Fingerprint(Long.parseUnsignedLong(hex, 16), bits);
    }

    // ==================== Object Methods | 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fingerprint that)) return false;
        return value == that.value && bits == that.bits;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value) * 31 + bits;
    }

    @Override
    public String toString() {
        return "Fingerprint{" +
                "value=" + toHex() +
                ", bits=" + bits +
                '}';
    }
}
