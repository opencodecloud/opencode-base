
package cloud.opencode.base.serialization;

import cloud.opencode.base.serialization.compress.CompressionAlgorithm;
import cloud.opencode.base.serialization.filter.ClassFilter;

import java.util.Objects;

/**
 * SerializerConfig - Serialization Configuration
 * 序列化配置
 *
 * <p>Configuration options for serialization behavior including compression settings,
 * type information inclusion, and other serialization options.</p>
 * <p>序列化行为的配置选项，包括压缩设置、类型信息包含和其他序列化选项。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compression control - 压缩控制</li>
 *   <li>Type information settings - 类型信息设置</li>
 *   <li>Immutable configuration - 不可变配置</li>
 *   <li>Builder pattern support - 支持构建器模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default configuration
 * SerializerConfig config = SerializerConfig.defaults();
 *
 * // Custom configuration
 * SerializerConfig config = SerializerConfig.builder()
 *     .enableCompression(true)
 *     .compressionAlgorithm(CompressionAlgorithm.GZIP)
 *     .compressionThreshold(1024)
 *     .includeTypeInfo(true)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per configuration access - 每次配置访问 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public final class SerializerConfig {

    /**
     * Default configuration instance
     * 默认配置实例
     */
    public static final SerializerConfig DEFAULT = new SerializerConfig(
            false,
            false,
            CompressionAlgorithm.GZIP,
            1024,
            false,
            null
    );

    /**
     * Whether to include type information for polymorphic types
     * 是否包含多态类型的类型信息
     */
    private final boolean includeTypeInfo;

    /**
     * Whether compression is enabled
     * 是否启用压缩
     */
    private final boolean compressionEnabled;

    /**
     * The compression algorithm to use
     * 使用的压缩算法
     */
    private final CompressionAlgorithm compressionAlgorithm;

    /**
     * The compression threshold in bytes
     * 压缩阈值（字节）
     */
    private final int compressionThreshold;

    /**
     * Whether to fail on unknown properties during deserialization
     * 反序列化时是否对未知属性失败
     */
    private final boolean failOnUnknownProperties;

    /**
     * The class filter for deserialization security, may be null (no filtering)
     * 反序列化安全的类过滤器，可能为 null（不过滤）
     *
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    private final ClassFilter classFilter;

    private SerializerConfig(
            boolean includeTypeInfo,
            boolean compressionEnabled,
            CompressionAlgorithm compressionAlgorithm,
            int compressionThreshold,
            boolean failOnUnknownProperties,
            ClassFilter classFilter
    ) {
        this.includeTypeInfo = includeTypeInfo;
        this.compressionEnabled = compressionEnabled;
        this.compressionAlgorithm = compressionAlgorithm;
        this.compressionThreshold = compressionThreshold;
        this.failOnUnknownProperties = failOnUnknownProperties;
        this.classFilter = classFilter;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Returns the default configuration.
     * 返回默认配置。
     *
     * @return the default configuration - 默认配置
     */
    public static SerializerConfig defaults() {
        return DEFAULT;
    }

    /**
     * Creates a new builder.
     * 创建新的构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder initialized with this configuration.
     * 创建用此配置初始化的构建器。
     *
     * @return the builder - 构建器
     */
    public Builder toBuilder() {
        return new Builder()
                .includeTypeInfo(this.includeTypeInfo)
                .enableCompression(this.compressionEnabled)
                .compressionAlgorithm(this.compressionAlgorithm)
                .compressionThreshold(this.compressionThreshold)
                .failOnUnknownProperties(this.failOnUnknownProperties)
                .classFilter(this.classFilter);
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Returns whether type information should be included.
     * 返回是否应包含类型信息。
     *
     * @return true if type info is included - 如果包含类型信息则返回 true
     */
    public boolean isIncludeTypeInfo() {
        return includeTypeInfo;
    }

    /**
     * Returns whether compression is enabled.
     * 返回是否启用压缩。
     *
     * @return true if compression is enabled - 如果启用压缩则返回 true
     */
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    /**
     * Returns the compression algorithm.
     * 返回压缩算法。
     *
     * @return the compression algorithm - 压缩算法
     */
    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /**
     * Returns the compression threshold in bytes.
     * 返回压缩阈值（字节）。
     *
     * <p>Data smaller than this threshold will not be compressed.</p>
     * <p>小于此阈值的数据不会被压缩。</p>
     *
     * @return the compression threshold - 压缩阈值
     */
    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    /**
     * Returns whether to fail on unknown properties.
     * 返回是否对未知属性失败。
     *
     * @return true if should fail on unknown properties - 如果应对未知属性失败则返回 true
     */
    public boolean isFailOnUnknownProperties() {
        return failOnUnknownProperties;
    }

    /**
     * Returns the class filter for deserialization security.
     * 返回反序列化安全的类过滤器。
     *
     * <p>When non-null, this filter should be consulted before deserializing
     * any class to prevent deserialization of dangerous types.</p>
     * <p>当非 null 时，应在反序列化任何类之前查询此过滤器，
     * 以防止危险类型的反序列化。</p>
     *
     * @return the class filter, or null if filtering is not enabled |
     *         类过滤器，如果未启用过滤则为 null
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    public ClassFilter getClassFilter() {
        return classFilter;
    }

    // ==================== Object Methods | 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializerConfig that)) return false;
        return includeTypeInfo == that.includeTypeInfo
                && compressionEnabled == that.compressionEnabled
                && compressionThreshold == that.compressionThreshold
                && failOnUnknownProperties == that.failOnUnknownProperties
                && compressionAlgorithm == that.compressionAlgorithm
                && Objects.equals(classFilter, that.classFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeTypeInfo, compressionEnabled, compressionAlgorithm,
                compressionThreshold, failOnUnknownProperties, classFilter);
    }

    @Override
    public String toString() {
        return "SerializerConfig{" +
                "includeTypeInfo=" + includeTypeInfo +
                ", compressionEnabled=" + compressionEnabled +
                ", compressionAlgorithm=" + compressionAlgorithm +
                ", compressionThreshold=" + compressionThreshold +
                ", failOnUnknownProperties=" + failOnUnknownProperties +
                ", classFilter=" + classFilter +
                '}';
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for SerializerConfig.
     * SerializerConfig 的构建器。
     */
    public static final class Builder {

        private boolean includeTypeInfo = false;
        private boolean compressionEnabled = false;
        private CompressionAlgorithm compressionAlgorithm = CompressionAlgorithm.GZIP;
        private int compressionThreshold = 1024;
        private boolean failOnUnknownProperties = false;
        private ClassFilter classFilter = null;

        private Builder() {}

        /**
         * Sets whether to include type information.
         * 设置是否包含类型信息。
         *
         * @param include true to include type info - 包含类型信息则为 true
         * @return this builder - 此构建器
         */
        public Builder includeTypeInfo(boolean include) {
            this.includeTypeInfo = include;
            return this;
        }

        /**
         * Sets whether compression is enabled.
         * 设置是否启用压缩。
         *
         * @param enable true to enable compression - 启用压缩则为 true
         * @return this builder - 此构建器
         */
        public Builder enableCompression(boolean enable) {
            this.compressionEnabled = enable;
            return this;
        }

        /**
         * Sets the compression algorithm.
         * 设置压缩算法。
         *
         * @param algorithm the compression algorithm - 压缩算法
         * @return this builder - 此构建器
         */
        public Builder compressionAlgorithm(CompressionAlgorithm algorithm) {
            this.compressionAlgorithm = Objects.requireNonNull(algorithm, "Algorithm must not be null");
            return this;
        }

        /**
         * Sets the compression threshold in bytes.
         * 设置压缩阈值（字节）。
         *
         * @param threshold the threshold in bytes - 阈值（字节）
         * @return this builder - 此构建器
         */
        public Builder compressionThreshold(int threshold) {
            if (threshold < 0) {
                throw new IllegalArgumentException("Threshold must be non-negative");
            }
            this.compressionThreshold = threshold;
            return this;
        }

        /**
         * Sets whether to fail on unknown properties.
         * 设置是否对未知属性失败。
         *
         * @param fail true to fail on unknown properties - 对未知属性失败则为 true
         * @return this builder - 此构建器
         */
        public Builder failOnUnknownProperties(boolean fail) {
            this.failOnUnknownProperties = fail;
            return this;
        }

        /**
         * Sets the class filter for deserialization security.
         * 设置反序列化安全的类过滤器。
         *
         * @param classFilter the class filter, may be null - 类过滤器，可以为 null
         * @return this builder - 此构建器
         * @since JDK 25, opencode-base-serialization V1.0.3
         */
        public Builder classFilter(ClassFilter classFilter) {
            this.classFilter = classFilter;
            return this;
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the configuration - 配置
         */
        public SerializerConfig build() {
            return new SerializerConfig(
                    includeTypeInfo,
                    compressionEnabled,
                    compressionAlgorithm,
                    compressionThreshold,
                    failOnUnknownProperties,
                    classFilter
            );
        }
    }
}
