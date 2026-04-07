package cloud.opencode.base.id.ulid;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;

/**
 * ULID Generator - Universally Unique Lexicographically Sortable Identifier
 * ULID生成器 - 通用唯一字典序可排序标识符
 *
 * <p>Generates Universally Unique Lexicographically Sortable Identifiers.
 * ULID is a 128-bit identifier encoded as 26 characters using Crockford's Base32.</p>
 * <p>生成通用唯一字典序可排序标识符。ULID是128位标识符，
 * 使用Crockford的Base32编码为26个字符。</p>
 *
 * <p><strong>ULID Structure | ULID结构:</strong></p>
 * <pre>
 *  01AN4Z07BY      79KA1307SR9X4MV3
 * |----------|    |----------------|
 *  Timestamp          Randomness
 *   48bits             80bits
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lexicographically sortable - 字典序可排序</li>
 *   <li>Case insensitive - 大小写不敏感</li>
 *   <li>URL safe - URL安全</li>
 *   <li>Monotonic within same millisecond (configurable) - 同毫秒内单调递增（可配置）</li>
 *   <li>Non-monotonic mode via {@link UlidConfig} - 通过UlidConfig配置非单调模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Monotonic (default)
 * UlidGenerator gen = UlidGenerator.create();
 * String ulid = gen.generate();
 * // -> "01ARZ3NDEKTSV4RRFFQ69G5FAV"
 *
 * // Non-monotonic
 * UlidGenerator gen2 = UlidGenerator.create(UlidConfig.nonMonotonic());
 * String ulid2 = gen2.generate();
 *
 * // Validate
 * boolean valid = UlidGenerator.isValid(ulid);
 *
 * // Compare
 * int cmp = UlidGenerator.compare(ulid1, ulid2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (synchronized) - 线程安全: 是（同步）</li>
 *   <li>Entropy: SecureRandom - 熵源: SecureRandom</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class UlidGenerator implements IdGenerator<String> {

    /**
     * Crockford's Base32 alphabet
     * Crockford的Base32字母表
     */
    private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    /**
     * Decoding array
     * 解码数组
     */
    private static final byte[] DECODING = new byte[128];

    static {
        for (int i = 0; i < DECODING.length; i++) {
            DECODING[i] = -1;
        }
        for (int i = 0; i < ENCODING.length; i++) {
            DECODING[ENCODING[i]] = (byte) i;
            DECODING[Character.toLowerCase(ENCODING[i])] = (byte) i;
        }
        // Handle ambiguous characters
        DECODING['O'] = DECODING['o'] = 0;
        DECODING['I'] = DECODING['i'] = 1;
        DECODING['L'] = DECODING['l'] = 1;
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final UlidGenerator INSTANCE = new UlidGenerator(true);

    private final boolean monotonic;
    private volatile long lastTimestamp = 0;
    private final byte[] lastRandomness = new byte[10];

    /**
     * Private constructor
     * 私有构造方法
     *
     * @param monotonic whether to use monotonic mode | 是否使用单调模式
     */
    private UlidGenerator(boolean monotonic) {
        this.monotonic = monotonic;
    }

    /**
     * Creates a default monotonic ULID generator (singleton)
     * 创建默认单调ULID生成器（单例）
     *
     * @return monotonic generator | 单调生成器
     */
    public static UlidGenerator create() {
        return INSTANCE;
    }

    /**
     * Creates a ULID generator with explicit configuration
     * 使用显式配置创建ULID生成器
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * UlidGenerator.create(UlidConfig.defaultConfig())    // monotonic singleton
     * UlidGenerator.create(UlidConfig.nonMonotonic())     // new non-monotonic instance
     * </pre>
     *
     * @param config the ULID configuration | ULID配置
     * @return generator | 生成器
     */
    public static UlidGenerator create(UlidConfig config) {
        if (config == null || config.monotonic()) {
            return INSTANCE;
        }
        return new UlidGenerator(false);
    }

    /**
     * Returns whether this generator uses monotonic mode
     * 返回此生成器是否使用单调模式
     *
     * @return true if monotonic | 如果是单调模式返回true
     */
    public boolean isMonotonic() {
        return monotonic;
    }

    @Override
    public synchronized String generate() {
        return generate(System.currentTimeMillis());
    }

    /**
     * Generates a ULID with specific timestamp
     * 使用指定时间戳生成ULID
     *
     * @param timestamp the timestamp in milliseconds | 时间戳（毫秒）
     * @return ULID string (26 characters) | ULID字符串（26字符）
     */
    public synchronized String generate(long timestamp) {
        if (monotonic && timestamp == lastTimestamp) {
            // Monotonic mode: increment randomness for monotonicity within same ms
            // 单调模式：在同一毫秒内递增随机数保证单调性
            if (!incrementRandomness()) {
                // Overflow: wait for next millisecond
                while (timestamp == lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
                lastTimestamp = timestamp;
                RANDOM.nextBytes(lastRandomness);
            }
        } else {
            // Non-monotonic mode or new millisecond: always fresh random bytes
            // 非单调模式或新毫秒：总是生成新的随机字节
            lastTimestamp = timestamp;
            RANDOM.nextBytes(lastRandomness);
        }

        char[] chars = new char[26];
        encodeTimestamp(timestamp, chars);
        encodeRandomness(lastRandomness, chars);

        return new String(chars);
    }

    /**
     * Generates ULID as byte array
     * 生成ULID字节数组
     *
     * @return 16-byte array | 16字节数组
     */
    public synchronized byte[] generateBytes() {
        long timestamp = System.currentTimeMillis();
        if (monotonic && timestamp == lastTimestamp) {
            if (!incrementRandomness()) {
                // Overflow: wait for next millisecond
                while (timestamp == lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
                lastTimestamp = timestamp;
                RANDOM.nextBytes(lastRandomness);
            }
        } else {
            lastTimestamp = timestamp;
            RANDOM.nextBytes(lastRandomness);
        }

        byte[] bytes = new byte[16];
        // Timestamp (6 bytes)
        bytes[0] = (byte) (timestamp >>> 40);
        bytes[1] = (byte) (timestamp >>> 32);
        bytes[2] = (byte) (timestamp >>> 24);
        bytes[3] = (byte) (timestamp >>> 16);
        bytes[4] = (byte) (timestamp >>> 8);
        bytes[5] = (byte) timestamp;
        // Randomness (10 bytes)
        System.arraycopy(lastRandomness, 0, bytes, 6, 10);

        return bytes;
    }

    /**
     * Validates a ULID string
     * 验证ULID字符串
     *
     * @param ulid the ULID string | ULID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String ulid) {
        if (ulid == null || ulid.length() != 26) {
            return false;
        }
        for (char c : ulid.toCharArray()) {
            if (c >= 128 || DECODING[c] == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses a ULID string
     * 解析ULID字符串
     *
     * @param ulid the ULID string | ULID字符串
     * @return parsed result | 解析结果
     */
    public static UlidParser.ParsedUlid parse(String ulid) {
        return UlidParser.create().parse(ulid);
    }

    /**
     * Compares two ULID strings
     * 比较两个ULID字符串
     *
     * @param ulid1 first ULID | 第一个ULID
     * @param ulid2 second ULID | 第二个ULID
     * @return comparison result | 比较结果
     * @throws NullPointerException if either argument is null
     */
    public static int compare(String ulid1, String ulid2) {
        java.util.Objects.requireNonNull(ulid1, "ulid1 must not be null");
        java.util.Objects.requireNonNull(ulid2, "ulid2 must not be null");
        return ulid1.compareTo(ulid2);
    }

    @Override
    public String getType() {
        return "ULID";
    }

    /**
     * Increments randomness for monotonicity within same millisecond
     * @return true if increment succeeded, false if overflow occurred
     */
    private boolean incrementRandomness() {
        for (int i = lastRandomness.length - 1; i >= 0; i--) {
            if (++lastRandomness[i] != 0) {
                return true; // No overflow
            }
        }
        // All bytes wrapped to 0 - overflow occurred
        return false;
    }

    private void encodeTimestamp(long timestamp, char[] chars) {
        chars[0] = ENCODING[(int) ((timestamp >>> 45) & 0x1F)];
        chars[1] = ENCODING[(int) ((timestamp >>> 40) & 0x1F)];
        chars[2] = ENCODING[(int) ((timestamp >>> 35) & 0x1F)];
        chars[3] = ENCODING[(int) ((timestamp >>> 30) & 0x1F)];
        chars[4] = ENCODING[(int) ((timestamp >>> 25) & 0x1F)];
        chars[5] = ENCODING[(int) ((timestamp >>> 20) & 0x1F)];
        chars[6] = ENCODING[(int) ((timestamp >>> 15) & 0x1F)];
        chars[7] = ENCODING[(int) ((timestamp >>> 10) & 0x1F)];
        chars[8] = ENCODING[(int) ((timestamp >>> 5) & 0x1F)];
        chars[9] = ENCODING[(int) (timestamp & 0x1F)];
    }

    private void encodeRandomness(byte[] randomness, char[] chars) {
        chars[10] = ENCODING[(randomness[0] >>> 3) & 0x1F];
        chars[11] = ENCODING[((randomness[0] << 2) | ((randomness[1] & 0xFF) >>> 6)) & 0x1F];
        chars[12] = ENCODING[(randomness[1] >>> 1) & 0x1F];
        chars[13] = ENCODING[((randomness[1] << 4) | ((randomness[2] & 0xFF) >>> 4)) & 0x1F];
        chars[14] = ENCODING[((randomness[2] << 1) | ((randomness[3] & 0xFF) >>> 7)) & 0x1F];
        chars[15] = ENCODING[(randomness[3] >>> 2) & 0x1F];
        chars[16] = ENCODING[((randomness[3] << 3) | ((randomness[4] & 0xFF) >>> 5)) & 0x1F];
        chars[17] = ENCODING[randomness[4] & 0x1F];
        chars[18] = ENCODING[(randomness[5] >>> 3) & 0x1F];
        chars[19] = ENCODING[((randomness[5] << 2) | ((randomness[6] & 0xFF) >>> 6)) & 0x1F];
        chars[20] = ENCODING[(randomness[6] >>> 1) & 0x1F];
        chars[21] = ENCODING[((randomness[6] << 4) | ((randomness[7] & 0xFF) >>> 4)) & 0x1F];
        chars[22] = ENCODING[((randomness[7] << 1) | ((randomness[8] & 0xFF) >>> 7)) & 0x1F];
        chars[23] = ENCODING[(randomness[8] >>> 2) & 0x1F];
        chars[24] = ENCODING[((randomness[8] << 3) | ((randomness[9] & 0xFF) >>> 5)) & 0x1F];
        chars[25] = ENCODING[randomness[9] & 0x1F];
    }
}
