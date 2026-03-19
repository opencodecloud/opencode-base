package cloud.opencode.base.id.tsid;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * TSID (Time-Sorted ID) Generator
 * TSID（时间排序ID）生成器
 *
 * <p>Generates 64-bit time-sorted unique identifiers. TSID is simpler than Snowflake
 * and recommended by Vlad Mihalcea for database primary keys.</p>
 * <p>生成64位时间排序的唯一标识符。TSID比Snowflake更简单，
 * 被Vlad Mihalcea推荐用于数据库主键。</p>
 *
 * <p><strong>TSID Structure | TSID结构 (64-bit):</strong></p>
 * <pre>
 * |------------------------------------------|-----------------|
 * |          42 bits timestamp               | 22 bits random  |
 * |------------------------------------------|-----------------|
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-sorted - 时间排序</li>
 *   <li>64-bit compact size - 64位紧凑大小</li>
 *   <li>No node configuration required - 无需节点配置</li>
 *   <li>Database index friendly - 数据库索引友好</li>
 *   <li>Crockford's Base32 encoding - Crockford Base32编码</li>
 * </ul>
 *
 * <p><strong>Capacity | 容量:</strong></p>
 * <ul>
 *   <li>~139 years from epoch - 从起始时间算起约139年</li>
 *   <li>~4 million IDs per millisecond - 每毫秒约400万个ID</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TsidGenerator gen = TsidGenerator.create();
 * long tsid = gen.generate();
 *
 * // As string (13 characters, Crockford's Base32)
 * String tsidStr = gen.generateStr();
 * // -> "0ARYZ1J8P0X0R"
 *
 * // With node configuration
 * TsidGenerator nodeGen = TsidGenerator.create(10, 1);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class TsidGenerator implements IdGenerator<Long> {

    /**
     * Default epoch: 2020-01-01 00:00:00 UTC
     * 默认起始时间：2020-01-01 00:00:00 UTC
     */
    public static final long DEFAULT_EPOCH = 1577836800000L;

    /**
     * Timestamp bits
     */
    private static final int TIMESTAMP_BITS = 42;

    /**
     * Random bits
     */
    private static final int RANDOM_BITS = 22;

    /**
     * Maximum random value
     */
    private static final long MAX_RANDOM = (1L << RANDOM_BITS) - 1;

    /**
     * Crockford's Base32 alphabet
     */
    private static final char[] BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private final long epoch;
    private final int nodeBits;
    private final long nodeId;
    private final long nodeMask;
    private final int counterBits;
    private final long counterMask;

    private volatile long lastTimestamp = -1;
    private volatile long counter = 0;

    /**
     * Creates a TSID generator with default settings
     */
    private TsidGenerator() {
        this(DEFAULT_EPOCH, 0, 0);
    }

    /**
     * Creates a TSID generator with node configuration
     *
     * @param nodeBits the number of bits for node ID (0-22)
     * @param nodeId   the node ID
     */
    private TsidGenerator(int nodeBits, long nodeId) {
        this(DEFAULT_EPOCH, nodeBits, nodeId);
    }

    /**
     * Creates a TSID generator with full configuration
     *
     * @param epoch    the custom epoch
     * @param nodeBits the number of bits for node ID (0-22)
     * @param nodeId   the node ID
     */
    private TsidGenerator(long epoch, int nodeBits, long nodeId) {
        if (nodeBits < 0 || nodeBits > RANDOM_BITS) {
            throw new IllegalArgumentException("nodeBits must be between 0 and " + RANDOM_BITS);
        }
        this.epoch = epoch;
        this.nodeBits = nodeBits;
        this.nodeId = nodeId & ((1L << nodeBits) - 1);
        this.nodeMask = (1L << nodeBits) - 1;
        this.counterBits = RANDOM_BITS - nodeBits;
        this.counterMask = (1L << counterBits) - 1;
    }

    /**
     * Creates a TSID generator with default settings
     * 使用默认设置创建TSID生成器
     *
     * @return generator | 生成器
     */
    public static TsidGenerator create() {
        return new TsidGenerator();
    }

    /**
     * Creates a TSID generator with node configuration
     * 使用节点配置创建TSID生成器
     *
     * @param nodeBits the number of bits for node ID (0-22) | 节点ID位数（0-22）
     * @param nodeId   the node ID | 节点ID
     * @return generator | 生成器
     */
    public static TsidGenerator create(int nodeBits, long nodeId) {
        return new TsidGenerator(nodeBits, nodeId);
    }

    /**
     * Creates a TSID generator with full configuration
     * 使用完整配置创建TSID生成器
     *
     * @param epoch    the custom epoch in milliseconds | 自定义起始时间（毫秒）
     * @param nodeBits the number of bits for node ID (0-22) | 节点ID位数（0-22）
     * @param nodeId   the node ID | 节点ID
     * @return generator | 生成器
     */
    public static TsidGenerator create(long epoch, int nodeBits, long nodeId) {
        return new TsidGenerator(epoch, nodeBits, nodeId);
    }

    @Override
    public synchronized Long generate() {
        long timestamp = System.currentTimeMillis() - epoch;

        if (timestamp == lastTimestamp) {
            counter = (counter + 1) & counterMask;
            if (counter == 0) {
                // Counter overflow, wait for next millisecond
                while (timestamp == lastTimestamp) {
                    timestamp = System.currentTimeMillis() - epoch;
                }
            }
        } else {
            // New millisecond, randomize counter
            counter = RANDOM.nextLong() & counterMask;
        }

        lastTimestamp = timestamp;

        // Compose TSID: timestamp (42 bits) | node (nodeBits) | counter (counterBits)
        long tsid = (timestamp << RANDOM_BITS);
        if (nodeBits > 0) {
            tsid |= (nodeId << counterBits);
        }
        tsid |= counter;

        return tsid;
    }

    /**
     * Generates a TSID as Crockford's Base32 string
     * 生成Crockford Base32编码的TSID字符串
     *
     * @return 13-character TSID string | 13字符TSID字符串
     */
    public String generateStr() {
        return encode(generate());
    }

    /**
     * Encodes a TSID to Crockford's Base32 string
     * 将TSID编码为Crockford Base32字符串
     *
     * @param tsid the TSID value | TSID值
     * @return 13-character string | 13字符字符串
     */
    public static String encode(long tsid) {
        char[] chars = new char[13];
        chars[0] = BASE32[(int) ((tsid >>> 60) & 0x1F)];
        chars[1] = BASE32[(int) ((tsid >>> 55) & 0x1F)];
        chars[2] = BASE32[(int) ((tsid >>> 50) & 0x1F)];
        chars[3] = BASE32[(int) ((tsid >>> 45) & 0x1F)];
        chars[4] = BASE32[(int) ((tsid >>> 40) & 0x1F)];
        chars[5] = BASE32[(int) ((tsid >>> 35) & 0x1F)];
        chars[6] = BASE32[(int) ((tsid >>> 30) & 0x1F)];
        chars[7] = BASE32[(int) ((tsid >>> 25) & 0x1F)];
        chars[8] = BASE32[(int) ((tsid >>> 20) & 0x1F)];
        chars[9] = BASE32[(int) ((tsid >>> 15) & 0x1F)];
        chars[10] = BASE32[(int) ((tsid >>> 10) & 0x1F)];
        chars[11] = BASE32[(int) ((tsid >>> 5) & 0x1F)];
        chars[12] = BASE32[(int) (tsid & 0x1F)];
        return new String(chars);
    }

    /**
     * Decodes a TSID string to long value
     * 将TSID字符串解码为长整型值
     *
     * @param tsidStr the TSID string | TSID字符串
     * @return TSID value | TSID值
     * @throws IllegalArgumentException if the string is invalid
     */
    public static long decode(String tsidStr) {
        if (tsidStr == null || tsidStr.length() != 13) {
            throw new IllegalArgumentException("TSID string must be 13 characters");
        }
        long tsid = 0;
        for (int i = 0; i < 13; i++) {
            int value = decodeChar(tsidStr.charAt(i));
            tsid = (tsid << 5) | value;
        }
        return tsid;
    }

    /**
     * Extracts the timestamp from a TSID
     * 从TSID提取时间戳
     *
     * @param tsid the TSID value | TSID值
     * @return the timestamp instant | 时间戳
     */
    public Instant extractTimestamp(long tsid) {
        long timestamp = (tsid >>> RANDOM_BITS) + epoch;
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Extracts the timestamp from a TSID with default epoch
     * 使用默认起始时间从TSID提取时间戳
     *
     * @param tsid the TSID value | TSID值
     * @return the timestamp instant | 时间戳
     */
    public static Instant extractTimestampStatic(long tsid) {
        long timestamp = (tsid >>> RANDOM_BITS) + DEFAULT_EPOCH;
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Validates a TSID string
     * 验证TSID字符串
     *
     * @param tsidStr the TSID string | TSID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String tsidStr) {
        if (tsidStr == null || tsidStr.length() != 13) {
            return false;
        }
        for (char c : tsidStr.toCharArray()) {
            if (decodeCharSafe(c) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the epoch
     * 获取起始时间
     *
     * @return epoch in milliseconds | 起始时间（毫秒）
     */
    public long getEpoch() {
        return epoch;
    }

    /**
     * Gets the node bits
     * 获取节点位数
     *
     * @return node bits | 节点位数
     */
    public int getNodeBits() {
        return nodeBits;
    }

    /**
     * Gets the node ID
     * 获取节点ID
     *
     * @return node ID | 节点ID
     */
    public long getNodeId() {
        return nodeId;
    }

    @Override
    public String getType() {
        return "TSID";
    }

    private static int decodeChar(char c) {
        int value = decodeCharSafe(c);
        if (value < 0) {
            throw new IllegalArgumentException("Invalid TSID character: " + c);
        }
        return value;
    }

    private static int decodeCharSafe(char c) {
        return switch (c) {
            case '0', 'O', 'o' -> 0;
            case '1', 'I', 'i', 'L', 'l' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            case 'A', 'a' -> 10;
            case 'B', 'b' -> 11;
            case 'C', 'c' -> 12;
            case 'D', 'd' -> 13;
            case 'E', 'e' -> 14;
            case 'F', 'f' -> 15;
            case 'G', 'g' -> 16;
            case 'H', 'h' -> 17;
            case 'J', 'j' -> 18;
            case 'K', 'k' -> 19;
            case 'M', 'm' -> 20;
            case 'N', 'n' -> 21;
            case 'P', 'p' -> 22;
            case 'Q', 'q' -> 23;
            case 'R', 'r' -> 24;
            case 'S', 's' -> 25;
            case 'T', 't' -> 26;
            case 'V', 'v' -> 27;
            case 'W', 'w' -> 28;
            case 'X', 'x' -> 29;
            case 'Y', 'y' -> 30;
            case 'Z', 'z' -> 31;
            default -> -1;
        };
    }
}
