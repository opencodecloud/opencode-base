package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.IdGenerator;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Snowflake ID Generator
 * 雪花ID生成器
 *
 * <p>Generates 64-bit unique IDs with the following structure:</p>
 * <p>生成具有以下结构的64位唯一ID：</p>
 * <ul>
 *   <li>1 bit - sign (always 0) | 符号位（始终为0）</li>
 *   <li>41 bits - timestamp (69 years) | 时间戳（约69年）</li>
 *   <li>5 bits - datacenter ID (0-31) | 数据中心ID（0-31）</li>
 *   <li>5 bits - worker ID (0-31) | 工作节点ID（0-31）</li>
 *   <li>12 bits - sequence (0-4095/ms) | 序列号（每毫秒0-4095）</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-ordered IDs - 时间有序ID</li>
 *   <li>High throughput (4096/ms) - 高吞吐量（每毫秒4096个）</li>
 *   <li>Clock backward handling - 时钟回拨处理</li>
 *   <li>Configurable bit allocation - 可配置位分配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default generator
 * SnowflakeGenerator gen = SnowflakeGenerator.create();
 * long id = gen.generate();
 *
 * // Custom worker/datacenter
 * SnowflakeGenerator gen2 = SnowflakeGenerator.create(1, 1);
 * long id2 = gen2.generate();
 *
 * // Using builder
 * SnowflakeGenerator gen3 = SnowflakeGenerator.builder()
 *     .workerId(1)
 *     .datacenterId(1)
 *     .clockBackwardStrategy(Wait.ofSeconds(5))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ReentrantLock) - 线程安全: 是（可重入锁）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class SnowflakeGenerator implements IdGenerator<Long> {

    private final SnowflakeConfig config;
    private final ClockBackwardStrategy clockBackwardStrategy;
    private final SnowflakeIdParser parser;

    private final long sequenceMask;
    private final int workerIdShift;
    private final int datacenterIdShift;
    private final int timestampShift;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile long lastTimestamp = -1L;
    private long sequence = 0L;

    /**
     * Creates a generator with configuration and strategy
     * 使用配置和策略创建生成器
     *
     * @param config                 the configuration | 配置
     * @param clockBackwardStrategy  the clock backward strategy | 时钟回拨策略
     */
    SnowflakeGenerator(SnowflakeConfig config, ClockBackwardStrategy clockBackwardStrategy) {
        config.validate();
        this.config = config;
        this.clockBackwardStrategy = clockBackwardStrategy != null
                ? clockBackwardStrategy : ThrowException.getInstance();
        this.parser = SnowflakeIdParser.create(config);

        this.sequenceMask = config.maxSequence();
        this.workerIdShift = config.workerIdShift();
        this.datacenterIdShift = config.datacenterIdShift();
        this.timestampShift = config.timestampShift();
    }

    /**
     * Creates a default generator
     * 创建默认生成器
     *
     * @return generator | 生成器
     */
    public static SnowflakeGenerator create() {
        return new SnowflakeGenerator(SnowflakeConfig.defaultConfig(), null);
    }

    /**
     * Creates a generator with worker and datacenter IDs
     * 使用工作节点ID和数据中心ID创建生成器
     *
     * @param workerId     the worker node ID (0-31) | 工作节点ID（0-31）
     * @param datacenterId the datacenter ID (0-31) | 数据中心ID（0-31）
     * @return generator | 生成器
     */
    public static SnowflakeGenerator create(long workerId, long datacenterId) {
        return new SnowflakeGenerator(SnowflakeConfig.of(workerId, datacenterId), null);
    }

    /**
     * Creates a builder for customized generator
     * 创建用于自定义生成器的构建器
     *
     * @return builder | 构建器
     */
    public static SnowflakeBuilder builder() {
        return new SnowflakeBuilder();
    }

    @Override
    public Long generate() {
        lock.lock();
        try {
            long timestamp = currentTimeMillis();

            if (timestamp < lastTimestamp) {
                timestamp = clockBackwardStrategy.handle(lastTimestamp, timestamp);
            }

            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = waitNextMillis(timestamp);
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return ((timestamp - config.epoch()) << timestampShift)
                    | (config.datacenterId() << datacenterIdShift)
                    | (config.workerId() << workerIdShift)
                    | sequence;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Generates an ID and returns as string
     * 生成ID并返回字符串
     *
     * @return ID string | ID字符串
     */
    public String generateStr() {
        return String.valueOf(generate());
    }

    /**
     * Gets the worker ID
     * 获取工作节点ID
     *
     * @return worker ID | 工作节点ID
     */
    public long getWorkerId() {
        return config.workerId();
    }

    /**
     * Gets the datacenter ID
     * 获取数据中心ID
     *
     * @return datacenter ID | 数据中心ID
     */
    public long getDatacenterId() {
        return config.datacenterId();
    }

    /**
     * Gets the epoch timestamp
     * 获取起始时间戳
     *
     * @return epoch timestamp | 起始时间戳
     */
    public long getEpoch() {
        return config.epoch();
    }

    /**
     * Gets the configuration
     * 获取配置
     *
     * @return configuration | 配置
     */
    public SnowflakeConfig getConfig() {
        return config;
    }

    /**
     * Parses a snowflake ID
     * 解析雪花ID
     *
     * @param id the ID to parse | 要解析的ID
     * @return parsed result | 解析结果
     */
    public SnowflakeIdParser.ParsedId parse(long id) {
        return parser.parse(id);
    }

    @Override
    public String getType() {
        return "Snowflake";
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long timestamp) {
        long current = currentTimeMillis();
        while (current <= timestamp) {
            current = currentTimeMillis();
        }
        return current;
    }
}
