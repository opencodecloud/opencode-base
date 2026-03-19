package cloud.opencode.base.log.context;

import cloud.opencode.base.log.spi.LogProviderFactory;
import cloud.opencode.base.log.spi.NDCAdapter;

import java.util.Deque;

/**
 * NDC - Nested Diagnostic Context
 * NDC - 嵌套诊断上下文
 *
 * <p>NDC provides a stack-based context for enriching log messages.
 * It is useful for tracking nested operations or call stacks.</p>
 * <p>NDC 提供基于栈的上下文来丰富日志消息。它对于跟踪嵌套操作或调用栈很有用。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * NDC.push("enter processOrder");
 * try {
 *     NDC.push("validate order");
 *     // validation
 *     NDC.pop();
 *
 *     NDC.push("save order");
 *     // save
 *     NDC.pop();
 * } finally {
 *     NDC.pop();
 * }
 *
 * // Use scope for automatic cleanup
 * try (NDCScope scope = NDC.scope("processPayment")) {
 *     OpenLog.info("Processing payment");
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Stack-based nested diagnostic context - 基于栈的嵌套诊断上下文</li>
 *   <li>Scope-based auto-cleanup (try-with-resources) - 基于作用域的自动清理（try-with-resources）</li>
 *   <li>Configurable maximum stack depth - 可配置的最大栈深度</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ThreadLocal-based) - 线程安全: 是（基于 ThreadLocal）</li>
 *   <li>Null-safe: Yes (returns null when empty) - 空值安全: 是（为空时返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class NDC {

    private NDC() {
        // Utility class
    }

    /**
     * Pushes a message onto the NDC stack.
     * 将消息推入 NDC 栈。
     *
     * @param message the message to push - 要推入的消息
     */
    public static void push(String message) {
        NDCAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.push(message);
        }
    }

    /**
     * Pops the top message from the NDC stack.
     * 从 NDC 栈弹出顶部消息。
     *
     * @return the popped message, or null if empty - 弹出的消息，如果为空则返回 null
     */
    public static String pop() {
        NDCAdapter adapter = getAdapter();
        return adapter != null ? adapter.pop() : null;
    }

    /**
     * Returns the top message without removing it.
     * 返回顶部消息但不移除它。
     *
     * @return the top message, or null if empty - 顶部消息，如果为空则返回 null
     */
    public static String peek() {
        NDCAdapter adapter = getAdapter();
        return adapter != null ? adapter.peek() : null;
    }

    /**
     * Clears the NDC stack.
     * 清空 NDC 栈。
     */
    public static void clear() {
        NDCAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.clear();
        }
    }

    /**
     * Returns the depth of the NDC stack.
     * 返回 NDC 栈的深度。
     *
     * @return the stack depth - 栈深度
     */
    public static int getDepth() {
        NDCAdapter adapter = getAdapter();
        return adapter != null ? adapter.getDepth() : 0;
    }

    /**
     * Sets the maximum depth of the NDC stack.
     * 设置 NDC 栈的最大深度。
     *
     * @param maxDepth the maximum depth - 最大深度
     */
    public static void setMaxDepth(int maxDepth) {
        NDCAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setMaxDepth(maxDepth);
        }
    }

    /**
     * Returns a copy of the NDC stack.
     * 返回 NDC 栈的副本。
     *
     * @return a copy of the stack - 栈的副本
     */
    public static Deque<String> getCopyOfStack() {
        NDCAdapter adapter = getAdapter();
        return adapter != null ? adapter.getCopyOfStack() : null;
    }

    /**
     * Sets the NDC stack.
     * 设置 NDC 栈。
     *
     * @param stack the stack to set - 要设置的栈
     */
    public static void setStack(Deque<String> stack) {
        NDCAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setStack(stack);
        }
    }

    /**
     * Creates an NDC scope for automatic cleanup.
     * 创建用于自动清理的 NDC 作用域。
     *
     * @param message the message to push - 要推入的消息
     * @return the scope - 作用域
     */
    public static NDCScope scope(String message) {
        push(message);
        return new NDCScope();
    }

    private static NDCAdapter getAdapter() {
        return LogProviderFactory.getProvider().getNDCAdapter();
    }

    /**
     * NDC Scope - AutoCloseable for automatic pop.
     * NDC 作用域 - 用于自动弹出的 AutoCloseable。
     */
    public static final class NDCScope implements AutoCloseable {
        @Override
        public void close() {
            pop();
        }
    }
}
