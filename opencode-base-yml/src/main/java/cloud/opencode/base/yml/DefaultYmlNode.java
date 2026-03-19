package cloud.opencode.base.yml;

import cloud.opencode.base.yml.path.YmlPath;
import cloud.opencode.base.yml.path.PathResolver;

import java.util.*;

/**
 * Default YmlNode Implementation - Wraps raw YAML data as a node tree
 * 默认 YmlNode 实现 - 将原始 YAML 数据包装为节点树
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Wrap raw YAML data as a navigable node tree - 将原始 YAML 数据包装为可导航的节点树</li>
 *   <li>Type-safe value access (int, long, boolean, double, String) - 类型安全的值访问</li>
 *   <li>Dot-notation path navigation - 点号路径导航</li>
 *   <li>Object binding via YmlBinder - 通过 YmlBinder 进行对象绑定</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YmlNode node = DefaultYmlNode.of(data);
 * String name = node.get("user").get("name").asText();
 * int port = node.at("server.port").asInt(8080);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (immutable after creation, but wraps mutable data) - 线程安全: 否（创建后不可变，但包装的数据可变）</li>
 *   <li>Null-safe: Yes (returns NULL_NODE for missing keys) - 空值安全: 是（缺失键返回空节点）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class DefaultYmlNode implements YmlNode {

    private static final DefaultYmlNode NULL_NODE = new DefaultYmlNode(null);

    private final Object value;

    private DefaultYmlNode(Object value) {
        this.value = value;
    }

    /**
     * Creates a node from a raw value.
     * 从原始值创建节点。
     *
     * @param value the raw value | 原始值
     * @return the node | 节点
     */
    public static YmlNode of(Object value) {
        if (value == null) {
            return NULL_NODE;
        }
        return new DefaultYmlNode(value);
    }

    /**
     * Creates a null node.
     * 创建空节点。
     *
     * @return the null node | 空节点
     */
    public static YmlNode nullNode() {
        return NULL_NODE;
    }

    @Override
    public NodeType getType() {
        if (value == null) {
            return NodeType.NULL;
        }
        if (value instanceof Map<?, ?>) {
            return NodeType.MAPPING;
        }
        if (value instanceof List<?>) {
            return NodeType.SEQUENCE;
        }
        return NodeType.SCALAR;
    }

    @Override
    public String asText() {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public String asText(String defaultValue) {
        String text = asText();
        return text != null ? text : defaultValue;
    }

    @Override
    public int asInt() {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    @Override
    public int asInt(int defaultValue) {
        try {
            return asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public long asLong() {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(value.toString());
    }

    @Override
    public long asLong(long defaultValue) {
        try {
            return asLong();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public boolean asBoolean() {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return asBoolean();
    }

    @Override
    public double asDouble() {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    @Override
    public double asDouble(double defaultValue) {
        try {
            return asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public YmlNode get(String key) {
        if (value instanceof Map<?, ?> map) {
            Object child = ((Map<String, Object>) map).get(key);
            return of(child);
        }
        return NULL_NODE;
    }

    @Override
    public YmlNode get(int index) {
        if (value instanceof List<?> list) {
            if (index >= 0 && index < list.size()) {
                return of(list.get(index));
            }
        }
        return NULL_NODE;
    }

    @Override
    public YmlNode at(String path) {
        Object result = PathResolver.resolve(value, YmlPath.of(path));
        return of(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean has(String key) {
        if (value instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).containsKey(key);
        }
        return false;
    }

    @Override
    public int size() {
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof List<?> list) {
            return list.size();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> keys() {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashSet<>(((Map<String, Object>) map).keySet());
        }
        return Collections.emptySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<YmlNode> values() {
        if (value instanceof Map<?, ?> map) {
            List<YmlNode> nodes = new ArrayList<>();
            for (Object v : ((Map<String, Object>) map).values()) {
                nodes.add(of(v));
            }
            return nodes;
        }
        if (value instanceof List<?> list) {
            List<YmlNode> nodes = new ArrayList<>();
            for (Object v : list) {
                nodes.add(of(v));
            }
            return nodes;
        }
        return Collections.emptyList();
    }

    @Override
    public <T> T toObject(Class<T> clazz) {
        return cloud.opencode.base.yml.bind.YmlBinder.bind(value, clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        if (value instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        return Collections.emptyMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toList() {
        if (value instanceof List<?> list) {
            return new ArrayList<>((List<Object>) list);
        }
        return Collections.emptyList();
    }

    @Override
    public String toYaml() {
        return OpenYml.dump(value);
    }

    @Override
    public Object getRawValue() {
        return value;
    }

    @Override
    public Iterator<YmlNode> iterator() {
        return values().iterator();
    }

    @Override
    public String toString() {
        return "YmlNode{" + value + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultYmlNode that = (DefaultYmlNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
