package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.key.TypedKey;

import java.util.List;
import java.util.Optional;

/**
 * Fact Store Interface - Manages Facts in Rule Context
 * 事实存储接口 - 管理规则上下文中的事实
 *
 * <p>Provides typed storage and retrieval of fact objects used during rule evaluation.</p>
 * <p>提供在规则评估期间使用的事实对象的类型化存储和检索。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-based fact retrieval - 基于类型的事实检索</li>
 *   <li>Named fact support - 命名事实支持</li>
 *   <li>Multiple facts of same type - 同类型多个事实</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FactStore store = new DefaultFactStore();
 * store.add("customer", customer);
 * store.add(order);
 * Optional<Order> order = store.get(Order.class);
 * Object customer = store.get("customer");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (names and types must not be null) - 空值安全: 否（名称和类型不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface FactStore {

    /**
     * Adds a fact to the store
     * 向存储添加事实
     *
     * @param fact the fact object | 事实对象
     */
    void add(Object fact);

    /**
     * Adds a named fact to the store
     * 向存储添加命名事实
     *
     * @param name the fact name | 事实名称
     * @param fact the fact object | 事实对象
     */
    void add(String name, Object fact);

    /**
     * Gets a fact by type
     * 按类型获取事实
     *
     * @param type the fact type | 事实类型
     * @param <T>  the fact type | 事实类型
     * @return optional containing the fact | 包含事实的Optional
     */
    <T> Optional<T> get(Class<T> type);

    /**
     * Gets a fact by name
     * 按名称获取事实
     *
     * @param name the fact name | 事实名称
     * @return the fact, or null if not found | 事实，如果未找到则为null
     */
    Object get(String name);

    /**
     * Gets all facts of a specific type
     * 获取特定类型的所有事实
     *
     * @param type the fact type | 事实类型
     * @param <T>  the fact type | 事实类型
     * @return list of facts | 事实列表
     */
    <T> List<T> getAll(Class<T> type);

    /**
     * Checks if a named fact exists
     * 检查命名事实是否存在
     *
     * @param name the fact name | 事实名称
     * @return true if exists | 如果存在返回true
     */
    boolean contains(String name);

    /**
     * Checks if a fact of the given type exists
     * 检查给定类型的事实是否存在
     *
     * @param type the fact type | 事实类型
     * @return true if exists | 如果存在返回true
     */
    boolean contains(Class<?> type);

    /**
     * Removes a named fact
     * 移除命名事实
     *
     * @param name the fact name | 事实名称
     * @return the removed fact, or null | 被移除的事实，或null
     */
    Object remove(String name);

    /**
     * Removes all facts of a specific type
     * 移除特定类型的所有事实
     *
     * @param type the fact type | 事实类型
     * @param <T>  the fact type | 事实类型
     * @return list of removed facts | 被移除的事实列表
     */
    <T> List<T> removeAll(Class<T> type);

    /**
     * Clears all facts
     * 清除所有事实
     */
    void clear();

    /**
     * Gets the total number of facts
     * 获取事实总数
     *
     * @return fact count | 事实数量
     */
    int size();

    /**
     * Gets a typed fact value by typed key
     * 通过类型化键获取类型化事实值
     *
     * @param key the typed key | 类型化键
     * @param <T> the value type | 值类型
     * @return optional containing the typed value | 包含类型化值的Optional
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    default <T> Optional<T> get(TypedKey<T> key) {
        Object value = get(key.name());
        if (value == null) return Optional.empty();
        return key.type().isInstance(value) ? Optional.of(key.type().cast(value)) : Optional.empty();
    }

    /**
     * Puts a typed value by typed key
     * 通过类型化键存放类型化值
     *
     * @param key   the typed key | 类型化键
     * @param value the value | 值
     * @param <T>   the value type | 值类型
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    default <T> void put(TypedKey<T> key, T value) {
        add(key.name(), value);
    }

    /**
     * Checks if a typed key exists in the store
     * 检查类型化键是否存在于存储中
     *
     * @param key the typed key | 类型化键
     * @param <T> the value type | 值类型
     * @return true if exists | 如果存在返回true
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    default <T> boolean contains(TypedKey<T> key) {
        return contains(key.name());
    }
}
