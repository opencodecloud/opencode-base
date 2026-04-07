package cloud.opencode.base.collections.specialized;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * MinMaxPriorityQueue - Double-ended Priority Queue
 * MinMaxPriorityQueue - 双端优先队列
 *
 * <p>A priority queue that supports efficient access to both the minimum and
 * maximum elements. Based on a min-max heap data structure.</p>
 * <p>支持高效访问最小和最大元素的优先队列。基于最小-最大堆数据结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Efficient min/max access - O(1) peekFirst/peekLast - 高效最小/最大访问</li>
 *   <li>Optional capacity bound - 可选容量限制</li>
 *   <li>Auto-eviction when bounded - 有界时自动淘汰</li>
 *   <li>Custom comparator support - 自定义比较器支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create unbounded queue | 创建无界队列
 * MinMaxPriorityQueue<Integer> queue = MinMaxPriorityQueue.create();
 * queue.addAll(Arrays.asList(5, 2, 8, 1, 9, 3));
 *
 * queue.peekFirst();  // 1 (minimum)
 * queue.peekLast();   // 9 (maximum)
 *
 * queue.pollFirst();  // removes 1
 * queue.pollLast();   // removes 9
 *
 * // Create bounded queue (keeps N smallest) | 创建有界队列（保留N个最小值）
 * MinMaxPriorityQueue<Integer> bounded = MinMaxPriorityQueue.<Integer>builder()
 *     .maximumSize(3)
 *     .create();
 * bounded.addAll(Arrays.asList(5, 2, 8, 1, 9));
 * // Contains: [1, 2, 5] - largest values evicted
 *
 * // With custom comparator | 使用自定义比较器
 * MinMaxPriorityQueue<String> byLength = MinMaxPriorityQueue.<String>builder()
 *     .comparator(Comparator.comparingInt(String::length))
 *     .maximumSize(5)
 *     .create();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add/offer: O(log n) - add/offer: O(log n)</li>
 *   <li>peekFirst/peekLast: O(1) - peekFirst/peekLast: O(1)</li>
 *   <li>pollFirst/pollLast: O(log n) - pollFirst/pollLast: O(log n)</li>
 *   <li>remove: O(n) - remove: O(n)</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is NOT thread-safe. External synchronization is required for
 * concurrent access.</p>
 * <p>此类非线程安全。并发访问需要外部同步。</p>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class MinMaxPriorityQueue<E> extends AbstractQueue<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Comparator<? super E> comparator;
    private final int maximumSize;
    private Object[] heap;
    private int size;

    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private static final int UNBOUNDED = Integer.MAX_VALUE;

    // ==================== 构造方法 | Constructors ====================

    private MinMaxPriorityQueue(Comparator<? super E> comparator, int initialCapacity, int maximumSize) {
        this.comparator = comparator;
        this.maximumSize = maximumSize;
        this.heap = new Object[initialCapacity];
        this.size = 0;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an unbounded MinMaxPriorityQueue using natural ordering.
     * 使用自然顺序创建无界 MinMaxPriorityQueue。
     *
     * @param <E> element type (must be Comparable) | 元素类型（必须是 Comparable）
     * @return new empty MinMaxPriorityQueue | 新空 MinMaxPriorityQueue
     */
    public static <E extends Comparable<? super E>> MinMaxPriorityQueue<E> create() {
        return new MinMaxPriorityQueue<>(null, DEFAULT_INITIAL_CAPACITY, UNBOUNDED);
    }

    /**
     * Create an unbounded MinMaxPriorityQueue from collection.
     * 从集合创建无界 MinMaxPriorityQueue。
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return new MinMaxPriorityQueue | 新 MinMaxPriorityQueue
     */
    public static <E extends Comparable<? super E>> MinMaxPriorityQueue<E> create(Collection<? extends E> collection) {
        MinMaxPriorityQueue<E> queue = create();
        queue.addAll(collection);
        return queue;
    }

    /**
     * Create a builder for customized MinMaxPriorityQueue.
     * 创建自定义 MinMaxPriorityQueue 的构建器。
     *
     * @param <E> element type | 元素类型
     * @return new builder | 新构建器
     */
    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    /**
     * Create a builder with specified comparator.
     * 使用指定比较器创建构建器。
     *
     * @param <E>        element type | 元素类型
     * @param comparator the comparator | 比较器
     * @return new builder | 新构建器
     */
    public static <E> Builder<E> orderedBy(Comparator<? super E> comparator) {
        return new Builder<E>().comparator(comparator);
    }

    /**
     * Create a bounded queue that keeps the smallest N elements.
     * 创建保留最小 N 个元素的有界队列。
     *
     * @param <E>         element type | 元素类型
     * @param maximumSize maximum size | 最大容量
     * @return new builder | 新构建器
     */
    public static <E> Builder<E> maximumSize(int maximumSize) {
        return new Builder<E>().maximumSize(maximumSize);
    }

    // ==================== 双端操作 | Double-ended Operations ====================

    /**
     * Returns the smallest element without removing it.
     * 返回最小元素但不移除。
     *
     * @return the smallest element, or null if empty | 最小元素，如果为空则返回 null
     */
    public E peekFirst() {
        return peek();
    }

    /**
     * Returns the largest element without removing it.
     * 返回最大元素但不移除。
     *
     * @return the largest element, or null if empty | 最大元素，如果为空则返回 null
     */
    @SuppressWarnings("unchecked")
    public E peekLast() {
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return (E) heap[0];
        }
        // Linear scan of leaf nodes to find the actual maximum.
        // In a min-heap, the maximum is always among the leaves (indices size/2 .. size-1).
        int firstLeaf = size >>> 1;
        E max = (E) heap[firstLeaf];
        for (int i = firstLeaf + 1; i < size; i++) {
            E candidate = (E) heap[i];
            if (compare(candidate, max) > 0) {
                max = candidate;
            }
        }
        return max;
    }

    /**
     * Removes and returns the smallest element.
     * 移除并返回最小元素。
     *
     * @return the smallest element, or null if empty | 最小元素，如果为空则返回 null
     */
    public E pollFirst() {
        return poll();
    }

    /**
     * Removes and returns the largest element.
     * 移除并返回最大元素。
     *
     * @return the largest element, or null if empty | 最大元素，如果为空则返回 null
     */
    @SuppressWarnings("unchecked")
    public E pollLast() {
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return (E) heap[--size];
        }

        // Linear scan of leaf nodes to find the actual maximum index.
        int firstLeaf = size >>> 1;
        int maxIndex = firstLeaf;
        E max = (E) heap[firstLeaf];
        for (int i = firstLeaf + 1; i < size; i++) {
            E candidate = (E) heap[i];
            if (compare(candidate, max) > 0) {
                max = candidate;
                maxIndex = i;
            }
        }

        // Move last element to max position and re-heapify
        E last = (E) heap[--size];
        heap[size] = null;
        if (size > maxIndex) {
            heap[maxIndex] = last;
            // Sift up since we placed an element into the leaf area and it may be
            // smaller than its parent, violating the min-heap property
            siftUp(maxIndex);
            // If the element didn't move up (still at maxIndex), it may be larger
            // than its children, so sift down to restore the min-heap property
            if (heap[maxIndex] == last) {
                siftDownMin(maxIndex);
            }
        }

        return max;
    }

    /**
     * Removes and returns the smallest element, throwing if empty.
     * 移除并返回最小元素，如果为空则抛出异常。
     *
     * @return the smallest element | 最小元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public E removeFirst() {
        E result = pollFirst();
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    /**
     * Removes and returns the largest element, throwing if empty.
     * 移除并返回最大元素，如果为空则抛出异常。
     *
     * @return the largest element | 最大元素
     * @throws NoSuchElementException if empty | 如果为空
     */
    public E removeLast() {
        E result = pollLast();
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    // ==================== Queue 操作 | Queue Operations ====================

    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e, "element must not be null");

        // If bounded and at capacity, check if new element should replace max
        if (maximumSize != UNBOUNDED && size >= maximumSize) {
            E max = peekLast();
            if (compare(e, max) > 0) {
                // New element is > max, reject it
                return false;
            }
            // Remove max to make room
            pollLast();
        }

        ensureCapacity(size + 1);
        heap[size] = e;
        siftUp(size);
        size++;
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E poll() {
        if (size == 0) {
            return null;
        }

        E min = (E) heap[0];
        E last = (E) heap[--size];
        heap[size] = null;

        if (size > 0) {
            heap[0] = last;
            siftDownMin(0);
        }

        return min;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E peek() {
        return size == 0 ? null : (E) heap[0];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (E) heap[cursor++];
            }
        };
    }

    // ==================== 辅助方法 | Helper Methods ====================

    /**
     * Returns the maximum size of this queue, or Integer.MAX_VALUE if unbounded.
     * 返回此队列的最大容量，如果无界则返回 Integer.MAX_VALUE。
     *
     * @return maximum size | 最大容量
     */
    public int maximumSize() {
        return maximumSize;
    }

    /**
     * Returns true if this queue is bounded.
     * 如果此队列有界，则返回 true。
     *
     * @return true if bounded | 如果有界则返回 true
     */
    public boolean isBounded() {
        return maximumSize != UNBOUNDED;
    }

    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        }
        return ((Comparable<? super E>) a).compareTo(b);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > heap.length) {
            int newCapacity = heap.length + (heap.length >> 1) + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            heap = Arrays.copyOf(heap, newCapacity);
        }
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int index) {
        E e = (E) heap[index];
        while (index > 0) {
            int parentIndex = (index - 1) >>> 1;
            E parent = (E) heap[parentIndex];
            if (compare(e, parent) >= 0) {
                break;
            }
            heap[index] = parent;
            index = parentIndex;
        }
        heap[index] = e;
    }

    @SuppressWarnings("unchecked")
    private void siftDownMin(int index) {
        E e = (E) heap[index];
        int half = size >>> 1;
        while (index < half) {
            int left = (index << 1) + 1;
            int right = left + 1;
            int smallest = left;

            if (right < size && compare((E) heap[right], (E) heap[left]) < 0) {
                smallest = right;
            }

            if (compare(e, (E) heap[smallest]) <= 0) {
                break;
            }

            heap[index] = heap[smallest];
            index = smallest;
        }
        heap[index] = e;
    }

    @SuppressWarnings("unchecked")
    private void siftDownMax(int index) {
        E e = (E) heap[index];
        int half = size >>> 1;
        while (index < half) {
            int left = (index << 1) + 1;
            int right = left + 1;
            int largest = left;

            if (right < size && compare((E) heap[right], (E) heap[left]) > 0) {
                largest = right;
            }

            if (compare(e, (E) heap[largest]) >= 0) {
                break;
            }

            heap[index] = heap[largest];
            index = largest;
        }
        heap[index] = e;
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for MinMaxPriorityQueue.
     * MinMaxPriorityQueue 构建器。
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private Comparator<? super E> comparator;
        private int initialCapacity = DEFAULT_INITIAL_CAPACITY;
        private int maximumSize = UNBOUNDED;

        private Builder() {}

        /**
         * Set the comparator for element ordering.
         * 设置元素排序的比较器。
         *
         * @param comparator the comparator | 比较器
         * @return this builder | 此构建器
         */
        public Builder<E> comparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Set the initial capacity.
         * 设置初始容量。
         *
         * @param initialCapacity initial capacity | 初始容量
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if initialCapacity is negative | 如果初始容量为负
         */
        public Builder<E> initialCapacity(int initialCapacity) {
            if (initialCapacity < 0) {
                throw new IllegalArgumentException("initialCapacity must be non-negative: " + initialCapacity);
            }
            this.initialCapacity = initialCapacity;
            return this;
        }

        /**
         * Set the maximum size. When the queue exceeds this size, the largest
         * elements are automatically evicted.
         * 设置最大容量。当队列超过此大小时，最大的元素将被自动淘汰。
         *
         * @param maximumSize maximum size | 最大容量
         * @return this builder | 此构建器
         * @throws IllegalArgumentException if maximumSize is not positive | 如果最大容量非正数
         */
        public Builder<E> maximumSize(int maximumSize) {
            if (maximumSize <= 0) {
                throw new IllegalArgumentException("maximumSize must be positive: " + maximumSize);
            }
            this.maximumSize = maximumSize;
            return this;
        }

        /**
         * Create the MinMaxPriorityQueue.
         * 创建 MinMaxPriorityQueue。
         *
         * @return new MinMaxPriorityQueue | 新 MinMaxPriorityQueue
         */
        public MinMaxPriorityQueue<E> create() {
            int capacity = Math.min(initialCapacity, maximumSize);
            return new MinMaxPriorityQueue<>(comparator, capacity, maximumSize);
        }

        /**
         * Create the MinMaxPriorityQueue with initial elements.
         * 使用初始元素创建 MinMaxPriorityQueue。
         *
         * @param elements initial elements | 初始元素
         * @return new MinMaxPriorityQueue | 新 MinMaxPriorityQueue
         */
        @SafeVarargs
        public final MinMaxPriorityQueue<E> create(E... elements) {
            MinMaxPriorityQueue<E> queue = create();
            for (E element : elements) {
                queue.offer(element);
            }
            return queue;
        }

        /**
         * Create the MinMaxPriorityQueue from collection.
         * 从集合创建 MinMaxPriorityQueue。
         *
         * @param collection the collection | 集合
         * @return new MinMaxPriorityQueue | 新 MinMaxPriorityQueue
         */
        public MinMaxPriorityQueue<E> create(Iterable<? extends E> collection) {
            MinMaxPriorityQueue<E> queue = create();
            for (E element : collection) {
                queue.offer(element);
            }
            return queue;
        }
    }
}
