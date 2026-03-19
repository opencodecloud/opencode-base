package cloud.opencode.base.yml.path;

import cloud.opencode.base.yml.exception.YmlPathException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Path Resolver - Resolves paths in YAML data structures
 * 路径解析器 - 在 YAML 数据结构中解析路径
 *
 * <p>This class provides utilities for accessing nested values using path expressions.</p>
 * <p>此类提供使用路径表达式访问嵌套值的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dot-notation and array-index path resolution - 点号和数组索引路径解析</li>
 *   <li>Typed value access (String, int, long, boolean, List, Map) - 类型化值访问</li>
 *   <li>Default value and Optional support - 默认值和 Optional 支持</li>
 *   <li>Path existence check - 路径存在性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Object> data = OpenYml.load(yaml);
 *
 * // Get value at path
 * String port = PathResolver.get(data, "server.port");
 *
 * // Get with default
 * int timeout = PathResolver.get(data, "server.timeout", 30);
 *
 * // Check if path exists
 * boolean exists = PathResolver.has(data, "database.url");
 *
 * // Get optional value
 * Optional<String> value = PathResolver.getOptional(data, "optional.path");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null/default for null root or missing paths) - 空值安全: 是（空根或缺失路径返回 null/默认值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class PathResolver {

    private PathResolver() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Gets a value at the specified path.
     * 获取指定路径的值。
     *
     * @param root the root object (Map or List) | 根对象（Map 或 List）
     * @param path the path string | 路径字符串
     * @param <T>  the type parameter | 类型参数
     * @return the value at path | 路径处的值
     * @throws YmlPathException if path is not found | 如果未找到路径
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, String path) {
        Object result = resolve(root, YmlPath.of(path));
        if (result == null) {
            throw new YmlPathException(path);
        }
        return (T) result;
    }

    /**
     * Gets a value at the specified path with default.
     * 获取指定路径的值（带默认值）。
     *
     * @param root         the root object | 根对象
     * @param path         the path string | 路径字符串
     * @param defaultValue the default value | 默认值
     * @param <T>          the type parameter | 类型参数
     * @return the value at path or default | 路径处的值或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, String path, T defaultValue) {
        Object result = resolve(root, YmlPath.of(path));
        return result != null ? (T) result : defaultValue;
    }

    /**
     * Gets a value at the specified YmlPath.
     * 获取指定 YmlPath 的值。
     *
     * @param root the root object | 根对象
     * @param path the path | 路径
     * @param <T>  the type parameter | 类型参数
     * @return the value at path | 路径处的值
     * @throws YmlPathException if path is not found | 如果未找到路径
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object root, YmlPath path) {
        Object result = resolve(root, path);
        if (result == null) {
            throw new YmlPathException(path.toString());
        }
        return (T) result;
    }

    /**
     * Gets an optional value at the specified path.
     * 获取指定路径的可选值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @param <T>  the type parameter | 类型参数
     * @return optional containing value if found | 如果找到则包含值的 Optional
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getOptional(Object root, String path) {
        return Optional.ofNullable((T) resolve(root, YmlPath.of(path)));
    }

    /**
     * Checks if a path exists.
     * 检查路径是否存在。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return true if path exists | 如果路径存在则返回 true
     */
    public static boolean has(Object root, String path) {
        return resolve(root, YmlPath.of(path)) != null;
    }

    /**
     * Gets a string value at the specified path.
     * 获取指定路径的字符串值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return the string value | 字符串值
     */
    public static String getString(Object root, String path) {
        Object value = get(root, path);
        return value != null ? value.toString() : null;
    }

    /**
     * Gets a string value at the specified path with default.
     * 获取指定路径的字符串值（带默认值）。
     *
     * @param root         the root object | 根对象
     * @param path         the path string | 路径字符串
     * @param defaultValue the default value | 默认值
     * @return the string value or default | 字符串值或默认值
     */
    public static String getString(Object root, String path, String defaultValue) {
        Object value = resolve(root, YmlPath.of(path));
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Gets an integer value at the specified path.
     * 获取指定路径的整数值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return the integer value | 整数值
     */
    public static Integer getInt(Object root, String path) {
        Object value = resolve(root, YmlPath.of(path));
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }

    /**
     * Gets an integer value at the specified path with default.
     * 获取指定路径的整数值（带默认值）。
     *
     * @param root         the root object | 根对象
     * @param path         the path string | 路径字符串
     * @param defaultValue the default value | 默认值
     * @return the integer value or default | 整数值或默认值
     */
    public static int getInt(Object root, String path, int defaultValue) {
        Integer value = getInt(root, path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a long value at the specified path.
     * 获取指定路径的长整数值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return the long value | 长整数值
     */
    public static Long getLong(Object root, String path) {
        Object value = resolve(root, YmlPath.of(path));
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    /**
     * Gets a boolean value at the specified path.
     * 获取指定路径的布尔值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return the boolean value | 布尔值
     */
    public static Boolean getBoolean(Object root, String path) {
        Object value = resolve(root, YmlPath.of(path));
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * Gets a boolean value at the specified path with default.
     * 获取指定路径的布尔值（带默认值）。
     *
     * @param root         the root object | 根对象
     * @param path         the path string | 路径字符串
     * @param defaultValue the default value | 默认值
     * @return the boolean value or default | 布尔值或默认值
     */
    public static boolean getBoolean(Object root, String path, boolean defaultValue) {
        Boolean value = getBoolean(root, path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list value at the specified path.
     * 获取指定路径的列表值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @param <T>  the element type | 元素类型
     * @return the list | 列表
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(Object root, String path) {
        Object value = resolve(root, YmlPath.of(path));
        if (value instanceof List<?> list) {
            return (List<T>) list;
        }
        return null;
    }

    /**
     * Gets a map value at the specified path.
     * 获取指定路径的映射值。
     *
     * @param root the root object | 根对象
     * @param path the path string | 路径字符串
     * @return the map | 映射
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Object root, String path) {
        Object value = resolve(root, YmlPath.of(path));
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    /**
     * Resolves a path in the given root object.
     * 在给定的根对象中解析路径。
     *
     * @param root the root object | 根对象
     * @param path the path | 路径
     * @return the value at path, or null if not found | 路径处的值，如果未找到则返回 null
     */
    @SuppressWarnings("unchecked")
    public static Object resolve(Object root, YmlPath path) {
        if (root == null || path.isRoot()) {
            return root;
        }

        Object current = root;

        for (YmlPath.Segment segment : path.getSegments()) {
            if (current == null) {
                return null;
            }

            if (segment instanceof YmlPath.PropertySegment ps) {
                if (current instanceof Map<?, ?> map) {
                    current = map.get(ps.property());
                } else {
                    return null;
                }
            } else if (segment instanceof YmlPath.IndexSegment is) {
                if (current instanceof List<?> list) {
                    int index = is.index();
                    if (index >= 0 && index < list.size()) {
                        current = list.get(index);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return current;
    }
}
