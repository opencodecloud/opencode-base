package cloud.opencode.base.deepclone;

/**
 * Interface for object cloners
 * 对象克隆器接口
 *
 * <p>Defines the contract for deep cloning objects. Implementations may use
 * different strategies such as reflection, serialization, or Unsafe.</p>
 * <p>定义深度克隆对象的契约。实现可以使用不同的策略，如反射、序列化或Unsafe。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy-based cloning - 基于策略的克隆</li>
 *   <li>Context-aware cloning - 上下文感知克隆</li>
 *   <li>Type support checking - 类型支持检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Cloner cloner = OpenClone.builder()
 *     .reflective()
 *     .maxDepth(50)
 *     .build();
 *
 * User cloned = cloner.clone(originalUser);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public interface Cloner {

    /**
     * Deep clones an object
     * 深度克隆对象
     *
     * @param original the original object | 原始对象
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    <T> T clone(T original);

    /**
     * Deep clones an object using a specific context
     * 使用特定上下文深度克隆对象
     *
     * @param original the original object | 原始对象
     * @param context  the clone context | 克隆上下文
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    <T> T clone(T original, CloneContext context);

    /**
     * Gets the name of the cloning strategy
     * 获取克隆策略名称
     *
     * @return the strategy name | 策略名称
     */
    String getStrategyName();

    /**
     * Checks if this cloner supports the given type
     * 检查此克隆器是否支持给定类型
     *
     * @param type the type to check | 要检查的类型
     * @return true if supported | 如果支持返回true
     */
    boolean supports(Class<?> type);
}
