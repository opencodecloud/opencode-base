package cloud.opencode.base.core.primitives;

import java.util.*;

/**
 * Float Array Utility Class - Guava-style operations for float primitive arrays
 * float 数组工具类 - Guava 风格的 float 原始类型数组操作
 *
 * <p>Provides comprehensive float array operations inspired by Guava Floats.</p>
 * <p>提供 float 原始类型数组的操作方法，参考 Guava Floats。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte conversion (toByteArray, fromByteArray) - 字节转换</li>
 *   <li>Array operations (concat, contains, indexOf, lastIndexOf) - 数组操作</li>
 *   <li>Min/Max (min, max, constrainToRange) - 最值和范围</li>
 *   <li>Collection conversion (asList, toArray) - 集合转换</li>
 *   <li>Array manipulation (reverse) - 数组操作</li>
 *   <li>Validation (isFinite) - 验证</li>
 *   <li>String operations (join, tryParse) - 字符串操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Array operations - 数组操作
 * float[] merged = Floats.concat(arr1, arr2);
 * int idx = Floats.indexOf(arr, 3.14f);
 * float max = Floats.max(1.0f, 2.0f, 3.0f);
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
public final class Floats {

    private Floats() {
    }

    public static final int BYTES = Float.BYTES;

    public static byte[] toByteArray(float value) {
        return Ints.toByteArray(Float.floatToRawIntBits(value));
    }

    public static float fromByteArray(byte[] bytes) {
        return Float.intBitsToFloat(Ints.fromByteArray(bytes));
    }

    public static float[] concat(float[]... arrays) {
        long totalLength = 0;
        for (float[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Total array length overflow: " + totalLength);
        }
        float[] result = new float[(int) totalLength];
        int offset = 0;
        for (float[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static boolean contains(float[] array, float target) {
        return indexOf(array, target) >= 0;
    }

    public static int indexOf(float[] array, float target) {
        for (int i = 0; i < array.length; i++) {
            if (Float.compare(array[i], target) == 0) return i;
        }
        return -1;
    }

    public static int lastIndexOf(float[] array, float target) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (Float.compare(array[i], target) == 0) return i;
        }
        return -1;
    }

    public static float min(float... array) {
        if (array.length == 0) throw new IllegalArgumentException("Array is empty");
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            min = Math.min(min, array[i]);
        }
        return min;
    }

    public static float max(float... array) {
        if (array.length == 0) throw new IllegalArgumentException("Array is empty");
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            max = Math.max(max, array[i]);
        }
        return max;
    }

    public static float constrainToRange(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isNaN(min) || Float.isNaN(max)) {
            throw new IllegalArgumentException("value, min, and max must not be NaN");
        }
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
        }
        return Math.max(min, Math.min(max, value));
    }

    public static int compare(float a, float b) {
        return Float.compare(a, b);
    }

    public static List<Float> asList(float... array) {
        if (array.length == 0) return Collections.emptyList();
        List<Float> list = new ArrayList<>(array.length);
        for (float value : array) list.add(value);
        return list;
    }

    public static float[] toArray(Collection<? extends Number> collection) {
        float[] result = new float[collection.size()];
        int i = 0;
        for (Number number : collection) {
            result[i++] = number.floatValue();
        }
        return result;
    }

    public static void reverse(float[] array) {
        int left = 0, right = array.length - 1;
        while (left < right) {
            float temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    public static boolean isFinite(float value) {
        return Float.isFinite(value);
    }

    public static String join(String separator, float... array) {
        if (array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(separator).append(array[i]);
        }
        return sb.toString();
    }

    public static Float tryParse(String string) {
        if (string == null || string.isEmpty()) return null;
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static final float[] EMPTY_ARRAY = new float[0];

    public static float[] ensureNonNull(float[] array) {
        return array != null ? array : EMPTY_ARRAY;
    }
}
