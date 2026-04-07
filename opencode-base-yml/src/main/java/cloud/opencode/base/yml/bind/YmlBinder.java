package cloud.opencode.base.yml.bind;

import cloud.opencode.base.yml.YmlDocument;
import cloud.opencode.base.yml.exception.YmlBindException;
import cloud.opencode.base.yml.path.PathResolver;

import java.lang.reflect.*;
import java.util.*;

/**
 * YML Binder - Binds YAML data to Java objects
 * YML 绑定器 - 将 YAML 数据绑定到 Java 对象
 *
 * <p>This class provides data binding between YAML documents and Java objects.</p>
 * <p>此类提供 YAML 文档和 Java 对象之间的数据绑定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Bind YAML documents to Java beans and records - 将 YAML 文档绑定到 Java Bean 和 Record</li>
 *   <li>Path-based binding with prefix support - 基于路径的前缀绑定</li>
 *   <li>Annotation-driven field mapping (@YmlProperty, @YmlAlias, @YmlIgnore) - 注解驱动的字段映射</li>
 *   <li>Bidirectional: object to Map and back - 双向转换：对象到 Map 及反向</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Bind to class
 * ServerConfig config = YmlBinder.bind(document, ServerConfig.class);
 *
 * // Bind with prefix
 * ServerConfig config = YmlBinder.bind(document, "server", ServerConfig.class);
 *
 * // Convert object to map
 * Map<String, Object> map = YmlBinder.toMap(config);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null for null input) - 空值安全: 是（空输入返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlBinder {

    /**
     * Cache for resolved fields per class to avoid repeated reflection.
     * Uses WeakHashMap so Class keys don't prevent class unloading and
     * cache entries are automatically evicted when classes are GC'd.
     * 每个类的已解析字段缓存，避免重复反射。
     * 使用 WeakHashMap 确保 Class 键不阻止类卸载，类被 GC 时缓存条目自动回收。
     */
    private static final Map<Class<?>, List<Field>> FIELD_CACHE =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    private YmlBinder() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Binds YAML data to a Java object.
     * 将 YAML 数据绑定到 Java 对象。
     *
     * @param document the YAML document | YAML 文档
     * @param type     the target type | 目标类型
     * @param <T>      the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    public static <T> T bind(YmlDocument document, Class<T> type) {
        return bind(document.getRoot(), type);
    }

    /**
     * Binds YAML data at a path to a Java object.
     * 将指定路径的 YAML 数据绑定到 Java 对象。
     *
     * @param document the YAML document | YAML 文档
     * @param path     the path prefix | 路径前缀
     * @param type     the target type | 目标类型
     * @param <T>      the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    public static <T> T bind(YmlDocument document, String path, Class<T> type) {
        Object data = PathResolver.get(document.getRoot(), path);
        return bind(data, type);
    }

    /**
     * Binds Map data to a Java object.
     * 将 Map 数据绑定到 Java 对象。
     *
     * @param data the data map | 数据映射
     * @param type the target type | 目标类型
     * @param <T>  the type parameter | 类型参数
     * @return the bound object | 绑定的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T bind(Object data, Class<T> type) {
        if (data == null) {
            return null;
        }

        // Handle records
        if (type.isRecord()) {
            return bindRecord(data, type);
        }

        // Handle simple types
        if (isSimpleType(type)) {
            return convertSimpleType(data, type);
        }

        // Handle maps
        if (!(data instanceof Map<?, ?>)) {
            throw new YmlBindException("Type mismatch: expected Map but got " + data.getClass().getName());
        }

        Map<String, Object> map = (Map<String, Object>) data;

        try {
            T instance = type.getDeclaredConstructor().newInstance();
            bindFields(instance, map);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new YmlBindException("Failed to bind to " + type.getName(), e);
        }
    }

    /**
     * Converts an object to a Map.
     * 将对象转换为 Map。
     *
     * @param object the object | 对象
     * @return the map representation | Map 表示
     */
    public static Map<String, Object> toMap(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), toMapValue(entry.getValue()));
            }
            return result;
        }

        Map<String, Object> result = new LinkedHashMap<>();

        // Handle records
        if (object.getClass().isRecord()) {
            for (RecordComponent component : object.getClass().getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(object);
                    if (value != null) {
                        result.put(component.getName(), toMapValue(value));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new YmlBindException("Failed to read record component: " + component.getName(), e);
                }
            }
            return result;
        }

        // Handle regular objects
        for (Field field : getAllFields(object.getClass())) {
            if (field.isAnnotationPresent(YmlIgnore.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            try {
                Object value = field.get(object);
                if (value != null) {
                    String name = getPropertyName(field);
                    result.put(name, toMapValue(value));
                }
            } catch (IllegalAccessException e) {
                throw new YmlBindException("Failed to read field: " + field.getName(), e);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T bindRecord(Object data, Class<T> type) {
        if (!(data instanceof Map<?, ?>)) {
            throw new YmlBindException("Type mismatch: expected Map but got " + data.getClass().getName());
        }

        Map<String, Object> map = (Map<String, Object>) data;
        RecordComponent[] components = type.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] paramTypes = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            paramTypes[i] = component.getType();

            Object value = map.get(component.getName());
            args[i] = convertValue(value, component.getType(), component.getGenericType());
        }

        try {
            Constructor<T> constructor = type.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new YmlBindException("Failed to bind record: " + type.getName(), e);
        }
    }

    private static void bindFields(Object instance, Map<String, Object> map) {
        for (Field field : getAllFields(instance.getClass())) {
            if (field.isAnnotationPresent(YmlIgnore.class)) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            Object value = findValue(field, map);
            if (value != null) {
                try {
                    Object converted = convertValue(value, field.getType(), field.getGenericType());
                    field.set(instance, converted);
                } catch (IllegalAccessException e) {
                    throw new YmlBindException("Failed to set field: " + field.getName(), e);
                }
            } else {
                // Check required
                YmlProperty prop = field.getAnnotation(YmlProperty.class);
                if (prop != null && prop.required()) {
                    throw new YmlBindException("Required property '" + prop.value() + "' is missing");
                }
            }
        }
    }

    private static Object findValue(Field field, Map<String, Object> map) {
        // Check YmlProperty annotation
        YmlProperty prop = field.getAnnotation(YmlProperty.class);
        if (prop != null) {
            Object value = PathResolver.get(map, prop.value(), null);
            if (value == null && !prop.defaultValue().isEmpty()) {
                return prop.defaultValue();
            }
            return value;
        }

        // Check YmlAlias annotation
        YmlAlias alias = field.getAnnotation(YmlAlias.class);
        if (alias != null) {
            for (String path : alias.value()) {
                Object value = PathResolver.get(map, path, null);
                if (value != null) {
                    return value;
                }
            }
        }

        // Use field name
        return map.get(field.getName());
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Object value, Class<?> type, Type genericType) {
        if (value == null) {
            return getDefaultValue(type);
        }

        if (type.isInstance(value)) {
            return value;
        }

        // Simple types
        if (isSimpleType(type)) {
            return convertSimpleType(value, type);
        }

        // Lists
        if (List.class.isAssignableFrom(type) && value instanceof List<?> list) {
            Class<?> elementType = getListElementType(genericType);
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(convertValue(item, elementType, elementType));
            }
            return result;
        }

        // Maps
        if (Map.class.isAssignableFrom(type) && value instanceof Map<?, ?> map) {
            return map;
        }

        // Nested objects
        if (value instanceof Map<?, ?>) {
            return bind(value, type);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private static <T> T convertSimpleType(Object value, Class<T> type) {
        if (type == String.class) {
            return (T) (value instanceof String s ? s : value.toString());
        }
        String str = value.toString();
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(str);
        }
        if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(str);
        }
        if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(str);
        }
        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(str);
        }
        if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(str);
        }
        if (type == short.class || type == Short.class) {
            return (T) Short.valueOf(str);
        }
        if (type == byte.class || type == Byte.class) {
            return (T) Byte.valueOf(str);
        }
        if (type == char.class || type == Character.class) {
            return (T) Character.valueOf(str.isEmpty() ? '\0' : str.charAt(0));
        }

        return (T) value;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Double.class ||
               type == Float.class ||
               type == Boolean.class ||
               type == Short.class ||
               type == Byte.class ||
               type == Character.class;
    }

    private static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }

    private static Class<?> getListElementType(Type genericType) {
        if (genericType instanceof ParameterizedType pt) {
            Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> c) {
                return c;
            }
        }
        return Object.class;
    }

    private static String getPropertyName(Field field) {
        YmlProperty prop = field.getAnnotation(YmlProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return prop.value();
        }
        return field.getName();
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> cached = FIELD_CACHE.get(type);
        if (cached != null) {
            return cached;
        }
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                f.setAccessible(true);
                fields.add(f);
            }
            current = current.getSuperclass();
        }
        List<Field> result = Collections.unmodifiableList(fields);
        FIELD_CACHE.put(type, result);
        return result;
    }

    private static Object toMapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (isSimpleType(value.getClass())) {
            return value;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(toMapValue(item));
            }
            return result;
        }
        if (value instanceof Map<?, ?>) {
            return toMap(value);
        }
        return toMap(value);
    }
}
