package cloud.opencode.base.collections.specialized;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * EvictingQueue - Fixed-size Queue with Automatic Eviction
 * EvictingQueue - 自动淘汰的固定大小队列
 *
 * <p>A non-blocking queue which automatically evicts elements from the head of the queue
 * when attempting to add new elements onto the queue and it is full. This queue orders
 * elements FIFO (first-in-first-out).</p>
 * <p>一个非阻塞队列，当队列已满时尝试添加新元素会自动从队列头部淘汰元素。
 * 此队列按 FIFO（先进先出）顺序排列元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed maximum size - 固定最大容量</li>
 *   <li>Automatic eviction of oldest elements - 自动淘汰最旧元素</li>
 *   <li>FIFO ordering - FIFO 顺序</li>
 *   <li>Non-blocking operations - 非阻塞操作</li>
 *   <li>Optional eviction listener - 可选淘汰监听器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a queue with max size 3 | 创建最大容量为3的队列
 * EvictingQueue<String> queue = EvictingQueue.create(3);
 *
 * queue.add("a");  // [a]
 * queue.add("b");  // [a, b]
 * queue.add("c");  // [a, b, c]
 * queue.add("d");  // [b, c, d] - "a" was evicted
 *
 * queue.poll();    // returns "b", queue is [c, d]
 *
 * // With eviction listener | 使用淘汰监听器
 * EvictingQueue<String> monitored = EvictingQueue.<String>builder(3)
 *     .onEviction(evicted -> System.out.println("Evicted: " + evicted))
 *     .create();
 *
 * // Great for keeping last N items | 非常适合保留最后 N 个项目
 * EvictingQueue<LogEntry> recentLogs = EvictingQueue.create(1000);
 * }</pre>
 *
 * <p><strong>Use Cases | 使用场景:</strong></p>
 * <ul>
 *   <li>Keeping last N log entries - 保留最后 N 条日志</li>
 *   <li>Recent history tracking - 最近历史跟踪</li>
 *   <li>Sliding window calculations - 滑动窗口计算</li>
 *   <li>Buffer with automatic cleanup - 自动清理的缓冲区</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>add/offer: O(1) - add/offer: O(1)</li>
 *   <li>poll/peek: O(1) - poll/peek: O(1)</li>
 *   <li>remainingCapacity: O(1) - remainingCapacity: O(1)</li>
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
public final class EvictingQueue<E> extends AbstractQueue<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int maxSize;
    private final ArrayDeque<E> delegate;
    private final transient EvictionListener<E> evictionListener;

    // ==================== 构造方法 | Constructors ====================

    private EvictingQueue(int maxSize, EvictionListener<E> evictionListener) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        this.maxSize = maxSize;
        this.delegate = new ArrayDeque<>(maxSize);
        this.evictionListener = evictionListener;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an EvictingQueue with the specified maximum size.
     * 创建具有指定最大容量的 EvictingQueue。
     *
     * @param <E>     element type | 元素类型
     * @param maxSize maximum size | 最大容量
     * @return new EvictingQueue | 新 EvictingQueue
     * @throws IllegalArgumentException if maxSize is not positive | 如果 maxSize 非正数
     */
    public static <E> EvictingQueue<E> create(int maxSize) {
        return new EvictingQueue<>(maxSize, null);
    }

    /**
     * Create an EvictingQueue from existing elements with specified maximum size.
     * 从现有元素创建具有指定最大容量的 EvictingQueue。
     *
     * @param <E>        element type | 元素类型
     * @param maxSize    maximum size | 最大容量
     * @param collection initial elements | 初始元素
     * @return new EvictingQueue | 新 EvictingQueue
     */
    public static <E> EvictingQueue<E> create(int maxSize, Collection<? extends E> collection) {
        EvictingQueue<E> queue = create(maxSize);
        queue.addAll(collection);
        return queue;
    }

    /**
     * Create a builder for customized EvictingQueue.
     * 创建自定义 EvictingQueue 的构建器。
     *
     * @param <E>     element type | 元素类型
     * @param maxSize maximum size | 最大容量
     * @return new builder | 新构建器
     */
    public static <E> Builder<E> builder(int maxSize) {
        return new Builder<>(maxSize);
    }

    // ==================== Queue 操作 | Queue Operations ====================

    /**
     * Adds element to queue. If queue is at capacity, the oldest element is evicted.
     * 向队列添加元素。如果队列已满，最旧的元素将被淘汰。
     *
     * @param e element to add | 要添加的元素
     * @return always true (eviction ensures space) | 始终为 true（淘汰确保空间）
     * @throws NullPointerException if element is null | 如果元素为 null
     */
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e, "element must not be null");

        if (delegate.size() >= maxSize) {
            E evicted = delegate.poll();
            if (evictionListener != null && evicted != null) {
                evictionListener.onEviction(evicted);
            }
        }

        return delegate.offer(e);
    }

    /**
     * Adds element to queue. Always returns true.
     * 向队列添加元素。始终返回 true。
     *
     * @param e element to add | 要添加的元素
     * @return always true | 始终为 true
     */
    @Override
    public boolean add(E e) {
        return offer(e);
    }

    @Override
    public E poll() {
        return delegate.poll();
    }

    @Override
    public E peek() {
        return delegate.peek();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    // ==================== 额外方法 | Additional Methods ====================

    /**
     * Returns the maximum size of this queue.
     * 返回此队列的最大容量。
     *
     * @return maximum size | 最大容量
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Returns the remaining capacity of this queue.
     * 返回此队列的剩余容量。
     *
     * @return remaining capacity | 剩余容量
     */
    public int remainingCapacity() {
        return maxSize - delegate.size();
    }

    /**
     * Returns true if the queue is at maximum capacity.
     * 如果队列已达到最大容量，则返回 true。
     *
     * @return true if full | 如果已满则返回 true
     */
    public boolean isFull() {
        return delegate.size() >= maxSize;
    }

    /**
     * Returns the element at the tail of the queue (most recently added).
     * 返回队列尾部的元素（最近添加的）。
     *
     * @return the last element, or null if empty | 最后一个元素，如果为空则返回 null
     */
    public E peekLast() {
        return delegate.peekLast();
    }

    /**
     * Returns a list containing all elements in FIFO order.
     * 返回包含所有元素的列表，按 FIFO 顺序排列。
     *
     * @return list of elements | 元素列表
     */
    public List<E> toList() {
        return new ArrayList<>(delegate);
    }

    // ==================== 淘汰监听器 | Eviction Listener ====================

    /**
     * Listener for element eviction events.
     * 元素淘汰事件的监听器。
     *
     * @param <E> element type | 元素类型
     */
    @FunctionalInterface
    public interface EvictionListener<E> {
        /**
         * Called when an element is evicted from the queue.
         * 当元素从队列中被淘汰时调用。
         *
         * @param evicted the evicted element | 被淘汰的元素
         */
        void onEviction(E evicted);
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for EvictingQueue.
     * EvictingQueue 构建器。
     *
     * @param <E> element type | 元素类型
     */
    public static final class Builder<E> {
        private final int maxSize;
        private EvictionListener<E> evictionListener;

        private Builder(int maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
            }
            this.maxSize = maxSize;
        }

        /**
         * Set the eviction listener.
         * 设置淘汰监听器。
         *
         * @param listener the eviction listener | 淘汰监听器
         * @return this builder | 此构建器
         */
        public Builder<E> onEviction(EvictionListener<E> listener) {
            this.evictionListener = listener;
            return this;
        }

        /**
         * Create the EvictingQueue.
         * 创建 EvictingQueue。
         *
         * @return new EvictingQueue | 新 EvictingQueue
         */
        public EvictingQueue<E> create() {
            return new EvictingQueue<>(maxSize, evictionListener);
        }

        /**
         * Create the EvictingQueue with initial elements.
         * 使用初始元素创建 EvictingQueue。
         *
         * @param elements initial elements | 初始元素
         * @return new EvictingQueue | 新 EvictingQueue
         */
        @SafeVarargs
        public final EvictingQueue<E> create(E... elements) {
            EvictingQueue<E> queue = create();
            for (E element : elements) {
                queue.offer(element);
            }
            return queue;
        }

        /**
         * Create the EvictingQueue from collection.
         * 从集合创建 EvictingQueue。
         *
         * @param collection the collection | 集合
         * @return new EvictingQueue | 新 EvictingQueue
         */
        public EvictingQueue<E> create(Iterable<? extends E> collection) {
            EvictingQueue<E> queue = create();
            for (E element : collection) {
                queue.offer(element);
            }
            return queue;
        }
    }
}
