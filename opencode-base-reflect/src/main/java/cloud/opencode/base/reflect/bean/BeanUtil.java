package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Utility Class
 * Bean工具类
 *
 * <p>Provides low-level bean operation utilities with caching.</p>
 * <p>提供带缓存的底层bean操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Getter/setter discovery with caching - 带缓存的getter/setter发现</li>
 *   <li>Property name extraction - 属性名提取</li>
 *   <li>Bean introspection utilities - Bean内省工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Method> getters = BeanUtil.getGetters(User.class);
 * List<Method> setters = BeanUtil.getSetters(User.class);
 * String propName = BeanUtil.getPropertyName(getter);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for cached lookups; O(m) for first access where m is the number of methods - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(m)，m为方法数量</li>
 *   <li>Space complexity: O(m) for the cached getter/setter maps per class - 空间复杂度: O(m)，每类缓存 getter/setter 映射</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class BeanUtil {

    private static final Map<Class<?>, List<Method>> GETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Method>> SETTER_CACHE = new ConcurrentHashMap<>();
    private static final Map<PropertyKey, Method> GETTER_BY_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<PropertyKey, Method> SETTER_BY_NAME_CACHE = new ConcurrentHashMap<>();

    private BeanUtil() {
    }

    // ==================== Getter/Setter Discovery | Getter/Setter发现 ====================

    /**
     * Gets all getter methods (cached)
     * 获取所有getter方法（缓存）
     *
     * @param clazz the class | 类
     * @return list of getters | getter列表
     */
    public static List<Method> getGetters(Class<?> clazz) {
        return GETTER_CACHE.computeIfAbsent(clazz, BeanUtil::discoverGetters);
    }

    /**
     * Gets all setter methods (cached)
     * 获取所有setter方法（缓存）
     *
     * @param clazz the class | 类
     * @return list of setters | setter列表
     */
    public static List<Method> getSetters(Class<?> clazz) {
        return SETTER_CACHE.computeIfAbsent(clazz, BeanUtil::discoverSetters);
    }

    /**
     * Gets getter for a property (cached)
     * 获取属性的getter（缓存）
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return the getter or null | getter或null
     */
    public static Method getGetter(Class<?> clazz, String propertyName) {
        PropertyKey key = new PropertyKey(clazz, propertyName);
        return GETTER_BY_NAME_CACHE.computeIfAbsent(key, k -> findGetter(k.clazz(), k.propertyName()));
    }

    /**
     * Gets setter for a property (cached)
     * 获取属性的setter（缓存）
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return the setter or null | setter或null
     */
    public static Method getSetter(Class<?> clazz, String propertyName) {
        PropertyKey key = new PropertyKey(clazz, propertyName);
        return SETTER_BY_NAME_CACHE.computeIfAbsent(key, k -> findSetter(k.clazz(), k.propertyName()));
    }

    private static List<Method> discoverGetters(Class<?> clazz) {
        List<Method> getters = new ArrayList<>();
        for (Method method : getAllMethods(clazz)) {
            if (isGetter(method)) {
                getters.add(method);
            }
        }
        return Collections.unmodifiableList(getters);
    }

    private static List<Method> discoverSetters(Class<?> clazz) {
        List<Method> setters = new ArrayList<>();
        for (Method method : getAllMethods(clazz)) {
            if (isSetter(method)) {
                setters.add(method);
            }
        }
        return Collections.unmodifiableList(setters);
    }

    private static Method findGetter(Class<?> clazz, String propertyName) {
        String capitalized = capitalize(propertyName);
        String getterName = "get" + capitalized;
        String booleanGetterName = "is" + capitalized;

        for (Method method : getAllMethods(clazz)) {
            if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                String name = method.getName();
                if (name.equals(getterName)) {
                    return method;
                }
                if (name.equals(booleanGetterName) &&
                    (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static Method findSetter(Class<?> clazz, String propertyName) {
        String setterName = "set" + capitalize(propertyName);

        for (Method method : getAllMethods(clazz)) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }

    // ==================== Property Operations | 属性操作 ====================

    /**
     * Gets property value using getter
     * 使用getter获取属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @return the value | 值
     */
    public static Object getPropertyValue(Object bean, String propertyName) {
        Method getter = getGetter(bean.getClass(), propertyName);
        if (getter == null) {
            throw new OpenReflectException("No getter found for property: " + propertyName);
        }
        try {
            getter.setAccessible(true);
            return getter.invoke(bean);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to get property: " + propertyName, e);
        }
    }

    /**
     * Gets property value safely
     * 安全获取属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @return Optional of value | 值的Optional
     */
    public static Optional<Object> getPropertyValueSafe(Object bean, String propertyName) {
        try {
            return Optional.ofNullable(getPropertyValue(bean, propertyName));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Sets property value using setter
     * 使用setter设置属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @param value        the value | 值
     */
    public static void setPropertyValue(Object bean, String propertyName, Object value) {
        Method setter = getSetter(bean.getClass(), propertyName);
        if (setter == null) {
            throw new OpenReflectException("No setter found for property: " + propertyName);
        }
        try {
            setter.setAccessible(true);
            setter.invoke(bean, value);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to set property: " + propertyName, e);
        }
    }

    /**
     * Sets property value safely
     * 安全设置属性值
     *
     * @param bean         the bean | bean
     * @param propertyName the property name | 属性名
     * @param value        the value | 值
     * @return true if successful | 如果成功返回true
     */
    public static boolean setPropertyValueSafe(Object bean, String propertyName, Object value) {
        try {
            setPropertyValue(bean, propertyName, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Method Checks | 方法检查 ====================

    /**
     * Checks if method is a getter
     * 检查方法是否为getter
     *
     * @param method the method | 方法
     * @return true if getter | 如果是getter返回true
     */
    public static boolean isGetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }
        if (method.getReturnType() == void.class) {
            return false;
        }

        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.isUpperCase(name.charAt(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.isUpperCase(name.charAt(2)) &&
                   (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class);
        }
        return false;
    }

    /**
     * Checks if method is a setter
     * 检查方法是否为setter
     *
     * @param method the method | 方法
     * @return true if setter | 如果是setter返回true
     */
    public static boolean isSetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }

        String name = method.getName();
        return name.startsWith("set") && name.length() > 3 && Character.isUpperCase(name.charAt(3));
    }

    /**
     * Extracts property name from getter/setter
     * 从getter/setter提取属性名
     *
     * @param method the method | 方法
     * @return the property name or null | 属性名或null
     */
    public static String extractPropertyName(Method method) {
        String name = method.getName();

        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return decapitalize(name.substring(2));
        }
        if (name.startsWith("set") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        return null;
    }

    // ==================== Property Names | 属性名 ====================

    /**
     * Gets all readable property names
     * 获取所有可读属性名
     *
     * @param clazz the class | 类
     * @return set of property names | 属性名集合
     */
    public static Set<String> getReadablePropertyNames(Class<?> clazz) {
        Set<String> names = new LinkedHashSet<>();
        for (Method getter : getGetters(clazz)) {
            String name = extractPropertyName(getter);
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Gets all writable property names
     * 获取所有可写属性名
     *
     * @param clazz the class | 类
     * @return set of property names | 属性名集合
     */
    public static Set<String> getWritablePropertyNames(Class<?> clazz) {
        Set<String> names = new LinkedHashSet<>();
        for (Method setter : getSetters(clazz)) {
            String name = extractPropertyName(setter);
            if (name != null) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Gets all property names (readable or writable)
     * 获取所有属性名（可读或可写）
     *
     * @param clazz the class | 类
     * @return set of property names | 属性名集合
     */
    public static Set<String> getAllPropertyNames(Class<?> clazz) {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(getReadablePropertyNames(clazz));
        names.addAll(getWritablePropertyNames(clazz));
        return names;
    }

    // ==================== Property Type | 属性类型 ====================

    /**
     * Gets property type from getter
     * 从getter获取属性类型
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return the type or null | 类型或null
     */
    public static Class<?> getPropertyType(Class<?> clazz, String propertyName) {
        Method getter = getGetter(clazz, propertyName);
        if (getter != null) {
            return getter.getReturnType();
        }
        Method setter = getSetter(clazz, propertyName);
        if (setter != null) {
            return setter.getParameterTypes()[0];
        }
        return null;
    }

    /**
     * Checks if property is readable
     * 检查属性是否可读
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return true if readable | 如果可读返回true
     */
    public static boolean isReadable(Class<?> clazz, String propertyName) {
        return getGetter(clazz, propertyName) != null;
    }

    /**
     * Checks if property is writable
     * 检查属性是否可写
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @return true if writable | 如果可写返回true
     */
    public static boolean isWritable(Class<?> clazz, String propertyName) {
        return getSetter(clazz, propertyName) != null;
    }

    // ==================== Utility Methods | 工具方法 ====================

    private static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            methods.addAll(Arrays.asList(current.getDeclaredMethods()));
            current = current.getSuperclass();
        }
        return methods;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
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

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears all caches
     * 清除所有缓存
     */
    public static void clearCache() {
        GETTER_CACHE.clear();
        SETTER_CACHE.clear();
        GETTER_BY_NAME_CACHE.clear();
        SETTER_BY_NAME_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        GETTER_CACHE.remove(clazz);
        SETTER_CACHE.remove(clazz);
        GETTER_BY_NAME_CACHE.keySet().removeIf(key -> key.clazz() == clazz);
        SETTER_BY_NAME_CACHE.keySet().removeIf(key -> key.clazz() == clazz);
    }

    // ==================== Internal | 内部 ====================

    private record PropertyKey(Class<?> clazz, String propertyName) {
    }
}
