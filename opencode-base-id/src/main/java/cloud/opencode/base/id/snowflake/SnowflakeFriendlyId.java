package cloud.opencode.base.id.snowflake;

import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Snowflake Friendly ID - Human-readable representation for debugging and logging
 * 雪花ID友好格式 - 用于调试和日志记录的人类可读表示
 *
 * <p>Converts opaque Snowflake {@code long} values to and from a human-readable string
 * format that makes it trivial to extract the timestamp, datacenter, worker, and sequence
 * components without a separate tool. Inspired by CosId's {@code SnowflakeFriendlyId}.</p>
 * <p>在不透明的雪花{@code long}值和人类可读字符串格式之间相互转换，
 * 无需额外工具即可轻松提取时间戳、数据中心、工作节点和序列号组件。
 * 灵感来自CosId的{@code SnowflakeFriendlyId}。</p>
 *
 * <p><strong>Format | 格式:</strong></p>
 * <pre>
 * {ISO-8601-timestamp}#{datacenterId}-{workerId}-{sequence}
 *
 * Examples:
 *   "2024-01-15T10:30:00.123Z#3-7-42"
 *   "2024-06-01T00:00:00.000Z#0-0-0"
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert Snowflake long to human-readable string - 雪花long转人类可读字符串</li>
 *   <li>Parse back to original Snowflake long - 解析还原原始雪花long</li>
 *   <li>Configurable bit layout via SnowflakeConfig - 通过SnowflakeConfig配置位布局</li>
 *   <li>Useful in logs, monitoring, and support tickets - 适用于日志、监控和技术支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SnowflakeFriendlyId friendly = SnowflakeFriendlyId.ofDefault();
 *
 * long id = snowflakeGenerator.generate();
 * String readable = friendly.toFriendly(id);
 * // e.g. "2024-01-15T10:30:00.123Z#3-7-42"
 *
 * long recovered = friendly.fromFriendly(readable);
 * assert recovered == id;
 *
 * boolean ok = friendly.isFriendlyFormat("2024-01-15T10:30:00.123Z#3-7-42"); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless after construction) - 线程安全: 是（构造后无状态）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public final class SnowflakeFriendlyId {

    /** Friendly format: {ISO timestamp}#{datacenterId}-{workerId}-{sequence} */
    private static final Pattern FRIENDLY_PATTERN =
            Pattern.compile("^([^#]+)#(\\d+)-(\\d+)-(\\d+)$");

    private final SnowflakeConfig config;

    private SnowflakeFriendlyId(SnowflakeConfig config) {
        this.config = config;
    }

    /**
     * Creates a SnowflakeFriendlyId with the given configuration
     * 使用给定配置创建SnowflakeFriendlyId
     *
     * @param config the Snowflake configuration | 雪花配置
     * @return instance | 实例
     */
    public static SnowflakeFriendlyId of(SnowflakeConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        return new SnowflakeFriendlyId(config);
    }

    /**
     * Creates a SnowflakeFriendlyId with the default configuration
     * 使用默认配置创建SnowflakeFriendlyId
     *
     * @return instance | 实例
     */
    public static SnowflakeFriendlyId ofDefault() {
        return new SnowflakeFriendlyId(SnowflakeConfig.defaultConfig());
    }

    /**
     * Converts a Snowflake ID to its human-readable friendly format
     * 将雪花ID转换为人类可读的友好格式
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toFriendly(1705312200123049003L) = "2024-01-15T10:30:00.123Z#3-7-42"
     * toFriendly(0L)                  = "2021-01-01T00:00:00.000Z#0-0-0"
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(1), Space: O(1)</p>
     *
     * @param snowflakeId the Snowflake ID | 雪花ID
     * @return human-readable string | 人类可读字符串
     */
    public String toFriendly(long snowflakeId) {
        long sequence    = snowflakeId & config.maxSequence();
        long workerId    = (snowflakeId >> config.workerIdShift()) & config.maxWorkerId();
        long datacenterId= (snowflakeId >> config.datacenterIdShift()) & config.maxDatacenterId();
        long tsRelative  = snowflakeId >> config.timestampShift();
        long tsMillis    = tsRelative + config.epoch();

        String isoTs = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(tsMillis));
        return isoTs + "#" + datacenterId + "-" + workerId + "-" + sequence;
    }

    /**
     * Parses a friendly format string back to a Snowflake ID long
     * 将友好格式字符串解析还原为雪花ID的long值
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * fromFriendly("2024-01-15T10:30:00.123Z#3-7-42") = 1705312200123049003L (config-dependent)
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(1), Space: O(1)</p>
     *
     * @param friendly the friendly format string | 友好格式字符串
     * @return Snowflake ID | 雪花ID
     * @throws OpenIdGenerationException if the format is invalid | 格式无效时抛出
     */
    public long fromFriendly(String friendly) {
        if (friendly == null) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    "friendly ID string must not be null");
        }
        Matcher m = FRIENDLY_PATTERN.matcher(friendly);
        if (!m.matches()) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId", friendly);
        }
        String isoTs = m.group(1);
        long datacenterId;
        long workerId;
        long sequence;
        try {
            datacenterId = Long.parseLong(m.group(2));
            workerId     = Long.parseLong(m.group(3));
            sequence     = Long.parseLong(m.group(4));
        } catch (NumberFormatException e) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (component value out of range)");
        }

        long tsMillis;
        try {
            tsMillis = Instant.parse(isoTs).toEpochMilli();
        } catch (DateTimeParseException e) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (unparseable timestamp: " + isoTs + ")");
        }

        // Validate component ranges
        if (datacenterId < 0 || datacenterId > config.maxDatacenterId()) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (datacenterId " + datacenterId + " exceeds max " + config.maxDatacenterId() + ")");
        }
        if (workerId < 0 || workerId > config.maxWorkerId()) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (workerId " + workerId + " exceeds max " + config.maxWorkerId() + ")");
        }
        if (sequence < 0 || sequence > config.maxSequence()) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (sequence " + sequence + " exceeds max " + config.maxSequence() + ")");
        }

        long tsRelative = tsMillis - config.epoch();
        if (tsRelative < 0) {
            throw OpenIdGenerationException.invalidIdFormat("SnowflakeFriendlyId",
                    friendly + " (timestamp is before epoch " + config.epoch() + ")");
        }
        return (tsRelative << config.timestampShift())
                | (datacenterId << config.datacenterIdShift())
                | (workerId     << config.workerIdShift())
                | sequence;
    }

    /**
     * Checks whether a string is in the expected friendly format
     * 检查字符串是否符合预期的友好格式
     *
     * @param s the string to check | 要检查的字符串
     * @return true if the format is valid | 如果格式有效返回true
     */
    public boolean isFriendlyFormat(String s) {
        if (s == null) return false;
        Matcher m = FRIENDLY_PATTERN.matcher(s);
        if (!m.matches()) return false;
        try {
            Instant.parse(m.group(1));
            Long.parseLong(m.group(2));
            Long.parseLong(m.group(3));
            Long.parseLong(m.group(4));
            return true;
        } catch (DateTimeParseException | NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the configuration used by this instance
     * 返回此实例使用的配置
     *
     * @return Snowflake configuration | 雪花配置
     */
    public SnowflakeConfig getConfig() {
        return config;
    }
}
