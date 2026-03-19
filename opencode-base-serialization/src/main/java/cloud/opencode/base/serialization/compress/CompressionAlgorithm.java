
package cloud.opencode.base.serialization.compress;

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
 *   <li>LZ4 - LZ4 fast compression - LZ4 快速压缩</li>
 *   <li>SNAPPY - Snappy compression - Snappy 压缩</li>
 *   <li>ZSTD - Zstandard high-ratio compression - Zstandard 高压缩比压缩</li>
 * </ul>
 *
 * <p><strong>Selection Guide | 选择指南:</strong></p>
 * <ul>
 *   <li>GZIP - Best compression ratio, moderate speed - 最佳压缩比，中等速度</li>
 *   <li>LZ4 - Fastest speed, moderate compression - 最快速度，中等压缩比</li>
 *   <li>SNAPPY - Good balance for real-time - 实时场景的良好平衡</li>
 *   <li>ZSTD - Best overall (ratio + speed) - 最佳综合表现</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Supported compression algorithm enumeration - 支持的压缩算法枚举</li>
 *   <li>GZIP, LZ4, Snappy, Deflate, ZSTD support - 支持GZIP、LZ4、Snappy、Deflate、ZSTD</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Select compression for serialization
 * // 为序列化选择压缩算法
 * CompressedSerializer serializer = new CompressedSerializer(
 *     delegate, CompressionAlgorithm.LZ4
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
     * LZ4 fast compression (requires lz4-java)
     * LZ4 快速压缩（需要 lz4-java）
     */
    LZ4("lz4", (byte) 2, false),

    /**
     * Snappy compression (requires snappy-java)
     * Snappy 压缩（需要 snappy-java）
     */
    SNAPPY("snappy", (byte) 3, false),

    /**
     * Zstandard high-ratio compression (requires zstd-jni)
     * Zstandard 高压缩比压缩（需要 zstd-jni）
     */
    ZSTD("zstd", (byte) 4, false);

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
        if (builtIn || this == NONE) {
            return true;
        }
        return switch (this) {
            case LZ4 -> isClassAvailable("net.jpountz.lz4.LZ4Factory");
            case SNAPPY -> isClassAvailable("org.xerial.snappy.Snappy");
            case ZSTD -> isClassAvailable("com.github.luben.zstd.Zstd");
            default -> false;
        };
    }

    /**
     * Returns the algorithm from its identifier.
     * 从标识符返回算法。
     *
     * @param id the identifier - 标识符
     * @return the algorithm, or NONE if not found - 算法，如果未找到则返回 NONE
     */
    public static CompressionAlgorithm fromId(byte id) {
        for (CompressionAlgorithm alg : values()) {
            if (alg.id == id) {
                return alg;
            }
        }
        return NONE;
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

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
