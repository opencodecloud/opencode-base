package cloud.opencode.base.cache.event;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Cache Event Listener - Receives notifications about cache events
 * 缓存事件监听器 - 接收缓存事件通知
 *
 * <p>Functional interface for handling cache events. Can be implemented
 * as a lambda or method reference for simple use cases.</p>
 * <p>用于处理缓存事件的函数式接口。可以实现为 lambda 或方法引用以用于简单场景。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple listener - 简单监听器
 * CacheEventListener<String, User> listener = event -> {
 *     System.out.println("Event: " + event.type() + " key=" + event.key());
 * };
 *
 * // Typed listener with filtering - 带过滤的类型监听器
 * CacheEventListener<String, User> writeListener = CacheEventListener
 *     .forTypes(EnumSet.of(EventType.PUT, EventType.REMOVE), event -> {
 *         auditLog.log("Cache write: " + event);
 *     });
 *
 * // Listener for specific event type - 特定事件类型的监听器
 * CacheEventListener<String, User> evictionListener = CacheEventListener
 *     .onEvict(event -> {
 *         metrics.incrementEvictionCount(event.cacheName());
 *     });
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for event handling - 事件处理的函数式接口</li>
 *   <li>Event type filtering - 事件类型过滤</li>
 *   <li>Factory methods for common patterns - 常见模式的工厂方法</li>
 *   <li>Lambda and method reference support - Lambda 和方法引用支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@FunctionalInterface
public interface CacheEventListener<K, V> {

    /**
     * Handle cache event
     * 处理缓存事件
     *
     * @param event the event | 事件
     */
    void onEvent(CacheEvent<K, V> event);

    /**
     * Get event types this listener is interested in (default: all)
     * 获取此监听器感兴趣的事件类型（默认：全部）
     *
     * @return set of event types | 事件类型集合
     */
    default Set<CacheEvent.EventType> interestedEventTypes() {
        return EnumSet.allOf(CacheEvent.EventType.class);
    }

    /**
     * Check if listener is interested in event type
     * 检查监听器是否对事件类型感兴趣
     *
     * @param type event type | 事件类型
     * @return true if interested | 如果感兴趣返回 true
     */
    default boolean isInterestedIn(CacheEvent.EventType type) {
        return interestedEventTypes().contains(type);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create listener for specific event types
     * 为特定事件类型创建监听器
     *
     * @param types    event types to listen for | 要监听的事件类型
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> forTypes(Set<CacheEvent.EventType> types,
                                                    Consumer<CacheEvent<K, V>> consumer) {
        return new CacheEventListener<>() {
            @Override
            public void onEvent(CacheEvent<K, V> event) {
                consumer.accept(event);
            }

            @Override
            public Set<CacheEvent.EventType> interestedEventTypes() {
                return types;
            }
        };
    }

    /**
     * Create listener for PUT events
     * 为 PUT 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onPut(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.PUT), consumer);
    }

    /**
     * Create listener for GET events
     * 为 GET 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onGet(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.GET), consumer);
    }

    /**
     * Create listener for REMOVE events
     * 为 REMOVE 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onRemove(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.REMOVE), consumer);
    }

    /**
     * Create listener for EVICT events
     * 为 EVICT 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onEvict(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.EVICT), consumer);
    }

    /**
     * Create listener for EXPIRE events
     * 为 EXPIRE 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onExpire(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.EXPIRE), consumer);
    }

    /**
     * Create listener for LOAD events
     * 为 LOAD 事件创建监听器
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onLoad(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(CacheEvent.EventType.LOAD), consumer);
    }

    /**
     * Create listener for all removal events (REMOVE, EXPIRE, EVICT)
     * 为所有移除事件创建监听器 (REMOVE, EXPIRE, EVICT)
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onRemoval(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(
                CacheEvent.EventType.REMOVE,
                CacheEvent.EventType.EXPIRE,
                CacheEvent.EventType.EVICT
        ), consumer);
    }

    /**
     * Create listener for all write events (PUT, REMOVE, CLEAR)
     * 为所有写事件创建监听器 (PUT, REMOVE, CLEAR)
     *
     * @param consumer event handler | 事件处理器
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return listener | 监听器
     */
    static <K, V> CacheEventListener<K, V> onWrite(Consumer<CacheEvent<K, V>> consumer) {
        return forTypes(EnumSet.of(
                CacheEvent.EventType.PUT,
                CacheEvent.EventType.REMOVE,
                CacheEvent.EventType.CLEAR
        ), consumer);
    }

    // ==================== Composition | 组合 ====================

    /**
     * Compose with another listener
     * 与另一个监听器组合
     *
     * @param after listener to invoke after this one | 在此监听器之后调用的监听器
     * @return composed listener | 组合的监听器
     */
    default CacheEventListener<K, V> andThen(CacheEventListener<K, V> after) {
        return event -> {
            this.onEvent(event);
            after.onEvent(event);
        };
    }
}
