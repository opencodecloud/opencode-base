
package cloud.opencode.base.serialization;

/**
 * FormatDetector - Serialization Format Auto-Detection Utility
 * 序列化格式自动检测工具类
 *
 * <p>Detects the serialization format of byte array data by inspecting header byte patterns.
 * Supports detection of JSON, XML, and Protobuf formats.</p>
 * <p>通过检查头部字节模式来检测字节数组数据的序列化格式。
 * 支持检测 JSON、XML 和 Protobuf 格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JSON detection (starts with '{', '[', '"' or whitespace before these) - JSON 检测</li>
 *   <li>XML detection (starts with '&lt;' or BOM + '&lt;') - XML 检测</li>
 *   <li>Protobuf detection (wire type pattern analysis) - Protobuf 检测（wire type 模式分析）</li>
 *   <li>Returns "unknown" for unrecognized formats - 无法识别的格式返回 "unknown"</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] data = ...; // some serialized data
 *
 * String format = FormatDetector.detect(data);  // "json", "xml", "protobuf", or "unknown"
 * boolean json = FormatDetector.isJson(data);    // true/false
 * boolean xml  = FormatDetector.isXml(data);     // true/false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
public final class FormatDetector {

    /** JSON format name | JSON 格式名称 */
    public static final String FORMAT_JSON = "json";

    /** XML format name | XML 格式名称 */
    public static final String FORMAT_XML = "xml";

    /** Protobuf format name | Protobuf 格式名称 */
    public static final String FORMAT_PROTOBUF = "protobuf";

    /** Unknown format name | 未知格式名称 */
    public static final String FORMAT_UNKNOWN = "unknown";

    /** UTF-8 BOM bytes | UTF-8 BOM 字节 */
    private static final byte BOM_EF = (byte) 0xEF;
    private static final byte BOM_BB = (byte) 0xBB;
    private static final byte BOM_BF = (byte) 0xBF;

    private FormatDetector() {
        // Utility class, no instantiation | 工具类，不可实例化
    }

    /**
     * Detects the serialization format of the given data.
     * 检测给定数据的序列化格式。
     *
     * <p>Detection is based on header byte analysis:</p>
     * <ul>
     *   <li>JSON: first non-whitespace byte is '{', '[', or '"'</li>
     *   <li>XML: first non-whitespace byte is '&lt;' (with optional BOM)</li>
     *   <li>Protobuf: valid wire type in first byte's lower 3 bits</li>
     *   <li>Otherwise: "unknown"</li>
     * </ul>
     *
     * @param data the data to inspect | 要检测的数据
     * @return the detected format name ("json", "xml", "protobuf", or "unknown") | 检测到的格式名称
     */
    public static String detect(byte[] data) {
        if (data == null || data.length == 0) {
            return FORMAT_UNKNOWN;
        }

        if (isJson(data)) {
            return FORMAT_JSON;
        }
        if (isXml(data)) {
            return FORMAT_XML;
        }
        if (isProtobuf(data)) {
            return FORMAT_PROTOBUF;
        }
        return FORMAT_UNKNOWN;
    }

    /**
     * Checks if the data appears to be JSON format.
     * 检查数据是否为 JSON 格式。
     *
     * <p>JSON data starts with '{', '[', or '"' (possibly preceded by whitespace or BOM).</p>
     * <p>JSON 数据以 '{'、'[' 或 '"' 开头（可能前面有空白字符或 BOM）。</p>
     *
     * @param data the data to check | 要检查的数据
     * @return true if the data appears to be JSON | 如果数据看起来是 JSON 则返回 true
     */
    public static boolean isJson(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        int offset = skipBomAndWhitespace(data);
        if (offset >= data.length) {
            return false;
        }
        byte b = data[offset];
        return b == '{' || b == '[' || b == '"';
    }

    /**
     * Checks if the data appears to be XML format.
     * 检查数据是否为 XML 格式。
     *
     * <p>XML data starts with '&lt;' (possibly preceded by BOM or whitespace).</p>
     * <p>XML 数据以 '&lt;' 开头（可能前面有 BOM 或空白字符）。</p>
     *
     * @param data the data to check | 要检查的数据
     * @return true if the data appears to be XML | 如果数据看起来是 XML 则返回 true
     */
    public static boolean isXml(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        int offset = skipBomAndWhitespace(data);
        if (offset >= data.length) {
            return false;
        }
        return data[offset] == '<';
    }

    /**
     * Checks if the data appears to be Protobuf format.
     * 检查数据是否为 Protobuf 格式。
     *
     * <p>Protobuf uses wire types in the lower 3 bits of tag bytes.
     * Valid wire types are 0 (varint), 1 (64-bit), 2 (length-delimited),
     * 5 (32-bit). Wire types 3 and 4 (groups) are deprecated.
     * The field number must be in the typical range (1-15 for single-byte tags).
     * At least 2 bytes are required for a meaningful detection, and wire type 2
     * (length-delimited) is further validated by checking the length prefix.</p>
     * <p>Protobuf 在标签字节的低 3 位使用 wire type。
     * 有效的 wire type 为 0（varint）、1（64 位）、2（长度分隔）、5（32 位）。
     * 字段编号必须在典型范围内（单字节标签为 1-15）。
     * 至少需要 2 个字节才能进行有意义的检测。</p>
     *
     * @param data the data to check | 要检查的数据
     * @return true if the data appears to be Protobuf | 如果数据看起来是 Protobuf 则返回 true
     */
    private static boolean isProtobuf(byte[] data) {
        if (data.length < 2) {
            return false;
        }
        int first = data[0] & 0xFF;
        int wireType = first & 0x07;
        int fieldNumber = first >>> 3;

        // Only detect protobuf for the most common pattern: small field numbers with
        // wire type 2 (length-delimited), which covers string/bytes/embedded message fields.
        // This avoids false positives with random binary and text data.
        if (fieldNumber < 1 || fieldNumber > 5) {
            return false;
        }
        if (wireType != 2) {
            return false;
        }

        // Verify the length prefix is reasonable and consistent with data size
        int length = data[1] & 0xFF;
        if ((length & 0x80) == 0) {
            // Simple single-byte length: must be positive and fit within data
            return length > 0 && length <= data.length - 2;
        }

        // Multi-byte varint length: at minimum the structure looks plausible
        return data.length >= 3;
    }

    /**
     * Skips BOM (Byte Order Mark) and whitespace characters.
     * 跳过 BOM（字节顺序标记）和空白字符。
     *
     * @param data the data | 数据
     * @return the index of the first non-BOM, non-whitespace byte | 第一个非 BOM、非空白字节的索引
     */
    private static int skipBomAndWhitespace(byte[] data) {
        int offset = 0;

        // Skip UTF-8 BOM if present
        if (data.length >= 3
                && data[0] == BOM_EF
                && data[1] == BOM_BB
                && data[2] == BOM_BF) {
            offset = 3;
        }

        // Skip whitespace
        while (offset < data.length) {
            byte b = data[offset];
            if (b == ' ' || b == '\t' || b == '\n' || b == '\r') {
                offset++;
            } else {
                break;
            }
        }

        return offset;
    }
}
