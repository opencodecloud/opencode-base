package cloud.opencode.base.id.ksuid;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;

/**
 * KSUID (K-Sortable Unique Identifier) Generator
 * KSUID（K可排序唯一标识符）生成器
 *
 * <p>Generates K-Sortable Unique Identifiers. KSUIDs are 20-byte identifiers
 * encoded as 27-character Base62 strings. They are roughly sortable by creation time.</p>
 * <p>生成K可排序唯一标识符。KSUID是20字节的标识符，
 * 编码为27字符的Base62字符串。它们可以按创建时间大致排序。</p>
 *
 * <p><strong>KSUID Structure | KSUID结构 (160-bit / 20-byte):</strong></p>
 * <pre>
 * |--------------------------------|------------------------------------------|
 * |    4 bytes timestamp           |         16 bytes random payload          |
 * |    (seconds since epoch)       |         (cryptographically secure)       |
 * |--------------------------------|------------------------------------------|
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>K-sortable (roughly time-ordered) - K可排序（大致时间有序）</li>
 *   <li>Base62 encoded (URL-safe) - Base62编码（URL安全）</li>
 *   <li>27 characters fixed length - 固定27字符长度</li>
 *   <li>Cryptographically secure random payload - 加密安全的随机负载</li>
 *   <li>~100 years from custom epoch (2014-05-13) - 从自定义起始时间起约100年</li>
 * </ul>
 *
 * <p><strong>Comparison with ULID | 与ULID比较:</strong></p>
 * <ul>
 *   <li>KSUID: 20 bytes, 27 chars, second precision - KSUID: 20字节，27字符，秒精度</li>
 *   <li>ULID: 16 bytes, 26 chars, millisecond precision - ULID: 16字节，26字符，毫秒精度</li>
 *   <li>KSUID has more random bits (128 vs 80) - KSUID有更多随机位（128 vs 80）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KsuidGenerator gen = KsuidGenerator.create();
 * String ksuid = gen.generate();
 * // -> "0ujtsYcgvSTl8PAuAdqWYSMnLOv"
 *
 * // Get raw bytes
 * byte[] bytes = gen.generateBytes();
 *
 * // Parse and extract timestamp
 * Instant time = KsuidGenerator.extractTimestamp(ksuid);
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
public final class KsuidGenerator implements IdGenerator<String> {

    /**
     * KSUID epoch: 2014-05-13 16:53:20 UTC (1400000000)
     * KSUID起始时间：2014-05-13 16:53:20 UTC
     */
    public static final long EPOCH_SECONDS = 1400000000L;

    /**
     * Total byte length
     */
    private static final int BYTE_LENGTH = 20;

    /**
     * Timestamp byte length
     */
    private static final int TIMESTAMP_LENGTH = 4;

    /**
     * Payload byte length
     */
    private static final int PAYLOAD_LENGTH = 16;

    /**
     * Encoded string length
     */
    private static final int STRING_LENGTH = 27;

    /**
     * Base62 alphabet
     */
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Base62 decoding table
     */
    private static final int[] BASE62_DECODE = new int[128];

    static {
        Arrays.fill(BASE62_DECODE, -1);
        for (int i = 0; i < BASE62.length(); i++) {
            BASE62_DECODE[BASE62.charAt(i)] = i;
        }
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final KsuidGenerator INSTANCE = new KsuidGenerator();

    private KsuidGenerator() {
    }

    /**
     * Creates a KSUID generator
     * 创建KSUID生成器
     *
     * @return generator | 生成器
     */
    public static KsuidGenerator create() {
        return INSTANCE;
    }

    @Override
    public String generate() {
        return encode(generateBytes());
    }

    /**
     * Generates a KSUID with specific timestamp
     * 使用指定时间戳生成KSUID
     *
     * @param timestamp the timestamp in seconds since Unix epoch | Unix纪元以来的秒数
     * @return KSUID string (27 characters) | KSUID字符串（27字符）
     */
    public String generate(long timestamp) {
        return encode(generateBytes(timestamp));
    }

    /**
     * Generates KSUID as raw bytes
     * 生成KSUID原始字节
     *
     * @return 20-byte array | 20字节数组
     */
    public byte[] generateBytes() {
        return generateBytes(System.currentTimeMillis() / 1000);
    }

    /**
     * Generates KSUID as raw bytes with specific timestamp
     * 使用指定时间戳生成KSUID原始字节
     *
     * @param timestamp the timestamp in seconds since Unix epoch | Unix纪元以来的秒数
     * @return 20-byte array | 20字节数组
     */
    public byte[] generateBytes(long timestamp) {
        byte[] bytes = new byte[BYTE_LENGTH];

        // Timestamp (4 bytes, big-endian, relative to KSUID epoch)
        long ksuidTimestamp = timestamp - EPOCH_SECONDS;
        bytes[0] = (byte) (ksuidTimestamp >>> 24);
        bytes[1] = (byte) (ksuidTimestamp >>> 16);
        bytes[2] = (byte) (ksuidTimestamp >>> 8);
        bytes[3] = (byte) ksuidTimestamp;

        // Random payload (16 bytes)
        byte[] payload = new byte[PAYLOAD_LENGTH];
        RANDOM.nextBytes(payload);
        System.arraycopy(payload, 0, bytes, TIMESTAMP_LENGTH, PAYLOAD_LENGTH);

        return bytes;
    }

    /**
     * Encodes raw bytes to KSUID string
     * 将原始字节编码为KSUID字符串
     *
     * @param bytes the 20-byte array | 20字节数组
     * @return KSUID string (27 characters) | KSUID字符串（27字符）
     */
    public static String encode(byte[] bytes) {
        if (bytes == null || bytes.length != BYTE_LENGTH) {
            throw new IllegalArgumentException("KSUID bytes must be exactly 20 bytes");
        }

        // Convert bytes to a big integer representation and then to Base62
        // Using a simplified approach that handles the byte array as a big number
        StringBuilder result = new StringBuilder(STRING_LENGTH);

        // Create a copy to avoid modifying original
        byte[] work = bytes.clone();

        for (int i = 0; i < STRING_LENGTH; i++) {
            int remainder = 0;
            for (int j = 0; j < work.length; j++) {
                int digit = (work[j] & 0xFF) + remainder * 256;
                work[j] = (byte) (digit / 62);
                remainder = digit % 62;
            }
            result.append(BASE62.charAt(remainder));
        }

        // Reverse because we computed from least significant digit
        return result.reverse().toString();
    }

    /**
     * Decodes a KSUID string to raw bytes
     * 将KSUID字符串解码为原始字节
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return 20-byte array | 20字节数组
     * @throws IllegalArgumentException if the string is invalid
     */
    public static byte[] decode(String ksuid) {
        if (ksuid == null || ksuid.length() != STRING_LENGTH) {
            throw new IllegalArgumentException("KSUID string must be exactly 27 characters");
        }

        byte[] bytes = new byte[BYTE_LENGTH];

        for (int i = 0; i < STRING_LENGTH; i++) {
            char c = ksuid.charAt(i);
            if (c >= 128 || BASE62_DECODE[c] < 0) {
                throw new IllegalArgumentException("Invalid KSUID character: " + c);
            }
            int carry = BASE62_DECODE[c];
            for (int j = BYTE_LENGTH - 1; j >= 0; j--) {
                int value = (bytes[j] & 0xFF) * 62 + carry;
                bytes[j] = (byte) value;
                carry = value >>> 8;
            }
        }

        return bytes;
    }

    /**
     * Extracts the timestamp from a KSUID string
     * 从KSUID字符串提取时间戳
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return the timestamp instant | 时间戳
     */
    public static Instant extractTimestamp(String ksuid) {
        byte[] bytes = decode(ksuid);
        return extractTimestamp(bytes);
    }

    /**
     * Extracts the timestamp from raw KSUID bytes
     * 从KSUID原始字节提取时间戳
     *
     * @param bytes the 20-byte array | 20字节数组
     * @return the timestamp instant | 时间戳
     */
    public static Instant extractTimestamp(byte[] bytes) {
        if (bytes == null || bytes.length != BYTE_LENGTH) {
            throw new IllegalArgumentException("KSUID bytes must be exactly 20 bytes");
        }
        long ksuidTimestamp = ((bytes[0] & 0xFFL) << 24)
                | ((bytes[1] & 0xFFL) << 16)
                | ((bytes[2] & 0xFFL) << 8)
                | (bytes[3] & 0xFFL);
        long unixTimestamp = ksuidTimestamp + EPOCH_SECONDS;
        return Instant.ofEpochSecond(unixTimestamp);
    }

    /**
     * Extracts the random payload from a KSUID string
     * 从KSUID字符串提取随机负载
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return 16-byte payload | 16字节负载
     */
    public static byte[] extractPayload(String ksuid) {
        byte[] bytes = decode(ksuid);
        byte[] payload = new byte[PAYLOAD_LENGTH];
        System.arraycopy(bytes, TIMESTAMP_LENGTH, payload, 0, PAYLOAD_LENGTH);
        return payload;
    }

    /**
     * Validates a KSUID string
     * 验证KSUID字符串
     *
     * @param ksuid the KSUID string | KSUID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String ksuid) {
        if (ksuid == null || ksuid.length() != STRING_LENGTH) {
            return false;
        }
        for (char c : ksuid.toCharArray()) {
            if (c >= 128 || BASE62_DECODE[c] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two KSUID strings
     * 比较两个KSUID字符串
     *
     * @param ksuid1 first KSUID | 第一个KSUID
     * @param ksuid2 second KSUID | 第二个KSUID
     * @return comparison result (negative if ksuid1 < ksuid2) | 比较结果
     */
    public static int compare(String ksuid1, String ksuid2) {
        return ksuid1.compareTo(ksuid2);
    }

    /**
     * Returns the minimum possible KSUID (all zeros)
     * 返回最小可能的KSUID（全零）
     *
     * @return minimum KSUID string | 最小KSUID字符串
     */
    public static String min() {
        return "000000000000000000000000000";
    }

    /**
     * Returns the maximum possible KSUID (all max values)
     * 返回最大可能的KSUID（全最大值）
     *
     * @return maximum KSUID string | 最大KSUID字符串
     */
    public static String max() {
        return "aWgEPTl1tmebfsQzFP4bxwgy80V";
    }

    @Override
    public String getType() {
        return "KSUID";
    }
}
