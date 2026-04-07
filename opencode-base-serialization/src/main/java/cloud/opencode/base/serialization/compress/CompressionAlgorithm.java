
package cloud.opencode.base.serialization.compress;

import java.util.Arrays;

/**
 * CompressionAlgorithm - Compression Algorithm Enumeration
 * 压缩算法枚举
 *
 * <p>Defines the available compression algorithms for serialized data.</p>
 * <p>定义序列化数据可用的压缩算法。</p>
 *
 * <p><strong>Available Algorithms | 可用算法:</strong></p>
 * <ul>
 *   <li>NONE - No compression - 无压缩</li>
 *   <li>GZIP - GZIP compression (JDK built-in) - GZIP 压缩（JDK 内置）</li>
 *   <li>DEFLATE - Deflate compression (JDK built-in) - Deflate 压缩（JDK 内置）</li>
 * </ul>
 *
 * <p><strong>Selection Guide | 选择指南:</strong></p>
 * <ul>
 *   <li>GZIP - Best compression ratio, moderate speed - 最佳压缩比，中等速度</li>
 *   <li>DEFLATE - Slightly faster than GZIP, slightly less overhead - 比 GZIP 略快，开销略小</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Supported compression algorithm enumeration - 支持的压缩算法枚举</li>
 *   <li>GZIP, Deflate support (JDK built-in) - 支持 GZIP、Deflate（JDK 内置）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Select compression for serialization
 * // 为序列化选择压缩算法
 * CompressedSerializer serializer = new CompressedSerializer(
 *     delegate, CompressionAlgorithm.GZIP
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public enum CompressionAlgorithm {

    /**
     * No compression
     * 无压缩
     */
    NONE("none", (byte) 0, false),

    /**
     * GZIP compression (JDK built-in)
     * GZIP 压缩（JDK 内置）
     */
    GZIP("gzip", (byte) 1, true),

    /**
     * Deflate compression (JDK built-in)
     * Deflate 压缩（JDK 内置）
     */
    DEFLATE("deflate", (byte) 5, true);

    /**
     * The algorithm name
     * 算法名称
     */
    private final String name;

    /**
     * The algorithm identifier for header
     * 用于头部的算法标识符
     */
    private final byte id;

    /**
     * Whether this algorithm is built-in to JDK
     * 此算法是否为 JDK 内置
     */
    private final boolean builtIn;

    CompressionAlgorithm(String name, byte id, boolean builtIn) {
        this.name = name;
        this.id = id;
        this.builtIn = builtIn;
    }

    /**
     * Returns the algorithm name.
     * 返回算法名称。
     *
     * @return the name - 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the algorithm identifier.
     * 返回算法标识符。
     *
     * @return the identifier - 标识符
     */
    public byte getId() {
        return id;
    }

    /**
     * Returns whether this algorithm is built-in to JDK.
     * 返回此算法是否为 JDK 内置。
     *
     * @return true if built-in - 如果是内置的则返回 true
     */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /**
     * Checks if this algorithm is available on the current classpath.
     * 检查此算法在当前类路径上是否可用。
     *
     * @return true if available - 如果可用则返回 true
     */
    public boolean isAvailable() {
        // All algorithms are JDK built-in or NONE, so always available
        return true;
    }

    /**
     * Pre-built lookup table: index = algorithm id byte, value = algorithm.
     * O(1) lookup instead of O(n) linear scan on every decompression call.
     * 预构建查找表：索引 = 算法 id 字节，值 = 算法。每次解压调用 O(1) 查找代替 O(n) 线性扫描。
     */
    private static final CompressionAlgorithm[] BY_ID;

    static {
        int maxId = 0;
        for (CompressionAlgorithm alg : values()) {
            maxId = Math.max(maxId, alg.id & 0xFF);
        }
        BY_ID = new CompressionAlgorithm[maxId + 1];
        Arrays.fill(BY_ID, NONE);
        for (CompressionAlgorithm alg : values()) {
            BY_ID[alg.id & 0xFF] = alg;
        }
    }

    /**
     * Returns the algorithm from its identifier.
     * 从标识符返回算法。
     *
     * @param id the identifier - 标识符
     * @return the algorithm, or NONE if not found - 算法，如果未找到则返回 NONE
     */
    public static CompressionAlgorithm fromId(byte id) {
        int idx = id & 0xFF;
        return idx < BY_ID.length ? BY_ID[idx] : NONE;
    }

    /**
     * Returns the algorithm from its name.
     * 从名称返回算法。
     *
     * @param name the name (case-insensitive) - 名称（不区分大小写）
     * @return the algorithm, or NONE if not found - 算法，如果未找到则返回 NONE
     */
    public static CompressionAlgorithm fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NONE;
        }
        String lowerName = name.toLowerCase();
        for (CompressionAlgorithm alg : values()) {
            if (alg.name.equals(lowerName)) {
                return alg;
            }
        }
        return NONE;
    }
}
