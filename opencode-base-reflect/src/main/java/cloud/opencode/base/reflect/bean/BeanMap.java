package cloud.opencode.base.reflect.bean;

import java.util.*;

/**
 * Bean Map
 * Bean映射
 *
 * <p>Provides Map interface view of a bean's properties.</p>
 * <p>提供bean属性的Map接口视图。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map interface view of bean properties - bean属性的Map接口视图</li>
 *   <li>Read/write through Map operations - 通过Map操作读写</li>
 *   <li>Cross-bean property copying - 跨bean属性复制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BeanMap<User> map = BeanMap.from(user);
 * Object name = map.get("name");
 * map.put("name", "Alice");
 * Map<String, Object> plain = map.toMap();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 线程安全: 否（未同步）</li>
 *   <li>Null-safe: No (bean must be non-null) - 空值安全: 否（bean须非空）</li>
 * </ul>
 *
 * @param <T> the bean type | bean类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class BeanMap<T> extends AbstractMap<String, Object> {

    private final T bean;
    private final Map<String, PropertyDescriptor> descriptors;
    private final boolean ignoreUnknownProperties;

    /**
     * Creates a BeanMap
     * 创建BeanMap
     *
     * @param bean        the bean | bean
     * @param descriptors the property descriptors | 属性描述符
     */
    public BeanMap(T bean, Map<String, PropertyDescriptor> descriptors) {
        this(bean, descriptors, true);
    }

    /**
     * Creates a BeanMap with configuration
     * 创建带配置的BeanMap
     *
     * @param bean                    the bean | bean
     * @param descriptors             the property descriptors | 属性描述符
     * @param ignoreUnknownProperties whether to ignore unknown properties | 是否忽略未知属性
     */
    public BeanMap(T bean, Map<String, PropertyDescriptor> descriptors, boolean ignoreUnknownProperties) {
        this.bean = Objects.requireNonNull(bean, "bean must not be null");
        this.descriptors = Objects.requireNonNull(descriptors, "descriptors must not be null");
        this.ignoreUnknownProperties = ignoreUnknownProperties;
    }

    /**
     * Creates a BeanMap from a bean
     * 从bean创建BeanMap
     *
     * @param bean the bean | bean
     * @param <T>  the bean type | bean类型
     * @return the BeanMap | BeanMap
     */
    public static <T> BeanMap<T> from(T bean) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) bean.getClass();
        Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(clazz);
        return new BeanMap<>(bean, descriptors);
    }

    /**
     * Gets the underlying bean
     * 获取底层bean
     *
     * @return the bean | bean
     */
    public T getBean() {
        return bean;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String name)) {
            return null;
        }
        PropertyDescriptor descriptor = descriptors.get(name);
        if (descriptor == null || !descriptor.isReadable()) {
            return null;
        }
        return descriptor.getValue(bean);
    }

    @Override
    public Object put(String key, Object value) {
        PropertyDescriptor descriptor = descriptors.get(key);
        if (descriptor == null) {
            if (ignoreUnknownProperties) {
                return null;
            }
            throw new IllegalArgumentException("Unknown property: " + key);
        }
        if (!descriptor.isWritable()) {
            if (ignoreUnknownProperties) {
                return descriptor.getValue(bean);
            }
            throw new IllegalArgumentException("Property is not writable: " + key);
        }
        Object oldValue = descriptor.isReadable() ? descriptor.getValue(bean) : null;
        descriptor.setValue(bean, value);
        return oldValue;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && descriptors.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(descriptors.keySet());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entries = new LinkedHashSet<>();
        for (Map.Entry<String, PropertyDescriptor> entry : descriptors.entrySet()) {
            PropertyDescriptor descriptor = entry.getValue();
            if (descriptor.isReadable()) {
                entries.add(new BeanEntry(entry.getKey(), descriptor));
            }
        }
        return entries;
    }

    @Override
    public int size() {
        return descriptors.size();
    }

    /**
     * Copies all properties to another bean
     * 复制所有属性到另一个bean
     *
     * @param target the target bean | 目标bean
     * @param <U>    the target type | 目标类型
     */
    public <U> void copyTo(U target) {
        BeanMap<U> targetMap = BeanMap.from(target);
        for (String key : keySet()) {
            if (targetMap.containsKey(key)) {
                targetMap.put(key, get(key));
            }
        }
    }

    /**
     * Copies properties from a Map
     * 从Map复制属性
     *
     * @param source the source map | 源映射
     */
    public void copyFrom(Map<String, ?> source) {
        for (Map.Entry<String, ?> entry : source.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Converts to a regular Map
     * 转换为普通Map
     *
     * @return the map | 映射
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Entry<String, Object> entry : entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Gets only readable properties as Map
     * 仅获取可读属性为Map
     *
     * @return the map | 映射
     */
    public Map<String, Object> getReadableProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, PropertyDescriptor> entry : descriptors.entrySet()) {
            PropertyDescriptor descriptor = entry.getValue();
            if (descriptor.isReadable()) {
                result.put(entry.getKey(), descriptor.getValue(bean));
            }
        }
        return result;
    }

    /**
     * Gets property descriptor by name
     * 按名称获取属性描述符
     *
     * @param name the property name | 属性名
     * @return the descriptor or null | 描述符或null
     */
    public PropertyDescriptor getPropertyDescriptor(String name) {
        return descriptors.get(name);
    }

    private class BeanEntry implements Entry<String, Object> {
        private final String key;
        private final PropertyDescriptor descriptor;

        BeanEntry(String key, PropertyDescriptor descriptor) {
            this.key = key;
            this.descriptor = descriptor;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return descriptor.getValue(bean);
        }

        @Override
        public Object setValue(Object value) {
            Object oldValue = descriptor.isReadable() ? descriptor.getValue(bean) : null;
            descriptor.setValue(bean, value);
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry<?, ?> e)) return false;
            return key.equals(e.getKey()) && Objects.equals(getValue(), e.getValue());
        }

        @Override
        public int hashCode() {
            return key.hashCode() ^ Objects.hashCode(getValue());
        }
    }
}
