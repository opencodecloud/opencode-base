package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Char Array Utility Class - Guava-style operations for char primitive arrays
 * char 数组工具类 - Guava 风格的 char 原始类型数组操作
 *
 * <p>Provides comprehensive char array operations inspired by Guava Chars.</p>
 * <p>提供 char 原始类型数组的操作方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte conversion (toByteArray, fromByteArray) - 字节转换</li>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Min/Max (min, max, constrainToRange) - 最值和范围</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse) - 数组操作</li>
 *   <li>Comparison (compare) - 比较</li>
 *   <li>String operations (join) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * char[] merged = Chars.concat(arr1, arr2);
 * boolean found = Chars.contains(arr, 'a');
 * char min = Chars.min(arr);
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
public final class Chars {

    private Chars() {
    }

    public static final int BYTES = Character.BYTES;

    public static byte[] toByteArray(char value) {
        return new byte[]{(byte) (value >> 8), (byte) value};
    }

    public static char fromByteArray(byte[] bytes) {
        return (char) ((bytes[0] << 8) | (bytes[1] & 0xFF));
    }

    public static char[] concat(char[]... arrays) {
        long totalLength = 0;
        for (char[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        char[] result = new char[(int) totalLength];
        int offset = 0;
        for (char[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean contains(char[] array, char target) {
        return indexOf(array, target) >= 0;
    }

    public static int indexOf(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(char[] array, char target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public static char min(char... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        char min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static char max(char... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        char max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static char constrainToRange(char value, char min, char max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static int compare(char a, char b) {
        return Character.compare(a, b);
    }

    public static List<Character> asList(char... array) {
        if (array.length == 0) {
            return Collections.emptyList();
        }
        List<Character> list = new ArrayList<>(array.length);
        for (char value : array) {
            list.add(value);
        }
        return list;
    }

    public static char[] toArray(Collection<Character> collection) {
        char[] result = new char[collection.size()];
        int i = 0;
        for (Character c : collection) {
            result[i++] = c;
        }
        return result;
    }

    public static void reverse(char[] array) {
        int left = 0, right = array.length - 1;
        while (left < right) {
            char temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    public static String join(String separator, char... array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    public static final char[] EMPTY_ARRAY = new char[0];

    public static char[] ensureNonNull(char[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
