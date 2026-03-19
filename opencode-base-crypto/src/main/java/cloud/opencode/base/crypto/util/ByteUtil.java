package cloud.opencode.base.crypto.util;

import cloud.opencode.base.core.primitives.Ints;
import cloud.opencode.base.core.primitives.Longs;

import java.util.Arrays;

/**
 * Byte array manipulation utilities for cryptographic operations - Provides operations for concatenation, splitting, XOR, and padding
 * 加密操作的字节数组操作工具 - 提供连接、拆分、异或和填充操作
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte array manipulation utilities - 字节数组操作工具</li>
 *   <li>Concatenation, splitting, comparison - 连接、分割、比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] combined = ByteUtil.concat(part1, part2);
 * boolean equal = ByteUtil.constantTimeEquals(a, b);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为数据长度</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class ByteUtil {

    private ByteUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Concatenate multiple byte arrays into a single array
     * 将多个字节数组连接成单个数组
     *
     * @param arrays byte arrays to concatenate
     * @return concatenated byte array
     * @throws NullPointerException if arrays is null or contains null elements
     */
    public static byte[] concat(byte[]... arrays) {
        if (arrays == null) {
            throw new NullPointerException("Arrays must not be null");
        }

        // Calculate total length with overflow check
        long totalLengthLong = 0;
        for (byte[] array : arrays) {
            if (array == null) {
                throw new NullPointerException("Array element must not be null");
            }
            totalLengthLong += array.length;
            if (totalLengthLong > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                    "Total concatenated length exceeds maximum array size");
            }
        }
        int totalLength = (int) totalLengthLong;

        // Concatenate arrays
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    /**
     * Split byte array into multiple arrays of specified lengths
     * 将字节数组拆分为指定长度的多个数组
     *
     * @param array byte array to split
     * @param lengths lengths of each resulting array
     * @return array of byte arrays
     * @throws NullPointerException if array or lengths is null
     * @throws IllegalArgumentException if sum of lengths does not equal array length or any length is negative
     */
    public static byte[][] split(byte[] array, int... lengths) {
        if (array == null) {
            throw new NullPointerException("Array must not be null");
        }
        if (lengths == null) {
            throw new NullPointerException("Lengths must not be null");
        }

        // Validate lengths
        int totalLength = 0;
        for (int length : lengths) {
            if (length < 0) {
                throw new IllegalArgumentException("Length must be non-negative");
            }
            totalLength += length;
        }

        if (totalLength != array.length) {
            throw new IllegalArgumentException(
                "Sum of lengths (" + totalLength + ") must equal array length (" + array.length + ")"
            );
        }

        // Split array
        byte[][] result = new byte[lengths.length][];
        int offset = 0;
        for (int i = 0; i < lengths.length; i++) {
            result[i] = Arrays.copyOfRange(array, offset, offset + lengths[i]);
            offset += lengths[i];
        }

        return result;
    }

    /**
     * XOR two byte arrays
     * 对两个字节数组执行异或操作
     *
     * @param a first byte array
     * @param b second byte array
     * @return XOR result
     * @throws NullPointerException if either array is null
     * @throws IllegalArgumentException if arrays have different lengths
     */
    public static byte[] xor(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new NullPointerException("Arrays must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                "Arrays must have equal length (a: " + a.length + ", b: " + b.length + ")"
            );
        }

        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }

    /**
     * Reverse byte array
     * 反转字节数组
     *
     * @param array byte array to reverse
     * @return reversed byte array
     * @throws NullPointerException if array is null
     */
    public static byte[] reverse(byte[] array) {
        if (array == null) {
            throw new NullPointerException("Array must not be null");
        }

        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }

        return result;
    }

    /**
     * Convert int to byte array (big-endian)
     * 将int转换为字节数组（大端序）
     *
     * @param value integer value
     * @return 4-byte array
     */
    public static byte[] intToBytes(int value) {
        return Ints.toByteArray(value);
    }

    /**
     * Convert byte array to int (big-endian)
     * 将字节数组转换为int（大端序）
     *
     * @param bytes byte array (must be at least 4 bytes)
     * @return integer value
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if bytes length is less than 4
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("Bytes must not be null");
        }
        if (bytes.length < 4) {
            throw new IllegalArgumentException("Bytes must be at least 4 bytes long");
        }
        return Ints.fromByteArray(bytes);
    }

    /**
     * Convert long to byte array (big-endian)
     * 将long转换为字节数组（大端序）
     *
     * @param value long value
     * @return 8-byte array
     */
    public static byte[] longToBytes(long value) {
        return Longs.toByteArray(value);
    }

    /**
     * Convert byte array to long (big-endian)
     * 将字节数组转换为long（大端序）
     *
     * @param bytes byte array (must be at least 8 bytes)
     * @return long value
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if bytes length is less than 8
     */
    public static long bytesToLong(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("Bytes must not be null");
        }
        if (bytes.length < 8) {
            throw new IllegalArgumentException("Bytes must be at least 8 bytes long");
        }
        return Longs.fromByteArray(bytes);
    }

    /**
     * Apply PKCS#7 padding to data
     * 对数据应用PKCS#7填充
     *
     * <p>PKCS#7 padding adds N bytes of value N, where N is the number of bytes
     * required to reach the next block boundary.
     * PKCS#7填充添加N个值为N的字节，其中N是到达下一个块边界所需的字节数。
     *
     * @param data data to pad
     * @param blockSize block size in bytes
     * @return padded data
     * @throws NullPointerException if data is null
     * @throws IllegalArgumentException if blockSize is not between 1 and 255
     */
    public static byte[] padPkcs7(byte[] data, int blockSize) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        if (blockSize < 1 || blockSize > 255) {
            throw new IllegalArgumentException("Block size must be between 1 and 255");
        }

        int paddingLength = blockSize - (data.length % blockSize);
        byte[] padded = new byte[data.length + paddingLength];

        System.arraycopy(data, 0, padded, 0, data.length);

        // Fill padding bytes
        for (int i = data.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }

        return padded;
    }

    /**
     * Remove PKCS#7 padding from data
     * 从数据中移除PKCS#7填充
     *
     * @param data padded data
     * @return unpadded data
     * @throws NullPointerException if data is null
     * @throws IllegalArgumentException if data is empty or padding is invalid
     */
    public static byte[] unpadPkcs7(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data must not be null");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("Data must not be empty");
        }

        int paddingLength = data[data.length - 1] & 0xFF;

        // Validate padding length
        if (paddingLength < 1 || paddingLength > data.length) {
            throw new IllegalArgumentException("Invalid padding length: " + paddingLength);
        }

        // Validate padding bytes
        for (int i = data.length - paddingLength; i < data.length; i++) {
            if ((data[i] & 0xFF) != paddingLength) {
                throw new IllegalArgumentException("Invalid padding at position " + i);
            }
        }

        return Arrays.copyOfRange(data, 0, data.length - paddingLength);
    }
}
