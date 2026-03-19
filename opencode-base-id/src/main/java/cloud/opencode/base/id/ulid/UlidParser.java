package cloud.opencode.base.id.ulid;

import cloud.opencode.base.id.IdParser;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.time.Instant;
import java.util.Arrays;

/**
 * ULID Parser
 * ULID解析器
 *
 * <p>Parses ULID strings to extract timestamp and randomness components.</p>
 * <p>解析ULID字符串以提取时间戳和随机性组件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extract timestamp - 提取时间戳</li>
 *   <li>Extract randomness - 提取随机性</li>
 *   <li>Validate format - 验证格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * UlidParser parser = UlidParser.create();
 * ParsedUlid parsed = parser.parse("01ARZ3NDEKTSV4RRFFQ69G5FAV");
 *
 * System.out.println("Time: " + parsed.time());
 * System.out.println("Timestamp part: " + parsed.getTimestampPart());
 * System.out.println("Randomness part: " + parsed.getRandomnessPart());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - parse() decodes a fixed 26-character Crockford Base32 string using a pre-built static lookup table - 时间复杂度: O(1) - parse() 使用预构建静态查找表解码固定 26 字符的 Crockford Base32 字符串</li>
 *   <li>Space complexity: O(1) - produces a fixed-size ParsedUlid record - 空间复杂度: O(1) - 生成固定大小的 ParsedUlid 记录</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class UlidParser implements IdParser<String, UlidParser.ParsedUlid> {

    /**
     * Decoding array for Crockford's Base32
     * Crockford Base32解码数组
     */
    private static final byte[] DECODING = new byte[128];

    static {
        Arrays.fill(DECODING, (byte) -1);
        char[] encoding = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
        for (int i = 0; i < encoding.length; i++) {
            DECODING[encoding[i]] = (byte) i;
            DECODING[Character.toLowerCase(encoding[i])] = (byte) i;
        }
        DECODING['O'] = DECODING['o'] = 0;
        DECODING['I'] = DECODING['i'] = 1;
        DECODING['L'] = DECODING['l'] = 1;
    }

    private static final UlidParser INSTANCE = new UlidParser();

    private UlidParser() {
    }

    /**
     * Creates a ULID parser
     * 创建ULID解析器
     *
     * @return parser | 解析器
     */
    public static UlidParser create() {
        return INSTANCE;
    }

    @Override
    public ParsedUlid parse(String ulid) {
        if (!isValid(ulid)) {
            throw OpenIdGenerationException.invalidIdFormat("ULID", ulid);
        }

        long timestamp = decodeTimestamp(ulid);
        byte[] randomness = decodeRandomness(ulid);

        return new ParsedUlid(
                ulid,
                timestamp,
                Instant.ofEpochMilli(timestamp),
                randomness
        );
    }

    @Override
    public Instant extractTimestamp(String ulid) {
        if (!isValid(ulid)) {
            throw OpenIdGenerationException.invalidIdFormat("ULID", ulid);
        }
        return Instant.ofEpochMilli(decodeTimestamp(ulid));
    }

    @Override
    public boolean isValid(String ulid) {
        return UlidGenerator.isValid(ulid);
    }

    private long decodeTimestamp(String ulid) {
        long timestamp = 0;
        for (int i = 0; i < 10; i++) {
            timestamp = (timestamp << 5) | DECODING[ulid.charAt(i)];
        }
        return timestamp;
    }

    private byte[] decodeRandomness(String ulid) {
        byte[] randomness = new byte[10];

        randomness[0] = (byte) ((DECODING[ulid.charAt(10)] << 3) | (DECODING[ulid.charAt(11)] >>> 2));
        randomness[1] = (byte) ((DECODING[ulid.charAt(11)] << 6) | (DECODING[ulid.charAt(12)] << 1) | (DECODING[ulid.charAt(13)] >>> 4));
        randomness[2] = (byte) ((DECODING[ulid.charAt(13)] << 4) | (DECODING[ulid.charAt(14)] >>> 1));
        randomness[3] = (byte) ((DECODING[ulid.charAt(14)] << 7) | (DECODING[ulid.charAt(15)] << 2) | (DECODING[ulid.charAt(16)] >>> 3));
        randomness[4] = (byte) ((DECODING[ulid.charAt(16)] << 5) | DECODING[ulid.charAt(17)]);
        randomness[5] = (byte) ((DECODING[ulid.charAt(18)] << 3) | (DECODING[ulid.charAt(19)] >>> 2));
        randomness[6] = (byte) ((DECODING[ulid.charAt(19)] << 6) | (DECODING[ulid.charAt(20)] << 1) | (DECODING[ulid.charAt(21)] >>> 4));
        randomness[7] = (byte) ((DECODING[ulid.charAt(21)] << 4) | (DECODING[ulid.charAt(22)] >>> 1));
        randomness[8] = (byte) ((DECODING[ulid.charAt(22)] << 7) | (DECODING[ulid.charAt(23)] << 2) | (DECODING[ulid.charAt(24)] >>> 3));
        randomness[9] = (byte) ((DECODING[ulid.charAt(24)] << 5) | DECODING[ulid.charAt(25)]);

        return randomness;
    }

    /**
     * Parsed ULID Result
     * 解析的ULID结果
     */
    public static final class ParsedUlid {

        private final String ulid;
        private final long timestamp;
        private final Instant time;
        private final byte[] randomness;

        /**
         * Creates a parsed ULID result
         * 创建解析的ULID结果
         *
         * @param ulid       the original ULID | 原始ULID
         * @param timestamp  the timestamp in milliseconds | 时间戳（毫秒）
         * @param time       the timestamp as Instant | 时间戳（Instant）
         * @param randomness the randomness bytes | 随机性字节
         */
        public ParsedUlid(String ulid, long timestamp, Instant time, byte[] randomness) {
            this.ulid = ulid;
            this.timestamp = timestamp;
            this.time = time;
            this.randomness = randomness;
        }

        /**
         * Gets the original ULID
         * 获取原始ULID
         *
         * @return ULID string | ULID字符串
         */
        public String ulid() {
            return ulid;
        }

        /**
         * Gets the timestamp
         * 获取时间戳
         *
         * @return timestamp in milliseconds | 时间戳（毫秒）
         */
        public long timestamp() {
            return timestamp;
        }

        /**
         * Gets the time
         * 获取时间
         *
         * @return time as Instant | 时间（Instant）
         */
        public Instant time() {
            return time;
        }

        /**
         * Gets the randomness bytes
         * 获取随机性字节
         *
         * @return randomness bytes (copy) | 随机性字节（副本）
         */
        public byte[] randomness() {
            return Arrays.copyOf(randomness, randomness.length);
        }

        /**
         * Gets the timestamp part (first 10 characters)
         * 获取时间戳部分（前10个字符）
         *
         * @return timestamp part | 时间戳部分
         */
        public String getTimestampPart() {
            return ulid.substring(0, 10);
        }

        /**
         * Gets the randomness part (last 16 characters)
         * 获取随机性部分（后16个字符）
         *
         * @return randomness part | 随机性部分
         */
        public String getRandomnessPart() {
            return ulid.substring(10);
        }

        @Override
        public String toString() {
            return String.format("ParsedUlid{ulid=%s, time=%s}", ulid, time);
        }
    }
}
