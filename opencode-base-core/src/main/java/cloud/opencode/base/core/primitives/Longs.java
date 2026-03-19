package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Long Array Utility Class - Guava-style operations for long primitive arrays
 * long 数组工具类 - Guava 风格的 long 原始类型数组操作
 *
 * <p>Provides comprehensive long array operations inspired by Guava Longs.</p>
 * <p>提供 long 原始类型数组的操作方法，参考 Guava Longs。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte conversion (toByteArray, fromByteArray, fromBytes) - 字节转换</li>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Min/Max (min, max, constrainToRange) - 最值和范围</li>
 *   <li>Safe casting (saturatedCast, checkedCast) - 安全转换</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse, rotate, sortDescending) - 数组操作</li>
 *   <li>Comparison (compare, lexicographicalComparator) - 比较</li>
 *   <li>String operations (join, tryParse) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Byte conversion - 字节转换
 * byte[] bytes = Longs.toByteArray(123456789L);
 * long value = Longs.fromByteArray(bytes);
 *
 * // Array operations - 数组操作
 * long[] merged = Longs.concat(arr1, arr2);
 * int idx = Longs.indexOf(arr, 5L);
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
public final class Longs {

    private Longs() {
    }

    public static final int BYTES = Long.BYTES;
    public static final long MAX_POWER_OF_TWO = 1L << (Long.SIZE - 2);

    // ==================== 转换 ====================

    /**
     * Converts long to byte array (big-endian)
     * long 转 byte 数组（大端序）
     */
    public static byte[] toByteArray(long value) {
        return new byte[]{
                (byte) (value >> 56),
                (byte) (value >> 48),
                (byte) (value >> 40),
                (byte) (value >> 32),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    /**
     * Converts byte array to long (big-endian)
     * byte 数组转 long（大端序）
     */
    public static long fromByteArray(byte[] bytes) {
        if (bytes.length < BYTES) {
            throw new IllegalArgumentException("Array must have at least " + BYTES + " bytes");
        }
        return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3],
                bytes[4], bytes[5], bytes[6], bytes[7]);
    }

    /**
     * Converts 8 bytes to long
     * 8 个字节转 long
     */
    public static long fromBytes(byte b1, byte b2, byte b3, byte b4,
                                  byte b5, byte b6, byte b7, byte b8) {
        return ((long) b1 & 0xFF) << 56
                | ((long) b2 & 0xFF) << 48
                | ((long) b3 & 0xFF) << 40
                | ((long) b4 & 0xFF) << 32
                | ((long) b5 & 0xFF) << 24
                | ((long) b6 & 0xFF) << 16
                | ((long) b7 & 0xFF) << 8
                | ((long) b8 & 0xFF);
    }

    // ==================== 数组操作 ====================

    /**
     * Concatenates multiple arrays
     * 合并多个数组
     */
    public static long[] concat(long[]... arrays) {
        long totalLength = 0;
        for (long[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        long[] result = new long[(int) totalLength];
        int offset = 0;
        for (long[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Checks if the array contains the specified element
     * 检查数组是否包含指定元素
     */
    public static boolean contains(long[] array, long target) {
        return indexOf(array, target) >= 0;
    }

    /**
     * Finds the element index
     * 查找元素索引
     */
    public static int indexOf(long[] array, long target) {
        return indexOf(array, target, 0, array.length);
    }

    /**
     * Finds the element index within the specified range
     * 在指定范围内查找元素索引
     */
    public static int indexOf(long[] array, long target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the element index from the end
     * 从后向前查找元素索引
     */
    public static int lastIndexOf(long[] array, long target) {
        return lastIndexOf(array, target, 0, array.length);
    }

    /**
     * Finds the element index from the end within the specified range
     * 在指定范围内从后向前查找元素索引
     */
    public static int lastIndexOf(long[] array, long target, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 最值 ====================

    /**
     * Returns the minimum value
     * 返回最小值
     */
    public static long min(long... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * Returns the maximum value
     * 返回最大值
     */
    public static long max(long... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        long max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    // ==================== 约束 ====================

    /**
     * Constrains the value within the specified range
     * 约束值在指定范围内
     */
    public static long constrainToRange(long value, long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
        }
        return Math.max(min, Math.min(max, value));
    }

    // ==================== 比较 ====================

    /**
     * Compares two long values
     * 比较两个 long 值
     */
    public static int compare(long a, long b) {
        return Long.compare(a, b);
    }

    /**
     * Gets the comparator
     * 获取比较器
     */
    public static Comparator<long[]> lexicographicalComparator() {
        return LexicographicalComparator.INSTANCE;
    }

    private enum LexicographicalComparator implements Comparator<long[]> {
        INSTANCE;

        @Override
        public int compare(long[] left, long[] right) {
            int minLength = Math.min(left.length, right.length);
            for (int i = 0; i < minLength; i++) {
                int result = Long.compare(left[i], right[i]);
                if (result != 0) {
                    return result;
                }
            }
            return Integer.compare(left.length, right.length);
        }
    }

    // ==================== 集合转换 ====================

    /**
     * Converts to List
     * 转为 List
     */
    public static List<Long> asList(long... array) {
        if (array.length == 0) {
            return Collections.emptyList();
        }
        List<Long> list = new ArrayList<>(array.length);
        for (long value : array) {
            list.add(value);
        }
        return list;
    }

    /**
     * Converts from Collection to array
     * 从 Collection 转为数组
     */
    public static long[] toArray(Collection<? extends Number> collection) {
        long[] result = new long[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.longValue();
        }
        return result;
    }

    // ==================== 数组反转 ====================

    /**
     * Reverses the array
     * 反转数组
     */
    public static void reverse(long[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the specified range of the array
     * 反转数组指定范围
     */
    public static void reverse(long[] array, int fromIndex, int toIndex) {
        int left = fromIndex;
        int right = toIndex - 1;
        while (left < right) {
            long temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * Rotates the array
     * 旋转数组
     * <p>
     * 正数向右旋转，负数向左旋转
     *
     * @param array    the array | 数组
     * @param distance rotation distance | 旋转距离
     */
    public static void rotate(long[] array, int distance) {
        if (array.length <= 1) {
            return;
        }
        int length = array.length;
        distance = distance % length;
        if (distance < 0) {
            distance += length;
        }
        if (distance == 0) {
            return;
        }
        reverse(array, 0, length);
        reverse(array, 0, distance);
        reverse(array, distance, length);
    }

    // ==================== 排序 ====================

    /**
     * Sorts in descending order
     * 降序排序
     */
    public static void sortDescending(long[] array) {
        sortDescending(array, 0, array.length);
    }

    /**
     * Sorts the specified range in descending order
     * 降序排序指定范围
     */
    public static void sortDescending(long[] array, int fromIndex, int toIndex) {
        Arrays.sort(array, fromIndex, toIndex);
        reverse(array, fromIndex, toIndex);
    }

    // ==================== 排序检查 ====================

    /**
     * Checks if the array is sorted
     * 检查数组是否已排序
     */
    public static boolean isSorted(long[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    // ==================== 字符串转换 ====================

    /**
     * Converts the array to string
     * 数组转字符串
     */
    public static String join(String separator, long... array) {
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

    /**
     * Attempts to parse as long
     * 尝试解析为 long
     */
    public static Long tryParse(String string) {
        return tryParse(string, 10);
    }

    /**
     * Attempts to parse as long (specified radix)
     * 尝试解析为 long（指定进制）
     */
    public static Long tryParse(String string, int radix) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(string, radix);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 空数组 ====================

    public static final long[] EMPTY_ARRAY = new long[0];

    public static long[] ensureNonNull(long[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
