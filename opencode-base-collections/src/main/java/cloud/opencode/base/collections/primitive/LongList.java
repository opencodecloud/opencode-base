package cloud.opencode.base.collections.primitive;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

/**
 * LongList - Primitive long List
 * LongList - 原始 long 列表
 *
 * <p>A resizable array implementation for primitive long values.
 * Avoids boxing overhead of Long objects.</p>
 * <p>原始 long 值的可调整大小数组实现。避免 Long 对象的装箱开销。</p>
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
 * LongList list = LongList.create();
 *
 * // Create from values - 从值创建
 * LongList list = LongList.of(1L, 2L, 3L, 4L, 5L);
 *
 * // Operations - 操作
 * list.add(10L);
 * long value = list.get(0);
 *
 * // Stream - 流
 * long sum = list.stream().sum();
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
public final class LongList implements Serializable, Iterable<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_CAPACITY = 10;

    private long[] elements;
    private int size;

    // ==================== 构造方法 | Constructors ====================

    private LongList(int initialCapacity) {
        this.elements = new long[initialCapacity];
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LongList.
     * 创建空 LongList。
     *
     * @return new LongList | 新的 LongList
     */
    public static LongList create() {
        return new LongList(DEFAULT_CAPACITY);
    }

    /**
     * Create a LongList with initial capacity.
     * 创建指定初始容量的 LongList。
     *
     * @param initialCapacity initial capacity | 初始容量
     * @return new LongList | 新的 LongList
     */
    public static LongList create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + initialCapacity);
        }
        return new LongList(initialCapacity);
    }

    /**
     * Create a LongList from values.
     * 从值创建 LongList。
     *
     * @param values the values | 值
     * @return new LongList | 新的 LongList
     */
    public static LongList of(long... values) {
        LongList list = new LongList(values.length);
        list.elements = values.clone();
        list.size = values.length;
        return list;
    }

    /**
     * Create a LongList from a range.
     * 从范围创建 LongList。
     *
     * @param start start (inclusive) | 开始（包含）
     * @param end   end (exclusive) | 结束（不包含）
     * @return new LongList | 新的 LongList
     */
    public static LongList range(long start, long end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        LongList list = new LongList((int) Math.min(end - start, Integer.MAX_VALUE));
        for (long i = start; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    // ==================== 基本操作 | Basic Operations ====================

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long get(int index) {
        checkIndex(index);
        return elements[index];
    }

    public long set(int index, long value) {
        checkIndex(index);
        long old = elements[index];
        elements[index] = value;
        return old;
    }

    public void add(long value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    public void add(int index, long value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = value;
        size++;
    }

    public void addAll(long... values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, elements, size, values.length);
        size += values.length;
    }

    public long removeAt(int index) {
        checkIndex(index);
        long old = elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        size--;
        return old;
    }

    public boolean remove(long value) {
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

    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    public int indexOf(long value) {
        for (int i = 0; i < size; i++) {
            if (elements[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(long value) {
        for (int i = size - 1; i >= 0; i--) {
            if (elements[i] == value) {
                return i;
            }
        }
        return -1;
    }

    // ==================== 统计操作 | Statistics Operations ====================

    public long sum() {
        long sum = 0;
        for (int i = 0; i < size; i++) {
            sum += elements[i];
        }
        return sum;
    }

    public long min() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        long min = elements[0];
        for (int i = 1; i < size; i++) {
            if (elements[i] < min) {
                min = elements[i];
            }
        }
        return min;
    }

    public long max() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        long max = elements[0];
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
        return (double) sum() / size;
    }

    // ==================== 转换 | Conversion ====================

    public long[] toArray() {
        return Arrays.copyOf(elements, size);
    }

    public LongStream stream() {
        return StreamSupport.longStream(
                Spliterators.spliterator(elements, 0, size, Spliterator.ORDERED),
                false
        );
    }

    public void sort() {
        Arrays.sort(elements, 0, size);
    }

    public void reverse() {
        for (int i = 0, j = size - 1; i < j; i++, j--) {
            long temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
        }
    }

    // ==================== 迭代器 | Iterator ====================

    public PrimitiveIterator.OfLong primitiveIterator() {
        return new PrimitiveIterator.OfLong() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public long nextLong() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    @Override
    public java.util.Iterator<Long> iterator() {
        return new java.util.Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }
        };
    }

    public void forEach(LongConsumer action) {
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
        if (!(o instanceof LongList that)) return false;
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
            result = 31 * result + Long.hashCode(elements[i]);
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
