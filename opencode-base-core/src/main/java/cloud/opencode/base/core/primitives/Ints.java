package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Int Array Utility Class - Guava-style operations for int primitive arrays
 * int 数组工具类 - Guava 风格的 int 原始类型数组操作
 *
 * <p>Provides comprehensive int array operations inspired by Guava Ints.</p>
 * <p>提供 int 原始类型数组的操作方法，参考 Guava Ints。</p>
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
 * byte[] bytes = Ints.toByteArray(12345);
 * int value = Ints.fromByteArray(bytes);
 *
 * // Array operations - 数组操作
 * int[] merged = Ints.concat(arr1, arr2);
 * int idx = Ints.indexOf(arr, 5);
 *
 * // Safe conversion - 安全转换
 * int clamped = Ints.saturatedCast(Long.MAX_VALUE); // Integer.MAX_VALUE
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
public final class Ints {

    private Ints() {
    }

    public static final int BYTES = Integer.BYTES;
    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    // ==================== 转换 ====================

    /**
     * Converts int to byte array (big-endian)
     * int 转 byte 数组（大端序）
     */
    public static byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    /**
     * Converts byte array to int (big-endian)
     * byte 数组转 int（大端序）
     */
    public static int fromByteArray(byte[] bytes) {
        if (bytes.length < BYTES) {
            throw new IllegalArgumentException("Array must have at least " + BYTES + " bytes");
        }
        return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
    }

    /**
     * Converts 4 bytes to int
     * 4 个字节转 int
     */
    public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
        return ((b1 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8) | (b4 & 0xFF);
    }

    // ==================== 数组操作 ====================

    /**
     * Concatenates multiple arrays
     * 合并多个数组
     */
    public static int[] concat(int[]... arrays) {
        long totalLength = 0;
        for (int[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        int[] result = new int[(int) totalLength];
        int offset = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Checks if the array contains the specified element
     * 检查数组是否包含指定元素
     */
    public static boolean contains(int[] array, int target) {
        return indexOf(array, target) >= 0;
    }

    /**
     * Finds the element index
     * 查找元素索引
     */
    public static int indexOf(int[] array, int target) {
        return indexOf(array, target, 0, array.length);
    }

    /**
     * Finds the element index within the specified range
     * 在指定范围内查找元素索引
     */
    public static int indexOf(int[] array, int target, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the position of a sub-array in the array
     * 查找子数组在数组中的位置
     *
     * @param array  the source array | 源数组
     * @param target the target sub-array | 目标子数组
     * @return the result | 子数组首次出现的索引，未找到返回 -1
     */
    public static int indexOf(int[] array, int[] target) {
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
     * Finds the element index from the end
     * 从后向前查找元素索引
     */
    public static int lastIndexOf(int[] array, int target) {
        return lastIndexOf(array, target, 0, array.length);
    }

    /**
     * Finds the element index from the end within the specified range
     * 在指定范围内从后向前查找元素索引
     */
    public static int lastIndexOf(int[] array, int target, int start, int end) {
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
    public static int min(int... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        int min = array[0];
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
    public static int max(int... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        int max = array[0];
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
    public static int constrainToRange(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Saturated conversion (long to int)
     * 饱和转换（long 转 int）
     */
    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    /**
     * Checked conversion (long to int, throws on overflow)
     * 检查转换（long 转 int，溢出则抛异常）
     */
    public static int checkedCast(long value) {
        int result = (int) value;
        if (result != value) {
            throw new ArithmeticException("Out of range: " + value);
        }
        return result;
    }

    // ==================== 比较 ====================

    /**
     * Compares two int values
     * 比较两个 int 值
     */
    public static int compare(int a, int b) {
        return Integer.compare(a, b);
    }

    /**
     * Gets the comparator
     * 获取比较器
     */
    public static Comparator<int[]> lexicographicalComparator() {
        return LexicographicalComparator.INSTANCE;
    }

    private enum LexicographicalComparator implements Comparator<int[]> {
        INSTANCE;

        @Override
        public int compare(int[] left, int[] right) {
            int minLength = Math.min(left.length, right.length);
            for (int i = 0; i < minLength; i++) {
                int result = Integer.compare(left[i], right[i]);
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
    public static List<Integer> asList(int... array) {
        if (array.length == 0) {
            return Collections.emptyList();
        }
        List<Integer> list = new ArrayList<>(array.length);
        for (int value : array) {
            list.add(value);
        }
        return list;
    }

    /**
     * Converts from Collection to array
     * 从 Collection 转为数组
     */
    public static int[] toArray(Collection<? extends Number> collection) {
        int[] result = new int[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.intValue();
        }
        return result;
    }

    // ==================== 数组反转 ====================

    /**
     * Reverses the array
     * 反转数组
     */
    public static void reverse(int[] array) {
        reverse(array, 0, array.length);
    }

    /**
     * Reverses the specified range of the array
     * 反转数组指定范围
     */
    public static void reverse(int[] array, int fromIndex, int toIndex) {
        int left = fromIndex;
        int right = toIndex - 1;
        while (left < right) {
            int temp = array[left];
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
    public static void rotate(int[] array, int distance) {
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
    public static void sortDescending(int[] array) {
        sortDescending(array, 0, array.length);
    }

    /**
     * Sorts the specified range in descending order
     * 降序排序指定范围
     */
    public static void sortDescending(int[] array, int fromIndex, int toIndex) {
        Arrays.sort(array, fromIndex, toIndex);
        reverse(array, fromIndex, toIndex);
    }

    // ==================== 排序检查 ====================

    /**
     * Checks if the array is sorted
     * 检查数组是否已排序
     */
    public static boolean isSorted(int[] array) {
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
    public static String join(String separator, int... array) {
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
     * Attempts to parse as int
     * 尝试解析为 int
     */
    public static Integer tryParse(String string) {
        return tryParse(string, 10);
    }

    /**
     * Attempts to parse as int (specified radix)
     * 尝试解析为 int（指定进制）
     */
    public static Integer tryParse(String string, int radix) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(string, radix);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 空数组 ====================

    /**
     * Empty array constant
     * 空数组常量
     */
    public static final int[] EMPTY_ARRAY = new int[0];

    /**
     * Ensures the array is not null
     * 确保数组不为 null
     */
    public static int[] ensureNonNull(int[] array) {
        return array != null ? array : EMPTY_ARRAY;
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
    public static int[] ensureCapacity(int[] array, int minLength, int padding) {
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
