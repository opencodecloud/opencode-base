package cloud.opencode.base.id;

import cloud.opencode.base.id.ulid.UlidGenerator;
import cloud.opencode.base.id.uuid.OpenUuid;

import java.util.UUID;

/**
 * ID Format Converter Utility
 * ID格式转换工具
 *
 * <p>Provides utilities for converting IDs between different formats.
 * Useful for generating short URLs, API paths, and format interoperability.</p>
 * <p>提供ID在不同格式之间转换的工具。
 * 适用于生成短链接、API路径和格式互操作。</p>
 *
 * <p><strong>Supported Conversions | 支持的转换:</strong></p>
 * <ul>
 *   <li>Long ID ↔ Base62/Base36 string</li>
 *   <li>ULID ↔ UUID interconversion</li>
 *   <li>UUID ↔ Base62 string</li>
 *   <li>Snowflake ID ↔ Base62 string</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Long to Base62
 * String shortId = IdConverter.toBase62(snowflakeId);
 * long id = IdConverter.fromBase62(shortId);
 *
 * // ULID to UUID
 * UUID uuid = IdConverter.ulidToUuid(ulid);
 * String ulid = IdConverter.uuidToUlid(uuid);
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ID format conversion between different representations - 不同表示形式之间的ID格式转换</li>
 *   <li>Base62/Base36 encoding and decoding - Base62/Base36编码和解码</li>
 *   <li>Numeric to string ID conversion - 数值到字符串ID转换</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(log₆₂ n) for toBase62/fromBase62/toBase36/fromBase36 where n=numeric value; O(1) for ulidToUuid/uuidToUlid (fixed 16-byte/26-char structures); O(b) for encodeBytesToBase62/decodeBase62ToBytes where b=byte count (22 iterations × 16 bytes each) - 时间复杂度: toBase62/fromBase62/toBase36/fromBase36 为 O(log₆₂ n)；ulidToUuid/uuidToUlid 为 O(1)（固定结构）；encodeBytesToBase62/decodeBase62ToBytes 为 O(b)</li>
 *   <li>Space complexity: O(1) - output strings are of bounded fixed length - 空间复杂度: O(1) - 输出字符串长度有界固定</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class IdConverter {

    /**
     * Base62 alphabet (0-9, A-Z, a-z)
     */
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Base36 alphabet (0-9, a-z)
     */
    private static final String BASE36_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";

    /**
     * Crockford's Base32 alphabet (used by ULID)
     */
    private static final String BASE32_CROCKFORD = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";

    /**
     * Base58 alphabet (Bitcoin-style: no 0, O, I, l — avoids visually ambiguous characters)
     * Base58字母表（比特币风格：无0、O、I、l — 避免视觉歧义字符）
     */
    private static final String BASE58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * Base62 decode table
     */
    private static final int[] BASE62_DECODE = new int[128];

    /**
     * Base36 decode table
     */
    private static final int[] BASE36_DECODE = new int[128];

    /**
     * Base32 Crockford decode table
     */
    private static final int[] BASE32_DECODE = new int[128];

    /**
     * Base58 decode table
     */
    private static final int[] BASE58_DECODE = new int[128];

    static {
        // Initialize Base62 decode
        for (int i = 0; i < 128; i++) {
            BASE62_DECODE[i] = -1;
            BASE36_DECODE[i] = -1;
            BASE32_DECODE[i] = -1;
        }
        for (int i = 0; i < BASE62_CHARS.length(); i++) {
            BASE62_DECODE[BASE62_CHARS.charAt(i)] = i;
        }
        for (int i = 0; i < BASE36_CHARS.length(); i++) {
            BASE36_DECODE[BASE36_CHARS.charAt(i)] = i;
            BASE36_DECODE[Character.toUpperCase(BASE36_CHARS.charAt(i))] = i;
        }
        for (int i = 0; i < BASE32_CROCKFORD.length(); i++) {
            BASE32_DECODE[BASE32_CROCKFORD.charAt(i)] = i;
            BASE32_DECODE[Character.toLowerCase(BASE32_CROCKFORD.charAt(i))] = i;
        }
        // Handle ambiguous characters for Crockford Base32
        BASE32_DECODE['O'] = BASE32_DECODE['o'] = 0;
        BASE32_DECODE['I'] = BASE32_DECODE['i'] = 1;
        BASE32_DECODE['L'] = BASE32_DECODE['l'] = 1;

        // Initialize Base58 decode
        for (int i = 0; i < 128; i++) {
            BASE58_DECODE[i] = -1;
        }
        for (int i = 0; i < BASE58_CHARS.length(); i++) {
            BASE58_DECODE[BASE58_CHARS.charAt(i)] = i;
        }
    }

    private IdConverter() {
    }

    // ==================== Base62 ====================

    /**
     * Converts a long ID to Base62 string
     * 将长整型ID转换为Base62字符串
     *
     * @param id the ID | ID
     * @return Base62 string | Base62字符串
     */
    public static String toBase62(long id) {
        if (id == 0) {
            return "0";
        }
        boolean negative = id < 0;
        StringBuilder sb = new StringBuilder();
        if (negative) {
            // Negate safely: -Long.MIN_VALUE overflows back to Long.MIN_VALUE,
            // so use unsigned division for that case.
            if (id == Long.MIN_VALUE) {
                long abs = Long.MIN_VALUE;
                while (abs != 0) {
                    int remainder = (int) Long.remainderUnsigned(abs, 62);
                    sb.append(BASE62_CHARS.charAt(remainder));
                    abs = Long.divideUnsigned(abs, 62);
                }
            } else {
                long abs = -id;
                while (abs > 0) {
                    sb.append(BASE62_CHARS.charAt((int) (abs % 62)));
                    abs /= 62;
                }
            }
            sb.append('-');
        } else {
            while (id > 0) {
                sb.append(BASE62_CHARS.charAt((int) (id % 62)));
                id /= 62;
            }
        }
        return sb.reverse().toString();
    }

    /**
     * Converts a Base62 string back to long ID
     * 将Base62字符串转换回长整型ID
     *
     * @param base62 the Base62 string | Base62字符串
     * @return the ID | ID
     * @throws IllegalArgumentException if the string is invalid
     */
    public static long fromBase62(String base62) {
        if (base62 == null || base62.isEmpty()) {
            throw new IllegalArgumentException("Base62 string cannot be null or empty");
        }
        boolean negative = base62.charAt(0) == '-';
        int startIndex = negative ? 1 : 0;

        long result = 0;
        for (int i = startIndex; i < base62.length(); i++) {
            char c = base62.charAt(i);
            if (c >= 128 || BASE62_DECODE[c] < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            result = result * 62 + BASE62_DECODE[c];
        }
        return negative ? -result : result;
    }

    // ==================== Base36 ====================

    /**
     * Converts a long ID to Base36 string (case-insensitive)
     * 将长整型ID转换为Base36字符串（大小写不敏感）
     *
     * @param id the ID | ID
     * @return Base36 string | Base36字符串
     */
    public static String toBase36(long id) {
        return Long.toString(id, 36);
    }

    /**
     * Converts a Base36 string back to long ID
     * 将Base36字符串转换回长整型ID
     *
     * @param base36 the Base36 string | Base36字符串
     * @return the ID | ID
     * @throws NumberFormatException if the string is invalid
     */
    public static long fromBase36(String base36) {
        return Long.parseLong(base36, 36);
    }

    // ==================== UUID / ULID ====================

    /**
     * Converts a ULID string to UUID
     * 将ULID字符串转换为UUID
     *
     * <p>Both ULID and UUID are 128-bit, so the conversion is lossless.</p>
     * <p>ULID和UUID都是128位，因此转换是无损的。</p>
     *
     * @param ulid the ULID string (26 characters) | ULID字符串（26字符）
     * @return UUID | UUID
     * @throws IllegalArgumentException if the ULID is invalid
     */
    public static UUID ulidToUuid(String ulid) {
        if (!UlidGenerator.isValid(ulid)) {
            throw new IllegalArgumentException("Invalid ULID: " + ulid);
        }
        // Decode ULID to bytes
        byte[] bytes = decodeUlidToBytes(ulid);
        // Convert to UUID
        return OpenUuid.fromBytes(bytes);
    }

    /**
     * Converts a UUID to ULID string
     * 将UUID转换为ULID字符串
     *
     * @param uuid the UUID | UUID
     * @return ULID string (26 characters) | ULID字符串（26字符）
     */
    public static String uuidToUlid(UUID uuid) {
        byte[] bytes = OpenUuid.toBytes(uuid);
        return encodeUlidFromBytes(bytes);
    }

    // ==================== Snowflake ====================

    /**
     * Converts a Snowflake ID to Base62 string (shorter representation)
     * 将雪花ID转换为Base62字符串（更短的表示）
     *
     * @param snowflakeId the Snowflake ID | 雪花ID
     * @return Base62 string (typically 10-11 characters) | Base62字符串（通常10-11字符）
     */
    public static String snowflakeToBase62(long snowflakeId) {
        return toBase62(snowflakeId);
    }

    /**
     * Converts a Base62 string back to Snowflake ID
     * 将Base62字符串转换回雪花ID
     *
     * @param base62 the Base62 string | Base62字符串
     * @return Snowflake ID | 雪花ID
     */
    public static long base62ToSnowflake(String base62) {
        return fromBase62(base62);
    }

    // ==================== UUID Base62 ====================

    /**
     * Converts a UUID to Base62 string (compact representation)
     * 将UUID转换为Base62字符串（紧凑表示）
     *
     * @param uuid the UUID | UUID
     * @return Base62 string (22 characters) | Base62字符串（22字符）
     */
    public static String uuidToBase62(UUID uuid) {
        byte[] bytes = OpenUuid.toBytes(uuid);
        return encodeBytesToBase62(bytes);
    }

    /**
     * Converts a Base62 string back to UUID
     * 将Base62字符串转换回UUID
     *
     * @param base62 the Base62 string (22 characters) | Base62字符串（22字符）
     * @return UUID | UUID
     */
    public static UUID base62ToUuid(String base62) {
        byte[] bytes = decodeBase62ToBytes(base62, 16);
        return OpenUuid.fromBytes(bytes);
    }

    // ==================== Validation ====================

    /**
     * Validates a Base62 string
     * 验证Base62字符串
     *
     * @param str the string to validate | 要验证的字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidBase62(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int start = str.charAt(0) == '-' ? 1 : 0;
        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 128 || BASE62_DECODE[c] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates a Base36 string
     * 验证Base36字符串
     *
     * @param str the string to validate | 要验证的字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidBase36(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int start = str.charAt(0) == '-' ? 1 : 0;
        for (int i = start; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 128 || BASE36_DECODE[c] < 0) {
                return false;
            }
        }
        return true;
    }

    // ==================== Base58 ====================

    /**
     * Converts a long ID to Base58 string (Bitcoin-style alphabet, no ambiguous characters)
     * 将长整型ID转换为Base58字符串（比特币风格字母表，无歧义字符）
     *
     * <p>Base58 avoids visually ambiguous characters (0, O, I, l) making it
     * suitable for human-readable short IDs in URLs and QR codes.</p>
     * <p>Base58避免视觉上模糊的字符（0、O、I、l），适合在URL和QR码中使用人类可读的短ID。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toBase58(0L)          = "1"  // zero maps to '1' (Bitcoin convention)
     * toBase58(57L)         = "z"
     * toBase58(58L)         = "21"
     * toBase58(1000000000L) = "2QGPK"
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(log₅₈ n), Space: O(1)</p>
     * <p>时间: O(log₅₈ n), 空间: O(1)</p>
     *
     * @param id the ID (treated as unsigned long) | ID（视为无符号长整型）
     * @return Base58 encoded string | Base58编码字符串
     */
    public static String toBase58(long id) {
        if (id == 0) {
            return "1";
        }
        StringBuilder sb = new StringBuilder();
        long remaining = id;
        // Use unsigned arithmetic to handle the full long range
        while (Long.compareUnsigned(remaining, 0L) != 0) {
            int rem = (int) Long.remainderUnsigned(remaining, 58);
            sb.append(BASE58_CHARS.charAt(rem));
            remaining = Long.divideUnsigned(remaining, 58);
        }
        return sb.reverse().toString();
    }

    /**
     * Converts a Base58 string back to long ID
     * 将Base58字符串转换回长整型ID
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * fromBase58("1")       = 0L
     * fromBase58("2QGPK")   = 1000000000L
     * fromBase58("21")      = 58L
     * </pre>
     *
     * <p><strong>Performance | 性能:</strong></p>
     * <p>Time: O(n) where n=string length, Space: O(1)</p>
     *
     * @param base58 the Base58 encoded string | Base58编码字符串
     * @return the decoded ID | 解码的ID
     * @throws IllegalArgumentException if the string is null, empty, or contains invalid characters | 字符串为null、空或含无效字符时抛出
     */
    public static long fromBase58(String base58) {
        if (base58 == null || base58.isEmpty()) {
            throw new IllegalArgumentException("Base58 string cannot be null or empty");
        }
        long result = 0;
        for (int i = 0; i < base58.length(); i++) {
            char c = base58.charAt(i);
            if (c >= 128 || BASE58_DECODE[c] < 0) {
                throw new IllegalArgumentException("Invalid Base58 character: '" + c + "'");
            }
            try {
                result = Math.addExact(Math.multiplyExact(result, 58), BASE58_DECODE[c]);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Base58 value overflows long range: " + base58);
            }
        }
        return result;
    }

    /**
     * Validates whether a string is valid Base58
     * 验证字符串是否是有效的Base58
     *
     * @param str the string to validate | 要验证的字符串
     * @return true if the string is a valid Base58 value | 如果是有效Base58字符串则返回true
     */
    public static boolean isValidBase58(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 128 || BASE58_DECODE[c] < 0) {
                return false;
            }
        }
        return true;
    }

    // ==================== Internal Methods ====================

    private static byte[] decodeUlidToBytes(String ulid) {
        byte[] bytes = new byte[16];

        // Decode timestamp (first 10 chars -> 48 bits -> 6 bytes)
        long timestamp = 0;
        for (int i = 0; i < 10; i++) {
            char c = ulid.charAt(i);
            int value = BASE32_DECODE[c];
            timestamp = (timestamp << 5) | value;
        }
        bytes[0] = (byte) (timestamp >>> 40);
        bytes[1] = (byte) (timestamp >>> 32);
        bytes[2] = (byte) (timestamp >>> 24);
        bytes[3] = (byte) (timestamp >>> 16);
        bytes[4] = (byte) (timestamp >>> 8);
        bytes[5] = (byte) timestamp;

        // Decode randomness (remaining 16 chars -> 80 bits -> 10 bytes)
        // Process in groups
        int byteIndex = 6;
        int bitBuffer = 0;
        int bitsInBuffer = 0;

        for (int i = 10; i < 26; i++) {
            char c = ulid.charAt(i);
            int value = BASE32_DECODE[c];
            bitBuffer = (bitBuffer << 5) | value;
            bitsInBuffer += 5;

            while (bitsInBuffer >= 8) {
                bitsInBuffer -= 8;
                bytes[byteIndex++] = (byte) (bitBuffer >>> bitsInBuffer);
                bitBuffer &= (1 << bitsInBuffer) - 1;
            }
        }

        return bytes;
    }

    private static String encodeUlidFromBytes(byte[] bytes) {
        char[] chars = new char[26];

        // Encode timestamp (6 bytes -> 48 bits -> 10 chars)
        long timestamp = ((bytes[0] & 0xFFL) << 40)
                | ((bytes[1] & 0xFFL) << 32)
                | ((bytes[2] & 0xFFL) << 24)
                | ((bytes[3] & 0xFFL) << 16)
                | ((bytes[4] & 0xFFL) << 8)
                | (bytes[5] & 0xFFL);

        chars[0] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 45) & 0x1F));
        chars[1] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 40) & 0x1F));
        chars[2] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 35) & 0x1F));
        chars[3] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 30) & 0x1F));
        chars[4] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 25) & 0x1F));
        chars[5] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 20) & 0x1F));
        chars[6] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 15) & 0x1F));
        chars[7] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 10) & 0x1F));
        chars[8] = BASE32_CROCKFORD.charAt((int) ((timestamp >>> 5) & 0x1F));
        chars[9] = BASE32_CROCKFORD.charAt((int) (timestamp & 0x1F));

        // Encode randomness (10 bytes -> 80 bits -> 16 chars)
        chars[10] = BASE32_CROCKFORD.charAt((bytes[6] >>> 3) & 0x1F);
        chars[11] = BASE32_CROCKFORD.charAt(((bytes[6] << 2) | ((bytes[7] & 0xFF) >>> 6)) & 0x1F);
        chars[12] = BASE32_CROCKFORD.charAt((bytes[7] >>> 1) & 0x1F);
        chars[13] = BASE32_CROCKFORD.charAt(((bytes[7] << 4) | ((bytes[8] & 0xFF) >>> 4)) & 0x1F);
        chars[14] = BASE32_CROCKFORD.charAt(((bytes[8] << 1) | ((bytes[9] & 0xFF) >>> 7)) & 0x1F);
        chars[15] = BASE32_CROCKFORD.charAt((bytes[9] >>> 2) & 0x1F);
        chars[16] = BASE32_CROCKFORD.charAt(((bytes[9] << 3) | ((bytes[10] & 0xFF) >>> 5)) & 0x1F);
        chars[17] = BASE32_CROCKFORD.charAt(bytes[10] & 0x1F);
        chars[18] = BASE32_CROCKFORD.charAt((bytes[11] >>> 3) & 0x1F);
        chars[19] = BASE32_CROCKFORD.charAt(((bytes[11] << 2) | ((bytes[12] & 0xFF) >>> 6)) & 0x1F);
        chars[20] = BASE32_CROCKFORD.charAt((bytes[12] >>> 1) & 0x1F);
        chars[21] = BASE32_CROCKFORD.charAt(((bytes[12] << 4) | ((bytes[13] & 0xFF) >>> 4)) & 0x1F);
        chars[22] = BASE32_CROCKFORD.charAt(((bytes[13] << 1) | ((bytes[14] & 0xFF) >>> 7)) & 0x1F);
        chars[23] = BASE32_CROCKFORD.charAt((bytes[14] >>> 2) & 0x1F);
        chars[24] = BASE32_CROCKFORD.charAt(((bytes[14] << 3) | ((bytes[15] & 0xFF) >>> 5)) & 0x1F);
        chars[25] = BASE32_CROCKFORD.charAt(bytes[15] & 0x1F);

        return new String(chars);
    }

    private static String encodeBytesToBase62(byte[] bytes) {
        StringBuilder result = new StringBuilder(22);
        byte[] work = bytes.clone();

        for (int i = 0; i < 22; i++) {
            int remainder = 0;
            for (int j = 0; j < work.length; j++) {
                int digit = (work[j] & 0xFF) + remainder * 256;
                work[j] = (byte) (digit / 62);
                remainder = digit % 62;
            }
            result.append(BASE62_CHARS.charAt(remainder));
        }

        return result.reverse().toString();
    }

    private static byte[] decodeBase62ToBytes(String base62, int byteLength) {
        byte[] bytes = new byte[byteLength];

        for (int i = 0; i < base62.length(); i++) {
            char c = base62.charAt(i);
            if (c >= 128 || BASE62_DECODE[c] < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + c);
            }
            int carry = BASE62_DECODE[c];
            for (int j = byteLength - 1; j >= 0; j--) {
                int value = (bytes[j] & 0xFF) * 62 + carry;
                bytes[j] = (byte) value;
                carry = value >>> 8;
            }
        }

        return bytes;
    }
}
