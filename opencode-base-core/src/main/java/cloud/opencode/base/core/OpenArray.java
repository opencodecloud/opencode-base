package cloud.opencode.base.core;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Array Utility Class - Comprehensive array operations for primitive and object arrays
 * 数组工具类 - 支持原始类型和对象数组的全面操作
 *
 * <p>Provides array creation, manipulation, search, conversion and collection operations.</p>
 * <p>提供数组创建、操作、搜索、转换和集合操作功能。参考 Apache Commons ArrayUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Array creation (of, newArray, nullToEmpty) - 数组创建</li>
 *   <li>Add/Insert/Remove operations - 添加/插入/删除操作</li>
 *   <li>Search (contains, indexOf, lastIndexOf) - 搜索</li>
 *   <li>Operations (reverse, shuffle, rotate, swap) - 操作</li>
 *   <li>Primitive/Wrapper conversion (toPrimitive, toObject) - 原始/包装类型转换</li>
 *   <li>Collection conversion (toList, toSet, toMap) - 集合转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create array - 创建数组
 * String[] arr = OpenArray.of("a", "b", "c");
 *
 * // Add element - 添加元素
 * String[] newArr = OpenArray.add(arr, "d");
 *
 * // Search - 搜索
 * boolean contains = OpenArray.contains(arr, "b");
 * int index = OpenArray.indexOf(arr, "b");
 *
 * // Convert - 转换
 * List<String> list = OpenArray.toList(arr);
 * int[] primitives = OpenArray.toPrimitive(integers);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenArray {

    private OpenArray() {
        // 工具类不可实例化
    }

    // ==================== 空数组常量（单例） ====================

    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    // ==================== 创建 ====================

    /**
     * Creates an array of the specified component type and length.
     * 创建指定类型和长度的数组
     *
     * @param componentType component type of the array | 数组元素类型
     * @param length        length of the array | 数组长度
     * @param <T>           element type | 元素类型
     * @return the newly created array | 新创建的数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
    }

    /**
     * Creates an array containing the specified elements.
     * 创建包含指定元素的数组
     *
     * @param elements elements to include | 元素
     * @param <T>      element type | 元素类型
     * @return the array | 数组
     */
    @SafeVarargs
    public static <T> T[] of(T... elements) {
        return elements;
    }

    /**
     * Converts a null array to an empty array of the specified type.
     * 将 null 数组转换为空数组
     *
     * @param array array, may be null | 数组
     * @param type  array type | 数组类型
     * @param <T>   element type | 元素类型
     * @return the original array, or an empty array if null | 非 null 数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] nullToEmpty(T[] array, Class<T[]> type) {
        if (array == null) {
            return (T[]) Array.newInstance(type.getComponentType(), 0);
        }
        return array;
    }

    /**
     * Converts a null int array to an empty int array.
     * 将 null int 数组转换为空数组
     *
     * @param array array, may be null | 数组
     * @return the original array, or an empty array if null | 非 null 数组
     */
    public static int[] nullToEmpty(int[] array) {
        return array == null ? EMPTY_INT_ARRAY : array;
    }

    /**
     * Converts a null long array to an empty long array.
     * 将 null long 数组转换为空数组
     *
     * @param array array, may be null | 数组
     * @return the original array, or an empty array if null | 非 null 数组
     */
    public static long[] nullToEmpty(long[] array) {
        return array == null ? EMPTY_LONG_ARRAY : array;
    }

    // ==================== 添加/插入 ====================

    /**
     * Appends an element to the end of the array.
     * 在数组末尾添加元素
     *
     * @param array   original array | 原数组
     * @param element element to add | 要添加的元素
     * @param <T>     element type | 元素类型
     * @return new array with the element appended | 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, T element) {
        Class<?> type = array != null ?
                array.getClass().getComponentType() :
                (element != null ? element.getClass() : Object.class);
        T[] newArray = (T[]) Array.newInstance(type, (array != null ? array.length : 0) + 1);
        if (array != null) {
            System.arraycopy(array, 0, newArray, 0, array.length);
        }
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * Inserts an element at the specified index.
     * 在指定位置插入元素
     *
     * @param array   original array | 原数组
     * @param index   insertion index | 插入位置
     * @param element element to insert | 要插入的元素
     * @param <T>     element type | 元素类型
     * @return new array with the element inserted | 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> type = array != null ?
                array.getClass().getComponentType() :
                (element != null ? element.getClass() : Object.class);
        int len = array != null ? array.length : 0;
        if (index < 0 || index > len) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + len);
        }
        T[] newArray = (T[]) Array.newInstance(type, len + 1);

        if (array != null && index > 0) {
            System.arraycopy(array, 0, newArray, 0, index);
        }
        newArray[index] = element;
        if (array != null && index < len) {
            System.arraycopy(array, index, newArray, index + 1, len - index);
        }
        return newArray;
    }

    /**
     * Concatenates two arrays into a new array.
     * 合并两个数组
     *
     * @param array1 first array | 数组1
     * @param array2 second array | 数组2
     * @param <T>    element type | 元素类型
     * @return merged array | 合并后的数组
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T[] addAll(T[] array1, T... array2) {
        if (array1 == null && array2 == null) {
            return null;
        }
        if (array1 == null) {
            return array2.clone();
        }
        if (array2 == null) {
            return array1.clone();
        }

        Class<?> type = array1.getClass().getComponentType();
        T[] result = (T[]) Array.newInstance(type, Math.addExact(array1.length, array2.length));
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * Inserts multiple elements at the specified index.
     * 在指定位置插入多个元素
     *
     * @param index  insertion index | 插入位置
     * @param array  original array | 原数组
     * @param values elements to insert | 要插入的元素
     * @param <T>    element type | 元素类型
     * @return new array with elements inserted | 新数组
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T[] insert(int index, T[] array, T... values) {
        if (array == null) {
            return values;
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        if (values == null || values.length == 0) {
            return array.clone();
        }

        Class<?> type = array.getClass().getComponentType();
        T[] result = (T[]) Array.newInstance(type, Math.addExact(array.length, values.length));

        if (index > 0) {
            System.arraycopy(array, 0, result, 0, Math.min(index, array.length));
        }
        System.arraycopy(values, 0, result, index, values.length);
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * Inserts multiple int elements at the specified index.
     * 在指定位置插入多个 int 元素
     *
     * @param index  insertion index | 插入位置
     * @param array  original array | 原数组
     * @param values elements to insert | 要插入的元素
     * @return new array with elements inserted | 新数组
     */
    public static int[] insert(int index, int[] array, int... values) {
        if (array == null) {
            return values;
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        if (values == null || values.length == 0) {
            return array.clone();
        }

        int[] result = new int[Math.addExact(array.length, values.length)];
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, Math.min(index, array.length));
        }
        System.arraycopy(values, 0, result, index, values.length);
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    // ==================== 删除 ====================

    /**
     * Removes the element at the specified index.
     * 删除指定索引的元素
     *
     * @param array original array | 原数组
     * @param index index to remove | 要删除的索引
     * @param <T>   element type | 元素类型
     * @return new array with the element removed | 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] remove(T[] array, int index) {
        if (array == null || array.length == 0) {
            return array;
        }
        int len = array.length;
        if (index < 0 || index >= len) {
            return array.clone();
        }

        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < len - 1) {
            System.arraycopy(array, index + 1, result, index, len - index - 1);
        }
        return result;
    }

    /**
     * Removes the first occurrence of the specified element.
     * 删除指定元素（首次出现）
     *
     * @param array   original array | 原数组
     * @param element element to remove | 要删除的元素
     * @param <T>     element type | 元素类型
     * @return new array with the element removed | 新数组
     */
    public static <T> T[] removeElement(T[] array, T element) {
        int index = indexOf(array, element);
        return index == -1 ? array : remove(array, index);
    }

    /**
     * Removes elements at multiple indices.
     * 删除多个索引位置的元素
     *
     * @param array   original array | 原数组
     * @param indices indices to remove | 要删除的索引
     * @param <T>     element type | 元素类型
     * @return new array with elements removed | 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] removeAll(T[] array, int... indices) {
        if (array == null || array.length == 0 || indices == null || indices.length == 0) {
            return array;
        }

        int[] sorted = indices.clone();
        Arrays.sort(sorted);

        // Deduplicate sorted indices
        int unique = 0;
        for (int i = 0; i < sorted.length; i++) {
            if (i == 0 || sorted[i] != sorted[i - 1]) {
                sorted[unique++] = sorted[i];
            }
        }

        int newLen = array.length - unique;
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), newLen);

        int srcPos = 0;
        int destPos = 0;
        int indexPos = 0;

        while (srcPos < array.length) {
            if (indexPos < unique && srcPos == sorted[indexPos]) {
                srcPos++;
                indexPos++;
            } else {
                result[destPos++] = array[srcPos++];
            }
        }
        return result;
    }

    /**
     * Removes elements at multiple indices from an int array.
     * 删除多个 int 数组索引位置的元素
     *
     * @param array   original array | 原数组
     * @param indices indices to remove | 要删除的索引
     * @return new array with elements removed | 新数组
     */
    public static int[] removeAll(int[] array, int... indices) {
        if (array == null || array.length == 0 || indices == null || indices.length == 0) {
            return array;
        }

        int[] sorted = indices.clone();
        Arrays.sort(sorted);

        // Deduplicate sorted indices
        int unique = 0;
        for (int i = 0; i < sorted.length; i++) {
            if (i == 0 || sorted[i] != sorted[i - 1]) {
                sorted[unique++] = sorted[i];
            }
        }

        int[] result = new int[array.length - unique];
        int srcPos = 0;
        int destPos = 0;
        int indexPos = 0;

        while (srcPos < array.length) {
            if (indexPos < unique && srcPos == sorted[indexPos]) {
                srcPos++;
                indexPos++;
            } else {
                result[destPos++] = array[srcPos++];
            }
        }
        return result;
    }

    // ==================== 子数组 ====================

    /**
     * Returns a subarray from startInclusive (inclusive) to endExclusive (exclusive).
     * 获取子数组
     *
     * @param array          original array | 原数组
     * @param startInclusive start index, inclusive | 起始索引（包含）
     * @param endExclusive   end index, exclusive | 结束索引（不包含）
     * @param <T>            element type | 元素类型
     * @return the subarray | 子数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] subarray(T[] array, int startInclusive, int endExclusive) {
        if (array == null) {
            return null;
        }
        if (startInclusive < 0) {
            startInclusive = 0;
        }
        if (endExclusive > array.length) {
            endExclusive = array.length;
        }
        int newLen = endExclusive - startInclusive;
        if (newLen <= 0) {
            return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }

        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), newLen);
        System.arraycopy(array, startInclusive, result, 0, newLen);
        return result;
    }

    /**
     * Returns an int subarray from startInclusive to endExclusive.
     * 获取 int 子数组
     */
    public static int[] subarray(int[] array, int startInclusive, int endExclusive) {
        if (array == null) {
            return null;
        }
        if (startInclusive < 0) {
            startInclusive = 0;
        }
        if (endExclusive > array.length) {
            endExclusive = array.length;
        }
        int newLen = endExclusive - startInclusive;
        if (newLen <= 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] result = new int[newLen];
        System.arraycopy(array, startInclusive, result, 0, newLen);
        return result;
    }

    /**
     * Returns a long subarray from startInclusive to endExclusive.
     * 获取 long 子数组
     */
    public static long[] subarray(long[] array, int startInclusive, int endExclusive) {
        if (array == null) {
            return null;
        }
        if (startInclusive < 0) {
            startInclusive = 0;
        }
        if (endExclusive > array.length) {
            endExclusive = array.length;
        }
        int newLen = endExclusive - startInclusive;
        if (newLen <= 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] result = new long[newLen];
        System.arraycopy(array, startInclusive, result, 0, newLen);
        return result;
    }

    /**
     * Returns a byte subarray from startInclusive to endExclusive.
     * 获取 byte 子数组
     */
    public static byte[] subarray(byte[] array, int startInclusive, int endExclusive) {
        if (array == null) {
            return null;
        }
        if (startInclusive < 0) {
            startInclusive = 0;
        }
        if (endExclusive > array.length) {
            endExclusive = array.length;
        }
        int newLen = endExclusive - startInclusive;
        if (newLen <= 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] result = new byte[newLen];
        System.arraycopy(array, startInclusive, result, 0, newLen);
        return result;
    }

    // ==================== 交换 ====================

    /**
     * Swaps two elements in the array.
     * 交换数组中两个位置的元素
     *
     * @param array   the array | 数组
     * @param offset1 first position | 位置1
     * @param offset2 second position | 位置2
     */
    public static void swap(Object[] array, int offset1, int offset2) {
        if (array == null || array.length < 2) {
            return;
        }
        Object temp = array[offset1];
        array[offset1] = array[offset2];
        array[offset2] = temp;
    }

    /**
     * Swaps two elements in an int array.
     * 交换 int 数组中两个位置的元素
     */
    public static void swap(int[] array, int offset1, int offset2) {
        if (array == null || array.length < 2) {
            return;
        }
        int temp = array[offset1];
        array[offset1] = array[offset2];
        array[offset2] = temp;
    }

    /**
     * Swaps a series of elements of the given length starting at two offsets.
     * 交换数组中指定长度的元素
     *
     * @param array   the array | 数组
     * @param offset1 first offset | 位置1
     * @param offset2 second offset | 位置2
     * @param len     number of elements to swap | 交换长度
     */
    public static void swap(Object[] array, int offset1, int offset2, int len) {
        if (array == null || array.length < 2 || len <= 0) {
            return;
        }
        for (int i = 0; i < len; i++) {
            int pos1 = offset1 + i;
            int pos2 = offset2 + i;
            if (pos1 < array.length && pos2 < array.length) {
                Object temp = array[pos1];
                array[pos1] = array[pos2];
                array[pos2] = temp;
            }
        }
    }

    // ==================== 操作 ====================

    /**
     * Reverses the order of the elements in the array.
     * 反转数组
     *
     * @param array the array to reverse | 数组
     * @param <T>   element type | 元素类型
     */
    public static <T> void reverse(T[] array) {
        if (array == null || array.length < 2) {
            return;
        }
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            T temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * Reverses the order of the elements in an int array.
     * 反转 int 数组
     */
    public static void reverse(int[] array) {
        if (array == null || array.length < 2) {
            return;
        }
        int left = 0;
        int right = array.length - 1;
        while (left < right) {
            int temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * Randomly shuffles the elements of the array.
     * 打乱数组
     *
     * @param array the array to shuffle | 数组
     * @param <T>   element type | 元素类型
     */
    public static <T> void shuffle(T[] array) {
        if (array == null || array.length < 2) {
            return;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Rotates the elements of the array by the given distance (positive = right, negative = left).
     * 旋转数组
     *
     * @param array    the array to rotate | 数组
     * @param distance rotation distance (positive = right, negative = left) | 旋转距离（正数向右，负数向左）
     */
    public static void rotate(Object[] array, int distance) {
        if (array == null || array.length < 2) {
            return;
        }
        int len = array.length;
        distance = distance % len;
        if (distance < 0) {
            distance += len;
        }
        if (distance == 0) {
            return;
        }

        Object[] temp = new Object[len];
        for (int i = 0; i < len; i++) {
            temp[(i + distance) % len] = array[i];
        }
        System.arraycopy(temp, 0, array, 0, len);
    }

    // ==================== 搜索 ====================

    /**
     * Returns true if the array contains the specified element.
     * 检查数组是否包含指定元素
     *
     * @param array   the array | 数组
     * @param element element to search for | 元素
     * @param <T>     element type | 元素类型
     * @return true if found | 如果包含返回 true
     */
    public static <T> boolean contains(T[] array, T element) {
        return indexOf(array, element) >= 0;
    }

    /**
     * Returns true if an int array contains the specified element.
     * 检查 int 数组是否包含指定元素
     */
    public static boolean contains(int[] array, int element) {
        if (array == null) {
            return false;
        }
        for (int i : array) {
            if (i == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the first occurrence of the element in the array.
     * 查找元素索引
     *
     * @param array   the array | 数组
     * @param element element to search for | 元素
     * @param <T>     element type | 元素类型
     * @return the index, or -1 if not found | 索引，未找到返回 -1
     */
    public static <T> int indexOf(T[] array, T element) {
        return indexOf(array, element, 0);
    }

    /**
     * Returns the index of the first occurrence of the element starting from startIndex.
     * 从指定位置开始查找元素索引
     *
     * @param array      the array | 数组
     * @param element    element to search for | 元素
     * @param startIndex start index | 起始索引
     * @param <T>        element type | 元素类型
     * @return the index, or -1 if not found | 索引，未找到返回 -1
     */
    public static <T> int indexOf(T[] array, T element, int startIndex) {
        if (array == null) {
            return -1;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (Objects.equals(array[i], element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the element in the array.
     * 从后向前查找元素索引
     *
     * @param array   the array | 数组
     * @param element element to search for | 元素
     * @param <T>     element type | 元素类型
     * @return the index, or -1 if not found | 索引，未找到返回 -1
     */
    public static <T> int lastIndexOf(T[] array, T element) {
        if (array == null) {
            return -1;
        }
        for (int i = array.length - 1; i >= 0; i--) {
            if (Objects.equals(array[i], element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the first element of the array wrapped in an Optional.
     * 获取数组第一个元素
     *
     * @param array the array | 数组
     * @param <T>   element type | 元素类型
     * @return Optional wrapping the first element | Optional 包装的第一个元素
     */
    public static <T> Optional<T> getFirst(T[] array) {
        if (array == null || array.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(array[0]);
    }

    /**
     * Returns the last element of the array wrapped in an Optional.
     * 获取数组最后一个元素
     *
     * @param array the array | 数组
     * @param <T>   element type | 元素类型
     * @return Optional wrapping the last element | Optional 包装的最后一个元素
     */
    public static <T> Optional<T> getLast(T[] array) {
        if (array == null || array.length == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(array[array.length - 1]);
    }

    // ==================== 判断 ====================

    /**
     * Returns true if the array is null or has length zero.
     * 检查数组是否为空
     *
     * @param array the array | 数组
     * @return true if null or empty | 如果为 null 或长度为 0 返回 true
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns true if an int array is null or has length zero.
     * 检查 int 数组是否为空
     */
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns true if the array is not null and has at least one element.
     * 检查数组是否非空
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * Returns true if both arrays have the same length.
     * 检查两个数组长度是否相同
     *
     * @param array1 first array | 数组1
     * @param array2 second array | 数组2
     * @return true if same length | 如果长度相同返回 true
     */
    public static boolean isSameLength(Object[] array1, Object[] array2) {
        int len1 = array1 != null ? array1.length : 0;
        int len2 = array2 != null ? array2.length : 0;
        return len1 == len2;
    }

    /**
     * Returns true if the int array is sorted in ascending order.
     * 检查 int 数组是否已排序
     *
     * @param array the array | 数组
     * @return true if sorted in ascending order | 如果已升序排序返回 true
     */
    public static boolean isSorted(int[] array) {
        if (array == null || array.length < 2) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the Comparable array is sorted in ascending order.
     * 检查 Comparable 数组是否已排序
     *
     * @param array the array | 数组
     * @param <T>   element type | 元素类型
     * @return true if sorted in ascending order | 如果已升序排序返回 true
     */
    public static <T extends Comparable<T>> boolean isSorted(T[] array) {
        if (array == null || array.length < 2) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i] == null || array[i - 1] == null) {
                continue;
            }
            if (array[i].compareTo(array[i - 1]) < 0) {
                return false;
            }
        }
        return true;
    }

    // ==================== 原始类型/包装类型转换 ====================

    /**
     * Converts an Integer array to an int array.
     * Integer 数组转 int 数组
     */
    public static int[] toPrimitive(Integer[] array) {
        return toPrimitive(array, 0);
    }

    /**
     * Converts an Integer array to an int array with a specified value for null elements.
     * Integer 数组转 int 数组（指定 null 替代值）
     */
    public static int[] toPrimitive(Integer[] array, int valueForNull) {
        if (array == null) {
            return null;
        }
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : valueForNull;
        }
        return result;
    }

    /**
     * Converts a Long array to a long array.
     * Long 数组转 long 数组
     */
    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0L;
        }
        return result;
    }

    /**
     * Converts a Double array to a double array.
     * Double 数组转 double 数组
     */
    public static double[] toPrimitive(Double[] array) {
        if (array == null) {
            return null;
        }
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0.0;
        }
        return result;
    }

    /**
     * Converts a Boolean array to a boolean array.
     * Boolean 数组转 boolean 数组
     */
    public static boolean[] toPrimitive(Boolean[] array) {
        if (array == null) {
            return null;
        }
        boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null && array[i];
        }
        return result;
    }

    /**
     * Converts a Byte array to a byte array.
     * Byte 数组转 byte 数组
     */
    public static byte[] toPrimitive(Byte[] array) {
        if (array == null) {
            return null;
        }
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0;
        }
        return result;
    }

    /**
     * Converts a Character array to a char array.
     * Character 数组转 char 数组
     */
    public static char[] toPrimitive(Character[] array) {
        if (array == null) {
            return null;
        }
        char[] result = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] != null ? array[i] : 0;
        }
        return result;
    }

    /**
     * Converts an int array to an Integer array.
     * int 数组转 Integer 数组
     */
    public static Integer[] toObject(int[] array) {
        if (array == null) {
            return null;
        }
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a long array to a Long array.
     * long 数组转 Long 数组
     */
    public static Long[] toObject(long[] array) {
        if (array == null) {
            return null;
        }
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a double array to a Double array.
     * double 数组转 Double 数组
     */
    public static Double[] toObject(double[] array) {
        if (array == null) {
            return null;
        }
        Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a boolean array to a Boolean array.
     * boolean 数组转 Boolean 数组
     */
    public static Boolean[] toObject(boolean[] array) {
        if (array == null) {
            return null;
        }
        Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a byte array to a Byte array.
     * byte 数组转 Byte 数组
     */
    public static Byte[] toObject(byte[] array) {
        if (array == null) {
            return null;
        }
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts a char array to a Character array.
     * char 数组转 Character 数组
     */
    public static Character[] toObject(char[] array) {
        if (array == null) {
            return null;
        }
        Character[] result = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    // ==================== 集合转换 ====================

    /**
     * Converts an array to a mutable List.
     * 数组转 List
     *
     * @param array elements | 数组
     * @param <T>   element type | 元素类型
     * @return mutable List | 可变 List
     */
    @SafeVarargs
    public static <T> List<T> toList(T... array) {
        if (array == null || array.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Converts an array to a Set.
     * 数组转 Set
     *
     * @param array elements | 数组
     * @param <T>   element type | 元素类型
     * @return Set containing all elements | Set
     */
    @SafeVarargs
    public static <T> Set<T> toSet(T... array) {
        if (array == null || array.length == 0) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * Converts a two-dimensional array to a Map. Each sub-array must contain at least two elements: [key, value].
     * 二维数组转 Map
     * <p>
     * 每个子数组必须包含至少两个元素：[key, value]
     *
     * @param array two-dimensional array | 二维数组
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return Map | Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toMap(Object[][] array) {
        if (array == null || array.length == 0) {
            return new HashMap<>();
        }
        Map<K, V> map = new HashMap<>(array.length);
        for (Object[] entry : array) {
            if (entry != null && entry.length >= 2) {
                map.put((K) entry[0], (V) entry[1]);
            }
        }
        return map;
    }

    // ==================== 函数式操作 ====================

    /**
     * Filters the array, returning a new array containing only elements matching the predicate.
     * 过滤数组
     *
     * @param array     the array | 数组
     * @param predicate filter condition | 过滤条件
     * @param <T>       element type | 元素类型
     * @return filtered array | 过滤后的数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] filter(T[] array, Predicate<T> predicate) {
        if (array == null || array.length == 0) {
            return array;
        }
        List<T> list = new ArrayList<>();
        for (T element : array) {
            if (predicate.test(element)) {
                list.add(element);
            }
        }
        return list.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), list.size()));
    }

    /**
     * Maps each element of the array using the given mapper function.
     * 映射数组
     *
     * @param array      original array | 原数组
     * @param mapper     mapping function | 映射函数
     * @param targetType target element type | 目标类型
     * @param <T>        source element type | 原元素类型
     * @param <R>        target element type | 目标元素类型
     * @return mapped array | 映射后的数组
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R[] map(T[] array, Function<T, R> mapper, Class<R> targetType) {
        if (array == null) {
            return null;
        }
        R[] result = (R[]) Array.newInstance(targetType, array.length);
        for (int i = 0; i < array.length; i++) {
            result[i] = mapper.apply(array[i]);
        }
        return result;
    }
}
