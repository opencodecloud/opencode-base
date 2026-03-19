package cloud.opencode.base.rules.listener;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging Rule Listener - Logs Rule Execution Events
 * 日志规则监听器 - 记录规则执行事件
 *
 * <p>Logs rule execution lifecycle events using JUL (java.util.logging).</p>
 * <p>使用JUL（java.util.logging）记录规则执行生命周期事件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JUL-based logging - 基于JUL的日志记录</li>
 *   <li>Evaluation and execution event logging - 评估和执行事件日志</li>
 *   <li>Failure logging with stack trace - 带堆栈跟踪的失败日志</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * engine.addListener(new LoggingRuleListener());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, logger is thread-safe) - 线程安全: 是（无状态，日志器是线程安全的）</li>
 *   <li>Null-safe: No (rule and context must not be null) - 空值安全: 否（规则和上下文不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class LoggingRuleListener implements RuleListener {

    private static final Logger logger = Logger.getLogger(LoggingRuleListener.class.getName());

    @Override
    public void beforeEvaluate(Rule rule, RuleContext context) {
        logger.fine(() -> "Evaluating rule: " + rule.getName());
    }

    @Override
    public void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
        logger.fine(() -> "Rule " + rule.getName() + " evaluated: " + (satisfied ? "satisfied" : "not satisfied"));
    }

    @Override
    public void beforeExecute(Rule rule, RuleContext context) {
        logger.fine(() -> "Executing rule: " + rule.getName());
    }

    @Override
    public void afterExecute(Rule rule, RuleContext context) {
        logger.info(() -> "Rule fired: " + rule.getName());
    }

    @Override
    public void onFailure(Rule rule, RuleContext context, Exception exception) {
        logger.log(Level.WARNING, "Rule execution failed: " + rule.getName(), exception);
    }

    @Override
    public void onStart(RuleContext context) {
        logger.fine("Rule engine started");
    }

    @Override
    public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
        logger.info(() -> "Rule engine finished: fired " + firedCount + " rules in " + elapsedMillis + "ms");
    }
}
