package cloud.opencode.base.cache.compression;

/**
 * Compression Algorithm Enumeration
 * 压缩算法枚举
 *
 * <p>Defines available compression algorithms for cache value compression.</p>
 * <p>定义缓存值压缩的可用压缩算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>GZIP - Good compression ratio - GZIP - 良好压缩率</li>
 *   <li>LZ4 - Fast compression - LZ4 - 快速压缩</li>
 *   <li>ZSTD - Best balance of speed and ratio - ZSTD - 速度和压缩率的最佳平衡</li>
 *   <li>Snappy - Very fast, lower ratio - Snappy - 非常快，较低压缩率</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CompressionAlgorithm algo = CompressionAlgorithm.GZIP;
 * String name = algo.algorithmName();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k) for fromId/fromName where k is the number of enum constants (5) - 时间复杂度: fromId/fromName 为 O(k)，k为枚举常量数量（5）</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public enum CompressionAlgorithm {

    /**
     * No compression - 无压缩
     */
    NONE(0, "none", 0),

    /**
     * GZIP compression - Good compression ratio, moderate speed
     * GZIP 压缩 - 压缩率好，速度适中
     */
    GZIP(1, "gzip", 6),

    /**
     * LZ4 compression - Fast compression, moderate ratio
     * LZ4 压缩 - 压缩速度快，压缩率适中
     */
    LZ4(2, "lz4", 9),

    /**
     * ZSTD compression - Best balance of speed and ratio
     * ZSTD 压缩 - 速度和压缩率的最佳平衡
     */
    ZSTD(3, "zstd", 3),

    /**
     * Snappy compression - Very fast, lower ratio
     * Snappy 压缩 - 非常快，压缩率较低
     */
    SNAPPY(4, "snappy", 0);

    private final int id;
    private final String name;
    private final int defaultLevel;

    CompressionAlgorithm(int id, String name, int defaultLevel) {
        this.id = id;
        this.name = name;
        this.defaultLevel = defaultLevel;
    }

    /**
     * Get algorithm ID
     * 获取算法 ID
     *
     * @return algorithm ID | 算法 ID
     */
    public int id() {
        return id;
    }

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name | 算法名称
     */
    public String algorithmName() {
        return name;
    }

    /**
     * Get default compression level
     * 获取默认压缩级别
     *
     * @return default level | 默认级别
     */
    public int defaultLevel() {
        return defaultLevel;
    }

    /**
     * Get algorithm by ID
     * 根据 ID 获取算法
     *
     * @param id algorithm ID | 算法 ID
     * @return algorithm | 算法
     */
    public static CompressionAlgorithm fromId(int id) {
        for (CompressionAlgorithm algo : values()) {
            if (algo.id == id) {
                return algo;
            }
        }
        return NONE;
    }

    /**
     * Get algorithm by name
     * 根据名称获取算法
     *
     * @param name algorithm name | 算法名称
     * @return algorithm | 算法
     */
    public static CompressionAlgorithm fromName(String name) {
        for (CompressionAlgorithm algo : values()) {
            if (algo.name.equalsIgnoreCase(name)) {
                return algo;
            }
        }
        return NONE;
    }
}
