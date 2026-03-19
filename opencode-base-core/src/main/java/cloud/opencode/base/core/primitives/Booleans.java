package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Boolean Array Utility Class - Guava-style operations for boolean primitive arrays
 * boolean 数组工具类 - Guava 风格的 boolean 原始类型数组操作
 *
 * <p>Provides comprehensive boolean array operations inspired by Guava Booleans.</p>
 * <p>提供 boolean 原始类型数组的操作方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse) - 数组操作</li>
 *   <li>Comparison (compare) - 比较</li>
 *   <li>Counting (countTrue) - 统计</li>
 *   <li>String operations (join) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * boolean[] merged = Booleans.concat(arr1, arr2);
 * boolean found = Booleans.contains(arr, true);
 * int count = Booleans.countTrue(arr);
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
public final class Booleans {

    private Booleans() {
    }

    public static boolean[] concat(boolean[]... arrays) {
        long totalLength = 0;
        for (boolean[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        boolean[] result = new boolean[(int) totalLength];
        int offset = 0;
        for (boolean[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean contains(boolean[] array, boolean target) {
        return indexOf(array, target) >= 0;
    }

    public static int indexOf(boolean[] array, boolean target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public static int lastIndexOf(boolean[] array, boolean target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    public static int compare(boolean a, boolean b) {
        return Boolean.compare(a, b);
    }

    public static List<Boolean> asList(boolean... array) {
        if (array.length == 0) return Collections.emptyList();
        List<Boolean> list = new ArrayList<>(array.length);
        for (boolean value : array) list.add(value);
        return list;
    }

    public static boolean[] toArray(Collection<Boolean> collection) {
        boolean[] result = new boolean[collection.size()];
        int i = 0;
        for (Boolean value : collection) {
            result[i++] = value != null && value;
        }
        return result;
    }

    public static void reverse(boolean[] array) {
        int left = 0, right = array.length - 1;
        while (left < right) {
            boolean temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    public static int countTrue(boolean... array) {
        int count = 0;
        for (boolean value : array) {
            if (value) count++;
        }
        return count;
    }

    public static int countFalse(boolean... array) {
        return array.length - countTrue(array);
    }

    public static boolean and(boolean... array) {
        for (boolean value : array) {
            if (!value) return false;
        }
        return true;
    }

    public static boolean or(boolean... array) {
        for (boolean value : array) {
            if (value) return true;
        }
        return false;
    }

    public static boolean xor(boolean... array) {
        int trueCount = countTrue(array);
        return trueCount % 2 == 1;
    }

    public static String join(String separator, boolean... array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    public static final boolean[] EMPTY_ARRAY = new boolean[0];

    public static boolean[] ensureNonNull(boolean[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
