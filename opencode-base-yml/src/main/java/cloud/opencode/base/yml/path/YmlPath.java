package cloud.opencode.base.yml.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * YAML Path - Represents a path to a value in YAML document
 * YAML 路径 - 表示 YAML 文档中值的路径
 *
 * <p>This class represents a path using dot notation (e.g., "server.port")
 * and array index notation (e.g., "items[0].name").</p>
 * <p>此类使用点号表示法（如 "server.port"）和数组索引表示法（如 "items[0].name"）表示路径。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse dot-notation paths (e.g., "a.b.c") - 解析点号路径</li>
 *   <li>Array index support (e.g., "items[0].name") - 数组索引支持</li>
 *   <li>Programmatic path construction with child/index - 通过 child/index 程序化构建路径</li>
 *   <li>Parent path navigation - 父路径导航</li>
 *   <li>Sealed segment types (PropertySegment, IndexSegment) - 密封段类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse a path
 * YmlPath path = YmlPath.of("server.port");
 *
 * // Create path programmatically
 * YmlPath path = YmlPath.root()
 *     .child("server")
 *     .child("port");
 *
 * // Array access
 * YmlPath path = YmlPath.of("items[0].name");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: Yes (null/empty input returns root path) - 空值安全: 是（空输入返回根路径）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class YmlPath {

    private static final YmlPath ROOT = new YmlPath(Collections.emptyList());

    private final List<Segment> segments;

    private YmlPath(List<Segment> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    /**
     * Gets the root path.
     * 获取根路径。
     *
     * @return the root path | 根路径
     */
    public static YmlPath root() {
        return ROOT;
    }

    /**
     * Parses a path string.
     * 解析路径字符串。
     *
     * @param path the path string (e.g., "a.b.c" or "items[0].name") | 路径字符串
     * @return the parsed path | 解析后的路径
     */
    public static YmlPath of(String path) {
        if (path == null || path.isEmpty()) {
            return ROOT;
        }
        return new YmlPath(parseSegments(path));
    }

    /**
     * Creates a path from segments.
     * 从段创建路径。
     *
     * @param segments the path segments | 路径段
     * @return the path | 路径
     */
    public static YmlPath of(String... segments) {
        List<Segment> list = new ArrayList<>(segments.length);
        for (String segment : segments) {
            list.add(new PropertySegment(segment));
        }
        return new YmlPath(list);
    }

    /**
     * Creates a child path by appending a property.
     * 通过追加属性创建子路径。
     *
     * @param property the property name | 属性名称
     * @return the new path | 新路径
     */
    public YmlPath child(String property) {
        List<Segment> newSegments = new ArrayList<>(segments);
        newSegments.add(new PropertySegment(property));
        return new YmlPath(newSegments);
    }

    /**
     * Creates a child path by appending an array index.
     * 通过追加数组索引创建子路径。
     *
     * @param index the array index | 数组索引
     * @return the new path | 新路径
     */
    public YmlPath index(int index) {
        List<Segment> newSegments = new ArrayList<>(segments);
        newSegments.add(new IndexSegment(index));
        return new YmlPath(newSegments);
    }

    /**
     * Gets the parent path.
     * 获取父路径。
     *
     * @return the parent path, or root if already at root | 父路径，如果已在根则返回根
     */
    public YmlPath parent() {
        if (segments.isEmpty()) {
            return ROOT;
        }
        return new YmlPath(segments.subList(0, segments.size() - 1));
    }

    /**
     * Gets the path segments.
     * 获取路径段。
     *
     * @return the segments | 段列表
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * Gets the last segment.
     * 获取最后一段。
     *
     * @return the last segment, or null if root | 最后一段，如果是根则返回 null
     */
    public Segment getLastSegment() {
        return segments.isEmpty() ? null : segments.getLast();
    }

    /**
     * Checks if this is the root path.
     * 检查是否为根路径。
     *
     * @return true if root | 如果是根则返回 true
     */
    public boolean isRoot() {
        return segments.isEmpty();
    }

    /**
     * Gets the path depth.
     * 获取路径深度。
     *
     * @return the depth | 深度
     */
    public int depth() {
        return segments.size();
    }

    @Override
    public String toString() {
        if (segments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            if (segment instanceof PropertySegment ps) {
                if (i > 0) {
                    sb.append('.');
                }
                sb.append(ps.property());
            } else if (segment instanceof IndexSegment is) {
                sb.append('[').append(is.index()).append(']');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YmlPath ymlPath = (YmlPath) o;
        return Objects.equals(segments, ymlPath.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(segments);
    }

    private static List<Segment> parseSegments(String path) {
        List<Segment> result = new ArrayList<>();
        int i = 0;
        StringBuilder current = new StringBuilder();

        while (i < path.length()) {
            char c = path.charAt(i);

            if (c == '.') {
                if (!current.isEmpty()) {
                    result.add(new PropertySegment(current.toString()));
                    current.setLength(0);
                }
                i++;
            } else if (c == '[') {
                if (!current.isEmpty()) {
                    result.add(new PropertySegment(current.toString()));
                    current.setLength(0);
                }
                int end = path.indexOf(']', i);
                if (end < 0) {
                    throw new IllegalArgumentException("Invalid path: unclosed bracket at " + i);
                }
                String indexStr = path.substring(i + 1, end);
                try {
                    result.add(new IndexSegment(Integer.parseInt(indexStr)));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid array index: " + indexStr);
                }
                i = end + 1;
            } else {
                current.append(c);
                i++;
            }
        }

        if (!current.isEmpty()) {
            result.add(new PropertySegment(current.toString()));
        }

        return result;
    }

    /**
     * Path Segment - Base interface for path segments
     * 路径段 - 路径段的基础接口
     */
    public sealed interface Segment permits PropertySegment, IndexSegment {
    }

    /**
     * Property Segment - Represents a property access
     * 属性段 - 表示属性访问
     *
     * @param property the property name | 属性名称
     */
    public record PropertySegment(String property) implements Segment {
    }

    /**
     * Index Segment - Represents an array index access
     * 索引段 - 表示数组索引访问
     *
     * @param index the array index | 数组索引
     */
    public record IndexSegment(int index) implements Segment {
    }
}
