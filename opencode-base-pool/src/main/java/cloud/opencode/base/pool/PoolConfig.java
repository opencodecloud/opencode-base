package cloud.opencode.base.pool;

import cloud.opencode.base.pool.policy.EvictionPolicy;
import cloud.opencode.base.pool.policy.WaitPolicy;

import java.time.Duration;

/**
 * PoolConfig - Pool Configuration Record (JDK 25 Record)
 * PoolConfig - 池配置记录 (JDK 25 Record)
 *
 * <p>Immutable configuration for object pools. Uses Record for type-safe,
 * immutable configuration with builder pattern support.</p>
 * <p>对象池的不可变配置。使用Record实现类型安全、不可变配置，支持构建器模式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pool size configuration - 池大小配置</li>
 *   <li>Timeout configuration - 超时配置</li>
 *   <li>Eviction configuration - 驱逐配置</li>
 *   <li>Validation configuration - 验证配置</li>
 *   <li>Builder pattern support - 构建器模式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PoolConfig config = PoolConfig.builder()
 *     .maxTotal(20)
 *     .maxIdle(10)
 *     .minIdle(5)
 *     .maxWait(Duration.ofSeconds(10))
 *     .testOnBorrow(true)
 *     .build();
 *
 * PoolConfig defaults = PoolConfig.defaults();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param maxTotal                  maximum total objects - 最大对象总数
 * @param maxIdle                   maximum idle objects - 最大空闲对象数
 * @param minIdle                   minimum idle objects - 最小空闲对象数
 * @param maxWait                   maximum wait time for borrow - 借用的最大等待时间
 * @param minEvictableIdleTime      minimum idle time before eviction - 驱逐前的最小空闲时间
 * @param timeBetweenEvictionRuns   time between eviction runs - 驱逐运行之间的时间
 * @param numTestsPerEvictionRun    number of objects to test per eviction run - 每次驱逐运行测试的对象数
 * @param testOnBorrow              validate on borrow - 借用时验证
 * @param testOnReturn              validate on return - 归还时验证
 * @param testOnCreate              validate on create - 创建时验证
 * @param testWhileIdle             validate while idle - 空闲时验证
 * @param waitPolicy                wait policy when exhausted - 耗尽时的等待策略
 * @param lifo                      last-in-first-out ordering - 后进先出顺序
 * @param evictionPolicy            eviction policy - 驱逐策略
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public record PoolConfig(
        int maxTotal,
        int maxIdle,
        int minIdle,
        Duration maxWait,
        Duration minEvictableIdleTime,
        Duration timeBetweenEvictionRuns,
        int numTestsPerEvictionRun,
        boolean testOnBorrow,
        boolean testOnReturn,
        boolean testOnCreate,
        boolean testWhileIdle,
        WaitPolicy waitPolicy,
        boolean lifo,
        EvictionPolicy<?> evictionPolicy
) {

    /**
     * Creates a builder for PoolConfig.
     * 创建PoolConfig构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates default configuration.
     * 创建默认配置。
     *
     * @return the default config - 默认配置
     */
    public static PoolConfig defaults() {
        return builder().build();
    }

    /**
     * Checks if eviction is enabled.
     * 检查是否启用驱逐。
     *
     * @return true if eviction is enabled - 如果启用驱逐返回true
     */
    public boolean isEvictionEnabled() {
        return !timeBetweenEvictionRuns.isZero() && !timeBetweenEvictionRuns.isNegative();
    }

    /**
     * Checks if blocking wait is enabled.
     * 检查是否启用阻塞等待。
     *
     * @return true if blocking - 如果阻塞返回true
     */
    public boolean blockWhenExhausted() {
        return waitPolicy == WaitPolicy.BLOCK;
    }

    /**
     * Builder for PoolConfig.
     * PoolConfig构建器。
     */
    public static class Builder {
        private int maxTotal = 8;
        private int maxIdle = 8;
        private int minIdle = 0;
        private Duration maxWait = Duration.ofSeconds(30);
        private Duration minEvictableIdleTime = Duration.ofMinutes(30);
        private Duration timeBetweenEvictionRuns = Duration.ZERO;
        private int numTestsPerEvictionRun = 3;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean testOnCreate = false;
        private boolean testWhileIdle = false;
        private WaitPolicy waitPolicy = WaitPolicy.BLOCK;
        private boolean lifo = true;
        private EvictionPolicy<?> evictionPolicy = new EvictionPolicy.IdleTime<>(Duration.ofMinutes(30));

        /**
         * Sets the maximum total objects.
         * 设置最大对象总数。
         *
         * @param maxTotal the max total - 最大总数
         * @return this builder - 此构建器
         */
        public Builder maxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        /**
         * Sets the maximum idle objects.
         * 设置最大空闲对象数。
         *
         * @param maxIdle the max idle - 最大空闲数
         * @return this builder - 此构建器
         */
        public Builder maxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        /**
         * Sets the minimum idle objects.
         * 设置最小空闲对象数。
         *
         * @param minIdle the min idle - 最小空闲数
         * @return this builder - 此构建器
         */
        public Builder minIdle(int minIdle) {
            this.minIdle = minIdle;
            return this;
        }

        /**
         * Sets the maximum wait time.
         * 设置最大等待时间。
         *
         * @param maxWait the max wait - 最大等待时间
         * @return this builder - 此构建器
         */
        public Builder maxWait(Duration maxWait) {
            this.maxWait = maxWait;
            return this;
        }

        /**
         * Sets the minimum evictable idle time.
         * 设置最小可驱逐空闲时间。
         *
         * @param duration the duration - 时长
         * @return this builder - 此构建器
         */
        public Builder minEvictableIdleTime(Duration duration) {
            this.minEvictableIdleTime = duration;
            return this;
        }

        /**
         * Sets the time between eviction runs.
         * 设置驱逐运行之间的时间。
         *
         * @param duration the duration - 时长
         * @return this builder - 此构建器
         */
        public Builder timeBetweenEvictionRuns(Duration duration) {
            this.timeBetweenEvictionRuns = duration;
            return this;
        }

        /**
         * Sets the number of tests per eviction run.
         * 设置每次驱逐运行的测试数。
         *
         * @param numTests the number of tests - 测试数
         * @return this builder - 此构建器
         */
        public Builder numTestsPerEvictionRun(int numTests) {
            this.numTestsPerEvictionRun = numTests;
            return this;
        }

        /**
         * Sets whether to test on borrow.
         * 设置是否在借用时测试。
         *
         * @param test true to test - 是否测试
         * @return this builder - 此构建器
         */
        public Builder testOnBorrow(boolean test) {
            this.testOnBorrow = test;
            return this;
        }

        /**
         * Sets whether to test on return.
         * 设置是否在归还时测试。
         *
         * @param test true to test - 是否测试
         * @return this builder - 此构建器
         */
        public Builder testOnReturn(boolean test) {
            this.testOnReturn = test;
            return this;
        }

        /**
         * Sets whether to test on create.
         * 设置是否在创建时测试。
         *
         * @param test true to test - 是否测试
         * @return this builder - 此构建器
         */
        public Builder testOnCreate(boolean test) {
            this.testOnCreate = test;
            return this;
        }

        /**
         * Sets whether to test while idle.
         * 设置是否在空闲时测试。
         *
         * @param test true to test - 是否测试
         * @return this builder - 此构建器
         */
        public Builder testWhileIdle(boolean test) {
            this.testWhileIdle = test;
            return this;
        }

        /**
         * Sets the wait policy.
         * 设置等待策略。
         *
         * @param policy the policy - 策略
         * @return this builder - 此构建器
         */
        public Builder waitPolicy(WaitPolicy policy) {
            this.waitPolicy = policy;
            return this;
        }

        /**
         * Sets LIFO ordering.
         * 设置LIFO顺序。
         *
         * @param lifo true for LIFO - 是否LIFO
         * @return this builder - 此构建器
         */
        public Builder lifo(boolean lifo) {
            this.lifo = lifo;
            return this;
        }

        /**
         * Sets the eviction policy.
         * 设置驱逐策略。
         *
         * @param policy the policy - 策略
         * @return this builder - 此构建器
         */
        public Builder evictionPolicy(EvictionPolicy<?> policy) {
            this.evictionPolicy = policy;
            return this;
        }

        /**
         * Builds the configuration.
         * 构建配置。
         *
         * @return the config - 配置
         */
        public PoolConfig build() {
            return new PoolConfig(
                    maxTotal, maxIdle, minIdle, maxWait,
                    minEvictableIdleTime, timeBetweenEvictionRuns,
                    numTestsPerEvictionRun, testOnBorrow, testOnReturn,
                    testOnCreate, testWhileIdle, waitPolicy,
                    lifo, evictionPolicy
            );
        }
    }
}
