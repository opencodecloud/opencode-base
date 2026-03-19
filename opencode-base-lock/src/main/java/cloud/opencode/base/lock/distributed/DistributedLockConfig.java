package cloud.opencode.base.lock.distributed;

import java.time.Duration;

/**
 * Distributed Lock Configuration Record
 * 分布式锁配置记录
 *
 * <p>Immutable configuration for distributed lock behavior including timeout,
 * lease time, auto-renewal, and fencing token support.</p>
 * <p>分布式锁行为的不可变配置，包括超时、租约时间、自动续期和防护令牌支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock acquisition timeout - 锁获取超时</li>
 *   <li>Lease time (TTL) configuration - 租约时间（TTL）配置</li>
 *   <li>Auto-renewal with configurable interval - 可配置间隔的自动续期</li>
 *   <li>Retry mechanism - 重试机制</li>
 *   <li>Fencing token support - 防护令牌支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create custom configuration | 创建自定义配置
 * DistributedLockConfig config = DistributedLockConfig.builder()
 *     .lockTimeout(Duration.ofSeconds(30))
 *     .leaseTime(Duration.ofSeconds(30))
 *     .autoRenew(true)
 *     .renewInterval(Duration.ofSeconds(10))
 *     .retryCount(3)
 *     .enableFencing(true)
 *     .build();
 *
 * // Use default configuration | 使用默认配置
 * DistributedLockConfig defaultConfig = DistributedLockConfig.defaults();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param lockTimeout   timeout for acquiring the lock | 获取锁的超时
 * @param leaseTime     maximum time to hold the lock (TTL) | 持有锁的最大时间（TTL）
 * @param renewInterval interval for auto-renewal | 自动续期间隔
 * @param autoRenew     whether to enable auto-renewal | 是否启用自动续期
 * @param retryCount    number of retries on failure | 失败重试次数
 * @param retryInterval interval between retries | 重试间隔
 * @param enableFencing whether to enable fencing token | 是否启用防护令牌
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see DistributedLock
 * @see DistributedLockProvider
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public record DistributedLockConfig(
        Duration lockTimeout,
        Duration leaseTime,
        Duration renewInterval,
        boolean autoRenew,
        int retryCount,
        Duration retryInterval,
        boolean enableFencing
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
     * Gets the default configuration with reasonable defaults
     * 获取具有合理默认值的默认配置
     *
     * @return default configuration | 默认配置
     */
    public static DistributedLockConfig defaults() {
        return builder().build();
    }

    /**
     * Distributed Lock Configuration Builder
     * 分布式锁配置构建器
     *
     * <p>Provides fluent API for building distributed lock configurations.</p>
     * <p>提供用于构建分布式锁配置的流式API。</p>
     */
    public static class Builder {
        private Duration lockTimeout = Duration.ofSeconds(30);
        private Duration leaseTime = Duration.ofSeconds(30);
        private Duration renewInterval = Duration.ofSeconds(10);
        private boolean autoRenew = true;
        private int retryCount = 3;
        private Duration retryInterval = Duration.ofMillis(100);
        private boolean enableFencing = false;

        /**
         * Sets the timeout for lock acquisition
         * 设置锁获取超时
         *
         * @param timeout the timeout duration | 超时时长
         * @return this builder | 此构建器
         */
        public Builder lockTimeout(Duration timeout) {
            this.lockTimeout = timeout;
            return this;
        }

        /**
         * Sets the lease time (TTL) for the lock
         * 设置锁的租约时间（TTL）
         *
         * @param duration the lease time duration | 租约时长
         * @return this builder | 此构建器
         */
        public Builder leaseTime(Duration duration) {
            this.leaseTime = duration;
            return this;
        }

        /**
         * Sets the interval for automatic lock renewal
         * 设置自动锁续期的间隔
         *
         * @param interval the renewal interval | 续期间隔
         * @return this builder | 此构建器
         */
        public Builder renewInterval(Duration interval) {
            this.renewInterval = interval;
            return this;
        }

        /**
         * Sets whether to enable automatic lock renewal
         * 设置是否启用自动锁续期
         *
         * @param autoRenew true to enable auto-renewal | true表示启用自动续期
         * @return this builder | 此构建器
         */
        public Builder autoRenew(boolean autoRenew) {
            this.autoRenew = autoRenew;
            return this;
        }

        /**
         * Sets the number of retry attempts on failure
         * 设置失败时的重试次数
         *
         * @param count the retry count | 重试次数
         * @return this builder | 此构建器
         */
        public Builder retryCount(int count) {
            this.retryCount = count;
            return this;
        }

        /**
         * Sets the interval between retry attempts
         * 设置重试尝试之间的间隔
         *
         * @param interval the retry interval | 重试间隔
         * @return this builder | 此构建器
         */
        public Builder retryInterval(Duration interval) {
            this.retryInterval = interval;
            return this;
        }

        /**
         * Sets whether to enable fencing token for safe resource access
         * 设置是否启用防护令牌以安全访问资源
         *
         * @param enable true to enable fencing | true表示启用防护
         * @return this builder | 此构建器
         */
        public Builder enableFencing(boolean enable) {
            this.enableFencing = enable;
            return this;
        }

        /**
         * Builds the distributed lock configuration
         * 构建分布式锁配置
         *
         * @return the configuration | 配置
         */
        public DistributedLockConfig build() {
            return new DistributedLockConfig(
                    lockTimeout, leaseTime, renewInterval,
                    autoRenew, retryCount, retryInterval, enableFencing
            );
        }
    }
}
