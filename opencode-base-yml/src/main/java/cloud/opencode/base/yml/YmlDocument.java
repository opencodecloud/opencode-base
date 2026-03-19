package cloud.opencode.base.yml;

import cloud.opencode.base.yml.path.PathResolver;
import cloud.opencode.base.yml.path.YmlPath;

import java.util.*;

/**
 * YAML Document - Represents a parsed YAML document
 * YAML 文档 - 表示解析后的 YAML 文档
 *
 * <p>This class provides a wrapper around parsed YAML data with
 * convenient access methods.</p>
 * <p>此类提供解析后 YAML 数据的包装器，带有便捷的访问方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Typed value access (String, int, long, boolean, List, Map) - 类型化值访问</li>
 *   <li>Dot-notation path navigation - 点号路径导航</li>
 *   <li>Optional value support - 可选值支持</li>
 *   <li>Sub-document extraction - 子文档提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YmlDocument doc = YmlDocument.of(data);
 *
 * // Get values
 * String name = doc.getString("app.name");
 * int port = doc.getInt("server.port", 8080);
 *
 * // Navigate
 * YmlDocument server = doc.get("server");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (wraps mutable data structures) - 线程安全: 否（包装可变数据结构）</li>
 *   <li>Null-safe: Yes (returns defaults or empty for missing paths) - 空值安全: 是（缺失路径返回默认值或空值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlDocument {

    private final Object root;

    private YmlDocument(Object root) {
        this.root = root;
    }

    /**
     * Creates a document from parsed data.
     * 从解析的数据创建文档。
     *
     * @param data the parsed data | 解析的数据
     * @return the document | 文档
     */
    public static YmlDocument of(Object data) {
        return new YmlDocument(data);
    }

    /**
     * Creates an empty document.
     * 创建空文档。
     *
     * @return an empty document | 空文档
     */
    public static YmlDocument empty() {
        return new YmlDocument(new LinkedHashMap<>());
    }

    /**
     * Gets the root data.
     * 获取根数据。
     *
     * @return the root object | 根对象
     */
    public Object getRoot() {
        return root;
    }

    /**
     * Gets the root as a Map.
     * 获取根作为 Map。
     *
     * @return the root map | 根映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap() {
        if (root instanceof Map<?, ?> map) {
            return Collections.unmodifiableMap((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    /**
     * Gets the root as a List.
     * 获取根作为 List。
     *
     * @return the root list | 根列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> asList() {
        if (root instanceof List<?> list) {
            return Collections.unmodifiableList((List<T>) list);
        }
        return Collections.emptyList();
    }

    /**
     * Gets a value at the specified path.
     * 获取指定路径的值。
     *
     * @param path the path string | 路径字符串
     * @param <T>  the type parameter | 类型参数
     * @return the value | 值
     */
    public <T> T get(String path) {
        return PathResolver.get(root, path);
    }

    /**
     * Gets a value at the specified path with default.
     * 获取指定路径的值（带默认值）。
     *
     * @param path         the path string | 路径字符串
     * @param defaultValue the default value | 默认值
     * @param <T>          the type parameter | 类型参数
     * @return the value or default | 值或默认值
     */
    public <T> T get(String path, T defaultValue) {
        return PathResolver.get(root, path, defaultValue);
    }

    /**
     * Gets a sub-document at the specified path.
     * 获取指定路径的子文档。
     *
     * @param path the path string | 路径字符串
     * @return the sub-document | 子文档
     */
    public YmlDocument getDocument(String path) {
        Object value = PathResolver.resolve(root, YmlPath.of(path));
        return value != null ? YmlDocument.of(value) : empty();
    }

    /**
     * Gets a string value.
     * 获取字符串值。
     *
     * @param path the path | 路径
     * @return the string value | 字符串值
     */
    public String getString(String path) {
        return PathResolver.getString(root, path);
    }

    /**
     * Gets a string value with default.
     * 获取字符串值（带默认值）。
     *
     * @param path         the path | 路径
     * @param defaultValue the default | 默认值
     * @return the string value or default | 字符串值或默认值
     */
    public String getString(String path, String defaultValue) {
        return PathResolver.getString(root, path, defaultValue);
    }

    /**
     * Gets an integer value.
     * 获取整数值。
     *
     * @param path the path | 路径
     * @return the integer value | 整数值
     */
    public Integer getInt(String path) {
        return PathResolver.getInt(root, path);
    }

    /**
     * Gets an integer value with default.
     * 获取整数值（带默认值）。
     *
     * @param path         the path | 路径
     * @param defaultValue the default | 默认值
     * @return the integer value or default | 整数值或默认值
     */
    public int getInt(String path, int defaultValue) {
        return PathResolver.getInt(root, path, defaultValue);
    }

    /**
     * Gets a long value.
     * 获取长整数值。
     *
     * @param path the path | 路径
     * @return the long value | 长整数值
     */
    public Long getLong(String path) {
        return PathResolver.getLong(root, path);
    }

    /**
     * Gets a boolean value.
     * 获取布尔值。
     *
     * @param path the path | 路径
     * @return the boolean value | 布尔值
     */
    public Boolean getBoolean(String path) {
        return PathResolver.getBoolean(root, path);
    }

    /**
     * Gets a boolean value with default.
     * 获取布尔值（带默认值）。
     *
     * @param path         the path | 路径
     * @param defaultValue the default | 默认值
     * @return the boolean value or default | 布尔值或默认值
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return PathResolver.getBoolean(root, path, defaultValue);
    }

    /**
     * Gets a list value.
     * 获取列表值。
     *
     * @param path the path | 路径
     * @param <T>  the element type | 元素类型
     * @return the list | 列表
     */
    public <T> List<T> getList(String path) {
        return PathResolver.getList(root, path);
    }

    /**
     * Gets a map value.
     * 获取映射值。
     *
     * @param path the path | 路径
     * @return the map | 映射
     */
    public Map<String, Object> getMap(String path) {
        return PathResolver.getMap(root, path);
    }

    /**
     * Checks if a path exists.
     * 检查路径是否存在。
     *
     * @param path the path | 路径
     * @return true if exists | 如果存在则返回 true
     */
    public boolean has(String path) {
        return PathResolver.has(root, path);
    }

    /**
     * Gets an optional value.
     * 获取可选值。
     *
     * @param path the path | 路径
     * @param <T>  the type | 类型
     * @return optional value | 可选值
     */
    public <T> Optional<T> getOptional(String path) {
        return PathResolver.getOptional(root, path);
    }

    /**
     * Gets all keys at the root level.
     * 获取根级别的所有键。
     *
     * @return the keys | 键集合
     */
    public Set<String> keys() {
        if (root instanceof Map<?, ?> map) {
            return new LinkedHashSet<>(map.keySet().stream()
                .map(Object::toString)
                .toList());
        }
        return Collections.emptySet();
    }

    /**
     * Gets the size of the root element.
     * 获取根元素的大小。
     *
     * @return the size | 大小
     */
    public int size() {
        if (root instanceof Map<?, ?> map) {
            return map.size();
        } else if (root instanceof List<?> list) {
            return list.size();
        }
        return 0;
    }

    /**
     * Checks if the document is empty.
     * 检查文档是否为空。
     *
     * @return true if empty | 如果为空则返回 true
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return "YmlDocument{" + root + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YmlDocument that = (YmlDocument) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
