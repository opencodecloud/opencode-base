package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;

/**
 * Interface for type-specific clone handlers
 * 特定类型克隆处理器接口
 *
 * <p>Implementations provide specialized cloning logic for specific types,
 * such as arrays, collections, maps, or records.</p>
 * <p>实现为特定类型提供专门的克隆逻辑，如数组、集合、Map或Record。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-specific cloning - 特定类型克隆</li>
 *   <li>Priority-based ordering - 基于优先级排序</li>
 *   <li>Recursive cloning support - 递归克隆支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class DateHandler implements TypeHandler<LocalDateTime> {
 *     @Override
 *     public LocalDateTime clone(LocalDateTime original, Cloner cloner, CloneContext context) {
 *         return original; // Immutable, return same reference
 *     }
 *
 *     @Override
 *     public boolean supports(Class<?> type) {
 *         return LocalDateTime.class.isAssignableFrom(type);
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @param <T> the type this handler processes | 此处理器处理的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public interface TypeHandler<T> {

    /**
     * Clones the object
     * 克隆对象
     *
     * @param original the original object | 原始对象
     * @param cloner   the cloner for recursive cloning | 用于递归克隆的克隆器
     * @param context  the clone context | 克隆上下文
     * @return the cloned object | 克隆的对象
     */
    T clone(T original, Cloner cloner, CloneContext context);

    /**
     * Checks if this handler supports the given type
     * 检查此处理器是否支持给定类型
     *
     * @param type the type to check | 要检查的类型
     * @return true if supported | 如果支持返回true
     */
    boolean supports(Class<?> type);

    /**
     * Gets the priority of this handler
     * 获取此处理器的优先级
     *
     * <p>Lower values indicate higher priority. Default is 100.</p>
     * <p>较小的值表示较高的优先级。默认值为100。</p>
     *
     * @return the priority | 优先级
     */
    default int priority() {
        return 100;
    }
}
