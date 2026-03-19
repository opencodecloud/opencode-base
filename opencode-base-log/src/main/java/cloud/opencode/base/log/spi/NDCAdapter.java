package cloud.opencode.base.log.spi;

import java.util.Deque;

/**
 * NDC Adapter Interface - Nested Diagnostic Context Adapter
 * NDC 适配器接口 - 嵌套诊断上下文适配器
 *
 * <p>This interface provides an abstraction layer for NDC (stack-based context)
 * operations, allowing different logging frameworks to provide their own implementations.</p>
 * <p>此接口为 NDC（基于栈的上下文）操作提供抽象层，允许不同的日志框架提供自己的实现。</p>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stack-based diagnostic context abstraction - 基于栈的诊断上下文抽象</li>
 *   <li>Push/pop/peek operations - 推入/弹出/查看操作</li>
 *   <li>Configurable maximum depth - 可配置的最大深度</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Implement custom NDC adapter
 * public class MyNDCAdapter implements NDCAdapter {
 *     @Override
 *     public void push(String message) { ... }
 *     @Override
 *     public String pop() { ... }
 *     // ... other methods
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes (returns null when empty) - 空值安全: 是（为空时返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public interface NDCAdapter {

    /**
     * Pushes a message onto the NDC stack.
     * 将消息推入 NDC 栈。
     *
     * @param message the message to push - 要推入的消息
     */
    void push(String message);

    /**
     * Pops the top message from the NDC stack.
     * 从 NDC 栈弹出顶部消息。
     *
     * @return the popped message, or null if empty - 弹出的消息，如果为空则返回 null
     */
    String pop();

    /**
     * Returns the top message without removing it.
     * 返回顶部消息但不移除它。
     *
     * @return the top message, or null if empty - 顶部消息，如果为空则返回 null
     */
    String peek();

    /**
     * Clears the NDC stack.
     * 清空 NDC 栈。
     */
    void clear();

    /**
     * Returns the depth of the NDC stack.
     * 返回 NDC 栈的深度。
     *
     * @return the stack depth - 栈深度
     */
    int getDepth();

    /**
     * Sets the maximum depth of the NDC stack.
     * 设置 NDC 栈的最大深度。
     *
     * @param maxDepth the maximum depth - 最大深度
     */
    void setMaxDepth(int maxDepth);

    /**
     * Returns a copy of the NDC stack.
     * 返回 NDC 栈的副本。
     *
     * @return a copy of the stack - 栈的副本
     */
    Deque<String> getCopyOfStack();

    /**
     * Sets the NDC stack.
     * 设置 NDC 栈。
     *
     * @param stack the stack to set - 要设置的栈
     */
    void setStack(Deque<String> stack);
}
