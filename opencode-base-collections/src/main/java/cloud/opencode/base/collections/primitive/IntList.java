package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * IntList - Primitive int List
 * IntList - 原始 int 列表
 *
 * <p>A resizable array implementation for primitive int values.
 * Avoids boxing overhead of Integer objects.</p>
 * <p>原始 int 值的可调整大小数组实现。避免 Integer 对象的装箱开销。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No boxing overhead - 无装箱开销</li>
 *   <li>Memory efficient - 内存高效</li>
 *   <li>Random access - 随机访问</li>
 *   <li>Resizable - 可调整大小</li>
 *   <li>Stream support - 流支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty - 创建空
 * IntList list = IntList.create();
 *
 * // Create with capacity - 创建指定容量
 * IntList list = IntList.create(100);
 *
 * // Create from values - 从值创建
 * IntList list = IntList.of(1, 2, 3, 4, 5);
 *
 * // Operations - 操作
 * list.add(10);
 * list.addAll(1, 2, 3);
 * int value = list.get(0);
 * list.set(0, 100);
 *
 * // Stream - 流
 * int sum = list.stream().sum();
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
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class IntList implements Serializable, Iterable<Integer> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 10;

    private int[] elements;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param initialCapacity initial capacity | 初始容量
     */
    private IntList(int initialCapacity) {
        this.elements = new int[initialCapacity];
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty IntList.
     * 创建空 IntList。
     *
     * @return new IntList | 新的 IntList
     */
    public static IntList create() {
        return new IntList(DEFAULT_CAPACITY);
    }

    /**
     * Create an IntList with initial capacity.
     * 创建指定初始容量的 IntList。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new IntList | 新的 IntList
     */
    public static IntList create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + initialCapacity);
        }
        return new IntList(initialCapacity);
    }

    /**
     * Create an IntList from values.
     * 从值创建 IntList。
     *
     * @param values the values | 值
     * @return new IntList | 新的 IntList
     */
    public static IntList of(int... values) {
        IntList list = new IntList(values.length);
        list.elements = values.clone();
        list.size = values.length;
        return list;
    }

    /**
     * Create an IntList from a range.
     * 从范围创建 IntList。
     *
     * @param start start (inclusive) | 开始（包含）
     * @param end   end (exclusive) | 结束（不包含）
     * @return new IntList | 新的 IntList
     */
    public static IntList range(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        IntList list = new IntList(end - start);
        for (int i = start; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    // ==================== 基本操作 | Basic Operations ====================

    /**
     * Return the size.
     * 返回大小。
     *
     * @return size | 大小
     */
    public int size() {
        return size;
    }

    /**
     * Check if empty.
     * 检查是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get element at index.
     * 获取索引处的元素。
     *
     * @param index the index | 索引
     * @return element | 元素
     */
    public int get(int index) {
        checkIndex(index);
        return elements[index];
    }

    /**
     * Set element at index.
     * 设置索引处的元素。
     *
     * @param index the index | 索引
     * @param value the value | 值
     * @return old value | 旧值
     */
    public int set(int index, int value) {
        checkIndex(index);
        int old = elements[index];
        elements[index] = value;
        return old;
    }

    /**
     * Add element.
     * 添加元素。
     *
     * @param value the value | 值
     */
    public void add(int value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    /**
     * Add element at index.
     * 在索引处添加元素。
     *
     * @param index the index | 索引
     * @param value the value | 值
     */
    public void add(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = value;
        size++;
    }

    /**
     * Add all values.
     * 添加所有值。
     *
     * @param values the values | 值
     */
    public void addAll(int... values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, elements, size, values.length);
        size += values.length;
    }

    /**
     * Remove element at index.
     * 移除索引处的元素。
     *
     * @param index the index | 索引
     * @return removed value | 移除的值
     */
    public int removeAt(int index) {
        checkIndex(index);
        int old = elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        size--;
        return old;
    }

    /**
     * Remove first occurrence of value.
     * 移除第一次出现的值。
     *
     * @param value the value | 值
     * @return true if removed | 如果移除则返回 true
     */
    public boolean remove(int value) {
        int index = indexOf(value);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }

    /**
     * Clear all elements.
     * 清除所有元素。
     */
    public void clear() {
        size = 0;
    }

    // ==================== 查找操作 | Search Operations ====================

    /**
     * Check if contains value.
     * 检查是否包含值。
     *
     * @param value the value | 值
     * @return true if contains | 如果包含则返回 true
     */
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    /**
     * Find index of value.
     * 查找值的索引。
     *
     * @param value the value | 值
     * @return index, or -1 | 索引，或 -1
     */
    public int indexOf(int value) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find last index of value.
     * 查找值的最后索引。
     *
     * @param value the value | 值
     * @return index, or -1 | 索引，或 -1
     */
    public int lastIndexOf(int value) {
        for (int i = size - 1; i >= 0; i--) {
            if (elements[i] == value) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 统计操作 | Statistics Operations ====================

    /**
     * Return the sum.
     * 返回总和。
     *
     * @return sum | 总和
     */
    public long sum() {
        long sum = 0;
        for (int i = 0; i < size; i++) {
            sum += elements[i];
        }
        return sum;
    }

    /**
     * Return the minimum.
     * 返回最小值。
     *
     * @return minimum | 最小值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public int min() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        int min = elements[0];
        for (int i = 1; i < size; i++) {
            if (elements[i] < min) {
                min = elements[i];
            }
        }
        return min;
    }

    /**
     * Return the maximum.
     * 返回最大值。
     *
     * @return maximum | 最大值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public int max() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        int max = elements[0];
        for (int i = 1; i < size; i++) {
            if (elements[i] > max) {
                max = elements[i];
            }
        }
        return max;
    }

    /**
     * Return the average.
     * 返回平均值。
     *
     * @return average | 平均值
     * @throws NoSuchElementException if empty | 如果为空
     */
    public double average() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return (double) sum() / size;
    }

    // ==================== 转换 | Conversion ====================

    /**
     * Return as array.
     * 返回数组。
     *
     * @return array | 数组
     */
    public int[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    /**
     * Return as stream.
     * 返回流。
     *
     * @return stream | 流
     */
    public IntStream stream() {
        return StreamSupport.intStream(
                Spliterators.spliterator(elements, 0, size, Spliterator.ORDERED),
                false
        );
    }

    /**
     * Sort in place.
     * 就地排序。
     */
    public void sort() {
        Arrays.sort(elements, 0, size);
    }

    /**
     * Reverse in place.
     * 就地反转。
     */
    public void reverse() {
        for (int i = 0, j = size - 1; i < j; i++, j--) {
            int temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
        }
    }

    // ==================== 迭代器 | Iterator ====================

    /**
     * Return primitive iterator.
     * 返回原始迭代器。
     *
     * @return primitive iterator | 原始迭代器
     */
    public PrimitiveIterator.OfInt primitiveIterator() {
        return new PrimitiveIterator.OfInt() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public int nextInt() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    @Override
    public java.util.Iterator<Integer> iterator() {
        return new java.util.Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    /**
     * ForEach with primitive consumer.
     * 使用原始消费者的 forEach。
     *
     * @param action the action | 动作
     */
    public void forEach(IntConsumer action) {
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
            int grown = elements.length + (elements.length >> 1); // 1.5x growth, avoids overflow
            int newCapacity = Math.max(grown, minCapacity);
            if (newCapacity < 0) { // overflow
                if (minCapacity < 0) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                newCapacity = Integer.MAX_VALUE - 8;
            }
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntList that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < size; i++) {
            if (elements[i] != that.elements[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            result = 31 * result + elements[i];
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
}
