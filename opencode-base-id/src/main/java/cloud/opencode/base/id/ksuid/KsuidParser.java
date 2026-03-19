package cloud.opencode.base.id.ksuid;

import cloud.opencode.base.id.IdParser;

import java.time.Instant;

/**
 * KSUID Parser
 * KSUID解析器
 *
 * <p>Parses KSUID strings and extracts components.</p>
 * <p>解析KSUID字符串并提取组成部分。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KsuidParser parser = KsuidParser.create();
 * KsuidParser.ParsedKsuid parsed = parser.parse("0ujtsYcgvSTl8PAuAdqWYSMnLOv");
 *
 * Instant time = parsed.time();
 * byte[] payload = parsed.payload();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse KSUID strings to extract timestamp and payload - 解析KSUID字符串提取时间戳和负载</li>
 *   <li>Validate KSUID format and structure - 验证KSUID格式和结构</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - parse() decodes a fixed 27-character Base62 string into a 20-byte structure (4-byte timestamp + 16-byte payload) - 时间复杂度: O(1) - parse() 将固定 27 字符的 Base62 字符串解码为 20 字节结构（4 字节时间戳 + 16 字节负载）</li>
 *   <li>Space complexity: O(1) - produces a fixed-size ParsedKsuid record with a 16-byte payload array - 空间复杂度: O(1) - 生成包含 16 字节负载数组的固定大小 ParsedKsuid 记录</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class KsuidParser implements IdParser<String, KsuidParser.ParsedKsuid> {

    private static final KsuidParser INSTANCE = new KsuidParser();

    private KsuidParser() {
    }

    /**
     * Creates a KSUID parser
     * 创建KSUID解析器
     *
     * @return parser | 解析器
     */
    public static KsuidParser create() {
        return INSTANCE;
    }

    @Override
    public ParsedKsuid parse(String ksuid) {
        if (!isValid(ksuid)) {
            throw new IllegalArgumentException("Invalid KSUID: " + ksuid);
        }
        byte[] bytes = KsuidGenerator.decode(ksuid);
        Instant time = KsuidGenerator.extractTimestamp(bytes);
        byte[] payload = new byte[16];
        System.arraycopy(bytes, 4, payload, 0, 16);
        return new ParsedKsuid(ksuid, bytes, time, payload);
    }

    @Override
    public Instant extractTimestamp(String ksuid) {
        return KsuidGenerator.extractTimestamp(ksuid);
    }

    @Override
    public boolean isValid(String ksuid) {
        return KsuidGenerator.isValid(ksuid);
    }

    /**
     * Parsed KSUID result
     * 解析后的KSUID结果
     *
     * @param ksuid   the original KSUID string | 原始KSUID字符串
     * @param bytes   the raw 20-byte representation | 原始20字节表示
     * @param time    the timestamp | 时间戳
     * @param payload the 16-byte random payload | 16字节随机负载
     */
    public record ParsedKsuid(
            String ksuid,
            byte[] bytes,
            Instant time,
            byte[] payload
    ) {
        /**
         * Compact canonical constructor that makes defensive copies of mutable byte arrays
         * to prevent callers from mutating internal state.
         * 紧凑规范构造函数，对可变字节数组进行防御性复制以防止调用者修改内部状态。
         */
        public ParsedKsuid {
            bytes = bytes.clone();
            payload = payload.clone();
        }

        /**
         * Returns a defensive copy of the raw bytes
         * 返回原始字节的防御性副本
         *
         * @return copy of bytes | 字节副本
         */
        @Override
        public byte[] bytes() {
            return bytes.clone();
        }

        /**
         * Returns a defensive copy of the payload
         * 返回负载的防御性副本
         *
         * @return copy of payload | 负载副本
         */
        @Override
        public byte[] payload() {
            return payload.clone();
        }

        /**
         * Gets the timestamp part as hex string
         * 获取时间戳部分的十六进制字符串
         *
         * @return hex string | 十六进制字符串
         */
        public String getTimestampHex() {
            return String.format("%02X%02X%02X%02X",
                    bytes[0], bytes[1], bytes[2], bytes[3]);
        }

        /**
         * Gets the payload as hex string
         * 获取负载的十六进制字符串
         *
         * @return hex string | 十六进制字符串
         */
        public String getPayloadHex() {
            StringBuilder sb = new StringBuilder(32);
            for (int i = 4; i < 20; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return String.format("ParsedKsuid{ksuid='%s', time=%s}", ksuid, time);
        }
    }
}
