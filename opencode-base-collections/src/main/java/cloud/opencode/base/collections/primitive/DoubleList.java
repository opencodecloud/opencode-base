package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * DoubleList - Primitive double List
 * DoubleList - 原始 double 列表
 *
 * <p>A resizable array implementation for primitive double values.
 * Avoids boxing overhead of Double objects.</p>
 * <p>原始 double 值的可调整大小数组实现。避免 Double 对象的装箱开销。</p>
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
 * DoubleList list = DoubleList.create();
 *
 * // Create from values - 从值创建
 * DoubleList list = DoubleList.of(1.0, 2.0, 3.0);
 *
 * // Operations - 操作
 * list.add(10.5);
 * double value = list.get(0);
 *
 * // Statistics - 统计
 * double sum = list.sum();
 * double avg = list.average();
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
public final class DoubleList implements Serializable, Iterable<Double> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 10;

    private double[] elements;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private DoubleList(int initialCapacity) {
        this.elements = new double[initialCapacity];
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty DoubleList.
     * 创建空 DoubleList。
     *
     * @return new DoubleList | 新的 DoubleList
     */
    public static DoubleList create() {
        return new DoubleList(DEFAULT_CAPACITY);
    }

    /**
     * Create a DoubleList with initial capacity.
     * 创建指定初始容量的 DoubleList。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new DoubleList | 新的 DoubleList
     */
    public static DoubleList create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + initialCapacity);
        }
        return new DoubleList(initialCapacity);
    }

    /**
     * Create a DoubleList from values.
     * 从值创建 DoubleList。
     *
     * @param values the values | 值
     * @return new DoubleList | 新的 DoubleList
     */
    public static DoubleList of(double... values) {
        DoubleList list = new DoubleList(values.length);
        list.elements = values.clone();
        list.size = values.length;
        return list;
    }

    // ==================== 基本操作 | Basic Operations ====================

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public double get(int index) {
        checkIndex(index);
        return elements[index];
    }

    public double set(int index, double value) {
        checkIndex(index);
        double old = elements[index];
        elements[index] = value;
        return old;
    }

    public void add(double value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    public void add(int index, double value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = value;
        size++;
    }

    public void addAll(double... values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, elements, size, values.length);
        size += values.length;
    }

    public double removeAt(int index) {
        checkIndex(index);
        double old = elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        size--;
        return old;
    }

    public boolean remove(double value) {
        int index = indexOf(value);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }

    public void clear() {
        size = 0;
    }

    // ==================== 查找操作 | Search Operations ====================

    public boolean contains(double value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(double value) {
        for (int i = 0; i < size; i++) {
            if (Double.compare(elements[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(double value) {
        for (int i = size - 1; i >= 0; i--) {
            if (Double.compare(elements[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 统计操作 | Statistics Operations ====================

    public double sum() {
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += elements[i];
        }
        return sum;
    }

    public double min() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        double min = elements[0];
        for (int i = 1; i < size; i++) {
            if (elements[i] < min) {
                min = elements[i];
            }
        }
        return min;
    }

    public double max() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        double max = elements[0];
        for (int i = 1; i < size; i++) {
            if (elements[i] > max) {
                max = elements[i];
            }
        }
        return max;
    }

    public double average() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return sum() / size;
    }

    /**
     * Return the variance.
     * 返回方差。
     *
     * @return variance | 方差
     * @throws NoSuchElementException if empty | 如果为空
     */
    public double variance() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        double mean = average();
        double sumSquares = 0;
        for (int i = 0; i < size; i++) {
            double diff = elements[i] - mean;
            sumSquares += diff * diff;
        }
        return sumSquares / size;
    }

    /**
     * Return the standard deviation.
     * 返回标准差。
     *
     * @return standard deviation | 标准差
     * @throws NoSuchElementException if empty | 如果为空
     */
    public double standardDeviation() {
        return Math.sqrt(variance());
    }

    // ==================== 转换 | Conversion ====================

    public double[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    public DoubleStream stream() {
        return StreamSupport.doubleStream(
                Spliterators.spliterator(elements, 0, size, Spliterator.ORDERED),
                false
        );
    }

    public void sort() {
        Arrays.sort(elements, 0, size);
    }

    public void reverse() {
        for (int i = 0, j = size - 1; i < j; i++, j--) {
            double temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
        }
    }

    // ==================== 迭代器 | Iterator ====================

    public PrimitiveIterator.OfDouble primitiveIterator() {
        return new PrimitiveIterator.OfDouble() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public double nextDouble() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    @Override
    public java.util.Iterator<Double> iterator() {
        return new java.util.Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public Double next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    public void forEach(DoubleConsumer action) {
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
        if (!(o instanceof DoubleList that)) return false;
        if (size != that.size) return false;
        for (int i = 0; i < size; i++) {
            if (Double.compare(elements[i], that.elements[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            long bits = Double.doubleToLongBits(elements[i]);
            result = 31 * result + (int) (bits ^ (bits >>> 32));
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
