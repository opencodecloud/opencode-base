package cloud.opencode.base.lock;

import java.time.Duration;

/**
 * Lock Configuration Record - Immutable Lock Settings
 * 锁配置记录 - 不可变锁设置
 *
 * <p>Provides immutable configuration for lock behavior with builder pattern support.</p>
 * <p>提供带有构建器模式支持的不可变锁行为配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default timeout configuration - 默认超时配置</li>
 *   <li>Fair/unfair lock selection - 公平/非公平锁选择</li>
 *   <li>Reentrant option - 可重入选项</li>
 *   <li>Spin count for spin locks - 自旋锁的自旋次数</li>
 *   <li>Metrics collection toggle - 指标收集开关</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create custom configuration | 创建自定义配置
 * LockConfig config = LockConfig.builder()
 *     .timeout(Duration.ofSeconds(10))
 *     .fair(true)
 *     .enableMetrics(true)
 *     .build();
 *
 * // Use with lock | 与锁一起使用
 * Lock<Long> lock = OpenLock.lock(config);
 *
 * // Use defaults | 使用默认值
 * LockConfig defaultConfig = LockConfig.defaults();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param defaultTimeout default timeout for lock acquisition | 锁获取的默认超时
 * @param fair           whether the lock should be fair | 锁是否应该是公平的
 * @param reentrant      whether the lock should be reentrant | 锁是否应该是可重入的
 * @param spinCount      maximum spin count for spin locks | 自旋锁的最大自旋次数
 * @param enableMetrics  whether to enable metrics collection | 是否启用指标收集
 * @param lockType       the type of lock | 锁类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see LockType
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public record LockConfig(
        Duration defaultTimeout,
        boolean fair,
        boolean reentrant,
        int spinCount,
        boolean enableMetrics,
        LockType lockType
) {

    /**
     * Creates a new configuration builder
     * 创建新的配置构建器
     *
     * @return a new builder instance | 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets default configuration with reasonable defaults
     * 获取具有合理默认值的默认配置
     *
     * <p>Default values: timeout=30s, fair=false, reentrant=true,
     * spinCount=1000, metrics=true, type=REENTRANT</p>
     * <p>默认值：超时=30秒，公平=false，可重入=true，
     * 自旋次数=1000，指标=true，类型=REENTRANT</p>
     *
     * @return default configuration | 默认配置
     */
    public static LockConfig defaults() {
        return builder().build();
    }

    /**
     * Lock Configuration Builder
     * 锁配置构建器
     *
     * <p>Provides fluent API for building lock configurations.</p>
     * <p>提供用于构建锁配置的流式API。</p>
     */
    public static class Builder {
        private Duration defaultTimeout = Duration.ofSeconds(30);
        private boolean fair = false;
        private boolean reentrant = true;
        private int spinCount = 1000;
        private boolean enableMetrics = true;
        private LockType lockType = LockType.REENTRANT;

        /**
         * Sets the default timeout for lock acquisition
         * 设置锁获取的默认超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.defaultTimeout = timeout;
            return this;
        }

        /**
         * Sets whether the lock should be fair
         * 设置锁是否应该是公平的
         *
         * <p>Fair locks grant access in FIFO order but may have lower throughput.</p>
         * <p>公平锁按FIFO顺序授予访问权限，但可能具有较低的吞吐量。</p>
         *
         * @param fair true for fair lock | true表示公平锁
         * @return this builder | 此构建器
         */
        public Builder fair(boolean fair) {
            this.fair = fair;
            return this;
        }

        /**
         * Sets whether the lock should be reentrant
         * 设置锁是否应该是可重入的
         *
         * @param reentrant true for reentrant lock | true表示可重入锁
         * @return this builder | 此构建器
         */
        public Builder reentrant(boolean reentrant) {
            this.reentrant = reentrant;
            return this;
        }

        /**
         * Sets the maximum spin count for spin locks
         * 设置自旋锁的最大自旋次数
         *
         * @param spinCount maximum spin count | 最大自旋次数
         * @return this builder | 此构建器
         */
        public Builder spinCount(int spinCount) {
            this.spinCount = spinCount;
            return this;
        }

        /**
         * Sets whether metrics collection is enabled
         * 设置是否启用指标收集
         *
         * @param enable true to enable metrics | true表示启用指标
         * @return this builder | 此构建器
         */
        public Builder enableMetrics(boolean enable) {
            this.enableMetrics = enable;
            return this;
        }

        /**
         * Sets the lock type
         * 设置锁类型
         *
         * @param type the lock type | 锁类型
         * @return this builder | 此构建器
         */
        public Builder lockType(LockType type) {
            this.lockType = type;
            return this;
        }

        /**
         * Builds the lock configuration
         * 构建锁配置
         *
         * @return the lock configuration | 锁配置
         */
        public LockConfig build() {
            return new LockConfig(defaultTimeout, fair, reentrant,
                    spinCount, enableMetrics, lockType);
        }
    }
}
