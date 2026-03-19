package cloud.opencode.base.collections.concurrent;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LockFreeQueue - Lock-free Queue Implementation
 * LockFreeQueue - 无锁队列实现
 *
 * <p>A lock-free queue implementation using atomic operations.</p>
 * <p>使用原子操作的无锁队列实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock-free operations - 无锁操作</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Non-blocking - 非阻塞</li>
 *   <li>Wait-free for single producer - 单生产者无等待</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LockFreeQueue<String> queue = LockFreeQueue.create();
 *
 * // Offer (enqueue) - 入队
 * queue.offer("item1");
 * queue.offer("item2");
 *
 * // Poll (dequeue) - 出队
 * String item = queue.poll();  // "item1"
 *
 * // Can be used from multiple threads - 可从多线程使用
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>offer: O(1) - offer: O(1)</li>
 *   <li>poll: O(1) - poll: O(1)</li>
 *   <li>peek: O(1) - peek: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (lock-free) - 线程安全: 是（无锁）</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class LockFreeQueue<E> extends AbstractQueue<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AtomicReference<Node<E>> head;
    private final AtomicReference<Node<E>> tail;

    // ==================== 构造方法 | Constructors ====================

    private LockFreeQueue() {
        Node<E> dummy = new Node<>(null);
        this.head = new AtomicReference<>(dummy);
        this.tail = new AtomicReference<>(dummy);
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty LockFreeQueue.
     * 创建空 LockFreeQueue。
     *
     * @param <E> element type | 元素类型
     * @return new empty LockFreeQueue | 新空 LockFreeQueue
     */
    public static <E> LockFreeQueue<E> create() {
        return new LockFreeQueue<>();
    }

    /**
     * Create a LockFreeQueue from collection.
     * 从集合创建 LockFreeQueue。
     *
     * @param <E>        element type | 元素类型
     * @param collection the collection | 集合
     * @return new LockFreeQueue | 新 LockFreeQueue
     */
    public static <E> LockFreeQueue<E> create(Collection<? extends E> collection) {
        LockFreeQueue<E> queue = create();
        for (E element : collection) {
            queue.offer(element);
        }
        return queue;
    }

    // ==================== Queue 方法 | Queue Methods ====================

    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e, "Element cannot be null");
        Node<E> newNode = new Node<>(e);

        while (true) {
            Node<E> currentTail = tail.get();
            Node<E> tailNext = currentTail.next.get();

            if (currentTail == tail.get()) {
                if (tailNext == null) {
                    // Try to link new node
                    if (currentTail.next.compareAndSet(null, newNode)) {
                        // Try to swing tail to new node
                        tail.compareAndSet(currentTail, newNode);
                        return true;
                    }
                } else {
                    // Tail was not pointing to last node, try to swing
                    tail.compareAndSet(currentTail, tailNext);
                }
            }
        }
    }

    @Override
    public E poll() {
        while (true) {
            Node<E> currentHead = head.get();
            Node<E> currentTail = tail.get();
            Node<E> headNext = currentHead.next.get();

            if (currentHead == head.get()) {
                if (currentHead == currentTail) {
                    if (headNext == null) {
                        return null; // Queue is empty
                    }
                    // Tail falling behind, try to advance it
                    tail.compareAndSet(currentTail, headNext);
                } else {
                    // Read value before CAS
                    E value = headNext.value;
                    if (head.compareAndSet(currentHead, headNext)) {
                        return value;
                    }
                }
            }
        }
    }

    @Override
    public E peek() {
        while (true) {
            Node<E> currentHead = head.get();
            Node<E> currentTail = tail.get();
            Node<E> headNext = currentHead.next.get();

            if (currentHead == head.get()) {
                if (currentHead == currentTail) {
                    if (headNext == null) {
                        return null; // Queue is empty
                    }
                    // Tail falling behind, try to advance it
                    tail.compareAndSet(currentTail, headNext);
                } else {
                    return headNext.value;
                }
            }
        }
    }

    @Override
    public int size() {
        int count = 0;
        Node<E> current = head.get().next.get();
        while (current != null && count < Integer.MAX_VALUE) {
            count++;
            current = current.next.get();
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        Node<E> currentHead = head.get();
        Node<E> currentTail = tail.get();
        Node<E> headNext = currentHead.next.get();
        return currentHead == currentTail && headNext == null;
    }

    @Override
    public Iterator<E> iterator() {
        return new LockFreeQueueIterator();
    }

    @Override
    public void clear() {
        Node<E> dummy = new Node<>(null);
        head.set(dummy);
        tail.set(dummy);
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) return false;
        Node<E> current = head.get().next.get();
        while (current != null) {
            if (o.equals(current.value)) {
                return true;
            }
            current = current.next.get();
        }
        return false;
    }

    // ==================== 内部类 | Internal Classes ====================

    private static class Node<E> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        final E value;
        final AtomicReference<Node<E>> next;

        Node(E value) {
            this.value = value;
            this.next = new AtomicReference<>(null);
        }
    }

    private class LockFreeQueueIterator implements Iterator<E> {
        private Node<E> current = head.get();
        private Node<E> lastReturned = null;

        @Override
        public boolean hasNext() {
            return current.next.get() != null;
        }

        @Override
        public E next() {
            Node<E> next = current.next.get();
            if (next == null) {
                throw new NoSuchElementException();
            }
            lastReturned = current;
            current = next;
            return next.value;
        }

        @Override
        public void remove() {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            // Note: This remove is not truly lock-free
            // For a true lock-free implementation, the queue should not support remove
            throw new UnsupportedOperationException("remove() not supported in lock-free iterator");
        }
    }
}
