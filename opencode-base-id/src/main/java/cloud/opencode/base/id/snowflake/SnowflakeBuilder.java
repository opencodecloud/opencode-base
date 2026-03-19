package cloud.opencode.base.id.snowflake;

import java.time.Instant;

/**
 * Snowflake ID Generator Builder
 * 雪花ID生成器构建器
 *
 * <p>Fluent builder for creating customized SnowflakeGenerator instances.</p>
 * <p>用于创建自定义SnowflakeGenerator实例的流式构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Worker/Datacenter ID configuration - 工作节点/数据中心ID配置</li>
 *   <li>Custom epoch setting - 自定义起始时间设置</li>
 *   <li>Bit allocation customization - 位分配自定义</li>
 *   <li>Clock backward strategy selection - 时钟回拨策略选择</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeGenerator gen = new SnowflakeBuilder()
 *     .workerId(1)
 *     .datacenterId(1)
 *     .epochMillis(1609459200000L)
 *     .clockBackwardStrategy(Wait.ofSeconds(5))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (use before build) - 线程安全: 否（在build前使用）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for build() and all setter methods - 时间复杂度: build() 及所有 setter 方法均为 O(1)</li>
 *   <li>Space complexity: O(1) - stores only primitive configuration values - 空间复杂度: O(1) - 仅存储原始配置值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class SnowflakeBuilder {

    private long workerId = 0;
    private long datacenterId = 0;
    private long epoch = SnowflakeConfig.DEFAULT_EPOCH;
    private int timestampBits = SnowflakeConfig.DEFAULT_TIMESTAMP_BITS;
    private int datacenterBits = SnowflakeConfig.DEFAULT_DATACENTER_BITS;
    private int workerBits = SnowflakeConfig.DEFAULT_WORKER_BITS;
    private int sequenceBits = SnowflakeConfig.DEFAULT_SEQUENCE_BITS;
    private ClockBackwardStrategy clockBackwardStrategy;
    private WorkerIdAssigner workerIdAssigner;

    /**
     * Creates a new builder
     * 创建新的构建器
     */
    public SnowflakeBuilder() {
    }

    /**
     * Sets the worker ID
     * 设置工作节点ID
     *
     * @param workerId the worker ID (0-31 default) | 工作节点ID（默认0-31）
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder workerId(long workerId) {
        this.workerId = workerId;
        return this;
    }

    /**
     * Sets the datacenter ID
     * 设置数据中心ID
     *
     * @param datacenterId the datacenter ID (0-31 default) | 数据中心ID（默认0-31）
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder datacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
        return this;
    }

    /**
     * Sets the epoch from Instant
     * 使用Instant设置起始时间
     *
     * @param epoch the epoch instant | 起始时间
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder epoch(Instant epoch) {
        this.epoch = epoch.toEpochMilli();
        return this;
    }

    /**
     * Sets the epoch in milliseconds
     * 使用毫秒设置起始时间
     *
     * @param epochMillis the epoch in milliseconds | 起始时间（毫秒）
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder epochMillis(long epochMillis) {
        this.epoch = epochMillis;
        return this;
    }

    /**
     * Sets the clock backward strategy
     * 设置时钟回拨策略
     *
     * @param strategy the strategy | 策略
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder clockBackwardStrategy(ClockBackwardStrategy strategy) {
        this.clockBackwardStrategy = strategy;
        return this;
    }

    /**
     * Sets the timestamp bits
     * 设置时间戳位数
     *
     * @param bits the number of bits | 位数
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder timestampBits(int bits) {
        this.timestampBits = bits;
        return this;
    }

    /**
     * Sets the datacenter ID bits
     * 设置数据中心ID位数
     *
     * @param bits the number of bits | 位数
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder datacenterBits(int bits) {
        this.datacenterBits = bits;
        return this;
    }

    /**
     * Sets the worker ID bits
     * 设置工作节点ID位数
     *
     * @param bits the number of bits | 位数
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder workerBits(int bits) {
        this.workerBits = bits;
        return this;
    }

    /**
     * Sets the sequence bits
     * 设置序列号位数
     *
     * @param bits the number of bits | 位数
     * @return this builder | 此构建器
     */
    public SnowflakeBuilder sequenceBits(int bits) {
        this.sequenceBits = bits;
        return this;
    }

    /**
     * Sets the worker ID assigner for automatic ID assignment
     * 设置工作节点ID分配器以自动分配ID
     *
     * <p>When set, workerId and datacenterId will be automatically assigned
     * from this assigner, overriding any manually set values.</p>
     * <p>设置后，workerId和datacenterId将从此分配器自动分配，
     * 覆盖任何手动设置的值。</p>
     *
     * @param assigner the worker ID assigner | 工作节点ID分配器
     * @return this builder | 此构建器
     * @see IpBasedAssigner
     * @see MacBasedAssigner
     * @see RandomAssigner
     */
    public SnowflakeBuilder workerIdAssigner(WorkerIdAssigner assigner) {
        this.workerIdAssigner = assigner;
        return this;
    }

    /**
     * Builds the SnowflakeGenerator
     * 构建SnowflakeGenerator
     *
     * @return the generator | 生成器
     * @throws IllegalArgumentException if configuration is invalid | 如果配置无效
     */
    public SnowflakeGenerator build() {
        long finalWorkerId = workerId;
        long finalDatacenterId = datacenterId;

        // Use assigner if provided
        if (workerIdAssigner != null) {
            finalWorkerId = workerIdAssigner.assignWorkerId();
            finalDatacenterId = workerIdAssigner.assignDatacenterId();
        }

        SnowflakeConfig config = new SnowflakeConfig(
                finalWorkerId,
                finalDatacenterId,
                epoch,
                timestampBits,
                datacenterBits,
                workerBits,
                sequenceBits
        );
        return new SnowflakeGenerator(config, clockBackwardStrategy);
    }
}
