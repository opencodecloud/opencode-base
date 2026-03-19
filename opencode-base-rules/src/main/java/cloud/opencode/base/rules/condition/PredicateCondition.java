package cloud.opencode.base.rules.condition;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Condition;

import java.util.function.Predicate;

/**
 * Predicate Condition - Condition Based on Java Predicate
 * 谓词条件 - 基于Java谓词的条件
 *
 * <p>Wraps a Java Predicate as a rule condition for flexible condition definition.</p>
 * <p>将Java谓词包装为规则条件，以便灵活定义条件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Predicate-to-Condition adapter - 谓词到条件的适配器</li>
 *   <li>Lambda-friendly API - Lambda友好的API</li>
 *   <li>Factory method support - 工厂方法支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Condition condition = new PredicateCondition(
 *     ctx -> ctx.<Integer>get("age") >= 18
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (depends on predicate) - 线程安全: 否（取决于谓词）</li>
 *   <li>Null-safe: No (predicate must not be null) - 空值安全: 否（谓词不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class PredicateCondition implements Condition {

    private final Predicate<RuleContext> predicate;

    /**
     * Creates a predicate condition
     * 创建谓词条件
     *
     * @param predicate the predicate | 谓词
     */
    public PredicateCondition(Predicate<RuleContext> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean evaluate(RuleContext context) {
        return predicate.test(context);
    }

    /**
     * Creates a predicate condition from a predicate
     * 从谓词创建谓词条件
     *
     * @param predicate the predicate | 谓词
     * @return the condition | 条件
     */
    public static PredicateCondition of(Predicate<RuleContext> predicate) {
        return new PredicateCondition(predicate);
    }
}
