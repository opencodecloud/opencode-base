package cloud.opencode.base.collections;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * AbstractMultimap - Abstract Base for Multimap Implementations
 * AbstractMultimap - Multimap 实现的抽象基类
 *
 * <p>Provides skeletal implementation for Multimap. Subclasses need to implement
 * the collection factory method.</p>
 * <p>为 Multimap 提供骨架实现。子类需要实现集合工厂方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Common multimap operations - 通用多重映射操作</li>
 *   <li>Live views - 实时视图</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Implementation dependent - 空值安全: 取决于实现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Subclass provides collection factory
 * Multimap<String, Integer> multimap = ArrayListMultimap.create();
 * multimap.put("a", 1);
 * multimap.put("a", 2);
 * Collection<Integer> values = multimap.get("a"); // [1, 2]
 * }</pre>
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public abstract class AbstractMultimap<K, V> implements Multimap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected final Map<K, Collection<V>> map;
    private int totalSize;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Constructor with backing map.
     * 使用后备映射的构造方法。
     *
     * @param map backing map | 后备映射
     */
    protected AbstractMultimap(Map<K, Collection<V>> map) {
        this.map = map;
        this.totalSize = 0;
    }

    // ==================== 抽象方法 | Abstract Methods ====================

    /**
     * Create a new collection for values.
     * 为值创建新集合。
     *
     * @return new collection | 新集合
     */
    protected abstract Collection<V> createCollection();

    // ==================== Multimap 实现 | Multimap Implementation ====================

    @Override
    public int size() {
        return totalSize;
    }

    @Override
    public boolean isEmpty() {
        return totalSize == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (Collection<V> collection : map.values()) {
            if (collection.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsEntry(Object key, Object value) {
        Collection<V> collection = map.get(key);
        return collection != null && collection.contains(value);
    }

    @Override
    public boolean put(K key, V value) {
        Collection<V> collection = map.computeIfAbsent(key, k -> createCollection());
        boolean changed = collection.add(value);
        if (changed) {
            totalSize++;
        }
        return changed;
    }

    @Override
    public boolean putAll(K key, Iterable<? extends V> values) {
        if (values == null) {
            return false;
        }

        boolean changed = false;
        for (V value : values) {
            if (put(key, value)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Collection<V> get(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return createCollection();
        }
        return collection;
    }

    @Override
    public boolean remove(Object key, Object value) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return false;
        }

        boolean removed = collection.remove(value);
        if (removed) {
            totalSize--;
            if (collection.isEmpty()) {
                map.remove(key);
            }
        }
        return removed;
    }

    @Override
    public Collection<V> removeAll(Object key) {
        Collection<V> collection = map.remove(key);
        if (collection == null) {
            return createCollection();
        }
        totalSize -= collection.size();

        Collection<V> result = createCollection();
        result.addAll(collection);
        return result;
    }

    @Override
    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
        Collection<V> oldValues = removeAll(key);
        if (values != null) {
            putAll(key, values);
        }
        return oldValues;
    }

    @Override
    public void clear() {
        map.clear();
        totalSize = 0;
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Multiset<K> keys() {
        HashMultiset<K> keys = HashMultiset.create();
        for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            keys.add(entry.getKey(), entry.getValue().size());
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>(totalSize);
        for (Collection<V> collection : map.values()) {
            values.addAll(collection);
        }
        return values;
    }

    @Override
    public Collection<Map.Entry<K, V>> entries() {
        List<Map.Entry<K, V>> entries = new ArrayList<>(totalSize);
        for (Map.Entry<K, Collection<V>> entry : map.entrySet()) {
            K key = entry.getKey();
            for (V value : entry.getValue()) {
                entries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
            }
        }
        return entries;
    }

    @Override
    public Map<K, Collection<V>> asMap() {
        return Collections.unmodifiableMap(map);
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Multimap<?, ?> that)) return false;
        return map.equals(that.asMap());
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
