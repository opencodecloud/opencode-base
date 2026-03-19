package cloud.opencode.base.collections.specialized;

import java.util.Map;

/**
 * ClassToInstanceMap - Class to Instance Map Interface
 * ClassToInstanceMap - 类实例映射接口
 *
 * <p>A type-safe map from Class objects to instances of that class.</p>
 * <p>从 Class 对象到该类实例的类型安全映射。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe retrieval - 类型安全检索</li>
 *   <li>Type-safe storage - 类型安全存储</li>
 *   <li>Extends Map interface - 扩展 Map 接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
 * map.putInstance(String.class, "hello");
 * map.putInstance(Integer.class, 42);
 *
 * String str = map.getInstance(String.class);  // Type-safe
 * Integer num = map.getInstance(Integer.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 取决于实现</li>
 *   <li>Null-safe: Partial (getInstance returns null for missing) - 部分（getInstance对缺失返回null）</li>
 * </ul>
 * @param <B> base type | 基础类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {

    /**
     * Get the instance for the given type in a type-safe manner.
     * 以类型安全的方式获取给定类型的实例。
     *
     * @param <T>  the type | 类型
     * @param type the class | 类
     * @return the instance, or null if not found | 实例，未找到则返回 null
     */
    <T extends B> T getInstance(Class<T> type);

    /**
     * Put the instance for the given type in a type-safe manner.
     * 以类型安全的方式放入给定类型的实例。
     *
     * @param <T>   the type | 类型
     * @param type  the class | 类
     * @param value the instance | 实例
     * @return the previous instance, or null if none | 之前的实例，如果没有则返回 null
     */
    <T extends B> T putInstance(Class<T> type, T value);
}
