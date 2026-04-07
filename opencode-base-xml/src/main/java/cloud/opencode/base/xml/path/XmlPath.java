package cloud.opencode.base.xml.path;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML Path - Simplified dot-notation path access for XML documents
 * XML 路径 - XML 文档的简化点表示法路径访问
 *
 * <p>This utility class provides a simpler alternative to XPath for common
 * XML access patterns. Paths use dot notation to navigate through elements.</p>
 * <p>此工具类为常见的 XML 访问模式提供了比 XPath 更简单的替代方案。
 * 路径使用点表示法来导航元素。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dot-notation element navigation: "root.child.name" - 点表示法元素导航</li>
 *   <li>Index-based access: "root.child[2].name" (0-based) - 基于索引的访问（从0开始）</li>
 *   <li>Attribute access: "root.child.@attr" (@ prefix) - 属性访问（@ 前缀）</li>
 *   <li>Multiple element retrieval: getElements for lists - 多元素检索</li>
 *   <li>Typed value access (int, boolean) with defaults - 类型化值访问（int、boolean）带默认值</li>
 *   <li>Path existence checking - 路径存在性检查</li>
 *   <li>Value setting with intermediate element creation - 值设置并自动创建中间元素</li>
 * </ul>
 *
 * <p><strong>Path Syntax | 路径语法:</strong></p>
 * <ul>
 *   <li>{@code "root.child.name"} — navigate by element name | 按元素名导航</li>
 *   <li>{@code "root.child[2].name"} — index-based access (0-based) | 基于索引的访问（从0开始）</li>
 *   <li>{@code "root.child.@attr"} — attribute access (@ prefix) | 属性访问（@ 前缀）</li>
 *   <li>{@code "root.items.item"} with getElements → returns all {@code item} children | 与 getElements 配合返回所有子元素</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * XmlDocument doc = XmlDocument.parse("<config><db><host>localhost</host></db></config>");
 *
 * // Simple path access
 * String host = XmlPath.getString(doc, "config.db.host"); // "localhost"
 *
 * // With default value
 * int port = XmlPath.getInt(doc, "config.db.port", 3306); // 3306
 *
 * // Attribute access
 * String id = XmlPath.getAttribute(doc, "config.db.@id");
 *
 * // Check existence
 * boolean exists = XmlPath.exists(doc, "config.db.host"); // true
 *
 * // Set value (creates intermediate elements)
 * XmlPath.set(doc, "config.db.port", "5432");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(d) where d = path depth - 时间复杂度: O(d)，d 为路径深度</li>
 *   <li>Space complexity: O(d) for path segment splitting - 空间复杂度: O(d)，路径段分割</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (null inputs throw NullPointerException) - 空值安全: 否（null 输入抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public final class XmlPath {

    /** Pattern to match indexed segments like "name[2]". */
    private static final Pattern INDEX_PATTERN = Pattern.compile("^(.+)\\[(\\d+)]$");

    /** Compiled dot pattern for path splitting. */
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    /** Maximum path depth to prevent unbounded element creation. */
    private static final int MAX_PATH_DEPTH = 128;

    private XmlPath() {
        // Utility class
    }

    // ==================== getString | 获取字符串 ====================

    /**
     * Gets the text value at the specified path.
     * 获取指定路径处的文本值。
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return the text value, or null if not found | 文本值，如果未找到则返回 null
     */
    public static String getString(XmlDocument doc, String path) {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        XmlElement root = doc.getRoot();
        if (root == null) {
            return null;
        }

        // Strip root element name from path if it matches
        String relativePath = stripRootSegment(root, path);
        if (relativePath == null) {
            return null;
        }
        if (relativePath.isEmpty()) {
            return root.getText();
        }
        return getString(root, relativePath);
    }

    /**
     * Gets the text value at the specified path relative to an element.
     * 获取相对于元素的指定路径处的文本值。
     *
     * @param element the starting element | 起始元素
     * @param path    the dot-notation path | 点表示法路径
     * @return the text value, or null if not found | 文本值，如果未找到则返回 null
     */
    public static String getString(XmlElement element, String path) {
        Objects.requireNonNull(element, "Element must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        String[] segments = DOT_PATTERN.split(path);
        XmlElement current = element;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            // Last segment: attribute access
            if (i == segments.length - 1 && segment.startsWith("@")) {
                return current.getAttribute(segment.substring(1));
            }

            // Navigate to child
            current = resolveSegment(current, segment);
            if (current == null) {
                return null;
            }
        }

        return current.getText();
    }

    // ==================== getOptional | 获取可选值 ====================

    /**
     * Gets an optional text value at the specified path.
     * 获取指定路径处的可选文本值。
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return an Optional containing the text value, or empty if not found | 包含文本值的 Optional，如果未找到则为空
     */
    public static Optional<String> getOptional(XmlDocument doc, String path) {
        return Optional.ofNullable(getString(doc, path));
    }

    // ==================== getString with default | 获取字符串带默认值 ====================

    /**
     * Gets the text value at the specified path, or the default value if not found.
     * 获取指定路径处的文本值，如果未找到则返回默认值。
     *
     * @param doc          the XML document | XML 文档
     * @param path         the dot-notation path | 点表示法路径
     * @param defaultValue the default value | 默认值
     * @return the text value or default | 文本值或默认值
     */
    public static String getString(XmlDocument doc, String path, String defaultValue) {
        String value = getString(doc, path);
        return value != null ? value : defaultValue;
    }

    // ==================== Typed Getters | 类型化获取器 ====================

    /**
     * Gets an integer value at the specified path, or the default if not found or not parseable.
     * 获取指定路径处的整数值，如果未找到或无法解析则返回默认值。
     *
     * @param doc          the XML document | XML 文档
     * @param path         the dot-notation path | 点表示法路径
     * @param defaultValue the default value | 默认值
     * @return the int value or default | 整数值或默认值
     */
    public static int getInt(XmlDocument doc, String path, int defaultValue) {
        String value = getString(doc, path);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a boolean value at the specified path, or the default if not found.
     * 获取指定路径处的布尔值，如果未找到则返回默认值。
     *
     * <p>"true" (case-insensitive) returns true; anything else returns the default.</p>
     * <p>"true"（不区分大小写）返回 true；其他值返回默认值。</p>
     *
     * @param doc          the XML document | XML 文档
     * @param path         the dot-notation path | 点表示法路径
     * @param defaultValue the default value | 默认值
     * @return the boolean value or default | 布尔值或默认值
     */
    public static boolean getBoolean(XmlDocument doc, String path, boolean defaultValue) {
        String value = getString(doc, path);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    // ==================== getAttribute | 获取属性 ====================

    /**
     * Gets an attribute value using @-notation path.
     * 使用 @-表示法路径获取属性值。
     *
     * <p>The last segment of the path must start with "@" to indicate an attribute.
     * For example, "users.user.@id" gets the "id" attribute of the "user" element.</p>
     * <p>路径的最后一段必须以 "@" 开头表示属性。
     * 例如，"users.user.@id" 获取 "user" 元素的 "id" 属性。</p>
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path ending with @attrName | 以 @attrName 结尾的点表示法路径
     * @return the attribute value, or null if not found | 属性值，如果未找到则返回 null
     */
    public static String getAttribute(XmlDocument doc, String path) {
        return getString(doc, path);
    }

    // ==================== getElement | 获取元素 ====================

    /**
     * Gets the element at the specified path.
     * 获取指定路径处的元素。
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return the element, or null if not found | 元素，如果未找到则返回 null
     */
    public static XmlElement getElement(XmlDocument doc, String path) {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        XmlElement root = doc.getRoot();
        if (root == null) {
            return null;
        }

        String relativePath = stripRootSegment(root, path);
        if (relativePath == null) {
            return null;
        }
        if (relativePath.isEmpty()) {
            return root;
        }
        return navigateToElement(root, relativePath);
    }

    // ==================== getElements | 获取元素列表 ====================

    /**
     * Gets all matching elements at the specified path.
     * 获取指定路径处的所有匹配元素。
     *
     * <p>The last segment of the path is used to match multiple children.
     * For example, "root.items.item" returns all "item" children under "items".</p>
     * <p>路径的最后一段用于匹配多个子元素。
     * 例如，"root.items.item" 返回 "items" 下的所有 "item" 子元素。</p>
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return the list of matching elements | 匹配元素列表
     */
    public static List<XmlElement> getElements(XmlDocument doc, String path) {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        XmlElement root = doc.getRoot();
        if (root == null) {
            return List.of();
        }

        String relativePath = stripRootSegment(root, path);
        if (relativePath == null || relativePath.isEmpty()) {
            return List.of();
        }

        String[] segments = DOT_PATTERN.split(relativePath);
        XmlElement current = root;

        // Navigate to the parent of the last segment
        for (int i = 0; i < segments.length - 1; i++) {
            current = resolveSegment(current, segments[i]);
            if (current == null) {
                return List.of();
            }
        }

        // Get all children matching the last segment
        String lastSegment = segments[segments.length - 1];
        return current.getChildren(lastSegment);
    }

    // ==================== getStrings | 获取字符串列表 ====================

    /**
     * Gets text values of all matching elements at the specified path.
     * 获取指定路径处所有匹配元素的文本值。
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return the list of text values | 文本值列表
     */
    public static List<String> getStrings(XmlDocument doc, String path) {
        List<XmlElement> elements = getElements(doc, path);
        List<String> result = new ArrayList<>(elements.size());
        for (XmlElement element : elements) {
            result.add(element.getText());
        }
        return result;
    }

    // ==================== exists | 检查存在性 ====================

    /**
     * Checks whether a path exists in the document.
     * 检查文档中是否存在指定路径。
     *
     * @param doc  the XML document | XML 文档
     * @param path the dot-notation path | 点表示法路径
     * @return true if the path exists | 如果路径存在则返回 true
     */
    public static boolean exists(XmlDocument doc, String path) {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        XmlElement root = doc.getRoot();
        if (root == null) {
            return false;
        }

        String relativePath = stripRootSegment(root, path);
        if (relativePath == null) {
            return false;
        }
        if (relativePath.isEmpty()) {
            return true;
        }

        String[] segments = DOT_PATTERN.split(relativePath);
        XmlElement current = root;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            // Attribute check
            if (i == segments.length - 1 && segment.startsWith("@")) {
                return current.hasAttribute(segment.substring(1));
            }

            current = resolveSegment(current, segment);
            if (current == null) {
                return false;
            }
        }

        return true;
    }

    // ==================== set | 设置值 ====================

    /**
     * Sets the value at the specified path, creating intermediate elements as needed.
     * 设置指定路径处的值，根据需要创建中间元素。
     *
     * <p>If any intermediate elements do not exist, they will be created automatically.</p>
     * <p>如果任何中间元素不存在，将自动创建。</p>
     *
     * @param doc   the XML document | XML 文档
     * @param path  the dot-notation path | 点表示法路径
     * @param value the value to set | 要设置的值
     */
    public static void set(XmlDocument doc, String path, String value) {
        Objects.requireNonNull(doc, "Document must not be null");
        Objects.requireNonNull(path, "Path must not be null");

        XmlElement root = doc.getRoot();
        if (root == null) {
            throw new IllegalStateException("Document has no root element");
        }

        String relativePath = stripRootSegment(root, path);
        if (relativePath == null || relativePath.isEmpty()) {
            root.setText(value);
            return;
        }

        String[] segments = DOT_PATTERN.split(relativePath);
        if (segments.length > MAX_PATH_DEPTH) {
            throw new IllegalArgumentException("Path depth " + segments.length
                + " exceeds maximum " + MAX_PATH_DEPTH);
        }
        XmlElement current = root;

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            // Last segment: set value or attribute
            if (i == segments.length - 1) {
                if (segment.startsWith("@")) {
                    current.setAttribute(segment.substring(1), value);
                } else {
                    XmlElement child = resolveSegment(current, segment);
                    if (child == null) {
                        child = current.addChild(segment);
                    }
                    child.setText(value);
                }
                return;
            }

            // Navigate or create intermediate element
            XmlElement next = resolveSegment(current, segment);
            if (next == null) {
                next = current.addChild(segment);
            }
            current = next;
        }
    }

    // ==================== Internal Helpers | 内部辅助方法 ====================

    /**
     * Resolves a single path segment (with optional index) to a child element.
     * 将单个路径段（带可选索引）解析为子元素。
     */
    private static XmlElement resolveSegment(XmlElement parent, String segment) {
        Matcher matcher = INDEX_PATTERN.matcher(segment);
        if (matcher.matches()) {
            String name = matcher.group(1);
            int index;
            try {
                index = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                // Index too large to parse — treat as not found
                return null;
            }
            List<XmlElement> children = parent.getChildren(name);
            if (index >= 0 && index < children.size()) {
                return children.get(index);
            }
            return null;
        }
        return parent.getChild(segment);
    }

    /**
     * Strips the root element name from the path if it matches.
     * 如果路径的第一段与根元素名匹配，则去除它。
     *
     * @return the remaining path, empty string if only root matched, or null if root doesn't match
     */
    private static String stripRootSegment(XmlElement root, String path) {
        String rootName = root.getName();
        if (path.equals(rootName)) {
            return "";
        }
        if (path.startsWith(rootName + ".")) {
            return path.substring(rootName.length() + 1);
        }
        // Path doesn't start with root name — treat as relative to root
        return path;
    }

    /**
     * Navigates to an element following the full path.
     * 沿完整路径导航到元素。
     */
    private static XmlElement navigateToElement(XmlElement root, String path) {
        String[] segments = DOT_PATTERN.split(path);
        XmlElement current = root;

        for (String segment : segments) {
            current = resolveSegment(current, segment);
            if (current == null) {
                return null;
            }
        }

        return current;
    }
}
