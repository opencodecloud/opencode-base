package cloud.opencode.base.rules.action;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;

import java.util.function.Consumer;

/**
 * Consumer Action - Action Based on Java Consumer
 * 消费者动作 - 基于Java消费者的动作
 *
 * <p>Wraps a Java Consumer as a rule action for flexible action definition.</p>
 * <p>将Java消费者包装为规则动作，以便灵活定义动作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Consumer-to-Action adapter - 消费者到动作的适配器</li>
 *   <li>Lambda-friendly API - Lambda友好的API</li>
 *   <li>Factory method support - 工厂方法支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Action action = new ConsumerAction(ctx -> {
 *     double amount = ctx.get("amount");
 *     ctx.setResult("discount", amount * 0.1);
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (depends on consumer) - 线程安全: 否（取决于消费者）</li>
 *   <li>Null-safe: No (consumer must not be null) - 空值安全: 否（消费者不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class ConsumerAction implements Action {

    private final Consumer<RuleContext> consumer;

    /**
     * Creates a consumer action
     * 创建消费者动作
     *
     * @param consumer the consumer | 消费者
     */
    public ConsumerAction(Consumer<RuleContext> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void execute(RuleContext context) {
        consumer.accept(context);
    }

    /**
     * Creates a consumer action from a consumer
     * 从消费者创建消费者动作
     *
     * @param consumer the consumer | 消费者
     * @return the action | 动作
     */
    public static ConsumerAction of(Consumer<RuleContext> consumer) {
        return new ConsumerAction(consumer);
    }
}
