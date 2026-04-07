package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * FloatList - Primitive float List
 * FloatList - 原始 float 列表
 *
 * <p>A resizable array implementation for primitive float values.
 * Avoids boxing overhead of Float objects.</p>
 * <p>原始 float 值的可调整大小数组实现。避免 Float 对象的装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No boxing overhead - 无装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Random access - 随机访问</li>
 *   <li>Resizable - 可调整大小</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空
 * FloatList list = FloatList.create();
 *
 * // Create from values - 从值创建
 * FloatList list = FloatList.of(1.0f, 2.0f, 3.0f);
 *
 * // Operations - 操作
 * list.add(10.5f);
 * float value = list.get(0);
 *
 * // Statistics - 统计
 * float sum = list.sum();
 * float avg = list.average();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get/set: O(1) - get/set: O(1)</li>
 *   <li>add: O(1) amortized - add: O(1) 均摊</li>
 *   <li>contains: O(n) - contains: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: N/A (primitive) - 空值安全: 不适用（原始类型）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class FloatList implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 10;

    private float[] elements;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private FloatList(int initialCapacity) {
        this.elements = new float[initialCapacity];
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty FloatList.
     * 创建空 FloatList。
     *
     * @return new FloatList | 新的 FloatList
     */
    public static FloatList create() {
        return new FloatList(DEFAULT_CAPACITY);
    }

    /**
     * Create a FloatList with initial capacity.
     * 创建指定初始容量的 FloatList。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new FloatList | 新的 FloatList
     * @throws IllegalArgumentException if capacity is negative | 如果容量为负
     */
    public static FloatList create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + initialCapacity);
        }
        return new FloatList(initialCapacity);
    }

    /**
     * Create a FloatList from values.
     * 从值创建 FloatList。
     *
     * @param values the values | 值
     * @return new FloatList | 新的 FloatList
     */
    public static FloatList of(float... values) {
        FloatList list = new FloatList(values.length);
        list.elements = values.clone();
        list.size = values.length;
        return list;
    }

    /**
     * Create a FloatList from a float array.
     * 从 float 数组创建 FloatList。
     *
     * @param array the source array | 源数组
     * @return new FloatList | 新的 FloatList
     */
    public static FloatList from(float[] array) {
        return of(array);
    }

    /**
     * Create a FloatList from a Collection of Float.
     * 从 Float 集合创建 FloatList。
     *
     * @param collection the source collection | 源集合
     * @return new FloatList | 新的 FloatList
     * @throws NullPointerException if collection contains null elements | 如果集合包含 null 元素
     */
    public static FloatList from(Collection<Float> collection) {
        FloatList list = new FloatList(collection.size());
        for (Float value : collection) {
            list.add(value);
        }
        return list;
    }

    // ==================== 基本操作 | Basic Operations ====================

    /**
     * Return the size of this list.
     * 返回此列表的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if this list is empty.
     * 检查此列表是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get the element at the specified index.
     * 获取指定索引处的元素。
     *
     * @param index the index | 索引
     * @return the element | 元素
     * @throws IndexOutOfBoundsException if index is out of range | 如果索引超出范围
     */
    public float get(int index) {
        checkIndex(index);
        return elements[index];
    }

    /**
     * Set the element at the specified index.
     * 设置指定索引处的元素。
     *
     * @param index the index | 索引
     * @param value the new value | 新值
     * @return the old value | 旧值
     * @throws IndexOutOfBoundsException if index is out of range | 如果索引超出范围
     */
    public float set(int index, float value) {
        checkIndex(index);
        float old = elements[index];
        elements[index] = value;
        return old;
    }

    /**
     * Add a value to the end of this list.
     * 向此列表末尾添加值。
     *
     * @param value the value | 值
     */
    public void add(float value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    /**
     * Add all values to the end of this list.
     * 向此列表末尾添加所有值。
     *
     * @param values the values | 值
     */
    public void addAll(float... values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, elements, size, values.length);
        size += values.length;
    }

    /**
     * Clear this list.
     * 清空此列表。
     */
    public void clear() {
        size = 0;
    }

    // ==================== 查找操作 | Search Operations ====================

    /**
     * Check if this list contains the value.
     * 检查此列表是否包含该值。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    public boolean contains(float value) {
        return indexOf(value) >= 0;
    }

    /**
     * Return the index of the first occurrence of the value.
     * 返回值首次出现的索引。
     *
     * @param value the value | 值
     * @return the index, or -1 if not found | 索引，未找到则返回 -1
     */
    public int indexOf(float value) {
        for (int i = 0; i < size; i++) {
            if (Float.compare(elements[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 统计操作 | Statistics Operations ====================

    /**
     * Return the sum of all elements.
     * 返回所有元素的和。
     *
     * @return the sum | 和
     */
    public float sum() {
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum += elements[i];
        }
        return (float) sum;
    }

    /**
     * Return the minimum element.
     * 返回最小元素。
     *
     * @return the minimum | 最小值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public float min() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        float min = elements[0];
        for (int i = 1; i < size; i++) {
            if (Float.compare(elements[i], min) < 0) {
                min = elements[i];
            }
        }
        return min;
    }

    /**
     * Return the maximum element.
     * 返回最大元素。
     *
     * @return the maximum | 最大值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public float max() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        float max = elements[0];
        for (int i = 1; i < size; i++) {
            if (Float.compare(elements[i], max) > 0) {
                max = elements[i];
            }
        }
        return max;
    }

    /**
     * Return the average of all elements.
     * 返回所有元素的平均值。
     *
     * @return the average | 平均值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public float average() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return sum() / size;
    }

    // ==================== 转换 | Conversion ====================

    /**
     * Return all elements as a float array.
     * 以 float 数组形式返回所有元素。
     *
     * @return the float array | float 数组
     */
    public float[] toFloatArray() {
        return Arrays.copyOf(elements, size);
    }

    /**
     * Return all elements as a boxed List.
     * 以装箱 List 形式返回所有元素。
     *
     * @return the boxed list | 装箱列表
     */
    public List<Float> toList() {
        List<Float> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(elements[i]);
        }
        return result;
    }

    /**
     * Sort the elements in ascending order.
     * 按升序排序元素。
     */
    public void sort() {
        Arrays.sort(elements, 0, size);
    }

    /**
     * Reverse the order of elements.
     * 反转元素顺序。
     */
    public void reverse() {
        for (int i = 0, j = size - 1; i < j; i++, j--) {
            float temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
        }
    }

    /**
     * Return a sub-list view as a new FloatList.
     * 返回子列表（新 FloatList）。
     *
     * @param fromIndex start index (inclusive) | 起始索引（包含）
     * @param toIndex   end index (exclusive) | 结束索引（不包含）
     * @return the sub-list | 子列表
     * @throws IndexOutOfBoundsException if indices are out of range | 如果索引超出范围
     */
    public FloatList subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException(
                    "fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", Size: " + size);
        }
        int len = toIndex - fromIndex;
        FloatList result = new FloatList(len);
        System.arraycopy(elements, fromIndex, result.elements, 0, len);
        result.size = len;
        return result;
    }

    // ==================== 迭代 | Iteration ====================

    /**
     * Apply action to each element.
     * 对每个元素应用操作。
     *
     * @param action the action | 操作
     */
    public void forEach(FloatConsumer action) {
        for (int i = 0; i < size; i++) {
            action.accept(elements[i]);
        }
    }

    // ==================== 内部方法 | Internal Methods ====================

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            long expanded = (long) elements.length * 2;
            int newCapacity = (int) Math.max(Math.min(expanded, Integer.MAX_VALUE - 8), minCapacity);
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FloatList that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < size; i++) {
            if (Float.compare(elements[i], that.elements[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            result = 31 * result + Float.floatToIntBits(elements[i]);
        }
        return result;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(elements[i]);
        }
        return sb.append("]").toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Functional interface for float consumer.
     * float 消费者函数式接口。
     */
    @FunctionalInterface
    public interface FloatConsumer {
        /**
         * Accept a float value.
         * 接受一个 float 值。
         *
         * @param value the value | 值
         */
        void accept(float value);
    }
}
