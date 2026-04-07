
package cloud.opencode.base.json.adapter;

/**
 * JSON Type Adapter Factory - Factory for Creating Type Adapters
 * JSON 类型适配器工厂 - 创建类型适配器的工厂
 *
 * <p>This interface defines a factory for creating {@link JsonTypeAdapter}
 * instances based on the target type. Implementations can provide adapters
 * for families of types (e.g., all enums, all collections).</p>
 * <p>此接口定义了基于目标类型创建 {@link JsonTypeAdapter} 实例的工厂。
 * 实现可以为类型族提供适配器（例如所有枚举、所有集合）。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonTypeAdapterFactory factory = new JsonTypeAdapterFactory() {
 *     @Override
 *     public <T> JsonTypeAdapter<T> create(Class<T> type) {
 *         if (type.isEnum()) {
 *             return createEnumAdapter(type);
 *         }
 *         return null; // not supported
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic adapter creation for type families - 为类型族动态创建适配器</li>
 *   <li>Returns null when the factory cannot handle a type - 当工厂无法处理类型时返回 null</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Returns null for unsupported types - 空值安全: 对不支持的类型返回 null</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface JsonTypeAdapterFactory {

    /**
     * Creates a type adapter for the given type.
     * 为给定类型创建类型适配器。
     *
     * @param type the target type - 目标类型
     * @param <T>  the type parameter - 类型参数
     * @return the adapter, or null if this factory does not support the type
     *         适配器，如果此工厂不支持该类型则返回 null
     */
    <T> JsonTypeAdapter<T> create(Class<T> type);
}
