package cloud.opencode.base.deepclone.contract;

import cloud.opencode.base.deepclone.Cloner;

/**
 * Contract interface for objects that support deep cloning
 * 支持深度克隆的对象契约接口
 *
 * <p>Classes implementing this interface can provide custom deep clone logic,
 * which will be used by the cloner instead of reflection-based cloning.</p>
 * <p>实现此接口的类可以提供自定义深度克隆逻辑，克隆器将使用该逻辑而非基于反射的克隆。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class Product implements DeepCloneable<Product> {
 *     private String id;
 *     private String name;
 *     private List<String> tags;
 *
 *     @Override
 *     public Product deepClone() {
 *         Product copy = new Product();
 *         copy.id = this.id;
 *         copy.name = this.name;
 *         copy.tags = new ArrayList<>(this.tags);
 *         return copy;
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom deep clone logic - 自定义深度克隆逻辑</li>
 *   <li>Cloner integration - 克隆器集成</li>
 *   <li>Type-safe contract - 类型安全契约</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @param <T> the type of the cloned object | 克隆对象的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public interface DeepCloneable<T> {

    /**
     * Performs a deep clone of this object
     * 执行此对象的深度克隆
     *
     * @return a deep copy of this object | 此对象的深度副本
     */
    T deepClone();

    /**
     * Performs a deep clone using the specified cloner
     * 使用指定的克隆器执行深度克隆
     *
     * @param cloner the cloner to use | 要使用的克隆器
     * @return a deep copy of this object | 此对象的深度副本
     */
    @SuppressWarnings("unchecked")
    default T deepClone(Cloner cloner) {
        return cloner.clone((T) this);
    }
}
