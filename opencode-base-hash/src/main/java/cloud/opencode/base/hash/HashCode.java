package cloud.opencode.base.hash;

import cloud.opencode.base.hash.exception.OpenHashException;

import java.util.Arrays;
import java.util.HexFormat;

/**
 * Hash computation result wrapper
 * 哈希计算结果封装
 *
 * <p>Represents the result of a hash computation. Provides methods to
 * access the hash value in different formats including int, long, byte array,
 * and hexadecimal string.</p>
 * <p>表示哈希计算的结果。提供以不同格式访问哈希值的方法，
 * 包括int、long、字节数组和十六进制字符串。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple output formats - 多种输出格式</li>
 *   <li>Factory methods for creation - 创建工厂方法</li>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HashCode hash = OpenHash.murmur3_128().hashUtf8("Hello");
 * int intValue = hash.asInt();
 * long longValue = hash.asLong();
 * String hex = hash.toHex();
 * byte[] bytes = hash.asBytes();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = byte array length - O(n), n为字节数组长度</li>
 *   <li>Space complexity: O(n) for hash bytes - 哈希字节 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public abstract class HashCode {

    private static final HexFormat HEX = HexFormat.of();

    HashCode() {
    }

    /**
     * Gets the number of bits in this hash
     * 获取此哈希的位数
     *
     * @return number of bits | 位数
     */
    public abstract int bits();

    /**
     * Converts to int (uses lower 32 bits)
     * 转换为int（使用低32位）
     *
     * @return int value | int值
     */
    public abstract int asInt();

    /**
     * Converts to long (uses lower 64 bits)
     * 转换为long（使用低64位）
     *
     * @return long value | long值
     * @throws IllegalStateException if hash is less than 64 bits | 如果哈希少于64位
     */
    public abstract long asLong();

    /**
     * Converts to long with padding for 32-bit hashes
     * 32位哈希填充后转换为long
     *
     * @return long value | long值
     */
    public abstract long padToLong();

    /**
     * Converts to byte array
     * 转换为字节数组
     *
     * @return byte array (defensive copy) | 字节数组（防御性复制）
     */
    public abstract byte[] asBytes();

    /**
     * Writes hash bytes to a destination array
     * 将哈希字节写入目标数组
     *
     * @param dest   destination array | 目标数组
     * @param offset starting offset | 起始偏移
     * @return number of bytes written | 写入的字节数
     */
    public abstract int writeBytesTo(byte[] dest, int offset);

    /**
     * Converts to hexadecimal string
     * 转换为十六进制字符串
     *
     * @return hexadecimal string | 十六进制字符串
     */
    public String toHex() {
        return HEX.formatHex(asBytes());
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a HashCode from an int value
     * 从int值创建HashCode
     *
     * @param hash int hash value | int哈希值
     * @return HashCode instance | HashCode实例
     */
    public static HashCode fromInt(int hash) {
        return new IntHashCode(hash);
    }

    /**
     * Creates a HashCode from a long value
     * 从long值创建HashCode
     *
     * @param hash long hash value | long哈希值
     * @return HashCode instance | HashCode实例
     */
    public static HashCode fromLong(long hash) {
        return new LongHashCode(hash);
    }

    /**
     * Creates a HashCode from a byte array
     * 从字节数组创建HashCode
     *
     * @param bytes byte array | 字节数组
     * @return HashCode instance | HashCode实例
     */
    public static HashCode fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw OpenHashException.invalidInput("bytes cannot be null or empty");
        }
        return new BytesHashCode(bytes.clone());
    }

    /**
     * Creates a HashCode from a hexadecimal string
     * 从十六进制字符串创建HashCode
     *
     * @param hex hexadecimal string | 十六进制字符串
     * @return HashCode instance | HashCode实例
     */
    public static HashCode fromHex(String hex) {
        if (hex == null || hex.isEmpty() || hex.length() % 2 != 0) {
            throw OpenHashException.invalidInput("hex string must have even length");
        }
        try {
            byte[] bytes = HEX.parseHex(hex);
            return new BytesHashCode(bytes);
        } catch (IllegalArgumentException e) {
            throw OpenHashException.invalidInput("invalid hex character");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashCode other)) return false;
        return Arrays.equals(asBytes(), other.asBytes());
    }

    @Override
    public int hashCode() {
        return asInt();
    }

    @Override
    public String toString() {
        return "HashCode{" + toHex() + "}";
    }

    // ==================== Internal Implementations | 内部实现 ====================

    /**
     * 32-bit hash code implementation
     */
    private static final class IntHashCode extends HashCode {
        private final int hash;

        IntHashCode(int hash) {
            this.hash = hash;
        }

        @Override
        public int bits() {
            return 32;
        }

        @Override
        public int asInt() {
            return hash;
        }

        @Override
        public long asLong() {
            throw new IllegalStateException("32-bit hash cannot be converted to long");
        }

        @Override
        public long padToLong() {
            return hash & 0xFFFFFFFFL;
        }

        @Override
        public byte[] asBytes() {
            return new byte[]{
                    (byte) hash,
                    (byte) (hash >> 8),
                    (byte) (hash >> 16),
                    (byte) (hash >> 24)
            };
        }

        @Override
        public int writeBytesTo(byte[] dest, int offset) {
            java.util.Objects.requireNonNull(dest, "dest");
            if (offset < 0 || offset + 4 > dest.length) {
                throw new IndexOutOfBoundsException(
                        "offset=" + offset + ", required=4, dest.length=" + dest.length);
            }
            dest[offset] = (byte) hash;
            dest[offset + 1] = (byte) (hash >> 8);
            dest[offset + 2] = (byte) (hash >> 16);
            dest[offset + 3] = (byte) (hash >> 24);
            return 4;
        }
    }

    /**
     * 64-bit hash code implementation
     */
    private static final class LongHashCode extends HashCode {
        private final long hash;

        LongHashCode(long hash) {
            this.hash = hash;
        }

        @Override
        public int bits() {
            return 64;
        }

        @Override
        public int asInt() {
            return (int) hash;
        }

        @Override
        public long asLong() {
            return hash;
        }

        @Override
        public long padToLong() {
            return hash;
        }

        @Override
        public byte[] asBytes() {
            byte[] bytes = new byte[8];
            for (int i = 0; i < 8; i++) {
                bytes[i] = (byte) (hash >> (i * 8));
            }
            return bytes;
        }

        @Override
        public int writeBytesTo(byte[] dest, int offset) {
            java.util.Objects.requireNonNull(dest, "dest");
            if (offset < 0 || offset + 8 > dest.length) {
                throw new IndexOutOfBoundsException(
                        "offset=" + offset + ", required=8, dest.length=" + dest.length);
            }
            for (int i = 0; i < 8; i++) {
                dest[offset + i] = (byte) (hash >> (i * 8));
            }
            return 8;
        }
    }

    /**
     * Variable-length hash code implementation
     */
    private static final class BytesHashCode extends HashCode {
        private final byte[] bytes;

        BytesHashCode(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int bits() {
            return bytes.length * 8;
        }

        @Override
        public int asInt() {
            if (bytes.length < 4) {
                throw new IllegalStateException("Hash is less than 32 bits");
            }
            return (bytes[0] & 0xFF)
                    | ((bytes[1] & 0xFF) << 8)
                    | ((bytes[2] & 0xFF) << 16)
                    | ((bytes[3] & 0xFF) << 24);
        }

        @Override
        public long asLong() {
            if (bytes.length < 8) {
                throw new IllegalStateException("Hash is less than 64 bits");
            }
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result |= (bytes[i] & 0xFFL) << (i * 8);
            }
            return result;
        }

        @Override
        public long padToLong() {
            long result = 0;
            for (int i = 0; i < Math.min(8, bytes.length); i++) {
                result |= (bytes[i] & 0xFFL) << (i * 8);
            }
            return result;
        }

        @Override
        public byte[] asBytes() {
            return bytes.clone();
        }

        @Override
        public int writeBytesTo(byte[] dest, int offset) {
            java.util.Objects.requireNonNull(dest, "dest");
            if (offset < 0 || offset + bytes.length > dest.length) {
                throw new IndexOutOfBoundsException(
                        "offset=" + offset + ", required=" + bytes.length + ", dest.length=" + dest.length);
            }
            System.arraycopy(bytes, 0, dest, offset, bytes.length);
            return bytes.length;
        }
    }
}
