package cloud.opencode.base.id.snowflake;

import java.time.Instant;

/**
 * Snowflake ID Generator Configuration
 * 雪花ID生成器配置
 *
 * <p>Configuration record for Snowflake ID generator including bit allocations
 * and epoch settings.</p>
 * <p>雪花ID生成器的配置记录，包括位分配和起始时间设置。</p>
 *
 * <p><strong>Default Bit Allocation | 默认位分配:</strong></p>
 * <ul>
 *   <li>Sign bit: 1 - 符号位: 1</li>
 *   <li>Timestamp bits: 41 (69 years) - 时间戳位: 41 (约69年)</li>
 *   <li>Datacenter bits: 5 (32 datacenters) - 数据中心位: 5 (32个)</li>
 *   <li>Worker bits: 5 (32 workers) - 工作节点位: 5 (32个)</li>
 *   <li>Sequence bits: 12 (4096/ms) - 序列号位: 12 (每毫秒4096个)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeConfig config = SnowflakeConfig.defaultConfig();
 * SnowflakeConfig custom = new SnowflakeConfig(1, 1, 1609459200000L, 41, 5, 5, 12);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration record for Snowflake ID generator - Snowflake ID生成器的配置记录</li>
 *   <li>Configurable bit allocation for timestamp, worker, sequence - 可配置时间戳、工作节点、序列的位分配</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param workerId       the worker node ID | 工作节点ID
 * @param datacenterId   the datacenter ID | 数据中心ID
 * @param epoch          the epoch timestamp in milliseconds | 起始时间戳（毫秒）
 * @param timestampBits  the number of bits for timestamp | 时间戳位数
 * @param datacenterBits the number of bits for datacenter ID | 数据中心ID位数
 * @param workerBits     the number of bits for worker ID | 工作节点ID位数
 * @param sequenceBits   the number of bits for sequence | 序列号位数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public record SnowflakeConfig(
        long workerId,
        long datacenterId,
        long epoch,
        int timestampBits,
        int datacenterBits,
        int workerBits,
        int sequenceBits
) {

    /**
     * Default epoch: 2021-01-01 00:00:00 UTC
     * 默认起始时间: 2021-01-01 00:00:00 UTC
     */
    public static final long DEFAULT_EPOCH = 1609459200000L;

    /**
     * Default timestamp bits
     * 默认时间戳位数
     */
    public static final int DEFAULT_TIMESTAMP_BITS = 41;

    /**
     * Default datacenter bits
     * 默认数据中心位数
     */
    public static final int DEFAULT_DATACENTER_BITS = 5;

    /**
     * Default worker bits
     * 默认工作节点位数
     */
    public static final int DEFAULT_WORKER_BITS = 5;

    /**
     * Default sequence bits
     * 默认序列号位数
     */
    public static final int DEFAULT_SEQUENCE_BITS = 12;

    /**
     * Creates a default configuration
     * 创建默认配置
     *
     * @return default configuration | 默认配置
     */
    public static SnowflakeConfig defaultConfig() {
        return new SnowflakeConfig(0, 0, DEFAULT_EPOCH,
                DEFAULT_TIMESTAMP_BITS, DEFAULT_DATACENTER_BITS,
                DEFAULT_WORKER_BITS, DEFAULT_SEQUENCE_BITS);
    }

    /**
     * Creates a configuration with worker and datacenter IDs
     * 使用工作节点ID和数据中心ID创建配置
     *
     * @param workerId     the worker node ID | 工作节点ID
     * @param datacenterId the datacenter ID | 数据中心ID
     * @return configuration | 配置
     */
    public static SnowflakeConfig of(long workerId, long datacenterId) {
        return new SnowflakeConfig(workerId, datacenterId, DEFAULT_EPOCH,
                DEFAULT_TIMESTAMP_BITS, DEFAULT_DATACENTER_BITS,
                DEFAULT_WORKER_BITS, DEFAULT_SEQUENCE_BITS);
    }

    /**
     * Gets the maximum worker ID value
     * 获取最大工作节点ID值
     *
     * @return maximum worker ID | 最大工作节点ID
     */
    public long maxWorkerId() {
        return ~(-1L << workerBits);
    }

    /**
     * Gets the maximum datacenter ID value
     * 获取最大数据中心ID值
     *
     * @return maximum datacenter ID | 最大数据中心ID
     */
    public long maxDatacenterId() {
        return ~(-1L << datacenterBits);
    }

    /**
     * Gets the maximum sequence value
     * 获取最大序列号值
     *
     * @return maximum sequence | 最大序列号
     */
    public long maxSequence() {
        return ~(-1L << sequenceBits);
    }

    /**
     * Gets the epoch as Instant
     * 获取起始时间的Instant表示
     *
     * @return epoch instant | 起始时间
     */
    public Instant epochInstant() {
        return Instant.ofEpochMilli(epoch);
    }

    /**
     * Gets the worker ID left shift
     * 获取工作节点ID左移位数
     *
     * @return shift bits | 移位数
     */
    public int workerIdShift() {
        return sequenceBits;
    }

    /**
     * Gets the datacenter ID left shift
     * 获取数据中心ID左移位数
     *
     * @return shift bits | 移位数
     */
    public int datacenterIdShift() {
        return sequenceBits + workerBits;
    }

    /**
     * Gets the timestamp left shift
     * 获取时间戳左移位数
     *
     * @return shift bits | 移位数
     */
    public int timestampShift() {
        return sequenceBits + workerBits + datacenterBits;
    }

    /**
     * Validates this configuration
     * 验证此配置
     *
     * @throws IllegalArgumentException if configuration is invalid | 如果配置无效则抛出异常
     */
    public void validate() {
        if (workerId < 0 || workerId > maxWorkerId()) {
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d, got: %d", maxWorkerId(), workerId));
        }
        if (datacenterId < 0 || datacenterId > maxDatacenterId()) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID must be between 0 and %d, got: %d", maxDatacenterId(), datacenterId));
        }
        int totalBits = timestampBits + datacenterBits + workerBits + sequenceBits;
        if (totalBits != 63) {
            throw new IllegalArgumentException(
                    String.format("Total bits must be 63 (excluding sign bit), got: %d", totalBits));
        }
    }
}
