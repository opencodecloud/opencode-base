package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.IdParser;

import java.time.Instant;

/**
 * Snowflake ID Parser
 * 雪花ID解析器
 *
 * <p>Parses Snowflake IDs to extract timestamp, datacenter ID,
 * worker ID, and sequence number.</p>
 * <p>解析雪花ID以提取时间戳、数据中心ID、工作节点ID和序列号。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract all ID components - 提取所有ID组件</li>
 *   <li>Configurable bit layout - 可配置位布局</li>
 *   <li>ID validation - ID验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeIdParser parser = SnowflakeIdParser.createDefault();
 * ParsedId parsed = parser.parse(123456789L);
 *
 * System.out.println("Time: " + parsed.time());
 * System.out.println("Datacenter: " + parsed.datacenterId());
 * System.out.println("Worker: " + parsed.workerId());
 * System.out.println("Sequence: " + parsed.sequence());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - parse() performs only bitwise shift and mask operations on the 64-bit ID - 时间复杂度: O(1) - parse() 仅对 64 位 ID 执行位移和掩码操作</li>
 *   <li>Space complexity: O(1) - returns a single ParsedId record with fixed fields - 空间复杂度: O(1) - 返回包含固定字段的单个 ParsedId 记录</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class SnowflakeIdParser implements IdParser<Long, SnowflakeIdParser.ParsedId> {

    private final SnowflakeConfig config;
    private final long sequenceMask;
    private final long workerIdMask;
    private final long datacenterIdMask;

    /**
     * Creates a parser with configuration
     * 使用配置创建解析器
     *
     * @param config the configuration | 配置
     */
    private SnowflakeIdParser(SnowflakeConfig config) {
        this.config = config;
        this.sequenceMask = config.maxSequence();
        this.workerIdMask = config.maxWorkerId();
        this.datacenterIdMask = config.maxDatacenterId();
    }

    /**
     * Creates a parser with configuration
     * 使用配置创建解析器
     *
     * @param config the configuration | 配置
     * @return parser | 解析器
     */
    public static SnowflakeIdParser create(SnowflakeConfig config) {
        return new SnowflakeIdParser(config);
    }

    /**
     * Creates a parser with default configuration
     * 使用默认配置创建解析器
     *
     * @return parser | 解析器
     */
    public static SnowflakeIdParser createDefault() {
        return new SnowflakeIdParser(SnowflakeConfig.defaultConfig());
    }

    @Override
    public ParsedId parse(Long id) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID must be a non-negative number");
        }

        long sequence = id & sequenceMask;
        long workerId = (id >> config.workerIdShift()) & workerIdMask;
        long datacenterId = (id >> config.datacenterIdShift()) & datacenterIdMask;
        long timestamp = (id >> config.timestampShift()) + config.epoch();

        return new ParsedId(
                id,
                timestamp,
                Instant.ofEpochMilli(timestamp),
                datacenterId,
                workerId,
                sequence
        );
    }

    @Override
    public Instant extractTimestamp(Long id) {
        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID must be a non-negative number");
        }
        long timestamp = (id >> config.timestampShift()) + config.epoch();
        return Instant.ofEpochMilli(timestamp);
    }

    @Override
    public boolean isValid(Long id) {
        if (id == null || id < 0) {
            return false;
        }
        try {
            ParsedId parsed = parse(id);
            return parsed.workerId() <= config.maxWorkerId()
                    && parsed.datacenterId() <= config.maxDatacenterId()
                    && parsed.sequence() <= config.maxSequence()
                    && parsed.timestamp() >= config.epoch();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parsed Snowflake ID Result
     * 解析的雪花ID结果
     *
     * @param id           the original ID | 原始ID
     * @param timestamp    the timestamp in milliseconds | 时间戳（毫秒）
     * @param time         the timestamp as Instant | 时间戳（Instant）
     * @param datacenterId the datacenter ID | 数据中心ID
     * @param workerId     the worker ID | 工作节点ID
     * @param sequence     the sequence number | 序列号
     */
    public record ParsedId(
            long id,
            long timestamp,
            Instant time,
            long datacenterId,
            long workerId,
            long sequence
    ) {
        @Override
        public String toString() {
            return String.format(
                    "ParsedId{id=%d, time=%s, datacenter=%d, worker=%d, sequence=%d}",
                    id, time, datacenterId, workerId, sequence
            );
        }
    }
}
