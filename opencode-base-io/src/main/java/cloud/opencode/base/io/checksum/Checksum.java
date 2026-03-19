package cloud.opencode.base.io.checksum;

import java.util.HexFormat;
import java.util.Objects;

/**
 * Checksum Result
 * 校验和结果
 *
 * <p>Immutable record representing a checksum calculation result.
 * Contains algorithm name, raw bytes, and hex string representation.</p>
 * <p>表示校验和计算结果的不可变记录。
 * 包含算法名称、原始字节和十六进制字符串表示。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Algorithm identification - 算法标识</li>
 *   <li>Hex string format - 十六进制字符串格式</li>
 *   <li>Byte array access - 字节数组访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Checksum checksum = OpenChecksum.calculateMd5(path);
 * String hex = checksum.hex();
 * byte[] bytes = checksum.bytes();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record with defensive copy) - 线程安全: 是（不可变记录，使用防御性拷贝）</li>
 *   <li>Null-safe: No, all fields validated non-null - 空值安全: 否，所有字段验证非null</li>
 * </ul>
 *
 * @param algorithm the algorithm name | 算法名称
 * @param bytes     the checksum bytes | 校验和字节
 * @param hex       the hex string | 十六进制字符串
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
public record Checksum(String algorithm, byte[] bytes, String hex) {

    private static final HexFormat HEX_FORMAT = HexFormat.of().withLowerCase();

    /**
     * Creates a checksum from bytes
     * 从字节创建校验和
     *
     * @param algorithm the algorithm | 算法
     * @param bytes     the checksum bytes | 校验和字节
     */
    public Checksum(String algorithm, byte[] bytes) {
        this(algorithm, bytes.clone(), HEX_FORMAT.formatHex(bytes));
    }

    /**
     * Compact constructor for validation
     * 紧凑构造器用于验证
     */
    public Checksum {
        Objects.requireNonNull(algorithm, "Algorithm must not be null");
        Objects.requireNonNull(bytes, "Bytes must not be null");
        Objects.requireNonNull(hex, "Hex must not be null");
    }

    /**
     * Gets a copy of the checksum bytes
     * 获取校验和字节的副本
     *
     * @return byte array copy | 字节数组副本
     */
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /**
     * Checks if this checksum matches another
     * 检查此校验和是否与另一个匹配
     *
     * @param other the other checksum | 另一个校验和
     * @return true if match | 如果匹配返回true
     */
    public boolean matches(Checksum other) {
        if (other == null) {
            return false;
        }
        return hex.equalsIgnoreCase(other.hex);
    }

    /**
     * Checks if this checksum matches a hex string
     * 检查此校验和是否与十六进制字符串匹配
     *
     * @param hexString the hex string | 十六进制字符串
     * @return true if match | 如果匹配返回true
     */
    public boolean matches(String hexString) {
        if (hexString == null) {
            return false;
        }
        return hex.equalsIgnoreCase(hexString);
    }

    /**
     * Creates a checksum from hex string
     * 从十六进制字符串创建校验和
     *
     * @param algorithm the algorithm | 算法
     * @param hexString the hex string | 十六进制字符串
     * @return checksum | 校验和
     */
    public static Checksum fromHex(String algorithm, String hexString) {
        byte[] bytes = HEX_FORMAT.parseHex(hexString.toLowerCase());
        return new Checksum(algorithm, bytes, hexString.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Checksum checksum)) return false;
        return Objects.equals(algorithm, checksum.algorithm) &&
                java.util.Arrays.equals(bytes, checksum.bytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(algorithm);
        result = 31 * result + java.util.Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return algorithm + ":" + hex;
    }
}
