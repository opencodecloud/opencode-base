package cloud.opencode.base.log.filter;

/**
 * Filter Action Enum - Defines Possible Filter Decisions
 * 过滤器动作枚举 - 定义可能的过滤器决策
 *
 * <p>Represents the three possible outcomes of a log filter evaluation:</p>
 * <p>表示日志过滤器评估的三种可能结果：</p>
 * <ul>
 *   <li>{@link #ACCEPT} - Accept the log event unconditionally - 无条件接受日志事件</li>
 *   <li>{@link #DENY} - Deny the log event unconditionally - 无条件拒绝日志事件</li>
 *   <li>{@link #NEUTRAL} - No decision, pass to next filter - 不做决策，传递给下一个过滤器</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public enum FilterAction {

    /**
     * Accept the log event unconditionally.
     * 无条件接受日志事件。
     */
    ACCEPT,

    /**
     * Deny the log event unconditionally.
     * 无条件拒绝日志事件。
     */
    DENY,

    /**
     * No decision; pass to the next filter in the chain.
     * 不做决策；传递给链中的下一个过滤器。
     */
    NEUTRAL
}
