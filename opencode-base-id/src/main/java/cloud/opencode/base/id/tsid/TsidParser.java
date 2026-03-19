package cloud.opencode.base.id.tsid;

import cloud.opencode.base.id.IdParser;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.time.Instant;

/**
 * TSID Parser
 * TSID解析器
 *
 * <p>Parses TSID values and strings to extract timestamp and random components.</p>
 * <p>解析TSID值和字符串以提取时间戳和随机组件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract timestamp - 提取时间戳</li>
 *   <li>Extract node ID (if configured) - 提取节点ID（如已配置）</li>
 *   <li>Extract counter/random bits - 提取计数器/随机位</li>
 *   <li>Validate format - 验证格式</li>
 *   <li>Parse both Long and String formats - 解析Long和String两种格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TsidParser parser = TsidParser.create();
 *
 * // Parse from string
 * ParsedTsid parsed = parser.parse("0ARYZ1J8P0X0R");
 * System.out.println("Time: " + parsed.time());
 * System.out.println("Random: " + parsed.random());
 *
 * // Parse from long
 * ParsedTsid parsed2 = parser.parse(123456789012345L);
 *
 * // With custom epoch
 * TsidParser customParser = TsidParser.create(customEpoch);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - parse() extracts timestamp (42 bits) and random bits (22 bits) from a fixed-size 64-bit value using bitwise operations - 时间复杂度: O(1) - parse() 通过位运算从固定大小的 64 位值中提取时间戳（42 位）和随机位（22 位）</li>
 *   <li>Space complexity: O(1) - produces a fixed-size ParsedTsid record - 空间复杂度: O(1) - 生成固定大小的 ParsedTsid 记录</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.4.0
 */
public final class TsidParser implements IdParser<String, TsidParser.ParsedTsid> {

    /**
     * Timestamp bits in TSID
     */
    private static final int TIMESTAMP_BITS = 42;

    /**
     * Random bits in TSID
     */
    private static final int RANDOM_BITS = 22;

    /**
     * Mask for random bits
     */
    private static final long RANDOM_MASK = (1L << RANDOM_BITS) - 1;

    private static final TsidParser DEFAULT_INSTANCE = new TsidParser(TsidGenerator.DEFAULT_EPOCH);

    private final long epoch;

    private TsidParser(long epoch) {
        this.epoch = epoch;
    }

    /**
     * Creates a TSID parser with default epoch (2020-01-01)
     * 使用默认起始时间（2020-01-01）创建TSID解析器
     *
     * @return parser | 解析器
     */
    public static TsidParser create() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Creates a TSID parser with custom epoch
     * 使用自定义起始时间创建TSID解析器
     *
     * @param epoch the custom epoch in milliseconds | 自定义起始时间（毫秒）
     * @return parser | 解析器
     */
    public static TsidParser create(long epoch) {
        if (epoch == TsidGenerator.DEFAULT_EPOCH) {
            return DEFAULT_INSTANCE;
        }
        return new TsidParser(epoch);
    }

    @Override
    public ParsedTsid parse(String tsidStr) {
        if (!isValid(tsidStr)) {
            throw OpenIdGenerationException.invalidIdFormat("TSID", tsidStr);
        }
        long tsid = TsidGenerator.decode(tsidStr);
        return parseInternal(tsidStr, tsid);
    }

    /**
     * Parses a TSID from long value
     * 从长整型值解析TSID
     *
     * @param tsid the TSID value | TSID值
     * @return parsed result | 解析结果
     * @throws IllegalArgumentException if the TSID is negative
     */
    public ParsedTsid parse(long tsid) {
        if (tsid < 0) {
            throw new IllegalArgumentException("TSID cannot be negative: " + tsid);
        }
        String tsidStr = TsidGenerator.encode(tsid);
        return parseInternal(tsidStr, tsid);
    }

    private ParsedTsid parseInternal(String tsidStr, long tsid) {
        long timestampMillis = (tsid >>> RANDOM_BITS) + epoch;
        long random = tsid & RANDOM_MASK;
        Instant time = Instant.ofEpochMilli(timestampMillis);

        return new ParsedTsid(tsidStr, tsid, time, timestampMillis, random);
    }

    @Override
    public Instant extractTimestamp(String tsidStr) {
        if (!isValid(tsidStr)) {
            throw OpenIdGenerationException.invalidIdFormat("TSID", tsidStr);
        }
        long tsid = TsidGenerator.decode(tsidStr);
        long timestampMillis = (tsid >>> RANDOM_BITS) + epoch;
        return Instant.ofEpochMilli(timestampMillis);
    }

    /**
     * Extracts the timestamp from a TSID long value
     * 从TSID长整型值提取时间戳
     *
     * @param tsid the TSID value | TSID值
     * @return the timestamp instant | 时间戳
     */
    public Instant extractTimestamp(long tsid) {
        long timestampMillis = (tsid >>> RANDOM_BITS) + epoch;
        return Instant.ofEpochMilli(timestampMillis);
    }

    @Override
    public boolean isValid(String tsidStr) {
        return TsidGenerator.isValid(tsidStr);
    }

    /**
     * Validates a TSID long value
     * 验证TSID长整型值
     *
     * @param tsid the TSID value | TSID值
     * @return true if valid (non-negative) | 如果有效（非负）返回true
     */
    public boolean isValid(long tsid) {
        return tsid >= 0;
    }

    /**
     * Gets the epoch used by this parser
     * 获取此解析器使用的起始时间
     *
     * @return epoch in milliseconds | 起始时间（毫秒）
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Parsed TSID Result
     * 解析的TSID结果
     *
     * @param tsidStr         the TSID as string (13 characters) | TSID字符串（13字符）
     * @param tsid            the TSID as long value | TSID长整型值
     * @param time            the timestamp as Instant | 时间戳（Instant）
     * @param timestampMillis the timestamp in milliseconds | 时间戳（毫秒）
     * @param random          the random/counter component (22 bits) | 随机/计数器组件（22位）
     */
    public record ParsedTsid(
            String tsidStr,
            long tsid,
            Instant time,
            long timestampMillis,
            long random
    ) {
        /**
         * Gets the timestamp part as binary string
         * 获取时间戳部分的二进制字符串
         *
         * @return 42-bit binary string | 42位二进制字符串
         */
        public String getTimestampBinary() {
            long timestamp = tsid >>> RANDOM_BITS;
            return String.format("%42s", Long.toBinaryString(timestamp)).replace(' ', '0');
        }

        /**
         * Gets the random part as binary string
         * 获取随机部分的二进制字符串
         *
         * @return 22-bit binary string | 22位二进制字符串
         */
        public String getRandomBinary() {
            return String.format("%22s", Long.toBinaryString(random)).replace(' ', '0');
        }

        /**
         * Calculates the approximate sequence number within the millisecond
         * 计算毫秒内的近似序列号
         *
         * <p>Note: This is only accurate if the TSID was generated with counter mode,
         * not purely random mode.</p>
         * <p>注意：仅当TSID使用计数器模式生成时才准确，纯随机模式不准确。</p>
         *
         * @return random/counter value | 随机/计数器值
         */
        public long getSequence() {
            return random;
        }

        @Override
        public String toString() {
            return String.format("ParsedTsid{tsid='%s', time=%s, random=%d}", tsidStr, time, random);
        }
    }
}
