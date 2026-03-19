package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableList;
import cloud.opencode.base.collections.ImmutableSet;
import cloud.opencode.base.collections.Multimap;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableMultimap - Immutable Multimap Implementation
 * ImmutableMultimap - 不可变多值映射实现
 *
 * <p>A multimap that cannot be modified after creation. Each key can map to
 * multiple values.</p>
 * <p>创建后不能修改的多值映射。每个键可以映射到多个值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Multiple values per key - 每个键多个值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create using builder - 使用构建器创建
 * ImmutableMultimap<String, Integer> multimap = ImmutableMultimap.<String, Integer>builder()
 *     .put("a", 1)
 *     .put("a", 2)
 *     .put("b", 3)
 *     .build();
 *
 * Collection<Integer> values = multimap.get("a"); // [1, 2]
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) - get: O(1)</li>
 *   <li>containsKey: O(1) - containsKey: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (nulls not allowed) - 空值安全: 是（不允许空值）</li>
 * </ul>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class ImmutableMultimap<K, V> implements Multimap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    final Map<K, ? extends Collection<V>> map;
    final int size;

    // ==================== 构造方法 | Constructors ====================

    ImmutableMultimap(Map<K, ? extends Collection<V>> map, int size) {
        this.map = map;
        this.size = size;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Copy from a multimap.
     * 从多值映射复制。
     *
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @param multimap the multimap | 多值映射
     * @return immutable multimap | 不可变多值映射
     */
    public static <K, V> ImmutableMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
        return ImmutableListMultimap.copyOf(multimap);
    }

    // ==================== Multimap 方法 | Multimap Methods ====================

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Collection<V> values : map.values()) {
            if (values.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsEntry(Object key, Object value) {
        Collection<V> values = map.get(key);
        return values != null && values.contains(value);
    }

    @Override
    public abstract Collection<V> get(K key);

    @Override
    public Set<K> keySet() {
        return ImmutableSet.copyOf(map.keySet());
    }

    @Override
    public cloud.opencode.base.collections.Multiset<K> keys() {
        throw new UnsupportedOperationException("keys() not implemented");
    }

    @Override
    public Collection<V> values() {
        List<V> result = new ArrayList<>(size);
        for (Collection<V> values : map.values()) {
            result.addAll(values);
        }
        return ImmutableList.copyOf(result);
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
        List<Map.Entry<K, V>> result = new ArrayList<>(size);
        for (Map.Entry<K, ? extends Collection<V>> entry : map.entrySet()) {
            for (V value : entry.getValue()) {
                result.add(Map.entry(entry.getKey(), value));
            }
        }
        return ImmutableList.copyOf(result);
    }

    @Override
    public abstract Map<K, ? extends Collection<V>> asMap();

    // ==================== 不可变方法 | Immutable Methods ====================

    @Override
    public boolean put(K key, V value) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public boolean putAll(K key, Iterable<? extends V> values) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public void putAll(Multimap<? extends K, ? extends V> multimap) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public Collection<V> removeAll(Object key) {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableMultimap is immutable");
    }

}
