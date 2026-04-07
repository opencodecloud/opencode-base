package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Double Array Utility Class - Guava-style operations for double primitive arrays
 * double 数组工具类 - Guava 风格的 double 原始类型数组操作
 *
 * <p>Provides comprehensive double array operations inspired by Guava Doubles.</p>
 * <p>提供 double 原始类型数组的操作方法，参考 Guava Doubles。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte conversion (toByteArray, fromByteArray) - 字节转换</li>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Min/Max (min, max, constrainToRange) - 最值和范围</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse, rotate, sortDescending) - 数组操作</li>
 *   <li>Validation (isFinite) - 验证</li>
 *   <li>String operations (join, tryParse) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * double[] merged = Doubles.concat(arr1, arr2);
 * int idx = Doubles.indexOf(arr, 3.14);
 * double min = Doubles.min(1.0, 2.0, 3.0);
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
public final class Doubles {

    private Doubles() {
    }

    public static final int BYTES = Double.BYTES;

    public static byte[] toByteArray(double value) {
        return Longs.toByteArray(Double.doubleToRawLongBits(value));
    }

    public static double fromByteArray(byte[] bytes) {
        return Double.longBitsToDouble(Longs.fromByteArray(bytes));
    }

    public static double[] concat(double[]... arrays) {
        long totalLength = 0;
        for (double[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        double[] result = new double[(int) totalLength];
        int offset = 0;
        for (double[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean contains(double[] array, double target) {
        return indexOf(array, target) >= 0;
    }

    public static int indexOf(double[] array, double target) {
        for (int i = 0; i < array.length; i++) {
            if (Double.compare(array[i], target) == 0) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(double[] array, double target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (Double.compare(array[i], target) == 0) {
                return i;
            }
        }
        return -1;
    }

    public static double min(double... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            min = Math.min(min, array[i]);
        }
        return min;
    }

    public static double max(double... array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        double max = array[0];
        for (int i = 1; i < array.length; i++) {
            max = Math.max(max, array[i]);
        }
        return max;
    }

    public static double constrainToRange(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException("value, min, and max must not be NaN");
        }
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
        }
        return Math.max(min, Math.min(max, value));
    }

    public static int compare(double a, double b) {
        return Double.compare(a, b);
    }

    public static List<Double> asList(double... array) {
        if (array.length == 0) {
            return Collections.emptyList();
        }
        List<Double> list = new ArrayList<>(array.length);
        for (double value : array) {
            list.add(value);
        }
        return list;
    }

    public static double[] toArray(Collection<? extends Number> collection) {
        double[] result = new double[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.doubleValue();
        }
        return result;
    }

    public static void reverse(double[] array) {
        reverse(array, 0, array.length);
    }

    public static void reverse(double[] array, int fromIndex, int toIndex) {
        int left = fromIndex;
        int right = toIndex - 1;
        while (left < right) {
            double temp = array[left];
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
    public static void rotate(double[] array, int distance) {
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

    /**
     * Sorts in descending order
     * 降序排序
     */
    public static void sortDescending(double[] array) {
        sortDescending(array, 0, array.length);
    }

    /**
     * Sorts the specified range in descending order
     * 降序排序指定范围
     */
    public static void sortDescending(double[] array, int fromIndex, int toIndex) {
        Arrays.sort(array, fromIndex, toIndex);
        reverse(array, fromIndex, toIndex);
    }

    public static boolean isFinite(double value) {
        return Double.isFinite(value);
    }

    public static String join(String separator, double... array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    public static Double tryParse(String string) {
        if (string == null || string.isEmpty()) return null;
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static final double[] EMPTY_ARRAY = new double[0];

    public static double[] ensureNonNull(double[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
