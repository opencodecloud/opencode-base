package cloud.opencode.base.collections.immutable;

import cloud.opencode.base.collections.ImmutableMap;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * ImmutableClassToInstanceMap - Immutable Class to Instance Map
 * ImmutableClassToInstanceMap - 不可变类型实例映射
 *
 * <p>A type-safe map from Class objects to instances of that class.
 * Cannot be modified after creation.</p>
 * <p>从 Class 对象到该类实例的类型安全映射。创建后不能修改。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable - 不可变</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Type-safe instance retrieval - 类型安全的实例获取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.builder()
 *     .put(String.class, "hello")
 *     .put(Integer.class, 42)
 *     .build();
 *
 * String str = map.getInstance(String.class);  // Type-safe
 * Integer num = map.getInstance(Integer.class);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>getInstance: O(1) - getInstance: O(1)</li>
 *   <li>containsKey: O(1) - containsKey: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (nulls not allowed) - 空值安全: 是（不允许空值）</li>
 * </ul>
 *
 * @param <B> base type | 基础类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-collections V1.0.0
 */
public final class ImmutableClassToInstanceMap<B> extends AbstractMap<Class<? extends B>, B>
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private static final ImmutableClassToInstanceMap EMPTY = new ImmutableClassToInstanceMap<>(Map.of());

    private final Map<Class<? extends B>, B> delegate;
    private transient Set<Entry<Class<? extends B>, B>> entrySet;

    // ==================== 构造方法 | Constructors ====================

    private ImmutableClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
        this.delegate = delegate;
    }

    // ==================== 工厂方法 | Factory Methods ====================

    /**
     * Return an empty immutable class to instance map.
     * 返回空不可变类型实例映射。
     *
     * @param <B> base type | 基础类型
     * @return empty immutable class to instance map | 空不可变类型实例映射
     */
    @SuppressWarnings("unchecked")
    public static <B> ImmutableClassToInstanceMap<B> of() {
        return (ImmutableClassToInstanceMap<B>) EMPTY;
    }

    /**
     * Return an immutable class to instance map with one entry.
     * 返回包含一个条目的不可变类型实例映射。
     *
     * @param <B>   base type | 基础类型
     * @param <T>   instance type | 实例类型
     * @param type  the class | 类
     * @param value the instance | 实例
     * @return immutable class to instance map | 不可变类型实例映射
     */
    public static <B, T extends B> ImmutableClassToInstanceMap<B> of(Class<T> type, T value) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
        return new ImmutableClassToInstanceMap<>(Map.of(type, value));
    }

    /**
     * Copy from a map.
     * 从映射复制。
     *
     * @param <B> base type | 基础类型
     * @param map the map | 映射
     * @return immutable class to instance map | 不可变类型实例映射
     */
    public static <B> ImmutableClassToInstanceMap<B> copyOf(
            Map<? extends Class<? extends B>, ? extends B> map) {
        if (map instanceof ImmutableClassToInstanceMap<?>) {
            @SuppressWarnings("unchecked")
            ImmutableClassToInstanceMap<B> result = (ImmutableClassToInstanceMap<B>) map;
            return result;
        }
        if (map.isEmpty()) {
            return of();
        }
        Builder<B> builder = builder();
        for (Entry<? extends Class<? extends B>, ? extends B> entry : map.entrySet()) {
            builder.putUnchecked(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    /**
     * Create a builder.
     * 创建构建器。
     *
     * @param <B> base type | 基础类型
     * @return builder | 构建器
     */
    public static <B> Builder<B> builder() {
        return new Builder<>();
    }

    // ==================== 类型安全方法 | Type-safe Methods ====================

    /**
     * Get the instance for the given type in a type-safe manner.
     * 以类型安全的方式获取给定类型的实例。
     *
     * @param <T>  the type | 类型
     * @param type the class | 类
     * @return the instance, or null if not found | 实例，未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends B> T getInstance(Class<T> type) {
        return (T) delegate.get(type);
    }

    // ==================== Map 方法 | Map Methods ====================

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public B get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Set<Entry<Class<? extends B>, B>> entrySet() {
        if (entrySet == null) {
            entrySet = Collections.unmodifiableSet(delegate.entrySet());
        }
        return entrySet;
    }

    @Override
    public B put(Class<? extends B> key, B value) {
        throw new UnsupportedOperationException("ImmutableClassToInstanceMap is immutable");
    }

    @Override
    public B remove(Object key) {
        throw new UnsupportedOperationException("ImmutableClassToInstanceMap is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableClassToInstanceMap is immutable");
    }

    // ==================== 内部类 | Inner Classes ====================

    /**
     * Builder for ImmutableClassToInstanceMap.
     * ImmutableClassToInstanceMap 构建器。
     *
     * @param <B> base type | 基础类型
     */
    public static final class Builder<B> {
        private final Map<Class<? extends B>, B> entries = new LinkedHashMap<>();

        Builder() {
        }

        /**
         * Put instance in a type-safe manner.
         * 以类型安全的方式放入实例。
         *
         * @param <T>   the type | 类型
         * @param type  the class | 类
         * @param value the instance | 实例
         * @return this builder | 此构建器
         */
        public <T extends B> Builder<B> put(Class<T> type, T value) {
            Objects.requireNonNull(type);
            Objects.requireNonNull(value);
            if (!type.isInstance(value)) {
                throw new ClassCastException("Value is not an instance of " + type);
            }
            entries.put(type, value);
            return this;
        }

        /**
         * Put all entries from a map.
         * 从映射放入所有条目。
         *
         * @param map the map | 映射
         * @return this builder | 此构建器
         */
        @SuppressWarnings("unchecked")
        public Builder<B> putAll(Map<? extends Class<? extends B>, ? extends B> map) {
            for (Entry<? extends Class<? extends B>, ? extends B> entry : map.entrySet()) {
                Class<? extends B> type = entry.getKey();
                B value = entry.getValue();
                put((Class<B>) type, value);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        void putUnchecked(Class<? extends B> type, B value) {
            entries.put(type, value);
        }

        /**
         * Build the immutable class to instance map.
         * 构建不可变类型实例映射。
         *
         * @return immutable class to instance map | 不可变类型实例映射
         */
        public ImmutableClassToInstanceMap<B> build() {
            if (entries.isEmpty()) {
                return of();
            }
            return new ImmutableClassToInstanceMap<>(Map.copyOf(entries));
        }
    }
}
