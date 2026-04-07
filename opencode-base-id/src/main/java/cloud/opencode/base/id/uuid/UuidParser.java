package cloud.opencode.base.id.uuid;

import cloud.opencode.base.id.IdParser;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.time.Instant;
import java.util.UUID;

/**
 * UUID Parser - Extracts structured information from UUID objects
 * UUID解析器 - 从UUID对象中提取结构化信息
 *
 * <p>Implements {@link IdParser} for {@link UUID}, supporting version detection,
 * timestamp extraction (for time-based UUIDs v1, v6, v7), and validation.
 * Fills the gap where other ID types have parsers but UUID did not.</p>
 * <p>为{@link UUID}实现{@link IdParser}，支持版本检测、时间戳提取
 * （适用于基于时间的UUID v1、v6、v7）和验证。
 * 填补了其他ID类型有解析器而UUID没有的空缺。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Detect UUID version (1-8) - 检测UUID版本（1-8）</li>
 *   <li>Extract timestamp for v1, v6, v7 UUIDs - 提取v1、v6、v7 UUID的时间戳</li>
 *   <li>Variant detection - 变体检测</li>
 *   <li>Short string representation (no hyphens) - 短字符串表示（无连字符）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UuidParser parser = UuidParser.create();
 *
 * // Parse a UUID v7
 * UUID v7 = UuidV7Generator.create().generate();
 * UuidParser.ParsedUuid parsed = parser.parse(v7);
 * System.out.println("Version: " + parsed.version());        // 7
 * System.out.println("Timestamp: " + parsed.timestamp());    // Instant
 * System.out.println("Time-ordered: " + parsed.timeOrdered()); // true
 * System.out.println("Short: " + parsed.toShortString());    // 32-char string
 *
 * // Extract timestamp directly
 * Instant ts = parser.extractTimestamp(v7);
 *
 * // Validate
 * boolean valid = parser.isValid(v7); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (throws on null) - 空值安全: 否（空值抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.3
 */
public final class UuidParser implements IdParser<UUID, UuidParser.ParsedUuid> {

    private static final UuidParser INSTANCE = new UuidParser();

    // UUID v1 time offset: 100-nanosecond intervals from 1582-10-15 to 1970-01-01
    private static final long UUID_V1_EPOCH_OFFSET = 122192928000000000L;

    private UuidParser() {
    }

    /**
     * Returns the singleton UuidParser instance
     * 返回单例UuidParser实例
     *
     * @return parser | 解析器
     */
    public static UuidParser create() {
        return INSTANCE;
    }

    /**
     * Parses a UUID into a structured result
     * 将UUID解析为结构化结果
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * parse(UuidV7Generator.create().generate()) → ParsedUuid{version=7, timeOrdered=true, timestamp=...}
     * parse(UUID.randomUUID())                   → ParsedUuid{version=4, timeOrdered=false, timestamp=null}
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(1), Space: O(1)</p>
     *
     * @param uuid the UUID to parse | 要解析的UUID
     * @return parsed result | 解析结果
     * @throws OpenIdGenerationException if uuid is null | uuid为null时抛出
     */
    @Override
    public ParsedUuid parse(UUID uuid) {
        if (uuid == null) {
            throw OpenIdGenerationException.invalidIdFormat("UUID", "uuid must not be null");
        }
        int version = (int) ((uuid.getMostSignificantBits() >> 12) & 0xFL);
        int variant = (int) (uuid.getLeastSignificantBits() >>> 62);
        boolean timeOrdered = (version == 1 || version == 6 || version == 7);
        Instant timestamp = timeOrdered ? extractTimestampInternal(uuid, version) : null;
        return new ParsedUuid(uuid, version, variant, timestamp, timeOrdered);
    }

    /**
     * Extracts the embedded timestamp from a time-based UUID (v1, v6, v7)
     * 从基于时间的UUID（v1、v6、v7）中提取嵌入的时间戳
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * extractTimestamp(v7Uuid) = Instant (ms precision)
     * extractTimestamp(v4Uuid) = throws OpenIdGenerationException
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(1), Space: O(1)</p>
     *
     * @param uuid the UUID | UUID
     * @return the embedded timestamp | 嵌入的时间戳
     * @throws OpenIdGenerationException if the UUID is not time-based (not v1/v6/v7) | 非基于时间的UUID时抛出
     */
    @Override
    public Instant extractTimestamp(UUID uuid) {
        if (uuid == null) {
            throw OpenIdGenerationException.invalidIdFormat("UUID", "uuid must not be null");
        }
        int version = (int) ((uuid.getMostSignificantBits() >> 12) & 0xFL);
        if (version != 1 && version != 6 && version != 7) {
            throw OpenIdGenerationException.invalidIdFormat("UUID-v" + version,
                    uuid.toString() + " — only UUID v1, v6, v7 have embedded timestamps");
        }
        return extractTimestampInternal(uuid, version);
    }

