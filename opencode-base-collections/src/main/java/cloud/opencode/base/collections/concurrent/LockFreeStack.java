package cloud.opencode.base.collections.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LockFreeStack - Lock-free Concurrent Stack using Treiber's Algorithm
 * LockFreeStack - 使用 Treiber 算法的无锁并发栈
 *
 * <p>A lock-free stack implementation using CAS (Compare-And-Swap) operations,
 * based on Treiber's algorithm for concurrent stack management.</p>
 * <p>基于 CAS（比较并交换）操作的无锁栈实现，
 * 采用 Treiber 算法进行并发栈管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lock-free operations - 无锁操作</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Non-blocking - 非阻塞</li>
 *   <li>LIFO ordering - 后进先出顺序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LockFreeStack<String> stack = new LockFreeStack<>();
 *
 * // Push (入栈)
 * stack.push("item1");
 * stack.push("item2");
 *
 * // Pop (出栈) - LIFO order
 * String item = stack.pop();  // "item2"
 *
 * // Peek (查看栈顶) - does not remove
 * String top = stack.peek();  // "item1"
 *
 * // Can be used from multiple threads - 可从多线程使用
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>push: O(1) amortized - push: 均摊 O(1)</li>
 *   <li>pop: O(1) amortized - pop: 均摊 O(1)</li>
 *   <li>peek: O(1) - peek: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (lock-free, CAS-based) - 线程安全: 是（无锁，基于 CAS）</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public final class LockFreeStack<E> {

    private final AtomicReference<Node<E>> top = new AtomicReference<>(null);
    /**
     * Approximate size counter. May be briefly inaccurate because {@code top} and
     * {@code size} are not updated atomically.
     * 近似大小计数器。由于 {@code top} 和 {@code size} 不是原子更新的，可能短暂不精确。
     */
    private final AtomicInteger size = new AtomicInteger(0);

    // ==================== 构造方法 | Constructors ====================

    /**
     * Create an empty LockFreeStack.
     * 创建空 LockFreeStack。
     */
    public LockFreeStack() {
    }

    // ==================== 栈操作 | Stack Operations ====================

    /**
     * Push an element onto the top of the stack.
     * 将元素压入栈顶。
     *
     * @param element the element to push | 要压入的元素
     * @throws NullPointerException if element is null | 如果元素为 null
     */
    public void push(E element) {
        Objects.requireNonNull(element, "Element cannot be null");
        Node<E> newNode = new Node<>(element);
        Node<E> oldTop;
        do {
            oldTop = top.get();
            newNode.next = oldTop;
        } while (!top.compareAndSet(oldTop, newNode));
        size.incrementAndGet();
    }

    /**
     * Pop the top element from the stack.
     * 弹出栈顶元素。
     *
     * @return the top element, or null if the stack is empty | 栈顶元素，如果栈为空则返回 null
     */
    public E pop() {
        Node<E> oldTop;
        Node<E> newTop;
        do {
            oldTop = top.get();
            if (oldTop == null) {
                return null;
            }
            newTop = oldTop.next;
        } while (!top.compareAndSet(oldTop, newTop));
        size.decrementAndGet();
        return oldTop.value;
    }

    /**
     * Peek at the top element without removing it.
     * 查看栈顶元素但不移除。
     *
     * @return the top element, or null if the stack is empty | 栈顶元素，如果栈为空则返回 null
     */
    public E peek() {
        Node<E> t = top.get();
        return t != null ? t.value : null;
    }

    /**
     * Check if the stack is empty.
     * 检查栈是否为空。
     *
     * @return true if the stack is empty | 如果栈为空返回 true
     */
    public boolean isEmpty() {
        return top.get() == null;
    }

    /**
     * Get the approximate size of the stack.
     * 获取栈的近似大小。
     *
     * <p>Note: In a concurrent environment, this value is approximate
     * and may be briefly inaccurate because {@code top} and {@code size}
     * are not updated atomically. The returned value is always non-negative.</p>
     * <p>注意：在并发环境中，该值为近似值，可能短暂不精确，
     * 因为 {@code top} 和 {@code size} 不是原子更新的。返回值始终非负。</p>
     *
     * @return approximate number of elements (always &ge; 0) | 近似元素数量（始终 &ge; 0）
     */
    public int size() {
        return Math.max(0, size.get());
    }

    /**
     * Remove all elements from the stack.
     * 移除栈中所有元素。
     *
     * <p>This operation is thread-safe. It atomically swaps the top to {@code null}
     * using CAS, then resets the size counter.</p>
     * <p>此操作是线程安全的。使用 CAS 原子地将栈顶交换为 {@code null}，然后重置大小计数器。</p>
     */
    public void clear() {
        // CAS loop to atomically set top to null
        Node<E> oldTop;
        do {
            oldTop = top.get();
        } while (!top.compareAndSet(oldTop, null));
        size.set(0);
    }

    // ==================== 内部类 | Internal Classes ====================

    private static final class Node<E> {
        final E value;
        volatile Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }
}
