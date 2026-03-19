package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Short Array Utility Class - Guava-style operations for short primitive arrays
 * short 数组工具类 - Guava 风格的 short 原始类型数组操作
 *
 * <p>Provides comprehensive short array operations inspired by Guava Shorts.</p>
 * <p>提供 short 原始类型数组的操作方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte conversion (toByteArray, fromByteArray) - 字节转换</li>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Min/Max (min, max, constrainToRange) - 最值和范围</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse) - 数组操作</li>
 *   <li>String operations (join, tryParse) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * short[] merged = Shorts.concat(arr1, arr2);
 * short min = Shorts.min(arr);
 * int idx = Shorts.indexOf(arr, (short) 100);
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
public final class Shorts {

    private Shorts() {
    }

    public static final int BYTES = Short.BYTES;

    public static byte[] toByteArray(short value) {
        return new byte[]{(byte) (value >> 8), (byte) value};
    }

    public static short fromByteArray(byte[] bytes) {
        return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
    }

    public static short[] concat(short[]... arrays) {
        long totalLength = 0;
        for (short[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        short[] result = new short[(int) totalLength];
        int offset = 0;
        for (short[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean contains(short[] array, short target) {
        return indexOf(array, target) >= 0;
    }

    public static int indexOf(short[] array, short target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public static int lastIndexOf(short[] array, short target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public static short min(short... array) {
        if (array.length == 0) throw new IllegalArgumentException("Array is empty");
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) min = array[i];
        }
        return min;
    }

    public static short max(short... array) {
        if (array.length == 0) throw new IllegalArgumentException("Array is empty");
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) max = array[i];
        }
        return max;
    }

    public static short constrainToRange(short value, short min, short max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static int compare(short a, short b) {
        return Short.compare(a, b);
    }

    public static List<Short> asList(short... array) {
        if (array.length == 0) return Collections.emptyList();
        List<Short> list = new ArrayList<>(array.length);
        for (short value : array) list.add(value);
        return list;
    }

    public static short[] toArray(Collection<? extends Number> collection) {
        short[] result = new short[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.shortValue();
        }
        return result;
    }

    public static void reverse(short[] array) {
        int left = 0, right = array.length - 1;
        while (left < right) {
            short temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    public static short saturatedCast(long value) {
        if (value > Short.MAX_VALUE) return Short.MAX_VALUE;
        if (value < Short.MIN_VALUE) return Short.MIN_VALUE;
        return (short) value;
    }

    public static short checkedCast(long value) {
        short result = (short) value;
        if (result != value) {
            throw new ArithmeticException("Out of range: " + value);
        }
        return result;
    }

    public static String join(String separator, short... array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    public static final short[] EMPTY_ARRAY = new short[0];

    public static short[] ensureNonNull(short[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