    /**
     * Validates that the UUID is non-null and has a recognized version
     * 验证UUID非空且具有可识别的版本
     *
     * @param uuid the UUID | UUID
     * @return true if valid | 如果有效返回true
     */
    @Override
    public boolean isValid(UUID uuid) {
        if (uuid == null) return false;
        int version = (int) ((uuid.getMostSignificantBits() >> 12) & 0xFL);
        return version >= 1 && version <= 8;
    }

    private Instant extractTimestampInternal(UUID uuid, int version) {
        return switch (version) {
            case 7 -> {
                // v7: top 48 bits of mostSignificantBits hold unix_ts_ms
                long unixMs = uuid.getMostSignificantBits() >>> 16;
                yield Instant.ofEpochMilli(unixMs);
            }
            case 1 -> {
                // v1: 60-bit timestamp in 100-ns intervals from 1582-10-15
                long timeLow  = uuid.getMostSignificantBits() >>> 32;
                long timeMid  = (uuid.getMostSignificantBits() >>> 16) & 0xFFFFL;
                long timeHigh = uuid.getMostSignificantBits() & 0x0FFFL;
                long ts100ns  = (timeHigh << 48) | (timeMid << 32) | timeLow;
                yield gregorianToInstant(ts100ns);
            }
            case 6 -> {
                // v6: time_high(32)|time_mid(16)|version(4)|time_low(12) in MSB
                // Reconstruct 60-bit timestamp: strip version nibble, reassemble high+mid+low
                long msb = uuid.getMostSignificantBits();
                long ts100ns = ((msb & 0xFFFFFFFFFFFF0000L) >>> 4) | (msb & 0x0FFFL);
                yield gregorianToInstant(ts100ns);
            }
            default -> throw OpenIdGenerationException.invalidIdFormat("UUID",
                    "unsupported UUID version for timestamp extraction: " + version);
        };
    }

    /**
     * Converts Gregorian 100-ns timestamp to Instant without long overflow.
     * 将Gregorian 100纳秒时间戳转换为Instant，避免long溢出。
     */
    private static Instant gregorianToInstant(long ts100ns) {
        // Convert 100-ns intervals to seconds and nanos separately to avoid overflow
        // ts100ns is at most 2^60 ≈ 1.15e18; dividing by 10_000_000 gives seconds safely
        long adjustedTs = ts100ns - UUID_V1_EPOCH_OFFSET;
        long seconds = adjustedTs / 10_000_000L;
        long remainderNanos = (adjustedTs % 10_000_000L) * 100;
        return Instant.ofEpochSecond(seconds, remainderNanos);
    }

    // ==================== ParsedUuid Record | 解析结果记录 ====================

    /**
     * Structured result of parsing a UUID
     * UUID解析的结构化结果
     *
     * @param uuid        the original UUID | 原始UUID
     * @param version     UUID version (1-8) | UUID版本（1-8）
     * @param variant     UUID variant bits | UUID变体位
     * @param timestamp   embedded timestamp for v1/v6/v7, null otherwise | v1/v6/v7的嵌入时间戳，否则为null
     * @param timeOrdered true for v1, v6, v7 (time-ordered) | v1、v6、v7为true（时间有序）
     */
    public record ParsedUuid(
            UUID uuid,
            int version,
            int variant,
            Instant timestamp,
            boolean timeOrdered
    ) {
        /**
         * Returns the UUID as a 32-character string without hyphens
         * 返回不含连字符的32字符UUID字符串
         *
         * @return short UUID string | 短UUID字符串
         */
        public String toShortString() {
            return uuid.toString().replace("-", "");
        }

        /**
         * Returns true if this is a UUID version 7
         * 如果是UUID版本7则返回true
         *
         * @return true for v7 | v7时返回true
         */
        public boolean isV7() {
            return version == 7;
        }

        /**
         * Returns true if this is a UUID version 4 (random)
         * 如果是UUID版本4（随机）则返回true
         *
         * @return true for v4 | v4时返回true
         */
        public boolean isV4() {
            return version == 4;
        }

        @Override
        public String toString() {
            return String.format("ParsedUuid{uuid=%s, version=%d, variant=%d, timestamp=%s, timeOrdered=%b}",
                    uuid, version, variant, timestamp, timeOrdered);
        }
    }
}
