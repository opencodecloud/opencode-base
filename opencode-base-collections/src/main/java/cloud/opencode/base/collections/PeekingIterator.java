package cloud.opencode.base.collections;

import java.util.Iterator;

/**
 * PeekingIterator - Iterator that allows peeking at the next element
 * PeekingIterator - 可查看下一个元素的迭代器
 *
 * <p>An iterator that supports a one-element lookahead. The peek() method returns the
 * next element without advancing the iterator.</p>
 * <p>支持单元素前瞻的迭代器。peek() 方法返回下一个元素但不移动迭代器指针。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Peek at next element without consuming - 查看下一个元素但不消费</li>
 *   <li>Standard Iterator operations - 标准迭代器操作</li>
 *   <li>Useful for parsing and lookahead scenarios - 适用于解析和前瞻场景</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PeekingIterator<String> it = IteratorUtil.peekingIterator(list.iterator());
 * while (it.hasNext()) {
 *     String next = it.peek();  // Look without consuming
 *     if (shouldProcess(next)) {
 *         process(it.next());   // Now consume
 *     } else {
 *         it.next();            // Skip
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (supports null elements) - 空值安全: 是（支持 null 元素）</li>
 * </ul>
 *
 * @param <E> element type | 元素类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface PeekingIterator<E> extends Iterator<E> {

    /**
     * Returns the next element without advancing the iterator
     * 查看下一个元素但不移动指针
     *
     * <p>Multiple calls to peek() return the same element until next() is called.</p>
     * <p>多次调用 peek() 返回相同元素，直到调用 next()。</p>
     *
     * @return the next element | 下一个元素
     * @throws java.util.NoSuchElementException if no more elements | 如果没有更多元素
     */
    E peek();
}
