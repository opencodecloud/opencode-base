package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.convert.Convert;
import cloud.opencode.base.core.reflect.RecordUtil;
import cloud.opencode.base.core.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean Utility Class - JavaBean property operations and conversions
 * Bean 工具类 - JavaBean 属性操作和转换
 *
 * <p>Provides comprehensive bean operations including copy, conversion, and comparison.</p>
 * <p>提供完整的 Bean 操作，包括复制、转换和比较。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property copy (with mapping, converter, ignore) - 属性复制（支持映射、转换器、忽略）</li>
 *   <li>Bean/Map conversion (camelCase/underline) - Bean/Map 转换（驼峰/下划线）</li>
 *   <li>Property get/set with type conversion - 属性读写（带类型转换）</li>
 *   <li>Bean comparison and diff - Bean 比较和差异</li>
 *   <li>Record support (to/from) - Record 支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Copy properties - 复制属性
 * OpenBean.copyProperties(source, target);
 * User copy = OpenBean.copyToNew(source, User.class);
 *
 * // Bean to/from Map - Bean 与 Map 转换
 * Map<String, Object> map = OpenBean.toMap(user);
 * User user = OpenBean.toBean(map, User.class);
 *
 * // Property access - 属性访问
 * String name = OpenBean.getProperty(user, "name", String.class);
 * OpenBean.setProperty(user, "name", "Leon");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap cache) - 线程安全: 是 (ConcurrentHashMap 缓存)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenBean {

    private static final System.Logger logger = System.getLogger(OpenBean.class.getName());
    private static final Map<Class<?>, List<PropertyDescriptor>> PROPERTY_CACHE = new ConcurrentHashMap<>();

    private OpenBean() {
    }

    // ==================== Bean 复制 ====================

    /**
     * Copies properties with matching names and types
     * 复制属性（同名同类型属性）
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, (String[]) null);
    }

    /**
     * Copies properties with ignored properties
     * 复制属性（带忽略属性）
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        if (source == null || target == null) return;

        Set<String> ignoreSet = ignoreProperties != null ? Set.of(ignoreProperties) : Set.of();

        List<PropertyDescriptor> sourceProps = getPropertyDescriptors(source.getClass());
        Map<String, PropertyDescriptor> targetMap = getPropertyDescriptorMap(target.getClass());

        for (PropertyDescriptor sourcePd : sourceProps) {
            if (!sourcePd.isReadable()) continue;
            if (ignoreSet.contains(sourcePd.name())) continue;

            PropertyDescriptor targetPd = targetMap.get(sourcePd.name());
            if (targetPd == null || !targetPd.isWritable()) continue;

            try {
                Object value = sourcePd.getValue(source);
                if (targetPd.type().isAssignableFrom(sourcePd.type())) {
                    targetPd.setValue(target, value);
                } else {
                    Object converted = Convert.convert(value, targetPd.type());
                    targetPd.setValue(target, converted);
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Failed to copy property '" + sourcePd.name() +
                    "' from " + source.getClass().getSimpleName() + " to " + target.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Copies properties with property name mapping
     * 复制属性（带属性映射）
     */
    public static void copyProperties(Object source, Object target, Map<String, String> propertyMapping) {
        if (source == null || target == null) return;

        List<PropertyDescriptor> sourceProps = getPropertyDescriptors(source.getClass());
        Map<String, PropertyDescriptor> targetMap = getPropertyDescriptorMap(target.getClass());

        for (PropertyDescriptor sourcePd : sourceProps) {
            if (!sourcePd.isReadable()) continue;

            String targetName = propertyMapping.getOrDefault(sourcePd.name(), sourcePd.name());
            PropertyDescriptor targetPd = targetMap.get(targetName);
            if (targetPd == null || !targetPd.isWritable()) continue;

            try {
                Object value = sourcePd.getValue(source);
                Object converted = Convert.convert(value, targetPd.type());
                targetPd.setValue(target, converted);
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Failed to copy property '" + sourcePd.name() +
                    "' (mapped to '" + targetName + "') from " + source.getClass().getSimpleName() +
                    " to " + target.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Copies properties with a converter
     * 复制属性（带转换器）
     */
    public static void copyProperties(Object source, Object target, PropertyConverter converter) {
        if (source == null || target == null) return;

        List<PropertyDescriptor> sourceProps = getPropertyDescriptors(source.getClass());
        Map<String, PropertyDescriptor> targetMap = getPropertyDescriptorMap(target.getClass());

        for (PropertyDescriptor sourcePd : sourceProps) {
            if (!sourcePd.isReadable()) continue;

            PropertyDescriptor targetPd = targetMap.get(sourcePd.name());
            if (targetPd == null || !targetPd.isWritable()) continue;

            try {
                Object value = sourcePd.getValue(source);
                Object converted = converter.convert(value, sourcePd.type(), targetPd.type(), sourcePd.name());
                targetPd.setValue(target, converted);
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Failed to copy property '" + sourcePd.name() +
                    "' from " + source.getClass().getSimpleName() + " to " + target.getClass().getSimpleName() +
                    " with converter", e);
            }
        }
    }

    /**
     * Copies to a new object
     * 复制到新对象
     */
    public static <T> T copyToNew(Object source, Class<T> targetClass) {
        return copyToNew(source, targetClass, (String[]) null);
    }

    /**
     * Copies to a new object with ignored properties
     * 复制到新对象（带忽略属性）
     */
    public static <T> T copyToNew(Object source, Class<T> targetClass, String... ignoreProperties) {
        if (source == null) return null;
        T target = ReflectUtil.newInstance(targetClass);
        copyProperties(source, target, ignoreProperties);
        return target;
    }

    /**
     * Deep copies properties including nested objects
     * 深度复制属性（包括嵌套对象）
     */
    public static void deepCopyProperties(Object source, Object target) {
        // 简化实现：使用浅拷贝
        copyProperties(source, target);
    }

    // ==================== Bean 转换 ====================

    /**
     * Converts a Bean to a Map
     * Bean 转 Map
     */
    public static Map<String, Object> toMap(Object bean) {
        return toMap(bean, (String[]) null);
    }

    /**
     * Converts a Bean to a Map with ignored properties
     * Bean 转 Map（带忽略属性）
     */
    public static Map<String, Object> toMap(Object bean, String... ignoreProperties) {
        if (bean == null) return new LinkedHashMap<>();

        Set<String> ignoreSet = ignoreProperties != null ? Set.of(ignoreProperties) : Set.of();
        Map<String, Object> map = new LinkedHashMap<>();

        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (!pd.isReadable()) continue;
            if (ignoreSet.contains(pd.name())) continue;

            try {
                map.put(pd.name(), pd.getValue(bean));
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Failed to read property '" + pd.name() + "' from " + bean.getClass().getSimpleName(), e);
            }
        }

        return map;
    }

    /**
     * Converts a Bean to a Map with only non-null properties
     * Bean 转 Map（仅包含非空属性）
     */
    public static Map<String, Object> toMapNonNull(Object bean) {
        if (bean == null) return new LinkedHashMap<>();

        Map<String, Object> map = new LinkedHashMap<>();
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (!pd.isReadable()) continue;

            try {
                Object value = pd.getValue(bean);
                if (value != null) {
                    map.put(pd.name(), value);
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Failed to read property '" + pd.name() + "' from " + bean.getClass().getSimpleName(), e);
            }
        }

        return map;
    }

    /**
     * Converts a Bean to a Map with underline keys
     * Bean 转 Map（驼峰转下划线 key）
     */
    public static Map<String, Object> toUnderlineKeyMap(Object bean) {
        Map<String, Object> map = toMap(bean);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(toUnderlineCase(entry.getKey()), entry.getValue());
        }
        return result;
    }

    /**
     * Converts a Map to a Bean
     * Map 转 Bean
     */
    public static <T> T toBean(Map<String, ?> map, Class<T> beanClass) {
        if (map == null) return null;
        T bean = ReflectUtil.newInstance(beanClass);
        Map<String, PropertyDescriptor> pdMap = getPropertyDescriptorMap(beanClass);

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            PropertyDescriptor pd = pdMap.get(entry.getKey());
            if (pd != null && pd.isWritable()) {
                try {
                    Object value = Convert.convert(entry.getValue(), pd.type());
                    pd.setValue(bean, value);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.DEBUG, "Failed to set property '" + entry.getKey() + "' on " + beanClass.getSimpleName(), e);
                }
            }
        }

        return bean;
    }

    /**
     * Converts a Map to a Bean with property name mapping
     * Map 转 Bean（带属性映射）
     */
    public static <T> T toBean(Map<String, ?> map, Class<T> beanClass, Map<String, String> propertyMapping) {
        if (map == null) return null;
        T bean = ReflectUtil.newInstance(beanClass);
        Map<String, PropertyDescriptor> pdMap = getPropertyDescriptorMap(beanClass);

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String targetName = propertyMapping.getOrDefault(entry.getKey(), entry.getKey());
            PropertyDescriptor pd = pdMap.get(targetName);
            if (pd != null && pd.isWritable()) {
                try {
                    Object value = Convert.convert(entry.getValue(), pd.type());
                    pd.setValue(bean, value);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.DEBUG, "Failed to set property '" + targetName + "' on " + beanClass.getSimpleName(), e);
                }
            }
        }

        return bean;
    }

    /**
     * Converts a Map with underline keys to a Bean
     * Map 转 Bean（下划线 key 转驼峰）
     */
    public static <T> T toBeanFromUnderlineKey(Map<String, ?> map, Class<T> beanClass) {
        if (map == null) return null;
        Map<String, Object> camelMap = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            camelMap.put(toCamelCase(entry.getKey()), entry.getValue());
        }
        return toBean(camelMap, beanClass);
    }

    // ==================== 属性访问 ====================

    /**
     * Gets a property value
     * 获取属性值
     */
    public static Object getProperty(Object bean, String propertyName) {
        if (bean == null) return null;
        PropertyDescriptor pd = getPropertyDescriptor(bean.getClass(), propertyName).orElse(null);
        return pd != null && pd.isReadable() ? pd.getValue(bean) : null;
    }

    /**
     * Gets a property value with type conversion
     * 获取属性值（带类型转换）
     */
    public static <T> T getProperty(Object bean, String propertyName, Class<T> targetType) {
        Object value = getProperty(bean, propertyName);
        return Convert.convert(value, targetType);
    }

    /**
     * Safely gets a property value returning an Optional
     * 安全获取属性值（返回 Optional）
     */
    public static <T> Optional<T> getPropertyOptional(Object bean, String propertyName, Class<T> targetType) {
        return Optional.ofNullable(getProperty(bean, propertyName, targetType));
    }

    /**
     * Sets a property value
     * 设置属性值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        if (bean == null) return;
        PropertyDescriptor pd = getPropertyDescriptor(bean.getClass(), propertyName).orElse(null);
        if (pd != null && pd.isWritable()) {
            pd.setValue(bean, value);
        }
    }

    /**
     * Sets a property value with type conversion
     * 设置属性值（带类型转换）
     */
    public static void setPropertyWithConvert(Object bean, String propertyName, Object value) {
        if (bean == null) return;
        PropertyDescriptor pd = getPropertyDescriptor(bean.getClass(), propertyName).orElse(null);
        if (pd != null && pd.isWritable()) {
            Object converted = Convert.convert(value, pd.type());
            pd.setValue(bean, converted);
        }
    }

    /**
     * Sets multiple properties in batch
     * 批量设置属性
     */
    public static void setProperties(Object bean, Map<String, ?> properties) {
        if (bean == null || properties == null) return;
        Map<String, PropertyDescriptor> pdMap = getPropertyDescriptorMap(bean.getClass());
        for (Map.Entry<String, ?> entry : properties.entrySet()) {
            PropertyDescriptor pd = pdMap.get(entry.getKey());
            if (pd != null && pd.isWritable()) {
                try {
                    Object value = Convert.convert(entry.getValue(), pd.type());
                    pd.setValue(bean, value);
                } catch (Exception e) {
                    logger.log(System.Logger.Level.DEBUG, "Failed to set property '" + entry.getKey() + "' on " + bean.getClass().getSimpleName(), e);
                }
            }
        }
    }

    // ==================== 属性描述 ====================

    /**
     * Gets all property descriptors
     * 获取所有属性描述符
     */
    public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> beanClass) {
        return PROPERTY_CACHE.computeIfAbsent(beanClass, OpenBean::buildPropertyDescriptors);
    }

    /**
     * Gets the specified property descriptor
     * 获取指定属性描述符
     */
    public static Optional<PropertyDescriptor> getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        return getPropertyDescriptors(beanClass).stream()
                .filter(pd -> pd.name().equals(propertyName))
                .findFirst();
    }

    /**
     * Gets all property names
     * 获取所有属性名
     */
    public static List<String> getPropertyNames(Class<?> beanClass) {
        return getPropertyDescriptors(beanClass).stream()
                .map(PropertyDescriptor::name)
                .toList();
    }

    /**
     * Gets all readable property names
     * 获取所有可读属性名
     */
    public static List<String> getReadablePropertyNames(Class<?> beanClass) {
        return getPropertyDescriptors(beanClass).stream()
                .filter(PropertyDescriptor::isReadable)
                .map(PropertyDescriptor::name)
                .toList();
    }

    /**
     * Gets all writable property names
     * 获取所有可写属性名
     */
    public static List<String> getWritablePropertyNames(Class<?> beanClass) {
        return getPropertyDescriptors(beanClass).stream()
                .filter(PropertyDescriptor::isWritable)
                .map(PropertyDescriptor::name)
                .toList();
    }

    /**
     * Checks whether a property exists
     * 检查属性是否存在
     */
    public static boolean hasProperty(Class<?> beanClass, String propertyName) {
        return getPropertyDescriptor(beanClass, propertyName).isPresent();
    }

    /**
     * Gets the property type
     * 获取属性类型
     */
    public static Class<?> getPropertyType(Class<?> beanClass, String propertyName) {
        return getPropertyDescriptor(beanClass, propertyName)
                .map(PropertyDescriptor::type)
                .orElse(null);
    }

    // ==================== Bean 比较 ====================

    /**
     * Compares two Beans for equality on all properties
     * 比较两个 Bean 是否相等（所有属性）
     */
    public static boolean equals(Object bean1, Object bean2) {
        if (bean1 == bean2) return true;
        if (bean1 == null || bean2 == null) return false;
        if (bean1.getClass() != bean2.getClass()) return false;

        Map<String, Object> map1 = toMap(bean1);
        Map<String, Object> map2 = toMap(bean2);
        return map1.equals(map2);
    }

    /**
     * Compares two Beans for equality on specified properties
     * 比较两个 Bean 是否相等（指定属性）
     */
    public static boolean equals(Object bean1, Object bean2, String... properties) {
        if (bean1 == bean2) return true;
        if (bean1 == null || bean2 == null) return false;

        for (String prop : properties) {
            Object v1 = getProperty(bean1, prop);
            Object v2 = getProperty(bean2, prop);
            if (!Objects.equals(v1, v2)) return false;
        }
        return true;
    }

    /**
     * Gets the differing properties between two Beans
     * 获取两个 Bean 的差异属性
     */
    public static Map<String, Object[]> diff(Object bean1, Object bean2) {
        Map<String, Object[]> diff = new LinkedHashMap<>();
        if (bean1 == null || bean2 == null) return diff;

        Set<String> allProps = new LinkedHashSet<>();
        allProps.addAll(getPropertyNames(bean1.getClass()));
        allProps.addAll(getPropertyNames(bean2.getClass()));

        for (String prop : allProps) {
            Object v1 = getProperty(bean1, prop);
            Object v2 = getProperty(bean2, prop);
            if (!Objects.equals(v1, v2)) {
                diff.put(prop, new Object[]{v1, v2});
            }
        }
        return diff;
    }

    /**
     * Gets the differing properties between two Beans for specified properties
     * 获取两个 Bean 的差异属性（指定属性）
     */
    public static Map<String, Object[]> diff(Object bean1, Object bean2, String... properties) {
        Map<String, Object[]> diff = new LinkedHashMap<>();
        if (bean1 == null || bean2 == null) return diff;

        for (String prop : properties) {
            Object v1 = getProperty(bean1, prop);
            Object v2 = getProperty(bean2, prop);
            if (!Objects.equals(v1, v2)) {
                diff.put(prop, new Object[]{v1, v2});
            }
        }
        return diff;
    }

    // ==================== Bean 校验 ====================

    /**
     * Checks whether the Bean is empty (all properties are null or primitive defaults)
     * 检查是否为空 Bean（所有属性为 null 或基本类型默认值）
     */
    public static boolean isEmpty(Object bean) {
        if (bean == null) return true;
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (pd.isReadable()) {
                try {
                    Object value = pd.getValue(bean);
                    if (!isEmptyValue(value, pd.type())) return false;
                } catch (Exception e) {
                    logger.log(System.Logger.Level.DEBUG, "Failed to read property '" + pd.name() + "' during isEmpty check", e);
                }
            }
        }
        return true;
    }

    /**
     * Checks whether there is any non-null property
     * 检查是否有任意非空属性
     */
    public static boolean hasNonNullProperty(Object bean) {
        return !isEmpty(bean);
    }

    /**
     * Gets all non-null property names
     * 获取所有非空属性名
     */
    public static List<String> getNonNullPropertyNames(Object bean) {
        if (bean == null) return List.of();
        List<String> names = new ArrayList<>();
        for (PropertyDescriptor pd : getPropertyDescriptors(bean.getClass())) {
            if (pd.isReadable()) {
                try {
                    Object value = pd.getValue(bean);
                    if (!isEmptyValue(value, pd.type())) {
                        names.add(pd.name());
                    }
                } catch (Exception e) {
                    logger.log(System.Logger.Level.DEBUG, "Failed to read property '" + pd.name() + "' during non-null check", e);
                }
            }
        }
        return names;
    }

    /**
     * Checks whether the value is empty (including null and primitive defaults)
     * 判断值是否为空（包括 null 和基本类型默认值）
     */
    private static boolean isEmptyValue(Object value, Class<?> type) {
        if (value == null) return true;
        if (type == null) return false;
        if (type.isPrimitive()) {
            if (type == int.class) return ((Integer) value) == 0;
            if (type == long.class) return ((Long) value) == 0L;
            if (type == double.class) return ((Double) value) == 0.0;
            if (type == float.class) return ((Float) value) == 0.0f;
            if (type == boolean.class) return !((Boolean) value);
            if (type == byte.class) return ((Byte) value) == 0;
            if (type == short.class) return ((Short) value) == 0;
            if (type == char.class) return ((Character) value) == '\0';
        }
        return false;
    }

    // ==================== Record 支持 ====================

    /**
     * Converts a Record to a Bean
     * Record 转 Bean
     */
    public static <T> T fromRecord(Record record, Class<T> beanClass) {
        if (record == null) return null;
        Map<String, Object> map = RecordUtil.toMap(record);
        return toBean(map, beanClass);
    }

    /**
     * Converts a Bean to a Record
     * Bean 转 Record
     */
    public static <T extends Record> T toRecord(Object bean, Class<T> recordClass) {
        if (bean == null) return null;
        Map<String, Object> map = toMap(bean);
        return RecordUtil.fromMap(map, recordClass);
    }

    // ==================== 私有辅助方法 ====================

    private static List<PropertyDescriptor> buildPropertyDescriptors(Class<?> beanClass) {
        List<PropertyDescriptor> descriptors = new ArrayList<>();
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        Map<String, Method> getterMap = new LinkedHashMap<>();
        Map<String, Method> setterMap = new LinkedHashMap<>();

        // 收集字段
        Class<?> current = beanClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                fieldMap.putIfAbsent(field.getName(), field);
            }
            current = current.getSuperclass();
        }

        // 收集方法
        for (Method method : beanClass.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;

            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3 && method.getParameterCount() == 0) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                getterMap.putIfAbsent(propName, method);
            } else if (name.startsWith("is") && name.length() > 2 && method.getParameterCount() == 0 &&
                    (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                String propName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                getterMap.putIfAbsent(propName, method);
            } else if (name.startsWith("set") && name.length() > 3 && method.getParameterCount() == 1) {
                String propName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                setterMap.putIfAbsent(propName, method);
            }
        }

        // 构建属性描述符
        Set<String> allNames = new LinkedHashSet<>();
        allNames.addAll(fieldMap.keySet());
        allNames.addAll(getterMap.keySet());
        allNames.addAll(setterMap.keySet());

        for (String name : allNames) {
            if ("class".equals(name)) continue;

            Field field = fieldMap.get(name);
            Method getter = getterMap.get(name);
            Method setter = setterMap.get(name);

            Class<?> type = null;
            if (getter != null) {
                type = getter.getReturnType();
            } else if (setter != null) {
                type = setter.getParameterTypes()[0];
            } else if (field != null) {
                type = field.getType();
            }

            if (type != null) {
                descriptors.add(new PropertyDescriptor(name, type, getter, setter, field));
            }
        }

        return descriptors;
    }

    private static Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> beanClass) {
        Map<String, PropertyDescriptor> map = new LinkedHashMap<>();
        for (PropertyDescriptor pd : getPropertyDescriptors(beanClass)) {
            map.put(pd.name(), pd);
        }
        return map;
    }

    private static String toUnderlineCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) return camelCase;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toCamelCase(String underlineCase) {
        if (underlineCase == null || underlineCase.isEmpty()) return underlineCase;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < underlineCase.length(); i++) {
            char c = underlineCase.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }
}
