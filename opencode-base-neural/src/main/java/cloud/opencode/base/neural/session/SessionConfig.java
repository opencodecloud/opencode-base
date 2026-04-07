package cloud.opencode.base.neural.session;

import cloud.opencode.base.neural.exception.NeuralErrorCode;
import cloud.opencode.base.neural.exception.NeuralException;

/**
 * Inference Session Configuration
 * 推理会话配置
 *
 * <p>Immutable configuration for {@link InferenceSession}, controlling thread pool size,
 * tensor pool capacity, profiling, and memory limits. Use {@link #defaults()} for
 * sensible defaults or {@link #builder()} for customization.</p>
 * <p>用于 {@link InferenceSession} 的不可变配置，控制线程池大小、
 * 张量池容量、性能分析和内存限制。使用 {@link #defaults()} 获取合理默认值，
 * 或使用 {@link #builder()} 进行自定义。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see InferenceSession
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class SessionConfig {

    private static final int DEFAULT_TENSOR_POOL_CAPACITY = 64;

    private final int threadPoolSize;
    private final int tensorPoolCapacity;
    private final boolean enableProfiling;
    private final long maxMemoryBytes;

    private SessionConfig(int threadPoolSize, int tensorPoolCapacity,
                          boolean enableProfiling, long maxMemoryBytes) {
        this.threadPoolSize = threadPoolSize;
        this.tensorPoolCapacity = tensorPoolCapacity;
        this.enableProfiling = enableProfiling;
        this.maxMemoryBytes = maxMemoryBytes;
    }

    /**
     * Create a default configuration
     * 创建默认配置
     *
     * <p>Defaults: threadPoolSize = available processors, tensorPoolCapacity = 64,
     * enableProfiling = false, maxMemoryBytes = 0 (unlimited).</p>
     * <p>默认值: 线程池大小 = 可用处理器数, 张量池容量 = 64,
     * 启用性能分析 = false, 最大内存字节数 = 0（无限制）。</p>
     *
     * @return default configuration | 默认配置
     */
    public static SessionConfig defaults() {
        return new SessionConfig(
                Runtime.getRuntime().availableProcessors(),
                DEFAULT_TENSOR_POOL_CAPACITY,
                false,
                0L
        );
    }

    /**
     * Create a new builder for custom configuration
     * 创建新的构建器用于自定义配置
     *
     * @return a new builder | 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the thread pool size
     * 获取线程池大小
     *
     * @return thread pool size | 线程池大小
     */
    public int threadPoolSize() {
        return threadPoolSize;
    }

    /**
     * Get the tensor pool capacity
     * 获取张量池容量
     *
     * @return tensor pool capacity | 张量池容量
     */
    public int tensorPoolCapacity() {
        return tensorPoolCapacity;
    }

    /**
     * Check if profiling is enabled
     * 检查是否启用性能分析
     *
     * @return true if profiling is enabled | 如果启用性能分析则返回 true
     */
    public boolean enableProfiling() {
        return enableProfiling;
    }

    /**
     * Get the maximum memory limit in bytes
     * 获取最大内存限制（字节）
     *
     * @return max memory bytes (0 means unlimited) | 最大内存字节数（0表示无限制）
     */
    public long maxMemoryBytes() {
        return maxMemoryBytes;
    }

    @Override
    public String toString() {
        return "SessionConfig{threadPoolSize=" + threadPoolSize
                + ", tensorPoolCapacity=" + tensorPoolCapacity
                + ", enableProfiling=" + enableProfiling
                + ", maxMemoryBytes=" + maxMemoryBytes + '}';
    }

    /**
     * Builder for SessionConfig
     * SessionConfig 构建器
     *
     * <p>Fluent builder that validates all parameters on {@link #build()}.</p>
     * <p>流式构建器，在 {@link #build()} 时验证所有参数。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @since JDK 25, opencode-base-neural V1.0.0
     */
    public static final class Builder {

        private int threadPoolSize = Runtime.getRuntime().availableProcessors();
        private int tensorPoolCapacity = DEFAULT_TENSOR_POOL_CAPACITY;
        private boolean enableProfiling = false;
        private long maxMemoryBytes = 0L;

        Builder() {
        }

        /**
         * Set the thread pool size
         * 设置线程池大小
         *
         * @param size thread pool size (must be &gt; 0) | 线程池大小（必须大于0）
         * @return this builder | 此构建器
         */
        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }

        /**
         * Set the tensor pool capacity
         * 设置张量池容量
         *
         * @param capacity tensor pool capacity (must be &gt; 0) | 张量池容量（必须大于0）
         * @return this builder | 此构建器
         */
        public Builder tensorPoolCapacity(int capacity) {
            this.tensorPoolCapacity = capacity;
            return this;
        }

        /**
         * Enable or disable profiling
         * 启用或禁用性能分析
         *
         * @param enable true to enable profiling | true 表示启用性能分析
         * @return this builder | 此构建器
         */
        public Builder enableProfiling(boolean enable) {
            this.enableProfiling = enable;
            return this;
        }

        /**
         * Set the maximum memory limit in bytes
         * 设置最大内存限制（字节）
         *
         * @param bytes max memory bytes (0 means unlimited, must be &ge; 0) |
         *              最大内存字节数（0表示无限制，必须大于等于0）
         * @return this builder | 此构建器
         */
        public Builder maxMemoryBytes(long bytes) {
            this.maxMemoryBytes = bytes;
            return this;
        }

        /**
         * Build the SessionConfig, validating all parameters
         * 构建 SessionConfig，验证所有参数
         *
         * @return the built configuration | 构建的配置
         * @throws NeuralException if any parameter is invalid | 如果任何参数无效
         */
        public SessionConfig build() {
            if (threadPoolSize <= 0) {
                throw new NeuralException(
                        "threadPoolSize must be > 0, got: " + threadPoolSize,
                        NeuralErrorCode.SESSION_CONFIG_INVALID);
            }
            if (tensorPoolCapacity <= 0) {
                throw new NeuralException(
                        "tensorPoolCapacity must be > 0, got: " + tensorPoolCapacity,
                        NeuralErrorCode.SESSION_CONFIG_INVALID);
            }
            if (maxMemoryBytes < 0) {
                throw new NeuralException(
                        "maxMemoryBytes must be >= 0, got: " + maxMemoryBytes,
                        NeuralErrorCode.SESSION_CONFIG_INVALID);
            }
            return new SessionConfig(threadPoolSize, tensorPoolCapacity,
                    enableProfiling, maxMemoryBytes);
        }
    }
}
