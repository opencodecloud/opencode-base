package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Byte Array Utility Class - Guava-style operations for byte primitive arrays
 * byte 数组工具类 - Guava 风格的 byte 原始类型数组操作
 *
 * <p>Provides comprehensive byte array operations inspired by Guava Bytes.</p>
 * <p>提供 byte 原始类型数组的操作方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse) - 数组操作</li>
 *   <li>String operations (join) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * byte[] merged = Bytes.concat(arr1, arr2);
 * boolean found = Bytes.contains(arr, (byte) 0x0F);
 * int idx = Bytes.indexOf(arr, (byte) 0x00);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Partially (throws on null array) - 空值安全: 部分 (null 数组抛异常)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Bytes {

    private Bytes() {
    }

    public static final int BYTES = Byte.BYTES;

    // ==================== 数组操作 ====================

    /**
     * Concatenates multiple arrays | 合并多个数组
     *
     * @param arrays the arrays to concatenate | 要合并的数组
     * @return the concatenated array | 合并后的数组
     */
    public static byte[] concat(byte[]... arrays) {
        long totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        byte[] result = new byte[(int) totalLength];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Checks whether the array contains the specified element | 检查数组是否包含指定元素
     *
     * @param array the array to search | 要搜索的数组
     * @param target the element to find | 要查找的元素
     * @return true if found | 找到返回 true
     */
    public static boolean contains(byte[] array, byte target) {
        return indexOf(array, target) >= 0;
    }

    /**
     * Finds the index of the specified element | 查找元素索引
     *
     * @param array the array to search | 要搜索的数组
     * @param target the element to find | 要查找的元素
     * @return the index, or -1 if not found | 索引，未找到返回 -1
     */
    public static int indexOf(byte[] array, byte target) {
        return indexOf(array, target, 0, array.length);
    }

    /**
     * Finds the index of the element within the specified range
     * 在指定范围内查找元素索引
     */
    public static int indexOf(byte[] array, byte target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the position of a sub-array
     * 查找子数组位置
     */
    public static int indexOf(byte[] array, byte[] target) {
        if (target.length == 0) {
            return 0;
        }
        if (target.length > array.length) {
            return -1;
        }
        outer:
        for (int i = 0; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /**
     * Finds the index of the element searching from the end
     * 从后向前查找元素索引
     */
    public static int lastIndexOf(byte[] array, byte target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 约束 ====================

    /**
     * Constrains the value to the specified range
     * 约束值在指定范围内
     */
    public static byte constrainToRange(byte value, byte min, byte max) {
        if (min > max) {
            throw new IllegalArgumentException("min > max");
        }
        return value < min ? min : (value > max ? max : value);
    }

    // ==================== 转换 ====================

    /**
     * Converts to an unsigned int
     * 转为无符号 int
     */
    public static int toUnsignedInt(byte value) {
        return value & 0xFF;
    }

    /**
     * Converts to an unsigned long
     * 转为无符号 long
     */
    public static long toUnsignedLong(byte value) {
        return value & 0xFFL;
    }

    // ==================== 集合转换 ====================

    /**
     * Converts to a List
     * 转为 List
     */
    public static List<Byte> asList(byte... array) {
        if (array.length == 0) {
            return Collections.emptyList();
        }
        List<Byte> list = new ArrayList<>(array.length);
        for (byte value : array) {
            list.add(value);
        }
        return list;
    }

    /**
     * Converts a Collection to an array
     * 从 Collection 转为数组
     */
    public static byte[] toArray(Collection<? extends Number> collection) {
        byte[] result = new byte[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.byteValue();
        }
        return result;
    }

    // ==================== 数组反转 ====================

    /**
     * Reverses the array
     * 反转数组
     */
    public static void reverse(byte[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the specified range of the array
     * 反转数组指定范围
     */
    public static void reverse(byte[] array, int fromIndex, int toIndex) {
        int left = fromIndex;
        int right = toIndex - 1;
        while (left < right) {
            byte temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    // ==================== 比较 ====================

    /**
     * Compares two byte arrays
     * 比较两个字节数组
     */
    public static int compare(byte[] a, byte[] b) {
        return Arrays.compare(a, b);
    }

    /**
     * Unsigned comparison
     * 无符号比较
     */
    public static int compareUnsigned(byte a, byte b) {
        return Byte.compareUnsigned(a, b);
    }

    /**
     * Checks whether two byte arrays are equal
     * 比较字节数组是否相等
     */
    public static boolean equals(byte[] a, byte[] b) {
        return Arrays.equals(a, b);
    }

    // ==================== 字符串转换 ====================

    /**
     * Joins array elements into a string
     * 数组转字符串
     */
    public static String join(String separator, byte... array) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    // ==================== 空数组 ====================

    public static final byte[] EMPTY_ARRAY = new byte[0];

    public static byte[] ensureNonNull(byte[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }

    // ==================== 哈希 ====================

    /**
     * Computes the hash code of a byte array
     * 计算字节数组哈希码
     */
    public static int hashCode(byte[] array) {
        return Arrays.hashCode(array);
    }

    // ==================== 子数组 ====================

    /**
     * Returns a sub-array
     * 获取子数组
     */
    public static byte[] subarray(byte[] array, int start, int end) {
        if (start < 0 || end > array.length || start > end) {
            throw new IndexOutOfBoundsException();
        }
        byte[] result = new byte[end - start];
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }

    /**
     * Ensures that array has minimum capacity, expanding if needed.
     * 确保数组具有最小容量，如果需要则扩展。
     *
     * @param array     the original array | 原数组
     * @param minLength the minimum required length | 最小需要长度
     * @param padding   additional padding beyond minLength | 超出 minLength 的额外填充
     * @return the array with ensured capacity | 确保容量后的数组
     */
    public static byte[] ensureCapacity(byte[] array, int minLength, int padding) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength cannot be negative: " + minLength);
        }
        if (padding < 0) {
            throw new IllegalArgumentException("padding cannot be negative: " + padding);
        }
        if (array.length >= minLength) {
            return array;
        }
        long newLength = (long) minLength + padding;
        if (newLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Required array size too large: " + newLength);
        }
        return Arrays.copyOf(array, (int) newLength);
    }
}
