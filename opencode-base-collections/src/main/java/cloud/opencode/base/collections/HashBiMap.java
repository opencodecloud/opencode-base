package cloud.opencode.base.collections;

import cloud.opencode.base.collections.exception.OpenCollectionException;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * HashBiMap - Hash-based BiMap Implementation
 * HashBiMap - 基于哈希的双向映射实现
 *
 * <p>A hash-based implementation of BiMap that maintains bidirectional mapping
 * using two internal HashMaps.</p>
 * <p>基于哈希的 BiMap 实现，使用两个内部 HashMap 维护双向映射。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>O(1) lookups in both directions - 双向 O(1) 查找</li>
 *   <li>Unique keys and values enforced - 强制键和值唯一</li>
 *   <li>Live inverse view - 实时反向视图</li>
 *   <li>Serializable - 可序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create empty BiMap - 创建空 BiMap
 * HashBiMap<String, Integer> bimap = HashBiMap.create();
 *
 * // Create with initial capacity - 创建指定容量
 * HashBiMap<String, Integer> bimap = HashBiMap.create(16);
 *
 * // Create from existing map - 从现有映射创建
 * HashBiMap<String, Integer> bimap = HashBiMap.create(existingMap);
 *
 * // Basic operations - 基本操作
 * bimap.put("a", 1);
 * bimap.get("a");        // 1
 * bimap.inverse().get(1); // "a"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>get: O(1) average - get: O(1) 平均</li>
 *   <li>put: O(1) average - put: O(1) 平均</li>
 *   <li>remove: O(1) average - remove: O(1) 平均</li>
 *   <li>containsValue: O(1) - containsValue: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: No (nulls not allowed) - 空值安全: 否（不允许空值）</li>
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
public class HashBiMap<K, V> implements BiMap<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<K, V> forward;
    private final Map<V, K> backward;
    private transient BiMap<V, K> inverse;

    // ==================== 构造方法 | Constructors ====================

    /**
     * Private constructor.
     * 私有构造方法。
     *
     * @param initialCapacity initial capacity | 初始容量
     */
    private HashBiMap(int initialCapacity) {
        this.forward = new HashMap<>(initialCapacity);
        this.backward = new HashMap<>(initialCapacity);
    }

    /**
     * Private constructor for inverse view.
     * 反向视图的私有构造方法。
     */
    private HashBiMap(Map<K, V> forward, Map<V, K> backward, BiMap<V, K> inverse) {
        this.forward = forward;
        this.backward = backward;
        this.inverse = inverse;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Create an empty HashBiMap.
     * 创建空 HashBiMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return new HashBiMap | 新的 HashBiMap
     */
    public static <K, V> HashBiMap<K, V> create() {
        return new HashBiMap<>(16);
    }

    /**
     * Create an empty HashBiMap with initial capacity.
     * 创建指定容量的空 HashBiMap。
     *
     * @param <K>             key type | 键类型
     * @param <V>             value type | 值类型
     * @param initialCapacity initial capacity | 初始容量
     * @return new HashBiMap | 新的 HashBiMap
     */
    public static <K, V> HashBiMap<K, V> create(int initialCapacity) {
        if (initialCapacity < 0) {
            throw OpenCollectionException.illegalCapacity(initialCapacity);
        }
        return new HashBiMap<>(initialCapacity);
    }

    /**
     * Create a HashBiMap from an existing map.
     * 从现有映射创建 HashBiMap。
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param map source map | 源映射
     * @return new HashBiMap | 新的 HashBiMap
     * @throws IllegalArgumentException if duplicate values exist | 如果存在重复值
     */
    public static <K, V> HashBiMap<K, V> create(Map<? extends K, ? extends V> map) {
        HashBiMap<K, V> bimap = new HashBiMap<>(map.size());
        bimap.putAll(map);
        return bimap;
    }

    // ==================== BiMap 实现 | BiMap Implementation ====================

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        // Check if value already exists with different key
        K existingKey = backward.get(value);
        if (existingKey != null && !existingKey.equals(key)) {
            throw new IllegalArgumentException("Value already present: " + value);
        }

        // Remove old value mapping if key exists
        V oldValue = forward.get(key);
        if (oldValue != null) {
            backward.remove(oldValue);
        }

        forward.put(key, value);
        backward.put(value, key);
        return oldValue;
    }

    @Override
    public V forcePut(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        // Remove any existing entry with the same value
        K existingKey = backward.remove(value);
        if (existingKey != null && !existingKey.equals(key)) {
            forward.remove(existingKey);
        }

        // Remove old value mapping if key exists
        V oldValue = forward.get(key);
        if (oldValue != null) {
            backward.remove(oldValue);
        }

        forward.put(key, value);
        backward.put(value, key);
        return oldValue;
    }

    @Override
    public BiMap<V, K> inverse() {
        if (inverse == null) {
            inverse = new HashBiMap<>(backward, forward, this);
        }
        return inverse;
    }

    // ==================== Map 实现 | Map Implementation ====================

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backward.containsKey(value);
    }

    @Override
    public V get(Object key) {
        return forward.get(key);
    }

    @Override
    public V remove(Object key) {
        V value = forward.remove(key);
        if (value != null) {
            backward.remove(value);
        }
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Set<V> values() {
        return new ValueSet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    // ==================== Object 方法 | Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map<?, ?> that)) return false;
        return forward.equals(that);
    }

    @Override
    public int hashCode() {
        return forward.hashCode();
    }

    @Override
    public String toString() {
        return forward.toString();
    }

    // ==================== 内部类 | Internal Classes ====================

    /**
     * Key set view
     */
    private class KeySet extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return forward.size();
        }

        @Override
        public boolean contains(Object o) {
            return forward.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return HashBiMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            HashBiMap.this.clear();
        }
    }

    /**
     * Value set view
     */
    private class ValueSet extends AbstractSet<V> {
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return backward.size();
        }

        @Override
        public boolean contains(Object o) {
            return backward.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            K key = backward.remove(o);
            if (key != null) {
                forward.remove(key);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            HashBiMap.this.clear();
        }
    }

    /**
     * Entry set view
     */
    private class EntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return forward.size();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry<?, ?> entry)) {
                return false;
            }
            V value = forward.get(entry.getKey());
            return value != null && value.equals(entry.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?, ?> entry)) {
                return false;
            }
            V value = forward.get(entry.getKey());
            if (value != null && value.equals(entry.getValue())) {
                HashBiMap.this.remove(entry.getKey());
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            HashBiMap.this.clear();
        }
    }

    /**
     * Key iterator
     */
    private class KeyIterator implements Iterator<K> {
        private final Iterator<Entry<K, V>> iterator = forward.entrySet().iterator();
        private V currentValue;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            Entry<K, V> entry = iterator.next();
            currentValue = entry.getValue();
            return entry.getKey();
        }

        @Override
        public void remove() {
            iterator.remove();
            if (currentValue != null) {
                backward.remove(currentValue);
            }
        }
    }

    /**
     * Value iterator
     */
    private class ValueIterator implements Iterator<V> {
        private final Iterator<Entry<V, K>> iterator = backward.entrySet().iterator();
        private K currentKey;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public V next() {
            Entry<V, K> entry = iterator.next();
            currentKey = entry.getValue();
            return entry.getKey();
        }

        @Override
        public void remove() {
            iterator.remove();
            if (currentKey != null) {
                forward.remove(currentKey);
            }
        }
    }

    /**
     * Entry iterator
     */
    private class EntryIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> iterator = forward.entrySet().iterator();
        private Entry<K, V> current;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            current = iterator.next();
            return new BiMapEntry(current);
        }

        @Override
        public void remove() {
            iterator.remove();
            if (current != null) {
                backward.remove(current.getValue());
            }
        }
    }

    /**
     * BiMap entry wrapper
     */
    private class BiMapEntry implements Entry<K, V> {
        private final Entry<K, V> delegate;

        BiMapEntry(Entry<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public K getKey() {
            return delegate.getKey();
        }

        @Override
        public V getValue() {
            return delegate.getValue();
        }

        @Override
        public V setValue(V value) {
            Objects.requireNonNull(value, "value");
            V oldValue = delegate.getValue();

            // Check if new value already exists
            K existingKey = backward.get(value);
            if (existingKey != null && !existingKey.equals(delegate.getKey())) {
                throw new IllegalArgumentException("Value already present: " + value);
            }

            backward.remove(oldValue);
            delegate.setValue(value);
            backward.put(value, delegate.getKey());
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry<?, ?> that)) {
                return false;
            }
            return Objects.equals(getKey(), that.getKey())
                    && Objects.equals(getValue(), that.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
