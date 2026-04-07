package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.util.*;

/**
 * Bean Path Navigator
 * Bean路径导航器
 *
 * <p>Navigates nested bean properties using path expressions.
 * Supports dot notation (e.g., "user.address.city").</p>
 * <p>使用路径表达式导航嵌套bean属性。
 * 支持点号表示法（如 "user.address.city"）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dot notation path navigation - 点号表示法路径导航</li>
 *   <li>Indexed property access (list, array, map) - 索引属性访问（列表、数组、映射）</li>
 *   <li>Nested property read/write - 嵌套属性读写</li>
 *   <li>Path existence checking - 路径存在检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get nested property
 * String city = BeanPath.get(user, "address.city", String.class);
 *
 * // Set nested property
 * BeanPath.set(user, "address.city", "Beijing");
 *
 * // Indexed access
 * Object item = BeanPath.get(order, "items[0].name");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Partially (returns null for null bean or intermediate null) - 空值安全: 部分（null bean或中间null返回null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class BeanPath {

    private static final String PATH_SEPARATOR = ".";
    private static final String INDEX_START = "[";
    private static final String INDEX_END = "]";

    private BeanPath() {
    }

    /**
     * Gets a nested property value
     * 获取嵌套属性值
     *
     * @param bean the root bean | 根bean
     * @param path the property path (e.g., "user.address.city") | 属性路径
     * @return the value | 值
     */
    public static Object get(Object bean, String path) {
        if (bean == null) {
            return null;
        }
        Objects.requireNonNull(path, "path must not be null");

        String[] parts = parsePath(path);
        Object current = bean;

        for (String part : parts) {
            if (current == null) {
                return null;
            }
            current = getProperty(current, part);
        }

        return current;
    }

    /**
     * Gets a nested property value with type
     * 获取嵌套属性值（带类型）
     *
     * @param bean the root bean | 根bean
     * @param path the property path | 属性路径
     * @param type the expected type | 期望类型
     * @param <T>  the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object bean, String path, Class<T> type) {
        Object value = get(bean, path);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new OpenReflectException("Value at path '" + path + "' is not of type " + type.getName());
        }
        return (T) value;
    }

    /**
     * Gets a nested property value or default
     * 获取嵌套属性值或默认值
     *
     * @param bean         the root bean | 根bean
     * @param path         the property path | 属性路径
     * @param defaultValue the default value | 默认值
     * @param <T>          the value type | 值类型
     * @return the value or default | 值或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(Object bean, String path, T defaultValue) {
        Object value = get(bean, path);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Sets a nested property value
     * 设置嵌套属性值
     *
     * @param bean  the root bean | 根bean
     * @param path  the property path | 属性路径
     * @param value the value to set | 要设置的值
     */
    public static void set(Object bean, String path, Object value) {
        Objects.requireNonNull(bean, "bean must not be null");
        Objects.requireNonNull(path, "path must not be null");

        String[] parts = parsePath(path);
        Object current = bean;

        // Navigate to parent
        for (int i = 0; i < parts.length - 1; i++) {
            current = getProperty(current, parts[i]);
            if (current == null) {
                throw new OpenReflectException("Cannot navigate to path '" + path + "': " +
                        "intermediate property '" + parts[i] + "' is null");
            }
        }

        // Set the final property
        setProperty(current, parts[parts.length - 1], value);
    }

    /**
     * Checks if a path exists and is readable
     * 检查路径是否存在且可读
     *
     * @param bean the root bean | 根bean
     * @param path the property path | 属性路径
     * @return true if path is valid | 如果路径有效返回true
     */
    public static boolean hasPath(Object bean, String path) {
        if (bean == null || path == null) {
            return false;
        }

        String[] parts = parsePath(path);
        Object current = bean;

        for (String part : parts) {
            if (current == null) {
                return false;
            }

            Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(current.getClass());
            String propName = extractPropertyName(part);

            if (!descriptors.containsKey(propName)) {
                return false;
            }

            current = getProperty(current, part);
        }

        return true;
    }

    /**
     * Gets all values along a path
     * 获取路径上的所有值
     *
     * @param bean the root bean | 根bean
     * @param path the property path | 属性路径
     * @return list of values (including intermediate values) | 值列表（包括中间值）
     */
    public static List<Object> getPathValues(Object bean, String path) {
        if (bean == null) {
            return Collections.emptyList();
        }

        String[] parts = parsePath(path);
        List<Object> values = new ArrayList<>(parts.length + 1);
        Object current = bean;
        values.add(current);

        for (String part : parts) {
            if (current == null) {
                break;
            }
            current = getProperty(current, part);
            values.add(current);
        }

        return values;
    }

    /**
     * Copies a property from one path to another
     * 从一个路径复制属性到另一个路径
     *
     * @param source     the source bean | 源bean
     * @param sourcePath the source path | 源路径
     * @param target     the target bean | 目标bean
     * @param targetPath the target path | 目标路径
     */
    public static void copy(Object source, String sourcePath, Object target, String targetPath) {
        Object value = get(source, sourcePath);
        set(target, targetPath, value);
    }

    private static String[] parsePath(String path) {
        // Split by dots, but handle indexed properties
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);

            if (c == '.') {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        return parts.toArray(new String[0]);
    }

    private static Object getProperty(Object bean, String part) {
        // Check for indexed property (e.g., "items[0]")
        int indexStart = part.indexOf('[');
        if (indexStart >= 0) {
            String propName = part.substring(0, indexStart);
            int indexEnd = part.indexOf(']', indexStart);
            if (indexEnd < 0) {
                throw new OpenReflectException("Malformed indexed property (missing ']'): " + part);
            }
            String indexStr = part.substring(indexStart + 1, indexEnd);

            Object collection = getSimpleProperty(bean, propName);
            if (collection == null) {
                return null;
            }

            return getIndexed(collection, indexStr);
        }

        return getSimpleProperty(bean, part);
    }

    private static Object getSimpleProperty(Object bean, String propertyName) {
        Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(bean.getClass());
        PropertyDescriptor descriptor = descriptors.get(propertyName);

        if (descriptor == null || !descriptor.isReadable()) {
            throw new OpenReflectException("Property not found or not readable: " + propertyName);
        }

        return descriptor.getValue(bean);
    }

    private static void setProperty(Object bean, String part, Object value) {
        // Check for indexed property
        int indexStart = part.indexOf('[');
        if (indexStart >= 0) {
            String propName = part.substring(0, indexStart);
            int indexEnd = part.indexOf(']', indexStart);
            if (indexEnd < 0) {
                throw new OpenReflectException("Malformed indexed property (missing ']'): " + part);
            }
            String indexStr = part.substring(indexStart + 1, indexEnd);

            Object collection = getSimpleProperty(bean, propName);
            if (collection == null) {
                throw new OpenReflectException("Cannot set indexed property: collection is null");
            }

            setIndexed(collection, indexStr, value);
            return;
        }

        setSimpleProperty(bean, part, value);
    }

    private static void setSimpleProperty(Object bean, String propertyName, Object value) {
        Map<String, PropertyDescriptor> descriptors = OpenBean.getPropertyDescriptors(bean.getClass());
        PropertyDescriptor descriptor = descriptors.get(propertyName);

        if (descriptor == null || !descriptor.isWritable()) {
            throw new OpenReflectException("Property not found or not writable: " + propertyName);
        }

        descriptor.setValue(bean, value);
    }

    private static Object getIndexed(Object collection, String indexStr) {
        if (collection instanceof List<?> list) {
            int index = Integer.parseInt(indexStr);
            if (index < 0) {
                throw new OpenReflectException("Negative index not allowed: " + index);
            }
            return index < list.size() ? list.get(index) : null;
        }

        if (collection instanceof Object[] array) {
            int index = Integer.parseInt(indexStr);
            if (index < 0) {
                throw new OpenReflectException("Negative index not allowed: " + index);
            }
            return index < array.length ? array[index] : null;
        }

        if (collection instanceof Map<?, ?> map) {
            return map.get(indexStr);
        }

        throw new OpenReflectException("Cannot get indexed value from: " + collection.getClass());
    }

    @SuppressWarnings("unchecked")
    private static void setIndexed(Object collection, String indexStr, Object value) {
        if (collection instanceof List list) {
            int index = Integer.parseInt(indexStr);
            if (index < list.size()) {
                list.set(index, value);
            }
            return;
        }

        if (collection instanceof Object[] array) {
            int index = Integer.parseInt(indexStr);
            if (index < array.length) {
                array[index] = value;
            }
            return;
        }

        if (collection instanceof Map map) {
            map.put(indexStr, value);
            return;
        }

        throw new OpenReflectException("Cannot set indexed value on: " + collection.getClass());
    }

    private static String extractPropertyName(String part) {
        int indexStart = part.indexOf('[');
        return indexStart >= 0 ? part.substring(0, indexStart) : part;
    }
}
