package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.OpenConstructor;
import cloud.opencode.base.reflect.OpenField;
import cloud.opencode.base.reflect.OpenMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Facade Entry Class
 * Bean门面入口类
 *
 * <p>Provides common bean operations API.</p>
 * <p>提供常用bean操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property discovery - 属性发现</li>
 *   <li>Property access - 属性访问</li>
 *   <li>Bean copying - Bean复制</li>
 *   <li>Bean to Map conversion - Bean转Map</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get property descriptors
 * Map<String, PropertyDescriptor> props = OpenBean.getPropertyDescriptors(User.class);
 *
 * // Copy properties
 * OpenBean.copyProperties(source, target);
 *
 * // Bean to Map
 * Map<String, Object> map = OpenBean.toMap(user);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenBean {

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private OpenBean() {
    }

    // ==================== Property Discovery | 属性发现 ====================

    /**
     * Gets all property descriptors for a class
     * 获取类的所有属性描述符
     *
     * @param clazz the class | 类
     * @return map of property name to descriptor | 属性名到描述符的映射
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
        return DESCRIPTOR_CACHE.computeIfAbsent(clazz, OpenBean::discoverProperties);
    }

    /**
     * Gets a property descriptor by name
     * 按名称获取属性描述符
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return the descriptor or null | 描述符或null
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) {
        return getPropertyDescriptors(clazz).get(propertyName);
    }

    /**
     * Gets all property names
     * 获取所有属性名
     *
     * @param clazz the class | 类
     * @return set of property names | 属性名集合
     */
    public static Set<String> getPropertyNames(Class<?> clazz) {
        return getPropertyDescriptors(clazz).keySet();
    }

    /**
     * Gets readable property names
     * 获取可读属性名
     *
     * @param clazz the class | 类
     * @return set of readable property names | 可读属性名集合
     */
    public static Set<String> getReadablePropertyNames(Class<?> clazz) {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, PropertyDescriptor> entry : getPropertyDescriptors(clazz).entrySet()) {
            if (entry.getValue().isReadable()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Gets writable property names
     * 获取可写属性名
     *
     * @param clazz the class | 类
     * @return set of writable property names | 可写属性名集合
     */
    public static Set<String> getWritablePropertyNames(Class<?> clazz) {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, PropertyDescriptor> entry : getPropertyDescriptors(clazz).entrySet()) {
            if (entry.getValue().isWritable()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    // ==================== Property Access | 属性访问 ====================

    /**
     * Gets a property value
     * 获取属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @return the value | 值
     */
    public static Object getProperty(Object bean, String propertyName) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), propertyName);
        if (descriptor == null || !descriptor.isReadable()) {
            throw new IllegalArgumentException("Property not found or not readable: " + propertyName);
        }
        return descriptor.getValue(bean);
    }

    /**
     * Gets a property value with type
     * 获取属性值（带类型）
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @param type         the expected type | 期望类型
     * @param <T>          the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(Object bean, String propertyName, Class<T> type) {
        return (T) getProperty(bean, propertyName);
    }

    /**
     * Sets a property value
     * 设置属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @param value        the value | 值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), propertyName);
        if (descriptor == null || !descriptor.isWritable()) {
            throw new IllegalArgumentException("Property not found or not writable: " + propertyName);
        }
        descriptor.setValue(bean, value);
    }

    /**
     * Sets a property value if writable
     * 如果可写则设置属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @param value        the value | 值
     * @return true if set | 如果已设置返回true
     */
    public static boolean setPropertyIfWritable(Object bean, String propertyName, Object value) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), propertyName);
        if (descriptor != null && descriptor.isWritable()) {
            descriptor.setValue(bean, value);
            return true;
        }
        return false;
    }

    // ==================== Bean Copying | Bean复制 ====================

    /**
     * Copies properties from source to target
     * 从源复制属性到目标
     *
     * @param source the source bean | 源bean
     * @param target the target bean | 目标bean
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, (String[]) null);
    }

    /**
     * Copies properties excluding specified ones
     * 复制属性（排除指定属性）
     *
     * @param source           the source bean | 源bean
     * @param target           the target bean | 目标bean
     * @param excludeProperties properties to exclude | 要排除的属性
     */
    public static void copyProperties(Object source, Object target, String... excludeProperties) {
        Set<String> excludes = excludeProperties != null ?
                new HashSet<>(Arrays.asList(excludeProperties)) : Collections.emptySet();

        Map<String, PropertyDescriptor> sourceDescriptors = getPropertyDescriptors(source.getClass());
        Map<String, PropertyDescriptor> targetDescriptors = getPropertyDescriptors(target.getClass());

        for (Map.Entry<String, PropertyDescriptor> entry : sourceDescriptors.entrySet()) {
            String name = entry.getKey();
            if (excludes.contains(name)) {
                continue;
            }

            PropertyDescriptor sourceDesc = entry.getValue();
            PropertyDescriptor targetDesc = targetDescriptors.get(name);

            if (sourceDesc.isReadable() && targetDesc != null && targetDesc.isWritable()) {
                Object value = sourceDesc.getValue(source);
                targetDesc.setValue(target, value);
            }
        }
    }

    /**
     * Copies and creates a new instance
     * 复制并创建新实例
     *
     * @param source      the source bean | 源bean
     * @param targetClass the target class | 目标类
     * @param <T>         the target type | 目标类型
     * @return the new instance | 新实例
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        T target = OpenConstructor.newInstance(targetClass);
        copyProperties(source, target);
        return target;
    }

    // ==================== Bean to Map | Bean转Map ====================

    /**
     * Converts a bean to a Map
     * 将bean转换为Map
     *
     * @param bean the bean | bean
     * @return the map | 映射
     */
    public static Map<String, Object> toMap(Object bean) {
        return BeanMap.from(bean).toMap();
    }

    /**
     * Creates a BeanMap view
     * 创建BeanMap视图
     *
     * @param bean the bean | bean
     * @param <T>  the bean type | bean类型
     * @return the BeanMap | BeanMap
     */
    public static <T> BeanMap<T> asBeanMap(T bean) {
        return BeanMap.from(bean);
    }

    /**
     * Populates a bean from a Map
     * 从Map填充bean
     *
     * @param bean the bean | bean
     * @param map  the source map | 源映射
     */
    public static void populate(Object bean, Map<String, ?> map) {
        Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(bean.getClass());

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            PropertyDescriptor descriptor = descriptors.get(entry.getKey());
            if (descriptor != null && descriptor.isWritable()) {
                Object value = entry.getValue();
                // Convert if needed
                if (value != null && !descriptor.getPropertyType().isInstance(value)) {
                    value = PropertyConverter.convertSafe(value, descriptor.getPropertyType());
                }
                if (value != null || !descriptor.getPropertyType().isPrimitive()) {
                    descriptor.setValue(bean, value);
                }
            }
        }
    }

    /**
     * Creates a bean from a Map
     * 从Map创建bean
     *
     * @param map   the source map | 源映射
     * @param clazz the bean class | bean类
     * @param <T>   the bean type | bean类型
     * @return the bean | bean
     */
    public static <T> T fromMap(Map<String, ?> map, Class<T> clazz) {
        T bean = OpenConstructor.newInstance(clazz);
        populate(bean, map);
        return bean;
    }

    // ==================== BeanCopier | Bean复制器 ====================

    /**
     * Creates a BeanCopier
     * 创建BeanCopier
     *
     * @param sourceClass the source class | 源类
     * @param targetClass the target class | 目标类
     * @param <S>         the source type | 源类型
     * @param <T>         the target type | 目标类型
     * @return the BeanCopier | BeanCopier
     */
    public static <S, T> BeanCopier<S, T> createCopier(Class<S> sourceClass, Class<T> targetClass) {
        return BeanCopier.create(sourceClass, targetClass);
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Checks if bean has a property
     * 检查bean是否有属性
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return true if has property | 如果有属性返回true
     */
    public static boolean hasProperty(Class<?> clazz, String propertyName) {
        return getPropertyDescriptor(clazz, propertyName) != null;
    }

    /**
     * Clears the descriptor cache for a class
     * 清除类的描述符缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        DESCRIPTOR_CACHE.remove(clazz);
    }

    /**
     * Clears all descriptor cache
     * 清除所有描述符缓存
     */
    public static void clearAllCache() {
        DESCRIPTOR_CACHE.clear();
    }

    private static Map<String, PropertyDescriptor> discoverProperties(Class<?> clazz) {
        Map<String, PropertyDescriptor> result = new LinkedHashMap<>();
        Map<String, Field> fieldMap = new HashMap<>();
        Map<String, Method> getterMap = new HashMap<>();
        Map<String, Method> setterMap = new HashMap<>();

        // Collect fields
        for (Field field : OpenField.getAllFields(clazz)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fieldMap.put(field.getName(), field);
            }
        }

        // Collect getters and setters
        for (Method method : OpenMethod.getAllMethods(clazz)) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String name = method.getName();
            if (OpenMethod.isGetter(method)) {
                String propertyName = extractPropertyName(name);
                if (propertyName != null) {
                    getterMap.put(propertyName, method);
                }
            } else if (OpenMethod.isSetter(method)) {
                String propertyName = extractSetterPropertyName(name);
                if (propertyName != null) {
                    setterMap.put(propertyName, method);
                }
            }
        }

        // Merge into property descriptors
        Set<String> allNames = new LinkedHashSet<>();
        allNames.addAll(fieldMap.keySet());
        allNames.addAll(getterMap.keySet());
        allNames.addAll(setterMap.keySet());

        for (String name : allNames) {
            Field field = fieldMap.get(name);
            Method getter = getterMap.get(name);
            Method setter = setterMap.get(name);

            Class<?> propertyType;
            java.lang.reflect.Type genericType;

            if (getter != null) {
                propertyType = getter.getReturnType();
                genericType = getter.getGenericReturnType();
            } else if (setter != null) {
                propertyType = setter.getParameterTypes()[0];
                genericType = setter.getGenericParameterTypes()[0];
            } else if (field != null) {
                propertyType = field.getType();
                genericType = field.getGenericType();
            } else {
                continue;
            }

            result.put(name, new PropertyDescriptor(name, propertyType, genericType, getter, setter, field, clazz));
        }

        return Collections.unmodifiableMap(result);
    }

    private static String extractPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    private static String extractSetterPropertyName(String methodName) {
        if (methodName.startsWith("set") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        return null;
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
