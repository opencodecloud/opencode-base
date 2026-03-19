package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.convert.Convert;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean Path Utility Class - Nested property path access
 * Bean 路径访问工具 - 嵌套属性路径访问
 *
 * <p>Supports nested property access like user.address.city and array/List index access like items[0].name.</p>
 * <p>支持嵌套属性访问（如 user.address.city）和数组/List 索引访问（如 items[0].name）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Nested property access (user.address.city) - 嵌套属性访问</li>
 *   <li>Array/List index access (items[0]) - 数组/List 索引访问</li>
 *   <li>Map key access (map[key]) - Map 键访问</li>
 *   <li>Path validation and existence check - 路径验证和存在性检查</li>
 *   <li>Auto-create intermediate objects - 自动创建中间对象</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get nested value - 获取嵌套值
 * String city = BeanPath.get(user, "address.city", String.class);
 *
 * // Get array element - 获取数组元素
 * Item item = BeanPath.get(order, "items[0]", Item.class);
 *
 * // Set with auto-create - 自动创建设置
 * BeanPath.setWithCreate(user, "address.city", "Beijing");
 *
 * // Check path exists - 检查路径存在
 * boolean exists = BeanPath.exists(user, "address.city");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes (returns null on null path) - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class BeanPath {

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)]");
    private static final Pattern MAP_KEY_PATTERN = Pattern.compile("\\[([^\\]]+)]");

    private BeanPath() {
    }

    // ==================== 路径访问 ====================

    /**
     * Gets a property value by path | 通过路径获取属性值
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @return the value at path, or null | 路径上的值，或 null
     */
    public static Object get(Object bean, String path) {
        if (bean == null || path == null || path.isEmpty()) {
            return null;
        }

        List<PathSegment> segments = parsePath(path);
        Object current = bean;

        for (PathSegment segment : segments) {
            if (current == null) return null;
            current = getSegmentValue(current, segment);
        }

        return current;
    }

    /**
     * Gets a property value by path with type conversion | 通过路径获取属性值（带类型转换）
     *
     * @param <T> the target type | 目标类型
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @param targetType the target class | 目标类
     * @return the converted value | 转换后的值
     */
    public static <T> T get(Object bean, String path, Class<T> targetType) {
        Object value = get(bean, path);
        return Convert.convert(value, targetType);
    }

    /**
     * Safely gets the value as Optional | 安全获取（返回 Optional）
     *
     * @param <T> the target type | 目标类型
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @param targetType the target class | 目标类
     * @return an Optional containing the value | 包含值的 Optional
     */
    public static <T> Optional<T> getOptional(Object bean, String path, Class<T> targetType) {
        return Optional.ofNullable(get(bean, path, targetType));
    }

    /**
     * Sets a property value by path | 通过路径设置属性值
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @param value the value to set | 要设置的值
     */
    public static void set(Object bean, String path, Object value) {
        if (bean == null || path == null || path.isEmpty()) {
            return;
        }

        List<PathSegment> segments = parsePath(path);
        if (segments.isEmpty()) return;

        Object current = bean;
        for (int i = 0; i < segments.size() - 1; i++) {
            current = getSegmentValue(current, segments.get(i));
            if (current == null) {
                throw new IllegalStateException("Intermediate path element is null: " + segments.get(i).value());
            }
        }

        setSegmentValue(current, segments.get(segments.size() - 1), value);
    }

    /**
     * Sets a property value by path, auto-creating intermediate objects | 通过路径设置属性值（自动创建中间对象）
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @param value the value to set | 要设置的值
     */
    public static void setWithCreate(Object bean, String path, Object value) {
        if (bean == null || path == null || path.isEmpty()) {
            return;
        }

        List<PathSegment> segments = parsePath(path);
        if (segments.isEmpty()) return;

        Object current = bean;
        for (int i = 0; i < segments.size() - 1; i++) {
            PathSegment segment = segments.get(i);
            Object next = getSegmentValue(current, segment);
            if (next == null) {
                // 尝试创建中间对象
                next = createIntermediateObject(current, segment, segments.get(i + 1));
                if (next != null) {
                    setSegmentValue(current, segment, next);
                }
            }
            current = next;
            if (current == null) {
                throw new IllegalStateException("Cannot create intermediate path element: " + segment.value());
            }
        }

        setSegmentValue(current, segments.get(segments.size() - 1), value);
    }

    // ==================== 路径检查 ====================

    /**
     * Checks if the path exists | 检查路径是否存在
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @return true if the path exists | 路径存在返回 true
     */
    public static boolean exists(Object bean, String path) {
        try {
            Object value = get(bean, path);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the value at path is null | 检查路径值是否为 null
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @return true if value is null | 值为 null 返回 true
     */
    public static boolean isNull(Object bean, String path) {
        return get(bean, path) == null;
    }

    /**
     * Checks if the value at path is not null | 检查路径值是否非 null
     *
     * @param bean the target bean | 目标 Bean
     * @param path the property path | 属性路径
     * @return true if value is not null | 值非 null 返回 true
     */
    public static boolean isNotNull(Object bean, String path) {
        return get(bean, path) != null;
    }

    // ==================== 路径解析 ====================

    /**
     * Parses a path string into segments | 解析路径为段列表
     *
     * @param path the property path | 属性路径
     * @return the list of path segments | 路径段列表
     */
    public static List<PathSegment> parsePath(String path) {
        List<PathSegment> segments = new ArrayList<>();
        String[] parts = path.split("\\.");

        for (String part : parts) {
            Matcher indexMatcher = INDEX_PATTERN.matcher(part);
            int lastEnd = 0;

            while (indexMatcher.find()) {
                if (indexMatcher.start() > lastEnd) {
                    String propertyName = part.substring(lastEnd, indexMatcher.start());
                    segments.add(new PropertySegment(propertyName));
                }
                int index = Integer.parseInt(indexMatcher.group(1));
                segments.add(new IndexSegment("[" + index + "]", index));
                lastEnd = indexMatcher.end();
            }

            if (lastEnd == 0) {
                // 没有索引，检查是否有 Map key
                Matcher mapMatcher = MAP_KEY_PATTERN.matcher(part);
                if (mapMatcher.find() && mapMatcher.start() > 0) {
                    String propertyName = part.substring(0, mapMatcher.start());
                    segments.add(new PropertySegment(propertyName));
                    String key = mapMatcher.group(1);
                    segments.add(new MapKeySegment("[" + key + "]", key));
                } else {
                    segments.add(new PropertySegment(part));
                }
            } else if (lastEnd < part.length()) {
                segments.add(new PropertySegment(part.substring(lastEnd)));
            }
        }

        return segments;
    }

    /**
     * Gets the parent path | 获取路径的父路径
     *
     * @param path the property path | 属性路径
     * @return the parent path | 父路径
     */
    public static String getParentPath(String path) {
        int lastDot = path.lastIndexOf('.');
        int lastBracket = path.lastIndexOf('[');
        int lastSep = Math.max(lastDot, lastBracket);
        return lastSep > 0 ? path.substring(0, lastSep) : "";
    }

    /**
     * Gets the last segment of the path | 获取路径的最后一段
     *
     * @param path the property path | 属性路径
     * @return the last segment | 最后一段
     */
    public static String getLastSegment(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }

    /**
     * Concatenates path segments | 拼接路径
     *
     * @param segments the path segments | 路径段
     * @return the joined path | 拼接后的路径
     */
    public static String joinPath(String... segments) {
        return String.join(".", segments);
    }

    // ==================== 路径段类型 ====================

    /**
     * Path segment interface | 路径段接口
     */
    public sealed interface PathSegment permits PropertySegment, IndexSegment, MapKeySegment {
        /**
         * Returns the segment value | 返回段值
         *
         * @return the segment value | 段值
         */
        String value();
    }

    /**
     * Property segment | 属性段
     *
     * @param value the property name | 属性名
     */
    public record PropertySegment(String value) implements PathSegment {
    }

    /**
     * Index segment | 索引段
     *
     * @param value the raw segment text | 原始段文本
     * @param index the array/list index | 数组/列表索引
     */
    public record IndexSegment(String value, int index) implements PathSegment {
    }

    /**
     * Map key segment | Map 键段
     *
     * @param value the raw segment text | 原始段文本
     * @param key the map key | Map 键
     */
    public record MapKeySegment(String value, String key) implements PathSegment {
    }

    // ==================== 私有辅助方法 ====================

    private static Object getSegmentValue(Object obj, PathSegment segment) {
        if (segment instanceof PropertySegment ps) {
            return getPropertyValue(obj, ps.value());
        } else if (segment instanceof IndexSegment is) {
            return getIndexValue(obj, is.index());
        } else if (segment instanceof MapKeySegment ms) {
            return getMapValue(obj, ms.key());
        }
        return null;
    }

    private static void setSegmentValue(Object obj, PathSegment segment, Object value) {
        if (segment instanceof PropertySegment ps) {
            setPropertyValue(obj, ps.value(), value);
        } else if (segment instanceof IndexSegment is) {
            setIndexValue(obj, is.index(), value);
        } else if (segment instanceof MapKeySegment ms) {
            setMapValue(obj, ms.key(), value);
        }
    }

    private static Object getPropertyValue(Object obj, String propertyName) {
        try {
            PropertyDescriptor pd = OpenBean.getPropertyDescriptor(obj.getClass(), propertyName).orElse(null);
            if (pd != null && pd.isReadable()) {
                return pd.getValue(obj);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void setPropertyValue(Object obj, String propertyName, Object value) {
        try {
            PropertyDescriptor pd = OpenBean.getPropertyDescriptor(obj.getClass(), propertyName).orElse(null);
            if (pd != null && pd.isWritable()) {
                pd.setValue(obj, value);
            }
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static Object getIndexValue(Object obj, int index) {
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            return index < length ? Array.get(obj, index) : null;
        }
        if (obj instanceof List<?> list) {
            return index < list.size() ? list.get(index) : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void setIndexValue(Object obj, int index, Object value) {
        if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            if (index < 0 || index >= length) {
                throw new IllegalArgumentException("Array index out of bounds: " + index + " (length: " + length + ")");
            }
            Array.set(obj, index, value);
        } else if (obj instanceof List list) {
            if (index < 0 || index >= list.size()) {
                throw new IllegalArgumentException("List index out of bounds: " + index + " (size: " + list.size() + ")");
            }
            list.set(index, value);
        }
    }

    private static Object getMapValue(Object obj, String key) {
        if (obj instanceof Map<?, ?> map) {
            return map.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void setMapValue(Object obj, String key, Object value) {
        if (obj instanceof Map map) {
            map.put(key, value);
        }
    }

    private static Object createIntermediateObject(Object parent, PathSegment current, PathSegment next) {
        // 根据下一个段的类型决定创建什么对象
        if (next instanceof IndexSegment) {
            return new ArrayList<>();
        } else if (next instanceof MapKeySegment) {
            return new LinkedHashMap<>();
        } else {
            // 尝试从属性类型推断
            if (current instanceof PropertySegment ps) {
                PropertyDescriptor pd = OpenBean.getPropertyDescriptor(parent.getClass(), ps.value()).orElse(null);
                if (pd != null) {
                    try {
                        return pd.type().getDeclaredConstructor().newInstance();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }
}
