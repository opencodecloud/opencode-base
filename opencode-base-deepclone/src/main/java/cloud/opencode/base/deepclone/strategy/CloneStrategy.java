package cloud.opencode.base.deepclone.strategy;

import cloud.opencode.base.deepclone.CloneContext;

/**
 * Strategy interface for object cloning (SPI)
 * 对象克隆策略接口（SPI）
 *
 * <p>Implementations provide different cloning approaches such as
 * reflection-based, serialization-based, or Unsafe-based cloning.</p>
 * <p>实现提供不同的克隆方法，如基于反射、基于序列化或基于Unsafe的克隆。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Strategy naming - 策略命名</li>
 *   <li>Type support checking - 类型支持检查</li>
 *   <li>Priority ordering - 优先级排序</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class CustomStrategy implements CloneStrategy {
 *     @Override
 *     public String name() {
 *         return "custom";
 *     }
 *
 *     @Override
 *     public <T> T clone(T original, CloneContext context) {
 *         // Custom cloning logic
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public interface CloneStrategy {

    /**
     * Gets the name of this strategy
     * 获取此策略的名称
     *
     * @return the strategy name | 策略名称
     */
    String name();

    /**
     * Clones the object using this strategy
     * 使用此策略克隆对象
     *
     * @param original the original object | 原始对象
     * @param context  the clone context | 克隆上下文
     * @param <T>      the object type | 对象类型
     * @return the cloned object | 克隆的对象
     */
    <T> T clone(T original, CloneContext context);

    /**
     * Gets the priority of this strategy
     * 获取此策略的优先级
     *
     * <p>Lower values indicate higher priority. Default is 100.</p>
     * <p>较小的值表示较高的优先级。默认值为100。</p>
     *
     * @return the priority | 优先级
     */
    default int priority() {
        return 100;
    }

    /**
     * Checks if this strategy supports the given type
     * 检查此策略是否支持给定类型
     *
     * @param type the type to check | 要检查的类型
     * @return true if supported | 如果支持返回true
     */
    default boolean supports(Class<?> type) {
        return true;
    }
}
