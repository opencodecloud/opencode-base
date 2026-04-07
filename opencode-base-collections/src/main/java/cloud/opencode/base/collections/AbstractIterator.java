package cloud.opencode.base.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * AbstractIterator - Skeletal Iterator implementation requiring only computeNext()
 * AbstractIterator - 只需实现 computeNext() 的骨架迭代器
 *
 * <p>Provides a simplified way to write an {@link Iterator}. Subclasses need only
 * implement {@link #computeNext()} and call {@link #endOfData()} when iteration is complete.</p>
 * <p>提供了一种简化的方式来编写 {@link Iterator}。子类只需实现 {@link #computeNext()}，
 * 并在迭代结束时调用 {@link #endOfData()}。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Lazy element computation - 延迟计算元素</li>
 *   <li>Supports null elements - 支持 null 元素</li>
 *   <li>peek() to inspect next element without consuming - peek() 查看下一个元素但不消费</li>
 *   <li>Fail-safe state machine prevents misuse - 安全的状态机防止误用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Iterate over an array range - 迭代数组范围
 * Iterator<String> iter = new AbstractIterator<>() {
 *     private int index = 0;
 *     private final String[] data = {"a", "b", "c"};
 *
 *     @Override
 *     protected String computeNext() {
 *         return index < data.length ? data[index++] : endOfData();
 *     }
 * };
 *
 * while (iter.hasNext()) {
 *     System.out.println(iter.next());
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per hasNext/next call - 时间复杂度: 每次 hasNext/next 调用 O(1)</li>
 *   <li>Space complexity: O(1) overhead - 空间复杂度: O(1) 额外开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null elements: Supported - 空元素: 支持</li>
 * </ul>
 *
 * @param <E> the type of elements returned by this iterator - 迭代器返回的元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.3
 */
public abstract class AbstractIterator<E> implements Iterator<E> {

    /**
     * Internal state machine states.
     * 内部状态机状态。
     */
    private enum State {
        /** Element not yet computed - 元素尚未计算 */
        NOT_READY,
        /** Element computed and available - 元素已计算并可用 */
        READY,
        /** Iteration complete - 迭代完成 */
        DONE,
        /** computeNext() threw an exception - computeNext() 抛出了异常 */
        FAILED
    }

    private State state = State.NOT_READY;
    private E next;

    /**
     * Computes the next element in the iteration.
     * 计算迭代中的下一个元素。
     *
     * <p>Implementations must return the next element, or call {@link #endOfData()}
     * and return its result when there are no more elements.</p>
     * <p>实现必须返回下一个元素，或者在没有更多元素时调用 {@link #endOfData()} 并返回其结果。</p>
     *
     * @return the next element, or the result of {@link #endOfData()}
     *         下一个元素，或 {@link #endOfData()} 的结果
     */
    protected abstract E computeNext();

    /**
     * Signals that the iteration is complete.
     * 通知迭代已完成。
     *
     * <p>Call this method from {@link #computeNext()} when there are no more elements.
     * Always return the result of this method from computeNext().</p>
     * <p>当没有更多元素时，从 {@link #computeNext()} 调用此方法。
     * 始终从 computeNext() 返回此方法的结果。</p>
     *
     * @return always {@code null} - 始终返回 {@code null}
     */
    protected final E endOfData() {
        state = State.DONE;
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Multiple calls to this method are idempotent; the underlying
     * {@link #computeNext()} is invoked at most once per element.</p>
     * <p>对此方法的多次调用是幂等的；底层的 {@link #computeNext()} 每个元素最多调用一次。</p>
     */
    @Override
    public final boolean hasNext() {
        switch (state) {
            case READY:
                return true;
            case DONE:
                return false;
            case FAILED:
                throw new IllegalStateException("AbstractIterator is in a failed state; "
                        + "a previous call to computeNext() threw an exception");
            default:
                // NOT_READY — try to compute
        }
        return tryToComputeNext();
    }

    private boolean tryToComputeNext() {
        state = State.FAILED; // temporary; restored on success
        next = computeNext();
        if (state != State.DONE) {
            state = State.READY;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NoSuchElementException if iteration has no more elements
     *         如果迭代没有更多元素
     */
    @Override
    public final E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        state = State.NOT_READY;
        E result = next;
        next = null;
        return result;
    }

    /**
     * Returns the next element without advancing the iterator.
     * 返回下一个元素但不推进迭代器。
     *
     * <p>Subsequent calls to {@code peek()} return the same element until
     * {@link #next()} is called.</p>
     * <p>在调用 {@link #next()} 之前，对 {@code peek()} 的后续调用返回相同的元素。</p>
     *
     * @return the next element - 下一个元素
     * @throws NoSuchElementException if iteration has no more elements
     *         如果迭代没有更多元素
     */
    public final E peek() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return next;
    }
}
