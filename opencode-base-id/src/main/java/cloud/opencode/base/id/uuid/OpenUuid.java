package cloud.opencode.base.id.uuid;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUID Utility Class
 * UUID工具类
 *
 * <p>Provides utility methods for UUID generation, conversion, and validation.</p>
 * <p>提供UUID生成、转换和验证的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>UUID generation (v4, v7) - UUID生成（v4, v7）</li>
 *   <li>String conversion - 字符串转换</li>
 *   <li>Byte array conversion - 字节数组转换</li>
 *   <li>Validation - 验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate UUIDs
 * UUID v4 = OpenUuid.randomUuid();
 * UUID v7 = OpenUuid.timeOrderedUuid();
 *
 * // Convert to/from simple string
 * String simple = OpenUuid.toSimpleString(uuid);
 * UUID parsed = OpenUuid.fromSimpleString(simple);
 *
 * // Convert to/from bytes
 * byte[] bytes = OpenUuid.toBytes(uuid);
 * UUID fromBytes = OpenUuid.fromBytes(bytes);
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
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class OpenUuid {

    private static final UuidV7Generator V7_GENERATOR = UuidV7Generator.create();
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private OpenUuid() {
    }

    // ==================== Generation | 生成 ====================

    /**
     * Generates a random UUID (v4)
     * 生成随机UUID（v4）
     *
     * @return UUID | UUID
     */
    public static UUID randomUuid() {
        return UUID.randomUUID();
    }

    /**
     * Generates a time-ordered UUID (v7)
     * 生成时间有序UUID（v7）
     *
     * @return UUID | UUID
     */
    public static UUID timeOrderedUuid() {
        return V7_GENERATOR.generate();
    }

    // ==================== String Conversion | 字符串转换 ====================

    /**
     * Converts UUID to simple string (no hyphens)
     * 将UUID转换为简化字符串（无连字符）
     *
     * @param uuid the UUID | UUID
     * @return simple string (32 chars) | 简化字符串（32字符）
     */
    public static String toSimpleString(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    /**
     * Parses a simple string to UUID
     * 将简化字符串解析为UUID
     *
     * @param simple the simple string | 简化字符串
     * @return UUID | UUID
     * @throws IllegalArgumentException if format is invalid | 如果格式无效
     */
    public static UUID fromSimpleString(String simple) {
        if (simple == null || simple.length() != 32) {
            throw new IllegalArgumentException("Simple UUID string must be 32 characters");
        }
        StringBuilder sb = new StringBuilder(36);
        sb.append(simple, 0, 8).append('-');
        sb.append(simple, 8, 12).append('-');
        sb.append(simple, 12, 16).append('-');
        sb.append(simple, 16, 20).append('-');
        sb.append(simple, 20, 32);
        return UUID.fromString(sb.toString());
    }

    // ==================== Byte Conversion | 字节转换 ====================

    /**
     * Converts UUID to byte array
     * 将UUID转换为字节数组
     *
     * @param uuid the UUID | UUID
     * @return 16-byte array | 16字节数组
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Parses byte array to UUID
     * 将字节数组解析为UUID
     *
     * @param bytes 16-byte array | 16字节数组
     * @return UUID | UUID
     * @throws IllegalArgumentException if byte array is invalid | 如果字节数组无效
     */
    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be 16 bytes");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long msb = buffer.getLong();
        long lsb = buffer.getLong();
        return new UUID(msb, lsb);
    }

    // ==================== Version & Validation | 版本与验证 ====================

    /**
     * Gets the UUID version
     * 获取UUID版本
     *
     * @param uuid the UUID | UUID
     * @return version number | 版本号
     */
    public static int getVersion(UUID uuid) {
        return uuid.version();
    }

    /**
     * Gets the UUID variant
     * 获取UUID变体
     *
     * @param uuid the UUID | UUID
     * @return variant number | 变体号
     */
    public static int getVariant(UUID uuid) {
        return uuid.variant();
    }

    /**
     * Validates a UUID string
     * 验证UUID字符串
     *
     * @param uuidStr the UUID string | UUID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String uuidStr) {
        if (uuidStr == null) {
            return false;
        }
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates a simple UUID string
     * 验证简化UUID字符串
     *
     * @param simple the simple UUID string | 简化UUID字符串
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidSimple(String simple) {
        if (simple == null || simple.length() != 32) {
            return false;
        }
        for (char c : simple.toCharArray()) {
            if (!isHexDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts timestamp from UUID v7
     * 从UUID v7提取时间戳
     *
     * @param uuid the UUID | UUID
     * @return timestamp in milliseconds, or -1 if not v7 | 时间戳（毫秒），如果不是v7返回-1
     */
    public static long extractTimestamp(UUID uuid) {
        if (uuid.version() != 7) {
            return -1;
        }
        return UuidV7Generator.extractTimestamp(uuid);
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
