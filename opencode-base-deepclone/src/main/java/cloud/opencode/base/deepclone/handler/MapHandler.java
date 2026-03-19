package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;

import java.util.*;
import java.util.concurrent.*;

/**
 * Handler for cloning Map types
 * Map类型克隆处理器
 *
 * <p>Handles all Map types including HashMap, TreeMap, LinkedHashMap, and concurrent implementations.
 * Both keys and values can be deep cloned.</p>
 * <p>处理所有Map类型，包括HashMap、TreeMap、LinkedHashMap和并发实现。
 * 键和值都可以被深度克隆。</p>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>HashMap, LinkedHashMap, TreeMap, WeakHashMap</li>
 *   <li>Hashtable, IdentityHashMap, EnumMap</li>
 *   <li>ConcurrentHashMap, ConcurrentSkipListMap</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep clone all Map types - 深度克隆所有Map类型</li>
 *   <li>Preserves original map type - 保留原始Map类型</li>
 *   <li>Recursive key and value cloning - 递归键值克隆</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MapHandler handler = new MapHandler();
 * Map<String, User> cloned = (Map<String, User>) handler.clone(originalMap, cloner, context);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
public final class MapHandler implements TypeHandler<Map<?, ?>> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<?, ?> clone(Map<?, ?> original, Cloner cloner, CloneContext context) {
        if (original == null) {
            return null;
        }

        Map clone = createInstance(original.getClass(), original.size());
        context.registerCloned(original, clone);

        for (Map.Entry<?, ?> entry : original.entrySet()) {
            Object key = cloner.clone(entry.getKey(), context);
            Object value = cloner.clone(entry.getValue(), context);
            clone.put(key, value);
        }

        return clone;
    }

    /**
     * Clones a Map with deep cloning of both keys and values
     * 克隆Map并深度克隆键和值
     *
     * @param map     the original map | 原始Map
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <K>     the key type | 键类型
     * @param <V>     the value type | 值类型
     * @return the cloned map | 克隆的Map
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> cloneDeep(Map<K, V> map, Cloner cloner, CloneContext context) {
        return (Map<K, V>) clone(map, cloner, context);
    }

    /**
     * Clones a Map with only values deep cloned (keys are shared)
     * 克隆Map并仅深度克隆值（键共享）
     *
     * @param map     the original map | 原始Map
     * @param cloner  the cloner | 克隆器
     * @param context the context | 上下文
     * @param <K>     the key type | 键类型
     * @param <V>     the value type | 值类型
     * @return the cloned map | 克隆的Map
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> cloneValues(Map<K, V> map, Cloner cloner, CloneContext context) {
        if (map == null) {
            return null;
        }

        Map<K, V> clone = createInstance(map.getClass(), map.size());
        context.registerCloned(map, clone);

        for (Map.Entry<K, V> entry : map.entrySet()) {
            V value = cloner.clone(entry.getValue(), context);
            clone.put(entry.getKey(), value);
        }

        return clone;
    }

    /**
     * Creates an instance of the specified map type
     * 创建指定Map类型的实例
     *
     * @param type the map type | Map类型
     * @param size the expected size | 预期大小
     * @param <K>  the key type | 键类型
     * @param <V>  the value type | 值类型
     * @return the new map instance | 新的Map实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> createInstance(Class<?> type, int size) {
        // Ordered maps
        if (LinkedHashMap.class.isAssignableFrom(type)) {
            return new LinkedHashMap<>(size);
        }
        if (TreeMap.class.isAssignableFrom(type)) {
            return new TreeMap<>();
        }

        // Concurrent maps
        if (ConcurrentHashMap.class.isAssignableFrom(type)) {
            return new ConcurrentHashMap<>(size);
        }
        if (ConcurrentSkipListMap.class.isAssignableFrom(type)) {
            return new ConcurrentSkipListMap<>();
        }

        // Other maps
        if (HashMap.class.isAssignableFrom(type)) {
            return new HashMap<>(size);
        }
        if (Hashtable.class.isAssignableFrom(type)) {
            return new Hashtable<>(size);
        }
        if (WeakHashMap.class.isAssignableFrom(type)) {
            return new WeakHashMap<>(size);
        }
        if (IdentityHashMap.class.isAssignableFrom(type)) {
            return new IdentityHashMap<>(size);
        }

        // Default to HashMap
        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>(size);
        }

        throw OpenDeepCloneException.unsupportedType(type);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type != null && Map.class.isAssignableFrom(type);
    }

    @Override
    public int priority() {
        return 20;
    }
}
